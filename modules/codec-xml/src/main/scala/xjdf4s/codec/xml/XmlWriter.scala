package xjdf4s.codec.xml

import xjdf4s.core.{QualifiedName, XjdfNamespace}

/**
 * Canonical, compact XML writer: no pretty-printing, deterministic attribute order, proper escaping. The root
 * element in the XJDF namespace gets an explicit `xmlns` declaration so that documents are self-contained.
 */
object XmlWriter:

  def write(xml: Xml): String =
    val builder = new StringBuilder
    writeNode(xml, builder, isRoot = true)
    builder.result()

  private def writeNode(xml: Xml, builder: StringBuilder, isRoot: Boolean): Unit =
    xml match
      case Xml.Text(value) => builder.append(escapeText(value))
      case element: Xml.Element =>
        builder.append('<').append(renderName(element.name))
        if isRoot && element.name.prefix.isEmpty && element.name.namespace == XjdfNamespace.uri then
          builder.append(" xmlns=\"").append(XjdfNamespace.uri).append('"')
        element.attributes.foreach: (name, value) =>
          builder.append(' ').append(renderName(name)).append("=\"").append(escapeAttribute(value)).append('"')
        if element.children.isEmpty then builder.append("/>")
        else
          builder.append('>')
          element.children.foreach(child => writeNode(child, builder, isRoot = false))
          builder.append("</").append(renderName(element.name)).append('>')

  private def renderName(name: QualifiedName): String =
    name.prefix match
      case Some(prefix) => s"$prefix:${name.localName}"
      case None         => name.localName

  private def escapeText(value: String): String =
    value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

  private def escapeAttribute(value: String): String =
    escapeText(value).replace("\"", "&quot;")
end XmlWriter
