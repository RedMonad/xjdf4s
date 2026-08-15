package xjdf4s
package resources

import xjdf4s.prim.*
import cats.kernel.Eq

/** The `NodeInfo` resource (Table 6.119): scheduling information about the
 *  process described by this XJDF. `NodeInfo` without `@ProcessUsage` applies to
 *  the whole process; `NodeInfo[@ProcessUsage="EndCustomer"]` carries end
 *  customer scheduling requirements (§3.4).
 */
final case class NodeInfo(
    cleanupDuration: Option[TimeSpan] = None,
    dueLevel: Option[Long] = None,
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
    totalDuration: Option[TimeSpan] = None
):

  /** The planned execution window `@FirstStart`..`@LastEnd`, when known. */
  def plannedWindow: Option[TimeRange] =
    for
      s <- firstStart.orElse(start)
      e <- lastEnd.orElse(end)
    yield TimeRange(s, e)
end NodeInfo

object NodeInfo:

  /** A minimal scheduled step: start and planned duration. */
  def scheduled(start: Timestamp, duration: TimeSpan): NodeInfo =
    NodeInfo(start = Some(start), totalDuration = Some(duration))

  given Eq[NodeInfo] = Eq.fromUniversalEquals

end NodeInfo
