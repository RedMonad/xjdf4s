package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

final case class CreasingParams(
    creases: NonEmptyVector[Crease],
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("CreasingParams")

final case class HoleMakingParams(
    patterns: NonEmptyVector[HolePattern],
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("HoleMakingParams")

final case class Cut(
    cutWidth: Option[Float] = None,
    startPosition: Option[XYPair] = None,
    workingDirection: Option[TopBottom] = None,
    workingPath: Option[XYPair] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class FoldingParams(
    foldCatalog: Option[Nmtoken] = None,
    foldingDetails: Option[Nmtoken] = None,
    sheetLay: Option[SheetLay] = None,
    fileSpec: Option[FileSpec] = None,
    creases: Vector[Crease] = Vector.empty,
    cuts: Vector[Cut] = Vector.empty,
    folds: Vector[Fold] = Vector.empty,
    perforations: Vector[Perforate] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("FoldingParams")

enum SewingPattern derives CanEqual:
  case CombinedStaggered, Normal, Side, Staggered
end SewingPattern

final case class ThreadSewingParams(
    blindStitch: Option[Boolean] = None,
    needlePositions: Vector[Float] = Vector.empty,
    numberOfNeedles: Option[Int] = None,
    offset: Option[Float] = None,
    sewingPattern: Option[SewingPattern] = None,
    threadThickness: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ThreadSewingParams")
