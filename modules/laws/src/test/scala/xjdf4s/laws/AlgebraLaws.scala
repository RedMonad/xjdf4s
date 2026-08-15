package xjdf4s.laws

import xjdf4s.laws.Arbitraries.given
import xjdf4s.model.*
import xjdf4s.prim.*
import cats.kernel.{Monoid, Semigroup, Semilattice}
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.*

/**
 * The algebraic structures of the model are *lawful*, not accidental:
 * associativity, identity, commutativity and idempotency are checked with
 * property-based tests (a discipline-style law suite on plain ScalaCheck).
 */
class AlgebraLaws extends ScalaCheckSuite:

  private def semigroupAssociativity[A](name: String)(using arb: Arbitrary[A])(using S: Semigroup[A]) =
    property(s"semigroup associativity: $name"):
      forAll(arb.arbitrary, arb.arbitrary, arb.arbitrary) { (a: A, b: A, c: A) =>
        S.combine(S.combine(a, b), c) == S.combine(a, S.combine(b, c))
      }

  private def monoidLaws[A](name: String)(using arb: Arbitrary[A])(using M: Monoid[A]) =
    property(s"monoid identity: $name"):
      forAll(arb.arbitrary) { (a: A) =>
        M.combine(a, M.empty) == a && M.combine(M.empty, a) == a
      }
    property(s"monoid associativity: $name"):
      forAll(arb.arbitrary, arb.arbitrary, arb.arbitrary) { (a: A, b: A, c: A) =>
        M.combine(M.combine(a, b), c) == M.combine(a, M.combine(b, c))
      }

  private def semilatticeLaws[A](name: String)(using arb: Arbitrary[A])(using S: Semilattice[A]) =
    property(s"semilattice commutativity: $name"):
      forAll(arb.arbitrary, arb.arbitrary) { (a: A, b: A) =>
        S.combine(a, b) == S.combine(b, a)
      }
    property(s"semilattice idempotency: $name"):
      forAll(arb.arbitrary) { (a: A) =>
        S.combine(a, a) == a
      }
    property(s"semilattice associativity: $name"):
      forAll(arb.arbitrary, arb.arbitrary, arb.arbitrary) { (a: A, b: A, c: A) =>
        S.combine(S.combine(a, b), c) == S.combine(a, S.combine(b, c))
      }

  // --- Part: overlay semigroup -----------------------------------------
  semigroupAssociativity[Part]("Part")

  // --- AmountPool: chronological concatenation -------------------------
  semigroupAssociativity[AmountPool]("AmountPool")

  // --- AuditPool: chronological concatenation --------------------------
  semigroupAssociativity[AuditPool]("AuditPool")

  // --- AmountRange: the meet semilattice (constraint intersection) -----
  semilatticeLaws[AmountRange]("AmountRange.meet")

  // --- TimeSpan: addition monoid ----------------------------------------
  monoidLaws[TimeSpan]("TimeSpan")

  // --- Patch: the endomorphism monoid of change orders ------------------
  // A Patch is a function, so its laws are checked *behaviorally*: through the
  // monoid action on tickets.
  property("patch monoid: identity acts trivially"):
    forAll { (t: XJDF) => Patch.identity.applyTo(t) == t }

  property("patch monoid: associativity of the action"):
    forAll { (t: XJDF, p: Patch, q: Patch) =>
      q.applyTo(p.applyTo(t)) == Monoid[Patch].combine(p, q).applyTo(t)
    }

  // --- Matrix: the affine transformation monoid (floating-point) -------
  private def approxEq(x: Double, y: Double): Boolean =
    x == y || math.abs(x - y) <= 1e-6 * math.max(1.0, math.max(math.abs(x), math.abs(y)))

  private def matrixEq(a: Matrix, b: Matrix): Boolean =
    approxEq(a.a, b.a) && approxEq(a.b, b.b) && approxEq(a.c, b.c) &&
      approxEq(a.d, b.d) && approxEq(a.tx, b.tx) && approxEq(a.ty, b.ty)

  property("matrix monoid: identity is 1 0 0 1 0 0"):
    forAll { (m: Matrix) =>
      matrixEq(m * Matrix.identity, m) && matrixEq(Matrix.identity * m, m)
    }

  property("matrix monoid: associativity of composition"):
    forAll { (a: Matrix, b: Matrix, c: Matrix) =>
      matrixEq((a * b) * c, a * (b * c))
    }

  property("matrix: inverse cancels composition when it exists"):
    forAll { (m: Matrix) =>
      m.inverse match
        case None      => math.abs(m.a * m.d - m.b * m.c) <= 1e-9
        case Some(inv) => matrixEq(m * inv, Matrix.identity) && matrixEq(inv * m, Matrix.identity)
    }

  property("matrix: composition matches point application"):
    forAll { (a: Matrix, b: Matrix, point: XYPairLike) =>
      val composed = (a * b).applyTo(point.p)
      val applied  = a.applyTo(b.applyTo(point.p))
      approxEq(composed.x, applied.x) && approxEq(composed.y, applied.y)
    }

  // --- IntegerRange: §1.10.2 counting semantics -------------------------
  property("IntegerRange.single(0) selects the first element"):
    IntegerRange.single(0).select(List("a", "b", "c")) == List("a")

  property("IntegerRange: negative indices count from the back"):
    IntegerRange.single(-1).select(List("a", "b", "c")) == List("c")

  property("IntegerRange 0 -1 selects everything"):
    IntegerRange(0, -1).select(List("a", "b", "c")) == List("a", "b", "c")

  property("IntegerRange -1 0 selects everything in reverse"):
    IntegerRange(-1, 0).select(List("a", "b", "c")) == List("c", "b", "a")

  property("IntegerRange 1 2 selects the middle"):
    IntegerRange(1, 2).select(List("a", "b", "c")) == List("b", "c")

/** A wrapper so the matrix-application property gets an arbitrary point. */
final case class XYPairLike(p: XYPair)

object XYPairLike:
  import org.scalacheck.{Arbitrary, Gen}
  given Arbitrary[XYPairLike] =
    Arbitrary:
      for
        x <- Gen.choose(-100.0, 100.0)
        y <- Gen.choose(-100.0, 100.0)
      yield XYPairLike(XYPair(x, y))
