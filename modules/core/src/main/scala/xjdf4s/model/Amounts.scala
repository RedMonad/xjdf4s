package xjdf4s
package model

import xjdf4s.prim.*
import cats.Show
import cats.data.{Chain, NonEmptyChain}
import cats.kernel.{Eq, Semigroup}

/** `PartWaste` (Table 6.5): particulars of a type or source of waste.
 *  At least one of `@ModuleIDs` and `@WasteDetails` SHALL be specified.
 */
final case class PartWaste(
    waste: Amount,
    moduleIds: Option[NmTokens] = None,
    wasteDetails: Option[WasteDetail] = None
):

  /** The “at least one of” rule of Table 6.5, as a convenience predicate
   *  derived from the `DomainRule` (the XPath passed here is discarded
   *  because only the presence of issues matters). The real validation in
   *  `TicketValidator` passes the actual location.
   */
  def isLawful: Boolean = PartWaste.law.check(this, XPath("")).isEmpty
end PartWaste

object PartWaste:

  /** Table 6.5: at least one of `@ModuleIDs` / `@WasteDetails` SHALL be present.
   *  Explicitly invoked from `TicketValidator.checkResourceLocalLaws`.
   */
  val law: DomainRule[PartWaste] =
    (value: PartWaste, at: XPath) =>
      if value.moduleIds.isDefined || value.wasteDetails.isDefined then Chain.empty
      else
        Chain.one(
          Issue.errorC(
            IssueCode.LocalLawViolation,
            at,
            "PartWaste: at least one of @ModuleIDs or @WasteDetails SHALL be specified (Table 6.5)"
          )
        )

  given Show[PartWaste] = Show.fromToString

  given Eq[PartWaste] = Eq.fromUniversalEquals

end PartWaste

/** `PartAmount` (Table 6.3): the amounts and waste of a resource partition.
 *  Multiple PartAmount elements specify partial completion of resources
 *  (§6.1.2.1).
 */
final case class PartAmount(
    amount: Option[Amount] = None,
    bounds: AmountBounds = AmountBounds.unbounded,
    waste: Option[Amount] = None,
    parts: Chain[Part] = Chain.empty, // Table 6.3: Part* (0..*)
    partWaste: Chain[PartWaste] = Chain.empty
):

  /** Transitional accessor over the former single-`Part` cardinality (N-10).
   *  Returns the first of `parts`, if any. Removed before M2 — use `parts`.
   */
  @deprecated("transitional accessor; removed before M2", "M1")
  def part: Option[Part] = parts.headOption

  require(amount.forall(bounds.includes), "Amount is outside MinAmount/MaxAmount bounds")
end PartAmount

object PartAmount:

  given Show[PartAmount] =
    Show.show { pa =>
      val base = List(
        pa.amount.map(value => s"amount ${Show[Amount].show(value)}"),
        Option.when(pa.bounds != AmountBounds.unbounded)(Show[AmountBounds].show(pa.bounds))
      ).flatten.mkString(", ")
      val parts = pa.parts.toList.map(Show[Part].show)
      if parts.isEmpty then s"PartAmount($base)"
      else s"PartAmount($base, parts=[${parts.mkString(", ")}])"
    }

  given Eq[PartAmount] = Eq.fromUniversalEquals

end PartAmount

/** `AmountPool` (Table 6.2): a non-empty, ordered list of PartAmount elements —
 *  the amount-related metadata of a Resource. Concatenation is the free-monoid
 *  (semigroup) operation: planning amounts and recorded amounts accumulate in
 *  order.
 */
opaque type AmountPool = NonEmptyChain[PartAmount]

object AmountPool:

  def of(head: PartAmount, tail: PartAmount*): AmountPool =
    NonEmptyChain(head, tail*)

  def from(chain: NonEmptyChain[PartAmount]): AmountPool = chain

  extension (pool: AmountPool)
    /** The underlying non-empty chain (representation). */
    def toNonEmptyChain: NonEmptyChain[PartAmount] = pool
    def toList: List[PartAmount] = pool.toNonEmptyChain.toChain.toList

    /** Total planned amount across all PartAmount entries. */
    def totalAmount: Amount =
      pool.toNonEmptyChain.toChain.toList.foldLeft(Amount.zero)((acc, pa) => acc + pa.amount.getOrElse(Amount.zero))

    /** Total waste across all PartAmount entries. */
    def totalWaste: Amount =
      pool.toNonEmptyChain.toChain.toList.foldLeft(Amount.zero)((acc, pa) => acc + pa.waste.getOrElse(Amount.zero))
  end extension

  given Semigroup[AmountPool] with
    def combine(a: AmountPool, b: AmountPool): AmountPool =
      NonEmptyChain.fromChainUnsafe(a.toNonEmptyChain.toChain ++ b.toNonEmptyChain.toChain)

  given Show[AmountPool] =
    Show.show(pool => pool.toNonEmptyChain.toChain.toList.map(Show[PartAmount].show).mkString("[", ", ", "]"))

  given Eq[AmountPool] = Eq.fromUniversalEquals

end AmountPool
