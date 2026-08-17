# Этап 01 — cats-основы: инстансы и законы типов

| Поле | Значение |
|---|---|
| Цель | Подключить cats-core и снабдить модель базовыми type class-инстансами: `Eq`, `Show`, `Hash`, `Semigroup`/`Monoid` для комбинируемых узлов |
| Вход | Зелёная сборка, модули `core`/`model`/`messaging`/`protocol`, munit-сюиты |
| Выход | `xjdf4s-core` и `xjdf4s-model` зависят от cats; инстансы для ключевых типов; smoke-тесты законов |
| Сложность | Низкая (идеальный первый этап для новичков в FP) |
| Зависимости | — |

## Зачем это нужно

Дальше по дорожной карте каждый слой будет опираться на эти инстансы:

- **`Eq`** — законы round-trip кодеков (`decode(encode(a)) == a`) и property-тесты требуют
  структурного равенства, единого для всех типов. `case class` уже даёт `==`, но `Eq` даёт
  типобезопасное равенство с законами, и его можно объявить для opaque-типов единообразно.
- **`Show`** — человекочитаемое представление для сообщений об ошибках валидации и логов транспорта.
- **`Hash`** — ключи кешей и реестров (`QualifiedName` уже является ключом `Map` в `Extensions`);
  нужен согласованный с `Eq` контракт (`x == y ⇒ hash(x) == hash(y)`).
- **`Semigroup`/`Monoid`** — комбинирование `Extensions` (объединение wildcard-атрибутов и
  foreign-элементов), списков `Comment`, журналов событий транспорта.

Косвенно этот этап решает и давнюю находку аудита LO-07: с cats появляется осмысленный путь к
`-language:strictEquality` — `Eq` становится точкой, вокруг которой это можно ввести без боли.

## Предпосылки: что читать

В репозитории (`reference/cats/docs/`):

- `algebra.md` — обзор алгебраических структур (что вообще такое полугруппа/моноид);
- `typeclasses/eq.md`, `typeclasses/show.md` — семантика и законы `Eq`/`Show`;
- `typeclasses/semigroup.md`, `typeclasses/monoid.md` — законы `|+|` и `empty`;
- `imports.md`, `jump_start_guide.md` — как подключать синтаксис и импорты в коде.

Если хочется теории — `reference/category-theory/Part 1 – simple-algebraic-data-types.md`
(структуры как суммы/произведения) и `Part 3 – algebra-for-monads.md` (что такое алгебра операции).

## Дизайн

### 1. Зависимости

```scala
// build.sbt
ThisBuild / libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.x",          // актуальную 2.x сверь с Maven Central
  "org.typelevel" %% "kittens"   % "3.x" % Test    // деривация инстансов для case class
)
```

kittens умеет `derives` для cats-инстансов в Scala 3:

```scala
import cats.derived.*
import cats.{Eq, Show, Hash}

final case class IntegerRange(first: Int, last: Int) derives Eq, Show, Hash
```

Политика: **деривация по умолчанию, ручные инстансы там, где семантика важна**.
Пример ручного инстанса — `Eq` для чисел с плавающей точкой, где точное равенство опасно,
или `Show` для `ValidationError`, который должен давать стабильные тексты ошибок.

### 2. Инстансы для opaque-типов

Opaque-типы (`Nmtoken`, `XsdId`, `XsdIdRef`, `XjdfString`, `Priority0To100`, `ForeignQName`)
— это обёртки над `String`/`Int`/`QualifiedName`. Для них:

```scala
given Eq[Nmtoken] = Eq.fromUniversalEquals
given Show[Nmtoken] = Show.show(_.value)
given Hash[Nmtoken] = Hash.fromUniversalHashCode
```

Это ровно те места, где `Eq` оправдывает себя больше, чем `==`: контракт равенства объявляется
рядом с инвариантом типа, а не полагается на молчаливое структурное равенство.

### 3. Моноид для `Extensions`

```scala
given Semigroup[Extensions] with
  def combine(x: Extensions, y: Extensions): Extensions =
    Extensions(
      attributes = x.attributes ++ y.attributes, // правая часть побеждает при конфликте ключа
      elements   = x.elements ++ y.elements,
    )

given Monoid[Extensions] with
  def empty: Extensions = Extensions.empty
  def combine(x: Extensions, y: Extensions) = semigroupForExtensions.combine(x, y)
```

Зачем: конкатенация wildcard-узлов при «склейке» частичных документов и при интерпретаторах DSL
(этап 03). Зафиксируйте семантику конфликта атрибутов (right-biased) и покройте тестом.

### 4. Где брать инстансы в коде

Следуйте `reference/cats/docs/imports.md`: в библиотечном коде — только `import cats.*` и
`import cats.syntax.all.*` там, где это нужно, без рассеивания `given` по коду.
Экспортируйте инстансы через companions типов, чтобы они находились по implicit scope.

## Задачи (пошагово)

1. Добавьте cats-core (и kittens в `Test`) в `build.sbt`; убедитесь, что `sbt "clean ; compile ; test"` зелёный.
2. В `core` объявите `given Eq/Show/Hash` для всех opaque-типов и `QualifiedName`.
3. В `model` продеривируйте `Eq, Show, Hash` для малых value-типов (`IntegerRange`, `XYPair`,
   `LabColor`, `CmykColor`, `SrgbColor`, `Matrix`, `Rectangle`) и для enum'ов — через `derives`.
4. Ручной `Show` для `ValidationError` (все 9 case'ов с человекочитаемым текстом) и `Eq` для него же.
5. `Monoid[Extensions]` + `Semigroup` для `Map[QualifiedName, ExtensionValue]`; тест
   right-biased слияния и пустоты.
6. Smoke-законы: подключите `cats-laws` в `Test` и прогоните законы `Eq`/`Hash`/`Monoid` на
   2–3 представителях (шаблон — `reference/cats/docs/typeclasses/lawtesting.md`):

```scala
class ExtensionsLaws extends munit.DisciplineSuite:
  checkAll("Monoid[Extensions]", MonoidTests[Extensions].monoid)
```

## Definition of Done

- [ ] `xjdf4s-core` и `xjdf4s-model` компилируются с cats-core; сборка и тесты зелёные.
- [ ] Инстансы `Eq`/`Show`/`Hash` существуют для всех opaque-типов и малых value-типов.
- [ ] `Show[ValidationError]` даёт стабильные, читаемые сообщения (покрыт тестом).
- [ ] `Monoid[Extensions]` покрыт тестами (empty, assoc, right-biased merge).
- [ ] Smoke-законы (`Eq`, `Monoid`) зелёные через discipline/munit.
- [ ] Политика импортов зафиксирована в комментарии/PR-описании (по `imports.md`).

## Риски и альтернативы

- **kittens тащит derivation-макросы** — если хочется минимум зависимостей, инстансы пишутся
  вручную; для ~200 типов модели это многословно, поэтому kittens оправдан, но его можно
  ограничить `Test`-скопом, если инстансы нужны только для законов.
- **`Eq.fromUniversalEquals` для Float/Double** — точное равенство на числах; для property-тестов
  кодеков (этапы 04–05) понадобится `Eq` с допуском (`Eq.instance((a, b) => math.abs(a-b) < eps)`)
  — заведите его сразу в `core`, это снимет боль на этапе round-trip.
- **Соблазн развести гигантский deriving-слой** — не трогайте здесь большие ресурсные типы
  (Media, Resource, XJDF): их `Eq`/`Show` появятся на этапе 08 вместе с деривацией кодеков,
  где решается общая проблема генерации.
