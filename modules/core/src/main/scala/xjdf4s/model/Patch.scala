package xjdf4s
package model

import xjdf4s.prim.*
import cats.Show
import cats.data.{Chain, Ior, NonEmptyChain}
import cats.kernel.{Monoid, Semigroup}

/** A change order (§1.3.2): a declarative modification of a ticket. The simplest
 *  method of initiating a change is to send an XJDF that contains only the
 *  modified values — the change order therefore IS a patch.
 *
 *  Patches form a lawful monoid under composition: the *endomorphism monoid*
 *  `Endo[XJDF]` acting on the set of tickets. `identity` is the empty change;
 *  `combine` is function composition. The action is a right monoid action:
 *  `applyTo(applyTo(t, p), q) == applyTo(t, p |+| q)`.
 */
opaque type Patch = XJDF => XJDF

object Patch:

  def apply(f: XJDF => XJDF): Patch = f

  /** The empty change order: changes nothing. */
  val identity: Patch = t => t

  extension (p: Patch)
    def applyTo(ticket: XJDF): XJDF = p(ticket)
    def andThen(q: Patch): Patch = t => q(p(t))

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
        case None => AuditPool.from(audits)
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

  /** Merges change-order ResourceSets into a ticket (§1.3.2, §3.4). The result is an `Ior`:
   *  `Right` — no conflicts, nothing replaced; `Both` — conflicting old ResourceSets were
   *  replaced by the update (the update wins); `Left` — the update itself is ambiguous
   *  (§3.4) and cannot be applied deterministically.
   *
   *  Conflict is the §3.4 predicate shared with the validator (`ResourceSet.clashesWith`):
   *  equal `@Name`/`@Usage`/`@ProcessUsage` and common or absent `@CombinedProcessIndex`.
   */
  def mergeResourceSets(ticket: XJDF, update: Chain[ResourceSet]): Ior[NonEmptyChain[Issue], XJDF] =
    NonEmptyChain.fromChain(pairsClashing(update)) match
      case Some(conflicting) =>
        Ior.left(
          conflicting.map: rs =>
            Issue.errorC(
              IssueCode.ResourceSetClash,
              XPath("/XJDF/ResourceSet"),
              s"Change order contains conflicting ResourceSets (§3.4): ${Show[ResourceSetKey].show(rs.key)}"
            )
        )
      case None =>
        val replaced = ticket.resourceSets.filter(rs => update.exists(u => ResourceSet.clashesWith(rs, u)))
        val retained = ticket.resourceSets.filterNot(rs => update.exists(u => ResourceSet.clashesWith(rs, u)))
        val merged   = ticket.copy(resourceSets = retained ++ update)
        NonEmptyChain.fromChain(replaced.map(warnReplaced)) match
          case Some(warnings) => Ior.both(warnings, merged)
          case None           => Ior.right(merged)

  /** The ResourceSets of a collection that clash with at least one *later* set (§3.4). */
  private def pairsClashing(sets: Chain[ResourceSet]): Chain[ResourceSet] =
    val list = sets.toList
    Chain.fromSeq(
      list.zipWithIndex.collect {
        case (a, i) if list.drop(i + 1).exists(b => ResourceSet.clashesWith(a, b)) => a
      }
    )

  /** A warning that an old ResourceSet was replaced by a change order (§3.4). */
  private def warnReplaced(rs: ResourceSet): Issue =
    Issue.warningC(
      IssueCode.ResourceSetClash,
      XPath("/XJDF/ResourceSet"),
      s"Duplicate ResourceSet replaced (§3.4): ${Show[ResourceSetKey].show(rs.key)}"
    )

end Patch

extension (ticket: XJDF)
  /** Applies a change-order patch — the right monoid action of `Patch` on
   *  tickets (§1.3.2).
   *
   *  M1.4-1 (ADR-0002): moved from a member of `XJDF` to an extension method
   *  in `Patch.scala` so `Ticket.scala` does not depend on the `Patch`
   *  implementation. Source-compatible wherever `xjdf4s.model.*` is imported.
   *
   *  The body uses the static form `Patch.applyTo(patch)(ticket)`: the
   *  extension-method syntax is not resolvable for the opaque `Patch` from a
   *  top-level extension body in the defining file, so the call goes through
   *  the companion's member directly.
   */
  def withPatch(patch: Patch): XJDF = Patch.applyTo(patch)(ticket)
