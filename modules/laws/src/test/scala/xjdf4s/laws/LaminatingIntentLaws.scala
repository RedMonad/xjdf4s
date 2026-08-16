package xjdf4s.laws

import xjdf4s.intents.{IntentPayload, LaminatingIntent}
import xjdf4s.model.*
import xjdf4s.prim.*
import cats.data.{Chain, NonEmptyChain}
import munit.FunSuite

/** Tests for `LaminatingIntent` (§4.9 / Table 4.30).
 *
 *  Normative mapping:
 *  - required `@Surface` list of `Side` values -> `NonEmptyChain[Side]`;
 *  - optional closed `@Temperature` -> `LaminatingTemperature` (`Hot`, `Cold`);
 *  - optional open `@Texture` -> `NmToken` with recommendations from Table A.80;
 *  - optional `@Thickness` in microns -> `Microns`;
 *  - no IDREF attributes or child elements.
 */
class LaminatingIntentLaws extends FunSuite:

  private val laminating = LaminatingIntent(
    surface = NonEmptyChain(Side.Front, Side.Back),
    temperature = Some(LaminatingTemperature.Hot),
    texture = Some(Catalog.Texture.Gloss),
    thickness = Some(Microns(25.0))
  )

  private def ticketWith(intent: Intent): XJDF =
    val product = Product(
      id = Some(Id.unsafe("P1")),
      isRoot = true,
      amount = Some(1L),
      intents = Chain.one(intent)
    )
    XJDF(
      jobId = JobId.unsafe("LaminatingLaws"),
      types = NonEmptyChain.one(ProcessType.Laminating),
      productList = Some(ProductList(products = NonEmptyChain.one(product)))
    )

  test("Table 4.30: payload element name is LaminatingIntent"):
    val payload = IntentPayload.Laminating(laminating)
    assertEquals(payload.elementName, NmToken.unsafe("LaminatingIntent"))

  test("Table 4.30: LaminatingIntent contributes no IDREFs"):
    val payload = IntentPayload.Laminating(laminating)
    assertEquals(payload.references.toList, Nil)

  test("Table 4.30: required @Surface is represented by a non-empty Side chain"):
    assertEquals(laminating.surface.toChain.toList, List(Side.Front, Side.Back))

  test("Table 4.30: all optional attributes map to their domain types"):
    assertEquals(laminating.temperature, Some(LaminatingTemperature.Hot))
    assertEquals(laminating.texture, Some(Catalog.Texture.Gloss))
    assertEquals(laminating.thickness.map(_.value), Some(25.0))

  test("Table 4.30 / A.80: @Texture remains open beyond recommended values"):
    val vendorTexture = NmToken.unsafe("VendorTexture")
    assert(!Catalog.Texture.recommended.contains(vendorTexture))
    assertEquals(
      LaminatingIntent(NonEmptyChain.one(Side.Front), texture = Some(vendorTexture)).texture,
      Some(vendorTexture)
    )

  test("Table 4.30: a LaminatingIntent ticket validates through the root validator"):
    val payload = IntentPayload.Laminating(laminating)
    val intent = Intent(IntentName.of(payload.elementName), payload)
    assert(ticketWith(intent).validate.isValid)

  test("Tables 4.1/4.30: a mismatched Intent/@Name is rejected"):
    val payload = IntentPayload.Laminating(laminating)
    val intent = Intent(IntentName.unsafe("MediaIntent"), payload)
    val report = ticketWith(intent).validateReport
    assert(!report.isValid)
    assert(report.errors.toList.exists(_.code.contains(IssueCode.IntentNameMismatch)))

end LaminatingIntentLaws
