package xjdf4s.http

import scala.concurrent.duration.*

/** One munit test per stage 07 check: a hung scenario fails with its own name (the demo scenarios additionally
 *  carry racePair step guards that name the exact step) instead of taking the whole suite down through a shared
 *  eager initializer. The suite-level munitTimeout is the final safety net.
 */
class HttpChecksSuite extends munit.FunSuite:

  override def munitTimeout: Duration = 30.seconds

  test("entity xml round trip") {
    XjdfEntityChecks.xmlRoundTrip
  }

  test("entity json round trip") {
    XjdfEntityChecks.jsonRoundTrip
  }

  test("xjmf entity round trip") {
    XjdfEntityChecks.xjmfRoundTrip
  }

  test("rejects wrong mime type") {
    XjdfEntityChecks.rejectsWrongMimeType
  }

  test("message entity round trip") {
    XjdfEntityChecks.messageEntityRoundTrip
  }

  test("diagnostic: submit over http") {
    XjdfServerChecks.submitOverHttp()
  }

  test("diagnostic: subscribe over http") {
    XjdfServerChecks.subscribeOverHttp()
  }

  test("diagnostic: hub delivery") {
    XjdfServerChecks.hubDelivery()
  }

  test("end to end demo") {
    XjdfServerChecks.endToEndDemo()
  }

  test("subscription stream route") {
    XjdfServerChecks.streamRoute()
  }

  test("stop channel") {
    XjdfServerChecks.stopChannel()
  }

  test("body limit") {
    XjdfServerChecks.bodyLimit()
  }

  test("transport await timeout") {
    XjdfIoChecks.transportTimeout()
  }

  test("await cancellation cleanup") {
    XjdfIoChecks.awaitCancellation()
  }

  test("effectful document builder") {
    XjdfIoChecks.documentInterpreter()
  }

  test("multipart submission round trip") {
    XjdfIoChecks.multipartSubmission()
  }
end HttpChecksSuite
