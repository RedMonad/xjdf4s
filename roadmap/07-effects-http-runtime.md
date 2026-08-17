# Этап 07 — Эффекты и HTTP-рантайм

| Поле | Значение |
|---|---|
| Цель | Работающий HTTP-слой в духе http4s: сервер и клиент для XJDF/XJMF, потоковая доставка сигналов, подключение кодеков (04–05) и транспорта (06) к сети |
| Вход | Этапы 04–06; модель и кодеки зелёные |
| Выход | Модуль `http`: routes + client, MIME-типы по Table 9.1, demo «отправить XJDF → подписаться → получить сигналы» |
| Сложность | Высокая |
| Зависимости | 04, 05, 06 |

## Зачем это нужно

Это этап, на котором xjdf4s становится «фреймворком наподобие http4s»: все предыдущие слои
(ADT, валидация, DSL, кодеки, машина каналов) соединяются в реальный сетевой продукт.
Ключевая дисциплина этапа: **эффекты живут только здесь** — ни в `model`, ни в `dsl`,
ни в `xjmf` нет `IO`.

## Нормативные требования транспорта (9.10.3/9.10.4, зафиксировано)

- **POST-only** (9.10.4.1): JSON-команды всегда несут тело — REST-эндпоинты XJDF/XJMF SHALL быть POST.
- **Таблица эндпоинтов контроллеров** (Table 9.4): `/devices`, `/messages`, `/pipes`,
  `/queue-entries/request`, `/queue-entries/return`; эндпоинты подписок задаются самим контроллером в
  `Query/Subscription/@URL`.
- **Таблица эндпоинтов устройств** (Table 9.5): `/gangs[/force|/subscribe]`, `/devices`,
  `/messages`, `/subscriptions[/stop]`, `/queue-entries[/modify|/resubmit|/submit]`,
  `/notifications[/subscribe]`, `/resources[/modify|/subscribe]`, `/devices/shut-down`,
  `/status[/subscribe]`, `/devices/wake-up`.
- **multipart/form-data** (9.10.4.2, Example 9.13): `CommandSubmitQueueEntry`/`CommandResubmitQueueEntry`/
  `CommandReturnQueueEntry` MAY упаковываться как multipart с полем `xjmf` и вложениями `attachment`
  (файлы ссылаются по `filename` относительным URL).

## Предпосылки: что читать

- `reference/cats/docs/typeclasses/applicativemonaderror.md` — `MonadError`/`ApplicativeError`
  как общий язык ошибок (у http4s он общий с cats-effect);
- `reference/cats/docs/typeclasses/parallel.md` — параллельная валидация/обработка;
- `reference/cats/docs/typelevelEcosystem.md` — карта экосистемы (cats-effect, fs2, http4s,
  log4cats, cats-retry, cats-time);
- внешняя документация: cats-effect 3 (IO, Resource, Ref/Queue), fs2 (Stream, Topic),
  http4s (HttpRoutes, EntityEncoder/Decoder, ember).

## Дизайн

### 1. Стек и зависимости

```scala
// новый модуль http
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.x",
  "co.fs2"        %% "fs2-core"    % "3.x",
  "org.http4s"    %% "http4s-ember-server" % "0.23.x",
  "org.http4s"    %% "http4s-ember-client" % "0.23.x",
  "org.http4s"    %% "http4s-dsl"          % "0.23.x",
)
```

Версии сверяются на старте (Maven Central); munit 1.3.5 уже в проекте.

### 2. MIME-типы (Table 9.1 нормативного текста)

```scala
object XjdfMediaTypes:
  val xjdfXml  = MediaType.unsafeParse("application/vnd.cip4-xjdf+xml")
  val xjmfXml  = MediaType.unsafeParse("application/vnd.cip4-xjmf+xml")
  val xjdfJson = MediaType.unsafeParse("application/vnd.cip4-xjdf+json")
  val xjmfJson = MediaType.unsafeParse("application/vnd.cip4-xjmf+json")
```

http4s `EntityEncoder`/`EntityDecoder` строятся поверх кодеков этапов 04–05 — с
`Content-Type` из этой таблицы и корректным `charset` (XML — UTF-8).

### 3. Маршруты сервера

```scala
def routes(service: XjmfService[IO]): HttpRoutes[IO] =
  HttpRoutes.of[IO] {
    case req @ POST -> Root / "submit" =>
      for
        xjdf <- req.as[XJDF]                          // EntityDecoder из codec-xml
        _    <- service.submit(xjdf)                  // tagless-capability из этапа 06
        resp <- Ok(service.receipt(xjdf))             // XJMF Response
      yield resp

    case GET -> Root / "devices" =>
      Ok(service.knownDevices)                        // ResponseKnownDevices

    case req @ POST -> Root / "subscribe" =>
      req.as[Subscription]                            // Query-сообщение
        .flatMap(sub => Ok(service.signals(sub)))     // fs2.Stream[IO, Signal] — потоковая доставка
  }
```

Потоковая доставка: каналы из этапа 06 отображаются на `fs2.concurrent.Topic[IO, Signal]`;
подписчик получает `Stream[IO, Signal]`, http4s отдаёт его chunked-ответом
(streaming body), каждый фрейм — отдельный XJMF-документ (JSON-lines или XML-последовательность
документов; выберите и задокументируйте один формат).

### 4. Клиент

```scala
def submit(client: Client[IO], doc: XJDF): IO[XJMF] =
  client.expect[XJMF](
    Request[IO](Method.POST, uri"http://host/xjdf/submit")
      .withEntity(doc)                                // EntityEncoder
      .withContentType(`application/vnd.cip4-xjdf+xml`),
  )
```

`expect[A]` требует `EntityDecoder[A]` — те же кодеки, никакого дублирования.

### 5. Интеграция с алгебрами

- `XjmfOp ~> IO` — HTTP-интерпретатор транспорта этапа 06: `Deliver` = POST сигнала,
  `AwaitResponse` = ожидание `Response` с `refID` (с `IO.timeout` + ретраи через cats-retry);
- `DocOp ~> IO` — эффектный интерпретатор DSL этапа 03: например, инструкция
  «разрешить FileSpec» читает/проверяет файл.

Это демонстрация главной ставки Free: сети добавляется **без изменения программ**.

### 6. Безопасность и живучесть

- таймауты на всех внешних вызовах; `Resource` для клиентов/серверов; graceful shutdown;
- ограничение размера тела запроса (XML-бомбы), лимиты очередей подписок;
- логирование через log4cats (структурированная трасса: channelId, refId).

## Задачи (пошагово)

1. Модуль `http`; `MediaType`-константы + `EntityEncoder/Decoder` для XJDF/XJMF (XML и JSON).
2. Минимальный сервер: `POST /submit`, `GET /devices`; интеграционный тест через
   ember на случайном порту (или `Client.fromHttpApp` без сокета).
3. Подписки: `Topic`-каналы, `GET/POST /subscribe` → `Stream[IO, Signal]`; тест доставки
   сигнала подписчику.
4. Клиентская обвязка + demo: submit XJDF → подписка → 2 сигнала → Response по refID.
5. HTTP-интерпретаторы `XjmfOp ~> IO` и `DocOp ~> IO`; тесты на таймаут и отмену.
6. Лимиты/таймауты/логи; README с примером запуска demo.

## Definition of Done

- [ ] Demo-сценарий проходит end-to-end в тесте (без внешней сети).
- [ ] Content-Type соответствуют Table 9.1; декодеры отвергают неверный MIME.
- [ ] Потоковая доставка сигналов работает; отмена подписки освобождает ресурсы.
- [ ] HTTP-интерпретаторы не меняют алгебры этапов 03/06 (только новые интерпретаторы).
- [ ] Таймауты и лимиты тела покрыты тестами.
- [ ] `sbt "clean ; compile ; test"` зелёный.

## Риски и альтернативы

- **Формат потоковой доставки** — спецификация не диктует HTTP-транспорт персистентных
  каналов; выберите прагматичный вариант (chunked-поток документов, возможно SSE для JSON)
  и задокументируйте как vendor-расширение, не претендуя на нормативность.
- **http4s 0.23 vs 1.x** — на момент старта сверьте мажорную версию; в эскизах — 0.23.x
  как стабильная линия экосистемы.
- **Интеграционные тесты на реальных сокетах** флакают в CI — предпочитайте
  `Client.fromHttpApp` (без сети), реальный сокет — только в одном smoke-тесте.
