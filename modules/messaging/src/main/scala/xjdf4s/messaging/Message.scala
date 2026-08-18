package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.{HasHeader, Header, Notification}

/** Table A.10: the reliability mode of a message channel. */
enum ChannelMode derives CanEqual:
  case FireAndForget, Reliable

/** Table 7.5. `@ChannelMode` is an ordered list of channel modes with the most preferred mode first; keeping the list
 *  ordered preserves the preference semantics of a persistent channel subscription. `@Languages` is Deprecated in
 *  XJDF 2.2 (the Query and the `SubscriptionInfo` element now carry the language selection) and is retained only for
 *  deprecated-backward compatibility.
 */
final case class Subscription(
    url: UriRef,
    channelMode: Vector[ChannelMode] = Vector.empty,
    languages: Vector[LanguageTag] = Vector.empty,
    repeatTime: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

/** Open protocol message substitution point. */
trait Message extends XjdfNode,
      HasHeader:
  def elementName: QualifiedName
  def extensions: Extensions

/** The family contract of Table 7.4 defines the common message attributes, but `@Languages` is only listed by the
 *  concrete tables of QueryNotification, QueryKnownDevices, QueryResource and QueryStatus. The trait therefore does
 *  not force a `languages` member; each query carries it exactly where the normative table does.
 */
trait Query extends Message:
  def subscription: Option[Subscription]

trait Command extends Message

trait Signal extends Message:
  def channelMode: Option[ChannelMode]

trait Response extends Message:
  def returnCode: Option[Int]
  def notification: Option[Notification]

/** Family-safe records for ICS and foreign-namespace message extensions outside the 44 standard messages. Each
 *  constructor takes a [[ForeignQName]], so a standard XJMF message name can never be smuggled through the generic
 *  fallback; the trait accessor re-exposes the name as a plain `QualifiedName`.
 */
final case class QueryMessage(
    foreignName: ForeignQName,
    header: Header,
    content: Vector[ExtensionElement] = Vector.empty,
    languages: Vector[LanguageTag] = Vector.empty,
    subscription: Option[Subscription] = None,
    extensions: Extensions = Extensions.empty,
) extends Query:
  def elementName: QualifiedName = foreignName.qualifiedName

final case class CommandMessage(
    foreignName: ForeignQName,
    header: Header,
    content: Vector[ExtensionElement] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends Command:
  def elementName: QualifiedName = foreignName.qualifiedName

final case class SignalMessage(
    foreignName: ForeignQName,
    header: Header,
    content: Vector[ExtensionElement] = Vector.empty,
    channelMode: Option[ChannelMode] = None,
    extensions: Extensions = Extensions.empty,
) extends Signal:
  def elementName: QualifiedName = foreignName.qualifiedName

final case class ResponseMessage(
    foreignName: ForeignQName,
    header: Header,
    content: Vector[ExtensionElement] = Vector.empty,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response:
  def elementName: QualifiedName = foreignName.qualifiedName
