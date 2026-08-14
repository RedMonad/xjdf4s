# xjdf4s

Доменная модель **[XJDF 2.2](https://www.cip4.org)** (Exchange Job Definition
Format) на **Scala 3.8.4**, построенная вокруг **cats 2.13.0** и
категориального взгляда на спецификацию. Сборка — **sbt 2.0.2**.

XJDF — формат обмена данными для полиграфических рабочих процессов (CIP4):
описание продукта (`ProductList`), производственных инструкций
(`ResourceSet`) и записей исполнения (`AuditPool`) в виде одной транзакции
между Controller и Device.

## Идея

Тикет — это не «большой объект», а **морфизм** Controller → Device;
`XJDF/@Types` — слово свободной категории процессов; `ProductList` — начальная
алгебра (катаморфизм по BOM); `AuditPool` — свободный моноид над аудитами;
change order — действие моноида эндоморфизмов на тикетах. Подробно:
`docs/01-category-theory-view.md`.

Код использует: opaque types (все типы Appendix A), named tuples
(`XYPair`, `Matrix`, `WorkstepKey`…), enum (40+ закрытых перечислений и
GADT-суммы полезных нагрузок), union types (`BindingDetails`,
`OrientationSpec`), intersection types (`ChangeOrder = XJDF & Partial`),
match types (`ValueOf[PartitionKey]`), trait-параметры, context functions.
cats даёт законы: `ValidatedNec` (валидация-аккумулятор), `Semigroup`/`Monoid`/
`Semilattice` (Part, AmountPool, AuditPool, Matrix, Patch), `FunctionK`
(выравнивание сигнал→аудит, Table 3.2), `Ior`, `State`, `Show`/`Eq`/`Order`.

## Модули

| Модуль | Артефакт | Содержимое |
|---|---|---|
| `modules/core` | `xjdf4s-core` | примитивы, модель, ресурсы, интенты, DSL, валидатор |
| `modules/laws` | `xjdf4s-laws` | законы структур (munit + ScalaCheck) |
| `modules/examples` | `xjdf4s-examples` | примеры из спецификации + демо `sbt examples/run` |

## Быстрый старт

```bash
sbt compile          # собрать
sbt test             # законы и примеры спецификации
sbt examples/run     # демо: примеры, BOM-катаморфизм, change order, матрицы
```

## Минимальный пример

```scala
import xjdf4s.dsl.dsl
import xjdf4s.model.*

// Example 3.1 спецификации: <XJDF JobID="J1" Types="Product" Version="2.2"/>
val ticket: ValidatedNec[Issue, XJDF] =
  dsl.TicketDraft.of("J1", ProcessType.Product).flatMap(_.build)
```

## Документация

- `ROADMAP.md` — план работ (M0 выполнено; M1–M6 впереди);
- `docs/01-category-theory-view.md` — XJDF через призму теории категорий;
- `docs/02-scala3-features.md` — соответствие «спецификация → Scala 3»;
- `docs/03-cats-mapping.md` — какие абстракции cats и зачем;
- `docs/04-architecture.md` — модули, пакеты, принципы.

Вся модель сверена с `./reference/xjdf/*` (каждый тип в scaladoc ссылается на
таблицу/раздел спецификации); языковые фичи — с `./reference/scala/*`;
cats — с `./reference/cats/*`; сборка — с `./reference/sbt/*`.
