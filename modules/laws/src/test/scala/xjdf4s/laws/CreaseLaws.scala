package xjdf4s.laws

import xjdf4s.model.*
import xjdf4s.model.elements.Crease
import xjdf4s.prim.*
import xjdf4s.resources.*
import cats.data.{Chain, NonEmptyChain}
import munit.FunSuite

/** The `Crease` element (Table 8.17) and its `FoldingParams` container
 *  (Table 6.74): table-to-type mapping and container-level validation
 *  (M1.6-2, PR-15). `Crease` carries no local SHALL rules — all four
 *  attributes are optional — so the suite guards the mapping and the wiring;
 *  the closed `WorkingDirection` enumeration is golden-checked in `EnumLaws`
 *  (Table A.50).
 */
class CreaseLaws extends FunSuite:

  private def ticketWithFoldingParams(creases: Chain[Crease]): XJDF =
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("FoldingParams"),
      usage = Some(Usage.Input),
      resources = Chain.one(
        Resource.of(ResourcePayload.FoldingParamsResource(FoldingParams(creases = creases)))
      )
    )
    XJDF(
      jobId = JobId.unsafe("CreaseLaws"),
      types = NonEmptyChain.one(ProcessType.Folding),
      resourceSets = Chain.one(rs)
    )

  test("Table 8.17: a fully populated Crease in FoldingParams constructs and validates"):
    val crease = Crease(
      depth = Some(Microns(150.0)),
      startPosition = Some(XYPair(0.0, 0.0)),
      workingDirection = Some(WorkingDirection.Top),
      workingPath = Some(XYPair(595.28, 0.0))
    )
    assert(ticketWithFoldingParams(Chain.one(crease)).validate.isValid)

  test("Table 8.17: an empty Crease (all four attributes optional) is representable"):
    assert(ticketWithFoldingParams(Chain.one(Crease())).validate.isValid)

  test("Table 6.74: multiple Crease elements (Crease*) in one FoldingParams"):
    val creases = Chain(
      Crease(depth = Some(Microns(100.0))),
      Crease(workingDirection = Some(WorkingDirection.Bottom))
    )
    assert(ticketWithFoldingParams(creases).validate.isValid)

  test("Table 6.74: FoldingParams without creases stays valid (Crease* allows zero)"):
    assert(ticketWithFoldingParams(Chain.empty).validate.isValid)

  test("Table 8.17 / Table A.50: attribute mapping and wire tokens"):
    val crease = Crease(
      depth = Some(Microns(150.0)),
      startPosition = Some(XYPair(0.0, 0.0)),
      workingDirection = Some(WorkingDirection.Top),
      workingPath = Some(XYPair(595.28, 0.0))
    )
    assertEquals(crease.depth.map(_.value), Some(150.0))
    assertEquals(crease.startPosition, Some(XYPair(0.0, 0.0)))
    assertEquals(crease.workingDirection, Some(WorkingDirection.Top))
    assertEquals(crease.workingDirection.map(_.token.value), Some("Top"))
    assertEquals(crease.workingPath, Some(XYPair(595.28, 0.0)))
    assertEquals(WorkingDirection.Bottom.token.value, "Bottom")

end CreaseLaws
