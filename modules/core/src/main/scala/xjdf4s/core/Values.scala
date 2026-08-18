package xjdf4s.core

import cats.{Eq, Hash, Show}

enum Version(val lexical: String) derives CanEqual:
  case V2_0 extends Version("2.0")
  case V2_1 extends Version("2.1")
  case V2_2 extends Version("2.2")

enum Orientation derives CanEqual:
  case Rotate0, Rotate90, Rotate180, Rotate270
  case Flip0, Flip90, Flip180, Flip270

enum ResourceUsage derives CanEqual:
  case Input, Output

enum ResourceAvailability derives CanEqual:
  case Available, Unavailable

enum Side derives CanEqual:
  case Front, Back

enum Severity derives CanEqual:
  case Event, Information, Warning, Error, Fatal

enum Scope derives CanEqual:
  case Allowed, Device, Estimate, Job, Present

enum NodeStatus derives CanEqual:
  case Aborted, Cleanup, Completed, InProgress, Setup, Stopped, Suspended, Waiting

enum DeviceStatus derives CanEqual:
  case Cleanup, Idle, NonProductive, Offline, Production, Setup, Stopped

enum SheetLay derives CanEqual:
  case Center, Left, Right

enum Face derives CanEqual:
  case Back, Bottom, Front, Left, Right, Top

/** The six values `a b c d e f` of an XJDF affine transformation matrix. */
final case class Matrix(a: Double, b: Double, c: Double, d: Double, e: Double, f: Double) derives CanEqual

object Matrix:
  val identity: Matrix = Matrix(1, 0, 0, 1, 0, 0)

  given Eq[Matrix] = Eq.fromUniversalEquals
  given Show[Matrix] = Show.show(matrix => s"${matrix.a} ${matrix.b} ${matrix.c} ${matrix.d} ${matrix.e} ${matrix.f}")
  given Hash[Matrix] = Hash.fromUniversalHashCode
end Matrix
