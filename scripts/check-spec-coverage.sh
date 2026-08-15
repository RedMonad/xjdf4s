#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# check-spec-coverage.sh — консистентность реестра покрытия спецификации.
#
# Проверяет, что docs/SPEC-COVERAGE.md согласован с кодом и нормативными
# текстами reference/xjdf/*. Запускается в CI (M1.0-1) и переиспользуется
# генератором отчёта M3. Без внешних зависимостей, только grep/sed/awk/sort.
#
# Проверки:
#   1. Каждая ссылка `Table N.M` в modules/**/*.scala, docs/* и README.md
#      существует в reference/xjdf/* (заголовки `**Table N.M: …**` глав 1–9
#      и Appendix, `### Table N.M …` главы 3).
#   2. Реестр содержит три обязательных раздела.
#   3. Каждый Scala-тип из колонки «Scala type» объявлен в modules/core
#      (доменный тип без нормативной ссылки / выдуманный тип).
#   4. Каждый `case class`/`enum`, объявленный в resources/* и intents/*,
#      имеет строку реестра (новый payload без строки покрытия).
#   5. Кардинальность — только из словаря спецификации: ?, *, +, 1.
#   6. Строка со статусом Implemented обязана иметь статус валидации/тестов:
#      Validation=✅ или Domain tests=✅, либо явную пометку
#      «container» в Notes (реализованное поле без validation/test-статуса).
#   7. Version notes: если в нормативном тексте таблицы есть пометки
#      `New in XJDF 2.x`, хотя бы одна строка этой таблицы обязана нести
#      `New in XJDF` в Notes (потерянная version note).
#
# Выход: 0 — все проверки прошли; 1 — есть нарушения. Сводка покрытия
# печатается в stdout всегда.
# -----------------------------------------------------------------------------
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

COVERAGE="docs/SPEC-COVERAGE.md"
REF="reference/xjdf"
fail=0

err() { printf 'SPEC-COVERAGE-CHECK: %s\n' "$*" >&2; fail=1; }

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

# ---------------------------------------------------------------------------
# 1. Известные таблицы: `**Table N.M: …**` (главы 1–9, Appendix A) и
#    `### Table N.M …` (глава 3). Номера нормализуются к `N.M`.
# ---------------------------------------------------------------------------
grep -rhoE '^[*][*]Table [A-Z]?[0-9]+\.[0-9]+' "$REF"/*.md \
  | sed -E 's/^[*][*]Table //' | sort -u > "$tmp/known"
grep -rhoE '^#+ ?Table [A-Z]?[0-9]+\.[0-9]+' "$REF"/*.md \
  | sed -E 's/^#+ ?Table //' | sort -u >> "$tmp/known"
sort -u "$tmp/known" -o "$tmp/known"

if [ ! -s "$tmp/known" ]; then
  err "no table headers found in $REF — is the normative corpus present?"
fi

# ---------------------------------------------------------------------------
# 2. Ссылки `Table N.M` в коде и документах обязаны существовать.
# ---------------------------------------------------------------------------
grep -rhoE 'Table [A-Z]?[0-9]+\.[0-9]+' modules docs README.md 2>/dev/null \
  | sed -E 's/Table //' | sort -u > "$tmp/used"
while IFS= read -r t; do
  grep -qxF "$t" "$tmp/known" \
    || err "reference to non-existent Table $t (no header in $REF/*.md)"
done < "$tmp/used"

# ---------------------------------------------------------------------------
# 3. Реестр обязан существовать и содержать три обязательных раздела.
# ---------------------------------------------------------------------------
[ -f "$COVERAGE" ] || { err "missing $COVERAGE"; }
if [ -f "$COVERAGE" ]; then
  for section in "## Resources (Chapter 6)" "## Intents (Chapter 4)" "## Deliberate Deviations"; do
    grep -qF "$section" "$COVERAGE" || err "missing mandatory section '$section' in $COVERAGE"
  done
fi

# ---------------------------------------------------------------------------
# Строки реестра: обе таблицы покрытия используют вид `| §… | Table N.M | …`.
# ---------------------------------------------------------------------------
grep -E '^\| §' "$COVERAGE" | sed 's/^| //; s/ |$//' > "$tmp/rows" || true

if [ ! -s "$tmp/rows" ]; then
  err "no coverage rows found in $COVERAGE (expected lines starting with '| §')"
fi

# ---------------------------------------------------------------------------
# Вспомогательные функции разбора.
# ---------------------------------------------------------------------------
cell() { # $1 = line, $2 = 1-based column
  printf '%s\n' "$1" | awk -F'|' -v c="$2" '{gsub(/^ +| +$/, "", $c); print $c}'
}

# Есть ли у таблицы пометки `New in XJDF` в нормативном тексте.
table_has_new_in() { # $1 = номер таблицы
  local pattern
  pattern="$(printf '%s' "$1" | sed 's/\./\\./g')"
  for f in "$REF"/*.md; do
    local line=""
    local end=""
    line="$(grep -nE "^[*][*]Table $pattern:|^#+ ?Table $pattern:" "$f" | head -1 | cut -d: -f1)"
    [ -n "$line" ] || continue
    end="$(awk -v s="$line" '
      NR > s && (/^[*][*]Table [A-Z]?[0-9]+\.[0-9]+:/ || /^#+ /) { print NR; exit }' "$f")"
    end="${end:-999999}"
    if sed -n "$line,$((end - 1))p" "$f" | grep -q 'New in XJDF'; then
      return 0
    fi
    return 1
  done
  return 1
}

# Объявлен ли тип в modules/core.
type_declared() { # $1 = имя типа
  grep -rqE "(final )?(case class|enum|opaque type|trait|class) $1([ (<:{]|\$|$)" \
    modules/core/src/main/scala
}

# ---------------------------------------------------------------------------
# 4. Построчная проверка реестра + сводка.
# ---------------------------------------------------------------------------
new_in_tables="$tmp/newin"; : > "$new_in_tables"
types_in_rows="$tmp/types_in_rows"; : > "$types_in_rows"

res_rows=0; res_impl=0; res_planned=0
int_rows=0; int_impl=0; int_planned=0

while IFS= read -r row; do
  sec="$(cell "$row" 1)"
  tbl="$(cell "$row" 2)"
  elem="$(cell "$row" 3)"
  scala_type="$(cell "$row" 4)"
  card="$(cell "$row" 5)"
  validation="$(cell "$row" 6)"
  tests="$(cell "$row" 7)"
  status="$(cell "$row" 10)"
  notes="$(cell "$row" 11)"

  case "$sec" in
    §6*) res_rows=$((res_rows + 1)) ;;
    §4*) int_rows=$((int_rows + 1)) ;;
  esac

  # 4.1 Таблица строки существует (уже проверено на шаге 2, дублируем для
  #     ясности диагностики).
  t="$(printf '%s\n' "$tbl" | sed -nE 's/.*Table ([A-Z]?[0-9]+\.[0-9]+).*/\1/p')"
  if [ -z "$t" ]; then
    err "row '$elem' has no parseable Table cell ('$tbl')"
  elif ! grep -qxF "$t" "$tmp/known"; then
    err "row '$elem' references non-existent Table $t"
  fi

  # 4.2 Scala-типы строки объявлены в коде.
  backticked="$(printf '%s\n' "$scala_type" | grep -oE '`[A-Za-z0-9_]+`' | tr -d '`' || true)"
  if [ -z "$backticked" ]; then
    err "row '$elem' has no Scala type ('$scala_type')"
  fi
  for ty in $backticked; do
    printf '%s\n' "$ty" >> "$types_in_rows"
    type_declared "$ty" || err "row '$elem': type $ty is not declared in modules/core"
  done

  # 4.3 Кардинальность — только словарь спецификации (Table 1.2).
  card_plain="$(printf '%s\n' "$card" | tr -d '\`')"
  case "$card_plain" in
    "?" | "*" | "+" | "1") ;;
    *) err "row '$elem': cardinality '$card' is not from the spec vocabulary (?, *, +, 1)" ;;
  esac

  # 4.4 Статусные колонки используют ✅/❌.
  for col in "$validation" "$tests"; do
    case "$col" in
      "✅" | "❌") ;;
      *) err "row '$elem': status cell '$col' must be ✅ or ❌" ;;
    esac
  done

  # 4.5 Implemented-строка без validation/test-статуса.
  if [ "$status" = "Implemented" ] && [ "$validation" = "❌" ] && [ "$tests" = "❌" ]; then
    case "$notes" in
      *container*) ;;
      *) err "row '$elem': Implemented without validation/test status (or 'container' note)" ;;
    esac
  fi

  # 4.6 Сводка + version notes (требование заметки проверяется ниже,
  #     по разделу «Version notes»).
  if [ -n "$t" ] && table_has_new_in "$t"; then
    printf '%s\n' "$t" >> "$new_in_tables"
  fi
  case "$status" in
    Implemented)
      case "$sec" in §6*) res_impl=$((res_impl + 1)) ;; §4*) int_impl=$((int_impl + 1)) ;; esac ;;
    Planned | "codec-only" | "codec-only (M2)" | "Not modelled")
      case "$sec" in §6*) res_planned=$((res_planned + 1)) ;; §4*) int_planned=$((int_planned + 1)) ;; esac ;;
  esac
done < "$tmp/rows"

# ---------------------------------------------------------------------------
# 5. Обратная проверка: каждый payload-тип из resources/* и intents/* имеет
#    строку реестра.
# ---------------------------------------------------------------------------
grep -rhoE '^(final )?(case class|enum) [A-Z][A-Za-z0-9]*' \
  modules/core/src/main/scala/xjdf4s/resources/*.scala \
  modules/core/src/main/scala/xjdf4s/intents/*.scala \
  | sed -E 's/.*(case class|enum) //' | sort -u > "$tmp/declared_payloads"
while IFS= read -r ty; do
  grep -qxF "$ty" "$types_in_rows" \
    || err "domain type $ty (resources/* or intents/*) has no coverage row"
done < "$tmp/declared_payloads"

# ---------------------------------------------------------------------------
# 6. Version notes: каждая покрытая таблица с пометками `New in XJDF` обязана
#    иметь запись в разделе «Version notes» реестра (потерянная version note).
# ---------------------------------------------------------------------------
grep -E '^\| Table [A-Z]?[0-9]+\.[0-9]+ \|' "$COVERAGE" \
  | sed -nE 's/^\| Table ([A-Z]?[0-9]+\.[0-9]+) \|.*/\1/p' \
  | sort -u > "$tmp/noted" || true
sort -u "$new_in_tables" -o "$new_in_tables"
comm -23 "$new_in_tables" "$tmp/noted" | while IFS= read -r t; do
  err "Table $t carries 'New in XJDF' markers in the spec but has no Version notes entry"
done

# ---------------------------------------------------------------------------
# Сводка покрытия (вычисляется, не хранится в README).
# ---------------------------------------------------------------------------
dev_rows="$(grep -c '^| `' "$COVERAGE" || true)"
printf '\nCoverage summary (computed by %s):\n' "$(basename "${BASH_SOURCE[0]}")"
printf '  Resources (Chapter 6): %d rows (%d Implemented, %d Planned/other)\n' \
  "$res_rows" "$res_impl" "$res_planned"
printf '  Intents (Chapter 4):   %d rows (%d Implemented, %d Planned/other)\n' \
  "$int_rows" "$int_impl" "$int_planned"
printf '  Deliberate deviations: %d rows\n' "$dev_rows"
printf '  Spec tables referenced: %d (all exist in reference/xjdf)\n' "$(wc -l < "$tmp/used")"

if [ "$fail" -ne 0 ]; then
  printf '\nRESULT: FAIL\n' >&2
  exit 1
fi
printf '\nRESULT: OK\n'
