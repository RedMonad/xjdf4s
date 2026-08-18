package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum TransferFunctionHandling derives CanEqual:
  case Preserve, Remove, Apply

enum UnderColorHandling derives CanEqual:
  case Preserve, Remove

final case class AdvancedPdfParams(
    allowPsxObjects: Option[Boolean] = None,
    allowTransparency: Option[Boolean] = None,
    autoPositionEpsInfo: Option[Boolean] = None,
    embedJobOptions: Option[Boolean] = None,
    emitDscWarnings: Option[Boolean] = None,
    lockDistillerParams: Option[Boolean] = None,
    parseDscCommentForDocInfo: Option[Boolean] = None,
    parseDscComments: Option[Boolean] = None,
    passThroughJpegImages: Option[Boolean] = None,
    preserveCopyPage: Option[Boolean] = None,
    preserveEpsInfo: Option[Boolean] = None,
    preserveHalftoneInfo: Option[Boolean] = None,
    preserveOpiComments: Option[Boolean] = None,
    preserveOverprintSettings: Option[Boolean] = None,
    transferFunctionInfo: Option[TransferFunctionHandling] = None,
    underColorAndBlackGenerationInfo: Option[UnderColorHandling] = None,
    usePrologue: Option[Boolean] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class FontParams(
    alwaysEmbed: Vector[Nmtoken] = Vector.empty,
    embedAllFonts: Option[Boolean] = None,
    maxSubsetPercent: Option[Int] = None,
    neverEmbed: Vector[Nmtoken] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum PdfXTrapped derives CanEqual:
  case Unknown, False, True

final case class PdfXParams(
    bleedBoxToTrimBoxOffset: Option[Rectangle] = None,
    checks: Vector[Nmtoken] = Vector.empty,
    compliantPdfOnly: Option[Boolean] = None,
    noTrimBoxError: Option[Boolean] = None,
    setBleedBoxToMediaBox: Option[Boolean] = None,
    trapped: Option[PdfXTrapped] = None,
    trimBoxToMediaBoxOffset: Option[Rectangle] = None,
    referenceOutputProfile: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum PdfAutoRotatePages derives CanEqual:
  case None, All, PageByPage

enum PdfBinding derives CanEqual:
  case Left, Right

final case class ThinPdfParams(
    filePerPage: Option[Boolean] = None,
    sidelineEps: Option[Boolean] = None,
    sidelineFonts: Option[Boolean] = None,
    sidelineImages: Option[Boolean] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class PdfCreationDetails(
    allowJbig2Globals: Option[Boolean] = None,
    ascii85EncodePages: Option[Boolean] = None,
    autoRotatePages: Option[PdfAutoRotatePages] = None,
    binding: Option[PdfBinding] = None,
    compressPages: Option[Boolean] = None,
    defaultRenderingIntent: Option[RenderingIntent] = None,
    detectBlend: Option[Boolean] = None,
    doThumbnails: Option[Boolean] = None,
    initialPageSize: Option[XYPair] = None,
    initialResolution: Option[XYPair] = None,
    optimize: Option[Boolean] = None,
    overPrintMode: Option[Int] = None,
    pdfVersion: Option[Nmtoken] = None,
    advancedParams: Option[AdvancedPdfParams] = None,
    pdfXParams: Option[PdfXParams] = None,
    thinPdfParams: Option[ThinPdfParams] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

enum IncludeResources derives CanEqual:
  case IncludeNever, IncludeOncePerDoc, IncludeOncePerPage

enum PostScriptOutputType derives CanEqual:
  case EPS, PostScript

final case class PostScriptCreationDetails(
    binaryOk: Option[Boolean] = None,
    boundingBox: Option[Float] = None,
    centerCropBox: Option[Boolean] = None,
    generatePageStreams: Option[Boolean] = None,
    ignoreAnnotationForms: Option[Boolean] = None,
    ignoreBlackGeneration: Option[Boolean] = None,
    ignoreColorSeparations: Option[Boolean] = None,
    ignoreDsc: Option[Boolean] = None,
    ignoreExternalStreamReferences: Option[Boolean] = None,
    ignoreHalftones: Option[Boolean] = None,
    ignoreOverprint: Option[Boolean] = None,
    ignorePageRotation: Option[Boolean] = None,
    ignoreRawData: Option[Boolean] = None,
    ignoreSeparableImagesOnly: Option[Boolean] = None,
    ignoreShowPage: Option[Boolean] = None,
    ignoreTransfers: Option[Boolean] = None,
    ignoreTrueTypeFontsFirst: Option[Boolean] = None,
    ignoreUnderColorRemoval: Option[Boolean] = None,
    includeBaseFonts: Option[IncludeResources] = None,
    includeCidFonts: Option[IncludeResources] = None,
    includeEmbeddedFonts: Option[IncludeResources] = None,
    includeOtherResources: Option[IncludeResources] = None,
    includeProcSets: Option[IncludeResources] = None,
    includeTrueTypeFonts: Option[IncludeResources] = None,
    includeType1Fonts: Option[IncludeResources] = None,
    includeType3FontsOncePerPage: Option[Boolean] = None,
    outputType: Option[PostScriptOutputType] = None,
    postScriptLevel: Option[Int] = None,
    scale: Option[Float] = None,
    setPageSize: Option[Boolean] = None,
    setupProcSets: Option[Boolean] = None,
    shrinkToFit: Option[Boolean] = None,
    suppressCenter: Option[Boolean] = None,
    suppressRotate: Option[Boolean] = None,
    trueTypeAsType42: Option[Boolean] = None,
    useFontAliasNames: Option[Boolean] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class PDLCreationParams(
    mimeType: XjdfString,
    fontParams: Option[FontParams] = None,
    pdfCreationDetails: Option[PdfCreationDetails] = None,
    postScriptCreationDetails: Option[PostScriptCreationDetails] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("PDLCreationParams")
