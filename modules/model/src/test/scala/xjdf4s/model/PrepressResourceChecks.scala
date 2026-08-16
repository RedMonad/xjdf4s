package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object PrepressResourceChecks:
  private val operationName = Nmtoken.from("Sharpen").toOption.get

  val requiredOperationCardinality: Unit =
    val operation = ImageEnhancementOperation(operationName)
    val resource: PrepressSpecificResource = ImageEnhancementParams(NonEmptyVector.one(operation))
    assert(resource.elementName.localName == "ImageEnhancementParams")

  val requiredShiftPoints: Unit =
    val point = ShiftPoint(Matrix.identity, XYPair(0, 0))
    val resource: TypedSpecificResource = LayoutShift(NonEmptyVector.one(point))
    assert(resource.elementName.localName == "LayoutShift")
end PrepressResourceChecks
