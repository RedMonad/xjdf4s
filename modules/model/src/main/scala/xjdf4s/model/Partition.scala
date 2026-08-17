package xjdf4s.model

import scala.util.Try

import xjdf4s.core.*

final case class XYPair(x: Double, y: Double) derives CanEqual
final case class TileCoordinate(x: Int, y: Int) derives CanEqual

enum PreviewType derives CanEqual:
  case Animation, Identification, SeparatedThumbNail, Separation, SeparationRaw, Static3D, ThumbNail, Viewable
end PreviewType

enum TransferCurveName derives CanEqual:
  case Film, Plate, Press, Proof, Substrate
end TransferCurveName

/**
 * Resource partition selector from XJDF 2.2 table 6.4. The five partition keys `DocIndex`, `PageNumber`, `RunIndex`,
 * `SetIndex` and `SheetIndex` are two-integer `IntegerRange` values, not arbitrary range expressions, so their
 * fixed-length integer semantics are preserved by the type.
 */
final case class Part(
    binderySignatureId: Option[Nmtoken] = None,
    blockName: Option[Nmtoken] = None,
    contactType: Option[Nmtoken] = None,
    docIndex: Option[IntegerRange] = None,
    dropId: Option[Nmtoken] = None,
    location: Option[Nmtoken] = None,
    lotId: Option[Nmtoken] = None,
    metadata: Option[String] = None,
    option: Option[Nmtoken] = None,
    pageNumber: Option[IntegerRange] = None,
    partVersion: Option[Nmtoken] = None,
    previewType: Option[PreviewType] = None,
    printCondition: Option[Nmtoken] = None,
    product: Option[Nmtoken] = None,
    productPart: Option[Nmtoken] = None,
    qualityMeasurement: Option[Nmtoken] = None,
    run: Option[Nmtoken] = None,
    runIndex: Option[IntegerRange] = None,
    separation: Option[Nmtoken] = None,
    setIndex: Option[IntegerRange] = None,
    sheetIndex: Option[IntegerRange] = None,
    sheetName: Option[Nmtoken] = None,
    side: Option[Side] = None,
    stationName: Option[Nmtoken] = None,
    tileId: Option[TileCoordinate] = None,
    transferCurveName: Option[TransferCurveName] = None,
    webName: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible,
      ValidatedNode:

  override def validate: Vector[ValidationError] =
    val ranges = Vector(
      ("DocIndex", docIndex),
      ("PageNumber", pageNumber),
      ("RunIndex", runIndex),
      ("SetIndex", setIndex),
      ("SheetIndex", sheetIndex),
    )
    val rangeErrors = ranges.collect:
      case (name, Some(range)) if range.first > range.last =>
        ValidationError.InvalidValue(s"Part/@$name", s"${range.first} ${range.last}", "first <= last")
    val metadataErrors = metadata.toVector.flatMap: pattern =>
      if Try(pattern.r).isSuccess then Vector.empty
      else Vector(ValidationError.InvalidValue("Part/@Metadata", pattern, "a valid regular expression"))
    rangeErrors ++ metadataErrors
end Part
