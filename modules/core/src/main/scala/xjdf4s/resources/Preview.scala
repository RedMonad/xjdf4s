package xjdf4s
package resources

import xjdf4s.prim.*
import cats.data.Chain
import cats.kernel.Eq

/** The `Preview` resource (Table 6.66): a preview associated with a process and
 *  used for display purposes. `Part/@PreviewType` SHOULD be `ThumbNail` or
 *  `Viewable`.
 */
final case class Preview(
    compensation: Option[Double] = None,
    previewFileType: Option[NmToken] = None,
    fileSpecs: Chain[FileSpec] = Chain.empty
)

object Preview:

  given Eq[Preview] = Eq.fromUniversalEquals

end Preview
