package xjdf4s.laws

import cats.data.{Validated, ValidatedNec}
import munit.FunSuite
import xjdf4s.examples.SpecExamples
import xjdf4s.model.{AmountPool, Bom, Issue, ResourceSetName, XJDF, validate}
import xjdf4s.prim.Amount

/** Conformance suite for the worked examples of the XJDF specification
 *  (M1.5-3, PR-13). The example *values* live in `modules/examples`
 *  (`SpecExamples`), which remains a demo module; this suite is the regular
 *  test gate that actually *executes* them — one named test per example,
 *  carrying the normative section/table (§12.1 «Specification» pyramid level).
 */
class SpecExamplesSuite extends FunSuite:

  private def assertConstructs[A](label: String)(v: ValidatedNec[Issue, A]): Unit =
    v match
      case Validated.Valid(_) => ()
      case Validated.Invalid(es) =>
        fail(s"$label failed to construct: ${es.toChain.toList.map(_.message).mkString("; ")}")

  private def assertValid[A](label: String)(v: ValidatedNec[Issue, A]): Unit =
    assertConstructs(label)(v)
    v.toOption.foreach {
      case t: XJDF => assert(t.validate.isValid, s"$label validates")
      case _       => ()
    }

  test("Example 3.1 (Table 3.1): minimalProduct constructs and validates"):
    assertValid("Example 3.1")(SpecExamples.minimalProduct)

  test("Example 3.4 (Table 3.11): notebook BOM unfolds and totalCopies is computed (N-01)"):
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

  test("Example 3.6 (Table 3.12): combinedProcesses constructs and validates"):
    assertValid("Example 3.6")(SpecExamples.combinedProcesses)

  test("Example 5.2 (Tables 6.38/6.54/6.55): splitDelivery constructs and validates"):
    assertValid("Example 5.2")(SpecExamples.splitDelivery)

  test("Example 3.3 (Table 3.3): mediaConsumptionAudit constructs"):
    assertConstructs("Example 3.3")(SpecExamples.mediaConsumptionAudit)

  test("brochureJob (Tables 4.8/4.31/4.32/6.148): constructs and validates"):
    assertValid("brochureJob")(SpecExamples.brochureJob)

  test("ADR-0001: updatedBrochureJob applies a nominal ChangeOrder and revalidates"):
    val t = SpecExamples.updatedBrochureJob
    assertValid("updatedBrochureJob")(t)
    t.toOption.foreach { x =>
      val amount = x
        .resourceSetsNamed(ResourceSetName.unsafe("Component"))
        .toList
        .flatMap(_.resources.toList)
        .flatMap(_.amountPool.toList)
        .flatMap(_.toList)
        .flatMap(_.amount.toList)
      assertEquals(amount, List(Amount(650.0)))
    }

end SpecExamplesSuite
