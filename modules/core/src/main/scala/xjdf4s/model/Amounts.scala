package xjdf4s
package model

import xjdf4s.prim.*
import cats.Show
import cats.data.{Chain, NonEmptyChain}
import cats.kernel.{Eq, Semigroup}

/**
 * `PartWaste` (Table 6.5): particulars of a type or source of waste.
 * At least one of `@ModuleIDs` and `@WasteDetails` SHALL be specified.
 */
final case class PartWaste(
  waste: Amount,
  moduleIds: Option[NmTokens] = None,
  wasteDetails: Option[WasteDetail] = None
):

  /** The “at least one of” rule of Table 6.5. */
  def isLawful: Boolean = moduleIds.isDefined || wasteDetails.isDefined

object PartWaste:

  given Show[PartWaste] = Show.fromToString

  given Eq[PartWaste] = Eq.fromUniversalEquals

end PartWaste

/**
 * `PartAmount` (Table 6.3): the amounts and waste of a resource partition.
 * Multiple PartAmount elements specify partial completion of resources
 * (§6.1.2.1).
 */
final case class PartAmount(
  amount: Option[Amount] = None,
  maxAmount: Option[Amount] = None,
  minAmount: Option[Amount] = None,
  waste: Option[Amount] = None,
  part: Part = Part.empty,
  partWaste: Chain[PartWaste] = Chain.empty
):

  /** The amount attributes viewed as one range. */
  def range: AmountRange = AmountRange(amount, maxAmount, minAmount)

object PartAmount:

  given Show[PartAmount] =
    Show.show { pa =>
      val base = Show[AmountRange].show(pa.range)
      if pa.part.isEmpty then s"PartAmount($base)"
      else s"PartAmount($base, ${Show[Part].show(pa.part)})"
    }

  given Eq[PartAmount] = Eq.fromUniversalEquals

end PartAmount

/**
 * `AmountPool` (Table 6.2): a non-empty, ordered list of PartAmount elements —
 * the amount-related metadata of a Resource. Concatenation is the free-monoid
 * (semigroup) operation: planning amounts and recorded amounts accumulate in
 * order.
 */
opaque type AmountPool = NonEmptyChain[PartAmount]

object AmountPool:

  def of(head: PartAmount, tail: PartAmount*): AmountPool =
    NonEmptyChain(head, tail*)

  def from(chain: NonEmptyChain[PartAmount]): AmountPool = chain

  extension (pool: AmountPool)
    def toChain: NonEmptyChain[PartAmount] = pool
    def toList: List[PartAmount]           = pool.toList
    def head: PartAmount                   = pool.head

    /** Total planned amount across all PartAmount entries. */
    def totalAmount: Amount = pool.toList.foldLeft(Amount.zero)((acc, pa) => acc + pa.amount.getOrElse(Amount.zero))

    /** Total waste across all PartAmount entries. */
    def totalWaste: Amount = pool.toList.foldLeft(Amount.zero)((acc, pa) => acc + pa.waste.getOrElse(Amount.zero))

  given Semigroup[AmountPool] with
    def combine(a: AmountPool, b: AmountPool): AmountPool =
      NonEmptyChain.fromChainUnsafe(a.toChain ++ b.toChain)

  given Show[AmountPool] =
    Show.show(pool => pool.toList.map(Show[PartAmount].show).mkString("[", ", ", "]"))

  given Eq[AmountPool] = Eq.fromUniversalEquals

end AmountPool
