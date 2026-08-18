package xjdf4s.http

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import org.http4s.{MediaTypeMismatch, Method, Request}
import org.http4s.headers.`Content-Type`

import xjdf4s.codec.json.given
import xjdf4s.codec.json.JsonRootCodecs.given
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.{Header, Notification, XJDF}

/**
 * The entity layer of stage 07: round-trips through the stage 04/05 codecs and strict media-type checking
 * (Table 9.1; wrong MIME types are rejected with MediaTypeMismatch).
 */
object XjdfEntityChecks:

  private val jobId = Nmtoken.from("job-1").toOption.get
  private val process = Nmtoken.from("Product").toOption.get
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val time = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
  private val header = Header(deviceId, time)
  private val url = UriRef.from("https://example.com/xjmfurl").toOption.get

  private def document: XJDF = XJDF(jobId, NonEmptyVector.one(process))

  private def envelope: XJMF =
    XJMF(header, NonEmptyVector.one(SignalNotification(header, Notification(Severity.Event))), Some(Version.V2_2))

  val xmlRoundTrip: Unit =
    val request = Request[IO](Method.POST).withEntity(document)(using XjdfEntities.xjdfXmlEncoder)
    val contentType = request.headers.get[`Content-Type`].map(_.mediaType).get
    assert(contentType.mainType == XjdfMediaTypes.xjdfXml.mainType)
    assert(contentType.subType == XjdfMediaTypes.xjdfXml.subType)
    val decoded = request.as[XJDF](using XjdfEntities.xjdfXmlDecoder).unsafeRunSync()
    assert(decoded == document)

  val jsonRoundTrip: Unit =
    val request = Request[IO](Method.POST).withEntity(document)(using XjdfEntities.xjdfJsonEncoder)
    val decoded = request.as[XJDF](using XjdfEntities.xjdfJsonDecoder).unsafeRunSync()
    assert(decoded == document)

  val xjmfRoundTrip: Unit =
    val xmlRequest = Request[IO](Method.POST).withEntity(envelope)(using XjdfEntities.xjmfXmlEncoder)
    assert(xmlRequest.as[XJMF](using XjdfEntities.xjmfXmlDecoder).unsafeRunSync() == envelope)
    val jsonRequest = Request[IO](Method.POST).withEntity(envelope)(using XjdfEntities.xjmfJsonEncoder)
    assert(jsonRequest.as[XJMF](using XjdfEntities.xjmfJsonDecoder).unsafeRunSync() == envelope)

  /** Definition of Done: a decoder rejects a body whose Content-Type is the wrong representation. */
  val rejectsWrongMimeType: Unit =
    val xmlBody = Request[IO](Method.POST).withEntity(document)(using XjdfEntities.xjdfXmlEncoder)
    xmlBody.as[XJDF](using XjdfEntities.xjdfJsonDecoder).attempt.unsafeRunSync() match
      case Left(_: MediaTypeMismatch) => ()
      case other                      => assert(false, s"expected MediaTypeMismatch, got $other")
    val jsonBody = Request[IO](Method.POST).withEntity(document)(using XjdfEntities.xjdfJsonEncoder)
    jsonBody.as[XJDF](using XjdfEntities.xjdfXmlDecoder).attempt.unsafeRunSync() match
      case Left(_: MediaTypeMismatch) => ()
      case other                      => assert(false, s"expected MediaTypeMismatch, got $other")

  /** A single message element as a JSON body: the endpoint implies the element name (9.10.3). */
  val messageEntityRoundTrip: Unit =
    val query = QueryStatus(header, subscription = Some(Subscription(url, channelMode = Vector(ChannelMode.Reliable))))
    val encoder = XjdfEntities.jsonEntityEncoder[QueryStatus](XjdfMediaTypes.xjmfJson)
    val decoder = XjdfEntities.jsonEntityDecoder[QueryStatus](XjdfMediaTypes.xjmfJson)
    val request = Request[IO](Method.POST).withEntity(query)(using encoder)
    val decoded = request.as[QueryStatus](using decoder).unsafeRunSync()
    assert(decoded == query)
end XjdfEntityChecks
