package xjdf4s.examples

import cats.data.{Validated, ValidatedNec}
import munit.FunSuite
import xjdf4s.model.Issue

/** Smoke test that actually *executes* the specification examples, so the CI
 *  gate cannot silently pass with "No tests to run" while the suites are
 *  unexercised (ROADMAP M1.0-3).
 *
 *  Scope note (PR-1): the notebook BOM is **not** unfolded here. `Bom.fromProductList`
 *  reports a false cycle for any tree with `@ChildRefs` (N-01); the failing
 *  regression test is added together with the fix in PR-2 (M1.1-1).
 */
class SpecExamplesSuite extends FunSuite:

  private def assertConstructs[A](label: String)(v: ValidatedNec[Issue, A]): Unit =
    v match
      case Validated.Valid(_) => ()
      case Validated.Invalid(es) =>
        fail(s"$label failed to construct: ${es.toChain.toList.map(_.message).mkString("; ")}")

  test("Example 3.1: minimalProduct constructs and validates"):
    val t = SpecExamples.minimalProduct
    assertConstructs("Example 3.1")(t)
    t.toOption.foreach(x => assert(x.validate.isValid, "Example 3.1 validates"))

  test("Example 3.4: notebook BOM constructs (unfolding deferred to PR-2 / M1.1-1)"):
    assertConstructs("Example 3.4 notebook")(SpecExamples.notebook)

  test("Example 3.6: combinedProcesses constructs and validates"):
    val t = SpecExamples.combinedProcesses
    assertConstructs("Example 3.6")(t)
    t.toOption.foreach(x => assert(x.validate.isValid, "Example 3.6 validates"))

  test("Example 5.2: splitDelivery constructs and validates"):
    val t = SpecExamples.splitDelivery
    assertConstructs("Example 5.2")(t)
    t.toOption.foreach(x => assert(x.validate.isValid, "Example 5.2 validates"))

  test("Example 3.3: mediaConsumptionAudit constructs"):
    assertConstructs("Example 3.3")(SpecExamples.mediaConsumptionAudit)

  test("brochureJob constructs and validates"):
    val t = SpecExamples.brochureJob
    assertConstructs("brochureJob")(t)
    t.toOption.foreach(x => assert(x.validate.isValid, "brochureJob validates"))
