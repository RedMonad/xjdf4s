package xjdf4s.messaging.generators

import scala.util.Random

import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*

/** XSD-safe generators for XJMF message trees, mirroring the XJDF document generators in the model test scope.
 *  They live in the messaging TEST scope and are shared with downstream modules through
 *  `dependsOn(messaging % "test->test")`.
 */
final class MessageGenerators(seed: Long):

  private val rng = new Random(seed)
  private var counter = 0

  private def nextName(prefix: String): String =
    counter += 1
    s"$prefix$counter"

  def nmtoken(prefix: String = "tok"): Nmtoken = Nmtoken.from(nextName(prefix)).toOption.get
  def xsdId(prefix: String = "id"): XsdId = XsdId.from(nextName(prefix)).toOption.get
  def uri: UriRef = UriRef.from(s"https://example.org/${nextName("file")}").toOption.get
  def float(low: Float, high: Float): Float = low + rng.nextFloat() * (high - low)
  def pick[A](values: A*): A = values(rng.nextInt(values.length))
  def maybe[A](probability: Double)(value: => A): Option[A] =
    if rng.nextDouble() < probability then Some(value) else None

  /** Optional vector field: either a 1..maxSize collection or the empty default. */
  def maybeVector[A](probability: Double, maxSize: Int)(value: => A): Vector[A] =
    if rng.nextDouble() < probability then Vector.fill(1 + rng.nextInt(maxSize))(value) else Vector.empty

  def dateTime: XsdDateTime =
    XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get

  def header(): Header =
    Header(
      deviceId = nmtoken("device"),
      time = dateTime,
      id = maybe(0.5)(xsdId("hdr")),
    )

  def subscription(): Subscription =
    Subscription(
      url = uri,
      channelMode = maybeVector(0.5, 1)(pick(ChannelMode.FireAndForget, ChannelMode.Reliable)),
      repeatTime = maybe(0.3)(float(1f, 60f)),
    )

  def resourceQuParams(): ResourceQuParams =
    ResourceQuParams(
      scope = pick(Scope.Allowed, Scope.Job, Scope.Present),
      resourceName = maybe(0.5)(nmtoken("resName")),
    )

  def message(): Message =
    pick[Message](
      QueryKnownMessages(header()),
      QueryResource(header(), resourceQuParams(), subscription = maybe(0.5)(subscription())),
      ResponseKnownMessages(header(), services = Vector(MessageService(nmtoken("svc")))),
      SignalResource(
        header(),
        resourceInfo = Vector(ResourceInfo(ResourceSet(nmtoken("set")))),
        channelMode = maybe(0.5)(pick(ChannelMode.FireAndForget, ChannelMode.Reliable)),
      ),
      SignalStatus(
        header(),
        DeviceInfo(DeviceStatus.Idle),
        channelMode = maybe(0.5)(pick(ChannelMode.FireAndForget, ChannelMode.Reliable)),
      ),
    )

  def xjmf(): XJMF =
    XJMF(header(), NonEmptyVector.one(message()))
end MessageGenerators
