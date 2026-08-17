package xjdf4s.model

import xjdf4s.core.*

/**
 * Root XJDF job/process document (chapter 3.1). Transport-specific members (the JSON-only `$schema` and `@Name`
 * properties) are intentionally absent from the domain root; they are codec concerns.
 */
final case class XJDF(
    jobId: Nmtoken,
    types: NonEmptyVector[Nmtoken],
    auditPool: Option[AuditPool] = None,
    comments: Vector[Comment] = Vector.empty,
    generalIds: Vector[GeneralId] = Vector.empty,
    productList: Option[ProductList] = None,
    resourceSets: Vector[ResourceSet] = Vector.empty,
    category: Option[Nmtoken] = None,
    commentUrl: Option[UriRef] = None,
    descriptiveName: Option[XjdfString] = None,
    icsVersions: Vector[Nmtoken] = Vector.empty,
    jobPartId: Option[Nmtoken] = None,
    projectId: Option[Nmtoken] = None,
    relatedJobId: Option[Nmtoken] = None,
    relatedJobPartId: Option[Nmtoken] = None,
    relatedProjectId: Option[Nmtoken] = None,
    version: Option[Version] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible,
      ValidatedNode:

  override def validate: Vector[ValidationError] =
    val companionErrors =
      if relatedJobPartId.nonEmpty && relatedJobId.isEmpty then
        Vector(ValidationError.MissingCompanionValue("XJDF/@RelatedJobPartID", "XJDF/@RelatedJobID"))
      else Vector.empty
    val resourceIds = resourceSets.flatMap: set =>
      set.id.toVector.map(_.value) ++ set.resources.flatMap(resource => resource.id.toVector.map(_.value))
    val duplicateIds = resourceIds
      .groupBy(identity)
      .iterator
      .collect { case (_, occurrences) if occurrences.size > 1 => ValidationError.DuplicateId(occurrences.head) }
      .toVector
    companionErrors ++ duplicateIds
end XJDF
