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
        \"HolePattern/@Pattern SHALL be supplied when @Center, @Extent or @Shape is missing (Table 8.30)\"
      ))
    else Chain.empty

  given Show[HolePattern] = Show.fromToString

  given Eq[HolePattern] = Eq.fromUniversalEquals

end HolePattern
