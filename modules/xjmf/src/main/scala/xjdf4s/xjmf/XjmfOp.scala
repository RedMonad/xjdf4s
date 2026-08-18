package xjdf4s.xjmf

import cats.Functor
import cats.free.Free
import xjdf4s.core.*
import xjdf4s.messaging.*

/** Free-monadic grammar of the XJMF transport (stage 06). A program of this grammar is pure data: it describes
 *  *what* the exchange does — open a persistent channel, deliver a signal, wait for its response, list the
 *  channels — and an interpreter decides *how* (the deterministic channel machine or the event trace; the HTTP
 *  runtime arrives in stage 07). The design rationale (Free for scenarios, tagless for capabilities) is fixed in
 *  `docs/free-dsl.md` and in the module README.
 *
 *  Normative anchors: the persistent channel is the subscription of 9.6.2; `@ChannelID` SHALL equal the
 *  `Header/@ID` of the Query that initiated the subscription and SHALL match `Header/@refID` of every signal on
 *  the channel (Table 8.71); responses correlate through `Header/@refID` (9.6.1).
 */
enum XjmfOp[A]:
  /** Open (or replace, 9.6.3) the persistent channel identified by the query `Header/@ID` (Table 8.71). */
  case OpenChannel(subscription: Subscription, channelId: Nmtoken, messageType: Nmtoken) extends XjmfOp[Unit]

  /** Delete the persistent channel (9.6.6): the entry stays visible as `Closed` for observability. */
  case CloseChannel(channelId: Nmtoken) extends XjmfOp[Unit]

  /** A signal arrives on the channel identified by `signal.header.refId` (Table 8.71). */
  case Deliver(signal: Signal) extends XjmfOp[Unit]

  /** A response arrives; `response.header.refId` identifies the message it answers (a signal or the query). */
  case DeliverResponse(response: Response) extends XjmfOp[Unit]

  /** The response that answers the message with `Header/@ID` equal to `answeredId`, if it has arrived. */
  case AwaitResponse(answeredId: Nmtoken) extends XjmfOp[Option[Response]]

  /** The active persistent channels as `SubscriptionInfo` elements (QueryKnownSubscriptions, 9.6.3). */
  case Channels extends XjmfOp[Vector[SubscriptionInfo]]
end XjmfOp

object XjmfOp:
  /** No constructor stores a value of its result type: the four effectful cases yield `Unit` and the two queries
   *  take no values, so `map` is the identity on the instruction itself. The cast is safe and total — the GADT
   *  has no case whose type argument is anything other than `Unit`, `Option[Response]` or
   *  `Vector[SubscriptionInfo]` fixed by construction, hence every `XjmfOp[A]` value is also an `XjmfOp[B]`
   *  value. (The same rationale as the `DocOp` functor of stage 03; a match-based version cannot be typed.)
   */
  given Functor[XjmfOp] with
    def map[A, B](fa: XjmfOp[A])(f: A => B): XjmfOp[B] =
      fa.asInstanceOf[XjmfOp[B]]

/** An XJMF exchange program: pure data, executed by an interpreter (see the `XjmfInterpreters` object). */
type Xjmf[A] = Free[XjmfOp, A]

object Xjmf:

  def openChannel(subscription: Subscription, channelId: Nmtoken, messageType: Nmtoken): Xjmf[Unit] =
    Free.liftF(XjmfOp.OpenChannel(subscription, channelId, messageType))

  def closeChannel(channelId: Nmtoken): Xjmf[Unit] =
    Free.liftF(XjmfOp.CloseChannel(channelId))

  def deliver(signal: Signal): Xjmf[Unit] =
    Free.liftF(XjmfOp.Deliver(signal))

  def deliverResponse(response: Response): Xjmf[Unit] =
    Free.liftF(XjmfOp.DeliverResponse(response))

  def awaitResponse(answeredId: Nmtoken): Xjmf[Option[Response]] =
    Free.liftF(XjmfOp.AwaitResponse(answeredId))

  def channels: Xjmf[Vector[SubscriptionInfo]] =
    Free.liftF(XjmfOp.Channels)
end Xjmf
