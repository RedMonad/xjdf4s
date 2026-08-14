package xjdf4s
package prim

import cats.Show
import cats.kernel.{Eq, Monoid, Order, Semigroup, Semilattice}

/** Canonical float rendering: drop the trailing `.0` (e.g. `80` instead of `80.0`). */
private[xjdf4s] def fmtDouble(d: Double): String =
  if d.isWhole && !d.isInfinite && !d.isNaN then d.toLong.toString
  else d.toString

/**
 * XJDF data type `XYPair` (Table A.1): the list SHALL contain two values
 * representing the sequence `x y`.
 *
 * Represented as a *named tuple* behind an opaque type: names survive at the
 * representation layer, while the public API is a nominal, extension-friendly
 * type. Pointwise addition is a lawful commutative monoid — the product of the
 * additive monoid on `Double` with itself.
 */
opaque type XYPair = (x: Double, y: Double)

object XYPair:

  def apply(x: Double, y: Double): XYPair = (x = x, y = y)

  val zero: XYPair = XYPair(0.0, 0.0)

  extension (p: XYPair)
    def x: Double = p.x
    def y: Double = p.y
    def +(o: XYPair): XYPair    = XYPair(p.x + o.x, p.y + o.y)
    def -(o: XYPair): XYPair    = XYPair(p.x - o.x, p.y - o.y)
    def scaled(k: Double): XYPair = XYPair(p.x * k, p.y * k)

  given Show[XYPair] = Show.show(p => s"${fmtDouble(p.x)} ${fmtDouble(p.y)}")

  given Eq[XYPair] = Eq.fromUniversalEquals

  given Order[XYPair] = Order.from((a, b) =>
    val byX = java.lang.Double.compare(a.x, b.x)
    if byX != 0 then byX else java.lang.Double.compare(a.y, b.y)
  )

  given Monoid[XYPair] with
    def empty: XYPair = zero
    def combine(a: XYPair, b: XYPair): XYPair = a + b

end XYPair

/**
 * XJDF data type `shape` (Table A.1): three values `width height depth`.
 */
opaque type Shape = (width: Double, height: Double, depth: Double)

object Shape:

  def apply(width: Double, height: Double, depth: Double): Shape =
    (width = width, height = height, depth = depth)

  def flat(width: Double, height: Double): Shape = Shape(width, height, 0.0)

  extension (s: Shape)
    def width: Double  = s.width
    def height: Double = s.height
    def depth: Double  = s.depth

  given Show[Shape] =
    Show.show(s => s"${fmtDouble(s.width)} ${fmtDouble(s.height)} ${fmtDouble(s.depth)}")

  given Eq[Shape] = Eq.fromUniversalEquals

end Shape

/**
 * XJDF data type `rectangle` (Table A.1): four values `llx lly urx ury`
 * (lower-left and upper-right corners).
 */
opaque type Rectangle = (llx: Double, lly: Double, urx: Double, ury: Double)

object Rectangle:

  /** Validated constructor: `llx <= urx` and `lly <= ury` SHALL hold. */
  def from(llx: Double, lly: Double, urx: Double, ury: Double): Option[Rectangle] =
    Option.when(llx <= urx && lly <= ury)((llx = llx, lly = lly, urx = urx, ury = ury))

  def unsafe(llx: Double, lly: Double, urx: Double, ury: Double): Rectangle =
    from(llx, lly, urx, ury)
      .getOrElse(throw new IllegalArgumentException(s"Invalid rectangle: $llx $lly $urx $ury"))

  extension (r: Rectangle)
    def llx: Double = r.llx
    def lly: Double = r.lly
    def urx: Double = r.urx
    def ury: Double = r.ury
    def width: Double  = r.urx - r.llx
    def height: Double = r.ury - r.lly

  given Show[Rectangle] =
    Show.show(r => s"${fmtDouble(r.llx)} ${fmtDouble(r.lly)} ${fmtDouble(r.urx)} ${fmtDouble(r.ury)}")

  given Eq[Rectangle] = Eq.fromUniversalEquals

end Rectangle

/**
 * XJDF data type `matrix` (Table A.1): six values `a b c d Tx Ty` of a 3×3
 * transformation matrix whose third column is fixed to `0 0 1` (§2.6.5).
 *
 * Matrices act on column vectors `[x y 1]ᵀ`; `m1 * m2` means “apply `m2`,
 * then `m1`”. Multiplication is associative with the identity `1 0 0 1 0 0` —
 * a lawful monoid, i.e. the group of affine plane transformations (§2.6).
 */
opaque type Matrix = (a: Double, b: Double, c: Double, d: Double, tx: Double, ty: Double)

object Matrix:

  def apply(a: Double, b: Double, c: Double, d: Double, tx: Double, ty: Double): Matrix =
    (a = a, b = b, c = c, d = d, tx = tx, ty = ty)

  val identity: Matrix = apply(1, 0, 0, 1, 0, 0)

  def translate(dx: Double, dy: Double): Matrix = apply(1, 0, 0, 1, dx, dy)

  def scale(sx: Double, sy: Double): Matrix = apply(sx, 0, 0, sy, 0, 0)

  def rotate(thetaRadians: Double): Matrix =
    val c = math.cos(thetaRadians)
    val s = math.sin(thetaRadians)
    apply(c, s, -s, c, 0, 0)

  extension (m: Matrix)
    def a: Double  = m.a
    def b: Double  = m.b
    def c: Double  = m.c
    def d: Double  = m.d
    def tx: Double = m.tx
    def ty: Double = m.ty

    /** Matrix composition: apply `o` first, then `m`. */
    def *(o: Matrix): Matrix =
      Matrix(
        a = m.a * o.a + m.c * o.b,
        b = m.b * o.a + m.d * o.b,
        c = m.a * o.c + m.c * o.d,
        d = m.b * o.c + m.d * o.d,
        tx = m.a * o.tx + m.c * o.ty + m.tx,
        ty = m.b * o.tx + m.d * o.ty + m.ty
      )

    /** Applies the transformation to a point (§2.6.5.1). */
    def applyTo(p: XYPair): XYPair =
      XYPair(m.a * p.x + m.c * p.y + m.tx, m.b * p.x + m.d * p.y + m.ty)

    /** The inverse matrix, when it exists (determinant ≠ 0). */
    def inverse: Option[Matrix] =
      val det = m.a * m.d - m.b * m.c
      if det == 0.0 then None
      else
        Some(
          Matrix(
            a = m.d / det,
            b = -m.b / det,
            c = -m.c / det,
            d = m.a / det,
            tx = (m.c * m.ty - m.d * m.tx) / det,
            ty = (m.b * m.tx - m.a * m.ty) / det
          )
        )

  given Show[Matrix] =
    Show.show(m => s"${fmtDouble(m.a)} ${fmtDouble(m.b)} ${fmtDouble(m.c)} ${fmtDouble(m.d)} ${fmtDouble(m.tx)} ${fmtDouble(m.ty)}")

  given Eq[Matrix] = Eq.fromUniversalEquals

  given Monoid[Matrix] with
    def empty: Matrix = identity
    def combine(a: Matrix, b: Matrix): Matrix = a * b

end Matrix

/**
 * A length in points — the default unit of XJDF geometry (Table A.3.23).
 * Distinct unit types make dimensional mistakes impossible: you cannot
 * accidentally add points to microns.
 */
opaque type Points = Double

object Points:

  def apply(d: Double): Points = d

  val zero: Points = 0.0

  extension (p: Points)
    def value: Double      = p
    def +(o: Points): Points = p + o
    def -(o: Points): Points = p - o
    def *(k: Double): Points = p * k
    def /(k: Double): Points = p / k
    def isNegative: Boolean = p < 0.0

  given Show[Points] = Show.show(p => fmtDouble(p.value))

  given Eq[Points] = Eq.fromUniversalEquals

  given Order[Points] = Order.from((a, b) => java.lang.Double.compare(a, b))

  given Monoid[Points] with
    def empty: Points = zero
    def combine(a: Points, b: Points): Points = a + b

end Points

/** A length in microns [µm] (e.g. media thickness). */
opaque type Microns = Double

object Microns:

  def apply(d: Double): Microns = d

  val zero: Microns = 0.0

  /** 1 point = 25.4 / 72 mm = 352.7(7) µm. */
  private val MicronsPerPoint = 25.4 / 72.0 * 1000.0

  def ofPoints(p: Points): Microns = Microns(p.value * MicronsPerPoint)

  extension (m: Microns)
    def value: Double        = m
    def +(o: Microns): Microns = m + o
    def *(k: Double): Microns = m * k

  given Show[Microns] = Show.show(m => fmtDouble(m.value))

  given Eq[Microns] = Eq.fromUniversalEquals

  given Order[Microns] = Order.from((a, b) => java.lang.Double.compare(a, b))

end Microns

/** Media weight in grammage, g/m² (Appendix B). */
opaque type Grammage = Double

object Grammage:

  def apply(d: Double): Grammage = d

  extension (g: Grammage)
    def value: Double = g
    def +(o: Grammage): Grammage = g + o

  given Show[Grammage] = Show.show(g => fmtDouble(g.value))

  given Eq[Grammage] = Eq.fromUniversalEquals

  given Order[Grammage] = Order.from((a, b) => java.lang.Double.compare(a, b))

end Grammage

/**
 * An amount of a resource or product, e.g. `PartAmount/@Amount` — in the units
 * of `ResourceSet/@Unit` or the default units of Table A.3.23.
 */
opaque type Amount = Double

object Amount:

  def apply(d: Double): Amount = d

  val zero: Amount = 0.0

  /** Planned/actual amounts SHALL not be negative. */
  def nonNegative(d: Double): Option[Amount] = Option.when(d >= 0.0)(d)

  extension (a: Amount)
    def value: Double        = a
    def +(o: Amount): Amount = a + o
    def -(o: Amount): Amount = a - o
    def *(k: Double): Amount = a * k
    def isNegative: Boolean  = a < 0.0

  given Show[Amount] = Show.show(a => fmtDouble(a.value))

  given Eq[Amount] = Eq.fromUniversalEquals

  given Order[Amount] = Order.from((a, b) => java.lang.Double.compare(a, b))

  given Semigroup[Amount] with
    def combine(a: Amount, b: Amount): Amount = a + b

end Amount

/** A percentage in [0, 100] (e.g. ink coverage). */
opaque type Coverage = Double

object Coverage:

  def from(d: Double): Option[Coverage] = Option.when(d >= 0.0 && d <= 100.0)(d)

  def unsafe(d: Double): Coverage =
    from(d).getOrElse(throw new IllegalArgumentException(s"Coverage out of range [0,100]: $d"))

  extension (c: Coverage) def value: Double = c

  given Show[Coverage] = Show.show(c => fmtDouble(c.value))

  given Eq[Coverage] = Eq.fromUniversalEquals

end Coverage

/** A normalized value in [0, 1] (e.g. CMYK/RGB color components, `@Area`). */
opaque type UnitInterval = Double

object UnitInterval:

  def from(d: Double): Option[UnitInterval] = Option.when(d >= 0.0 && d <= 1.0)(d)

  def unsafe(d: Double): UnitInterval =
    from(d).getOrElse(throw new IllegalArgumentException(s"Value out of range [0,1]: $d"))

  extension (u: UnitInterval) def value: Double = u

  given Show[UnitInterval] = Show.show(u => fmtDouble(u.value))

  given Eq[UnitInterval] = Eq.fromUniversalEquals

end UnitInterval

/**
 * XJDF quality scoring `@Severity`: an integer in [0, 100], where 0 is the
 * highest and 100 the lowest quality (§5.3.4.1). The spec defines the exact
 * mapping between severity values and quality scores, implemented here.
 */
opaque type Severity = Int

object Severity:

  /** Maps a quality score to a severity: `Severity = 100 × P / (N − 1)`. */
  def fromScore(position: Int, numberOfScores: Int): Severity =
    require(numberOfScores > 1, "numberOfScores must be greater than 1")
    (100.0 * position / (numberOfScores - 1)).round.toInt

  /** Maps a severity back to a quality score: `P = S × (N − 1) / 100`. */
  def toScore(s: Severity, numberOfScores: Int): Int =
    (s.toDouble * (numberOfScores - 1) / 100.0).round.toInt

  def fromInt(i: Int): Option[Severity] = Option.when(i >= 0 && i <= 100)(i)

  def unsafe(i: Int): Severity =
    fromInt(i).getOrElse(throw new IllegalArgumentException(s"Severity out of range [0,100]: $i"))

  extension (s: Severity)
    def value: Int = s
    def isPerfect: Boolean = s == 0

  given Show[Severity] = Show.show(_.value.toString)

  given Eq[Severity] = Eq.fromUniversalEquals

  given Order[Severity] = Order.from((a, b) => Integer.compare(a, b))

end Severity

/**
 * XJDF data type `IntegerRange` (Table A.1): two integer values representing an
 * inclusive range. Zero-based with negative values counting from the back of
 * the list (§1.10.2): `0-1` selects all entries, `-1 0` the same in reverse.
 */
opaque type IntegerRange = (from: Long, to: Long)

object IntegerRange:

  def apply(from: Long, to: Long): IntegerRange = (from = from, to = to)

  def single(i: Long): IntegerRange = apply(i, i)

  def all: IntegerRange = apply(0, -1)

  extension (r: IntegerRange)
    def from: Long = r.from
    def to: Long   = r.to

    /** Normalizes a single index: negative values count from the back. */
    def normalizeIndex(i: Long, size: Long): Long =
      if i < 0 then size + i else i

    /** The inclusive list of normalized indices selected by this range. */
    def indices(size: Long): List[Long] =
      if size <= 0 then Nil
      else
        val f = normalizeIndex(r.from, size)
        val t = normalizeIndex(r.to, size)
        val lo = math.max(0L, math.min(f, size - 1))
        val hi = math.max(0L, math.min(t, size - 1))
        if lo <= hi then (lo to hi).toList else (lo to hi by -1).toList

    /** Selects items of a list, applying the counting rules of §1.10.2. */
    def select[A](items: List[A]): List[A] =
      indices(items.size.toLong).map(i => items(i.toInt))

  given Show[IntegerRange] = Show.show(r => s"${r.from} ${r.to}")

  given Eq[IntegerRange] = Eq.fromUniversalEquals

end IntegerRange

/** XJDF data type `LabColor` (Table A.1): three values `L a b`, L ∈ [0, 100]. */
opaque type LabColor = (l: Double, a: Double, b: Double)

object LabColor:

  def from(l: Double, a: Double, b: Double): Option[LabColor] =
    Option.when(l >= 0.0 && l <= 100.0)((l = l, a = a, b = b))

  def unsafe(l: Double, a: Double, b: Double): LabColor =
    from(l, a, b).getOrElse(throw new IllegalArgumentException(s"Invalid LabColor: $l $a $b"))

  extension (c: LabColor)
    def l: Double = c.l
    def a: Double = c.a
    def b: Double = c.b

  given Show[LabColor] =
    Show.show(c => s"${fmtDouble(c.l)} ${fmtDouble(c.a)} ${fmtDouble(c.b)}")

  given Eq[LabColor] = Eq.fromUniversalEquals

end LabColor

/** XJDF data type `CMYKColor` (Table A.1): four values `C M Y K`, each in [0, 1]. */
opaque type CMYKColor = (c: UnitInterval, m: UnitInterval, y: UnitInterval, k: UnitInterval)

object CMYKColor:

  def from(c: Double, m: Double, y: Double, k: Double): Option[CMYKColor] =
    for
      ci <- UnitInterval.from(c)
      mi <- UnitInterval.from(m)
      yi <- UnitInterval.from(y)
      ki <- UnitInterval.from(k)
    yield (c = ci, m = mi, y = yi, k = ki)

  def unsafe(c: Double, m: Double, y: Double, k: Double): CMYKColor =
    from(c, m, y, k)
      .getOrElse(throw new IllegalArgumentException(s"Invalid CMYKColor: $c $m $y $k"))

  extension (color: CMYKColor)
    def c: UnitInterval = color.c
    def m: UnitInterval = color.m
    def y: UnitInterval = color.y
    def k: UnitInterval = color.k

  given Show[CMYKColor] =
    Show.show(color => s"${fmtDouble(color.c.value)} ${fmtDouble(color.m.value)} ${fmtDouble(color.y.value)} ${fmtDouble(color.k.value)}")

  given Eq[CMYKColor] = Eq.fromUniversalEquals

end CMYKColor

/** XJDF data type `RGBColor` (Table A.1): three values `r g b`, each in [0, 1]. */
opaque type RGBColor = (r: UnitInterval, g: UnitInterval, b: UnitInterval)

object RGBColor:

  def from(r: Double, g: Double, b: Double): Option[RGBColor] =
    for
      ri <- UnitInterval.from(r)
      gi <- UnitInterval.from(g)
      bi <- UnitInterval.from(b)
    yield (r = ri, g = gi, b = bi)

  def unsafe(r: Double, g: Double, b: Double): RGBColor =
    from(r, g, b).getOrElse(throw new IllegalArgumentException(s"Invalid RGBColor: $r $g $b"))

  extension (color: RGBColor)
    def r: UnitInterval = color.r
    def g: UnitInterval = color.g
    def b: UnitInterval = color.b

  given Show[RGBColor] =
    Show.show(color => s"${fmtDouble(color.r.value)} ${fmtDouble(color.g.value)} ${fmtDouble(color.b.value)}")

  given Eq[RGBColor] = Eq.fromUniversalEquals

end RGBColor

/**
 * XJDF data type `FloatList` (Table A.1): a non-empty list of float values.
 */
opaque type FloatList = List[Double]

object FloatList:

  def of(head: Double, tail: Double*): FloatList = head :: tail.toList

  extension (values: FloatList)
    def toList: List[Double] = values
    def size: Int            = values.size

  given Show[FloatList] = Show.show(_.map(fmtDouble).mkString(" "))

  given Eq[FloatList] = Eq.fromUniversalEquals

end FloatList

/**
 * XJDF data type `IntegerList` (Table A.1): a non-empty list of integer values.
 */
opaque type IntegerList = List[Long]

object IntegerList:

  def of(head: Long, tail: Long*): IntegerList = head :: tail.toList

  extension (values: IntegerList)
    def toList: List[Long] = values
    def size: Int          = values.size

  given Show[IntegerList] = Show.show(_.mkString(" "))

  given Eq[IntegerList] = Eq.fromUniversalEquals

end IntegerList

/**
 * A planned or recorded amount range: `@Amount` together with the tolerances
 * `@MinAmount`/`@MaxAmount` (PartAmount, Table 6.3).
 *
 * The *meet* of two ranges — taking the stricter constraint on every axis — is
 * commutative, associative and idempotent: a lawful `Semilattice`, the
 * “constraint intersection”. The dual *join* (optimistic widening) is a
 * semilattice as well; both are modelled here.
 */
opaque type AmountRange = (amount: Option[Amount], max: Option[Amount], min: Option[Amount])

object AmountRange:

  val unbounded: AmountRange = (amount = None, max = None, min = None)

  def apply(amount: Option[Amount], max: Option[Amount], min: Option[Amount]): AmountRange =
    (amount = amount, max = max, min = min)

  def exact(a: Amount): AmountRange = apply(Some(a), None, None)

  def between(min: Amount, max: Amount): AmountRange = apply(None, Some(max), Some(min))

  private def stricterMin(a: Option[Amount], b: Option[Amount]): Option[Amount] =
    (a, b) match
      case (None, other)      => other
      case (other, None)      => other
      case (Some(x), Some(y)) => Some(if Order[Amount].compare(x, y) >= 0 then x else y)

  private def stricterMax(a: Option[Amount], b: Option[Amount]): Option[Amount] =
    (a, b) match
      case (None, other)      => other
      case (other, None)      => other
      case (Some(x), Some(y)) => Some(if Order[Amount].compare(x, y) <= 0 then x else y)

  extension (r: AmountRange)
    def amount: Option[Amount] = r.amount
    def max: Option[Amount]    = r.max
    def min: Option[Amount]    = r.min

    /** Constraint tightening: the greatest lower bound of two ranges. */
    def meet(o: AmountRange): AmountRange =
      AmountRange(
        amount = AmountRange.stricterMin(r.amount, o.amount),
        max = AmountRange.stricterMax(r.max, o.max),
        min = AmountRange.stricterMin(r.min, o.min)
      )

    /** Optimistic widening: the least upper bound of two ranges. */
    def join(o: AmountRange): AmountRange =
      AmountRange(
        amount = AmountRange.stricterMax(r.amount, o.amount),
        max = AmountRange.stricterMax(r.max, o.max),
        min = AmountRange.stricterMin(r.min, o.min)
      )

    /** True when `a` satisfies this range. */
    def includes(a: Amount): Boolean =
      r.min.forall(m => Order[Amount].compare(a, m) >= 0) &&
        r.max.forall(m => Order[Amount].compare(a, m) <= 0)

  given Semilattice[AmountRange] with
    def combine(a: AmountRange, b: AmountRange): AmountRange = a.meet(b)

  given Show[AmountRange] =
    Show.show { r =>
      val parts = List(
        r.min.map(m => s"min ${fmtDouble(m.value)}"),
        r.amount.map(a => s"amount ${fmtDouble(a.value)}"),
        r.max.map(m => s"max ${fmtDouble(m.value)}")
      ).flatten
      if parts.isEmpty then "unbounded" else parts.mkString(", ")
    }

  given Eq[AmountRange] = Eq.fromUniversalEquals

end AmountRange
