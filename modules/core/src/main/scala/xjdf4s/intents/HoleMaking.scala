package xjdf4s
package intents

import xjdf4s.model.elements.HolePattern
import cats.data.NonEmptyChain
import cats.kernel.Eq

/** `HoleMakingIntent` (Table 4.29, §4.8): the hole making intent for a Product.
 *  This Product Intent does not specify whether the media will be pre-drilled or
 *  the media will be drilled or punched as part of making the product.
 *
 *  The sole member is `HolePattern+` (cardinality `+`; Table 4.29 and
 *  `schema.xsd` `HoleMakingIntent` with `minOccurs="1" maxOccurs="unbounded"`),
 *  modelled as `NonEmptyChain[HolePattern]` — the "at least one HolePattern"
 *  SHALL is enforced structurally by the type. Each `HolePattern` SHALL satisfy
 *  its local rule (Table 8.30: `@Pattern` required when `@Center`/`@Extent`/
 *  `@Shape` is missing), wired through `TicketValidator.checkHoleMakingLaws`.
 *  `HolePattern` carries no IDREF attributes, so this intent contributes no
 *  references.
 */
final case class HoleMakingIntent(
    holePatterns: NonEmptyChain[HolePattern]
)

object HoleMakingIntent:
  given Eq[HoleMakingIntent] = Eq.fromUniversalEquals

end HoleMakingIntent
