# XJDF глазами теории категорий

Документ связывает модель xjdf4s с категориальной терминологией справочника
`./reference/category-theory/*` (Б. Милевски, «Category Theory for Programmers»,
части 1–3) и с текстом спецификации `./reference/xjdf/*`.

## 1. Один XJDF = один морфизм

Спецификация прямо определяет XJDF как **транзакцию между двумя сторонами**
(§1.3): «each XJDF ticket specifies a single transaction between two parties».
Категориально это означает, что у нас есть категория

- **объекты** — интерфейсы участников (Device, Controller, MIS) как «типы портов»;
- **морфизмы** — сами XJDF-тикеты: Controller → Device.

Ссылка: `Part 1 – its-all-about-morphisms`-идея («сначала морфизмы, потом объекты»)
и `Part 1 – category-the-essence-of-composition`: суть XJDF — композиция, а не
структура данных. Поэтому в xjdf4s корневой `XJDF` — это **значение-морфизм**,
а не «большой объект-тикет» (ср. критику JDF 1.x «monolithic job ticket», §1.3).

### 1.1 `XJDF/@Types` — слово в свободной категории

`@Types` — упорядоченный список процессов (`Interpreting Rendering
DigitalPrinting`). По `Part 1 – categories-great-and-small` это **морфизм
свободной категории, порождённой графом процессов**: вершины — «типы
интерфейсов ресурсов», рёбра — типы процессов, а путь = композиция рёбер.

В коде: `ProcessPath(steps: NonEmptyChain[ProcessType])` — слово свободного
моноида (см. §4 ниже), `ResourceSet/@CombinedProcessIndex` — индекс позиции в
этом слове (§3.4, §5.2), с проверкой границ в `TicketValidator`.

## 2. ProductList — начальная алгебра (F-алгебра) спецификации

Bill of Materials — это рекурсивная структура: продукт состоит из продуктов
(обложка + блок + задник), каждый со своим `@Amount` (§3.3.1.1). По
`Part 3 – f-algebras` мы выделяем **образующий функтор** `ProductTree[A]`:

```scala
enum ProductTree[+A]:
  case Leaf(product: Product)
  case Node(product: Product, children: Chain[A])
```

- `Fix[ProductTree]` — наименьшая неподвижная точка, начальная алгебра BOM;
- `Bom.cata(alg)` — **катаморфизм**: единственный гомоморфизм из начальной
  алгебры. В коде это `validateAmounts` — свёртка с носителем
  `ValidatedNec[Issue, Unit]` (аппликативный функтор накопления ошибок);
- `Bom.fromProductList` — «развёртка» списка продуктов в лес деревьев; граф
  ссылок (`ChildRefs`) может быть циклическим, поэтому развёртка —
  **монадическая** (Either с обнаружением циклов и висячих ссылок);
- `Bom.totalCopies` — нисходящая свёртка мультипликаторов `@Amount` (§3.3.1.1).

Ссылка: `Part 3 – f-algebras` (μ: F(m) → m, катаморфизм), `Part 2 – free-monoids`.

## 3. ResourceSet и Partition — расслоение и предпучки

Разбиение ресурсов (partitioning, §1.5.2) — ключевая структура XJDF.
Категориально:

- `Part` — набор Partition Keys (Table 6.4). Каждый `Resource` «живёт над»
  своим контекстом разбиения; выбор раздела (§6.1.3.2 «Selecting a Partition»)
  — это **подъём вдоль отображения ключей**: итерация сверху вниз и взятие
  первого `Resource`, у которого нет несовпадающих атрибутов.
- Семантика выбора — гом-множество в **тонкой категории** (preorder, `Part 1 –
  categories-great-and-small`): `part.matches(selector)` — отношение порядка
  «контекст part совместим с селектором selector» (рефлексивно и транзитивно —
  свойства проверяются в laws-модуле).
- `Part` образует **полугруппу** наложения ключей (right-biased overlay) и
  поддерживает обнаружение конфликтов: `mergeWith: Either[keys, Part]`.
- Условие уникальности §3.4 («ResourceSet с одинаковыми Name/Usage/ProcessUsage/
  CombinedProcessIndex SHALL NOT повторяться») — **свойство мономорфности**
  отображения ключ → ResourceSet внутри тикета: `key` обязано быть инъективным
  (проверяется в `TicketValidator.checkResourceSetKeys`).

Ссылки: `Part 1 – products-and-coproducts` (универсальные свойства как способ
определять объекты через отношения), `Part 2 – limits-and-colimits`.

## 4. Свободные моноиды — главный строительный блок модели

По `Part 2 – free-monoids` свободный моноид над множеством генераторов — это
списки. В XJDF ровно это и происходит на каждом уровне:

| Структура XJDF | Свободная конструкция | В коде |
|---|---|---|
| `@Types` NMTOKENS | слово свободного моноида над процессами | `ProcessPath` = `NonEmptyChain[ProcessType]` |
| `ResourceSet/Resource*` | список разделов | `Chain[Resource]` |
| `Part*` (несколько Part у одного Resource, §6.1.3.3) | дизъюнкция контекстов | `Chain[Part]` |
| `AmountPool/PartAmount+` | упорядоченные частичные суммы | `AmountPool` = `NonEmptyChain[PartAmount]`, `Semigroup` — конкатенация |
| `AuditPool` | хронологическая история | `AuditPool` = `NonEmptyChain[Audit]`, `Semigroup` — конкатенация |

Именно поэтому cats-овские `Chain`/`NonEmptyChain` — правильные носители:
у них честные законы полугруппы и Foldable/Traverse, а не ad-hoc коллекции.

## 5. AuditPool, сигналы и Writer

По §3.2 «аудиты — это агрегированные сигналы»; Table 3.2 «Alignment of Audits
and Messages» задаёт соответствие `SignalX → AuditX`. Категориально:

- `Pulse[A]` — функтор **мгновенного наблюдения** (одно значение);
- `NonEmptyChain[A]` — функтор **накопленной истории** (свободный моноид);
- `Alignment.snapshot: Pulse ~> NonEmptyChain` — **естественное преобразование**
  `Beat(a) → one(a)`; закон естественности (`map f ∘ snapshot = snapshot ∘ map f`)
  проверяется свойством в laws-модуле;
- `Alignment.signalToAudit` — перевод Table 3.2 в код;
- поток сигналов сворачивается в `AuditPool` — в точности **Writer**-паттерн
  (см. `./reference/cats/docs/datatypes/writer.md`): исполнение пишет историю в
  аккумулятор-моноид.

Ссылки: `Part 1 – natural-transformations`, `Part 2 – free-monoids`,
`Part 3 – monads-programmers-definition` (Writer как прототип моноид-эффекта).

## 6. Change order — моноид эндоморфизмов и действие на тикетах

§1.3.2: «The simplest method of initiating a change transaction is to send an
XJDF that contains only the modified values». Изменение — это **функция**
`XJDF => XJDF`:

- `Patch` — моноид эндоморфизмов (`Endo[XJDF]`): `empty` — пустое изменение,
  `combine` — композиция. Законы моноида проверяются **поведенчески** (через
  действие на тикеты), т.к. функции не сравниваются по значению;
- `applyTo` — правое **действие моноида** на множестве тикетов:
  `applyTo(applyTo(t, p), q) == applyTo(t, p |+| q)`;
- `Patch.mergeResourceSets` возвращает `Ior[Issues, XJDF]` — трёхзначный итог
  слияния (чисто / слито-с-предупреждениями / невозможно), `./reference/cats/
  docs/datatypes/ior.md`.

Ссылка: `Part 3 – monads-monoids-and-categories` (моноид как категория с одним
объектом; действие — функтор из моноида в Set).

## 7. Intents и Process Resource Pairing — сопряжение (adjunction)

У каждого Product Intent в спецификации есть «Process Resource Pairing»
(например, `MediaIntent ↔ Media`, `BindingIntent ↔ 13 ресурсов главы 6).
Это не совпадение: **Intent** — описание желаемого результата с точки зрения
покупателя, **Resource** — описание производственного процесса. Связка

```
  Категория интентов (что заказано) ⇄ Категория процессов (как сделать)
```

— классическая **сопряжённая пара** (см. `Part 3 – adjunctions`): MIS играет
роль левого сопряжённого, «генерируя» ресурсное описание из интента; Device —
правого, «забывающего» производственные детали до аудита результата.
В коде сопряжение выражено структурно: закрытый `IntentPayload`/`ResourcePayload`
с явным `elementName`, зеркальные типы полей (MediaIntent/Media, LayoutIntent/
Layout) и закон «`@Name` контейнера = имя элемента полезной нагрузки»
(`Intent.isLawful`, `ResourceSet.hasLawfulChildren`).

Ссылки: `Part 3 – adjunctions`, `Part 3 – free-forgetful-adjunctions`.

## 8. AmountRange — решётка ограничений

`@Amount` + `@MinAmount` + `@MaxAmount` (Table 6.3) — интервал обязательств.
`meet` (ужесточение: выше нижняя граница, ниже верхняя, меньше обещанное
количество) ассоциативен, коммутативен и идемпотентен — **полурешётка**
(`cats.kernel.Semilattice`); двойственная операция `join` — оптимистичное
расширение. Законы полурешётки проверяются свойствами.

Ссылка: `Part 1 – categories-great-and-small` (тонкие категории и порядок),
`./reference/cats/docs/algebra.md` (lattice-иерархия).

## 9. Матрицы и координатные системы — моноид аффинных преобразований

`Resource/@Transformation` — матрица `a b c d Tx Ty` (Table A.1, §2.6.5).
Композиция матриц ассоциативна, единица — `1 0 0 1 0 0`: законный моноид
(группа при det ≠ 0 — есть `inverse`). Таблица 2.1 «Matrices and Orientation
values» закодирована как `Orientation.matrix(w, h)`.

## 10. XJDF = произведение/копроизведение трёх представлений

Корневой тикет — сумма (копроизведение) трёх ортогональных «взглядов» на
работу (§2.4.1):

- `ProductList` — что заказано (BOM);
- `ResourceSet*` — как исполняется (процесс);
- `AuditPool` — что произошло (история).

В коде это явные поля-компоненты одного значения; при этом каждый компонент —
свободная конструкция (§4), а валидация — единый гомоморфизм в
`ValidatedNec[Issue, Unit]` (аппликативная композиция проверок, накопление всех
ошибок сразу). Ссылка: `Part 1 – products-and-coproducts`.

## 11. ID/IDREF — внутренние ссылки как тождества документа

`@ID`/`@IDREF` (§2.2.3) — локальная система ссылок внутри одного документа.
Категориально: множество `ID` — «носители тождеств» (identity carriers), а
закон «каждый IDREF указывает на существующий ID» — закон композиционности
морфизмов: нельзя сослаться на несуществующую стрелку. `XJDF.validate`
проверяет уникальность ID и разрешимость всех IDREF — в точности закон
единиц/композиции категории.
