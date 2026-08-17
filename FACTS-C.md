# FACTS — Факт-чек `AUDIT.md` и предложения по исправлению

Статус: черновик для правок. Файл, который проверяется: `AUDIT.md`.
Цель: отделить подтверждённые находки от неточностей и предложить конкретные правки текста `AUDIT.md`.

## 0. Методология и объём проверки

Факт-чек выполнен по трём источникам:

1. **Текущая Scala-модель** в `modules/` (`core`, `model`, `messaging`, `protocol`) — фактическое состояние кода.
2. **Нормативная документация XJDF/XJMF 2.2** в `reference/xjdf/*.md` — приоритетный источник истины.
3. **XSD-index** через инструмент `reference/xjdf/tool/xsdq.py` (`summary`, `get`, `attrs`, `children`, `hierarchy`) и прямой разбор `xsd-index.json` — второй источник.

Ключевые количества проверены напрямую:

| Сущность | Заявлено в AUDIT.md | Проверено | Статус |
|---|---:|---:|---|
| Global elements (XSD) | 365 | 365 (`xsdq summary`) | ✅ |
| Complex types (XSD) | 366 | 366 (`xsdq summary`) | ✅ |
| Simple types (XSD) | 228 | 228 (`xsdq summary`) | ✅ |
| `SpecificResource` XSD substitution members | 101 | 101 рёбер `substitution_group` → `SpecificResource` | ✅ |
| `xsdq hierarchy SpecificResource` (complexType) | 100 | `derivedCount = 100`, `RasterReadingParams` отсутствует | ✅ |
| Конкретных `SpecificResource` в модели | 102 | 102 уникальных `resources.*` в `StandardSpecificResource` (101 XSD + `SheetOptimizingReport`) | ✅ |
| `ProductIntent` | 14 | 14 в `StandardProductIntent` | ✅ |
| XJMF Command | 11 | 11 в `StandardCommand` | ✅ |
| XJMF Query | 8 | 8 в `StandardQuery` | ✅ |
| XJMF Response | 18 | 18 в `StandardResponse` | ✅ |
| XJMF Signal | 7 | 7 в `StandardSignal` | ✅ |
| Итого сообщений | 44 | 44 (`StandardMessage`) | ✅ |
| Audits | 5 | 5 (`AuditName`: Created, Notification, ProcessRun, Resource, Status) | ✅ |
| Счётчики severity | 3/14/10/9 = 36 | 3 CR + 14 HI + 10 MD + 9 LO = 36 | ✅ |

Итоговый вердикт факт-чека: **AUDIT.md в подавляющем большинстве точен.** Все три Critical-находки, 13 из 14 High и все Medium/Low подтверждены прямым сопоставлением с кодом и нормативом. Обнаружены **2 существенные неточности** (одна из них — внутри `HI-04`, другая — внутри `HI-02`) и несколько мелких уточнений.

---

## 1. Подтверждённые находки (сводка сверки)

Ниже перечислены находки, которые воспроизводятся по коду и нормативу, с указанием строк, где это подтверждается.

### Critical

| ID | Суть | Подтверждение |
|---|---|---|
| CR-01 `ChannelMode` | Норматив Table A.10 = `FireAndForget \| Reliable`; `Subscription/@ChannelMode` — ordered list (Table 7.5), `Signal/@ChannelMode` — single (Table 7.7). Код: `Message.scala` enum = `Reliable, Simulate, Transactional, Unreliable`; `Subscription.channelMode: Option[ChannelMode]`. | ✅ Полностью подтверждено |
| CR-02 `BoxFoldingParams` | Норматив Table 6.17/6.19/6.20: `BoxFoldAction*` повторяемый, action `Glue` отсутствует, дочерний `Glue?`, верхнеуровневый `Glue*` deprecated. Код: `action: BoxFoldAction` (единственный, обязательный) + обязательный `glue: Glue`; в `BoxFoldActionType` нет `Glue`. | ✅ Полностью подтверждено |
| CR-03 `MediaLayers` | Норматив §8.28/Table 8.45: ordered `Glue* \| Media*`, порядок слоёв значим, JSON-exception `@Name`. Код: `MediaLayers(glue: Glue, media: Media)` — фиксированная пара. | ✅ Полностью подтверждено |

### High

| ID | Суть | Подтверждение |
|---|---|---|
| HI-01 `GeneralId.DataType` | Table A.14 = `boolean, dateTime, duration, float, integer, NamedFeature, NMTOKEN, string`. Код: `Boolean, DateTime, Double, Duration, Integer, Name, Nmtoken, String, Uri` (нет `float`, `NamedFeature`; лишние `Double`, `Name`, `Uri`). | ✅ |
| HI-02 отсутствующие 2.2-поля | `ResourceQueryParams.types` нет; `SignalResource.replaceAfter/replaceBefore` нет; `SubscriptionInfo.languages` нет. (Про `channelId` — см. правку ниже.) | ✅ (с оговоркой, см. §2) |
| HI-03 `Tool` / `Patch` | Норматив Table 6.174: `Manufacturer?`, `ManufacturerURL?`, `SerialNumber?`; Table 8.10: `Patch/@SpotType? = Emulated\|Spot`. Код: `Tool` без этих атрибутов; `ColorPatch` без `spotType`. | ✅ (имя `Patch` в модели — `ColorPatch`) |
| HI-04 недостающие значения enum | `Scope.Device` (A.36), `ISOPaperSubstrate.PS9` (A.26), `MediaType.Synthetic` (A.30), `Sides.Unprinted` (A.40) — все отсутствуют в модели. (Про версии — см. правку ниже.) | ✅ (с оговоркой про версии, §2) |
| HI-05 `NamedColor` = `String` | `NamedColor` — restricted simple type (pattern vocabulary). В модели все соответствующие поля (`coverColor`, `bindingColor`, `ribbonColor`, `foilColor`, `mediaColor`, `backCoverColor`, `reinforceColor`, `headBandColor`) — `Option[String]`; отдельного типа `NamedColor` нет. | ✅ |
| HI-06 `CombinedProcessIndex` | Норматив Table 3.12: `IntegerList`. Код: `combinedProcessIndex: Vector[Float]` (`Resource.scala:140`). | ✅ |
| HI-07 `Part` → `IntegerRange` | Норматив: `Part/@DocIndex`, `@PageNumber`, `@RunIndex`, `@SetIndex`, `@SheetIndex` — двухцелочисленные `IntegerRange` (`"42 42"`). Код: `RangeExpression` (opaque `String`). | ✅ |
| HI-08 list/color invariants | `Glue.gluingPattern: Vector[Float]` без чётности (норматив «SHALL contain an even number of entries»). | ✅ |
| HI-09 binding details | `LooseBindingDetails` содержит три независимых `Option` (`coilBinding`, `combBinding`, `ringBinding`); `BindingSpecification.CoilBinding(details: Option[LooseBindingDetails])` позволяет Comb-детали под Coil. | ✅ |
| HI-10 отсутствие validation layer | Корректная, но в основном дизайн-находка (нет `validate` API; `ValidationError` ограничен). | ✅ |
| HI-11 priority 0..100 | `Disposition.priority: Option[Int]`, `NodeInfo.jobPriority: Option[Int]` — bare `Int`. | ✅ |
| HI-12 dateTime/duration | Regex `XsdDateTime` допускает отсутствие зоны; regex `XsdDuration` (`-?P(?=.+)(...)?`) принимает `PT`, `P1YT`. Норматив требует зону для `dateTime`. | ✅ |
| HI-13 `Illumination` | Table 8.13: `NMTOKEN` (`D50, D65, Unknown`). Код: `illumination: Option[Float]`. | ✅ |
| HI-14 сборка `sbt clean;compile;test` | `java`/`sbt` недоступны, сборка не выполнялась — констатируется как факт среды. | ✅ |

### Medium / Low

`MD-01` (`PartContext` singular `Nmtoken` vs `NMTOKENS`), `MD-03` (XsdId = declaration+reference без graph validation), `MD-05` (XJDF `string` ≤ 1023, normalized — Appendix A.1), `MD-08` (тесты — `object ...Checks` с eager `assert`, без framework), `MD-09` (docs `102/102`, `44/44` только по именам), `MD-10` (нет codec), `LO-01` (`NumberUp` = `GridSize` vs XYPair), `LO-02` (`RunList/@Docs` = `IntegerRange`, XSD `IntegerList`), `LO-03` (`Address.addressLines: Vector[String]`), `LO-04` (`Company.organizationalUnits: Vector[String]`), `LO-08` (`foldCatalog: Option[Nmtoken]`) — **все подтверждены** по коду/нормативу.

---

## 2. Найденные ошибки/неточности — предлагаемые правки `AUDIT.md`

### Правка 1 (важная) — `HI-04`: подпункт про «JDF/XJDF версии 1.0, 1.8» наведён не на ту сущность

**Текущий текст `AUDIT.md` (таблица в `HI-04`):**

> `JDF/XJDF versions` | `1.0`, `1.8`, если это требуется нормативной version table

**Почему это неточно:**

- Атрибут `@Version` корня XJDF/XJMF имеет тип `XJDFXJMFVersion` (Table A.52), значения которого **только** `2.0`, `2.1`, `2.2`.
- Scala `enum Version` (`core/Values.scala`) = `V2_0, V2_1, V2_2` — **корректен** и **не должен** содержать `1.x`. Включение `1.0`/`1.8` сюда было бы ошибкой.
- Значения `1.x` относятся к отдельному типу `JDFJMFVersion` (Table A.27 = `1.0`–`1.8`), который используется, в частности, для `Device/@JDFVersions` (Table 6.x: «whitespace separated list of JDF and XJDF versions»).
- Именно там есть реальный пробел: `enum JdfVersion` (`resources/Device.scala`) содержит `V1_1..V1_7, V2_0, V2_1, V2_2` и **не содержит `1.8` и `1.0`**.

**Предлагаемый текст (замена строки в таблице `HI-04`):**

> `JdfVersion` / `Device/@JDFVersions` | отсутствуют `1.0`, `1.8` (норматив Table A.27 = 1.0–1.8)
> Примечание: `enum Version` (для `@Version` XJDF/XJMF) корректен и ограничен `2.0|2.1|2.2` (Table A.52); `1.x` к нему добавлять не следует.

**Правки в §10 «Фаза 1»:** пункт про «JDF/XJDF versions по нормативной таблице» переформулировать как «добавить в `JdfVersion` значения `1.8` (и при необходимости `1.0`), не трогая `Version`».

---

### Правка 2 (важная) — `HI-02`: утверждение «`SubscriptionInfo/@ChannelID` должен быть `NMTOKEN`» не подтверждается нормативом

**Текущий текст `AUDIT.md` (таблица в `HI-02`):**

> `SubscriptionInfo/@ChannelID` | `SubscriptionInfo` | Должен быть `NMTOKEN`, а не unconstrained `String`

**Почему это неточно:**

- XSD-индекс типизирует `SubscriptionInfo/@ChannelID` как **`builtin:xs:string`** (проверено через `xsdq attrs`; атрибуты: `ChannelID: xs:string`, `DeviceID: xs:NMTOKEN`, `MessageType: xs:NMTOKEN`).
- В нормативном тексте `reference/xjdf/7 – Messaging.md` `SubscriptionInfo/@ChannelID` **не** описан как `NMTOKEN` (тип `NMTOKEN` встречается у `CommandStopPersistentChannel/@ChannelID` — это другой атрибут).
- Принцип самого аудита «normative > XSD» здесь не даёт основания требовать `NMTOKEN`: нет нормативного указания на `NMTOKEN`.

**Предлагаемый текст (замена строки в таблице `HI-02`):**

> `SubscriptionInfo/@ChannelID` | `SubscriptionInfo` | Сейчас — unconstrained `String` без валидации; по XSD тип `xs:string`. Рекомендуется выделить в opaque-тип с валидацией, но без требования `NMTOKEN` (норматив тип `NMTOKEN` не задаёт).

Соответственно, в фикс-коде `HI-02` заменить `channelId: Nmtoken` на `channelId: ChannelIdentifier` (или `String` + валидатор), с пояснением.

---

### Правка 3 (уточнение) — `HI-04`: снять неопределённость «могут не содержать»

Текст `AUDIT.md` в §5.1 и `HI-04` говорит «`MediaType` и `Sides` **могут** не содержать нормативные значения `Synthetic` и `Unprinted` по данным AUDIT-C». Оба значения **точно отсутствуют** (проверено: `MediaType` в `MediaIntent.scala` и `resources/MediaAndColor.scala` без `Synthetic`; `Sides` в `SimpleIntents.scala` без `Unprinted`; норматив Table A.30 / A.40). Предлагается убрать «могут» и «по данным AUDIT-C»: «**не содержат** `MediaType.Synthetic` и `Sides.Unprinted`».

---

### Правка 4 (уточнение) — `HI-02`: отметить deprecated `Subscription/@Languages`

Модель сохраняет `Subscription.languages: Vector[LanguageTag]` (`messaging/Message.scala:9`). По нормативу (Table 7.5) `Subscription/@Languages` **Deprecated in XJDF 2.2** (перенесён в `Query`… и `SubscriptionInfo`). AUDIT.md корректно требует `SubscriptionInfo.languages`, но полезно добавить примечание, что `Subscription.languages` остаётся deprecated-совместимым и удалять его не нужно (только зафиксировать deprecation). Это не ошибка, а полнота.

---

### Правка 5 (мелкая) — комментарий модели `SpecificResource` (дополнение, выходит за рамки AUDIT.md)

`Resource.scala:5` комментирует `SpecificResource` как «100 schema-defined descendants». Это повторяет ошибку `xsdq hierarchy` (100), тогда как авторитетный подсчёт substitution members — **101** (+ `SheetOptimizingReport` = 102). Предлагается в рамках темы MD-09/LO-05 добавить в AUDIT.md примечание: обновить комментарий в модели на «101 schema-defined descendants», поскольку docs/resource-coverage.md уже корректно отражает `RasterReadingParams` как substitution-group.

---

## 3. Дополнительные наблюдения (не ошибки AUDIT.md, но для справки)

- `xsdq hierarchy` (complexType-запрос) возвращает 100 и пропускает `RasterReadingParams`; рёбер `substitution_group → SpecificResource` — 101. Утверждение AUDIT.md в §5.3/LO-05 о «инструментальной ошибке» **подтверждается**.
- Имя класса в модели — `ResourceQueryParams`, тогда как AUDIT.md называет его `ResourceQuParams` (как в нормативном тексте). Это вопрос терминологии; в правках можно добавить сноску о соответствии имён.
- `ColorPatch` в модели соответствует нормативному `Patch`; правки HI-03/§10 должны упоминать оба имени.

---

## 4. Итоговая рекомендация по правкам AUDIT.md

1. **HI-04 (таблица + §10)** — переписать подпункт про версии: добавить `1.8`/`1.0` в `JdfVersion` (`Device/@JDFVersions`), явно указав, что `Version` для `@Version` корректен (2.0/2.1/2.2).
2. **HI-02 (таблица + фикс-код)** — смягчить/уточнить требование `NMTOKEN` для `SubscriptionInfo/@ChannelID`; предложить opaque `ChannelIdentifier`.
3. **HI-04 / §5.1** — убрать «могут», зафиксировать фактическое отсутствие `MediaType.Synthetic` и `Sides.Unprinted`.
4. **HI-02** — добавить примечание о deprecated `Subscription/@Languages`.
5. **MD-09/LO-05** — отметить расхождение комментария модели «100» vs авторитетных «101».
6. Прочие 33 находки (3 CR + 12 HI + 10 MD + 9 LO за вычетом двух правок) подтверждены и **могут остаться без изменений**.

После внесения этих правок `AUDIT.md` будет точно отражать фактическое состояние модели относительно нормативной документации XJDF/XJMF 2.2 и XSD-индекса.