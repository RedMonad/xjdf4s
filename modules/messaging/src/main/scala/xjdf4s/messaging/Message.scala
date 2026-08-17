package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.{HasHeader, Header, Notification}

enum ChannelMode derives CanEqual:
  case Reliable, Simulate, Transactional, Unreliable
end ChannelMode

final case class Subscription(
    url: UriRef,
    channelMode: Option[ChannelMode] = None,
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
end Message

trait Query extends Message:
  def languages: Vector[LanguageTag]
  def subscription: Option[Subscription]
end Query

trait Command extends Message

trait Signal extends Message:
  def channelMode: Option[ChannelMode]
end Signal

trait Response extends Message:
  def returnCode: Option[Int]
  def notification: Option[Notification]
end Response

/** Family-safe records for ICS and foreign-namespace message extensions outside the 44 standard messages. */
final case class QueryMessage(
    elementName: QualifiedName,
    header: Header,
    content: Vector[ExtensionElement] = Vector.empty,
    languages: Vector[LanguageTag] = Vector.empty,
    subscription: Option[Subscription] = None,
    extensions: Extensions = Extensions.empty,
) extends Query

final case class CommandMessage(
    elementName: QualifiedName,
    header: Header,
    content: Vector[ExtensionElement] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends Command

final case class SignalMessage(
    elementName: QualifiedName,
    header: Header,
    content: Vector[ExtensionElement] = Vector.empty,
    channelMode: Option[ChannelMode] = None,
    extensions: Extensions = Extensions.empty,
) extends Signal

final case class ResponseMessage(
    elementName: QualifiedName,
    header: Header,
    content: Vector[ExtensionElement] = Vector.empty,
    returnCode: Option[Int] = None,
    notification: Option[Notification] = None,
    extensions: Extensions = Extensions.empty,
) extends Response
