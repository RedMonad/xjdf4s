package xjdf4s.model

import xjdf4s.core.*

enum EmbossDirection derives CanEqual:
  case Both, Depressed, Flat, Raised
end EmbossDirection

enum EmbossType derives CanEqual:
  case BlindEmbossing, Braille, EmbossedFinish, FoilEmbossing, FoilStamping
end EmbossType

final case class EmbossingItem(
    embossingType: EmbossType,
    direction: Option[EmbossDirection] = None,
    face: Option[Face] = None,
    foilColor: Option[String] = None,
    foilColorDetails: Option[String] = None,
    height: Option[Float] = None,
    imageSize: Option[XYPair] = None,
    position: Option[XYPair] = None,
    separation: Option[Nmtoken] = None,
    toolName: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class EmbossingIntent(
    items: NonEmptyVector[EmbossingItem],
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("EmbossingIntent")

enum TopBottom derives CanEqual:
  case Top, Bottom
end TopBottom

final case class Crease(
    depth: Option[Float] = None,
    startPosition: Option[XYPair] = None,
    workingDirection: Option[TopBottom] = None,
    workingPath: Option[XYPair] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum FoldFrom derives CanEqual:
  case Front, Left
end FoldFrom

final case class Fold(
    from: FoldFrom,
    to: NonEmptyVector[Nmtoken],
    travel: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Perforate(
    depth: Option[Float] = None,
    startPosition: Option[XYPair] = None,
    teethPerDimension: Option[Float] = None,
    workingDirection: Option[TopBottom] = None,
    workingPath: Option[XYPair] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class FoldingIntent(
    foldCatalog: Option[Nmtoken] = None,
    foldingDetails: Option[Nmtoken] = None,
    orientation: Option[Orientation] = None,
    creases: Vector[Crease] = Vector.empty,
    folds: Vector[Fold] = Vector.empty,
    perforations: Vector[Perforate] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("FoldingIntent")

enum HoleCenterReference derives CanEqual:
  case RegistrationMark, TrailingEdge
end HoleCenterReference

enum HoleReferenceEdge derives CanEqual:
  case Bottom, Left, Pattern, Right, Top
end HoleReferenceEdge

enum HoleShape derives CanEqual:
  case Elliptic, Rectangular, Round
end HoleShape

enum HolePatternCatalog(val lexical: String):
  case None extends HolePatternCatalog("None")
  case S1Generic extends HolePatternCatalog("S1-generic")
  case SGeneric extends HolePatternCatalog("S-generic")
  case R2Generic extends HolePatternCatalog("R2-generic")
  case R2mDin extends HolePatternCatalog("R2m-DIN")
  case R2mIso extends HolePatternCatalog("R2m-ISO")
  case R2mMib extends HolePatternCatalog("R2m-MIB")
  case R2iUsA extends HolePatternCatalog("R2i-US-a")
  case R2iUsB extends HolePatternCatalog("R2i-US-b")
  case R3Generic extends HolePatternCatalog("R3-generic")
  case R3iUs extends HolePatternCatalog("R3i-US")
  case R4Generic extends HolePatternCatalog("R4-generic")
  case R4mDinA4 extends HolePatternCatalog("R4m-DIN-A4")
  case R4mDinA5 extends HolePatternCatalog("R4m-DIN-A5")
  case R4mSwedish extends HolePatternCatalog("R4m-swedish")
  case R4iUs extends HolePatternCatalog("R4i-US")
  case R5Generic extends HolePatternCatalog("R5-generic")
  case R5iUsA extends HolePatternCatalog("R5i-US-a")
  case R5iUsB extends HolePatternCatalog("R5i-US-b")
  case R5iUsC extends HolePatternCatalog("R5i-US-c")
  case R6Generic extends HolePatternCatalog("R6-generic")
  case R6m4h2s extends HolePatternCatalog("R6m-4h2s")
  case R6mDinA5 extends HolePatternCatalog("R6m-DIN-A5")
  case R7Generic extends HolePatternCatalog("R7-generic")
  case R7iUsA extends HolePatternCatalog("R7i-US-a")
  case R7iUsB extends HolePatternCatalog("R7i-US-b")
  case R7iUsC extends HolePatternCatalog("R7i-US-c")
  case R11m7h4s extends HolePatternCatalog("R11m-7h4s")
  case P16_9iRect0t extends HolePatternCatalog("P16_9i-rect-0t")
  case P12mRect0t extends HolePatternCatalog("P12m-rect-0t")
  case W2_1iRound0t extends HolePatternCatalog("W2_1i-round-0t")
  case W2_1iSquare0t extends HolePatternCatalog("W2_1i-square-0t")
  case W3_1iSquare0t extends HolePatternCatalog("W3_1i-square-0t")
  case C9_5mRound0t extends HolePatternCatalog("C9.5m-round-0t")
end HolePatternCatalog

final case class HolePattern(
    center: Option[XYPair] = None,
    centerReference: Option[HoleCenterReference] = None,
    extent: Option[XYPair] = None,
    holeCount: Vector[Int] = Vector.empty,
    pattern: Option[HolePatternCatalog] = None,
    pitch: Option[XYPair] = None,
    referenceEdge: Option[HoleReferenceEdge] = None,
    reinforcement: Option[Nmtoken] = None,
    shape: Option[HoleShape] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class HoleMakingIntent(
    patterns: NonEmptyVector[HolePattern],
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("HoleMakingIntent")
