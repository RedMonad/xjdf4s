# Роль cats в описании сути XJDF

Сверено с `./reference/cats/docs/*` (typeclasses, datatypes, algebra). Cats не
«декорация» модели — он несёт **законы**: каждый инстанс ниже имеет
соответствующее свойство в модуле `laws` (munit + ScalaCheck).

## ValidatedNec — валидация как аппликативный эффект

`type Validation[A] = ValidatedNec[Issue, A]`

Спецификация предписывает много независимых SHALL-требований (уникальность
ключей ResourceSet §3.4, границы CombinedProcessIndex, ID/IDREF-целостность
§2.2.3, хронология AuditPool §3.2, парность ключей PartAmount §6.1.2.1…).
Аппликативный функтор `Validated` накапливает **все** нарушения сразу
(monoidal combine), а не останавливается на первом — то, что нужно
интегратору. `NonEmptyChain[Issue]` гарантирует непустой список ошибок.
Ссылка: `docs/typeclasses/applicative.md`, `docs/datatypes/validated.md`.

> **Важно:** `Validated` — **не монада**: у него сознательно нет монадического
> `flatMap` (см. validated.md: «Validated isn't a monad, but an Applicative
> Functor»), поэтому for-comprehensions на нём не компилируются. При этом
> метод `.andThen` у `Validated` **есть** — это right-biased последовательная
> композиция без накопления левой ошибки; он используется, например, в
> `dsl.intent` и в README-примере. Последовательная композиция с накоплением
> ошибок — либо через `mapN` (аппликативно, параллельно), либо через явный
> паттерн-матч по `Valid`/`Invalid` (в примерах — хелпер `SpecExamples.chainV`),
> либо через конверсию `.toEither`.

Катаморфизм BOM использует тот же носитель: `cata: ProductTree[ValidatedNec]
=> ValidatedNec` — «свёртка дерева в аккумулятор ошибок».

## NonEmptyChain — свободная полугруппа без пустого слова

Везде, где спецификация пишет `T+` (один или более), используется
`NonEmptyChain`; где `T*` — `Chain`. Категориально (см. 01 §4):
`NonEmptyChain[A]` — свободная **полугруппа** (носитель `T+`, нейтрального
элемента нет), `Chain[A]` — свободный **моноид** (`Chain.empty`). Поэтому
`AuditPool`, `AmountPool`, `NmTokens`, `ProcessPath` объявляют `Semigroup`,
а не `Monoid` — иначе пришлось бы выдумывать несуществующее пустое значение.
Это даёт:

- законную `Semigroup` (конкатенация) у `AmountPool` и `AuditPool`;
- `Foldable`/`NonEmptyTraverse` для свёрток и травесов;
- `mkString_`, `last`/`head`, дешёвое конкатенирование.

Ссылка: `docs/datatypes/chain.md`, `docs/typeclasses/foldable.md`.

## Semigroup/Monoid — явные алгебры домена

| Тип | Структура | Операция | Смысл |
|---|---|---|---|
| `Part` | `Semigroup` | right-biased overlay | уточнение расписания поверх общего |
| `AmountPool`, `AuditPool` | `Semigroup` (носитель `T+`) | конкатенация | хронологическое накопление |
| `NmTokens`, `ProcessPath` | `Semigroup` (носитель `T+`) | конкатенация | списки токенов/процессов |
| `Matrix` | `Monoid` (+ частичный `inverse`) | композиция | аффинные преобразования (§2.6.5) |
| `Patch` | `Monoid` | композиция функций | change orders (§1.3.2) |
| `TimeSpan`, `XYPair`, `Points` | `CommutativeMonoid` | сложение | длительности / векторная арифметика |

Все инстансы проверяются property-тестами в `AlgebraLaws` (ассоциативность,
единица, коммутативность там, где она объявлена); `Patch` — поведенчески через
действие на тикеты. ADR-0009: законы — рукописные сьюты, `cats-laws`/
`discipline-munit` осознанно не приняты.

## Order — полный порядок для величин

`Coverage`, `UnitInterval`, `IntegerRange`, `TimeSpan` (а также `Amount`,
`Points`, `Microns`, `Grammage`, `Severity`, `Timestamp`, `XYPair`) объявляют
`Order`: сравнение осмыслено спецификацией и используется выборкой разделов,
хронологическими проверками и законами (PR-12, `AlgebraLaws`).

## AmountBounds — интервалы обязательств (ADR-0004)

После PR-11 тройка `@Amount` + `@MinAmount` + `@MaxAmount` (Table 6.3)
разделена: nominal `Amount` живёт отдельно от контракта
`AmountBounds(min, max)`. `meet` — ужесточение (пересечение) контракта и
возвращает `Option`, потому что пересечение может быть пустым; `widen` —
оптимистичное расширение. `Semilattice` **не объявляется**: `meet` частична,
а «законы важнее названий» (N-23) — вместо инстанса property-тесты
направлений, законов и пустого пересечения в `AlgebraLaws`.
Ссылка: `docs/algebra.md` (иерархия lattice из algebra).

## FunctionK — естественное преобразование сигнал → история

```scala
val snapshot: FunctionK[Pulse, NonEmptyChain]  // Pulse ~> Log
val signalToAudit: Signal => Audit             // Table 3.2
```

Закон естественности (`map f ∘ snapshot = snapshot ∘ map f`) проверяется
свойством в laws. `Pulse` имеет `Functor`, `NonEmptyChain` — функтор
свободного моноида. Ссылка: `docs/datatypes/functionk.md`,
`docs/typeclasses/functor.md`.

## Ior — трёхзначный итог слияния change order

`Patch.mergeResourceSets: Ior[NonEmptyChain[Issue], XJDF]` — `Left` (слить
нельзя), `Right` (чисто), `Both` (слито, но ключи конфликтовали — issue
остаётся свидетелем). Ссылка: `docs/datatypes/ior.md`.

## State — выделение ID как чистый эффект

`IdSource.fresh: State[Counter, Id]` — генератор `@ID` без мутаций; наружу —
context function `IdAllocator` (см. 02-scala3-features.md). Ссылка:
`docs/datatypes/state.md`.

## Show/Eq/Order — наблюдаемость домена

- `Show` каждого типа — **debug-вывод** (матрица — 6 чисел, XYPair — `x y`,
  Part — `key=value`, Issue — `severity at XPath: message`). Он приближен к
  формату спецификации, но **не является сериализацией**: канонические
  XML/JSON wire-рендеры появляются в M2, и только они станут golden-форматом
  (§12.2: `Show` тестируется только как debug-вывод);
- `Eq`/`Order` — сравнения для выборки разделов, хронологических проверок и
  законов.

## Eval — стек-безопасность BOM (M1.4-7)

С PR-12 `Eval` используется: `Bom.toTreeEval`/`cataEval` — варианты развёртки
и катаморфизма на trampoline `Eval.defer`; `toTree`/`cata` — совместимые
обёртки. Deep-тест `BomLaws` гоняет цепочку `@ChildRefs` глубиной 10 000 без
`StackOverflowError`. Ссылка: `docs/datatypes/eval.md`.

Остаются запланированными в `ROADMAP.md`:
- потоковая обработка сигналов (`fs2`-совместимый слой, `Writer`-семантика
  аудитов);
- эффектная загрузка больших RunList (`Kleisli[F, IdSource, *]`).
