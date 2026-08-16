package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object ImagingResourceChecks:
  val imageCompressionCardinality: Unit =
    val compression = ImageCompression(imageType = Some(CompressedImageType.Color))
    val resource: PrepressSpecificResource = ImageCompressionParams(NonEmptyVector.one(compression))
    assert(resource.elementName.localName == "ImageCompressionParams")

  val colorConversionOperation: Unit =
    val operation = ColorSpaceConversionOperation(ColorConversionOperation.Convert)
    val resource: TypedSpecificResource = ColorSpaceConversionParams(operations = Vector(operation))
    assert(resource.elementName.localName == "ColorSpaceConversionParams")

  val typedTiffTagValue: Unit =
    val tag = TiffTag(tagNumber = 270, tagType = 2, value = Some(TiffTagValue.Text("description")))
    val resource: PrepressSpecificResource = RenderingParams(tiffFormatParams = Some(TiffFormatParams(tags = Vector(tag))))
    assert(resource.elementName.localName == "RenderingParams")
end ImagingResourceChecks
