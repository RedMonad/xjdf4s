package xjdf4s
package intents

import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** The closed vocabulary of Product Intents modelled by this library (a subset
 *  of Table 4.2), plus the escape hatch `Extension` for foreign-namespace
 *  intents (§3.5.4). The `elementName` of a payload SHALL match `Intent/@Name`
 *  of its container.
 */
enum IntentPayload:
  case Assembly(value: AssemblingIntent)
  case Binding(value: BindingIntent)
  case Color(value: ColorIntent)
  case ContentCheck(value: ContentCheckIntent)
  case Embossing(value: EmbossingIntent)
  case Folding(value: FoldingIntent)
  case HoleMaking(value: HoleMakingIntent)
  case Laminating(value: LaminatingIntent)
  case Layout(value: LayoutIntent)
  case Media(value: MediaIntent)
  case Production(value: ProductionIntent)
  case Variable(value: VariableIntent)
  case Extension(namespace: NsPrefix, local: NmToken)

  /** The local element name this payload is serialized as. */
  def elementName: NmToken =
    this match
      case Assembly(_) => NmToken.unsafe("AssemblingIntent")
      case Binding(_) => NmToken.unsafe("BindingIntent")
      case Color(_) => NmToken.unsafe("ColorIntent")
      case ContentCheck(_) => NmToken.unsafe("ContentCheckIntent")
      case Embossing(_) => NmToken.unsafe("EmbossingIntent")
      case Folding(_) => NmToken.unsafe("FoldingIntent")
      case HoleMaking(_) => NmToken.unsafe("HoleMakingIntent")
      case Laminating(_) => NmToken.unsafe("LaminatingIntent")
      case Layout(_) => NmToken.unsafe("LayoutIntent")
      case Media(_) => NmToken.unsafe("MediaIntent")
      case Production(_) => NmToken.unsafe("ProductionIntent")
      case Variable(_) => NmToken.unsafe("VariableIntent")
      case Extension(_, local) => local

  /** All IDREFs used by this intent payload. */
  def references: Chain[IdRef] =
    this match
      case Assembly(a) => a.references
      case Binding(b) => b.references
      case Color(_) => Chain.empty
      case ContentCheck(_) => Chain.empty
      case Embossing(_) => Chain.empty
      case Folding(_) => Chain.empty
      case HoleMaking(_) => Chain.empty
      case Laminating(_) => Chain.empty
      case Layout(_) => Chain.empty
      case Media(_) => Chain.empty
      case Production(_) => Chain.empty
      case Variable(v) => v.references
      case Extension(_, _) => Chain.empty

  /** All document-scoped `@ID`s declared inside this intent payload
   *  (§2.2.3). Only `ContentCheckIntent` declares IDs today — the
   *  `ProofItem/@ID` values that `DeliveryParams/DropItem/@ItemRef` may
   *  reference (Table 6.55).
   */
  def declaredIds: Chain[Id] =
    this match
      case ContentCheck(c) => c.declaredIds
      case _               => Chain.empty
end IntentPayload

object IntentPayload:

  given Eq[IntentPayload] = Eq.fromUniversalEquals

end IntentPayload
