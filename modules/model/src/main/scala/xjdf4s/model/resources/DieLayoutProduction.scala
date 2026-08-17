package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum Anchor derives CanEqual:
  case BottomCenter, BottomLeft, BottomRight, Center, CenterLeft, CenterRight, TopCenter, TopLeft, TopRight
end Anchor

enum AllowedRotation derives CanEqual:
  case None, Grain, MinorGrain, CrossGrain
end AllowedRotation

final case class ConvertingConfig(
    marginBottom: Option[Float] = None,
    marginLeft: Option[Float] = None,
    marginRight: Option[Float] = None,
    marginTop: Option[Float] = None,
    sheetHeightMax: Option[Float] = None,
    sheetHeightMin: Option[Float] = None,
    sheetWidthMax: Option[Float] = None,
    sheetWidthMin: Option[Float] = None,
    cutBlocks: Vector[CutBlock] = Vector.empty,
    devices: Vector[Device] = Vector.empty,
    media: Vector[Media] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class RepeatDescription(
    shapeDefRef: XsdIdRef,
    allowedRotation: Option[AllowedRotation] = None,
    gutterX: Option[Float] = None,
    gutterX2: Option[Float] = None,
    gutterY: Option[Float] = None,
    gutterY2: Option[Float] = None,
    layoutStyles: Vector[Nmtoken] = Vector.empty,
    orderQuantity: Option[Int] = None,
    useBleed: Option[Boolean] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class DieLayoutProductionParams(
    convertingConfigs: NonEmptyVector[ConvertingConfig],
    repeatDescriptions: NonEmptyVector[RepeatDescription],
    estimate: Option[Boolean] = None,
    position: Option[Anchor] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("DieLayoutProductionParams")
