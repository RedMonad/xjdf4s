package xjdf4s

/** Discovered munit suite that forces all eager checks of the protocol module (MD-08). */
class ProtocolChecksSuite extends munit.FunSuite:
  test("all protocol checks pass") {
    val results: Vector[Unit] = Vector(ProtocolChecks.xjdfCardinality)
    assert(results.size == 1)
  }
