package xjdf4s.http

/** Discovered munit suite that forces all eager checks of the http module. */
class HttpChecksSuite extends munit.FunSuite:
  test("all http checks pass") {
    val results: Vector[Unit] = Vector(
      XjdfEntityChecks.xmlRoundTrip,
      XjdfEntityChecks.jsonRoundTrip,
      XjdfEntityChecks.xjmfRoundTrip,
      XjdfEntityChecks.rejectsWrongMimeType,
      XjdfEntityChecks.messageEntityRoundTrip,
      XjdfServerChecks.endToEndDemo,
      XjdfServerChecks.streamRoute,
      XjdfServerChecks.stopChannel,
      XjdfServerChecks.bodyLimit,
      XjdfIoChecks.transportTimeout,
      XjdfIoChecks.awaitCancellation,
      XjdfIoChecks.documentInterpreter,
    )
    assert(results.size == 12)
  }
end HttpChecksSuite
