# ADR-0012 — Пустой `Certification` (Table 8.8): prose SHALL против optional-атрибутов XSD

- **Статус:** принято (решение владельца от 2026-08-16, выбор при старте среза M1.6-1)
- **Дата:** 2026-08-16
- **Задача:** M1.6-1 (`Certification`, §8.7 / Table 8.8; PR-22)
- **Связанные ADR:** ADR-0003 (форма локальных правил), ADR-0006 (политика severity),
  ADR-0007 (закрытые enum vs открытые каталоги), ADR-0011 (прецедент prose vs XSD)

---

## Context

Элемент `Certification` (§8.7) описывает сертификационные свойства ресурса или
процесса. **Table 8.8** перечисляет ровно три атрибута, и все три помечены `?`:

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Claim?` | string | Name of the certification as defined by the issuing organization. Values include: `FSC 100%`, `FSC Mix 70%`, `FSC Mix Credit`, `FSC Recycled 85%`, `FSC Recycled Credit`, `PEFC nn%`, `PEFC Certified`, `PEFC Recycled`. |
| `@Identifier?` | string | Certification identification number as defined by the issuing organization. |
| `@Organization?` | NMTOKEN | Identifier of the issuing organization. Values include: `CFCC`, `FSC`, `IFCC`, `PEFC`. |

`schema.xsd` (структурный оракул, §1.2) подтверждает: подэлементов нет,
ID/IDREF-атрибутов нет, все три атрибута опциональны:

```xml
<xs:element name="Certification">
    <xs:complexType>
        <xs:attribute name="Claim" type="xs:string" use="optional"/>
        <xs:attribute name="Identifier" type="xs:string" use="optional"/>
        <xs:attribute name="Organization" type="xs:NMTOKEN" use="optional"/>
        <xs:anyAttribute namespace="##other" processContents="lax"/>
    </xs:complexType>
</xs:element>
```

Однако **все контейнеры** элемента формулируют требование к каждому экземпляру:

- Table 4.21 (`ColorIntent/SurfaceColor`, Sheet 2), *(New in XJDF 2.1)*:
  «Each Certification SHALL specify a minimum requested **ink** certification level.
  If more than one Certification is present, at least one of the ink certification
  levels SHALL be met.»
- Table 4.32 (`MediaIntent`): «Each Certification SHALL specify a minimum requested
  **paper** certification level. …»
- Table 4.33 (`ProductionIntent`), *(New in XJDF 2.1)*: «Each Certification SHALL
  specify a minimum requested certification level **for production**. …»
- Table 6.114 (`Media`): «Each Certification SHALL specify a paper certification level.»
- Table 6.83 (`Ink`), Table 6.117 (`MiscConsumable`) — та же формулировка;
  оба ресурса ещё не смоделированы.

Итого возникают **два разных вопроса**, которые нельзя решать одним правилом.

### Вопрос 1: пустой `<Certification/>`

XSD допускает элемент без единого атрибута. Такой элемент не специфицирует
никакого уровня сертификации, то есть прямо противоречит «Each Certification
SHALL specify a … certification level». Расхождение prose vs XSD — по §1.2
приоритет у текста, XSD остаётся тест-оракулом (прецеденты: N-50/ADR-0011,
`HolePattern/@Pattern` в M1.6-5).

### Вопрос 2: «at least one of the … levels SHALL be met»

Это утверждение **о фактическом производстве**, а не об инварианте документа.
XJDF-тикет не содержит поля «уровень выполнен»; при нескольких `Certification`
набор читается как дизъюнкция требований, выполнение которой проверяет не
валидатор документа, а производственный процесс. Трактовать фразу как
структурное «at least one Certification present» нельзя: кардинальность во всех
контейнерах — `*` (`minOccurs="0"`), то есть ноль элементов заведомо законен.

## Decision

1. **Пустой `Certification` — SHALL-нарушение.** Метод `Certification.law`
   (`DomainRule`-совместимая сигнатура `(Certification, XPath) => Chain[Issue]`,
   ADR-0003) требует наличия **хотя бы одного** из `@Claim`, `@Identifier`,
   `@Organization`; иначе выдаётся `Issue.errorC(IssueCode.CertificationLevelMissing, …)`
   с кодом `CERTIFICATION-LEVEL-MISSING`. Severity — `Error`: это SHALL, а не
   SHOULD (ADR-0006).
2. **«Уровень» не сужается до `@Claim`.** Формулировка «specify a certification
   level» не называет конкретный атрибут; `@Organization="FSC"` или
   `@Identifier="FSC-C012345"` тоже идентифицируют сертификацию. Требовать
   именно `@Claim` — более узкая трактовка, отвергнутая как рискующая ложными
   срабатываниями (см. Alternatives).
3. **Контейнерное «at least one … SHALL be met» не проверяется.** Оно
   документируется в scaladoc элемента и всех контейнеров и вносится в реестр
   сознательных отклонений `docs/SPEC-COVERAGE.md`. Позитивный тест закрепляет
   поведение: два `Certification` в `ProductionIntent` оставляют тикет валидным.
4. **Единая точка обхода контейнеров.** `Certification.containerLaw(chain, at)`
   применяет `law` к каждому элементу `Certification*` и индексирует XPath
   (`…/Certification[i]`). Все четыре смоделированных контейнера
   (`ColorIntent/SurfaceColor`, `MediaIntent`, `ProductionIntent`, `Media`)
   вызывают именно её, поэтому правило не может разойтись между ними.
5. **Открытые каталоги (ADR-0007).** `@Claim` и `@Organization` описаны словами
   «Values include» → открытые каталоги `Catalog.CertificationClaim`
   (`XjdfString`: значения содержат пробелы и `%`, поэтому не `NmToken`; плюс
   `pefcPercent(nn)` для параметрического `PEFC nn%`) и
   `Catalog.CertificationOrganization` (`NmToken`). Значение вне каталога
   остаётся законным — есть тест расширяемости.
6. **Кардинальность.** Во всех шести контейнерах XSD даёт
   `minOccurs="0" maxOccurs="unbounded"` → `Chain[Certification]`; runtime-проверки
   «at least one present» не вводится.

## Alternatives

| Вариант | Оценка |
| --- | --- |
| A. Следовать XSD буквально: пустой `Certification` валиден, ограничение только в scaladoc | Нарушает §1.2 (приоритет prose); срез остался бы без единого негативного теста, хотя SHALL в спецификации сформулирован явно и четырежды |
| B. Требовать именно `@Claim` | Более узкая трактовка «certification level»: отвергает законный `Certification[@Organization="FSC"][@Identifier="FSC-C012345"]`, который идентифицирует сертификацию однозначно; текст Table 8.8 не называет `@Claim` носителем «уровня» |
| C. Требовать хотя бы один из трёх атрибутов (**решение**) | Минимальная трактовка, при которой SHALL перестаёт быть пустым: отвергается ровно тот случай, когда элемент не специфицирует ничего |
| D. Превратить контейнерное «at least one … met» в предупреждение | SHOULD/MAY не становятся ошибками без явной политики, а здесь и не SHOULD: проверяемого предиката в документе нет (ADR-0006) |

## Consequences

- **Положительные:** каждый `Certification` в тикете несёт содержательные
  данные; расхождение prose/XSD зафиксировано явно, а не разрешено молча;
  единый `containerLaw` исключает дрейф правила между четырьмя контейнерами.
- **Отрицательные:** документ, схемно валидный по `schema.xsd`, может быть
  отвергнут доменным валидатором. Это осознанная цена приоритета prose;
  расхождение внесено в реестр сознательных отклонений, а декодер M2 обязан
  сообщать код `CERTIFICATION-LEVEL-MISSING`, а не молча отбрасывать элемент.
- **Отложено:** `Ink` (Table 6.83) и `MiscConsumable` (Table 6.117) — ещё два
  контейнера `Certification*`; при их моделировании (M3) обход расширяется
  вызовом того же `containerLaw`.
- Срок пересмотра: при появлении нормативного разъяснения CIP4 либо при
  обновлении `schema.xsd`, делающего атрибуты обязательными.

## Normative references

- `reference/xjdf/8 – Subelements.md`, §8.7, Table 8.8 (`Certification Element`);
- `reference/xjdf/4 – Product Intent.md`, Table 4.21 (Sheet 2, `SurfaceColor`),
  Table 4.32 (`MediaIntent`), Table 4.33 (`ProductionIntent`);
- `reference/xjdf/6 – Resources.md`, Table 6.114 (`Media`), Table 6.83 (`Ink`),
  Table 6.117 (`MiscConsumable`);
- `reference/xjdf/schema.xsd`: `<xs:element name="Certification">` (три
  `use="optional"`), шесть `<xs:element ref="Certification" minOccurs="0"
  maxOccurs="unbounded"/>`;
- ROADMAP §1.2 (приоритет источников истины), ADR-0003, ADR-0006, ADR-0007.

## Migration impact

Изменения аддитивны, breaking change отсутствует: во все четыре контейнера
добавлено поле `certifications: Chain[Certification] = Chain.empty` со значением
по умолчанию, поэтому существующие вызовы конструкторов
(`SurfaceColor`, `MediaIntent`, `ProductionIntent`, `Media`) компилируются без
изменений. `Media.references` перестал быть константой `Chain.empty` и обходит
`certifications` (которые по Table 8.8 ссылок не содержат) — поведение прежнее,
факт теперь проверяется, а не предполагается.
