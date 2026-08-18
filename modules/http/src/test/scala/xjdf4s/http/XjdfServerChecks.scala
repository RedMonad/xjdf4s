package xjdf4s.http

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import org.http4s.client.Client
import org.http4s.{Method, Request, Status}
import org.http4s.implicits.*

import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.{DeviceInfo, Header, XJDF}

/**
 * The end-to-end demo of stage 07, over `Client.fromHttpApp` (no sockets, per the stage risk note): submit an
 * XJDF, subscribe for status, receive signals on the stream, stop the channel, and the body-limit middleware.
 */
object XjdfServerChecks:

  private val jobId = Nmtoken.from("job-1").toOption.get
  private val process = Nmtoken.from("Product").toOption.get
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val channelId = Nmtoken.from("Q1").toOption.get
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

  /** One signal frame delivered over the subscription stream (direct app.run: see the framesOf note). */
  private def deliverFrame(hub: XjdfHub, signalToDeliver: SignalStatus): IO[Vector[XJMF]] =
    for
      fiber <- XjdfServer
        .app(hub)
        .run(Request[IO](Method.GET, uri"/channels" / channelId.value / "signals"))
        .flatMap(response => XjdfClient.framesOf(response).take(1).compile.toVector)
        .start
      // fs2 publish races a subscriber that has not registered yet, so the producer awaits the handshake
      _ <- hub.awaitingSubscriber(channelId)
      _ <- hub.publish(signalToDeliver)
      frame <- fiber.joinWithNever
    yield frame

  /** Task 2 + 4: the demo runs end-to-end — submit, subscribe, two delivered signals, refID correlation. */
  val endToEndDemo: Unit =
    val document = XJDF(jobId, NonEmptyVector.one(process))
    val query = QueryStatus(
      header = Header(deviceId, time, id = Some(XsdId.from("Q1").toOption.get)),
      subscription = Some(subscription),
    )
    val firstSignal = signal("S1")
    val secondSignal = signal("S2")
    val run: IO[(ResponseSubmitQueueEntry, ResponseStatus, Vector[XJMF], Vector[XJMF])] =
      for
        hub <- XjdfHub.create
        // Client.fromHttpApp handles the finite request/response pairs; the infinite subscription stream is
        // consumed via a direct app.run (the fromHttpApp body finalizer drains a channel that only closes
        // when the infinite stream ends - see the framesOf note in XjdfClient)
        client = Client.fromHttpApp(XjdfServer.app(hub))
        receipt <- XjdfClient.submit(client, document)
        response <- XjdfClient.subscribeStatus(client, query)
        first <- deliverFrame(hub, firstSignal)
        second <- deliverFrame(hub, secondSignal)
      yield (receipt, response, first, second)
    val (receipt, response, first, second) = run.unsafeRunSync()
    assert(receipt.returnCode.contains(0))
    assert(receipt.header.refId.contains(jobId))
    assert(response.returnCode.contains(0))
    assert(response.header.refId.contains(channelId))
    assert(first == Vector(envelope(firstSignal)), s"first frame: $first")
    assert(second == Vector(envelope(secondSignal)), s"second frame: $second")

  /** Task 2 + 6: `POST /subscriptions/stop` closes the channel; the stream endpoint then answers 404. */
  val stopChannel: Unit =
    val query = QueryStatus(
      header = Header(deviceId, time, id = Some(XsdId.from("Q1").toOption.get)),
      subscription = Some(subscription),
    )
    val run: IO[(ResponseStopPersistentChannel, Status)] =
      for
        hub <- XjdfHub.create
        client = Client.fromHttpApp(XjdfServer.app(hub))
        _ <- XjdfClient.subscribeStatus(client, query)
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

  /** Task 6: the EntityLimiter middleware rejects oversized bodies with 413. */
  val bodyLimit: Unit =
    val run: IO[Status] =
      for
        hub <- XjdfHub.create
        limited = XjdfServer.limitedApp(hub, limit = 64)
        status <- limited.run(Request[IO](Method.POST, uri"/submit").withEntity("x" * 1024)).map(_.status)
      yield status
    assert(run.unsafeRunSync() == Status.PayloadTooLarge)
end XjdfServerChecks
