package xjdf4s.laws

import xjdf4s.model.elements.FileSpec
import xjdf4s.prim.HexBinary
import cats.Show
import cats.kernel.Eq
import munit.FunSuite

import java.io.File
import scala.io.Source

/** Regression and conformance tests for the XJDF `hexBinary` data type
 *  (Appendix A / Table A.1) and `FileSpec/@CheckSum` (Table 8.22), N-57.
 */
class HexBinaryLaws extends FunSuite:

  test("Appendix A / Table A.1: HexBinary accepts even-length upper- and lower-case hexadecimal"):
    val lower = HexBinary.from("d41d8cd98f00b204e9800998ecf8427e")
    val upper = HexBinary.from("D41D8CD98F00B204E9800998ECF8427E")

    assertEquals(lower.map(_.value), Some("d41d8cd98f00b204e9800998ecf8427e"))
    assertEquals(upper.map(_.value), Some("D41D8CD98F00B204E9800998ECF8427E"))

  test("Appendix A / Table A.1: HexBinary accepts the empty lexical representation"):
    assertEquals(HexBinary.from("").map(_.value), Some(""))

  test("Appendix A / Table A.1: HexBinary rejects an odd number of hexadecimal digits"):
    assertEquals(HexBinary.from("abc"), None)
    intercept[IllegalArgumentException](HexBinary.unsafe("abc"))

  test("Appendix A / Table A.1: HexBinary rejects non-hexadecimal characters"):
    assertEquals(HexBinary.from("0g"), None)
    assertEquals(HexBinary.from("00-ff"), None)
    assertEquals(HexBinary.from(null), None)

  test("Appendix A / Table A.1: HexBinary applies the fixed xsd:hexBinary whitespace collapse"):
    assertEquals(HexBinary.from(" \t0aFF\r\n ").map(_.value), Some("0aFF"))
    assertEquals(HexBinary.from("0a FF"), None)

  test("Appendix A / Table A.1: HexBinary has Show, Eq and lexical round-trip"):
    val checksum = HexBinary.unsafe("d41D8cD98f00b204e9800998ecf8427E")
    val roundTripped = HexBinary.from(checksum.value)

    assertEquals(roundTripped, Some(checksum))
    assertEquals(Show[HexBinary].show(checksum), checksum.value)
    assert(Eq[HexBinary].eqv(checksum, HexBinary.unsafe(checksum.value)))
    assert(!Eq[HexBinary].eqv(checksum, HexBinary.unsafe("00")))

  test("Table 8.22 / N-57: FileSpec/@CheckSum is optional xsd:hexBinary in prose, XSD and model"):
    val checksum: HexBinary = HexBinary.unsafe("d41d8cd98f00b204e9800998ecf8427e")
    val modelValue: Option[HexBinary] = FileSpec(checkSum = Some(checksum)).checkSum

    assertEquals(modelValue, Some(checksum))
    assert(
      appendixA.contains("| `hexBinary` | `xsd:hexBinary` | None | Represents arbitrary hex-encoded binary data. |"),
      "Table A.1 no longer maps hexBinary to xsd:hexBinary"
    )
    assert(
      subelements.contains("| `@CheckSum?` | hexBinary |"),
      "Table 8.22 no longer declares FileSpec/@CheckSum as hexBinary"
    )
    val checkSumAttribute = raw"""<xs:attribute name="CheckSum" type="([^"]+)" use="([^"]+)"/>""".r
    assertEquals(
      checkSumAttribute.findAllMatchIn(fileSpecSchema).map(m => (m.group(1), m.group(2))).toList,
      List(("xs:hexBinary", "optional"))
    )

  private lazy val schema: String = loadReference("schema.xsd")

  private lazy val appendixA: String = loadReference("Appendix A – Data Types and Values.md")

  private lazy val subelements: String = loadReference("8 – Subelements.md")

  private lazy val fileSpecSchema: String =
    schemaSlice("<xs:element name=\"FileSpec\">", "</xs:element>")

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

end HexBinaryLaws
