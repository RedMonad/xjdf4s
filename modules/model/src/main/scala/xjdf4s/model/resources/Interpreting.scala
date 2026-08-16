package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum GutterPolicy derives CanEqual:
  case Distribute, Fixed
end GutterPolicy

enum HorizontalGridDirection derives CanEqual:
  case LeftToRight, RightToLeft
end HorizontalGridDirection

enum RotatePolicy derives CanEqual:
  case NoRotate, RotateOrthogonal, RotateClockwise, RotateCounterClockwise
end RotatePolicy

enum FitSizePolicy derives CanEqual:
  case Abort, ClipToMaxPage, CompleteGrid, FillGrid, FitToPage, ReduceToFit, Tile
end FitSizePolicy

enum VerticalGridDirection derives CanEqual:
  case BottomToTop, TopToBottom
end VerticalGridDirection

final case class FitPolicy(
    clipOffset: Option[XYPair] = None,
    expansionPolicy: Option[Nmtoken] = None,
    gutterPolicy: Option[GutterPolicy] = None,
    horizontalGridDirection: Option[HorizontalGridDirection] = None,
    minimumGutter: Option[XYPair] = None,
    rotatePolicy: Option[RotatePolicy] = None,
    sizePolicy: Option[FitSizePolicy] = None,
    verticalGridDirection: Option[VerticalGridDirection] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class InterpretingDetails(
    minimumLineWidth: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class OptionalContentGroupControl(
    include: Boolean,
    name: Option[String] = None,
    processStepsGroup: Option[Nmtoken] = None,
    processStepsType: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum ReferenceXObjectMode derives CanEqual:
  case Ignore, ResolveAlways, ResolveIfPDFX5
end ReferenceXObjectMode

final case class ReferenceXObjectParams(
    mode: ReferenceXObjectMode,
    searchPaths: Vector[FileSpec] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class PdfInterpretingParams(
    emitPdfBlackGeneration: Option[Boolean] = None,
    emitPdfHalftones: Option[Boolean] = None,
    emitPdfTransfers: Option[Boolean] = None,
    emitPdfUnderColorRemoval: Option[Boolean] = None,
    honorPdfOverprint: Option[Boolean] = None,
    iccColorAsDeviceColor: Option[Boolean] = None,
    optionalContentIntent: Option[Nmtoken] = None,
    optionalContentProcess: Option[Nmtoken] = None,
    optionalContentZoom: Option[Float] = None,
    printPdfAnnotations: Option[Boolean] = None,
    printTrapAnnotations: Option[Boolean] = None,
    transparencyRenderingQuality: Option[Float] = None,
    optionalContentControls: Vector[OptionalContentGroupControl] = Vector.empty,
    referenceXObjectParams: Option[ReferenceXObjectParams] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class InterpretingParams(
    center: Option[Boolean] = None,
    filmRef: Option[XsdId] = None,
    mirrorAround: Option[DeviceAxis] = None,
    paperRef: Option[XsdId] = None,
    plateRef: Option[XsdId] = None,
    polarity: Option[Polarity] = None,
    printQuality: Option[PrintQuality] = None,
    proofPaperRef: Option[XsdId] = None,
    scaling: Option[XYPair] = None,
    scalingOrigin: Option[XYPair] = None,
    fitPolicy: Option[FitPolicy] = None,
    interpretingDetails: Option[InterpretingDetails] = None,
    objectResolutions: Vector[ObjectResolution] = Vector.empty,
    pdfInterpretingParams: Option[PdfInterpretingParams] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("InterpretingParams")
