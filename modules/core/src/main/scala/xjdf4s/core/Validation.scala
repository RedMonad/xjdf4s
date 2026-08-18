package xjdf4s.core

import cats.{Eq, Show}
import cats.data.{NonEmptyList, Validated, ValidatedNel}

/** Domain validation error vocabulary. Type-level invariants (opaque types, coproducts, cardinality containers) make
 *  most invalid states unrepresentable; this error set backs the remaining cross-field SHALL constraints, which are
 *  exposed per node through [[ValidatedNode#validate]].
 */
enum ValidationError derives CanEqual:
  case EmptyValue(field: String)
  case EmptyCollection(field: String)
  case InvalidValue(field: String, value: String, expected: String)
  case ValueOutOfBounds(field: String, value: String, bounds: String)
  case OddListLength(field: String, length: Int)
  case ConflictingValues(fields: Vector[String], note: String)
  case MissingCompanionValue(field: String, companion: String)
  case DuplicateId(id: String)
  case ForeignNameExpected(name: String)

  /** Context wrapper: the document path of the node that produced the underlying error. */
  case AtPath(path: Vector[String], underlying: ValidationError)
end ValidationError

object ValidationError:
  given Eq[ValidationError] = Eq.fromUniversalEquals

  given Show[ValidationError] = Show.show(render)

  private def render(error: ValidationError): String =
    error match
      case ValidationError.EmptyValue(field) =>
        s"$field: value must not be empty"
      case ValidationError.EmptyCollection(field) =>
        s"$field: collection must not be empty"
      case ValidationError.InvalidValue(field, value, expected) =>
        s"$field: '$value' is invalid; expected $expected"
      case ValidationError.ValueOutOfBounds(field, value, bounds) =>
        s"$field: $value is out of bounds $bounds"
      case ValidationError.OddListLength(field, length) =>
        s"$field: list length $length must be even and non-empty"
      case ValidationError.ConflictingValues(fields, note) =>
        s"${fields.mkString(", ")}: mutually exclusive — $note"
      case ValidationError.MissingCompanionValue(field, companion) =>
        s"$field requires $companion"
      case ValidationError.DuplicateId(id) =>
        s"duplicate ID '$id'"
      case ValidationError.ForeignNameExpected(name) =>
        s"'$name' is a standard XJDF name; a foreign-namespace name is required"
      case ValidationError.AtPath(path, underlying) =>
        s"${path.mkString(" → ")}: ${render(underlying)}"
end ValidationError

/** Non-blocking advisory note attached to an otherwise valid node or document (e.g. deprecation notes). */
final case class Warning(code: String, message: String)

object Warning:
  given Show[Warning] = Show.show(warning => s"[${warning.code}] ${warning.message}")

/** Two-channel result of a validation pass: blocking errors plus non-blocking warnings. Errors make a document
 *  invalid; warnings never do.
 */
final case class ValidationOutcome(errors: Vector[ValidationError], warnings: Vector[Warning]):
  def isValid: Boolean = errors.isEmpty
  def toValidatedNel: ValidatedNel[ValidationError, Unit] = errors.toValidatedNel

extension (errors: Vector[ValidationError])
  /** Accumulating view: `Invalid` carries every error in a `NonEmptyList` (cats `ValidatedNel`). */
  def toValidatedNel: ValidatedNel[ValidationError, Unit] =
    NonEmptyList.fromList(errors.toList) match
      case Some(nel) => Validated.invalid(nel)
      case None => Validated.validNel[ValidationError, Unit](())

  /** Fail-fast view of the same errors. */
  def toEitherNel: Either[NonEmptyList[ValidationError], Unit] =
    toValidatedNel.toEither
end extension

extension (node: ValidatedNode)
  def validateNel: ValidatedNel[ValidationError, Unit] =
    node.validate.toValidatedNel

  /** Attaches a document path to every error produced by this node. */
  def validateAt(path: Vector[String]): ValidatedNel[ValidationError, Unit] =
    node.validate.map(error => ValidationError.AtPath(path, error)).toValidatedNel
end extension

/** Compositional validation hook for nodes carrying normative cross-field SHALL constraints that cannot be expressed as
 *  a product/coproduct shape (mutually exclusive attributes, reference targets, dependent attributes). A node with no
 *  such constraints simply returns an empty vector; the API exists uniformly so that documents and codecs can reject
 *  invalid states without pattern-matching on every node kind.
 */
trait ValidatedNode:
  def validate: Vector[ValidationError]
