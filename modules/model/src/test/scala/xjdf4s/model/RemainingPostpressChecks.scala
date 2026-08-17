package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object RemainingPostpressChecks:
  val bindingDetailsFollowType: Unit =
    val binding = ProductionLooseBinding.Coil(Some(CoilBindingProductionDetails(diameter = Some(12))))
    val resource: PostpressSpecificResource = LooseBindingParams(binding)
    assert(resource.elementName.localName == "LooseBindingParams")

  val boxFoldingOrderedActions: Unit =
    val fold = BoxFoldAction(BoxFoldActionType.Rotate90, XYPair(1, 0))
    val glueAction = BoxFoldAction(BoxFoldActionType.Glue, XYPair(0, 1), glue = Some(Glue()))
    val resource: TypedSpecificResource = BoxFoldingParams(
      BoxFoldingType.Type00,
      actions = Vector(fold, glueAction),
      legacyGlues = Vector(Glue()),
    )
    assert(resource.elementName.localName == "BoxFoldingParams")

  val exclusiveCollatingPlacement: Unit =
    val item = CollatingItem(placement = Some(CollatingPlacement.ByOrientation(Orientation.Rotate0)))
    val resource: PostpressSpecificResource = FeedingParams(collatingItems = Vector(item))
    assert(resource.elementName.localName == "FeedingParams")
end RemainingPostpressChecks
