package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

final case class ObjectResolution(
    resolution: XYPair,
    antiAliasing: Option[Nmtoken] = None,
    sourceObjects: Vector[SourceObject] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum TiffByteOrder derives CanEqual:
  case II, MM
end TiffByteOrder

enum TiffSegmentation derives CanEqual:
  case SingleStrip, Stripped, Tiled
end TiffSegmentation

final case class TiffEmbeddedFile(
    tagNumber: Int,
    tagType: Int,
    fileSpec: FileSpec,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum TiffTagValue:
  case Binary(value: Vector[Byte])
  case Integers(value: Vector[Int])
  case Numbers(value: Vector[Float])
  case Text(value: String)
end TiffTagValue

final case class TiffTag(
    tagNumber: Int,
    tagType: Int,
    value: Option[TiffTagValue] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class TiffFormatParams(
    byteOrder: Option[TiffByteOrder] = None,
    interleaving: Option[Int] = None,
    rowsPerStrip: Option[Int] = None,
    segmentation: Option[TiffSegmentation] = None,
    separationNameTag: Option[Int] = None,
    tileSize: Option[XYPair] = None,
    whiteIsZero: Option[Boolean] = None,
    embeddedFiles: Vector[TiffEmbeddedFile] = Vector.empty,
    tags: Vector[TiffTag] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class RenderingParams(
    bandHeight: Option[Int] = None,
    bandOrdering: Option[BandOrdering] = None,
    bandWidth: Option[Int] = None,
    colorantDepth: Option[Int] = None,
    interleaved: Option[Boolean] = None,
    mimeType: Option[String] = None,
    automatedOverPrintParams: Option[AutomatedOverPrintParams] = None,
    objectResolutions: Vector[ObjectResolution] = Vector.empty,
    tiffFormatParams: Option[TiffFormatParams] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("RenderingParams")
