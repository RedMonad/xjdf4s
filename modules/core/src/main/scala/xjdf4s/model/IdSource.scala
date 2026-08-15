package xjdf4s
package model

import xjdf4s.prim.Id
import cats.data.State

/**
 * Fresh `@ID` allocation is a stateful computation — an effect, modelled
 * purely with `cats.data.State`: `fresh` threads an internal counter and
 * returns a new, unique ID for the current document.
 */
object IdSource:

  opaque type Counter = Long

  object Counter:
    val zero: Counter = 0L

  /** Allocates the next ID with the given prefix, e.g. `r_000007`. */
  def fresh(prefix: String): State[Counter, Id] =
    State(c => (c + 1L, Id.unsafe(s"${prefix}_${c + 1L}")))

  /** Runs an ID-allocation program, discarding the final counter. */
  def run[A](program: State[Counter, A]): A =
    program.runA(Counter.zero).value

  /** Allocates a sequence of IDs with one prefix. */
  def freshMany(prefix: String, n: Int): State[Counter, List[Id]] =
    import cats.syntax.traverse.*
    List.fill(n)(fresh(prefix)).sequence

end IdSource

/**
 * Context function of ticket authoring: code inside a `WithIds[A]` scope can
 * allocate fresh IDs via `summon[IdAllocator]`. This is the “build-time
 * environment” of the declarative DSL.
 */
trait IdAllocator:
  def fresh(prefix: String): Id

type WithIds[A] = IdAllocator ?=> A

object IdAllocator:

  /** An allocator that runs the pure `State`-based source internally. */
  def stateful(initial: Long): IdAllocator =
    new IdAllocator:
      private var counter = initial
      def fresh(prefix: String): Id =
        counter += 1
        Id.unsafe(s"${prefix}_$counter")

  /** Runs a context-function body with a fresh allocator. */
  def run[A](body: IdAllocator ?=> A): A =
    given IdAllocator = stateful(0L)
    body

end IdAllocator
