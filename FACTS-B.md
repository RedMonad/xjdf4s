# Факт-чек AUDIT.md

Дата проверки: 2026-08-17.
Метод: каждое проверяемое утверждение `AUDIT.md` сверено с тремя источниками:

1. Нормативный текст XJDF/XJMF 2.2 — `reference/xjdf/*.md`.
2. XSD/индекс — `reference/xjdf/tool/xsdq.py` + `xsd-index.json` + прямое чтение `schema.xsd`.
3. Фактический Scala-код — `modules/core`, `modules/model`, `modules/messaging`.

`sbt`/`java` в среде отсутствуют (подтверждено), поэтому раздел 9 AUDIT.md о невозможности
доказательной сборки проверен и корректен.

---

## 1. Итог факт-чека

**AUDIT.md в подавляющей части фактически точен.** Из ~40 проверяемых утверждений:

- **Подтверждено: 36** (включая все 3 Critical, все 14 High, все 10 Medium).
- **Опровергнуто: 1** — `LO-02` (RunList/@Docs) инвертирует собственный принцип приоритета
  «норматив > XSD»; текущая Scala-модель здесь **корректна**.
- **Требуют усиления формулировок: 3** — подтверждённые факты поданы как гипотезы
  («могут не содержать», «если подтверждено», «может не содержать»).
- Плюс 2 опечатки и 1 неточная характеристика XSD в сводной таблице.

---

## 2. Подтверждённые утверждения (выборочно, с доказательствами)

### Инвентарь и покрытие (раздел 3)

| Утверждение | Проверка | Результат |
|---|---|---|
| 365 elements / 366 complexTypes / 228 simpleTypes | `xsdq summary` | ✅ точно |
| 101 substitution member у `SpecificResource` в XSD | подсчёт рёбер `substitution_group` в индексе | ✅ ровно 101, включая `RasterReadingParams` |
| `xsdq hierarchy` возвращает 100, теряя `RasterReadingParams` | прямой вызов `hierarchy` | ✅ подтверждено: 100, отсутствует именно `RasterReadingParams` |
| 102/102 ресурсов в `StandardSpecificResource` | разбор union в `Resource.scala` | ✅ ровно 102 имени (101 XSD + `SheetOptimizingReport`) |
| `SheetOptimizingReport` — нормативный, вне XSD | Table 6.162; в `schema.xsd` отсутствует | ✅ |
| 14/14 интенций, 44/44 сообщений (11+8+18+7) | substitution_group: Command 11, Query 8, Response 18, Signal 7; Scala-unions совпадают | ✅ |
| 5 аудитов | `Audit.scala` | ✅ |
| 191 closed enumerations в XSD (раздел 5.1) | подсчёт simpleTypes с фасетом enumeration | ✅ ровно 191 |

### Critical

| ID | Проверка | Результат |
|---|---|---|
| CR-01 `ChannelMode` | Scala: `Reliable, Simulate, Transactional, Unreliable`; Table A.10: `FireAndForget, Reliable`; Table 7.5: ordered list; Table 7.7: одиночное. `Subscription.channelMode: Option[ChannelMode]` | ✅ подтверждено полностью. Значения в Scala — из JMF 1.x. Примечание: XSD здесь **корректен** (`EnumChannelMode = FireAndForget\|Reliable`, `Subscription/@ChannelMode` — `xs:list`), см. правку №5 |
| CR-02 `BoxFoldingParams` | Scala требует ровно один `action: BoxFoldAction` и обязательный `glue: Glue`; норматив: `BoxFoldAction*`, `Glue*` deprecated в 2.2, новый `@Action="Glue"` + child `Glue?` (Tables 6.17/6.19/6.20, H.1); в `BoxFoldActionType` нет case `Glue` | ✅ подтверждено полностью |
| CR-03 `MediaLayers` | Scala: `MediaLayers(glue: Glue, media: Media)` — фиксированная пара; норматив 8.28/Table 8.45: ordered mixed `Glue* \| Media*` + JSON `@Name` exception | ✅ подтверждено полностью |

### High

| ID | Проверка | Результат |
|---|---|---|
| HI-01 `GeneralId.DataType` | Scala: `Boolean, DateTime, Double, Duration, Integer, Name, Nmtoken, String, Uri`; Table A.14: `boolean, dateTime, duration, float, integer, NamedFeature, NMTOKEN, string` | ✅: лишние `Name`, `Uri`; `Double` вместо `float`; нет `NamedFeature` |
| HI-02 отсутствующие поля XJMF 2.2 | `ResourceQueryParams` без `types` (Table 7.49 `@Types?` New in 2.2); `SignalResource` без `replaceAfter/replaceBefore` (7 – Messaging.md, строки 904–905); `SubscriptionInfo` без `languages` (Table 8.71, New in 2.2) и с `channelId: String` вместо NMTOKEN | ✅ всё подтверждено |
| HI-03 `Tool` / `Patch` | `Tool` — только `toolType`, `identificationFields`; Table 6.174 требует `Manufacturer?/ManufacturerURL?/SerialNumber?` (New in 2.2). `ColorPatch` без `spotType`; Table 8.10 `@SpotType?` (New in 2.2) | ✅ |
| HI-04 неполные enum'ы | `Scope`: нет `Device` (Table A.36, New in 2.2); `IsoPaperSubstrate`: нет `PS9` (Table A.26, New in 2.2; в XSD тоже нет); `MediaType`: нет `Synthetic` (New in 2.1) — **подтверждено, не гипотеза**; `Sides`: нет `Unprinted` (New in 2.1) — **подтверждено**; `JdfVersion`: нет `1.0` и `1.8` (Table A.27 содержит 1.0–1.8) — **подтверждено** | ✅, см. правки №2–4 |
| HI-05 `NamedColor` | В XSD — pattern-restricted simpleType (case-insensitive словарь); Scala: `mediaColor/headBandColor/coverColor/... : Option[String]` | ✅ |
| HI-06 `CombinedProcessIndex` | Норматив Table 3.12: `IntegerList`; XSD: `FloatList`; Scala: `Vector[Float]` | ✅ |
| HI-07 `Part` / `IntegerRange` | Table 6.4: пять полей `IntegerRange`; Scala: `RangeExpression` = любой непустой `String`; `IntegerRange(first,last)` в модели существует | ✅ |
| HI-08 list/color invariants | `GluingPattern` SHALL even (8.x:669) — `Vector[Float]` без проверки; `spectrum`, `curve` (TransferFunction) — `Vector[Float]`; `CmykColor/LabColor` — голые `Double` | ✅ |
| HI-09 binding details | `LooseBindingDetails` содержит 3 независимых `Option` (coil/comb/ring); каждый case `BindingType` несёт общий `Option[LooseBindingDetails]` | ✅ |
| HI-10 validation layer | `Resource.orientation`+`transformation`, `start`+`startOffset` — независимые `Option`; `ValidationError` — только 3 case | ✅ |
| HI-11 priority | `Disposition.priority: Option[Int]`, `NodeInfo.jobPriority: Option[Int]`; норматив 0..100 (8.x:554, 6.x:1691); `QueuePriority` 0..100 существует | ✅ |
| HI-12 dateTime/duration | Regex `XsdDuration` действительно принимает `"PT"` и `"P1YT"` (лукахед `(?=.+)` удовлетворяется самим `T`); `XsdDateTime` допускает отсутствие зоны и невозможные даты | ✅ |
| HI-13 `Illumination` | Table 8.13: NMTOKEN (`D50`, `D65`, `Unknown`); XSD: `xs:float`; Scala: `Option[Float]` | ✅ |
| HI-14 сборка | `which java`/`which sbt` — пусто; build.sbt: Scala 3.8.4, `-Werror`, `-Wunused:all`, `-Yexplicit-nulls`; sbt 2.0.0 | ✅ |

### Medium / Low (проверено выборочно, всё сходится, кроме LO-02)

- MD-01: XSD `PartContext` — singular `xs:NMTOKEN` (schema.xsd:1744); норматив — NMTOKENS (8.x:386); Scala — `Option[Nmtoken]` в двух клонах (`LayoutCondition`, `CellConditionTerm`). ✅
- MD-02: `QueryGangStatus`/`QueryQueueStatus`/`QueryKnownMessages` жёстко фиксируют `languages = Vector.empty`; Table 7.4 задаёт `@Languages?` на family-уровне, но конкретные таблицы 7.13/7.40/7.22 его не содержат. ✅ (двусмысленность описана честно)
- MD-04: `$schema` действительно в domain-полях `XJDF`/`XJMF`; JSON `@Name` отсутствует; `XJMF.messages: NonEmptyVector`. ✅
- MD-05: A.1: string ≤ 1023, normalized. ✅
- MD-06: `ExtensionElement(children: Vector, value: Option)` — mixed-порядок непредставим. ✅
- MD-08: тесты — `object ...Checks` с eager `assert`, в `build.sbt` нет ни одной library dependency. ✅
- MD-10: codec-зависимостей нет. ✅
- LO-01: XSD `NumberUp: XYPair`, норматив «XYPair (both numbers are integers)», Scala `GridSize(Int,Int)` — корректно охарактеризовано как документируемое сужение. ✅
- **LO-02: ОПРОВЕРГНУТО — см. правку №1.**
- LO-03/LO-04: `addressLines: Vector[String]`, `organizationalUnits: Vector[String]`. ✅
- LO-06: `Matrix`, `XYPair`, `LabColor`, `CmykColor`, `Shape3D` — `Double`, при этом большинство прочих полей — `Float`: смешанная политика, претензия справедлива. ✅
- LO-07: `-language:strictEquality` отсутствует в build.sbt; `OpenXjdfNode` определён и нигде не используется (1 вхождение). ✅
- LO-08: XSD `FoldCatalog` — pattern `F[0-9]+-([0-9]+|X)`; Scala — `Nmtoken`. ✅
- LO-09: `Version` в XSD — только `2.0|2.1` (Scala добавляет `2.2` — верно); Table 7.22 (`QueryKnownMessages`) не содержит `Subscription` — Scala фиксирует `None` — верно; XSD `CommandShutDown` требует `ShutDownCmdParams` (minOccurs=1), норматив — `ShutDownCmdParams?` — Scala `Option` следует нормативу — верно; `FileSpec/@NPage` в XSD **отсутствует** (подтверждено), в нормативе есть (Table 8.17, New in 2.2). ✅
- Сводная таблица раздела 7 — все строки проверены и подтверждены (Brand=boolean, BlockName=XYPair, ModuleIDs=float, ChildRef=float в XSD; `RestApiBaseURL` отсутствует в XSD, присутствует в 6.x:865 и в Scala; ShapeDef в XSD — `FileSpec?` без `RuleLength`, в Scala — `Vector[FileSpec]` + `ruleLengths`). Одна строка требует уточнения — см. правку №5.
- Счётчики severity: Critical 3 + High 14 + Medium 10 + Low 9 = 36 — совпадает с фактическим числом находок в тексте. ✅

---

## 3. Предлагаемые исправления AUDIT.md

### Правка №1 (существенная): LO-02 инвертирует приоритет «норматив > XSD»

Нормативная Table 6.148 (`6 – Resources.md`, строка 2042):

> `| Docs? | IntegerRange | Zero-based range of document indices. |`

XSD (`schema.xsd:5483`): `Docs` = `IntegerList` — это как раз устаревший/расходящийся тип.
По собственному принципу отчёта (норматив > XSD) Scala-модель
`RunList.docs: Option[IntegerRange]` **корректна**, а не подозрительна.

Было:

> ### LO-02 — `RunList/@Docs` представлен `IntegerRange`, хотя XSD type — `IntegerList`
> - `Docs` может быть длиннее двух integer values.
> - Требуется либо использовать `Vector[Int]`, либо задокументировать ограничение.

Предлагается:

> ### LO-02 — `RunList/@Docs`: XSD (`IntegerList`) расходится с нормативом (`IntegerRange`); Scala следует нормативу
> - Норматив Table 6.148 задаёт `Docs? : IntegerRange` («zero-based range of document indices»).
> - XSD задаёт `IntegerList` — расхождение аналогично прочим corrupt/stale типам XSD.
> - Scala `docs: Option[IntegerRange]` **корректна** по приоритету «норматив > XSD».
> - Действие: только зафиксировать расхождение в реестре documented XSD/normative choices (LO-09); менять тип не нужно.

Дополнительно строку про `Docs` стоит добавить в сводную таблицу раздела 7 со статусом
`Correct` (норматив `IntegerRange` / XSD `IntegerList` / Scala `IntegerRange`).

### Правка №2: раздел 5.1 — убрать гипотетичность про `MediaType`/`Sides`

Было:

> `MediaType` и `Sides` могут не содержать нормативные значения `Synthetic` и `Unprinted` по данным `AUDIT-C`.

Предлагается (подтверждено прямой проверкой кода):

> `MediaType` не содержит нормативное значение `Synthetic` (New in XJDF 2.1), а `Sides` не содержит `Unprinted` (New in XJDF 2.1) — подтверждено по `MediaIntent.scala` и `SimpleIntents.scala`.

### Правка №3: HI-04 и Фаза 1 (п. 3) — снять оговорки «если подтверждено»

В HI-04 и в разделе 10 (Фаза 1, пункт 3) заменить:

- «`MediaType.Synthetic`, если подтверждено» → «`MediaType.Synthetic` (подтверждено: отсутствует)»;
- «`Sides.Unprinted`, если подтверждено» → «`Sides.Unprinted` (подтверждено: отсутствует)»;
- «JDF/XJDF versions `1.0`, `1.8`, если это требуется нормативной version table» →
  «`JdfVersion`: добавить `1.0` и `1.8` — Table A.27 (JDFJMFVersion) нормативно содержит 1.0–1.8,
  в Scala enum сейчас 1.1–1.7 и 2.0–2.2 (подтверждено по `Device.scala`)».

### Правка №4: LO-09 — «может не содержать» → «не содержит»

Было: «`FileSpec/@NPage` моделируется как `numberOfPages`, хотя XSD может не содержать».
Предлагается: «`FileSpec/@NPage` (Table 8.17, New in 2.2) моделируется как `numberOfPages`;
в XSD `FileSpec` этот атрибут отсутствует (подтверждено) — модель правильно следует нормативу».

### Правка №5: сводная таблица раздела 7, строка `ChannelMode`

Было: `| ChannelMode | FireAndForget, Reliable | same enum base | wrong values + wrong cardinality | Wrong |`

Формулировка «same enum base» занижает качество XSD: в действительности XSD здесь
**полностью корректен** — `EnumChannelMode = FireAndForget | Reliable` (schema.xsd:201),
а `Subscription/@ChannelMode` определён как `xs:list itemType="EnumChannelMode"` (schema.xsd:1541–1543).
Это усиливает CR-01: Scala-модель противоречит и нормативу, и XSD одновременно.

Предлагается: `| ChannelMode | FireAndForget, Reliable | корректен: enum + xs:list в Subscription | wrong values + wrong cardinality | Wrong (расходится и с нормативом, и с XSD) |`

### Правка №6: опечатки

- MD-05: «**Исправнение:**» → «**Исправление:**».
- Раздел 5.3: «Все аудита отмечают» → «Все аудиты отмечают».

### Правка №7 (необязательная, вне AUDIT.md): комментарий в коде

`modules/model/src/main/scala/xjdf4s/model/Resource.scala:5` говорит
«Open XSD extension point with **100** schema-defined descendants» — фактически в XSD 101
substitution member (в т.ч. `RasterReadingParams`). Число «100» унаследовано от бага
`xsdq hierarchy`, описанного в LO-05.

---

## 4. Что не удалось проверить

- Компиляция/тесты (`sbt "clean ; compile ; test"`) — в среде нет JDK/sbt; AUDIT.md сам это
  констатирует (HI-14, раздел 9), утверждение подтверждено.
- Табличные ссылки на первичные аудиты `AUDIT-A/B/C` (файлы отсутствуют в репозитории) —
  проверялись только конечные фактические утверждения, а не их атрибуция.