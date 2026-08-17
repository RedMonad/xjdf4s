package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object NormativeResourceChecks:
  val omittedHierarchyMember: Unit =
    val resource: StandardSpecificResource = RasterReadingParams(fitPolicy = Some(FitPolicy()))
    assert(resource.elementName.localName == "RasterReadingParams")

  val normativeReport: Unit =
    val report: StandardSpecificResource = SheetOptimizingReport(areaUse = 0.8f, volumeUse = 0.75f)
    assert(report.elementName.localName == "SheetOptimizingReport")

  val xjdf22JobPhaseAmount: Unit =
    val jobId = Nmtoken.from("job-1").toOption.get
    val phase = JobPhase(jobId, NodeStatus.InProgress, totalAmount = Some(100))
    assert(phase.totalAmount.contains(100))
end NormativeResourceChecks
