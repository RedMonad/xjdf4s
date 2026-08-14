package xjdf4s
package model

import cats.Show
import cats.data.{Chain, Ior, NonEmptyChain}
import cats.kernel.{Monoid, Semigroup}

/**
 * A change order (§1.3.2): a declarative modification of a ticket. The simplest
 * method of initiating a change is to send an XJDF that contains only the
 * modified values — the change order therefore IS a patch.
 *
 * Patches form a lawful monoid under composition: the *endomorphism monoid*
 * `Endo[XJDF]` acting on the set of tickets. `identity` is the empty change;
 * `combine` is function composition. The action is a right monoid action:
 * `applyTo(applyTo(t, p), q) == applyTo(t, p |+| q)`.
 */
opaque type Patch = XJDF => XJDF

object Patch:

  def apply(f: XJDF => XJDF): Patch = f

  /** The empty change order: changes nothing. */
  val identity: Patch = t => t

  extension (p: Patch)
    def applyTo(ticket: XJDF): XJDF = p(ticket)
    def andThen(q: Patch): Patch    = t => q(p(t))

  given Monoid[Patch] with
    def empty: Patch = Patch.identity
    def combine(a: Patch, b: Patch): Patch = a.andThen(b)

  given Show[Patch] = Show.show(_ => "Patch(<modification>)")

  // ------------------------------------------------------------------
  // The declarative vocabulary of change orders
  // ------------------------------------------------------------------

  /** Replaces the whole AuditPool — e.g. the Device appends its audits. */
  def withAuditPool(pool: AuditPool): Patch =
    Patch(t => t.copy(auditPool = Some(pool)))

  /** Appends audits to the existing (or a new) audit pool, chronologically. */
  def appendAudits(audits: NonEmptyChain[Audit]): Patch =
    Patch: t =>
      val appended = t.auditPool match
        case Some(existing) => Semigroup[AuditPool].combine(existing, AuditPool.from(audits))
        case None           => AuditPool.from(audits)
      t.copy(auditPool = Some(appended))

  /** Replaces the ProductList. */
  def withProductList(pl: ProductList): Patch =
    Patch(t => t.copy(productList = Some(pl)))

  /** Adds one ResourceSet to the ticket. */
  def addResourceSet(rs: ResourceSet): Patch =
    Patch(t => t.copy(resourceSets = t.resourceSets :+ rs))

  /** Applies `f` to every ResourceSet, keeping the ones `f` returns. */
  def updateResourceSets(f: ResourceSet => Option[ResourceSet]): Patch =
    Patch(t => t.copy(resourceSets = t.resourceSets.flatMap(r => Chain.fromOption(f(r)))))

  /** Adds a comment to the ticket. */
  def addComment(comment: Comment): Patch =
    Patch(t => t.copy(comments = t.comments :+ comment))

  /**
   * Merges change-order ResourceSets into a ticket. The result is an `Ior`:
   * `Right` — a clean merge; `Both` — merged, but some ResourceSet keys were
   * duplicated (the update wins, the issue is reported); `Left` — the update
   * cannot be applied at all.
   */
  def mergeResourceSets(ticket: XJDF, update: Chain[ResourceSet]): Ior[NonEmptyChain[Issue], XJDF] =
    val conflicts = update.filter(rs => ticket.resourceSets.exists(_.key == rs.key))
    val merged    = ticket.copy(resourceSets = ticket.resourceSets ++ update)
    if conflicts.isEmpty then Ior.right(merged)
    else
      val issues = conflicts.map: rs =>
        Issue.warning(XPath("/XJDF/ResourceSet"), s"Duplicate ResourceSet key replaced: ${Show[ResourceSetKey].show(rs.key)}")
      NonEmptyChain.fromChain(issues) match
        case Some(nec) => Ior.both(nec, merged)
        case None      => Ior.right(merged)

end Patch
