# PROPOSAL — предложения по улучшению xjdf4s

> Дополняет `REVIEW-A.md` (аудит) конкретным планом действий. Каждое предложение
> привязано к замечанию из аудита, к файлам и к разделам `./reference/*`, и
> оценено по объёму работ. Приоритеты: **P0** — блокеры конформности и сборки,
> **P1** — качество домена, **P2** — точность алгебр и документации, **P3** —
> стратегия следующих милстоунов.
>
> Стиль фиксации — как в ROADMAP: один PR = один пункт, коммит `M<n>: …`,
> каждый новый cats-инстанс — с property-тестом в `modules/laws`.

---

## 1. Резюме

Проект уже является сильным фундаментом: честный категориальный слой, законы,
осмысленный выбор Scala 3-конструкций. Дальнейшее улучшение — это не
«переписывание», а три последовательных движения:

1. **Закрыть долг конформности** (типы `Part/@ProductPart`, `Part/@Metadata`,
   красный `build.log`, неверные ссылки на таблицы) — без этого M0 нельзя
   считать готовым и нельзя начинать кодеки (M2), т.к. ошибки типа размножатся
   на ~130 ресурсов и XML/JSON-слой.
2. **Убрать «витринные» конструкции** (intersection-type `ChangeOrder`,
   мёртвый `IdAllocator`, `meet`/`join` с противоречивой семантикой) и заменить
   их на то, что реально несёт типобезопасность и используется.
3. **Довести точность алгебр и автоматизацию** (группы/коммутативные моноиды,
   law-слой в стиле discipline, golden-тесты примеров, генератор «таблица → тип»,
   CI) — это то, что делает проект *воспроизводимым*, а не только красивым.

---

## 2. P0 — блокеры (закрыть до признания M0)

### 2.1 Полный зелёный прогон и честный `build.log`

**Проблема.** В репозитории лежит `build.log` с падением `PartitionLaws`
(свойство «right-biased overlay always matches the right side», seed
`OTuTle3wdwIq0xzSV4dazdmMsIQi0cnuxXiQD3-a-KB=`). Лог — это `testQuick`
одного сьюта; остальные сьюты не запускались. Статически свойство в текущем
коде является тавтологией (overlay право-смещённый, `matches` проверяет
«каждый ключ либо отсутствует, либо совпадает»), поэтому либо лог устарел, либо
есть тонкая причина, которую надо воспроизвести.

**Предложение.**

1. Прогнать `sbt clean test` на чистом кэше и зафиксировать вывод целиком
   (`sbt test`), а не фрагмент `testQuick`.
2. Если падение воспроизводится с указанным seed — воспроизвести детерминированно
   (`override def scalaCheckInitialSeed = "…"`) и починить. Наиболее вероятная
   причина — несоответствие между старой и новой семантикой `matches`/`combine`
   (право-смещённый overlay был введён в одной из M1-итераций).
3. Сделать `build.log` **артефактом CI, а не коммитом**: добавить его в
   `.gitignore`, а в репозитории держать только badge/статус последнего прогона.
4. Добавить в `TicketLaws`/`AlignmentLaws`/`AlgebraLaws` по одному smoke-тесту,
   который гарантированно запускается и в `testQuick`, чтобы «No tests to run»
   больше не маскировало непроверенные сьюты (в текущем логе — три «No tests to
   run» подряд).

**Объём.** 0.5–1 день. **Инвариант:** после этого любой PR обязан приносить
полный зелёный `sbt test`.

### 2.2 `Part/@ProductPart`: `IdRef` → `NmToken`

**Проблема.** `reference/xjdf/6 – Resources.md`, Table 6.4:

```
| ProductPart? (Deprecated in XJDF 2.1) | NMTOKEN | References the Product/@ID
  that this Part applies to. Deprecation note: Use @Product to reference
  Product/@ExternalID. |
```

В `model/Partition.scala` `ProductPart` типизирован как `IdRef` (поле,
`PartitionValue.ProductRef`, `ValueOf`, `byProductRef`). Это ошибка конформности:
`@ProductPart` — `NMTOKEN`, он не входит в механизм `ID`/`IDREF` (§2.2.3) и не
должен участвовать в `XJDF.references`.

**Предложение.**

- Поле `productPart: Option[NmToken]`.
- `PartitionValue` для этого ключа — переиспользовать `Token` (убрать
  `ProductRef`), либо оставить именованный case `ProductRef(value: NmToken)`
  ради читаемости, но **с типом `NmToken`**.
- `ValueOf[PartitionKey.ProductPart.type] => NmToken`.
- `byProductRef(value: NmToken)` (переименовать в `byProduct`, симметрично
  `@Product`).
- Удалить `ProductPart` из любого сбора `references` (сейчас он туда не
  попадает, но проверку добавить тестом).

Симметрия с `@Product` (уже корректно `NmToken`, ссылка на `@ExternalID`)
— целевое состояние.

**Объём.** 0.5 дня + тест. **Критерий:** `Part.productPart` имеет тип
`Option[NmToken]`, и ни один law не ожидает `IdRef`.

### 2.3 `Part/@Metadata`: `NmToken` → `RegExp`

**Проблема.** Table 6.4: `| Metadata? | regExp | Metadata SHALL match metadata
extracted from a PDL using RunList/MetadataMap or IdentificationField/
MetadataMap. |`. В `Partition.scala` это `Option[NmToken]`, а `NmToken`
валидирует «нет пробелов» — заведомо ложно для регулярных выражений.

**Предложение.**

- Завести в `prim/` (рядом с `Tokens.scala`) opaque type `RegExp`:
  ```scala
  opaque type RegExp = String
  object RegExp:
    def from(raw: String): Option[RegExp] =
      Option(raw).filter(r => try { java.util.regex.Pattern.compile(r); true }
                              catch { case _: PatternSyntaxException => false })
    def unsafe(raw: String): RegExp = ...
    given Show[RegExp]; given Eq[RegExp]
  ```
  (валидация — компиляция паттерна; без требований к пробелам).
- `metadata: Option[RegExp]`, `PartitionValue` — добавить case
  `RegExpValue(value: RegExp)` (или разрешить `Token`-подобный).
- При переносе `RunList/MetadataMap` и `IdentificationField/MetadataMap` (M3)
  использовать тот же `RegExp`.

**Объём.** 0.5 дня + тест. **Критерий:** тип соответствует Table 6.4.

---

## 3. P1 — качество домена

### 3.1 Починить ссылки на таблицы (7 файлов) и завести «реестр таблиц»

**Проблема.** См. `REVIEW-A.md` §2.1: в scaladoc семи ресурсов номер **раздела**
выдан за номер **таблицы**:

| Файл | Сейчас | Должно быть |
|---|---|---|
| `resources/Color.scala:7` | Table 6.14 | Table 6.27 |
| `resources/Finishing.scala:9` (CuttingParams) | Table 6.25 | Table 6.53 |
| `resources/Finishing.scala:44` (FoldingParams) | Table 6.36 | Table 6.74 |
| `resources/Layout.scala:8` | Table 6.52 | Table 6.95 |
| `resources/Media.scala:8` | Table 6.57 | Table 6.114 |
| `resources/NodeInfo.scala:7` | Table 6.59 | Table 6.119 |
| `resources/Preview.scala:8` | Table 6.66 | Table 6.134 |

**Предложение.**

1. Механически исправить семь scaladoc-строк.
2. Завести в `docs/` (или как генерируемый артефакт M3) **реестр
   «раздел §6.x → таблица 6.N → файл»** — единственный источник сверки.
3. В генератор M3 («таблица → тип») встроить проверку, что каждый тип ссылается
   на таблицу, **существующую** в `./reference/xjdf/6 – Resources.md` (grep по
   `**Table 6.N: …**`). Это превращает единичную ошибку в системную защиту.

**Объём.** 1–2 часа на правку + 0.5 дня на реестр/генератор.

### 3.2 `ChangeOrder`: заменить декоративное пересечение настоящим типом

**Проблема.** `XJDF extends Partial`, поэтому `type ChangeOrder = XJDF & Partial
≡ XJDF`. Тип-пересечение ничего не различает; `Partial` не несёт семантики
«relaxed cardinality» §1.6.5. Это витрина фичи, а не механизм (реальный
change order в демо уже сделан через `Patch`, что верно).

**Предложение (вариант A — минимальный, рекомендую).**

- Убрать `trait Partial` и псевдоним `ChangeOrder = XJDF & Partial`.
- Оставить change order как **значение `Patch`** (эндоморфизм) — это и есть
  корректная модель «изменения»; добавить в `Patch` явный конструктор из
  «частичного» тикета:
  ```scala
  object Patch:
    def fromChangeOrder(change: XJDF, updateMethod: UpdateMethod = …): Patch
  ```
  где поля `XJDF` с `Option` и так позволяют «только изменённые значения».
- Переместить `UpdateMethod`/`@Version`-политику change order (§1.3.2) в
  отдельный параметр, а не маркер.

**Предложение (вариант B — если нужен отдельный тип).**

- `final case class ChangeOrder(fields: ChangeOrderFields)` с явными
  `Option`-полями (реализация §1.6.5 «relaxed cardinality»), отделённый от
  `XJDF`. Плюс: тип реально различает контексты; минус: дублирование ~17 полей
  и синхронизация с `XJDF` (риск дрейфа).

Рекомендую **вариант A**: он сохраняет типобезопасность там, где она есть
(`Patch`, `Ior`-merge), и убирает иллюзию.

**Объём.** 0.5–1 день. **Критерий:** в кодовой базе нет `& Partial`; change
order в демо остаётся `Patch`-моноидом с законами действия.

### 3.3 `IdAllocator`/`WithIds`/`IdSource`: подключить или вынести

**Проблема.** `model/IdSource.scala` (чистый `State`-генератор и context-function
`IdAllocator ?=> A`) нигде не используется. DSL выделяет `@ID` через явный
`id: Option[String]`, а не через генератор. Это мёртвый код, противоречащий
цели «не анемичная модель».

**Предложение (рекомендую подключить).**

- Добавить в `dsl` обёртку авторинга:
  ```scala
  object dsl:
    def inIds[A](body: WithIds[A]): A = IdAllocator.run(body)
    // внутри — свежие ID:
    def freshId(prefix: String): WithIds[Option[Id]] = summon[IdAllocator]...
  ```
- Сделать `dsl.resourceSet`/`dsl.product` способными при `id = None` **брать ID из
  контекста** (`IdAllocator`), а при явном `Some` — использовать заданный.
- Оставить чистый `IdSource.fresh: State[Counter, Id]` как референтную реализацию
  и покрыть её законом: «`freshMany` возвращает уникальные ID».

Если подключать не хочется сейчас — честно перенести `IdSource` в M5
(«живой workflow»), убрать из списка «реализовано» в ROADMAP и README.

**Объём.** 0.5 дня на подключение + тест; либо 15 минут на перенос в роадмап.

### 3.4 `AmountRange.meet`/`join`: согласовать семантику или удалить `join`

**Проблема.** `prim/Quantity.scala`:

- `meet.amount` использует `stricterMin`, которая возвращает **большее**
  значение, хотя doc и `docs/01` обещают «меньше обещанное количество»;
- `join.min` использует `stricterMin` (максимум) — **сужает** интервал, хотя
  `join` заявлен как «оптимистичное расширение».

Законы `Semilattice` это не ловят (каждая координата независимо min/max), а
`join` нигде не используется и не тестируется.

**Предложение.**

1. Определить желаемую семантику по назначению `@MinAmount`/`@MaxAmount`
   (§6.1.2, Table 6.3): «ужесточение» обязательства = поднять нижнюю границу,
   опустить верхнюю. Для точечного `@Amount` «ужесточение» логично = **минимум**
   (берём меньшее обещание), что противоречит текущему коду.
2. Исправить направления, добавив комментарий-таблицу «что растёт, что падает»
   прямо в объекте.
3. **Удалить `join`** (не используется, семантика сомнительна) либо переименовать
   в явное `widen` и покрыть собственным законом (идемпотентность/коммутативность
   уже даёт `Semilattice`; добавить монотонность относительно `includes`).

**Объём.** 0.5 дня. **Критерий:** `meet` согласован с doc, а `join` либо удалён,
либо покрыт законом и примером использования.

### 3.5 `NamedColor`: открытый тип + `Catalog`, либо явная неполнота

**Проблема.** `Appendix A.2.30`: «For a list of allowed values, see
[Color Names]» — внешний, открытый список. Закрытый enum из 16 «common values»
делает невыразимым легальный `MediaIntent(@MediaColor="Pantone 123 C")`, что
противоречит собственному принципу open/closed (§1.10.3.1/2).

**Предложение.**

- `NamedColor` → `NmToken` + `Catalog.NamedColor` (рекомендуемые значения), как
  уже сделано для `ContactType`, `PrintingTechnology` и др.
- Если хочется сохранить enum для типизированных «стандартных» цветов — оставить
  `NamedColor` закрытым, но добавить явный escape (например, поле
  `mediaColorDetails` уже есть) и задокументировать, что enum — только
  «common values».

**Объём.** 0.5 дня. **Критерий:** любой токен цвета выразим; в scaladoc —
ссылка на `[Color Names]`.

### 3.6 Согласованность ID-скоупа `XJDF.declaredIds`/`references`

**Проблема.** `Ticket.scala`: `declaredIds` включает `auditPool…origin.id`
(Header-ы), но область уникальности `Header/@ID` — мессенджинговая (Table 7.3:
«unique for all messages and XJMF initiated by the Sender»), а не документная
(§2.2.3). Это даёт ложные срабатывания `checkIdUniqueness` для легальных
аудитов. При этом `references` не собирает IDREF из вложенных ресурсов
`AuditResource/ResourceInfo` — асимметрия.

**Предложение.**

1. Явно решить: audit-`Header/@ID` **не участвует** в документном ID-скоупе
   (рекомендую), и убрать `origin.id` из `declaredIds`.
2. Сделать `references` полным: пройти по `ResourceInfo.resourceSet` внутри
   аудитов так же, как по обычным `ResourceSet`.
3. Добавить тест: два аудита с одинаковым `Header/@ID`, разным `@Time` —
   тикет валиден; и контрпример — два `Resource/@ID` с одинаковым значением —
   невалиден.

**Объём.** 0.5 дня + 2 теста.

---

## 4. P2 — точность алгебр, laws, тестирование

### 4.1 Усилить типы алгебр

- `Matrix`: фактически **группа** (есть `inverse` при det≠0). Использовать
  `cats.kernel.Group` (у нас уже есть `Semigroup`/`Monoid` в зависимостях
  cats-kernel). Осторожно: `Group` требует `inverse` для **всех** значений, а у
  вырожденных матриц обратной нет. Правильнее либо ввести явный
  `AffineGroup`-инстанс на подмножестве невырожденных (отдельный тип
  `InvertibleMatrix`), либо оставить `Monoid` и добавить **метод** `inverse`.
  Рекомендую: `Monoid` + `inverse: Option[Matrix]` (текущее состояние) **с
  задокументированной причиной**, плюс новый тип `InvertibleMatrix` с честным
  `Group` — это и есть «категориально точное» решение.
- `XYPair`, `Points`, `TimeSpan`: `CommutativeMonoid` вместо `Monoid`
  (коммутативность очевидна и тестируется).
- `Patch`: добавить комментарий, что это **моноид эндоморфизмов с правым
  действием** на `XJDF` (сейчас в doc есть, но в коде `andThen` — левая
  ассоциативность; выверить согласованность закона `applyTo(applyTo(t,p),q) ==
  applyTo(t, p |+| q)` с определением `combine = andThen`).

**Объём.** 0.5–1 день + законы.

### 4.2 Law-слой в стиле discipline

Сейчас законы написаны вручную (`AlgebraLaws` со вспомогательными
`semigroupAssociativity`/`monoidLaws`/…). Это работает, но не масштабируется на
M2/M3. Предложение:

- Добавить **cats-laws** (org.typelevel %% cats-laws) и **discipline** — они
  дают `GroupTests`, `CommutativeMonoidTests`, `SemilatticeTests`,
  `FunctorTests` и т.д. как готовые наборы, с генераторами.
- Перевести `AlgebraLaws`/`AlignmentLaws` на discipline; `Pulse`-функтор
  проверить `FunctorTests[Pulse].functor`; `snapshot` — `Invariant`-/закон
  естественности оставить явным (он специфичен).
- Держать **доменные** законы (семантика выборки §6.1.3.2, хронология аудитов,
  действие Patch) как обычные `ScalaCheckSuite`-свойства — они не из discipline.

Это не меняет публичный API, только слой проверки.

**Объём.** 1–2 дня. **Критерий:** каждый cats-инстанс имеет либо discipline-тест,
либо доменный property-тест.

### 4.3 Закон согласованности `Part`

`Part` имеет 27 полей и **пять** параллельных мест, перечисляющих их:
`keys`, `valueOf`, `combine`, `PartBuilder.set`, `ValueOf` (match type). Это
источник дрейфа. Предложение — добавить инвариантные законы, которые ловят
рассинхрон:

```scala
property("Part.keys ↔ valueOf согласованы"):
  forAll { (p: Part) =>
    p.keys.forall(k => p.valueOf(k).isDefined) &&
    (0 until PartitionKey.all.size).forall(i =>
      p.valueOf(PartitionKey.all(i)).isDefined == p.keys.contains(PartitionKey.all(i)))
  }

property("Part.combine сохраняет право-смещённость по каждому ключу"):
  forAll { (a: Part, b: Part) =>
    PartitionKey.all.forall(k =>
      Part.combine(a, b).valueOf(k) == b.valueOf(k).orElse(a.valueOf(k)))
  }
```

Второе свойство — по сути исполняемая спецификация overlay; оно же защитит от
регресса, если `combine` забудет поле.

**Объём.** 0.5 дня. **Критерий:** добавление нового Partition Key без
обновления одного из пяти мест ломает сборку **или** закон.

### 4.4 Golden-тесты примеров

Сейчас примеры из спецификации существуют как `ValidatedNec`-значения, но нет
теста «вывод совпадает с литералом спецификации». Предложение — модуль
`examples/src/test` с golden-файлами:

- для каждого примера (3.1, 3.4, 3.6, 5.2, 3.3, brochure) зафиксировать
  ожидаемый `Show`-вывод (или, после M2, XML/JSON-вывод) в ресурсе;
- тест сравнивает рендер с golden-файлом; при осознанном изменении — обновить
  golden.

Это превращает «примеры компилируются» в «примеры **и** дают ожидаемый
результат», и станет основой для M2 round-trip-тестов.

**Объём.** 1 день. **Критерий:** `sbt examples/test` сверяет примеры с
эталонами.

---

## 5. P3 — стратегия следующих милстоунов

### 5.1 Скорректировать ROADMAP под выводы аудита

В `ROADMAP.md` пункт M1 содержит `[x] Feedback-итерация сборки`, но аудит
показал незакрытые пункты. Предложение — переписать M1 в виде чек-листа
закрытия **всех** P0/P1 из этого документа, явно указав:

- `Part/@ProductPart` и `Part/@Metadata` (конформность Table 6.4);
- зелёный `sbt test` + перегенерация `build.log`/перевод его в CI;
- исправление 7 ссылок на таблицы;
- судьба `ChangeOrder` и `IdAllocator`;
- семантика `AmountRange`.

И добавить в «Определение готовности этапа» обязательный полный прогон
(не `testQuick`).

### 5.2 Ввести CI до M2

До начала кодеков (M2) критично иметь CI, иначе «сборка вслепую» (риск №1 в
ROADMAP) останется навсегда. Минимальный workflow:

```yaml
# .github/workflows/ci.yml
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4  # temurin 17/21
      - uses: sbt/setup-sbt@v1
      - run: sbt scalafmtCheckAll test examples/run
```

Это автоматически проверяет: форматирование, полный тест, запуск примеров —
то, что сейчас никак не подтверждено. При недоступности CI-раннера — хотя бы
скрипт `scripts/check.sh` с теми же шагами, запускаемый ревьюером.

### 5.3 Генератор «таблица → тип» как ядро M3

M3 (~130 ресурсов) — самый объёмный этап. Чтобы не повторить ошибки M0
(неверные ссылки, дрейф полей), предложение — **кодогенератор**:

- парсер `./reference/xjdf/6 – Resources.md`: секции `## 6.x`, строки
  `**Table 6.N: … Resource**`, таблицы `| NAME | DATA TYPE | DESCRIPTION |`;
- эмиссия `case class` с полями из столбца NAME, типом из карты
  `DATA TYPE → Scala` (см. ниже), и scaladoc-ссылкой на **найденную** таблицу;
- ручная доводка только там, где нужны инварианты/методы (как `Media.paper`,
  `Color.cyan`).

Карта типов (сверенная с Appendix A, Table A.1):

| DATA TYPE | Scala |
|---|---|
| `NMTOKEN` | `Option[NmToken]` |
| `NMTOKENS` | `Option[NmTokens]` |
| `string` | `Option[XjdfString]` |
| `ID` | `Option[Id]` |
| `IDREF` | `Option[IdRef]` |
| `IDREFS` | `Option[IdRefs]` |
| `float` | `Option[Double]` |
| `integer` | `Option[Long]` |
| `XYPair` | `Option[XYPair]` |
| `shape` | `Option[Shape]` |
| `rectangle` | `Option[Rectangle]` |
| `matrix` | `Option[Matrix]` |
| `dateTime` | `Option[Timestamp]` |
| `duration` | `Option[TimeSpan]` |
| `enumeration`/`enumerations` | закрытый enum (по «Allowed values are») |
| `regExp` | `Option[RegExp]` (новый) |
| `IntegerRange` | `Option[IntegerRange]` |

Кардинальность: `?` → `Option`, `*` → `Chain`, `+` → `NonEmptyChain` (уже принято
в `docs/04`).

**Объём.** 2–3 дня на генератор, дальше — механический прогон партиями.

### 5.4 Архитектура кодеков (M2) — typeclasses + round-trip законы

Чтобы не закапывать сериализацию в домен, предложение:

- модули `codec-core` (typeclasses `Encoder`/`Decoder`), `codec-xml`
  (scala-xml), `codec-json`;
- законы round-trip `decode ∘ encode = id` на **всех** типах M1 (property-тесты),
  плюс diff против литералов спецификации (golden, см. 4.4);
- `Show`-инстансы (формат значений спецификации) переиспользовать как
  «сериализацию атомарных типов», а не дублировать формат в кодеке.

Отдельно — не тянуть `Header/@ID`-скоуп и `ProductPart`-тип в кодеки, пока не
закрыты P0 (иначе XML/JSON-слой зацементирует ошибки).

### 5.5 Пакетная организация `prim` vs `model`

`prim/Common.scala` содержит элементы глав 3/8 (`Comment`, `GeneralID`, `Event`,
`Milestone`, `Dependent`, `FileSpec`, `Disposition`), а не «примитивы». Для
чистоты слоёв (см. `docs/04`) предложение — вынести элементы в `model/` или
новый пакет `elements/`, оставив в `prim/` только типы данных Appendix A. Это
механическое перемещение, но оно снижает когнитивную нагрузку и готовит M3.

**Объём.** 1 час + правка импортов. Не делать в одном PR с функциональными
изменениями.

---

## 6. Порядок работ (рекомендуемый план PR)

1. **PR A (P0):** 2.2 (`ProductPart`), 2.3 (`Metadata`/`RegExp`), 2.1
   (зелёный `test`, лог в CI) — «конформность Table 6.4 и сборка».
2. **PR B (P1):** 3.1 (ссылки на таблицы), 3.4 (`AmountRange`), 3.6 (ID-скоуп).
3. **PR C (P1):** 3.2 (`ChangeOrder`), 3.3 (`IdAllocator`), 3.5 (`NamedColor`).
4. **PR D (P2):** 4.1 (типы алгебр), 4.2 (discipline), 4.3 (закон `Part`).
5. **PR E (P3):** 5.1 (ROADMAP), 5.2 (CI), 4.4 (golden-тесты), 5.5 (слои).
6. **Дальше:** 5.3 (генератор) → M2 → M3.

Каждый PR автономен и приносит полный зелёный прогон (после 2.1).

---

## 7. Критерии «готово» после изменений

- `sbt clean test` зелёный **и** `sbt examples/run` выполняется без ошибок;
- `scalafmtCheckAll` чистый;
- в кодовой базе нет `& Partial`, нет `ProductRef(IdRef)`, нет
  `Metadata: Option[NmToken]`;
- `Part/@ProductPart: Option[NmToken]`, `Part/@Metadata: Option[RegExp]`;
- каждая таблица, на которую ссылается scaladoc, существует в спецификации
  (проверка генератором);
- каждый cats-инстанс имеет discipline- или property-тест;
- ROADMAP M1 переписан как закрытие всех пунктов P0/P1.

---

## 8. Что сознательно НЕ предлагается менять

- **Не переписывать ядро**: opaque/named-tuple/enum/union-выбор верен и
  документирован; изменения точечные.
- **Не вводить эффект-систему в домен**: `Validated`/`State`/`Patch` достаточно
  для чистого ядра; `Kleisli`/`WriterT`/`fs2` остаются в M5, как и запланировано.
- **Не добавлять derivation макросами**: ручные `Show`/`Eq` документируют токены
  спецификации; при желании автоматизировать — через кодогенератор M3, а не
  макросы (совместимо с консервативным диалектом 3.8).
- **Не менять версии стека** (Scala 3.8.4, sbt 2.0.2, cats 2.13.0) без
  подтверждения реестров — это отдельный риск, вынесенный в ROADMAP.