package xjdf4s.codec.json

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax.*
import xjdf4s.codec.xml.Lexical
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/** Hand-written JSON codecs for the special forms whose normative shape is not field-uniform: the payload-enum
 *  location of `FileSpec` (URL/UID/FileFormat+FileTemplate), the `Disposition` time pair (MinDuration/Until),
 *  the simple-content `NetworkHeader` (text under the `"Text"` member, like the Comment exception) and the
 *  TIFF tag value variants (BinaryValue/IntegerValue/NumberValue/StringValue). The flat member sets mirror the
 *  XML hand codecs so the XML and JSON encodings stay interchangeable.
 *
 *  Members are ordered by dependency: a given is in scope only from its definition point onward, so the
 *  self-contained codecs (NetworkHeader, Disposition) precede the FileSpec codecs that use them.
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
      name  <- cursor.get[XjdfString]("Name")
      value <- cursor.get[XjdfString]("Text")
    yield NetworkHeader(name, value),
  )

  // -- Disposition ---------------------------------------------------------------

  given Encoder[Disposition] = Encoder.instance(disposition =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("Action", disposition.action),
        JsonHelpers.optMember("ExtraDuration", disposition.extraDuration),
        JsonHelpers.optMember(
          "MinDuration",
          disposition.time.collect { case DispositionTime.AfterProcess(duration) => duration }
        ),
        JsonHelpers.optMember("Until", disposition.time.collect { case DispositionTime.At(until) => until }),
        JsonHelpers.optMember("Priority", disposition.priority),
      ),
    ),
  )
  given Decoder[Disposition] = Decoder.instance(cursor =>
    for
      action        <- JsonHelpers.opt[DispositionAction](cursor, "Action")
      extraDuration <- JsonHelpers.opt[XsdDuration](cursor, "ExtraDuration")
      minDuration   <- JsonHelpers.opt[XsdDuration](cursor, "MinDuration")
      until         <- JsonHelpers.opt[XsdDateTime](cursor, "Until")
      priority      <- JsonHelpers.opt[Priority0To100](cursor, "Priority")
      time          <- (minDuration, until) match
        case (Some(duration), None) => Right(Some(DispositionTime.AfterProcess(duration)))
        case (None, Some(at)) => Right(Some(DispositionTime.At(at)))
        case (None, None) => Right(None)
        case _ => JsonHelpers.fail(cursor, "MinDuration/Until are mutually exclusive")
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
      checkSum        <- JsonHelpers.opt[Vector[Byte]](cursor, "CheckSum")
      encoding        <- JsonHelpers.opt[Nmtoken](cursor, "Encoding")
      fileSize        <- JsonHelpers.opt[Long](cursor, "FileSize")
      mimeType        <- JsonHelpers.opt[XjdfString](cursor, "MIMEType")
      numberOfPages   <- JsonHelpers.opt[Int](cursor, "NPage")
      overwritePolicy <- JsonHelpers.opt[OverwritePolicy](cursor, "OverwritePolicy")
      password        <- JsonHelpers.opt[XjdfString](cursor, "Password")
      resourceUsage   <- JsonHelpers.opt[Nmtoken](cursor, "ResourceUsage")
      searchDepth     <- JsonHelpers.opt[Int](cursor, "SearchDepth")
      userFileName    <- JsonHelpers.opt[XjdfString](cursor, "UserFileName")
      url             <- JsonHelpers.opt[UriRef](cursor, "URL")
      uid             <- JsonHelpers.opt[Nmtoken](cursor, "UID")
      fileFormat      <- JsonHelpers.opt[XjdfString](cursor, "FileFormat")
      fileTemplate    <- JsonHelpers.vec[Nmtoken](cursor, "FileTemplate")
      disposition     <- JsonHelpers.opt[Disposition](cursor, "Disposition")
      networkHeaders  <- JsonHelpers.vec[NetworkHeader](cursor, "NetworkHeader")
      location        <- (url, uid, fileFormat, fileTemplate) match
        case (Some(value), None, None, template) if template.isEmpty => Right(FileLocation.Url(value))
        case (None, Some(value), None, template) if template.isEmpty => Right(FileLocation.Uid(value))
        case (None, None, Some(format), template) =>
          NonEmptyVector.from(template) match
            case Right(nonEmpty) => Right(FileLocation.Sequence(format, nonEmpty))
            case Left(_) => JsonHelpers.fail(cursor, "FileTemplate must not be empty")
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

  /** The XML codec represents these wrappers as FileSpec children selected by their `@ResourceUsage` role; the
   *  JSON mapping keeps the same role names as members holding the FileSpec object.
   */
  private def fileSpecRoleMembers[A](
      roles: Vector[(String, A => Option[FileSpec])],
      wrapper: A
  ): Vector[(String, Json)] =
    roles.flatMap { case (role, getter) => getter(wrapper).toVector.map(spec => JsonHelpers.member(role, spec.asJson)) }

  private def fileSpecRoleDecoder[A](
      roles: Vector[(String, A => Option[FileSpec])],
      build: Vector[(String, FileSpec)] => A,
  ): Decoder[A] =
    Decoder.instance(cursor =>
      roles.foldLeft[Decoder.Result[Vector[(String, FileSpec)]]](Right(Vector.empty)) { (acc, role) =>
        for
          pairs <- acc
          next  <- JsonHelpers.opt[FileSpec](cursor, role._1)
        yield next match
          case Some(spec) => pairs :+ (role._1, spec)
          case None => pairs
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
          Vector(
            "Accepted" -> (_.accepted),
            "Combined" -> (_.combined),
            "Rejected" -> (_.rejected),
            "Unknown" -> (_.unknown)
          ),
          files,
        ),
      ),
    ),
  )
  given Decoder[VerificationFiles] = fileSpecRoleDecoder[VerificationFiles](
    Vector(
      "Accepted" -> (_.accepted),
      "Combined" -> (_.combined),
      "Rejected" -> (_.rejected),
      "Unknown" -> (_.unknown)
    ),
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
      ctm                 <- cursor.get[Matrix]("CTM")
      anchor              <- JsonHelpers.opt[Anchor](cursor, "Anchor")
      clipBox             <- JsonHelpers.opt[Rectangle](cursor, "ClipBox")
      clipPath            <- JsonHelpers.opt[PdfPath](cursor, "ClipPath")
      halfTonePhaseOrigin <- JsonHelpers.opt[XYPair](cursor, "HalfTonePhaseOrigin")
      id                  <- JsonHelpers.opt[XsdId](cursor, "ID")
      order               <- JsonHelpers.opt[Int](cursor, "Order")
      positionRef         <- JsonHelpers.opt[XsdIdRef](cursor, "PositionRef")
      sourceClipPath      <- JsonHelpers.opt[PdfPath](cursor, "SourceClipPath")
      trimCtm             <- JsonHelpers.opt[Matrix](cursor, "TrimCTM")
      trimSize            <- JsonHelpers.opt[XYPair](cursor, "TrimSize")
      markObject          <- JsonHelpers.opt[MarkObject](cursor, "MarkObject")
      pageActivation      <- JsonHelpers.opt[PageActivation](cursor, "PageActivation")
      pageCondition       <- JsonHelpers.opt[PageCondition](cursor, "PageCondition")
      kind                <- (markObject, hasContentObject) match
        case (Some(mark), None) => Right(PlacedObjectKind.Mark(mark))
        case (None, Some(_)) => Right(PlacedObjectKind.Content)
        case (None, None) => JsonHelpers.fail(cursor, "ContentObject or MarkObject is required")
        case _ => JsonHelpers.fail(cursor, "ContentObject/MarkObject are mutually exclusive")
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
      case PlacedObjectKind.Content => Vector(JsonHelpers.member("ContentObject", Json.obj()))
      case PlacedObjectKind.Mark(mark) => Vector(JsonHelpers.member("MarkObject", mark.asJson))

  // -- Intent (open ProductIntent union, dispatched through the registry) --------

  /** Mirror of the XML IntentCodec: the intent child is dispatched by its member name through [[JsonRegistry]],
   *  so exactly one intent member may be present; a foreign-namespace member becomes a NamedProductIntent via
   *  [[JsonForeign]]. The root product-list batch reuses this codec.
   */
  given Encoder[Intent] = Encoder.instance(intent =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("Name", Json.fromString(intent.name.value))),
        JsonHelpers.optMember("DescriptiveName", intent.descriptiveName),
        JsonHelpers.optMember("ExternalID", intent.externalId),
        intent.productIntent.toVector.flatMap {
          case named: NamedProductIntent =>
            JsonForeign.encodeForeignElementMember(
              ExtensionElement(
                named.foreignName,
                attributes = named.extensions.attributes,
                content = named.extensions.elements.map(ExtensionContent.Element(_)),
              ),
            )
          case standard =>
            Vector(JsonHelpers.member(JsonRegistry.intentName(standard), JsonRegistry.encodeProductIntent(standard)))
        },
        JsonForeign.encodeExtensions(intent.extensions),
      ),
    ),
  )
  given Decoder[Intent] = Decoder.instance(cursor =>
    for
      name            <- cursor.get[Nmtoken]("Name")
      descriptiveName <- JsonHelpers.opt[XjdfString](cursor, "DescriptiveName")
      externalId      <- JsonHelpers.opt[Nmtoken](cursor, "ExternalID")
      standardIntents <-
        JsonRegistry.intentNames.toVector.sorted.foldLeft[Decoder.Result[Vector[ProductIntent]]](Right(Vector.empty)) {
          (acc, intentName) =>
            for
              accumulated <- acc
              next        <- cursor.downField(intentName).focus match
                case Some(json) => JsonRegistry.decodeProductIntent(intentName, json).map(accumulated :+ _)
                case None => Right(accumulated)
            yield next
        }
      foreignIntents <- JsonForeign.decodeForeignElement(cursor)
      productIntent  <- (standardIntents, foreignIntents) match
        case (Vector(single), None) => Right(Some(single))
        case (Vector(), Some(element)) =>
          Right(Some(NamedProductIntent(
            element.name,
            Extensions(element.attributes, element.content.collect { case ExtensionContent.Element(node) => node })
          )))
        case (Vector(), None) => Right(None)
        case _ => JsonHelpers.fail(cursor, "Intent requires at most one intent member")
      extensions <- JsonForeign.decodeExtensions(cursor)
    yield Intent(name, productIntent, descriptiveName, externalId, extensions),
  )

  // -- self-recursive types (XML rule f: never derived; recursion is explicit, never implicit) -------

  /** BundleItem is self-recursive (`children: Vector[BundleItem]`, Table 6.23). A given is not visible in its
   *  own initializer, so the recursive steps cannot go through implicit search - they reference the codec of
   *  this helper object explicitly, mirroring the XML BundleItemCodec (the closure defers the self-reference to
   *  call time, when the instance is already initialized).
   */
  private object BundleItemJson:
    val decoder: Decoder[BundleItem] = Decoder.instance(cursor =>
      for
        amount          <- cursor.get[Int]("Amount")
        bundleType      <- JsonHelpers.opt[BundleType](cursor, "BundleType")
        itemRef         <- JsonHelpers.opt[XsdIdRef](cursor, "ItemRef")
        totalAmount     <- JsonHelpers.opt[Int](cursor, "TotalAmount")
        totalDimensions <- JsonHelpers.opt[Shape3D](cursor, "TotalDimensions")
        totalVolume     <- JsonHelpers.opt[Float](cursor, "TotalVolume")
        totalWeight     <- JsonHelpers.opt[Float](cursor, "TotalWeight")
        children        <- cursor.downField("BundleItem").focus match
          case Some(json) =>
            json.as[List[Json]].flatMap {
              _.foldLeft[Decoder.Result[Vector[BundleItem]]](Right(Vector.empty)) { (acc, item) =>
                for
                  accumulated <- acc
                  child       <- decoder.decodeJson(item)
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
        commonFolds        <- JsonHelpers.opt[CommonFolds](cursor, "CommonFolds")
        descriptiveName    <- JsonHelpers.opt[XjdfString](cursor, "DescriptiveName")
        externalId         <- JsonHelpers.opt[Nmtoken](cursor, "ExternalID")
        sections           <- cursor.downField("AssemblySection").focus match
          case Some(json) =>
            json.as[List[Json]].flatMap {
              _.foldLeft[Decoder.Result[Vector[AssemblySection]]](Right(Vector.empty)) { (acc, item) =>
                for
                  accumulated <- acc
                  section     <- decoder.decodeJson(item)
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

  // -- StickOn: @Face xor @Folio -----------------------------------------------------

  given Encoder[StickOn] = Encoder.instance(stickOn =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("ChildRef", Json.fromString(stickOn.childRef.value))),
        JsonHelpers.optMember("Face", stickOn.location.collect { case ProductLocation.OnFace(face) => face }),
        JsonHelpers.optMember("Folio", stickOn.location.collect { case ProductLocation.OnFolio(folio) => folio }),
        JsonHelpers.optMember("Orientation", stickOn.orientation),
        JsonHelpers.optMember("Position", stickOn.position),
        JsonHelpers.optMember("Glue", stickOn.glue),
      ),
    ),
  )
  given Decoder[StickOn] = Decoder.instance(cursor =>
    for
      childRef    <- cursor.get[XsdIdRef]("ChildRef")
      face        <- JsonHelpers.opt[Face](cursor, "Face")
      folio       <- JsonHelpers.opt[Int](cursor, "Folio")
      orientation <- JsonHelpers.opt[Orientation](cursor, "Orientation")
      position    <- JsonHelpers.opt[XYPair](cursor, "Position")
      glue        <- JsonHelpers.opt[Glue](cursor, "Glue")
      location    <- (face, folio) match
        case (Some(f), None) => Right(Some(ProductLocation.OnFace(f)))
        case (None, Some(p)) => Right(Some(ProductLocation.OnFolio(p)))
        case (None, None) => Right(None)
        case _ => JsonHelpers.fail(cursor, "Face/Folio are mutually exclusive")
    yield StickOn(childRef, location, orientation, position, glue),
  )

  // -- CollatingItem: @Orientation xor @Transformation ----------------------------------

  given Encoder[CollatingItem] = Encoder.instance(item =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("Amount", item.amount),
        JsonHelpers.optMember("ComponentRef", item.componentRef),
        JsonHelpers.optMember(
          "Orientation",
          item.placement.collect { case CollatingPlacement.ByOrientation(orientation) => orientation }
        ),
        JsonHelpers.optMember(
          "Transformation",
          item.placement.collect { case CollatingPlacement.ByTransformation(transformation) => transformation }
        ),
        JsonHelpers.optMember("TransformationContext", item.transformationContext),
      ),
    ),
  )
  given Decoder[CollatingItem] = Decoder.instance(cursor =>
    for
      amount                <- JsonHelpers.opt[Int](cursor, "Amount")
      componentRef          <- JsonHelpers.opt[XsdIdRef](cursor, "ComponentRef")
      orientation           <- JsonHelpers.opt[Orientation](cursor, "Orientation")
      transformation        <- JsonHelpers.opt[Matrix](cursor, "Transformation")
      transformationContext <- JsonHelpers.opt[TransformationContext](cursor, "TransformationContext")
      placement             <- (orientation, transformation) match
        case (Some(o), None) => Right(Some(CollatingPlacement.ByOrientation(o)))
        case (None, Some(t)) => Right(Some(CollatingPlacement.ByTransformation(t)))
        case (None, None) => Right(None)
        case _ => JsonHelpers.fail(cursor, "Orientation/Transformation are mutually exclusive")
    yield CollatingItem(amount, componentRef, placement, transformationContext),
  )

  // -- LooseBindingParams: BindingType plus per-case details --------------------------

  private val looseBindingTypes: Vector[(ProductionLooseBinding, String)] = Vector(
    ProductionLooseBinding.Channel() -> "ChannelBinding",
    ProductionLooseBinding.Coil() -> "CoilBinding",
    ProductionLooseBinding.Comb() -> "CombBinding",
    ProductionLooseBinding.Ring() -> "RingBinding",
    ProductionLooseBinding.Strip() -> "StripBinding",
  )

  private def looseBindingTypeName(binding: ProductionLooseBinding): String =
    looseBindingTypes.find(_._1.productPrefix == binding.productPrefix).map(_._2).getOrElse(binding.productPrefix)

  private def looseBindingDetailsMembers(binding: ProductionLooseBinding): Vector[(String, Json)] =
    binding match
      case ProductionLooseBinding.Channel(details) =>
        details.toVector.map(value => JsonHelpers.member("ChannelBindingDetails", value.asJson))
      case ProductionLooseBinding.Coil(details) =>
        details.toVector.map(value => JsonHelpers.member("CoilBindingDetails", value.asJson))
      case ProductionLooseBinding.Comb(details) =>
        details.toVector.map(value => JsonHelpers.member("CombBindingDetails", value.asJson))
      case ProductionLooseBinding.Ring(details) =>
        details.toVector.map(value => JsonHelpers.member("RingBindingDetails", value.asJson))
      case ProductionLooseBinding.Strip(details) =>
        details.toVector.map(value => JsonHelpers.member("StripBindingDetails", value.asJson))

  given Encoder[LooseBindingParams] = Encoder.instance(params =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("BindingType", Json.fromString(looseBindingTypeName(params.binding)))),
        JsonHelpers.optMember("CoverMaterial", params.coverMaterial),
        JsonHelpers.vecMember("HolePattern", params.holePatterns),
        looseBindingDetailsMembers(params.binding),
      ),
    ),
  )
  given Decoder[LooseBindingParams] = Decoder.instance(cursor =>
    for
      bindingType   <- cursor.get[String]("BindingType")
      coverMaterial <- JsonHelpers.opt[Nmtoken](cursor, "CoverMaterial")
      holePatterns  <- JsonHelpers.vec[HolePattern](cursor, "HolePattern")
      binding       <- bindingType match
        case "ChannelBinding" =>
          JsonHelpers.opt[ChannelBindingProductionDetails](
            cursor,
            "ChannelBindingDetails"
          ).map(ProductionLooseBinding.Channel(_))
        case "CoilBinding" =>
          JsonHelpers.opt[CoilBindingProductionDetails](
            cursor,
            "CoilBindingDetails"
          ).map(ProductionLooseBinding.Coil(_))
        case "CombBinding" =>
          JsonHelpers.opt[CombBindingProductionDetails](
            cursor,
            "CombBindingDetails"
          ).map(ProductionLooseBinding.Comb(_))
        case "RingBinding" =>
          JsonHelpers.opt[RingBindingProductionDetails](
            cursor,
            "RingBindingDetails"
          ).map(ProductionLooseBinding.Ring(_))
        case "StripBinding" =>
          JsonHelpers.opt[StripBindingProductionDetails](
            cursor,
            "StripBindingDetails"
          ).map(ProductionLooseBinding.Strip(_))
        case other => JsonHelpers.fail(cursor, s"unknown LooseBindingParams BindingType '$other'")
    yield LooseBindingParams(binding, coverMaterial, holePatterns),
  )

  // -- Assembly: the plan coproduct maps to BinderySignatureIDs / AssemblySection -------

  given Encoder[Assembly] = Encoder.instance(assembly =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        assembly.plan match
          case AssemblyPlan.Collecting(ids) =>
            Vector(JsonHelpers.member("BinderySignatureIDs", Json.arr(ids.map(id => Json.fromString(id.value))*)))
          case AssemblyPlan.Gathering(ids) =>
            Vector(JsonHelpers.member("BinderySignatureIDs", Json.arr(ids.map(id => Json.fromString(id.value))*)))
          case AssemblyPlan.Listed(sections) =>
            Vector(JsonHelpers.member("AssemblySection", Json.arr(sections.toVector.map(_.asJson)*)))
          case AssemblyPlan.None => Vector.empty,
      ),
    ),
  )
  given Decoder[Assembly] = Decoder.instance(cursor =>
    for
      binderySignatureIds <- JsonHelpers.vec[Nmtoken](cursor, "BinderySignatureIDs")
      sections            <- JsonHelpers.vec[AssemblySection](cursor, "AssemblySection")
      plan                <-
        if sections.nonEmpty then
          NonEmptyVector.from(sections) match
            case Right(nonEmpty) => Right(AssemblyPlan.Listed(nonEmpty))
            case Left(_) => JsonHelpers.fail(cursor, "AssemblySection must not be empty")
        else if binderySignatureIds.nonEmpty then Right(AssemblyPlan.Collecting(binderySignatureIds))
        else Right(AssemblyPlan.None)
    yield Assembly(plan),
  )

  // -- BindingIntent: BindingType plus per-case details -------------------------------

  private val bindingTypes: Vector[(BindingSpecification, String)] = Vector(
    BindingSpecification.AdhesiveNote() -> "AdhesiveNote",
    BindingSpecification.ChannelBinding() -> "ChannelBinding",
    BindingSpecification.CoilBinding() -> "CoilBinding",
    BindingSpecification.CombBinding() -> "CombBinding",
    BindingSpecification.CornerStitch -> "CornerStitch",
    BindingSpecification.EdgeGluing() -> "EdgeGluing",
    BindingSpecification.HardCover() -> "HardCover",
    BindingSpecification.LooseBinding() -> "LooseBinding",
    BindingSpecification.None -> "None",
    BindingSpecification.RingBinding() -> "RingBinding",
    BindingSpecification.SaddleStitch() -> "SaddleStitch",
    BindingSpecification.SideStitch() -> "SideStitch",
    BindingSpecification.SoftCover() -> "SoftCover",
    BindingSpecification.StripBinding() -> "StripBinding",
    BindingSpecification.Tape -> "Tape",
    BindingSpecification.WireComb() -> "WireComb",
  )

  private def bindingTypeName(binding: BindingSpecification): String =
    bindingTypes.find(_._1.productPrefix == binding.productPrefix).map(_._2).getOrElse(binding.productPrefix)

  private def bindingDetailsMembers(binding: BindingSpecification): Vector[(String, Json)] =
    binding match
      case BindingSpecification.AdhesiveNote(details) =>
        details.toVector.map(value => JsonHelpers.member("AdhesiveNote", value.asJson))
      case BindingSpecification.ChannelBinding(details) =>
        details.toVector.map(value => JsonHelpers.member("LooseBinding", value.asJson))
      case BindingSpecification.CoilBinding(details) =>
        details.toVector.map(value => JsonHelpers.member("LooseBinding", value.asJson))
      case BindingSpecification.CombBinding(details) =>
        details.toVector.map(value => JsonHelpers.member("LooseBinding", value.asJson))
      case BindingSpecification.EdgeGluing(details) =>
        details.toVector.map(value => JsonHelpers.member("EdgeGluing", value.asJson))
      case BindingSpecification.HardCover(details) =>
        details.toVector.map(value => JsonHelpers.member("HardCoverBinding", value.asJson))
      case BindingSpecification.LooseBinding(details) =>
        details.toVector.map(value => JsonHelpers.member("LooseBinding", value.asJson))
      case BindingSpecification.RingBinding(details) =>
        details.toVector.map(value => JsonHelpers.member("LooseBinding", value.asJson))
      case BindingSpecification.SaddleStitch(details) =>
        details.toVector.map(value => JsonHelpers.member("SaddleStitching", value.asJson))
      case BindingSpecification.SideStitch(details) =>
        details.toVector.map(value => JsonHelpers.member("SideStitching", value.asJson))
      case BindingSpecification.SoftCover(details) =>
        details.toVector.map(value => JsonHelpers.member("SoftCoverBinding", value.asJson))
      case BindingSpecification.StripBinding(details) =>
        details.toVector.map(value => JsonHelpers.member("LooseBinding", value.asJson))
      case BindingSpecification.WireComb(details) =>
        details.toVector.map(value => JsonHelpers.member("LooseBinding", value.asJson))
      case _ => Vector.empty

  given Encoder[BindingIntent] = Encoder.instance(intent =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("BackCoverColor", intent.backCoverColor),
        JsonHelpers.optMember("BackCoverColorDetails", intent.backCoverColorDetails),
        JsonHelpers.optMember("BindingColor", intent.bindingColor),
        JsonHelpers.optMember("BindingColorDetails", intent.bindingColorDetails),
        JsonHelpers.optMember("BindingOrder", intent.bindingOrder),
        JsonHelpers.optMember("BindingSide", intent.bindingSide),
        JsonHelpers.vecMemberOf("ChildRefs", intent.childRefs.toVector.flatMap(_.toVector))(ref =>
          Json.fromString(ref.value)
        ),
        JsonHelpers.optMember("CoverColor", intent.coverColor),
        JsonHelpers.optMember("CoverColorDetails", intent.coverColorDetails),
        Vector(JsonHelpers.member("BindingType", Json.fromString(bindingTypeName(intent.binding)))),
        JsonHelpers.optMember("Tabs", intent.tabs),
        bindingDetailsMembers(intent.binding),
      ),
    ),
  )
  given Decoder[BindingIntent] = Decoder.instance(cursor =>
    for
      bindingType           <- cursor.get[String]("BindingType")
      backCoverColor        <- JsonHelpers.opt[NamedColor](cursor, "BackCoverColor")
      backCoverColorDetails <- JsonHelpers.opt[XjdfString](cursor, "BackCoverColorDetails")
      bindingColor          <- JsonHelpers.opt[NamedColor](cursor, "BindingColor")
      bindingColorDetails   <- JsonHelpers.opt[XjdfString](cursor, "BindingColorDetails")
      bindingOrder          <- JsonHelpers.opt[BindingOrder](cursor, "BindingOrder")
      bindingSide           <- JsonHelpers.opt[BindingEdge](cursor, "BindingSide")
      childRefs             <- JsonHelpers.vec[XsdIdRef](cursor, "ChildRefs")
      coverColor            <- JsonHelpers.opt[NamedColor](cursor, "CoverColor")
      coverColorDetails     <- JsonHelpers.opt[XjdfString](cursor, "CoverColorDetails")
      tabs                  <- JsonHelpers.opt[Tabs](cursor, "Tabs")
      binding               <- decodeBindingDetails(bindingType, cursor)
    yield BindingIntent(
      binding,
      backCoverColor,
      backCoverColorDetails,
      bindingColor,
      bindingColorDetails,
      bindingOrder,
      bindingSide,
      TwoOrMore.from(childRefs).toOption,
      coverColor,
      coverColorDetails,
      tabs,
    ),
  )

  private def decodeBindingDetails(bindingType: String, cursor: HCursor): Decoder.Result[BindingSpecification] =
    bindingType match
      case "AdhesiveNote" =>
        JsonHelpers.opt[AdhesiveNoteDetails](cursor, "AdhesiveNote").map(BindingSpecification.AdhesiveNote(_))
      case "ChannelBinding" =>
        JsonHelpers.opt[LooseBindingDetails](cursor, "LooseBinding").map(BindingSpecification.ChannelBinding(_))
      case "CoilBinding" =>
        JsonHelpers.opt[CoilLooseBindingDetails](cursor, "LooseBinding").map(BindingSpecification.CoilBinding(_))
      case "CombBinding" =>
        JsonHelpers.opt[CombLooseBindingDetails](cursor, "LooseBinding").map(BindingSpecification.CombBinding(_))
      case "CornerStitch" => Right(BindingSpecification.CornerStitch)
      case "EdgeGluing" =>
        JsonHelpers.opt[EdgeGluingDetails](cursor, "EdgeGluing").map(BindingSpecification.EdgeGluing(_))
      case "HardCover" =>
        JsonHelpers.opt[HardCoverBindingDetails](cursor, "HardCoverBinding").map(BindingSpecification.HardCover(_))
      case "LooseBinding" =>
        JsonHelpers.opt[LooseBindingDetails](cursor, "LooseBinding").map(BindingSpecification.LooseBinding(_))
      case "None" => Right(BindingSpecification.None)
      case "RingBinding" =>
        JsonHelpers.opt[RingLooseBindingDetails](cursor, "LooseBinding").map(BindingSpecification.RingBinding(_))
      case "SaddleStitch" =>
        JsonHelpers.opt[StitchingDetails](cursor, "SaddleStitching").map(BindingSpecification.SaddleStitch(_))
      case "SideStitch" =>
        JsonHelpers.opt[StitchingDetails](cursor, "SideStitching").map(BindingSpecification.SideStitch(_))
      case "SoftCover" =>
        JsonHelpers.opt[SoftCoverBindingDetails](cursor, "SoftCoverBinding").map(BindingSpecification.SoftCover(_))
      case "StripBinding" =>
        JsonHelpers.opt[LooseBindingDetails](cursor, "LooseBinding").map(BindingSpecification.StripBinding(_))
      case "Tape" => Right(BindingSpecification.Tape)
      case "WireComb" =>
        JsonHelpers.opt[LooseBindingDetails](cursor, "LooseBinding").map(BindingSpecification.WireComb(_))
      case other => JsonHelpers.fail(cursor, s"unknown BindingType '$other'")

  // -- ColorIntent: SurfaceColor members distinguished by @Surface ---------------------

  private def surfaceColorMember(side: Side, value: SurfaceColor): Json =
    value.asJson.mapObject(_.add("Surface", Json.fromString(side.toString)))

  given Encoder[ColorIntent] = Encoder.instance(intent =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        intent.surfaces match
          case ColorSurfaces.Unprinted => Vector.empty
          case ColorSurfaces.Front(front) =>
            Vector(JsonHelpers.member("SurfaceColor", Json.arr(surfaceColorMember(Side.Front, front))))
          case ColorSurfaces.Back(back) =>
            Vector(JsonHelpers.member("SurfaceColor", Json.arr(surfaceColorMember(Side.Back, back))))
          case ColorSurfaces.Both(front, back) =>
            Vector(JsonHelpers.member(
              "SurfaceColor",
              Json.arr(surfaceColorMember(Side.Front, front), surfaceColorMember(Side.Back, back))
            )),
      ),
    ),
  )
  given Decoder[ColorIntent] = Decoder.instance(cursor =>
    for
      items <- cursor.downField("SurfaceColor").focus match
        case Some(json) => json.as[List[Json]]
        case None => Right(List.empty)
      pairs <- items.foldLeft[Decoder.Result[Vector[(Side, SurfaceColor)]]](Right(Vector.empty)) { (acc, item) =>
        for
          accumulated <- acc
          side        <- item.hcursor.get[Side]("Surface")
          color       <- item.mapObject(_.remove("Surface")).as[SurfaceColor]
        yield accumulated :+ (side, color)
      }
      surfaces <- pairs match
        case Vector((Side.Front, front), (Side.Back, back)) => Right(ColorSurfaces.Both(front, back))
        case Vector((Side.Front, front)) => Right(ColorSurfaces.Front(front))
        case Vector((Side.Back, back)) => Right(ColorSurfaces.Back(back))
        case Vector() => Right(ColorSurfaces.Unprinted)
        case _ => JsonHelpers.fail(cursor, "one front and one back SurfaceColor at most")
    yield ColorIntent(surfaces),
  )

  // -- ModifyQueueEntryParams: Operation plus target payload ---------------------------

  private val queueOperations: Vector[(QueueModification, String)] = Vector(
    QueueModification.Abort -> "Abort",
    QueueModification.Complete -> "Complete",
    QueueModification.Hold -> "Hold",
    QueueModification.Remove -> "Remove",
    QueueModification.Resume -> "Resume",
    QueueModification.Suspend -> "Suspend",
    QueueModification.Move() -> "Move",
    QueueModification.SetGang() -> "SetGang",
  )

  private def queueOperationName(operation: QueueModification): String =
    queueOperations.find(_._1.productPrefix == operation.productPrefix).map(_._2).getOrElse(operation.productPrefix)

  given Encoder[ModifyQueueEntryParams] = Encoder.instance(params =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("Operation", Json.fromString(queueOperationName(params.operation)))),
        Vector(JsonHelpers.member("QueueFilter", params.filter.asJson)),
        params.operation match
          case QueueModification.Move(target) =>
            target.toVector.flatMap {
              case QueueMoveTarget.After(next) =>
                Vector(JsonHelpers.member("NextQueueEntryID", Json.fromString(next.value)))
              case QueueMoveTarget.Before(prev) =>
                Vector(JsonHelpers.member("PrevQueueEntryID", Json.fromString(prev.value)))
              case QueueMoveTarget.Position(pos) => Vector(JsonHelpers.member("Position", Json.fromInt(pos)))
              case QueueMoveTarget.Priority(prio) => Vector(JsonHelpers.member("Priority", Json.fromInt(prio.value)))
            }
          case QueueModification.SetGang(gangName) =>
            JsonHelpers.optMember("GangName", gangName)
          case _ => Vector.empty,
      ),
    ),
  )
  given Decoder[ModifyQueueEntryParams] = Decoder.instance(cursor =>
    for
      operation        <- cursor.get[String]("Operation")
      filter           <- cursor.get[QueueFilter]("QueueFilter")
      nextQueueEntryId <- JsonHelpers.opt[Nmtoken](cursor, "NextQueueEntryID")
      prevQueueEntryId <- JsonHelpers.opt[Nmtoken](cursor, "PrevQueueEntryID")
      position         <- JsonHelpers.opt[Int](cursor, "Position")
      priority         <- JsonHelpers.opt[Priority0To100](cursor, "Priority")
      gangName         <- JsonHelpers.opt[Nmtoken](cursor, "GangName")
      modification     <- operation match
        case "Move" =>
          val target = (nextQueueEntryId, prevQueueEntryId, position, priority) match
            case (Some(next), _, _, _) => Some(QueueMoveTarget.After(next))
            case (_, Some(prev), _, _) => Some(QueueMoveTarget.Before(prev))
            case (_, _, Some(pos), _) => Some(QueueMoveTarget.Position(pos))
            case (_, _, _, Some(prio)) => Some(QueueMoveTarget.Priority(prio))
            case _ => None
          Right(QueueModification.Move(target))
        case "SetGang" => Right(QueueModification.SetGang(gangName))
        case "Abort" => Right(QueueModification.Abort)
        case "Complete" => Right(QueueModification.Complete)
        case "Hold" => Right(QueueModification.Hold)
        case "Remove" => Right(QueueModification.Remove)
        case "Resume" => Right(QueueModification.Resume)
        case "Suspend" => Right(QueueModification.Suspend)
        case other => JsonHelpers.fail(cursor, s"unknown Operation '$other'")
    yield ModifyQueueEntryParams(modification, filter),
  )

  // -- QueueSubmissionParams: position payload flattens to attributes ------------------

  given Encoder[QueueSubmissionParams] = Encoder.instance(params =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("URL", Json.fromString(params.url.value.toString))),
        JsonHelpers.optMember("Activation", params.activation),
        JsonHelpers.optMember("GangName", params.gangName),
        JsonHelpers.optMember("GangPolicy", params.gangPolicy),
        params.position.toVector.flatMap {
          case QueueSubmissionPosition.After(next) =>
            Vector(JsonHelpers.member("NextQueueEntryID", Json.fromString(next.value)))
          case QueueSubmissionPosition.Before(prev) =>
            Vector(JsonHelpers.member("PrevQueueEntryID", Json.fromString(prev.value)))
          case QueueSubmissionPosition.Priority(prio) =>
            Vector(JsonHelpers.member("Priority", Json.fromInt(prio.value)))
        },
        JsonHelpers.optMember("ReturnJMF", params.returnJmf),
      ),
    ),
  )
  given Decoder[QueueSubmissionParams] = Decoder.instance(cursor =>
    for
      url              <- cursor.get[UriRef]("URL")
      activation       <- JsonHelpers.opt[QueueActivation](cursor, "Activation")
      gangName         <- JsonHelpers.opt[Nmtoken](cursor, "GangName")
      gangPolicy       <- JsonHelpers.opt[QueueGangPolicy](cursor, "GangPolicy")
      nextQueueEntryId <- JsonHelpers.opt[Nmtoken](cursor, "NextQueueEntryID")
      prevQueueEntryId <- JsonHelpers.opt[Nmtoken](cursor, "PrevQueueEntryID")
      priority         <- JsonHelpers.opt[Priority0To100](cursor, "Priority")
      returnJmf        <- JsonHelpers.opt[UriRef](cursor, "ReturnJMF")
      position         <- (nextQueueEntryId, prevQueueEntryId, priority) match
        case (Some(next), _, _) => Right(Some(QueueSubmissionPosition.After(next)))
        case (_, Some(prev), _) => Right(Some(QueueSubmissionPosition.Before(prev)))
        case (_, _, Some(prio)) => Right(Some(QueueSubmissionPosition.Priority(prio)))
        case _ => Right(None)
    yield QueueSubmissionParams(url, activation, gangName, gangPolicy, position, returnJmf),
  )

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
      tagType   <- cursor.get[Int]("TagType")
      binary    <- JsonHelpers.opt[Vector[Byte]](cursor, "BinaryValue")
      integers  <- JsonHelpers.vec[Int](cursor, "IntegerValue")
      numbers   <- JsonHelpers.vec[Float](cursor, "NumberValue")
      text      <- JsonHelpers.opt[XjdfString](cursor, "StringValue")
      value     <- (binary, integers, numbers, text) match
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
        Vector(JsonHelpers.member(
          "NumberValue",
          Json.arr(numbers.map(number => Json.fromFloat(number).getOrElse(Json.Null))*)
        ))
      case Some(TiffTagValue.Text(text)) =>
        Vector(JsonHelpers.member("StringValue", Json.fromString(text)))
      case None => Vector.empty
end JsonSpecialCodecs
