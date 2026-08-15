package xjdf4s
package resources

import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** The `DeliveryParams` resource (Table 6.54): the details of individual
 *  deliveries, partitioned by `Part/@DropID` for split deliveries (§5.3.2).
 */
final case class DeliveryParams(
    buyerAccount: Option[XjdfString] = None,
    earliest: Option[Timestamp] = None,
    earliestDuration: Option[TimeSpan] = None,
    method: Option[NmToken] = None,
    required: Option[Timestamp] = None,
    requiredDuration: Option[TimeSpan] = None,
    trackingId: Option[NmToken] = None,
    dropItems: Chain[DropItem] = Chain.empty
):

  def references: Chain[IdRef] = dropItems.map(_.itemRef)
end DeliveryParams

object DeliveryParams:

  given Eq[DeliveryParams] = Eq.fromUniversalEquals

end DeliveryParams

/** The `DropItem` element (Table 6.55): an item of one delivery — `@Amount`
 *  copies of the product referenced by `@ItemRef`.
 */
final case class DropItem(
    amount: Long,
    itemRef: IdRef,
    totalDimensions: Option[Shape] = None,
    totalVolume: Option[Double] = None,
    totalWeight: Option[Double] = None
)

object DropItem:
  given Eq[DropItem] = Eq.fromUniversalEquals
