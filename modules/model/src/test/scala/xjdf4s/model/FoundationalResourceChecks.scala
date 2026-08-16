package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object FoundationalResourceChecks:
  private val customerId = Nmtoken.from("customer-1").toOption.get
  private val mediaId = XsdId.from("media-1").toOption.get

  val foundationalUnion: Unit =
    val customer: FoundationalSpecificResource = CustomerInfo(customerId = Some(customerId))
    val media: FoundationalSpecificResource = ExposedMedia(mediaRef = mediaId)
    assert(customer.elementName.localName == "CustomerInfo")
    assert(media.elementName.localName == "ExposedMedia")

  val countryCodeValidation: Unit =
    assert(CountryCode.from("EE").isRight)
    assert(CountryCode.from("Estonia").isLeft)
end FoundationalResourceChecks
