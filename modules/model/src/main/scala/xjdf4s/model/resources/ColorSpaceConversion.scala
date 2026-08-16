package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum IccProfileUsage derives CanEqual:
  case UsePDL, UseSupplied
end IccProfileUsage

enum ColorConversionOperation derives CanEqual:
  case Convert, Tag, Untag
end ColorConversionOperation

enum RenderingIntent derives CanEqual:
  case AbsoluteColorimetric, ColorSpaceDependent, Perceptual, RelativeColorimetric, Saturation
end RenderingIntent

enum SourceColorSpace derives CanEqual:
  case All, CalGray, Calibrated, CalRGB, CIEBased, CMYK, DeviceCMYK, DeviceGray, DeviceN, DeviceRGB
  case DevIndep, Gray, ICCBased, ICCCMYK, ICCGray, ICCLAB, ICCRGB, Lab, RGB, Separation, YUV
end SourceColorSpace

final case class ColorConversionProfiles(
    deviceLink: Option[FileSpec] = None,
    pdlSource: Option[FileSpec] = None,
    source: Option[FileSpec] = None,
)

final case class ColorSpaceConversionOperation(
    operation: ColorConversionOperation,
    blackPointCompensation: Option[Boolean] = None,
    blackPointCompensationDetails: Option[Nmtoken] = None,
    preserveBlack: Option[Boolean] = None,
    renderingIntent: Option[RenderingIntent] = None,
    rgbGrayToBlack: Option[Boolean] = None,
    rgbGrayToBlackThreshold: Option[Float] = None,
    separations: Vector[Nmtoken] = Vector.empty,
    sourceColorSpace: Option[SourceColorSpace] = None,
    sourceObjects: Vector[SourceObject] = Vector.empty,
    sourceRenderingIntent: Option[RenderingIntent] = None,
    profiles: ColorConversionProfiles = ColorConversionProfiles(),
    screenSelector: Option[ScreenSelector] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class ColorSpaceConversionParams(
    iccProfileUsage: Option[IccProfileUsage] = None,
    operations: Vector[ColorSpaceConversionOperation] = Vector.empty,
    profile: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ColorSpaceConversionParams")
