package xjdf4s.dsl

import cats.Functor
import cats.free.Free
import xjdf4s.core.*
import xjdf4s.model.*

/** Free-monadic grammar for constructing XJDF documents. A program of this grammar is pure data: it describes *what*
 *  to build, and an interpreter decides *how* (document assembly, trace, step validation — see the interpreters in
 *  `DocInterpreters`). The design rationale and the interpreter totality rule are documented in `docs/free-dsl.md`.
 */
enum DocOp[A]:
  case SetVersion(value: Version) extends DocOp[Unit]
  case AddComment(value: Comment) extends DocOp[Unit]
  case AddGeneralId(value: GeneralId) extends DocOp[Unit]
  case AddAudit(value: Audit) extends DocOp[Unit]
  case AddProduct(value: Product) extends DocOp[Unit]
  case AddResourceSet(value: ResourceSet) extends DocOp[Unit]

object DocOp:
  /** Every instruction yields `Unit` and no constructor stores a value of the result type, so `map` is the identity
   *  on the instruction itself. The cast is safe and total: the GADT has no constructor whose type argument is
   *  anything other than `Unit`, hence every `DocOp[A]` value is also a `DocOp[B]` value. (A match-based version
   *  cannot be typed: GADT refinement only constrains the scrutinee's type parameter `A`, while `map` must return
   *  `DocOp[B]`.)
   */
  given Functor[DocOp] with
    def map[A, B](fa: DocOp[A])(f: A => B): DocOp[B] =
      fa.asInstanceOf[DocOp[B]]

/** A document-construction program: pure data, executed by an interpreter (see the `DocInterpreters` object). */
type DocDsl[A] = Free[DocOp, A]

object DocDsl:
  def version(value: Version): DocDsl[Unit] = Free.liftF(DocOp.SetVersion(value))

  def comment(value: Comment): DocDsl[Unit] = Free.liftF(DocOp.AddComment(value))
  def comment(text: String): DocDsl[Unit] = comment(Comment(text))

  def generalId(value: GeneralId): DocDsl[Unit] = Free.liftF(DocOp.AddGeneralId(value))
  def audit(value: Audit): DocDsl[Unit] = Free.liftF(DocOp.AddAudit(value))
  def product(value: Product): DocDsl[Unit] = Free.liftF(DocOp.AddProduct(value))
  def resourceSet(value: ResourceSet): DocDsl[Unit] = Free.liftF(DocOp.AddResourceSet(value))

  /** Convenience constructor for the most common case: an explicit resource set with a usage. */
  def resourceSet(name: Nmtoken, usage: ResourceUsage)(resources: Resource*): DocDsl[Unit] =
    Free.liftF(DocOp.AddResourceSet(ResourceSet(name, usage = Some(usage), resources = resources.toVector)))
end DocDsl
