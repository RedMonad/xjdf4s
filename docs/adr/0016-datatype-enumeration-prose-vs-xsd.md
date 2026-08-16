# ADR-0016 — `DataType` (Table A.14) против inline-энумерации `GeneralID/@DataType` в `schema.xsd`

- **Статус:** принято (N-59, решение владельца от 2026-08-16, приоритет источников ROADMAP §1.2)
- **Дата:** 2026-08-16
- **Задача:** M1.6-14 (NamedFeatures §3.1.3.1)
- **Закрывает находки:** N-59
- **Связанные ADR:** ADR-0007 (закрытые enum против открытых каталогов), ADR-0010 (нормализация кодеков), ADR-0013 (аналогичное расхождение prose/XSD для `XPath`), ADR-0015 (schema-gap `FileSpec/@NPage`)

---

## Context

Предстартовая сверка M1.6-14 охватывает `GeneralID` (§8.23 / Table 8.28),
поскольку NamedFeature — это `GeneralID[@DataType="NamedFeature"]`.

**Table 8.28 (`reference/xjdf/8 – Subelements.md`)**:

> `@DataType?` | enumeration | Data type of the variable. Allowed value is from: DataType.

**Table A.14 «DataType Enumeration Values» (`reference/xjdf/Appendix A – Data Types and Values.md`, §A.2.13)**
перечисляет восемь значений:

> `boolean`, `dateTime`, `duration`, `float`, `integer`, `NamedFeature`, `NMTOKEN`, `string`

**`reference/xjdf/schema.xsd`, глобальный элемент `GeneralID`** объявляет
inline-restriction по `xs:NMTOKEN` — тоже восемь значений, но семь из них
записаны в форме с префиксом `xs:`:

```xml
<xs:attribute name="DataType" use="optional">
    <xs:simpleType>
        <xs:restriction base="xs:NMTOKEN">
            <xs:enumeration value="xs:string"/>
            <xs:enumeration value="xs:int"/>
            <xs:enumeration value="xs:float"/>
            <xs:enumeration value="xs:NMTOKEN"/>
            <xs:enumeration value="xs:boolean"/>
            <xs:enumeration value="xs:dateTime"/>
            <xs:enumeration value="xs:duration"/>
            <xs:enumeration value="NamedFeature"/>
        </xs:restriction>
    </xs:simpleType>
</xs:attribute>
```

Соответствие значений однозначно, но лексика расходится в семи из восьми
случаев, а `integer` против `xs:int` расходится ещё и по смыслу базового типа
(`xsd:integer` — произвольная точность, `xs:int` — 32 бита). Release notes
XJDF 2.1/2.2 разъяснений не содержат. По §1.2 молчаливый выбор недопустим.

Дополнительный аргумент в пользу prose: Table A.1 «XJDF Data Types» —
нормативный словарь имён типов домена (`boolean`, `float`, `integer`,
`NMTOKEN`, `string`, `dateTime`, `duration`) — использует ровно те же имена
без префикса `xs:`, и все остальные таблицы спецификации ссылаются на них же.
Форма `xs:int` внутри `xs:NMTOKEN`-энумерации выглядит как перенос имён XML
Schema в данные, а не как отдельный нормативный словарь.

### Минимальная конфликтная фикстура

```xml
<GeneralID xmlns="http://www.CIP4.org/JDFSchema_2_0"
           DataType="integer"
           IDUsage="Copies"
           IDValue="12"/>
```

Фикстура соответствует Table 8.28 и Table A.14. Текущий `schema.xsd`
отклоняет её: `integer` отсутствует в inline-энумерации, где записано
`xs:int`. Обратная фикстура с `DataType="xs:int"` проходит XSD, но нарушает
Table A.14.

## Decision

1. **Ввести закрытый `prim.DataType` по Table A.14.** Восемь значений, wire-
   токены — ровно из prose: `boolean`, `dateTime`, `duration`, `float`,
   `integer`, `NamedFeature`, `NMTOKEN`, `string`.
2. **Типизировать `GeneralID.dataType` как `Option[DataType]`** вместо
   `Option[NmToken]`. Это breaking change: до M1.6-14 модель допускала
   произвольный NMTOKEN там, где Table 8.28 требует значение из закрытого
   набора (тот же класс дефекта, что N-06/N-07/N-52).
3. **Не принимать XSD-написания в доменной модели.** Приоритет — за prose
   (§1.2). Толерантный разбор `xs:int` → `IntegerType` — это нормализация на
   границе декодера, то есть ответственность M2 (ADR-0010), а не доменного
   типа: домен обязан быть каноническим.
4. **Закрепить обе стороны машинным тестом.** `NamedFeatureLaws` проверяет
   восемь prose-значений Table A.14, восемь XSD-написаний в inline-энумерации
   `GeneralID` и факт расхождения ровно в семи из них. Тест падает, если CIP4
   исправит любую из сторон.
5. **Сверять обе стороны на своём уровне.** Prose-сторона — обычной машинной
   сверкой `appendixAEnums` в `EnumLaws` (Table A.14 — таблица Appendix A, то
   есть штатный случай конвенции). XSD-сторона — oracle-тестом в
   `NamedFeatureLaws`, где зафиксировано и само расхождение (ровно семь из
   восьми значений), чтобы находка и решение были видны в одном месте.

Scala-имена семи из восьми членов несут суффикс `Type`
(`BooleanType`, `StringType`, …): `Boolean`, `String`, `Float` и `Int`
столкнулись бы с предопределёнными типами Scala, а частичное переименование
хуже единообразного. Wire-токены при этом заданы явным `match` без ветки
`case other => …` — та же политика, что у `HardCoverJacket` (ADR-0007),
которая ранее уже предотвратила выдуманный токен (N-08).

## Alternatives

| Вариант | Оценка |
| --- | --- |
| A. Следовать XSD (`xs:int`, `xs:string`, …) | Отклонено: нарушает Table A.14 и Table A.1, противоречит §1.2 |
| B. Оставить `Option[NmToken]` | Отклонено: Table 8.28 объявляет `enumeration`; открытый тип делает представимым `DataType="banana"` |
| C. Принимать оба написания в домене | Отклонено владельцем: два лексических представления одного значения ломают каноничность домена и `Eq`; толерантность — граница M2 |
| D. Закрытый enum по prose + ADR + oracle-тест (**решение**) | Следует нормативному приоритету, оставляет XSD воспроизводимым oracle |

## Consequences

- **Положительные:** неконформное значение `@DataType` больше не
  представимо; выбор prose над XSD зафиксирован; изменение любой из сторон
  будет замечено тестом; появляется предпосылка для локального SHALL Table 8.28
  («`@IDValue` SHALL correspond to `@DataType`»), который без закрытого типа
  сформулировать нельзя.
- **Отрицательные:** документ с `GeneralID/@DataType="integer"` не проходит
  текущий `schema.xsd`, а документ с `xs:int` не декодируется без
  нормализации. XML/JSON-кодек M2 обязан: (1) писать prose-токены,
  (2) принимать XSD-написания при чтении, (3) отражать расхождение в
  conformance report как известное ADR-0016.
- **Совместимость:** breaking change публичного API — см. migration impact.
- **Срок пересмотра:** публикация CIP4 исправленной схемы либо erratum к
  Table A.14.

## Normative references

- `reference/xjdf/Appendix A – Data Types and Values.md`, §A.2.13, Table A.14; Table A.1;
- `reference/xjdf/8 – Subelements.md`, §8.23, Table 8.28;
- `reference/xjdf/3 – Structure.md`, §3.1.3.1;
- `reference/xjdf/schema.xsd`, element `GeneralID`;
- ROADMAP §1.2, §9.1, §12, §15; ADR-0007, ADR-0010, ADR-0013, ADR-0015.

## Migration impact

**Breaking change:** `GeneralID.dataType: Option[NmToken]` →
`Option[DataType]`; удалена константа `GeneralID.NamedFeatureDataType:
NmToken`, её заменяет `DataType.NamedFeature`.

Полный список call sites в репозитории на момент изменения:

| Файл | Было | Стало |
| --- | --- | --- |
| `modules/core/src/main/scala/xjdf4s/model/elements/CommonElements.scala` | `dataType: Option[NmToken]`, `val NamedFeatureDataType: NmToken` | `dataType: Option[DataType]`, `def namedFeature(...)`, `isNamedFeature`, `hasLawfulValue`, `law`, `containerLaw` |

Других call sites нет: до M1.6-14 ни один модуль (`core`, `examples`, `laws`)
не конструировал `GeneralID` и не читал `dataType` — поля `generalIds`
существовали только как структурные `Chain[GeneralID] = Chain.empty` в
`XJDF`, `ResourceSet`, `Product`, `Resource` и в `dsl.TicketDraft`.

Миграция потребителя:

```scala
// было
GeneralID(NmToken.unsafe("pool"), XjdfString.unsafe("bar snax"),
          dataType = Some(GeneralID.NamedFeatureDataType))
// стало
GeneralID.namedFeature(NmToken.unsafe("pool"), XjdfString.unsafe("bar snax"))
// либо
GeneralID(NmToken.unsafe("pool"), XjdfString.unsafe("bar snax"),
          dataType = Some(DataType.NamedFeature))
```
