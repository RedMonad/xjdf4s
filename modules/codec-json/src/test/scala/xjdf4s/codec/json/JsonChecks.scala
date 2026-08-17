package xjdf4s.codec.json

import io.circe.syntax.*

import xjdf4s.codec.json.given
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

object JsonChecks:

  private def roundTrip[A: io.circe.Encoder: io.circe.Decoder](value: A): A =
    val decoded = value.asJson.as[A]
    assert(decoded.isRight, decoded.left.map(_.toString).getOrElse(""))
    decoded.toOption.get

  private val jobId = Nmtoken.from("job-1").toOption.get
  private val process = Nmtoken.from("Product").toOption.get
  private val mediaName = Nmtoken.from("Media").toOption.get
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val time = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
  private val header = Header(deviceId, time)

  val scalars: Unit =
    assert(roundTrip(Nmtoken.from("token-1").toOption.get).value == "token-1")
    assert(roundTrip(IntegerRange(3, 7)) == IntegerRange(3, 7))
    assert(roundTrip(XYPair(1.5, 2.5)) == XYPair(1.5, 2.5))
    assert(roundTrip(MediaType.Paper) == MediaType.Paper)
    assert(roundTrip(Version.V2_2) == Version.V2_2)

  val mediaWithLayers: Unit =
    val layers = Vector(
      MediaLayer.MediaLayer(Media(MediaType.Paper, weight = Some(90.0f))),
      MediaLayer.GlueLayer(Glue(areaGlue = Some(true), glueType = Some(GlueType.Removable))),
      MediaLayer.MediaLayer(Media(MediaType.Paper, weight = Some(60.0f))),
    )
    val media = Media(
      MediaType.SelfAdhesive,
      dimension = Some(XYPair(1190.5511811, 0.0)),
      mediaUnit = Some(MediaUnit.Roll),
      thickness = Some(900.0f),
      mediaLayers = Some(MediaLayers(layers)),
    )
    val decoded = roundTrip(media)
    assert(decoded == media)
    // in-lining exception: the layer array carries the "Name" discriminator members
    val encoded = media.asJson
    val layerArray = encoded.hcursor.downField("MediaLayers").focus.flatMap(_.asArray).get
    assert(layerArray.size == 3)
    assert(layerArray(0).hcursor.get[String]("Name").toOption.contains("Media"))
    assert(layerArray(1).hcursor.get[String]("Name").toOption.contains("Glue"))
    assert(layerArray(2).hcursor.get[String]("Name").toOption.contains("Media"))

  val resourceTree: Unit =
    val document = XJDF(
      jobId,
      NonEmptyVector.one(process),
      resourceSets = Vector(
        ResourceSet(
          mediaName,
          usage = Some(ResourceUsage.Input),
          resources = Vector(
            Resource(specificResource = Some(Media(MediaType.Paper, weight = Some(80.0f)))),
            Resource(specificResource = Some(Color(density = Some(0.5f)))),
          ),
        ),
      ),
    )
    val decoded = roundTrip(document)
    assert(decoded == document)
    val encoded = document.asJson
    assert(encoded.hcursor.get[String]("Name").toOption.contains("XJDF"))
    assert(encoded.hcursor.get[List[String]]("Types").toOption.contains(List("Product")))

  val messages: Unit =
    val query = QueryResource(header, ResourceQuParams(Scope.Allowed))
    assert(roundTrip(query) == query)
    val signal = SignalResource(header, channelMode = Some(ChannelMode.Reliable))
    assert(roundTrip(signal) == signal)
    val xjmf = XJMF(header, NonEmptyVector.one(query), version = Some(Version.V2_2))
    assert(roundTrip(xjmf) == xjmf)
    val encoded = xjmf.asJson
    assert(encoded.hcursor.get[String]("Name").toOption.contains("XJMF"))
    assert(encoded.hcursor.downField("QueryResource").focus.nonEmpty)
end JsonChecks
