package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.Header

/** Regression checks for CR-01 and the 2.2 XJMF field additions (HI-02). */
object ChannelAndResourceQueryChecks:
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val time = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
  private val header = Header(deviceId, time)
  private val url = UriRef.from("https://example.org/subscribe").toOption.get

  val channelModeVocabulary: Unit =
    assert(ChannelMode.values.toSet == Set(ChannelMode.FireAndForget, ChannelMode.Reliable))

  val subscriptionChannelModeIsOrderedList: Unit =
    val subscription = Subscription(url, channelMode = Vector(ChannelMode.FireAndForget, ChannelMode.Reliable))
    assert(subscription.channelMode.head == ChannelMode.FireAndForget)
    assert(subscription.channelMode == Vector(ChannelMode.FireAndForget, ChannelMode.Reliable))

  val signalChannelModeIsSingle: Unit =
    val message: Signal = SignalNotification(
      header,
      Notification(Severity.Event),
      channelMode = Some(ChannelMode.FireAndForget),
    )
    assert(message.channelMode.contains(ChannelMode.FireAndForget))

  val resourceQueryFiltersByTypes: Unit =
    val scopeType = Nmtoken.from("Product").toOption.get
    val params = ResourceQuParams(Scope.Allowed, types = Vector(scopeType))
    val query: Query = QueryResource(header, params)
    assert(query.elementName.localName == "QueryResource")
    assert(ResourceQuParams(Scope.Job).types.isEmpty)

  val signalResourceReplacementWindow: Unit =
    val after = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
    val before = XsdDateTime.from("2026-08-17T13:00:00+03:00").toOption.get
    val window = SignalResource(header, replaceAfter = Some(after), replaceBefore = Some(before))
    assert(window.validate.isEmpty)
    val inverted = SignalResource(header, replaceAfter = Some(before), replaceBefore = Some(after))
    assert(inverted.validate.nonEmpty)

  val subscriptionInfoCarriesLanguages: Unit =
    val channelId = Nmtoken.from("Q1").toOption.get
    val messageType = Nmtoken.from("SignalResource").toOption.get
    val subscription = Subscription(url)
    val info = SubscriptionInfo(channelId, messageType, subscription, languages = Vector(LanguageTag.from("en-US").toOption.get))
    assert(info.channelId.value == "Q1")
    assert(info.languages.nonEmpty)
end ChannelAndResourceQueryChecks
