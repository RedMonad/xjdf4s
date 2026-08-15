# Как XJDF-домен выражается возможностями Scala 3

Соответствие «требование спецификации → конструкция языка», сверенное с
`./reference/scala/docs/reference/*` и `./reference/scala/spec/*`.
Используемый компилятор: Scala 3.8.4.

## opaque type — номинальные обёртки без рантайм-стоимости

Каждый XJDF-тип данных (Appendix A, Table A.1) — отдельный номинальный тип:

- `NmToken`, `NmTokens`, `Id`, `IdRef`, `IdRefs`, `JobId`, `JobPartId`,
  `ProjectId`, `Url`, `XjdfString`, `LanguageTag`, `CommentText`, `XPath`,
  `IcsVersion`, `XjdfVersion` — строковые лексемы с валидацией;
- `XYPair`, `Shape`, `Rectangle`, `Matrix`, `AmountRange`, `TimeRange`,
  `WorkstepKey` — **opaque поверх named tuple** (см. ниже);
- `Amount`, `Points`, `Microns`, `Grammage`, `Coverage`, `UnitInterval`,
  `Severity`, `Timestamp`, `TimeSpan`, `ProcessIndex` — числовые/временны́е
  величины со своими единицами.

Это не «анемичные обёртки»: в компаньонах живут конструкторы с валидацией
(`from: … => Option`, `unsafe`), законы-инварианты и cats-инстансы
(`Show`, `Eq`, `Order`, `Monoid`, `Semigroup`, `Semilattice`).
Ссылка: `reference/other-new-features/opaques.md`.

> **Урок прозрачности (подтверждён сборкой).** Внутри файла, где определён
> opaque type, тип **прозрачен** (виден как лежащий в основе), поэтому:
> 1. extension-методы компаньона (`NmToken.value`, `Timestamp.toJava`) в
>    другом объекте того же файла не находятся ни лексически, ни через
>    implicit scope (последний строится по базовому типу — `String`,
>    `OffsetDateTime`, …) — их нужно либо импортировать, либо заменять
>    прямым обращением к базовому типу;
> 2. `given`-инстансы компаньона (например, `given Show[Timestamp]`) в том
>    же файле не находятся через implicit scope — нужны именованные given
>    и явные ссылки на них;
> 3. при совпадении имени с членом базового типа (например, `String.value`)
>    компилятор выбирает член и падает с ошибкой доступа.
> В коде это учтено: `Timestamp.showTimestamp`, прямые `t.plus(d)`,
> `r.start.toInstant`, `fmtDouble(color.c)` и т.п.

## named tuples — именованные записи там, где class избыточен

Начиная со Scala 3.7 (в 3.8 — стабильная фича,
`reference/other-new-features/named-tuples.md`):

- `XYPair = (x: Double, y: Double)`, `Shape = (width, height, depth)`,
  `Rectangle = (llx, lly, urx, ury)`, `Matrix = (a, b, c, d, tx, ty)` —
  точное отражение синтаксиса значений спецификации («`595.27559055
  822.04724409`» — это и есть пара x y);
- `AmountRange = (amount, max, min)` — трио «обещание ± допуски»;
- `TimeRange = (start, end)` — интервал исполнения;
- `WorkstepKey = (jobId, jobPartId, part)` — §2.2.2: рабочая операция
  однозначно идентифицируется тройкой JobID/JobPartID/Partition Keys.

Имена полей живут на уровне представления; наружу типы смотрят как
номинальные (opaque), с расширениями `x`/`y`, `start`/`end` и т.п.

## enum — закрытые перечисления из Appendix A

`enum Usage`, `Side`, `Sides`, `Edge`, `Face`, `Orientation`, `Status`,
`DeviceStatus`, `SeverityClass`, `BindingType`, `BindingOrder`, `StapleShape`,
`GlueType`, `TightBacking`, `Coating`, `Opacity`, `MediaDirection`,
`ISOPaperSubstrate`, `MediaType`, `Automation`, `SheetLay`, `NamedColor`,
`EndStatus`, `PrintPreference`, `PreflightLevel`, `PreviewType`,
`TransferCurveTarget`, `VariableType`, `VariableQuality`, `ColorType`,
`SpreadType`, `Scope`, `FitPolicy`, `Anchor`, `CommandResult`, `ResourceLevel`,
`OverwritePolicy`, `DispositionAction`, `WasteDetail`, `FoldFrom`, `FoldTo`,
`ResourceStatus`, `SoftCoverGlueProcedure`, `SoftCoverScoring`,
`HardCoverJacket` — все со значениями ровно из таблиц спецификации.

Открытость XJDF-списков (§1.1.1, §1.10.3.2: NMTOKEN-списки расширяемы)
моделируется **парой механизмов**: закрытый `enum` (для «Allowed values are»)
и открытый `NmToken` + каталоги `Catalog.*` (для «Values include»). Расширения
вендоров — через namespace-префиксы (`NsPrefix`, `Foreign`, `Extension`).

Каждый enum реализует `XjdfEnum` (токен из спецификации) и наследует `Show`/
`Eq`/`fromToken` из `XjdfEnumCompanion` — миксин-трейт для компаньонов.

## enum с параметрами (GADT-стиль) — суммы с полезной нагрузкой

- `ResourcePayload` — 12 реализованных ресурсов + `Foreign(ns, local)`;
- `IntentPayload` — 8 реализованных интентов + `Extension(ns, local)`;
- `Audit` — 5 видов аудитов с Header-ом (Table 3.3);
- `PartitionValue` — рантайм-тегованное значение ключа разбиения.

Ссылка: `reference/enums/adts.md`.

## union types — «не более одного из» и открытые альтернативы

- `OrientationSpec = Orientation | Matrix` — «at most one of @Orientation /
  @Transformation» (Table 6.1);
- `NotificationDetail = Event | Milestone` — «not more than one of Event and
  Milestone» (Table 8.49);
- `BindingDetails = AdhesiveNote | EdgeGluing | … | SoftCoverBinding` —
  типо-зависимые детали BindingIntent (Table 4.8), с проверкой парности
  «BindingType ↔ деталь» в `BindingIntent.isLawful`.

Ссылка: `reference/new-types/union-types.md`.

## intersection types — уточнения типа на уровне маркеров

- `trait Partial` — маркер ослабленной кардинальности change order (§1.6.5);
- `XJDF extends Partial`, и `type ChangeOrder = XJDF & Partial` — «тот же
  тикет, но в контексте изменения». Значение одно, а API различает контексты
  на уровне типов.

Ссылка: `reference/new-types/intersection-types.md`.

## match types — тип значения ключа зависит от самого ключа

```scala
type ValueOf[K <: PartitionKey] = K match
  case PartitionKey.DocIndex.type | … | SheetIndex.type => IntegerRange
  case PartitionKey.Side.type        => Side
  case PartitionKey.TileID.type      => XYPair
  case PartitionKey.PreviewType.type => PreviewType
  case PartitionKey.TransferCurveName.type => TransferCurveTarget
  case _ => NmToken
```

`ValueOf` — **type-level** отображение «ключ → тип значения» (Table 6.4),
доступное для программирования на уровне типов (например, как свидетель
`Option[ValueOf[PartitionKey.SheetName.type]] = Option[NmToken]`).
На уровне значений типизированный интерфейс — это поля case class
(`part.docIndex: Option[IntegerRange]`, `part.side: Option[Side]`) и
типизированные конструкторы `Part.docIndex(r)`, `Part.bySide(s)`,
`Part.sheetName("S1")`; рантайм-доступ по нелитеральному ключу —
`Part.valueOf: PartitionKey => Option[PartitionValue]` с тегованным значением.
(Обобщённый `get[K <: PartitionKey](key: K): Option[ValueOf[K]]` не
реализуем без кастов: компилятор не уточняет абстрактный ключ в ветках матча
при редукции match type — подтверждено сборкой; см. `ROADMAP.md`, «Риски и меры снижения».)

Ссылка: `reference/new-types/match-types.md`.

## trait — общая поверхность именованных элементов

`trait Named[N] { def name: N }` реализуют `Intent` и `ResourceSet` — общий
«именованный» интерфейс без дублирования поля. Член оставлен абстрактным
сознательно: trait-конструктор `(val name: N)` конфликтовал бы с
case-class-параметром `name` подкласса (E164: «needs `override` modifier»),
а `Named[N](name: N)` без val не даёт членов. Ссылки:
`reference/other-new-features/trait-parameters.md`,
`reference/changed-features/…`.

## context functions — окружение сборки тикета

`IdAllocator ?=> A` (псевдоним `WithIds[A]`) — код внутри такого контекста
может выделять свежие `@ID` через `summon[IdAllocator]`; чистая версия того же
эффекта — `IdSource.fresh: State[Counter, Id]`. Ссылка:
`reference/contextual/context-functions.md`.

## case class + параметры по умолчанию — таблицы спецификации один-в-один

Каждая таблица главы 4/6/8 перенесена как case class с именованными полями
(имена — из столбца NAME таблиц, `Option` — кардинальность `?`/`*`,
`NonEmptyChain` — `+`). Пример: `BindingIntent(bindingType: BindingType,
childRefs: Option[IdRefs], details: Option[BindingDetails], …)`.

## Структурная индукция вместо рантайм-проверок

Где спецификация задаёт структурные ограничения («SHALL», «at most one»),
они выражены типами (union/enum/opaque); семантические ограничения («SHALL»
связанные с несколькими элементами: уникальность ключей ResourceSet, границы
CombinedProcessIndex, хронология AuditPool) — аппликативным валидатором
`ValidatedNec[Issue, Unit]` с XPath-локализацией каждой ошибки (см. 03-cats-mapping.md).

## Что сознательно НЕ использовано

- **derivation/macros** — инстансы `Show`/`Eq` написаны вручную: их ровно по
  одному на тип, и они документируют токены спецификации;
- **экспериментальные фичи** (capture checking, `erased`, `NamedTypeArguments`)
  — домен должен собираться на стабильном диалекте 3.8;
- **implicits Scala 2-стиля** — только `given`/`using`/extension-методы.
