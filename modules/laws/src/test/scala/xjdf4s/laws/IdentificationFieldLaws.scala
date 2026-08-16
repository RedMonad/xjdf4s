package xjdf4s.laws

import xjdf4s.model.*
import xjdf4s.model.elements.{BarcodeDetails, ExtraValues, IdentificationField}
import xjdf4s.prim.*
import xjdf4s.resources.{Component, ResourcePayload}
import cats.Show
import cats.data.{Chain, NonEmptyChain}
import cats.kernel.Eq
import munit.FunSuite

import java.io.File
import scala.io.Source

/** Tests for the `IdentificationField` element (§8.26 / Table 8.31) together
 *  with its two modelled children `BarcodeDetails` (§8.26.1 / Table 8.33) and
 *  `ExtraValues` (§8.26.2 / Table 8.34) — M1.6-6, PR-26.
 *
 *  The normative facts pinned here: the exact ten-attribute set and their XSD
 *  types, the child cardinalities (`BarcodeDetails?`, `ExtraValues?`,
 *  `MetadataMap*` — the last one not modelled yet), the two closed inline
 *  enumerations, the open catalogs of Tables 8.32/8.33/8.34, the absence of
 *  IDREFs, and every way of breaking the single SHALL of Table 8.31.
 */
class IdentificationFieldLaws extends FunSuite:

  private val at = XPath("/XJDF/ResourceSet/Resource/Component/IdentificationField")

  /** The canonical lawful field of Example 8.4: an EAN_13 product barcode. */
  private val ean13 = IdentificationField(
    encoding = Some(FieldEncoding.Barcode),
    encodingDetails = Some(Catalog.EncodingDetails.EAN_13),
    purpose = Some(FieldPurpose.Label),
    purposeDetails = Some(Catalog.PurposeDetails.ProductIdentification),
    value = Some(XjdfString.unsafe("0123456789128"))
  )

  // ---------------------------------------------------------------------------
  // Table 8.31: attribute mapping and cardinality
  // ---------------------------------------------------------------------------

  test("Table 8.31: a fully populated IdentificationField preserves all ten attributes"):
    val field = IdentificationField(
      boundingBox = Some(Rectangle.unsafe(0.0, 0.0, 100.0, 50.0)),
      encoding = Some(FieldEncoding.Barcode),
      encodingDetails = Some(Catalog.EncodingDetails.QR),
      format = None,
      orientation = Some(Matrix.identity),
      position = Some(Face.Front),
      purpose = Some(FieldPurpose.Verification),
      purposeDetails = Some(Catalog.PurposeDetails.ProductIdentification),
      value = Some(XjdfString.unsafe("0123456789128"))
    )
    assertEquals(field.boundingBox.map(_.width), Some(100.0))
    assertEquals(field.encoding, Some(FieldEncoding.Barcode))
    assertEquals(field.encodingDetails.map(_.value), Some("QR"))
    assertEquals(field.orientation, Some(Matrix.identity))
    assertEquals(field.position, Some(Face.Front))
    assertEquals(field.purpose, Some(FieldPurpose.Verification))
    assertEquals(field.purposeDetails.map(_.value), Some("ProductIdentification"))
    assertEquals(field.value.map(_.value), Some("0123456789128"))
    assertEquals(
      Show[IdentificationField].show(field),
      "IdentificationField(encoding=Barcode, details=QR, purpose=Verification, value=0123456789128)"
    )

  test("Table 8.31: every attribute is optional and both children default to absent"):
    // schema.xsd declares all ten `use="optional"`; only the SHALL of the
    // value sources constrains which combinations are lawful.
    val empty = IdentificationField()
    assertEquals(empty.boundingBox, None)
    assertEquals(empty.encoding, None)
    assertEquals(empty.encodingDetails, None)
    assertEquals(empty.format, None)
    assertEquals(empty.orientation, None)
    assertEquals(empty.position, None)
    assertEquals(empty.purpose, None)
    assertEquals(empty.purposeDetails, None)
    assertEquals(empty.value, None)
    assertEquals(empty.valueFormat, None)
    assertEquals(empty.valueTemplate, None)
    assertEquals(empty.barcodeDetails, None)
    assertEquals(empty.extraValues, None)

  test("Table 8.31: @ValueTemplate is NMTOKENS — a non-empty list by type"):
    val template = NmTokens.fromStrings("job", "doc").getOrElse(fail("valid NMTOKENS rejected"))
    val field = IdentificationField(
      valueFormat = Some(XjdfString.unsafe("Job_%s_%i")),
      valueTemplate = Some(template)
    )
    assertEquals(field.valueTemplate.map(t => Show[NmTokens].show(t)), Some("job doc"))
    // NMTOKENS admits no empty entries and no whitespace inside a token.
    assertEquals(NmTokens.fromStrings("job doc"), None)

  test("Table 8.31: @Format is a regExp, @Value and @ValueFormat are strings"):
    val field = IdentificationField(format = Some(RegExp.unsafe("[0-9]{13}")))
    assertEquals(field.format.map(_.value), Some("[0-9]{13}"))
    assertEquals(RegExp.from(""), None)
    assertEquals(XjdfString.from("with\ttab"), None)

  test("Table 8.31: @Position reuses the Face enumeration of Table A.20"):
    // The data type cell says "Allowed value is from: Face" — the shared
    // closed enum, not a private copy.
    Face.all.foreach: face =>
      assertEquals(IdentificationField(position = Some(face)).position, Some(face))

  // ---------------------------------------------------------------------------
  // Table 8.31: the SHALL — exactly one value source
  // ---------------------------------------------------------------------------

  test("Table 8.31: @Value alone satisfies the exactly-one SHALL"):
    assertEquals(ean13.valueSources, List("@Value"))
    assertEquals(IdentificationField.law(ean13, at), Chain.empty[Issue])

  test("Table 8.31: @Format alone satisfies the exactly-one SHALL"):
    val field = IdentificationField(format = Some(RegExp.unsafe("[0-9]{13}")))
    assertEquals(field.valueSources, List("@Format"))
    assertEquals(IdentificationField.law(field, at), Chain.empty[Issue])

  test("Table 8.31: the pair @ValueFormat + @ValueTemplate satisfies the exactly-one SHALL"):
    val field = IdentificationField(
      valueFormat = Some(XjdfString.unsafe("Job_%s")),
      valueTemplate = Some(NmTokens.of(NmToken.unsafe("job")))
    )
    assertEquals(field.valueSources, List("@ValueFormat + @ValueTemplate"))
    assertEquals(IdentificationField.law(field, at), Chain.empty[Issue])

  test("Table 8.31 SHALL violation: no value source at all"):
    val field = IdentificationField(encoding = Some(FieldEncoding.Barcode))
    val issues = IdentificationField.law(field, at).toList
    assertEquals(issues.map(_.code), List(Some(IssueCode.IdentificationFieldValueSource)))
    assertEquals(issues.map(_.severity), List(SeverityClass.Error))
    assert(issues.head.message.contains("none is specified"), issues.head.message)

  test("Table 8.31 SHALL violation: @Format and @Value together"):
    val field = IdentificationField(
      format = Some(RegExp.unsafe("[0-9]{13}")),
      value = Some(XjdfString.unsafe("0123456789128"))
    )
    assertEquals(field.valueSources, List("@Format", "@Value"))
    val issues = IdentificationField.law(field, at).toList
    assertEquals(issues.map(_.code), List(Some(IssueCode.IdentificationFieldValueSource)))
    assert(issues.head.message.contains("@Format and @Value"), issues.head.message)

  test("Table 8.31 SHALL violation: @Value together with the complete pair"):
    val field = IdentificationField(
      value = Some(XjdfString.unsafe("fixed")),
      valueFormat = Some(XjdfString.unsafe("Job_%s")),
      valueTemplate = Some(NmTokens.of(NmToken.unsafe("job")))
    )
    assertEquals(field.valueSources.size, 2)
    assertEquals(
      IdentificationField.law(field, at).toList.map(_.code),
      List(Some(IssueCode.IdentificationFieldValueSource))
    )

  test("Table 8.31 SHALL violation: half of the pair is not an alternative"):
    // "the pair @ValueFormat and @ValueTemplate" is indivisible: either half
    // on its own specifies no complete value source.
    val formatOnly = IdentificationField(valueFormat = Some(XjdfString.unsafe("Job_%s")))
    val templateOnly = IdentificationField(valueTemplate = Some(NmTokens.of(NmToken.unsafe("job"))))
    List(formatOnly, templateOnly).foreach: field =>
      assertEquals(field.valueSources, List.empty[String])
      assert(field.hasPartialPair, "half a pair is partial")
      val issues = IdentificationField.law(field, at).toList
      assertEquals(issues.map(_.code), List(Some(IssueCode.IdentificationFieldValueSource)))
      assert(
        issues.head.message.contains("SHALL be specified together"),
        issues.head.message
      )

  test("Table 8.31 SHALL violation: half a pair does not become lawful next to @Value"):
    // @Value alone would be lawful; the dangling @ValueFormat still breaks the
    // rule, so the partial pair is checked independently of the count.
    val field = IdentificationField(
      value = Some(XjdfString.unsafe("fixed")),
      valueFormat = Some(XjdfString.unsafe("Job_%s"))
    )
    assertEquals(field.valueSources, List("@Value"))
    assert(field.hasPartialPair)
    assertEquals(
      IdentificationField.law(field, at).toList.map(_.code),
      List(Some(IssueCode.IdentificationFieldValueSource))
    )

  test("Table 8.31: containerLaw indexes the XPath of each element of the chain"):
    val bad = IdentificationField(encoding = Some(FieldEncoding.RFID))
    val issues = IdentificationField
      .containerLaw(Chain(ean13, bad, bad), XPath("/XJDF/ResourceSet/Resource/Component"))
      .toList
    assertEquals(issues.size, 2)
    assertEquals(
      issues.map(_.location.value),
      List(
        "/XJDF/ResourceSet/Resource/Component/IdentificationField[1]",
        "/XJDF/ResourceSet/Resource/Component/IdentificationField[2]"
      )
    )

  // ---------------------------------------------------------------------------
  // Table 8.33 / Table 8.34: the two modelled children
  // ---------------------------------------------------------------------------

  test("Table 8.33: BarcodeDetails maps four optional attributes and no children"):
    val details = BarcodeDetails(
      barcodeVersion = Some(Catalog.BarcodeVersion.qr(7)),
      errorCorrectionLevel = Some(Catalog.ErrorCorrectionLevel.QR_EC_M),
      xCells = Some(45L),
      yCells = Some(45L)
    )
    assertEquals(details.barcodeVersion.map(_.value), Some("QR_7"))
    assertEquals(details.errorCorrectionLevel.map(_.value), Some("QR_EC_M"))
    assertEquals(details.xCells, Some(45L))
    assertEquals(details.yCells, Some(45L))
    assertEquals(
      Show[BarcodeDetails].show(details),
      "BarcodeDetails(version=QR_7, ec=QR_EC_M, xCells=45, yCells=45)"
    )
    // All four optional → an empty element is valid and carries no SHALL.
    assertEquals(Show[BarcodeDetails].show(BarcodeDetails()), "BarcodeDetails()")

  test("Table 8.33: @XCells and @YCells carry no invented positivity restriction"):
    val values = List(Int.MinValue.toLong, -1L, 0L, 1L, Int.MaxValue.toLong)
    assertEquals(values.flatMap(v => BarcodeDetails(xCells = Some(v)).xCells.toList), values)

  test("Table 8.34: ExtraValues requires both attributes structurally"):
    val extra = ExtraValues(Catalog.ExtraValuesUsage.Supplemental, XjdfString.unsafe("12345"))
    assertEquals(extra.usage.value, "Supplemental")
    assertEquals(extra.value.value, "12345")
    assertEquals(Show[ExtraValues].show(extra), "ExtraValues(Supplemental=12345)")

  test("Table 8.31: BarcodeDetails? and ExtraValues? are single optional children"):
    // schema.xsd gives maxOccurs="1" for both — an Option, not a Chain.
    val field = ean13.copy(
      barcodeDetails = Some(BarcodeDetails(barcodeVersion = Some(Catalog.BarcodeVersion.dataMatrix(12, 26)))),
      extraValues = Some(ExtraValues(Catalog.ExtraValuesUsage.Coupon, XjdfString.unsafe("99")))
    )
    assertEquals(field.barcodeDetails.flatMap(_.barcodeVersion).map(_.value), Some("DM_12_by_26"))
    assertEquals(field.extraValues.map(_.usage.value), Some("Coupon"))
    // Children do not affect the value-source SHALL of the parent.
    assertEquals(IdentificationField.law(field, at), Chain.empty[Issue])

  // ---------------------------------------------------------------------------
  // Open catalogs (ADR-0007)
  // ---------------------------------------------------------------------------

  test("Table 8.32 / ADR-0007: @EncodingDetails is an open catalog of 47 sample values"):
    assertEquals(Catalog.EncodingDetails.recommended.size, 47)
    assertEquals(Catalog.EncodingDetails.recommended.distinct.size, 47)
    // "Values that are not present in this list MAY be valid in an XJDF
    // workflow" — a vendor scheme stays representable.
    val vendor = NmToken.unsafe("VendorCode2D")
    assert(!Catalog.EncodingDetails.recommended.contains(vendor))
    val withVendor = IdentificationField(
      value = Some(XjdfString.unsafe("x")),
      encodingDetails = Some(vendor)
    )
    assertEquals(withVendor.encodingDetails, Some(vendor))

  test("Table 8.32: the normative typo CODABAR_Tradional is preserved verbatim"):
    // Copying the table "correctly" would silently invent a token nobody
    // writes on the wire (the same class of defect as N-08).
    assert(Catalog.EncodingDetails.recommended.exists(_.value == "CODABAR_Tradional"))
    assert(!Catalog.EncodingDetails.recommended.exists(_.value == "CODABAR_Traditional"))

  test("Tables 8.36 / 8.37 / ADR-0007: @BarcodeVersion covers both families"):
    assertEquals(Catalog.BarcodeVersion.dataMatrixVersions.size, 29)
    assertEquals(Catalog.BarcodeVersion.qrVersions.size, 40)
    assertEquals(Catalog.BarcodeVersion.recommended.distinct.size, 69)
    assertEquals(Catalog.BarcodeVersion.dataMatrix(144, 144).value, "DM_144_by_144")
    assertEquals(Catalog.BarcodeVersion.qr(40).value, "QR_40")
    // Table 8.36 lists both square and rectangular DATAMATRIX sizes.
    List("DM_8_by_18", "DM_10_by_10", "DM_16_by_36", "DM_144_by_144").foreach: v =>
      assert(Catalog.BarcodeVersion.dataMatrixVersions.exists(_.value == v), v)

  test("Table 8.33 / ADR-0007: @ErrorCorrectionLevel covers PDF417 and QR levels"):
    assertEquals(Catalog.ErrorCorrectionLevel.pdf417Levels.map(_.value).head, "PDF417_EC_0")
    assertEquals(Catalog.ErrorCorrectionLevel.pdf417Levels.size, 9)
    assertEquals(
      Catalog.ErrorCorrectionLevel.qrLevels.map(_.value),
      List("QR_EC_L", "QR_EC_M", "QR_EC_Q", "QR_EC_H")
    )
    assertEquals(Catalog.ErrorCorrectionLevel.recommended.size, 13)

  test("Table 8.34 / ADR-0007: @Usage of ExtraValues is an open catalog"):
    assertEquals(
      Catalog.ExtraValuesUsage.recommended.map(_.value),
      List("CompositeCode", "Coupon", "Supplemental")
    )
    val vendor = NmToken.unsafe("VendorExtra")
    assert(!Catalog.ExtraValuesUsage.recommended.contains(vendor))
    assertEquals(ExtraValues(vendor, XjdfString.unsafe("v")).usage, vendor)

  test("Table 8.31 / ADR-0007: @PurposeDetails is an open catalog"):
    assertEquals(Catalog.PurposeDetails.recommended.map(_.value), List("ProductIdentification"))
    val vendor = NmToken.unsafe("InternalTracking")
    assert(!Catalog.PurposeDetails.recommended.contains(vendor))
    assertEquals(ean13.copy(purposeDetails = Some(vendor)).purposeDetails, Some(vendor))

  // ---------------------------------------------------------------------------
  // §2.2.3: references, and the container wiring
  // ---------------------------------------------------------------------------

  test("Table 8.31 / §2.2.3: IdentificationField collects no IDREFs, children included"):
    val field = ean13.copy(
      barcodeDetails = Some(BarcodeDetails(xCells = Some(21L))),
      extraValues = Some(ExtraValues(Catalog.ExtraValuesUsage.CompositeCode, XjdfString.unsafe("c")))
    )
    assertEquals(field.references, Chain.empty[IdRef])
    assertEquals(field.barcodeDetails.map(_.references), Some(Chain.empty[IdRef]))
    assertEquals(field.extraValues.map(_.references), Some(Chain.empty[IdRef]))

  test("Table 6.37: Component/IdentificationField* preserves order and leaves @MediaRef intact"):
    val mediaRef = IdRef.unsafe("MediaResource")
    val second = IdentificationField(format = Some(RegExp.unsafe("[0-9]+")))
    val component = Component(
      mediaRef = Some(mediaRef),
      identificationFields = Chain(ean13, second)
    )
    assertEquals(component.identificationFields.toList, List(ean13, second))
    assertEquals(component.references.toList, List(mediaRef))
    assertEquals(Component().identificationFields, Chain.empty[IdentificationField])

  test("Table 6.37: a lawful Component/IdentificationField* validates through the root"):
    val ticket = componentTicket(Chain.one(ean13))
    assert(ticket.validate.isValid, "a Component carrying a lawful IdentificationField validates")
    assertEquals(ticket.references, Chain.empty[IdRef])

  test("Table 8.31: the SHALL reaches the root validator through Component"):
    val bad = IdentificationField(encoding = Some(FieldEncoding.Braille))
    val report = TicketValidator.validateReport(componentTicket(Chain(ean13, bad)))
    assert(!report.isValid, "an unlawful IdentificationField invalidates the ticket")
    assertEquals(
      report.errors.map(_.code).toList,
      List(Some(IssueCode.IdentificationFieldValueSource))
    )
    assertEquals(
      report.errors.map(_.location.value).toList,
      List("/XJDF/ResourceSet[@Name='Component']/Resource/Component/IdentificationField[1]")
    )

  private def componentTicket(fields: Chain[IdentificationField]): XJDF =
    XJDF(
      jobId = JobId.unsafe("BarcodeJob"),
      types = NonEmptyChain.one(ProcessType.Cutting),
      resourceSets = Chain.one(
        ResourceSet(
          ResourceSetName.unsafe("Component"),
          usage = Some(Usage.Input),
          resources = Chain.one(
            Resource.of(
              ResourcePayload.ComponentResource(Component(identificationFields = fields))
            )
          )
        )
      )
    )

  test("Table 8.31: IdentificationField equality covers attributes and both children"):
    val a = ean13.copy(barcodeDetails = Some(BarcodeDetails(xCells = Some(21L))))
    val b = ean13.copy(barcodeDetails = Some(BarcodeDetails(xCells = Some(21L))))
    val c = ean13.copy(barcodeDetails = Some(BarcodeDetails(xCells = Some(22L))))
    val d = ean13
    assert(Eq[IdentificationField].eqv(a, b))
    assert(!Eq[IdentificationField].eqv(a, c))
    assert(!Eq[IdentificationField].eqv(a, d))
    assert(Eq[BarcodeDetails].eqv(BarcodeDetails(), BarcodeDetails()))
    assert(
      Eq[ExtraValues].eqv(
        ExtraValues(Catalog.ExtraValuesUsage.Coupon, XjdfString.unsafe("1")),
        ExtraValues(Catalog.ExtraValuesUsage.Coupon, XjdfString.unsafe("1"))
      )
    )

  // ---------------------------------------------------------------------------
  // schema.xsd as the structural oracle (§1.2)
  // ---------------------------------------------------------------------------

  test("Table 8.31: schema.xsd declares the exact attribute set, all optional"):
    val named = raw"""<xs:attribute name="([^"]+)" type="([^"]+)" use="([^"]+)"/>""".r
    val inline = raw"""<xs:attribute name="([^"]+)" use="([^"]+)">""".r
    val namedAttrs = named.findAllMatchIn(identificationFieldSchema).map { m =>
      (m.group(1), m.group(2), m.group(3))
    }.toSet
    val inlineAttrs = inline.findAllMatchIn(identificationFieldSchema).map { m =>
      (m.group(1), m.group(2))
    }.toSet
    assertEquals(
      namedAttrs,
      Set(
        ("BoundingBox", "rectangle", "optional"),
        ("EncodingDetails", "xs:NMTOKEN", "optional"),
        ("Format", "regExp", "optional"),
        ("Orientation", "matrix", "optional"),
        ("Position", "Face", "optional"),
        ("PurposeDetails", "xs:NMTOKEN", "optional"),
        ("Value", "xs:string", "optional"),
        ("ValueFormat", "xs:string", "optional"),
        ("ValueTemplate", "xs:NMTOKENS", "optional")
      )
    )
    // @Encoding and @Purpose are the two inline enumerations.
    assertEquals(inlineAttrs, Set(("Encoding", "optional"), ("Purpose", "optional")))

  test("Table 8.31: schema.xsd inline enumerations match FieldEncoding and FieldPurpose"):
    val enumeration = raw"""<xs:enumeration value="([^"]+)"/>""".r
    val declared = enumeration.findAllMatchIn(identificationFieldSchema).map(_.group(1)).toSet
    val modelled = (FieldEncoding.all.map(_.token.value) ++ FieldPurpose.all.map(_.token.value)).toSet
    assertEquals(declared, modelled)

  test("Table 8.31: schema.xsd declares three children with the modelled cardinalities"):
    val child = raw"""<xs:element maxOccurs="([^"]+)" minOccurs="([^"]+)" ref="([^"]+)"/>""".r
    val actual = child.findAllMatchIn(identificationFieldSchema).map { m =>
      (m.group(3), m.group(2), m.group(1))
    }.toList
    assertEquals(
      actual,
      List(
        ("BarcodeDetails", "0", "1"),
        ("ExtraValues", "0", "1"),
        ("MetadataMap", "0", "unbounded")
      )
    )
    // MetadataMap (Table 8.46) is deliberately deferred to its own slice
    // (M1.6-6b): it drags in Expr (Table 8.47) and the XPath data type and is
    // shared with RunList (Table 6.148).

  test("Table 8.33: schema.xsd declares four optional attributes and no children"):
    val named = raw"""<xs:attribute name="([^"]+)" type="([^"]+)" use="([^"]+)"/>""".r
    assertEquals(
      named.findAllMatchIn(barcodeDetailsSchema).map(m => (m.group(1), m.group(2), m.group(3))).toSet,
      Set(
        ("BarcodeVersion", "xs:NMTOKEN", "optional"),
        ("ErrorCorrectionLevel", "xs:NMTOKEN", "optional"),
        ("XCells", "xs:int", "optional"),
        ("YCells", "xs:int", "optional")
      )
    )
    assertEquals(childElementCount(barcodeDetailsSchema), 0, "BarcodeDetails has no child elements")

  test("Table 8.34: schema.xsd declares both attributes of ExtraValues as required"):
    val named = raw"""<xs:attribute name="([^"]+)" type="([^"]+)" use="([^"]+)"/>""".r
    assertEquals(
      named.findAllMatchIn(extraValuesSchema).map(m => (m.group(1), m.group(2), m.group(3))).toSet,
      Set(
        ("Usage", "xs:NMTOKEN", "required"),
        ("Value", "xs:string", "required")
      )
    )
    assertEquals(childElementCount(extraValuesSchema), 0, "ExtraValues has no child elements")

  test("Table 8.31 containers: schema.xsd gives Component the unbounded cardinality"):
    val occurrence = raw"""<xs:element maxOccurs="([^"]+)" minOccurs="0" ref="IdentificationField"/>""".r
    val componentSchema = schemaSlice("<xs:complexType name=\"Component\">", "</xs:complexType>")
    assertEquals(
      occurrence.findAllMatchIn(componentSchema).map(_.group(1)).toList,
      List("unbounded")
    )
    // The two containers that cap the element at one are Emboss and
    // BarcodeProductionParams — neither is modelled yet (M3).
    List("Emboss", "BarcodeProductionParams").foreach: name =>
      assertEquals(
        occurrence.findAllMatchIn(schemaSlice(s"<xs:element name=\"$name\">", "</xs:element>"))
          .map(_.group(1)).toList,
        List("1"),
        name
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

  /** Child `<xs:element .../>` declarations inside a slice, excluding the
   *  opening tag of the sliced element itself.
   */
  private def childElementCount(slice: String): Int =
    raw"""<xs:element """.r.findAllMatchIn(slice).size - 1

  private def schemaSlice(startToken: String, endToken: String): String =
    val start = schema.indexOf(startToken)
    assert(start >= 0, s"$startToken is missing from schema.xsd")
    val end = schema.indexOf(endToken, start)
    assert(end > start, s"$startToken is not closed by $endToken in schema.xsd")
    schema.substring(start, end + endToken.length)

  private lazy val identificationFieldSchema: String =
    schemaSlice("<xs:element name=\"IdentificationField\">", "</xs:element>")

  private lazy val barcodeDetailsSchema: String =
    schemaSlice("<xs:element name=\"BarcodeDetails\">", "</xs:element>")

  private lazy val extraValuesSchema: String =
    schemaSlice("<xs:element name=\"ExtraValues\">", "</xs:element>")

end IdentificationFieldLaws
