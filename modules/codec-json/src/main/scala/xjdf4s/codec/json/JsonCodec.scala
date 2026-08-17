package xjdf4s.codec.json

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax.*

export JsonNodeCodecs.given
export JsonScalars.given
export JsonMediaCodecs.given
export JsonMessagingCodecs.given

/**
 * JSON codec helpers. Member names follow the normative JSON convention: attribute names WITHOUT the leading
 * `@` (`@JobID` -> `"JobID"`); list-typed attribute values become JSON arrays; child elements become nested
 * objects (optional) or arrays of objects (repeated).
 */
object JsonCodec:

  def member(name: String, value: Json): (String, Json) = (name, value)

  def optMember[A: Encoder](name: String, value: Option[A]): Vector[(String, Json)] =
    value.toVector.map(item => (name, item.asJson))

  def vecMember[A: Encoder](name: String, values: Vector[A]): Vector[(String, Json)] =
    if values.nonEmpty then Vector((name, Json.arr(values.map(_.asJson)*))) else Vector.empty

  def vecMemberOf[A](name: String, values: Vector[A])(encode: A => Json): Vector[(String, Json)] =
    if values.nonEmpty then Vector((name, Json.arr(values.map(encode)*))) else Vector.empty

  /** Optional member: absent member decodes to None; present member is decoded with the element decoder. */
  def opt[A: Decoder](cursor: HCursor, name: String): Decoder.Result[Option[A]] =
    cursor.downField(name).focus match
      case Some(json) => json.as[A].map(Some(_))
      case None       => Right(None)

  /** Optional vector member: absent member decodes to the empty vector. */
  def vec[A: Decoder](cursor: HCursor, name: String): Decoder.Result[Vector[A]] =
    cursor.downField(name).focus match
      case Some(json) => json.as[List[A]].map(_.toVector)
      case None       => Right(Vector.empty)

  /** Required member. */
  def req[A: Decoder](cursor: HCursor, name: String): Decoder.Result[A] =
    cursor.get[A](name)

  def fail(cursor: HCursor, message: String): Decoder.Result[Nothing] =
    Left(io.circe.DecodingFailure(message, cursor.history))

  def memberList(members: Vector[(String, Json)]*): List[(String, Json)] =
    members.flatten.toList

  /** Object builder over the flattened member list (avoids varargs splatting at every call site). */
  def obj(members: List[(String, Json)]): Json = Json.obj(members*)

  /** Root-level JSON options: the optional `$schema` member (SHOULD, per the normative tables). */
  def withSchema(json: Json, schemaUri: String): Json =
    json.mapObject(_.add("$schema", Json.fromString(schemaUri)))

  /** The `"Name"` member every root object carries (`"XJDF"` / `"XJMF"`). */
  def rootName(name: String): (String, Json) = ("Name", Json.fromString(name))
end JsonCodec
