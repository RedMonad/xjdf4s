package xjdf4s
package resources

import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/**
 * The `Contact` resource (Table 6.38): a person or a role within an
 * organization. `Resource/Part/@ContactType` SHALL be provided for all
 * contacts (§5.1).
 */
final case class Contact(
  contactTypeDetails: Option[XjdfString] = None,
  costCenterId: Option[NmToken] = None,
  userId: Option[NmToken] = None,
  address: Option[Address] = None,
  comChannels: Chain[ComChannel] = Chain.empty,
  company: Option[Company] = None,
  person: Option[Person] = None
)

object Contact:

  given Eq[Contact] = Eq.fromUniversalEquals

end Contact

/** The `Address` element (Table 8.1), derived from the vCard format. */
final case class Address(
  addressUsage: Option[NmToken] = None,
  city: Option[XjdfString] = None,
  civicNumber: Option[XjdfString] = None,
  country: Option[XjdfString] = None,
  countryCode: Option[NmToken] = None,
  extendedAddress: Option[XjdfString] = None,
  postalCode: Option[XjdfString] = None,
  postBox: Option[XjdfString] = None,
  region: Option[XjdfString] = None,
  street: Option[XjdfString] = None,
  addressLines: Chain[XjdfString] = Chain.empty
)

object Address:
  given Eq[Address] = Eq.fromUniversalEquals

/** The `ComChannel` element (Table 6.39): a communication channel. */
final case class ComChannel(
  channelType: NmToken,
  channelUsage: Option[NmToken] = None,
  descriptiveName: Option[XjdfString] = None,
  locator: Option[XjdfString] = None
)

object ComChannel:
  given Eq[ComChannel] = Eq.fromUniversalEquals

/** The `Company` element (Table 6.40). */
final case class Company(
  companyId: Option[NmToken] = None,
  descriptiveName: Option[XjdfString] = None,
  organizationName: Option[XjdfString] = None,
  organizationalUnits: Chain[XjdfString] = Chain.empty
)

object Company:
  given Eq[Company] = Eq.fromUniversalEquals

/** The `Person` element (Table 6.42). */
final case class Person(
  additionalNames: Option[XjdfString] = None,
  descriptiveName: Option[XjdfString] = None,
  familyName: Option[XjdfString] = None,
  firstName: Option[XjdfString] = None,
  jobTitle: Option[XjdfString] = None,
  languages: Option[NmTokens] = None,
  namePrefix: Option[XjdfString] = None
)

object Person:
  given Eq[Person] = Eq.fromUniversalEquals
