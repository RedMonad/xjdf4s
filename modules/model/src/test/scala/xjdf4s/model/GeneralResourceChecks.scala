package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object GeneralResourceChecks:
  private val contactId = XsdId.from("contact-1").toOption.get

  val requiredApprovalPeople: Unit =
    val person = ApprovalPerson(contactId)
    val resource: GeneralSpecificResource = ApprovalParams(NonEmptyVector.one(person))
    assert(resource.elementName.localName == "ApprovalParams")

  val verificationFileRoles: Unit =
    val result: TypedSpecificResource = VerificationResult(files = VerificationFiles())
    assert(result.elementName.localName == "VerificationResult")

  val pressResources: Unit =
    val resource: PressSpecificResource = DigitalPrintingParams(sides = Some(DigitalSides.TwoSided))
    assert(resource.elementName.localName == "DigitalPrintingParams")
end GeneralResourceChecks
