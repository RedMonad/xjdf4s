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

  /** Self-recursive types are hand-coded (XML rule f); both recursion shapes round-trip. */
  val assemblySection: Unit =
    val section = AssemblySection(
      Nmtoken.from("sig-1").toOption.get,
      sections = Vector(AssemblySection(Nmtoken.from("sig-2").toOption.get)),
    )
    assert(roundTrip(section) == section)

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

  /**
   * Plain enums extend scala.Product (reference/enums.md), so they must stay scalar attributes in the derived
   * codecs - the member is the attribute name "Status", never the enum class name "NodeStatus".
   */
  val nodeInfoStatus: Unit =
    val info = NodeInfo(start = Some(time), status = Some(NodeStatus.Waiting))
    val decoded = roundTrip(info)
    assert(decoded.status.contains(NodeStatus.Waiting))
    val members = info.asJson.hcursor.keys.getOrElse(Iterable.empty).toSet
    assert(members.contains("Status"))
    assert(!members.contains("NodeStatus"))

  /** Value-type case classes (Rectangle here) are attributes too: the member is "ExpansionBox", not "Rectangle". */
  val layoutExpansionBox: Unit =
    val layout = Layout(expansionBox = Some(Rectangle(XYPair(0, 0), XYPair(100, 50))))
    val decoded = roundTrip(layout)
    assert(decoded.expansionBox.contains(Rectangle(XYPair(0, 0), XYPair(100, 50))))
    val members = layout.asJson.hcursor.keys.getOrElse(Iterable.empty).toSet
    assert(members.contains("ExpansionBox"))
    assert(!members.contains("Rectangle"))

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
