package xjdf4s.model

import cats.{Eq, Hash, Show}
import xjdf4s.core.ValidationError

/** `NamedColor` (Appendix A.2.30): a machine-readable color definition. The closed vocabulary below is the value set
 *  defined by `[Color Names]` and encoded by the XSD pattern restriction of the `NamedColor` simple type; matching is
 *  case-insensitive. Representing it as a closed enum makes invalid color names unrepresentable.
 */
enum NamedColor(val lexical: String) derives CanEqual:
  case AliceBlue extends NamedColor("ALICEBLUE")
  case AntiqueWhite extends NamedColor("ANTIQUEWHITE")
  case Aqua extends NamedColor("AQUA")
  case Aquamarine extends NamedColor("AQUAMARINE")
  case Azure extends NamedColor("AZURE")
  case Beige extends NamedColor("BEIGE")
  case Bisque extends NamedColor("BISQUE")
  case Black extends NamedColor("BLACK")
  case BlanchedAlmond extends NamedColor("BLANCHEDALMOND")
  case Blue extends NamedColor("BLUE")
  case BlueViolet extends NamedColor("BLUEVIOLET")
  case Brown extends NamedColor("BROWN")
  case BurlyWood extends NamedColor("BURLYWOOD")
  case CadetBlue extends NamedColor("CADETBLUE")
  case Chartreuse extends NamedColor("CHARTREUSE")
  case Chocolate extends NamedColor("CHOCOLATE")
  case Coral extends NamedColor("CORAL")
  case CornflowerBlue extends NamedColor("CORNFLOWERBLUE")
  case Cornsilk extends NamedColor("CORNSILK")
  case Crimson extends NamedColor("CRIMSON")
  case Cyan extends NamedColor("CYAN")
  case DarkBlue extends NamedColor("DARKBLUE")
  case DarkCyan extends NamedColor("DARKCYAN")
  case DarkGoldenrod extends NamedColor("DARKGOLDENROD")
  case DarkGray extends NamedColor("DARKGRAY")
  case DarkGreen extends NamedColor("DARKGREEN")
  case DarkGrey extends NamedColor("DARKGREY")
  case DarkKhaki extends NamedColor("DARKKHAKI")
  case DarkMagenta extends NamedColor("DARKMAGENTA")
  case DarkOliveGreen extends NamedColor("DARKOLIVEGREEN")
  case DarkOrange extends NamedColor("DARKORANGE")
  case DarkOrchid extends NamedColor("DARKORCHID")
  case DarkRed extends NamedColor("DARKRED")
  case DarkSalmon extends NamedColor("DARKSALMON")
  case DarkSeaGreen extends NamedColor("DARKSEAGREEN")
  case DarkSlateBlue extends NamedColor("DARKSLATEBLUE")
  case DarkSlateGray extends NamedColor("DARKSLATEGRAY")
  case DarkSlateGrey extends NamedColor("DARKSLATEGREY")
  case DarkTurquoise extends NamedColor("DARKTURQUOISE")
  case DarkViolet extends NamedColor("DARKVIOLET")
  case DeepPink extends NamedColor("DEEPPINK")
  case DeepSkyBlue extends NamedColor("DEEPSKYBLUE")
  case DimGray extends NamedColor("DIMGRAY")
  case DimGrey extends NamedColor("DIMGREY")
  case DodgerBlue extends NamedColor("DODGERBLUE")
  case FireBrick extends NamedColor("FIREBRICK")
  case FloralWhite extends NamedColor("FLORALWHITE")
  case ForestGreen extends NamedColor("FORESTGREEN")
  case Fuchsia extends NamedColor("FUCHSIA")
  case Gainsboro extends NamedColor("GAINSBORO")
  case GhostWhite extends NamedColor("GHOSTWHITE")
  case Gold extends NamedColor("GOLD")
  case Goldenrod extends NamedColor("GOLDENROD")
  case Gray extends NamedColor("GRAY")
  case Green extends NamedColor("GREEN")
  case GreenYellow extends NamedColor("GREENYELLOW")
  case Grey extends NamedColor("GREY")
  case HoneyDew extends NamedColor("HONEYDEW")
  case HotPink extends NamedColor("HOTPINK")
  case IndianRed extends NamedColor("INDIANRED")
  case Indigo extends NamedColor("INDIGO")
  case Ivory extends NamedColor("IVORY")
  case Khaki extends NamedColor("KHAKI")
  case Lavender extends NamedColor("LAVENDER")
  case LavenderBlush extends NamedColor("LAVENDERBLUSH")
  case LawnGreen extends NamedColor("LAWNGREEN")
  case LemonChiffon extends NamedColor("LEMONCHIFFON")
  case LightBlue extends NamedColor("LIGHTBLUE")
  case LightCoral extends NamedColor("LIGHTCORAL")
  case LightCyan extends NamedColor("LIGHTCYAN")
  case LightGoldenrodYellow extends NamedColor("LIGHTGOLDENRODYELLOW")
  case LightGray extends NamedColor("LIGHTGRAY")
  case LightGreen extends NamedColor("LIGHTGREEN")
  case LightGrey extends NamedColor("LIGHTGREY")
  case LightPink extends NamedColor("LIGHTPINK")
  case LightSalmon extends NamedColor("LIGHTSALMON")
  case LightSeaGreen extends NamedColor("LIGHTSEAGREEN")
  case LightSkyBlue extends NamedColor("LIGHTSKYBLUE")
  case LightSlateGray extends NamedColor("LIGHTSLATEGRAY")
  case LightSlateGrey extends NamedColor("LIGHTSLATEGREY")
  case LightSteelBlue extends NamedColor("LIGHTSTEELBLUE")
  case LightYellow extends NamedColor("LIGHTYELLOW")
  case Lime extends NamedColor("LIME")
  case LimeGreen extends NamedColor("LIMEGREEN")
  case Linen extends NamedColor("LINEN")
  case Magenta extends NamedColor("MAGENTA")
  case Maroon extends NamedColor("MAROON")
  case MediumAquamarine extends NamedColor("MEDIUMAQUAMARINE")
  case MediumBlue extends NamedColor("MEDIUMBLUE")
  case MediumOrchid extends NamedColor("MEDIUMORCHID")
  case MediumPurple extends NamedColor("MEDIUMPURPLE")
  case MediumSeaGreen extends NamedColor("MEDIUMSEAGREEN")
  case MediumSlateBlue extends NamedColor("MEDIUMSLATEBLUE")
  case MediumSpringGreen extends NamedColor("MEDIUMSPRINGGREEN")
  case MediumTurquoise extends NamedColor("MEDIUMTURQUOISE")
  case MediumVioletRed extends NamedColor("MEDIUMVIOLETRED")
  case MidnightBlue extends NamedColor("MIDNIGHTBLUE")
  case MintCream extends NamedColor("MINTCREAM")
  case MistyRose extends NamedColor("MISTYROSE")
  case Moccasin extends NamedColor("MOCCASIN")
  case NavajoWhite extends NamedColor("NAVAJOWHITE")
  case Navy extends NamedColor("NAVY")
  case OldLace extends NamedColor("OLDLACE")
  case Olive extends NamedColor("OLIVE")
  case OliveDrab extends NamedColor("OLIVEDRAB")
  case Orange extends NamedColor("ORANGE")
  case OrangeRed extends NamedColor("ORANGERED")
  case Orchid extends NamedColor("ORCHID")
  case PaleGoldenrod extends NamedColor("PALEGOLDENROD")
  case PaleGreen extends NamedColor("PALEGREEN")
  case PaleTurquoise extends NamedColor("PALETURQUOISE")
  case PaleVioletRed extends NamedColor("PALEVIOLETRED")
  case PapayaWhip extends NamedColor("PAPAYAWHIP")
  case PeachPuff extends NamedColor("PEACHPUFF")
  case Peru extends NamedColor("PERU")
  case Pink extends NamedColor("PINK")
  case Plum extends NamedColor("PLUM")
  case PowderBlue extends NamedColor("POWDERBLUE")
  case Purple extends NamedColor("PURPLE")
  case Red extends NamedColor("RED")
  case RosyBrown extends NamedColor("ROSYBROWN")
  case RoyalBlue extends NamedColor("ROYALBLUE")
  case SaddleBrown extends NamedColor("SADDLEBROWN")
  case Salmon extends NamedColor("SALMON")
  case SandyBrown extends NamedColor("SANDYBROWN")
  case SeaGreen extends NamedColor("SEAGREEN")
  case SeaShell extends NamedColor("SEASHELL")
  case Sienna extends NamedColor("SIENNA")
  case Silver extends NamedColor("SILVER")
  case SkyBlue extends NamedColor("SKYBLUE")
  case SlateBlue extends NamedColor("SLATEBLUE")
  case SlateGray extends NamedColor("SLATEGRAY")
  case SlateGrey extends NamedColor("SLATEGREY")
  case Snow extends NamedColor("SNOW")
  case SpringGreen extends NamedColor("SPRINGGREEN")
  case SteelBlue extends NamedColor("STEELBLUE")
  case Tan extends NamedColor("TAN")
  case Teal extends NamedColor("TEAL")
  case Thistle extends NamedColor("THISTLE")
  case Tomato extends NamedColor("TOMATO")
  case Turquoise extends NamedColor("TURQUOISE")
  case Violet extends NamedColor("VIOLET")
  case Wheat extends NamedColor("WHEAT")
  case White extends NamedColor("WHITE")
  case WhiteSmoke extends NamedColor("WHITESMOKE")
  case Yellow extends NamedColor("YELLOW")
  case YellowGreen extends NamedColor("YELLOWGREEN")

object NamedColor:
  private val byLexical: Map[String, NamedColor] = values.map(value => value.lexical -> value).toMap

  def from(value: String): Either[ValidationError, NamedColor] =
    byLexical
      .get(value.toUpperCase)
      .toRight(ValidationError.InvalidValue("NamedColor", value, "a color name from the [Color Names] vocabulary"))
end NamedColor

/** CIE Lab color (Appendix A.1): three values in the sequence `L a b`. `L` is restricted to `[0..100]`; `a` and `b`
 *  are unbounded. Values are normalized to D50 illumination at a 2-degree observer angle.
 */
opaque type LabColor = (Double, Double, Double)
object LabColor:
  def from(lightness: Double, a: Double, b: Double): Either[ValidationError, LabColor] =
    Either.cond(
      lightness >= 0.0 && lightness <= 100.0,
      (lightness, a, b),
      ValidationError.ValueOutOfBounds("LabColor/@L", lightness.toString, "[0..100]"),
    )

  extension (color: LabColor)
    def lightness: Double = color._1
    def a: Double = color._2
    def b: Double = color._3

  given Eq[LabColor] = Eq.instance: (left, right) =>
    left.lightness == right.lightness && left.a == right.a && left.b == right.b
  given Show[LabColor] = Show.show(color => s"${color.lightness} ${color.a} ${color.b}")
  given Hash[LabColor] = Hash.fromUniversalHashCode
end LabColor

/** CMYK color (Appendix A.1): four values in the sequence `C M Y K`, each in the range `[0..1.0]`, where `0.0`
 *  specifies no ink and `1.0` specifies full ink.
 */
opaque type CmykColor = (Double, Double, Double, Double)
object CmykColor:
  private def inRange(name: String, component: Double): Either[ValidationError, Unit] =
    Either.cond(
      component >= 0.0 && component <= 1.0,
      (),
      ValidationError.ValueOutOfBounds(s"CMYKColor/@$name", component.toString, "[0..1.0]"),
    )

  def from(cyan: Double, magenta: Double, yellow: Double, black: Double): Either[ValidationError, CmykColor] =
    for
      _ <- inRange("C", cyan)
      _ <- inRange("M", magenta)
      _ <- inRange("Y", yellow)
      _ <- inRange("K", black)
    yield (cyan, magenta, yellow, black)

  extension (color: CmykColor)
    def cyan: Double = color._1
    def magenta: Double = color._2
    def yellow: Double = color._3
    def black: Double = color._4

  given Eq[CmykColor] = Eq.instance: (left, right) =>
    left.cyan == right.cyan && left.magenta == right.magenta &&
      left.yellow == right.yellow && left.black == right.black
  given Show[CmykColor] = Show.show(color => s"${color.cyan} ${color.magenta} ${color.yellow} ${color.black}")
  given Hash[CmykColor] = Hash.fromUniversalHashCode
end CmykColor

/** sRGB color: three components in the sequence `R G B`, each in the valid sRGB range `[0..1.0]`. The XSD restricts
 *  only the list length; the component range is the normative sRGB value space and is enforced here as a refinement.
 */
opaque type SrgbColor = (Double, Double, Double)
object SrgbColor:
  private def inRange(name: String, component: Double): Either[ValidationError, Unit] =
    Either.cond(
      component >= 0.0 && component <= 1.0,
      (),
      ValidationError.ValueOutOfBounds(s"sRGBColor/@$name", component.toString, "[0..1.0]"),
    )

  def from(red: Double, green: Double, blue: Double): Either[ValidationError, SrgbColor] =
    for
      _ <- inRange("R", red)
      _ <- inRange("G", green)
      _ <- inRange("B", blue)
    yield (red, green, blue)

  extension (color: SrgbColor)
    def red: Double = color._1
    def green: Double = color._2
    def blue: Double = color._3

  given Eq[SrgbColor] = Eq.instance: (left, right) =>
    left.red == right.red && left.green == right.green && left.blue == right.blue
  given Show[SrgbColor] = Show.show(color => s"${color.red} ${color.green} ${color.blue}")
  given Hash[SrgbColor] = Hash.fromUniversalHashCode
end SrgbColor
