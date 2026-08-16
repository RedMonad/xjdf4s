package xjdf4s.model

import xjdf4s.core.*

/** Open substitution point corresponding to abstract `ProductIntent`. */
trait ProductIntent extends XjdfNode:
  def elementName: Nmtoken
end ProductIntent

/** Schema-shaped fallback used until a concrete chapter-4 intent receives its dedicated ADT. */
final case class NamedProductIntent(
    elementName: Nmtoken,
    attributes: Map[QualifiedName, ExtensionValue] = Map.empty,
    children: Vector[ExtensionElement] = Vector.empty,
) extends ProductIntent

final case class Intent(
    name: Nmtoken,
    productIntent: Option[ProductIntent] = None,
    descriptiveName: Option[String] = None,
    externalId: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Product(
    amount: Option[Int] = None,
    commentUrl: Option[UriRef] = None,
    descriptiveName: Option[String] = None,
    externalId: Option[Nmtoken] = None,
    id: Option[XsdId] = None,
    isRoot: Option[Boolean] = None,
    maxAmount: Option[Int] = None,
    minAmount: Option[Int] = None,
    partVersion: Option[Nmtoken] = None,
    productType: Option[Nmtoken] = None,
    productTypeDetails: Option[String] = None,
    comments: Vector[Comment] = Vector.empty,
    generalIds: Vector[GeneralId] = Vector.empty,
    intents: Vector[Intent] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ProductList(
    products: NonEmptyVector[Product],
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible
