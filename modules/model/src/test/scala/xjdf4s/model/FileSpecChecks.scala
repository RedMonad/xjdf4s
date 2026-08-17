package xjdf4s.model

import xjdf4s.core.*

object FileSpecChecks:
  private val url = UriRef.from("https://example.org/input.pdf").toOption.get
  private val template = Nmtoken.from("1~10").toOption.get

  val exclusiveLocations: Unit =
    val remote = FileSpec(location = FileLocation.Url(url))
    val format = XjdfString.from("page_%02d.pdf").toOption.get
    val sequence = FileSpec(location = FileLocation.Sequence(format, NonEmptyVector.one(template)))
    val pipe = FileSpec()
    assert(remote.location.isInstanceOf[FileLocation.Url])
    assert(sequence.location.isInstanceOf[FileLocation.Sequence])
    assert(pipe.location == FileLocation.Pipe)

  val typedDisposition: Unit =
    val disposition = Disposition(action = Some(DispositionAction.Archive))
    val headerName = XjdfString.from("Authorization").toOption.get
    val headerValue = XjdfString.from("Bearer").toOption.get
    val file = FileSpec(
      disposition = Some(disposition),
      networkHeaders = Vector(NetworkHeader(headerName, headerValue)),
    )
    assert(file.networkHeaders.nonEmpty)
end FileSpecChecks
