package xjdf4s
package intents

import xjdf4s.model.{DomainRule, Issue, IssueCode, XPath}
import xjdf4s.model.elements.{Glue as GlueElement, HolePattern}
import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** `BindingIntent` (Table 4.8): the binding intent for a Product. The
 *  type-specific detail elements SHALL only be specified together with the
 *  matching `@BindingType` — modelled as a union type `BindingDetails`, with
 *  the pairing enforced by `validate`.
 *
 *  Color attributes are `NamedColor` (§A.2.30): an open catalog, so they are
 *  typed `NmToken` with recommended values in `Catalog.NamedColor` (ADR-0007).
 */
final case class BindingIntent(
    bindingType: BindingType,
    bindingOrder: Option[BindingOrder] = None,
    bindingSide: Option[Edge] = None,
    childRefs: Option[IdRefs] = None,
    coverColor: Option[NmToken] = None,
    coverColorDetails: Option[XjdfString] = None,
    bindingColor: Option[NmToken] = None,
    bindingColorDetails: Option[XjdfString] = None,
    backCoverColor: Option[NmToken] = None,
    backCoverColorDetails: Option[XjdfString] = None,
    details: Option[BindingDetails] = None,
    tabs: Option[Tabs] = None
):

  def references: Chain[IdRef] =
    Chain.fromSeq(childRefs.toList.flatMap(_.toList)) ++
      details.collect { case an: AdhesiveNote => AdhesiveNote.references(an) }
        .fold(Chain.empty[IdRef])(identity)

  /** The pairing rules between `@BindingType` and the detail elements (Table 4.8),
   *  plus the rule `@BindingSide` SHALL NOT be provided when `@BindingOrder="None"`.
   */
  def isLawful: Boolean =
    detailsPairingOk && bindingSideOk

  private[intents] def detailsPairingOk: Boolean =
    details match
      case None => true
      case Some(d) =>
        d match
          case _: AdhesiveNote => bindingType == BindingType.AdhesiveNote
          case _: EdgeGluing => bindingType == BindingType.EdgeGluing
          case _: HardCoverBinding => bindingType == BindingType.HardCover
          case _: LooseBinding => BindingIntent.looseTypes.contains(bindingType)
          case _: SaddleStitching => bindingType == BindingType.SaddleStitch
          case _: SideStitching => bindingType == BindingType.SideStitch
          case _: SoftCoverBinding => bindingType == BindingType.SoftCover

  /** Table 4.8: `@BindingSide` SHALL NOT be provided if `@BindingOrder="None"`
   *  (i.e. `BindingOrder.Unbound`, whose wire token is `"None"`).
   */
  private[intents] def bindingSideOk: Boolean =
    !(bindingOrder.contains(BindingOrder.Unbound) && bindingSide.isDefined)
end BindingIntent

object BindingIntent:

  private val looseTypes: Set[BindingType] =
    Set(
      BindingType.ChannelBinding,
      BindingType.CoilBinding,
      BindingType.CombBinding,
      BindingType.RingBinding,
      BindingType.StripBinding
    )

  /** Table 4.8 local laws: details ↔ @BindingType pairing and the
   *  @BindingSide / @BindingOrder="None" exclusion. Explicitly invoked from
   *  `TicketValidator.checkIntentLocalLaws`.
   */
  val law: DomainRule[BindingIntent] =
    (value: BindingIntent, at: XPath) =>
      val pairing =
        if value.detailsPairingOk then Chain.empty
        else
          Chain.one(
            Issue.errorC(
              IssueCode.LocalLawViolation,
              at,
              s"BindingIntent details do not match @BindingType=${value.bindingType.token.value} (Table 4.8)"
            )
          )
      val side =
        if value.bindingSideOk then Chain.empty
        else
          Chain.one(
            Issue.errorC(
              IssueCode.LocalLawViolation,
              at,
              "@BindingSide SHALL NOT be provided when @BindingOrder=\"None\" (Table 4.8)"
            )
          )
      pairing ++ side

  given Eq[BindingIntent] = Eq.fromUniversalEquals

end BindingIntent

/** `BindingDetails`: the union of the type-specific detail elements of a
 *  BindingIntent (Tables 4.9–4.18). Exactly one alternative can be present —
 *  the coproduct of the binding details.
 */
type BindingDetails =
  AdhesiveNote | EdgeGluing | HardCoverBinding | LooseBinding | SaddleStitching | SideStitching | SoftCoverBinding

/** Details of adhesive note binding (Table 4.9). */
final case class AdhesiveNote(glue: Option[GlueElement] = None)

object AdhesiveNote:

  /** IDREFs from the contained `Glue/@GlueRef` (Table 8.29). */
  def references(an: AdhesiveNote): Chain[IdRef] =
    an.glue.fold(Chain.empty[IdRef])(GlueElement.references)

  given Eq[AdhesiveNote] = Eq.fromUniversalEquals
end AdhesiveNote

/** Details of EdgeGluing (Table 4.10). */
final case class EdgeGluing(edgeGlue: Option[EnumGlue] = None)

object EdgeGluing:
  given Eq[EdgeGluing] = Eq.fromUniversalEquals

/** Details of HardCoverBinding (Table 4.11).
 *
 *  `@HeadBandColor` is `NamedColor` (§A.2.30): an open catalog typed `NmToken`,
 *  recommended values in `Catalog.NamedColor` (ADR-0007).
 */
final case class HardCoverBinding(
    blockThreadSewing: Option[Boolean] = None,
    coverStyle: Option[NmToken] = None,
    endSheets: Option[Boolean] = None,
    headBands: Option[Boolean] = None,
    headBandColor: Option[NmToken] = None,
    headBandColorDetails: Option[XjdfString] = None,
    jacket: Option[HardCoverJacket] = None,
    jacketFoldingWidth: Option[Points] = None,
    japanBind: Option[Boolean] = None,
    spineGlue: Option[EnumGlue] = None,
    spineOperations: Option[NmTokens] = None,
    thickness: Option[Points] = None,
    tightBacking: Option[TightBacking] = None
)

object HardCoverBinding:
  given Eq[HardCoverBinding] = Eq.fromUniversalEquals

/** Details of LooseBinding (Table 4.12): `HolePattern?` describes the hole
 *  pattern that the binder requires (the media MAY have additional compatible
 *  holes).
 */
final case class LooseBinding(
    brand: Option[XjdfString] = None,
    diameter: Option[Points] = None,
    holePattern: Option[HolePattern] = None,
    coilBinding: Option[CoilBinding] = None,
    combBinding: Option[CombBinding] = None,
    ringBinding: Option[RingBinding] = None
)

object LooseBinding:
  given Eq[LooseBinding] = Eq.fromUniversalEquals

/** Details of CoilBinding (Table 4.13). */
final case class CoilBinding(
    coilShape: Option[NmToken] = None,
    material: Option[NmToken] = None
)

object CoilBinding:
  given Eq[CoilBinding] = Eq.fromUniversalEquals

/** Details of CombBinding (Table 4.14). */
final case class CombBinding(
    combShape: Option[NmToken] = None,
    material: Option[NmToken] = None
)

object CombBinding:
  given Eq[CombBinding] = Eq.fromUniversalEquals

/** Details of RingBinding (Table 4.15). */
final case class RingBinding(
    binderMaterial: Option[NmToken] = None,
    ringShape: Option[NmToken] = None,
    rivetsExposed: Option[Boolean] = None,
    viewBinder: Option[NmToken] = None
)

object RingBinding:
  given Eq[RingBinding] = Eq.fromUniversalEquals

/** Details of SaddleStitching (Table 4.16). */
final case class SaddleStitching(
    stapleShape: Option[StapleShape] = None,
    stitchNumber: Option[Long] = None
)

object SaddleStitching:
  given Eq[SaddleStitching] = Eq.fromUniversalEquals

/** Details of SideStitching (Table 4.17). */
final case class SideStitching(
    stapleShape: Option[StapleShape] = None,
    stitchNumber: Option[Long] = None
)

object SideStitching:
  given Eq[SideStitching] = Eq.fromUniversalEquals

/** Details of SoftCoverBinding (Table 4.18). */
final case class SoftCoverBinding(
    blockThreadSewing: Option[Boolean] = None,
    endSheets: Option[Boolean] = None,
    foldingWidth: Option[Points] = None,
    foldingWidthBack: Option[Points] = None,
    glueProcedure: Option[SoftCoverGlueProcedure] = None,
    scoring: Option[SoftCoverScoring] = None,
    spineGlue: Option[EnumGlue] = None,
    spineOperations: Option[NmTokens] = None
)

object SoftCoverBinding:
  given Eq[SoftCoverBinding] = Eq.fromUniversalEquals

/** Tabs in a bound document (Table 4.19).
 *
 *  `@ReinforceColor` is `NamedColor` (§A.2.30): an open catalog typed `NmToken`,
 *  recommended values in `Catalog.NamedColor` (ADR-0007).
 */
final case class Tabs(
    reinforceTabs: Option[Boolean] = None,
    reinforceBind: Option[Boolean] = None,
    reinforceColor: Option[NmToken] = None,
    reinforceColorDetails: Option[XjdfString] = None,
    tabBrand: Option[XjdfString] = None,
    tabCount: Option[Long] = None,
    tabsPerBank: Option[Long] = None,
    tabExtensionDistance: Option[Points] = None,
    tabBodyCopy: Option[Boolean] = None
)

object Tabs:
  given Eq[Tabs] = Eq.fromUniversalEquals
