package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.Header

/** XJMF envelope. Non-empty cardinality preserves the chapter 7.1 requirement. The JSON-only `$schema` and `@Name`
 *  properties are codec concerns and are intentionally absent from the domain root; the JSON exactly-one-message
 *  restriction is likewise a codec-level check.
 */
final case class XJMF(
    header: Header,
    messages: NonEmptyVector[Message],
    version: Option[Version] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible,
      ValidatedNode:

  override def validate: Vector[ValidationError] =
    messages.toVector
      .map(_.header.id)
      .collect { case Some(id) => id }
      .groupBy(identity)
      .iterator
      .collect { case (_, occurrences) if occurrences.size > 1 => ValidationError.DuplicateId(occurrences.head.value) }
      .toVector
end XJMF
