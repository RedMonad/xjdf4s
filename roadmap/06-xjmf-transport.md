# Этап 06 — XJMF-транспорт: каналы и сессии

| Поле | Значение |
|---|---|
| Цель | Модель поведения XJMF-обмена: персистентные каналы, подписки, корреляция ответов, окна замены `SignalResource` — как чистая алгебра с интерпретаторами (in-memory, журнал) |
| Вход | Этапы 03 (паттерн Free) и 04 (кодеки сообщений); модель messaging уже содержит 44 сообщения |
| Выход | Модуль `xjmf`: алгебра `XjmfOp`, машина состояний каналов, in-memory интерпретатор, событийный журнал |
| Сложкость | Высокая |
| Зависимости | 03, 04 |

## Зачем это нужно

Модель знает *форму* сообщений (`SignalResource`, `Subscription`, `ChannelMode`…), но не знает
*протокола*: как подписка превращается в канал, как `Signal` коррелирует с `Response` через
`Header/@refID`, когда `SignalResource` замещает предыдущие данные (окна `@ReplaceAfter`/
`@ReplaceBefore` уже валидируются в модели). Этот этап делает протокол исполняемым **без сети** —
что даёт детерминированные тесты для всех сценариев обмена.

## Предпосылки: что читать

- норматив: `reference/xjdf/7 – Messaging.md` (Subscription/Signal/Response semantics,
  `@refID`, persistent channels) и `reference/xjdf/8 – Subelements.md` Table 8.71
  (`SubscriptionInfo`);
- `reference/cats/docs/datatypes/freemonad.md` (паттерн из этапа 03),
  `datatypes/state.md`, `datatypes/chain.md`, `datatypes/eval.md` (стекобезопасные рекурсии),
  `datatypes/writer.md` (журнал событий).

## Дизайн

### 1. Алгебра транспорта

Транспорт абстрагируется над *семантикой* обмена, а не над сокетами:

```scala
enum XjmfOp[A]:
  case OpenChannel(subscription: Subscription, channelId: Nmtoken)        extends XjmfOp[Unit]
  case CloseChannel(channelId: Nmtoken)                                   extends XjmfOp[Unit]
  case Deliver(signal: Signal)                                            extends XjmfOp[Unit]
  case AwaitResponse(refId: Nmtoken)                                      extends XjmfOp[Option[Response]]
  case Channels                                                            extends XjmfOp[Vector[SubscriptionInfo]]

type Xjmf[A] = Free[XjmfOp, A]
```

Заметьте: `Signal` — конкретный тип модели (а не «сырые байты») — транспорт работает на уровне
домена; кодеки (этап 04) подключаются в HTTP-интерпретаторе (этап 07).

### 2. Машина состояний канала

Состояние in-memory интерпретатора — `State[XjmfState, *]`:

```scala
final case class XjmfState(
    channels: Map[Nmtoken, ChannelState],
    pending: Chain[Signal],                 // доставленные, ждущие ответа
    events: Chain[TransportEvent],          // журнал
)

enum ChannelState:
  case Subscribed(subscription: Subscription)
  case Closed
end ChannelState
```

Правила, которые кодируются в интерпретаторе (и только там — домен их не знает):

- `Deliver(signal)` при закрытом канале → ошибка/событие;
- `Signal/@ChannelMode = Reliable` ⇒ получатель **SHALL** ответить: `AwaitResponse` по
  `signal.header.refId` находит (или не находит) `Response`; неотвеченный Reliable-сигнал —
  это видимое состояние (для таймаутов на этапе 07);
- `FireAndForget` — ответ опционален;
- `SignalResource` с окнами: предыдущие сигналы канала, попавшие в окно
  (`@ReplaceAfter < time ≤ @ReplaceBefore`), замещаются новым — реализуется как
  трансформация `pending`, покрытая тестами (окна уже валидируются в модели).

### 3. Корреляция через `@refID`

`Header/@refID` связывает `Signal` с инициировавшим `Query` и с `Response`. В состоянии
держите индекс `refId → channelId`; тест-сценарий: Query (refID=Q1) → подписка →
`SignalResource` (refID=Q1) → `ResponseResource`. Это проверяет именно то, что ломается
в реальных интеграциях.

### 4. Два интерпретатора сейчас, сеть — потом

1. `XjmfOp ~> State[XjmfState, *]` — детерминированная машина состояний (тесты);
2. `XjmfOp ~> Writer[Chain[TransportEvent], *]` — трасса обмена (диагностика, replay).

Один и тот же сценарий-программа исполняется обоими; на этапе 07 добавится
`XjmfOp ~> IO` поверх HTTP.

### 5. Free vs tagless (решение этого этапа)

Для транспорта берите **tagless final** для внутренних границ (`Xjmf[F[_]]` с
`Send`/`Receive`), а Free — для **сценариев** (программ, которые пользователь пишет и
которые должны исполняться многократно и разно). Практический критерий: то, что будет
сериализоваться/тестироваться как сценарий — Free; то, что является capability
рантайма — tagless.

## Задачи (пошагово)

1. Модуль `xjmf`; алгебра `XjmfOp` + синтаксис.
2. `XjmfState`/`ChannelState` и State-интерпретатор с правилами каналов.
3. Сценарий «подписка → сигнал → ответ» с корреляцией `refID` (тест).
4. Окна замены `SignalResource`: три сигнала, средний с окном, проверка замещения.
5. Семантика `ChannelMode`: Reliable без ответа — наблюдаемое состояние; FireAndForget — нет.
6. Writer-трасса: события `Opened/Closed/Delivered/Replaced` в хронологическом порядке.
7. README модуля: диаграмма состояний канала + пример сценария.

## Уточнения дизайна (зафиксированы по нормативу при реализации)

1. **`@ChannelID` — это `Header/@ID` инициировавшего Query** (Table 8.71, подтверждено scaladoc'ом
   модели `SubscriptionInfo`): `OpenChannel(subscription, channelId, messageType)` не нуждается в
   отдельном индексе — сигнал маршрутизируется по `Header/@refID` прямо в канал.
2. **`DeliverResponse` добавлен в алгебру**: корреляция требует, чтобы ответы входили в состояние;
   `AwaitResponse(answeredId)` возвращает ответ, отвечающий сообщению с этим `Header/@ID`
   (для сигнала это `Header/@ID` сигнала, для Query — ID запроса, 9.6.1/9.6.2).
3. **Traced-интерпретатор — `WriterT[State[XjmfState, *], Chain[TransportEvent], *]`, а не голый
   `Writer`**: трасса зависит от состояния канала (`Unrouted`/`ChannelNotOpen`). Оба интерпретатора —
   тонкие обёртки над одним ядром `transition`, трассы совпадают по построению.
4. **Окно замены — строгое с обеих сторон**: `@ReplaceAfter < time < @ReplaceBefore` («after»/«prior
   to» в Table 7.54; эскизный вариант `≤ ReplaceBefore` скорректирован по нормативной таблице —
   норматив > эскиз). Scope = канал подписки; фильтр — `SignalResource`, тот же `Header/@DeviceID`.
5. **Надёжность берётся из `Signal/@ChannelMode`** (Table 7.7), как в эскизе; `Subscription/@ChannelMode`
   остаётся списком предпочтений канала (Table 7.5) и в машине не перечитывается.
6. Повторное открытие того же `@ChannelID` **замещает** подписку (9.6.3 SHOULD); закрытие канала
   идемпотентно; закрытый канал остаётся видимым как `Closed` (попытка доставки → событие).

## Definition of Done

- [x] Сценарий подписки исполняется State- и Writer-интерпретаторами с одинаковой трассой.
- [x] Корреляция `refID`: тест связки Query→Signal→Response (+ начальный ответ на подписку).
- [x] Окна замены `SignalResource` работают (замещение по `@ReplaceAfter/@ReplaceBefore`).
- [x] `Reliable`-сигнал без ответа виден как незакрытое ожидание; `FireAndForget` — нет.
- [x] Транспорт не содержит IO; сеть появится только на этапе 07.
- [ ] `sbt "clean ; compile ; test"` зелёный (ждёт прогона).

## Риски и альтернативы

- **Перетащить в алгебру сетевые детали** (таймауты, ретраи) — они пойдут в
  HTTP-интерпретатор этапа 07 через cats-retry/`IO.timeout`, иначе in-memory тесты
  станут недетерминированными.
- **Раздувание состояния.** `pending`/индексы должны быть минимальны; окна замены требуют
  упорядоченного журнала — `Chain` сохраняет порядок и дёшево конкатенируется.
- **Два стиля (Free и tagless) в одном модуле** сбивают новичков — зафиксируйте правило из
  п. 5 в README и в ревью-гайде.

## Состояние исполнения

Реализовано в модуле `xjmf` (`cats-free`; `State`/`WriterT`/`Chain` — из cats-core):

- **Алгебра** (`XjmfOp.scala`): `OpenChannel`/`CloseChannel`/`Deliver`/`DeliverResponse`/
  `AwaitResponse`/`Channels` + `Functor` (обоснование каста — как у `DocOp` этапа 03) и синтаксис
  `Xjmf.*` через `Free.liftF`.
- **Состояние** (`XjmfState.scala`): каналы по `@ChannelID`, `pending` (неотвеченные Reliable-сигналы
  в порядке доставки — 9.6.5.1), `responses` (корреляция по `@refID`), `delivered` (журнал канала,
  прореживается окнами), `events` (трасса).
- **Интерпретаторы** (`XjmfInterpreters.scala`): единое ядро `transition` + две обёртки —
  `stateful: XjmfOp ~> State[XjmfState, *]` и `traced: XjmfOp ~> WriterT[State, Chain[TransportEvent], *]`;
  трассы совпадают по построению (проверено тестом `tracesAgree`).
- **Сценарии** (`XjmfTransportChecks`, 7 проверок): жизненный цикл подписки, начальный ответ на
  Query (9.6.2), цепочка корреляции Q1→S1→R1, окно замены (Table 7.54, строгие границы), семантика
  `ChannelMode` (Reliable-ожидание видимо, FireAndForget — нет), маршрутные отказы (`Unrouted`/
  `ChannelNotOpen`), замещение подписки при повторном открытии (9.6.3).
- **README модуля**: нормативная таблица-карта, диаграмма состояний канала, пример сценария,
  правило Free-vs-tagless, заметки о scope (окна — `SignalResource`; `SignalStatus` — точка расширения;
  таймеры/ретраи — этап 07).
