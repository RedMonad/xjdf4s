package xjdf4s.codec.json

import scala.compiletime.{constValueTuple, erasedValue, summonFrom, summonInline}
import scala.deriving.Mirror
import scala.reflect.ClassTag

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}

import xjdf4s.codec.xml.derivation.{Defaults, Names}
import xjdf4s.core.*

/**
 * Per-field serialization contract used by the derived JSON node codec. Unlike the XML side there is no
 * attribute/element split - every field becomes a member of the object - but the naming source differs:
 * scalar fields use the attribute name (`Names.attributeName`), node fields use the element name of the child
 * type. `isElement` carries exactly that distinction.
 */
trait JsonFieldCodec[A]:
  def isElement: Boolean

  /** Element name used as the member name of node fields; empty for scalar fields. */
  def elementName: String

  /** The JSON value of the whole field; `None` means the member must be omitted (absent Option, empty vector). */
  def encodeField(value: Any): Option[Json]

  /** Decodes the member named `memberName` of `cursor`; a missing required member is a failure. */
  def decodeMember(cursor: HCursor, memberName: String): Decoder.Result[A]

  /** Decodes a bare JSON value (array items of container codecs). */
  def decodeValue(json: Json): Decoder.Result[A]
end JsonFieldCodec

/** Lower-priority fallbacks: any type with a plain Encoder/Decoder pair (scalars, opaque wrappers, enums). */
trait LowPriorityJsonFieldCodecs:

  given scalarFieldCodec[A](using encoder: Encoder[A], decoder: Decoder[A]): JsonFieldCodec[A] =
    JsonFieldCodec.from(encoder, decoder)
end LowPriorityJsonFieldCodecs

object JsonFieldCodec extends LowPriorityJsonFieldCodecs:

  /** Scalar field: the member name is the attribute name of the owning field label. */
  def from[A](encoder: Encoder[A], decoder: Decoder[A]): JsonFieldCodec[A] =
    new JsonFieldCodec[A]:
      def isElement: Boolean = false
      def elementName: String = ""
      def encodeField(value: Any): Option[Json] =
        value match
          case None    => None
          case Some(v) => Some(encoder(v.asInstanceOf[A]))
          case other   => Some(encoder(other.asInstanceOf[A]))
      def decodeMember(cursor: HCursor, memberName: String): Decoder.Result[A] =
        cursor.downField(memberName).focus match
          case Some(json) => decoder.decodeJson(json)
          case None       => Left(DecodingFailure(s"missing member '$memberName'", cursor.history))
      def decodeValue(json: Json): Decoder.Result[A] = decoder.decodeJson(json)
  end from

  /** Node field: the member name is the element name derived from the runtime class (with naming overrides). */
  def fromProduct[A](encoder: Encoder[A], decoder: Decoder[A], classTag: ClassTag[?]): JsonFieldCodec[A] =
    new JsonFieldCodec[A]:
      private val name: String = JsonDerived.elementNameOf(classTag)
      def isElement: Boolean = true
      def elementName: String = name
      def encodeField(value: Any): Option[Json] =
        value match
          case None    => None
          case Some(v) => Some(encoder(v.asInstanceOf[A]))
          case other   => Some(encoder(other.asInstanceOf[A]))
      def decodeMember(cursor: HCursor, memberName: String): Decoder.Result[A] =
        cursor.downField(memberName).focus match
          case Some(json) => decoder.decodeJson(json)
          case None       => Left(DecodingFailure(s"missing member '$memberName'", cursor.history))
      def decodeValue(json: Json): Decoder.Result[A] = decoder.decodeJson(json)
  end fromProduct

  // -- containers ----------------------------------------------------------------

  given optionFieldCodec[A](using inner: JsonFieldCodec[A]): JsonFieldCodec[Option[A]] =
    new JsonFieldCodec[Option[A]]:
      def isElement: Boolean = inner.isElement
      def elementName: String = inner.elementName
      def encodeField(value: Any): Option[Json] =
        value match
          case None    => None
          case Some(v) => inner.encodeField(v)
          case other   => inner.encodeField(other)
      def decodeMember(cursor: HCursor, memberName: String): Decoder.Result[Option[A]] =
        cursor.downField(memberName).focus match
          case Some(json) => inner.decodeValue(json).map(Some(_))
          case None       => Right(None)
      def decodeValue(json: Json): Decoder.Result[Option[A]] = inner.decodeValue(json).map(Some(_))
  end optionFieldCodec

  given vectorFieldCodec[A](using inner: JsonFieldCodec[A]): JsonFieldCodec[Vector[A]] =
    new JsonFieldCodec[Vector[A]]:
      def isElement: Boolean = inner.isElement
      def elementName: String = inner.elementName
      def encodeField(value: Any): Option[Json] =
        value match
          case values: Vector[?] if values.isEmpty => None
          case values: Vector[?]                   => Some(Json.arr(values.map(item => inner.encodeField(item).getOrElse(Json.Null))*))
          case None                                => None
          case other                               => Some(Json.arr(inner.encodeField(other).getOrElse(Json.Null)))
      def decodeMember(cursor: HCursor, memberName: String): Decoder.Result[Vector[A]] =
        cursor.downField(memberName).focus match
          case Some(json) => decodeValue(json)
          case None       => Right(Vector.empty)
      def decodeValue(json: Json): Decoder.Result[Vector[A]] =
        json.asArray match
          case Some(items) =>
            items.toVector.foldLeft[Decoder.Result[Vector[A]]](Right(Vector.empty)) { (acc, item) =>
              for
                values <- acc
                decoded <- inner.decodeValue(item)
              yield values :+ decoded
            }
          case None => Left(DecodingFailure("expected an array", Nil))
  end vectorFieldCodec

  given nonEmptyVectorFieldCodec[A](using inner: JsonFieldCodec[A]): JsonFieldCodec[NonEmptyVector[A]] =
    new JsonFieldCodec[NonEmptyVector[A]]:
      def isElement: Boolean = inner.isElement
      def elementName: String = inner.elementName
      def encodeField(value: Any): Option[Json] =
        value match
          case values: Vector[?] => Some(Json.arr(values.map(item => inner.encodeField(item).getOrElse(Json.Null))*))
          case other                     => Some(Json.arr(inner.encodeField(other).getOrElse(Json.Null)))
      def decodeMember(cursor: HCursor, memberName: String): Decoder.Result[NonEmptyVector[A]] =
        cursor.downField(memberName).focus match
          case Some(json) => decodeValue(json)
          case None       => Left(DecodingFailure(s"missing member '$memberName'", cursor.history))
      def decodeValue(json: Json): Decoder.Result[NonEmptyVector[A]] =
        vectorFieldCodec[A].decodeValue(json).flatMap { values =>
          NonEmptyVector.from(values).left.map(_ => DecodingFailure("expected a non-empty array", Nil))
        }
  end nonEmptyVectorFieldCodec

  given twoOrMoreFieldCodec[A](using inner: JsonFieldCodec[A]): JsonFieldCodec[TwoOrMore[A]] =
    new JsonFieldCodec[TwoOrMore[A]]:
      def isElement: Boolean = inner.isElement
      def elementName: String = inner.elementName
      def encodeField(value: Any): Option[Json] =
        value match
          case values: Vector[?] => Some(Json.arr(values.map(item => inner.encodeField(item).getOrElse(Json.Null))*))
          case other                => Some(Json.arr(inner.encodeField(other).getOrElse(Json.Null)))
      def decodeMember(cursor: HCursor, memberName: String): Decoder.Result[TwoOrMore[A]] =
        cursor.downField(memberName).focus match
          case Some(json) => decodeValue(json)
          case None       => Left(DecodingFailure(s"missing member '$memberName'", cursor.history))
      def decodeValue(json: Json): Decoder.Result[TwoOrMore[A]] =
        vectorFieldCodec[A].decodeValue(json).flatMap { values =>
          TwoOrMore.from(values).left.map(_ => DecodingFailure("expected at least two elements", Nil))
        }
  end twoOrMoreFieldCodec

  given atMostTwoFieldCodec[A](using inner: JsonFieldCodec[A]): JsonFieldCodec[AtMostTwo[A]] =
    new JsonFieldCodec[AtMostTwo[A]]:
      def isElement: Boolean = inner.isElement
      def elementName: String = inner.elementName
      def encodeField(value: Any): Option[Json] =
        value match
          case values: Vector[?] => Some(Json.arr(values.map(item => inner.encodeField(item).getOrElse(Json.Null))*))
          case other                => Some(Json.arr(inner.encodeField(other).getOrElse(Json.Null)))
      def decodeMember(cursor: HCursor, memberName: String): Decoder.Result[AtMostTwo[A]] =
        cursor.downField(memberName).focus match
          case Some(json) => decodeValue(json)
          case None       => Left(DecodingFailure(s"missing member '$memberName'", cursor.history))
      def decodeValue(json: Json): Decoder.Result[AtMostTwo[A]] =
        vectorFieldCodec[A].decodeValue(json).flatMap { values =>
          AtMostTwo.from(values).left.map(_ => DecodingFailure("expected at most two elements", Nil))
        }
  end atMostTwoFieldCodec

  // -- special markers handled by the derived runtime by field name --------------

  /**
   * The `extensions` field of every node is handled by the derived runtime (foreign members plus `"@context"`),
   * so this instance exists only to satisfy the per-field materialization of the inline instance walk; the
   * runtime never calls it.
   */
  given extensionsFieldCodec: JsonFieldCodec[Extensions] =
    new JsonFieldCodec[Extensions]:
      def isElement: Boolean = false
      def elementName: String = ""
      def encodeField(value: Any): Option[Json] = None
      def decodeMember(cursor: HCursor, memberName: String): Decoder.Result[Extensions] = Right(Extensions.empty)
      def decodeValue(json: Json): Decoder.Result[Extensions] = Right(Extensions.empty)
  end extensionsFieldCodec

  /** Same marker role for `foreignElements` fields of type `Vector[ExtensionElement]`. */
  given extensionElementFieldCodec: JsonFieldCodec[ExtensionElement] =
    new JsonFieldCodec[ExtensionElement]:
      def isElement: Boolean = false
      def elementName: String = ""
      def encodeField(value: Any): Option[Json] = None
      def decodeMember(cursor: HCursor, memberName: String): Decoder.Result[ExtensionElement] =
        Left(DecodingFailure("foreign elements are collected by the derived codec", Nil))
      def decodeValue(json: Json): Decoder.Result[ExtensionElement] =
        Left(DecodingFailure("foreign elements are collected by the derived codec", Nil))
  end extensionElementFieldCodec

  // -- nodes ---------------------------------------------------------------------

  /**
   * Canonical `deriveOrSummon` pattern from the reference documentation: an inline given selected through
   * `summonInline`, which at its expansion site first looks for an existing Encoder/Decoder pair (hand codecs,
   * generated per-type givens) and otherwise derives both inline, right there. Self-recursive types such as
   * BundleItem resolve through their per-type givens, keeping the recursion at runtime.
   */
  inline given productFieldCodec[A <: scala.Product]: JsonFieldCodec[A] =
    val classTag = summonInline[ClassTag[A]]
    summonFrom {
      case encoder: Encoder[A] =>
        val decoder = summonFrom { case decoder: Decoder[A] => decoder }
        JsonFieldCodec.fromProduct(encoder, decoder, classTag)
      case _ =>
        val node = JsonDerived.derived[A](using summonInline[Mirror.ProductOf[A]], classTag)
        JsonFieldCodec.fromProduct(node._1, node._2, classTag)
    }
end JsonFieldCodec

/**
 * Compile-time derivation of circe Encoder/Decoder pairs for case classes. Every case-class field is
 * serialized through its [[JsonFieldCodec]] instance: scalar fields become members named by the normative
 * attribute name, node fields become members named by the child element name; `Option` fields are omitted when
 * absent, vector fields become arrays; `extensions`/`foreignElements` are handled through [[JsonForeign]].
 *
 * The entry points are inline *defs* rather than inline givens: inline givens cannot be found by ordinary
 * implicit search, so per-type non-inline givens are generated in `JsonDerivedInstances` and call these methods
 * directly; the inlining then happens here, at those call sites.
 */
object JsonDerived:

  /** Per-type element-name overrides the naming policy cannot derive (mirrors the XML hand-codec overrides). */
  private val ElementNameOverrides: Map[String, String] = Map(
    "DeviceModule" -> "Module",
    "TiffTag" -> "TIFFtag",
  )

  def elementNameOf(classTag: ClassTag[?]): String =
    val simpleName = classTag.runtimeClass.getSimpleName
    ElementNameOverrides.getOrElse(simpleName, Names.elementName(simpleName))

  inline def derivedEncoder[A <: scala.Product](using mirror: Mirror.ProductOf[A]): Encoder[A] =
    val labels = constValueTuple[mirror.MirroredElemLabels]
    val codecs = fieldCodecInstances[mirror.MirroredElemTypes]
    new DerivedJsonEncoder[A](labels, codecs)

  inline def derivedDecoder[A <: scala.Product](using mirror: Mirror.ProductOf[A], classTag: ClassTag[A]): Decoder[A] =
    val labels = constValueTuple[mirror.MirroredElemLabels]
    val codecs = fieldCodecInstances[mirror.MirroredElemTypes]
    new DerivedJsonDecoder[A](mirror, labels, codecs, Defaults.of(classTag))

  inline def derived[A <: scala.Product](using mirror: Mirror.ProductOf[A], classTag: ClassTag[A]): (Encoder[A], Decoder[A]) =
    (derivedEncoder[A], derivedDecoder[A])

  /**
   * Per-element instance collection: the tuple is walked with `summonInline` so every search is delayed until
   * inlining at the call site, where the field codecs (including this file's inline `productFieldCodec`) are
   * visible.
   */
  private inline def fieldCodecInstances[Elems <: Tuple]: Tuple =
    inline erasedValue[Elems] match
      case _: EmptyTuple      => EmptyTuple
      case _: (elem *: elems) => summonInline[JsonFieldCodec[elem]] *: fieldCodecInstances[elems]
end JsonDerived

/**
 * Runtime engine of the derivation. The field labels and the field codecs are produced at compile time by
 * [[JsonDerived.derivedEncoder]]/[[JsonDerived.derivedDecoder]]; encoding and decoding are plain runtime loops
 * over the fields. The decoder additionally takes the mirror (to rebuild the case class) and the reflective
 * defaults, so a missing optional member falls back to the declared default as on the XML side.
 */
final class DerivedJsonEncoder[A](
    labels: Tuple,
    codecs: Tuple,
) extends Encoder[A]:

  private val arity: Int = labels.productArity

  def apply(a: A): Json =
    val product = a.asInstanceOf[scala.Product]
    val members = Vector.newBuilder[(String, Json)]
    var index = 0
    while index < arity do
      val label = labelAt(index)
      val codec = codecAt(index)
      val value = product.productElement(index)
      label match
        case "extensions" =>
          members ++= JsonForeign.encodeExtensions(value.asInstanceOf[Extensions])
        case "foreignElements" =>
          members ++= JsonForeign.encodeExtensions(Extensions(elements = value.asInstanceOf[Vector[ExtensionElement]]))
        case _ =>
          val memberName = if codec.isElement then codec.elementName else Names.attributeName(label)
          codec.encodeField(value).foreach { json => members += ((memberName, json)) }
      index += 1
    Json.obj(members.result()*)

  private def labelAt(index: Int): String = labels.productElement(index).asInstanceOf[String]

  private def codecAt(index: Int): JsonFieldCodec[Any] = codecs.productElement(index).asInstanceOf[JsonFieldCodec[Any]]
end DerivedJsonEncoder

final class DerivedJsonDecoder[A](
    mirror: Mirror.ProductOf[A],
    labels: Tuple,
    codecs: Tuple,
    defaults: Defaults,
) extends Decoder[A]:

  private val arity: Int = labels.productArity

  private val expectedNames: Set[String] = buildExpectedNames()

  def apply(c: HCursor): Decoder.Result[A] =
    val unexpected = unexpectedMembers(c)
    val values = new Array[Any](arity)
    var index = 0
    var failure: Option[DecodingFailure] = None
    while index < arity && failure.isEmpty do
      val label = labelAt(index)
      val codec = codecAt(index)
      label match
        case "extensions" =>
          JsonForeign.decodeExtensions(c) match
            case Right(extensions) => values(index) = extensions
            case Left(error)       => failure = Some(error)
        case "foreignElements" =>
          JsonForeign.decodeForeignElements(c) match
            case Right(elements) => values(index) = elements
            case Left(error)     => failure = Some(error)
        case _ =>
          val memberName = if codec.isElement then codec.elementName else Names.attributeName(label)
          codec.decodeMember(c, memberName) match
            case Right(decoded) => values(index) = decoded
            case Left(error) =>
              defaults.get(index) match
                case Some(default) => values(index) = default
                case None          => failure = Some(error)
      index += 1
    for
      _ <- unexpected
      _ <- failure.toLeft(())
    yield mirror.fromProduct(Tuple.fromArray(values.asInstanceOf[Array[Object]]).asInstanceOf[scala.Product])

  private def labelAt(index: Int): String = labels.productElement(index).asInstanceOf[String]

  private def codecAt(index: Int): JsonFieldCodec[Any] = codecs.productElement(index).asInstanceOf[JsonFieldCodec[Any]]

  private def buildExpectedNames(): Set[String] =
    val names = Set.newBuilder[String]
    var index = 0
    while index < arity do
      val label = labelAt(index)
      val codec = codecAt(index)
      label match
        case "extensions"      => ()
        case "foreignElements" => ()
        case _ =>
          val memberName = if codec.isElement then codec.elementName else Names.attributeName(label)
          names += memberName
      index += 1
    names.result()

  /** Strict like the XML codec: unknown standard members are rejected; foreign keys and `"@context"` pass. */
  private def unexpectedMembers(c: HCursor): Either[DecodingFailure, Unit] =
    c.keys match
      case Some(keys) =>
        val unknown = keys
          .filter(key => !expectedNames.contains(key) && key != "@context" && !key.contains(":"))
          .toVector
          .sorted
        if unknown.isEmpty then Right(())
        else Left(DecodingFailure(s"unexpected member(s): ${unknown.mkString(", ")}", c.history))
      case None => Right(())
  end unexpectedMembers
end DerivedJsonDecoder
