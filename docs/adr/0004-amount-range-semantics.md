# ADR-0004 — Семантика nominal Amount и AmountBounds

- **Статус:** принято
- **Дата:** 2026-08-15
- **Задача:** M1.4-5 (PR-11); дедлайн по ROADMAP §6 — «до M1.4-5»
- **Закрывает находку:** N-23
- **Связанные документы:** ROADMAP §6 (ADR-0004), §8 (M1.4-5), §12.2; Table 6.3

---

## Context

В текущем API `AmountRange` объединяет три независимые по смыслу величины из
`PartAmount`: nominal `@Amount`, нижнюю границу `@MinAmount` и верхнюю границу
`@MaxAmount`. Он объявляет `meet` и `join` вместе с `Semilattice[AmountRange]`.

Это не имеет достаточного доменного основания. Нормативная Table 6.3
(`reference/xjdf/6 – Resources.md`, §6.1.2) различает:

> `Amount?` — Amount, excluding waste, in units defined in
> `ResourceSet/@Unit` or implied by Table A.3.23 Units.
>
> `MaxAmount?` — Defines the planned `@Amount` including the maximum overage.
> `@MaxAmount` SHALL NOT be specified as actual amounts.
>
> `MinAmount?` — Defines the planned `@Amount` including the maximum underage
> that the customer is willing to accept. `@MinAmount` SHALL NOT be specified
> as actual amounts.

Следовательно nominal amount — заявленная величина, а min/max — допустимый
контрактный интервал для *planned* amount. Покомпонентные `min`/`max` над всеми
тремя полями не определяют, как объединять две nominal величины. Кроме того,
старые направления расходятся с заявленным смыслом: при ужесточении нижняя
граница должна увеличиваться, а верхняя — уменьшаться. Пустое пересечение
старый тотальный `meet` мог представить как обычный range. `join` не имеет
потребителей.

## Decision

### Разделить nominal и bounds

`Amount` остаётся самостоятельным типом nominal/actual количества. Вводится
отдельный тип:

```scala
/** §6.1.2 / Table 6.3: planned acceptable lower and upper amount bounds. */
final case class AmountBounds(min: Option[Amount], max: Option[Amount]):
  require(min.forall(lower => max.forall(lower <= _)), "MinAmount > MaxAmount")
```

`AmountBounds` содержит только допустимые границы. Для `PartAmount` nominal
`amount` и bounds будут храниться раздельно; адаптер/конструктор обязан
отвергать состояние, в котором заданный nominal amount находится вне заданных
границ. Границы относятся к planned amounts: проверка контекста actual amount
остаётся ответственностью валидации контекста Resource/AmountPool, а не
произвольной алгебры значений.

### Операции над bounds

```scala
def meet(a: AmountBounds, b: AmountBounds): Option[AmountBounds]
def widen(a: AmountBounds, b: AmountBounds): AmountBounds
```

| Операция | min | max | Смысл |
| --- | --- | --- | --- |
| `meet` | ↑ (`max`) | ↓ (`min`) | Ужесточение контракта, пересечение допустимых диапазонов |
| `widen` | ↓ (`min`) | ↑ (`max`) | Оптимистичное расширение, объединение допустимых диапазонов |

`meet` возвращает `None`, если пересечение пусто. Поэтому это частичная
операция и для него **не** объявляется `Semilattice`: обычный cats
`Semilattice.combine` тотален. `widen` тотален; algebraic instance может быть
добавлен только после явного определения порядка и доказательства law-тестами.
В PR-11 он остаётся именованной операцией с тестами ассоциативности,
коммутативности и идемпотентности, без преждевременного typeclass-обещания.

Nominal `Amount` не участвует в `meet` или `widen`: выбор между двумя
независимыми плановыми/фактическими количествами является бизнес-решением, а
не алгебраическим `min`/`max`.

### Миграция API

`AmountRange`, его `meet`, `join`, `includes` и `Semilattice[AmountRange]`
удаляются в M1.4-5. `join` не получает compatibility alias: у него нет
потребителей, а сохранение старого имени закрепило бы неверную семантику.
Потребители выражают нужное намерение явно:

- проверка приемлемости nominal amount — через `AmountBounds.includes`;
- пересечение контрактов — через `AmountBounds.meet` и обработку `None`;
- расширение допустимого контракта — через `AmountBounds.widen`.

До кода обязателен регрессионный тест на пустое пересечение; после миграции
полный список call sites должен быть пустым для `AmountRange` и `join`.

## Consequences

Положительные:

- API отражает различие Table 6.3 между `@Amount` и `@MinAmount`/`@MaxAmount`;
- невозможный `min > max` не создаётся обычным конструктором;
- пустое пересечение видно в типе результата;
- операции называются по доменному смыслу и имеют проверяемые направления.

Отрицательные:

- это breaking change публичного API; переходный alias не вводится намеренно;
- `PartAmount` и его отображение должны быть мигрированы за один логический
  коммит вместе с полным списком call sites;
- без дополнительной нормативной политики нельзя автоматически выбирать или
  сливать две nominal величины.

## Alternatives

| Вариант | Причина отклонения |
| --- | --- |
| Оставить `AmountRange` и только поменять направления `meet`/`join` | Продолжает смешивать nominal amount и bounds; не выражает пустое пересечение. |
| Оставить тотальный `meet`, нормализуя пустой интервал | Скрывает конфликт контракта как валидное значение. |
| Переименовать старый `join` в `widen` без law-тестов | Старое направление `min` неверно; одно переименование не доказывает смысл. |
| Дать `Semilattice` частичному `meet` | Нарушает требование тотальности `combine` у typeclass. |
| Комбинировать nominal amounts через `min`/`max` | В Table 6.3 нет доменного правила, оправдывающего такой выбор. |
