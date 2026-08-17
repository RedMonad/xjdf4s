package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.Header

object GangAndQueueChecks:
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val queueEntryId = Nmtoken.from("entry-1").toOption.get
  private val gangName = Nmtoken.from("gang-1").toOption.get
  private val time = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
  private val header = Header(deviceId, time)

  val forceGang: Unit =
    val filter = GangCommandFilter(Vector(gangName), Some(ForceGangPolicy.Optimized))
    val message: Command = CommandForceGang(header, filter)
    assert(message.elementName.localName == "CommandForceGang")

  val queueSignal: Unit =
    val entry = QueueEntry(queueEntryId, NodeStatus.Waiting)
    val message: Signal = SignalQueueStatus(header, Queue(entries = Vector(entry)))
    assert(message.elementName.localName == "SignalQueueStatus")
end GangAndQueueChecks
