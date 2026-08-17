package xjdf4s.codec.xml.derivation

import scala.reflect.ClassTag

import xjdf4s.codec.xml.*
import xjdf4s.codec.xml.domain.CodecHelpers
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * Per-field serialization contract used by the derived node codec. A field is either an attribute (scalars, opaque
 * types, enums, value products such as `XYPair`) or an element (nodes, containers of nodes). Containers of
 * attribute types stay attributes (`Vector[Nmtoken]` is `NMTOKENS`), containers of element types become repeated
 * children.
 */
trait FieldCodec[A]:
  def isElement: Boolean

  /** Element name used to select child elements; empty means the codec selects from all children itself. */
  def elementName: String

  /** Attribute path: `None` means the attribute is absent. */
  def decodeAttribute(raw: Option[String]): Either[String, A]

  /** Attribute path: `None` means the attribute must be omitted. */
  def renderAttribute(value: A): Option[String]

  /** Element path: receives the children of the owning element selected by `elementName`. */
  def decodeElements(children: Vector[Xml.Element]): Either[XmlError, A]

  /** Element path: the child elements this value contributes. */
  def encodeElements(value: A): Vector[Xml.Element]
end FieldCodec

trait LowPriorityFieldCodecs:

  given enumCodec[A <: scala.reflect.Enum](using ClassTag[A]): FieldCodec[A] =
    val values = EnumValues.of[A]
    FieldCodec.attribute(
      raw => values.find(_.toString.equalsIgnoreCase(raw.trim)).toRight(s"'$raw' is not a valid enum value"),
      value => Some(value.toString),
    )
end LowPriorityFieldCodecs

object EnumValues:
  /** Reflectively reads the `values` array of an enum companion; cached per class. */
  private val cache = new java.util.concurrent.ConcurrentHashMap[String, Vector[Any]]

  def of[A](using ClassTag[A]): Vector[A] =
    val name = summon[ClassTag[A]].runtimeClass.getName
    cache
      .computeIfAbsent(
        name,
        _ =>
          val companionClass = Class.forName(name + "$")
          val module = companionClass.getField("MODULE$").get(null)
          companionClass
            .getMethod("values")
            .invoke(module)
            .asInstanceOf[Array[?]]
            .toVector,
      )
      .asInstanceOf[Vector[A]]
end EnumValues

object FieldCodec extends LowPriorityFieldCodecs:

  def attribute[A](
      parse: String => Either[String, A],
      render: A => Option[String],
  ): FieldCodec[A] =
    new FieldCodec[A]:
      def isElement: Boolean = false
      def elementName: String = ""
      def decodeAttribute(raw: Option[String]): Either[String, A] =
        raw match
          case Some(value) => parse(value)
          case None        => Left("missing required attribute")
      def renderAttribute(value: A): Option[String] = render(value)
      def decodeElements(children: Vector[Xml.Element]): Either[XmlError, A] =
        Left(XmlError.UnexpectedElement("attribute", elementName))
      def encodeElements(value: A): Vector[Xml.Element] = Vector.empty
  end attribute

  def element[A](codec: XmlElementCodec[A]): FieldCodec[A] =
    new FieldCodec[A]:
      def isElement: Boolean = true
      def elementName: String = codec.elementName
      def decodeAttribute(raw: Option[String]): Either[String, A] =
        Left(s"$elementName is an element, not an attribute")
      def renderAttribute(value: A): Option[String] = None
      def decodeElements(children: Vector[Xml.Element]): Either[XmlError, A] =
        children match
          case Vector(single) => codec.decode(single)
          case _ =>
            Left(XmlError.MissingElement("", elementName))
      def encodeElements(value: A): Vector[Xml.Element] = Vector(codec.encode(value))
  end element

  // -- XSD built-ins -------------------------------------------------------------

  given intCodec: FieldCodec[Int] = attribute(Lexical.int, value => Some(value.toString))
  given longCodec: FieldCodec[Long] = attribute(Lexical.long, value => Some(value.toString))
  given floatCodec: FieldCodec[Float] = attribute(Lexical.float, value => Some(value.toString))
  given doubleCodec: FieldCodec[Double] = attribute(Lexical.double, value => Some(value.toString))
  given booleanCodec: FieldCodec[Boolean] = attribute(Lexical.bool, value => Some(value.toString))
  given stringCodec: FieldCodec[String] = attribute(Right(_), value => Some(value))
  given byteVectorCodec: FieldCodec[Vector[Byte]] =
    attribute(Lexical.hexBinary, value => Some(Lexical.renderHexBinary(value)))

  // -- core opaque types ---------------------------------------------------------

  given nmtokenCodec: FieldCodec[Nmtoken] = attribute(Lexical.nmtoken, value => Some(value.value))
  given xsdIdCodec: FieldCodec[XsdId] = attribute(Lexical.xsdId, value => Some(value.value))
  given xsdIdRefCodec: FieldCodec[XsdIdRef] = attribute(Lexical.xsdIdRef, value => Some(value.value))
  given xjdfStringCodec: FieldCodec[XjdfString] = attribute(Lexical.xjdfString, value => Some(value.value))
  given dateTimeCodec: FieldCodec[XsdDateTime] = attribute(Lexical.dateTime, value => Some(value.value))
  given durationCodec: FieldCodec[XsdDuration] = attribute(Lexical.duration, value => Some(value.value))
  given languageTagCodec: FieldCodec[LanguageTag] = attribute(Lexical.languageTag, value => Some(value.value))
  given uriCodec: FieldCodec[UriRef] = attribute(Lexical.uri, value => Some(value.value.toString))
  given priorityCodec: FieldCodec[Priority0To100] = attribute(Lexical.priority, value => Some(value.value.toString))

  // -- model opaque/value types --------------------------------------------------

  given countryCodeCodec: FieldCodec[CountryCode] =
    attribute(value => CountryCode.from(value).left.map(_.toString), v => Some(v.value))
  given xPathCodec: FieldCodec[XPath] =
    attribute(value => XPath.from(value).left.map(_.toString), v => Some(v.value))
  given pdfPathCodec: FieldCodec[PdfPath] =
    attribute(value => PdfPath.from(value).left.map(_.toString), v => Some(v.value))
  given evenPageCountCodec: FieldCodec[EvenPageCount] =
    attribute(
      raw => Lexical.int(raw).flatMap(v => EvenPageCount.from(v).left.map(_.toString)),
      v => Some(v.value.toString),
    )
  given commonFoldsCodec: FieldCodec[CommonFolds] =
    attribute(
      raw => Lexical.int(raw).flatMap(v => CommonFolds.from(v).left.map(_.toString)),
      v => Some(v.value.toString),
    )
  given qualityScoreCodec: FieldCodec[QualityScore] =
    attribute(
      raw => Lexical.int(raw).flatMap(v => QualityScore.from(v).left.map(_.toString)),
      v => Some(v.value.toString),
    )
  given namedColorCodec: FieldCodec[NamedColor] = attribute(Lexical.namedColor, v => Some(v.lexical))
  given foldCatalogCodec: FieldCodec[FoldCatalog] = attribute(Lexical.foldCatalog, v => Some(v.value))
  given neutralDensityCodec: FieldCodec[NeutralDensity] =
    attribute(Lexical.neutralDensity, v => Some(v.value.toString))
  given transferFunctionCodec: FieldCodec[TransferFunction] =
    attribute(Lexical.transferFunction, v => Some(CodecHelpers.renderFloats(v.toVector)))
  given gluingPatternCodec: FieldCodec[GluingPattern] =
    attribute(Lexical.gluingPattern, v => Some(CodecHelpers.renderFloats(v.toVector)))
  given labColorCodec: FieldCodec[LabColor] = attribute(Lexical.labColor, v => Some(CodecHelpers.renderLabColor(v)))
  given cmykColorCodec: FieldCodec[CmykColor] =
    attribute(Lexical.cmykColor, v => Some(CodecHelpers.renderCmykColor(v)))
  given srgbColorCodec: FieldCodec[SrgbColor] =
    attribute(Lexical.srgbColor, v => Some(CodecHelpers.renderSrgbColor(v)))
  given integerRangeCodec: FieldCodec[IntegerRange] =
    attribute(Lexical.integerRange, v => Some(CodecHelpers.renderRange(v)))
  given xypairCodec: FieldCodec[XYPair] = attribute(Lexical.xypair, v => Some(CodecHelpers.renderXypair(v)))
  given tileCoordinateCodec: FieldCodec[TileCoordinate] =
    attribute(Lexical.tileCoordinate, v => Some(CodecHelpers.renderTile(v)))
  given shape3dCodec: FieldCodec[Shape3D] = attribute(Lexical.shape3d, v => Some(CodecHelpers.renderShape3d(v)))
  given gridSizeCodec: FieldCodec[GridSize] = attribute(Lexical.gridSize, v => Some(CodecHelpers.renderGridSize(v)))
  given matrixCodec: FieldCodec[Matrix] = attribute(Lexical.matrix, v => Some(CodecHelpers.renderMatrix(v)))
  given rectangleCodec: FieldCodec[Rectangle] = attribute(Lexical.rectangle, v => Some(CodecHelpers.renderRectangle(v)))

  // -- enums with custom lexical forms ------------------------------------------

  given versionCodec: FieldCodec[Version] = attribute(Lexical.version, v => Some(v.lexical))
  given jdfVersionCodec: FieldCodec[JdfVersion] = attribute(Lexical.jdfVersion, v => Some(v.lexical))
  given holePatternCatalogCodec: FieldCodec[HolePatternCatalog] =
    attribute(Lexical.holePatternCatalog, v => Some(v.lexical))
  given screeningTypeCodec: FieldCodec[ScreeningType] = attribute(Lexical.screeningType, v => Some(v.lexical))
  given messageUrlSchemeCodec: FieldCodec[MessageUrlScheme] =
    attribute(Lexical.messageUrlScheme, v => Some(v.lexical))

  // -- special markers handled by the DerivedCodec runtime by field name -----------------

  /**
   * The `extensions` field of every node is handled specially by the derived codec (foreign attributes and
   * children), so this instance exists only to satisfy the per-field materialization of the inline instance
   * walk; the runtime never calls it.
   */
  given extensionsCodec: FieldCodec[Extensions] =
    new FieldCodec[Extensions]:
      def isElement: Boolean = false
      def elementName: String = ""
      def decodeAttribute(raw: Option[String]): Either[String, Extensions] = Right(Extensions.empty)
      def renderAttribute(value: Extensions): Option[String] = None
      def decodeElements(children: Vector[Xml.Element]): Either[XmlError, Extensions] = Right(Extensions.empty)
      def encodeElements(value: Extensions): Vector[Xml.Element] = Vector.empty
  end extensionsCodec

  // -- containers ----------------------------------------------------------------

  given optionCodec[A](using inner: FieldCodec[A]): FieldCodec[Option[A]] =
    new FieldCodec[Option[A]]:
      def isElement: Boolean = inner.isElement
      def elementName: String = inner.elementName
      def decodeAttribute(raw: Option[String]): Either[String, Option[A]] =
        raw match
          case None       => Right(None)
          case Some(value) => inner.decodeAttribute(Some(value)).map(Some(_))
      def renderAttribute(value: Option[A]): Option[String] = value.flatMap(inner.renderAttribute)
      def decodeElements(children: Vector[Xml.Element]): Either[XmlError, Option[A]] =
        children match
          case Vector()      => Right(None)
          case Vector(single) => inner.decodeElements(Vector(single)).map(Some(_))
          case _             => Left(XmlError.UnexpectedElement("Option", elementName))
      def encodeElements(value: Option[A]): Vector[Xml.Element] =
        value.toVector.flatMap(inner.encodeElements)
  end optionCodec

  given vectorCodec[A](using inner: FieldCodec[A]): FieldCodec[Vector[A]] =
    new FieldCodec[Vector[A]]:
      def isElement: Boolean = inner.isElement
      def elementName: String = inner.elementName
      def decodeAttribute(raw: Option[String]): Either[String, Vector[A]] =
        raw match
          case None => Right(Vector.empty)
          case Some(value) =>
            value.trim.split("\\s+").toVector.filter(_.nonEmpty).foldLeft[Either[String, Vector[A]]](Right(Vector.empty)) {
              (acc, token) =>
                for
                  values <- acc
                  parsed <- inner.decodeAttribute(Some(token))
                yield values :+ parsed
            }
      def renderAttribute(value: Vector[A]): Option[String] =
        Option.when(value.nonEmpty)(value.flatMap(inner.renderAttribute).mkString(" "))
      def decodeElements(children: Vector[Xml.Element]): Either[XmlError, Vector[A]] =
        children.foldLeft[Either[XmlError, Vector[A]]](Right(Vector.empty)) { (acc, child) =>
          for
            values <- acc
            parsed <- inner.decodeElements(Vector(child))
          yield values :+ parsed
        }
      def encodeElements(value: Vector[A]): Vector[Xml.Element] = value.flatMap(inner.encodeElements)
  end vectorCodec

  given nonEmptyVectorCodec[A](using inner: FieldCodec[A]): FieldCodec[NonEmptyVector[A]] =
    new FieldCodec[NonEmptyVector[A]]:
      private val vectorCodec = summon[FieldCodec[Vector[A]]]
      def isElement: Boolean = inner.isElement
      def elementName: String = inner.elementName
      def decodeAttribute(raw: Option[String]): Either[String, NonEmptyVector[A]] =
        vectorCodec.decodeAttribute(raw).flatMap: values =>
          NonEmptyVector.from(values).left.map(_ => "at least one value is required")
      def renderAttribute(value: NonEmptyVector[A]): Option[String] = vectorCodec.renderAttribute(value.toVector)
      def decodeElements(children: Vector[Xml.Element]): Either[XmlError, NonEmptyVector[A]] =
        vectorCodec.decodeElements(children).flatMap: values =>
          NonEmptyVector
            .from(values)
            .left
            .map(_ => XmlError.MissingElement("", elementName))
      def encodeElements(value: NonEmptyVector[A]): Vector[Xml.Element] = vectorCodec.encodeElements(value.toVector)
  end nonEmptyVectorCodec

  given twoOrMoreCodec[A](using inner: FieldCodec[A]): FieldCodec[TwoOrMore[A]] =
    new FieldCodec[TwoOrMore[A]]:
      private val vectorCodec = summon[FieldCodec[Vector[A]]]
      def isElement: Boolean = inner.isElement
      def elementName: String = inner.elementName
      def decodeAttribute(raw: Option[String]): Either[String, TwoOrMore[A]] =
        vectorCodec.decodeAttribute(raw).flatMap: values =>
          TwoOrMore.from(values).left.map(_ => "at least two values are required")
      def renderAttribute(value: TwoOrMore[A]): Option[String] = vectorCodec.renderAttribute(value.toVector)
      def decodeElements(children: Vector[Xml.Element]): Either[XmlError, TwoOrMore[A]] =
        vectorCodec.decodeElements(children).flatMap: values =>
          TwoOrMore.from(values).left.map(_ => XmlError.MissingElement("", elementName))
      def encodeElements(value: TwoOrMore[A]): Vector[Xml.Element] = vectorCodec.encodeElements(value.toVector)
  end twoOrMoreCodec

  given atMostTwoCodec[A](using inner: FieldCodec[A]): FieldCodec[AtMostTwo[A]] =
    new FieldCodec[AtMostTwo[A]]:
      private val vectorCodec = summon[FieldCodec[Vector[A]]]
      def isElement: Boolean = inner.isElement
      def elementName: String = inner.elementName
      def decodeAttribute(raw: Option[String]): Either[String, AtMostTwo[A]] =
        vectorCodec.decodeAttribute(raw).flatMap: values =>
          AtMostTwo.from(values).left.map(_ => "at most two values are required")
      def renderAttribute(value: AtMostTwo[A]): Option[String] = vectorCodec.renderAttribute(value.toVector)
      def decodeElements(children: Vector[Xml.Element]): Either[XmlError, AtMostTwo[A]] =
        vectorCodec.decodeElements(children).flatMap: values =>
          AtMostTwo.from(values).left.map(_ => XmlError.MissingElement("", elementName))
      def encodeElements(value: AtMostTwo[A]): Vector[Xml.Element] = vectorCodec.encodeElements(value.toVector)
  end atMostTwoCodec

  // -- nodes: any Product with an XmlElementCodec becomes an element field ---------

  /**
   * Fallback for case classes without a hand-written codec. Deliberately non-inline: `inline given` definitions
   * are invisible to ordinary implicit search, which would break every nested product field.
   * The [[XmlElementCodec]] instances themselves come from the generated non-inline givens in
   * `DerivedInstances` (or from hand codecs), so the whole chain stays in ordinary implicit search.
   */
  given productCodec[A <: Product](using codec: XmlElementCodec[A]): FieldCodec[A] = element(codec)
end FieldCodec
