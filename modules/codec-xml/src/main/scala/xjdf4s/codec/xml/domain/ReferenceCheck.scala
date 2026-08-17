package xjdf4s.codec.xml.domain

import xjdf4s.core.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * Document-wide ID/IDREF integrity pass over the node surface covered by the codec slice. Declared IDs are the
 * `ResourceSet/@ID` and `Resource/@ID` attributes; references are collected from the covered nodes that carry
 * IDREF attributes (`Component/@MediaRef`, `Component/@ContentRefs`, `Glue/@GlueRef`). Duplicate detection is
 * performed by the model (`XJDF.validate`); this pass checks that every reference points at a declared ID.
 */
object ReferenceCheck:

  def validate(document: XJDF): Vector[ValidationError] =
    val declared = declaredIds(document)
    collectReferences(document).flatMap { case (context, reference) =>
      if declared.contains(reference.value) then Vector.empty
      else Vector(ValidationError.InvalidValue(s"IDREF $context", reference.value, "an ID declared in the document"))
    }

  private def declaredIds(document: XJDF): Set[String] =
    val setIds = document.resourceSets.flatMap(_.id.toVector.map(_.value))
    val resourceIds = document.resourceSets.flatMap(_.resources.flatMap(_.id.toVector.map(_.value)))
    (setIds ++ resourceIds).toSet

  private def collectReferences(document: XJDF): Vector[(String, XsdIdRef)] =
    document.resourceSets.toVector.flatMap: resourceSet =>
      resourceSet.resources.toVector.flatMap: resource =>
        resource.specificResource match
          case Some(component: Component) =>
            component.mediaRef.toVector.map(("Component/@MediaRef", _)) ++
              component.contentRefs.map(("Component/@ContentRefs", _))
          case Some(media: Media) =>
            media.mediaLayers.toVector.flatMap(_.layers).flatMap {
              case MediaLayer.GlueLayer(glue) => glue.glueRef.toVector.map(("Glue/@GlueRef", _))
              case _: MediaLayer.MediaLayer   => Vector.empty[(String, XsdIdRef)]
            }
          case _ => Vector.empty[(String, XsdIdRef)]
end ReferenceCheck
