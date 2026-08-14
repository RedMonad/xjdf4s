package xjdf4s
package resources

import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/**
 * The `Component` resource (Table 6.37): the unprinted media, Partial and Final
 * Products in the press and postpress area. Components reference the Media
 * they are made of via `@MediaRef`.
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
  windingResult: Option[Long] = None
):

  def references: Chain[IdRef] =
    Chain.fromSeq(contentRefs.toList.flatMap(_.toList) ++ mediaRef.toList)

object Component:

  /** A printed component of a given product type, e.g. a booklet. */
  def of(productType: NmToken): Component = Component(productType = Some(productType))

  given Eq[Component] = Eq.fromUniversalEquals

end Component
