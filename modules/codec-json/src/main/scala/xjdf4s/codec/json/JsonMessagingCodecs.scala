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
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("AgentName", header.agentName),
        JsonHelpers.optMember("AgentVersion", header.agentVersion),
        JsonHelpers.optMember("Author", header.author),
        JsonHelpers.optMember("DescriptiveName", header.descriptiveName),
        Vector(JsonHelpers.member("DeviceID", Json.fromString(header.deviceId.value))),
        JsonHelpers.vecMember("ICSVersions", header.icsVersions),
        JsonHelpers.optMember("ID", header.id),
        JsonHelpers.optMember("PersonalID", header.personalId),
        JsonHelpers.optMember("refID", header.refId),
        Vector(JsonHelpers.member("Time", Json.fromString(header.time.value))),
      ),
    ),
  )
  given Decoder[Header] = Decoder.instance(cursor =>
    for
      deviceId <- cursor.get[Nmtoken]("DeviceID")
      time <- cursor.get[XsdDateTime]("Time")
      agentName <- JsonHelpers.opt[XjdfString](cursor, "AgentName")
      agentVersion <- JsonHelpers.opt[XjdfString](cursor, "AgentVersion")
      author <- JsonHelpers.opt[XjdfString](cursor, "Author")
      descriptiveName <- JsonHelpers.opt[XjdfString](cursor, "DescriptiveName")
      icsVersions <- JsonHelpers.vec[Nmtoken](cursor, "ICSVersions")
      id <- JsonHelpers.opt[XsdId](cursor, "ID")
      personalId <- JsonHelpers.opt[Nmtoken](cursor, "PersonalID")
      refId <- JsonHelpers.opt[Nmtoken](cursor, "refID")
    yield Header(deviceId, time, agentName, agentVersion, author, descriptiveName, icsVersions, id, personalId, refId),
  )

  given Encoder[Subscription] = Encoder.instance(subscription =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.vecMember("ChannelMode", subscription.channelMode),
        JsonHelpers.vecMember("Languages", subscription.languages),
        JsonHelpers.optMember("RepeatTime", subscription.repeatTime),
        Vector(JsonHelpers.member("URL", Json.fromString(subscription.url.value.toString))),
      ),
    ),
  )
  given Decoder[Subscription] = Decoder.instance(cursor =>
    for
      url <- cursor.get[UriRef]("URL")
      channelMode <- JsonHelpers.vec[ChannelMode](cursor, "ChannelMode")
      languages <- JsonHelpers.vec[LanguageTag](cursor, "Languages")
      repeatTime <- JsonHelpers.opt[Float](cursor, "RepeatTime")
    yield Subscription(url, channelMode, languages, repeatTime),
  )

  given Encoder[ResourceQuParams] = Encoder.instance(params =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("ExternalID", params.externalId),
        JsonHelpers.optMember("JobID", params.jobId),
        JsonHelpers.optMember("JobPartID", params.jobPartId),
        JsonHelpers.optMember("QueueEntryID", params.queueEntryId),
        JsonHelpers.optMember("ResourceDetails", params.details),
        JsonHelpers.optMember("ResourceName", params.resourceName),
        Vector(JsonHelpers.member("Scope", Json.fromString(params.scope.toString))),
        JsonHelpers.vecMember("Types", params.types),
        JsonHelpers.vecMember("Part", params.parts),
      ),
    ),
  )
  given Decoder[ResourceQuParams] = Decoder.instance(cursor =>
    for
      scope <- cursor.get[Scope]("Scope")
      externalId <- JsonHelpers.opt[Nmtoken](cursor, "ExternalID")
      jobId <- JsonHelpers.opt[Nmtoken](cursor, "JobID")
      jobPartId <- JsonHelpers.opt[Nmtoken](cursor, "JobPartID")
      queueEntryId <- JsonHelpers.opt[Nmtoken](cursor, "QueueEntryID")
      details <- JsonHelpers.opt[ResourceDetails](cursor, "ResourceDetails")
      resourceName <- JsonHelpers.opt[Nmtoken](cursor, "ResourceName")
      types <- JsonHelpers.vec[Nmtoken](cursor, "Types")
      parts <- JsonHelpers.vec[Part](cursor, "Part")
    yield ResourceQuParams(scope, externalId, jobId, jobPartId, queueEntryId, details, resourceName, types, parts),
  )

  given Encoder[ResourceInfo] = Encoder.instance(info =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("CommandResult", info.commandResult),
        JsonHelpers.optMember("JobID", info.jobId),
        JsonHelpers.optMember("JobPartID", info.jobPartId),
        JsonHelpers.optMember("Level", info.level),
        JsonHelpers.optMember("ModuleID", info.moduleId),
        JsonHelpers.optMember("QueueEntryID", info.queueEntryId),
        JsonHelpers.optMember("Scope", info.scope),
        JsonHelpers.optMember("Speed", info.speed),
        JsonHelpers.vecMember("Types", info.types),
        JsonHelpers.optMember("TotalAmount", info.totalAmount),
        Vector(JsonHelpers.member("ResourceSet", info.resourceSet.asJson)),
      ),
    ),
  )
  given Decoder[ResourceInfo] = Decoder.instance(cursor =>
    for
      resourceSet <- cursor.get[ResourceSet]("ResourceSet")
      commandResult <- JsonHelpers.opt[CommandResult](cursor, "CommandResult")
      jobId <- JsonHelpers.opt[Nmtoken](cursor, "JobID")
      jobPartId <- JsonHelpers.opt[Nmtoken](cursor, "JobPartID")
      level <- JsonHelpers.opt[ResourceLevel](cursor, "Level")
      moduleId <- JsonHelpers.opt[Nmtoken](cursor, "ModuleID")
      queueEntryId <- JsonHelpers.opt[Nmtoken](cursor, "QueueEntryID")
      scope <- JsonHelpers.opt[Scope](cursor, "Scope")
      speed <- JsonHelpers.opt[Float](cursor, "Speed")
      types <- JsonHelpers.vec[Nmtoken](cursor, "Types")
      totalAmount <- JsonHelpers.opt[Float](cursor, "TotalAmount")
    yield ResourceInfo(resourceSet, Vector.empty, None, commandResult, jobId, jobPartId, level, moduleId, queueEntryId, scope, speed, types, totalAmount),
  )

  given Encoder[DeviceInfo] = Encoder.instance(info =>
    if info.activities.nonEmpty || info.events.nonEmpty || info.jobPhases.nonEmpty ||
        info.schemas.current.nonEmpty || info.schemas.global.nonEmpty
    then
      throw new UnsupportedOperationException("DeviceInfo children are not covered by the JSON codec slice yet")
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("CounterUnit", info.counterUnit),
        JsonHelpers.optMember("EndTime", info.endTime),
        JsonHelpers.optMember("HourCounter", info.hourCounter),
        JsonHelpers.optMember("IdleStartTime", info.idleStartTime),
        JsonHelpers.vecMember("ModuleIDs", info.moduleIds),
        JsonHelpers.optMember("PowerOnTime", info.powerOnTime),
        JsonHelpers.optMember("ProductionCounter", info.productionCounter),
        JsonHelpers.optMember("Speed", info.speed),
        Vector(JsonHelpers.member("Status", Json.fromString(info.status.toString))),
        JsonHelpers.optMember("StatusDetails", info.statusDetails),
        JsonHelpers.vecMember("ToolIDs", info.toolIds),
        JsonHelpers.optMember("TotalProductionCounter", info.totalProductionCounter),
      ),
    ),
  )
  given Decoder[DeviceInfo] = Decoder.instance(cursor =>
    for
      status <- cursor.get[DeviceStatus]("Status")
      counterUnit <- JsonHelpers.opt[Nmtoken](cursor, "CounterUnit")
      endTime <- JsonHelpers.opt[XsdDateTime](cursor, "EndTime")
      hourCounter <- JsonHelpers.opt[XsdDuration](cursor, "HourCounter")
      idleStartTime <- JsonHelpers.opt[XsdDateTime](cursor, "IdleStartTime")
      moduleIds <- JsonHelpers.vec[Nmtoken](cursor, "ModuleIDs")
      powerOnTime <- JsonHelpers.opt[XsdDateTime](cursor, "PowerOnTime")
      productionCounter <- JsonHelpers.opt[Float](cursor, "ProductionCounter")
      speed <- JsonHelpers.opt[Float](cursor, "Speed")
      statusDetails <- JsonHelpers.opt[Nmtoken](cursor, "StatusDetails")
      toolIds <- JsonHelpers.vec[Nmtoken](cursor, "ToolIDs")
      totalProductionCounter <- JsonHelpers.opt[Float](cursor, "TotalProductionCounter")
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
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("Class", Json.fromString(notification.severity.toString))),
        JsonHelpers.vecMember("Comment", notification.comments),
        JsonHelpers.optMember("JobID", notification.jobId),
        JsonHelpers.optMember("JobPartID", notification.jobPartId),
        JsonHelpers.optMember("ModuleID", notification.moduleId),
        JsonHelpers.optMember("QueueEntryID", notification.queueEntryId),
      ),
    ),
  )
  given Decoder[Notification] = Decoder.instance(cursor =>
    for
      severity <- cursor.get[Severity]("Class")
      comments <- JsonHelpers.vec[Comment](cursor, "Comment")
      jobId <- JsonHelpers.opt[Nmtoken](cursor, "JobID")
      jobPartId <- JsonHelpers.opt[Nmtoken](cursor, "JobPartID")
      moduleId <- JsonHelpers.opt[Nmtoken](cursor, "ModuleID")
      queueEntryId <- JsonHelpers.opt[Nmtoken](cursor, "QueueEntryID")
    yield Notification(severity, comments, None, None, Vector.empty, Vector.empty, jobId, jobPartId, moduleId, queueEntryId),
  )

  given Encoder[MessageService] = Encoder.instance(service =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.vecMember("ResponseModes", service.responseModes),
        Vector(JsonHelpers.member("Type", Json.fromString(service.messageType.value))),
        JsonHelpers.vecMember("URLSchemes", service.urlSchemes),
      ),
    ),
  )
  given Decoder[MessageService] = Decoder.instance(cursor =>
    for
      messageType <- cursor.get[Nmtoken]("Type")
      responseModes <- JsonHelpers.vec[MessageResponseMode](cursor, "ResponseModes")
      urlSchemes <- JsonHelpers.vec[MessageUrlScheme](cursor, "URLSchemes")
    yield MessageService(messageType, responseModes, urlSchemes),
  )

  // -- concrete messages ---------------------------------------------------------

  given Encoder[QueryKnownMessages] =
    Encoder.instance(query => Json.obj(JsonHelpers.member("Header", query.header.asJson)))
  given Decoder[QueryKnownMessages] = Decoder.instance(cursor =>
    cursor.get[Header]("Header").map(QueryKnownMessages(_)),
  )

  given Encoder[QueryResource] = Encoder.instance(query =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("Header", query.header.asJson)),
        JsonHelpers.vecMember("Languages", query.languages),
        query.subscription.toVector.map(subscription => JsonHelpers.member("Subscription", subscription.asJson)),
        Vector(JsonHelpers.member("ResourceQuParams", query.params.asJson)),
      ),
    ),
  )
  given Decoder[QueryResource] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      params <- cursor.get[ResourceQuParams]("ResourceQuParams")
      subscription <- JsonHelpers.opt[Subscription](cursor, "Subscription")
      languages <- JsonHelpers.vec[LanguageTag](cursor, "Languages")
    yield QueryResource(header, params, languages, subscription),
  )

  given Encoder[ResponseKnownMessages] = Encoder.instance(response =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("Header", response.header.asJson)),
        JsonHelpers.vecMember("MessageService", response.services),
        JsonHelpers.optMember("ReturnCode", response.returnCode),
        response.notification.toVector.map(notification => JsonHelpers.member("Notification", notification.asJson)),
      ),
    ),
  )
  given Decoder[ResponseKnownMessages] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      services <- JsonHelpers.vec[MessageService](cursor, "MessageService")
      returnCode <- JsonHelpers.opt[Int](cursor, "ReturnCode")
      notification <- JsonHelpers.opt[Notification](cursor, "Notification")
    yield ResponseKnownMessages(header, services, returnCode, notification),
  )

  given Encoder[ResponseResource] = Encoder.instance(response =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("Header", response.header.asJson)),
        JsonHelpers.vecMember("ResourceInfo", response.resourceInfo),
        JsonHelpers.optMember("ReturnCode", response.returnCode),
        response.notification.toVector.map(notification => JsonHelpers.member("Notification", notification.asJson)),
      ),
    ),
  )
  given Decoder[ResponseResource] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      resourceInfo <- JsonHelpers.vec[ResourceInfo](cursor, "ResourceInfo")
      returnCode <- JsonHelpers.opt[Int](cursor, "ReturnCode")
      notification <- JsonHelpers.opt[Notification](cursor, "Notification")
    yield ResponseResource(header, resourceInfo, returnCode, notification),
  )

  given Encoder[SignalResource] = Encoder.instance(signal =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("ChannelMode", signal.channelMode),
        Vector(JsonHelpers.member("Header", signal.header.asJson)),
        JsonHelpers.optMember("ReplaceAfter", signal.replaceAfter),
        JsonHelpers.optMember("ReplaceBefore", signal.replaceBefore),
        JsonHelpers.vecMember("ResourceInfo", signal.resourceInfo),
      ),
    ),
  )
  given Decoder[SignalResource] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      resourceInfo <- JsonHelpers.vec[ResourceInfo](cursor, "ResourceInfo")
      channelMode <- JsonHelpers.opt[ChannelMode](cursor, "ChannelMode")
      replaceAfter <- JsonHelpers.opt[XsdDateTime](cursor, "ReplaceAfter")
      replaceBefore <- JsonHelpers.opt[XsdDateTime](cursor, "ReplaceBefore")
    yield SignalResource(header, resourceInfo, replaceAfter, replaceBefore, channelMode),
  )

  given Encoder[SignalNotification] = Encoder.instance(signal =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("ChannelMode", signal.channelMode),
        Vector(JsonHelpers.member("Header", signal.header.asJson)),
        Vector(JsonHelpers.member("Notification", signal.notification.asJson)),
      ),
    ),
  )
  given Decoder[SignalNotification] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      notification <- cursor.get[Notification]("Notification")
      channelMode <- JsonHelpers.opt[ChannelMode](cursor, "ChannelMode")
    yield SignalNotification(header, notification, channelMode),
  )

  given Encoder[SignalStatus] = Encoder.instance(signal =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("ChannelMode", signal.channelMode),
        Vector(JsonHelpers.member("Header", signal.header.asJson)),
        Vector(JsonHelpers.member("DeviceInfo", signal.deviceInfo.asJson)),
        signal.replacement.toVector.map(window =>
          JsonHelpers.member("ReplaceAfter", Json.fromString(window.after.value)),
        ),
        signal.replacement.toVector.map(window =>
          JsonHelpers.member("ReplaceBefore", Json.fromString(window.before.value)),
        ),
      ),
    ),
  )
  given Decoder[SignalStatus] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      deviceInfo <- cursor.get[DeviceInfo]("DeviceInfo")
      replaceAfter <- JsonHelpers.opt[XsdDateTime](cursor, "ReplaceAfter")
      replaceBefore <- JsonHelpers.opt[XsdDateTime](cursor, "ReplaceBefore")
      channelMode <- JsonHelpers.opt[ChannelMode](cursor, "ChannelMode")
      replacement <- (replaceAfter, replaceBefore) match
        case (Some(after), Some(before)) => Right(Some(StatusReplacementWindow(after, before)))
        case (None, None)                => Right(None)
        case _                           => JsonHelpers.fail(cursor, "both ReplaceAfter and ReplaceBefore or neither")
    yield SignalStatus(header, deviceInfo, replacement, channelMode),
  )

  // -- XJMF envelope (JSON exception: exactly one message) ------------------------

  private val messageNames: Vector[String] = JsonRegistry.messageNames.toVector.sorted

  private def decodeMessage(name: String, json: Json): Decoder.Result[Message] =
    JsonRegistry.decodeMessage(name, json)

  private def encodeMessage(message: Message): Json = JsonRegistry.encodeMessage(message)

  private def messageName(message: Message): String = JsonRegistry.messageName(message)

  given Encoder[XJMF] = Encoder.instance(xjmf =>
    val messages = xjmf.messages.toVector
    if messages.size != 1 then
      throw new UnsupportedOperationException(
        s"JSON XJMF requires exactly one message (normative JSON exception), got ${messages.size}",
      )
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("Header", xjmf.header.asJson)),
        JsonHelpers.optMember("Version", xjmf.version),
        Vector(JsonHelpers.member(messageName(messages.head), encodeMessage(messages.head))),
        Vector(JsonHelpers.rootName("XJMF")),
      ),
    ),
  )
  given Decoder[XJMF] = Decoder.instance(cursor =>
    for
      header <- cursor.get[Header]("Header")
      version <- JsonHelpers.opt[Version](cursor, "Version")
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
        case other => JsonHelpers.fail(cursor, s"JSON XJMF requires exactly one message, found ${other.size}")
    yield XJMF(header, NonEmptyVector.one(single), version),
  )
end JsonMessagingCodecs
