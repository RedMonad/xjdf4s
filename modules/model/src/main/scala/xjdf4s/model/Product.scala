package xjdf4s.model

import xjdf4s.core.*

/** Open substitution point corresponding to abstract `ProductIntent` and extension intents from section 3.5.4. */
trait ProductIntent extends XjdfNode,
      Extensible:
  def elementName: QualifiedName

/** The 14 schema-defined ProductIntent descendants; ProductIntent itself stays open for ICS extensions. */
type StandardProductIntent =
  AssemblingIntent | BindingIntent | ColorIntent | ContentCheckIntent | EmbossingIntent | FoldingIntent |
    HoleMakingIntent | IntentResource | LaminatingIntent | LayoutIntent | MediaIntent | ProductionIntent |
    ShapeCuttingIntent | VariableIntent

/** Lossless fallback for a product intent defined by an ICS or a foreign namespace. The constructor takes a
 *  [[ForeignQName]], so a standard XJDF intent name can never be smuggled through the generic fallback; the trait
 *  accessor re-exposes the name as a plain `QualifiedName`.
 */
final case class NamedProductIntent(
    foreignName: ForeignQName,
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  def elementName: QualifiedName = foreignName.qualifiedName

final case class Intent(
    name: Nmtoken,
    productIntent: Option[ProductIntent] = None,
    descriptiveName: Option[XjdfString] = None,
    externalId: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Product(
    amount: Option[Int] = None,
    commentUrl: Option[UriRef] = None,
    descriptiveName: Option[XjdfString] = None,
    externalId: Option[Nmtoken] = None,
    id: Option[XsdId] = None,
    isRoot: Option[Boolean] = None,
    maxAmount: Option[Int] = None,
    minAmount: Option[Int] = None,
    partVersion: Option[Nmtoken] = None,
    productType: Option[Nmtoken] = None,
    productTypeDetails: Option[XjdfString] = None,
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
