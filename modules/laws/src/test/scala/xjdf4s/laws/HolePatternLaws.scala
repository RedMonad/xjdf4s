package xjdf4s.laws

import xjdf4s.model.{IssueCode, XPath}
import xjdf4s.model.elements.HolePattern
import xjdf4s.prim.*
import munit.FunSuite

/** Tests for the `HolePattern` element (Table 8.30) and its SHALL rule.
 *
 *  Normative references:
 *  - Table 8.30 (`HolePattern` element, §8.25)
 *  - Appendix F (Hole Pattern Catalog)
 *  - schema.xsd `HolePattern` (all nine attributes optional, SHALL for @Pattern)
 */
class HolePatternLaws extends FunSuite:

  private val at = XPath("/XJDF/HolePattern")

  // --- Positive tests -------------------------------------------------------

  test("Table 8.30: fully populated HolePattern with Pattern passes law check") {
    val hp = HolePattern(
      center = Some(XYPair(10.0, 20.0)),
      centerReference = Some(HoleCenterReference.TrailingEdge),
      extent = Some(XYPair(5.0, 5.0)),
      holeCount = Some(IntegerList.of(2L, 2L)),
      pattern = Some(Catalog.HolePattern.R2mDIN),
      pitch = Some(XYPair(80.0, 0.0)),
      referenceEdge = Some(HoleReferenceEdge.Left),
      reinforcement = Some(Catalog.HoleReinforcement.Grommet),
      shape = Some(HoleShape.Round)
    )
    val issues = HolePattern.law(hp, at)
    assertEquals(issues.toList, Nil)
  }

  test("Table 8.30: HolePattern with only @Pattern (center/extent/shape from pattern) is valid") {
    val hp = HolePattern(pattern = Some(Catalog.HolePattern.R2Generic))
    val issues = HolePattern.law(hp, at)
    assertEquals(issues.toList, Nil, "Center/Extent/Shape missing but Pattern present — SHALL satisfied")
  }

  test("Table 8.30: HolePattern with center, extent, shape all present and no pattern is valid") {
    val hp = HolePattern(
      center = Some(XYPair(0.0, 0.0)),
      extent = Some(XYPair(6.0, 6.0)),
      shape = Some(HoleShape.Round)
    )
    val issues = HolePattern.law(hp, at)
    assertEquals(issues.toList, Nil, "All three required fields present, pattern not needed")
  }

  test("Table 8.30: Pattern-only with R4m-DIN-A4 passes") {
    val hp = HolePattern(pattern = Some(Catalog.HolePattern.R4mDINA4))
    assertEquals(HolePattern.law(hp, at).toList, Nil)
  }

  test(
    "Table 8.30: empty HolePattern without center/extent/shape but with pattern is valid per SHALL (pattern required when any missing)"
  ) {
    // Empty center/extent/shape -> needs pattern; pattern empty would fail, but here pattern present is valid.
    // Actually this test checks that pattern present satisfies the SHALL even if all three are missing.
    val hp = HolePattern(pattern = Some(Catalog.HolePattern.R3Generic))
    assertEquals(HolePattern.law(hp, at).toList, Nil)
  }

  // --- Negative tests: Pattern required -----------------------------------

  test("Table 8.30: HolePattern missing @Pattern when @Center is absent is invalid") {
    val hp = HolePattern(
      extent = Some(XYPair(5.0, 5.0)),
      shape = Some(HoleShape.Round)
      // center missing, pattern missing
    )
    val issues = HolePattern.law(hp, at)
    assertEquals(issues.size.toInt, 1)
    assertEquals(issues.toList.head.code, Some(IssueCode.HolePatternPatternRequired))
  }

  test("Table 8.30: HolePattern missing @Pattern when @Extent is absent is invalid") {
    val hp = HolePattern(
      center = Some(XYPair(0.0, 0.0)),
      shape = Some(HoleShape.Round)
    )
    val issues = HolePattern.law(hp, at)
    assertEquals(issues.size.toInt, 1)
    assertEquals(issues.toList.head.code, Some(IssueCode.HolePatternPatternRequired))
  }

  test("Table 8.30: HolePattern missing @Pattern when @Shape is absent is invalid") {
    val hp = HolePattern(
      center = Some(XYPair(0.0, 0.0)),
      extent = Some(XYPair(5.0, 5.0))
    )
    val issues = HolePattern.law(hp, at)
    assertEquals(issues.size.toInt, 1)
    assertEquals(issues.toList.head.code, Some(IssueCode.HolePatternPatternRequired))
  }

  test("Table 8.30: completely empty HolePattern is invalid (needs @Pattern)") {
    val hp = HolePattern()
    val issues = HolePattern.law(hp, at)
    assertEquals(issues.size.toInt, 1)
    assertEquals(issues.toList.head.code, Some(IssueCode.HolePatternPatternRequired))
  }

  test("Table 8.30: HolePattern with only center, missing extent/shape/pattern is invalid") {
    val hp = HolePattern(center = Some(XYPair(0.0, 0.0)))
    val issues = HolePattern.law(hp, at)
    assertEquals(issues.size.toInt, 1)
    assertEquals(issues.toList.head.code, Some(IssueCode.HolePatternPatternRequired))
  }

  // --- Open catalog extensibility ------------------------------------------

  test("Table 8.30 / Appendix F: Catalog.HolePattern is open — accepts value outside list") {
    val outside = NmToken.from("CustomHole-42")
    assertEquals(outside.map(_.value), Some("CustomHole-42"))
    assert(!Catalog.HolePattern.recommended.contains(NmToken.unsafe("CustomHole-42")))
  }

  test("Table 8.30: Catalog.HolePattern carries 34 values (including None from XSD)") {
    assertEquals(Catalog.HolePattern.recommended.size, 34)
    assertEquals(Catalog.HolePattern.recommended.distinct.size, 34)
    // Spot-check a few normative tokens
    assert(Catalog.HolePattern.recommended.contains(NmToken.unsafe("R2m-DIN")))
    assert(Catalog.HolePattern.recommended.contains(NmToken.unsafe("R4m-DIN-A4")))
    assert(Catalog.HolePattern.recommended.contains(NmToken.unsafe("P16_9i-rect-0t")))
    assert(Catalog.HolePattern.recommended.contains(NmToken.unsafe("None")))
  }

  test("Table 8.30: Catalog.HoleReinforcement carries Grommet") {
    assert(Catalog.HoleReinforcement.recommended.contains(NmToken.unsafe("Grommet")))
  }

  // --- Mapping tests --------------------------------------------------------

  test("Table 8.30: attribute mapping and wire tokens for enums") {
    val hp = HolePattern(
      centerReference = Some(HoleCenterReference.RegistrationMark),
      referenceEdge = Some(HoleReferenceEdge.Pattern),
      shape = Some(HoleShape.Rectangular),
      pattern = Some(Catalog.HolePattern.R2Generic)
    )
    assertEquals(hp.centerReference.map(_.token.value), Some("RegistrationMark"))
    assertEquals(hp.referenceEdge.map(_.token.value), Some("Pattern"))
    assertEquals(hp.shape.map(_.token.value), Some("Rectangular"))
    assertEquals(hp.pattern.map(_.value), Some("R2-generic"))
  }

end HolePatternLaws
