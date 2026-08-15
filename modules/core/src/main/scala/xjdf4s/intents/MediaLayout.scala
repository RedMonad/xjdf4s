package xjdf4s
package intents

import xjdf4s.prim.*
import cats.kernel.Eq

/**
 * `MediaIntent` (Table 4.32): the media to be used for the Product, described
 * from the customer's point of view.
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
  mediaColor: Option[NamedColor] = None,
  mediaColorDetails: Option[XjdfString] = None,
  mediaQuality: Option[XjdfString] = None,
  mediaTypeDetails: Option[NmToken] = None,
  opacity: Option[Opacity] = None,
  prePrinted: Option[Boolean] = None,
  stockType: Option[NmToken] = None,
  texture: Option[NmToken] = None,
  thickness: Option[Microns] = None,
  weight: Option[Grammage] = None
)

object MediaIntent:
  given Eq[MediaIntent] = Eq.fromUniversalEquals

/**
 * `LayoutIntent` (Table 4.31): the size of the Finished Pages of the product
 * component, and how the pages SHALL be imaged onto the finished media.
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
