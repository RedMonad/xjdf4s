package xjdf4s
package prim

import cats.Show
import cats.data.{Chain, NonEmptyChain}
import cats.kernel.Eq

/** XJDF data type `NMTOKEN` (xsd:NMTOKEN): a continuous sequence of characters
 *  without whitespace (Table A.1).
 *
 *  NMTOKEN lists in XJDF are *open by design* (§1.1.1, §3.5.5): the recommended
 *  values form a catalog, but vendor extensions are legal. This is why a closed
 *  Scala `enum` is NOT sufficient here: `NmToken` is an opaque, validated String,
 *  and the closed enumerations (`Usage`, `BindingType`, …) live beside it as
 *  recommended sets. Openness itself is modelled with `NsPrefix` for foreign
 *  namespace extensions (`foo:FooParams`, §3.5.2).
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
      case i => (Some(raw.substring(0, i)), raw.substring(i + 1))

  extension (t: NmToken)
    def value: String = t
    def isExtension: Boolean = t.contains(':')

  given Show[NmToken] = Show.show(identity)

  given Eq[NmToken] = Eq.fromUniversalEquals

end NmToken

/** XJDF data type `NMTOKENS`: a whitespace-separated list of one or more
 *  NMTOKEN values (Table A.1). Modelled as a non-empty chain — the free monoid
 *  over `NmToken` without the empty word.
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
    /** The underlying non-empty chain (representation). */
    def toNonEmptyChain: NonEmptyChain[NmToken] = tokens
    def toList: List[NmToken] = tokens.toNonEmptyChain.toChain.toList
    def contains(token: NmToken): Boolean = tokens.toNonEmptyChain.exists(_ == token)

  given Show[NmTokens] = Show.show(tokens => tokens.toNonEmptyChain.toChain.toList.mkString(" "))

  given Eq[NmTokens] = Eq.fromUniversalEquals

end NmTokens

/** A namespace prefix of a foreign namespace extension (§3.5): `foo` in
 *  `foo:FooParams`.
 */
opaque type NsPrefix = String

object NsPrefix:

  def from(raw: String): Option[NsPrefix] = NmToken.from(raw)

  def unsafe(raw: String): NsPrefix = raw

  extension (prefix: NsPrefix) def value: String = prefix

  given Show[NsPrefix] = Show.show(identity)

  given Eq[NsPrefix] = Eq.fromUniversalEquals

end NsPrefix

/** XJDF data type `string` (Table A.1): normalized string of at most 1023
 *  characters; tabs, line feeds and similar control characters are invalid.
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

/** XJDF data type `hexBinary` (§A.1 / Table A.1): arbitrary binary data
 *  represented by pairs of hexadecimal digits.
 *
 *  Both upper- and lower-case digits are valid and an empty representation is
 *  valid. The `xsd:hexBinary` white-space facet is fixed to `collapse`; case is
 *  preserved here so the domain remains lossless, while canonical wire casing
 *  belongs to codecs (ADR-0010).
 */
opaque type HexBinary = String

object HexBinary:

  /** Validates and white-space-normalizes an `xsd:hexBinary` lexical value. */
  def from(raw: String): Option[HexBinary] =
    Option(raw).map(collapseXmlWhitespace).filter: value =>
      value.length % 2 == 0 && value.forall(isHexDigit)

  /** Raises `IllegalArgumentException` when `raw` is not valid `xsd:hexBinary`. */
  def unsafe(raw: String): HexBinary =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid hexBinary value: '$raw'"))

  extension (binary: HexBinary) def value: String = binary

  given Show[HexBinary] = Show.show(identity)

  given Eq[HexBinary] = Eq.fromUniversalEquals

  private def isHexDigit(char: Char): Boolean =
    char >= '0' && char <= '9' || char >= 'a' && char <= 'f' || char >= 'A' && char <= 'F'

  private def collapseXmlWhitespace(raw: String): String =
    val out = new StringBuilder(raw.length)
    var pendingSpace = false
    raw.foreach: char =>
      if isXmlWhitespace(char) then
        if out.nonEmpty then pendingSpace = true
      else
        if pendingSpace then out.append(' ')
        out.append(char)
        pendingSpace = false
    out.result()

  private def isXmlWhitespace(char: Char): Boolean =
    char == ' ' || char == '\t' || char == '\n' || char == '\r'

end HexBinary

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

/** XJDF data type `regExp` (Table A.1): a regular expression as defined by
 *  `[XMLSchema]`. Used e.g. by `Part/@Metadata` (Table 6.4).
 *
 *  The XSD regular-expression grammar differs from `java.util.regex`
 *  (character-class subtraction `[a-z-[aeiou]]` vs `&&`, no backreferences or
 *  lookaround), so compatibility with the Java engine cannot be claimed and
 *  validation is deliberately limited to non-emptiness (ROADMAP risk R5,
 *  Appendix C). Grammar-level validation is a codec concern (M2).
 */
opaque type RegExp = String

object RegExp:

  /** Validates `raw` as a non-empty regExp. */
  def from(raw: String): Option[RegExp] =
    Option(raw).filter(_.nonEmpty)

  /** Raises `IllegalArgumentException` when `raw` is empty. */
  def unsafe(raw: String): RegExp =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid regExp: '$raw'"))

  extension (r: RegExp) def value: String = r

  given Show[RegExp] = Show.show(_.value)

  given Eq[RegExp] = Eq.fromUniversalEquals

end RegExp

/** XJDF data type `XPath` (Table A.1): an XPath expression encoded as an
 *  `xsd:token`.
 *
 *  The Scala name is deliberately `XjdfXPath`: `xjdf4s.model.XPath` is an
 *  unrelated validation-issue locator (ADR-0002), not a value carried by an
 *  XJDF attribute. Keeping distinct nominal names prevents wildcard imports
 *  from silently mixing the two concepts (ADR-0013, N-54).
 *
 *  Table A.1 specifies `xsd:token`, whose white-space facet is `collapse`.
 *  `schema.xsd` instead restricts `XPath` from `xs:string`; ADR-0013 follows
 *  the higher-priority prose and records that discrepancy. Full XPath grammar
 *  validation is deferred to the codec boundary; the domain constructor
 *  enforces the lexical `xsd:token` boundary and non-emptiness.
 */
opaque type XjdfXPath = String

object XjdfXPath:

  /** Constructs a canonical non-empty XPath value, applying the `xsd:token`
   *  white-space collapse (`#x9`, `#xA`, `#xD`, `#x20` → one space).
   */
  def from(raw: String): Option[XjdfXPath] =
    Option(raw).map(collapseXmlWhitespace).filter(_.nonEmpty)

  /** Raises `IllegalArgumentException` when `raw` does not contain an XPath expression. */
  def unsafe(raw: String): XjdfXPath =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid XPath expression: '$raw'"))

  extension (path: XjdfXPath) def value: String = path

  given Show[XjdfXPath] = Show.show(identity)

  given Eq[XjdfXPath] = Eq.fromUniversalEquals

  private def collapseXmlWhitespace(raw: String): String =
    val out = new StringBuilder(raw.length)
    var pendingSpace = false
    raw.foreach: char =>
      if isXmlWhitespace(char) then
        if out.nonEmpty then pendingSpace = true
      else
        if pendingSpace then out.append(' ')
        out.append(char)
        pendingSpace = false
    out.result()

  private def isXmlWhitespace(char: Char): Boolean =
    char == ' ' || char == '\t' || char == '\n' || char == '\r'

end XjdfXPath

/** XJDF data type `text` (Table A.1): free text content of an element. */
opaque type CommentText = String

object CommentText:

  def apply(raw: String): CommentText = raw

  extension (text: CommentText) def value: String = text

  given Show[CommentText] = Show.show(identity)

  given Eq[CommentText] = Eq.fromUniversalEquals

end CommentText

/** Scala 3 trait parameter: a named element — the common surface of XJDF
 *  elements that are identified by their `@Name` (ResourceSet, Intent).
 *  The member is abstract; case-class subclasses supply it with their `name`
 *  parameter (a trait *constructor* parameter `(val name: N)` would clash with
 *  the case-class parameter of the same name and require an `override`).
 */
trait Named[N]:
  def name: N
