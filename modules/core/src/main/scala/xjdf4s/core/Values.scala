package xjdf4s.core

enum Version(val lexical: String) derives CanEqual:
  case V2_0 extends Version("2.0")
  case V2_1 extends Version("2.1")
  case V2_2 extends Version("2.2")
end Version

enum Orientation derives CanEqual:
  case Rotate0, Rotate90, Rotate180, Rotate270
  case Flip0, Flip90, Flip180, Flip270
end Orientation

enum ResourceUsage derives CanEqual:
  case Input, Output
end ResourceUsage

enum ResourceAvailability derives CanEqual:
  case Available, Unavailable
end ResourceAvailability

enum Side derives CanEqual:
  case Front, Back
end Side

/** The six values `a b c d e f` of an XJDF affine transformation matrix. */
final case class Matrix(a: Double, b: Double, c: Double, d: Double, e: Double, f: Double) derives CanEqual

object Matrix:
  val identity: Matrix = Matrix(1, 0, 0, 1, 0, 0)
end Matrix
