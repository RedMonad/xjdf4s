package xjdf4s
package model

import xjdf4s.prim.*
import cats.{Functor, Show}
import cats.arrow.FunctionK
import cats.data.{Chain, NonEmptyChain}
import cats.kernel.{Eq, Semigroup}

/**
 * `AuditPool` entries (§3.2). Audits are conceptually very similar to
 * job-specific signals: signals record the momentary state of a process or
 * Device, audits summarize that state over a phase. Each audit carries the
 * Header of its originator, exactly as in Table 3.3.
 */
enum Audit:
  /** Logs creation of an XJDF — SHOULD be the first audit (§3.2.1). */
  case Created(header: Header)
  /** Logs individual events that occurred during processing (§3.2.2). */
  case Notified(header: Header, event: Notification)
  /** Summarizes one complete execution run of an XJDF (§3.2.3). */
  case Run(header: Header, run: ProcessRun)
  /** Describes the usage of resources during execution (§3.2.4). */
  case Resource(header: Header, resourceInfo: ResourceInfo)
  /** Logs start and end times of process phases (§3.2.5). */
  case Status(header: Header, deviceInfo: DeviceInfo)

  /** The local element name, per Table 3.3 (`AuditPool/@Name`). */
  def elementName: NmToken =
    this match
      case Created(_)    => NmToken.unsafe("AuditCreated")
      case Notified(_, _) => NmToken.unsafe("AuditNotification")
      case Run(_, _)     => NmToken.unsafe("AuditProcessRun")
      case Resource(_, _) => NmToken.unsafe("AuditResource")
      case Status(_, _)  => NmToken.unsafe("AuditStatus")

  def header: Header =
    this match
      case Created(h)    => h
      case Notified(h, _) => h
      case Run(h, _)     => h
      case Resource(h, _) => h
      case Status(h, _)  => h

  def time: Timestamp = header.time

object Audit:

  given Show[Audit] =
    Show.show:
      case Created(h)    => s"AuditCreated(${Show[Header].show(h)})"
      case Notified(h, n) => s"AuditNotification(${Show[Header].show(h)}, ${Show[Notification].show(n)})"
      case Run(h, r)     => s"AuditProcessRun(${Show[Header].show(h)}, ${Show[ProcessRun].show(r)})"
      case Resource(h, r) => s"AuditResource(${Show[Header].show(h)}, ${Show[ResourceInfo].show(r)})"
      case Status(h, d)  => s"AuditStatus(${Show[Header].show(h)}, ${Show[DeviceInfo].show(d)})"

  given Eq[Audit] = Eq.fromUniversalEquals

end Audit

/**
 * `AuditPool` (§3.2, Table 3.3): the recorded results of a process, ordered
 * chronologically — the last entry represents the newest state.
 *
 * Categorically an AuditPool is an element of the *free monoid* over `Audit`:
 * the semigroup operation is chronological concatenation. `isChronological`
 * checks that the sequence is lawful as a time-ordered history.
 */
opaque type AuditPool = NonEmptyChain[Audit]

object AuditPool:

  def of(head: Audit, tail: Audit*): AuditPool =
    NonEmptyChain(head, tail*)

  def from(chain: NonEmptyChain[Audit]): AuditPool = chain

  extension (pool: AuditPool)
    def toChain: NonEmptyChain[Audit] = pool
    def toList: List[Audit]           = pool.toList
    def latest: Audit                 = pool.last
    def oldest: Audit                 = pool.head

    /** True when the audits are ordered chronologically from oldest to newest. */
    def isChronological: Boolean =
      pool.toList.sliding(2).forall {
        case a :: b :: Nil => !b.time.isBefore(a.time)
        case _             => true
      }

    /** The process runs recorded in this pool, in order. */
    def processRuns: Chain[ProcessRun] =
      Chain.fromSeq(pool.toList.collect { case Audit.Run(_, run) => run })

    /** The `AuditProcessRun` that finalizes the newest workstep. */
    def latestProcessRun: Option[ProcessRun] = processRuns.lastOption

  given Semigroup[AuditPool] with
    def combine(a: AuditPool, b: AuditPool): AuditPool =
      NonEmptyChain.fromChainUnsafe(a.toChain ++ b.toChain)

  given Show[AuditPool] =
    Show.show(pool => pool.toList.map(Show[Audit].show).mkString("[", "\n, ", "]"))

  given Eq[AuditPool] = Eq.fromUniversalEquals

end AuditPool

/**
 * XJMF signals aligned with audits (Table 3.2): the momentary observations of
 * a process or Device. The payloads are syntactically identical to the
 * corresponding audits — this is what makes the alignment a *natural*
 * transformation.
 */
enum SignalPayload:
  case Notified(event: Notification)
  case Resource(resourceInfo: ResourceInfo)
  case Status(deviceInfo: DeviceInfo)

object SignalPayload:

  given Show[SignalPayload] = Show.fromToString

  given Eq[SignalPayload] = Eq.fromUniversalEquals

end SignalPayload

/** A signal: a momentary state observation with the Header of its sender. */
final case class Signal(header: Header, payload: SignalPayload)

object Signal:

  given Show[Signal] =
    Show.show(s => s"Signal(${Show[Header].show(s.header)}, ${Show[SignalPayload].show(s.payload)})")

  given Eq[Signal] = Eq.fromUniversalEquals

end Signal

/**
 * `Pulse[+A]`: the *functor of momentary observations* — a one-shot container
 * for a value `A`.
 */
enum Pulse[+A]:
  case Beat(value: A)

object Pulse:

  def beat[A](a: A): Pulse[A] = Beat(a)

  given Functor[Pulse] with
    def map[A, B](fa: Pulse[A])(f: A => B): Pulse[B] =
      fa match
        case Beat(a) => Beat(f(a))

end Pulse

/**
 * The categorical backbone of the audit/signal story:
 *
 *  - `Pulse` is the functor of momentary observations;
 *  - `NonEmptyChain` is the *free monoid* functor of accumulated history;
 *  - `snapshot` is a natural transformation `Pulse ~> NonEmptyChain` that
 *    remembers each pulse as a one-element history (the unit of the free
 *    monoid); `map`-naturality is a law, tested in the laws module;
 *  - Table 3.2 “Alignment of Audits and Messages” is the translation of signal
 *    payloads into audit payloads (`signalToAudit`); composing `Pulse.map` with
 *    `signalToAudit` and then `snapshot` turns a pulse of signals into the
 *    chronological log — an `AuditPool`.
 */
object Alignment:

  /** Natural transformation: remember every pulse as a one-element history. */
  val snapshot: FunctionK[Pulse, NonEmptyChain] =
    new FunctionK[Pulse, NonEmptyChain]:
      def apply[A](fa: Pulse[A]): NonEmptyChain[A] =
        fa match
          case Pulse.Beat(a) => NonEmptyChain.one(a)

  /** Table 3.2: the alignment of signals with their syntactically equal audits. */
  val signalToAudit: Signal => Audit =
    (s: Signal) =>
      s.payload match
        case SignalPayload.Notified(event) => Audit.Notified(s.header, event)
        case SignalPayload.Resource(info)  => Audit.Resource(s.header, info)
        case SignalPayload.Status(info)    => Audit.Status(s.header, info)

  /** A pulse of signals becomes a one-element log of audits. */
  def toAuditLog(pulse: Pulse[Signal]): AuditPool =
    AuditPool.from(snapshot(Functor[Pulse].map(pulse)(signalToAudit)))

  /** A stream of signal pulses becomes the chronological audit pool. */
  def foldSignals(pulses: NonEmptyChain[Pulse[Signal]]): AuditPool =
    val logs = pulses.map(toAuditLog)
    NonEmptyChain.fromChainUnsafe(logs.toChain.flatMap(_.toChain))

end Alignment
