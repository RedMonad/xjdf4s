package xjdf4s.laws

import xjdf4s.model.{XPath as ValidationXPath}
import xjdf4s.model.elements.Expr
import xjdf4s.prim.*
import cats.Show
import cats.data.Chain
import cats.kernel.Eq
import munit.FunSuite

import java.io.File
import scala.io.Source

/** Tests for the XJDF `XPath` data type (Appendix A / Table A.1) and the
 *  `Expr` element (§8.29.1 / Table 8.47), M1.6-6b/B1.
 *
 *  `XjdfXPath` is deliberately distinct from the validation locator
 *  `model.XPath`. The prose/XSD base-type disagreement is pinned by an oracle
 *  test and resolved by ADR-0013 (N-54).
 */
class XjdfXPathExprLaws extends FunSuite:

  test("Table A.1: XjdfXPath and model.XPath are distinct domain concepts"):
    val expression: XjdfXPath = XjdfXPath.unsafe("/doc/record/Geschlecht")
    val issueLocation: ValidationXPath = ValidationXPath("/XJDF/ResourceSet/Resource/RunList")

    assertEquals(expression.value, "/doc/record/Geschlecht")
    assertEquals(issueLocation.value, "/XJDF/ResourceSet/Resource/RunList")

  test("Table A.1 / xsd:token: XjdfXPath collapses XML whitespace"):
    val expression = XjdfXPath.from(" \t/doc/record  [@id = '42']\r\n ")
    assertEquals(expression.map(_.value), Some("/doc/record [@id = '42']"))

  test("Table A.1: an empty XPath expression is rejected by both constructors"):
    assertEquals(XjdfXPath.from(""), None)
    assertEquals(XjdfXPath.from(" \t\r\n "), None)
    assertEquals(XjdfXPath.from(null), None)
    intercept[IllegalArgumentException](XjdfXPath.unsafe("  "))

  test("Table A.1: XjdfXPath has lawful Show and Eq instances"):
    val left = XjdfXPath.unsafe("/doc/record/Status")
    val same = XjdfXPath.unsafe("/doc/record/Status")
    val other = XjdfXPath.unsafe("/doc/record/Geschlecht")

    assertEquals(Show[XjdfXPath].show(left), "/doc/record/Status")
    assert(Eq[XjdfXPath].eqv(left, same))
    assert(!Eq[XjdfXPath].eqv(left, other))

  test("Table 8.47: Expr preserves the required @Name and XJDF @Path"):
    val expression = Expr(
      name = NmToken.unsafe("gender"),
      path = XjdfXPath.unsafe("/doc/record/Geschlecht")
    )

    assertEquals(expression.name.value, "gender")
    assertEquals(expression.path.value, "/doc/record/Geschlecht")
    assertEquals(Show[Expr].show(expression), "Expr(gender=/doc/record/Geschlecht)")
    assert(Eq[Expr].eqv(expression, expression.copy()))

  test("Table 8.47 / §2.2.3: Expr declares no IDREF attributes"):
    val expression = Expr(NmToken.unsafe("status"), XjdfXPath.unsafe("/doc/record/Status"))
    assertEquals(expression.references, Chain.empty[IdRef])

  test("Table 8.47: schema.xsd requires exactly @Name NMTOKEN and @Path XPath"):
    val attribute = raw"""<xs:attribute name="([^"]+)" type="([^"]+)" use="([^"]+)"/>""".r
    val actual = attribute.findAllMatchIn(exprSchema).map { matched =>
      (matched.group(1), matched.group(2), matched.group(3))
    }.toSet

    assertEquals(
      actual,
      Set(
        ("Name", "xs:NMTOKEN", "required"),
        ("Path", "XPath", "required")
      )
    )
    assertEquals(raw"""<xs:element\b""".r.findAllMatchIn(exprSchema).length, 1)

  test("Table A.1 / N-54: prose says xsd:token while schema.xsd derives XPath from xs:string"):
    assert(
      appendixA.contains("| `XPath` | `xsd:token` | None |"),
      "Table A.1 no longer declares XPath as xsd:token — revisit ADR-0013"
    )
    val base = raw"""<xs:restriction base="([^"]+)"/>""".r
    assertEquals(base.findAllMatchIn(xpathSchema).map(_.group(1)).toList, List("xs:string"))

  private lazy val schema: String = loadReference("schema.xsd")

  private lazy val appendixA: String = loadReference("Appendix A – Data Types and Values.md")

  private lazy val exprSchema: String =
    schemaSlice("<xs:element name=\"Expr\">", "</xs:element>")

  private lazy val xpathSchema: String =
    schemaSlice("<xs:simpleType name=\"XPath\">", "</xs:simpleType>")

  private def schemaSlice(startToken: String, endToken: String): String =
    val start = schema.indexOf(startToken)
    assert(start >= 0, s"$startToken is missing from schema.xsd")
    val end = schema.indexOf(endToken, start)
    assert(end > start, s"$startToken is not closed by $endToken in schema.xsd")
    schema.substring(start, end + endToken.length)

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

end XjdfXPathExprLaws
