package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum SheetOptimizingPolicy derives CanEqual:
  case All, Collect, Optimized

enum PositionPolicy derives CanEqual:
  case Exact, Free

final case class GangElement(
    gangElementId: Nmtoken,
    orderQuantity: Int,
    binderySignatureIds: Vector[Nmtoken] = Vector.empty,
    collapseBleeds: Option[Boolean] = None,
    customerId: Option[Nmtoken] = None,
    dimension: Option[XYPair] = None,
    deliveryDate: Option[XsdDateTime] = None,
    dueDate: Option[XsdDateTime] = None,
    externalId: Option[Nmtoken] = None,
    fillPriority: Option[Int] = None,
    grainDirection: Option[MediaDirection] = None,
    groupCode: Option[Nmtoken] = None,
    jobId: Option[Nmtoken] = None,
    maxQuantity: Option[Int] = None,
    mediaRef: Option[XsdIdRef] = None,
    minQuantity: Option[Int] = None,
    numberOfPages: Option[Int] = None,
    numberUp: Option[GridSize] = None,
    oneSheet: Option[Nmtoken] = None,
    operations: Vector[Nmtoken] = Vector.empty,
    pageDimension: Option[XYPair] = None,
    placedQuantity: Option[Int] = None,
    priority: Option[Priority0To100] = None,
    rotationPolicy: Option[PositionPolicy] = None,
    runListRef: Option[XsdIdRef] = None,
    separationsBack: Vector[Nmtoken] = Vector.empty,
    separationsFront: Vector[Nmtoken] = Vector.empty,
    generalIds: Vector[GeneralId] = Vector.empty,
    media: Option[Media] = None,
    runLists: Vector[RunList] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class SheetOptimizingParams(
    gangElements: NonEmptyVector[GangElement],
    policy: Option[SheetOptimizingPolicy] = None,
    convertingConfigs: Vector[ConvertingConfig] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("SheetOptimizingParams")
