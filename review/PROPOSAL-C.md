# PROPOSAL-C.md — Предложения по улучшению проекта xjdf4s

> Документ парный к `REVIEW-C.md`: каждая находка R-XX из отчёта превращена
> здесь в конкретное предложение с приоритетом. Ничего из этого **не внесено
> в код** — это план, который можно принимать/отклонять пунктами.
>
> **Готовый набор реализаций для P0–P2 существует в истории ветки** —
> коммит `41aff7e` («M1e: deep audit review…», откачен коммитом `1de0ab8` по
> просьбе не вносить правки). Его можно применить целиком
> (`git cherry-pick 41aff7e`) или переносить по пунктам — все исправления в
> нём соответствуют предложениям ниже.

---

## Как читать приоритеты

| Приоритет | Смысл | Связь с REVIEW |
|---|---|---|
| P0 | блокер сборки — без этого `sbt compile` не пройдёт | R-01 |
| P1 | функциональная корректность — без этого красные тесты/демо | R-02, R-03 |
| P2 | соответствие спецификации — искажает домен или валидацию | R-04…R-14 |
| P3 | дизайн, API, документация | R-15…R-21 |
| P4 | инженерия: CI, репозиторий, инструменты | §4 REVIEW |
| P5 | развитие по ROADMAP (M2–M6) | — |

---

## P0 — Блокер сборки

### P0-1. Дать `ValidatedNec[Issue, Unit]` законный Monoid (R-01)

Проблема: `checks.combineAll` (`TicketValidator.validate`) и
`kids.combineAll` (`Bom.validateAmounts`) требуют
`Monoid[ValidatedNec[Issue, Unit]]`; cats его не выводит, т.к. у
`NonEmptyChain[Issue]` нет `Monoid` (только `Semigroup` —
`./reference/cats/docs/datatypes/chain.md`).

**Вариант A (рекомендуемый) — законный инстанс в `object Issue`:**

```scala
// model/Validation.scala
given Monoid[ValidatedNec[Issue, Unit]] with
  def empty: ValidatedNec[Issue, Unit] = Validated.Valid(())

  def combine(a: ValidatedNec[Issue, Unit], b: ValidatedNec[Issue, Unit]): ValidatedNec[Issue, Unit] =
    Semigroup[ValidatedNec[Issue, Unit]].combine(a, b)
```

`Valid(())` — настоящий нейтральный элемент накопительного `combine`, законы
выполняются. Обязательно добавить закон в `AlgebraLaws` (генератор `Issue` и
`ValidatedNec[Issue, Unit]` — в `Arbitraries`).

**Вариант B (без нового инстанса):** `checks.foldLeft(().validNec)(_ |+| _)`
и аналогично в `Bom` — работает, но размазывает знание «как складывать
валидации» по вызовам. Вариант A честнее: инстанс — точка единой семантики.

**Вариант C (не рекомендуется):** заменить `NonEmptyChain[Issue]` на
`Chain[Issue]` ради `Monoid` — теряется гарантия «ошибка непуста», которую
проект сознательно выбрал.

---

## P1 — Функциональная корректность

### P1-1. Исправить детектирование циклов в `Bom.toTree` (R-02)

Добавлять в `seen` ID **текущего** узла при спуске, а не ID ребёнка:

```scala
case refs =>
  val seenIncludingCurrent = seen ++ product.id.map(_.value)
  val children = refs.foldLeft(Right(Chain.empty[Fix[ProductTree]]): Either[Issue, Chain[Fix[ProductTree]]]) {
    case (acc, ref) =>
      for
        kids <- acc
        child = byId.get(ref.value)
          .toRight(Issue.error(XPath("/XJDF/ProductList"), s"Unresolved ChildRef '$ref'"))
        kid <- child.flatMap(c => toTree(c, byId, seenIncludingCurrent))
      yield kids :+ kid
  }
  children.map(cs => Fix(ProductTree.Node(product, cs)))
```

Обязательные регрессионные тесты в `TicketLaws` (сейчас их нет — поэтому баг
и прошёл незамеченным):
1. двухуровневое дерево (`Book → BookBlock` через `BindingIntent/@ChildRefs`)
   разворачивается без ложного цикла;
2. истинный цикл `a → b → a` даёт `Left`.

Попутный вывод для процесса: на любую «развёртку» и «свёртку» рекурсивных
структур нужен хотя бы один happy-path и один negative тест — в законы
BOM-машинерия вообще не покрыта.

### P1-2. Реализовать нисходящие диапазоны в `IntegerRange` (R-03)

Направление обхода должно определяться порядком первого и второго значения
после нормализации (§1.10.2: первое — старт, второе — конец):

```scala
def indices(size: Long): List[Long] =
  if size <= 0 then Nil
  else
    val f = normalizeIndex(r.from, size)
    val t = normalizeIndex(r.to, size)
    val from = math.max(0L, math.min(f, size - 1))
    val to   = math.max(0L, math.min(t, size - 1))
    if from <= to then (from to to).toList else (from to to by -1).toList
```

Существующий закон «`-1 0` selects everything in reverse» станет зелёным —
то есть исправление проверяется уже написанным тестом (хороший признак, что
закон сформулирован правильно). Дополнительно стоит добавить case `5 2`.

---

## P2 — Соответствие спецификации

### P2-1. Дополнить enum до спецификационных наборов (R-04, R-05)

- `DeviceStatus` += `Cleanup`, `Setup` (Table A.15, «New in XJDF 2.1»);
- `Sides` += `Unprinted` (Table A.40, «New in XJDF 2.1»).

Процессный вывод: при сверке enum с Appendix A специально искать пометки
«New in XJDF 2.x» — именно они теряются при беглом переносе. То же правило
применить к будущему M3 (в главе 6 таких пометок много).

### P2-2. Исправить номера таблиц в scaladoc ресурсов (R-06)

| Файл | Было | Надо |
|---|---|---|
| `resources/Color.scala` | 6.14 | 6.27 |
| `resources/Finishing.scala` (CuttingParams) | 6.25 | 6.53 |
| `resources/Finishing.scala` (FoldingParams) | 6.36 | 6.74 |
| `resources/Layout.scala` | 6.52 | 6.95 |
| `resources/Media.scala` | 6.57 | 6.114 |
| `resources/NodeInfo.scala` | 6.59 | 6.119 |
| `resources/Preview.scala` | 6.66 | 6.134 |

Системная мера на будущее: в M3 включить **генератор-отчёт «таблица → тип»**
(парсинг `./reference/xjdf/*.md`, сопоставление со scaladoc-ссылками) —
тогда номер секции и номер таблицы больше не перепутаются, а покрытие
каталога станет измеримым. Этот отчёт одновременно закрывает DoD-пункт M3
«счётчик покрытия ресурсов».

### P2-3. Рендерить `Part/@Option` спецификационным именем (R-07)

Добавить `PartitionKey.attributeName` (для `OptionKey` → `"Option"`) и
использовать его в `Show[Part]` и в сообщениях `TicketValidator`
(`checkPartAmountKeys` сейчас печатает `@OptionKey`, чего в XJDF нет).
Переименование enum-члена в `Option` невозможно (коллизия со `scala.Option`),
поэтому отображение имени — правильное место.

### P2-4. Ужесточить валидаторы до текста спецификации (R-08, R-09, R-13)

- **§3.4 (R-08):** дубликатом считать пары ResourceSet с одинаковыми
  `@Name/@Usage/@ProcessUsage`, у которых `@CombinedProcessIndex` либо
  отсутствует хотя бы с одной стороны, либо пересекается:

```scala
def sameKey(a: ResourceSet, b: ResourceSet): Boolean =
  a.name == b.name && a.usage == b.usage && a.processUsage == b.processUsage &&
    (a.combinedProcessIndex.isEmpty || b.combinedProcessIndex.isEmpty ||
      a.combinedProcessIndex.toChain.toList.exists(i => b.combinedProcessIndex.toChain.toList.contains(i)))
```

- **§6.1.2.1 (R-09):** собирать ключи со **всех** родительских
  `Resource/Part` (а не только при единственном Part):

```scala
val parentKeys = r.parts.toList.flatMap(_.keys).distinct
```

- **R-13 (второе предложение Table 6.3):** если ключ `PartAmount/Part`
  повторяет родительский, его значение SHALL совпадать с одним из значений
  родителя — добавить отдельной проверкой (нужен `matches`-подобный предикат
  «значение входит в список значений родителя по ключу»). Приоритет ниже:
  это SHALL, но редкий случай; можно вынести в M1 с пометкой в ROADMAP.

### P2-5. Починить сниппет в README (R-10)

`Validated` — не монада (см. собственный `docs/03-cats-mapping.md`), поэтому
`.flatMap(_.build)` не компилируется. Варианты: `.andThen(_.build)` (то же
самое, что делает `chainV` в `SpecExamples`) либо показать `mapN`. README —
первое, что читает пользователь; некомпилируемый пример там недопустим.

### P2-6. Добить `DropItem` и тип `Metadata` (R-11, R-12)

- `DropItem` += `totalDimensions: Option[Shape]`, `totalVolume: Option[Double]`,
  `totalWeight: Option[Double]` (Table 6.55);
- `Part/@Metadata`: тип в спецификации `regExp` — если не вводить отдельный
  opaque `RegExp`, хотя бы пометить в scaladoc `Part` намеренное упрощение
  `NmToken` (аналогично тому, как это сделано для `NmToken`-каталогов).

### P2-7. Решить интерпретацию дубликатов `"Product"` в `@Types` (R-14)

§3.1.3 формулирует запрет «Product + другие токены»; про `Product Product`
текст молчит, но слово «additional» подразумевает, что дубликат токена —
тоже нарушение. Предложение: в `checkTypes` проверять
`hasProduct && types.size > 1` (т.е. запрещать `Product` в любой комбинации
с чем-либо, включая себя). Если хочется консервативно — оставить как есть,
но записать решение в ROADMAP со ссылкой на §3.1.3.

---

## P3 — Дизайн и API

### P3-1. Определиться с типом ChangeOrder (R-15)

Текущее `type ChangeOrder = XJDF & Partial` — законный синтаксис
intersection types, но `XJDF extends Partial`, поэтому пересечение равно
`XJDF` и «контекст изменения» на уровне типов не существует. Три варианта:

- **(A) Оставить маркер, починить документацию.** Минимальный дифф: scaladoc
  `Partial`/`ChangeOrder` и раздел `docs/02` честно описывают, что
  пересечение документирует контекст, а не создаёт отдельный тип. Подходит,
  если до M2-кодеков отдельный тип не нужен.
- **(B) `opaque type ChangeOrder = XJDF`** — отдельный номинал с нулевой
  стоимостью; conversion-методы `of`/`asTicket` в компаньоне. Даёт настоящий
  тип-контекст, но требует решений о конверсиях в DSL/кодеках.
- **(C) Обёртка с полезной нагрузкой** (`ChangeOrder(diff: Patch, base: Option[XJDF])`)
  — самый «честный» с точки зрения §1.3.2 («содержит только изменённые
  значения»), но это уже не точечная правка, а дизайн-этап.

Рекомендация: **(A) сейчас** (правки только в документации), **(B) или (C) —
решением M1** до проектирования кодеков M2, потому что способ кодирования
partial-тикета (§1.4.2, §1.6.5) завязан на представление типа.

### P3-2. Починить текст про `matches` (R-16)

В `docs/01-category-theory-view.md` заменить «рефлексивно и транзитивно» на
точную формулировку: отношение совместимости §6.1.3.2 («нет атрибутов,
расходящихся с селектором»), **рефлексивное, но не транзитивное**
(контрпример: `{k=1} ⊑ {} ⊑ {k=2}`, но `{k=1} ⋢ {k=2}`), поэтому это не
предпорядок и не «тонкая категория» в строгом смысле. Закон рефлексивности
уже есть в `PartitionLaws`; можно добавить явный контрпример транзитивности
как `test`, чтобы зафиксировать ожидаемое поведение.

### P3-3. Выровнять API `TicketDraft` (R-17)

`withJobPart`/`withProject` должны вести себя как `TicketDraft.of` —
возвращать `ValidatedNec[Issue, TicketDraft]` (или накапливать ошибку до
`build`). Сейчас невалидный `@JobPartID` молча исчезает, а невалидный
`@JobID` — ошибка. Для декларативного DSL важна симметрия: либо всё
«мягко» с накоплением, либо всё «жёстко».

### P3-4. Убрать скрытые исключения из `PartBuilder` (R-18)

`PartBuilder.set` бросает `IllegalArgumentException` при несовпадении вида
значения ключа. Предложение: `Either[String, Part]` у `set`, либо явные
имена `setTokenUnsafe`/`setToken` по конвенции принципа 5 (`docs/04`:
исключения только в `unsafe`-конструкторах).

### P3-5. Пометить `IdAllocator.stateful` как не-потокобезопасный (R-19)

Мутабельный `var` — осознанный компромисс, но context function не передаёт
пользователю этого знания. Предложение: scaladoc-пометка
«not thread-safe; use `IdSource.fresh` (State) for pure/parallel contexts» +
в M4 заменить на эффектный `Ref[F, Long]` при появлении `F`.

### P3-6. Включить целостность BOM в `TicketValidator` (R-21)

`validate` должен для тикетов с `productList` вызывать `Bom.fromProductList`
и превращать `Left(Issue)` в накопленную ошибку (XPath `/XJDF/ProductList`).
Иначе «структурно валидный» тикет может содержать цикл в BOM — а это ровно
тот класс ошибок, ради которого существует валидатор.

### P3-7. Мелкое (R-20)

Scaladoc `XjdfVersion`: упомянуть Table A.52 (2.0/2.1 существуют как значения
enum `XJDFXJMFVersion`; библиотека сознательно принимает только `"2.2"`,
Table 3.1).

---

## P4 — Инженерия и процесс

### P4-1. CI (главное)

`.github/workflows/ci.yml` на базе `actions/setup-java` (Temurin 17/21) +
`coursier/setup-action` для sbt 2.0.2: `sbt compile test examples/run
scalafmtCheckAll`. Прямо решает системную проблему проекта («сборка вслепую»,
пункт 1 рисков ROADMAP): блокер R-01 был бы пойман автоматически на PR, а не
через ручное ревью. Кэш coursier — для скорости. Замечание ROADMAP «warnings
не допускаются» превратить в флаг `-Werror` в CI (или отдельный job) — но
вводить `-Werror` только после того, как текущая сборка зелёная.

### P4-2. Убрать `build.log` из git

`git rm --cached build.log` (он уже в `.gitignore` по маске `*.log`) — файл
устарел и противоречит ROADMAP (утверждение о закрытой feedback-итерации).

### P4-3. Добавить LICENSE

Apache-2.0 — стандарт для Typelevel-экосистемы и требование для заявленной
публикации в Sonatype (M6). Сейчас файла нет.

### P4-4. Минимальный sbt-плагин scalafmt

Сейчас `.scalafmt.conf` есть, но форматирование держится на дисциплине.
`sbt-scalafmt` (плюс `scalafmtCheckAll` в CI, см. P4-1) — единственный
плагин, который стоит добавить; «никаких плагинов» в build.sbt было
разумным решением для риска, но этот плагин не влияет на сборку домена.

### P4-5. Покрытие законов по каждому инстансу

Конвенция ROADMAP «каждый cats-инстанс — с property-тестом» выполняется, но
стоит формализовать: в P0-1 показан шаблон (инстанс → генератор → законы);
прогнать ревизию всех 10+ инстансов на соответствие шаблону и дописать
недостающее (`Order`, `Show`-согласованность не тестируются — можно не
гоняться, но зафиксировать решение).

### P4-6. Примеры спецификации — как тесты

`SpecExamples` живут в `modules/examples` (main), но их «валидность» —
идеальный тестовый оракул: перенести проверки
`minimalProduct/notebook/combinedProcesses/splitDelivery/brochureJob →
isValid` в `TicketLaws` (munit), а `examples` оставить демонстрационным.
Сейчас `TicketLaws` дублирует часть примеров вручную — дублирование уйдёт.

---

## P5 — Развитие по ROADMAP (архитектурные советы на M2–M6)

### M2 — кодеки

- `codec-core`: `Encoder[A]`/`Decoder[A]` как typeclass'ы с законами
  `decode ∘ encode = id` на **всех** типах M1 — тот же дисциплинарный шаблон,
  что в laws; законы должны жить в `laws`-модуле, а не в codec-тестах.
- Атомарные типы (XYPair, matrix, rectangle, dateTime/duration, LabColor,
  PDFPath, TransferFunction) — на **cats-parse**, с property-тестами
  round-trip; не изобретать парсеры вручную.
- JSON-исключения (§1.4.2) оформить **списком-реестром** (`JSON_EXCEPTIONS.md`
  или раздел ROADMAP): `$schema`, `Name`, `AuditPool` как массив с `Name`,
  `Comment/@Text`, `Types` массивом — чтобы каждый кодек ссылался на пункт, а
  не на память. Это же решит «текст vs schema.xsd» риск (п. 4 рисков
  ROADMAP): текст — истина, XSD — оракул в тестах.
- `ChangeOrder`/Partial-кодирование (§1.6.5) — после решения P3-1.

### M3 — каталог ресурсов главы 6

- Партии по алфавиту, каждая — отдельный PR с тестом «строится и
  валидируется» (план уже в ROADMAP — поддержать).
- «Intent Pairing» оформлять не комментарием, а **типовым реестром**
  (`ResourceRegistry`/`IntentPairing` как таблица на уровне типов), иначе
  проверку «процесс получает разрешённые ресурсы» (заявленную в ROADMAP)
  нечем исполнять.
- Отчёт «таблица → тип» (см. P2-2) — ввести с первой же партии.

### M4 — XJMF (глава 7)

- 4 семейства сообщений — enum-иерархией с общей поверхностью (`Header`),
  как сделано с `Audit`; транспорт — за `Kleisli[F, *, *]`, домен без
  эффектов.
- Продолжить выравнивание Table 3.2: `CommandReturnQueueEntry →
  AuditProcessRun` — тем же приёмом, что `Alignment.signalToAudit`, и с
  законом на каждый case (шаблон `AlignmentLaws`).

### M5 — workflow

- «Конвейер тикетов»: композиция морфизмов Controller → Device с проверкой
  стыковки output→input ResourceSet — оформить как **категорию** (типы
  «порт-наборов» как объекты, тикеты как стрелки) с законом
  ассоциативности композиции в laws; это прямое продолжение «XJDF —
  морфизм» из `docs/01`.
- `Eval`-безопасный cata для глубоких BOM (пункт уже в ROADMAP) — начать с
  бенчмарка глубины, не оптимизировать вслепую.
- Writer-семантика аудитов на fs2-потоке сигналов — как демо, с
  `WriterT`/`NonEmptyChain`-аккумулятором (обоснование уже в `docs/03`).

### M6 — публикация

- `sbt-typelevel` (или ручной Sonatype) — но перед этим LICENSE (P4-3) и
  MiMa для ядра.
- «Дорожная проверка» на реальных XJDF из CIP4-репозитория — включить в CI
  как отдельный job на тегах, чтобы регрессий кодеков не было.

---

## Приложение A. Соответствие находок REVIEW и предложений

| REVIEW | Предложение |
|---|---|
| R-01 | P0-1 |
| R-02 | P1-1 |
| R-03 | P1-2 |
| R-04, R-05 | P2-1 |
| R-06 | P2-2 |
| R-07 | P2-3 |
| R-08, R-09, R-13 | P2-4 |
| R-10 | P2-5 |
| R-11, R-12 | P2-6 |
| R-14 | P2-7 |
| R-15 | P3-1 |
| R-16 | P3-2 |
| R-17 | P3-3 |
| R-18 | P3-4 |
| R-19 | P3-5 |
| R-20 | P3-7 |
| R-21 | P3-6 |
| §4 REVIEW (нет CI, build.log, LICENSE) | P4-1, P4-2, P4-3 |

## Приложение B. Готовые реализации

- Коммит `41aff7e` в истории ветки содержит реализации P0-1, P1-1, P1-2,
  P2-1…P2-5 (код, тесты, документация) — применяется целиком через
  `git cherry-pick 41aff7e`; откат-коммит `1de0ab8` можно удалить
  (`git revert 1de0ab8`) либо пересоздать ветку с чистого состояния.
- Пункты P2-6…P3-7 в том коммите **не** реализованы — это новые предложения
  этого документа.

## Приложение C. Порядок внедрения (рекомендуемый)

1. P0-1 → `sbt compile` зелёный.
2. P1-1, P1-2 → `sbt test` зелёный (закрывает красный закон).
3. P2 (весь блок) → домен соответствует спецификации; пересобрать,
   перегнать тесты.
4. P3-2, P3-1(вариант A), P3-6 — документирующие/валидационные правки без
   риска.
5. P4-1 — CI, чтобы дальнейшие итерации больше не шли вслепую.
6. P3-3…P3-7 и P5 — по мере движения по M1/M2 ROADMAP.