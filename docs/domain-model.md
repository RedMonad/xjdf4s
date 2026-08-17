# Доменная модель xjdf4s

Единственный краткий справочник по устройству доменной модели. Обновляется только вместе с изменением
архитектуры — это не журнал работ и не свод правил.

## Источник истины

Нормативный текст XJDF/XJMF 2.2 (`reference/xjdf/*.md`). Приоритет при расхождениях:

**нормативные таблицы и проза → XSD/индекс → модель Scala**.

Проверенный в репозиторий `schema.xsd` отстаёт от нормативных таблиц (см. раздел «Нормативные расхождения»),
поэтому модель сверяется с текстом спецификации, а не с XSD.

## Модули

```
core       — скалярные типы, кардинальности, расширения, словарь валидации      (только cats-core)
model      — XJDF: продукт, интенции, ресурсы, саб-элементы, валидация документа (→ core)
messaging  — XJMF: конверт, заголовки, 44 сообщения                             (→ model)
protocol   — union/intersection-типы поверх XJDF + XJMF                         (→ model, messaging)
dsl        — Free-DSL конструирования документов (см. free-dsl.md)              (→ model, cats-free)
codec-xml  — XML-кодек поверх домена: парсер/писатель, декодеры/энкодеры,
             typeclass-деривация (derivation/), генерируемый Registry 102/14/44,
             ID/IDREF-проход, property-based proof против schema.xsd          (→ model, messaging)

Генераторы доменных значений для кодек-тестов живут в тестовых scope'ах `model` и `messaging`
(пакеты `*.generators`) и переиспользуются нижележащими модулями через `dependsOn(project % "test->test")`
(справочник sbt, «Per-configuration classpath dependencies»). Генераторы XSD-safe: расхождения
норматив/XSD не генерируются, поэтому падение property — это дефект кодека.
```

Домен (`core`–`protocol`) не тянет эффектов и транспорта: JSON/XML-кодеки, HTTP и `IO` — слои поверх,
по roadmap. `codec-xml` — первый такой слой; домен о нём не знает.

## Принципы

1. **ADT — источник истины.** Каждый слой (DSL, кодеки, транспорт) строит значения модели или
   интерпретирует их, но не дублирует структуру.
2. **Транспорт не протекает в домен.** JSON-only члены (`$schema`, `@Name`), XML-экранирование и MIME-типы
   живут только в кодек/HTTP-слоях.
3. **Недопустимое состояние невыразимо.** Инварианты держатся типами: opaque-типы с smart-конструкторами,
   копродукты для XOR-выборов (`FileLocation`, `DispositionTime`, `BindingSpecification`, `MediaLayer`),
   `NonEmptyVector`/`TwoOrMore` для кардинальностей.
4. **Межполевые SHALL — через `ValidatedNode#validate`.** То, что не выразить формой типа, отклоняется
   валидацией (`Resource` orientation/transformation, документные ID-дубликаты, окна замены сигналов).
5. **Точки расширения открыты, но namespace-безопасны.** `ForeignQName` гарантирует: стандартное имя XJDF
   нельзя провести через generic-носители (`NamedSpecificResource`, `QueryMessage`, `ExtensionElement`).
6. **Кодеки — отдельные модули с round-trip-законами** (`decode ∘ encode = id`), а не часть модели.

## Словарь скалярных типов (core)

| Тип | Лексика/инвариант | Зачем |
|---|---|---|
| `Nmtoken` | XML NMTOKEN, без пробелов | идентификаторы/токены |
| `XsdId` / `XsdIdRef` | XML Name без `:` | **объявление** vs **ссылка** — разные типы, перепутать нельзя |
| `XjdfString` | нормализованная строка ≤ 1023, без управляющих символов | нормативный тип `string` (Appendix A.1) |
| `XsdDateTime` | дата+время, **обязательная зона**, реальный календарь | нормативный `dateTime` |
| `XsdDuration` | XSD duration, непустой (`P`/`PT` запрещены) | |
| `LanguageTag`, `UriRef`, `CountryCode`, `XPath`, `PdfPath` | по назначению | |
| `Priority0To100` | целое 0–100 | приоритеты (Disposition, NodeInfo, GangElement, очереди) |
| `LabColor` / `CmykColor` / `SrgbColor` | L∈[0..100]; C/M/Y/K∈[0..1]; R/G/B∈[0..1] | нормативные диапазоны (Appendix A.1) |
| `TransferFunction` / `GluingPattern` | непустые списки чётной длины | x/y-пары; сегмент/зазор |
| `NeutralDensity` | 0.001–10 | Patch/@NeutralDensity |
| `FoldCatalog` | `F[0-9]+-([0-9]+\|X)` | паттерн каталога фальцовки |
| `IntegerRange(first, last)` | first ≤ last | партиционные ключи `Part` и диапазоны RunList |

Все opaque-типы: `from(...) : Either[ValidationError, T]` + аксессор `.value`.

## Нормативные расхождения (модель следует нормативу, не XSD)

| Тема | Норматив 2.2 | Старый XSD |
|---|---|---|
| `@Version` | `2.0/2.1/2.2` | только `2.0/2.1` |
| `JdfVersion` (`Device/@JDFVersions`) | JDF `1.0`–`1.8` + XJDF `2.x` | без 1.0/1.8 |
| `Resource/@Brand` | `string` | `boolean` |
| `Part/@BlockName`, `PartWaste/@ModuleIDs`, `AssemblyItem/@ChildRef` | `NMTOKEN`/`NMTOKENS`/`IDREF` | XYPair/float/float |
| `ResourceSet/@CombinedProcessIndex` | `IntegerList` | `FloatList` |
| `Condition/@PartContext` | `NMTOKENS` (список) | `NMTOKEN` |
| `ColorMeasurementConditions/@Illumination` | `NMTOKEN` (`D50`, `D65`, `Unknown`) | `float` |
| `GeneralID/@DataType` | Table A.14 (с `NamedFeature`) | устаревший набор |
| `RunList/@Docs` | `IntegerRange` | `IntegerList` |
| `BoxFoldingParams` | `BoxFoldAction*` + `Action="Glue"` | устаревшая форма (legacy-глюи опциональны) |
| `MediaLayers` | упорядоченные `Glue* \| Media*` | фиксированная пара |
| `ChannelMode` | `FireAndForget \| Reliable`; в `Subscription` — список | корректен |
| `Patch`, `ResourceQuParams`, `StatusQuParams` | нормативные имена элементов | (имена классов модели) |

## Валидация

- Узлы: `validate: Vector[ValidationError]` (trait `ValidatedNode`).
- Аккумуляция: `toValidatedNel`, `validateNel`, `XJDF.validateAll` — все ошибки разом, с путём
  (`ValidationError.AtPath`).
- Двухканальность: `Warning` + `ValidationOutcome` (`validateWithWarnings`, `validateDocumentWithWarnings`) —
  ошибки блокируют, deprecated-заметки — нет.
- Fail-fast: `toEitherNel`.

## Расширения

`Extensions` (anyAttribute + foreign children) с `Monoid` (right-biased по атрибутам);
`ExtensionElement`/`ExtensionContent` сохраняют упорядоченный mixed content.
Generic-носители принимают только `ForeignQName`.

## Дальше

План развития — [roadmap/](../roadmap/README.md); понятийный минимум FP — [fp-glossary.md](fp-glossary.md);
дизайн DSL — [free-dsl.md](free-dsl.md).
