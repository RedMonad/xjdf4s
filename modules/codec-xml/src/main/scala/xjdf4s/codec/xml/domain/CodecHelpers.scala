package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.core.*
import xjdf4s.model.*

private[domain] object CodecHelpers:
  def qname(localName: String): QualifiedName = QualifiedName(XjdfNamespace.uri, localName)

  def attribute(name: String, value: Option[String]): Vector[(QualifiedName, String)] =
    value.toVector.map(nameValue => (qname(name), nameValue))

  def attributeOf[A](name: String, value: Option[A], render: A => String): Vector[(QualifiedName, String)] =
    attribute(name, value.map(render))

  def extensionAttributes(extensions: Extensions): Vector[(QualifiedName, String)] =
    extensions.attributes.toVector
      .sortBy(pair => (pair._1.namespace, pair._1.localName))
      .map { case (name, value) => (name, ForeignCodec.renderExtensionValue(value)) }

  def decodeExtensionAttributes(element: Xml.Element): Map[QualifiedName, ExtensionValue] =
    element.attributes
      .collect { case (name, value) if name.namespace != XjdfNamespace.uri => (name, ExtensionValue.Text(value)) }
      .toMap

  // -- renders ---------------------------------------------------------------

  def renderFloat(value: Float): String = value.toString
  def renderDouble(value: Double): String = value.toString
  def renderInt(value: Int): String = value.toString
  def renderBoolean(value: Boolean): String = value.toString

  def renderXypair(value: XYPair): String = s"${value.x} ${value.y}"
  def renderTile(value: TileCoordinate): String = s"${value.x} ${value.y}"
  def renderShape3d(value: Shape3D): String = s"${value.width} ${value.height} ${value.depth}"
  def renderMatrix(value: Matrix): String =
    s"${value.a} ${value.b} ${value.c} ${value.d} ${value.e} ${value.f}"
  def renderRange(value: IntegerRange): String = s"${value.first} ${value.last}"

  def renderNmtokens(values: Vector[Nmtoken]): String = values.map(_.value).mkString(" ")
  def renderLanguages(values: Vector[LanguageTag]): String = values.map(_.value).mkString(" ")
  def renderInts(values: Vector[Int]): String = values.mkString(" ")
  def renderFloats(values: Vector[Float]): String = values.map(_.toString).mkString(" ")
  def renderIdRefs(values: Vector[XsdIdRef]): String = values.map(_.value).mkString(" ")

  def renderLabColor(color: LabColor): String = s"${color.lightness} ${color.a} ${color.b}"
  def renderCmykColor(color: CmykColor): String = s"${color.cyan} ${color.magenta} ${color.yellow} ${color.black}"
  def renderSrgbColor(color: SrgbColor): String = s"${color.red} ${color.green} ${color.blue}"
end CodecHelpers
