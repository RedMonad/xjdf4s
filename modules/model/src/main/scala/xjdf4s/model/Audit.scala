package xjdf4s.model

import xjdf4s.core.*

enum AuditName derives CanEqual:
  case AuditCreated, AuditNotification, AuditProcessRun, AuditResource, AuditStatus

/** Closed choice of the five XJDF 2.2 audit variants from table 3.3. */
sealed trait Audit extends XjdfNode,
      HasHeader,
      Extensible:
  def name: AuditName

final case class AuditCreated(
    header: Header,
    extensions: Extensions = Extensions.empty,
) extends Audit:
  val name: AuditName = AuditName.AuditCreated

final case class AuditNotification(
    header: Header,
    notification: Notification,
    extensions: Extensions = Extensions.empty,
) extends Audit:
  val name: AuditName = AuditName.AuditNotification

final case class AuditProcessRun(
    header: Header,
    processRun: ProcessRun,
    extensions: Extensions = Extensions.empty,
) extends Audit:
  val name: AuditName = AuditName.AuditProcessRun

final case class AuditResource(
    header: Header,
    resourceInfo: ResourceInfo,
    extensions: Extensions = Extensions.empty,
) extends Audit:
  val name: AuditName = AuditName.AuditResource

final case class AuditStatus(
    header: Header,
    deviceInfo: DeviceInfo,
    extensions: Extensions = Extensions.empty,
) extends Audit:
  val name: AuditName = AuditName.AuditStatus

final case class AuditPool(
    audits: Vector[Audit] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible
