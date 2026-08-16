package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object SmallProductionResourceChecks:
  private val deviceId = Nmtoken.from("press-1").toOption.get

  val nestedDevice: Unit =
    val resource: PrepressSpecificResource = InkZoneCalculationParams(device = Some(Device(deviceId)))
    assert(resource.elementName.localName == "InkZoneCalculationParams")

  val inlineFinishing: Unit =
    val folder = FolderProduction(productionType = Some(FolderProductionType.Collect))
    val resource: PostpressSpecificResource = WebInlineFinishingParams(folderProductions = Vector(folder))
    assert(resource.elementName.localName == "WebInlineFinishingParams")
end SmallProductionResourceChecks
