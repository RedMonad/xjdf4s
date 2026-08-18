package xjdf4s.xjmf

import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.Header

/** The stage 06 scenarios, executed by both interpreters: the subscription handshake (9.6.2), the refID
 *  correlation chain (9.6.1, Table 8.71), the SignalResource replacement windows (Table 7.54), the
 *  ChannelMode semantics (9.6.4/9.6.5) and the event trace.
 */
object XjmfTransportChecks:

  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val channelId = Nmtoken.from("Q1").toOption.get
  private val signalS1 = Nmtoken.from("S1").toOption.get
  private val messageType = Nmtoken.from("SignalResource").toOption.get
  private val url = UriRef.from("https://example.com/xjmfurl").toOption.get
  private val subscription = Subscription(url, channelMode = Vector(ChannelMode.Reliable))

  private def header(id: String, refId: Option[String], time: String = "2026-08-17T12:00:00+03:00"): Header =
    Header(
      deviceId,
      XsdDateTime.from(time).toOption.get,
      id = Some(XsdId.from(id).toOption.get),
      refId = refId.map(value => Nmtoken.from(value).toOption.get),
    )

  private def resourceSignal(
      id: String,
      time: String,
      replaceAfter: Option[String] = None,
      replaceBefore: Option[String] = None,
      mode: Option[ChannelMode] = Some(ChannelMode.Reliable),
  ): SignalResource =
    SignalResource(
      header = header(id, refId = Some(channelId.value), time = time),
      replaceAfter = replaceAfter.map(value => XsdDateTime.from(value).toOption.get),
      replaceBefore = replaceBefore.map(value => XsdDateTime.from(value).toOption.get),
      channelMode = mode,
    )

  private val initialResponse = ResponseResource(header("R1", refId = Some("S1")), returnCode = Some(0))

  /** Task 3: subscription -> signal -> response; the correlation goes Query Q1 -> Signal S1 (refID=Q1) -> Response R1 (refID=S1). */
  val subscriptionScenario: Unit =
    val program: Xjmf[(Vector[SubscriptionInfo], Option[Response], Option[Response])] =
      for
        _             <- Xjmf.openChannel(subscription, channelId, messageType)
        channels      <- Xjmf.channels
        _             <- Xjmf.deliver(resourceSignal("S1", "2026-08-17T12:00:00+03:00"))
        awaitedBefore <- Xjmf.awaitResponse(signalS1)
        _             <- Xjmf.deliverResponse(initialResponse)
        awaitedAfter  <- Xjmf.awaitResponse(signalS1)
      yield (channels, awaitedBefore, awaitedAfter)
    val finalState = XjmfInterpreters.run(program)
    val (channels, awaitedBefore, awaitedAfter) =
      program.foldMap(XjmfInterpreters.stateful).runA(XjmfState.empty).value
    assert(channels == Vector(SubscriptionInfo(channelId, messageType, subscription)))
    assert(awaitedBefore.isEmpty, "a reliable signal without an answer is not answered yet")
    assert(awaitedAfter.contains(initialResponse))
    assert(finalState.pending.isEmpty, "the response closed the pending wait")
    assert(
      finalState.events.toList ==
        List(
          TransportEvent.ChannelOpened(channelId, messageType),
          TransportEvent.SignalDelivered(channelId, "S1"),
          TransportEvent.ResponseReceived("S1"),
        ),
    )

  /** Task 3: the initial response to the subscription query is observable too (9.6.2: the receiver SHALL answer first). */
  val initialQueryResponse: Unit =
    val program: Xjmf[Option[Response]] =
      for
        _       <- Xjmf.openChannel(subscription, channelId, messageType)
        _       <- Xjmf.deliverResponse(ResponseResource(header("R0", refId = Some("Q1"))))
        awaited <- Xjmf.awaitResponse(channelId)
      yield awaited
    val awaited = program.foldMap(XjmfInterpreters.stateful).runA(XjmfState.empty).value
    assert(awaited.nonEmpty)
    assert(awaited.get.header.refId.contains(channelId))

  /** Task 4: three signals, the middle one carrying a replacement window (Table 7.54: strictly between the bounds). */
  val replacementWindows: Unit =
    val program: Xjmf[Unit] =
      for
        _ <- Xjmf.openChannel(subscription, channelId, messageType)
        _ <- Xjmf.deliver(resourceSignal("A", "2026-08-17T10:01:00+03:00"))
        _ <- Xjmf.deliver(resourceSignal(
          "B",
          "2026-08-17T10:05:00+03:00",
          replaceAfter = Some("2026-08-17T10:00:00+03:00"),
          replaceBefore = Some("2026-08-17T10:06:00+03:00")
        ))
        _ <- Xjmf.deliver(resourceSignal("C", "2026-08-17T10:06:00+03:00"))
      yield ()
    val finalState = XjmfInterpreters.run(program)
    val deliveredIds = finalState.delivered.toList.map { case (_, signal) =>
      signal.header.id.map(_.value).getOrElse("")
    }
    assert(deliveredIds == List("B", "C"), s"the window retired A, journal is $deliveredIds")
    assert(
      finalState.events.toList.contains(TransportEvent.SignalsReplaced(channelId, by = "B", replaced = Vector("A"))),
    )

  /** Task 5: a Reliable signal without an answer stays observable; FireAndForget never becomes pending (9.6.4/9.6.5). */
  val channelModes: Unit =
    val program: Xjmf[Unit] =
      for
        _ <- Xjmf.openChannel(subscription, channelId, messageType)
        _ <- Xjmf.deliver(resourceSignal("S1", "2026-08-17T12:00:00+03:00"))
        _ <- Xjmf.deliver(resourceSignal("S2", "2026-08-17T12:01:00+03:00", mode = Some(ChannelMode.FireAndForget)))
      yield ()
    val finalState = XjmfInterpreters.run(program)
    assert(finalState.pending.toList.map(_.signalId) == List("S1"), "only the reliable signal awaits an answer")

  /** Task 6 + Definition of Done: the stateful and the traced interpreters agree on the trace, by construction. */
  val tracesAgree: Unit =
    val program: Xjmf[Unit] =
      for
        _ <- Xjmf.openChannel(subscription, channelId, messageType)
        _ <- Xjmf.deliver(resourceSignal("A", "2026-08-17T10:01:00+03:00"))
        _ <- Xjmf.deliver(resourceSignal(
          "B",
          "2026-08-17T10:05:00+03:00",
          replaceAfter = Some("2026-08-17T10:00:00+03:00"),
          replaceBefore = Some("2026-08-17T10:06:00+03:00")
        ))
        _ <- Xjmf.deliverResponse(ResponseResource(header("R2", refId = Some("B"))))
        _ <- Xjmf.closeChannel(channelId)
      yield ()
    val stateRun = XjmfInterpreters.run(program)
    val traceRun = XjmfInterpreters.trace(program)
    assert(stateRun.events.toList == traceRun.toList)
    assert(
      traceRun.toList ==
        List(
          TransportEvent.ChannelOpened(channelId, messageType),
          TransportEvent.SignalDelivered(channelId, "A"),
          TransportEvent.SignalsReplaced(channelId, by = "B", replaced = Vector("A")),
          TransportEvent.SignalDelivered(channelId, "B"),
          TransportEvent.ResponseReceived("B"),
          TransportEvent.ChannelClosed(channelId),
        ),
    )

  /** Routing failures are observable: no refID, unknown refID, delivery on a closed channel (9.6.6). */
  val routingFailures: Unit =
    val unknownRef = Nmtoken.from("QX").toOption.get
    val program: Xjmf[Unit] =
      for
        _ <- Xjmf.openChannel(subscription, channelId, messageType)
        _ <- Xjmf.deliver(SignalResource(header = header("N1", refId = None)))
        _ <- Xjmf.deliver(SignalResource(header = header("N2", refId = Some(unknownRef.value))))
        _ <- Xjmf.deliver(resourceSignal("N3", "2026-08-17T12:00:00+03:00"))
        _ <- Xjmf.closeChannel(channelId)
        _ <- Xjmf.closeChannel(channelId)
        _ <- Xjmf.deliver(resourceSignal("N4", "2026-08-17T12:02:00+03:00"))
      yield ()
    val finalState = XjmfInterpreters.run(program)
    // N1 has no refID and N2 references an unknown channel; N3 is routed while the channel is open;
    // N4 arrives after the channel was closed (the second close is a no-op and emits nothing).
    assert(
      finalState.events.toList ==
        List(
          TransportEvent.ChannelOpened(channelId, messageType),
          TransportEvent.Unrouted("N1"),
          TransportEvent.Unrouted("N2"),
          TransportEvent.SignalDelivered(channelId, "N3"),
          TransportEvent.ChannelClosed(channelId),
          TransportEvent.ChannelNotOpen(channelId, "N4"),
        ),
    )

  /** 9.6.3: reopening the same channel replaces the subscription instead of duplicating the channel. */
  val reopenReplaces: Unit =
    val otherSubscription = subscription.copy(repeatTime = Some(30.0f))
    val program: Xjmf[Unit] =
      for
        _ <- Xjmf.openChannel(subscription, channelId, messageType)
        _ <- Xjmf.openChannel(otherSubscription, channelId, messageType)
      yield ()
    val finalState = XjmfInterpreters.run(program)
    assert(finalState.channels.size == 1)
    assert(finalState.channels(channelId) == ChannelState.Subscribed(messageType, otherSubscription))
end XjmfTransportChecks
