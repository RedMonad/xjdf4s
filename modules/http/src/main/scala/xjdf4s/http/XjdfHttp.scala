package xjdf4s.http

import xjdf4s.core.*
import xjdf4s.model.Header

/** Small shared helpers of the HTTP layer: the device identity of the demo server, the current timestamp and
 *  response-header construction (`Header/@ID` fresh per response, `Header/@refID` per Table 7.3).
 */
object XjdfHttp:

  val serverDeviceId: Nmtoken = Nmtoken.from("server-1").toOption.get

  /** The current time as an XSD dateTime (the ISO lexical form always matches the domain pattern). */
  def now: XsdDateTime =
    XsdDateTime.from(java.time.OffsetDateTime.now().toString).toOption.get

  def responseHeader(deviceId: Nmtoken, refId: Nmtoken, id: String): Header =
    Header(
      deviceId,
      now,
      id = Some(XsdId.from(id).toOption.get),
      refId = Some(refId),
    )
end XjdfHttp
