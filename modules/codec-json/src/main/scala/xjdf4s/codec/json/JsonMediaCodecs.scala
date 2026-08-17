package xjdf4s.codec.json

import io.circe.{Decoder, Encoder, HCursor, Json}
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
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("AreaGlue", glue.areaGlue),
        JsonCodec.optMember("GlueLineWidth", glue.glueLineWidth),
        JsonCodec.optMember("GlueRef", glue.glueRef),
        JsonCodec.optMember("GlueType", glue.glueType),
        JsonCodec.optMember("GluingPattern", glue.gluingPattern.map(_.toVector)),
        JsonCodec.optMember("GluingTechnique", glue.gluingTechnique),
        JsonCodec.optMember("MeltingTemperature", glue.meltingTemperature),
        JsonCodec.optMember("StartPosition", glue.startPosition),
        JsonCodec.optMember("WorkingDirection", glue.workingDirection),
        JsonCodec.optMember("WorkingPath", glue.workingPath),
      ),
    ),
  )
  given Decoder[Glue] = Decoder.instance(cursor =>
    for
      areaGlue <- JsonCodec.opt[Boolean](cursor, "AreaGlue")
      glueLineWidth <- JsonCodec.opt[Float](cursor, "GlueLineWidth")
      glueRef <- JsonCodec.opt[XsdIdRef](cursor, "GlueRef")
      glueType <- JsonCodec.opt[GlueType](cursor, "GlueType")
      gluingPattern <- JsonCodec.opt[Vector[Float]](cursor, "GluingPattern")
      gluingTechnique <- JsonCodec.opt[GluingTechnique](cursor, "GluingTechnique")
      meltingTemperature <- JsonCodec.opt[Int](cursor, "MeltingTemperature")
      startPosition <- JsonCodec.opt[XYPair](cursor, "StartPosition")
      workingDirection <- JsonCodec.opt[Face](cursor, "WorkingDirection")
      workingPath <- JsonCodec.opt[XYPair](cursor, "WorkingPath")
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
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("Aperture", conditions.aperture),
        JsonCodec.optMember("DensityStandard", conditions.densityStandard),
        JsonCodec.optMember("Illumination", conditions.illumination),
        JsonCodec.optMember("IlluminationAngle", conditions.illuminationAngle),
        JsonCodec.optMember("InkState", conditions.inkState),
        JsonCodec.optMember("MeasurementAngle", conditions.measurementAngle),
        JsonCodec.optMember("MeasurementFilter", conditions.measurementFilter),
        JsonCodec.optMember("MeasurementMode", conditions.measurementMode),
        JsonCodec.optMember("Observer", conditions.observer),
        JsonCodec.optMember("SampleBacking", conditions.sampleBacking),
        JsonCodec.optMember("SpectralResolution", conditions.spectralResolution),
        JsonCodec.optMember("WhiteBase", conditions.whiteBase),
      ),
    ),
  )
  given Decoder[ColorMeasurementConditions] = Decoder.instance(cursor =>
    for
      aperture <- JsonCodec.opt[Float](cursor, "Aperture")
      densityStandard <- JsonCodec.opt[Nmtoken](cursor, "DensityStandard")
      illumination <- JsonCodec.opt[Nmtoken](cursor, "Illumination")
      illuminationAngle <- JsonCodec.opt[Int](cursor, "IlluminationAngle")
      inkState <- JsonCodec.opt[InkState](cursor, "InkState")
      measurementAngle <- JsonCodec.opt[Int](cursor, "MeasurementAngle")
      measurementFilter <- JsonCodec.opt[MeasurementFilter](cursor, "MeasurementFilter")
      measurementMode <- JsonCodec.opt[Nmtoken](cursor, "MeasurementMode")
      observer <- JsonCodec.opt[Int](cursor, "Observer")
      sampleBacking <- JsonCodec.opt[SampleBacking](cursor, "SampleBacking")
      spectralResolution <- JsonCodec.opt[Float](cursor, "SpectralResolution")
      whiteBase <- JsonCodec.opt[WhiteBase](cursor, "WhiteBase")
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
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("BackBrightness", media.backBrightness),
        JsonCodec.optMember("BackCIETint", media.backCieTint),
        JsonCodec.optMember("BackCIEWhiteness", media.backCieWhiteness),
        JsonCodec.optMember("BackCoating", media.backCoating),
        JsonCodec.optMember("BackCoatingDetail", media.backCoatingDetail),
        JsonCodec.optMember("BackGlossValue", media.backGlossValue),
        JsonCodec.optMember("BackISOPaperSubstrate", media.backIsoPaperSubstrate),
        JsonCodec.optMember("BackLabColorValue", media.backLabColorValue),
        JsonCodec.optMember("BackSpectrum", media.backSpectrum.map(_.toVector)),
        JsonCodec.optMember("Brightness", media.brightness),
        JsonCodec.optMember("CIETint", media.cieTint),
        JsonCodec.optMember("CIEWhiteness", media.cieWhiteness),
        JsonCodec.optMember("Coating", media.coating),
        JsonCodec.optMember("CoatingDetail", media.coatingDetail),
        JsonCodec.optMember("CoreWeight", media.coreWeight),
        JsonCodec.optMember("Dimension", media.dimension),
        JsonCodec.optMember("Flute", media.flute),
        JsonCodec.optMember("FluteDirection", media.fluteDirection),
        JsonCodec.optMember("GlossValue", media.glossValue),
        JsonCodec.optMember("GrainDirection", media.grainDirection),
        JsonCodec.optMember("ImagableSide", media.imagableSide),
        JsonCodec.optMember("InnerCoreDiameter", media.innerCoreDiameter),
        JsonCodec.optMember("InsideLoss", media.insideLoss),
        JsonCodec.optMember("ISOPaperSubstrate", media.isoPaperSubstrate),
        JsonCodec.optMember("LabColorValue", media.labColorValue),
        JsonCodec.optMember("MediaColorName", media.mediaColorName),
        JsonCodec.optMember("MediaColorNameDetails", media.mediaColorNameDetails),
        JsonCodec.optMember("MediaQuality", media.mediaQuality),
        JsonCodec.optMember("MediaSetCount", media.mediaSetCount),
        Vector(JsonCodec.member("MediaType", Json.fromString(media.mediaType.toString))),
        JsonCodec.optMember("MediaTypeDetails", media.mediaTypeDetails),
        JsonCodec.optMember("MediaUnit", media.mediaUnit),
        JsonCodec.optMember("Opacity", media.opacity),
        JsonCodec.optMember("OpacityLevel", media.opacityLevel),
        JsonCodec.optMember("OuterCoreDiameter", media.outerCoreDiameter),
        JsonCodec.optMember("OutsideGain", media.outsideGain),
        JsonCodec.optMember("PlateTechnology", media.plateTechnology),
        JsonCodec.optMember("Polarity", media.polarity),
        JsonCodec.optMember("PrintingTechnology", media.printingTechnology),
        JsonCodec.optMember("RecycledPercentage", media.recycledPercentage),
        JsonCodec.optMember("ReliefThickness", media.reliefThickness),
        JsonCodec.optMember("RollDiameter", media.rollDiameter),
        JsonCodec.optMember("ShrinkIndex", media.shrinkIndex),
        JsonCodec.optMember("SleeveInterlock", media.sleeveInterlock),
        JsonCodec.optMember("Spectrum", media.spectrum.map(_.toVector)),
        JsonCodec.optMember("StockType", media.stockType),
        JsonCodec.optMember("Texture", media.texture),
        JsonCodec.optMember("Thickness", media.thickness),
        JsonCodec.optMember("Weight", media.weight),
        JsonCodec.optMember("ColorMeasurementConditions", media.colorMeasurementConditions),
        JsonCodec.optMember("MediaLayers", media.mediaLayers),
      ),
    ),
  )
  given Decoder[Media] = Decoder.instance(cursor =>
    for
      mediaType <- cursor.get[MediaType]("MediaType")
      backBrightness <- JsonCodec.opt[Float](cursor, "BackBrightness")
      backCieTint <- JsonCodec.opt[Float](cursor, "BackCIETint")
      backCieWhiteness <- JsonCodec.opt[Float](cursor, "BackCIEWhiteness")
      backCoating <- JsonCodec.opt[Coating](cursor, "BackCoating")
      backCoatingDetail <- JsonCodec.opt[Nmtoken](cursor, "BackCoatingDetail")
      backGlossValue <- JsonCodec.opt[Float](cursor, "BackGlossValue")
      backIsoPaperSubstrate <- JsonCodec.opt[IsoPaperSubstrate](cursor, "BackISOPaperSubstrate")
      backLabColorValue <- JsonCodec.opt[LabColor](cursor, "BackLabColorValue")
      backSpectrum <- JsonCodec.opt[Vector[Float]](cursor, "BackSpectrum")
      brightness <- JsonCodec.opt[Float](cursor, "Brightness")
      cieTint <- JsonCodec.opt[Float](cursor, "CIETint")
      cieWhiteness <- JsonCodec.opt[Float](cursor, "CIEWhiteness")
      coating <- JsonCodec.opt[Coating](cursor, "Coating")
      coatingDetail <- JsonCodec.opt[Nmtoken](cursor, "CoatingDetail")
      coreWeight <- JsonCodec.opt[Float](cursor, "CoreWeight")
      dimension <- JsonCodec.opt[XYPair](cursor, "Dimension")
      flute <- JsonCodec.opt[Nmtoken](cursor, "Flute")
      fluteDirection <- JsonCodec.opt[MediaDirection](cursor, "FluteDirection")
      glossValue <- JsonCodec.opt[Float](cursor, "GlossValue")
      grainDirection <- JsonCodec.opt[MediaDirection](cursor, "GrainDirection")
      imagableSide <- JsonCodec.opt[ImagableSide](cursor, "ImagableSide")
      innerCoreDiameter <- JsonCodec.opt[Float](cursor, "InnerCoreDiameter")
      insideLoss <- JsonCodec.opt[Float](cursor, "InsideLoss")
      isoPaperSubstrate <- JsonCodec.opt[IsoPaperSubstrate](cursor, "ISOPaperSubstrate")
      labColorValue <- JsonCodec.opt[LabColor](cursor, "LabColorValue")
      mediaColorName <- JsonCodec.opt[NamedColor](cursor, "MediaColorName")
      mediaColorNameDetails <- JsonCodec.opt[XjdfString](cursor, "MediaColorNameDetails")
      mediaQuality <- JsonCodec.opt[XjdfString](cursor, "MediaQuality")
      mediaSetCount <- JsonCodec.opt[Int](cursor, "MediaSetCount")
      mediaTypeDetails <- JsonCodec.opt[Nmtoken](cursor, "MediaTypeDetails")
      mediaUnit <- JsonCodec.opt[MediaUnit](cursor, "MediaUnit")
      opacity <- JsonCodec.opt[Opacity](cursor, "Opacity")
      opacityLevel <- JsonCodec.opt[Float](cursor, "OpacityLevel")
      outerCoreDiameter <- JsonCodec.opt[Float](cursor, "OuterCoreDiameter")
      outsideGain <- JsonCodec.opt[Float](cursor, "OutsideGain")
      plateTechnology <- JsonCodec.opt[PlateTechnology](cursor, "PlateTechnology")
      polarity <- JsonCodec.opt[Polarity](cursor, "Polarity")
      printingTechnology <- JsonCodec.opt[Nmtoken](cursor, "PrintingTechnology")
      recycledPercentage <- JsonCodec.opt[Float](cursor, "RecycledPercentage")
      reliefThickness <- JsonCodec.opt[Float](cursor, "ReliefThickness")
      rollDiameter <- JsonCodec.opt[Float](cursor, "RollDiameter")
      shrinkIndex <- JsonCodec.opt[XYPair](cursor, "ShrinkIndex")
      sleeveInterlock <- JsonCodec.opt[Nmtoken](cursor, "SleeveInterlock")
      spectrum <- JsonCodec.opt[Vector[Float]](cursor, "Spectrum")
      stockType <- JsonCodec.opt[Nmtoken](cursor, "StockType")
      texture <- JsonCodec.opt[Nmtoken](cursor, "Texture")
      thickness <- JsonCodec.opt[Float](cursor, "Thickness")
      weight <- JsonCodec.opt[Float](cursor, "Weight")
      backSpectrumValue <- backSpectrum match
        case Some(values) => TransferFunction.from(values).left.map(error => io.circe.DecodingFailure(error.toString, cursor.history)).map(Some(_))
        case None         => Right(None)
      spectrumValue <- spectrum match
        case Some(values) => TransferFunction.from(values).left.map(error => io.circe.DecodingFailure(error.toString, cursor.history)).map(Some(_))
        case None         => Right(None)
      conditions <- JsonCodec.opt[ColorMeasurementConditions](cursor, "ColorMeasurementConditions")
      mediaLayers <- JsonCodec.opt[MediaLayers](cursor, "MediaLayers")
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
      backSpectrumValue,
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
      spectrumValue,
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
            case other   => JsonCodec.fail(item.hcursor, s"unknown MediaLayers Name '$other'")
        yield accumulated :+ layer
      }
    yield MediaLayers(layers),
  )

  given Encoder[Color] = Encoder.instance(color =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("ActualColorName", color.actualColorName),
        JsonCodec.optMember("CMYK", color.cmyk),
        JsonCodec.optMember("ColorBook", color.colorBook),
        JsonCodec.optMember("ColorBookEntry", color.colorBookEntry),
        JsonCodec.optMember("ColorDetails", color.colorDetails),
        JsonCodec.optMember("ColorName", color.colorName),
        JsonCodec.optMember("ColorType", color.colorType),
        JsonCodec.optMember("ColorTypeDetails", color.colorTypeDetails),
        JsonCodec.optMember("Density", color.density),
        JsonCodec.optMember("Gray", color.gray),
        JsonCodec.optMember("Lab", color.lab),
        JsonCodec.optMember("NeutralDensity", color.neutralDensity),
        JsonCodec.optMember("PrintingTechnology", color.printingTechnology),
        JsonCodec.optMember("PrintStandard", color.printStandard),
        JsonCodec.optMember("Spectrum", color.spectrum.map(_.toVector)),
        JsonCodec.optMember("sRGB", color.srgb),
        JsonCodec.optMember("ColorMeasurementConditions", color.colorMeasurementConditions),
      ),
    ),
  )
  given Decoder[Color] = Decoder.instance(cursor =>
    for
      actualColorName <- JsonCodec.opt[XjdfString](cursor, "ActualColorName")
      cmyk <- JsonCodec.opt[Vector[Float]](cursor, "CMYK")
      colorBook <- JsonCodec.opt[XjdfString](cursor, "ColorBook")
      colorBookEntry <- JsonCodec.opt[XjdfString](cursor, "ColorBookEntry")
      colorDetails <- JsonCodec.opt[XjdfString](cursor, "ColorDetails")
      colorName <- JsonCodec.opt[NamedColor](cursor, "ColorName")
      colorType <- JsonCodec.opt[ColorType](cursor, "ColorType")
      colorTypeDetails <- JsonCodec.opt[XjdfString](cursor, "ColorTypeDetails")
      density <- JsonCodec.opt[Float](cursor, "Density")
      gray <- JsonCodec.opt[Float](cursor, "Gray")
      lab <- JsonCodec.opt[LabColor](cursor, "Lab")
      neutralDensity <- JsonCodec.opt[Float](cursor, "NeutralDensity")
      printingTechnology <- JsonCodec.opt[Nmtoken](cursor, "PrintingTechnology")
      printStandard <- JsonCodec.opt[XjdfString](cursor, "PrintStandard")
      spectrum <- JsonCodec.opt[Vector[Float]](cursor, "Spectrum")
      srgb <- JsonCodec.opt[Vector[Float]](cursor, "sRGB")
      conditions <- JsonCodec.opt[ColorMeasurementConditions](cursor, "ColorMeasurementConditions")
      cmykValue <- cmyk match
        case Some(Vector(c, m, y, k)) => CmykColor.from(c, m, y, k).left.map(error => io.circe.DecodingFailure(error.toString, cursor.history)).map(Some(_))
        case Some(_)                  => JsonCodec.fail(cursor, "CMYK requires exactly four numbers")
        case None                     => Right(None)
      spectrumValue <- spectrum match
        case Some(values) => TransferFunction.from(values).left.map(error => io.circe.DecodingFailure(error.toString, cursor.history)).map(Some(_))
        case None         => Right(None)
      srgbValue <- srgb match
        case Some(Vector(r, g, b)) => SrgbColor.from(r, g, b).left.map(error => io.circe.DecodingFailure(error.toString, cursor.history)).map(Some(_))
        case Some(_)               => JsonCodec.fail(cursor, "sRGB requires exactly three numbers")
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
      spectrumValue,
      srgbValue,
      conditions,
      Vector.empty,
    ),
  )

  given Encoder[Component] = Encoder.instance(component =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("Automation", component.automation),
        JsonCodec.optMember("CartonTopFlaps", component.cartonTopFlaps),
        JsonCodec.optMember("Columns", component.columns),
        JsonCodec.vecMember("ContentRefs", component.contentRefs),
        JsonCodec.optMember("Dimensions", component.dimensions),
        JsonCodec.optMember("MaxHeat", component.maxHeat),
        JsonCodec.optMember("MediaRef", component.mediaRef),
        JsonCodec.optMember("Overfold", component.overfold),
        JsonCodec.optMember("OverfoldSide", component.overfoldSide),
        JsonCodec.optMember("ProductType", component.productType),
        JsonCodec.optMember("ProductTypeDetails", component.productTypeDetails),
        JsonCodec.optMember("ReaderPageCount", component.readerPageCount),
        JsonCodec.optMember("SurfaceCount", component.surfaceCount),
        JsonCodec.optMember("WindingResult", component.windingResult),
      ),
    ),
  )
  given Decoder[Component] = Decoder.instance(cursor =>
    for
      automation <- JsonCodec.opt[Automation](cursor, "Automation")
      cartonTopFlaps <- JsonCodec.opt[XYPair](cursor, "CartonTopFlaps")
      columns <- JsonCodec.opt[Int](cursor, "Columns")
      contentRefs <- JsonCodec.vec[XsdIdRef](cursor, "ContentRefs")
      dimensions <- JsonCodec.opt[Shape3D](cursor, "Dimensions")
      maxHeat <- JsonCodec.opt[Float](cursor, "MaxHeat")
      mediaRef <- JsonCodec.opt[XsdIdRef](cursor, "MediaRef")
      overfold <- JsonCodec.opt[Float](cursor, "Overfold")
      overfoldSide <- JsonCodec.opt[Side](cursor, "OverfoldSide")
      productType <- JsonCodec.opt[Nmtoken](cursor, "ProductType")
      productTypeDetails <- JsonCodec.opt[XjdfString](cursor, "ProductTypeDetails")
      readerPageCount <- JsonCodec.opt[Int](cursor, "ReaderPageCount")
      surfaceCount <- JsonCodec.opt[Int](cursor, "SurfaceCount")
      windingResult <- JsonCodec.opt[Int](cursor, "WindingResult")
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
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("ToolType", tool.toolType),
        JsonCodec.optMember("Manufacturer", tool.manufacturer),
        JsonCodec.optMember("ManufacturerURL", tool.manufacturerUrl),
        JsonCodec.optMember("SerialNumber", tool.serialNumber),
      ),
    ),
  )
  given Decoder[Tool] = Decoder.instance(cursor =>
    for
      toolType <- JsonCodec.opt[Nmtoken](cursor, "ToolType")
      manufacturer <- JsonCodec.opt[XjdfString](cursor, "Manufacturer")
      manufacturerUrl <- JsonCodec.opt[UriRef](cursor, "ManufacturerURL")
      serialNumber <- JsonCodec.opt[XjdfString](cursor, "SerialNumber")
    yield Tool(toolType, manufacturer, manufacturerUrl, serialNumber, Vector.empty),
  )

  given Encoder[RunList] = Encoder.instance(runList =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("Automation", runList.automation),
        JsonCodec.optMember("ClipPath", runList.clipPath),
        JsonCodec.vecMember("ContentRefs", runList.contentRefs),
        JsonCodec.optMember("Docs", runList.docs),
        JsonCodec.vecMember("DocPages", runList.docPages),
        JsonCodec.optMember("EndOfDocument", runList.endOfDocument),
        JsonCodec.optMember("EndOfSet", runList.endOfSet),
        JsonCodec.optMember("FinishedPages", runList.finishedPages),
        JsonCodec.optMember("LogicalPage", runList.logicalPage),
        JsonCodec.optMember("NumberOfPages", runList.numberOfPages),
        JsonCodec.optMember("OrderType", runList.orderType),
        JsonCodec.optMember("Pages", runList.pages),
        JsonCodec.optMember("Sets", runList.sets),
        JsonCodec.optMember("SourceBleedBox", runList.sourceBleedBox),
        JsonCodec.optMember("SourceClipBox", runList.sourceClipBox),
        JsonCodec.optMember("SourceMediaBox", runList.sourceMediaBox),
        JsonCodec.optMember("SourceTrimBox", runList.sourceTrimBox),
      ),
    ),
  )
  given Decoder[RunList] = Decoder.instance(cursor =>
    for
      automation <- JsonCodec.opt[Automation](cursor, "Automation")
      clipPath <- JsonCodec.opt[PdfPath](cursor, "ClipPath")
      contentRefs <- JsonCodec.vec[XsdIdRef](cursor, "ContentRefs")
      docs <- JsonCodec.opt[IntegerRange](cursor, "Docs")
      docPages <- JsonCodec.vec[Int](cursor, "DocPages")
      endOfDocument <- JsonCodec.opt[Boolean](cursor, "EndOfDocument")
      endOfSet <- JsonCodec.opt[Boolean](cursor, "EndOfSet")
      finishedPages <- JsonCodec.opt[Int](cursor, "FinishedPages")
      logicalPage <- JsonCodec.opt[Int](cursor, "LogicalPage")
      numberOfPages <- JsonCodec.opt[Int](cursor, "NumberOfPages")
      orderType <- JsonCodec.opt[RunListOrderType](cursor, "OrderType")
      pages <- JsonCodec.opt[IntegerRange](cursor, "Pages")
      sets <- JsonCodec.opt[IntegerRange](cursor, "Sets")
      sourceBleedBox <- JsonCodec.opt[Rectangle](cursor, "SourceBleedBox")
      sourceClipBox <- JsonCodec.opt[Rectangle](cursor, "SourceClipBox")
      sourceMediaBox <- JsonCodec.opt[Rectangle](cursor, "SourceMediaBox")
      sourceTrimBox <- JsonCodec.opt[Rectangle](cursor, "SourceTrimBox")
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
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("Center", mark.center),
        JsonCodec.optMember("MarkName", mark.markName),
        JsonCodec.vecMember("MarkTypes", mark.markTypes),
        JsonCodec.vecMember("MarkUsage", mark.markUsage),
        JsonCodec.optMember("Rotation", mark.rotation),
        JsonCodec.vecMember("Separations", mark.separations),
        JsonCodec.optMember("Size", mark.size),
      ),
    ),
  )
  given Decoder[RegisterMark] = Decoder.instance(cursor =>
    for
      center <- JsonCodec.opt[XYPair](cursor, "Center")
      markName <- JsonCodec.opt[Nmtoken](cursor, "MarkName")
      markTypes <- JsonCodec.vec[Nmtoken](cursor, "MarkTypes")
      markUsage <- JsonCodec.vec[MarkUsage](cursor, "MarkUsage")
      rotation <- JsonCodec.opt[Float](cursor, "Rotation")
      separations <- JsonCodec.vec[Nmtoken](cursor, "Separations")
      size <- JsonCodec.opt[XYPair](cursor, "Size")
    yield RegisterMark(center, markName, markTypes, markUsage, rotation, separations, size, Vector.empty),
  )

  given Encoder[Device] = Encoder.instance(device =>
    JsonCodec.obj(
      JsonCodec.memberList(
        JsonCodec.optMember("CostCenterID", device.costCenterId),
        JsonCodec.optMember("DescriptiveName", device.descriptiveName),
        JsonCodec.vecMember("DeviceClass", device.deviceClasses),
        Vector(JsonCodec.member("DeviceID", Json.fromString(device.deviceId.value))),
        JsonCodec.optMember("DeviceType", device.deviceType),
        JsonCodec.vecMember("ICSVersions", device.icsVersions),
        JsonCodec.vecMember("JDFVersions", device.jdfVersions),
        JsonCodec.vecMember("KnownLocalizations", device.knownLocalizations),
        JsonCodec.optMember("Manufacturer", device.manufacturer),
        JsonCodec.optMember("ManufacturerURL", device.manufacturerUrl),
        JsonCodec.optMember("MaxRunSpeed", device.maxRunSpeed),
        JsonCodec.vecMember("Packaging", device.packaging),
        JsonCodec.optMember("PresentationURL", device.presentationUrl),
        JsonCodec.optMember("RestApiBaseURL", device.restApiBaseUrl),
        JsonCodec.optMember("Revision", device.revision),
        JsonCodec.optMember("SerialNumber", device.serialNumber),
        JsonCodec.vecMember("URLSchemes", device.urlSchemes),
        JsonCodec.optMember("XJMFURL", device.xjmfUrl),
      ),
    ),
  )
  given Decoder[Device] = Decoder.instance(cursor =>
    for
      deviceId <- cursor.get[Nmtoken]("DeviceID")
      costCenterId <- JsonCodec.opt[Nmtoken](cursor, "CostCenterID")
      descriptiveName <- JsonCodec.opt[XjdfString](cursor, "DescriptiveName")
      deviceClasses <- JsonCodec.vec[Nmtoken](cursor, "DeviceClass")
      deviceType <- JsonCodec.opt[XjdfString](cursor, "DeviceType")
      icsVersions <- JsonCodec.vec[Nmtoken](cursor, "ICSVersions")
      jdfVersions <- JsonCodec.vec[JdfVersion](cursor, "JDFVersions")
      knownLocalizations <- JsonCodec.vec[LanguageTag](cursor, "KnownLocalizations")
      manufacturer <- JsonCodec.opt[XjdfString](cursor, "Manufacturer")
      manufacturerUrl <- JsonCodec.opt[UriRef](cursor, "ManufacturerURL")
      maxRunSpeed <- JsonCodec.opt[Float](cursor, "MaxRunSpeed")
      packaging <- JsonCodec.vec[DevicePackaging](cursor, "Packaging")
      presentationUrl <- JsonCodec.opt[UriRef](cursor, "PresentationURL")
      restApiBaseUrl <- JsonCodec.opt[UriRef](cursor, "RestApiBaseURL")
      revision <- JsonCodec.opt[XjdfString](cursor, "Revision")
      serialNumber <- JsonCodec.opt[XjdfString](cursor, "SerialNumber")
      urlSchemes <- JsonCodec.vec[Nmtoken](cursor, "URLSchemes")
      xjmfUrl <- JsonCodec.opt[UriRef](cursor, "XJMFURL")
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
