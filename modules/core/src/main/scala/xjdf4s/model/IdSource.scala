package xjdf4s
package model

import xjdf4s.prim.Id
import cats.data.{Chain, State}
import cats.syntax.all.*

/** A pure ID-allocation program. The state contains one counter per prefix, so
 *  independently named ID families do not affect each other.
 */
type IdAllocator[A] = State[Map[String, Int], A]

/** Pure source of fresh `@ID` values for DSL authoring. Programs are referentially
 *  transparent: the same initial counter map produces the same IDs.
 */
object IdSource:

  /** Allocates the next ID for `prefix`, beginning at `<prefix>_0`. */
  def freshId(prefix: String): IdAllocator[Id] =
    State { counters =>
      val count = counters.getOrElse(prefix, 0)
      (counters.updated(prefix, count + 1), Id.unsafe(s"${prefix}_$count"))
    }

  /** Allocates `n` fresh IDs in order for one prefix. */
  def freshMany(prefix: String, n: Int): IdAllocator[Chain[Id]] =
    List.fill(n)(freshId(prefix)).sequence.map(Chain.fromSeq)

  /** Runs a pure allocation program with no prior allocations. */
  def run[A](program: IdAllocator[A]): A = program.runA(Map.empty).value

end IdSource

/** Compatibility boundary for imperative integration only. This allocator is
 *  not thread-safe because it mutates a private counter; new domain and DSL
 *  code must use the pure `IdAllocator[A]` / `IdSource` State program instead.
 */
trait StatefulIdAllocator:
  def fresh(prefix: String): Id

object IdAllocator:

  /** Creates a non-thread-safe imperative allocator for integration boundaries.
   *  Prefer `IdSource.freshId` and `IdSource.run` in ordinary code.
   */
  def stateful(initial: Long): StatefulIdAllocator =
    new StatefulIdAllocator:
      private var counter = initial

      def fresh(prefix: String): Id =
        val id = Id.unsafe(s"${prefix}_$counter")
        counter += 1L
        id

end IdAllocator
