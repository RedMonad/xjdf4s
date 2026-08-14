package xjdf4s
package prim

import cats.Show
import cats.kernel.{Eq, Monoid, Order}

/**
 * XJDF data type `dateTime` (Table A.1): a specific instant of time — UTC or a
 * local time that includes the time zone. Backed by `java.time.OffsetDateTime`.
 */
opaque type Timestamp = java.time.OffsetDateTime

object Timestamp:

  def from(raw: String): Option[Timestamp] =
    try Some(java.time.OffsetDateTime.parse(raw))
    catch case _: java.time.DateTimeParseException => None

  def unsafe(raw: String): Timestamp =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid dateTime: '$raw'"))

  def now(): Timestamp = java.time.OffsetDateTime.now()

  def ofEpochSecond(seconds: Long): Timestamp =
    java.time.OffsetDateTime.ofInstant(java.time.Instant.ofEpochSecond(seconds), java.time.ZoneOffset.UTC)

  extension (t: Timestamp)
    def toJava: java.time.OffsetDateTime      = t
    def instant: java.time.Instant            = t.toInstant
    def isBefore(o: Timestamp): Boolean       = t.isBefore(o)
    def isAfter(o: Timestamp): Boolean        = t.isAfter(o)
    def plus(d: TimeSpan): Timestamp          = t.plus(d.toJava)
    def minus(d: TimeSpan): Timestamp         = t.minus(d.toJava)

  given Show[Timestamp] =
    Show.show(t => t.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME))

  given Eq[Timestamp] = Eq.fromUniversalEquals

  given Order[Timestamp] = Order.from((a, b) => a.compareTo(b))

end Timestamp

/**
 * XJDF data type `duration` (Table A.1): a duration of time, including
 * non-working hours. Backed by `java.time.Duration`. Addition of durations is
 * a lawful commutative monoid.
 */
opaque type TimeSpan = java.time.Duration

object TimeSpan:

  def from(raw: String): Option[TimeSpan] =
    try Some(java.time.Duration.parse(raw))
    catch case _: java.time.DateTimeParseException => None

  def ofSeconds(seconds: Long): TimeSpan = java.time.Duration.ofSeconds(seconds)

  def ofMinutes(minutes: Long): TimeSpan = java.time.Duration.ofMinutes(minutes)

  def ofHours(hours: Long): TimeSpan = java.time.Duration.ofHours(hours)

  extension (d: TimeSpan)
    def toJava: java.time.Duration = d
    def seconds: Long              = d.getSeconds
    def isZero: Boolean            = d.isZero

  given Show[TimeSpan] = Show.show(_.toJava.toString)

  given Eq[TimeSpan] = Eq.fromUniversalEquals

  given Monoid[TimeSpan] with
    def empty: TimeSpan = java.time.Duration.ZERO
    def combine(a: TimeSpan, b: TimeSpan): TimeSpan = a.plus(b)

end TimeSpan

/**
 * A time interval — e.g. the `@Start`..`@End` span of a ProcessRun or the
 * `@FirstStart`..`@LastEnd` span of a NodeInfo. A named tuple behind an opaque
 * type.
 */
opaque type TimeRange = (start: Timestamp, end: Timestamp)

object TimeRange:

  def apply(start: Timestamp, end: Timestamp): TimeRange = (start = start, end = end)

  extension (r: TimeRange)
    def start: Timestamp = r.start
    def end: Timestamp   = r.end
    def duration: TimeSpan = java.time.Duration.between(r.start.toJava.toInstant, r.end.toJava.toInstant)
    def contains(t: Timestamp): Boolean = !t.isBefore(r.start) && !t.isAfter(r.end)

  given Show[TimeRange] =
    Show.show(r => s"${Show[Timestamp].show(r.start)} .. ${Show[Timestamp].show(r.end)}")

  given Eq[TimeRange] = Eq.fromUniversalEquals

end TimeRange
