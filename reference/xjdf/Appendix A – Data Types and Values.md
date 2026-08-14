# Appendix A — Data Types and Values

This appendix lists the XJDF data types and describes how they are encoded in XML. The appendix also contains commonly used closed list enumerations, for example **Activation**, and preferred values for open lists of `NMTOKEN` or strings, for example **Contact Types**.

All XJDF types are derived from XML Schema types defined in *XML Schema Part 2 — Datatypes*, see `[XMLSchema]`, either by extension, use of lists, or restriction.

---

## A.1 XJDF Data Types

All XJDF data types are described in the following table.

**Table A.1: XJDF Data Types**

| XJDF Data Type | XML Schema Data Type | Derivation | Description |
|---|---|---|---|
| `boolean` | `xsd:boolean` | Restriction | Only string literals are permitted. Numeric values `"0"` and `"1"` SHALL NOT be used. |
| `CMYKColor` | `xsd:float` | Restricted list | The list SHALL contain four values, each in the range `[0...1.0]`. `0.0` specifies no ink and `1.0` specifies full ink. The sequence SHALL be `C M Y K`. |
| `dateTime` | `xsd:dateTime` | None | Represents a specific instant of time. It SHALL be a UTC time or a local time that includes the time zone. |
| `duration` | `xsd:duration` | None | Represents a duration of time. The duration includes all time, including non-working hours such as tea breaks and non-working days such as weekends or holidays. |
| `enumeration` | `xsd:NMTOKEN` | Restriction | Individual token from a closed set of values. |
| `enumerations` | `xsd:NMTOKENS` | Restricted list | List of one or more unique tokens from a closed set of values. |
| `float` | `xsd:float` | None | Corresponds to the `[IEEE754]` single-precision, 32-bit floating point type. For details, see `[XMLSchema]`. |
| `FloatList` | `xsd:float` | List | A list of float values. |
| `hexBinary` | `xsd:hexBinary` | None | Represents arbitrary hex-encoded binary data. |
| `ID` | `xsd:ID` | None | Unique identifier as defined by `[XML]`. SHALL be unique within the scope of the XJDF document. |
| `IDREF` | `xsd:IDREF` | None | Reference to an element holding the unique identifier as defined by `[XML Specification 1.0]`. |
| `IDREFS` | `xsd:IDREFS` | None | List of references, `IDREF` values, separated by white space as defined by `[XML]`. |
| `integer` | `xsd:integer` | None | Represents numerical integer values. Values greater than +/-2³¹ are not expected to occur for this data type. For details, see `[XMLSchema]`. |
| `IntegerList` | `xsd:integer` | List | A list of integer values. |
| `IntegerRange` | `xsd:integer` | Restricted list | The list SHALL contain two values representing a range of values. An `IntegerRange` MAY be used to select a contiguous set of items from a list, as defined in Section 1.10.2, “Counting in XJDF.” In this case, the two values represent an inclusive range of index values to be selected from the target list. For example, `"a b"` selects items `Ia...Ib` inclusive, and `"m m"` selects the single item `Im`. |
| `LabColor` | `xsd:float` | Restricted list | The list SHALL contain exactly three values in the sequence `L a b`. The value of `L` is restricted to `[0..100]`; `a` and `b` are unbounded. Values of type `LabColor` are used to specify absolute Lab colors. The Lab values are normalized to a light of D50 and an angle of 2 degrees as specified in `[CIE 015:2004]` and `[ISO13655:2017]`. This corresponds to a white point of `X = 0.9642`, `Y = 1.0000`, and `Z = 0.8249` in CIEXYZ color space. |
| `language` | `xsd:language` | None | Represents a language and country code, for example `en-US`, for a natural language. Values SHALL conform to `[RFC3066]`. |
| `languages` | `xsd:language` | List | A list of language values. |
| `matrix` | `xsd:float` | Restricted list | The list SHALL contain six values representing the sequence `a b c d Tx Ty`. The variables `Tx` and `Ty` describe distances, which are defined in points. For more details, see Section 2.6.3, “Coordinate System Transformations.” |
| `NMTOKEN` | `xsd:NMTOKEN` | None | A continuous sequence of special characters as defined by `[XML]`. `NMTOKEN` values MAY begin with any non-white-space character, including numerical characters. |
| `NMTOKENS` | `xsd:NMTOKENS` | None | White-space-separated list of `NMTOKEN` values. |
| `PDFPath` | `xsd:string` | Restriction | Values of type `PDFPath` are encoded as a string that conforms to a sequence of PDF path operators. PDF operators are limited to those described in “Path Construction Operators” in `[PDF1.6]`. |
| `rectangle` | `xsd:float` | Restricted list | The list SHALL contain four values representing `llx lly urx ury`. |
| `regExp` | `xsd:normalizedString` | None | Regular expression as defined by `[XMLSchema]`. |
| `RGBColor` | `xsd:float` | Restricted list | The list SHALL contain three values representing the sequence `r g b`. A value of `0.0` SHALL specify no intensity, black, and a value of `1.0` SHALL specify full intensity. |
| `shape` | `xsd:float` | Restricted list | The list SHALL contain three values representing the sequence `width height depth`, which are the same as `x y z`. |
| `string` | `xsd:normalizedString` | Restriction | The length of the string SHALL NOT exceed 1023 characters. In order to enable fixed-length storage of strings in databases, string values SHALL NOT be longer than 1023 characters. Tabs, line feeds, and similar characters are not valid characters. |
| `text` | `xsd:string` | None | String data in the body of an XML element. This is the only data type that is not encoded as an XML attribute. |
| `TransferFunction` | `xsd:float` | Restricted list | The list SHALL contain an even number of values representing a sequence of `x0 y0 x1 y1 ... xn yn` pairs. See Section A.1.1, “TransferFunction.” |
| `URI` | `xsd:anyURI` | None | Values of type `URI` represent a Uniform Resource Identifier, as defined in `[RFC3986]`. The URI data type is represented as an Internationalized Resource Identifier, IRI, as defined in `[RFC3987]`. |
| `URL` | `xsd:anyURI` | None | Values of type `URL` represent a Uniform Resource Locator, as defined in `[RFC3986]`. The URL data type is represented as an Internationalized Resource Identifier, IRI, as defined in `[RFC3987]`. Some characters in a URL SHALL be escaped, and all characters MAY be escaped by encoding their UTF-8 representation into a `%` followed by the two-digit hex representation of the character. The list of characters that SHALL be encoded is dependent on the URL scheme. Non-escaped characters SHALL be encoded in the encoding of the containing XJDF document. |
| `XPath` | `xsd:token` | None | Values of type `XPath` represent an XPath expression as described in `[XPath]`. |
| `XYPair` | `xsd:float` | Restricted list | The list SHALL contain two values representing the sequence `x y`. |

### A.1.1 TransferFunction

Values of type `TransferFunction` are functions that have a one-dimensional input and output. In XJDF, they are encoded as a simple kind of sampled function and are used to describe transfer curves of image transfer processes from one medium to the next, for example film to plate, or plate to press.

A transfer curve consists of a series of XY pairs, where each pair consists of the stimuli, `X`, and the resulting value, `Y`. To calculate the result of a certain stimulus, the following algorithm SHALL be applied:

1. If `x <= first stimulus`, then the result is the `y` value of the first XY pair.
2. If `x >= last stimulus`, then the result is the `y` value of the last XY pair.
3. Search the interval in which `x` is located.
4. Return the linearly interpolated value of `y` within that interval.

---

## A.2 Enumerations

This section contains tables, each with a closed set of values for an `enumeration` or `enumerations` type. If there are any implications to the order of the values, this is detailed in the description; otherwise, no order is implied.

### A.2.1 Action

`Action` specifies what action, if any, to take as a result of a particular event.

**Table A.2: Action Enumeration Values**

| Value | Description |
|---|---|
| `Abort` | Abort the ongoing activity and do not proceed with any other further activity. |
| `Continue` | Continue with the present activity. Details SHOULD be logged. |
| `Repair` | Repair the condition before proceeding with the activity. Details SHOULD be logged. The actions required to perform the repair are system specified. |

### A.2.2 Activation

`Activation` SHALL specify the activation of a `QueueEntry`.

The values in the following table are ordered from least active to most active.

**Table A.3: Activation Enumeration Values**

| Value | Description |
|---|---|
| `Informative` | The `QueueEntry` is for information only. If a `QueueEntry` is `Informative`, it SHALL NOT be processed. Queue entries with `@Activation="Informative"` will generally be sent to an operator console for preview but are still completely under the control of an external Controller. |
| `Held` | The `QueueEntry` has been held and SHALL NOT be processed until its `@Activation` is changed to `Active`. The transition to `Active` MAY be triggered with `CommandModifyQueueEntry/ModifyQueueEntryParams/@Operation="Resume"`. |
| `Active` | The `QueueEntry` is active and SHALL be processed regularly. |
| `PendingReturn` | Indicates that the `QueueEntry` has been processed but has not yet been successfully returned to the respective Controller. |
| `Removed` | The `QueueEntry` has been removed. This `@Activation` SHALL NOT be provided unless `Queue/@UpdateGranularity="ChangesOnly"`. |

### A.2.3 Anchor

`Anchor` specifies the nine anchor points of a rectangle.

**Table A.4: Anchor Enumeration Values**

| Value | Description |
|---|---|
| `BottomCenter` | — |
| `BottomLeft` | — |
| `BottomRight` | — |
| `Center` | — |
| `CenterLeft` | — |
| `CenterRight` | — |
| `TopCenter` | — |
| `TopLeft` | — |
| `TopRight` | — |

### A.2.4 Automation

`Automation` specifies how complete an item is.

**Table A.5: Automation Enumeration Values**

| Value | Description |
|---|---|
| `Dynamic` | The item is incomplete and should be completed automatically. |
| `Static` | The item is complete. |

### A.2.5 Axis

`Axis` specifies the notional line around which an operation, such as mirroring, SHALL be performed.

**Table A.6: Axis Enumeration Values**

| Value | Description |
|---|---|
| `Both` | The operation is performed around both axes. |
| `FeedDirection` | The operation is performed around the feed direction axis. |
| `MediaWidth` | The operation is performed around the media width axis. |
| `None` | No operation is to be performed. |

### A.2.6 BinderMaterial

`BinderMaterial` specifies the material that SHALL be used for loose binding.

**Table A.7: BinderMaterial Enumeration Values**

| Value | Description |
|---|---|
| `ColorCoatedSteel` | Coated steel. |
| `Plastic` | Any kind of plastic. |
| `Steel` | Plain steel. |

### A.2.7 BindingType

`BindingType` specifies the required style of binding to be used.

**Table A.8: BindingType Enumeration Values**

| Value | Description |
|---|---|
| `AdhesiveNote` | Binding with removable adhesive on the back side of a product. Typically used for small brightly colored paper designed to be stuck prominently to an object or surface and easily removed when necessary. |
| `ChannelBinding` | Metal clamps are used to bind sheets. This type of binding is handled by the `LooseBinding` process. |
| `CoilBinding` | Metal wire, plastic-coated wire, or pure plastic wire is used to fasten pre-punched sheets of paper, cardboard, or other materials. This type of binding is handled by the `LooseBinding` process. |
| `CombBinding` | Plastic insert wraps through pre-punched holes in the substrate. This type of binding is handled by the `LooseBinding` process. |
| `CornerStitch` | Stitch in the corner that is at the clockwise end of the binding edge. This type of binding is handled by the `Stitching` process. |
| `EdgeGluing` | Gluing gathered sheets at one edge of the pile. This type of binding is handled by the `Gluing` process. Products of this type are also referred to as padded. |
| `HardCover` | This type of binding defines a hardcover bound book. This type of binding is handled by the `CaseMaking` process. |
| `LooseBinding` | Generic loose binding — one of `ChannelBinding`, `CoilBinding`, `CombBinding`, `RingBinding`, or `StripBinding`. These types of binding are handled by the `LooseBinding` process. |
| `None` | This type of binding defines a stack of pages with no additional binding. |
| `RingBinding` | Pre-punched sheets are placed in a ring binder. This type of binding is handled by the `LooseBinding` process. |
| `SaddleStitch` | Sheets are bound together using stitches along the middle fold, which is on a saddle. This type of binding is handled by the `Stitching` process. |
| `SideStitch` | Sheets are bound together using stitches along the reference edge. This type of binding is handled by the `Stitching` process. |
| `SoftCover` | This type of binding defines a softcover bound book. It includes perfect binding and is handled by the `CoverApplication` process. |
| `StripBinding` | Hard plastic strips are held together by plastic pins, which in turn are bound to the strips with heat. This type of binding is handled by the `LooseBinding` process. |
| `Tape` | This type of binding is an inexpensive version of `SoftCover`. It is handled by the `CoverApplication` process. |
| `WireComb` | Wire is used to fasten pre-punched sheets of paper, cardboard, or other materials. This type of binding is handled by the `LooseBinding` process. |

### A.2.8 BundleType

`BundleType` specifies the type of items that are bundled.

**Table A.9: BundleType Enumeration Values**

| Value | Description |
|---|---|
| `BoundSet` | Stack of components that are bound together. |
| `Box` | Convenience packaging that is not envisioned to be protection for shipping. |
| `Carton` | Protection packaging typically used for shipping. |
| `CollectedStack` | Components collected on a saddle, for example as a result of the `Collecting` process. |
| `CompensatedStack` | Loose stack of compensated components. |
| `Pallet` | — |
| `Product` | An individual product. |
| `Roll` | Rolled components on a print roll. |
| `Sheet` | Multiple individual items printed on one sheet. |
| `Stack` | Loose stack of equally stacked components. |
| `StrappedCompensatedStack` | Strapped stack of compensated components. |
| `StrappedStack` | Strapped stack of equally stacked components. |
| `WrappedBundle` | — |

### A.2.9 ChannelMode

`ChannelMode` specifies the reliability mode of a message channel.

**Table A.10: ChannelMode Enumeration Values**

| Value | Description |
|---|---|
| `FireAndForget` | The receiver of the signal MAY respond using an XJMF response message. |
| `Reliable` | Indicates that the signal is the result of a subscription where reliable signaling was specified. The receiver of the signal SHALL respond using an XJMF response message. |

### A.2.10 Coating

`Coating` specifies the coating of a substrate.

**Table A.11: Coating Enumeration Values**

| Value | Description |
|---|---|
| `Coated` | A coating of a system-specified type. |
| `Gloss` | A glossy coating. |
| `Matte` | A matte coating. |
| `None` | No coating. |
| `Satin` | A coating between `Gloss` and `Matte`. |

### A.2.11 Compensation

`Compensation` specifies how a process SHALL apply transfer curve compensation.

**Table A.12: Compensation Enumeration Values**

| Value | Description |
|---|---|
| `Film` | Compensated until film exposure. |
| `None` | No compensation. |
| `Plate` | Compensated until plate exposure. |
| `Press` | Compensated until press. |

### A.2.12 CutMarkType

`CutMarkType` specifies the types of printer’s mark used to aid cutting.

The original table contains a `SYMBOL` column with small graphical symbols. Those symbols are omitted here; the value names identify the mark positions.

**Table A.13: CutMarkType Enumeration Values**

| Value | Symbol | Description |
|---|---|---|
| `CrossCutMark` | Graphic symbol omitted | Centered at logical position. |
| `TopVerticalCutMark` | Graphic symbol omitted | Slightly above logical position. |
| `BottomVerticalCutMark` | Graphic symbol omitted | Slightly below logical position. |
| `LeftHorizontalCutMark` | Graphic symbol omitted | Slightly to the left of logical position. |
| `RightHorizontalCutMark` | Graphic symbol omitted | Slightly to the right of logical position. |
| `LowerLeftCutMark` | Graphic symbol omitted | Corner at logical position. |
| `UpperLeftCutMark` | Graphic symbol omitted | Corner at logical position. |
| `LowerRightCutMark` | Graphic symbol omitted | Corner at logical position. |
| `UpperRightCutMark` | Graphic symbol omitted | Corner at logical position. |

### A.2.13 DataType

`DataType` is used to specify the data type of a value where it cannot be inferred from the context and thus needs to be explicitly stated. It is therefore expected that `DataType` will be suitably paired with an item containing the value.

**Table A.14: DataType Enumeration Values**

| Value | Description |
|---|---|
| `boolean` | Binary value logic, either `true` or `false`. |
| `dateTime` | Represents a specific instant of time. It SHALL be a UTC time or a local time that includes the time zone. |
| `duration` | Represents a duration of time. |
| `float` | Corresponds to the `[IEEE754]` single-precision, 32-bit floating point type. For details, see `[XMLSchema]`. |
| `integer` | Represents numerical integer values. For details, see `[XMLSchema]`. |
| `NamedFeature` | This represents a named feature as defined in Section 3.1.3.1, “Specifying NamedFeatures with GeneralID.” `NamedFeature` describes a value that is identified by a specific name; thus, in this case, it is expected to have both an item containing the value and an item containing the name. For example: `<GeneralID DataType="NamedFeature" IDUsage="pool" IDValue="bar snax"/>`. |
| `NMTOKEN` | A continuous sequence of special characters as defined by `[XML]`. |
| `string` | Character strings without tabs or line feeds. Corresponds to the standard XML `normalizedString` data type. For details, see `[XMLSchema]`. |

### A.2.14 DeviceStatus

`DeviceStatus` specifies the state of a Device.

**Table A.15: DeviceStatus Enumeration Values**

| Value | Description |
|---|---|
| `Cleanup` *(New in XJDF 2.1)* | The Device is cleaning up. Either an XJDF with `NodeInfo/@Status="Cleanup"` is being processed or the Device is cleaning up with no jobs loaded. |
| `Idle` | No job is being processed and the Device is accepting new jobs. |
| `NonProductive` | The Device is not doing productive work but rather doing something like maintenance or running a test job. |
| `Offline` | The Device is either switched off, cannot be accessed, or is in stand-by that requires a wake-up. |
| `Production` | At least one job is in a productive status on the Device, i.e. `NodeInfo/@Status="Running"`. If multiple jobs are running in parallel, the value of `DeviceInfo/@Status` SHOULD be `Production`. |
| `Setup` *(New in XJDF 2.1)* | The Device is setting up. Either an XJDF with `NodeInfo/@Status="Setup"` is being processed or the Device is setting up with no jobs loaded. |
| `Stopped` | At least one job with `NodeInfo/@Status="Stopped"` is being processed or the Device. This status indicates some kind of break as long as execution has not been aborted. |

### A.2.15 Drying

`Drying` specifies the method employed to dry an item.

**Table A.16: Drying Enumeration Values**

| Value | Description |
|---|---|
| `Heatset` | Heatset dryer. |
| `IR` | Infrared dryer. |
| `Off` | No dryer is used. |
| `On` | The Device’s default drying unit is used. |
| `UV` | Ultraviolet dryer. |

### A.2.16 Edge

`Edge` specifies the edge of an object.

**Table A.17: Edge Enumeration Values**

| Value | Description |
|---|---|
| `Bottom` | Bottom edge of a sheet or product. |
| `Left` | Left edge of a sheet or product. |
| `Right` | Right edge of a sheet or product. |
| `Top` | Top edge of a sheet or product. |

### A.2.17 EmbossDirection

`EmbossDirection` specifies the type and direction of embossing.

**Table A.18: EmbossDirection Enumeration Values**

| Value | Description |
|---|---|
| `Both` | Both debossing and embossing using one stamp. |
| `Depressed` | Debossing only. |
| `Flat` | The embossing foil is applied flat. Used for foil stamping. |
| `Raised` | Embossing only. |

### A.2.18 EmbossType

`EmbossType` specifies the type of embossing required.

**Table A.19: EmbossType Enumeration Values**

| Value | Description |
|---|---|
| `BlindEmbossing` | Embossed forms are not inked or foiled. The color of the image is the same as the substrate. |
| `Braille` | Six-dot braille embossing. |
| `EmbossedFinish` | The overall design or pattern is impressed in laminated paper when passed between metal rollers engraved with the desired pattern. It is produced on a special embossing Device to create finishes such as linen. |
| `FoilEmbossing` | Combines embossing and foil stamping in a single operation. |
| `FoilStamping` | Uses a heated die to place a metallic or pigmented image from coated foil onto the substrate. |

### A.2.19 Face

`Face` specifies the location on a three-dimensional object, for example a Component.

**Table A.20: Face Enumeration Values**

| Value | Description |
|---|---|
| `Back` | Back side of a sheet or product. |
| `Bottom` | Bottom of a product. |
| `Front` | Front side of a sheet or product. |
| `Left` | Left side of a product, for example the spine of a left-bound book. |
| `Right` | Right side of a product, for example the spine of a right-bound book. |
| `Top` | Top of a product. |

### A.2.20 FeedQuality

`FeedQuality` specifies the action of a feeder in response to a feeder failure condition.

**Table A.21: FeedQuality Enumeration Values**

| Value | Description |
|---|---|
| `Check` | Check the quality and register. |
| `NotActive` | Quality control is not active. |
| `StopNoWaste` | Check the quality and register. The consuming Device SHALL stop after the predefined number of consecutive errors. The error SHALL be corrected, for example manually. |
| `StopWaste` | Check the quality and register. The object failing the test SHALL be waste. The consuming Device SHALL stop after the predefined number of consecutive errors. The error SHALL be corrected, for example manually. |
| `Waste` | The object failing the test SHALL be waste. |

### A.2.21 FitPolicy

`FitPolicy` specifies how an object should be manipulated to enable it to fit into a given area.

The “given direction” in the following text is derived from the attribute’s context. For example, for `@HorizontalFitPolicy`, this would be horizontal.

**Table A.22: FitPolicy Enumeration Values**

| Value | Description |
|---|---|
| `NoRepeat` | The object is neither resized nor repeated. If it is bigger than the given area, then it SHALL be clipped. |
| `RepeatToFill` | The object SHALL be placed in the requested position. It SHALL then be repeated in the given direction, allowing clipping to occur, until all the allocated space is filled. |
| `RepeatUnclipped` | The object SHALL be placed in the requested position. It SHALL then be repeated in the given direction, without clipping, to fill as much of the allocated space as possible. |
| `StretchToFit` | The object SHALL be stretched along the given direction to entirely fill the allocated space. If used in isolation, this can result in distortion of the object’s aspect ratio. |
| `UndistortedScaleToFit` | The object SHALL be resized to fit in the given direction. For the orthogonal direction, this may result in either the object being clipped or the object not filling the allocated space. |

### A.2.22 GangPolicy

`GangPolicy` specifies how multiple jobs SHALL be ganged.

**Table A.23: GangPolicy Enumeration Values**

| Value | Description |
|---|---|
| `Gang` | The job SHALL be ganged and MAY be submitted to the Device. |
| `GangAndForce` | The job SHALL be ganged and SHALL be submitted to the Device. |
| `NoGang` | The job SHALL NOT be ganged. |

### A.2.23 Glue

`Glue` specifies the type of glue to be used.

**Table A.24: Glue Enumeration Values**

| Value | Description |
|---|---|
| `ColdGlue` | — |
| `Hotmelt` | — |
| `PUR` | Polyurethane rubber. |

### A.2.24 IncludeResources

`IncludeResources` specifies how fonts SHALL be embedded.

**Table A.25: IncludeResources Enumeration Values**

| Value | Description |
|---|---|
| `IncludeNever` | Never embed fonts. |
| `IncludeOncePerDoc` | Embed once per document. |
| `IncludeOncePerPage` | Embed once per page. |

### A.2.25 ISOPaperSubstrate

`ISOPaperSubstrate` specifies a print substrate according to either `[ISO12647-2:2023]`, `[ISO12647-3:2013]`, or `[ISO12647-4:2014]`.

See Section B.3, “Paper Grade,” for a mapping to the paper grade values defined in `[ISO12647-2:2004]` and `[ISO12647-2:2023]`.

**Table A.26: ISOPaperSubstrate Enumeration Values**

| Value | Description |
|---|---|
| `LWCPlus` *(New in XJDF 2.1)* | Light weight calendered plus. From `[ISO12647-4:2014]`. |
| `LWCStandard` *(New in XJDF 2.1)* | Light weight calendered standard. From `[ISO12647-4:2014]`. |
| `NewsPlus` *(New in XJDF 2.1)* | Newsprint plus. From `[ISO12647-4:2014]`. |
| `PS1` | Premium coated. From `[ISO12647-2:2023]`. |
| `PS2` | Improved coated. From `[ISO12647-2:2023]`. |
| `PS3` | Standard coated glossy. From `[ISO12647-2:2023]`. |
| `PS4` | Standard coated matte. From `[ISO12647-2:2023]`. |
| `PS5` | Wood-free uncoated. From `[ISO12647-2:2023]`. |
| `PS6` | Super calendered. From `[ISO12647-2:2023]`. |
| `PS7` | Improved uncoated. From `[ISO12647-2:2023]`. |
| `PS8` | Standard uncoated. From `[ISO12647-2:2023]`. |
| `PS9` *(New in XJDF 2.2)* | Premium coated. From `[ISO12647-2:2023]`. |
| `SCPlus` *(New in XJDF 2.1)* | Super calendered plus. From `[ISO12647-4:2014]`. |
| `SCStandard` *(New in XJDF 2.1)* | Super calendered standard. From `[ISO12647-4:2014]`. |
| `SNP` *(New in XJDF 2.1)* | Standard newsprint. From `[ISO12647-3:2013]`. |

### A.2.26 JDFJMFVersion

`JDFJMFVersion` specifies the version of a JDF or JMF instance.

**Table A.27: JDFJMFVersion Enumeration Values**

| Value | Description |
|---|---|
| `1.0` | JDF 1.0. |
| `1.1` *(New in JDF 1.1)* | JDF 1.1. |
| `1.2` *(New in JDF 1.2)* | JDF 1.2. |
| `1.3` *(New in JDF 1.3)* | JDF 1.3. |
| `1.4` *(New in JDF 1.4)* | JDF 1.4. |
| `1.5` *(New in JDF 1.5)* | JDF 1.5. |
| `1.6` *(New in JDF 1.6)* | JDF 1.6. |
| `1.7` *(New in JDF 1.7)* | JDF 1.7. |
| `1.8` *(New in JDF 1.8)* | JDF 1.8. |

### A.2.27 MappingSelection

`MappingSelection` specifies how a Device should construct a color.

**Table A.28: MappingSelection Enumeration Values**

| Value | Description |
|---|---|
| `UseLocalPrinterValues` | Use the Device’s best local mapping. |
| `UsePDLValues` | Use color values specified in the PDL. See `[ColorPS]`. |
| `UseProcessColorValues` | Use the values defined in the associated process. |

### A.2.28 MediaDirection

`MediaDirection` specifies a preferred orientation of a characteristic of Media, such as grain or flute.

**Table A.29: MediaDirection Enumeration Values**

| Value | Description |
|---|---|
| `Any` | No restrictions apply to alignment of the media property. |
| `SameDirection` | The media property SHALL be aligned along the same axis of the coordinate system for all items. |
| `XDirection` | The media property SHALL be aligned along the X-axis of the coordinate system. |
| `YDirection` | The media property SHALL be aligned along the Y-axis of the coordinate system. |

### A.2.29 MediaType

`MediaType` specifies the general type of media to be used.

**Table A.30: MediaType Enumeration Values**

| Value | Description |
|---|---|
| `Blanket` | A blanket used for varnishing. |
| `CorrugatedBoard` | Media that consists of multiple sheets of paper, called liners, with fluted material in between. |
| `Disc` | CD or DVD disc to be printed on. |
| `EmbossingFoil` *(Deprecated in XJDF 2.1; deprecation amended in XJDF 2.2)* | Foil that is used in the `Embossing` process when `EmbossingParams/Emboss/@EmbossingType=["FoilEmbossing" or "FoilStamping"]`. Deprecation note: From XJDF 2.2, use `MiscConsumable(Foil)`. |
| `Film` | Media that is coated with a light-sensitive layer that can be exposed with a process like `ImageSetting`. |
| `Foil` *(Deprecated in XJDF 2.2)* | Foil that is used in the `Embossing` process when `EmbossingParams/Emboss/@EmbossingType=["FoilEmbossing" or "FoilStamping"]`. Deprecation note: From XJDF 2.2, use `MiscConsumable(Foil)`. |
| `GravureCylinder` | Gravure cylinder. |
| `ImagingCylinder` | Reusable direct imaging cylinder in a press. |
| `LaminatingFoil` *(Deprecated in XJDF 2.2)* | Media that is used to adhere to a substrate for protecting or surface enhancement. Typically a transparent media with a gloss, matte, or semi-gloss surface. Deprecation note: From XJDF 2.2, use `MiscConsumable(Foil)`. |
| `MountingTape` *(Deprecated in XJDF 2.2)* | Flexo plate mounting tape. Deprecation note: From XJDF 2.2, use `MiscConsumable(MountingTape)`. |
| `Other` | Something other than a media defined by this table. |
| `Paper` | Unprinted paper. Includes single-layer cardboard. |
| `Plate` | A printing plate used in, for example, offset printing technology. |
| `Screen` | Used for screen printing. |
| `SelfAdhesive` *(Deprecated in XJDF 2.2)* | Media that consists of multiple layers that include media and glue. Deprecation note: From XJDF 2.2, use the `MediaLayers/Media/@MediaType` that best describes the composite media. |
| `ShrinkFoil` *(Deprecated in XJDF 2.2)* | Consumable. Deprecation note: From XJDF 2.2, use `MiscConsumable(ShrinkWrap)`. |
| `Sleeve` | Flexo sleeve. |
| `Synthetic` *(New in XJDF 2.1)* | Any print substrate that contains a large amount of synthetic material, such as vinyl. |
| `Textile` | Media that is a type of cloth or woven fabric. |
| `Transparency` | Media that is transparent, typically used for presentation purposes. |
| `Vinyl` *(Deprecated in XJDF 2.1)* | Deprecation note: Use `@MediaType="Synthetic"` and `@MediaTypeDetails="Vinyl"`. |

### A.2.30 NamedColor

`NamedColor` specifies a machine-readable definition of a color. For a list of allowed values, see `[Color Names]`.

### A.2.31 Opacity

`Opacity` specifies the opacity of a resource.

**Table A.31: Opacity Enumeration Values**

| Value | Description |
|---|---|
| `Opaque` | The media or resource is opaque and does not transmit light under normal incident lighting conditions. |
| `Translucent` | The media or resource is translucent. For example, translucent material can be used for back-lit viewing. |
| `Transparent` | The media or resource is transparent. |

### A.2.32 Orientation

`Orientation` specifies the orientation of a Resource. For details, see Table 2.1, “Matrices and Orientation values for describing the orientation of a Component.”

In the transformation matrix values below, `h` and `w` refer to the height and width of the object being transformed.

**Table A.32: Orientation Enumeration Values**

| Value | Equivalent Transformation Matrix | Description |
|---|---:|---|
| `Rotate0` | `1 0 0 1 0 0` | No action. |
| `Rotate90` | `0 1 -1 0 h 0` | 90° counterclockwise rotation. |
| `Rotate180` | `-1 0 0 -1 w h` | 180° rotation. |
| `Rotate270` | `0 -1 1 0 0 w` | 270° counterclockwise rotation. |
| `Flip0` | `1 0 0 -1 0 h` | Flip around X. |
| `Flip90` | `0 -1 -1 0 h w` | 90° counterclockwise rotation + flip around X. |
| `Flip180` | `-1 0 0 1 w 0` | 180° rotation + flip around X. |
| `Flip270` | `0 1 1 0 0 0` | 270° counterclockwise rotation + flip around X. |

### A.2.33 Polarity

`Polarity` specifies whether a given image SHALL be color inverted.

**Table A.33: Polarity Enumeration Values**

| Value | Description |
|---|---|
| `Negative` | The image is color-inverted. |
| `Positive` | The image is not color-inverted. |

### A.2.34 PositionPolicy

`PositionPolicy` specifies the level of freedom when applying placement or positioning values.

**Table A.34: PositionPolicy Enumeration Values**

| Value | Description |
|---|---|
| `Exact` | The values SHALL be followed precisely. |
| `Free` | The values are used as guidance and MAY be modified by the designer. |

### A.2.35 RenderingIntent

`RenderingIntent` specifies the rendering intent that SHALL be applied when rendering the selected object. Values are defined in `[ICC.1]`.

**Table A.35: RenderingIntent Enumeration Values**

| Value | Description |
|---|---|
| `AbsoluteColorimetric` | — |
| `ColorSpaceDependent` | The rendering intent is dependent on the color space. The dependencies are implementation specific. |
| `Perceptual` | — |
| `RelativeColorimetric` | — |
| `Saturation` | — |

### A.2.36 Scope

`Scope` specifies the availability of resources and amounts in a Device.

**Table A.36: Scope Enumeration Values**

| Value | Description |
|---|---|
| `Allowed` | The resources are potentially available but currently not available without operator intervention. |
| `Device` *(New in XJDF 2.2)* | The amount of resources is an absolute measurement that is currently available within the scope of a Device. |
| `Estimate` | The amount of resources is an estimate that a Device has calculated within the scope of a job. |
| `Job` | The amount of resources is an actual measurement of data that is currently available within the scope of a job. |
| `Present` | The resources are currently available without operator intervention. |

### A.2.37 Severity

`Severity` specifies the severity of an error.

This table is not ordered alphabetically; it is ordered by increasing level of severity.

**Table A.37: Severity Enumeration Values**

| Value | Description |
|---|---|
| `Event` | Normal operating event. |
| `Information` | Informational event worthy of being logged. |
| `Warning` | A minor error. The executing Device is able to repair the condition and continue. |
| `Error` | A significant error. Operator intervention is required to allow the Device to continue. |
| `Fatal` | A fatal error. The Device has aborted the operation and cannot continue. |

### A.2.38 SheetLay

`SheetLay` specifies the reference edge where media or components are placed in a Device. `SheetLay` SHALL be specified in the Device coordinate system and therefore applies to the media or component after any rotation specified in `Resource/@Orientation` or `Resource/@Transformation` has been applied.

**Table A.38: SheetLay Enumeration Values**

| Value | Description |
|---|---|
| `Center` | The media is placed in the center. This is most commonly used in web Devices. |
| `Left` | The media is placed so that it is guided on the left. |
| `Right` | The media is placed so that it is guided on the right. |

### A.2.39 Side

`Side` specifies which side is to be used for an action.

**Table A.39: Side Enumeration Values**

| Value | Description |
|---|---|
| `Back` | The back surface. |
| `Front` | The front surface. |

### A.2.40 Sides

`Sides` specifies the sides of the product that SHALL be imaged.

**Table A.40: Sides Enumeration Values**

| Value | Description |
|---|---|
| `OneSided` | Page contents SHALL be imposed on the front side of the Final Product. |
| `OneSidedBack` | Page contents SHALL be imposed on the back side of the Final Product. |
| `TwoSidedHeadToFoot` | Page contents SHALL be imposed on the front and back sides of media sheets so that the head, top, of the front backs up to the foot, bottom, of the back. |
| `TwoSidedHeadToHead` | Page contents SHALL be imposed on the front and back sides so that the head, top, of the page contents back up to each other. |
| `Unprinted` *(New in XJDF 2.1)* | Page contents SHALL NOT be imposed on either side. |

### A.2.41 SourceColorSpace

`SourceColorSpace` specifies the color space that is to be operated on.

**Table A.41: SourceColorSpace Enumeration Values**

| Value | Description |
|---|---|
| `All` | Operates on all source color spaces. This is useful when specifying a convert operation using all PDL source-supplied characterizations with an XJDF-supplied final target Device profile. |
| `CalGray` | Defines a calibrated Device-independent representation of gray. |
| `Calibrated` | Operates on `CalGray` and `CalRGB` color spaces. |
| `CalRGB` | Defines a calibrated Device-independent representation of RGB. |
| `CIEBased` | Operates on CIE-based color spaces: `CIEBasedA`, `CIEBasedABC`, `CIEBasedDEF`, and `CIEBasedDEFG`. |
| `CMYK` | Operates on all CMYK color spaces. This includes both characterized and uncharacterized CMYK color spaces. |
| `DeviceCMYK` | Operates on uncharacterized CMYK color spaces. |
| `DeviceGray` | Operates on uncharacterized gray color spaces. |
| `DeviceN` | Identifies the source color encoding as a `DeviceN` color space. The specific `DeviceN` color space to operate on is defined in the `ColorantControl/DeviceNSpace` resource. If `DeviceN` is specified, then `ColorantControl/DeviceNSpace` SHALL also be present. |
| `DeviceRGB` | Operates on uncharacterized RGB color spaces. |
| `Gray` | Operates on all gray color spaces. This includes both characterized and uncharacterized gray color spaces. |
| `ICCBased` | Operates on color spaces defined using ICC profiles. The `ICCBased` value includes EPS, TIFF, or PICT files with embedded ICC profiles. See `[ICC.1]`. It also includes PDF Device color spaces that are characterized in footnote b of Table A.42. |
| `ICCCMYK` | Operates on `ICCBased` color spaces with ICC CMYK profiles or `DeviceCMYK` having an ICC-based characterization. See footnote b of Table A.42. |
| `ICCGray` | Operates on `ICCBased` color spaces with ICC gray profiles or `DeviceGray` having an ICC-based characterization. See footnote b of Table A.42. |
| `ICCLAB` | Operates on an `ICCBased` Device-independent representation of Lab. |
| `ICCRGB` | Operates on `ICCBased` color spaces with ICC RGB profiles or `DeviceRGB` having an ICC-based characterization. See footnote b of Table A.42. |
| `Lab` | Operates on Lab color spaces. |
| `RGB` | Operates on all RGB color spaces. This includes both characterized and uncharacterized RGB color spaces. |
| `Separation` | Operates on separation color spaces, spot colors. The specific separations to operate on are defined in the `@Separations` attribute. If `@Separations` is not defined, the operation will operate on all the separation color spaces in the input `RunList`. |
| `YUV` | Operates on YUV color spaces, also known as YCbCr. See `[BT.601-7]`. |

#### A.2.41.1 Source color space mapping

The following table summarizes how the color spaces in Table A.41, “SourceColorSpace,” SHALL be mapped to and from different file formats.

**Table A.42: Mapping of SourceColorSpace enumerations to color spaces in the most common input file formats**

| SourceCS | File Format | Color Space |
|---|---|---|
| `Calibrated` | PDFᵃ | `CalGray`, `CalRGB` |
| `Calibrated` | PostScriptᵃ | n/a |
| `Calibrated` | TIFF | n/a |
| `CIEBased` | PDFᵃ | n/a |
| `CIEBased` | PostScriptᵃ | `CIEBasedABC`, `CIEBasedA`, `CIEBasedDEF`, and `CIEBasedDEFG` |
| `CIEBased` | TIFF | n/a |
| `CMYK` | PDFᵃ | `DeviceCMYK`ᵇ<br>PDF `ICCBased` color spaces with ICC CMYK profiles.<br>`CIEBasedDEFG` spaces that resolve to a characterized CMYK space. |
| `CMYK` | PostScriptᵃ | `DeviceCMYK` |
| `CMYK` | TIFF | `PhotometricInterp = 5`, `Samples per pixel = 4` |
| `DeviceCMYK` | PDFᵃ | `DeviceCMYK`ᵇ |
| `DeviceCMYK` | PostScriptᵃ | `DeviceCMYK` |
| `DeviceCMYK` | TIFF | `PhotometricInterp = 5`, `Samples per pixel = 4` |
| `DeviceGray` | PDFᵃ | `DeviceGray`ᵇ |
| `DeviceGray` | PostScriptᵃ | `DeviceGray` |
| `DeviceGray` | TIFF | `PhotometricInterp = 0 or 1` |
| `DeviceN` | PDFᵃ | `DeviceN` |
| `DeviceN` | PostScriptᵃ | `DeviceN` |
| `DeviceN` | TIFF | `PhotometricInterp = 5`, `Samples per pixel = N` |
| `DeviceRGB` | PDFᵃ | `DeviceRGB`ᵇ |
| `DeviceRGB` | PostScript | `DeviceRGB` |
| `DeviceRGB` | TIFF | `PhotometricInterp = 2` |
| `Gray` | PDFᵃ | `DeviceGray`ᵇ<br>PDF `ICCBased` color spaces with ICC gray profiles.<br>`CIEBasedA` spaces that resolve to a characterized gray space. |
| `Gray` | PostScriptᵃ | `DeviceGray` |
| `Gray` | TIFF | `PhotometricInterp = 0 or 1` |
| `ICCBased`<br>`ICCCMYK`<br>`ICCGray`<br>`ICCLAB`<br>`ICCRGB` | PDFᵃ | `ICCBased`<br>`DeviceGray`ᵇ, `DeviceCMYK`ᵇ, `DeviceRGB`ᵇ |
| `ICCBased`<br>`ICCCMYK`<br>`ICCGray`<br>`ICCLAB`<br>`ICCRGB` | PostScriptᵃ | n/a |
| `ICCBased`<br>`ICCCMYK`<br>`ICCGray`<br>`ICCLAB`<br>`ICCRGB` | PostScript/EPS | The EPS file has an embedded ICC profile. |
| `ICCBased`<br>`ICCCMYK`<br>`ICCGray`<br>`ICCLAB`<br>`ICCRGB` | TIFF | The TIFF file has an embedded ICC profile. |
| `Lab` | PDFᵃ | `Lab` |
| `Lab` | PostScriptᵃ | n/a |
| `Lab` | TIFF | `PhotometricInterp = 8` — CIELAB 1976 “normal” encoding — or `PhotometricInterp = 9` — CIELAB 1976 using ICC profile v2 encoding. |
| `RGB` | PDFᵃ | `DeviceRGB`ᵇ<br>PDF `ICCBased` color spaces with ICC RGB profiles.<br>`CIEBasedDEF` spaces that resolve to a characterized RGB space. |
| `RGB` | PostScript | `DeviceRGB` |
| `RGB` | TIFF | `PhotometricInterp = 2` |
| `Separation` | PDFᵃ | `Separation` |
| `Separation` | PostScriptᵃ | `Separation` |
| `Separation` | TIFF | `PhotometricInterp = 5`. Applies only to one of the planes in the separated image. |
| `YUV` | PDFᵃ | n/a |
| `YUV` | PostScriptᵃ | n/a |
| `YUV` | TIFF | `PhotometricInterp = 6` |

ᵃ Where a Pattern or Indexed color space has been used in the PDL, the base color space is used to determine whether to apply this operation.

ᵇ In PDF, `DeviceCMYK`, `DeviceRGB`, and `DeviceGray` source color spaces can be characterized through providing a `DefaultCMYK`, `DefaultRGB`, or `DefaultGray` resource specifying a profile to be associated with source objects in that color space. In such cases, the resulting color space is considered characterized by XJDF operations.

### A.2.42 SourceObjects

`SourceObjects` specifies the class of a graphical object. Multiple tokens specify that the action filtered by `SourceObjects` applies to all of the listed classes.

**Table A.43: SourceObjects Enumeration Values**

| Value | Description |
|---|---|
| `ImagePhotographic` | Contone images. |
| `ImageScreenShot` | Images largely comprised of rasterized vector art. |
| `LineArt` | Vector objects other than text. |
| `SmoothShades` | Gradients and blends. |
| `Text` | Text objects. |

### A.2.43 SpreadType

*New in XJDF 2.1.*

`SpreadType` specifies how individual pages in a PDF SHALL be treated for use in an imposition.

**Table A.44: SpreadType Enumeration Values**

| Value | Description |
|---|---|
| `SinglePage` | The content of each page SHALL be imaged in a single cell in imposition. Each Finished Page SHALL be counted as an individual page. For instance, a booklet cover would have four pages. |
| `Spread` | The content of each page SHALL be imaged as a single surface onto the Final Product. Examples include wraparound covers. `Spread` SHOULD NOT be provided for adjacent pages that are not imaged onto the same surface. Each surface of the spread SHALL be counted as an individual page. For instance, a wraparound cover would have two pages. |

### A.2.44 StapleShape

`StapleShape` specifies the required shape of the finished staple used for Stitching.

**Table A.45: StapleShape Enumeration Values**

| Value | Description |
|---|---|
| `Butted` | — |
| `ClinchOut` | — |
| `Crown` | — |
| `Eyelet` | — |
| `Overlap` | — |

### A.2.45 Status

`Status` specifies the state of a process or queue entry that is required to execute a given task.

**Table A.46: Status Enumeration Values**

| Value | Description |
|---|---|
| `Aborted` | Indicates that the process executing the XJDF has been aborted, which means that execution will not be resumed again. |
| `Cleanup` | The process represented by this node is currently being cleaned up. |
| `Completed` | Indicates that the node or queue entry has been executed correctly, and is finished. |
| `InProgress` | The node is currently executing. |
| `Setup` | The process represented by this node is currently being set up. |
| `Stopped` | Execution has been stopped. If a job is `Stopped`, running can be resumed later. This status can indicate a break, a pause, maintenance, or a breakdown — in short, any pause that does not lead to the job being removed from the Device. |
| `Suspended` | Execution has been stopped. If a job is `Suspended`, running will be resumed later. Unlike `Stopped`, this status indicates that the job is no longer blocking resources on the Device, and other jobs may be run on the Device. For instance, a job that has been ripped on a DFE and is waiting for the marker is `Suspended`. When resumed, the job MAY go into `@Status="Setup"` before changing to `InProgress` again. The value `Suspended` is also used to describe iterations. In an iterative environment, `Suspended` specifies that at least one iteration cycle has completed but additional iteration cycles MAY still occur. In this case, `@StatusDetails` SHOULD be set to `IterationPaused`. |
| `Waiting` | The node can be executed. |

### A.2.46 TightBacking

`TightBacking` specifies the required geometry for the back of a book block.

This table is not ordered alphabetically; it is ordered by the pressure required, lowest first.

**Table A.47: TightBacking Enumeration Values**

| Value | Description | Book Form |
|---|---|---|
| `Round` | Rounding way. | — |
| `RoundBacked` | Rounding way, backing way. | — |
| `Flat` | A flat backing — no tight backing is applied. | — |
| `FlatBacked` | Backing way. | — |

### A.2.47 UpdateGranularity

`UpdateGranularity` specifies which `QueueEntry` items in a Queue SHALL be included in any action.

**Table A.48: UpdateGranularity Enumeration Values**

| Value | Description |
|---|---|
| `All` | All `QueueEntry` elements SHOULD be included. |
| `ChangesOnly` | Only those `QueueEntry` elements that have new information since the action SHOULD be included. |

### A.2.48 Usage

`Usage` specifies how a resource SHALL be used by a process.

**Table A.49: Usage Enumeration Values**

| Value | Description |
|---|---|
| `Input` | The resource SHALL be used as an input. |
| `Output` | The resource SHALL be used as an output. |

### A.2.49 WorkingDirection

`WorkingDirection` specifies the direction of an action or of the application of a resource.

**Table A.50: WorkingDirection Enumeration Values**

| Value | Description |
|---|---|
| `Bottom` | From below. |
| `Top` | From above. |

### A.2.50 WorkStyle

`WorkStyle` specifies the style of working in a sheet-fed press. It is defined in the press coordinate system, where the sheet moves parallel to the Y axis. In the simple case of a single unrotated page per surface, this implies that a flip around the Y-axis, `WorkAndTurn` or `WorkAndBack`, will result in head-to-head images for the back side, whereas a flip around the X-axis, `WorkAndTumble`, will result in head-to-foot images.

**Table A.51: WorkStyle Enumeration Values**

| Value | Description | Turn Axis | Plate Reuse |
|---|---|---|---|
| `Perfecting` | This work style describes printing on both sides of the substrate using a separate set of plates for each side. The front lay is altered by flipping the sheet along the X-axis and thus retaining the side lays. Perfecting is geometrically very similar to `WorkAndTumble`. Perfecting is most commonly used with in-press perfecting units, but the sheets can also be flipped outside of the press. The name `Perfecting` was chosen mainly for backwards compatibility. | X-Axis | No |
| `Simplex` | This work style describes a single press run with no turning of the press sheet. | None | No |
| `WorkAndBack` | This work style describes printing on both sides of the substrate using different plate sets for each surface. After the first press run, the side lays are altered by flipping the sheet along the Y-axis and thus retaining the front lays prior to the second press run. `WorkAndBack` is geometrically very similar to `WorkAndTurn`. | Y-Axis | No |
| `WorkAndTumble` | This work style describes printing on both sides of the substrate using the same plate set for both surfaces. After the first press run, the side lays are altered by flipping the sheet along the X-axis and thus retaining the side lays prior to the second press run. This work style may also be used for perfecting. `WorkAndTumble` SHOULD NOT be specified for digital printing. | X-Axis | Yes |
| `WorkAndTurn` | This work style describes printing on both sides of the substrate using the same plate set for both surfaces. After the first press run, the side lays are altered by flipping the sheet along the Y-axis and thus retaining the front lays prior to the second press run. `WorkAndTurn` SHOULD NOT be specified for digital printing. | Y-Axis | Yes |

### A.2.51 XJDFXJMFVersion

`XJDFXJMFVersion` specifies the version of an XJDF or XJMF instance.

**Table A.52: XJDFXJMFVersion Enumeration Values**

| Value | Description |
|---|---|
| `2.0` | XJDF 2.0. |
| `2.1` *(New in XJDF 2.1)* | XJDF 2.1. |
| `2.2` *(New in XJDF 2.2)* | XJDF 2.2. |

---

## A.3 Preferred NMTOKEN Values

This section contains the preferred values for items of `NMTOKEN`. Although these types are open lists, the values in these tables SHOULD be used where possible.

If an ICS requires new `NMTOKEN` values, or a work group has agreed upon new recommended `NMTOKEN` values, these will be published at `[CIP4Names]` prior to being added to the specification and SHOULD be used where appropriate.

### A.3.1 Comb and Coil Shapes

When specifying the shape of a comb or coil for `LooseBinding`, values from the following table are recommended.

**Table A.53: Comb and Coil Shapes**

| Value | Description |
|---|---|
| `Single` | Each “tooth” is made with one wire. |
| `SingleCalendar` | Each “tooth” is made with one wire and an extension for hanging the bound product is provided in the center. |
| `Twin` | The shape of each “tooth” is made with a double wire, for example Wire-O®. |
| `TwinCalendar` | The shape of each “tooth” is made with a double wire and an extension for hanging the bound product is provided in the center. |

### A.3.2 Contact Types

*Modified in XJDF 2.2.*

When specifying the role of a contact, values from the following table are recommended.

Contact types are typically used for either customers or print providers; this is indicated in the `Usage` column.

Modification note: The introduction has been revised, and the `Usage` column has been added to Table A.54, “Contact Types.”

**Table A.54: Contact Types**

| Value | Usage | Description |
|---|---|---|
| `Accounting` | Customer | Contact information that relates to the invoice. |
| `Administrator` | Customer | Person to contact for queries concerning the execution of the job. An administrator can also be the person that has extra rights to set up or control rights for other users, for example a web approval system. |
| `Agency` | Customer | The contact is an employee of an agency. |
| `Approver` | Customer | The person who approves the job. |
| `ArtDelivery` | Customer | Delivery contact for artwork of the job. |
| `ArtReturn` | Customer | Return delivery contact for artwork of this job. |
| `Author` | Customer | — |
| `Customer` | Customer | The end customer. |
| `Delivery` | Customer | The delivery address for all products of the job. |
| `DeliveryCharge` | Customer | The contact who is charged for delivery of the job. |
| `Designer` | Customer | — |
| `Editor` | Customer | — |
| `Employee` | Print Provider | Employee who works for the company processing the job. |
| `Illustrator` | Customer | — |
| `Owner` | Print Provider | The owner of a resource. |
| `Photographer` | Customer | — |
| `Recipient` | Customer, Print Provider | The contact is a recipient of a variable data record. |
| `Sender` | Customer | The source address of a delivery. |
| `SenderAlias` | Print Provider | The sender address that SHALL be printed on a delivery to an end customer. This allows a company that has contracted out the work to hide the subcontractor. |
| `TelephoneSanitizer` | Customer, Print Provider | — |

### A.3.3 Content Types

When specifying the type of content required or delivered, values from the following table are recommended.

**Table A.55: Content Types**

| Value | Description |
|---|---|
| `Ad` | A single advertisement. |
| `Article` | A single article, including headers, text bodies, photos, etc. |
| `Barcode` | A barcode. |
| `Composed` | A combination of elements that define an element that is not bound to a document page. |
| `Editorial` | An element that contains editorial matter, for example text, photographs, etc. |
| `Graphic` | An element that contains line art. |
| `IdentificationField` | A general identification field excluding barcodes. |
| `Image` | A bitmap image. |
| `Page` | A representation of one document page. |
| `Surface` | A representation of an imposed surface. |
| `Text` | Formatted or unformatted text. |

### A.3.4 Delivery Methods

Delivery methods specify the recommended values for requesting how items SHALL be delivered.

**Table A.56: Delivery Methods**

| Value | Description |
|---|---|
| `BestWay` | The sender decides how to deliver. |
| `CompanyTruck` | The sender uses their own vehicles to deliver. |
| `Courier` | The sender uses an independent third party to deliver. |
| `CourierNoSignature` | A delivery service that does not require receipt stamps at the recipient’s mailbox and/or mail room. This value is compatible with the commonly used Japanese “Mail Bin” delivery service. |
| `Email` | The sender uses email to deliver electronic items. |
| `ExpressMail` | The sender uses an express mail service to deliver. |
| `ExpressShipping` | Guaranteed delivery, faster than `StandardShipping`, optimized by time. |
| `Ground` | The sender uses a ground-based delivery system. |
| `InstantMessaging` | The sender uses instant messaging to deliver electronic items. |
| `InterofficeMail` | The sender uses their own internal mail network to deliver. |
| `Local` | The items are already in place; no other delivery process is required. |
| `NetworkCopy` | This includes LAN and VPN. |
| `StandardShipping` | Delivery according to the standard terms of service of the carrier, optimized by price. |
| `Storage` | The item is stored by the supplier. |
| `WebServer` | Upload/download from HTTP/FTP server. |

### A.3.5 Device Classes

CIP4 supports many Device classes. The following values SHOULD be used when filling `Device/@DeviceClass`.

**Table A.57: Device Classes**

| Value | Description |
|---|---|
| `BandingStation` | A Device that performs the `Wrapping` process with `MiscConsumable/@Type="PaperBand"`, `"PlasticBand"`, or `"RubberBand"` in counted quantity. |
| `CartonErector` | A Device that performs the `BoxPacking` process. A carton erector erects flat cartons and closes the bottom, ready to fill. Carton erectors may use glue, tape, or automatic-bottom. |
| `CartonLoader` | A Device that performs the `BoxPacking` process. A carton loader loads products into a carton or box. |
| `CartonSealer` | A Device that performs the `BoxPacking` process. A carton sealer seals or tapes a loaded carton or box. |
| `CaseMaker` | A Device that performs the `CaseMaking` process. A case maker produces the hard case for books. |
| `Controller` | A Controller is a Device that is a proxy for one or more individual Devices or Machines. |
| `Cutter` | A Device that performs the `Cutting` process. A cutter can be used either to cut sheet blocks from a sheet-fed press or to slit a ribbon from a web-fed press. |
| `DieCutter` | A Device that performs the `ShapeCutting` process. A die cutter can be used to cut shapes from printed sheet blocks, for example windows in envelopes. |
| `EndsheetFeeder` | A Device that performs the `Feeding` process. Specifically, an endsheet feeder adds end sheets to a cover prior to binding. |
| `FilmSetter` | A Device that performs the `ImageSetting` process. A film setter creates a printable image on film. |
| `Folder` | A Device that performs the `Folding` process. A folder can be used to fold the output from either sheet-fed or web-fed presses. |
| `FolderGluer` | A Device that performs the `BoxFolding` process. A box folder folds and glues blanks into folded boxes for packaging. |
| `Gatherer` | A Device that performs the `Gathering` process. A gatherer can be used to collect sheets into piles. |
| `GathererBinder` | A Device that performs the `Gathering` and `LooseBinding` processes. A gatherer binder can be used to collect sheets into collated piles that are then bound. |
| `HardCopyProofer` | A Device that provides a physical representation of the printed pages or sheets. |
| `Hardcover` | A Device that performs the `CaseMaking` process. This Device creates a hardcover. |
| `HardcoverBookLine` | A Device that combines multiple processes such as `BlockPreparation`, `CaseMaking`, `CasingIn`, `Collecting`, `CoverApplication`, `EndSheetGluing`, `Gathering`, `Gluing`, `HeadBandApplication`, `Jacketing`, `SpinePreparation`, and `SpineTaping` to create and apply a hardcover to a block that it creates from a set of pages. The Device may not support all of the shown processes depending upon its capabilities. |
| `HeatShrink` | A Device that performs the `Shrinking` process, for example a heat shrink tunnel. |
| `HolePuncher` | A Device that performs the `HoleMaking` process. A hole puncher can be used to stamp or drill a number of holes, usually in a block of pages. |
| `Inserter` | A Device that performs the `Inserting` process. An inserter can be used to insert a component within another component. |
| `IntegratedDigitalPrinter` | A Device that performs the `DigitalPrinting` process. Specifically, an integrated digital printer that has additional postpress capabilities, such as folding or binding. |
| `Jacketer` | A Device that performs the `Jacketing` process. A jacketer wraps a bound book with a folded jacket. |
| `LabelPrinter` | A Device that prints and attaches labels to a Component. |
| `MultipleWebConventionalPress` | A Device that performs the `ConventionalPrinting` process, specifically on a multiple web conventional press. |
| `Palletizer` | A Device that performs the `Palletizing` process by placing products or bundles onto a pallet. |
| `PerfectBinderLine` | A Device that combines multiple processes such as `BlockPreparation`, `CaseMaking`, `CasingIn`, `Collecting`, `CoverApplication`, `EndSheetGluing`, `Gathering`, `Gluing`, `HeadBandApplication`, `Jacketing`, `SpinePreparation`, and `SpineTaping` to create perfect bound books. |
| `PlateSetter` | A Device that performs the `ImageSetting` process. A plate setter creates a printable image on a plate suitable for conventional printing. |
| `PrintingPress` | A Device that performs the `ConventionalPrinting` or `DigitalPrinting` process. Any type of printing press. |
| `Scanner` | A Device that performs the `ManualLabor` process. A scanner is used to describe the manual process of producing machine-readable image data from pre-printed documents. |
| `SheetFedConventionalPress` | A Device that performs the `ConventionalPrinting` process. A standard sheet-fed conventional press. |
| `SheetFedDigitalPrinter` | A Device that performs the `DigitalPrinting` process. A standard sheet-fed digital press. |
| `ShrinkWrapper` | A Device that performs the `Wrapping` and `Shrinking` process with shrink wrap foil. Various bundles can be shrink wrapped, for example individual products, boxes, or even pallets. |
| `SingleWebConventionalPress` | A Device that performs the `ConventionalPrinting` process, specifically a single web conventional press. |
| `SoftCopyProofer` | A Device that provides an on-screen representation of the printed pages or sheets. |
| `Stacker` | A Device that performs the `Stacking` process. A stacker can be used to create a pile or bundle of components suitable for delivery. |
| `Stitcher` | A Device that performs the `Stitching` process. A stitcher can be used to stitch a number of sheets together into a block and may also add a cover. |
| `ThreadSewer` | A Device that performs the `ThreadSewing` process. A thread sewer can be used to sew a number of sheets together into a block. |
| `Trimmer` | A Device that performs the `Trimming` process. A trimmer can be used to reduce a block to the required size, for example for subsequent hardcover binding. |
| `VirtualPrinter` | A value of `VirtualPrinter` should be provided if a physical Machine is represented as multiple Devices. |
| `WebDigitalPrinter` | A Device that performs the `DigitalPrinting` process, specifically a single web digital press. Modification note: From XJDF 2.1, the value changed from `WebDigitalprinter`. |
| `WeighingStation` | A Device that weighs products, for example for postage calculations. |
| `WideFormatPrinter` | A Device that performs the `DigitalPrinting` process, specifically a wide-format printer that can be used to create large printed products such as banners. |
| `WrappingStation` | A Device that performs the `Wrapping` process with `MiscConsumable/@Type="PaperWrap"`. |

### A.3.6 Employee Roles

Values of this type define the roles that are performed by an employee.

**Table A.58: Employee Roles**

| Value | Description |
|---|---|
| `Apprentice` | Employee that is in training. |
| `Assistant` | Assistant operator. |
| `Craftsman` | Trained employee. |
| `CSR` | Customer Service Representative. |
| `Manager` | Manager. |
| `Master` | Highly trained employee. |
| `Operator` | Operator. |
| `ShiftLeader` | The leader of the shift. |
| `StandBy` | Employee who is allocated to a specific task on demand. |

### A.3.7 Flute Types

Values of this type define the required flute type, size and frequency, for corrugated media.

Although the classification of flutes using a letter code, such as “A,” “B,” etc., is used very frequently, there seems to be no agreement on the exact numerical specification of those categories. Slightly varying numbers for flute size and frequency can be found between regions and between vendors.

See `[Corrugated Packaging]`.

**Table A.59: Flute Types**

| Value | Description |
|---|---|
| `A` | 33 ± 3 flutes/foot, 108 ± 10 flutes/meter. |
| `B` | 47 ± 3 flutes/foot, 154 ± 10 flutes/meter. |
| `C` | 39 ± 3 flutes/foot, 128 ± 10 flutes/meter. |
| `E` | 90 ± 4 flutes/foot, 295 ± 13 flutes/meter. |
| `F` | 125 ± 4 flutes/foot, 420 ± 13 flutes/meter. |

### A.3.8 Fold Catalog

The fold catalog describes a type of fold according to the folding catalog in Figure A-1. In case of any ambiguity, the folding notation SHALL take precedence over the graphic illustration.

The value format is: `Fn-i`, where:

- `n` is the number of Finished Pages.
- `i` is either an integer, which identifies a particular fold, or the letter `X`, which identifies a generic fold.

Examples:

- `F6-2` describes a Z-fold of 6 Finished Pages.
- `F6-X` describes a generic fold with 6 Finished Pages.

**Figure A-1: Fold catalog**

> Image description: A multi-page catalog of schematic fold diagrams. The figure shows fold codes such as `F2-1`, `F4-1`, `F6-1` through `F64-2`, with simplified drawings of folded sheets, fold order numbers, fractional fold positions, fold direction indicators, and a legend. The final sheet includes an example for `F32-3`, explaining fold-up, fold-down, fold direction changes, and sheet format orientation.

### A.3.9 Ink and Varnish Coatings

When specifying coating types, such as ink or varnish, values from the following table are recommended.

**Table A.60: Ink and Varnish Coatings**

| Value | Description |
|---|---|
| `Aqueous` | Water-based coating. |
| `Bronzing` | Printing an adhesive that is then immediately dusted with a bronze or other metallic powder that adheres to the adhesive. |
| `Gloss` | A glossy coating. |
| `Ink` | Any generic ink. |
| `InkJet` | Ink. |
| `Latex` | Liquid that is similar to ink. |
| `Matte` | A matte coating. |
| `Primer` | A coating that is applied beneath the image. |
| `Relief` | Property of the coating. |
| `RubResistant` | Attribute of the ink. |
| `Satin` | A coating between `Gloss` and `Matte`. |
| `Silicone` | Liquid that is similar to ink. |
| `Toner` | Liquid that is similar to ink. |
| `UV` | Ultraviolet-cured polymers. |
| `Varnish` | Unpigmented ink. |
| `WaterResistant` | Attribute of the ink. |

### A.3.10 Input Tray and Output Bin Names

`Part/@Location` MAY be used to specify a location within a Device, for example a paper tray. When specifying input paper trays, indicated with `I`, and/or output bins, indicated with `O`, the following values for `Part/@Location` SHOULD be used.

**Table A.61: Input Tray and Output Bin Names**

| Value | I/O | Description |
|---|---|---|
| `AnyLargeFormat` | I/O | The location that holds larger format media with one dimension larger than 11 inches. The media dimensions SHALL be specified. |
| `AnySmallFormat` | I/O | The location that holds smaller format media. The media dimensions SHALL be specified. |
| `AutoSelect` | I/O | The location that the Device selects based on the Media specification. |
| `Booklet` | O | The bin where the Device places booklets. |
| `Bottom` | I/O | The location that, when facing the Device, can best be identified as “bottom.” |
| `BypassTray` | I | The input tray used to handle odd or special papers. MAY be used to specify the input tray that is used for insert sheets that SHALL NOT be imaged. |
| `BypassTray-N` | I | The input tray used to handle odd or special papers. MAY be used to specify the input tray that is used for insert sheets that SHALL NOT be imaged. `N = 1, 2, ...` |
| `Continuous` | I/O | The location to handle continuous media, i.e. continuously connected sheets. |
| `Disc` | I/O | The location to handle CD or DVD discs to be printed on. |
| `Disc-N` | I/O | The location to handle CD or DVD discs to be printed on. `N = 1, 2, ...` |
| `Envelope` | I/O | The location to handle envelopes. |
| `Envelope-N` | I/O | The location to handle envelopes. `N = 1, 2, ...` |
| `Front` | I/O | The location that, when facing the Device, can best be identified as “front.” |
| `InsertTray` | I | The input tray that can best be identified as “insert tray.” Used to specify the input tray that is used for insert sheets. Insert sheets are never imaged. |
| `InsertTray-N` | I | The input tray that can best be identified as “insert tray-1,” “insert tray-2,” etc. Used to specify the input tray that is used for insert sheets. Insert sheets are never imaged. |
| `LargeCapacity` | I/O | The location that can best be identified as the “large capacity” location, in terms of the number of sheets, with respect to the Device. |
| `LargeCapacity-N` | I/O | The location that can best be identified as the “large-capacity-1,” “large-capacity-2,” etc., input or output location, in terms of the number of sheets, with respect to the Device. |
| `Left` | I/O | The location that, when facing the Device, can best be identified as “left.” |
| `Mailbox-N` | O | The output location that is best identified as “Mailbox #1,” “Mailbox #2,” etc. |
| `Middle` | I/O | The location that, when facing the Device, can best be identified as “middle.” |
| `PostMarkerInserter` | I | The input tray that is downstream of the marking engine and allows the user to pass media through a non-marking paper path for covers and/or inserts. |
| `Rear` | I/O | The location that, when facing the Device, can best be identified as “rear.” |
| `Right` | I/O | The location that, when facing the Device, can best be identified as “right.” |
| `Roll` | I/O | The location to handle web-fed media. |
| `Roll-N` | I/O | The Nth location to handle the Nth web-fed media. |
| `Side` | I/O | The location that, when facing the Device, can best be identified as “side.” |
| `Stacker-N` | O | The output location that is best identified as “Stacker #1,” “Stacker #2,” etc. |
| `Top` | I/O | The location that, when facing the Device, can best be identified as “top.” |

### A.3.11 MediaType Details

MediaType Details specifies additional details of the media to be used.

**Table A.62: MediaType Details**

| Value | Description |
|---|---|
| `Aluminum` | Conventional or CTP press plate. |
| `Backlit` | Any media that is designed to be illuminated from the back side. |
| `Blueback` | Blueback poster paper is a specialist printing paper with a white printing surface and an opaque blue back that avoids any show-through from the posters underneath. |
| `Cardboard` | — |
| `CD` | CD disc to be printed on. |
| `Cloth` | Cloth, for example for a hardcover book case. |
| `Continuous` | Continuously connected sheets of an opaque material. The edge that is connected is not specified. |
| `ContinuousLong` | Continuously connected sheets of an opaque material connected along the long edge. |
| `ContinuousShort` | Continuously connected sheets of an opaque material connected along the short edge. |
| `DoubleWall` | Double-wall corrugated board. |
| `DryFilm` | — |
| `DVD` | DVD disc to be printed on. |
| `Envelope` | Envelopes that can be used for conventional mailing purposes. |
| `EnvelopePlain` | Envelopes that are not preprinted and have no windows. |
| `EnvelopeWindow` | Envelopes that have windows for addressing purposes. |
| `EnvelopeWindowLeft` | Envelopes that have windows on the left for addressing purposes. |
| `EnvelopeWindowRight` | Envelopes that have windows on the right for addressing purposes. |
| `FlexoBase` | For the base layer of flexo plates. |
| `FlexoPhotoPolymer` | For the photopolymer layer of flexo plates. |
| `Flute` | Flute layer of a corrugated board. |
| `FullCutTabs` | Media with a tab that runs the full length of the medium so that only one tab is visible extending out beyond the edge of non-tabbed media. |
| `ImageSetterPaper` | Contact paper as replacement for film. |
| `Labels` | Label stock, for example a sheet of peel-off labels. |
| `Leather` | Leather stock, for example for a hardcover book case. |
| `Letterhead` | Separately cut sheets of an opaque material including a letterhead. |
| `MultiLayer` | Form medium composed of multiple layers that are attached to one another, for example for use with impact printers. |
| `MultiPartForm` | Form medium composed of multiple layers not attached to one another; each sheet might be drawn separately from an input source. |
| `Photographic` | Separately cut sheets of an opaque material to produce photographic quality images. |
| `Polyester` | Conventional or CTP press plate. |
| `PreCutTabs` | Media with tabs that are cut so that more than one tab is visible extending out beyond the edge of non-tabbed media. |
| `ScrimBanner` | Specific type of vinyl. Use with `@MediaType="Synthetic"`. |
| `SingleFace` | Single-face corrugated board. |
| `SingleWall` | Single-wall corrugated board. |
| `Stationery` | Separately cut sheets of an opaque material; includes generic paper. |
| `TabStock` | Media with tabs, either precut or full-cut. |
| `Tractor` | Tractor feed with holes. |
| `TripleWall` | Triple-wall corrugated board. |
| `Vinyl` | Specific type of synthetic media. Use with `@MediaType="Synthetic"`. |
| `WallPaper` | Details of wallpaper. |
| `WetFilm` | Conventional photographic film. |

### A.3.12 Milestones

The following table defines a list of values that are valid for `QueryNotification/NotificationFilter/@MilestoneTypes` and `Milestone/@MilestoneType`.

Milestones usually refer to events involving multiple objects, although `Milestone/@MilestoneType` is specified as a singular. The scope of the Milestone is defined by the parent Notification element.

**Table A.63: Milestones**

| Value | Description |
|---|---|
| `BindingCompleted` | All binding worksteps, including packing of the job, have been completed. Postpress worksteps are defined according to Section 5.6, “Postpress Processes.” |
| `BindingInProgress` | At least one of the binding worksteps of the job is in progress. |
| `Delivered` | The files were delivered to the destination. |
| `DigitalArtArrived` | Digital content has been received. |
| `JobCompletedSuccessfully` | Job completed successfully. |
| `JobCompletedWithErrors` | Job completed with errors. |
| `JobCompletedWithWarnings` | Job completed with warnings. |
| `PageApproved` | Planned page proofs have been approved. |
| `PageCompleted` | Pages are ready; no further page processing or page proofing is required. |
| `PageProofed` | Planned page proofs have been made. |
| `PostPressCompleted` | All postpress worksteps, including packing of the job, have been completed. Postpress worksteps are defined according to Section 5.6, “Postpress Processes.” |
| `PostPressInProgress` | At least one of the postpress worksteps of the job is in progress. |
| `PrePressCompleted` | All prepress worksteps of the job have been completed. Prepress worksteps are defined according to Section 5.4, “Prepress Processes.” In conventional prepress, this is the case when all plates have been made. |
| `PrePressInProgress` | At least one of the prepress worksteps of the job is in progress. |
| `PressCompleted` | All press worksteps of the job have been completed. Press worksteps are defined according to Section 5.5, “Press Processes.” |
| `PressInProgress` | At least one of the press worksteps of the job is in progress. |
| `ProofSent` | Planned proofs sent to customer. |
| `ShippingCompleted` | Final Product was delivered to the customer or distributors. |
| `ShippingInProgress` | Final Product is being shipped. |
| `SurfaceApproved` | Planned imposition proofs have been approved. |
| `SurfaceAssigned` | Surfaces have their corresponding pages assigned; for example, they could be proofed. |
| `SurfaceCompleted` | Planned surfaces are ready; for example, plates could be made. |
| `SurfaceProofed` | Planned imposition proofs have been made. |

### A.3.13 Module Types

#### Table A.64: Module Types for Conventional Printing

| Value | Description |
|---|---|
| `CoatingModule` | Unit for coatings, for example full coating of varnish. |
| `Delivery` | Delivery module, unit for gathering the printed sheets. |
| `Drier` | Module for drying the previously printed color or varnish. |
| `ExtensionModule` | Unit for extending the distance between modules, for example to increase the distance between the last printing module and the delivery module. |
| `Feeder` | Feeder module; feeds the Device with paper. |
| `Imaging` | Imaging module in a direct-to-plate Machine. |
| `Numbering` | Numbering unit. |
| `PerfectingModule` | Unit for perfecting, reversing Device. |
| `PrintModule` | Unit for printing a color. Describes one cylinder and one side. |

#### Table A.65: Module Types for Postpress

| Value | Description |
|---|---|
| `BlockPreparer` | The block preparer prepares the book block for a hardcover book. See Section 5.6.1, “BlockPreparation.” |
| `BoxFolder` | The box folder folds and glues blanks into folded boxes for packaging. See Section 5.6.2, “BoxFolding.” |
| `CaseMaker` | The case maker produces the hard case for books. See Section 5.6.5, “CaseMaking.” |
| `Caser` | The caser joins the hardcover book case and the book block. See Section 5.6.6, “CasingIn.” |
| `Chain` | The transport chain or conveyor to transport gathered/collected product. |
| `EndSheetGluer` | The end sheet gluer merges the front end sheet, the book block, and the back end sheet together. See Section 5.6.13, “EndSheetGluing.” |
| `Feeder` | The feeder module feeds the Device with paper. See Section 5.6.14, “Feeding.” |
| `Gluer` | The gluer applies glue to a component. See Section 5.6.17, “Gluing.” |
| `HeadBandApplicator` | The head band applicator applies a head band to the book block. See Section 5.6.18, “HeadBandApplication.” |
| `InkJetPrinter` | The printer that uses inkjet technology to print images or text on a component. See Section 5.5.2, “DigitalPrinting.” |
| `Inserter` | The inserter inserts one or more “child” components into one “mother” component. See Section 5.6.20, “Inserting.” |
| `Jacketer` | The jacketer wraps a jacket around a book. See Section 5.6.21, “Jacketing.” |
| `PaperPath` | The paper path module; path that paper follows through the Machine. |
| `PressingStation` | The pressing station presses the cover to the book block. |
| `ShapeCutter` | The shape cutter produces special shapes like an envelope window or a heart-shaped beer mat. The shape cutter module may contain tools that correspond to the actual dies. See Section 5.6.27, “ShapeCutting.” |
| `SpinePreparer` | The spine preparer prepares the spine of a book for hard and softcover production. See Section 5.6.29, “SpinePreparation.” |
| `SpineTaper` | The spine taper applies a tape strip to the spine of a book block. See Section 5.6.30, “SpineTaping.” |
| `Strapper` | The strapper straps a bundle of products. See Section 5.6.33, “Strapping.” |
| `ThreadSealer` | The thread sealer sews and seals a signature at the spine. See Section 5.6.34, “ThreadSealing.” |
| `ThreadSewer` | The thread sewer sews all signatures of a book block together. See Section 5.6.35, “ThreadSewing.” |

#### Table A.66: Module Types for Digital Printing

| Value | Description |
|---|---|
| `FarmPrinter` | Individual printer in a printer farm of printers. |
| `Fuser` | Fuser module — fuses the toner onto the media. |
| `Marker` | Marker module, excluding in-line finishing. |
| `ReferencedDataCollector` | Module that fetches data referenced from the XJDF and MAY include data referenced from the PDL. Does not include accepting a zip, unpacking a zip, or fetching the XJDF itself. |
| `RIP` | Raster image processor module. |
| `Unpacker` | Module that receives and unpacks the zip package and fetches the XJDF if it is referenced from the XJMF. |

#### Table A.67: Module Types for Web Printing

| Value | Description |
|---|---|
| `ChillUnit` | Chill unit that chills the heated printed paper. |
| `ImprintUnit` | Printing unit that allows changing plates during a production run, doing imprints. |
| `PrintUnit` | A print unit consists of multiple print module units. |
| `RemoisteningModule` | Module that can be used for high gloss varnish, re-moistened glue, rub-off ink, or encapsulated fragrances. The re-moistening module is located between the last printing unit and the dryer. |
| `UVCoater` | The UV coater module applies UV varnish with subsequent drying in a UV dryer. |

#### Table A.68: Module Types for Web Finishing

| Value | Description |
|---|---|
| `CrossCutter` | Cuts the web/ribbon n-times into sheets and transports the sheets to inline postpress equipment. |
| `Delivery` | Delivers the printed and/or folded sheets out of the folder. |
| `Folder` | Module for cutting the collected ribbons into sheets, in some cases collecting these sheets, and folding the sheets, including quarter and cross folds. |
| `Former` | Module for gathering ribbons and in most instances doing the first fold of the ribbons, quarter fold. |
| `GluingAndSofteningModule` | Consists of multiple heads, spread out in the press, for gluing and/or softening of ribbons or folded sheets. |
| `MoebiusDeinfinitizer` | Used to resolve the infinite loops caused by printing on interleaving surfaces of Möbius-banded webs. |
| `PerforatingModule` | Module for doing cross, longitudinal, or diagonal perforations and die cuts on a web. The module is placed between the chill unit and folder. |
| `PlanoModule` | The plano module cuts the web/ribbon into sheets and stacks the sheets into a pile. |
| `PloughFoldModule` | The plough fold module does a quarter fold to ribbons or webs and is mostly found in front of a folder module. |
| `Rewinder` | Rewinds the printed web to a roll. |
| `RibbonCompensator` | Controls the web’s or ribbon’s running direction regarding the cross cut. |
| `Slitter` | Module for cutting in the Machine direction. |
| `Stitcher` | Stitches folded sheets together. |
| `Superstructure` | Module in which a web will be cut into ribbons that will then be moved to the correct position for folding. |
| `TurnerBar` | Turns the front side of a web to the back side and vice versa. |
| `TurnerBarUnit` | Turns the front side of a web to the back side and vice versa in a separate unit. |

#### Table A.69: Module Types for Packing

| Value | Description |
|---|---|
| `BundlingModule` | The bundling module is used for bundling components. See Section 5.6.4, “Bundling.” |
| `LabelingModule` | The labeling module is used for labeling a bundle. See Section 5.6.22, “Labeling.” |
| `PalletizingModule` | The palletizing module collects the bundles on a pallet. See Section 5.6.25, “Palletizing.” |
| `Stacker` | The stacker module stacks the component into a pile. See Section 5.6.31, “Stacking.” |
| `Trimmer` | The trimmer module trims the component to its final size. See Section 5.6.36, “Trimming.” |

### A.3.14 Node Categories

Node categories are used to indicate the general purpose of an XJDF.

**Table A.70: Node Categories**

| Value | Description |
|---|---|
| `Binding` | Binding of a bound product. |
| `Cutting` | Specifies cutting of a Component. |
| `DigitalPrinting` | A RIP and print run on a digital printer that produces final output. |
| `FinalImaging` | A RIP and image that produces final output that is ready for further processing, for example film or plates. |
| `FinalRIPing` | A RIP process for generating final output. |
| `Folding` | Folding of a product. |
| `Newsprinting` | A press run on a news printing web press. |
| `PostPress` | General postpress. Includes `Folding` and `Binding`. |
| `PrePress` | General prepress. |
| `Printing` | A press run that produces final output. |
| `ProofImaging` | A RIP that produces proof output. |
| `ProofRIPing` | A RIP process for generating a proof. The processes are identical to those specified for `FinalRIPing`. |
| `RIPing` | General RIP Gray Box. |
| `WebPrinting` | A press run on a web press can produce one or more components as output at the same time. A web printing press might be equipped with prepress and postpress equipment. |
| `WebToPrint` | A product description that describes a product order in a web shop. |

### A.3.15 Pallet Types

The following table defines a list of values that are valid for indicating the intended type of pallet to be used.

**Table A.71: Pallet Types**

| Value | Description |
|---|---|
| `2Way` | Two-way entry. |
| `4Way` | Four-way entry. |
| `Euro800x600` | 800 mm × 600 mm. See `[DIN 15146-4]`; equals half Euro pallet. |
| `Euro800x1200` | 800 mm × 1200 mm. See `[DIN EN 13698-1]`; equals Euro pallet. |
| `Euro1000x1200` | 1000 mm × 1200 mm. See `[DIN EN 13698-2]`; flat pallet. |
| `Euro1200x1200` | 1200 mm × 1200 mm, no norm, but used in the field. |

### A.3.16 Printing Technologies

The following table defines a list of values that are valid for indicating the intended printing technology to be used.

**Table A.72: Printing Technologies**

| Value | Description |
|---|---|
| `DyeSublimation` | Digital printing using heat to transfer dye to the substrate. |
| `ElectroInk` | Digital printing with liquid toner. |
| `Electrophotography` | Electrophotographic printing with toner. |
| `Flexography` | Conventional printing using a flexible relief plate. |
| `InkJet` | Digital printing where individual droplets of ink are transferred directly to the substrate. |
| `Letterpress` | Conventional printing with traditional relief masters. |
| `OffsetLithography` | Conventional printing that uses an intermediate blanket between the plate and the substrate. |
| `Potato` | Unconventional printing using a carved potato as a print master. |
| `Rotogravure` | Conventional printing using an engraved circular cylinder. |
| `ScreenPrinting` | Conventional printing where the ink is transferred to the substrate via a mesh. |
| `Thermal` | Digital printing that uses heat directly on a matching thermal paper. |

### A.3.17 PrintStandard Characterization Data Sets

`PrintStandard` specifies the reference name of a characterization data set. There are research and trade associations, such as Fogra, IDEAlliance, WAN-IFRA, JPMA, and ICC, that provide characterization data sets for standard printing conditions.

Most reference names of standard printing conditions are registered with the ICC; see `[Characterization Data]`.

Official reference names SHALL be taken if a standard printing condition exists. Custom or Device-dependent reference names MAY be provided if no official standard printing condition is available.

In digital printing, `PrintStandard` will typically be used to specify the selected internal color model that defines the Device-specific use of colorants such as light cyan or additional gamut colors.

Whereas `PrintStandard` defines a media-independent characterization data set, `Part/@PrintCondition` defines a characterization data set that is applied to a specific setup, including paper selection and screening setup.

**Table A.73: PrintStandard Values**

| PrintStandard Name | Provider | Description |
|---|---|---|
| `CGATS21-2-CRPC5` | International Color Consortium | Valid for `CGATS21-2-CRPC5` based profiles such as `SWOP2013C3-CPRC5`. See `[CGATS.21]`. |
| `CGATS21-2-CRPC6` | International Color Consortium | Valid for `CGATS21-2-CRPC6` based profiles such as `SWOP2013C3-CPRC5`. See `[CGATS.21]`. |
| `FOGRA39` | FOGRA | Valid for `FOGRA39L` based profiles such as `ISO Coated v2 (ECI)` or `ISO Coated v2 300% (ECI)`. See `[FOGRA]`. |
| `FOGRA47` | FOGRA | Valid for `FOGRA47L` based profiles such as `PSO Uncoated ISO 12647 (ECI)`. See `[FOGRA]`. |
| `FOGRA51` | FOGRA | Valid for `FOGRA51` based profiles such as `PSO Coated v3`. See `[FOGRA]`. |
| `FOGRA52` | FOGRA | Valid for `FOGRA52` based profiles such as `PSO Uncoated v3`. See `[FOGRA]`. |
| `FOGRA53` | FOGRA | Valid for `FOGRA53` based profiles such as `eciCMYK`. See `[FOGRA]`. |

### A.3.18 Product Types

**Table A.74: Product Types**

| Value | Description |
|---|---|
| `BackCover` | The last page or sheet of a softcover book or magazine, commonly a heavier media. |
| `BlankBox` | Cut, unfolded box; input for folder-gluer. |
| `BlankSheet` | An unprinted divider page or sheet. Also describes die-cut unprinted label. |
| `BlankWeb` | A web with connected blanks after die cutting. |
| `Body` | Generic content inside of a cover, for example `BookBlock`. Also, in page assembly, the main text content, body copy, in contrast to headings or front matter. |
| `Book` | Body with a cover and a spine, either a `HardCoverBook` or a `SoftCoverBook`. |
| `BookBlock` | The assembled body of pages for a hardcover book. |
| `BookCase` | The assembled covers and spine component of a hardcover book, prior to casing in, i.e. attaching to the book block. |
| `Booklet` | Body with a cover without a spine, typically stapled. |
| `Box` | Convenience packaging that is not envisioned to be protection for shipping. |
| `Brochure` | A single folded sheet. |
| `BusinessCard` | A small card that displays contact information for an individual employed by a company. |
| `Carton` | Protection packaging for shipping. |
| `Cover` | A single sheet covering a side of a print product. |
| `CoverBoard` | A cover board used in hardcover book production. See `CaseMaking`. |
| `CoverLetter` | A letter accompanying another print product. |
| `EndSheet` | A glued sheet that spans and attaches `BookBlock` to `BookCase`, in both front and back of a hardcover book, printed or not. |
| `Envelope` | A folded paper container, with sealable flap, that encloses and protects a document or contents. |
| `FlatBox` | A folded and glued blank, not opened. Output from a box folder-gluer. |
| `FlatWork` | Non-bound, non-folded products or products that only have packaging folds. |
| `FrontCover` | The first page or sheet of a softcover book or magazine, commonly a heavier media. |
| `HardCoverBook` | A book bound with hard and rigid protective covers. |
| `Insert` | A product part intended to be inserted into a print product. |
| `Jacket` | Hardcover case jacket. |
| `Label` | A piece of paper or plastic that is attached to an object in order to give information about it. |
| `Leaflet` | A single unfolded sheet. |
| `Letter` | A written or printed communication addressed to a person or organization and usually transmitted by mail or messenger. |
| `Map` | A drawing/representation of a particular area such as a city or a continent, showing its main features as they would appear if viewed from above. |
| `Media` | Unprinted media, the substrate, usually paper, on which an image is to be printed. |
| `Newspaper` | A newspaper product. |
| `Notebook` | A book or block with a set of identical or similar pages, for example a writing tablet, where all page fronts have identical content and all page backs have identical content. |
| `Pallet` | Loaded pallet of boxes, cartons, or Component resources. |
| `Postcard` | A card designed for sending a message by mail without an envelope. |
| `Poster` | A large printed picture. |
| `Preprinted` | Preprinted media intended to be used as input to a printing process to allow that media to have additional printing. |
| `Proof` | A representation that visualizes the intended output of page assembly or the printing process. `Proof` SHOULD NOT be specified for a product as defined in Section 3.3.1, “Product.” |
| `ResponseCard` | A self-mailer to respond to an offer. |
| `Section` | Main division of a book, such as a chapter, typically with a name or number. |
| `SelfMailer` | A document to be sent via the post without an additional envelope. |
| `SoftCoverBook` | A book bound with thick paper or paperboard covers. |
| `Spine` | The bound edge of a book. Also, the portion of the cover that connects the front and back cover, wrapping the binding edge. |
| `SpineBoard` | A spine board used in hardcover book production. See `CaseMaking`. |
| `Stack` | Stacked Component. |
| `WrapAroundCover` | A single cover sheet containing the front cover, spine, and back cover. |

### A.3.19 Quality Control Methods

*New in XJDF 2.1.*

**Table A.75: Quality Control Methods**

| Value | Description |
|---|---|
| `Barcode` | A barcode quality test measures whether a printed barcode adheres to the technical requirements for a barcode of that type. |
| `BindingPull` | Binding quality test that measures the force required to pull out a bound page. |
| `BindingFlex` | Binding quality test that measures the number of times a page can be turned before the binding fails. |
| `ColorDensitometry` | Color quality test that measures the color density. |
| `ColorSpectrophotometry` | Color quality test that measures the color spectrum. |
| `Colorimetry` | Color quality test that measures the color metrics according to `[CIE 015:2004]`. |
| `Inspection` | Generic inspection of a given component. The result of an inspection is typically a list of defects. Inspection includes visual inspection by a human being. |
| `Registration` | A separation registration test measures the registration offset of color separations relative to a master separation. Front-to-back registration and any other registrations, such as finishing registration or image-to-sheet registration, are covered by `Inspection`. |
| `Structural` | A structural test that measures the structural stability of finished products such as boxes. |

### A.3.20 Spine Operations

**Table A.76: Spine Operations**

| Value | Description |
|---|---|
| `Brushing` | Brushes away dust from the spine to improve the binding quality. |
| `FiberRoughing` | The fibers of the paper on the spine are exposed without the risk of glazing the paper coating. This optimizes the spine preparation considering paper and adhesive types. |
| `Leveling` | After milling the spine, any uneven areas are leveled to achieve an even surface. |
| `Milling` | Cuts off part of the spine so the spine is not too even. A rough texture of the fibers is assured. This creates ideal conditions for stable anchoring of the sheets in the glue. |
| `Notching` | This gives a clamping effect on the spine that is desirable for some products. |
| `Sanding` | Used for voluminous book papers. |
| `Sealing` | Apply heat to a spine of a book that contains signatures that have been prepared by `ThreadSealing`. |
| `Shredding` | Produces a relatively smooth surface. Further operations like `Notching`, `Leveling`, `FiberRoughing`, `Sanding`, or `Brushing` are necessary. |

### A.3.21 Status Details

The `@StatusDetails` attribute refines the concept of a job status to be job-specific, or a Device status to be Device-specific. The following tables define individual `@StatusDetails` values and map them to the appropriate job-specific state `NodeInfo/@Status`, `JobPhase/@Status`, `QueueEntry/@Status`, or Device-specific state `DeviceInfo/@Status`.

Localized user data SHOULD be specified in `@DescriptiveName` or `Comment` elements.

In the following tables, the column `NodeInfo/@Status` also applies to `JobPhase/@Status` and `QueueEntry/@Status`.

#### A.3.21.1 Status Details for Generic Devices

**Table A.77: Status Details Mapping for Generic Devices**

| Status Details | NodeInfo/@Status | DeviceInfo/@Status | Description |
|---|---|---|---|
| `AbortedBySystem` | `Aborted` | `Stopped` | The job is being or has been aborted by the Device. |
| `BreakDown` | `Stopped` | `Offline` | Breakdown of the Device; repair needed. |
| `Calibrating` | `Setup` | `Setup` | The Device is calibrating, either manually or automatically. |
| `ControlDeferred` | — | `Offline` | The Machine is not accessible by the Device. |
| `CoverOpen` | `Stopped` | `Stopped` | One or more covers on the Device are open. |
| `DocumentAccessError` | `Aborted` | `Stopped` | The Device could not access one or more documents passed by reference. |
| `DoorOpen` | `Stopped` | `Stopped` | One or more doors on the Device are open. |
| `Failure` | `Stopped` | `Stopped` | Failure of the Device. Requires some maintenance in order to restart the Device. `Failure` has specialized subcategories: `PaperJam`, `DoubleFeed`, `BadFeed`, `BadTrim`, `ObliqueSheet`, `IncorrectComponent`, and `IncorrectThickness`. |
| `Good` | `InProgress` | `Production` | Production of products in progress; good copy counter is on, waste copy counter is off. |
| `Idling` | `Stopped` | `Production` | Device is running, but no products are produced or consumed. Good and waste copy counters are off. |
| `InputTrayMissing` | `Stopped` | `Stopped` | One or more input trays are not in the Device. |
| `InterlockOpen` | `Stopped` | `Stopped` | One or more interlock Devices on the printer are unlocked. |
| `IterationPaused` | `Suspended` | `Production` | At least one iteration cycle has completed but additional iteration cycles MAY still occur. |
| `JobCanceledByOperator` | `Aborted` | `Production` | The job was canceled by the Device operator using `ModifyQueueEntry/ModifyQueueEntryParams/@Operation="Abort"`, or means local to the Device. |
| `JobCanceledByUser` | `Aborted` | `Production` | The job was canceled by the owner of the job using `ModifyQueueEntry/ModifyQueueEntryParams/@Operation="Abort"`. |
| `JobCompletedSuccessfully` | `Completed` | `Production` | The job completed successfully. |
| `JobCompletedWithErrors` | `Completed` | `Production` | The job completed with errors, and possibly warnings too. |
| `JobCompletedWithWarnings` | `Completed` | `Production` | The job completed with warnings. |
| `JobHeld` | `Waiting` | `Production` | The Device held the job that had been waiting by performing a `ModifyQueueEntry/ModifyQueueEntryParams/@Operation="Hold"` request on a waiting `QueueEntry`. |
| `JobHeldOnCreate` | `Waiting` | `Production` | The job was submitted to the queue with `QueueSubmissionParams/@Activation="Held"`. |
| `JobIncoming` | `Waiting` | `Production` | The Device is retrieving/accepting document data. |
| `JobMissResources` | `Waiting`, `InProgress` | `Stopped` | When `@Status` is `InProgress` or `Waiting`, the `QueueEntry` waits for resources to become available to process further. |
| `JobReadyForStart` | `Waiting`, `InProgress` | `Stopped` | When `@Status` is `InProgress` or `Waiting`, the `QueueEntry` is ready and waits for a manual start event to process further. |
| `JobResuming` | `Waiting` | `Production` | The Device is in the process of moving the job from a suspended condition to a candidate for processing, using `ModifyQueueEntry/ModifyQueueEntryParams/@Operation="Resume"`. |
| `JobScheduling` | `Waiting` | `Production` | The Device is scheduling the job for processing. |
| `JobStreaming` | `InProgress` | `Production` | Same as `JobIncoming`, with the specialization that the Device is processing the document data as it is being received. The job data is not being spooled, but is being processed in chunks by the output Device and is being imaged during reception. |
| `JobSuspended` | `Suspended` | `Production` | The Device suspended the job that had been processing, for example by performing a `ModifyQueueEntry/ModifyQueueEntryParams/@Operation="Suspend"` request on a running `QueueEntry`, and other jobs can be processed by the Device. |
| `JobSuspending` | `InProgress` | `Production` | The Device is in the process of moving the job from a processing condition to a suspended condition where other jobs can be processed. |
| `JobUserInputRequired` | `Waiting`, `InProgress` | `Stopped` | When `@Status` is `Waiting` or `InProgress`, the `QueueEntry` is not producible and waits for user input required to process further, for example missing parameters, decisions, etc. |
| `Maintenance` | `Stopped` | `Stopped` | General maintenance of the Device. |
| `MissResources` | `Stopped` | `Stopped` | Production has been stopped because resources are missing or unavailable. Waits for new resources; subcategory of `Pause`. |
| `MovingToPaused` | `InProgress` | `Production` | The Device has been paused, but the Machines are taking an appreciable time to stop. |
| `OutputAreaFull` | `Stopped` | `Stopped` | One or more output areas are full, for example tray, stacker, or collator. |
| `OutputTrayMissing` | `Stopped` | `Stopped` | One or more output trays are not in the Device. |
| `PaperJam` | `Stopped` | `Stopped` | Media jam in the Device; subcategory of `Failure`. |
| `Pause` | `Stopped` | `Stopped` | Machine paused; restart is possible. |
| `PendingReturn` | `Cleanup` | `Production` | When `@Status` is `Cleanup`, the `QueueEntry` is currently returning. |
| `ProcessingToStopPoint` | `InProgress` | `Production` | The requester has issued a `ModifyQueueEntry/ModifyQueueEntryParams/@Operation="Abort"` request or the Device has aborted the job, but is still performing some actions on the job until a specified stop point occurs or job termination/cleanup is completed. |
| `QueuedToRun` | `Waiting` | `Stopped` | When `@Status` is `Waiting`, the `QueueEntry` is queued to run and waits for the Device to become available, idle, to process further. |
| `Repair` | `Stopped` | `Offline` | The Device is being repaired after a breakdown. |
| `Running` | `InProgress` | `Production` | When `@Status` is `InProgress`, the `QueueEntry` is processing. |
| `ShutDown` | `Stopped` | `Offline` | Machine stopped, possibly switched off; restart requires a run-up. |
| `SizeChange` | `Setup` | `Setup` | Changing setup for media size. |
| `StandBy` | — | `Idle` | The Device has been switched into power save mode and is still accepting new jobs. |
| `StandBy` | — | `Stopped` | The Device has been switched into power saving mode and cannot process jobs without prior intervention such as `CommandWakeUp`. |
| `WaitForApproval` | `Stopped` | `Stopped` | Production has been stopped because a necessary approval is still missing; subcategory of `Pause`. |
| `WaitForGang` | `Waiting`, `InProgress` | `Production` | The process has commenced to a point where parts of the job can be ganged on a sheet and the process is waiting for additional Gang elements. |
| `WarmingUp` | `Setup` | `Setup` | Device is warming up after power-up or power-saver mode wake-up. |
| `Waste` | `InProgress` | `Production` | Production of products in progress; good copy counter is off, waste copy counter is on. |
| `WasteFull` | `Stopped` | `Stopped` | The Device waste receptacle is full. |

#### A.3.21.2 Status Details for Printing Devices

**Table A.78: Status Details Mapping for Printing Devices**

| Status Details | NodeInfo/@Status | DeviceInfo/@Status | Description |
|---|---|---|---|
| `BlanketChange` | `Stopped` | `Stopped` | Changing of blankets; subcategory of `Maintenance`. |
| `BlanketWash` | `Cleanup` | `Cleanup` | Washing of the blanket; subcategory of `WashUp`. |
| `CleaningInkFountain` | `Cleanup` | `Cleanup` | Cleaning of the ink fountain; subcategory of `WashUp`. |
| `CylinderWash` | `Cleanup` | `Cleanup` | Washing of impression cylinders; subcategory of `WashUp`. |
| `DampeningRollerWash` | `Cleanup` | `Cleanup` | Washing of the dampening roller; subcategory of `WashUp`. |
| `FormChange` | `Setup` | `Setup` | In conventional printing, changing of plates. |
| `InkRollerWash` | `Cleanup` | `Cleanup` | Washing of the inking roller; subcategory of `WashUp`. |
| `PlateWash` | `Cleanup` | `Cleanup` | Washing of the plate; subcategory of `WashUp`. |
| `Processing` | `InProgress` | `Production` | Other productive processing, such as RIP, is taking place but no final output is being produced. All input data has arrived; it is not `JobStreaming`/`InProgress` or `JobIncoming`/`Waiting`. |
| `SleeveChange` | `Stopped` | `Stopped` | Changing of sleeves; subcategory of `Maintenance`. |
| `WaitingForMarker` | `Suspended` | `Production` | Processing is automatically suspended by the Device because it is waiting behind other jobs in the marker module. The Device will resume processing when a marker module becomes available. |
| `WashUp` | `Cleanup` | `Cleanup` | Machine is washed before, during, or after production. |

#### A.3.21.3 Status Details for Postpress Devices

**Table A.79: Status Details Mapping for Postpress Devices**

| Status Details | NodeInfo/@Status | DeviceInfo/@Status | Description |
|---|---|---|---|
| `BadFeed` | `Stopped` | `Stopped` | Bad feed on a feeder; subcategory of `Failure`. |
| `BadTrim` | `Stopped` | `Stopped` | Bad trimmed components; subcategory of `Failure`. |
| `DoubleFeed` | `Stopped` | `Stopped` | Double feeds on a feeder; subcategory of `Failure`. |
| `IncorrectComponent` | `Stopped` | `Stopped` | Incorrect components on a feeder; subcategory of `Failure`. |
| `IncorrectThickness` | `Stopped` | `Stopped` | Incorrect thickness of components; subcategory of `Failure`. |
| `ObliqueSheet` | `Stopped` | `Stopped` | Oblique sheets on components; subcategory of `Failure`. Oblique sheets are sheets or signatures that are not properly aligned within a pile, for example on a gathering or collecting chain. |

### A.3.22 Texture

The following table defines a list of values that are valid for indicating the intended texture of the item to be used. This is typically the media or substrate.

Values of the form `IPP:xxx` are provided for mapping to PWG Print Job Ticket. See `[PWGMAP]`.

**Table A.80: Texture**

| Value | Description |
|---|---|
| `Antique` | Rougher than vellum surface. |
| `Calendared` | Extra smooth or polished, uncoated paper. |
| `Gloss` | Glossy media. |
| `IPP:Course` | Generic value for coarse finish. |
| `IPP:Fine` | Generic value for fine finish. |
| `IPP:Medium` | Generic value for finish that is neither `IPP:Fine` nor `IPP:Course`. |
| `Linen` | Texture of coarse woven cloth. |
| `Matte` | Matte media. |
| `Smooth` | Generic term for smooth paper. |
| `Stipple` | Fine pebble finish. |
| `Uncalendared` | Rough, unpolished, and uncoated paper. |
| `Vellum` | Slightly rough surface. |

### A.3.23 Units

The following defines a list of values that are valid for indicating the unit of a measurement quantity.

The values in the following table are ordered alphabetically by measurement type.

**Table A.81: Units**

| Value | Measurement | Unit | Description |
|---|---|---|---|
| `degree` | Angle | degree ° | An angle in degrees. |
| `m2` | Area | m² | Used for media, for example in wide-format printing. |
| `count` | Countable Objects | 1 | Countable objects, such as sheets, MAY be specified as `count`. |
| `pt` | Length | point, 1/72 inch | Used for all except microscopic lengths. |
| `um` | Length | micron | Used for microscopic lengths. Where used instead of points, it will be explicitly stated in the definition of the item. See `Media/@Thickness`. |
| `lpi` | Line Screen | lpi | The lines per inch, lpi, for conventionally screened halftone, screened grayscale, and screened monotone bitmap images. |
| `gsm` | Paper weight | g/m² | Paper weight SHALL be provided in grams per square meter. See Appendix B, “Media Weight,” for details of calculating paper weights that are not in g/m². |
| `kWh` | Power, electrical | kilowatt hour | Used to measure consumption of electricity. Current power consumption, kW, MAY be provided in a `ResourceInfo` as “rate of consumption” of electric power, i.e. kWh/h = kW. |
| `dpi` | Resolution | dpi | The dots per inch, dpi, for print output and bitmap image file resolution, such as TIFF or BMP. |
| `ppi` | Screen Resolution | ppi | The pixels per inch, ppi, for screen display, for example soft proof display and user interface display, scanner capture settings, and digital camera settings. |
| `spi` | Spot Resolution | spi | For imaging Devices such as filmsetters, platesetters, and proofers, the fundamental imaging unit, for example one “on” laser or imaging head imaged unit. Many imaging Devices construct dots from multiple imaging spots, so dpi and spots per inch, spi, are not necessarily equivalent. |
| `C` | Temperature | °C, Celsius | The temperature in degrees centigrade. |
| `m3` | Volume, gas | m³, cubic meter | Used to measure consumption of gas. |
| `l` | Volume, liquid | liter | The volume in liters. |
| `g` | Weight | gram | The weight in grams. |

---

## A.4 Integer Values

This section contains the preferred values for items that use an integer as a code value. Although these code values are open lists, the entries in these tables SHOULD be used where possible.

If an ICS requires new code values, or a work group has agreed upon new recommended code values, these will be published at `[CIP4Names]` prior to being added to the specification and SHOULD be used where appropriate.

### A.4.1 DDES3 Diecutting Data

The following list of line types is taken from Annex A of ANSI® IT8.6-2002, *Graphic Technology — Prepress Digital Data Exchange — Diecutting data*, `[DDES3]`. The list is included in the XJDF specification with permission of IT8.6.

**Table A.82: Diecutting Data, DDES3**

| DDES3 Line Type Number | DDES3 Line Type | Description |
|---:|---|---|
| 12 | Non-varnish/UV area | Contour indicating a varnish-free area. |
| 15 | Printing/UV Blanket Edge | Contour enclosing a spot varnish area. Spot varnish will be applied with a varnish blanket. |
| 16 | Zipper/Tear Strip/Tear Edge, reference lines for cutting edge | Cutting contours indicating a tear strip. |
| 17 | Wave/Scallop, reference lines for cutting edge | Cutting contours indicating a wave/scallop. |
| 18 | Punches, reference lines for center/cutting edge | Contours indicating the shape and center of a punch. |
| 100 | Miscellaneous ruled lines for dies | — |
| 101 | Knife/Cutting rule | Contour indicating how the printed artwork will be cut from the printed sheet, for example with a guillotine cutter or die cutting Device. |
| 102 | Crease/Scoring rule | Contour indicating where the substrate will be creased to guide subsequent folding. |
| 103 | Perforation, alternating cutting and spaces | Contour indicating where the substrate will be perforated. |
| 104 | Cutscore/Halfcut, partial depth cutting rule | Contour indicating where the substrate will be cut partially, i.e. not entirely through the material. Cutting is done from the front side. |
| 105 | Cut-Crease rule, alternating cutting and creasing rule | Contour indicating alternating cutting and creasing. |
| 106 | Cutscore-Crease, alternating partial depth cutting and creasing rule | Contour indicating alternating half-cutting and creasing. |
| 107 | Reverse cutscore/halfcut, for anvil in die | Contour indicating where the substrate will be cut partially, i.e. not entirely through the material. Cutting is done from the back side. |
| 108 | Emboss/Deboss crease profile | Contour enclosing an area where embossing will be applied. |

### A.4.2 Return Codes

The following list defines the standard return codes for messaging. Return code values SHALL be integers. Error values below 100 are reserved for protocol errors. Error values above 100 SHALL be used for Device and Controller errors, while those higher than 200 refer to job and pipe specific errors.

Implementations SHOULD supply values from this list. If no appropriate return code exists in this list, then proprietary return codes MAY be used and SHOULD have a value above 1000.

**Table A.83: Return Codes**

| Return Code | Description |
|---:|---|
| 0 | Success. |
| — | **Protocol errors in the range 1–99** |
| 1 | General error. |
| 2 | Internal error. |
| 3 | XML parser error, for example if a zip file is sent to a Device that does not support zip packaging. |
| 4 | XML validation error. |

> Note: The provided text extract for this file ends after the beginning of Table A.83.
