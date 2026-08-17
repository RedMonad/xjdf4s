package xjdf4s.messaging

import xjdf4s.core.*
import xjdf4s.model.Header

object ControlMessageChecks:
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val jobId = Nmtoken.from("job-1").toOption.get
  private val pipeId = Nmtoken.from("pipe-1").toOption.get
  private val time = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
  private val header = Header(deviceId, time)

  val pipeCommand: Unit =
    val message: Command = CommandPipeControl(header, PipeParams(jobId, PipeOperation.Pull, pipeId))
    assert(message.elementName.localName == "CommandPipeControl")

  val shutdownWithoutParams: Unit =
    val message: Command = CommandShutDown(header)
    assert(message.elementName.localName == "CommandShutDown")

  val wakeUp: Unit =
    val message: Response = ResponseWakeUp(header, returnCode = Some(0))
    assert(message.elementName.localName == "ResponseWakeUp")
end ControlMessageChecks
