package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.Header

object QueueEntryMessageChecks:
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val entryId = Nmtoken.from("entry-1").toOption.get
  private val time = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
  private val url = UriRef.from("https://example.org/job.xjdf").toOption.get
  private val header = Header(deviceId, time)

  val exclusiveMoveTarget: Unit =
    val priority = QueuePriority.from(75).toOption.get
    val params = ModifyQueueEntryParams(
      QueueModification.Move(Some(QueueMoveTarget.Priority(priority))),
      QueueFilter(queueEntryIds = Vector(entryId)),
    )
    val message: StandardMessage = CommandModifyQueueEntry(header, params)
    assert(message.elementName.localName == "CommandModifyQueueEntry")

  val resubmission: Unit =
    val params = ResubmissionParams(entryId, ResubmissionUpdateMethod.Incremental, url)
    val message: StandardCommand = CommandResubmitQueueEntry(header, params)
    assert(message.elementName.localName == "CommandResubmitQueueEntry")

  val submissionPosition: Unit =
    val params = QueueSubmissionParams(url, position = Some(QueueSubmissionPosition.After(entryId)))
    val message: StandardCommand = CommandSubmitQueueEntry(header, params)
    assert(message.elementName.localName == "CommandSubmitQueueEntry")

  val priorityBounds: Unit =
    assert(QueuePriority.from(0).isRight)
    assert(QueuePriority.from(100).isRight)
    assert(QueuePriority.from(101).isLeft)
end QueueEntryMessageChecks
