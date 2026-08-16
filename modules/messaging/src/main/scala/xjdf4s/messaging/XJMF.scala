package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.Header

/** XJMF envelope. Non-empty cardinality preserves the chapter 7.1 requirement. */
final case class XJMF(
    header: Header,
    messages: NonEmptyVector[Message],
    version: Option[Version] = None,
    schema: Option[UriRef] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible
