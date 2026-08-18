package xjdf4s.codec.xml.derivation

/** Central naming policy of the derived codecs.
 *
 *  - `attributeName` maps a case-class field name to the normative attribute name (PascalCase with the acronyms the
 *    XJDF specification spells differently, e.g. `jobId` -> `JobID`, `xjmfUrl` -> `XJMFURL`, `cieLab` -> `CIELab`).
 *  - `elementName` maps a case-class name to the element name (`PdfCreationDetails` -> `PDFCreationDetails`,
 *    `TiffFormatParams` -> `TIFFFormatParams`, `CieLabMeasuringField` -> `CIELabMeasuringField`).
 *
 *  Per-type overrides belong here, not scattered through the codecs, so a normative naming correction is a
 *  one-line change.
 */
object Names:
  private val Acronyms: Map[String, String] = Map(
    "id" -> "ID",
    "ids" -> "ID",
    "url" -> "URL",
    "urls" -> "URL",
    "uri" -> "URI",
    "cie" -> "CIE",
    "iso" -> "ISO",
    "jdf" -> "JDF",
    "jmf" -> "JMF",
    "xjmf" -> "XJMF",
    "ics" -> "ICS",
    "cmyk" -> "CMYK",
    "srgb" -> "sRGB",
    "rgb" -> "RGB",
    "pdl" -> "PDL",
    "pdf" -> "PDF",
    "mime" -> "MIME",
    "isbn" -> "ISBN",
    "mis" -> "MIS",
    "ddes" -> "DDES",
    "ctm" -> "CTM",
    "cip3" -> "CIP3",
    "tiff" -> "TIFF",
  )

  private val AttributeOverrides: Map[String, String] = Map(
    "refId" -> "refID",
  )

  def attributeName(fieldName: String): String =
    AttributeOverrides.getOrElse(fieldName, pascal(fieldName))

  def elementName(className: String): String = pascal(className)

  private def pascal(name: String): String =
    splitWords(name).map(word => Acronyms.getOrElse(word, word.capitalize)).mkString

  private def splitWords(name: String): Vector[String] =
    name
      .replaceAll("([a-z0-9])([A-Z])", "$1 $2")
      .split("\\s+")
      .toVector
      .filter(_.nonEmpty)
end Names
