package xjdf4s.http

import scala.concurrent.duration.*

/**
 * One munit test per stage 07 check: a hung scenario fails with its own name and timeout instead of taking the
 * whole suite down through a shared eager initializer.
 */
class HttpChecksSuite extends munit.FunSuite:

  test("entity xml round trip".timeout(15.seconds)) {
    XjdfEntityChecks.xmlRoundTrip
  }

  test("entity json round trip".timeout(15.seconds)) {
    XjdfEntityChecks.jsonRoundTrip
  }

  test("xjmf entity round trip".timeout(15.seconds)) {
    XjdfEntityChecks.xjmfRoundTrip
  }

  test("rejects wrong mime type".timeout(15.seconds)) {
    XjdfEntityChecks.rejectsWrongMimeType
  }

  test("message entity round trip".timeout(15.seconds)) {
    XjdfEntityChecks.messageEntityRoundTrip
  }

  test("diagnostic: submit over http".timeout(15.seconds)) {
    XjdfServerChecks.submitOverHttp()
  }

  test("diagnostic: subscribe over http".timeout(15.seconds)) {
    XjdfServerChecks.subscribeOverHttp()
  }

  test("diagnostic: hub delivery".timeout(15.seconds)) {
    XjdfServerChecks.hubDelivery()
  }

  test("end to end demo".timeout(15.seconds)) {
    XjdfServerChecks.endToEndDemo()
  }

  test("subscription stream route".timeout(15.seconds)) {
    XjdfServerChecks.streamRoute()
  }

  test("stop channel".timeout(15.seconds)) {
    XjdfServerChecks.stopChannel()
  }

  test("body limit".timeout(15.seconds)) {
    XjdfServerChecks.bodyLimit()
  }

  test("transport await timeout".timeout(15.seconds)) {
    XjdfIoChecks.transportTimeout
  }

  test("await cancellation cleanup".timeout(15.seconds)) {
    XjdfIoChecks.awaitCancellation
  }

  test("effectful document builder".timeout(15.seconds)) {
    XjdfIoChecks.documentInterpreter
  }
end HttpChecksSuite
