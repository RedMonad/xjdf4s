package xjdf4s.laws

import java.io.File

import scala.io.Source

import xjdf4s.model.elements.FileSpec
import xjdf4s.prim.Url
import xjdf4s.resources.{CuttingParams, FoldingParams, Layout, Preview}
import munit.FunSuite

/** Regression tests for `FileSpec?` cardinality in Tables 6.53, 6.74, 6.95
 *  and 6.134 (N-58).
 */
class FileSpecCardinalityLaws extends FunSuite:

  private val fileSpec = FileSpec.ofUrl(Url.unsafe("file:///artwork/instructions.pdf"))

  test("Table 6.53 / N-58: CuttingParams carries one optional FileSpec child"):
    val absent: Option[FileSpec] = CuttingParams().fileSpecs
    val present: Option[FileSpec] = CuttingParams(fileSpecs = Some(fileSpec)).fileSpecs

    assertEquals(absent, Option.empty[FileSpec])
    assertEquals(present, Some(fileSpec))

  test("Table 6.74 / N-58: FoldingParams carries one optional FileSpec child"):
    val absent: Option[FileSpec] = FoldingParams().fileSpecs
    val present: Option[FileSpec] = FoldingParams(fileSpecs = Some(fileSpec)).fileSpecs

    assertEquals(absent, Option.empty[FileSpec])
    assertEquals(present, Some(fileSpec))

  test("Table 6.95 / N-58: Layout carries one optional FileSpec child"):
    val absent: Option[FileSpec] = Layout().fileSpecs
    val present: Option[FileSpec] = Layout(fileSpecs = Some(fileSpec)).fileSpecs

    assertEquals(absent, Option.empty[FileSpec])
    assertEquals(present, Some(fileSpec))

  test("Table 6.134 / N-58: Preview carries one optional FileSpec child"):
    val absent: Option[FileSpec] = Preview().fileSpecs
    val present: Option[FileSpec] = Preview(fileSpecs = Some(fileSpec)).fileSpecs

    assertEquals(absent, Option.empty[FileSpec])
    assertEquals(present, Some(fileSpec))

  test("Tables 6.53/6.74/6.95/6.134 / N-58: schema.xsd declares FileSpec with 0..1 cardinality"):
    val actual = List("CuttingParams", "FoldingParams", "Layout", "Preview").map { resourceName =>
      resourceName -> fileSpecOccurrence(complexTypeSchema(resourceName))
    }

    assertEquals(
      actual,
      List(
        "CuttingParams" -> ("0", "1"),
        "FoldingParams" -> ("0", "1"),
        "Layout" -> ("0", "1"),
        "Preview" -> ("0", "1")
      )
    )

  private def fileSpecOccurrence(schema: String): (String, String) =
    val occurrence = raw"""<xs:element maxOccurs="([^"]+)" minOccurs="([^"]+)" ref="FileSpec"/>""".r
    val matches = occurrence.findAllMatchIn(schema).map(m => (m.group(2), m.group(1))).toList
    assertEquals(matches.size, 1, s"expected exactly one FileSpec child in schema slice: $schema")
    matches.head

  private def complexTypeSchema(name: String): String =
    val startToken = s"<xs:complexType name=\"$name\">"
    val endToken = "</xs:complexType>"
    val start = schema.indexOf(startToken)
    assert(start >= 0, s"$startToken is missing from schema.xsd")
    val end = schema.indexOf(endToken, start)
    assert(end > start, s"$startToken is not closed by $endToken in schema.xsd")
    schema.substring(start, end + endToken.length)

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
  end schema

end FileSpecCardinalityLaws
