package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.core.*
import xjdf4s.model.*

object PartCodec:
  val decoder: XmlDecoder[Part] =
    XmlDecoder.instance: element =>
      for
        binderySignatureId <- XmlDecoders.attributeOf("BinderySignatureID")(Lexical.nmtoken).decode(element)
        blockName <- XmlDecoders.attributeOf("BlockName")(Lexical.nmtoken).decode(element)
        contactType <- XmlDecoders.attributeOf("ContactType")(Lexical.nmtoken).decode(element)
        docIndex <- XmlDecoders.attributeOf("DocIndex")(Lexical.integerRange).decode(element)
        dropId <- XmlDecoders.attributeOf("DropID")(Lexical.nmtoken).decode(element)
        location <- XmlDecoders.attributeOf("Location")(Lexical.nmtoken).decode(element)
        lotId <- XmlDecoders.attributeOf("LotID")(Lexical.nmtoken).decode(element)
        metadata <- XmlDecoders.attribute("Metadata").decode(element)
        option <- XmlDecoders.attributeOf("Option")(Lexical.nmtoken).decode(element)
        pageNumber <- XmlDecoders.attributeOf("PageNumber")(Lexical.integerRange).decode(element)
        partVersion <- XmlDecoders.attributeOf("PartVersion")(Lexical.nmtoken).decode(element)
        previewType <- XmlDecoders.attributeOf("PreviewType")(Lexical.previewType).decode(element)
        printCondition <- XmlDecoders.attributeOf("PrintCondition")(Lexical.nmtoken).decode(element)
        product <- XmlDecoders.attributeOf("Product")(Lexical.nmtoken).decode(element)
        productPart <- XmlDecoders.attributeOf("ProductPart")(Lexical.nmtoken).decode(element)
        qualityMeasurement <- XmlDecoders.attributeOf("QualityMeasurement")(Lexical.nmtoken).decode(element)
        run <- XmlDecoders.attributeOf("Run")(Lexical.nmtoken).decode(element)
        runIndex <- XmlDecoders.attributeOf("RunIndex")(Lexical.integerRange).decode(element)
        separation <- XmlDecoders.attributeOf("Separation")(Lexical.nmtoken).decode(element)
        setIndex <- XmlDecoders.attributeOf("SetIndex")(Lexical.integerRange).decode(element)
        sheetIndex <- XmlDecoders.attributeOf("SheetIndex")(Lexical.integerRange).decode(element)
        sheetName <- XmlDecoders.attributeOf("SheetName")(Lexical.nmtoken).decode(element)
        side <- XmlDecoders.attributeOf("Side")(Lexical.side).decode(element)
        stationName <- XmlDecoders.attributeOf("StationName")(Lexical.nmtoken).decode(element)
        tileId <- XmlDecoders.attributeOf("TileID")(Lexical.tileCoordinate).decode(element)
        transferCurveName <- XmlDecoders.attributeOf("TransferCurveName")(Lexical.transferCurveName).decode(element)
        webName <- XmlDecoders.attributeOf("WebName")(Lexical.nmtoken).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
      yield Part(
        binderySignatureId,
        blockName,
        contactType,
        docIndex,
        dropId,
        location,
        lotId,
        metadata,
        option,
        pageNumber,
        partVersion,
        previewType,
        printCondition,
        product,
        productPart,
        qualityMeasurement,
        run,
        runIndex,
        separation,
        setIndex,
        sheetIndex,
        sheetName,
        side,
        stationName,
        tileId,
        transferCurveName,
        webName,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[Part] =
    XmlEncoder.instance: part =>
      val attributes =
        CodecHelpers.attributeOf("BinderySignatureID", part.binderySignatureId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("BlockName", part.blockName, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("ContactType", part.contactType, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("DocIndex", part.docIndex, CodecHelpers.renderRange) ++
          CodecHelpers.attributeOf("DropID", part.dropId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Location", part.location, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("LotID", part.lotId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attribute("Metadata", part.metadata) ++
          CodecHelpers.attributeOf("Option", part.option, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("PageNumber", part.pageNumber, CodecHelpers.renderRange) ++
          CodecHelpers.attributeOf("PartVersion", part.partVersion, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("PreviewType", part.previewType, _.toString) ++
          CodecHelpers.attributeOf("PrintCondition", part.printCondition, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Product", part.product, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("ProductPart", part.productPart, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("QualityMeasurement", part.qualityMeasurement, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Run", part.run, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("RunIndex", part.runIndex, CodecHelpers.renderRange) ++
          CodecHelpers.attributeOf("Separation", part.separation, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("SetIndex", part.setIndex, CodecHelpers.renderRange) ++
          CodecHelpers.attributeOf("SheetIndex", part.sheetIndex, CodecHelpers.renderRange) ++
          CodecHelpers.attributeOf("SheetName", part.sheetName, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("Side", part.side, _.toString) ++
          CodecHelpers.attributeOf("StationName", part.stationName, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("TileID", part.tileId, CodecHelpers.renderTile) ++
          CodecHelpers.attributeOf("TransferCurveName", part.transferCurveName, _.toString) ++
          CodecHelpers.attributeOf("WebName", part.webName, (v: Nmtoken) => v.value) ++
          CodecHelpers.extensionAttributes(part.extensions)
      Xml.Element(CodecHelpers.qname("Part"), attributes, Vector.empty)
end PartCodec

object PartWasteCodec:
  val decoder: XmlDecoder[PartWaste] =
    XmlDecoder.instance: element =>
      for
        waste <- XmlDecoders.requiredAttribute("Waste")(Lexical.float).decode(element)
        moduleIds <- XmlDecoders.attributeOf("ModuleIDs")(Lexical.nmtokens).decode(element)
        wasteDetails <- XmlDecoders.attributeOf("WasteDetails")(Lexical.nmtoken).decode(element)
        origin <- (moduleIds, wasteDetails) match
          case (Some(modules), _) if modules.nonEmpty =>
            val nonEmpty = NonEmptyVector(modules.head, modules.tail*)
            wasteDetails match
              case Some(details) => Right(WasteOrigin.ModulesAndDetails(nonEmpty, details))
              case None          => Right(WasteOrigin.Modules(nonEmpty))
          case (_, Some(details)) => Right(WasteOrigin.Details(details))
          case _ =>
            Left(
              XmlError.InvalidAttribute(
                "PartWaste",
                "ModuleIDs/WasteDetails",
                "",
                "at least one of @ModuleIDs or @WasteDetails",
              ),
            )
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
      yield PartWaste(waste, origin, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[PartWaste] =
    XmlEncoder.instance: partWaste =>
      val originAttributes = partWaste.origin match
        case WasteOrigin.Modules(moduleIds) =>
          CodecHelpers.attribute("ModuleIDs", Some(CodecHelpers.renderNmtokens(moduleIds.toVector)))
        case WasteOrigin.Details(wasteDetails) =>
          CodecHelpers.attribute("WasteDetails", Some(wasteDetails.value))
        case WasteOrigin.ModulesAndDetails(moduleIds, wasteDetails) =>
          CodecHelpers.attribute("ModuleIDs", Some(CodecHelpers.renderNmtokens(moduleIds.toVector))) ++
            CodecHelpers.attribute("WasteDetails", Some(wasteDetails.value))
      val attributes =
        originAttributes ++
          CodecHelpers.attribute("Waste", Some(CodecHelpers.renderFloat(partWaste.waste))) ++
          CodecHelpers.extensionAttributes(partWaste.extensions)
      Xml.Element(CodecHelpers.qname("PartWaste"), attributes, Vector.empty)
end PartWasteCodec

object PartAmountCodec:
  val decoder: XmlDecoder[PartAmount] =
    XmlDecoder.instance: element =>
      for
        amount <- XmlDecoders.attributeOf("Amount")(Lexical.float).decode(element)
        maxAmount <- XmlDecoders.attributeOf("MaxAmount")(Lexical.float).decode(element)
        minAmount <- XmlDecoders.attributeOf("MinAmount")(Lexical.float).decode(element)
        waste <- XmlDecoders.attributeOf("Waste")(Lexical.float).decode(element)
        parts <- XmlDecoders.repeatedChild("Part")(PartCodec.decoder).decode(element)
        partWaste <- XmlDecoders.repeatedChild("PartWaste")(PartWasteCodec.decoder).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("Part", "PartWaste")).decode(element)
      yield PartAmount(
        amount,
        maxAmount,
        minAmount,
        waste,
        parts,
        partWaste,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[PartAmount] =
    XmlEncoder.instance: partAmount =>
      val attributes =
        CodecHelpers.attributeOf("Amount", partAmount.amount, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("MaxAmount", partAmount.maxAmount, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("MinAmount", partAmount.minAmount, CodecHelpers.renderFloat) ++
          CodecHelpers.attributeOf("Waste", partAmount.waste, CodecHelpers.renderFloat) ++
          CodecHelpers.extensionAttributes(partAmount.extensions)
      val children =
        partAmount.parts.map(PartCodec.encoder.encode) ++ partAmount.partWaste.map(PartWasteCodec.encoder.encode)
      Xml.Element(CodecHelpers.qname("PartAmount"), attributes, children)
end PartAmountCodec

object AmountPoolCodec:
  val decoder: XmlDecoder[AmountPool] =
    XmlDecoder.instance: element =>
      for
        amounts <- XmlDecoders.oneOrMoreChild("PartAmount")(PartAmountCodec.decoder).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("PartAmount")).decode(element)
      yield AmountPool(amounts, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[AmountPool] =
    XmlEncoder.instance: amountPool =>
      Xml.Element(
        CodecHelpers.qname("AmountPool"),
        CodecHelpers.extensionAttributes(amountPool.extensions),
        amountPool.amounts.toVector.map(PartAmountCodec.encoder.encode),
      )
end AmountPoolCodec
