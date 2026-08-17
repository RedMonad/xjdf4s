package xjdf4s.core

/** Discovered munit suite that forces all eager checks of the core module (MD-08). */
class CoreChecksSuite extends munit.FunSuite:
  test("all core checks pass") {
    val results: Vector[Unit] = Vector(
      CardinalityChecks.nonEmptyConstruction,
      CardinalityChecks.refinedValues,
      PrimitiveValueChecks.idRefLexicalSpace,
      PrimitiveValueChecks.xjdfStringNormalization,
      PrimitiveValueChecks.priorityBounds,
      PrimitiveValueChecks.dateTimeRequiresZoneAndCalendar,
      PrimitiveValueChecks.durationRequiresAtLeastOneComponent,
      PrimitiveValueChecks.foreignNamesRejectStandardNamespace,
      PrimitiveValueChecks.extensionContentPreservesOrder,
    )
    assert(results.size == 9)
  }
end CoreChecksSuite
