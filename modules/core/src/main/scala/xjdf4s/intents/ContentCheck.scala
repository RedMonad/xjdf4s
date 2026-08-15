package xjdf4s
package intents

import xjdf4s.model.elements.FileSpec
import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** `ContentCheckIntent` (§4.5 / Table 4.22): the prepress proofing and
 *  preflighting intent for a Product.
 *
 *  Both members are optional chains (Table 4.22 and `schema.xsd`
 *  `ContentCheckIntent` with `minOccurs="0" maxOccurs="unbounded"` for each):
 *  `PreflightItem*` defines the preflight rules, `ProofItem*` the customer
 *  proofs that are needed. An empty `ContentCheckIntent` is therefore valid;
 *  per Table 4.22, if no `ProofItem` exists, no customer proofs SHALL be
 *  provided — a delivery-time obligation, not a structural rule.
 *
 *  The intent pairs with the `Approval` (§5.3.1) and `Preflight` (§5.4.14)
 *  processes — Chapter 5 defines no `ContentCheck` process of its own.
 *
 *  The intent has no IDREF attributes and therefore contributes no
 *  references; the `ProofItem/@ID` values it declares are collected via
 *  `declaredIds` so that `DeliveryParams/DropItem/@ItemRef` may target a
 *  `ProofItem` (Table 6.55).
 */
final case class ContentCheckIntent(
    preflightItems: Chain[PreflightItem] = Chain.empty,
    proofItems: Chain[ProofItem] = Chain.empty
):

  /** All document-scoped `@ID`s declared by the nested `ProofItem`s
   *  (Table 4.24), in document order (§2.2.3).
   */
  def declaredIds: Chain[Id] =
    proofItems.toChain.flatMap(p => Chain.fromOption(p.id))

end ContentCheckIntent

object ContentCheckIntent:
  given Eq[ContentCheckIntent] = Eq.fromUniversalEquals

end ContentCheckIntent

/** `PreflightItem` (§4.5.1 / Table 4.23): the preflight rules for the pages
 *  in a Product. The single optional attribute `@PreflightLevel` is a closed
 *  enumeration (`Basic`, `Extended`, `Premium`); the details of each level
 *  are implementation specific.
 */
final case class PreflightItem(
    preflightLevel: Option[PreflightLevel] = None
)

object PreflightItem:
  given Eq[PreflightItem] = Eq.fromUniversalEquals

end PreflightItem

/** `ProofItem` (§4.5.2 / Table 4.24): one customer proof to be provided —
 *  its amount, color quality, page selection and the remote target the proof
 *  output SHALL be sent to.
 *
 *  Rules of the table:
 *  - `@ID` SHALL be specified if delivery of a proof is specified in
 *    `DeliveryParams` — enforced structurally: `DropItem/@ItemRef`
 *    (Table 6.55) is an IDREF and resolves only against declared `@ID`s
 *    (`TicketValidator.checkReferences`, §2.2.3); a proof without `@ID`
 *    cannot be referenced.
 *  - If `@HalfTone="true"`, the proof SHALL emulate halftone screens —
 *    device behaviour, not model-checkable.
 *  - If `@PageIndex` is not specified, all pages SHALL be proofed — reader-
 *    order semantics, not model-checkable.
 *  - `@ProofTarget` is *Deprecated in XJDF 2.1*: `FileSpec` (New in XJDF
 *    2.1) supersedes it. The field is retained so documents conforming to
 *    XJDF 2.0 decode losslessly; it carries no `@deprecated` annotation to
 *    keep the build warning-free (the same policy as the deprecated
 *    `MediaType` values, ROADMAP Appendix C).
 *
 *  `FileSpec` is the shared chapter-8 element (`model/elements`, Table 8.22)
 *  relocated in M1.4-8; this is its first reuse by a Product Intent.
 */
final case class ProofItem(
    amount: Option[Long] = None,
    colorType: Option[ProofColorType] = None,
    contract: Option[Boolean] = None,
    halfTone: Option[Boolean] = None,
    id: Option[Id] = None,
    pageIndex: Option[IntegerRange] = None,
    proofTarget: Option[Url] = None,
    fileSpec: Option[FileSpec] = None
)

object ProofItem:
  given Eq[ProofItem] = Eq.fromUniversalEquals

end ProofItem
