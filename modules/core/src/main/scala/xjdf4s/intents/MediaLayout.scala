package xjdf4s
package intents

import xjdf4s.model.elements.Certification
import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** `MediaIntent` (Table 4.32): the media to be used for the Product, described
 *  from the customer's point of view.
 *
 *  Color attributes are `NamedColor` (§A.2.30): an open catalog, so they are
 *  typed `NmToken` with recommended values in `Catalog.NamedColor` (ADR-0007).
 *
 *  `Certification*`: "Each Certification SHALL specify a minimum requested
 *  paper certification level. If more than one Certification is present, at
 *  least one of the paper certification levels SHALL be met." Cardinality `*`
 *  (`schema.xsd` `minOccurs="0" maxOccurs="unbounded"`) → `Chain`; the
 *  per-element SHALL is `Certification.law` (M1.6-1, ADR-0012).
 */
final case class MediaIntent(
    mediaType: MediaType,
    backCoating: Option[Coating] = None,
    backIsoPaperSubstrate: Option[ISOPaperSubstrate] = None,
    brand: Option[XjdfString] = None,
    buyerSupplied: Option[Boolean] = None,
    coating: Option[Coating] = None,
    flute: Option[NmToken] = None,
    fluteDirection: Option[MediaDirection] = None,
    grainDirection: Option[MediaDirection] = None,
    isoPaperSubstrate: Option[ISOPaperSubstrate] = None,
    labColorValue: Option[LabColor] = None,
    mediaColor: Option[NmToken] = None,
    mediaColorDetails: Option[XjdfString] = None,
    mediaQuality: Option[XjdfString] = None,
    mediaTypeDetails: Option[NmToken] = None,
    opacity: Option[Opacity] = None,
    prePrinted: Option[Boolean] = None,
    stockType: Option[NmToken] = None,
    texture: Option[NmToken] = None,
    thickness: Option[Microns] = None,
    weight: Option[Grammage] = None,
    certifications: Chain[Certification] = Chain.empty
)

object MediaIntent:
  given Eq[MediaIntent] = Eq.fromUniversalEquals

/** `LayoutIntent` (Table 4.31): the size of the Finished Pages of the product
 *  component, and how the pages SHALL be imaged onto the finished media.
 */
final case class LayoutIntent(
    bleed: Option[Points] = None,
    dimensions: Option[XYPair] = None,
    finishedDimensions: Option[Shape] = None,
    namedDimensions: Option[NmToken] = None,
    numberUp: Option[XYPair] = None,
    orientation: Option[Orientation] = None,
    pages: Option[Long] = None,
    sides: Option[Sides] = None,
    spreadType: Option[SpreadType] = None
)

object LayoutIntent:
  given Eq[LayoutIntent] = Eq.fromUniversalEquals
