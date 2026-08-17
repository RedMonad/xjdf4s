package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.codec.xml.derivation.*
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*

object HeaderCodec:
  val decoder: XmlDecoder[Header] =
    XmlDecoder.instance: element =>
      for
        deviceId <- XmlDecoders.requiredAttribute("DeviceID")(Lexical.nmtoken).decode(element)
        time <- XmlDecoders.requiredAttribute("Time")(Lexical.dateTime).decode(element)
        agentName <- XmlDecoders.attributeOf("AgentName")(Lexical.xjdfString).decode(element)
        agentVersion <- XmlDecoders.attributeOf("AgentVersion")(Lexical.xjdfString).decode(element)
        author <- XmlDecoders.attributeOf("Author")(Lexical.xjdfString).decode(element)
        descriptiveName <- XmlDecoders.attributeOf("DescriptiveName")(Lexical.xjdfString).decode(element)
        icsVersions <- XmlDecoders.attributeOf("ICSVersions")(Lexical.nmtokens).decode(element)
        id <- XmlDecoders.attributeOf("ID")(Lexical.xsdId).decode(element)
        personalId <- XmlDecoders.attributeOf("PersonalID")(Lexical.nmtoken).decode(element)
        refId <- XmlDecoders.attributeOf("refID")(Lexical.nmtoken).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
      yield Header(
        deviceId,
        time,
        agentName,
        agentVersion,
        author,
        descriptiveName,
        icsVersions.getOrElse(Vector.empty),
        id,
        personalId,
        refId,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[Header] =
    XmlEncoder.instance: header =>
      val attributes =
        CodecHelpers.attributeOf("AgentName", header.agentName, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("AgentVersion", header.agentVersion, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("Author", header.author, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("DescriptiveName", header.descriptiveName, (v: XjdfString) => v.value) ++
          CodecHelpers.attribute("DeviceID", Some(header.deviceId.value)) ++
          CodecHelpers.attribute(
            "ICSVersions",
        Option.when(header.icsVersions.nonEmpty)(CodecHelpers.renderNmtokens(header.icsVersions)),
      ) ++
          CodecHelpers.attributeOf("ID", header.id, (v: XsdId) => v.value) ++
          CodecHelpers.attributeOf("PersonalID", header.personalId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("refID", header.refId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attribute("Time", Some(header.time.value)) ++
          CodecHelpers.extensionAttributes(header.extensions)
      Xml.Element(CodecHelpers.qname("Header"), attributes, Vector.empty)
end HeaderCodec

object SubscriptionCodec:
  private val channelMode: Lexical.Lex[ChannelMode] =
    Lexical.enumOf(ChannelMode.values.toVector, _.toString)

  val decoder: XmlDecoder[Subscription] =
    XmlDecoder.instance: element =>
      for
        url <- XmlDecoders.requiredAttribute("URL")(Lexical.uri).decode(element)
        channelModes <- XmlDecoders.attributeOf("ChannelMode")(Lexical.list(channelMode)).decode(element)
        languages <- XmlDecoders.attributeOf("Languages")(Lexical.languages).decode(element)
        repeatTime <- XmlDecoders.attributeOf("RepeatTime")(Lexical.float).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
      yield Subscription(
        url,
        channelModes.getOrElse(Vector.empty),
        languages.getOrElse(Vector.empty),
        repeatTime,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[Subscription] =
    XmlEncoder.instance: subscription =>
      val attributes =
        CodecHelpers.attribute(
          "ChannelMode",
        Option.when(subscription.channelMode.nonEmpty)(subscription.channelMode.map(_.toString).mkString(" ")),
      ) ++
          CodecHelpers.attribute(
            "Languages",
            Option.when(subscription.languages.nonEmpty)(CodecHelpers.renderLanguages(subscription.languages)),
          ) ++
          CodecHelpers.attributeOf("RepeatTime", subscription.repeatTime, CodecHelpers.renderFloat) ++
          CodecHelpers.attribute("URL", Some(subscription.url.value.toString)) ++
          CodecHelpers.extensionAttributes(subscription.extensions)
      Xml.Element(CodecHelpers.qname("Subscription"), attributes, Vector.empty)
end SubscriptionCodec

object ResourceQuParamsCodec:
  private val resourceDetails: Lexical.Lex[ResourceDetails] =
    Lexical.enumOf(ResourceDetails.values.toVector, _.toString)

  val decoder: XmlDecoder[ResourceQuParams] =
    XmlDecoder.instance: element =>
      for
        scope <- XmlDecoders.requiredAttribute("Scope")(Lexical.scope).decode(element)
        externalId <- XmlDecoders.attributeOf("ExternalID")(Lexical.nmtoken).decode(element)
        jobId <- XmlDecoders.attributeOf("JobID")(Lexical.nmtoken).decode(element)
        jobPartId <- XmlDecoders.attributeOf("JobPartID")(Lexical.nmtoken).decode(element)
        queueEntryId <- XmlDecoders.attributeOf("QueueEntryID")(Lexical.nmtoken).decode(element)
        details <- XmlDecoders.attributeOf("ResourceDetails")(resourceDetails).decode(element)
        resourceName <- XmlDecoders.attributeOf("ResourceName")(Lexical.nmtoken).decode(element)
        types <- XmlDecoders.attributeOf("Types")(Lexical.nmtokens).decode(element)
        parts <- XmlDecoders.repeatedChild("Part")(PartCodec.decoder).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("Part")).decode(element)
      yield ResourceQuParams(
        scope,
        externalId,
        jobId,
        jobPartId,
        queueEntryId,
        details,
        resourceName,
        types.getOrElse(Vector.empty),
        parts,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[ResourceQuParams] =
    XmlEncoder.instance: params =>
      val attributes =
        CodecHelpers.attributeOf("ExternalID", params.externalId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("JobID", params.jobId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("JobPartID", params.jobPartId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("QueueEntryID", params.queueEntryId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("ResourceDetails", params.details, _.toString) ++
          CodecHelpers.attributeOf("ResourceName", params.resourceName, (v: Nmtoken) => v.value) ++
          CodecHelpers.attribute("Scope", Some(params.scope.toString)) ++
          CodecHelpers.attribute(
            "Types",
            Option.when(params.types.nonEmpty)(CodecHelpers.renderNmtokens(params.types)),
          ) ++
          CodecHelpers.extensionAttributes(params.extensions)
      Xml.Element(
        CodecHelpers.qname("ResourceQuParams"),
        attributes,
        params.parts.map(PartCodec.encoder.encode),
      )
end ResourceQuParamsCodec

object QueryResourceCodec:
  val decoder: XmlDecoder[QueryResource] =
    XmlDecoder.instance: element =>
      for
        header <- XmlDecoders.singleChild("Header")(HeaderCodec.decoder).decode(element)
        params <- XmlDecoders.singleChild("ResourceQuParams")(ResourceQuParamsCodec.decoder).decode(element)
        subscription <- XmlDecoders.optionalChild("Subscription")(SubscriptionCodec.decoder).decode(element)
        languages <- XmlDecoders.attributeOf("Languages")(Lexical.languages).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("Header", "ResourceQuParams", "Subscription")).decode(element)
      yield QueryResource(
        header,
        params,
        languages.getOrElse(Vector.empty),
        subscription,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[QueryResource] =
    XmlEncoder.instance: query =>
      val attributes =
        CodecHelpers.attribute(
          "Languages",
        Option.when(query.languages.nonEmpty)(CodecHelpers.renderLanguages(query.languages)),
      ) ++ CodecHelpers.extensionAttributes(query.extensions)
      val children =
        Vector(HeaderCodec.encoder.encode(query.header), ResourceQuParamsCodec.encoder.encode(query.params)) ++
          query.subscription.toVector.map(SubscriptionCodec.encoder.encode)
      Xml.Element(CodecHelpers.qname("QueryResource"), attributes, children)
end QueryResourceCodec

object ResourceInfoCodec:
  private val commandResultLex: Lexical.Lex[CommandResult] =
    Lexical.enumOf(CommandResult.values.toVector, _.toString)
  private val resourceLevelLex: Lexical.Lex[ResourceLevel] =
    Lexical.enumOf(ResourceLevel.values.toVector, _.toString)

  val decoder: XmlDecoder[ResourceInfo] =
    XmlDecoder.instance: element =>
      for
        resourceSet <- XmlDecoders.singleChild("ResourceSet")(ResourceSetCodec.decoder).decode(element)
        commandResult <- XmlDecoders.attributeOf("CommandResult")(commandResultLex).decode(element)
        jobId <- XmlDecoders.attributeOf("JobID")(Lexical.nmtoken).decode(element)
        jobPartId <- XmlDecoders.attributeOf("JobPartID")(Lexical.nmtoken).decode(element)
        level <- XmlDecoders.attributeOf("Level")(resourceLevelLex).decode(element)
        moduleId <- XmlDecoders.attributeOf("ModuleID")(Lexical.nmtoken).decode(element)
        queueEntryId <- XmlDecoders.attributeOf("QueueEntryID")(Lexical.nmtoken).decode(element)
        scope <- XmlDecoders.attributeOf("Scope")(Lexical.scope).decode(element)
        speed <- XmlDecoders.attributeOf("Speed")(Lexical.float).decode(element)
        types <- XmlDecoders.attributeOf("Types")(Lexical.nmtokens).decode(element)
        totalAmount <- XmlDecoders.attributeOf("TotalAmount")(Lexical.float).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("ResourceSet")).decode(element)
      yield ResourceInfo(
        resourceSet,
        Vector.empty,
        None,
        commandResult,
        jobId,
        jobPartId,
        level,
        moduleId,
        queueEntryId,
        scope,
        speed,
        types.getOrElse(Vector.empty),
        totalAmount,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[ResourceInfo] =
    XmlEncoder.instance: info =>
      val attributes =
        CodecHelpers.attributeOf("CommandResult", info.commandResult, _.toString) ++
          CodecHelpers.attributeOf("JobID", info.jobId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("JobPartID", info.jobPartId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Level", info.level, _.toString) ++
          CodecHelpers.attributeOf("ModuleID", info.moduleId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("QueueEntryID", info.queueEntryId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Scope", info.scope, _.toString) ++
          CodecHelpers.attributeOf("Speed", info.speed, CodecHelpers.renderFloat) ++
          CodecHelpers.attribute(
            "Types",
        Option.when(info.types.nonEmpty)(CodecHelpers.renderNmtokens(info.types)),
      ) ++
          CodecHelpers.attributeOf("TotalAmount", info.totalAmount, CodecHelpers.renderFloat) ++
          CodecHelpers.extensionAttributes(info.extensions)
      Xml.Element(
        CodecHelpers.qname("ResourceInfo"),
        attributes,
        Vector(ResourceSetCodec.encoder.encode(info.resourceSet)),
      )
end ResourceInfoCodec

object ResponseResourceCodec:
  val decoder: XmlDecoder[ResponseResource] =
    XmlDecoder.instance: element =>
      for
        header <- XmlDecoders.singleChild("Header")(HeaderCodec.decoder).decode(element)
        returnCode <- XmlDecoders.attributeOf("ReturnCode")(Lexical.int).decode(element)
        notification <- XmlDecoders.optionalChild("Notification")(summon[XmlElementCodec[Notification]]).decode(element)
        resourceInfo <- XmlDecoders.repeatedChild("ResourceInfo")(ResourceInfoCodec.decoder).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("Header", "Notification", "ResourceInfo")).decode(element)
      yield ResponseResource(header, resourceInfo, returnCode, notification, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[ResponseResource] =
    XmlEncoder.instance: response =>
      val attributes =
        CodecHelpers.attributeOf("ReturnCode", response.returnCode, CodecHelpers.renderInt) ++
          CodecHelpers.extensionAttributes(response.extensions)
      val children =
        Vector(HeaderCodec.encoder.encode(response.header)) ++
          response.notification.toVector.map(summon[XmlElementCodec[Notification]].encode) ++
          response.resourceInfo.map(ResourceInfoCodec.encoder.encode)
      Xml.Element(CodecHelpers.qname("ResponseResource"), attributes, children)
end ResponseResourceCodec

object SignalResourceCodec:
  private val channelMode: Lexical.Lex[ChannelMode] =
    Lexical.enumOf(ChannelMode.values.toVector, _.toString)

  val decoder: XmlDecoder[SignalResource] =
    XmlDecoder.instance: element =>
      for
        header <- XmlDecoders.singleChild("Header")(HeaderCodec.decoder).decode(element)
        resourceInfo <- XmlDecoders.repeatedChild("ResourceInfo")(ResourceInfoCodec.decoder).decode(element)
        channelMode <- XmlDecoders.attributeOf("ChannelMode")(channelMode).decode(element)
        replaceAfter <- XmlDecoders.attributeOf("ReplaceAfter")(Lexical.dateTime).decode(element)
        replaceBefore <- XmlDecoders.attributeOf("ReplaceBefore")(Lexical.dateTime).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("Header", "ResourceInfo")).decode(element)
      yield SignalResource(
        header,
        resourceInfo,
        replaceAfter,
        replaceBefore,
        channelMode,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[SignalResource] =
    XmlEncoder.instance: signal =>
      val attributes =
        CodecHelpers.attributeOf("ChannelMode", signal.channelMode, _.toString) ++
          CodecHelpers.attributeOf("ReplaceAfter", signal.replaceAfter, (v: XsdDateTime) => v.value) ++
          CodecHelpers.attributeOf("ReplaceBefore", signal.replaceBefore, (v: XsdDateTime) => v.value) ++
          CodecHelpers.extensionAttributes(signal.extensions)
      val children =
        Vector(HeaderCodec.encoder.encode(signal.header)) ++
          signal.resourceInfo.map(ResourceInfoCodec.encoder.encode)
      Xml.Element(CodecHelpers.qname("SignalResource"), attributes, children)
end SignalResourceCodec

object QueryKnownMessagesCodec:
  val decoder: XmlDecoder[QueryKnownMessages] =
    XmlDecoder.instance: element =>
      for
        header <- XmlDecoders.singleChild("Header")(HeaderCodec.decoder).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("Header")).decode(element)
      yield QueryKnownMessages(header, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[QueryKnownMessages] =
    XmlEncoder.instance: query =>
      Xml.Element(
        CodecHelpers.qname("QueryKnownMessages"),
        CodecHelpers.extensionAttributes(query.extensions),
        Vector(HeaderCodec.encoder.encode(query.header)),
      )
end QueryKnownMessagesCodec

object MessageServiceCodec:
  private val responseMode: Lexical.Lex[MessageResponseMode] =
    Lexical.enumOf(MessageResponseMode.values.toVector, _.toString)
  private val urlScheme: Lexical.Lex[MessageUrlScheme] =
    Lexical.enumOf(MessageUrlScheme.values.toVector, _.lexical)

  val decoder: XmlDecoder[MessageService] =
    XmlDecoder.instance: element =>
      for
        messageType <- XmlDecoders.requiredAttribute("Type")(Lexical.nmtoken).decode(element)
        responseModes <- XmlDecoders.attributeOf("ResponseModes")(Lexical.list(responseMode)).decode(element)
        urlSchemes <- XmlDecoders.attributeOf("URLSchemes")(Lexical.list(urlScheme)).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
      yield MessageService(
        messageType,
        responseModes.getOrElse(Vector.empty),
        urlSchemes.getOrElse(Vector.empty),
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[MessageService] =
    XmlEncoder.instance: service =>
      val attributes =
        CodecHelpers.attribute(
          "ResponseModes",
        Option.when(service.responseModes.nonEmpty)(service.responseModes.map(_.toString).mkString(" ")),
      ) ++
          CodecHelpers.attribute("Type", Some(service.messageType.value)) ++
          CodecHelpers.attribute(
            "URLSchemes",
        Option.when(service.urlSchemes.nonEmpty)(service.urlSchemes.map(_.lexical).mkString(" ")),
      ) ++
          CodecHelpers.extensionAttributes(service.extensions)
      Xml.Element(CodecHelpers.qname("MessageService"), attributes, Vector.empty)
end MessageServiceCodec

object ResponseKnownMessagesCodec:
  val decoder: XmlDecoder[ResponseKnownMessages] =
    XmlDecoder.instance: element =>
      for
        header <- XmlDecoders.singleChild("Header")(HeaderCodec.decoder).decode(element)
        returnCode <- XmlDecoders.attributeOf("ReturnCode")(Lexical.int).decode(element)
        notification <- XmlDecoders.optionalChild("Notification")(summon[XmlElementCodec[Notification]]).decode(element)
        services <- XmlDecoders.repeatedChild("MessageService")(MessageServiceCodec.decoder).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("Header", "Notification", "MessageService")).decode(element)
      yield ResponseKnownMessages(header, services, returnCode, notification, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[ResponseKnownMessages] =
    XmlEncoder.instance: response =>
      val attributes =
        CodecHelpers.attributeOf("ReturnCode", response.returnCode, CodecHelpers.renderInt) ++
          CodecHelpers.extensionAttributes(response.extensions)
      val children =
        Vector(HeaderCodec.encoder.encode(response.header)) ++
          response.notification.toVector.map(summon[XmlElementCodec[Notification]].encode) ++
          response.services.map(MessageServiceCodec.encoder.encode)
      Xml.Element(CodecHelpers.qname("ResponseKnownMessages"), attributes, children)
end ResponseKnownMessagesCodec




object XjmfCodec:
  val decoder: XmlDecoder[XJMF] =
    XmlDecoder.instance: element =>
      for
        header <- XmlDecoders.singleChild("Header")(HeaderCodec.decoder).decode(element)
        version <- XmlDecoders.attributeOf("Version")(Lexical.version).decode(element)
        messages <- element.childElements
          .filter(_.name.localName != "Header")
          .foldLeft[Either[XmlError, Vector[Message]]](Right(Vector.empty)) { (acc, child) =>
            for
              values <- acc
              message <- Registry.decodeMessage(child)
            yield values :+ message
          }
        nonEmpty <- NonEmptyVector.from(messages) match
          case Right(nonEmpty) => Right(nonEmpty)
          case Left(_)         => Left(XmlError.MissingElement("XJMF", "Message"))
      yield XJMF(header, nonEmpty, version, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[XJMF] =
    XmlEncoder.instance: xjmf =>
      val attributes =
        CodecHelpers.attributeOf("Version", xjmf.version, (v: Version) => v.lexical) ++
          CodecHelpers.extensionAttributes(xjmf.extensions)
      val children =
        Vector(HeaderCodec.encoder.encode(xjmf.header)) ++ xjmf.messages.toVector.map(Registry.encodeMessage)
      Xml.Element(CodecHelpers.qname("XJMF"), attributes, children)
end XjmfCodec
