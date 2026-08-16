package xjdf4s.core

import java.net.URI

import scala.util.Try

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
end XsdId

opaque type UriRef = URI
object UriRef:
  def from(value: String): Either[ValidationError, UriRef] =
    Try(URI.create(value)).toEither.left.map: _ =>
      ValidationError.InvalidValue("URL", value, "a URI reference")

  extension (value: UriRef) def value: URI = value
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
end LanguageTag

opaque type RangeExpression = String
object RangeExpression:
  def from(value: String): Either[ValidationError, RangeExpression] = nonBlank("range", value)
  extension (value: RangeExpression) def value: String = value
end RangeExpression

opaque type XsdDateTime = String
object XsdDateTime:
  private val Pattern =
    "-?[0-9]{4,}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]+)?(Z|[+-][0-9]{2}:[0-9]{2})?".r

  def from(value: String): Either[ValidationError, XsdDateTime] =
    Either.cond(
      Pattern.matches(value),
      value,
      ValidationError.InvalidValue("dateTime", value, "an XSD dateTime lexical value"),
    )

  extension (value: XsdDateTime) def value: String = value
end XsdDateTime

opaque type XsdDuration = String
object XsdDuration:
  private val Pattern = "-?P(?=.+)([0-9]+Y)?([0-9]+M)?([0-9]+D)?(T([0-9]+H)?([0-9]+M)?([0-9]+(\\.[0-9]+)?S)?)?".r

  def from(value: String): Either[ValidationError, XsdDuration] =
    Either.cond(
      Pattern.matches(value),
      value,
      ValidationError.InvalidValue("duration", value, "an XSD duration lexical value"),
    )

  extension (value: XsdDuration) def value: String = value
end XsdDuration
