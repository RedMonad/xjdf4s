package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.{Rectangle, XYPair}

enum IdentificationEncoding derives CanEqual:
  case ASCII, Barcode, Braille, RFID

enum IdentificationPurpose derives CanEqual:
  case Label, Separation, Verification

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
    value: XjdfString,
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
    valueFormat: XjdfString,
    valueTemplate: NonEmptyVector[Nmtoken],
    expressions: Vector[MetadataExpression] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class IdentificationField(
    boundingBox: Option[Rectangle] = None,
    encoding: Option[IdentificationEncoding] = None,
    encodingDetails: Option[Nmtoken] = None,
    format: Option[XjdfString] = None,
    orientation: Option[Matrix] = None,
    position: Option[Face] = None,
    purpose: Option[IdentificationPurpose] = None,
    purposeDetails: Option[Nmtoken] = None,
    value: Option[XjdfString] = None,
    valueFormat: Option[XjdfString] = None,
    valueTemplate: Vector[Nmtoken] = Vector.empty,
    barcodeDetails: Option[BarcodeDetails] = None,
    extraValues: Option[ExtraValues] = None,
    metadataMaps: Vector[MetadataMap] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible
