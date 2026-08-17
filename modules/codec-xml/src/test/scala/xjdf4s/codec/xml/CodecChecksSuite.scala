package xjdf4s.codec.xml

/** Discovered munit suite that forces all eager checks of the codec-xml module. */
class CodecChecksSuite extends munit.FunSuite:
  test("all codec checks pass") {
    val results: Vector[Unit] = Vector(
      XmlParserChecks.basicElement,
      XmlParserChecks.nestedContent,
      XmlParserChecks.singleQuotes,
      XmlParserChecks.entityDecoding,
      XmlParserChecks.cdataAndComments,
      XmlParserChecks.prologAndPrologSkipping,
      XmlParserChecks.namespaceScoping,
      XmlParserChecks.parseErrors,
      XmlWriterChecks.escaping,
      XmlWriterChecks.selfClosing,
      XmlWriterChecks.rootNamespaceDeclaration,
      XmlWriterChecks.writerRoundTrip,
      XmlWriterChecks.prefixRendering,
      LexicalChecks.numbers,
      LexicalChecks.lists,
      LexicalChecks.fixedProducts,
      LexicalChecks.hexBinary,
      LexicalChecks.enums,
      NormativeFixtureChecks.example75QueryResource,
      NormativeFixtureChecks.example85MediaLayers,
      NormativeFixtureChecks.example78SignalResource,
      RoundTripChecks.comment,
      RoundTripChecks.generalId,
      RoundTripChecks.part,
      RoundTripChecks.mediaWithLayers,
      RoundTripChecks.resourceSet,
      RoundTripChecks.xjdfDocument,
      RoundTripChecks.colorAndToolAndComponent,
      RoundTripChecks.device,
      RoundTripChecks.messages,
      ReferenceAndWildcardChecks.danglingReference,
      ReferenceAndWildcardChecks.validReferences,
      ReferenceAndWildcardChecks.foreignElementRoundTrip,
      ReferenceAndWildcardChecks.foreignSpecificResource,
      ReferenceAndWildcardChecks.unsupportedStandardElement,
    )
    assert(results.size == 35)
  }
end CodecChecksSuite
