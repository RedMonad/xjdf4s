package xjdf4s

import xjdf4s.core.*
import xjdf4s.model.XJDF

object ProtocolChecks:
  private val jobId = Nmtoken.from("job-1").toOption.get
  private val process = Nmtoken.from("Product").toOption.get

  val xjdfCardinality: Unit =
    val document: ProtocolDocument = XJDF(jobId, NonEmptyVector.one(process))
    assert(Protocol.documentKind(document) == "XJDF")
end ProtocolChecks
