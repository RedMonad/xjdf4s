package xjdf4s
package intents

import xjdf4s.prim.*
import cats.data.NonEmptyChain
import cats.kernel.Eq

/** `LaminatingIntent` (§4.9 / Table 4.30): the laminating intent for a Product.
 *
 *  `@Surface` is required and contains one or more values from `Side`
 *  (Table A.39). The XSD declares a required list whose item type is `Side`;
 *  `NonEmptyChain[Side]` additionally captures the prose's "surface or
 *  surfaces" requirement without a runtime "at least one" check.
 *  `@Texture` is an open `NMTOKEN` catalog (Table A.80), while `@Temperature`
 *  is the closed `Hot`/`Cold` vocabulary declared by Table 4.30.
 *  The intent has no IDREF attributes and therefore contributes no references.
 */
final case class LaminatingIntent(
    surface: NonEmptyChain[Side],
    temperature: Option[LaminatingTemperature] = None,
    texture: Option[NmToken] = None,
    thickness: Option[Microns] = None
)

object LaminatingIntent:
  given Eq[LaminatingIntent] = Eq.fromUniversalEquals

end LaminatingIntent
