package xjdf4s.model

import xjdf4s.core.*

final case class Event(
    eventId: Nmtoken,
    eventValue: Option[XjdfString] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Milestone(
    milestoneType: Nmtoken,
    typeAmount: Option[Int] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Notification(
    severity: Severity,
    comments: Vector[Comment] = Vector.empty,
    event: Option[Event] = None,
    milestone: Option[Milestone] = None,
    parts: Vector[Part] = Vector.empty,
    foreignElements: Vector[ExtensionElement] = Vector.empty,
    jobId: Option[Nmtoken] = None,
    jobPartId: Option[Nmtoken] = None,
    moduleId: Option[Nmtoken] = None,
    queueEntryId: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum ProcessRunEndStatus derives CanEqual:
  case Aborted, Completed

final case class ProcessRun(
    end: XsdDateTime,
    endStatus: ProcessRunEndStatus,
    start: XsdDateTime,
    duration: Option[XsdDuration] = None,
    queueEntryId: Option[Nmtoken] = None,
    returnTime: Option[XsdDateTime] = None,
    submissionTime: Option[XsdDateTime] = None,
    parts: Vector[Part] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum CommandResult derives CanEqual:
  case Merged, New, Rejected, Removed, Replaced

enum ResourceLevel derives CanEqual:
  case Empty, Full, High, Low, OK

enum CostType derives CanEqual:
  case Chargeable, NonChargeable

enum WorkType derives CanEqual:
  case Original, Alteration, Rework

final case class MisDetails(
    complexity: Option[SheetLay] = None,
    costType: Option[CostType] = None,
    workType: Option[WorkType] = None,
    workTypeDetails: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ResourceInfo(
    resourceSet: ResourceSet,
    events: Vector[Event] = Vector.empty,
    misDetails: Option[MisDetails] = None,
    commandResult: Option[CommandResult] = None,
    jobId: Option[Nmtoken] = None,
    jobPartId: Option[Nmtoken] = None,
    level: Option[ResourceLevel] = None,
    moduleId: Option[Nmtoken] = None,
    queueEntryId: Option[Nmtoken] = None,
    scope: Option[Scope] = None,
    speed: Option[Float] = None,
    types: Vector[Nmtoken] = Vector.empty,
    totalAmount: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Activity(
    activityId: Option[Nmtoken] = None,
    activityName: Option[XjdfString] = None,
    endTime: Option[XsdDateTime] = None,
    personalId: Option[Nmtoken] = None,
    roles: Vector[Nmtoken] = Vector.empty,
    startTime: Option[XsdDateTime] = None,
    comment: Option[Comment] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class GangSource(
    copies: Int,
    jobId: Nmtoken,
    binderySignatureId: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum Deadline derives CanEqual:
  case InTime, Late, Warning

final case class JobPhase(
    jobId: Nmtoken,
    status: NodeStatus,
    amount: Option[Float] = None,
    costCenterId: Option[Nmtoken] = None,
    deadline: Option[Deadline] = None,
    endTime: Option[XsdDateTime] = None,
    jobPartId: Option[Nmtoken] = None,
    moduleIds: Vector[Nmtoken] = Vector.empty,
    percentCompleted: Option[Float] = None,
    queueEntryId: Option[Nmtoken] = None,
    relatedJobId: Option[XjdfString] = None,
    relatedJobPartId: Option[XjdfString] = None,
    restTime: Option[XsdDuration] = None,
    startTime: Option[XsdDateTime] = None,
    statusDetails: Option[Nmtoken] = None,
    toolIds: Vector[Nmtoken] = Vector.empty,
    totalAmount: Option[Float] = None,
    waste: Option[Float] = None,
    workStepId: Option[Nmtoken] = None,
    activities: Vector[Activity] = Vector.empty,
    gangSources: Vector[GangSource] = Vector.empty,
    misDetails: Option[MisDetails] = None,
    parts: Vector[Part] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum OverwritePolicy derives CanEqual:
  case Overwrite, RenameNew, RenameOld, NewVersion, OperatorIntervention, Abort

enum DispositionAction derives CanEqual:
  case Archive, Delete

enum DispositionTime:
  case AfterProcess(minimumDuration: XsdDuration)
  case At(until: XsdDateTime)

final case class Disposition(
    action: Option[DispositionAction] = None,
    time: Option[DispositionTime] = None,
    extraDuration: Option[XsdDuration] = None,
    priority: Option[Priority0To100] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class NetworkHeader(
    name: XjdfString,
    value: XjdfString,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum FileLocation:
  case Url(value: UriRef)
  case Uid(value: Nmtoken)
  case Sequence(format: XjdfString, template: NonEmptyVector[Nmtoken])
  case Pipe

final case class FileSpec(
    location: FileLocation = FileLocation.Pipe,
    checkSum: Option[Vector[Byte]] = None,
    encoding: Option[Nmtoken] = None,
    fileSize: Option[Long] = None,
    mimeType: Option[XjdfString] = None,
    numberOfPages: Option[Int] = None,
    overwritePolicy: Option[OverwritePolicy] = None,
    password: Option[XjdfString] = None,
    resourceUsage: Option[Nmtoken] = None,
    searchDepth: Option[Int] = None,
    userFileName: Option[XjdfString] = None,
    disposition: Option[Disposition] = None,
    networkHeaders: Vector[NetworkHeader] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class DeviceInfoSchemas(
    current: Option[FileSpec] = None,
    global: Option[FileSpec] = None,
)

final case class DeviceInfo(
    status: DeviceStatus,
    counterUnit: Option[Nmtoken] = None,
    endTime: Option[XsdDateTime] = None,
    hourCounter: Option[XsdDuration] = None,
    idleStartTime: Option[XsdDateTime] = None,
    moduleIds: Vector[Nmtoken] = Vector.empty,
    powerOnTime: Option[XsdDateTime] = None,
    productionCounter: Option[Float] = None,
    speed: Option[Float] = None,
    statusDetails: Option[Nmtoken] = None,
    toolIds: Vector[Nmtoken] = Vector.empty,
    totalProductionCounter: Option[Float] = None,
    activities: Vector[Activity] = Vector.empty,
    events: Vector[Event] = Vector.empty,
    schemas: DeviceInfoSchemas = DeviceInfoSchemas(),
    jobPhases: Vector[JobPhase] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible
