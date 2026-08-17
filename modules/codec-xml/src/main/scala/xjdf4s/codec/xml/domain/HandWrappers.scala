package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.codec.xml.derivation.FieldCodec
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * Wires the hand-written codec objects into the typeclass-driven derivation: every hand-coded node gets an
 * [[XmlElementCodec]] (used by registries and derived parents) and a [[FieldCodec]] (used when the node appears as a
 * field of a derived node). Hand instances are more specific than the universal derivation, so they win.
 * All givens are top-level so that every file of this package sees them without imports.
 */
private def handBoth[A](name: String)(decoder: XmlDecoder[A], encoder: XmlEncoder[A])
      : (XmlElementCodec[A], FieldCodec[A]) =
    val codec = XmlElementCodec.instance(name)(decoder.decode, encoder.encode)
    (codec, FieldCodec.element(codec))

  given commentCodec: XmlElementCodec[Comment] = handBoth("Comment")(CommentCodec.decoder, CommentCodec.encoder)._1
  given commentField: FieldCodec[Comment] = FieldCodec.element(summon[XmlElementCodec[Comment]])

  given generalIdCodec: XmlElementCodec[GeneralId] = handBoth("GeneralID")(GeneralIdCodec.decoder, GeneralIdCodec.encoder)._1
  given generalIdField: FieldCodec[GeneralId] = FieldCodec.element(summon[XmlElementCodec[GeneralId]])

  given partCodec: XmlElementCodec[Part] = handBoth("Part")(PartCodec.decoder, PartCodec.encoder)._1
  given partField: FieldCodec[Part] = FieldCodec.element(summon[XmlElementCodec[Part]])

  given partWasteCodec: XmlElementCodec[PartWaste] = handBoth("PartWaste")(PartWasteCodec.decoder, PartWasteCodec.encoder)._1
  given partWasteField: FieldCodec[PartWaste] = FieldCodec.element(summon[XmlElementCodec[PartWaste]])

  given partAmountCodec: XmlElementCodec[PartAmount] =
    handBoth("PartAmount")(PartAmountCodec.decoder, PartAmountCodec.encoder)._1
  given partAmountField: FieldCodec[PartAmount] = FieldCodec.element(summon[XmlElementCodec[PartAmount]])

  given amountPoolCodec: XmlElementCodec[AmountPool] =
    handBoth("AmountPool")(AmountPoolCodec.decoder, AmountPoolCodec.encoder)._1
  given amountPoolField: FieldCodec[AmountPool] = FieldCodec.element(summon[XmlElementCodec[AmountPool]])

  given dependentCodec: XmlElementCodec[Dependent] =
    handBoth("Dependent")(DependentCodec.decoder, DependentCodec.encoder)._1
  given dependentField: FieldCodec[Dependent] = FieldCodec.element(summon[XmlElementCodec[Dependent]])

  given resourceCodec: XmlElementCodec[Resource] = handBoth("Resource")(ResourceCodec.decoder, ResourceCodec.encoder)._1
  given resourceField: FieldCodec[Resource] = FieldCodec.element(summon[XmlElementCodec[Resource]])

  given resourceSetCodec: XmlElementCodec[ResourceSet] =
    handBoth("ResourceSet")(ResourceSetCodec.decoder, ResourceSetCodec.encoder)._1
  given resourceSetField: FieldCodec[ResourceSet] = FieldCodec.element(summon[XmlElementCodec[ResourceSet]])

  given xjdfCodec: XmlElementCodec[XJDF] = handBoth("XJDF")(XjdfCodec.decoder, XjdfCodec.encoder)._1

  given xjmfCodec: XmlElementCodec[XJMF] = handBoth("XJMF")(XjmfCodec.decoder, XjmfCodec.encoder)._1

  given headerCodec: XmlElementCodec[Header] = handBoth("Header")(HeaderCodec.decoder, HeaderCodec.encoder)._1
  given headerField: FieldCodec[Header] = FieldCodec.element(summon[XmlElementCodec[Header]])

  given subscriptionCodec: XmlElementCodec[Subscription] =
    handBoth("Subscription")(SubscriptionCodec.decoder, SubscriptionCodec.encoder)._1
  given subscriptionField: FieldCodec[Subscription] = FieldCodec.element(summon[XmlElementCodec[Subscription]])

  given resourceQuParamsCodec: XmlElementCodec[ResourceQuParams] =
    handBoth("ResourceQuParams")(ResourceQuParamsCodec.decoder, ResourceQuParamsCodec.encoder)._1
  given resourceQuParamsField: FieldCodec[ResourceQuParams] =
    FieldCodec.element(summon[XmlElementCodec[ResourceQuParams]])

  given resourceInfoCodec: XmlElementCodec[ResourceInfo] =
    handBoth("ResourceInfo")(ResourceInfoCodec.decoder, ResourceInfoCodec.encoder)._1
  given resourceInfoField: FieldCodec[ResourceInfo] = FieldCodec.element(summon[XmlElementCodec[ResourceInfo]])

  given messageServiceCodec: XmlElementCodec[MessageService] =
    handBoth("MessageService")(MessageServiceCodec.decoder, MessageServiceCodec.encoder)._1
  given messageServiceField: FieldCodec[MessageService] = FieldCodec.element(summon[XmlElementCodec[MessageService]])

  given glueCodec: XmlElementCodec[Glue] = handBoth("Glue")(GlueCodec.decoder, GlueCodec.encoder)._1
  given glueField: FieldCodec[Glue] = FieldCodec.element(summon[XmlElementCodec[Glue]])

  given mediaLayersCodec: XmlElementCodec[MediaLayers] =
    handBoth("MediaLayers")(MediaLayersCodec.decoder, MediaLayersCodec.encoder)._1
  given mediaLayersField: FieldCodec[MediaLayers] = FieldCodec.element(summon[XmlElementCodec[MediaLayers]])

  given conditionsCodec: XmlElementCodec[ColorMeasurementConditions] =
    handBoth("ColorMeasurementConditions")(ColorMeasurementConditionsCodec.decoder, ColorMeasurementConditionsCodec.encoder)._1
  given conditionsField: FieldCodec[ColorMeasurementConditions] =
    FieldCodec.element(summon[XmlElementCodec[ColorMeasurementConditions]])

  given mediaCodec: XmlElementCodec[Media] = handBoth("Media")(MediaCodec.decoder, MediaCodec.encoder)._1
  given mediaField: FieldCodec[Media] = FieldCodec.element(summon[XmlElementCodec[Media]])

  given colorCodec: XmlElementCodec[Color] = handBoth("Color")(ColorCodec.decoder, ColorCodec.encoder)._1
  given colorField: FieldCodec[Color] = FieldCodec.element(summon[XmlElementCodec[Color]])

  given componentCodec: XmlElementCodec[Component] =
    handBoth("Component")(ComponentCodec.decoder, ComponentCodec.encoder)._1
  given componentField: FieldCodec[Component] = FieldCodec.element(summon[XmlElementCodec[Component]])

  given deviceCodec: XmlElementCodec[Device] = handBoth("Device")(DeviceCodec.decoder, DeviceCodec.encoder)._1
  given deviceField: FieldCodec[Device] = FieldCodec.element(summon[XmlElementCodec[Device]])

  given toolCodec: XmlElementCodec[Tool] = handBoth("Tool")(ToolCodec.decoder, ToolCodec.encoder)._1
  given toolField: FieldCodec[Tool] = FieldCodec.element(summon[XmlElementCodec[Tool]])

  given queryKnownMessagesCodec: XmlElementCodec[QueryKnownMessages] =
    handBoth("QueryKnownMessages")(QueryKnownMessagesCodec.decoder, QueryKnownMessagesCodec.encoder)._1

  given queryResourceCodec: XmlElementCodec[QueryResource] =
    handBoth("QueryResource")(QueryResourceCodec.decoder, QueryResourceCodec.encoder)._1

  given responseKnownMessagesCodec: XmlElementCodec[ResponseKnownMessages] =
    handBoth("ResponseKnownMessages")(ResponseKnownMessagesCodec.decoder, ResponseKnownMessagesCodec.encoder)._1

  given responseResourceCodec: XmlElementCodec[ResponseResource] =
    handBoth("ResponseResource")(ResponseResourceCodec.decoder, ResponseResourceCodec.encoder)._1

  given signalResourceCodec: XmlElementCodec[SignalResource] =
    handBoth("SignalResource")(SignalResourceCodec.decoder, SignalResourceCodec.encoder)._1
