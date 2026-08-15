package xjdf4s
package resources

import xjdf4s.prim.*
import cats.kernel.Eq

/** The `Color` resource (Table 6.27): the colorant information of one
 *  separation. Color resources are partitioned by `Part/@Separation`.
 */
final case class Color(
    actualColorName: Option[XjdfString] = None,
    cmyk: Option[CMYKColor] = None,
    colorBook: Option[XjdfString] = None,
    colorBookEntry: Option[XjdfString] = None,
    colorDetails: Option[XjdfString] = None,
    colorName: Option[XjdfString] = None,
    colorType: Option[ColorType] = None,
    colorTypeDetails: Option[XjdfString] = None,
    density: Option[Double] = None,
    gray: Option[Double] = None,
    lab: Option[LabColor] = None,
    neutralDensity: Option[Double] = None,
    printingTechnology: Option[NmToken] = None,
    rawName: Option[XjdfString] = None,
    sRgb: Option[RGBColor] = None
)

object Color:

  /** The standard process colors. */
  def cyan: Color = Color(cmyk = Some(CMYKColor.unsafe(1, 0, 0, 0)), colorName = XjdfString.from("Cyan"))
  def magenta: Color = Color(cmyk = Some(CMYKColor.unsafe(0, 1, 0, 0)), colorName = XjdfString.from("Magenta"))
  def yellow: Color = Color(cmyk = Some(CMYKColor.unsafe(0, 0, 1, 0)), colorName = XjdfString.from("Yellow"))
  def black: Color = Color(cmyk = Some(CMYKColor.unsafe(0, 0, 0, 1)), colorName = XjdfString.from("Black"))

  given Eq[Color] = Eq.fromUniversalEquals

end Color
