package xjdf4s.codec.json

import io.circe.syntax.*

import xjdf4s.codec.json.given
import xjdf4s.codec.json.JsonSpecialCodecs.given
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/** Round-trips for the derived JSON codecs (JsonDerived + JsonDerivedInstances + JsonSpecialCodecs). */
object JsonDerivedChecks:

  private def roundTrip[A: io.circe.Encoder: io.circe.Decoder](value: A): A =
    val decoded = value.asJson.as[A]
    assert(decoded.isRight, decoded.left.map(_.toString).getOrElse(""))
    decoded.toOption.get

  private val time = XsdDateTime.from("2026-08-18T12:00:00+03:00").toOption.get
  private val url = UriRef.from("https://example.com/job.xjdf").toOption.get
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val header = Header(deviceId, time)

  val fileSpec: Unit =
    val spec = FileSpec(
      location = FileLocation.Url(url),
      checkSum = Some(Vector(1.toByte, 2.toByte)),
      mimeType = Some(XjdfString.from("application/vnd.cip4-xjdf+xml").toOption.get),
      disposition = Some(Disposition(time = Some(DispositionTime.At(time)))),
    )
    assert(roundTrip(spec) == spec)

  val deliveryParams: Unit =
    val params = DeliveryParams(
      earliest = Some(time),
      method = Some(Nmtoken.from("Download").toOption.get),
      files = DeliveryFiles(contents = Some(FileSpec(location = FileLocation.Pipe))),
    )
    assert(roundTrip(params) == params)

  val tiffFormatParams: Unit =
    val params = TiffFormatParams(
      tags = Vector(TiffTag(315, 2, Some(TiffTagValue.Text("copyright")))),
      embeddedFiles = Vector(TiffEmbeddedFile(700, 1, FileSpec(FileLocation.Url(url)))),
    )
    assert(roundTrip(params) == params)

  val placedObject: Unit =
    val placed = PlacedObject(Matrix.identity, PlacedObjectKind.Mark(MarkObject()))
    assert(roundTrip(placed) == placed)

  val contact: Unit =
    val contact = Contact(
      address = Some(Address(city = Some(XjdfString.from("Berlin").toOption.get), addressLines = Vector("Line 1", "Line 2"))),
      company = Some(Company(XjdfString.from("ACME").toOption.get, organizationalUnits = Vector("R&D"))),
    )
    assert(roundTrip(contact) == contact)

  val bundleItem: Unit =
    val bundle = BundleItem(amount = 1, children = Vector(BundleItem(amount = 2)))
    assert(roundTrip(bundle) == bundle)

  val message: Unit =
    val query = QueryStatus(header)
    assert(roundTrip(query) == query)

  val intent: Unit =
    val mediaIntent = MediaIntent(MediaType.Paper)
    assert(roundTrip(mediaIntent) == mediaIntent)

  /** The Product -> Intent -> ProductIntent chain exercises the open-union dispatch of the derived codecs. */
  val productWithIntent: Unit =
    val product = Product(intents = Vector(Intent(Nmtoken.from("media-intent").toOption.get, Some(MediaIntent(MediaType.Paper)))))
    assert(roundTrip(product) == product)

  val foreignExtensions: Unit =
    val mediaIntent = MediaIntent(
      MediaType.Paper,
      extensions = Extensions(attributes = Map(QualifiedName("http://example.com/ics", "weight") -> ExtensionValue.Number(BigDecimal("1.5")))),
    )
    val decoded = roundTrip(mediaIntent)
    val decodedAttribute = decoded.extensions.attributes.toVector.head
    assert(decodedAttribute._1.namespace == "http://example.com/ics")
    assert(decodedAttribute._1.localName == "weight")
    assert(decodedAttribute._2 == ExtensionValue.Number(BigDecimal("1.5")))

  val registryCoverage: Unit =
    assert(JsonRegistry.resourceNames.size == 99)
    assert(JsonRegistry.intentNames.size == 11)
    assert(JsonRegistry.messageNames.size == 42)
    assert(JsonRegistry.resourceNames.contains("Media"))
    assert(JsonRegistry.resourceNames.contains("DeliveryParams"))
    assert(JsonRegistry.intentNames.contains("MediaIntent"))
    assert(JsonRegistry.messageNames.contains("QueryStatus"))
    assert(JsonRegistry.messageNames.contains("SignalStatus"))
end JsonDerivedChecks
