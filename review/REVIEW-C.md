# REVIEW.md — Аудит и ревью результата выполнения задания

> **Объект ревью:** коммит `996b756` («Reformat code using ScalaFmt») ветки
> `arena/01a0044b-xjdf4s` — исходный результат задания «XJDF модель на
> Scala 3.8.4 / sbt 2.0.2 / cats, категориальный взгляд, ROADMAP».
> **Ревью не вносит изменений в код** — только отчёт. Рекомендации по
> исправлению находок — в `PROPOSAL-C.md` (там же ссылка на готовый набор
> патчей, сохранённый в истории ветки).

---

## 1. Методология

В окружении нет java/scala/sbt, поэтому ревью — полностью статическое, но не
«на глаз»:

- прочитан весь код проекта: `modules/core` (36 файлов, ~5 700 строк),
  `modules/laws` (5 файлов), `modules/examples` (2 файла), `build.sbt`,
  `project/build.properties`, `.scalafmt.conf`, `.gitignore`, `README.md`,
  `ROADMAP.md`, `docs/01–04`;
- сверка со спецификацией: `./reference/xjdf/*` (главы 1–9, Appendix A–H,
  `schema.xsd` выборочно);
- сверка с `./reference/cats/docs/*` (все используемые typeclasses/datatypes),
  `./reference/scala/docs/reference/*` и `./reference/scala/spec/*`,
  `./reference/sbt/docs/*`;
- спорная семантика компилятора (приоритет поля named tuple перед
  extension-методом, creator applications, контрольный синтаксис) проверена
  по исходникам компилятора dotty 3.7.3
  (`Typer.typedSelectWithAdapt`: цепочка фолбэков
  `tryType → tryInstantiateTypeVar → tryLiftToThis → tryNamedTupleSelection →
  trySmallGenericTuple → tryExt`);
- правила SHALL/SHOULD проверялись по тексту таблиц, а не по памяти; все
  ссылки на таблицы в scaladoc сверены с фактическими номерами таблиц в
  `./reference/xjdf/*`.

`build.log` в корне репозитория проигнорирован: автор явно пометил его
устаревшим.

---

## 2. Вердикт

**Архитектурно и категориально — работа высокого уровня; сданный HEAD не
собирается (1 блокер компиляции), содержит 2 функциональных бага (один из
которых делает красным собственный тест) и ряд отступлений от спецификации.**
После применения предложений из `PROPOSAL-C.md` (P0–P2) проект, по статической
оценке, должен собираться и проходить тесты.

Сильные стороны (подробно в §6, §7):

- модель не анемична: алгебры (`Semigroup`/`Monoid`/`Semilattice`), валидация
  через `ValidatedNec`, `State`, `Ior`, `FunctionK` — с законами в
  `modules/laws`;
- категориальный слой — настоящие конструкции (`Fix[ProductTree]` + cata,
  естественное преобразование, моноид эндоморфизмов), а не метафоры;
- дисциплина трассируемости: почти каждый тип ссылается на таблицу
  спецификации, конвенция «кардинальность → вид типа» выдержана;
- чистые слои пакетов без циклов, аккуратные opaque-обёртки с валидацией.

Слабые стороны: см. §5 (находки R-01…R-16).

---

## 3. Соответствие заданию (чеклист)

| Требование задания | Статус | Где |
|---|---|---|
| opaque type | ✅ | все типы Appendix A + `AmountPool`, `AuditPool`, `Patch`, `WorkstepKey`, `ProcessIndex`, `ResourceSetName`, `IntentName`… |
| named tuple | ✅ | `XYPair`, `Shape`, `Rectangle`, `Matrix`, `IntegerRange`, `AmountRange`, `TimeRange`, `WorkstepKey` — opaque поверх named tuple |
| enum | ✅ | 45 закрытых перечислений Appendix A (см. R-04/R-05 о пропусках) + GADT-суммы (`Audit`, `IntentPayload`, `ResourcePayload`, `PartitionValue`, `Pulse`, `ProductTree`, `SignalPayload`, `FileLocation`, `PartitionKey`) |
| union types | ✅ | `BindingDetails`, `NotificationDetail`, `OrientationSpec` |
| intersection types | ⚠️ | `ChangeOrder = XJDF & Partial` — синтаксически есть; семантически маркер, различия типов не создаёт (R-15) |
| match types | ✅ | `ValueOf[PartitionKey]` (Table 6.4); ограничение GADT-редукции честно задокументировано в ROADMAP |
| trait / case class | ✅ | `Named[N]`, `XjdfEnum`/`XjdfEnumCompanion`; case class = таблицы спецификации |
| context functions | ✅ | `IdAllocator ?=> A` |
| «не анемичная, используй cats» | ✅ | инстансы с законами; `ValidatedNec`, `NonEmptyChain`, `Ior`, `State`, `FunctionK`, `Show/Eq/Order` |
| ProductList, ResourceSet, AuditPool декларативно | ✅ | `dsl.*` + `TicketDraft`; примеры глав 3/5 |
| ROADMAP.md подробный | ✅ | M0–M6, риски, DoD, конвенции; ссылки на `./reference/*` |
| Имя xjdf4s, Scala 3.8.4, sbt 2.0.2 | ✅ | `build.sbt` (sbt 2.x, Scala 3-синтаксис), `project/build.properties`; cats 2.13.0, munit 1.3.0 |
| Решения сверяются с `./reference/*`, без предположений | ⚠️ | в основном да; отступления — R-04…R-12 |
| Категориальный взгляд | ✅ с оговорками | `docs/01` + рабочие конструкции; две неточности в тексте (R-15, R-16) |

---

## 4. Сборка и структура

**Положительное.** `build.sbt` — корректный sbt 2.x build definition на
Scala 3-синтаксисе (`reference/sbt/docs`); версии зафиксированы; агрегация
`core`/`laws`/`examples`; флаги `-deprecation -feature -unchecked
-Wunused:all -Wvalue-discard -Wnonunit-statement`. `.gitignore` покрывает
sbt-таргеты. `.scalafmt.conf` согласован (rewrite-правила, диалект scala3).

**Замечания к процессу (не баги):**

- **Нет CI.** В репозитории отсутствует `.github/workflows` — при заявленной
  модели «я делаю commit, вы даёте обратную связь по сборке» автоматический
  прогон на PR закрыл бы блокер R-01 до ревью. Предложение: P4-1.
- **`build.log` закоммичен в git** (`git ls-files` показывает его), при этом
  `*.log` уже в `.gitignore` (игнор не действует на отслеживаемые файлы).
  Файл устарел и вводит в заблуждение. Предложение: P4-2.
- **Нет LICENSE** — блокер для заявленной публикации в M6. Предложение: P4-3.
- ROADMAP утверждает, что feedback-итерация сборки закрыта; фактические
  данные (R-01) этому противоречат — либо итерация шла по более раннему коду,
  либо проверка не была полной. Само по себе не ошибка кода, но признак
  отсутствия CI-гейта.

Организация пакетов — образцовая: `prim ← {model, intents, resources}`,
`model ← {intents, resources}` (только для закрытых перечислений полезных
нагрузок), циклов нет.

---

## 5. Находки

### 5.1 Блокеры компиляции

**R-01. `Monoid[ValidatedNec[Issue, Unit]]` не существует — два вызова
`combineAll` не скомпилируются. Критично.**

Места: `TicketValidator.validate` (`checks.combineAll`) и
`Bom.validateAmounts` (`kids.combineAll`), где элемент —
`ValidatedNec[Issue, Unit] = Validated[NonEmptyChain[Issue], Unit]`.

`Foldable#combineAll` требует `Monoid[A]`; cats выводит
`Monoid[Validated[E, A]]` только при наличии `Monoid[E]`, а у
`NonEmptyChain` моноида нет:

> «It does not have a Monoid instance since it cannot be empty, but it does
> have a Semigroup instance» — `./reference/cats/docs/datatypes/chain.md`.

Следствие: «no given instance of type cats.kernel.Monoid[Validated[...]]» на
обоих вызовах. Способы устранения — P0-1.

### 5.2 Функциональные баги

**R-02. `Bom.toTree`: ложное детектирование циклов — BOM с любыми ссылками не
разворачивается. Серьёзно.**

В `model/Product.scala` при спуске в `seen` добавляется ID **ребёнка**, а не
текущего узла:

```scala
kid <- child.flatMap(c => toTree(c, byId, seen + c.id.fold("")(_.value)))
```

Любой продукт с `@ID`, на который есть `@ChildRefs`, немедленно объявляется
циклом. `Bom.fromProductList` фактически работает только для списков без
ссылок — ровно для случая, где развёртка не нужна. Демо
`Main.demoBomFold` на Example 3.4 (notebook) печатает
«unfold failed: Cycle in product structure at ID 'IBack'». Тестов на
`fromProductList` в laws нет, поэтому регрессия не ловится.

**R-03. `IntegerRange` не реализует нисходящие диапазоны §1.10.2 — собственный
закон красный. Серьёзно.**

Спецификация (§1.10.2): «the range `"0-1"` represents all entries of a list
and the range `"-1 0"` represents the same list **in reverse order**».
`indices` сворачивает нормализованные концы в `lo = min`, `hi = max` и всегда
обходит по возрастанию; ветка `(lo to hi by -1)` недостижима. Следствие:
закон «IntegerRange -1 0 selects everything in reverse» в `AlgebraLaws`
**падает**; любой нисходящий диапазон (`5 2`, `3 1`) выдаёт восходящий.

### 5.3 Отступления от спецификации

| # | Находка | Факт (источник) | В коде |
|---|---|---|---|
| R-04 | `DeviceStatus` неполон | Table A.15: `Cleanup`, `Setup` («New in XJDF 2.1»), `Idle`, `NonProductive`, `Offline`, `Production`, `Stopped` — 7 значений | 5 из 7 (`prim/Enums.scala`) |
| R-05 | `Sides` неполон | Table A.40: `Unprinted` («New in XJDF 2.1») | 4 из 5 |
| R-06 | 7 ссылок на таблицы в scaladoc ресурсов — это номера **секций**, а не **таблиц** | глава 6 | `Color` «6.14» → факт 6.27; `CuttingParams` «6.25» → 6.53; `FoldingParams` «6.36» → 6.74; `Layout` «6.52» → 6.95; `Media` «6.57» → 6.114 (6.57 — таблица Device!); `NodeInfo` «6.59» → 6.119; `Preview` «6.66» → 6.134. Верные: Component 6.37, Contact 6.38, ComChannel 6.39, Company 6.40, Person 6.42, DeliveryParams 6.54, DropItem 6.55, Device 6.57, RunList 6.148 |
| R-07 | `Show[Part]` печатает ключ `OptionKey` | Table 6.4: имя атрибута — `Option` | искажён wire-формат `OptionKey=…` |
| R-08 | Проверка уникальности ResourceSet (§3.4) слабее правила | §3.4: «same values of @Name, @Usage, @ProcessUsage **and common or no entries** in @CombinedProcessIndex SHALL NOT be specified» | `groupBy(_.key)` с точным равенством: `[0]` против `[0,1]` не считается дубликатом |
| R-09 | Проверка §6.1.2.1 учитывает родительские Part только при ровно одном Part | Table 6.3: «SHALL NOT include any Partition Keys that are already uniquely specified in **any** parent Resource/Part element» | `case 1 => …; case _ => Nil` |
| R-10 | Сниппет в README не компилируется | `Validated` — не монада (`./reference/cats/docs/datatypes/validated.md`); противоречит собственному `docs/03-cats-mapping.md` | `dsl.TicketDraft.of(...).flatMap(_.build)` |
| R-11 | `DropItem` неполон | Table 6.55: `TotalDimensions?`, `TotalVolume?`, `TotalWeight?` | нет полей |
| R-12 | `Part/@Metadata` — тип `NmToken` | Table 6.4: тип `regExp` | упрощение без пометки |
| R-13 | Второе предложение правила Table 6.3 не проверяется | «If any of these Part elements specify the same Partition Key as the parent Resource/Part element, then the value of that key SHALL match one of the values from the parent» | проверки нет |
| R-14 | `@Types` с дублирующимся токеном `"Product"` не отклоняется | §3.1.3: «SHALL NOT contain the token "Product" if any additional process type tokens are present» | `checkTypes` ловит `Product`+процесс, но не `Product Product` (серое место; интерпретация спорна) |

Примечание к R-08: `Patch.mergeResourceSets` использует точное равенство ключа
осознанно (семантика «заменить набор целиком») — это корректно, претензия
только к валидатору.

### 5.4 Неточности документации (не код, но часть сдаваемого результата)

- **R-15.** `docs/02-scala3-features.md` утверждает, что
  `ChangeOrder = XJDF & Partial` «различает контексты на уровне типов». Это
  неверно: `XJDF` уже `extends Partial`, поэтому `XJDF & Partial` **равен**
  `XJDF` (взаимная субтипизация). Маркер несёт только документационную
  нагрузку. Сам тип при этом — законное применение синтаксиса
  intersection types, просто его семантика описана завышенно.
- **R-16.** `docs/01-category-theory-view.md` пишет, что `Part.matches`
  «рефлексивно и транзитивно». Рефлексивность верна (и проверяется законом),
  **транзитивность — нет**: `a={k=1}.matches(b={})` и `b.matches(c={k=2})`
  истинны, а `a.matches(c)` ложно. Это отношение совместимости, а не
  предпорядок; «тонкая категория» как строгая категориальная интерпретация
  неверна.

### 5.5 Замечания по API/дизайну (не баги)

- **R-17.** `dsl.TicketDraft.withJobPart`/`withProject` молча отбрасывают
  невалидные значения (`JobPartId.from(...)` → `None`), тогда как
  `TicketDraft.of` валидирует `JobID` в `ValidatedNec`. Несимметричный UX.
- **R-18.** `PartBuilder.set` бросает `IllegalArgumentException` при
  несовпадении вида значения ключа (Token vs Range и т.п.) — «unsafe»-путь не
  вынесен в имя/сигнатуру; по духу принципа 5 `docs/04` должен быть `Either`
  или явно `unsafe`.
- **R-19.** `IdAllocator.stateful` мутирует `var` — компромисс задокументирован
  (чистая версия — `IdSource.fresh: State`), но стоит явно пометить как
  не-потокобезопасный контекст.
- **R-20.** `XjdfVersion.from` принимает только `"2.2"` — корректно для
  библиотеки XJDF 2.2 (Table 3.1: «SHALL be "2.2"»), но scaladoc не упоминает
  значения 2.0/2.1 из Table A.52.
- **R-21.** Целостность BOM (ацикличность, висячие `@ChildRefs`) не включена в
  `TicketValidator.validate`: невалидный по ссылкам `ProductList` тикет
  проходит `validate.isValid`, и только явный вызов `Bom.fromProductList`
  вскрывает проблему.

---

## 6. Категориальный слой — оценка утверждений docs/01

| Утверждение документа | Оценка |
|---|---|
| «Один XJDF = один морфизм»; объекты — стороны, стрелки — тикеты (§1.3) | ✅ корректно; спецификация сама определяет тикет как транзакцию между двумя сторонами |
| `@Types` — слово свободного моноида/свободной категории процессов; `ProcessPath` | ✅ `NonEmptyChain[ProcessType]`, `@CombinedProcessIndex` — индекс в слове, границы проверяются (формально: `NonEmptyChain` — свободная **полугруппа**, в документе это оговорено) |
| `ProductList` — начальная алгебра; `Fix[ProductTree]` + `cata`; развёртка — монадическая (обнаружение циклов/висячих ссылок) | ✅ конструкция настоящая; **но работает с багом R-02** |
| `Part.matches` — тонкая категория/preorder, рефлексивно и транзитивно | ⚠️ рефлексивно — да (закон есть); транзитивность — нет (R-16) |
| Свободные моноиды на всех уровнях (таблица в §4 документа) | ✅ |
| `Pulse ~> NonEmptyChain` — естественное преобразование; закон естественности | ✅ `FunctionK`, закон в `AlignmentLaws` |
| `Patch` — моноид эндоморфизмов, правое действие на тикетах | ✅ `Monoid[Patch]`, закон действия проверяется поведенчески |
| `AmountRange` — полурешётка meet + двойственная join | ✅ законы meet в `AlgebraLaws`; «полурешётка» сформулировано аккуратно (полной решётки нет — закона поглощения нет) |
| `Matrix` — моноид аффинных преобразований (группа при det ≠ 0) | ✅ законы + inverse; 8 матриц `Orientation.matrix` сверены с Table 2.1/§A.2.32 — совпадают |
| Intents ⇄ Resources — сопряжение | ✅ подано как структурное зеркалирование с проверяемым законом «`@Name` = elementName» — честная формализация, без спекуляций |
| XJDF = произведение/копроизведение трёх взглядов | ✅ метафора подана аккуратно |
| ID/IDREF — закон единиц/композиции категории | ✅ метафора; проверки уникальности ID и разрешимости IDREF реализованы |

Итог: категориальная часть — лучшая часть работы; из фактических ошибок —
только R-16 (и работоспособность BOM из-за R-02).

---

## 7. Оценка по модулям

| Файл/модуль | Оценка | Комментарий |
|---|---|---|
| `prim/Tokens`, `Ids`, `Versions` | ✅ | аккуратные валидируемые opaque; `XjdfString` (≤1023, без управляющих символов) и `LanguageTag` сверены с Table A.1 |
| `prim/Time` | ✅ | `OffsetDateTime`/`Duration`; `Monoid[TimeSpan]`; named-given `showTimestamp` — грамотный обход прозрачности opaque |
| `prim/Enums` | ✅ с оговорками | `XjdfEnum`/`XjdfEnumCompanion` — хороший миксин; «None»-токены (`NoBinding`, `Unbound`, `Uncoated`, `Unscored`, `Unjacketed`) сверены с таблицами; пропуски R-04/R-05 |
| `prim/Quantity` | ✅ с оговоркой | named tuples за opaque; `Matrix.*` сверен с §2.6.5; формула `Severity` = §5.3.4.1; баг R-03 |
| `prim/Common` | ✅ | `FileSpec.location` — замкнутый копродукт по правилу Table 8.22; каталоги Appendix A.3 |
| `model/Partition` | ✅ | 27 ключей = Table 6.4 (с вынужденным переименованием `Option` → `OptionKey`); `ValueOf` match type; `matches`/`mergeWith`/`PartBuilder`; R-07 |
| `model/Amounts` | ✅ | `PartWaste.isLawful` = Table 6.5 («at least one of») |
| `model/Header`, `Audit` | ✅ | 5 видов аудитов = Table 3.3; `isChronological`; `Pulse`/`Alignment` |
| `model/Product` | ⚠️ | `Bom` — катаморфизм и `totalCopies` по §3.3.1.1 корректны; развёртка сломана (R-02) |
| `model/Resource`, `Ticket`, `Patch`, `Validation`, `IdSource` | ✅ с оговорками | §3.4-ключ, `WorkstepKey` (§2.2.2), `Patch`-моноид, 12 проверок; R-01, R-08, R-09, R-21 |
| `intents/*` | ✅ | 8 интентов главы 4; паринг `BindingType ↔ BindingDetails` в `isLawful`; имена элементов сверены (включая спецификационные `AdhesiveNote`, `SaddleStitching`) |
| `resources/*` | ✅ с оговоркой | 12 ресурсов главы 6; `references` для IDREF-целостности; R-06 |
| `dsl/XjdfDsl` | ✅ | декларативные конструкторы → `ValidatedNec`; `TicketDraft.build` прогоняет полный валидатор; R-17 |
| `laws/*` | ✅ с оговоркой | 4 сьюта, ScalaCheck-свойства, поведенческие законы Patch; один закон красный (R-03), нет тестов на `Bom.fromProductList` (пропустил бы R-02) |
| `examples/*` | ✅ | примеры 3.1/3.3/3.4/3.6/5.2 + brochure + change order; `Main` демонстрирует cata/Alignment/Matrix; demo BOM красное из-за R-02 |
| `build.sbt`, `docs/*`, `ROADMAP.md` | ✅ с оговорками | sbt 2.0.2 (Scala 3-синтаксис); документация насыщенная; R-15, R-16, R-10 |

---

## 8. Что подтверждено как корректное (для сбалансированности)

- Матрицы `Orientation.matrix` — все 8 значений против Table 2.1/§A.2.32 ✅.
- Состав и токены 43 из 45 enum против Appendix A.2 ✅ (включая специальные
  «None»-токены); пропуски — только R-04/R-05.
- Имена элементов интентов/ресурсов/подэлементов против таблиц глав 4/6/8 ✅.
- Правила: §3.1.3 (`Product` не смешивается с процессами), Table 3.1
  `@RelatedJobPartID`, Table 6.1 `@Status` запрещён для Output, «at most one
  of @Orientation/@Transformation» — структурно через union, Table 6.5 «at
  least one of», §2.2.3 ID/IDREF, хронология AuditPool, §2.2.2 `WorkstepKey`
  ✅.
- 27 Partition Keys = Table 6.4; семантика выбора §6.1.3.2 (итерация сверху
  вниз, первый совпавший) и §6.1.3.3 (несколько Part = дизъюнкция) ✅.
- Счёт §1.10.2 для восходящих диапазонов и отрицательных индексов ✅
  (нисходящие — R-03).
- Номера таблиц: ProductList 3.10 / Product 3.11 / XJDF 3.1 / Header 7.3 /
  ProcessRun 3.7 / Dependent 3.13 / DeviceInfo 7.67 / ResourceInfo 7.53 /
  подэлементы главы 8 — верны ✅.
- Инстансы `Monoid`/`Semigroup`/`Semilattice` — законы в tests ✅ (Patch —
  поведенчески, как и положено для функций).
- Синтаксические конструкции, вызывавшие сомнения, валидны: `Arbitrary:` —
  creator application (компаньон Scala 2-класса предоставляет `apply`),
  `import … .given`, tuple-лямбды, `property(...):` — new control syntax;
  «поле named tuple против extension-метода с тем же именем» (`def x = p.x` и
  т.п.) — поле выигрывает (проверено по `Typer.typedSelectWithAdapt`,
  dotty 3.7.3), рекурсии нет.
- Слои пакетов без циклов; `prim` не импортирует domain-пакеты ✅.

---

## 9. Что остаётся проверить только компилятором

1. `sbt compile` — после R-01 единственный известный блокер; остальное по
   статике согласовано. Возможны предупреждения `-Wunused:all -Wvalue-discard
   -Wnonunit-statement` (сборка заявлена «без предупреждений»).
2. `sbt test` — ожидается красный `AlgebraLaws` (закон `-1 0`, R-03);
   остальные сьюты по статике должны проходить.
3. Доступность версий артефактов (cats 2.13.0, munit 1.3.0,
   munit-scalacheck 1.3.0, Scala 3.8.4, sbt 2.0.2) — первым резолвом.
4. Идемпотентность `scalafmt` (включены rewrite-правила;
   `newlines.source=keep`).
5. Scala 3.8.4 против 3.7.x: named tuples и creator applications стабильны
   (справочник `./reference/scala/.../named-tuples.md`), риск низкий.

---

## 10. Приложение: структура репозитория на момент ревью

```
xjdf4s/
├── build.sbt                  # sbt 2.x, Scala 3-синтаксис, 3 модуля
├── project/build.properties   # sbt.version=2.0.2
├── .scalafmt.conf / .gitignore
├── build.log                  # устаревший, закоммичен (P4-2)
├── README.md / ROADMAP.md
├── docs/01…04 (category-theory, scala3-features, cats-mapping, architecture)
├── modules/
│   ├── core/src/main/scala/xjdf4s/{prim,model,intents,resources,dsl}
│   ├── laws/src/test/scala/xjdf4s/laws/   # 4 сьюта + Arbitraries
│   └── examples/src/main/scala/xjdf4s/examples/
└── reference/{xjdf,category-theory,scala,cats,sbt}
```

Основные источники истины, использованные при проверке: `./reference/xjdf/`
(§1.3, §1.4, §1.6.5, §1.10.2, §2.2, §2.4, §2.6, §3.1–3.5, §4.3–4.14, §5.2,
§5.3.4.1, §6.1, Table 6.1–6.6, Table 6.14–6.148, §7, §8, Appendix A),
`./reference/cats/docs/{validated,chain,ior,state,functionk,algebra}.md`,
`./reference/scala/docs/reference/{opaques,named-tuples,enums,union-types,
intersection-types,match-types,creator-applications,context-functions,
control-syntax}.md`, `./reference/scala/spec/*`,
`./reference/sbt/docs/00.md`.