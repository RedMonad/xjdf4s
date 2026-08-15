# Архитектура и организация кода

Сборка: **sbt 2.0.2** (build definition на Scala 3 — `./reference/sbt/docs`),
Scala **3.8.4**, cats-core **2.13.0**, тесты — munit 1.3.0 +
munit-scalacheck 1.3.0. Структура модулей:

```
xjdf4s/
├── build.sbt                  # sbt 2.x, Scala 3-синтаксис
├── project/build.properties   # sbt.version=2.0.2
├── modules/
│   ├── core/                  # xjdf4s-core: весь домен
│   ├── laws/                  # xjdf4s-laws: законы + conformance-сьюты (Test);
│   │                          # зависит от core и examples (M1.5-3, PR-13)
│   └── examples/              # xjdf4s-examples: только демо примеров
│                              # спецификации (conformance — в laws)
├── docs/                      # 01-category-theory-view, 02-scala3-features,
│                              # 03-cats-mapping, 04-architecture,
│                              # SPEC-COVERAGE.md (реестр покрытия),
│                              # adr/ (архитектурные решения)
├── scripts/check-spec-coverage.sh
│                              # чекер согласованности реестра покрытия
└── ROADMAP.md (консолидированный)
```

## Слои пакета `xjdf4s`

```
prim/        базовые типы-значения (без зависимостей от домена)
  Tokens     NmToken/NmTokens, XjdfString, LanguageTag, NsPrefix, Named
  Ids        Id/IdRef/IdRefs, JobId/JobPartId/ProjectId
  Versions   IcsVersion, XjdfVersion
  Quantity   XYPair, Shape, Rectangle, Matrix, Points, Microns, Grammage,
             Amount, AmountBounds, Coverage, UnitInterval, Severity,
             IntegerRange, LabColor, CMYKColor, RGBColor, FloatList,
             IntegerList
  Time       Timestamp, TimeSpan, TimeRange
  Enums      закрытые перечисления Appendix A + XjdfEnum/XjdfEnumCompanion
  Common     Comment, GeneralID, Event, Milestone, Dependent, FileSpec,
             Disposition, FileLocation, Catalog.*

model/       скелет XJDF
  Partition  PartitionKey, PartitionValue, ValueOf (match type), Part, PartBuilder
  Amounts    PartWaste, PartAmount, AmountPool
  Header     Header, DeviceInfo, Notification(+union), ProcessRun, ResourceInfo
  Audit      Audit, AuditPool, Signal, Pulse, Alignment (FunctionK)
  Product    Product, ProductList, ProductTree, Fix, Bom (cata)
  Intent     Intent, IntentName
  Resource   ProcessType, ProcessPath, ProcessIndex, ResourceSetName,
             ResourceSet, ResourceSetKey, Resource, OrientationSpec (union)
  Ticket     XJDF, WorkstepKey (named tuple)
  ChangeOrder
             номинальный partial-документ (§1.3.2, §1.6.5, ADR-0001);
             compile → Patch; applyChange ревалидирует результат
  Patch      Patch (Endo-моноид), mergeResourceSets (Ior),
             extension XJDF.withPatch
  ValidationTypes
             Issue, IssueCode, SeverityClass, XPath, DomainRule,
             ValidationResult, ValidationReport — фундамент валидации
             с Fan-Out 0 (ADR-0002, M1.4-1)
  TicketValidator
             корневой валидатор, агрегирует правила (ADR-0002);
             extension XJDF.validate / XJDF.validateReport
  IdSource   IdSource (State), IdAllocator (context function)

intents/     Product Intents главы 4
resources/   Resources главы 6
dsl/         декларативные конструкторы, возвращающие ValidatedNec
```

Зависимости пакетов: `prim ← {model, intents, resources}`,
`model ← {intents, resources}` (только для закрытых перечислений полезных
нагрузок `IntentPayload`/`ResourcePayload`), **`resources ← intents`**:
`resources/Finishing.scala` импортирует `xjdf4s.intents.{Fold, Perforate}`
(`FoldingParams` переиспользует элементы главы 8, объявленные рядом с
`FoldingIntent`; N-40), `dsl ← все`. Циклов файловых зависимостей нет
(ADR-0002, M1.4-1 / PR-9): `ValidationTypes` — фундамент с Fan-Out 0;
`intents` и `resources` ссылаются на `model.ValidationTypes` и закрытые
перечисления payload; корневой `TicketValidator` импортирует модель
интентов — файловый граф ацикличен.

**Слой валидации** (ADR-0002, PR-9): `model/ValidationTypes.scala` (`Issue`,
`IssueCode`, `SeverityClass`, `XPath`, `DomainRule`, `ValidationResult`,
`ValidationReport`) — фундамент с Fan-Out 0, импортирует только `prim.*` и
cats; `model/TicketValidator.scala` — корневой валидатор, агрегирует
локальные правила (`DomainRule`) и глобальные проверки (ID/IDREF, §3.4, BOM,
хронология), предоставляет extension-методы `XJDF.validate` /
`XJDF.validateReport`.

**Межмодульный граф (M1.5-3).** `core ← {laws, examples}`; дополнительно
`laws → examples`: conformance-сьют `laws/SpecExamplesSuite.scala` исполняет
примеры `examples.SpecExamples` как именованные тесты с номерами
разделов/таблиц. `examples` остаётся демонстрационным (`Main` + построители
примеров), тестового кода в нём нет. Цикла нет: `examples → core` и
`laws → {core, examples}`.

## Принципы

1. **Таблицы спецификации → типы.** Имя каждого поля совпадает с колонкой
   NAME соответствующей таблицы; scaladoc ссылается на номер таблицы/раздела.
2. **Кардинальность → вид типа.** `T?` → `Option[T]`, `T*` → `Chain[T]`,
   `T+` → `NonEmptyChain[T]`, `T` → обязательный параметр.
3. **Закрытое/открытое различается.** «Allowed values are» → enum;
   «Values include» → `NmToken` + `Catalog`. Расширения — `Foreign`/
   `Extension` с namespace-префиксом.
4. **Законы сначала.** Каждая алгебраическая структура имеет свойство-тест;
   каждый структурный инвариант спецификации — либо тип, либо проверка в
   `TicketValidator`.
5. **Без исключений в управляющем потоке.** Невалидный вход — `Option`/
   `ValidatedNec`; `unsafe`-конструкторы — только для констант, про которые
   известно, что они валидны (токены спецификации, литералы тестов).

## Что реализовано, а что осознанно отложено в `ROADMAP.md`

- Реализовано: скелет XJDF 2.2 (§3), ProductList/BOM (§3.3, §4 — 8 интентов из
  13), ResourceSet/Resource/Part/AmountPool (§3.4, §6.1), AuditPool и
  выравнивание с сигналами (§3.2, Table 3.2), 12 ресурсов главы 6, валидатор,
  Patch/change orders, DSL, примеры из спецификации, законы.
- В `ROADMAP.md`: оставшиеся ~130 ресурсов и 5 интентов, кодеки XML/JSON
  (см. §1.4, §9.10), JSON-исключения (`$schema`, `@Name`, AuditPool-массив),
  XJMF (глава 7), интеграция с REST-эндпоинтами §9.10.3.
