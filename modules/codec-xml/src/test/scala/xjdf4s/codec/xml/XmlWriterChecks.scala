package xjdf4s.codec.xml

import xjdf4s.core.{QualifiedName, XjdfNamespace}

object XmlWriterChecks:
  private def qname(localName: String): QualifiedName = QualifiedName(XjdfNamespace.uri, localName)

  val escaping: Unit =
    val xml = Xml.element(qname("Comment"), Vector((qname("Author"), "A & B")))(Xml.Text("1 < 2 > 0"))
    val written = XmlWriter.write(xml)
    assert(written.contains("Author=\"A &amp; B\""))
    assert(written.contains("1 &lt; 2 &gt; 0"))

  val selfClosing: Unit =
    val written = XmlWriter.write(Xml.element(qname("Media"), Vector((qname("MediaType"), "Paper")))())
    assert(written.contains("<Media"))
    assert(written.endsWith("/>"))

  val rootNamespaceDeclaration: Unit =
    val written = XmlWriter.write(Xml.element(qname("XJDF"))())
    assert(written.contains("xmlns=\"http://www.CIP4.org/JDFSchema_2_0\""))

  val writerRoundTrip: Unit =
    // attributes are unqualified in XML (no namespace), per the Namespaces spec
    val xml = Xml.element(
      qname("Resource"),
      Vector((QualifiedName("", "ExternalID"), "MIS-1")),
    )(Xml.element(qname("Part"), Vector((QualifiedName("", "LotID"), "Lot1")))(), Xml.Text("payload"))
    val reparsed = XmlParser.parse(XmlWriter.write(xml)).toOption.get
    assert(reparsed == xml)

  val prefixRendering: Unit =
    val foreign = QualifiedName("urn:vendor", "Note", Some("foo"))
    val written = XmlWriter.write(Xml.element(foreign)())
    assert(written.startsWith("<foo:Note"))
end XmlWriterChecks
