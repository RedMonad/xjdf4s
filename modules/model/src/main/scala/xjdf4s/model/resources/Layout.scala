package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

final case class MarkObject(
    contentRef: Option[XsdIdRef] = None,
    colorControlStrips: Vector[ColorControlStrip] = Vector.empty,
    cutMarks: Vector[CutMark] = Vector.empty,
    registerMarks: Vector[RegisterMark] = Vector.empty,
    scavengerAreas: Vector[ScavengerArea] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum PlacedObjectKind:
  case Content
  case Mark(value: MarkObject)

final case class PageActivation(
    conditions: NonEmptyVector[Condition],
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class PageCondition(
    conditions: NonEmptyVector[Condition],
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class PlacedObject(
    ctm: Matrix,
    kind: PlacedObjectKind,
    anchor: Option[Anchor] = None,
    clipBox: Option[Rectangle] = None,
    clipPath: Option[PdfPath] = None,
    halfTonePhaseOrigin: Option[XYPair] = None,
    id: Option[XsdId] = None,
    order: Option[Int] = None,
    positionRef: Option[XsdIdRef] = None,
    sourceClipPath: Option[PdfPath] = None,
    trimCtm: Option[Matrix] = None,
    trimSize: Option[XYPair] = None,
    pageActivation: Option[PageActivation] = None,
    pageCondition: Option[PageCondition] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class LayoutPosition(
    absoluteBox: Option[Rectangle] = None,
    anchor: Option[Anchor] = None,
    binderySignatureId: Option[Nmtoken] = None,
    blockName: Option[Nmtoken] = None,
    gangElementId: Option[Nmtoken] = None,
    id: Option[XsdId] = None,
    marginBottom: Option[Float] = None,
    marginLeft: Option[Float] = None,
    marginRight: Option[Float] = None,
    marginTop: Option[Float] = None,
    orientation: Option[Orientation] = None,
    positionOrder: Option[Int] = None,
    relativeBox: Option[Rectangle] = None,
    stackDepth: Option[Int] = None,
    stackOrder: Option[Int] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class SheetActivation(
    conditions: NonEmptyVector[Condition],
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Layout(
    anchor: Option[Anchor] = None,
    automated: Option[Boolean] = None,
    expansionBox: Option[Rectangle] = None,
    filmRef: Option[XsdIdRef] = None,
    innermostShingling: Option[Float] = None,
    maxCollect: Option[Int] = None,
    minCollect: Option[Int] = None,
    outermostShingling: Option[Float] = None,
    paperRef: Option[XsdIdRef] = None,
    plateRef: Option[XsdIdRef] = None,
    proofPaperRef: Option[XsdIdRef] = None,
    sheetLay: Option[SheetLay] = None,
    surfaceContentsBox: Option[Rectangle] = None,
    workStyle: Option[WorkStyle] = None,
    devices: Vector[Device] = Vector.empty,
    externalImpositionTemplate: Option[FileSpec] = None,
    fitPolicy: Option[FitPolicy] = None,
    placedObjects: Vector[PlacedObject] = Vector.empty,
    positions: Vector[LayoutPosition] = Vector.empty,
    sheetActivation: Option[SheetActivation] = None,
    stripMarks: Vector[StripMark] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Layout")
