# 📊 Architecture Graph Analysis — Full Report

> **Сгенерировано:** 2026-08-17T18:15:33.682037
> **Узлов (файлов):** 68 | **Рёбер (зависимостей):** 528 | **Модулей:** 5 | **Циклов:** 2

## 🧑‍🔧 Ручная ревизия (добавлена после генерации)

**Цикл 1 (`DocOp ↔ DocInterpreters`) — ложное срабатывание, устранено в коде.**
Единственные рёбра `DocOp → DocInterpreters` порождались scaladoc-ссылками `[[DocInterpreters]]`
в `DocOp.scala`; кодовых ссылок (импортов/использований типов) в этом направлении нет — реальная
зависимость однонаправленная (`DocInterpreters → DocOp`, импорт `DocOp`/`DocDsl`). Ссылки заменены
обычным текстом; после перегенерации отчёта цикл исчезнет.

**Цикл 2 (43 файла) — файловый уровень внутри одного модуля `xjdf4s-model`; нарушением не является.**
Все 43 файла принадлежат одному модулю, а взаимные ссылки типов внутри единого доменного модуля —
нормальная практика Scala. На уровне модулей граф ацикличен (проверено по импортам):
`core ← model ← messaging ← protocol`, `dsl → model`; `core` не импортирует xjdf4s-модули, `model`
не импортирует messaging/dsl/protocol. Рекомендация анализатора «вынести интерфейс (trait) в общий
модуль» неприменима: все файлы цикла уже находятся в одном модуле. Принцип ацикличности зависимостей
(ADP) соблюдён на уровне модулей.

**«God Object» `model.Resource` (Fan-Out 39) и «узкие места» — артефакт хаба, а не дефект.**
`Resource.scala` содержит union-тип `StandardSpecificResource` — единственную точку, перечисляющую
все 102 ресурса подстановочной группы XSD. Высокая betweenness-центральность `Resource`/`ResourceSet` —
следствие их роли корня дерева документа. Это намеренный дизайн: разбиение union-хаба ухудшит
эргономику API без выигрыша в модульности.

**«Фундамент» (Fan-In > 50) — верный и ожидаемый сигнал.** Это `core.Extension` (57) и
`core.Primitives` (55): стабильный словарь скалярных типов и расширений, от которого зависит всё
остальное. Их стабильность — требование, а не проблема; изменения в этих файлах должны проходить
проверку совместимости (MiMa, этап 08 roadmap).

---

## 🎯 Executive Summary

### Ключевые находки

1. **Циклические зависимости:** Обнаружено **2** циклов. Это нарушает модульность и требует вмешательства.
2. **Узкие места (Bottlenecks):** 5 файлов имеют аномально высокую Betweenness Centrality. Главный: `xjdf4s.model.Resource` (score: 1977.8671957471497).
3. **God Objects:** 1 файлов имеют Fan-Out > 25. Худший: `xjdf4s.model.Resource` (Fan-Out: 39).
4. **Фундамент:** 2 файлов имеют Fan-In > 50. Они должны быть максимально стабильными.
5. **Изолированные файлы:** 0 файлов (0.0%) не имеют связей. Кандидаты на удаление.
6. **Средний Fan-In:** 7.8 | **Средний Fan-Out:** 7.8

---

## 📈 Распределение метрик


### Fan-In (входящие зависимости)

| Квантиль | Значение |
|---|---|
| Min | 0 |
| P25 | 1 |
| Median (P50) | 3 |
| P75 | 7 |
| P90 | 25 |
| P95 | 30 |
| Max | 57 |
| Mean | 7.8 |

### Fan-In: Гистограмма

| Диапазон | Количество файлов | Процент |
|---|---|---|
| 0–0 | 3 | 4.4% |
| 1–5 | 40 | 58.8% |
| 6–10 | 13 | 19.1% |
| 11–20 | 4 | 5.9% |
| 21–50 | 6 | 8.8% |
| 51–100 | 2 | 2.9% |
| 101+ | 0 | 0.0% |

### Fan-Out (исходящие зависимости)

| Квантиль | Значение |
|---|---|
| Min | 0 |
| P25 | 5 |
| Median (P50) | 8 |
| P75 | 9 |
| P90 | 12 |
| P95 | 15 |
| Max | 39 |
| Mean | 7.8 |

### Betweenness Centrality

- Файлов с Betweenness > 0: **60** из 68 (88.2%)
- Медиана (из ненулевых): 8.157916861399833
- P90 (из ненулевых): 52.70644257703083
- Max: 1977.87

---

## 📦 Анализ по модулям

| Модуль | Файлов | Ср. Fan-In | Ср. Fan-Out | Ср. Betweenness | Ср. Instability | Внутр. связей | Внешн. связей |
|---|---|---|---|---|---|---|---|
| `core` | 6 | 32.8 | 0.8 | 4.4 | 0.04 | 5 | 0 |
| `dsl` | 2 | 1.0 | 8.5 | 0.8 | 0.89 | 2 | 15 |
| `messaging` | 9 | 2.2 | 6.4 | 13.3 | 0.74 | 18 | 40 |
| `model` | 50 | 6.2 | 8.9 | 75.2 | 0.66 | 282 | 162 |
| `protocol` | 1 | 0.0 | 4.0 | 0.0 | 1.00 | 0 | 4 |

---

## 🔗 Матрица межмодульных зависимостей

Строки = откуда, столбцы = куда. Число = количество файловых зависимостей.

| from \ to | `core` | `dsl` | `messaging` | `model` | `protocol` |
|---|---|---|---|---|---|
| **core** | **5** | · | · | · | · |
| **dsl** | **8** | **2** | · | **7** | · |
| **messaging** | **21** | · | **18** | **19** | · |
| **model** | **162** | · | · | **282** | · |
| **protocol** | **1** | · | **2** | **1** | · |

---

## 🔥 Risk Ranking (все файлы)

Risk Score = (Fan-Out × 0.3 + Instability × 50 × 0.3 + Betweenness × 0.4). Чем выше — тем опаснее.

| # | File | Module | Fan-In | Fan-Out | Betweenness | Instability | **Risk** |
|---|---|---|---|---|---|---|---|
| 1 | `xjdf4s.model.Resource` | model | 39 | 39 | 1977.9 | 0.50 | 🔴 **810.3** |
| 2 | `xjdf4s.model.Subelements` | model | 27 | 7 | 570.1 | 0.21 | 🔴 **233.2** |
| 3 | `xjdf4s.model.ContentAndShapeIntents` | model | 14 | 8 | 343.6 | 0.36 | 🔴 **145.3** |
| 4 | `xjdf4s.model.Product` | model | 9 | 10 | 222.6 | 0.53 | 🟡 **99.9** |
| 5 | `xjdf4s.model.resources.MediaAndColor` | model | 6 | 15 | 92.3 | 0.71 | 🟡 **52.2** |
| 6 | `xjdf4s.model.Partition` | model | 30 | 5 | 96.9 | 0.14 | 🟡 **42.4** |
| 7 | `xjdf4s.model.XJDF` | model | 3 | 9 | 52.7 | 0.75 | 🟡 **35.0** |
| 8 | `xjdf4s.model.resources.AdditionalResources` | model | 3 | 14 | 34.9 | 0.82 | 🟡 **30.5** |
| 9 | `xjdf4s.model.resources.MarksAndStacking` | model | 3 | 15 | 31.8 | 0.83 | 🟢 **29.7** |
| 10 | `xjdf4s.messaging.ControlMessages` | messaging | 2 | 7 | 34.2 | 0.78 | 🟢 **27.5** |
| 11 | `xjdf4s.messaging.Message` | messaging | 7 | 4 | 49.3 | 0.36 | 🟢 **26.4** |
| 12 | `xjdf4s.messaging.StatusNotificationResourceMessages` | messaging | 1 | 10 | 23.7 | 0.91 | 🟢 **26.1** |
| 13 | `xjdf4s.model.resources.PostpressResources` | model | 1 | 9 | 23.0 | 0.90 | 🟢 **25.4** |
| 14 | `xjdf4s.model.resources.Device` | model | 6 | 9 | 29.6 | 0.60 | 🟢 **23.5** |
| 15 | `xjdf4s.model.resources.SheetOptimizing` | model | 2 | 12 | 16.9 | 0.86 | 🟢 **23.2** |
| 16 | `xjdf4s.model.resources.SimpleResources` | model | 4 | 8 | 24.7 | 0.67 | 🟢 **22.3** |
| 17 | `xjdf4s.model.resources.ProcessResources` | model | 7 | 13 | 21.1 | 0.65 | 🟢 **22.1** |
| 18 | `xjdf4s.model.resources.MorePostpressResources` | model | 1 | 7 | 16.8 | 0.88 | 🟢 **22.0** |
| 19 | `xjdf4s.model.resources.RemainingPostpressResources` | model | 2 | 9 | 17.4 | 0.82 | 🟢 **21.9** |
| 20 | `xjdf4s.model.resources.ShapeDefinitionResources` | model | 2 | 10 | 15.7 | 0.83 | 🟢 **21.8** |
| 21 | `xjdf4s.model.resources.Content` | model | 2 | 15 | 8.2 | 0.88 | 🟢 **21.0** |
| 22 | `xjdf4s.model.resources.BinderySignature` | model | 2 | 9 | 12.1 | 0.82 | 🟢 **19.8** |
| 23 | `xjdf4s.model.resources.Layout` | model | 1 | 16 | 1.4 | 0.94 | 🟢 **19.5** |
| 24 | `xjdf4s.model.FinishingIntents` | model | 7 | 9 | 20.4 | 0.56 | 🟢 **19.3** |
| 25 | `xjdf4s.model.resources.FoundationalResources` | model | 6 | 9 | 18.3 | 0.60 | 🟢 **19.0** |
| 26 | `xjdf4s.model.resources.QualityControl` | model | 1 | 12 | 1.4 | 0.92 | 🟢 **18.0** |
| 27 | `xjdf4s.model.resources.FoldingResources` | model | 2 | 9 | 7.4 | 0.82 | 🟢 **17.9** |
| 28 | `xjdf4s.model.resources.RunList` | model | 3 | 9 | 9.9 | 0.75 | 🟢 **17.9** |
| 29 | `xjdf4s.messaging.KnownMessages` | messaging | 1 | 8 | 4.6 | 0.89 | 🟢 **17.6** |
| 30 | `xjdf4s.dsl.DocInterpreters` | dsl | 1 | 10 | 1.7 | 0.91 | 🟢 **17.3** |
| 31 | `xjdf4s.model.resources.DieLayout` | model | 1 | 10 | 1.4 | 0.91 | 🟢 **17.2** |
| 32 | `xjdf4s.model.resources.DeliveryAndPreflightResources` | model | 1 | 8 | 3.3 | 0.89 | 🟢 **17.0** |
| 33 | `xjdf4s.model.DocumentValidation` | model | 0 | 6 | 0.0 | 1.00 | 🟢 **16.8** |
| 34 | `xjdf4s.messaging.QueueEntryMessages` | messaging | 1 | 8 | 2.5 | 0.89 | 🟢 **16.7** |
| 35 | `xjdf4s.messaging.StandardMessages` | messaging | 0 | 5 | 0.0 | 1.00 | 🟢 **16.5** |
| 36 | `xjdf4s.model.resources.PdlCreation` | model | 1 | 8 | 1.4 | 0.89 | 🟢 **16.3** |
| 37 | `xjdf4s.Protocol` | protocol | 0 | 4 | 0.0 | 1.00 | 🟢 **16.2** |
| 38 | `xjdf4s.messaging.XJMF` | messaging | 1 | 7 | 2.2 | 0.88 | 🟢 **16.1** |
| 39 | `xjdf4s.model.resources.Interpreting` | model | 4 | 10 | 5.9 | 0.71 | 🟢 **16.1** |
| 40 | `xjdf4s.model.resources.MissingSchemaResources` | model | 1 | 8 | 0.3 | 0.89 | 🟢 **15.9** |
| 41 | `xjdf4s.model.resources.SmallProductionResources` | model | 1 | 7 | 1.4 | 0.88 | 🟢 **15.8** |
| 42 | `xjdf4s.messaging.GangAndQueueStatusMessages` | messaging | 2 | 8 | 3.3 | 0.80 | 🟢 **15.7** |
| 43 | `xjdf4s.model.BindingIntent` | model | 4 | 8 | 8.2 | 0.67 | 🟢 **15.7** |
| 44 | `xjdf4s.model.resources.Rendering` | model | 2 | 9 | 1.7 | 0.82 | 🟢 **15.6** |
| 45 | `xjdf4s.model.SimpleIntents` | model | 11 | 7 | 18.5 | 0.39 | 🟢 **15.3** |
| 46 | `xjdf4s.dsl.DocOp` | dsl | 1 | 7 | 0.0 | 0.88 | 🟢 **15.2** |
| 47 | `xjdf4s.model.AssemblingIntent` | model | 7 | 7 | 13.3 | 0.50 | 🟢 **14.9** |
| 48 | `xjdf4s.model.resources.GeneralAndPressResources` | model | 2 | 8 | 1.0 | 0.80 | 🟢 **14.8** |
| 49 | `xjdf4s.model.resources.ColorSpaceConversion` | model | 2 | 7 | 1.7 | 0.78 | 🟢 **14.4** |
| 50 | `xjdf4s.model.MediaIntent` | model | 5 | 7 | 8.7 | 0.58 | 🟢 **14.3** |
| 51 | `xjdf4s.model.resources.Contact` | model | 1 | 5 | 0.3 | 0.83 | 🟢 **14.1** |
| 52 | `xjdf4s.model.resources.ColorantControl` | model | 1 | 5 | 0.3 | 0.83 | 🟢 **14.1** |
| 53 | `xjdf4s.model.resources.DieLayoutProduction` | model | 5 | 8 | 4.3 | 0.62 | 🟢 **13.3** |
| 54 | `xjdf4s.model.resources.ImageCompression` | model | 2 | 6 | 0.4 | 0.75 | 🟢 **13.2** |
| 55 | `xjdf4s.model.resources.PrepressResources` | model | 6 | 8 | 4.4 | 0.57 | 🟢 **12.7** |
| 56 | `xjdf4s.model.resources.Identification` | model | 8 | 7 | 6.7 | 0.47 | 🟢 **11.8** |
| 57 | `xjdf4s.model.Audit` | model | 3 | 3 | 7.5 | 0.50 | 🟢 **11.4** |
| 58 | `xjdf4s.core.Extension` | core | 57 | 1 | 13.0 | 0.02 | 🟢 **5.8** |
| 59 | `xjdf4s.core.Primitives` | core | 55 | 1 | 10.0 | 0.02 | 🟢 **4.6** |
| 60 | `xjdf4s.model.Header` | model | 8 | 2 | 0.5 | 0.20 | 🟢 **3.8** |
| 61 | `xjdf4s.core.Common` | core | 11 | 2 | 0.5 | 0.15 | 🟢 **3.1** |
| 62 | `xjdf4s.messaging.MessageNames` | messaging | 5 | 1 | 0.0 | 0.17 | 🟢 **2.8** |
| 63 | `xjdf4s.model.TypedValues` | model | 6 | 1 | 0.7 | 0.14 | 🟢 **2.7** |
| 64 | `xjdf4s.model.ColorValues` | model | 6 | 1 | 0.7 | 0.14 | 🟢 **2.7** |
| 65 | `xjdf4s.core.Cardinality` | core | 25 | 1 | 3.1 | 0.04 | 🟢 **2.1** |
| 66 | `xjdf4s.model.XjdfNames` | model | 38 | 1 | 0.0 | 0.03 | 🟢 **0.7** |
| 67 | `xjdf4s.core.Values` | core | 29 | 0 | 0.0 | 0.00 | 🟢 **0.0** |
| 68 | `xjdf4s.core.Validation` | core | 20 | 0 | 0.0 | 0.00 | 🟢 **0.0** |

---

## 🧱 Полный рейтинг Fan-In

| # | File | Fan-In | Module | Instability |
|---|---|---|---|---|
| 1 | `xjdf4s.core.Extension` | 57 | core | 0.02 |
| 2 | `xjdf4s.core.Primitives` | 55 | core | 0.02 |
| 3 | `xjdf4s.model.Resource` | 39 | model | 0.50 |
| 4 | `xjdf4s.model.XjdfNames` | 38 | model | 0.03 |
| 5 | `xjdf4s.model.Partition` | 30 | model | 0.14 |
| 6 | `xjdf4s.core.Values` | 29 | core | 0.00 |
| 7 | `xjdf4s.model.Subelements` | 27 | model | 0.21 |
| 8 | `xjdf4s.core.Cardinality` | 25 | core | 0.04 |
| 9 | `xjdf4s.core.Validation` | 20 | core | 0.00 |
| 10 | `xjdf4s.model.ContentAndShapeIntents` | 14 | model | 0.36 |
| 11 | `xjdf4s.model.SimpleIntents` | 11 | model | 0.39 |
| 12 | `xjdf4s.core.Common` | 11 | core | 0.15 |
| 13 | `xjdf4s.model.Product` | 9 | model | 0.53 |
| 14 | `xjdf4s.model.resources.Identification` | 8 | model | 0.47 |
| 15 | `xjdf4s.model.Header` | 8 | model | 0.20 |
| 16 | `xjdf4s.model.FinishingIntents` | 7 | model | 0.56 |
| 17 | `xjdf4s.model.resources.ProcessResources` | 7 | model | 0.65 |
| 18 | `xjdf4s.model.AssemblingIntent` | 7 | model | 0.50 |
| 19 | `xjdf4s.messaging.Message` | 7 | messaging | 0.36 |
| 20 | `xjdf4s.model.resources.FoundationalResources` | 6 | model | 0.60 |
| 21 | `xjdf4s.model.resources.Device` | 6 | model | 0.60 |
| 22 | `xjdf4s.model.TypedValues` | 6 | model | 0.14 |
| 23 | `xjdf4s.model.ColorValues` | 6 | model | 0.14 |
| 24 | `xjdf4s.model.resources.PrepressResources` | 6 | model | 0.57 |
| 25 | `xjdf4s.model.resources.MediaAndColor` | 6 | model | 0.71 |
| 26 | `xjdf4s.model.MediaIntent` | 5 | model | 0.58 |
| 27 | `xjdf4s.model.resources.DieLayoutProduction` | 5 | model | 0.62 |
| 28 | `xjdf4s.messaging.MessageNames` | 5 | messaging | 0.17 |
| 29 | `xjdf4s.model.BindingIntent` | 4 | model | 0.67 |
| 30 | `xjdf4s.model.resources.SimpleResources` | 4 | model | 0.67 |
| 31 | `xjdf4s.model.resources.Interpreting` | 4 | model | 0.71 |
| 32 | `xjdf4s.model.Audit` | 3 | model | 0.50 |
| 33 | `xjdf4s.model.resources.MarksAndStacking` | 3 | model | 0.83 |
| 34 | `xjdf4s.model.XJDF` | 3 | model | 0.75 |
| 35 | `xjdf4s.model.resources.AdditionalResources` | 3 | model | 0.82 |
| 36 | `xjdf4s.model.resources.RunList` | 3 | model | 0.75 |
| 37 | `xjdf4s.model.resources.BinderySignature` | 2 | model | 0.82 |
| 38 | `xjdf4s.model.resources.SheetOptimizing` | 2 | model | 0.86 |
| 39 | `xjdf4s.model.resources.Content` | 2 | model | 0.88 |
| 40 | `xjdf4s.model.resources.FoldingResources` | 2 | model | 0.82 |
| 41 | `xjdf4s.messaging.ControlMessages` | 2 | messaging | 0.78 |
| 42 | `xjdf4s.model.resources.GeneralAndPressResources` | 2 | model | 0.80 |
| 43 | `xjdf4s.model.resources.ImageCompression` | 2 | model | 0.75 |
| 44 | `xjdf4s.model.resources.RemainingPostpressResources` | 2 | model | 0.82 |
| 45 | `xjdf4s.model.resources.ColorSpaceConversion` | 2 | model | 0.78 |
| 46 | `xjdf4s.model.resources.Rendering` | 2 | model | 0.82 |
| 47 | `xjdf4s.model.resources.ShapeDefinitionResources` | 2 | model | 0.83 |
| 48 | `xjdf4s.messaging.GangAndQueueStatusMessages` | 2 | messaging | 0.80 |
| 49 | `xjdf4s.model.resources.PdlCreation` | 1 | model | 0.89 |
| 50 | `xjdf4s.messaging.StatusNotificationResourceMessages` | 1 | messaging | 0.91 |
| 51 | `xjdf4s.model.resources.Contact` | 1 | model | 0.83 |
| 52 | `xjdf4s.model.resources.PostpressResources` | 1 | model | 0.90 |
| 53 | `xjdf4s.dsl.DocInterpreters` | 1 | dsl | 0.91 |
| 54 | `xjdf4s.dsl.DocOp` | 1 | dsl | 0.88 |
| 55 | `xjdf4s.messaging.QueueEntryMessages` | 1 | messaging | 0.89 |
| 56 | `xjdf4s.model.resources.Layout` | 1 | model | 0.94 |
| 57 | `xjdf4s.model.resources.DieLayout` | 1 | model | 0.91 |
| 58 | `xjdf4s.messaging.KnownMessages` | 1 | messaging | 0.89 |
| 59 | `xjdf4s.model.resources.ColorantControl` | 1 | model | 0.83 |
| 60 | `xjdf4s.messaging.XJMF` | 1 | messaging | 0.88 |
| 61 | `xjdf4s.model.resources.MorePostpressResources` | 1 | model | 0.88 |
| 62 | `xjdf4s.model.resources.DeliveryAndPreflightResources` | 1 | model | 0.89 |
| 63 | `xjdf4s.model.resources.SmallProductionResources` | 1 | model | 0.88 |
| 64 | `xjdf4s.model.resources.QualityControl` | 1 | model | 0.92 |
| 65 | `xjdf4s.model.resources.MissingSchemaResources` | 1 | model | 0.89 |
| 66 | `xjdf4s.Protocol` | 0 | protocol | 1.00 |
| 67 | `xjdf4s.messaging.StandardMessages` | 0 | messaging | 1.00 |
| 68 | `xjdf4s.model.DocumentValidation` | 0 | model | 1.00 |

---

## 🕸️ Полный рейтинг Fan-Out

| # | File | Fan-Out | Module | Instability |
|---|---|---|---|---|
| 1 | `xjdf4s.model.Resource` | 39 | model | 0.50 |
| 2 | `xjdf4s.model.resources.Layout` | 16 | model | 0.94 |
| 3 | `xjdf4s.model.resources.MarksAndStacking` | 15 | model | 0.83 |
| 4 | `xjdf4s.model.resources.Content` | 15 | model | 0.88 |
| 5 | `xjdf4s.model.resources.MediaAndColor` | 15 | model | 0.71 |
| 6 | `xjdf4s.model.resources.AdditionalResources` | 14 | model | 0.82 |
| 7 | `xjdf4s.model.resources.ProcessResources` | 13 | model | 0.65 |
| 8 | `xjdf4s.model.resources.SheetOptimizing` | 12 | model | 0.86 |
| 9 | `xjdf4s.model.resources.QualityControl` | 12 | model | 0.92 |
| 10 | `xjdf4s.messaging.StatusNotificationResourceMessages` | 10 | messaging | 0.91 |
| 11 | `xjdf4s.dsl.DocInterpreters` | 10 | dsl | 0.91 |
| 12 | `xjdf4s.model.Product` | 10 | model | 0.53 |
| 13 | `xjdf4s.model.resources.Interpreting` | 10 | model | 0.71 |
| 14 | `xjdf4s.model.resources.DieLayout` | 10 | model | 0.91 |
| 15 | `xjdf4s.model.resources.ShapeDefinitionResources` | 10 | model | 0.83 |
| 16 | `xjdf4s.model.resources.FoundationalResources` | 9 | model | 0.60 |
| 17 | `xjdf4s.model.resources.Device` | 9 | model | 0.60 |
| 18 | `xjdf4s.model.XJDF` | 9 | model | 0.75 |
| 19 | `xjdf4s.model.resources.BinderySignature` | 9 | model | 0.82 |
| 20 | `xjdf4s.model.resources.PostpressResources` | 9 | model | 0.90 |
| 21 | `xjdf4s.model.resources.FoldingResources` | 9 | model | 0.82 |
| 22 | `xjdf4s.model.FinishingIntents` | 9 | model | 0.56 |
| 23 | `xjdf4s.model.resources.RunList` | 9 | model | 0.75 |
| 24 | `xjdf4s.model.resources.RemainingPostpressResources` | 9 | model | 0.82 |
| 25 | `xjdf4s.model.resources.Rendering` | 9 | model | 0.82 |
| 26 | `xjdf4s.model.resources.PdlCreation` | 8 | model | 0.89 |
| 27 | `xjdf4s.model.BindingIntent` | 8 | model | 0.67 |
| 28 | `xjdf4s.model.ContentAndShapeIntents` | 8 | model | 0.36 |
| 29 | `xjdf4s.model.resources.SimpleResources` | 8 | model | 0.67 |
| 30 | `xjdf4s.model.resources.DieLayoutProduction` | 8 | model | 0.62 |
| 31 | `xjdf4s.model.resources.GeneralAndPressResources` | 8 | model | 0.80 |
| 32 | `xjdf4s.messaging.QueueEntryMessages` | 8 | messaging | 0.89 |
| 33 | `xjdf4s.messaging.KnownMessages` | 8 | messaging | 0.89 |
| 34 | `xjdf4s.model.resources.PrepressResources` | 8 | model | 0.57 |
| 35 | `xjdf4s.model.resources.DeliveryAndPreflightResources` | 8 | model | 0.89 |
| 36 | `xjdf4s.messaging.GangAndQueueStatusMessages` | 8 | messaging | 0.80 |
| 37 | `xjdf4s.model.resources.MissingSchemaResources` | 8 | model | 0.89 |
| 38 | `xjdf4s.model.SimpleIntents` | 7 | model | 0.39 |
| 39 | `xjdf4s.model.resources.Identification` | 7 | model | 0.47 |
| 40 | `xjdf4s.model.MediaIntent` | 7 | model | 0.58 |
| 41 | `xjdf4s.model.Subelements` | 7 | model | 0.21 |
| 42 | `xjdf4s.messaging.ControlMessages` | 7 | messaging | 0.78 |
| 43 | `xjdf4s.dsl.DocOp` | 7 | dsl | 0.88 |
| 44 | `xjdf4s.messaging.XJMF` | 7 | messaging | 0.88 |
| 45 | `xjdf4s.model.resources.MorePostpressResources` | 7 | model | 0.88 |
| 46 | `xjdf4s.model.AssemblingIntent` | 7 | model | 0.50 |
| 47 | `xjdf4s.model.resources.ColorSpaceConversion` | 7 | model | 0.78 |
| 48 | `xjdf4s.model.resources.SmallProductionResources` | 7 | model | 0.88 |
| 49 | `xjdf4s.model.resources.ImageCompression` | 6 | model | 0.75 |
| 50 | `xjdf4s.model.DocumentValidation` | 6 | model | 1.00 |
| 51 | `xjdf4s.model.Partition` | 5 | model | 0.14 |
| 52 | `xjdf4s.model.resources.Contact` | 5 | model | 0.83 |
| 53 | `xjdf4s.messaging.StandardMessages` | 5 | messaging | 1.00 |
| 54 | `xjdf4s.model.resources.ColorantControl` | 5 | model | 0.83 |
| 55 | `xjdf4s.Protocol` | 4 | protocol | 1.00 |
| 56 | `xjdf4s.messaging.Message` | 4 | messaging | 0.36 |
| 57 | `xjdf4s.model.Audit` | 3 | model | 0.50 |
| 58 | `xjdf4s.core.Common` | 2 | core | 0.15 |
| 59 | `xjdf4s.model.Header` | 2 | model | 0.20 |
| 60 | `xjdf4s.core.Extension` | 1 | core | 0.02 |
| 61 | `xjdf4s.core.Cardinality` | 1 | core | 0.04 |
| 62 | `xjdf4s.model.TypedValues` | 1 | model | 0.14 |
| 63 | `xjdf4s.model.XjdfNames` | 1 | model | 0.03 |
| 64 | `xjdf4s.model.ColorValues` | 1 | model | 0.14 |
| 65 | `xjdf4s.messaging.MessageNames` | 1 | messaging | 0.17 |
| 66 | `xjdf4s.core.Primitives` | 1 | core | 0.02 |
| 67 | `xjdf4s.core.Values` | 0 | core | 0.00 |
| 68 | `xjdf4s.core.Validation` | 0 | core | 0.00 |

---

## 🚨 Полный рейтинг Betweenness Centrality

| # | File | Betweenness | Fan-In | Fan-Out | Module |
|---|---|---|---|---|---|
| 1 | `xjdf4s.model.Resource` | 1977.87 | 39 | 39 | model |
| 2 | `xjdf4s.model.Subelements` | 570.15 | 27 | 7 | model |
| 3 | `xjdf4s.model.ContentAndShapeIntents` | 343.63 | 14 | 8 | model |
| 4 | `xjdf4s.model.Product` | 222.61 | 9 | 10 | model |
| 5 | `xjdf4s.model.Partition` | 96.93 | 30 | 5 | model |
| 6 | `xjdf4s.model.resources.MediaAndColor` | 92.34 | 6 | 15 | model |
| 7 | `xjdf4s.model.XJDF` | 52.71 | 3 | 9 | model |
| 8 | `xjdf4s.messaging.Message` | 49.33 | 7 | 4 | messaging |
| 9 | `xjdf4s.model.resources.AdditionalResources` | 34.95 | 3 | 14 | model |
| 10 | `xjdf4s.messaging.ControlMessages` | 34.24 | 2 | 7 | messaging |
| 11 | `xjdf4s.model.resources.MarksAndStacking` | 31.78 | 3 | 15 | model |
| 12 | `xjdf4s.model.resources.Device` | 29.59 | 6 | 9 | model |
| 13 | `xjdf4s.model.resources.SimpleResources` | 24.70 | 4 | 8 | model |
| 14 | `xjdf4s.messaging.StatusNotificationResourceMessages` | 23.74 | 1 | 10 | messaging |
| 15 | `xjdf4s.model.resources.PostpressResources` | 22.95 | 1 | 9 | model |
| 16 | `xjdf4s.model.resources.ProcessResources` | 21.14 | 7 | 13 | model |
| 17 | `xjdf4s.model.FinishingIntents` | 20.44 | 7 | 9 | model |
| 18 | `xjdf4s.model.SimpleIntents` | 18.50 | 11 | 7 | model |
| 19 | `xjdf4s.model.resources.FoundationalResources` | 18.28 | 6 | 9 | model |
| 20 | `xjdf4s.model.resources.RemainingPostpressResources` | 17.42 | 2 | 9 | model |
| 21 | `xjdf4s.model.resources.SheetOptimizing` | 16.86 | 2 | 12 | model |
| 22 | `xjdf4s.model.resources.MorePostpressResources` | 16.85 | 1 | 7 | model |
| 23 | `xjdf4s.model.resources.ShapeDefinitionResources` | 15.71 | 2 | 10 | model |
| 24 | `xjdf4s.model.AssemblingIntent` | 13.34 | 7 | 7 | model |
| 25 | `xjdf4s.core.Extension` | 13.04 | 57 | 1 | core |
| 26 | `xjdf4s.model.resources.BinderySignature` | 12.13 | 2 | 9 | model |
| 27 | `xjdf4s.core.Primitives` | 10.04 | 55 | 1 | core |
| 28 | `xjdf4s.model.resources.RunList` | 9.90 | 3 | 9 | model |
| 29 | `xjdf4s.model.MediaIntent` | 8.75 | 5 | 7 | model |
| 30 | `xjdf4s.model.BindingIntent` | 8.20 | 4 | 8 | model |
| 31 | `xjdf4s.model.resources.Content` | 8.16 | 2 | 15 | model |
| 32 | `xjdf4s.model.Audit` | 7.50 | 3 | 3 | model |
| 33 | `xjdf4s.model.resources.FoldingResources` | 7.39 | 2 | 9 | model |
| 34 | `xjdf4s.model.resources.Identification` | 6.69 | 8 | 7 | model |
| 35 | `xjdf4s.model.resources.Interpreting` | 5.88 | 4 | 10 | model |
| 36 | `xjdf4s.messaging.KnownMessages` | 4.59 | 1 | 8 | messaging |
| 37 | `xjdf4s.model.resources.PrepressResources` | 4.39 | 6 | 8 | model |
| 38 | `xjdf4s.model.resources.DieLayoutProduction` | 4.28 | 5 | 8 | model |
| 39 | `xjdf4s.messaging.GangAndQueueStatusMessages` | 3.30 | 2 | 8 | messaging |
| 40 | `xjdf4s.model.resources.DeliveryAndPreflightResources` | 3.28 | 1 | 8 | model |
| 41 | `xjdf4s.core.Cardinality` | 3.13 | 25 | 1 | core |
| 42 | `xjdf4s.messaging.QueueEntryMessages` | 2.47 | 1 | 8 | messaging |
| 43 | `xjdf4s.messaging.XJMF` | 2.17 | 1 | 7 | messaging |
| 44 | `xjdf4s.model.resources.ColorSpaceConversion` | 1.67 | 2 | 7 | model |
| 45 | `xjdf4s.model.resources.Rendering` | 1.67 | 2 | 9 | model |
| 46 | `xjdf4s.dsl.DocInterpreters` | 1.67 | 1 | 10 | dsl |
| 47 | `xjdf4s.model.resources.PdlCreation` | 1.40 | 1 | 8 | model |
| 48 | `xjdf4s.model.resources.Layout` | 1.40 | 1 | 16 | model |
| 49 | `xjdf4s.model.resources.DieLayout` | 1.40 | 1 | 10 | model |
| 50 | `xjdf4s.model.resources.SmallProductionResources` | 1.40 | 1 | 7 | model |
| 51 | `xjdf4s.model.resources.QualityControl` | 1.40 | 1 | 12 | model |
| 52 | `xjdf4s.model.resources.GeneralAndPressResources` | 1.01 | 2 | 8 | model |
| 53 | `xjdf4s.model.TypedValues` | 0.66 | 6 | 1 | model |
| 54 | `xjdf4s.model.ColorValues` | 0.66 | 6 | 1 | model |
| 55 | `xjdf4s.model.Header` | 0.50 | 8 | 2 | model |
| 56 | `xjdf4s.core.Common` | 0.45 | 11 | 2 | core |
| 57 | `xjdf4s.model.resources.ImageCompression` | 0.44 | 2 | 6 | model |
| 58 | `xjdf4s.model.resources.Contact` | 0.32 | 1 | 5 | model |
| 59 | `xjdf4s.model.resources.ColorantControl` | 0.32 | 1 | 5 | model |
| 60 | `xjdf4s.model.resources.MissingSchemaResources` | 0.32 | 1 | 8 | model |

---

## 🔄 Циклические зависимости (детальный разбор)

Обнаружено **2** циклов. Циклы нарушают принцип ацикличности зависимостей (ADP).


### Цикл 1 (2 файлов)

```
xjdf4s.dsl.DocOp → xjdf4s.dsl.DocInterpreters → xjdf4s.dsl.DocOp
```

**💡 Рекомендация:** Разорвать цикл через `xjdf4s.dsl.DocOp` (Fan-In: 1). Вынесите интерфейс (trait) в общий модуль.


### Цикл 2 (43 файлов)

```
xjdf4s.model.resources.ProcessResources → xjdf4s.model.resources.ColorSpaceConversion → xjdf4s.model.resources.DeliveryAndPreflightResources → xjdf4s.model.ContentAndShapeIntents → xjdf4s.model.resources.QualityControl → xjdf4s.model.resources.DieLayoutProduction → xjdf4s.model.resources.BinderySignature → xjdf4s.model.resources.FoundationalResources → xjdf4s.model.resources.ColorantControl → xjdf4s.model.resources.Content → xjdf4s.model.resources.MarksAndStacking → xjdf4s.model.resources.PrepressResources → xjdf4s.model.resources.PostpressResources → xjdf4s.model.resources.FoldingResources → xjdf4s.model.BindingIntent → xjdf4s.model.Product → xjdf4s.model.resources.AdditionalResources → xjdf4s.model.resources.SmallProductionResources → xjdf4s.model.resources.MissingSchemaResources → xjdf4s.model.resources.GeneralAndPressResources → xjdf4s.model.resources.ShapeDefinitionResources → xjdf4s.model.resources.Rendering → xjdf4s.model.resources.RemainingPostpressResources → xjdf4s.model.resources.RunList → xjdf4s.model.resources.DieLayout → xjdf4s.model.Resource → xjdf4s.model.FinishingIntents → xjdf4s.model.SimpleIntents → xjdf4s.model.resources.Contact → xjdf4s.model.resources.Interpreting → xjdf4s.model.resources.Layout → xjdf4s.model.resources.PdlCreation → xjdf4s.model.Subelements → xjdf4s.model.Partition → xjdf4s.model.resources.SimpleResources → xjdf4s.model.resources.Device → xjdf4s.model.resources.SheetOptimizing → xjdf4s.model.resources.MorePostpressResources → xjdf4s.model.resources.ImageCompression → xjdf4s.model.resources.Identification → xjdf4s.model.MediaIntent → xjdf4s.model.resources.MediaAndColor → xjdf4s.model.AssemblingIntent → xjdf4s.model.resources.ProcessResources
```

**💡 Рекомендация:** Разорвать цикл через `xjdf4s.model.resources.DeliveryAndPreflightResources` (Fan-In: 1). Вынесите интерфейс (trait) в общий модуль.


---

## 💀 Изолированные файлы (Dead Nodes)

✅ Изолированных файлов нет.

---

## 🍃 Листовые узлы (Fan-In = 0, Fan-Out > 0)

Всего: **3**

| File | Fan-Out | Module |
|---|---|---|
| `xjdf4s.model.DocumentValidation` | 6 | model |
| `xjdf4s.messaging.StandardMessages` | 5 | messaging |
| `xjdf4s.Protocol` | 4 | protocol |

---

## 🌳 Корневые узлы (Fan-In > 0, Fan-Out = 0)

Всего: **2**

| File | Fan-In | Module |
|---|---|---|
| `xjdf4s.core.Values` | 29 | core |
| `xjdf4s.core.Validation` | 20 | core |

---

## ⚖️ Stability vs Importance (Зонный анализ)

Файл с высоким Fan-In и высокой Instability — это архитектурное нарушение.

✅ Нарушений принципа стабильных зависимостей не обнаружено.

---

## 📋 Автоматические рекомендации

### 🔴 Приоритет: Высокий

1. **Разорвать 2 циклических зависимостей.** Используйте Dependency Inversion.
3. **Снизить нагрузку на `xjdf4s.model.Resource`.** Betweenness = 1978.

### 🟡 Приоритет: Средний

4. **Разбить 1 God Objects** (Fan-Out > 25) на более мелкие модули.

### 🟢 Приоритет: Низкий

6. **Добавить тесты** для топ-10 файлов по Fan-In.
7. **Документировать** интерфейсы файлов с Betweenness > 21.14322044670343.
