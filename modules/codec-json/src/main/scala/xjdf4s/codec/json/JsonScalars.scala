package xjdf4s.codec.json

import scala.reflect.ClassTag

import io.circe.{Decoder, Encoder, Json}

import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * JSON codecs for the scalar layer. Opaque types reuse the domain smart constructors (validation identical to the
 * XML lexical layer); list-typed attribute values become JSON arrays (`XYPair`, `IntegerRange`, colors, matrixes).
 * Plain enums serialize as their case-name string; enums with custom lexical forms get explicit instances.
 */
object JsonScalars:

  // -- opaque domain scalars ----------------------------------------------------

  private def stringCodec[A](parse: String => Either[ValidationError, A], render: A => String)
      : (Encoder[A], Decoder[A]) =
    (Encoder.encodeString.contramap(render), Decoder.decodeString.emap(value => parse(value).left.map(_.toString)))

  given Encoder[Nmtoken] = stringCodec[Nmtoken](Nmtoken.from, _.value)._1
  given Decoder[Nmtoken] = stringCodec[Nmtoken](Nmtoken.from, _.value)._2
  given Encoder[XsdId] = stringCodec[XsdId](XsdId.from, _.value)._1
  given Decoder[XsdId] = stringCodec[XsdId](XsdId.from, _.value)._2
  given Encoder[XsdIdRef] = stringCodec[XsdIdRef](XsdIdRef.from, _.value)._1
  given Decoder[XsdIdRef] = stringCodec[XsdIdRef](XsdIdRef.from, _.value)._2
  given Encoder[XjdfString] = stringCodec[XjdfString](XjdfString.from, _.value)._1
  given Decoder[XjdfString] = stringCodec[XjdfString](XjdfString.from, _.value)._2
  given Encoder[XsdDateTime] = stringCodec[XsdDateTime](XsdDateTime.from, _.value)._1
  given Decoder[XsdDateTime] = stringCodec[XsdDateTime](XsdDateTime.from, _.value)._2
  given Encoder[XsdDuration] = stringCodec[XsdDuration](XsdDuration.from, _.value)._1
  given Decoder[XsdDuration] = stringCodec[XsdDuration](XsdDuration.from, _.value)._2
  given Encoder[LanguageTag] = stringCodec[LanguageTag](LanguageTag.from, _.value)._1
  given Decoder[LanguageTag] = stringCodec[LanguageTag](LanguageTag.from, _.value)._2
  given Encoder[UriRef] = Encoder.encodeString.contramap[UriRef](_.value.toString)
  given Decoder[UriRef] = Decoder.decodeString.emap(value => UriRef.from(value).left.map(_.toString))
  given Encoder[Priority0To100] = Encoder.encodeInt.contramap(_.value)
  given Decoder[Priority0To100] =
    Decoder.decodeInt.emap(value => Priority0To100.from(value).left.map(_.toString))
  given Encoder[PdfPath] = Encoder.encodeString.contramap(_.value)
  given Decoder[PdfPath] = Decoder.decodeString.emap(value => PdfPath.from(value).left.map(_.toString))

  given Encoder[CmykColor] = Encoder.instance(color =>
    Json.arr(
      Json.fromDouble(color.cyan).get,
      Json.fromDouble(color.magenta).get,
      Json.fromDouble(color.yellow).get,
      Json.fromDouble(color.black).get,
    ),
  )
  given Decoder[CmykColor] = Decoder.instance(cursor =>
    cursor.as[List[Double]].flatMap {
      case List(c, m, y, k) => CmykColor.from(c, m, y, k).left.map(error => io.circe.DecodingFailure(error.toString, cursor.history))
      case other            => JsonCodec.fail(cursor, s"CMYKColor requires exactly four numbers, got ${other.size}")
    },
  )

  given Encoder[SrgbColor] = Encoder.instance(color =>
    Json.arr(
      Json.fromDouble(color.red).get,
      Json.fromDouble(color.green).get,
      Json.fromDouble(color.blue).get,
    ),
  )
  given Decoder[SrgbColor] = Decoder.instance(cursor =>
    cursor.as[List[Double]].flatMap {
      case List(r, g, b) => SrgbColor.from(r, g, b).left.map(error => io.circe.DecodingFailure(error.toString, cursor.history))
      case other         => JsonCodec.fail(cursor, s"sRGBColor requires exactly three numbers, got ${other.size}")
    },
  )

  given Encoder[NeutralDensity] = Encoder.encodeFloat.contramap(_.value)
  given Decoder[NeutralDensity] =
    Decoder.decodeFloat.emap(value => NeutralDensity.from(value).left.map(_.toString))

  given Encoder[Vector[Float]] =
    Encoder.instance(values => Json.arr(values.map(value => Json.fromFloat(value).getOrElse(Json.Null))*))
  given Decoder[Vector[Float]] = Decoder.instance(cursor => cursor.as[List[Float]].map(_.toVector))

  given Encoder[Rectangle] = Encoder.instance(rectangle =>
    Json.arr(
      Json.fromDouble(rectangle.lowerLeft.x).get,
      Json.fromDouble(rectangle.lowerLeft.y).get,
      Json.fromDouble(rectangle.upperRight.x).get,
      Json.fromDouble(rectangle.upperRight.y).get,
    ),
  )
  given Decoder[Rectangle] = Decoder.instance(cursor =>
    cursor.as[List[Double]].flatMap {
      case List(x1, y1, x2, y2) => Right(Rectangle(XYPair(x1, y1), XYPair(x2, y2)))
      case other => JsonCodec.fail(cursor, s"rectangle requires exactly four numbers, got ${other.size}")
    },
  )

  // -- list-typed attribute values -> arrays ------------------------------------

  given Encoder[XYPair] = Encoder.instance(pair => Json.arr(Json.fromDouble(pair.x).get, Json.fromDouble(pair.y).get))
  given Decoder[XYPair] = Decoder.instance(cursor =>
    for
      values <- cursor.as[List[Double]]
      pair <- values match
        case List(x, y) => Right(XYPair(x, y))
        case other      => JsonCodec.fail(cursor, s"XYPair requires exactly two numbers, got ${other.size}")
    yield pair,
  )

  given Encoder[IntegerRange] =
    Encoder.instance(range => Json.arr(Json.fromInt(range.first), Json.fromInt(range.last)))
  given Decoder[IntegerRange] = Decoder.instance(cursor =>
    for
      values <- cursor.as[List[Int]]
      range <- values match
        case List(first, last) =>
          IntegerRange.from(first, last).left.map(error => io.circe.DecodingFailure(error.toString, cursor.history))
        case other => JsonCodec.fail(cursor, s"IntegerRange requires exactly two integers, got ${other.size}")
    yield range,
  )

  given Encoder[LabColor] = Encoder.instance(color =>
    Json.arr(
      Json.fromDouble(color.lightness).get,
      Json.fromDouble(color.a).get,
      Json.fromDouble(color.b).get,
    ),
  )
  given Decoder[LabColor] = Decoder.instance(cursor =>
    cursor.as[List[Double]].flatMap {
      case List(l, a, b) => LabColor.from(l, a, b).left.map(error => io.circe.DecodingFailure(error.toString, cursor.history))
      case other         => JsonCodec.fail(cursor, s"LabColor requires exactly three numbers, got ${other.size}")
    },
  )

  given Encoder[Matrix] = Encoder.instance(matrix =>
    Json.arr(
      Json.fromDouble(matrix.a).get,
      Json.fromDouble(matrix.b).get,
      Json.fromDouble(matrix.c).get,
      Json.fromDouble(matrix.d).get,
      Json.fromDouble(matrix.e).get,
      Json.fromDouble(matrix.f).get,
    ),
  )
  given Decoder[Matrix] = Decoder.instance(cursor =>
    cursor.as[List[Double]].flatMap {
      case List(a, b, c, d, e, f) => Right(Matrix(a, b, c, d, e, f))
      case other                  => JsonCodec.fail(cursor, s"matrix requires exactly six numbers, got ${other.size}")
    },
  )

  given Encoder[Shape3D] = Encoder.instance(shape =>
    Json.arr(
      Json.fromDouble(shape.width).get,
      Json.fromDouble(shape.height).get,
      Json.fromDouble(shape.depth).get,
    ),
  )
  given Decoder[Shape3D] = Decoder.instance(cursor =>
    cursor.as[List[Double]].flatMap {
      case List(width, height, depth) => Right(Shape3D(width, height, depth))
      case other => JsonCodec.fail(cursor, s"shape requires exactly three numbers, got ${other.size}")
    },
  )

  // -- enums --------------------------------------------------------------------

  private val enumCache = new java.util.concurrent.ConcurrentHashMap[String, Vector[Any]]

  def enumValues[A](using ct: ClassTag[A]): Vector[A] =
    val name = ct.runtimeClass.getName
    enumCache
      .computeIfAbsent(
        name,
        _ =>
          val companionClass = Class.forName(name + "$")
          val module = companionClass.getField("MODULE$").get(null)
          companionClass.getMethod("values").invoke(module).asInstanceOf[Array[?]].toVector,
      )
      .asInstanceOf[Vector[A]]

  /**
   * Generic fallback for plain (parameter-less) enums: the JSON value is the case-name string. Enums with custom
   * lexical forms (Version, JdfVersion, MessageUrlScheme, NamedColor) have specific instances below and win by
   * specificity.
   */
  given [A <: scala.reflect.Enum]: Encoder[A] =
    Encoder.encodeString.contramap(_.toString)

  given [A <: scala.reflect.Enum](using ct: ClassTag[A]): Decoder[A] =
    Decoder.decodeString.emap(value =>
      enumValues[A].find(_.toString.equalsIgnoreCase(value)).toRight(s"'$value' is not a valid enum value"),
    )

  /** Enum with a custom lexical form (`Version`, `JdfVersion`, `ScreeningType`, `HolePatternCatalog`, ...). */
  def lexicalEnumCodec[A](values: Vector[A], lexical: A => String): (Encoder[A], Decoder[A]) =
    (
      Encoder.encodeString.contramap(lexical),
      Decoder.decodeString.emap(value =>
        values.find(candidate => lexical(candidate).equalsIgnoreCase(value)).toRight(s"'$value' is not a valid enum value"),
      ),
    )

  given Encoder[Version] = lexicalEnumCodec(Version.values.toVector, _.lexical)._1
  given Decoder[Version] = lexicalEnumCodec(Version.values.toVector, _.lexical)._2
  given Encoder[JdfVersion] = lexicalEnumCodec(JdfVersion.values.toVector, _.lexical)._1
  given Decoder[JdfVersion] = lexicalEnumCodec(JdfVersion.values.toVector, _.lexical)._2
  given Encoder[MessageUrlScheme] = lexicalEnumCodec(MessageUrlScheme.values.toVector, _.lexical)._1
  given Decoder[MessageUrlScheme] = lexicalEnumCodec(MessageUrlScheme.values.toVector, _.lexical)._2
  given Encoder[NamedColor] = lexicalEnumCodec(NamedColor.values.toVector, _.lexical)._1
  given Decoder[NamedColor] = lexicalEnumCodec(NamedColor.values.toVector, _.lexical)._2
end JsonScalars
