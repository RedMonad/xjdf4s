package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object QualityControlChecks:
  val boundedSeverity: Unit =
    assert(QualityScore.from(0).isRight)
    assert(QualityScore.from(100).isRight)
    assert(QualityScore.from(101).isLeft)

  val defectKindsAreNonEmpty: Unit =
    val defect = Defect(NonEmptyVector.one(DefectKind.ImageDefect))
    val result: GeneralSpecificResource = QualityControlResult(
      inspection = Some(Inspection(defects = Vector(defect))),
    )
    assert(result.elementName.localName == "QualityControlResult")

  val namedMasterFiles: Unit =
    val params: TypedSpecificResource = QualityControlParams(files = QualityControlFiles())
    assert(params.elementName.localName == "QualityControlParams")
end QualityControlChecks
