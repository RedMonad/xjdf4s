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

/** XJDF data type `text` (Table A.1): free text content of an element. */
opaque type CommentText = String

object CommentText:

  def apply(raw: String): CommentText = raw

  extension (text: CommentText) def value: String = text

  given Show[CommentText] = Show.show(identity)

  given Eq[CommentText] = Eq.fromUniversalEquals

end CommentText

/** A simple XPath expression referencing an XJDF trait (§1.3). Used to locate
 *  validation issues inside a ticket.
 */
opaque type XPath = String

object XPath:

  def apply(raw: String): XPath = raw

  extension (p: XPath) def value: String = p

  given Show[XPath] = Show.show(identity)

  given Eq[XPath] = Eq.fromUniversalEquals

end XPath

/** A stable, machine-readable identifier of a validation finding (ADR-0003,
 *  ADR-0006). Codecs (M2) and transport layers (M4) dispatch on the code
 *  rather than on human-readable messages. Codes are NMTOKEN tokens so they
 *  serialize directly as XML/JSON attributes.
 */
opaque type IssueCode = String

object IssueCode:

  def apply(raw: String): IssueCode = raw

  /** Constructs a code from a plain Scala string; the string must be a valid NMTOKEN. */
  def unsafe(raw: String): IssueCode =
    require(NmToken.from(raw).isDefined, s"Not a valid IssueCode (NMTOKEN): '$raw'")
    raw

  extension (code: IssueCode) def value: String = code

  given Show[IssueCode] = Show.show(identity)

  given Eq[IssueCode] = Eq.fromUniversalEquals

  // --- Well-known codes used by the core validator ---------------------------
  // Adding a code here is intentional: every new SHALL rule gets a stable code
  // so downstream consumers do not parse message strings.

  /** `@Version` is not `"2.2"` (Table 3.1). */
  val UnsupportedVersion: IssueCode = unsafe("XJDF-VERSION-UNSUPPORTED")

  /** `"Product"` is combined with process type tokens (§3.1.3). */
  val ProductTokenMixed: IssueCode = unsafe("XJDF-TYPES-PRODUCT-MIXED")

  /** A duplicate `"Product"` token in `@Types` (N-36, strict policy). */
  val ProductTokenDuplicate: IssueCode = unsafe("XJDF-TYPES-PRODUCT-DUPLICATE")

  /** `@RelatedJobPartID` without `@RelatedJobID` (Table 3.1). */
  val RelatedJobPartIdWithoutJobId: IssueCode = unsafe("XJDF-RELATED-JOBPARTID-WITHOUT-JOBID")

  /** Two `ResourceSet`s clash per §3.4 (same Name/Usage/ProcessUsage with
   *  common or no `@CombinedProcessIndex` entries). */
  val ResourceSetClash: IssueCode = unsafe("RESOURCESET-CLASH")

  /** A `Resource` payload element name does not match the parent `@Name`. */
  val ResourceSetChildNameMismatch: IssueCode = unsafe("RESOURCESET-CHILD-NAME-MISMATCH")

  /** `@Status` is specified on a `ResourceSet/@Usage="Output"` (Table 6.1). */
  val ResourceStatusOnOutput: IssueCode = unsafe("RESOURCE-STATUS-ON-OUTPUT")

  /** A `@CombinedProcessIndex` value is out of bounds for `@Types`. */
  val CombinedProcessIndexOutOfBounds: IssueCode = unsafe("COMBINED-PROCESS-INDEX-OUT-OF-BOUNDS")

  /** Duplicate document-scoped `@ID` (§2.2.3). */
  val DuplicateId: IssueCode = unsafe("ID-DUPLICATE")

  /** An `IDREF` does not resolve to a declared `@ID` (§2.2.3). */
  val DanglingIdRef: IssueCode = unsafe("IDREF-DANGLING")

  /** `AuditPool` is not ordered chronologically (§3.2). */
  val AuditNotChronological: IssueCode = unsafe("AUDIT-NOT-CHRONOLOGICAL")

  /** A PartAmount/Part repeats or mismatches a parent Partition Key (§6.1.2.1). */
  val PartKeyShadowsParent: IssueCode = unsafe("PART-KEY-SHADOWS-PARENT")

  /** `Intent/@Name` does not match the payload element name (Table 4.1). */
  val IntentNameMismatch: IssueCode = unsafe("INTENT-NAME-MISMATCH")

  /** A `Notification` carries a `Milestone` but `@Class` is not `"Event"` (Table 8.49). */
  val NotificationMilestoneClass: IssueCode = unsafe("NOTIFICATION-MILESTONE-CLASS")

  /** Multiple `Comment` elements in a container share a `@Language` (Table 8.49, N-38). */
  val CommentLanguageDuplicate: IssueCode = unsafe("COMMENT-LANGUAGE-DUPLICATE")

  /** BOM: a cycle in `Product/@ChildRefs` (§3.3.1.1). */
  val BomCycle: IssueCode = unsafe("BOM-CYCLE")

  /** BOM: an unresolved `@ChildRefs` target (§3.3.1.1). */
  val BomUnresolvedChildRef: IssueCode = unsafe("BOM-UNRESOLVED-CHILDREF")

  /** BOM: a ProductList has no root product. */
  val BomNoRoot: IssueCode = unsafe("BOM-NO-ROOT")

  /** Product `@PartVersion` disagrees between a child and its referencing root (Table 3.11, N-37). */
  val PartVersionMismatch: IssueCode = unsafe("PART-VERSION-MISMATCH")

  /** A local structural law of a model node is violated (ADR-0003). */
  val LocalLawViolation: IssueCode = unsafe("LOCAL-LAW-VIOLATION")

end IssueCode

/** Scala 3 trait parameter: a named element — the common surface of XJDF
 *  elements that are identified by their `@Name` (ResourceSet, Intent).
 *  The member is abstract; case-class subclasses supply it with their `name`
 *  parameter (a trait *constructor* parameter `(val name: N)` would clash with
 *  the case-class parameter of the same name and require an `override`).
 */
trait Named[N]:
  def name: N
