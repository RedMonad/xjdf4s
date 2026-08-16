package xjdf4s
package intents

import xjdf4s.model.{DomainRule, Issue, IssueCode, XPath}
import xjdf4s.prim.*
import cats.data.{Chain, NonEmptyChain}
import cats.kernel.Eq

/** `ShapeCuttingIntent` (§4.13 / Table 4.34): finishing of Products with
 *  irregular shapes, including die cutting and envelope windows.
 *
 *  The sole member is `ShapeCut+` (cardinality `+`; Table 4.34 and
 *  `schema.xsd` `minOccurs="1" maxOccurs="unbounded"`), modelled as
 *  `NonEmptyChain[ShapeCut]`. Neither the intent nor its children declare an
 *  ID or IDREF, but `references` still traverses every child so later additive
 *  extensions cannot be silently skipped by `IntentPayload`.
 */
final case class ShapeCuttingIntent(
    shapeCuts: NonEmptyChain[ShapeCut]
):

  /** All document-scoped IDREFs of the nested `ShapeCut` elements. */
  def references: Chain[IdRef] = shapeCuts.toChain.flatMap(_.references)

end ShapeCuttingIntent

object ShapeCuttingIntent:
  given Eq[ShapeCuttingIntent] = Eq.fromUniversalEquals

/** `ShapeCut` (§4.13.1 / Table 4.35): one line, path or bounded shape to cut.
 *
 *  Exact table-to-type mapping:
 *  - `@CutBox?` -> `Option[Rectangle]`;
 *  - `@CutDepth?` -> `Option[CutDepth]`;
 *  - `@CutOut?` -> `Option[Boolean]`;
 *  - `@CutPath?` -> `Option[PDFPath]`;
 *  - `@CutType?` -> `Option[CutType]`;
 *  - required `@ShapeType` -> `ShapeCutType`;
 *  - `@ShapeTypeDetails?` -> `Option[XjdfString]`.
 *
 *  Table 4.35 says that a `Path` SHOULD provide additional details in either
 *  `@CutPath` or `@ShapeTypeDetails`. `law` reports the missing details as a
 *  warning (ADR-0006), never as an unconditional error. The two SHALL clauses
 *  of `@CutOut` define production semantics: `true` removes the inside and
 *  `false` removes the outside. Both values are therefore lawful document
 *  states rather than inputs to a validation predicate.
 */
final case class ShapeCut(
    cutBox: Option[Rectangle] = None,
    cutDepth: Option[CutDepth] = None,
    cutOut: Option[Boolean] = None,
    cutPath: Option[PDFPath] = None,
    cutType: Option[CutType] = None,
    shapeType: ShapeCutType,
    shapeTypeDetails: Option[XjdfString] = None
):

  /** Table 4.35 declares no ID or IDREF attributes. */
  def references: Chain[IdRef] = Chain.empty

end ShapeCut

object ShapeCut:

  /** Table 4.35: irregular paths SHOULD carry a path or site-defined details.
   *  Explicitly invoked from `TicketValidator.checkShapeCuttingLaws`.
   */
  val law: DomainRule[ShapeCut] =
    (value: ShapeCut, at: XPath) =>
      if value.shapeType == ShapeCutType.Path && value.cutPath.isEmpty && value.shapeTypeDetails.isEmpty then
        Chain.one(
          Issue.warningC(
            IssueCode.ShapeCutPathDetailsRecommended,
            at,
            "@ShapeType='Path' SHOULD provide @CutPath or @ShapeTypeDetails (Table 4.35)"
          )
        )
      else Chain.empty

  given Eq[ShapeCut] = Eq.fromUniversalEquals

end ShapeCut
