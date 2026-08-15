package xjdf4s.laws

import xjdf4s.intents.{EmbossingIntent, EmbossingItem, IntentPayload}
import xjdf4s.model.*
import xjdf4s.prim.*
import xjdf4s.resources.{Color, ResourcePayload}
import cats.data.{Chain, NonEmptyChain}
import munit.FunSuite

/** Tests for `EmbossingIntent` (§4.6 / Table 4.25) and `EmbossingItem`
 *  (Table 4.26).
 *
 *  Normative mapping:
 *  - `EmbossingItem+` -> `NonEmptyChain[EmbossingItem]` (cardinality `+`,
 *    enforced structurally; `schema.xsd`: `minOccurs="1" maxOccurs="unbounded"`);
 *  - required `@EmbossingType` -> plain `EmbossType` field (Table A.19);
 *  - optional closed `@Direction` -> `EmbossDirection` (Table A.18);
 *  - optional `@Face` -> `Face` (Table A.20);
 *  - optional open `@FoilColor` -> `NmToken` from the `NamedColor` catalog
 *    (§A.2.30, ADR-0007);
 *  - SHALL (Table 4.26): a `Color` resource specified for `@Separation` SHALL
 *    have `@ColorType="DieLine"` — global rule
 *    `TicketValidator.checkEmbossingColorTypes`,
 *    `IssueCode.EmbossingColorNotDieLine`;
 *  - SHOULD (not an error, ADR-0006): `@FoilColorDetails` SHOULD come with
 *    `@FoilColor`;
 *  - no IDREF attributes or child elements.
 */
class EmbossingIntentLaws extends FunSuite:

  private val blind = EmbossingItem(
    direction = Some(EmbossDirection.Raised),
    embossingType = EmbossType.BlindEmbossing,
    face = Some(Face.Front),
    foilColor = Some(Catalog.NamedColor.Silver),
    foilColorDetails = Some(XjdfString.unsafe("Holographic")),
    height = Some(0.3),
    imageSize = Some(XYPair(20.0, 20.0)),
    position = Some(XYPair(50.0, 50.0)),
    separation = Some(NmToken.unsafe("Emboss")),
    toolName = Some(NmToken.unsafe("EmbossDie-1"))
  )

  private val foilStamp = EmbossingItem(
    embossingType = EmbossType.FoilStamping,
    direction = Some(EmbossDirection.Flat)
  )

  private def intentOf(items: NonEmptyChain[EmbossingItem]): Intent =
    val payload = IntentPayload.Embossing(EmbossingIntent(items))
    Intent(name = IntentName.of(payload.elementName), specific = payload)

  private def ticketWith(intent: Intent, resourceSets: Chain[ResourceSet] = Chain.empty): XJDF =
    val product = Product(
      id = Some(Id.unsafe("P1")),
      isRoot = true,
      amount = Some(1L),
      intents = Chain.one(intent)
    )
    XJDF(
      jobId = JobId.unsafe("EmbossingLaws"),
      types = NonEmptyChain.one(ProcessType.Embossing),
      productList = Some(ProductList(products = NonEmptyChain.one(product))),
      resourceSets = resourceSets
    )

  /** A `Color` resource, optionally partitioned by `Part/@Separation`. */
  private def colorResource(separation: Option[String], colorType: Option[ColorType]): Resource =
    Resource(
      specific = Some(ResourcePayload.ColorResource(Color(colorType = colorType))),
      parts = Chain.fromOption(separation.map(s => Part.token(PartitionKey.Separation, NmToken.unsafe(s))))
    )

  private def colorResourceSet(resource: Resource): ResourceSet =
    ResourceSet(ResourceSetName.unsafe("Color"), usage = Some(Usage.Input), resources = Chain.one(resource))

  test("Table 4.25: payload element name is EmbossingIntent"):
    val payload = IntentPayload.Embossing(EmbossingIntent(NonEmptyChain.one(blind)))
    assertEquals(payload.elementName, NmToken.unsafe("EmbossingIntent"))

  test("Table 4.25: EmbossingIntent contributes no IDREFs"):
    val payload = IntentPayload.Embossing(EmbossingIntent(NonEmptyChain.one(blind)))
    assertEquals(payload.references.toList, Nil)

  test("Table 4.25: EmbossingItem+ is a NonEmptyChain (structural cardinality)"):
    val items = NonEmptyChain(blind, foilStamp)
    assertEquals(EmbossingIntent(items).embossingItems.toChain.toList, List(blind, foilStamp))

  test("Table 4.26: all attributes map to their domain types"):
    assertEquals(blind.direction, Some(EmbossDirection.Raised))
    assertEquals(blind.embossingType, EmbossType.BlindEmbossing)
    assertEquals(blind.face, Some(Face.Front))
    assertEquals(blind.foilColor, Some(Catalog.NamedColor.Silver))
    assertEquals(blind.foilColorDetails.map(_.value), Some("Holographic"))
    assertEquals(blind.height, Some(0.3))
    assertEquals(blind.imageSize, Some(XYPair(20.0, 20.0)))
    assertEquals(blind.position, Some(XYPair(50.0, 50.0)))
    assertEquals(blind.separation.map(_.value), Some("Emboss"))
    assertEquals(blind.toolName.map(_.value), Some("EmbossDie-1"))

  test("Table 4.26 / A.2.30: @FoilColor remains open beyond the NamedColor catalog"):
    val vendor = NmToken.unsafe("Pantone185C")
    assert(!Catalog.NamedColor.recommended.contains(vendor))
    assertEquals(blind.copy(foilColor = Some(vendor)).foilColor, Some(vendor))

  test("Table 4.25: a valid EmbossingIntent ticket validates through the root validator"):
    val ticket = ticketWith(
      intentOf(NonEmptyChain.one(blind)),
      Chain.one(colorResourceSet(colorResource(Some("Emboss"), Some(ColorType.DieLine))))
    )
    assert(ticket.validate.isValid)

  test("Tables 4.1/4.25: a mismatched Intent/@Name is rejected"):
    val payload = IntentPayload.Embossing(EmbossingIntent(NonEmptyChain.one(blind)))
    val intent = Intent(IntentName.unsafe("MediaIntent"), payload)
    val report = ticketWith(intent).validateReport
    assert(!report.isValid)
    assert(report.errors.toList.exists(_.code.contains(IssueCode.IntentNameMismatch)))

  test("Table 4.26 SHALL: Color for the embossing separation with @ColorType='Normal' is rejected"):
    val ticket = ticketWith(
      intentOf(NonEmptyChain.one(blind)),
      Chain.one(colorResourceSet(colorResource(Some("Emboss"), Some(ColorType.Normal))))
    )
    val report = ticket.validateReport
    assert(!report.isValid)
    assert(report.errors.toList.exists(_.code.contains(IssueCode.EmbossingColorNotDieLine)))

  test("Table 4.26 SHALL: Color for the embossing separation without @ColorType is rejected"):
    val ticket = ticketWith(
      intentOf(NonEmptyChain.one(blind)),
      Chain.one(colorResourceSet(colorResource(Some("Emboss"), None)))
    )
    val report = ticket.validateReport
    assert(!report.isValid)
    assert(report.errors.toList.exists(_.code.contains(IssueCode.EmbossingColorNotDieLine)))

  test("Table 4.26: Color for a different separation is not constrained"):
    val ticket = ticketWith(
      intentOf(NonEmptyChain.one(blind)), // separation "Emboss"
      Chain.one(colorResourceSet(colorResource(Some("Cyan"), Some(ColorType.Normal))))
    )
    assert(ticket.validate.isValid)

  test("Table 4.26: an unpartitioned Color resource is a generic colorant and is not matched"):
    val ticket = ticketWith(
      intentOf(NonEmptyChain.one(blind)),
      Chain.one(colorResourceSet(colorResource(None, Some(ColorType.Normal))))
    )
    assert(ticket.validate.isValid)

  test("Table 4.26: the rule is conditional on EmbossingItem/@Separation and does not fire without it"):
    // A Color partitioned by "Emboss" with @ColorType='Normal', but the ticket
    // carries no EmbossingIntent — nothing to constrain.
    val product = Product(
      id = Some(Id.unsafe("P1")),
      isRoot = true,
      amount = Some(1L)
    )
    val ticket = XJDF(
      jobId = JobId.unsafe("EmbossingLawsNoIntent"),
      types = NonEmptyChain.one(ProcessType.Embossing),
      productList = Some(ProductList(products = NonEmptyChain.one(product))),
      resourceSets = Chain.one(colorResourceSet(colorResource(Some("Emboss"), Some(ColorType.Normal))))
    )
    assert(ticket.validate.isValid)

end EmbossingIntentLaws
