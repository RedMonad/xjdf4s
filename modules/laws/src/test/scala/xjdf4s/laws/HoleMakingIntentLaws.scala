package xjdf4s.laws

import xjdf4s.intents.{HoleMakingIntent, IntentPayload}
import xjdf4s.model.elements.HolePattern
import xjdf4s.model.*
import xjdf4s.prim.*
import cats.data.{Chain, NonEmptyChain}
import munit.FunSuite

/** Tests for `HoleMakingIntent` (Table 4.29, §4.8) and the wiring of the nested
 *  `HolePattern` SHALL rule (Table 8.30) through the root validator.
 *
 *  Normative references:
 *  - Table 4.29 (`HoleMakingIntent` element, §4.8): sole member `HolePattern+`
 *  - Table 8.30 (`HolePattern` element, §8.25)
 *  - Appendix F (Hole Pattern Catalog)
 *  - schema.xsd `HoleMakingIntent` (`minOccurs="1" maxOccurs="unbounded"`)
 */
class HoleMakingIntentLaws extends FunSuite:

  private val validPattern: HolePattern =
    HolePattern(pattern = Some(Catalog.HolePattern.R2mDIN))

  private def intentWith(patterns: NonEmptyChain[HolePattern]): Intent =
    val payload = IntentPayload.HoleMaking(HoleMakingIntent(patterns))
    Intent(name = IntentName.of(payload.elementName), specific = payload)

  private def ticketWith(patterns: NonEmptyChain[HolePattern]): XJDF =
    val product = Product(
      id = Some(Id.unsafe("P1")),
      isRoot = true,
      amount = Some(1L),
      intents = Chain.one(intentWith(patterns))
    )
    XJDF(
      jobId = JobId.unsafe("HoleMakingLaws"),
      types = NonEmptyChain.one(ProcessType.HoleMaking),
      productList = Some(ProductList(products = NonEmptyChain.one(product)))
    )

  // --- Structure ------------------------------------------------------------

  test("Table 4.29: HoleMakingIntent payload element name is HoleMakingIntent"):
    val payload = IntentPayload.HoleMaking(HoleMakingIntent(NonEmptyChain.one(validPattern)))
    assertEquals(payload.elementName, NmToken.unsafe("HoleMakingIntent"))

  test("Table 4.29: HoleMakingIntent contributes no IDREFs (HolePattern has none)"):
    val payload = IntentPayload.HoleMaking(HoleMakingIntent(NonEmptyChain.one(validPattern)))
    assertEquals(payload.references.toList, Nil)

  test("Table 4.29: HolePattern+ cardinality is NonEmptyChain (single element constructs)"):
    val intent = HoleMakingIntent(NonEmptyChain.one(validPattern))
    assertEquals(intent.holePatterns.toChain.size.toInt, 1)

  // --- Positive -------------------------------------------------------------

  test("Table 4.29: ticket with a single valid HolePattern validates"):
    assert(ticketWith(NonEmptyChain.one(validPattern)).validate.isValid)

  test("Table 4.29: ticket with several valid HolePatterns validates"):
    val patterns = NonEmptyChain(
      validPattern,
      HolePattern(pattern = Some(Catalog.HolePattern.R4mDINA4)),
      HolePattern(
        center = Some(XYPair(0.0, 0.0)),
        extent = Some(XYPair(6.0, 6.0)),
        shape = Some(HoleShape.Round)
      )
    )
    assert(ticketWith(patterns).validate.isValid)

  // --- Negative: nested HolePattern SHALL rule (Table 8.30) -----------------

  test("Table 4.29/8.30: HoleMakingIntent with a HolePattern missing @Pattern is rejected"):
    val bad = HolePattern(extent = Some(XYPair(5.0, 5.0)), shape = Some(HoleShape.Round))
    val report = ticketWith(NonEmptyChain.one(bad)).validateReport
    assert(!report.isValid)
    assert(report.errors.toList.exists(_.code.contains(IssueCode.HolePatternPatternRequired)))

  test("Table 4.29/8.30: only the offending HolePattern among several is flagged"):
    val bad = HolePattern(center = Some(XYPair(0.0, 0.0))) // missing extent/shape/pattern
    val report = ticketWith(NonEmptyChain(validPattern, bad)).validateReport
    val matching = report.errors.toList.count(_.code.contains(IssueCode.HolePatternPatternRequired))
    assertEquals(matching, 1)

  test("Table 4.29/8.30: fully empty HolePattern inside HoleMakingIntent is rejected"):
    val report = ticketWith(NonEmptyChain.one(HolePattern())).validateReport
    assert(!report.isValid)
    assert(report.errors.toList.exists(_.code.contains(IssueCode.HolePatternPatternRequired)))

end HoleMakingIntentLaws
