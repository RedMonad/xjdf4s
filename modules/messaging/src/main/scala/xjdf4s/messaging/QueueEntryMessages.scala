package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.*

enum QueueMoveTarget:
  case After(queueEntryId: Nmtoken)
  case Before(queueEntryId: Nmtoken)
  case Position(value: Int)
  case Priority(value: QueuePriority)
end QueueMoveTarget

enum QueueModification:
  case Abort, Complete, Hold, Remove, Resume, Suspend
  case Move(target: Option[QueueMoveTarget] = None)
  case SetGang(gangName: Option[Nmtoken] = None)
end QueueModification

final case class ModifyQueueEntryParams(
    operation: QueueModification,
    filter: QueueFilter,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CommandModifyQueueEntry(
    header: Header,
    params: ModifyQueueEntryParams,
    extensions: Extensions = Extensions.empty,
) extends Command:
  val elementName: QualifiedName = MessageNames.element("CommandModifyQueueEntry")

final case class ResponseModifyQueueEntry(
    header: Header,
    queueEntries: Vector[QueueEntry] = Vector.empty,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseModifyQueueEntry")

final case class RequestQueueEntryParams(
    queueUrl: UriRef,
    activation: Option[QueueActivation] = None,
    jobId: Option[Nmtoken] = None,
    jobPartId: Option[Nmtoken] = None,
    parts: Vector[Part] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CommandRequestQueueEntry(
    header: Header,
    params: RequestQueueEntryParams,
    extensions: Extensions = Extensions.empty,
) extends Command:
  val elementName: QualifiedName = MessageNames.element("CommandRequestQueueEntry")

final case class ResponseRequestQueueEntry(
    header: Header,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseRequestQueueEntry")

enum ResubmissionUpdateMethod derives CanEqual:
  case Complete, Incremental, Remove
end ResubmissionUpdateMethod

final case class ResubmissionParams(
    queueEntryId: Nmtoken,
    updateMethod: ResubmissionUpdateMethod,
    url: UriRef,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CommandResubmitQueueEntry(
    header: Header,
    params: ResubmissionParams,
    extensions: Extensions = Extensions.empty,
) extends Command:
  val elementName: QualifiedName = MessageNames.element("CommandResubmitQueueEntry")

final case class ResponseResubmitQueueEntry(
    header: Header,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseResubmitQueueEntry")

final case class ReturnQueueEntryParams(
    queueEntryId: Nmtoken,
    url: UriRef,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CommandReturnQueueEntry(
    header: Header,
    params: ReturnQueueEntryParams,
    extensions: Extensions = Extensions.empty,
) extends Command:
  val elementName: QualifiedName = MessageNames.element("CommandReturnQueueEntry")

final case class ResponseReturnQueueEntry(
    header: Header,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseReturnQueueEntry")

enum QueueSubmissionPosition:
  case After(queueEntryId: Nmtoken)
  case Before(queueEntryId: Nmtoken)
  case Priority(value: QueuePriority)
end QueueSubmissionPosition

final case class QueueSubmissionParams(
    url: UriRef,
    activation: Option[QueueActivation] = None,
    gangName: Option[Nmtoken] = None,
    gangPolicy: Option[QueueGangPolicy] = None,
    position: Option[QueueSubmissionPosition] = None,
    returnJmf: Option[UriRef] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CommandSubmitQueueEntry(
    header: Header,
    params: QueueSubmissionParams,
    extensions: Extensions = Extensions.empty,
) extends Command:
  val elementName: QualifiedName = MessageNames.element("CommandSubmitQueueEntry")

final case class ResponseSubmitQueueEntry(
    header: Header,
    queueEntry: Option[QueueEntry] = None,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseSubmitQueueEntry")
