package xjdf4s.http

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import scala.concurrent.duration.*

import org.http4s.{MediaType, Method, Request, Status}
import org.http4s.client.Client
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import org.http4s.server.middleware.EntityLimiter

import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.{DeviceInfo, Header, XJDF}

/**
 * The stage 07 scenarios, without sockets (per the stage risk note): the finite request/response pairs go
 * through `Client.fromHttpApp`, while the subscription stream is consumed over the hub's deterministic
 * `subscribeAwait` resource - its acquisition completes only after the subscriber is registered, so no signal
 * can be dropped by a publish race, and no cross-fiber timing is involved. The infinite stream is kept away
 * from `Client.fromHttpApp` because its body finalizer drains a channel that only closes when the stream ends
 * (see the framesOf note in XjdfClient).
 */
object XjdfServerChecks:

  private val jobId = Nmtoken.from("job-1").toOption.get
  private val process = Nmtoken.from("Product").toOption.get
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val channelId = Nmtoken.from("Q1").toOption.get
  private val messageType = Nmtoken.from("SignalStatus").toOption.get
  private val time = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
  private val url = UriRef.from("https://example.com/xjmfurl").toOption.get
  private val subscription = Subscription(url, channelMode = Vector(ChannelMode.Reliable))

  private def signalHeader(id: String): Header =
    Header(
      deviceId,
      time,
      id = Some(XsdId.from(id).toOption.get),
      refId = Some(channelId),
    )

  private def signal(id: String): SignalStatus =
    SignalStatus(signalHeader(id), DeviceInfo(DeviceStatus.Production), channelMode = Some(ChannelMode.Reliable))

  private def envelope(value: SignalStatus): XJMF =
    XJMF(value.header, NonEmptyVector.one(value), Some(Version.V2_2))

  private def statusQuery: QueryStatus =
    QueryStatus(
      header = Header(deviceId, time, id = Some(XsdId.from("Q1").toOption.get)),
      subscription = Some(subscription),
    )

  /**
   * Diagnostic guard: names the step that fails to complete instead of hanging the whole suite. racePair (not
   * race) is deliberate: cats-effect 3's race waits for the loser's cancellation, and a hung step with an
   * uncancelable region (like the fromHttpApp producer) would hang the guard itself. racePair returns as soon
   * as either side completes; when the timer wins, the hung step leaks but the test fails with its name.
   */
  private def step[A](name: String)(io: IO[A]): IO[A] =
    IO.racePair(IO.sleep(3.seconds), io).flatMap {
      case Left(_) => IO.raiseError[A](new RuntimeException(s"step timed out: $name"))
      // racePair's pair is (loser fiber, winner outcome): the step wins when its outcome is the second element
      case Right((_, outcome)) =>
        outcome match
          case cats.effect.kernel.Outcome.Succeeded(value) => value
          case cats.effect.kernel.Outcome.Errored(error)  => IO.raiseError(error)
          // self-cancel, typed: the continuation is unreachable because the cancellation raises on evaluation
          case cats.effect.kernel.Outcome.Canceled()      => IO.canceled.flatMap(_ => IO.never[A])
    }

  /** Diagnostic stage: submit over HTTP alone (Client.fromHttpApp with a finite body). */
  def submitOverHttp(): Unit =
    val document = XJDF(jobId, NonEmptyVector.one(process))
    val run: IO[ResponseSubmitQueueEntry] =
      for
        hub <- step("submit: hub.create")(XjdfHub.create)
        client = Client.fromHttpApp(XjdfServer.app(hub))
        receipt <- step("submit: client.submit")(XjdfClient.submit(client, document))
      yield receipt
    val receipt = run.unsafeRunSync()
    assert(receipt.returnCode.contains(0))
    assert(receipt.header.refId.contains(jobId))

  /** Diagnostic stage: subscription over HTTP alone. */
  def subscribeOverHttp(): Unit =
    val run: IO[ResponseStatus] =
      for
        hub <- step("subscribe: hub.create")(XjdfHub.create)
        client = Client.fromHttpApp(XjdfServer.app(hub))
        response <- step("subscribe: client.subscribeStatus")(XjdfClient.subscribeStatus(client, statusQuery))
      yield response
    val response = run.unsafeRunSync()
    assert(response.returnCode.contains(0))
    assert(response.header.refId.contains(channelId))

  /** Diagnostic stage: the hub delivery alone (no HTTP client involved). */
  def hubDelivery(): Unit =
    val firstSignal = signal("S1")
    val secondSignal = signal("S2")
    val run: IO[Vector[XJMF]] =
      for
        hub <- step("delivery: hub.create")(XjdfHub.create)
        _ <- step("delivery: openChannel")(hub.openChannel(subscription, channelId, messageType))
        resource <- step("delivery: subscribeAwait")(hub.subscribeAwait(channelId))
        frames <- resource match
          case Some(resource) =>
            step("delivery: two signals") {
              resource.use { stream =>
                for
                  fiber <- stream.map(hub.envelope).take(2).compile.toVector.start
                  _ <- hub.publish(firstSignal)
                  _ <- hub.publish(secondSignal)
                  delivered <- fiber.joinWithNever
                yield delivered
              }
            }
          case None => IO.raiseError(new AssertionError(s"channel '$channelId' was not opened"))
      yield frames
    val frames = run.unsafeRunSync()
    assert(frames == Vector(envelope(firstSignal), envelope(secondSignal)), s"frames: $frames")

  /** Task 2 + 4: the demo runs end-to-end — submit and subscribe over HTTP, then two signals over the hub. */
  def endToEndDemo(): Unit =
    val document = XJDF(jobId, NonEmptyVector.one(process))
    val firstSignal = signal("S1")
    val secondSignal = signal("S2")
    val run: IO[(ResponseSubmitQueueEntry, ResponseStatus, Vector[XJMF])] =
      for
        hub <- step("demo: hub.create")(XjdfHub.create)
        client = Client.fromHttpApp(XjdfServer.app(hub))
        receipt <- step("demo: client.submit")(XjdfClient.submit(client, document))
        response <- step("demo: client.subscribeStatus")(XjdfClient.subscribeStatus(client, statusQuery))
        resource <- step("demo: hub.subscribeAwait")(hub.subscribeAwait(channelId))
        frames <- resource match
          case Some(resource) =>
            step("demo: deliver-two-signals") {
              resource.use { stream =>
                for
                  fiber <- stream.map(hub.envelope).take(2).compile.toVector.start
                  _ <- hub.publish(firstSignal)
                  _ <- hub.publish(secondSignal)
                  delivered <- fiber.joinWithNever
                yield delivered
              }
            }
          case None => IO.raiseError(new AssertionError(s"channel '$channelId' was not opened"))
      yield (receipt, response, frames)
    val (receipt, response, frames) = run.unsafeRunSync()
    assert(receipt.returnCode.contains(0))
    assert(receipt.header.refId.contains(jobId))
    assert(response.returnCode.contains(0))
    assert(response.header.refId.contains(channelId))
    assert(frames == Vector(envelope(firstSignal), envelope(secondSignal)), s"frames: $frames")

  /** The subscription stream route serves an open channel with the documented vendor framing media type. */
  def streamRoute(): Unit =
    val run: IO[(Status, Option[MediaType])] =
      for
        hub <- XjdfHub.create
        _ <- hub.openChannel(subscription, channelId, messageType)
        response <- XjdfServer.app(hub).run(Request[IO](Method.GET, uri"/channels" / channelId.value / "signals"))
      yield (response.status, response.headers.get[`Content-Type`].map(_.mediaType))
    val (status, mediaType) = run.unsafeRunSync()
    assert(status == Status.Ok)
    assert(
      mediaType.exists(mt => mt.mainType == XjdfMediaTypes.xjmfJson.mainType && mt.subType == XjdfMediaTypes.xjmfJson.subType),
      s"media type: $mediaType",
    )

  /** Task 2 + 6: `POST /subscriptions/stop` closes the channel; the stream endpoint then answers 404. */
  def stopChannel(): Unit =
    val run: IO[(ResponseStopPersistentChannel, Status)] =
      for
        hub <- XjdfHub.create
        client = Client.fromHttpApp(XjdfServer.app(hub))
        _ <- XjdfClient.subscribeStatus(client, statusQuery)
        stopResponse <- client.expect[ResponseStopPersistentChannel](
          Request[IO](Method.POST, uri"/subscriptions/stop").withEntity(
            CommandStopPersistentChannel(
              Header(deviceId, time, id = Some(XsdId.from("C1").toOption.get)),
              StopPersistentChannelParams(channelId = Some(channelId)),
            ),
          )(using XjdfMessageEntities.commandStopPersistentChannelEncoder),
        )(using XjdfMessageEntities.responseStopPersistentChannelDecoder)
        streamStatus <- client.status(Request[IO](Method.GET, uri"/channels" / channelId.value / "signals"))
      yield (stopResponse, streamStatus)
    val (stopResponse, streamStatus) = run.unsafeRunSync()
    assert(stopResponse.returnCode.contains(0))
    assert(streamStatus == Status.NotFound)

  /** Task 6: the EntityLimiter middleware raises EntityTooLarge once a body over the limit is actually read. */
  def bodyLimit(): Unit =
    val run: IO[Unit] =
      for
        hub <- XjdfHub.create
        limited = XjdfServer.limitedApp(hub, limit = 64)
        // the XJDF media type lets the strict decoder start reading the body, so the limiter trips on it
        request = Request[IO](Method.POST, uri"/submit")
          .withContentType(`Content-Type`(XjdfMediaTypes.xjdfXml))
          .withEntity("x" * 1024)
        _ <- limited.run(request).void
      yield ()
    // in http4s 0.23 the middleware raises EntityTooLarge (it does not fabricate a 413 response itself)
    run.attempt.unsafeRunSync() match
      case Left(_: EntityLimiter.EntityTooLarge) => ()
      case other                                => assert(false, s"expected EntityTooLarge, got $other")
end XjdfServerChecks
