package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum BoxType derives CanEqual:
  case Box, Carton, Envelope, Tube

final case class BoxPackingParams(
    boxType: BoxType,
    boxTypeDetails: Option[XjdfString] = None,
    columns: Option[Int] = None,
    componentsPerRow: Option[Int] = None,
    copies: Option[Int] = None,
    faceDown: Option[Face] = None,
    layers: Option[Int] = None,
    maxWeight: Option[Float] = None,
    pattern: Option[Nmtoken] = None,
    rows: Option[Int] = None,
    ties: Vector[Int] = Vector.empty,
    underLays: Vector[Int] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("BoxPackingParams")

final case class PalletizingParams(
    layerAmount: Vector[Int] = Vector.empty,
    maxHeight: Option[Float] = None,
    maxWeight: Option[Float] = None,
    overhang: Option[XYPair] = None,
    overhangOffset: Option[XYPair] = None,
    pattern: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("PalletizingParams")

enum CompensationProcess derives CanEqual:
  case Printing, Platemaking

final case class BarcodeCompParams(
    compensationProcess: CompensationProcess,
    compensationValue: Float,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("BarcodeCompParams")

final case class FontPolicy(
    preferredFont: Nmtoken,
    useDefaultFont: Boolean,
    useFontEmulation: Boolean,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("FontPolicy")

final case class TransferCurve(
    ctm: Option[Matrix] = None,
    curve: Option[TransferFunction] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("TransferCurve")

final case class BundlingParams(
    copies: Option[Int] = None,
    length: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("BundlingParams")

final case class InkZoneProfile(
    zoneSettingsX: Vector[Float],
    zoneWidth: Float,
    zoneHeight: Option[Float] = None,
    zoneSettingsY: Vector[Float] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("InkZoneProfile")

enum BundleType derives CanEqual:
  case BoundSet, Box, Carton, CollectedStack, CompensatedStack, Product, Pallet, Roll, Sheet, Stack
  case StrappedStack, StrappedCompensatedStack, WrappedBundle

final case class BundleItem(
    amount: Int,
    bundleType: Option[BundleType] = None,
    itemRef: Option[XsdIdRef] = None,
    totalAmount: Option[Int] = None,
    totalDimensions: Option[Shape3D] = None,
    totalVolume: Option[Float] = None,
    totalWeight: Option[Float] = None,
    children: Vector[BundleItem] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Bundle(
    items: NonEmptyVector[BundleItem],
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Bundle")

final case class Pallet(
    palletType: Nmtoken,
    size: Option[XYPair] = None,
    identificationFields: Vector[IdentificationField] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Pallet")

enum EmbossEdgeShape derives CanEqual:
  case Beveled, Rounded

final case class EmbossOperation(
    direction: EmbossDirection,
    embossingType: EmbossType,
    edgeAngle: Option[Float] = None,
    edgeShape: Option[EmbossEdgeShape] = None,
    face: Option[Face] = None,
    height: Option[Float] = None,
    imageSize: Option[XYPair] = None,
    position: Option[XYPair] = None,
    toolRef: Option[XsdIdRef] = None,
    identificationField: Option[IdentificationField] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class EmbossingParams(
    embosses: NonEmptyVector[EmbossOperation],
    moduleId: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("EmbossingParams")

final case class LabelingParams(
    application: Option[Nmtoken] = None,
    face: Option[Face] = None,
    offset: Option[XYPair] = None,
    fileSpec: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("LabelingParams")

enum InsertLocation derives CanEqual:
  case Back, FinishedPage, Front, Overfold

enum InsertingMethod derives CanEqual:
  case BindIn, BlowIn

final case class InsertingParams(
    insertLocation: InsertLocation,
    finishedPage: Option[Int] = None,
    method: Option[InsertingMethod] = None,
    glues: Vector[Glue] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("InsertingParams")

opaque type CommonFolds = Int
object CommonFolds:
  def from(value: Int): Either[ValidationError, CommonFolds] =
    Either.cond(
      value >= 2,
      value,
      ValidationError.InvalidValue("CommonFolds", value.toString, "an integer greater than or equal to two"),
    )

  extension (value: CommonFolds) def value: Int = value
end CommonFolds

final case class AssemblySection(
    binderySignatureId: Nmtoken,
    commonFolds: Option[CommonFolds] = None,
    descriptiveName: Option[XjdfString] = None,
    externalId: Option[Nmtoken] = None,
    sections: Vector[AssemblySection] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum AssemblyPlan:
  case Collecting(binderySignatureIds: Vector[Nmtoken] = Vector.empty)
  case Gathering(binderySignatureIds: Vector[Nmtoken] = Vector.empty)
  case None
  case Listed(sections: NonEmptyVector[AssemblySection])

final case class Assembly(
    plan: AssemblyPlan,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Assembly")
