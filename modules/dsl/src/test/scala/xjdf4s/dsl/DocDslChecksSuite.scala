package xjdf4s.dsl

/** Discovered munit suite that forces all eager checks of the dsl module. */
class DocDslChecksSuite extends munit.FunSuite:
  test("all dsl checks pass") {
    val results: Vector[Unit] = Vector(
      DocDslChecks.buildInterpreter,
      DocDslChecks.traceInterpreter,
      DocDslChecks.dryRunValidation,
      DocDslChecks.oneProgramThreeInterpreters,
    )
    assert(results.size == 4)
  }
end DocDslChecksSuite
