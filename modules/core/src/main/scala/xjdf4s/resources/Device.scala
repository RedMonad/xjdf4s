package xjdf4s
package resources

import xjdf4s.prim.*
import cats.kernel.Eq

/** The `Device` resource (Table 6.57): the Device that is associated with
 *  processing this XJDF.
 */
final case class Device(
    deviceId: NmToken,
    costCenterId: Option[NmToken] = None,
    descriptiveName: Option[XjdfString] = None,
    deviceClass: Option[NmToken] = None,
    deviceType: Option[NmToken] = None,
    icsVersions: Option[NmTokens] = None,
    manufacturer: Option[XjdfString] = None,
    manufacturerUrl: Option[Url] = None,
    maxRunSpeed: Option[Double] = None,
    packaging: Option[NmToken] = None,
    presentationUrl: Option[Url] = None,
    restApiBaseUrl: Option[Url] = None,
    revision: Option[XjdfString] = None,
    serialNumber: Option[XjdfString] = None,
    urlSchemes: Option[NmTokens] = None,
    xjmfUrl: Option[Url] = None
)

object Device:

  given Eq[Device] = Eq.fromUniversalEquals

end Device
