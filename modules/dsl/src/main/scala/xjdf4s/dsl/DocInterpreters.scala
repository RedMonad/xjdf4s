package xjdf4s.dsl

import cats.~>
import cats.data.{Chain, State, Writer}
import cats.implicits.*

import xjdf4s.core.*
import xjdf4s.model.*

/**
 * The three standard executions of a [[DocDsl]] program:
 *
 *  1. `buildDocument` — assembles a finished `XJDF` by folding the steps into document state;
 *  2. `traceProgram` — write-only trace of the steps, without building a document;
 *  3. `validateSteps` — fail-fast dry-run that rejects the first step adding an invalid node.
 *
 * One program, several interpreters: the program itself never changes when a new execution is added.
 *
 * The target effects are declared as type aliases (Scala 3 type lambdas are not expressible with the
 * kind-projector `*` placeholder unless `-Ykind-projector` is enabled).
 */
object DocInterpreters:

  type BuildState[A] = State[XJDF, A]
  type TraceWriter[A] = Writer[Chain[String], A]
  type StepValidation[A] = Either[Vector[ValidationError], A]

  /** Assembles a document from program steps, starting from a seed `XJDF`. */
  val buildDocument: DocOp ~> BuildState = new (DocOp ~> BuildState):
    def apply[A](op: DocOp[A]): BuildState[A] =
      op match
        case DocOp.SetVersion(value) => State.modify[XJDF](_.copy(version = Some(value)))
        case DocOp.AddComment(value) =>
          State.modify[XJDF](document => document.copy(comments = document.comments :+ value))
        case DocOp.AddGeneralId(value) =>
          State.modify[XJDF](document => document.copy(generalIds = document.generalIds :+ value))
        case DocOp.AddAudit(value) =>
          State.modify[XJDF](document => document.copy(auditPool = document.auditPool match
            case Some(pool) => Some(pool.copy(audits = pool.audits :+ value))
            case None       => Some(AuditPool(Vector(value)))))
        case DocOp.AddProduct(value) =>
          State.modify[XJDF](document => document.copy(productList = document.productList match
            case Some(list) => Some(list.copy(products = list.products.append(value)))
            case None       => Some(ProductList(NonEmptyVector.one(value)))))
        case DocOp.AddResourceSet(value) =>
          State.modify[XJDF](document => document.copy(resourceSets = document.resourceSets :+ value))

  /** Runs a program against a seed document and returns the finished document. */
  def run(program: DocDsl[Unit], seed: XJDF): XJDF =
    program.foldMap(buildDocument).runS(seed).value

  /** Write-only interpreter: produces a human-readable trace of the steps. */
  val traceProgram: DocOp ~> TraceWriter = new (DocOp ~> TraceWriter):
    def apply[A](op: DocOp[A]): TraceWriter[A] =
      op match
        case DocOp.SetVersion(value)     => Writer.tell(Chain.one(s"set version ${value.lexical}"))
        case DocOp.AddComment(value)     => Writer.tell(Chain.one(s"add comment: ${value.value.take(40)}"))
        case DocOp.AddGeneralId(value)   => Writer.tell(Chain.one(s"add general id ${value.usage.value}"))
        case DocOp.AddAudit(value)       => Writer.tell(Chain.one(s"add audit ${value.name}"))
        case DocOp.AddProduct(value)     => Writer.tell(Chain.one(s"add product ${value.id.fold("(unnamed)")(_.value)}"))
        case DocOp.AddResourceSet(value) => Writer.tell(Chain.one(s"add resource set ${value.name.value}"))

  /** Runs a program for its trace only. */
  def trace(program: DocDsl[Unit]): Chain[String] =
    program.foldMap(traceProgram).written

  /** Fail-fast validation interpreter: rejects the first step that adds an invalid resource. */
  val validateSteps: DocOp ~> StepValidation = new (DocOp ~> StepValidation):
    def apply[A](op: DocOp[A]): StepValidation[A] =
      op match
        case DocOp.AddResourceSet(value) =>
          val errors = value.resources.flatMap(_.validate)
          Either.cond(errors.isEmpty, (), errors)
        case DocOp.SetVersion(_)     => Right(())
        case DocOp.AddComment(_)     => Right(())
        case DocOp.AddGeneralId(_)   => Right(())
        case DocOp.AddAudit(_)       => Right(())
        case DocOp.AddProduct(_)     => Right(())

  /** Dry-run: validates the program without building a document. */
  def dryRun(program: DocDsl[Unit]): Either[Vector[ValidationError], Unit] =
    program.foldMap(validateSteps)
end DocInterpreters
