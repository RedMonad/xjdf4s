package xjdf4s
package intents

import xjdf4s.model.{DomainRule, Issue, IssueCode, XPath}
import xjdf4s.model.elements.{Glue => GlueElement}
import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** `FoldingIntent` (Table 4.27): straight line folding, creasing and perforating
 *  of a product — in the coordinate system of the Final Product. Folds implied
 *  by binding SHALL NOT be specified here.
 */
final case class FoldingIntent(
    foldCatalog: Option[NmToken] = None,
    foldingDetails: Option[NmToken] = None,
    orientation: Option[Orientation] = None,
    folds: Chain[Fold] = Chain.empty,
    perforates: Chain[Perforate] = Chain.empty
)

object FoldingIntent:
  given Eq[FoldingIntent] = Eq.fromUniversalEquals

/** `VariableIntent` (Table 4.36): the variations of printed data with variable
 *  content.
 */
final case class VariableIntent(
    variableType: VariableType,
    area: Option[UnitInterval] = None,
    averagePages: Option[Long] = None,
    childRefs: Option[IdRefs] = None,
    colorsUsedBack: Option[NmTokens] = None,
    colorsUsedFront: Option[NmTokens] = None,
    maxPages: Option[Long] = None,
    minPages: Option[Long] = None,
    numberOfCopies: Option[Long] = None,
    variableQuality: Option[VariableQuality] = None
):

  def references: Chain[IdRef] = Chain.fromSeq(childRefs.toList.flatMap(_.toList))

  /** True when `@MinPages <= @AveragePages <= @MaxPages` (Table 4.36). */
  def isLawful: Boolean =
    val okMin = (minPages, averagePages) match
      case (Some(mn), Some(av)) => mn <= av
      case _                     => true
    val okMax = (averagePages, maxPages) match
      case (Some(av), Some(mx)) => av <= mx
      case _                     => true
    okMin && okMax
end VariableIntent

object VariableIntent:

  /** Table 4.36: `@MinPages <= @AveragePages <= @MaxPages`. Explicitly invoked
   *  from `TicketValidator.checkIntentLocalLaws`.
   */
  val law: DomainRule[VariableIntent] =
    (value: VariableIntent, at: XPath) =>
      val minIssue = (value.minPages, value.averagePages) match
        case (Some(mn), Some(av)) if mn > av =>
          Chain.one(
            Issue.errorC(
              IssueCode.LocalLawViolation,
              at,
              s"@MinPages=$mn SHALL NOT be larger than @AveragePages=$av (Table 4.36)"
            )
          )
        case _ => Chain.empty
      val maxIssue = (value.averagePages, value.maxPages) match
        case (Some(av), Some(mx)) if av > mx =>
          Chain.one(
            Issue.errorC(
              IssueCode.LocalLawViolation,
              at,
              s"@MaxPages=$mx SHALL NOT be smaller than @AveragePages=$av (Table 4.36)"
            )
          )
        case _ => Chain.empty
      minIssue ++ maxIssue

  given Eq[VariableIntent] = Eq.fromUniversalEquals

/** `AssemblingIntent` (Table 4.3): placing or inserting one component within
 *  another. `@Container` SHALL reference the main Product — an IDREF, not the
 *  parent product of this intent.
 */
final case class AssemblingIntent(
    container: IdRef,
    assemblyItems: Chain[AssemblyItem] = Chain.empty,
    bindIns: Chain[BindIn] = Chain.empty,
    blowIns: Chain[BlowIn] = Chain.empty,
    stickOns: Chain[StickOn] = Chain.empty
):

  def references: Chain[IdRef] =
    Chain.one(container) ++
      assemblyItems.map(_.childRef) ++
      bindIns.map(_.childRef) ++
      bindIns.flatMap(bi => bi.glue.fold(Chain.empty[IdRef])(GlueElement.references)) ++
      blowIns.map(_.childRef) ++
      stickOns.map(_.childRef) ++
      stickOns.flatMap(so => so.glue.fold(Chain.empty[IdRef])(GlueElement.references))
end AssemblingIntent

object AssemblingIntent:
  given Eq[AssemblingIntent] = Eq.fromUniversalEquals

/** An individual item assembled with the main product (Table 4.4). */
final case class AssemblyItem(childRef: IdRef)

object AssemblyItem:
  given Eq[AssemblyItem] = Eq.fromUniversalEquals

/** An insert that is glued into the main product (Table 4.5). */
final case class BindIn(
    childRef: IdRef,
    folio: Option[Long] = None,
    orientation: Option[Orientation] = None,
    position: Option[XYPair] = None,
    glue: Option[GlueElement] = None
)

object BindIn:
  given Eq[BindIn] = Eq.fromUniversalEquals

/** An insert that is loosely inserted into the main product (Table 4.6). */
final case class BlowIn(
    childRef: IdRef,
    folioFrom: Option[Long] = None,
    folioTo: Option[Long] = None,
    orientation: Option[Orientation] = None
)

object BlowIn:
  given Eq[BlowIn] = Eq.fromUniversalEquals

/** A child product glued onto the main product, e.g. a label (Table 4.7). */
final case class StickOn(
    childRef: IdRef,
    face: Option[Face] = None,
    folio: Option[Long] = None,
    orientation: Option[Orientation] = None,
    position: Option[XYPair] = None,
    glue: Option[GlueElement] = None
)

object StickOn:
  given Eq[StickOn] = Eq.fromUniversalEquals

/** `Fold` (Table 8.26): an individual folding operation. Shared by
 *  `FoldingIntent` and the `FoldingParams` resource.
 */
final case class Fold(
    from: FoldFrom,
    to: FoldTo,
    travel: Option[Points] = None
)

object Fold:
  given Eq[Fold] = Eq.fromUniversalEquals

/** `Perforate` (Table 8.53): one perforated line. Shared by `FoldingIntent` and
 *  `FoldingParams`/`PerforatingParams`.
 */
final case class Perforate(
    depth: Option[Microns] = None,
    startPosition: Option[XYPair] = None,
    teethPerDimension: Option[Double] = None,
    workingDirection: Option[NmToken] = None,
    workingPath: Option[XYPair] = None
)

object Perforate:
  given Eq[Perforate] = Eq.fromUniversalEquals
