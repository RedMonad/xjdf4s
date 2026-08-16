# ADR-0015 — `FileSpec/@NPage`: Table 8.22 и release notes против `schema.xsd`

- **Статус:** принято (N-51/N-56, решение по приоритету источников ROADMAP §1.2)
- **Дата:** 2026-08-16
- **Задача:** N-51 (`FileSpec.law` + `NetworkHeader*`)
- **Закрывает находки:** N-56
- **Связанные ADR:** ADR-0003 (локальные и контекстные правила), ADR-0010 (нормализация кодеков)

---

## Context

Предстартовая сверка N-51 охватывает весь структурный контракт `FileSpec`
§8.19 / Table 8.22, поскольку новый `NetworkHeader*` добавляется в тот же
элемент. Нормативный текст XJDF 2.2 и release notes добавляют атрибут
`FileSpec/@NPage`, но структурный oracle его не содержит.

**Table 8.22**:

> `@NPage?` *(New in XJDF 2.2)* | integer | `@NPage` SHALL specify the total number of reader Pages in the file that is referenced by `@URL`. If FileSpec is a descendant of a RunList, values of negative indices in `RunList/@Pages` SHALL then be calculated using `FileSpec/@NPage` as a count of the total number of pages in the referenced file.

**Appendix H, release notes XJDF 2.2**:

> `@NPage` | New | Added attribute to FileSpec. | Table 8.22 FileSpec Element.

**`reference/xjdf/schema.xsd`, element `FileSpec`** перечисляет `CheckSum`,
`Encoding`, `FileFormat`, `FileSize`, `FileTemplate`, `MimeType`,
`OverwritePolicy`, `Password`, `ResourceUsage`, `SearchDepth`, `UID`, `URL` и
`UserFileName`, но не `NPage`.

При этом в схеме существует одноимённый атрибут у других типов, включая
`RunList`; следовательно, отсутствие в `FileSpec` нельзя объяснить тем, что XSD
вообще не знает имя или тип. Модель до N-51 уже содержит
`FileSpec.nPage: Option[Long]`, то есть фактически следует prose, но это решение
не было явно зафиксировано.

### Минимальная конфликтная фикстура

```xml
<FileSpec xmlns="http://www.CIP4.org/JDFSchema_2_0"
          URL="file:///input.pdf"
          NPage="12"/>
```

Фикстура соответствует Table 8.22 и release notes XJDF 2.2: `@NPage`
задаёт число reader Pages в файле по `@URL`. Текущий `schema.xsd` отклоняет её,
поскольку `NPage` отсутствует в списке атрибутов глобального элемента
`FileSpec`. Это минимальный пример расхождения: один элемент, нормативный
атрибут и связанный с ним `@URL`, без других необязательных данных.

## Decision

1. **Сохранить `FileSpec.nPage: Option[Long]`.** Приоритет имеет Table 8.22,
   дополнительно подтверждённая release notes XJDF 2.2 (ROADMAP §1.2).
2. **Не удалять поле ради прохождения XSD.** В M2 XSD остаётся тест-оракулом,
   но схема не должна отвергать нормативную возможность доменной модели.
   Политика schema-validation для такого документа должна явно учитывать это
   известное расхождение.
3. **Закрепить обе стороны машинным тестом.** `FileSpecLaws` проверяет наличие
   строки `@NPage` в Table 8.22 и release notes, отсутствие атрибута в XSD-срезе
   `FileSpec` и представимость значения в доменной модели.
4. **Не смешивать с локационными законами N-51.** Связь `@NPage` с `@URL`
   описывает смысл счётчика страниц; N-51 не вводит вычисление
   `RunList/@Pages` и не расширяет локальное взаимное исключение location-групп.
   Семантика отрицательных индексов остаётся задачей кодеков/selection-логики M2.

## Alternatives

| Вариант | Оценка |
| --- | --- |
| A. Удалить `FileSpec.nPage`, следуя `schema.xsd` | Отклонено: нарушает Table 8.22 и явно зафиксированное изменение XJDF 2.2 |
| B. Сохранить поле без ADR | Отклонено: ROADMAP §1.2 запрещает молчаливый выбор при расхождении prose/XSD |
| C. Сохранить поле и зафиксировать расхождение ADR + oracle-тестом (**решение**) | Следует нормативному приоритету и оставляет XSD воспроизводимым oracle |
| D. Добавить локальный запрет `@NPage` без `@URL` в N-51 | Отклонено: таблица определяет смысл значения через файл, referenced by `@URL`, но не формулирует отдельное `SHALL NOT`; ужесточение без явной нормы нарушило бы ADR-0006 |

## Consequences

- **Положительные:** модель принимает новый атрибут XJDF 2.2; выбор prose над
  XSD больше не скрыт; изменение спецификации или схемы будет замечено тестом.
- **Отрицательные:** документ с `FileSpec/@NPage` не проходит текущий
  `schema.xsd` без известного исключения. XML-кодек M2 обязан учитывать это в
  отчёте schema-conformance, а не удалять поле.
- **Совместимость:** breaking change отсутствует — поле уже существовало;
  решение документирует текущий API.
- **Срок пересмотра:** публикация CIP4 исправленной схемы либо официальный
  erratum, удаляющий `@NPage` из Table 8.22/release notes.

## Normative references

- `reference/xjdf/8 – Subelements.md`, §8.19, Table 8.22;
- `reference/xjdf/Appendix H – Release Notes.md`, XJDF 2.2, `@NPage`;
- `reference/xjdf/schema.xsd`, element `FileSpec`;
- ROADMAP §1.2, §9.1, §12, §15; ADR-0010.

## Migration impact

Breaking change отсутствует. Потребители продолжают использовать
`FileSpec.nPage: Option[Long]`. Кодек M2 не должен ориентироваться только на
набор атрибутов XSD: `@NPage` кодируется и декодируется по Table 8.22, а
расхождение отражается в conformance report как известное ADR-0015.
