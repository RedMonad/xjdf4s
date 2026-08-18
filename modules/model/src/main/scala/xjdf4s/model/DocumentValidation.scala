package xjdf4s.model

import cats.data.{Validated, ValidatedNel}
import cats.implicits.*
import xjdf4s.core.*

/** Compositional document validation: an accumulating traversal over the node tree. Type-level invariants keep most
 *  invalid states unrepresentable; this pass collects every cross-field violation of a document at once, with the
 *  document path attached to each error (`ValidationError.AtPath`).
 */
extension (resource: Resource)
  /** Validates the resource itself, its `Part` elements and its specific resource, accumulating all errors. `path`
   *  is the location of this resource inside the document (empty for the root of the traversal).
   */
  def validateAllAt(path: Vector[String]): ValidatedNel[ValidationError, Unit] =
    def atPath(error: ValidationError): ValidationError =
      if path.isEmpty then error else ValidationError.AtPath(path, error)

    val ownErrors = resource.validate.map(atPath).toValidatedNel
    val partErrors = resource.parts.traverse_(_.validateAt(path :+ "Part"))
    val specificErrors = resource.specificResource match
      case Some(node: ValidatedNode) => node.validateAt(path :+ "SpecificResource")
      case _ => Validated.validNel[ValidationError, Unit](())
    ownErrors *> partErrors *> specificErrors
  end validateAllAt

  def validateAll: ValidatedNel[ValidationError, Unit] =
    validateAllAt(Vector.empty)
end extension

extension (document: XJDF)
  /** Validates the whole document tree, accumulating errors with paths to the offending nodes. */
  def validateAll: ValidatedNel[ValidationError, Unit] =
    document.validateNel *> document.resourceSets.zipWithIndex.traverse_ { case (resourceSet, setIndex) =>
      resourceSet.resources.zipWithIndex.traverse_ { case (resource, resourceIndex) =>
        resource.validateAllAt(Vector(s"ResourceSet[$setIndex]", s"Resource[$resourceIndex]"))
      }
    }

private val legacyVinylMediaType: Option[MediaType] =
  // The Vinyl enum case is @deprecated: detect it by lexical match to avoid referencing the deprecated symbol.
  MediaType.values.find(_.toString == "Vinyl")

/** Deprecation notes for nodes that remain representable for backward compatibility. */
def resourceWarnings(resource: Resource): Vector[Warning] =
  resource.specificResource match
    case Some(media: resources.Media) if legacyVinylMediaType.contains(media.mediaType) =>
      Vector(
        Warning(
          "media-type-deprecated",
          "MediaType 'Vinyl' is deprecated in XJDF 2.1; use MediaType.Synthetic with @MediaTypeDetails='Vinyl'",
        ),
      )
    case Some(box: resources.BoxFoldingParams) if box.legacyGlues.nonEmpty =>
      Vector(
        Warning(
          "box-folding-legacy-glue",
          "top-level Glue in BoxFoldingParams is deprecated in XJDF 2.2; use BoxFoldAction/@Action='Glue' with a child Glue",
        ),
      )
    case _ => Vector.empty

/** Two-channel validation of a single resource: blocking errors plus advisory deprecation warnings. */
def validateWithWarnings(resource: Resource): ValidationOutcome =
  ValidationOutcome(resource.validate, resourceWarnings(resource))

/** Whole-document two-channel validation. */
def validateDocumentWithWarnings(document: XJDF): ValidationOutcome =
  val errors = document.validateAll.toEither match
    case Left(nel) => nel.toList.toVector
    case Right(_) => Vector.empty
  val warnings = document.resourceSets.flatMap(_.resources).flatMap(resourceWarnings)
  ValidationOutcome(errors, warnings)
