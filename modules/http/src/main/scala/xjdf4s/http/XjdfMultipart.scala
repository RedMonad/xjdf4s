package xjdf4s.http

import cats.effect.IO
import fs2.Stream

import io.circe.syntax.*

import org.http4s.multipart.{Multipart, Multiparts, Part}

import java.nio.charset.StandardCharsets

import xjdf4s.codec.json.given
import xjdf4s.messaging.XJMF

/**
 * 9.10.4.2 (Example 9.13): JSON XJMFs with a CommandSubmitQueueEntry, CommandResubmitQueueEntry or
 * CommandReturnQueueEntry MAY be packaged as multipart/form-data. The XJMF goes into the "xjmf" field and the
 * referenced XJDF plus any additional files go into "attachment" parts, referenced by the part filename as a
 * relative URL. This helper packages and unpacks exactly that shape; it is transport-agnostic (the parts are
 * `Multipart[IO]` values - the HTTP layer attaches them to requests or reads them from request bodies).
 */
object XjdfMultipart:

  /** A file attachment: referenced from the XJMF by the relative URL "filename" (9.10.4.2). */
  final case class Attachment(filename: String, content: Array[Byte])

  /** Packages the XJMF into the "xjmf" field and the files into "attachment" parts. */
  def packageSubmission(
      multiparts: Multiparts[IO],
      xjmf: XJMF,
      attachments: Vector[Attachment],
  ): IO[Multipart[IO]] =
    multiparts.multipart(
      Vector(Part.formData[IO]("xjmf", xjmf.asJson.noSpaces)) ++
        attachments.map(attachment =>
          Part.fileData[IO]("attachment", attachment.filename, Stream.emits(attachment.content.toSeq)),
        ),
    )

  /** Extracts the XJMF field and the attachments of a multipart submission. */
  def partsOf(multipart: Multipart[IO]): IO[(XJMF, Vector[Attachment])] =
    for
      xjmfText <- multipart.parts
        .find(_.name.contains("xjmf"))
        .toRight(new IllegalArgumentException("multipart submission without an 'xjmf' field"))
        .fold(IO.raiseError[String], part => part.body.compile.toVector.map(bytes => new String(bytes.toArray, StandardCharsets.UTF_8)))
      xjmf <- IO.fromEither(
        io.circe.parser
          .parse(xjmfText)
          .flatMap(_.as[XJMF])
          .left
          .map(error => new IllegalArgumentException(s"xjmf field: $error")),
      )
      attachments <- multipart.parts
        .collect {
          case part if part.name.contains("attachment") && part.filename.nonEmpty =>
            part.body.compile.toVector.map(bytes => Attachment(part.filename.get, bytes.toArray))
        }
        .sequence
    yield (xjmf, attachments)
end XjdfMultipart
