# ADR-0011 — Две Glue-энумерации: элемент `Glue` (Table 8.29) и «Allowed value is from: Glue» (Table A.24)

- **Статус:** принято (решение владельца от 2026-08-15: PR-15 = Crease + подготовка N-50)
- **Дата:** 2026-08-15
- **Задача:** M1.6-3 (Glue, PR-16); регистрация находки — M1.6-2 (PR-15)
- **Закрывает находки:** N-50
- **Связанные ADR:** ADR-0007 (закрытые enum vs открытые каталоги), ADR-0003 (форма локальных правил)

---

## Context

При подготовке вертикального среза `Glue` (Table 8.29) обнаружено, что спецификация
использует **два разных закрытых набора Glue-токенов**, а модель смешивает их в одном
enum `prim.GlueType` (3 значения).

**Table A.24 (Appendix A, §A.2.23 «Glue»)** — 3 значения:

| Value | Description |
|---|---|
| `ColdGlue` | — |
| `Hotmelt` | — |
| `PUR` | Polyurethane rubber. |

**Table 8.29 (`Glue/@GlueType`)** — 5 значений:

> `GlueType?` | enumeration | Glue type. Allowed values are: `ColdGlue` – Any type of glue that needs no heat treatment. `Hotmelt` – Hotmelt EVA (Ethylene-vinyl acetate). `Permanent` – Any glue that is designed not to be removed. `PUR` – Polyurethane. `Removable` – Any glue that is designed to be removed.

**`schema.xsd` (структурный оракул, §1.2) подтверждает обе стороны:**

- `simpleType EnumGlue` — ровно 3 значения (`ColdGlue`, `Hotmelt`, `PUR`); используется
  атрибутами «Allowed value is from: Glue» (`EdgeGlue`, `SpineGlue`);
- элемент `Glue` объявляет `@GlueType` с inline-ограничением из 5 значений
  (`ColdGlue`, `Hotmelt`, `Permanent`, `PUR`, `Removable`).

**Example 8.15** (нормативный пример) использует значение из 5-значного набора:

```xml
<Glue AreaGlue="true" GlueType="Removable"/>
```

**Внутренний конфликт источников:** Table A.24 (3 значения) против Table 8.29 + Example 8.15
(5 значений). По §1.2 приоритет — нормативный текст (Table 8.29 прямо перечисляет
«Allowed values are:») и примеры; XSD показывает, что это не «один неполный список»,
а **два различных набора** с разными потребителями.

**Текущий код:**

- `prim/Enums.scala`: `enum GlueType` — 3 значения (`ColdGlue`, `Hotmelt`, `PUR`);
- 7 полей используют его, из которых по Table 4.5/4.7/4.9 и XSD **три являются элементом** `Glue`:
  - `BindIn.glue: Option[GlueType]` (`intents/FoldingVariable.scala:119`, Table 4.5: `Glue?` — element);
  - `StickOn.glue: Option[GlueType]` (`intents/FoldingVariable.scala:143`, Table 4.7: `Glue?` — element);
  - `AdhesiveNote.glue: Option[GlueType]` (`intents/Binding.scala:111`, Table 4.9: `Glue?` — element);
- четыре поля — энумерации «Allowed value is from: Glue»: `EdgeGluing.edgeGlue`,
  `HardCoverBinding.spineGlue`, `SoftCoverBinding.spineGlue` (Table 4.10/4.11/4.18) — набор Table A.24;
- 5-значный набор `Glue/@GlueType` не смоделирован вовсе.

## Decision

1. **Элемент `Glue`** (Table 8.29) вводится в `model.elements` (глава 8, общий элемент);
   поле типа клея — `glueType: Option[GlueType]`, где `GlueType` — **новый** закрытый enum
   из 5 значений с wire-токенами Table 8.29 (`ColdGlue`, `Hotmelt`, `Permanent`, `PUR`,
   `Removable`). Имя enum совпадает с именем атрибута `@GlueType` — коллизии нет:
   прежний 3-значный enum переименовывается (см. п. 2).
2. **Существующий 3-значный enum `prim.GlueType` переименовывается в `prim.Glue`** —
   это спецификационное имя типа (Table A.24 «Glue»; XSD — `EnumGlue`). Коллизии с
   элементом `Glue` нет: типы живут в разных пакетах (`prim` vs `model.elements`),
   явный импорт элемента перекрывает wildcard-импорт `prim` (Scala 3 precedence).
   Расхождение «Scala-имя `Glue` = имя элемента `Glue`» вносится в Приложение C
   реестра сознательных отклонений.
3. **Поля-элементы** `BindIn.glue`, `StickOn.glue`, `AdhesiveNote.glue` меняют тип
   `Option[Glue]`(enum) → `Option[Glue]`(элемент) — breaking change, миграция в PR-16
   с полным списком call sites.
4. **Поля-энумерации** `EdgeGluing.edgeGlue`, `HardCoverBinding.spineGlue`,
   `SoftCoverBinding.spineGlue` остаются `Option[Glue]` (переименованный enum, набор Table A.24).
5. **Локальные SHALL-правила** (DomainRule, ADR-0003; PR-16):
   - `@GluingPattern` SHALL contain an even number of entries (Table 8.29);
   - `@MeltingTemperature` SHALL NOT be specified unless `@GlueType="Hotmelt"` or
     `@GlueType="PUR"` (Table 8.29);
   - IDREF `@GlueRef` собирается в `references` элемента.
6. **SPEC-COVERAGE и Приложение C** фиксируют разрешение внутреннего конфликта
   спецификации: Table A.24 (3) и Table 8.29 (5) — разные наборы, модель содержит оба.

## Alternatives

| Вариант | Оценка |
| --- | --- |
| A. Один enum `GlueType` (3 значения) для всего | Невыразимы `Permanent`/`Removable` — Example 8.15 не моделируется; смешение «элемент vs enum» сохраняется |
| B. Один enum на 5 значений для всех Glue-атрибутов | Ломает соответствие XSD `EnumGlue` и Table A.24: «Allowed value is from: Glue»-атрибуты получат значения вне XSD-допустимых |
| C. Два enum + элемент `Glue` (**решение**) | Точно отражает XSD и prose; цена — переименование одного enum и смена типов трёх полей (компилятор укажет все call sites) |

## Consequences

- **Положительные:** Example 8.15 моделируем; golden-множества EnumLaws разделяются
  (Table A.24 — 3 токена, Table 8.29 — 5 токенов); XSD-оракул согласуется с доменом.
- **Отрицательные:** breaking change: переименование `GlueType` → `Glue` и типы трёх
  полей; затрагиваются `intents/Binding.scala`, `intents/FoldingVariable.scala`,
  `laws/EnumLaws.scala`, генераторы/примеры при их обращении к enum.
- **Решение по открытому вопросу (реализация M1.6-3, PR-16):** 3-значный enum
  переименован в `prim.EnumGlue` (XSD-имя `simpleType EnumGlue`), а не в
  `prim.Glue`. Причина: `intents/Binding.scala` содержит как элементные поля
  (`AdhesiveNote.glue: Option[GlueElement]`), так и энумерационные
  (`EdgeGluing.edgeGlue: Option[EnumGlue]`) — в одном файле; явный импорт
  элемента `Glue` перекрывал бы wildcard `prim.*` для обоих случаев, что
  невозможно. Имя `EnumGlue` устраняет конфликт без квалификации.
- Срок пересмотра: закрыт.

## Normative references

- `reference/xjdf/Appendix A – Data Types and Values.md`, §A.2.23, Table A.24;
- `reference/xjdf/8 – Subelements.md`, Table 8.29 (включая SHALL-правила `@GluingPattern`
  и `@MeltingTemperature`), Example 8.15;
- `reference/xjdf/4 – Product Intent.md`, Tables 4.5 (`BindIn`), 4.7 (`StickOn`),
  4.9 (`AdhesiveNote`), 4.10 (`EdgeGluing`), 4.11 (`HardCoverBinding`), 4.18 (`SoftCoverBinding`);
- `reference/xjdf/schema.xsd`: `EnumGlue`, элемент `Glue` (inline `@GlueType`);
- ROADMAP §1.2 (приоритет источников истины), ADR-0007 (закрытые enum).

## Migration impact

Реализуется в PR-16 (M1.6-3) вместе с элементом `Glue`:

- `prim.GlueType` → `prim.EnumGlue` (переименование по XSD-имени; `EnumLaws` golden Table A.24 обновляется);
- `BindIn.glue`, `StickOn.glue`, `AdhesiveNote.glue`: `Option[GlueType]` → `Option[Glue]` (элемент);
- `EdgeGluing.edgeGlue`, `HardCoverBinding.spineGlue`, `SoftCoverBinding.spineGlue`: `Option[GlueType]` → `Option[EnumGlue]`;
- новый `prim.GlueType` (5 значений, Table 8.29) + golden;
- новый `prim.GluingTechnique` (3 значения, Table 8.29) + golden;
- полный список call sites: `intents/Binding.scala` (4 поля), `intents/FoldingVariable.scala` (2 поля), `laws/EnumLaws.scala` (3 golden + round-trip + duplicates).
