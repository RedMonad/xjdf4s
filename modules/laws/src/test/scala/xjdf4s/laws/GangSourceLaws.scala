package xjdf4s.laws

import java.io.File

import scala.io.Source

import xjdf4s.model.elements.GangSource
import xjdf4s.prim.*
import cats.Show
import cats.data.Chain
import cats.kernel.Eq
import munit.FunSuite

/** Tests for the `GangSource` element (§8.22 / Table 8.27): exact attribute
 *  mapping, cardinalities and ID/IDREF classification (M1.6-4, PR-23).
 *
 *  The two "SHALL reference" statements name an external source XJDF and a
 *  `BinderySignature` in that job. Table 8.27 and `schema.xsd` type both as
 *  NMTOKEN rather than IDREF, so a single-ticket validator cannot resolve
 *  them. This deliberate boundary is recorded in SPEC-COVERAGE (ADR-0006).
 */
class GangSourceLaws extends FunSuite:

  private val sourceJob = JobId.unsafe("SourceJob-42")
  private val signature = NmToken.unsafe("Signature-A")

  test("Table 8.27: a fully populated GangSource preserves all three attributes"):
    val source = GangSource(
      copies = 500L,
      jobId = sourceJob,
      binderySignatureId = Some(signature)
    )
    assertEquals(source.copies, 500L)
    assertEquals(source.jobId, sourceJob)
    assertEquals(source.binderySignatureId, Some(signature))
    assertEquals(
      Show[GangSource].show(source),
      "GangSource(job=SourceJob-42, copies=500, binderySignature=Signature-A)"
    )

  test("Table 8.27: @BinderySignatureID is optional"):
    val source = GangSource(copies = 25L, jobId = sourceJob)
    assertEquals(source.binderySignatureId, None)
    assertEquals(Show[GangSource].show(source), "GangSource(job=SourceJob-42, copies=25)")

  test("Table 8.27 / schema.xsd: @Copies is an integer with no invented positivity restriction"):
    val values = List(Int.MinValue.toLong, -1L, 0L, 1L, Int.MaxValue.toLong)
    assertEquals(values.map(copies => GangSource(copies, sourceJob).copies), values)

  test("Table 8.27: @JobID and @BinderySignatureID enforce their NMTOKEN lexical boundary"):
    assertEquals(JobId.from("Source Job"), None)
    assertEquals(NmToken.from("Signature A"), None)
    assertEquals(JobId.from("SourceJob").map(_.value), Some("SourceJob"))
    assertEquals(NmToken.from("Signature-A").map(_.value), Some("Signature-A"))

  test("Table 8.27 / §2.2.3: cross-document identifiers are not collected as IDREFs"):
    val source = GangSource(500L, sourceJob, Some(signature))
    assertEquals(source.references, Chain.empty[IdRef])

  test("Table 8.27: GangSource equality includes the source job, copies and optional signature"):
    val a = GangSource(500L, sourceJob, Some(signature))
    val b = GangSource(500L, sourceJob, Some(signature))
    val c = GangSource(501L, sourceJob, Some(signature))
    assert(Eq[GangSource].eqv(a, b))
    assert(!Eq[GangSource].eqv(a, c))

  test("Table 8.27: schema.xsd declares the exact attribute types and requiredness"):
    val attribute = raw"""<xs:attribute name="([^"]+)" type="([^"]+)" use="([^"]+)"/>""".r
    val actual = attribute.findAllMatchIn(gangSourceSchema).map { m =>
      (m.group(1), m.group(2), m.group(3))
    }.toSet
    assertEquals(
      actual,
      Set(
        ("BinderySignatureID", "xs:NMTOKEN", "optional"),
        ("Copies", "xs:int", "required"),
        ("JobID", "xs:NMTOKEN", "required")
      )
    )

  test("Table 8.27 containers: schema.xsd declares GangSource* in all four locations"):
    val occurrence = raw"""<xs:element maxOccurs="unbounded" minOccurs="0" ref="GangSource"/>""".r
    val counts = List("JobPhase", "QueueFilter", "QueueEntry", "NodeInfo").map { name =>
      name -> occurrence.findAllMatchIn(containerSchema(name)).length
    }.toMap
    assertEquals(
      counts,
      Map("JobPhase" -> 1, "QueueFilter" -> 1, "QueueEntry" -> 1, "NodeInfo" -> 1)
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

  private lazy val gangSourceSchema: String =
    schemaSlice("<xs:element name=\"GangSource\">", "</xs:element>")

  private def containerSchema(name: String): String =
    if name == "NodeInfo" then
      schemaSlice("<xs:complexType name=\"NodeInfo\">", "</xs:complexType>")
    else
      schemaSlice(s"<xs:element name=\"$name\">", "</xs:element>")

end GangSourceLaws
