# Консолидированный отчёт аудита Scala 3 ADT для XJDF/XJMF 2.2

Основание: три первичных аудита — `AUDIT-A`, `AUDIT-B`, `AUDIT-C`.  
Объект: Scala 3 ADT-модель XJDF/XJMF 2.2 в репозитории `xjdf4s`, модули `core`, `model`, `messaging`, `protocol`.  
Принцип консолидации:

1. Нормативные таблицы и текст XJDF/XJMF 2.2 имеют приоритет над XSD/index.
2. XSD/index используется как второй источник, если нормативный текст не противоречит.
3. Текущая Scala-модель и coverage-документы не являются источником истины; они сами подлежат проверке.
4. Дублирующие находки объединены; противоречия между аудитами разрешаются в пользу более доказательных проверок значений, полей и нормативных требований.

---

## 1. Итоговый вердикт

**Итог: модель структурно амбициозна и почти полна на уровне имён сущностей, но не является полной или полностью соответствующей XJDF/XJMF 2.2.**

Консолидированный вердикт:

> **Incomplete / семантически неполная модель.**  
> Структурный каркас — 102 конкретных `SpecificResource`, 14 `ProductIntent`, 44 конкретных XJMF-сообщения, 5 аудитов — присутствует. Однако найдены критические ошибки закрытых словарей, отсутствующие нормативные поля 2.2, неверные типы списков, незащищённые SHALL-инварианты, неполные simple types и отсутствующая доказательная сборка `sbt "clean ; compile ; test"`.

Оценка по слоям:

| Слой | Оценка |
|---|---|
| Инвентарь XSD/index: 365 elements / 366 complex types / 228 simple types | Подтверждено |
| Покрытие имён ресурсов | Complete: 102/102, включая `RasterReadingParams` и нормативный `SheetOptimizingReport` |
| Покрытие имён интенций | Complete: 14/14 |
| Покрытие имён сообщений | Complete: 44/44 |
| Покрытие атрибутов и полей 2.2 | Incomplete |
| Закрытые перечисления | Incomplete / частично wrong |
| Simple types | Incomplete: есть доказанные gaps |
| Межполевые SHALL-ограничения | Incomplete / no validation layer |
| Сборка и тесты | Не подтверждены, `sbt`/Java недоступны |
| Coverage-документация | Частично переоценивает полноту |

---

## 2. Сводка по трём исходным аудитам

| Аудит | Заявленный вердикт | Сильные стороны | Ограничения / что следует скорректировать |
|---|---|---|---|
| `AUDIT-A` | **Incomplete** | Хорошо выявил нормативные пробелы 2.2: `ChannelMode`, `GeneralId.DataType`, `Scope.Device`, `ISOPaperSubstrate.PS9`, отсутствующие поля `ResourceQuParams`, `SubscriptionInfo`, `Tool`, `SignalResource`. Аккуратно работает с приоритетом нормативного текста. | Не покрыл часть критических структурных проблем, найденных в `AUDIT-C`: `BoxFoldingParams`, `MediaLayers`, часть list/color invariants. В заявленных счётчиках severity есть расхождение с перечнем находок. |
| `AUDIT-B` | **Structurally complete with documented semantic gaps** | Полезен для подтверждения entity-name coverage: 365 elements, 102 resources, 14 intents, 44 messages. Верно отмечает, что каркас substitution groups собран. | Слишком оптимистичен. Утверждение о полноте closed enumerations не подтверждено на уровне значений. Пропущены критические дефекты: неверный `ChannelMode`, неполные `Scope`, `ISOPaperSubstrate`, `NamedColor`, отсутствующие 2.2-поля, `BoxFoldingParams`, `MediaLayers`. Его вердикт следует понизить до структурно полного, но семантически неполного. |
| `AUDIT-C` | **Incomplete** | Наиболее полный семантический аудит. Покрывает не только имена, но и значения enum'ов, cardinality, ordered mixed content, list invariants, `BoxFoldingParams`, `MediaLayers`, `Part IntegerRange`, `TransferFunction`, `GluingPattern`, ID/IDREF, wildcard namespace safety, JSON exceptions, string constraints. | Содержит более строгие severity, чем `AUDIT-A`/`AUDIT-B`; часть оценок консолидирована с учётом влияния на совместимость. |

Консолидированная позиция:

- `AUDIT-B` прав в части **entity-name coverage**.
- `AUDIT-A` и `AUDIT-C` совместно доказывают, что **field-level и semantic-level покрытие неполны**.
- `AUDIT-C` даёт наиболее полный набор критических структурных дефектов, поэтому его находки по `BoxFoldingParams`, `MediaLayers`, list/color invariants и `Part` включены в итоговый отчёт.

---

## 3. Подтверждённый инвентарь и покрытие имён

Все три аудита сходятся в базовом инвентаре XSD-index.

| Сущность | Ожидаемое количество | Подтверждено | Комментарий |
|---|---:|---:|---|
| Global elements | 365 | 365 | `xsdq summary` |
| Complex types | 366 | 366 | `xsdq summary` |
| Simple types | 228 | 228 | `xsdq summary` |
| `ProductIntent` descendants | 14 | 14 | Все присутствуют в `StandardProductIntent` |
| `SpecificResource` XSD substitution members | 101 | 101 | Включая `RasterReadingParams`, который не виден в `xsdq hierarchy`, но виден в `get` |
| Нормативный resource вне XSD | 1 | `SheetOptimizingReport` | Таблица 6.162 |
| Итоговое покрытие ресурсов по именам | 102 | 102/102 | `StandardSpecificResource` содержит все 102 имени |
| XJMF Command messages | 11 | 11 | `StandardCommand` |
| XJMF Query messages | 8 | 8 | `StandardQuery` |
| XJMF Response messages | 18 | 18 | `StandardResponse` |
| XJMF Signal messages | 7 | 7 | `StandardSignal` |
| Итого concrete messages | 44 | 44/44 | `StandardMessage` |
| Audits | 5 | 5 | `AuditCreated`, `AuditNotification`, `AuditProcessRun`, `AuditResource`, `AuditStatus` |

Важные подтверждения:

- `RasterReadingParams` действительно является substitution member `SpecificResource`, хотя `xsdq hierarchy` его опускает.
- `SheetOptimizingReport` отсутствует в XSD, но требуется нормативно и представлен в Scala-модели.
- `ProductIntent` остаётся открытым trait'ом для extension intents, что корректно.
- `SpecificResource` и message families также корректно оставляют открытые extension points.
- `StandardProductIntent`, `StandardSpecificResource`, `StandardMessage` — закрытые union'ы стандартных вариантов, что в целом правильно.

---

## 4. Что сделано хорошо

Консолидированные положительные результаты:

1. **Каркас substitution groups собран корректно.**
    - 14 `ProductIntent`.
    - 102 `SpecificResource`, включая XSD member `RasterReadingParams` и нормативный `SheetOptimizingReport`.
    - 44 конкретных XJMF-сообщения.

2. **Открытые extension points оставлены открытыми.**
    - `ProductIntent`.
    - `SpecificResource`.
    - `Message`, `Command`, `Query`, `Response`, `Signal`.
    - Это соответствует механизмам расширения XJDF/XJMF.

3. **Часть XOR-ограничений уже защищена типами.**
    - `FileLocation` для `FileSpec`: URL / UID / FileTemplate+FileFormat / Pipe.
    - `DispositionTime`: MinDuration vs Until.
    - `ColorSurfaces`: исключает дублирование Front/Back.
    - `BindingSpecification` на верхнем уровне защищает несовместимость BindingType/details.
    - `WasteOrigin` защищает at-least-one для `PartWaste`.
    - `EvenPageCount` защищает чётность `LayoutIntent/@Pages`.
    - `NonEmptyVector` и `TwoOrMore` защищают часть cardinality требований.

4. **Часть corrupt XSD типов исправлена в пользу нормативного текста.**
    - `Resource/@Brand` как `String`, а не `Boolean`.
    - `Part/@BlockName` как `Nmtoken`.
    - `PartWaste/@ModuleIDs` как `NonEmptyVector[Nmtoken]`.
    - `AssemblyItem/@ChildRef` как `XsdId`.
    - `Version` включает `2.2`, несмотря на устаревший XSD.

5. **Модель в целом transport-neutral.**
    - Доменные типы не смешиваются напрямую с XML/JSON-представлением.
    - `Extensions`, `QualifiedName` используются как транспортно-нейтральные extension-носители.

---

## 5. Критические расхождения между аудитами и арбитраж

### 5.1. Оценка закрытых перечислений

`AUDIT-B` утверждает, что все 191 closed enumeration представлены как Scala `enum` и ни один не сведён к bare `String`.

Это утверждение консолидированно **отклоняется как достаточное основание для полноты**, потому что:

- `ChannelMode` имеет неверный набор значений и неверную cardinality в `Subscription`.
- `GeneralId.DataType` не соответствует таблице A.14.
- `Scope` не содержит 2.2-значение `Device`.
- `ISOPaperSubstrate` не содержит 2.2-значение `PS9`.
- `MediaType` и `Sides` могут не содержать нормативные значения `Synthetic` и `Unprinted` по данным `AUDIT-C`.
- `NamedColor`, являющийся ограниченным simple type, представлен как `String`.

Проверка «имя enum присутствует» не эквивалентна проверке «значения, cardinality и лексическое пространство корректны».

### 5.2. Вердикт AUDIT-B

`AUDIT-B` корректен в части структурного каркаса, но недостаточно проверяет:

- значения enum'ов;
- нормативные поля 2.2;
- list cardinality;
- ordered mixed content;
- list invariants;
- cross-field SHALL constraints;
- ID/IDREF integrity;
- JSON/XML codec readiness.

Поэтому его вердикт «Structurally complete with documented semantic gaps» в консолидированном отчёте заменяется на:

> **Structurally complete skeleton with critical semantic gaps; overall Incomplete.**

### 5.3. Инструментальная ошибка `xsdq hierarchy`

Все аудита отмечают или используют факт:

- `xsdq hierarchy SpecificResource` возвращает 100 descendants.
- Авторитетный подсчёт substitution members даёт 101.
- Пропущен `RasterReadingParams`.

Это не дефект Scala-модели, а инструментальная/индексная особенность. Модель содержит `RasterReadingParams` корректно.

---

## 6. Консолидированные находки

После дедупликации и арбитража сформирован следующий реестр:

| Severity | Count |
|---|---:|
| Critical | 3 |
| High | 14 |
| Medium | 10 |
| Low | 9 |
| **Total** | **36** |

Ниже приведены консолидированные находки. Severity назначены по влиянию на возможность представить нормативные XJDF/XJMF 2.2 документы и предотвратить недопустимые состояния.

---

## 6.1. Critical findings

### CR-01 — `ChannelMode` имеет неверные значения и теряет list cardinality

**Источники:** `AUDIT-A` ADT-001, `AUDIT-C` ADT-001.

**Нормативная база:**

- Table A.10: `ChannelMode = FireAndForget | Reliable`.
- Table 7.5: `Subscription/@ChannelMode` является ordered list.
- Table 7.7: `Signal/@ChannelMode` — одиночное значение.

**Текущее состояние:**

- Scala enum содержит значения вида `Reliable`, `Simulate`, `Transactional`, `Unreliable`.
- `Subscription.channelMode` представлен как `Option[ChannelMode]`, а не как список.

**Влияние:**

- Невозможно представить валидный `Subscription/@ChannelMode="FireAndForget Reliable"`.
- Невозможно представить `Signal/@ChannelMode="FireAndForget"`.
- Можно представить несуществующие значения.
- Теряется порядок предпочтений persistent channel modes.

**Исправление:**

```scala
enum ChannelMode:
  case FireAndForget, Reliable

// Subscription
channelMode: Vector[ChannelMode]

// Signal
channelMode: Option[ChannelMode]
```

---

### CR-02 — `BoxFoldingParams` моделирует устаревшую XSD-структуру, а не нормативный 2.2

**Источники:** `AUDIT-C` ADT-002.

**Нормативная база:**

- Tables 6.17, 6.19, 6.20.
- Release notes H.1.

**Проблема:**

- Текущая модель требует фиксированную комбинацию одного `BoxFoldAction` и одного верхнеуровневого `Glue`.
- Нормативный 2.2 требует/допускает:
    - повторяющиеся `BoxFoldAction*`;
    - новый action `Glue`;
    - дочерний `Glue` внутри `BoxFoldAction`;
    - старый верхнеуровневый `Glue` может быть deprecated, но не должен быть обязательным.

**Влияние:**

- Невозможно представить нормальные 2.2 folder-gluer sequences.
- Невозможно представить новый action `Glue`.
- Конструктор навязывает недопустимую/устаревшую форму.

**Исправление:**

- Заменить фиксированный product на ordered action vector.
- Добавить `BoxFoldActionType.Glue`.
- Добавить optional child `Glue` в `BoxFoldAction`.
- Верхнеуровневый deprecated `Glue` оставить как optional, но не required.

---

### CR-03 — `MediaLayers` теряет нормативный порядок и cardinality смешанной последовательности `Glue | Media`

**Источники:** `AUDIT-C` ADT-003.

**Нормативная база:**

- Section 8.28 / table 8.45.
- Порядок слоёв SHALL precisely describe layers.
- Допустимы повторяющиеся `Glue*`, `Media*`.
- JSON имеет отдельный `Name` exception.

**Текущее состояние:**

- Модель фиксирует ровно один `Glue` и ровно один `Media` в фиксированном порядке.

**Влияние:**

- Невозможно представить:
    - `Media, Glue, Media`;
    - несколько `Glue`;
    - несколько `Media`;
    - media-only структуры;
    - корректный порядок многослойного материала.

**Исправление:**

```scala
sealed trait MediaLayer
final case class GlueLayer(glue: Glue) extends MediaLayer
final case class MediaLayerValue(media: Media) extends MediaLayer

final case class MediaLayers(layers: Vector[MediaLayer])
```

Дополнительно нужно учесть JSON `Name` exception на уровне codec.

---

## 6.2. High findings

### HI-01 — `GeneralId.DataType` не соответствует Table A.14

**Источники:** `AUDIT-A` ADT-002, `AUDIT-C` ADT-008.

**Нормативные значения:**

```text
boolean
dateTime
duration
float
integer
NamedFeature
NMTOKEN
string
```

**Проблема:**

- Текущий Scala enum содержит несоответствующие варианты, например `Name`, `Uri`, `Double`.
- Отсутствует `NamedFeature`.
- `NamedFeature` является стандартным механизмом gray-box process presets.

**Влияние:**

- Невозможно представить `GeneralID[@DataType="NamedFeature"]`.
- Можно представить недопустимые datatype tokens.

**Исправление:**

- Переписать enum строго по Table A.14.
- Лексические префиксы `xs:` могут быть деталью codec, но не должны менять набор значений.

---

### HI-02 — Отсутствуют новые/нормативные поля XJMF 2.2

**Источники:** `AUDIT-A` ADT-003, ADT-007, ADT-008; `AUDIT-C` ADT-004.

Отсутствуют или неверны:

| Поле | Где | Причина |
|---|---|---|
| `ResourceQuParams/@Types` | `QueryResource` payload | New in 2.2 |
| `SignalResource/@ReplaceAfter` | `SignalResource` | Нормативно требуется для replacement window |
| `SignalResource/@ReplaceBefore` | `SignalResource` | Нормативно требуется для replacement window |
| `SubscriptionInfo/@Languages` | `SubscriptionInfo` | New in 2.2 |
| `SubscriptionInfo/@ChannelID` | `SubscriptionInfo` | Должен быть `NMTOKEN`, а не unconstrained `String` |

**Влияние:**

- 2.2 resource query не может фильтровать по process type.
- Device не может корректно retract/replace previous resource signal.
- Persistent-channel inventory не может сообщить языки originating Query.
- `ChannelID` принимает недопустимые лексические значения.

**Исправление:**

```scala
// ResourceQuParams
types: Vector[Nmtoken] = Vector.empty

// SignalResource
replaceAfter: Option[XsdDateTime]
replaceBefore: Option[XsdDateTime]

// SubscriptionInfo
languages: Vector[LanguageTag]
channelId: Nmtoken
```

---

### HI-03 — Отсутствуют 2.2-поля у `Tool` и `Patch`

**Источники:** `AUDIT-A` ADT-006; `AUDIT-C` ADT-006.

Для `Tool` отсутствуют:

```text
@Manufacturer
@ManufacturerURL
@SerialNumber
```

Для `Patch` отсутствует:

```text
@SpotType = Emulated | Spot
```

**Влияние:**

- 2.2 `Tool` resource truncated.
- 2.2 `Patch` entity field-incomplete.

**Исправление:**

```scala
manufacturer: Option[String]
manufacturerUrl: Option[UriRef]
serialNumber: Option[String]
```

Для `Patch`:

```scala
enum SpotType:
  case Emulated, Spot
```

---

### HI-04 — Неполные нормативные closed enumerations

**Источники:** `AUDIT-A` ADT-004, ADT-005; `AUDIT-C` ADT-005.

Необходимо добавить/проверить:

| Enum | Missing / normative value |
|---|---|
| `Scope` | `Device` |
| `ISOPaperSubstrate` | `PS9` |
| `MediaType` | `Synthetic` |
| `Sides` | `Unprinted` |
| JDF/XJDF versions | `1.0`, `1.8`, если это требуется нормативной version table |

**Влияние:**

- Стандартные 2.2 значения невозможно сконструировать.
- Закрытые union'ы неполны, что нарушает conformance.

**Исправление:**

- Добавить недостающие cases.
- Deprecated значения, если они остаются representable в 2.2, не удалять.

---

### HI-05 — `NamedColor` представлен unconstrained `String`

**Источники:** `AUDIT-A` ADT-010; `AUDIT-C` ADT-010.

**Нормативное состояние:**

- `NamedColor` является ограниченным simple type с pattern-based vocabulary.
- Это не произвольный текст.

**Текущее состояние:**

- Во всех наблюдаемых местах `NamedColor` представлен как `String`.

**Влияние:**

- Любой string принимается как named color.
- Нарушается принцип: restricted simple type не должен быть bare `String`.

**Исправление:**

- Ввести opaque/validated `NamedColor`.
- Либо сгенерировать lexical enum, если vocabulary закрыт.
- Заменить все standard `NamedColor` fields.

---

### HI-06 — `ResourceSet/@CombinedProcessIndex` использует `Vector[Float]` вместо нормативного `IntegerList`

**Источники:** `AUDIT-A` ADT-009; `AUDIT-C` ADT-007.

**Норматив:**

- Table 3.12 задаёт `IntegerList`.
- Примеры используют целые индексы `0`, `1`.

**Текущее состояние:**

- `combinedProcessIndex: Vector[Float]`.

**Влияние:**

- Дробные process indices representable.
- Модель следует corrupt/stale XSD вместо нормативной таблицы.

**Исправление:**

```scala
combinedProcessIndex: Vector[Int]
```

---

### HI-07 — `Part` стирает fixed-length `IntegerRange` до произвольного `String`

**Источники:** `AUDIT-C` ADT-009.

**Норматив:**

- Table 6.4 и Appendix A.1.
- `DocIndex`, `PageNumber`, `RunIndex`, `SetIndex`, `SheetIndex` являются two-integer `IntegerRange` values.

**Текущее состояние:**

- Используется nonblank arbitrary string-like `RangeExpression`.

**Влияние:**

- Значения вида `"anything"` representable.
- Теряется fixed-length integer semantics.

**Исправление:**

- Использовать существующий `IntegerRange(first, last)` для всех пяти полей.
- При необходимости добавить ordering validation.

---

### HI-08 — Нормативные list/color invariants не защищены

**Источники:** `AUDIT-C` ADT-011.

Проблемные области:

| Тип / поле | Нормативное требование |
|---|---|
| `Glue.gluingPattern` | even-length list |
| `TransferFunction` | even x/y pairs |
| `CMYKColor` | компоненты в допустимых пределах |
| `sRGBColor` | компоненты в допустимых пределах |
| `LabColor` | lightness в пределах 0..100 |

**Текущее состояние:**

- Используются обычные `Vector[Float]` / product types без smart constructors.
- Пустые, odd-length, out-of-domain значения representable.

**Исправление:**

- Ввести refined types:
    - `EvenPairs`
    - `NonEmptyVector[XYPair]`
    - validated color products
- Добавить boundary/parity tests.

---

### HI-09 — Внутренние binding choices остаются несовместимыми

**Источники:** `AUDIT-C` ADT-012.

**Норматив:**

- Tables 4.8–4.17 связывают detail children с `BindingType`.
- Запрещены несовместимые nested Coil/Comb/Ring combinations.

**Текущее состояние:**

- Верхнеуровневый `BindingSpecification` полезен.
- Но `LooseBindingDetails` может содержать три независимых optional variants одновременно.
- Возможна ситуация, когда `CombBindingDetails` находится под Coil binding.

**Исправление:**

- Каждый binding case должен нести только совместимый detail ADT.
- Либо добавить typed validator с domain errors.

---

### HI-10 — Системное отсутствие validation layer для cross-field SHALL constraints

**Источники:** `AUDIT-A` ADT-017, ADT-012, ADT-013; `AUDIT-C` ADT-013.

Примеры нормативных ограничений, которые не защищены:

| Ограничение | Состояние |
|---|---|
| `Resource/@Orientation` xor `Resource/@Transformation` | Representable both |
| `Resource/@Start` xor `Resource/@StartOffset` | Representable both |
| `RelatedJobPartID` требует `RelatedJobID` | Не проверяется |
| `QueryResource` payload vs `Subscription` filters | Не проверяется |
| `QueryStatus` subscription/filter exclusions | Не проверяется |
| `ModifyQueueEntry` move targets at most one | Не проверяется |
| `MediaIntent.BackISO` требует ISO | Не проверяется |
| Product amount/min/max contradictions | Не проверяются |

**Влияние:**

- Многие документно-недопустимые значения компилируются.
- Нет API, который мог бы их отклонить.

**Исправление:**

- Там, где возможно, заменить independent `Option` fields на coproducts.
- Для остальных ввести compositional validation API:
  ```scala
  def validate: Vector[ValidationError]
  ```
- Расширить `ValidationError` за пределы `EmptyValue`, `EmptyCollection`, `InvalidValue`.

---

### HI-11 — 0..100 priority хранятся как bare `Int`

**Источники:** `AUDIT-A` ADT-011; `AUDIT-C` ADT-014.

Проблемные места:

- `Disposition.priority: Option[Int]`.
- `NodeInfo.jobPriority: Option[Int]`.

**Норматив:**

- Значения между 0 и 100.

**Текущее состояние:**

- `QueuePriority` уже существует как opaque 0..100 для `QueueEntry` / `QueueFilter`.
- Но `Disposition` и `NodeInfo` используют bare `Int`.

**Влияние:**

- `Disposition(priority = Some(999))` representable.
- `NodeInfo(jobPriority = Some(-1))` representable.

**Исправление:**

- Ввести общий `Priority0To100` или переиспользовать `QueuePriority`.

---

### HI-12 — `dateTime` / `duration` smart constructors не реализуют полный lexical/value space

**Источники:** `AUDIT-C` ADT-015.

**Проблема:**

- Regex-based validation принимает невозможные даты/время.
- `dateTime` может допускать отсутствие zone там, где норматив требует явную зону.
- `duration` принимает некорректные формы вроде `PT` или `P1YT`.

**Влияние:**

- Невалидные temporal values проходят smart construction.

**Исправление:**

- Использовать проверенный XSD datatype parser.
- Либо реализовать полную лексическую и value validation.

---

### HI-13 — `ColorMeasurementConditions/@Illumination` следует corrupt XSD `float` вместо нормативного `NMTOKEN`

**Источники:** `AUDIT-C` ADT-016.

**Норматив:**

- Table 8.13: `Illumination` является `NMTOKEN`, например `D50`, `D65`, `Unknown`.

**Текущее состояние:**

- `illumination: Option[Float]`.

**Влияние:**

- Нормативные token values невозможны.
- Модель следует corrupt XSD type.

**Исправление:**

```scala
illumination: Option[Nmtoken]
```

---

### HI-14 — Чистая сборка `sbt "clean ; compile ; test"` не выполнена

**Источники:** `AUDIT-A` §7; `AUDIT-B` ADT-001; `AUDIT-C` ADT-022.

**Факт:**

- `java` недоступен.
- `sbt` недоступен.
- Network/apt недоступны.
- Команда:
  ```bash
  sbt "clean ; compile ; test"
  ```
  не выполнена.

**Влияние:**

- Не подтверждено:
    - compilation;
    - `-Werror`;
    - `-Wunused:all`;
    - `-Yexplicit-nulls`;
    - exhaustivity;
    - test execution.

**Исправление:**

- Выполнить в окружении с JDK 17+ и sbt 2.x:
  ```bash
  sbt "clean ; compile ; test"
  ```
- Не ослаблять strict flags.

---

## 6.3. Medium findings

### MD-01 — `Condition/@PartContext` представлен singular `Nmtoken`, а норматив требует `NMTOKENS`

**Источники:** `AUDIT-A` ADT-014, ADT-020.

**Проблема:**

- `LayoutCondition.partContext: Option[Nmtoken]`.
- `CellConditionTerm.partContext: Option[Nmtoken]`.
- Норматив: `NMTOKENS`, то есть список.
- Также `Condition` представлен двумя клонами: `LayoutCondition` и `CellConditionTerm`.

**Влияние:**

- Multi-key context reset, например `PartContext="DocIndex SetIndex"`, невозможно представить.

**Исправление:**

```scala
partContext: Vector[Nmtoken]
```

- Ввести общий `Condition` ADT, если оба клона изоморфны.

---

### MD-02 — Query-family `@Languages` не унифицированно представим

**Источники:** `AUDIT-A` ADT-015; частично `AUDIT-C`.

**Норматив:**

- Table 7.4 задаёт `Query/@Languages?` как family contract.
- Appendix H отмечает добавление на ряде Query types.

**Текущее состояние:**

- На части Query поле доступно.
- На части Query оно hard-wired к `Vector.empty` или скрыто.

**Влияние:**

- 2.2 `QueryGangStatus` / `QueryQueueStatus` не могут запросить localized comments, если следовать family contract.

**Исправление:**

- Либо все Query expose `languages: Vector[LanguageTag]`.
- Либо явно задокументировать, что конкретные таблицы без `Languages` выбраны как source of truth, и убрать требование из trait.

---

### MD-03 — ID и IDREF смешаны, reference integrity не проверяется

**Источники:** `AUDIT-C` ADT-018.

**Проблема:**

- `XsdId` используется и для declaration, и для references.
- Нет document-level validation:
    - uniqueness;
    - existence;
    - target type correctness.

**Влияние:**

- Dangling references representable.
- Duplicate IDs representable.
- Wrong target types representable.

**Исправление:**

- Различать semantic ID и IDREF.
- Добавить whole-document reference validation.

---

### MD-04 — JSON exceptions представлены несогласованно

**Источники:** `AUDIT-A` ADT-018; `AUDIT-C` ADT-019.

Проблемы:

- `$schema` моделируется в domain roots.
- JSON-only `@Name` для `XJDF` / `XJMF` отсутствует.
- JSON XJMF exactly-one message не валидируется.
- Audit JSON `Name` может быть derivable, но это не покрыто codec.
- `MediaLayers` JSON form невозможна из-за CR-03.

**Влияние:**

- Нет последовательной transport-specific policy.
- Нет доказательств valid XML/JSON round trip.

**Исправление:**

- Хранить domain transport-independent.
- JSON-only требования вынести в codec.
- Добавить codec tests.

---

### MD-05 — Нормативные ограничения XJDF `string` стерты

**Источники:** `AUDIT-C` ADT-020.

**Норматив:**

- Appendix A.1 определяет XJDF `string` как normalized и ограниченный по длине, например до 1023 символов.

**Текущее состояние:**

- Многие поля используют arbitrary `String`.

**Влияние:**

- Контрольные символы и чрезмерная длина representable.
- Simple-type coverage нельзя считать полным только из-за наличия `String`.

**Исправнение:**

- Ввести `XjdfString` opaque validated type там, где норматив требует `string`.
- Отдельно сохранить true open text, если он нужен.

---

### MD-06 — `ExtensionElement` не lossless для mixed XML content

**Источники:** `AUDIT-C` ADT-021.

**Проблема:**

- Foreign extension content может быть mixed/ordered XML.
- Текущая структура не сохраняет `text, child, text`.
- Нет представления comments/PI, если они нужны для lossless fallback.

**Влияние:**

- Lossless extension fallback невозможен для общего XML.

**Исправление:**

- Ввести ordered content ADT:
  ```scala
  enum ExtensionContent:
    case Text(value: String)
    case Element(node: ExtensionElement)
    ...
  ```

---

### MD-07 — Generic fallbacks и wildcards не namespace-safe

**Источники:** `AUDIT-C` ADT-017.

**Проблемы:**

- `NamedSpecificResource`, `NamedProductIntent`, `QueryMessage`, `CommandMessage`, `SignalMessage`, `ResponseMessage` могут принять standard XJDF QName.
- `Extensions` может переоценивать wildcard capabilities.
- Узлы только с `anyAttribute` могут получать foreign child elements через общий `Extensions`.

**Влияние:**

- Стандартные элементы могут быть скрыты за generic nodes.
- Wildcard constraints over-approximated.

**Исправление:**

- Ввести namespace-refined `ForeignQName`.
- Запретить standard namespace/local names в extension fallbacks.
- Разделить attribute-only и element-capable extension types.

---

### MD-08 — Существующие тесты не являются зарегистрированным test suite

**Источники:** `AUDIT-A` ADT-022; `AUDIT-C` ADT-023.

**Проблема:**

- Тестовые файлы содержат `object ...Checks` с eager `assert(...)`.
- Нет test framework dependency.
- Нет `@main` или discovered suite.
- Даже при успешном `sbt test` эти объекты могут не выполниться автоматически.

**Влияние:**

- Существующие assertions не являются доказательством runtime invariant coverage.

**Исправление:**

- Добавить munit/scalatest/weaver или другой test framework.
- Включить negative compile/runtime tests.
- Покрыть smart constructor failures.

---

### MD-09 — Coverage-документы переоценивают полноту

**Источники:** `AUDIT-A` ADT-021; `AUDIT-C` ADT-024.

**Проблема:**

- `docs/resource-coverage.md` утверждает 102/102.
- `docs/message-coverage.md` утверждает 44/44.
- Это верно только как подсчёт имён.
- Это не означает field-level или semantic completeness.

**Исправление:**

Разделить coverage на:

- entity-name coverage;
- field coverage;
- simple-type coverage;
- semantic validation coverage;
- codec coverage.

---

### MD-10 — Отсутствует XML/JSON codec; aliased top-level resources требуют explicit `elementName`

**Источники:** `AUDIT-B` ADT-008; связано с `AUDIT-C` ADT-019.

**Проблема:**

- В дереве нет реального XML/JSON codec.
- `build.sbt` не содержит codec dependencies.
- Некоторые aliased top-level ресурсы могут не иметь explicit normative `elementName`.

**Влияние:**

- Wire-format round-trip conformance невозможно оценить.
- Будущий codec может emitir неверные element names для aliases.

**Исправление:**

- Либо явно зафиксировать, что проект является pure ADT без codec.
- Либо добавить codec и гарантировать canonical XSD local names.

---

## 6.4. Low findings

### LO-01 — `LayoutIntent/@NumberUp` представлен integer grid вместо `XYPair`

**Источники:** `AUDIT-A` ADT-016.

- XSD использует `XYPair`.
- Scala использует integer `GridSize`.
- Это может быть допустимым сужением, но должно быть явно задокументировано.

---

### LO-02 — `RunList/@Docs` представлен `IntegerRange`, хотя XSD type — `IntegerList`

**Источники:** `AUDIT-A` ADT-019.

- `Docs` может быть длиннее двух integer values.
- Требуется либо использовать `Vector[Int]`, либо задокументировать ограничение.

---

### LO-03 — `AddressLine` схлопнут в `Vector[String]`

**Источники:** `AUDIT-B` ADT-002.

- `AddressLine` является real global element.
- Его foreign attributes могут теряться.
- Допустимо как упрощение, но снижает round-trip fidelity.

---

### LO-04 — `OrganizationalUnit` не типизирован как нормативный sub-element

**Источники:** `AUDIT-B` ADT-004.

- `Company/OrganizationalUnit` может иметь nested structure.
- Текущая модель хранит `Vector[String]`.

---

### LO-05 — `xsdq hierarchy` недооценивает `SpecificResource`

**Источники:** `AUDIT-A`, `AUDIT-B`, `AUDIT-C`.

- `hierarchy` возвращает 100.
- Авторитетный substitution set — 101.
- Это tool/index discrepancy, не дефект модели.

---

### LO-06 — XSD float products widened to `Double` без явной политики

**Источники:** `AUDIT-C` ADT-025.

- Нужно задокументировать intentional widening.
- Либо использовать Float-compatible refined values.
- Особенно важно для `INF`, `-INF`, `NaN` lexical policy.

---

### LO-07 — Strict equality не включён, `CanEqual` используется непоследовательно, intersection alias декоративен

**Источники:** `AUDIT-C` ADT-026.

- `-language:strictEquality` не включён.
- Не все ADT derive `CanEqual`.
- `OpenXjdfNode = XjdfNode & Extensible` может быть неиспользуемым.

---

### LO-08 — `FoldCatalog` pattern ослаблен до `Nmtoken`

**Источники:** `AUDIT-C` simple-type gap; `AUDIT-A` упоминает pattern/open-ish handling.

- `FoldCatalog` имеет pattern-constrained vocabulary.
- Представление как `Nmtoken` теряет pattern semantics.
- Severity low/medium, но для strict simple-type coverage следует уточнить.

---

### LO-09 — Требуется единый реестр documented XSD/normative choices

Некоторые расхождения не являются дефектами, но должны быть явно задокументированы:

- `Version` добавляет `2.2` сверх XSD.
- `QueryKnownMessages` может не содержать `Subscription`, если следовать конкретной таблице.
- `CommandShutDown` params optional, несмотря на XSD minOccurs=1.
- `XJMF` messages используют `NonEmptyVector` для XML `1..*`, но JSON exactly-one требует codec-level validation.
- `FileSpec/@NPage` моделируется как `numberOfPages`, хотя XSD может не содержать.

---

## 7. Консолидированная таблица XSD / нормативных расхождений

Приоритет: **normative > XSD > Scala > docs**.

| Тема | Норматив | XSD/index | Текущая Scala | Консолидированная оценка |
|---|---|---|---|---|
| `Version` | включает `2.2` | только `2.0`, `2.1` | включает `2.2` | Correct |
| `Resource/@Brand` | `string` | `boolean` | `String` | Correct |
| `Part/@BlockName` | `NMTOKEN` | corrupt/XYPair | `Nmtoken` | Correct |
| `PartWaste/@ModuleIDs` | `NMTOKENS` | corrupt/float | `NonEmptyVector[Nmtoken]` | Correct |
| `AssemblyItem/@ChildRef` | `IDREF` | corrupt/float | `XsdId` | Correct |
| `ResourceSet/@CombinedProcessIndex` | `IntegerList` | `FloatList` | `Vector[Float]` | Wrong |
| `Condition/@PartContext` | `NMTOKENS` | singular `NMTOKEN` | `Option[Nmtoken]` | Wrong |
| `ColorMeasurementConditions/@Illumination` | `NMTOKEN` | corrupt/float | `Float` | Wrong |
| `GeneralID/@DataType` | Table A.14 | stale lexical forms | neither correct | Wrong |
| `ChannelMode` | `FireAndForget`, `Reliable` | same enum base | wrong values + wrong cardinality | Wrong |
| `Scope` | includes `Device` | stale 2.1 set | stale set | Wrong |
| `ISOPaperSubstrate` | includes `PS9` | stale | missing | Wrong |
| `Tool` 2.2 attributes | present | absent | absent | Wrong |
| `Patch/@SpotType` | present | absent | absent | Wrong |
| `ResourceQuParams/@Types` | present | absent | absent | Wrong |
| `SignalResource` replacement attrs | present | absent | absent | Wrong |
| `SubscriptionInfo/@Languages` | present | absent | absent | Wrong |
| `FileSpec/@NPage` | new in 2.2 | absent | present | Correct |
| `Device/@RestApiBaseURL` | new in 2.2 | absent | present | Correct |
| `ShapeDef/RuleLength`, multiple `FileSpec` | 2.2 changes | stale | present | Correct |
| `SheetOptimizingReport` | normative-only | absent | present | Correct |
| `RasterReadingParams` | XSD substitution member | hierarchy omits | present | Correct |
| `BoxFoldingParams` | 2.2 action/Glue structure | stale | stale/invalid | Wrong |
| `MediaLayers` | ordered mixed children | stale/incomplete | fixed pair | Wrong |

---

## 8. Сводка invalid states

### 8.1. Уже защищены типами

| Инвариант | Защита |
|---|---|
| Empty `1..unbounded` | `NonEmptyVector` |
| `minLength=2` для части списков | `TwoOrMore` |
| `FileSpec` location XOR | `FileLocation` coproduct |
| `Disposition` MinDuration vs Until | `DispositionTime` coproduct |
| Duplicate Front/Back `SurfaceColor` | `ColorSurfaces` |
| Верхнеуровневый `BindingType` vs incompatible details | `BindingSpecification` |
| `PartWaste` at-least-one origin | `WasteOrigin` |
| Even `LayoutIntent/@Pages` | `EvenPageCount` |
| Queue priority 0..100 | `QueuePriority` |

### 8.2. Всё ещё representable

| Инвариант | Проблема |
|---|---|
| `ChannelMode` values | wrong enum |
| `Subscription/@ChannelMode` list | collapsed to optional single value |
| `GeneralID/@DataType` values | wrong enum |
| `Scope="Device"` | missing case |
| `ISOPaperSubstrate="PS9"` | missing case |
| `NamedColor` arbitrary string | no validation |
| `Resource/@Orientation` + `@Transformation` | both representable |
| `Resource/@Start` + `@StartOffset` | both representable |
| `Disposition/@Priority` outside 0..100 | bare `Int` |
| `NodeInfo/@JobPriority` outside 0..100 | bare `Int` |
| Odd `TransferFunction` / `GluingPattern` | no parity validation |
| Invalid date/time/duration | incomplete smart constructors |
| `Part` arbitrary range strings | `IntegerRange` erased |
| `MediaLayers` arbitrary order | fixed pair, wrong |
| `BoxFoldingParams` normative sequences | required obsolete shape |
| IDREF dangling | no graph validation |
| Standard QName in generic fallback | no namespace safety |

---

## 9. Состояние сборки и тестов

Консолидированный факт:

```bash
which java
which sbt
sbt "clean ; compile ; test"
```

Результат во всех аудитах:

- `java: command not found`
- `sbt: command not found`
- build не выполнен
- тесты не запущены

Дополнительно:

- `build.sbt` declares Scala 3.8.4.
- Указаны строгие флаги:
    - `-Werror`
    - `-Wunused:all`
    - `-Yexplicit-nulls`
- Эти флаги не были практически проверены.
- Тестовые файлы не являются discovered test suites.

Следствие:

> Нельзя утверждать, что проект проходит clean compile/test.  
> Отсутствие build evidence является самостоятельным блокером для статуса «Complete and conformant».

---

## 10. Рекомендуемый порядок исправлений

### Фаза 1: критические словари и cardinality

1. Исправить `ChannelMode`:
    - values: `FireAndForget`, `Reliable`;
    - `Subscription.channelMode: Vector[ChannelMode]`;
    - `Signal.channelMode: Option[ChannelMode]`.

2. Исправить `GeneralId.DataType`:
    - строго Table A.14;
    - добавить `NamedFeature`;
    - удалить незаконные `Name`, `Uri`.

3. Добавить недостающие enum cases:
    - `Scope.Device`;
    - `IsoPaperSubstrate.PS9`;
    - `MediaType.Synthetic`, если подтверждено;
    - `Sides.Unprinted`, если подтверждено;
    - JDF/XJDF versions по нормативной таблице.

---

### Фаза 2: отсутствующие нормативные поля 2.2

4. Добавить:
   ```scala
   ResourceQuParams.types: Vector[Nmtoken]
   SignalResource.replaceAfter: Option[XsdDateTime]
   SignalResource.replaceBefore: Option[XsdDateTime]
   SubscriptionInfo.languages: Vector[LanguageTag]
   SubscriptionInfo.channelId: Nmtoken
   Tool.manufacturer: Option[String]
   Tool.manufacturerUrl: Option[UriRef]
   Tool.serialNumber: Option[String]
   Patch.spotType: Option[SpotType]
   ```

---

### Фаза 3: критические структурные ресурсы

5. Переделать `BoxFoldingParams`:
    - ordered `BoxFoldAction*`;
    - action `Glue`;
    - child `Glue`;
    - optional deprecated top-level `Glue`.

6. Переделать `MediaLayers`:
    - ordered `Vector[MediaLayer]`;
    - support `Glue | Media` mixed sequence;
    - codec должен обработать JSON `Name` exception.

---

### Фаза 4: scalar/list/simple-type corrections

7. Исправить:
    - `ResourceSet/@CombinedProcessIndex`: `Vector[Int]`;
    - `Part`: использовать `IntegerRange`;
    - `Condition/@PartContext`: `Vector[Nmtoken]`;
    - `ColorMeasurementConditions/@Illumination`: `Nmtoken`;
    - `NamedColor`: opaque/validated type или enum;
    - `FoldCatalog`: восстановить pattern semantics или явно задокументировать open-ish policy.

8. Ввести validated list/color types:
    - even `GluingPattern`;
    - even-pair `TransferFunction`;
    - bounded CMYK/sRGB/Lab components.

---

### Фаза 5: validation layer

9. Добавить domain validation API:
   ```scala
   trait ValidatedXjdfNode:
     def validate: Vector[XjdfValidationError]
   ```

10. Покрыть минимум:
- `Orientation` xor `Transformation`;
- `Start` xor `StartOffset`;
- `RelatedJobPartID` requires `RelatedJobID`;
- Query/Subscription filter exclusions;
- ModifyQueueEntry move targets;
- Media BackISO requires ISO;
- Product amount contradictions;
- ID/IDREF uniqueness/existence/target correctness.

---

### Фаза 6: extensions и transport

11. Улучшить `Extensions`:
- namespace-safe foreign QName;
- запрет standard names в generic fallbacks;
- разделить attribute-only и element-capable wildcards;
- ordered mixed content для lossless XML extensions.

12. Определить codec policy:
- либо pure ADT без codec;
- либо добавить XML/JSON codec;
- проверить JSON exceptions:
    - `$schema`;
    - `@Name`;
    - XJMF exactly-one message;
    - audit names;
    - `MediaLayers` JSON form.

---

### Фаза 7: тесты и сборка

13. Перевести тесты на реальный framework.
14. Добавить negative tests для smart constructors.
15. Добавить tests для:
- list parity;
- priority bounds;
- color bounds;
- date/time/duration lexical validity;
- reference integrity;
- extension namespace rejection.

16. Выполнить:
   ```bash
   sbt "clean ; compile ; test"
   ```
в окружении с JDK 17+ и sbt 2.x, не ослабляя `-Werror`, `-Wunused:all`, `-Yexplicit-nulls`.

---

## 11. Обновление документации

Coverage documents следует переписать так, чтобы отдельно фиксировать:

1. **Entity-name coverage**
    - 102/102 resources.
    - 14/14 intents.
    - 44/44 messages.

2. **Field coverage**
    - какие attributes/children присутствуют;
    - какие 2.2 fields отсутствуют.

3. **Simple-type coverage**
    - closed enums;
    - pattern types;
    - list types;
    - bounded numeric types.

4. **Semantic validation coverage**
    - какие SHALL constraints защищены типами;
    - какие требуют validator;
    - какие покрыты тестами.

5. **Transport coverage**
    - XML codec status;
    - JSON codec status;
    - JSON exceptions.

Текущие формулировки вида “102/102 typed” и “44/44 typed” следует считать верными только для уровня имён, но не для утверждения о полной 2.2 conformance.

---

## 12. Финальное заключение

Консолидированный результат трёх аудитов:

1. **Структурный каркас модели полный.**
    - Все основные standard entity names присутствуют.
    - Substitution groups собраны корректно.
    - Extension points оставлены открытыми правильно.

2. **Семантическая полнота не достигнута.**
    - Есть критически неверные closed vocabularies.
    - Отсутствуют нормативные 2.2 fields.
    - Есть неверные scalar/list types.
    - Есть незащищённые SHALL constraints.
    - Есть critical structural defects в `BoxFoldingParams` и `MediaLayers`.

3. **Доказательная база сборки отсутствует.**
    - `sbt "clean ; compile ; test"` не выполнен.
    - Тесты не являются зарегистрированным test suite.
    - Strict compiler flags не проверены.

Итоговая формулировка:

> **Модель является structurally complete skeleton, но не является complete or conformant XJDF/XJMF 2.2 ADT. Консолидированный вердикт: Incomplete.**