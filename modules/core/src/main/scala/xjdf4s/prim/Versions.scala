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

/** `XJDF/@Version` (Table 3.1): the version of the XJDF document. For documents
 *  complying with this specification the value SHALL be `"2.2"` (enumeration
 *  `XJDFXJMFVersion`, Table A.2.51 — `2.0`; XJDF 2.2 is backwards compatible and
 *  keeps the `2.0` namespace/schema generation).
 */
opaque type XjdfVersion = String

object XjdfVersion:

  /** The version value mandated for documents complying with XJDF 2.2. */
  val V2_2: XjdfVersion = "2.2"

  def from(raw: String): Option[XjdfVersion] =
    Option(raw).filter(_ == "2.2")

  extension (v: XjdfVersion) def value: String = v

  given Show[XjdfVersion] = Show.show(identity)

  given Eq[XjdfVersion] = Eq.fromUniversalEquals

end XjdfVersion
