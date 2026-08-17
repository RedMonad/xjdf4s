package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object LayoutResourceChecks:
  val placedObjectChoice: Unit =
    val placed = PlacedObject(
      ctm = Matrix.identity,
      kind = PlacedObjectKind.Mark(MarkObject()),
    )
    val resource: StandardSpecificResource = Layout(placedObjects = Vector(placed))
    assert(resource.elementName.localName == "Layout")

  val conditionalSheet: Unit =
    val condition = Condition(NonEmptyVector.one(Part()))
    val resource: FoundationalSpecificResource = Layout(
      sheetActivation = Some(SheetActivation(NonEmptyVector.one(condition))),
    )
    assert(resource.elementName.localName == "Layout")
end LayoutResourceChecks
