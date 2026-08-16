package xjdf4s
package model

import xjdf4s.prim.*
import cats.Show
import cats.data.{Chain, ValidatedNec}
import cats.kernel.Eq

/** The dependency-free foundation of the validation layer (ADR-0002): the
 *  vocabulary that every model node and the root validator share —
 *  `XPath`, `SeverityClass`, `IssueCode`, `Issue`, `DomainRule`,
 *  `ValidationResult` and `ValidationReport`.
 *
 *  This file has fan-out 0 within `xjdf4s`: it imports only `prim` and cats,
 *  never a domain aggregate. In M1.4-1 the types collected here were extracted
 *  from `Validation.scala` (which became `TicketValidator.scala`) and from
 *  `prim` (`XPath`, `SeverityClass`, `IssueCode`), breaking the file-dependency
 *  cycle `Validation → Product → Ticket → Patch → Validation` (N-21).
 */

/** A simple XPath expression referencing an XJDF trait (§1.3). Used to locate
 *  validation issues inside a ticket.
 */
opaque type XPath = String

object XPath:

  def apply(raw: String): XPath = raw

  extension (p: XPath) def value: String = p

  given Show[XPath] = Show.show(identity)

  given Eq[XPath] = Eq.fromUniversalEquals

end XPath

/** `Severity`: class of a notification (Table A.2.37). */
enum SeverityClass extends XjdfEnum:
  case Event, Information, Warning, Error, Fatal
  def token: NmToken = NmToken.unsafe(this.toString)

object SeverityClass extends XjdfEnumCompanion[SeverityClass]:
  val all: List[SeverityClass] = List(Event, Information, Warning, Error, Fatal)

/** A stable, machine-readable identifier of a validation finding (ADR-0003,
 *  ADR-0006). Codecs (M2) and transport layers (M4) dispatch on the code
 *  rather than on human-readable messages. Codes are NMTOKEN tokens so they
 *  serialize directly as XML/JSON attributes.
 */
opaque type IssueCode = String

object IssueCode:

  def apply(raw: String): IssueCode = raw

  /** Constructs a code from a plain Scala string; the string must be a valid NMTOKEN. */
  def unsafe(raw: String): IssueCode =
    require(NmToken.from(raw).isDefined, s"Not a valid IssueCode (NMTOKEN): '$raw'")
    raw

  extension (code: IssueCode) def value: String = code

  given Show[IssueCode] = Show.show(identity)

  given Eq[IssueCode] = Eq.fromUniversalEquals

  // --- Well-known codes used by the core validator ---------------------------
  // Adding a code here is intentional: every new SHALL rule gets a stable code
  // so downstream consumers do not parse message strings.

  /** `@Version` is not `"2.2"` (Table 3.1). */
  val UnsupportedVersion: IssueCode = unsafe("XJDF-VERSION-UNSUPPORTED")

  /** `"Product"` is combined with process type tokens (§3.1.3). */
  val ProductTokenMixed: IssueCode = unsafe("XJDF-TYPES-PRODUCT-MIXED")

  /** A duplicate `"Product"` token in `@Types` (N-36, strict policy). */
  val ProductTokenDuplicate: IssueCode = unsafe("XJDF-TYPES-PRODUCT-DUPLICATE")

  /** `@RelatedJobPartID` without `@RelatedJobID` (Table 3.1). */
  val RelatedJobPartIdWithoutJobId: IssueCode = unsafe("XJDF-RELATED-JOBPARTID-WITHOUT-JOBID")

  /** Two `ResourceSet`s clash per §3.4 (same Name/Usage/ProcessUsage with
   *  common or no `@CombinedProcessIndex` entries). */
  val ResourceSetClash: IssueCode = unsafe("RESOURCESET-CLASH")

  /** A `Resource` payload element name does not match the parent `@Name`. */
  val ResourceSetChildNameMismatch: IssueCode = unsafe("RESOURCESET-CHILD-NAME-MISMATCH")

  /** `@Status` is specified on a `ResourceSet/@Usage="Output"` (Table 6.1). */
  val ResourceStatusOnOutput: IssueCode = unsafe("RESOURCE-STATUS-ON-OUTPUT")

  /** A `@CombinedProcessIndex` value is out of bounds for `@Types`. */
  val CombinedProcessIndexOutOfBounds: IssueCode = unsafe("COMBINED-PROCESS-INDEX-OUT-OF-BOUNDS")

  /** Duplicate document-scoped `@ID` (§2.2.3). */
  val DuplicateId: IssueCode = unsafe("ID-DUPLICATE")

  /** An `IDREF` does not resolve to a declared `@ID` (§2.2.3). */
  val DanglingIdRef: IssueCode = unsafe("IDREF-DANGLING")

  /** `AuditPool` is not ordered chronologically (§3.2). */
  val AuditNotChronological: IssueCode = unsafe("AUDIT-NOT-CHRONOLOGICAL")

  /** A PartAmount/Part repeats or mismatches a parent Partition Key (§6.1.2.1). */
  val PartKeyShadowsParent: IssueCode = unsafe("PART-KEY-SHADOWS-PARENT")

  /** `Intent/@Name` does not match the payload element name (Table 4.1). */
  val IntentNameMismatch: IssueCode = unsafe("INTENT-NAME-MISMATCH")

  /** A `Notification` carries a `Milestone` but `@Class` is not `"Event"` (Table 8.49). */
  val NotificationMilestoneClass: IssueCode = unsafe("NOTIFICATION-MILESTONE-CLASS")

  /** Multiple `Comment` elements in a container share a `@Language` (Table 8.49, N-38). */
  val CommentLanguageDuplicate: IssueCode = unsafe("COMMENT-LANGUAGE-DUPLICATE")

  /** BOM: a cycle in `Product/@ChildRefs` (§3.3.1.1). */
  val BomCycle: IssueCode = unsafe("BOM-CYCLE")

  /** BOM: an unresolved `@ChildRefs` target (§3.3.1.1). */
  val BomUnresolvedChildRef: IssueCode = unsafe("BOM-UNRESOLVED-CHILDREF")

  /** BOM: a ProductList has no root product. */
  val BomNoRoot: IssueCode = unsafe("BOM-NO-ROOT")

  /** Product `@PartVersion` disagrees between a child and its referencing root (Table 3.11, N-37). */
  val PartVersionMismatch: IssueCode = unsafe("PART-VERSION-MISMATCH")

  /** A local structural law of a model node is violated (ADR-0003). */
  val LocalLawViolation: IssueCode = unsafe("LOCAL-LAW-VIOLATION")

  /** Change order `@JobID` does not match the base ticket (Table 7.56, §9.8.2.1.1). */
  val ChangeOrderJobIdMismatch: IssueCode = unsafe("CHANGE-ORDER-JOBID-MISMATCH")

  /** Change order `@JobPartID` does not match the base ticket (§9.8.2.1.1). */
  val ChangeOrderJobPartIdMismatch: IssueCode = unsafe("CHANGE-ORDER-JOBPARTID-MISMATCH")

  /** `Glue/@GluingPattern` has an odd number of entries (Table 8.29). */
  val GluePatternOdd: IssueCode = unsafe("GLUE-PATTERN-ODD")

  /** `Glue/@MeltingTemperature` specified without `Hotmelt` or `PUR` (Table 8.29). */
  val GlueMeltingTempWithoutHeat: IssueCode = unsafe("GLUE-MELTING-TEMP-WITHOUT-HEAT")

  /** `HolePattern/@Pattern` SHALL be supplied when `@Center`, `@Extent` or `@Shape` is missing (Table 8.30). */
  val HolePatternPatternRequired: IssueCode = unsafe("HOLE-PATTERN-PATTERN-REQUIRED")

  /** A `Color` resource specified for an `EmbossingItem/@Separation` does not
   *  have `@ColorType="DieLine"` (Table 4.26). */
  val EmbossingColorNotDieLine: IssueCode = unsafe("EMBOSSING-COLOR-NOT-DIELINE")

  /** A `Certification` element specifies no certification level: none of
   *  `@Claim`, `@Identifier`, `@Organization` is present (Table 8.8 with
   *  Tables 4.21/4.32/4.33/6.114; ADR-0012). */
  val CertificationLevelMissing: IssueCode = unsafe("CERTIFICATION-LEVEL-MISSING")

  /** An `IdentificationField` does not specify exactly one of `@Format`,
   *  `@Value` or the pair `@ValueFormat` + `@ValueTemplate` (Table 8.31). */
  val IdentificationFieldValueSource: IssueCode = unsafe("IDENTIFICATION-FIELD-VALUE-SOURCE")

  /** `MetadataMap/@Name` is absent from its parent `IdentificationField/@ValueTemplate` (Table 8.31). */
  val MetadataMapNameNotInParentTemplate: IssueCode =
    unsafe("METADATA-MAP-NAME-NOT-IN-PARENT-TEMPLATE")

  /** An `IdentificationField/MetadataMap/@ValueTemplate` variable is absent from the parent template (Table 8.46). */
  val MetadataMapVariableNotInParentTemplate: IssueCode =
    unsafe("METADATA-MAP-VARIABLE-NOT-IN-PARENT-TEMPLATE")

  /** An `Expr` occurs below `IdentificationField/MetadataMap`, where Table 8.46 forbids it. */
  val MetadataMapExprForbiddenInIdentificationField: IssueCode =
    unsafe("METADATA-MAP-EXPR-FORBIDDEN-IN-IDENTIFICATION-FIELD")

  /** A non-predefined `RunList/MetadataMap` variable has other than exactly one matching `Expr` (Table 8.46). */
  val MetadataMapExprResolution: IssueCode = unsafe("METADATA-MAP-EXPR-RESOLUTION")

end IssueCode

/** A validation finding: a stable machine-readable code, the severity class,
 *  the XPath location of the offending trait and a human-readable message.
 *  Issues are accumulated across the aggregate traversal
 *  (ADR-0003, ADR-0006).
 */
final case class Issue(
    severity: SeverityClass,
    location: XPath,
    message: String,
    code: Option[IssueCode] = None
)

object Issue:

  def error(location: XPath, message: String, code: Option[IssueCode] = None): Issue =
    Issue(SeverityClass.Error, location, message, code)

  def warning(location: XPath, message: String, code: Option[IssueCode] = None): Issue =
    Issue(SeverityClass.Warning, location, message, code)

  /** Convenience constructor that requires a code (new SHALL rules use this). */
  def errorC(code: IssueCode, location: XPath, message: String): Issue =
    Issue(SeverityClass.Error, location, message, Some(code))

  /** Convenience constructor that requires a code (new SHOULD/MAY rules use this). */
  def warningC(code: IssueCode, location: XPath, message: String): Issue =
    Issue(SeverityClass.Warning, location, message, Some(code))

  given Show[Issue] =
    Show.show { i =>
      val prefix = i.code.fold("")(c => s"[${c.value}] ")
      s"$prefix${i.severity.token.value} at ${i.location.value}: ${i.message}"
    }

  given Eq[Issue] = Eq.fromUniversalEquals

end Issue

/** A local structural law of a model node (ADR-0003). A `DomainRule` checks a
 *  value of type `A` at a given XPath and returns zero or more `Issue`s.
 *
 *  Unlike a bare `Boolean` predicate, a rule carries the reason, the severity,
 *  a stable `IssueCode` and the XPath of the violation — so the root validator
 *  can accumulate all findings in one traversal and report them uniformly.
 *  Global rules (ID uniqueness, §3.4 clashes, BOM integrity, chronology) live
 *  directly in `TicketValidator`; node-local SHALL/SHOULD rules are
 *  `DomainRule`s.
 *
 *  Lives in `ValidationTypes.scala` — the fan-out-0 foundation of the
 *  validation layer (ADR-0002, M1.4-1).
 */
trait DomainRule[-A]:
  def check(value: A, at: XPath): Chain[Issue]

end DomainRule

/** ADR-0002: the canonical result type of the core validator — the applicative
 *  functor that accumulates every finding of a traversal. An alias for
 *  `ValidatedNec[Issue, A]`, so call sites do not depend on the concrete cats
 *  encoding.
 */
type ValidationResult[A] = ValidatedNec[Issue, A]

/** The result of validating an XJDF ticket, split by severity (ADR-0006).
 *
 *  - `errors` — SHALL violations; they invalidate the result (`isValid`).
 *  - `warnings` — SHOULD/MAY findings; they do not turn `Valid` into
 *    `Invalid` by default, but a strict consumer can escalate them via
 *    `withWarningsAsErrors`.
 *
 *  Each issue carries a stable machine-readable `IssueCode` so downstream
 *  layers do not parse human-readable message strings.
 */
final case class ValidationReport(errors: Chain[Issue], warnings: Chain[Issue]):

  /** A report is valid when there are no errors. Warnings alone do not
   *  invalidate a ticket (ADR-0006).
   */
  def isValid: Boolean = errors.isEmpty

  /** All findings, in document/encounter order. */
  def issues: Chain[Issue] = errors ++ warnings

  /** Strict mode: escalate every warning to an error. Useful for CI gates
   *  or pipelines that treat SHOULD as SHALL. The report returned is a
   *  fresh value; the original is unchanged.
   */
  def withWarningsAsErrors: ValidationReport =
    ValidationReport(
      errors = errors ++ warnings.map(_.copy(severity = SeverityClass.Error)),
      warnings = Chain.empty
    )

  /** Escalates only the warnings carrying the given codes. */
  def escalate(codes: Set[IssueCode]): ValidationReport =
    val (escalated, remaining) = warnings.foldLeft((Chain.empty[Issue], Chain.empty[Issue])) {
      case ((up, rest), i) =>
        if i.code.exists(codes.contains) then
          (up :+ i.copy(severity = SeverityClass.Error), rest)
        else (up, rest :+ i)
    }
    copy(errors = errors ++ escalated, warnings = remaining)
end ValidationReport

object ValidationReport:

  /** An empty report: no errors, no warnings — the ticket is valid. */
  val empty: ValidationReport = ValidationReport(Chain.empty, Chain.empty)

  /** Builds a report from a flat chain of issues, partitioning by severity.
   *  `Error` and `Fatal` are errors; everything else (including
   *  `Warning`, `Information`, `Event`) is a non-invalidating warning.
   */
  def fromIssues(issues: Chain[Issue]): ValidationReport =
    val (errors, warnings) =
      issues.foldLeft((Chain.empty[Issue], Chain.empty[Issue])) {
        case ((errs, warns), i) if i.severity == SeverityClass.Error || i.severity == SeverityClass.Fatal =>
          (errs :+ i, warns)
        case ((errs, warns), i) =>
          (errs, warns :+ i)
      }
    ValidationReport(errors, warnings)
end ValidationReport
