# Этап 05 — JSON-кодек и JSON-исключения

| Поле | Значение |
|---|---|
| Цель | JSON-кодек для XJDF/XJMF с реализацией нормативных JSON-исключений и законом кросс-консистентности XML ↔ JSON |
| Вход | Этап 04 (XML-кодек, фикстуры, round-trip-инфраструктура) |
| Выход | Модуль `codec-json` (circe): `Encoder`/`Decoder`, JSON-исключения, кросс-законы |
| Сложность | Средняя |
| Зависимости | 04 |

## Зачем это нужно

XJDF 2.2 определяет JSON-кодирование с рядом **исключений** — мест, где JSON-форма не
изоморфна XML-форме. Именно они чаще всего ломаются в реализациях, поэтому этап целиком
построен вокруг них.

MIME: `application/vnd.cip4-xjdf+json`, `application/vnd.cip4-xjmf+json` (Table 9.1).

## Предпосылки: что читать

- нормативный текст: `reference/xjdf/8 – Subelements.md` §8.28 (MediaLayers, Example 8.5 —
  единственный нормативный пример JSON MediaLayers) и `reference/xjdf/3 – Structure.md`
  (JSON-кодирование корней);
- `reference/cats/docs/datatypes/validated.md` — аккумулирующие декодеры (в circe это
  паттерн `Decoder` + `ValidatedNel` для сообщений «все ошибки сразу»);
- `reference/cats/docs/datatypes/kleisli.md` — перенос паттерна декодеров из этапа 04.

## Дизайн

### 1. Библиотека

circe (`io.circe %% circe-core`, `circe-parser`; `circe-generic` — если решено деривировать).
Правило из этапа 01 остаётся: **ручные инстансы для всего, что имеет семантику**;
деривация — только для внутренних вспомогательных типов. Модель намеренно не хранит
JSON-специфику, поэтому кодек пишется в `codec-json`, а не в `model`.

### 2. JSON-исключения (главный объём этапа)

**a) Корневые `$schema` и `@Name`.** JSON XJDF/XJMF содержит поля `$schema` и `@Name`,
которых нет в XML и в доменной модели. Кодек обязан:
при декодировании — прочитать и проверить (`@Name` должен соответствовать корню),
при кодировании — подставить (`$schema` — версия схемы, `@Name` — `XJDF`/`XJMF`).

**b) `XJMF` — ровно одно сообщение.** JSON-XJMF несёт единственный объект сообщения
(без XML-обёртки «списка»). Декодер: объект с ключом-именем сообщения;
энкодер: сериализует `NonEmptyVector` размера 1, иначе — ошибка кодека
(домен допускает несколько сообщений — это XML-форма; JSON-ограничение живёт здесь).

**c) `MediaLayers` — инлайнинг с `@Name`.** В JSON слои — плоские объекты с
`@Name: "Glue" | "Media"` вместо вложенных элементов (Example 8.5). Кодек: 
`MediaLayer.GlueLayer(g)` → объект `{...поля Glue..., "@Name": "Glue"}`; при декодировании
диспетчер по `@Name`. Круглый тест обязателен — именно здесь теряют порядок слоёв.

**d) Аудиты.** Имена аудитов в JSON — отдельные ключи; проверьте соответствие
`AuditName` и JSON-имён на фикстурах.

**e) Числа.** XJDF JSON пишет `1190.5511811` (Double). Зафиксируйте политику:
float-поля кодируются без артефактов представления (`80.0f` → `80`), `INF/-INF/NaN`
либо отклоняются, либо проходят по явной политике — решите и задокументируйте.

### 3. Кросс-кодековый закон

Ключевой закон этапа: XML и JSON кодируют **один и тот же домен**:

```scala
def xmlJsonAgree[A: XmlDecoder: JsonCodec: Eq](value: A): Boolean =
  val viaXml  = XmlDecoder[A].decode(XmlEncoder[A].encode(value))
  val viaJson = JsonDecoder[A].decode(JsonEncoder[A].encode(value))
  (viaXml, viaJson) match
    case (Right(x), Right(j)) => Eq[A].eqv(x, j)
    case _                    => false
```

Плюс направленный тест на нормативной паре (Example 8.5 содержит XML и JSON рядом —
сравниваем оба декода с одним и тем же ADT-значением).

## Нормативные правила маппинга (сверка с 1.4.2 и 9.10, зафиксировано)

Сверка реализации с разделами 1.4 «Encoding Methods» и 9.10 «Use of JSON and REST APIs» выявила и закрыла:

1. **`TransferFunction` → `array of array of float`** (Table 9.3): внешний массив — точки, внутренние — пары
   `[x, y]` длины 2. Плоский float-массив НЕ нормативен. Реализовано в `JsonScalars` (кодек точек),
   поля `Spectrum`/`BackSpectrum` переведены на него. `GluingPattern` остаётся плоским (`FloatList`).
2. **`Comment` → объект с ключом `"Text"`** (9.10.2.5 + Table 8.14): Comment — единственный элемент со
   смешанным контентом; тело маппится в `"Text"` (JSON Exception `@Text`), атрибуты — обычными членами.
3. **`AuditPool` → массив аудитов с `"Name"`-дискриминатором** (Example 9.11): каждый аудит — объект
   `{Header, payload, "Name": "AuditCreated" | ...}`. Реализованы все пять аудитов + `ProcessRun`.
4. **Foreign namespaces → префиксные ключи + `"@context"` (JSON-LD минимум)** (9.10.2.4, Example 9.12):
   XJDF-члены без префикса; foreign-элементы/атрибуты — `"Prefix:Name"` и контекст `{"@context": {"Prefix": "uri"}}`.
   Пока зафиксировано как требование батча extensions (в текущем срезе foreign отсутствует).
5. **Кардинальность элементов**: «1» → объект, «>1» → массив (9.10.2.1); значения по ТИПУ схемы, а не
   инстанса (один NMTOKEN в NMTOKENS — массив с одним элементом).
6. **Root `"Name"`** — для корневых объектов; для XJDF/XJMF как подэлементов — локальное имя, `@Name` опционален.

## Предостережения из этапа 04 (обязательны к прочтению перед стартом)

Опыт XML-кодека сформулирован как список ловушек, которые JSON-реализация должна учесть сразу:

1. **Не деривируйте «вслепую».** circe-generic на всей модели не сработает: opaque-типы, копродукты
   (`FileLocation`, `BindingSpecification`, `ColorSurfaces`…) и служебные поля (`extensions`,
   `foreignElements`) требуют ручных инстансов. Переиспользуйте то же разделение «ручное для особых форм /
   деривация для остального» и тот же реестр имён: каждая особая форма уже имеет один кодек-объект
   (например `BindingIntentCodec`) — добавляйте JSON-пару рядом с XML-парой, а не в отдельном месте.
2. **Единая политика имён.** Нормативные имена атрибутов/элементов уже централизованы (`derivation/Names`,
   `Registry`); JSON-ключи должны браться оттуда же. XSD-имена НЕ копировать: проверенная схема устарела
   (`@NPage` vs норматив `@NPage`/модель `NumberOfPages`, `MediaColorName`, `ChannelBinding` vs `Channel`),
   источник истины — нормативные таблицы и `docs/domain-model.md`.
3. **Порядок.** В XML он был строгим (`xs:sequence`) и property-тест поймал три нарушения. В JSON объекты
   неупорядочены — но **массивы упорядочены**: порядок слоёв `MediaLayers`, списков `ChannelMode`,
   `Types` сохранять при энкоде/декоде; JSON-исключения (`@Name` инлайнинг слоёв) менять форму, но не порядок.
4. **ID/IDREF и валидность.** XML-валидатор ловил dangling-ссылки. У JSON нет XSD-валидатора — аналог
   proof-а: **кросс-закон XML↔JSON** на одних и тех же сгенерированных значениях (уже в дизайне этапа) +
   доменный `ReferenceCheck` на декодированных документах. Генераторы из тестовых scope'ов
   `model`/`messaging` переиспользовать через `dependsOn(project % "test->test")` — и расширить их так,
   чтобы покрыть JSON-исключения (`$schema`/`@Name`, exactly-one-message, инлайнинг MediaLayers).
5. **Деривационные правила Scala 3** (из боёв этапа 04, если JSON-слой тоже будет деривировать):
   inline-given невидимы обычному поиску (inline-def + обычные given'ы на тип); nested-продукты — через
   канонический `deriveOrSummon` (`summonFrom` + `summonInline`); `-Xmax-inlines` для широких классов;
   `scala.Product` квалифицировать (затенение моделью `Product`); самодостаточно-рекурсивные типы
   (`AssemblySection`, `BundleItem`) — только ручные кодеки; top-level givens требуют `import ...given`.
6. **Строгие флаги.** `-Werror`/`-Wunused:all` ловят лишние импорты и мёртвый код — держать чистыми с
   первого коммита этапа, а не чинить в конце.
7. **Сравнение чисел.** Float/Double-поля в кросс-законе сравнивать через `Eq` с допуском (NaN/INF
   политика фиксирована в лексическом слое XML — JSON должен её повторить).

## Задачи (пошагово)

1. Модуль `codec-json`; circe; хелперы для `$schema`/`@Name` корней.
2. Скалярные кодеки: строки/токены/числа/даты (переиспользовать лексику из этапа 04).
3. `XJDF`/`XJMF` корни с JSON-исключениями (a)–(b); тесты на отсутствие/наличие `@Name`.
4. `MediaLayers` инлайнинг (c): декод и энкод Example 8.5, тест на порядок слоёв.
5. Кодеки ресурсов/сообщений (диспетчер по имени — общий с этапом 04 реестр);
   JSON-форматы вложенных элементов по нормативу.
6. Кросс-закон `xmlJsonAgree` на всех фикстурах этапа 04.
7. Политика чисел (e): тесты `80.0f → 80`, поведение `NaN/INF` задокументировано.
8. Негативные тесты: JSON без `@Name`, два сообщения в JSON-XJMF, кривой `@Name` у слоя.

## Definition of Done

- [ ] Example 8.5 (MediaLayers) декодируется из XML и JSON в одно и то же ADT-значение.
- [ ] `$schema`/`@Name` корней корректны в обе стороны; отсутствие в домене подтверждено тестом.
- [ ] JSON-XJMF ровно-одно-сообщение: энкодер отклоняет `size != 1` с внятной ошибкой.
- [ ] Кросс-закон зелёный на всех фикстурах; порядок слоёв MediaLayers проверяется явно.
- [ ] Негативные тесты из п. 8 зелёные.
- [ ] `sbt "clean ; compile ; test"` зелёный.

## Риски и альтернативы

- **Соблазн «просто задеривировать» circe-generic.** Модель велика и содержит opaque-типы
  и union'ы — generic-деривация даст неверные формы (например, плоские `Option` там, где
  норматив требует пропуска полей). Ручные инстансы + реестр имён.
- **Два формата — две расходящиеся реализации.** Держите общий реестр имён и общие
  тестовые фикстуры (один файл fixtures, используемый обоими кодек-модулями).
- **Числовая точность в кросс-законе.** Используйте `Eq` с допуском; сравнивайте не
  сериализованные строки, а доменные значения.

---

## Состояние исполнения (первый заход)

Реализовано в модуле `codec-json` (circe-core; parser — только в тестах):

- **Нормативный срез ручными кодеками** (по предостережениям из этапа 04 — деривация отложена до батча полного
  покрытия): скаляры (все opaque-типы через smart-конструкторы, списки-в-массивы `XYPair`/`IntegerRange`/цвета/
  матрицы, plain-энамы generic-given + lexical-энамы `Version`/`JdfVersion`/`MessageUrlScheme`/`NamedColor`),
  дерево XJDF (`ResourceSet`/`Resource`/`Part`/`AmountPool`/`Comment`/`GeneralID`), ресурсы `Media` (с рекурсией
  `MediaLayers`), `Color`, `Component`, `Tool`, `Device`, `RunList`, `RegisterMark`, и messaging-набор
  (`Header`, `Subscription`, `ResourceQuParams`, `ResourceInfo`, `DeviceInfo`, `Notification`,
  `QueryKnownMessages`, `QueryResource`, `ResponseKnownMessages`, `ResponseResource`, `SignalNotification`,
  `SignalResource`, `SignalStatus`, `XJMF`).
- **JSON-исключения реализованы:** `"Name"`-член корней (имена атрибутов в JSON — без `@`; `$schema` опционален
  через `JsonCodec.withSchema`); XJMF ровно-одно-сообщение (энкодер бросает на `size != 1`, декодер требует
  ровно один message-член); `MediaLayers` инлайнинг — массив слоёв с `"Name": "Media" | "Glue"`.
- **Кросс-закон** (аналог XSD-proof для JSON): по 100 детерминированных XJDF и XJMF документов обе ветки —
  XML-кодек и JSON-кодек — декодируются в одно и то же доменное значение; генераторы переиспользованы из
  тестовых scope'ов `model`/`messaging` через `test->test`, XML-кодеки — через `codecXml % "test->compile"`.
- **Нормативные JSON-фикстуры:** Example 3.1 (корень), Example 8.5 (MediaLayers JSON = XML-декод того же ADT),
  Example 7.1 (XJMF + SignalNotification), Example 9.11 (AuditPool — JSON-декод + XML/JSON-кросс-проверка).
- **Аудиты (Example 9.11):** `AuditPool` — массив `{Header, payload, "Name"}`; все пять аудитов + `ProcessRun`;
  член `AuditPool` в кодеке корня XJDF. XJDF-кодеки корня вынесены в `JsonRootCodecs.scala`: компилятор
  поймал цикл givens (`E046` через export-форвардеры фасада: корень требует `Encoder[AuditPool]`, аудиты —
  `Encoder[Part]`/`Header`) — граф givens, как и граф файлов, должен оставаться ацикличным.

## Состояние исполнения (батч деривации — в работе)

- **Деривация JSON-кодеков** (`JsonDerived.scala`, правила этапа 04): `JsonFieldCodec` (скаляры — low-priority
  given поверх любой пары `Encoder`/`Decoder`, включая generic-энамы; контейнеры `Option`/`Vector`/
  `NonEmptyVector`/`TwoOrMore`/`AtMostTwo`; узлы — inline `deriveOrSummon` через `productFieldCodec`),
  inline `JsonDerived.derivedEncoder/derivedDecoder` (обход полей через `summonInline`, дефолты —
  переиспользованный `Defaults` из XML-деривации), рантайм-кодеки: имена членов = `Names.attributeName`
  для скалярных полей и element-name класса для узловых; отсутствующие опциональные члены падают в
  дефолты; неизвестные стандартные члены отклоняются (как в XML).
- **Генерируемые файлы** (`tools/gen-json-codecs.py`): `JsonDerivedInstances.scala` — по два не-inline given'а
  (`Encoder`/`Decoder`) на каждый деривируемый case class (277 типов, топологический порядок);
  `JsonRegistry.scala` — таблицы диспетчеризации 99 ресурсов / 11 интенций / 42 сообщений. Перегенерация —
  скриптом при росте модели; замыкание «класс содержит спец-класс» вычисляется автоматически.
  **Урок (и подтверждение правила 04f):** самодостаточно-рекурсивные типы нельзя деривировать даже через
  per-type givens — given невидим в собственном инициализаторе, и inline-fallback пере-деривирует тип
  бесконечно (компилятор: «Infinite loop in function body», под `-Werror` — ошибка). `BundleItem`/
  `AssemblySection` — только ручные кодеки, и рекурсия в них идёт ЯВНО через helper-объект
  (`BundleItemJson`/`AssemblySectionJson`), как в XML, а не через implicit-поиск (самоссылка given'а в
  собственном теле не находится).
  **Урок 2 (энамы — Product'ы):** `scala.reflect.Enum extends Any, Product, Serializable`
  (reference/enums.md), поэтому generic `productFieldCodec[A <: scala.Product]` перехватывает и plain-энамы,
  назначая им element-name вместо attribute-name (`status: NodeStatus` → член «NodeStatus» вместо «Status» —
  поймано нормативной фикстурой 9.11). Различение — по наличию `Mirror.ProductOf` (у энамов только
  `Mirror.SumOf`); value-типы-кейсклассы (`XYPair`, `Matrix`, `Rectangle`, `IntegerRange`, `Shape3D`,
  `GridSize`, `TileCoordinate`) защищены точными given'ами `JsonFieldCodec`, как в XML-`FieldCodec`.
- **Ручные кодеки спец-форм** (`JsonSpecialCodecs.scala`, зеркалят XML-маппинг): `FileSpec` (плоские члены
  `URL`/`UID`/`FileFormat`+`FileTemplate`), `Disposition` (`MinDuration`/`Until`), `NetworkHeader` (`"Text"`),
  `TiffTag` (`BinaryValue`/`IntegerValue`/`NumberValue`/`StringValue`), `PlacedObject` (`MarkObject`/
  `ContentObject`), FileSpec-роли (`DeliveryFiles`, `DeviceSchemas`, `DeviceInfoSchemas`, `VerificationFiles`,
  `QualityControlFiles`), JSON-исключения `Address`/`Company` (`"AddressLine"`/`"OrganizationalUnit"` —
  массивы строк, 9.10.2.1).
- **Foreign JSON** (`JsonForeign.scala`, 9.10.2.4 + Example 9.12): члены `"Prefix:Name"` + `"@context"`
  (JSON-LD минимум); синтез префиксов `ns1…` для беспрефиксных пространств; декод по `"@context"`,
  неотмапленный префикс → сам префикс как namespace (round-trip без потерь). `extensions`/`foreignElements`
  обрабатываются рантайм-кодеками по имени поля.
- **Выравнивание по Table 9.3**: `TileCoordinate` → строка (как `<all other types>`), `GridSize` → строка,
  `GluingPattern` → массив float, `Vector[Byte]` → hex-строка, кардинальные контейнеры → массивы.
- **Переиспользование XML-слоя**: `codecXml` подключён в compile-scope (`Names`, `Defaults`, `Lexical`,
  `CodecHelpers`); naming-политика проверена скриптом против 116 `XjdfNames.element`-имён модели — 0 расхождений.
- **Осталось:** ручные кодеки оставшихся спец-форм (payload-энамы: `BindingIntent`, `ColorIntent`, `StickOn`,
  `CollatingItem`, `LooseBindingParams`, `Assembly`, `ModifyQueueEntryParams`, `QueueSubmissionParams` —
  из-за них отложены ресурсы `Assembly`/`LooseBindingParams`/`FeedingParams`, интенты
  `BindingIntent`/`ColorIntent`/`AssemblingIntent`, сообщения `CommandModifyQueueEntry`/`CommandSubmitQueueEntry`);
  `Dependent`/`ProductList` и интенты в корне XJDF; foreign-ресурсы в `Resource`-членах; ReferenceCheck
  для JSON (предостережение #4).
