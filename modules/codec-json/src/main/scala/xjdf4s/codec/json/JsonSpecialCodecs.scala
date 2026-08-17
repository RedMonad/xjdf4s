package xjdf4s.codec.json

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.*

import xjdf4s.codec.xml.Lexical
import xjdf4s.core.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * Hand-written JSON codecs for the special forms whose normative shape is not field-uniform: the payload-enum
 * location of `FileSpec` (URL/UID/FileFormat+FileTemplate), the `Disposition` time pair (MinDuration/Until),
 * the simple-content `NetworkHeader` (text under the `"Text"` member, like the Comment exception) and the
 * TIFF tag value variants (BinaryValue/IntegerValue/NumberValue/StringValue). The flat member sets mirror the
 * XML hand codecs so the XML and JSON encodings stay interchangeable.
 *
 * Members are ordered by dependency: a given is in scope only from its definition point onward, so the
 * self-contained codecs (NetworkHeader, Disposition) precede the FileSpec codecs that use them.
 */
object JsonSpecialCodecs:

  // -- NetworkHeader (simple content: text maps to the "Text" member) --------------

  given Encoder[NetworkHeader] = Encoder.instance(header =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("Name", Json.fromString(header.name.value))),
        Vector(JsonHelpers.member("Text", Json.fromString(header.value.value))),
      ),
    ),
  )
  given Decoder[NetworkHeader] = Decoder.instance(cursor =>
    for
      name <- cursor.get[XjdfString]("Name")
      value <- cursor.get[XjdfString]("Text")
    yield NetworkHeader(name, value),
  )

  // -- Disposition ---------------------------------------------------------------

  given Encoder[Disposition] = Encoder.instance(disposition =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("Action", disposition.action),
        JsonHelpers.optMember("ExtraDuration", disposition.extraDuration),
        JsonHelpers.optMember("MinDuration", disposition.time.collect { case DispositionTime.AfterProcess(duration) => duration }),
        JsonHelpers.optMember("Until", disposition.time.collect { case DispositionTime.At(until) => until }),
        JsonHelpers.optMember("Priority", disposition.priority),
      ),
    ),
  )
  given Decoder[Disposition] = Decoder.instance(cursor =>
    for
      action <- JsonHelpers.opt[DispositionAction](cursor, "Action")
      extraDuration <- JsonHelpers.opt[XsdDuration](cursor, "ExtraDuration")
      minDuration <- JsonHelpers.opt[XsdDuration](cursor, "MinDuration")
      until <- JsonHelpers.opt[XsdDateTime](cursor, "Until")
      priority <- JsonHelpers.opt[Priority0To100](cursor, "Priority")
      time <- (minDuration, until) match
        case (Some(duration), None) => Right(Some(DispositionTime.AfterProcess(duration)))
        case (None, Some(at))       => Right(Some(DispositionTime.At(at)))
        case (None, None)           => Right(None)
        case _                      => JsonHelpers.fail(cursor, "MinDuration/Until are mutually exclusive")
    yield Disposition(action, time, extraDuration, priority),
  )

  // -- FileSpec ------------------------------------------------------------------

  given Encoder[FileSpec] = Encoder.instance(fileSpec =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("CheckSum", fileSpec.checkSum),
        JsonHelpers.optMember("Encoding", fileSpec.encoding),
        JsonHelpers.optMember("FileSize", fileSpec.fileSize),
        JsonHelpers.optMember("MIMEType", fileSpec.mimeType),
        JsonHelpers.optMember("NPage", fileSpec.numberOfPages),
        JsonHelpers.optMember("OverwritePolicy", fileSpec.overwritePolicy),
        JsonHelpers.optMember("Password", fileSpec.password),
        JsonHelpers.optMember("ResourceUsage", fileSpec.resourceUsage),
        JsonHelpers.optMember("SearchDepth", fileSpec.searchDepth),
        JsonHelpers.optMember("UserFileName", fileSpec.userFileName),
        locationMembers(fileSpec.location),
        JsonHelpers.optMember("Disposition", fileSpec.disposition),
        JsonHelpers.vecMember("NetworkHeader", fileSpec.networkHeaders),
      ),
    ),
  )
  given Decoder[FileSpec] = Decoder.instance(cursor =>
    for
      checkSum <- JsonHelpers.opt[Vector[Byte]](cursor, "CheckSum")
      encoding <- JsonHelpers.opt[Nmtoken](cursor, "Encoding")
      fileSize <- JsonHelpers.opt[Long](cursor, "FileSize")
      mimeType <- JsonHelpers.opt[XjdfString](cursor, "MIMEType")
      numberOfPages <- JsonHelpers.opt[Int](cursor, "NPage")
      overwritePolicy <- JsonHelpers.opt[OverwritePolicy](cursor, "OverwritePolicy")
      password <- JsonHelpers.opt[XjdfString](cursor, "Password")
      resourceUsage <- JsonHelpers.opt[Nmtoken](cursor, "ResourceUsage")
      searchDepth <- JsonHelpers.opt[Int](cursor, "SearchDepth")
      userFileName <- JsonHelpers.opt[XjdfString](cursor, "UserFileName")
      url <- JsonHelpers.opt[UriRef](cursor, "URL")
      uid <- JsonHelpers.opt[Nmtoken](cursor, "UID")
      fileFormat <- JsonHelpers.opt[XjdfString](cursor, "FileFormat")
      fileTemplate <- JsonHelpers.vec[Nmtoken](cursor, "FileTemplate")
      disposition <- JsonHelpers.opt[Disposition](cursor, "Disposition")
      networkHeaders <- JsonHelpers.vec[NetworkHeader](cursor, "NetworkHeader")
      location <- (url, uid, fileFormat, fileTemplate) match
        case (Some(value), None, None, template) if template.isEmpty => Right(FileLocation.Url(value))
        case (None, Some(value), None, template) if template.isEmpty => Right(FileLocation.Uid(value))
        case (None, None, Some(format), template) =>
          NonEmptyVector.from(template) match
            case Right(nonEmpty) => Right(FileLocation.Sequence(format, nonEmpty))
            case Left(_)         => JsonHelpers.fail(cursor, "FileTemplate must not be empty")
        case (None, None, None, template) if template.isEmpty => Right(FileLocation.Pipe)
        case _ => JsonHelpers.fail(cursor, "URL/UID/FileFormat+FileTemplate are mutually exclusive")
    yield FileSpec(
      location,
      checkSum,
      encoding,
      fileSize,
      mimeType,
      numberOfPages,
      overwritePolicy,
      password,
      resourceUsage,
      searchDepth,
      userFileName,
      disposition,
      networkHeaders,
    ),
  )

  private def locationMembers(location: FileLocation): Vector[(String, Json)] =
    location match
      case FileLocation.Url(value) => Vector(JsonHelpers.member("URL", Json.fromString(value.value.toString)))
      case FileLocation.Uid(value) => Vector(JsonHelpers.member("UID", Json.fromString(value.value)))
      case FileLocation.Sequence(format, template) =>
        Vector(
          JsonHelpers.member("FileFormat", Json.fromString(format.value)),
          JsonHelpers.member("FileTemplate", Json.arr(template.toVector.map(token => Json.fromString(token.value))*)),
        )
      case FileLocation.Pipe => Vector.empty

  // -- FileSpec role wrappers (DeviceSchemas, DeliveryFiles, ...) -----------------

  /**
   * The XML codec represents these wrappers as FileSpec children selected by their `@ResourceUsage` role; the
   * JSON mapping keeps the same role names as members holding the FileSpec object.
   */
  private def fileSpecRoleMembers[A](roles: Vector[(String, A => Option[FileSpec])], wrapper: A): Vector[(String, Json)] =
    roles.flatMap { case (role, getter) => getter(wrapper).toVector.map(spec => JsonHelpers.member(role, spec.asJson)) }

  private def fileSpecRoleDecoder[A](
      roles: Vector[(String, A => Option[FileSpec])],
      build: Vector[(String, FileSpec)] => A,
  ): Decoder[A] =
    Decoder.instance(cursor =>
      roles.foldLeft[Decoder.Result[Vector[(String, FileSpec)]]](Right(Vector.empty)) { (acc, role) =>
        for
          pairs <- acc
          next <- JsonHelpers.opt[FileSpec](cursor, role._1)
        yield next match
          case Some(spec) => pairs :+ (role._1, spec)
          case None       => pairs
      }.map(build),
    )

  given Encoder[DeliveryFiles] = Encoder.instance(files =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        fileSpecRoleMembers[DeliveryFiles](
          Vector("Contents" -> (_.contents), "MailingList" -> (_.mailingList), "RemoteURL" -> (_.remoteUrl)),
          files,
        ),
      ),
    ),
  )
  given Decoder[DeliveryFiles] = fileSpecRoleDecoder[DeliveryFiles](
    Vector("Contents" -> (_.contents), "MailingList" -> (_.mailingList), "RemoteURL" -> (_.remoteUrl)),
    pairs =>
      DeliveryFiles(
        pairs.find(_._1 == "Contents").map(_._2),
        pairs.find(_._1 == "MailingList").map(_._2),
        pairs.find(_._1 == "RemoteURL").map(_._2),
      ),
  )

  given Encoder[DeviceSchemas] = Encoder.instance(schemas =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        fileSpecRoleMembers[DeviceSchemas](Vector("CurrentSchema" -> (_.current), "Schema" -> (_.global)), schemas),
      ),
    ),
  )
  given Decoder[DeviceSchemas] = fileSpecRoleDecoder[DeviceSchemas](
    Vector("CurrentSchema" -> (_.current), "Schema" -> (_.global)),
    pairs =>
      DeviceSchemas(
        pairs.find(_._1 == "CurrentSchema").map(_._2),
        pairs.find(_._1 == "Schema").map(_._2),
      ),
  )

  given Encoder[DeviceInfoSchemas] = Encoder.instance(schemas =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        fileSpecRoleMembers[DeviceInfoSchemas](Vector("CurrentSchema" -> (_.current), "Schema" -> (_.global)), schemas),
      ),
    ),
  )
  given Decoder[DeviceInfoSchemas] = fileSpecRoleDecoder[DeviceInfoSchemas](
    Vector("CurrentSchema" -> (_.current), "Schema" -> (_.global)),
    pairs =>
      DeviceInfoSchemas(
        pairs.find(_._1 == "CurrentSchema").map(_._2),
        pairs.find(_._1 == "Schema").map(_._2),
      ),
  )

  given Encoder[VerificationFiles] = Encoder.instance(files =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        fileSpecRoleMembers[VerificationFiles](
          Vector("Accepted" -> (_.accepted), "Combined" -> (_.combined), "Rejected" -> (_.rejected), "Unknown" -> (_.unknown)),
          files,
        ),
      ),
    ),
  )
  given Decoder[VerificationFiles] = fileSpecRoleDecoder[VerificationFiles](
    Vector("Accepted" -> (_.accepted), "Combined" -> (_.combined), "Rejected" -> (_.rejected), "Unknown" -> (_.unknown)),
    pairs =>
      VerificationFiles(
        pairs.find(_._1 == "Accepted").map(_._2),
        pairs.find(_._1 == "Combined").map(_._2),
        pairs.find(_._1 == "Rejected").map(_._2),
        pairs.find(_._1 == "Unknown").map(_._2),
      ),
  )

  given Encoder[QualityControlFiles] = Encoder.instance(files =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        fileSpecRoleMembers[QualityControlFiles](Vector("Image" -> (_.image), "Setup" -> (_.setup)), files),
      ),
    ),
  )
  given Decoder[QualityControlFiles] = fileSpecRoleDecoder[QualityControlFiles](
    Vector("Image" -> (_.image), "Setup" -> (_.setup)),
    pairs => QualityControlFiles(pairs.find(_._1 == "Image").map(_._2), pairs.find(_._1 == "Setup").map(_._2)),
  )

  // -- PlacedObject (payload-enum kind: MarkObject vs ContentObject) ------------------

  given Encoder[PlacedObject] = Encoder.instance(placed =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("CTM", placed.ctm.asJson)),
        JsonHelpers.optMember("Anchor", placed.anchor),
        JsonHelpers.optMember("ClipBox", placed.clipBox),
        JsonHelpers.optMember("ClipPath", placed.clipPath),
        JsonHelpers.optMember("HalfTonePhaseOrigin", placed.halfTonePhaseOrigin),
        JsonHelpers.optMember("ID", placed.id),
        JsonHelpers.optMember("Order", placed.order),
        JsonHelpers.optMember("PositionRef", placed.positionRef),
        JsonHelpers.optMember("SourceClipPath", placed.sourceClipPath),
        JsonHelpers.optMember("TrimCTM", placed.trimCtm),
        JsonHelpers.optMember("TrimSize", placed.trimSize),
        kindMembers(placed.kind),
        JsonHelpers.optMember("PageActivation", placed.pageActivation),
        JsonHelpers.optMember("PageCondition", placed.pageCondition),
      ),
    ),
  )
  given Decoder[PlacedObject] = Decoder.instance(cursor =>
    val hasContentObject = cursor.downField("ContentObject").focus
    for
      ctm <- cursor.get[Matrix]("CTM")
      anchor <- JsonHelpers.opt[Anchor](cursor, "Anchor")
      clipBox <- JsonHelpers.opt[Rectangle](cursor, "ClipBox")
      clipPath <- JsonHelpers.opt[PdfPath](cursor, "ClipPath")
      halfTonePhaseOrigin <- JsonHelpers.opt[XYPair](cursor, "HalfTonePhaseOrigin")
      id <- JsonHelpers.opt[XsdId](cursor, "ID")
      order <- JsonHelpers.opt[Int](cursor, "Order")
      positionRef <- JsonHelpers.opt[XsdIdRef](cursor, "PositionRef")
      sourceClipPath <- JsonHelpers.opt[PdfPath](cursor, "SourceClipPath")
      trimCtm <- JsonHelpers.opt[Matrix](cursor, "TrimCTM")
      trimSize <- JsonHelpers.opt[XYPair](cursor, "TrimSize")
      markObject <- JsonHelpers.opt[MarkObject](cursor, "MarkObject")
      pageActivation <- JsonHelpers.opt[PageActivation](cursor, "PageActivation")
      pageCondition <- JsonHelpers.opt[PageCondition](cursor, "PageCondition")
      kind <- (markObject, hasContentObject) match
        case (Some(mark), None) => Right(PlacedObjectKind.Mark(mark))
        case (None, Some(_))    => Right(PlacedObjectKind.Content)
        case (None, None)       => JsonHelpers.fail(cursor, "ContentObject or MarkObject is required")
        case _                  => JsonHelpers.fail(cursor, "ContentObject/MarkObject are mutually exclusive")
    yield PlacedObject(
      ctm,
      kind,
      anchor,
      clipBox,
      clipPath,
      halfTonePhaseOrigin,
      id,
      order,
      positionRef,
      sourceClipPath,
      trimCtm,
      trimSize,
      pageActivation,
      pageCondition,
    ),
  )

  private def kindMembers(kind: PlacedObjectKind): Vector[(String, Json)] =
    kind match
      case PlacedObjectKind.Content    => Vector(JsonHelpers.member("ContentObject", Json.obj()))
      case PlacedObjectKind.Mark(mark) => Vector(JsonHelpers.member("MarkObject", mark.asJson))

  // -- Intent (open ProductIntent union, dispatched through the registry) --------

  /**
   * Mirror of the XML IntentCodec: the intent child is dispatched by its element name through [[JsonRegistry]],
   * so exactly one intent member may be present. The root product-list batch will reuse this codec.
   */
  given Encoder[Intent] = Encoder.instance(intent =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("Name", Json.fromString(intent.name.value))),
        JsonHelpers.optMember("DescriptiveName", intent.descriptiveName),
        JsonHelpers.optMember("ExternalID", intent.externalId),
        intent.productIntent.toVector.map(value => JsonHelpers.member(JsonRegistry.intentName(value), JsonRegistry.encodeProductIntent(value))),
        JsonForeign.encodeExtensions(intent.extensions),
      ),
    ),
  )
  given Decoder[Intent] = Decoder.instance(cursor =>
    for
      name <- cursor.get[Nmtoken]("Name")
      descriptiveName <- JsonHelpers.opt[XjdfString](cursor, "DescriptiveName")
      externalId <- JsonHelpers.opt[Nmtoken](cursor, "ExternalID")
      decodedIntents <- JsonRegistry.intentNames.toVector.sorted.foldLeft[Decoder.Result[Vector[ProductIntent]]](Right(Vector.empty)) {
        (acc, intentName) =>
          for
            accumulated <- acc
            next <- cursor.downField(intentName).focus match
              case Some(json) => JsonRegistry.decodeProductIntent(intentName, json).map(accumulated :+ _)
              case None       => Right(accumulated)
          yield next
      }
      productIntent <- decodedIntents match
        case Vector(single) => Right(Some(single))
        case Vector()       => Right(None)
        case other          => JsonHelpers.fail(cursor, s"Intent requires at most one intent member, found ${other.size}")
      extensions <- JsonForeign.decodeExtensions(cursor)
    yield Intent(name, productIntent, descriptiveName, externalId, extensions),
  )

  // -- self-recursive types (XML rule f: never derived; recursion is explicit, never implicit) -------

  /**
   * BundleItem is self-recursive (`children: Vector[BundleItem]`, Table 6.23). A given is not visible in its
   * own initializer, so the recursive steps cannot go through implicit search - they reference the codec of
   * this helper object explicitly, mirroring the XML BundleItemCodec (the closure defers the self-reference to
   * call time, when the instance is already initialized).
   */
  private object BundleItemJson:
    val decoder: Decoder[BundleItem] = Decoder.instance(cursor =>
      for
        amount <- cursor.get[Int]("Amount")
        bundleType <- JsonHelpers.opt[BundleType](cursor, "BundleType")
        itemRef <- JsonHelpers.opt[XsdIdRef](cursor, "ItemRef")
        totalAmount <- JsonHelpers.opt[Int](cursor, "TotalAmount")
        totalDimensions <- JsonHelpers.opt[Shape3D](cursor, "TotalDimensions")
        totalVolume <- JsonHelpers.opt[Float](cursor, "TotalVolume")
        totalWeight <- JsonHelpers.opt[Float](cursor, "TotalWeight")
        children <- cursor.downField("BundleItem").focus match
          case Some(json) =>
            json.as[List[Json]].flatMap {
              _.foldLeft[Decoder.Result[Vector[BundleItem]]](Right(Vector.empty)) { (acc, item) =>
                for
                  accumulated <- acc
                  child <- decoder.decodeJson(item)
                yield accumulated :+ child
              }
            }
          case None => Right(Vector.empty)
      yield BundleItem(amount, bundleType, itemRef, totalAmount, totalDimensions, totalVolume, totalWeight, children),
    )

    val encoder: Encoder[BundleItem] = Encoder.instance(item =>
      JsonHelpers.obj(
        JsonHelpers.memberList(
          Vector(JsonHelpers.member("Amount", Json.fromInt(item.amount))),
          JsonHelpers.optMember("BundleType", item.bundleType),
          JsonHelpers.optMember("ItemRef", item.itemRef),
          JsonHelpers.optMember("TotalAmount", item.totalAmount),
          JsonHelpers.optMember("TotalDimensions", item.totalDimensions),
          JsonHelpers.optMember("TotalVolume", item.totalVolume),
          JsonHelpers.optMember("TotalWeight", item.totalWeight),
          JsonHelpers.vecMemberOf("BundleItem", item.children)(encoder.apply),
        ),
      ),
    )
  end BundleItemJson

  given encoderBundleItem: Encoder[BundleItem] = BundleItemJson.encoder
  given decoderBundleItem: Decoder[BundleItem] = BundleItemJson.decoder

  /** Mirror of the XML AssemblySectionCodec: the `sections` vector recurses through the helper codec. */
  private object AssemblySectionJson:
    val decoder: Decoder[AssemblySection] = Decoder.instance(cursor =>
      for
        binderySignatureId <- cursor.get[Nmtoken]("BinderySignatureID")
        commonFolds <- JsonHelpers.opt[CommonFolds](cursor, "CommonFolds")
        descriptiveName <- JsonHelpers.opt[XjdfString](cursor, "DescriptiveName")
        externalId <- JsonHelpers.opt[Nmtoken](cursor, "ExternalID")
        sections <- cursor.downField("AssemblySection").focus match
          case Some(json) =>
            json.as[List[Json]].flatMap {
              _.foldLeft[Decoder.Result[Vector[AssemblySection]]](Right(Vector.empty)) { (acc, item) =>
                for
                  accumulated <- acc
                  section <- decoder.decodeJson(item)
                yield accumulated :+ section
              }
            }
          case None => Right(Vector.empty)
      yield AssemblySection(binderySignatureId, commonFolds, descriptiveName, externalId, sections),
    )

    val encoder: Encoder[AssemblySection] = Encoder.instance(section =>
      JsonHelpers.obj(
        JsonHelpers.memberList(
          Vector(JsonHelpers.member("BinderySignatureID", Json.fromString(section.binderySignatureId.value))),
          JsonHelpers.optMember("CommonFolds", section.commonFolds),
          JsonHelpers.optMember("DescriptiveName", section.descriptiveName),
          JsonHelpers.optMember("ExternalID", section.externalId),
          JsonHelpers.vecMemberOf("AssemblySection", section.sections)(encoder.apply),
        ),
      ),
    )
  end AssemblySectionJson

  given encoderAssemblySection: Encoder[AssemblySection] = AssemblySectionJson.encoder
  given decoderAssemblySection: Decoder[AssemblySection] = AssemblySectionJson.decoder

  // -- TIFF tag ------------------------------------------------------------------

  given Encoder[TiffTag] = Encoder.instance(tag =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("TagNumber", Json.fromInt(tag.tagNumber))),
        Vector(JsonHelpers.member("TagType", Json.fromInt(tag.tagType))),
        tagValueMembers(tag.value),
      ),
    ),
  )
  given Decoder[TiffTag] = Decoder.instance(cursor =>
    for
      tagNumber <- cursor.get[Int]("TagNumber")
      tagType <- cursor.get[Int]("TagType")
      binary <- JsonHelpers.opt[Vector[Byte]](cursor, "BinaryValue")
      integers <- JsonHelpers.vec[Int](cursor, "IntegerValue")
      numbers <- JsonHelpers.vec[Float](cursor, "NumberValue")
      text <- JsonHelpers.opt[XjdfString](cursor, "StringValue")
      value <- (binary, integers, numbers, text) match
        case (Some(bytes), ints, nums, None) if ints.isEmpty && nums.isEmpty =>
          Right(Some(TiffTagValue.Binary(bytes)))
        case (None, ints, nums, None) if ints.nonEmpty && nums.isEmpty =>
          Right(Some(TiffTagValue.Integers(ints)))
        case (None, ints, nums, None) if ints.isEmpty && nums.nonEmpty =>
          Right(Some(TiffTagValue.Numbers(nums)))
        case (None, ints, nums, Some(string)) if ints.isEmpty && nums.isEmpty =>
          Right(Some(TiffTagValue.Text(string.value)))
        case (None, ints, nums, None) if ints.isEmpty && nums.isEmpty => Right(None)
        case _ => JsonHelpers.fail(cursor, "BinaryValue/IntegerValue/NumberValue/StringValue are mutually exclusive")
    yield TiffTag(tagNumber, tagType, value),
  )

  private def tagValueMembers(value: Option[TiffTagValue]): Vector[(String, Json)] =
    value match
      case Some(TiffTagValue.Binary(bytes)) =>
        Vector(JsonHelpers.member("BinaryValue", Json.fromString(Lexical.renderHexBinary(bytes))))
      case Some(TiffTagValue.Integers(ints)) =>
        Vector(JsonHelpers.member("IntegerValue", Json.arr(ints.map(Json.fromInt)*)))
      case Some(TiffTagValue.Numbers(numbers)) =>
        Vector(JsonHelpers.member("NumberValue", Json.arr(numbers.map(number => Json.fromFloat(number).getOrElse(Json.Null))*)))
      case Some(TiffTagValue.Text(text)) =>
        Vector(JsonHelpers.member("StringValue", Json.fromString(text)))
      case None => Vector.empty
end JsonSpecialCodecs
