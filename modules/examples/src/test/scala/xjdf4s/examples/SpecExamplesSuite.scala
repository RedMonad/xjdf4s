package xjdf4s.examples

import cats.data.{Validated, ValidatedNec}
import munit.FunSuite
import xjdf4s.model.{Bom, Issue, validate}

/** Smoke test that actually *executes* the specification examples, so the CI
 *  gate cannot silently pass with "No tests to run" while the suites are
 *  unexercised (ROADMAP M1.0-3).
 *
 *  Since PR-2 (M1.1-1) the notebook BOM unfolds correctly — the `Example 3.4`
 *  test asserts the unfold and `Bom.totalCopies`, which is the N-01 regression.
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

  test("Example 3.4: notebook BOM unfolds and totalCopies is computed (N-01)"):
    val pl = SpecExamples.notebook
    assertConstructs("Example 3.4 notebook")(pl)
    pl.toOption.foreach { list =>
      Bom.fromProductList(list) match
        case Left(issue) => fail(s"notebook BOM failed to unfold: ${issue.message}")
        case Right(forest) =>
          assertEquals(forest.toChain.toList.size, 1)
          val copies = Bom.totalCopies(forest.head)
          assertEquals(copies.size, 4)
          assertEquals(copies.head._2, 10L) // the notebook root: 10 copies
    }

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

  test("ADR-0001: updatedBrochureJob applies a nominal ChangeOrder and revalidates"):
    val t = SpecExamples.updatedBrochureJob
    assertConstructs("updatedBrochureJob")(t)
    t.toOption.foreach { x =>
      assert(x.validate.isValid, "updatedBrochureJob validates")
      val amount = x
        .resourceSetsNamed(ResourceSetName.unsafe("Component"))
        .toList
        .flatMap(_.resources.toList)
        .flatMap(_.amountPool.toList)
        .flatMap(_.toList)
        .flatMap(_.amount.toList)
      assertEquals(amount, List(Amount(650.0)))
    }
