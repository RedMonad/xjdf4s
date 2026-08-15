# ADR-0002 — Слои валидации и разрыв цикла зависимостей

- **Статус:** принято (реализовано в PR-9, M1.4-1)
- **Дата:** 2026-08-15
- **Задача:** M1.4-1 (PR-9); дедлайн по ROADMAP §6 — «до M1.4-1»
- **Закрывает находку:** N-21
- **Связанные ADR:** ADR-0003 (форма локальных правил), ADR-0006 (errors vs warnings)

---

## Context

На срезе M1.3 цикл из 4 файлов `Validation → Product → Ticket → Patch →
Validation` нарушал Acyclic Dependencies Principle. Причина — все четыре файла
используют тип `Issue`, а `Validation.scala` одновременно определяет `Issue` и
зависит от доменных агрегатов.

## Decision

Вынести фундамент валидации в независимый файл с Fan-Out 0:

```
model/ValidationTypes.scala    Issue, IssueCode, SeverityClass, XPath,
                               trait DomainRule[-A],
                               type ValidationResult[A] = ValidatedNec[Issue, A]
model/Product.scala            зависит только от ValidationTypes
model/Ticket.scala             не зависит от реализации Patch
model/Patch.scala              зависит от Ticket и ValidationTypes
model/TicketValidator.scala    зависит от всей доменной модели, агрегирует правила
```

```
ДО:   [Validation] → [Product] → [Ticket] → [Patch] → [Validation]   ЦИКЛ

ПОСЛЕ:            [ValidationTypes]   (Fan-Out 0, фундамент)
                    ▲     ▲     ▲
              [Product] [Ticket] [Patch]
                    ▲     ▲     ▲
                   [TicketValidator]
```

Искусственный trait ради метрики не вводится: зависимости должны следовать
ответственности.

## Alternatives

| Вариант | Суть | Оценка |
| --- | --- | --- |
| Оставить цикл | «Цикл не мешает компиляции» | Отклонено: цикл цементирует связность валидатора и домена, расширение validator/codecs становится дорогим |
| Общий trait ради метрики | Ввести интерфейс-посредник только чтобы «разорвать» ребро | Отклонено: маскирует зависимость, не убирает её |
| **Fan-Out 0 фундамент + корневой агрегатор** | Принято | Зависимости следуют ответственности; метрика честна |

## Consequences

**Критерий приёмки (выполнен, PR-9).** Повторный прогон анализатора
зависимостей тем же алгоритмом даёт 0 циклов; межмодульный граф остаётся
прежним (`core` без зависимостей на `laws`/`examples`).

**Реализация (PR-9).** Список содержимого `ValidationTypes.scala` выполнен
буквально, по решению владельца: `IssueCode`, `SeverityClass` и `XPath`
перенесены из `prim` (`Tokens.scala`, `Enums.scala`) в слой валидации; alias
`ValidationResult[A]` введён. Для нуля циклов `Ticket.scala` освобождён от
ссылок на валидатор и на `Patch`: `XJDF.validate`/`validateReport` стали
extension-методами в `TicketValidator.scala`, `XJDF.withPatch` — в
`Patch.scala`. Migration-алиасы не понадобились: типы остались в пакете
`xjdf4s.model`, call sites обновлены импортами (полный список — DR-M1.4-1,
`docs/SPEC-COVERAGE.md`).

**Принятые издержки.** `ChangeOrder.scala` (PR-10) добавляет ребро
`model → TicketValidator` — допустимо: валидатор является корнем агрегации,
обратного ребра нет.

## Normative references

- ROADMAP §6 (ADR-0002), §3.6, §3.5 (hotspot `TicketValidator`)
- `model/ValidationTypes.scala`, `model/TicketValidator.scala` — фактическое состояние после PR-9
- `docs/SPEC-COVERAGE.md`, DR-M1.4-1

## Migration impact

Публичные типы остались в пакете `xjdf4s.model`; у call sites обновились
только импорты. Бинарной совместимости проект не обещает до M6.

**Срок пересмотра:** M2/M3 — при появлении новых слоёв кодеков направление
`core → codec` запрещено (ROADMAP §7.1); фундамент валидации расширяется
только вертикально.
