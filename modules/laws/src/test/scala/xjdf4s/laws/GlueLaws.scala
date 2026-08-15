package xjdf4s.laws

import xjdf4s.model.elements.{Glue => GlueElement}
import xjdf4s.model.{IssueCode, XPath}
import xjdf4s.prim.*
import munit.FunSuite

/** Tests for the `Glue` element (Table 8.29) and its SHALL rules.
 *
 *  Normative references:
 *  - Table 8.29 (`Glue` element, §8.24)
 *  - Example 8.15 (`GlueType="Removable"`)
 *  - ADR-0011 (two Glue enumerations)
 */
class GlueLaws extends FunSuite:

  private val at = XPath("/XJDF/Glue")

  // --- Positive tests -------------------------------------------------------

  test("Table 8.29: valid Glue with all attributes passes law check") {
    val glue = GlueElement(
      areaGlue = Some(true),
      glueLineWidth = Some(12.5),
      glueRef = Some(IdRef.unsafe("MC1")),
      glueType = Some(GlueType.Hotmelt),
      gluingPattern = Some(FloatList.of(1.0, 0.5, 2.0, 0.5)),
      gluingTechnique = Some(GluingTechnique.SpineGluing),
      meltingTemperature = Some(180L),
      startPosition = Some(XYPair(10.0, 20.0)),
      workingDirection = Some(Face.Front),
      workingPath = Some(XYPair(100.0, 0.0))
    )
    val issues = GlueElement.law(glue, at)
    assertEquals(issues.toList, Nil)
  }

  test("Table 8.29: valid Glue with only @GlueType=Permanent and no melting temp") {
    val glue = GlueElement(glueType = Some(GlueType.Permanent))
    val issues = GlueElement.law(glue, at)
    assertEquals(issues.toList, Nil)
  }

  test("Table 8.29: valid Glue with @GlueType=PUR and @MeltingTemperature") {
    val glue = GlueElement(glueType = Some(GlueType.PUR), meltingTemperature = Some(120L))
    val issues = GlueElement.law(glue, at)
    assertEquals(issues.toList, Nil)
  }

  test("Table 8.29: empty Glue element is valid (all attributes optional)") {
    val glue = GlueElement()
    val issues = GlueElement.law(glue, at)
    assertEquals(issues.toList, Nil)
  }

  test("Table 8.29: Glue with even-length @GluingPattern (1 0)") {
    val glue = GlueElement(gluingPattern = Some(FloatList.of(1.0, 0.0)))
    val issues = GlueElement.law(glue, at)
    assertEquals(issues.toList, Nil)
  }

  // --- Negative tests: @GluingPattern even entries ----------------------------

  test("Table 8.29: @GluingPattern with odd entries is invalid") {
    val glue = GlueElement(gluingPattern = Some(FloatList.of(1.0, 0.5, 2.0)))
    val issues = GlueElement.law(glue, at)
    assertEquals(issues.size.toInt, 1)
    assertEquals(issues.toList.head.code, Some(IssueCode.GluePatternOdd))
  }

  test("Table 8.29: @GluingPattern with single entry is invalid") {
    val glue = GlueElement(gluingPattern = Some(FloatList.of(5.0)))
    val issues = GlueElement.law(glue, at)
    assertEquals(issues.size.toInt, 1)
    assertEquals(issues.toList.head.code, Some(IssueCode.GluePatternOdd))
  }

  // --- Negative tests: @MeltingTemperature rule -----------------------------

  test("Table 8.29: @MeltingTemperature with @GlueType=ColdGlue is invalid") {
    val glue = GlueElement(glueType = Some(GlueType.ColdGlue), meltingTemperature = Some(50L))
    val issues = GlueElement.law(glue, at)
    assertEquals(issues.size.toInt, 1)
    assertEquals(issues.toList.head.code, Some(IssueCode.GlueMeltingTempWithoutHeat))
  }

  test("Table 8.29: @MeltingTemperature with @GlueType=Permanent is invalid") {
    val glue = GlueElement(glueType = Some(GlueType.Permanent), meltingTemperature = Some(100L))
    val issues = GlueElement.law(glue, at)
    assertEquals(issues.size.toInt, 1)
    assertEquals(issues.toList.head.code, Some(IssueCode.GlueMeltingTempWithoutHeat))
  }

  test("Table 8.29: @MeltingTemperature with @GlueType=Removable is invalid") {
    val glue = GlueElement(glueType = Some(GlueType.Removable), meltingTemperature = Some(80L))
    val issues = GlueElement.law(glue, at)
    assertEquals(issues.size.toInt, 1)
    assertEquals(issues.toList.head.code, Some(IssueCode.GlueMeltingTempWithoutHeat))
  }

  test("Table 8.29: @MeltingTemperature without any @GlueType is invalid") {
    val glue = GlueElement(meltingTemperature = Some(150L))
    val issues = GlueElement.law(glue, at)
    assertEquals(issues.size.toInt, 1)
    assertEquals(issues.toList.head.code, Some(IssueCode.GlueMeltingTempWithoutHeat))
  }

  // --- IDREF collection -----------------------------------------------------

  test("Table 8.29: @GlueRef is collected in references") {
    val glue = GlueElement(glueRef = Some(IdRef.unsafe("MC42")))
    assertEquals(GlueElement.references(glue).toList, List(IdRef.unsafe("MC42")))
  }

  test("Table 8.29: Glue without @GlueRef yields empty references") {
    val glue = GlueElement(glueType = Some(GlueType.PUR))
    assertEquals(GlueElement.references(glue).toList, Nil)
  }

  // --- Regression: N-50 / ADR-0011 ------------------------------------------

  test("ADR-0011: GlueType (5 values) includes Permanent and Removable") {
    assertEquals(
      GlueType.all.map(_.token.value).toSet,
      Set("ColdGlue", "Hotmelt", "Permanent", "PUR", "Removable")
    )
  }

  test("ADR-0011: EnumGlue (3 values) matches Table A.24") {
    assertEquals(
      EnumGlue.all.map(_.token.value).toSet,
      Set("ColdGlue", "Hotmelt", "PUR")
    )
  }

end GlueLaws
