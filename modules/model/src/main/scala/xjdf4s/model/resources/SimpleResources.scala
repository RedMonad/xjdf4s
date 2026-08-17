package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum ResourceShapeType derives CanEqual:
  case Path, Rectangular, Round, RoundedRectangle
end ResourceShapeType

final case class Shape(
    shapeType: ResourceShapeType,
    cutBox: Option[Rectangle] = None,
    cutOut: Option[Boolean] = None,
    cutPath: Option[PdfPath] = None,
    ddesCutType: Option[Int] = None,
    shapeDepth: Option[Float] = None,
    teethPerDimension: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Shape")

final case class Tool(
    toolType: Option[Nmtoken] = None,
    manufacturer: Option[XjdfString] = None,
    manufacturerUrl: Option[UriRef] = None,
    serialNumber: Option[XjdfString] = None,
    identificationFields: Vector[IdentificationField] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Tool")

enum UsageCounterScope derives CanEqual:
  case Lifetime, PowerOn, Job
end UsageCounterScope

final case class UsageCounter(
    scope: UsageCounterScope,
    counterTypes: Vector[Nmtoken] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("UsageCounter")

final case class MiscConsumable(
    consumableType: Nmtoken,
    color: Option[NamedColor] = None,
    colorDetails: Option[XjdfString] = None,
    typeDetails: Option[Nmtoken] = None,
    certifications: Vector[Certification] = Vector.empty,
    identificationFields: Vector[IdentificationField] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("MiscConsumable")

enum PrintQuality derives CanEqual:
  case High, Normal, Draft
end PrintQuality

final case class PrintCondition(
    name: Nmtoken,
    colorantOrder: Vector[Nmtoken] = Vector.empty,
    printQuality: Option[PrintQuality] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("PrintCondition")
