package xjdf4s.core

import java.net.URI
import java.time.OffsetDateTime

import scala.util.Try

import cats.{Eq, Hash, Show}

private def nonBlank(field: String, value: String): Either[ValidationError, String] =
  Either.cond(value.trim.nonEmpty, value, ValidationError.EmptyValue(field))

opaque type Nmtoken = String
object Nmtoken:
  private val Pattern = "[\\p{L}\\p{N}._:·\\p{M}-]+".r

  def from(value: String): Either[ValidationError, Nmtoken] =
    nonBlank("NMTOKEN", value).flatMap: candidate =>
      Either.cond(
        Pattern.matches(candidate),
        candidate,
        ValidationError.InvalidValue("NMTOKEN", value, "an XML NMTOKEN without whitespace"),
      )

  extension (value: Nmtoken) def value: String = value

  given Eq[Nmtoken] = Eq.by(_.value)
  given Show[Nmtoken] = Show.show(_.value)
  given Hash[Nmtoken] = Hash.by(_.value)
end Nmtoken

opaque type XsdId = String
object XsdId:
  private val Pattern = "[\\p{L}_][\\p{L}\\p{N}._·\\p{M}-]*".r

  def from(value: String): Either[ValidationError, XsdId] =
    nonBlank("ID", value).flatMap: candidate =>
      Either.cond(
        Pattern.matches(candidate),
        candidate,
        ValidationError.InvalidValue("ID", value, "an XML Name without ':'"),
      )

  extension (value: XsdId) def value: String = value

  given Eq[XsdId] = Eq.by(_.value)
  given Show[XsdId] = Show.show(_.value)
  given Hash[XsdId] = Hash.by(_.value)
end XsdId

/** The IDREF side of the XSD identity constraint pair. IDREF shares the lexical space of [[XsdId]] but is a *reference*,
 *  never a declaration: keeping the two types separate makes it impossible to point a reference at a declaration slot or
 *  vice versa. Reference integrity (existence, target type) is checked document-wide, see the `validate` methods of the
 *  document roots.
 */
opaque type XsdIdRef = String
object XsdIdRef:
  private val Pattern = "[\\p{L}_][\\p{L}\\p{N}._·\\p{M}-]*".r

  def from(value: String): Either[ValidationError, XsdIdRef] =
    nonBlank("IDREF", value).flatMap: candidate =>
      Either.cond(
        Pattern.matches(candidate),
        candidate,
        ValidationError.InvalidValue("IDREF", value, "an XML Name without ':'"),
      )

  extension (value: XsdIdRef) def value: String = value

  given Eq[XsdIdRef] = Eq.by(_.value)
  given Show[XsdIdRef] = Show.show(_.value)
  given Hash[XsdIdRef] = Hash.by(_.value)
end XsdIdRef

opaque type UriRef = URI
object UriRef:
  def from(value: String): Either[ValidationError, UriRef] =
    Try(URI.create(value)).toEither.left.map: _ =>
      ValidationError.InvalidValue("URL", value, "a URI reference")

  extension (value: UriRef) def value: URI = value

  given Eq[UriRef] = Eq.fromUniversalEquals
  given Show[UriRef] = Show.show(_.value.toString)
  given Hash[UriRef] = Hash.fromUniversalHashCode
end UriRef

opaque type LanguageTag = String
object LanguageTag:
  private val Pattern = "[A-Za-z]{1,8}(-[A-Za-z0-9]{1,8})*".r

  def from(value: String): Either[ValidationError, LanguageTag] =
    Either.cond(
      Pattern.matches(value),
      value,
      ValidationError.InvalidValue("language", value, "a BCP 47-style language tag"),
    )

  extension (value: LanguageTag) def value: String = value

  given Eq[LanguageTag] = Eq.by(_.value)
  given Show[LanguageTag] = Show.show(_.value)
  given Hash[LanguageTag] = Hash.by(_.value)
end LanguageTag

/** The XJDF `string` simple type (Appendix A.1): a normalized string of at most 1023 characters. Tabs, line feeds and
 *  similar control characters are not valid. Field values of the normative `string` type are modelled with this opaque
 *  type; XML `text` element bodies (which are not length-restricted) remain plain `String`.
 */
opaque type XjdfString = String
object XjdfString:
  val MaxLength: Int = 1023

  def from(value: String): Either[ValidationError, XjdfString] =
    Either.cond(
      value.length <= MaxLength && !value.exists(_ < ' '),
      value,
      ValidationError.InvalidValue(
        "string",
        value,
        "a normalized string of at most 1023 characters without tabs, line feeds or control characters",
      ),
    )

  extension (value: XjdfString) def value: String = value

  given Eq[XjdfString] = Eq.by(_.value)
  given Show[XjdfString] = Show.show(_.value)
  given Hash[XjdfString] = Hash.by(_.value)
end XjdfString

opaque type XsdDateTime = String
object XsdDateTime:
  private val Pattern = "[0-9]{4,}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]+)?(Z|[+-][0-9]{2}:[0-9]{2})".r

  def from(value: String): Either[ValidationError, XsdDateTime] =
    Either.cond(
      Pattern.matches(value) && Try(OffsetDateTime.parse(value)).isSuccess,
      value,
      ValidationError.InvalidValue(
        "dateTime",
        value,
        "an XSD dateTime lexical value with a mandatory time zone and a valid calendar date",
      ),
    )

  extension (value: XsdDateTime) def value: String = value

  given Eq[XsdDateTime] = Eq.by(_.value)
  given Show[XsdDateTime] = Show.show(_.value)
  given Hash[XsdDateTime] = Hash.by(_.value)
end XsdDateTime

opaque type XsdDuration = String
object XsdDuration:
  // `(?=.)` guards reject the empty forms "P" and "PT" as well as a trailing "T" without any component.
  private val Pattern =
    "-?P(?=.)(?:[0-9]+Y)?(?:[0-9]+M)?(?:[0-9]+D)?(?:T(?=.)(?:[0-9]+H)?(?:[0-9]+M)?(?:[0-9]+(\\.[0-9]+)?S)?)?".r

  def from(value: String): Either[ValidationError, XsdDuration] =
    Either.cond(
      Pattern.matches(value),
      value,
      ValidationError.InvalidValue("duration", value, "an XSD duration lexical value with at least one component"),
    )

  extension (value: XsdDuration) def value: String = value

  given Eq[XsdDuration] = Eq.by(_.value)
  given Show[XsdDuration] = Show.show(_.value)
  given Hash[XsdDuration] = Hash.by(_.value)
end XsdDuration

/** The normative 0..100 integer priority used by Disposition, NodeInfo, GangElement and queue entries. */
opaque type Priority0To100 = Int
object Priority0To100:
  def from(value: Int): Either[ValidationError, Priority0To100] =
    Either.cond(
      value >= 0 && value <= 100,
      value,
      ValidationError.ValueOutOfBounds("Priority", value.toString, "[0..100]"),
    )

  extension (value: Priority0To100) def value: Int = value

  given Eq[Priority0To100] = Eq.by(_.value)
  given Show[Priority0To100] = Show.show(_.value.toString)
  given Hash[Priority0To100] = Hash.by(_.value)
end Priority0To100
