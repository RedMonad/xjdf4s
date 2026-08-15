package xjdf4s
package intents

import xjdf4s.prim.*
import cats.kernel.Eq

/** `ColorIntent` (Table 4.20): the color and varnishing of the product. Each
 *  surface SHALL be specified individually in a SurfaceColor element; single
 *  sided printing uses exactly one SurfaceColor.
 */
final case class ColorIntent(
    front: Option[SurfaceColor] = None,
    back: Option[SurfaceColor] = None
)

object ColorIntent:
  given Eq[ColorIntent] = Eq.fromUniversalEquals

/** `SurfaceColor` (Table 4.21): the color configuration of one surface of the
 *  product.
 */
final case class SurfaceColor(
    surface: Side,
    coatings: Option[NmTokens] = None,
    colorsUsed: Option[NmTokens] = None,
    coverage: Option[Coverage] = None,
    printStandard: Option[NmToken] = None
)

object SurfaceColor:
  given Eq[SurfaceColor] = Eq.fromUniversalEquals

/** `ProductionIntent` (Table 4.33): the manufacturing intent — the desired
 *  result or the specified manufacturing path.
 */
final case class ProductionIntent(
    printPreference: Option[PrintPreference] = None,
    printProcess: Option[NmTokens] = None
)

object ProductionIntent:
  given Eq[ProductionIntent] = Eq.fromUniversalEquals
