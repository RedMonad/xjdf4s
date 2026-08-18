package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum BinderySignatureType derives CanEqual:
  case Fold, Grid, Die

enum Bottling derives CanEqual:
  case All, Last, None

enum OverfoldSide derives CanEqual:
  case Back, BackHalf, Front, FrontHalf

enum CellMask derives CanEqual:
  case BleedBox, DieCut, None, PDL, SourceBleedBox, SourceTrimBox, TrimBox

enum CellOrientation derives CanEqual:
  case Down, Left, Right, Up

final case class MultiPageFold(
    binderySignatureId: Nmtoken,
    orientation: Orientation,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

/** The Table 8.15 `Condition` element, shared by `Layout` (PageActivation, PageCondition, SheetActivation) and
 *  `BinderySignature` (CellCondition). `@PartContext` is a normative `NMTOKENS` list of partition keys that reset the
 *  Part context; modelling it as a list makes multi-key resets such as `PartContext="DocIndex SetIndex"`
 *  representable. The two former isomorphic clones (`LayoutCondition`, `CellConditionTerm`) are unified here.
 */
final case class Condition(
    parts: NonEmptyVector[Part],
    partContext: Vector[Nmtoken] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CellCondition(
    side: Side,
    conditions: NonEmptyVector[Condition],
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class SignatureCell(
    backPages: Vector[Int] = Vector.empty,
    backSpread: Vector[Int] = Vector.empty,
    bleedFace: Option[Float] = None,
    bleedFoot: Option[Float] = None,
    bleedHead: Option[Float] = None,
    bleedSpine: Option[Float] = None,
    faceCells: Vector[Int] = Vector.empty,
    frontPages: Vector[Int] = Vector.empty,
    frontSpread: Vector[Int] = Vector.empty,
    mask: Option[CellMask] = None,
    maskBleed: Option[Float] = None,
    maskSeparation: Option[Nmtoken] = None,
    orientation: Option[CellOrientation] = None,
    sides: Option[Sides] = None,
    stationName: Option[Nmtoken] = None,
    trimFace: Option[Float] = None,
    trimFoot: Option[Float] = None,
    trimHead: Option[Float] = None,
    trimSize: Option[XYPair] = None,
    trimSpine: Option[Float] = None,
    conditions: Vector[CellCondition] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class BinderySignature(
    signatureType: BinderySignatureType,
    size: Option[XYPair] = None,
    bindingOrientation: Option[Orientation] = None,
    bottling: Option[Bottling] = None,
    dieLayoutRef: Option[XsdIdRef] = None,
    foldCatalog: Option[FoldCatalog] = None,
    numberUp: Option[GridSize] = None,
    overfold: Option[Float] = None,
    overfoldSide: Option[OverfoldSide] = None,
    spreadType: Option[SpreadType] = None,
    staggerColumns: Vector[Float] = Vector.empty,
    staggerContinuous: Option[Boolean] = None,
    staggerRows: Vector[Float] = Vector.empty,
    multiPageFolds: Vector[MultiPageFold] = Vector.empty,
    signatureCells: Vector[SignatureCell] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("BinderySignature")
