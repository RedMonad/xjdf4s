package xjdf4s.http

import cats.effect.IO
import fs2.Stream
import fs2.text
import org.http4s.{Method, Request}
import org.http4s.client.Client
import org.http4s.implicits.*
import xjdf4s.codec.json.given
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.XJDF

/** The client side of the demo transport: submits XJDF documents, subscribes for status signals and streams the
 *  delivered signals. The same stage 04/05 codecs back every entity — no duplicated encoding logic.
 */
object XjdfClient:

  /** POST /submit: sends the XJDF document (XML) and returns the submit receipt. */
  def submit(client: Client[IO], document: XJDF): IO[ResponseSubmitQueueEntry] =
    val request = Request[IO](Method.POST, uri"/submit").withEntity(document)(using XjdfEntities.xjdfXmlEncoder)
    client.expect[ResponseSubmitQueueEntry](request)(using XjdfMessageEntities.responseSubmitQueueEntryDecoder)

  /** POST /status/subscribe: opens a persistent status channel and returns the initial response (9.6.2). */
  def subscribeStatus(client: Client[IO], query: QueryStatus): IO[ResponseStatus] =
    val request =
      Request[IO](Method.POST, uri"/status/subscribe").withEntity(query)(using XjdfMessageEntities.queryStatusEncoder)
    client.expect[ResponseStatus](request)(using XjdfMessageEntities.responseStatusDecoder)

  /** GET /channels/{id}/signals: the subscription stream, one JSON XJMF envelope per line (the vendor framing
   *  documented in [[XjdfServer]]). The stream ends when the server closes the channel or the client cancels.
   *
   *  NOTE for in-memory testing: `Client.fromHttpApp` pumps the response body through a synchronous channel and
   *  its finalizer drains that channel, which only finishes when the producer ends - with an infinite
   *  subscription stream the finalizer deadlocks on completion/cancellation. Tests consume [[framesOf]] from a
   *  direct `app.run(...)` response instead; socket-backed clients (ember) are unaffected.
   */
  def signals(client: Client[IO], channelId: Nmtoken): Stream[IO, XJMF] =
    client
      .stream(Request[IO](Method.GET, uri"/channels" / channelId.value / "signals"))
      .flatMap(framesOf)

  /** Decodes one JSON-Lines frame of the subscription stream (the vendor framing of [[XjdfServer]]). */
  def decodeFrame(line: String): Either[Throwable, XJMF] =
    io.circe.parser
      .parse(line)
      .flatMap(_.as[XJMF])
      .left
      .map(error => new IllegalArgumentException(s"signal frame: $error"))

  /** The signal frames of a raw subscription response, used by tests over a direct `app.run` call. */
  def framesOf(response: org.http4s.Response[IO]): Stream[IO, XJMF] =
    response.bodyText
      .through(text.lines)
      .filter(_.nonEmpty)
      .evalMap(line => IO.fromEither(decodeFrame(line)))
end XjdfClient
