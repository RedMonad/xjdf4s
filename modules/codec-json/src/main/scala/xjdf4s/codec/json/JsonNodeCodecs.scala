package xjdf4s.codec.json

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax.*

import xjdf4s.core.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * JSON codecs for the document tree. Member names follow the normative JSON convention (attribute names without
 * `@`); repeated children are arrays; simple-content elements (Comment) become strings.
 */
object JsonNodeCodecs:

  // -- simple-content and small nodes -------------------------------------------

  given Encoder[Comment] = Encoder.encodeString.contramap(_.value)
  given Decoder[Comment] = Decoder.decodeString.map(Comment(_))

  given Encoder[GeneralId] = Encoder.instance(generalId =>
    Json.obj(
      JsonCodec.member("IDUsage", Json.fromString(generalId.usage.value)),
      JsonCodec.member("IDValue", Json.fromString(generalId.value.value)),
      JsonCodec.optMember("DataType", generalId.dataType),
    ),
  )
  given Decoder[GeneralId] = Decoder.instance(cursor =>
    for
      usage <- cursor.get[Nmtoken]("IDUsage")
      value <- cursor.get[XjdfString]("IDValue")
      dataType <- JsonCodec.opt[GeneralId.DataType](cursor, "DataType")
    yield GeneralId(usage, value, dataType),
  )

  given Encoder[TileCoordinate] = Encoder.instance(tile => Json.arr(Json.fromInt(tile.x), Json.fromInt(tile.y)))
  given Decoder[TileCoordinate] = Decoder.instance(cursor =>
    cursor.as[List[Int]].flatMap {
      case List(x, y) => Right(TileCoordinate(x, y))
      case other      => JsonCodec.fail(cursor, s"TileCoordinate requires exactly two integers, got ${other.size}")
    },
  )

  given Encoder[Part] = Encoder.instance(part =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("BinderySignatureID", part.binderySignatureId),
        JsonCodec.optMember("BlockName", part.blockName),
        JsonCodec.optMember("ContactType", part.contactType),
        JsonCodec.optMember("DocIndex", part.docIndex),
        JsonCodec.optMember("DropID", part.dropId),
        JsonCodec.optMember("Location", part.location),
        JsonCodec.optMember("LotID", part.lotId),
        JsonCodec.optMember("Metadata", part.metadata),
        JsonCodec.optMember("Option", part.option),
        JsonCodec.optMember("PageNumber", part.pageNumber),
        JsonCodec.optMember("PartVersion", part.partVersion),
        JsonCodec.optMember("PreviewType", part.previewType),
        JsonCodec.optMember("PrintCondition", part.printCondition),
        JsonCodec.optMember("Product", part.product),
        JsonCodec.optMember("ProductPart", part.productPart),
        JsonCodec.optMember("QualityMeasurement", part.qualityMeasurement),
        JsonCodec.optMember("Run", part.run),
        JsonCodec.optMember("RunIndex", part.runIndex),
        JsonCodec.optMember("Separation", part.separation),
        JsonCodec.optMember("SetIndex", part.setIndex),
        JsonCodec.optMember("SheetIndex", part.sheetIndex),
        JsonCodec.optMember("SheetName", part.sheetName),
        JsonCodec.optMember("Side", part.side),
        JsonCodec.optMember("StationName", part.stationName),
        JsonCodec.optMember("TileID", part.tileId),
        JsonCodec.optMember("TransferCurveName", part.transferCurveName),
        JsonCodec.optMember("WebName", part.webName),
      ),
    ),
  )
  given Decoder[Part] = Decoder.instance(cursor =>
    for
      binderySignatureId <- JsonCodec.opt[Nmtoken](cursor, "BinderySignatureID")
      blockName <- JsonCodec.opt[Nmtoken](cursor, "BlockName")
      contactType <- JsonCodec.opt[Nmtoken](cursor, "ContactType")
      docIndex <- JsonCodec.opt[IntegerRange](cursor, "DocIndex")
      dropId <- JsonCodec.opt[Nmtoken](cursor, "DropID")
      location <- JsonCodec.opt[Nmtoken](cursor, "Location")
      lotId <- JsonCodec.opt[Nmtoken](cursor, "LotID")
      metadata <- JsonCodec.opt[String](cursor, "Metadata")
      option <- JsonCodec.opt[Nmtoken](cursor, "Option")
      pageNumber <- JsonCodec.opt[IntegerRange](cursor, "PageNumber")
      partVersion <- JsonCodec.opt[Nmtoken](cursor, "PartVersion")
      previewType <- JsonCodec.opt[PreviewType](cursor, "PreviewType")
      printCondition <- JsonCodec.opt[Nmtoken](cursor, "PrintCondition")
      product <- JsonCodec.opt[Nmtoken](cursor, "Product")
      productPart <- JsonCodec.opt[Nmtoken](cursor, "ProductPart")
      qualityMeasurement <- JsonCodec.opt[Nmtoken](cursor, "QualityMeasurement")
      run <- JsonCodec.opt[Nmtoken](cursor, "Run")
      runIndex <- JsonCodec.opt[IntegerRange](cursor, "RunIndex")
      separation <- JsonCodec.opt[Nmtoken](cursor, "Separation")
      setIndex <- JsonCodec.opt[IntegerRange](cursor, "SetIndex")
      sheetIndex <- JsonCodec.opt[IntegerRange](cursor, "SheetIndex")
      sheetName <- JsonCodec.opt[Nmtoken](cursor, "SheetName")
      side <- JsonCodec.opt[Side](cursor, "Side")
      stationName <- JsonCodec.opt[Nmtoken](cursor, "StationName")
      tileId <- JsonCodec.opt[TileCoordinate](cursor, "TileID")
      transferCurveName <- JsonCodec.opt[TransferCurveName](cursor, "TransferCurveName")
      webName <- JsonCodec.opt[Nmtoken](cursor, "WebName")
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
        Vector(JsonCodec.member("ModuleIDs", Json.arr(moduleIds.toVector.map(id => Json.fromString(id.value))*)))
      case WasteOrigin.Details(wasteDetails) =>
        Vector(JsonCodec.member("WasteDetails", Json.fromString(wasteDetails.value)))
      case WasteOrigin.ModulesAndDetails(moduleIds, wasteDetails) =>
        Vector(
          JsonCodec.member("ModuleIDs", Json.arr(moduleIds.toVector.map(id => Json.fromString(id.value))*)),
          JsonCodec.member("WasteDetails", Json.fromString(wasteDetails.value)),
        )
    Json.obj((JsonCodec.member("Waste", Json.fromFloat(partWaste.waste).getOrElse(Json.Null)) +: originMembers)*),
  )
  given Decoder[PartWaste] = Decoder.instance(cursor =>
    for
      waste <- cursor.get[Float]("Waste")
      moduleIds <- JsonCodec.vec[Nmtoken](cursor, "ModuleIDs")
      wasteDetails <- JsonCodec.opt[Nmtoken](cursor, "WasteDetails")
      origin <- (moduleIds, wasteDetails) match
        case (ids, Some(details)) if ids.nonEmpty =>
          NonEmptyVector.from(ids) match
            case Right(nonEmpty) => Right(WasteOrigin.ModulesAndDetails(nonEmpty, details))
            case Left(_)         => JsonCodec.fail(cursor, "ModuleIDs must not be empty")
        case (ids, None) if ids.nonEmpty =>
          NonEmptyVector.from(ids) match
            case Right(nonEmpty) => Right(WasteOrigin.Modules(nonEmpty))
            case Left(_)         => JsonCodec.fail(cursor, "ModuleIDs must not be empty")
        case (_, Some(details)) => Right(WasteOrigin.Details(details))
        case _ => JsonCodec.fail(cursor, "at least one of ModuleIDs or WasteDetails is required")
    yield PartWaste(waste, origin),
  )

  given Encoder[PartAmount] = Encoder.instance(partAmount =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("Amount", partAmount.amount),
        JsonCodec.optMember("MaxAmount", partAmount.maxAmount),
        JsonCodec.optMember("MinAmount", partAmount.minAmount),
        JsonCodec.optMember("Waste", partAmount.waste),
        JsonCodec.vecMember("Part", partAmount.parts),
        JsonCodec.vecMember("PartWaste", partAmount.partWaste),
      ),
    ),
  )
  given Decoder[PartAmount] = Decoder.instance(cursor =>
    for
      amount <- JsonCodec.opt[Float](cursor, "Amount")
      maxAmount <- JsonCodec.opt[Float](cursor, "MaxAmount")
      minAmount <- JsonCodec.opt[Float](cursor, "MinAmount")
      waste <- JsonCodec.opt[Float](cursor, "Waste")
      parts <- JsonCodec.vec[Part](cursor, "Part")
      partWaste <- JsonCodec.vec[PartWaste](cursor, "PartWaste")
    yield PartAmount(amount, maxAmount, minAmount, waste, parts, partWaste),
  )

  given Encoder[AmountPool] =
    Encoder.instance(pool => Json.obj(JsonCodec.member("PartAmount", Json.arr(pool.amounts.toVector.map(_.asJson)*))))
  given Decoder[AmountPool] = Decoder.instance(cursor =>
    for
      amounts <- cursor.get[List[PartAmount]]("PartAmount")
      nonEmpty <- NonEmptyVector.from(amounts.toVector) match
        case Right(nonEmpty) => Right(nonEmpty)
        case Left(_)         => JsonCodec.fail(cursor, "PartAmount must not be empty")
    yield AmountPool(nonEmpty),
  )

  // -- resource tree -------------------------------------------------------------

  given Encoder[Resource] = Encoder.instance(resource =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("Brand", resource.brand),
        JsonCodec.optMember("CommentURL", resource.commentUrl),
        JsonCodec.optMember("DescriptiveName", resource.descriptiveName),
        JsonCodec.optMember("Duration", resource.duration),
        JsonCodec.optMember("Expires", resource.expires),
        JsonCodec.optMember("ExternalID", resource.externalId),
        JsonCodec.optMember("GrossWeight", resource.grossWeight),
        JsonCodec.optMember("ID", resource.id),
        JsonCodec.optMember("Orientation", resource.orientation),
        JsonCodec.optMember("ResourceWeight", resource.resourceWeight),
        JsonCodec.optMember("Start", resource.start),
        JsonCodec.optMember("StartOffset", resource.startOffset),
        JsonCodec.optMember("Status", resource.status),
        JsonCodec.optMember("Transformation", resource.transformation),
        JsonCodec.optMember("AmountPool", resource.amountPool),
        JsonCodec.vecMember("Comment", resource.comments),
        JsonCodec.vecMember("GeneralID", resource.generalIds),
        JsonCodec.vecMember("Part", resource.parts),
        resource.specificResource.toVector.map(specific =>
          JsonCodec.member(JsonResources.nameOf(specific), JsonResources.encode(specific)),
        ),
      ),
    ),
  )
  given Decoder[Resource] = Decoder.instance(cursor =>
    for
      amountPool <- JsonCodec.opt[AmountPool](cursor, "AmountPool")
      comments <- JsonCodec.vec[Comment](cursor, "Comment")
      generalIds <- JsonCodec.vec[GeneralId](cursor, "GeneralID")
      parts <- JsonCodec.vec[Part](cursor, "Part")
      specific <- JsonResources.decodeSpecific(cursor)
      brand <- JsonCodec.opt[XjdfString](cursor, "Brand")
      commentUrl <- JsonCodec.opt[UriRef](cursor, "CommentURL")
      descriptiveName <- JsonCodec.opt[XjdfString](cursor, "DescriptiveName")
      duration <- JsonCodec.opt[XsdDuration](cursor, "Duration")
      expires <- JsonCodec.opt[XsdDateTime](cursor, "Expires")
      externalId <- JsonCodec.opt[Nmtoken](cursor, "ExternalID")
      grossWeight <- JsonCodec.opt[Float](cursor, "GrossWeight")
      id <- JsonCodec.opt[XsdId](cursor, "ID")
      orientation <- JsonCodec.opt[Orientation](cursor, "Orientation")
      resourceWeight <- JsonCodec.opt[Float](cursor, "ResourceWeight")
      start <- JsonCodec.opt[XsdDateTime](cursor, "Start")
      startOffset <- JsonCodec.opt[XsdDuration](cursor, "StartOffset")
      status <- JsonCodec.opt[ResourceAvailability](cursor, "Status")
      transformation <- JsonCodec.opt[Matrix](cursor, "Transformation")
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
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.vecMember("CombinedProcessIndex", resourceSet.combinedProcessIndex),
        JsonCodec.optMember("CommentURL", resourceSet.commentUrl),
        JsonCodec.optMember("DescriptiveName", resourceSet.descriptiveName),
        JsonCodec.optMember("ID", resourceSet.id),
        Vector(JsonCodec.member("Name", Json.fromString(resourceSet.name.value))),
        JsonCodec.optMember("ProcessUsage", resourceSet.processUsage),
        JsonCodec.optMember("Unit", resourceSet.unit),
        JsonCodec.optMember("Usage", resourceSet.usage),
        JsonCodec.vecMember("Comment", resourceSet.comments),
        JsonCodec.vecMember("GeneralID", resourceSet.generalIds),
        JsonCodec.vecMember("Resource", resourceSet.resources),
      ),
    ),
  )
  given Decoder[ResourceSet] = Decoder.instance(cursor =>
    for
      name <- cursor.get[Nmtoken]("Name")
      combinedProcessIndex <- JsonCodec.vec[Int](cursor, "CombinedProcessIndex")
      commentUrl <- JsonCodec.opt[UriRef](cursor, "CommentURL")
      descriptiveName <- JsonCodec.opt[XjdfString](cursor, "DescriptiveName")
      id <- JsonCodec.opt[XsdId](cursor, "ID")
      processUsage <- JsonCodec.opt[Nmtoken](cursor, "ProcessUsage")
      unit <- JsonCodec.opt[Nmtoken](cursor, "Unit")
      usage <- JsonCodec.opt[ResourceUsage](cursor, "Usage")
      comments <- JsonCodec.vec[Comment](cursor, "Comment")
      generalIds <- JsonCodec.vec[GeneralId](cursor, "GeneralID")
      resources <- JsonCodec.vec[Resource](cursor, "Resource")
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
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("Category", document.category),
        JsonCodec.optMember("CommentURL", document.commentUrl),
        JsonCodec.optMember("DescriptiveName", document.descriptiveName),
        JsonCodec.vecMember("ICSVersions", document.icsVersions),
        Vector(JsonCodec.member("JobID", Json.fromString(document.jobId.value))),
        JsonCodec.optMember("JobPartID", document.jobPartId),
        JsonCodec.optMember("ProjectID", document.projectId),
        JsonCodec.optMember("RelatedJobID", document.relatedJobId),
        JsonCodec.optMember("RelatedJobPartID", document.relatedJobPartId),
        JsonCodec.optMember("RelatedProjectID", document.relatedProjectId),
        Vector(JsonCodec.member("Types", Json.arr(document.types.toVector.map(token => Json.fromString(token.value))*))),
        JsonCodec.optMember("Version", document.version),
        JsonCodec.vecMember("Comment", document.comments),
        JsonCodec.vecMember("GeneralID", document.generalIds),
        JsonCodec.vecMember("ResourceSet", document.resourceSets),
        Vector(JsonCodec.rootName("XJDF")),
      ),
    ),
  )
  given Decoder[XJDF] = Decoder.instance(cursor =>
    for
      jobId <- cursor.get[Nmtoken]("JobID")
      types <- cursor.get[List[Nmtoken]]("Types")
      nonEmptyTypes <- NonEmptyVector.from(types.toVector) match
        case Right(nonEmpty) => Right(nonEmpty)
        case Left(_)         => JsonCodec.fail(cursor, "Types must not be empty")
      category <- JsonCodec.opt[Nmtoken](cursor, "Category")
      commentUrl <- JsonCodec.opt[UriRef](cursor, "CommentURL")
      descriptiveName <- JsonCodec.opt[XjdfString](cursor, "DescriptiveName")
      icsVersions <- JsonCodec.vec[Nmtoken](cursor, "ICSVersions")
      jobPartId <- JsonCodec.opt[Nmtoken](cursor, "JobPartID")
      projectId <- JsonCodec.opt[Nmtoken](cursor, "ProjectID")
      relatedJobId <- JsonCodec.opt[Nmtoken](cursor, "RelatedJobID")
      relatedJobPartId <- JsonCodec.opt[Nmtoken](cursor, "RelatedJobPartID")
      relatedProjectId <- JsonCodec.opt[Nmtoken](cursor, "RelatedProjectID")
      version <- JsonCodec.opt[Version](cursor, "Version")
      comments <- JsonCodec.vec[Comment](cursor, "Comment")
      generalIds <- JsonCodec.vec[GeneralId](cursor, "GeneralID")
      resourceSets <- JsonCodec.vec[ResourceSet](cursor, "ResourceSet")
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
