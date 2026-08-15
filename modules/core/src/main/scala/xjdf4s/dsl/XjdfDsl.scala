package xjdf4s
package dsl

import xjdf4s.intents.*
import xjdf4s.model.*
import xjdf4s.prim.*
import xjdf4s.resources.*
import cats.Show
import cats.data.{Chain, NonEmptyChain, State, Validated, ValidatedNec}
import cats.syntax.all.*

/** The declarative DSL of xjdf4s: build tickets the way a Controller writes
 *  them — bottom-up from resources, products and audits — with every structural
 *  invariant checked and every violation accumulated.
 */
object dsl:

  // ------------------------------------------------------------------
  // Fresh IDs
  // ------------------------------------------------------------------

  /** Runs a pure fresh-ID program with empty per-prefix counters. */
  def inIds[A](allocator: IdAllocator[A]): A = IdSource.run(allocator)

  /** Allocates the next deterministic ID for `prefix`. */
  def freshId(prefix: String): IdAllocator[Id] = IdSource.freshId(prefix)

  /** Allocates deterministic IDs for `prefix` in document order. */
  def freshMany(prefix: String, n: Int): IdAllocator[Chain[Id]] = IdSource.freshMany(prefix, n)

  /** Builds a Product inside the pure ID-allocation DSL. An explicitly supplied
   *  ID is preserved; otherwise the program allocates a `Product_<n>` ID.
   */
  def productWithFreshId(
      amount: Option[Long] = None,
      isRoot: Boolean = true,
      productType: Option[NmToken] = None,
      id: Option[Id] = None,
      externalId: Option[NmToken] = None
  )(intents: Intent*): IdAllocator[Product] =
    id.fold(freshId("Product"))(State.pure).map { allocatedId =>
      Product(
        amount = amount,
        externalId = externalId,
        id = Some(allocatedId),
        isRoot = isRoot,
        productType = productType,
        intents = Chain.fromSeq(intents)
      )
    }

  // ------------------------------------------------------------------
  // Partitions
  // ------------------------------------------------------------------

  /** A single-key partition with a runtime-tagged value,
   *  e.g. `part(PartitionKey.SheetName, PartitionValue.Token("S1"))`.
   */
  def part(key: PartitionKey, value: PartitionValue): Part =
    Part.ofValue(key, value)

  /** A single-key NMTOKEN partition, e.g. `partToken(PartitionKey.SheetName, "S1")`. */
  def partToken(key: PartitionKey, value: String): ValidatedNec[Issue, Part] =
    NmToken
      .from(value)
      .toValidNec(Issue.error(XPath("/XJDF/ResourceSet/Resource/Part"), s"Invalid partition value: '$value'"))
      .map(Part.token(key, _))

  /** A partition builder: `.withValue(PartitionKey.SheetName, PartitionValue.Token("S1"))`. */
  def partBuilder: PartBuilder = PartBuilder.empty

  // ------------------------------------------------------------------
  // Resources
  // ------------------------------------------------------------------

  /** A ResourceSet with validated `@Name`. Child resources SHALL match the name
   *  — enforced structurally, since the payload's element name is compared with
   *  the ResourceSet name at build time.
   */
  def resourceSet(
      name: String,
      usage: Option[Usage] = None,
      processUsage: Option[String] = None,
      combinedProcessIndex: Option[NonEmptyChain[ProcessIndex]] = None
  )(resources: Resource*): ValidatedNec[Issue, ResourceSet] =
    val nameV = ResourceSetName
      .from(name)
      .toValidNec(Issue.error(XPath("/XJDF/ResourceSet/@Name"), s"Invalid ResourceSet name: '$name'"))
    val usageV = processUsage match
      case Some(raw) =>
        NmToken
          .from(raw)
          .toValidNec(Issue.error(XPath("/XJDF/ResourceSet/@ProcessUsage"), s"Invalid ProcessUsage: '$raw'"))
          .map(Some(_))
      case None => None.validNec
    val childrenOk = resources.forall(_.elementName.forall(_.value == name))
    val childrenV = Validated.condNec(
      childrenOk,
      (),
      Issue.error(XPath("/XJDF/ResourceSet/Resource"), s"Resource does not match ResourceSet @Name='$name'")
    )
    (nameV, usageV, childrenV).mapN { (n, pu, _) =>
      ResourceSet(
        name = n,
        usage = usage,
        processUsage = pu,
        combinedProcessIndex = combinedProcessIndex,
        resources = Chain.fromSeq(resources)
      )
    }

  /** A bodyless resource: `<Resource/>` (Table 6.1, Example 3.6). */
  def emptyResource: Resource = Resource.empty

  /** A resource with payload: `Resource.withPayload(payload)`. */
  def withPayload(payload: ResourcePayload): Resource = Resource.withPayload(payload)

  /** A resource carrying a Media payload. */
  def media(media: Media, id: Option[String] = None, parts: Part*): ValidatedNec[Issue, Resource] =
    validateId(id).map { idv =>
      Resource(specific = Some(ResourcePayload.MediaResource(media)), id = idv, parts = Chain.fromSeq(parts))
    }

  /** A resource carrying a Component payload. */
  def component(component: Component, id: Option[String] = None, parts: Part*): ValidatedNec[Issue, Resource] =
    validateId(id).map { idv =>
      Resource(specific = Some(ResourcePayload.ComponentResource(component)), id = idv, parts = Chain.fromSeq(parts))
    }

  /** A resource carrying a NodeInfo payload. */
  def nodeInfo(info: NodeInfo, parts: Part*): Resource =
    Resource(specific = Some(ResourcePayload.NodeInfoResource(info)), parts = Chain.fromSeq(parts))

  /** A resource carrying a RunList payload. */
  def runList(runList: RunList, id: Option[String] = None, parts: Part*): ValidatedNec[Issue, Resource] =
    validateId(id).map { idv =>
      Resource(specific = Some(ResourcePayload.RunListResource(runList)), id = idv, parts = Chain.fromSeq(parts))
    }

  /** A resource carrying a DeliveryParams payload, partitioned by `@DropID`. */
  def delivery(dropId: String, params: DeliveryParams): ValidatedNec[Issue, Resource] =
    NmToken
      .from(dropId)
      .toValidNec(Issue.error(XPath("/XJDF/ResourceSet/Resource/Part"), s"Invalid DropID: '$dropId'"))
      .map { token =>
        Resource(
          specific = Some(ResourcePayload.DeliveryParamsResource(params)),
          parts = Chain.one(Part.token(PartitionKey.DropID, token))
        )
      }

  private def validateId(id: Option[String]): ValidatedNec[Issue, Option[Id]] =
    id match
      case None => None.validNec
      case Some(raw) =>
        Id.from(raw)
          .toValidNec(Issue.error(XPath("/XJDF/Resource/@ID"), s"Invalid ID: '$raw'"))
          .map(Some(_))

  // ------------------------------------------------------------------
  // Products and intents
  // ------------------------------------------------------------------

  /** A product with optional amount, product type, id and intents. */
  def product(
      amount: Option[Long] = None,
      isRoot: Boolean = true,
      productType: Option[String] = None,
      id: Option[String] = None,
      externalId: Option[String] = None
  )(intents: Intent*): ValidatedNec[Issue, Product] =
    val idValidated = validateId(id)
    val typeV = productType match
      case Some(raw) =>
        NmToken
          .from(raw)
          .toValidNec(Issue.error(XPath("/XJDF/ProductList/Product/@ProductType"), s"Invalid ProductType: '$raw'"))
          .map(Some(_))
      case None => None.validNec
    val externalV = externalId match
      case Some(raw) =>
        NmToken
          .from(raw)
          .toValidNec(Issue.error(XPath("/XJDF/ProductList/Product/@ExternalID"), s"Invalid ExternalID: '$raw'"))
          .map(Some(_))
      case None => None.validNec
    val amountOk = amount.forall(_ >= 0L)
    val amountV = Validated.condNec(
      amountOk,
      (),
      Issue.error(XPath("/XJDF/ProductList/Product/@Amount"), "Negative product amount")
    )
    (idValidated, typeV, externalV, amountV).mapN { (idv, tv, ev, _) =>
      Product(
        amount = amount,
        externalId = ev,
        id = idv,
        isRoot = isRoot,
        productType = tv,
        intents = Chain.fromSeq(intents)
      )
    }

  /** An intent container: name + payload (Table 4.1). */
  def intent(name: String, payload: IntentPayload): ValidatedNec[Issue, Intent] =
    IntentName
      .from(name)
      .toValidNec(Issue.error(XPath("/XJDF/ProductList/Intent/@Name"), s"Invalid Intent name: '$name'"))
      .andThen { n =>
        val i = Intent(name = n, specific = payload)
        Validated.condNec(
          i.isLawful,
          i,
          Issue.error(
            XPath("/XJDF/ProductList/Intent"),
            s"Intent @Name='$name' does not match payload ${payload.elementName.value}"
          )
        )
      }

  // ------------------------------------------------------------------
  // Tickets
  // ------------------------------------------------------------------

  /** A ticket draft: fields are added incrementally, validated at `build`. */
  final case class TicketDraft private (
      jobId: Option[JobId],
      types: Chain[ProcessType],
      jobPartId: Option[JobPartId],
      projectId: Option[ProjectId],
      productList: Option[ProductList],
      resourceSets: Chain[ResourceSet],
      auditPool: Option[AuditPool],
      comments: Chain[Comment],
      generalIds: Chain[GeneralID]
  ):
    /** Safely sets `@JobPartID`, preserving an invalid raw value as an Issue. */
    def withJobPart(jobPartId: String): ValidatedNec[Issue, TicketDraft] =
      JobPartId
        .from(jobPartId)
        .toValidNec(Issue.error(XPath("/XJDF/@JobPartID"), s"Invalid JobPartID: '$jobPartId'"))
        .map(value => copy(jobPartId = Some(value)))

    /** Sets `@JobPartID` or throws `IllegalArgumentException`. Prefer `withJobPart`. */
    def withJobPartUnsafe(jobPartId: String): TicketDraft =
      withJobPart(jobPartId).toEither.fold(
        issues => throw new IllegalArgumentException(issues.head.message),
        identity
      )

    /** Safely sets `@ProjectID`, preserving an invalid raw value as an Issue. */
    def withProject(projectId: String): ValidatedNec[Issue, TicketDraft] =
      ProjectId
        .from(projectId)
        .toValidNec(Issue.error(XPath("/XJDF/@ProjectID"), s"Invalid ProjectID: '$projectId'"))
        .map(value => copy(projectId = Some(value)))

    /** Sets `@ProjectID` or throws `IllegalArgumentException`. Prefer `withProject`. */
    def withProjectUnsafe(projectId: String): TicketDraft =
      withProject(projectId).toEither.fold(
        issues => throw new IllegalArgumentException(issues.head.message),
        identity
      )

    def withProductList(pl: ProductList): TicketDraft =
      copy(productList = Some(pl))

    def withResources(rs: ResourceSet*): TicketDraft =
      copy(resourceSets = resourceSets ++ Chain.fromSeq(rs))

    def withAuditPool(pool: AuditPool): TicketDraft =
      copy(auditPool = Some(pool))

    def withComment(comment: Comment): TicketDraft =
      copy(comments = comments :+ comment)

    def build: ValidatedNec[Issue, XJDF] =
      (jobId, NonEmptyChain.fromChain(types)) match
        case (None, _) =>
          Issue.error(XPath("/XJDF/@JobID"), "JobID is missing").invalidNec
        case (_, None) =>
          Issue.error(XPath("/XJDF/@Types"), "@Types SHALL contain at least one process type").invalidNec
        case (Some(job), Some(ts)) =>
          val ticket = XJDF(
            jobId = job,
            types = ts,
            jobPartId = jobPartId,
            projectId = projectId,
            productList = productList,
            resourceSets = resourceSets,
            auditPool = auditPool,
            comments = comments,
            generalIds = generalIds
          )
          ticket.validate.as(ticket)
  end TicketDraft

  object TicketDraft:

    def of(jobId: String, types: ProcessType*): ValidatedNec[Issue, TicketDraft] =
      val jobV = JobId
        .from(jobId)
        .toValidNec(Issue.error(XPath("/XJDF/@JobID"), s"Invalid JobID: '$jobId'"))
      jobV.map(job =>
        TicketDraft(Some(job), Chain.fromSeq(types), None, None, None, Chain.empty, None, Chain.empty, Chain.empty)
      )
  end TicketDraft

  // ------------------------------------------------------------------
  // Audits
  // ------------------------------------------------------------------

  /** An `AuditProcessRun` recording one completed workstep execution. */
  def processRunAudit(header: Header, run: ProcessRun): Audit = Audit.Run(header, run)

  /** An `AuditResource` recording resource usage. */
  def resourceAudit(header: Header, info: ResourceInfo): Audit = Audit.Resource(header, info)

  /** An `AuditStatus` recording a Device status phase. */
  def statusAudit(header: Header, device: DeviceInfo): Audit = Audit.Status(header, device)

  /** An `AuditCreated` entry — SHOULD be the first audit (§3.2.1). */
  def createdAudit(header: Header): Audit = Audit.Created(header)

  /** An `AuditNotification` recording an event. */
  def notificationAudit(header: Header, notification: Notification): Audit = Audit.Notified(header, notification)

  // ------------------------------------------------------------------
  // Rendering
  // ------------------------------------------------------------------

  /** Renders a ticket as a compact, human-readable description. */
  def render(ticket: XJDF): String = Show[XJDF].show(ticket)
end dsl
