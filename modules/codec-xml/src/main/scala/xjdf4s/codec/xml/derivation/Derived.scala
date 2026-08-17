package xjdf4s.codec.xml.derivation

import scala.compiletime.{constValue, constValueTuple, summonAll}
import scala.deriving.Mirror
import scala.reflect.ClassTag

import xjdf4s.codec.xml.*
import xjdf4s.core.*

/**
 * Compile-time derivation of [[XmlElementCodec]] for case classes.
 *
 * Every case-class field is serialized through its [[FieldCodec]] instance:
 *
 *  - attribute fields read/write the attribute named by [[Names.attributeName]];
 *  - element fields read/write children selected by the element name of the inner codec;
 *  - `extensions` collects foreign attributes and (for derived nodes) foreign children;
 *  - `foreignElements` (where present) receives foreign children directly;
 *  - fields with default values are optional: a missing attribute/child falls back to the default, obtained
 *    reflectively from the companion `apply$default$N` methods;
 *  - unexpected standard children are rejected (unless a wildcard field codec claims them).
 */
object Derived:

  inline given derived[A <: Product](using m: Mirror.ProductOf[A], ct: ClassTag[A]): XmlElementCodec[A] =
    val labels = constValueTuple[m.MirroredElemLabels]
    val codecs = summonAll[Tuple.Map[m.MirroredElemTypes, FieldCodec]]
    new DerivedCodec[A](
      m,
      labels,
      codecs,
      Names.elementName(constValue[m.MirroredLabel]),
      Defaults.of(ct),
    )

  /** Same derivation with an explicit element name (for names the naming policy cannot derive, e.g. TIFFtag). */
  inline def derivedNamed[A <: Product](elementName: String)(using
      m: Mirror.ProductOf[A],
      ct: ClassTag[A],
  ): XmlElementCodec[A] =
    val labels = constValueTuple[m.MirroredElemLabels]
    val codecs = summonAll[Tuple.Map[m.MirroredElemTypes, FieldCodec]]
    new DerivedCodec[A](m, labels, codecs, elementName, Defaults.of(ct))
end Derived

/** Default constructor values of a case class, read reflectively from the companion's `apply$default$N` methods. */
final class Defaults private (values: Vector[Option[Any]]):

  def get(index: Int): Option[Any] =
    if index >= 0 && index < values.length then values(index) else None
end Defaults

object Defaults:
  def of(ct: ClassTag[?]): Defaults =
    val values =
      try
        val companionClass = Class.forName(ct.runtimeClass.getName + "$")
        val module = companionClass.getField("MODULE$").get(null)
        (1 to 64).toVector.map: i =>
          companionClass.getMethods
            .find(_.getName == s"apply$$default$$$i")
            .map(_.invoke(module))
      catch case _: Exception => Vector.empty
    new Defaults(values)
end Defaults

/**
 * Runtime engine of the derivation. The mirror, the field labels and the field codecs are produced at compile time
 * by [[Derived.derived]]; decoding and encoding are plain runtime loops over the fields.
 */
final class DerivedCodec[A](
    mirror: Mirror.ProductOf[A],
    labels: Tuple,
    codecs: Tuple,
    val elementName: String,
    defaults: Defaults,
) extends XmlElementCodec[A]:

  private val arity: Int = labels.productArity

  private def labelAt(index: Int): String = labels.productElement(index).asInstanceOf[String]

  private def codecAt(index: Int): FieldCodec[Any] = codecs.productElement(index).asInstanceOf[FieldCodec[Any]]

  def decode(element: Xml.Element): Either[XmlError, A] =
    val standardChildren = element.childElements.filter(_.name.namespace == XjdfNamespace.uri)
    val foreignChildren = element.childElements.filter(_.name.namespace != XjdfNamespace.uri)
    val decodedForeign = decodeForeignElements(foreignChildren)
    val unexpected = unexpectedChild(elementName, standardChildren)
    val values = new Array[Any](arity)
    var index = 0
    var failure: Option[XmlError] = None
    while index < arity && failure.isEmpty do
      val label = labelAt(index)
      val codec = codecAt(index)
      label match
        case "extensions" =>
          values(index) = Extensions(
            attributes = foreignAttributes(element),
            elements = if hasForeignElementsField then Vector.empty else decodedForeign,
          )
        case "foreignElements" =>
          values(index) = decodedForeign
        case _ =>
          if codec.isElement then
            val children =
              if codec.elementName.isEmpty then standardChildren
              else standardChildren.filter(_.name.localName == codec.elementName)
            codec.decodeElements(children) match
              case Right(value) => values(index) = value
              case Left(error) =>
                defaults.get(index) match
                  case Some(default) => values(index) = default
                  case None          => failure = Some(error)
          else
            codec.decodeAttribute(element.attribute(Names.attributeName(label))) match
              case Right(value) => values(index) = value
              case Left(_) =>
                defaults.get(index) match
                  case Some(default) => values(index) = default
                  case None => failure = Some(XmlError.MissingAttribute(elementName, Names.attributeName(label)))
      index += 1
    for
      _ <- unexpected
      _ <- failure.toLeft(())
    yield mirror.fromProduct(Tuple.fromArray(values.asInstanceOf[Array[Object]]).asInstanceOf[Product])

  def encode(value: A): Xml.Element =
    val product = value.asInstanceOf[Product]
    val attributes = Vector.newBuilder[(QualifiedName, String)]
    val children = Vector.newBuilder[Xml.Element]
    var index = 0
    while index < arity do
      val label = labelAt(index)
      val codec = codecAt(index)
      val fieldValue = product.productElement(index).asInstanceOf[Any]
      label match
        case "extensions" =>
          val extensions = fieldValue.asInstanceOf[Extensions]
          attributes ++= extensions.attributes.toVector
            .sortBy(pair => (pair._1.namespace, pair._1.localName))
            .map { case (name, raw) => (name, ForeignCodec.renderExtensionValue(raw)) }
          children ++= extensions.elements.map(ForeignCodec.encodeForeignElement)
        case "foreignElements" =>
          children ++= fieldValue.asInstanceOf[Vector[ExtensionElement]].map(ForeignCodec.encodeForeignElement)
        case _ =>
          if codec.isElement then children ++= codec.encodeElements(fieldValue)
          else
            codec.renderAttribute(fieldValue).foreach: rendered =>
              attributes += ((QualifiedName(XjdfNamespace.uri, Names.attributeName(label)), rendered))
      index += 1
    Xml.Element(QualifiedName(XjdfNamespace.uri, elementName), attributes.result(), children.result())

  // -- helpers -----------------------------------------------------------------

  private def foreignAttributes(element: Xml.Element): Map[QualifiedName, ExtensionValue] =
    element.attributes
      .collect { case (name, raw) if name.namespace != XjdfNamespace.uri => (name, ExtensionValue.Text(raw)) }
      .toMap

  private def hasForeignElementsField: Boolean =
    var index = 0
    var found = false
    while index < arity && !found do
      found = labelAt(index) == "foreignElements"
      index += 1
    found
  end hasForeignElementsField

  private def decodeForeignElements(children: Vector[Xml.Element]): Vector[ExtensionElement] =
    children
      .foldLeft[Either[XmlError, Vector[ExtensionElement]]](Right(Vector.empty)) { (acc, child) =>
        for
          elements <- acc
          decoded <- ForeignCodec.decodeForeignElement(child)
        yield elements :+ decoded
      }
      .getOrElse(Vector.empty)

  private def unexpectedChild(context: String, standardChildren: Vector[Xml.Element]): Either[XmlError, Unit] =
    val expectedNames = Vector.newBuilder[String]
    var hasWildcard = false
    var index = 0
    while index < arity do
      val label = labelAt(index)
      val codec = codecAt(index)
      if label != "extensions" && label != "foreignElements" && codec.isElement then
        if codec.elementName.isEmpty then hasWildcard = true
        else expectedNames += codec.elementName
      index += 1
    if hasWildcard then Right(())
    else
      val expected = expectedNames.result().toSet
      standardChildren.find(child => !expected.contains(child.name.localName)) match
        case Some(child) => Left(XmlError.UnexpectedElement(context, child.name.localName))
        case None        => Right(())
  end unexpectedChild
end DerivedCodec
