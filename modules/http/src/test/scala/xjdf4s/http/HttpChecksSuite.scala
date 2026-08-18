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
    )
    assert(results.size == 5)
  }
end HttpChecksSuite
