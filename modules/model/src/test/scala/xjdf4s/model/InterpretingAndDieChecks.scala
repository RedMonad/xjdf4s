package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object InterpretingAndDieChecks:
  private val shapeId = XsdId.from("shape-1").toOption.get

  val interpretingPolicy: Unit =
    val policy = FitPolicy(sizePolicy = Some(FitSizePolicy.FitToPage))
    val resource: PrepressSpecificResource = InterpretingParams(fitPolicy = Some(policy))
    assert(resource.elementName.localName == "InterpretingParams")

  val productionCardinalities: Unit =
    val config = ConvertingConfig()
    val repeat = RepeatDescription(shapeId)
    val resource: TypedSpecificResource = DieLayoutProductionParams(
      NonEmptyVector.one(config),
      NonEmptyVector.one(repeat),
    )
    assert(resource.elementName.localName == "DieLayoutProductionParams")
end InterpretingAndDieChecks
