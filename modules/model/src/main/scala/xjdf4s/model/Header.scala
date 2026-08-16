package xjdf4s.model

import xjdf4s.core.*

final case class Header(
    deviceId: Nmtoken,
    time: XsdDateTime,
    agentName: Option[String] = None,
    agentVersion: Option[String] = None,
    author: Option[String] = None,
    descriptiveName: Option[String] = None,
    icsVersions: Vector[Nmtoken] = Vector.empty,
    id: Option[XsdId] = None,
    personalId: Option[Nmtoken] = None,
    refId: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

trait HasHeader:
  def header: Header
end HasHeader
