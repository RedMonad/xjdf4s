package xjdf4s.laws

import xjdf4s.model.*
import xjdf4s.model.elements.Comment
import xjdf4s.prim.*
import xjdf4s.resources.*
import cats.data.{Chain, NonEmptyChain}
import cats.kernel.Monoid
import munit.FunSuite

/** M1.4-2 / ADR-0001: the nominal partial `ChangeOrder` compiles to a
 *  `Patch` and `applyChange` revalidates the result. The degenerate
 *  `XJDF & Partial` alias is gone (N-20).
 */
class ChangeOrderLaws extends FunSuite:

  private def mediaSet(resourceId: Option[Id]): ResourceSet =
    ResourceSet(
      name = ResourceSetName.unsafe("Media"),
      usage = Some(Usage.Input),
      resources = Chain.one(
        Resource(
          specific = Some(ResourcePayload.MediaResource(Media(MediaType.Paper))),
          id = resourceId
        )
      )
    )

  private def ticket(
      jobId: JobId = JobId.unsafe("J1"),
      jobPartId: Option[JobPartId] = None,
      resourceSets: Chain[ResourceSet] = Chain.empty,
      productList: Option[ProductList] = None,
      auditPool: Option[AuditPool] = None,
      types: NonEmptyChain[ProcessType] = NonEmptyChain.one(ProcessType.Product)
  ): XJDF =
    XJDF(
      jobId = jobId,
      types = types,
      jobPartId = jobPartId,
      resourceSets = resourceSets,
      productList = productList,
      auditPool = auditPool
    )

  private def issuesOf(result: cats.data.ValidatedNec[Issue, XJDF]): List[Issue] =
    result.toEither.left.toOption.toList.flatMap(_.toChain.toList)

  test("§1.3.2 / §3.4: change order resourceSets replace a clashing set"):
    val base = ticket(
      types = NonEmptyChain.one(ProcessType.Cutting),
      resourceSets = Chain.one(mediaSet(Some(Id.unsafe("old"))))
    )
    val change = ChangeOrder(
      jobId = base.jobId,
      resourceSets = Chain.one(mediaSet(Some(Id.unsafe("new"))))
    )
    val result = ChangeOrder.applyChange(base, change)
    assert(result.isValid)
    val updated = result.toOption.get
    assertEquals(updated.resourceSets.toList.size, 1)
    assertEquals(updated.resourceSets.toList.head.resources.toList.head.id, Some(Id.unsafe("new")))
    assertEquals(updated.resourceSets.toList.head.select(Part.empty).flatMap(_.id), Some(Id.unsafe("new")))

  test("§3.2: change order auditPool appends audits chronologically"):
    val first = Audit.Created(Header(NmToken.unsafe("MIS"), Timestamp.ofEpochSecond(1)))
    val second = Audit.Status(
      Header(NmToken.unsafe("Press"), Timestamp.ofEpochSecond(100)),
      DeviceInfo(DeviceStatus.Idle)
    )
    val base = ticket(auditPool = Some(AuditPool.of(first)))
    val change = ChangeOrder(jobId = base.jobId, auditPool = Some(AuditPool.of(second)))
    val result = ChangeOrder.applyChange(base, change)
    assert(result.isValid)
    val pool = result.toOption.get.auditPool.get
    assertEquals(pool.toList, List(first, second))
    assert(pool.isChronological)

  test("§3.3: change order productList replaces the BOM"):
    val original = ProductList.of(Product(amount = Some(10L), productType = Some(NmToken.unsafe("Brochure"))))
    val replacement = ProductList.of(Product(amount = Some(42L), productType = Some(NmToken.unsafe("Book"))))
    val base = ticket(productList = Some(original))
    val change = ChangeOrder(jobId = base.jobId, productList = Some(replacement))
    val result = ChangeOrder.applyChange(base, change)
    assert(result.isValid)
    assertEquals(result.toOption.get.productList, Some(replacement))

  test("Table 7.56 / §9.8.2.1.1: change order with a non-existent jobId is rejected"):
    val base = ticket(jobId = JobId.unsafe("J1"))
    val change = ChangeOrder(jobId = JobId.unsafe("OTHER"))
    val result = ChangeOrder.applyChange(base, change)
    assert(result.isInvalid)
    assert(issuesOf(result).exists(_.code.contains(IssueCode.ChangeOrderJobIdMismatch)))

  test("§9.8.2.1.1: change order with a non-matching jobPartId is rejected"):
    val base = ticket(jobPartId = Some(JobPartId.unsafe("Body")))
    val change = ChangeOrder(jobId = base.jobId, jobPartId = Some(JobPartId.unsafe("Cover")))
    val result = ChangeOrder.applyChange(base, change)
    assert(result.isInvalid)
    assert(issuesOf(result).exists(_.code.contains(IssueCode.ChangeOrderJobPartIdMismatch)))

  test("§3.4: change order whose update contains two clashing ResourceSets is rejected"):
    val base = ticket(types = NonEmptyChain.one(ProcessType.Cutting))
    val dup = mediaSet(Some(Id.unsafe("a")))
    val change = ChangeOrder(jobId = base.jobId, resourceSets = Chain(dup, dup))
    val compiled = ChangeOrder.compile(change, base)
    assert(compiled.isInvalid)
    val result = ChangeOrder.applyChange(base, change)
    assert(result.isInvalid)
    assert(issuesOf(result).exists(_.code.contains(IssueCode.ResourceSetClash)))

  test("§6.1.2.1: a change order that breaks parent/child Part keys is caught by revalidation"):
    val parent = PartBuilder.empty.withTokenUnsafe(PartitionKey.Separation, NmToken.unsafe("Cyan")).build
    val child = PartBuilder.empty.withTokenUnsafe(PartitionKey.Separation, NmToken.unsafe("Cyan")).build
    val lawful = Resource(
      specific = Some(ResourcePayload.MediaResource(Media(MediaType.Paper))),
      parts = Chain.one(parent)
    )
    val shadowing = lawful.copy(amountPool = Some(AmountPool.of(PartAmount(parts = Chain.one(child)))))
    val base = ticket(
      types = NonEmptyChain.one(ProcessType.Cutting),
      resourceSets = Chain.one(
        ResourceSet(ResourceSetName.unsafe("Media"), usage = Some(Usage.Input), resources = Chain.one(lawful))
      )
    )
    val change = ChangeOrder(
      jobId = base.jobId,
      resourceSets = Chain.one(
        ResourceSet(ResourceSetName.unsafe("Media"), usage = Some(Usage.Input), resources = Chain.one(shadowing))
      )
    )
    assert(ChangeOrder.compile(change, base).isValid, "the update is internally consistent")
    val result = ChangeOrder.applyChange(base, change)
    assert(result.isInvalid)
    assert(issuesOf(result).exists(_.code.contains(IssueCode.PartKeyShadowsParent)))

  test("Patch monoid action is preserved: applyTo(applyTo(t, p), q) == applyTo(t, p |+| q)"):
    val base = ticket()
    val c1 = ChangeOrder(jobId = base.jobId, comments = Chain.one(Comment("one")))
    val c2 = ChangeOrder(jobId = base.jobId, comments = Chain.one(Comment("two")))
    val p = ChangeOrder.compile(c1, base).toOption.get
    val q = ChangeOrder.compile(c2, base).toOption.get
    val sequential = q.applyTo(p.applyTo(base))
    val combined = Monoid[Patch].combine(p, q).applyTo(base)
    assertEquals(sequential, combined)
    assertEquals(sequential.comments.toList.map(_.text.value), List("one", "two"))

end ChangeOrderLaws
