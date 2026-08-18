package xjdf4s.xjmf

import cats.data.Chain
import xjdf4s.core.*
import xjdf4s.messaging.*

/** A reliable signal that has been delivered but not yet answered. `signalId` is the `Header/@ID` of the signal
 *  (SHOULD be present per Table 7.3; an absent ID yields the empty string and the signal cannot be correlated).
 *  Normative anchor: 9.6.5 — a reliable signal SHALL be resent until it is answered; this pending record is the
 *  observable state the stage 07 runtime will attach timers to.
 */
final case class PendingSignal(
    channelId: Nmtoken,
    signalId: String,
    signal: Signal,
)

/** The lifecycle of a persistent channel (9.6.2, 9.6.3, 9.6.6). A closed channel stays in the channel map as
 *  `Closed` so that a delivery attempt on it is observable as a `ChannelNotOpen` event instead of being silently
 *  dropped. `messageType` is the `@MessageType` of Table 8.71: the local element name of the channel's signals.
 */
enum ChannelState derives CanEqual:
  case Subscribed(messageType: Nmtoken, subscription: Subscription)
  case Closed

/** One entry of the exchange trace. The trace is a flat, chronological list of what happened on the transport —
 *  it is the material both interpreters must agree on.
 */
enum TransportEvent derives CanEqual:
  /** A persistent channel was opened (or replaced, 9.6.3). */
  case ChannelOpened(channelId: Nmtoken, messageType: Nmtoken)

  /** A persistent channel was closed (9.6.6); closing twice is idempotent and emits nothing. */
  case ChannelClosed(channelId: Nmtoken)

  /** A signal was routed to its channel (Table 8.71: via `Header/@refID`). */
  case SignalDelivered(channelId: Nmtoken, signalId: String)

  /** The replacement window of a `SignalResource` retired previous signals of the channel (Table 7.54). */
  case SignalsReplaced(channelId: Nmtoken, by: String, replaced: Vector[String])

  /** A response arrived; `refId` is the `Header/@ID` of the message it answers. */
  case ResponseReceived(refId: String)

  /** The signal has no `Header/@refID` or the referenced query never opened a channel. */
  case Unrouted(signalId: String)

  /** The referenced channel exists but is `Closed`. */
  case ChannelNotOpen(channelId: Nmtoken, signalId: String)
end TransportEvent

/** The full state of the in-memory transport machine:
 *
 *  - `channels` — the persistent channels by `@ChannelID` (the initiating query's `Header/@ID`, Table 8.71);
 *  - `pending` — reliable signals awaiting a response, in delivery order (the sequence order of 9.6.5.1);
 *  - `responses` — received responses keyed by the `Header/@ID` they answer (`Header/@refID` of the response);
 *  - `delivered` — the per-channel delivery journal, pruned by `SignalResource` replacement windows;
 *  - `events` — the exchange trace recorded by the stateful interpreter (the traced interpreter writes the
 *    same list instead of storing it).
 */
final case class XjmfState(
    channels: Map[Nmtoken, ChannelState] = Map.empty,
    pending: Chain[PendingSignal] = Chain.empty,
    responses: Chain[(String, Response)] = Chain.empty,
    delivered: Chain[(Nmtoken, Signal)] = Chain.empty,
    events: Chain[TransportEvent] = Chain.empty,
)

object XjmfState:
  val empty: XjmfState = XjmfState()
