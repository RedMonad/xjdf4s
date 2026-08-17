package xjdf4s.codec.xml

import cats.Show

import xjdf4s.core.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * Attribute-value parsers for every scalar type used by the codec. Each parser maps a raw attribute string to a
 * domain value or a human-readable failure; the [[XmlDecoders]] combinators attach the element/attribute context.
 * Domain smart constructors are reused, so the lexical space stays in sync with the model invariants.
 */
object Lexical:
  type Lex[A] = String => Either[String, A]

  private def via[A](parse: String => Either[ValidationError, A]): Lex[A] =
    value => parse(value).left.map(error => Show[ValidationError].show(error))

  def enumOf[A](values: Vector[A], nameOf: A => String): Lex[A] =
    value =>
      values
        .find(candidate => nameOf(candidate).equalsIgnoreCase(value.trim))
        .toRight(s"'$value' is not one of: ${values.map(nameOf).mkString(", ")}")

  private def tokens(value: String): Vector[String] =
    value.trim.split("\\s+").toVector.filter(_.nonEmpty)

  // -- opaque domain scalars (reuse smart constructors) --------------------------------

  val nmtoken: Lex[Nmtoken] = via(Nmtoken.from)
  val xsdId: Lex[XsdId] = via(XsdId.from)
  val xsdIdRef: Lex[XsdIdRef] = via(XsdIdRef.from)
  val xjdfString: Lex[XjdfString] = via(XjdfString.from)
  val dateTime: Lex[XsdDateTime] = via(XsdDateTime.from)
  val duration: Lex[XsdDuration] = via(XsdDuration.from)
  val languageTag: Lex[LanguageTag] = via(LanguageTag.from)
  val uri: Lex[UriRef] = via(UriRef.from)
  val namedColor: Lex[NamedColor] = via(NamedColor.from)
  private def renderValidation(error: ValidationError): String = Show[ValidationError].show(error)

  val priority: Lex[Priority0To100] =
    value => int(value).flatMap(parsed => Priority0To100.from(parsed).left.map(renderValidation))

  val transferFunction: Lex[TransferFunction] =
    value => floatList(value).flatMap(parsed => TransferFunction.from(parsed).left.map(renderValidation))

  val gluingPattern: Lex[GluingPattern] =
    value => floatList(value).flatMap(parsed => GluingPattern.from(parsed).left.map(renderValidation))

  val neutralDensity: Lex[NeutralDensity] =
    value => float(value).flatMap(parsed => NeutralDensity.from(parsed).left.map(renderValidation))

  // -- XSD built-ins ----------------------------------------------------------------

  val int: Lex[Int] =
    value => value.trim.toIntOption.toRight(s"'$value' is not an integer")

  val long: Lex[Long] =
    value => value.trim.toLongOption.toRight(s"'$value' is not a long integer")

  val float: Lex[Float] =
    value =>
      value.trim.toUpperCase match
        case "INF"  => Right(Float.PositiveInfinity)
        case "-INF" => Right(Float.NegativeInfinity)
        case "NAN"  => Right(Float.NaN)
        case other  => other.toFloatOption.toRight(s"'$value' is not a float")

  val double: Lex[Double] =
    value =>
      value.trim.toUpperCase match
        case "INF"  => Right(Double.PositiveInfinity)
        case "-INF" => Right(Double.NegativeInfinity)
        case "NAN"  => Right(Double.NaN)
        case other  => other.toDoubleOption.toRight(s"'$value' is not a double")

  val bool: Lex[Boolean] =
    value =>
      value.trim.toLowerCase match
        case "true" | "1"  => Right(true)
        case "false" | "0" => Right(false)
        case _             => Left(s"'$value' is not a boolean")

  val hexBinary: Lex[Vector[Byte]] =
    value =>
      val hex = value.trim
      if hex.length % 2 != 0 then Left(s"'$value' is not hexBinary: odd length")
      else
        try Right(hex.grouped(2).map(Integer.parseInt(_, 16).toByte).toVector)
        catch case _: NumberFormatException => Left(s"'$value' is not hexBinary")

  def renderHexBinary(bytes: Vector[Byte]): String =
    bytes.map(byte => f"${byte & 0xff}%02x").mkString

  // -- lists -----------------------------------------------------------------------

  def list[A](element: Lex[A]): Lex[Vector[A]] =
    value =>
      tokens(value).foldLeft[Either[String, Vector[A]]](Right(Vector.empty)) { (acc, token) =>
        for
          values <- acc
          parsed <- element(token)
        yield values :+ parsed
      }

  val nmtokens: Lex[Vector[Nmtoken]] = list(nmtoken)
  val languages: Lex[Vector[LanguageTag]] = list(languageTag)
  val intList: Lex[Vector[Int]] = list(int)
  val floatList: Lex[Vector[Float]] = list(float)
  val xsdIdRefs: Lex[Vector[XsdIdRef]] = list(xsdIdRef)

  // -- fixed-length products -------------------------------------------------------

  val integerRange: Lex[IntegerRange] =
    value =>
      tokens(value) match
        case Vector(first, last) =>
          for
            f <- int(first)
            l <- int(last)
            range <- IntegerRange.from(f, l).left.map(error => Show[ValidationError].show(error))
          yield range
        case other => Left(s"'$value' must contain exactly two integers, got ${other.size}")

  val xypair: Lex[XYPair] =
    value =>
      tokens(value) match
        case Vector(x, y) =>
          for
            xx <- double(x)
            yy <- double(y)
          yield XYPair(xx, yy)
        case other => Left(s"'$value' must contain exactly two numbers, got ${other.size}")

  val tileCoordinate: Lex[TileCoordinate] =
    value =>
      tokens(value) match
        case Vector(x, y) =>
          for
            xx <- int(x)
            yy <- int(y)
          yield TileCoordinate(xx, yy)
        case other => Left(s"'$value' must contain exactly two integers, got ${other.size}")

  val shape3d: Lex[Shape3D] =
    value =>
      tokens(value) match
        case Vector(w, h, d) =>
          for
            ww <- double(w)
            hh <- double(h)
            dd <- double(d)
          yield Shape3D(ww, hh, dd)
        case other => Left(s"'$value' must contain exactly three numbers, got ${other.size}")

  val matrix: Lex[Matrix] =
    value =>
      tokens(value) match
        case Vector(a, b, c, d, e, f) =>
          for
            aa <- double(a)
            bb <- double(b)
            cc <- double(c)
            dd <- double(d)
            ee <- double(e)
            ff <- double(f)
          yield Matrix(aa, bb, cc, dd, ee, ff)
        case other => Left(s"'$value' must contain exactly six numbers, got ${other.size}")

  val labColor: Lex[LabColor] =
    value =>
      tokens(value) match
        case Vector(l, a, b) =>
          for
            ll <- double(l)
            aa <- double(a)
            bb <- double(b)
            color <- LabColor.from(ll, aa, bb).left.map(error => Show[ValidationError].show(error))
          yield color
        case other => Left(s"'$value' must contain exactly three numbers, got ${other.size}")

  val cmykColor: Lex[CmykColor] =
    value =>
      tokens(value) match
        case Vector(c, m, y, k) =>
          for
            cc <- double(c)
            mm <- double(m)
            yy <- double(y)
            kk <- double(k)
            color <- CmykColor.from(cc, mm, yy, kk).left.map(error => Show[ValidationError].show(error))
          yield color
        case other => Left(s"'$value' must contain exactly four numbers, got ${other.size}")

  val srgbColor: Lex[SrgbColor] =
    value =>
      tokens(value) match
        case Vector(r, g, b) =>
          for
            rr <- double(r)
            gg <- double(g)
            bb <- double(b)
            color <- SrgbColor.from(rr, gg, bb).left.map(error => Show[ValidationError].show(error))
          yield color
        case other => Left(s"'$value' must contain exactly three numbers, got ${other.size}")

  // -- enums ----------------------------------------------------------------------

  val version: Lex[Version] =
    value => Version.values.find(_.lexical == value.trim).toRight(s"'$value' is not an XJDF version")

  /** Device/@JDFVersions: the union of JDF versions (1.0-1.8) and XJDF versions (2.x). */
  val jdfVersion: Lex[JdfVersion] = enumOf(JdfVersion.values.toVector, _.lexical)

  val orientation: Lex[Orientation] = enumOf(Orientation.values.toVector, _.toString)
  val resourceAvailability: Lex[ResourceAvailability] = enumOf(ResourceAvailability.values.toVector, _.toString)
  val resourceUsage: Lex[ResourceUsage] = enumOf(ResourceUsage.values.toVector, _.toString)
  val side: Lex[Side] = enumOf(Side.values.toVector, _.toString)
  val face: Lex[Face] = enumOf(Face.values.toVector, _.toString)
  val scope: Lex[Scope] = enumOf(Scope.values.toVector, _.toString)
  val deviceStatus: Lex[DeviceStatus] = enumOf(DeviceStatus.values.toVector, _.toString)
  val previewType: Lex[PreviewType] = enumOf(PreviewType.values.toVector, _.toString)
  val transferCurveName: Lex[TransferCurveName] = enumOf(TransferCurveName.values.toVector, _.toString)
  val coating: Lex[Coating] = enumOf(Coating.values.toVector, _.toString)
  val opacity: Lex[Opacity] = enumOf(Opacity.values.toVector, _.toString)
  val isoPaperSubstrate: Lex[IsoPaperSubstrate] = enumOf(IsoPaperSubstrate.values.toVector, _.toString)
  val mediaDirection: Lex[MediaDirection] = enumOf(MediaDirection.values.toVector, _.toString)
  val mediaType: Lex[MediaType] = enumOf(MediaType.values.toVector, _.toString)
  val mediaUnit: Lex[MediaUnit] = enumOf(MediaUnit.values.toVector, _.toString)
  val glueType: Lex[GlueType] = enumOf(GlueType.values.toVector, _.toString)
  val gluingTechnique: Lex[GluingTechnique] = enumOf(GluingTechnique.values.toVector, _.toString)
  val automation: Lex[Automation] = enumOf(Automation.values.toVector, _.toString)
  val polarity: Lex[Polarity] = enumOf(Polarity.values.toVector, _.toString)
  val plateTechnology: Lex[PlateTechnology] = enumOf(PlateTechnology.values.toVector, _.toString)
  val imagableSide: Lex[ImagableSide] = enumOf(ImagableSide.values.toVector, _.toString)
  val devicePackaging: Lex[DevicePackaging] = enumOf(DevicePackaging.values.toVector, _.toString)
  val colorType: Lex[ColorType] = enumOf(ColorType.values.toVector, _.toString)
  val inkState: Lex[InkState] = enumOf(InkState.values.toVector, _.toString)
  val measurementFilter: Lex[MeasurementFilter] = enumOf(MeasurementFilter.values.toVector, _.toString)
  val sampleBacking: Lex[SampleBacking] = enumOf(SampleBacking.values.toVector, _.toString)
  val whiteBase: Lex[WhiteBase] = enumOf(WhiteBase.values.toVector, _.toString)
end Lexical
