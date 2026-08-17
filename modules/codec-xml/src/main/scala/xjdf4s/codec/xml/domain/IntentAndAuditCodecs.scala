package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.codec.xml.derivation.{Derived, FieldCodec}
import xjdf4s.codec.xml.derivation.Derived.given
import xjdf4s.codec.xml.derivation.FieldCodec.given
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * Hand codecs for nodes whose XML shape cannot be produced by the generic derivation: coproducts that map to
 * attribute sets or children, context-dependent element names, and the closed Audit family.
 */

// -- element-name overrides for detail types (normative names differ from class names) ----------

given adhesiveNoteDetailsCodec: XmlElementCodec[AdhesiveNoteDetails] = Derived.derivedNamed[AdhesiveNoteDetails]("AdhesiveNote")
given edgeGluingDetailsCodec: XmlElementCodec[EdgeGluingDetails] = Derived.derivedNamed[EdgeGluingDetails]("EdgeGluing")
given hardCoverBindingDetailsCodec: XmlElementCodec[HardCoverBindingDetails] = Derived.derivedNamed[HardCoverBindingDetails]("HardCoverBinding")
given softCoverBindingDetailsCodec: XmlElementCodec[SoftCoverBindingDetails] = Derived.derivedNamed[SoftCoverBindingDetails]("SoftCoverBinding")
given looseBindingDetailsCodec: XmlElementCodec[LooseBindingDetails] = Derived.derivedNamed[LooseBindingDetails]("LooseBinding")
given coilLooseBindingDetailsCodec: XmlElementCodec[CoilLooseBindingDetails] = Derived.derivedNamed[CoilLooseBindingDetails]("LooseBinding")
given combLooseBindingDetailsCodec: XmlElementCodec[CombLooseBindingDetails] = Derived.derivedNamed[CombLooseBindingDetails]("LooseBinding")
given ringLooseBindingDetailsCodec: XmlElementCodec[RingLooseBindingDetails] = Derived.derivedNamed[RingLooseBindingDetails]("LooseBinding")
given coilBindingDetailsCodec: XmlElementCodec[CoilBindingDetails] = Derived.derivedNamed[CoilBindingDetails]("CoilBinding")
given combBindingDetailsCodec: XmlElementCodec[CombBindingDetails] = Derived.derivedNamed[CombBindingDetails]("CombBinding")
given ringBindingDetailsCodec: XmlElementCodec[RingBindingDetails] = Derived.derivedNamed[RingBindingDetails]("RingBinding")
given channelBindingProductionDetailsCodec: XmlElementCodec[ChannelBindingProductionDetails] =
  Derived.derivedNamed[ChannelBindingProductionDetails]("ChannelBindingDetails")
given coilBindingProductionDetailsCodec: XmlElementCodec[CoilBindingProductionDetails] =
  Derived.derivedNamed[CoilBindingProductionDetails]("CoilBindingDetails")
given combBindingProductionDetailsCodec: XmlElementCodec[CombBindingProductionDetails] =
  Derived.derivedNamed[CombBindingProductionDetails]("CombBindingDetails")
given ringBindingProductionDetailsCodec: XmlElementCodec[RingBindingProductionDetails] =
  Derived.derivedNamed[RingBindingProductionDetails]("RingBindingDetails")
given stripBindingProductionDetailsCodec: XmlElementCodec[StripBindingProductionDetails] =
  Derived.derivedNamed[StripBindingProductionDetails]("StripBindingDetails")

// -- BindingIntent: @BindingType + compatible detail child -------------------------------------

object BindingIntentCodec:
  private val bindingTypes: Vector[(BindingSpecification, String)] = Vector(
    BindingSpecification.AdhesiveNote() -> "AdhesiveNote",
    BindingSpecification.ChannelBinding() -> "ChannelBinding",
    BindingSpecification.CoilBinding() -> "CoilBinding",
    BindingSpecification.CombBinding() -> "CombBinding",
    BindingSpecification.CornerStitch -> "CornerStitch",
    BindingSpecification.EdgeGluing() -> "EdgeGluing",
    BindingSpecification.HardCover() -> "HardCover",
    BindingSpecification.LooseBinding() -> "LooseBinding",
    BindingSpecification.None -> "None",
    BindingSpecification.RingBinding() -> "RingBinding",
    BindingSpecification.SaddleStitch() -> "SaddleStitch",
    BindingSpecification.SideStitch() -> "SideStitch",
    BindingSpecification.SoftCover() -> "SoftCover",
    BindingSpecification.StripBinding() -> "StripBinding",
    BindingSpecification.Tape -> "Tape",
    BindingSpecification.WireComb() -> "WireComb",
  )

  private val bindingType: Lexical.Lex[BindingSpecification] =
    value =>
      bindingTypes
        .find(_._2.equalsIgnoreCase(value.trim))
        .map(_._1)
        .toRight(s"'$value' is not a BindingType")

  private def plainDetails(name: String): XmlDecoder[Option[LooseBindingDetails]] =
    XmlDecoders.optionalChild(name)(summon[XmlElementCodec[LooseBindingDetails]])

  val decoder: XmlDecoder[BindingIntent] =
    XmlDecoder.instance: element =>
      for
        binding <- XmlDecoders.requiredAttribute("BindingType")(bindingType).decode(element)
        backCoverColor <- XmlDecoders.attributeOf("BackCoverColor")(Lexical.namedColor).decode(element)
        backCoverColorDetails <- XmlDecoders.attributeOf("BackCoverColorDetails")(Lexical.xjdfString).decode(element)
        bindingColor <- XmlDecoders.attributeOf("BindingColor")(Lexical.namedColor).decode(element)
        bindingColorDetails <- XmlDecoders.attributeOf("BindingColorDetails")(Lexical.xjdfString).decode(element)
        bindingOrder <- XmlDecoders.attributeOf("BindingOrder")(Lexical.enumOf(BindingOrder.values.toVector, _.toString))
          .decode(element)
        bindingSide <- XmlDecoders.attributeOf("BindingSide")(Lexical.enumOf(BindingEdge.values.toVector, _.toString))
          .decode(element)
        childRefs <- XmlDecoders.attributeOf("ChildRefs")(Lexical.xsdIdRefs).decode(element)
        coverColor <- XmlDecoders.attributeOf("CoverColor")(Lexical.namedColor).decode(element)
        coverColorDetails <- XmlDecoders.attributeOf("CoverColorDetails")(Lexical.xjdfString).decode(element)
        tabs <- XmlDecoders.optionalChild("Tabs")(summon[XmlElementCodec[Tabs]]).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("Tabs")).decode(element)
        details <- binding match
          case BindingSpecification.AdhesiveNote(_) =>
            XmlDecoders.optionalChild("AdhesiveNote")(summon[XmlElementCodec[AdhesiveNoteDetails]]).decode(element)
              .map(BindingSpecification.AdhesiveNote(_))
          case BindingSpecification.ChannelBinding(_) =>
            plainDetails("LooseBinding").decode(element).map(BindingSpecification.ChannelBinding(_))
          case BindingSpecification.CoilBinding(_) =>
            XmlDecoders.optionalChild("LooseBinding")(summon[XmlElementCodec[CoilLooseBindingDetails]]).decode(element)
              .map(BindingSpecification.CoilBinding(_))
          case BindingSpecification.CombBinding(_) =>
            XmlDecoders.optionalChild("LooseBinding")(summon[XmlElementCodec[CombLooseBindingDetails]]).decode(element)
              .map(BindingSpecification.CombBinding(_))
          case BindingSpecification.EdgeGluing(_) =>
            XmlDecoders.optionalChild("EdgeGluing")(summon[XmlElementCodec[EdgeGluingDetails]]).decode(element)
              .map(BindingSpecification.EdgeGluing(_))
          case BindingSpecification.HardCover(_) =>
            XmlDecoders.optionalChild("HardCoverBinding")(summon[XmlElementCodec[HardCoverBindingDetails]]).decode(element)
              .map(BindingSpecification.HardCover(_))
          case BindingSpecification.LooseBinding(_) =>
            plainDetails("LooseBinding").decode(element).map(BindingSpecification.LooseBinding(_))
          case BindingSpecification.RingBinding(_) =>
            XmlDecoders.optionalChild("LooseBinding")(summon[XmlElementCodec[RingLooseBindingDetails]]).decode(element)
              .map(BindingSpecification.RingBinding(_))
          case BindingSpecification.SaddleStitch(_) =>
            StitchingCodec.optional("SaddleStitching").decode(element).map(BindingSpecification.SaddleStitch(_))
          case BindingSpecification.SideStitch(_) =>
            StitchingCodec.optional("SideStitching").decode(element).map(BindingSpecification.SideStitch(_))
          case BindingSpecification.SoftCover(_) =>
            XmlDecoders.optionalChild("SoftCoverBinding")(summon[XmlElementCodec[SoftCoverBindingDetails]]).decode(element)
              .map(BindingSpecification.SoftCover(_))
          case BindingSpecification.StripBinding(_) =>
            plainDetails("LooseBinding").decode(element).map(BindingSpecification.StripBinding(_))
          case BindingSpecification.WireComb(_) =>
            plainDetails("LooseBinding").decode(element).map(BindingSpecification.WireComb(_))
          case other => Right(other)
      yield BindingIntent(
        details,
        backCoverColor,
        backCoverColorDetails,
        bindingColor,
        bindingColorDetails,
        bindingOrder,
        bindingSide,
        childRefs.flatMap(values => TwoOrMore.from(values).toOption),
        coverColor,
        coverColorDetails,
        tabs,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[BindingIntent] =
    XmlEncoder.instance: intent =>
      val detailChild = intent.binding match
        case BindingSpecification.AdhesiveNote(details) =>
          details.toVector.map(summon[XmlElementCodec[AdhesiveNoteDetails]].encode)
        case BindingSpecification.ChannelBinding(details) =>
          details.toVector.map(summon[XmlElementCodec[LooseBindingDetails]].encode)
        case BindingSpecification.CoilBinding(details) =>
          details.toVector.map(summon[XmlElementCodec[CoilLooseBindingDetails]].encode)
        case BindingSpecification.CombBinding(details) =>
          details.toVector.map(summon[XmlElementCodec[CombLooseBindingDetails]].encode)
        case BindingSpecification.EdgeGluing(details) =>
          details.toVector.map(summon[XmlElementCodec[EdgeGluingDetails]].encode)
        case BindingSpecification.HardCover(details) =>
          details.toVector.map(summon[XmlElementCodec[HardCoverBindingDetails]].encode)
        case BindingSpecification.LooseBinding(details) =>
          details.toVector.map(summon[XmlElementCodec[LooseBindingDetails]].encode)
        case BindingSpecification.RingBinding(details) =>
          details.toVector.map(summon[XmlElementCodec[RingLooseBindingDetails]].encode)
        case BindingSpecification.SaddleStitch(details) =>
          details.toVector.map(StitchingCodec.encode("SaddleStitching"))
        case BindingSpecification.SideStitch(details) =>
          details.toVector.map(StitchingCodec.encode("SideStitching"))
        case BindingSpecification.SoftCover(details) =>
          details.toVector.map(summon[XmlElementCodec[SoftCoverBindingDetails]].encode)
        case BindingSpecification.StripBinding(details) =>
          details.toVector.map(summon[XmlElementCodec[LooseBindingDetails]].encode)
        case BindingSpecification.WireComb(details) =>
          details.toVector.map(summon[XmlElementCodec[LooseBindingDetails]].encode)
        case _ => Vector.empty
      val attributes =
        CodecHelpers.attributeOf("BackCoverColor", intent.backCoverColor, (v: NamedColor) => v.lexical) ++
          CodecHelpers.attributeOf("BackCoverColorDetails", intent.backCoverColorDetails, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("BindingColor", intent.bindingColor, (v: NamedColor) => v.lexical) ++
          CodecHelpers.attributeOf("BindingColorDetails", intent.bindingColorDetails, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("BindingOrder", intent.bindingOrder, _.toString) ++
          CodecHelpers.attributeOf("BindingSide", intent.bindingSide, _.toString) ++
          CodecHelpers.attribute(
            "ChildRefs",
            intent.childRefs.map(values => CodecHelpers.renderIdRefs(values.toVector)),
          ) ++
          CodecHelpers.attributeOf("CoverColor", intent.coverColor, (v: NamedColor) => v.lexical) ++
          CodecHelpers.attributeOf("CoverColorDetails", intent.coverColorDetails, (v: XjdfString) => v.value) ++
          CodecHelpers.attribute("BindingType", Some(intent.binding.toString)) ++
          CodecHelpers.extensionAttributes(intent.extensions)
      val children =
        detailChild ++ intent.tabs.toVector.map(summon[XmlElementCodec[Tabs]].encode)
      Xml.Element(CodecHelpers.qname("BindingIntent"), attributes, children)
end BindingIntentCodec

/** StitchingDetails appears as `<SaddleStitching>` or `<SideStitching>` depending on the binding case. */
object StitchingCodec:
  val optional: String => XmlDecoder[Option[StitchingDetails]] =
    name =>
      XmlDecoder.instance: element =>
        element.childElements.find(_.name.localName == name) match
          case None => Right(None)
          case Some(child) =>
            for
              stapleShape <- XmlDecoders.attributeOf("StapleShape")(Lexical.enumOf(StapleShape.values.toVector, _.toString))
                .decode(child)
              stitchNumber <- XmlDecoders.attributeOf("StitchNumber")(Lexical.int).decode(child)
              _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(child)
            yield Some(StitchingDetails(stapleShape, stitchNumber, CodecHelpers.decodeExtensionAttributes(child)))

  def encode(name: String)(details: StitchingDetails): Xml.Element =
    val attributes =
      CodecHelpers.attributeOf("StapleShape", details.stapleShape, _.toString) ++
        CodecHelpers.attributeOf("StitchNumber", details.stitchNumber, CodecHelpers.renderInt) ++
        CodecHelpers.extensionAttributes(details.extensions)
    Xml.Element(CodecHelpers.qname(name), attributes, Vector.empty)
end StitchingCodec

// -- ColorIntent: SurfaceColor children distinguished by @Surface ---------------------------------

object ColorIntentCodec:
  private val surface: Lexical.Lex[Side] = Lexical.enumOf(Side.values.toVector, _.toString)

  val decoder: XmlDecoder[ColorIntent] =
    XmlDecoder.instance: element =>
      val surfaceColors = element.childElements.filter(_.name.localName == "SurfaceColor")
      val unexpected = element.childElements.find(_.name.localName != "SurfaceColor")
      for
        _ <- unexpected match
          case Some(child) => Left(XmlError.UnexpectedElement("ColorIntent", child.name.localName))
          case None        => Right(())
        decoded <- surfaceColors.foldLeft[Either[XmlError, Vector[(Side, SurfaceColor)]]](Right(Vector.empty)) {
          (acc, child) =>
            for
              pairs <- acc
              side <- surface(child.attribute("Surface").getOrElse(""))
                .left
                .map(message => XmlError.InvalidAttribute("SurfaceColor", "Surface", "", message))
              color <- summon[XmlElementCodec[SurfaceColor]].decode(child)
            yield pairs :+ (side, color)
        }
        surfaces <- decoded match
          case Vector((Side.Front, front), (Side.Back, back)) => Right(ColorSurfaces.Both(front, back))
          case Vector((Side.Front, front))                    => Right(ColorSurfaces.Front(front))
          case Vector((Side.Back, back))                      => Right(ColorSurfaces.Back(back))
          case Vector()                                       => Right(ColorSurfaces.Unprinted)
          case _ => Left(XmlError.InvalidAttribute("ColorIntent", "SurfaceColor", "", "one front and one back surface"))
      yield ColorIntent(surfaces, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[ColorIntent] =
    XmlEncoder.instance: intent =>
      def surfaceColor(side: Side, value: SurfaceColor): Xml.Element =
        val encoded = summon[XmlElementCodec[SurfaceColor]].encode(value)
        encoded.copy(attributes = encoded.attributes :+ (CodecHelpers.qname("Surface"), side.toString))
      val children = intent.surfaces match
        case ColorSurfaces.Unprinted      => Vector.empty
        case ColorSurfaces.Front(front)   => Vector(surfaceColor(Side.Front, front))
        case ColorSurfaces.Back(back)     => Vector(surfaceColor(Side.Back, back))
        case ColorSurfaces.Both(f, b)     => Vector(surfaceColor(Side.Front, f), surfaceColor(Side.Back, b))
      Xml.Element(
        CodecHelpers.qname("ColorIntent"),
        CodecHelpers.extensionAttributes(intent.extensions),
        children,
      )
end ColorIntentCodec

// -- StickOn: @Face xor @Folio -------------------------------------------------------------------

object StickOnCodec:
  val decoder: XmlDecoder[StickOn] =
    XmlDecoder.instance: element =>
      for
        childRef <- XmlDecoders.requiredAttribute("ChildRef")(Lexical.xsdIdRef).decode(element)
        face <- XmlDecoders.attributeOf("Face")(Lexical.face).decode(element)
        folio <- XmlDecoders.attributeOf("Folio")(Lexical.int).decode(element)
        orientation <- XmlDecoders.attributeOf("Orientation")(Lexical.orientation).decode(element)
        position <- XmlDecoders.attributeOf("Position")(Lexical.xypair).decode(element)
        glue <- XmlDecoders.optionalChild("Glue")(GlueCodec.decoder).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("Glue")).decode(element)
        location <- (face, folio) match
          case (Some(f), None)  => Right(Some(ProductLocation.OnFace(f)))
          case (None, Some(p))  => Right(Some(ProductLocation.OnFolio(p)))
          case (None, None)     => Right(None)
          case _                => Left(XmlError.ConflictingFields("StickOn", "Face/Folio"))
      yield StickOn(childRef, location, orientation, position, glue, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[StickOn] =
    XmlEncoder.instance: stickOn =>
      val locationAttributes = stickOn.location match
        case Some(ProductLocation.OnFace(face)) => CodecHelpers.attribute("Face", Some(face.toString))
        case Some(ProductLocation.OnFolio(folio)) => CodecHelpers.attribute("Folio", Some(CodecHelpers.renderInt(folio)))
        case None                                 => Vector.empty
      val attributes =
        locationAttributes ++
          CodecHelpers.attributeOf("Orientation", stickOn.orientation, _.toString) ++
          CodecHelpers.attributeOf("Position", stickOn.position, CodecHelpers.renderXypair) ++
          CodecHelpers.attribute("ChildRef", Some(stickOn.childRef.value)) ++
          CodecHelpers.extensionAttributes(stickOn.extensions)
      Xml.Element(
        CodecHelpers.qname("StickOn"),
        attributes,
        stickOn.glue.toVector.map(GlueCodec.encoder.encode),
      )
end StickOnCodec

// -- CollatingItem: @Orientation xor @Transformation ----------------------------------------------

object CollatingItemCodec:
  val decoder: XmlDecoder[CollatingItem] =
    XmlDecoder.instance: element =>
      for
        amount <- XmlDecoders.attributeOf("Amount")(Lexical.int).decode(element)
        componentRef <- XmlDecoders.attributeOf("ComponentRef")(Lexical.xsdIdRef).decode(element)
        orientation <- XmlDecoders.attributeOf("Orientation")(Lexical.orientation).decode(element)
        transformation <- XmlDecoders.attributeOf("Transformation")(Lexical.matrix).decode(element)
        transformationContext <- XmlDecoders
          .attributeOf("TransformationContext")(Lexical.enumOf(TransformationContext.values.toVector, _.toString))
          .decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
        placement <- (orientation, transformation) match
          case (Some(o), None) => Right(Some(CollatingPlacement.ByOrientation(o)))
          case (None, Some(t)) => Right(Some(CollatingPlacement.ByTransformation(t)))
          case (None, None)    => Right(None)
          case _ => Left(XmlError.ConflictingFields("CollatingItem", "Orientation/Transformation"))
      yield CollatingItem(amount, componentRef, placement, transformationContext, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[CollatingItem] =
    XmlEncoder.instance: item =>
      val placementAttributes = item.placement match
        case Some(CollatingPlacement.ByOrientation(o)) => CodecHelpers.attribute("Orientation", Some(o.toString))
        case Some(CollatingPlacement.ByTransformation(t)) =>
          CodecHelpers.attribute("Transformation", Some(CodecHelpers.renderMatrix(t)))
        case None => Vector.empty
      val attributes =
        CodecHelpers.attributeOf("Amount", item.amount, CodecHelpers.renderInt) ++
          CodecHelpers.attributeOf("ComponentRef", item.componentRef, (v: XsdIdRef) => v.value) ++
          placementAttributes ++
          CodecHelpers.attributeOf("TransformationContext", item.transformationContext, _.toString) ++
          CodecHelpers.extensionAttributes(item.extensions)
      Xml.Element(CodecHelpers.qname("CollatingItem"), attributes, Vector.empty)
end CollatingItemCodec

// -- LooseBindingParams ----------------------------------------------------------------------------

object LooseBindingParamsCodec:
  private val bindingTypes: Vector[(ProductionLooseBinding, String)] = Vector(
    ProductionLooseBinding.Channel() -> "ChannelBinding",
    ProductionLooseBinding.Coil() -> "CoilBinding",
    ProductionLooseBinding.Comb() -> "CombBinding",
    ProductionLooseBinding.Ring() -> "RingBinding",
    ProductionLooseBinding.Strip() -> "StripBinding",
  )

  private val bindingType: Lexical.Lex[ProductionLooseBinding] =
    value =>
      bindingTypes
        .find(_._2.equalsIgnoreCase(value.trim))
        .map(_._1)
        .toRight(s"'$value' is not a LooseBindingParams BindingType")

  val decoder: XmlDecoder[LooseBindingParams] =
    XmlDecoder.instance: element =>
      for
        binding <- XmlDecoders.requiredAttribute("BindingType")(bindingType).decode(element)
        coverMaterial <- XmlDecoders.attributeOf("CoverMaterial")(Lexical.nmtoken).decode(element)
        holePatterns <- XmlDecoders.repeatedChild("HolePattern")(summon[XmlElementCodec[HolePattern]]).decode(element)
        details <- binding match
          case ProductionLooseBinding.Channel(_) =>
            XmlDecoders.optionalChild("ChannelBindingDetails")(summon[XmlElementCodec[ChannelBindingProductionDetails]])
              .decode(element).map(ProductionLooseBinding.Channel(_))
          case ProductionLooseBinding.Coil(_) =>
            XmlDecoders.optionalChild("CoilBindingDetails")(summon[XmlElementCodec[CoilBindingProductionDetails]])
              .decode(element).map(ProductionLooseBinding.Coil(_))
          case ProductionLooseBinding.Comb(_) =>
            XmlDecoders.optionalChild("CombBindingDetails")(summon[XmlElementCodec[CombBindingProductionDetails]])
              .decode(element).map(ProductionLooseBinding.Comb(_))
          case ProductionLooseBinding.Ring(_) =>
            XmlDecoders.optionalChild("RingBindingDetails")(summon[XmlElementCodec[RingBindingProductionDetails]])
              .decode(element).map(ProductionLooseBinding.Ring(_))
          case ProductionLooseBinding.Strip(_) =>
            XmlDecoders.optionalChild("StripBindingDetails")(summon[XmlElementCodec[StripBindingProductionDetails]])
              .decode(element).map(ProductionLooseBinding.Strip(_))
      yield LooseBindingParams(details, coverMaterial, holePatterns, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[LooseBindingParams] =
    XmlEncoder.instance: params =>
      val detailChild = params.binding match
        case ProductionLooseBinding.Channel(details) =>
          details.toVector.map(summon[XmlElementCodec[ChannelBindingProductionDetails]].encode)
        case ProductionLooseBinding.Coil(details) =>
          details.toVector.map(summon[XmlElementCodec[CoilBindingProductionDetails]].encode)
        case ProductionLooseBinding.Comb(details) =>
          details.toVector.map(summon[XmlElementCodec[CombBindingProductionDetails]].encode)
        case ProductionLooseBinding.Ring(details) =>
          details.toVector.map(summon[XmlElementCodec[RingBindingProductionDetails]].encode)
        case ProductionLooseBinding.Strip(details) =>
          details.toVector.map(summon[XmlElementCodec[StripBindingProductionDetails]].encode)
      val attributes =
        CodecHelpers.attributeOf("CoverMaterial", params.coverMaterial, (v: Nmtoken) => v.value) ++
          CodecHelpers.attribute("BindingType", Some(params.binding.toString)) ++
          CodecHelpers.extensionAttributes(params.extensions)
      val children =
        detailChild ++ params.holePatterns.map(summon[XmlElementCodec[HolePattern]].encode)
      Xml.Element(CodecHelpers.qname("LooseBindingParams"), attributes, children)
end LooseBindingParamsCodec

// -- Assembly: the plan coproduct maps to @BinderySignatureIDs and AssemblySection children -----------

object AssemblyCodec:
  val decoder: XmlDecoder[Assembly] =
    XmlDecoder.instance: element =>
      for
        binderySignatureIds <- XmlDecoders.attributeOf("BinderySignatureIDs")(Lexical.nmtokens).decode(element)
        sections <- XmlDecoders.repeatedChild("AssemblySection")(summon[XmlElementCodec[AssemblySection]]).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("AssemblySection")).decode(element)
        plan <- (binderySignatureIds, sections) match
          case (_, sections) if sections.nonEmpty =>
            NonEmptyVector.from(sections) match
              case Right(nonEmpty) => Right(AssemblyPlan.Listed(nonEmpty))
              case Left(_)         => Left(XmlError.MissingElement("Assembly", "AssemblySection"))
          case (Some(ids), _) if ids.nonEmpty => Right(AssemblyPlan.Collecting(ids))
          case _                              => Right(AssemblyPlan.None)
      yield Assembly(plan, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[Assembly] =
    XmlEncoder.instance: assembly =>
      val (idsAttribute, sectionChildren) = assembly.plan match
        case AssemblyPlan.Collecting(ids) => (CodecHelpers.attribute(
          "BinderySignatureIDs",
          Option.when(ids.nonEmpty)(CodecHelpers.renderNmtokens(ids)),
        ), Vector.empty[Xml.Element])
        case AssemblyPlan.Gathering(ids) => (CodecHelpers.attribute(
          "BinderySignatureIDs",
          Option.when(ids.nonEmpty)(CodecHelpers.renderNmtokens(ids)),
        ), Vector.empty[Xml.Element])
        case AssemblyPlan.Listed(sections) =>
          (Vector.empty, sections.toVector.map(summon[XmlElementCodec[AssemblySection]].encode))
        case AssemblyPlan.None => (Vector.empty, Vector.empty)
      Xml.Element(
        CodecHelpers.qname("Assembly"),
        idsAttribute ++ CodecHelpers.extensionAttributes(assembly.extensions),
        sectionChildren,
      )
end AssemblyCodec

// -- PlacedObject: ContentObject / MarkObject children ----------------------------------------------

object PlacedObjectCodec:
  val decoder: XmlDecoder[PlacedObject] =
    XmlDecoder.instance: element =>
      for
        ctm <- XmlDecoders.requiredAttribute("CTM")(Lexical.matrix).decode(element)
        anchor <- XmlDecoders.attributeOf("Anchor")(Lexical.enumOf(Anchor.values.toVector, _.toString)).decode(element)
        clipBox <- XmlDecoders.attributeOf("ClipBox")(Lexical.rectangle).decode(element)
        clipPath <- XmlDecoders.attributeOf("ClipPath")(Lexical.pdfPath).decode(element)
        halfTonePhaseOrigin <- XmlDecoders.attributeOf("HalfTonePhaseOrigin")(Lexical.xypair).decode(element)
        id <- XmlDecoders.attributeOf("ID")(Lexical.xsdId).decode(element)
        order <- XmlDecoders.attributeOf("Order")(Lexical.int).decode(element)
        positionRef <- XmlDecoders.attributeOf("PositionRef")(Lexical.xsdIdRef).decode(element)
        sourceClipPath <- XmlDecoders.attributeOf("SourceClipPath")(Lexical.pdfPath).decode(element)
        trimCtm <- XmlDecoders.attributeOf("TrimCTM")(Lexical.matrix).decode(element)
        trimSize <- XmlDecoders.attributeOf("TrimSize")(Lexical.xypair).decode(element)
        markObject <- XmlDecoders.optionalChild("MarkObject")(summon[XmlElementCodec[MarkObject]]).decode(element)
        hasContentObject <- XmlDecoders.optionalChild("ContentObject")(ContentObjectCodec.decoder).decode(element)
        pageActivation <- XmlDecoders.optionalChild("PageActivation")(summon[XmlElementCodec[PageActivation]]).decode(element)
        pageCondition <- XmlDecoders.optionalChild("PageCondition")(summon[XmlElementCodec[PageCondition]]).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("MarkObject", "ContentObject", "PageActivation", "PageCondition"))
          .decode(element)
        kind <- (markObject, hasContentObject) match
          case (Some(mark), None) => Right(PlacedObjectKind.Mark(mark))
          case (None, Some(_))    => Right(PlacedObjectKind.Content)
          case (None, None)       => Left(XmlError.MissingElement("PlacedObject", "ContentObject or MarkObject"))
          case _ => Left(XmlError.ConflictingFields("PlacedObject", "ContentObject/MarkObject"))
      yield PlacedObject(
        ctm,
        kind,
        anchor,
        clipBox,
        clipPath,
        halfTonePhaseOrigin,
        id,
        order,
        positionRef,
        sourceClipPath,
        trimCtm,
        trimSize,
        pageActivation,
        pageCondition,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[PlacedObject] =
    XmlEncoder.instance: placed =>
      val kindChildren = placed.kind match
        case PlacedObjectKind.Content     => Vector(Xml.Element(CodecHelpers.qname("ContentObject"), Vector.empty, Vector.empty))
        case PlacedObjectKind.Mark(mark)  => Vector(summon[XmlElementCodec[MarkObject]].encode(mark))
      val attributes =
        CodecHelpers.attributeOf("Anchor", placed.anchor, _.toString) ++
          CodecHelpers.attributeOf("ClipBox", placed.clipBox, CodecHelpers.renderRectangle) ++
          CodecHelpers.attributeOf("ClipPath", placed.clipPath, (v: PdfPath) => v.value) ++
          CodecHelpers.attributeOf("HalfTonePhaseOrigin", placed.halfTonePhaseOrigin, CodecHelpers.renderXypair) ++
          CodecHelpers.attributeOf("ID", placed.id, (v: XsdId) => v.value) ++
          CodecHelpers.attributeOf("Order", placed.order, CodecHelpers.renderInt) ++
          CodecHelpers.attributeOf("PositionRef", placed.positionRef, (v: XsdIdRef) => v.value) ++
          CodecHelpers.attributeOf("SourceClipPath", placed.sourceClipPath, (v: PdfPath) => v.value) ++
          CodecHelpers.attributeOf("TrimCTM", placed.trimCtm, CodecHelpers.renderMatrix) ++
          CodecHelpers.attributeOf("TrimSize", placed.trimSize, CodecHelpers.renderXypair) ++
          CodecHelpers.attribute("CTM", Some(CodecHelpers.renderMatrix(placed.ctm))) ++
          CodecHelpers.extensionAttributes(placed.extensions)
      val children =
        kindChildren ++
          placed.pageActivation.toVector.map(summon[XmlElementCodec[PageActivation]].encode) ++
          placed.pageCondition.toVector.map(summon[XmlElementCodec[PageCondition]].encode)
      Xml.Element(CodecHelpers.qname("PlacedObject"), attributes, children)
end PlacedObjectCodec

object ContentObjectCodec:
  val decoder: XmlDecoder[Unit] =
    XmlDecoder.instance: element =>
      XmlDecoders.expectChildrenOnly(Set.empty).decode(element).map(_ => ())
end ContentObjectCodec

// -- ModifyQueueEntryParams: the Move/SetGang payload maps to attributes ------------------------------

object ModifyQueueEntryParamsCodec:
  private val operations: Vector[(QueueModification, String)] = Vector(
    QueueModification.Abort -> "Abort",
    QueueModification.Complete -> "Complete",
    QueueModification.Hold -> "Hold",
    QueueModification.Remove -> "Remove",
    QueueModification.Resume -> "Resume",
    QueueModification.Suspend -> "Suspend",
    QueueModification.Move() -> "Move",
    QueueModification.SetGang() -> "SetGang",
  )

  private val operation: Lexical.Lex[QueueModification] =
    value =>
      operations
        .find(_._2.equalsIgnoreCase(value.trim))
        .map(_._1)
        .toRight(s"'$value' is not a QueueModification operation")

  val decoder: XmlDecoder[ModifyQueueEntryParams] =
    XmlDecoder.instance: element =>
      for
        op <- XmlDecoders.requiredAttribute("Operation")(operation).decode(element)
        filter <- XmlDecoders.singleChild("Filter")(summon[XmlElementCodec[QueueFilter]]).decode(element)
        afterQueueEntryId <- XmlDecoders.attributeOf("AfterQueueEntryID")(Lexical.nmtoken).decode(element)
        beforeQueueEntryId <- XmlDecoders.attributeOf("BeforeQueueEntryID")(Lexical.nmtoken).decode(element)
        position <- XmlDecoders.attributeOf("Position")(Lexical.int).decode(element)
        priority <- XmlDecoders.attributeOf("Priority")(Lexical.priority).decode(element)
        gangName <- XmlDecoders.attributeOf("GangName")(Lexical.nmtoken).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("Filter")).decode(element)
        modification <- op match
          case _: QueueModification.Move =>
            val target = (afterQueueEntryId, beforeQueueEntryId, position, priority) match
              case (Some(after), _, _, _)   => Some(QueueMoveTarget.After(after))
              case (_, Some(before), _, _)  => Some(QueueMoveTarget.Before(before))
              case (_, _, Some(pos), _)     => Some(QueueMoveTarget.Position(pos))
              case (_, _, _, Some(prio))    => Some(QueueMoveTarget.Priority(prio))
              case _                        => None
            Right(QueueModification.Move(target))
          case _: QueueModification.SetGang => Right(QueueModification.SetGang(gangName))
          case other                        => Right(other)
      yield ModifyQueueEntryParams(modification, filter, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[ModifyQueueEntryParams] =
    XmlEncoder.instance: params =>
      val targetAttributes = params.operation match
        case QueueModification.Move(target) =>
          target match
            case Some(QueueMoveTarget.After(after))   => CodecHelpers.attribute("AfterQueueEntryID", Some(after.value))
            case Some(QueueMoveTarget.Before(before)) => CodecHelpers.attribute("BeforeQueueEntryID", Some(before.value))
            case Some(QueueMoveTarget.Position(pos))  => CodecHelpers.attribute("Position", Some(CodecHelpers.renderInt(pos)))
            case Some(QueueMoveTarget.Priority(prio)) => CodecHelpers.attribute("Priority", Some(prio.value.toString))
            case None                                 => Vector.empty
        case QueueModification.SetGang(gangName) =>
          CodecHelpers.attributeOf("GangName", gangName, (v: Nmtoken) => v.value)
        case _ => Vector.empty
      val attributes =
        targetAttributes ++
          CodecHelpers.attribute("Operation", Some(params.operation.toString)) ++
          CodecHelpers.extensionAttributes(params.extensions)
      Xml.Element(
        CodecHelpers.qname("ModifyQueueEntryParams"),
        attributes,
        Vector(summon[XmlElementCodec[QueueFilter]].encode(params.filter)),
      )
end ModifyQueueEntryParamsCodec

// -- QueueSubmissionParams: position payload maps to attributes ---------------------------------------

object QueueSubmissionParamsCodec:
  val decoder: XmlDecoder[QueueSubmissionParams] =
    XmlDecoder.instance: element =>
      for
        url <- XmlDecoders.requiredAttribute("URL")(Lexical.uri).decode(element)
        activation <- XmlDecoders.attributeOf("Activation")(Lexical.enumOf(QueueActivation.values.toVector, _.toString))
          .decode(element)
        gangName <- XmlDecoders.attributeOf("GangName")(Lexical.nmtoken).decode(element)
        gangPolicy <- XmlDecoders.attributeOf("GangPolicy")(Lexical.enumOf(QueueGangPolicy.values.toVector, _.toString))
          .decode(element)
        afterQueueEntryId <- XmlDecoders.attributeOf("AfterQueueEntryID")(Lexical.nmtoken).decode(element)
        beforeQueueEntryId <- XmlDecoders.attributeOf("BeforeQueueEntryID")(Lexical.nmtoken).decode(element)
        priority <- XmlDecoders.attributeOf("Priority")(Lexical.priority).decode(element)
        returnJmf <- XmlDecoders.attributeOf("ReturnJMF")(Lexical.uri).decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
        position <- (afterQueueEntryId, beforeQueueEntryId, priority) match
          case (Some(after), _, _)  => Right(Some(QueueSubmissionPosition.After(after)))
          case (_, Some(before), _) => Right(Some(QueueSubmissionPosition.Before(before)))
          case (_, _, Some(prio))   => Right(Some(QueueSubmissionPosition.Priority(prio)))
          case _                    => Right(None)
      yield QueueSubmissionParams(url, activation, gangName, gangPolicy, position, returnJmf, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[QueueSubmissionParams] =
    XmlEncoder.instance: params =>
      val positionAttributes = params.position match
        case Some(QueueSubmissionPosition.After(after))   => CodecHelpers.attribute("AfterQueueEntryID", Some(after.value))
        case Some(QueueSubmissionPosition.Before(before)) => CodecHelpers.attribute("BeforeQueueEntryID", Some(before.value))
        case Some(QueueSubmissionPosition.Priority(prio)) => CodecHelpers.attribute("Priority", Some(prio.value.toString))
        case None                                         => Vector.empty
      val attributes =
        CodecHelpers.attributeOf("Activation", params.activation, _.toString) ++
          CodecHelpers.attributeOf("GangName", params.gangName, (v: Nmtoken) => v.value) ++
          CodecHelpers.attributeOf("GangPolicy", params.gangPolicy, _.toString) ++
          positionAttributes ++
          CodecHelpers.attributeOf("ReturnJMF", params.returnJmf, (v: UriRef) => v.value.toString) ++
          CodecHelpers.attribute("URL", Some(params.url.value.toString)) ++
          CodecHelpers.extensionAttributes(params.extensions)
      Xml.Element(CodecHelpers.qname("QueueSubmissionParams"), attributes, Vector.empty)
end QueueSubmissionParamsCodec

// -- SignalStatus: the replacement window flattens to @ReplaceAfter/@ReplaceBefore ----------------------

object SignalStatusCodec:
  val decoder: XmlDecoder[SignalStatus] =
    XmlDecoder.instance: element =>
      for
        header <- XmlDecoders.singleChild("Header")(HeaderCodec.decoder).decode(element)
        deviceInfo <- XmlDecoders.singleChild("DeviceInfo")(summon[XmlElementCodec[DeviceInfo]]).decode(element)
        replaceAfter <- XmlDecoders.attributeOf("ReplaceAfter")(Lexical.dateTime).decode(element)
        replaceBefore <- XmlDecoders.attributeOf("ReplaceBefore")(Lexical.dateTime).decode(element)
        channelMode <- XmlDecoders.attributeOf("ChannelMode")(Lexical.enumOf(ChannelMode.values.toVector, _.toString))
          .decode(element)
        _ <- XmlDecoders.expectChildrenOnly(Set("Header", "DeviceInfo")).decode(element)
        replacement <- (replaceAfter, replaceBefore) match
          case (Some(after), Some(before)) => Right(Some(StatusReplacementWindow(after, before)))
          case (None, None)                => Right(None)
          case _ => Left(XmlError.ConflictingFields("SignalStatus", "ReplaceAfter/ReplaceBefore"))
      yield SignalStatus(header, deviceInfo, replacement, channelMode, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[SignalStatus] =
    XmlEncoder.instance: signal =>
      val windowAttributes = signal.replacement match
        case Some(window) =>
          CodecHelpers.attribute("ReplaceAfter", Some(window.after.value)) ++
            CodecHelpers.attribute("ReplaceBefore", Some(window.before.value))
        case None => Vector.empty
      val attributes =
        windowAttributes ++
          CodecHelpers.attributeOf("ChannelMode", signal.channelMode, _.toString) ++
          CodecHelpers.extensionAttributes(signal.extensions)
      Xml.Element(
        CodecHelpers.qname("SignalStatus"),
        attributes,
        Vector(HeaderCodec.encoder.encode(signal.header), summon[XmlElementCodec[DeviceInfo]].encode(signal.deviceInfo)),
      )
end SignalStatusCodec

// -- Audit family -----------------------------------------------------------------------------------

object AuditCodec:
  private[domain] def decodeOne(element: Xml.Element): Either[XmlError, Audit] =
    def header: Either[XmlError, Header] =
      XmlDecoders.singleChild("Header")(HeaderCodec.decoder).decode(element)

    element.name.localName match
      case "AuditCreated" =>
        for
          h <- header
          _ <- XmlDecoders.expectChildrenOnly(Set("Header")).decode(element)
        yield AuditCreated(h, CodecHelpers.decodeExtensionAttributes(element))
      case "AuditNotification" =>
        for
          h <- header
          notification <- XmlDecoders.singleChild("Notification")(summon[XmlElementCodec[Notification]]).decode(element)
          _ <- XmlDecoders.expectChildrenOnly(Set("Header", "Notification")).decode(element)
        yield AuditNotification(h, notification, CodecHelpers.decodeExtensionAttributes(element))
      case "AuditProcessRun" =>
        for
          h <- header
          processRun <- XmlDecoders.singleChild("ProcessRun")(summon[XmlElementCodec[ProcessRun]]).decode(element)
          _ <- XmlDecoders.expectChildrenOnly(Set("Header", "ProcessRun")).decode(element)
        yield AuditProcessRun(h, processRun, CodecHelpers.decodeExtensionAttributes(element))
      case "AuditResource" =>
        for
          h <- header
          resourceInfo <- XmlDecoders.singleChild("ResourceInfo")(ResourceInfoCodec.decoder).decode(element)
          _ <- XmlDecoders.expectChildrenOnly(Set("Header", "ResourceInfo")).decode(element)
        yield AuditResource(h, resourceInfo, CodecHelpers.decodeExtensionAttributes(element))
      case "AuditStatus" =>
        for
          h <- header
          deviceInfo <- XmlDecoders.singleChild("DeviceInfo")(summon[XmlElementCodec[DeviceInfo]]).decode(element)
          _ <- XmlDecoders.expectChildrenOnly(Set("Header", "DeviceInfo")).decode(element)
        yield AuditStatus(h, deviceInfo, CodecHelpers.decodeExtensionAttributes(element))
      case other => Left(XmlError.UnexpectedElement("AuditPool", other))

  private[domain] def encodeOne(audit: Audit): Xml.Element =
    val (name, header, payload, extensions) = audit match
      case AuditCreated(h, extensions) => ("AuditCreated", h, Vector.empty[Xml.Element], extensions)
      case AuditNotification(h, notification, extensions) =>
        ("AuditNotification", h, Vector(summon[XmlElementCodec[Notification]].encode(notification)), extensions)
      case AuditProcessRun(h, processRun, extensions) =>
        ("AuditProcessRun", h, Vector(summon[XmlElementCodec[ProcessRun]].encode(processRun)), extensions)
      case AuditResource(h, resourceInfo, extensions) =>
        ("AuditResource", h, Vector(ResourceInfoCodec.encoder.encode(resourceInfo)), extensions)
      case AuditStatus(h, deviceInfo, extensions) =>
        ("AuditStatus", h, Vector(summon[XmlElementCodec[DeviceInfo]].encode(deviceInfo)), extensions)
    Xml.Element(
      CodecHelpers.qname(name),
      CodecHelpers.extensionAttributes(extensions),
      HeaderCodec.encoder.encode(header) +: payload,
    )

  /** FieldCodec with a wildcard element name: consumes all standard children of the parent. */
  val field: FieldCodec[Audit] =
    new FieldCodec[Audit]:
      def isElement: Boolean = true
      def elementName: String = ""
      def decodeAttribute(raw: Option[String]): Either[String, Audit] = Left("element, not attribute")
      def renderAttribute(value: Audit): Option[String] = None
      def decodeElements(children: Vector[Xml.Element]): Either[XmlError, Audit] =
        Left(XmlError.UnexpectedElement("AuditPool", children.headOption.map(_.name.localName).getOrElse("")))
      def encodeElements(value: Audit): Vector[Xml.Element] = Vector(encodeOne(value))
  end field

end AuditCodec

given auditVectorField: FieldCodec[Vector[Audit]] =
  new FieldCodec[Vector[Audit]]:
    def isElement: Boolean = true
    def elementName: String = ""
    def decodeAttribute(raw: Option[String]): Either[String, Vector[Audit]] = Left("element, not attribute")
    def renderAttribute(value: Vector[Audit]): Option[String] = None
    def decodeElements(children: Vector[Xml.Element]): Either[XmlError, Vector[Audit]] =
      children.foldLeft[Either[XmlError, Vector[Audit]]](Right(Vector.empty)) { (acc, child) =>
        for
          audits <- acc
          decoded <- AuditCodec.decodeOne(child)
        yield audits :+ decoded
      }
    def encodeElements(value: Vector[Audit]): Vector[Xml.Element] = value.map(AuditCodec.encodeOne)
  end new

given auditField: FieldCodec[Audit] = AuditCodec.field

// -- Named carriers --------------------------------------------------------------------------------

given namedSpecificResourceCodec: XmlElementCodec[NamedSpecificResource] = XmlElementCodec.instance("")(
  element =>
    ForeignQName
      .from(element.name.namespace, element.name.localName, element.name.prefix)
      .left
      .map(_ => XmlError.ForeignNameExpected(element.name.localName))
      .flatMap: name =>
        ForeignCodec.decodeForeignElement(element).map(extension => NamedSpecificResource(name, Extensions(elements = Vector(extension)))),
  named =>
    named.extensions.elements match
      case Vector(single) => ForeignCodec.encodeForeignElement(single)
      case _ =>
        throw new UnsupportedOperationException("NamedSpecificResource without exactly one foreign element cannot be encoded"),
)

given namedProductIntentCodec: XmlElementCodec[NamedProductIntent] = XmlElementCodec.instance("")(
  element =>
    ForeignQName
      .from(element.name.namespace, element.name.localName, element.name.prefix)
      .left
      .map(_ => XmlError.ForeignNameExpected(element.name.localName))
      .flatMap: name =>
        ForeignCodec.decodeForeignElement(element).map(extension =>
          NamedProductIntent(name, Extensions(elements = Vector(extension)))),
  intent =>
    intent.extensions.elements match
      case Vector(single) => ForeignCodec.encodeForeignElement(single)
      case _ =>
        throw new UnsupportedOperationException("NamedProductIntent without exactly one foreign element cannot be encoded"),
)

// -- Intent dispatch -------------------------------------------------------------------------------

object IntentCodec:
  val decoder: XmlDecoder[Intent] =
    XmlDecoder.instance: element =>
      for
        name <- XmlDecoders.requiredAttribute("Name")(Lexical.nmtoken).decode(element)
        descriptiveName <- XmlDecoders.attributeOf("DescriptiveName")(Lexical.xjdfString).decode(element)
        externalId <- XmlDecoders.attributeOf("ExternalID")(Lexical.nmtoken).decode(element)
        child <- element.childElements match
          case Vector()      => Right(None)
          case Vector(single) => Right(Some(single))
          case _ => Left(XmlError.UnexpectedElement("Intent", element.childElements(1).name.localName))
        productIntent <- child match
          case None => Right(None)
          case Some(childElement) =>
            Registry.decodeProductIntent(childElement).map(Some(_))
      yield Intent(name, productIntent, descriptiveName, externalId, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[Intent] =
    XmlEncoder.instance: intent =>
      val attributes =
        CodecHelpers.attributeOf("DescriptiveName", intent.descriptiveName, (v: XjdfString) => v.value) ++
          CodecHelpers.attributeOf("ExternalID", intent.externalId, (v: Nmtoken) => v.value) ++
          CodecHelpers.attribute("Name", Some(intent.name.value)) ++
          CodecHelpers.extensionAttributes(intent.extensions)
      Xml.Element(
        CodecHelpers.qname("Intent"),
        attributes,
        intent.productIntent.toVector.map(Registry.encodeProductIntent),
      )
end IntentCodec

// -- givens for the hand nodes of this file ----------------------------------------------------------

given bindingIntentCodec: XmlElementCodec[BindingIntent] = XmlElementCodec.instance("BindingIntent")(
  BindingIntentCodec.decoder.decode,
  BindingIntentCodec.encoder.encode,
)
given colorIntentCodec: XmlElementCodec[ColorIntent] = XmlElementCodec.instance("ColorIntent")(
  ColorIntentCodec.decoder.decode,
  ColorIntentCodec.encoder.encode,
)
given stickOnCodec: XmlElementCodec[StickOn] = XmlElementCodec.instance("StickOn")(
  StickOnCodec.decoder.decode,
  StickOnCodec.encoder.encode,
)
given stickOnField: FieldCodec[StickOn] = FieldCodec.element(summon[XmlElementCodec[StickOn]])
given collatingItemCodec: XmlElementCodec[CollatingItem] = XmlElementCodec.instance("CollatingItem")(
  CollatingItemCodec.decoder.decode,
  CollatingItemCodec.encoder.encode,
)
given collatingItemField: FieldCodec[CollatingItem] = FieldCodec.element(summon[XmlElementCodec[CollatingItem]])
given looseBindingParamsCodec: XmlElementCodec[LooseBindingParams] = XmlElementCodec.instance("LooseBindingParams")(
  LooseBindingParamsCodec.decoder.decode,
  LooseBindingParamsCodec.encoder.encode,
)
given assemblyCodec: XmlElementCodec[Assembly] = XmlElementCodec.instance("Assembly")(
  AssemblyCodec.decoder.decode,
  AssemblyCodec.encoder.encode,
)
given placedObjectCodec: XmlElementCodec[PlacedObject] = XmlElementCodec.instance("PlacedObject")(
  PlacedObjectCodec.decoder.decode,
  PlacedObjectCodec.encoder.encode,
)
given placedObjectField: FieldCodec[PlacedObject] = FieldCodec.element(summon[XmlElementCodec[PlacedObject]])
given modifyQueueEntryParamsCodec: XmlElementCodec[ModifyQueueEntryParams] =
  XmlElementCodec.instance("ModifyQueueEntryParams")(
    ModifyQueueEntryParamsCodec.decoder.decode,
    ModifyQueueEntryParamsCodec.encoder.encode,
  )
given modifyQueueEntryParamsField: FieldCodec[ModifyQueueEntryParams] =
  FieldCodec.element(summon[XmlElementCodec[ModifyQueueEntryParams]])
given queueSubmissionParamsCodec: XmlElementCodec[QueueSubmissionParams] =
  XmlElementCodec.instance("QueueSubmissionParams")(
    QueueSubmissionParamsCodec.decoder.decode,
    QueueSubmissionParamsCodec.encoder.encode,
  )
given queueSubmissionParamsField: FieldCodec[QueueSubmissionParams] =
  FieldCodec.element(summon[XmlElementCodec[QueueSubmissionParams]])
given signalStatusCodec: XmlElementCodec[SignalStatus] = XmlElementCodec.instance("SignalStatus")(
  SignalStatusCodec.decoder.decode,
  SignalStatusCodec.encoder.encode,
)
given intentCodec: XmlElementCodec[Intent] = XmlElementCodec.instance("Intent")(
  IntentCodec.decoder.decode,
  IntentCodec.encoder.encode,
)
given intentField: FieldCodec[Intent] = FieldCodec.element(summon[XmlElementCodec[Intent]])
