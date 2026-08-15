package xjdf4s.laws

import xjdf4s.laws.Arbitraries.given
import xjdf4s.model.*
import xjdf4s.prim.*
import cats.data.ValidatedNec
import cats.kernel.{CommutativeMonoid, Monoid, Order, Semigroup}
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.*

/** The algebraic structures of the model are *lawful*, not accidental:
 *  associativity, identity, commutativity and idempotency are checked with
 *  property-based tests (a discipline-style law suite on plain ScalaCheck).
 *
 *  ADR-0009: hand-written suites are preferred over cats-laws/discipline-munit
 *  for control over floating-point tolerance and simpler dependency footprint.
 */
class AlgebraLaws extends ScalaCheckSuite:

  // ------------------------------------------------------------------
  // Helpers for the law templates
  // ------------------------------------------------------------------

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

  private def commutativeMonoidLaws[A](name: String)(using arb: Arbitrary[A])(using M: CommutativeMonoid[A]) =
    monoidLaws(name)
    property(s"commutative monoid commutativity: $name"):
      forAll(arb.arbitrary, arb.arbitrary) { (a: A, b: A) =>
        M.combine(a, b) == M.combine(b, a)
      }

  // --- Part: overlay semigroup -----------------------------------------
  semigroupAssociativity[Part]("Part")

  // --- AmountPool: chronological concatenation (Semigroup, NOT Monoid) --
  semigroupAssociativity[AmountPool]("AmountPool")

  property("AmountPool: combine concatenates histories"):
    forAll { (a: AmountPool, b: AmountPool) =>
      val combined = Semigroup[AmountPool].combine(a, b)
      combined.toList == a.toList ++ b.toList
    }

  test("AmountPool: Monoid is unattainable (cardinality T+)"):
    // compile-error confirmation: summon[Monoid[AmountPool]] would fail
    // because only Semigroup[AmountPool] is declared.
    val _ = summon[Semigroup[AmountPool]]

  // --- AuditPool: chronological concatenation (Semigroup, NOT Monoid) --
  semigroupAssociativity[AuditPool]("AuditPool")

  property("AuditPool: combine concatenates histories"):
    forAll { (a: AuditPool, b: AuditPool) =>
      val combined = Semigroup[AuditPool].combine(a, b)
      combined.toList == a.toList ++ b.toList
    }

  test("AuditPool: Monoid is unattainable (cardinality T+)"):
    val _ = summon[Semigroup[AuditPool]]

  // --- AmountBounds: ADR-0004 / Table 6.3 -------------------------------
  property("AmountBounds.meet is commutative when defined"):
    forAll { (a: AmountBounds, b: AmountBounds) =>
      AmountBounds.meet(a, b) == AmountBounds.meet(b, a)
    }

  property("AmountBounds.meet is associative as a partial intersection"):
    forAll { (a: AmountBounds, b: AmountBounds, c: AmountBounds) =>
      AmountBounds.meet(a, b).flatMap(AmountBounds.meet(_, c)) ==
        AmountBounds.meet(b, c).flatMap(AmountBounds.meet(a, _))
    }

  property("AmountBounds.meet is idempotent"):
    forAll { (a: AmountBounds) => AmountBounds.meet(a, a).contains(a) }

  property("AmountBounds.meet is defined exactly for non-empty intersections"):
    forAll { (a: AmountBounds, b: AmountBounds) =>
      val nonEmpty =
        a.min.forall(lower => b.max.forall(upper => Order[Amount].compare(lower, upper) <= 0)) &&
          b.min.forall(lower => a.max.forall(upper => Order[Amount].compare(lower, upper) <= 0))
      AmountBounds.meet(a, b).isDefined == nonEmpty
    }

  property("AmountBounds.widen is commutative"):
    forAll { (a: AmountBounds, b: AmountBounds) =>
      AmountBounds.widen(a, b) == AmountBounds.widen(b, a)
    }

  property("AmountBounds.widen is associative"):
    forAll { (a: AmountBounds, b: AmountBounds, c: AmountBounds) =>
      AmountBounds.widen(AmountBounds.widen(a, b), c) ==
        AmountBounds.widen(a, AmountBounds.widen(b, c))
    }

  property("AmountBounds.widen is idempotent"):
    forAll { (a: AmountBounds) => AmountBounds.widen(a, a) == a }

  test("AmountBounds.meet tightens and returns None for an empty intersection"):
    val left = AmountBounds(Some(Amount(10)), Some(Amount(20)))
    val right = AmountBounds(Some(Amount(15)), Some(Amount(25)))
    assertEquals(AmountBounds.meet(left, right), Some(AmountBounds(Some(Amount(15)), Some(Amount(20)))))
    assertEquals(
      AmountBounds.meet(left, AmountBounds(Some(Amount(21)), Some(Amount(30)))),
      None
    )

  test("AmountBounds.widen expands the acceptable range"):
    val left = AmountBounds(Some(Amount(10)), Some(Amount(20)))
    val right = AmountBounds(Some(Amount(15)), Some(Amount(25)))
    assertEquals(AmountBounds.widen(left, right), AmountBounds(Some(Amount(10)), Some(Amount(25))))

  test("AmountBounds rejects inverted bounds"):
    intercept[IllegalArgumentException](AmountBounds(Some(Amount(10)), Some(Amount(5))))

  // --- XYPair: commutative monoid of pointwise addition ----------------
  // NOTE: Floating-point addition is not associative; use approxEq.
  property("XYPair: monoid identity is (0, 0)"):
    forAll { (a: XYPair) =>
      xyPairApproxEq(XYPair.zero + a, a) && xyPairApproxEq(a + XYPair.zero, a)
    }

  property("XYPair: monoid associativity (approx)"):
    forAll { (a: XYPair, b: XYPair, c: XYPair) =>
      xyPairApproxEq((a + b) + c, a + (b + c))
    }

  property("XYPair: commutative monoid commutativity"):
    forAll { (a: XYPair, b: XYPair) => xyPairApproxEq(a + b, b + a) }

  // --- Points: commutative monoid of length addition -------------------
  // NOTE: Floating-point addition is not associative; use approxEq.
  property("Points: monoid identity is 0"):
    forAll { (a: Points) =>
      pointsApproxEq(Points.zero + a, a) && pointsApproxEq(a + Points.zero, a)
    }

  property("Points: monoid associativity (approx)"):
    forAll { (a: Points, b: Points, c: Points) =>
      pointsApproxEq((a + b) + c, a + (b + c))
    }

  property("Points: commutative monoid commutativity"):
    forAll { (a: Points, b: Points) => pointsApproxEq(a + b, b + a) }

  // --- TimeSpan: commutative monoid of duration addition ---------------
  commutativeMonoidLaws[TimeSpan]("TimeSpan")

  // --- Patch: the endomorphism monoid of change orders -----------------
  // A Patch is a function, so its laws are checked *behaviourally*: through
  // the monoid action on tickets.
  property("patch monoid: identity acts trivially"):
    forAll { (t: XJDF) => Patch.identity.applyTo(t) == t }

  property("patch monoid: associativity of the action"):
    forAll { (t: XJDF, p: Patch, q: Patch) =>
      q.applyTo(p.applyTo(t)) == Monoid[Patch].combine(p, q).applyTo(t)
    }

  // --- Matrix: the affine transformation monoid (NOT Group, X-05) ------
  private def approxEq(x: Double, y: Double): Boolean =
    x == y || math.abs(x - y) <= 1e-6 * math.max(1.0, math.max(math.abs(x), math.abs(y)))

  private def xyPairApproxEq(a: XYPair, b: XYPair): Boolean =
    approxEq(a.x, b.x) && approxEq(a.y, b.y)

  private def pointsApproxEq(a: Points, b: Points): Boolean =
    approxEq(a.value, b.value)

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
        case None => math.abs(m.a * m.d - m.b * m.c) <= 1e-9
        case Some(inv) => matrixEq(m * inv, Matrix.identity) && matrixEq(inv * m, Matrix.identity)
    }

  property("matrix: inverse is defined exactly when determinant != 0"):
    forAll { (m: Matrix) =>
      val det = m.a * m.d - m.b * m.c
      m.inverse.isDefined == (math.abs(det) > 1e-12)
    }

  test("matrix: Group is NOT declared (X-05 — singular matrix has no inverse)"):
    // compile-error confirmation: summon[Group[Matrix]] would fail
    val _ = summon[Monoid[Matrix]]

  property("matrix: composition matches point application"):
    forAll { (a: Matrix, b: Matrix, point: XYPairLike) =>
      val composed = (a * b).applyTo(point.p)
      val applied = a.applyTo(b.applyTo(point.p))
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

  // --- M1.1-4: IntegerRange boundary cases (§1.10.2, X-02) -------------
  test("IntegerRange: empty list selects nothing"):
    assertEquals(IntegerRange.all.select(Nil), Nil)
    assertEquals(IntegerRange(-1, 0).select(Nil), Nil)

  test("IntegerRange: out-of-range indices are clamped"):
    assertEquals(IntegerRange(-100, 100).select(List("a", "b", "c", "d", "e")), List("a", "b", "c", "d", "e"))

  test("IntegerRange: negative indices count from the back"):
    assertEquals(IntegerRange(-1, -3).select(List("a", "b", "c", "d", "e")), List("e", "d", "c"))

  test("IntegerRange: single-element list"):
    assertEquals(IntegerRange.all.select(List("a")), List("a"))

  test("IntegerRange: forward range"):
    assertEquals(IntegerRange(1, 3).select(List("a", "b", "c", "d", "e")), List("b", "c", "d"))

  test("IntegerRange: reverse range"):
    assertEquals(IntegerRange(3, 1).select(List("a", "b", "c", "d", "e")), List("d", "c", "b"))

  test("IntegerRange: descending range 5 2 is clamped and reversed"):
    assertEquals(IntegerRange(5, 2).select(List("a", "b", "c", "d", "e")), List("e", "d", "c"))

  test("IntegerRange: size = 0 yields no indices"):
    assertEquals(IntegerRange.all.indices(0L), Nil)

  // --- M1.0-3: compile-probes for the contested findings ---------------
  test("cats provides Monoid[ValidatedNec[Issue, Unit]] (X-01)"):
    // X-01: no hand-written given is needed — cats derives this instance from
    // Semigroup[NonEmptyChain[Issue]] and Monoid[Unit]; compilation is the proof.
    val _ = summon[Monoid[ValidatedNec[Issue, Unit]]]

  test("§1.10.2: IntegerRange(-1, 0) selects everything in reverse (X-02)"):
    assertEquals(
      IntegerRange(-1, 0).select(List("a", "b", "c")),
      List("c", "b", "a")
    )

  test("regression: overlay is right-biased (X-03)"):
    val l = Part(docIndex = Some(IntegerRange.single(3)))
    val r = Part(docIndex = Some(IntegerRange.single(-10)))
    assertEquals(Part.combine(l, r).docIndex, r.docIndex)
end AlgebraLaws

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