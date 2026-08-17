package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.Header
import xjdf4s.model.resources.Device

object KnownMessageChecks:
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val time = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
  private val header = Header(deviceId, time)

  val knownDevices: Unit =
    val message: Response = ResponseKnownDevices(header, devices = Vector(Device(deviceId)))
    assert(message.elementName.localName == "ResponseKnownDevices")

  val knownMessages: Unit =
    val messageType = Nmtoken.from("QueryStatus").toOption.get
    val service = MessageService(messageType, responseModes = Vector(MessageResponseMode.Reliable))
    val message: Response = ResponseKnownMessages(header, services = Vector(service))
    assert(message.elementName.localName == "ResponseKnownMessages")

  val knownSubscriptions: Unit =
    val message: Query = QueryKnownSubscriptions(header)
    assert(message.elementName.localName == "QueryKnownSubscriptions")
end KnownMessageChecks
