package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * GENERATED registry: dispatch tables for the open substitution points. Every entry summons the
 * [[XmlElementCodec]] of the type - hand-written where the XML shape is special, derived otherwise. Regenerate
 * from the union type lists when the model grows.
 *
 * Covered: 102 SpecificResource, 14 ProductIntent, 44 XJMF messages.
 */
object Registry:

  private def name(localName: String): QualifiedName = QualifiedName(XjdfNamespace.uri, localName)

  val resourceDecoders: Map[QualifiedName, XmlDecoder[SpecificResource]] = Map(
    name("ApprovalDetails") -> XmlDecoder.widen(summon[XmlElementCodec[ApprovalDetails]]),
    name("ApprovalParams") -> XmlDecoder.widen(summon[XmlElementCodec[ApprovalParams]]),
    name("Assembly") -> XmlDecoder.widen(summon[XmlElementCodec[Assembly]]),
    name("BarcodeCompParams") -> XmlDecoder.widen(summon[XmlElementCodec[BarcodeCompParams]]),
    name("BarcodeReproParams") -> XmlDecoder.widen(summon[XmlElementCodec[BarcodeReproParams]]),
    name("BendingParams") -> XmlDecoder.widen(summon[XmlElementCodec[BendingParams]]),
    name("BinderySignature") -> XmlDecoder.widen(summon[XmlElementCodec[BinderySignature]]),
    name("BlockPreparationParams") -> XmlDecoder.widen(summon[XmlElementCodec[BlockPreparationParams]]),
    name("BoxFoldingParams") -> XmlDecoder.widen(summon[XmlElementCodec[BoxFoldingParams]]),
    name("BoxPackingParams") -> XmlDecoder.widen(summon[XmlElementCodec[BoxPackingParams]]),
    name("Bundle") -> XmlDecoder.widen(summon[XmlElementCodec[Bundle]]),
    name("BundlingParams") -> XmlDecoder.widen(summon[XmlElementCodec[BundlingParams]]),
    name("CaseMakingParams") -> XmlDecoder.widen(summon[XmlElementCodec[CaseMakingParams]]),
    name("CasingInParams") -> XmlDecoder.widen(summon[XmlElementCodec[CasingInParams]]),
    name("Color") -> XmlDecoder.widen(summon[XmlElementCodec[Color]]),
    name("ColorCorrectionParams") -> XmlDecoder.widen(summon[XmlElementCodec[ColorCorrectionParams]]),
    name("ColorSpaceConversionParams") -> XmlDecoder.widen(summon[XmlElementCodec[ColorSpaceConversionParams]]),
    name("ColorantControl") -> XmlDecoder.widen(summon[XmlElementCodec[ColorantControl]]),
    name("Component") -> XmlDecoder.widen(summon[XmlElementCodec[Component]]),
    name("Contact") -> XmlDecoder.widen(summon[XmlElementCodec[Contact]]),
    name("Content") -> XmlDecoder.widen(summon[XmlElementCodec[Content]]),
    name("ConventionalPrintingParams") -> XmlDecoder.widen(summon[XmlElementCodec[ConventionalPrintingParams]]),
    name("CoverApplicationParams") -> XmlDecoder.widen(summon[XmlElementCodec[CoverApplicationParams]]),
    name("CreasingParams") -> XmlDecoder.widen(summon[XmlElementCodec[CreasingParams]]),
    name("CustomerInfo") -> XmlDecoder.widen(summon[XmlElementCodec[CustomerInfo]]),
    name("CuttingParams") -> XmlDecoder.widen(summon[XmlElementCodec[CuttingParams]]),
    name("DeliveryParams") -> XmlDecoder.widen(summon[XmlElementCodec[DeliveryParams]]),
    name("DevelopingParams") -> XmlDecoder.widen(summon[XmlElementCodec[DevelopingParams]]),
    name("Device") -> XmlDecoder.widen(summon[XmlElementCodec[Device]]),
    name("DieLayout") -> XmlDecoder.widen(summon[XmlElementCodec[DieLayout]]),
    name("DieLayoutProductionParams") -> XmlDecoder.widen(summon[XmlElementCodec[DieLayoutProductionParams]]),
    name("DigitalPrintingParams") -> XmlDecoder.widen(summon[XmlElementCodec[DigitalPrintingParams]]),
    name("EmbossingParams") -> XmlDecoder.widen(summon[XmlElementCodec[EmbossingParams]]),
    name("EndSheetGluingParams") -> XmlDecoder.widen(summon[XmlElementCodec[EndSheetGluingParams]]),
    name("ExposedMedia") -> XmlDecoder.widen(summon[XmlElementCodec[ExposedMedia]]),
    name("FeedingParams") -> XmlDecoder.widen(summon[XmlElementCodec[FeedingParams]]),
    name("FoldingParams") -> XmlDecoder.widen(summon[XmlElementCodec[FoldingParams]]),
    name("FontPolicy") -> XmlDecoder.widen(summon[XmlElementCodec[FontPolicy]]),
    name("GluingParams") -> XmlDecoder.widen(summon[XmlElementCodec[GluingParams]]),
    name("HeadBandApplicationParams") -> XmlDecoder.widen(summon[XmlElementCodec[HeadBandApplicationParams]]),
    name("HoleMakingParams") -> XmlDecoder.widen(summon[XmlElementCodec[HoleMakingParams]]),
    name("ImageCompressionParams") -> XmlDecoder.widen(summon[XmlElementCodec[ImageCompressionParams]]),
    name("ImageEnhancementParams") -> XmlDecoder.widen(summon[XmlElementCodec[ImageEnhancementParams]]),
    name("ImageSetterParams") -> XmlDecoder.widen(summon[XmlElementCodec[ImageSetterParams]]),
    name("Ink") -> XmlDecoder.widen(summon[XmlElementCodec[Ink]]),
    name("InkZoneCalculationParams") -> XmlDecoder.widen(summon[XmlElementCodec[InkZoneCalculationParams]]),
    name("InkZoneProfile") -> XmlDecoder.widen(summon[XmlElementCodec[InkZoneProfile]]),
    name("InsertingParams") -> XmlDecoder.widen(summon[XmlElementCodec[InsertingParams]]),
    name("InterpretingParams") -> XmlDecoder.widen(summon[XmlElementCodec[InterpretingParams]]),
    name("JacketingParams") -> XmlDecoder.widen(summon[XmlElementCodec[JacketingParams]]),
    name("LabelingParams") -> XmlDecoder.widen(summon[XmlElementCodec[LabelingParams]]),
    name("LaminatingParams") -> XmlDecoder.widen(summon[XmlElementCodec[LaminatingParams]]),
    name("Layout") -> XmlDecoder.widen(summon[XmlElementCodec[Layout]]),
    name("LayoutElementProductionParams") -> XmlDecoder.widen(summon[XmlElementCodec[LayoutElementProductionParams]]),
    name("LayoutShift") -> XmlDecoder.widen(summon[XmlElementCodec[LayoutShift]]),
    name("LooseBindingParams") -> XmlDecoder.widen(summon[XmlElementCodec[LooseBindingParams]]),
    name("ManualLaborParams") -> XmlDecoder.widen(summon[XmlElementCodec[ManualLaborParams]]),
    name("Media") -> XmlDecoder.widen(summon[XmlElementCodec[Media]]),
    name("MiscConsumable") -> XmlDecoder.widen(summon[XmlElementCodec[MiscConsumable]]),
    name("NodeInfo") -> XmlDecoder.widen(summon[XmlElementCodec[NodeInfo]]),
    name("PDLCreationParams") -> XmlDecoder.widen(summon[XmlElementCodec[PDLCreationParams]]),
    name("Pallet") -> XmlDecoder.widen(summon[XmlElementCodec[Pallet]]),
    name("PalletizingParams") -> XmlDecoder.widen(summon[XmlElementCodec[PalletizingParams]]),
    name("PerforatingParams") -> XmlDecoder.widen(summon[XmlElementCodec[PerforatingParams]]),
    name("PreflightParams") -> XmlDecoder.widen(summon[XmlElementCodec[PreflightParams]]),
    name("PreflightReport") -> XmlDecoder.widen(summon[XmlElementCodec[PreflightReport]]),
    name("Preview") -> XmlDecoder.widen(summon[XmlElementCodec[Preview]]),
    name("PreviewGenerationParams") -> XmlDecoder.widen(summon[XmlElementCodec[PreviewGenerationParams]]),
    name("PrintCondition") -> XmlDecoder.widen(summon[XmlElementCodec[PrintCondition]]),
    name("QualityControlParams") -> XmlDecoder.widen(summon[XmlElementCodec[QualityControlParams]]),
    name("QualityControlResult") -> XmlDecoder.widen(summon[XmlElementCodec[QualityControlResult]]),
    name("RasterReadingParams") -> XmlDecoder.widen(summon[XmlElementCodec[RasterReadingParams]]),
    name("RegisterMark") -> XmlDecoder.widen(summon[XmlElementCodec[RegisterMark]]),
    name("RenderingParams") -> XmlDecoder.widen(summon[XmlElementCodec[RenderingParams]]),
    name("RunList") -> XmlDecoder.widen(summon[XmlElementCodec[RunList]]),
    name("ScreeningParams") -> XmlDecoder.widen(summon[XmlElementCodec[ScreeningParams]]),
    name("SeparationControlParams") -> XmlDecoder.widen(summon[XmlElementCodec[SeparationControlParams]]),
    name("Shape") -> XmlDecoder.widen(summon[XmlElementCodec[Shape]]),
    name("ShapeCuttingParams") -> XmlDecoder.widen(summon[XmlElementCodec[ShapeCuttingParams]]),
    name("ShapeDef") -> XmlDecoder.widen(summon[XmlElementCodec[ShapeDef]]),
    name("ShapeDefProductionParams") -> XmlDecoder.widen(summon[XmlElementCodec[ShapeDefProductionParams]]),
    name("SheetOptimizingParams") -> XmlDecoder.widen(summon[XmlElementCodec[SheetOptimizingParams]]),
    name("SheetOptimizingReport") -> XmlDecoder.widen(summon[XmlElementCodec[SheetOptimizingReport]]),
    name("ShrinkingParams") -> XmlDecoder.widen(summon[XmlElementCodec[ShrinkingParams]]),
    name("SpinePreparationParams") -> XmlDecoder.widen(summon[XmlElementCodec[SpinePreparationParams]]),
    name("SpineTapingParams") -> XmlDecoder.widen(summon[XmlElementCodec[SpineTapingParams]]),
    name("StackingParams") -> XmlDecoder.widen(summon[XmlElementCodec[StackingParams]]),
    name("StitchingParams") -> XmlDecoder.widen(summon[XmlElementCodec[StitchingParams]]),
    name("StrappingParams") -> XmlDecoder.widen(summon[XmlElementCodec[StrappingParams]]),
    name("ThreadSealingParams") -> XmlDecoder.widen(summon[XmlElementCodec[ThreadSealingParams]]),
    name("ThreadSewingParams") -> XmlDecoder.widen(summon[XmlElementCodec[ThreadSewingParams]]),
    name("Tool") -> XmlDecoder.widen(summon[XmlElementCodec[Tool]]),
    name("TransferCurve") -> XmlDecoder.widen(summon[XmlElementCodec[TransferCurve]]),
    name("TrappingParams") -> XmlDecoder.widen(summon[XmlElementCodec[TrappingParams]]),
    name("TrimmingParams") -> XmlDecoder.widen(summon[XmlElementCodec[TrimmingParams]]),
    name("UsageCounter") -> XmlDecoder.widen(summon[XmlElementCodec[UsageCounter]]),
    name("VarnishingParams") -> XmlDecoder.widen(summon[XmlElementCodec[VarnishingParams]]),
    name("VerificationParams") -> XmlDecoder.widen(summon[XmlElementCodec[VerificationParams]]),
    name("VerificationResult") -> XmlDecoder.widen(summon[XmlElementCodec[VerificationResult]]),
    name("WebInlineFinishingParams") -> XmlDecoder.widen(summon[XmlElementCodec[WebInlineFinishingParams]]),
    name("WindingParams") -> XmlDecoder.widen(summon[XmlElementCodec[WindingParams]]),
    name("WrappingParams") -> XmlDecoder.widen(summon[XmlElementCodec[WrappingParams]]),
  )

  private val resourceEncoders: Map[String, SpecificResource => Xml.Element] = Map(
    "ApprovalDetails" -> (value => summon[XmlElementCodec[ApprovalDetails]].encode(value.asInstanceOf[ApprovalDetails])),
    "ApprovalParams" -> (value => summon[XmlElementCodec[ApprovalParams]].encode(value.asInstanceOf[ApprovalParams])),
    "Assembly" -> (value => summon[XmlElementCodec[Assembly]].encode(value.asInstanceOf[Assembly])),
    "BarcodeCompParams" -> (value => summon[XmlElementCodec[BarcodeCompParams]].encode(value.asInstanceOf[BarcodeCompParams])),
    "BarcodeReproParams" -> (value => summon[XmlElementCodec[BarcodeReproParams]].encode(value.asInstanceOf[BarcodeReproParams])),
    "BendingParams" -> (value => summon[XmlElementCodec[BendingParams]].encode(value.asInstanceOf[BendingParams])),
    "BinderySignature" -> (value => summon[XmlElementCodec[BinderySignature]].encode(value.asInstanceOf[BinderySignature])),
    "BlockPreparationParams" -> (value => summon[XmlElementCodec[BlockPreparationParams]].encode(value.asInstanceOf[BlockPreparationParams])),
    "BoxFoldingParams" -> (value => summon[XmlElementCodec[BoxFoldingParams]].encode(value.asInstanceOf[BoxFoldingParams])),
    "BoxPackingParams" -> (value => summon[XmlElementCodec[BoxPackingParams]].encode(value.asInstanceOf[BoxPackingParams])),
    "Bundle" -> (value => summon[XmlElementCodec[Bundle]].encode(value.asInstanceOf[Bundle])),
    "BundlingParams" -> (value => summon[XmlElementCodec[BundlingParams]].encode(value.asInstanceOf[BundlingParams])),
    "CaseMakingParams" -> (value => summon[XmlElementCodec[CaseMakingParams]].encode(value.asInstanceOf[CaseMakingParams])),
    "CasingInParams" -> (value => summon[XmlElementCodec[CasingInParams]].encode(value.asInstanceOf[CasingInParams])),
    "Color" -> (value => summon[XmlElementCodec[Color]].encode(value.asInstanceOf[Color])),
    "ColorCorrectionParams" -> (value => summon[XmlElementCodec[ColorCorrectionParams]].encode(value.asInstanceOf[ColorCorrectionParams])),
    "ColorSpaceConversionParams" -> (value => summon[XmlElementCodec[ColorSpaceConversionParams]].encode(value.asInstanceOf[ColorSpaceConversionParams])),
    "ColorantControl" -> (value => summon[XmlElementCodec[ColorantControl]].encode(value.asInstanceOf[ColorantControl])),
    "Component" -> (value => summon[XmlElementCodec[Component]].encode(value.asInstanceOf[Component])),
    "Contact" -> (value => summon[XmlElementCodec[Contact]].encode(value.asInstanceOf[Contact])),
    "Content" -> (value => summon[XmlElementCodec[Content]].encode(value.asInstanceOf[Content])),
    "ConventionalPrintingParams" -> (value => summon[XmlElementCodec[ConventionalPrintingParams]].encode(value.asInstanceOf[ConventionalPrintingParams])),
    "CoverApplicationParams" -> (value => summon[XmlElementCodec[CoverApplicationParams]].encode(value.asInstanceOf[CoverApplicationParams])),
    "CreasingParams" -> (value => summon[XmlElementCodec[CreasingParams]].encode(value.asInstanceOf[CreasingParams])),
    "CustomerInfo" -> (value => summon[XmlElementCodec[CustomerInfo]].encode(value.asInstanceOf[CustomerInfo])),
    "CuttingParams" -> (value => summon[XmlElementCodec[CuttingParams]].encode(value.asInstanceOf[CuttingParams])),
    "DeliveryParams" -> (value => summon[XmlElementCodec[DeliveryParams]].encode(value.asInstanceOf[DeliveryParams])),
    "DevelopingParams" -> (value => summon[XmlElementCodec[DevelopingParams]].encode(value.asInstanceOf[DevelopingParams])),
    "Device" -> (value => summon[XmlElementCodec[Device]].encode(value.asInstanceOf[Device])),
    "DieLayout" -> (value => summon[XmlElementCodec[DieLayout]].encode(value.asInstanceOf[DieLayout])),
    "DieLayoutProductionParams" -> (value => summon[XmlElementCodec[DieLayoutProductionParams]].encode(value.asInstanceOf[DieLayoutProductionParams])),
    "DigitalPrintingParams" -> (value => summon[XmlElementCodec[DigitalPrintingParams]].encode(value.asInstanceOf[DigitalPrintingParams])),
    "EmbossingParams" -> (value => summon[XmlElementCodec[EmbossingParams]].encode(value.asInstanceOf[EmbossingParams])),
    "EndSheetGluingParams" -> (value => summon[XmlElementCodec[EndSheetGluingParams]].encode(value.asInstanceOf[EndSheetGluingParams])),
    "ExposedMedia" -> (value => summon[XmlElementCodec[ExposedMedia]].encode(value.asInstanceOf[ExposedMedia])),
    "FeedingParams" -> (value => summon[XmlElementCodec[FeedingParams]].encode(value.asInstanceOf[FeedingParams])),
    "FoldingParams" -> (value => summon[XmlElementCodec[FoldingParams]].encode(value.asInstanceOf[FoldingParams])),
    "FontPolicy" -> (value => summon[XmlElementCodec[FontPolicy]].encode(value.asInstanceOf[FontPolicy])),
    "GluingParams" -> (value => summon[XmlElementCodec[GluingParams]].encode(value.asInstanceOf[GluingParams])),
    "HeadBandApplicationParams" -> (value => summon[XmlElementCodec[HeadBandApplicationParams]].encode(value.asInstanceOf[HeadBandApplicationParams])),
    "HoleMakingParams" -> (value => summon[XmlElementCodec[HoleMakingParams]].encode(value.asInstanceOf[HoleMakingParams])),
    "ImageCompressionParams" -> (value => summon[XmlElementCodec[ImageCompressionParams]].encode(value.asInstanceOf[ImageCompressionParams])),
    "ImageEnhancementParams" -> (value => summon[XmlElementCodec[ImageEnhancementParams]].encode(value.asInstanceOf[ImageEnhancementParams])),
    "ImageSetterParams" -> (value => summon[XmlElementCodec[ImageSetterParams]].encode(value.asInstanceOf[ImageSetterParams])),
    "Ink" -> (value => summon[XmlElementCodec[Ink]].encode(value.asInstanceOf[Ink])),
    "InkZoneCalculationParams" -> (value => summon[XmlElementCodec[InkZoneCalculationParams]].encode(value.asInstanceOf[InkZoneCalculationParams])),
    "InkZoneProfile" -> (value => summon[XmlElementCodec[InkZoneProfile]].encode(value.asInstanceOf[InkZoneProfile])),
    "InsertingParams" -> (value => summon[XmlElementCodec[InsertingParams]].encode(value.asInstanceOf[InsertingParams])),
    "InterpretingParams" -> (value => summon[XmlElementCodec[InterpretingParams]].encode(value.asInstanceOf[InterpretingParams])),
    "JacketingParams" -> (value => summon[XmlElementCodec[JacketingParams]].encode(value.asInstanceOf[JacketingParams])),
    "LabelingParams" -> (value => summon[XmlElementCodec[LabelingParams]].encode(value.asInstanceOf[LabelingParams])),
    "LaminatingParams" -> (value => summon[XmlElementCodec[LaminatingParams]].encode(value.asInstanceOf[LaminatingParams])),
    "Layout" -> (value => summon[XmlElementCodec[Layout]].encode(value.asInstanceOf[Layout])),
    "LayoutElementProductionParams" -> (value => summon[XmlElementCodec[LayoutElementProductionParams]].encode(value.asInstanceOf[LayoutElementProductionParams])),
    "LayoutShift" -> (value => summon[XmlElementCodec[LayoutShift]].encode(value.asInstanceOf[LayoutShift])),
    "LooseBindingParams" -> (value => summon[XmlElementCodec[LooseBindingParams]].encode(value.asInstanceOf[LooseBindingParams])),
    "ManualLaborParams" -> (value => summon[XmlElementCodec[ManualLaborParams]].encode(value.asInstanceOf[ManualLaborParams])),
    "Media" -> (value => summon[XmlElementCodec[Media]].encode(value.asInstanceOf[Media])),
    "MiscConsumable" -> (value => summon[XmlElementCodec[MiscConsumable]].encode(value.asInstanceOf[MiscConsumable])),
    "NodeInfo" -> (value => summon[XmlElementCodec[NodeInfo]].encode(value.asInstanceOf[NodeInfo])),
    "PDLCreationParams" -> (value => summon[XmlElementCodec[PDLCreationParams]].encode(value.asInstanceOf[PDLCreationParams])),
    "Pallet" -> (value => summon[XmlElementCodec[Pallet]].encode(value.asInstanceOf[Pallet])),
    "PalletizingParams" -> (value => summon[XmlElementCodec[PalletizingParams]].encode(value.asInstanceOf[PalletizingParams])),
    "PerforatingParams" -> (value => summon[XmlElementCodec[PerforatingParams]].encode(value.asInstanceOf[PerforatingParams])),
    "PreflightParams" -> (value => summon[XmlElementCodec[PreflightParams]].encode(value.asInstanceOf[PreflightParams])),
    "PreflightReport" -> (value => summon[XmlElementCodec[PreflightReport]].encode(value.asInstanceOf[PreflightReport])),
    "Preview" -> (value => summon[XmlElementCodec[Preview]].encode(value.asInstanceOf[Preview])),
    "PreviewGenerationParams" -> (value => summon[XmlElementCodec[PreviewGenerationParams]].encode(value.asInstanceOf[PreviewGenerationParams])),
    "PrintCondition" -> (value => summon[XmlElementCodec[PrintCondition]].encode(value.asInstanceOf[PrintCondition])),
    "QualityControlParams" -> (value => summon[XmlElementCodec[QualityControlParams]].encode(value.asInstanceOf[QualityControlParams])),
    "QualityControlResult" -> (value => summon[XmlElementCodec[QualityControlResult]].encode(value.asInstanceOf[QualityControlResult])),
    "RasterReadingParams" -> (value => summon[XmlElementCodec[RasterReadingParams]].encode(value.asInstanceOf[RasterReadingParams])),
    "RegisterMark" -> (value => summon[XmlElementCodec[RegisterMark]].encode(value.asInstanceOf[RegisterMark])),
    "RenderingParams" -> (value => summon[XmlElementCodec[RenderingParams]].encode(value.asInstanceOf[RenderingParams])),
    "RunList" -> (value => summon[XmlElementCodec[RunList]].encode(value.asInstanceOf[RunList])),
    "ScreeningParams" -> (value => summon[XmlElementCodec[ScreeningParams]].encode(value.asInstanceOf[ScreeningParams])),
    "SeparationControlParams" -> (value => summon[XmlElementCodec[SeparationControlParams]].encode(value.asInstanceOf[SeparationControlParams])),
    "Shape" -> (value => summon[XmlElementCodec[Shape]].encode(value.asInstanceOf[Shape])),
    "ShapeCuttingParams" -> (value => summon[XmlElementCodec[ShapeCuttingParams]].encode(value.asInstanceOf[ShapeCuttingParams])),
    "ShapeDef" -> (value => summon[XmlElementCodec[ShapeDef]].encode(value.asInstanceOf[ShapeDef])),
    "ShapeDefProductionParams" -> (value => summon[XmlElementCodec[ShapeDefProductionParams]].encode(value.asInstanceOf[ShapeDefProductionParams])),
    "SheetOptimizingParams" -> (value => summon[XmlElementCodec[SheetOptimizingParams]].encode(value.asInstanceOf[SheetOptimizingParams])),
    "SheetOptimizingReport" -> (value => summon[XmlElementCodec[SheetOptimizingReport]].encode(value.asInstanceOf[SheetOptimizingReport])),
    "ShrinkingParams" -> (value => summon[XmlElementCodec[ShrinkingParams]].encode(value.asInstanceOf[ShrinkingParams])),
    "SpinePreparationParams" -> (value => summon[XmlElementCodec[SpinePreparationParams]].encode(value.asInstanceOf[SpinePreparationParams])),
    "SpineTapingParams" -> (value => summon[XmlElementCodec[SpineTapingParams]].encode(value.asInstanceOf[SpineTapingParams])),
    "StackingParams" -> (value => summon[XmlElementCodec[StackingParams]].encode(value.asInstanceOf[StackingParams])),
    "StitchingParams" -> (value => summon[XmlElementCodec[StitchingParams]].encode(value.asInstanceOf[StitchingParams])),
    "StrappingParams" -> (value => summon[XmlElementCodec[StrappingParams]].encode(value.asInstanceOf[StrappingParams])),
    "ThreadSealingParams" -> (value => summon[XmlElementCodec[ThreadSealingParams]].encode(value.asInstanceOf[ThreadSealingParams])),
    "ThreadSewingParams" -> (value => summon[XmlElementCodec[ThreadSewingParams]].encode(value.asInstanceOf[ThreadSewingParams])),
    "Tool" -> (value => summon[XmlElementCodec[Tool]].encode(value.asInstanceOf[Tool])),
    "TransferCurve" -> (value => summon[XmlElementCodec[TransferCurve]].encode(value.asInstanceOf[TransferCurve])),
    "TrappingParams" -> (value => summon[XmlElementCodec[TrappingParams]].encode(value.asInstanceOf[TrappingParams])),
    "TrimmingParams" -> (value => summon[XmlElementCodec[TrimmingParams]].encode(value.asInstanceOf[TrimmingParams])),
    "UsageCounter" -> (value => summon[XmlElementCodec[UsageCounter]].encode(value.asInstanceOf[UsageCounter])),
    "VarnishingParams" -> (value => summon[XmlElementCodec[VarnishingParams]].encode(value.asInstanceOf[VarnishingParams])),
    "VerificationParams" -> (value => summon[XmlElementCodec[VerificationParams]].encode(value.asInstanceOf[VerificationParams])),
    "VerificationResult" -> (value => summon[XmlElementCodec[VerificationResult]].encode(value.asInstanceOf[VerificationResult])),
    "WebInlineFinishingParams" -> (value => summon[XmlElementCodec[WebInlineFinishingParams]].encode(value.asInstanceOf[WebInlineFinishingParams])),
    "WindingParams" -> (value => summon[XmlElementCodec[WindingParams]].encode(value.asInstanceOf[WindingParams])),
    "WrappingParams" -> (value => summon[XmlElementCodec[WrappingParams]].encode(value.asInstanceOf[WrappingParams])),
  )

  val resourceNames: Set[String] = resourceEncoders.keySet

  val intentDecoders: Map[QualifiedName, XmlDecoder[ProductIntent]] = Map(
    name("AssemblingIntent") -> XmlDecoder.widen(summon[XmlElementCodec[AssemblingIntent]]),
    name("BindingIntent") -> XmlDecoder.widen(summon[XmlElementCodec[BindingIntent]]),
    name("ColorIntent") -> XmlDecoder.widen(summon[XmlElementCodec[ColorIntent]]),
    name("ContentCheckIntent") -> XmlDecoder.widen(summon[XmlElementCodec[ContentCheckIntent]]),
    name("EmbossingIntent") -> XmlDecoder.widen(summon[XmlElementCodec[EmbossingIntent]]),
    name("FoldingIntent") -> XmlDecoder.widen(summon[XmlElementCodec[FoldingIntent]]),
    name("HoleMakingIntent") -> XmlDecoder.widen(summon[XmlElementCodec[HoleMakingIntent]]),
    name("IntentResource") -> XmlDecoder.widen(summon[XmlElementCodec[IntentResource]]),
    name("LaminatingIntent") -> XmlDecoder.widen(summon[XmlElementCodec[LaminatingIntent]]),
    name("LayoutIntent") -> XmlDecoder.widen(summon[XmlElementCodec[LayoutIntent]]),
    name("MediaIntent") -> XmlDecoder.widen(summon[XmlElementCodec[MediaIntent]]),
    name("ProductionIntent") -> XmlDecoder.widen(summon[XmlElementCodec[ProductionIntent]]),
    name("ShapeCuttingIntent") -> XmlDecoder.widen(summon[XmlElementCodec[ShapeCuttingIntent]]),
    name("VariableIntent") -> XmlDecoder.widen(summon[XmlElementCodec[VariableIntent]]),
  )

  private val intentEncoders: Map[String, ProductIntent => Xml.Element] = Map(
    "AssemblingIntent" -> (value => summon[XmlElementCodec[AssemblingIntent]].encode(value.asInstanceOf[AssemblingIntent])),
    "BindingIntent" -> (value => summon[XmlElementCodec[BindingIntent]].encode(value.asInstanceOf[BindingIntent])),
    "ColorIntent" -> (value => summon[XmlElementCodec[ColorIntent]].encode(value.asInstanceOf[ColorIntent])),
    "ContentCheckIntent" -> (value => summon[XmlElementCodec[ContentCheckIntent]].encode(value.asInstanceOf[ContentCheckIntent])),
    "EmbossingIntent" -> (value => summon[XmlElementCodec[EmbossingIntent]].encode(value.asInstanceOf[EmbossingIntent])),
    "FoldingIntent" -> (value => summon[XmlElementCodec[FoldingIntent]].encode(value.asInstanceOf[FoldingIntent])),
    "HoleMakingIntent" -> (value => summon[XmlElementCodec[HoleMakingIntent]].encode(value.asInstanceOf[HoleMakingIntent])),
    "IntentResource" -> (value => summon[XmlElementCodec[IntentResource]].encode(value.asInstanceOf[IntentResource])),
    "LaminatingIntent" -> (value => summon[XmlElementCodec[LaminatingIntent]].encode(value.asInstanceOf[LaminatingIntent])),
    "LayoutIntent" -> (value => summon[XmlElementCodec[LayoutIntent]].encode(value.asInstanceOf[LayoutIntent])),
    "MediaIntent" -> (value => summon[XmlElementCodec[MediaIntent]].encode(value.asInstanceOf[MediaIntent])),
    "ProductionIntent" -> (value => summon[XmlElementCodec[ProductionIntent]].encode(value.asInstanceOf[ProductionIntent])),
    "ShapeCuttingIntent" -> (value => summon[XmlElementCodec[ShapeCuttingIntent]].encode(value.asInstanceOf[ShapeCuttingIntent])),
    "VariableIntent" -> (value => summon[XmlElementCodec[VariableIntent]].encode(value.asInstanceOf[VariableIntent])),
  )

  val intentNames: Set[String] = intentEncoders.keySet

  val messageDecoders: Map[QualifiedName, XmlDecoder[Message]] = Map(
    name("CommandForceGang") -> XmlDecoder.widen(summon[XmlElementCodec[CommandForceGang]]),
    name("CommandModifyQueueEntry") -> XmlDecoder.widen(summon[XmlElementCodec[CommandModifyQueueEntry]]),
    name("CommandPipeControl") -> XmlDecoder.widen(summon[XmlElementCodec[CommandPipeControl]]),
    name("CommandRequestQueueEntry") -> XmlDecoder.widen(summon[XmlElementCodec[CommandRequestQueueEntry]]),
    name("CommandResource") -> XmlDecoder.widen(summon[XmlElementCodec[CommandResource]]),
    name("CommandResubmitQueueEntry") -> XmlDecoder.widen(summon[XmlElementCodec[CommandResubmitQueueEntry]]),
    name("CommandReturnQueueEntry") -> XmlDecoder.widen(summon[XmlElementCodec[CommandReturnQueueEntry]]),
    name("CommandShutDown") -> XmlDecoder.widen(summon[XmlElementCodec[CommandShutDown]]),
    name("CommandStopPersistentChannel") -> XmlDecoder.widen(summon[XmlElementCodec[CommandStopPersistentChannel]]),
    name("CommandSubmitQueueEntry") -> XmlDecoder.widen(summon[XmlElementCodec[CommandSubmitQueueEntry]]),
    name("CommandWakeUp") -> XmlDecoder.widen(summon[XmlElementCodec[CommandWakeUp]]),
    name("QueryGangStatus") -> XmlDecoder.widen(summon[XmlElementCodec[QueryGangStatus]]),
    name("QueryKnownDevices") -> XmlDecoder.widen(summon[XmlElementCodec[QueryKnownDevices]]),
    name("QueryKnownMessages") -> XmlDecoder.widen(summon[XmlElementCodec[QueryKnownMessages]]),
    name("QueryKnownSubscriptions") -> XmlDecoder.widen(summon[XmlElementCodec[QueryKnownSubscriptions]]),
    name("QueryNotification") -> XmlDecoder.widen(summon[XmlElementCodec[QueryNotification]]),
    name("QueryQueueStatus") -> XmlDecoder.widen(summon[XmlElementCodec[QueryQueueStatus]]),
    name("QueryResource") -> XmlDecoder.widen(summon[XmlElementCodec[QueryResource]]),
    name("QueryStatus") -> XmlDecoder.widen(summon[XmlElementCodec[QueryStatus]]),
    name("ResponseForceGang") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseForceGang]]),
    name("ResponseGangStatus") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseGangStatus]]),
    name("ResponseKnownDevices") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseKnownDevices]]),
    name("ResponseKnownMessages") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseKnownMessages]]),
    name("ResponseKnownSubscriptions") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseKnownSubscriptions]]),
    name("ResponseModifyQueueEntry") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseModifyQueueEntry]]),
    name("ResponseNotification") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseNotification]]),
    name("ResponsePipeControl") -> XmlDecoder.widen(summon[XmlElementCodec[ResponsePipeControl]]),
    name("ResponseQueueStatus") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseQueueStatus]]),
    name("ResponseRequestQueueEntry") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseRequestQueueEntry]]),
    name("ResponseResource") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseResource]]),
    name("ResponseResubmitQueueEntry") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseResubmitQueueEntry]]),
    name("ResponseReturnQueueEntry") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseReturnQueueEntry]]),
    name("ResponseShutDown") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseShutDown]]),
    name("ResponseStatus") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseStatus]]),
    name("ResponseStopPersistentChannel") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseStopPersistentChannel]]),
    name("ResponseSubmitQueueEntry") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseSubmitQueueEntry]]),
    name("ResponseWakeUp") -> XmlDecoder.widen(summon[XmlElementCodec[ResponseWakeUp]]),
    name("SignalGangStatus") -> XmlDecoder.widen(summon[XmlElementCodec[SignalGangStatus]]),
    name("SignalKnownDevices") -> XmlDecoder.widen(summon[XmlElementCodec[SignalKnownDevices]]),
    name("SignalKnownSubscriptions") -> XmlDecoder.widen(summon[XmlElementCodec[SignalKnownSubscriptions]]),
    name("SignalNotification") -> XmlDecoder.widen(summon[XmlElementCodec[SignalNotification]]),
    name("SignalQueueStatus") -> XmlDecoder.widen(summon[XmlElementCodec[SignalQueueStatus]]),
    name("SignalResource") -> XmlDecoder.widen(summon[XmlElementCodec[SignalResource]]),
    name("SignalStatus") -> XmlDecoder.widen(summon[XmlElementCodec[SignalStatus]]),
  )

  private val messageEncoders: Map[String, Message => Xml.Element] = Map(
    "CommandForceGang" -> (value => summon[XmlElementCodec[CommandForceGang]].encode(value.asInstanceOf[CommandForceGang])),
    "CommandModifyQueueEntry" -> (value => summon[XmlElementCodec[CommandModifyQueueEntry]].encode(value.asInstanceOf[CommandModifyQueueEntry])),
    "CommandPipeControl" -> (value => summon[XmlElementCodec[CommandPipeControl]].encode(value.asInstanceOf[CommandPipeControl])),
    "CommandRequestQueueEntry" -> (value => summon[XmlElementCodec[CommandRequestQueueEntry]].encode(value.asInstanceOf[CommandRequestQueueEntry])),
    "CommandResource" -> (value => summon[XmlElementCodec[CommandResource]].encode(value.asInstanceOf[CommandResource])),
    "CommandResubmitQueueEntry" -> (value => summon[XmlElementCodec[CommandResubmitQueueEntry]].encode(value.asInstanceOf[CommandResubmitQueueEntry])),
    "CommandReturnQueueEntry" -> (value => summon[XmlElementCodec[CommandReturnQueueEntry]].encode(value.asInstanceOf[CommandReturnQueueEntry])),
    "CommandShutDown" -> (value => summon[XmlElementCodec[CommandShutDown]].encode(value.asInstanceOf[CommandShutDown])),
    "CommandStopPersistentChannel" -> (value => summon[XmlElementCodec[CommandStopPersistentChannel]].encode(value.asInstanceOf[CommandStopPersistentChannel])),
    "CommandSubmitQueueEntry" -> (value => summon[XmlElementCodec[CommandSubmitQueueEntry]].encode(value.asInstanceOf[CommandSubmitQueueEntry])),
    "CommandWakeUp" -> (value => summon[XmlElementCodec[CommandWakeUp]].encode(value.asInstanceOf[CommandWakeUp])),
    "QueryGangStatus" -> (value => summon[XmlElementCodec[QueryGangStatus]].encode(value.asInstanceOf[QueryGangStatus])),
    "QueryKnownDevices" -> (value => summon[XmlElementCodec[QueryKnownDevices]].encode(value.asInstanceOf[QueryKnownDevices])),
    "QueryKnownMessages" -> (value => summon[XmlElementCodec[QueryKnownMessages]].encode(value.asInstanceOf[QueryKnownMessages])),
    "QueryKnownSubscriptions" -> (value => summon[XmlElementCodec[QueryKnownSubscriptions]].encode(value.asInstanceOf[QueryKnownSubscriptions])),
    "QueryNotification" -> (value => summon[XmlElementCodec[QueryNotification]].encode(value.asInstanceOf[QueryNotification])),
    "QueryQueueStatus" -> (value => summon[XmlElementCodec[QueryQueueStatus]].encode(value.asInstanceOf[QueryQueueStatus])),
    "QueryResource" -> (value => summon[XmlElementCodec[QueryResource]].encode(value.asInstanceOf[QueryResource])),
    "QueryStatus" -> (value => summon[XmlElementCodec[QueryStatus]].encode(value.asInstanceOf[QueryStatus])),
    "ResponseForceGang" -> (value => summon[XmlElementCodec[ResponseForceGang]].encode(value.asInstanceOf[ResponseForceGang])),
    "ResponseGangStatus" -> (value => summon[XmlElementCodec[ResponseGangStatus]].encode(value.asInstanceOf[ResponseGangStatus])),
    "ResponseKnownDevices" -> (value => summon[XmlElementCodec[ResponseKnownDevices]].encode(value.asInstanceOf[ResponseKnownDevices])),
    "ResponseKnownMessages" -> (value => summon[XmlElementCodec[ResponseKnownMessages]].encode(value.asInstanceOf[ResponseKnownMessages])),
    "ResponseKnownSubscriptions" -> (value => summon[XmlElementCodec[ResponseKnownSubscriptions]].encode(value.asInstanceOf[ResponseKnownSubscriptions])),
    "ResponseModifyQueueEntry" -> (value => summon[XmlElementCodec[ResponseModifyQueueEntry]].encode(value.asInstanceOf[ResponseModifyQueueEntry])),
    "ResponseNotification" -> (value => summon[XmlElementCodec[ResponseNotification]].encode(value.asInstanceOf[ResponseNotification])),
    "ResponsePipeControl" -> (value => summon[XmlElementCodec[ResponsePipeControl]].encode(value.asInstanceOf[ResponsePipeControl])),
    "ResponseQueueStatus" -> (value => summon[XmlElementCodec[ResponseQueueStatus]].encode(value.asInstanceOf[ResponseQueueStatus])),
    "ResponseRequestQueueEntry" -> (value => summon[XmlElementCodec[ResponseRequestQueueEntry]].encode(value.asInstanceOf[ResponseRequestQueueEntry])),
    "ResponseResource" -> (value => summon[XmlElementCodec[ResponseResource]].encode(value.asInstanceOf[ResponseResource])),
    "ResponseResubmitQueueEntry" -> (value => summon[XmlElementCodec[ResponseResubmitQueueEntry]].encode(value.asInstanceOf[ResponseResubmitQueueEntry])),
    "ResponseReturnQueueEntry" -> (value => summon[XmlElementCodec[ResponseReturnQueueEntry]].encode(value.asInstanceOf[ResponseReturnQueueEntry])),
    "ResponseShutDown" -> (value => summon[XmlElementCodec[ResponseShutDown]].encode(value.asInstanceOf[ResponseShutDown])),
    "ResponseStatus" -> (value => summon[XmlElementCodec[ResponseStatus]].encode(value.asInstanceOf[ResponseStatus])),
    "ResponseStopPersistentChannel" -> (value => summon[XmlElementCodec[ResponseStopPersistentChannel]].encode(value.asInstanceOf[ResponseStopPersistentChannel])),
    "ResponseSubmitQueueEntry" -> (value => summon[XmlElementCodec[ResponseSubmitQueueEntry]].encode(value.asInstanceOf[ResponseSubmitQueueEntry])),
    "ResponseWakeUp" -> (value => summon[XmlElementCodec[ResponseWakeUp]].encode(value.asInstanceOf[ResponseWakeUp])),
    "SignalGangStatus" -> (value => summon[XmlElementCodec[SignalGangStatus]].encode(value.asInstanceOf[SignalGangStatus])),
    "SignalKnownDevices" -> (value => summon[XmlElementCodec[SignalKnownDevices]].encode(value.asInstanceOf[SignalKnownDevices])),
    "SignalKnownSubscriptions" -> (value => summon[XmlElementCodec[SignalKnownSubscriptions]].encode(value.asInstanceOf[SignalKnownSubscriptions])),
    "SignalNotification" -> (value => summon[XmlElementCodec[SignalNotification]].encode(value.asInstanceOf[SignalNotification])),
    "SignalQueueStatus" -> (value => summon[XmlElementCodec[SignalQueueStatus]].encode(value.asInstanceOf[SignalQueueStatus])),
    "SignalResource" -> (value => summon[XmlElementCodec[SignalResource]].encode(value.asInstanceOf[SignalResource])),
    "SignalStatus" -> (value => summon[XmlElementCodec[SignalStatus]].encode(value.asInstanceOf[SignalStatus])),
  )

  val messageNames: Set[String] = messageEncoders.keySet

  def decodeSpecificResource(element: Xml.Element): Either[XmlError, SpecificResource] =
    resourceDecoders.get(element.name) match
      case Some(decoder) => decoder.decode(element)
      case None if element.name.namespace == XjdfNamespace.uri =>
        Left(XmlError.UnsupportedElement(element.name.localName))
      case None => summon[XmlElementCodec[NamedSpecificResource]].decode(element)

  def encodeSpecificResource(resource: SpecificResource): Xml.Element =
    resource match
      case named: NamedSpecificResource => summon[XmlElementCodec[NamedSpecificResource]].encode(named)
      case other =>
        resourceEncoders.get(other.getClass.getSimpleName) match
          case Some(encoder) => encoder(other)
          case None =>
            throw new UnsupportedOperationException(s"no encoder for ${other.getClass.getName} in this codec")

  def decodeProductIntent(element: Xml.Element): Either[XmlError, ProductIntent] =
    intentDecoders.get(element.name) match
      case Some(decoder) => decoder.decode(element)
      case None if element.name.namespace == XjdfNamespace.uri =>
        Left(XmlError.UnsupportedElement(element.name.localName))
      case None => summon[XmlElementCodec[NamedProductIntent]].decode(element)

  def encodeProductIntent(intent: ProductIntent): Xml.Element =
    intent match
      case named: NamedProductIntent => summon[XmlElementCodec[NamedProductIntent]].encode(named)
      case other =>
        intentEncoders.get(other.getClass.getSimpleName) match
          case Some(encoder) => encoder(other)
          case None =>
            throw new UnsupportedOperationException(s"no encoder for ${other.getClass.getName} in this codec")

  def decodeMessage(element: Xml.Element): Either[XmlError, Message] =
    messageDecoders.get(element.name) match
      case Some(decoder) => decoder.decode(element)
      case None          => Left(XmlError.UnsupportedElement(element.name.localName))

  def encodeMessage(message: Message): Xml.Element =
    messageEncoders.get(message.getClass.getSimpleName) match
      case Some(encoder) => encoder(message)
      case None =>
        throw new UnsupportedOperationException(s"no encoder for ${message.getClass.getName} in this codec")
end Registry
