package xjdf4s
package resources

import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** The `RunList` resource (Table 6.148): a PDL file (or a set of files) together
 *  with the description of how its pages are selected.
 */
final case class RunList(
    automation: Option[Automation] = None,
    contentRefs: Option[IdRefs] = None,
    docPages: Option[Long] = None,
    docs: Option[Long] = None,
    endOfDocument: Option[Boolean] = None,
    endOfSet: Option[Boolean] = None,
    finishedPages: Option[Long] = None,
    logicalPage: Option[Long] = None,
    nPage: Option[Long] = None,
    ordType: Option[NmToken] = None,
    pages: Option[IntegerRange] = None,
    sets: Option[Long] = None,
    sourceMediaBox: Option[Rectangle] = None,
    sourceTrimBox: Option[Rectangle] = None,
    sourceBleedBox: Option[Rectangle] = None,
    sourceClipBox: Option[Rectangle] = None,
    byteMap: Option[ByteMap] = None,
    fileSpecs: Chain[FileSpec] = Chain.empty
):

  def references: Chain[IdRef] =
    Chain.fromSeq(contentRefs.toList.flatMap(_.toList))
end RunList

object RunList:

  given Eq[RunList] = Eq.fromUniversalEquals

end RunList

/** The `ByteMap` subelement of a RunList (Table 6.148, ImageCompression area):
 *  an uncompressed or lossless-compressed raster image.
 */
final case class ByteMap(
    height: Long,
    width: Long,
    bandOrdering: Option[NmToken] = None,
    frameHeight: Option[Long] = None
)

object ByteMap:
  given Eq[ByteMap] = Eq.fromUniversalEquals
