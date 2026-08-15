# Консолидированный план улучшения xjdf4s

> **Дата:** 2026-08-15  
> **Ветка:** `arena/01a0048d-xjdf4s`  
> **HEAD:** `90462ae82f9bc47dcff5ee59125a8c2957541504`  
> **Среда:** без JVM/sbt — статический аудит + факт-чекинг по исходникам  
> **Источники:** `review/REVIEW-{A,B,C}.md`, `review/PROPOSAL-{A,B,C}.md`, `review/DEPENDENCY-{DIAGRAM,REPORT}.md`, исходный код `modules/`

---

## Сводка факт-чекинга

Перед составлением плана выполнена перекрёстная проверка всех утверждений из трёх
ревью и трёх предложений. Ниже — консолидированные результаты.

### Подтверждённые находки

| ID | Утверждение | Источник | Факт-чекинг | Статус |
|---|---|---|---|---|
| F-01 | `Part/@ProductPart` — тип `IdRef` вместо `NMTOKEN` | REVIEW-A §1.2 | `Partition.scala:137`: `productPart: Option[IdRef]`; `ValueOf` (строка 110): `ProductPart.type => IdRef`; спек: Table 6.4 — `NMTOKEN` | ✅ **Подтверждено** |
| F-02 | `Part/@Metadata` — тип `NmToken` вместо `regExp` | REVIEW-A §1.3 | `Partition.scala:130`: `metadata: Option[NmToken]`; спек: Table 6.4 — `regExp` | ✅ **Подтверждено** |
| F-03 | `Sides` не содержит `Unprinted` | REVIEW-B §2.1 | `Enums.scala:49-53`: только 4 значения; спек Table A.40: 5 значений, `Unprinted` (New 2.1) | ✅ **Подтверждено** |
| F-04 | `DeviceStatus` не содержит `Cleanup`, `Setup` | REVIEW-B §2.2 | `Enums.scala:109-114`: `Idle, NonProductive, Offline, Production, Stopped` — 5 из 7; спек Table A.15: +`Cleanup`, `Setup` | ✅ **Подтверждено** |
| F-05 | `HardCoverJacket.Glued` — токен `"Glued"` вместо `"Glue"` | REVIEW-B §2.3 | `Enums.scala:514-515`: `case Glued` → `this.toString = "Glued"`; спек Table 4.11: `Glue` | ✅ **Подтверждено** |
| F-06 | `ChangeOrder = XJDF & Partial` — вырожденное пересечение | REVIEW-A §3.1 | `Ticket.scala:23`: `XJDF extends Partial`; строка 118: `type ChangeOrder = XJDF & Partial`. `XJDF <: Partial ⇒ XJDF & Partial ≡ XJDF` | ✅ **Подтверждено** |
| F-07 | `Monoid[ValidatedNec[Issue, Unit]]` не существует | REVIEW-C §5.1 (R-01) | `Validation.scala:56`: `checks.combineAll`; `Product.scala:192`: `kids.combineAll`. cats выводит `Monoid[Validated[E,A]]` только при `Monoid[E]`, у `NonEmptyChain` его нет | ✅ **Подтверждено** |
| F-08 | README-сниппет `.flatMap(_.build)` не компилируется | REVIEW-B §4 | `Validated` не имеет `flatMap` (документировано в `docs/03`); `docs/03` сам утверждает что `.andThen` «не компилируется» — но он компилируется и используется в `dsl.intent` | ✅ **Подтверждено** |
| F-09 | `Bom.toTree` добавляет в `seen` ID ребёнка, а не текущего узла | REVIEW-C §5.2 (R-02) | `Product.scala:151`: `toTree(c, byId, seen + c.id.fold("")(_.value))` — в `seen` попадает ID ребёнка, а должен родителя | ✅ **Подтверждено** |
| F-10 | 7 неверных ссылок на таблицы в scaladoc ресурсов | REVIEW-A §2.1 | Проверено по файлам: `Color.scala:7` (6.14→6.27), `Finishing.scala:9/44` (6.25→6.53, 6.36→6.74), `Layout.scala:8` (6.52→6.95), `Media.scala:8` (6.57→6.114), `NodeInfo.scala:7` (6.59→6.119), `Preview.scala:8` (6.66→6.134) | ✅ **Подтверждено** |
| F-11 | `NamedColor` — закрытый enum из 16 значений, а спек — открытый список | REVIEW-A §2.2 | `Enums.scala` — `NamedColor` закрытый; Appendix A.2.30: «For a list of allowed values, see [Color Names]» | ✅ **Подтверждено** |
| F-12 | `Header/@ID` входит в документный скоуп `declaredIds` | REVIEW-A §2.3 | `Ticket.scala:74-76`: `auditPool…origin.id` включён в `declaredIds`; Table 7.3: область уникальности — мессенджинговая | ✅ **Подтверждено** |
| F-13 | `IdAllocator`/`WithIds`/`IdSource` не используются | REVIEW-A §3.2 | `grep -r "IdAllocator\|WithIds\|IdSource" modules/` — только объявление в `IdSource.scala`, ни одного вызова | ✅ **Подтверждено** |
| F-14 | `AmountRange.meet`/`join` — семантика не соответствует документации | REVIEW-A §3.3 | `Quantity.scala:420-434`: `meet.amount` = `stricterMin` (берёт **большее**), «ужесточение» должно уменьшать обещание | ✅ **Подтверждено** |
| F-15 | `PartAmount.parts` — один `Part`, в спеке `Part*` | REVIEW-B §2.5 | `Resource.scala`: поле `part: Part = Part.empty` (единственный), спек Table 6.3: `Part*` (множественный) | ✅ **Подтверждено** |
| F-16 | `checkPartAmountKeys` слабее §6.1.2.1 | REVIEW-B §2.5 | `Validation.scala:179-181`: `r.parts.size match { case 1 => …; case _ => Nil }` — учитывает только ровно один родительский Part | ✅ **Подтверждено** |
| F-17 | `checkResourceSetKeys` слабее §3.4 | REVIEW-B §2.4 | `Validation.scala:90-99`: `groupBy(_.key)` — точное равенство, не ловит пересечение CPI | ✅ **Подтверждено** |
| F-18 | `isLawful` не вызываются валидатором | REVIEW-B §2.6 | `BindingIntent.isLawful`, `VariableIntent.isLawful` и др. определены, но `TicketValidator.checkIntentLawfulness` проверяет только `@Name` | ✅ **Подтверждено** |
| F-19 | `docs/01 §3` называет `matches` preorder (транзитивным) — неверно | REVIEW-B §3.3 | `matches` рефлексивно, симметрично, но НЕ транзитивно (контрпример: `{SheetName=S1}≼{}≼{SheetName=S2}`) | ✅ **Подтверждено** |
| F-20 | `Resource.specific` обязателен, спек допускает `<Resource/>` | REVIEW-B §2.7 | `Resource.scala:222`: `specific: ResourcePayload` (обязателен); Table 6.1: Specific Resource — опциональный (`?`) | ✅ **Подтверждено** |
| F-21 | `XJDF/@Name` отсутствует в модели | REVIEW-B §2.10 | `Ticket.scala:24-42`: поля XJDF — нет `name: Option[XjdfString]`; Table 3.1: `@Name` присутствует | ✅ **Подтверждено** |
| F-22 | `Notification/@ModuleID` отсутствует | REVIEW-B §2.10 | `Common.scala`: `Notification` не имеет `moduleId: Option[NmToken]`; Table 8.49 — есть | ✅ **Подтверждено** |
| F-23 | Циклическая зависимость `Validation → Product → Ticket → Patch → Validation` | DEPENDENCY-REPORT.md | 4 файла в цикле; нарушает принцип ацикличности | ✅ **Подтверждено** |
| F-24 | Нет LICENSE файла | REVIEW-C §4 (P4-3) | `ls` — нет `LICENSE`; блокирует M6 (публикация в Sonatype) | ✅ **Подтверждено** |
| F-25 | `build.log` закоммичен — гигиена VCS нарушена | REVIEW-A §1.1 | `git ls-files build.log` — файл есть; `.gitignore` содержит `*.log`, но на уже отслеживаемый файл не действует | ✅ **Частично** (файл может быть уже исправлен) |
| F-26 | `docs/02` ссылается на `03-cats.md`, а файл — `03-cats-mapping.md` | REVIEW-B §4 | Проверено: файл `docs/03-cats-mapping.md` | ✅ **Подтверждено** |
| F-27 | `docs/01 §1` ссылается на «Part 1 – its-all-about-morphisms» — неверно | REVIEW-B §4 | Такой файл — в Part 3, а не 1 | ✅ **Подтверждено** |

### Спорные/неподтверждённые утверждения

| ID | Утверждение | Источник | Факт-чекинг | Статус |
|---|---|---|---|---|
| F-28 | `IntegerRange` не реализует нисходящие диапазоны | REVIEW-C §5.2 (R-03) | `Quantity.scala:383-390`: текущий код использует `lo = max(0, min(f, size-1))`, `hi = max(0, min(t, size-1))`, направление определяется сравнением `lo <= hi`. Для `-1 0` при size=3: f=2, t=0, lo=2, hi=0 → descending — **работает**. Тест в `AlgebraLaws` (`-1 0` → `"c","b","a"`) должен проходить | ⚠️ **Не подтверждено** — либо код уже исправлен, либо ревью ошибочно |
| F-29 | `meet`/`join` семантика противоречит коду | REVIEW-A §3.3 | `Quantity.scala:420-421`: `meet.amount` = `stricterMin(r.amount, o.amount)` (берёт большее) — действительно, «ужесточение» должно брать **меньшее** для amount. Однако полурешёточные законы выполняются. Документация и код расходятся | ✅ **Подтверждено** (расхождение doc/code) |
| F-30 | Matrix — должен быть `Group`, не только `Monoid` | REVIEW-A §3.4 | `Quantity.scala:155-157`: `Monoid[Matrix]`; `inverse: Option[Matrix]` существует, но `Group` требует `inverse` для всех значений — не выполняется для вырожденных | ✅ **Подтверждено** (оставить `Monoid` + `inverse: Option`) |
| F-31 | `docs/03` утверждает, что `.andThen` не компилируется | REVIEW-B §4 | `docs/03-cats-mapping.md` утверждает обратное; на деле `Validated.andThen` существует в cats 2.13.0 и используется в `dsl` | ✅ **Подтверждено** (ошибка документации) |
| F-32 | `PartBuilder.set` бросает `IllegalArgumentException` | REVIEW-C §5.5 (R-18) | `Partition.scala:422-462`: `set` бросает исключение при несовпадении типа значения ключа — нет `unsafe`-префикса | ✅ **Подтверждено** |

### Пропуски в ревью (что не было отмечено, но найдено при факт-чекинге)

| ID | Находка | Где | Комментарий |
|---|---|---|---|
| F-33 | `docs/03-cats-mapping.md` — файл существует, но ссылка в `docs/02` битая | `docs/02-scala3-features.md` → `03-cats.md` (нет файла) | Не пересекается ни с одним ревью |
| F-34 | `build.log` может отсутствовать в индексе после `git rm` | Статус неясен | В репозитории найден, но `.gitignore` имеет `*.log` |

---

## Приоритезированный план улучшений

Ниже — консолидированный план, объединяющий находки трёх аудитов и предложения
трёх PROPOSAL-документов. Приоритеты:

- **P0:** Блокирует сборку (`sbt compile`) или делает модель неконформной спецификации
- **P1:** Функциональная корректность — красные тесты, неверная валидация, расхождения со спекой
- **P2:** Качество домена и документации — точность алгебр, API, тестирование
- **P3:** Инженерия — CI, гигиена репозитория, слои пакетов
- **P4:** Стратегическое развитие (M2–M6)

---

### P0 — Немедленные блокеры

#### P0-1. `Monoid[ValidatedNec[Issue, Unit]]` для `combineAll` ❗❗

**Проблема (F-07):** `checks.combineAll` (Validation.scala:56) и `kids.combineAll`
(Product.scala:192) не компилируются — cats не выводит `Monoid[Validated[NonEmptyChain[Issue], Unit]]`,
т.к. у `NonEmptyChain` нет `Monoid`.

**Решение (вариант A — рекомендованный):** Дать законный инстанс в `object Issue`:
```scala
given Monoid[ValidatedNec[Issue, Unit]] with
  def empty: ValidatedNec[Issue, Unit] = Validated.Valid(())
  def combine(a: ValidatedNec[Issue, Unit], b: ValidatedNec[Issue, Unit]) =
    Semigroup[ValidatedNec[Issue, Unit]].combine(a, b)
```
Покрыть законом в `AlgebraLaws`.

**Файлы:** `model/Validation.scala`  
**Источники:** REVIEW-C R-01, PROPOSAL-C P0-1

---

#### P0-2. `Part/@ProductPart`: `IdRef` → `NmToken`

**Проблема (F-01):** `ProductPart` типизирован как `IdRef`; спек — `NMTOKEN`
(Table 6.4). Ошибка конформности.

**Решение:**
1. Поле `productPart: Option[NmToken]` (вместо `Option[IdRef]`)
2. `PartitionValue.ProductRef(value: NmToken)` — сохранить именованный case для
   читаемости, но с типом `NmToken`
3. `ValueOf[ProductPart.type] => NmToken`
4. `byProductRef(value: NmToken)` — переименовать в `byProductPart`
5. Удалить `ProductPart` из контекста ID/IDREF

**Файлы:** `model/Partition.scala`  
**Источники:** REVIEW-A §1.2, PROPOSAL-A §2.2

---

#### P0-3. `Part/@Metadata`: `NmToken` → `RegExp`

**Проблема (F-02):** В модели `Option[NmToken]`; спек — `regExp` (Table 6.4).
`NmToken` запрещает пробелы, regex их содержит.

**Решение:**
1. Создать `opaque type RegExp = String` в `prim/` с валидацией через
   `java.util.regex.Pattern.compile`
2. Поле `metadata: Option[RegExp]`
3. `PartitionValue.RegExpValue(value: RegExp)` или аналогичный

**Файлы:** `prim/Tokens.scala` (новый тип), `model/Partition.scala`  
**Источники:** REVIEW-A §1.3, PROPOSAL-A §2.3

---

#### P0-4. Исправить `Bom.toTree` — ложные циклы

**Проблема (F-09):** В `toTree` (Product.scala:151) в `seen` добавляется ID
ребёнка, а не текущего узла. Любая ссылка на продукт с `@ID` объявляется циклом.

**Решение:**
```scala
// Вместо:
kid <- child.flatMap(c => toTree(c, byId, seen + c.id.fold("")(_.value)))
// Добавлять ID текущего узла:
val seenIncludingCurrent = seen ++ product.id.map(_.value)
...
kid <- child.flatMap(c => toTree(c, byId, seenIncludingCurrent))
```

Добавить регрессионные тесты: двухуровневое дерево (happy path), истинный цикл
(negative).

**Файлы:** `model/Product.scala`  
**Источники:** REVIEW-C R-02, PROPOSAL-C P1-1

---

#### P0-5. Восстановить enum-значения XJDF 2.1

**Проблема (F-03, F-04, F-05):**
- `Sides` — нет `Unprinted` (Table A.40)
- `DeviceStatus` — нет `Cleanup`, `Setup` (Table A.15)
- `HardCoverJacket.Glued` — токен `"Glued"`, должно быть `"Glue"` (Table 4.11)

**Решение:**
```scala
enum Sides extends XjdfEnum:
  case OneSided, OneSidedBack, TwoSidedHeadToFoot, TwoSidedHeadToHead, Unprinted

enum DeviceStatus extends XjdfEnum:
  case Cleanup, Idle, NonProductive, Offline, Production, Setup, Stopped

enum HardCoverJacket extends XjdfEnum:
  case Unjacketed, Loose, GlueApplied
  def token: NmToken = this match
    case Unjacketed  => NmToken.unsafe("None")
    case Loose       => NmToken.unsafe("Loose")
    case GlueApplied => NmToken.unsafe("Glue")
```

Завести property-тест: для каждого enum `all.map(_.token.value).toSet == <золотое множество>`.

**Файлы:** `prim/Enums.scala`  
**Источники:** REVIEW-B §2.1-2.3, PROPOSAL-B P-01

---

### P1 — Функциональная корректность

#### P1-1. Исправить ссылки на таблицы в scaladoc (7 файлов)

**Проблема (F-10):** 7 ресурсов ссылаются на номер раздела вместо номера таблицы.

**Решение:** Исправить скаладок:
- `Color.scala:7`: Table 6.14 → Table 6.27
- `Finishing.scala:9` (CuttingParams): Table 6.25 → Table 6.53
- `Finishing.scala:44` (FoldingParams): Table 6.36 → Table 6.74
- `Layout.scala:8`: Table 6.52 → Table 6.95
- `Media.scala:8`: Table 6.57 → Table 6.114
- `NodeInfo.scala:7`: Table 6.59 → Table 6.119
- `Preview.scala:8`: Table 6.66 → Table 6.134

**Файлы:** `resources/{Color,Finishing,Layout,Media,NodeInfo,Preview}.scala`  
**Источники:** REVIEW-A §2.1, PROPOSAL-A §3.1, PROPOSAL-B P-02, PROPOSAL-C P2-2

---

#### P1-2. Решить судьбу `ChangeOrder`/`Partial`

**Проблема (F-06):** `type ChangeOrder = XJDF & Partial` — вырожденное пересечение,
т.к. `XJDF extends Partial`. Фича не несёт типобезопасности.

**Решение (вариант B — рекомендованный PROPOSAL-B):**
1. Убрать `extends Partial` из `XJDF`
2. Сделать `ChangeOrder` отдельным case class-ом с релаксированной кардинальностью
3. `ChangeOrder.toPatch` — мост к существующему `Patch`-моноиду
4. Intersection-types использовать честно: `type TicketInChange = XJDF & Partial`

**Файлы:** `model/Ticket.scala`, `model/Patch.scala`  
**Источники:** REVIEW-A §3.1, REVIEW-B §3.1, PROPOSAL-B P-04

---

#### P1-3. Ужесточить `checkResourceSetKeys` по §3.4

**Проблема (F-17):** Валидатор ловит только точное равенство `ResourceSetKey`;
§3.4 запрещает также пересечения `@CombinedProcessIndex` и смеси «без CPI + с CPI».

**Решение:** Заменить `groupBy(_.key)` на явную проверку пар:
```scala
def clashesWith(a: ResourceSet, b: ResourceSet): Boolean =
  a.name == b.name && a.usage == b.usage && a.processUsage == b.processUsage &&
    (a.combinedProcessIndex.isEmpty || b.combinedProcessIndex.isEmpty ||
      a.combinedProcessIndex.toChain.toList.exists(i =>
        b.combinedProcessIndex.toChain.toList.contains(i)))
```

**Файлы:** `model/Validation.scala`, `model/Resource.scala`  
**Источники:** REVIEW-B §2.4, PROPOSAL-B P-05, PROPOSAL-C P2-4

---

#### P1-4. `PartAmount.parts: Chain[Part]` + полный §6.1.2.1

**Проблема (F-15, F-16):** Table 6.3: `Part*`; модель — единственный `Part`.
Проверка `checkPartAmountKeys` учитывает родительские Part только при `case 1`.

**Решение:**
1. Поле `parts: Chain[Part] = Chain.empty` (вместо одного `Part`)
2. Полная проверка по всем родительским Part
3. Второе предложение Table 6.3: если ключ совпадает с родительским, значение
   SHALL быть одним из значений родителя

**Файлы:** `model/Amounts.scala`, `model/Resource.scala`, `model/Validation.scala`  
**Источники:** REVIEW-B §2.5, PROPOSAL-B P-06

---

#### P1-5. Подключить `isLawful` к `TicketValidator`

**Проблема (F-18):** `BindingIntent.isLawful`, `VariableIntent.isLawful` и др.
определены, но не вызываются. Валидатор проверяет только `@Name`.

**Решение:** Ввести `trait Lawful { def localIssues: Chain[Issue] }` и единый
обход дерева тикета. Все доменные законы подключаются через `Lawful.audit`.

**Файлы:** `model/Validation.scala`, `model/Intent.scala`, `prim/Common.scala`  
**Источники:** REVIEW-B §2.6, PROPOSAL-B P-07

---

#### P1-6. `Resource.specific: Option[ResourcePayload]`

**Проблема (F-20):** Table 6.1 допускает `<Resource/>` без specific payload.
Модель требует `ResourcePayload`.

**Решение:**
- `specific: Option[ResourcePayload] = None`
- Обновить `SpecExamples.combinedProcesses` для literal Example 3.6

**Файлы:** `model/Resource.scala`, `examples/SpecExamples.scala`  
**Источники:** REVIEW-B §2.7, PROPOSAL-B P-08

---

#### P1-7. README/docs: починить некомпилируемые сниппеты и битые ссылки

**Проблема (F-08, F-26, F-27):**
- README: `.flatMap(_.build)` — не компилируется
- `docs/02` → `"03-cats.md"` — файл `03-cats-mapping.md`
- `docs/01 §1` → «Part 1 – its-all-about-morphisms» — файл в Part 3
- `docs/03`: утверждает что `.andThen` не компилируется — неверно

**Решение:**
- README: `.andThen(_.build)`
- `docs/03`: исправить тезис
- Проверить все ссылки в `docs/*`

**Файлы:** `README.md`, `docs/*.md`  
**Источники:** REVIEW-B §4, PROPOSAL-B P-02

---

#### P1-8. `NamedColor` → открытый тип + `Catalog`

**Проблема (F-11):** Закрытый enum из 16 значений не может выразить
`Pantone 123 C`.

**Решение:** `NamedColor` → `NmToken` + `Catalog.NamedColor` (рекомендуемые
значения), по аналогии с `ContactType`, `PrintingTechnology`.

**Файлы:** `prim/Enums.scala`, `prim/Common.scala` (Catalog)  
**Источники:** REVIEW-A §2.2, PROPOSAL-A §3.5

---

#### P1-9. `Header/@ID` — исключить из документного ID-скоупа

**Проблема (F-12):** `declaredIds` включает `auditPool…origin.id`;
область уникальности `Header/@ID` — мессенджинговая (Table 7.3).

**Решение:**
1. Убрать `origin.id` из `declaredIds`
2. Сделать `references` полным (IDREF из `AuditResource/ResourceInfo`)
3. Тест: два аудита с одинаковым `Header/@ID`, разным `@Time` — валидны

**Файлы:** `model/Ticket.scala`, `model/Audit.scala`, `model/Validation.scala`  
**Источники:** REVIEW-A §2.3, PROPOSAL-A §3.6

---

#### P1-10. `AmountRange.meet`/`join` — согласовать семантику

**Проблема (F-14, F-29):** `meet.amount` = `stricterMin` (берёт **большее**),
документация обещает «меньше обещанное количество». `join` заявлен как
«оптимистичное расширение», но код сужает интервал.

**Решение:**
1. Определить семантику по `@MinAmount`/`@MaxAmount` (§6.1.2)
2. Исправить направления
3. Удалить `join` (не используется, семантика сомнительна) или переименовать в
   `widen` с собственным законом

**Файлы:** `prim/Quantity.scala`  
**Источники:** REVIEW-A §3.3, PROPOSAL-A §3.4

---

#### P1-11. `docs/01 §3` — исправить «транзитивность» на «отношение толерантности»

**Проблема (F-19):** `matches` — рефлексивно + симметрично, но не транзитивно.
Названо preorder, что неверно.

**Решение:** Исправить формулировку в `docs/01-category-theory-view.md`:
отношение совместимости (tolerance relation). Добавить закон-мост
`a.matches(b) == a.conflictingKeys(b).isEmpty`.

**Файлы:** `docs/01-category-theory-view.md`, `modules/laws/...`  
**Источники:** REVIEW-B §3.3, PROPOSAL-B P-10

---

#### P1-12. `XJDF/@Name`, `Notification/@ModuleID` и мелкие поля

**Проблема (F-21, F-22):** Пропущены поля из Table 3.1 и Table 8.49.

**Решение:**
- `XJDF` += `name: Option[XjdfString]`
- `Notification` += `moduleId: Option[NmToken]` + правило Milestone ⇒ Event

**Файлы:** `model/Ticket.scala`, `prim/Common.scala`  
**Источники:** REVIEW-B §2.10, PROPOSAL-B P-09

---

### P2 — Качество домена и точность

#### P2-1. Усилить алгебраические типы

- `Matrix`: оставить `Monoid` + `inverse: Option[Matrix]`, задокументировать
  причину (F-30)
- `XYPair`, `Points`, `TimeSpan`: `CommutativeMonoid` вместо `Monoid`
- Добавить discipline-подобные law-тесты

**Файлы:** `prim/Quantity.scala`, `prim/Time.scala`, `modules/laws/...`  
**Источники:** REVIEW-A §3.4, PROPOSAL-A §4.1

---

#### P2-2. Закон согласованности `Part` (keys ↔ valueOf ↔ combine)

`Part` имеет 27 полей и 5 параллельных мест перечисления (`keys`, `valueOf`,
`combine`, `PartBuilder.set`, `ValueOf`). Добавить инвариантный property-тест:
- `keys` согласован с `valueOf`
- `combine` право-смещён по каждому ключу

**Файлы:** `modules/laws/PartitionLaws.scala`  
**Источники:** PROPOSAL-A §4.3

---

#### P2-3. Подключить `IdAllocator` или вынести в роадмап

**Проблема (F-13):** `IdSource.scala` — мёртвый код.

**Решение:** Либо подключить к DSL (авто-генерация ID при `id = None` через
контекст `IdAllocator`), либо перенести в M5 и убрать из списка «реализовано».

**Файлы:** `model/IdSource.scala`, `dsl/XjdfDsl.scala`  
**Источники:** REVIEW-A §3.2, PROPOSAL-A §3.3

---

#### P2-4. Golden-тесты примеров

Зафиксировать ожидаемый вывод примеров (3.1, 3.3, 3.4, 3.6, 5.2, brochure)
как golden-файлы; тест сравнивает `Show`-рендер с эталоном.

**Файлы:** `modules/examples/src/test/` (новый модуль)  
**Источники:** PROPOSAL-A §4.4, PROPOSAL-B P-12

---

#### P2-5. `PartBuilder.set`: исключения → `Either`

**Проблема (F-32):** `set` бросает `IllegalArgumentException`, не следуя
собственному принципу 5 (`docs/04`).

**Решение:** `unsafe`-префикс или возврат `Either[String, Part]`.

**Файлы:** `model/Partition.scala` (PartBuilder)  
**Источники:** REVIEW-C R-18, PROPOSAL-C P3-4

---

#### P2-6. `DropItem` — добавить недостающие поля

Table 6.55: `TotalDimensions?`, `TotalVolume?`, `TotalWeight?`.

**Файлы:** `prim/Common.scala` (DropItem)  
**Источники:** REVIEW-C R-11, PROPOSAL-C P2-6

---

#### P2-7. Целостность BOM в `TicketValidator`

Включить проверку `Bom.fromProductList` в `validate` (сейчас невалидный по
ссылкам ProductList проходит валидацию).

**Файлы:** `model/Validation.scala`, `model/Product.scala`  
**Источники:** REVIEW-C R-21, PROPOSAL-C P3-6

---

### P3 — Инженерия

#### P3-1. CI (GitHub Actions)

Добавить `.github/workflows/ci.yml`:
```yaml
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4 (Temurin 21)
      - uses: sbt/setup-sbt@v1
      - run: sbt scalafmtCheckAll compile test examples/run
```

**Файлы:** `.github/workflows/ci.yml` (новый)  
**Источники:** PROPOSAL-A §5.2, PROPOSAL-C P4-1

---

#### P3-2. Гигиена VCS

- `git rm --cached build.log` (если ещё в индексе)
- Коммиты по конвенции `M<n>: …`
- Добавить `LICENSE` (Apache-2.0)

**Источники:** REVIEW-A §5, REVIEW-C §4, PROPOSAL-C P4-2, P4-3

---

#### P3-3. Разорвать циклическую зависимость

**Проблема (F-23):** `Validation → Product → Ticket → Patch → Validation`.

**Решение:** Вынести интерфейсы (trait) в общий пакет или через dependency
inversion.

**Файлы:** `model/{Validation,Product,Ticket,Patch}.scala`  
**Источники:** DEPENDENCY-REPORT.md

---

#### P3-4. Переместить элементы из `prim/Common.scala` в `model/`

`Comment`, `GeneralID`, `Event`, `Milestone`, `Dependent`, `FileSpec`,
`Disposition` — элементы глав 3/8, не «примитивы».

**Файлы:** `prim/Common.scala` → `model/Elements.scala`  
**Источники:** PROPOSAL-A §5.5

---

#### P3-5. `PartitionKey.OptionKey` → спецификационное имя в `Show`

`Show[Part]` печатает `OptionKey=…`, в спецификации — `Option=…`. Добавить
`PartitionKey.attributeName`.

**Файлы:** `model/Partition.scala`  
**Источники:** REVIEW-B R-07, PROPOSAL-C P2-3

---

#### P3-6. ADR-каталог

Завести `docs/adr/`:
- `0001-changeorder-design`
- `0002-spec-deviations`
- `0003-lawful-validation-bus`
- `0004-discipline-laws`

**Источники:** PROPOSAL-B P-15

---

### P4 — Стратегическое развитие (M2–M6)

#### P4-1. Генератор «таблица → тип» для M3

Парсер `./reference/xjdf/*.md`: секции → таблицы → case class с полями.

**Источники:** PROPOSAL-A §5.3

#### P4-2. Архитектура кодеков (M2)

- Модули `codec-core`, `codec-xml`, `codec-json`
- Typeclasses `Encoder`/`Decoder` с round-trip законами
- `Show`-инстансы как сериализация атомарных типов

**Источники:** PROPOSAL-A §5.4

#### P4-3. Стек-безопасный `Bom.cata` на `Eval`

Глубокие BOM (500+ уровней) — `StackOverflowError`. Использовать `Eval`:
```scala
def cataM[A](algebra: ProductTree[A] => Eval[A])(tree: Tree): Eval[A]
```

**Источники:** PROPOSAL-B P-14

#### P4-4. Покрытие тестами: discipline-munit

cats-laws + discipline-munit дают готовые `SemigroupTests`, `MonoidTests` и т.д.

**Источники:** PROPOSAL-A §4.2, PROPOSAL-B P-13

---

## График внедрения (рекомендуемый порядок PR)

### Итерация M1a (P0 — блокеры)
1. **PR-A1:** P0-1 (Monoid для ValidatedNec) — `sbt compile` зелёный
2. **PR-A2:** P0-2 + P0-3 (ProductPart → NmToken, Metadata → RegExp)
3. **PR-A3:** P0-4 (Bom.toTree — ложные циклы)
4. **PR-A4:** P0-5 (enum-значения — Sides, DeviceStatus, HardCoverJacket)
5. **PR-A5:** P0 проверка: `sbt test` зелёный, обновление build.log/CI

### Итерация M1b (P1 — корректность)
6. **PR-B1:** P1-1 (7 ссылок на таблицы) + P1-11 (docs/01 matches)
7. **PR-B2:** P1-2 (ChangeOrder) + P1-7 (README/docs)
8. **PR-B3:** P1-3 + P1-4 (checkResourceSetKeys, PartAmount)
9. **PR-B4:** P1-5 (Lawful-шина) + P1-12 (мелкие поля)
10. **PR-B5:** P1-6 (Resource.specific: Option) + P1-8 (NamedColor)
11. **PR-B6:** P1-9 (ID-скоуп) + P1-10 (AmountRange)

### Итерация M1c (P2 — качество)
12. **PR-C1:** P2-1 (CommutativeMonoid, Group-документация)
13. **PR-C2:** P2-2 (закон Part) + P2-5 (PartBuilder)
14. **PR-C3:** P2-3 (IdAllocator) + P2-7 (BOM-целостность)
15. **PR-C4:** P2-4 (golden-тесты) + P2-6 (DropItem)
16. **PR-C5:** P3-5 (OptionKey → Option в Show) + P3-4 (prim/model)

### Итерация M1d (P3 — инженерия)
17. **PR-D1:** P3-1 (CI) + P3-2 (гигиена VCS + LICENSE)
18. **PR-D2:** P3-3 (циклическая зависимость) + P3-6 (ADR)

---

## Критерии приёмки M1

1. **`sbt clean test` зелёный** (полный прогон, не `testQuick`)
2. **`sbt examples/run`** выполняется без ошибок
3. **`scalafmtCheckAll`** чистый
4. В кодовой базе нет:
    - `& Partial` (ChangeOrder — отдельный тип)
    - `ProductRef(IdRef)` — только `NmToken`
    - `Metadata: Option[NmToken]` — только `Option[RegExp]`
5. Каждая ссылка на таблицу в scaladoc проверена и верна
6. Каждый cats-инстанс имеет property-тест
7. Все `isLawful` вызваны из `TicketValidator`
8. README-сниппет компилируется; docs-ссылки не битые
9. Добавлен LICENSE (Apache-2.0)
10. CI-конфигурация в репозитории

---

## Риски

| Риск | Вероятность | Мitigation |
|---|---|---|
| Проблемы резолва cats 2.13.0 / munit 1.3.0 на Scala 3.8.4 | Средняя | Проверить первой сборкой; при неудаче — откат на проверенные версии |
| `Monoid[ValidatedNec]` инстанс может конфликтовать с cats-provided | Низкая | Явный `given` в `object Issue` — более специфичный чем generic |
| Рефакторинг ChangeOrder затронет DSL и examples | Средняя | PR-B2 изолирован; демо остаётся через Patch |
| Генератор RegExp-валидации может быть costly | Низкая | Валидация только при конструировании; `unsafe` без проверки |
| Нет доступа к JVM/sbt для верификации | Высокая | CI (P3-1) — единственный способ; до CI — ручная проверка через `git push` |

---

## Приложение A. Матрица покрытия REVIEW → PLAN

| REVIEW находка | PLAN пункт | Приоритет |
|---|---|---|
| REVIEW-A §1.1 (красный build.log) | P3-2 | P3 |
| REVIEW-A §1.2 (ProductPart) | P0-2 | **P0** |
| REVIEW-A §1.3 (Metadata) | P0-3 | **P0** |
| REVIEW-A §2.1 (ссылки на таблицы) | P1-1 | P1 |
| REVIEW-A §2.2 (NamedColor) | P1-8 | P1 |
| REVIEW-A §2.3 (ID-скоуп) | P1-9 | P1 |
| REVIEW-A §3.1 (ChangeOrder) | P1-2 | P1 |
| REVIEW-A §3.2 (IdAllocator) | P2-3 | P2 |
| REVIEW-A §3.3 (AmountRange) | P1-10 | P1 |
| REVIEW-A §3.4 (типы алгебр) | P2-1 | P2 |
| REVIEW-B §2.1-2.3 (enum) | P0-5 | **P0** |
| REVIEW-B §2.4 (§3.4 CPI) | P1-3 | P1 |
| REVIEW-B §2.5 (PartAmount) | P1-4 | P1 |
| REVIEW-B §2.6 (isLawful) | P1-5 | P1 |
| REVIEW-B §2.7 (Resource.specific) | P1-6 | P1 |
| REVIEW-B §2.10 (поля) | P1-12 | P1 |
| REVIEW-B §3.1-3.2 (ChangeOrder+merge) | P1-2 | P1 |
| REVIEW-B §3.3 (matches/preorder) | P1-11 | P1 |
| REVIEW-B §4 (docs) | P1-7 | P1 |
| REVIEW-C R-01 (Monoid) | P0-1 | **P0** |
| REVIEW-C R-02 (Bom.toTree) | P0-4 | **P0** |
| REVIEW-C R-03 (IntegerRange) | ✅ не подтверждён | — |
| REVIEW-C R-06 (ссылки на таблицы) | P1-1 | P1 |
| REVIEW-C R-07 (OptionKey Show) | P3-5 | P3 |
| REVIEW-C R-08 (§3.4) | P1-3 | P1 |
| REVIEW-C R-09 (§6.1.2.1) | P1-4 | P1 |
| REVIEW-C R-10 (README) | P1-7 | P1 |
| REVIEW-C R-11 (DropItem) | P2-6 | P2 |
| REVIEW-C R-12 (Metadata) | P0-3 | **P0** |
| REVIEW-C R-15 (ChangeOrder) | P1-2 | P1 |
| REVIEW-C R-16 (matches) | P1-11 | P1 |
| REVIEW-C R-17 (TicketDraft API) | отложено | P3 |
| REVIEW-C R-18 (PartBuilder) | P2-5 | P2 |
| REVIEW-C R-19 (IdAllocator) | P2-3 | P2 |
| REVIEW-C R-21 (BOM-целостность) | P2-7 | P2 |
| DEPENDENCY-REPORT (циклы) | P3-3 | P3 |