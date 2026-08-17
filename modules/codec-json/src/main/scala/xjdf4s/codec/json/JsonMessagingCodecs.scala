package xjdf4s.codec.json

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.*

import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*

/**
 * JSON codecs for the XJMF surface. `XJMF` carries the normative JSON exception: exactly one message, in-lined as
 * a member named by the message element.
 */
object JsonMessagingCodecs:

  given Encoder[Header] = Encoder.instance(header =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("AgentName", header.agentName),
        JsonCodec.optMember("AgentVersion", header.agentVersion),
        JsonCodec.optMember("Author", header.author),
        JsonCodec.optMember("DescriptiveName", header.descriptiveName),
        Vector(JsonCodec.member("DeviceID", Json.fromString(header.deviceId.value))),
        JsonCodec.vecMember("ICSVersions", header.icsVersions),
        JsonCodec.optMember("ID", header.id),
        JsonCodec.optMember("PersonalID", header.personalId),
        JsonCodec.optMember("refID", header.refId),
        Vector(JsonCodec.member("Time", Json.fromString(header.time.value))),
      ),
    ),
  )
  given Decoder[Header] = Decoder.instance(cursor =>
    for
      deviceId <- cursor.get[Nmtoken]("DeviceID")
      time <- cursor.get[XsdDateTime]("Time")
      agentName <- JsonCodec.opt[XjdfString](cursor, "AgentName")
      agentVersion <- JsonCodec.opt[XjdfString](cursor, "AgentVersion")
      author <- JsonCodec.opt[XjdfString](cursor, "Author")
      descriptiveName <- JsonCodec.opt[XjdfString](cursor, "DescriptiveName")
      icsVersions <- JsonCodec.vec[Nmtoken](cursor, "ICSVersions")
      id <- JsonCodec.opt[XsdId](cursor, "ID")
      personalId <- JsonCodec.opt[Nmtoken](cursor, "PersonalID")
      refId <- JsonCodec.opt[Nmtoken](cursor, "refID")
    yield Header(deviceId, time, agentName, agentVersion, author, descriptiveName, icsVersions, id, personalId, refId),
  )

  given Encoder[Subscription] = Encoder.instance(subscription =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.vecMember("ChannelMode", subscription.channelMode),
        JsonCodec.vecMember("Languages", subscription.languages),
        JsonCodec.optMember("RepeatTime", subscription.repeatTime),
        Vector(JsonCodec.member("URL", Json.fromString(subscription.url.value.toString))),
      ),
    ),
  )
  given Decoder[Subscription] = Decoder.instance(cursor =>
    for
      url <- cursor.get[UriRef]("URL")
      channelMode <- JsonCodec.vec[ChannelMode](cursor, "ChannelMode")
      languages <- JsonCodec.vec[LanguageTag](cursor, "Languages")
      repeatTime <- JsonCodec.opt[Float](cursor, "RepeatTime")
    yield Subscription(url, channelMode, languages, repeatTime),
  )

  given Encoder[ResourceQuParams] = Encoder.instance(params =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("ExternalID", params.externalId),
        JsonCodec.optMember("JobID", params.jobId),
        JsonCodec.optMember("JobPartID", params.jobPartId),
        JsonCodec.optMember("QueueEntryID", params.queueEntryId),
        JsonCodec.optMember("ResourceDetails", params.details),
        JsonCodec.optMember("ResourceName", params.resourceName),
        Vector(JsonCodec.member("Scope", Json.fromString(params.scope.toString))),
        JsonCodec.vecMember("Types", params.types),
        JsonCodec.vecMember("Part", params.parts),
      ),
    ),
  )
  given Decoder[ResourceQuParams] = Decoder.instance(cursor =>
    for
      scope <- cursor.get[Scope]("Scope")
      externalId <- JsonCodec.opt[Nmtoken](cursor, "ExternalID")
      jobId <- JsonCodec.opt[Nmtoken](cursor, "JobID")
      jobPartId <- JsonCodec.opt[Nmtoken](cursor, "JobPartID")
      queueEntryId <- JsonCodec.opt[Nmtoken](cursor, "QueueEntryID")
      details <- JsonCodec.opt[ResourceDetails](cursor, "ResourceDetails")
      resourceName <- JsonCodec.opt[Nmtoken](cursor, "ResourceName")
      types <- JsonCodec.vec[Nmtoken](cursor, "Types")
      parts <- JsonCodec.vec[Part](cursor, "Part")
    yield ResourceQuParams(scope, externalId, jobId, jobPartId, queueEntryId, details, resourceName, types, parts),
  )

  given Encoder[ResourceInfo] = Encoder.instance(info =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("CommandResult", info.commandResult),
        JsonCodec.optMember("JobID", info.jobId),
        JsonCodec.optMember("JobPartID", info.jobPartId),
        JsonCodec.optMember("Level", info.level),
        JsonCodec.optMember("ModuleID", info.moduleId),
        JsonCodec.optMember("QueueEntryID", info.queueEntryId),
        JsonCodec.optMember("Scope", info.scope),
        JsonCodec.optMember("Speed", info.speed),
        JsonCodec.vecMember("Types", info.types),
        JsonCodec.optMember("TotalAmount", info.totalAmount),
        Vector(JsonCodec.member("ResourceSet", info.resourceSet.asJson)),
      ),
    ),
  )
  given Decoder[ResourceInfo] = Decoder.instance(cursor =>
    for
      resourceSet <- cursor.get[ResourceSet]("ResourceSet")
      commandResult <- JsonCodec.opt[CommandResult](cursor, "CommandResult")
      jobId <- JsonCodec.opt[Nmtoken](cursor, "JobID")
      jobPartId <- JsonCodec.opt[Nmtoken](cursor, "JobPartID")
      level <- JsonCodec.opt[ResourceLevel](cursor, "Level")
      moduleId <- JsonCodec.opt[Nmtoken](cursor, "ModuleID")
      queueEntryId <- JsonCodec.opt[Nmtoken](cursor, "QueueEntryID")
      scope <- JsonCodec.opt[Scope](cursor, "Scope")
      speed <- JsonCodec.opt[Float](cursor, "Speed")
      types <- JsonCodec.vec[Nmtoken](cursor, "Types")
      totalAmount <- JsonCodec.opt[Float](cursor, "TotalAmount")
    yield ResourceInfo(resourceSet, Vector.empty, None, commandResult, jobId, jobPartId, level, moduleId, queueEntryId, scope, speed, types, totalAmount),
  )

  given Encoder[DeviceInfo] = Encoder.instance(info =>
    if info.activities.nonEmpty || info.events.nonEmpty || info.jobPhases.nonEmpty ||
        info.schemas.current.nonEmpty || info.schemas.global.nonEmpty
    then
      throw new UnsupportedOperationException("DeviceInfo children are not covered by the JSON codec slice yet")
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("CounterUnit", info.counterUnit),
        JsonCodec.optMember("EndTime", info.endTime),
        JsonCodec.optMember("HourCounter", info.hourCounter),
        JsonCodec.optMember("IdleStartTime", info.idleStartTime),
        JsonCodec.vecMember("ModuleIDs", info.moduleIds),
        JsonCodec.optMember("PowerOnTime", info.powerOnTime),
        JsonCodec.optMember("ProductionCounter", info.productionCounter),
        JsonCodec.optMember("Speed", info.speed),
        Vector(JsonCodec.member("Status", Json.fromString(info.status.toString))),
        JsonCodec.optMember("StatusDetails", info.statusDetails),
        JsonCodec.vecMember("ToolIDs", info.toolIds),
        JsonCodec.optMember("TotalProductionCounter", info.totalProductionCounter),
      ),
    ),
  )
  given Decoder[DeviceInfo] = Decoder.instance(cursor =>
    for
      status <- cursor.get[DeviceStatus]("Status")
      counterUnit <- JsonCodec.opt[Nmtoken](cursor, "CounterUnit")
      endTime <- JsonCodec.opt[XsdDateTime](cursor, "EndTime")
      hourCounter <- JsonCodec.opt[XsdDuration](cursor, "HourCounter")
      idleStartTime <- JsonCodec.opt[XsdDateTime](cursor, "IdleStartTime")
      moduleIds <- JsonCodec.vec[Nmtoken](cursor, "ModuleIDs")
      powerOnTime <- JsonCodec.opt[XsdDateTime](cursor, "PowerOnTime")
      productionCounter <- JsonCodec.opt[Float](cursor, "ProductionCounter")
      speed <- JsonCodec.opt[Float](cursor, "Speed")
      statusDetails <- JsonCodec.opt[Nmtoken](cursor, "StatusDetails")
      toolIds <- JsonCodec.vec[Nmtoken](cursor, "ToolIDs")
      totalProductionCounter <- JsonCodec.opt[Float](cursor, "TotalProductionCounter")
    yield DeviceInfo(
      status,
      counterUnit,
      endTime,
      hourCounter,
      idleStartTime,
      moduleIds,
      powerOnTime,
      productionCounter,
      speed,
      statusDetails,
      toolIds,
      totalProductionCounter,
      Vector.empty,
      Vector.empty,
      DeviceInfoSchemas(),
      Vector.empty,
    ),
  )

  given Encoder[Notification] = Encoder.instance(notification =>
    if notification.event.nonEmpty || notification.milestone.nonEmpty || notification.parts.nonEmpty then
      throw new UnsupportedOperationException("Notification children are not covered by the JSON codec slice yet")
    JsonCodec.obj(
      JsonCodec.memberList(
        Vector(JsonCodec.member("Class", Json.fromString(notification.severity.toString))),
        JsonCodec.vecMember("Comment", notification.comments),
        JsonCodec.optMember("JobID", notification.jobId),
        JsonCodec.optMember("JobPartID", notification.jobPartId),
        JsonCodec.optMember("ModuleID", notification.moduleId),
        JsonCodec.optMember("QueueEntryID", notification.queueEntryId),
      ),
    ),
  )
  given Decoder[Notification] = Decoder.instance(cursor =>
    for
      severity <- cursor.get[Severity]("Class")
      comments <- JsonCodec.vec[Comment](cursor, "Comment")
      jobId <- JsonCodec.opt[Nmtoken](cursor, "JobID")
      jobPartId <- JsonCodec.opt[Nmtoken](cursor, "JobPartID")
      moduleId <- JsonCodec.opt[Nmtoken](cursor, "ModuleID")
      queueEntryId <- JsonCodec.opt[Nmtoken](cursor, "QueueEntryID")
    yield Notification(severity, comments, None, None, Vector.empty, Vector.empty, jobId, jobPartId, moduleId, queueEntryId),
  )

  given Encoder[MessageService] = Encoder.instance(service =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.vecMember("ResponseModes", service.responseModes),
        Vector(JsonCodec.member("Type", Json.fromString(service.messageType.value))),
        JsonCodec.vecMember("URLSchemes", service.urlSchemes),
      ),
    ),
  )
  given Decoder[MessageService] = Decoder.instance(cursor =>
    for
      messageType <- cursor.get[Nmtoken]("Type")
      responseModes <- JsonCodec.vec[MessageResponseMode](cursor, "ResponseModes")
      urlSchemes <- JsonCodec.vec[MessageUrlScheme](cursor, "URLSchemes")
    yield MessageService(messageType, responseModes, urlSchemes),
  )

  // -- concrete messages ---------------------------------------------------------

  given Encoder[QueryKnownMessages] =
    Encoder.instance(query => Json.obj(JsonCodec.member("Header", query.header.asJson)))
  given Decoder[QueryKnownMessages] = Decoder.instance(cursor =>
    cursor.get[Header]("Header").map(QueryKnownMessages(_)),
  )

  given Encoder[QueryResource] = Encoder.instance(query =>
    JsonCodec.obj(
      JsonCodec.memberList(
        Vector(JsonCodec.member("Header", query.header.asJson)),
        JsonCodec.vecMember("Languages", query.languages),
        query.subscription.toVector.map(subscription => JsonCodec.member("Subscription", subscription.asJson)),
        Vector(JsonCodec.member("ResourceQuParams", query.params.asJson)),
      ),
    ),
  )
  given Decoder[QueryResource] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      params <- cursor.get[ResourceQuParams]("ResourceQuParams")
      subscription <- JsonCodec.opt[Subscription](cursor, "Subscription")
      languages <- JsonCodec.vec[LanguageTag](cursor, "Languages")
    yield QueryResource(header, params, languages, subscription),
  )

  given Encoder[ResponseKnownMessages] = Encoder.instance(response =>
    JsonCodec.obj(
      JsonCodec.memberList(
        Vector(JsonCodec.member("Header", response.header.asJson)),
        JsonCodec.vecMember("MessageService", response.services),
        JsonCodec.optMember("ReturnCode", response.returnCode),
        response.notification.toVector.map(notification => JsonCodec.member("Notification", notification.asJson)),
      ),
    ),
  )
  given Decoder[ResponseKnownMessages] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      services <- JsonCodec.vec[MessageService](cursor, "MessageService")
      returnCode <- JsonCodec.opt[Int](cursor, "ReturnCode")
      notification <- JsonCodec.opt[Notification](cursor, "Notification")
    yield ResponseKnownMessages(header, services, returnCode, notification),
  )

  given Encoder[ResponseResource] = Encoder.instance(response =>
    JsonCodec.obj(
      JsonCodec.memberList(
        Vector(JsonCodec.member("Header", response.header.asJson)),
        JsonCodec.vecMember("ResourceInfo", response.resourceInfo),
        JsonCodec.optMember("ReturnCode", response.returnCode),
        response.notification.toVector.map(notification => JsonCodec.member("Notification", notification.asJson)),
      ),
    ),
  )
  given Decoder[ResponseResource] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      resourceInfo <- JsonCodec.vec[ResourceInfo](cursor, "ResourceInfo")
      returnCode <- JsonCodec.opt[Int](cursor, "ReturnCode")
      notification <- JsonCodec.opt[Notification](cursor, "Notification")
    yield ResponseResource(header, resourceInfo, returnCode, notification),
  )

  given Encoder[SignalResource] = Encoder.instance(signal =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("ChannelMode", signal.channelMode),
        Vector(JsonCodec.member("Header", signal.header.asJson)),
        JsonCodec.optMember("ReplaceAfter", signal.replaceAfter),
        JsonCodec.optMember("ReplaceBefore", signal.replaceBefore),
        JsonCodec.vecMember("ResourceInfo", signal.resourceInfo),
      ),
    ),
  )
  given Decoder[SignalResource] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      resourceInfo <- JsonCodec.vec[ResourceInfo](cursor, "ResourceInfo")
      channelMode <- JsonCodec.opt[ChannelMode](cursor, "ChannelMode")
      replaceAfter <- JsonCodec.opt[XsdDateTime](cursor, "ReplaceAfter")
      replaceBefore <- JsonCodec.opt[XsdDateTime](cursor, "ReplaceBefore")
    yield SignalResource(header, resourceInfo, replaceAfter, replaceBefore, channelMode),
  )

  given Encoder[SignalNotification] = Encoder.instance(signal =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("ChannelMode", signal.channelMode),
        Vector(JsonCodec.member("Header", signal.header.asJson)),
        Vector(JsonCodec.member("Notification", signal.notification.asJson)),
      ),
    ),
  )
  given Decoder[SignalNotification] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      notification <- cursor.get[Notification]("Notification")
      channelMode <- JsonCodec.opt[ChannelMode](cursor, "ChannelMode")
    yield SignalNotification(header, notification, channelMode),
  )

  given Encoder[SignalStatus] = Encoder.instance(signal =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("ChannelMode", signal.channelMode),
        Vector(JsonCodec.member("Header", signal.header.asJson)),
        Vector(JsonCodec.member("DeviceInfo", signal.deviceInfo.asJson)),
        signal.replacement.toVector.map(window =>
          JsonCodec.member("ReplaceAfter", Json.fromString(window.after.value)),
        ),
        signal.replacement.toVector.map(window =>
          JsonCodec.member("ReplaceBefore", Json.fromString(window.before.value)),
        ),
      ),
    ),
  )
  given Decoder[SignalStatus] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      deviceInfo <- cursor.get[DeviceInfo]("DeviceInfo")
      replaceAfter <- JsonCodec.opt[XsdDateTime](cursor, "ReplaceAfter")
      replaceBefore <- JsonCodec.opt[XsdDateTime](cursor, "ReplaceBefore")
      channelMode <- JsonCodec.opt[ChannelMode](cursor, "ChannelMode")
      replacement <- (replaceAfter, replaceBefore) match
        case (Some(after), Some(before)) => Right(Some(StatusReplacementWindow(after, before)))
        case (None, None)                => Right(None)
        case _                           => JsonCodec.fail(cursor, "both ReplaceAfter and ReplaceBefore or neither")
    yield SignalStatus(header, deviceInfo, replacement, channelMode),
  )

  // -- XJMF envelope (JSON exception: exactly one message) ------------------------

  private val messageNames: Vector[String] = Vector(
    "QueryKnownMessages",
    "QueryResource",
    "ResponseKnownMessages",
    "ResponseResource",
    "SignalNotification",
    "SignalResource",
    "SignalStatus",
  )

  private def decodeMessage(name: String, json: Json): Decoder.Result[Message] =
    name match
      case "QueryKnownMessages"    => json.as[QueryKnownMessages].map(identity)
      case "QueryResource"         => json.as[QueryResource].map(identity)
      case "ResponseKnownMessages" => json.as[ResponseKnownMessages].map(identity)
      case "ResponseResource"      => json.as[ResponseResource].map(identity)
      case "SignalNotification"    => json.as[SignalNotification].map(identity)
      case "SignalResource"        => json.as[SignalResource].map(identity)
      case "SignalStatus"          => json.as[SignalStatus].map(identity)
      case other                   => JsonCodec.fail(json.hcursor, s"message '$other' is not covered by the JSON slice")

  private def encodeMessage(message: Message): Json =
    message match
      case query: QueryKnownMessages    => query.asJson
      case query: QueryResource         => query.asJson
      case response: ResponseKnownMessages => response.asJson
      case response: ResponseResource   => response.asJson
      case signal: SignalNotification   => signal.asJson
      case signal: SignalResource       => signal.asJson
      case signal: SignalStatus         => signal.asJson
      case other => throw new UnsupportedOperationException(s"no JSON codec for ${other.getClass.getName} in this slice")

  private def messageName(message: Message): String =
    message match
      case _: QueryKnownMessages    => "QueryKnownMessages"
      case _: QueryResource         => "QueryResource"
      case _: ResponseKnownMessages => "ResponseKnownMessages"
      case _: ResponseResource      => "ResponseResource"
      case _: SignalNotification    => "SignalNotification"
      case _: SignalResource        => "SignalResource"
      case _: SignalStatus          => "SignalStatus"
      case other => throw new UnsupportedOperationException(s"no JSON codec for ${other.getClass.getName} in this slice")

  given Encoder[XJMF] = Encoder.instance(xjmf =>
    val messages = xjmf.messages.toVector
    if messages.size != 1 then
      throw new UnsupportedOperationException(
        s"JSON XJMF requires exactly one message (normative JSON exception), got ${messages.size}",
      )
    JsonCodec.obj(
      JsonCodec.memberList(
        Vector(JsonCodec.member("Header", xjmf.header.asJson)),
        JsonCodec.optMember("Version", xjmf.version),
        Vector(JsonCodec.member(messageName(messages.head), encodeMessage(messages.head))),
        Vector(JsonCodec.rootName("XJMF")),
      ),
    ),
  )
  given Decoder[XJMF] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      version <- JsonCodec.opt[Version](cursor, "Version")
      messageMembers <- messageNames.foldLeft[Decoder.Result[Vector[Message]]](Right(Vector.empty)) { (acc, name) =>
        for
          accumulated <- acc
          next <- cursor.downField(name).focus match
            case Some(json) => decodeMessage(name, json).map(accumulated :+ _)
            case None       => Right(accumulated)
        yield next
      }
      single <- messageMembers match
        case Vector(one) => Right(one)
        case other => JsonCodec.fail(cursor, s"JSON XJMF requires exactly one message, found ${other.size}")
    yield XJMF(header, NonEmptyVector.one(single), version),
  )
end JsonMessagingCodecs
