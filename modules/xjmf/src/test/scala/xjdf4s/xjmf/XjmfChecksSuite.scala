package xjdf4s.xjmf

/** Discovered munit suite that forces all eager checks of the xjmf module. */
class XjmfChecksSuite extends munit.FunSuite:
  test("all xjmf checks pass") {
    val results: Vector[Unit] = Vector(
      XjmfTransportChecks.subscriptionScenario,
      XjmfTransportChecks.initialQueryResponse,
      XjmfTransportChecks.replacementWindows,
      XjmfTransportChecks.channelModes,
      XjmfTransportChecks.tracesAgree,
      XjmfTransportChecks.routingFailures,
      XjmfTransportChecks.reopenReplaces,
    )
    assert(results.size == 7)
  }
