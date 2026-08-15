package xjdf4s.laws

import xjdf4s.prim.*
import munit.FunSuite

import java.io.File
import scala.io.Source

/** Wire tokens of the closed XJDF enumerations (§1.10.3.1) and extensibility of
 *  the open catalogs (§1.10.3.2).
 *
 *  Two complementary levels of checking, per ADR-0007:
 *
 *    1. golden sets written out as literals next to the test, each carrying the
 *       normative table it was copied from — these fail loudly if a value is
 *       added, removed or renamed by accident;
 *    2. a machine cross-check of every closed enum against the tables of
 *       Appendix A under `reference/xjdf` — a fast transcription of a table
 *       tends to lose exactly the values marked *New in XJDF 2.1 / 2.2*, which
 *       is how N-06, N-07, N-47, N-48 and N-49 came about.
 */
class EnumLaws extends FunSuite:

  // ---------------------------------------------------------------------------
  // 1. Golden wire-token sets
  // ---------------------------------------------------------------------------

  private def tokensOf[E <: XjdfEnum](all: List[E]): Set[String] =
    all.map(_.token.value).toSet

  test("Table A.40: Sides wire tokens"):
    assertEquals(
      tokensOf(Sides.all),
      Set("OneSided", "OneSidedBack", "TwoSidedHeadToFoot", "TwoSidedHeadToHead", "Unprinted")
    )

  test("Table A.15: DeviceStatus wire tokens"):
    assertEquals(
      tokensOf(DeviceStatus.all),
      Set("Cleanup", "Idle", "NonProductive", "Offline", "Production", "Setup", "Stopped")
    )

  test("Table 4.11, Sheet 1: HardCoverBinding/@Jacket wire tokens"):
    assertEquals(tokensOf(HardCoverJacket.all), Set("None", "Loose", "Glue"))

  test("Table 4.11, Sheet 1: the Jacket token of a glued jacket is 'Glue', not 'Glued'"):
    // Regression for N-08: the Scala name is GlueApplied, the wire token is Glue.
    assertEquals(HardCoverJacket.GlueApplied.token.value, "Glue")
    assertEquals(HardCoverJacket.Unjacketed.token.value, "None")

  test("Table A.26: ISOPaperSubstrate wire tokens"):
    assertEquals(
      tokensOf(ISOPaperSubstrate.all),
      Set(
        "LWCPlus",
        "LWCStandard",
        "NewsPlus",
        "PS1",
        "PS2",
        "PS3",
        "PS4",
        "PS5",
        "PS6",
        "PS7",
        "PS8",
        "PS9",
        "SCPlus",
        "SCStandard",
        "SNP"
      )
    )

  test("Table A.30: MediaType wire tokens"):
    assertEquals(
      tokensOf(MediaType.all),
      Set(
        "Blanket",
        "CorrugatedBoard",
        "Disc",
        "EmbossingFoil",
        "Film",
        "Foil",
        "GravureCylinder",
        "ImagingCylinder",
        "LaminatingFoil",
        "MountingTape",
        "Other",
        "Paper",
        "Plate",
        "Screen",
        "SelfAdhesive",
        "ShrinkFoil",
        "Sleeve",
        "Synthetic",
        "Textile",
        "Transparency",
        "Vinyl"
      )
    )

  test("Table A.36: Scope wire tokens"):
    assertEquals(tokensOf(Scope.all), Set("Allowed", "Device", "Estimate", "Job", "Present"))

  test("Table A.8 / A.11 / A.46: the '→ None' token family"):
    // Scala reserves `None`, so four enumerations rename the case and map the
    // token explicitly (ROADMAP Appendix C).
    assertEquals(BindingType.NoBinding.token.value, "None")
    assertEquals(BindingOrder.Unbound.token.value, "None")
    assertEquals(Coating.Uncoated.token.value, "None")
    assertEquals(SoftCoverScoring.Unscored.token.value, "None")
    assertEquals(HardCoverJacket.Unjacketed.token.value, "None")

  test("fromToken round-trips every value of every closed enumeration"):
    val companions: List[(String, List[XjdfEnum], NmToken => Option[XjdfEnum])] =
      List(
        ("Sides", Sides.all, Sides.fromToken),
        ("DeviceStatus", DeviceStatus.all, DeviceStatus.fromToken),
        ("HardCoverJacket", HardCoverJacket.all, HardCoverJacket.fromToken),
        ("ISOPaperSubstrate", ISOPaperSubstrate.all, ISOPaperSubstrate.fromToken),
        ("MediaType", MediaType.all, MediaType.fromToken),
        ("Scope", Scope.all, Scope.fromToken),
        ("BindingType", BindingType.all, BindingType.fromToken),
        ("Coating", Coating.all, Coating.fromToken),
        ("SoftCoverScoring", SoftCoverScoring.all, SoftCoverScoring.fromToken)
      )
    companions.foreach: (name, all, fromToken) =>
      all.foreach: v =>
        assertEquals(fromToken(v.token), Some(v), s"$name: ${v.token.value}")

  test("closed enumerations have no duplicate wire tokens"):
    val closed: List[(String, List[XjdfEnum])] =
      List(
        "Sides"             -> Sides.all,
        "DeviceStatus"      -> DeviceStatus.all,
        "HardCoverJacket"   -> HardCoverJacket.all,
        "ISOPaperSubstrate" -> ISOPaperSubstrate.all,
        "MediaType"         -> MediaType.all,
        "Scope"             -> Scope.all,
        "Status"            -> Status.all,
        "BindingType"       -> BindingType.all
      )
    closed.foreach: (name, all) =>
      assertEquals(all.map(_.token.value).distinct.size, all.size, s"$name has duplicate tokens")

  // ---------------------------------------------------------------------------
  // 2. Open catalogs (§1.10.3.2)
  // ---------------------------------------------------------------------------

  test("§A.2.30: NamedColor is an open catalog and accepts a value outside the list"):
    // §1.10.3.2: “This does not preclude the use of other values as required by
    // vendor or customer extensions.” Pantone185C is a valid NMTOKEN that is
    // not among the 147 [Color Names] values — a genuine vendor extension that
    // the old closed 16-value enum could not express (N-09).
    val outside = NmToken.from("Pantone185C")
    assertEquals(outside.map(_.value), Some("Pantone185C"))
    assert(!Catalog.NamedColor.recommended.contains(NmToken.unsafe("Pantone185C")))

  test("§A.2.30: Catalog.NamedColor carries all 147 [Color Names] values"):
    // [Color Names] = SVG 1.1 Second Edition (Appendix G); the same 147 names
    // appear as xs:pattern entries of `NamedColor` in schema.xsd.
    assertEquals(Catalog.NamedColor.recommended.size, 147)
    assertEquals(Catalog.NamedColor.recommended.distinct.size, 147)
    List("AliceBlue", "MintCream", "RebeccaPurple", "YellowGreen").foreach: name =>
      val present = Catalog.NamedColor.recommended.exists(_.value == name)
      // RebeccaPurple is NOT part of SVG 1.1 — it was added in CSS Color 4.
      assertEquals(present, name != "RebeccaPurple", name)

  test("§A.2.30: the values dropped by the old 16-value enum are now expressible"):
    val previouslyMissing = List("MintCream", "SeaGreen", "Turquoise", "PapayaWhip", "Chartreuse")
    previouslyMissing.foreach: name =>
      assert(
        Catalog.NamedColor.recommended.exists(_.value == name),
        s"$name missing from Catalog.NamedColor"
      )

  // ---------------------------------------------------------------------------
  // 3. Machine cross-check against Appendix A
  // ---------------------------------------------------------------------------

  /** The closed enumerations that are defined by a table of Appendix A,
   *  section A.2, together with the name the specification uses for them.
   */
  private val appendixAEnums: List[(String, List[XjdfEnum])] =
    List(
      "Anchor"            -> Anchor.all,
      "Automation"        -> Automation.all,
      "BindingType"       -> BindingType.all,
      "Coating"           -> Coating.all,
      "DeviceStatus"      -> DeviceStatus.all,
      "Edge"              -> Edge.all,
      "Face"              -> Face.all,
      "FitPolicy"         -> FitPolicy.all,
      "Glue"              -> GlueType.all,
      "ISOPaperSubstrate" -> ISOPaperSubstrate.all,
      "MediaDirection"    -> MediaDirection.all,
      "MediaType"         -> MediaType.all,
      "Opacity"           -> Opacity.all,
      "Orientation"       -> Orientation.all,
      "Scope"             -> Scope.all,
      "Severity"          -> SeverityClass.all,
      "SheetLay"          -> SheetLay.all,
      "Side"              -> Side.all,
      "Sides"             -> Sides.all,
      "SpreadType"        -> SpreadType.all,
      "StapleShape"       -> StapleShape.all,
      "Status"            -> Status.all,
      "TightBacking"      -> TightBacking.all,
      "Usage"             -> Usage.all
    )

  /** Values that the normative text lists but this model deliberately omits,
   *  each with the reason. Empty by design: an entry here is a deviation and
   *  belongs in ROADMAP Appendix C as well.
   */
  private val acceptedOmissions: Map[String, Set[String]] = Map.empty

  private lazy val appendixA: String =
    def findUp(from: File, depth: Int): Option[File] =
      val candidate = File(from, "reference/xjdf/Appendix A – Data Types and Values.md")
      if candidate.isFile then Some(candidate)
      else if depth == 0 || from.getParentFile == null then None
      else findUp(from.getParentFile, depth - 1)
    val file = findUp(File(".").getAbsoluteFile, 5).getOrElse(
      fail("reference/xjdf/Appendix A – Data Types and Values.md not found from " + File(".").getAbsolutePath)
    )
    val source = Source.fromFile(file, "UTF-8")
    try source.mkString
    finally source.close()

  /** The value column of the enumeration table of `name`, in table order. */
  private def specValues(name: String): List[String] =
    val header = raw"\*\*Table A\.\d+: " + java.util.regex.Pattern.quote(name) + raw" Enumeration Values\*\*"
    val lines  = appendixA.linesIterator.toList
    val start  = lines.indexWhere(_.matches(header))
    assert(start >= 0, s"no Appendix A table found for enumeration $name")
    val row = raw"^\|\s*`([^`]+)`.*".r
    lines
      .drop(start + 1)
      .dropWhile(l => !l.trim.startsWith("|")) // blank line between caption and table
      .takeWhile(l => l.trim.startsWith("|"))
      .collect { case row(value) => value }
      .distinct

  test("Appendix A: every closed enumeration matches its normative table exactly"):
    val problems = appendixAEnums.flatMap: (name, all) =>
      val modelled = all.map(_.token.value).toSet
      val expected = specValues(name).toSet -- acceptedOmissions.getOrElse(name, Set.empty)
      val missing  = (expected -- modelled).toList.sorted
      val extra    = (modelled -- expected).toList.sorted
      Option.when(missing.nonEmpty || extra.nonEmpty)(
        s"$name: missing=${missing.mkString(",")} extra=${extra.mkString(",")}"
      )
    assertEquals(problems, List.empty[String], problems.mkString("\n"))

  test("Appendix A: the tables cross-checked above are non-empty and were really parsed"):
    // Guards the cross-check itself: a silently empty table would make the
    // comparison above pass for the wrong reason.
    appendixAEnums.foreach: (name, all) =>
      assert(specValues(name).nonEmpty, s"$name: parsed an empty table")
      assert(all.nonEmpty, s"$name: no modelled values")

end EnumLaws
