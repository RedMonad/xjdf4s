package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

final case class DropItem(
    amount: Int,
    itemRef: XsdIdRef,
    totalDimensions: Option[Shape3D] = None,
    totalVolume: Option[Float] = None,
    totalWeight: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum DeliveryOwnership derives CanEqual:
  case Destination, Origin

enum DeliveryTransfer derives CanEqual:
  case BuyerToPrinterDeliver, BuyerToPrinterPickup, PrinterToBuyerDeliver, PrinterToBuyerPickup

final case class DeliveryFiles(
    contents: Option[FileSpec] = None,
    mailingList: Option[FileSpec] = None,
    remoteUrl: Option[FileSpec] = None,
)

final case class DeliveryParams(
    buyerAccount: Option[XjdfString] = None,
    earliest: Option[XsdDateTime] = None,
    earliestDuration: Option[XsdDuration] = None,
    method: Option[Nmtoken] = None,
    ownership: Option[DeliveryOwnership] = None,
    required: Option[XsdDateTime] = None,
    requiredDuration: Option[XsdDuration] = None,
    trackingId: Option[XjdfString] = None,
    transfer: Option[DeliveryTransfer] = None,
    dropItems: Vector[DropItem] = Vector.empty,
    files: DeliveryFiles = DeliveryFiles(),
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("DeliveryParams")

enum PreflightAction derives CanEqual:
  case Abort, Continue, Repair

final case class PreflightTest(
    action: Option[PreflightAction] = None,
    descriptiveName: Option[XjdfString] = None,
    severity: Option[Severity] = None,
    testClass: Option[Nmtoken] = None,
    testId: Option[Nmtoken] = None,
    generalIds: Vector[GeneralId] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class PreflightParams(
    fileSpec: Option[FileSpec] = None,
    tests: Vector[PreflightTest] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("PreflightParams")

final case class PreflightCheck(
    action: Option[PreflightAction] = None,
    count: Option[Int] = None,
    pages: Vector[Int] = Vector.empty,
    severity: Option[Severity] = None,
    testClass: Option[Nmtoken] = None,
    testId: Option[Nmtoken] = None,
    comment: Option[Comment] = None,
    generalIds: Vector[GeneralId] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class PreflightReport(
    errorCount: Option[Int] = None,
    warningCount: Option[Int] = None,
    fileSpec: Option[FileSpec] = None,
    checks: Vector[PreflightCheck] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("PreflightReport")
