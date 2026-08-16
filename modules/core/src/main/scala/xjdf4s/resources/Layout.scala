package xjdf4s
package resources

import xjdf4s.model.elements.FileSpec
import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** The `Layout` resource (Table 6.95): imposition — the placement of source
 *  pages onto a sheet surface. XJDF 2.x merged MIS-level stripping and RIP-level
 *  imposition into this one resource (§1.3.1.7).
 */
final case class Layout(
    anchor: Option[Anchor] = None,
    automated: Option[Boolean] = None,
    filmRef: Option[IdRef] = None,
    innermostShingling: Option[Points] = None,
    maxCollect: Option[Long] = None,
    minCollect: Option[Long] = None,
    outermostShingling: Option[Points] = None,
    paperRef: Option[IdRef] = None,
    plateRef: Option[IdRef] = None,
    proofPaperRef: Option[IdRef] = None,
    sheetLay: Option[SheetLay] = None,
    surfaceContentsBox: Option[Rectangle] = None,
    workStyle: Option[NmToken] = None,
    fitPolicy: Option[FitPolicy] = None,
    fileSpecs: Option[FileSpec] = None
):

  def references: Chain[IdRef] =
    Chain.fromSeq(List(filmRef, paperRef, plateRef, proofPaperRef).flatten)
end Layout

object Layout:

  given Eq[Layout] = Eq.fromUniversalEquals

end Layout
