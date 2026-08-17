package xjdf4s.codec.json

import io.circe.{Decoder, Encoder, HCursor, Json}

import xjdf4s.core.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * Dispatch for the specific-resource member of `Resource`: the member name is the resource element name and the
 * tables come from [[JsonRegistry]] (all standard resources with a JSON codec). Also holds the JSON-exception
 * codecs for `Address` and `Company` (9.10.2.1: their `AddressLine`/`OrganizationalUnit` text elements map to
 * arrays of strings instead of arrays of objects).
 */
object JsonResources:

  def nameOf(resource: SpecificResource): String = JsonRegistry.resourceName(resource)

  def encode(resource: SpecificResource): Json = JsonRegistry.encodeSpecificResource(resource)

  def decodeSpecific(cursor: HCursor): Decoder.Result[Option[SpecificResource]] =
    JsonRegistry.resourceNames.toVector.sorted.foldLeft[Decoder.Result[Option[SpecificResource]]](Right(None)) {
      (acc, name) =>
        acc match
          case found @ Right(Some(_)) => found
          case Right(None) =>
            cursor.downField(name).focus match
              case Some(json) => JsonRegistry.decodeSpecificResource(name, json).map(value => Some(value))
              case None       => Right(None)
          case failure => failure
    }

  /**
   * 9.10.2.1 JSON exception: the `AddressLine` text elements of `Address` map to an array of strings under the
   * `"AddressLine"` member, not to an array of objects.
   */
  given Encoder[Address] = Encoder.instance(address =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("AddressUsage", address.addressUsage),
        JsonHelpers.optMember("City", address.city),
        JsonHelpers.optMember("CivicNumber", address.civicNumber),
        JsonHelpers.optMember("Country", address.country),
        JsonHelpers.optMember("CountryCode", address.countryCode),
        JsonHelpers.optMember("ExtendedAddress", address.extendedAddress),
        JsonHelpers.optMember("PostalCode", address.postalCode),
        JsonHelpers.optMember("PostBox", address.postBox),
        JsonHelpers.optMember("Region", address.region),
        JsonHelpers.optMember("Street", address.street),
        JsonHelpers.vecMemberOf("AddressLine", address.addressLines)(Json.fromString),
      ),
    ),
  )
  given Decoder[Address] = Decoder.instance(cursor =>
    for
      addressUsage <- JsonHelpers.opt[Nmtoken](cursor, "AddressUsage")
      city <- JsonHelpers.opt[XjdfString](cursor, "City")
      civicNumber <- JsonHelpers.opt[XjdfString](cursor, "CivicNumber")
      country <- JsonHelpers.opt[XjdfString](cursor, "Country")
      countryCode <- JsonHelpers.opt[CountryCode](cursor, "CountryCode")
      extendedAddress <- JsonHelpers.opt[XjdfString](cursor, "ExtendedAddress")
      postalCode <- JsonHelpers.opt[XjdfString](cursor, "PostalCode")
      postBox <- JsonHelpers.opt[XjdfString](cursor, "PostBox")
      region <- JsonHelpers.opt[XjdfString](cursor, "Region")
      street <- JsonHelpers.opt[XjdfString](cursor, "Street")
      addressLines <- JsonHelpers.vec[String](cursor, "AddressLine")
    yield Address(
      addressUsage,
      city,
      civicNumber,
      country,
      countryCode,
      extendedAddress,
      postalCode,
      postBox,
      region,
      street,
      addressLines,
    ),
  )

  /** Same JSON exception for the `OrganizationalUnit` text elements of `Company`. */
  given Encoder[Company] = Encoder.instance(company =>
    JsonHelpers.obj(
      JsonHelpers.memberList(
        JsonHelpers.optMember("CompanyID", company.companyId),
        JsonHelpers.optMember("DescriptiveName", company.descriptiveName),
        Vector(JsonHelpers.member("OrganizationName", Json.fromString(company.organizationName.value))),
        JsonHelpers.vecMemberOf("OrganizationalUnit", company.organizationalUnits)(Json.fromString),
      ),
    ),
  )
  given Decoder[Company] = Decoder.instance(cursor =>
    for
      organizationName <- cursor.get[XjdfString]("OrganizationName")
      companyId <- JsonHelpers.opt[Nmtoken](cursor, "CompanyID")
      descriptiveName <- JsonHelpers.opt[XjdfString](cursor, "DescriptiveName")
      organizationalUnits <- JsonHelpers.vec[String](cursor, "OrganizationalUnit")
    yield Company(organizationName, companyId, descriptiveName, organizationalUnits),
  )
end JsonResources
