package xjdf4s.laws

import java.io.File

import scala.io.Source

import xjdf4s.intents.{IntentPayload, ShapeCut, ShapeCuttingIntent}
import xjdf4s.model.*
import xjdf4s.prim.*
import cats.data.{Chain, NonEmptyChain}
import munit.FunSuite

/** Tests for `ShapeCuttingIntent` and `ShapeCut` (§4.13 / Tables 4.34–4.35),
 *  M1.6-13(B2).
 *
 *  Normative mapping:
 *  - `ShapeCut+` -> `NonEmptyChain[ShapeCut]` (`schema.xsd`
 *    `minOccurs="1" maxOccurs="unbounded"`);
 *  - `@CutBox?` -> `Rectangle`, while `@CutPath?` -> the nominally distinct
 *    `PDFPath` introduced in B1;
 *  - `@CutDepth?`, `@CutType?` and required `@ShapeType` -> the three closed
 *    inline enumerations of Table 4.35;
 *  - neither table declares an ID or IDREF;
 *  - `@ShapeType="Path"` SHOULD provide `@CutPath` or `@ShapeTypeDetails` — a
 *    warning under ADR-0006, not an invalidating error;
 *  - the SHALL clauses of `@CutOut` define what each boolean value means to a
 *    producer; both values are valid document states.
 */
class ShapeCuttingIntentLaws extends FunSuite:

  private val rectangular: ShapeCut =
    ShapeCut(
      cutBox = Some(Rectangle.unsafe(0.0, 0.0, 100.0, 50.0)),
      cutDepth = Some(CutDepth.Full),
      cutOut = Some(true),
      cutType = Some(CutType.Cut),
      shapeType = ShapeCutType.Rectangular,
      shapeTypeDetails = Some(XjdfString.unsafe("EnvelopeWindow"))
    )

  private val path: ShapeCut =
    ShapeCut(
      cutDepth = Some(CutDepth.Partial),
      cutOut = Some(false),
      cutPath = Some(PDFPath.unsafe("0 0 m 50 0 l 50 50 l 0 50 l h")),
      cutType = Some(CutType.Perforate),
      shapeType = ShapeCutType.Path
    )

  private def payloadOf(shapeCuts: NonEmptyChain[ShapeCut]): IntentPayload =
    IntentPayload.ShapeCutting(ShapeCuttingIntent(shapeCuts))

  private def intentOf(shapeCuts: NonEmptyChain[ShapeCut]): Intent =
    val payload = payloadOf(shapeCuts)
    Intent(IntentName.of(payload.elementName), payload)

  private def ticketWith(intent: Intent): XJDF =
    val product = Product(
      id = Some(Id.unsafe("P1")),
      isRoot = true,
      amount = Some(1L),
      intents = Chain.one(intent)
    )
    XJDF(
      jobId = JobId.unsafe("ShapeCuttingLaws"),
      types = NonEmptyChain.one(ProcessType.ShapeCutting),
      productList = Some(ProductList(NonEmptyChain.one(product)))
    )

  test("Table 4.34: payload element name is ShapeCuttingIntent"):
    assertEquals(payloadOf(NonEmptyChain.one(rectangular)).elementName, NmToken.unsafe("ShapeCuttingIntent"))

  test("Tables 4.34–4.35 / §2.2.3: reference traversal reaches every ShapeCut and finds no IDREFs"):
    assertEquals(rectangular.references, Chain.empty[IdRef])
    assertEquals(path.references, Chain.empty[IdRef])
    assertEquals(payloadOf(NonEmptyChain(rectangular, path)).references, Chain.empty[IdRef])

  test("Table 4.34: ShapeCut+ is represented by a NonEmptyChain"):
    val intent = ShapeCuttingIntent(NonEmptyChain(rectangular, path))
    assertEquals(intent.shapeCuts.toChain.toList, List(rectangular, path))

  test("Table 4.35: all seven ShapeCut attributes map to their domain types"):
    val cut = rectangular.copy(cutPath = path.cutPath)
    assertEquals(cut.cutBox.map(_.width), Some(100.0))
    assertEquals(cut.cutDepth, Some(CutDepth.Full))
    assertEquals(cut.cutOut, Some(true))
    assertEquals(cut.cutPath.map(_.value), Some("0 0 m 50 0 l 50 50 l 0 50 l h"))
    assertEquals(cut.cutType, Some(CutType.Cut))
    assertEquals(cut.shapeType, ShapeCutType.Rectangular)
    assertEquals(cut.shapeTypeDetails.map(_.value), Some("EnvelopeWindow"))

  test("Table 4.35: @CutBox is Rectangle and @CutPath is the distinct PDFPath type"):
    assertEquals(rectangular.cutBox.map(box => (box.llx, box.lly, box.urx, box.ury)), Some((0.0, 0.0, 100.0, 50.0)))
    assertEquals(path.cutPath.map(_.value), Some("0 0 m 50 0 l 50 50 l 0 50 l h"))

  test("Table 4.35: inline enumeration wire tokens are exact golden sets"):
    assertEquals(CutDepth.all.map(_.token.value), List("Full", "Partial"))
    assertEquals(CutType.all.map(_.token.value), List("Cut", "Perforate"))
    assertEquals(
      ShapeCutType.all.map(_.token.value),
      List("Line", "Path", "Rectangular", "Round", "RoundedRectangle")
    )

  test("Tables 4.34–4.35: a complete ShapeCuttingIntent validates without findings"):
    val report = ticketWith(intentOf(NonEmptyChain(rectangular, path))).validateReport
    assert(report.isValid)
    assertEquals(report.errors, Chain.empty[Issue])
    assertEquals(report.warnings, Chain.empty[Issue])

  test("Tables 4.1/4.34: a mismatched Intent/@Name is rejected"):
    val payload = payloadOf(NonEmptyChain.one(rectangular))
    val report = ticketWith(Intent(IntentName.unsafe("MediaIntent"), payload)).validateReport
    assert(!report.isValid)
    assert(report.errors.toList.exists(_.code.contains(IssueCode.IntentNameMismatch)))

  test("Table 4.35 SHALL semantics: both @CutOut values are lawful document states"):
    List(true, false).foreach: cutOut =>
      val cut = ShapeCut(cutOut = Some(cutOut), shapeType = ShapeCutType.Rectangular)
      assertEquals(ShapeCut.law.check(cut, XPath("/ShapeCut")), Chain.empty[Issue])

  test("Table 4.35 SHOULD: Path without @CutPath or @ShapeTypeDetails produces a non-invalidating warning"):
    val underspecified = ShapeCut(shapeType = ShapeCutType.Path)
    val report = ticketWith(intentOf(NonEmptyChain.one(underspecified))).validateReport
    assert(report.isValid)
    assertEquals(report.errors, Chain.empty[Issue])
    assertEquals(report.warnings.toList.map(_.code), List(Some(IssueCode.ShapeCutPathDetailsRecommended)))
    assertEquals(report.warnings.toList.map(_.severity), List(SeverityClass.Warning))
    assertEquals(
      report.warnings.toList.map(_.location.value),
      List("/XJDF/ProductList/Product[@ID='P1']/Intent[@Name='ShapeCuttingIntent']/ShapeCut[0]")
    )
    assert(!report.withWarningsAsErrors.isValid)

  test("Table 4.35 SHOULD: @CutPath supplies the recommended Path details"):
    val report = ticketWith(intentOf(NonEmptyChain.one(path))).validateReport
    assert(report.isValid)
    assertEquals(report.warnings, Chain.empty[Issue])

  test("Table 4.35 SHOULD: @ShapeTypeDetails supplies the recommended Path details"):
    val detailed = ShapeCut(
      shapeType = ShapeCutType.Path,
      shapeTypeDetails = Some(XjdfString.unsafe("VendorKnifePath"))
    )
    val report = ticketWith(intentOf(NonEmptyChain.one(detailed))).validateReport
    assert(report.isValid)
    assertEquals(report.warnings, Chain.empty[Issue])

  test("Table 4.35 SHOULD: non-Path shapes do not require path details"):
    val cuts = NonEmptyChain.of(
      ShapeCut(shapeType = ShapeCutType.Line),
      ShapeCut(shapeType = ShapeCutType.Round),
      ShapeCut(shapeType = ShapeCutType.RoundedRectangle)
    )
    val report = ticketWith(intentOf(cuts)).validateReport
    assert(report.isValid)
    assertEquals(report.warnings, Chain.empty[Issue])

  test("Table 4.35: schema.xsd declares the exact attribute set, types and optionality"):
    val named = raw"""<xs:attribute name="([^"]+)" type="([^"]+)" use="([^"]+)"/>""".r
    val inline = raw"""<xs:attribute name="([^"]+)" use="([^"]+)">""".r
    val namedAttrs = named.findAllMatchIn(shapeCutSchema).map { m =>
      (m.group(1), m.group(2), m.group(3))
    }.toSet
    val inlineAttrs = inline.findAllMatchIn(shapeCutSchema).map { m =>
      (m.group(1), m.group(2))
    }.toSet
    assertEquals(
      namedAttrs,
      Set(
        ("CutBox", "rectangle", "optional"),
        ("CutOut", "xs:boolean", "optional"),
        ("CutPath", "PDFPath", "optional"),
        ("ShapeTypeDetails", "xs:string", "optional")
      )
    )
    assertEquals(
      inlineAttrs,
      Set(("CutDepth", "optional"), ("CutType", "optional"), ("ShapeType", "required"))
    )

  test("Table 4.35: schema.xsd inline enumerations match each closed domain enum"):
    assertEquals(inlineEnumeration("CutDepth"), CutDepth.all.map(_.token.value))
    assertEquals(inlineEnumeration("CutType"), CutType.all.map(_.token.value))
    assertEquals(inlineEnumeration("ShapeType"), ShapeCutType.all.map(_.token.value))

  test("Tables 4.34–4.35: prose and schema.xsd agree on cardinality and geometry types"):
    assert(productIntent.contains("| ShapeCut+ | element |"))
    assert(productIntent.contains("| CutBox? | rectangle |"))
    assert(productIntent.contains("| CutPath? | PDFPath |"))
    assert(
      shapeCuttingIntentSchema.contains(
        "<xs:element maxOccurs=\"unbounded\" minOccurs=\"1\" ref=\"ShapeCut\"/>"
      )
    )

  private lazy val schema: String = loadReference("schema.xsd")

  private lazy val productIntent: String = loadReference("4 – Product Intent.md")

  private lazy val shapeCutSchema: String =
    schemaSlice(schema, "<xs:element name=\"ShapeCut\">", "</xs:element>")

  private lazy val shapeCuttingIntentSchema: String =
    schemaSlice(schema, "<xs:complexType name=\"ShapeCuttingIntent\">", "</xs:complexType>")

  private def inlineEnumeration(attributeName: String): List[String] =
    val attribute = schemaSlice(
      shapeCutSchema,
      s"<xs:attribute name=\"$attributeName\"",
      "</xs:attribute>"
    )
    raw"""<xs:enumeration value="([^"]+)"/>""".r
      .findAllMatchIn(attribute)
      .map(_.group(1))
      .toList

  private def schemaSlice(source: String, startToken: String, endToken: String): String =
    val start = source.indexOf(startToken)
    assert(start >= 0, s"$startToken is missing from schema.xsd")
    val end = source.indexOf(endToken, start)
    assert(end > start, s"$startToken is not closed by $endToken in schema.xsd")
    source.substring(start, end + endToken.length)

  private def loadReference(name: String): String =
    def findUp(from: File, depth: Int): Option[File] =
      val candidate = File(from, s"reference/xjdf/$name")
      if candidate.isFile then Some(candidate)
      else if depth == 0 || from.getParentFile == null then None
      else findUp(from.getParentFile, depth - 1)

    val file = findUp(File(".").getAbsoluteFile, 5).getOrElse(
      fail(s"reference/xjdf/$name not found from " + File(".").getAbsolutePath)
    )
    val source = Source.fromFile(file, "UTF-8")
    try source.mkString
    finally source.close()
  end loadReference

end ShapeCuttingIntentLaws
