# Этап 02 — Валидация на Validated (NonEmptyList)

| Поле | Значение |
|---|---|
| Цель | Превратить `validate: Vector[ValidationError]` в композиционный DSL валидации: аккумуляция ошибок через `ValidatedNel` (NonEmptyList), предупреждения через `Ior`, сквозная валидация документов через `Traverse` |
| Вход | Этап 01 (cats-core, `Show[ValidationError]`, законы) |
| Выход | Расширение `core`/`model`: `Validated`-API поверх существующего `ValidatedNode`; документная валидация «сверху вниз» с аккумуляцией |
| Сложность | Средняя |
| Зависимости | 01 |

## Зачем это нужно

Сейчас каждый узел умеет `validate: Vector[ValidationError]`, но:

- нет стандартного способа **аккумулировать** ошибки по всему дереву документа (приходится
  вручную склеивать векторы, легко потерять ошибку);
- нет различения **ошибка vs предупреждение** (например, `MediaType.Vinyl` deprecated,
  `Subscription/@Languages` deprecated — это не ошибки, но их хочется видеть);
- ошибка не несёт контекст (путь к узлу, ID элемента), что важно в больших документах.

`Validated` — ровно тот инструмент: это `Either` с **аккумулирующей** семантикой: два `Invalid`
объединяются через `Semigroup` левой части, а не отбрасываются. `ValidatedNel[E, A]` — сокращение
для `Validated[NonEmptyList[E], A]`.

## Предпосылки: что читать

- `reference/cats/docs/datatypes/validated.md` — обязательно целиком: там и «почему не Either»,
  и синтаксис (`validatedNel`, `toValidatedNel`, `*>`);
- `reference/cats/docs/datatypes/ior.md` — inclusive-or: и значение, и предупреждения;
- `reference/cats/docs/datatypes/nel.md`, `datatypes/chain.md` — структуры аккумуляции ошибок
  (`ValidatedNel` в cats = `Validated[NonEmptyList[E], A]`; есть и `ValidatedNec` на `NonEmptyChain`);
- `reference/cats/docs/typeclasses/applicative.md`, `typeclasses/traverse.md` — как пройти по
  коллекциям и «сложить» результаты валидации;
- `reference/cats/docs/typeclasses/parallel.md` — полезно на этапе 07 (валидация параллельно с IO).

## Дизайн

### 1. Не ломаем публичный API

`validate: Vector[ValidationError]` остаётся (это стабильный контракт). Рядом добавляется
расширение:

```scala
// core
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.syntax.all.*

extension (errors: Vector[ValidationError])
  def toValidatedNel: ValidatedNel[ValidationError, Unit] =
    NonEmptyList.fromList(errors.toList) match
      case Some(nel) => Validated.invalid(nel)
      case None      => Validated.validNel[ValidationError, Unit](())

extension (node: ValidatedNode)
  def validateNel: ValidatedNel[ValidationError, Unit] = node.validate.toValidatedNel
```

`ValidatedNel[ValidationError, Unit]` — «успех без значения»; для операций, возвращающих значение
(декодеры на этапе 04), тип обобщается до `ValidatedNel[ValidationError, A]`.

### 2. Аккумуляция по дереву через Traverse

Документ валидируется снизу вверх, ошибки накапливаются:

```scala
def validateAll(resourceSet: ResourceSet): ValidatedNel[ValidationError, Unit] =
  resourceSet.resources.traverse_(_.validateNel) // Traverse по Vector[Resource]
```

`traverse_` для каждого `Resource` возвращает `Validated`, Applicative `Validated` склеивает
ошибки всех узлов в один `NonEmptyList`. Аналогично для `XJDF`: productList, resourceSets, auditPool.

### 3. Ошибка с контекстом

Добавьте в `ValidationError` (или рядом — не ломая существующие case'ы) контекстную обёртку:

```scala
enum ValidationError derives CanEqual:
  // существующие case'ы...
  case AtPath(path: Vector[String], underlying: ValidationError)
```

И `Show[ValidationError]` из этапа 01 печатает цепочку: `ResourceSet[Media] → Resource[0] →
@Orientation xor @Transformation`. Отдельный `Semigroup` для `ValidationError` не нужен —
аккумуляция идёт на уровне `NonEmptyList`.

### 4. Предупреждения через двухканальный результат (и Ior)

Ошибки блокируют приём документа, предупреждения — нет, но они видны в логах и в
codec-слое (этап 05). Простая запись покрывает большинство случаев:

```scala
final case class Warning(code: String, message: String) derives Show

/** Двухканальный результат: ошибки блокируют, предупреждения — нет. */
final case class ValidationOutcome(errors: Vector[ValidationError], warnings: Vector[Warning]):
  def isValid: Boolean = errors.isEmpty

def validateWithWarnings(resource: Resource): ValidationOutcome =
  ValidationOutcome(
    errors = resource.validate,
    warnings = resource.specificResource.collect {
      case _: resources.Media if usesDeprecatedVinyl(resource) =>
        Warning("media-type-deprecated", "MediaType 'Vinyl' is deprecated in XJDF 2.1; use Synthetic")
    }.toVector,
  )
```

Там, где нужен именно *тип*, а не запись (например, декодер: «значение получено, но с
предупреждением»), берите `Ior`: левая часть аккумулируется через `Semigroup`
(`NonEmptyList[Warning]`), и `Ior.both(warnings, value)` выражает «и то, и другое»
(`reference/cats/docs/datatypes/ior.md`).

### 5. Fail-fast вариант

Там, где нужен быстрый отказ (например, валидация до дорогого кодирования), дайте
`validateEither: Either[NonEmptyList[ValidationError], Unit]` — перевод `Validated → Either`
через `.toEither`. Два режима — это осознанный выбор API, а не дублирование.

## Задачи (пошагово)

1. `toValidatedNel`/`validateNel`-расширения в `core` + тесты (пустой вектор
   ошибок ⇒ `Valid`, непустой ⇒ `Invalid` с цепочкой).
2. `traverse_`-валидация: `Resource.validateAll`, `ResourceSet.validateAll`,
   `XJDF.validateAll` — с тестом «три дефектных ресурса ⇒ все три ошибки в результате».
3. `AtPath`-обёртка + прошивка пути при обходе дерева; `Show` печатает полный путь.
4. Предупреждения: `Warning`, `validateWithWarnings` для выбранных deprecated-мест
   (`MediaType.Vinyl`, `Subscription/@Languages`, legacy-глюи `BoxFoldingParams`).
5. `Ior`-тест: значение + предупреждение одновременно (`Ior.both`).
6. Документация в README модуля: когда `Validated` (аккумуляция), когда `Either` (fail-fast).

## Definition of Done

- [ ] `validateNel`/`toValidatedNel` работают и покрыты тестами.
- [ ] `XJDF.validateAll` аккумулирует ошибки всех вложенных узлов (тест с 3+ ошибками).
- [ ] Ошибки несут путь к узлу; `Show` печатает читаемую цепочку.
- [ ] Deprecated-места порождают предупреждения, не ошибки (`Ior`-канал покрыт тестом).
- [ ] Старый API `validate: Vector[ValidationError]` сохранён без изменений (бинарная/исходная совместимость).
- [ ] `sbt "clean ; compile ; test"` зелёный.

## Риски и альтернативы

- **Раздувание модели ошибок.** Не добавляйте case на каждый чих: `InvalidValue` + путь покрывают
  большинство случаев; новые case'ы — только под структурно новые ситуации.
- **`NonEmptyList` vs `Vector`.** Переход оправдан только внутри нового API (аккумуляция);
  публичный `Vector` не трогаем, чтобы не ломать пользователей.
- **Валидация всего документа может быть дорогой** на гигабайтных RunList — это не проблема
  этого этапа, но помните: `Traverse`-обход должен оставаться short-circuit'ируемым для
  fail-fast режима.
