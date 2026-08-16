package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object ProcessResourceChecks:
  private val blockName = Nmtoken.from("block-1").toOption.get
  private val markType = Nmtoken.from("RegisterMark").toOption.get
  private val separation = Nmtoken.from("Black").toOption.get

  val cuttingInstructions: Unit =
    val resource: PostpressSpecificResource = CuttingParams(cutBlocks = Vector(CutBlock(blockName)))
    assert(resource.elementName.localName == "CuttingParams")

  val requiredScreenSelectors: Unit =
    val resource: PrepressSpecificResource = ScreeningParams(NonEmptyVector.one(ScreenSelector()))
    assert(resource.elementName.localName == "ScreeningParams")

  val requiredMarkFields: Unit =
    val mark = MarkElement(XYPair(0, 0), markType, separation)
    val resource: TypedSpecificResource = RegisterMark(markElements = Vector(mark))
    assert(resource.elementName.localName == "RegisterMark")
end ProcessResourceChecks
