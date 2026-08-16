package xjdf4s.model

import xjdf4s.core.*

/** Open substitution point corresponding to abstract `ProductIntent` and extension intents from section 3.5.4. */
trait ProductIntent extends XjdfNode,
      Extensible:
  def elementName: QualifiedName
end ProductIntent

/** The 14 schema-defined ProductIntent descendants; ProductIntent itself stays open for ICS extensions. */
type StandardProductIntent =
  AssemblingIntent | BindingIntent | ColorIntent | ContentCheckIntent | EmbossingIntent | FoldingIntent |
    HoleMakingIntent | IntentResource | LaminatingIntent | LayoutIntent | MediaIntent | ProductionIntent |
    ShapeCuttingIntent | VariableIntent

/** Lossless fallback for a product intent defined by an ICS or a foreign namespace. */
final case class NamedProductIntent(
    elementName: QualifiedName,
    extensions: Extensions = Extensions.empty,
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
