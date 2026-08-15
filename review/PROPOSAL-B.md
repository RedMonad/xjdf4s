# PROPOSAL — предложения по развитию xjdf4s

> Статус: предложение к обсуждению (RD, request for decisions).
> Основано на аудите `REVIEW-B.md` (2026-08-15) и сверено с `./reference/xjdf/*`,
> `./reference/cats/*`, `./reference/scala/*`, `./reference/sbt/*`.
> Документ **дополняет**, а не заменяет `ROADMAP.md`: каждый пункт либо
> закрывает находку REVIEW, либо уточняет милстоуны M1–M6.

## Как читать

- **Приоритет**: `P0` — блокирует корректность M0/M1, делать немедленно;
  `P1` — до старта кодеков (M2); `P2` — качество жизни разработчика; `P3` —
  стратегические, синхронизировать с ROADMAP.
- Каждое предложение: *Контекст → Проблема (ссылка на evidence) → Предложение
  (с эскизом кода, где уместно) → Шаги → Критерий приёмки*.
- Нумерация находок `REVIEW-B.md` сохранена: `R<раздел>.<пункт>` (например, R2.1 —
  REVIEW, раздел «Соответствие спецификации», пункт «Sides»).

## Свод приоритетов

| # | Приоритет | Тема | Закрывает |
|---|-----------|------|-----------|
| P-01 | P0 | Enum-значения XJDF 2.1 и токены | R2.1, R2.2, R2.3 |
| P-02 | P0 | Несобираемые/неверные документы (README, docs/03, ссылки) | R4 |
| P-03 | P0 | Гигиена VCS и процесс | R1.1, R1.3 |
| P-04 | P0 | Дизайн `ChangeOrder` | R3.1, R3.2 |
| P-05 | P1 | §3.4 «common or no entries» | R2.4 |
| P-06 | P1 | `PartAmount.parts: Chain[Part]` + полное §6.1.2.1 | R2.5 |
| P-07 | P1 | Унифицированная проводка законов в `TicketValidator` | R2.6 |
| P-08 | P1 | `Resource.specific: Option` | R2.7 |
| P-09 | P1 | `Notification`: `@ModuleID`, правило Milestone⇒Event | R2.10 |
| P-10 | P1 | CT-точность: tolerance relation, free semigroup | R3.3, R3.4 |
| P-11 | P2 | `specToken`-реестр и порядок элементов (подиум M2) | R2.9 |
| P-12 | P1 | Генераторы/золотые примеры/регрессия по seed | развитие laws |
| P-13 | P2 | discipline-munit (опционально, обоснованный выбор) | качество laws |
| P-14 | P2 | Стек-безопасный `Bom.cata` на `Eval` раньше M5 | R3.6 |
| P-15 | P2 | ADR-каталог, матрица покрытия спеки, scaladoc-конвенция | R2.8, R4 |
| P-16 | P2 | CI и закрепление зелёной сборки | R1.1, R5 |
| P-17 | P3 | Выравнивание M2–M6 | ROADMAP |

---

## P-0x. Предложения

### P-01. Восстановить полноту enum под XJDF 2.2 — `P0`

**Контекст.** Проект декларирует цель «enumeration values из Appendix A» и
5 значений там, где спека даёт 7 (примеры R2.1, R2.2). Закрытый enum —
главный продукт библиотеки: его неполнота — это *неверное знание о домене*,
а не недостача фич.

**Проблема.**
1. `Sides` не содержит `Unprinted` (Table A.40, *New in XJDF 2.1*) — R2.1.
2. `DeviceStatus` не содержит `Cleanup`, `Setup` (Table A.15, *New in XJDF 2.1*) — R2.2.
   По нарративу ROADMAP M1d кейс `Setup` был удалён при починке сборки;
   если конфликт был имённым (`Status.Setup` vs `DeviceStatus.Setup` после
   wildcard-импорта), решается явной ссылкой, а не удалением члена спеки.
3. `HardCoverJacket.Glued` печатается токеном `"Glued"`; спека (таб. 4.11):
   `Glue` — R2.3.

**Предложение.**
```scala
enum Sides extends XjdfEnum:
  case OneSided, OneSidedBack, TwoSidedHeadToFoot, TwoSidedHeadToHead, Unprinted
  def token: NmToken = NmToken.unsafe(this.toString)

enum DeviceStatus extends XjdfEnum:
  case Cleanup, Idle, NonProductive, Offline, Production, Setup, Stopped
  def token: NmToken = NmToken.unsafe(this.toString)

enum HardCoverJacket extends XjdfEnum:
  case Unjacketed, Loose, GlueApplied   // имя кейса ≠ токену спеки намеренно
  def token: NmToken = this match
    case Unjacketed  => NmToken.unsafe("None")
    case Loose       => NmToken.unsafe("Loose")
    case GlueApplied => NmToken.unsafe("Glue")   // Table 4.11
```

**Шаги.**
1. Исправить три enum; прогнать `grep` по использованиям (`DeviceStatus.Idle`
   в демах остаётся валидным).
2. Завести **единый реестр намеренных переименований** «кейс Scala ↔ токен
   спеки» (см. P-11): сейчас это `NoBinding→None`, `Unbound→None`,
   `Uncoated→None`, `Unscored→None`, `Unjacketed→None`, `GlueApplied→Glue`,
   `OptionKey→Option`. Каждая запись — со ссылкой на таблицу.
3. Добавить property-закон в laws: для каждого enum-модуля
   `E.all.map(_.token.value).toSet == <золотое множество из таблицы>` — золотое
   множество хранится литералом рядом с тестом со ссылкой на таблицу.

**Критерий приёмки.** Закон-тест по каждому enum сопоставляет токены один-в-
один таблице Appendix A / главы 4; `sbt test` зелёный; реестр переименований
описан в `docs/02-scala3-features.md`.

---

### P-02. Починить документы, которые врут компилятору и читателю — `P0`

**Проблема.** README «Минимальный пример» — `.flatMap(_.build)` на
`ValidatedNec` (не компилируется; R4). `docs/03` утверждает, что `.andThen` на
`Validated` «не компилируется» — фактически неверно и противоречит
`dsl.intent`, который его использует (подтверждено сырцом cats 2.13.0).
Битые ссылки: `docs/02 → "03-cats.md"`, `docs/01 §1 → "Part 1 –
its-all-about-morphisms"` (файл — Part 3).

**Предложение.**
- README-пример переписать через `.andThen`:
  ```scala
  val ticket: ValidatedNec[Issue, XJDF] =
    dsl.TicketDraft.of("J1", ProcessType.Product).andThen(_.build)
  ```
  и добавить в munit-тест («README-example compiles and validates») — один
  тест, который больше не даст примеру сгнить.
- `docs/03`: переписать тезис — «нет `flatMap`/for-comprehension; есть
  `andThen` (right-biased sequencing без накопления левой ошибки)».
- Провести линт markdown-ссылок по `docs/*` и ROADMAP (вручную; 6 файлов).

**Критерий приёмки.** Каждый код-сниппет README/docs либо компилируется в
тесте, либо помечен «псевдокод»; ни одной битой ссылки на `./reference/*`.

---

### P-03. Гигиена VCS и процесс — `P0`

**Проблема.** Весь M0 — один коммит «Reformat code using ScalaFmt»
(+7150 строк; R1.3); `build.log` с красным прогоном лежит в индексе в обход
`*.log` из `.gitignore` (R1.1); в истории нет следов задокументированных
итераций M1/M1b/M1c/M1d.

**Предложение.**
1. `git rm --cached build.log`; удалить файл; подтвердить, что `*.log`
   работает. Если хочется следов — хранить логи артефактами CI, не деревом.
2. С этого момента — коммиты по `ROADMAP §4` (`M<n>: …`), один логический
   шаг = один коммит; PR-описание — со ссылками на таблицы (как заявлено в
   конвенциях — конвенцию стоит соблюдать начиная с себя).
3. В ROADMAP M1 «Feedback-итерация» дописать **дату и хэш** состояния, на
   котором suite зелёный — чтобы следующий аудит не читал чаепитие из
   устаревшего лога.

**Критерий приёмки.** В индексе нет файлов, покрытых `.gitignore`; история
M1 читается как серия атомарных `M1: …` коммитов.

---

### P-04. Сделать `ChangeOrder` настоящим типом (ADR-решение) — `P0`

**Проблема (R3.1, R3.2).** `ChangeOrder = XJDF & Partial` вырождено: `XJDF`
уже `extends Partial`, пересечение совпадает с `XJDF`, ни одна сигнатура API
`ChangeOrder` не принимает. Требование §1.3.2 «change order несёт только
изменённые значения» не выражено: `JobId`/`Types` обязательны и там.
`mergeResourceSets` документирует `Left` (недостижим) и «update wins»
(на практике wins old — `select` first-match доходит до раннего дубликата),
и результат нарушает §3.4 дубликатом ключей.

**Обсуждаемые варианты.**
- **Вариант A (минимальный).** Убрать `extends Partial` из `XJDF`;
  `ChangeOrder` оставить алиасом-интент-заявлением, но перестать его
  рекламировать как «различитель»; `mergeResourceSets` принимает
  `update: Chain[ResourceSet]` и делает **upsert по ключу** (с P-05). Минусы:
  intersection остаётся декоративным — фича заявки не раскрыта.
- **Вариант B (рекомендуемый).** Отдельный тип change order с релаксированной
  кардинальностью; `XJDF` получает пару `extends Complete`, а intersection
  используется честно — как тип «тикет, который ДЕЙСТВИТЕЛЬНО содержит
  изменённые поля», возвращаемый умным конструктором:
  ```scala
  trait Complete        // маркер полного тикета
  trait Partial         // маркер ослабленной кардинальности (§1.6.5)

  final case class XJDF( /* …без Partial… */ ) extends Complete

  /** §1.3.2: хранит только изменённые значения. */
  final case class ChangeOrder(
      jobId: JobId,                              // требуется: цель изменения
      jobPartId: Option[JobPartId] = None,
      productList: Option[ProductList] = None,   // replace
      auditPool: Option[AuditPool] = None,       // append (хронологически)
      resourceSets: Chain[ResourceSet] = Chain.empty,  // upsert по §3.4-ключу
      comments: Chain[Comment] = Chain.empty
  ) extends Partial:
      /** Декларативное ядро: change order ЕСТЬ патч (эндоморфизм). */
      def toPatch: Patch = Patch { t =>
        t.copy(
          productList = productList.orElse(t.productList),
          auditPool   = Merge.appendAudits(t.auditPool, auditPool),
          resourceSets = Merge.upsertSets(t.resourceSets, resourceSets) // P-05 ключ
        )
      }

  /** Честное место для intersection: тикет, прошедший через контекст изменения. */
  type TicketInChange = XJDF & Partial
  ```
  `TicketInChange` населяется обёрткой `private final case class Changed(x: XJDF)
  extends Partial`; конструировать можно только через
  `ChangeOrder.applyTo(t): TicketInChange` — intersection становится
  невырожденным *и* рабочим: ни одна полная редакция не проскочит этот тип.
- **Вариант C.** Как B, но без маркеров вообще; intersection-демонстрацию
  перенести в M4 (XJMF): там она органична — `type SignalledResponse =
  Response & WithHeader` и т.п.

**Предложение.** Реализовать B; intersection оставить как readonly-вид
`TicketInChange`; README/docs обновить честным примером. Забанить конструкции
«тип-A extends M + type X = A & M» правилом стиля (вывеска — в ADR, P-15).

**Плюс:** починить `mergeResourceSets`: заменить набор по ключу (с P-05) в
порядке «старые позиции сохраняются, конфликтующие заменяются, новые —
добавляются»; `Ior.both` — с `Issue.warning` на каждый заменённый ключ;
скаладок — под реальное поведение.

**Критерий приёмки.** `ChangeOrder` используется минимум в трёх сигнатурах
(`applyTo`, `toPatch`, merge-API); пример «change order» из `SpecExample`
переписан на новый тип; закон-действие `applyTo(applyTo(t, p), q) ==
applyTo(t, p |+| q)` сохраняется (через `toPatch`); ADR-0001 принят.

---

### P-05. §3.4 — дубликаты ResourceSet по «common or no entries» — `P1`

**Проблема (R2.4).** Модель ловит только точное равенство
`ResourceSetKey`; спека запрещает также пересечения по
`@CombinedProcessIndex` и смеси «без CPI + с CPI».

**Предложение.**
```scala
/** §3.4: two sets clash when Name/Usage/ProcessUsage are equal AND the CPI
 *  lists have common entries, or either of them is absent. */
def clashesWith(a: ResourceSet, b: ResourceSet): Boolean =
  a.name == b.name && a.usage == b.usage && a.processUsage == b.processUsage &&
    ((a.combinedProcessIndex, b.combinedProcessIndex) match
      case (None, _) | (_, None) => true
      case (Some(x), Some(y)) =>
        val xs = x.toChain.toList.toSet
        y.toChain.toList.exists(xs.contains))
```
`checkResourceSetKeys` переписать как `pairs.exists(clashesWith)` (O(n²) на
_chain_ сетах допустимо — сетов в тикете единицы). Использовать ту же функцию
в `Merge.upsertSets` (P-04) — один источник истины.

**Критерий приёмки.** Тесты: `[CPI=[0], CPI=[0,1]]` и `[no-CPI, CPI=[1]]`
— invalid; `[CPI=[0], CPI=[1]]` — valid (текущий Example 3.6); старый
тест `Chain(a, a)` — invalid (регрессия).

---

### P-06. `PartAmount.parts: Chain[Part]` и полный §6.1.2.1 — `P1`

**Проблема (R2.5).** Table 6.3: `Part*`; модель держит один `Part`. Правила:
(а) `PartAmount/Part` SHALL NOT содержать ключей, уже **однозначно** заданных
в родителе; (б) при совпадении ключа значение SHALL быть одним из значений
родительских Part.

**Предложение.**
1. `PartAmount(part: Part=…)` → `parts: Chain[Part] = Chain.empty`; миграция
   вызовов тривиальна (все текущие — одиночные).
2. Полная проверка:
   ```scala
   /** Parent context: every distinct value of a key across Resource/Part. */
   def parentValues(parts: Chain[Part], key: PartitionKey): List[PartitionValue] =
     parts.toList.flatMap(_.valueOf(key)).distinct

   /** Table 6.3 / §6.1.2.1: a PartAmount/Part SHALL NOT repeat a key that is
    *  uniquely specified in the parent; if the parent mentions the key with
    *  several values, the PartAmount value SHALL be one of them. */
   def checkPartAmount(r: Resource): ValidatedNec[Issue, Unit] =
     val checks =
       for
         pa <- r.amountPool.toList.flatMap(_.toList)
         p  <- pa.parts.toList
         k  <- p.keys
         pv <- p.valueOf(k).toList
         parentVals = parentValues(r.parts, k)
         if parentVals.nonEmpty
       yield
         if parentVals.lengthIs == 1 then
           Validated.invalidNec[Issue, Unit](
             Issue.error(XPath("/XJDF/ResourceSet/Resource/AmountPool"),
                         s"§6.1.2.1: key $k is already uniquely specified in the parent"))
         else if !parentVals.contains(pv) then
           Validated.invalidNec[Issue, Unit](
             Issue.error(XPath("/XJDF/ResourceSet/Resource/AmountPool"),
                         s"§6.1.2.1: value of $k SHALL be one of the parent values"))
         else ().validNec
     checks.foldLeft(().validNec[Issue])(_ |+| _)
   ```
   (скелет; `parentValues` — единственная точка сбора контекста родителя).
3. Сохранить поведение «single parent-Part» как частный случай — текущие тесты
   зелёными.

**Критерий приёмки.** Примеры §6.1.2.1 (положительный и оба негативных) —
тесты; `mergeWith` логика `Part` переиспользуется без дублирования traversal.

---

### P-07. Законы — из декорации в механизм: единая `Lawful`-сверка — `P1`

**Проблема (R2.6).** `isLawful` у `BindingIntent`, `VariableIntent`,
`Disposition`, `PartWaste` никем не вызываются; `@BindingSide SHALL NOT be
provided if @BindingOrder="None"` (Table 4.8), «Milestone ⇒ `@Class="Event"`»
(Table 8.49) — вообще не смоделированы. Валидатор — главный продукт M0, а до
доменных законов у него нет шинного доступа.

**Предложение.** Категорийно аккуратный дизайн: **локальные законы как
алгебра, валидация как катаморфизм дерева тикета**. Каждый узел дерева
домена объявляет свои SHALL-правила одинаково:
```scala
/** A model node that carries local structural laws (spec SHALL/SHALL NOT). */
trait Lawful:
  /** Local violations, independent of the node's children. */
  def localIssues: Chain[Issue]

object Lawful:
  /** The structural fold: root checks + every descendant's localIssues. */
  def audit(ticket: XJDF): ValidatedNec[Issue, Unit] = ??? // обход:
    // XJDF → ProductList → Product → Intent → payload (+details)
    //      → ResourceSet → Resource → payload (+FileSpec/Disposition/…)
    //      → AuditPool → Audit → payload
    // каждый узел folds его localIssues; combineAll по ValidatedNec.
```
- `BindingIntent.localIssues` = парность details↔BindingType + запрет
  BindingSide при `BindingOrder=None` (Table 4.8).
- `VariableIntent.localIssues` = min≤avg≤max (§4.14) + запрет `@ChildRefs`
  при AssemblingIntent/BindingIntent на продукте (Table 4.36 — требует контекста
  продукта: закон поднимается на уровень `Product` localIssues — честно,
  потому что правило не локально для интента).
- `Disposition.localIssues`, `PartWaste.localIssues`, `Notification…` и т.д.
- `TicketValidator` оставляет только *глобальные* законы (уникальность ID,
  CLI-пересечения, хронология, IDREF) и вызывает `Lawful.audit`.

Так восстанавливается заявленный образ мыслей: «валидация — единый
гомоморфизм из дерева тикета в `ValidatedNec[Issue, Unit]`» — и
де-факто реализованный, а не декларированный.

**Альтернатива (дешевле, хуже):** вручную дописать вызовы в
`checkIntentLawfulness`. Не рекомендуется: уже 5 реализаций `isLawful` — и все
розно; законы глав 4/6/8 прибудут с M1/M3, шина окупится немедленно.

**Критерий приёмки.** Негативные тесты: SaddleStitch с SoftCoverBinding;
VariableIntent 9<5; Milestone+Class=Warning; Disposition с двумя временами —
все invalid. Позитивные примеры (brochureJob, notebook) не деградируют.

---

### P-08. `Resource.specific: Option[ResourcePayload]` — `P1` (до M2 обязательно)

**Проблема (R2.7).** Table 6.1 допускает `<Resource/>` без specific; Example
3.6 это использует. Сейчас такое непредставимо, и M2-кодеки не смогут
round-trip'ить легальный документ.

**Предложение.** `specific: Option[ResourcePayload] = None`; `elementName`
→ `Option[NmToken]`; `hasLawfulChildren` пропускает `None` (правило «Name
совпадает» применимо лишь при наличии specific); в `Foreign`-логику ничего не
переносится. Обновить `SpecExamples.combinedProcesses`, сделав его буквально
равным Example 3.6 (`<Resource/>`).

**Критерий приёмки.** Example 3.6 собирается один-в-один со спекой; закон
«select первым матчем» не деградирует.

---

### P-09. Мелкие поля и правила таблиц 3.1/8.49 — `P1`

- `XJDF` += `name: Option[XjdfString]` (Table 3.1 `@Name`).
- `Notification` += `moduleId: Option[NmToken]` (Table 8.49) и правило
  «Milestone present ⇒ Class="Event"» — через умный конструктор:
  ```scala
  def milestone(header: Header, m: Milestone, ...): Notification =
    Notification(SeverityClass.Event, ..., detail = Some(m)) // текст: Some(...) : Event|Milestone
  ```
- `Comment`-множественность по `@Language` (Table 8.49-контекст) — в Lawful
  локальных законах контейнеров (P-07).
- `Product/@PartVersion`-паритет root/child (Table 3.11 ш.2) — глобальным
  законом `TicketValidator` (не локален).

---

### P-10. Категориальная честность docs (и одна настоящая находка) — `P1`

1. **`docs/01 §3` (R3.3):** формулировка «matches — preorder (рефлексивно и
   транзитивно)» — ложна. `matches` рефлексивно, симметрично (доказательство:
   `a.matches(b) ⟺ conflictingKeys(a,b).isEmpty`), и **не** транзитивно
   (контрпример: `{SheetName=S1} ≼ {} ≼ {SheetName=S2}`). Это **отношение
   толерантности** — граф совместимости контекстов. Настоящий порядок здесь —
   по конфликт-свободному merge:
   `a ≤ b ⟺ mergeWith(a,b).isRight && merge(a,b) == b` (поглощение без
   конфликта); на конфликт-фри фрагментах `Part` — частичная полурешётка.
   Предлагаю переписать §3 с этой парой («совместимость — tolerance;
   уточнение — partial semilattice»), и **добавить закон** в laws:
   `a.matches(b) == a.conflictingKeys(b).isEmpty` (симметрия-мост).
2. **Термин «свободный моноид» без единицы (R3.4):** `NonEmptyChain`-носители —
   свободные **полугруппы**; в `docs/01 §4` заменить таблицу колонкой
   «свободная конструкция» значениями `free semigroup (NonEmptyChain)` /
   `free monoid (Chain)`, и одной строкой пояснить `T+`/`T*`-кардинальности
   спеки. Читатель, пришедший за Милевски, оценит.
3. **«Сопряжение» §7 (R3.5):** пометить как эвристику; *или* сделать честным
   артефактом — типизированный реестр `Intent Pairing` из главы 4
   (например, `MediaIntent ↔ Media, LayoutIntent ↔ Layout…`) + закон-тест
   «реестр закрыт по таблице спеки» — это уже заявлено в ROADMAP M3, можно
   начать с интентов M0.
4. Ссылка «Part 1 – its-all-about-morphisms» → Part 3.

---

### P-11. Подиум для кодеков (M2): токены и порядок — `P2`

M2 зайдёт темой сериализации; готовые сейчас артефакты снимут 80% трения:

1. `specToken`-реестр (см. P-01): `PartitionKey` реализует `XjdfEnum`-стиль с
   единственным отступлением `OptionKey→"Option"` (и `Metadata: regExp`,
   `ProductPart: NMTOKEN`-отклонения — R2.9 — зафиксировать в scaladoc как
   *documented deviations*; либо вернуть строгую типизацию спеки и вынести
   удобства в extension — решить в ADR-0002).
2. Порядок сериализации §1.3.5.1 (лексикографический, Specific Resource —
   последним; Table 6.1 exception) — уже сейчас задать в модели явные
   константы порядка (например, `Resource.subelementOrder: List[NmToken]`), а
   не выковыривать их из таблиц во время M2.
3. JSON-исключения (`$schema`, AuditPool-массив с `@Name`, `Comment/@Text`) —
   собрать в `docs/04`-приложение таблицей «правило → источник».

**Критерий приёмки (подиума).** Ни один кодек-решение не требует
переписывания доменных case-классов; список отступлений — зафиксирован.

---

### P-12. Тестовая инфраструктура — `P1/P2`

1. **Генераторы (P1).** `arbPart` порождает только 5 из 27 ключей — почти все
   соединения overlay/matches не покрыты. Переписать: для каждого ключа —
   `Gen` по его типу (включая `Side`, `TileID: XYPair`, `PreviewType`,
   `TransferCurveTarget`, `ProductPart: IdRef`, range-ключи) с
   `Gen.option`/`Gen.listOf` призмы; оставить «малый» генератор для тикетов.
2. **Золотые примеры (P1).** Для каждого `SpecExample` — текстовый golden
   (рендер `Show`) в ресурсах теста с шаблоном «обновил модель — перечитал
   diff». Иначе примеры «3.1 из спеки» проверяются только на валидность, но не
   на верность спеке.
3. **Регрессия по failing seed (P2).** Хотя текущий код делает упавшее
   свойство истинным (см. R1.1), фиксация контрпримера стоит недорого:
   ```scala
   property("regression: pre-M1c overlay direction") {
     // left-biased overlay бы провалил это; храним пример из build.log
     val l = Part(docIndex = Some(IntegerRange(3, 3)), run = Some(NmToken.unsafe("Y")))
     val r = Part(docIndex = Some(IntegerRange(-10, -10)), run = Some(NmToken.unsafe("R")))
     Part.combine(l, r).docIndex.contains(IntegerRange(-10, -10))
   }
   ```
4. **coverage-report (P2):** тест, который считает реализованные ресурсы/
   интенты по отношению к спискам глав 4/6 и падает при регрессе
   (`assert(covered >= 12)` + в README счётчик — ROADMAP M3 это уже планирует,
   начать раньше).

---

### P-13. `discipline-munit` для kernel-laws — `P2` (обоснованный выбор)

Сейчас законы рукописные (`AlgebraLaws`) — это корректно, но
велосипед: `cats-laws` + `discipline-munit` дают готовые
`SemigroupTests`, `MonoidTests`, `SemilatticeTests`, `FunctorTests` с Eq-
инстансами, DisciplineSuite и стабильными именами. Плюсы: унификация,
меньше кода, признанные формулировки законов. Минусы: +2 зависимости
(`typelevel/cats-laws`, `discipline-munit`), резолв под 3.8.4 нужно проверить
в реестре (в нашем окружении нет JVM), Double-законы потребуют
приближённого Eq (уже есть `matrixEq`). **Решение:** попробовать в ветке
упражнения M1; если резолв или совместимость munit 1.3.0 хоть раз укусят —
оставить текущие законы, задокументировать выбор. Независимо от решения —
добавить `Eq`-инстансы там, где законы их требуют по-честному (`Patch` —
поведенческие законы уже есть ✅).

---

### P-14. Стек-безопасный `Bom.cata` раньше M5 — `P2`

ROADMAP относит `Eval`-cata в M5, но замена дешёвая и локальная:
```scala
def cataM[A](algebra: ProductTree[A] => Eval[A])(tree: Tree): Eval[A] =
  tree.unfix match
    case Leaf(p) => algebra(Leaf(p))
    case Node(p, kids) =>
      kids.toList.traverse(k => Eval.defer(cataM(algebra)(k)))
        .flatMap(cs => algebra(Node(p, Chain.fromSeq(cs))))
```
Глубокие BOM (500+ уровней вложенности продуктов — реальный кейс коробочного
производства) перестанут быть scala.StackOverflowError-лотереей. Тестом —
синтетическое дерево глубиной 100 000 (цепочкой ChildRefs).

---

### P-15. Документно-процессный слой — `P2`

1. **ADR-каталог** `docs/adr/NNNN-title.md` (формат Michael Nygard, 1 страница).
   Сразу завести: `0001-changeorder-design` (P-04), `0002-spec-deviations`
   (P-11), `0003-lawful-validation-bus` (P-07), `0004-discipline-laws`
   (P-13, если выбрано). ROADMAP молчаливо подразумевает ADR-дисциплину;
   ревью M1 она бы сэкономила откат-итерации.
2. **Матрица покрытия** (`docs/coverage.md`, генерируется вручную до M3):
   строки — главные таблицы (3.1, 3.3, …), колонки — «тип / поля / законы /
   тест / codec». Каждая P0/P1-находка закрывается строкой.
3. **Scaladoc-конвенция:** `§x.y / Table z` (раздел И таблица) — R2.8
   (Media/Device) показал, что одной таблицы мало: у спеки номера разделов и
   таблиц разнесены. Массовая правка — sed'абельна, влить до M1-финала.
4. **Ссылки в docs:** markdown-lint руками (P-02 покрывает битые).

---

### P-16. CI и «зелёная печать» — `P2`

В окружении автора JVM нет; значит, источник истины о сборке — пользователь.
Предложение: GitHub Actions (checkout → setup-java Temurin 21 → sbt
`+test examples/run` — с caching-`coursier`), бейдж в README; при
недоступности CI на стороне пользователя — скрипт `scripts/verify.sh`,
который печатает PASS/FAIL и версию sbt/Scala, и его вывод прикладывается в
PR-тред. Любой из двух вариантов **исключает** класс проблем «устаревший
build.log» навсегда (R1.1).

---

### P-17. Синхронизация с ROADMAP — `P3`

- M1: добавить пунктами P-01, P-04, P-05, P-06, P-07 (все P0/P1).
- M2: уточнить подготовкой из P-11; Acceptance M2 дописать «round-trip
  Example 3.6 (с пустым Resource)» (P-08).
- M3: coverage-counter (P-12.4) запустить уже в M1 («12 из ~14X», счётчик в
  README).
- M4: перенос честной intersection-демонстрации (P-04 вариант C-аспект).
- M5: P-14 вычеркнуть (закрыто раньше).
- Категориальные доклады (docs/01) — после P-10 держать в зелёном: тезис
  «каждое CT-утверждение имеет закон в laws-модуле или помечено как
  эвристика» — записать в `ROADMAP §4` конвенцией.

---

## План внедрения (2 итерации)

**Итерация M1a (P0, ~1–2 дня):** P-01 (+token law), P-02, P-03, P-04
(ADR-0001 + реализация B), P-05. Результат: модель снова «истинна для XJDF
2.2», intersection работает, README собирается, история чистая.

**Итерация M1b (P1, ~2–3 дня):** P-06, P-07 (шина + все локальные законы),
P-08, P-09, P-10, P-12.1–12.2. Результат: валидатор покрывает все
задекларированные SHALL из глав 3–6 реализованных типов; любой документ docs
имеет закон или ярлык «эвристика».

Дальше — по ROADMAP с включёнными P-11/P-14.

## Метрики приёмки PROPOSAL

1. `sbt test` зелёный и **подтверждён свежим логом вне VCS** (P-16).
2. Ни одного enum-несовпадения с Appendix A (law-тесты P-01.3).
3. `ChangeOrder` — в ≥3 сигнатурах; ни одного вырожденного intersection в
   публичном API.
4. Все `isLawful` вызваны из корневой валидации (grep-доказательство: `0`
   приватных `isLawful` без `localIssues`).
5. Golden-рендеры четырёх примеров спеки — зафиксированы.
6. Каждое расхождение со спекой — задокументировано (ADR-0002).