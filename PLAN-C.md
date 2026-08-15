# Консолидированный план улучшения и стабилизации проекта xjdf4s

> **Проект:** `xjdf4s` — XJDF 2.2 Domain Model for Scala 3.8.4 / Cats 2.13.0 / sbt 2.0.2  
> **Базовый срез:** ветка `arena/01a00491-xjdf4s`, HEAD `90462ae` (коммит `996b756` + аудит `./review/*`)  
> **Источники данных:** `./review/REVIEW-A.md`, `./review/REVIEW-B.md`, `./review/REVIEW-C.md`, `./review/PROPOSAL-A.md`, `./review/PROPOSAL-B.md`, `./review/PROPOSAL-C.md`, `./review/DEPENDENCY-REPORT.md`, `./review/DEPENDENCY-DIAGRAM.md`, `./reference/*` (XJDF 2.2, Cats, Scala 3, sbt), исходный код `modules/*`, `docs/*`, `ROADMAP.md`.  
> **Статус документа:** Итоговый консолидированный план действий с верификацией фактов (Fact-Checked Master Plan).

---

## Оглавление

1. [Сводный вердикт и результаты факт-чекинга](#1-сводный-вердикт-и-результаты-факт-чекинга)
    - [1.1 Матрица верификации находок аудита](#11-матрица-верификации-находок-аудита)
    - [1.2 Разбор ключевых разногласий и опровержение ложных находок](#12-разбор-ключевых-разногласий-и-опровержение-ложных-находок)
2. [Архитектурный синтез и ключевые решения (ADR)](#2-архитектурный-синтез-и-ключевые-решения-adr)
    - [ADR-1: Дизайн ChangeOrder и статус типа Partial](#adr-1-дизайн-changeorder-и-статус-типа-partial)
    - [ADR-2: Устранение циклической зависимости в пакете model](#adr-2-устранение-циклической-зависимости-в-пакете-model)
    - [ADR-3: Интеграция IdAllocator и IdSource в DSL](#adr-3-интеграция-idallocator-и-idsource-в-dsl)
    - [ADR-4: Семантическая модель AmountRange (meet/join)](#adr-4-семантическая-модель-amountrange-meetjoin)
    - [ADR-5: Коррекция категориальной строгости (Part.matches)](#adr-5-коррекция-категориальной-строгости-partmatches)
3. [Поэтапный план реализации (Actionable Roadmap)](#3-поэтапный-план-реализации-actionable-roadmap)
    - [Фаза 0 (P0): Критические блокеры сборки и корректности](#фаза-0-p0-критические-блокеры-сборки-и-корректности)
    - [Фаза 1 (P1): Соответствие спецификации XJDF 2.2 и целостность домена](#фаза-1-p1-соответствие-спецификации-xjdf-22-и-целостность-домена)
    - [Фаза 2 (P2): Архитектурная стабилизация, алгебры и качество кода](#фаза-2-p2-архитектурная-стабилизация-алгебры-и-качество-кода)
    - [Фаза 3 (P3): Документация, категориальная строгость и Developer Experience](#фаза-3-p3-документация-категориальная-строгость-и-developer-experience)
    - [Фаза 4 (P4): Инженерная инфраструктура и CI/CD](#фаза-4-p4-инженерная-инфраструктура-и-cicd)
    - [Фаза 5 (P5): Дорожная карта следующих милстоунов (M2–M6)](#фаза-5-p5-дорожная-карта-следующих-милстоунов-m2m6)
4. [Матрица сквозной трассируемости (Traceability Matrix)](#4-матрица-сквозной-трассируемости-traceability-matrix)
5. [Критерии приёмки и протокол валидации (Definition of Done)](#5-критерии-приёмки-и-протокол-валидации-definition-of-done)

---

## 1. Сводный вердикт и результаты факт-чекинга

Проект `xjdf4s` демонстрирует зрелый фундамент: неанемичная доменная модель на Scala 3.8.4, осмысленное применение Cats (законы, `ValidatedNec`, `NonEmptyChain`, `FunctionK`), честная начальная F-алгебра BOM (`Fix[ProductTree]`), моноид эндоморфизмов для патчей и высокая точность переноса базовых таблиц XJDF 2.2.

Однако анализ пакета `./review/*` вскрыл ряд проблем, которые были разделены на **реальные критические дефекты**, **отклонения от спецификации CIP4**, **ошибки в документации** и **ложные выводы самих ревьюеров**.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      ИТОГИ СТАТИЧЕСКОГО ФАКТ-ЧЕКИНГА                        │
├────────────────────────┬────────┬───────────────────────────────────────────┤
│ Категория              │ Кол-во │ Статус                                    │
├────────────────────────┼────────┼───────────────────────────────────────────┤
│ Подтверждённые баги    │   3    │ BOM-циклы, Patch.merge, README.md сниппет │
│ Спецификационные баги  │   9    │ ProductPart, Metadata, Enums, PartAmount… │
│ Ошибки документации    │   5    │ Scaladoc (7 табл.), docs/01-03            │
│ Архитектурные дефекты  │   3    │ Цикл зависимостей, ChangeOrder, IdSource  │
│ Опровергнутые находки  │   2    │ R-01 (Validated Monoid), R-03 (Range test)│
│ Ложные артефакты       │   1    │ build.log устарел (код корректен)         │
└────────────────────────┴────────┴───────────────────────────────────────────┘
```

---

### 1.1 Матрица верификации находок аудита

Каждое замечание из файлов `REVIEW-A`, `REVIEW-B`, `REVIEW-C` и `DEPENDENCY-REPORT` было независимо проверено по исходному коду `modules/*` и эталонным источникам `./reference/*`.

| ID | Замечание | Источник ревью | Эталонный факт (`./reference/*` / код) | Вердикт факт-чекинга |
|---|---|---|---|---|
| **FC-01** | `checks.combineAll` не компилируется из-за отсутствия `Monoid[ValidatedNec[Issue, Unit]]` | REVIEW-C (R-01) | Cats v2.13.0 предоставляет инстанс `Monoid[Validated[E, A]]` при наличии `Semigroup[E]` и `Monoid[A]`. Для `E = NonEmptyChain[Issue]` (`Semigroup`) и `A = Unit` (`Monoid`) инстанс синтезируется компилятором автоматически. | ❌ **ОПРОВЕРГНУТО** (Ложная тревога Reviewer C; Reviewer B §5 прав). Код компилируется без кастомного инстанса. |
| **FC-02** | Ложное детектирование циклов в `Bom.toTree`; развёртка любого BOM со ссылками падает | REVIEW-C (R-02), REVIEW-B (2.2) | В `model/Product.scala:35` вызов `seen + c.id` выполняется *до* входа в `toTree(c)`. Внутри `toTree` проверка `seen.contains(id)` немедленно срабатывает на собственном ID ребёнка. Демо `Main.demoBomFold` на примере 3.4 падает. | ✅ **ПОДТВЕРЖДЕНО** (Критический функциональный блокер). |
| **FC-03** | `IntegerRange` не выполняет закон нисходящих диапазонов `-1 0` | REVIEW-C (R-03) | В `prim/Quantity.scala:42` переменная `lo` — это зажатый `from` (`2`), а `hi` — зажатый `to` (`0`). Ветка `else (lo to hi by -1)` выполняется и возвращает `List(2, 1, 0)`. Закон в `AlgebraLaws` истинен. | ❌ **ОПРОВЕРГНУТО** (Ложный вывод Reviewer C из-за неудачных имён переменных `lo`/`hi`). Требуется только рефакторинг имён. |
| **FC-04** | Красный тест в закоммиченном `build.log` (`PartitionLaws`) | REVIEW-A (1.1), REVIEW-B (1.1) | В текущем коде `Partition.scala` операция `combine` право-смещённая (`b.x.orElse(a.x)`), поэтому `combine(a, b).matches(b)` всегда истинно. Лог остался от старой feedback-итерации. | ⚠️ **УТОЧНЕНО** (Лог устарел; в коде бага нет, но закоммиченный красный лог нарушает гигиену репозитория). |
| **FC-05** | `Part/@ProductPart` имеет тип `IdRef` вместо `NmToken` | REVIEW-A (1.2), REVIEW-B (R2.3), REVIEW-C (R-12) | Table 6.4 (стр. 6 – Resources.md): `ProductPart? (Deprecated in XJDF 2.1) | NMTOKEN`. В `model/Partition.scala:137` тип `Option[IdRef]`. Это нарушает конформность и сбор ссылок. | ✅ **ПОДТВЕРЖДЕНО** (Спецификационный дефект). |
| **FC-06** | `Part/@Metadata` имеет тип `NmToken` вместо `regExp` | REVIEW-A (1.3), REVIEW-B (R2.3), REVIEW-C (R-12) | Table 6.4: `Metadata? | regExp`. В `Partition.scala:130` тип `Option[NmToken]`, запрещающий пробелы и regex-символы. | ✅ **ПОДТВЕРЖДЕНО** (Спецификационный дефект). |
| **FC-07** | 7 ссылок на таблицы в scaladoc ресурсов путают номер раздела и номер таблицы | REVIEW-A (2.1), REVIEW-B (R2.8), REVIEW-C (R-06) | Сверено с `6 – Resources.md`: Color (6.27 vs 6.14), CuttingParams (6.53 vs 6.25), FoldingParams (6.74 vs 6.36), Layout (6.95 vs 6.52), Media (6.114 vs 6.57), NodeInfo (6.119 vs 6.59), Preview (6.134 vs 6.66). | ✅ **ПОДТВЕРЖДЕНО** (Систематическая ошибка scaladoc). |
| **FC-08** | Неполнота перечислений `DeviceStatus` и `Sides` | REVIEW-B (R2.1, R2.2), REVIEW-C (R-04, R-05) | Table A.15 содержит 7 значений (пропущены `Cleanup`, `Setup`). Table A.40 содержит 5 значений (пропущен `Unprinted`). | ✅ **ПОДТВЕРЖДЕНО** (Пропуск значений XJDF 2.1). |
| **FC-09** | Опечатка в токене `HardCoverJacket.Glued` | REVIEW-B (R2.3) | Table 4.11 (Sheet 1): допустимые значения `None`, `Loose`, `Glue`. В `prim/Enums.scala:515` объявлен `Glued`. | ✅ **ПОДТВЕРЖДЕНО** (Неверный wire-токен). |
| **FC-10** | `PartAmount.parts` смоделирован как одиночный `part: Part` вместо `Chain[Part]` | REVIEW-B (R2.5), REVIEW-C (R-09) | Table 6.3: `Part* | element` (кардинальность 0..*). В `model/Amounts.scala:28` поле `part: Part = Part.empty`. | ✅ **ПОДТВЕРЖДЕНО** (Нарушение кардинальности). |
| **FC-11** | `Patch.mergeResourceSets` не замещает конфликтующие сеты, а дублирует их | REVIEW-B (R3.2), REVIEW-C (R-08 note) | В `model/Patch.scala:67` выполняется `ticket.resourceSets ++ update`. Старый и новый `ResourceSet` остаются вместе, нарушая §3.4. | ✅ **ПОДТВЕРЖДЕНО** (Логический баг слияния). |
| **FC-12** | `ChangeOrder = XJDF & Partial` вырожден, так как `XJDF extends Partial` | REVIEW-A (3.1), REVIEW-B (R3.1), REVIEW-C (R-15) | `XJDF <: Partial` означает `XJDF & Partial ≡ XJDF`. Пересечение типов ничего не уточняет на уровне системы типов Scala. | ✅ **ПОДТВЕРЖДЕНО** (Декоративная конструкция). |
| **FC-13** | Сниппет в `README.md` не компилируется (`ValidatedNec.flatMap`) | REVIEW-B (R4.1), REVIEW-C (R-10) | `dsl.TicketDraft.of(...).flatMap(_.build)` не компилируется, так как `Validated` не монада. Требуется `.andThen`. | ✅ **ПОДТВЕРЖДЕНО** (Ошибка в документации). |
| **FC-14** | `docs/03` утверждает, что `.andThen` на `Validated` не компилируется | REVIEW-B (R4.2) | В Cats `Validated.andThen` специально реализован для последовательной валидации. Текст `docs/03` ошибочен. | ✅ **ПОДТВЕРЖДЕНО** (Неточность в описании Cats). |
| **FC-15** | Утверждение `docs/01`, что `Part.matches` образует предпорядок (транзитивен) | REVIEW-A (3.5), REVIEW-B (R3.3), REVIEW-C (R-16) | Контрпример: `{A: 1} ⊑ {}` и `{} ⊑ {A: 2}`, но `{A: 1} ⋢ {A: 2}`. Отношение рефлексивно, но **не транзитивно** (отношение совместимости/толерантности). | ✅ **ПОДТВЕРЖДЕНО** (Категориальная неточность). |
| **FC-16** | Циклическая зависимость между 4 файлами в `xjdf4s.model` | DEPENDENCY-REPORT, DEPENDENCY-DIAGRAM | `Validation.scala` ⇄ `Product.scala` ⇄ `Ticket.scala` ⇄ `Patch.scala`. Все используют тип `Issue` и взаимные доменные структуры. | ✅ **ПОДТВЕРЖДЕНО** (Нарушение модульности). |
| **FC-17** | Неподключённые инварианты `isLawful` в `TicketValidator` | REVIEW-B (R2.6), REVIEW-C (R-21) | `PartWaste.isLawful`, `Disposition.isLawful`, `Product.hasLawfulAmounts`, `Bom.fromProductList` объявлены, но не вызываются в `TicketValidator.validate`. | ✅ **ПОДТВЕРЖДЕНО** (Пробелы валидации). |
| **FC-18** | Проверка уникальности ResourceSet (§3.4) не учитывает пересечение CombinedProcessIndex | REVIEW-B (R2.4), REVIEW-C (R-08) | `groupBy(_.key)` сверяет точное равенство CPI, пропуская частичные пересечения (например, `[0]` и `[0, 1]`) и пустые значения. | ✅ **ПОДТВЕРЖДЕНО** (Неполная проверка §3.4). |
| **FC-19** | `DropItem` не содержит полей TotalDimensions, TotalVolume, TotalWeight | REVIEW-C (R-11) | Table 6.55 содержит опциональные поля `TotalDimensions`, `TotalVolume`, `TotalWeight`. В `resources/Delivery.scala` их нет. | ✅ **ПОДТВЕРЖДЕНО** (Неполнота структуры). |
| **FC-20** | `Notification` не содержит поля `@ModuleID` и правила Milestone ⇒ Class="Event" | REVIEW-B (R2.10) | Table 8.49 задает `@ModuleID?` и инвариант: если указан `Milestone`, то `@Class` SHALL be `"Event"`. В коде этого нет. | ✅ **ПОДТВЕРЖДЕНО** (Неполнота структуры и валидации). |
| **FC-21** | `Resource.specific` обязателен, хотя в Table 6.1 он опционален | REVIEW-B (R2.7) | В Table 6.1 `<Specific Resource>` имеет кардинальность 0..1 (в чистых partition/amount ресурсах тела может не быть). | ✅ **ПОДТВЕРЖДЕНО** (Чрезмерно строгое поле). |
| **FC-22** | `IdAllocator`/`WithIds`/`IdSource` оторваны от DSL | REVIEW-A (3.2), REVIEW-B (R3.5), REVIEW-C (R-19) | `dsl.TicketDraft` принимает `Option[String]`, игнорируя чистый генератор ID. | ✅ **ПОДТВЕРЖДЕНО** (Мёртвый API). |
| **FC-23** | `PartBuilder.set` бросает исключение вместо типобезопасного API | REVIEW-C (R-18) | При несовпадении типа `PartitionValue` выбрасывается `IllegalArgumentException`, нарушая принцип 5 (`docs/04`). | ✅ **ПОДТВЕРЖДЕНО** (Дефект дизайна API). |
| **FC-24** | Отсутствуют `.github/workflows/ci.yml` и `LICENSE` | REVIEW-C (§4), PROPOSAL-A (5.2), PROPOSAL-B (P-16) | В репозитории нет конфигурации CI для sbt 2.0.2 / Scala 3.8.4 и лицензии Apache-2.0. | ✅ **ПОДТВЕРЖДЕНО** (Инженерный пробел). |

---

### 1.2 Разбор ключевых разногласий и опровержение ложных находок

#### 1. R-01: Вопрос о `Monoid[ValidatedNec[Issue, Unit]]` (Reviewer C vs Reviewer B)
- **Утверждение Reviewer C:** Сборка падает, так как у `NonEmptyChain` нет `Monoid`, а значит `checks.combineAll` требует рукописного инстанса.
- **Факт:** `cats.data.Validated` имеет стандартный инстанс моноида:
  ```scala
  implicit def catsDataMonoidForValidated[E: Semigroup, A: Monoid]: Monoid[Validated[E, A]]
  ```
  Нейтральным элементом выступает `Valid(Monoid[A].empty)` (то есть `Valid(())`). Ошибки комбинируются полугруппой `Semigroup[NonEmptyChain[Issue]]`. В Cats v2.13.0 инстанс присутствует в `cats.kernel.instances.ValidatedInstances`.
- **Решение:** Не вводить избыточный рукописный `Monoid` в `object Issue`, чтобы избежать конфликтов implicit-разрешения. Оставить стандартный механизм Cats.

#### 2. R-03: Поведение `IntegerRange.indices` (Reviewer C)
- **Утверждение Reviewer C:** Нисходящие диапазоны (например, `-1 0`) всегда обходятся по возрастанию, ветка `(lo to hi by -1)` недостижима, тест в `AlgebraLaws` красный.
- **Факт:** Анализ строк 42–46 файла `prim/Quantity.scala`:
  ```scala
  val f = normalizeIndex(r.from, size) // для -1 на размере 3 => 2
  val t = normalizeIndex(r.to, size)   // для  0 на размере 3 => 0
  val lo = math.max(0L, math.min(f, size - 1)) // lo = 2 (clamped from)
  val hi = math.max(0L, math.min(t, size - 1)) // hi = 0 (clamped to)
  if lo <= hi then (lo to hi).toList else (lo to hi by -1).toList
  ```
  Условие `2 <= 0` ложно, выполняется `(2 to 0 by -1).toList`, что даёт `List(2, 1, 0)`. Закон выполняется. Ошибка ревьюера вызвана именами `lo`/`hi`.
- **Решение:** Переименовать переменные в `clampedFrom` и `clampedTo` для исключения двусмысленности.

---

## 2. Архитектурный синтез и ключевые решения (ADR)

### ADR-1: Дизайн ChangeOrder и статус типа Partial
- **Проблема:** Конструкция `type ChangeOrder = XJDF & Partial` при `final case class XJDF(...) extends Partial` семантически вырождена. Спецификация §1.3.2 / §1.6.5 определяет Change Order как тикет с ослабленной кардинальностью (передаются только изменённые поля).
- **Решение:**
    1. В рамках милстоуна **M1**: Зафиксировать, что изменение тикета моделируется моноидом эндоморфизмов `Patch: XJDF => XJDF` с действием `applyTo`.
    2. Исправить неточности в `docs/02-scala3-features.md`, честно описав статус маркерного трейта `Partial`.
    3. Убрать `extends Partial` из `XJDF` либо оформить `ChangeOrder` как номинальный тип `opaque type ChangeOrder = XJDF` с фабрикой `ChangeOrder.of(ticket)` для использования в сигнатурах патчей и кодеков M2.

```scala
// Целевой дизайн:
opaque type ChangeOrder = XJDF

object ChangeOrder:
  def apply(ticket: XJDF): ChangeOrder = ticket
  extension (co: ChangeOrder)
    def toTicket: XJDF = co
    def asPatch: Patch = Patch.fromChangeOrder(co)
```

---

### ADR-2: Устранение циклической зависимости в пакете model
- **Проблема:** В `DEPENDENCY-REPORT.md` зафиксирован 4-узловой цикл между файлами `Validation.scala` ⇄ `Product.scala` ⇄ `Ticket.scala` ⇄ `Patch.scala`.
- **Решение:**
    1. Вынести структуры представления ошибок и базовые типы валидации (`Issue`, `SeverityClass`, `XPath`, тип-алиас `type Validation[A] = ValidatedNec[Issue, A]`) в отдельный независимый файл `modules/core/src/main/scala/xjdf4s/model/ValidationTypes.scala` (или `prim/Validation.scala`).
    2. `Product.scala`, `Patch.scala`, `Ticket.scala` зависят только от `ValidationTypes`.
    3. `TicketValidator.scala` зависит от доменных сущностей и агрегирует проверки. Цикл полностью разрывается.

```
ДО:
[Validation] ──> [Product] ──> [Ticket] ──> [Patch] ──> [Validation] (ЦИКЛ)

ПОСЛЕ:
[ValidationTypes (Issue, XPath)] <─── фундамент (Fan-In: 5, Fan-Out: 0)
        ▲              ▲             ▲
        │              │             │
   [Product]      [Ticket]       [Patch]
        ▲              ▲             ▲
        └──────────────┼─────────────┘
                       │
               [TicketValidator] (корень проверки)
```

---

### ADR-3: Интеграция IdAllocator и IdSource в DSL
- **Проблема:** Чистый монадический генератор `IdSource.fresh: State[Counter, Id]` и контекстная функция `WithIds[A]` не задействованы в конструкторах DSL.
- **Решение:**
    1. Добавить в `XjdfDsl` контекстные методы:
       ```scala
       def inIds[A](body: WithIds[A]): A = IdAllocator.run(body)
       def freshId(prefix: String = "id"): WithIds[Id] = summon[IdAllocator].fresh(prefix)
       ```
    2. Разрешить конструкторам `dsl.resourceSet` и `dsl.product` брать `Id` из контекста `IdAllocator` при `id = None`.
    3. Покрыть генерацию законами уникальности последовательности в `laws`.

---

### ADR-4: Семантическая модель AmountRange (meet/join)
- **Проблема:** В `prim/Quantity.scala` функция `meet` для `amount` выбирала максимум через `stricterMin`, что противоречило текстовому описанию («меньшее обещанное количество»).
- **Решение:**
    1. Чётко зафиксировать алгебраический смысл полурешётки обязательств по Table 6.3:
        - `meet` (пересечение ограничений / ужесточение контракта): нижняя граница `min` повышается (`max(min1, min2)`), верхняя граница `max` понижается (`min(max1, max2)`), номинальный `amount` берётся как более строгий минимум (`min(amount1, amount2)`).
        - `join` (оптимистичное расширение / объединение допусков): нижняя граница `min` понижается (`min(min1, min2)`), верхняя граница `max` повышается (`max(max1, max2)`), `amount` расширяется (`max(amount1, amount2)`).
    2. Добавить `semilatticeLaws[AmountRange]("AmountRange.join")` в `AlgebraLaws`.

---

### ADR-5: Коррекция категориальной строгости (Part.matches)
- **Проблема:** В `docs/01-category-theory-view.md` §3 отношение `Part.matches` названо предпорядком (preorder), что ложно из-за отсутствия транзитивности.
- **Решение:**
    1. Переформулировать в `docs/01`: `Part.matches` — это **отношение совместимости (толерантности)** на множестве частичных спецификаций контекста.
    2. В `PartitionLaws` сохранить свойство рефлексивности и добавить явный юнит-тест с контрпримером к транзитивности:
       ```scala
       test("Part.matches is a tolerance relation (reflexive, non-transitive)"):
         val a = Part.bySide(Side.Front)
         val b = Part.empty
         val c = Part.bySide(Side.Back)
         assert(a.matches(b) && b.matches(c) && !a.matches(c))
       ```

---

## 3. Поэтапный план реализации (Actionable Roadmap)

План разбит на 5 последовательных фаз с чёткими приоритетами (P0–P5), конкретными шагами, затрагиваемыми файлами и кодовыми сигнатурами.

---

### Фаза 0 (P0): Критические блокеры сборки и корректности
*Цель: Устранить функциональные баги ядра, восстановить работоспособность BOM-алгебры и демо.*

#### Задача P0-1. Исправление развёртки BOM в `Bom.toTree` (FC-02)
- **Файл:** `modules/core/src/main/scala/xjdf4s/model/Product.scala`
- **Проблема:** Добавление ID ребёнка в `seen` до входа в рекурсию блокирует валидные деревья.
- **Реализация:**
  ```scala
  // Было (строка 35):
  kid <- child.flatMap(c => toTree(c, byId, seen + c.id.fold("")(_.value)))

  // Стало:
  private def toTree(
      product: Product,
      byId: Map[String, Product],
      seen: Set[String]
  ): Either[Issue, Fix[ProductTree]] =
    val currentIdOpt = product.id.map(_.value)
    currentIdOpt match
      case Some(id) if seen.contains(id) =>
        Left(Issue.error(XPath("/XJDF/ProductList"), s"Cycle in product structure at ID '$id'"))
      case _ =>
        val nextSeen = currentIdOpt.fold(seen)(seen + _)
        val childRefs = product.references.toList.distinct
        childRefs match
          case Nil =>
            Right(Fix(ProductTree.Leaf(product)))
          case refs =>
            val children = refs.foldLeft(Right(Chain.empty[Fix[ProductTree]]): Either[Issue, Chain[Fix[ProductTree]]]) {
              case (acc, ref) =>
                for
                  kids  <- acc
                  child <- byId.get(ref.value).toRight(
                             Issue.error(XPath("/XJDF/ProductList"), s"Unresolved ChildRef '${ref.value}'")
                           )
                  kid   <- toTree(child, byId, nextSeen)
                yield kids :+ kid
            }
            children.map(cs => Fix(ProductTree.Node(product, cs)))
  ```
- **Тесты:** Добавить тест в `TicketLaws`: развёртка `SpecExamples.notebook` должна возвращать `Right(forest)` и успешно вычислять `Bom.totalCopies`.

---

#### Задача P0-2. Исправление компилируемости примера в `README.md` (FC-13)
- **Файл:** `README.md`
- **Проблема:** Использование `.flatMap` на `ValidatedNec`.
- **Реализация:**
  ```markdown
  ```scala
  import xjdf4s.dsl.dsl
  import xjdf4s.model.*

  // Example 3.1 спецификации: <XJDF JobID="J1" Types="Product" Version="2.2"/>
  val ticket: ValidatedNec[Issue, XJDF] =
    dsl.TicketDraft.of("J1", ProcessType.Product).andThen(_.build)
  ```
  ```

---

#### Задача P0-3. Исправление логики замещения в `Patch.mergeResourceSets` (FC-11)
- **Файл:** `modules/core/src/main/scala/xjdf4s/model/Patch.scala`
- **Проблема:** Конфликтующие наборы ресурсов дублировались вместо перезаписи.
- **Реализация:**
  ```scala
  def mergeResourceSets(ticket: XJDF, update: Chain[ResourceSet]): Ior[NonEmptyChain[Issue], XJDF] =
    val updateKeys = update.map(_.key).toList.toSet
    val conflicts  = ticket.resourceSets.filter(rs => updateKeys.contains(rs.key))
    val retained   = ticket.resourceSets.filterNot(rs => updateKeys.contains(rs.key))
    val mergedTicket = ticket.copy(resourceSets = retained ++ update)

    if conflicts.isEmpty then Ior.right(mergedTicket)
    else
      val issues = conflicts.map: rs =>
        Issue.warning(
          XPath("/XJDF/ResourceSet"),
          s"Duplicate ResourceSet key replaced: ${Show[ResourceSetKey].show(rs.key)}"
        )
      NonEmptyChain.fromChain(issues) match
        case Some(nec) => Ior.both(nec, mergedTicket)
        case None      => Ior.right(mergedTicket)
  ```
- **Тесты:** Добавить property-тест в `TicketLaws`: после `mergeResourceSets` в тикете отсутствуют дубликаты `ResourceSetKey`.

---

### Фаза 1 (P1): Соответствие спецификации XJDF 2.2 и целостность домена
*Цель: Приведение типов, кардинальностей и перечислений в 100% соответствие со спецификацией CIP4.*

#### Задача P1-1. Исправление типов `Part/@ProductPart` и `Part/@Metadata` (FC-05, FC-06)
- **Файлы:**
    - `modules/core/src/main/scala/xjdf4s/prim/Tokens.scala`
    - `modules/core/src/main/scala/xjdf4s/model/Partition.scala`
- **Реализация:**
    1. Добавить `RegExp` opaque type в `Tokens.scala`:
       ```scala
       opaque type RegExp = String
       object RegExp:
         def from(raw: String): Option[RegExp] =
           if raw != null && raw.nonEmpty then
             try { java.util.regex.Pattern.compile(raw); Some(raw) }
             catch { case _: java.util.regex.PatternSyntaxException => None }
           else None
         def unsafe(raw: String): RegExp =
           from(raw).getOrElse(throw new IllegalArgumentException(s"Invalid RegExp pattern: '$raw'"))
         extension (r: RegExp) def value: String = r
         given Show[RegExp] = Show.show(_.value)
         given Eq[RegExp] = Eq.fromUniversalEquals
       ```
    2. В `model/Partition.scala`:
        - Изменить поле `productPart: Option[NmToken] = None` (вместо `Option[IdRef]`).
        - Изменить поле `metadata: Option[RegExp] = None` (вместо `Option[NmToken]`).
        - Обновить `PartitionValue`:
          ```scala
          case ProductRef(value: NmToken)
          case RegExpVal(value: RegExp)
          ```
        - Скорректировать match type `ValueOf`:
          ```scala
          case PartitionKey.ProductPart.type => NmToken
          case PartitionKey.Metadata.type    => RegExp
          ```
        - Конструктор `byProductPart(value: NmToken)` (вместо `byProductRef`).

---

#### Задача P1-2. Дополнение перечислений Appendix A и исправление токенов (FC-08, FC-09)
- **Файл:** `modules/core/src/main/scala/xjdf4s/prim/Enums.scala`
- **Реализация:**
    1. `DeviceStatus`: добавить `Cleanup` и `Setup` (Table A.15):
       ```scala
       enum DeviceStatus extends XjdfEnum:
         case Cleanup, Idle, NonProductive, Offline, Production, Setup, Stopped
         def token: NmToken = NmToken.unsafe(this.toString)
       object DeviceStatus extends XjdfEnumCompanion[DeviceStatus]:
         val all: List[DeviceStatus] = List(Cleanup, Idle, NonProductive, Offline, Production, Setup, Stopped)
       ```
    2. `Sides`: добавить `Unprinted` (Table A.40):
       ```scala
       enum Sides extends XjdfEnum:
         case OneSided, OneSidedBack, TwoSidedHeadToFoot, TwoSidedHeadToHead, Unprinted
         def token: NmToken = NmToken.unsafe(this.toString)
       object Sides extends XjdfEnumCompanion[Sides]:
         val all: List[Sides] = List(OneSided, OneSidedBack, TwoSidedHeadToFoot, TwoSidedHeadToHead, Unprinted)
       ```
    3. `HardCoverJacket`: исправить `Glued` на `Glue` (Table 4.11):
       ```scala
       enum HardCoverJacket extends XjdfEnum:
         case Unjacketed, Loose, Glue
         def token: NmToken = this match
           case Unjacketed => NmToken.unsafe("None")
           case other      => NmToken.unsafe(other.toString)
       ```

---

#### Задача P1-3. Исправление кардинальности `PartAmount.parts` и валидация Table 6.3 (FC-10)
- **Файлы:**
    - `modules/core/src/main/scala/xjdf4s/model/Amounts.scala`
    - `modules/core/src/main/scala/xjdf4s/model/Validation.scala`
- **Реализация:**
    1. В `PartAmount`:
       ```scala
       final case class PartAmount(
           amount: Option[Amount] = None,
           maxAmount: Option[Amount] = None,
           minAmount: Option[Amount] = None,
           waste: Option[Amount] = None,
           parts: Chain[Part] = Chain.empty,
           partWaste: Chain[PartWaste] = Chain.empty
       ):
         def part: Option[Part] = parts.headOption
       ```
    2. В `TicketValidator.checkPartAmountKeys`:
        - Реализовать оба правила Table 6.3:
          а) PartAmount/Part не содержит ключей, однозначно заданных в родительском Resource/Part.
          б) Если PartAmount/Part задаёт тот же ключ, его значение обязано совпадать с одним из значений родителя.

---

#### Задача P1-4. Исправление 7 ссылок на таблицы в scaladoc ресурсов (FC-07)
- **Файлы:** `modules/core/src/main/scala/xjdf4s/resources/*`
- **Правки:**
    - `resources/Color.scala:7`: `Table 6.14` ➔ `Table 6.27: Color Resource`
    - `resources/Finishing.scala:9`: `Table 6.25` ➔ `Table 6.53: CuttingParams Resource`
    - `resources/Finishing.scala:44`: `Table 6.36` ➔ `Table 6.74: FoldingParams Resource`
    - `resources/Layout.scala:8`: `Table 6.52` ➔ `Table 6.95: Layout Resource`
    - `resources/Media.scala:8`: `Table 6.57` ➔ `Table 6.114: Media Resource`
    - `resources/NodeInfo.scala:7`: `Table 6.59` ➔ `Table 6.119: NodeInfo Resource`
    - `resources/Preview.scala:8`: `Table 6.66` ➔ `Table 6.134: Preview Resource`

---

#### Задача P1-5. Подключение всех инвариантов `isLawful` и BOM в `TicketValidator` (FC-17)
- **Файл:** `modules/core/src/main/scala/xjdf4s/model/Validation.scala`
- **Реализация:** Добавить в pipeline валидации:
    1. `checkProductAmounts(ticket)`: проверка `Product.hasLawfulAmounts`.
    2. `checkBomIntegrity(ticket)`: вызов `Bom.fromProductList` для проверки ацикличности и валидности `@ChildRefs`.
    3. `checkPartWasteLawfulness(ticket)`: проверка `PartWaste.isLawful` (наличие `@ModuleIDs` или `@WasteDetails`).
    4. `checkIntentPayloadLawfulness(ticket)`: делегирование в `BindingIntent.isLawful` и `FoldingVariableIntent.isLawful`.

---

#### Задача P1-6. Полная проверка уникальности ResourceSet по §3.4 (FC-18)
- **Файл:** `modules/core/src/main/scala/xjdf4s/model/Validation.scala`
- **Реализация:**
  ```scala
  private def checkResourceSetKeys(ticket: XJDF): ValidatedNec[Issue, Unit] =
    val sets = ticket.resourceSets.toList
    val conflicts = for
      (s1, idx1) <- sets.zipWithIndex
      (s2, idx2) <- sets.zipWithIndex if idx1 < idx2
      if s1.name == s2.name && s1.usage == s2.usage && s1.processUsage == s2.processUsage
      if cpiOverlap(s1.combinedProcessIndex, s2.combinedProcessIndex)
    yield s"${s1.name.toNmToken.value} (indices $idx1 and $idx2)"

    Validated.condNec(
      conflicts.isEmpty,
      (),
      Issue.error(XPath("/XJDF/ResourceSet"), s"Conflicting ResourceSets per §3.4: ${conflicts.mkString("; ")}")
    )

  private def cpiOverlap(
      a: Option[NonEmptyChain[ProcessIndex]],
      b: Option[NonEmptyChain[ProcessIndex]]
  ): Boolean =
    (a, b) match
      case (None, _) | (_, None) => true // No entries = applies to all => conflict!
      case (Some(cpi1), Some(cpi2)) =>
        val s1 = cpi1.toChain.toList.map(_.value).toSet
        val s2 = cpi2.toChain.toList.map(_.value).toSet
        s1.intersect(s2).nonEmpty
  ```

---

#### Задача P1-7. Дополнение `DropItem`, `Notification` и `Resource.specific` (FC-19, FC-20, FC-21)
- **Файлы:**
    - `modules/core/src/main/scala/xjdf4s/resources/Delivery.scala`
    - `modules/core/src/main/scala/xjdf4s/model/Header.scala`
    - `modules/core/src/main/scala/xjdf4s/model/Resource.scala`
- **Реализация:**
    1. `DropItem`:
       ```scala
       final case class DropItem(
           amount: Long,
           itemRef: IdRef,
           totalDimensions: Option[Shape] = None,
           totalVolume: Option[Double] = None,
           totalWeight: Option[Double] = None
       )
       ```
    2. `Notification`:
       ```scala
       final case class Notification(
           classification: SeverityClass,
           jobId: Option[JobId] = None,
           jobPartId: Option[JobPartId] = None,
           moduleId: Option[NmToken] = None,
           queueEntryId: Option[NmToken] = None,
           detail: Option[NotificationDetail] = None,
           parts: Chain[Part] = Chain.empty,
           comments: Chain[Comment] = Chain.empty
       ):
         def isLawful: Boolean = detail match
           case Some(_: Milestone) => classification == SeverityClass.Event
           case _                  => true
       ```
    3. `Resource`:
       ```scala
       final case class Resource(
           specific: Option[ResourcePayload] = None,
           id: Option[Id] = None,
           ...
       )
       ```

---

### Фаза 2 (P2): Архитектурная стабилизация, алгебры и качество кода
*Цель: Разрыв циклов модульности, усиление математических структур и рефакторинг API.*

#### Задача P2-1. Разрыв цикла зависимостей `model` (FC-16)
- **Создать файл:** `modules/core/src/main/scala/xjdf4s/model/ValidationTypes.scala`
- **Содержимое:**
    - Перенести `final case class Issue(...)`, `object Issue`, `SeverityClass`, `XPath`.
    - Задать общий тип-алиас: `type ValidationResult[A] = ValidatedNec[Issue, A]`.
- **Результат:** Зависимости становятся строго направленными: `ValidationTypes` ➔ `{Product, Ticket, Patch}` ➔ `TicketValidator`. Граф зависимостей ацикличен (0 циклов в `DEPENDENCY-REPORT`).

---

#### Задача P2-2. Рефакторинг `Quantity.IntegerRange` (FC-03)
- **Файл:** `modules/core/src/main/scala/xjdf4s/prim/Quantity.scala`
- **Реализация:** Заменить имена `lo`/`hi` на `clampedFrom`/`clampedTo`:
  ```scala
  def indices(size: Long): List[Long] =
    if size <= 0 then Nil
    else
      val f = normalizeIndex(r.from, size)
      val t = normalizeIndex(r.to, size)
      val clampedFrom = math.max(0L, math.min(f, size - 1))
      val clampedTo   = math.max(0L, math.min(t, size - 1))
      if clampedFrom <= clampedTo then (clampedFrom to clampedTo).toList
      else (clampedFrom to clampedTo by -1).toList
  ```

---

#### Задача P2-3. Интеграция `IdAllocator` и `IdSource` в DSL (FC-22)
- **Файл:** `modules/core/src/main/scala/xjdf4s/dsl/XjdfDsl.scala`
- **Реализация:**
    - Добавить контекстный блок `inIds[A](body: WithIds[A]): A`.
    - В методах `product` и `resourceSet` добавить поддержку неявного `summon[IdAllocator]` при отсутствии явного ID.
    - Пометить `IdAllocator.stateful` аннотацией `@threadUnsafe` с документацией о pure State-альтернативе.

---

#### Задача P2-4. Безопасный API для `PartBuilder` (FC-23)
- **Файл:** `modules/core/src/main/scala/xjdf4s/model/Partition.scala`
- **Реализация:**
    - Метод `setSafe(key: PartitionKey, value: PartitionValue): Either[Issue, Part]`.
    - Метод `setUnsafe(key: PartitionKey, value: PartitionValue): Part` (явное указание на возможность выброса исключения).

---

#### Задача P2-5. Уточнение области уникальности ID в `declaredIds`
- **Файл:** `modules/core/src/main/scala/xjdf4s/model/Ticket.scala`
- **Реализация:** Исключить `origin.id` из `Header` аудитов из документной проверки `checkIdUniqueness`, так как `Header/@ID` имеет мессенджинговый скоуп (Table 7.3), либо выделить в отдельную проверку уникальности сообщений.

---

#### Задача P2-6. Усиление алгебр (CommutativeMonoid, Group)
- **Файлы:**
    - `modules/core/src/main/scala/xjdf4s/prim/Quantity.scala`
    - `modules/core/src/main/scala/xjdf4s/prim/Time.scala`
- **Реализация:**
    - `CommutativeMonoid[XYPair]`, `CommutativeMonoid[TimeSpan]`.
    - Документировать `Matrix` как группу Ли над аффинными преобразованиями плоскости с частичной операцией взятия обратной матрицы (`inverse: Option[Matrix]`).

---

#### Задача P2-7. Стек-безопасный `Bom.cata` на `Eval`
- **Файл:** `modules/core/src/main/scala/xjdf4s/model/Product.scala`
- **Реализация:**
  ```scala
  def cataEval[A](algebra: ProductTree[A] => Eval[A])(tree: Fix[ProductTree]): Eval[A] =
    tree.unfix match
      case ProductTree.Leaf(p) => algebra(ProductTree.Leaf(p))
      case ProductTree.Node(p, kids) =>
        kids.traverse(cataEval(algebra)).flatMap(cs => algebra(ProductTree.Node(p, cs)))
  ```

---

### Фаза 3 (P3): Документация, категориальная строгость и Developer Experience
*Цель: Устранение неточностей в `docs/*`, актуализация README и синхронизация с теорией.*

#### Задача P3-1. Коррекция `docs/01-category-theory-view.md` (FC-15)
- Заменить термин «preorder / тонкая категория» для `Part.matches` на **«отношение совместимости (толерантности)»**.
- Добавить диаграмму отношений наложения и полурешёток `AmountRange`.

#### Задача P3-2. Коррекция `docs/02-scala3-features.md` (FC-12)
- Актуализировать раздел про Intersection Types: честно зафиксировать роль маркерных типов и переход к nominal opaque для `ChangeOrder`.

#### Задача P3-3. Коррекция `docs/03-cats-mapping.md` (FC-14)
- Удалить ошибочное утверждение о том, что `.andThen` не компилируется на `Validated`. Показать канонический пример `Validated.andThen`.

#### Задача P3-4. Добавление файла `LICENSE` (FC-24)
- Создать в корне репозитория файл `LICENSE` с лицензией **Apache License 2.0**.

#### Задача P3-5. Создание автоматического реестра покрытия спецификации
- Создать документ `docs/SPEC-COVERAGE.md` со сквозной таблицей: `Раздел XJDF` ➔ `Номер таблицы` ➔ `Scala-тип` ➔ `Статус покрытия`.

#### Задача P3-6. Примеры спецификации как эталонные тесты (Golden Tests)
- Перенести проверку валидности всех примеров из `SpecExamples` (`minimalProduct`, `notebook`, `combinedProcesses`, `splitDelivery`, `brochureJob`) в регулярный тестовый сьют `TicketLaws`.

---

### Фаза 4 (P4): Инженерная инфраструктура и CI/CD
*Цель: Обеспечение непрерывной верификации и воспроизводимости сборки.*

#### Задача P4-1. Настройка GitHub Actions CI Workflow
- **Создать файл:** `.github/workflows/ci.yml`
- **Конфигурация:**
  ```yaml
  name: CI

  on:
    push:
      branches: [ "main", "develop", "arena/**" ]
    pull_request:
      branches: [ "main", "develop" ]

  jobs:
    build:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v4
        - name: Set up JDK 21
          uses: actions/setup-java@v4
          with:
            java-version: '21'
            distribution: 'temurin'
            cache: 'sbt'
        - name: Compile and Test
          run: |
            sbt -batch compile
            sbt -batch test
            sbt -batch examples/run
  ```

#### Задача P4-2. Гигиена VCS
- Убедиться, что `*.log` исключён из отслеживания, а любые промежуточные логи тестирования не попадают в git-индекс.

---

### Фаза 5 (P5): Дорожная карта следующих милстоунов (M2–M6)
*Цель: Долгосрочное развитие проекта в соответствии с ROADMAP.*

- **Милстоун M2 (Кодеки XML / JSON):**
    - Разработка `xjdf4s-codec-xml` на базе `fs2-data-xml` или `scala-xml`.
    - Разработка `xjdf4s-codec-json` с поддержкой JSON-исключений XJDF (§1.4.2: `$schema`, `@Name`, массивы `AuditPool`).
    - Round-trip property-тесты: `decode(encode(ticket)) == ticket`.
    - Парсинг строковых примитивов через `cats-parse`.

- **Милстоун M3 (Каталог ресурсов главы 6):**
    - Реализация оставшихся ~130 ресурсов пакетами по алфавиту с генератором типовых структур.
    - Реестр `IntentPairing` на уровне типов.

- **Милстоун M4 (XJMF Messaging, глава 7):**
    - Поддержка семейств `Command`, `Response`, `Query`, `Signal`.
    - Выравнивание сообщений с аудитами через `Alignment` (Table 3.2).

- **Милстоун M5 (Workflow & Pipelines):**
    - Конвейер тикетов как композиция стрелок категории.
    - Потоковая обработка сигналов через `fs2` и `WriterT`.

- **Милстоун M6 (Релиз и публикация):**
    - Проверка бинарной совместимости (MiMa).
    - Публикация в Maven Central через `sbt-typelevel`.

---

## 4. Матрица сквозной трассируемости (Traceability Matrix)

| Находка ревью | Документ-источник | Пункт Consolidated PLAN | Затронутые файлы | Приоритет |
|---|---|---|---|---|
| R-01 (Validated Monoid) | REVIEW-C / PROPOSAL-C | Раздел 1.2 (Опровергнуто) | — | — |
| R-02 (BOM cycle false positive) | REVIEW-C / PROPOSAL-C | **P0-1** | `model/Product.scala` | **P0** |
| README snippet flatMap | REVIEW-B / REVIEW-C | **P0-2** | `README.md` | **P0** |
| Patch.mergeResourceSets bug | REVIEW-B / PROPOSAL-B | **P0-3** | `model/Patch.scala` | **P0** |
| GitHub Actions CI | REVIEW-C / PROPOSAL-A/B | **P4-1** | `.github/workflows/ci.yml` | **P0** |
| ProductPart: IdRef ➔ NmToken | REVIEW-A / REVIEW-B / C | **P1-1** | `model/Partition.scala` | **P1** |
| Metadata: NmToken ➔ RegExp | REVIEW-A / REVIEW-B / C | **P1-1** | `prim/Tokens.scala`, `model/Partition.scala` | **P1** |
| Enums: Cleanup, Setup, Unprinted | REVIEW-B / REVIEW-C | **P1-2** | `prim/Enums.scala` | **P1** |
| HardCoverJacket: Glued ➔ Glue | REVIEW-B | **P1-2** | `prim/Enums.scala` | **P1** |
| PartAmount.parts: Chain[Part] | REVIEW-B / REVIEW-C | **P1-3** | `model/Amounts.scala`, `model/Validation.scala` | **P1** |
| 7 Scaladoc table references | REVIEW-A / REVIEW-B / C | **P1-4** | `resources/*.scala` (7 файлов) | **P1** |
| Подключение isLawful в Validator | REVIEW-B / REVIEW-C | **P1-5** | `model/Validation.scala` | **P1** |
| §3.4 CPI overlap uniqueness | REVIEW-B / REVIEW-C | **P1-6** | `model/Validation.scala` | **P1** |
| DropItem / Notification completeness| REVIEW-B / REVIEW-C | **P1-7** | `resources/Delivery.scala`, `model/Header.scala` | **P1** |
| Resource.specific: Option | REVIEW-B | **P1-7** | `model/Resource.scala` | **P1** |
| Цикл зависимостей в model | DEPENDENCY-REPORT | **P2-1** | `model/ValidationTypes.scala`, `model/*.scala` | **P2** |
| IntegerRange lo/hi naming | REVIEW-C | **P2-2** | `prim/Quantity.scala` | **P2** |
| IdAllocator wire-up | REVIEW-A / REVIEW-B / C | **P2-3** | `dsl/XjdfDsl.scala` | **P2** |
| PartBuilder safe API | REVIEW-C | **P2-4** | `model/Partition.scala` | **P2** |
| declaredIds Header scope | REVIEW-A | **P2-5** | `model/Ticket.scala` | **P2** |
| CommutativeMonoid / Group | REVIEW-A | **P2-6** | `prim/Quantity.scala`, `prim/Time.scala` | **P2** |
| Bom.cataEval | REVIEW-B / REVIEW-C | **P2-7** | `model/Product.scala` | **P2** |
| docs/01 tolerance relation | REVIEW-A / REVIEW-B / C | **P3-1** | `docs/01-category-theory-view.md` | **P3** |
| docs/02 intersection types | REVIEW-A / REVIEW-B / C | **P3-2** | `docs/02-scala3-features.md` | **P3** |
| docs/03 andThen description | REVIEW-B | **P3-3** | `docs/03-cats-mapping.md` | **P3** |
| LICENSE file | REVIEW-C | **P3-4** | `LICENSE` | **P3** |
| Golden Tests for Spec Examples | PROPOSAL-B / PROPOSAL-C | **P3-6** | `laws/TicketLaws.scala` | **P3** |

---

## 5. Критерии приёмки и протокол валидации (Definition of Done)

После реализации намеченных фаз проект считается полностью стабилизированным и готовым к этапу кодеков M2 при выполнении следующих условий:

1. **Компиляция и сборка:**
    - Команда `sbt compile` выполняется успешно без фатальных ошибок.
    - Компилятор не генерирует предупреждений при флагах `-Wunused:all -Wvalue-discard -Wnonunit-statement`.
2. **Тестирование и законы:**
    - Команда `sbt test` проходит на 100% (все 4 сьюта `AlgebraLaws`, `AlignmentLaws`, `PartitionLaws`, `TicketLaws` зелёные).
    - Развёртка дерева BOM `Bom.fromProductList` успешно работает на всех спецификационных примерах с дочерними ссылками.
    - Демо `sbt examples/run` выполняется от начала до конца без исключений и выводит корректно рассчитанные копии продуктов.
3. **Соответствие спецификации CIP4:**
    - `Part.productPart` имеет тип `Option[NmToken]`.
    - `Part.metadata` имеет тип `Option[RegExp]`.
    - `PartAmount.parts` имеет тип `Chain[Part]`.
    - Все 45+ перечислений совпадают с таблицами Appendix A и разделов 3–8.
    - Все ссылки в Scaladoc указывают на существующие таблицы спецификации.
4. **Архитектурная чистота:**
    - Граф зависимостей между файлами пакета `model` не содержит циклов.
    - Все объявленные методы `isLawful` вызываются из корневого валидатора `TicketValidator`.
5. **Документация и репозиторий:**
    - Документы `docs/01`, `docs/02`, `docs/03` актуализированы и не содержат теоретических противоречий.
    - В репозитории присутствует файл `LICENSE` (Apache-2.0) и рабочий пайплайн `.github/workflows/ci.yml`.