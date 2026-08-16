package xjdf4s
package model

import xjdf4s.intents.{
  AdhesiveNote,
  AssemblingIntent,
  BindingIntent,
  ContentCheckIntent,
  HoleMakingIntent,
  IntentPayload,
  VariableIntent
}
import xjdf4s.model.elements.{
  Certification,
  Disposition,
  FileSpec,
  Glue => GlueElement,
  HolePattern => HolePatternElement,
  IdentificationField,
  MetadataMap
}
import xjdf4s.prim.*
import xjdf4s.resources.ResourcePayload
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
      checkEmbossingColorTypes,
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
        val isPipe = rs.dependents.exists(_.pipeId.isDefined)
        val perResource = rs.resources.flatMap { r =>
          val path = XPath(s"${rsPath.value}/Resource")
          checkResourceLocalLaws(r, path, isPipe)
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

  private def checkResourceLocalLaws(resource: Resource, at: XPath, parentIsPipe: Boolean): Chain[Issue] =
    val amountIssues = resource.amountPool.fold(Chain.empty[Issue]) { pool =>
      pool.toList.foldLeft(Chain.empty[Issue]) { (acc, pa) =>
        acc ++ pa.partWaste.foldLeft(Chain.empty[Issue])((w, pw) => w ++ PartWaste.law.check(pw, at))
      }
    }
    val certificationIssues = resource.specific match
      case Some(ResourcePayload.MediaResource(m)) =>
        Certification.containerLaw(m.certifications, XPath(s"$at/Media"))
      case _ => Chain.empty
    // Table 6.37: `Component/IdentificationField*` (Table 8.31, M1.6-6). The
    // other modelled containers of the element (`Device`, `Media`) gain the
    // same one-line traversal when they carry the chain.
    val identificationFieldIssues = resource.specific match
      case Some(ResourcePayload.ComponentResource(c)) =>
        val componentPath = XPath(s"$at/Component")
        IdentificationField.containerLaw(c.identificationFields, componentPath) ++
          checkIdentificationFieldMetadataMaps(c.identificationFields, componentPath)
      case _ => Chain.empty
    val runListMetadataIssues = resource.specific match
      case Some(ResourcePayload.RunListResource(r)) =>
        checkRunListMetadataMaps(r.metadataMaps, XPath(s"$at/RunList"))
      case _ => Chain.empty
    val fileSpecIssues = checkResourceFileSpecs(resource, at, parentIsPipe)
    amountIssues ++ certificationIssues ++ identificationFieldIssues ++ runListMetadataIssues ++ fileSpecIssues

  /** Applies the local FileSpec law, the parent-sensitive pipe implication and
   *  the nested Disposition law to every currently modelled FileSpec-bearing
   *  chapter-6 resource (Tables 8.22/8.23 and 3.13).
   */
  private def checkResourceFileSpecs(resource: Resource, at: XPath, parentIsPipe: Boolean): Chain[Issue] =
    resource.specific match
      case Some(ResourcePayload.CuttingParamsResource(c)) =>
        checkFileSpecs(c.fileSpecs, XPath(s"$at/CuttingParams"), parentIsPipe)
      case Some(ResourcePayload.FoldingParamsResource(f)) =>
        checkFileSpecs(f.fileSpecs, XPath(s"$at/FoldingParams"), parentIsPipe)
      case Some(ResourcePayload.LayoutResource(l)) =>
        checkFileSpecs(l.fileSpecs, XPath(s"$at/Layout"), parentIsPipe)
      case Some(ResourcePayload.PreviewResource(p)) =>
        checkFileSpecs(p.fileSpecs, XPath(s"$at/Preview"), parentIsPipe)
      case Some(ResourcePayload.RunListResource(r)) =>
        r.fileSpecs.fold(Chain.empty[Issue]) { fileSpec =>
          checkFileSpec(fileSpec, XPath(s"$at/RunList/FileSpec"), parentIsPipe)
        }
      case _ => Chain.empty

  private def checkFileSpecs(fileSpecs: Chain[FileSpec], at: XPath, parentIsPipe: Boolean): Chain[Issue] =
    fileSpecs.zipWithIndex.flatMap { (fileSpec, index) =>
      checkFileSpec(fileSpec, XPath(s"$at/FileSpec[$index]"), parentIsPipe)
    }

  private def checkFileSpec(fileSpec: FileSpec, at: XPath, parentIsPipe: Boolean): Chain[Issue] =
    val localIssues = FileSpec.law.check(fileSpec, at)
    val pipeIssues =
      if fileSpec.locationAttributesAbsent && !parentIsPipe then
        Chain.one(Issue.errorC(
          IssueCode.FileSpecLocationMissing,
          at,
          "A locationless FileSpec SHALL be referenced only by a ResourceSet pipe " +
            "(FileSpec Table 8.22; Dependent/@PipeID Table 3.13)"
        ))
      else Chain.empty
    val dispositionIssues = fileSpec.disposition.fold(Chain.empty[Issue]) { disposition =>
      dispositionLaw.check(disposition, XPath(s"$at/Disposition"))
    }
    localIssues ++ pipeIssues ++ dispositionIssues

  /** Contextual SHALLs for `IdentificationField/MetadataMap` (Tables 8.31
   *  and 8.46). They live in the root traversal because a `MetadataMap` alone
   *  cannot know which kind of parent contains it (ADR-0003).
   */
  private def checkIdentificationFieldMetadataMaps(
      fields: Chain[IdentificationField],
      at: XPath
  ): Chain[Issue] =
    fields.zipWithIndex.flatMap { (field, fieldIndex) =>
      val fieldPath = XPath(s"$at/IdentificationField[$fieldIndex]")
      field.metadataMaps.zipWithIndex.flatMap { (mapping, mapIndex) =>
        val mapPath = XPath(s"${fieldPath.value}/MetadataMap[$mapIndex]")
        val parentTemplate = field.valueTemplate.fold(Set.empty[String])(_.toList.map(_.value).toSet)
        val nameIssue =
          if parentTemplate.contains(mapping.name.value) then Chain.empty
          else Chain.one(Issue.errorC(
            IssueCode.MetadataMapNameNotInParentTemplate,
            mapPath,
            "MetadataMap/@Name SHALL be included in the parent IdentificationField/@ValueTemplate (Table 8.31)"
          ))
        val variableIssues = mapping.valueTemplate.toList
          .filterNot(token => parentTemplate.contains(token.value))
          .distinct
          .map { token =>
            Issue.errorC(
              IssueCode.MetadataMapVariableNotInParentTemplate,
              mapPath,
              s"MetadataMap/@ValueTemplate variable '${token.value}' SHALL be defined in the parent " +
                "IdentificationField/@ValueTemplate (Table 8.46)"
            )
          }
        val expressionIssue =
          if mapping.expressions.isEmpty then Chain.empty
          else Chain.one(Issue.errorC(
            IssueCode.MetadataMapExprForbiddenInIdentificationField,
            mapPath,
            "Expr SHALL NOT be specified in IdentificationField/MetadataMap (Table 8.46)"
          ))
        nameIssue ++ Chain.fromSeq(variableIssues) ++ expressionIssue
      }
    }

  /** Contextual variable resolution for `RunList/MetadataMap` (Table 8.46).
   *  Every variable not predefined by Table D.1 must have exactly one matching
   *  `Expr`; duplicates are rejected as well as missing expressions.
   */
  private def checkRunListMetadataMaps(mappings: Chain[MetadataMap], at: XPath): Chain[Issue] =
    mappings.zipWithIndex.flatMap { (mapping, mapIndex) =>
      val mapPath = XPath(s"$at/MetadataMap[$mapIndex]")
      Chain.fromSeq(
        mapping.valueTemplate.toList.map(_.value).distinct
          .filterNot(isPredefinedTemplateVariable)
          .flatMap { variable =>
            val count = mapping.expressions.toList.count(_.name.value == variable)
            Option.when(count != 1)(Issue.errorC(
              IssueCode.MetadataMapExprResolution,
              mapPath,
              s"RunList/MetadataMap variable '$variable' SHALL have exactly one matching Expr; found $count " +
                "(Table 8.46 / Table D.1)"
            ))
          }
      )
    }

  private val predefinedTemplateVariables: Set[String] =
    Set(
      "ActualAmount", "Amount", "CustomerID", "CustomerName", "Date",
      "DeviceID", "DeviceName", "EndTime", "Error", "ErrorStats",
      "ExposedMediaName", "Generated", "Input", "JobID", "JobName",
      "JobPartID", "MediaBrand", "MoonPhase", "Operator", "OperatorText",
      "PressProfileName", "PrintQuality", "ProoferProfileName", "Resolution",
      "ResolutionX", "ResolutionY", "ScreeningFamily", "StartTime", "Time",
      "TotalPagesInDoc", "Warning"
    ) ++ PartitionKey.all.map(_.attributeName)

  /** Table D.1 also defines the parameterized variable `GeneralID:XXX`. */
  private def isPredefinedTemplateVariable(variable: String): Boolean =
    predefinedTemplateVariables.contains(variable) ||
      (variable.startsWith("GeneralID:") && variable.length > "GeneralID:".length)

  private def checkIntentLocalLaws(intent: Intent, parentPath: XPath): Chain[Issue] =
    val path = XPath(s"$parentPath/Intent[@Name='${intent.name.toNmToken.value}']")
    val nameIssues = Intent.nameLaw.check(intent, path)
    val payloadIssues = intent.specific match
      case IntentPayload.Binding(b)  =>
        BindingIntent.law.check(b, path) ++ checkBindingGlueLaws(b, path) ++ checkBindingHolePatternLaws(b, path)
      case IntentPayload.Assembly(a) => checkAssemblyGlueLaws(a, path)
      case IntentPayload.ContentCheck(c) => checkContentCheckLaws(c, path)
      case IntentPayload.HoleMaking(h) => checkHoleMakingLaws(h, path)
      case IntentPayload.Color(c) => checkColorIntentCertifications(c, path)
      case IntentPayload.Media(m) =>
        Certification.containerLaw(m.certifications, path)
      case IntentPayload.Production(p) =>
        Certification.containerLaw(p.certifications, path)
      case IntentPayload.Variable(v) => VariableIntent.law.check(v, path)
      case _                         => Chain.empty
    nameIssues ++ payloadIssues

  /** Validates `Glue` elements nested inside `BindingIntent/AdhesiveNote` (Table 8.29). */
  private def checkBindingGlueLaws(b: BindingIntent, path: XPath): Chain[Issue] =
    b.details match
      case Some(an: AdhesiveNote) =>
        an.glue.fold(Chain.empty[Issue]) { g =>
          GlueElement.law(g, XPath(s"$path/AdhesiveNote/Glue"))
        }
      case _ => Chain.empty

  /** Validates `HolePattern` elements nested inside `BindingIntent/LooseBinding` (Table 8.30). */
  private def checkBindingHolePatternLaws(b: BindingIntent, path: XPath): Chain[Issue] =
    b.details match
      case Some(lb: xjdf4s.intents.LooseBinding) =>
        lb.holePattern.fold(Chain.empty[Issue]) { hp =>
          HolePatternElement.law(hp, XPath(s"$path/LooseBinding/HolePattern"))
        }
      case _ => Chain.empty

  /** Validates `HolePattern` elements nested inside `HoleMakingIntent` (Table 4.29, `HolePattern+`). */
  private def checkHoleMakingLaws(h: HoleMakingIntent, path: XPath): Chain[Issue] =
    h.holePatterns.toChain.zipWithIndex.flatMap { (hp, i) =>
      HolePatternElement.law(hp, XPath(s"$path/HolePattern[$i]"))
    }

  /** Validates the `Certification` elements nested inside
   *  `ColorIntent/SurfaceColor` (Table 4.21, `Certification*`). `ColorIntent`
   *  models the two surfaces as `front`/`back` (`maxOccurs="2"` in
   *  `schema.xsd`), so both are walked; the XPath names the surface rather
   *  than an index, matching the model shape (M1.6-1).
   */
  private def checkColorIntentCertifications(c: xjdf4s.intents.ColorIntent, path: XPath): Chain[Issue] =
    val surfaces = Chain.fromOption(c.front) ++ Chain.fromOption(c.back)
    surfaces.flatMap { sc =>
      Certification.containerLaw(
        sc.certifications,
        XPath(s"$path/SurfaceColor[@Surface='${sc.surface.token.value}']")
      )
    }

  /** Validates FileSpec and its nested Disposition below
   *  `ContentCheckIntent/ProofItem` (Tables 8.22/8.23). ProofItem has no pipe
   *  context, so a locationless FileSpec is invalid here.
   */
  private def checkContentCheckLaws(c: ContentCheckIntent, path: XPath): Chain[Issue] =
    c.proofItems.zipWithIndex.flatMap { (pi, i) =>
      pi.fileSpec.fold(Chain.empty[Issue]) { fileSpec =>
        checkFileSpec(fileSpec, XPath(s"$path/ProofItem[$i]/FileSpec"), parentIsPipe = false)
      }
    }

  /** Validates `Glue` elements nested inside `AssemblingIntent/BindIn` and `StickOn` (Table 8.29). */
  private def checkAssemblyGlueLaws(a: AssemblingIntent, path: XPath): Chain[Issue] =
    val bindInIssues = a.bindIns.zipWithIndex.flatMap { (bi, i) =>
      bi.glue.fold(Chain.empty[Issue]) { g =>
        GlueElement.law(g, XPath(s"$path/BindIn[$i]/Glue"))
      }
    }
    val stickOnIssues = a.stickOns.zipWithIndex.flatMap { (so, i) =>
      so.glue.fold(Chain.empty[Issue]) { g =>
        GlueElement.law(g, XPath(s"$path/StickOn[$i]/Glue"))
      }
    }
    bindInIssues ++ stickOnIssues

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

  /** Table 4.26 (`EmbossingItem/@Separation`): if a
   *  `ResourceSet/Resource/Color` element is specified for a separation that an
   *  `EmbossingItem` names, the value of `Color/@ColorType` SHALL be
   *  `"DieLine"`.
   *
   *  Interpretation (documented in SPEC-COVERAGE.md):
   *  - a `Color` resource is "specified for this separation" when at least one
   *    of its `Part` elements carries `@Separation` with that value (`Color`
   *    resources are partitioned by `Part/@Separation`, Table 6.27); a `Color`
   *    without such a Part is a generic colorant and is not matched;
   *  - the SHALL is read strictly: `@ColorType` must be present and equal to
   *    `DieLine` — an absent `@ColorType` is not `DieLine`.
   */
  private def checkEmbossingColorTypes(ticket: XJDF): Chain[Issue] =
    val separations: List[NmToken] =
      ticket.productList
        .fold(List.empty[NmToken]) { pl =>
          pl.products.toChain.toList.flatMap { p =>
            p.intents.toList.flatMap { i =>
              i.specific match
                case IntentPayload.Embossing(e) =>
                  e.embossingItems.toChain.toList.flatMap(_.separation.toList)
                case _ => Nil
            }
          }
        }
        .distinct
    if separations.isEmpty then Chain.empty
    else
      val violations = ticket.resourceSets.toList.flatMap { rs =>
        rs.resources.toList.flatMap { r =>
          r.specific match
            case Some(ResourcePayload.ColorResource(c)) =>
              r.parts.toList
                .flatMap(p => p.separation.toList)
                .filter(s => separations.contains(s))
                .distinct
                .filter(_ => c.colorType != Some(ColorType.DieLine))
                .map { s =>
                  val actual = c.colorType.fold("no @ColorType")(t => s"@ColorType='${t.token.value}'")
                  Issue.errorC(
                    IssueCode.EmbossingColorNotDieLine,
                    XPath(s"/XJDF/ResourceSet[@Name='${rs.name.toNmToken.value}']/Resource/Color"),
                    s"Color specified for embossing separation '${s.value}' SHALL have " +
                      s"@ColorType=\"DieLine\", found $actual (Table 4.26)"
                  )
                }
            case _ => Nil
        }
      }
      Chain.fromSeq(violations)

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
   *  M1.4-8 relocates the data type only; validation behavior remains here unchanged.
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
