package xjdf4s.codec.xml

import xjdf4s.core.{NonEmptyVector, XjdfNamespace}

/** Decoder combinators shared by every node codec. */
object XmlDecoders:

  /** The concatenated text content of the element. */
  val textContent: XmlDecoder[String] =
    XmlDecoder.instance(element => Right(element.text))

  /** The raw value of the first attribute with the given local name, if present. */
  def attribute(name: String): XmlDecoder[Option[String]] =
    XmlDecoder.instance(element => Right(element.attribute(name)))

  /** Parses the first attribute with the given local name into a domain value, if present. */
  def attributeOf[A](name: String)(lex: Lexical.Lex[A]): XmlDecoder[Option[A]] =
    XmlDecoder.instance: element =>
      element.attribute(name) match
        case None => Right(None)
        case Some(raw) =>
          lex(raw)
            .left
            .map(message => XmlError.InvalidAttribute(element.name.localName, name, raw, message))
            .map(Some(_))

  /** Parses a required attribute. */
  def requiredAttribute[A](name: String)(lex: Lexical.Lex[A]): XmlDecoder[A] =
    XmlDecoder.instance: element =>
      attributeOf(name)(lex).decode(element).flatMap {
        case Some(value) => Right(value)
        case None        => Left(XmlError.MissingAttribute(element.name.localName, name))
      }

  /** The first child element with the given local name, if present. */
  def optionalChild[A](name: String)(decoder: XmlDecoder[A]): XmlDecoder[Option[A]] =
    XmlDecoder.instance: element =>
      element.childElements.find(_.name.localName == name) match
        case Some(child) => decoder.decode(child).map(Some(_))
        case None        => Right(None)

  /** A required child element. */
  def singleChild[A](name: String)(decoder: XmlDecoder[A]): XmlDecoder[A] =
    XmlDecoder.instance: element =>
      element.childElements.find(_.name.localName == name) match
        case Some(child) => decoder.decode(child)
        case None        => Left(XmlError.MissingElement(element.name.localName, name))

  /** All child elements with the given local name, in document order. */
  def repeatedChild[A](name: String)(decoder: XmlDecoder[A]): XmlDecoder[Vector[A]] =
    XmlDecoder.instance: element =>
      element.childElements
        .filter(_.name.localName == name)
        .foldLeft[Either[XmlError, Vector[A]]](Right(Vector.empty)) { (acc, child) =>
          for
            values <- acc
            value <- decoder.decode(child)
          yield values :+ value
        }

  /** One or more child elements with the given local name (`1..unbounded`). */
  def oneOrMoreChild[A](name: String)(decoder: XmlDecoder[A]): XmlDecoder[NonEmptyVector[A]] =
    XmlDecoder.instance: element =>
      repeatedChild(name)(decoder).decode(element).flatMap: values =>
        NonEmptyVector.from(values) match
          case Right(nonEmpty) => Right(nonEmpty)
          case Left(_)         => Left(XmlError.MissingElement(element.name.localName, name))

  /**
   * Rejects child elements outside the given standard local names. When `allowForeign` is set, children in a
   * foreign namespace are tolerated (they are collected separately by the parent decoder).
   */
  def expectChildrenOnly(names: Set[String], allowForeign: Boolean = false): XmlDecoder[Unit] =
    XmlDecoder.instance: element =>
      element.childElements.find: child =>
        !names.contains(child.name.localName) && !(allowForeign && child.name.namespace != XjdfNamespace.uri)
      match
        case Some(other) => Left(XmlError.UnexpectedElement(element.name.localName, other.name.localName))
        case None        => Right(())
end XmlDecoders
