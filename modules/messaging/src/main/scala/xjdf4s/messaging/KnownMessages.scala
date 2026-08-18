package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.*
import xjdf4s.model.resources.Device

enum DeviceDetails derives CanEqual:
  case Brief, Modules, Full

final case class DeviceFilter(
    details: Option[DeviceDetails] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QueryKnownDevices(
    header: Header,
    filter: Option[DeviceFilter] = None,
    languages: Vector[LanguageTag] = Vector.empty,
    subscription: Option[Subscription] = None,
    extensions: Extensions = Extensions.empty,
) extends Query:
  val elementName: QualifiedName = MessageNames.element("QueryKnownDevices")

final case class ResponseKnownDevices(
    header: Header,
    devices: Vector[Device] = Vector.empty,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseKnownDevices")

final case class SignalKnownDevices(
    header: Header,
    devices: Vector[Device] = Vector.empty,
    channelMode: Option[ChannelMode] = None,
    extensions: Extensions = Extensions.empty,
) extends Signal:
  val elementName: QualifiedName = MessageNames.element("SignalKnownDevices")

enum MessageResponseMode derives CanEqual:
  case FireAndForget, Reliable, Response

enum MessageUrlScheme(val lexical: String):
  case Http extends MessageUrlScheme("http")
  case Https extends MessageUrlScheme("https")

final case class MessageService(
    messageType: Nmtoken,
    responseModes: Vector[MessageResponseMode] = Vector.empty,
    urlSchemes: Vector[MessageUrlScheme] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QueryKnownMessages(
    header: Header,
    extensions: Extensions = Extensions.empty,
) extends Query:
  val subscription: Option[Subscription] = None
  val elementName: QualifiedName = MessageNames.element("QueryKnownMessages")

final case class ResponseKnownMessages(
    header: Header,
    services: Vector[MessageService] = Vector.empty,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseKnownMessages")

final case class SubscriptionFilter(
    deviceId: Option[Nmtoken] = None,
    url: Option[UriRef] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QueryKnownSubscriptions(
    header: Header,
    filter: Option[SubscriptionFilter] = None,
    subscription: Option[Subscription] = None,
    extensions: Extensions = Extensions.empty,
) extends Query:
  val elementName: QualifiedName = MessageNames.element("QueryKnownSubscriptions")

final case class ResponseKnownSubscriptions(
    header: Header,
    subscriptions: Vector[SubscriptionInfo] = Vector.empty,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseKnownSubscriptions")

final case class SignalKnownSubscriptions(
    header: Header,
    subscriptions: Vector[SubscriptionInfo] = Vector.empty,
    channelMode: Option[ChannelMode] = None,
    extensions: Extensions = Extensions.empty,
) extends Signal:
  val elementName: QualifiedName = MessageNames.element("SignalKnownSubscriptions")
