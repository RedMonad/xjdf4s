package xjdf4s.http

import cats.data.EitherT
import cats.effect.IO
import fs2.Stream

import io.circe.{DecodingFailure, Json}
import io.circe.syntax.*

import org.http4s.headers.`Content-Type`
import org.http4s.{EntityDecoder, EntityEncoder, Headers, MalformedMessageBodyFailure, Media, MediaTypeMismatch}

import java.nio.charset.StandardCharsets

import xjdf4s.codec.json.given
import xjdf4s.codec.xml.{XjdfCodec, XjmfCodec, Xml, XmlError, XmlParser, XmlWriter}
import xjdf4s.messaging.XJMF
import xjdf4s.model.XJDF

/**
 * HTTP entity codecs for the XJDF/XJMF documents, built directly over the stage 04/05 codecs. Two deliberate
 * choices, documented here because they deviate from the http4s defaults:
 *
 *  1. The codecs are named values, not givens: both representations (XML and JSON) exist for the same domain
 *     type, so a given instance would be ambiguous; endpoints pick their representation explicitly.
 *  2. Decoding is strict about the media type: http4s only uses a decoder's declared types when *chaining*
 *     decoders and ignores them otherwise, so each decoder verifies the request Content-Type itself and fails
 *     with `MediaTypeMismatch` — the Definition-of-Done requirement that wrong MIME types are rejected.
 */
object XjdfEntities:

  private def utf8(text: String): Array[Byte] = text.getBytes(StandardCharsets.UTF_8)

  private def decodeFailure(message: String): MalformedMessageBodyFailure = MalformedMessageBodyFailure(message)

  /** Media-type-checking decoder skeleton: only a matching Content-Type reaches `decode`. */
  private def strictDecodeBy[A](mediaType: org.http4s.MediaType)(
      decode: String => Either[MalformedMessageBodyFailure, A],
  ): EntityDecoder[IO, A] =
    EntityDecoder.decodeBy[IO, A](mediaType) { media =>
      val result: IO[Either[org.http4s.DecodeFailure, A]] =
        media.headers.get[`Content-Type`] match
          case Some(contentType)
              if contentType.mediaType.mainType == mediaType.mainType &&
                contentType.mediaType.subType == mediaType.subType =>
            media.as[String].map(text => decode(text))
          case other =>
            IO.pure(Left(MediaTypeMismatch(mediaType, other.map(_.mediaType)): Either[org.http4s.DecodeFailure, A]))
      EitherT(result)
    }

  private def xmlBody[A](decode: Xml.Element => Either[XmlError, A])(
      text: String,
  ): Either[MalformedMessageBodyFailure, A] =
    for
      parsed <- XmlParser.parse(text).left.map(error => decodeFailure(error.toString))
      decoded <- decode(parsed).left.map(error => decodeFailure(error.toString))
    yield decoded

  private def jsonBody[A](decode: Json => Either[DecodingFailure, A])(
      text: String,
  ): Either[MalformedMessageBodyFailure, A] =
    for
      parsed <- io.circe.parser.parse(text).left.map(error => decodeFailure(error.toString))
      decoded <- decode(parsed).left.map(error => decodeFailure(error.toString))
    yield decoded

  // -- XJDF ----------------------------------------------------------------------

  val xjdfXmlEncoder: EntityEncoder[IO, XJDF] =
    EntityEncoder.encodeBy[IO, XJDF](Headers(`Content-Type`(XjdfMediaTypes.xjdfXmlUtf8))) { xjdf =>
      Stream.emits(utf8(XmlWriter.write(XjdfCodec.encoder.encode(xjdf)))).covary[IO]
    }

  val xjdfXmlDecoder: EntityDecoder[IO, XJDF] =
    strictDecodeBy(XjdfMediaTypes.xjdfXml)(xmlBody(XjdfCodec.decoder.decode))

  val xjdfJsonEncoder: EntityEncoder[IO, XJDF] =
    EntityEncoder.encodeBy[IO, XJDF](Headers(`Content-Type`(XjdfMediaTypes.xjdfJson))) { xjdf =>
      Stream.emits(utf8(xjdf.asJson.noSpaces)).covary[IO]
    }

  val xjdfJsonDecoder: EntityDecoder[IO, XJDF] =
    strictDecodeBy(XjdfMediaTypes.xjdfJson)(jsonBody(json => json.as[XJDF]))

  // -- XJMF ----------------------------------------------------------------------

  val xjmfXmlEncoder: EntityEncoder[IO, XJMF] =
    EntityEncoder.encodeBy[IO, XJMF](Headers(`Content-Type`(XjdfMediaTypes.xjmfXmlUtf8))) { xjmf =>
      Stream.emits(utf8(XmlWriter.write(XjmfCodec.encoder.encode(xjmf)))).covary[IO]
    }

  val xjmfXmlDecoder: EntityDecoder[IO, XJMF] =
    strictDecodeBy(XjdfMediaTypes.xjmfXml)(xmlBody(XjmfCodec.decoder.decode))

  val xjmfJsonEncoder: EntityEncoder[IO, XJMF] =
    EntityEncoder.encodeBy[IO, XJMF](Headers(`Content-Type`(XjdfMediaTypes.xjmfJson))) { xjmf =>
      Stream.emits(utf8(xjmf.asJson.noSpaces)).covary[IO]
    }

  val xjmfJsonDecoder: EntityDecoder[IO, XJMF] =
    strictDecodeBy(XjdfMediaTypes.xjmfJson)(jsonBody(json => json.as[XJMF]))

  // -- message elements (single messages as bodies, REST endpoints of 9.10.3) -----

  /** A single XJMF message element as a JSON body: the element name is implied by the endpoint. */
  def jsonEntityEncoder[A](mediaType: org.http4s.MediaType)(using encoder: io.circe.Encoder[A]): EntityEncoder[IO, A] =
    EntityEncoder.encodeBy[IO, A](Headers(`Content-Type`(mediaType))) { value =>
      Stream.emits(utf8(value.asJson.noSpaces)).covary[IO]
    }

  /** The strict counterpart of [[jsonEntityEncoder]]. */
  def jsonEntityDecoder[A](mediaType: org.http4s.MediaType)(using decoder: io.circe.Decoder[A]): EntityDecoder[IO, A] =
    strictDecodeBy(mediaType)(jsonBody(json => json.as[A]))
end XjdfEntities
