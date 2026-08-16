package xjdf4s.model

import xjdf4s.core.*

enum PreflightLevel derives CanEqual:
  case Basic, Extended, Premium
end PreflightLevel

final case class PreflightItem(
    level: Option[PreflightLevel] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum ProofColorType derives CanEqual:
  case Monochrome, BasicColor, MatchedColor
end ProofColorType

final case class IntegerRange(first: Int, last: Int) derives CanEqual

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

enum CutDepth derives CanEqual:
  case Full, Partial
end CutDepth

enum ShapeCutType derives CanEqual:
  case Cut, Perforate
end ShapeCutType

enum ProductShapeType derives CanEqual:
  case Line, Path, Rectangular, Round, RoundedRectangle
end ProductShapeType

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
    shapeTypeDetails: Option[String] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ShapeCuttingIntent(
    shapeCuts: NonEmptyVector[ShapeCut],
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("ShapeCuttingIntent")
