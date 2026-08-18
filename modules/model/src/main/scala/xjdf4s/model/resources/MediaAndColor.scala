package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum InkState derives CanEqual:
  case Dry, Wet

enum MeasurementFilter derives CanEqual:
  case None, Pol, UV

enum SampleBacking derives CanEqual:
  case Black, Substrate, White

enum WhiteBase derives CanEqual:
  case Absolute, Substrate

final case class ColorMeasurementConditions(
    aperture: Option[Float] = None,
    densityStandard: Option[Nmtoken] = None,
    illumination: Option[Nmtoken] = None,
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

enum MediaUnit derives CanEqual:
  case Continuous, Roll, Sheet

enum PlateTechnology derives CanEqual:
  case FlexoAnalogSolvent, FlexoAnalogThermal, FlexoDigitalSolvent, FlexoDigitalThermal
  case FlexoDirectEngraving, InkJet, Thermal, UV, Visible

/** One ordered entry of a MediaLayers sequence: either a glue layer or a media layer (section 8.28). */
enum MediaLayer derives CanEqual:
  case GlueLayer(value: Glue)
  case MediaLayer(value: Media)

/** Section 8.28: an ordered list of `Glue* | Media*` subelements. The order SHALL precisely describe the order of the
 *  individual layers; the first and the last layer SHALL be `Media` layers (front and back of the composite). The JSON
 *  `@Name` exception for in-lined layers is a codec concern and is not part of the domain representation.
 */
final case class MediaLayers(
    layers: Vector[MediaLayer],
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible,
      ValidatedNode:

  override def validate: Vector[ValidationError] =
    val emptyErrors =
      if layers.isEmpty then Vector(ValidationError.EmptyCollection("MediaLayers"))
      else Vector.empty
    val boundaryErrors =
      if layers.nonEmpty then
        val frontErrors = layers.head match
          case _: MediaLayer.MediaLayer => Vector.empty
          case _: MediaLayer.GlueLayer => Vector(ValidationError.InvalidValue("MediaLayers/first", "Glue", "Media"))
        val backErrors = layers.last match
          case _: MediaLayer.MediaLayer => Vector.empty
          case _: MediaLayer.GlueLayer => Vector(ValidationError.InvalidValue("MediaLayers/last", "Glue", "Media"))
        frontErrors ++ backErrors
      else Vector.empty
    emptyErrors ++ boundaryErrors
end MediaLayers

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
    backSpectrum: Option[TransferFunction] = None,
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
    mediaColorName: Option[NamedColor] = None,
    mediaColorNameDetails: Option[XjdfString] = None,
    mediaQuality: Option[XjdfString] = None,
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
    spectrum: Option[TransferFunction] = None,
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
) extends SpecificResource,
      ValidatedNode:
  val elementName: QualifiedName = XjdfNames.element("Media")

  override def validate: Vector[ValidationError] =
    val companionErrors =
      if backIsoPaperSubstrate.nonEmpty && isoPaperSubstrate.isEmpty then
        Vector(ValidationError.MissingCompanionValue("Media/@BackISOPaperSubstrate", "Media/@ISOPaperSubstrate"))
      else Vector.empty
    companionErrors ++ mediaLayers.toVector.flatMap(_.validate)
end Media

enum ColorType derives CanEqual:
  case DieLine, Normal, Opaque, OpaqueIgnore, Primer, Transparent

final case class DeviceNColor(
    colorList: Vector[Float],
    name: Nmtoken,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Color(
    actualColorName: Option[XjdfString] = None,
    cmyk: Option[CmykColor] = None,
    colorBook: Option[XjdfString] = None,
    colorBookEntry: Option[XjdfString] = None,
    colorDetails: Option[XjdfString] = None,
    colorName: Option[NamedColor] = None,
    colorType: Option[ColorType] = None,
    colorTypeDetails: Option[XjdfString] = None,
    density: Option[Float] = None,
    gray: Option[Float] = None,
    lab: Option[LabColor] = None,
    neutralDensity: Option[NeutralDensity] = None,
    printingTechnology: Option[Nmtoken] = None,
    printStandard: Option[XjdfString] = None,
    rawName: Vector[Byte] = Vector.empty,
    spectrum: Option[TransferFunction] = None,
    srgb: Option[SrgbColor] = None,
    colorMeasurementConditions: Option[ColorMeasurementConditions] = None,
    deviceNColors: Vector[DeviceNColor] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Color")
