package xjdf4s.model

import xjdf4s.core.*

/** Audit is an XSD choice; concrete audit variants are delivered in the audit slice. */
trait Audit extends XjdfNode:
  def elementName: Nmtoken
end Audit

final case class NamedAudit(elementName: Nmtoken, content: ExtensionElement) extends Audit

final case class AuditPool(
    audits: NonEmptyVector[Audit],
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

/** Root XJDF job/process document (chapter 3.1). */
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
    descriptiveName: Option[String] = None,
    icsVersions: Vector[Nmtoken] = Vector.empty,
    jobPartId: Option[Nmtoken] = None,
    projectId: Option[Nmtoken] = None,
    relatedJobId: Option[Nmtoken] = None,
    relatedJobPartId: Option[Nmtoken] = None,
    relatedProjectId: Option[Nmtoken] = None,
    version: Option[Version] = None,
    schema: Option[UriRef] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible
