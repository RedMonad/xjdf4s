package xjdf4s.core

/** The XJDF/XJMF schema target namespace, shared by the standard element names in the model and messaging modules. */
object XjdfNamespace:
  val uri: String = "http://www.CIP4.org/JDFSchema_2_0"
end XjdfNamespace

final case class QualifiedName(namespace: String, localName: String, prefix: Option[String] = None) derives CanEqual

object QualifiedName:
  def from(
      namespace: String,
      localName: String,
      prefix: Option[String] = None,
  ): Either[ValidationError, QualifiedName] =
    for
      ns <- Either.cond(namespace.nonEmpty, namespace, ValidationError.EmptyValue("namespace"))
      ln <- Either.cond(
        localName.nonEmpty,
        localName,
        ValidationError.EmptyValue("localName"),
      )
    yield QualifiedName(ns, ln, prefix)
end QualifiedName

/**
 * A qualified name that is guaranteed to belong to a foreign (non-XJDF) namespace. Extension fallbacks such as
 * [[Extensions]], `NamedSpecificResource` or the generic message carriers accept only [[ForeignQName]] values, so a
 * standard XJDF element name can never be smuggled through a generic extension node. Wildcard attributes are not
 * constrained here because `xs:anyAttribute` processing is codec policy; foreign *elements*, however, are hard
 * namespace facts.
 */
opaque type ForeignQName = QualifiedName
object ForeignQName:
  def from(
      namespace: String,
      localName: String,
      prefix: Option[String] = None,
  ): Either[ValidationError, ForeignQName] =
    QualifiedName
      .from(namespace, localName, prefix)
      .flatMap: name =>
        Either.cond(
          name.namespace != XjdfNamespace.uri,
          name,
          ValidationError.ForeignNameExpected(name.toString),
        )

  extension (name: ForeignQName)
    def namespace: String = name.namespace
    def localName: String = name.localName
    def prefix: Option[String] = name.prefix
    def qualifiedName: QualifiedName = name
end ForeignQName

enum ExtensionValue derives CanEqual:
  case Text(value: String)
  case Number(value: BigDecimal)
  case Bool(value: Boolean)
  case Null
end ExtensionValue

/**
 * Ordered content of a foreign-namespace extension element. XJDF foreign content can be mixed and ordered
 * (`text, child, text`); a plain children vector plus a single text value cannot preserve that order, so both are
 * carried by one ordered content sequence. Comment and processing-instruction nodes are retained for lossless
 * XML round-tripping.
 */
enum ExtensionContent derives CanEqual:
  case Text(value: String)
  case Element(node: ExtensionElement)
  case Comment(value: String)
  case ProcessingInstruction(target: String, data: String)
end ExtensionContent

/**
 * Transport-neutral representation of an element from a foreign namespace. The element name is namespace-checked at
 * construction, so XJDF standard elements cannot appear as foreign extension content.
 */
final case class ExtensionElement(
    name: ForeignQName,
    attributes: Map[QualifiedName, ExtensionValue] = Map.empty,
    content: Vector[ExtensionContent] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) derives CanEqual

object ExtensionElement:
  def text(name: ForeignQName, value: String, attributes: Map[QualifiedName, ExtensionValue] = Map.empty)
      : ExtensionElement =
    ExtensionElement(name, attributes, Vector(ExtensionContent.Text(value)))
end ExtensionElement

/** Transport-neutral carrier of the `anyAttribute` wildcard attributes and foreign child elements of a node. */
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
