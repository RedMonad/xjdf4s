# ADR-0007 — Закрытые enum vs открытые каталоги; JSON Exceptions вне домена

- **Статус:** принято
- **Дата:** 2026-08-15
- **Задача:** M1.2-2 (PR-5); дедлайн по ROADMAP §6 — «до M1.2-2»
- **Закрывает находки:** N-06, N-07, N-08, N-09; регистрирует N-47, N-48, N-49
- **Связанные ADR:** ADR-0010 (нормализация кодеков), ADR-0003 (форма локальных правил)

---

## Context

XJDF 2.2 использует два разных механизма для «списков допустимых значений», и они
различаются нормативно, а не стилистически (§1.10.3).

**§1.10.3.1 «Enumeration data types»:**

> These are designed to be Machine readable values with a limited, well-defined, closed set of valid values. Enumeration data types cannot be localized. Thus implementers can rely on the values of these data types to be from the known list.
>
> If the data type of the attribute in the tables is ‘enumeration’ then the description contains either the phrase “Allowed values are:” to show a set of values, or “Allowed value is from:” to refer to a set of values defined elsewhere. In either case one of the values from the indicated set SHALL be used as the value of the attribute.

**§1.10.3.2 «NMTOKEN data types»:**

> These are designed to be Machine readable values with a limited set of recommended values but an unlimited set of valid values. … As the list of values is an open list, implementers cannot rely on the values of these data types to be from a predetermined list.
>
> If the data type of the attribute in the tables is ‘NMTOKEN’ or ‘string’ then the description contains either the phrase “Values include:” … or “Values include those from:” … This does not preclude the use of other values as required by vendor or customer extensions.

Структурно это разделение видно и в самом Appendix A: раздел **A.2 «Enumerations»**
(«This section contains tables, each with a closed set of values») против раздела
**A.3 «Preferred NMTOKEN Values»** («Although these types are open lists, the values in
these tables SHOULD be used where possible»).

В коде `xjdf4s` до M1.2-2 это различие проведено непоследовательно:

- часть открытых каталогов уже смоделирована правильно — `Catalog.ContactType`
  (A.3.2), `Catalog.PrintingTechnology` (A.3.16), `Catalog.ProductType` (A.3.18),
  `Catalog.NodeCategory`, `Catalog.FluteType`, `Catalog.Coatings` — как значения
  `NmToken` в `prim/Common.scala`;
- закрытые enum в `prim/Enums.scala` местами неполны: при переносе таблиц потеряны
  именно значения с пометками *New in XJDF 2.1* и *New in XJDF 2.2*;
- Scala-имя case молча использовалось как wire-токен через fallback
  `case other => NmToken.unsafe(other.toString)`, что уже дало неверный токен
  `"Glued"` вместо нормативного `"Glue"` (N-08);
- `NamedColor` смоделирован как закрытый enum из 16 значений, хотя нормативные
  источники дают 147 (см. «Decision, часть 3» — здесь prose и XSD расходятся, и это
  расхождение решается настоящим ADR, а не молча).

Дополнительно спецификация содержит пометки «JSON Exception»: атрибуты, существующие
только в JSON-кодировании и запрещённые в XML. Без явного решения они склонны
протекать в доменные case-классы, где им не место.

## Decision

### Часть 1 — открытые и закрытые типы

1. Закрытый Scala `enum` (`XjdfEnum` + `XjdfEnumCompanion`) допустим **только** если
   тип атрибута в таблице — `enumeration`/`enumerations` **и** набор значений
   перечислён на месте («Allowed values are:») либо задан ссылкой на закрытую таблицу
   раздела A.2 («Allowed value is from:»).
2. Если тип — `NMTOKEN`/`NMTOKENS`/`string`, либо спецификация ссылается на внешний
   открытый каталог (раздел A.3, `[CIP4Names]`, `[Color Names]`), тип моделируется
   открытым: `NmToken` + объект `Catalog.*` с рекомендованными значениями и тестом на
   расширяемость («каталог принимает значение вне списка»).
3. **Scala-имя case не является wire-токеном по умолчанию.** Для каждого закрытого
   enum ведётся golden-множество wire-токенов, выписанное литералом рядом с тестом со
   ссылкой на таблицу. Там, где Scala-имя вынужденно отличается от токена, `def token`
   задаётся явным исчерпывающим `match` без fallback-ветки `case other => …`:
   fallback скрывает именно те дефекты, которые породили N-08.
4. Известные намеренные расхождения «Scala-имя ↔ wire-токен» ведутся централизованным
   реестром (ROADMAP, Приложение C) и переиспользуются кодеками M2.
5. Полнота закрытых enum проверяется **машинной сверкой** с Appendix A, а не глазами:
   сверка сопоставляет множество `all.map(_.token.value)` с множеством значений
   соответствующей таблицы A.2. Именно эта сверка выявила N-47…N-49.

### Часть 2 — JSON Exceptions

Все пометки «JSON Exception» собираются в единый реестр кодека M2 и **не протекают** в
доменные case-классы. Это касается как минимум:

| Артефакт | Норма | Статус |
| --- | --- | --- |
| `XJDF/@Name` | Table 3.1, Sheet 2; *New in XJDF 2.2* | codec-only (M2) |
| `XJDF/@$schema` | Table 3.1, Sheet 2; *New in XJDF 2.2* | codec-only (M2) |
| `Comment/@Text` | Table 8.14: «`@Text` MAY be specified when encoded in JSON and SHALL NOT be specified when encoded in XML» | codec-only (M2) |
| `@Types` как массив | JSON-кодирование `NMTOKENS` | codec-only (M2) |
| `AuditPool` как массив с `@Name` | Table 3.3; *New in XJDF 2.2* | codec-only (M2) |

Обоснование: домен описывает XJDF как модель, а не как одну из её сериализаций.
Пара кодеков XML/JSON обязана быть двумя интерпретациями одного домена (ADR-0010);
поле, существующее ровно в одной из них, — свойство кодека.

### Часть 3 — `NamedColor` (расхождение prose ↔ XSD, §1.2)

Источники расходятся, поэтому решение фиксируется здесь явно.

**За закрытый тип:**

- атрибуты, использующие `NamedColor`, объявлены в таблицах с DATA TYPE
  `enumeration` и формулой «Allowed value is from: NamedColor» (Table 4.11
  `@HeadBandColor`, Table 4.8 `@CoverColor`/`@BindingColor`/`@BackCoverColor`,
  Table 4.19 `@ReinforceColor`, Table 4.32 и Table 6.114 `@MediaColor`) — то есть
  дословно формула §1.10.3.1;
- §A.2.30 расположен внутри раздела **A.2 Enumerations**, а не A.3;
- `schema.xsd` определяет `<xs:simpleType name="NamedColor">` как `restriction
  base="xs:string"` со 147 `xs:pattern`, соответствующими регистронезависимым
  ключевым словам SVG 1.1 (`ALICEBLUE` … `YELLOWGREEN`).

**За открытый тип:**

- §A.2.30 не содержит таблицы значений вообще — единственная нормативная фраза:
  «`NamedColor` specifies a machine-readable definition of a color. For a list of
  allowed values, see `[Color Names]`.» Множество значений определено **внешним**
  документом (Appendix G: SVG 1.1 Second Edition), а не спецификацией XJDF;
- следовательно, набор эволюционирует вне версий XJDF, и §1.10.3.1 гарантия «implementers
  can rely on the values … to be from the known list» на практике не обеспечивается
  текстом XJDF;
- закрытый enum из 16 значений делает невыразимыми 131 легальное значение и любые
  vendor-расширения; каждое такое значение — потеря данных при декодировании M2, а
  ADR-0010 запрещает молча отбрасывать неизвестные данные.

**Решение (владелец, 2026-08-15):** `NamedColor` моделируется **открыто** —
`NmToken` + `Catalog.NamedColor` со 147 рекомендованными значениями `[Color Names]`.
Приоритет отдан внешней определённости каталога и требованию lossless-декодирования.

Уточнение к тестам: ROADMAP §8 (M1.2-2) предлагал проверять расширяемость значением
`Pantone 123 C`. Это значение **непригодно как тест-фикстура**: оно содержит пробелы и
потому не является валидным `NMTOKEN`, тогда как открытые каталоги проекта
типизированы через `NmToken`. Тест на расширяемость использует значение вне
рекомендованного списка, но валидное как NMTOKEN — `MintCream` (реальное имя SVG,
отсутствующее в прежнем 16-значном enum). Полная лексическая проверка `NamedColor`
против XSD-pattern-ов — задача кодеков M2 (ср. R5/ADR по `RegExp`).

## Alternatives

1. **`NamedColor` как закрытый тип на 147 значений** (opaque type поверх
   канонизированного имени SVG). Ближе всего к букве §1.10.3.1 и к `schema.xsd`.
   Отклонено: набор определён внешним документом и эволюционирует вне релизов XJDF;
   любой цвет вне SVG-списка (краски по каталогам вендоров) становится невыразим, что
   конфликтует с требованием lossless-декодирования ADR-0010.
2. **Гибрид `Known(<один из 147>) | Extension(NmToken)`.** Сохраняет и закрытый список,
   и escape hatch. Отклонено для M1: удваивает поверхность API и ветвление в кодеках
   ради различия, которое на уровне домена не влечёт никакого правила; при
   необходимости вводится позже поверх `Catalog.NamedColor` без ломающего изменения
   формата.
3. **Оставить 16 значений и дополнить список позже.** Отклонено: это и есть
   зафиксированный дефект N-09.
4. **Считать `schema.xsd` решающим источником.** Отклонено: противоречит §1.2, где XSD —
   структурный oracle, а приоритет за нормативным текстом.
5. **Fallback `case other => NmToken.unsafe(other.toString)` во всех enum.** Отклонено:
   именно он скрыл N-08; отсутствие fallback превращает добавление case без токена в
   ошибку компиляции (исчерпывающий match).

## Consequences

**Положительные**

- Расхождение «Scala-имя ↔ wire-токен» перестаёт быть неявным: каждый случай — строка
  реестра плюс golden-тест.
- Машинная сверка с Appendix A становится воспроизводимой процедурой и уже дала три
  новые находки (N-47…N-49).
- Домен не загрязняется JSON-специфичными полями; M2 получает готовый реестр
  исключений.
- Открытый `NamedColor` снимает потерю данных на 131 значении SVG и на
  vendor-расширениях.

**Отрицательные / принимаемые издержки**

- `NamedColor` теряет статическую исчерпываемость: опечатка в имени цвета больше не
  ловится компилятором. Компенсация — `Catalog.NamedColor` как единственный
  рекомендуемый способ записи констант и тест на присутствие всех 147 значений
  каталога.
- Это **ломающее изменение публичного API** (см. Migration impact).
- `prim/Enums.scala` — файл с Fan-In 24 (ROADMAP §3.5); любое изменение здесь
  затрагивает широкий срез сборки и требует полного прогона laws и examples (§7.3).

## Normative references

- §1.10.3.1 «Enumeration data types», §1.10.3.2 «NMTOKEN data types»
  (`reference/xjdf/1 – Introduction.md`)
- Appendix A, раздел A.2 «Enumerations»; раздел A.3 «Preferred NMTOKEN Values»
  (`reference/xjdf/Appendix A – Data Types and Values.md`)
- Table A.15 «DeviceStatus Enumeration Values» — `Cleanup`, `Setup` *(New in XJDF 2.1)*
- Table A.26 «ISOPaperSubstrate Enumeration Values» — `LWCPlus`, `LWCStandard`,
  `NewsPlus`, `SCPlus`, `SCStandard`, `SNP` *(New in XJDF 2.1)*, `PS9` *(New in XJDF 2.2)*
- Table A.30 «MediaType Enumeration Values» — `Synthetic` *(New in XJDF 2.1)*
- Table A.36 «Scope Enumeration Values» — `Device` *(New in XJDF 2.2)*
- Table A.40 «Sides Enumeration Values» — `Unprinted` *(New in XJDF 2.1)*:
  «Page contents SHALL NOT be imposed on either side.»
- §A.2.30 «NamedColor»; `[Color Names]` = SVG 1.1 Second Edition (Appendix G)
- Table 4.11 «HardCoverBinding Element», Sheet 1, `@Jacket`: «Allowed values are:
  **None** – No jacket is needed. **Loose** – The jacket is loosely wrapped.
  **Glue** – The jacket is glued to the spine.»
- Table 3.1 Sheet 2 (`@Name`, `@$schema`), Table 3.3 (`AuditPool/@Name`),
  Table 8.14 (`Comment/@Text`) — JSON Exceptions
- `reference/xjdf/schema.xsd`: `NamedColor` (147 pattern-ов), `HardCoverBinding/@Jacket`
  (`None|Loose|Glue`), `DeviceStatus` (7 значений)

**Отмеченное расхождение oracle.** `schema.xsd` отстаёт от prose по двум таблицам:
`Sides` не содержит `Unprinted`, `Scope` не содержит `Device`, хотя обе пометки
*New* присутствуют в Appendix A. По §1.2 приоритет за нормативным текстом — enum
пополняются по prose. Расхождение зафиксировано здесь и в Приложении C, чтобы при
валидации против XSD в M2 оно не было принято за ошибку домена.

## Migration impact

**Ломающее изменение:** `NamedColor` перестаёт быть типом; поля меняют тип
`Option[NamedColor]` → `Option[NmToken]`.

Полный список call sites (статический grep по `modules/`, на момент принятия ADR):

| Файл | Строки | Поля |
| --- | --- | --- |
| `intents/Binding.scala` | 18, 20, 22 | `BindingIntent.coverColor`, `.bindingColor`, `.backCoverColor` |
| `intents/Binding.scala` | 85 | `HardCoverBinding.headBandColor` |
| `intents/Binding.scala` | 177 | `Tabs.reinforceColor` |
| `intents/MediaLayout.scala` | 22 | `MediaIntent.mediaColor` |
| `resources/Media.scala` | 30 | `Media.mediaColor` |
| `prim/Enums.scala` | 264–290 | само определение `enum NamedColor` + `object NamedColor` |

Вне этого списка `NamedColor` не встречается: в `laws/Arbitraries.scala`,
`examples/*` и `resources/Color.scala` он не используется (`Color` оперирует
`XjdfString`/`CMYKColor`/`LabColor`), поэтому генераторы и примеры правок не требуют.

**Путь миграции для пользователей API:**

```scala
// было
Media(MediaType.Paper, mediaColor = Some(NamedColor.White))

// стало — рекомендованное значение из каталога
Media(MediaType.Paper, mediaColor = Some(Catalog.NamedColor.White))

// стало — значение вне рекомендованного списка (легально, §1.10.3.2)
Media(MediaType.Paper, mediaColor = NmToken.from("MintCream"))
```

Переходный alias `NamedColor` не вводится: тип и его значения исчезают одновременно,
поэтому любое использование ломается на компиляции, а не молча меняет семантику.
Это соответствует правилу проекта «невалидный вход не превращается молча в `None`».

**Неломающие изменения** этого же PR: пополнение `Sides`, `DeviceStatus`,
`ISOPaperSubstrate`, `MediaType`, `Scope` новыми case-ами и переименование
`HardCoverJacket.Glued` → `GlueApplied`. Последнее ломает исходный код, ссылающийся на
`HardCoverJacket.Glued`; в `modules/` таких ссылок нет.

**Срок пересмотра:** при заморозке API кодеков M2 (ADR-0010) — проверить, что
`Catalog.NamedColor` покрывает актуальный `[Color Names]`, и что лексическая валидация
цвета реализована на стороне кодека.
