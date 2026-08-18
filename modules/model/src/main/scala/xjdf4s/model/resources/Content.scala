package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

final case class BarcodeProductionParams(
    reproduction: Option[BarcodeReproParams] = None,
    identificationField: Option[IdentificationField] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ContentMetadata(
    contactRefs: Vector[XsdIdRef] = Vector.empty,
    isbn: Option[Nmtoken] = None,
    title: Option[Nmtoken] = None,
    comment: Option[Comment] = None,
    generalIds: Vector[GeneralId] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum RefAnchorType derives CanEqual:
  case Parent, Sibling

final case class RefAnchor(
    anchor: Anchor,
    anchorType: RefAnchorType,
    ref: XsdIdRef,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class PositionedObject(
    anchor: Option[Anchor] = None,
    ctm: Option[Matrix] = None,
    pageRange: Option[IntegerRange] = None,
    positionPolicy: Option[PositionPolicy] = None,
    relativeSize: Option[XYPair] = None,
    rotationPolicy: Option[PositionPolicy] = None,
    size: Option[XYPair] = None,
    sizePolicy: Option[PositionPolicy] = None,
    refAnchor: Option[RefAnchor] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Content(
    binderySignatureIds: Vector[Nmtoken] = Vector.empty,
    contentStatus: Vector[Nmtoken] = Vector.empty,
    contentType: Option[Nmtoken] = None,
    hasBleeds: Option[Boolean] = None,
    isBlank: Option[Boolean] = None,
    isTrapped: Option[Boolean] = None,
    pageLabel: Option[XjdfString] = None,
    separations: Vector[Nmtoken] = Vector.empty,
    sourceBleedBox: Option[Rectangle] = None,
    sourceClipBox: Option[Rectangle] = None,
    sourceTrimBox: Option[Rectangle] = None,
    barcodeProductionParams: Option[BarcodeProductionParams] = None,
    metadata: Option[ContentMetadata] = None,
    fileSpecs: Vector[FileSpec] = Vector.empty,
    imageCompression: Option[ImageCompression] = None,
    optionalContentControls: Vector[OptionalContentGroupControl] = Vector.empty,
    positionedObjects: Vector[PositionedObject] = Vector.empty,
    screenSelector: Option[ScreenSelector] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Content")
