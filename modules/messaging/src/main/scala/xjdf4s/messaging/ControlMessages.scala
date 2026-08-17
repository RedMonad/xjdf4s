package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.*

enum PipeOperation derives CanEqual:
  case Close, Pause, Pull, Push
end PipeOperation

final case class PipeParams(
    jobId: Nmtoken,
    operation: PipeOperation,
    pipeId: Nmtoken,
    jobPartId: Option[Nmtoken] = None,
    misDetails: Option[MisDetails] = None,
    resourceSet: Option[ResourceSet] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CommandPipeControl(
    header: Header,
    params: PipeParams,
    extensions: Extensions = Extensions.empty,
) extends Command:
  val elementName: QualifiedName = MessageNames.element("CommandPipeControl")

final case class ResponsePipeControl(
    header: Header,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponsePipeControl")

enum ShutDownType derives CanEqual:
  case StandBy, Full
end ShutDownType

final case class ShutDownParams(
    shutDownType: Option[ShutDownType] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CommandShutDown(
    header: Header,
    params: Option[ShutDownParams] = None,
    extensions: Extensions = Extensions.empty,
) extends Command:
  val elementName: QualifiedName = MessageNames.element("CommandShutDown")

final case class ResponseShutDown(
    header: Header,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseShutDown")

final case class CommandWakeUp(
    header: Header,
    extensions: Extensions = Extensions.empty,
) extends Command:
  val elementName: QualifiedName = MessageNames.element("CommandWakeUp")

final case class ResponseWakeUp(
    header: Header,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseWakeUp")

final case class StopPersistentChannelParams(
    channelId: Option[Nmtoken] = None,
    messageType: Option[Nmtoken] = None,
    url: Option[UriRef] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

/**
 * Table 8.71. `@ChannelID` is the NMTOKEN-valued `Header/@ID` of the Query that initiated the persistent channel;
 * `@Languages` (New in XJDF 2.2) reports the language list selected by that Query.
 */
final case class SubscriptionInfo(
    channelId: Nmtoken,
    messageType: Nmtoken,
    subscription: Subscription,
    deviceId: Option[Nmtoken] = None,
    languages: Vector[LanguageTag] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CommandStopPersistentChannel(
    header: Header,
    params: StopPersistentChannelParams,
    extensions: Extensions = Extensions.empty,
) extends Command:
  val elementName: QualifiedName = MessageNames.element("CommandStopPersistentChannel")

final case class ResponseStopPersistentChannel(
    header: Header,
    subscriptions: Vector[SubscriptionInfo] = Vector.empty,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseStopPersistentChannel")
