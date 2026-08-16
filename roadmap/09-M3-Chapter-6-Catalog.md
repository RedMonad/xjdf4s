# M3 — Полный каталог ресурсов главы 6

Статус: `[ ]` (предусловие: ADR по ResourcePayload representation принят)

## Инвентаризация
Инструмент читает markdown-таблицы `reference/xjdf/6 – Resources.md` и строит
отчёт: Table | Resource | Attribute/Element | XJDF type | Cardinality |
Version note | Scala mapping | Validation | Codec | Test.

Карта типов (Appendix A, Table A.1):
| XJDF DATA TYPE | Scala |
| --- | --- |
| NMTOKEN / NMTOKENS / string | `Option[NmToken]` / `Option[NmTokens]` / `Option[XjdfString]` |
| ID / IDREF / IDREFS | `Option[Id]` / `Option[IdRef]` / `Option[IdRefs]` |
| float / integer | по ADR числового wire-домена |
| enumeration(s) | закрытый enum по «Allowed values are/is from», иначе `NmToken`+`Catalog` |

Кардинальность: `? → Option`, `* → Chain`, `+ → NonEmptyChain`.
Сгенерированный код — черновик, не норматив. Prose-ограничения SHALL, release
notes и JSON Exceptions проверяются человеком.

## Вертикальные срезы
Порядок: prepress/content → layout/imposition → printing/color →
finishing/binding → packing/delivery → device/scheduling/quality → остаток.
Один PR не добавляет десятки непроверенных case-классов. Каждый ресурс проходит
шаблон вертикального среза из M1.6, уже включая кодеки.

## DoD M3
- 100% таблиц главы 6 классифицированы: Implemented / Not Applicable /
  Deliberately Deferred с причиной.
- Каждый Implemented-ресурс имеет domain + validation + XML + JSON тесты.