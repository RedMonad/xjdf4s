package xjdf4s.core

final case class QualifiedName(namespace: String, localName: String, prefix: Option[String] = None) derives CanEqual

enum ExtensionValue derives CanEqual:
  case Text(value: String)
  case Number(value: BigDecimal)
  case Bool(value: Boolean)
  case Null
end ExtensionValue

/** Transport-neutral representation of an element from a foreign namespace. */
final case class ExtensionElement(
    name: QualifiedName,
    attributes: Map[QualifiedName, ExtensionValue] = Map.empty,
    children: Vector[ExtensionElement] = Vector.empty,
    value: Option[ExtensionValue] = None,
) derives CanEqual

final case class Extensions(
    attributes: Map[QualifiedName, ExtensionValue] = Map.empty,
    elements: Vector[ExtensionElement] = Vector.empty,
) derives CanEqual

object Extensions:
  val empty: Extensions = Extensions()
end Extensions

trait XjdfNode

trait Extensible:
  def extensions: Extensions
end Extensible

/** A node that is both part of XJDF and capable of carrying schema wildcards. */
type OpenXjdfNode = XjdfNode & Extensible
