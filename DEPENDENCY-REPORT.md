# 📊 Architecture Graph Analysis — Full Report

> **Сгенерировано:** 2026-08-18T00:19:07.243627
> **Узлов (файлов):** 100 | **Рёбер (зависимостей):** 950 | **Модулей:** 7 | **Циклов:** 4

## 🎯 Executive Summary

### Ключевые находки

1. **Циклические зависимости:** Обнаружено **4** циклов. Это нарушает модульность и требует вмешательства.
2. **Узкие места (Bottlenecks):** 5 файлов имеют аномально высокую Betweenness Centrality. Главный: `xjdf4s.model.Resource` (score: 2541.479446286827).
3. **God Objects:** 5 файлов имеют Fan-Out > 25. Худший: `xjdf4s.codec.xml.domain.DerivedInstances` (Fan-Out: 50).
4. **Фундамент:** 2 файлов имеют Fan-In > 50. Они должны быть максимально стабильными.
5. **Изолированные файлы:** 0 файлов (0.0%) не имеют связей. Кандидаты на удаление.
6. **Средний Fan-In:** 9.5 | **Средний Fan-Out:** 9.5

---

## 📈 Распределение метрик


### Fan-In (входящие зависимости)

| Квантиль | Значение |
|---|---|
| Min | 0 |
| P25 | 3 |
| Median (P50) | 5 |
| P75 | 12 |
| P90 | 20 |
| P95 | 38 |
| Max | 73 |
| Mean | 9.5 |

### Fan-In: Гистограмма

| Диапазон | Количество файлов | Процент |
|---|---|---|
| 0–0 | 11 | 11.0% |
| 1–5 | 45 | 45.0% |
| 6–10 | 14 | 14.0% |
| 11–20 | 20 | 20.0% |
| 21–50 | 8 | 8.0% |
| 51–100 | 2 | 2.0% |
| 101+ | 0 | 0.0% |

### Fan-Out (исходящие зависимости)

| Квантиль | Значение |
|---|---|
| Min | 0 |
| P25 | 5 |
| Median (P50) | 8 |
| P75 | 10 |
| P90 | 16 |
| P95 | 23 |
| Max | 50 |
| Mean | 9.5 |

### Betweenness Centrality

- Файлов с Betweenness > 0: **82** из 100 (82.0%)
- Медиана (из ненулевых): 11.020075757575757
- P90 (из ненулевых): 88.12447330513814
- Max: 2541.48

---

## 📦 Анализ по модулям

| Модуль | Файлов | Ср. Fan-In | Ср. Fan-Out | Ср. Betweenness | Ср. Instability | Внутр. связей | Внешн. связей |
|---|---|---|---|---|---|---|---|
| `codec-json` | 6 | 1.7 | 10.2 | 26.8 | 0.84 | 10 | 51 |
| `codec-xml` | 26 | 4.5 | 13.9 | 18.5 | 0.70 | 116 | 246 |
| `core` | 6 | 43.0 | 0.8 | 6.7 | 0.03 | 5 | 0 |
| `dsl` | 2 | 0.5 | 8.0 | 0.0 | 0.93 | 1 | 15 |
| `messaging` | 9 | 5.6 | 6.4 | 21.7 | 0.56 | 18 | 40 |
| `model` | 50 | 10.3 | 8.9 | 99.1 | 0.52 | 282 | 162 |
| `protocol` | 1 | 0.0 | 4.0 | 0.0 | 1.00 | 0 | 4 |

---

## 🔗 Матрица межмодульных зависимостей

Строки = откуда, столбцы = куда. Число = количество файловых зависимостей.

| from \ to | `codec-json` | `codec-xml` | `core` | `dsl` | `messaging` | `model` | `protocol` |
|---|---|---|---|---|---|---|---|
| **codec-json** | **10** | · | **13** | · | **5** | **33** | · |
| **codec-xml** | · | **116** | **48** | · | **25** | **173** | · |
| **core** | · | · | **5** | · | · | · | · |
| **dsl** | · | · | **8** | **1** | · | **7** | · |
| **messaging** | · | · | **21** | · | **18** | **19** | · |
| **model** | · | · | **162** | · | · | **282** | · |
| **protocol** | · | · | **1** | · | **2** | **1** | · |

---

## 🔥 Risk Ranking (все файлы)

Risk Score = (Fan-Out × 0.3 + Instability × 50 × 0.3 + Betweenness × 0.4). Чем выше — тем опаснее.

| # | File | Module | Fan-In | Fan-Out | Betweenness | Instability | **Risk** |
|---|---|---|---|---|---|---|---|
| 1 | `xjdf4s.model.Resource` | model | 48 | 39 | 2541.5 | 0.45 | 🔴 **1035.0** |
| 2 | `xjdf4s.model.Subelements` | model | 33 | 7 | 611.6 | 0.18 | 🔴 **249.4** |
| 3 | `xjdf4s.model.ContentAndShapeIntents` | model | 23 | 8 | 402.1 | 0.26 | 🔴 **167.1** |
| 4 | `xjdf4s.codec.xml.Lexical` | codec-xml | 12 | 18 | 238.9 | 0.60 | 🔴 **109.9** |
| 5 | `xjdf4s.model.Product` | model | 13 | 10 | 239.8 | 0.43 | 🔴 **105.5** |
| 6 | `xjdf4s.model.resources.MediaAndColor` | model | 15 | 15 | 215.3 | 0.50 | 🟡 **98.1** |
| 7 | `xjdf4s.codec.xml.domain.Registry` | codec-xml | 3 | 50 | 76.7 | 0.94 | 🟡 **59.8** |
| 8 | `xjdf4s.codec.xml.derivation.FieldCodec` | codec-xml | 6 | 23 | 88.8 | 0.79 | 🟡 **54.3** |
| 9 | `xjdf4s.model.resources.Device` | model | 16 | 9 | 114.6 | 0.36 | 🟡 **53.9** |
| 10 | `xjdf4s.model.resources.ProcessResources` | model | 13 | 13 | 88.1 | 0.50 | 🟡 **46.6** |
| 11 | `xjdf4s.model.Partition` | model | 39 | 5 | 102.5 | 0.11 | 🟡 **44.2** |
| 12 | `xjdf4s.model.resources.AdditionalResources` | model | 8 | 14 | 71.8 | 0.64 | 🟡 **42.5** |
| 13 | `xjdf4s.model.XJDF` | model | 7 | 9 | 66.4 | 0.56 | 🟡 **37.7** |
| 14 | `xjdf4s.messaging.KnownMessages` | messaging | 9 | 8 | 68.0 | 0.47 | 🟡 **36.7** |
| 15 | `xjdf4s.codec.json.JsonMessagingCodecs` | codec-json | 1 | 13 | 40.4 | 0.93 | 🟡 **34.0** |
| 16 | `xjdf4s.model.resources.FoundationalResources` | model | 14 | 9 | 63.5 | 0.39 | 🟡 **34.0** |
| 17 | `xjdf4s.model.resources.MarksAndStacking` | model | 6 | 15 | 42.1 | 0.71 | 🟡 **32.1** |
| 18 | `xjdf4s.codec.xml.domain.DerivedInstances` | codec-xml | 0 | 50 | 0.0 | 1.00 | 🟢 **30.0** |
| 19 | `xjdf4s.codec.json.JsonCodec` | codec-json | 5 | 4 | 55.3 | 0.44 | 🟢 **30.0** |
| 20 | `xjdf4s.codec.json.JsonNodeCodecs` | codec-json | 1 | 10 | 29.8 | 0.91 | 🟢 **28.5** |
| 21 | `xjdf4s.codec.json.JsonMediaCodecs` | codec-json | 1 | 16 | 20.1 | 0.94 | 🟢 **27.0** |
| 22 | `xjdf4s.model.resources.SimpleResources` | model | 10 | 8 | 44.1 | 0.44 | 🟢 **26.7** |
| 23 | `xjdf4s.messaging.ControlMessages` | messaging | 4 | 7 | 35.5 | 0.64 | 🟢 **25.8** |
| 24 | `xjdf4s.codec.xml.domain.IntentAndAuditCodecs` | codec-xml | 0 | 36 | 0.0 | 1.00 | 🟢 **25.8** |
| 25 | `xjdf4s.messaging.Message` | messaging | 12 | 4 | 49.7 | 0.25 | 🟢 **24.8** |
| 26 | `xjdf4s.model.resources.PostpressResources` | model | 3 | 9 | 24.7 | 0.75 | 🟢 **23.8** |
| 27 | `xjdf4s.codec.xml.domain.ResourceCodecs` | codec-xml | 2 | 19 | 11.4 | 0.90 | 🟢 **23.8** |
| 28 | `xjdf4s.messaging.StatusNotificationResourceMessages` | messaging | 7 | 10 | 29.8 | 0.59 | 🟢 **23.7** |
| 29 | `xjdf4s.codec.xml.domain.HandWrappers` | codec-xml | 0 | 27 | 0.0 | 1.00 | 🟢 **23.1** |
| 30 | `xjdf4s.codec.xml.domain.MessagingCodecs` | codec-xml | 2 | 19 | 8.6 | 0.90 | 🟢 **22.7** |
| 31 | `xjdf4s.model.resources.QualityControl` | model | 5 | 12 | 20.4 | 0.71 | 🟢 **22.3** |
| 32 | `xjdf4s.model.resources.SheetOptimizing` | model | 4 | 12 | 18.1 | 0.75 | 🟢 **22.1** |
| 33 | `xjdf4s.codec.json.JsonScalars` | codec-json | 1 | 11 | 11.0 | 0.92 | 🟢 **21.5** |
| 34 | `xjdf4s.codec.xml.domain.SpecialCodecs` | codec-xml | 0 | 19 | 0.0 | 1.00 | 🟢 **20.7** |
| 35 | `xjdf4s.model.resources.ShapeDefinitionResources` | model | 4 | 10 | 16.7 | 0.71 | 🟢 **20.4** |
| 36 | `xjdf4s.model.resources.Content` | model | 4 | 15 | 10.0 | 0.79 | 🟢 **20.4** |
| 37 | `xjdf4s.model.resources.MorePostpressResources` | model | 3 | 7 | 18.2 | 0.70 | 🟢 **19.9** |
| 38 | `xjdf4s.model.resources.RemainingPostpressResources` | model | 5 | 9 | 18.7 | 0.64 | 🟢 **19.8** |
| 39 | `xjdf4s.model.FinishingIntents` | model | 13 | 9 | 27.4 | 0.41 | 🟢 **19.8** |
| 40 | `xjdf4s.model.SimpleIntents` | model | 20 | 7 | 33.3 | 0.26 | 🟢 **19.3** |
| 41 | `xjdf4s.codec.xml.domain.CodecHelpers` | codec-xml | 11 | 9 | 24.5 | 0.45 | 🟢 **19.3** |
| 42 | `xjdf4s.codec.xml.domain.MediaCodec` | codec-xml | 3 | 15 | 4.5 | 0.83 | 🟢 **18.8** |
| 43 | `xjdf4s.model.resources.BinderySignature` | model | 4 | 9 | 13.4 | 0.69 | 🟢 **18.4** |
| 44 | `xjdf4s.model.resources.RunList` | model | 7 | 9 | 17.9 | 0.56 | 🟢 **18.3** |
| 45 | `xjdf4s.model.resources.Layout` | model | 4 | 16 | 3.7 | 0.80 | 🟢 **18.3** |
| 46 | `xjdf4s.dsl.DocInterpreters` | dsl | 0 | 10 | 0.0 | 1.00 | 🟢 **18.0** |
| 47 | `xjdf4s.codec.xml.domain.SimpleResourceCodecs` | codec-xml | 1 | 12 | 0.6 | 0.92 | 🟢 **17.7** |
| 48 | `xjdf4s.codec.xml.domain.ReferenceCheck` | codec-xml | 0 | 7 | 0.0 | 1.00 | 🟢 **17.1** |
| 49 | `xjdf4s.codec.xml.domain.ColorCodec` | codec-xml | 1 | 10 | 0.9 | 0.91 | 🟢 **17.0** |
| 50 | `xjdf4s.codec.json.JsonResources` | codec-json | 1 | 7 | 4.0 | 0.88 | 🟢 **16.8** |
| 51 | `xjdf4s.model.DocumentValidation` | model | 0 | 6 | 0.0 | 1.00 | 🟢 **16.8** |
| 52 | `xjdf4s.messaging.StandardMessages` | messaging | 0 | 5 | 0.0 | 1.00 | 🟢 **16.5** |
| 53 | `xjdf4s.model.resources.FoldingResources` | model | 4 | 9 | 8.3 | 0.69 | 🟢 **16.4** |
| 54 | `xjdf4s.Protocol` | protocol | 0 | 4 | 0.0 | 1.00 | 🟢 **16.2** |
| 55 | `xjdf4s.codec.xml.XmlParser` | codec-xml | 0 | 3 | 0.0 | 1.00 | 🟢 **15.9** |
| 56 | `xjdf4s.codec.xml.derivation.Derived` | codec-xml | 3 | 7 | 8.2 | 0.70 | 🟢 **15.9** |
| 57 | `xjdf4s.codec.xml.XmlWriter` | codec-xml | 0 | 2 | 0.0 | 1.00 | 🟢 **15.6** |
| 58 | `xjdf4s.model.AssemblingIntent` | model | 15 | 7 | 21.5 | 0.32 | 🟢 **15.5** |
| 59 | `xjdf4s.model.resources.DeliveryAndPreflightResources` | model | 4 | 8 | 7.4 | 0.67 | 🟢 **15.3** |
| 60 | `xjdf4s.model.resources.DieLayout` | model | 3 | 10 | 1.9 | 0.77 | 🟢 **15.3** |
| 61 | `xjdf4s.model.resources.Rendering` | model | 5 | 9 | 7.2 | 0.64 | 🟢 **15.2** |
| 62 | `xjdf4s.codec.xml.domain.PartCodecs` | codec-xml | 3 | 10 | 1.4 | 0.77 | 🟢 **15.1** |
| 63 | `xjdf4s.model.resources.Interpreting` | model | 6 | 10 | 6.3 | 0.63 | 🟢 **14.9** |
| 64 | `xjdf4s.dsl.DocOp` | dsl | 1 | 6 | 0.0 | 0.86 | 🟢 **14.7** |
| 65 | `xjdf4s.messaging.QueueEntryMessages` | messaging | 4 | 8 | 4.1 | 0.67 | 🟢 **14.1** |
| 66 | `xjdf4s.model.resources.PdlCreation` | model | 3 | 8 | 1.8 | 0.73 | 🟢 **14.0** |
| 67 | `xjdf4s.codec.xml.domain.CoreNodeCodecs` | codec-xml | 2 | 7 | 0.7 | 0.78 | 🟢 **14.0** |
| 68 | `xjdf4s.codec.xml.domain.GeneralIdCodec` | codec-xml | 2 | 7 | 0.7 | 0.78 | 🟢 **14.0** |
| 69 | `xjdf4s.model.BindingIntent` | model | 7 | 8 | 9.0 | 0.53 | 🟢 **14.0** |
| 70 | `xjdf4s.model.resources.Contact` | model | 4 | 5 | 9.8 | 0.56 | 🟢 **13.7** |
| 71 | `xjdf4s.messaging.GangAndQueueStatusMessages` | messaging | 5 | 8 | 5.1 | 0.62 | 🟢 **13.7** |
| 72 | `xjdf4s.model.resources.MissingSchemaResources` | model | 3 | 8 | 0.6 | 0.73 | 🟢 **13.6** |
| 73 | `xjdf4s.model.resources.GeneralAndPressResources` | model | 5 | 8 | 4.7 | 0.62 | 🟢 **13.5** |
| 74 | `xjdf4s.model.MediaIntent` | model | 9 | 7 | 12.1 | 0.44 | 🟢 **13.5** |
| 75 | `xjdf4s.model.resources.SmallProductionResources` | model | 3 | 7 | 1.8 | 0.70 | 🟢 **13.3** |
| 76 | `xjdf4s.messaging.XJMF` | messaging | 4 | 7 | 3.0 | 0.64 | 🟢 **12.9** |
| 77 | `xjdf4s.model.resources.ColorSpaceConversion` | model | 4 | 7 | 2.0 | 0.64 | 🟢 **12.4** |
| 78 | `xjdf4s.model.resources.DieLayoutProduction` | model | 8 | 8 | 5.1 | 0.50 | 🟢 **11.9** |
| 79 | `xjdf4s.model.resources.PrepressResources` | model | 8 | 8 | 4.9 | 0.50 | 🟢 **11.9** |
| 80 | `xjdf4s.model.resources.Identification` | model | 13 | 7 | 10.1 | 0.35 | 🟢 **11.4** |
| 81 | `xjdf4s.model.resources.ImageCompression` | model | 4 | 6 | 0.8 | 0.60 | 🟢 **11.1** |
| 82 | `xjdf4s.model.resources.ColorantControl` | model | 3 | 5 | 0.7 | 0.63 | 🟢 **11.1** |
| 83 | `xjdf4s.codec.xml.XmlDecoders` | codec-xml | 11 | 6 | 9.4 | 0.35 | 🟢 **10.9** |
| 84 | `xjdf4s.model.Audit` | model | 6 | 3 | 11.2 | 0.33 | 🟢 **10.4** |
| 85 | `xjdf4s.core.Extension` | core | 73 | 1 | 20.8 | 0.01 | 🟢 **8.8** |
| 86 | `xjdf4s.codec.xml.ForeignCodec` | codec-xml | 5 | 3 | 1.2 | 0.38 | 🟢 **7.0** |
| 87 | `xjdf4s.core.Primitives` | core | 73 | 1 | 13.3 | 0.01 | 🟢 **5.8** |
| 88 | `xjdf4s.codec.xml.XmlCodec` | codec-xml | 16 | 2 | 2.6 | 0.11 | 🟢 **3.3** |
| 89 | `xjdf4s.model.Header` | model | 12 | 2 | 0.7 | 0.14 | 🟢 **3.0** |
| 90 | `xjdf4s.core.Common` | core | 16 | 2 | 1.4 | 0.11 | 🟢 **2.8** |
| 91 | `xjdf4s.messaging.MessageNames` | messaging | 5 | 1 | 0.0 | 0.17 | 🟢 **2.8** |
| 92 | `xjdf4s.core.Cardinality` | core | 34 | 1 | 4.6 | 0.03 | 🟢 **2.6** |
| 93 | `xjdf4s.codec.xml.Xml` | codec-xml | 19 | 1 | 2.9 | 0.05 | 🟢 **2.2** |
| 94 | `xjdf4s.model.TypedValues` | model | 13 | 1 | 1.3 | 0.07 | 🟢 **1.9** |
| 95 | `xjdf4s.model.ColorValues` | model | 14 | 1 | 1.5 | 0.07 | 🟢 **1.9** |
| 96 | `xjdf4s.model.XjdfNames` | model | 38 | 1 | 0.0 | 0.03 | 🟢 **0.7** |
| 97 | `xjdf4s.core.Validation` | core | 23 | 0 | 0.0 | 0.00 | 🟢 **0.0** |
| 98 | `xjdf4s.codec.xml.XmlError` | codec-xml | 13 | 0 | 0.0 | 0.00 | 🟢 **0.0** |
| 99 | `xjdf4s.codec.xml.derivation.Names` | codec-xml | 1 | 0 | 0.0 | 0.00 | 🟢 **0.0** |
| 100 | `xjdf4s.core.Values` | core | 39 | 0 | 0.0 | 0.00 | 🟢 **0.0** |

---

## 🧱 Полный рейтинг Fan-In

| # | File | Fan-In | Module | Instability |
|---|---|---|---|---|
| 1 | `xjdf4s.core.Primitives` | 73 | core | 0.01 |
| 2 | `xjdf4s.core.Extension` | 73 | core | 0.01 |
| 3 | `xjdf4s.model.Resource` | 48 | model | 0.45 |
| 4 | `xjdf4s.model.Partition` | 39 | model | 0.11 |
| 5 | `xjdf4s.core.Values` | 39 | core | 0.00 |
| 6 | `xjdf4s.model.XjdfNames` | 38 | model | 0.03 |
| 7 | `xjdf4s.core.Cardinality` | 34 | core | 0.03 |
| 8 | `xjdf4s.model.Subelements` | 33 | model | 0.18 |
| 9 | `xjdf4s.core.Validation` | 23 | core | 0.00 |
| 10 | `xjdf4s.model.ContentAndShapeIntents` | 23 | model | 0.26 |
| 11 | `xjdf4s.model.SimpleIntents` | 20 | model | 0.26 |
| 12 | `xjdf4s.codec.xml.Xml` | 19 | codec-xml | 0.05 |
| 13 | `xjdf4s.core.Common` | 16 | core | 0.11 |
| 14 | `xjdf4s.model.resources.Device` | 16 | model | 0.36 |
| 15 | `xjdf4s.codec.xml.XmlCodec` | 16 | codec-xml | 0.11 |
| 16 | `xjdf4s.model.resources.MediaAndColor` | 15 | model | 0.50 |
| 17 | `xjdf4s.model.AssemblingIntent` | 15 | model | 0.32 |
| 18 | `xjdf4s.model.ColorValues` | 14 | model | 0.07 |
| 19 | `xjdf4s.model.resources.FoundationalResources` | 14 | model | 0.39 |
| 20 | `xjdf4s.model.TypedValues` | 13 | model | 0.07 |
| 21 | `xjdf4s.model.resources.Identification` | 13 | model | 0.35 |
| 22 | `xjdf4s.codec.xml.XmlError` | 13 | codec-xml | 0.00 |
| 23 | `xjdf4s.model.Product` | 13 | model | 0.43 |
| 24 | `xjdf4s.model.resources.ProcessResources` | 13 | model | 0.50 |
| 25 | `xjdf4s.model.FinishingIntents` | 13 | model | 0.41 |
| 26 | `xjdf4s.model.Header` | 12 | model | 0.14 |
| 27 | `xjdf4s.messaging.Message` | 12 | messaging | 0.25 |
| 28 | `xjdf4s.codec.xml.Lexical` | 12 | codec-xml | 0.60 |
| 29 | `xjdf4s.codec.xml.domain.CodecHelpers` | 11 | codec-xml | 0.45 |
| 30 | `xjdf4s.codec.xml.XmlDecoders` | 11 | codec-xml | 0.35 |
| 31 | `xjdf4s.model.resources.SimpleResources` | 10 | model | 0.44 |
| 32 | `xjdf4s.messaging.KnownMessages` | 9 | messaging | 0.47 |
| 33 | `xjdf4s.model.MediaIntent` | 9 | model | 0.44 |
| 34 | `xjdf4s.model.resources.AdditionalResources` | 8 | model | 0.64 |
| 35 | `xjdf4s.model.resources.DieLayoutProduction` | 8 | model | 0.50 |
| 36 | `xjdf4s.model.resources.PrepressResources` | 8 | model | 0.50 |
| 37 | `xjdf4s.model.XJDF` | 7 | model | 0.56 |
| 38 | `xjdf4s.model.resources.RunList` | 7 | model | 0.56 |
| 39 | `xjdf4s.model.BindingIntent` | 7 | model | 0.53 |
| 40 | `xjdf4s.messaging.StatusNotificationResourceMessages` | 7 | messaging | 0.59 |
| 41 | `xjdf4s.model.resources.Interpreting` | 6 | model | 0.63 |
| 42 | `xjdf4s.model.Audit` | 6 | model | 0.33 |
| 43 | `xjdf4s.model.resources.MarksAndStacking` | 6 | model | 0.71 |
| 44 | `xjdf4s.codec.xml.derivation.FieldCodec` | 6 | codec-xml | 0.79 |
| 45 | `xjdf4s.model.resources.GeneralAndPressResources` | 5 | model | 0.62 |
| 46 | `xjdf4s.model.resources.Rendering` | 5 | model | 0.64 |
| 47 | `xjdf4s.model.resources.RemainingPostpressResources` | 5 | model | 0.64 |
| 48 | `xjdf4s.messaging.MessageNames` | 5 | messaging | 0.17 |
| 49 | `xjdf4s.messaging.GangAndQueueStatusMessages` | 5 | messaging | 0.62 |
| 50 | `xjdf4s.codec.xml.ForeignCodec` | 5 | codec-xml | 0.38 |
| 51 | `xjdf4s.model.resources.QualityControl` | 5 | model | 0.71 |
| 52 | `xjdf4s.codec.json.JsonCodec` | 5 | codec-json | 0.44 |
| 53 | `xjdf4s.model.resources.SheetOptimizing` | 4 | model | 0.75 |
| 54 | `xjdf4s.model.resources.BinderySignature` | 4 | model | 0.69 |
| 55 | `xjdf4s.model.resources.Content` | 4 | model | 0.79 |
| 56 | `xjdf4s.model.resources.Contact` | 4 | model | 0.56 |
| 57 | `xjdf4s.messaging.XJMF` | 4 | messaging | 0.64 |
| 58 | `xjdf4s.model.resources.DeliveryAndPreflightResources` | 4 | model | 0.67 |
| 59 | `xjdf4s.model.resources.ColorSpaceConversion` | 4 | model | 0.64 |
| 60 | `xjdf4s.model.resources.Layout` | 4 | model | 0.80 |
| 61 | `xjdf4s.model.resources.FoldingResources` | 4 | model | 0.69 |
| 62 | `xjdf4s.model.resources.ShapeDefinitionResources` | 4 | model | 0.71 |
| 63 | `xjdf4s.messaging.QueueEntryMessages` | 4 | messaging | 0.67 |
| 64 | `xjdf4s.model.resources.ImageCompression` | 4 | model | 0.60 |
| 65 | `xjdf4s.messaging.ControlMessages` | 4 | messaging | 0.64 |
| 66 | `xjdf4s.model.resources.PostpressResources` | 3 | model | 0.75 |
| 67 | `xjdf4s.codec.xml.derivation.Derived` | 3 | codec-xml | 0.70 |
| 68 | `xjdf4s.codec.xml.domain.PartCodecs` | 3 | codec-xml | 0.77 |
| 69 | `xjdf4s.model.resources.DieLayout` | 3 | model | 0.77 |
| 70 | `xjdf4s.model.resources.ColorantControl` | 3 | model | 0.63 |
| 71 | `xjdf4s.codec.xml.domain.MediaCodec` | 3 | codec-xml | 0.83 |
| 72 | `xjdf4s.model.resources.PdlCreation` | 3 | model | 0.73 |
| 73 | `xjdf4s.codec.xml.domain.Registry` | 3 | codec-xml | 0.94 |
| 74 | `xjdf4s.model.resources.SmallProductionResources` | 3 | model | 0.70 |
| 75 | `xjdf4s.model.resources.MorePostpressResources` | 3 | model | 0.70 |
| 76 | `xjdf4s.model.resources.MissingSchemaResources` | 3 | model | 0.73 |
| 77 | `xjdf4s.codec.xml.domain.ResourceCodecs` | 2 | codec-xml | 0.90 |
| 78 | `xjdf4s.codec.xml.domain.CoreNodeCodecs` | 2 | codec-xml | 0.78 |
| 79 | `xjdf4s.codec.xml.domain.MessagingCodecs` | 2 | codec-xml | 0.90 |
| 80 | `xjdf4s.codec.xml.domain.GeneralIdCodec` | 2 | codec-xml | 0.78 |
| 81 | `xjdf4s.dsl.DocOp` | 1 | dsl | 0.86 |
| 82 | `xjdf4s.codec.xml.domain.SimpleResourceCodecs` | 1 | codec-xml | 0.92 |
| 83 | `xjdf4s.codec.json.JsonNodeCodecs` | 1 | codec-json | 0.91 |
| 84 | `xjdf4s.codec.xml.domain.ColorCodec` | 1 | codec-xml | 0.91 |
| 85 | `xjdf4s.codec.json.JsonMediaCodecs` | 1 | codec-json | 0.94 |
| 86 | `xjdf4s.codec.json.JsonScalars` | 1 | codec-json | 0.92 |
| 87 | `xjdf4s.codec.json.JsonResources` | 1 | codec-json | 0.88 |
| 88 | `xjdf4s.codec.json.JsonMessagingCodecs` | 1 | codec-json | 0.93 |
| 89 | `xjdf4s.codec.xml.derivation.Names` | 1 | codec-xml | 0.00 |
| 90 | `xjdf4s.codec.xml.XmlWriter` | 0 | codec-xml | 1.00 |
| 91 | `xjdf4s.Protocol` | 0 | protocol | 1.00 |
| 92 | `xjdf4s.codec.xml.domain.DerivedInstances` | 0 | codec-xml | 1.00 |
| 93 | `xjdf4s.codec.xml.XmlParser` | 0 | codec-xml | 1.00 |
| 94 | `xjdf4s.messaging.StandardMessages` | 0 | messaging | 1.00 |
| 95 | `xjdf4s.codec.xml.domain.HandWrappers` | 0 | codec-xml | 1.00 |
| 96 | `xjdf4s.codec.xml.domain.SpecialCodecs` | 0 | codec-xml | 1.00 |
| 97 | `xjdf4s.codec.xml.domain.IntentAndAuditCodecs` | 0 | codec-xml | 1.00 |
| 98 | `xjdf4s.model.DocumentValidation` | 0 | model | 1.00 |
| 99 | `xjdf4s.dsl.DocInterpreters` | 0 | dsl | 1.00 |
| 100 | `xjdf4s.codec.xml.domain.ReferenceCheck` | 0 | codec-xml | 1.00 |

---

## 🕸️ Полный рейтинг Fan-Out

| # | File | Fan-Out | Module | Instability |
|---|---|---|---|---|
| 1 | `xjdf4s.codec.xml.domain.DerivedInstances` | 50 | codec-xml | 1.00 |
| 2 | `xjdf4s.codec.xml.domain.Registry` | 50 | codec-xml | 0.94 |
| 3 | `xjdf4s.model.Resource` | 39 | model | 0.45 |
| 4 | `xjdf4s.codec.xml.domain.IntentAndAuditCodecs` | 36 | codec-xml | 1.00 |
| 5 | `xjdf4s.codec.xml.domain.HandWrappers` | 27 | codec-xml | 1.00 |
| 6 | `xjdf4s.codec.xml.derivation.FieldCodec` | 23 | codec-xml | 0.79 |
| 7 | `xjdf4s.codec.xml.domain.ResourceCodecs` | 19 | codec-xml | 0.90 |
| 8 | `xjdf4s.codec.xml.domain.SpecialCodecs` | 19 | codec-xml | 1.00 |
| 9 | `xjdf4s.codec.xml.domain.MessagingCodecs` | 19 | codec-xml | 0.90 |
| 10 | `xjdf4s.codec.xml.Lexical` | 18 | codec-xml | 0.60 |
| 11 | `xjdf4s.model.resources.Layout` | 16 | model | 0.80 |
| 12 | `xjdf4s.codec.json.JsonMediaCodecs` | 16 | codec-json | 0.94 |
| 13 | `xjdf4s.model.resources.MediaAndColor` | 15 | model | 0.50 |
| 14 | `xjdf4s.model.resources.Content` | 15 | model | 0.79 |
| 15 | `xjdf4s.codec.xml.domain.MediaCodec` | 15 | codec-xml | 0.83 |
| 16 | `xjdf4s.model.resources.MarksAndStacking` | 15 | model | 0.71 |
| 17 | `xjdf4s.model.resources.AdditionalResources` | 14 | model | 0.64 |
| 18 | `xjdf4s.model.resources.ProcessResources` | 13 | model | 0.50 |
| 19 | `xjdf4s.codec.json.JsonMessagingCodecs` | 13 | codec-json | 0.93 |
| 20 | `xjdf4s.model.resources.SheetOptimizing` | 12 | model | 0.75 |
| 21 | `xjdf4s.codec.xml.domain.SimpleResourceCodecs` | 12 | codec-xml | 0.92 |
| 22 | `xjdf4s.model.resources.QualityControl` | 12 | model | 0.71 |
| 23 | `xjdf4s.codec.json.JsonScalars` | 11 | codec-json | 0.92 |
| 24 | `xjdf4s.codec.xml.domain.PartCodecs` | 10 | codec-xml | 0.77 |
| 25 | `xjdf4s.model.resources.DieLayout` | 10 | model | 0.77 |
| 26 | `xjdf4s.model.resources.Interpreting` | 10 | model | 0.63 |
| 27 | `xjdf4s.codec.json.JsonNodeCodecs` | 10 | codec-json | 0.91 |
| 28 | `xjdf4s.model.Product` | 10 | model | 0.43 |
| 29 | `xjdf4s.codec.xml.domain.ColorCodec` | 10 | codec-xml | 0.91 |
| 30 | `xjdf4s.model.resources.ShapeDefinitionResources` | 10 | model | 0.71 |
| 31 | `xjdf4s.messaging.StatusNotificationResourceMessages` | 10 | messaging | 0.59 |
| 32 | `xjdf4s.dsl.DocInterpreters` | 10 | dsl | 1.00 |
| 33 | `xjdf4s.model.resources.PostpressResources` | 9 | model | 0.75 |
| 34 | `xjdf4s.model.resources.BinderySignature` | 9 | model | 0.69 |
| 35 | `xjdf4s.model.resources.Rendering` | 9 | model | 0.64 |
| 36 | `xjdf4s.model.resources.RemainingPostpressResources` | 9 | model | 0.64 |
| 37 | `xjdf4s.model.XJDF` | 9 | model | 0.56 |
| 38 | `xjdf4s.codec.xml.domain.CodecHelpers` | 9 | codec-xml | 0.45 |
| 39 | `xjdf4s.model.resources.FoldingResources` | 9 | model | 0.69 |
| 40 | `xjdf4s.model.resources.RunList` | 9 | model | 0.56 |
| 41 | `xjdf4s.model.resources.Device` | 9 | model | 0.36 |
| 42 | `xjdf4s.model.FinishingIntents` | 9 | model | 0.41 |
| 43 | `xjdf4s.model.resources.FoundationalResources` | 9 | model | 0.39 |
| 44 | `xjdf4s.model.resources.GeneralAndPressResources` | 8 | model | 0.62 |
| 45 | `xjdf4s.model.resources.SimpleResources` | 8 | model | 0.44 |
| 46 | `xjdf4s.messaging.KnownMessages` | 8 | messaging | 0.47 |
| 47 | `xjdf4s.messaging.GangAndQueueStatusMessages` | 8 | messaging | 0.62 |
| 48 | `xjdf4s.model.resources.DeliveryAndPreflightResources` | 8 | model | 0.67 |
| 49 | `xjdf4s.model.ContentAndShapeIntents` | 8 | model | 0.26 |
| 50 | `xjdf4s.model.resources.DieLayoutProduction` | 8 | model | 0.50 |
| 51 | `xjdf4s.model.resources.PdlCreation` | 8 | model | 0.73 |
| 52 | `xjdf4s.model.resources.PrepressResources` | 8 | model | 0.50 |
| 53 | `xjdf4s.model.BindingIntent` | 8 | model | 0.53 |
| 54 | `xjdf4s.messaging.QueueEntryMessages` | 8 | messaging | 0.67 |
| 55 | `xjdf4s.model.resources.MissingSchemaResources` | 8 | model | 0.73 |
| 56 | `xjdf4s.model.AssemblingIntent` | 7 | model | 0.32 |
| 57 | `xjdf4s.codec.xml.derivation.Derived` | 7 | codec-xml | 0.70 |
| 58 | `xjdf4s.model.resources.Identification` | 7 | model | 0.35 |
| 59 | `xjdf4s.codec.xml.domain.CoreNodeCodecs` | 7 | codec-xml | 0.78 |
| 60 | `xjdf4s.messaging.XJMF` | 7 | messaging | 0.64 |
| 61 | `xjdf4s.model.Subelements` | 7 | model | 0.18 |
| 62 | `xjdf4s.model.resources.ColorSpaceConversion` | 7 | model | 0.64 |
| 63 | `xjdf4s.model.SimpleIntents` | 7 | model | 0.26 |
| 64 | `xjdf4s.model.resources.SmallProductionResources` | 7 | model | 0.70 |
| 65 | `xjdf4s.model.MediaIntent` | 7 | model | 0.44 |
| 66 | `xjdf4s.codec.json.JsonResources` | 7 | codec-json | 0.88 |
| 67 | `xjdf4s.model.resources.MorePostpressResources` | 7 | model | 0.70 |
| 68 | `xjdf4s.codec.xml.domain.GeneralIdCodec` | 7 | codec-xml | 0.78 |
| 69 | `xjdf4s.codec.xml.domain.ReferenceCheck` | 7 | codec-xml | 1.00 |
| 70 | `xjdf4s.messaging.ControlMessages` | 7 | messaging | 0.64 |
| 71 | `xjdf4s.dsl.DocOp` | 6 | dsl | 0.86 |
| 72 | `xjdf4s.model.resources.ImageCompression` | 6 | model | 0.60 |
| 73 | `xjdf4s.model.DocumentValidation` | 6 | model | 1.00 |
| 74 | `xjdf4s.codec.xml.XmlDecoders` | 6 | codec-xml | 0.35 |
| 75 | `xjdf4s.model.Partition` | 5 | model | 0.11 |
| 76 | `xjdf4s.messaging.StandardMessages` | 5 | messaging | 1.00 |
| 77 | `xjdf4s.model.resources.ColorantControl` | 5 | model | 0.63 |
| 78 | `xjdf4s.model.resources.Contact` | 5 | model | 0.56 |
| 79 | `xjdf4s.messaging.Message` | 4 | messaging | 0.25 |
| 80 | `xjdf4s.Protocol` | 4 | protocol | 1.00 |
| 81 | `xjdf4s.codec.json.JsonCodec` | 4 | codec-json | 0.44 |
| 82 | `xjdf4s.codec.xml.XmlParser` | 3 | codec-xml | 1.00 |
| 83 | `xjdf4s.codec.xml.ForeignCodec` | 3 | codec-xml | 0.38 |
| 84 | `xjdf4s.model.Audit` | 3 | model | 0.33 |
| 85 | `xjdf4s.core.Common` | 2 | core | 0.11 |
| 86 | `xjdf4s.model.Header` | 2 | model | 0.14 |
| 87 | `xjdf4s.codec.xml.XmlWriter` | 2 | codec-xml | 1.00 |
| 88 | `xjdf4s.codec.xml.XmlCodec` | 2 | codec-xml | 0.11 |
| 89 | `xjdf4s.codec.xml.Xml` | 1 | codec-xml | 0.05 |
| 90 | `xjdf4s.core.Primitives` | 1 | core | 0.01 |
| 91 | `xjdf4s.model.TypedValues` | 1 | model | 0.07 |
| 92 | `xjdf4s.model.ColorValues` | 1 | model | 0.07 |
| 93 | `xjdf4s.model.XjdfNames` | 1 | model | 0.03 |
| 94 | `xjdf4s.core.Cardinality` | 1 | core | 0.03 |
| 95 | `xjdf4s.messaging.MessageNames` | 1 | messaging | 0.17 |
| 96 | `xjdf4s.core.Extension` | 1 | core | 0.01 |
| 97 | `xjdf4s.core.Validation` | 0 | core | 0.00 |
| 98 | `xjdf4s.codec.xml.XmlError` | 0 | codec-xml | 0.00 |
| 99 | `xjdf4s.codec.xml.derivation.Names` | 0 | codec-xml | 0.00 |
| 100 | `xjdf4s.core.Values` | 0 | core | 0.00 |

---

## 🚨 Полный рейтинг Betweenness Centrality

| # | File | Betweenness | Fan-In | Fan-Out | Module |
|---|---|---|---|---|---|
| 1 | `xjdf4s.model.Resource` | 2541.48 | 48 | 39 | model |
| 2 | `xjdf4s.model.Subelements` | 611.62 | 33 | 7 | model |
| 3 | `xjdf4s.model.ContentAndShapeIntents` | 402.10 | 23 | 8 | model |
| 4 | `xjdf4s.model.Product` | 239.84 | 13 | 10 | model |
| 5 | `xjdf4s.codec.xml.Lexical` | 238.86 | 12 | 18 | codec-xml |
| 6 | `xjdf4s.model.resources.MediaAndColor` | 215.30 | 15 | 15 | model |
| 7 | `xjdf4s.model.resources.Device` | 114.58 | 16 | 9 | model |
| 8 | `xjdf4s.model.Partition` | 102.45 | 39 | 5 | model |
| 9 | `xjdf4s.codec.xml.derivation.FieldCodec` | 88.78 | 6 | 23 | codec-xml |
| 10 | `xjdf4s.model.resources.ProcessResources` | 88.12 | 13 | 13 | model |
| 11 | `xjdf4s.codec.xml.domain.Registry` | 76.68 | 3 | 50 | codec-xml |
| 12 | `xjdf4s.model.resources.AdditionalResources` | 71.83 | 8 | 14 | model |
| 13 | `xjdf4s.messaging.KnownMessages` | 68.00 | 9 | 8 | messaging |
| 14 | `xjdf4s.model.XJDF` | 66.42 | 7 | 9 | model |
| 15 | `xjdf4s.model.resources.FoundationalResources` | 63.47 | 14 | 9 | model |
| 16 | `xjdf4s.codec.json.JsonCodec` | 55.32 | 5 | 4 | codec-json |
| 17 | `xjdf4s.messaging.Message` | 49.75 | 12 | 4 | messaging |
| 18 | `xjdf4s.model.resources.SimpleResources` | 44.05 | 10 | 8 | model |
| 19 | `xjdf4s.model.resources.MarksAndStacking` | 42.09 | 6 | 15 | model |
| 20 | `xjdf4s.codec.json.JsonMessagingCodecs` | 40.40 | 1 | 13 | codec-json |
| 21 | `xjdf4s.messaging.ControlMessages` | 35.47 | 4 | 7 | messaging |
| 22 | `xjdf4s.model.SimpleIntents` | 33.25 | 20 | 7 | model |
| 23 | `xjdf4s.messaging.StatusNotificationResourceMessages` | 29.81 | 7 | 10 | messaging |
| 24 | `xjdf4s.codec.json.JsonNodeCodecs` | 29.76 | 1 | 10 | codec-json |
| 25 | `xjdf4s.model.FinishingIntents` | 27.38 | 13 | 9 | model |
| 26 | `xjdf4s.model.resources.PostpressResources` | 24.69 | 3 | 9 | model |
| 27 | `xjdf4s.codec.xml.domain.CodecHelpers` | 24.53 | 11 | 9 | codec-xml |
| 28 | `xjdf4s.model.AssemblingIntent` | 21.54 | 15 | 7 | model |
| 29 | `xjdf4s.core.Extension` | 20.81 | 73 | 1 | core |
| 30 | `xjdf4s.model.resources.QualityControl` | 20.39 | 5 | 12 | model |
| 31 | `xjdf4s.codec.json.JsonMediaCodecs` | 20.14 | 1 | 16 | codec-json |
| 32 | `xjdf4s.model.resources.RemainingPostpressResources` | 18.73 | 5 | 9 | model |
| 33 | `xjdf4s.model.resources.MorePostpressResources` | 18.19 | 3 | 7 | model |
| 34 | `xjdf4s.model.resources.SheetOptimizing` | 18.09 | 4 | 12 | model |
| 35 | `xjdf4s.model.resources.RunList` | 17.86 | 7 | 9 | model |
| 36 | `xjdf4s.model.resources.ShapeDefinitionResources` | 16.71 | 4 | 10 | model |
| 37 | `xjdf4s.model.resources.BinderySignature` | 13.39 | 4 | 9 | model |
| 38 | `xjdf4s.core.Primitives` | 13.26 | 73 | 1 | core |
| 39 | `xjdf4s.model.MediaIntent` | 12.10 | 9 | 7 | model |
| 40 | `xjdf4s.codec.xml.domain.ResourceCodecs` | 11.35 | 2 | 19 | codec-xml |
| 41 | `xjdf4s.model.Audit` | 11.18 | 6 | 3 | model |
| 42 | `xjdf4s.codec.json.JsonScalars` | 11.02 | 1 | 11 | codec-json |
| 43 | `xjdf4s.model.resources.Identification` | 10.09 | 13 | 7 | model |
| 44 | `xjdf4s.model.resources.Content` | 10.05 | 4 | 15 | model |
| 45 | `xjdf4s.model.resources.Contact` | 9.76 | 4 | 5 | model |
| 46 | `xjdf4s.codec.xml.XmlDecoders` | 9.39 | 11 | 6 | codec-xml |
| 47 | `xjdf4s.model.BindingIntent` | 9.03 | 7 | 8 | model |
| 48 | `xjdf4s.codec.xml.domain.MessagingCodecs` | 8.57 | 2 | 19 | codec-xml |
| 49 | `xjdf4s.model.resources.FoldingResources` | 8.28 | 4 | 9 | model |
| 50 | `xjdf4s.codec.xml.derivation.Derived` | 8.19 | 3 | 7 | codec-xml |
| 51 | `xjdf4s.model.resources.DeliveryAndPreflightResources` | 7.37 | 4 | 8 | model |
| 52 | `xjdf4s.model.resources.Rendering` | 7.18 | 5 | 9 | model |
| 53 | `xjdf4s.model.resources.Interpreting` | 6.27 | 6 | 10 | model |
| 54 | `xjdf4s.model.resources.DieLayoutProduction` | 5.08 | 8 | 8 | model |
| 55 | `xjdf4s.messaging.GangAndQueueStatusMessages` | 5.06 | 5 | 8 | messaging |
| 56 | `xjdf4s.model.resources.PrepressResources` | 4.88 | 8 | 8 | model |
| 57 | `xjdf4s.model.resources.GeneralAndPressResources` | 4.72 | 5 | 8 | model |
| 58 | `xjdf4s.core.Cardinality` | 4.65 | 34 | 1 | core |
| 59 | `xjdf4s.codec.xml.domain.MediaCodec` | 4.47 | 3 | 15 | codec-xml |
| 60 | `xjdf4s.messaging.QueueEntryMessages` | 4.13 | 4 | 8 | messaging |
| 61 | `xjdf4s.codec.json.JsonResources` | 4.00 | 1 | 7 | codec-json |
| 62 | `xjdf4s.model.resources.Layout` | 3.68 | 4 | 16 | model |
| 63 | `xjdf4s.messaging.XJMF` | 3.02 | 4 | 7 | messaging |
| 64 | `xjdf4s.codec.xml.Xml` | 2.90 | 19 | 1 | codec-xml |
| 65 | `xjdf4s.codec.xml.XmlCodec` | 2.57 | 16 | 2 | codec-xml |
| 66 | `xjdf4s.model.resources.ColorSpaceConversion` | 1.99 | 4 | 7 | model |
| 67 | `xjdf4s.model.resources.DieLayout` | 1.85 | 3 | 10 | model |
| 68 | `xjdf4s.model.resources.PdlCreation` | 1.84 | 3 | 8 | model |
| 69 | `xjdf4s.model.resources.SmallProductionResources` | 1.77 | 3 | 7 | model |
| 70 | `xjdf4s.model.ColorValues` | 1.45 | 14 | 1 | model |
| 71 | `xjdf4s.codec.xml.domain.PartCodecs` | 1.38 | 3 | 10 | codec-xml |
| 72 | `xjdf4s.core.Common` | 1.37 | 16 | 2 | core |
| 73 | `xjdf4s.model.TypedValues` | 1.33 | 13 | 1 | model |
| 74 | `xjdf4s.codec.xml.ForeignCodec` | 1.23 | 5 | 3 | codec-xml |
| 75 | `xjdf4s.codec.xml.domain.ColorCodec` | 0.88 | 1 | 10 | codec-xml |
| 76 | `xjdf4s.model.resources.ImageCompression` | 0.85 | 4 | 6 | model |
| 77 | `xjdf4s.model.Header` | 0.69 | 12 | 2 | model |
| 78 | `xjdf4s.codec.xml.domain.CoreNodeCodecs` | 0.68 | 2 | 7 | codec-xml |
| 79 | `xjdf4s.codec.xml.domain.GeneralIdCodec` | 0.68 | 2 | 7 | codec-xml |
| 80 | `xjdf4s.model.resources.ColorantControl` | 0.65 | 3 | 5 | model |
| 81 | `xjdf4s.model.resources.MissingSchemaResources` | 0.63 | 3 | 8 | model |
| 82 | `xjdf4s.codec.xml.domain.SimpleResourceCodecs` | 0.62 | 1 | 12 | codec-xml |

---

## 🔄 Циклические зависимости (детальный разбор)

Обнаружено **4** циклов. Циклы нарушают принцип ацикличности зависимостей (ADP).


### Цикл 1 (2 файлов)

```
xjdf4s.codec.xml.derivation.FieldCodec → xjdf4s.codec.xml.derivation.Derived → xjdf4s.codec.xml.derivation.FieldCodec
```

**💡 Рекомендация:** Разорвать цикл через `xjdf4s.codec.xml.derivation.Derived` (Fan-In: 3). Вынесите интерфейс (trait) в общий модуль.


### Цикл 2 (6 файлов)

```
xjdf4s.codec.json.JsonCodec → xjdf4s.codec.json.JsonMediaCodecs → xjdf4s.codec.json.JsonNodeCodecs → xjdf4s.codec.json.JsonResources → xjdf4s.codec.json.JsonScalars → xjdf4s.codec.json.JsonMessagingCodecs → xjdf4s.codec.json.JsonCodec
```

**💡 Рекомендация:** Разорвать цикл через `xjdf4s.codec.json.JsonMediaCodecs` (Fan-In: 1). Вынесите интерфейс (trait) в общий модуль.


### Цикл 3 (2 файлов)

```
xjdf4s.codec.xml.Lexical → xjdf4s.codec.xml.XmlDecoders → xjdf4s.codec.xml.Lexical
```

**💡 Рекомендация:** Разорвать цикл через `xjdf4s.codec.xml.XmlDecoders` (Fan-In: 11). Вынесите интерфейс (trait) в общий модуль.


### Цикл 4 (43 файлов)

```
xjdf4s.model.resources.ProcessResources → xjdf4s.model.resources.DeliveryAndPreflightResources → xjdf4s.model.resources.ColorSpaceConversion → xjdf4s.model.ContentAndShapeIntents → xjdf4s.model.resources.QualityControl → xjdf4s.model.resources.DieLayoutProduction → xjdf4s.model.resources.BinderySignature → xjdf4s.model.resources.ColorantControl → xjdf4s.model.resources.FoundationalResources → xjdf4s.model.resources.Content → xjdf4s.model.resources.MarksAndStacking → xjdf4s.model.resources.PrepressResources → xjdf4s.model.resources.PostpressResources → xjdf4s.model.resources.FoldingResources → xjdf4s.model.BindingIntent → xjdf4s.model.Product → xjdf4s.model.resources.AdditionalResources → xjdf4s.model.resources.SmallProductionResources → xjdf4s.model.resources.MissingSchemaResources → xjdf4s.model.resources.GeneralAndPressResources → xjdf4s.model.resources.ShapeDefinitionResources → xjdf4s.model.resources.Rendering → xjdf4s.model.resources.RemainingPostpressResources → xjdf4s.model.resources.RunList → xjdf4s.model.resources.DieLayout → xjdf4s.model.Resource → xjdf4s.model.FinishingIntents → xjdf4s.model.SimpleIntents → xjdf4s.model.resources.Contact → xjdf4s.model.resources.Interpreting → xjdf4s.model.resources.Layout → xjdf4s.model.resources.PdlCreation → xjdf4s.model.Subelements → xjdf4s.model.Partition → xjdf4s.model.resources.Device → xjdf4s.model.resources.SimpleResources → xjdf4s.model.resources.SheetOptimizing → xjdf4s.model.resources.MorePostpressResources → xjdf4s.model.resources.ImageCompression → xjdf4s.model.resources.Identification → xjdf4s.model.MediaIntent → xjdf4s.model.resources.MediaAndColor → xjdf4s.model.AssemblingIntent → xjdf4s.model.resources.ProcessResources
```

**💡 Рекомендация:** Разорвать цикл через `xjdf4s.model.resources.ColorantControl` (Fan-In: 3). Вынесите интерфейс (trait) в общий модуль.


---

## 💀 Изолированные файлы (Dead Nodes)

✅ Изолированных файлов нет.

---

## 🍃 Листовые узлы (Fan-In = 0, Fan-Out > 0)

Всего: **11**

| File | Fan-Out | Module |
|---|---|---|
| `xjdf4s.codec.xml.domain.DerivedInstances` | 50 | codec-xml |
| `xjdf4s.codec.xml.domain.IntentAndAuditCodecs` | 36 | codec-xml |
| `xjdf4s.codec.xml.domain.HandWrappers` | 27 | codec-xml |
| `xjdf4s.codec.xml.domain.SpecialCodecs` | 19 | codec-xml |
| `xjdf4s.dsl.DocInterpreters` | 10 | dsl |
| `xjdf4s.codec.xml.domain.ReferenceCheck` | 7 | codec-xml |
| `xjdf4s.model.DocumentValidation` | 6 | model |
| `xjdf4s.messaging.StandardMessages` | 5 | messaging |
| `xjdf4s.Protocol` | 4 | protocol |
| `xjdf4s.codec.xml.XmlParser` | 3 | codec-xml |
| `xjdf4s.codec.xml.XmlWriter` | 2 | codec-xml |

---

## 🌳 Корневые узлы (Fan-In > 0, Fan-Out = 0)

Всего: **4**

| File | Fan-In | Module |
|---|---|---|
| `xjdf4s.core.Values` | 39 | core |
| `xjdf4s.core.Validation` | 23 | core |
| `xjdf4s.codec.xml.XmlError` | 13 | codec-xml |
| `xjdf4s.codec.xml.derivation.Names` | 1 | codec-xml |

---

## ⚖️ Stability vs Importance (Зонный анализ)

Файл с высоким Fan-In и высокой Instability — это архитектурное нарушение.

⚠️ Обнаружено **1** файлов с высоким Fan-In (>10) и высокой нестабильностью (>0.5):

| File | Fan-In | Instability | Module | Проблема |
|---|---|---|---|---|
| `xjdf4s.codec.xml.Lexical` | 12 | 0.60 | codec-xml | 🟡 Предупреждение |

---

## 📋 Автоматические рекомендации

### 🔴 Приоритет: Высокий

1. **Разорвать 4 циклических зависимостей.** Используйте Dependency Inversion.
2. **Стабилизировать 1 нестабильных фундаментов.** Инвертируйте зависимости через интерфейсы.
3. **Снизить нагрузку на `xjdf4s.model.Resource`.** Betweenness = 2541.

### 🟡 Приоритет: Средний

4. **Разбить 5 God Objects** (Fan-Out > 25) на более мелкие модули.

### 🟢 Приоритет: Низкий

6. **Добавить тесты** для топ-10 файлов по Fan-In.
7. **Документировать** интерфейсы файлов с Betweenness > 33.251410871897704.
