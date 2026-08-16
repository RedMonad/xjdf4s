package xjdf4s.model

import xjdf4s.core.NonEmptyVector
import xjdf4s.model.resources.*

object PostpressResourceChecks:
  val requiredChildren: Unit =
    val perforation = Perforate()
    val resource: PostpressSpecificResource = PerforatingParams(NonEmptyVector.one(perforation))
    assert(resource.elementName.localName == "PerforatingParams")

  val requiredAttributes: Unit =
    val resource: TypedSpecificResource = WrappingParams(WrappingKind.ShrinkWrap)
    assert(resource.elementName.localName == "WrappingParams")
end PostpressResourceChecks
