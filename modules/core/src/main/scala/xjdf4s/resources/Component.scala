package xjdf4s
package resources

import xjdf4s.model.elements.IdentificationField
import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** The `Component` resource (Table 6.37): the unprinted media, Partial and Final
 *  Products in the press and postpress area. Components reference the Media
 *  they are made of via `@MediaRef`.
 *
 *  `IdentificationField*` (Table 6.37): "IdentificationField associates bar
 *  codes or labels with this Component." Cardinality `*` (`schema.xsd`
 *  `minOccurs="0" maxOccurs="unbounded"`) → `Chain`; the per-element SHALL of
 *  Table 8.31 is `IdentificationField.law` (M1.6-6).
 */
final case class Component(
    automation: Option[Automation] = None,
    cartonTopFlaps: Option[XYPair] = None,
    columns: Option[Long] = None,
    contentRefs: Option[IdRefs] = None,
    dimensions: Option[Shape] = None,
    maxHeat: Option[Double] = None,
    mediaRef: Option[IdRef] = None,
    overfold: Option[Points] = None,
    overfoldSide: Option[Side] = None,
    productType: Option[NmToken] = None,
    productTypeDetails: Option[XjdfString] = None,
    surfaceCount: Option[Long] = None,
    windingResult: Option[Long] = None,
    identificationFields: Chain[IdentificationField] = Chain.empty
):

  /** `@ContentRefs` and `@MediaRef` are the IDREFs of Table 6.37; the nested
   *  `IdentificationField` chain declares none (Table 8.31) but is walked so
   *  the fact stays checked rather than assumed (M1.6-6).
   */
  def references: Chain[IdRef] =
    Chain.fromSeq(contentRefs.toList.flatMap(_.toList) ++ mediaRef.toList) ++
      identificationFields.flatMap(_.references)
end Component

object Component:

  /** A printed component of a given product type, e.g. a booklet. */
  def of(productType: NmToken): Component = Component(productType = Some(productType))

  given Eq[Component] = Eq.fromUniversalEquals

end Component
