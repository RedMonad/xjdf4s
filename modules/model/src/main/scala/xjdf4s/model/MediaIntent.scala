package xjdf4s.model

import xjdf4s.core.*

enum Coating derives CanEqual:
  case Coated, Gloss, Matte, None, Satin

enum IsoPaperSubstrate derives CanEqual:
  case PS1, PS2, PS3, PS4, PS5, PS6, PS7, PS8, PS9
  case LWCPlus, LWCStandard, NewsPlus, SCPlus, SCStandard, SNP

enum MediaDirection derives CanEqual:
  case Any, SameDirection, XDirection, YDirection

enum MediaType derives CanEqual:
  case Blanket, CorrugatedBoard, Disc, EmbossingFoil, Film, Foil, GravureCylinder, ImagingCylinder
  case LaminatingFoil, MountingTape, Other, Paper, Plate, Screen, SelfAdhesive, ShrinkFoil, Sleeve
  case Synthetic, Textile, Transparency

  /** Deprecated in XJDF 2.1: use `Synthetic` with `MediaTypeDetails = "Vinyl"`. */
  @deprecated("Deprecated in XJDF 2.1: use MediaType.Synthetic with MediaTypeDetails = \"Vinyl\"", "XJDF 2.1")
  case Vinyl
end MediaType

enum Opacity derives CanEqual:
  case Opaque, Translucent, Transparent

final case class MediaIntent(
    mediaType: MediaType,
    backCoating: Option[Coating] = None,
    backIsoPaperSubstrate: Option[IsoPaperSubstrate] = None,
    brand: Option[XjdfString] = None,
    buyerSupplied: Option[Boolean] = None,
    coating: Option[Coating] = None,
    flute: Option[Nmtoken] = None,
    fluteDirection: Option[MediaDirection] = None,
    grainDirection: Option[MediaDirection] = None,
    isoPaperSubstrate: Option[IsoPaperSubstrate] = None,
    labColorValue: Option[LabColor] = None,
    mediaColor: Option[NamedColor] = None,
    mediaColorDetails: Option[XjdfString] = None,
    mediaQuality: Option[XjdfString] = None,
    mediaTypeDetails: Option[Nmtoken] = None,
    opacity: Option[Opacity] = None,
    prePrinted: Option[Boolean] = None,
    stockType: Option[Nmtoken] = None,
    texture: Option[Nmtoken] = None,
    thickness: Option[Float] = None,
    weight: Option[Float] = None,
    certifications: Vector[Certification] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends ProductIntent,
      ValidatedNode:
  val elementName: QualifiedName = XjdfNames.element("MediaIntent")

  override def validate: Vector[ValidationError] =
    backIsoPaperSubstrate match
      case Some(_) if isoPaperSubstrate.isEmpty =>
        Vector(ValidationError.MissingCompanionValue(
          "MediaIntent/@BackISOPaperSubstrate",
          "MediaIntent/@ISOPaperSubstrate"
        ))
      case _ => Vector.empty
end MediaIntent
