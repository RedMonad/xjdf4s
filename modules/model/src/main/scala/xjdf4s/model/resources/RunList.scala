package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum BandOrdering derives CanEqual:
  case BandMajor, ColorMajor
end BandOrdering

final case class Band(
    height: Option[Int] = None,
    width: Option[Int] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ByteMap(
    bandOrdering: Option[BandOrdering] = None,
    frameHeight: Option[Int] = None,
    frameWidth: Option[Int] = None,
    halftoned: Option[Boolean] = None,
    interleaved: Option[Boolean] = None,
    pixelColorants: Vector[Nmtoken] = Vector.empty,
    pixelDepth: Option[Int] = None,
    pixelSkip: Option[Int] = None,
    resolution: Option[XYPair] = None,
    band: Option[Band] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum RunListOrderType derives CanEqual:
  case Content, Insert, Reservation
end RunListOrderType

final case class RunList(
    automation: Option[Automation] = None,
    clipPath: Option[PdfPath] = None,
    contentRefs: Vector[XsdIdRef] = Vector.empty,
    docs: Option[IntegerRange] = None,
    docPages: Vector[Int] = Vector.empty,
    endOfDocument: Option[Boolean] = None,
    endOfSet: Option[Boolean] = None,
    finishedPages: Option[Int] = None,
    logicalPage: Option[Int] = None,
    numberOfPages: Option[Int] = None,
    orderType: Option[RunListOrderType] = None,
    pages: Option[IntegerRange] = None,
    sets: Option[IntegerRange] = None,
    sourceBleedBox: Option[Rectangle] = None,
    sourceClipBox: Option[Rectangle] = None,
    sourceMediaBox: Option[Rectangle] = None,
    sourceTrimBox: Option[Rectangle] = None,
    byteMap: Option[ByteMap] = None,
    fileSpec: Option[FileSpec] = None,
    metadataMaps: Vector[MetadataMap] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("RunList")
