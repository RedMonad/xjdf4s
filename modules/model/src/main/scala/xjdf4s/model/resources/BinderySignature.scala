package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum BinderySignatureType derives CanEqual:
  case Fold, Grid, Die
end BinderySignatureType

enum Bottling derives CanEqual:
  case All, Last, None
end Bottling

enum OverfoldSide derives CanEqual:
  case Back, BackHalf, Front, FrontHalf
end OverfoldSide

enum CellMask derives CanEqual:
  case BleedBox, DieCut, None, PDL, SourceBleedBox, SourceTrimBox, TrimBox
end CellMask

enum CellOrientation derives CanEqual:
  case Down, Left, Right, Up
end CellOrientation

final case class MultiPageFold(
    binderySignatureId: Nmtoken,
    orientation: Orientation,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CellConditionTerm(
    parts: NonEmptyVector[Part],
    partContext: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CellCondition(
    side: Side,
    conditions: NonEmptyVector[CellConditionTerm],
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
    dieLayoutRef: Option[XsdId] = None,
    foldCatalog: Option[Nmtoken] = None,
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
