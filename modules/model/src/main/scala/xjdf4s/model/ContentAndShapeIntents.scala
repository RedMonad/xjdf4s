package xjdf4s.model

import cats.{Eq, Hash, Show}
import xjdf4s.core.*

enum PreflightLevel derives CanEqual:
  case Basic, Extended, Premium

final case class PreflightItem(
    level: Option[PreflightLevel] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum ProofColorType derives CanEqual:
  case Monochrome, BasicColor, MatchedColor

/** An inclusive two-integer range (Appendix A.1). `IntegerRange` MAY select a contiguous set of items from a list;
 *  the two values represent an inclusive index range.
 */
final case class IntegerRange(first: Int, last: Int) derives CanEqual

object IntegerRange:
  def from(first: Int, last: Int): Either[ValidationError, IntegerRange] =
    Either.cond(
      first <= last,
      IntegerRange(first, last),
      ValidationError.InvalidValue(
        "IntegerRange",
        s"$first $last",
        "a range whose first value does not exceed its last"
      ),
    )

  given Eq[IntegerRange] = Eq.fromUniversalEquals
  given Show[IntegerRange] = Show.show(range => s"${range.first} ${range.last}")
  given Hash[IntegerRange] = Hash.fromUniversalHashCode
end IntegerRange

final case class ProofItem(
    amount: Option[Int] = None,
    colorType: Option[ProofColorType] = None,
    contract: Option[Boolean] = None,
    halfTone: Option[Boolean] = None,
    id: Option[XsdId] = None,
    pageIndex: Option[IntegerRange] = None,
    proofTarget: Option[UriRef] = None,
    fileSpec: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ContentCheckIntent(
    preflightItems: Vector[PreflightItem] = Vector.empty,
    proofItems: Vector[ProofItem] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("ContentCheckIntent")

final case class Rectangle(lowerLeft: XYPair, upperRight: XYPair) derives CanEqual

object Rectangle:
  given Eq[Rectangle] = Eq.fromUniversalEquals
  given Show[Rectangle] = Show.show(rectangle =>
    s"${rectangle.lowerLeft.x} ${rectangle.lowerLeft.y} ${rectangle.upperRight.x} ${rectangle.upperRight.y}"
  )
  given Hash[Rectangle] = Hash.fromUniversalHashCode

enum CutDepth derives CanEqual:
  case Full, Partial

enum ShapeCutType derives CanEqual:
  case Cut, Perforate

enum ProductShapeType derives CanEqual:
  case Line, Path, Rectangular, Round, RoundedRectangle

opaque type PdfPath = String
object PdfPath:
  def from(value: String): Either[ValidationError, PdfPath] =
    Either.cond(
      value.nonEmpty,
      value,
      ValidationError.EmptyValue("PDFPath"),
    )

  extension (value: PdfPath) def value: String = value
end PdfPath

final case class ShapeCut(
    shapeType: ProductShapeType,
    cutBox: Option[Rectangle] = None,
    cutDepth: Option[CutDepth] = None,
    cutOut: Option[Boolean] = None,
    cutPath: Option[PdfPath] = None,
    cutType: Option[ShapeCutType] = None,
    shapeTypeDetails: Option[XjdfString] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ShapeCuttingIntent(
    shapeCuts: NonEmptyVector[ShapeCut],
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("ShapeCuttingIntent")
