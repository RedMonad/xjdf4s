package xjdf4s.http

import cats.effect.IO
import fs2.Stream
import fs2.text

import io.circe.syntax.*

import org.http4s.{Entity, EntityEncoder, Headers, HttpApp, HttpRoutes, Response, Status}
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.server.middleware.EntityLimiter

import xjdf4s.codec.json.given
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.{Header, XJDF}

/**
 * The REST surface of the device side (Table 9.5): `POST /submit`, `POST /status/subscribe`,
 * `GET /channels/{id}/signals`, `POST /subscriptions/stop` and `GET /devices`. All endpoints are POST-only for
 * messages (9.10.4.1); the only GET is the subscription stream.
 *
 * The subscription stream is a documented vendor extension: the specification does not define an HTTP framing
 * for persistent channels, so each frame is one JSON XJMF envelope per line over a chunked response — the
 * stage 05 exactly-one-message exception keeps every line independently decodable.
 */
object XjdfServer:

  /** One JSON XJMF envelope per line: the framing of the subscription stream. */
  val xjmfJsonLinesEncoder: EntityEncoder[IO, Stream[IO, XJMF]] =
    EntityEncoder.encodeBy[IO, Stream[IO, XJMF]](Headers(`Content-Type`(XjdfMediaTypes.xjmfJson))) { stream =>
      Entity(stream.map(xjmf => xjmf.asJson.noSpaces + "\n").through(text.utf8.encode), None)
    }

  /** The stage 03/06 algebras are untouched; responses are built manually to keep the entity encoder explicit. */
  private def withStatus[A](status: Status, body: A)(using encoder: EntityEncoder[IO, A]): IO[Response[IO]] =
    IO.pure(Response[IO](status).withEntity(body)(using encoder))

  def routes(hub: XjdfHub): HttpRoutes[IO] =
    HttpRoutes.of[IO] {

      case request @ POST -> Root / "submit" =>
        for
          xjdf <- request.as[XJDF](using XjdfEntities.xjdfXmlDecoder)
          _ <- hub.submit(xjdf)
          receipt = ResponseSubmitQueueEntry(
            XjdfHttp.responseHeader(XjdfHttp.serverDeviceId, xjdf.jobId, "receipt"),
            returnCode = Some(0),
          )
          response <- withStatus(Status.Ok, receipt)(using XjdfMessageEntities.responseSubmitQueueEntryEncoder)
        yield response

      case request @ POST -> Root / "status" / "subscribe" =>
        for
          query <- request.as[QueryStatus](using XjdfMessageEntities.queryStatusDecoder)
          response <- hub.subscribeStatus(query).flatMap {
            case Some(receipt) => withStatus(Status.Ok, receipt)(using XjdfMessageEntities.responseStatusEncoder)
            case None =>
              BadRequest("a Query with a Subscription SHALL carry Header/@ID (Table 7.4)")
          }
        yield response

      case GET -> Root / "channels" / channelId / "signals" =>
        Nmtoken.from(channelId) match
          case Left(_) => BadRequest(s"'$channelId' is not a valid channel id")
          case Right(id) =>
            hub.signals(id).flatMap {
              case Some(stream) => withStatus(Status.Ok, stream.map(hub.envelope))(using XjdfServer.xjmfJsonLinesEncoder)
              case None         => NotFound(s"unknown channel '$channelId'")
            }

      case request @ POST -> Root / "subscriptions" / "stop" =>
        for
          command <- request.as[CommandStopPersistentChannel](using XjdfMessageEntities.commandStopPersistentChannelDecoder)
          _ <- command.params.channelId match
            case Some(channelId) => hub.closeChannel(channelId)
            case None            => IO.unit
          receipt = ResponseStopPersistentChannel(command.header, returnCode = Some(0))
          response <- withStatus(Status.Ok, receipt)(using XjdfMessageEntities.responseStopPersistentChannelEncoder)
        yield response

      case GET -> Root / "devices" =>
        withStatus(
          Status.Ok,
          ResponseKnownDevices(
            Header(XjdfHttp.serverDeviceId, XjdfHttp.now),
            devices = Vector.empty,
            returnCode = Some(0),
          ),
        )(using XjdfMessageEntities.responseKnownDevicesEncoder)
    }

  def app(hub: XjdfHub): HttpApp[IO] = routes(hub).orNotFound

  /** The server with the request-body limit applied (the stage DoD: oversized bodies are rejected). */
  def limitedApp(hub: XjdfHub, limit: Long): HttpApp[IO] =
    EntityLimiter.httpApp[IO](app(hub), limit = limit)
end XjdfServer
