package xjdf4s.http

import org.http4s.MediaType

/** Table 9.1: MIME types and file extensions for unpackaged and zip-packaged XJDF/XJMF. The XML variants carry
 *  an explicit UTF-8 charset, matching the XML writer's declaration (in http4s 0.23 a charset is a media-type
 *  extension, so the parameterized form is parsed).
 */
object XjdfMediaTypes:

  val xjdfXml: MediaType = MediaType.unsafeParse("application/vnd.cip4-xjdf+xml")
  val xjmfXml: MediaType = MediaType.unsafeParse("application/vnd.cip4-xjmf+xml")
  val xjdfJson: MediaType = MediaType.unsafeParse("application/vnd.cip4-xjdf+json")
  val xjmfJson: MediaType = MediaType.unsafeParse("application/vnd.cip4-xjmf+json")
  val xjdfZip: MediaType = MediaType.unsafeParse("application/vnd.cip4-xjdf+zip")
  val xjmfZip: MediaType = MediaType.unsafeParse("application/vnd.cip4-xjmf+zip")

  val xjdfXmlUtf8: MediaType = MediaType.unsafeParse("application/vnd.cip4-xjdf+xml; charset=utf-8")
  val xjmfXmlUtf8: MediaType = MediaType.unsafeParse("application/vnd.cip4-xjmf+xml; charset=utf-8")
end XjdfMediaTypes
