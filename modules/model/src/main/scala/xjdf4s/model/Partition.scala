package xjdf4s.model

import xjdf4s.core.*

final case class XYPair(x: Int, y: Int) derives CanEqual

enum PreviewType derives CanEqual:
  case Animation, Identification, SeparatedThumbNail, Separation, SeparationRaw, Static3D, ThumbNail, Viewable
end PreviewType

enum TransferCurveName derives CanEqual:
  case Film, Plate, Press, Proof, Substrate
end TransferCurveName

/** Resource partition selector from XJDF 2.2 table 6.4. */
final case class Part(
    binderySignatureId: Option[Nmtoken] = None,
    blockName: Option[XYPair] = None,
    contactType: Option[Nmtoken] = None,
    docIndex: Option[RangeExpression] = None,
    dropId: Option[Nmtoken] = None,
    location: Option[Nmtoken] = None,
    lotId: Option[Nmtoken] = None,
    metadata: Option[String] = None,
    option: Option[Nmtoken] = None,
    pageNumber: Option[RangeExpression] = None,
    partVersion: Option[Nmtoken] = None,
    previewType: Option[PreviewType] = None,
    printCondition: Option[Nmtoken] = None,
    product: Option[Nmtoken] = None,
    productPart: Option[Nmtoken] = None,
    qualityMeasurement: Option[Nmtoken] = None,
    run: Option[Nmtoken] = None,
    runIndex: Option[RangeExpression] = None,
    separation: Option[Nmtoken] = None,
    setIndex: Option[RangeExpression] = None,
    sheetIndex: Option[RangeExpression] = None,
    sheetName: Option[Nmtoken] = None,
    side: Option[Side] = None,
    stationName: Option[Nmtoken] = None,
    tileId: Option[XYPair] = None,
    transferCurveName: Option[TransferCurveName] = None,
    webName: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible
