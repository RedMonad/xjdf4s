package xjdf4s.laws

import xjdf4s.intents.{ContentCheckIntent, IntentPayload, PreflightItem, ProofItem}
import xjdf4s.model.*
import xjdf4s.model.elements.{Disposition, FileSpec}
import xjdf4s.prim.*
import xjdf4s.resources.{DeliveryParams, DropItem, ResourcePayload}
import cats.data.{Chain, NonEmptyChain}
import munit.FunSuite

/** Tests for `ContentCheckIntent` (§4.5 / Tables 4.22–4.24).
 *
 *  Normative mapping:
 *  - `PreflightItem*` and `ProofItem*` are both optional chains — an empty
 *    intent is structurally valid (Table 4.22, `schema.xsd`
 *    `minOccurs="0" maxOccurs="unbounded"`);
 *  - `PreflightItem/@PreflightLevel` -> closed `PreflightLevel` (Table 4.23);
 *  - `ProofItem` carries 7 attributes and the nested `FileSpec?` element
 *    (Table 4.24): `@Amount` integer, `@ColorType` closed `ProofColorType`,
 *    `@Contract`/`@HalfTone` boolean, `@ID` ID, `@PageIndex` IntegerRange,
 *    `@ProofTarget` URL (Deprecated in XJDF 2.1), `FileSpec` element (New in
 *    XJDF 2.1) — the shared chapter-8 element reused from `model/elements`;
 *  - no IDREF attributes: the intent contributes no references, but its
 *    `ProofItem/@ID` values are document-scoped IDs (Table 6.55 lets
 *    `DropItem/@ItemRef` target a ProofItem);
 *  - `FileSpec/Disposition` local law (Table 8.23) is reached through this
 *    intent — the first FileSpec-bearing traversal of the local-law bus.
 */
class ContentCheckIntentLaws extends FunSuite:

  private val proofItem = ProofItem(
    amount = Some(2L),
    colorType = Some(ProofColorType.MatchedColor),
    contract = Some(true),
    halfTone = Some(false),
    id = Some(Id.unsafe("PI1")),
    pageIndex = Some(IntegerRange(0, 3)),
    fileSpec = Some(FileSpec.ofUrl(Url.unsafe("file:///proofs/customer-proof.pdf")))
  )

  private val contentCheck = ContentCheckIntent(
    preflightItems = Chain.one(PreflightItem(preflightLevel = Some(PreflightLevel.Premium))),
    proofItems = Chain.one(proofItem)
  )

  private def ticketWith(intent: Intent): XJDF =
    val product = Product(
      id = Some(Id.unsafe("P1")),
      isRoot = true,
      amount = Some(1L),
      intents = Chain.one(intent)
    )
    XJDF(
      jobId = JobId.unsafe("ContentCheckLaws"),
      types = NonEmptyChain(ProcessType.Approval, ProcessType.Preflight),
      productList = Some(ProductList(products = NonEmptyChain.one(product)))
    )

  private def contentCheckIntent(payload: IntentPayload): Intent =
    Intent(IntentName.of(payload.elementName), payload)

  test("Table 4.22: payload element name is ContentCheckIntent") {
    val payload = IntentPayload.ContentCheck(contentCheck)
    assertEquals(payload.elementName, NmToken.unsafe("ContentCheckIntent"))
  }

  test("Table 4.22: ContentCheckIntent contributes no IDREFs") {
    val payload = IntentPayload.ContentCheck(contentCheck)
    assertEquals(payload.references.toList, Nil)
  }

  test("Table 4.22: both members are `*` — an empty intent validates") {
    val payload = IntentPayload.ContentCheck(ContentCheckIntent())
    assert(ticketWith(contentCheckIntent(payload)).validate.isValid)
  }

  test("Table 4.23: @PreflightLevel maps to the closed PreflightLevel enumeration") {
    PreflightLevel.all.foreach { level =>
      val intent = Intent(
        IntentName.unsafe("ContentCheckIntent"),
        IntentPayload.ContentCheck(
          ContentCheckIntent(preflightItems = Chain.one(PreflightItem(preflightLevel = Some(level))))
        )
      )
      assert(ticketWith(intent).validate.isValid, level.toString)
    }
  }

  test("Table 4.24: all ProofItem members map to their domain types") {
    val withDeprecatedTarget = proofItem.copy(
      proofTarget = Some(Url.unsafe("file:///proofs/soft-proof"))
    )
    assertEquals(withDeprecatedTarget.amount, Some(2L))
    assertEquals(withDeprecatedTarget.colorType, Some(ProofColorType.MatchedColor))
    assertEquals(withDeprecatedTarget.contract, Some(true))
    assertEquals(withDeprecatedTarget.halfTone, Some(false))
    assertEquals(withDeprecatedTarget.id.map(_.value), Some("PI1"))
    assertEquals(withDeprecatedTarget.pageIndex.map(_.from), Some(0L))
    assertEquals(withDeprecatedTarget.pageIndex.map(_.to), Some(3L))
    // Deprecated in XJDF 2.1 @ProofTarget coexists with FileSpec (New in
    // XJDF 2.1) so XJDF 2.0 documents decode losslessly.
    assertEquals(withDeprecatedTarget.proofTarget.map(_.value), Some("file:///proofs/soft-proof"))
    assertEquals(
      withDeprecatedTarget.fileSpec.map(_.url.map(_.value)),
      Some(Some("file:///proofs/customer-proof.pdf"))
    )
  }

  test("Table 4.24: ProofItem/@ID is collected into the document-scoped IDs") {
    val payload = IntentPayload.ContentCheck(contentCheck)
    assertEquals(payload.declaredIds.toList.map(_.value), List("PI1"))
    val ticket = ticketWith(contentCheckIntent(payload))
    assert(ticket.declaredIds.toList.map(_.value).contains("PI1"))
  }

  test("Tables 4.24/6.55: DropItem/@ItemRef resolves against ProofItem/@ID") {
    val ticket = ticketWith(contentCheckIntent(IntentPayload.ContentCheck(contentCheck)))
    val delivery = Resource(
      specific = Some(
        ResourcePayload.DeliveryParamsResource(
          DeliveryParams(dropItems = Chain.one(DropItem(2L, IdRef.unsafe("PI1"))))
        )
      )
    )
    val withDelivery = ticket.copy(
      resourceSets = Chain.one(
        ResourceSet(
          ResourceSetName.unsafe("DeliveryParams"),
          usage = Some(Usage.Input),
          resources = Chain.one(delivery)
        )
      )
    )
    assert(withDelivery.validate.isValid)
  }

  test("Table 6.55: a DropItem/@ItemRef without a matching ProofItem/@ID is rejected") {
    val ticket = ticketWith(contentCheckIntent(IntentPayload.ContentCheck(contentCheck)))
    val delivery = Resource(
      specific = Some(
        ResourcePayload.DeliveryParamsResource(
          DeliveryParams(dropItems = Chain.one(DropItem(2L, IdRef.unsafe("NoProof"))))
        )
      )
    )
    val withDelivery = ticket.copy(
      resourceSets = Chain.one(
        ResourceSet(
          ResourceSetName.unsafe("DeliveryParams"),
          usage = Some(Usage.Input),
          resources = Chain.one(delivery)
        )
      )
    )
    val report = withDelivery.validateReport
    assert(!report.isValid)
    assert(report.errors.toList.exists(_.code.contains(IssueCode.DanglingIdRef)))
  }

  test("Table 4.24: duplicate ProofItem/@ID values are rejected (§2.2.3)") {
    val duplicate = contentCheck.copy(proofItems = Chain(proofItem, proofItem.copy(amount = Some(1L))))
    val report = ticketWith(contentCheckIntent(IntentPayload.ContentCheck(duplicate))).validateReport
    assert(!report.isValid)
    assert(report.errors.toList.exists(_.code.contains(IssueCode.DuplicateId)))
  }

  test("Table 8.23: Disposition under ProofItem/FileSpec with @MinDuration and @Until is rejected") {
    val badDisposition = Disposition(
      minDuration = Some(TimeSpan.ofHours(24)),
      until = Some(Timestamp.unsafe("2026-09-01T00:00:00+02:00"))
    )
    val item = proofItem.copy(fileSpec = Some(
      FileSpec.ofUrl(Url.unsafe("file:///proofs/customer-proof.pdf")).copy(disposition = Some(badDisposition))
    ))
    val intent = contentCheckIntent(
      IntentPayload.ContentCheck(contentCheck.copy(proofItems = Chain.one(item)))
    )
    val report = ticketWith(intent).validateReport
    assert(!report.isValid)
    assert(report.errors.toList.exists(_.code.contains(IssueCode.LocalLawViolation)))
  }

  test("Table 8.23: Disposition with only @MinDuration is accepted") {
    val goodDisposition = Disposition(minDuration = Some(TimeSpan.ofHours(24)))
    val item = proofItem.copy(fileSpec = Some(
      FileSpec.ofUrl(Url.unsafe("file:///proofs/customer-proof.pdf")).copy(disposition = Some(goodDisposition))
    ))
    val intent = contentCheckIntent(
      IntentPayload.ContentCheck(contentCheck.copy(proofItems = Chain.one(item)))
    )
    assert(ticketWith(intent).validate.isValid)
  }

  test("Tables 4.1/4.22: a mismatched Intent/@Name is rejected") {
    val payload = IntentPayload.ContentCheck(contentCheck)
    val intent = Intent(IntentName.unsafe("MediaIntent"), payload)
    val report = ticketWith(intent).validateReport
    assert(!report.isValid)
    assert(report.errors.toList.exists(_.code.contains(IssueCode.IntentNameMismatch)))
  }

end ContentCheckIntentLaws
