package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum ImageDownsampleType derives CanEqual:
  case Average, Bicubic, Subsample

enum CompressedImageType derives CanEqual:
  case Color, Grayscale, Monochrome

enum DctColorTransform derives CanEqual:
  case None, YUV, Automatic

enum Jpeg2000ProgressionOrder derives CanEqual:
  case LRCP, RLCP, RPCL, PCRL, CPRL

final case class CcittFaxParams(
    encodedByteAlign: Option[Boolean] = None,
    endOfBlock: Option[Boolean] = None,
    endOfLine: Option[Boolean] = None,
    k: Option[Int] = None,
    uncompressed: Option[Boolean] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class DctParams(
    colorTransform: Option[DctColorTransform] = None,
    horizontalSamples: Vector[Int] = Vector.empty,
    huffmanTable: Vector[Float] = Vector.empty,
    qualityFactor: Option[Float] = None,
    quantizationTable: Vector[Float] = Vector.empty,
    verticalSamples: Vector[Int] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class FlateParams(
    predictor: Option[Int] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Jbig2Params(
    lossless: Option[Boolean] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Jpeg2000Params(
    codeBlockSize: Option[Int] = None,
    layerRates: Vector[Float] = Vector.empty,
    layersPerTile: Option[Int] = None,
    numberOfResolutions: Option[Int] = None,
    progressionOrder: Option[Jpeg2000ProgressionOrder] = None,
    tileSize: Option[XYPair] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class LzwParams(
    earlyChange: Option[Int] = None,
    predictor: Option[Int] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ImageCompression(
    antiAliasImages: Option[Boolean] = None,
    autoFilterImages: Option[Boolean] = None,
    convertImagesToIndexed: Option[Boolean] = None,
    dctQuality: Option[Float] = None,
    downsampleImages: Option[Boolean] = None,
    encodeImages: Option[Boolean] = None,
    imageAutoFilterStrategy: Option[Nmtoken] = None,
    imageDepth: Option[Int] = None,
    imageDownsampleThreshold: Option[Float] = None,
    imageDownsampleType: Option[ImageDownsampleType] = None,
    imageFilter: Option[Nmtoken] = None,
    imageResolution: Option[Float] = None,
    imageType: Option[CompressedImageType] = None,
    jpxQuality: Option[Int] = None,
    ccittFaxParams: Option[CcittFaxParams] = None,
    dctParams: Option[DctParams] = None,
    flateParams: Option[FlateParams] = None,
    jbig2Params: Option[Jbig2Params] = None,
    jpeg2000Params: Option[Jpeg2000Params] = None,
    lzwParams: Option[LzwParams] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ImageCompressionParams(
    compressions: NonEmptyVector[ImageCompression],
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ImageCompressionParams")
