package xjdf4s.laws

import java.io.File

import scala.io.Source

import xjdf4s.model.*
import xjdf4s.model.elements.{GangSource, MISDetails}
import xjdf4s.prim.*
import xjdf4s.resources.{NodeInfo, ResourcePayload}
import cats.data.{Chain, NonEmptyChain}
import cats.kernel.Eq
import munit.FunSuite

/** Tests for the `NodeInfo` resource (§6.59 / Table 6.119) after it gained its
 *  two child elements — `GangSource*` and `MISDetails?` (M1.6-8, PR-25).
 *
 *  Table 6.119 declares fifteen optional attributes and the two children;
 *  `schema.xsd` agrees on every name and on `use="optional"` throughout, so an
 *  empty `<NodeInfo/>` is valid and the resource carries no local SHALL rule.
 *  The normative facts pinned here are the two cardinalities, the closed
 *  `@DueLevel` enumeration (N-52) and the absence of IDREFs.
 */
class NodeInfoLaws extends FunSuite:

  private val sourceJob = GangSource(copies = 500L, jobId = JobId.unsafe("SourceJob-42"))
  private val details = MISDetails(workType = Some(WorkType.Rework))

  test("Table 6.119: GangSource* maps to a Chain that preserves order and multiplicity"):
    val second = GangSource(copies = 250L, jobId = JobId.unsafe("SourceJob-43"))
    val info = NodeInfo(gangSources = Chain(sourceJob, second))
    assertEquals(info.gangSources.toList, List(sourceJob, second))
    // `*` admits zero, so the default stays empty rather than being required.
    assertEquals(NodeInfo().gangSources, Chain.empty[GangSource])

  test("Table 6.119: MISDetails? maps to an Option, absent by default"):
    assertEquals(NodeInfo().misDetails, None)
    assertEquals(NodeInfo(misDetails = Some(details)).misDetails, Some(details))

  test("Table 6.119: the fifteen attributes and both children are all optional"):
    // No local SHALL rule exists, so an empty NodeInfo is a valid element.
    val empty = NodeInfo()
    assertEquals(empty.cleanupDuration, None)
    assertEquals(empty.dueLevel, None)
    assertEquals(empty.end, None)
    assertEquals(empty.firstEnd, None)
    assertEquals(empty.firstStart, None)
    assertEquals(empty.jobPriority, None)
    assertEquals(empty.lastEnd, None)
    assertEquals(empty.lastStart, None)
    assertEquals(empty.naturalLang, None)
    assertEquals(empty.personalId, None)
    assertEquals(empty.setupDuration, None)
    assertEquals(empty.start, None)
    assertEquals(empty.status, None)
    assertEquals(empty.statusDetails, None)
    assertEquals(empty.totalDuration, None)
    assertEquals(empty.gangSources, Chain.empty[GangSource])
    assertEquals(empty.misDetails, None)

  test("Table 6.119 / N-52: @DueLevel is the closed enumeration, not a bare integer"):
    // Prose gives three values (JobCancelled, Penalty, Trivial) and schema.xsd
    // declares exactly those inline — the attribute was typed Option[Long]
    // until M1.6-8, which made `DueLevel = 7` representable.
    assertEquals(DueLevel.all.map(_.token.value), List("JobCancelled", "Penalty", "Trivial"))
    val info = NodeInfo(dueLevel = Some(DueLevel.Penalty))
    assertEquals(info.dueLevel, Some(DueLevel.Penalty))
    assertEquals(DueLevel.fromToken(NmToken.unsafe("Trivial")), Some(DueLevel.Trivial))
    assertEquals(DueLevel.fromToken(NmToken.unsafe("7")), None)

  test("Table 6.119 / §2.2.3: NodeInfo collects no IDREFs, children included"):
    // Neither Table 6.119 nor its two children declare an IDREF attribute; the
    // chains are still walked so the fact is checked, not assumed.
    val info = NodeInfo(
      personalId = Some(NmToken.unsafe("Operator-7")),
      gangSources = Chain.one(sourceJob),
      misDetails = Some(details)
    )
    assertEquals(info.references, Chain.empty[IdRef])
    // @PersonalID names a Resource/@ExternalID, which is not an @ID — so it is
    // no more an IDREF than GangSource/@JobID is (Deliberate Deviations).
    assertEquals(ResourcePayload.NodeInfoResource(info).references, Chain.empty[IdRef])

  test("Table 6.119: the NodeInfo payload reaches the root reference walk"):
    // A NodeInfo carrying children must not disturb XJDF.references, which is
    // what the payload dispatch of AllResources now delegates to.
    val info = NodeInfo(gangSources = Chain.one(sourceJob), misDetails = Some(details))
    val ticket = XJDF(
      jobId = JobId.unsafe("NodeInfoJob"),
      types = NonEmptyChain.one(ProcessType.Cutting),
      resourceSets = Chain.one(
        ResourceSet(
          ResourceSetName.unsafe("NodeInfo"),
          usage = Some(Usage.Input),
          resources = Chain.one(Resource.of(ResourcePayload.NodeInfoResource(info)))
        )
      )
    )
    assertEquals(ticket.references, Chain.empty[IdRef])
    assert(ticket.validate.isValid, "a NodeInfo with both children validates")

  test("Table 6.119: plannedWindow is unaffected by the new children"):
    val info = NodeInfo(
      firstStart = Some(Timestamp.ofEpochSecond(1000)),
      lastEnd = Some(Timestamp.ofEpochSecond(2000)),
      gangSources = Chain.one(sourceJob),
      misDetails = Some(details)
    )
    assertEquals(info.plannedWindow.map(_.duration.seconds), Some(1000L))
    assertEquals(NodeInfo(gangSources = Chain.one(sourceJob)).plannedWindow, None)

  test("Table 6.119: NodeInfo equality covers both new children"):
    val a = NodeInfo(gangSources = Chain.one(sourceJob), misDetails = Some(details))
    val b = NodeInfo(gangSources = Chain.one(sourceJob), misDetails = Some(details))
    val c = NodeInfo(gangSources = Chain.one(sourceJob))
    val d = NodeInfo(misDetails = Some(details))
    assert(Eq[NodeInfo].eqv(a, b))
    assert(!Eq[NodeInfo].eqv(a, c))
    assert(!Eq[NodeInfo].eqv(a, d))

  test("Table 6.119: schema.xsd declares GangSource* and MISDetails? as the only children"):
    val child = raw"""<xs:element maxOccurs="([^"]+)" minOccurs="([^"]+)" ref="([^"]+)"/>""".r
    val actual = child.findAllMatchIn(nodeInfoSchema).map { m =>
      (m.group(3), m.group(2), m.group(1))
    }.toList
    assertEquals(
      actual,
      List(
        ("GangSource", "0", "unbounded"),
        ("MISDetails", "0", "1")
      )
    )

  test("Table 6.119: schema.xsd declares the exact attribute set, all optional"):
    val named = raw"""<xs:attribute name="([^"]+)" type="([^"]+)" use="([^"]+)"/>""".r
    val inline = raw"""<xs:attribute name="([^"]+)" use="([^"]+)">""".r
    val namedAttrs = named.findAllMatchIn(nodeInfoSchema).map { m =>
      (m.group(1), m.group(2), m.group(3))
    }.toSet
    val inlineAttrs = inline.findAllMatchIn(nodeInfoSchema).map { m =>
      (m.group(1), m.group(2))
    }.toSet
    assertEquals(
      namedAttrs,
      Set(
        ("CleanupDuration", "xs:duration", "optional"),
        ("End", "xs:dateTime", "optional"),
        ("FirstEnd", "xs:dateTime", "optional"),
        ("FirstStart", "xs:dateTime", "optional"),
        ("JobPriority", "xs:int", "optional"),
        ("LastEnd", "xs:dateTime", "optional"),
        ("LastStart", "xs:dateTime", "optional"),
        ("NaturalLang", "language", "optional"),
        ("PersonalID", "xs:NMTOKEN", "optional"),
        ("SetupDuration", "xs:duration", "optional"),
        ("Start", "xs:dateTime", "optional"),
        ("Status", "NodeStatus", "optional"),
        ("StatusDetails", "xs:NMTOKEN", "optional"),
        ("TotalDuration", "xs:duration", "optional")
      )
    )
    // @DueLevel is the single inline enumeration of the complexType.
    assertEquals(inlineAttrs, Set(("DueLevel", "optional")))

  test("Table 6.119 / N-52: schema.xsd inline enumeration matches the closed DueLevel enum"):
    val enumeration = raw"""<xs:enumeration value="([^"]+)"/>""".r
    val declared = enumeration.findAllMatchIn(nodeInfoSchema).map(_.group(1)).toSet
    assertEquals(declared, DueLevel.all.map(_.token.value).toSet)

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

  private lazy val nodeInfoSchema: String =
    val startToken = "<xs:complexType name=\"NodeInfo\">"
    val endToken = "</xs:complexType>"
    val start = schema.indexOf(startToken)
    assert(start >= 0, s"$startToken is missing from schema.xsd")
    val end = schema.indexOf(endToken, start)
    assert(end > start, s"$startToken is not closed by $endToken in schema.xsd")
    schema.substring(start, end + endToken.length)

end NodeInfoLaws
