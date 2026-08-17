package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * Dispatch registries for the open substitution points. Coverage slice implemented so far:
 *
 *  - resources: Color, Component, Device, Media, Tool;
 *  - messages: QueryKnownMessages, QueryResource, ResponseKnownMessages, ResponseResource, SignalResource.
 *
 * A standard XJDF name without a decoder fails with `UnsupportedElement` (never silently degrades); foreign
 * resource elements decode losslessly into `NamedSpecificResource`. Foreign messages are not yet distinguishable by
 * family from the element alone and fail loudly as well.
 */
object Registry:

  private def name(localName: String): QualifiedName = QualifiedName(XjdfNamespace.uri, localName)

  val resourceDecoders: Map[QualifiedName, XmlDecoder[SpecificResource]] = Map(
    name("Color")     -> XmlDecoder.widen(ColorCodec.decoder),
    name("Component") -> XmlDecoder.widen(ComponentCodec.decoder),
    name("Device")    -> XmlDecoder.widen(DeviceCodec.decoder),
    name("Media")     -> XmlDecoder.widen(MediaCodec.decoder),
    name("Tool")      -> XmlDecoder.widen(ToolCodec.decoder),
  )

  val messageDecoders: Map[QualifiedName, XmlDecoder[Message]] = Map(
    name("QueryKnownMessages")    -> XmlDecoder.widen(QueryKnownMessagesCodec.decoder),
    name("QueryResource")         -> XmlDecoder.widen(QueryResourceCodec.decoder),
    name("ResponseKnownMessages") -> XmlDecoder.widen(ResponseKnownMessagesCodec.decoder),
    name("ResponseResource")      -> XmlDecoder.widen(ResponseResourceCodec.decoder),
    name("SignalResource")        -> XmlDecoder.widen(SignalResourceCodec.decoder),
  )

  def decodeSpecificResource(element: Xml.Element): Either[XmlError, SpecificResource] =
    resourceDecoders.get(element.name) match
      case Some(decoder) => decoder.decode(element)
      case None if element.name.namespace == XjdfNamespace.uri =>
        Left(XmlError.UnsupportedElement(element.name.localName))
      case None =>
        ForeignQName
          .from(element.name.namespace, element.name.localName, element.name.prefix)
          .left
          .map(_ => XmlError.ForeignNameExpected(element.name.localName))
          .flatMap: foreignName =>
            ForeignCodec.decodeForeignElement(element).map: extension =>
              NamedSpecificResource(foreignName, Extensions(elements = Vector(extension)))

  def encodeSpecificResource(resource: SpecificResource): Xml.Element =
    resource match
      case color: Color         => ColorCodec.encoder.encode(color)
      case component: Component => ComponentCodec.encoder.encode(component)
      case device: Device       => DeviceCodec.encoder.encode(device)
      case media: Media         => MediaCodec.encoder.encode(media)
      case tool: Tool           => ToolCodec.encoder.encode(tool)
      case named: NamedSpecificResource =>
        named.extensions.elements match
          case Vector(single) => ForeignCodec.encodeForeignElement(single)
          case _ =>
            throw new UnsupportedOperationException(
              "NamedSpecificResource without exactly one foreign element cannot be encoded",
            )
      case other =>
        throw new UnsupportedOperationException(s"no encoder for ${other.getClass.getName} in this codec slice")

  def decodeMessage(element: Xml.Element): Either[XmlError, Message] =
    messageDecoders.get(element.name) match
      case Some(decoder) => decoder.decode(element)
      case None          => Left(XmlError.UnsupportedElement(element.name.localName))

  def encodeMessage(message: Message): Xml.Element =
    message match
      case query: QueryKnownMessages    => QueryKnownMessagesCodec.encoder.encode(query)
      case query: QueryResource         => QueryResourceCodec.encoder.encode(query)
      case response: ResponseKnownMessages => ResponseKnownMessagesCodec.encoder.encode(response)
      case response: ResponseResource   => ResponseResourceCodec.encoder.encode(response)
      case signal: SignalResource       => SignalResourceCodec.encoder.encode(signal)
      case other =>
        throw new UnsupportedOperationException(s"no encoder for ${other.getClass.getName} in this codec slice")
end Registry
