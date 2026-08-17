package xjdf4s.codec.json

/** Discovered munit suite that forces all eager checks of the codec-json module. */
class JsonChecksSuite extends munit.FunSuite:
  test("all json checks pass") {
    val results: Vector[Unit] = Vector(
      JsonChecks.scalars,
      JsonChecks.mediaWithLayers,
      JsonChecks.resourceTree,
      JsonChecks.transferFunctionPoints,
      JsonChecks.commentTextMember,
      JsonChecks.auditPool,
      JsonChecks.messages,
      JsonExceptionChecks.rootSchemaMember,
      JsonExceptionChecks.xjmfExactlyOneMessage,
      JsonExceptionChecks.mediaLayersInlineName,
      NormativeJsonFixtureChecks.example31Root,
      NormativeJsonFixtureChecks.example85MediaLayers,
      NormativeJsonFixtureChecks.example911AuditPool,
      NormativeJsonFixtureChecks.example71Xjmf,
      CrossCodecPropertyChecks.xjdfDocuments,
      CrossCodecPropertyChecks.xjmfMessages,
    )
    assert(results.size == 16)
  }
end JsonChecksSuite
