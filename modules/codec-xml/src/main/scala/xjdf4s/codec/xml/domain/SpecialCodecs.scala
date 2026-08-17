package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.codec.xml.derivation.FieldCodec
import xjdf4s.core.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/** FileSpec: the location XOR coproduct maps to the @URL/@UID/@FileFormat+@FileTemplate attribute sets. */
object FileSpecCodec:
  val decoder: XmlDecoder[FileSpec] =
    XmlDecoder.instance: element =>
      for
        checkSum <- XmlDecoders.attributeOf("CheckSum")(Lexical.hexBinary).decode(element)
        encoding <- XmlDecoders.attributeOf("Encoding")(Lexical.nmtoken).decode(element)
        fileSize <- XmlDecoders.attributeOf("FileSize")(Lexical.long).decode(element)
        mimeType <- XmlDecoders.attributeOf("MIMEType")(Lexical.xjdfString).decode(element)
        numberOfPages <- XmlDecoders.attributeOf("NPage")(Lexical.int).decode(element)
        overwritePolicy <- XmlDecoders.attributeOf("OverwritePolicy")(Lexical.enumOf(
          OverwritePolicy.values.toVector,
          _.toString,
        )).decode(element)
        password <- XmlDecoders.attributeOf("Password")(Lexical.xjdfString).decode(element)
        resourceUsage <- XmlDecoders.attributeOf("ResourceUsage")(Lexical.nmtoken).decode(element)
        searchDepth <- XmlDecoders.attributeOf("SearchDepth")(Lexical.int).decode(element)
        userFileName <- XmlDecoders.attributeOf("UserFileName")(Lexical.xjdfString).decode(element)
        url <- XmlDecoders.attributeOf("URL")(Lexical.uri).decode(element)
        uid <- XmlDecoders.attributeOf("UID")(Lexical.nmtoken).decode(element)
        fileFormat <- XmlDecoders.attributeOf("FileFormat")(Lexical.xjdfString).decode(element)
        fileTemplate <- XmlDecoders.attributeOf("FileTemplate")(Lexical.nmtokens).decode(element)
        disposition <- XmlDecoders.optionalChild("Disposition")(DispositionCodec.decoder).decode(element)
        networkHeaders <- XmlDecoders.repeatedChild("NetworkHeader")(NetworkHeaderCodec.decoder).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("Disposition", "NetworkHeader")).decode(element)
        location <- (url, uid, fileFormat, fileTemplate) match
          case (Some(u), None, None, None) => Right(FileLocation.Url(u))
          case (None, Some(u), None, None) => Right(FileLocation.Uid(u))
          case (None, None, Some(format), Some(template)) =>
            NonEmptyVector.from(template) match
              case Right(nonEmpty) => Right(FileLocation.Sequence(format, nonEmpty))
              case Left(_)         => Left(XmlError.InvalidAttribute("FileSpec", "FileTemplate", "", "a non-empty NMTOKENS"))
          case (None, None, None, None) => Right(FileLocation.Pipe)
          case _ => Left(XmlError.ConflictingFields("FileSpec", "URL/UID/FileFormat+FileTemplate"))
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
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[FileSpec] =
    XmlEncoder.instance: fileSpec =>
      val locationAttributes = fileSpec.location match
        case FileLocation.Url(url) => CodecHelpers.attribute("URL", Some(url.value.toString))
        case FileLocation.Uid(uid) => CodecHelpers.attribute("UID", Some(uid.value))
        case FileLocation.Sequence(format, template) =>
          CodecHelpers.attribute("FileFormat", Some(format.value)) ++
            CodecHelpers.attribute("FileTemplate", Some(CodecHelpers.renderNmtokens(template.toVector)))
        case FileLocation.Pipe => Vector.empty
      val attributes =
        CodecHelpers.attribute(
          "CheckSum",
          Option.when(fileSpec.checkSum.exists(_.nonEmpty))(
            Lexical.renderHexBinary(fileSpec.checkSum.getOrElse(Vector.empty)),
          ),
        ) ++
          CodecHelpers.attributeOf("Encoding", fileSpec.encoding, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("FileSize", fileSpec.fileSize, _.toString) ++
          CodecHelpers.attributeOf("MIMEType", fileSpec.mimeType, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("NPage", fileSpec.numberOfPages, CodecHelpers.renderInt) ++
          CodecHelpers.attributeOf("OverwritePolicy", fileSpec.overwritePolicy, _.toString) ++
          CodecHelpers.attributeOf("Password", fileSpec.password, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("ResourceUsage", fileSpec.resourceUsage, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("SearchDepth", fileSpec.searchDepth, CodecHelpers.renderInt) ++
          CodecHelpers.attributeOf("UserFileName", fileSpec.userFileName, (v: XjdfString) => v.value) ++
          locationAttributes ++ CodecHelpers.extensionAttributes(fileSpec.extensions)
      val children =
        fileSpec.disposition.toVector.map(DispositionCodec.encoder.encode) ++
          fileSpec.networkHeaders.map(NetworkHeaderCodec.encoder.encode)
      Xml.Element(CodecHelpers.qname("FileSpec"), attributes, children)
end FileSpecCodec

object DispositionCodec:
  val decoder: XmlDecoder[Disposition] =
    XmlDecoder.instance: element =>
      for
        action <- XmlDecoders.attributeOf("Action")(Lexical.enumOf(DispositionAction.values.toVector, _.toString))
          .decode(element)
        extraDuration <- XmlDecoders.attributeOf("ExtraDuration")(Lexical.duration).decode(element)
        minDuration <- XmlDecoders.attributeOf("MinDuration")(Lexical.duration).decode(element)
        until <- XmlDecoders.attributeOf("Until")(Lexical.dateTime).decode(element)
        priority <- XmlDecoders.attributeOf("Priority")(Lexical.priority).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
        time <- (minDuration, until) match
          case (Some(duration), None) => Right(Some(DispositionTime.AfterProcess(duration)))
          case (None, Some(at))      => Right(Some(DispositionTime.At(at)))
          case (None, None)          => Right(None)
          case _ => Left(XmlError.ConflictingFields(element.name.localName, "MinDuration/Until"))
      yield Disposition(action, time, extraDuration, priority, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[Disposition] =
    XmlEncoder.instance: disposition =>
      val timeAttributes = disposition.time match
        case Some(DispositionTime.AfterProcess(duration)) =>
          CodecHelpers.attribute("MinDuration", Some(duration.value))
        case Some(DispositionTime.At(until)) => CodecHelpers.attribute("Until", Some(until.value))
        case None                            => Vector.empty
      val attributes =
        CodecHelpers.attributeOf("Action", disposition.action, _.toString) ++
          CodecHelpers.attributeOf("ExtraDuration", disposition.extraDuration, (v: XsdDuration) => v.value) ++
          timeAttributes ++
          CodecHelpers.attributeOf("Priority", disposition.priority, (v: Priority0To100) => v.value.toString) ++
          CodecHelpers.extensionAttributes(disposition.extensions)
      Xml.Element(CodecHelpers.qname("Disposition"), attributes, Vector.empty)
end DispositionCodec

object NetworkHeaderCodec:
  val decoder: XmlDecoder[NetworkHeader] =
    XmlDecoder.instance: element =>
      for
        name <- XmlDecoders.requiredAttribute("Name")(Lexical.xjdfString).decode(element)
        value <- XjdfString
          .from(element.text)
          .left
          .map(error => XmlError.InvalidAttribute("NetworkHeader", "text", element.text, error.toString))
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
      yield NetworkHeader(name, value, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[NetworkHeader] =
    XmlEncoder.instance: header =>
      Xml.Element(
        CodecHelpers.qname("NetworkHeader"),
        CodecHelpers.attribute("Name", Some(header.name.value)) ++ CodecHelpers.extensionAttributes(header.extensions),
        Vector(Xml.Text(header.value.value)),
      )
end NetworkHeaderCodec

object ExtraValuesCodec:
  val decoder: XmlDecoder[ExtraValues] =
    XmlDecoder.instance: element =>
      for
        usage <- XmlDecoders.requiredAttribute("Usage")(Lexical.nmtoken).decode(element)
        value <- XjdfString.from(element.text).left.map(error => XmlError.InvalidAttribute("ExtraValues", "text", element.text, error.toString))
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
      yield ExtraValues(usage, value, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[ExtraValues] =
    XmlEncoder.instance: extraValues =>
      Xml.Element(
        CodecHelpers.qname("ExtraValues"),
        CodecHelpers.attribute("Usage", Some(extraValues.usage.value)) ++
          CodecHelpers.extensionAttributes(extraValues.extensions),
        Vector(Xml.Text(extraValues.value.value)),
      )
end ExtraValuesCodec

/** TIFFtag: the normative element name is `TIFFtag` (lowercase tag), and the value is one XOR attribute. */
object TiffTagCodec:
  val decoder: XmlDecoder[TiffTag] =
    XmlDecoder.instance: element =>
      for
        tagNumber <- XmlDecoders.requiredAttribute("TagNumber")(Lexical.int).decode(element)
        tagType <- XmlDecoders.requiredAttribute("TagType")(Lexical.int).decode(element)
        binaryValue <- XmlDecoders.attributeOf("BinaryValue")(Lexical.hexBinary).decode(element)
        integerValue <- XmlDecoders.attributeOf("IntegerValue")(Lexical.intList).decode(element)
        numberValue <- XmlDecoders.attributeOf("NumberValue")(Lexical.floatList).decode(element)
        stringValue <- XmlDecoders.attributeOf("StringValue")(Lexical.xjdfString).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
        value <- (binaryValue, integerValue, numberValue, stringValue) match
          case (Some(bytes), _, _, _)   => Right(Some(TiffTagValue.Binary(bytes)))
          case (_, Some(ints), _, _)    => Right(Some(TiffTagValue.Integers(ints)))
          case (_, _, Some(numbers), _) => Right(Some(TiffTagValue.Numbers(numbers)))
          case (_, _, _, Some(text))    => Right(Some(TiffTagValue.Text(text.value)))
          case _                        => Right(None)
      yield TiffTag(tagNumber, tagType, value, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[TiffTag] =
    XmlEncoder.instance: tag =>
      val valueAttributes = tag.value match
        case Some(TiffTagValue.Binary(bytes)) =>
          CodecHelpers.attribute("BinaryValue", Some(Lexical.renderHexBinary(bytes)))
        case Some(TiffTagValue.Integers(ints)) =>
          CodecHelpers.attribute("IntegerValue", Some(CodecHelpers.renderInts(ints)))
        case Some(TiffTagValue.Numbers(numbers)) =>
          CodecHelpers.attribute("NumberValue", Some(CodecHelpers.renderFloats(numbers)))
        case Some(TiffTagValue.Text(text)) => CodecHelpers.attribute("StringValue", Some(text))
        case None                          => Vector.empty
      val attributes =
        CodecHelpers.attribute("TagNumber", Some(CodecHelpers.renderInt(tag.tagNumber))) ++
          CodecHelpers.attribute("TagType", Some(CodecHelpers.renderInt(tag.tagType))) ++
          valueAttributes ++ CodecHelpers.extensionAttributes(tag.extensions)
      Xml.Element(CodecHelpers.qname("TIFFtag"), attributes, Vector.empty)
end TiffTagCodec

/** Patch (Table 8.10): color patch with SeparationTint children. */
object PatchCodec:
  val decoder: XmlDecoder[Patch] =
    XmlDecoder.instance: element =>
      for
        usage <- XmlDecoders.requiredAttribute("PatchUsage")(Lexical.enumOf(PatchUsage.values.toVector, _.toString))
          .decode(element)
        center <- XmlDecoders.attributeOf("Center")(Lexical.xypair).decode(element)
        density <- XmlDecoders.attributeOf("Density")(Lexical.float).decode(element)
        externalId <- XmlDecoders.attributeOf("ExternalID")(Lexical.nmtoken).decode(element)
        lab <- XmlDecoders.attributeOf("Lab")(Lexical.labColor).decode(element)
        neutralDensity <- XmlDecoders.attributeOf("NeutralDensity")(Lexical.neutralDensity).decode(element)
        rgb <- XmlDecoders.attributeOf("RGB")(Lexical.srgbColor).decode(element)
        size <- XmlDecoders.attributeOf("Size")(Lexical.xypair).decode(element)
        spectrum <- XmlDecoders.attributeOf("Spectrum")(Lexical.transferFunction).decode(element)
        spotType <- XmlDecoders.attributeOf("SpotType")(Lexical.enumOf(SpotType.values.toVector, _.toString))
          .decode(element)
        separationTints <- XmlDecoders.repeatedChild("SeparationTint")(SeparationTintCodec.decoder).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("SeparationTint")).decode(element)
      yield Patch(
        usage,
        center,
        density,
        externalId,
        lab,
        neutralDensity,
        rgb,
        size,
        spectrum,
        spotType,
        separationTints,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[Patch] =
    XmlEncoder.instance: patch =>
      val attributes =
        CodecHelpers.attributeOf("Center", patch.center, CodecHelpers.renderXypair) ++
          CodecHelpers.attributeOf("Density", patch.density, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("ExternalID", patch.externalId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Lab", patch.lab, CodecHelpers.renderLabColor) ++
          CodecHelpers.attributeOf("NeutralDensity", patch.neutralDensity, (v: NeutralDensity) => v.value.toString) ++
          CodecHelpers.attributeOf("RGB", patch.rgb, CodecHelpers.renderSrgbColor) ++
          CodecHelpers.attributeOf("Size", patch.size, CodecHelpers.renderXypair) ++
          CodecHelpers.attribute(
            "Spectrum",
            patch.spectrum.map(spectrum => CodecHelpers.renderFloats(spectrum.toVector)),
          ) ++
          CodecHelpers.attributeOf("SpotType", patch.spotType, _.toString) ++
          CodecHelpers.attribute("PatchUsage", Some(patch.usage.toString)) ++
          CodecHelpers.extensionAttributes(patch.extensions)
      Xml.Element(
        CodecHelpers.qname("Patch"),
        attributes,
        patch.separationTints.map(SeparationTintCodec.encoder.encode),
      )
end PatchCodec

object SeparationTintCodec:
  val decoder: XmlDecoder[SeparationTint] =
    XmlDecoder.instance: element =>
      for
        name <- XmlDecoders.requiredAttribute("Name")(Lexical.nmtoken).decode(element)
        tint <- XmlDecoders.requiredAttribute("Tint")(Lexical.float).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
      yield SeparationTint(name, tint)

  val encoder: XmlEncoder[SeparationTint] =
    XmlEncoder.instance: tint =>
      Xml.Element(
        CodecHelpers.qname("SeparationTint"),
        CodecHelpers.attribute("Name", Some(tint.name.value)) ++
          CodecHelpers.attribute("Tint", Some(CodecHelpers.renderFloat(tint.tint))),
        Vector.empty,
      )
end SeparationTintCodec

/**
 * FileSpec-role wrappers: several resources carry two or more FileSpec children distinguished by `@ResourceUsage`
 * (`DeviceSchemas`, `DeviceInfoSchemas`, `DeliveryFiles`, `VerificationFiles`, `QualityControlFiles`). Each wrapper
 * is a FieldCodec that selects the FileSpec children of the owning element.
 */
object FileSpecRoles:

  private def split(children: Vector[Xml.Element], roles: Set[String])
      : Either[XmlError, Vector[(String, FileSpec)]] =
    children.foldLeft[Either[XmlError, Vector[(String, FileSpec)]]](Right(Vector.empty)) { (acc, child) =>
      for
        pairs <- acc
        role = child.attribute("ResourceUsage").getOrElse("")
        _ <- if roles.contains(role) then Right(()) else Left(XmlError.InvalidAttribute("FileSpec", "ResourceUsage", role, "one of " + roles.mkString(", ")))
        fileSpec <- FileSpecCodec.decoder.decode(child)
      yield pairs :+ (role, fileSpec)
    }

  private def encode(role: String, fileSpec: Option[FileSpec]): Vector[Xml.Element] =
    fileSpec.toVector.map: spec =>
      val withRole = spec.copy(resourceUsage = Some(Nmtoken.from(role).toOption.get))
      FileSpecCodec.encoder.encode(withRole)

  def wrapper[W](
      names: Vector[(String, W => Option[FileSpec])],
      build: Vector[(String, FileSpec)] => W,
  ): FieldCodec[W] =
    new FieldCodec[W]:
      def isElement: Boolean = true
      def elementName: String = "FileSpec"
      def decodeAttribute(raw: Option[String]): Either[String, W] = Left("element, not attribute")
      def renderAttribute(value: W): Option[String] = None
      def decodeElements(children: Vector[Xml.Element]): Either[XmlError, W] =
        split(children, names.map(_._1).toSet).map(build)
      def encodeElements(value: W): Vector[Xml.Element] =
        names.flatMap { case (role, getter) => encode(role, getter(value)) }
  end wrapper

end FileSpecRoles

given deviceSchemasField: FieldCodec[DeviceSchemas] = FileSpecRoles.wrapper(
    Vector("CurrentSchema" -> (_.current), "Schema" -> (_.global)),
    pairs => DeviceSchemas(pairs.find(_._1 == "CurrentSchema").map(_._2), pairs.find(_._1 == "Schema").map(_._2)),
  )

given deviceInfoSchemasField: FieldCodec[DeviceInfoSchemas] = FileSpecRoles.wrapper(
    Vector("CurrentSchema" -> (_.current), "Schema" -> (_.global)),
    pairs => DeviceInfoSchemas(pairs.find(_._1 == "CurrentSchema").map(_._2), pairs.find(_._1 == "Schema").map(_._2)),
  )

given deliveryFilesField: FieldCodec[DeliveryFiles] = FileSpecRoles.wrapper(
    Vector("Contents" -> (_.contents), "MailingList" -> (_.mailingList), "RemoteURL" -> (_.remoteUrl)),
    pairs =>
      DeliveryFiles(
        pairs.find(_._1 == "Contents").map(_._2),
        pairs.find(_._1 == "MailingList").map(_._2),
        pairs.find(_._1 == "RemoteURL").map(_._2),
      ),
  )

given verificationFilesField: FieldCodec[VerificationFiles] = FileSpecRoles.wrapper(
    Vector("Accepted" -> (_.accepted), "Combined" -> (_.combined), "Rejected" -> (_.rejected), "Unknown" -> (_.unknown)),
    pairs =>
      VerificationFiles(
        pairs.find(_._1 == "Accepted").map(_._2),
        pairs.find(_._1 == "Combined").map(_._2),
        pairs.find(_._1 == "Rejected").map(_._2),
        pairs.find(_._1 == "Unknown").map(_._2),
      ),
  )

given qualityControlFilesField: FieldCodec[QualityControlFiles] = FileSpecRoles.wrapper(
  Vector("Image" -> (_.image), "Setup" -> (_.setup)),
  pairs => QualityControlFiles(pairs.find(_._1 == "Image").map(_._2), pairs.find(_._1 == "Setup").map(_._2)),
)

// -- givens --------------------------------------------------------------------

given fileSpecCodec: XmlElementCodec[FileSpec] = XmlElementCodec.instance("FileSpec")(
  FileSpecCodec.decoder.decode,
  FileSpecCodec.encoder.encode,
)
given fileSpecField: FieldCodec[FileSpec] = FieldCodec.element(summon[XmlElementCodec[FileSpec]])

given dispositionCodec: XmlElementCodec[Disposition] = XmlElementCodec.instance("Disposition")(
  DispositionCodec.decoder.decode,
  DispositionCodec.encoder.encode,
)
given dispositionField: FieldCodec[Disposition] = FieldCodec.element(summon[XmlElementCodec[Disposition]])

given networkHeaderCodec: XmlElementCodec[NetworkHeader] = XmlElementCodec.instance("NetworkHeader")(
  NetworkHeaderCodec.decoder.decode,
  NetworkHeaderCodec.encoder.encode,
)
given networkHeaderField: FieldCodec[NetworkHeader] = FieldCodec.element(summon[XmlElementCodec[NetworkHeader]])

given extraValuesCodec: XmlElementCodec[ExtraValues] = XmlElementCodec.instance("ExtraValues")(
  ExtraValuesCodec.decoder.decode,
  ExtraValuesCodec.encoder.encode,
)
given extraValuesField: FieldCodec[ExtraValues] = FieldCodec.element(summon[XmlElementCodec[ExtraValues]])

given tiffTagCodec: XmlElementCodec[TiffTag] = XmlElementCodec.instance("TIFFtag")(
  TiffTagCodec.decoder.decode,
  TiffTagCodec.encoder.encode,
)
given tiffTagField: FieldCodec[TiffTag] = FieldCodec.element(summon[XmlElementCodec[TiffTag]])

given patchCodec: XmlElementCodec[Patch] = XmlElementCodec.instance("Patch")(
  PatchCodec.decoder.decode,
  PatchCodec.encoder.encode,
)
given patchField: FieldCodec[Patch] = FieldCodec.element(summon[XmlElementCodec[Patch]])
