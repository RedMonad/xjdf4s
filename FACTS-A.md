# Факт-чек AUDIT.md

Проверка выполнена по трём источникам:

1. Нормативная документация XJDF/XJMF 2.2 — `reference/xjdf/*.md`.
2. XSD-индекс — `reference/xjdf/tool/xsd-index.json` через `xsdq.py`.
3. Фактический Scala-код — `modules/{core,model,messaging,protocol}`.

Дата проверки: 2026-08-17.

---

## 1. Вывод

`AUDIT.md` в целом **точен**: подавляющее большинство находок (включая все три Critical, все 14 High, все 10 Medium и почти все Low) подтверждаются нормативным текстом и фактическим кодом. Консолидированный вердикт **«Incomplete»** обоснован.

Обнаружено **одно фактическое заблуждение** (инструментальное объяснение расхождения 100/101 у `RasterReadingParams`) и **несколько неточностей/пробелов**, которые стоит исправить. Критические семантические находки (CR-01…CR-03) — корректны.

---

## 2. Подтверждённые утверждения (выборка)

Проверены и подтверждены как нормативным текстом, так и кодом:

| Находка | Подтверждение |
|---|---|
| Инвентарь: 365 elements / 366 complexTypes / 228 simpleTypes | `xsdq summary` + прямой подсчёт индекса (959 узлов) |
| 191 закрытая перечислимая (enumeration simpleTypes) | прямой подсчёт индекса |
| CR-01 `ChannelMode` | Табл. A.10 = `FireAndForget \| Reliable`; код: `Reliable, Simulate, Transactional, Unreliable`; `Subscription.channelMode: Option[ChannelMode]`; Табл. 7.5 (list) и 7.7/`Signal` (single) |
| CR-02 `BoxFoldingParams` | Табл. 6.17 (`BoxFoldAction*`, `Glue*` deprecated), 6.19 (`Glue?` child, New 2.2), 6.20 (action `Glue`); код: фиксированный `action: BoxFoldAction` + обязательный верхнеуровневый `glue: Glue` |
| CR-03 `MediaLayers` | Табл. 8.45: ordered `Glue* \| Media*` + JSON `@Name` exception; код: `MediaLayers(glue, media)` — фиксированная пара |
| HI-01 `GeneralId.DataType` | Табл. A.14 = boolean, dateTime, duration, float, integer, NamedFeature, NMTOKEN, string; код: `Boolean, DateTime, Double, Duration, Integer, Name, Nmtoken, String, Uri` |
| HI-02 отсутствующие 2.2-поля | `ResourceQuParams/@Types` (Табл. 7.49), `SignalResource/@ReplaceAfter|@ReplaceBefore` (стр. 904–905), `SubscriptionInfo/@Languages` (Табл. 8.71), `@ChannelID` NMTOKEN (Табл. 8.71) |
| HI-03 `Tool`/`Patch` | Табл. 6.174 (Manufacturer/ManufacturerURL/SerialNumber, New 2.2); `@SpotType` (Табл. 8.10); код: отсутствуют |
| HI-04 недостающие enum-значения | `Scope.Device` (Табл. A.36), `ISOPaperSubstrate.PS9` (Табл. A.24), `MediaType.Synthetic`, `Sides.Unprinted` — отсутствуют в коде |
| HI-05 `NamedColor` как `String` | simpleType `NamedColor` = xs:string + pattern-vocabulary; код: `String` в `Color`/`Media`/`MediaIntent`/`MiscConsumable` и др. |
| HI-06 `CombinedProcessIndex` | Норм. `IntegerList` (Табл. 3.12/структура); XSD = `FloatList`; код = `Vector[Float]` |
| HI-07 `Part` IntegerRange | Табл. 6.4: DocIndex/PageNumber/RunIndex/SetIndex/SheetIndex = `IntegerRange`; код = `RangeExpression` (opaque String) |
| HI-08/11/12/13 | color/list invariant'ы; `Disposition.priority: Option[Int]`, `NodeInfo.jobPriority: Option[Int]`; regex `XsdDateTime`/`XsdDuration` (допускают `PT`, `P1YT`, невалидные даты); `Illumination: Option[Float]` (норм. NMTOKEN) |
| MD-01 `Condition/@PartContext` | норм. `NMTOKENS`; код `Option[Nmtoken]` |
| Таблица XSD-расхождений (разд. 7) | `Brand=boolean`, `BlockName=XYPair`, `ModuleIDs=float`, `ChildRef=float`, `CombinedProcessIndex=FloatList`, `PartContext=NMTOKEN`, `Illumination=float` — всё подтверждено по индексу |
| Разд. 8.1 «уже защищено типами» | `NonEmptyVector`, `TwoOrMore`, `FileLocation`, `DispositionTime`, `ColorSurfaces`, `BindingSpecification`, `WasteOrigin`, `EvenPageCount`, `QueuePriority` — присутствуют |
| Разд. 9 сборка | `build.sbt`: Scala `3.8.4`, флаги `-Werror`, `-Wunused:all`, `-Yexplicit-nulls`; `project/build.properties` = sbt `2.0.0`; Java/sbt в среде отсутствуют |
| Количество сообщений 44 | `StandardCommand`=11, `StandardQuery`=8, `StandardResponse`=18, `StandardSignal`=7 |
| 5 аудитов | `AuditCreated, AuditNotification, AuditProcessRun, AuditResource, AuditStatus` |
| 14 ProductIntent, 102 SpecificResource | union-типы `StandardProductIntent`/`StandardSpecificResource` |
| Severity-счётчики 3+14+10+9=36 | внутренне согласованы |

---

## 3. Ошибки и неточности — предлагаемые исправления

### 3.1. ОШИБКА: объяснение расхождения 100/101 (`RasterReadingParams`)

**Где:** разд. 5.3, LO-05, а также строка таблицы в разд. 3 («…который не виден в `xsdq hierarchy`, но виден в `get`»).

**Что утверждается:** `xsdq hierarchy SpecificResource` возвращает 100 потомков, а «авторитетный» подсчёт substitution members даёт 101; `RasterReadingParams` якобы пропущен из-за «инструментальной/индексной особенности».

**Факт:** причина — **не** особая индексная особенность и **не** специфический пропуск `RasterReadingParams`. Подкоманда `hierarchy` имеет дефолтный `--limit 100`, который просто обрезает список `substitutionMembers`:

```text
xsdq hierarchy element:{…}SpecificResource             → substitutionMembers: 100
xsdq hierarchy element:{…}SpecificResource --limit 200 → substitutionMembers: 101  (включая RasterReadingParams)
```

`RasterReadingParams` **присутствует** в `hierarchy` при достаточном `--limit`. По edges индекса substitution-group членов `SpecificResource` ровно **101**.

**Исправление:** переписать 5.3 и LO-05: это не «индексная особенность, опускающая RasterReadingParams», а усечение по дефолтному `--limit 100`. Рекомендуемая формулировка:

> `xsdq hierarchy SpecificResource` по умолчанию обрезает `substitutionMembers` до 100 (дефолтный `--limit 100`); с `--limit 200` возвращаются все 101, включая `RasterReadingParams`. Это CLI-поведение инструмента, а не дефект модели или индекса.

Дополнительно: комментарий в `modules/model/src/main/scala/xjdf4s/model/Resource.scala` («100 schema-defined descendants») — off-by-one; schema-defined потомков **101** (см. `RasterReadingParams`).

---

### 3.2. НЕТОЧНОСТЬ: версии JDF/XJDF (HI-04 и таблица разд. 7)

**Где:** HI-04 («JDF/XJDF versions `1.0`, `1.8`, если это требуется нормативной version table») и строка «JDF/XJDF versions» в фазе 1 разд. 10.

**Факт (уточнение):** замечание верно по сути, но сформулировано слишком расплывчато. Конкретика:

- Нормативная Табл. **A.27 `JDFJMFVersion`** = `1.0`…`1.8` (девять значений; 2.x там **нет**).
- В коде есть отдельный enum `JdfVersion` (`modules/model/.../resources/Device.scala`, атрибут `Device/@JDFVersions`): значения `1.1`…`1.7`, `2.0`, `2.1`, `2.2`.
- То есть пропущены именно **`1.0` и `1.8`**, а `2.x` (XJDF-версии) добавлены. `Device/@JDFVersions` по нормативу — «list of **JDF and XJDF** versions», т.е. объединение `JDFJMFVersion` (1.0–1.8) и `Version` (2.0–2.2).

**Исправление:** сделать формулировку определённой и точной:

> enum `JdfVersion` (для `Device/@JDFVersions`) неполон: отсутствуют JDF `1.0` и `1.8` (Табл. A.27 `JDFJMFVersion`), при этом включает XJDF `2.0/2.1/2.2`. Следует либо дополнить `JdfVersion` значениями `1.0`/`1.8`, либо моделировать `@JDFVersions` как объединение `JDFJMFVersion ∪ Version`.

Убрать оговорку «если это требуется нормативной version table» — она не требуется, факт доказан.

---

### 3.3. ЗАМЕЧАНИЕ: имя `ResourceQuParams` vs `ResourceQueryParams`

**Где:** HI-02 (и разд. 7) использует нормативное имя `ResourceQuParams`.

**Факт:** нормативный/XSD элемент называется **`ResourceQuParams`** (Табл. 7.49, `#### 7.14.1.1 ResourceQuParams`), а Scala-класс — **`ResourceQueryParams`** (`StatusNotificationResourceMessages.scala`). Расхождение имён не отражено в отчёте.

**Исправление:** добавить в HI-02 (или LO-09) пункт о переименовании/алиасе `ResourceQueryParams → ResourceQuParams` для round-trip fidelity (аналогично уже отмеченному MD-10 про `elementName` у aliases).

---

### 3.4. Мелочи (не ошибки, но для полноты)

- **HI-08** дополнительно: `Patch/@NeutralDensity` нормативно ограничен `0.001…10` (Табл. 8.10), а в коде `neutralDensity: Option[Float]` — ещё один пример неограниченного bounded-значения, который стоит добавить в список HI-08.
- **HI-11** дополнительно: `GangElement.priority: Option[Int]` (`SheetOptimizing.scala`) — ещё одно bare-Int 0..100, не упомянутое в HI-11 (там только `Disposition` и `NodeInfo`).
- **MediaType.Vinyl**: нормативно `Vinyl` *Deprecated in 2.1* с указанием использовать `Synthetic`; в коде `Vinyl` есть, а `Synthetic` нет (HI-04). Полезно явно пометить deprecation-статус `Vinyl`, а не только отсутствие `Synthetic`.
- **Patch**: нормативное имя элемента — `Patch`, в коде — `ColorPatch` (`MarksAndStacking.scala`); аналогичное расхождение имён, стоит добавить к 3.3/MD-10.

---

## 4. Итог

Рекомендуемые правки `AUDIT.md` (по приоритету):

1. **5.3 / LO-05 / разд. 3** — заменить объяснение «индексная особенность» на «дефолтный `--limit 100` у `xsdq hierarchy`»; `RasterReadingParams` не «не виден», а обрезается лимитом.
2. **HI-04** — конкретизировать: пропущены JDF `1.0`/`1.8` в enum `JdfVersion` (`Device/@JDFVersions`).
3. **HI-02 / MD-10** — добавить расхождение имён `ResourceQuParams` (норм.) vs `ResourceQueryParams` (код), и `Patch` vs `ColorPatch`.
4. **HI-08 / HI-11** — расширить списки на `Patch/@NeutralDensity` (0.001–10) и `GangElement/@Priority` (0..100).

Все остальные проверенные утверждения отчёта **подтверждены**.