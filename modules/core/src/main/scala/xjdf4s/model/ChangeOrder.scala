package xjdf4s
package model

import xjdf4s.prim.*
import cats.Show
import cats.data.{Chain, Ior, Validated, ValidatedNec}
import cats.kernel.Eq
import cats.syntax.all.*

/** A change order (§1.3.2, §1.6.5 / Table 1.2): the incoming *partial*
 *  document that carries only the modified values. Cardinality is loosened
 *  relative to Table 3.1 — the only required trait is the addressing
 *  context `@JobID`. `@JobPartID`, when present, further identifies the
 *  workstep (§9.8.2.1.1).
 *
 *  This is not an `XJDF` and not a `Patch`. The three roles mixed by the
 *  degenerate `XJDF & Partial` alias (N-20) are kept apart (ADR-0001):
 *
 *   - `ChangeOrder` is the input document;
 *   - `compile` turns it into a lawful `Patch` (the endomorphism monoid);
 *   - `applyChange` applies the patch and *revalidates* the result, because
 *     a change order is capable of breaking the target ticket's invariants
 *     (§1.6.5).
 *
 *  `compile` implements Incremental semantics (Table 7.56 / §9.8.2): absent
 *  fields are left untouched. `Complete` / `Remove` live on
 *  `ResubmissionParams/@UpdateMethod` and are deferred to M4.
 *
 *  Field set closed against §1.3.2, §1.6.5, Table 3.1, Table 7.56,
 *  §9.8.2.1.1–9.8.2.1.2 and Examples 9.5–9.10 — see ADR-0001.
 */
final case class ChangeOrder(
    jobId: JobId,
    jobPartId: Option[JobPartId] = None,
    productList: Option[ProductList] = None,
    auditPool: Option[AuditPool] = None,
    resourceSets: Chain[ResourceSet] = Chain.empty,
    comments: Chain[Comment] = Chain.empty
)

object ChangeOrder:

  /** Compiles a change order against a base ticket into a lawful
   *  endomorphism. Fails when the addressing context does not match the
   *  base (§9.8.2.1.1) or when the update's own `ResourceSet`s clash
   *  (§3.4 / `Patch.mergeResourceSets`).
   *
   *  Result-level invariants (audit chronology, §6.1.2.1, BOM, …) are
   *  *not* checked here — they are the job of `applyChange`'s revalidation.
   */
  def compile(change: ChangeOrder, base: XJDF): ValidatedNec[Issue, Patch] =
    (checkContext(change, base), checkResourceSets(change, base)).mapN((_, _) => toPatch(change))

  /** Applies a change order to a base ticket and revalidates the result
   *  (ADR-0001). `Patch.applyTo` stays a total endomorphism; validation
   *  sits outside so `Monoid[Patch]` is undisturbed.
   */
  def applyChange(base: XJDF, change: ChangeOrder): ValidatedNec[Issue, XJDF] =
    compile(change, base).andThen { patch =>
      val result = Patch.applyTo(patch)(base)
      TicketValidator.validate(result).as(result)
    }

  /** `@JobID` SHALL match; `@JobPartID`, if supplied, SHALL match (§9.8.2.1.1). */
  private def checkContext(change: ChangeOrder, base: XJDF): ValidatedNec[Issue, Unit] =
    val job =
      if change.jobId == base.jobId then Validated.valid(())
      else
        Validated.invalidNec(
          Issue.errorC(
            IssueCode.ChangeOrderJobIdMismatch,
            XPath("/XJDF/@JobID"),
            s"Change order @JobID='${change.jobId.value}' does not match ticket @JobID='${base.jobId.value}' " +
              "(Table 7.56, §9.8.2.1.1)"
          )
        )
    val part = change.jobPartId match
      case None => Validated.valid(())
      case Some(jp) if base.jobPartId.contains(jp) => Validated.valid(())
      case Some(jp) =>
        Validated.invalidNec(
          Issue.errorC(
            IssueCode.ChangeOrderJobPartIdMismatch,
            XPath("/XJDF/@JobPartID"),
            s"Change order @JobPartID='${jp.value}' does not match ticket @JobPartID=" +
              s"'${base.jobPartId.fold("<absent>")(_.value)}' (§9.8.2.1.1)"
          )
        )
    job.combine(part)

  /** The update itself must be a deterministic §3.4 substitution. */
  private def checkResourceSets(change: ChangeOrder, base: XJDF): ValidatedNec[Issue, Unit] =
    if change.resourceSets.isEmpty then Validated.valid(())
    else
      Patch.mergeResourceSets(base, change.resourceSets) match
        case Ior.Left(errs) => Validated.invalid(errs)
        case Ior.Both(_, _) | Ior.Right(_) => Validated.valid(())

  /** The Incremental endomorphism (ADR-0001). Each present field becomes
   *  one `Patch`; they compose with `andThen` = `Monoid[Patch].combine`.
   */
  private def toPatch(change: ChangeOrder): Patch =
    val product = change.productList.fold(Patch.identity)(Patch.withProductList)
    val audits = change.auditPool.fold(Patch.identity)(pool => Patch.appendAudits(pool.toNonEmptyChain))
    val resources =
      if change.resourceSets.isEmpty then Patch.identity
      else upsertResourceSets(change.resourceSets)
    val comments = change.comments.toList.foldLeft(Patch.identity): (acc, comment) =>
      acc.andThen(Patch.addComment(comment))
    product.andThen(audits).andThen(resources).andThen(comments)

  /** Total ResourceSet upsert. Internal §3.4 clashes have already been
   *  rejected by `checkResourceSets`, so `Ior.Left` is unreachable here;
   *  the body therefore does not consult `mergeResourceSets` and cannot
   *  silently drop an illegal update.
   */
  private def upsertResourceSets(update: Chain[ResourceSet]): Patch =
    Patch { ticket =>
      val retained = ticket.resourceSets.filterNot(rs => update.exists(u => ResourceSet.clashesWith(rs, u)))
      ticket.copy(resourceSets = retained ++ update)
    }

  given Show[ChangeOrder] =
    Show.show { c =>
      val parts = List(
        Some(s"job=${c.jobId.value}"),
        c.jobPartId.map(jp => s"part=${jp.value}"),
        c.productList.map(_ => "productList"),
        c.auditPool.map(ap => s"audits=${ap.toList.size}"),
        Option.when(c.resourceSets.nonEmpty)(s"resourceSets=${c.resourceSets.size}"),
        Option.when(c.comments.nonEmpty)(s"comments=${c.comments.size}")
      ).flatten
      s"ChangeOrder(${parts.mkString(", ")})"
    }

  given Eq[ChangeOrder] = Eq.fromUniversalEquals

end ChangeOrder

extension (ticket: XJDF)
  /** Compiles and applies a change order, revalidating the result (ADR-0001). */
  def applyChange(change: ChangeOrder): ValidatedNec[Issue, XJDF] =
    ChangeOrder.applyChange(ticket, change)
