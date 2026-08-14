package xjdf4s.laws

import xjdf4s.model.*
import xjdf4s.prim.*
import cats.Functor
import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll

/**
 * Table 3.2 “Alignment of Audits and Messages” and the categorical backbone
 * around it: `Pulse` (momentary observation), `NonEmptyChain` (accumulated
 * history, the free monoid) and the natural transformation `snapshot` between
 * them.
 */
class AlignmentLaws extends ScalaCheckSuite:

  property("naturality: snapshot ∘ map(f) == map(f) ∘ snapshot"):
    forAll { (i: Int, f: Int => String) =>
      val left  = Alignment.snapshot(Functor[Pulse].map(Pulse.beat(i))(f))
      val right = Alignment.snapshot(Pulse.beat(i)).map(f)
      left.toList == right.toList
    }

  property("alignment of Table 3.2: SignalNotification → AuditNotification"):
    val header = Header(NmToken.unsafe("Dev1"), Timestamp.ofEpochSecond(0))
    val notification = Notification(SeverityClass.Warning)
    val signal = Signal(header, SignalPayload.Notified(notification))
    Alignment.signalToAudit(signal) match
      case Audit.Notified(h, n) => h == header && n == notification
      case _                    => false

  property("alignment of Table 3.2: SignalStatus → AuditStatus"):
    val header = Header(NmToken.unsafe("Dev1"), Timestamp.ofEpochSecond(0))
    val deviceInfo = DeviceInfo(DeviceStatus.Production)
    val signal = Signal(header, SignalPayload.Status(deviceInfo))
    Alignment.signalToAudit(signal) match
      case Audit.Status(h, d) => h == header && d == deviceInfo
      case _                  => false

  property("alignment of Table 3.2: SignalResource → AuditResource"):
    val header = Header(NmToken.unsafe("Dev1"), Timestamp.ofEpochSecond(0))
    val info = ResourceInfo(ResourceSet(ResourceSetName.unsafe("Component")))
    val signal = Signal(header, SignalPayload.Resource(info))
    Alignment.signalToAudit(signal) match
      case Audit.Resource(h, r) => h == header && r == info
      case _                    => false

  property("folding a stream of signal pulses yields a chronological audit pool"):
    val t0 = Timestamp.ofEpochSecond(0)
    val t1 = Timestamp.ofEpochSecond(10)
    val header = Header(NmToken.unsafe("Dev1"), t0)
    val pulses = cats.data.NonEmptyChain(
      Pulse.beat(Signal(header, SignalPayload.Status(DeviceInfo(DeviceStatus.Setup)))),
      Pulse.beat(Signal(header.copy(time = t1), SignalPayload.Status(DeviceInfo(DeviceStatus.Production))))
    )
    val pool = Alignment.foldSignals(pulses)
    pool.toList.size == 2 && pool.isChronological

  property("an audit pool is the free monoid over audits"):
    val a = Audit.Created(Header(NmToken.unsafe("D"), Timestamp.ofEpochSecond(0)))
    val b = Audit.Status(Header(NmToken.unsafe("D"), Timestamp.ofEpochSecond(1)), DeviceInfo(DeviceStatus.Idle))
    val p1 = AuditPool.of(a, b)
    val p2 = AuditPool.of(b)
    val combined = cats.kernel.Semigroup[AuditPool].combine(p1, p2)
    combined.toList == List(a, b, b) && combined.isChronological
