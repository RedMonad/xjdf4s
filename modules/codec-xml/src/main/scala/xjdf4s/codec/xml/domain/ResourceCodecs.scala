package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.codec.xml.derivation.*
import xjdf4s.core.*
import xjdf4s.model.*

object ResourceCodec:
  private val KnownChildren = Set("AmountPool", "Comment", "GeneralID", "Part")

  val decoder: XmlDecoder[Resource] =
    XmlDecoder.instance: element =>
      val standardChildren = element.childElements.filter(_.name.namespace == XjdfNamespace.uri)
      val specificCandidates = standardChildren.filter(child => !KnownChildren.contains(child.name.localName))
      val foreignChildren = element.childElements.filter(_.name.namespace != XjdfNamespace.uri)
      for
        amountPool <- XmlDecoders.optionalChild("AmountPool")(AmountPoolCodec.decoder).decode(element)
        comments <- XmlDecoders.repeatedChild("Comment")(CommentCodec.decoder).decode(element)
        generalIds <- XmlDecoders.repeatedChild("GeneralID")(GeneralIdCodec.decoder).decode(element)
        parts <- XmlDecoders.repeatedChild("Part")(PartCodec.decoder).decode(element)
        specific <- specificCandidates match
          case Vector()      => Right(None)
          case Vector(single) => Registry.decodeSpecificResource(single).map(Some(_))
          case _             => Left(XmlError.UnexpectedElement("Resource", specificCandidates(1).name.localName))
        foreign <- foreignChildren.foldLeft[Either[XmlError, Vector[ExtensionElement]]](Right(Vector.empty)) {
          (acc, child) =>
            for
              elements <- acc
              decoded <- ForeignCodec.decodeForeignElement(child)
            yield elements :+ decoded
        }
        brand <- XmlDecoders.attributeOf("Brand")(Lexical.xjdfString).decode(element)
        commentUrl <- XmlDecoders.attributeOf("CommentURL")(Lexical.uri).decode(element)
        descriptiveName <- XmlDecoders.attributeOf("DescriptiveName")(Lexical.xjdfString).decode(element)
        duration <- XmlDecoders.attributeOf("Duration")(Lexical.duration).decode(element)
        expires <- XmlDecoders.attributeOf("Expires")(Lexical.dateTime).decode(element)
        externalId <- XmlDecoders.attributeOf("ExternalID")(Lexical.nmtoken).decode(element)
        grossWeight <- XmlDecoders.attributeOf("GrossWeight")(Lexical.float).decode(element)
        id <- XmlDecoders.attributeOf("ID")(Lexical.xsdId).decode(element)
        orientation <- XmlDecoders.attributeOf("Orientation")(Lexical.orientation).decode(element)
        resourceWeight <- XmlDecoders.attributeOf("ResourceWeight")(Lexical.float).decode(element)
        start <- XmlDecoders.attributeOf("Start")(Lexical.dateTime).decode(element)
        startOffset <- XmlDecoders.attributeOf("StartOffset")(Lexical.duration).decode(element)
        status <- XmlDecoders.attributeOf("Status")(Lexical.resourceAvailability).decode(element)
        transformation <- XmlDecoders.attributeOf("Transformation")(Lexical.matrix).decode(element)
      yield Resource(
        amountPool,
        comments,
        generalIds,
        parts,
        specific,
        foreign,
        brand,
        commentUrl,
        descriptiveName,
        duration,
        expires,
        externalId,
        grossWeight,
        id,
        orientation,
        resourceWeight,
        start,
        startOffset,
        status,
        transformation,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[Resource] =
    XmlEncoder.instance: resource =>
      val attributes =
        CodecHelpers.attributeOf("Brand", resource.brand, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("CommentURL", resource.commentUrl, (v: UriRef) => v.value.toString) ++
          CodecHelpers.attributeOf("DescriptiveName", resource.descriptiveName, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("Duration", resource.duration, (v: XsdDuration) => v.value) ++
          CodecHelpers.attributeOf("Expires", resource.expires, (v: XsdDateTime) => v.value) ++
          CodecHelpers.attributeOf("ExternalID", resource.externalId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("GrossWeight", resource.grossWeight, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("ID", resource.id, (v: XsdId) => v.value) ++
          CodecHelpers.attributeOf("Orientation", resource.orientation, _.toString) ++
          CodecHelpers.attributeOf("ResourceWeight", resource.resourceWeight, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("Start", resource.start, (v: XsdDateTime) => v.value) ++
          CodecHelpers.attributeOf("StartOffset", resource.startOffset, (v: XsdDuration) => v.value) ++
          CodecHelpers.attributeOf("Status", resource.status, _.toString) ++
          CodecHelpers.attributeOf("Transformation", resource.transformation, CodecHelpers.renderMatrix) ++
          CodecHelpers.extensionAttributes(resource.extensions)
      val children =
        resource.amountPool.toVector.map(AmountPoolCodec.encoder.encode) ++
          resource.comments.map(CommentCodec.encoder.encode) ++
          resource.generalIds.map(GeneralIdCodec.encoder.encode) ++
          resource.parts.map(PartCodec.encoder.encode) ++
          resource.specificResource.toVector.map(Registry.encodeSpecificResource) ++
          resource.foreignElements.map(ForeignCodec.encodeForeignElement)
      Xml.Element(CodecHelpers.qname("Resource"), attributes, children)
end ResourceCodec

object DependentCodec:
  val decoder: XmlDecoder[Dependent] =
    XmlDecoder.instance: element =>
      for
        jobId <- XmlDecoders.requiredAttribute("JobID")(Lexical.nmtoken).decode(element)
        jobPartId <- XmlDecoders.attributeOf("JobPartID")(Lexical.nmtoken).decode(element)
        pipeId <- XmlDecoders.attributeOf("PipeID")(Lexical.nmtoken).decode(element)
        pipeProtocol <- XmlDecoders.attributeOf("PipeProtocol")(Lexical.nmtoken).decode(element)
        xjmfUrl <- XmlDecoders.attributeOf("XJMFURL")(Lexical.uri).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
      yield Dependent(jobId, jobPartId, pipeId, pipeProtocol, xjmfUrl, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[Dependent] =
    XmlEncoder.instance: dependent =>
      val attributes =
        CodecHelpers.attributeOf("JobPartID", dependent.jobPartId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("PipeID", dependent.pipeId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("PipeProtocol", dependent.pipeProtocol, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("XJMFURL", dependent.xjmfUrl, (v: UriRef) => v.value.toString) ++
          CodecHelpers.attribute("JobID", Some(dependent.jobId.value)) ++
          CodecHelpers.extensionAttributes(dependent.extensions)
      Xml.Element(CodecHelpers.qname("Dependent"), attributes, Vector.empty)
end DependentCodec

object ResourceSetCodec:
  val decoder: XmlDecoder[ResourceSet] =
    XmlDecoder.instance: element =>
      for
        name <- XmlDecoders.requiredAttribute("Name")(Lexical.nmtoken).decode(element)
        combinedProcessIndex <- XmlDecoders.attributeOf("CombinedProcessIndex")(Lexical.intList).decode(element)
        commentUrl <- XmlDecoders.attributeOf("CommentURL")(Lexical.uri).decode(element)
        descriptiveName <- XmlDecoders.attributeOf("DescriptiveName")(Lexical.xjdfString).decode(element)
        id <- XmlDecoders.attributeOf("ID")(Lexical.xsdId).decode(element)
        processUsage <- XmlDecoders.attributeOf("ProcessUsage")(Lexical.nmtoken).decode(element)
        unit <- XmlDecoders.attributeOf("Unit")(Lexical.nmtoken).decode(element)
        usage <- XmlDecoders.attributeOf("Usage")(Lexical.resourceUsage).decode(element)
        comments <- XmlDecoders.repeatedChild("Comment")(CommentCodec.decoder).decode(element)
        dependents <- XmlDecoders.repeatedChild("Dependent")(DependentCodec.decoder).decode(element)
        generalIds <- XmlDecoders.repeatedChild("GeneralID")(GeneralIdCodec.decoder).decode(element)
        resources <- XmlDecoders.repeatedChild("Resource")(ResourceCodec.decoder).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("Comment", "Dependent", "GeneralID", "Resource")).decode(element)
      yield ResourceSet(
        name,
        combinedProcessIndex.getOrElse(Vector.empty),
        commentUrl,
        descriptiveName,
        id,
        processUsage,
        unit,
        usage,
        comments,
        dependents,
        generalIds,
        resources,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[ResourceSet] =
    XmlEncoder.instance: resourceSet =>
      val attributes =
        CodecHelpers.attribute(
          "CombinedProcessIndex",
          Option.when(resourceSet.combinedProcessIndex.nonEmpty)(CodecHelpers.renderInts(resourceSet.combinedProcessIndex)),
        ) ++
          CodecHelpers.attributeOf("CommentURL", resourceSet.commentUrl, (v: UriRef) => v.value.toString) ++
          CodecHelpers.attributeOf("DescriptiveName", resourceSet.descriptiveName, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("ID", resourceSet.id, (v: XsdId) => v.value) ++
          CodecHelpers.attribute("Name", Some(resourceSet.name.value)) ++
          CodecHelpers.attributeOf("ProcessUsage", resourceSet.processUsage, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Unit", resourceSet.unit, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Usage", resourceSet.usage, _.toString) ++
          CodecHelpers.extensionAttributes(resourceSet.extensions)
      val children =
        resourceSet.comments.map(CommentCodec.encoder.encode) ++
          resourceSet.dependents.map(DependentCodec.encoder.encode) ++
          resourceSet.generalIds.map(GeneralIdCodec.encoder.encode) ++
          resourceSet.resources.map(ResourceCodec.encoder.encode)
      Xml.Element(CodecHelpers.qname("ResourceSet"), attributes, children)
end ResourceSetCodec

object XjdfCodec:
  private val KnownChildren = Set("AuditPool", "Comment", "GeneralID", "ProductList", "ResourceSet")

  val decoder: XmlDecoder[XJDF] =
    XmlDecoder.instance: element =>
      for
        category <- XmlDecoders.attributeOf("Category")(Lexical.nmtoken).decode(element)
        commentUrl <- XmlDecoders.attributeOf("CommentURL")(Lexical.uri).decode(element)
        descriptiveName <- XmlDecoders.attributeOf("DescriptiveName")(Lexical.xjdfString).decode(element)
        icsVersions <- XmlDecoders.attributeOf("ICSVersions")(Lexical.nmtokens).decode(element)
        jobId <- XmlDecoders.requiredAttribute("JobID")(Lexical.nmtoken).decode(element)
        jobPartId <- XmlDecoders.attributeOf("JobPartID")(Lexical.nmtoken).decode(element)
        projectId <- XmlDecoders.attributeOf("ProjectID")(Lexical.nmtoken).decode(element)
        relatedJobId <- XmlDecoders.attributeOf("RelatedJobID")(Lexical.nmtoken).decode(element)
        relatedJobPartId <- XmlDecoders.attributeOf("RelatedJobPartID")(Lexical.nmtoken).decode(element)
        relatedProjectId <- XmlDecoders.attributeOf("RelatedProjectID")(Lexical.nmtoken).decode(element)
        types <- XmlDecoders.requiredAttribute("Types")(Lexical.nmtokens).decode(element)
        version <- XmlDecoders.attributeOf("Version")(Lexical.version).decode(element)
        comments <- XmlDecoders.repeatedChild("Comment")(CommentCodec.decoder).decode(element)
        generalIds <- XmlDecoders.repeatedChild("GeneralID")(GeneralIdCodec.decoder).decode(element)
        resourceSets <- XmlDecoders.repeatedChild("ResourceSet")(ResourceSetCodec.decoder).decode(element)
        auditPool <- XmlDecoders.optionalChild("AuditPool")(summon[XmlElementCodec[AuditPool]]).decode(element)
        productList <- XmlDecoders.optionalChild("ProductList")(summon[XmlElementCodec[ProductList]]).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(KnownChildren).decode(element)
      yield XJDF(
        jobId,
        NonEmptyVector(types.head, types.tail*),
        auditPool,
        comments,
        generalIds,
        productList,
        resourceSets,
        category,
        commentUrl,
        descriptiveName,
        icsVersions.getOrElse(Vector.empty),
        jobPartId,
        projectId,
        relatedJobId,
        relatedJobPartId,
        relatedProjectId,
        version,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[XJDF] =
    XmlEncoder.instance: document =>
      val attributes =
        CodecHelpers.attributeOf("Category", document.category, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("CommentURL", document.commentUrl, (v: UriRef) => v.value.toString) ++
          CodecHelpers.attributeOf("DescriptiveName", document.descriptiveName, (v: XjdfString) => v.value) ++
          CodecHelpers.attribute(
            "ICSVersions",
            Option.when(document.icsVersions.nonEmpty)(CodecHelpers.renderNmtokens(document.icsVersions)),
          ) ++
          CodecHelpers.attribute("JobID", Some(document.jobId.value)) ++
          CodecHelpers.attributeOf("JobPartID", document.jobPartId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("ProjectID", document.projectId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("RelatedJobID", document.relatedJobId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("RelatedJobPartID", document.relatedJobPartId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("RelatedProjectID", document.relatedProjectId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attribute("Types", Some(CodecHelpers.renderNmtokens(document.types.toVector))) ++
          CodecHelpers.attributeOf("Version", document.version, (v: Version) => v.lexical) ++
          CodecHelpers.extensionAttributes(document.extensions)
      val children =
        document.comments.map(CommentCodec.encoder.encode) ++
          document.generalIds.map(GeneralIdCodec.encoder.encode) ++
          document.resourceSets.map(ResourceSetCodec.encoder.encode) ++
          document.auditPool.toVector.map(summon[XmlElementCodec[AuditPool]].encode) ++
          document.productList.toVector.map(summon[XmlElementCodec[ProductList]].encode)
      Xml.Element(CodecHelpers.qname("XJDF"), attributes, children)
end XjdfCodec
