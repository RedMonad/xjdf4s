# xjdf4s-http — effects and the HTTP runtime (stage 07)

The only module where `IO` lives: `model`, `dsl`, `xjmf` and the codecs stay pure. The stage 04/05
codecs become `EntityEncoder`/`EntityDecoder`, the stage 06 channel machine is bridged to `fs2.Topic`
streams, and the stage 03/06 Free algebras gain effectful interpreters — **without being changed**.

## Demo scenario

```scala
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.client.Client

val hub = XjdfHub.create.unsafeRunSync()      // in-memory channel machine + signal topics
val app = XjdfServer.app(hub)                 // the Table 9.5 device surface
val client = Client.fromHttpApp(app)          // no sockets (see the note below)

val program: IO[Unit] =
  for
    receipt  <- XjdfClient.submit(client, document)          // POST /submit, XJDF XML in
    response <- XjdfClient.subscribeStatus(client, query)    // POST /status/subscribe (9.6.2)
    _ <- hub.subscribeAwait(channelId).use { stream =>       // deterministic subscriber
           stream.map(hub.envelope).take(2).compile.drain     // two SignalStatus envelopes
             .start.flatMap { fiber =>
               hub.publish(signal("S1")) *> hub.publish(signal("S2")) *> fiber.joinWithNever
             }
         }
  yield ()
```

Run it with `program.unsafeRunSync()` in a scratch, or mount `app` in `IOApp` with an ember server for
a real socket (the one place `IO.sleep`-backed timeouts are exercised — see the timers note).

## The pieces

| File | Role |
|---|---|
| `XjdfMediaTypes` | the six Table 9.1 media types (XML variants with an explicit UTF-8 charset) |
| `XjdfEntities` | `EntityEncoder`/`EntityDecoder` for XJDF/XJMF over the stage 04/05 codecs, plus generic JSON entity codecs for single message bodies. Named values, not givens (two representations per type), and **strict** media-type checking — wrong MIME types fail with `MediaTypeMismatch`/`MediaTypeMissing` (http4s only uses declared types when chaining decoders) |
| `XjdfMultipart` | the 9.10.4.2 packaging: `"xjmf"` field plus `"attachment"` parts (Example 9.13) |
| `XjdfServer` | `POST /submit`, `POST /status/subscribe`, `GET /channels/{id}/signals`, `POST /subscriptions/stop`, `GET /devices`; `limitedApp` wires the `EntityLimiter` |
| `XjdfClient` | submit, subscription and the signals stream; `framesOf`/`decodeFrame` for the JSON-Lines framing |
| `XjdfHub` | the in-memory bridge: every transition runs through the stage 06 core (`XjmfInterpreters.transition`), signals are published to per-channel `fs2.Topic`s; `subscribeAwait` is the deterministic subscription primitive (acquisition completes only after registration, so a producer can never race it) |
| `XjdfIoInterpreters` | `XjmfOp ~> IO` (stateful core + delivery hook + `Deferred` correlation + **injectable** await timer) and `DocOp ~> IO` |

## Documented vendor choices and limitations

- **Subscription framing**: the specification does not define an HTTP framing for persistent channels, so
  the stream endpoint serves one JSON XJMF envelope per line over a chunked response. The stage 05
  exactly-one-message exception keeps every line independently decodable. A vendor extension, not a
  normative claim.
- **`Client.fromHttpApp` and infinite streams**: the in-memory client pumps response bodies through a
  synchronous channel and its finalizer drains that channel, which only closes when the producer ends —
  with an infinite subscription stream the finalizer deadlocks. Finite request/response pairs are fine;
  tests consume infinite streams over a direct `app.run` response (`framesOf`) or the hub. Socket-backed
  clients (ember) are unaffected.
- **Timers**: the transport's await timer is injected (`sleep: FiniteDuration => IO[Unit]`, default
  `IO.sleep`). This keeps the deterministic tests independent of wall-clock scheduling (instant / never
  timers) — and the stage 07 test environment has shown that `IO.sleep` itself may not fire under
  `unsafeRunSync` inside munit, so no test relies on it. Production code and the ember smoke test run on a
  real `IORuntime`.
- **Body limit**: `EntityLimiter` raises `EntityTooLarge` while the body is read (it does not fabricate a
  413); wire a recovery middleware in the app layer to translate it into the HTTP status you want.
- **Logging**: the structured trace is the stage 06 event log (`TransportEvent` carries channelId/refId) —
  `XjdfHub.currentState.events` is the ready-made source for a log4cats sink in stage 08.
