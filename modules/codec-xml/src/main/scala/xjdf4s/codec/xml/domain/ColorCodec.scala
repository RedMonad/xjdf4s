package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.core.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

object DeviceNColorCodec:
  val decoder: XmlDecoder[DeviceNColor] =
    XmlDecoder.instance: element =>
      for
        colorList <- XmlDecoders.requiredAttribute("ColorList")(Lexical.floatList).decode(element)
        name <- XmlDecoders.requiredAttribute("Name")(Lexical.nmtoken).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
      yield DeviceNColor(colorList, name, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[DeviceNColor] =
    XmlEncoder.instance: deviceNColor =>
      val attributes =
        CodecHelpers.attribute("ColorList", Some(CodecHelpers.renderFloats(deviceNColor.colorList))) ++
          CodecHelpers.attribute("Name", Some(deviceNColor.name.value)) ++
          CodecHelpers.extensionAttributes(deviceNColor.extensions)
      Xml.Element(CodecHelpers.qname("DeviceNColor"), attributes, Vector.empty)
end DeviceNColorCodec

object ColorCodec:
  val decoder: XmlDecoder[Color] =
    XmlDecoder.instance: element =>
      for
        actualColorName <- XmlDecoders.attributeOf("ActualColorName")(Lexical.xjdfString).decode(element)
        cmyk <- XmlDecoders.attributeOf("CMYK")(Lexical.cmykColor).decode(element)
        colorBook <- XmlDecoders.attributeOf("ColorBook")(Lexical.xjdfString).decode(element)
        colorBookEntry <- XmlDecoders.attributeOf("ColorBookEntry")(Lexical.xjdfString).decode(element)
        colorDetails <- XmlDecoders.attributeOf("ColorDetails")(Lexical.xjdfString).decode(element)
        colorName <- XmlDecoders.attributeOf("ColorName")(Lexical.namedColor).decode(element)
        colorType <- XmlDecoders.attributeOf("ColorType")(Lexical.colorType).decode(element)
        colorTypeDetails <- XmlDecoders.attributeOf("ColorTypeDetails")(Lexical.xjdfString).decode(element)
        density <- XmlDecoders.attributeOf("Density")(Lexical.float).decode(element)
        gray <- XmlDecoders.attributeOf("Gray")(Lexical.float).decode(element)
        lab <- XmlDecoders.attributeOf("Lab")(Lexical.labColor).decode(element)
        neutralDensity <- XmlDecoders.attributeOf("NeutralDensity")(Lexical.neutralDensity).decode(element)
        printingTechnology <- XmlDecoders.attributeOf("PrintingTechnology")(Lexical.nmtoken).decode(element)
        printStandard <- XmlDecoders.attributeOf("PrintStandard")(Lexical.xjdfString).decode(element)
        rawName <- XmlDecoders.attributeOf("RawName")(Lexical.hexBinary).decode(element)
        spectrum <- XmlDecoders.attributeOf("Spectrum")(Lexical.transferFunction).decode(element)
        srgb <- XmlDecoders.attributeOf("sRGB")(Lexical.srgbColor).decode(element)
        conditions <- XmlDecoders.optionalChild("ColorMeasurementConditions")(ColorMeasurementConditionsCodec.decoder)
          .decode(element)
        deviceNColors <- XmlDecoders.repeatedChild("DeviceNColor")(DeviceNColorCodec.decoder).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("ColorMeasurementConditions", "DeviceNColor")).decode(element)
      yield Color(
        actualColorName,
        cmyk,
        colorBook,
        colorBookEntry,
        colorDetails,
        colorName,
        colorType,
        colorTypeDetails,
        density,
        gray,
        lab,
        neutralDensity,
        printingTechnology,
        printStandard,
        rawName.getOrElse(Vector.empty),
        spectrum,
        srgb,
        conditions,
        deviceNColors,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[Color] =
    XmlEncoder.instance: color =>
      val attributes =
        CodecHelpers.attributeOf("ActualColorName", color.actualColorName, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("CMYK", color.cmyk, CodecHelpers.renderCmykColor) ++
          CodecHelpers.attributeOf("ColorBook", color.colorBook, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("ColorBookEntry", color.colorBookEntry, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("ColorDetails", color.colorDetails, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("ColorName", color.colorName, (v: NamedColor) => v.lexical) ++
          CodecHelpers.attributeOf("ColorType", color.colorType, _.toString) ++
          CodecHelpers.attributeOf("ColorTypeDetails", color.colorTypeDetails, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("Density", color.density, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("Gray", color.gray, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("Lab", color.lab, CodecHelpers.renderLabColor) ++
          CodecHelpers.attributeOf("NeutralDensity", color.neutralDensity, (v: NeutralDensity) => v.value.toString) ++
          CodecHelpers.attributeOf("PrintingTechnology", color.printingTechnology, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("PrintStandard", color.printStandard, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf(
            "RawName",
            Option.when(color.rawName.nonEmpty)(Lexical.renderHexBinary(color.rawName)),
          ) ++
          CodecHelpers.attributeOf("Spectrum", color.spectrum.map(CodecHelpers.renderFloats)) ++
          CodecHelpers.attributeOf("sRGB", color.srgb, CodecHelpers.renderSrgbColor) ++
          CodecHelpers.extensionAttributes(color.extensions)
      val children =
        color.colorMeasurementConditions.toVector.map(ColorMeasurementConditionsCodec.encoder.encode) ++
          color.deviceNColors.map(DeviceNColorCodec.encoder.encode)
      Xml.Element(CodecHelpers.qname("Color"), attributes, children)
end ColorCodec
