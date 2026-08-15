package xjdf4s
package model

import xjdf4s.intents.{BindingIntent, IntentPayload, VariableIntent}
import xjdf4s.prim.*
import cats.Show
import cats.data.{Chain, NonEmptyChain, Validated}

/** Structural validation of an XJDF ticket against the requirements of the
 *  specification. The validator is the single root traversal: global
 *  inter-node rules (ID uniqueness, §3.4 clashes, BOM integrity, audit
 *  chronology) live here; node-local rules are `DomainRule`s invoked from
 *  `checkLocalLaws`.
 *
 *  This file is the aggregation root of the validation layer (ADR-0002,
 *  M1.4-1): it depends on the whole domain model, while the model depends
 *  only on the fan-out-0 foundation `ValidationTypes`.
 *
 *  Errors and warnings are separated by `ValidationReport` (ADR-0006); the
 *  legacy entry point `validate` treats any finding as invalid and is
 *  retained for call sites that predate M1.3-5, including the
 *  `XJDF.validate` extension defined below.
 */
object TicketValidator:

  /** Validates the ticket and returns a `ValidationReport` separating errors
   *  (SHALL violations) from warnings (SHOULD/MAY findings). This is the
   *  primary entry point from M1.3-5 on (ADR-0006).
   */
  def validateReport(ticket: XJDF): ValidationReport =
    ValidationReport.fromIssues(allIssues(ticket))

  /** Legacy entry point: treats every finding — error or warning — as a
   *  validation failure. Retained for call sites that predate M1.3-5 and for
   *  the `XJDF.validate` extension (ADR-0006, ADR-0002).
   */
  def validate(ticket: XJDF): ValidationResult[Unit] =
    NonEmptyChain.fromChain(allIssues(ticket)) match
      case Some(nec) => Validated.invalid(nec)
      case None      => Validated.valid(())

  /** Runs every check and returns the flat chain of findings in traversal
   *  order. Each check is a `XJDF => Chain[Issue]` — the sequence is the
   *  single source of truth for both `validate` and `validateReport`.
   */
  private def allIssues(ticket: XJDF): Chain[Issue] =
    Chain(
      checkVersion,
      checkTypes,
      checkRelatedIds,
      checkResourceSetKeys,
      checkCombinedProcessIndices,
      checkIdUniqueness,
      checkReferences,
      checkBomIntegrity,
      checkPartVersion,
      checkAuditChronology,
      checkPartAmountKeys,
      checkLocalLaws
    ).flatMap(_(ticket))

  // --- Local-law bus (ADR-0003) --------------------------------------------

  /** Walks the aggregate and invokes the registered `DomainRule`s of every
   *  node. Each rule gets the XPath of the node it validates, so findings
   *  carry a precise location rather than a single aggregate message.
   *
   *  Grep-proofness (N-18): every `DomainRule[T]` is reached here. The
   *  registry test in `TicketLaws` enumerates the types that carry local
   *  laws and asserts they are reached by this traversal.
   */
  private def checkLocalLaws(ticket: XJDF): Chain[Issue] =
    val resourceIssues: Chain[Issue] =
      ticket.resourceSets.flatMap { rs =>
        val rsPath = XPath(s"/XJDF/ResourceSet[@Name='${rs.name.toNmToken.value}']")
        val rsRuleIssues = ResourceSetLaw.children.check(rs, rsPath) ++
          ResourceSetLaw.statuses.check(rs, rsPath)
        val perResource = rs.resources.flatMap { r =>
          val path = XPath(s"${rsPath.value}/Resource")
          checkResourceLocalLaws(r, path)
        }
        rsRuleIssues ++ perResource
      }

    val productIssues: Chain[Issue] =
      ticket.productList.fold(Chain.empty[Issue]) { pl =>
        pl.products.toChain.flatMap { p =>
          val path = p.id.fold(XPath("/XJDF/ProductList/Product"))(id =>
            XPath(s"/XJDF/ProductList/Product[@ID='${id.value}']"))
          Product.amountsLaw.check(p, path) ++
            p.intents.flatMap(checkIntentLocalLaws(_, path))
        }
      }

    val notificationIssues: Chain[Issue] =
      ticket.auditPool.fold(Chain.empty[Issue]) { pool =>
        pool.toNonEmptyChain.toChain.zipWithIndex.flatMap {
          case (Audit.Notified(_, n), idx) =>
            Notification.law.check(n, XPath(s"/XJDF/AuditPool/AuditNotification[$idx]"))
          case _ => Chain.empty
        }
      }

    resourceIssues ++ productIssues ++ notificationIssues

  private def checkResourceLocalLaws(resource: Resource, at: XPath): Chain[Issue] =
    resource.amountPool.fold(Chain.empty[Issue]) { pool =>
      pool.toList.foldLeft(Chain.empty[Issue]) { (acc, pa) =>
        acc ++ pa.partWaste.foldLeft(Chain.empty[Issue])((w, pw) => w ++ PartWaste.law.check(pw, at))
      }
    }
    // Note: Disposition (Table 8.23) is a child of FileSpec inside chapter-6
    // resources; once resources carrying FileSpec are implemented (M1.6/M3),
    // the traversal extends here with `TicketValidator.dispositionLaw.check(d, at)`.
    // The rule is defined as `TicketValidator.dispositionLaw` to keep `prim`
    // free of validation dependencies (ADR-0002, M1.4-1); the hook stays a
    // one-line addition.

  private def checkIntentLocalLaws(intent: Intent, parentPath: XPath): Chain[Issue] =
    val path = XPath(s"$parentPath/Intent[@Name='${intent.name.toNmToken.value}']")
    val nameIssues = Intent.nameLaw.check(intent, path)
    val payloadIssues = intent.specific match
      case IntentPayload.Binding(b)  => BindingIntent.law.check(b, path)
      case IntentPayload.Variable(v) => VariableIntent.law.check(v, path)
      case _                         => Chain.empty
    nameIssues ++ payloadIssues

  // --- Global checks -------------------------------------------------------

  /** `@Version` SHALL be `"2.2"` (Table 3.1). */
  private def checkVersion(ticket: XJDF): Chain[Issue] =
    issueUnless(
      ticket.version == XjdfVersion.V2_2,
      XPath("/XJDF/@Version"),
      s"Unsupported XJDF version: ${ticket.version.value}",
      IssueCode.UnsupportedVersion
    )

  /** §3.1.3: `@Types` of process XJDF SHALL NOT contain `"Product"` if any
   *  additional process type tokens are present.
   *
   *  N-36 (strict policy, documented in SPEC-COVERAGE.md): a *duplicate*
   *  `"Product"` token is also rejected. The specification wording is
   *  "additional process type tokens"; treating a duplicate as additional is
   *  a deliberate, documented interpretation (ROADMAP N-36, Appendix C).
   */
  private def checkTypes(ticket: XJDF): Chain[Issue] =
    val tokens = ticket.types.toChain.toList
    val productCount = tokens.count(_ == ProcessType.Product)
    val hasProcesses = tokens.exists(_ != ProcessType.Product)
    val mixed =
      if productCount > 0 && hasProcesses then
        Chain.one(Issue.errorC(
          IssueCode.ProductTokenMixed,
          XPath("/XJDF/@Types"),
          "\"Product\" SHALL NOT be combined with process type tokens (§3.1.3)"
        ))
      else Chain.empty
    val dup =
      if productCount > 1 then
        Chain.one(Issue.errorC(
          IssueCode.ProductTokenDuplicate,
          XPath("/XJDF/@Types"),
          "Duplicate \"Product\" token in @Types is rejected by the strict policy (§3.1.3, N-36)"
        ))
      else Chain.empty
    mixed ++ dup

  /** `@RelatedJobPartID` SHALL NOT be specified unless `@RelatedJobID` is (Table 3.1). */
  private def checkRelatedIds(ticket: XJDF): Chain[Issue] =
    issueUnless(
      ticket.relatedJobId.isDefined || ticket.relatedJobPartId.isEmpty,
      XPath("/XJDF/@RelatedJobPartID"),
      "@RelatedJobPartID SHALL NOT be specified without @RelatedJobID",
      IssueCode.RelatedJobPartIdWithoutJobId
    )

  /** §3.4: two ResourceSets clash when `@Name`/`@Usage`/`@ProcessUsage` are
   *  equal AND their `@CombinedProcessIndex` lists have common entries, or
   *  either list is absent ("no entries" applies to all processes). The
   *  pairwise comparison uses the shared `ResourceSet.clashesWith` predicate
   *  (M1.1-2) instead of exact-key `groupBy`, catching partial CPI overlap
   *  and the "no-CPI vs CPI" case (N-16).
   */
  private def checkResourceSetKeys(ticket: XJDF): Chain[Issue] =
    val sets = ticket.resourceSets.toList
    val clashes = sets.indices.flatMap { i =>
      ((i + 1) until sets.size).flatMap { j =>
        val a = sets(i)
        val b = sets(j)
        Option.when(ResourceSet.clashesWith(a, b))((a, b))
      }
    }
    Chain.fromSeq(clashes).map { case (a, _) =>
      Issue.errorC(
        IssueCode.ResourceSetClash,
        XPath("/XJDF/ResourceSet"),
        s"ResourceSet '${a.name.toNmToken.value}' clashes with a sibling " +
          "(same @Name/@Usage/@ProcessUsage with common or no @CombinedProcessIndex entries, §3.4)"
      )
    }

  /** `@CombinedProcessIndex` SHALL reference existing positions of `@Types` (§3.4). */
  private def checkCombinedProcessIndices(ticket: XJDF): Chain[Issue] =
    val size = ticket.types.toChain.size.toInt
    val bad = ticket.resourceSets.toList.flatMap { rs =>
      rs.combinedProcessIndex.toList.flatMap { indices =>
        indices.toChain.toList.filter(_.value >= size).map(i => s"${rs.name.toNmToken.value}@${i.value}")
      }
    }
    issueUnless(
      bad.isEmpty,
      XPath("/XJDF/ResourceSet/@CombinedProcessIndex"),
      s"Process index out of bounds: ${bad.mkString(", ")}",
      IssueCode.CombinedProcessIndexOutOfBounds
    )

  /** `@ID` SHALL be unique within the scope of the XJDF document (§2.2.3). */
  private def checkIdUniqueness(ticket: XJDF): Chain[Issue] =
    val duplicates =
      ticket.declaredIds.toList.groupBy(_.value).collect { case (v, ids) if ids.size > 1 => v }
    issueUnless(
      duplicates.isEmpty,
      XPath("/XJDF"),
      s"Duplicate @ID values: ${duplicates.mkString(", ")}",
      IssueCode.DuplicateId
    )

  /** Every `@IDREF` SHALL reference an existing `@ID` (§2.2.3). */
  private def checkReferences(ticket: XJDF): Chain[Issue] =
    val known = ticket.declaredIds.toList.map(_.value).toSet
    val dangling = ticket.references.toList.filterNot(r => known.contains(r.value)).distinct
    issueUnless(
      dangling.isEmpty,
      XPath("/XJDF"),
      s"Dangling IDREFs: ${dangling.map(_.value).mkString(", ")}",
      IssueCode.DanglingIdRef
    )

  /** AuditPool entries SHALL be ordered chronologically from oldest to newest (§3.2). */
  private def checkAuditChronology(ticket: XJDF): Chain[Issue] =
    issueUnless(
      ticket.auditPool.forall(_.isChronological),
      XPath("/XJDF/AuditPool"),
      "AuditPool is not ordered chronologically",
      IssueCode.AuditNotChronological
    )

  /** N-19: BOM integrity — `Bom.fromProductList` SHALL succeed. This detects
   *  cycles and unresolved `@ChildRefs` at the root validator entry point
   *  (`/XJDF/ProductList`). Duplicate `Product/@ID` values are detected
   *  separately by `checkIdUniqueness`, so they are not masked as a BOM
   *  cycle here.
   */
  private def checkBomIntegrity(ticket: XJDF): Chain[Issue] =
    ticket.productList match
      case None => Chain.empty
      case Some(pl) =>
        Bom.fromProductList(pl) match
          case Left(issue) =>
            val retagged = issue.code match
              case Some(_) => issue
              case None =>
                val code = issue.message match
                  case msg if msg.startsWith("Cycle")         => IssueCode.BomCycle
                  case msg if msg.startsWith("Unresolved")    => IssueCode.BomUnresolvedChildRef
                  case msg if msg.contains("at least one root") => IssueCode.BomNoRoot
                  case _                                      => IssueCode.LocalLawViolation
                issue.copy(code = Some(code))
            Chain.one(retagged)
          case Right(_) => Chain.empty

  /** N-37 / Table 3.11 Sheet 2: if `@PartVersion` is specified for a child
   *  product, every root product that references that child (directly or
   *  transitively) SHALL also contain `@PartVersion` with the same value.
   *
   *  The check is conservative: when neither the child nor the root carries
   *  `@PartVersion`, the ticket is accepted. A child without `@PartVersion`
   *  referenced by a root that does carry one is also accepted (the rule
   *  only fires when the *child* specifies one).
   */
  private def checkPartVersion(ticket: XJDF): Chain[Issue] =
    ticket.productList match
      case None => Chain.empty
      case Some(pl) =>
        val byId: Map[String, Product] =
          pl.products.toChain.toList.flatMap(p => p.id.map(_.value -> p)).toMap
        val issues = pl.products.toChain.toList.flatMap { root =>
          if !root.isRoot then Nil
          else
            collectRefs(root, byId, Set.empty).flatMap { childId =>
              byId.get(childId) match
                case None => Nil // unresolved refs are BOM's job (N-19)
                case Some(child) =>
                  (child.partVersion, root.partVersion) match
                    case (Some(cv), Some(rv)) if cv != rv =>
                      List(Issue.errorC(
                        IssueCode.PartVersionMismatch,
                        XPath(s"/XJDF/ProductList/Product[@ID='$childId']"),
                        s"Child product @PartVersion='${cv.value}' does not match " +
                          s"referencing root @PartVersion='${rv.value}' (Table 3.11)"
                      ))
                    case (Some(cv), None) =>
                      val rootId = root.id.fold("?")(_.value)
                      List(Issue.errorC(
                        IssueCode.PartVersionMismatch,
                        XPath(s"/XJDF/ProductList/Product[@ID='$rootId']"),
                        s"Root referencing child @PartVersion='${cv.value}' SHALL also " +
                          "contain @PartVersion with the same value (Table 3.11)"
                      ))
                    case _ => Nil
            }
        }
        Chain.fromSeq(issues)

  /** Transitive closure of `@ChildRefs` reachable from `p`, skipping already
   *  visited IDs to avoid infinite recursion on cycles (cycles are reported
   *  separately by `checkBomIntegrity`).
   */
  private def collectRefs(
      p: Product,
      byId: Map[String, Product],
      visited: Set[String]
  ): List[String] =
    p.references.toList.map(_.value).distinct.flatMap { ref =>
      if visited.contains(ref) then List(ref)
      else
        byId.get(ref) match
          case None        => List(ref)
          case Some(child) => ref :: collectRefs(child, byId, visited + ref)
    }.distinct

  /** Every distinct value of a Partition Key across the parent Resource/Part
   *  elements (Table 6.3 / §6.1.2.1).
   */
  private def parentValues(parts: Chain[Part], key: PartitionKey): List[PartitionValue] =
    parts.toList.flatMap(_.valueOf(key)).distinct

  /** §6.1.2.1 (Table 6.3, `Part*`), both clauses:
   *
   *   1. a PartAmount/Part SHALL NOT include a Partition Key that the parent
   *      Resource/Part elements already specify *uniquely* (a single value);
   *   2. if a PartAmount/Part repeats a Partition Key that the parent specifies
   *      with more than one value, the child value SHALL match one of the parent
   *      values.
   */
  private def checkPartAmountKeys(ticket: XJDF): Chain[Issue] =
    val violations = ticket.resourceSets.toList.flatMap { rs =>
      rs.resources.toList.flatMap { r =>
        r.amountPool.toList.flatMap { pool =>
          pool.toList.flatMap { pa =>
            pa.parts.toList.flatMap { child =>
              child.keys.flatMap { key =>
                parentValues(r.parts, key) match
                  case Nil => Nil
                  case _ :: Nil =>
                    List(
                      s"${rs.name.toNmToken.value}/@${key.attributeName} overrides a Partition Key " +
                        "already uniquely specified by the parent Resource/Part"
                    )
                  case parents =>
                    child.valueOf(key) match
                      case Some(v) if !parents.contains(v) =>
                        List(
                          s"${rs.name.toNmToken.value}/@${key.attributeName}=${Show[PartitionValue].show(v)} " +
                            "does not match a parent Resource/Part value"
                        )
                      case _ => Nil
              }
            }
          }
        }
      }
    }
    issueUnless(
      violations.isEmpty,
      XPath("/XJDF/ResourceSet/Resource/AmountPool"),
      s"PartAmount keys shadow parent Part keys: ${violations.mkString(", ")}",
      IssueCode.PartKeyShadowsParent
    )

  /** Helper: emits one error issue with the given code when `condition` is false. */
  private def issueUnless(
      condition: Boolean,
      location: XPath,
      message: String,
      code: IssueCode
  ): Chain[Issue] =
    if condition then Chain.empty
    else Chain.one(Issue.errorC(code, location, message))

  /** Table 8.23: `Disposition/@MinDuration` and `@Until` are mutually exclusive.
   *  Defined here rather than on the `prim.Disposition` companion to avoid a
   *  dependency from `prim` onto the validation layer (ADR-0002, M1.4-1).
   */
  def dispositionLaw: DomainRule[Disposition] =
    (d: Disposition, at: XPath) =>
      if d.minDuration.isDefined && d.until.isDefined then
        Chain.one(Issue.errorC(
          IssueCode.LocalLawViolation,
          at,
          "@MinDuration and @Until are mutually exclusive (Table 8.23)"
        ))
      else Chain.empty

end TicketValidator

extension (ticket: XJDF)

  /** Validates this ticket against the structural requirements of the
   *  specification (uniqueness of ResourceSet keys, index bounds, ID/IDREF
   *  consistency, `@Types` rules, audit chronology, …). All violations are
   *  accumulated — the applicative functor of errors.
   *
   *  This is the legacy entry point: every finding (error or warning) is
   *  treated as invalid. Prefer `validateReport` for the errors/warnings
   *  split (ADR-0006).
   *
   *  M1.4-1 (ADR-0002): moved from a member of `XJDF` to an extension method
   *  in `TicketValidator.scala` so `Ticket.scala` does not depend on the
   *  validator. Source-compatible wherever `xjdf4s.model.*` is imported.
   */
  def validate: ValidationResult[Unit] =
    TicketValidator.validate(ticket)

  /** Validates this ticket and returns a `ValidationReport` separating errors
   *  (SHALL violations) from warnings (SHOULD/MAY findings) (ADR-0006).
   *
   *  M1.4-1 (ADR-0002): moved from `XJDF` to an extension method, see
   *  `validate` above.
   */
  def validateReport: ValidationReport =
    TicketValidator.validateReport(ticket)
