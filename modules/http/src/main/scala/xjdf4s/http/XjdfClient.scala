package xjdf4s.http

import cats.effect.IO
import fs2.Stream
import fs2.text

import org.http4s.{Client, Method, Request}
import org.http4s.implicits.*

import xjdf4s.codec.json.given
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.XJDF

/**
 * The client side of the demo transport: submits XJDF documents, subscribes for status signals and streams the
 * delivered signals. The same stage 04/05 codecs back every entity — no duplicated encoding logic.
 */
object XjdfClient:

  /** POST /submit: sends the XJDF document (XML) and returns the submit receipt. */
  def submit(client: Client[IO], document: XJDF): IO[ResponseSubmitQueueEntry] =
    val request = Request[IO](Method.POST, uri"/submit").withEntity(document)(XjdfEntities.xjdfXmlEncoder)
    client.expect[ResponseSubmitQueueEntry](request)(XjdfMessageEntities.responseSubmitQueueEntryDecoder)

  /** POST /status/subscribe: opens a persistent status channel and returns the initial response (9.6.2). */
  def subscribeStatus(client: Client[IO], query: QueryStatus): IO[ResponseStatus] =
    val request =
      Request[IO](Method.POST, uri"/status/subscribe").withEntity(query)(XjdfMessageEntities.queryStatusEncoder)
    client.expect[ResponseStatus](request)(XjdfMessageEntities.responseStatusDecoder)

  /**
   * GET /channels/{id}/signals: the subscription stream, one JSON XJMF envelope per line (the vendor framing
   * documented in [[XjdfServer]]). The stream ends when the server closes the channel or the client cancels.
   */
  def signals(client: Client[IO], channelId: Nmtoken): Stream[IO, XJMF] =
    client
      .stream(Request[IO](Method.GET, uri"/channels" / channelId.value / "signals"))
      .flatMap { response =>
        response.bodyText
          .through(text.lines)
          .filter(_.nonEmpty)
          .evalMap { line =>
            IO.fromEither(
              io.circe.parser
                .parse(line)
                .flatMap(_.as[XJMF])
                .left
                .map(error => new IllegalArgumentException(s"signal frame: $error")),
            )
          }
      }
end XjdfClient
