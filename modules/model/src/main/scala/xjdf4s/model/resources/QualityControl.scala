package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

opaque type QualityScore = Int
object QualityScore:
  def from(value: Int): Either[ValidationError, QualityScore] =
    Either.cond(
      value >= 0 && value <= 100,
      value,
      ValidationError.InvalidValue("Score", value.toString, "an integer from zero through one hundred"),
    )

  extension (value: QualityScore) def value: Int = value
end QualityScore

enum QualityBase derives CanEqual:
  case Absolute, Standard

enum MeasurementUsage derives CanEqual:
  case Master, Standard

final case class BindingQualityParams(
    flexValue: Option[Float] = None,
    pullOutRange: Option[XYPair] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class BindingQuality(
    flexValue: Option[Float] = None,
    pullOutValue: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ColorMeasurement(
    colorControlStrip: Option[ColorControlStrip] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class RegistrationQuality(
    offset: XYPair,
    reference: Nmtoken,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QualityControlFiles(
    image: Option[FileSpec] = None,
    setup: Option[FileSpec] = None,
)

final case class QualityControlParams(
    box: Option[Rectangle] = None,
    position: Option[Face] = None,
    qualityBase: Option[QualityBase] = None,
    qualityControlMethods: Vector[Nmtoken] = Vector.empty,
    sampleInterval: Option[Int] = None,
    severity: Option[QualityScore] = None,
    sourceDeviceId: Option[Nmtoken] = None,
    timeInterval: Option[XsdDuration] = None,
    bindingQualityParams: Option[BindingQualityParams] = None,
    bindingQuality: Option[BindingQuality] = None,
    colorMeasurement: Option[ColorMeasurement] = None,
    files: QualityControlFiles = QualityControlFiles(),
    registrationQuality: Option[RegistrationQuality] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("QualityControlParams")

final case class BindingQualityMeasurement(
    flexValue: Option[Float] = None,
    pullOutValue: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum DefectKind derives CanEqual:
  case FinishingDefect, ImageDefect, ImageFinishingDefect, Other, SheetDefect, SubstrateDefect

final case class Defect(
    kinds: NonEmptyVector[DefectKind],
    box: Option[Rectangle] = None,
    reason: Option[Nmtoken] = None,
    typeDetails: Option[Nmtoken] = None,
    face: Option[Face] = None,
    severity: Option[QualityScore] = None,
    size: Option[Float] = None,
    comment: Option[Comment] = None,
    fileSpec: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Inspection(
    defects: Vector[Defect] = Vector.empty,
    fileSpec: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QualityControlResult(
    box: Option[Rectangle] = None,
    end: Option[XsdDateTime] = None,
    failed: Option[Int] = None,
    measurements: Option[Int] = None,
    measurementUsage: Vector[MeasurementUsage] = Vector.empty,
    passed: Option[Int] = None,
    position: Option[Face] = None,
    qualityControlMethods: Vector[Nmtoken] = Vector.empty,
    sample: Option[IntegerRange] = None,
    severity: Option[QualityScore] = None,
    sourceDeviceId: Option[Nmtoken] = None,
    start: Option[XsdDateTime] = None,
    bindingQualityMeasurements: Vector[BindingQualityMeasurement] = Vector.empty,
    bindingQuality: Option[BindingQuality] = None,
    colorMeasurement: Option[ColorMeasurement] = None,
    fileSpec: Option[FileSpec] = None,
    inspection: Option[Inspection] = None,
    registrationQuality: Option[RegistrationQuality] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("QualityControlResult")
