# REVIEW — аудит xjdf4s M0 «Каркас домена»

Аудитор: сеньор-архитектор Scala 3 / FP. Дата: 2026-08-15.
Объект: состояние на `main` (`996b756`, единственный содержательный коммит).
Метод: статический аудит без компиляции (в окружении нет JVM) — чтение всего
дерева исходников + сверка каждого спорного места с `./reference/xjdf/*`,
`./reference/cats/*` (включая исходник `cats.data.Validated` v2.13.0),
`./reference/scala/*`, `./reference/sbt/*`.

Шкала: 🔴 исправить в M1 (фактическая ошибка) · 🟠 design/семантика · 🟡
мелочь/документация · ✅ проверено, всё верно.

---

## 0. Общий вердикт

Работа **хорошая и во многом образцовая**: таблицы глав 3/4/6/7/8 перенесены с
высокой точностью, примеры спецификации (3.1, 3.4, 3.6, 5.2) воспроизведены
почти буквально, cats применён по делу, а не «для галочки», риск match type
честно задокументирован и снят типизированными полями. Но есть: **два
фактических несовпадения enum со спецификацией (🔴)**, **вырожденный
intersection `ChangeOrder`**, **неподключённые к валидатору «законы»**,
**неточная реализация §3.4**, методологическая проблема с `build.log` и коммит-
гигиеной, и одна **категориальная ошибка** в `docs/01` (§3 — см. п. 15).

Детали ниже; каждая находка снабжена ссылкой на артефакт и на спецификацию.

---

## 1. Сборка и воспроизводимость

### 1.1 🟠 `build.log` в VCS фиксирует КРАСНЫЙ прогон; зелёного следа нет

`build.log` (закоммичен, при этом `*.log` есть в `.gitignore` — файл попал в
индекс в обход ignore) содержит падение:

```
xjdf4s.laws.PartitionLaws.right-biased overlay always matches the right side
Falsified after 0 passed tests.
ARG_0: Part(...,Some((3,3)),...,Some(Y),...,Some(nB),...,Some(u),Some(Back),...)
ARG_1: Part(...,Some((-10,-10)),...,Some(R),...,Some(o),...,Some(N),Some(Back),...)
```

Верификация по коду (текущий `Partition.scala`): overlay правосторонний
(`b.field.orElse(a.field)`), поэтому на этих данных `combine(left, right)`
поле-в-поле равен `right`, и `matches(right)` по построению истинно — ключи
`combined` ⊆ ключи `right` c теми же значениями. **Свойство с текущим кодом
упасть не может** — лог остался от pre-fix итерации (на момент прогона overlay,
судя по контрпримеру, был левосторонним). Это согласуется с нарративом
ROADMAP M1 (четыре итерации фиксов), но:

- промежуточная история не видна в git (см. п. 1.3);
- пользователю-компилятору предъявлен только красный evidence.

**Действие:** удалить `build.log` из индекса (`git rm --cached build.log`),
прогнать suite, при случае приложить зелёный лог в CI, а не в VCS.

### 1.2 ✅ `build.sbt` и `project/build.properties` соответствуют sbt 2.0.2

- `sbt.version=2.0.2` ✅; без плагинов ✅; `publish / skip := true` задан
  **scoped** (внутри `root.settings`) — безопасно: предупреждение
  `reference/sbt/docs/changes/migrating-from-sbt-1.x.md` («all subprojects will
  be skipped!») относится только к *bare settings* sbt 2.x, здесь не тот случай ✅.
- Замечание: миграционный гид рекомендует *bare common settings* вместо
  `ThisBuild / …` — `ThisBuild` легален и работает, но при M6 (публикация)
  стоит перейти на рекомендованный стиль. 🟡
- Не верифицируемо из `./reference/*`: существование артефактов `munit 1.3.0` /
  `munit-scalacheck 1.3.0` и `cats-core 2.13.0` под Scala 3.8.4 — по ROADMAP
  «риски» это зона доверия; при следующей сборке проверить резолв. 🟡

### 1.3 🟠 История git не соответствует собственным конвенциям

Весь M0 (54 файла, +7150 строк) приземлился **одним коммитом** с сообщением
«Reformat code using ScalaFmt» — это не reformat, а вся имплементация.
ROADMAP §4 требует «один PR = один пункт милстоуна, сообщения `M<n>: …`» и
нарративно описывает итерации M1/M1b/M1c/M1d — в истории их нет. Аудит
эволюции решений (например, разворот overlay в `Part`) невозможен.
**Действие:** с M1 — атомарные коммиты согласно конвенции; в описании PR —
ссылки на таблицы (удачное правило, его стоит соблюдать самим).

---

## 2. Соответствие спецификации XJDF 2.2

Проверено по тексту `./reference/xjdf/*` (таблицы и разделы — по строкам).

### 2.1 🔴 `Sides` — нет значения `Unprinted`

`Appendix A`, Table A.40: `OneSided, OneSidedBack, TwoSidedHeadToFoot,
TwoSidedHeadToHead, Unprinted` *(New in XJDF 2.1)*. В
`prim/Enums.scala` `Sides` содержит только 4 значения — `Unprinted` отсутствует.
Для модели, декларирующей XJDF 2.2 и «значения ровно из таблиц», это дефект.

### 2.2 🔴 `DeviceStatus` — нет значений `Cleanup` и `Setup`

`Appendix A`, Table A.15: `Cleanup` *(New in XJDF 2.1)*, `Idle`, `NonProductive`,
`Offline`, `Production`, `Setup` *(New in XJDF 2.1)*, `Stopped`. В модели —
только 5 значений, `Cleanup` и `Setup` отсутствуют. ROADMAP M1d упоминает
замену `DeviceStatus.Setup → Idle` в ходе починки сборки — похоже, кейс удалили
вместо починки конфликта. Восстановить оба значения; конфликт, если он был,
решается явной ссылкой (`DeviceStatus.Setup`), а не удалением члена enum.

### 2.3 🟠 Токен `HardCoverJacket`: модель `"Glued"`, спецификация — `"Glue"`

Таблица 4.11 (`chapter 4`): «Allowed values are: **None** … **Loose** …
**Glue** – The jacket is glued to the spine». В `Enums.scala`
`case Glued` c токеном `this.toString = "Glued"`. Токен неверен — в M2
(кодеки) это станет сериализационным багом. Аналогично сверить остальные
«переименованные» токены (`BindingType.NoBinding → "None"` ✅ верно,
`BindingOrder.Unbound → "None"` ✅ верно, `Coating.Uncoated → "None"` — сверить
с A.2.10 при M1).

### 2.4 🟠 `checkResourceSetKeys` реализует §3.4 уже, чем спецификация

§3.4: «ResourceSet elements with the same values of @Name, @Usage, @ProcessUsage
and **common or no entries** in @CombinedProcessIndex SHALL NOT be specified».
Модель (`ResourceSetKey` + `groupBy`) ловит только **точное равенство** ключей.
Не ловит (а спека запрещает): `NodeInfo` без CPI рядом с `NodeInfo@CPI="0"`,
и `CPI=[0,1]` рядом с `CPI=[1]`. Реализация: сравнение
`(name, usage, processUsage)` + пересечение цепочек CPI
(`a.isEmpty || b.isEmpty || пересечение непусто`).

### 2.5 🟠 `PartAmount.parts`: спека — `Part*`, модель — единственный `Part`

Table 6.3: `Part* | element`. Модель: `part: Part = Part.empty`. Из-за этого же
`checkPartAmountKeys` (§6.1.2.1) неполон вдвойне:
1. учитываются только parent-ресурсы ровно с одним `Part` (`case 1 => …`);
2. реализована только первая половина правила («SHALL NOT include keys already
   uniquely specified in any parent Resource/Part»); вторая половина — «value
   SHALL match **one of the values** from the parent Resource/Part» при
   совпадающем ключе — не реализована.

### 2.6 🟠 «Мёртвые законы»: инварианты не подключены к `TicketValidator`

Методы `isLawful` есть, но никто их не вызывает:
- `BindingIntent.isLawful` (парность details ↔ @BindingType, Table 4.8) —
  `TicketValidator.checkIntentLawfulness` проверяет лишь `@Name == имя элемента`;
- `VariableIntent.isLawful` (min ≤ avg ≤ max, §4.14);
- `Disposition.isLawful` (@MinDuration ⟂ @Until, Table 8.23);
- `PartWaste.isLawful` (хотя бы одно из @ModuleIDs/@WasteDetails, Table 6.5);
- правило Notification: `Milestone present ⇒ @Class="Event"` (Table 8.49) —
  не смоделировано совсем.

Домен тем самым наполовину анемичен: законы описаны, но тикет, нарушающий
парность BindingIntent, пройдёт валидацию. Подключить к `XJDF.validate` +
протесты на каждый (заявленное в ROADMAP DoD «каждая структура — с
тестом» этим структурам не выполнено).

### 2.7 🟡 `Resource.specific` обязателен, а спека допускает `<Resource/>`

Table 6.1: «Specific Resource **?**» — опционален; Example 3.6 спеки пользуется
(`<Resource/>` без тела). Модель требует payload ⇒ `SpecExamples.combinedProcesses`
вынужден эмулировать пример пустыми ресурс-сетами вместо `<Resource/>`. Это
отклонение от примера — зафиксировать и решить (например, `Option[ResourcePayload]`)
до кодеков M2, где расхождение станет видимым в XML.

### 2.8 🟡 Номера таблиц: `Media` — «Table 6.114» (§6.57), `Device` — «Table 6.57» (§6.28)

В `resources/Device.scala` scaladoc верный по номеру таблицы; в
`resources/Media.scala` указана «Table 6.57» — это таблица Device; у Media
таблица 6.114 (§6.57). Собственная конвенция «ссылка на таблицу» требует
точности; рекомендую формат `§x.y / Table z` — спека их нумерует раздельно.

### 2.9 🟡 Table 6.4: расхождения типов/имён ключей

- `Metadata` — в спеке `regExp`, в модели `NmToken` (нет типа regExp) — ок как
  упрощение M0, но не задокументировано.
- `ProductPart` — в спеке `NMTOKEN` (ссылка на `Product/@ID`), **Deprecated в
  XJDF 2.1**; в модели `IdRef`. Семантически красивее, но это сознательное
  отклонение от DATA TYPE колонки — задокументировать в scaladoc (сейчас везде
  заявлена верность Table 6.4). Парный ключ `Product` (New in 2.1, ссылка на
  `@ExternalID`) в модели — `NmToken` ✅.
- `OptionKey` — в спеке ключ называется `Option`; переименование понятно
  (коллизия со stdlib), но `Show[PartitionKey] = fromToString` напечатает
  `OptionKey` — не токен спеки. К M2 нужен отдельный `specToken`-маппинг
  (и вообще у `PartitionKey` нет `XjdfEnum`-инфраструктуры).

### 2.10 🟡 Мелкие пропуски полей из проверенных таблиц

- `XJDF/@Name` (Table 3.1) отсутствует в модели.
- `Notification/@ModuleID` (Table 8.49) отсутствует.
- `Comment/@Language`-множественность («несколько Comment SHALL различаться
  Language», Table 8.49-контекст) — не проверяется (ок для M0, внести в M1).
- `Product/@PartVersion`-правило («root products SHALL repeat child
  PartVersion», Table 3.11 sh.2) — не проверяется.

### 2.11 ✅ Что проверено и верно (выборка)

- Table 3.1 (кроме `@Name`), Table 3.3 (5 аудитов + @Name JSON Exception
  учтён в M2), Table 3.7 ProcessRun (все поля, EndStatus={Aborted,Completed} ✅),
  Table 3.10/3.11 Product/ProductList ✅, Table 3.12 ResourceSet (все поля) ✅,
  Table 7.3 Header (включая `refID: NMTOKEN` с обоснованием ✅), Table 3.2
  выравнивание (AuditProcessRun ↔ CommandReturnQueueEntry корректно
  отсутствует в SignalPayload ✅).
- Примеры 3.1/3.4/3.6/5.2 — структурно буквальные (вкл. JobID `splitDelivery`,
  Header-параметры примера 3.3 воспроизведены точно: `Writer/V_2.0/TestSender/
  2020-03-01T19:55:57+01:00` ✅).
- §5.3.4.1 формулы Severity ✅; §1.10.2 подсчёт (включая `"-1 0"` reverse) ✅ с
  property-тестами; §6.1.3.2 (first-match + multi-select + IDREF-обход Part) и
  §6.1.3.3 (Part* = дизъюнкция) ✅; Table 6.1 `@Status` ⟂ `@Usage="Output"` ✅;
  Table 4.8 парности BindingIntent ✅ (на уровне `isLawful`); Table 4.36
  `@ChildRefs` ✅; A.2.37 Severity (5 значений) ✅ — переименование в
  `SeverityClass` оправдано коллизией с `@Severity: Int [0..100]`;
  27 ключей Table 6.4 в порядке таблицы ✅.

---

## 3. Категориальный слой

### 3.1 🟠 `ChangeOrder = XJDF & Partial` — вырожденное пересечение

`Ticket.scala`: `XJDF … extends Partial`, затем `type ChangeOrder = XJDF & Partial`.
Поскольку `XJDF <: Partial`, имеем `XJDF <: XJDF & Partial`, т.е. тип-алиас
**ничего не отличает**: любой тикет — уже ChangeOrder (что и фиксирует тест
в `TicketLaws`, где `val changeOrder: ChangeOrder = ticket(...)` проходит
тривиально). Хуже: ни одна сигнатура API не принимает `ChangeOrder` — фича
«intersection types», заявленная в README/ROADMAP как принципиальная, в API не
оказывает никакого эффекта. И семантика §1.3.2/§1.6.5 («change order несёт
только изменённые значения») не выражена: `JobID`/`Types` остаются обязательными.

**Варианты:** (a) убрать `extends Partial` из `XJDF` и принимать
`XJDF & Partial` в API change-order операций (`Patch.from(co: ChangeOrder)`,
`mergeResourceSets(update: …)`) — минимальный вариант; (b) честный отдельный
тип change-order с релаксированной кардинальностью (JobID обязателен, Types
нет, всё остальное Option) — соответствует §1.3.2, дороже. Сейчас — ни то, ни
другое (и `docs/02` утверждает обратное).

### 3.2 🟡 `Patch.mergeResourceSets` — документация/контракт расходятся с кодом

Scaladoc обещает `Left` «слить нельзя» — код `Left` не возвращает никогда; и
«the update wins» — ложно: `ticket.resourceSets ++ update` **добавляет дубликат**
после старого, а `select` (§6.1.3.2, first match) вернёт **старый** ресурс —
«wins» ровно наоборот. Кроме того, результат с дублирующимися ключами
ResourceSet сам невалиден по §3.4/2.4. Реализация должна заменять набор по
ключу (с учётом поправки из п. 2.4) или явно заявить иное.

### 3.3 🔴(категориально) `docs/01` §3: `matches` названо preorder/«тонкой категорией» — это не так

Утверждение: «`part.matches(selector)` — отношение порядка … (рефлексивно и
транзитивно — свойства проверяются в laws-модуле)». Проверка: `matches` —
рефлексивно ✅ (есть закон), симметрично (по построению: `a.matches(b) ⟺
b.matches(a) ⟺ conflictingKeys(a,b).isEmpty` — конфликт-фри), но **не
транзитивно**: `a={SheetName=S1} ≼ b={} ≼ c={SheetName=S2}` — оба шага бес-
конфликтны, а `a` против `c` конфликтует. Закона транзитивности в laws,
правильно, нет — он не выполняется. Это не preorder, а **отношение
толерантности** (reflexive + symmetric), т.е. граф совместимости контекстов —
в терминах Милевски корректнее описать как «взвешенный» (by keys) вариант
compatibility relation, чем как тонкую категорию. Поправить формулировку;
если хочется порядка — правильный объект: решётка/частичный порядок по
`conflict-free merge` (`a ≤ b ⟺ b = merge(a,b)`), где допустимость пары =
`mergeWith.isRight`.

### 3.4 🟡 «Свободный моноид» без единицы — терминология

`AuditPool`/`AmountPool`/`NmTokens` — `NonEmptyChain`: без `empty` это
**свободная полугруппа**, «свободный моноид без нейтрального» — оксюморон.
Scaladoc `NmTokens` это сознаёт («free monoid … without the empty word»),
`AuditPool`/`docs/01 §4` — нет. Одна аккуратная оговорка в `docs/01` снимет
претензию (для домена `+`-кардинальность правильная — спека требует ≥1).

### 3.5 🟡 `docs/01` §7 «сопряжение» Intent ⇄ Resource

Подано почти как факт; это **аналогия**, не adjunction (нет функторов и
изоморфизма хом-множеств). Оставить, но явно пометить как эвристику —
документ в остальном аккуратный (морфизм-тикет §1, free category §1.1,
начальная алгебра §2, monoid action §6 — всё по `reference/category-theory`
корректно). В §1 ссылка «Part 1 – its-all-about-morphisms» — такого файла нет,
он в Part 3. 🟡

### 3.6 ✅ F-алгебра BOM

`ProductTree`/`Fix`/`Bom.cata`, монадическая развёртка с детекцией
циклов/висячих ссылок, `totalCopies` по §3.3.1.1 («копии части на одного
родителя» — множитель ✅), покрытие `ValidatedNec`-носителем в
`validateAmounts` — честная категорийная работа. Stack-safety вынесена в M5
осознанно ✅. Ребра BOM собраны из `Product.references` — корректно:
`BindingIntent/@ChildRefs`, `AssemblingIntent/@Container`, `VariableIntent/@ChildRefs`
— все ссылаются на `Product/@ID` (проверено по гл. 4); `MediaIntent`/@… на
ресурсы в `Intent.references` не попадают ✅.

---

## 4. Документация и заявления

- 🟡 **README, «Минимальный пример»**: `dsl.TicketDraft.of(...).flatMap(_.build)`
  — `Validated` не имеет `flatMap` (сам `docs/03` это правильно объясняет!);
  флагманский сниппет не компилируется. Заменить на `andThen` или `chainV`-стиль.
- 🟡 **docs/03**: «ни `.flatMap`/`.andThen` на `Validated` не компилируются» —
  фактически неверно: `Validated.andThen` существует (проверено по сырцу
  cats 2.13.0) и **сам dsl его использует** (`dsl.intent`). Поправить тезис:
  нет `flatMap`/for-comprehensions; `andThen` (right-biased sequencing) есть.
- 🟡 docs/02 ссылается на «03-cats.md» — файл называется `03-cats-mapping.md`.
- 🟡 docs/04: граф зависимостей не показывает ребро `resources → intents`
  (`Finishing.scala` импортирует `Fold/Perforate` из `intents`).
- 🟡 docs/03 Semilattice-таблица: «meet: amount ↓» — код делает `stricterMin`
  по `amount` (берёт **большее**). Определиться с семантикой «ужесточения
  обещанного количества» и синхронизировать код/документ.
- 🟢 ROADMAP: подробный, с DoD, рисками, конвенциями, привязкой к reference;
  расчёты по интентам (8 из 13) и ресурсам (~12 из ~140) сверены с главами 4/6 —
  сходятся. Risk-3 (match type / GADT-утончение) — корректная диагностика
  ограничения компилятора; решение (типизированные поля + `valueOf`) — здравое.

---

## 5. Статические риски компиляции (оценка без JVM)

Скомпилируемость оценивалась чтением с учётом задокументированного в docs/02
«урока прозрачности» opaque-типов. Критических находок нет; отмеченное:

- Все opaque-типы инкапсулируют доступ в companion-ы; перекрёстный доступ —
  через именованные given (`showTimestamp`) ✅; именованные named-tuple поля
  используются только в прозрачных скоупах ✅.
- Перегрузки `apply` (`Comment(String)`, `Header(String, Timestamp)`) рядом с
  синтетическими фабриками — разрешимы благодаря opaque-непрозрачности снаружи
  компаньонов и различной арности ✅ (тонко — одна строка пояснения в коде не
  помешает: это место хрупкое для будущих правок).
- `Monoid[Validated[E,A]]` существует в cats-kernel 2.13 (` Semigroup[E]`,
  `Monoid[A]`) — `checks.combineAll`/`kids.combineAll` ✅ существование подтверждено
  сырцом cats.
- `Show.show:` + match-колонки (colon-lambda) — валидный синтаксис 3.x ✅.
- Named tuples — «Starting in Scala 3.7» по локальному reference; заявление
  ROADMAP о стабильности ✅.
- Остаточный flake-риск: property «matrix: inverse cancels …» для почти
  вырожденных `det≈1e-16` — генератор это практически не производит, но
  формально возможно; допустимо с текущим допуском. 🟡

---

## 6. Приоритеты на M1 (свод действий)

1. 🔴 Вернуть `Sides.Unprinted`; вернуть `DeviceStatus.Cleanup, Setup`;
   исправить токен `HardCoverJacket.Glued → Glue` (+ ниже протоколировать
   «переименованные» кейсы отдельным списком для кодеков M2).
2. 🔴 Исправить формулировку «preorder» → «отношение толерантности /
   конфликт-фри граф» в docs/01 §3 (+ опционально показать порядок по merge).
3. 🟠 Устранить вырождение `ChangeOrder` (вариант (a) минимум) и починить
   `mergeResourceSets` (замена по ключу; честный контракт `Ior`).
4. 🟠 §3.4: дубликаты ResourceSet по «common or no entries» (пересечение CPI).
5. 🟠 Подключить `isLawful`-инварианты (BindingIntent/VariableIntent/
   Disposition/PartWaste/…) к `TicketValidator` + тесты (п. 2.6).
6. 🟠 `PartAmount.parts: Chain[Part]` и полное §6.1.2.1 (вторая половина).
7. 🟡 `README`-пример; тезис про `andThen` в docs/03; ссылки на файлы/Part 3;
   номера таблиц Media/Device; `@Name` у XJDF; `@ModuleID` у Notification;
   `Resource.specific: Option`.
8. 🟡 Гигиена: вычистить `build.log` из индекса; коммиты по конвенции `M<n>`.
9. 🟡 При первой зелёной сборке — зафиксировать лог вне VCS и закрыть риск
   версий (munit 1.3.0-пара, cats на 3.8.4).