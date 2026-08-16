package xjdf4s.laws

import xjdf4s.intents.{ColorIntent, IntentPayload, MediaIntent, ProductionIntent, SurfaceColor}
import xjdf4s.model.*
import xjdf4s.model.elements.Certification
import xjdf4s.prim.*
import xjdf4s.resources.{Media => MediaResourceValue, ResourcePayload}
import cats.data.{Chain, NonEmptyChain}
import munit.FunSuite

/** Tests for the `Certification` element (§8.7 / Table 8.8) and its SHALL rule.
 *
 *  Normative references:
 *  - Table 8.8 (`Certification` element): `@Claim?` string, `@Identifier?`
 *    string, `@Organization?` NMTOKEN — all optional in `schema.xsd`
 *    (`<xs:element name="Certification">`, three `use="optional"`, no
 *    subelements, no ID/IDREF);
 *  - Tables 4.21 / 4.32 / 4.33 / 6.114 (`Certification*` in `SurfaceColor`,
 *    `MediaIntent`, `ProductionIntent`, `Media`): "Each Certification SHALL
 *    specify a … certification level" — the SHALL modelled by
 *    `Certification.law` (ADR-0012);
 *  - the container sentence "If more than one Certification is present, at
 *    least one of the … levels SHALL be met" is a requirement on actual
 *    production, not on the document, and is deliberately not validated
 *    (SPEC-COVERAGE, Deliberate Deviations).
 */
class CertificationLaws extends FunSuite:

  private val at = XPath("/XJDF/Certification")

  private val fscMix = Certification(
    claim = Some(Catalog.CertificationClaim.FscMix70),
    identifier = Some(XjdfString.unsafe("FSC-C012345")),
    organization = Some(Catalog.CertificationOrganization.FSC)
  )

  // --- Positive tests -------------------------------------------------------

  test("Table 8.8: a fully populated Certification passes the law check") {
    assertEquals(Certification.law(fscMix, at).toList, Nil)
  }

  test("Table 8.8: @Claim alone specifies a certification level") {
    val c = Certification(claim = Some(Catalog.CertificationClaim.Fsc100))
    assertEquals(Certification.law(c, at).toList, Nil)
  }

  test("Table 8.8: @Identifier alone specifies a certification level") {
    val c = Certification(identifier = Some(XjdfString.unsafe("PEFC/04-31-0812")))
    assertEquals(Certification.law(c, at).toList, Nil)
  }

  test("Table 8.8: @Organization alone specifies a certification level") {
    val c = Certification(organization = Some(Catalog.CertificationOrganization.PEFC))
    assertEquals(Certification.law(c, at).toList, Nil)
  }

  test("Table 8.8: Certification declares no ID/IDREF attributes") {
    assertEquals(fscMix.references.toList, Nil)
  }

  // --- Negative test: the SHALL of Tables 4.21/4.32/4.33/6.114 -------------

  test("Table 8.8 + Table 4.33 (ADR-0012): an empty Certification specifies no level and is rejected") {
    // schema.xsd declares all three attributes use="optional", so <Certification/>
    // is schema-valid; the prose SHALL wins per ROADMAP §1.2.
    val issues = Certification.law(Certification(), at)
    assertEquals(issues.size.toInt, 1)
    assertEquals(issues.toList.head.code, Some(IssueCode.CertificationLevelMissing))
    assertEquals(issues.toList.head.severity, SeverityClass.Error)
    assertEquals(issues.toList.head.location, at)
  }

  test("ADR-0012: specifiesLevel is false exactly for the empty Certification") {
    assert(!Certification().specifiesLevel)
    assert(fscMix.specifiesLevel)
  }

  // --- Container law: XPath indexing over `Certification*` -----------------

  test("Table 8.8: containerLaw indexes each offending Certification by position") {
    val chain = Chain(fscMix, Certification(), fscMix, Certification())
    val issues = Certification.containerLaw(chain, XPath("/XJDF/X"))
    assertEquals(issues.size.toInt, 2)
    assertEquals(
      issues.toList.map(_.location.value),
      List("/XJDF/X/Certification[1]", "/XJDF/X/Certification[3]")
    )
  }

  test("Table 8.8: containerLaw on an empty chain reports nothing (Certification* is optional)") {
    assertEquals(Certification.containerLaw(Chain.empty, XPath("/XJDF/X")).toList, Nil)
  }

  // --- Open catalogs (ADR-0007) --------------------------------------------

  test("Table 8.8: @Organization is an open catalog — a value outside it stays legal") {
    val c = Certification(organization = Some(NmToken.unsafe("SFI")))
    assertEquals(Certification.law(c, at).toList, Nil)
    assertEquals(Catalog.CertificationOrganization.recommended.map(_.value), List("CFCC", "FSC", "IFCC", "PEFC"))
  }

  test("Table 8.8: @Claim is an open catalog of strings — spaces and % are legal, PEFC nn% is parameterised") {
    assertEquals(Catalog.CertificationClaim.pefcPercent(70).value, "PEFC 70%")
    assertEquals(
      Catalog.CertificationClaim.recommended.map(_.value),
      List(
        "FSC 100%",
        "FSC Mix 70%",
        "FSC Mix Credit",
        "FSC Recycled 85%",
        "FSC Recycled Credit",
        "PEFC Certified",
        "PEFC Recycled"
      )
    )
    val c = Certification(claim = Some(XjdfString.unsafe("Blue Angel")))
    assertEquals(Certification.law(c, at).toList, Nil)
  }

  // --- Wiring: the four modelled containers reach the rule -----------------

  private def ticketWithIntent(payload: IntentPayload): XJDF =
    val intent = Intent(IntentName.of(payload.elementName), payload)
    val product = Product(
      id = Some(Id.unsafe("P1")),
      isRoot = true,
      amount = Some(1L),
      intents = Chain.one(intent)
    )
    XJDF(
      jobId = JobId.unsafe("CertificationLaws"),
      types = NonEmptyChain.one(ProcessType.Product),
      productList = Some(ProductList(products = NonEmptyChain.one(product)))
    )

  private def codesOf(t: XJDF): List[IssueCode] =
    t.validateReport.issues.toList.flatMap(_.code.toList)

  test("Table 4.21: an empty Certification under ColorIntent/SurfaceColor is rejected by the validator") {
    val payload = IntentPayload.Color(
      ColorIntent(front = Some(SurfaceColor(surface = Side.Front, certifications = Chain.one(Certification()))))
    )
    val t = ticketWithIntent(payload)
    assert(t.validate.isInvalid)
    assertEquals(codesOf(t), List(IssueCode.CertificationLevelMissing))
  }

  test("Table 4.21: both surfaces are traversed — the back surface is not skipped") {
    val payload = IntentPayload.Color(
      ColorIntent(
        front = Some(SurfaceColor(surface = Side.Front, certifications = Chain.one(fscMix))),
        back = Some(SurfaceColor(surface = Side.Back, certifications = Chain.one(Certification())))
      )
    )
    val t = ticketWithIntent(payload)
    assert(t.validate.isInvalid)
    val locations = t.validateReport.errors.toList.map(_.location.value)
    assertEquals(locations.size, 1)
    assert(
      locations.head.contains("SurfaceColor[@Surface='Back']"),
      s"expected the back surface in the XPath, got ${locations.head}"
    )
  }

  test("Table 4.32: an empty Certification under MediaIntent is rejected by the validator") {
    val payload = IntentPayload.Media(
      MediaIntent(MediaType.Paper, certifications = Chain.one(Certification()))
    )
    val t = ticketWithIntent(payload)
    assert(t.validate.isInvalid)
    assertEquals(codesOf(t), List(IssueCode.CertificationLevelMissing))
  }

  test("Table 4.33: an empty Certification under ProductionIntent is rejected by the validator") {
    val payload = IntentPayload.Production(
      ProductionIntent(printPreference = Some(PrintPreference.HighestQuality), certifications = Chain.one(Certification()))
    )
    val t = ticketWithIntent(payload)
    assert(t.validate.isInvalid)
    assertEquals(codesOf(t), List(IssueCode.CertificationLevelMissing))
  }

  test("Table 6.114: an empty Certification under the Media resource is rejected by the validator") {
    val media = MediaResourceValue(MediaType.Paper, certifications = Chain.one(Certification()))
    val t = XJDF(
      jobId = JobId.unsafe("CertificationLaws"),
      types = NonEmptyChain.one(ProcessType.Cutting),
      resourceSets = Chain.one(
        ResourceSet(
          ResourceSetName.unsafe("Media"),
          usage = Some(Usage.Input),
          resources = Chain.one(Resource(specific = Some(ResourcePayload.MediaResource(media))))
        )
      )
    )
    assert(t.validate.isInvalid)
    assertEquals(codesOf(t), List(IssueCode.CertificationLevelMissing))
  }

  test("Tables 4.21/4.32/4.33/6.114: lawful Certifications in all four containers keep the ticket valid") {
    val colorTicket = ticketWithIntent(
      IntentPayload.Color(
        ColorIntent(front = Some(SurfaceColor(surface = Side.Front, certifications = Chain.one(fscMix))))
      )
    )
    val mediaIntentTicket = ticketWithIntent(
      IntentPayload.Media(MediaIntent(MediaType.Paper, certifications = Chain.one(fscMix)))
    )
    val productionTicket = ticketWithIntent(
      IntentPayload.Production(ProductionIntent(certifications = Chain.one(fscMix)))
    )
    val mediaResourceTicket = XJDF(
      jobId = JobId.unsafe("CertificationLaws"),
      types = NonEmptyChain.one(ProcessType.Cutting),
      resourceSets = Chain.one(
        ResourceSet(
          ResourceSetName.unsafe("Media"),
          usage = Some(Usage.Input),
          resources = Chain.one(
            Resource(specific = Some(ResourcePayload.MediaResource(
              MediaResourceValue(MediaType.Paper, certifications = Chain.one(fscMix))
            )))
          )
        )
      )
    )
    assert(colorTicket.validate.isValid, colorTicket.validateReport.issues.toList.toString)
    assert(mediaIntentTicket.validate.isValid, mediaIntentTicket.validateReport.issues.toList.toString)
    assert(productionTicket.validate.isValid, productionTicket.validateReport.issues.toList.toString)
    assert(mediaResourceTicket.validate.isValid, mediaResourceTicket.validateReport.issues.toList.toString)
  }

  test("Table 4.33: more than one Certification is accepted — the 'at least one level met' sentence is not a document rule") {
    // Deliberate deviation: a ticket cannot state which level was met, so the
    // container sentence is documented, not validated.
    val payload = IntentPayload.Production(
      ProductionIntent(certifications = Chain(fscMix, Certification(organization = Some(Catalog.CertificationOrganization.PEFC))))
    )
    val t = ticketWithIntent(payload)
    assert(t.validate.isValid, t.validateReport.issues.toList.toString)
  }

end CertificationLaws
