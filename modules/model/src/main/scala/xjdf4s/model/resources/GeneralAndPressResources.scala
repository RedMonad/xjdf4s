package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum ApprovalRole derives CanEqual:
  case Approvinator, Informative, Obligated

final case class ApprovalPerson(
    contactRef: XsdIdRef,
    approvalRole: Option[ApprovalRole] = None,
    approvalRoleDetails: Option[XjdfString] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum ApprovalState derives CanEqual:
  case Approved, ApprovedWithComment, Rejected

final case class ApprovalDetails(
    approvalState: ApprovalState,
    approvalStateDetails: Option[XjdfString] = None,
    approvalPerson: Option[ApprovalPerson] = None,
    comment: Option[Comment] = None,
    fileSpec: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ApprovalDetails")

final case class ApprovalParams(
    approvalPeople: NonEmptyVector[ApprovalPerson],
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ApprovalParams")

final case class VerificationParams(
    tolerance: Option[Float] = None,
    fileSpec: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("VerificationParams")

/** Named slots preserve the four distinct FileSpec roles from table 6.182. */
final case class VerificationFiles(
    accepted: Option[FileSpec] = None,
    combined: Option[FileSpec] = None,
    rejected: Option[FileSpec] = None,
    unknown: Option[FileSpec] = None,
)

final case class VerificationResult(
    accepted: Option[Int] = None,
    rejected: Option[Int] = None,
    unknown: Option[Int] = None,
    files: VerificationFiles = VerificationFiles(),
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("VerificationResult")

enum Drying derives CanEqual:
  case Heatset, IR, Off, On, UV

enum FountainSolution derives CanEqual:
  case On, Off

enum WorkStyle derives CanEqual:
  case Perfecting, Simplex, WorkAndBack, WorkAndTumble, WorkAndTurn

final case class ConventionalPrintingParams(
    drying: Option[Drying] = None,
    firstSurface: Option[Side] = None,
    fountainSolution: Option[FountainSolution] = None,
    moduleDrying: Option[Drying] = None,
    powder: Option[Float] = None,
    sheetLay: Option[SheetLay] = None,
    speed: Option[Float] = None,
    workStyle: Option[WorkStyle] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ConventionalPrintingParams")

enum DigitalCollation derives CanEqual:
  case None, Sheet

enum PageDelivery derives CanEqual:
  case FanFold, SameOrderFaceUp, SameOrderFaceDown, ReverseOrderFaceUp, ReverseOrderFaceDown

enum DigitalSides derives CanEqual:
  case OneSidedFront, OneSidedBack, TwoSided, Unprinted

final case class DigitalPrintingParams(
    collate: Option[DigitalCollation] = None,
    manualFeed: Option[Boolean] = None,
    pageDelivery: Option[PageDelivery] = None,
    sheetLay: Option[SheetLay] = None,
    sides: Option[DigitalSides] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("DigitalPrintingParams")

enum PreviewCompensation derives CanEqual:
  case None, Film, Plate, Press

enum PreviewFileType derives CanEqual:
  case PNG, CIP3Multiple, CIP3Single

final case class Preview(
    compensation: Option[PreviewCompensation] = None,
    ctm: Option[Matrix] = None,
    previewFileType: Option[PreviewFileType] = None,
    fileSpec: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Preview")
