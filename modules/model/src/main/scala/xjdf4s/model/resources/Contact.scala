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
    city: Option[String] = None,
    civicNumber: Option[String] = None,
    country: Option[String] = None,
    countryCode: Option[CountryCode] = None,
    extendedAddress: Option[String] = None,
    postalCode: Option[String] = None,
    postBox: Option[String] = None,
    region: Option[String] = None,
    street: Option[String] = None,
    addressLines: Vector[String] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CommunicationChannel(
    channelType: Nmtoken,
    locator: String,
    channelUsage: Vector[Nmtoken] = Vector.empty,
    descriptiveName: Option[String] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Company(
    organizationName: String,
    companyId: Option[Nmtoken] = None,
    descriptiveName: Option[String] = None,
    organizationalUnits: Vector[String] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Person(
    additionalNames: Option[String] = None,
    descriptiveName: Option[String] = None,
    familyName: Option[String] = None,
    firstName: Option[String] = None,
    jobTitle: Option[String] = None,
    languages: Vector[LanguageTag] = Vector.empty,
    namePrefix: Option[String] = None,
    nameSuffix: Option[String] = None,
    phoneticFirstName: Option[String] = None,
    phoneticLastName: Option[String] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class Contact(
    contactTypeDetails: Vector[Nmtoken] = Vector.empty,
    costCenterId: Option[Nmtoken] = None,
    userId: Option[String] = None,
    address: Option[Address] = None,
    communicationChannels: Vector[CommunicationChannel] = Vector.empty,
    company: Option[Company] = None,
    person: Option[Person] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Contact")
