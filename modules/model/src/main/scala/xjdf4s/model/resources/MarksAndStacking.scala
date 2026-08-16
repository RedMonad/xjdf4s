package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum KnockoutSource derives CanEqual:
  case ClipPath, SourceClipPath, TrimBox
end KnockoutSource

final case class MarkColor(name: String, tint: Float) derives CanEqual

final case class FillMark(
    knockoutSource: KnockoutSource,
    colors: NonEmptyVector[MarkColor],
    knockoutBleed: Option[Float] = None,
    knockoutRefs: Vector[XsdId] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CieLabMeasuringField(
    center: XYPair,
    cieLab: LabColor,
    diameter: Option[Float] = None,
    percentages: Vector[Float] = Vector.empty,
    screenRuling: Vector[Float] = Vector.empty,
    screenShape: Option[String] = None,
    tolerance: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class DensityMeasuringField(
    center: XYPair,
    density: Vector[Float],
    diameter: Float,
    dotGain: Float,
    separation: Nmtoken,
    toleranceBlack: Option[XYPair] = None,
    toleranceCyan: Option[XYPair] = None,
    toleranceDotGain: Option[XYPair] = None,
    toleranceMagenta: Option[XYPair] = None,
    toleranceYellow: Option[XYPair] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum PatchUsage derives CanEqual:
  case Color, Ignore, Image, Technical
end PatchUsage

final case class SeparationTint(name: Nmtoken, tint: Float) derives CanEqual

final case class ColorPatch(
    usage: PatchUsage,
    center: Option[XYPair] = None,
    density: Option[Float] = None,
    externalId: Option[Nmtoken] = None,
    lab: Option[LabColor] = None,
    neutralDensity: Option[Float] = None,
    rgb: Option[SrgbColor] = None,
    size: Option[XYPair] = None,
    spectrum: Vector[Float] = Vector.empty,
    separationTints: Vector[SeparationTint] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ColorControlStrip(
    center: Option[XYPair] = None,
    rotation: Option[Float] = None,
    separations: Vector[Nmtoken] = Vector.empty,
    size: Option[XYPair] = None,
    stripType: Option[String] = None,
    cieLabFields: Vector[CieLabMeasuringField] = Vector.empty,
    measurementConditions: Option[ColorMeasurementConditions] = None,
    densityFields: Vector[DensityMeasuringField] = Vector.empty,
    patches: Vector[ColorPatch] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum CutMarkType derives CanEqual:
  case CrossCutMark, TopVerticalCutMark, BottomVerticalCutMark, LeftHorizontalCutMark, RightHorizontalCutMark
  case LowerLeftCutMark, UpperLeftCutMark, LowerRightCutMark, UpperRightCutMark
end CutMarkType

final case class CutMark(markType: CutMarkType, position: XYPair) derives CanEqual

final case class JobField(
    jobFormat: Option[String] = None,
    jobTemplate: Vector[Nmtoken] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ScavengerArea(
    center: XYPair,
    separations: Vector[Nmtoken] = Vector.empty,
    size: Option[XYPair] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum MarkFitPolicy derives CanEqual:
  case NoRepeat, RepeatToFill, RepeatUnclipped, StretchToFit, UndistortedScaleToFit
end MarkFitPolicy

final case class StripMark(
    absoluteHeight: Option[Float] = None,
    absoluteWidth: Option[Float] = None,
    anchor: Option[Anchor] = None,
    font: Option[Nmtoken] = None,
    fontSize: Option[Float] = None,
    horizontalFitPolicy: Option[MarkFitPolicy] = None,
    id: Option[XsdId] = None,
    markName: Option[Nmtoken] = None,
    offset: Option[XYPair] = None,
    orientation: Option[Orientation] = None,
    relativeHeight: Option[Float] = None,
    relativeWidth: Option[Float] = None,
    details: Option[String] = None,
    verticalFitPolicy: Option[MarkFitPolicy] = None,
    barcodeReproduction: Option[BarcodeReproParams] = None,
    colorControlStrips: Vector[ColorControlStrip] = Vector.empty,
    cutMarks: Vector[CutMark] = Vector.empty,
    fillMarks: Vector[FillMark] = Vector.empty,
    identificationFields: Vector[IdentificationField] = Vector.empty,
    jobField: Option[JobField] = None,
    refAnchor: Option[RefAnchor] = None,
    registerMarks: Vector[RegisterMark] = Vector.empty,
    scavengerAreas: Vector[ScavengerArea] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum InsertSheetType derives CanEqual:
  case AccountingSheet, ErrorSheet, JobSheet, SeparatorSheet
end InsertSheetType

enum InsertSheetUsage derives CanEqual:
  case Header, Interleaved, InterleavedBefore, OnError, Slip, SlipCopy, Trailer
end InsertSheetUsage

final case class InsertSheet(
    sheetType: InsertSheetType,
    sheetUsage: InsertSheetUsage,
    isWaste: Option[Boolean] = None,
    mediaRef: Option[XsdId] = None,
    sheetFormat: Option[Nmtoken] = None,
    stripMarks: Vector[StripMark] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum DisjointingDirection derives CanEqual:
  case Alternate, Left, None, Right
end DisjointingDirection

final case class Disjointing(
    amount: Option[Int] = None,
    direction: Option[DisjointingDirection] = None,
    offset: Option[XYPair] = None,
    units: Option[Nmtoken] = None,
    insertSheets: Vector[InsertSheet] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum PreStackMethod derives CanEqual:
  case All, First, None
end PreStackMethod

final case class StackingParams(
    bundleType: Option[BundleType] = None,
    compensate: Option[Boolean] = None,
    layerAmount: Vector[Int] = Vector.empty,
    layerCompression: Option[Boolean] = None,
    layerLift: Option[Boolean] = None,
    maxAmount: Option[Int] = None,
    maxHeight: Option[Int] = None,
    maxWeight: Option[Float] = None,
    minAmount: Option[Int] = None,
    outputBins: Vector[Nmtoken] = Vector.empty,
    preStackAmount: Option[Int] = None,
    preStackMethod: Option[PreStackMethod] = None,
    stackAmount: Option[Int] = None,
    stackCompression: Option[Boolean] = None,
    standardAmount: Option[Int] = None,
    underLays: Vector[Int] = Vector.empty,
    disjointing: Option[Disjointing] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("StackingParams")
