package xjdf4s
package model

import xjdf4s.prim.*
import cats.Show
import cats.data.Chain
import cats.kernel.Eq

/**
 * The `Header` element (Table 7.3): information about the sender of an audit,
 * message or XJMF. `@DeviceID` and `@Time` are required; `@ID` is unique for
 * all messages and XJMF initiated by the sender.
 */
final case class Header(
  deviceId: NmToken,
  time: Timestamp,
  agentName: Option[XjdfString] = None,
  agentVersion: Option[XjdfString] = None,
  author: Option[XjdfString] = None,
  descriptiveName: Option[XjdfString] = None,
  icsVersions: Option[NmTokens] = None,
  id: Option[Id] = None,
  refId: Option[NmToken] = None,
  personalId: Option[NmToken] = None
)

object Header:

  def apply(deviceId: String, time: Timestamp): Option[Header] =
    NmToken.from(deviceId).map(Header(_, time))

  given Show[Header] =
    Show.show(h => s"Header(device=${h.deviceId.value}, time=${Show[Timestamp].show(h.time)})")

  given Eq[Header] = Eq.fromUniversalEquals

end Header

/**
 * `DeviceInfo` (Table 7.67): details of the actual Device status — the payload
 * of `AuditStatus`/`SignalStatus`.
 */
final case class DeviceInfo(
  status: DeviceStatus,
  statusDetails: Option[NmToken] = None,
  counterUnit: Option[NmToken] = None,
  productionCounter: Option[Double] = None,
  totalProductionCounter: Option[Double] = None,
  speed: Option[Double] = None,
  idleStartTime: Option[Timestamp] = None,
  endTime: Option[Timestamp] = None,
  powerOnTime: Option[Timestamp] = None,
  hourCounter: Option[TimeSpan] = None,
  moduleIds: Option[NmTokens] = None,
  toolIds: Option[NmTokens] = None
)

object DeviceInfo:

  given Show[DeviceInfo] =
    Show.show(d => s"DeviceInfo(status=${d.status.token.value})")

  given Eq[DeviceInfo] = Eq.fromUniversalEquals

end DeviceInfo

/**
 * The `Notification` element (Table 8.49): information about an individual
 * event that occurred during processing. Not more than one of `Event` and
 * `Milestone` SHALL be specified — modelled as `Option[Event | Milestone]`, a
 * union type with at most one inhabitant slot.
 */
final case class Notification(
  classification: SeverityClass,
  jobId: Option[JobId] = None,
  jobPartId: Option[JobPartId] = None,
  queueEntryId: Option[NmToken] = None,
  detail: Option[NotificationDetail] = None,
  parts: Chain[Part] = Chain.empty,
  comments: Chain[Comment] = Chain.empty
)

/** `Event | Milestone` — a union type: the two alternative payloads of a Notification. */
type NotificationDetail = Event | Milestone

object Notification:

  given Show[Notification] =
    Show.show(n => s"Notification(class=${n.classification.token.value})")

  given Eq[Notification] = Eq.fromUniversalEquals

end Notification

/**
 * `ProcessRun` (Table 3.7): the details of an individual Workstep execution —
 * the payload of `AuditProcessRun`.
 */
final case class ProcessRun(
  start: Timestamp,
  end: Timestamp,
  endStatus: EndStatus,
  duration: Option[TimeSpan] = None,
  queueEntryId: Option[NmToken] = None,
  returnTime: Option[Timestamp] = None,
  submissionTime: Option[Timestamp] = None,
  parts: Chain[Part] = Chain.empty
):

  /** The execution interval `@Start`..`@End`. */
  def interval: TimeRange = TimeRange(start, end)

object ProcessRun:

  given Show[ProcessRun] =
    Show.show(r => s"ProcessRun(${Show[TimeRange].show(r.interval)}, ${r.endStatus.token.value})")

  given Eq[ProcessRun] = Eq.fromUniversalEquals

end ProcessRun

/**
 * `ResourceInfo` (Table 7.53): the consumption or production of an individual
 * Resource — the payload of `AuditResource`/`SignalResource`.
 */
final case class ResourceInfo(
  resourceSet: ResourceSet,
  commandResult: Option[CommandResult] = None,
  jobId: Option[JobId] = None,
  jobPartId: Option[JobPartId] = None,
  level: Option[ResourceLevel] = None,
  moduleId: Option[NmToken] = None,
  queueEntryId: Option[NmToken] = None,
  scope: Option[Scope] = None,
  speed: Option[Double] = None,
  totalAmount: Option[Double] = None,
  types: Option[NmTokens] = None
)

object ResourceInfo:

  given Show[ResourceInfo] =
    Show.show(r => s"ResourceInfo(${r.resourceSet.name.value})")

  given Eq[ResourceInfo] = Eq.fromUniversalEquals

end ResourceInfo
