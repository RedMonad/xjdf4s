package xjdf4s
package intents

import xjdf4s.model.elements.Certification
import xjdf4s.prim.*
import cats.data.Chain
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
 *
 *  `Certification*` *(New in XJDF 2.1)*: "Each Certification SHALL specify a
 *  minimum requested ink certification level. If more than one Certification
 *  is present, at least one of the ink certification levels SHALL be met."
 *  Cardinality `*` (`schema.xsd` `minOccurs="0" maxOccurs="unbounded"`) →
 *  `Chain`; the per-element SHALL is `Certification.law` (M1.6-1, ADR-0012),
 *  the container sentence is a production requirement and is not validated.
 */
final case class SurfaceColor(
    surface: Side,
    coatings: Option[NmTokens] = None,
    colorsUsed: Option[NmTokens] = None,
    coverage: Option[Coverage] = None,
    printStandard: Option[NmToken] = None,
    certifications: Chain[Certification] = Chain.empty
)

object SurfaceColor:
  given Eq[SurfaceColor] = Eq.fromUniversalEquals

/** `ProductionIntent` (Table 4.33): the manufacturing intent — the desired
 *  result or the specified manufacturing path.
 *
 *  `Certification*` *(New in XJDF 2.1)*: "Each Certification SHALL specify a
 *  minimum requested certification level for production. If more than one
 *  Certification is present, at least one of the certification levels SHALL be
 *  met." Cardinality `*` → `Chain` (M1.6-1, see `SurfaceColor`).
 */
final case class ProductionIntent(
    printPreference: Option[PrintPreference] = None,
    printProcess: Option[NmTokens] = None,
    certifications: Chain[Certification] = Chain.empty
)

object ProductionIntent:
  given Eq[ProductionIntent] = Eq.fromUniversalEquals
