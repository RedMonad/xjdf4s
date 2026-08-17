package xjdf4s.codec.xml

import xjdf4s.codec.xml.domain.*
import xjdf4s.model.generators.XjdfGenerators
import xjdf4s.messaging.generators.MessageGenerators

/**
 * Property-based proof of the codec, validated AGAINST THE XML SCHEMA:
 *
 * for every randomly generated document:
 *   1. the encoder produces XML that OUR parser reads back to an equal value (round-trip law);
 *   2. the emitted XML is valid against the checked-in `xjdf.xsd` (JAXP validator).
 *
 * The generators are deliberately XSD-safe: fields where the normative tables supersede the stale XSD are left
 * unset (see `XjdfGenerators`), so a failure here is a codec defect, not a known normative divergence. Runs are
 * deterministic (fixed seeds); the full normative surface is covered by the plain round-trip suites.
 */
object SchemaRoundTripPropertyChecks:

  private val iterations = 100

  val xjdfDocuments: Unit =
    val generator = new XjdfGenerators(20260817L)
    var index = 1
    while index <= iterations do
      val document = generator.xjdf()
      val xml = XmlWriter.write(XjdfCodec.encoder.encode(document))
      val reparsed = XmlParser
        .parse(xml)
        .flatMap(XjdfCodec.decoder.decode)
        .fold(error => throw new AssertionError(s"iteration $index: ${error}"), identity)
      assert(reparsed == document, s"iteration $index: round-trip mismatch")
      XsdValidator.validate(xml) match
        case Left(problem) =>
          throw new AssertionError(s"iteration $index: not schema-valid: $problem\n$xml")
        case Right(_) => ()
      index += 1

  val xjmfMessages: Unit =
    val generator = new MessageGenerators(20260817L)
    var index = 1
    while index <= iterations do
      val message = generator.xjmf()
      val xml = XmlWriter.write(XjmfCodec.encoder.encode(message))
      val reparsed = XmlParser
        .parse(xml)
        .flatMap(XjmfCodec.decoder.decode)
        .fold(error => throw new AssertionError(s"iteration $index: ${error}"), identity)
      assert(reparsed == message, s"iteration $index: round-trip mismatch")
      XsdValidator.validate(xml) match
        case Left(problem) =>
          throw new AssertionError(s"iteration $index: not schema-valid: $problem\n$xml")
        case Right(_) => ()
      index += 1
end SchemaRoundTripPropertyChecks
