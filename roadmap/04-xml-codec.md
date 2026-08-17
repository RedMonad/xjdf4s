# Этап 04 — XML-кодек

| Поле | Значение |
|---|---|
| Цель | Читать и писать XJDF/XJMF 2.2 XML: парсер → ADT → эмиттер, с round-trip-законом и документной проверкой ID/IDREF |
| Вход | Этапы 01–02 (cats, `Eq`, валидация) |
| Выход | Модуль `codec-xml`: типы `XmlDecoder`/`XmlEncoder`, парсер, эмиттер, документный проход ссылок |
| Сложность | Высокая |
| Зависимости | 01, 02 |

## Зачем это нужно

ADT без кодека — это только половина продукта: реальные XJDF/XJMF документы живут в XML
(MIME `application/vnd.cip4-xjdf+xml` / `application/vnd.cip4-xjmf+xml`, Table 9.1).
Кодек — первый потребитель `Eq` (round-trip-закон) и валидации (отказ от невалидных
документов), а его API станет типовой базой для JSON (этап 05).

## Предпосылки: что читать

- `reference/cats/docs/datatypes/kleisli.md` — композиция «функций с эффектом»: декодер поля
  как `Kleisli[F, Context, A]` — главный приём этапа;
- `reference/cats/docs/datatypes/either.md`, `datatypes/validated.md` — fail-fast (`Either`)
  против аккумуляции (`Validated`) при разборе;
- `reference/cats/docs/datatypes/state.md` — проход по документу с реестром ID;
- cats-parse (документация вне репозитория — typelevel.org/cats-parse) — парсер-комбинаторы
  в экосистеме cats: чистый, без `IO`, что важно для тестируемости.

## Дизайн

### 1. XML-модель (транспортный слой, не домен)

Домен не знает про XML; кодек работает с промежуточным представлением:

```scala
enum Xml:
  case Element(
      name: QualifiedName,
      attributes: Vector[(QualifiedName, String)],
      children: Vector[Xml],
  )
  case Text(value: String)
end Xml
```

Разбор `String => Either[XmlError, Xml]` — на cats-parse; ошибка содержит позицию (строка/колонка).

### 2. Кодеки как type class + Kleisli

```scala
trait XmlDecoder[A]:
  def decode(element: Xml.Element): Either[XmlError, A]

trait XmlEncoder[A]:
  def encode(value: A): Xml.Element // документы — всегда элементы; текст — внутренняя деталь AST
```

Декодеры полей — `Kleisli[Either[XmlError, *], Xml.Element, A]`: их можно компоновать
(`andThen`, `product`) и поднимать в аккумулирующий режим через `ValidatedNel`:

```scala
val attr: String => Kleisli[Either[XmlError, *], Xml.Element, Option[String]] =
  name => Kleisli(el => Right(el.attributes.collectFirst { case (q, v) if q.localName == name => v }))
```

Для декодирования «всех полей сразу» используйте `mapN`-комбинацию `Validated` с
`NonEmptyList[XmlError]` (cats `ValidatedNel`), чтобы пользователь получал список всех проблем разом,
а не первую встречную.

### 3. Диспетчер имён для открытых точек

`SpecificResource` и `Message` — открытые union'ы: декодер выбирается по `elementName`.
Стройте реестр `Map[QualifiedName, XmlDecoder[...]]` (публичный `QualifiedName` и
`XjdfNamespace.uri` из `core` — `XjdfNames` в model приватный, в кодеке имена строятся явно):

```scala
val resourceDecoders: Map[QualifiedName, XmlDecoder[SpecificResource]] = Map(
  QualifiedName(XjdfNamespace.uri, "Media") -> mediaDecoder.widen,
  QualifiedName(XjdfNamespace.uri, "Color") -> colorDecoder.widen,
  // ... 102 записи — генерируйте таблицу скриптом, а не руками
)
```

Энкодеру реестр не нужен — у каждого узла есть `elementName` (плюс `ForeignQName` для
расширений). Неизвестное имя → `NamedSpecificResource`/generic-сообщение, если namespace
иностранный; стандартное неизвестное имя → ошибка (защита `ForeignQName` из модели).

### 4. Round-trip-закон

```scala
def roundTrip[A: XmlEncoder: XmlDecoder: Eq](value: A): Boolean =
  XmlDecoder[A].decode(XmlEncoder[A].encode(value)) match
    case Right(decoded) => Eq[A].eqv(value, decoded)
    case Left(_)        => false
```

Для Float/Double используйте `Eq` с допуском из этапа 01. Закон прогоняется на
фикстурах: возьмите примеры из нормативного текста (Example 8.5 MediaLayers,
Example 7.5 QueryResource, Example 7.8 SignalResource) как literal-строки XML.

### 5. Документный проход ID/IDREF

Модель различает `XsdId` (объявление) и `XsdIdRef` (ссылка), но целостность ссылок —
свойство всего документа. После декодирования:

```scala
def checkReferences(doc: XJDF): ValidatedNel[ValidationError, Unit] =
  val registry: Map[String, NodeKind] = buildRegistry(doc) // State-проход, собирает все id (ключ — id.value)
  doc.collectIdRefs.traverse_ { ref =>
    registry.get(ref.value) match
      case Some(_) => ().validNel
      case None    => ValidationError.InvalidValue("IDREF", ref.value, "an existing ID").invalidNel
  }
```

Реализация `collectIdRefs` — обход дерева (ручной или на `Traverse`), `buildRegistry` — аккумуляция
через `State`/`Monoid` (`Map` с конфликтами ⇒ `DuplicateId`). Это закрывает отложенные
документные проверки из аудита.

### 6. Wildcards и расширения

- `Extensions.attributes` — произвольные атрибуты: энкодер пишет как есть, декодер складывает
  в `Map[QualifiedName, ExtensionValue]`;
- `ExtensionElement` — foreign-элементы с упорядоченным mixed content (`ExtensionContent`):
  декодер обязан сохранять порядок `Text/Element/Comment/PI` (модель это уже умеет);
- `ForeignQName` гарантирует: стандартное имя не протечёт как foreign.

## Реестр нормативных расхождений (что кодек обязан знать)

Эти места нормативный текст описывает иначе, чем старый XSD — модель уже исправлена,
кодек должен **не** «исправлять обратно»:

| Тема | Норматив 2.2 | Старый XSD | Поведение кодека |
|---|---|---|---|
| `@Version` | `2.0/2.1/2.2` | только 2.0/2.1 | принимать 2.2 |
| `Resource/@Brand` | `string` | `boolean` | строка |
| `Part/@BlockName`, `PartWaste/@ModuleIDs`, `AssemblyItem/@ChildRef` | `NMTOKEN`/`NMTOKENS`/`IDREF` | corrupt (XYPair/float/float) | как в модели |
| `ResourceSet/@CombinedProcessIndex` | `IntegerList` | `FloatList` | целые |
| `Condition/@PartContext` | `NMTOKENS` | `NMTOKEN` | список |
| `ColorMeasurementConditions/@Illumination` | `NMTOKEN` | `float` | токен |
| `GeneralID/@DataType` | Table A.14 (`NamedFeature` и др.) | stale | как в модели |
| `BoxFoldingParams` | `BoxFoldAction*` + `Action="Glue"` | устаревшая форма | 2.2-форма; legacy-глюи читаются для совместимости |
| `MediaLayers` | упорядоченные `Glue* | Media*` | фикс. пара | упорядоченный список |
| `ChannelMode` | `FireAndForget | Reliable`; в `Subscription` — список | верен | список с порядком |
| `RunList/@Docs` | `IntegerRange` | `IntegerList` | диапазон из двух целых |

## Задачи (пошагово)

1. Модуль `codec-xml`; cats-parse в зависимостях; XML-AST + парсер с позициями ошибок.
2. `XmlEncoder`/`XmlDecoder` для скаляров: `Nmtoken`, `XsdId`/`XsdIdRef`, `XjdfString`,
   `XsdDateTime`, `XsdDuration`, числа, списки (`NMTOKENS`, `FloatList`, `IntegerRange`).
3. Кодеки для узлов снизу вверх: `Part`, `Comment`, `GeneralId`, `Glue`, `Media`… → `Resource` →
   `ResourceSet` → `XJDF`; и messaging: `Header` → 44 сообщения → `XJMF`.
4. Реестр диспетчеризации для `SpecificResource`/`Message` (таблица генерируется скриптом).
5. Эмиттер: каноничный XML с правильным namespace (корень `http://www.CIP4.org/JDFSchema_2_0`),
   экранирование, порядок атрибутов/детей как в нормативных примерах.
6. Фикстуры из нормативного текста: декод → ассерты по полям; энкод → строковое сравнение
   с нормативным примером (с нормализацией пробелов).
7. Round-trip-закон на фикстурах; `Eq` с допуском для float-полей.
8. `checkReferences`: реестр ID + проверка всех IDREF; тесты на dangling и duplicate.
9. Wildcard-тесты: foreign-элементы и атрибуты; стандартное имя в foreign-позиции → ошибка.

## Definition of Done

- [ ] Нормативные примеры (MediaLayers, QueryResource, SignalResource, …) проходят декод-ассерты.
- [ ] Энкодер выдаёт XML, совпадающий с нормативными примерами (по модулю форматирования).
- [ ] Round-trip `decode ∘ encode = id` зелёный на всех фикстурах.
- [ ] ID/IDREF: dangling-ссылки и дубликаты отклоняются с читаемыми ошибками.
- [ ] Foreign-контент сохраняет порядок mixed content и namespace.
- [ ] `sbt "clean ; compile ; test"` зелёный; кодек-модуль не тянет IO/эффекты.

## Риски и альтернативы

- **Ручной парсер vs библиотеки.** Альтернативы: fs2-data-xml (потоковость, но требует
  fs2/cats-effect уже здесь) или scalaxb (генерация из XSD — быстро, но XSD сам устарел и
  противоречит нормативу, см. реестр выше). Выбор cats-parse: чистый, тестируемый,
  полный контроль над нормативными расхождениями. Если позже понадобится потоковый разбор
  гигантских RunList — fs2-data в этапе 07, с тем же AST.
- **Строковое сравнение с нормативными примерами хрупко** — нормализуйте пробелы и порядок
  атрибутов перед сравнением, фиксируйте это в тест-утилите.
- **Порядок полей в эмиттере** — держите порядок как в нормативных таблицах; это упрощает
  сравнение и диффы в интеграциях.
