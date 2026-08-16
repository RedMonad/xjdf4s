package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum InkState derives CanEqual:
  case Dry, Wet
end InkState

enum MeasurementFilter derives CanEqual:
  case None, Pol, UV
end MeasurementFilter

enum SampleBacking derives CanEqual:
  case Black, Substrate, White
end SampleBacking

enum WhiteBase derives CanEqual:
  case Absolute, Substrate
end WhiteBase

final case class ColorMeasurementConditions(
    aperture: Option[Float] = None,
    densityStandard: Option[Nmtoken] = None,
    illumination: Option[Float] = None,
    illuminationAngle: Option[Int] = None,
    inkState: Option[InkState] = None,
    measurementAngle: Option[Int] = None,
    measurementFilter: Option[MeasurementFilter] = None,
    measurementMode: Option[Nmtoken] = None,
    observer: Option[Int] = None,
    sampleBacking: Option[SampleBacking] = None,
    spectralResolution: Option[Float] = None,
    whiteBase: Option[WhiteBase] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum TabCollationOrder derives CanEqual:
  case Forward, Reverse
end TabCollationOrder

final case class TabDimensions(
    tabEdge: Option[BindingEdge] = None,
    tabExtensionDistance: Option[Float] = None,
    tabOffset: Option[Float] = None,
    tabSetCollationOrder: Option[TabCollationOrder] = None,
    tabsPerBank: Option[Int] = None,
    tabWidth: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum ImagableSide derives CanEqual:
  case Front, Back, Both, Neither
end ImagableSide

enum MediaUnit derives CanEqual:
  case Continuous, Roll, Sheet
end MediaUnit

enum PlateTechnology derives CanEqual:
  case FlexoAnalogSolvent, FlexoAnalogThermal, FlexoDigitalSolvent, FlexoDigitalThermal
  case FlexoDirectEngraving, InkJet, Thermal, UV, Visible
end PlateTechnology

final case class MediaLayers(
    glue: Glue,
    media: Media,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Media(
    mediaType: MediaType,
    backBrightness: Option[Float] = None,
    backCieTint: Option[Float] = None,
    backCieWhiteness: Option[Float] = None,
    backCoating: Option[Coating] = None,
    backCoatingDetail: Option[Nmtoken] = None,
    backGlossValue: Option[Float] = None,
    backIsoPaperSubstrate: Option[IsoPaperSubstrate] = None,
    backLabColorValue: Option[LabColor] = None,
    backSpectrum: Vector[Float] = Vector.empty,
    brightness: Option[Float] = None,
    cieTint: Option[Float] = None,
    cieWhiteness: Option[Float] = None,
    coating: Option[Coating] = None,
    coatingDetail: Option[Nmtoken] = None,
    coreWeight: Option[Float] = None,
    dimension: Option[XYPair] = None,
    flute: Option[Nmtoken] = None,
    fluteDirection: Option[MediaDirection] = None,
    glossValue: Option[Float] = None,
    grainDirection: Option[MediaDirection] = None,
    imagableSide: Option[ImagableSide] = None,
    innerCoreDiameter: Option[Float] = None,
    insideLoss: Option[Float] = None,
    isoPaperSubstrate: Option[IsoPaperSubstrate] = None,
    labColorValue: Option[LabColor] = None,
    mediaColorName: Option[String] = None,
    mediaColorNameDetails: Option[String] = None,
    mediaQuality: Option[String] = None,
    mediaSetCount: Option[Int] = None,
    mediaTypeDetails: Option[Nmtoken] = None,
    mediaUnit: Option[MediaUnit] = None,
    opacity: Option[Opacity] = None,
    opacityLevel: Option[Float] = None,
    outerCoreDiameter: Option[Float] = None,
    outsideGain: Option[Float] = None,
    plateTechnology: Option[PlateTechnology] = None,
    polarity: Option[Polarity] = None,
    printingTechnology: Option[Nmtoken] = None,
    recycledPercentage: Option[Float] = None,
    reliefThickness: Option[Float] = None,
    rollDiameter: Option[Float] = None,
    shrinkIndex: Option[XYPair] = None,
    sleeveInterlock: Option[Nmtoken] = None,
    spectrum: Vector[Float] = Vector.empty,
    stockType: Option[Nmtoken] = None,
    texture: Option[Nmtoken] = None,
    thickness: Option[Float] = None,
    weight: Option[Float] = None,
    certifications: Vector[Certification] = Vector.empty,
    colorMeasurementConditions: Option[ColorMeasurementConditions] = None,
    holePatterns: Vector[HolePattern] = Vector.empty,
    identificationFields: Vector[IdentificationField] = Vector.empty,
    mediaLayers: Option[MediaLayers] = None,
    tabDimensions: Option[TabDimensions] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Media")

final case class CmykColor(cyan: Double, magenta: Double, yellow: Double, black: Double) derives CanEqual
final case class SrgbColor(red: Double, green: Double, blue: Double) derives CanEqual

enum ColorType derives CanEqual:
  case DieLine, Normal, Opaque, OpaqueIgnore, Primer, Transparent
end ColorType

final case class DeviceNColor(
    colorList: Vector[Float],
    name: Nmtoken,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Color(
    actualColorName: Option[String] = None,
    cmyk: Option[CmykColor] = None,
    colorBook: Option[String] = None,
    colorBookEntry: Option[String] = None,
    colorDetails: Option[String] = None,
    colorName: Option[String] = None,
    colorType: Option[ColorType] = None,
    colorTypeDetails: Option[String] = None,
    density: Option[Float] = None,
    gray: Option[Float] = None,
    lab: Option[LabColor] = None,
    neutralDensity: Option[Float] = None,
    printingTechnology: Option[Nmtoken] = None,
    printStandard: Option[String] = None,
    rawName: Vector[Byte] = Vector.empty,
    spectrum: Vector[Float] = Vector.empty,
    srgb: Option[SrgbColor] = None,
    colorMeasurementConditions: Option[ColorMeasurementConditions] = None,
    deviceNColors: Vector[DeviceNColor] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Color")
