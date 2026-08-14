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

Катаморфизм BOM использует тот же носитель: `cata: ProductTree[ValidatedNec]
=> ValidatedNec` — «свёртка дерева в аккумулятор ошибок».

## NonEmptyChain — свободный моноид без пустого слова

Везде, где спецификация пишет `T+` (один или более), используется
`NonEmptyChain`; где `T*` — `Chain`. Это даёт:

- законную `Semigroup` (конкатенация) у `AmountPool` и `AuditPool`;
- `Foldable`/`NonEmptyTraverse` для свёрток и травесов;
- `mkString_`, `last`/`head`, дешёвое конкатенирование.

Ссылка: `docs/datatypes/chain.md`, `docs/typeclasses/foldable.md`.

## Semigroup/Monoid — явные алгебры домена

| Тип | Операция | Смысл |
|---|---|---|
| `Part` | right-biased overlay | уточнение расписания поверх общего |
| `AmountPool`, `AuditPool` | конкатенация | хронологическое накопление |
| `Matrix` | композиция | аффинные преобразования (§2.6.5) |
| `Patch` | композиция функций | change orders (§1.3.2) |
| `TimeSpan` | сложение | длительности |
| `XYPair` | покоординатное сложение | векторная арифметика |

Все они проверяются на ассоциативность/единицу; `Patch` — поведенчески через
действие на тикеты.

## Semilattice — интервалы обязательств

`Semilattice[AmountRange]`, где `combine = meet` — ужесточение ограничений
(min ↑, max ↓, amount ↓). Законы: ассоциативность, коммутативность,
идемпотентность. Двойственная операция — `join` (оптимистичное расширение).
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

- `Show` каждого типа печатает **формат спецификации** (матрица — 6 чисел,
  XYPair — `x y`, Part — `key=value`, Issue — `severity at XPath: message`);
- `Eq`/`Order` — сравнения для выборки разделов, хронологических проверок и
  законов.

## Eval-подход и дальнейшие шаги

Текущая версия не использует `Eval`/`Kleisli`/`WriterT` явно (валидация —
чистый `Validated`); в ROADMAP они запланированы для:
- потоковой обработки сигналов (`fs2`-совместимый слой, `Writer`-семантика
  аудитов);
- эффектной загрузки больших RunList (`Kleisli[F, IdSource, *]`);
- ленивых свёрток BOM на глубоких деревьях (`Eval`-стек-безопасность).
