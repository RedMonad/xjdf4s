package xjdf4s.model

import xjdf4s.core.*

object ProductIntentChecks:
  private val productId = XsdIdRef.from("product-1").toOption.get

  val assemblingIsStandard: Unit =
    val intent: StandardProductIntent = AssemblingIntent(productId)
    assert(intent.elementName.localName == "AssemblingIntent")

  val bindingDetailsMatchType: Unit =
    val binding: BindingSpecification = BindingSpecification.HardCover(Some(HardCoverBindingDetails()))
    val intent: StandardProductIntent = BindingIntent(binding)
    assert(intent.elementName.localName == "BindingIntent")

  val semanticCardinalities: Unit =
    assert(EvenPageCount.from(8).isRight)
    assert(EvenPageCount.from(7).isLeft)
    assert(TwoOrMore.from(Vector(productId)).isLeft)
end ProductIntentChecks
