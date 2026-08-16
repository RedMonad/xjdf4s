package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

final case class CutBlock(
    blockName: Nmtoken,
    binderySignatureIds: Vector[Nmtoken] = Vector.empty,
    box: Option[Rectangle] = None,
    cutWidth: Option[Float] = None,
    descriptiveName: Option[String] = None,
    externalId: Option[Nmtoken] = None,
    operations: Vector[Nmtoken] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CuttingParams(
    sheetLay: Option[SheetLay] = None,
    cuts: Vector[Cut] = Vector.empty,
    cutBlocks: Vector[CutBlock] = Vector.empty,
    cip3File: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("CuttingParams")

enum ShapeCuttingDeliveryMode derives CanEqual:
  case FullSheet, RemoveGripperMargin, SeparateBlanks
end ShapeCuttingDeliveryMode

final case class ShapeCuttingParams(
    deliveryMode: Option[ShapeCuttingDeliveryMode] = None,
    dieLayoutRef: Option[XsdId] = None,
    moduleId: Option[Nmtoken] = None,
    sheetLay: Option[SheetLay] = None,
    shapes: Vector[Shape] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ShapeCuttingParams")

final case class AutomatedOverPrintParams(
    knockOutCmykWhite: Option[Boolean] = None,
    lineArtBlackLevel: Option[Float] = None,
    overPrintBlackLineArt: Option[Boolean] = None,
    overPrintBlackText: Option[Boolean] = None,
    textBlackLevel: Option[Float] = None,
    textSizeThreshold: Option[Int] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class SeparationControlParams(
    automatedOverPrintParams: Option[AutomatedOverPrintParams] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("SeparationControlParams")

enum ScreeningType(val lexical: String):
  case Adaptive extends ScreeningType("Adaptive")
  case AM extends ScreeningType("AM")
  case ErrorDiffusion extends ScreeningType("ErrorDiffusion")
  case FM extends ScreeningType("FM")
  case HybridAmFm extends ScreeningType("HybridAM-FM")
  case HybridAmLineDot extends ScreeningType("HybridAMline-dot")
end ScreeningType

final case class ScreenSelector(
    angle: Option[Float] = None,
    dotSize: Option[Float] = None,
    frequency: Option[Float] = None,
    screeningFamily: Option[String] = None,
    screeningType: Option[ScreeningType] = None,
    separation: Option[Nmtoken] = None,
    sourceFrequencyMax: Option[Float] = None,
    sourceFrequencyMin: Option[Float] = None,
    sourceObjects: Vector[SourceObject] = Vector.empty,
    spotFunction: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ScreeningParams(
    selectors: NonEmptyVector[ScreenSelector],
    ignoreSourceFile: Option[Boolean] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ScreeningParams")

enum BearerBars derives CanEqual:
  case None, TopBottom, Box, BoxHMarks
end BearerBars

enum BarcodeMasking derives CanEqual:
  case None, WhiteBox
end BarcodeMasking

final case class BarcodeReproParams(
    bearerBars: Option[BearerBars] = None,
    height: Option[Float] = None,
    magnification: Option[Float] = None,
    masking: Option[BarcodeMasking] = None,
    moduleHeight: Option[Float] = None,
    moduleWidth: Option[Float] = None,
    ratio: Option[Float] = None,
    compensations: Vector[BarcodeCompParams] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("BarcodeReproParams")

enum MarkUsage derives CanEqual:
  case Color, PaperPath, Tile
end MarkUsage

final case class MarkElement(
    center: XYPair,
    markType: Nmtoken,
    separation: Nmtoken,
    rotation: Option[Float] = None,
    size: Option[XYPair] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class RegisterMark(
    center: Option[XYPair] = None,
    markName: Option[Nmtoken] = None,
    markTypes: Vector[Nmtoken] = Vector.empty,
    markUsage: Vector[MarkUsage] = Vector.empty,
    rotation: Option[Float] = None,
    separations: Vector[Nmtoken] = Vector.empty,
    size: Option[XYPair] = None,
    markElements: Vector[MarkElement] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("RegisterMark")
