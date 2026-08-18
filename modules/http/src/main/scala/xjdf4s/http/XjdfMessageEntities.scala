package xjdf4s.http

import cats.effect.IO

import org.http4s.{EntityDecoder, EntityEncoder}

import xjdf4s.codec.json.given
import xjdf4s.messaging.*

/**
 * Entity codecs for the single-message bodies of the 9.10.3 REST endpoints: the element name of the message is
 * implied by the endpoint, so the body is the bare message object (JSON) with the XJMF media type. Named values,
 * not givens — the same rationale as in [[XjdfEntities]].
 */
object XjdfMessageEntities:

  val queryStatusDecoder: EntityDecoder[IO, QueryStatus] =
    XjdfEntities.jsonEntityDecoder[QueryStatus](XjdfMediaTypes.xjmfJson)
  val queryStatusEncoder: EntityEncoder[IO, QueryStatus] =
    XjdfEntities.jsonEntityEncoder[QueryStatus](XjdfMediaTypes.xjmfJson)

  val responseStatusDecoder: EntityDecoder[IO, ResponseStatus] =
    XjdfEntities.jsonEntityDecoder[ResponseStatus](XjdfMediaTypes.xjmfJson)
  val responseStatusEncoder: EntityEncoder[IO, ResponseStatus] =
    XjdfEntities.jsonEntityEncoder[ResponseStatus](XjdfMediaTypes.xjmfJson)

  val commandStopPersistentChannelDecoder: EntityDecoder[IO, CommandStopPersistentChannel] =
    XjdfEntities.jsonEntityDecoder[CommandStopPersistentChannel](XjdfMediaTypes.xjmfJson)
  val commandStopPersistentChannelEncoder: EntityEncoder[IO, CommandStopPersistentChannel] =
    XjdfEntities.jsonEntityEncoder[CommandStopPersistentChannel](XjdfMediaTypes.xjmfJson)

  val responseStopPersistentChannelDecoder: EntityDecoder[IO, ResponseStopPersistentChannel] =
    XjdfEntities.jsonEntityDecoder[ResponseStopPersistentChannel](XjdfMediaTypes.xjmfJson)
  val responseStopPersistentChannelEncoder: EntityEncoder[IO, ResponseStopPersistentChannel] =
    XjdfEntities.jsonEntityEncoder[ResponseStopPersistentChannel](XjdfMediaTypes.xjmfJson)

  val responseKnownDevicesEncoder: EntityEncoder[IO, ResponseKnownDevices] =
    XjdfEntities.jsonEntityEncoder[ResponseKnownDevices](XjdfMediaTypes.xjmfJson)

  val responseSubmitQueueEntryDecoder: EntityDecoder[IO, ResponseSubmitQueueEntry] =
    XjdfEntities.jsonEntityDecoder[ResponseSubmitQueueEntry](XjdfMediaTypes.xjmfJson)
  val responseSubmitQueueEntryEncoder: EntityEncoder[IO, ResponseSubmitQueueEntry] =
    XjdfEntities.jsonEntityEncoder[ResponseSubmitQueueEntry](XjdfMediaTypes.xjmfJson)
end XjdfMessageEntities
