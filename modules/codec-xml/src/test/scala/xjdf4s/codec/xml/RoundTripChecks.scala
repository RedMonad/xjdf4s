package xjdf4s.codec.xml

import xjdf4s.codec.xml.domain.*
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/** Round-trip law `decode(encode(value)) == value` for the covered node surface. */
object RoundTripChecks:
  private def roundTrip[A](value: A, encoder: XmlEncoder[A], decoder: XmlDecoder[A]): A =
    val decoded = decoder.decode(encoder.encode(value))
    assert(decoded.isRight, decoded.left.toOption.map(_.toString).getOrElse(""))
    decoded.toOption.get

  private val jobId = Nmtoken.from("job-1").toOption.get
  private val process = Nmtoken.from("Product").toOption.get
  private val mediaName = Nmtoken.from("Media").toOption.get

  val comment: Unit =
    val value = Comment("shipped", externalId = Some(Nmtoken.from("ext-1").toOption.get))
    assert(roundTrip(value, CommentCodec.encoder, CommentCodec.decoder) == value)

  val generalId: Unit =
    val value = GeneralId(
      Nmtoken.from("pool").toOption.get,
      XjdfString.from("bar snax").toOption.get,
      Some(GeneralId.DataType.NamedFeature),
    )
    assert(roundTrip(value, GeneralIdCodec.encoder, GeneralIdCodec.decoder) == value)

  val part: Unit =
    val value = Part(
      lotId = Some(Nmtoken.from("Lot1").toOption.get),
      docIndex = Some(IntegerRange(0, 9)),
      side = Some(Side.Front),
      tileId = Some(TileCoordinate(1, 0)),
    )
    assert(roundTrip(value, PartCodec.encoder, PartCodec.decoder) == value)

  val mediaWithLayers: Unit =
    val inner = Media(MediaType.Paper, weight = Some(60.0f))
    val layers = Vector(
      MediaLayer.MediaLayer(Media(MediaType.Paper, weight = Some(90.0f))),
      MediaLayer.GlueLayer(Glue(areaGlue = Some(true), glueType = Some(GlueType.Removable))),
      MediaLayer.MediaLayer(inner),
    )
    val value = Media(
      MediaType.SelfAdhesive,
      mediaUnit = Some(MediaUnit.Roll),
      thickness = Some(900.0f),
      mediaLayers = Some(MediaLayers(layers)),
    )
    assert(roundTrip(value, MediaCodec.encoder, MediaCodec.decoder) == value)

  val resourceSet: Unit =
    val media = Media(MediaType.Paper, weight = Some(80.0f))
    val value = ResourceSet(
      mediaName,
      usage = Some(ResourceUsage.Input),
      resources = Vector(Resource(specificResource = Some(media))),
    )
    assert(roundTrip(value, ResourceSetCodec.encoder, ResourceSetCodec.decoder) == value)

  val xjdfDocument: Unit =
    val value = XJDF(
      jobId,
      NonEmptyVector.one(process),
      resourceSets = Vector(
        ResourceSet(mediaName, usage = Some(ResourceUsage.Input), resources = Vector(Resource())),
      ),
      version = Some(Version.V2_2),
    )
    assert(roundTrip(value, XjdfCodec.encoder, XjdfCodec.decoder) == value)

  val colorAndToolAndComponent: Unit =
    val color = Color(lab = LabColor.from(50.0, 0.0, 0.0).toOption, colorName = Some(NamedColor.AliceBlue))
    assert(roundTrip(color, ColorCodec.encoder, ColorCodec.decoder) == color)
    val tool = Tool(manufacturer = XjdfString.from("Acme").toOption)
    assert(roundTrip(tool, ToolCodec.encoder, ToolCodec.decoder) == tool)
    val mediaId = XsdIdRef.from("media-1").toOption.get
    val component = Component(mediaRef = Some(mediaId), surfaceCount = Some(2))
    assert(roundTrip(component, ComponentCodec.encoder, ComponentCodec.decoder) == component)

  val device: Unit =
    val value = Device(
      Nmtoken.from("dev-1").toOption.get,
      jdfVersions = Vector(JdfVersion.V1_8, JdfVersion.V2_2),
      packaging = Vector(DevicePackaging.XML),
    )
    assert(roundTrip(value, DeviceCodec.encoder, DeviceCodec.decoder) == value)

  val messages: Unit =
    val deviceId = Nmtoken.from("device-1").toOption.get
    val time = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
    val header = Header(deviceId, time)
    val query = QueryResource(header, ResourceQuParams(Scope.Allowed))
    assert(roundTrip(query, QueryResourceCodec.encoder, QueryResourceCodec.decoder) == query)
    val signal = SignalResource(header, channelMode = Some(ChannelMode.Reliable))
    assert(roundTrip(signal, SignalResourceCodec.encoder, SignalResourceCodec.decoder) == signal)
    val xjmf = XJMF(header, NonEmptyVector.one(query))
    assert(roundTrip(xjmf, XjmfCodec.encoder, XjmfCodec.decoder) == xjmf)
end RoundTripChecks
