package xjdf4s.model

import xjdf4s.core.*

object FileSpecChecks:
  private val url = UriRef.from("https://example.org/input.pdf").toOption.get
  private val template = Nmtoken.from("1~10").toOption.get

  val exclusiveLocations: Unit =
    val remote = FileSpec(location = FileLocation.Url(url))
    val sequence = FileSpec(location = FileLocation.Sequence("page_%02d.pdf", NonEmptyVector.one(template)))
    val pipe = FileSpec()
    assert(remote.location.isInstanceOf[FileLocation.Url])
    assert(sequence.location.isInstanceOf[FileLocation.Sequence])
    assert(pipe.location == FileLocation.Pipe)

  val typedDisposition: Unit =
    val disposition = Disposition(action = Some(DispositionAction.Archive))
    val file = FileSpec(disposition = Some(disposition), networkHeaders = Vector(NetworkHeader("Authorization", "Bearer")))
    assert(file.networkHeaders.nonEmpty)
end FileSpecChecks
