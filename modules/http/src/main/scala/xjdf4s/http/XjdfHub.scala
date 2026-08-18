package xjdf4s.http

import cats.data.Chain
import cats.effect.{IO, Ref}
import fs2.Stream
import fs2.concurrent.Topic

import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.XJDF
import xjdf4s.xjmf.{TransportEvent, XjmfState, XjmfInterpreters, XjmfOp}

/**
 * In-memory bridge between the stage 06 channel machine and the network: every persistent channel is an fs2
 * `Topic` of signals, and every transition runs through the stage 06 core, so the channel machine remains the
 * single source of protocol truth. The HTTP layer never re-implements 9.6.
 */
object XjdfHub:

  def create: IO[XjdfHub] =
    for
      state <- Ref.of[IO, XjmfState](XjmfState.empty)
      topics <- Ref.of[IO, Map[Nmtoken, Topic[IO, Signal]]](Map.empty)
      submitted <- Ref.of[IO, Vector[XJDF]](Vector.empty)
    yield new XjdfHub(state, topics, submitted)
end XjdfHub

final class XjdfHub private (
    state: Ref[IO, XjmfState],
    topics: Ref[IO, Map[Nmtoken, Topic[IO, Signal]]],
    submitted: Ref[IO, Vector[XJDF]],
):

  /** The stage 06 machine state, exposed for observability and the IO interpreter. */
  def currentState: IO[XjmfState] = state.get

  def submit(document: XJDF): IO[Unit] = submitted.update(_ :+ document)

  def submittedDocuments: IO[Vector[XJDF]] = submitted.get

  /** 9.6.2: opens (or replaces, 9.6.3) the persistent channel and registers its signal topic. */
  def openChannel(subscription: Subscription, channelId: Nmtoken, messageType: Nmtoken): IO[Unit] =
    for
      topic <- Topic[IO, Signal]
      _ <- topics.update(_ + (channelId -> topic))
      _ <- runOp(XjmfOp.OpenChannel(subscription, channelId, messageType))
    yield ()

  /** 9.6.6: closes the channel and releases its topic (the stage 06 entry stays visible as Closed). */
  def closeChannel(channelId: Nmtoken): IO[Unit] =
    for
      _ <- topics.update(_ - channelId)
      _ <- runOp(XjmfOp.CloseChannel(channelId))
    yield ()

  /**
   * A signal arrives: it is routed and journaled by the stage 06 core (replacement windows included), then
   * published to the channel topic. Unrouted signals only produce their stage 06 event.
   */
  def publish(signal: Signal): IO[Unit] =
    for
      events <- runOp(XjmfOp.Deliver(signal))
      delivered = events.exists {
        case TransportEvent.SignalDelivered(_, _) => true
        case _                                    => false
      }
      _ <-
        if delivered then
          signal.header.refId match
            case Some(channelId) =>
              topics.get.flatMap {
                _.get(channelId) match
                  case Some(topic) => topic.publish1(signal).void
                  case None        => IO.unit
              }
            case None => IO.unit
        else IO.unit
    yield ()

  /** The stream of a channel's signals; `None` when the channel is unknown or closed. */
  def signals(channelId: Nmtoken): IO[Option[Stream[IO, Signal]]] =
    topics.get.map(_.get(channelId).map(_.subscribe(16)))

  /**
   * The delivery handshake: completes when at least one subscriber is attached to the channel's topic (fs2's
   * own subscriber count). fs2 `publish1` DROPS an element published while nobody is subscribed, so a producer
   * MUST await this before the first publish - otherwise a signal raced against the subscriber registration is
   * silently lost and a waiting `take(1)` stream hangs forever.
   */
  def awaitingSubscriber(channelId: Nmtoken): IO[Unit] =
    topics.get.flatMap {
      _.get(channelId) match
        case Some(topic) => topic.subscribers.exists(_ > 0).compile.drain
        case None        => IO.unit
    }

  /** The subscription handshake for the `/status/subscribe` endpoint: opens the channel and answers (9.6.2). */
  def subscribeStatus(query: QueryStatus): IO[Option[ResponseStatus]] =
    (query.header.id, query.subscription) match
      case (Some(queryId), Some(subscription)) =>
        Nmtoken.from(queryId.value) match
          case Right(channelId) =>
            for
              _ <- openChannel(subscription, channelId, StatusMessageType)
              response <- IO.pure(
                ResponseStatus(
                  XjdfHttp.responseHeader(query.header.deviceId, channelId, s"r-${queryId.value}"),
                  returnCode = Some(0),
                ),
              )
            yield Some(response)
          case Left(_) => IO.pure(None)
      case _ => IO.pure(None)

  /** The XJMF envelope of a signal, as delivered on the subscription stream. */
  def envelope(signal: Signal): XJMF =
    XJMF(signal.header, NonEmptyVector.one(signal), Some(Version.V2_2))

  private val StatusMessageType: Nmtoken = Nmtoken.from("SignalStatus").toOption.get

  private def runOp[A](op: XjmfOp[A]): IO[Chain[TransportEvent]] =
    state.modify { current =>
      val (next, events, _) = XjmfInterpreters.transition(op, current)
      (next.copy(events = next.events ++ events), events)
    }
end XjdfHub
