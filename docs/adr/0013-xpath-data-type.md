# ADR-0013 — Тип данных XJDF `XPath`: `xsd:token` в Table A.1 против `xs:string` в XSD

- **Статус:** принято (M1.6-6b/B1, решение по приоритету источников ROADMAP §1.2)
- **Дата:** 2026-08-16
- **Задача:** M1.6-6b/B1 (`XPath` + `Expr`, Table A.1 / Table 8.47)
- **Закрывает находки:** N-54
- **Связанные ADR:** ADR-0002 (validation `model.XPath`), ADR-0010 (нормализация кодеков)

---

## Context

M1.6-6b вводит тип данных `XPath`, используемый обязательным атрибутом
`Expr/@Path` (Table 8.47). Нормативные источники расходятся в его базовом XSD-типе.

**Appendix A, Table A.1**:

> `XPath` | `xsd:token` | None | Values of type `XPath` represent an XPath expression as described in `[XPath]`.

Для `xsd:token` whitespace facet имеет значение `collapse`: символы `#x9`,
`#xA`, `#xD`, `#x20` нормализуются, последовательности и края схлопываются.

**`reference/xjdf/schema.xsd`**:

```xml
<xs:simpleType name="XPath">
    <xs:restriction base="xs:string"/>
</xs:simpleType>
```

`xs:string` сохраняет whitespace. Release notes XJDF 2.1/2.2 не содержат
изменения или разъяснения `XPath`. Следовательно, это прямое расхождение prose
и XSD, которое по ROADMAP §1.2 нельзя разрешать молча.

В коде уже существует `xjdf4s.model.XPath` — opaque type из
`ValidationTypes.scala`, который хранит **путь к месту ошибки в доменном
тикете**. Он не является значением атрибута XJDF, не реализует Appendix A и
может содержать синтетические локаторы валидатора. Повторное использование
этого типа для `Expr/@Path` смешало бы два независимых понятия.

## Decision

1. **Следовать Table A.1.** Доменный тип реализует нормативный `xsd:token`, а
   расхождение `schema.xsd` фиксируется тестом-оракулом и реестром отклонений.
   Это прямое применение приоритета источников ROADMAP §1.2.
2. **Scala-имя — `prim.XjdfXPath`.** Wire/spec-имя остаётся `XPath`, но Scala-тип
   получает префикс `Xjdf`, как `XjdfString`: это исключает коллизию с
   `model.XPath` при распространённых wildcard-импортах `model.*` + `prim.*` и
   делает случайное смешение типов невозможным на компиляции.
3. **Канонический конструктор.** `XjdfXPath.from` схлопывает ровно XML whitespace
   (`#x9`, `#xA`, `#xD`, `#x20`) по правилу `xsd:token` и отвергает пустой
   результат. `unsafe` использует тот же путь и явно помечен как бросающий.
4. **Грамматика XPath не интерпретируется в M1.** Table A.1 ссылается на
   `[XPath]`, но доменное ядро не должно выбирать версию/движок XPath и не
   вычисляет выражения. Полная синтаксическая проверка и wire-нормализация
   остаются границей кодека M2 (ADR-0010); M1 обеспечивает номинальное различие,
   token-нормализацию и непустоту, аналогично консервативной политике `RegExp`.
5. **`Expr` структурен.** `Expr(name: NmToken, path: XjdfXPath)` делает оба
   обязательных атрибута Table 8.47 непредставимыми как отсутствующие. Таблица
   не содержит локального проверяемого SHALL: implied `text()` — семантика
   вычислителя, а правила соответствия `Expr` переменным зависят от родителя
   `MetadataMap` и реализуются в M1.6-6b/B2.

## Alternatives

| Вариант | Оценка |
| --- | --- |
| A. Следовать `schema.xsd` и хранить произвольный `xs:string` | Отклонено: нарушает приоритет Table A.1; допускает лексические значения, которые не являются каноническим `xsd:token` |
| B. Переиспользовать `model.XPath` | Отклонено: смешивает wire-значение XJDF с внутренним локатором ошибки; имя одинаково, законы и жизненный цикл различны |
| C. Ввести `prim.XPath` и везде использовать import alias | Типы различны, но ломаются существующие `model.*` + `prim.*` wildcard call sites; цена миграции не несёт доменной пользы |
| D. Ввести `prim.XjdfXPath` и следовать `xsd:token` (**решение**) | Номинально и лексически точно, коллизия видна в имени, существующие validation call sites не меняются |
| E. Проверять выражение через `javax.xml.xpath` в M1 | Отклонено: не зафиксирована версия/политика namespace, variables и functions; JAXP-движок стал бы неоговорённой частью доменного контракта |

## Consequences

- **Положительные:** `Expr/@Path` нельзя спутать с местом `Issue`; whitespace
  канонизируется по Table A.1; prose/XSD-конфликт закреплён машинным тестом.
- **Отрицательные:** XSD-валидное значение с существенным ведущим, хвостовым или
  повторным XML whitespace нормализуется доменом. Это осознанное следствие
  приоритета prose; кодек M2 обязан применять ту же канонизацию.
- **Совместимость:** изменение аддитивно. Существующие `model.XPath` и его call
  sites не меняются; `XjdfXPath` и `Expr` ранее отсутствовали.
- **Срок пересмотра:** нормативное исправление CIP4, синхронизирующее Table A.1
  и `schema.xsd`, либо решение M2 о поддерживаемой версии XPath.

## Normative references

- `reference/xjdf/Appendix A – Data Types and Values.md`, §A.1, Table A.1;
- `reference/xjdf/8 – Subelements.md`, §8.29.1, Table 8.47;
- `reference/xjdf/schema.xsd`, `simpleType XPath`, element `Expr`;
- ROADMAP §1.2, §9.1, §15; ADR-0002, ADR-0010.

## Migration impact

Breaking change отсутствует. Новые API:

- `xjdf4s.prim.XjdfXPath.from` / `.unsafe` / `.value`;
- `xjdf4s.model.elements.Expr(name: NmToken, path: XjdfXPath)`.

В коде, где одновременно обсуждаются оба понятия, рекомендуется явное имя
`XjdfXPath` для XJDF-выражения и при необходимости alias
`import xjdf4s.model.{XPath as ValidationXPath}` для локатора валидатора.
