package xjdf4s.laws

import java.io.File

import scala.io.Source

import xjdf4s.prim.PDFPath
import cats.Show
import cats.kernel.Eq
import munit.FunSuite

/** Tests for the XJDF `PDFPath` data type (§A.1 / Table A.1),
 *  M1.6-13(B1).
 *
 *  Full PDF 1.6 path grammar validation belongs to M2.3. These tests pin the
 *  conservative M1 boundary and the matching `xs:string` XSD declaration.
 */
class PDFPathLaws extends FunSuite:

  test("§A.1 / Table A.1: PDFPath accepts PDF 1.6 path-construction sequences"):
    val paths = List(
      "0 0 m 100 0 l 100 100 l 0 100 l h",
      "10 20 30 40 re",
      "0 0 m 10 10 20 20 30 30 c"
    )

    assertEquals(paths.flatMap(PDFPath.from).map(_.value), paths)

  test("§A.1 / Table A.1: PDFPath preserves its xsd:string lexical representation"):
    val raw = "  0\t0 m\n100 100 l  "
    assertEquals(PDFPath.from(raw).map(_.value), Some(raw))

  test("§A.1 / Table A.1: PDFPath rejects null, empty and whitespace-only values"):
    assertEquals(PDFPath.from(null), None)
    assertEquals(PDFPath.from(""), None)
    assertEquals(PDFPath.from(" \t\r\n "), None)
    intercept[IllegalArgumentException](PDFPath.unsafe("  "))

  test("§A.1 / Table A.1: PDFPath has Show, Eq and lexical round-trip"):
    val path = PDFPath.unsafe("0 0 m 72 72 l h")
    val same = PDFPath.unsafe("0 0 m 72 72 l h")
    val other = PDFPath.unsafe("0 0 72 72 re")

    assertEquals(PDFPath.from(path.value), Some(path))
    assertEquals(Show[PDFPath].show(path), path.value)
    assert(Eq[PDFPath].eqv(path, same))
    assert(!Eq[PDFPath].eqv(path, other))

  test("§A.1 / Table A.1: M1 validation deliberately does not parse PDF operators"):
    val notAPathConstructionSequence = "0 0 q"
    assertEquals(PDFPath.from(notAPathConstructionSequence).map(_.value), Some(notAPathConstructionSequence))

  test("§A.1 / Table A.1: prose and schema.xsd agree on the xsd:string base type"):
    assert(
      appendixA.contains("| `PDFPath` | `xsd:string` | Restriction |"),
      "Table A.1 no longer declares PDFPath as xsd:string — revisit the M1 boundary"
    )

    val base = raw"""<xs:restriction base="([^"]+)"/>""".r
    assertEquals(base.findAllMatchIn(pdfPathSchema).map(_.group(1)).toList, List("xs:string"))

  test("§A.1 / Table A.1: schema.xsd adds no lexical facets to PDFPath"):
    assertEquals(raw"""<xs:(?:pattern|minLength|maxLength|length|enumeration)\b""".r.findAllIn(pdfPathSchema).length, 0)

  private lazy val schema: String = loadReference("schema.xsd")

  private lazy val appendixA: String = loadReference("Appendix A – Data Types and Values.md")

  private lazy val pdfPathSchema: String =
    schemaSlice("<xs:simpleType name=\"PDFPath\">", "</xs:simpleType>")

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
  end loadReference

end PDFPathLaws
