package xjdf4s.model

import xjdf4s.core.*

/** Open XSD extension point with 101 schema-defined descendants plus foreign-namespace descendants. */
trait SpecificResource extends XjdfNode,
      Extensible:
  def elementName: QualifiedName

/** Typed resources completed in the foundational resource slice. */
type FoundationalSpecificResource =
  resources.Assembly | resources.BinderySignature | resources.Bundle | resources.Color | resources.ColorantControl |
    resources.Component | resources.Content |
    resources.Contact | resources.CustomerInfo | resources.Device | resources.DieLayout | resources.ExposedMedia |
    resources.Ink | resources.Layout | resources.Media |
    resources.MiscConsumable |
    resources.NodeInfo | resources.Pallet | resources.PrintCondition | resources.RunList | resources.Shape |
    resources.ShapeDef | resources.Tool | resources.TransferCurve | resources.UsageCounter

type PostpressSpecificResource =
  resources.BlockPreparationParams | resources.BoxFoldingParams | resources.BoxPackingParams |
    resources.BundlingParams | resources.CaseMakingParams | resources.CasingInParams |
    resources.CoverApplicationParams |
    resources.CreasingParams | resources.CuttingParams | resources.EmbossingParams |
    resources.EndSheetGluingParams | resources.FeedingParams | resources.FoldingParams |
    resources.GluingParams | resources.HeadBandApplicationParams | resources.HoleMakingParams |
    resources.InsertingParams |
    resources.JacketingParams | resources.LabelingParams | resources.LaminatingParams |
    resources.LooseBindingParams | resources.PalletizingParams |
    resources.PerforatingParams | resources.ShapeCuttingParams | resources.ShrinkingParams |
    resources.SpinePreparationParams |
    resources.SpineTapingParams | resources.StackingParams | resources.StitchingParams | resources.StrappingParams |
    resources.ThreadSealingParams | resources.ThreadSewingParams | resources.TrimmingParams |
    resources.VarnishingParams | resources.WebInlineFinishingParams | resources.WindingParams |
    resources.WrappingParams

type PrepressSpecificResource =
  resources.BarcodeCompParams | resources.BarcodeReproParams | resources.BendingParams |
    resources.ColorCorrectionParams | resources.ColorSpaceConversionParams | resources.DevelopingParams |
    resources.DieLayoutProductionParams | resources.FontPolicy |
    resources.ImageCompressionParams | resources.ImageEnhancementParams | resources.ImageSetterParams |
    resources.InkZoneCalculationParams | resources.InterpretingParams |
    resources.InkZoneProfile | resources.LayoutElementProductionParams | resources.LayoutShift |
    resources.ManualLaborParams | resources.PDLCreationParams | resources.PreflightParams |
    resources.PreflightReport | resources.PreviewGenerationParams | resources.RasterReadingParams |
    resources.RegisterMark | resources.RenderingParams | resources.ScreeningParams |
    resources.SeparationControlParams | resources.ShapeDefProductionParams | resources.SheetOptimizingParams |
    resources.SheetOptimizingReport | resources.TrappingParams

type GeneralSpecificResource =
  resources.ApprovalDetails | resources.ApprovalParams | resources.DeliveryParams | resources.Preview |
    resources.QualityControlParams | resources.QualityControlResult | resources.VerificationParams |
    resources.VerificationResult

type PressSpecificResource = resources.ConventionalPrintingParams | resources.DigitalPrintingParams

/** Complete union of schema-defined and normative XJDF 2.2 SpecificResource descendants. */
type StandardSpecificResource =
  FoundationalSpecificResource | PostpressSpecificResource | PrepressSpecificResource | GeneralSpecificResource |
    PressSpecificResource

/** Compatibility name retained from the incremental construction phase. */
type TypedSpecificResource = StandardSpecificResource

/** Lossless carrier for ICS and foreign-namespace resources outside the standard XJDF resource union. The
 *  constructor takes a [[ForeignQName]], so a standard XJDF resource name can never be smuggled through the generic
 *  fallback; the trait accessor re-exposes the name as a plain `QualifiedName`.
 */
final case class NamedSpecificResource(
    foreignName: ForeignQName,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  def elementName: QualifiedName = foreignName.qualifiedName

enum WasteOrigin:
  case Modules(moduleIds: NonEmptyVector[Nmtoken])
  case Details(wasteDetails: Nmtoken)
  case ModulesAndDetails(moduleIds: NonEmptyVector[Nmtoken], wasteDetails: Nmtoken)

/** `WasteOrigin` makes the table 6.5 at-least-one-of constraint unrepresentable as an invalid state. */
final case class PartWaste(
    waste: Float,
    origin: WasteOrigin,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class PartAmount(
    amount: Option[Float] = None,
    maxAmount: Option[Float] = None,
    minAmount: Option[Float] = None,
    waste: Option[Float] = None,
    parts: Vector[Part] = Vector.empty,
    partWaste: Vector[PartWaste] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class AmountPool(
    amounts: NonEmptyVector[PartAmount],
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Resource(
    amountPool: Option[AmountPool] = None,
    comments: Vector[Comment] = Vector.empty,
    generalIds: Vector[GeneralId] = Vector.empty,
    parts: Vector[Part] = Vector.empty,
    specificResource: Option[SpecificResource] = None,
    foreignElements: Vector[ExtensionElement] = Vector.empty,
    brand: Option[XjdfString] = None,
    commentUrl: Option[UriRef] = None,
    descriptiveName: Option[XjdfString] = None,
    duration: Option[XsdDuration] = None,
    expires: Option[XsdDateTime] = None,
    externalId: Option[Nmtoken] = None,
    grossWeight: Option[Float] = None,
    id: Option[XsdId] = None,
    orientation: Option[Orientation] = None,
    resourceWeight: Option[Float] = None,
    start: Option[XsdDateTime] = None,
    startOffset: Option[XsdDuration] = None,
    status: Option[ResourceAvailability] = None,
    transformation: Option[Matrix] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible,
      ValidatedNode:

  override def validate: Vector[ValidationError] =
    val placementErrors =
      if orientation.nonEmpty && transformation.nonEmpty then
        Vector(
          ValidationError.ConflictingValues(
            Vector("Resource/@Orientation", "Resource/@Transformation"),
            "the two placement attributes are mutually exclusive",
          ),
        )
      else Vector.empty
    val timingErrors =
      if start.nonEmpty && startOffset.nonEmpty then
        Vector(
          ValidationError.ConflictingValues(
            Vector("Resource/@Start", "Resource/@StartOffset"),
            "the two timing attributes are mutually exclusive",
          ),
        )
      else Vector.empty
    placementErrors ++ timingErrors
end Resource

final case class Dependent(
    jobId: Nmtoken,
    jobPartId: Option[Nmtoken] = None,
    pipeId: Option[Nmtoken] = None,
    pipeProtocol: Option[Nmtoken] = None,
    xjmfUrl: Option[UriRef] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ResourceSet(
    name: Nmtoken,
    combinedProcessIndex: Vector[Int] = Vector.empty,
    commentUrl: Option[UriRef] = None,
    descriptiveName: Option[XjdfString] = None,
    id: Option[XsdId] = None,
    processUsage: Option[Nmtoken] = None,
    unit: Option[Nmtoken] = None,
    usage: Option[ResourceUsage] = None,
    comments: Vector[Comment] = Vector.empty,
    dependents: Vector[Dependent] = Vector.empty,
    generalIds: Vector[GeneralId] = Vector.empty,
    resources: Vector[Resource] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible
