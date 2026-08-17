package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

final case class BendingParams(
    bend: Option[Boolean] = None,
    punch: Option[Boolean] = None,
    punchType: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("BendingParams")

final case class DevelopingParams(
    postBakeTemperature: Option[Float] = None,
    postBakeTime: Option[XsdDuration] = None,
    postExposeTime: Option[XsdDuration] = None,
    preHeatTemperature: Option[Float] = None,
    preHeatTime: Option[XsdDuration] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("DevelopingParams")

final case class ManualLaborParams(
    laborType: Nmtoken,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ManualLaborParams")

enum SourceObject derives CanEqual:
  case ImagePhotographic, ImageScreenShot, LineArt, SmoothShades, Text
end SourceObject

final case class ImageEnhancementOperation(
    operation: Nmtoken,
    operationDetails: Option[XjdfString] = None,
    sourceObjects: Vector[SourceObject] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ImageEnhancementParams(
    operations: NonEmptyVector[ImageEnhancementOperation],
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ImageEnhancementParams")

enum DeviceAxis derives CanEqual:
  case None, FeedDirection, MediaWidth, Both
end DeviceAxis

final case class ImageSetterParams(
    advanceDistance: Option[Float] = None,
    burnOutArea: Option[XYPair] = None,
    centerAcross: Option[DeviceAxis] = None,
    cutMedia: Option[Boolean] = None,
    manualFeed: Option[Boolean] = None,
    mirrorAround: Option[DeviceAxis] = None,
    polarity: Option[Polarity] = None,
    rollCut: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ImageSetterParams")

final case class ShiftPoint(
    ctm: Matrix,
    position: XYPair,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class LayoutShift(
    shiftPoints: NonEmptyVector[ShiftPoint],
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("LayoutShift")

final case class ColorCorrectionOperation(
    adjustContrast: Option[Float] = None,
    adjustHue: Option[Float] = None,
    adjustLightness: Option[Float] = None,
    adjustSaturation: Option[Float] = None,
    sourceObjects: Vector[SourceObject] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ColorCorrectionParams(
    operations: Vector[ColorCorrectionOperation] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ColorCorrectionParams")

enum PreviewAspectRatio derives CanEqual:
  case CenterMax, CenterMin, Crop, Expand, Ignore
end PreviewAspectRatio

final case class PreviewGenerationParams(
    aspectRatio: Option[PreviewAspectRatio] = None,
    resolution: Option[XYPair] = None,
    size: Option[XYPair] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("PreviewGenerationParams")

final case class TrappingParams(
    imageInternalTrapping: Option[Boolean] = None,
    imageMaskTrapping: Option[Boolean] = None,
    imageToImageTrapping: Option[Boolean] = None,
    imageToObjectTrapping: Option[Boolean] = None,
    minimumBlackWidth: Option[Float] = None,
    stepLimit: Option[Float] = None,
    trapColorScaling: Option[Float] = None,
    trapWidth: Option[XYPair] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("TrappingParams")
