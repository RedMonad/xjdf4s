package xjdf4s.codec.xml

import xjdf4s.codec.xml.domain.*
import xjdf4s.core.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

object ReferenceAndWildcardChecks:
  private val jobId = Nmtoken.from("job-1").toOption.get
  private val process = Nmtoken.from("Product").toOption.get
  private val mediaName = Nmtoken.from("Media").toOption.get
  private val mediaId = XsdId.from("media-1").toOption.get

  private def documentWith(component: Component): XJDF =
    XJDF(
      jobId,
      NonEmptyVector.one(process),
      resourceSets = Vector(
        ResourceSet(
          mediaName,
          resources = Vector(Resource(id = Some(mediaId)), Resource(specificResource = Some(component))),
        ),
      ),
    )

  val danglingReference: Unit =
    val component = Component(mediaRef = Some(XsdIdRef.from("nowhere").toOption.get))
    assert(ReferenceCheck.validate(documentWith(component)).size == 1)

  val validReferences: Unit =
    val component = Component(
      mediaRef = Some(XsdIdRef.from("media-1").toOption.get),
      contentRefs = Vector(XsdIdRef.from("media-1").toOption.get),
    )
    assert(ReferenceCheck.validate(documentWith(component)).isEmpty)

  val foreignElementRoundTrip: Unit =
    val xml =
      """<XJDF xmlns="http://www.CIP4.org/JDFSchema_2_0" xmlns:foo="urn:vendor" JobID="job-1" Types="Product">
        |  <ResourceSet Name="Media">
        |    <Resource>
        |      <foo:Custom foo:code="7"><foo:Inner/></foo:Custom>
        |    </Resource>
        |  </ResourceSet>
        |</XJDF>""".stripMargin
    val decoded = XmlParser.parse(xml).flatMap(XjdfCodec.decoder.decode).toOption.get
    val resource = decoded.resourceSets.head.resources.head
    assert(resource.foreignElements.size == 1)
    assert(resource.foreignElements.head.name.localName == "Custom")
    val reencoded = XmlParser.parse(XmlWriter.write(XjdfCodec.encoder.encode(decoded))).toOption.get
    val redecode = XjdfCodec.decoder.decode(reencoded).toOption.get
    assert(redecode.resourceSets.head.resources.head.foreignElements == resource.foreignElements)

  val foreignSpecificResource: Unit =
    val xml =
      """<Resource xmlns="http://www.CIP4.org/JDFSchema_2_0" xmlns:foo="urn:vendor">
        |  <foo:Wobble foo:size="3"/>
        |</Resource>""".stripMargin
    val decoded = XmlParser.parse(xml).flatMap(ResourceCodec.decoder.decode).toOption.get
    assert(decoded.specificResource.isEmpty)
    assert(decoded.foreignElements.head.name.localName == "Wobble")
    assert(decoded.foreignElements.head.attributes.exists { case (name, _) => name.localName == "size" })
    val fallback = Registry
      .decodeSpecificResource(XmlParser.parse("""<foo:Wobble xmlns:foo="urn:vendor" foo:size="3"/>""").toOption.get)
      .toOption
      .get
      .asInstanceOf[NamedSpecificResource]
    assert(fallback.foreignName.localName == "Wobble")
    val reencoded = Registry.encodeSpecificResource(fallback)
    assert(reencoded.name.localName == "Wobble")
    assert(reencoded.attribute("size").contains("3"))

  val unsupportedStandardElement: Unit =
    val xml = """<Resource xmlns="http://www.CIP4.org/JDFSchema_2_0"><RunList/></Resource>"""
    val result = XmlParser.parse(xml).flatMap(ResourceCodec.decoder.decode)
    assert(result.left.toOption.exists(_.isInstanceOf[XmlError.UnsupportedElement]))
end ReferenceAndWildcardChecks
