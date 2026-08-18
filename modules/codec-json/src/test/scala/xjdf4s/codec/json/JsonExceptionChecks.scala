package xjdf4s.codec.json

import io.circe.parser.*
import io.circe.syntax.*
import xjdf4s.codec.json.JsonRootCodecs.given
import xjdf4s.codec.json.given
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/** The normative JSON exceptions: root `$schema`, exactly-one XJMF message, MediaLayers in-lining. */
object JsonExceptionChecks:

  private val jobId = Nmtoken.from("job-1").toOption.get
  private val process = Nmtoken.from("Product").toOption.get
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val time = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
  private val header = Header(deviceId, time)

  val rootSchemaMember: Unit =
    val document = XJDF(jobId, NonEmptyVector.one(process))
    val withSchema = JsonHelpers.withSchema(document.asJson, "https://schema.cip4.org/xjdf-2.2.json")
    assert(withSchema.hcursor.get[String]("$schema").toOption.contains("https://schema.cip4.org/xjdf-2.2.json"))
    // the member is optional: the plain encoder omits it
    assert(document.asJson.hcursor.downField("$schema").focus.isEmpty)
    // decode accepts and ignores the member
    assert(withSchema.as[XJDF].toOption.contains(document))

  val xjmfExactlyOneMessage: Unit =
    val single = XJMF(header, NonEmptyVector.one(QueryKnownMessages(header)))
    assert(single.asJson.as[XJMF].toOption.contains(single))
    // two messages cannot be encoded to JSON (normative exactly-one exception)
    val double = XJMF(header, NonEmptyVector(QueryKnownMessages(header), QueryKnownMessages(header)))
    val doubleThrows =
      try
        double.asJson
        false
      catch case _: UnsupportedOperationException => true
    assert(doubleThrows)
    // ... and a JSON document with two message members does not decode
    val twoMembers =
      """{"Name":"XJMF","Header":{"DeviceID":"device-1","Time":"2026-08-17T12:00:00+03:00"},
        |"QueryKnownMessages":{"Header":{"DeviceID":"device-1","Time":"2026-08-17T12:00:00+03:00"}},
        |"SignalResource":{"Header":{"DeviceID":"device-1","Time":"2026-08-17T12:00:00+03:00"}}}""".stripMargin
    assert(parse(twoMembers).flatMap(_.as[XJMF]).isLeft)

  val mediaLayersInlineName: Unit =
    // the discriminator member is "Name" (attribute name without the leading @)
    val json = parse(
      """{"MediaType":"Paper","MediaLayers":[
        |  {"MediaType":"Paper","Name":"Media","Weight":90},
        |  {"AreaGlue":true,"GlueType":"Removable","Name":"Glue"},
        |  {"MediaType":"Paper","Name":"Media","Weight":60}
        |]}""".stripMargin,
    ).flatMap(_.as[Media])
    val media = json.toOption.get
    val layers = media.mediaLayers.get.layers
    assert(layers.size == 3)
    assert(layers.head.isInstanceOf[MediaLayer.MediaLayer])
    assert(layers(1).isInstanceOf[MediaLayer.GlueLayer])
    assert(layers(2).isInstanceOf[MediaLayer.MediaLayer])
    val roundTrip = media.asJson.as[Media].toOption.get
    assert(roundTrip == media)
end JsonExceptionChecks
