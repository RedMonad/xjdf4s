package xjdf4s.model

import xjdf4s.core.*

enum Coating derives CanEqual:
  case Coated, Gloss, Matte, None, Satin
end Coating

enum IsoPaperSubstrate derives CanEqual:
  case PS1, PS2, PS3, PS4, PS5, PS6, PS7, PS8
  case LWCPlus, LWCStandard, NewsPlus, SCPlus, SCStandard, SNP
end IsoPaperSubstrate

enum MediaDirection derives CanEqual:
  case Any, SameDirection, XDirection, YDirection
end MediaDirection

enum MediaType derives CanEqual:
  case Blanket, CorrugatedBoard, Disc, EmbossingFoil, Film, Foil, GravureCylinder, ImagingCylinder
  case LaminatingFoil, MountingTape, Other, Paper, Plate, Screen, SelfAdhesive, ShrinkFoil, Sleeve
  case Textile, Transparency, Vinyl
end MediaType

enum Opacity derives CanEqual:
  case Opaque, Translucent, Transparent
end Opacity

final case class LabColor(lightness: Double, a: Double, b: Double) derives CanEqual

final case class MediaIntent(
    mediaType: MediaType,
    backCoating: Option[Coating] = None,
    backIsoPaperSubstrate: Option[IsoPaperSubstrate] = None,
    brand: Option[String] = None,
    buyerSupplied: Option[Boolean] = None,
    coating: Option[Coating] = None,
    flute: Option[Nmtoken] = None,
    fluteDirection: Option[MediaDirection] = None,
    grainDirection: Option[MediaDirection] = None,
    isoPaperSubstrate: Option[IsoPaperSubstrate] = None,
    labColorValue: Option[LabColor] = None,
    mediaColor: Option[String] = None,
    mediaColorDetails: Option[String] = None,
    mediaQuality: Option[String] = None,
    mediaTypeDetails: Option[Nmtoken] = None,
    opacity: Option[Opacity] = None,
    prePrinted: Option[Boolean] = None,
    stockType: Option[Nmtoken] = None,
    texture: Option[Nmtoken] = None,
    thickness: Option[Float] = None,
    weight: Option[Float] = None,
    certifications: Vector[Certification] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends ProductIntent:
  val elementName: QualifiedName = XjdfNames.element("MediaIntent")
