package xjdf4s.laws

import java.io.File

import scala.io.Source

import xjdf4s.model.*
import xjdf4s.model.elements.GeneralID
import xjdf4s.prim.*
import cats.Show
import cats.data.{Chain, NonEmptyChain}
import cats.kernel.{Eq, Monoid}
import munit.FunSuite

/** NamedFeatures (§3.1.3.1), the `GeneralID` element (§8.23 / Table 8.28) and
 *  the `DataType` enumeration (§A.2.13 / Table A.14), M1.6-14.
 *
 *  Covers three normative obligations:
 *
 *    1. Table 8.28 — «The data type of the value SHALL correspond to
 *       `GeneralID/@DataType`» (`GeneralID.law`, negative test per data type);
 *    2. §3.1.3.1 — «Explicitly specified Traits SHALL override any implied
 *       Traits defined by `GeneralID[@Datatype="NamedFeature"]`»
 *       (`NamedFeatures.resolve`, negative test: the implied value never wins);
 *    3. ADR-0016/N-59 — Table A.14 against the inline XSD enumeration of
 *       `GeneralID/@DataType` (oracle test fixing both sides).
 */
class NamedFeatureLaws extends FunSuite:

  private def path(raw: String): XjdfXPath = XjdfXPath.unsafe(raw)

  private def str(raw: String): XjdfString = XjdfString.unsafe(raw)

  private def feature(name: String, value: String): NamedFeature =
    NamedFeature(NmToken.unsafe(name), str(value))

  private def ticketWith(generalIds: Chain[GeneralID]): XJDF =
    XJDF(
      jobId = JobId.unsafe("NamedFeatureLaws"),
      types = NonEmptyChain.one(ProcessType.Cutting),
      generalIds = generalIds
    )

  private def codes(ticket: XJDF): List[IssueCode] =
    ticket.validateReport.errors.toList.flatMap(_.code)

  // ---------------------------------------------------------------------------
  // 1. Table 8.28 / Table A.14: the GeneralID element and its data type
  // ---------------------------------------------------------------------------

  test("Table 8.28: @IDUsage and @IDValue are required by the type, @DataType is optional"):
    val plain = GeneralID(NmToken.unsafe("Campaign"), str("Spring 2026"))

    assertEquals(plain.idUsage.value, "Campaign")
    assertEquals(plain.idValue.value, "Spring 2026")
    assertEquals(plain.dataType, None)
    assertEquals(plain.references, Chain.empty)
    assert(!plain.isNamedFeature)

  test("Table A.14: the DataType wire tokens follow the prose, not the XSD spelling"):
    assertEquals(
      DataType.all.map(_.token.value),
      List("boolean", "dateTime", "duration", "float", "integer", "NamedFeature", "NMTOKEN", "string")
    )
    DataType.all.foreach: value =>
      assertEquals(DataType.fromToken(value.token), Some(value), value.toString)
    assertEquals(DataType.fromToken(NmToken.unsafe("xs:int")), None)
    assertEquals(DataType.fromToken(NmToken.unsafe("banana")), None)

  test("Table A.14: DataType has Show and Eq consistent with its token"):
    assertEquals(Show[DataType].show(DataType.NamedFeature), "NamedFeature")
    assert(Eq[DataType].eqv(DataType.IntegerType, DataType.IntegerType))
    assert(!Eq[DataType].eqv(DataType.IntegerType, DataType.FloatType))

  test("ADR-0016 / N-59: Table A.14 and the inline XSD enumeration disagree in seven of eight values"):
    val proseValues = DataType.all.map(_.token.value)
    val xsdValues = enumerationValues(generalIdSchema)

    assertEquals(
      xsdValues.sorted,
      List(
        "NamedFeature",
        "xs:NMTOKEN",
        "xs:boolean",
        "xs:dateTime",
        "xs:duration",
        "xs:float",
        "xs:int",
        "xs:string"
      ).sorted
    )
    assertEquals(proseValues.size, xsdValues.size)
    assertEquals(proseValues.toSet.intersect(xsdValues.toSet), Set("NamedFeature"))
    proseValues.foreach: value =>
      assert(
        appendixA.contains(s"| `$value` |"),
        s"Table A.14 no longer lists $value"
      )

  // ---------------------------------------------------------------------------
  // 2. Table 8.28 SHALL: @IDValue corresponds to @DataType
  // ---------------------------------------------------------------------------

  test("Table 8.28: a lawful @IDValue is accepted for every data type"):
    val lawful = List(
      DataType.BooleanType -> "true",
      DataType.BooleanType -> "false",
      DataType.DateTimeType -> "2026-08-16T10:00:00+03:00",
      DataType.DurationType -> "PT36H",
      DataType.FloatType -> "1.5",
      DataType.IntegerType -> "12",
      DataType.NamedFeature -> "bar snax",
      DataType.NmTokenType -> "Spring2026",
      DataType.StringType -> "Spring 2026"
    )
    lawful.foreach: (dataType, value) =>
      val generalId = GeneralID(NmToken.unsafe("Usage"), str(value), Some(dataType))
      assert(generalId.hasLawfulValue, s"$dataType / $value")
      assertEquals(GeneralID.law.check(generalId, XPath("/XJDF/GeneralID[0]")), Chain.empty)

  test("Table 8.28 (negative): @IDValue that does not correspond to @DataType is rejected"):
    val unlawful = List(
      DataType.BooleanType -> "1",
      DataType.DateTimeType -> "yesterday",
      DataType.DurationType -> "36h",
      DataType.FloatType -> "one point five",
      DataType.IntegerType -> "1.5",
      DataType.NmTokenType -> "two tokens"
    )
    unlawful.foreach: (dataType, value) =>
      val generalId = GeneralID(NmToken.unsafe("Usage"), str(value), Some(dataType))
      assert(!generalId.hasLawfulValue, s"$dataType / $value")
      assertEquals(
        GeneralID.law.check(generalId, XPath("/XJDF/GeneralID[0]")).toList.flatMap(_.code),
        List(IssueCode.GeneralIdValueDataTypeMismatch),
        s"$dataType / $value"
      )

  test("Table 8.28: an absent @DataType imposes no constraint on @IDValue"):
    val generalId = GeneralID(NmToken.unsafe("Usage"), str("anything at all"))
    assert(generalId.hasLawfulValue)
    assertEquals(GeneralID.law.check(generalId, XPath("/XJDF/GeneralID[0]")), Chain.empty)

  test("Table 8.28: NamedFeature and string accept any @IDValue (Table A.14)"):
    List(DataType.NamedFeature, DataType.StringType).foreach: dataType =>
      val generalId = GeneralID(NmToken.unsafe("pool"), str("bar snax 42 -"), Some(dataType))
      assert(generalId.hasLawfulValue, dataType.toString)

  test("Table 8.28: the local law is reached from all four modelled GeneralID containers"):
    val unlawful = GeneralID(NmToken.unsafe("Copies"), str("1.5"), Some(DataType.IntegerType))
    val resource = Resource(generalIds = Chain.one(unlawful))
    val product = Product(id = Some(Id.unsafe("P1")), generalIds = Chain.one(unlawful))
    val resourceSet = ResourceSet(
      name = ResourceSetName.unsafe("Component"),
      usage = Some(Usage.Input),
      resources = Chain.one(resource),
      generalIds = Chain.one(unlawful)
    )
    val ticket = XJDF(
      jobId = JobId.unsafe("NamedFeatureLaws"),
      types = NonEmptyChain.one(ProcessType.Cutting),
      productList = Some(ProductList(NonEmptyChain.one(product))),
      resourceSets = Chain.one(resourceSet),
      generalIds = Chain.one(unlawful)
    )

    assert(ticket.validate.isInvalid)
    assertEquals(
      codes(ticket).count(_ == IssueCode.GeneralIdValueDataTypeMismatch),
      4,
      "XJDF, ResourceSet, Product and Resource each contribute one finding"
    )

  test("Table 8.28: container law indexes the XPath of each GeneralID"):
    val unlawful = GeneralID(NmToken.unsafe("Copies"), str("1.5"), Some(DataType.IntegerType))
    val lawful = GeneralID.namedFeature(NmToken.unsafe("pool"), str("bar snax"))
    val issues = GeneralID.containerLaw(Chain(lawful, unlawful), XPath("/XJDF"))

    assertEquals(issues.toList.map(_.location.value), List("/XJDF/GeneralID[1]"))

  // ---------------------------------------------------------------------------
  // 3. §3.1.3.1: NamedFeature projection
  // ---------------------------------------------------------------------------

  test("§3.1.3.1 / Table A.14: GeneralID[@DataType=\"NamedFeature\"] projects to a NamedFeature"):
    // The literal example of Table A.14:
    // <GeneralID DataType="NamedFeature" IDUsage="pool" IDValue="bar snax"/>
    val generalId = GeneralID.namedFeature(NmToken.unsafe("pool"), str("bar snax"))

    assert(generalId.isNamedFeature)
    assertEquals(generalId.dataType, Some(DataType.NamedFeature))
    assertEquals(NamedFeature.from(generalId), Some(feature("pool", "bar snax")))

  test("§3.1.3.1: the projection is lossless — NamedFeature.toGeneralID round-trips"):
    val generalId = GeneralID.namedFeature(NmToken.unsafe("pool"), str("bar snax"))

    assertEquals(NamedFeature.from(generalId).map(_.toGeneralID), Some(generalId))
    assertEquals(NamedFeature.from(feature("pool", "bar snax").toGeneralID), Some(feature("pool", "bar snax")))

  test("§3.1.3.1: a GeneralID of any other data type is not a NamedFeature"):
    val others = DataType.all.filter(_ != DataType.NamedFeature)
    others.foreach: dataType =>
      val generalId = GeneralID(NmToken.unsafe("pool"), str("12"), Some(dataType))
      assertEquals(NamedFeature.from(generalId), None, dataType.toString)
    assertEquals(NamedFeature.from(GeneralID(NmToken.unsafe("pool"), str("bar snax"))), None)

  test("§3.1.3.1: XJDF MAY contain zero or more NamedFeatures, collected in document order"):
    val empty = ticketWith(Chain.empty)
    assertEquals(empty.namedFeatures, Chain.empty)
    assert(empty.validate.isValid)

    val mixed = ticketWith(Chain(
      GeneralID.namedFeature(NmToken.unsafe("pool"), str("bar snax")),
      GeneralID(NmToken.unsafe("Copies"), str("12"), Some(DataType.IntegerType)),
      GeneralID.namedFeature(NmToken.unsafe("finish"), str("matte"))
    ))
    assertEquals(
      mixed.namedFeatures.toList,
      List(feature("pool", "bar snax"), feature("finish", "matte"))
    )
    assert(mixed.validate.isValid)

  test("§3.1.3.1: NamedFeature has Show and Eq"):
    assertEquals(Show[NamedFeature].show(feature("pool", "bar snax")), "NamedFeature(pool=bar snax)")
    assert(Eq[NamedFeature].eqv(feature("pool", "bar snax"), feature("pool", "bar snax")))
    assert(!Eq[NamedFeature].eqv(feature("pool", "bar snax"), feature("pool", "other")))

  // ---------------------------------------------------------------------------
  // 4. §3.1.3.1 SHALL: explicit Traits override implied Traits
  // ---------------------------------------------------------------------------

  private val glossPath = path("/XJDF/ResourceSet[@Name='Media']/Resource/Media/@Gloss")
  private val weightPath = path("/XJDF/ResourceSet[@Name='Media']/Resource/Media/@Weight")
  private val gradePath = path("/XJDF/ResourceSet[@Name='Media']/Resource/Media/@Grade")

  private val glossyPaper: TraitSet =
    TraitSet.of(glossPath -> str("Glossy"), weightPath -> str("150"))

  private val registry: NamedFeatures.Registry =
    NamedFeatures.registryOf(Map(feature("paper", "glossy") -> glossyPaper))

  test("§3.1.3.1 (SHALL): an explicitly specified Trait overrides the implied Trait"):
    val explicitTraits = TraitSet.of(glossPath -> str("Matte"))
    val resolution = NamedFeatures.resolve(glossyPaper, explicitTraits)

    // The SHALL itself: the explicit value wins, the implied value is gone.
    assertEquals(resolution.resolved.get(glossPath), Some(str("Matte")))
    assert(resolution.resolved.get(glossPath) != Some(str("Glossy")))
    // Implied Traits that were not contradicted stay in force.
    assertEquals(resolution.resolved.get(weightPath), Some(str("150")))
    assertEquals(resolution.overridden.toList, List(glossPath))
    assertEquals(
      resolution.issues.toList.flatMap(_.code),
      List(IssueCode.NamedFeatureTraitOverridden)
    )
    assertEquals(resolution.issues.toList.map(_.severity), List(SeverityClass.Information))

  test("§3.1.3.1 (negative): the implied Trait never wins, whatever the order of composition"):
    val explicitTraits = TraitSet.of(glossPath -> str("Matte"))
    // The wrong reading of the rule is `explicit |+| implied`; it would yield
    // "Glossy". Every accessor of the resolution must disagree with it.
    val wrongWayRound = Monoid[TraitSet].combine(explicitTraits, glossyPaper)
    val resolution = NamedFeatures.resolve(glossyPaper, explicitTraits)

    assertEquals(wrongWayRound.get(glossPath), Some(str("Glossy")))
    assert(
      resolution.resolved.get(glossPath) != wrongWayRound.get(glossPath),
      "resolution must not be the implied-wins overlay"
    )
    assertEquals(resolution.resolved.get(glossPath), Some(str("Matte")))

  test("§3.1.3.1: an implied Trait restated with the same value is not reported as overridden"):
    val explicitTraits = TraitSet.of(glossPath -> str("Glossy"))
    val resolution = NamedFeatures.resolve(glossyPaper, explicitTraits)

    assertEquals(resolution.resolved.get(glossPath), Some(str("Glossy")))
    assertEquals(resolution.overridden, Chain.empty)
    assertEquals(resolution.issues, Chain.empty)

  test("§3.1.3.1: an explicit Trait with no implied counterpart is simply added"):
    val explicitTraits = TraitSet.of(gradePath -> str("3"))
    val resolution = NamedFeatures.resolve(glossyPaper, explicitTraits)

    assertEquals(resolution.resolved.size, 3)
    assertEquals(resolution.resolved.get(gradePath), Some(str("3")))
    assertEquals(resolution.overridden, Chain.empty)

  test("§3.1.3.1: NamedFeatures of a ticket are expanded through the Controller registry"):
    val ticket = ticketWith(Chain.one(GeneralID.namedFeature(NmToken.unsafe("paper"), str("glossy"))))
    val resolution = NamedFeatures.resolveTicket(ticket, registry, TraitSet.of(glossPath -> str("Matte")))

    assertEquals(ticket.namedFeatures.toList, List(feature("paper", "glossy")))
    assertEquals(resolution.resolved.get(glossPath), Some(str("Matte")))
    assertEquals(resolution.resolved.get(weightPath), Some(str("150")))

  test("§3.1.3.1: an unknown NamedFeature implies nothing and the explicit Traits stand alone"):
    val ticket = ticketWith(Chain.one(GeneralID.namedFeature(NmToken.unsafe("paper"), str("unknown"))))
    val explicitTraits = TraitSet.of(glossPath -> str("Matte"))
    val resolution = NamedFeatures.resolveTicket(ticket, registry, explicitTraits)

    assertEquals(resolution.resolved, explicitTraits)
    assertEquals(resolution.overridden, Chain.empty)

  test("§3.1.3.1: later NamedFeatures override earlier ones on a shared Trait"):
    val matteRegistry = NamedFeatures.registryOf(Map(
      feature("paper", "glossy") -> glossyPaper,
      feature("paper", "matte") -> TraitSet.of(glossPath -> str("Matte"))
    ))
    val features = Chain(feature("paper", "glossy"), feature("paper", "matte"))

    assertEquals(NamedFeatures.implied(features, matteRegistry).get(glossPath), Some(str("Matte")))
    assertEquals(NamedFeatures.implied(features.reverse, matteRegistry).get(glossPath), Some(str("Glossy")))

  test("§3.1.3.1: the empty registry implies nothing"):
    val features = Chain(feature("paper", "glossy"), feature("finish", "matte"))
    assertEquals(NamedFeatures.implied(features, NamedFeatures.emptyRegistry), TraitSet.empty)

  // ---------------------------------------------------------------------------
  // 5. TraitSet: the algebra the override rule rests on
  // ---------------------------------------------------------------------------

  test("TraitSet is a right-biased overlay monoid — identity, associativity, right bias"):
    val monoid = Monoid[TraitSet]
    val a = TraitSet.of(glossPath -> str("Glossy"))
    val b = TraitSet.of(glossPath -> str("Matte"), weightPath -> str("150"))
    val c = TraitSet.of(gradePath -> str("3"))

    assertEquals(monoid.combine(monoid.empty, a), a)
    assertEquals(monoid.combine(a, monoid.empty), a)
    assertEquals(monoid.combine(monoid.combine(a, b), c), monoid.combine(a, monoid.combine(b, c)))
    assertEquals(monoid.combine(a, b).get(glossPath), Some(str("Matte")))
    assertEquals(monoid.combine(a, a), a) // idempotent on itself

  test("TraitSet accessors are deterministic and Show is sorted by XPath"):
    val traits = TraitSet.of(weightPath -> str("150"), glossPath -> str("Glossy"))

    assertEquals(traits.size, 2)
    assert(traits.nonEmpty)
    assert(!traits.isEmpty)
    assert(traits.contains(glossPath))
    assertEquals(traits.paths, List(glossPath, weightPath))
    assertEquals(
      traits.entries.map((tracePath, value) => tracePath.value -> value.value),
      List(glossPath.value -> "Glossy", weightPath.value -> "150")
    )
    assertEquals(Show[TraitSet].show(TraitSet.empty), "TraitSet()")
    assertEquals(TraitSet.fromMap(traits.toMap), traits)

  test("TraitSet keys are the XJDF XPath data type, not the validation locator"):
    // ADR-0013 / N-54: prim.XjdfXPath and model.XPath are different types; the
    // Trait address is a wire XPath expression.
    val traits = TraitSet.of(path(" /XJDF/@JobID \n") -> str("J1"))
    assertEquals(traits.paths.map(_.value), List("/XJDF/@JobID"))

  // ---------------------------------------------------------------------------
  // Reference corpus helpers
  // ---------------------------------------------------------------------------

  private lazy val schema: String = loadReference("schema.xsd")

  private lazy val appendixA: String = loadReference("Appendix A – Data Types and Values.md")

  private lazy val generalIdSchema: String =
    schemaSlice("<xs:element name=\"GeneralID\">", "</xs:element>")

  private def enumerationValues(slice: String): List[String] =
    raw"""<xs:enumeration value="([^"]+)"/>""".r.findAllMatchIn(slice).map(_.group(1)).toList

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

end NamedFeatureLaws
