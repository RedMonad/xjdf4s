package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.codec.xml.derivation.*
import xjdf4s.codec.xml.derivation.Derived.given
import xjdf4s.codec.xml.derivation.FieldCodec.given
import xjdf4s.core.*
import xjdf4s.model.resources.*

object ToolCodec:
  val decoder: XmlDecoder[Tool] =
    XmlDecoder.instance: element =>
      for
        toolType <- XmlDecoders.attributeOf("ToolType")(Lexical.nmtoken).decode(element)
        manufacturer <- XmlDecoders.attributeOf("Manufacturer")(Lexical.xjdfString).decode(element)
        manufacturerUrl <- XmlDecoders.attributeOf("ManufacturerURL")(Lexical.uri).decode(element)
        serialNumber <- XmlDecoders.attributeOf("SerialNumber")(Lexical.xjdfString).decode(element)
        identificationFields <- XmlDecoders
          .repeatedChild("IdentificationField")(summon[XmlElementCodec[IdentificationField]])
          .decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("IdentificationField")).decode(element)
      yield Tool(
        toolType,
        manufacturer,
        manufacturerUrl,
        serialNumber,
        identificationFields,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[Tool] =
    XmlEncoder.instance: tool =>
      val attributes =
        CodecHelpers.attributeOf("ToolType", tool.toolType, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Manufacturer", tool.manufacturer, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("ManufacturerURL", tool.manufacturerUrl, (v: UriRef) => v.value.toString) ++
          CodecHelpers.attributeOf("SerialNumber", tool.serialNumber, (v: XjdfString) => v.value) ++
          CodecHelpers.extensionAttributes(tool.extensions)
      val children = tool.identificationFields.map(summon[XmlElementCodec[IdentificationField]].encode)
      Xml.Element(CodecHelpers.qname("Tool"), attributes, children)
end ToolCodec

object ComponentCodec:
  val decoder: XmlDecoder[Component] =
    XmlDecoder.instance: element =>
      for
        automation <- XmlDecoders.attributeOf("Automation")(Lexical.automation).decode(element)
        cartonTopFlaps <- XmlDecoders.attributeOf("CartonTopFlaps")(Lexical.xypair).decode(element)
        columns <- XmlDecoders.attributeOf("Columns")(Lexical.int).decode(element)
        contentRefs <- XmlDecoders.attributeOf("ContentRefs")(Lexical.xsdIdRefs).decode(element)
        dimensions <- XmlDecoders.attributeOf("Dimensions")(Lexical.shape3d).decode(element)
        maxHeat <- XmlDecoders.attributeOf("MaxHeat")(Lexical.float).decode(element)
        mediaRef <- XmlDecoders.attributeOf("MediaRef")(Lexical.xsdIdRef).decode(element)
        overfold <- XmlDecoders.attributeOf("Overfold")(Lexical.float).decode(element)
        overfoldSide <- XmlDecoders.attributeOf("OverfoldSide")(Lexical.side).decode(element)
        productType <- XmlDecoders.attributeOf("ProductType")(Lexical.nmtoken).decode(element)
        productTypeDetails <- XmlDecoders.attributeOf("ProductTypeDetails")(Lexical.xjdfString).decode(element)
        readerPageCount <- XmlDecoders.attributeOf("ReaderPageCount")(Lexical.int).decode(element)
        surfaceCount <- XmlDecoders.attributeOf("SurfaceCount")(Lexical.int).decode(element)
        windingResult <- XmlDecoders.attributeOf("WindingResult")(Lexical.int).decode(element)
        identificationFields <- XmlDecoders
          .repeatedChild("IdentificationField")(summon[XmlElementCodec[IdentificationField]])
          .decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("IdentificationField")).decode(element)
      yield Component(
        automation,
        cartonTopFlaps,
        columns,
        contentRefs.getOrElse(Vector.empty),
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
        identificationFields,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[Component] =
    XmlEncoder.instance: component =>
      val attributes =
        CodecHelpers.attributeOf("Automation", component.automation, _.toString) ++
          CodecHelpers.attributeOf("CartonTopFlaps", component.cartonTopFlaps, CodecHelpers.renderXypair) ++
          CodecHelpers.attributeOf("Columns", component.columns, CodecHelpers.renderInt) ++
          CodecHelpers.attribute(
            "ContentRefs",
            Option.when(component.contentRefs.nonEmpty)(CodecHelpers.renderIdRefs(component.contentRefs)),
          ) ++
          CodecHelpers.attributeOf("Dimensions", component.dimensions, CodecHelpers.renderShape3d) ++
          CodecHelpers.attributeOf("MaxHeat", component.maxHeat, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("MediaRef", component.mediaRef, (v: XsdIdRef) => v.value) ++
          CodecHelpers.attributeOf("Overfold", component.overfold, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("OverfoldSide", component.overfoldSide, _.toString) ++
          CodecHelpers.attributeOf("ProductType", component.productType, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("ProductTypeDetails", component.productTypeDetails, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("ReaderPageCount", component.readerPageCount, CodecHelpers.renderInt) ++
          CodecHelpers.attributeOf("SurfaceCount", component.surfaceCount, CodecHelpers.renderInt) ++
          CodecHelpers.attributeOf("WindingResult", component.windingResult, CodecHelpers.renderInt) ++
          CodecHelpers.extensionAttributes(component.extensions)
      val children = component.identificationFields.map(summon[XmlElementCodec[IdentificationField]].encode)
      Xml.Element(CodecHelpers.qname("Component"), attributes, children)
end ComponentCodec

object DeviceCodec:
  val decoder: XmlDecoder[Device] =
    XmlDecoder.instance: element =>
      for
        deviceId <- XmlDecoders.requiredAttribute("DeviceID")(Lexical.nmtoken).decode(element)
        costCenterId <- XmlDecoders.attributeOf("CostCenterID")(Lexical.nmtoken).decode(element)
        descriptiveName <- XmlDecoders.attributeOf("DescriptiveName")(Lexical.xjdfString).decode(element)
        deviceClasses <- XmlDecoders.attributeOf("DeviceClass")(Lexical.nmtokens).decode(element)
        deviceType <- XmlDecoders.attributeOf("DeviceType")(Lexical.xjdfString).decode(element)
        icsVersions <- XmlDecoders.attributeOf("ICSVersions")(Lexical.nmtokens).decode(element)
        jdfVersions <- XmlDecoders.attributeOf("JDFVersions")(Lexical.list(Lexical.jdfVersion)).decode(element)
        knownLocalizations <- XmlDecoders.attributeOf("KnownLocalizations")(Lexical.languages).decode(element)
        manufacturer <- XmlDecoders.attributeOf("Manufacturer")(Lexical.xjdfString).decode(element)
        manufacturerUrl <- XmlDecoders.attributeOf("ManufacturerURL")(Lexical.uri).decode(element)
        maxRunSpeed <- XmlDecoders.attributeOf("MaxRunSpeed")(Lexical.float).decode(element)
        packaging <- XmlDecoders.attributeOf("Packaging")(Lexical.list(Lexical.devicePackaging)).decode(element)
        presentationUrl <- XmlDecoders.attributeOf("PresentationURL")(Lexical.uri).decode(element)
        restApiBaseUrl <- XmlDecoders.attributeOf("RestApiBaseURL")(Lexical.uri).decode(element)
        revision <- XmlDecoders.attributeOf("Revision")(Lexical.xjdfString).decode(element)
        serialNumber <- XmlDecoders.attributeOf("SerialNumber")(Lexical.xjdfString).decode(element)
        urlSchemes <- XmlDecoders.attributeOf("URLSchemes")(Lexical.nmtokens).decode(element)
        xjmfUrl <- XmlDecoders.attributeOf("XJMFURL")(Lexical.uri).decode(element)
        fileSpecChildren = element.childElements.filter(_.name.localName == "FileSpec")
        schemas <- summon[FieldCodec[DeviceSchemas]].decodeElements(fileSpecChildren)
        iconList <- XmlDecoders.optionalChild("IconList")(summon[XmlElementCodec[IconList]]).decode(element)
        identificationFields <- XmlDecoders
          .repeatedChild("IdentificationField")(summon[XmlElementCodec[IdentificationField]])
          .decode(element)
        modules <- XmlDecoders.repeatedChild("Module")(summon[XmlElementCodec[DeviceModule]]).decode(element)
        _ <- XmlDecoders
          .expectChildrenOnly(Set("FileSpec", "IconList", "IdentificationField", "Module"))
          .decode(element)
      yield Device(
        deviceId,
        costCenterId,
        descriptiveName,
        deviceClasses.getOrElse(Vector.empty),
        deviceType,
        icsVersions.getOrElse(Vector.empty),
        jdfVersions.getOrElse(Vector.empty),
        knownLocalizations.getOrElse(Vector.empty),
        manufacturer,
        manufacturerUrl,
        maxRunSpeed,
        packaging.getOrElse(Vector.empty),
        presentationUrl,
        restApiBaseUrl,
        revision,
        serialNumber,
        urlSchemes.getOrElse(Vector.empty),
        xjmfUrl,
        schemas,
        iconList,
        identificationFields,
        modules,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[Device] =
    XmlEncoder.instance: device =>
      val attributes =
        CodecHelpers.attributeOf("CostCenterID", device.costCenterId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("DescriptiveName", device.descriptiveName, (v: XjdfString) => v.value) ++
          CodecHelpers.attribute(
            "DeviceClass",
            Option.when(device.deviceClasses.nonEmpty)(CodecHelpers.renderNmtokens(device.deviceClasses)),
          ) ++
          CodecHelpers.attribute("DeviceID", Some(device.deviceId.value)) ++
          CodecHelpers.attributeOf("DeviceType", device.deviceType, (v: XjdfString) => v.value) ++
          CodecHelpers.attribute(
            "ICSVersions",
        Option.when(device.icsVersions.nonEmpty)(CodecHelpers.renderNmtokens(device.icsVersions)),
      ) ++
          CodecHelpers.attribute(
            "JDFVersions",
        Option.when(device.jdfVersions.nonEmpty)(device.jdfVersions.map(_.lexical).mkString(" ")),
      ) ++
          CodecHelpers.attribute(
            "KnownLocalizations",
        Option.when(device.knownLocalizations.nonEmpty)(CodecHelpers.renderLanguages(device.knownLocalizations)),
      ) ++
          CodecHelpers.attributeOf("Manufacturer", device.manufacturer, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("ManufacturerURL", device.manufacturerUrl, (v: UriRef) => v.value.toString) ++
          CodecHelpers.attributeOf("MaxRunSpeed", device.maxRunSpeed, CodecHelpers.renderFloat) ++
          CodecHelpers.attribute(
            "Packaging",
        Option.when(device.packaging.nonEmpty)(device.packaging.map(_.toString).mkString(" ")),
      ) ++
          CodecHelpers.attributeOf("PresentationURL", device.presentationUrl, (v: UriRef) => v.value.toString) ++
          CodecHelpers.attributeOf("RestApiBaseURL", device.restApiBaseUrl, (v: UriRef) => v.value.toString) ++
          CodecHelpers.attributeOf("Revision", device.revision, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("SerialNumber", device.serialNumber, (v: XjdfString) => v.value) ++
          CodecHelpers.attribute(
            "URLSchemes",
        Option.when(device.urlSchemes.nonEmpty)(CodecHelpers.renderNmtokens(device.urlSchemes)),
      ) ++
          CodecHelpers.attributeOf("XJMFURL", device.xjmfUrl, (v: UriRef) => v.value.toString) ++
          CodecHelpers.extensionAttributes(device.extensions)
      val children =
        summon[FieldCodec[DeviceSchemas]].encodeElements(device.schemas) ++
          device.iconList.toVector.map(summon[XmlElementCodec[IconList]].encode) ++
          device.identificationFields.map(summon[XmlElementCodec[IdentificationField]].encode) ++
          device.modules.map(summon[XmlElementCodec[DeviceModule]].encode)
      Xml.Element(CodecHelpers.qname("Device"), attributes, children)
end DeviceCodec
