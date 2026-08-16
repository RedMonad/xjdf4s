package xjdf4s.model

import xjdf4s.core.*

/** Open XSD extension point with 100 schema-defined descendants plus foreign-namespace descendants. */
trait SpecificResource extends XjdfNode,
      Extensible:
  def elementName: QualifiedName
end SpecificResource

/** Typed resources completed in the foundational resource slice. */
type FoundationalSpecificResource =
  resources.Assembly | resources.BinderySignature | resources.Bundle | resources.Color | resources.ColorantControl |
    resources.Component | resources.Content |
    resources.Contact | resources.CustomerInfo | resources.Device | resources.DieLayout | resources.ExposedMedia |
    resources.Ink | resources.Media |
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
    resources.SpineTapingParams | resources.StitchingParams | resources.StrappingParams |
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
    resources.ManualLaborParams | resources.PreflightParams |
    resources.PreflightReport | resources.PreviewGenerationParams | resources.RegisterMark |
    resources.RenderingParams | resources.ScreeningParams | resources.SeparationControlParams |
    resources.ShapeDefProductionParams | resources.SheetOptimizingParams | resources.TrappingParams

type GeneralSpecificResource =
  resources.ApprovalDetails | resources.ApprovalParams | resources.DeliveryParams | resources.Preview |
    resources.VerificationParams | resources.VerificationResult

type PressSpecificResource = resources.ConventionalPrintingParams | resources.DigitalPrintingParams

/** Union of all schema-defined SpecificResource descendants implemented so far. */
type TypedSpecificResource =
  FoundationalSpecificResource | PostpressSpecificResource | PrepressSpecificResource | GeneralSpecificResource |
    PressSpecificResource

/** Lossless schema-shaped fallback while dedicated resource records are added slice by slice. */
final case class NamedSpecificResource(
    elementName: QualifiedName,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource

enum WasteOrigin:
  case Modules(moduleIds: NonEmptyVector[Nmtoken])
  case Details(wasteDetails: Nmtoken)
  case ModulesAndDetails(moduleIds: NonEmptyVector[Nmtoken], wasteDetails: Nmtoken)
end WasteOrigin

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
    brand: Option[String] = None,
    commentUrl: Option[UriRef] = None,
    descriptiveName: Option[String] = None,
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
      Extensible

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
    combinedProcessIndex: Vector[Float] = Vector.empty,
    commentUrl: Option[UriRef] = None,
    descriptiveName: Option[String] = None,
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
