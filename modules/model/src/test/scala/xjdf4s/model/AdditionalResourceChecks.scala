package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

object AdditionalResourceChecks:
  private val signatureId = Nmtoken.from("signature-1").toOption.get

  val recursiveAssembly: Unit =
    val section = AssemblySection(signatureId)
    val resource: TypedSpecificResource = Assembly(AssemblyPlan.Listed(NonEmptyVector.one(section)))
    assert(resource.elementName.localName == "Assembly")

  val recursiveBundle: Unit =
    val item = BundleItem(amount = 10)
    val resource: FoundationalSpecificResource = Bundle(NonEmptyVector.one(item))
    assert(resource.elementName.localName == "Bundle")

  val constrainedCommonFolds: Unit =
    assert(CommonFolds.from(2).isRight)
    assert(CommonFolds.from(1).isLeft)
end AdditionalResourceChecks
