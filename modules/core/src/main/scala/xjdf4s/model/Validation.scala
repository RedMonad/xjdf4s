package xjdf4s
package model

import xjdf4s.prim.*
import cats.Show
import cats.data.{Chain, Validated, ValidatedNec}
import cats.kernel.Eq
import cats.syntax.all.*

/** A validation finding: the severity class, the XPath location of the trait
 *  and a human-readable message. Issues are accumulated in a
 *  `ValidatedNec[Issue, A]` — the applicative functor of collected errors.
 */
final case class Issue(
    severity: SeverityClass,
    location: XPath,
    message: String
)

object Issue:

  def error(location: XPath, message: String): Issue =
    Issue(SeverityClass.Error, location, message)

  def warning(location: XPath, message: String): Issue =
    Issue(SeverityClass.Warning, location, message)

  given Show[Issue] =
    Show.show(i => s"${i.severity.token.value} at ${i.location.value}: ${i.message}")

  given Eq[Issue] = Eq.fromUniversalEquals

end Issue

/** Structural validation of an XJDF ticket against the requirements of the
 *  specification. Every check is a `ValidatedNec[Issue, Unit]`; combining them
 *  accumulates *all* violations instead of failing fast.
 */
object TicketValidator:

  def validate(ticket: XJDF): ValidatedNec[Issue, Unit] =
    val checks = Chain(
      checkVersion(ticket),
      checkTypes(ticket),
      checkRelatedIds(ticket),
      checkResourceSetKeys(ticket),
      checkResourceSetChildren(ticket),
      checkResourceSetStatuses(ticket),
      checkCombinedProcessIndices(ticket),
      checkIdUniqueness(ticket),
      checkReferences(ticket),
      checkAuditChronology(ticket),
      checkPartAmountKeys(ticket),
      checkIntentLawfulness(ticket)
    )
    checks.combineAll

  /** `@Version` SHALL be `"2.2"` (Table 3.1). */
  private def checkVersion(ticket: XJDF): ValidatedNec[Issue, Unit] =
    Validated.condNec(
      ticket.version == XjdfVersion.V2_2,
      (),
      Issue.error(XPath("/XJDF/@Version"), s"Unsupported XJDF version: ${ticket.version.value}")
    )

  /** §3.1.3: `@Types` of process XJDF SHALL NOT contain `"Product"` if any
   *  additional process type tokens are present.
   */
  private def checkTypes(ticket: XJDF): ValidatedNec[Issue, Unit] =
    val hasProduct = ticket.types.exists(_ == ProcessType.Product)
    val hasProcesses = ticket.types.exists(_ != ProcessType.Product)
    Validated.condNec(
      !(hasProduct && hasProcesses),
      (),
      Issue.error(XPath("/XJDF/@Types"), "\"Product\" SHALL NOT be combined with process type tokens")
    )

  /** `@RelatedJobPartID` SHALL NOT be specified unless `@RelatedJobID` is (Table 3.1). */
  private def checkRelatedIds(ticket: XJDF): ValidatedNec[Issue, Unit] =
    Validated.condNec(
      ticket.relatedJobId.isDefined || ticket.relatedJobPartId.isEmpty,
      (),
      Issue.error(XPath("/XJDF/@RelatedJobPartID"), "@RelatedJobPartID SHALL NOT be specified without @RelatedJobID")
    )

  /** §3.4: ResourceSets with the same key SHALL NOT be specified. */
  private def checkResourceSetKeys(ticket: XJDF): ValidatedNec[Issue, Unit] =
    val duplicates = ticket.resourceSets.toList
      .groupBy(_.key)
      .collect { case (key, sets) if sets.size > 1 => key }
    Validated.condNec(
      duplicates.isEmpty,
      (),
      Issue.error(
        XPath("/XJDF/ResourceSet"),
        s"Duplicate ResourceSet keys: ${duplicates.map(Show[ResourceSetKey].show).mkString("; ")}"
      )
    )

  /** `ResourceSet/@Name` SHALL match the specific resources of its children (§3.4). */
  private def checkResourceSetChildren(ticket: XJDF): ValidatedNec[Issue, Unit] =
    val bad = ticket.resourceSets.toList.filterNot(_.hasLawfulChildren)
    Validated.condNec(
      bad.isEmpty,
      (),
      Issue.error(
        XPath("/XJDF/ResourceSet"),
        s"ResourceSet children do not match @Name: ${bad.map(_.name.toNmToken.value).mkString(", ")}"
      )
    )

  /** `Resource/@Status` SHALL NOT be specified for `@Usage="Output"` (Table 6.1). */
  private def checkResourceSetStatuses(ticket: XJDF): ValidatedNec[Issue, Unit] =
    val bad = ticket.resourceSets.toList.filterNot(_.hasLawfulStatuses)
    Validated.condNec(
      bad.isEmpty,
      (),
      Issue.error(
        XPath("/XJDF/ResourceSet"),
        s"Resource @Status specified for output ResourceSet: ${bad.map(_.name.toNmToken.value).mkString(", ")}"
      )
    )

  /** `@CombinedProcessIndex` SHALL reference existing positions of `@Types` (§3.4). */
  private def checkCombinedProcessIndices(ticket: XJDF): ValidatedNec[Issue, Unit] =
    val size = ticket.types.toChain.size.toInt
    val bad = ticket.resourceSets.toList.flatMap: rs =>
      rs.combinedProcessIndex.toList.flatMap: indices =>
        indices.toChain.toList.filter(_.value >= size).map(i => s"${rs.name.toNmToken.value}@${i.value}")
    Validated.condNec(
      bad.isEmpty,
      (),
      Issue.error(
        XPath("/XJDF/ResourceSet/@CombinedProcessIndex"),
        s"Process index out of bounds: ${bad.mkString(", ")}"
      )
    )

  /** `@ID` SHALL be unique within the scope of the XJDF document (§2.2.3). */
  private def checkIdUniqueness(ticket: XJDF): ValidatedNec[Issue, Unit] =
    val duplicates = ticket.declaredIds.toList.groupBy(_.value).collect { case (v, ids) if ids.size > 1 => v }
    Validated.condNec(
      duplicates.isEmpty,
      (),
      Issue.error(XPath("/XJDF"), s"Duplicate @ID values: ${duplicates.mkString(", ")}")
    )

  /** Every `@IDREF` SHALL reference an existing `@ID` (§2.2.3). */
  private def checkReferences(ticket: XJDF): ValidatedNec[Issue, Unit] =
    val known = ticket.declaredIds.toList.map(_.value).toSet
    val dangling = ticket.references.toList.filterNot(r => known.contains(r.value)).distinct
    Validated.condNec(
      dangling.isEmpty,
      (),
      Issue.error(XPath("/XJDF"), s"Dangling IDREFs: ${dangling.map(_.value).mkString(", ")}")
    )

  /** AuditPool entries SHALL be ordered chronologically from oldest to newest (§3.2). */
  private def checkAuditChronology(ticket: XJDF): ValidatedNec[Issue, Unit] =
    val ok = ticket.auditPool.forall(_.isChronological)
    Validated.condNec(
      ok,
      (),
      Issue.error(XPath("/XJDF/AuditPool"), "AuditPool is not ordered chronologically")
    )

  /** §6.1.2.1: PartAmount/Part SHALL NOT include Partition Keys that are
   *  already uniquely specified in the parent Resource/Part.
   */
  private def checkPartAmountKeys(ticket: XJDF): ValidatedNec[Issue, Unit] =
    val violations = ticket.resourceSets.toList.flatMap: rs =>
      rs.resources.toList.flatMap: r =>
        val parentKeys = r.parts.size match
          case 1 => r.parts.headOption.toList.flatMap(_.keys)
          case _ => Nil
        r.amountPool.toList.flatMap: pool =>
          pool.toList.flatMap: pa =>
            val overlap = pa.part.keys.filter(parentKeys.contains)
            overlap.map(k => s"${rs.name.toNmToken.value}/@${k.toString}")
    Validated.condNec(
      violations.isEmpty,
      (),
      Issue.error(
        XPath("/XJDF/ResourceSet/Resource/AmountPool"),
        s"PartAmount keys shadow parent Part keys: ${violations.mkString(", ")}"
      )
    )

  /** `Intent/@Name` SHALL match the payload element name, and the payloads SHALL be lawful. */
  private def checkIntentLawfulness(ticket: XJDF): ValidatedNec[Issue, Unit] =
    val bad = ticket.productList.toList.flatMap(_.products.toChain.toList).flatMap: p =>
      p.intents.toList.filterNot(_.isLawful).map(i => s"${p.productType.fold("?")(_.value)}/${i.name.toNmToken.value}")
    Validated.condNec(
      bad.isEmpty,
      (),
      Issue.error(XPath("/XJDF/ProductList/Intent"), s"Intent @Name does not match its payload: ${bad.mkString(", ")}")
    )
end TicketValidator
