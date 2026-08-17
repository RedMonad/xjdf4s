package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.*

enum ForceGangPolicy derives CanEqual:
  case All, Optimized
end ForceGangPolicy

final case class GangCommandFilter(
    gangNames: Vector[Nmtoken] = Vector.empty,
    policy: Option[ForceGangPolicy] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CommandForceGang(
    header: Header,
    filter: GangCommandFilter,
    extensions: Extensions = Extensions.empty,
) extends Command:
  val elementName: QualifiedName = MessageNames.element("CommandForceGang")

final case class ResponseForceGang(
    header: Header,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseForceGang")

final case class GangQueryFilter(
    gangNames: Vector[Nmtoken] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class GangInfo(
    gangName: Nmtoken,
    amount: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QueryGangStatus(
    header: Header,
    filter: Option[GangQueryFilter] = None,
    subscription: Option[Subscription] = None,
    extensions: Extensions = Extensions.empty,
) extends Query:
  val elementName: QualifiedName = MessageNames.element("QueryGangStatus")

final case class ResponseGangStatus(
    header: Header,
    gangs: Vector[GangInfo] = Vector.empty,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseGangStatus")

final case class SignalGangStatus(
    header: Header,
    gangs: Vector[GangInfo] = Vector.empty,
    channelMode: Option[ChannelMode] = None,
    extensions: Extensions = Extensions.empty,
) extends Signal:
  val elementName: QualifiedName = MessageNames.element("SignalGangStatus")

enum UpdateGranularity derives CanEqual:
  case All, ChangesOnly
end UpdateGranularity

/** Compatibility alias: the 0..100 priority is the shared core type [[Priority0To100]]. */
type QueuePriority = Priority0To100

enum QueueActivation derives CanEqual:
  case Informative, Held, Active, PendingReturn, Removed
end QueueActivation

enum QueueGangPolicy derives CanEqual:
  case Gang, GangAndForce, NoGang
end QueueGangPolicy

final case class QueueFilter(
    firstEntry: Option[Nmtoken] = None,
    gangNames: Vector[Nmtoken] = Vector.empty,
    jobId: Option[Nmtoken] = None,
    jobPartId: Option[Nmtoken] = None,
    lastEntry: Option[Nmtoken] = None,
    maxEntries: Option[Int] = None,
    maxPriority: Option[QueuePriority] = None,
    minPriority: Option[QueuePriority] = None,
    newerThan: Option[XsdDateTime] = None,
    olderThan: Option[XsdDateTime] = None,
    queueEntryIds: Vector[Nmtoken] = Vector.empty,
    statuses: Vector[NodeStatus] = Vector.empty,
    gangSources: Vector[GangSource] = Vector.empty,
    parts: Vector[Part] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QueueStatusParams(
    updateGranularity: Option[UpdateGranularity] = None,
    filter: Option[QueueFilter] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QueueEntry(
    queueEntryId: Nmtoken,
    status: NodeStatus,
    activation: Option[QueueActivation] = None,
    endTime: Option[XsdDateTime] = None,
    gangName: Option[Nmtoken] = None,
    gangPolicy: Option[QueueGangPolicy] = None,
    jobId: Option[Nmtoken] = None,
    jobPartId: Option[Nmtoken] = None,
    priority: Option[QueuePriority] = None,
    relatedJobId: Option[Nmtoken] = None,
    relatedJobPartId: Option[Nmtoken] = None,
    startTime: Option[XsdDateTime] = None,
    statusDetails: Option[Nmtoken] = None,
    submissionTime: Option[XsdDateTime] = None,
    fileSpec: Option[FileSpec] = None,
    gangSources: Vector[GangSource] = Vector.empty,
    parts: Vector[Part] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Queue(
    maxQueueSize: Option[Int] = None,
    queueSize: Option[Int] = None,
    updateGranularity: Option[UpdateGranularity] = None,
    entries: Vector[QueueEntry] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QueryQueueStatus(
    header: Header,
    params: QueueStatusParams,
    subscription: Option[Subscription] = None,
    extensions: Extensions = Extensions.empty,
) extends Query:
  val elementName: QualifiedName = MessageNames.element("QueryQueueStatus")

final case class ResponseQueueStatus(
    header: Header,
    queue: Option[Queue] = None,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseQueueStatus")

final case class SignalQueueStatus(
    header: Header,
    queue: Queue,
    channelMode: Option[ChannelMode] = None,
    extensions: Extensions = Extensions.empty,
) extends Signal:
  val elementName: QualifiedName = MessageNames.element("SignalQueueStatus")
