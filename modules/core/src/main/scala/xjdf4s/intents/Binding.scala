package xjdf4s
package intents

import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/**
 * `BindingIntent` (Table 4.8): the binding intent for a Product. The
 * type-specific detail elements SHALL only be specified together with the
 * matching `@BindingType` — modelled as a union type `BindingDetails`, with
 * the pairing enforced by `validate`.
 */
final case class BindingIntent(
  bindingType: BindingType,
  bindingOrder: Option[BindingOrder] = None,
  bindingSide: Option[Edge] = None,
  childRefs: Option[IdRefs] = None,
  coverColor: Option[NamedColor] = None,
  coverColorDetails: Option[XjdfString] = None,
  bindingColor: Option[NamedColor] = None,
  bindingColorDetails: Option[XjdfString] = None,
  backCoverColor: Option[NamedColor] = None,
  backCoverColorDetails: Option[XjdfString] = None,
  details: Option[BindingDetails] = None,
  tabs: Option[Tabs] = None
):

  def references: Chain[IdRef] = Chain.fromSeq(childRefs.toList.flatMap(_.toList))

  /** The pairing rules between `@BindingType` and the detail elements (Table 4.8). */
  def isLawful: Boolean =
    details match
      case None => true
      case Some(d) =>
        d match
          case _: AdhesiveNote      => bindingType == BindingType.AdhesiveNote
          case _: EdgeGluing        => bindingType == BindingType.EdgeGluing
          case _: HardCoverBinding  => bindingType == BindingType.HardCover
          case _: LooseBinding      => BindingIntent.looseTypes.contains(bindingType)
          case _: SaddleStitching   => bindingType == BindingType.SaddleStitch
          case _: SideStitching     => bindingType == BindingType.SideStitch
          case _: SoftCoverBinding  => bindingType == BindingType.SoftCover

object BindingIntent:

  private val looseTypes: Set[BindingType] =
    Set(BindingType.ChannelBinding, BindingType.CoilBinding, BindingType.CombBinding,
      BindingType.RingBinding, BindingType.StripBinding)

  given Eq[BindingIntent] = Eq.fromUniversalEquals

end BindingIntent

/**
 * `BindingDetails`: the union of the type-specific detail elements of a
 * BindingIntent (Tables 4.9–4.18). Exactly one alternative can be present —
 * the coproduct of the binding details.
 */
type BindingDetails =
  AdhesiveNote | EdgeGluing | HardCoverBinding | LooseBinding | SaddleStitching | SideStitching | SoftCoverBinding

/** Details of adhesive note binding (Table 4.9). */
final case class AdhesiveNote(glue: Option[GlueType] = None)

object AdhesiveNote:
  given Eq[AdhesiveNote] = Eq.fromUniversalEquals

/** Details of EdgeGluing (Table 4.10). */
final case class EdgeGluing(edgeGlue: Option[GlueType] = None)

object EdgeGluing:
  given Eq[EdgeGluing] = Eq.fromUniversalEquals

/** Details of HardCoverBinding (Table 4.11). */
final case class HardCoverBinding(
  blockThreadSewing: Option[Boolean] = None,
  coverStyle: Option[NmToken] = None,
  endSheets: Option[Boolean] = None,
  headBands: Option[Boolean] = None,
  headBandColor: Option[NamedColor] = None,
  headBandColorDetails: Option[XjdfString] = None,
  jacket: Option[HardCoverJacket] = None,
  jacketFoldingWidth: Option[Points] = None,
  japanBind: Option[Boolean] = None,
  spineGlue: Option[GlueType] = None,
  spineOperations: Option[NmTokens] = None,
  thickness: Option[Points] = None,
  tightBacking: Option[TightBacking] = None
)

object HardCoverBinding:
  given Eq[HardCoverBinding] = Eq.fromUniversalEquals

/** Details of LooseBinding (Table 4.12). */
final case class LooseBinding(
  brand: Option[XjdfString] = None,
  diameter: Option[Points] = None,
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
  spineGlue: Option[GlueType] = None,
  spineOperations: Option[NmTokens] = None
)

object SoftCoverBinding:
  given Eq[SoftCoverBinding] = Eq.fromUniversalEquals

/** Tabs in a bound document (Table 4.19). */
final case class Tabs(
  reinforceTabs: Option[Boolean] = None,
  reinforceBind: Option[Boolean] = None,
  reinforceColor: Option[NamedColor] = None,
  reinforceColorDetails: Option[XjdfString] = None,
  tabBrand: Option[XjdfString] = None,
  tabCount: Option[Long] = None,
  tabsPerBank: Option[Long] = None,
  tabExtensionDistance: Option[Points] = None,
  tabBodyCopy: Option[Boolean] = None
)

object Tabs:
  given Eq[Tabs] = Eq.fromUniversalEquals
