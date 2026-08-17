package xjdf4s.core

/**
 * Domain validation error vocabulary. Type-level invariants (opaque types, coproducts, cardinality containers) make
 * most invalid states unrepresentable; this error set backs the remaining cross-field SHALL constraints, which are
 * exposed per node through [[ValidatedNode#validate]].
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
end ValidationError

/**
 * Compositional validation hook for nodes carrying normative cross-field SHALL constraints that cannot be expressed as
 * a product/coproduct shape (mutually exclusive attributes, reference targets, dependent attributes). A node with no
 * such constraints simply returns an empty vector; the API exists uniformly so that documents and codecs can reject
 * invalid states without pattern-matching on every node kind.
 */
trait ValidatedNode:
  def validate: Vector[ValidationError]
end ValidatedNode
