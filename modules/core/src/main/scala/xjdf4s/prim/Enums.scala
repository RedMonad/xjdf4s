package xjdf4s
package prim

import cats.Show
import cats.kernel.Eq

/** Common surface of closed XJDF enumerations (data type `enumeration`,
 *  Table A.1): every value has a machine-readable token, and the companion
 *  provides `Show`, `Eq` and `fromToken` uniformly.
 */
trait XjdfEnum:
  /** The machine-readable token of this value, exactly as written in XJDF. */
  def token: NmToken

/** Mix-in for enumeration companions.
 *
 *  The member is called `all` (not `values`): every Scala 3 enum companion
 *  already defines a synthetic `values: Array[E]`, and the two would clash
 *  after erasure.
 */
trait XjdfEnumCompanion[E <: XjdfEnum]:
  /** All values, in specification order. */
  def all: List[E]

  def fromToken(token: NmToken): Option[E] = all.find(_.token == token)

  given Show[E] = Show.show(_.token.value)

  given Eq[E] = Eq.fromUniversalEquals
end XjdfEnumCompanion

/** `@Usage`: a resource is either consumed or produced (§3.4, Table A.2.48). */
enum Usage extends XjdfEnum:
  case Input, Output
  def token: NmToken = NmToken.unsafe(this.toString)

object Usage extends XjdfEnumCompanion[Usage]:
  val all: List[Usage] = List(Input, Output)

/** `Side`: the side of a sheet or surface (Table A.2.39). */
enum Side extends XjdfEnum:
  case Front, Back
  def token: NmToken = NmToken.unsafe(this.toString)

object Side extends XjdfEnumCompanion[Side]:
  val all: List[Side] = List(Front, Back)

/** `Sides`: which sides of the product SHALL be imaged (§A.2.40 / Table A.40).
 *
 *  `Unprinted` is *New in XJDF 2.1*: “Page contents SHALL NOT be imposed on
 *  either side.” Note that `schema.xsd` still lists only four values; per the
 *  source-of-truth order (ROADMAP §1.2) the normative text wins (ADR-0007).
 */
enum Sides extends XjdfEnum:
  case OneSided, OneSidedBack, TwoSidedHeadToFoot, TwoSidedHeadToHead, Unprinted
  def token: NmToken = NmToken.unsafe(this.toString)

object Sides extends XjdfEnumCompanion[Sides]:
  val all: List[Sides] = List(OneSided, OneSidedBack, TwoSidedHeadToFoot, TwoSidedHeadToHead, Unprinted)

/** `Edge`: an edge of a product in its coordinate system (Table A.2.16). */
enum Edge extends XjdfEnum:
  case Bottom, Left, Right, Top
  def token: NmToken = NmToken.unsafe(this.toString)

object Edge extends XjdfEnumCompanion[Edge]:
  val all: List[Edge] = List(Bottom, Left, Right, Top)

/** `Face`: a named position on a product (Table A.2.19). */
enum Face extends XjdfEnum:
  case Back, Bottom, Front, Left, Right, Top
  def token: NmToken = NmToken.unsafe(this.toString)

object Face extends XjdfEnumCompanion[Face]:
  val all: List[Face] = List(Back, Bottom, Front, Left, Right, Top)

/** `Orientation`: named orientation of a resource or product (Table A.2.32).
 *  The names reflect the state of the resource, not the order of applied
 *  transformations (Table 2.1).
 */
enum Orientation extends XjdfEnum:
  case Rotate0, Rotate90, Rotate180, Rotate270, Flip0, Flip90, Flip180, Flip270

  def token: NmToken = NmToken.unsafe(this.toString)

  /** The transformation matrix of this orientation, per Table 2.1.
   *  `w`/`h` are the width and height of the Component.
   */
  def matrix(w: Double, h: Double): Matrix =
    this match
      case Rotate0 => Matrix(1, 0, 0, 1, 0, 0)
      case Rotate90 => Matrix(0, 1, -1, 0, h, 0)
      case Rotate180 => Matrix(-1, 0, 0, -1, w, h)
      case Rotate270 => Matrix(0, -1, 1, 0, 0, w)
      case Flip0 => Matrix(1, 0, 0, -1, 0, h)
      case Flip90 => Matrix(0, -1, -1, 0, h, w)
      case Flip180 => Matrix(-1, 0, 0, 1, w, 0)
      case Flip270 => Matrix(0, 1, 1, 0, 0, 0)
end Orientation

object Orientation extends XjdfEnumCompanion[Orientation]:
  val all: List[Orientation] =
    List(Rotate0, Rotate90, Rotate180, Rotate270, Flip0, Flip90, Flip180, Flip270)

/** `Status`: the state of a process or queue entry (Table A.2.45). */
enum Status extends XjdfEnum:
  case Aborted, Cleanup, Completed, InProgress, Setup, Stopped, Suspended, Waiting
  def token: NmToken = NmToken.unsafe(this.toString)

object Status extends XjdfEnumCompanion[Status]:
  val all: List[Status] = List(Aborted, Cleanup, Completed, InProgress, Setup, Stopped, Suspended, Waiting)

/** `DeviceStatus`: the overall status of a Device (§A.2.14 / Table A.15).
 *
 *  `Cleanup` and `Setup` are *New in XJDF 2.1*. Both names also exist in the
 *  neighbouring `Status` enumeration (Table A.46); the two are distinct XJDF
 *  types, so members are always referred to with an explicit qualifier —
 *  `DeviceStatus.Setup` vs `Status.Setup` — rather than dropped or aliased
 *  (ADR-0007).
 */
enum DeviceStatus extends XjdfEnum:
  case Cleanup, Idle, NonProductive, Offline, Production, Setup, Stopped
  def token: NmToken = NmToken.unsafe(this.toString)

object DeviceStatus extends XjdfEnumCompanion[DeviceStatus]:
  val all: List[DeviceStatus] =
    List(Cleanup, Idle, NonProductive, Offline, Production, Setup, Stopped)

/** `Severity`: class of a notification (Table A.2.37). */
enum SeverityClass extends XjdfEnum:
  case Event, Information, Warning, Error, Fatal
  def token: NmToken = NmToken.unsafe(this.toString)

object SeverityClass extends XjdfEnumCompanion[SeverityClass]:
  val all: List[SeverityClass] = List(Event, Information, Warning, Error, Fatal)

/** `BindingType`: the required style of binding (Table A.2.7). */
enum BindingType extends XjdfEnum:
  case AdhesiveNote, ChannelBinding, CoilBinding, CombBinding, CornerStitch, EdgeGluing,
    HardCover, LooseBinding, RingBinding, SaddleStitch, SideStitch, SoftCover,
    StripBinding, Tape, WireComb, NoBinding

  def token: NmToken = this match
    case NoBinding => NmToken.unsafe("None")
    case other => NmToken.unsafe(other.toString)
end BindingType

object BindingType extends XjdfEnumCompanion[BindingType]:
  val all: List[BindingType] =
    List(
      AdhesiveNote,
      ChannelBinding,
      CoilBinding,
      CombBinding,
      CornerStitch,
      EdgeGluing,
      HardCover,
      LooseBinding,
      NoBinding,
      RingBinding,
      SaddleStitch,
      SideStitch,
      SoftCover,
      StripBinding,
      Tape,
      WireComb
    )

/** `BindingOrder`: whether child products are collected or gathered (§4.3). */
enum BindingOrder extends XjdfEnum:
  case Unbound, Collecting, Gathering
  def token: NmToken = this match
    case Unbound => NmToken.unsafe("None")
    case other => NmToken.unsafe(other.toString)

object BindingOrder extends XjdfEnumCompanion[BindingOrder]:
  val all: List[BindingOrder] = List(Unbound, Collecting, Gathering)

/** `StapleShape`: the shape of staples used for stitching (Table A.2.44). */
enum StapleShape extends XjdfEnum:
  case Butted, ClinchOut, Crown, Eyelet, Overlap
  def token: NmToken = NmToken.unsafe(this.toString)

object StapleShape extends XjdfEnumCompanion[StapleShape]:
  val all: List[StapleShape] = List(Butted, ClinchOut, Crown, Eyelet, Overlap)

/** `Glue`: glue types (Table A.2.23). */
enum GlueType extends XjdfEnum:
  case ColdGlue, Hotmelt, PUR
  def token: NmToken = NmToken.unsafe(this.toString)

object GlueType extends XjdfEnumCompanion[GlueType]:
  val all: List[GlueType] = List(ColdGlue, Hotmelt, PUR)

/** `TightBacking`: the geometry of the back of a book block (Table A.2.46). */
enum TightBacking extends XjdfEnum:
  case Round, RoundBacked, Flat, FlatBacked
  def token: NmToken = NmToken.unsafe(this.toString)

object TightBacking extends XjdfEnumCompanion[TightBacking]:
  val all: List[TightBacking] = List(Round, RoundBacked, Flat, FlatBacked)

/** `Coating`: the pre-process coating of media (Table A.2.10). */
enum Coating extends XjdfEnum:
  case Coated, Gloss, Matte, Uncoated, Satin
  def token: NmToken = this match
    case Uncoated => NmToken.unsafe("None")
    case other => NmToken.unsafe(other.toString)

object Coating extends XjdfEnumCompanion[Coating]:
  val all: List[Coating] = List(Coated, Gloss, Matte, Uncoated, Satin)

/** `Opacity`: the opacity of media (Table A.2.31). */
enum Opacity extends XjdfEnum:
  case Opaque, Translucent, Transparent
  def token: NmToken = NmToken.unsafe(this.toString)

object Opacity extends XjdfEnumCompanion[Opacity]:
  val all: List[Opacity] = List(Opaque, Translucent, Transparent)

/** `MediaDirection`: direction relative to the product coordinate system (Table A.2.28). */
enum MediaDirection extends XjdfEnum:
  case Any, SameDirection, XDirection, YDirection
  def token: NmToken = NmToken.unsafe(this.toString)

object MediaDirection extends XjdfEnumCompanion[MediaDirection]:
  val all: List[MediaDirection] = List(Any, SameDirection, XDirection, YDirection)

/** `ISOPaperSubstrate`: print substrate classification (§A.2.25 / Table A.26).
 *
 *  `LWCPlus`, `LWCStandard`, `NewsPlus`, `SCPlus`, `SCStandard` and `SNP` are
 *  *New in XJDF 2.1* (from `[ISO12647-4:2014]` and `[ISO12647-3:2013]`); `PS9`
 *  is *New in XJDF 2.2*.
 */
enum ISOPaperSubstrate extends XjdfEnum:
  case LWCPlus, LWCStandard, NewsPlus, PS1, PS2, PS3, PS4, PS5, PS6, PS7, PS8, PS9,
    SCPlus, SCStandard, SNP
  def token: NmToken = NmToken.unsafe(this.toString)

object ISOPaperSubstrate extends XjdfEnumCompanion[ISOPaperSubstrate]:
  val all: List[ISOPaperSubstrate] =
    List(
      LWCPlus,
      LWCStandard,
      NewsPlus,
      PS1,
      PS2,
      PS3,
      PS4,
      PS5,
      PS6,
      PS7,
      PS8,
      PS9,
      SCPlus,
      SCStandard,
      SNP
    )

/** `MediaType`: the medium being employed (§A.2.29 / Table A.30).
 *
 *  `Synthetic` is *New in XJDF 2.1*. The values `EmbossingFoil`, `Foil`,
 *  `LaminatingFoil`, `MountingTape`, `SelfAdhesive` and `Vinyl` are *Deprecated
 *  in XJDF 2.1* and `ShrinkFoil` is *Deprecated in XJDF 2.2*; deprecated values
 *  remain part of the closed set because a decoder SHALL still be able to read
 *  documents that use them (ADR-0007, ADR-0010). They are marked in prose only:
 *  a Scala `@deprecated` annotation would make the `all` listing itself emit
 *  warnings, and the build runs warning-free by policy.
 */
enum MediaType extends XjdfEnum:
  case Blanket, CorrugatedBoard, Disc, EmbossingFoil, Film, Foil, GravureCylinder,
    ImagingCylinder, LaminatingFoil, MountingTape, Other, Paper, Plate, Screen,
    SelfAdhesive, ShrinkFoil, Sleeve, Synthetic, Textile, Transparency, Vinyl
  def token: NmToken = NmToken.unsafe(this.toString)

object MediaType extends XjdfEnumCompanion[MediaType]:
  val all: List[MediaType] =
    List(
      Blanket,
      CorrugatedBoard,
      Disc,
      EmbossingFoil,
      Film,
      Foil,
      GravureCylinder,
      ImagingCylinder,
      LaminatingFoil,
      MountingTape,
      Other,
      Paper,
      Plate,
      Screen,
      SelfAdhesive,
      ShrinkFoil,
      Sleeve,
      Synthetic,
      Textile,
      Transparency,
      Vinyl
    )

/** `Automation`: dynamic or static components (Table A.2.4). */
enum Automation extends XjdfEnum:
  case Dynamic, Static
  def token: NmToken = NmToken.unsafe(this.toString)

object Automation extends XjdfEnumCompanion[Automation]:
  val all: List[Automation] = List(Dynamic, Static)

/** `SheetLay`: the lay of the sheet relative to the machine (Table A.2.38). */
enum SheetLay extends XjdfEnum:
  case Center, Left, Right
  def token: NmToken = NmToken.unsafe(this.toString)

object SheetLay extends XjdfEnumCompanion[SheetLay]:
  val all: List[SheetLay] = List(Center, Left, Right)

// `NamedColor` (§A.2.30) is deliberately NOT a closed enum: its values are
// defined by the external catalog `[Color Names]`, so it is modelled as an open
// `NmToken` with recommended values in `Catalog.NamedColor` (ADR-0007, N-09).

/** `EndStatus`: the `NodeInfo/@Status` of a workstep at the end of its run
 *  (`ProcessRun/@EndStatus`, Table 3.7).
 */
enum EndStatus extends XjdfEnum:
  case Aborted, Completed
  def token: NmToken = NmToken.unsafe(this.toString)

object EndStatus extends XjdfEnumCompanion[EndStatus]:
  val all: List[EndStatus] = List(Aborted, Completed)

/** `PrintPreference`: the manufacturing goal (Table 4.33). */
enum PrintPreference extends XjdfEnum:
  case Balanced, CostEffective, Fastest, HighestQuality
  def token: NmToken = NmToken.unsafe(this.toString)

object PrintPreference extends XjdfEnumCompanion[PrintPreference]:
  val all: List[PrintPreference] = List(Balanced, CostEffective, Fastest, HighestQuality)

/** `PreflightLevel`: level of content data checking (Table 4.23). */
enum PreflightLevel extends XjdfEnum:
  case Basic, Extended, Premium
  def token: NmToken = NmToken.unsafe(this.toString)

object PreflightLevel extends XjdfEnumCompanion[PreflightLevel]:
  val all: List[PreflightLevel] = List(Basic, Extended, Premium)

/** `PreviewType`: the type and usage of a Preview (Table 6.4). */
enum PreviewType extends XjdfEnum:
  case Animation, Identification, SeparatedThumbNail, Separation, SeparationRaw,
    Static3D, ThumbNail, Viewable
  def token: NmToken = NmToken.unsafe(this.toString)

object PreviewType extends XjdfEnumCompanion[PreviewType]:
  val all: List[PreviewType] =
    List(
      Animation,
      Identification,
      SeparatedThumbNail,
      Separation,
      SeparationRaw,
      Static3D,
      ThumbNail,
      Viewable
    )

/** `TransferCurveName`: the destination system a TransferCurve applies to (Table 6.4). */
enum TransferCurveTarget extends XjdfEnum:
  case Film, Plate, Press, Proof, Substrate
  def token: NmToken = NmToken.unsafe(this.toString)

object TransferCurveTarget extends XjdfEnumCompanion[TransferCurveTarget]:
  val all: List[TransferCurveTarget] = List(Film, Plate, Press, Proof, Substrate)

/** `VariableType`: the type of variable content (Table 4.36). */
enum VariableType extends XjdfEnum:
  case OneLine, AddressField, IdentificationField, Area
  def token: NmToken = NmToken.unsafe(this.toString)

object VariableType extends XjdfEnumCompanion[VariableType]:
  val all: List[VariableType] = List(OneLine, AddressField, IdentificationField, Area)

/** `VariableQuality`: the desired quality of the variable data (Table 4.36). */
enum VariableQuality extends XjdfEnum:
  case Simple, Imprint, Full
  def token: NmToken = NmToken.unsafe(this.toString)

object VariableQuality extends XjdfEnumCompanion[VariableQuality]:
  val all: List[VariableQuality] = List(Simple, Imprint, Full)

/** `ColorType`: a name that characterizes the colorant (Color resource, §6.14). */
enum ColorType extends XjdfEnum:
  case DieLine, Normal, Opaque, OpaqueIgnore, Primer, Transparent
  def token: NmToken = NmToken.unsafe(this.toString)

object ColorType extends XjdfEnumCompanion[ColorType]:
  val all: List[ColorType] = List(DieLine, Normal, Opaque, OpaqueIgnore, Primer, Transparent)

/** `SpreadType`: treatment of individual PDF pages for imposition (Table A.2.43). */
enum SpreadType extends XjdfEnum:
  case SinglePage, Spread
  def token: NmToken = NmToken.unsafe(this.toString)

object SpreadType extends XjdfEnumCompanion[SpreadType]:
  val all: List[SpreadType] = List(SinglePage, Spread)

/** `Scope`: the context of resources defined in a ResourceInfo (§A.2.36 /
 *  Table A.36).
 *
 *  `Device` is *New in XJDF 2.2*: “The amount of resources is an absolute
 *  measurement that is currently available within the scope of a Device.” The
 *  case shares its name with the `Device` resource, so it is always written
 *  qualified as `Scope.Device`; `schema.xsd` still lists only four values and
 *  lags the normative text here (ADR-0007).
 */
enum Scope extends XjdfEnum:
  case Allowed, Device, Estimate, Job, Present
  def token: NmToken = NmToken.unsafe(this.toString)

object Scope extends XjdfEnumCompanion[Scope]:
  val all: List[Scope] = List(Allowed, Device, Estimate, Job, Present)

/** `FitPolicy`: how artwork is fitted into its target box (Table A.2.21). */
enum FitPolicy extends XjdfEnum:
  case NoRepeat, RepeatToFill, RepeatUnclipped, StretchToFit, UndistortedScaleToFit
  def token: NmToken = NmToken.unsafe(this.toString)

object FitPolicy extends XjdfEnumCompanion[FitPolicy]:
  val all: List[FitPolicy] = List(NoRepeat, RepeatToFill, RepeatUnclipped, StretchToFit, UndistortedScaleToFit)

/** `Anchor`: the reference point of a placed object (Table A.2.3). */
enum Anchor extends XjdfEnum:
  case BottomCenter, BottomLeft, BottomRight, Center, CenterLeft, CenterRight,
    TopCenter, TopLeft, TopRight
  def token: NmToken = NmToken.unsafe(this.toString)

object Anchor extends XjdfEnumCompanion[Anchor]:
  val all: List[Anchor] =
    List(
      BottomCenter,
      BottomLeft,
      BottomRight,
      Center,
      CenterLeft,
      CenterRight,
      TopCenter,
      TopLeft,
      TopRight
    )

/** `CommandResult`: the result of a CommandResource (Table 7.53). */
enum CommandResult extends XjdfEnum:
  case Merged, New, Rejected, Removed, Replaced
  def token: NmToken = NmToken.unsafe(this.toString)

object CommandResult extends XjdfEnumCompanion[CommandResult]:
  val all: List[CommandResult] = List(Merged, New, Rejected, Removed, Replaced)

/** `Level`: the level of a consumable or output bin (Table 7.53). */
enum ResourceLevel extends XjdfEnum:
  case Empty, Full, High, Low, OK
  def token: NmToken = NmToken.unsafe(this.toString)

object ResourceLevel extends XjdfEnumCompanion[ResourceLevel]:
  val all: List[ResourceLevel] = List(Empty, Full, High, Low, OK)

/** `OverwritePolicy`: the policy when an output file already exists (Table 8.22). */
enum OverwritePolicy extends XjdfEnum:
  case Abort, NewVersion, OperatorIntervention, Overwrite, RenameNew, RenameOld
  def token: NmToken = NmToken.unsafe(this.toString)

object OverwritePolicy extends XjdfEnumCompanion[OverwritePolicy]:
  val all: List[OverwritePolicy] = List(Abort, NewVersion, OperatorIntervention, Overwrite, RenameNew, RenameOld)

/** `DispositionAction`: what to do with an asset at disposition time (Table 8.23). */
enum DispositionAction extends XjdfEnum:
  case Archive, Delete
  def token: NmToken = NmToken.unsafe(this.toString)

object DispositionAction extends XjdfEnumCompanion[DispositionAction]:
  val all: List[DispositionAction] = List(Archive, Delete)

/** `WasteDetails`: how waste was produced (Table 6.6). */
enum WasteDetail extends XjdfEnum:
  case AuxiliarySheet, BadFeedWaste, BindingQualityTest, CaliperWaste, DoubleFeedWaste,
    IncorrectComponentWaste, ObliqueSheetWaste, Overrun, PaperJamWaste, Rejected,
    Reusable, Waste, WhitePaperWaste
  def token: NmToken = NmToken.unsafe(this.toString)

object WasteDetail extends XjdfEnumCompanion[WasteDetail]:
  val all: List[WasteDetail] =
    List(
      AuxiliarySheet,
      BadFeedWaste,
      BindingQualityTest,
      CaliperWaste,
      DoubleFeedWaste,
      IncorrectComponentWaste,
      ObliqueSheetWaste,
      Overrun,
      PaperJamWaste,
      Rejected,
      Reusable,
      Waste,
      WhitePaperWaste
    )

/** `FoldFrom`/`FoldTo`: geometry of a fold (Table 8.26). */
enum FoldFrom extends XjdfEnum:
  case Front, Left
  def token: NmToken = NmToken.unsafe(this.toString)

object FoldFrom extends XjdfEnumCompanion[FoldFrom]:
  val all: List[FoldFrom] = List(Front, Left)

enum FoldTo extends XjdfEnum:
  case Up, Down
  def token: NmToken = NmToken.unsafe(this.toString)

object FoldTo extends XjdfEnumCompanion[FoldTo]:
  val all: List[FoldTo] = List(Up, Down)

/** `Resource/@Status`: availability of a resource for processing (Table 6.1). */
enum ResourceStatus extends XjdfEnum:
  case Available, Unavailable
  def token: NmToken = NmToken.unsafe(this.toString)

object ResourceStatus extends XjdfEnumCompanion[ResourceStatus]:
  val all: List[ResourceStatus] = List(Available, Unavailable)

/** `SoftCoverBinding/@GlueProcedure` (Table 4.18). */
enum SoftCoverGlueProcedure extends XjdfEnum:
  case Spine, SideOnly, SingleSide, SideSpine
  def token: NmToken = NmToken.unsafe(this.toString)

object SoftCoverGlueProcedure extends XjdfEnumCompanion[SoftCoverGlueProcedure]:
  val all: List[SoftCoverGlueProcedure] = List(Spine, SideOnly, SingleSide, SideSpine)

/** `SoftCoverBinding/@Scoring` (Table 4.18). */
enum SoftCoverScoring extends XjdfEnum:
  case TwiceScored, QuadScored, Unscored
  def token: NmToken = this match
    case Unscored => NmToken.unsafe("None")
    case other => NmToken.unsafe(other.toString)

object SoftCoverScoring extends XjdfEnumCompanion[SoftCoverScoring]:
  val all: List[SoftCoverScoring] = List(TwiceScored, QuadScored, Unscored)

/** `HardCoverBinding/@Jacket` (§4.3.3 / Table 4.11, Sheet 1): whether a
 *  hardcover jacket is needed and how it is attached.
 *
 *  Two of the three Scala names differ from their wire tokens — `None` is
 *  `scala.None` and `Glue` reads as a verb — so every token is written out
 *  explicitly. There is deliberately no `case other => …` fallback: it is what
 *  silently produced the non-normative token `"Glued"` (ROADMAP N-08, ADR-0007).
 */
enum HardCoverJacket extends XjdfEnum:
  case Unjacketed, Loose, GlueApplied
  def token: NmToken = this match
    case Unjacketed  => NmToken.unsafe("None")
    case Loose       => NmToken.unsafe("Loose")
    case GlueApplied => NmToken.unsafe("Glue")
end HardCoverJacket

object HardCoverJacket extends XjdfEnumCompanion[HardCoverJacket]:
  val all: List[HardCoverJacket] = List(Unjacketed, Loose, GlueApplied)
