# 📊 Architecture Graph Analysis — Full Report

> **Сгенерировано:** 2026-08-15T11:20:15.193276
> **Узлов (файлов):** 43 | **Рёбер (зависимостей):** 232 | **Модулей:** 3 | **Циклов:** 1

## 🎯 Executive Summary

### Ключевые находки

1. **Циклические зависимости:** Обнаружено **1** циклов. Это нарушает модульность и требует вмешательства.
2. **Узкие места (Bottlenecks):** 5 файлов имеют аномально высокую Betweenness Centrality. Главный: `xjdf4s.resources.AllResources` (score: 161.5952380952381).
3. **God Objects:** 0 файлов имеют Fan-Out > 25. ✅
4. **Фундамент:** 0 файлов имеют Fan-In > 50. Они должны быть максимально стабильными.
5. **Изолированные файлы:** 0 файлов (0.0%) не имеют связей. Кандидаты на удаление.
6. **Средний Fan-In:** 5.4 | **Средний Fan-Out:** 5.4

---

## 📈 Распределение метрик


### Fan-In (входящие зависимости)

| Квантиль | Значение |
|---|---|
| Min | 0 |
| P25 | 1 |
| Median (P50) | 2 |
| P75 | 6 |
| P90 | 12 |
| P95 | 19 |
| Max | 36 |
| Mean | 5.4 |

### Fan-In: Гистограмма

| Диапазон | Количество файлов | Процент |
|---|---|---|
| 0–0 | 6 | 14.0% |
| 1–5 | 25 | 58.1% |
| 6–10 | 4 | 9.3% |
| 11–20 | 5 | 11.6% |
| 21–50 | 3 | 7.0% |
| 51–100 | 0 | 0.0% |
| 101+ | 0 | 0.0% |

### Fan-Out (исходящие зависимости)

| Квантиль | Значение |
|---|---|
| Min | 0 |
| P25 | 3 |
| Median (P50) | 4 |
| P75 | 7 |
| P90 | 11 |
| P95 | 12 |
| Max | 19 |
| Mean | 5.4 |

### Betweenness Centrality

- Файлов с Betweenness > 0: **31** из 43 (72.1%)
- Медиана (из ненулевых): 4.15
- P90 (из ненулевых): 42.069047619047616
- Max: 161.60

---

## 📦 Анализ по модулям

| Модуль | Файлов | Ср. Fan-In | Ср. Fan-Out | Ср. Betweenness | Ср. Instability | Внутр. связей | Внешн. связей |
|---|---|---|---|---|---|---|---|
| `core` | 36 | 6.4 | 4.4 | 15.7 | 0.52 | 160 | 0 |
| `examples` | 2 | 0.5 | 14.0 | 8.1 | 0.98 | 1 | 27 |
| `laws` | 5 | 0.4 | 8.8 | 3.8 | 0.97 | 2 | 42 |

---

## 🔗 Матрица межмодульных зависимостей

Строки = откуда, столбцы = куда. Число = количество файловых зависимостей.

| from \ to | `core` | `examples` | `laws` |
|---|---|---|---|
| **core** | **160** | · | · |
| **examples** | **27** | **1** | · |
| **laws** | **42** | · | **2** |

---

## 🔥 Risk Ranking (все файлы)

Risk Score = (Fan-Out × 0.3 + Instability × 50 × 0.3 + Betweenness × 0.4). Чем выше — тем опаснее.

| # | File | Module | Fan-In | Fan-Out | Betweenness | Instability | **Risk** |
|---|---|---|---|---|---|---|---|
| 1 | `xjdf4s.resources.AllResources` | core | 5 | 13 | 161.6 | 0.72 | 🟡 **79.4** |
| 2 | `xjdf4s.model.Resource` | core | 11 | 9 | 135.1 | 0.45 | 🟡 **63.5** |
| 3 | `xjdf4s.intents.AllIntents` | core | 3 | 6 | 45.9 | 0.67 | 🟡 **30.2** |
| 4 | `xjdf4s.model.Validation` | core | 6 | 11 | 42.1 | 0.65 | 🟢 **29.8** |
| 5 | `xjdf4s.examples.SpecExamples` | examples | 1 | 19 | 16.3 | 0.95 | 🟢 **26.5** |
| 6 | `xjdf4s.model.Ticket` | core | 7 | 11 | 30.8 | 0.61 | 🟢 **24.8** |
| 7 | `test.xjdf4s.laws.Arbitraries` | laws | 2 | 12 | 19.1 | 0.86 | 🟢 **24.1** |
| 8 | `xjdf4s.model.Intent` | core | 3 | 3 | 35.9 | 0.50 | 🟢 **22.8** |
| 9 | `xjdf4s.dsl.XjdfDsl` | core | 1 | 19 | 3.2 | 0.95 | 🟢 **21.2** |
| 10 | `xjdf4s.model.Header` | core | 3 | 7 | 20.6 | 0.70 | 🟢 **20.8** |
| 11 | `test.xjdf4s.laws.TicketLaws` | laws | 0 | 11 | 0.0 | 1.00 | 🟢 **18.3** |
| 12 | `xjdf4s.model.Patch` | core | 4 | 7 | 15.8 | 0.64 | 🟢 **18.0** |
| 13 | `xjdf4s.resources.Finishing` | core | 1 | 5 | 9.6 | 0.83 | 🟢 **17.8** |
| 14 | `xjdf4s.examples.Main` | examples | 0 | 9 | 0.0 | 1.00 | 🟢 **17.7** |
| 15 | `test.xjdf4s.laws.PartitionLaws` | laws | 0 | 8 | 0.0 | 1.00 | 🟢 **17.4** |
| 16 | `test.xjdf4s.laws.AlgebraLaws` | laws | 0 | 8 | 0.0 | 1.00 | 🟢 **17.4** |
| 17 | `test.xjdf4s.laws.AlignmentLaws` | laws | 0 | 5 | 0.0 | 1.00 | 🟢 **16.5** |
| 18 | `xjdf4s.model.Product` | core | 7 | 5 | 20.9 | 0.42 | 🟢 **16.1** |
| 19 | `xjdf4s.model.IdSource` | core | 0 | 1 | 0.0 | 1.00 | 🟢 **15.3** |
| 20 | `xjdf4s.resources.Layout` | core | 1 | 5 | 0.5 | 0.83 | 🟢 **14.2** |
| 21 | `xjdf4s.intents.Binding` | core | 1 | 4 | 1.0 | 0.80 | 🟢 **13.6** |
| 22 | `xjdf4s.intents.ColorProduction` | core | 1 | 3 | 1.0 | 0.75 | 🟢 **12.6** |
| 23 | `xjdf4s.intents.MediaLayout` | core | 1 | 3 | 1.0 | 0.75 | 🟢 **12.6** |
| 24 | `xjdf4s.resources.RunList` | core | 2 | 5 | 0.7 | 0.71 | 🟢 **12.5** |
| 25 | `xjdf4s.resources.Color` | core | 1 | 3 | 0.3 | 0.75 | 🟢 **12.3** |
| 26 | `xjdf4s.intents.FoldingVariable` | core | 2 | 4 | 1.5 | 0.67 | 🟢 **11.8** |
| 27 | `xjdf4s.resources.Preview` | core | 1 | 2 | 0.2 | 0.67 | 🟢 **10.7** |
| 28 | `xjdf4s.resources.Device` | core | 1 | 2 | 0.2 | 0.67 | 🟢 **10.7** |
| 29 | `xjdf4s.prim.Common` | core | 14 | 4 | 14.7 | 0.22 | 🟢 **10.4** |
| 30 | `xjdf4s.resources.NodeInfo` | core | 2 | 3 | 0.8 | 0.60 | 🟢 **10.2** |
| 31 | `xjdf4s.resources.Delivery` | core | 2 | 3 | 0.7 | 0.60 | 🟢 **10.2** |
| 32 | `xjdf4s.resources.Media` | core | 3 | 4 | 0.5 | 0.57 | 🟢 **10.0** |
| 33 | `xjdf4s.resources.Component` | core | 3 | 4 | 0.5 | 0.57 | 🟢 **10.0** |
| 34 | `xjdf4s.model.Amounts` | core | 5 | 4 | 1.0 | 0.44 | 🟢 **8.2** |
| 35 | `xjdf4s.resources.Contact` | core | 1 | 1 | 0.0 | 0.50 | 🟢 **7.8** |
| 36 | `xjdf4s.model.Audit` | core | 10 | 3 | 7.2 | 0.23 | 🟢 **7.3** |
| 37 | `xjdf4s.model.Partition` | core | 11 | 4 | 4.2 | 0.27 | 🟢 **6.9** |
| 38 | `xjdf4s.prim.Enums` | core | 24 | 2 | 6.3 | 0.08 | 🟢 **4.3** |
| 39 | `xjdf4s.prim.Ids` | core | 23 | 0 | 0.0 | 0.00 | 🟢 **0.0** |
| 40 | `xjdf4s.prim.Quantity` | core | 19 | 0 | 0.0 | 0.00 | 🟢 **0.0** |
| 41 | `xjdf4s.prim.Time` | core | 12 | 0 | 0.0 | 0.00 | 🟢 **0.0** |
| 42 | `xjdf4s.prim.Versions` | core | 2 | 0 | 0.0 | 0.00 | 🟢 **0.0** |
| 43 | `xjdf4s.prim.Tokens` | core | 36 | 0 | 0.0 | 0.00 | 🟢 **0.0** |

---

## 🧱 Полный рейтинг Fan-In

| # | File | Fan-In | Module | Instability |
|---|---|---|---|---|
| 1 | `xjdf4s.prim.Tokens` | 36 | core | 0.00 |
| 2 | `xjdf4s.prim.Enums` | 24 | core | 0.08 |
| 3 | `xjdf4s.prim.Ids` | 23 | core | 0.00 |
| 4 | `xjdf4s.prim.Quantity` | 19 | core | 0.00 |
| 5 | `xjdf4s.prim.Common` | 14 | core | 0.22 |
| 6 | `xjdf4s.prim.Time` | 12 | core | 0.00 |
| 7 | `xjdf4s.model.Resource` | 11 | core | 0.45 |
| 8 | `xjdf4s.model.Partition` | 11 | core | 0.27 |
| 9 | `xjdf4s.model.Audit` | 10 | core | 0.23 |
| 10 | `xjdf4s.model.Product` | 7 | core | 0.42 |
| 11 | `xjdf4s.model.Ticket` | 7 | core | 0.61 |
| 12 | `xjdf4s.model.Validation` | 6 | core | 0.65 |
| 13 | `xjdf4s.model.Amounts` | 5 | core | 0.44 |
| 14 | `xjdf4s.resources.AllResources` | 5 | core | 0.72 |
| 15 | `xjdf4s.model.Patch` | 4 | core | 0.64 |
| 16 | `xjdf4s.intents.AllIntents` | 3 | core | 0.67 |
| 17 | `xjdf4s.resources.Media` | 3 | core | 0.57 |
| 18 | `xjdf4s.model.Intent` | 3 | core | 0.50 |
| 19 | `xjdf4s.model.Header` | 3 | core | 0.70 |
| 20 | `xjdf4s.resources.Component` | 3 | core | 0.57 |
| 21 | `xjdf4s.resources.Delivery` | 2 | core | 0.60 |
| 22 | `xjdf4s.prim.Versions` | 2 | core | 0.00 |
| 23 | `test.xjdf4s.laws.Arbitraries` | 2 | laws | 0.86 |
| 24 | `xjdf4s.intents.FoldingVariable` | 2 | core | 0.67 |
| 25 | `xjdf4s.resources.NodeInfo` | 2 | core | 0.60 |
| 26 | `xjdf4s.resources.RunList` | 2 | core | 0.71 |
| 27 | `xjdf4s.intents.ColorProduction` | 1 | core | 0.75 |
| 28 | `xjdf4s.intents.MediaLayout` | 1 | core | 0.75 |
| 29 | `xjdf4s.resources.Color` | 1 | core | 0.75 |
| 30 | `xjdf4s.intents.Binding` | 1 | core | 0.80 |
| 31 | `xjdf4s.resources.Contact` | 1 | core | 0.50 |
| 32 | `xjdf4s.resources.Preview` | 1 | core | 0.67 |
| 33 | `xjdf4s.resources.Layout` | 1 | core | 0.83 |
| 34 | `xjdf4s.examples.SpecExamples` | 1 | examples | 0.95 |
| 35 | `xjdf4s.resources.Finishing` | 1 | core | 0.83 |
| 36 | `xjdf4s.dsl.XjdfDsl` | 1 | core | 0.95 |
| 37 | `xjdf4s.resources.Device` | 1 | core | 0.67 |
| 38 | `test.xjdf4s.laws.PartitionLaws` | 0 | laws | 1.00 |
| 39 | `xjdf4s.examples.Main` | 0 | examples | 1.00 |
| 40 | `test.xjdf4s.laws.TicketLaws` | 0 | laws | 1.00 |
| 41 | `test.xjdf4s.laws.AlgebraLaws` | 0 | laws | 1.00 |
| 42 | `test.xjdf4s.laws.AlignmentLaws` | 0 | laws | 1.00 |
| 43 | `xjdf4s.model.IdSource` | 0 | core | 1.00 |

---

## 🕸️ Полный рейтинг Fan-Out

| # | File | Fan-Out | Module | Instability |
|---|---|---|---|---|
| 1 | `xjdf4s.examples.SpecExamples` | 19 | examples | 0.95 |
| 2 | `xjdf4s.dsl.XjdfDsl` | 19 | core | 0.95 |
| 3 | `xjdf4s.resources.AllResources` | 13 | core | 0.72 |
| 4 | `test.xjdf4s.laws.Arbitraries` | 12 | laws | 0.86 |
| 5 | `test.xjdf4s.laws.TicketLaws` | 11 | laws | 1.00 |
| 6 | `xjdf4s.model.Validation` | 11 | core | 0.65 |
| 7 | `xjdf4s.model.Ticket` | 11 | core | 0.61 |
| 8 | `xjdf4s.examples.Main` | 9 | examples | 1.00 |
| 9 | `xjdf4s.model.Resource` | 9 | core | 0.45 |
| 10 | `test.xjdf4s.laws.PartitionLaws` | 8 | laws | 1.00 |
| 11 | `test.xjdf4s.laws.AlgebraLaws` | 8 | laws | 1.00 |
| 12 | `xjdf4s.model.Patch` | 7 | core | 0.64 |
| 13 | `xjdf4s.model.Header` | 7 | core | 0.70 |
| 14 | `xjdf4s.intents.AllIntents` | 6 | core | 0.67 |
| 15 | `xjdf4s.model.Product` | 5 | core | 0.42 |
| 16 | `test.xjdf4s.laws.AlignmentLaws` | 5 | laws | 1.00 |
| 17 | `xjdf4s.resources.Layout` | 5 | core | 0.83 |
| 18 | `xjdf4s.resources.Finishing` | 5 | core | 0.83 |
| 19 | `xjdf4s.resources.RunList` | 5 | core | 0.71 |
| 20 | `xjdf4s.resources.Media` | 4 | core | 0.57 |
| 21 | `xjdf4s.prim.Common` | 4 | core | 0.22 |
| 22 | `xjdf4s.intents.Binding` | 4 | core | 0.80 |
| 23 | `xjdf4s.model.Amounts` | 4 | core | 0.44 |
| 24 | `xjdf4s.resources.Component` | 4 | core | 0.57 |
| 25 | `xjdf4s.intents.FoldingVariable` | 4 | core | 0.67 |
| 26 | `xjdf4s.model.Partition` | 4 | core | 0.27 |
| 27 | `xjdf4s.intents.ColorProduction` | 3 | core | 0.75 |
| 28 | `xjdf4s.intents.MediaLayout` | 3 | core | 0.75 |
| 29 | `xjdf4s.resources.Delivery` | 3 | core | 0.60 |
| 30 | `xjdf4s.model.Intent` | 3 | core | 0.50 |
| 31 | `xjdf4s.resources.Color` | 3 | core | 0.75 |
| 32 | `xjdf4s.model.Audit` | 3 | core | 0.23 |
| 33 | `xjdf4s.resources.NodeInfo` | 3 | core | 0.60 |
| 34 | `xjdf4s.prim.Enums` | 2 | core | 0.08 |
| 35 | `xjdf4s.resources.Preview` | 2 | core | 0.67 |
| 36 | `xjdf4s.resources.Device` | 2 | core | 0.67 |
| 37 | `xjdf4s.resources.Contact` | 1 | core | 0.50 |
| 38 | `xjdf4s.model.IdSource` | 1 | core | 1.00 |
| 39 | `xjdf4s.prim.Ids` | 0 | core | 0.00 |
| 40 | `xjdf4s.prim.Quantity` | 0 | core | 0.00 |
| 41 | `xjdf4s.prim.Time` | 0 | core | 0.00 |
| 42 | `xjdf4s.prim.Versions` | 0 | core | 0.00 |
| 43 | `xjdf4s.prim.Tokens` | 0 | core | 0.00 |

---

## 🚨 Полный рейтинг Betweenness Centrality

| # | File | Betweenness | Fan-In | Fan-Out | Module |
|---|---|---|---|---|---|
| 1 | `xjdf4s.resources.AllResources` | 161.60 | 5 | 13 | core |
| 2 | `xjdf4s.model.Resource` | 135.11 | 11 | 9 | core |
| 3 | `xjdf4s.intents.AllIntents` | 45.90 | 3 | 6 | core |
| 4 | `xjdf4s.model.Validation` | 42.07 | 6 | 11 | core |
| 5 | `xjdf4s.model.Intent` | 35.90 | 3 | 3 | core |
| 6 | `xjdf4s.model.Ticket` | 30.80 | 7 | 11 | core |
| 7 | `xjdf4s.model.Product` | 20.90 | 7 | 5 | core |
| 8 | `xjdf4s.model.Header` | 20.62 | 3 | 7 | core |
| 9 | `test.xjdf4s.laws.Arbitraries` | 19.14 | 2 | 12 | laws |
| 10 | `xjdf4s.examples.SpecExamples` | 16.25 | 1 | 19 | examples |
| 11 | `xjdf4s.model.Patch` | 15.76 | 4 | 7 | core |
| 12 | `xjdf4s.prim.Common` | 14.68 | 14 | 4 | core |
| 13 | `xjdf4s.resources.Finishing` | 9.60 | 1 | 5 | core |
| 14 | `xjdf4s.model.Audit` | 7.25 | 10 | 3 | core |
| 15 | `xjdf4s.prim.Enums` | 6.32 | 24 | 2 | core |
| 16 | `xjdf4s.model.Partition` | 4.15 | 11 | 4 | core |
| 17 | `xjdf4s.dsl.XjdfDsl` | 3.17 | 1 | 19 | core |
| 18 | `xjdf4s.intents.FoldingVariable` | 1.50 | 2 | 4 | core |
| 19 | `xjdf4s.intents.ColorProduction` | 1.00 | 1 | 3 | core |
| 20 | `xjdf4s.intents.MediaLayout` | 1.00 | 1 | 3 | core |
| 21 | `xjdf4s.intents.Binding` | 1.00 | 1 | 4 | core |
| 22 | `xjdf4s.model.Amounts` | 0.95 | 5 | 4 | core |
| 23 | `xjdf4s.resources.NodeInfo` | 0.81 | 2 | 3 | core |
| 24 | `xjdf4s.resources.RunList` | 0.68 | 2 | 5 | core |
| 25 | `xjdf4s.resources.Delivery` | 0.67 | 2 | 3 | core |
| 26 | `xjdf4s.resources.Layout` | 0.51 | 1 | 5 | core |
| 27 | `xjdf4s.resources.Media` | 0.48 | 3 | 4 | core |
| 28 | `xjdf4s.resources.Component` | 0.48 | 3 | 4 | core |
| 29 | `xjdf4s.resources.Color` | 0.31 | 1 | 3 | core |
| 30 | `xjdf4s.resources.Preview` | 0.20 | 1 | 2 | core |
| 31 | `xjdf4s.resources.Device` | 0.20 | 1 | 2 | core |

---

## 🔄 Циклические зависимости (детальный разбор)

Обнаружено **1** циклов. Циклы нарушают принцип ацикличности зависимостей (ADP).


### Цикл 1 (4 файлов)

```
xjdf4s.model.Validation → xjdf4s.model.Product → xjdf4s.model.Ticket → xjdf4s.model.Patch → xjdf4s.model.Validation
```

**💡 Рекомендация:** Разорвать цикл через `xjdf4s.model.Patch` (Fan-In: 4). Вынесите интерфейс (trait) в общий модуль.


---

## 💀 Изолированные файлы (Dead Nodes)

✅ Изолированных файлов нет.

---

## 🍃 Листовые узлы (Fan-In = 0, Fan-Out > 0)

Всего: **6**

| File | Fan-Out | Module |
|---|---|---|
| `test.xjdf4s.laws.TicketLaws` | 11 | laws |
| `xjdf4s.examples.Main` | 9 | examples |
| `test.xjdf4s.laws.PartitionLaws` | 8 | laws |
| `test.xjdf4s.laws.AlgebraLaws` | 8 | laws |
| `test.xjdf4s.laws.AlignmentLaws` | 5 | laws |
| `xjdf4s.model.IdSource` | 1 | core |

---

## 🌳 Корневые узлы (Fan-In > 0, Fan-Out = 0)

Всего: **5**

| File | Fan-In | Module |
|---|---|---|
| `xjdf4s.prim.Tokens` | 36 | core |
| `xjdf4s.prim.Ids` | 23 | core |
| `xjdf4s.prim.Quantity` | 19 | core |
| `xjdf4s.prim.Time` | 12 | core |
| `xjdf4s.prim.Versions` | 2 | core |

---

## ⚖️ Stability vs Importance (Зонный анализ)

Файл с высоким Fan-In и высокой Instability — это архитектурное нарушение.

✅ Нарушений принципа стабильных зависимостей не обнаружено.

---

## 📋 Автоматические рекомендации

### 🔴 Приоритет: Высокий

1. **Разорвать 1 циклических зависимостей.** Используйте Dependency Inversion.
3. **Снизить нагрузку на `xjdf4s.resources.AllResources`.** Betweenness = 162.

### 🟡 Приоритет: Средний


### 🟢 Приоритет: Низкий

6. **Добавить тесты** для топ-10 файлов по Fan-In.
7. **Документировать** интерфейсы файлов с Betweenness > 19.142857142857142.
