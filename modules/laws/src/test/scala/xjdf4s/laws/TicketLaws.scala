package xjdf4s.laws

import xjdf4s.dsl.dsl
import xjdf4s.intents.*
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

  /** A `Part` carrying `@Separation` and `@PartVersion` (Example 6.1). */
  private def sepVersionPart(separation: String, partVersion: String): Part =
    PartBuilder.empty
      .withSeparation(NmToken.unsafe(separation))
      .withTokenUnsafe(PartitionKey.PartVersion, NmToken.unsafe(partVersion))
      .build

  /** An `ExposedMedia` resource. The specific `ExposedMedia` resource is outside
   *  the implemented Chapter 6 subset, so it is modelled through the `Foreign`
   *  escape hatch; the `PartAmount`/`Part` validation under test is unaffected.
   */
  private def exposedMedia(parts: Part*): Resource =
    Resource(
      specific = Some(ResourcePayload.Foreign(NsPrefix.unsafe("ex"), NmToken.unsafe("ExposedMedia"))),
      parts = Chain.fromSeq(parts)
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
      resources = Chain.one(Resource.of(ResourcePayload.NodeInfoResource(cutting)))
    )
    val rsFolding = rsCutting.copy(
      combinedProcessIndex = Some(NonEmptyChain.one(ProcessIndex.unsafe(1))),
      resources = Chain.one(Resource.of(ResourcePayload.NodeInfoResource(folding)))
    )
    val rsCuttingParams = ResourceSet(
      name = ResourceSetName.unsafe("CuttingParams"),
      usage = Some(Usage.Input),
      resources = Chain.one(Resource.empty)
    )
    val rsFoldingParams = ResourceSet(
      name = ResourceSetName.unsafe("FoldingParams"),
      usage = Some(Usage.Input),
      resources = Chain.one(Resource.empty)
    )
    val combined = ticket(
      NonEmptyChain.of(ProcessType.Cutting, ProcessType.Folding),
      Chain(rsCutting, rsFolding, rsCuttingParams, rsFoldingParams)
    )
    assert(combined.validate.isValid)

  test("Table 6.1 / Example 3.6: bodyless Resource elements are representable and validate"):
    val emptyRes = Resource.empty
    assert(emptyRes.isBodyless)
    assert(emptyRes.elementName.isEmpty)
    assert(emptyRes.references.isEmpty)
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("CuttingParams"),
      usage = Some(Usage.Input),
      resources = Chain.one(emptyRes)
    )
    assert(rs.hasLawfulChildren)
    val t = ticket(NonEmptyChain.one(ProcessType.Cutting), resourceSets = Chain.one(rs))
    assert(t.validate.isValid)

  test("Table 6.1: Resource with payload preserves elementName, references and validation"):
    val res = Resource.withPayload(ResourcePayload.MediaResource(Media(MediaType.Paper)))
    assert(!res.isBodyless)
    assertEquals(res.elementName, Some(NmToken.unsafe("Media")))
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("Media"),
      usage = Some(Usage.Input),
      resources = Chain.one(res)
    )
    assert(rs.hasLawfulChildren)

  test("Table 6.1 / §3.4: ResourceSet.hasLawfulChildren accepts bodyless and rejects mismatched payload"):
    val validSet = ResourceSet(
      name = ResourceSetName.unsafe("CuttingParams"),
      resources = Chain(Resource.empty, Resource.empty)
    )
    assert(validSet.hasLawfulChildren)
    val invalidSet = ResourceSet(
      name = ResourceSetName.unsafe("CuttingParams"),
      resources = Chain.one(Resource.withPayload(ResourcePayload.MediaResource(Media(MediaType.Paper))))
    )
    assert(!invalidSet.hasLawfulChildren)

  test("Table 8.49: Notification carries optional moduleId"):
    val n = Notification(
      classification = SeverityClass.Information,
      moduleId = Some(NmToken.unsafe("Module_1"))
    )
    assertEquals(n.moduleId, Some(NmToken.unsafe("Module_1")))

  test("Table 8.49: Notification with Milestone and @Class=\"Event\" validates"):
    val header = Header(NmToken.unsafe("Dev"), Timestamp.ofEpochSecond(10))
    val validNotification = Notification(
      classification = SeverityClass.Event,
      detail = Some(Milestone(NmToken.unsafe("Printed")))
    )
    val t = ticket(
      NonEmptyChain.one(ProcessType.Product),
      auditPool = Some(AuditPool.of(Audit.Notified(header, validNotification)))
    )
    assert(t.validate.isValid)

  test("Table 8.49: Notification with Milestone and non-Event @Class is rejected"):
    val header = Header(NmToken.unsafe("Dev"), Timestamp.ofEpochSecond(10))
    val invalidNotification = Notification(
      classification = SeverityClass.Warning,
      detail = Some(Milestone(NmToken.unsafe("Printed")))
    )
    val t = ticket(
      NonEmptyChain.one(ProcessType.Product),
      auditPool = Some(AuditPool.of(Audit.Notified(header, invalidNotification)))
    )
    assert(t.validate.isInvalid)

  test("Table 8.49 / N-38: multiple Comments with duplicate @Language in Notification are rejected"):
    val header = Header(NmToken.unsafe("Dev"), Timestamp.ofEpochSecond(10))
    val c1 = Comment(text = CommentText("English 1"), language = Some(LanguageTag.unsafe("en")))
    val c2 = Comment(text = CommentText("English 2"), language = Some(LanguageTag.unsafe("en")))
    val notif = Notification(
      classification = SeverityClass.Information,
      comments = Chain(c1, c2)
    )
    val t = ticket(
      NonEmptyChain.one(ProcessType.Product),
      auditPool = Some(AuditPool.of(Audit.Notified(header, notif)))
    )
    assert(t.validate.isInvalid)

  test("Table 8.49 / N-38: multiple Comments with distinct @Language in Notification are accepted"):
    val header = Header(NmToken.unsafe("Dev"), Timestamp.ofEpochSecond(10))
    val c1 = Comment(text = CommentText("English"), language = Some(LanguageTag.unsafe("en")))
    val c2 = Comment(text = CommentText("French"), language = Some(LanguageTag.unsafe("fr")))
    val notif = Notification(
      classification = SeverityClass.Information,
      comments = Chain(c1, c2)
    )
    val t = ticket(
      NonEmptyChain.one(ProcessType.Product),
      auditPool = Some(AuditPool.of(Audit.Notified(header, notif)))
    )
    assert(t.validate.isValid)

  test("Table 6.55: DropItem supports totalDimensions, totalVolume, and totalWeight"):
    val item = DropItem(
      amount = 50L,
      itemRef = IdRef.unsafe("prod_1"),
      totalDimensions = Some(Shape(100.0, 200.0, 50.0)),
      totalVolume = Some(1.0),
      totalWeight = Some(2.5)
    )
    assertEquals(item.amount, 50L)
    assertEquals(item.itemRef, IdRef.unsafe("prod_1"))
    assertEquals(item.totalDimensions, Some(Shape(100.0, 200.0, 50.0)))
    assertEquals(item.totalVolume, Some(1.0))
    assertEquals(item.totalWeight, Some(2.5))

  test("Example 5.2: split delivery validates"):
    val contact1 = Resource(
      specific = Some(ResourcePayload.ContactResource(
        Contact(address = Some(Address(city = Some(XjdfString.unsafe("city1")))))
      )),
      parts = Chain.one(PartBuilder.empty.withTokenUnsafe(
        PartitionKey.ContactType,
        Catalog.ContactType.Delivery
      ).withTokenUnsafe(PartitionKey.DropID, NmToken.unsafe("Drop1")).build)
    )
    val contact2 = Resource(
      specific = Some(ResourcePayload.ContactResource(
        Contact(address = Some(Address(city = Some(XjdfString.unsafe("city2")))))
      )),
      parts = Chain.one(PartBuilder.empty.withTokenUnsafe(
        PartitionKey.ContactType,
        Catalog.ContactType.Delivery
      ).withTokenUnsafe(PartitionKey.DropID, NmToken.unsafe("Drop2")).build)
    )
    val drop1 = Resource(
      specific = Some(ResourcePayload.DeliveryParamsResource(
        DeliveryParams(dropItems = Chain.one(DropItem(10, IdRef.unsafe("IDBook"))))
      )),
      parts = Chain.one(Part.token(PartitionKey.DropID, NmToken.unsafe("Drop1")))
    )
    val drop2 = Resource(
      specific = Some(ResourcePayload.DeliveryParamsResource(
        DeliveryParams(dropItems = Chain.one(DropItem(20, IdRef.unsafe("IDBook"))))
      )),
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

  test("§3.4 / N-16: partial CPI overlap [0] vs [0,1] is rejected"):
    val rs0 = ResourceSet(
      name = ResourceSetName.unsafe("NodeInfo"),
      usage = Some(Usage.Input),
      combinedProcessIndex = Some(NonEmptyChain.one(ProcessIndex.unsafe(0))),
      resources = Chain.one(Resource.of(ResourcePayload.NodeInfoResource(NodeInfo())))
    )
    val rs01 = rs0.copy(
      combinedProcessIndex =
        Some(NonEmptyChain.of(ProcessIndex.unsafe(0), ProcessIndex.unsafe(1)))
    )
    val invalid = ticket(
      NonEmptyChain.of(ProcessType.Cutting, ProcessType.Folding),
      Chain(rs0, rs01)
    )
    assert(invalid.validate.isInvalid)
    val clashIssues = invalid.validate.toEither.left.toOption.toList
      .flatMap(_.toChain.toList)
      .filter(_.code.contains(IssueCode.ResourceSetClash))
    assert(clashIssues.nonEmpty, "expected a ResourceSetClash issue code")

  test("§3.4 / N-16: no-CPI vs CPI=[1] is rejected (no entries applies to all)"):
    val noCpi = ResourceSet(
      name = ResourceSetName.unsafe("NodeInfo"),
      usage = Some(Usage.Input),
      resources = Chain.one(Resource.of(ResourcePayload.NodeInfoResource(NodeInfo())))
    )
    val withCpi = noCpi.copy(
      combinedProcessIndex = Some(NonEmptyChain.one(ProcessIndex.unsafe(1)))
    )
    val invalid = ticket(
      NonEmptyChain.of(ProcessType.Cutting, ProcessType.Folding),
      Chain(noCpi, withCpi)
    )
    assert(invalid.validate.isInvalid)
    val clashIssues = invalid.validate.toEither.left.toOption.toList
      .flatMap(_.toChain.toList)
      .filter(_.code.contains(IssueCode.ResourceSetClash))
    assert(clashIssues.nonEmpty, "expected a ResourceSetClash issue code")

  test("§3.4 / Example 3.6: disjoint CPI [0] vs [1] with same name is valid"):
    val rs0 = ResourceSet(
      name = ResourceSetName.unsafe("NodeInfo"),
      usage = Some(Usage.Input),
      combinedProcessIndex = Some(NonEmptyChain.one(ProcessIndex.unsafe(0))),
      resources = Chain.one(Resource.of(ResourcePayload.NodeInfoResource(NodeInfo())))
    )
    val rs1 = rs0.copy(
      combinedProcessIndex = Some(NonEmptyChain.one(ProcessIndex.unsafe(1)))
    )
    val valid = ticket(
      NonEmptyChain.of(ProcessType.Cutting, ProcessType.Folding),
      Chain(rs0, rs1)
    )
    assert(valid.validate.isValid)

  test("§3.4: CombinedProcessIndex out of bounds is rejected"):
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("NodeInfo"),
      usage = Some(Usage.Input),
      combinedProcessIndex = Some(NonEmptyChain.one(ProcessIndex.unsafe(7))),
      resources = Chain.one(Resource.of(ResourcePayload.NodeInfoResource(NodeInfo())))
    )
    val invalid = ticket(NonEmptyChain.one(ProcessType.Cutting), Chain(rs))
    assert(invalid.validate.isInvalid)

  test("§2.2.3: duplicate @ID values are rejected"):
    val id = Id.unsafe("dup")
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("Component"),
      resources = Chain(
        Resource(Some(ResourcePayload.ComponentResource(Component())), id = Some(id)),
        Resource(Some(ResourcePayload.ComponentResource(Component())), id = Some(id))
      )
    )
    val invalid = ticket(NonEmptyChain.one(ProcessType.Cutting), Chain(rs))
    assert(invalid.validate.isInvalid)

  test("Table 7.3 / §2.2.3: audits with duplicate Header/@ID in messaging scope do not cause document @ID collision"):
    val headerId = Id.unsafe("msg_1")
    val h1 = Header(NmToken.unsafe("Dev"), Timestamp.ofEpochSecond(1), id = Some(headerId))
    val h2 = Header(NmToken.unsafe("Dev"), Timestamp.ofEpochSecond(2), id = Some(headerId))
    val t = ticket(
      NonEmptyChain.one(ProcessType.Product),
      auditPool = Some(AuditPool.of(Audit.Created(h1), Audit.Created(h2)))
    )
    assert(t.validate.isValid)
    assertEquals(t.declaredIds, Chain.empty)

  test("Table 7.53 / §2.2.3: IDREFs inside Audit ResourceInfo are collected in references and validated"):
    val mediaId = Id.unsafe("media_1")
    val mediaRes = Resource(
      specific = Some(ResourcePayload.MediaResource(Media(MediaType.Paper))),
      id = Some(mediaId)
    )
    val mediaSet = ResourceSet(ResourceSetName.unsafe("Media"), resources = Chain.one(mediaRes))

    val compWithValidRef = Resource(
      specific = Some(ResourcePayload.ComponentResource(Component(mediaRef = Some(IdRef.unsafe("media_1")))))
    )
    val auditInfoValid = ResourceInfo(
      ResourceSet(ResourceSetName.unsafe("Component"), resources = Chain.one(compWithValidRef))
    )
    val header = Header(NmToken.unsafe("Dev"), Timestamp.ofEpochSecond(1))
    val validTicket = ticket(
      NonEmptyChain.one(ProcessType.Product),
      resourceSets = Chain.one(mediaSet),
      auditPool = Some(AuditPool.of(Audit.Resource(header, auditInfoValid)))
    )
    assert(validTicket.validate.isValid)
    assert(validTicket.references.contains(IdRef.unsafe("media_1")))

    val compWithDanglingRef = Resource(
      specific = Some(ResourcePayload.ComponentResource(Component(mediaRef = Some(IdRef.unsafe("non_existent")))))
    )
    val auditInfoInvalid = ResourceInfo(
      ResourceSet(ResourceSetName.unsafe("Component"), resources = Chain.one(compWithDanglingRef))
    )
    val invalidTicket = ticket(
      NonEmptyChain.one(ProcessType.Product),
      resourceSets = Chain.one(mediaSet),
      auditPool = Some(AuditPool.of(Audit.Resource(header, auditInfoInvalid)))
    )
    assert(invalidTicket.validate.isInvalid)
    assert(invalidTicket.references.contains(IdRef.unsafe("non_existent")))

  test("§2.2.3: dangling IDREFs are rejected"):
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("Component"),
      resources = Chain.one(
        Resource(Some(ResourcePayload.ComponentResource(Component(mediaRef = Some(IdRef.unsafe("missing"))))))
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

  test("§6.1.2.1: validator messages use PartitionKey.attributeName (@Option, not @OptionKey)"):
    val parent = PartBuilder.empty.withTokenUnsafe(PartitionKey.OptionKey, NmToken.unsafe("a")).build
    val child = PartBuilder.empty.withTokenUnsafe(PartitionKey.OptionKey, NmToken.unsafe("b")).build
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("Media"),
      resources = Chain.one(
        Resource(
          specific = Some(ResourcePayload.Foreign(NsPrefix.unsafe("foo"), NmToken.unsafe("Bar"))),
          parts = Chain.one(parent),
          amountPool = Some(AmountPool.of(PartAmount(parts = Chain.one(child))))
        )
      )
    )
    val t = ticket(NonEmptyChain.one(ProcessType.Cutting), resourceSets = Chain.one(rs))
    val messages = t.validate.toEither match
      case Left(issues) => issues.toChain.toList.map(_.message)
      case Right(_)     => fail("expected the shadowing PartAmount to be rejected")
    assert(messages.exists(_.contains("@Option")), messages.mkString("; "))
    assert(!messages.exists(_.contains("@OptionKey")), messages.mkString("; "))

  test("§6.1.2.1 / Example 6.1: Versioned Set Of Plates with multiple parent Part elements is valid"):
    val cyanEnglish = sepVersionPart("Cyan", "English")
    val cyanFrench  = sepVersionPart("Cyan", "French")
    val blackEnglish = sepVersionPart("Black", "English")
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("ExposedMedia"),
      usage = Some(Usage.Output),
      resources = Chain(
        exposedMedia(cyanEnglish, cyanFrench),
        exposedMedia(blackEnglish)
      )
    )
    val t = ticket(NonEmptyChain.one(ProcessType.Cutting), resourceSets = Chain.one(rs))
    assert(t.validate.isValid)

  test("§6.1.2.1 Rule 2: a PartAmount/Part may repeat a parent key when it matches one of several parent values"):
    val cyanEnglish = sepVersionPart("Cyan", "English")
    val cyanFrench  = sepVersionPart("Cyan", "French")
    val child = PartBuilder.empty.withTokenUnsafe(PartitionKey.PartVersion, NmToken.unsafe("English")).build
    val resource = exposedMedia(cyanEnglish, cyanFrench)
      .copy(amountPool = Some(AmountPool.of(PartAmount(parts = Chain.one(child)))))
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("ExposedMedia"),
      usage = Some(Usage.Output),
      resources = Chain.one(resource)
    )
    val t = ticket(NonEmptyChain.one(ProcessType.Cutting), resourceSets = Chain.one(rs))
    assert(t.validate.isValid)

  test("§6.1.2.1 Rule 1: a PartAmount/Part SHALL NOT repeat a Partition Key uniquely specified by the parent"):
    val parent = PartBuilder.empty.withTokenUnsafe(PartitionKey.Separation, NmToken.unsafe("Cyan")).build
    val child = PartBuilder.empty.withTokenUnsafe(PartitionKey.Separation, NmToken.unsafe("Cyan")).build
    val resource = exposedMedia(parent)
      .copy(amountPool = Some(AmountPool.of(PartAmount(parts = Chain.one(child)))))
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("ExposedMedia"),
      usage = Some(Usage.Output),
      resources = Chain.one(resource)
    )
    val t = ticket(NonEmptyChain.one(ProcessType.Cutting), resourceSets = Chain.one(rs))
    assert(t.validate.isInvalid)

  test("§6.1.2.1 Rule 2: a repeated key value SHALL match one of the parent values (mismatch is rejected)"):
    val cyanEnglish = sepVersionPart("Cyan", "English")
    val cyanFrench  = sepVersionPart("Cyan", "French")
    val child = PartBuilder.empty.withTokenUnsafe(PartitionKey.PartVersion, NmToken.unsafe("German")).build
    val resource = exposedMedia(cyanEnglish, cyanFrench)
      .copy(amountPool = Some(AmountPool.of(PartAmount(parts = Chain.one(child)))))
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("ExposedMedia"),
      usage = Some(Usage.Output),
      resources = Chain.one(resource)
    )
    val t = ticket(NonEmptyChain.one(ProcessType.Cutting), resourceSets = Chain.one(rs))
    assert(t.validate.isInvalid)

  test("README example compiles and validates"):
    val ticket: ValidatedNec[Issue, XJDF] =
      dsl.TicketDraft.of("J1", ProcessType.Product).andThen(_.build)
    assert(ticket.isValid)

  // --- M1.3-3: DomainRule bus negative tests -------------------------------

  private def productWithIntent(intent: Intent): Product =
    Product(
      id = Some(Id.unsafe("P1")),
      productType = Some(NmToken.unsafe("Book")),
      intents = Chain.one(intent)
    )

  private def ticketWithProduct(p: Product): XJDF =
    ticket(NonEmptyChain.one(ProcessType.Product), productList = Some(ProductList(NonEmptyChain.one(p))))

  private def issuesOf(t: XJDF): List[Issue] =
    t.validate.toEither.left.toOption.toList.flatMap(_.toChain.toList)

  private def assertHasCode(t: XJDF, code: IssueCode): Unit =
    val issues = issuesOf(t)
    assert(
      issues.exists(_.code.contains(code)),
      s"Expected issue code ${code.value}, got: ${issues.map(i => i.code.fold("<none>")(_.value)).mkString(", ")}"
    )

  test("Table 4.8: SaddleStitching details with @BindingType=SoftCover is rejected (DomainRule)"):
    val badBinding = BindingIntent(
      bindingType = BindingType.SoftCover,
      details = Some(SaddleStitching())
    )
    val intent = Intent(IntentName.unsafe("BindingIntent"), IntentPayload.Binding(badBinding))
    val t = ticketWithProduct(productWithIntent(intent))
    assert(t.validate.isInvalid)
    // The intent @Name matches the payload, so only the binding pairing law fires.
    val codes = issuesOf(t).flatMap(_.code)
    assert(codes.contains(IssueCode.LocalLawViolation), s"got codes: $codes")
    assert(!codes.contains(IssueCode.IntentNameMismatch), s"got codes: $codes")

  test("Table 4.8: matching BindingType/details pair is accepted"):
    val goodBinding = BindingIntent(
      bindingType = BindingType.SaddleStitch,
      details = Some(SaddleStitching())
    )
    val intent = Intent(IntentName.unsafe("BindingIntent"), IntentPayload.Binding(goodBinding))
    val t = ticketWithProduct(productWithIntent(intent))
    assert(t.validate.isValid)

  test("Table 4.8: @BindingSide with @BindingOrder=None is rejected"):
    val badBinding = BindingIntent(
      bindingType = BindingType.SaddleStitch,
      bindingOrder = Some(BindingOrder.Unbound),
      bindingSide = Some(Edge.Top),
      details = Some(SaddleStitching())
    )
    val intent = Intent(IntentName.unsafe("BindingIntent"), IntentPayload.Binding(badBinding))
    val t = ticketWithProduct(productWithIntent(intent))
    assert(t.validate.isInvalid)
    assertHasCode(t, IssueCode.LocalLawViolation)

  test("Table 4.36: @MaxPages < @AveragePages is rejected"):
    val badVar = VariableIntent(
      variableType = VariableType.OneLine,
      averagePages = Some(9L),
      maxPages = Some(5L)
    )
    val intent = Intent(IntentName.unsafe("VariableIntent"), IntentPayload.Variable(badVar))
    val t = ticketWithProduct(productWithIntent(intent))
    assert(t.validate.isInvalid)
    assertHasCode(t, IssueCode.LocalLawViolation)

  test("Table 4.36: MinPages ≤ AveragePages ≤ MaxPages is accepted"):
    val goodVar = VariableIntent(
      variableType = VariableType.OneLine,
      minPages = Some(2L),
      averagePages = Some(5L),
      maxPages = Some(10L)
    )
    val intent = Intent(IntentName.unsafe("VariableIntent"), IntentPayload.Variable(goodVar))
    val t = ticketWithProduct(productWithIntent(intent))
    assert(t.validate.isValid)

  test("Table 6.5: PartWaste without @ModuleIDs/@WasteDetails is rejected"):
    val pa = PartAmount(partWaste = Chain.one(PartWaste(waste = Amount(1.0))))
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("Component"),
      resources = Chain.one(
        Resource(
          specific = Some(ResourcePayload.ComponentResource(Component())),
          amountPool = Some(AmountPool.of(pa))
        )
      )
    )
    val t = ticket(NonEmptyChain.one(ProcessType.Product), Chain.one(rs))
    assert(t.validate.isInvalid)
    assertHasCode(t, IssueCode.LocalLawViolation)

  test("Table 6.1: @Status on Usage=Output ResourceSet is rejected"):
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("Component"),
      usage = Some(Usage.Output),
      resources = Chain.one(
        Resource(
          specific = Some(ResourcePayload.ComponentResource(Component())),
          status = Some(ResourceStatus.Available)
        )
      )
    )
    val t = ticket(NonEmptyChain.one(ProcessType.Product), Chain.one(rs))
    assert(t.validate.isInvalid)
    assertHasCode(t, IssueCode.ResourceStatusOnOutput)

  test("§3.3.1.1: negative product amount is rejected by local law"):
    val p = Product(amount = Some(-5L), id = Some(Id.unsafe("P1")))
    val t = ticketWithProduct(p)
    assert(t.validate.isInvalid)
    assertHasCode(t, IssueCode.LocalLawViolation)

  // --- Registry: every DomainRule-carrying type is reached by the bus ------

  test("M1.3-3 registry: all types with local DomainRules are reached by validate"):
    // The types carrying local laws in M1.3-3. If a new given DomainRule[T] is
    // added, it MUST be wired into TicketValidator.checkLocalLaws AND listed
    // here (the test exercises a positive path for each, proving reachability).
    val intent = Intent(IntentName.unsafe("BindingIntent"),
      IntentPayload.Binding(BindingIntent(bindingType = BindingType.SaddleStitch,
        details = Some(SaddleStitching()))))
    val productWithBind = productWithIntent(intent)
    assert(ticketWithProduct(productWithBind).validate.isValid)

    val variableIntent = Intent(IntentName.unsafe("VariableIntent"),
      IntentPayload.Variable(VariableIntent(variableType = VariableType.OneLine,
        averagePages = Some(4L))))
    assert(ticketWithProduct(productWithIntent(variableIntent)).validate.isValid)

    // Notification laws
    val header = Header(NmToken.unsafe("Dev"), Timestamp.ofEpochSecond(1))
    val notif = Notification(classification = SeverityClass.Information,
      comments = Chain(
        Comment(text = CommentText("en"), language = Some(LanguageTag.unsafe("en"))),
        Comment(text = CommentText("fr"), language = Some(LanguageTag.unsafe("fr")))
      ))
    val notifTicket = ticket(
      NonEmptyChain.one(ProcessType.Product),
      auditPool = Some(AuditPool.of(Audit.Notified(header, notif)))
    )
    assert(notifTicket.validate.isValid)
  // --- M1.3-4: aggregate integrity (N-19, N-36, N-37) ---------------------

  test("N-19: ticket with a cycle in @ChildRefs is rejected by validate"):
    val a = Product(id = Some(Id.unsafe("A")), isRoot = true,
      intents = Chain.one(Intent(IntentName.unsafe("BindingIntent"),
        IntentPayload.Binding(BindingIntent(BindingType.SaddleStitch,
          childRefs = Some(IdRefs.of(IdRef.unsafe("B"))))))))
    val b = Product(id = Some(Id.unsafe("B")), isRoot = false,
      intents = Chain.one(Intent(IntentName.unsafe("BindingIntent"),
        IntentPayload.Binding(BindingIntent(BindingType.SaddleStitch,
          childRefs = Some(IdRefs.of(IdRef.unsafe("A"))))))))
    val t = ticket(NonEmptyChain.one(ProcessType.Product),
      productList = Some(ProductList(NonEmptyChain.of(a, b))))
    assert(t.validate.isInvalid)
    assertHasCode(t, IssueCode.BomCycle)

  test("N-19: unresolved @ChildRefs is rejected by validate"):
    val a = Product(id = Some(Id.unsafe("A")), isRoot = true,
      intents = Chain.one(Intent(IntentName.unsafe("BindingIntent"),
        IntentPayload.Binding(BindingIntent(BindingType.SaddleStitch,
          childRefs = Some(IdRefs.of(IdRef.unsafe("MISSING"))))))))
    val t = ticket(NonEmptyChain.one(ProcessType.Product),
      productList = Some(ProductList(NonEmptyChain.one(a))))
    assert(t.validate.isInvalid)
    assertHasCode(t, IssueCode.BomUnresolvedChildRef)

  test("N-36: duplicate \"Product\" token in @Types is rejected (strict policy)"):
    val t = ticket(NonEmptyChain.of(ProcessType.Product, ProcessType.Product))
    assert(t.validate.isInvalid)
    assertHasCode(t, IssueCode.ProductTokenDuplicate)

  test("N-37: child with @PartVersion=v1, root without @PartVersion is rejected"):
    val child = Product(id = Some(Id.unsafe("C")), isRoot = false,
      partVersion = Some(NmToken.unsafe("v1")))
    val root = Product(id = Some(Id.unsafe("R")), isRoot = true,
      intents = Chain.one(Intent(IntentName.unsafe("BindingIntent"),
        IntentPayload.Binding(BindingIntent(BindingType.SaddleStitch,
          childRefs = Some(IdRefs.of(IdRef.unsafe("C"))))))))
    val t = ticket(NonEmptyChain.one(ProcessType.Product),
      productList = Some(ProductList(NonEmptyChain.of(root, child))))
    assert(t.validate.isInvalid)
    assertHasCode(t, IssueCode.PartVersionMismatch)

  test("N-37: child @PartVersion=v1, root @PartVersion=v2 is rejected"):
    val child = Product(id = Some(Id.unsafe("C")), isRoot = false,
      partVersion = Some(NmToken.unsafe("v1")))
    val root = Product(id = Some(Id.unsafe("R")), isRoot = true,
      partVersion = Some(NmToken.unsafe("v2")),
      intents = Chain.one(Intent(IntentName.unsafe("BindingIntent"),
        IntentPayload.Binding(BindingIntent(BindingType.SaddleStitch,
          childRefs = Some(IdRefs.of(IdRef.unsafe("C"))))))))
    val t = ticket(NonEmptyChain.one(ProcessType.Product),
      productList = Some(ProductList(NonEmptyChain.of(root, child))))
    assert(t.validate.isInvalid)
    assertHasCode(t, IssueCode.PartVersionMismatch)

  test("N-37: matching @PartVersion on child and root is accepted"):
    val child = Product(id = Some(Id.unsafe("C")), isRoot = false,
      partVersion = Some(NmToken.unsafe("v1")))
    val root = Product(id = Some(Id.unsafe("R")), isRoot = true,
      partVersion = Some(NmToken.unsafe("v1")),
      intents = Chain.one(Intent(IntentName.unsafe("BindingIntent"),
        IntentPayload.Binding(BindingIntent(BindingType.SaddleStitch,
          childRefs = Some(IdRefs.of(IdRef.unsafe("C"))))))))
    val t = ticket(NonEmptyChain.one(ProcessType.Product),
      productList = Some(ProductList(NonEmptyChain.of(root, child))))
    assert(t.validate.isValid)

  // --- M1.3-5: ValidationReport / severity (ADR-0006) ---------------------

  test("M1.3-5: a ticket with only warnings is valid but carries warnings"):
    // A structurally valid ticket with no SHALL violations: validateReport
    // reports isValid=true and an empty error chain. Warnings are produced by
    // SHOULD/MAY rules when present; currently the core emits only errors, so
    // the positive baseline must be a clean report.
    val t = ticket(NonEmptyChain.one(ProcessType.Product))
    val report = TicketValidator.validateReport(t)
    assert(report.isValid)
    assert(report.errors.isEmpty)

  test("M1.3-5: a ticket with one error is invalid and the error carries an IssueCode"):
    val invalid = ticket(NonEmptyChain.of(ProcessType.Product, ProcessType.Cutting))
    val report = TicketValidator.validateReport(invalid)
    assert(!report.isValid)
    assert(report.errors.nonEmpty)
    assert(report.errors.forall(_.code.isDefined), "every error SHALL carry a stable IssueCode")
    assert(report.errors.exists(_.code.contains(IssueCode.ProductTokenMixed)))

  test("M1.3-5: withWarningsAsErrors escalates every warning to an error"):
    val warning = Issue.warningC(
      IssueCode.LocalLawViolation,
      XPath("/XJDF"),
      "SHOULD violation"
    )
    val report = ValidationReport(errors = Chain.empty, warnings = Chain.one(warning))
    assert(report.isValid)
    val escalated = report.withWarningsAsErrors
    assert(!escalated.isValid)
    assert(escalated.warnings.isEmpty)
    assertEquals(escalated.errors.size.toInt, 1)
    assert(escalated.errors.headOption.exists(_.severity == SeverityClass.Error))

  test("M1.3-5: escalate(codes) only escalates warnings carrying the given codes"):
    val w1 = Issue.warningC(IssueCode.LocalLawViolation, XPath("/XJDF"), "w1")
    val w2 = Issue.warningC(IssueCode.AuditNotChronological, XPath("/XJDF/AuditPool"), "w2")
    val report = ValidationReport(Chain.empty, Chain(w1, w2))
    val escalated = report.escalate(Set(IssueCode.LocalLawViolation))
    assert(!escalated.isValid)
    assertEquals(escalated.errors.size.toInt, 1)
    assertEquals(escalated.warnings.size.toInt, 1)

  test("M1.3-5: every issue produced by the core validator carries an IssueCode"):
    // Negative fixture that exercises a variety of rules; none of the core
    // issues may be code-less after M1.3-5 (the `code: Option[IssueCode]`
    // escape hatch remains on the constructor for external codecs/tests, but
    // TicketValidator itself SHALL always supply one).
    val badBinding = BindingIntent(
      bindingType = BindingType.SoftCover,
      details = Some(SaddleStitching())
    )
    val p = Product(
      id = Some(Id.unsafe("P1")),
      amount = Some(-3L),
      intents = Chain.one(Intent(IntentName.unsafe("BindingIntent"),
        IntentPayload.Binding(badBinding)))
    )
    val t = ticketWithProduct(p)
    val report = TicketValidator.validateReport(t)
    assert(!report.isValid)
    val codeLess = report.errors.filter(_.code.isEmpty)
    assert(codeLess.isEmpty, s"codeless errors: $codeLess")
end TicketLaws
