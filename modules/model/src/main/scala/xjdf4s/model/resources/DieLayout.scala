package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum DieSide derives CanEqual:
  case Up, Down
end DieSide

final case class DieStation(
    stationName: Nmtoken,
    binderySignatureIds: Vector[Nmtoken] = Vector.empty,
    shapeDefRef: Option[XsdIdRef] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class DieLayout(
    cutBox: Option[Rectangle] = None,
    cutLines: Vector[Nmtoken] = Vector.empty,
    dieSide: Option[DieSide] = None,
    mediaSide: Option[Side] = None,
    rotated: Option[Boolean] = None,
    waste: Option[Float] = None,
    devices: Vector[Device] = Vector.empty,
    fileSpecs: Vector[FileSpec] = Vector.empty,
    media: Option[Media] = None,
    ruleLengths: Vector[RuleLength] = Vector.empty,
    stations: Vector[DieStation] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("DieLayout")
