package xjdf4s.laws

import xjdf4s.dsl.dsl
import xjdf4s.model.*
import xjdf4s.prim.*
import xjdf4s.resources.*
import cats.data.{Chain, NonEmptyChain, ValidatedNec}
import munit.ScalaCheckSuite

/** Structural validation against the XJDF specification, driven by the examples
 *  of Chapter 3 and 5 (valid tickets stay valid; every violation is detected).
 */
class TicketLaws extends ScalaCheckSuite:

  private def ticket(
      types: NonEmptyChain[ProcessType],
      resourceSets: Chain[ResourceSet] = Chain.empty,
      productList: Option[ProductList] = None,
      auditPool: Option[AuditPool] = None
  ): XJDF =
    XJDF(
      jobId = JobId.unsafe("TicketLaws"),
      types = types,
      resourceSets = resourceSets,
      productList = productList,
      auditPool = auditPool
    )

  test("Example 3.1: the minimal product ticket is valid"):
    val minimal = ticket(NonEmptyChain.one(ProcessType.Product))
    assert(minimal.validate.isValid)

  test("Example 3.6: combined processes with CombinedProcessIndex are valid"):
    val cutting = NodeInfo(start = Some(Timestamp.ofEpochSecond(100)))
    val folding = NodeInfo(start = Some(Timestamp.ofEpochSecond(200)))
    val rsCutting = ResourceSet(
      name = ResourceSetName.unsafe("NodeInfo"),
      usage = Some(Usage.Input),
      combinedProcessIndex = Some(NonEmptyChain.one(ProcessIndex.unsafe(0))),
      resources = Chain.one(Resource(ResourcePayload.NodeInfoResource(cutting)))
    )
    val rsFolding = rsCutting.copy(
      combinedProcessIndex = Some(NonEmptyChain.one(ProcessIndex.unsafe(1))),
      resources = Chain.one(Resource(ResourcePayload.NodeInfoResource(folding)))
    )
    val rsCuttingParams = ResourceSet(ResourceSetName.unsafe("CuttingParams"), usage = Some(Usage.Input))
    val rsFoldingParams = ResourceSet(ResourceSetName.unsafe("FoldingParams"), usage = Some(Usage.Input))
    val combined = ticket(
      NonEmptyChain.of(ProcessType.Cutting, ProcessType.Folding),
      Chain(rsCutting, rsFolding, rsCuttingParams, rsFoldingParams)
    )
    assert(combined.validate.isValid)

  test("Example 5.2: split delivery validates"):
    val contact1 = Resource(
      specific = ResourcePayload.ContactResource(
        Contact(address = Some(Address(city = Some(XjdfString.unsafe("city1")))))
      ),
      parts = Chain.one(PartBuilder.empty.withToken(
        PartitionKey.ContactType,
        Catalog.ContactType.Delivery
      ).withToken(PartitionKey.DropID, NmToken.unsafe("Drop1")).build)
    )
    val contact2 = Resource(
      specific = ResourcePayload.ContactResource(
        Contact(address = Some(Address(city = Some(XjdfString.unsafe("city2")))))
      ),
      parts = Chain.one(PartBuilder.empty.withToken(
        PartitionKey.ContactType,
        Catalog.ContactType.Delivery
      ).withToken(PartitionKey.DropID, NmToken.unsafe("Drop2")).build)
    )
    val drop1 = Resource(
      specific = ResourcePayload.DeliveryParamsResource(
        DeliveryParams(dropItems = Chain.one(DropItem(10, IdRef.unsafe("IDBook"))))
      ),
      parts = Chain.one(Part.token(PartitionKey.DropID, NmToken.unsafe("Drop1")))
    )
    val drop2 = Resource(
      specific = ResourcePayload.DeliveryParamsResource(
        DeliveryParams(dropItems = Chain.one(DropItem(20, IdRef.unsafe("IDBook"))))
      ),
      parts = Chain.one(Part.token(PartitionKey.DropID, NmToken.unsafe("Drop2")))
    )
    val book = Product(amount = Some(30), id = Some(Id.unsafe("IDBook")), productType = Some(Catalog.ProductType.Book))
    val delivery = ticket(
      NonEmptyChain.one(ProcessType.Product),
      Chain(
        ResourceSet(
          ResourceSetName.unsafe("Contact"),
          usage = Some(Usage.Input),
          resources = Chain(contact1, contact2)
        ),
        ResourceSet(
          ResourceSetName.unsafe("DeliveryParams"),
          usage = Some(Usage.Input),
          resources = Chain(drop1, drop2)
        )
      ),
      productList = Some(ProductList(NonEmptyChain.one(book)))
    )
    assert(delivery.validate.isValid)

  test("§3.1.3: \"Product\" SHALL NOT be combined with process tokens"):
    val invalid = ticket(NonEmptyChain.of(ProcessType.Product, ProcessType.Cutting))
    assert(invalid.validate.isInvalid)

  test("§3.4: duplicate ResourceSet keys are rejected"):
    val a = ResourceSet(ResourceSetName.unsafe("Media"), usage = Some(Usage.Input))
    val invalid = ticket(NonEmptyChain.one(ProcessType.Cutting), Chain(a, a))
    assert(invalid.validate.isInvalid)

  test("§3.4: CombinedProcessIndex out of bounds is rejected"):
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("NodeInfo"),
      usage = Some(Usage.Input),
      combinedProcessIndex = Some(NonEmptyChain.one(ProcessIndex.unsafe(7))),
      resources = Chain.one(Resource(ResourcePayload.NodeInfoResource(NodeInfo())))
    )
    val invalid = ticket(NonEmptyChain.one(ProcessType.Cutting), Chain(rs))
    assert(invalid.validate.isInvalid)

  test("§2.2.3: duplicate @ID values are rejected"):
    val id = Id.unsafe("dup")
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("Component"),
      resources = Chain(
        Resource(ResourcePayload.ComponentResource(Component()), id = Some(id)),
        Resource(ResourcePayload.ComponentResource(Component()), id = Some(id))
      )
    )
    val invalid = ticket(NonEmptyChain.one(ProcessType.Cutting), Chain(rs))
    assert(invalid.validate.isInvalid)

  test("§2.2.3: dangling IDREFs are rejected"):
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("Component"),
      resources = Chain.one(
        Resource(ResourcePayload.ComponentResource(Component(mediaRef = Some(IdRef.unsafe("missing")))))
      )
    )
    val invalid = ticket(NonEmptyChain.one(ProcessType.Cutting), Chain(rs))
    assert(invalid.validate.isInvalid)

  test("§3.2: a non-chronological AuditPool is rejected"):
    val later = Audit.Created(Header(NmToken.unsafe("Dev"), Timestamp.ofEpochSecond(100)))
    val earlier = Audit.Status(Header(NmToken.unsafe("Dev"), Timestamp.ofEpochSecond(1)), DeviceInfo(DeviceStatus.Idle))
    val invalid = ticket(
      NonEmptyChain.one(ProcessType.Cutting),
      auditPool = Some(AuditPool.of(later, earlier))
    )
    assert(invalid.validate.isInvalid)

  test("§3.2: a chronological AuditPool is accepted"):
    val first = Audit.Created(Header(NmToken.unsafe("Dev"), Timestamp.ofEpochSecond(1)))
    val last = Audit.Status(Header(NmToken.unsafe("Dev"), Timestamp.ofEpochSecond(100)), DeviceInfo(DeviceStatus.Idle))
    val valid = ticket(
      NonEmptyChain.one(ProcessType.Cutting),
      auditPool = Some(AuditPool.of(first, last))
    )
    assert(valid.validate.isValid)

  test("Table 3.1: @RelatedJobPartID requires @RelatedJobID"):
    val invalid = ticket(NonEmptyChain.one(ProcessType.Product)).copy(relatedJobPartId = Some(JobPartId.unsafe("P1")))
    assert(invalid.validate.isInvalid)

  test("a change order is an XJDF refined by Partial (intersection type)"):
    val changeOrder: ChangeOrder = ticket(NonEmptyChain.one(ProcessType.Product))
    val asXjdf: XJDF = changeOrder // the refinement is erased at the value level
    assert(asXjdf.validate.isValid)

  test("§6.1.2.1: validator messages use PartitionKey.attributeName (@Option, not @OptionKey)"):
    val parent = PartBuilder.empty.withToken(PartitionKey.OptionKey, NmToken.unsafe("a")).build
    val child = PartBuilder.empty.withToken(PartitionKey.OptionKey, NmToken.unsafe("b")).build
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("Media"),
      resources = Chain.one(
        Resource(
          specific = ResourcePayload.Foreign(NsPrefix.unsafe("foo"), NmToken.unsafe("Bar")),
          parts = Chain.one(parent),
          amountPool = Some(AmountPool.of(PartAmount(part = child)))
        )
      )
    )
    val t = ticket(NonEmptyChain.one(ProcessType.Cutting), resourceSets = Chain.one(rs))
    val messages = t.validate.toEither match
      case Left(issues) => issues.toChain.toList.map(_.message)
      case Right(_)     => fail("expected the shadowing PartAmount to be rejected")
    assert(messages.exists(_.contains("@Option")), messages.mkString("; "))
    assert(!messages.exists(_.contains("@OptionKey")), messages.mkString("; "))

  test("README example compiles and validates"):
    val ticket: ValidatedNec[Issue, XJDF] =
      dsl.TicketDraft.of("J1", ProcessType.Product).andThen(_.build)
    assert(ticket.isValid)
end TicketLaws
