package xjdf4s
package prim

import cats.Show
import cats.kernel.Eq

/** ICS conformance version (§3.1.1), value format:
 *  `<ICSName>_L<ICSLevel>-<ICSVersion>`, e.g. `MISPRE_L1-2.0`.
 */
opaque type IcsVersion = String

object IcsVersion:

  private val Pattern =
    java.util.regex.Pattern.compile("[A-Za-z0-9]+_L[0-9]+-[0-9]+(\\.[0-9]+)*")

  def from(raw: String): Option[IcsVersion] =
    Option(raw).filter(r => Pattern.matcher(r).matches())

  def unsafe(raw: String): IcsVersion =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid ICS version: '$raw'"))

  extension (v: IcsVersion) def value: String = v

  given Show[IcsVersion] = Show.show(identity)

  given Eq[IcsVersion] = Eq.fromUniversalEquals

end IcsVersion

/** `XJDF/@Version` (Table 3.1): the version of the XJDF document.
 *
 *  Two different normative tables describe versions, and they describe
 *  different things:
 *
 *  - Table A.52 (`XJDFXJMFVersion` Enumeration Values) lists the *type's*
 *    vocabulary: `2.0`, `2.1` *(New in XJDF 2.1)*, `2.2` *(New in XJDF 2.2)*.
 *  - Table 3.1 restricts the *root document*: «The value of `@Version` SHALL be
 *    `"2.2"` for documents that comply to this specification.»
 *
 *  This library models documents that comply to XJDF 2.2, so only `"2.2"` is
 *  accepted. Supporting `2.0`/`2.1` documents would be a separate decision
 *  (outside M1); it is not silently folded into this parser.
 */
opaque type XjdfVersion = String

object XjdfVersion:

  /** The version value mandated for documents complying with XJDF 2.2. */
  val V2_2: XjdfVersion = "2.2"

  /** Parses an XJDF version string.
   *
   *  Table A.52 lists `2.0`, `2.1`, `2.2` as valid values.
   *  However, Table 3.1 requires: «The value of `@Version` SHALL be `"2.2"` for
   *  documents that comply to this specification.»
   *
   *  This parser accepts only `"2.2"` for conformant documents.
   *  Support for `2.0`/`2.1` would require a separate decision (outside M1).
   */
  def from(raw: String): Option[XjdfVersion] =
    Option(raw).filter(_ == "2.2")

  extension (v: XjdfVersion) def value: String = v

  given Show[XjdfVersion] = Show.show(identity)

  given Eq[XjdfVersion] = Eq.fromUniversalEquals

end XjdfVersion
