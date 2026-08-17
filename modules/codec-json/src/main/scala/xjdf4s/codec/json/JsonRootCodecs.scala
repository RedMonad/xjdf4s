package xjdf4s.codec.json

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.*

import xjdf4s.core.*
import xjdf4s.model.*

/**
 * JSON codecs for the XJDF root document. The root is the top of the codec dependency graph: it references the
 * node codecs (Comment, GeneralID, ResourceSet), the scalar codecs and the AuditPool codecs, so it lives in its
 * own file - otherwise `JsonNodeCodecs` would have to depend on `JsonAuditCodecs`, whose own codecs reference
 * `Part` and the messaging codecs, closing a file-level cycle.
 */
object JsonRootCodecs:

  given Encoder[XJDF] = Encoder.instance(document =>
    if document.productList.nonEmpty then
      throw new UnsupportedOperationException("ProductList is not covered by the JSON codec slice yet")
    JsonHelpers.obj(
      JsonHelpers.memberList(
        document.auditPool.toVector.map(pool => JsonHelpers.member("AuditPool", pool.asJson)),
        JsonHelpers.optMember("Category", document.category),
        JsonHelpers.optMember("CommentURL", document.commentUrl),
        JsonHelpers.optMember("DescriptiveName", document.descriptiveName),
        JsonHelpers.vecMember("ICSVersions", document.icsVersions),
        Vector(JsonHelpers.member("JobID", Json.fromString(document.jobId.value))),
        JsonHelpers.optMember("JobPartID", document.jobPartId),
        JsonHelpers.optMember("ProjectID", document.projectId),
        JsonHelpers.optMember("RelatedJobID", document.relatedJobId),
        JsonHelpers.optMember("RelatedJobPartID", document.relatedJobPartId),
        JsonHelpers.optMember("RelatedProjectID", document.relatedProjectId),
        Vector(JsonHelpers.member("Types", Json.arr(document.types.toVector.map(token => Json.fromString(token.value))*))),
        JsonHelpers.optMember("Version", document.version),
        JsonHelpers.vecMember("Comment", document.comments),
        JsonHelpers.vecMember("GeneralID", document.generalIds),
        JsonHelpers.vecMember("ResourceSet", document.resourceSets),
        Vector(JsonHelpers.rootName("XJDF")),
      ),
    ),
  )
  given Decoder[XJDF] = Decoder.instance(cursor =>
    for
      jobId <- cursor.get[Nmtoken]("JobID")
      types <- cursor.get[List[Nmtoken]]("Types")
      nonEmptyTypes <- NonEmptyVector.from(types.toVector) match
        case Right(nonEmpty) => Right(nonEmpty)
        case Left(_)         => JsonHelpers.fail(cursor, "Types must not be empty")
      category <- JsonHelpers.opt[Nmtoken](cursor, "Category")
      commentUrl <- JsonHelpers.opt[UriRef](cursor, "CommentURL")
      descriptiveName <- JsonHelpers.opt[XjdfString](cursor, "DescriptiveName")
      icsVersions <- JsonHelpers.vec[Nmtoken](cursor, "ICSVersions")
      jobPartId <- JsonHelpers.opt[Nmtoken](cursor, "JobPartID")
      projectId <- JsonHelpers.opt[Nmtoken](cursor, "ProjectID")
      relatedJobId <- JsonHelpers.opt[Nmtoken](cursor, "RelatedJobID")
      relatedJobPartId <- JsonHelpers.opt[Nmtoken](cursor, "RelatedJobPartID")
      relatedProjectId <- JsonHelpers.opt[Nmtoken](cursor, "RelatedProjectID")
      version <- JsonHelpers.opt[Version](cursor, "Version")
      comments <- JsonHelpers.vec[Comment](cursor, "Comment")
      generalIds <- JsonHelpers.vec[GeneralId](cursor, "GeneralID")
      resourceSets <- JsonHelpers.vec[ResourceSet](cursor, "ResourceSet")
      auditPool <- JsonHelpers.opt[AuditPool](cursor, "AuditPool")
    yield XJDF(
      jobId,
      nonEmptyTypes,
      auditPool,
      comments,
      generalIds,
      None,
      resourceSets,
      category,
      commentUrl,
      descriptiveName,
      icsVersions,
      jobPartId,
      projectId,
      relatedJobId,
      relatedJobPartId,
      relatedProjectId,
      version,
    ),
  )
end JsonRootCodecs
