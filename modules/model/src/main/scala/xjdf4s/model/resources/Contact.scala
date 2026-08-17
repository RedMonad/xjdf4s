package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.{SpecificResource, XjdfNames}

opaque type CountryCode = String
object CountryCode:
  def from(value: String): Either[ValidationError, CountryCode] =
    Either.cond(
      value.matches("[A-Z][A-Z]"),
      value,
      ValidationError.InvalidValue("CountryCode", value, "two uppercase ASCII letters"),
    )

  extension (value: CountryCode) def value: String = value
end CountryCode

final case class Address(
    addressUsage: Option[Nmtoken] = None,
    city: Option[XjdfString] = None,
    civicNumber: Option[XjdfString] = None,
    country: Option[XjdfString] = None,
    countryCode: Option[CountryCode] = None,
    extendedAddress: Option[XjdfString] = None,
    postalCode: Option[XjdfString] = None,
    postBox: Option[XjdfString] = None,
    region: Option[XjdfString] = None,
    street: Option[XjdfString] = None,
    addressLines: Vector[String] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CommunicationChannel(
    channelType: Nmtoken,
    locator: XjdfString,
    channelUsage: Vector[Nmtoken] = Vector.empty,
    descriptiveName: Option[XjdfString] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Company(
    organizationName: XjdfString,
    companyId: Option[Nmtoken] = None,
    descriptiveName: Option[XjdfString] = None,
    organizationalUnits: Vector[String] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Person(
    additionalNames: Option[XjdfString] = None,
    descriptiveName: Option[XjdfString] = None,
    familyName: Option[XjdfString] = None,
    firstName: Option[XjdfString] = None,
    jobTitle: Option[XjdfString] = None,
    languages: Vector[LanguageTag] = Vector.empty,
    namePrefix: Option[XjdfString] = None,
    nameSuffix: Option[XjdfString] = None,
    phoneticFirstName: Option[XjdfString] = None,
    phoneticLastName: Option[XjdfString] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Contact(
    contactTypeDetails: Vector[Nmtoken] = Vector.empty,
    costCenterId: Option[Nmtoken] = None,
    userId: Option[XjdfString] = None,
    address: Option[Address] = None,
    communicationChannels: Vector[CommunicationChannel] = Vector.empty,
    company: Option[Company] = None,
    person: Option[Person] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Contact")
