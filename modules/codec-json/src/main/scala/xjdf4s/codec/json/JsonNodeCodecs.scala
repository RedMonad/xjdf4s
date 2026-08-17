package xjdf4s.codec.json

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.*

import xjdf4s.core.*
import xjdf4s.model.*

/**
 * JSON codecs for the document tree. Member names follow the normative JSON convention (attribute names without
 * `@`); repeated children are arrays; simple-content elements (Comment) become strings.
 */
object JsonNodeCodecs:

  // -- simple-content and small nodes -------------------------------------------

  given Encoder[Comment] = Encoder.encodeString.contramap(_.value)
  given Decoder[Comment] = Decoder.decodeString.map(Comment(_))

  given Encoder[GeneralId] = Encoder.instance(generalId =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("IDUsage", Json.fromString(generalId.usage.value))),
        Vector(JsonHelpers.member("IDValue", Json.fromString(generalId.value.value))),
        JsonHelpers.optMember("DataType", generalId.dataType),
      ),
    ),
  )
  given Decoder[GeneralId] = Decoder.instance(cursor =>
    for
      usage <- cursor.get[Nmtoken]("IDUsage")
      value <- cursor.get[XjdfString]("IDValue")
      dataType <- JsonHelpers.opt[GeneralId.DataType](cursor, "DataType")
    yield GeneralId(usage, value, dataType),
  )

  given Encoder[TileCoordinate] = Encoder.instance(tile => Json.arr(Json.fromInt(tile.x), Json.fromInt(tile.y)))
  given Decoder[TileCoordinate] = Decoder.instance(cursor =>
    cursor.as[List[Int]].flatMap {
      case List(x, y) => Right(TileCoordinate(x, y))
      case other      => JsonHelpers.fail(cursor, s"TileCoordinate requires exactly two integers, got ${other.size}")
    },
  )

  given Encoder[Part] = Encoder.instance(part =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("BinderySignatureID", part.binderySignatureId),
        JsonHelpers.optMember("BlockName", part.blockName),
        JsonHelpers.optMember("ContactType", part.contactType),
        JsonHelpers.optMember("DocIndex", part.docIndex),
        JsonHelpers.optMember("DropID", part.dropId),
        JsonHelpers.optMember("Location", part.location),
        JsonHelpers.optMember("LotID", part.lotId),
        JsonHelpers.optMember("Metadata", part.metadata),
        JsonHelpers.optMember("Option", part.option),
        JsonHelpers.optMember("PageNumber", part.pageNumber),
        JsonHelpers.optMember("PartVersion", part.partVersion),
        JsonHelpers.optMember("PreviewType", part.previewType),
        JsonHelpers.optMember("PrintCondition", part.printCondition),
        JsonHelpers.optMember("Product", part.product),
        JsonHelpers.optMember("ProductPart", part.productPart),
        JsonHelpers.optMember("QualityMeasurement", part.qualityMeasurement),
        JsonHelpers.optMember("Run", part.run),
        JsonHelpers.optMember("RunIndex", part.runIndex),
        JsonHelpers.optMember("Separation", part.separation),
        JsonHelpers.optMember("SetIndex", part.setIndex),
        JsonHelpers.optMember("SheetIndex", part.sheetIndex),
        JsonHelpers.optMember("SheetName", part.sheetName),
        JsonHelpers.optMember("Side", part.side),
        JsonHelpers.optMember("StationName", part.stationName),
        JsonHelpers.optMember("TileID", part.tileId),
        JsonHelpers.optMember("TransferCurveName", part.transferCurveName),
        JsonHelpers.optMember("WebName", part.webName),
      ),
    ),
  )
  given Decoder[Part] = Decoder.instance(cursor =>
    for
      binderySignatureId <- JsonHelpers.opt[Nmtoken](cursor, "BinderySignatureID")
      blockName <- JsonHelpers.opt[Nmtoken](cursor, "BlockName")
      contactType <- JsonHelpers.opt[Nmtoken](cursor, "ContactType")
      docIndex <- JsonHelpers.opt[IntegerRange](cursor, "DocIndex")
      dropId <- JsonHelpers.opt[Nmtoken](cursor, "DropID")
      location <- JsonHelpers.opt[Nmtoken](cursor, "Location")
      lotId <- JsonHelpers.opt[Nmtoken](cursor, "LotID")
      metadata <- JsonHelpers.opt[String](cursor, "Metadata")
      option <- JsonHelpers.opt[Nmtoken](cursor, "Option")
      pageNumber <- JsonHelpers.opt[IntegerRange](cursor, "PageNumber")
      partVersion <- JsonHelpers.opt[Nmtoken](cursor, "PartVersion")
      previewType <- JsonHelpers.opt[PreviewType](cursor, "PreviewType")
      printCondition <- JsonHelpers.opt[Nmtoken](cursor, "PrintCondition")
      product <- JsonHelpers.opt[Nmtoken](cursor, "Product")
      productPart <- JsonHelpers.opt[Nmtoken](cursor, "ProductPart")
      qualityMeasurement <- JsonHelpers.opt[Nmtoken](cursor, "QualityMeasurement")
      run <- JsonHelpers.opt[Nmtoken](cursor, "Run")
      runIndex <- JsonHelpers.opt[IntegerRange](cursor, "RunIndex")
      separation <- JsonHelpers.opt[Nmtoken](cursor, "Separation")
      setIndex <- JsonHelpers.opt[IntegerRange](cursor, "SetIndex")
      sheetIndex <- JsonHelpers.opt[IntegerRange](cursor, "SheetIndex")
      sheetName <- JsonHelpers.opt[Nmtoken](cursor, "SheetName")
      side <- JsonHelpers.opt[Side](cursor, "Side")
      stationName <- JsonHelpers.opt[Nmtoken](cursor, "StationName")
      tileId <- JsonHelpers.opt[TileCoordinate](cursor, "TileID")
      transferCurveName <- JsonHelpers.opt[TransferCurveName](cursor, "TransferCurveName")
      webName <- JsonHelpers.opt[Nmtoken](cursor, "WebName")
    yield Part(
      binderySignatureId,
      blockName,
      contactType,
      docIndex,
      dropId,
      location,
      lotId,
      metadata,
      option,
      pageNumber,
      partVersion,
      previewType,
      printCondition,
      product,
      productPart,
      qualityMeasurement,
      run,
      runIndex,
      separation,
      setIndex,
      sheetIndex,
      sheetName,
      side,
      stationName,
      tileId,
      transferCurveName,
      webName,
    ),
  )

  // -- amounts ------------------------------------------------------------------

  given Encoder[PartWaste] = Encoder.instance(partWaste =>
    val originMembers = partWaste.origin match
      case WasteOrigin.Modules(moduleIds) =>
        Vector(JsonHelpers.member("ModuleIDs", Json.arr(moduleIds.toVector.map(id => Json.fromString(id.value))*)))
      case WasteOrigin.Details(wasteDetails) =>
        Vector(JsonHelpers.member("WasteDetails", Json.fromString(wasteDetails.value)))
      case WasteOrigin.ModulesAndDetails(moduleIds, wasteDetails) =>
        Vector(
          JsonHelpers.member("ModuleIDs", Json.arr(moduleIds.toVector.map(id => Json.fromString(id.value))*)),
          JsonHelpers.member("WasteDetails", Json.fromString(wasteDetails.value)),
        )
    Json.obj((JsonHelpers.member("Waste", Json.fromFloat(partWaste.waste).getOrElse(Json.Null)) +: originMembers)*),
  )
  given Decoder[PartWaste] = Decoder.instance(cursor =>
    for
      waste <- cursor.get[Float]("Waste")
      moduleIds <- JsonHelpers.vec[Nmtoken](cursor, "ModuleIDs")
      wasteDetails <- JsonHelpers.opt[Nmtoken](cursor, "WasteDetails")
      origin <- (moduleIds, wasteDetails) match
        case (ids, Some(details)) if ids.nonEmpty =>
          NonEmptyVector.from(ids) match
            case Right(nonEmpty) => Right(WasteOrigin.ModulesAndDetails(nonEmpty, details))
            case Left(_)         => JsonHelpers.fail(cursor, "ModuleIDs must not be empty")
        case (ids, None) if ids.nonEmpty =>
          NonEmptyVector.from(ids) match
            case Right(nonEmpty) => Right(WasteOrigin.Modules(nonEmpty))
            case Left(_)         => JsonHelpers.fail(cursor, "ModuleIDs must not be empty")
        case (_, Some(details)) => Right(WasteOrigin.Details(details))
        case _ => JsonHelpers.fail(cursor, "at least one of ModuleIDs or WasteDetails is required")
    yield PartWaste(waste, origin),
  )

  given Encoder[PartAmount] = Encoder.instance(partAmount =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("Amount", partAmount.amount),
        JsonHelpers.optMember("MaxAmount", partAmount.maxAmount),
        JsonHelpers.optMember("MinAmount", partAmount.minAmount),
        JsonHelpers.optMember("Waste", partAmount.waste),
        JsonHelpers.vecMember("Part", partAmount.parts),
        JsonHelpers.vecMember("PartWaste", partAmount.partWaste),
      ),
    ),
  )
  given Decoder[PartAmount] = Decoder.instance(cursor =>
    for
      amount <- JsonHelpers.opt[Float](cursor, "Amount")
      maxAmount <- JsonHelpers.opt[Float](cursor, "MaxAmount")
      minAmount <- JsonHelpers.opt[Float](cursor, "MinAmount")
      waste <- JsonHelpers.opt[Float](cursor, "Waste")
      parts <- JsonHelpers.vec[Part](cursor, "Part")
      partWaste <- JsonHelpers.vec[PartWaste](cursor, "PartWaste")
    yield PartAmount(amount, maxAmount, minAmount, waste, parts, partWaste),
  )

  given Encoder[AmountPool] =
    Encoder.instance(pool => Json.obj(JsonHelpers.member("PartAmount", Json.arr(pool.amounts.toVector.map(_.asJson)*))))
  given Decoder[AmountPool] = Decoder.instance(cursor =>
    for
      amounts <- cursor.get[List[PartAmount]]("PartAmount")
      nonEmpty <- NonEmptyVector.from(amounts.toVector) match
        case Right(nonEmpty) => Right(nonEmpty)
        case Left(_)         => JsonHelpers.fail(cursor, "PartAmount must not be empty")
    yield AmountPool(nonEmpty),
  )

  // -- resource tree -------------------------------------------------------------

  given Encoder[Resource] = Encoder.instance(resource =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("Brand", resource.brand),
        JsonHelpers.optMember("CommentURL", resource.commentUrl),
        JsonHelpers.optMember("DescriptiveName", resource.descriptiveName),
        JsonHelpers.optMember("Duration", resource.duration),
        JsonHelpers.optMember("Expires", resource.expires),
        JsonHelpers.optMember("ExternalID", resource.externalId),
        JsonHelpers.optMember("GrossWeight", resource.grossWeight),
        JsonHelpers.optMember("ID", resource.id),
        JsonHelpers.optMember("Orientation", resource.orientation),
        JsonHelpers.optMember("ResourceWeight", resource.resourceWeight),
        JsonHelpers.optMember("Start", resource.start),
        JsonHelpers.optMember("StartOffset", resource.startOffset),
        JsonHelpers.optMember("Status", resource.status),
        JsonHelpers.optMember("Transformation", resource.transformation),
        JsonHelpers.optMember("AmountPool", resource.amountPool),
        JsonHelpers.vecMember("Comment", resource.comments),
        JsonHelpers.vecMember("GeneralID", resource.generalIds),
        JsonHelpers.vecMember("Part", resource.parts),
        resource.specificResource.toVector.map(specific =>
          JsonHelpers.member(JsonResources.nameOf(specific), JsonResources.encode(specific)),
        ),
      ),
    ),
  )
  given Decoder[Resource] = Decoder.instance(cursor =>
    for
      amountPool <- JsonHelpers.opt[AmountPool](cursor, "AmountPool")
      comments <- JsonHelpers.vec[Comment](cursor, "Comment")
      generalIds <- JsonHelpers.vec[GeneralId](cursor, "GeneralID")
      parts <- JsonHelpers.vec[Part](cursor, "Part")
      specific <- JsonResources.decodeSpecific(cursor)
      brand <- JsonHelpers.opt[XjdfString](cursor, "Brand")
      commentUrl <- JsonHelpers.opt[UriRef](cursor, "CommentURL")
      descriptiveName <- JsonHelpers.opt[XjdfString](cursor, "DescriptiveName")
      duration <- JsonHelpers.opt[XsdDuration](cursor, "Duration")
      expires <- JsonHelpers.opt[XsdDateTime](cursor, "Expires")
      externalId <- JsonHelpers.opt[Nmtoken](cursor, "ExternalID")
      grossWeight <- JsonHelpers.opt[Float](cursor, "GrossWeight")
      id <- JsonHelpers.opt[XsdId](cursor, "ID")
      orientation <- JsonHelpers.opt[Orientation](cursor, "Orientation")
      resourceWeight <- JsonHelpers.opt[Float](cursor, "ResourceWeight")
      start <- JsonHelpers.opt[XsdDateTime](cursor, "Start")
      startOffset <- JsonHelpers.opt[XsdDuration](cursor, "StartOffset")
      status <- JsonHelpers.opt[ResourceAvailability](cursor, "Status")
      transformation <- JsonHelpers.opt[Matrix](cursor, "Transformation")
    yield Resource(
      amountPool,
      comments,
      generalIds,
      parts,
      specific,
      Vector.empty,
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
    ),
  )

  given Encoder[ResourceSet] = Encoder.instance(resourceSet =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.vecMember("CombinedProcessIndex", resourceSet.combinedProcessIndex),
        JsonHelpers.optMember("CommentURL", resourceSet.commentUrl),
        JsonHelpers.optMember("DescriptiveName", resourceSet.descriptiveName),
        JsonHelpers.optMember("ID", resourceSet.id),
        Vector(JsonHelpers.member("Name", Json.fromString(resourceSet.name.value))),
        JsonHelpers.optMember("ProcessUsage", resourceSet.processUsage),
        JsonHelpers.optMember("Unit", resourceSet.unit),
        JsonHelpers.optMember("Usage", resourceSet.usage),
        JsonHelpers.vecMember("Comment", resourceSet.comments),
        JsonHelpers.vecMember("GeneralID", resourceSet.generalIds),
        JsonHelpers.vecMember("Resource", resourceSet.resources),
      ),
    ),
  )
  given Decoder[ResourceSet] = Decoder.instance(cursor =>
    for
      name <- cursor.get[Nmtoken]("Name")
      combinedProcessIndex <- JsonHelpers.vec[Int](cursor, "CombinedProcessIndex")
      commentUrl <- JsonHelpers.opt[UriRef](cursor, "CommentURL")
      descriptiveName <- JsonHelpers.opt[XjdfString](cursor, "DescriptiveName")
      id <- JsonHelpers.opt[XsdId](cursor, "ID")
      processUsage <- JsonHelpers.opt[Nmtoken](cursor, "ProcessUsage")
      unit <- JsonHelpers.opt[Nmtoken](cursor, "Unit")
      usage <- JsonHelpers.opt[ResourceUsage](cursor, "Usage")
      comments <- JsonHelpers.vec[Comment](cursor, "Comment")
      generalIds <- JsonHelpers.vec[GeneralId](cursor, "GeneralID")
      resources <- JsonHelpers.vec[Resource](cursor, "Resource")
    yield ResourceSet(
      name,
      combinedProcessIndex,
      commentUrl,
      descriptiveName,
      id,
      processUsage,
      unit,
      usage,
      comments,
      Vector.empty,
      generalIds,
      resources,
    ),
  )

  // -- XJDF root -----------------------------------------------------------------

  given Encoder[XJDF] = Encoder.instance(document =>
    if document.auditPool.nonEmpty then
      throw new UnsupportedOperationException("AuditPool is not covered by the JSON codec slice yet")
    if document.productList.nonEmpty then
      throw new UnsupportedOperationException("ProductList is not covered by the JSON codec slice yet")
    JsonHelpers.obj(
      JsonHelpers.memberList(
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
    yield XJDF(
      jobId,
      nonEmptyTypes,
      None,
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
end JsonNodeCodecs
