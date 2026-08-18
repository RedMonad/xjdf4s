package xjdf4s.model

import cats.Show
import xjdf4s.core.*
import xjdf4s.model.resources.*

/** Stage 02 checks: accumulating document validation, error paths and deprecation warnings. */
object ValidationDslChecks:
  private val jobId = Nmtoken.from("job-1").toOption.get
  private val process = Nmtoken.from("Product").toOption.get
  private val mediaName = Nmtoken.from("Media").toOption.get

  private def invalidResource: Resource =
    Resource(orientation = Some(Orientation.Rotate90), transformation = Some(Matrix.identity))

  val accumulatedDocumentValidation: Unit =
    val document = XJDF(
      jobId,
      NonEmptyVector.one(process),
      resourceSets = Vector(
        ResourceSet(mediaName, resources = Vector(invalidResource, invalidResource)),
        ResourceSet(mediaName, resources = Vector(invalidResource)),
      ),
    )
    val errors = document.validateAll.toEither.left.toOption.map(_.toList).getOrElse(Nil)
    assert(errors.size == 3)
    assert(errors.forall(error => Show[ValidationError].show(error).contains("Resource[")))

  val pathThreading: Unit =
    val errors = invalidResource
      .validateAllAt(Vector("ResourceSet[0]", "Resource[1]"))
      .toEither
      .left
      .toOption
      .map(_.toList)
      .getOrElse(Nil)
    assert(errors.size == 1)
    assert(Show[ValidationError].show(errors.head).contains("ResourceSet[0]"))
    assert(Show[ValidationError].show(errors.head).contains("Resource[1]"))

  val deprecationWarnings: Unit =
    val legacyVinyl = MediaType.values.find(_.toString == "Vinyl").get
    val vinyl = resourceWarnings(Resource(specificResource = Some(Media(legacyVinyl))))
    assert(vinyl.exists(_.code == "media-type-deprecated"))
    val box = resourceWarnings(
      Resource(specificResource = Some(BoxFoldingParams(BoxFoldingType.Type00, legacyGlues = Vector(Glue())))),
    )
    assert(box.exists(_.code == "box-folding-legacy-glue"))
    assert(resourceWarnings(Resource(specificResource = Some(Media(MediaType.Paper)))).isEmpty)

  val twoChannelOutcome: Unit =
    val clean = validateWithWarnings(Resource(specificResource = Some(Media(MediaType.Paper))))
    assert(clean.isValid)
    assert(clean.warnings.isEmpty)
    assert(!validateWithWarnings(invalidResource).isValid)
    val document =
      XJDF(
        jobId,
        NonEmptyVector.one(process),
        resourceSets = Vector(ResourceSet(mediaName, resources = Vector(invalidResource)))
      )
    val outcome = validateDocumentWithWarnings(document)
    assert(!outcome.isValid)
    assert(outcome.errors.size == 1)
end ValidationDslChecks
