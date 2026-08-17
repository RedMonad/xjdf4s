package xjdf4s.codec.json

import io.circe.parser.*
import io.circe.syntax.*

import xjdf4s.codec.json.given
import xjdf4s.codec.xml.*
import xjdf4s.codec.xml.domain.*
import xjdf4s.model.generators.XjdfGenerators
import xjdf4s.messaging.generators.MessageGenerators

/**
 * The JSON analog of the XML schema proof: the cross-codec law. For every generated document both transport paths
 * (XML codec and JSON codec) must decode back to the same domain value. XML additionally stays under the
 * `xjdf.xsd` property proof of the XML module.
 */
object CrossCodecPropertyChecks:

  private val iterations = 100

  private def viaXml[A](
      encode: A => xjdf4s.codec.xml.Xml.Element,
      decode: xjdf4s.codec.xml.Xml.Element => Either[xjdf4s.codec.xml.XmlError, A],
  )(value: A): A =
    val xml = encode(value)
    val reparsed = XmlParser.parse(XmlWriter.write(xml)).toOption.get
    decode(reparsed) match
      case Right(decoded) => decoded
      case Left(error)    => throw new AssertionError(s"XML decode failed: $error")

  val xjdfDocuments: Unit =
    val generator = new XjdfGenerators(20260817L)
    var index = 1
    while index <= iterations do
      val document = generator.xjdf()
      val xmlDecoded = viaXml(XjdfCodec.encoder.encode, XjdfCodec.decoder.decode)(document)
      val jsonText = document.asJson.noSpaces
      val jsonDecoded = parse(jsonText).flatMap(_.as[xjdf4s.model.XJDF]).fold(error => throw new AssertionError(error), identity)
      assert(xmlDecoded == document, s"iteration $index: XML round-trip mismatch")
      assert(jsonDecoded == document, s"iteration $index: JSON round-trip mismatch\n$jsonText")
      assert(xmlDecoded == jsonDecoded, s"iteration $index: XML and JSON disagree")
      index += 1

  val xjmfMessages: Unit =
    val generator = new MessageGenerators(20260817L)
    var index = 1
    while index <= iterations do
      val message = generator.xjmf()
      val xmlDecoded = viaXml(XjmfCodec.encoder.encode, XjmfCodec.decoder.decode)(message)
      val jsonText = message.asJson.noSpaces
      val jsonDecoded = parse(jsonText).flatMap(_.as[xjdf4s.messaging.XJMF]).fold(error => throw new AssertionError(error), identity)
      assert(xmlDecoded == message, s"iteration $index: XML round-trip mismatch")
      assert(jsonDecoded == message, s"iteration $index: JSON round-trip mismatch\n$jsonText")
      assert(xmlDecoded == jsonDecoded, s"iteration $index: XML and JSON disagree")
      index += 1
end CrossCodecPropertyChecks
