# 📊 Architecture Graph Analysis — Full Report

> **Сгенерировано:** 2026-08-16T19:20:07.773939
> **Узлов (файлов):** 47 | **Рёбер (зависимостей):** 271 | **Модулей:** 1 | **Циклов:** 0

## 🎯 Executive Summary

### Ключевые находки

1. **Циклические зависимости:** Обнаружено **0** циклов. Архитектура ациклична — ✅.
2. **Узкие места (Bottlenecks):** 5 файлов имеют аномально высокую Betweenness Centrality. Главный: `xjdf4s.resources.AllResources` (score: 98.0).
3. **God Objects:** 1 файлов имеют Fan-Out > 25. Худший: `xjdf4s.model.TicketValidator` (Fan-Out: 31).
4. **Фундамент:** 0 файлов имеют Fan-In > 50. Они должны быть максимально стабильными.
5. **Изолированные файлы:** 0 файлов (0.0%) не имеют связей. Кандидаты на удаление.
6. **Средний Fan-In:** 5.8 | **Средний Fan-Out:** 5.8

---

## 📈 Распределение метрик


### Fan-In (входящие зависимости)

| Квантиль | Значение |
|---|---|
| Min | 0 |
| P25 | 2 |
| Median (P50) | 2 |
| P75 | 6 |
| P90 | 18 |
| P95 | 23 |
| Max | 37 |
| Mean | 5.8 |

### Fan-In: Гистограмма

| Диапазон | Количество файлов | Процент |
|---|---|---|
| 0–0 | 1 | 2.1% |
| 1–5 | 33 | 70.2% |
| 6–10 | 7 | 14.9% |
| 11–20 | 1 | 2.1% |
| 21–50 | 5 | 10.6% |
| 51–100 | 0 | 0.0% |
| 101+ | 0 | 0.0% |

### Fan-Out (исходящие зависимости)

| Квантиль | Значение |
|---|---|
| Min | 0 |
| P25 | 2 |
| Median (P50) | 5 |
| P75 | 6 |
| P90 | 11 |
| P95 | 13 |
| Max | 31 |
| Mean | 5.8 |

### Betweenness Centrality

- Файлов с Betweenness > 0: **37** из 47 (78.7%)
- Медиана (из ненулевых): 1.4166666666666665
- P90 (из ненулевых): 38.5
- Max: 98.00

---

## 📦 Анализ по модулям

| Модуль | Файлов | Ср. Fan-In | Ср. Fan-Out | Ср. Betweenness | Ср. Instability | Внутр. связей | Внешн. связей |
|---|---|---|---|---|---|---|---|
| `modules` | 47 | 5.8 | 5.8 | 11.8 | 0.55 | 271 | 0 |

---

## 🔗 Матрица межмодульных зависимостей

Строки = откуда, столбцы = куда. Число = количество файловых зависимостей.

| from \ to | `modules` |
|---|---|
| **modules** | **271** |

---

## 🔥 Risk Ranking (все файлы)

Risk Score = (Fan-Out × 0.3 + Instability × 50 × 0.3 + Betweenness × 0.4). Чем выше — тем опаснее.

| # | File | Module | Fan-In | Fan-Out | Betweenness | Instability | **Risk** |
|---|---|---|---|---|---|---|---|
| 1 | `xjdf4s.resources.AllResources` | modules | 4 | 13 | 98.0 | 0.76 | 🟡 **54.6** |
| 2 | `xjdf4s.model.Resource` | modules | 8 | 11 | 86.0 | 0.58 | 🟡 **46.4** |
| 3 | `xjdf4s.intents.AllIntents` | modules | 4 | 11 | 72.4 | 0.73 | 🟡 **43.3** |
| 4 | `xjdf4s.model.TicketValidator` | modules | 2 | 31 | 29.2 | 0.94 | 🟡 **35.1** |
| 5 | `xjdf4s.examples.SpecExamples` | modules | 1 | 22 | 27.9 | 0.96 | 🟡 **32.1** |
| 6 | `xjdf4s.model.elements.CommonElements` | modules | 23 | 7 | 59.4 | 0.23 | 🟢 **29.4** |
| 7 | `xjdf4s.model.Ticket` | modules | 6 | 10 | 37.3 | 0.63 | 🟢 **27.3** |
| 8 | `xjdf4s.model.Intent` | modules | 4 | 4 | 38.5 | 0.50 | 🟢 **24.1** |
| 9 | `xjdf4s.dsl.XjdfDsl` | modules | 1 | 21 | 6.0 | 0.95 | 🟢 **23.0** |
| 10 | `xjdf4s.model.Product` | modules | 7 | 6 | 34.9 | 0.46 | 🟢 **22.7** |
| 11 | `xjdf4s.model.Header` | modules | 3 | 8 | 21.4 | 0.73 | 🟢 **21.9** |
| 12 | `xjdf4s.examples.Main` | modules | 0 | 9 | 0.0 | 1.00 | 🟢 **17.7** |
| 13 | `xjdf4s.model.ChangeOrder` | modules | 1 | 9 | 3.0 | 0.90 | 🟢 **17.4** |
| 14 | `xjdf4s.resources.Finishing` | modules | 2 | 5 | 6.2 | 0.71 | 🟢 **14.7** |
| 15 | `xjdf4s.model.Patch` | modules | 1 | 6 | 0.0 | 0.86 | 🟢 **14.7** |
| 16 | `xjdf4s.model.NamedFeatures` | modules | 1 | 5 | 0.0 | 0.83 | 🟢 **14.0** |
| 17 | `xjdf4s.intents.Binding` | modules | 2 | 6 | 1.4 | 0.75 | 🟢 **13.6** |
| 18 | `xjdf4s.intents.ContentCheck` | modules | 2 | 5 | 3.3 | 0.71 | 🟢 **13.5** |
| 19 | `xjdf4s.intents.FoldingVariable` | modules | 3 | 6 | 2.5 | 0.67 | 🟢 **12.8** |
| 20 | `xjdf4s.resources.NodeInfo` | modules | 2 | 5 | 1.1 | 0.71 | 🟢 **12.6** |
| 21 | `xjdf4s.intents.ShapeCutting` | modules | 2 | 5 | 0.7 | 0.71 | 🟢 **12.5** |
| 22 | `xjdf4s.resources.Layout` | modules | 2 | 5 | 0.6 | 0.71 | 🟢 **12.5** |
| 23 | `xjdf4s.intents.Laminating` | modules | 1 | 3 | 0.4 | 0.75 | 🟢 **12.3** |
| 24 | `xjdf4s.intents.MediaLayout` | modules | 2 | 4 | 1.1 | 0.67 | 🟢 **11.6** |
| 25 | `xjdf4s.intents.ColorProduction` | modules | 2 | 4 | 1.1 | 0.67 | 🟢 **11.6** |
| 26 | `xjdf4s.resources.Delivery` | modules | 2 | 4 | 0.9 | 0.67 | 🟢 **11.6** |
| 27 | `xjdf4s.resources.RunList` | modules | 3 | 5 | 0.7 | 0.63 | 🟢 **11.2** |
| 28 | `xjdf4s.resources.Device` | modules | 1 | 2 | 1.0 | 0.67 | 🟢 **11.0** |
| 29 | `xjdf4s.model.Amounts` | modules | 3 | 5 | 0.1 | 0.63 | 🟢 **10.9** |
| 30 | `xjdf4s.resources.Media` | modules | 4 | 5 | 0.7 | 0.56 | 🟢 **10.1** |
| 31 | `xjdf4s.resources.Component` | modules | 4 | 5 | 0.7 | 0.56 | 🟢 **10.1** |
| 32 | `xjdf4s.intents.Embossing` | modules | 2 | 3 | 0.4 | 0.60 | 🟢 **10.1** |
| 33 | `xjdf4s.resources.Color` | modules | 2 | 3 | 0.3 | 0.60 | 🟢 **10.0** |
| 34 | `xjdf4s.model.Audit` | modules | 7 | 4 | 7.2 | 0.36 | 🟢 **9.5** |
| 35 | `xjdf4s.resources.Preview` | modules | 2 | 2 | 0.3 | 0.50 | 🟢 **8.2** |
| 36 | `xjdf4s.resources.Contact` | modules | 1 | 1 | 0.0 | 0.50 | 🟢 **7.8** |
| 37 | `xjdf4s.model.IdSource` | modules | 1 | 1 | 0.0 | 0.50 | 🟢 **7.8** |
| 38 | `xjdf4s.model.Partition` | modules | 7 | 4 | 1.3 | 0.36 | 🟢 **7.2** |
| 39 | `xjdf4s.intents.HoleMaking` | modules | 2 | 1 | 0.7 | 0.33 | 🟢 **5.6** |
| 40 | `xjdf4s.model.ValidationTypes` | modules | 18 | 2 | 2.8 | 0.10 | 🟢 **3.2** |
| 41 | `xjdf4s.prim.Enums` | modules | 26 | 2 | 3.1 | 0.07 | 🟢 **2.9** |
| 42 | `xjdf4s.prim.Common` | modules | 7 | 1 | 0.3 | 0.13 | 🟢 **2.3** |
| 43 | `xjdf4s.prim.Ids` | modules | 24 | 0 | 0.0 | 0.00 | 🟢 **0.0** |
| 44 | `xjdf4s.prim.Versions` | modules | 2 | 0 | 0.0 | 0.00 | 🟢 **0.0** |
| 45 | `xjdf4s.prim.Time` | modules | 8 | 0 | 0.0 | 0.00 | 🟢 **0.0** |
| 46 | `xjdf4s.prim.Tokens` | modules | 37 | 0 | 0.0 | 0.00 | 🟢 **0.0** |
| 47 | `xjdf4s.prim.Quantity` | modules | 22 | 0 | 0.0 | 0.00 | 🟢 **0.0** |

---

## 🧱 Полный рейтинг Fan-In

| # | File | Fan-In | Module | Instability |
|---|---|---|---|---|
| 1 | `xjdf4s.prim.Tokens` | 37 | modules | 0.00 |
| 2 | `xjdf4s.prim.Enums` | 26 | modules | 0.07 |
| 3 | `xjdf4s.prim.Ids` | 24 | modules | 0.00 |
| 4 | `xjdf4s.model.elements.CommonElements` | 23 | modules | 0.23 |
| 5 | `xjdf4s.prim.Quantity` | 22 | modules | 0.00 |
| 6 | `xjdf4s.model.ValidationTypes` | 18 | modules | 0.10 |
| 7 | `xjdf4s.model.Resource` | 8 | modules | 0.58 |
| 8 | `xjdf4s.prim.Time` | 8 | modules | 0.00 |
| 9 | `xjdf4s.model.Partition` | 7 | modules | 0.36 |
| 10 | `xjdf4s.prim.Common` | 7 | modules | 0.13 |
| 11 | `xjdf4s.model.Product` | 7 | modules | 0.46 |
| 12 | `xjdf4s.model.Audit` | 7 | modules | 0.36 |
| 13 | `xjdf4s.model.Ticket` | 6 | modules | 0.63 |
| 14 | `xjdf4s.resources.Media` | 4 | modules | 0.56 |
| 15 | `xjdf4s.resources.AllResources` | 4 | modules | 0.76 |
| 16 | `xjdf4s.resources.Component` | 4 | modules | 0.56 |
| 17 | `xjdf4s.intents.AllIntents` | 4 | modules | 0.73 |
| 18 | `xjdf4s.model.Intent` | 4 | modules | 0.50 |
| 19 | `xjdf4s.model.Amounts` | 3 | modules | 0.63 |
| 20 | `xjdf4s.model.Header` | 3 | modules | 0.73 |
| 21 | `xjdf4s.intents.FoldingVariable` | 3 | modules | 0.67 |
| 22 | `xjdf4s.resources.RunList` | 3 | modules | 0.63 |
| 23 | `xjdf4s.resources.Layout` | 2 | modules | 0.71 |
| 24 | `xjdf4s.intents.ShapeCutting` | 2 | modules | 0.71 |
| 25 | `xjdf4s.model.TicketValidator` | 2 | modules | 0.94 |
| 26 | `xjdf4s.resources.Finishing` | 2 | modules | 0.71 |
| 27 | `xjdf4s.prim.Versions` | 2 | modules | 0.00 |
| 28 | `xjdf4s.intents.MediaLayout` | 2 | modules | 0.67 |
| 29 | `xjdf4s.intents.ContentCheck` | 2 | modules | 0.71 |
| 30 | `xjdf4s.resources.Preview` | 2 | modules | 0.50 |
| 31 | `xjdf4s.resources.Delivery` | 2 | modules | 0.67 |
| 32 | `xjdf4s.intents.ColorProduction` | 2 | modules | 0.67 |
| 33 | `xjdf4s.resources.NodeInfo` | 2 | modules | 0.71 |
| 34 | `xjdf4s.intents.Binding` | 2 | modules | 0.75 |
| 35 | `xjdf4s.intents.HoleMaking` | 2 | modules | 0.33 |
| 36 | `xjdf4s.intents.Embossing` | 2 | modules | 0.60 |
| 37 | `xjdf4s.resources.Color` | 2 | modules | 0.60 |
| 38 | `xjdf4s.resources.Contact` | 1 | modules | 0.50 |
| 39 | `xjdf4s.intents.Laminating` | 1 | modules | 0.75 |
| 40 | `xjdf4s.dsl.XjdfDsl` | 1 | modules | 0.95 |
| 41 | `xjdf4s.examples.SpecExamples` | 1 | modules | 0.96 |
| 42 | `xjdf4s.model.Patch` | 1 | modules | 0.86 |
| 43 | `xjdf4s.resources.Device` | 1 | modules | 0.67 |
| 44 | `xjdf4s.model.ChangeOrder` | 1 | modules | 0.90 |
| 45 | `xjdf4s.model.IdSource` | 1 | modules | 0.50 |
| 46 | `xjdf4s.model.NamedFeatures` | 1 | modules | 0.83 |
| 47 | `xjdf4s.examples.Main` | 0 | modules | 1.00 |

---

## 🕸️ Полный рейтинг Fan-Out

| # | File | Fan-Out | Module | Instability |
|---|---|---|---|---|
| 1 | `xjdf4s.model.TicketValidator` | 31 | modules | 0.94 |
| 2 | `xjdf4s.examples.SpecExamples` | 22 | modules | 0.96 |
| 3 | `xjdf4s.dsl.XjdfDsl` | 21 | modules | 0.95 |
| 4 | `xjdf4s.resources.AllResources` | 13 | modules | 0.76 |
| 5 | `xjdf4s.model.Resource` | 11 | modules | 0.58 |
| 6 | `xjdf4s.intents.AllIntents` | 11 | modules | 0.73 |
| 7 | `xjdf4s.model.Ticket` | 10 | modules | 0.63 |
| 8 | `xjdf4s.examples.Main` | 9 | modules | 1.00 |
| 9 | `xjdf4s.model.ChangeOrder` | 9 | modules | 0.90 |
| 10 | `xjdf4s.model.Header` | 8 | modules | 0.73 |
| 11 | `xjdf4s.model.elements.CommonElements` | 7 | modules | 0.23 |
| 12 | `xjdf4s.intents.FoldingVariable` | 6 | modules | 0.67 |
| 13 | `xjdf4s.model.Product` | 6 | modules | 0.46 |
| 14 | `xjdf4s.model.Patch` | 6 | modules | 0.86 |
| 15 | `xjdf4s.intents.Binding` | 6 | modules | 0.75 |
| 16 | `xjdf4s.resources.Media` | 5 | modules | 0.56 |
| 17 | `xjdf4s.resources.Layout` | 5 | modules | 0.71 |
| 18 | `xjdf4s.resources.Component` | 5 | modules | 0.56 |
| 19 | `xjdf4s.model.Amounts` | 5 | modules | 0.63 |
| 20 | `xjdf4s.intents.ShapeCutting` | 5 | modules | 0.71 |
| 21 | `xjdf4s.resources.Finishing` | 5 | modules | 0.71 |
| 22 | `xjdf4s.intents.ContentCheck` | 5 | modules | 0.71 |
| 23 | `xjdf4s.resources.RunList` | 5 | modules | 0.63 |
| 24 | `xjdf4s.resources.NodeInfo` | 5 | modules | 0.71 |
| 25 | `xjdf4s.model.NamedFeatures` | 5 | modules | 0.83 |
| 26 | `xjdf4s.model.Partition` | 4 | modules | 0.36 |
| 27 | `xjdf4s.intents.MediaLayout` | 4 | modules | 0.67 |
| 28 | `xjdf4s.model.Audit` | 4 | modules | 0.36 |
| 29 | `xjdf4s.model.Intent` | 4 | modules | 0.50 |
| 30 | `xjdf4s.resources.Delivery` | 4 | modules | 0.67 |
| 31 | `xjdf4s.intents.ColorProduction` | 4 | modules | 0.67 |
| 32 | `xjdf4s.intents.Laminating` | 3 | modules | 0.75 |
| 33 | `xjdf4s.intents.Embossing` | 3 | modules | 0.60 |
| 34 | `xjdf4s.resources.Color` | 3 | modules | 0.60 |
| 35 | `xjdf4s.resources.Preview` | 2 | modules | 0.50 |
| 36 | `xjdf4s.model.ValidationTypes` | 2 | modules | 0.10 |
| 37 | `xjdf4s.prim.Enums` | 2 | modules | 0.07 |
| 38 | `xjdf4s.resources.Device` | 2 | modules | 0.67 |
| 39 | `xjdf4s.prim.Common` | 1 | modules | 0.13 |
| 40 | `xjdf4s.resources.Contact` | 1 | modules | 0.50 |
| 41 | `xjdf4s.intents.HoleMaking` | 1 | modules | 0.33 |
| 42 | `xjdf4s.model.IdSource` | 1 | modules | 0.50 |
| 43 | `xjdf4s.prim.Ids` | 0 | modules | 0.00 |
| 44 | `xjdf4s.prim.Versions` | 0 | modules | 0.00 |
| 45 | `xjdf4s.prim.Time` | 0 | modules | 0.00 |
| 46 | `xjdf4s.prim.Tokens` | 0 | modules | 0.00 |
| 47 | `xjdf4s.prim.Quantity` | 0 | modules | 0.00 |

---

## 🚨 Полный рейтинг Betweenness Centrality

| # | File | Betweenness | Fan-In | Fan-Out | Module |
|---|---|---|---|---|---|
| 1 | `xjdf4s.resources.AllResources` | 98.00 | 4 | 13 | modules |
| 2 | `xjdf4s.model.Resource` | 85.97 | 8 | 11 | modules |
| 3 | `xjdf4s.intents.AllIntents` | 72.39 | 4 | 11 | modules |
| 4 | `xjdf4s.model.elements.CommonElements` | 59.43 | 23 | 7 | modules |
| 5 | `xjdf4s.model.Intent` | 38.50 | 4 | 4 | modules |
| 6 | `xjdf4s.model.Ticket` | 37.29 | 6 | 10 | modules |
| 7 | `xjdf4s.model.Product` | 34.88 | 7 | 6 | modules |
| 8 | `xjdf4s.model.TicketValidator` | 29.23 | 2 | 31 | modules |
| 9 | `xjdf4s.examples.SpecExamples` | 27.92 | 1 | 22 | modules |
| 10 | `xjdf4s.model.Header` | 21.42 | 3 | 8 | modules |
| 11 | `xjdf4s.model.Audit` | 7.18 | 7 | 4 | modules |
| 12 | `xjdf4s.resources.Finishing` | 6.23 | 2 | 5 | modules |
| 13 | `xjdf4s.dsl.XjdfDsl` | 6.00 | 1 | 21 | modules |
| 14 | `xjdf4s.intents.ContentCheck` | 3.28 | 2 | 5 | modules |
| 15 | `xjdf4s.prim.Enums` | 3.13 | 26 | 2 | modules |
| 16 | `xjdf4s.model.ChangeOrder` | 3.00 | 1 | 9 | modules |
| 17 | `xjdf4s.model.ValidationTypes` | 2.75 | 18 | 2 | modules |
| 18 | `xjdf4s.intents.FoldingVariable` | 2.54 | 3 | 6 | modules |
| 19 | `xjdf4s.intents.Binding` | 1.42 | 2 | 6 | modules |
| 20 | `xjdf4s.model.Partition` | 1.26 | 7 | 4 | modules |
| 21 | `xjdf4s.intents.MediaLayout` | 1.08 | 2 | 4 | modules |
| 22 | `xjdf4s.intents.ColorProduction` | 1.08 | 2 | 4 | modules |
| 23 | `xjdf4s.resources.NodeInfo` | 1.08 | 2 | 5 | modules |
| 24 | `xjdf4s.resources.Device` | 1.00 | 1 | 2 | modules |
| 25 | `xjdf4s.resources.Delivery` | 0.93 | 2 | 4 | modules |
| 26 | `xjdf4s.intents.ShapeCutting` | 0.75 | 2 | 5 | modules |
| 27 | `xjdf4s.resources.Media` | 0.73 | 4 | 5 | modules |
| 28 | `xjdf4s.resources.Component` | 0.73 | 4 | 5 | modules |
| 29 | `xjdf4s.resources.RunList` | 0.73 | 3 | 5 | modules |
| 30 | `xjdf4s.intents.HoleMaking` | 0.67 | 2 | 1 | modules |
| 31 | `xjdf4s.resources.Layout` | 0.61 | 2 | 5 | modules |
| 32 | `xjdf4s.intents.Embossing` | 0.42 | 2 | 3 | modules |
| 33 | `xjdf4s.intents.Laminating` | 0.36 | 1 | 3 | modules |
| 34 | `xjdf4s.resources.Color` | 0.34 | 2 | 3 | modules |
| 35 | `xjdf4s.prim.Common` | 0.33 | 7 | 1 | modules |
| 36 | `xjdf4s.resources.Preview` | 0.27 | 2 | 2 | modules |
| 37 | `xjdf4s.model.Amounts` | 0.06 | 3 | 5 | modules |

---

## 🔄 Циклические зависимости (детальный разбор)

✅ Циклических зависимостей не обнаружено.

---

## 💀 Изолированные файлы (Dead Nodes)

✅ Изолированных файлов нет.

---

## 🍃 Листовые узлы (Fan-In = 0, Fan-Out > 0)

Всего: **1**

| File | Fan-Out | Module |
|---|---|---|
| `xjdf4s.examples.Main` | 9 | modules |

---

## 🌳 Корневые узлы (Fan-In > 0, Fan-Out = 0)

Всего: **5**

| File | Fan-In | Module |
|---|---|---|
| `xjdf4s.prim.Tokens` | 37 | modules |
| `xjdf4s.prim.Ids` | 24 | modules |
| `xjdf4s.prim.Quantity` | 22 | modules |
| `xjdf4s.prim.Time` | 8 | modules |
| `xjdf4s.prim.Versions` | 2 | modules |

---

## ⚖️ Stability vs Importance (Зонный анализ)

Файл с высоким Fan-In и высокой Instability — это архитектурное нарушение.

✅ Нарушений принципа стабильных зависимостей не обнаружено.

---

## 📋 Автоматические рекомендации

### 🔴 Приоритет: Высокий


### 🟡 Приоритет: Средний

4. **Разбить 1 God Objects** (Fan-Out > 25) на более мелкие модули.

### 🟢 Приоритет: Низкий

6. **Добавить тесты** для топ-10 файлов по Fan-In.
7. **Документировать** интерфейсы файлов с Betweenness > 21.416666666666668.
