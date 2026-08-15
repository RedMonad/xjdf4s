# ADR-0008 — Масштабируемое представление `ResourcePayload`

- **Статус:** принято (направление); реализация сравнивается до массового M3
- **Дата:** 2026-08-15
- **Задача:** M3.1 (дедлайн по ROADMAP §6 — «до массового M3»)
- **Закрывает находку:** N-39
- **Связанные ADR:** ADR-0007 (открытые/закрытые типы), ADR-0010 (кодеки)

---

## Context

`resources.AllResources` уже имеет максимальную betweenness (161.6) при 12
реализованных ресурсах; `model.Resource` — 135.1. Глава 6 содержит около
полутора сотен таблиц ресурсов. Единый постоянно растущий центральный enum
усилит bottleneck линейно.

## Decision

До массового расширения (M3) сравнить три варианта:

1. центральный генерируемый enum;
2. иерархия payload по семействам процессов;
3. registry/typeclass dispatch.

Выбранный дизайн обязан сохранять:

- исчерпывающий стандартный каталог;
- escape hatch для foreign extensions;
- тотальные `elementName`, `references`, validation и codec dispatch;
- отсутствие unchecked casts;
- возможность добавить ресурс одной вертикальной правкой;
- контролируемую centrality `AllResources`/`Resource`.

Рост betweenness этих узлов после M3 не принимается без обновления этого ADR.

## Alternatives

| Вариант | Суть | Оценка |
| --- | --- | --- |
| **1. Центральный генерируемый enum** | Один GADT на ~150 ресурсов | Прост в dispatch, но linear centrality; генератор — scaffolding (риск R10) |
| **2. Иерархия по семействам** | `Payload` по группам процессов (cutting, binding, …) | Умеренная centrality; нужен корневой union |
| **3. Registry/typeclass** | Реестр payload-типов + typeclass dispatch | Минимальная centrality; сложнее тотальность |

**Промежуточное состояние M1.** Текущий `ResourcePayload` (12 ресурсов +
`Foreign`) сохраняется как есть: он закрыт, тотален по `elementName` и не
содержит unchecked casts. Инвентаризация и выбор — задача M3.1, до массового
наполнения.

## Consequences

- M3.1 начинается с инвентаризации и tooling, а не с добавления case-ов.
- `docs/SPEC-COVERAGE.md` фиксирует статус каждого payload; чекер
  (`scripts/check-spec-coverage.sh`) ловит payload без строки покрытия.
- Генератор (если выбран вариант 1) — только scaffolding и отчёт; prose и
  JSON Exceptions проверяются вручную (риск R10).

## Normative references

- ROADMAP §6 (ADR-0008), §3.5 (hotspots), §13 (R9, R10), §8 (M3)
- `resources/AllResources.scala`, `model/Resource.scala`
- `docs/SPEC-COVERAGE.md`

## Migration impact

Нет до M3: публичный `ResourcePayload` не меняется. При выборе дизайна M3.1
breaking-изменения получат migration note и полный список call sites.

**Срок пересмотра:** M3.1 — фиксация выбранного варианта и бюджетов
centrality в этом ADR.
