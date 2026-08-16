package xjdf4s.laws

import java.io.File

import scala.io.Source

import xjdf4s.model.elements.FileSpec
import xjdf4s.prim.Url
import xjdf4s.resources.RunList
import munit.FunSuite

/** Regression tests for the `RunList` resource (§6.73 / Table 6.148), N-53. */
class RunListLaws extends FunSuite:

  test("Table 6.148 / N-53: RunList carries one optional FileSpec child"):
    val fileSpec = FileSpec.ofUrl(Url.unsafe("file:///artwork/run-list.pdf"))
    val absent: Option[FileSpec] = RunList().fileSpecs
    val present: Option[FileSpec] = RunList(fileSpecs = Some(fileSpec)).fileSpecs

    assertEquals(absent, Option.empty[FileSpec])
    assertEquals(present, Some(fileSpec))

  test("Table 6.148 / N-53: schema.xsd declares FileSpec with 0..1 cardinality"):
    val occurrence = raw"""<xs:element maxOccurs="([^"]+)" minOccurs="([^"]+)" ref="FileSpec"/>""".r
    assertEquals(
      occurrence.findAllMatchIn(runListSchema).map(m => (m.group(2), m.group(1))).toList,
      List(("0", "1"))
    )

  private lazy val runListSchema: String =
    def findUp(from: File, depth: Int): Option[File] =
      val candidate = File(from, "reference/xjdf/schema.xsd")
      if candidate.isFile then Some(candidate)
      else if depth == 0 || from.getParentFile == null then None
      else findUp(from.getParentFile, depth - 1)

    val file = findUp(File(".").getAbsoluteFile, 5).getOrElse(
      fail("reference/xjdf/schema.xsd not found from " + File(".").getAbsolutePath)
    )
    val source = Source.fromFile(file, "UTF-8")
    val schema =
      try source.mkString
      finally source.close()
    val startToken = "<xs:complexType name=\"RunList\">"
    val endToken = "</xs:complexType>"
    val start = schema.indexOf(startToken)
    assert(start >= 0, s"$startToken is missing from schema.xsd")
    val end = schema.indexOf(endToken, start)
    assert(end > start, s"$startToken is not closed by $endToken in schema.xsd")
    schema.substring(start, end + endToken.length)
  end runListSchema

end RunListLaws
