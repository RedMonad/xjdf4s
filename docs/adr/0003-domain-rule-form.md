# ADR-0003 — Форма локальных правил: `DomainRule`, а не `Boolean`

- **Статус:** принято (реализовано в PR-8, M1.3-3)
- **Дата:** 2026-08-15
- **Задача:** M1.3-3 (PR-8); дедлайн по ROADMAP §6 — «до M1.3-3»
- **Закрывает находки:** N-18, N-38
- **Связанные ADR:** ADR-0002 (слои валидации), ADR-0006 (severity)

---

## Context

N-18: пять реализаций `isLawful: Boolean` не были подключены к корневому
валидатору. `Boolean` теряет причину, путь и severity: невозможно отличить
«какой закон нарушен» от «где» и «насколько серьёзно».

## Decision

```scala
/** A model node carrying local structural laws (spec SHALL / SHALL NOT). */
trait DomainRule[-A]:
  def check(value: A, at: XPath): Chain[Issue]
```

- Локальные правила возвращают структурированные `Issue` (код, severity,
  XPath, человекочитаемое сообщение).
- `TicketValidator` выполняет один обход агрегата и накапливает их.
- Глобальные правила (уникальность ID, §3.4, хронология, целостность BOM)
  остаются в валидаторе.
- Каждый `Issue` получает стабильный machine-readable `IssueCode`, чтобы
  кодеки M2 и HTTP-слой M4 не разбирали строки сообщений.

Так восстанавливается заявленный в `docs/01` образ: валидация — единый
гомоморфизм из дерева тикета в `ValidatedNec[Issue, Unit]`, реализованный,
а не декларированный.

**Критерий полноты.** Registry-тест перечисляет все типы с локальными
правилами и доказывает, что у каждого есть вызов из корня. Grep по приватным
`isLawful` не должен находить «мёртвых законов».

## Alternatives

| Вариант | Суть | Оценка |
| --- | --- | --- |
| Оставить `Boolean isLawful` и вызывать из валидатора | Минимум кода | Теряет причину/путь/severity; нарушает образ единого гомоморфизма |
| `Either[String, Unit]` | Строковые ошибки | Теряет стабильный код и XPath; парсинг строк — анти-паттерн |
| **`DomainRule[-A]`** | Принято | Композируемо, структурировано, тестируемо |

## Consequences

**Реализация (PR-8).** Все бывшие `Boolean`-предикаты приведены к
`DomainRule` и явно вызываются из `TicketValidator.checkLocalLaws`:
`Intent.nameLaw`, `BindingIntent.law`, `VariableIntent.law`, `PartWaste.law`,
`Notification.law`, `Product.amountsLaw`, `ResourceSetLaw.children/statuses`.
`Disposition` (Table 8.23) вынесен в `TicketValidator.dispositionLaw`, чтобы
`prim` не зависел от слоя валидации до M1.4-1; хук в обходе ресурсов
подключается при реализации FileSpec-несущих ресурсов (M1.6/M3).
`Boolean`-аксессоры сохранены как производные для тестов/DSL, но первичной
формой закона является `DomainRule`.

Негативные тесты обязательны на каждое правило (список — ROADMAP M1.3-3);
каждый закон имеет стабильный `IssueCode`.

## Normative references

- ROADMAP §6 (ADR-0003), §8 (M1.3-3), §12.2 (негативные тесты на SHALL)
- `model/ValidationTypes.scala` (`DomainRule`), `model/TicketValidator.scala`
- `docs/SPEC-COVERAGE.md`, DR-DomainRule

## Migration impact

Ломающих изменений нет: `isLawful`-аксессоры сохранены как производные.
Внутренние предикаты заменены на `DomainRule`; новые правила обязаны идти
через шину.

**Срок пересмотра:** M2 — при добавлении кодеков проверяется, что декодеры
используют `IssueCode`, а не строки сообщений.
