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

/**
 * The normative element name is `StatusQuParams` (Table 7.65); the earlier construction-phase name
 * `StatusQueryParams` has been retired for round-trip fidelity.
 */
final case class StatusQuParams(
    jobId: Option[XjdfString] = None,
    jobPartId: Option[XjdfString] = None,
    queueEntryId: Option[Nmtoken] = None,
    types: Vector[Nmtoken] = Vector.empty,
    parts: Vector[Part] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QueryStatus(
    header: Header,
    params: Option[StatusQuParams] = None,
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

/**
 * The normative element name is `ResourceQuParams` (Table 7.49); the earlier construction-phase name
 * `ResourceQueryParams` has been retired for round-trip fidelity. `@Types` (New in XJDF 2.2) filters by the
 * `XJDF/@Types` of the processes whose resources are queried.
 */
final case class ResourceQuParams(
    scope: Scope,
    externalId: Option[Nmtoken] = None,
    jobId: Option[Nmtoken] = None,
    jobPartId: Option[Nmtoken] = None,
    queueEntryId: Option[Nmtoken] = None,
    details: Option[ResourceDetails] = None,
    resourceName: Option[Nmtoken] = None,
    types: Vector[Nmtoken] = Vector.empty,
    parts: Vector[Part] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class QueryResource(
    header: Header,
    params: ResourceQuParams,
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

/**
 * Table 7.54: `@ReplaceAfter` and `@ReplaceBefore` bound the replacement window of previous SignalResource data in
 * the same scope. If neither is specified, the signal is an original and SHALL NOT replace a previous signal.
 */
final case class SignalResource(
    header: Header,
    resourceInfo: Vector[ResourceInfo] = Vector.empty,
    replaceAfter: Option[XsdDateTime] = None,
    replaceBefore: Option[XsdDateTime] = None,
    channelMode: Option[ChannelMode] = None,
    extensions: Extensions = Extensions.empty,
) extends Signal,
      ValidatedNode:
  val elementName: QualifiedName = MessageNames.element("SignalResource")

  override def validate: Vector[ValidationError] =
    (replaceAfter, replaceBefore) match
      case (Some(after), Some(before)) if after.value > before.value =>
        Vector(
          ValidationError.ConflictingValues(
            Vector("SignalResource/@ReplaceAfter", "SignalResource/@ReplaceBefore"),
            "the replacement window start SHALL NOT be later than its end",
          ),
        )
      case _ => Vector.empty
end SignalResource
