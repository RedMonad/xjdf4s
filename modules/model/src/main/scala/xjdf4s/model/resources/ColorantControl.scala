package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

final case class ColorantAlias(
    colorantName: XjdfString,
    replacementColorantName: Nmtoken,
    rawName: Vector[Byte] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class DeviceNSpace(
    name: Nmtoken,
    separations: NonEmptyVector[Nmtoken],
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum MappingSelection derives CanEqual:
  case UsePDLValues, UseLocalPrinterValues, UseProcessColorValues
end MappingSelection

enum ProcessColorModel derives CanEqual:
  case DeviceCMY, DeviceCMYK, DeviceGray, DeviceN, DeviceRGB, None
end ProcessColorModel

final case class ColorantControl(
    colorantConvertProcess: Vector[Nmtoken] = Vector.empty,
    colorantOrder: Vector[Nmtoken] = Vector.empty,
    colorantParams: Vector[Nmtoken] = Vector.empty,
    mappingSelection: Option[MappingSelection] = None,
    processColorModel: Option[ProcessColorModel] = None,
    colorantAliases: Vector[ColorantAlias] = Vector.empty,
    deviceNSpace: Option[DeviceNSpace] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ColorantControl")
