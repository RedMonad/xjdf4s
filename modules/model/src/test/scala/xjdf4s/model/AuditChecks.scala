package xjdf4s.model

import xjdf4s.core.*

object AuditChecks:
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val time = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
  private val header = Header(deviceId, time)

  val auditChoice: Unit =
    val audit: Audit = AuditCreated(header)
    assert(audit.name == AuditName.AuditCreated)

  val boundedCardinality: Unit =
    assert(AtMostTwo.from(Vector(1, 2)).isRight)
    assert(AtMostTwo.from(Vector(1, 2, 3)).isLeft)
end AuditChecks
