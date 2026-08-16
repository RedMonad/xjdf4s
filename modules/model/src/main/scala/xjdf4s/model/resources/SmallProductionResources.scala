package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

final case class InkZoneCalculationParams(
    printableArea: Option[Rectangle] = None,
    zoneHeight: Option[Float] = None,
    zones: Option[Int] = None,
    zonesY: Option[Int] = None,
    zoneWidth: Option[Float] = None,
    device: Option[Device] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("InkZoneCalculationParams")

final case class LayoutElementProductionParams(
    contentRefs: Vector[XsdId] = Vector.empty,
    shapeDefRef: Option[XsdId] = None,
    dataList: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("LayoutElementProductionParams")

enum FolderProductionType derives CanEqual:
  case Collect, NonCollect
end FolderProductionType

final case class FolderProduction(
    moduleId: Option[Nmtoken] = None,
    productionType: Option[FolderProductionType] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ProductionPath(
    productionPathId: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class WebInlineFinishingParams(
    folderProductions: Vector[FolderProduction] = Vector.empty,
    productionPath: Option[ProductionPath] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("WebInlineFinishingParams")
