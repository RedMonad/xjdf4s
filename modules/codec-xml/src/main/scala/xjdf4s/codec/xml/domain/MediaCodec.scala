package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.core.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

object GlueCodec:
  val decoder: XmlDecoder[Glue] =
    XmlDecoder.instance: element =>
      for
        areaGlue <- XmlDecoders.attributeOf("AreaGlue")(Lexical.bool).decode(element)
        glueLineWidth <- XmlDecoders.attributeOf("GlueLineWidth")(Lexical.float).decode(element)
        glueRef <- XmlDecoders.attributeOf("GlueRef")(Lexical.xsdIdRef).decode(element)
        glueType <- XmlDecoders.attributeOf("GlueType")(Lexical.glueType).decode(element)
        gluingPattern <- XmlDecoders.attributeOf("GluingPattern")(Lexical.gluingPattern).decode(element)
        gluingTechnique <- XmlDecoders.attributeOf("GluingTechnique")(Lexical.gluingTechnique).decode(element)
        meltingTemperature <- XmlDecoders.attributeOf("MeltingTemperature")(Lexical.int).decode(element)
        startPosition <- XmlDecoders.attributeOf("StartPosition")(Lexical.xypair).decode(element)
        workingDirection <- XmlDecoders.attributeOf("WorkingDirection")(Lexical.face).decode(element)
        workingPath <- XmlDecoders.attributeOf("WorkingPath")(Lexical.xypair).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
      yield Glue(
        areaGlue,
        glueLineWidth,
        glueRef,
        glueType,
        gluingPattern,
        gluingTechnique,
        meltingTemperature,
        startPosition,
        workingDirection,
        workingPath,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[Glue] =
    XmlEncoder.instance: glue =>
      val attributes =
        CodecHelpers.attributeOf("AreaGlue", glue.areaGlue, CodecHelpers.renderBoolean) ++
          CodecHelpers.attributeOf("GlueLineWidth", glue.glueLineWidth, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("GlueRef", glue.glueRef, (v: XsdIdRef) => v.value) ++
          CodecHelpers.attributeOf("GlueType", glue.glueType, _.toString) ++
          CodecHelpers.attribute(
            "GluingPattern",
            glue.gluingPattern.map(pattern => CodecHelpers.renderFloats(pattern.toVector)),
          ) ++
          CodecHelpers.attributeOf("GluingTechnique", glue.gluingTechnique, _.toString) ++
          CodecHelpers.attributeOf("MeltingTemperature", glue.meltingTemperature, CodecHelpers.renderInt) ++
          CodecHelpers.attributeOf("StartPosition", glue.startPosition, CodecHelpers.renderXypair) ++
          CodecHelpers.attributeOf("WorkingDirection", glue.workingDirection, _.toString) ++
          CodecHelpers.attributeOf("WorkingPath", glue.workingPath, CodecHelpers.renderXypair) ++
          CodecHelpers.extensionAttributes(glue.extensions)
      Xml.Element(CodecHelpers.qname("Glue"), attributes, Vector.empty)
end GlueCodec

object MediaLayersCodec:
  val decoder: XmlDecoder[MediaLayers] =
    XmlDecoder.instance: element =>
      val standardChildren = element.childElements.filter(_.name.namespace == XjdfNamespace.uri)
      val unexpected = standardChildren.find(child => child.name.localName != "Glue" && child.name.localName != "Media")
      for
        _ <- unexpected match
          case Some(child) => Left(XmlError.UnexpectedElement("MediaLayers", child.name.localName))
          case None        => Right(())
        layers <- standardChildren.foldLeft[Either[XmlError, Vector[MediaLayer]]](Right(Vector.empty)) {
          (acc, child) =>
            for
              values <- acc
              layer <- child.name.localName match
                case "Glue"  => GlueCodec.decoder.decode(child).map(MediaLayer.GlueLayer(_))
                case _       => MediaCodec.decoder.decode(child).map(MediaLayer.MediaLayer(_))
            yield values :+ layer
        }
      yield MediaLayers(layers, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[MediaLayers] =
    XmlEncoder.instance: mediaLayers =>
      val children = mediaLayers.layers.map {
        case MediaLayer.GlueLayer(glue) => GlueCodec.encoder.encode(glue)
        case MediaLayer.MediaLayer(media) => MediaCodec.encoder.encode(media)
      }
      Xml.Element(
        CodecHelpers.qname("MediaLayers"),
        CodecHelpers.extensionAttributes(mediaLayers.extensions),
        children,
      )
end MediaLayersCodec

object ColorMeasurementConditionsCodec:
  val decoder: XmlDecoder[ColorMeasurementConditions] =
    XmlDecoder.instance: element =>
      for
        aperture <- XmlDecoders.attributeOf("Aperture")(Lexical.float).decode(element)
        densityStandard <- XmlDecoders.attributeOf("DensityStandard")(Lexical.nmtoken).decode(element)
        illumination <- XmlDecoders.attributeOf("Illumination")(Lexical.nmtoken).decode(element)
        illuminationAngle <- XmlDecoders.attributeOf("IlluminationAngle")(Lexical.int).decode(element)
        inkState <- XmlDecoders.attributeOf("InkState")(Lexical.inkState).decode(element)
        measurementAngle <- XmlDecoders.attributeOf("MeasurementAngle")(Lexical.int).decode(element)
        measurementFilter <- XmlDecoders.attributeOf("MeasurementFilter")(Lexical.measurementFilter).decode(element)
        measurementMode <- XmlDecoders.attributeOf("MeasurementMode")(Lexical.nmtoken).decode(element)
        observer <- XmlDecoders.attributeOf("Observer")(Lexical.int).decode(element)
        sampleBacking <- XmlDecoders.attributeOf("SampleBacking")(Lexical.sampleBacking).decode(element)
        spectralResolution <- XmlDecoders.attributeOf("SpectralResolution")(Lexical.float).decode(element)
        whiteBase <- XmlDecoders.attributeOf("WhiteBase")(Lexical.whiteBase).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
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
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[ColorMeasurementConditions] =
    XmlEncoder.instance: conditions =>
      val attributes =
        CodecHelpers.attributeOf("Aperture", conditions.aperture, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("DensityStandard", conditions.densityStandard, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Illumination", conditions.illumination, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("IlluminationAngle", conditions.illuminationAngle, CodecHelpers.renderInt) ++
          CodecHelpers.attributeOf("InkState", conditions.inkState, _.toString) ++
          CodecHelpers.attributeOf("MeasurementAngle", conditions.measurementAngle, CodecHelpers.renderInt) ++
          CodecHelpers.attributeOf("MeasurementFilter", conditions.measurementFilter, _.toString) ++
          CodecHelpers.attributeOf("MeasurementMode", conditions.measurementMode, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Observer", conditions.observer, CodecHelpers.renderInt) ++
          CodecHelpers.attributeOf("SampleBacking", conditions.sampleBacking, _.toString) ++
          CodecHelpers.attributeOf("SpectralResolution", conditions.spectralResolution, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("WhiteBase", conditions.whiteBase, _.toString) ++
          CodecHelpers.extensionAttributes(conditions.extensions)
      Xml.Element(CodecHelpers.qname("ColorMeasurementConditions"), attributes, Vector.empty)
end ColorMeasurementConditionsCodec

object MediaCodec:
  val decoder: XmlDecoder[Media] =
    XmlDecoder.instance: element =>
      for
        mediaType <- XmlDecoders.requiredAttribute("MediaType")(Lexical.mediaType).decode(element)
        backBrightness <- XmlDecoders.attributeOf("BackBrightness")(Lexical.float).decode(element)
        backCieTint <- XmlDecoders.attributeOf("BackCIETint")(Lexical.float).decode(element)
        backCieWhiteness <- XmlDecoders.attributeOf("BackCIEWhiteness")(Lexical.float).decode(element)
        backCoating <- XmlDecoders.attributeOf("BackCoating")(Lexical.coating).decode(element)
        backCoatingDetail <- XmlDecoders.attributeOf("BackCoatingDetail")(Lexical.nmtoken).decode(element)
        backGlossValue <- XmlDecoders.attributeOf("BackGlossValue")(Lexical.float).decode(element)
        backIsoPaperSubstrate <- XmlDecoders.attributeOf("BackISOPaperSubstrate")(Lexical.isoPaperSubstrate).decode(element)
        backLabColorValue <- XmlDecoders.attributeOf("BackLabColorValue")(Lexical.labColor).decode(element)
        backSpectrum <- XmlDecoders.attributeOf("BackSpectrum")(Lexical.transferFunction).decode(element)
        brightness <- XmlDecoders.attributeOf("Brightness")(Lexical.float).decode(element)
        cieTint <- XmlDecoders.attributeOf("CIETint")(Lexical.float).decode(element)
        cieWhiteness <- XmlDecoders.attributeOf("CIEWhiteness")(Lexical.float).decode(element)
        coating <- XmlDecoders.attributeOf("Coating")(Lexical.coating).decode(element)
        coatingDetail <- XmlDecoders.attributeOf("CoatingDetail")(Lexical.nmtoken).decode(element)
        coreWeight <- XmlDecoders.attributeOf("CoreWeight")(Lexical.float).decode(element)
        dimension <- XmlDecoders.attributeOf("Dimension")(Lexical.xypair).decode(element)
        flute <- XmlDecoders.attributeOf("Flute")(Lexical.nmtoken).decode(element)
        fluteDirection <- XmlDecoders.attributeOf("FluteDirection")(Lexical.mediaDirection).decode(element)
        glossValue <- XmlDecoders.attributeOf("GlossValue")(Lexical.float).decode(element)
        grainDirection <- XmlDecoders.attributeOf("GrainDirection")(Lexical.mediaDirection).decode(element)
        imagableSide <- XmlDecoders.attributeOf("ImagableSide")(Lexical.imagableSide).decode(element)
        innerCoreDiameter <- XmlDecoders.attributeOf("InnerCoreDiameter")(Lexical.float).decode(element)
        insideLoss <- XmlDecoders.attributeOf("InsideLoss")(Lexical.float).decode(element)
        isoPaperSubstrate <- XmlDecoders.attributeOf("ISOPaperSubstrate")(Lexical.isoPaperSubstrate).decode(element)
        labColorValue <- XmlDecoders.attributeOf("LabColorValue")(Lexical.labColor).decode(element)
        mediaColorName <- XmlDecoders.attributeOf("MediaColorName")(Lexical.namedColor).decode(element)
        mediaColorNameDetails <- XmlDecoders.attributeOf("MediaColorNameDetails")(Lexical.xjdfString).decode(element)
        mediaQuality <- XmlDecoders.attributeOf("MediaQuality")(Lexical.xjdfString).decode(element)
        mediaSetCount <- XmlDecoders.attributeOf("MediaSetCount")(Lexical.int).decode(element)
        mediaTypeDetails <- XmlDecoders.attributeOf("MediaTypeDetails")(Lexical.nmtoken).decode(element)
        mediaUnit <- XmlDecoders.attributeOf("MediaUnit")(Lexical.mediaUnit).decode(element)
        opacity <- XmlDecoders.attributeOf("Opacity")(Lexical.opacity).decode(element)
        opacityLevel <- XmlDecoders.attributeOf("OpacityLevel")(Lexical.float).decode(element)
        outerCoreDiameter <- XmlDecoders.attributeOf("OuterCoreDiameter")(Lexical.float).decode(element)
        outsideGain <- XmlDecoders.attributeOf("OutsideGain")(Lexical.float).decode(element)
        plateTechnology <- XmlDecoders.attributeOf("PlateTechnology")(Lexical.plateTechnology).decode(element)
        polarity <- XmlDecoders.attributeOf("Polarity")(Lexical.polarity).decode(element)
        printingTechnology <- XmlDecoders.attributeOf("PrintingTechnology")(Lexical.nmtoken).decode(element)
        recycledPercentage <- XmlDecoders.attributeOf("RecycledPercentage")(Lexical.float).decode(element)
        reliefThickness <- XmlDecoders.attributeOf("ReliefThickness")(Lexical.float).decode(element)
        rollDiameter <- XmlDecoders.attributeOf("RollDiameter")(Lexical.float).decode(element)
        shrinkIndex <- XmlDecoders.attributeOf("ShrinkIndex")(Lexical.xypair).decode(element)
        sleeveInterlock <- XmlDecoders.attributeOf("SleeveInterlock")(Lexical.nmtoken).decode(element)
        spectrum <- XmlDecoders.attributeOf("Spectrum")(Lexical.transferFunction).decode(element)
        stockType <- XmlDecoders.attributeOf("StockType")(Lexical.nmtoken).decode(element)
        texture <- XmlDecoders.attributeOf("Texture")(Lexical.nmtoken).decode(element)
        thickness <- XmlDecoders.attributeOf("Thickness")(Lexical.float).decode(element)
        weight <- XmlDecoders.attributeOf("Weight")(Lexical.float).decode(element)
        conditions <- XmlDecoders.optionalChild("ColorMeasurementConditions")(ColorMeasurementConditionsCodec.decoder)
          .decode(element)
        mediaLayers <- XmlDecoders.optionalChild("MediaLayers")(MediaLayersCodec.decoder).decode(element)
        certifications <- XmlDecoders.repeatedChild("Certification")(summon[XmlElementCodec[Certification]])
          .decode(element)
        holePatterns <- XmlDecoders.repeatedChild("HolePattern")(summon[XmlElementCodec[HolePattern]]).decode(element)
        identificationFields <- XmlDecoders
          .repeatedChild("IdentificationField")(summon[XmlElementCodec[IdentificationField]])
          .decode(element)
        tabDimensions <- XmlDecoders.optionalChild("TabDimensions")(summon[XmlElementCodec[TabDimensions]]).decode(element)
        _ <- XmlDecoders
          .expectChildrenOnly(Set("ColorMeasurementConditions", "MediaLayers", "Certification", "HolePattern", "IdentificationField", "TabDimensions"))
          .decode(element)
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
        certifications,
        conditions,
        holePatterns,
        identificationFields,
        mediaLayers,
        tabDimensions,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[Media] =
    XmlEncoder.instance: media =>
      val attributes =
        CodecHelpers.attributeOf("BackBrightness", media.backBrightness, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("BackCIETint", media.backCieTint, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("BackCIEWhiteness", media.backCieWhiteness, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("BackCoating", media.backCoating, _.toString) ++
          CodecHelpers.attributeOf("BackCoatingDetail", media.backCoatingDetail, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("BackGlossValue", media.backGlossValue, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("BackISOPaperSubstrate", media.backIsoPaperSubstrate, _.toString) ++
          CodecHelpers.attributeOf("BackLabColorValue", media.backLabColorValue, CodecHelpers.renderLabColor) ++
          CodecHelpers.attribute(
            "BackSpectrum",
            media.backSpectrum.map(spectrum => CodecHelpers.renderFloats(spectrum.toVector)),
          ) ++
          CodecHelpers.attributeOf("Brightness", media.brightness, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("CIETint", media.cieTint, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("CIEWhiteness", media.cieWhiteness, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("Coating", media.coating, _.toString) ++
          CodecHelpers.attributeOf("CoatingDetail", media.coatingDetail, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("CoreWeight", media.coreWeight, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("Dimension", media.dimension, CodecHelpers.renderXypair) ++
          CodecHelpers.attributeOf("Flute", media.flute, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("FluteDirection", media.fluteDirection, _.toString) ++
          CodecHelpers.attributeOf("GlossValue", media.glossValue, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("GrainDirection", media.grainDirection, _.toString) ++
          CodecHelpers.attributeOf("ImagableSide", media.imagableSide, _.toString) ++
          CodecHelpers.attributeOf("InnerCoreDiameter", media.innerCoreDiameter, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("InsideLoss", media.insideLoss, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("ISOPaperSubstrate", media.isoPaperSubstrate, _.toString) ++
          CodecHelpers.attributeOf("LabColorValue", media.labColorValue, CodecHelpers.renderLabColor) ++
          CodecHelpers.attributeOf("MediaColorName", media.mediaColorName, (v: NamedColor) => v.lexical) ++
          CodecHelpers.attributeOf("MediaColorNameDetails", media.mediaColorNameDetails, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("MediaQuality", media.mediaQuality, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("MediaSetCount", media.mediaSetCount, CodecHelpers.renderInt) ++
          CodecHelpers.attribute("MediaType", Some(media.mediaType.toString)) ++
          CodecHelpers.attributeOf("MediaTypeDetails", media.mediaTypeDetails, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("MediaUnit", media.mediaUnit, _.toString) ++
          CodecHelpers.attributeOf("Opacity", media.opacity, _.toString) ++
          CodecHelpers.attributeOf("OpacityLevel", media.opacityLevel, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("OuterCoreDiameter", media.outerCoreDiameter, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("OutsideGain", media.outsideGain, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("PlateTechnology", media.plateTechnology, _.toString) ++
          CodecHelpers.attributeOf("Polarity", media.polarity, _.toString) ++
          CodecHelpers.attributeOf("PrintingTechnology", media.printingTechnology, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("RecycledPercentage", media.recycledPercentage, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("ReliefThickness", media.reliefThickness, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("RollDiameter", media.rollDiameter, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("ShrinkIndex", media.shrinkIndex, CodecHelpers.renderXypair) ++
          CodecHelpers.attributeOf("SleeveInterlock", media.sleeveInterlock, (v: Nmtoken) => v.value) ++
          CodecHelpers.attribute(
            "Spectrum",
            media.spectrum.map(spectrum => CodecHelpers.renderFloats(spectrum.toVector)),
          ) ++
          CodecHelpers.attributeOf("StockType", media.stockType, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Texture", media.texture, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Thickness", media.thickness, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("Weight", media.weight, CodecHelpers.renderFloat) ++
          CodecHelpers.extensionAttributes(media.extensions)
      // XSD xs:sequence order: Certification*, ColorMeasurementConditions?, HolePattern*, IdentificationField*,
      // MediaLayers?, TabDimensions?
      val children =
        media.certifications.map(summon[XmlElementCodec[Certification]].encode) ++
          media.colorMeasurementConditions.toVector.map(ColorMeasurementConditionsCodec.encoder.encode) ++
          media.holePatterns.map(summon[XmlElementCodec[HolePattern]].encode) ++
          media.identificationFields.map(summon[XmlElementCodec[IdentificationField]].encode) ++
          media.mediaLayers.toVector.map(MediaLayersCodec.encoder.encode) ++
          media.tabDimensions.toVector.map(summon[XmlElementCodec[TabDimensions]].encode)
      Xml.Element(CodecHelpers.qname("Media"), attributes, children)
end MediaCodec
