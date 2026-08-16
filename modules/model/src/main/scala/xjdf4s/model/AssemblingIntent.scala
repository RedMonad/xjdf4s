package xjdf4s.model

import xjdf4s.core.*

private val XjdfNamespace = "http://www.CIP4.org/JDFSchema_2_0"

final case class AssemblyItem(
    childRef: XsdId,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class BindIn(
    childRef: XsdId,
    folio: Option[Int] = None,
    orientation: Option[Orientation] = None,
    position: Option[XYPair] = None,
    glue: Option[Glue] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class BlowIn(
    childRef: XsdId,
    folioFrom: Option[Int] = None,
    folioTo: Option[Int] = None,
    orientation: Option[Orientation] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum ProductLocation derives CanEqual:
  case OnFace(face: Face)
  case OnFolio(folio: Int)
end ProductLocation

final case class StickOn(
    childRef: XsdId,
    location: Option[ProductLocation] = None,
    orientation: Option[Orientation] = None,
    position: Option[XYPair] = None,
    glue: Option[Glue] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum GlueType derives CanEqual:
  case ColdGlue, Hotmelt, Permanent, PUR, Removable
end GlueType

enum GluingTechnique derives CanEqual:
  case SpineGluing, SideGluingFront, SideGluingBack
end GluingTechnique

final case class Glue(
    areaGlue: Option[Boolean] = None,
    glueLineWidth: Option[Float] = None,
    glueRef: Option[XsdId] = None,
    glueType: Option[GlueType] = None,
    gluingPattern: Vector[Float] = Vector.empty,
    gluingTechnique: Option[GluingTechnique] = None,
    meltingTemperature: Option[Int] = None,
    startPosition: Option[XYPair] = None,
    workingDirection: Option[Face] = None,
    workingPath: Option[XYPair] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class AssemblingIntent(
    container: XsdId,
    assemblyItems: Vector[AssemblyItem] = Vector.empty,
    bindIns: Vector[BindIn] = Vector.empty,
    blowIns: Vector[BlowIn] = Vector.empty,
    stickOns: Vector[StickOn] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = QualifiedName(XjdfNamespace, "AssemblingIntent")
