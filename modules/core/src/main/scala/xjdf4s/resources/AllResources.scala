package xjdf4s
package resources

import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** The closed vocabulary of specific Resources modelled by this library (a
 *  subset of Chapter 6), plus the escape hatch `Foreign` for extension
 *  ResourceSets in a proprietary namespace (§3.5.2). The `elementName` of a
 *  payload SHALL match the `@Name` of its ResourceSet.
 */
enum ResourcePayload:
  case MediaResource(value: Media)
  case ComponentResource(value: Component)
  case RunListResource(value: RunList)
  case NodeInfoResource(value: NodeInfo)
  case ContactResource(value: Contact)
  case DeliveryParamsResource(value: DeliveryParams)
  case CuttingParamsResource(value: CuttingParams)
  case FoldingParamsResource(value: FoldingParams)
  case LayoutResource(value: Layout)
  case ColorResource(value: Color)
  case PreviewResource(value: Preview)
  case DeviceResource(value: Device)
  case Foreign(namespace: NsPrefix, local: NmToken)

  /** The local element name this payload is serialized as. */
  def elementName: NmToken =
    this match
      case MediaResource(_) => NmToken.unsafe("Media")
      case ComponentResource(_) => NmToken.unsafe("Component")
      case RunListResource(_) => NmToken.unsafe("RunList")
      case NodeInfoResource(_) => NmToken.unsafe("NodeInfo")
      case ContactResource(_) => NmToken.unsafe("Contact")
      case DeliveryParamsResource(_) => NmToken.unsafe("DeliveryParams")
      case CuttingParamsResource(_) => NmToken.unsafe("CuttingParams")
      case FoldingParamsResource(_) => NmToken.unsafe("FoldingParams")
      case LayoutResource(_) => NmToken.unsafe("Layout")
      case ColorResource(_) => NmToken.unsafe("Color")
      case PreviewResource(_) => NmToken.unsafe("Preview")
      case DeviceResource(_) => NmToken.unsafe("Device")
      case Foreign(_, local) => local

  /** All IDREFs used by this resource payload. */
  def references: Chain[IdRef] =
    this match
      case MediaResource(m) => m.references
      case ComponentResource(c) => c.references
      case RunListResource(r) => r.references
      case NodeInfoResource(n) => n.references
      case ContactResource(_) => Chain.empty
      case DeliveryParamsResource(d) => d.references
      case CuttingParamsResource(_) => Chain.empty
      case FoldingParamsResource(_) => Chain.empty
      case LayoutResource(l) => l.references
      case ColorResource(_) => Chain.empty
      case PreviewResource(_) => Chain.empty
      case DeviceResource(_) => Chain.empty
      case Foreign(_, _) => Chain.empty
end ResourcePayload

object ResourcePayload:

  given Eq[ResourcePayload] = Eq.fromUniversalEquals

end ResourcePayload
