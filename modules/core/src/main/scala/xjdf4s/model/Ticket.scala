package xjdf4s
package model

import xjdf4s.prim.*
import cats.Show
import cats.data.{Chain, NonEmptyChain, ValidatedNec}
import cats.kernel.Eq
import cats.syntax.all.*

/** Marker of relaxed cardinality: a change order only conveys the modified
 *  values (§1.3.2, §1.6.5).
 */
trait Partial

/** `XJDF` (Table 3.1): the root element — a single transaction between two
 *  parties. There is exactly one `XJDF` element per ticket; multiple work steps
 *  SHALL be submitted as separate XJDF (§3).
 *
 *  The ticket carries the three orthogonal concerns of the specification:
 *  `ProductList` (bill of materials), `ResourceSet`s (process resources) and
 *  `AuditPool` (recorded history) — the coproduct-style root of the model.
 */
final case class XJDF(
    jobId: JobId,
    types: NonEmptyChain[ProcessType],
    jobPartId: Option[JobPartId] = None,
    projectId: Option[ProjectId] = None,
    category: Option[NmToken] = None,
    commentUrl: Option[Url] = None,
    descriptiveName: Option[XjdfString] = None,
    icsVersions: Option[NmTokens] = None,
    relatedJobId: Option[JobId] = None,
    relatedJobPartId: Option[JobPartId] = None,
    relatedProjectId: Option[ProjectId] = None,
    version: XjdfVersion = XjdfVersion.V2_2,
    productList: Option[ProductList] = None,
    resourceSets: Chain[ResourceSet] = Chain.empty,
    auditPool: Option[AuditPool] = None,
    comments: Chain[Comment] = Chain.empty,
    generalIds: Chain[GeneralID] = Chain.empty
) extends Partial:

  /** The ordered list of processes executed by this ticket (§5.2). */
  def processPath: ProcessPath = ProcessPath(types)

  /** True for product tickets: `@Types` contains `"Product"` (§3.1.2). */
  def isProductTicket: Boolean = processPath.contains(ProcessType.Product)

  /** The ResourceSets with a given name, in document order. */
  def resourceSetsNamed(name: ResourceSetName): Chain[ResourceSet] =
    resourceSets.filter(_.name == name)

  /** All document-scoped `@ID`s declared inside this ticket (§2.2.3).
   *  Header/@ID from AuditPool is messaging-scoped (Table 7.3) and excluded.
   */
  def declaredIds: Chain[Id] =
    val resourceIds =
      resourceSets.flatMap(rs => Chain.fromOption(rs.id) ++ rs.resources.flatMap(r => Chain.fromOption(r.id)))
    val productIds = productList.fold(Chain.empty[Id])(_.declaredIds)
    resourceIds ++ productIds

  /** All IDREFs used inside this ticket across resources, products and audits (§2.2.3). */
  def references: Chain[IdRef] =
    val resourceRefs = resourceSets.flatMap(_.references)
    val productRefs = productList.fold(Chain.empty[IdRef])(_.references)
    val auditRefs = auditPool.fold(Chain.empty[IdRef])(_.references)
    resourceRefs ++ productRefs ++ auditRefs

  /** §2.2.2: an individual workstep is uniquely identified by the combination of
   *  `@JobID`, `@JobPartID` and the Partition Keys of its `Part` elements.
   */
  def workstepKeys: Chain[WorkstepKey] =
    resourceSets.flatMap: rs =>
      rs.resources.flatMap: r =>
        if r.parts.isEmpty then Chain.one(WorkstepKey(jobId, jobPartId, Part.empty))
        else r.parts.map(part => WorkstepKey(jobId, jobPartId, part))

end XJDF

object XJDF:

  /** Builds a ticket from raw attribute strings, accumulating validation issues. */
  def of(jobId: String, types: NonEmptyChain[ProcessType]): ValidatedNec[Issue, XJDF] =
    JobId.from(jobId).toValidNec(Issue.error(XPath("/XJDF"), s"Invalid JobID: '$jobId'")).map(XJDF(_, types))

  given Show[XJDF] =
    Show.show { x =>
      val mandatory = List(
        s"job=${x.jobId.value}",
        s"types=${Show[ProcessPath].show(x.processPath)}"
      )
      val optional: List[Option[String]] = List(
        x.jobPartId.map(jp => s"part=${jp.value}"),
        x.productList.map(pl => Show[ProductList].show(pl)),
        x.auditPool.map(ap => s"audits=${ap.toList.size}")
      )
      val parts = mandatory ++ optional.flatten
      s"XJDF(${parts.mkString(", ")})"
    }

  given Eq[XJDF] = Eq.fromUniversalEquals

end XJDF

/** §2.2.2: the unique identification of an individual workstep — the
 *  combination of `@JobID`, `@JobPartID` and the Partition Keys defined in any
 *  `Part`. A named tuple behind an opaque type.
 */
opaque type WorkstepKey = (jobId: JobId, jobPartId: Option[JobPartId], part: Part)

object WorkstepKey:

  def apply(jobId: JobId, jobPartId: Option[JobPartId], part: Part): WorkstepKey =
    (jobId = jobId, jobPartId = jobPartId, part = part)

  extension (key: WorkstepKey)
    def jobId: JobId = key.jobId
    def jobPartId: Option[JobPartId] = key.jobPartId
    def part: Part = key.part

  given Show[WorkstepKey] =
    Show.show: k =>
      s"${k.jobId.value}${k.jobPartId.fold("")(jp => s"/${jp.value}")} ${Show[Part].show(k.part)}"

  given Eq[WorkstepKey] = Eq.fromUniversalEquals

end WorkstepKey
