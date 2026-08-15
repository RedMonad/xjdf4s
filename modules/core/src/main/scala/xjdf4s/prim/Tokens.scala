package xjdf4s
package prim

import cats.Show
import cats.data.{Chain, NonEmptyChain}
import cats.kernel.Eq

/**
 * XJDF data type `NMTOKEN` (xsd:NMTOKEN): a continuous sequence of characters
 * without whitespace (Table A.1).
 *
 * NMTOKEN lists in XJDF are *open by design* (§1.1.1, §3.5.5): the recommended
 * values form a catalog, but vendor extensions are legal. This is why a closed
 * Scala `enum` is NOT sufficient here: `NmToken` is an opaque, validated String,
 * and the closed enumerations (`Usage`, `BindingType`, …) live beside it as
 * recommended sets. Openness itself is modelled with `NsPrefix` for foreign
 * namespace extensions (`foo:FooParams`, §3.5.2).
 */
opaque type NmToken = String

object NmToken:

  /** Validates `raw` as an NMTOKEN. */
  def from(raw: String): Option[NmToken] =
    Option(raw).filter(s => s.nonEmpty && !s.exists(_.isWhitespace))

  /** Raises `IllegalArgumentException` when `raw` is not a valid NMTOKEN. */
  def unsafe(raw: String): NmToken =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid NMTOKEN: '$raw'"))

  /** Splits an optional namespace prefix, e.g. `foo:FooParams` (§3.5.2). */
  def splitPrefix(raw: String): (Option[String], String) =
    raw.indexOf(':') match
      case -1 => (None, raw)
      case i  => (Some(raw.substring(0, i)), raw.substring(i + 1))

  extension (t: NmToken)
    def value: String = t
    def isExtension: Boolean = t.contains(':')

  given Show[NmToken] = Show.show(identity)

  given Eq[NmToken] = Eq.fromUniversalEquals

end NmToken

/**
 * XJDF data type `NMTOKENS`: a whitespace-separated list of one or more
 * NMTOKEN values (Table A.1). Modelled as a non-empty chain — the free monoid
 * over `NmToken` without the empty word.
 */
opaque type NmTokens = NonEmptyChain[NmToken]

object NmTokens:

  def of(head: NmToken, tail: NmToken*): NmTokens =
    NonEmptyChain(head, tail*)

  def from(chain: NonEmptyChain[NmToken]): NmTokens = chain

  def fromStrings(head: String, tail: String*): Option[NmTokens] =
    val parsed = (head +: tail).map(NmToken.from)
    if parsed.forall(_.isDefined) then
      Some(NonEmptyChain.fromChainUnsafe(Chain.fromSeq(parsed.map(_.get))))
    else None

  extension (tokens: NmTokens)
    def toChain: NonEmptyChain[NmToken] = tokens
    def toList: List[NmToken]           = tokens.toChain.toList
    def head: NmToken                   = tokens.head
    def contains(token: NmToken): Boolean = tokens.exists(_ == token)

  given Show[NmTokens] = Show.show(_.toList.map(_.value).mkString(" "))

  given Eq[NmTokens] = Eq.fromUniversalEquals

end NmTokens

/**
 * A namespace prefix of a foreign namespace extension (§3.5): `foo` in
 * `foo:FooParams`.
 */
opaque type NsPrefix = String

object NsPrefix:

  def from(raw: String): Option[NsPrefix] = NmToken.from(raw)

  def unsafe(raw: String): NsPrefix = raw

  extension (prefix: NsPrefix) def value: String = prefix

  given Show[NsPrefix] = Show.show(identity)

  given Eq[NsPrefix] = Eq.fromUniversalEquals

end NsPrefix

/**
 * XJDF data type `string` (Table A.1): normalized string of at most 1023
 * characters; tabs, line feeds and similar control characters are invalid.
 */
opaque type XjdfString = String

object XjdfString:

  val MaxLength = 1023

  def from(raw: String): Option[XjdfString] =
    Option(raw).filter: s =>
      s.length <= MaxLength && !s.exists: c =>
        c == '\t' || c == '\n' || c == '\r' || Character.isISOControl(c)

  def unsafe(raw: String): XjdfString =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid XJDF string: '$raw'"))

  extension (s: XjdfString) def value: String = s

  given Show[XjdfString] = Show.show(identity)

  given Eq[XjdfString] = Eq.fromUniversalEquals

end XjdfString

/** XJDF data type `language` (Table A.1), e.g. `en-US` ([RFC3066]). */
opaque type LanguageTag = String

object LanguageTag:

  private val Pattern = java.util.regex.Pattern.compile("[a-zA-Z]{2,3}(-[a-zA-Z0-9]{1,8})*")

  def from(raw: String): Option[LanguageTag] =
    Option(raw).filter(r => Pattern.matcher(r).matches())

  def unsafe(raw: String): LanguageTag =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid language tag: '$raw'"))

  extension (tag: LanguageTag) def value: String = tag

  given Show[LanguageTag] = Show.show(identity)

  given Eq[LanguageTag] = Eq.fromUniversalEquals

end LanguageTag

/** XJDF data type `text` (Table A.1): free text content of an element. */
opaque type CommentText = String

object CommentText:

  def apply(raw: String): CommentText = raw

  extension (text: CommentText) def value: String = text

  given Show[CommentText] = Show.show(identity)

  given Eq[CommentText] = Eq.fromUniversalEquals

end CommentText

/**
 * A simple XPath expression referencing an XJDF trait (§1.3). Used to locate
 * validation issues inside a ticket.
 */
opaque type XPath = String

object XPath:

  def apply(raw: String): XPath = raw

  extension (p: XPath) def value: String = p

  given Show[XPath] = Show.show(identity)

  given Eq[XPath] = Eq.fromUniversalEquals

end XPath

/**
 * Scala 3 trait parameters: a named element — the common surface of XJDF
 * elements that are identified by their `@Name` (ResourceSet, Intent).
 */
trait Named[N](val name: N)
