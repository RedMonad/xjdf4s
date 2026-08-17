package xjdf4s.codec.xml

import xjdf4s.core.QualifiedName

/**
 * Canonical, compact XML writer: no pretty-printing, deterministic attribute order, proper escaping. Namespace
 * declarations (`xmlns`, `xmlns:prefix`) are emitted where they are needed — an element that introduces a prefix
 * or an unprefixed namespace not bound by its ancestors declares it itself, so foreign content stays well-formed
 * and round-trips.
 */
object XmlWriter:

  def write(xml: Xml): String =
    val builder = new StringBuilder
    writeNode(xml, builder, inheritedScopes = Map.empty)
    builder.result()

  private def writeNode(xml: Xml, builder: StringBuilder, inheritedScopes: Map[String, String]): Unit =
    xml match
      case Xml.Text(value) => builder.append(escapeText(value))
      case element: Xml.Element =>
        val declared = neededDeclarations(element, inheritedScopes)
        val scopes = inheritedScopes ++ declared
        builder.append('<').append(renderName(element.name))
        declared.toVector
          .sortBy { case (prefix, _) => prefix }
          .foreach { case (prefix, uri) =>
            builder.append(' ')
            if prefix.isEmpty then builder.append("xmlns") else builder.append("xmlns:").append(prefix)
            builder.append("=\"").append(escapeAttribute(uri)).append('"')
          }
        element.attributes.foreach: (name, value) =>
          builder.append(' ').append(renderName(name)).append("=\"").append(escapeAttribute(value)).append('"')
        if element.children.isEmpty then builder.append("/>")
        else
          builder.append('>')
          element.children.foreach(child => writeNode(child, builder, scopes))
          builder.append("</").append(renderName(element.name)).append('>')

  /** Namespaces used by the element's own name and attribute names that its ancestors do not already bind. */
  private def neededDeclarations(
      element: Xml.Element,
      inheritedScopes: Map[String, String],
  ): Map[String, String] =
    val used = Vector.newBuilder[(String, String)]
    element.name.prefix match
      case Some(prefix) =>
        if element.name.namespace.nonEmpty then used += (prefix -> element.name.namespace)
      case None =>
        if element.name.namespace.nonEmpty then used += ("" -> element.name.namespace)
    element.attributes.foreach: (name, _) =>
      name.prefix.foreach(prefix => if name.namespace.nonEmpty then used += (prefix -> name.namespace))
    used.result().toMap.filter { case (prefix, uri) => inheritedScopes.get(prefix) != Some(uri) }

  private def renderName(name: QualifiedName): String =
    name.prefix match
      case Some(prefix) => s"$prefix:${name.localName}"
      case None         => name.localName

  private def escapeText(value: String): String =
    value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

  private def escapeAttribute(value: String): String =
    escapeText(value).replace("\"", "&quot;")
end XmlWriter
