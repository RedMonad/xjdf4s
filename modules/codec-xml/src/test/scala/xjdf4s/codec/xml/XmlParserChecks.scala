package xjdf4s.codec.xml

import xjdf4s.core.{QualifiedName, XjdfNamespace}

object XmlParserChecks:
  private def qname(localName: String): QualifiedName = QualifiedName(XjdfNamespace.uri, localName)

  val basicElement: Unit =
    val parsed = XmlParser.parse("<Media MediaType=\"Paper\"/>").toOption.get
    assert(parsed.name == qname("Media"))
    assert(parsed.attribute("MediaType").contains("Paper"))
    assert(parsed.children.isEmpty)

  val nestedContent: Unit =
    val parsed = XmlParser.parse("<ResourceSet><Resource/><Resource/></ResourceSet>").toOption.get
    assert(parsed.childElements.size == 2)
    assert(parsed.childElements.head.name == qname("Resource"))

  val singleQuotes: Unit =
    val parsed = XmlParser.parse("<Part LotID='Lot1'/>").toOption.get
    assert(parsed.attribute("LotID").contains("Lot1"))

  val entityDecoding: Unit =
    val parsed = XmlParser.parse("<Comment>A &amp; B &lt; C</Comment>").toOption.get
    assert(parsed.text == "A & B < C")
    val numeric = XmlParser.parse("<Comment>&#65;&#x42;</Comment>").toOption.get
    assert(numeric.text == "AB")

  val cdataAndComments: Unit =
    val parsed = XmlParser.parse("<Comment><!-- note --><![CDATA[<raw>]]></Comment>").toOption.get
    assert(parsed.text == "<raw>")

  val prologAndPrologSkipping: Unit =
    val parsed = XmlParser.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><XJDF JobID=\"j\" Types=\"Product\"/>")
      .toOption.get
    assert(parsed.name == qname("XJDF"))

  val namespaceScoping: Unit =
    val parsed = XmlParser
      .parse("""<XJDF xmlns="http://www.CIP4.org/JDFSchema_2_0" xmlns:foo="urn:vendor">
               |  <Resource><foo:Custom foo:bar="1">text</foo:Custom></Resource>
               |</XJDF>""".stripMargin)
      .toOption.get
    assert(parsed.name.namespace == XjdfNamespace.uri)
    val resource = parsed.childElements.head
    val foreign = resource.childElements.head
    assert(foreign.name.namespace == "urn:vendor")
    assert(foreign.name.prefix.contains("foo"))
    assert(foreign.attribute("bar").contains("1"))
    assert(foreign.text == "text")

  val parseErrors: Unit =
    assert(XmlParser.parse("<Media>").isLeft)
    assert(XmlParser.parse("<Media></Color>").isLeft)
    assert(XmlParser.parse("<Media x=/>").isLeft)
    val error = XmlParser.parse("<Media></Color>").left.toOption.get
    assert(error.isInstanceOf[XmlError.Parse])
end XmlParserChecks
