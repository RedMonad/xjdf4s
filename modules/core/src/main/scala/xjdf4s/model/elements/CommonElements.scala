package xjdf4s
package model
package elements

import xjdf4s.prim.*
import cats.Show
import cats.data.Chain
import cats.kernel.Eq

/** The `Comment` element (Table 8.14): human-readable text with an optional
 *  language, author, external reference and time stamp.
 */
final case class Comment(
    text: CommentText,
    language: Option[LanguageTag] = None,
    author: Option[XjdfString] = None,
    externalId: Option[NmToken] = None,
    personalId: Option[NmToken] = None,
    timestamp: Option[Timestamp] = None,
    commentType: Option[NmToken] = None
)

object Comment:

  def apply(text: String): Comment =
    Comment(CommentText(text))

  given Show[Comment] =
    Show.show(c => s"Comment(${c.text.value})")

  given Eq[Comment] = Eq.fromUniversalEquals

end Comment

/** The `GeneralID` element (Table 8.28): a generic identifier, e.g.
 *  `GeneralID[@Datatype="NamedFeature"]` (§3.1.3.1).
 */
final case class GeneralID(
    idUsage: NmToken,
    idValue: XjdfString,
    dataType: Option[NmToken] = None
)

object GeneralID:

  /** The predefined `NamedFeature` data type (§3.1.3.1). */
  val NamedFeatureDataType: NmToken = NmToken.unsafe("NamedFeature")

  given Show[GeneralID] = Show.show(g => s"GeneralID(${g.idUsage.value}=${g.idValue.value})")

  given Eq[GeneralID] = Eq.fromUniversalEquals

end GeneralID

/** The `Event` element (Table 8.21): an individual event or error code emitted
 *  by a Device.
 */
final case class Event(
    eventId: NmToken,
    eventValue: Option[XjdfString] = None
)

object Event:

  given Show[Event] = Show.show(e => s"Event(${e.eventId.value})")

  given Eq[Event] = Eq.fromUniversalEquals

end Event

/** The `Milestone` element (Table 8.50): an abstract, job-wide status
 *  description.
 */
final case class Milestone(
    milestoneType: NmToken,
    typeAmount: Option[Long] = None
)

object Milestone:

  given Show[Milestone] = Show.show(m => s"Milestone(${m.milestoneType.value})")

  given Eq[Milestone] = Eq.fromUniversalEquals

end Milestone

/** The `Dependent` element (Table 3.13): a reference to the XJDF that produces
 *  an input ResourceSet or consumes an output ResourceSet; the connecting edge
 *  of a pipe between two worksteps.
 */
final case class Dependent(
    jobId: JobId,
    jobPartId: Option[JobPartId] = None,
    pipeId: Option[NmToken] = None,
    pipeProtocol: Option[NmToken] = None,
    xjmfUrl: Option[Url] = None
)

object Dependent:

  given Show[Dependent] = Show.show(d => s"Dependent(${d.jobId.value})")

  given Eq[Dependent] = Eq.fromUniversalEquals

end Dependent

/** The `FileSpec` element (Table 8.22): a URL or a set of URLs. Exactly one of
 *  `@URL`, `@UID` and the `@FileFormat`+`@FileTemplate` pair SHALL be present,
 *  unless the referenced resource is a pipe — modelled as a closed `FileLocation`
 *  enum (a coproduct: every alternative carries its own data).
 */
final case class FileSpec(
    url: Option[Url] = None,
    uid: Option[NmToken] = None,
    fileFormat: Option[XjdfString] = None,
    fileTemplate: Option[NmTokens] = None,
    mimeType: Option[XjdfString] = None,
    checkSum: Option[NmToken] = None,
    encoding: Option[NmToken] = None,
    fileSize: Option[Long] = None,
    nPage: Option[Long] = None,
    overwritePolicy: Option[OverwritePolicy] = None,
    password: Option[XjdfString] = None,
    resourceUsage: Option[NmToken] = None,
    searchDepth: Option[Long] = None,
    userFileName: Option[XjdfString] = None,
    disposition: Option[Disposition] = None
):

  /** The resolved location: URL, UID, a template or an (empty) pipe. */
  def location: Option[FileLocation] =
    url.map(FileLocation.UrlLocation.apply)
      .orElse(uid.map(FileLocation.UidLocation.apply))
      .orElse {
        for
          format   <- fileFormat
          template <- fileTemplate
        yield FileLocation.Template(format, template)
      }
      .orElse(Option.when(allLocationAttributesAbsent)(FileLocation.Pipe))

  private def allLocationAttributesAbsent: Boolean =
    url.isEmpty && uid.isEmpty && fileFormat.isEmpty && fileTemplate.isEmpty
end FileSpec

object FileSpec:

  def ofUrl(url: Url): FileSpec = FileSpec(url = Some(url))

  def ofUid(uid: NmToken): FileSpec = FileSpec(uid = Some(uid))

  /** A pipe reference: no location attributes (Table 8.22). */
  val pipe: FileSpec = FileSpec()

  given Show[FileSpec] =
    Show.show(f => s"FileSpec(${f.location.fold("pipe")(_.toString)})")

  given Eq[FileSpec] = Eq.fromUniversalEquals

end FileSpec

/** The closed location alternatives of a FileSpec (Table 8.22). The case names
 *  carry a suffix so they do not clash with the opaque type `Url`.
 */
enum FileLocation:
  case UrlLocation(value: Url)
  case UidLocation(value: NmToken)
  case Template(fileFormat: XjdfString, fileTemplate: NmTokens)
  case Pipe

object FileLocation:

  given Show[FileLocation] = Show.fromToString

  given Eq[FileLocation] = Eq.fromUniversalEquals

end FileLocation

/** The `Disposition` element (Table 8.23): how long the referenced asset SHALL
 *  be maintained. `@MinDuration` and `@Until` are mutually exclusive.
 */
final case class Disposition(
    dispositionAction: Option[DispositionAction] = None,
    extraDuration: Option[TimeSpan] = None,
    minDuration: Option[TimeSpan] = None,
    until: Option[Timestamp] = None,
    priority: Option[Int] = None
)

object Disposition:

  given Show[Disposition] = Show.fromToString

  given Eq[Disposition] = Eq.fromUniversalEquals

end Disposition

/** The `Crease` element (Table 8.17): an individual crease line on a
 *  component. Used by the `CreasingParams` (`Crease+`, Table 6.51) and
 *  `FoldingParams` (`Crease*`, Table 6.74) resources. `@Depth` is measured in
 *  microns; `@WorkingPath` runs from `@StartPosition`.
 */
final case class Crease(
    depth: Option[Microns] = None,
    startPosition: Option[XYPair] = None,
    workingDirection: Option[WorkingDirection] = None,
    workingPath: Option[XYPair] = None
)

object Crease:

  given Show[Crease] = Show.fromToString

  given Eq[Crease] = Eq.fromUniversalEquals

end Crease

/** The `GangSource` element (§8.22 / Table 8.27): source-job information for
 *  a `BinderySignature` placed on a gang form.
 *
 *  Table 8.27 and `schema.xsd` agree on three attributes and no children:
 *  `@Copies` and `@JobID` are required, while `@BinderySignatureID` is
 *  optional. The four normative containers (`JobPhase`, `QueueFilter`,
 *  `QueueEntry`, `NodeInfo`) all declare `GangSource*` (`minOccurs="0"`,
 *  `maxOccurs="unbounded"`). `NodeInfo.gangSources` is wired in M1.6-8; the
 *  other three belong to messaging (M4).
 *
 *  `@JobID` names another XJDF and `@BinderySignatureID` names a signature in
 *  that source job. Both are NMTOKEN-valued cross-document identifiers, not
 *  document-scoped `IDREF`s (§2.2.3), so they do not contribute to
 *  `XJDF.references`. Resolving them requires an external job registry and is
 *  deliberately outside the pure single-ticket validator (ADR-0006;
 *  SPEC-COVERAGE, Deliberate Deviations).
 */
final case class GangSource(
    copies: Long,
    jobId: JobId,
    binderySignatureId: Option[NmToken] = None
):

  /** Table 8.27 declares no `IDREF` attributes. Its two reference-like
   *  attributes are cross-document NMTOKEN values, not references in the
   *  current XJDF ID scope.
   */
  def references: Chain[IdRef] = Chain.empty

end GangSource

object GangSource:

  given Show[GangSource] = Show.show { source =>
    val signature = source.binderySignatureId.fold("")(id => s", binderySignature=${id.value}")
    s"GangSource(job=${source.jobId.value}, copies=${source.copies}$signature)"
  }

  given Eq[GangSource] = Eq.fromUniversalEquals

end GangSource

/** The `MISDetails` element (§8.30 / Table 8.48): a container for MIS-related
 *  information — task complexity, chargeability and work type.
 *
 *  Table 8.48 and `schema.xsd` agree on four attributes, all optional, and no
 *  child elements: an empty `<MISDetails/>` is valid, so the element carries
 *  no local SHALL rule. The four normative containers (`ResourceInfo`,
 *  `PipeParams`, `JobPhase`, `NodeInfo`) all declare `MISDetails?`
 *  (`minOccurs="0"`, `maxOccurs="1"`). The first three belong to messaging
 *  (M4); `NodeInfo.misDetails` is wired in M1.6-8 together with
 *  `GangSource*`.
 *
 *  Data types (Table 8.48):
 *  - `@Complexity` → `UnitInterval`: the prose constrains the value to the
 *    range 0.0..1.0 ("in a range from 0.0 to 1.0"), which the factory of
 *    `UnitInterval` enforces at the boundary; the XSD type is a plain
 *    `xs:float` — per ROADMAP §1.2 the prose wins and the XSD stays a test
 *    oracle. The interpretation of values is implementation dependent
 *    (0.0 simple, 0.5 standard, 1.0 complex).
 *  - `@CostType` → closed enum `CostType` (`Chargeable`, `NonChargeable`)
 *  - `@WorkType` → closed enum `WorkType` (`Alteration`, `Original`, `Rework`)
 *  - `@WorkTypeDetails` → `NmToken` (open catalog `Catalog.WorkTypeDetails`,
 *    "Values include", ADR-0007)
 */
final case class MISDetails(
    complexity: Option[UnitInterval] = None,
    costType: Option[CostType] = None,
    workType: Option[WorkType] = None,
    workTypeDetails: Option[NmToken] = None
):

  /** Table 8.48 declares no ID or IDREF attributes (verified against
   *  `schema.xsd`), so the element contributes no references.
   */
  def references: Chain[IdRef] = Chain.empty

end MISDetails

object MISDetails:

  given Show[MISDetails] = Show.show { details =>
    val parts = List(
      details.complexity.map(c => s"complexity=${Show[UnitInterval].show(c)}"),
      details.costType.map(c => s"costType=${c.token.value}"),
      details.workType.map(w => s"workType=${w.token.value}"),
      details.workTypeDetails.map(d => s"workTypeDetails=${d.value}")
    ).flatten
    s"MISDetails(${parts.mkString(", ")})"
  }

  given Eq[MISDetails] = Eq.fromUniversalEquals

end MISDetails

/** The `Glue` element (Table 8.29): details of glue application on a
 *  component. Used by `BindIn` (Table 4.5), `StickOn` (Table 4.7),
 *  `AdhesiveNote` (Table 4.9) and finishing resources
 *  (`GluingParams`, `EndSheetGluingParams`, etc.).
 *
 *  `@GlueType` uses the 5-value enumeration (Table 8.29), distinct from
 *  the 3-value `EnumGlue` of Table A.24 (see ADR-0011, N-50).
 *
 *  SHALL rules (Table 8.29):
 *  - `@GluingPattern` SHALL contain an even number of entries.
 *  - `@MeltingTemperature` SHALL NOT be specified unless
 *    `@GlueType` is `Hotmelt` or `PUR`.
 */
final case class Glue(
    areaGlue: Option[Boolean] = None,
    glueLineWidth: Option[Double] = None,
    glueRef: Option[IdRef] = None,
    glueType: Option[GlueType] = None,
    gluingPattern: Option[FloatList] = None,
    gluingTechnique: Option[GluingTechnique] = None,
    meltingTemperature: Option[Long] = None,
    startPosition: Option[XYPair] = None,
    workingDirection: Option[Face] = None,
    workingPath: Option[XYPair] = None
)

object Glue:

  /** IDREF: `@GlueRef` references a `MiscConsumable` resource. */
  def references(glue: Glue): Chain[IdRef] =
    Chain.fromOption(glue.glueRef)

  /** Local SHALL rules for the `Glue` element (Table 8.29, ADR-0003). */
  def law(glue: Glue, at: XPath): Chain[Issue] =
    Chain.fromOption(gluingPatternEven(glue, at)) ++
      Chain.fromOption(meltingTemperatureRule(glue, at))

  private def gluingPatternEven(glue: Glue, at: XPath): Option[Issue] =
    glue.gluingPattern.flatMap { pattern =>
      if pattern.size % 2 != 0 then
        Some(Issue.errorC(
          IssueCode.GluePatternOdd,
          at,
          "Glue/@GluingPattern SHALL contain an even number of entries (Table 8.29)"
        ))
      else None
    }

  private def meltingTemperatureRule(glue: Glue, at: XPath): Option[Issue] =
    glue.meltingTemperature.flatMap { _ =>
      glue.glueType match
        case Some(GlueType.Hotmelt) | Some(GlueType.PUR) => None
        case _ =>
          Some(Issue.errorC(
            IssueCode.GlueMeltingTempWithoutHeat,
            at,
            "Glue/@MeltingTemperature SHALL NOT be specified unless @GlueType is Hotmelt or PUR (Table 8.29)"
          ))
    }

  given Show[Glue] = Show.fromToString

  given Eq[Glue] = Eq.fromUniversalEquals

end Glue

/** The `HolePattern` element (Table 8.30): a pattern of one or more holes.
 *  Used by `HoleMakingIntent` (Table 4.29, `HolePattern+`), `HoleMakingParams`
 *  (`HolePattern+`, Table 6.78), `LooseBinding` (Table 4.12), `Media`
 *  (Table 6.114) and other finishing resources.
 *
 *  All nine attributes are optional per Table 8.30, but SHALL:
 *  - `@Pattern` SHALL be supplied if `@Center`, `@Extent` or `@Shape` is
 *    not specified — i.e. when any of those three is absent, `@Pattern`
 *    is required (contrapositive: when `@Pattern` is absent, all three
 *    SHALL be present).
 *
 *  Data types:
 *  - `@Center`, `@Extent`, `@Pitch` → `XYPair`
 *  - `@CenterReference` → `HoleCenterReference` (RegistrationMark, TrailingEdge)
 *  - `@HoleCount` → `IntegerList`
 *  - `@Pattern` → `NmToken` (Allowed value is from: Appendix F Hole Pattern Catalog)
 *    open catalog `Catalog.HolePattern` (34 values, including `None` from XSD)
 *  - `@ReferenceEdge` → `HoleReferenceEdge` (Bottom, Left, Pattern, Right, Top)
 *  - `@Reinforcement` → `NmToken` (Values include: Grommet) open catalog
 *    `Catalog.HoleReinforcement`
 *  - `@Shape` → `HoleShape` (Elliptic, Rectangular, Round)
 */
final case class HolePattern(
    center: Option[XYPair] = None,
    centerReference: Option[HoleCenterReference] = None,
    extent: Option[XYPair] = None,
    holeCount: Option[IntegerList] = None,
    pattern: Option[NmToken] = None,
    pitch: Option[XYPair] = None,
    referenceEdge: Option[HoleReferenceEdge] = None,
    reinforcement: Option[NmToken] = None,
    shape: Option[HoleShape] = None
)

object HolePattern:

  /** Local SHALL rule for `HolePattern` (Table 8.30, ADR-0003):
   *  `@Pattern` SHALL be supplied if `@Center`, `@Extent` or `@Shape`
   *  is not specified.
   */
  def law(hole: HolePattern, at: XPath): Chain[Issue] =
    val needsPattern = hole.center.isEmpty || hole.extent.isEmpty || hole.shape.isEmpty
    if needsPattern && hole.pattern.isEmpty then
      Chain.one(Issue.errorC(
        IssueCode.HolePatternPatternRequired,
        at,
        "HolePattern/@Pattern SHALL be supplied when @Center, @Extent or @Shape is missing (Table 8.30)"
      ))
    else Chain.empty

  given Show[HolePattern] = Show.fromToString

  given Eq[HolePattern] = Eq.fromUniversalEquals

end HolePattern

/** The `Certification` element (§8.7 / Table 8.8): the certification
 *  properties of a resource or a process — a sustainability claim such as
 *  `FSC Mix 70%`, the identification number issued for it and the issuing
 *  organization.
 *
 *  *(New in XJDF 2.1)* in every chapter-4 container it appears in
 *  (`ColorIntent/SurfaceColor`, `ProductionIntent`); `MediaIntent` and `Media`
 *  carry it without a version note.
 *
 *  Containers (`Certification*` everywhere, `schema.xsd`
 *  `minOccurs="0" maxOccurs="unbounded"`): `ColorIntent/SurfaceColor`
 *  (Table 4.21), `ProductionIntent` (Table 4.33), `MediaIntent` (Table 4.32),
 *  `Media` (Table 6.114), plus `Ink` (Table 6.83) and `MiscConsumable`
 *  (Table 6.117), which this library does not model yet.
 *
 *  All three attributes are strings/NMTOKEN and are optional in `schema.xsd`,
 *  yet Table 4.21/4.32/4.33 and Table 6.114 state that "Each Certification
 *  SHALL specify a … certification level". An element carrying none of the
 *  three therefore specifies nothing and violates that SHALL — see
 *  `Certification.law` and ADR-0012.
 *
 *  The container-level sentence "If more than one Certification is present, at
 *  least one of the … levels SHALL be met" constrains actual production, not
 *  the document: a ticket cannot state whether a level *was met*. It is
 *  deliberately not a validation rule (SPEC-COVERAGE, Deliberate Deviations).
 *
 *  Data types (Table 8.8):
 *  - `@Claim` → `XjdfString` (open catalog `Catalog.CertificationClaim`,
 *    "Values include", ADR-0007)
 *  - `@Identifier` → `XjdfString` (free-form, issued by the organization)
 *  - `@Organization` → `NmToken` (open catalog
 *    `Catalog.CertificationOrganization`, "Values include", ADR-0007)
 */
final case class Certification(
    claim: Option[XjdfString] = None,
    identifier: Option[XjdfString] = None,
    organization: Option[NmToken] = None
):

  /** True when at least one attribute of Table 8.8 is present, i.e. the
   *  element actually specifies a certification level (see `law`).
   */
  def specifiesLevel: Boolean =
    claim.isDefined || identifier.isDefined || organization.isDefined

  /** `Certification` carries no ID or IDREF attributes (Table 8.8, verified
   *  against `schema.xsd`), so it contributes no references.
   */
  def references: Chain[IdRef] = Chain.empty

end Certification

object Certification:

  /** Local SHALL rule for `Certification` (Table 8.8 + Tables 4.21/4.32/4.33,
   *  6.114; ADR-0003, ADR-0012): each `Certification` SHALL specify a
   *  certification level. `schema.xsd` declares all three attributes
   *  `use="optional"`, so an empty `<Certification/>` is schema-valid but
   *  specifies nothing; per ROADMAP §1.2 the prose wins and the XSD stays a
   *  test oracle.
   */
  def law(certification: Certification, at: XPath): Chain[Issue] =
    if certification.specifiesLevel then Chain.empty
    else
      Chain.one(Issue.errorC(
        IssueCode.CertificationLevelMissing,
        at,
        "Each Certification SHALL specify a certification level: at least one of " +
          "@Claim, @Identifier or @Organization is required (Table 8.8, Tables 4.21/4.32/4.33/6.114)"
      ))

  /** Applies `law` to every element of a container's `Certification*` chain,
   *  indexing the XPath by position. The four modelled containers
   *  (`SurfaceColor`, `ProductionIntent`, `MediaIntent`, `Media`) share this
   *  traversal so the rule cannot drift between them.
   */
  def containerLaw(certifications: Chain[Certification], at: XPath): Chain[Issue] =
    certifications.zipWithIndex.flatMap { (c, i) =>
      law(c, XPath(s"$at/Certification[$i]"))
    }

  given Show[Certification] = Show.fromToString

  given Eq[Certification] = Eq.fromUniversalEquals

end Certification

/** The `Expr` element (§8.29.1 / Table 8.47): a named XPath expression used by
 *  a parent `RunList/MetadataMap` to extract one template variable.
 *
 *  Both attributes are required in Table 8.47 and in `schema.xsd`, so they are
 *  plain fields rather than `Option`s:
 *
 *  - `@Name` → `NmToken`;
 *  - `@Path` → `XjdfXPath`, the Appendix A XJDF data type `XPath`.
 *
 *  `XjdfXPath` is intentionally distinct from `model.XPath`, which locates
 *  validation issues. Table 8.47 adds evaluation semantics (an implied
 *  `text()` when the path selects an element), but no node-local structural
 *  SHALL that can be checked without the parent `MetadataMap`; those
 *  context-dependent rules belong to M1.6-6b/B2.
 */
final case class Expr(
    name: NmToken,
    path: XjdfXPath
):

  /** Table 8.47 declares no ID or IDREF attributes (verified against
   *  `schema.xsd`), so the element contributes no document references.
   */
  def references: Chain[IdRef] = Chain.empty

end Expr

object Expr:

  given Show[Expr] = Show.show(e => s"Expr(${e.name.value}=${e.path.value})")

  given Eq[Expr] = Eq.fromUniversalEquals

end Expr

/** The `BarcodeDetails` element (§8.26.1 / Table 8.33): additional
 *  specification for complex barcodes, i.e. the matrix geometry and error
 *  correction that `IdentificationField/@EncodingDetails` alone cannot
 *  express.
 *
 *  Table 8.33 and `schema.xsd` agree on four attributes, all optional, and no
 *  child elements: an empty `<BarcodeDetails/>` is valid, so the element
 *  carries no local SHALL rule. The single container is
 *  `IdentificationField` with `BarcodeDetails?` (`minOccurs="0"`,
 *  `maxOccurs="1"`).
 *
 *  Data types (Table 8.33):
 *  - `@BarcodeVersion` → `NmToken` (open catalog `Catalog.BarcodeVersion`,
 *    "Values include those from" Tables 8.36/8.37, ADR-0007)
 *  - `@ErrorCorrectionLevel` → `NmToken` (open catalog
 *    `Catalog.ErrorCorrectionLevel`, "Values include", ADR-0007)
 *  - `@XCells`, `@YCells` → `Option[Long]` (`xs:int`; the spec states no
 *    positivity restriction, so none is invented)
 */
final case class BarcodeDetails(
    barcodeVersion: Option[NmToken] = None,
    errorCorrectionLevel: Option[NmToken] = None,
    xCells: Option[Long] = None,
    yCells: Option[Long] = None
):

  /** Table 8.33 declares no ID or IDREF attributes (verified against
   *  `schema.xsd`), so the element contributes no references.
   */
  def references: Chain[IdRef] = Chain.empty

end BarcodeDetails

object BarcodeDetails:

  given Show[BarcodeDetails] = Show.show { details =>
    val parts = List(
      details.barcodeVersion.map(v => s"version=${v.value}"),
      details.errorCorrectionLevel.map(l => s"ec=${l.value}"),
      details.xCells.map(x => s"xCells=$x"),
      details.yCells.map(y => s"yCells=$y")
    ).flatten
    s"BarcodeDetails(${parts.mkString(", ")})"
  }

  given Eq[BarcodeDetails] = Eq.fromUniversalEquals

end BarcodeDetails

/** The `ExtraValues` element (§8.26.2 / Table 8.34): an additional value
 *  encoded in the containing `IdentificationField`, e.g. the composite code
 *  part of an RSS-14 barcode or the supplemental digits of a UPC.
 *
 *  Both attributes are `use="required"` in `schema.xsd` and unmarked in
 *  Table 8.34, so they are plain fields rather than `Option`s: the type makes
 *  an incomplete `ExtraValues` unrepresentable and no runtime rule is needed.
 *  The single container is `IdentificationField` with `ExtraValues?`
 *  (`minOccurs="0"`, `maxOccurs="1"` — one element, not a list).
 *
 *  Data types (Table 8.34):
 *  - `@Usage` → `NmToken` (open catalog `Catalog.ExtraValuesUsage`,
 *    "Values include", ADR-0007)
 *  - `@Value` → `XjdfString`
 */
final case class ExtraValues(
    usage: NmToken,
    value: XjdfString
):

  /** Table 8.34 declares no ID or IDREF attributes (verified against
   *  `schema.xsd`), so the element contributes no references.
   */
  def references: Chain[IdRef] = Chain.empty

end ExtraValues

object ExtraValues:

  given Show[ExtraValues] =
    Show.show(e => s"ExtraValues(${e.usage.value}=${e.value.value})")

  given Eq[ExtraValues] = Eq.fromUniversalEquals

end ExtraValues

/** The `IdentificationField` element (§8.26 / Table 8.31): a mark on a
 *  document — a bar code, a plain-text field, Braille or an RFID tag. It is
 *  read in both directions: a workflow generates the mark from the element,
 *  or decodes a scanned mark and verifies it against the element.
 *
 *  Containers (Table 8.31, "Element referenced by"): `Component`,
 *  `Content/BarcodeProductionParams`, `Device`, `EmbossingParams/Emboss`,
 *  `ExposedMedia`, `Ink`, `Layout/StripMark`, `Media`, `MiscConsumable`,
 *  `Pallet`, `Tool`, `Module`. `schema.xsd` gives `IdentificationField*`
 *  (`minOccurs="0" maxOccurs="unbounded"`) everywhere except
 *  `BarcodeProductionParams` and `Emboss`, which declare `maxOccurs="1"`.
 *  `Component` (Table 6.37) is the container wired in M1.6-6; the remaining
 *  modelled ones (`Device`, `Media`) follow with their own traversals.
 *
 *  All ten attributes are optional in Table 8.31 and in `schema.xsd`, but the
 *  element is not free of obligations:
 *
 *  - SHALL (repeated in the rows of `@Format`, `@Value`, `@ValueFormat` and
 *    `@ValueTemplate`): "Exactly one of `@Format`, `@Value` or the pair
 *    `@ValueFormat` and `@ValueTemplate` SHALL be specified." The three
 *    alternatives are mutually exclusive, the pair is indivisible, and an
 *    element specifying none of them is a violation as well — see
 *    `IdentificationField.law`.
 *
 *  Data types (Table 8.31):
 *  - `@BoundingBox` → `Rectangle`
 *  - `@Encoding` → closed enum `FieldEncoding` (ASCII, Barcode, Braille, RFID)
 *  - `@EncodingDetails` → `NmToken` (open catalog `Catalog.EncodingDetails`,
 *    Table 8.32 is explicitly a sample list, ADR-0007)
 *  - `@Format` → `RegExp`
 *  - `@Orientation` → `Matrix`
 *  - `@Position` → `Face` (Table A.20, the `Face` enumeration)
 *  - `@Purpose` → closed enum `FieldPurpose` (Label, Separation, Verification)
 *  - `@PurposeDetails` → `NmToken` (open catalog `Catalog.PurposeDetails`,
 *    "Values include", ADR-0007)
 *  - `@Value` → `XjdfString`
 *  - `@ValueFormat` → `XjdfString` (Appendix D String Generation)
 *  - `@ValueTemplate` → `NmTokens` (NMTOKENS: a non-empty list by type)
 *
 *  `MetadataMap*` (Table 8.46) is the third child of Table 8.31 and is not
 *  modelled yet: it carries `Expr*` (Table 8.47), the `XPath` data type and
 *  two context-dependent SHALL rules, and it is shared with `RunList`
 *  (Table 6.148). It is a slice of its own (M1.6-6b).
 */
final case class IdentificationField(
    boundingBox: Option[Rectangle] = None,
    encoding: Option[FieldEncoding] = None,
    encodingDetails: Option[NmToken] = None,
    format: Option[RegExp] = None,
    orientation: Option[Matrix] = None,
    position: Option[Face] = None,
    purpose: Option[FieldPurpose] = None,
    purposeDetails: Option[NmToken] = None,
    value: Option[XjdfString] = None,
    valueFormat: Option[XjdfString] = None,
    valueTemplate: Option[NmTokens] = None,
    barcodeDetails: Option[BarcodeDetails] = None,
    extraValues: Option[ExtraValues] = None
):

  /** The alternatives of the Table 8.31 SHALL that this element specifies, as
   *  the specification names them. An element is lawful exactly when this
   *  list has one entry (see `IdentificationField.law`).
   *
   *  `@ValueFormat` and `@ValueTemplate` count as the single alternative
   *  "the pair", and only when both are present: half a pair specifies no
   *  complete alternative.
   */
  def valueSources: List[String] =
    List(
      Option.when(format.isDefined)("@Format"),
      Option.when(value.isDefined)("@Value"),
      Option.when(valueFormat.isDefined && valueTemplate.isDefined)("@ValueFormat + @ValueTemplate")
    ).flatten

  /** True when exactly one of `@ValueFormat` and `@ValueTemplate` is present,
   *  i.e. the pair alternative is started but not completed.
   */
  def hasPartialPair: Boolean = valueFormat.isDefined != valueTemplate.isDefined

  /** Table 8.31 declares no ID or IDREF attributes, and neither do the two
   *  modelled children (Tables 8.33 and 8.34) — verified against
   *  `schema.xsd`. The children are still walked so the fact stays checked
   *  rather than assumed.
   */
  def references: Chain[IdRef] =
    barcodeDetails.fold(Chain.empty[IdRef])(_.references) ++
      extraValues.fold(Chain.empty[IdRef])(_.references)

end IdentificationField

object IdentificationField:

  /** The local SHALL rule of Table 8.31 (ADR-0003): "Exactly one of
   *  `@Format`, `@Value` or the pair `@ValueFormat` and `@ValueTemplate`
   *  SHALL be specified."
   *
   *  `schema.xsd` types all four attributes `use="optional"` and cannot
   *  express the exclusion, so the prose wins and the XSD stays a test
   *  oracle (ROADMAP §1.2) — the same shape of divergence as ADR-0012, but
   *  without an interpretation to decide: the sentence is explicit, so no ADR
   *  is required.
   *
   *  Three ways to break it, one stable `IssueCode`:
   *  none of the alternatives, more than one of them, or half of the pair.
   */
  def law(field: IdentificationField, at: XPath): Chain[Issue] =
    val sources = field.valueSources
    if sources.sizeIs == 1 && !field.hasPartialPair then Chain.empty
    else
      val detail =
        if field.hasPartialPair && sources.isEmpty then
          "@ValueFormat and @ValueTemplate SHALL be specified together"
        else if sources.isEmpty then "none is specified"
        else if sources.sizeIs > 1 then s"${sources.mkString(" and ")} are specified together"
        else "@ValueFormat and @ValueTemplate SHALL be specified together"
      Chain.one(Issue.errorC(
        IssueCode.IdentificationFieldValueSource,
        at,
        "Exactly one of @Format, @Value or the pair @ValueFormat and @ValueTemplate SHALL be " +
          s"specified (Table 8.31): $detail"
      ))
  end law

  /** Applies `law` to every element of a container's `IdentificationField*`
   *  chain, indexing the XPath by position — the shared traversal keeps the
   *  rule from drifting between containers (same shape as
   *  `Certification.containerLaw`).
   */
  def containerLaw(fields: Chain[IdentificationField], at: XPath): Chain[Issue] =
    fields.zipWithIndex.flatMap { (f, i) =>
      law(f, XPath(s"$at/IdentificationField[$i]"))
    }

  given Show[IdentificationField] = Show.show { field =>
    val parts = List(
      field.encoding.map(e => s"encoding=${e.token.value}"),
      field.encodingDetails.map(d => s"details=${d.value}"),
      field.purpose.map(p => s"purpose=${p.token.value}"),
      field.format.map(f => s"format=${f.value}"),
      field.value.map(v => s"value=${v.value}"),
      field.valueFormat.map(v => s"valueFormat=${v.value}"),
      field.valueTemplate.map(t => s"valueTemplate=${Show[NmTokens].show(t)}")
    ).flatten
    s"IdentificationField(${parts.mkString(", ")})"
  }

  given Eq[IdentificationField] = Eq.fromUniversalEquals

end IdentificationField
