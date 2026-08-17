package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

/** Present in the XSD substitution group but omitted by the generated hierarchy index. */
final case class RasterReadingParams(
    center: Option[Boolean] = None,
    filmRef: Option[XsdIdRef] = None,
    mirrorAround: Option[DeviceAxis] = None,
    paperRef: Option[XsdIdRef] = None,
    plateRef: Option[XsdIdRef] = None,
    polarity: Option[Polarity] = None,
    proofPaperRef: Option[XsdIdRef] = None,
    scaling: Option[XYPair] = None,
    scalingOrigin: Option[XYPair] = None,
    fitPolicy: Option[FitPolicy] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("RasterReadingParams")

/** XJDF 2.2 resource from table 6.162; it is absent from the checked-in XSD. */
final case class SheetOptimizingReport(
    areaUse: Float,
    volumeUse: Float,
    backUse: Option[Float] = None,
    dateSpread: Option[XsdDuration] = None,
    orderQuantity: Option[Int] = None,
    positions: Option[Int] = None,
    printableArea: Option[Float] = None,
    printedWaste: Option[Float] = None,
    uniquePositions: Option[Int] = None,
    uniqueUse: Option[Float] = None,
    wasteQuantity: Option[Int] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("SheetOptimizingReport")
