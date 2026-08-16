package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object MediaResourceChecks:
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val separation = Nmtoken.from("Black").toOption.get
  private val colorSpace = Nmtoken.from("DeviceCMYK").toOption.get

  val recursiveMediaLayers: Unit =
    val inner = Media(MediaType.Paper)
    val outer = Media(MediaType.Paper, mediaLayers = Some(MediaLayers(Glue(), inner)))
    val resource: FoundationalSpecificResource = outer
    assert(resource.elementName.localName == "Media")

  val deviceCardinality: Unit =
    val icon = DeviceIcon(bitDepth = 8, size = XYPair(32, 32))
    val device: TypedSpecificResource = Device(deviceId, iconList = Some(IconList(NonEmptyVector.one(icon))))
    assert(device.elementName.localName == "Device")

  val deviceNSeparations: Unit =
    val deviceN = DeviceNSpace(colorSpace, NonEmptyVector.one(separation))
    val resource: TypedSpecificResource = ColorantControl(deviceNSpace = Some(deviceN))
    assert(resource.elementName.localName == "ColorantControl")
end MediaResourceChecks
