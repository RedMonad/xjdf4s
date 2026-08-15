package xjdf4s.laws

import cats.Show
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
 *
 *  Golden `Show` renders of the same examples are checked below: they are
 *  *temporary structural goldens*, replaced by canonical XML/JSON renders in
 *  M2 (§12.2: `Show` is debug output, not serialization).
 *
 *  Update procedure for the golden literals:
 *    1. run `sbt -batch examples/run` — `SpecExamples.renderAll` prints each
 *       example label followed by the `Show` render indented by two spaces;
 *    2. copy the indented line into the corresponding `assertEquals` literal
 *       (the label itself is not part of the golden);
 *    3. re-run `sbt -batch test` and `sbt -batch scalafmtCheckAll`.
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

  // ---------------------------------------------------------------------------
  // Golden tests: `Show` renders fixed as literals (temporary, see scaladoc).
  // ---------------------------------------------------------------------------

  private def showOf[A: Show](label: String)(v: ValidatedNec[Issue, A]): String =
    v match
      case Validated.Valid(a)    => Show[A].show(a)
      case Validated.Invalid(es) =>
        fail(s"$label failed to construct: ${es.toChain.toList.map(_.message).mkString("; ")}")

  test("golden: Example 3.1 Show render"):
    assertEquals(showOf("Example 3.1")(SpecExamples.minimalProduct), "XJDF(job=J1, types=Product)")

  test("golden: Example 3.4 notebook Show render"):
    assertEquals(
      showOf("Example 3.4 notebook")(SpecExamples.notebook),
      "ProductList(Product(Notebook×10, root), Product(FrontCover×1, part), Product(BookBlock×50, part), Product(BackCover×1, part))"
    )

  test("golden: Example 3.6 combinedProcesses Show render"):
    assertEquals(
      showOf("Example 3.6")(SpecExamples.combinedProcesses),
      "XJDF(job=CPI_Example, types=Cutting Folding)"
    )

  test("golden: Example 5.2 splitDelivery Show render"):
    assertEquals(
      showOf("Example 5.2")(SpecExamples.splitDelivery),
      "XJDF(job=splitDelivery, types=Product, ProductList(Product(Book×30, root)))"
    )

  test("golden: Example 3.3 mediaConsumptionAudit Show render"):
    assertEquals(
      showOf("Example 3.3")(SpecExamples.mediaConsumptionAudit),
      "[AuditResource(Header(device=TestSender, time=2020-03-01T19:55:57+01:00), ResourceInfo(Media))]"
    )

  test("golden: brochureJob Show render"):
    assertEquals(
      showOf("brochureJob")(SpecExamples.brochureJob),
      "XJDF(job=Brochure-2026, types=DigitalPrinting Stitching, ProductList(Product(Brochure×500, root)), audits=2)"
    )

  test("golden: updatedBrochureJob Show render (change order does not alter audits)"):
    assertEquals(
      showOf("updatedBrochureJob")(SpecExamples.updatedBrochureJob),
      "XJDF(job=Brochure-2026, types=DigitalPrinting Stitching, ProductList(Product(Brochure×500, root)), audits=2)"
    )

end SpecExamplesSuite
