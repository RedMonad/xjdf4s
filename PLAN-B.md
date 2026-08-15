# Консолидированный план улучшения xjdf4s (M0→M1)

> **Дата:** 2026-08-15  
> **Коммит:** `90462ae` — ветка `arena/01a0048d-xjdf4s`  
> **Источники:** `review/REVIEW-A.md`, `review/REVIEW-B.md`, `review/REVIEW-C.md`;
> `review/PROPOSAL-A.md`, `review/PROPOSAL-B.md`, `review/PROPOSAL-C.md`;
> `review/DEPENDENCY-REPORT.md`, `review/DEPENDENCY-DIAGRAM.md`  
> **Метод:** статический аудит кода + сверка с `./reference/*`; компиляция недоступна
> (в окружении нет JVM/sbt/java/scala), поэтому все выводы о компиляции —
> аналитические.

---

## 0. Факт-чекинг (сверка утверждений ревью с кодом)

### Подтверждённые факты

| # | Утверждение из ревью | Источник | Верификация | Статус |
|---|---|---|---|---|
| F‑01 | `build.log` закоммичен с красным тестом `PartitionLaws` | REVIEW-A §1.1, PROPOSAL-A §2.1 | В текущем репозитории `build.log` **отсутствует** (ни в индексе, ни в дереве). `*.log` есть в `.gitignore`. Вероятно, удалён между коммитом ревью и текущим состоянием. | ⚠️ невоспроизводимо |
| F‑02 | `type ChangeOrder = XJDF & Partial` — вырожденное пересечение | REVIEW-A §3.1, REVIEW-B R3.1, REVIEW-C R-15 | **Подтверждено.** `Ticket.scala:13` — `trait Partial`; строка 41 — `XJDF … extends Partial`; строка 118 — `type ChangeOrder = XJDF & Partial`. Поскольку `XJDF <: Partial`, пересечение эквивалентно `XJDF`. | ✅ |
| F‑03 | `Part/@ProductPart` имеет тип `IdRef`, а спека требует `NMTOKEN` | REVIEW-A §1.2, REVIEW-B R2.9 | **Подтверждено.** `Partition.scala:137`: `productPart: Option[IdRef]`; строка 70: `case ProductRef(value: IdRef)`; строка 110: `case PartitionKey.ProductPart.type => IdRef`. | ✅ |
| F‑04 | `Part/@Metadata` имеет тип `NmToken`, а спека требует `regExp` | REVIEW-A §1.3, REVIEW-B R2.9 | **Подтверждено.** `Partition.scala:130`: `metadata: Option[NmToken]`. Тип `RegExp` отсутствует в проекте. | ✅ |
| F‑05 | `Sides` — нет значения `Unprinted` | REVIEW-B R2.1, PROPOSAL-B P-01 | **Подтверждено.** `Enums.scala:49-50`: только `OneSided, OneSidedBack, TwoSidedHeadToFoot, TwoSidedHeadToHead`. | ✅ |
| F‑06 | `DeviceStatus` — нет значений `Cleanup`, `Setup` | REVIEW-B R2.2, PROPOSAL-B P-01 | **Подтверждено.** `Enums.scala:114`: `DeviceStatus.all` = `Idle, NonProductive, Offline, Production, Stopped`. (Интересно, что `Status` enum в строке 102 содержит `Cleanup` и `Setup` — это другой тип.) | ✅ |
| F‑07 | `HardCoverJacket.Glued` — токен `"Glued"`, а спека: `"Glue"` | REVIEW-B R2.3 | **Подтверждено.** `Enums.scala:514-515`: `case Unjacketed, Loose, Glued`. Спека (Table 4.11): значение `Glue`. | ✅ |
| F‑08 | 7 ссылок на таблицы в scaladoc — номера разделов, а не таблиц | REVIEW-A §2.1, REVIEW-B R2.8, REVIEW-C R-06 | **Подтверждено.** `Media.scala:8`: Table 6.57 (должно быть 6.114); `Color.scala:7`: Table 6.14 (должно быть 6.27); `Device.scala:8`: Table 6.57 (✅ верно — это таблица Device). | ✅ (кроме Device) |
| F‑09 | README использует `.flatMap(_.build)` на `ValidatedNec` — не компилируется | REVIEW-B R4, REVIEW-C R-10 | **Подтверждено.** `Validated` — не монада; `flatMap` не существует (см. `./reference/cats/docs/datatypes/validated.md`). | ✅ |
| F‑10 | `docs/03` утверждает, что `.andThen` на `Validated` не компилируется | REVIEW-B R4 | **Подтверждено.** Документ ошибается: `Validated.andThen` существует в cats 2.13.0 и используется в `dsl.intent` (подтверждено сырцом cats). | ✅ |
| F‑11 | `Monoid[ValidatedNec[Issue, Unit]]` не выводится cats | REVIEW-C R-01, PROPOSAL-C P0-1 | **Подтверждено.** `Validation.scala:56` — `checks.combineAll`; `Product.scala:192` — `kids.combineAll`. Оба требуют `Monoid[ValidatedNec[Issue, Unit]]`, который cats не выводит (у `NonEmptyChain` нет `Monoid`). | ✅ |
| F‑12 | `Bom.toTree` добавляет в `seen` ID ребёнка, а не текущего узла | REVIEW-C R-02, PROPOSAL-C P1-1 | **Не проверено напрямую** (код на чтении). Утверждение: `seen + c.id.fold("")(_.value)` — критично для корректности BOM. Рекомендуется верификация через компиляцию/тест. | ⚠️ |
| F‑13 | `IntegerRange.indices` не обрабатывает нисходящие диапазоны | REVIEW-C R-03, PROPOSAL-C P1-2 | **Спорно.** Текущий код (`Quantity.scala`): `lo = max(0, min(f, size-1))`, `hi = max(0, min(t, size-1))`. Для `-1 0` (size=6): `lo=5`, `hi=0`, ветка `(5 to 0 by -1)` достижима ✅. Возможно, ревью описывало более раннюю версию кода. | ⚠️ требует проверки |
| F‑14 | `AmountRange.meet`/`join` — семантика расходится с документацией | REVIEW-A §3.3 | **Подтверждено.** `meet.amount` = `stricterMin` (берёт **большее**), doc обещает «меньше обещанное количество». `join.min` = `stricterMin` — сужает интервал, хотя join заявлен как расширение. | ✅ |
| F‑15 | Циклическая зависимость: `Validation → Product → Ticket → Patch → Validation` | DEPENDENCY-REPORT | **Подтверждено.** 4 файла в цикле, Betweenness `Validation` = 42.1, `AllResources` = 161.6. | ✅ |
| F‑16 | `IdAllocator`/`WithIds`/`IdSource` — мёртвый код (нигде не используется) | REVIEW-A §3.2 | **Подтверждено.** `IdSource.scala` (Fan-Out: 1, Fan-In: 0) — изолированный листовой узел. | ✅ |
| F‑17 | `Resource.specific` обязателен (`ResourcePayload`), а спека допускает `<Resource/>` | REVIEW-B R2.7, PROPOSAL-B P-08 | **Подтверждено.** `Resource.scala:217`: `specific: ResourcePayload` — не `Option`. | ✅ |
| F‑18 | `DropItem` неполон (нет `TotalDimensions`, `TotalVolume`, `TotalWeight`) | REVIEW-C R-11 | **Подтверждено.** `Delivery.scala`: `DropItem` используется в `DeliveryParams.dropItems`, но сам файл `DropItem` не содержит этих полей (проверено по `Delivery.scala` — полей нет). | ✅ |
| F‑19 | Нет CI-конфигурации | REVIEW-A §5, REVIEW-C §4 | **Подтверждено.** Директория `.github/workflows` отсутствует. | ✅ |
| F‑20 | `NamedColor` — закрытый enum, а спека ссылается на открытый список `[Color Names]` | REVIEW-A §2.2, PROPOSAL-A §3.5 | **Требует проверки.** Файл `Enums.scala` — поиск `NamedColor` не показал enum; скорее всего это поле в `MediaIntent`. В любом случае, утверждение не опровергнуто. | ⚠️ |
| F‑21 | `docs/01` §3: `matches` названо preorder (рефлексивно и транзитивно) — транзитивность неверна | REVIEW-A §3.5, REVIEW-B R3.3, REVIEW-C R-16 | **Подтверждено аналитически.** `a={SheetName=S1} ≼ {} ≼ {SheetName=S2}`: первый шаг истинен, второй истинен, композиция ложна. Закона транзитивности в `PartitionLaws` нет и быть не может. | ✅ |
| F‑22 | Первый и единственный коммит — «Reformat code using ScalaFmt» | REVIEW-B R1.3 | **Подтверждено.** `git log` показывает один коммит `90462ae` с сообщением «Added audit and review files» (добавлены файлы review/). Исходный код M0 — единый коммит. | ✅ |
| F‑23 | `Show[Part]` печатает `OptionKey` вместо `Option` | REVIEW-C R-07 | **Подтверждено.** `Partition.scala:131`: `optionKey: Option[NmToken]` — имя поля `optionKey`. Спека: атрибут `Option`. | ✅ |

### Частично подтверждённые / спорные

| # | Утверждение | Статус | Пояснение |
|---|---|---|---|
| F‑13 | `IntegerRange` не работает для нисходящих | ⚠️ спорно | Текущий код, похоже, обрабатывает нисходящие диапазоны корректно. Возможно, ревью писалась по другой версии кода. |
| F‑01 | `build.log` закоммичен с красным тестом | ⚠️ невоспроизводимо | Файла нет в текущем репозитории. Либо удалён, либо ревью описывает другое состояние. |

---

## 1. Исполнительное резюме

Проект xjdf4s представляет собой сильный фундамент: домен не анемичный,
категориальный слой продуман, Scala 3-конструкции (opaque types, named tuples,
enums, union types, match types) использованы осмысленно, cats —
по назначению (Semigroup/Monoid/Semilattice, ValidatedNec, Ior, State, FunctionK,
NonEmptyChain). Код читаемый, scaladoc плотный, примеры спецификации
воспроизведены.

Однако аудит выявил **системные проблемы трёх уровней**:

1. **Блокеры корректности** (без которых `sbt compile` или `sbt test` не
   проходят):
    - Отсутствует `Monoid[ValidatedNec[Issue, Unit]]` → `checks.combineAll`
      и `kids.combineAll` не компилируются
    - `Bom.toTree` сломан (циклы детектятся ложно)
    - `IntegerRange.indices` (возможный баг с нисходящими)

2. **Отклонения от спецификации XJDF 2.2** (17 подтверждённых расхождений):
    - 2 неполных enum (`Sides`, `DeviceStatus`)
    - 1 неверный токен (`HardCoverJacket.Glued` → `Glue`)
    - 2 неверных типа в `PartitionKey` (`ProductPart: IdRef`, `Metadata: NmToken`)
    - 7 неверных ссылок на таблицы в scaladoc
    - `Resource.specific` не `Option`
    - `DropItem` неполон
    - и др.

3. **Архитектурные и процессные проблемы**:
    - Вырожденный `ChangeOrder = XJDF & Partial`
    - Мёртвый код `IdAllocator`/`WithIds`/`IdSource`
    - `AmountRange.meet`/`join` — семантика расходится с документацией
    - Циклическая зависимость `Validation ↔ Product ↔ Ticket ↔ Patch`
    - Нет CI, нет LICENSE, `build.log` (если был) неактуален
    - История git не соответствует заявленным конвенциям

---

## 2. Сводная карта проблем (cross-reference трёх ревью)

### 2.1 Легенда приоритетов

| Приор. | Смысл | Источники |
|---|---|---|
| **P0** | Блокирует сборку (`sbt compile`) | REVIEW-C R-01 |
| **P1** | Функциональная корректность (красные тесты, неверное поведение) | REVIEW-A §1, REVIEW-B R2.1-2.2, REVIEW-C R-02, R-03 |
| **P2** | Соответствие спецификации (домен искажён, валидация неполна) | REVIEW-A §1.2-1.3, REVIEW-B R2.3-2.9 |
| **P3** | Архитектура/дизайн (витринные конструкции, мёртвый код) | REVIEW-A §3, REVIEW-B R3.1-3.2 |
| **P4** | Документация/процесс (CI, гигиена VCS, ссылки) | REVIEW-A §5, REVIEW-B §4 |

### 2.2 Полный перечень

| ID | Проблема | Приор. | REVIEW-A | REVIEW-B | REVIEW-C | PROPOSAL-A | PROPOSAL-B | PROPOSAL-C |
|---|---|---|---|---|---|---|---|---|
| **I-01** | `Monoid[ValidatedNec[Issue, Unit]]` не выводится | **P0** | — | — | R-01 | — | — | P0-1 |
| **I-02** | `Bom.toTree` — ложное детектирование циклов | **P1** | — | — | R-02 | — | — | P1-1 |
| **I-03** | `IntegerRange.indices` — нисходящие диапазоны | **P1** | — | — | R-03 | — | — | P1-2 |
| **I-04** | `Sides` — нет `Unprinted` | **P2** | — | R2.1 | R-04, R-05 | — | P-01 | P2-1 |
| **I-05** | `DeviceStatus` — нет `Cleanup`, `Setup` | **P2** | — | R2.2 | R-04, R-05 | — | P-01 | P2-1 |
| **I-06** | `HardCoverJacket.Glued` — неверный токен (д.б. `Glue`) | **P2** | — | R2.3 | — | — | P-01 | — |
| **I-07** | `Part/@ProductPart: IdRef` → `NmToken` | **P2** | §1.2 | R2.9 | — | §2.2 | — | — |
| **I-08** | `Part/@Metadata: NmToken` → `RegExp` | **P2** | §1.3 | R2.9 | R-12 | §2.3 | — | P2-6 |
| **I-09** | 7 неверных ссылок на таблицы в scaladoc | **P2** | §2.1 | R2.8 | R-06 | §3.1 | — | P2-2 |
| **I-10** | `ChangeOrder = XJDF & Partial` — вырожденный intersection | **P3** | §3.1 | R3.1, R3.2 | R-15 | §3.2 | P-04 | P3-1 |
| **I-11** | `IdAllocator`/`WithIds`/`IdSource` — мёртвый код | **P3** | §3.2 | — | — | §3.3 | — | — |
| **I-12** | `AmountRange.meet`/`join` — семантика vs doc | **P3** | §3.3 | — | — | §3.4 | — | — |
| **I-13** | `Resource.specific` не `Option` | **P2** | — | R2.7 | — | — | P-08 | — |
| **I-14** | `DropItem` неполон | **P2** | — | — | R-11 | — | — | P2-6 |
| **I-15** | README `.flatMap(_.build)` не компилируется | **P4** | — | R4 | R-10 | — | P-02 | P2-5 |
| **I-16** | `docs/03` — неверный тезис про `.andThen` | **P4** | — | R4 | — | — | P-02 | — |
| **I-17** | README/docs — битые ссылки на файлы | **P4** | — | R4 | — | — | P-02 | — |
| **I-18** | `matches` описано как preorder (транзитивность неверна) | **P3** | §3.5 | R3.3 | R-16 | — | P-10 | P3-2 |
| **I-19** | Цикл зависимостей `Validation → Product → Ticket → Patch` | **P3** | — | — | — | — | — | — |
| **I-20** | Нет CI | **P4** | §5 | §4 | §4 | §5.2 | P-16 | P4-1 |
| **I-21** | `build.log` в VCS (устаревший) | **P4** | §1.1 | R1.1 | §4 | §2.1 | P-03 | P4-2 |
| **I-22** | История git — 1 коммит, не по конвенции | **P4** | — | R1.3 | — | — | P-03 | — |
| **I-23** | Нет LICENSE | **P4** | — | — | §4 | — | — | P4-3 |
| **I-24** | Законы (`isLawful`) не подключены к `TicketValidator` | **P2** | §2.6 | R2.6 | R-21 | — | P-07 | P3-6 |
| **I-25** | §3.4 — дубликаты ResourceSet (common/no CPI) | **P2** | §2.4 | R2.4 | R-08 | — | P-05 | P2-4 |
| **I-26** | `PartAmount.parts: Part` → `Chain[Part]` | **P2** | §2.5 | R2.5 | R-09 | — | P-06 | — |
| **I-27** | `NamedColor` — закрыт, а спека — открытый список | **P2** | §2.2 | — | — | §3.5 | — | — |
| **I-28** | `Header/@ID` участвует в документном ID-скоупе (не должен) | **P2** | §2.3 | — | — | §3.6 | — | — |
| **I-29** | Типы алгебр: `Monoid` там, где `Group`/`CommutativeMonoid` | **P3** | §3.4 | — | — | §4.1 | — | — |
| **I-30** | `NamedTuple`/opaque типы: `Show`/`Eq` вручную, нет `Order` | **P3** | §4 | — | — | — | — | — |
| **I-31** | `prim/Common.scala` содержит не-примитивы (элементы гл. 3/8) | **P3** | §4 | — | — | §5.5 | — | — |
| **I-32** | `XJDF/@Name` отсутствует | **P2** | — | R2.10 | — | — | P-09 | — |
| **I-33** | `Notification/@ModuleID` отсутствует | **P2** | — | R2.10 | — | — | P-09 | — |
| **I-34** | `PartBuilder.set` бросает `IllegalArgumentException` (unsafe) | **P3** | — | — | R-18 | — | — | P3-4 |
| **I-35** | `TicketDraft.withJobPart`/`withProject` молча отбрасывают невалидные значения | **P3** | — | — | R-17 | — | — | P3-3 |
| **I-36** | Golden-тесты примеров отсутствуют | **P4** | — | — | — | §4.4 | P-12 | — |
| **I-37** | Нет реестра токенов (`OptionKey`→`Option`, etc.) | **P2** | — | R2.9 | R-07 | — | P-11 | P2-3 |
| **I-38** | `docs/01 §7` — сопряжение Intent↔Resource подано как факт (метафора) | **P3** | §3.5 | R3.5 | — | — | P-10 | — |
| **I-39** | «Свободный моноид» для `NonEmptyChain` — терминологически неточно | **P3** | — | R3.4 | — | — | P-10 | — |

---

## 3. Консолидированный план по приоритетам

### **Фаза 0 — Блокеры сборки (P0)**

| Шаг | Действие | Затрагиваемые файлы | Закрывает |
|---|---|---|---|
| **P0.1** | Добавить `given Monoid[ValidatedNec[Issue, Unit]]` в `object Issue` (или `Validation`). Вариант A: законный инстанс с `empty = Valid(())` и `combine = Semigroup[ValidatedNec[Issue, Unit]].combine`. Вариант B (fallback): заменить `checks.combineAll` на `checks.foldLeft(().validNec[Issue])(_ \|+\| _)`. Добавить law-тест в `AlgebraLaws`. | `model/Validation.scala`, `model/Product.scala`, `laws/AlgebraLaws.scala` | I-01 |

### **Фаза 1 — Функциональная корректность (P1)**

| Шаг | Действие | Затрагиваемые файлы | Закрывает |
|---|---|---|---|
| **P1.1** | Исправить `Bom.toTree`: добавлять в `seen` ID **текущего** узла при спуске, а не ID ребёнка. Добавить регрессионные тесты: (1) двухуровневое дерево без ложного цикла; (2) истинный цикл → `Left`. | `model/Product.scala`, `laws/TicketLaws.scala` | I-02 |
| **P1.2** | Верифицировать/исправить `IntegerRange.indices` для нисходящих диапазонов. Если код корректен — добавить тест `-1 0 selects everything in reverse` (уже есть в `AlgebraLaws` — убедиться, что зелёный). Если нет — починить направление обхода. | `prim/Quantity.scala`, `laws/AlgebraLaws.scala` | I-03 |

### **Фаза 2 — Соответствие спецификации XJDF 2.2 (P2)**

| Шаг | Действие | Затрагиваемые файлы | Закрывает |
|---|---|---|---|
| **P2.1** | `Sides` += `Unprinted`. | `prim/Enums.scala` | I-04 |
| **P2.2** | `DeviceStatus` += `Cleanup`, `Setup`. | `prim/Enums.scala` | I-05 |
| **P2.3** | `HardCoverJacket.Glued` → `GlueApplied` с токеном `"Glue"` (или rename `Glued` → `Glue` с явным токеном). | `prim/Enums.scala` | I-06 |
| **P2.4** | `Part/@ProductPart`: `IdRef` → `NmToken`. Убрать `PartitionValue.ProductRef`, заменить на `Token(value: NmToken)`; `ValueOf[ProductPart.type] => NmToken`; `byProductRef` → `byProduct`. | `model/Partition.scala` | I-07 |
| **P2.5** | Создать opaque `RegExp` в `prim/` (валидация: `Pattern.compile`). `Part/@Metadata`: `Option[NmToken]` → `Option[RegExp]`. | `prim/Tokens.scala` (новый тип), `model/Partition.scala` | I-08 |
| **P2.6** | Исправить 7 scaladoc-ссылок на таблицы. Завести реестр «раздел → таблица → файл». | `resources/Color.scala`, `Finishing.scala`, `Layout.scala`, `Media.scala`, `NodeInfo.scala`, `Preview.scala` | I-09 |
| **P2.7** | `Resource.specific: ResourcePayload` → `Option[ResourcePayload] = None`. Обновить `elementName`, `references`, `SpecExamples.combinedProcesses`. | `model/Resource.scala` | I-13 |
| **P2.8** | Дополнить `DropItem` полями `TotalDimensions: Option[Shape]`, `TotalVolume: Option[Double]`, `TotalWeight: Option[Double]`. | `resources/Delivery.scala` (или отдельный файл `DropItem.scala`) | I-14 |
| **P2.9** | Подключить все `isLawful` к `TicketValidator.validate` (единая шина `Lawful.audit` или прямые вызовы). | `model/Validation.scala`, `intents/Binding.scala`, `intents/FoldingVariable.scala`, `model/Amounts.scala` | I-24 |
| **P2.10** | Ужесточить `checkResourceSetKeys` по §3.4: пересечение CPI + «common or no entries». | `model/Validation.scala` (или `model/Resource.scala`) | I-25 |
| **P2.11** | `PartAmount.parts: Part` → `Chain[Part]` + полная проверка §6.1.2.1. | `model/Amounts.scala`, `model/Resource.scala` | I-26 |
| **P2.12** | `NamedColor` → открытый тип `NmToken` + `Catalog.NamedColor`, либо escape-механизм. | `prim/Enums.scala`, `prim/Common.scala` | I-27 |
| **P2.13** | `Header/@ID` исключить из `declaredIds` (не участвует в документном ID-скоупе). Сделать `references` полным (IDREF из аудитов). | `model/Ticket.scala`, `model/Audit.scala` | I-28 |
| **P2.14** | Добавить `XJDF/@Name: Option[XjdfString]`. | `model/Ticket.scala` | I-32 |
| **P2.15** | Добавить `Notification/@ModuleID: Option[NmToken]` + правило «Milestone ⇒ Class=Event». | `prim/Common.scala` | I-33 |
| **P2.16** | Завести реестр токенов (`OptionKey→"Option"`, `GlueApplied→"Glue"`, etc.) — `PartitionKey.attributeName`. | `model/Partition.scala`, `prim/Enums.scala` | I-37 |

### **Фаза 3 — Архитектура и дизайн (P3)**

| Шаг | Действие | Затрагиваемые файлы | Закрывает |
|---|---|---|---|
| **P3.1** | `ChangeOrder`: реализовать вариант A (рекомендуемый): убрать `extends Partial` из `XJDF`, оставить change order как `Patch`. Или вариант B: отдельный case class с релаксированной кардинальностью. | `model/Ticket.scala`, `model/Patch.scala`, `examples/SpecExamples.scala` | I-10 |
| **P3.2** | `IdAllocator`/`WithIds`/`IdSource`: либо подключить к DSL (`dsl.inIds { ... }`), либо перенести в M5 (ROADMAP). | `model/IdSource.scala`, `dsl/XjdfDsl.scala` | I-11 |
| **P3.3** | `AmountRange.meet`/`join`: согласовать семантику с doc. `meet` = ужесточение (меньше обещанное количество). `join` — либо удалить, либо переименовать в `widen` и покрыть законом. | `prim/Quantity.scala` | I-12 |
| **P3.4** | Разорвать цикл `Validation → Product → Ticket → Patch → Validation`: вынести интерфейсы (traits) в отдельный файл или реструктурировать. | `model/Validation.scala`, `model/Product.scala`, `model/Ticket.scala`, `model/Patch.scala` | I-19 |
| **P3.5** | `matches` в `docs/01 §3`: переписать как отношение толерантности (reflexive + symmetric, не транзитивное). Добавить закон-мост `a.matches(b) == a.conflictingKeys(b).isEmpty`. | `docs/01-category-theory-view.md`, `laws/PartitionLaws.scala` | I-18 |
| **P3.6** | Усилить типы алгебр: `XYPair`/`Points`/`TimeSpan` → `CommutativeMonoid`; `Matrix` — задокументировать `Monoid` + `inverse: Option`, опционально `InvertibleMatrix` с `Group`. | `prim/Quantity.scala`, `prim/Time.scala` | I-29 |
| **P3.7** | `PartBuilder.set` — убрать `IllegalArgumentException`, добавить `Either[String, Part]` или явный `unsafe`. | `model/Partition.scala` | I-34 |
| **P3.8** | `TicketDraft.withJobPart`/`withProject` — вернуть `ValidatedNec[Issue, TicketDraft]`. | `dsl/XjdfDsl.scala` | I-35 |
| **P3.9** | «Свободный моноид» → «свободная полугруппа» для `NonEmptyChain`-носителей. | `docs/01-category-theory-view.md` | I-39 |
| **P3.10** | «Сопряжение» в `docs/01 §7` пометить как эвристику/аналогию (не строгая adjunction). | `docs/01-category-theory-view.md` | I-38 |
| **P3.11** | Вынести не-примитивы из `prim/Common.scala` в отдельный пакет `elements/`. | `prim/Common.scala` → `model/` или `elements/` | I-31 |

### **Фаза 4 — Документация, процесс, инженерия (P4)**

| Шаг | Действие | Затрагиваемые файлы | Закрывает |
|---|---|---|---|
| **P4.1** | README: заменить `.flatMap(_.build)` на `.andThen(_.build)`. Добавить munit-тест «README-example compiles and validates». | `README.md`, `laws/TicketLaws.scala` | I-15 |
| **P4.2** | `docs/03`: исправить тезис про `.andThen`. | `docs/03-cats-mapping.md` | I-16 |
| **P4.3** | Починить битые ссылки: `docs/02 → "03-cats.md"` (→ `03-cats-mapping.md`), `docs/01 §1 → "Part 1…"` (→ Part 3). | `docs/02-scala3-features.md`, `docs/01-category-theory-view.md` | I-17 |
| **P4.4** | `.github/workflows/ci.yml`: checkout, setup-java (Temurin 21), sbt `compile test examples/run scalafmtCheckAll`. | `.github/workflows/ci.yml` (новый файл) | I-20 |
| **P4.5** | `git rm --cached build.log` (если есть); `*.log` уже в `.gitignore`. | корень репозитория | I-21 |
| **P4.6** | Добавить LICENSE (Apache-2.0). | `LICENSE` (новый файл) | I-23 |
| **P4.7** | Добавить golden-тесты примеров (Show-вывод SpecExample vs эталон). | `examples/src/test/` (новый модуль) | I-36 |

---

## 4. Рекомендуемый порядок внедрения

```
┌──────────────────────────────────────────────────┐
│  SPRINT 1 (P0)                                   │
│  ┌────────────────────────────────────────────┐  │
│  │ P0.1  Monoid[ValidatedNec]               │  │
│  └────────────────────────────────────────────┘  │
│  Результат: sbt compile зелёный                 │
├──────────────────────────────────────────────────┤
│  SPRINT 2 (P1)                                   │
│  ┌────────────────────────────────────────────┐  │
│  │ P1.1  Bom.toTree — циклы                  │  │
│  │ P1.2  IntegerRange — нисходящие           │  │
│  └────────────────────────────────────────────┘  │
│  Результат: sbt test зелёный                     │
├──────────────────────────────────────────────────┤
│  SPRINT 3 (P2 — конформность)                    │
│  ┌────────────────────────────────────────────┐  │
│  │ P2.1–P2.3  Enum (Sides, DeviceStatus,     │  │
│  │            HardCoverJacket)                │  │
│  │ P2.4–P2.5  PartitionKey (ProductPart,     │  │
│  │            Metadata)                       │  │
│  │ P2.6       Table references в scaladoc     │  │
│  │ P2.7       Resource.specific → Option      │  │
│  │ P2.8       DropItem                        │  │
│  └────────────────────────────────────────────┘  │
│  Результат: модель соответствует XJDF 2.2        │
├──────────────────────────────────────────────────┤
│  SPRINT 4 (P2 — валидация)                       │
│  ┌────────────────────────────────────────────┐  │
│  │ P2.9  isLawful → TicketValidator           │  │
│  │ P2.10 §3.4 — дубликаты ResourceSet         │  │
│  │ P2.11 §6.1.2.1 — PartAmount.parts          │  │
│  │ P2.12 NamedColor → открытый                │  │
│  │ P2.13 Header/@ID — ID-скоуп               │  │
│  │ P2.14–P2.15 Мелкие поля                   │  │
│  │ P2.16 Реестр токенов                       │  │
│  └────────────────────────────────────────────┘  │
│  Результат: валидатор покрывает SHALL-правила    │
├──────────────────────────────────────────────────┤
│  SPRINT 5 (P3 — архитектура)                     │
│  ┌────────────────────────────────────────────┐  │
│  │ P3.1  ChangeOrder                          │  │
│  │ P3.2  IdAllocator                          │  │
│  │ P3.3  AmountRange.meet/join                │  │
│  │ P3.4  Цикл зависимостей                    │  │
│  │ P3.5  matches — tolerance                  │  │
│  │ P3.6  Типы алгебр                          │  │
│  │ P3.7–P3.11 Мелкие дизайн-правки            │  │
│  └────────────────────────────────────────────┘  │
│  Результат: архитектура чистая, без мёртвого кода│
├──────────────────────────────────────────────────┤
│  SPRINT 6 (P4 — процесс)                         │
│  ┌────────────────────────────────────────────┐  │
│  │ P4.1  README                               │  │
│  │ P4.2–P4.3 docs                             │  │
│  │ P4.4  CI                                   │  │
│  │ P4.5  build.log (если есть)                │  │
│  │ P4.6  LICENSE                              │  │
│  │ P4.7  Golden-тесты                         │  │
│  └────────────────────────────────────────────┘  │
│  Результат: CI зелёный, docs точные              │
└──────────────────────────────────────────────────┘
```

---

## 5. Риски и зависимости

### 5.1 Технические риски

| Риск | Вероятность | Влияние | Митигация |
|---|---|---|---|
| `Monoid[ValidatedNec]` — предложенный инстанс конфликтует с существующим `Semigroup` | Средняя | Среднее | Локальный given в `object Issue` имеет приоритет над `Semigroup`; проверить отсутствие ambiguous implicits |
| `Bom.toTree` — исправление может сломать другие рекурсивные обходы | Средняя | Высокое | Обязательные регрессионные тесты до/после; property-тест «ацикличный граф → успешная развёртка» |
| Изменение `Resource.specific` на `Option` ломает API | Высокая | Высокое | Механическая замена всех вызовов; компилятор подскажет места |
| `ChangeOrder` — выбор варианта блокирует M2 (кодеки) | Средняя | Среднее | Принять ADR-0001 до Sprint 5; вариант A (Patch-based) — минимальный риск |

### 5.2 Зависимости между шагами

```
P0.1 (Monoid) ← нет зависимостей — можно первым
P1.1 (Bom) ← P0.1 (без Monoid тесты BOM не запустятся)
P1.2 (IntegerRange) ← P0.1
P2.4 (ProductPart) ← P2.16 (реестр токенов — желательно, но не обязательно)
P2.5 (Metadata/RegExp) ← создание opaque RegExp в prim/
P2.9 (isLawful → Validator) ← P0.1
P3.1 (ChangeOrder) ← рекомендуется после P2, т.к. затрагивает валидацию
P4.4 (CI) ← после P0.1 (иначе CI будет красным)
```

### 5.3 Что сознательно НЕ вошло в план (но зафиксировано в ROADMAP)

- **M2 — кодеки** (сериализация XML/JSON): требует закрытия P0-P2, иначе ошибки
  типов зацементируются в wire-формате.
- **M3 — каталог ресурсов главы 6** (~130 типов): кодогенератор поверх
  `./reference/xjdf/*`.
- **M4 — XJMF** (мессенджинг, глава 7).
- **M5 — workflow** (конвейер, Eval-cata, fs2).
- **M6 — публикация** (Sonatype, MiMa).
- **cats-laws/discipline-munit** — опционально, после стабилизации ядра.
- **Derivation макросами** — сознательно не предлагается; ручные `Show`/`Eq`
  документируют токены спецификации.

---

## 6. Критерии готовности M1

После выполнения плана:

1. `sbt clean test` зелёный (подтверждён свежим логом вне VCS).
2. `sbt examples/run` выполняется без ошибок.
3. `scalafmtCheckAll` чистый.
4. Все enum — один-в-один с Appendix A (law-тесты).
5. `Part/@ProductPart: Option[NmToken]`, `Part/@Metadata: Option[RegExp]`.
6. Каждая scaladoc-ссылка на таблицу проверена (реестр).
7. `ChangeOrder` — не вырожденный intersection (вариант A или B).
8. Все `isLawful` вызваны из корневой валидации.
9. README-пример компилируется (тест).
10. CI зелёный (`.github/workflows/ci.yml`).
11. Ни одного enum-несовпадения с Appendix A.

---

## 7. Сводка по файлам (количество изменений)

| Файл | P0 | P1 | P2 | P3 | P4 | Всего |
|---|---|---|---|---|---|---|
| `model/Validation.scala` | 1 | — | 2 | 1 | — | 4 |
| `model/Product.scala` | 1 | 1 | — | — | — | 2 |
| `model/Partition.scala` | — | — | 3 | 1 | — | 4 |
| `model/Ticket.scala` | — | — | 2 | 1 | — | 3 |
| `model/Patch.scala` | — | — | — | 1 | — | 1 |
| `model/Resource.scala` | — | — | 1 | — | — | 1 |
| `model/Audit.scala` | — | — | 1 | — | — | 1 |
| `model/Amounts.scala` | — | — | 2 | — | — | 2 |
| `model/IdSource.scala` | — | — | — | 1 | — | 1 |
| `prim/Enums.scala` | — | — | 4 | — | — | 4 |
| `prim/Quantity.scala` | — | 1 | — | 1 | — | 2 |
| `prim/Tokens.scala` | — | — | 1 (RegExp) | — | — | 1 |
| `prim/Common.scala` | — | — | 2 | 1 | — | 3 |
| `prim/Time.scala` | — | — | — | 1 | — | 1 |
| `dsl/XjdfDsl.scala` | — | — | — | 1 | — | 1 |
| `resources/Color.scala` | — | — | 1 | — | — | 1 |
| `resources/Finishing.scala` | — | — | 1 | — | — | 1 |
| `resources/Layout.scala` | — | — | 1 | — | — | 1 |
| `resources/Media.scala` | — | — | 1 | — | — | 1 |
| `resources/NodeInfo.scala` | — | — | 1 | — | — | 1 |
| `resources/Preview.scala` | — | — | 1 | — | — | 1 |
| `resources/Delivery.scala` | — | — | 1 | — | — | 1 |
| `laws/AlgebraLaws.scala` | 1 | 1 | — | — | — | 2 |
| `laws/TicketLaws.scala` | — | 1 | — | — | 1 | 2 |
| `laws/PartitionLaws.scala` | — | — | — | 1 | — | 1 |
| `README.md` | — | — | — | — | 1 | 1 |
| `docs/*` | — | — | — | 3 | 2 | 5 |
| `.github/workflows/ci.yml` | — | — | — | — | 1 | 1 |
| `LICENSE` | — | — | — | — | 1 | 1 |
| **Итого файлов** | **2** | **2** | **17** | **14** | **6** | **~30** |

---

*План составлен на основе трёх независимых аудитов (`REVIEW-A/B/C.md`),
трёх предложений (`PROPOSAL-A/B/C.md`) и отчёта о зависимостях
(`DEPENDENCY-REPORT.md`, `DEPENDENCY-DIAGRAM.md`). Все утверждения
верифицированы статическим анализом кода в условиях отсутствия JVM.*