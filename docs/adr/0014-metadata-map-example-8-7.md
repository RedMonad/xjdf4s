# ADR-0014 — `MetadataMap`: Table 8.31 SHALL против Example 8.7

- **Статус:** принято (M1.6-6b/B2, приоритет источников ROADMAP §1.2)
- **Дата:** 2026-08-16
- **Задача:** M1.6-6b/B2 (`MetadataMap`, Table 8.46)
- **Закрывает находки:** N-55
- **Связанные ADR:** ADR-0003 (контекстные правила), ADR-0013 (`Expr/@Path`)

## Context

Table 8.31 явно задаёт правило для `IdentificationField`:

> If MetadataMap elements are present, `MetadataMap/@Name` SHALL be included in
> `@ValueTemplate` to select the data from the MetadataMap.

Table 8.46 одновременно требует, чтобы переменные дочернего
`MetadataMap/@ValueTemplate` были определены в родительском
`IdentificationField/@ValueTemplate`.

Example 8.7 содержит родительский шаблон:

```xml
<IdentificationField ValueFormat="%6s%3i%2i" ValueTemplate="job doc sheet">
  <MetadataMap Name="JobID" ValueFormat="Job_%s" ValueTemplate="job"/>
  <MetadataMap Name="DocIndex" ValueFormat="%i%i" ValueTemplate="doc doc"/>
  <MetadataMap Name="SheetIndex" ValueFormat="%i%i" ValueTemplate="sheet sheet"/>
</IdentificationField>
```

Переменные `job`, `doc`, `sheet` удовлетворяют Table 8.46, но значения
`MetadataMap/@Name` (`JobID`, `DocIndex`, `SheetIndex`) отсутствуют в
родительском `@ValueTemplate`. Поэтому пример буквально нарушает SHALL Table
8.31. `schema.xsd` проверяет только структуру и это отношение выразить не может.
Release notes 2.1/2.2 не содержат разъяснения.

## Decision

1. Следовать нормативному prose Table 8.31 согласно приоритету ROADMAP §1.2.
   Корневой валидатор отклоняет каждый `IdentificationField/MetadataMap`, чьё
   `@Name` отсутствует в родительском `@ValueTemplate`.
2. Сохранить Example 8.7 как минимальный regression fixture против молчаливого
   ослабления правила: его первая пара `ValueTemplate="job"` / `Name="JobID"`
   образует негативный тест `METADATA-MAP-NAME-NOT-IN-PARENT-TEMPLATE`.
3. Позитивная conformance-фикстура повторяет обе стороны Examples 8.6/8.7, но
   для части 8.7 расширяет родительский шаблон до
   `job doc sheet JobID DocIndex SheetIndex`, а `@ValueFormat` — соответствующими
   шестью преобразованиями, чтобы выполнить явный SHALL и сохранить парность
   format/template. Это осознанная нормативная поправка, а не буквальная копия
   ошибочного примера.
4. Отдельного локального `MetadataMap.law` нет: допустимость зависит от вида
   родителя (`RunList` или `IdentificationField`) и проверяется в
   `TicketValidator` по ADR-0003.

## Alternatives

| Вариант | Оценка |
| --- | --- |
| A. Следовать Example 8.7 и не проверять Table 8.31 | Отклонено: пример ниже prose в иерархии источников и явный SHALL был бы потерян |
| B. Считать `@Name` не переменной и трактовать SHALL как необязательное пояснение | Отклонено: нормативное `SHALL` не допускает такого ослабления |
| C. Следовать Table 8.31, зафиксировать конфликт и адаптировать позитивную фикстуру (**решение**) | Сохраняет проверяемую норму и явно документирует отличие примера |

## Consequences

- Буквальный Example 8.7 не проходит доменную валидацию XJDF 2.2.
- Для конформного `IdentificationField` отправитель обязан включить как входные
  переменные дочерних шаблонов, так и все `MetadataMap/@Name` в родительский
  `@ValueTemplate`.
- XML/XSD-валидация сама по себе это расхождение не обнаруживает; regression
  закреплён доменным тестом.

## Normative references

- XJDF 2.2, §8.26 / Table 8.31 (`IdentificationField/@ValueTemplate`).
- XJDF 2.2, §8.29 / Table 8.46 (`MetadataMap`).
- XJDF 2.2, Example 8.7.
- ROADMAP §1.2.

## Migration impact

Срез аддитивен: `MetadataMap` до B2 отсутствовал. Пользователи, переносившие
Example 8.7 вне типизированной модели, должны добавить `MetadataMap/@Name` в
родительский `IdentificationField/@ValueTemplate` перед конструированием
конформного доменного значения.
