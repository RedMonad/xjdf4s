package xjdf4s.model

import cats.{Eq, Hash, Show}
import xjdf4s.core.*

final case class Certification(
    claim: Option[XjdfString] = None,
    identifier: Option[XjdfString] = None,
    organization: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class SurfaceColor(
    coatings: Vector[Nmtoken] = Vector.empty,
    colorsUsed: Vector[Nmtoken] = Vector.empty,
    coverage: Option[Float] = None,
    printStandard: Option[Nmtoken] = None,
    certifications: Vector[Certification] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

/** Isomorphic representation of zero, one, or two SurfaceColor elements with unique Side attributes. */
enum ColorSurfaces:
  case Unprinted
  case Front(value: SurfaceColor)
  case Back(value: SurfaceColor)
  case Both(front: SurfaceColor, back: SurfaceColor)

final case class ColorIntent(
    surfaces: ColorSurfaces = ColorSurfaces.Unprinted,
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("ColorIntent")

enum LaminatedSurfaces derives CanEqual:
  case Front, Back, Both

enum LaminationTemperature derives CanEqual:
  case Hot, Cold

final case class LaminatingIntent(
    surfaces: LaminatedSurfaces,
    temperature: Option[LaminationTemperature] = None,
    texture: Option[Nmtoken] = None,
    thickness: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("LaminatingIntent")

final case class Shape3D(width: Double, height: Double, depth: Double) derives CanEqual

object Shape3D:
  given Eq[Shape3D] = Eq.fromUniversalEquals
  given Show[Shape3D] = Show.show(shape => s"${shape.width} ${shape.height} ${shape.depth}")
  given Hash[Shape3D] = Hash.fromUniversalHashCode

final case class GridSize(columns: Int, rows: Int) derives CanEqual

object GridSize:
  given Eq[GridSize] = Eq.fromUniversalEquals
  given Show[GridSize] = Show.show(grid => s"${grid.columns} ${grid.rows}")
  given Hash[GridSize] = Hash.fromUniversalHashCode

opaque type EvenPageCount = Int
object EvenPageCount:
  def from(value: Int): Either[ValidationError, EvenPageCount] =
    Either.cond(
      value >= 0 && value % 2 == 0,
      value,
      ValidationError.InvalidValue("Pages", value.toString, "a non-negative even number"),
    )

  extension (value: EvenPageCount) def value: Int = value
end EvenPageCount

enum Sides derives CanEqual:
  case OneSided, OneSidedBack, TwoSidedHeadToFoot, TwoSidedHeadToHead, Unprinted

enum SpreadType derives CanEqual:
  case SinglePage, Spread

final case class LayoutIntent(
    bleed: Option[Float] = None,
    dimensions: Option[XYPair] = None,
    finishedDimensions: Option[Shape3D] = None,
    namedDimensions: Option[Nmtoken] = None,
    numberUp: Option[GridSize] = None,
    orientation: Option[Orientation] = None,
    pages: Option[EvenPageCount] = None,
    sides: Option[Sides] = None,
    spreadType: Option[SpreadType] = None,
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("LayoutIntent")

enum PrintPreference derives CanEqual:
  case Balanced, CostEffective, Fastest, HighestQuality

final case class ProductionIntent(
    printPreference: Option[PrintPreference] = None,
    printProcesses: Vector[Nmtoken] = Vector.empty,
    certifications: Vector[Certification] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("ProductionIntent")

enum VariableType derives CanEqual:
  case OneLine, AddressField, IdentificationField, Area

enum VariableQuality derives CanEqual:
  case Simple, Imprint, Full

final case class VariableIntent(
    variableType: VariableType,
    area: Option[Float] = None,
    averagePages: Option[Int] = None,
    childRefs: Vector[XsdIdRef] = Vector.empty,
    colorsUsedBack: Vector[Nmtoken] = Vector.empty,
    colorsUsedFront: Vector[Nmtoken] = Vector.empty,
    maxPages: Option[Int] = None,
    minPages: Option[Int] = None,
    numberOfCopies: Option[Int] = None,
    variableQuality: Option[VariableQuality] = None,
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("VariableIntent")

/** Schema-defined empty ProductIntent extension point used by extension resource descriptions. */
final case class IntentResource(
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("IntentResource")
