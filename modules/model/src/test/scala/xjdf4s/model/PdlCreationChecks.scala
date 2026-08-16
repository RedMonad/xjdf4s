package xjdf4s.model

import xjdf4s.model.resources.*

object PdlCreationChecks:
  val pdfOutput: Unit =
    val details = PdfCreationDetails(
      autoRotatePages = Some(PdfAutoRotatePages.PageByPage),
      pdfXParams = Some(PdfXParams(trapped = Some(PdfXTrapped.True))),
    )
    val resource: PrepressSpecificResource = PDLCreationParams(
      mimeType = "application/pdf",
      pdfCreationDetails = Some(details),
    )
    assert(resource.elementName.localName == "PDLCreationParams")

  val postScriptOutput: Unit =
    val details = PostScriptCreationDetails(
      outputType = Some(PostScriptOutputType.PostScript),
      includeTrueTypeFonts = Some(IncludeResources.IncludeOncePerDoc),
    )
    val resource: TypedSpecificResource = PDLCreationParams(
      mimeType = "application/postscript",
      postScriptCreationDetails = Some(details),
    )
    assert(resource.elementName.localName == "PDLCreationParams")
end PdlCreationChecks
