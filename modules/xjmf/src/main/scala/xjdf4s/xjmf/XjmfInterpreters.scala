package xjdf4s.xjmf

import cats.~>
import cats.data.{Chain, State, WriterT}

import xjdf4s.core.*
import xjdf4s.messaging.*

/**
 * The two executions of an [[Xjmf]] program — one program, several interpreters, as in stage 03:
 *
 *  1. `stateful` — the deterministic channel machine (`State[XjmfState, *]`): full state transitions, with the
 *     trace appended to `XjmfState.events`;
 *  2. `traced` — `WriterT` over the same state: runs the same transitions but *writes* the events instead of
 *     storing them (diagnostics, replay).
 *
 * The event trace depends on channel state (`Unrouted`/`ChannelNotOpen` branches), so a plain
 * `Writer[Chain, *]` cannot produce it. Both interpreters are therefore thin wrappers over the single
 * transition core `transition`, and the two traces agree by construction.
 *
 * The target effects are declared as type aliases (Scala 3 type lambdas are not expressible with the
 * kind-projector `*` placeholder unless `-Ykind-projector` is enabled).
 */
object XjmfInterpreters:

  type ChannelMachine[A] = State[XjmfState, A]
  type TracedMachine[A] = WriterT[ChannelMachine, Chain[TransportEvent], A]

  /**
   * The core transition: computes the next state (without the trace), the trace events and the result. It is
   * the single point where the protocol rules of 9.6 live — the interpreters only decide where the events go.
   * Public so that the stage 07 HTTP runtime can reuse the very same state machine as its in-memory core.
   */
  def transition[A](op: XjmfOp[A], state: XjmfState): (XjmfState, Chain[TransportEvent], A) =
    op match
      case XjmfOp.OpenChannel(subscription, channelId, messageType) =>
        // 9.6.3: a Subscription of the same type to the same URL SHALL replace the existing subscription;
        // reopening a Closed channel reactivates it.
        val next =
          state.copy(channels = state.channels.updated(channelId, ChannelState.Subscribed(messageType, subscription)))
        (next, Chain.one(TransportEvent.ChannelOpened(channelId, messageType)), ())

      case XjmfOp.CloseChannel(channelId) =>
        state.channels.get(channelId) match
          case Some(ChannelState.Subscribed(_, _)) =>
            val next = state.copy(channels = state.channels.updated(channelId, ChannelState.Closed))
            (next, Chain.one(TransportEvent.ChannelClosed(channelId)), ())
          case _ =>
            // closing an unknown or already closed channel is a no-op
            (state, Chain.empty, ())

      case XjmfOp.Deliver(signal) =>
        deliverSignal(signal, state)

      case XjmfOp.DeliverResponse(response) =>
        deliverResponse(response, state)

      case XjmfOp.AwaitResponse(answeredId) =>
        val found = state.responses.collectFirst { case (id, response) if id == answeredId.value => response }
        (state, Chain.empty, found)

      case XjmfOp.Channels =>
        val infos = state.channels.toVector.collect {
          case (channelId, ChannelState.Subscribed(messageType, subscription)) =>
            SubscriptionInfo(channelId, messageType, subscription)
        }
        (state, Chain.empty, infos.sortBy(_.channelId.value))
  end transition

  /**
   * Table 8.71: the signal is routed by `Header/@refID`, which SHALL match the `@ChannelID` (the initiating
   * query's `Header/@ID`) of its channel. A signal without `@refID` is unroutable.
   */
  private def deliverSignal(signal: Signal, state: XjmfState): (XjmfState, Chain[TransportEvent], Unit) =
    val signalId = signal.header.id.map(_.value).getOrElse("")
    signal.header.refId match
      case None =>
        (state, Chain.one(TransportEvent.Unrouted(signalId)), ())
      case Some(refId) =>
        state.channels.get(refId) match
          case Some(ChannelState.Closed) =>
            (state, Chain.one(TransportEvent.ChannelNotOpen(refId, signalId)), ())
          case Some(ChannelState.Subscribed(_, _)) =>
            val (journal, replaced) = replaceInWindow(signal, refId, state.delivered)
            val withJournal = state.copy(delivered = journal.append((refId, signal)))
            // 9.6.5: a Reliable signal needs an answer; a FireAndForget signal does not (9.6.4).
            val withPending = signal.channelMode match
              case Some(ChannelMode.Reliable) =>
                withJournal.copy(pending = withJournal.pending.append(PendingSignal(refId, signalId, signal)))
              case _ => withJournal
            val events =
              Chain
                .fromSeq(
                  Option.when(replaced.nonEmpty)(TransportEvent.SignalsReplaced(refId, signalId, replaced)).toVector,
                ) ++ Chain.one(TransportEvent.SignalDelivered(refId, signalId))
            (withPending, events, ())
          case None =>
            (state, Chain.one(TransportEvent.Unrouted(signalId)), ())

  /**
   * 9.6.1: `Header/@refID` of a response identifies the message it answers. The response is recorded for
   * `AwaitResponse`; if it answers a pending reliable signal, that signal stops being pending.
   */
  private def deliverResponse(response: Response, state: XjmfState): (XjmfState, Chain[TransportEvent], Unit) =
    val answeredId = response.header.refId.map(_.value).getOrElse("")
    val withResponses = state.copy(responses = state.responses.append((answeredId, response)))
    val next = withResponses.copy(pending = withResponses.pending.filterNot(_.signalId == answeredId))
    (next, Chain.one(TransportEvent.ResponseReceived(answeredId)), ())

  /**
   * Table 7.54: previous `SignalResource` messages of the same channel (the subscription scope) and the same
   * `Header/@DeviceID` whose `Header/@Time` lies strictly between `@ReplaceAfter` and `@ReplaceBefore` are
   * replaced by the incoming signal. If neither bound is present the signal is an original and replaces
   * nothing. `XsdDateTime` values are lexical ISO 8601 strings, so ordering compares correctly.
   */
  private def replaceInWindow(
      signal: Signal,
      channelId: Nmtoken,
      journal: Chain[(Nmtoken, Signal)],
  ): (Chain[(Nmtoken, Signal)], Vector[String]) =
    signal match
      case replacement: SignalResource =>
        val after = replacement.replaceAfter.map(_.value)
        val before = replacement.replaceBefore.map(_.value)
        if after.isEmpty && before.isEmpty then (journal, Vector.empty)
        else
          val replaced = Vector.newBuilder[String]
          val kept = journal.filter { case (entryChannel, entrySignal) =>
            entrySignal match
              case previous: SignalResource
                  if entryChannel == channelId &&
                    previous.header.deviceId == replacement.header.deviceId &&
                    after.forall(_ < previous.header.time.value) &&
                    before.forall(previous.header.time.value < _) =>
                replaced += previous.header.id.map(_.value).getOrElse("")
                false
              case _ => true
          }
          (kept, replaced.result())
      case _ => (journal, Vector.empty)
  end replaceInWindow

  /** The deterministic channel machine: full state, trace appended to `XjmfState.events`. */
  val stateful: XjmfOp ~> ChannelMachine = new (XjmfOp ~> ChannelMachine):
    def apply[A](op: XjmfOp[A]): ChannelMachine[A] =
      State[XjmfState, A] { state =>
        val (next, events, result) = transition(op, state)
        (next.copy(events = next.events ++ events), result)
      }

  /** The trace writer: the same transitions, events written instead of stored. */
  val traced: XjmfOp ~> TracedMachine = new (XjmfOp ~> TracedMachine):
    def apply[A](op: XjmfOp[A]): TracedMachine[A] =
      WriterT(
        State[XjmfState, (Chain[TransportEvent], A)] { state =>
          val (next, events, result) = transition(op, state)
          (next, (events, result))
        },
      )

  /** Runs a program on the channel machine and returns the final state (with the trace in `events`). */
  def run[A](program: Xjmf[A], initial: XjmfState = XjmfState.empty): XjmfState =
    program.foldMap(stateful).runS(initial).value

  /** Runs a program for its trace only; equal to `run(program, initial).events` by construction. */
  def trace[A](program: Xjmf[A], initial: XjmfState = XjmfState.empty): Chain[TransportEvent] =
    program.foldMap(traced).run.runA(initial).value._1
end XjmfInterpreters
