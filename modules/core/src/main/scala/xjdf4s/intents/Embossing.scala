package xjdf4s
package intents

import xjdf4s.prim.*
import cats.data.NonEmptyChain
import cats.kernel.Eq

/** `EmbossingIntent` (§4.6 / Table 4.25): the embossing and/or foil stamping
 *  intent for a Product.
 *
 *  The sole member is `EmbossingItem+` (cardinality `+`; Table 4.25 and
 *  `schema.xsd` `EmbossingIntent` with `minOccurs="1" maxOccurs="unbounded"`),
 *  modelled as `NonEmptyChain[EmbossingItem]` — the "at least one
 *  EmbossingItem" requirement is enforced structurally by the type.
 *
 *  The intent has no IDREF attributes and therefore contributes no references.
 *  The inter-node SHALL of `EmbossingItem/@Separation` (Table 4.26: a `Color`
 *  resource specified for the separation SHALL have `@ColorType="DieLine"`)
 *  is a global ticket-level rule, wired through
 *  `TicketValidator.checkEmbossingColorTypes`.
 */
final case class EmbossingIntent(
    embossingItems: NonEmptyChain[EmbossingItem]
)

object EmbossingIntent:
  given Eq[EmbossingIntent] = Eq.fromUniversalEquals

/** `EmbossingItem` (Table 4.26): one embossed image — its type, direction,
 *  geometry and the foil material used.
 *
 *  `@EmbossingType` is required (Table 4.26 and `schema.xsd` `use="required"`)
 *  and is therefore a plain field, not an `Option`. `@FoilColor` is typed
 *  `NMTOKEN` from the open `NamedColor` catalog (§A.2.30, ADR-0007) — any
 *  other valid NMTOKEN remains legal.
 *
 *  Rules of the table:
 *  - SHOULD (not an error, ADR-0006): if `@FoilColorDetails` is supplied,
 *    `@FoilColor` SHOULD also be supplied.
 *  - SHALL (global, `TicketValidator.checkEmbossingColorTypes`): if a
 *    `ResourceSet/Resource/Color` element is specified for `@Separation`,
 *    the value of `Color/@ColorType` SHALL be `"DieLine"`.
 *  - PDL semantics (not model-checkable): a value of 0.0 in the PDL SHALL
 *    specify no embossing, a value of 1.0 SHALL specify embossing with full
 *    depth.
 */
final case class EmbossingItem(
    direction: Option[EmbossDirection] = None,
    embossingType: EmbossType,
    face: Option[Face] = None,
    foilColor: Option[NmToken] = None,
    foilColorDetails: Option[XjdfString] = None,
    height: Option[Double] = None,
    imageSize: Option[XYPair] = None,
    position: Option[XYPair] = None,
    separation: Option[NmToken] = None,
    toolName: Option[NmToken] = None
)

object EmbossingItem:
  given Eq[EmbossingItem] = Eq.fromUniversalEquals
