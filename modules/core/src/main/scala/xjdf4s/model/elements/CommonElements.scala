package xjdf4s
package model
package elements

import xjdf4s.prim.*
import cats.Show
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
 *  microns, `@StartPosition` and `@WorkingPath` in the operation coordinate
 *  system.
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
