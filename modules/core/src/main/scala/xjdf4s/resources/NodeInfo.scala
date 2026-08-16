package xjdf4s
package resources

import xjdf4s.model.elements.{GangSource, MISDetails}
import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** The `NodeInfo` resource (Table 6.119): scheduling information about the
 *  process described by this XJDF. `NodeInfo` without `@ProcessUsage` applies to
 *  the whole process; `NodeInfo[@ProcessUsage="EndCustomer"]` carries end
 *  customer scheduling requirements (§3.4).
 *
 *  Table 6.119 declares fifteen optional attributes and two child elements;
 *  `schema.xsd` (`<xs:complexType name="NodeInfo">`) agrees on every name and
 *  on `use="optional"` throughout. There is no local SHALL rule, so an empty
 *  `<NodeInfo/>` is valid.
 *
 *  Child elements (M1.6-8):
 *  - `GangSource*` → `Chain` (`minOccurs="0" maxOccurs="unbounded"`): the
 *    source jobs processed as a gang job (§8.22 / Table 8.27).
 *  - `MISDetails?` → `Option` (`minOccurs="0" maxOccurs="1"`): how the costs of
 *    executing this node are charged (§8.30 / Table 8.48).
 *
 *  `@PersonalID` is a NMTOKEN naming the `Resource/@ExternalID` of the Contact
 *  that represents the responsible employee. `@ExternalID` is not an `@ID`, so
 *  the attribute is not an `IDREF` in the document scope of §2.2.3 and is not
 *  collected into `references` — the same classification as the cross-document
 *  identifiers of `GangSource` (SPEC-COVERAGE, Deliberate Deviations).
 */
final case class NodeInfo(
    cleanupDuration: Option[TimeSpan] = None,
    dueLevel: Option[DueLevel] = None,
    end: Option[Timestamp] = None,
    firstEnd: Option[Timestamp] = None,
    firstStart: Option[Timestamp] = None,
    jobPriority: Option[Long] = None,
    lastEnd: Option[Timestamp] = None,
    lastStart: Option[Timestamp] = None,
    naturalLang: Option[LanguageTag] = None,
    personalId: Option[NmToken] = None,
    setupDuration: Option[TimeSpan] = None,
    start: Option[Timestamp] = None,
    status: Option[Status] = None,
    statusDetails: Option[NmToken] = None,
    totalDuration: Option[TimeSpan] = None,
    gangSources: Chain[GangSource] = Chain.empty,
    misDetails: Option[MISDetails] = None
):

  /** The planned execution window `@FirstStart`..`@LastEnd`, when known. */
  def plannedWindow: Option[TimeRange] =
    for
      s <- firstStart.orElse(start)
      e <- lastEnd.orElse(end)
    yield TimeRange(s, e)

  /** `NodeInfo` itself declares no IDREF attributes, and neither do the nested
   *  `GangSource` (Table 8.27) and `MISDetails` (Table 8.48) — both chains are
   *  walked so the fact stays checked rather than assumed (M1.6-8).
   */
  def references: Chain[IdRef] =
    gangSources.flatMap(_.references) ++ misDetails.fold(Chain.empty[IdRef])(_.references)

end NodeInfo

object NodeInfo:

  /** A minimal scheduled step: start and planned duration. */
  def scheduled(start: Timestamp, duration: TimeSpan): NodeInfo =
    NodeInfo(start = Some(start), totalDuration = Some(duration))

  given Eq[NodeInfo] = Eq.fromUniversalEquals

end NodeInfo
