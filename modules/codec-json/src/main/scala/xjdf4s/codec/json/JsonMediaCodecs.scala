package xjdf4s.codec.json

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.*

import xjdf4s.core.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * JSON codecs for the media/color resource surface. `MediaLayers` carries the normative JSON exception: the
 * layer array in-lines each layer with a `"Name": "Media" | "Glue"` member.
 */
object JsonMediaCodecs:

  given Encoder[Glue] = Encoder.instance(glue =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("AreaGlue", glue.areaGlue),
        JsonHelpers.optMember("GlueLineWidth", glue.glueLineWidth),
        JsonHelpers.optMember("GlueRef", glue.glueRef),
        JsonHelpers.optMember("GlueType", glue.glueType),
        JsonHelpers.optMember("GluingPattern", glue.gluingPattern.map(_.toVector)),
        JsonHelpers.optMember("GluingTechnique", glue.gluingTechnique),
        JsonHelpers.optMember("MeltingTemperature", glue.meltingTemperature),
        JsonHelpers.optMember("StartPosition", glue.startPosition),
        JsonHelpers.optMember("WorkingDirection", glue.workingDirection),
        JsonHelpers.optMember("WorkingPath", glue.workingPath),
      ),
    ),
  )
  given Decoder[Glue] = Decoder.instance(cursor =>
    for
      areaGlue <- JsonHelpers.opt[Boolean](cursor, "AreaGlue")
      glueLineWidth <- JsonHelpers.opt[Float](cursor, "GlueLineWidth")
      glueRef <- JsonHelpers.opt[XsdIdRef](cursor, "GlueRef")
      glueType <- JsonHelpers.opt[GlueType](cursor, "GlueType")
      gluingPattern <- JsonHelpers.opt[Vector[Float]](cursor, "GluingPattern")
      gluingTechnique <- JsonHelpers.opt[GluingTechnique](cursor, "GluingTechnique")
      meltingTemperature <- JsonHelpers.opt[Int](cursor, "MeltingTemperature")
      startPosition <- JsonHelpers.opt[XYPair](cursor, "StartPosition")
      workingDirection <- JsonHelpers.opt[Face](cursor, "WorkingDirection")
      workingPath <- JsonHelpers.opt[XYPair](cursor, "WorkingPath")
      pattern <- gluingPattern match
        case Some(values) => GluingPattern.from(values).left.map(error => io.circe.DecodingFailure(error.toString, cursor.history)).map(Some(_))
        case None         => Right(None)
    yield Glue(
      areaGlue,
      glueLineWidth,
      glueRef,
      glueType,
      pattern,
      gluingTechnique,
      meltingTemperature,
      startPosition,
      workingDirection,
      workingPath,
    ),
  )

  given Encoder[ColorMeasurementConditions] = Encoder.instance(conditions =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("Aperture", conditions.aperture),
        JsonHelpers.optMember("DensityStandard", conditions.densityStandard),
        JsonHelpers.optMember("Illumination", conditions.illumination),
        JsonHelpers.optMember("IlluminationAngle", conditions.illuminationAngle),
        JsonHelpers.optMember("InkState", conditions.inkState),
        JsonHelpers.optMember("MeasurementAngle", conditions.measurementAngle),
        JsonHelpers.optMember("MeasurementFilter", conditions.measurementFilter),
        JsonHelpers.optMember("MeasurementMode", conditions.measurementMode),
        JsonHelpers.optMember("Observer", conditions.observer),
        JsonHelpers.optMember("SampleBacking", conditions.sampleBacking),
        JsonHelpers.optMember("SpectralResolution", conditions.spectralResolution),
        JsonHelpers.optMember("WhiteBase", conditions.whiteBase),
      ),
    ),
  )
  given Decoder[ColorMeasurementConditions] = Decoder.instance(cursor =>
    for
      aperture <- JsonHelpers.opt[Float](cursor, "Aperture")
      densityStandard <- JsonHelpers.opt[Nmtoken](cursor, "DensityStandard")
      illumination <- JsonHelpers.opt[Nmtoken](cursor, "Illumination")
      illuminationAngle <- JsonHelpers.opt[Int](cursor, "IlluminationAngle")
      inkState <- JsonHelpers.opt[InkState](cursor, "InkState")
      measurementAngle <- JsonHelpers.opt[Int](cursor, "MeasurementAngle")
      measurementFilter <- JsonHelpers.opt[MeasurementFilter](cursor, "MeasurementFilter")
      measurementMode <- JsonHelpers.opt[Nmtoken](cursor, "MeasurementMode")
      observer <- JsonHelpers.opt[Int](cursor, "Observer")
      sampleBacking <- JsonHelpers.opt[SampleBacking](cursor, "SampleBacking")
      spectralResolution <- JsonHelpers.opt[Float](cursor, "SpectralResolution")
      whiteBase <- JsonHelpers.opt[WhiteBase](cursor, "WhiteBase")
    yield ColorMeasurementConditions(
      aperture,
      densityStandard,
      illumination,
      illuminationAngle,
      inkState,
      measurementAngle,
      measurementFilter,
      measurementMode,
      observer,
      sampleBacking,
      spectralResolution,
      whiteBase,
    ),
  )

  given Encoder[Media] = Encoder.instance(media =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("BackBrightness", media.backBrightness),
        JsonHelpers.optMember("BackCIETint", media.backCieTint),
        JsonHelpers.optMember("BackCIEWhiteness", media.backCieWhiteness),
        JsonHelpers.optMember("BackCoating", media.backCoating),
        JsonHelpers.optMember("BackCoatingDetail", media.backCoatingDetail),
        JsonHelpers.optMember("BackGlossValue", media.backGlossValue),
        JsonHelpers.optMember("BackISOPaperSubstrate", media.backIsoPaperSubstrate),
        JsonHelpers.optMember("BackLabColorValue", media.backLabColorValue),
        JsonHelpers.optMember("BackSpectrum", media.backSpectrum),
        JsonHelpers.optMember("Brightness", media.brightness),
        JsonHelpers.optMember("CIETint", media.cieTint),
        JsonHelpers.optMember("CIEWhiteness", media.cieWhiteness),
        JsonHelpers.optMember("Coating", media.coating),
        JsonHelpers.optMember("CoatingDetail", media.coatingDetail),
        JsonHelpers.optMember("CoreWeight", media.coreWeight),
        JsonHelpers.optMember("Dimension", media.dimension),
        JsonHelpers.optMember("Flute", media.flute),
        JsonHelpers.optMember("FluteDirection", media.fluteDirection),
        JsonHelpers.optMember("GlossValue", media.glossValue),
        JsonHelpers.optMember("GrainDirection", media.grainDirection),
        JsonHelpers.optMember("ImagableSide", media.imagableSide),
        JsonHelpers.optMember("InnerCoreDiameter", media.innerCoreDiameter),
        JsonHelpers.optMember("InsideLoss", media.insideLoss),
        JsonHelpers.optMember("ISOPaperSubstrate", media.isoPaperSubstrate),
        JsonHelpers.optMember("LabColorValue", media.labColorValue),
        JsonHelpers.optMember("MediaColorName", media.mediaColorName),
        JsonHelpers.optMember("MediaColorNameDetails", media.mediaColorNameDetails),
        JsonHelpers.optMember("MediaQuality", media.mediaQuality),
        JsonHelpers.optMember("MediaSetCount", media.mediaSetCount),
        Vector(JsonHelpers.member("MediaType", Json.fromString(media.mediaType.toString))),
        JsonHelpers.optMember("MediaTypeDetails", media.mediaTypeDetails),
        JsonHelpers.optMember("MediaUnit", media.mediaUnit),
        JsonHelpers.optMember("Opacity", media.opacity),
        JsonHelpers.optMember("OpacityLevel", media.opacityLevel),
        JsonHelpers.optMember("OuterCoreDiameter", media.outerCoreDiameter),
        JsonHelpers.optMember("OutsideGain", media.outsideGain),
        JsonHelpers.optMember("PlateTechnology", media.plateTechnology),
        JsonHelpers.optMember("Polarity", media.polarity),
        JsonHelpers.optMember("PrintingTechnology", media.printingTechnology),
        JsonHelpers.optMember("RecycledPercentage", media.recycledPercentage),
        JsonHelpers.optMember("ReliefThickness", media.reliefThickness),
        JsonHelpers.optMember("RollDiameter", media.rollDiameter),
        JsonHelpers.optMember("ShrinkIndex", media.shrinkIndex),
        JsonHelpers.optMember("SleeveInterlock", media.sleeveInterlock),
        JsonHelpers.optMember("Spectrum", media.spectrum),
        JsonHelpers.optMember("StockType", media.stockType),
        JsonHelpers.optMember("Texture", media.texture),
        JsonHelpers.optMember("Thickness", media.thickness),
        JsonHelpers.optMember("Weight", media.weight),
        JsonHelpers.optMember("ColorMeasurementConditions", media.colorMeasurementConditions),
        JsonHelpers.optMember("MediaLayers", media.mediaLayers),
      ),
    ),
  )
  given Decoder[Media] = Decoder.instance(cursor =>
    for
      mediaType <- cursor.get[MediaType]("MediaType")
      backBrightness <- JsonHelpers.opt[Float](cursor, "BackBrightness")
      backCieTint <- JsonHelpers.opt[Float](cursor, "BackCIETint")
      backCieWhiteness <- JsonHelpers.opt[Float](cursor, "BackCIEWhiteness")
      backCoating <- JsonHelpers.opt[Coating](cursor, "BackCoating")
      backCoatingDetail <- JsonHelpers.opt[Nmtoken](cursor, "BackCoatingDetail")
      backGlossValue <- JsonHelpers.opt[Float](cursor, "BackGlossValue")
      backIsoPaperSubstrate <- JsonHelpers.opt[IsoPaperSubstrate](cursor, "BackISOPaperSubstrate")
      backLabColorValue <- JsonHelpers.opt[LabColor](cursor, "BackLabColorValue")
      backSpectrum <- JsonHelpers.opt[TransferFunction](cursor, "BackSpectrum")
      brightness <- JsonHelpers.opt[Float](cursor, "Brightness")
      cieTint <- JsonHelpers.opt[Float](cursor, "CIETint")
      cieWhiteness <- JsonHelpers.opt[Float](cursor, "CIEWhiteness")
      coating <- JsonHelpers.opt[Coating](cursor, "Coating")
      coatingDetail <- JsonHelpers.opt[Nmtoken](cursor, "CoatingDetail")
      coreWeight <- JsonHelpers.opt[Float](cursor, "CoreWeight")
      dimension <- JsonHelpers.opt[XYPair](cursor, "Dimension")
      flute <- JsonHelpers.opt[Nmtoken](cursor, "Flute")
      fluteDirection <- JsonHelpers.opt[MediaDirection](cursor, "FluteDirection")
      glossValue <- JsonHelpers.opt[Float](cursor, "GlossValue")
      grainDirection <- JsonHelpers.opt[MediaDirection](cursor, "GrainDirection")
      imagableSide <- JsonHelpers.opt[ImagableSide](cursor, "ImagableSide")
      innerCoreDiameter <- JsonHelpers.opt[Float](cursor, "InnerCoreDiameter")
      insideLoss <- JsonHelpers.opt[Float](cursor, "InsideLoss")
      isoPaperSubstrate <- JsonHelpers.opt[IsoPaperSubstrate](cursor, "ISOPaperSubstrate")
      labColorValue <- JsonHelpers.opt[LabColor](cursor, "LabColorValue")
      mediaColorName <- JsonHelpers.opt[NamedColor](cursor, "MediaColorName")
      mediaColorNameDetails <- JsonHelpers.opt[XjdfString](cursor, "MediaColorNameDetails")
      mediaQuality <- JsonHelpers.opt[XjdfString](cursor, "MediaQuality")
      mediaSetCount <- JsonHelpers.opt[Int](cursor, "MediaSetCount")
      mediaTypeDetails <- JsonHelpers.opt[Nmtoken](cursor, "MediaTypeDetails")
      mediaUnit <- JsonHelpers.opt[MediaUnit](cursor, "MediaUnit")
      opacity <- JsonHelpers.opt[Opacity](cursor, "Opacity")
      opacityLevel <- JsonHelpers.opt[Float](cursor, "OpacityLevel")
      outerCoreDiameter <- JsonHelpers.opt[Float](cursor, "OuterCoreDiameter")
      outsideGain <- JsonHelpers.opt[Float](cursor, "OutsideGain")
      plateTechnology <- JsonHelpers.opt[PlateTechnology](cursor, "PlateTechnology")
      polarity <- JsonHelpers.opt[Polarity](cursor, "Polarity")
      printingTechnology <- JsonHelpers.opt[Nmtoken](cursor, "PrintingTechnology")
      recycledPercentage <- JsonHelpers.opt[Float](cursor, "RecycledPercentage")
      reliefThickness <- JsonHelpers.opt[Float](cursor, "ReliefThickness")
      rollDiameter <- JsonHelpers.opt[Float](cursor, "RollDiameter")
      shrinkIndex <- JsonHelpers.opt[XYPair](cursor, "ShrinkIndex")
      sleeveInterlock <- JsonHelpers.opt[Nmtoken](cursor, "SleeveInterlock")
      spectrum <- JsonHelpers.opt[TransferFunction](cursor, "Spectrum")
      stockType <- JsonHelpers.opt[Nmtoken](cursor, "StockType")
      texture <- JsonHelpers.opt[Nmtoken](cursor, "Texture")
      thickness <- JsonHelpers.opt[Float](cursor, "Thickness")
      weight <- JsonHelpers.opt[Float](cursor, "Weight")
      conditions <- JsonHelpers.opt[ColorMeasurementConditions](cursor, "ColorMeasurementConditions")
      mediaLayers <- JsonHelpers.opt[MediaLayers](cursor, "MediaLayers")
    yield Media(
      mediaType,
      backBrightness,
      backCieTint,
      backCieWhiteness,
      backCoating,
      backCoatingDetail,
      backGlossValue,
      backIsoPaperSubstrate,
      backLabColorValue,
      backSpectrum,
      brightness,
      cieTint,
      cieWhiteness,
      coating,
      coatingDetail,
      coreWeight,
      dimension,
      flute,
      fluteDirection,
      glossValue,
      grainDirection,
      imagableSide,
      innerCoreDiameter,
      insideLoss,
      isoPaperSubstrate,
      labColorValue,
      mediaColorName,
      mediaColorNameDetails,
      mediaQuality,
      mediaSetCount,
      mediaTypeDetails,
      mediaUnit,
      opacity,
      opacityLevel,
      outerCoreDiameter,
      outsideGain,
      plateTechnology,
      polarity,
      printingTechnology,
      recycledPercentage,
      reliefThickness,
      rollDiameter,
      shrinkIndex,
      sleeveInterlock,
      spectrum,
      stockType,
      texture,
      thickness,
      weight,
      Vector.empty,
      conditions,
      Vector.empty,
      Vector.empty,
      mediaLayers,
      None,
    ),
  )

  given Encoder[MediaLayers] = Encoder.instance(mediaLayers =>
    Json.arr(
      mediaLayers.layers.map {
        case MediaLayer.GlueLayer(glue)     => glue.asJson.mapObject(_.add("Name", Json.fromString("Glue")))
        case MediaLayer.MediaLayer(media)   => media.asJson.mapObject(_.add("Name", Json.fromString("Media")))
      }*,
    ),
  )
  given Decoder[MediaLayers] = Decoder.instance(cursor =>
    for
      items <- cursor.as[List[Json]]
      layers <- items.foldLeft[Decoder.Result[Vector[MediaLayer]]](Right(Vector.empty)) { (acc, item) =>
        for
          accumulated <- acc
          kind <- item.hcursor.get[String]("Name")
          layer <- kind match
            case "Glue"  => item.as[Glue].map(MediaLayer.GlueLayer(_))
            case "Media" => item.as[Media].map(MediaLayer.MediaLayer(_))
            case other   => JsonHelpers.fail(item.hcursor, s"unknown MediaLayers Name '$other'")
        yield accumulated :+ layer
      }
    yield MediaLayers(layers),
  )

  given Encoder[Color] = Encoder.instance(color =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("ActualColorName", color.actualColorName),
        JsonHelpers.optMember("CMYK", color.cmyk),
        JsonHelpers.optMember("ColorBook", color.colorBook),
        JsonHelpers.optMember("ColorBookEntry", color.colorBookEntry),
        JsonHelpers.optMember("ColorDetails", color.colorDetails),
        JsonHelpers.optMember("ColorName", color.colorName),
        JsonHelpers.optMember("ColorType", color.colorType),
        JsonHelpers.optMember("ColorTypeDetails", color.colorTypeDetails),
        JsonHelpers.optMember("Density", color.density),
        JsonHelpers.optMember("Gray", color.gray),
        JsonHelpers.optMember("Lab", color.lab),
        JsonHelpers.optMember("NeutralDensity", color.neutralDensity),
        JsonHelpers.optMember("PrintingTechnology", color.printingTechnology),
        JsonHelpers.optMember("PrintStandard", color.printStandard),
        JsonHelpers.optMember("Spectrum", color.spectrum),
        JsonHelpers.optMember("sRGB", color.srgb),
        JsonHelpers.optMember("ColorMeasurementConditions", color.colorMeasurementConditions),
      ),
    ),
  )
  given Decoder[Color] = Decoder.instance(cursor =>
    for
      actualColorName <- JsonHelpers.opt[XjdfString](cursor, "ActualColorName")
      cmyk <- JsonHelpers.opt[Vector[Float]](cursor, "CMYK")
      colorBook <- JsonHelpers.opt[XjdfString](cursor, "ColorBook")
      colorBookEntry <- JsonHelpers.opt[XjdfString](cursor, "ColorBookEntry")
      colorDetails <- JsonHelpers.opt[XjdfString](cursor, "ColorDetails")
      colorName <- JsonHelpers.opt[NamedColor](cursor, "ColorName")
      colorType <- JsonHelpers.opt[ColorType](cursor, "ColorType")
      colorTypeDetails <- JsonHelpers.opt[XjdfString](cursor, "ColorTypeDetails")
      density <- JsonHelpers.opt[Float](cursor, "Density")
      gray <- JsonHelpers.opt[Float](cursor, "Gray")
      lab <- JsonHelpers.opt[LabColor](cursor, "Lab")
      neutralDensity <- JsonHelpers.opt[Float](cursor, "NeutralDensity")
      printingTechnology <- JsonHelpers.opt[Nmtoken](cursor, "PrintingTechnology")
      printStandard <- JsonHelpers.opt[XjdfString](cursor, "PrintStandard")
      spectrum <- JsonHelpers.opt[TransferFunction](cursor, "Spectrum")
      srgb <- JsonHelpers.opt[Vector[Float]](cursor, "sRGB")
      conditions <- JsonHelpers.opt[ColorMeasurementConditions](cursor, "ColorMeasurementConditions")
      cmykValue <- cmyk match
        case Some(Vector(c, m, y, k)) => CmykColor.from(c, m, y, k).left.map(error => io.circe.DecodingFailure(error.toString, cursor.history)).map(Some(_))
        case Some(_)                  => JsonHelpers.fail(cursor, "CMYK requires exactly four numbers")
        case None                     => Right(None)
      srgbValue <- srgb match
        case Some(Vector(r, g, b)) => SrgbColor.from(r, g, b).left.map(error => io.circe.DecodingFailure(error.toString, cursor.history)).map(Some(_))
        case Some(_)               => JsonHelpers.fail(cursor, "sRGB requires exactly three numbers")
        case None                  => Right(None)
      neutralDensityValue <- neutralDensity match
        case Some(value) => NeutralDensity.from(value).left.map(error => io.circe.DecodingFailure(error.toString, cursor.history)).map(Some(_))
        case None        => Right(None)
    yield Color(
      actualColorName,
      cmykValue,
      colorBook,
      colorBookEntry,
      colorDetails,
      colorName,
      colorType,
      colorTypeDetails,
      density,
      gray,
      lab,
      neutralDensityValue,
      printingTechnology,
      printStandard,
      Vector.empty,
      spectrum,
      srgbValue,
      conditions,
      Vector.empty,
    ),
  )

  given Encoder[Component] = Encoder.instance(component =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("Automation", component.automation),
        JsonHelpers.optMember("CartonTopFlaps", component.cartonTopFlaps),
        JsonHelpers.optMember("Columns", component.columns),
        JsonHelpers.vecMember("ContentRefs", component.contentRefs),
        JsonHelpers.optMember("Dimensions", component.dimensions),
        JsonHelpers.optMember("MaxHeat", component.maxHeat),
        JsonHelpers.optMember("MediaRef", component.mediaRef),
        JsonHelpers.optMember("Overfold", component.overfold),
        JsonHelpers.optMember("OverfoldSide", component.overfoldSide),
        JsonHelpers.optMember("ProductType", component.productType),
        JsonHelpers.optMember("ProductTypeDetails", component.productTypeDetails),
        JsonHelpers.optMember("ReaderPageCount", component.readerPageCount),
        JsonHelpers.optMember("SurfaceCount", component.surfaceCount),
        JsonHelpers.optMember("WindingResult", component.windingResult),
      ),
    ),
  )
  given Decoder[Component] = Decoder.instance(cursor =>
    for
      automation <- JsonHelpers.opt[Automation](cursor, "Automation")
      cartonTopFlaps <- JsonHelpers.opt[XYPair](cursor, "CartonTopFlaps")
      columns <- JsonHelpers.opt[Int](cursor, "Columns")
      contentRefs <- JsonHelpers.vec[XsdIdRef](cursor, "ContentRefs")
      dimensions <- JsonHelpers.opt[Shape3D](cursor, "Dimensions")
      maxHeat <- JsonHelpers.opt[Float](cursor, "MaxHeat")
      mediaRef <- JsonHelpers.opt[XsdIdRef](cursor, "MediaRef")
      overfold <- JsonHelpers.opt[Float](cursor, "Overfold")
      overfoldSide <- JsonHelpers.opt[Side](cursor, "OverfoldSide")
      productType <- JsonHelpers.opt[Nmtoken](cursor, "ProductType")
      productTypeDetails <- JsonHelpers.opt[XjdfString](cursor, "ProductTypeDetails")
      readerPageCount <- JsonHelpers.opt[Int](cursor, "ReaderPageCount")
      surfaceCount <- JsonHelpers.opt[Int](cursor, "SurfaceCount")
      windingResult <- JsonHelpers.opt[Int](cursor, "WindingResult")
    yield Component(
      automation,
      cartonTopFlaps,
      columns,
      contentRefs,
      dimensions,
      maxHeat,
      mediaRef,
      overfold,
      overfoldSide,
      productType,
      productTypeDetails,
      readerPageCount,
      surfaceCount,
      windingResult,
      Vector.empty,
    ),
  )

  given Encoder[Tool] = Encoder.instance(tool =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("ToolType", tool.toolType),
        JsonHelpers.optMember("Manufacturer", tool.manufacturer),
        JsonHelpers.optMember("ManufacturerURL", tool.manufacturerUrl),
        JsonHelpers.optMember("SerialNumber", tool.serialNumber),
      ),
    ),
  )
  given Decoder[Tool] = Decoder.instance(cursor =>
    for
      toolType <- JsonHelpers.opt[Nmtoken](cursor, "ToolType")
      manufacturer <- JsonHelpers.opt[XjdfString](cursor, "Manufacturer")
      manufacturerUrl <- JsonHelpers.opt[UriRef](cursor, "ManufacturerURL")
      serialNumber <- JsonHelpers.opt[XjdfString](cursor, "SerialNumber")
    yield Tool(toolType, manufacturer, manufacturerUrl, serialNumber, Vector.empty),
  )

  given Encoder[RunList] = Encoder.instance(runList =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("Automation", runList.automation),
        JsonHelpers.optMember("ClipPath", runList.clipPath),
        JsonHelpers.vecMember("ContentRefs", runList.contentRefs),
        JsonHelpers.optMember("Docs", runList.docs),
        JsonHelpers.vecMember("DocPages", runList.docPages),
        JsonHelpers.optMember("EndOfDocument", runList.endOfDocument),
        JsonHelpers.optMember("EndOfSet", runList.endOfSet),
        JsonHelpers.optMember("FinishedPages", runList.finishedPages),
        JsonHelpers.optMember("LogicalPage", runList.logicalPage),
        JsonHelpers.optMember("NumberOfPages", runList.numberOfPages),
        JsonHelpers.optMember("OrderType", runList.orderType),
        JsonHelpers.optMember("Pages", runList.pages),
        JsonHelpers.optMember("Sets", runList.sets),
        JsonHelpers.optMember("SourceBleedBox", runList.sourceBleedBox),
        JsonHelpers.optMember("SourceClipBox", runList.sourceClipBox),
        JsonHelpers.optMember("SourceMediaBox", runList.sourceMediaBox),
        JsonHelpers.optMember("SourceTrimBox", runList.sourceTrimBox),
      ),
    ),
  )
  given Decoder[RunList] = Decoder.instance(cursor =>
    for
      automation <- JsonHelpers.opt[Automation](cursor, "Automation")
      clipPath <- JsonHelpers.opt[PdfPath](cursor, "ClipPath")
      contentRefs <- JsonHelpers.vec[XsdIdRef](cursor, "ContentRefs")
      docs <- JsonHelpers.opt[IntegerRange](cursor, "Docs")
      docPages <- JsonHelpers.vec[Int](cursor, "DocPages")
      endOfDocument <- JsonHelpers.opt[Boolean](cursor, "EndOfDocument")
      endOfSet <- JsonHelpers.opt[Boolean](cursor, "EndOfSet")
      finishedPages <- JsonHelpers.opt[Int](cursor, "FinishedPages")
      logicalPage <- JsonHelpers.opt[Int](cursor, "LogicalPage")
      numberOfPages <- JsonHelpers.opt[Int](cursor, "NumberOfPages")
      orderType <- JsonHelpers.opt[RunListOrderType](cursor, "OrderType")
      pages <- JsonHelpers.opt[IntegerRange](cursor, "Pages")
      sets <- JsonHelpers.opt[IntegerRange](cursor, "Sets")
      sourceBleedBox <- JsonHelpers.opt[Rectangle](cursor, "SourceBleedBox")
      sourceClipBox <- JsonHelpers.opt[Rectangle](cursor, "SourceClipBox")
      sourceMediaBox <- JsonHelpers.opt[Rectangle](cursor, "SourceMediaBox")
      sourceTrimBox <- JsonHelpers.opt[Rectangle](cursor, "SourceTrimBox")
    yield RunList(
      automation,
      clipPath,
      contentRefs,
      docs,
      docPages,
      endOfDocument,
      endOfSet,
      finishedPages,
      logicalPage,
      numberOfPages,
      orderType,
      pages,
      sets,
      sourceBleedBox,
      sourceClipBox,
      sourceMediaBox,
      sourceTrimBox,
      None,
      None,
      Vector.empty,
    ),
  )

  given Encoder[RegisterMark] = Encoder.instance(mark =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("Center", mark.center),
        JsonHelpers.optMember("MarkName", mark.markName),
        JsonHelpers.vecMember("MarkTypes", mark.markTypes),
        JsonHelpers.vecMember("MarkUsage", mark.markUsage),
        JsonHelpers.optMember("Rotation", mark.rotation),
        JsonHelpers.vecMember("Separations", mark.separations),
        JsonHelpers.optMember("Size", mark.size),
      ),
    ),
  )
  given Decoder[RegisterMark] = Decoder.instance(cursor =>
    for
      center <- JsonHelpers.opt[XYPair](cursor, "Center")
      markName <- JsonHelpers.opt[Nmtoken](cursor, "MarkName")
      markTypes <- JsonHelpers.vec[Nmtoken](cursor, "MarkTypes")
      markUsage <- JsonHelpers.vec[MarkUsage](cursor, "MarkUsage")
      rotation <- JsonHelpers.opt[Float](cursor, "Rotation")
      separations <- JsonHelpers.vec[Nmtoken](cursor, "Separations")
      size <- JsonHelpers.opt[XYPair](cursor, "Size")
    yield RegisterMark(center, markName, markTypes, markUsage, rotation, separations, size, Vector.empty),
  )

  given Encoder[Device] = Encoder.instance(device =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("CostCenterID", device.costCenterId),
        JsonHelpers.optMember("DescriptiveName", device.descriptiveName),
        JsonHelpers.vecMember("DeviceClass", device.deviceClasses),
        Vector(JsonHelpers.member("DeviceID", Json.fromString(device.deviceId.value))),
        JsonHelpers.optMember("DeviceType", device.deviceType),
        JsonHelpers.vecMember("ICSVersions", device.icsVersions),
        JsonHelpers.vecMember("JDFVersions", device.jdfVersions),
        JsonHelpers.vecMember("KnownLocalizations", device.knownLocalizations),
        JsonHelpers.optMember("Manufacturer", device.manufacturer),
        JsonHelpers.optMember("ManufacturerURL", device.manufacturerUrl),
        JsonHelpers.optMember("MaxRunSpeed", device.maxRunSpeed),
        JsonHelpers.vecMember("Packaging", device.packaging),
        JsonHelpers.optMember("PresentationURL", device.presentationUrl),
        JsonHelpers.optMember("RestApiBaseURL", device.restApiBaseUrl),
        JsonHelpers.optMember("Revision", device.revision),
        JsonHelpers.optMember("SerialNumber", device.serialNumber),
        JsonHelpers.vecMember("URLSchemes", device.urlSchemes),
        JsonHelpers.optMember("XJMFURL", device.xjmfUrl),
      ),
    ),
  )
  given Decoder[Device] = Decoder.instance(cursor =>
    for
      deviceId <- cursor.get[Nmtoken]("DeviceID")
      costCenterId <- JsonHelpers.opt[Nmtoken](cursor, "CostCenterID")
      descriptiveName <- JsonHelpers.opt[XjdfString](cursor, "DescriptiveName")
      deviceClasses <- JsonHelpers.vec[Nmtoken](cursor, "DeviceClass")
      deviceType <- JsonHelpers.opt[XjdfString](cursor, "DeviceType")
      icsVersions <- JsonHelpers.vec[Nmtoken](cursor, "ICSVersions")
      jdfVersions <- JsonHelpers.vec[JdfVersion](cursor, "JDFVersions")
      knownLocalizations <- JsonHelpers.vec[LanguageTag](cursor, "KnownLocalizations")
      manufacturer <- JsonHelpers.opt[XjdfString](cursor, "Manufacturer")
      manufacturerUrl <- JsonHelpers.opt[UriRef](cursor, "ManufacturerURL")
      maxRunSpeed <- JsonHelpers.opt[Float](cursor, "MaxRunSpeed")
      packaging <- JsonHelpers.vec[DevicePackaging](cursor, "Packaging")
      presentationUrl <- JsonHelpers.opt[UriRef](cursor, "PresentationURL")
      restApiBaseUrl <- JsonHelpers.opt[UriRef](cursor, "RestApiBaseURL")
      revision <- JsonHelpers.opt[XjdfString](cursor, "Revision")
      serialNumber <- JsonHelpers.opt[XjdfString](cursor, "SerialNumber")
      urlSchemes <- JsonHelpers.vec[Nmtoken](cursor, "URLSchemes")
      xjmfUrl <- JsonHelpers.opt[UriRef](cursor, "XJMFURL")
    yield Device(
      deviceId,
      costCenterId,
      descriptiveName,
      deviceClasses,
      deviceType,
      icsVersions,
      jdfVersions,
      knownLocalizations,
      manufacturer,
      manufacturerUrl,
      maxRunSpeed,
      packaging,
      presentationUrl,
      restApiBaseUrl,
      revision,
      serialNumber,
      urlSchemes,
      xjmfUrl,
      DeviceSchemas(),
      None,
      Vector.empty,
      Vector.empty,
    ),
  )
end JsonMediaCodecs
