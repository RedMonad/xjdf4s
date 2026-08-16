package xjdf4s.model

import xjdf4s.core.*

/** Open XSD extension point with 100 schema-defined descendants plus foreign-namespace descendants. */
trait SpecificResource extends XjdfNode:
  def elementName: QualifiedName
end SpecificResource

/** Lossless schema-shaped fallback while dedicated resource records are added slice by slice. */
final case class NamedSpecificResource(
    elementName: QualifiedName,
    attributes: Map[QualifiedName, ExtensionValue] = Map.empty,
    children: Vector[ExtensionElement] = Vector.empty,
) extends SpecificResource

final case class PartWaste(
    waste: Float,
    moduleIds: Vector[Nmtoken] = Vector.empty,
    wasteDetails: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class PartAmount(
    amount: Option[Float] = None,
    maxAmount: Option[Float] = None,
    minAmount: Option[Float] = None,
    waste: Option[Float] = None,
    parts: Vector[Part] = Vector.empty,
    partWaste: Vector[PartWaste] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class AmountPool(
    amounts: NonEmptyVector[PartAmount],
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Resource(
    amountPool: Option[AmountPool] = None,
    comments: Vector[Comment] = Vector.empty,
    generalIds: Vector[GeneralId] = Vector.empty,
    parts: Vector[Part] = Vector.empty,
    specificResource: Option[SpecificResource] = None,
    foreignElements: Vector[ExtensionElement] = Vector.empty,
    brand: Option[Boolean] = None,
    commentUrl: Option[UriRef] = None,
    descriptiveName: Option[String] = None,
    duration: Option[XsdDuration] = None,
    expires: Option[XsdDateTime] = None,
    externalId: Option[Nmtoken] = None,
    grossWeight: Option[Float] = None,
    id: Option[XsdId] = None,
    orientation: Option[Orientation] = None,
    resourceWeight: Option[Float] = None,
    start: Option[XsdDateTime] = None,
    startOffset: Option[XsdDuration] = None,
    status: Option[ResourceAvailability] = None,
    transformation: Option[Matrix] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Dependent(
    jobId: Nmtoken,
    jobPartId: Option[Nmtoken] = None,
    pipeId: Option[Nmtoken] = None,
    pipeProtocol: Option[Nmtoken] = None,
    xjmfUrl: Option[UriRef] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ResourceSet(
    name: Nmtoken,
    combinedProcessIndex: Vector[Float] = Vector.empty,
    commentUrl: Option[UriRef] = None,
    descriptiveName: Option[String] = None,
    id: Option[XsdId] = None,
    processUsage: Option[Nmtoken] = None,
    unit: Option[Nmtoken] = None,
    usage: Option[ResourceUsage] = None,
    comments: Vector[Comment] = Vector.empty,
    dependents: Vector[Dependent] = Vector.empty,
    generalIds: Vector[GeneralId] = Vector.empty,
    resources: Vector[Resource] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible
