package xjdf4s.laws

import xjdf4s.model.elements.MISDetails
import xjdf4s.prim.*
import cats.Show
import cats.data.Chain
import cats.kernel.Eq
import munit.FunSuite

import java.io.File
import scala.io.Source

/** Tests for the `MISDetails` element (§8.30 / Table 8.48): exact attribute
 *  mapping, the prose range of `@Complexity`, ID/IDREF absence and the four
 *  `MISDetails?` containers of `schema.xsd` (M1.6-7, PR-24).
 *
 *  Table 8.48 declares four attributes, all optional, and no children — an
 *  empty `<MISDetails/>` is valid, so there is no local SHALL rule and no
 *  negative validation test. The boundary that *is* normative is the prose
 *  range of `@Complexity` ("in a range from 0.0 to 1.0"), enforced by the
 *  `UnitInterval` factory; the XSD types the attribute as a plain `xs:float`
 *  and stays a test oracle (ROADMAP §1.2).
 */
class MISDetailsLaws extends FunSuite:

  test("Table 8.48: a fully populated MISDetails preserves all four attributes"):
    val details = MISDetails(
      complexity = Some(UnitInterval.unsafe(0.5)),
      costType = Some(CostType.Chargeable),
      workType = Some(WorkType.Rework),
      workTypeDetails = Some(Catalog.WorkTypeDetails.ResourceDamaged)
    )
    assertEquals(details.complexity.map(_.value), Some(0.5))
    assertEquals(details.costType, Some(CostType.Chargeable))
    assertEquals(details.workType, Some(WorkType.Rework))
    assertEquals(details.workTypeDetails.map(_.value), Some("ResourceDamaged"))
    assertEquals(
      Show[MISDetails].show(details),
      "MISDetails(complexity=0.5, costType=Chargeable, workType=Rework, workTypeDetails=ResourceDamaged)"
    )

  test("Table 8.48: all four attributes are optional — an empty MISDetails is valid"):
    val details = MISDetails()
    assertEquals(details.complexity, None)
    assertEquals(details.costType, None)
    assertEquals(details.workType, None)
    assertEquals(details.workTypeDetails, None)
    assertEquals(Show[MISDetails].show(details), "MISDetails()")

  test("Table 8.48: @Complexity enforces the prose range 0.0..1.0 at the boundary"):
    // The three interpretation anchors of the prose are representable…
    List(0.0, 0.5, 1.0).foreach: d =>
      assertEquals(UnitInterval.from(d).map(_.value), Some(d))
    // …while values outside the prose range are rejected even though the XSD
    // types the attribute as a plain xs:float (prose wins, ROADMAP §1.2).
    List(-0.1, 1.1, Double.NaN).foreach: d =>
      assertEquals(UnitInterval.from(d), None, s"UnitInterval.from($d)")

  test("Table 8.48 / ADR-0007: @WorkTypeDetails is an open catalog beyond the five recommended values"):
    assertEquals(
      Catalog.WorkTypeDetails.recommended.map(_.value),
      List("CustomerRequest", "EquipmentMalfunction", "InternalChange", "ResourceDamaged", "UserError")
    )
    val vendor = NmToken.unsafe("VendorSpecificReason")
    assert(!Catalog.WorkTypeDetails.recommended.contains(vendor))
    assertEquals(MISDetails(workTypeDetails = Some(vendor)).workTypeDetails, Some(vendor))

  test("Table 8.48 / §2.2.3: MISDetails declares no ID or IDREF attributes"):
    val details = MISDetails(
      complexity = Some(UnitInterval.unsafe(1.0)),
      costType = Some(CostType.NonChargeable),
      workType = Some(WorkType.Original),
      workTypeDetails = Some(Catalog.WorkTypeDetails.InternalChange)
    )
    assertEquals(details.references, Chain.empty[IdRef])

  test("Table 8.48: MISDetails equality covers all four attributes"):
    val a = MISDetails(costType = Some(CostType.Chargeable), workType = Some(WorkType.Alteration))
    val b = MISDetails(costType = Some(CostType.Chargeable), workType = Some(WorkType.Alteration))
    val c = MISDetails(costType = Some(CostType.NonChargeable), workType = Some(WorkType.Alteration))
    assert(Eq[MISDetails].eqv(a, b))
    assert(!Eq[MISDetails].eqv(a, c))

  test("Table 8.48: schema.xsd declares the exact attribute set, all optional"):
    val named = raw"""<xs:attribute name="([^"]+)" type="([^"]+)" use="([^"]+)"/>""".r
    val inline = raw"""<xs:attribute name="([^"]+)" use="([^"]+)">""".r
    val namedAttrs = named.findAllMatchIn(misDetailsSchema).map { m =>
      (m.group(1), m.group(2), m.group(3))
    }.toSet
    val inlineAttrs = inline.findAllMatchIn(misDetailsSchema).map { m =>
      (m.group(1), m.group(2))
    }.toSet
    assertEquals(
      namedAttrs,
      Set(
        ("Complexity", "xs:float", "optional"),
        ("WorkTypeDetails", "xs:NMTOKEN", "optional")
      )
    )
    assertEquals(inlineAttrs, Set(("CostType", "optional"), ("WorkType", "optional")))

  test("Table 8.48: schema.xsd inline enumerations match the closed CostType and WorkType enums"):
    val enumeration = raw"""<xs:enumeration value="([^"]+)"/>""".r
    val declared = enumeration.findAllMatchIn(misDetailsSchema).map(_.group(1)).toSet
    val modelled = (CostType.all.map(_.token.value) ++ WorkType.all.map(_.token.value)).toSet
    assertEquals(declared, modelled)

  test("Table 8.48 containers: schema.xsd declares MISDetails? in all four locations"):
    val occurrence = raw"""<xs:element maxOccurs="1" minOccurs="0" ref="MISDetails"/>""".r
    val counts = List("ResourceInfo", "PipeParams", "JobPhase", "NodeInfo").map { name =>
      name -> occurrence.findAllMatchIn(containerSchema(name)).length
    }.toMap
    assertEquals(
      counts,
      Map("ResourceInfo" -> 1, "PipeParams" -> 1, "JobPhase" -> 1, "NodeInfo" -> 1)
    )

  private lazy val schema: String =
    def findUp(from: File, depth: Int): Option[File] =
      val candidate = File(from, "reference/xjdf/schema.xsd")
      if candidate.isFile then Some(candidate)
      else if depth == 0 || from.getParentFile == null then None
      else findUp(from.getParentFile, depth - 1)
    val file = findUp(File(".").getAbsoluteFile, 5).getOrElse(
      fail("reference/xjdf/schema.xsd not found from " + File(".").getAbsolutePath)
    )
    val source = Source.fromFile(file, "UTF-8")
    try source.mkString
    finally source.close()

  private def schemaSlice(startToken: String, endToken: String): String =
    val start = schema.indexOf(startToken)
    assert(start >= 0, s"$startToken is missing from schema.xsd")
    val end = schema.indexOf(endToken, start)
    assert(end > start, s"$startToken is not closed by $endToken in schema.xsd")
    schema.substring(start, end + endToken.length)

  private lazy val misDetailsSchema: String =
    schemaSlice("<xs:element name=\"MISDetails\">", "</xs:element>")

  private def containerSchema(name: String): String =
    if name == "NodeInfo" then
      schemaSlice("<xs:complexType name=\"NodeInfo\">", "</xs:complexType>")
    else
      schemaSlice(s"<xs:element name=\"$name\">", "</xs:element>")

end MISDetailsLaws
