package xjdf4s.messaging

/** Discovered munit suite that forces all eager checks of the messaging module (MD-08). */
class MessagingChecksSuite extends munit.FunSuite:
  test("all messaging checks pass") {
    val results: Vector[Unit] = Vector(
      ConcreteMessageChecks.statusMessages,
      ConcreteMessageChecks.resourceCommand,
      ConcreteMessageChecks.notificationSignal,
      ControlMessageChecks.pipeCommand,
      ControlMessageChecks.shutdownWithoutParams,
      ControlMessageChecks.wakeUp,
      GangAndQueueChecks.forceGang,
      GangAndQueueChecks.queueSignal,
      KnownMessageChecks.knownDevices,
      KnownMessageChecks.knownMessages,
      KnownMessageChecks.knownSubscriptions,
      QueueEntryMessageChecks.exclusiveMoveTarget,
      QueueEntryMessageChecks.resubmission,
      QueueEntryMessageChecks.submissionPosition,
      QueueEntryMessageChecks.priorityBounds,
      ChannelAndResourceQueryChecks.channelModeVocabulary,
      ChannelAndResourceQueryChecks.subscriptionChannelModeIsOrderedList,
      ChannelAndResourceQueryChecks.signalChannelModeIsSingle,
      ChannelAndResourceQueryChecks.resourceQueryFiltersByTypes,
      ChannelAndResourceQueryChecks.signalResourceReplacementWindow,
      ChannelAndResourceQueryChecks.subscriptionInfoCarriesLanguages,
    )
    assert(results.size == 21)
  }
