package xjdf4s.codec.xml

/** Decodes an XML element into a domain value. Failures are [[XmlError]]s, fast-failing per decoder call. */
trait XmlDecoder[A]:
  def decode(element: Xml.Element): Either[XmlError, A]
end XmlDecoder

object XmlDecoder:
  def instance[A](decodeFunction: Xml.Element => Either[XmlError, A]): XmlDecoder[A] =
    new XmlDecoder[A]:
      def decode(element: Xml.Element): Either[XmlError, A] = decodeFunction(element)

  /** Widens a decoder to a supertype, for registration under an open trait (e.g. `SpecificResource`). */
  def widen[A, B >: A](decoder: XmlDecoder[A]): XmlDecoder[B] =
    XmlDecoder.instance(element => decoder.decode(element).map(value => value))
end XmlDecoder

/** Encodes a domain value as an XML element. */
trait XmlEncoder[A]:
  def encode(value: A): Xml.Element
end XmlEncoder

object XmlEncoder:
  def instance[A](encodeFunction: A => Xml.Element): XmlEncoder[A] =
    new XmlEncoder[A]:
      def encode(value: A): Xml.Element = encodeFunction(value)
end XmlEncoder
