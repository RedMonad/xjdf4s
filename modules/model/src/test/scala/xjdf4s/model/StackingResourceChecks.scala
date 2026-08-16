package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object StackingResourceChecks:
  private val separation = Nmtoken.from("Black").toOption.get

  val nestedStripMarks: Unit =
    val mark = StripMark(
      fillMarks = Vector(
        FillMark(
          KnockoutSource.TrimBox,
          NonEmptyVector.one(MarkColor(separation.value, 1.0f)),
        ),
      ),
    )
    val sheet = InsertSheet(InsertSheetType.SeparatorSheet, InsertSheetUsage.Interleaved, stripMarks = Vector(mark))
    val resource: PostpressSpecificResource = StackingParams(
      disjointing = Some(Disjointing(insertSheets = Vector(sheet))),
    )
    assert(resource.elementName.localName == "StackingParams")
end StackingResourceChecks
