package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object GangAndSignatureChecks:
  private val gangId = Nmtoken.from("gang-1").toOption.get

  val requiredGangElements: Unit =
    val element = GangElement(gangId, orderQuantity = 100)
    val resource: PrepressSpecificResource = SheetOptimizingParams(NonEmptyVector.one(element))
    assert(resource.elementName.localName == "SheetOptimizingParams")

  val cellConditions: Unit =
    val condition = Condition(NonEmptyVector.one(Part()))
    val cell = SignatureCell(conditions = Vector(CellCondition(Side.Front, NonEmptyVector.one(condition))))
    val resource: FoundationalSpecificResource = BinderySignature(
      BinderySignatureType.Fold,
      signatureCells = Vector(cell),
    )
    assert(resource.elementName.localName == "BinderySignature")
end GangAndSignatureChecks
