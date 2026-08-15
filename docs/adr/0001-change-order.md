# ADR-0001 — ChangeOrder как номинальный partial-документ

- **Статус:** принято
- **Дата:** 2026-08-15
- **Задача:** M1.4-2 (PR-10); дедлайн по ROADMAP §6 — «до M1.4-2»
- **Закрывает находку:** N-20
- **Связанные ADR:** ADR-0002 (слои валидации; `applyChange` не должен вернуть цикл), ADR-0006 (повторная валидация результата)

---

## Context

На срезе M1.4-1 change order смоделирован вырожденным пересечением:

```scala
trait Partial
final case class XJDF(...) extends Partial
type ChangeOrder = XJDF & Partial
```

Поскольку `XJDF <: Partial`, пересечение `XJDF & Partial` тождественно `XJDF`. Ни одна сигнатура публичного API не принимает `ChangeOrder`. Тип семантически пуст (N-20): он не выражает ослабленную кардинальность, не отличает входной документ от операции и не заставляет повторно валидировать результат.

Нормативная база различает три вещи, которые пересечение смешивает.

**§1.3.2 «Enable dynamic changes»** (`reference/xjdf/1 – Introduction.md`):

> The simplest method of initiating a change transaction is to send an XJDF that contains only the modified values. Only the explicitly stated values will then be modified.

**§1.4.1.3 «Schema and XJDF Context»:**

> As change orders can only be used to alter an existing job, mandatory content will have been delivered to the executing Device by the original job instruction, and the change order does not need to convey this same data again. In fact, the XJDF document being used for a change order SHOULD only describe those values that have changed.
>
> CIP4 provides two XML schema definitions for use with XJDF depending upon which context the XJDF document is being used in. … For change orders, most attributes and elements have been made optional in the schema …

**§1.6.5 «Specification of Cardinality» / Table 1.2:**

> The cardinality for XJDF and any child elements applies to original job instruction XJDF documents that are submitted to a Device. In case of change orders, i.e. XJDF that is referenced by a `CommandResubmitQueueEntry`, the cardinality restrictions are loosened and all elements and attributes that are not required to identify the context of the change order become optional.
>
> Note: The XML schema for change orders is designed to reflect this loosened state.

**Table 7.56 `ResubmissionParams` / §9.8.2.1.1:**

- `XJDF/@JobID` SHALL быть тождественен `@JobID` исходного тикета;
- XJDF совпадает, если тождественны и `@JobID`, и `@JobPartID`;
- `@UpdateMethod="Incremental"`: «All Traits of the referenced XJDF are optional and only values that are explicitly specified in the referenced XJDF SHALL be modified.»

`reference/xjdf/schema.xsd` — это **жёсткая** схема original job instruction (`XJDF/@JobID` и `@Types` — `use="required"`). Отдельной change-order-схемы в репозитории нет: §1.4.1.3 отсылает к [XJDF Schema Repository]. Это ожидаемо и фиксируется здесь: домен M1 моделирует ослабленную кардинальность типом, а не второй копией XSD.

Примеры §9.8.3 (9.5–9.10) все несут `@JobID`, `@JobPartID`, `@Types` и один или несколько `ResourceSet`. Ни один не несёт `ProductList`, `AuditPool` или `Comment`. `@Types` присутствует потому, что примеры сериализованы как обычный XJDF (жёсткая схема); контекст адресации по §9.8.2.1.1 — это `@JobID` + `@JobPartID`, не `@Types`.

## Decision

### Вариант C — три сущности

Разделить то, что пересечение смешивало:

1. **`ChangeOrder`** — входной partial-документ (`final case class` с `Option`/`Chain`-полями). Обязателен только контекст адресации.
2. **`Patch`** — уже существующий моноид эндоморфизмов `XJDF => XJDF` (`combine = andThen`). Нормализованная операция, не документ.
3. **Результат применения** — `ValidatedNec[Issue, XJDF]`. Change order способен нарушить инварианты целевого тикета (§1.6.5); применение **обязано** повторно прогнать корневой валидатор.

`trait Partial` и `type ChangeOrder = XJDF & Partial` удаляются.

### Финальный набор полей

Сверка §1.3.2, §1.6.5, Table 1.2, Table 3.1, Table 7.56, §9.8.2 / §9.8.2.1.1–9.8.2.1.2, примеров 9.5–9.10 и `schema.xsd` (`element name="XJDF"`) закрывает единственный открытый вопрос ADR. Стартовая сигнатура ROADMAP §6 принимается **без расширения**.

```scala
/** §1.3.2, §1.6.5: a change order carries only the modified values. */
final case class ChangeOrder(
    jobId: JobId,                                    // обязательный контекст
    jobPartId: Option[JobPartId] = None,             // уточнение контекста
    productList: Option[ProductList] = None,         // replace
    auditPool: Option[AuditPool] = None,             // append, chronologically
    resourceSets: Chain[ResourceSet] = Chain.empty,  // upsert by §3.4 predicate
    comments: Chain[Comment] = Chain.empty           // append
)
```

| Поле | Кардинальность change order | Операция при compile | Нормативное основание |
| --- | --- | --- | --- |
| `jobId` | обязателен | контекст: SHALL совпасть с `base.jobId` | Table 7.56: «`XJDF/@JobID` SHALL be identical to `XJDF/@JobID` of the originally submitted XJDF»; §9.8.2.1.1 |
| `jobPartId` | `?` | если задан — SHALL совпасть с `base.jobPartId`; `None` не фильтрует | §9.8.2.1.1; Table 9.2 (`Complete` без `@JobPartID` адресует всю очередь — вне скоупа одного тикета M1) |
| `productList` | `?` | replace (`Some` замещает BOM целиком) | Table 3.1 `ProductList?`; §1.3.2 «only the explicitly stated values» |
| `auditPool` | `?` | append через `Semigroup[AuditPool]` (хронологическая конкатенация) | Table 3.1 `AuditPool?`; §3.2 «ordered chronologically, last = newest» |
| `resourceSets` | `*` | upsert: замещение конфликтующих по `ResourceSet.clashesWith` (§3.4), сохранение остальных | Table 3.1 `ResourceSet*`; §9.8.2.1.2 (совпадение по `@Name`/`@ProcessUsage`/`@Usage`); M1.1-3 |
| `comments` | `*` | append | Table 3.1 `Comment*` |

Семантика `compile` — **Incremental** (Table 7.56 / §9.8.2): присутствует только то, что меняется; отсутствующее не трогается. `Complete` и `Remove` живут на `ResubmissionParams/@UpdateMethod` (XJMF) и откладываются в M4 вместе с `CommandResubmitQueueEntry`.

### Что сознательно не входит в M1.4-2

Эти поля/операции нормативны для полного ChangeOrder-документа, но не нужны, чтобы закрыть N-20 и выразить §1.3.2 / Incremental. Расширение — отдельное решение (M4 или точечный ADR), не молчаливое добавление.

| Отсутствует | Почему не в M1.4-2 |
| --- | --- |
| `@Types: Option[NonEmptyChain[ProcessType]]` | не идентифицирует контекст (§9.8.2.1.1). В примерах 9.5–9.10 есть только потому, что они сериализованы жёсткой схемой (`schema.xsd`: `Types use="required"`). Замена `@Types` — Complete-семантика / новый process step (Table 9.2 Incremental + new `@JobPartID`) |
| `@Category`, `@CommentURL`, `@DescriptiveName`, `@ICSVersions`, `@ProjectID`, `@RelatedJobID`/`@RelatedJobPartID`/`@RelatedProjectID`, `@Version` | простые Option-replace корневых атрибутов Table 3.1; ни один пример §9.8.3 их не меняет |
| `GeneralID*` | NamedFeatures §3.1.3.1; нет Incremental-примера |
| partition-level match внутри `ResourceSet` (§9.8.2.1.2: совпадение `Resource/Part`) | M1.1-3 уже дал ResourceSet-level upsert через `clashesWith`; более тонкое слияние Part — M4 |
| `UpdateMethod` | атрибут `ResubmissionParams` (XJMF), не ребёнок `XJDF` |

### Компиляция и применение

```scala
object ChangeOrder:
  /** Compiles a change order against a base ticket into a lawful endomorphism. */
  def compile(change: ChangeOrder, base: XJDF): ValidatedNec[Issue, Patch]

  def applyChange(base: XJDF, change: ChangeOrder): ValidatedNec[Issue, XJDF]
```

`compile` проверяет:

1. контекст (`jobId` / опциональный `jobPartId`);
2. внутреннюю непротиворечивость `resourceSets` через уже существующий `Patch.mergeResourceSets` (ветка `Ior.Left` = §3.4 внутри самого update).

и собирает `Patch` композицией (`andThen` = `Monoid[Patch].combine`):

- `productList` → `Patch.withProductList`;
- `auditPool` → `Patch.appendAudits`;
- `resourceSets` → тотальный upsert (после проверки `Ior.Left` недостижим);
- `comments` → последовательный `Patch.addComment`.

`compile` сообщает **только ошибки**. Предупреждения `Ior.Both` («старый ResourceSet замещён») — ожидаемая Incremental-семантика, не дефект; они остаются на прямых вызовах `mergeResourceSets`. Нарушения инвариантов *результата* (хронология, §6.1.2.1, §3.1.3, BOM, …) ловит повторная валидация, а не `compile`.

`applyChange`:

```scala
def applyChange(base: XJDF, change: ChangeOrder): ValidatedNec[Issue, XJDF] =
  ChangeOrder.compile(change, base).andThen { patch =>
    val result = Patch.applyTo(patch)(base)
    TicketValidator.validate(result).as(result)
  }
```

`Patch.applyTo` остаётся тотальным эндоморфизмом (`XJDF => XJDF`). Набросок ROADMAP, в котором `applyTo` возвращал `ValidatedNec`, отвергнут: он сломал бы `Monoid[Patch]` и закон действия. Валидация — снаружи, на результате.

### Размещение и направление зависимостей

`ChangeOrder` живёт в новом файле `model/ChangeOrder.scala` (тот же пакет `xjdf4s.model`). Файл зависит от `Ticket`, `Patch`, `TicketValidator` и `ValidationTypes`. `TicketValidator` **не** импортирует `ChangeOrder` — цикл ADR-0002 не возвращается:

```
ChangeOrder → TicketValidator → Ticket
ChangeOrder → Patch           → Ticket
                  ValidationTypes   (Fan-Out 0)
```

`Ticket.scala` после удаления `Partial` больше не знает ни о `Patch`, ни о валидаторе.

## Alternatives

| Вариант | Суть | Оценка |
| --- | --- | --- |
| **A** | Убрать `Partial`, change order — только `Patch` | Честно как операция, но теряется представление входящего документа §1.3.2 / §1.4.1.3. Кодеки M2 и `CommandResubmitQueueEntry` (M4) не получили бы куда декодировать partial-XJDF. |
| **B** | `opaque type ChangeOrder = XJDF` | Даёт номинал, но **не выражает** ослабленную кардинальность: `JobID` и `Types` остаются обязательными, «отправить только изменённые значения» нетипизируемо. |
| **C** | Отдельный `final case class` + компиляция в `Patch` | Единственный, который одновременно (i) несёт partial-документ, (ii) сохраняет моноид эндоморфизмов, (iii) заставляет ревалидировать результат. **Принят.** |
| **C′** | Вариант C, но `ChangeOrder` зеркалирует *все* поля Table 3.1 как `Option` | Максимально близок к «второй схеме». Отклонён для M1.4-2: раздувает API ради атрибутов, которые ни один нормативный пример не меняет; наращивается в M4 без ломки архитектуры. |
| **Оставить `XJDF & Partial`** | Текущее состояние | Отклонено: при `XJDF <: Partial` пересечение вырождено (N-20). |

## Consequences

**Положительные**

- Публичный API впервые принимает `ChangeOrder` как отдельный тип; N-20 закрыта.
- Ослабленная кардинальность §1.6.5 выражена структурой, а не комментарием.
- `Patch` остаётся чистым моноидом; закон `applyTo(applyTo(t, p), q) == applyTo(t, p |+| q)` не меняется.
- Повторная валидация делает невозможным «тихо проглотить» change order, ломающий §3.4 / §6.1.2.1 / BOM / хронологию.
- Демонстрация intersection types из README / `docs/02` теряет ложное обоснование и честно переносится в M4 (`type SubscribedQuery = Query & WithSubscription`).

**Отрицательные / принимаемые издержки**

- **Ломающее изменение:** `type ChangeOrder` больше не является `XJDF`. Любой код, который присваивал тикет переменной типа `ChangeOrder`, перестаёт компилироваться.
- Набор полей — подмножество Table 3.1. Полная форма документа (включая `@Types` и Complete/Remove) появится в M4; до тех пор декодер change-order XML/JSON не сможет представить произвольный partial-XJDF без расширения типа.
- `ChangeOrder.scala` добавляет ребро `model → TicketValidator`. Это допустимо (валидатор — корень агрегации, ADR-0002); обратного ребра нет.

## Normative references

- §1.3.2 «Enable dynamic changes» — `reference/xjdf/1 – Introduction.md`
- §1.4.1.3 «Schema and XJDF Context» (две схемы; change order SHOULD нести только изменённые значения)
- §1.6.5 «Specification of Cardinality»; Table 1.2 Cardinality Symbols
- Table 3.1 «XJDF» (Sheet 1–2) — `reference/xjdf/3 – Structure.md`: `@JobID`, `@JobPartID?`, `@Types`, `AuditPool?`, `Comment*`, `GeneralID*`, `ProductList?`, `ResourceSet*`
- §3.2 / Table 3.3 — хронологический порядок `AuditPool`
- §3.4 / Table 3.12 — уникальность `ResourceSet` (предикат `clashesWith`)
- Table 7.55–7.56 `CommandResubmitQueueEntry` / `ResubmissionParams` — `reference/xjdf/7 – Messaging.md`
- §9.8 / Table 9.2 «Modifying Job Parameters»; §9.8.2.1.1–9.8.2.1.2; примеры 9.5–9.10 — `reference/xjdf/9 – Building a System.md`
- `reference/xjdf/schema.xsd` (`element name="XJDF"`): `@JobID`/`@Types` required — oracle **жёсткой** схемы; change-order schema в репозитории отсутствует (онлайн, §1.4.1.3)

## Migration impact

**Ломающее изменение.** `ChangeOrder` перестаёт быть alias на `XJDF`.

Полный список call sites (статический grep по `modules/` на момент принятия ADR, вне самого определения):

| Файл | Было | Стало |
| --- | --- | --- |
| `model/Ticket.scala` | `trait Partial`; `XJDF extends Partial`; `type ChangeOrder = XJDF & Partial` | удаляются; `XJDF` больше ни от чего не наследует |
| `laws/TicketLaws.scala` | тест «a change order is an XJDF refined by Partial» присваивает `XJDF` к `ChangeOrder` | удаляется; позитивные/негативные тесты нового типа — в `laws/ChangeOrderLaws.scala` |
| `examples/SpecExamples.scala` | `val changeOrder: Patch = Patch.updateResourceSets { … }` | `val changeOrder: ChangeOrder = ChangeOrder(jobId, resourceSets = …)` + `applyChange` |
| `examples/Main.scala` | демо печатает результат `withPatch` | демо печатает результат `applyChange` (тот же вывод) |
| `README.md`, `docs/02-scala3-features.md`, `docs/04-architecture.md` | intersection `XJDF & Partial` как «фича Scala 3» | честный отказ; intersection переносится в M4 |

Переходный alias `type ChangeOrder = XJDF` **не** вводится: он вернул бы N-20. Любое использование ломается на компиляции.

`Patch` и `XJDF.withPatch` не удаляются: это по-прежнему законный моноид эндоморфизмов. Change order — один из способов *получить* `Patch`, не замена `Patch`.

**Срок пересмотра:** M4 (`CommandResubmitQueueEntry`, полная форма документа, Complete/Remove, partition-level Incremental). Если к заморозке API M2 декодеру понадобится поле, отсутствующее в таблице выше, набор расширяется отдельным решением в этом ADR, а не молча.

## Реализация

Реализуется в PR-10 (M1.4-2). Финальный набор полей — таблица раздела Decision; отклонения от неё в коде требуют обновления настоящего ADR.

Статус исполнения ведётся в `ROADMAP.md` §8 (M1.4-2) и §14.1 (N-20).
