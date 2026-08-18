package xjdf4s

import xjdf4s.core.{Extensible, XjdfNode}
import xjdf4s.messaging.{Command, Message, Query, Response, Signal, XJMF}
import xjdf4s.model.XJDF

/** Exactly the two top-level protocol document alternatives. */
type ProtocolDocument = XJDF | XJMF

/** Request-like and event/result alternatives expressed without wrapper allocation. */
type RequestMessage = Query | Command
type ResultMessage = Signal | Response

/** Cross-cutting capability for protocol values carrying extension wildcards. */
type ExtensibleProtocolNode = XjdfNode & Extensible

object Protocol:
  def documentKind(document: ProtocolDocument): String = document match
    case _: XJDF => "XJDF"
    case _: XJMF => "XJMF"

  def messageFamily(message: Message): String = message match
    case _: Query => "Query"
    case _: Command => "Command"
    case _: Signal => "Signal"
    case _: Response => "Response"
    case _ => "Extension"
end Protocol
