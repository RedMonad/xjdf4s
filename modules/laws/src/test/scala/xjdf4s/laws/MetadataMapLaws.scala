package xjdf4s.laws

import java.io.File

import scala.io.Source

import xjdf4s.model.*
import xjdf4s.model.elements.{Expr, IdentificationField, MetadataMap}
import xjdf4s.prim.*
import xjdf4s.resources.{Component, ResourcePayload, RunList}
import cats.Show
import cats.data.{Chain, NonEmptyChain}
import cats.kernel.Eq
import munit.FunSuite

/** Context and mapping tests for `MetadataMap` (§8.29 / Table 8.46),
 *  including literal Example 8.6 and the ADR-0014-conformant adaptation of
 *  Example 8.7.
 */
class MetadataMapLaws extends FunSuite:

  private def tokens(head: String, tail: String*): NmTokens =
    NmTokens.fromStrings(head, tail*).getOrElse(fail("invalid NMTOKENS fixture"))

  private def mapping(
      name: String,
      template: NmTokens,
      expressions: Chain[Expr] = Chain.empty
  ): MetadataMap =
    MetadataMap(
      name = NmToken.unsafe(name),
      valueFormat = XjdfString.unsafe("%s"),
      valueTemplate = template,
      expressions = expressions
    )

  private def ticket(payload: ResourcePayload, name: String): XJDF =
    XJDF(
      jobId = JobId.unsafe("MetadataMapLaws"),
      types = NonEmptyChain.one(ProcessType.Cutting),
      resourceSets = Chain.one(ResourceSet(
        name = ResourceSetName.unsafe(name),
        usage = Some(Usage.Input),
        resources = Chain.one(Resource.of(payload))
      ))
    )

  private def componentTicket(field: IdentificationField): XJDF =
    ticket(ResourcePayload.ComponentResource(Component(identificationFields = Chain.one(field))), "Component")

  private def runListTicket(map: MetadataMap): XJDF =
    ticket(ResourcePayload.RunListResource(RunList(metadataMaps = Chain.one(map))), "RunList")

  private def codes(value: XJDF): List[IssueCode] =
    value.validateReport.errors.toList.flatMap(_.code)

  test("Table 8.46: all three attributes are required by the Scala type and Expr has 0..* cardinality"):
    val value = mapping("Metadata", tokens("gender", "status"))
    assertEquals(value.name.value, "Metadata")
    assertEquals(value.valueFormat.value, "%s")
    assertEquals(value.valueTemplate.toList.map(_.value), List("gender", "status"))
    assertEquals(value.expressions, Chain.empty[Expr])
    assertEquals(value.references, Chain.empty[IdRef])
    assertEquals(Show[MetadataMap].show(value), "MetadataMap(Metadata=gender status)")
    assert(Eq[MetadataMap].eqv(value, value.copy()))

  test("Table 8.46 / schema.xsd: exact required attributes, Expr* and no IDREF"):
    val attribute = raw"""<xs:attribute name="([^"]+)" type="([^"]+)" use="([^"]+)"/>""".r
    assertEquals(
      attribute.findAllMatchIn(metadataMapSchema).map(m => (m.group(1), m.group(2), m.group(3))).toSet,
      Set(
        ("Name", "xs:NMTOKEN", "required"),
        ("ValueFormat", "xs:string", "required"),
        ("ValueTemplate", "xs:NMTOKENS", "required")
      )
    )
    assert(metadataMapSchema.contains("maxOccurs=\"unbounded\" minOccurs=\"0\" ref=\"Expr\""))
    assert(!metadataMapSchema.contains("xs:IDREF"))

  test("Tables 6.148/8.31: schema.xsd gives both MetadataMap containers 0..* cardinality"):
    val occurrence = raw"""<xs:element maxOccurs="unbounded" minOccurs="0" ref="MetadataMap"/>""".r
    assertEquals(occurrence.findAllMatchIn(runListSchema).length, 1)
    assertEquals(occurrence.findAllMatchIn(identificationFieldSchema).length, 1)

  test("Table 8.31 SHALL: MetadataMap/@Name must occur in parent IdentificationField/@ValueTemplate"):
    val field = IdentificationField(
      valueFormat = Some(XjdfString.unsafe("%s")),
      valueTemplate = Some(tokens("job")),
      metadataMaps = Chain.one(mapping("JobID", tokens("job")))
    )
    assertEquals(codes(componentTicket(field)), List(IssueCode.MetadataMapNameNotInParentTemplate))

  test("Table 8.46 SHALL: every IdentificationField/MetadataMap variable must occur in the parent template"):
    val field = IdentificationField(
      valueFormat = Some(XjdfString.unsafe("%s")),
      valueTemplate = Some(tokens("JobID", "job")),
      metadataMaps = Chain.one(mapping("JobID", tokens("missing")))
    )
    assertEquals(codes(componentTicket(field)), List(IssueCode.MetadataMapVariableNotInParentTemplate))

  test("Table 8.46 SHALL: Expr is forbidden in IdentificationField/MetadataMap"):
    val expression = Expr(NmToken.unsafe("job"), XjdfXPath.unsafe("/barcode/job"))
    val field = IdentificationField(
      valueFormat = Some(XjdfString.unsafe("%s")),
      valueTemplate = Some(tokens("JobID", "job")),
      metadataMaps = Chain.one(mapping("JobID", tokens("job"), Chain.one(expression)))
    )
    assertEquals(codes(componentTicket(field)), List(IssueCode.MetadataMapExprForbiddenInIdentificationField))

  test("Table 8.46 SHALL: a non-predefined RunList variable requires one matching Expr"):
    val value = mapping("Metadata", tokens("gender"))
    assertEquals(codes(runListTicket(value)), List(IssueCode.MetadataMapExprResolution))

  test("Table 8.46 SHALL: duplicate matching Expr elements are rejected"):
    val expression = Expr(NmToken.unsafe("gender"), XjdfXPath.unsafe("/doc/record/Geschlecht"))
    val value = mapping("Metadata", tokens("gender"), Chain(expression, expression))
    assertEquals(codes(runListTicket(value)), List(IssueCode.MetadataMapExprResolution))

  test("Table D.1: predefined variables and Partition Keys need no Expr"):
    List("JobID", "DocIndex", "GeneralID:Campaign").foreach { variable =>
      assert(runListTicket(mapping("Metadata", tokens(variable))).validate.isValid, variable)
    }

  test("Example 8.6: RunList metadata variables are resolved by exactly one Expr each"):
    val value = MetadataMap(
      NmToken.unsafe("Metadata"),
      XjdfString.unsafe("%s_%s"),
      tokens("gender", "status"),
      Chain(
        Expr(NmToken.unsafe("gender"), XjdfXPath.unsafe("/doc/record/Geschlecht")),
        Expr(NmToken.unsafe("status"), XjdfXPath.unsafe("/doc/record/Status"))
      )
    )
    assert(runListTicket(value).validate.isValid)

  test("Example 8.7 / ADR-0014: adapted parent template feeds three MetadataMap elements without Expr"):
    val field = IdentificationField(
      valueFormat = Some(XjdfString.unsafe("%6s%3i%2i%s%s%s")),
      valueTemplate = Some(tokens("job", "doc", "sheet", "JobID", "DocIndex", "SheetIndex")),
      metadataMaps = Chain(
        MetadataMap(NmToken.unsafe("JobID"), XjdfString.unsafe("Job_%s"), tokens("job")),
        MetadataMap(NmToken.unsafe("DocIndex"), XjdfString.unsafe("%i%i"), tokens("doc", "doc")),
        MetadataMap(NmToken.unsafe("SheetIndex"), XjdfString.unsafe("%i%i"), tokens("sheet", "sheet"))
      )
    )
    assert(componentTicket(field).validate.isValid)

  private lazy val schema: String =
    def findUp(from: File, depth: Int): Option[File] =
      val candidate = File(from, "reference/xjdf/schema.xsd")
      if candidate.isFile then Some(candidate)
      else if depth == 0 || from.getParentFile == null then None
      else findUp(from.getParentFile, depth - 1)
    val file = findUp(File(".").getAbsoluteFile, 5).getOrElse(fail("reference/xjdf/schema.xsd not found"))
    val source = Source.fromFile(file, "UTF-8")
    try source.mkString
    finally source.close()

  private def schemaSlice(startToken: String, endToken: String): String =
    val start = schema.indexOf(startToken)
    assert(start >= 0, s"$startToken missing")
    val end = schema.indexOf(endToken, start)
    assert(end > start, s"$startToken not closed")
    schema.substring(start, end + endToken.length)

  private lazy val metadataMapSchema = schemaSlice("<xs:element name=\"MetadataMap\">", "</xs:element>")
  private lazy val runListSchema = schemaSlice("<xs:complexType name=\"RunList\">", "</xs:complexType>")
  private lazy val identificationFieldSchema =
    schemaSlice("<xs:element name=\"IdentificationField\">", "</xs:element>")

end MetadataMapLaws
