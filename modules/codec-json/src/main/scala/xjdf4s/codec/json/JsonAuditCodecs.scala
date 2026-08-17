package xjdf4s.codec.json

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.*

import xjdf4s.core.*
import xjdf4s.model.*

/**
 * JSON codecs for the Audit family (Example 9.11): `AuditPool` is an array of audit objects, each carrying the
 * root-style `"Name"` discriminator (`AuditCreated`, `AuditNotification`, ...), its `Header` and its payload.
 */
object JsonAuditCodecs:

  given Encoder[ProcessRun] = Encoder.instance(processRun =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("End", Json.fromString(processRun.end.value))),
        Vector(JsonHelpers.member("EndStatus", Json.fromString(processRun.endStatus.toString))),
        Vector(JsonHelpers.member("Start", Json.fromString(processRun.start.value))),
        JsonHelpers.optMember("Duration", processRun.duration),
        JsonHelpers.optMember("QueueEntryID", processRun.queueEntryId),
        JsonHelpers.optMember("ReturnTime", processRun.returnTime),
        JsonHelpers.optMember("SubmissionTime", processRun.submissionTime),
        JsonHelpers.vecMember("Part", processRun.parts),
      ),
    ),
  )
  given Decoder[ProcessRun] = Decoder.instance(cursor =>
    for
      end <- cursor.get[XsdDateTime]("End")
      endStatus <- cursor.get[ProcessRunEndStatus]("EndStatus")
      start <- cursor.get[XsdDateTime]("Start")
      duration <- JsonHelpers.opt[XsdDuration](cursor, "Duration")
      queueEntryId <- JsonHelpers.opt[Nmtoken](cursor, "QueueEntryID")
      returnTime <- JsonHelpers.opt[XsdDateTime](cursor, "ReturnTime")
      submissionTime <- JsonHelpers.opt[XsdDateTime](cursor, "SubmissionTime")
      parts <- JsonHelpers.vec[Part](cursor, "Part")
    yield ProcessRun(end, endStatus, start, duration, queueEntryId, returnTime, submissionTime, parts),
  )

  private def auditName(audit: Audit): String =
    audit.name.toString

  private def encodeAudit(audit: Audit): Json =
    val (header, payload) = audit match
      case AuditCreated(header, _) => (header, Vector.empty[(String, Json)])
      case AuditNotification(header, notification, _) =>
        (header, Vector(JsonHelpers.member("Notification", notification.asJson)))
      case AuditProcessRun(header, processRun, _) =>
        (header, Vector(JsonHelpers.member("ProcessRun", processRun.asJson)))
      case AuditResource(header, resourceInfo, _) =>
        (header, Vector(JsonHelpers.member("ResourceInfo", resourceInfo.asJson)))
      case AuditStatus(header, deviceInfo, _) =>
        (header, Vector(JsonHelpers.member("DeviceInfo", deviceInfo.asJson)))
    JsonHelpers.obj(
      JsonHelpers.memberList(
        Vector(JsonHelpers.member("Header", header.asJson)),
        payload,
        Vector(JsonHelpers.member("Name", Json.fromString(auditName(audit)))),
      ),
    )

  private def decodeAudit(json: Json): Decoder.Result[Audit] =
    for
      name <- json.hcursor.get[String]("Name")
      header <- json.hcursor.get[Header]("Header")
      audit <- name match
        case "AuditCreated" =>
          Right(AuditCreated(header))
        case "AuditNotification" =>
          json.hcursor.get[Notification]("Notification").map(AuditNotification(header, _))
        case "AuditProcessRun" =>
          json.hcursor.get[ProcessRun]("ProcessRun").map(AuditProcessRun(header, _))
        case "AuditResource" =>
          json.hcursor.get[ResourceInfo]("ResourceInfo").map(AuditResource(header, _))
        case "AuditStatus" =>
          json.hcursor.get[DeviceInfo]("DeviceInfo").map(AuditStatus(header, _))
        case other => JsonHelpers.fail(json.hcursor, s"unknown audit Name '$other'")
    yield audit

  given Encoder[AuditPool] = Encoder.instance(pool => Json.arr(pool.audits.map(encodeAudit)*))
  given Decoder[AuditPool] = Decoder.instance(cursor =>
    cursor.as[List[Json]].flatMap { items =>
      items.foldLeft[Decoder.Result[Vector[Audit]]](Right(Vector.empty)) { (acc, item) =>
        for
          audits <- acc
          decoded <- decodeAudit(item)
        yield audits :+ decoded
      }
    }.map(AuditPool(_)),
  )
end JsonAuditCodecs
