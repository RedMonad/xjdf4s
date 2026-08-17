package xjdf4s.model

import xjdf4s.core.*

enum BindingOrder derives CanEqual:
  case None, Collecting, Gathering
end BindingOrder

enum BindingEdge derives CanEqual:
  case Bottom, Left, Right, Top
end BindingEdge

enum BindingGlue derives CanEqual:
  case ColdGlue, Hotmelt, PUR
end BindingGlue

enum TightBacking derives CanEqual:
  case Round, RoundBacked, Flat, FlatBacked
end TightBacking

enum StapleShape derives CanEqual:
  case Butted, ClinchOut, Crown, Eyelet, Overlap
end StapleShape

final case class AdhesiveNoteDetails(
    glue: Option[Glue] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class EdgeGluingDetails(
    edgeGlue: Option[BindingGlue] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class RegisterRibbon(
    lengthOverall: Option[Float] = None,
    material: Option[XjdfString] = None,
    ribbonColor: Option[NamedColor] = None,
    ribbonColorDetails: Option[XjdfString] = None,
    ribbonEnd: Option[Nmtoken] = None,
    visibleLength: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum JacketStyle derives CanEqual:
  case None, Loose, Glue
end JacketStyle

final case class HardCoverBindingDetails(
    blockThreadSewing: Option[Boolean] = None,
    coverStyle: Option[Nmtoken] = None,
    endSheets: Option[Boolean] = None,
    headBands: Option[Boolean] = None,
    headBandColor: Option[NamedColor] = None,
    headBandColorDetails: Option[XjdfString] = None,
    jacket: Option[JacketStyle] = None,
    jacketFoldingWidth: Option[Float] = None,
    japanBind: Option[Boolean] = None,
    spineGlue: Option[BindingGlue] = None,
    spineOperations: Vector[Nmtoken] = Vector.empty,
    thickness: Option[Float] = None,
    tightBacking: Option[TightBacking] = None,
    registerRibbons: Vector[RegisterRibbon] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum BinderMaterial derives CanEqual:
  case Steel, ColorCoatedSteel, Plastic
end BinderMaterial

final case class CoilBindingDetails(
    coilShape: Option[Nmtoken] = None,
    material: Option[BinderMaterial] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CombBindingDetails(
    combShape: Option[Nmtoken] = None,
    material: Option[BinderMaterial] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class RingBindingDetails(
    binderMaterial: Option[Nmtoken] = None,
    ringShape: Option[Nmtoken] = None,
    rivetsExposed: Option[Boolean] = None,
    viewBinder: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

/**
 * The shared attribute set of the `LooseBinding` detail element (Table 4.12) for binding types that have no
 * type-specific child element: ChannelBinding, LooseBinding, StripBinding and WireComb.
 */
final case class LooseBindingDetails(
    brand: Option[XjdfString] = None,
    diameter: Option[Float] = None,
    holePattern: Option[HolePattern] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

/**
 * `LooseBinding` details for CoilBinding. Tables 4.12-4.13: the `CoilBinding` child element SHALL NOT be specified
 * unless `BindingIntent/@BindingType = "CoilBinding"`, which the dedicated detail type makes unrepresentable for the
 * other binding cases.
 */
final case class CoilLooseBindingDetails(
    brand: Option[XjdfString] = None,
    diameter: Option[Float] = None,
    holePattern: Option[HolePattern] = None,
    coilBinding: Option[CoilBindingDetails] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

/** `LooseBinding` details for CombBinding, including the `CombBinding` child (Tables 4.12-4.14). */
final case class CombLooseBindingDetails(
    brand: Option[XjdfString] = None,
    diameter: Option[Float] = None,
    holePattern: Option[HolePattern] = None,
    combBinding: Option[CombBindingDetails] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

/** `LooseBinding` details for RingBinding, including the `RingBinding` child (Tables 4.12-4.15). */
final case class RingLooseBindingDetails(
    brand: Option[XjdfString] = None,
    diameter: Option[Float] = None,
    holePattern: Option[HolePattern] = None,
    ringBinding: Option[RingBindingDetails] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class StitchingDetails(
    stapleShape: Option[StapleShape] = None,
    stitchNumber: Option[Int] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum GlueProcedure derives CanEqual:
  case Spine, SideOnly, SingleSide, SideSpine
end GlueProcedure

enum Scoring derives CanEqual:
  case TwiceScored, QuadScored, None
end Scoring

final case class SoftCoverBindingDetails(
    blockThreadSewing: Option[Boolean] = None,
    endSheets: Option[Boolean] = None,
    foldingWidth: Option[Float] = None,
    foldingWidthBack: Option[Float] = None,
    glueProcedure: Option[GlueProcedure] = None,
    scoring: Option[Scoring] = None,
    spineGlue: Option[BindingGlue] = None,
    spineOperations: Vector[Nmtoken] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Tabs(
    reinforceTabs: Option[Boolean] = None,
    reinforceBind: Option[Boolean] = None,
    reinforceColor: Option[NamedColor] = None,
    reinforceColorDetails: Option[XjdfString] = None,
    tabBrand: Option[XjdfString] = None,
    tabCount: Option[Int] = None,
    tabsPerBank: Option[Int] = None,
    tabExtensionDistance: Option[Float] = None,
    tabBodyCopy: Option[Boolean] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

/**
 * Binding type and its compatible detail element represented by one coproduct. Each case carries the detail ADT that
 * the corresponding table (4.8-4.17) permits, so incompatible nested combinations such as CombBinding details under
 * a Coil binding are unrepresentable. `None`, `Tape` and `CornerStitch` have no detail element.
 */
enum BindingSpecification:
  case AdhesiveNote(details: Option[AdhesiveNoteDetails] = Option.empty)
  case ChannelBinding(details: Option[LooseBindingDetails] = Option.empty)
  case CoilBinding(details: Option[CoilLooseBindingDetails] = Option.empty)
  case CombBinding(details: Option[CombLooseBindingDetails] = Option.empty)
  case CornerStitch
  case EdgeGluing(details: Option[EdgeGluingDetails] = Option.empty)
  case HardCover(details: Option[HardCoverBindingDetails] = Option.empty)
  case LooseBinding(details: Option[LooseBindingDetails] = Option.empty)
  case None
  case RingBinding(details: Option[RingLooseBindingDetails] = Option.empty)
  case SaddleStitch(details: Option[StitchingDetails] = Option.empty)
  case SideStitch(details: Option[StitchingDetails] = Option.empty)
  case SoftCover(details: Option[SoftCoverBindingDetails] = Option.empty)
  case StripBinding(details: Option[LooseBindingDetails] = Option.empty)
  case Tape
  case WireComb(details: Option[LooseBindingDetails] = Option.empty)
end BindingSpecification

final case class BindingIntent(
    binding: BindingSpecification,
    backCoverColor: Option[NamedColor] = None,
    backCoverColorDetails: Option[XjdfString] = None,
    bindingColor: Option[NamedColor] = None,
    bindingColorDetails: Option[XjdfString] = None,
    bindingOrder: Option[BindingOrder] = None,
    bindingSide: Option[BindingEdge] = None,
    childRefs: Option[TwoOrMore[XsdIdRef]] = None,
    coverColor: Option[NamedColor] = None,
    coverColorDetails: Option[XjdfString] = None,
    tabs: Option[Tabs] = None,
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("BindingIntent")
