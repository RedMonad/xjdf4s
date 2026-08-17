package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

final case class RuleLength(
    ddesCutType: Int,
    length: Float,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ShapeDef(
    area: Option[Float] = None,
    cutBox: Option[Rectangle] = None,
    cutLines: Vector[Nmtoken] = Vector.empty,
    dimensions: Option[Shape3D] = None,
    flatDimensions: Option[Shape3D] = None,
    fluteDirection: Option[MediaDirection] = None,
    grainDirection: Option[MediaDirection] = None,
    mediaRef: Option[XsdIdRef] = None,
    mediaSide: Option[Side] = None,
    resourceWeight: Option[Float] = None,
    fileSpecs: Vector[FileSpec] = Vector.empty,
    ruleLengths: Vector[RuleLength] = Vector.empty,
    shapes: Vector[Shape] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ShapeDef")

final case class ObjectModel(
    dimensions: Option[Shape3D] = None,
    fileSpec: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ShapeDimension(
    usage: XjdfString,
    value: Float,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ShapeTemplate(
    innerDimensions: Option[Shape3D] = None,
    name: Option[Nmtoken] = None,
    standard: Option[Nmtoken] = None,
    fileSpecs: Vector[FileSpec] = Vector.empty,
    dimensions: Vector[ShapeDimension] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ShapeDefProductionParams(
    objectModels: Vector[ObjectModel] = Vector.empty,
    shapeTemplate: Option[ShapeTemplate] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ShapeDefProductionParams")
