package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum ProductionLooseBinding:
  case Channel(details: Option[ChannelBindingProductionDetails] = Option.empty)
  case Coil(details: Option[CoilBindingProductionDetails] = Option.empty)
  case Comb(details: Option[CombBindingProductionDetails] = Option.empty)
  case Ring(details: Option[RingBindingProductionDetails] = Option.empty)
  case Strip(details: Option[StripBindingProductionDetails] = Option.empty)
end ProductionLooseBinding

final case class ChannelBindingProductionDetails(
    clampDistance: Option[Float] = None,
    clampSize: Option[Shape3D] = None,
    cover: Option[Boolean] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CoilBindingProductionDetails(
    coilShape: Option[Nmtoken] = None,
    diameter: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CombBindingProductionDetails(
    combShape: Option[Nmtoken] = None,
    diameter: Option[Float] = None,
    flipBackCover: Option[Boolean] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class RingBindingProductionDetails(
    diameter: Option[Float] = None,
    ringMechanic: Option[Boolean] = None,
    ringShape: Option[Nmtoken] = None,
    rivetsExposed: Option[Boolean] = None,
    spineWidth: Option[Float] = None,
    viewBinder: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class StripBindingProductionDetails(
    length: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class LooseBindingParams(
    binding: ProductionLooseBinding,
    coverMaterial: Option[Nmtoken] = None,
    holePatterns: Vector[HolePattern] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("LooseBindingParams")

enum BoxFoldingType derives CanEqual:
  case Type00, Type01, Type02, Type03, Type04, Type10, Type11, Type12, Type13, Type15, Type20
end BoxFoldingType

enum BoxFoldActionType derives CanEqual:
  case LongFoldLeftToRight, LongFoldRightToLeft, LongPreFoldLeftToRight, LongPreFoldRightToLeft
  case FrontFoldComplete, FrontFoldDiagonal, FrontFoldCompleteDiagonal
  case BackFoldComplete, BackFoldDiagonal, BackFoldCompleteDiagonal
  case ReverseFold, Milling, Rotate90, Rotate180, Rotate270

  /** New in XJDF 2.2: glue application is now an individual action of the folder-gluer sequence. */
  case Glue
end BoxFoldActionType

final case class BoxFoldAction(
    action: BoxFoldActionType,
    foldIndex: XYPair,
    glue: Option[Glue] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

/**
 * Tables 6.17/6.19/6.20 and release note H.1: a folder-gluer program is an ordered, repeatable sequence of
 * `BoxFoldAction` elements; glue application is the `Action = "Glue"` action with a child `Glue` element. The
 * deprecated 2.1-style top-level `Glue` children remain representable for compatibility but are optional.
 */
final case class BoxFoldingParams(
    boxFoldingType: BoxFoldingType,
    actions: Vector[BoxFoldAction] = Vector.empty,
    legacyGlues: Vector[Glue] = Vector.empty,
    blankDimensionsX: Vector[Float] = Vector.empty,
    blankDimensionsY: Vector[Float] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("BoxFoldingParams")

enum CollatingPlacement:
  case ByOrientation(value: Orientation)
  case ByTransformation(value: Matrix)
end CollatingPlacement

enum TransformationContext derives CanEqual:
  case CollateItem, Component, StackItem
end TransformationContext

final case class CollatingItem(
    amount: Option[Int] = None,
    componentRef: Option[XsdIdRef] = None,
    placement: Option[CollatingPlacement] = None,
    transformationContext: Option[TransformationContext] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum FeedQuality derives CanEqual:
  case NotActive, Check, Waste, StopNoWaste, StopWaste
end FeedQuality

final case class FeederQualityParams(
    badFeedQuality: Option[FeedQuality] = None,
    badFeeds: Option[Int] = None,
    doubleFeedQuality: Option[FeedQuality] = None,
    doubleFeeds: Option[Int] = None,
    incorrectComponentQuality: Option[FeedQuality] = None,
    incorrectComponents: Option[Int] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum FeederSynchronization derives CanEqual:
  case Alternate, Backup, Chain, Primary
end FeederSynchronization

enum FeederOpening derives CanEqual:
  case Back, Front, None, Sucker
end FeederOpening

final case class Feeder(
    alternatePositions: Vector[Int] = Vector.empty,
    componentRef: Option[XsdIdRef] = None,
    synchronization: Option[FeederSynchronization] = None,
    feederType: Option[Nmtoken] = None,
    loading: Option[Nmtoken] = None,
    opening: Option[FeederOpening] = None,
    position: Option[Int] = None,
    quality: Option[FeederQualityParams] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class FeedingParams(
    collatingItems: Vector[CollatingItem] = Vector.empty,
    feeders: Vector[Feeder] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("FeedingParams")
