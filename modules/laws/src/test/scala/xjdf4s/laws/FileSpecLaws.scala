package xjdf4s.laws

import xjdf4s.intents.{ContentCheckIntent, IntentPayload, ProofItem}
import xjdf4s.model.*
import xjdf4s.model.elements.{Dependent, Disposition, FileLocation, FileSpec, NetworkHeader}
import xjdf4s.prim.*
import xjdf4s.resources.*
import cats.data.{Chain, NonEmptyChain}
import munit.FunSuite

import java.io.File
import scala.io.Source

/** Regression and conformance tests for `FileSpec` and `NetworkHeader`
 *  (§8.19 / Tables 8.22 and 8.24), N-51.
 *
 *  Table 8.22 defines two local location constraints: the `@URL`/`@UID`
 *  group SHALL NOT be combined with `@FileFormat`/`@FileTemplate`, and the
 *  template attributes SHALL occur as a complete pair. A wholly absent
 *  location is lawful only when the referencing ResourceSet is a pipe; that
 *  parent-sensitive check belongs to `TicketValidator` (ADR-0003).
 */
class FileSpecLaws extends FunSuite:

  private val at = XPath("/XJDF/FileSpec")
  private val url = Url.unsafe("https://files.example.test/job.pdf")
  private val uid = NmToken.unsafe("File-42")
  private val fileFormat = XjdfString.unsafe("https://files.example.test/page-%s.pdf")
  private val fileTemplate = NmTokens.of(NmToken.unsafe("PageNumber"))

  private def templateLocation: FileSpec =
    FileSpec(fileFormat = Some(fileFormat), fileTemplate = Some(fileTemplate))

  private def conflictingLocation: FileSpec =
    templateLocation.copy(url = Some(url))

  private def conflictingLocationAndDisposition: FileSpec =
    conflictingLocation.copy(disposition = Some(Disposition(
      minDuration = Some(TimeSpan.ofHours(24)),
      until = Some(Timestamp.unsafe("2026-09-01T00:00:00+02:00"))
    )))

  private def contentCheckTicket(fileSpec: FileSpec): XJDF =
    val payload = IntentPayload.ContentCheck(
      ContentCheckIntent(proofItems = Chain.one(ProofItem(fileSpec = Some(fileSpec))))
    )
    val product = Product(
      id = Some(Id.unsafe("P1")),
      isRoot = true,
      amount = Some(1L),
      intents = Chain.one(Intent(IntentName.of(payload.elementName), payload))
    )
    XJDF(
      jobId = JobId.unsafe("FileSpecLaws"),
      types = NonEmptyChain(ProcessType.Approval, ProcessType.Preflight),
      productList = Some(ProductList(NonEmptyChain.one(product)))
    )

  private def resourceTicket(
      name: String,
      payload: ResourcePayload,
      dependents: Chain[Dependent] = Chain.empty
  ): XJDF =
    XJDF(
      jobId = JobId.unsafe("FileSpecResourceLaws"),
      types = NonEmptyChain.one(ProcessType.Cutting),
      resourceSets = Chain.one(
        ResourceSet(
          name = ResourceSetName.unsafe(name),
          usage = Some(Usage.Input),
          resources = Chain.one(Resource(specific = Some(payload))),
          dependents = dependents
        )
      )
    )

  private def codesOf(ticket: XJDF): List[IssueCode] =
    ticket.validateReport.errors.toList.flatMap(_.code.toList)

  // --- Local location law --------------------------------------------------

  test("Table 8.22: @URL alone is a lawful FileSpec location"):
    assertEquals(FileSpec.law.check(FileSpec.ofUrl(url), at).toList, Nil)

  test("Table 8.22: @UID alone is a lawful FileSpec location"):
    assertEquals(FileSpec.law.check(FileSpec.ofUid(uid), at).toList, Nil)

  test("Table 8.22: @URL and @UID may coexist — only the template group is excluded"):
    val fileSpec = FileSpec(url = Some(url), uid = Some(uid))
    assertEquals(FileSpec.law.check(fileSpec, at).toList, Nil)
    assertEquals(fileSpec.location, Some(FileLocation.UrlAndUid(url, uid)))

  test("Table 8.22: a complete @FileFormat + @FileTemplate pair is lawful"):
    assertEquals(FileSpec.law.check(templateLocation, at).toList, Nil)
    assertEquals(templateLocation.location, Some(FileLocation.Template(fileFormat, fileTemplate)))

  test("Table 8.22 SHALL NOT: @URL/@UID and template attributes cannot be mixed"):
    val issues = FileSpec.law.check(conflictingLocation, at)
    assertEquals(issues.toList.map(_.code), List(Some(IssueCode.FileSpecLocationGroupsConflict)))
    assertEquals(issues.toList.map(_.severity), List(SeverityClass.Error))
    assertEquals(issues.toList.map(_.location), List(at))
    assertEquals(conflictingLocation.location, None)

  test("Table 8.22 SHALL: @FileFormat without @FileTemplate is rejected"):
    val issues = FileSpec.law.check(FileSpec(fileFormat = Some(fileFormat)), at)
    assertEquals(issues.toList.map(_.code), List(Some(IssueCode.FileSpecTemplateIncomplete)))

  test("Table 8.22 SHALL: @FileTemplate without @FileFormat is rejected"):
    val issues = FileSpec.law.check(FileSpec(fileTemplate = Some(fileTemplate)), at)
    assertEquals(issues.toList.map(_.code), List(Some(IssueCode.FileSpecTemplateIncomplete)))

  test("Table 8.22: a locationless FileSpec is locally pipe-compatible"):
    assertEquals(FileSpec.law.check(FileSpec.pipe, at).toList, Nil)
    assertEquals(FileSpec.pipe.location, Some(FileLocation.Pipe))

  // --- Parent-sensitive pipe rule and root traversal ----------------------

  test("Table 8.22 SHALL: a locationless ProofItem/FileSpec is rejected outside a pipe"):
    val ticket = contentCheckTicket(FileSpec.pipe)
    assert(!ticket.validateReport.isValid)
    assertEquals(codesOf(ticket), List(IssueCode.FileSpecLocationMissing))

  test("Tables 8.22/3.13: a locationless FileSpec is accepted in a ResourceSet pipe"):
    val pipe = Dependent(
      jobId = JobId.unsafe("ProducerJob"),
      pipeId = Some(NmToken.unsafe("Pipe-1"))
    )
    val ticket = resourceTicket(
      "RunList",
      ResourcePayload.RunListResource(RunList(fileSpecs = Some(FileSpec.pipe))),
      dependents = Chain.one(pipe)
    )
    assert(ticket.validateReport.isValid, ticket.validateReport.issues.toList.toString)

  test("Tables 8.22/3.13: a locationless resource FileSpec is rejected when @PipeID is absent"):
    val ticket = resourceTicket(
      "RunList",
      ResourcePayload.RunListResource(RunList(fileSpecs = Some(FileSpec.pipe)))
    )
    assert(!ticket.validateReport.isValid)
    assertEquals(codesOf(ticket), List(IssueCode.FileSpecLocationMissing))

  test("Tables 8.22/8.23: the root traversal reaches every modelled FileSpec-bearing resource"):
    val carriers = List(
      "CuttingParams" -> ResourcePayload.CuttingParamsResource(
        CuttingParams(fileSpecs = Some(conflictingLocationAndDisposition))
      ),
      "FoldingParams" -> ResourcePayload.FoldingParamsResource(
        FoldingParams(fileSpecs = Some(conflictingLocationAndDisposition))
      ),
      "Layout" -> ResourcePayload.LayoutResource(
        Layout(fileSpecs = Some(conflictingLocationAndDisposition))
      ),
      "Preview" -> ResourcePayload.PreviewResource(
        Preview(fileSpecs = Some(conflictingLocationAndDisposition))
      ),
      "RunList" -> ResourcePayload.RunListResource(
        RunList(fileSpecs = Some(conflictingLocationAndDisposition))
      )
    )
    carriers.foreach { (name, payload) =>
      val ticket = resourceTicket(name, payload)
      val codes = codesOf(ticket)
      assert(
        codes.contains(IssueCode.FileSpecLocationGroupsConflict),
        s"FileSpec.law is not reachable through $name: ${ticket.validateReport.issues.toList}"
      )
      assert(
        codes.contains(IssueCode.LocalLawViolation),
        s"Disposition law is not reachable through $name: ${ticket.validateReport.issues.toList}"
      )
    }

  test("Tables 4.24/8.22: the ContentCheck traversal reaches FileSpec.law"):
    val ticket = contentCheckTicket(conflictingLocation)
    assert(!ticket.validateReport.isValid)
    assertEquals(codesOf(ticket), List(IssueCode.FileSpecLocationGroupsConflict))

  // --- NetworkHeader model, cardinality, version and ID/IDREF --------------

  test("§8.19.2 / Table 8.24: NetworkHeader preserves required @Name and @Value strings"):
    val header = NetworkHeader(
      name = XjdfString.unsafe("Authorization"),
      value = XjdfString.unsafe("Bearer opaque-token")
    )
    assertEquals(header.name.value, "Authorization")
    assertEquals(header.value.value, "Bearer opaque-token")

  test("Table 8.22: NetworkHeader* is represented by an ordered Chain"):
    val first = NetworkHeader(XjdfString.unsafe("Authorization"), XjdfString.unsafe("Bearer token"))
    val second = NetworkHeader(XjdfString.unsafe("X-Job-ID"), XjdfString.unsafe("J-42"))
    val fileSpec = FileSpec.ofUrl(url).copy(networkHeaders = Chain(first, second))
    assertEquals(fileSpec.networkHeaders.toList, List(first, second))

  test("Tables 8.22/8.24 / §2.2.3: FileSpec and NetworkHeader declare no IDREF attributes"):
    val header = NetworkHeader(XjdfString.unsafe("X-Trace"), XjdfString.unsafe("trace-42"))
    val fileSpec = FileSpec.ofUrl(url).copy(networkHeaders = Chain.one(header))
    assertEquals(header.references, Chain.empty[IdRef])
    assertEquals(fileSpec.references, Chain.empty[IdRef])

  test("Tables 8.22/8.24: schema.xsd declares Disposition? followed by NetworkHeader*"):
    val child = raw"""<xs:element maxOccurs="([^"]+)" minOccurs="([^"]+)" ref="([^"]+)"/>""".r
    val actual = child.findAllMatchIn(fileSpecSchema).map { matched =>
      (matched.group(3), matched.group(2), matched.group(1))
    }.toList
    assertEquals(actual, List(("Disposition", "0", "1"), ("NetworkHeader", "0", "unbounded")))

  test("§8.19.2 / Table 8.24: schema.xsd requires exactly two string attributes"):
    val attribute = raw"""<xs:attribute name="([^"]+)" type="([^"]+)" use="([^"]+)"/>""".r
    val actual = attribute.findAllMatchIn(networkHeaderSchema).map { matched =>
      (matched.group(1), matched.group(2), matched.group(3))
    }.toSet
    assertEquals(
      actual,
      Set(
        ("Name", "xs:string", "required"),
        ("Value", "xs:string", "required")
      )
    )
    assertEquals(raw"""<xs:element\b""".r.findAllMatchIn(networkHeaderSchema).length, 1)

  test("Table 8.22: NetworkHeader* carries the New in XJDF 2.1 version note"):
    assert(
      subelements.contains("| `NetworkHeader*` *(New in XJDF 2.1)* | element |"),
      "Table 8.22 no longer marks NetworkHeader* as New in XJDF 2.1"
    )
    assert(
      subelements.contains("### 8.19.2 NetworkHeader\n\n*New in XJDF 2.1*"),
      "§8.19.2 no longer carries the New in XJDF 2.1 note"
    )

  // --- Prose/XSD oracles ---------------------------------------------------

  test("Table 8.22: prose pins both local location SHALL clauses"):
    assert(
      subelements.contains(
        "If neither `@URL` nor `@UID` is present, both `@FileFormat` and `@FileTemplate` SHALL be present, " +
          "unless the resource is a pipe. If either `@URL` or `@UID` is specified, then `@FileFormat` and " +
          "`@FileTemplate` SHALL NOT be specified."
      ),
      "FileSpec location clauses changed — revisit N-51"
    )

  test("Table 8.22 / N-56: @NPage exists in prose and release notes but is absent from the XSD FileSpec"):
    assert(
      subelements.contains("| `@NPage?` *(New in XJDF 2.2)* | integer |"),
      "Table 8.22 no longer declares FileSpec/@NPage"
    )
    assert(
      releaseNotes.contains("| @NPage | New | Added attribute to FileSpec. | Table 8.22 FileSpec Element. |"),
      "XJDF 2.2 release notes no longer list FileSpec/@NPage"
    )
    val attributeName = raw"""<xs:attribute name="([^"]+)""".r
    val xsdAttributes = attributeName.findAllMatchIn(fileSpecSchema).map(_.group(1)).toSet
    assert(!xsdAttributes.contains("NPage"), "schema.xsd gained FileSpec/@NPage — revisit ADR-0015")
    assertEquals(FileSpec(nPage = Some(12L)).nPage, Some(12L))

  private lazy val schema: String = loadReference("schema.xsd")
  private lazy val subelements: String = loadReference("8 – Subelements.md")
  private lazy val releaseNotes: String = loadReference("Appendix H – Release Notes.md")

  private lazy val fileSpecSchema: String =
    schemaSlice("<xs:element name=\"FileSpec\">", "</xs:element>")

  private lazy val networkHeaderSchema: String =
    schemaSlice("<xs:element name=\"NetworkHeader\">", "</xs:element>")

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
      fail(s"reference/xjdf/$name not found from ${File(".").getAbsolutePath}")
    )
    val source = Source.fromFile(file, "UTF-8")
    try source.mkString
    finally source.close()

end FileSpecLaws
