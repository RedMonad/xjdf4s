package xjdf4s.codec.xml

import xjdf4s.core.*

/** Lossless support for foreign-namespace content: `ExtensionElement`/`ExtensionContent` decode and encode.
 *
 *  Known limitations, by design of the XML tree: comments and processing instructions are not represented by the
 *  parser (they are dropped), so `ExtensionContent.Comment`/`ProcessingInstruction` cannot be *emitted* by this
 *  writer and fail loudly instead of being corrupted; attribute values of foreign elements decode to
 *  `ExtensionValue.Text` (the lexical type is not recoverable from XML).
 */
object ForeignCodec:

  def decodeForeignElement(element: Xml.Element): Either[XmlError, ExtensionElement] =
    ForeignQName
      .from(element.name.namespace, element.name.localName, element.name.prefix)
      .left
      .map(_ => XmlError.ForeignNameExpected(renderName(element.name)))
      .flatMap: name =>
        decodeChildren(element).map: children =>
          ExtensionElement(
            name = name,
            attributes = element.attributes
              .map { case (attributeName, value) => (attributeName, ExtensionValue.Text(value)) }
              .toMap,
            content = children,
          )

  def encodeForeignElement(element: ExtensionElement): Xml.Element =
    Xml.Element(
      element.name.qualifiedName,
      element.attributes.toVector
        .sortBy(pair => (pair._1.namespace, pair._1.localName))
        .map { case (name, value) => (name, renderExtensionValue(value)) },
      element.content.map(encodeExtensionContent),
    )

  def encodeExtensionContent(content: ExtensionContent): Xml =
    content match
      case ExtensionContent.Text(value) => Xml.Text(value)
      case ExtensionContent.Element(node) => encodeForeignElement(node)
      case ExtensionContent.Comment(value) =>
        throw new UnsupportedOperationException(
          "ExtensionContent.Comment cannot be emitted: the XML writer does not represent comments",
        )
      case ExtensionContent.ProcessingInstruction(target, _) =>
        throw new UnsupportedOperationException(
          s"ExtensionContent.ProcessingInstruction($target) cannot be emitted: the XML writer does not represent processing instructions",
        )

  def renderExtensionValue(value: ExtensionValue): String =
    value match
      case ExtensionValue.Text(text) => text
      case ExtensionValue.Number(n) => n.toString
      case ExtensionValue.Bool(flag) => flag.toString
      case ExtensionValue.Null => "null"

  private def decodeChildren(element: Xml.Element): Either[XmlError, Vector[ExtensionContent]] =
    element.children.foldLeft[Either[XmlError, Vector[ExtensionContent]]](Right(Vector.empty)) { (acc, child) =>
      for
        contents <- acc
        content  <- child match
          case Xml.Text(value) => Right(ExtensionContent.Text(value))
          case childElement: Xml.Element => decodeForeignElement(childElement).map(ExtensionContent.Element(_))
      yield contents :+ content
    }

  private def renderName(name: QualifiedName): String =
    name.prefix match
      case Some(prefix) => s"$prefix:${name.localName}"
      case None => name.localName
end ForeignCodec
