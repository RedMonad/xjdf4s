package xjdf4s
package model

import xjdf4s.intents.IntentPayload
import xjdf4s.prim.*
import cats.Show
import cats.data.Chain
import cats.kernel.Eq

/** The `Intent` element (Table 4.1): a container for one Product Intent. The
 *  element name of the specific intent SHALL be the value of `@Name` — an
 *  invariant of the whole intent (checked by `XJDF.validate` and law tests).
 */
final case class Intent(
    name: IntentName,
    specific: IntentPayload,
    descriptiveName: Option[XjdfString] = None,
    externalId: Option[NmToken] = None
) extends Named[IntentName]:

  /** True when `@Name` matches the local element name of the payload. */
  def isLawful: Boolean = name == specific.elementName

  /** All IDREFs used by this intent (e.g. `BindingIntent/@ChildRefs`). */
  def references: Chain[IdRef] = specific.references

  /** All document-scoped `@ID`s declared inside this intent (e.g.
   *  `ContentCheckIntent/ProofItem/@ID`, Table 4.24).
   */
  def declaredIds: Chain[Id] = specific.declaredIds
end Intent

object Intent:

  /** Table 4.1: `Intent/@Name` SHALL equal the specific intent's element name.
   *  Explicitly invoked from `TicketValidator.checkIntentLocalLaws`; not a
   *  `given` to keep the local-law registry grep-proof (N-18).
   */
  val nameLaw: DomainRule[Intent] =
    (value: Intent, at: XPath) =>
      if value.name == value.specific.elementName then Chain.empty
      else
        Chain.one(
          Issue.errorC(
            IssueCode.IntentNameMismatch,
            at,
            s"Intent/@Name='${value.name.toNmToken.value}' does not match payload element " +
              s"'${value.specific.elementName.value}' (Table 4.1)"
          )
        )

  given Show[Intent] =
    Show.show(i => s"Intent(${i.name.toNmToken.value})")

  given Eq[Intent] = Eq.fromUniversalEquals

end Intent

/** `Intent/@Name` (Table 4.1): the type of the Product Intent. Predefined names
 *  are listed in Table 4.2; extension intents may use a namespace prefix
 *  (§3.5.4).
 */
opaque type IntentName = NmToken

object IntentName:

  def from(raw: String): Option[IntentName] =
    NmToken.from(raw).flatMap { t =>
      t.value.indexOf(':') match
        case -1 => Some(t)
        case i => Option.when(i > 0 && i < t.value.length - 1)(t)
    }

  def unsafe(raw: String): IntentName =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid Intent name: '$raw'"))

  def of(token: NmToken): IntentName = token

  extension (name: IntentName)
    def toNmToken: NmToken = name
    def isExtension: Boolean = name.value.contains(':')

  given Show[IntentName] = Show.show(_.value)

  given Eq[IntentName] = Eq.fromUniversalEquals

end IntentName
