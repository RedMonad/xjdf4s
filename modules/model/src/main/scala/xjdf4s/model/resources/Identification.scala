package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.{Rectangle, XYPair}

enum IdentificationEncoding derives CanEqual:
  case ASCII, Barcode, Braille, RFID
end IdentificationEncoding

enum IdentificationPurpose derives CanEqual:
  case Label, Separation, Verification
end IdentificationPurpose

final case class BarcodeDetails(
    barcodeVersion: Option[Nmtoken] = None,
    errorCorrectionLevel: Option[XYPair] = None,
    xCells: Option[Int] = None,
    yCells: Option[Int] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ExtraValues(
    usage: Nmtoken,
    value: String,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

opaque type XPath = String
object XPath:
  def from(value: String): Either[ValidationError, XPath] =
    Either.cond(value.nonEmpty, value, ValidationError.EmptyValue("XPath"))

  extension (value: XPath) def value: String = value
end XPath

final case class MetadataExpression(
    name: Nmtoken,
    path: XPath,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class MetadataMap(
    name: Nmtoken,
    valueFormat: String,
    valueTemplate: NonEmptyVector[Nmtoken],
    expressions: Vector[MetadataExpression] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class IdentificationField(
    boundingBox: Option[Rectangle] = None,
    encoding: Option[IdentificationEncoding] = None,
    encodingDetails: Option[Nmtoken] = None,
    format: Option[String] = None,
    orientation: Option[Matrix] = None,
    position: Option[Face] = None,
    purpose: Option[IdentificationPurpose] = None,
    purposeDetails: Option[Nmtoken] = None,
    value: Option[String] = None,
    valueFormat: Option[String] = None,
    valueTemplate: Vector[Nmtoken] = Vector.empty,
    barcodeDetails: Option[BarcodeDetails] = None,
    extraValues: Option[ExtraValues] = None,
    metadataMaps: Vector[MetadataMap] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible
