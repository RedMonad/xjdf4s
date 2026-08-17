package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum DueLevel derives CanEqual:
  case Trivial, Penalty, JobCancelled
end DueLevel

enum Automation derives CanEqual:
  case Dynamic, Static
end Automation

enum Polarity derives CanEqual:
  case Negative, Positive
end Polarity

enum PlateType derives CanEqual:
  case Exposed, Dummy
end PlateType

final case class CustomerInfo(
    customerId: Option[Nmtoken] = None,
    customerJobName: Option[XjdfString] = None,
    customerOrderId: Option[XjdfString] = None,
    customerProjectId: Option[XjdfString] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("CustomerInfo")

final case class NodeInfo(
    cleanupDuration: Option[XsdDuration] = None,
    dueLevel: Option[DueLevel] = None,
    end: Option[XsdDateTime] = None,
    firstEnd: Option[XsdDateTime] = None,
    firstStart: Option[XsdDateTime] = None,
    jobPriority: Option[Priority0To100] = None,
    lastEnd: Option[XsdDateTime] = None,
    lastStart: Option[XsdDateTime] = None,
    naturalLanguage: Option[LanguageTag] = None,
    personalId: Option[Nmtoken] = None,
    setupDuration: Option[XsdDuration] = None,
    start: Option[XsdDateTime] = None,
    status: Option[NodeStatus] = None,
    statusDetails: Option[Nmtoken] = None,
    totalDuration: Option[XsdDuration] = None,
    gangSources: Vector[GangSource] = Vector.empty,
    misDetails: Option[MisDetails] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("NodeInfo")

final case class Component(
    automation: Option[Automation] = None,
    cartonTopFlaps: Option[XYPair] = None,
    columns: Option[Int] = None,
    contentRefs: Vector[XsdIdRef] = Vector.empty,
    dimensions: Option[Shape3D] = None,
    maxHeat: Option[Float] = None,
    mediaRef: Option[XsdIdRef] = None,
    overfold: Option[Float] = None,
    overfoldSide: Option[Side] = None,
    productType: Option[Nmtoken] = None,
    productTypeDetails: Option[XjdfString] = None,
    readerPageCount: Option[Int] = None,
    surfaceCount: Option[Int] = None,
    windingResult: Option[Int] = None,
    identificationFields: Vector[IdentificationField] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Component")

final case class ExposedMedia(
    mediaRef: XsdIdRef,
    plateType: Option[PlateType] = None,
    polarity: Option[Polarity] = None,
    punchType: Option[Nmtoken] = None,
    resolution: Option[XYPair] = None,
    identificationFields: Vector[IdentificationField] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ExposedMedia")

final case class Ink(
    inkTypes: Vector[Nmtoken] = Vector.empty,
    specificYield: Option[Float] = None,
    certifications: Vector[Certification] = Vector.empty,
    identificationFields: Vector[IdentificationField] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Ink")
