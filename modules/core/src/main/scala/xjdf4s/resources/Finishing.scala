package xjdf4s
package resources

import xjdf4s.intents.{Fold, Perforate}
import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** The `CuttingParams` resource (Table 6.53): the parameter set of a Cutting
 *  process.
 */
final case class CuttingParams(
    sheetLay: Option[SheetLay] = None,
    cutBlocks: Chain[CutBlock] = Chain.empty,
    fileSpecs: Chain[FileSpec] = Chain.empty
)

object CuttingParams:

  given Eq[CuttingParams] = Eq.fromUniversalEquals

end CuttingParams

/** The `CutBlock` element (Table 8.19): exactly one cut block on a sheet, in
 *  the coordinate system of the input Component. The output Component of the
 *  Cutting process SHALL be partitioned by `@BlockName`.
 */
final case class CutBlock(
    blockName: NmToken,
    binderySignatureIds: Option[NmTokens] = None,
    box: Option[Rectangle] = None,
    cutWidth: Option[Points] = None,
    descriptiveName: Option[XjdfString] = None,
    externalId: Option[NmToken] = None,
    operations: Option[NmTokens] = None
)

object CutBlock:

  given Eq[CutBlock] = Eq.fromUniversalEquals

end CutBlock

/** The `FoldingParams` resource (Table 6.74): the parameter set of a Folding
 *  process. The geometry elements `Fold` and `Perforate` are shared with
 *  `FoldingIntent`.
 */
final case class FoldingParams(
    foldCatalog: Option[NmToken] = None,
    foldingDetails: Option[NmToken] = None,
    sheetLay: Option[SheetLay] = None,
    fileSpecs: Chain[FileSpec] = Chain.empty,
    folds: Chain[Fold] = Chain.empty,
    perforates: Chain[Perforate] = Chain.empty
)

object FoldingParams:

  given Eq[FoldingParams] = Eq.fromUniversalEquals

end FoldingParams
