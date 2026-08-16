package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.*

final case class NotificationFilter(
    classes: Vector[Severity] = Vector.empty,
    milestoneTypes: Vector[Nmtoken] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QueryNotification(
    header: Header,
    filter: Option[NotificationFilter] = None,
    languages: Vector[LanguageTag] = Vector.empty,
    subscription: Option[Subscription] = None,
    extensions: Extensions = Extensions.empty,
) extends Query:
  val elementName: QualifiedName = MessageNames.element("QueryNotification")

final case class ResponseNotification(
    header: Header,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseNotification")

final case class SignalNotification(
    header: Header,
    notification: Notification,
    channelMode: Option[ChannelMode] = None,
    extensions: Extensions = Extensions.empty,
) extends Signal:
  val elementName: QualifiedName = MessageNames.element("SignalNotification")

final case class StatusQueryParams(
    jobId: Option[String] = None,
    jobPartId: Option[String] = None,
    queueEntryId: Option[Nmtoken] = None,
    types: Vector[Nmtoken] = Vector.empty,
    parts: Vector[Part] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QueryStatus(
    header: Header,
    params: Option[StatusQueryParams] = None,
    languages: Vector[LanguageTag] = Vector.empty,
    subscription: Option[Subscription] = None,
    extensions: Extensions = Extensions.empty,
) extends Query:
  val elementName: QualifiedName = MessageNames.element("QueryStatus")

final case class ResponseStatus(
    header: Header,
    deviceInfo: Option[DeviceInfo] = None,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseStatus")

final case class StatusReplacementWindow(after: XsdDateTime, before: XsdDateTime) derives CanEqual

final case class SignalStatus(
    header: Header,
    deviceInfo: DeviceInfo,
    replacement: Option[StatusReplacementWindow] = None,
    channelMode: Option[ChannelMode] = None,
    extensions: Extensions = Extensions.empty,
) extends Signal:
  val elementName: QualifiedName = MessageNames.element("SignalStatus")

enum ResourceUpdateMethod derives CanEqual:
  case Complete, CompleteSet, Incremental, Remove, RemoveSet
end ResourceUpdateMethod

final case class ResourceCommandParams(
    updateMethod: ResourceUpdateMethod,
    resourceSet: ResourceSet,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CommandResource(
    header: Header,
    params: ResourceCommandParams,
    extensions: Extensions = Extensions.empty,
) extends Command:
  val elementName: QualifiedName = MessageNames.element("CommandResource")

enum ResourceDetails derives CanEqual:
  case Brief, Full
end ResourceDetails

final case class ResourceQueryParams(
    scope: Scope,
    externalId: Option[Nmtoken] = None,
    jobId: Option[Nmtoken] = None,
    jobPartId: Option[Nmtoken] = None,
    queueEntryId: Option[Nmtoken] = None,
    details: Option[ResourceDetails] = None,
    resourceName: Option[Nmtoken] = None,
    parts: Vector[Part] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QueryResource(
    header: Header,
    params: ResourceQueryParams,
    languages: Vector[LanguageTag] = Vector.empty,
    subscription: Option[Subscription] = None,
    extensions: Extensions = Extensions.empty,
) extends Query:
  val elementName: QualifiedName = MessageNames.element("QueryResource")

final case class ResponseResource(
    header: Header,
    resourceInfo: Vector[ResourceInfo] = Vector.empty,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  val elementName: QualifiedName = MessageNames.element("ResponseResource")

final case class SignalResource(
    header: Header,
    resourceInfo: Vector[ResourceInfo] = Vector.empty,
    channelMode: Option[ChannelMode] = None,
    extensions: Extensions = Extensions.empty,
) extends Signal:
  val elementName: QualifiedName = MessageNames.element("SignalResource")
