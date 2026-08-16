package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.*

object ConcreteMessageChecks:
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val time = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
  private val header = Header(deviceId, time)

  val statusMessages: Unit =
    val info = DeviceInfo(DeviceStatus.Idle)
    val message: Message = SignalStatus(header, info)
    assert(message.elementName.localName == "SignalStatus")

  val resourceCommand: Unit =
    val resourceName = Nmtoken.from("Media").toOption.get
    val command: Command = CommandResource(
      header,
      ResourceCommandParams(ResourceUpdateMethod.Incremental, ResourceSet(resourceName)),
    )
    assert(command.elementName.localName == "CommandResource")

  val notificationSignal: Unit =
    val message: Signal = SignalNotification(header, Notification(Severity.Event))
    assert(message.elementName.localName == "SignalNotification")
end ConcreteMessageChecks
