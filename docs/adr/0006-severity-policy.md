# ADR-0006 — Политика severity: errors vs warnings

- **Статус:** принято (реализовано в PR-8, M1.3-5)
- **Дата:** 2026-08-15
- **Задача:** M1.3-5 (PR-8); дедлайн по ROADMAP §6 — «до M1.3-5»
- **Связанные ADR:** ADR-0002 (слои валидации), ADR-0003 (`DomainRule`)

---

## Context

`Issue` уже нёс `SeverityClass`, но `ValidatedNec` инвалидировал результат
при любом issue. Спецификация различает SHALL (обязательно), SHOULD и MAY
(§1.6.x, RFC-2119-стиль): не каждое нарушение должно превращать документ в
невалидный.

## Decision

```scala
final case class ValidationReport(errors: Chain[Issue], warnings: Chain[Issue]):
  def isValid: Boolean = errors.isEmpty
```

- SHALL-нарушения → `errors`, инвалидируют результат;
- SHOULD/MAY → `warnings`, не превращают `Valid` в `Invalid` по умолчанию;
- строгий режим может эскалировать warnings отдельным явным вызовом;
- каждый `Issue` получает стабильный `IssueCode`; вызывающая сторона не
  разбирает строки сообщений (совместно с ADR-0003).

## Alternatives

| Вариант | Суть | Оценка |
| --- | --- | --- |
| Всё — ошибки (`ValidatedNec` как есть) | Минимум кода | SHOULD/MAY-нарушения ломают приёмку документов без нормативного основания |
| Два параллельных пайплайна | Валидатор вызывается дважды | Удвоение обхода; рассинхрон правил |
| **`ValidationReport`** | Принято | Один обход, честное разделение, стабильные коды |

## Consequences

- `XJDF.validate` возвращает `ValidationReport`; `validateReport` —
  единственный источник истины для обоих представлений.
- Повторная валидация результата change order (ADR-0001) использует тот же
  отчёт: `applyChange` отвергает результат с непустыми `errors`.
- Warning- и error-кейсы тестируются отдельно (ROADMAP §12.2).

## Normative references

- §1.6.x (SHALL/SHOULD/MAY), RFC 2119-стиль — `reference/xjdf/1 – Introduction.md`
- ROADMAP §6 (ADR-0006), §8 (M1.3-5), §12.2
- `model/ValidationTypes.scala` (`ValidationReport`, `Issue`, `SeverityClass`)

## Migration impact

`validate: ValidatedNec[Issue, Unit]` заменён на `validate:
ValidationReport` (PR-8). Call sites: `dsl/XjdfDsl.scala` (`ticket.validate.as
(ticket)`), тесты, `examples/SpecExamples.scala` — обновлены в том же PR.

**Срок пересмотра:** M2 — декодеры обязаны сообщать warnings без
инвалидации; при появлении SHOULD-правил с реальной семантикой — решение о
strict-режиме фиксируется здесь же.
