package xjdf4s
package resources

import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** The `Media` resource (Table 6.114): a physical medium — paper, film, plate,
 *  textile — consumed by a process. The counterpart of `MediaIntent`.
 *
 *  Color attributes are `NamedColor` (§A.2.30): an open catalog, so they are
 *  typed `NmToken` with recommended values in `Catalog.NamedColor` (ADR-0007).
 */
final case class Media(
    mediaType: MediaType,
    backBrightness: Option[Double] = None,
    backCoating: Option[Coating] = None,
    backCoatingDetail: Option[NmToken] = None,
    backGlossValue: Option[Double] = None,
    backIsoPaperSubstrate: Option[ISOPaperSubstrate] = None,
    brightness: Option[Double] = None,
    coating: Option[Coating] = None,
    coatingDetail: Option[NmToken] = None,
    coreWeight: Option[Grammage] = None,
    dimension: Option[XYPair] = None,
    flute: Option[NmToken] = None,
    fluteDirection: Option[MediaDirection] = None,
    glossValue: Option[Double] = None,
    grainDirection: Option[MediaDirection] = None,
    innerCoreDiameter: Option[Microns] = None,
    insideLoss: Option[Microns] = None,
    isoPaperSubstrate: Option[ISOPaperSubstrate] = None,
    mediaColor: Option[NmToken] = None,
    mediaColorDetails: Option[XjdfString] = None,
    mediaQuality: Option[XjdfString] = None,
    mediaTypeDetails: Option[NmToken] = None,
    opacity: Option[Opacity] = None,
    prePrinted: Option[Boolean] = None,
    stockType: Option[NmToken] = None,
    texture: Option[NmToken] = None,
    thickness: Option[Microns] = None,
    weight: Option[Grammage] = None
):

  def references: Chain[IdRef] = Chain.empty
end Media

object Media:

  /** The common case: `Media[@MediaType="Paper"]` with a weight. */
  def paper(weight: Grammage): Media = Media(MediaType.Paper, weight = Some(weight))

  given Eq[Media] = Eq.fromUniversalEquals

end Media
