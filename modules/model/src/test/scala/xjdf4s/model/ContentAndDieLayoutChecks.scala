package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object ContentAndDieLayoutChecks:
  private val contentRef = XsdId.from("content-1").toOption.get
  private val stationName = Nmtoken.from("station-1").toOption.get

  val positionedContent: Unit =
    val anchor = RefAnchor(Anchor.Center, RefAnchorType.Parent, contentRef)
    val content: FoundationalSpecificResource = Content(
      positionedObjects = Vector(PositionedObject(refAnchor = Some(anchor))),
    )
    assert(content.elementName.localName == "Content")

  val dieStations: Unit =
    val layout: TypedSpecificResource = DieLayout(stations = Vector(DieStation(stationName)))
    assert(layout.elementName.localName == "DieLayout")
end ContentAndDieLayoutChecks
