package xjdf4s.codec.xml

import xjdf4s.core.QualifiedName

/** Transport-level XML tree. The domain never sees this type: it lives entirely inside the codec layer. */
enum Xml derives CanEqual:
  case Element(name: QualifiedName, attributes: Vector[(QualifiedName, String)], children: Vector[Xml])
  case Text(value: String)

object Xml:
  def element(name: QualifiedName, attributes: Vector[(QualifiedName, String)] = Vector.empty)(
      children: Xml*,
  ): Xml.Element =
    Xml.Element(name, attributes, children.toVector)

  extension (element: Xml.Element)
    def attribute(localName: String): Option[String] =
      element.attributes.collectFirst { case (name, value) if name.localName == localName => value }

    def childElements: Vector[Xml.Element] =
      element.children.collect { case child: Xml.Element => child }

    /** The concatenated text content of the element. */
    def text: String =
      element.children.collect { case Xml.Text(value) => value }.mkString
  end extension
end Xml
