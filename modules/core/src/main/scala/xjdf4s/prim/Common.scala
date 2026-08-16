package xjdf4s
package prim

import cats.Show
import cats.kernel.Eq

/** XJDF data type `URL` (Table A.1), as defined by [RFC3986]. */
opaque type Url = String

object Url:

  def from(raw: String): Option[Url] =
    Option(raw).filter: r =>
      try
        java.net.URI.create(r)
        true
      catch case _: IllegalArgumentException => false

  def unsafe(raw: String): Url =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid URL: '$raw'"))

  extension (url: Url)
    def value: String = url
    def isAbsolute: Boolean = java.net.URI.create(url).isAbsolute

  given Show[Url] = Show.show(identity)

  given Eq[Url] = Eq.fromUniversalEquals

end Url

/** Recommended NMTOKEN values from Appendix A.3 — open catalogs. These are
 *  suggestions, not a closed enumeration: any NMTOKEN value is legal
 *  (§1.1.1, §1.10.3.2).
 */
object Catalog:

  /** Node Categories (Table A.3.14), used by `XJDF/@Category`. */
  object NodeCategory:
    val Binding: NmToken = NmToken.unsafe("Binding")
    val Cutting: NmToken = NmToken.unsafe("Cutting")
    val DigitalPrinting: NmToken = NmToken.unsafe("DigitalPrinting")
    val FinalImaging: NmToken = NmToken.unsafe("FinalImaging")
    val FinalRIPing: NmToken = NmToken.unsafe("FinalRIPing")
    val Folding: NmToken = NmToken.unsafe("Folding")
    val Newsprinting: NmToken = NmToken.unsafe("Newsprinting")
    val PostPress: NmToken = NmToken.unsafe("PostPress")
    val PrePress: NmToken = NmToken.unsafe("PrePress")
    val Printing: NmToken = NmToken.unsafe("Printing")
    val ProofImaging: NmToken = NmToken.unsafe("ProofImaging")
    val ProofRIPing: NmToken = NmToken.unsafe("ProofRIPing")
    val RIPing: NmToken = NmToken.unsafe("RIPing")
    val WebPrinting: NmToken = NmToken.unsafe("WebPrinting")
    val WebToPrint: NmToken = NmToken.unsafe("WebToPrint")

  /** Product Types (Table A.3.18). */
  object ProductType:
    val BackCover: NmToken = NmToken.unsafe("BackCover")
    val BlankBox: NmToken = NmToken.unsafe("BlankBox")
    val BlankSheet: NmToken = NmToken.unsafe("BlankSheet")
    val BlankWeb: NmToken = NmToken.unsafe("BlankWeb")
    val Body: NmToken = NmToken.unsafe("Body")
    val Book: NmToken = NmToken.unsafe("Book")
    val BookBlock: NmToken = NmToken.unsafe("BookBlock")
    val BookCase: NmToken = NmToken.unsafe("BookCase")
    val Booklet: NmToken = NmToken.unsafe("Booklet")
    val Box: NmToken = NmToken.unsafe("Box")
    val Brochure: NmToken = NmToken.unsafe("Brochure")
    val BusinessCard: NmToken = NmToken.unsafe("BusinessCard")
    val Carton: NmToken = NmToken.unsafe("Carton")
    val Cover: NmToken = NmToken.unsafe("Cover")
    val CoverBoard: NmToken = NmToken.unsafe("CoverBoard")
    val CoverLetter: NmToken = NmToken.unsafe("CoverLetter")
    val EndSheet: NmToken = NmToken.unsafe("EndSheet")
    val Envelope: NmToken = NmToken.unsafe("Envelope")
    val FlatBox: NmToken = NmToken.unsafe("FlatBox")
    val FlatWork: NmToken = NmToken.unsafe("FlatWork")
    val FrontCover: NmToken = NmToken.unsafe("FrontCover")
    val HardCoverBook: NmToken = NmToken.unsafe("HardCoverBook")
    val Insert: NmToken = NmToken.unsafe("Insert")
    val Jacket: NmToken = NmToken.unsafe("Jacket")
    val Label: NmToken = NmToken.unsafe("Label")
    val Leaflet: NmToken = NmToken.unsafe("Leaflet")
    val Letter: NmToken = NmToken.unsafe("Letter")
    val Map: NmToken = NmToken.unsafe("Map")
    val Media: NmToken = NmToken.unsafe("Media")
    val Newspaper: NmToken = NmToken.unsafe("Newspaper")
    val Notebook: NmToken = NmToken.unsafe("Notebook")
    val Pallet: NmToken = NmToken.unsafe("Pallet")
    val Postcard: NmToken = NmToken.unsafe("Postcard")
    val Poster: NmToken = NmToken.unsafe("Poster")
    val Preprinted: NmToken = NmToken.unsafe("Preprinted")
    val Proof: NmToken = NmToken.unsafe("Proof")
    val ResponseCard: NmToken = NmToken.unsafe("ResponseCard")
    val Section: NmToken = NmToken.unsafe("Section")
    val SelfMailer: NmToken = NmToken.unsafe("SelfMailer")
    val SoftCoverBook: NmToken = NmToken.unsafe("SoftCoverBook")
    val Spine: NmToken = NmToken.unsafe("Spine")
    val SpineBoard: NmToken = NmToken.unsafe("SpineBoard")
    val Stack: NmToken = NmToken.unsafe("Stack")
    val WrapAroundCover: NmToken = NmToken.unsafe("WrapAroundCover")

  /** Printing Technologies (Table A.3.16). */
  object PrintingTechnology:
    val DyeSublimation: NmToken = NmToken.unsafe("DyeSublimation")
    val ElectroInk: NmToken = NmToken.unsafe("ElectroInk")
    val Electrophotography: NmToken = NmToken.unsafe("Electrophotography")
    val Flexography: NmToken = NmToken.unsafe("Flexography")
    val InkJet: NmToken = NmToken.unsafe("InkJet")
    val Letterpress: NmToken = NmToken.unsafe("Letterpress")
    val OffsetLithography: NmToken = NmToken.unsafe("OffsetLithography")
    val Potato: NmToken = NmToken.unsafe("Potato")
    val Rotogravure: NmToken = NmToken.unsafe("Rotogravure")
    val ScreenPrinting: NmToken = NmToken.unsafe("ScreenPrinting")
    val Thermal: NmToken = NmToken.unsafe("Thermal")

  /** Contact Types (Table A.3.2). */
  object ContactType:
    val Accounting: NmToken = NmToken.unsafe("Accounting")
    val Administrator: NmToken = NmToken.unsafe("Administrator")
    val Agency: NmToken = NmToken.unsafe("Agency")
    val Approver: NmToken = NmToken.unsafe("Approver")
    val ArtDelivery: NmToken = NmToken.unsafe("ArtDelivery")
    val ArtReturn: NmToken = NmToken.unsafe("ArtReturn")
    val Author: NmToken = NmToken.unsafe("Author")
    val Customer: NmToken = NmToken.unsafe("Customer")
    val Delivery: NmToken = NmToken.unsafe("Delivery")
    val DeliveryCharge: NmToken = NmToken.unsafe("DeliveryCharge")
    val Designer: NmToken = NmToken.unsafe("Designer")
    val Editor: NmToken = NmToken.unsafe("Editor")
    val Employee: NmToken = NmToken.unsafe("Employee")
    val Illustrator: NmToken = NmToken.unsafe("Illustrator")
    val Owner: NmToken = NmToken.unsafe("Owner")
    val Photographer: NmToken = NmToken.unsafe("Photographer")
    val Recipient: NmToken = NmToken.unsafe("Recipient")
    val Sender: NmToken = NmToken.unsafe("Sender")
    val SenderAlias: NmToken = NmToken.unsafe("SenderAlias")
    val TelephoneSanitizer: NmToken = NmToken.unsafe("TelephoneSanitizer")

  /** Flute Types (Table A.3.7). */
  object FluteType:
    val A: NmToken = NmToken.unsafe("A")
    val B: NmToken = NmToken.unsafe("B")
    val C: NmToken = NmToken.unsafe("C")
    val E: NmToken = NmToken.unsafe("E")
    val F: NmToken = NmToken.unsafe("F")

  /** Product fold catalog (Table 4.28). */
  object FoldCatalog:
    val F2_1: NmToken = NmToken.unsafe("F2-1")
    val F4_1: NmToken = NmToken.unsafe("F4-1")
    val F6_1: NmToken = NmToken.unsafe("F6-1")
    val F6_3: NmToken = NmToken.unsafe("F6-3")
    val F6_4: NmToken = NmToken.unsafe("F6-4")
    val F6_7: NmToken = NmToken.unsafe("F6-7")
    val F8_2: NmToken = NmToken.unsafe("F8-2")
    val F8_4: NmToken = NmToken.unsafe("F8-4")
    val F8_5: NmToken = NmToken.unsafe("F8-5")

  /** `NamedColor` (§A.2.30): a machine-readable definition of a color.
   *
   *  §A.2.30 defines no value table of its own — it delegates to the external
   *  catalog `[Color Names]` (SVG 1.1 Second Edition, Appendix G), which is why
   *  this is an open `NmToken` catalog and not a closed `enum` (ADR-0007).
   *  The 147 recommended values below are the SVG color keywords, and match the
   *  147 patterns of `<xs:simpleType name="NamedColor">` in `schema.xsd`
   *  one-for-one. Values outside this list are legal (§1.10.3.2).
   *
   *  Wire tokens are matched case-insensitively by `schema.xsd`; the spelling
   *  here is the canonical mixed case of `[Color Names]`.
   */
  object NamedColor:
    val AliceBlue: NmToken = NmToken.unsafe("AliceBlue")
    val AntiqueWhite: NmToken = NmToken.unsafe("AntiqueWhite")
    val Aqua: NmToken = NmToken.unsafe("Aqua")
    val Aquamarine: NmToken = NmToken.unsafe("Aquamarine")
    val Azure: NmToken = NmToken.unsafe("Azure")
    val Beige: NmToken = NmToken.unsafe("Beige")
    val Bisque: NmToken = NmToken.unsafe("Bisque")
    val Black: NmToken = NmToken.unsafe("Black")
    val BlanchedAlmond: NmToken = NmToken.unsafe("BlanchedAlmond")
    val Blue: NmToken = NmToken.unsafe("Blue")
    val BlueViolet: NmToken = NmToken.unsafe("BlueViolet")
    val Brown: NmToken = NmToken.unsafe("Brown")
    val BurlyWood: NmToken = NmToken.unsafe("BurlyWood")
    val CadetBlue: NmToken = NmToken.unsafe("CadetBlue")
    val Chartreuse: NmToken = NmToken.unsafe("Chartreuse")
    val Chocolate: NmToken = NmToken.unsafe("Chocolate")
    val Coral: NmToken = NmToken.unsafe("Coral")
    val CornflowerBlue: NmToken = NmToken.unsafe("CornflowerBlue")
    val Cornsilk: NmToken = NmToken.unsafe("Cornsilk")
    val Crimson: NmToken = NmToken.unsafe("Crimson")
    val Cyan: NmToken = NmToken.unsafe("Cyan")
    val DarkBlue: NmToken = NmToken.unsafe("DarkBlue")
    val DarkCyan: NmToken = NmToken.unsafe("DarkCyan")
    val DarkGoldenrod: NmToken = NmToken.unsafe("DarkGoldenrod")
    val DarkGray: NmToken = NmToken.unsafe("DarkGray")
    val DarkGreen: NmToken = NmToken.unsafe("DarkGreen")
    val DarkGrey: NmToken = NmToken.unsafe("DarkGrey")
    val DarkKhaki: NmToken = NmToken.unsafe("DarkKhaki")
    val DarkMagenta: NmToken = NmToken.unsafe("DarkMagenta")
    val DarkOliveGreen: NmToken = NmToken.unsafe("DarkOliveGreen")
    val DarkOrange: NmToken = NmToken.unsafe("DarkOrange")
    val DarkOrchid: NmToken = NmToken.unsafe("DarkOrchid")
    val DarkRed: NmToken = NmToken.unsafe("DarkRed")
    val DarkSalmon: NmToken = NmToken.unsafe("DarkSalmon")
    val DarkSeaGreen: NmToken = NmToken.unsafe("DarkSeaGreen")
    val DarkSlateBlue: NmToken = NmToken.unsafe("DarkSlateBlue")
    val DarkSlateGray: NmToken = NmToken.unsafe("DarkSlateGray")
    val DarkSlateGrey: NmToken = NmToken.unsafe("DarkSlateGrey")
    val DarkTurquoise: NmToken = NmToken.unsafe("DarkTurquoise")
    val DarkViolet: NmToken = NmToken.unsafe("DarkViolet")
    val DeepPink: NmToken = NmToken.unsafe("DeepPink")
    val DeepSkyBlue: NmToken = NmToken.unsafe("DeepSkyBlue")
    val DimGray: NmToken = NmToken.unsafe("DimGray")
    val DimGrey: NmToken = NmToken.unsafe("DimGrey")
    val DodgerBlue: NmToken = NmToken.unsafe("DodgerBlue")
    val FireBrick: NmToken = NmToken.unsafe("FireBrick")
    val FloralWhite: NmToken = NmToken.unsafe("FloralWhite")
    val ForestGreen: NmToken = NmToken.unsafe("ForestGreen")
    val Fuchsia: NmToken = NmToken.unsafe("Fuchsia")
    val Gainsboro: NmToken = NmToken.unsafe("Gainsboro")
    val GhostWhite: NmToken = NmToken.unsafe("GhostWhite")
    val Gold: NmToken = NmToken.unsafe("Gold")
    val Goldenrod: NmToken = NmToken.unsafe("Goldenrod")
    val Gray: NmToken = NmToken.unsafe("Gray")
    val Grey: NmToken = NmToken.unsafe("Grey")
    val Green: NmToken = NmToken.unsafe("Green")
    val GreenYellow: NmToken = NmToken.unsafe("GreenYellow")
    val Honeydew: NmToken = NmToken.unsafe("Honeydew")
    val HotPink: NmToken = NmToken.unsafe("HotPink")
    val IndianRed: NmToken = NmToken.unsafe("IndianRed")
    val Indigo: NmToken = NmToken.unsafe("Indigo")
    val Ivory: NmToken = NmToken.unsafe("Ivory")
    val Khaki: NmToken = NmToken.unsafe("Khaki")
    val Lavender: NmToken = NmToken.unsafe("Lavender")
    val LavenderBlush: NmToken = NmToken.unsafe("LavenderBlush")
    val LawnGreen: NmToken = NmToken.unsafe("LawnGreen")
    val LemonChiffon: NmToken = NmToken.unsafe("LemonChiffon")
    val LightBlue: NmToken = NmToken.unsafe("LightBlue")
    val LightCoral: NmToken = NmToken.unsafe("LightCoral")
    val LightCyan: NmToken = NmToken.unsafe("LightCyan")
    val LightGoldenrodYellow: NmToken = NmToken.unsafe("LightGoldenrodYellow")
    val LightGray: NmToken = NmToken.unsafe("LightGray")
    val LightGreen: NmToken = NmToken.unsafe("LightGreen")
    val LightGrey: NmToken = NmToken.unsafe("LightGrey")
    val LightPink: NmToken = NmToken.unsafe("LightPink")
    val LightSalmon: NmToken = NmToken.unsafe("LightSalmon")
    val LightSeaGreen: NmToken = NmToken.unsafe("LightSeaGreen")
    val LightSkyBlue: NmToken = NmToken.unsafe("LightSkyBlue")
    val LightSlateGray: NmToken = NmToken.unsafe("LightSlateGray")
    val LightSlateGrey: NmToken = NmToken.unsafe("LightSlateGrey")
    val LightSteelBlue: NmToken = NmToken.unsafe("LightSteelBlue")
    val LightYellow: NmToken = NmToken.unsafe("LightYellow")
    val Lime: NmToken = NmToken.unsafe("Lime")
    val LimeGreen: NmToken = NmToken.unsafe("LimeGreen")
    val Linen: NmToken = NmToken.unsafe("Linen")
    val Magenta: NmToken = NmToken.unsafe("Magenta")
    val Maroon: NmToken = NmToken.unsafe("Maroon")
    val MediumAquamarine: NmToken = NmToken.unsafe("MediumAquamarine")
    val MediumBlue: NmToken = NmToken.unsafe("MediumBlue")
    val MediumOrchid: NmToken = NmToken.unsafe("MediumOrchid")
    val MediumPurple: NmToken = NmToken.unsafe("MediumPurple")
    val MediumSeaGreen: NmToken = NmToken.unsafe("MediumSeaGreen")
    val MediumSlateBlue: NmToken = NmToken.unsafe("MediumSlateBlue")
    val MediumSpringGreen: NmToken = NmToken.unsafe("MediumSpringGreen")
    val MediumTurquoise: NmToken = NmToken.unsafe("MediumTurquoise")
    val MediumVioletRed: NmToken = NmToken.unsafe("MediumVioletRed")
    val MidnightBlue: NmToken = NmToken.unsafe("MidnightBlue")
    val MintCream: NmToken = NmToken.unsafe("MintCream")
    val MistyRose: NmToken = NmToken.unsafe("MistyRose")
    val Moccasin: NmToken = NmToken.unsafe("Moccasin")
    val NavajoWhite: NmToken = NmToken.unsafe("NavajoWhite")
    val Navy: NmToken = NmToken.unsafe("Navy")
    val OldLace: NmToken = NmToken.unsafe("OldLace")
    val Olive: NmToken = NmToken.unsafe("Olive")
    val OliveDrab: NmToken = NmToken.unsafe("OliveDrab")
    val Orange: NmToken = NmToken.unsafe("Orange")
    val OrangeRed: NmToken = NmToken.unsafe("OrangeRed")
    val Orchid: NmToken = NmToken.unsafe("Orchid")
    val PaleGoldenrod: NmToken = NmToken.unsafe("PaleGoldenrod")
    val PaleGreen: NmToken = NmToken.unsafe("PaleGreen")
    val PaleTurquoise: NmToken = NmToken.unsafe("PaleTurquoise")
    val PaleVioletRed: NmToken = NmToken.unsafe("PaleVioletRed")
    val PapayaWhip: NmToken = NmToken.unsafe("PapayaWhip")
    val PeachPuff: NmToken = NmToken.unsafe("PeachPuff")
    val Peru: NmToken = NmToken.unsafe("Peru")
    val Pink: NmToken = NmToken.unsafe("Pink")
    val Plum: NmToken = NmToken.unsafe("Plum")
    val PowderBlue: NmToken = NmToken.unsafe("PowderBlue")
    val Purple: NmToken = NmToken.unsafe("Purple")
    val Red: NmToken = NmToken.unsafe("Red")
    val RosyBrown: NmToken = NmToken.unsafe("RosyBrown")
    val RoyalBlue: NmToken = NmToken.unsafe("RoyalBlue")
    val SaddleBrown: NmToken = NmToken.unsafe("SaddleBrown")
    val Salmon: NmToken = NmToken.unsafe("Salmon")
    val SandyBrown: NmToken = NmToken.unsafe("SandyBrown")
    val SeaGreen: NmToken = NmToken.unsafe("SeaGreen")
    val Seashell: NmToken = NmToken.unsafe("Seashell")
    val Sienna: NmToken = NmToken.unsafe("Sienna")
    val Silver: NmToken = NmToken.unsafe("Silver")
    val SkyBlue: NmToken = NmToken.unsafe("SkyBlue")
    val SlateBlue: NmToken = NmToken.unsafe("SlateBlue")
    val SlateGray: NmToken = NmToken.unsafe("SlateGray")
    val SlateGrey: NmToken = NmToken.unsafe("SlateGrey")
    val Snow: NmToken = NmToken.unsafe("Snow")
    val SpringGreen: NmToken = NmToken.unsafe("SpringGreen")
    val SteelBlue: NmToken = NmToken.unsafe("SteelBlue")
    val Tan: NmToken = NmToken.unsafe("Tan")
    val Teal: NmToken = NmToken.unsafe("Teal")
    val Thistle: NmToken = NmToken.unsafe("Thistle")
    val Tomato: NmToken = NmToken.unsafe("Tomato")
    val Turquoise: NmToken = NmToken.unsafe("Turquoise")
    val Violet: NmToken = NmToken.unsafe("Violet")
    val Wheat: NmToken = NmToken.unsafe("Wheat")
    val White: NmToken = NmToken.unsafe("White")
    val WhiteSmoke: NmToken = NmToken.unsafe("WhiteSmoke")
    val Yellow: NmToken = NmToken.unsafe("Yellow")
    val YellowGreen: NmToken = NmToken.unsafe("YellowGreen")

    /** The 147 recommended values of `[Color Names]`, in catalog order. */
    val recommended: List[NmToken] =
      List(
        AliceBlue,
        AntiqueWhite,
        Aqua,
        Aquamarine,
        Azure,
        Beige,
        Bisque,
        Black,
        BlanchedAlmond,
        Blue,
        BlueViolet,
        Brown,
        BurlyWood,
        CadetBlue,
        Chartreuse,
        Chocolate,
        Coral,
        CornflowerBlue,
        Cornsilk,
        Crimson,
        Cyan,
        DarkBlue,
        DarkCyan,
        DarkGoldenrod,
        DarkGray,
        DarkGreen,
        DarkGrey,
        DarkKhaki,
        DarkMagenta,
        DarkOliveGreen,
        DarkOrange,
        DarkOrchid,
        DarkRed,
        DarkSalmon,
        DarkSeaGreen,
        DarkSlateBlue,
        DarkSlateGray,
        DarkSlateGrey,
        DarkTurquoise,
        DarkViolet,
        DeepPink,
        DeepSkyBlue,
        DimGray,
        DimGrey,
        DodgerBlue,
        FireBrick,
        FloralWhite,
        ForestGreen,
        Fuchsia,
        Gainsboro,
        GhostWhite,
        Gold,
        Goldenrod,
        Gray,
        Grey,
        Green,
        GreenYellow,
        Honeydew,
        HotPink,
        IndianRed,
        Indigo,
        Ivory,
        Khaki,
        Lavender,
        LavenderBlush,
        LawnGreen,
        LemonChiffon,
        LightBlue,
        LightCoral,
        LightCyan,
        LightGoldenrodYellow,
        LightGray,
        LightGreen,
        LightGrey,
        LightPink,
        LightSalmon,
        LightSeaGreen,
        LightSkyBlue,
        LightSlateGray,
        LightSlateGrey,
        LightSteelBlue,
        LightYellow,
        Lime,
        LimeGreen,
        Linen,
        Magenta,
        Maroon,
        MediumAquamarine,
        MediumBlue,
        MediumOrchid,
        MediumPurple,
        MediumSeaGreen,
        MediumSlateBlue,
        MediumSpringGreen,
        MediumTurquoise,
        MediumVioletRed,
        MidnightBlue,
        MintCream,
        MistyRose,
        Moccasin,
        NavajoWhite,
        Navy,
        OldLace,
        Olive,
        OliveDrab,
        Orange,
        OrangeRed,
        Orchid,
        PaleGoldenrod,
        PaleGreen,
        PaleTurquoise,
        PaleVioletRed,
        PapayaWhip,
        PeachPuff,
        Peru,
        Pink,
        Plum,
        PowderBlue,
        Purple,
        Red,
        RosyBrown,
        RoyalBlue,
        SaddleBrown,
        Salmon,
        SandyBrown,
        SeaGreen,
        Seashell,
        Sienna,
        Silver,
        SkyBlue,
        SlateBlue,
        SlateGray,
        SlateGrey,
        Snow,
        SpringGreen,
        SteelBlue,
        Tan,
        Teal,
        Thistle,
        Tomato,
        Turquoise,
        Violet,
        Wheat,
        White,
        WhiteSmoke,
        Yellow,
        YellowGreen
      )
  end NamedColor

  /** Ink and Varnish Coatings (Table A.3.9). */
  object Coatings:
    val Aqueous: NmToken = NmToken.unsafe("Aqueous")
    val Bronzing: NmToken = NmToken.unsafe("Bronzing")
    val Gloss: NmToken = NmToken.unsafe("Gloss")
    val Ink: NmToken = NmToken.unsafe("Ink")
    val InkJet: NmToken = NmToken.unsafe("InkJet")
    val Latex: NmToken = NmToken.unsafe("Latex")
    val Matte: NmToken = NmToken.unsafe("Matte")
    val Primer: NmToken = NmToken.unsafe("Primer")
    val Relief: NmToken = NmToken.unsafe("Relief")
    val RubResistant: NmToken = NmToken.unsafe("RubResistant")
    val Satin: NmToken = NmToken.unsafe("Satin")
    val Silicone: NmToken = NmToken.unsafe("Silicone")
    val Toner: NmToken = NmToken.unsafe("Toner")
    val UV: NmToken = NmToken.unsafe("UV")
    val Varnish: NmToken = NmToken.unsafe("Varnish")
    val WaterResistant: NmToken = NmToken.unsafe("WaterResistant")

  /** Texture catalog (§A.3.22 / Table A.80): recommended textures for
   *  `NMTOKEN` attributes such as `LaminatingIntent/@Texture` (Table 4.30).
   *  The catalog is open per ADR-0007: any other valid NMTOKEN remains legal.
   */
  object Texture:
    val Antique: NmToken = NmToken.unsafe("Antique")
    val Calendared: NmToken = NmToken.unsafe("Calendared")
    val Gloss: NmToken = NmToken.unsafe("Gloss")
    val IPPCourse: NmToken = NmToken.unsafe("IPP:Course")
    val IPPFine: NmToken = NmToken.unsafe("IPP:Fine")
    val IPPMedium: NmToken = NmToken.unsafe("IPP:Medium")
    val Linen: NmToken = NmToken.unsafe("Linen")
    val Matte: NmToken = NmToken.unsafe("Matte")
    val Smooth: NmToken = NmToken.unsafe("Smooth")
    val Stipple: NmToken = NmToken.unsafe("Stipple")
    val Uncalendared: NmToken = NmToken.unsafe("Uncalendared")
    val Vellum: NmToken = NmToken.unsafe("Vellum")

    val recommended: List[NmToken] = List(
      Antique, Calendared, Gloss, IPPCourse, IPPFine, IPPMedium,
      Linen, Matte, Smooth, Stipple, Uncalendared, Vellum
    )
  end Texture

  /** Hole Pattern Catalog (Appendix F, Table 8.30 `@Pattern`): predefined hole
   *  patterns. Data type `NMTOKEN`, allowed from Section F. The list here
   *  is the XSD enumeration (34 values, including `None`) — the normative
   *  text references Appendix F, the XSD is the structural oracle. Open
   *  catalog per ADR-0007: any NMTOKEN outside the list remains legal.
   */
  object HolePattern:
    val NonePattern: NmToken = NmToken.unsafe("None")
    val S1Generic: NmToken = NmToken.unsafe("S1-generic")
    val SGeneric: NmToken = NmToken.unsafe("S-generic")
    val R2Generic: NmToken = NmToken.unsafe("R2-generic")
    val R2mDIN: NmToken = NmToken.unsafe("R2m-DIN")
    val R2mISO: NmToken = NmToken.unsafe("R2m-ISO")
    val R2mMIB: NmToken = NmToken.unsafe("R2m-MIB")
    val R2iUSa: NmToken = NmToken.unsafe("R2i-US-a")
    val R2iUSb: NmToken = NmToken.unsafe("R2i-US-b")
    val R3Generic: NmToken = NmToken.unsafe("R3-generic")
    val R3iUS: NmToken = NmToken.unsafe("R3i-US")
    val R4Generic: NmToken = NmToken.unsafe("R4-generic")
    val R4mDINA4: NmToken = NmToken.unsafe("R4m-DIN-A4")
    val R4mDINA5: NmToken = NmToken.unsafe("R4m-DIN-A5")
    val R4mSwedish: NmToken = NmToken.unsafe("R4m-swedish")
    val R4iUS: NmToken = NmToken.unsafe("R4i-US")
    val R5Generic: NmToken = NmToken.unsafe("R5-generic")
    val R5iUSa: NmToken = NmToken.unsafe("R5i-US-a")
    val R5iUSb: NmToken = NmToken.unsafe("R5i-US-b")
    val R5iUSc: NmToken = NmToken.unsafe("R5i-US-c")
    val R6Generic: NmToken = NmToken.unsafe("R6-generic")
    val R6m4h2s: NmToken = NmToken.unsafe("R6m-4h2s")
    val R6mDINA5: NmToken = NmToken.unsafe("R6m-DIN-A5")
    val R7Generic: NmToken = NmToken.unsafe("R7-generic")
    val R7iUSa: NmToken = NmToken.unsafe("R7i-US-a")
    val R7iUSb: NmToken = NmToken.unsafe("R7i-US-b")
    val R7iUSc: NmToken = NmToken.unsafe("R7i-US-c")
    val R11m7h4s: NmToken = NmToken.unsafe("R11m-7h4s")
    val P16_9iRect0t: NmToken = NmToken.unsafe("P16_9i-rect-0t")
    val P12mRect0t: NmToken = NmToken.unsafe("P12m-rect-0t")
    val W2_1iRound0t: NmToken = NmToken.unsafe("W2_1i-round-0t")
    val W2_1iSquare0t: NmToken = NmToken.unsafe("W2_1i-square-0t")
    val W3_1iSquare0t: NmToken = NmToken.unsafe("W3_1i-square-0t")
    val C9_5mRound0t: NmToken = NmToken.unsafe("C9.5m-round-0t")

    val recommended: List[NmToken] = List(
      NonePattern, S1Generic, SGeneric, R2Generic, R2mDIN, R2mISO, R2mMIB, R2iUSa, R2iUSb,
      R3Generic, R3iUS, R4Generic, R4mDINA4, R4mDINA5, R4mSwedish, R4iUS,
      R5Generic, R5iUSa, R5iUSb, R5iUSc, R6Generic, R6m4h2s, R6mDINA5,
      R7Generic, R7iUSa, R7iUSb, R7iUSc, R11m7h4s, P16_9iRect0t, P12mRect0t,
      W2_1iRound0t, W2_1iSquare0t, W3_1iSquare0t, C9_5mRound0t
    )
  end HolePattern

  /** Hole reinforcement (Table 8.30 `@Reinforcement`): `NMTOKEN`, values
   *  include `Grommet`. Open catalog — any NMTOKEN remains legal.
   */
  object HoleReinforcement:
    val Grommet: NmToken = NmToken.unsafe("Grommet")
    val recommended: List[NmToken] = List(Grommet)
  end HoleReinforcement

  /** Certification claims (Table 8.8 `@Claim`): the name of the certification
   *  as defined by the issuing organization. The data type is `string` and the
   *  spec says "Values include", so this is an open catalog (ADR-0007) — any
   *  string remains legal, e.g. the parameterised `PEFC nn%`.
   *
   *  Note that the values are `XjdfString`, not `NmToken`: `FSC 100%` and
   *  `FSC Mix 70%` contain spaces and `%`, which no NMTOKEN may carry.
   */
  object CertificationClaim:
    val Fsc100: XjdfString = XjdfString.unsafe("FSC 100%")
    val FscMix70: XjdfString = XjdfString.unsafe("FSC Mix 70%")
    val FscMixCredit: XjdfString = XjdfString.unsafe("FSC Mix Credit")
    val FscRecycled85: XjdfString = XjdfString.unsafe("FSC Recycled 85%")
    val FscRecycledCredit: XjdfString = XjdfString.unsafe("FSC Recycled Credit")
    val PefcCertified: XjdfString = XjdfString.unsafe("PEFC Certified")
    val PefcRecycled: XjdfString = XjdfString.unsafe("PEFC Recycled")

    /** The `PEFC nn%` claim of Table 8.8, with the percentage filled in. */
    def pefcPercent(percent: Int): XjdfString = XjdfString.unsafe(s"PEFC $percent%")

    val recommended: List[XjdfString] = List(
      Fsc100, FscMix70, FscMixCredit, FscRecycled85, FscRecycledCredit,
      PefcCertified, PefcRecycled
    )
  end CertificationClaim

  /** Certification organizations (Table 8.8 `@Organization`): the identifier of
   *  the issuing organization. `NMTOKEN` with "Values include" — an open
   *  catalog (ADR-0007).
   */
  object CertificationOrganization:
    /** China's National Forest Certification System. */
    val CFCC: NmToken = NmToken.unsafe("CFCC")
    /** Forest Stewardship Council. */
    val FSC: NmToken = NmToken.unsafe("FSC")
    /** Sustainable Forest Management Requirements. */
    val IFCC: NmToken = NmToken.unsafe("IFCC")
    /** The Programme for the Endorsement of Forest Certification. */
    val PEFC: NmToken = NmToken.unsafe("PEFC")

    val recommended: List[NmToken] = List(CFCC, FSC, IFCC, PEFC)
  end CertificationOrganization

  /** Pipe protocols of `Dependent/@PipeProtocol` (Table 3.13). */
  object PipeProtocol:
    val IdentificationField: NmToken = NmToken.unsafe("IdentificationField")
    val XJMF: NmToken = NmToken.unsafe("XJMF")
    val XJMFPush: NmToken = NmToken.unsafe("XJMFPush")
    val XJMFPull: NmToken = NmToken.unsafe("XJMFPull")
    val None: NmToken = NmToken.unsafe("None")

  /** Work type details of `MISDetails/@WorkTypeDetails` (§8.30 / Table 8.48):
   *  machine-readable reason why the work was done. `NMTOKEN` with "Values
   *  include" — an open catalog (ADR-0007): any other valid NMTOKEN remains
   *  legal as a vendor or customer extension.
   */
  object WorkTypeDetails:
    /** The customer requested change(s) requiring the work. */
    val CustomerRequest: NmToken = NmToken.unsafe("CustomerRequest")
    /** Equipment used to produce the resource malfunctioned; the resource
     *  needs to be created again.
     */
    val EquipmentMalfunction: NmToken = NmToken.unsafe("EquipmentMalfunction")
    /** Change was made for production efficiency or another internal reason. */
    val InternalChange: NmToken = NmToken.unsafe("InternalChange")
    /** A resource needs to be created again to account for a damaged resource
     *  (damaged plate, etc.).
     */
    val ResourceDamaged: NmToken = NmToken.unsafe("ResourceDamaged")
    /** Incorrect operation of equipment or incorrect creation of a resource
     *  requires creating the resource again.
     */
    val UserError: NmToken = NmToken.unsafe("UserError")

    val recommended: List[NmToken] =
      List(CustomerRequest, EquipmentMalfunction, InternalChange, ResourceDamaged, UserError)
  end WorkTypeDetails
end Catalog
