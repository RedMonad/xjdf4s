package xjdf4s.laws

import xjdf4s.model.*
import xjdf4s.prim.*
import xjdf4s.resources.*
import cats.data.{Chain, NonEmptyChain}
import munit.FunSuite

/** M1.1-2 / M1.1-3: the single §3.4 conflict predicate (`ResourceSet.clashesWith`) and
 *  the correct `Patch.mergeResourceSets` — the update replaces conflicting sets
 *  instead of concatenating them (N-02).
 */
class PatchLaws extends FunSuite:

  private def mediaSet(
      cpi: Option[NonEmptyChain[ProcessIndex]],
      usage: Option[Usage] = Some(Usage.Input),
      resourceId: Option[Id] = None
  ): ResourceSet =
    ResourceSet(
      name = ResourceSetName.unsafe("Media"),
      usage = usage,
      combinedProcessIndex = cpi,
      resources = Chain.one(
        Resource(
          specific = ResourcePayload.MediaResource(Media(MediaType.Paper)),
          id = resourceId
        )
      )
    )

  private def nodeInfoSet(cpi: Option[NonEmptyChain[ProcessIndex]], resourceId: Option[Id] = None): ResourceSet =
    ResourceSet(
      name = ResourceSetName.unsafe("NodeInfo"),
      usage = Some(Usage.Input),
      combinedProcessIndex = cpi,
      resources = Chain.one(
        Resource(
          specific = ResourcePayload.NodeInfoResource(NodeInfo()),
          id = resourceId
        )
      )
    )

  private def ticket(sets: ResourceSet*): XJDF =
    XJDF(
      jobId = JobId.unsafe("PatchLaws"),
      types = NonEmptyChain.of(ProcessType.Cutting, ProcessType.Folding),
      resourceSets = Chain.fromSeq(sets)
    )

  test("§3.4: exact key equality clashes"):
    val a = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))))
    assert(ResourceSet.clashesWith(a, mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))))))

  test("§3.4: partial CombinedProcessIndex overlap clashes"):
    val wide = mediaSet(Some(NonEmptyChain.of(ProcessIndex.unsafe(0), ProcessIndex.unsafe(1))))
    val narrow = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))))
    assert(ResourceSet.clashesWith(wide, narrow))

  test("§3.4: absent CPI clashes with present CPI"):
    val none = mediaSet(None)
    val some = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(1))))
    assert(ResourceSet.clashesWith(none, some))

  test("§3.4: disjoint CPI do not clash"):
    val zero = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))))
    val one = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(1))))
    assert(!ResourceSet.clashesWith(zero, one))

  test("merge without conflict keeps both and is Right"):
    val t = ticket(mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0)))))
    val update = Chain.one(mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(1)))))
    val merged = Patch.mergeResourceSets(t, update)
    assert(merged.isRight)
    assertEquals(merged.toOption.map(_.resourceSets.toList.size), Some(2))

  test("merge with exact key conflict replaces the old set (Both) and validates"):
    val old = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))), resourceId = Some(Id.unsafe("old")))
    val fresh = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))), resourceId = Some(Id.unsafe("new")))
    val merged = Patch.mergeResourceSets(ticket(old), Chain.one(fresh))
    assert(merged.isBoth)
    val result = merged.toOption.get
    assertEquals(result.resourceSets.toList.size, 1)
    assertEquals(result.resourceSets.toList.head.resources.toList.head.id, Some(Id.unsafe("new")))
    assert(result.validate.isValid)

  test("merge with partial CPI overlap replaces (Both) — §3.4 N-16"):
    val old = mediaSet(Some(NonEmptyChain.of(ProcessIndex.unsafe(0), ProcessIndex.unsafe(1))), resourceId = Some(Id.unsafe("old")))
    val fresh = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))), resourceId = Some(Id.unsafe("new")))
    val merged = Patch.mergeResourceSets(ticket(old), Chain.one(fresh))
    assert(merged.isBoth)
    val result = merged.toOption.get
    assertEquals(result.resourceSets.toList.size, 1)
    assertEquals(result.resourceSets.toList.head.resources.toList.head.id, Some(Id.unsafe("new")))

  test("merge with absent-CPI vs present-CPI replaces (Both)"):
    val old = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(1))), resourceId = Some(Id.unsafe("old")))
    val fresh = mediaSet(None, resourceId = Some(Id.unsafe("new")))
    val merged = Patch.mergeResourceSets(ticket(old), Chain.one(fresh))
    assert(merged.isBoth)
    assertEquals(merged.toOption.get.resourceSets.toList.size, 1)

  test("merge replaces several conflicting sets at once"):
    val t = ticket(
      mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))), resourceId = Some(Id.unsafe("m0"))),
      nodeInfoSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))), Some(Id.unsafe("n0")))
    )
    val update = Chain(
      mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))), resourceId = Some(Id.unsafe("m0new"))),
      nodeInfoSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))), Some(Id.unsafe("n0new")))
    )
    val merged = Patch.mergeResourceSets(t, update)
    assert(merged.isBoth)
    val ids = merged.toOption.get.resourceSets.flatMap(_.resources).map(_.id).toList.toSet
    assertEquals(ids, Set[Option[Id]](Some(Id.unsafe("m0new")), Some(Id.unsafe("n0new"))))

  test("merge rejects an internally conflicting update (Left)"):
    val dup = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))))
    assert(Patch.mergeResourceSets(ticket(), Chain(dup, dup)).isLeft)
    val partial = Chain(
      mediaSet(Some(NonEmptyChain.of(ProcessIndex.unsafe(0), ProcessIndex.unsafe(1)))),
      mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))))
    )
    assert(Patch.mergeResourceSets(ticket(), partial).isLeft)

  test("merge: the old ResourceSet no longer wins selection (§6.1.3.2)"):
    val old = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))), resourceId = Some(Id.unsafe("old")))
    val fresh = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))), resourceId = Some(Id.unsafe("new")))
    val result = Patch.mergeResourceSets(ticket(old), Chain.one(fresh)).toOption.get
    assertEquals(result.resourceSets.toList.head.select(Part.empty).flatMap(_.id), Some(Id.unsafe("new")))

  test("merge is idempotent on the resource sets"):
    val old = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))), resourceId = Some(Id.unsafe("old")))
    val fresh = mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))), resourceId = Some(Id.unsafe("new")))
    val first = Patch.mergeResourceSets(ticket(old), Chain.one(fresh)).toOption.get
    val second = Patch.mergeResourceSets(first, Chain.one(fresh)).toOption.get
    assertEquals(first.resourceSets.toList, second.resourceSets.toList)

  test("after merge no two ResourceSets clash and the ticket validates"):
    val t = ticket(
      mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0)))),
      nodeInfoSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(0))))
    )
    val update = Chain(
      mediaSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(1)))),
      nodeInfoSet(Some(NonEmptyChain.one(ProcessIndex.unsafe(1))))
    )
    val result = Patch.mergeResourceSets(t, update).toOption.get
    val list = result.resourceSets.toList
    val clash = list.zipWithIndex.exists { case (a, i) => list.drop(i + 1).exists(b => ResourceSet.clashesWith(a, b)) }
    assert(!clash)
    assert(result.validate.isValid)
end PatchLaws
