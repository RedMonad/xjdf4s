package xjdf4s.http

import cats.~>
import cats.data.Chain
import cats.effect.{Deferred, IO, Ref}

import scala.concurrent.duration.FiniteDuration

import xjdf4s.dsl.{DocInterpreters, DocOp}
import xjdf4s.messaging.*
import xjdf4s.model.XJDF
import xjdf4s.xjmf.{TransportEvent, XjmfState, XjmfInterpreters, XjmfOp}

/**
 * The effectful interpreters of stage 07 — the only place `IO` lives. The stage 03 and 06 algebras are NOT
 * touched: these are new interpreters over the existing operations, which is the whole point of the Free
 * design. The transport interpreter reuses the stage 06 transition core as its in-memory state machine and
 * adds the effectful parts: signal delivery (`send`), response correlation via `Deferred`, and the await
 * timeout with cancellation cleanup.
 */
object XjdfIoInterpreters:

  /**
   * `XjmfOp ~> IO`: the HTTP-shaped transport. `send` is the effectful delivery hook (an HTTP POST in
   * production, a recording ref in tests); `AwaitResponse` waits on a `Deferred` with `awaitTimeout` and frees
   * its registration on timeout, completion or cancellation.
   */
  final class Transport(
      state: Ref[IO, XjmfState],
      waits: Ref[IO, Map[String, Deferred[IO, Response]]],
      send: Signal => IO[Unit],
      awaitTimeout: FiniteDuration,
  ):

    val interpreter: XjmfOp ~> IO = new (XjmfOp ~> IO):
      def apply[A](op: XjmfOp[A]): IO[A] =
        op match
          case _: XjmfOp.OpenChannel | _: XjmfOp.CloseChannel =>
            runTransition(op).void
          case deliver: XjmfOp.Deliver =>
            for
              events <- runTransition(deliver)
              delivered = events.exists {
                case TransportEvent.SignalDelivered(_, _) => true
                case _                                    => false
              }
              _ <- if delivered then send(deliver.signal) else IO.unit
            yield ()
          case deliverResponse: XjmfOp.DeliverResponse =>
            for
              _ <- runTransition(deliverResponse)
              _ <- deliverResponse.response.header.refId match
                case Some(refId) => completeWaiter(refId.value, deliverResponse.response)
                case None        => IO.unit
            yield ()
          case await: XjmfOp.AwaitResponse =>
            for
              current <- state.get
              found = current.responses.collectFirst {
                case (answeredId, stored) if answeredId == await.answeredId.value => stored
              }
              result <- found.fold(awaitNewResponse(await.answeredId.value))(stored => IO.pure(Some(stored)))
            yield result
          case XjmfOp.Channels =>
            state.get.map(current => XjmfInterpreters.transition(op, current)._3)

    private def awaitNewResponse(responseId: String): IO[Option[Response]] =
      Deferred[IO, Response].flatMap { deferred =>
        waits.update(_ + (responseId -> deferred)) *>
          deferred.get
            .map(Some(_))
            .timeoutTo(awaitTimeout, IO.pure(None))
            .guarantee(waits.update(_ - responseId))
      }

    private def runTransition[A](operation: XjmfOp[A]): IO[Chain[TransportEvent]] =
      state.modify { current =>
        val (next, events, _) = XjmfInterpreters.transition(operation, current)
        (next.copy(events = next.events ++ events), events)
      }

    private def completeWaiter(answeredId: String, response: Response): IO[Unit] =
      waits.get.flatMap {
        _.get(answeredId) match
          case Some(deferred) => deferred.complete(response).void
          case None           => IO.unit
      }

    /** The number of registered awaiters — observability for the cancellation test. */
    def waitingCount: IO[Int] = waits.get.map(_.size)
  end Transport

  /** Creates the transport interpreter: state, the delivery hook and the await timeout. */
  def transport(
      state: Ref[IO, XjmfState],
      send: Signal => IO[Unit],
      awaitTimeout: FiniteDuration,
  ): IO[Transport] =
    Ref.of[IO, Map[String, Deferred[IO, Response]]](Map.empty).map { waits =>
      new Transport(state, waits, send, awaitTimeout)
    }

  /** `DocOp ~> IO`: the effectful document builder — the stage 03 step logic, folded into a `Ref`. */
  def document(ref: Ref[IO, XJDF]): DocOp ~> IO = new (DocOp ~> IO):
    def apply[A](op: DocOp[A]): IO[A] =
      // DocOp's GADT has no case whose result type is anything but Unit (the stage 03 functor instance relies
      // on the same fact), so the IO[Unit] state update is safely an IO[A].
      ref.update(current => DocInterpreters.buildDocument(op).runS(current).value).asInstanceOf[IO[A]]
end XjdfIoInterpreters
