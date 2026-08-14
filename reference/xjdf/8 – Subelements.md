# 8 Subelements

The elements in this chapter are subelements that can occur in multiple elements. They are not specific resource elements and are therefore never directly linked to processes.

---

## 8.1 Address

Definition of an address. The structure is derived from the vCard format. The corresponding vCard fields are quoted in the table.

**Element Properties**

- Element referenced by: `Contact`

### 8.1.1 AddressLine

AddressLine represents an individual address line.

> **Note:** An address may be encoded as attributes (e.g. `@City`, `@Street`, etc.), text elements in `AddressLine`, or both. The latter case may occur where the original database does not provide all the individual details of the address.

**Table 8.1: Address Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@AddressUsage?` | NMTOKEN | Specifies the intended use of the address. Values include: `Business` – Business company address. `Residence` – Private home address. |
| `City?` | string | City or locality of the Address (vCard: ADR:locality). |
| `CivicNumber?` | string | `@CivicNumber` SHALL specify the street number of the street address. If `@CivicNumber` is specified, it SHALL NOT be included in `@Street`. |
| `Country?` | string | Country code of the Address (vCard: ADR:country). |
| `CountryCode?` | NMTOKEN | `@CountryCode` SHALL specify the country code of the Address using the two letter values (ALPHA-2) from [ISO/NP 3166-1:2013]. Allowed values are from: [ISO/NP 3166-1:2013]. |
| `ExtendedAddress?` | string | Extended address (vCard: ADR:extadd. For example: Suite 245). |
| `PostalCode?` | string | Zip code or postal code of the Address (vCard: ADR:pcode). |
| `PostBox?` | string | Post office address (vCard: ADR:pobox. For example: P.O. Box 101). |
| `Region?` | string | State or province of the Address (vCard: ADR:region). |
| `Street?` | string | Street of the Address (vCard: ADR:street). `@Street` SHALL include the name of the street and SHOULD include the street number unless the street number is specified separately in `@CivicNumber`. |
| `AddressLine*` *(JSON Exception)* | element | Each AddressLine element SHALL specify one line of a printed address. If AddressLine is provided, the complete address SHALL be provided as an ordered sequence of AddressLine elements. **JSON Exception:** AddressLine SHALL be encoded as an 'array of string' in JSON. |

**Table 8.2: AddressLine Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| *(text content)* | — | Text content of the AddressLine. |

**Example 8.1: JSON Mapping of AddressLine**

The following example shows how AddressLine is encoded in XML and JSON.

XML Encoding:

```xml
<Address>
  <AddressLine>line 1</AddressLine>
  <AddressLine>line 2</AddressLine>
  <AddressLine>line 3</AddressLine>
</Address>
```

JSON Encoding:

```json
{
  "Address": {
    "AddressLine": ["line 1", "line 2", "line 3"]
  }
}
```

---

## 8.2 ApprovalPerson

ApprovalPerson specifies the details of the person who is responsible for modifying the state of an approval by updating an ApprovalDetails resource.

**Element Properties**

- Element referenced by: `ApprovalDetails`, `ApprovalParams`

**Table 8.3: ApprovalPerson Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@ApprovalRole?` | enumeration | Role of the ApprovalPerson. Allowed values are: `Approvinator` – The decision of this approver immediately overrides the decisions of the other approvers and ends the approval cycle. The "Approvinator" NEED NOT update ApprovalDetails for the approval to become valid. `Informative` – The approver is informed of the Approval process, but the approval is still valid, even without his approval. `Obligated` – The approver SHALL update the ApprovalDetails of the approval. |
| `@ApprovalRoleDetails?` | string | Additional details on the `@ApprovalRole`. `@ApprovalRole` SHOULD be specified if `@ApprovalRoleDetails` is specified. |
| `ContactRef` | IDREF | Additional details of the person who SHALL sign the approval. The referenced Contact SHALL contain a `Part/@ContactType="Approver"`. |

---

## 8.3 AutomatedOverPrintParams

AutomatedOverPrintParams provides controls for the automated selection of overprinting of black text or graphics.

**Element Properties**

- Element referenced by: `RenderingParams`, `SeparationControlParams`

**Table 8.4: AutomatedOverPrintParams Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@KnockOutCMYKWhite?` | boolean | If `@KnockOutCMYKWhite="true"`, graphic objects defined in DeviceCMYK, where all colorant values are < 0.001 SHALL be knocked out, even when set to overprint and when the PDF overprint mode is set to 1. |
| `@LineArtBlackLevel?` | float | A value between 0.0 and 1.0 that indicates the minimum black level for the stroke or fill colors that cause the line art to be set to overprint. `@LineArtBlackLevel` SHALL NOT be specified unless `@OverPrintBlackLineArt="true"`. |
| `@OverPrintBlackLineArt?` | boolean | Indicates whether overprint SHALL be set to `"true"` for black line art (i.e., vector elements other than text). If `"true"`, overprint of black line art is applied regardless of any values in the PDL. |
| `@OverPrintBlackText?` | boolean | Indicates whether overprint SHALL be set to `"true"` for black text. If `"true"`, overprint of black text is applied regardless of any values in the PDL. |
| `@TextBlackLevel?` | float | A value between 0.0 and 1.0 that indicates the minimum black level for the text stroke or fill colors that cause the text to be set to overprint. `@TextBlackLevel` SHALL NOT be specified unless `@OverPrintBlackText="true"`. |
| `@TextSizeThreshold?` | integer | Indicates the point size for text below which black text will be set to overprint. For asymmetrically scaled text, the minimum point size between both axes SHALL be used. `@TextSizeThreshold` SHALL NOT be specified unless `@OverPrintBlackText="true"`. |

---

## 8.4 BarcodeCompParams

BarcodeCompParams specifies the technical compensation parameters for barcodes.

**Element Properties**

- Element referenced by: `BarcodeReproParams`

**Table 8.5: BarcodeCompParams Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `CompensationProcess` | enumeration | Process that is bar width spread SHALL be compensated for. Allowed values are: `Platemaking`, `Printing`. |
| `CompensationValue` | float | The width of the bars SHALL be reduced by this amount (in microns) to compensate for technical spread. |

---

## 8.5 BarcodeReproParams

BarcodeReproParams specifies the reproduction parameters for barcodes.

**Element Properties**

- Element referenced by: `Content/BarcodeProductionParams`, `Layout/StripMark`

**Table 8.6: BarcodeReproParams Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@BearerBars?` | enumeration | `@BearerBars` specifies how to generate bearer bars (ITF). Allowed values are: `Box`, `BoxHMarks`, `None`, `TopBottom`. |
| `@Height?` | float | `@Height` SHALL specify the height (Y direction) of the bars of a linear barcode in the PDL. |
| `@Magnification?` | float | The magnification factor that linear barcodes SHALL be scaled with. For example, a value for `@Magnification > 1` requests thicker barcode lines in the resulting PDL. |
| `@Masking?` | enumeration | Indicates the properties of the mask around the graphical content of the barcode that masks out all underlying graphics. Allowed values are: `None` – No masking, the barcode is put on top of underlying graphics. `WhiteBox` – An area of the underlying graphics SHALL be masked out (the white box) and the barcode SHALL be put on top of this masked area. The area of the white box SHALL be the box enclosing all artwork of the barcode, excluding optional human readable text. The box SHALL enclose bearer bars, quiet zones and non-optional human readable text (UPC and EAN barcodes). |
| `@ModuleHeight?` | float | The Y size in microns of an element of a 2D barcode (e.g., PDF417). For DATAMATRIX, Y dimension MAY be omitted (X dimension = Y dimension). |
| `@ModuleWidth?` | float | The X size in microns of an element of a 2D barcode such as DATAMATRIX or PDF417. |
| `@Ratio?` | float | The ratio between the width of the narrow bars and the wide bars for those barcodes where the ratio of the width of the wide bars to the narrow bars MAY vary. |
| `BarcodeCompParams*` | element | Parameters for bar width compensation. The total reduction of bar width SHALL be the sum of all `BarcodeCompParams/@CompensationValue`. |

---

## 8.6 BindingQuality

*New in XJDF 2.1*

The set of parameters in BindingQuality identifies how the quality of the binding is verified.

### 8.6.1 Flex test

The page flex test (page turning test) is used more and more rarely in quality checking, not least because it is time-consuming. In the page flex test a sheet is moved back and forth under varying tensile loads, usually at 1 N/cm, until it pulls out of the glue film, with the number of to and fro movements being measured automatically.

> **Note:** As this test procedure involves a rapid turning movement, the flex test is called a dynamic test procedure.

### 8.6.2 Pull test

In the pull test (sheet pulling test), a single sheet is subjected to slowly increasing tensile loading until it comes away from the glue film or the material breaks down. The load increases constantly during the automatic test procedure. It is applied evenly along the whole length of the glued seam.

> **Note:** That is why the pull test is also described as a static test method.

**Table 8.7: BindingQuality Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@FlexValue?` | float | Minimum flex quality parameter measured in [N/cm] that SHALL be applied for a test to succeed. |
| `@PullOutValue?` | float | Minimum pull out quality parameter measured in [N/cm] that SHALL be applied for a test to succeed. |

---

## 8.7 Certification

Certification specifies the certification properties of a resource or process.

**Element Properties**

- Element referenced by: `ColorIntent/SurfaceColor`, `Ink`, `MediaIntent`, `Media`, `MiscConsumable`, `ProductionIntent`

**Table 8.8: Certification Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Claim?` | string | Name of the certification as defined by the issuing organization. Values include: `FSC 100%`, `FSC Mix 70%`, `FSC Mix Credit`, `FSC Recycled 85%`, `FSC Recycled Credit`, `PEFC nn%`, `PEFC Certified`, `PEFC Recycled`. |
| `@Identifier?` | string | Certification identification number as defined by the issuing organization. |
| `@Organization?` | NMTOKEN | Identifier of the issuing organization. Values include: `CFCC` – China's National Forest Certification System, `FSC` – Forest Stewardship Council, `IFCC` – Sustainable Forest Management Requirements, `PEFC` – The Programme for the Endorsement of Forest Certification. |

---

## 8.8 ColorControlStrip

ColorControlStrip describes a color control strip. The type of the color control strip is given in the `@StripType` attribute. The lower left corner of the control strip box is used as the origin of the coordinate system used for the definition of the measuring fields. Its coordinates (x0, y0) can be calculated using the following formula:

```
x0 = x - (w/2) * cos(φ) + (h/2) * sin(φ)
y0 = y - (w/2) * sin(φ) - (h/2) * cos(φ)
```

Where:

- `x` = X element of the `@Center` attribute
- `y` = Y element of the `@Center` attribute
- `w` = X element of the `@Size` attribute
- `h` = Y element of the `@Size` attribute
- `φ` = Value of the `@Rotation` attribute

**Element Properties**

- Element referenced by: `ColorMeasurement`, `MarkObject`, `StripMark`

**Table 8.9: ColorControlStrip Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Center?` | XYPair | Position of the center of the color control strip in the coordinates of the MarkObject that contains this mark. |
| `@Rotation?` | float | Rotation in degrees. Positive graduation figures indicate counter-clockwise rotation; negative figures indicate clockwise rotation. |
| `@Separations?` *(Deprecated in XJDF 2.1)* | NMTOKENS | Ordered list of separation identifiers that comprise the ColorControlStrip. The geometry is implied by the value of `@StripType`. Additional details of the colorants SHOULD be provided in `ResourceSet[@Name="Color"]`. **Deprecation note:** See `Patch/SeparationTint`. |
| `@Size?` | XYPair | Size, in points, of the color control strip. |
| `@StripType?` | string | Type of color control strip. This attribute MAY be used for specifying a pre-defined, company-specific color control strip. |
| `CIELABMeasuringField*` *(Deprecated in XJDF 2.1)* | element | Details of a CIELAB measuring field that is part of this ColorControlStrip. **Deprecation note:** See `Patch`. |
| `ColorMeasurementConditions?` *(New in XJDF 2.1)* | element | Detailed description of the measurement conditions for color measurements that are defined in this ColorControlStrip. |
| `DensityMeasuringField*` *(Deprecated in XJDF 2.1)* | element | Details of a density measuring field that is part of this ColorControlStrip. **Deprecation note:** See `Patch`. |
| `Patch*` *(New in XJDF 2.1)* | element | Details of a color measurement field that is part of this ColorControlStrip. |

### 8.8.1 Patch

*New in XJDF 2.1*

Patch elements SHALL specify the values of a color measurement patch. When Patch is specified as a descendent of a QualityControlResult, it SHALL define actual measurement values. In any other context, a Patch element SHALL specify measurement target data.

> **Note:** A patch can represent either a dedicated printed technical patch or an area in the printed content.

**Table 8.10: Patch Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Center?` | XYPair | Position of the center of the Patch in the coordinates of the parent ColorControlStrip. `@Center` SHALL refer to the lower left corner of the unrotated rectangle defined by `@Center` and `@Size` of the parent ColorControlStrip. |
| `@Density?` | float | Density value of the Patch. Whereas `@NeutralDensity` describes measurements of inks on substrate with wide-band filter functions, `@Density` is derived from measurements of inks on substrate with special small-band filter functions according to ANSI and DIN. |
| `@ExternalID?` | NMTOKEN | Identification that is used to reference the Patch. |
| `@Lab?` | LabColor | L, a, b value of the Patch value of the colorant. |
| `@NeutralDensity?` | float | A number in the range of 0.001 to 10 that represents the neutral density of the Patch, defined as `10 * log10(1/Y)`. Y is the tristimulus value in CIEXYZ coordinates, normalized to 1.0. |
| `@PatchUsage` | enumeration | `@PatchUsage` SHALL specify the general type of the color patch. Allowed values are: `Color` – The patch contains data for colorimetric density or spectral measurements. `Image` – The patch is part of printed content. `Technical` – The patch contains data for auxiliary technical measurements such as Moiré or doubling of images. `Ignore` – The patch is on the sheet but SHALL be ignored for technical reasons. |
| `@RGB?` | RGBColor | RGB equivalent of the color in the Patch. `@RGB` SHOULD only be used for display purposes. |
| `@Size?` | XYPair | The size of the Patch. |
| `@Spectrum?` | TransferFunction | Spectrum of the color as measured with the measurement conditions defined in `ColorControlStrip/ColorMeasurementConditions`. The x values of `@Spectrum` SHALL specify the wavelength in NM and the y values SHALL specify the spectral reflectance measurements. A value of 0.0 SHALL specify total absorption. A value of 1.0 SHALL specify 100% reflectance. **Note:** Values that are greater than 1.0 are possible due to wavelength shifts e.g. from optical brighteners. |
| `@SpotType?` *(New in XJDF 2.2)* | enumeration | `@SpotType` specifies how the colorant of the Patch SHALL be, or has been, produced. Allowed values are: `Emulated` – The patch SHALL be, or has been, produced by emulating the spot color using multiple colorants. `Spot` – The patch SHALL be, or has been, produced using a real colorant. |
| `SeparationTint*` | element | Each SeparationTint element SHALL specify the tint of a separation at the Patch position. The values of SeparationTint are always target values that SHALL be calculated from the input data including an output profile if available. `SeparationTint/@Name` SHALL be unique in the context of an individual Patch. |

#### 8.8.1.1 SeparationTint

*New in XJDF 2.1*

**Table 8.11: SeparationTint Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Name` | NMTOKEN | Separation identifier of a colorant that is expected to be printed. Additional details of the colorants SHOULD be provided in `ResourceSet[@Name="Color"]`. |
| `@Tint` | float | Value of the tint where a value of 1 specifies 100% solid tint value of the colorant that is selected by `@Name`. |

---

## 8.9 ColorMeasurement

*New in XJDF 2.1*

ColorMeasurement SHALL provide a detailed definition of the color measurements. If ColorMeasurement is specified as a child of QualityControlParams it SHALL specify color quality target values. If ColorMeasurement is specified as a child of QualityControlResult it SHALL specify color quality measurements results.

**Element Properties**

- Element referenced by: `QualityControlParams`, `QualityControlResult`

**Table 8.12: ColorMeasurement Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `ColorControlStrip?` | element | ColorControlStrip SHALL specify a color control strip for color quality measurement. |

---

## 8.10 ColorMeasurementConditions

*New in XJDF 2.1*

This element contains information about the specific measurement conditions for spectral or densitometric color measurements. Spectral measurements refer to [CIE 015:2004] and [ISO13655:2017]. The default measurement conditions for spectral measurements are illuminant D50 and 2 degree observer.

Density measurements refer to [ISO5-3:2009] and [ISO5-4:2009]. The default measurement conditions for densitometric measurements are density standard ISO/ANSI Status T, calibration to absolute white and using no polarization filter.

**Element Properties**

- Element referenced by: `Color`, `ColorControlStrip`, `Media`

**Table 8.13: ColorMeasurementConditions Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Aperture?` | float | Aperture of the measurement optics in millimeters. |
| `@DensityStandard?` | enumeration | Density filter standard used during density measurements. Allowed values are: `ANSIA` – ANSI Status A, `ANSIE` – ANSI Status E, `ANSII` – ANSI Status I, `ANSIT` – ANSI Status T, `DIN16536`, `DIN16536NB`. |
| `@Illumination?` | NMTOKEN | Illumination used during spectral measurements. Allowed values include: `D50`, `D65`, `Unknown`. |
| `@IlluminationAngle?` | integer | `@IlluminationAngle` specifies the angle between a line normal to the surface and the incident angle of the illumination. |
| `@InkState?` | enumeration | State of the ink during color measurements. Allowed values are: `Dry` – The ink is completely dry and can be compared to standard target values. `Wet` – The ink is not yet completely dry and cannot be compared to standard target values. |
| `@MeasurementAngle?` | integer | `@MeasurementAngle` specifies the angle between a line normal to the surface and the incident angle of the measurement. |
| `@MeasurementFilter?` | enumeration | Optical filter used during color measurements. Allowed values are: `None` – No filter used. `Pol` – Polarization filter used. `UV` – Ultraviolet cut filter used. |
| `@MeasurementMode?` | NMTOKEN | `@MeasurementMode` SHALL specify the illumination conditions according to [ISO13655:2017] or a proprietary standard such as a printer's internal or vendor specific standard. Values include: `M0` – CIE illuminant A, undefined UV amount, includes all legacy spectrophotometers. `M1` – CIE illuminant D50. Part1: D50 match, use for all fluorescence (ink, papers, etc). Part2: Calculated UV response to emulate UV excitation of OBAs (for paper only). `M2` – UV cut. `M3` – Polarization filter with UV cut. |
| `@Observer?` | integer | CIE standard observer function (2 degree and 10 degree) used during spectral measurements. Values are in degrees. |
| `@SampleBacking?` | enumeration | Backing material used behind the sample during color measurements. Allowed values are: `Black` – Measurement on a black background. `Substrate` – Measurement on a pile of the measured substrate. `White` – Measurement on a white background. |
| `@SpectralResolution?` | float | Spectral resolution of the measuring Device in nm. |
| `@WhiteBase?` | enumeration | Reference white used for color measurements. Allowed values are: `Absolute` – The instrument is calibrated to a Device specific calibration target (absolute white) and measures spectral reflectance with respect to the incident light, e.g. D50 or D65. `Substrate` – The instrument is calibrated relative to paper white. The spectral reflectance is divided by that of the print substrate. Therefore the media relative spectral reflectance of the substrate is defined as unity: X/Y/Z=1; L\*/a\*/b\*=100/0/0 and D=0. |

---

## 8.11 Comment

The Comment element can be used to provide human readable text.

**Element Properties**

- Element referenced by: `Activity`, `ApprovalDetails`, `ContentMetadata`, `Defect`, `Notification`, `PreflightCheck`, `Product`, `Resource`, `ResourceSet`, `XJDF`

**Table 8.14: Comment Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Author?` | string | Human readable text that identifies the person who created the Comment. See also `@PersonalID`. |
| `@ExternalID?` | NMTOKEN | Identification that is used to reference the Comment. |
| `@Language?` | language | Human readable language of the Comment. |
| `@PersonalID?` | NMTOKEN | Machine readable identifier of the employee that entered the comment. When the Comment is created by a person with a known `Contact/@UserID`, then `@PersonalID` SHOULD contain the value of `Contact/@UserID`. See also `@Author`. |
| `@TimeStamp?` | dateTime | Describes the date and time when the Comment was created. |
| `@Text?` *(JSON Exception, New in XJDF 2.2)* | string | `@Text` specifies the body of the Comment. **JSON Exception:** `@Text` MAY be specified when encoded in JSON and SHALL NOT be specified when encoded in XML. |
| `@Type?` | NMTOKEN | `@Type` specifies the usage of a comment. Values include: `DeviceText` – Human readable description created by the Device that provides details beyond the value of `@StatusDetails`. The "DeviceText" value SHOULD only be specified in `Notification/Comment/@Type`. `Instruction` – Message to the operator that contains information regarding the processing of the job. The "Instruction" value SHOULD only be specified in `XJDF/Comment/@Type`. `JobDescription` – Description of the job. The "JobDescription" value SHOULD only be specified in `XJDF/Comment/@Type`. See also `CustomerInfo/@CustomerJobName`. `OperatorText` – Message from the operator that contains information regarding the processing of the job. The "OperatorText" value SHOULD only be specified in `Notification/Comment/@Type`. `Orientation` – Description of the orientation of a Resource. The "Orientation" value SHOULD only be specified in `Resource/Comment/@Type` or `ResourceSet/Comment/@Type`. |
| *(text content)* | — | Body of the comment. **Note:** Whitespace is preserved only as generic whitespace in XML. Applications that display comments to the user SHOULD maintain whitespace. |

**Example 8.2: Multi-line Comment**

The following example shows a multi-line comment with whitespace.

```xml
<Comment ExternalID="c_000004" Type="Instruction">Multiline text with white space

and empty lines</Comment>
```

**Example 8.3: Use of JSON for Comments**

The following example shows how a Comment is encoded in XML and JSON.

XML Encoding:

```xml
<Comment Author="Wyle E Coyote" PersonalID="p123">line 1
line 2</Comment>
```

JSON Encoding:

```json
{
  "Comment": {
    "Author": "Wyle E Coyote",
    "PersonalID": "p123",
    "Text": "line 1\nline 2"
  }
}
```

---

## 8.12 Condition

The condition element defines the condition when a CellCondition, PageActivation, PageCondition or SheetActivation is active when processing a layout with `@Automated="true"`.

The source of the Part elements that SHALL be evaluated by Condition SHALL be the input RunList of the Imposition process.

The content pages of the RunList that are evaluated by Condition NEED NOT be present on the Layout that is currently being evaluated. For instance a job summary will not contain any content pages but will typically be triggered by the last content page of a job.

**Element Properties**

- Element referenced by: `BinderySignature/SignatureCell/CellCondition`, `Layout/PageActivation`, `Layout/PageCondition`, `Layout/SheetActivation`

**Table 8.15: Condition Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@PartContext?` | NMTOKENS | List of attribute names within Part that SHALL reset the context of the Part elements in this Condition. The Part elements in this Condition SHALL NOT contain any attributes that are in `@PartContext`. Example: `@PartContext="DocIndex"` and `Part/@RunIndex="0 1"`. In this case `@RunIndex` is recalculated whenever `@DocIndex` evaluates to a different value. **Note:** Without `@PartContext` and no explicit `Part/@DocIndex`; `@RunIndex` would be evaluated in the context of the entire RunList. |
| `Part+` | element | This Condition SHALL evaluate to `"true"` whenever any RunList partition matches at least one of the Part elements. |

---

## 8.13 ConvertingConfig

The ConvertingConfig element describes a range of sheet sizes that can be used for optimizing a die layout in DieLayoutProduction or a press sheet for SheetOptimizing.

**Element Properties**

- Element referenced by: `DieLayoutProductionParams`, `SheetOptimizingParams`

**Table 8.16: ConvertingConfig Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@MarginBottom?` | float | The bottom margin for positioning the layout on the sheet. |
| `@MarginLeft?` | float | The left margin for positioning the layout on the sheet. |
| `@MarginRight?` | float | The right margin for positioning the layout on the sheet. |
| `@MarginTop?` | float | The top margin for positioning the layout on the sheet. |
| `@SheetHeightMax?` | float | The maximum sheet height, in points. |
| `@SheetHeightMin?` | float | The minimum sheet height, in points. |
| `@SheetWidthMax?` | float | The maximum sheet width, in points. |
| `@SheetWidthMin?` | float | The minimum sheet width, in points. |
| `CutBlock*` *(New in XJDF 2.1)* | element | If present, each CutBlock element SHALL specify a cut block on the selected Media. **Note:** CutBlock is provided to specify regions of common finishing properties if the press sheet size is larger than the finishing sheet sizes. |
| `Device*` | element | The target Devices (printing press, die cutter and further finishing equipment) corresponding to this configuration. Typically only the type of Device would be used (e.g., the model of the die cutter). If multiple Devices are specified, then the other attributes in this element SHALL apply to a production configuration that uses all specified Devices. |
| `Media*` | element | Zero or more Media elements that are candidates for optimization. **Note:** Media allows a media database savvy consumer to loop over an explicit list of known materials rather than providing results based on a range of dimensions only. |

---

## 8.14 Crease

Crease defines an individual crease line on a component.

**Element Properties**

- Element referenced by: `CreasingParams`, `FoldingParams`

**Table 8.17: Crease Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Depth?` | float | Depth of the crease, measured in microns [µm]. |
| `@StartPosition?` | XYPair | Starting position of the tool. |
| `@WorkingDirection?` | enumeration | Direction from which the tool is working. Allowed value is from: WorkingDirection. |
| `@WorkingPath?` | XYPair | Working path of the tool beginning at `@StartPosition`. |

---

## 8.15 Cut

Cut describes one straight cut with an arbitrary tool.

**Element Properties**

- Element referenced by: `CuttingParams`, `FoldingParams`

**Table 8.18: Cut Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@CutWidth?` | float | Width in points of u-shaped knife, saw blade, etc. |
| `@StartPosition?` | XYPair | Starting position of the tool. |
| `@WorkingDirection?` | enumeration | Direction from which the tool is working. Allowed value is from: WorkingDirection. |
| `@WorkingPath?` | XYPair | Working path of the tool beginning at `@StartPosition`. |

---

## 8.16 CutBlock

CutBlock specifies exactly one cut block on a sheet. The CutBlock SHALL be defined in the coordinate system of the input Component.

**Element Properties**

- Element referenced by: `ConvertingConfig`, `CuttingParams`

**Table 8.19: CutBlock Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@BinderySignatureIDs?` | NMTOKENS | If specified, `@BinderySignatureIDs` SHALL list the `BinderySignature/@BinderySignatureID` of all BinderySignatures that comprise this CutBlock. |
| `@BlockName` | NMTOKEN | Name of the block. The output Component of the Cutting process SHALL be partitioned by `@BlockName`. The values of `@BlockName` SHALL match the value of this `@BlockName`. |
| `@Box?` | rectangle | Defines the position and size of the block relative to the parent Component coordinate system. |
| `@CutWidth?` | float | Width in points of the u-shaped knife, saw blade, etc. |
| `@DescriptiveName?` *(New in XJDF 2.1)* | string | Human readable descriptive name of the CutBlock. |
| `@ExternalID?` *(New in XJDF 2.1)* | NMTOKEN | External identifier of the CutBlock, e.g. in an MIS. |
| `@Operations?` *(New in XJDF 2.1)* | NMTOKENS | List of finishing operations or properties that are common to the CutBlock. The values are implementation dependent and MAY depend on the specific details of the finishing process. See also `GangElement/@Operations`. |

---

## 8.17 CutMark

CutMark provides the means to position cut marks on the sheet. After printing, these marks can be used to adapt the theoretical block positions (as specified in `CuttingParams/CutBlock`) to the real position of the corresponding blocks on the printed sheet.

**Element Properties**

- Element referenced by: `Layout/MarkObject`, `StripMark`

**Table 8.20: CutMark Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@MarkType` | enumeration | Cut mark type. Allowed value is from: CutMarkType. |
| `@Position` | XYPair | Position of the logical center of the cut mark in the coordinates of the object that contains this mark. **Note:** The logical center of the cut mark does not always coincide with the center of the visible cut mark symbol. |

---

## 8.18 Event

This element provides additional information for common events. Events are designed to enable Devices to send individual event or error codes. These codes SHOULD NOT be used to transport status transitions or resource information that is standardized in DeviceInfo, ResourceInfo or their descendent elements.

**Element Properties**

- Element referenced by: `DeviceInfo`, `Notification`, `ResourceInfo`

**Table 8.21: Event Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@EventID` | NMTOKEN | Internal event ID of the application that emits the event. |
| `@EventValue?` | string | Additional user defined value related to this event. |

---

## 8.19 FileSpec

FileSpec SHALL specify a URL or a set of URLs. FileSpec is independent of the protocol and MAY implicitly or explicitly reference either files or network locations. If a single FileSpec instance specifies a set of URLs, it SHALL do so using the `@FileFormat` and `@FileTemplate` attributes to specify a sequence of URLs. Otherwise, each FileSpec instance specifies a single URL.

**Element Properties**

- Element referenced by: `ApprovalDetails`, `ColorSpaceConversionOp`, `ColorSpaceConversionParams`, `Content`, `CuttingParams`, `ContentCheckIntent/ProofItem`, `DeliveryParams`, `Device`, `Device/IconList/Icon`, `DieLayout`, `FoldingParams`, `InterpretingParams/PDFInterpretingParams/ReferenceXObjParams`, `LabelingParams`, `Layout`, `LayoutElementProductionParams`, `PDLCreationParams/PDFCreationDetails/PDFXParams`, `PreflightParams`, `PreflightReport`, `Preview`, `QualityControlParams`, `QualityControlResult`, `QualityControlResult/Inspection`, `QualityControlResult/Inspection/Defect`, `RenderingParams/TIFFFormatParams/TIFFEmbeddedFile`, `RunList`, `ShapeDef`, `ShapeDefProductionParams/ObjectModel`, `ShapeDefProductionParams/ShapeTemplate`, `StitchingParams`, `VerificationParams`, `VerificationResult`

**Table 8.22: FileSpec Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@CheckSum?` | hexBinary | Checksum of the file being referenced using the RSA MD5 algorithm. The data type was chosen as hexBinary to accommodate the 128 bit output of the MD5 algorithm. The `@CheckSum` SHALL be calculated from the entire file, not just parts of the file. |
| `@Encoding?` | NMTOKEN | Encoding or code page of the file contents. Values include those from: [IANA-character sets]. |
| `@FileFormat?` | string | A formatting string used with the `@FileTemplate` attribute to define a sequence of URLs in a batch process, each of which has the same semantics as the `@URL` attribute. If neither `@URL` nor `@UID` is present, both `@FileFormat` and `@FileTemplate` SHALL be present, unless the resource is a pipe. If either `@URL` or `@UID` is specified, then `@FileFormat` and `@FileTemplate` SHALL NOT be specified. Allowed values are from: Appendix D String Generation. |
| `@FileSize?` | integer | Size of the file in bytes. |
| `@FileTemplate?` | NMTOKENS | A template, used with `@FileFormat`, to define a sequence of URLs in a batch process, each of which has the same semantics as the `@URL` attribute. If neither `@URL` nor `@UID` is present, both `@FileFormat` and `@FileTemplate` SHALL be present, unless the resource is a pipe. If either `@URL` or `@UID` is specified, then `@FileFormat` and `@FileTemplate` SHALL NOT be specified. Values include those from: Appendix D String Generation. |
| `@MimeType?` | string | MIME type or file type of the file (or files of identical type when specifying a sequence of file names using the `@FileFormat` and `@FileTemplate` attributes). If the file format has a MIME Media Type [IANA-mt] registered with IANA, that value SHALL be used. The [RFC2046] defines that MIME Media Types are case-insensitive. |
| `@NPage?` *(New in XJDF 2.2)* | integer | `@NPage` SHALL specify the total number of reader Pages in the file that is referenced by `@URL`. If FileSpec is a descendant of a RunList, values of negative indices in `RunList/@Pages` SHALL then be calculated using `FileSpec/@NPage` as a count of the total number of pages in the referenced file. |
| `@OverwritePolicy?` | enumeration | Policy that specifies the policy to follow when a file already exists and the FileSpec is used in an output resource. Allowed values are: `Abort` – Abort the process without modifying the old file. `NewVersion` – Create a new file version. Only valid when the FileSpec references a file on a version aware file system. `OperatorIntervention` – Present a dialog to an operator. `Overwrite` – Overwrite the old file. `RenameNew` – Rename the new file. `RenameOld` – Rename the old file. |
| `@Password?` | string | Password or decryption key that is needed to read the file contents. **Note:** Since this password string is not encrypted, it SHOULD only be passed around within a protected environment. |
| `@ResourceUsage?` | NMTOKEN | If this specification specifies FileSpec(ResourceUsage) in the name column of an element table, then `FileSpec/@ResourceUsage` SHALL be provided. See Table 1.3 Template for Element Descriptions for details. **Note:** `@ResourceUsage` is generally required if an element contains more than one FileSpec subelement. |
| `@SearchDepth?` | integer | Used when FileSpec refers to a directory to specify the maximum directory depth that will be recursively searched. 0 specifies this directory only, -1 specifies an unlimited search. |
| `@UID?` | NMTOKEN | Internal ID of the referenced file. The `@UID` SHALL be unique within the workflow. The value of `@UID` is dependent on the type of file that is referenced: **PDF** – Variable unique identifier in the ID field of the PDF file's trailer. **ICC Profile** – The Profile ID in bytes 84-99 of the ICC profile header. **Others** – Format specific. If neither `@URL` nor `@UID` is present on an input FileSpec, and neither `@FileFormat` nor `@FileTemplate` is present, the referencing resource SHALL be a pipe. If either `@URL` or `@UID` is specified, then `@FileFormat` and `@FileTemplate` SHALL NOT be specified. |
| `@URL?` | URL | Location of the file specified as either an absolute URI or a relative URI. If neither `@URL` nor `@UID` is present on an input FileSpec, and neither `@FileFormat` nor `@FileTemplate` is present, the referencing resource SHALL be a pipe. If either `@URL` or `@UID` is specified, then `@FileFormat` and `@FileTemplate` SHALL NOT be specified. See [RFC3986] for the syntax and examples. For the 'file' URL scheme see also [RFC1738]. |
| `@UserFileName?` | string | A user-friendly name that can be used to identify the file. MAY be used by a Controller to identify a file on a Device without knowing the file's internal location. |
| `Disposition?` | element | Indicates what the Device SHOULD do with the file when the process that uses this FileSpec completes. If not specified the file specified by this FileSpec SHOULD NOT be deleted by the Device. |
| `NetworkHeader*` *(New in XJDF 2.1)* | element | NetworkHeader elements MAY provide protocol header information in case communication requires specific http or https header setup. |

### 8.19.1 Disposition

This element describes how long the digital asset that is referenced by the FileSpec SHOULD be maintained by a Device. The Device SHALL perform an action defined by `Disposition/@DispositionAction` when a "disposition time" occurs. Disposition time is defined as either:

- `@Until` <= "Disposition time" <= `@Until` + `@ExtraDuration`
- ProcessCompleteTime + `@MinDuration` <= "Disposition time" <= ProcessCompleteTime + `@MinDuration` + `@ExtraDuration`

**Table 8.23: Disposition Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@DispositionAction?` | enumeration | `@DispositionAction` specifies the required disposal action for the asset. Allowed values are: `Archive` – The asset SHALL be archived when disposition time occurs. `Delete` – The asset SHALL be deleted when disposition time occurs. |
| `@ExtraDuration?` | duration | Indicates the maximum duration that the Device SHALL retain the asset after the time specified by `@MinDuration` or `@Until`. If `@ExtraDuration`, `@MinDuration` and `@Until` are all unspecified, the asset MAY be retained for a system specified time. |
| `@MinDuration?` | duration | Indicates the minimum duration for which the Device SHOULD retain the asset after the process that uses the asset completes. `@MinDuration` SHALL NOT be specified if `@Until` is present. |
| `@Priority?` | integer | Value between 0 and 100 that specifies the order in which assets SHALL be deleted or archived when the values of `@ExtraDuration`, `@MinDuration` and `@Until` cannot be honored (e.g., when local storage runs low). Assets with `@Priority="0"` SHALL be deleted first. |
| `@Until?` | dateTime | Indicates an absolute point in time when the Device or application SHOULD discard the asset. `@Until` SHALL NOT be specified if `@MinDuration` is present. |

### 8.19.2 NetworkHeader

*New in XJDF 2.1*

NetworkHeader elements MAY provide protocol header information in case communication requires specific http or https header setup. Examples include authentication using bearer tokens, see [RFC6750].

**Table 8.24: Header Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Name` | string | Name of the header excluding any required protocol dependent syntax elements or characters, e.g. ":", ";", "." or "=". |
| `@Value` | string | Value of the header. |

---

## 8.20 FitPolicy

This element specifies how to fit content into a receiving container (e.g., a page onto a ContentObject of an imposed sheet).

See the description of each reference to FitPolicy to determine what the context-specific content is and what the receiving containers are.

**Element Properties**

- Element referenced by: `InterpretingParams`, `Layout`, `RasterReadingParams`

**Table 8.25: FitPolicy Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@ClipOffset?` | XYPair | Defines the offset (position) of the imaged area in the non-rotated source image when `@SizePolicy` is `"ClipToMaxPage"`. The values `"0.0 0.0"` mean that the imaged area starts at the lower left point of the receiving container. If absent, the imaged area SHALL be taken from the center of the source image. |
| `@ExpansionPolicy?` *(New in XJDF 2.1)* | enumeration | `@ExpansionPolicy` SHALL specify the fill direction for placing content into the container. `@ExpansionPolicy` SHALL NOT be specified unless the value of `@SizePolicy` is one of `"CompleteGrid"`, `"FillGrid"` or `"Tile"`. Allowed values are: `HorizontalOnly` – The grid SHALL be expanded in horizontal direction only. `HorizontalVertical` – The grid SHALL be expanded in horizontal direction by adding cells to a row and adding a new row after the current row is filled. `VerticalHorizontal` – The grid SHALL be expanded in vertical direction by adding cells to a column and adding a new column after the current column is filled. `VerticalOnly` – The grid SHALL be expanded in vertical direction only. |
| `@GutterPolicy?` | enumeration | Allows printing of NUp grids even if the media size does not match the requirements of the data. Allowed values are: `Distribute` – The gutters can grow or shrink to the value specified in `@MinGutter`. `Fixed` – The gutters are fixed. |
| `@HorizontalGridDirection?` *(New in XJDF 2.1)* | enumeration | `@HorizontalGridDirection` SHALL specify the direction in which a row is filled with content. `@HorizontalGridDirection` SHALL NOT be specified unless `@ExpansionPolicy` is present and does not have a value of `"VerticalOnly"`. Allowed values are: `LeftToRight` – Each row SHALL be filled from left to right. `RightToLeft` – Each row SHALL be filled from right to left. |
| `@MinGutter?` | XYPair | Minimum width in points of the horizontal and vertical gutters formed between rows and columns of pages of a multi-up sheet layout. The first value specifies the minimum width of all horizontal gutters and the second value specifies the minimum width of all vertical gutters. |
| `@RotatePolicy?` | enumeration | Specifies the policy for the Device to automatically rotate the content to optimize the fit of the content to the receiving container. Allowed values are: `NoRotate` – Do not rotate. `RotateClockwise` – Rotate clockwise by 90°. `RotateCounterClockwise` – Rotate counterclockwise by 90°. `RotateOrthogonal` – Rotate by 90° in either direction. |
| `@SizePolicy?` | enumeration | Allows printing even if the container size does not match the requirements of the data. Allowed values are: `Abort` – Emit an error and abort printing. `ClipToMaxPage` – The page contents SHALL be clipped to the size of the container. The printed area is either centered in the source image if no `@ClipOffset` key is given, or from that position that is determined by `@ClipOffset`. `CompleteGrid` – Allow multiple complete occurrences of data to be placed into the container. If the size of the data is larger than the receiving container, printing SHALL be aborted. `FillGrid` – Allow multiple occurrences of data to be placed into the container. Partial occurrences SHALL be printed. **Note:** A value of `"FillGrid"` allows printing of the complete sheet with repeated placement of the content, e.g. for textile printing. `FitToPage` – The page contents SHALL be scaled up or down to fit the container. The aspect ratio SHALL be maintained. `ReduceToFit` – The page contents SHALL be scaled down but not scaled up to fit the container. The aspect ratio SHALL be maintained. `Tile` – The page contents SHALL be split into several tiles, each tile SHALL be printed on its own surface. |
| `@VerticalGridDirection?` *(New in XJDF 2.1)* | enumeration | `@VerticalGridDirection` SHALL specify the direction in which a column is filled with content. `@VerticalGridDirection` SHALL NOT be specified unless `@ExpansionPolicy` is present and does not have a value of `"HorizontalOnly"`. Allowed values are: `BottomToTop` – Each column SHALL be filled from bottom to top. `TopToBottom` – Each column SHALL be filled from top to bottom. |

---

## 8.21 Fold

Fold describes an individual folding operation of the Component.

**Element Properties**

- Element referenced by: `FoldingIntent`, `FoldingParams`

**Table 8.26: Fold Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@From` | enumeration | Edge from which the page SHALL be folded. Allowed values are: `Front`, `Left`. |
| `@To` | enumeration | Direction in which the page SHALL be folded. Allowed values are: `Up` – Upwards; corresponds to a valley fold with the left/bottom side coming over the opposite side. `Down` – Downwards; corresponds to a mountain or peak fold with the left/bottom side coming under the opposite side. |
| `@Travel?` | float | Distance of the reference edge relative to `@From`. |

---

## 8.22 GangSource

GangSource provides source job information about a BinderySignature that is placed on a Gang form.

**Element Properties**

- Element referenced by: `JobPhase`, `QueueFilter`, `QueueEntry`, `NodeInfo`

**Table 8.27: GangSource Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@BinderySignatureID?` | NMTOKEN | If present, `@BinderySignatureID` SHALL reference the BinderySignature that this GangSource represents. |
| `@Copies` | integer | `@Copies` SHALL specify the number of copies of the BinderySignature that are required. |
| `@JobID` | NMTOKEN | `@JobID` SHALL reference `XJDF/@JobID` of the individual job that describes the processing prior to and after printing and cutting the Gang sheet. |

---

## 8.23 GeneralID

GeneralID describes a generic identifier. The name or usage of the identifier is specified in `GeneralID/@IDUsage` and the specific value of the variable is specified in `GeneralID/@IDValue`. The data type is specified in `GeneralID/@DataType`.

Although GeneralID could technically be used to describe arbitrary proprietary data, this is strongly discouraged as it is non interoperable. Proprietary extensions SHOULD be avoided if possible, or if absolutely required, they MAY be implemented in proprietary namespaces.

**Element Properties**

- Element referenced by: `XJDF`, `ResourceSet`, `Product`, `Resource`, `Content`, `PreflightParams/PreflightTest`, `PreflightReport/PreflightCheck`

**Table 8.28: GeneralID Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@DataType?` | enumeration | Data type of the variable. Allowed value is from: DataType. |
| `@IDUsage` | NMTOKEN | Usage of the GeneralID. If GeneralID is required by an ICS or other specification, the recommended values of `@IDUsage` are defined by that ICS or specification. This specification makes no assumptions on the format of `@IDUsage`, e.g. whether a prefix is recommended. |
| `@IDValue` | string | Value of the GeneralID. The data type of the value SHALL correspond to `GeneralID/@DataType`. |

---

## 8.24 Glue

This element provides the information for determining where and how to apply glue. All positions and paths are specified relative to the center of the glue application tool.

**Element Properties**

- Element referenced by: `AssemblingIntent/BindIn`, `AssemblingIntent/StickOn`, `BindingIntent/AdhesiveNote`, `BoxFoldingParams`, `CaseMakingParams`, `EndSheetGluingParams`, `GluingParams`, `HeadBandApplicationParams`, `InsertingParams`, `ThreadSewingParams`, `MediaLayers`

**Table 8.29: Glue Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@AreaGlue?` | boolean | Specifies that this Glue SHOULD cover the complete width of the Component it is applied to. |
| `@GlueLineWidth?` | float | Width of the glue line in points. If not specified, the default behavior depends on the value of `@AreaGlue`: If `@AreaGlue="true"`, then the implied width is the width of the Component. If `@AreaGlue="false"`, then the implied width is the system dependent glue line width. |
| `@GlueRef?` | IDREF | Reference to a MiscConsumable that represents the physical glue. |
| `@GlueType?` | enumeration | Glue type. Allowed values are: `ColdGlue` – Any type of glue that needs no heat treatment. `Hotmelt` – Hotmelt EVA (Ethylene-vinyl acetate). `Permanent` – Any glue that is designed not to be removed. `PUR` – Polyurethane. `Removable` – Any glue that is designed to be removed. |
| `@GluingPattern?` | FloatList | Glue line pattern defined by the length of a glue line segment (1st element, 3rd and all odd elements of the list of values) and glue line gap (2nd element, 4th and all even elements of the list of values). A solid line SHALL be expressed by the pattern `(1 0)`. `@GluingPattern` SHALL contain an even number of entries. If the total length of `@GluingPattern` is less than `@WorkingPath`, the pattern restarts after the last gap. If the total length of `@GluingPattern` is larger than `@WorkingPath`, the pattern SHALL be clipped at the end. |
| `@GluingTechnique?` | enumeration | When glue is specified in the context of hardcover binding, then `@GluingTechnique` specifies the technique of gluing operation. Allowed values are: `SideGluingBack`, `SideGluingFront`, `SpineGluing`. |
| `@MeltingTemperature?` | integer | Temperature needed for melting the glue, in degrees centigrade. `@MeltingTemperature` SHALL NOT be specified unless `@GlueType="Hotmelt"` or `@GlueType="PUR"`. |
| `@StartPosition?` | XYPair | Start position of the glue line. |
| `@WorkingDirection?` | enumeration | Direction from which the glue should be applied to the Component. Allowed value is from: Face. |
| `@WorkingPath?` | XYPair | Relative working path of the gluing tool. |

---

## 8.25 HolePattern

The HolePattern element describes a pattern of one or more holes.

> **Note:** For dealing with the default case of `@HoleCount` (i.e., when it is not supplied), intelligent systems MAY take into consideration physical properties such as the length of the binding edge or distance of holes to the paper edges to calculate the appropriate number of holes. For production of the holes and selection/production of the matching binding element, the "system specified" values SHALL match 100% between the HoleMaking and the process for obvious reasons.

**Element Properties**

- Element referenced by: `HoleMakingIntent`, `HoleMakingParams`, `LooseBinding`, `LooseBindingParams`, `Media`

**Table 8.30: HolePattern Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Center?` | XYPair | Position of the center of the first hole relative to the coordinate system that is defined in `@CenterReference` or the coordinate system of the input Component if `@CenterReference` is not present. If not specified, the value SHALL be defined by the value of `@Pattern`. |
| `@CenterReference?` | enumeration | Defines the reference coordinate system for `@Center`. Allowed values are: `RegistrationMark` – The center is relative to a registration mark. `TrailingEdge` – Physical coordinate system of the component. **Note:** RegistrationMark is typically used in webfed printing where no trailing edge is available. |
| `@Extent?` | XYPair | Size (bounding box) of each hole, in points. If `@Shape` is `"Round"`, only the first entry of `@Extent` SHALL be evaluated and SHALL define the hole diameter. If not specified, the value SHALL be defined by the value of `@Pattern`. |
| `@HoleCount?` | IntegerList | `@HoleCount` specifies the number of consecutive holes and spaces. The first entry defines the number of holes, the second entry defines the number of spaces, and consecutive entries alternately define holes (h) and spaces (s), for instance: `"2 2 2"` = `"h h s s h h"`. `"0 3 3 3 3"` = `"s s s h h h s s s h h h"`. **Note:** `@HoleCount` is typically applied to patterns with `@Pattern` whose enumeration values begin with a "P", "W" or "C" in Table F.1 Naming Scheme for Hole Patterns. |
| `@Pattern?` | NMTOKEN | Predefined hole pattern. `@Pattern` SHALL be supplied if one of `@Center`, `@Extent` or `@Shape` is not specified. Allowed value is from: Section F Hole Pattern Catalog. |
| `@Pitch?` | XYPair | If `@Pitch` is specified, this HolePattern represents a line of holes. `@Pitch` represents the distance between the centers of two adjacent holes. |
| `@ReferenceEdge?` | enumeration | The edge of the Component relative to where the holes SHALL be placed. Allowed values are: `Bottom`, `Left`, `Pattern` – Specifies the reference edge implied by the value of `@Pattern` in Section F Hole Pattern Catalog, `Right`, `Top`. |
| `@Reinforcement?` | NMTOKEN | `@Reinforcement` specifies how the holes SHALL be reinforced. Values include: `Grommet`. **Note:** Additional details of the reinforcement MAY be supplied in a MiscConsumable with `MiscConsumable/@Type="Grommet"`. |
| `@Shape?` | enumeration | Shape of the holes. If not specified, the value SHALL be defined by the value of `@Pattern`. Allowed values are: `Elliptic`, `Rectangular`, `Round`. |

---

## 8.26 IdentificationField

This resource contains information about a mark on a document, e.g. a bar code. The data in IdentificationField can be used to dynamically generate barcodes. It can also be used to decode the contents of a bar code, e.g. when used for OCR-based verification purposes or document separation.

**Element Properties**

- Element referenced by: `Component`, `Content/BarcodeProductionParams`, `Device`, `EmbossingParams/Emboss`, `ExposedMedia`, `Ink`, `Layout/StripMark`, `Media`, `MiscConsumable`, `Pallet`, `Tool`, `Module`

**Table 8.31: IdentificationField Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@BoundingBox?` | rectangle | Box that provides the boundaries of the mark that indicates where the IdentificationField is placed. If the IdentificationField is specified in a Layout, the coordinate system SHALL be defined by the StripMark containing the IdentificationField. If no Layout context is available, the origin of the coordinate system SHALL be defined as the lower left corner of the resource surface that `@Position` specifies when the specified surface is viewed in its natural orientation. Each item in the list below specifies a value of `@Position` and the corner that is the origin for the specified value when the viewer is positioned in front of the front surface. For example, when `@Position="Left"`, the origin is the bottom-back corner of the left surface when viewed from the front surface of the resource and lower left corner when viewed from the left surface. `"Back"` – Bottom right corner. `"Bottom"` – Back left corner. `"Front"` – Bottom left corner. `"Left"` – Bottom back corner. `"Right"` – Bottom front corner. `"Top"` – Front left corner. If no `@BoundingBox` is defined and the IdentificationField is specified outside the context of a Layout, the complete visible surface SHALL be scanned for an appropriate bar code. If no `@BoundingBox` is defined and the IdentificationField is specified within the context of a Layout, the implied `@BoundingBox` SHALL be specified by the position of the StripMark. **Note:** `@BoundingBox` is used only as metadata when searching or scanning IdentificationField elements and not used when generating IdentificationField elements in a LayoutElementProduction process. |
| `@Encoding?` | enumeration | Encoding of the information. Allowed values are: `ASCII` – Plain-text font. `Barcode` – Any bar code. `Braille` – Braille text. `RFID` – Radio Frequency Identification tag. |
| `@EncodingDetails?` | NMTOKEN | Details about the encoding type. An example is the bar code scheme. Values include those from: Table 8.32 EncodingDetails Attribute Values. |
| `@Format?` | regExp | Regular expression that defines the expected format of the expression (e.g., the number of digits, alphanumeric or numeric). **Note:** This field MAY also be used to define constant fields (e.g., the end of document markers or packaging labels). If not specified, any expression is valid. Exactly one of `@Format`, `@Value` or the pair `@ValueFormat` and `@ValueTemplate` SHALL be specified. |
| `@Orientation?` | matrix | Orientation of the contents within the IdentificationField. The coordinate system is defined in the system of the sheet or component where the IdentificationField resides. The `@Orientation` is used only as metadata when searching or scanning IdentificationField elements and not used when generating IdentificationField elements in a LayoutElementProduction process. |
| `@Position?` | enumeration | Position with respect to the Instance Document or Resource to which IdentificationField refers. Allowed value is from: Face. |
| `@Purpose?` | enumeration | Purpose defines the usage of the field. Allowed values are: `Label` – Used to mark a product or component. `Separation` – Used to separate documents. `Verification` – Used for verification of documents. |
| `@PurposeDetails?` | NMTOKEN | More detail about the usage of the barcode. Values include: `ProductIdentification` – End product identification (e.g., scanning in the supermarket). |
| `@Value?` | string | Fixed value of the IdentificationField (e.g., on a label). Exactly one of `@Format`, `@Value` or the pair `@ValueFormat` and `@ValueTemplate` SHALL be specified. |
| `@ValueFormat?` | string | A formatting string used with `@ValueTemplate` to define fixed and/or variable content of barcodes or text. Exactly one of `@Format`, `@Value` or the pair `@ValueFormat` and `@ValueTemplate` SHALL be specified. Allowed values are from: Appendix D String Generation. |
| `@ValueTemplate?` | NMTOKENS | A list of values used with `@ValueFormat` to define fixed and/or variable content of barcodes or text. If MetadataMap elements are present, `MetadataMap/@Name` SHALL be included in `@ValueTemplate` to select the data from the MetadataMap. Exactly one of `@Format`, `@Value` or the pair `@ValueFormat` and `@ValueTemplate` SHALL be specified. Values include those from: Appendix D String Generation. |
| `BarcodeDetails?` | element | Additional specification for complex barcodes. |
| `ExtraValues?` | element | Additional values encoded in the IdentificationField. |
| `MetadataMap*` | element | Describes the mapping of metadata that is encoded in an IdentificationField to Partition Keys. **Note:** This allows for automated selective finishing based on bar codes. |

**Table 8.32: EncodingDetails Attribute Values**

The following list provides a sample of barcode encoding details. Values that are not present in this list MAY be valid in an XJDF workflow.

| Value | Description | Value | Description |
|-------|-------------|-------|-------------|
| `BOBST` | | `ITF_14` | |
| `BrailleASCII` | A binary representation for 6 dot Braille messages. See [Braille ASCII]. | `ITF_6` | |
| `BrailleUnicode` | A binary representation for Braille messages. See [Braille Unicode]. | `ITF_16` | |
| `CODABAR` | | `KURANDT` | |
| `CODABAR_Tradional` | | `LAETUS_PHARMA` | |
| `CODABLOCK` | | `MSI` | |
| `CODABLOCK_F` | | `NDC_HRI` | |
| `Code128` | | `PARAF` | |
| `Code25` | | `Plessey` | |
| `Code39` | | `PDF417` | |
| `Code39_Extended` | | `PZN` | |
| `DATAMATRIX` | | `QR` | |
| `EAN` | Includes Bookland_EAN and ISSN. | `RSS_14` | |
| `EAN_13` | | `RSS_14_Stacked` | |
| `EAN_8` | | `RSS_14_Stacked_Omnidir` | |
| `EAN_Coupon` | | `RSS_14_Truncated` | |
| `EAN_128` | | `RSS_Limited` | |
| `HIBC_Code39` | | `RSS_Expanded` | |
| `HIBC_Code128` | | `RSS_Expanded_Stacked` | |
| `HIBC_Code39_2` | | `UPC_A` | |
| `HIBC_CODABLOCK_F` | | `UPC_Coupon` | |
| `HIBC_QR` | | `UPC_E` | |
| `HIBC_DATAMATRIX` | | `UPC_SCS` | |
| `Interleave25` | | | |

### 8.26.1 BarcodeDetails

**Table 8.33: BarcodeDetails Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@BarcodeVersion?` | NMTOKEN | The version of a barcode. Values include those from: Table 8.36 BarcodeVersion Values – for DATAMATRIX or HIBC_DATAMATRIX barcodes. Values include those from: Table 8.37 BarcodeVersion Values – for QR barcodes. |
| `@ErrorCorrectionLevel?` | NMTOKEN | Error correction level for barcodes having a separately definable error correction level. Each value can be used only for certain values of `IdentificationField/@EncodingDetails`. Values include: `PDF417_EC_0` – For `@EncodingDetails="PDF417"`, `PDF417_EC_1` – For `@EncodingDetails="PDF417"`, `PDF417_EC_2` – For `@EncodingDetails="PDF417"`, `PDF417_EC_3` – For `@EncodingDetails="PDF417"`, `PDF417_EC_4` – For `@EncodingDetails="PDF417"`, `PDF417_EC_5` – For `@EncodingDetails="PDF417"`, `PDF417_EC_6` – For `@EncodingDetails="PDF417"`, `PDF417_EC_7` – For `@EncodingDetails="PDF417"`, `PDF417_EC_8` – For `@EncodingDetails="PDF417"`, `QR_EC_L` – For `@EncodingDetails="QR"`, `QR_EC_M` – For `@EncodingDetails="QR"`, `QR_EC_Q` – For `@EncodingDetails="QR"`, `QR_EC_H` – For `@EncodingDetails="QR"`. |
| `@XCells?` | integer | The number of cells in the x direction of a matrix barcode. For "DATAMATRIX" this field can be omitted since `@BarcodeVersion` already defines this. For "PDF417" this is the number of codewords/row. |
| `@YCells?` | integer | The number of cells in the y direction of a matrix barcode. For "DATAMATRIX" this field can be omitted since `@BarcodeVersion` already defines this. For "PDF417" this is the number of rows. |

### 8.26.2 ExtraValues

**Table 8.34: ExtraValues Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Usage` | NMTOKEN | The usage of the value. Values include: `CompositeCode` – This is applicable for barcodes like RSS-14 that have an optional composite code part. `Coupon` – The additional message for the EAN128 part of a UPC or EAN coupon. `Supplemental` – UPC supplemental 2/5 digit symbology. |
| `@Value` | string | Additional value of the IdentificationField as specified in `@Usage`. |

### 8.26.3 Usage of barcode attributes

The following table specifies whether the BarcodeReproParams attributes `@Height`, `@Magnification` and `@Ratio` are applicable for a given barcode type that is specified by `@EncodingDetails`.

**Table 8.35: Usage of Barcode Attributes for Certain Barcode Types**

| EncodingDetails Values (Barcode Types) | Height | Magnification | Ratio |
|----------------------------------------|--------|---------------|-------|
| Code25, Code39, Code39_Extended, Interleave25, MSI, Plessey | Used | Used | Used |
| CODABAR, Code128, EAN_128, EAN_13, EAN_8, HIBC_Code39, HIBC_Code128, ITF_14, ITF_16, NDC_HRI, PARAF, UPC_A, UPC_E, UPC_SCS | Used | Used | Not used |
| BOBST, KURANDT, LAETUS_PHARMA | Used | Not used | Not used |
| RSS_14, RSS_14_Stacked, RSS_14_Stacked_Omnidir, RSS_14_Truncated, RSS_Limited, RSS_Expanded, RSS_Expanded_Stacked | Not used | Used | Not used |
| PZN | Not used | Not used | Not used |

The following table specifies valid values of `BarcodeDetails/@BarcodeVersion` for DATAMATRIX or HIBC_DATAMATRIX barcode.

**Table 8.36: BarcodeVersion Values – for DATAMATRIX or HIBC_DATAMATRIX barcodes**

| Values | | | |
|--------|---|---|---|
| DM_8_by_18 | DM_8_by_32 | DM_16_by_16 | DM_16_by_36 |
| DM_10_by_10 | DM_16_by_48 | DM_26_by_26 | DM_32_by_32 |
| DM_12_by_12 | DM_18_by_18 | DM_40_by_40 | DM_72_by_72 |
| DM_12_by_26 | DM_20_by_20 | DM_44_by_44 | DM_80_by_80 |
| DM_12_by_36 | DM_22_by_22 | DM_48_by_48 | DM_88_by_88 |
| DM_14_by_14 | DM_24_by_24 | DM_52_by_52 | DM_96_by_96 |
| | | DM_64_by_64 | DM_104_by_104 |
| | | | DM_120_by_120 |
| | | | DM_132_by_132 |
| | | | DM_144_by_144 |

The following table specifies valid values of `BarcodeDetails/@BarcodeVersion` for a QR barcode.

**Table 8.37: BarcodeVersion Values – for QR barcodes**

| Values | | | | | | | |
|--------|---|---|---|---|---|---|---|
| QR_1 | QR_6 | QR_11 | QR_16 | QR_21 | QR_26 | QR_31 | QR_36 |
| QR_2 | QR_7 | QR_12 | QR_17 | QR_22 | QR_27 | QR_32 | QR_37 |
| QR_3 | QR_8 | QR_13 | QR_18 | QR_23 | QR_28 | QR_33 | QR_38 |
| QR_4 | QR_9 | QR_14 | QR_19 | QR_24 | QR_29 | QR_34 | QR_39 |
| QR_5 | QR_10 | QR_15 | QR_20 | QR_25 | QR_30 | QR_35 | QR_40 |

**Example 8.4: Barcode**

The following example illustrates the description of a barcode in a LayoutElementProduction process.

```xml
<XJDF xmlns="http://www.CIP4.org/JDFSchema_2_0" JobID="LayoutElementProduction" JobPartID="Barcode" Types="LayoutElementProduction">
  <ResourceSet Name="LayoutElementProductionParams" Usage="Input">
    <Resource>
      <LayoutElementProductionParams ContentRefs="r_000007"/>
    </Resource>
  </ResourceSet>
  <ResourceSet Name="Content">
    <Resource ID="r_000007">
      <Content ContentType="Page">
        <BarcodeProductionParams>
          <BarcodeReproParams Height="73.5" Magnification="1">
            <BarcodeCompParams CompensationProcess="Printing" CompensationValue="10"/>
          </BarcodeReproParams>
          <IdentificationField Encoding="Barcode" EncodingDetails="EAN_13" Purpose="Label" PurposeDetails="ProductIdentification" Value="0123456789128"/>
        </BarcodeProductionParams>
      </Content>
    </Resource>
  </ResourceSet>
</XJDF>
```

---

## 8.27 ImageCompression

ImageCompression specifies image compression properties of individual types of images.

**Element Properties**

- Element referenced by: `Content`, `ImageCompressionParams`

**Table 8.38: ImageCompression Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@AntiAliasImages?` | boolean | If `"true"`, anti-aliasing is permitted on images. If `"false"`, anti-aliasing is not permitted. Anti-aliasing increases the number of bits per component in downsampled images to preserve some of the information that would otherwise be lost by downsampling. Anti-aliasing is only performed if the image is actually downsampled and if `@ImageDepth` has a value greater than the number of bits per color component in the input image. |
| `@AutoFilterImages?` | boolean | If `"true"`, the filter defined by `@ImageAutoFilterStrategy` is applied to photos and the zip compression ("FlateEncode") filter is applied to screen shots. If `"false"`, the `@ImageFilter` compression method is applied to all images. `@AutoFilterImages` SHALL NOT be specified unless `@EncodeImages` is `"true"`. This attribute SHALL NOT be specified if `@ImageType="Monochrome"`. |
| `@ConvertImagesToIndexed?` | boolean | If `"true"`, the application converts images that use fewer than 257 colors to an indexed color space for compactness. This attribute is used only when `@ImageType="Color"`. |
| `@DCTQuality?` | float | A value between 0 and 1 that indicates the amount of compression with which the process SHALL compress images when using a "DCTEncode" filter. A value of 0.0 requires that the compression SHOULD be lossless, whereas a value of 1.0 requires the maximum compression possible. |
| `@DownsampleImages?` | boolean | If `"true"`, sampled color images are downsampled using the resolution specified by `@ImageResolution`. If `"false"`, downsampling is not carried out and the image resolution in the PDF file is the same as that in the source file. |
| `@EncodeImages?` | boolean | If `"true"`, images are encoded using the compression filter specified by the value of `@ImageFilter`. If `"false"`, no compression filters are applied to sampled images. |
| `@ImageAutoFilterStrategy?` | NMTOKEN | Selects what the image compression strategy to employ if passing through an image that is not already compressed. Values include: `JPEG` – Lossy JPEG compression for low-frequency images and lossless Flate compression for high-frequency images. `JPEG2000` – Lossy JPEG2000 compression for low-frequency images and lossless JPEG2000 compression for high-frequency images. |
| `@ImageDepth?` | integer | Specifies the number of bits per component in the downsampled image when `@DownsampleImages="true"`. If not specified, the downsampled image has the same number of bits per sample as the original image. |
| `@ImageDownsampleThreshold?` | float | Sets the image downsample threshold for images. This is the ratio of image resolution to output resolution above which downsampling can be performed. For example, if `@ImageDownsampleThreshold="1.5"` and `@ImageResolution="72"`, then the input image would not be downsampled unless it has a resolution greater than (72 * 1.5) = 108 dpi. |
| `@ImageDownsampleType?` | enumeration | Downsampling algorithm for images. Allowed values are: `Average` – The program averages groups of samples to get the new downsampled value. `Bicubic` – The program uses bicubic interpolation on a group of samples to get a new downsampled value. `Subsample` – The program picks the middle sample from a group of samples to get the new downsampled value. |
| `@ImageFilter?` | NMTOKEN | Specifies the compression filter to be used for images. Ignored if `@AutoFilterImages="true"` or if `@EncodeImages="false"`. Values include: `CCITTFaxEncode` – Used to select CCITT group 3 or 4 facsimile encoding. SHALL NOT be specified unless `@ImageType="Monochrome"`. `DCTEncode` – Used to select JPEG compression. `FlateEncode` – Used to select zip compression. `JBIG2Encode` – Used to select JBIG2 encoding. SHALL NOT be specified unless `@ImageType="Monochrome"`. `JPEG2000` – Used to select JPEG2000/Wavelet compression. `LZWEncode` – Used to select LZW compression. `PackBits` – Used to select a simple byte-oriented run length scheme. |
| `@ImageResolution?` | float | Specifies the minimum resolution for downsampled color images in dots per inch. This value is used only when `@DownsampleImages="true"`. The application downsamples only images whose resolution is above this value. |
| `@ImageType?` | enumeration | Specifies the kind of image that SHALL be manipulated. Allowed values are: `Color`, `Grayscale`, `Monochrome`. |
| `@JPXQuality?` | integer | Specifies the image quality. Valid values are greater than or equal to one (1) and less than or equal to 100. One (1) means lowest quality (highest compression), 99 means visually lossless compression, and 100 means numerically lossless compression. |
| `CCITTFaxParams?` | element | The equivalent of the PostScript Rows and BlackIs1 parameters, which are implicit in the raster data to be compressed. |
| `DCTParams?` | element | The equivalent of the PostScript Columns, Rows and Colors parameters, which are assumed to be implicit in the raster data to be compressed. |
| `FlateParams?` | element | The equivalent of the PostScript Columns, BitsPerComponent and Colors parameters, which are implicit in the raster data to be compressed. |
| `JBIG2Params?` | element | Provides the JBIG2 compression parameters. |
| `JPEG2000Params?` | element | Provides the JPEG2000 compression parameters. |
| `LZWParams?` | element | The equivalent of the PostScript Columns, BitsPerComponent and Colors parameters, which are implicit in the raster data to be compressed. |

### 8.27.1 CCITTFaxParams

**Table 8.39: CCITTFaxParams Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@EncodedByteAlign?` | boolean | `@EncodedByteAlign` indicates whether the CCITTFaxEncode filter SHALL insert extra 0 bits before each encoded line so that the line begins on a byte boundary. |
| `@EndOfBlock?` | boolean | A flag indicating whether the CCITTFaxEncode filter SHALL append an end-of-block pattern to the encoded data. |
| `@EndOfLine?` | boolean | A flag indicating whether the CCITTFaxEncode filter SHALL prefix an end-of-line bit pattern to each line of encoded data. |
| `@K?` | integer | An integer that selects the encoding scheme to be used. < 0 – Pure two-dimensional encoding (Group 4, TIFF Compression = 4). = 0 – Pure one-dimensional encoding (Group 3, 1-D, TIFF Compression = 2). > 0 – Mixed one- and two-dimensional encoding (Group 3, 2-D, TIFF Compression = 3), in which a line encoded one-dimensionally MAY be followed by at most `@K – 1` lines encoded two-dimensionally. |
| `@Uncompressed?` | boolean | A flag to indicate whether the file generated MAY use uncompressed encoding when advantageous. |

### 8.27.2 DCTParams

**Table 8.40: DCTParams Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@ColorTransform?` | enumeration | Color transformation algorithm. Allowed values are: `Automatic` – "YUV" for 3-channel raster data, "None" otherwise. `None` – Colors SHALL NOT be transformed. `YUV` – RGB raster values SHALL be transformed to YUV before encoding and from YUV to RGB after decoding. If four channels are present CMYK values SHALL be transformed to YUVK before encoding and SHALL be transformed from YUVK to CMYK after decoding. **Note:** YUV is equivalent to YCbCr in TIFF terminology. |
| `@HSamples?` | IntegerList | A sequence of horizontal sampling factors. If present, one entry SHALL be provided per color channel in the raster data. If not specified, the implied default is `"1"` for every channel. |
| `@HuffTable?` | FloatList | Huffman tables for DC and AC components. If present, there SHALL be at least one HuffTable element for each color channel. |
| `@QFactor?` | float | A scale factor that SHALL be applied to the elements of `@QuantTable`. |
| `@QuantTable?` | FloatList | Quantization tables. If present, there SHALL be one `@QuantTable` entry for each color channel. |
| `@VSamples?` | IntegerList | A sequence of vertical sampling factors. If present, one entry SHALL be provided per color channel in the raster data. If not specified, the implied default is `"1"` for every channel. |

When the DCTParams element is a subelement of ImageCompression used in a Rendering process to generate TIFF files, YUV is equivalent to YCbCr in TIFF terminology. The HSamples and VSamples values are used to set YCbCrSubSampling or CIELabSubSampling. This means that they are only relevant for data supplied as Lab, or data where `@ColorTransform` is `"YUV"`; that the first element SHALL be 1 in each case; that the fourth element SHALL be 1 where CMYK data is to be compressed; and that the second and third elements SHALL equal each other.

### 8.27.3 FlateParams

**Table 8.41: FlateParams Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Predictor?` | integer | A code that selects the predictor function. **Note:** On "1n" PNG predictors, these values select the specific PNG predictor function(s) to be used. When decoding, the predictor function SHALL be explicitly encoded in the incoming data. Values include: `1` – No predictor (normal encoding or decoding). `2` – TIFF Predictor 2. `10` – PNG predictor, None function. `11` – PNG predictor, Sub function. `12` – PNG predictor, Up function. `13` – PNG predictor, Average function. `14` – PNG predictor, Path function. `15` – PNG predictor in which the encoding filter SHALL automatically choose the optimum function separately for each row. |

### 8.27.4 JBIG2Params

**Table 8.42: JBIG2Params Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@JBIG2Lossless?` | boolean | If `"true"` requires JBIG2 compressed images to retain the exact representation of the original image without loss. |

### 8.27.5 JPEG2000Params

**Table 8.43: JPEG2000Params Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@CodeBlockSize?` | integer | The nominal code block width and height. The value SHALL be a power of 2. |
| `@LayerRates?` | FloatList | Compression bit ratio for each layer. If specified, there SHALL be the same number of values in this list as `@LayersPerTile` in ascending order. Small values correspond to maximum compression and 1.0 corresponds to no compression (lossless). If available, `@LayerRates` SHOULD be supplied. |
| `@LayersPerTile?` | integer | Specifies the number of quality layers per tile at the same resolution. |
| `@NumResolutions?` | integer | The number of resolution levels that SHALL be encoded in the file. |
| `@ProgressionOrder?` | enumeration | Per tile progression order. Allowed values are: `CPRL` – Component-position-resolution-layer progressive. `LRCP` – Layer-resolution-component-position progressive (i.e., rate scalable). `PCRL` – Position-component-resolution-layer progressive. `RLCP` – Resolution-layer-component-position progressive (i.e., resolution scalable). `RPCL` – Resolution-position-component-layer progressive. |
| `@TileSize?` | XYPair | The width and height of each encoding tile. If not specified the image SHALL be encoded as a single tile. |

### 8.27.6 LZWParams

**Table 8.44: LZWParams Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@EarlyChange?` | integer | A code indicating when to increase the code word length. The TIFF specification can be interpreted to imply that code word length increases are postponed as long as possible. However, some existing implementations of LZW increase the code word length one code word earlier than necessary. The PostScript language supports both interpretations. If `@EarlyChange` is `"0"`, code word length increases are postponed as long as possible. If it is `"1"`, they occur one code word early. |
| `@Predictor?` | integer | A code that selects the predictor function. **Note:** On "1n" PNG predictors, these values select the specific PNG predictor function(s) to be used. When decoding, the predictor function SHALL be explicitly encoded in the incoming data. Values include: `1` – No predictor (normal encoding or decoding). `2` – TIFF Predictor 2. `10` – PNG predictor, None function. `11` – PNG predictor, Sub function. `12` – PNG predictor, Up function. `13` – PNG predictor, Average function. `14` – PNG predictor, Path function. `15` – PNG predictor in which the encoding filter SHALL automatically choose the optimum function separately for each row. |

---

## 8.28 MediaLayers

MediaLayers contains an ordered list of subelements. Each subelement describes an individual layer of a multi-layered Media such as self-adhesive labels or corrugated boards. The first layer in MediaLayers SHALL specify the front layer of the Media until the last layer, which SHALL define the back layer.

The order of the Glue and Media elements SHALL precisely specify the order of the individual layers.

> **Note:** Unlike the majority of the specification, the child elements in MediaLayers are not lexically ordered.

**Element Properties**

- Element referenced by: `Media`

**Table 8.45: MediaLayers Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Name?` *(JSON Exception, New in XJDF 2.2)* | enumeration | `@Name` SHALL specify whether MediaLayers is Glue or Media. **JSON Exception:** `@Name` SHALL only be supplied in JSON-encoded XJDF. Allowed values are: `Glue`, `Media`. |
| `Glue*` *(JSON Exception)* | element | Glue SHALL specify a glue layer of multi-layered Media. The value of `Glue/@AreaGlue` SHALL be `"true"`. **JSON Exception:** Glue SHALL be in-lined in JSON-encoded XJDF. See Example 8.5: JSON-encoded MediaLayers. |
| `Media*` *(JSON Exception)* | element | Each Media SHALL describe an individual layer of multi-layered Media. **JSON Exception:** Media SHALL be in-lined in JSON-encoded XJDF. See Example 8.5: JSON-encoded MediaLayers. |

**Example 8.5: JSON-encoded MediaLayers**

The following example illustrates how the XJDF MediaLayers element is encoded in both XML and JSON.

XML Encoding:

```xml
<ResourceSet Name="Media" Usage="Input">
  <Resource>
    <Media Dimension="1190.5511811 0" MediaType="SelfAdhesive" MediaUnit="Roll" Thickness="900">
      <MediaLayers>
        <Media MediaType="Paper" Weight="90"/>
        <Glue AreaGlue="true" GlueType="Removable"/>
        <Media MediaType="Paper" Weight="60"/>
      </MediaLayers>
    </Media>
  </Resource>
</ResourceSet>
```

JSON Encoding:

```json
{
  "ResourceSet": {
    "Name": "Media",
    "Resource": [{
      "Media": {
        "Dimension": [1190.5511811, 0],
        "MediaLayers": [{
          "MediaType": "Paper",
          "Name": "Media",
          "Weight": 90
        }, {
          "AreaGlue": true,
          "GlueType": "Removable",
          "Name": "Glue"
        }, {
          "MediaType": "Paper",
          "Name": "Media",
          "Weight": 60
        }],
        "MediaType": "SelfAdhesive",
        "MediaUnit": "Roll",
        "Thickness": 900
      }
    }],
    "Usage": "Input"
  }
}
```

---

## 8.29 MetadataMap

MetadataMap allows metadata embedded in PDL files or barcodes that are represented by IdentificationField to be assigned to Partition Key values. If MetadataMap is defined in a RunList, the metadata SHALL be extracted from the PDL as follows: each MetadataMap element SHALL be evaluated for each node (set, document, page, etc.) of the PDL document structure.

For XML-based PDL files an XPath expression SHALL be evaluated relative to the XML node that defines each node in the document hierarchy. For non-XML-based PDLs a PDL specific mapping of the XPath to the PDL document structure SHALL be used instead and the value assignment SHALL be performed on the derived XML for the PDL file.

If MetadataMap is defined in an IdentificationField, then `IdentificationField/@ValueTemplate` SHALL provide a list of variables that can be further processed in `MetadataMap/@ValueTemplate`.

**Element Properties**

- Element referenced by: `IdentificationField`, `RunList`

**Table 8.46: MetadataMap Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Name` | NMTOKEN | `@Name` SHALL define the Partition Key that SHALL be filled with the value that is calculated from `@ValueFormat` and `@ValueTemplate`. See Table 6.4 Part Element. |
| `@ValueFormat` | string | Formatting value for combining values from `@ValueTemplate` into a dynamic result value. Allowed values are from: Appendix D String Generation. |
| `@ValueTemplate` | NMTOKENS | Arguments for combining extracted values. If MetadataMap is a child of RunList, then each value shall be selected from the list of predefined values in Appendix D String Generation or match a value of `Expr/@Name`. If MetadataMap is a child of IdentificationField, each value shall be defined in the parent `IdentificationField/@ValueTemplate`. |
| `Expr*` | element | Exactly one Expr element with a matching `@Name` SHALL be specified for each variable in `@ValueTemplate` that is NOT defined in the parent `IdentificationField/@ValueTemplate` and NOT defined in Table D.1 Template Variables. Expr SHALL NOT be specified in an `IdentificationField/MetadataMap`. |

### 8.29.1 Expr

Expr elements define how the variables that are specified in `@ValueTemplate` SHALL be extracted from the parent RunList.

**Table 8.47: Expr Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Name` | NMTOKEN | Name of this Expr. The value extracted from `@Path` SHALL be used to evaluate the parent `@ValueTemplate`. |
| `@Path` | XPath | The value specified by this path SHALL be assigned to `Expr/@Name`. If the XPath points to an element, then an implied XPath `text()` function SHALL be executed. |

**Example 8.6: RunList/MetadataMap**

In the following example, the MetadataMap element maps the data in `/doc/record/Geschlecht` and `/doc/record/Status` in the document to `Part/@Metadata`. The calculated `Part/@Metadata` is then used to select the appropriate color and quality of the paper component.

```xml
<ResourceSet Name="RunList" Usage="Input">
  <Resource>
    <RunList>
      <FileSpec URL="file://host/file/data.pdf"/>
      <MetadataMap Name="Metadata" ValueFormat="%s_%s" ValueTemplate="gender status">
        <Expr Name="gender" Path="/doc/record/Geschlecht"/>
        <Expr Name="status" Path="/doc/record/Status"/>
      </MetadataMap>
    </RunList>
  </Resource>
</ResourceSet>
<ResourceSet Name="Component" Usage="Input">
  <Resource ExternalID="BlueGoodPaper">
    <Part Metadata="Mann_Platin"/>
    <Component/>
  </Resource>
  <Resource ExternalID="BlueCheapPaper">
    <Part Metadata="Mann(.)\*"/>
    <Component/>
  </Resource>
  <Resource ExternalID="PinkGoodPaper">
    <Part Metadata="Frau_Platin"/>
    <Component/>
  </Resource>
  <Resource ExternalID="PinkCheapPaper">
    <Part Metadata="Frau_(.)\*"/>
    <Component/>
  </Resource>
</ResourceSet>
```

**Example 8.7: IdentificationField/MetadataMap**

In the following example, barcodes are scanned on a sheet to verify that all sheets have been produced. The three MetadataMap elements map the data that is extracted from a barcode to `XJDF/@JobID`, `Part/@DocIndex` and `Part/@SheetIndex`. The first six characters are read into a virtual string variable "job", which is appended to a fixed string "Job_", to generate the value for `XJDF/@JobID`. The next three characters are read into a virtual integer variable "doc", which is used twice to generate a blank separated range value for `Part/@DocIndex`. The final two characters are read into a virtual integer variable "sheet", which is used twice to generate a blank separated range value for `Part/@SheetIndex`.

Thus the barcode "Dec00704216" would generate:

- `XJDF/@JobID="Job_Dec007"`
- `Part/@DocIndex="42 42"`
- `Part/@SheetIndex="16 16"`

```xml
<XJDF xmlns="http://www.CIP4.org/JDFSchema_2_0" JobID="Barcode" JobPartID="Metadata" Types="Verification">
  <ResourceSet Name="Component" Usage="Input">
    <Resource>
      <Component>
        <IdentificationField ValueFormat="%6s%3i%2i" ValueTemplate="job doc sheet">
          <MetadataMap Name="JobID" ValueFormat="Job_%s" ValueTemplate="job"/>
          <MetadataMap Name="DocIndex" ValueFormat="%i%i" ValueTemplate="doc doc"/>
          <MetadataMap Name="SheetIndex" ValueFormat="%i%i" ValueTemplate="sheet sheet"/>
        </IdentificationField>
      </Component>
    </Resource>
  </ResourceSet>
</XJDF>
```

---

## 8.30 MISDetails

MISDetails is a container for MIS related information.

**Element Properties**

- Element referenced by: `ResourceInfo`, `PipeParams`, `JobPhase`, `NodeInfo`

**Table 8.48: MISDetails Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Complexity?` | float | Complexity of the task specified by this XJDF in a range from 0.0 to 1.0. **Note:** The interpretation of values is implementation dependent. Values include: `0.0` – The job is simple and therefore reduced setup and waste or higher speeds are possible. `0.5` – The job is of standard complexity and therefore standard setup and waste or normal speeds are possible. `1.0` – The job is complex and therefore more setup and waste or lower speeds are possible. |
| `@CostType?` | enumeration | Specifies whether or not this MISDetails is chargeable to the customer or not. Allowed values are: `Chargeable`, `NonChargeable`. |
| `@WorkType?` | enumeration | Definition of the work type for this MISDetails (i.e., whether or not this MISDetails relates to originally planned work, an alteration or rework). Allowed values are: `Alteration` – Work done to accommodate a change made to the job. `Original` – Standard work that was originally planned for the job. `Rework` – Work done due to unforeseen problems with the original work (bad plate, resource damaged, etc.). |
| `@WorkTypeDetails?` | NMTOKEN | Machine readable definition of the details of the work type for this MISDetails (i.e., why the work was done). Values include: `CustomerRequest` – The customer requested change(s) requiring the work. `EquipmentMalfunction` – Equipment used to produce the resource malfunctioned; resource needs to be created again. `InternalChange` – Change was made for production efficiency or other internal reason. `ResourceDamaged` – A resource needs to be created again to account for a damaged resource (damaged plate, etc.). `UserError` – Incorrect operation of equipment or incorrect creation of resource requires creating the resource again. |

---

## 8.31 Notification

This element contains information about individual events that occurred during processing. For a detailed discussion of event properties, see Section 9.3.8 Error Handling.

**Element Properties**

- Element referenced by: `AuditNotification`, `Response`, `ResponseForceGang`, `ResponseGangStatus`, `ResponseKnownDevices`, `ResponseKnownMessages`, `ResponseKnownSubscriptions`, `ResponseModifyQueueEntry`, `ResponseNotification`, `SignalNotification`, `ResponsePipeControl`, `ResponseQueueStatus`, `ResponseRequestQueueEntry`, `ResponseResource`, `ResponseResubmitQueueEntry`, `ResponseReturnQueueEntry`, `ResponseShutDown`, `ResponseStatus`, `ResponseStopPersistentChannel`, `ResponseSubmitQueueEntry`, `ResponseWakeUp`

**Table 8.49: Notification Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Class` | enumeration | Class of the notification. Allowed value is from: Severity. |
| `@JobID?` | NMTOKEN | `@JobID` that this Notification applies to. |
| `@JobPartID?` | NMTOKEN | `@JobPartID` that this Notification applies to. |
| `@ModuleID?` | NMTOKEN | `@ModuleID` of the Module that this Notification relates to. |
| `@QueueEntryID?` | NMTOKEN | `@QueueEntryID` of the QueueEntry during which this Notification was generated. |
| `Comment*` | element | A Comment element contains a verbose, human-readable description of the Notification. If multiple Comment elements occur, they SHALL have different `Comment/@Language` values. |
| `Event?` | element | See Event element below. Not more than one of Event and Milestone SHALL be specified. |
| `Milestone?` | element | See Milestone element below. Not more than one of Event and Milestone SHALL be specified. If Milestone is present, the value of `@Class` SHALL be `"Event"`. |
| `Part*` | element | Describes which parts of a process this Notification belongs to. If Part is not specified for a Notification, it refers to all parts. |
| `<foreign namespace elements>*` | element | Any elements in a foreign namespace. Foreign namespace extensions SHOULD NOT duplicate functionality of XJDF. |

### 8.31.1 Milestone

In addition to the concrete XJMF feedback with respect to process status (see Section 7.18 Status) and available/consumed resources (see Section 7.14 Resource), many actors in the workflow want to track certain overall milestones concerning the entire job across all resources and processes in order to display this to the operator. Sometimes the XJMF recipients cannot determine these milestones from the detailed XJDF/XJMF, therefore a more abstract representation of job status is described by Milestone events.

> **Note:** Milestone elements usually refer to events involving multiple objects, although `Milestone/@MilestoneType` is specified as a singular. The scope of the Milestone is defined by the parent Notification element.

**Table 8.50: Milestone Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@MilestoneType` | NMTOKEN | Type of Milestone. Values include those from: Milestones. |
| `@TypeAmount?` | integer | Indication of how many elements have been processed if the milestone refers to certain resources, e.g. the number of pages proofed or the number of different printed sheets. It is not the cumulative amount. |

---

## 8.32 ObjectResolution

ObjectResolution defines a resolution depending on `@SourceObjects` data types.

**Element Properties**

- Element referenced by: `InterpretingParams`, `RenderingParams`

**Table 8.51: ObjectResolution Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@AntiAliasing?` | NMTOKEN | Indicates the anti-aliasing algorithm that the Device SHALL apply to the rendered output images. An anti-aliasing algorithm causes lines and curves to appear smooth which would otherwise have a jagged appearance, especially at lower resolutions such as 300 dpi and lower. Values include: `AntiAlias` – Anti-aliasing SHALL be applied. The algorithm is system specified. `None` – Anti-aliasing SHALL NOT be applied. |
| `@Resolution` | XYPair | Horizontal and vertical output resolution in DPI. |
| `@SourceObjects?` | enumerations | Identifies the class(es) of incoming graphical objects to render at the specified resolution. If `@SourceObjects` is not specified then ObjectResolution SHALL apply to all object classes. Allowed values are from: SourceObjects. |

---

## 8.33 OCGControl

OCGControl defines the policy for including or excluding layers that are encoded as 'Optional Content Groups' (OCGs) in PDF.

The order of OCGControl elements SHALL have no effect; the Z-order of graphic elements that make up each optional content group (the term layer is misleading in this regard) within the PDF file SHALL define the drawing order of those graphic elements.

Any preferences recorded in an OCG within the PDF file as to whether that OCG SHOULD be displayed or not SHALL be ignored if that OCG is referenced from an OCGControl element.

The state of all OCGs explicitly referenced from OCGControl elements SHALL be set before determining the state of any remaining OCGs.

> **Note:** All controls for OCGs in XJDF address OCGs directly, and not Optional Content Member Dictionaries (OCMDs do not have unique names).

> **Note:** [PDF1.6] does not state that all OCGs SHALL have unique names. It is therefore possible for a single PDF file to contain multiple OCGs with the same name. When `OCGControl/@OCGName` refers to multiple OCGs in a file, they will all be explicitly included or excluded together.

**Element Properties**

- Element referenced by: `Content`, `InterpretingParams/PDFInterpretingParams`

**Table 8.52: OCGControl Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@IncludeOCG` | boolean | Defines whether the optional content group(s) identified by `@OCGName` SHALL be included in the RunList. If `"true"`, then the layer SHALL be included. If `"false"`, it SHALL NOT. The contents stream of excluded OCGs SHALL still be interpreted so that changes to CTM, etc., are acted on. The objects drawn in excluded OCGs SHALL NOT be rendered. |
| `@OCGName?` | string | The name of the optional content group(s) that SHALL be included or excluded. Exactly one of `@OCGName` or `@ProcStepsGroup` SHALL be present. **Note:** The Name attribute of an optional content group entry is encoded as a PDF text string, and `@OCGName` is encoded with the Unicode variant identified in the XJDF file header; names SHALL be re-encoded as necessary for comparison. |
| `@ProcStepsGroup?` | NMTOKEN | An OCG is selected, if `@ProcStepsGroup` matches the value of GTS_ProcStepsGroup in the GTS_Metadata dictionary of the OCG of a PDF that complies with [ISO19593-1:2016]. |
| `@ProcStepsType?` | NMTOKEN | If specified, an OCG is selected, if `@ProcStepsType` matches the value of GTS_ProcStepsType in the GTS_Metadata dictionary of the OCG of a PDF that complies with [ISO19593-1:2016]. `@ProcStepsType` SHALL NOT be specified unless `@ProcStepsGroup` is present. |

---

## 8.34 Perforate

Perforate describes one perforated line.

**Element Properties**

- Element referenced by: `FoldingParams`, `PerforatingParams`

**Table 8.53: Perforate Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Depth?` | float | Depth of the perforation, in microns [µm]. |
| `@StartPosition?` | XYPair | Starting position of the tool. |
| `@TeethPerDimension?` | float | Number of teeth in a given perforation extent in teeth/point. MicroPerforation is defined by specifying a large number of teeth (`@TeethPerDimension > 1000`). |
| `@WorkingDirection?` | enumeration | Direction from which the tool is working. Allowed value is from: WorkingDirection. |
| `@WorkingPath?` | XYPair | Working path of the tool beginning at `@StartPosition`. |

---

## 8.35 QueueEntry

The QueueEntry element contains metadata for a single item in a Device's queue.

**Element Properties**

- Element referenced by: `ResponseModifyQueueEntry`, `ResponseQueueStatus/Queue`, `ResponseSubmitQueueEntry`

**Table 8.54: QueueEntry Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Activation?` | enumeration | Specifies the activation of the QueueEntry. Allowed value is from: Activation. |
| `@EndTime?` | dateTime | Date and time when processing of the QueueEntry has been ended. |
| `@GangName?` | NMTOKEN | Name of the Gang that this QueueEntry belongs to. `@GangName` SHALL be specified, if the QueueEntry is a candidate member of a Gang job. |
| `@GangPolicy?` | enumeration | Ganging policy for the QueueEntry. Allowed value is from: GangPolicy. |
| `@JobID?` | NMTOKEN | The `@JobID` of the XJDF process. |
| `@JobPartID?` | NMTOKEN | The `@JobPartID` of the XJDF process. |
| `@Priority?` | integer | Priority of the QueueEntry. Values are 0-100. A value of `"0"` is the lowest priority, while `"100"` is the highest priority. |
| `@RelatedJobID?` *(New in XJDF 2.1)* | string | The `@RelatedJobID` of the XJDF process. |
| `@RelatedJobPartID?` *(New in XJDF 2.1)* | string | The `@RelatedJobPartID` of the XJDF process. |
| `@QueueEntryID` | NMTOKEN | Identifier of a QueueEntry. This ID SHALL be generated by the queue owner. `@QueueEntryID` SHALL be unique in the context of a Queue. |
| `@StartTime?` | dateTime | Date and time when processing of the QueueEntry has been started. |
| `@Status` | enumeration | Specifies the status of the requested QueueEntry. `@Status` SHALL be identical to the `NodeInfo/@Status` of the underlying XJDF. Allowed value is from: Status. |
| `@StatusDetails?` | NMTOKEN | `@StatusDetails` provides additional details on the status of the QueueEntry. Values include those from: Status Details. |
| `@SubmissionTime?` | dateTime | Date and time when the entry was submitted to the queue. |
| `FileSpec(Preview)?` | element | This FileSpec MAY be used to provide a visualization of the QueueEntry. `FileSpec(Preview)` SHOULD reference an image format such as PNG or JPEG. |
| `GangSource*` | element | If present, each GangSource SHALL represent the source jobs that are being processed as a Gang job by this QueueEntry. |
| `Part*` | element | Describes which parts of a job were submitted to the queue. This SHALL be a copy of `ResourceSet[@Name="NodeInfo"]/Resource/Part`. |

---

## 8.36 QueueFilter

*Modified in XJDF 2.2*

The QueueFilter element defines a filter that selects QueueEntry elements in a Queue. The supplied elements of the QueueFilter define a matching criteria that is a logical "and". Only QueueEntry elements that match all restrictions specified by the QueueFilter SHALL be selected.

An empty or missing QueueFilter element NEED NOT select any QueueEntry elements in the Queue; the resulting action is implementation dependent.

> **Note:** This behavior allows implementations to ensure that defective requests do not have far reaching consequences, e.g., accidentally flushing an entire Queue.

**Modification note:** The behavior of an empty or missing QueueFilter element was clarified in XJDF 2.2.

**Element Properties**

- Element referenced by: `CommandModifyQueueEntry/ModifyQueueEntryParams`, `QueryQueueStatus/QueueStatusParams`

**Table 8.55: QueueFilter Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@FirstEntry?` | NMTOKEN | `@QueueEntryID` of the first QueueEntry that this QueueFilter applies to. Only QueueEntry elements that are behind this (including this) QueueEntry in the current queue sorting SHALL be selected. If not specified, there is no filtering based on the position of items in the queue. |
| `@GangNames?` | NMTOKENS | Gang names of the QueueEntry elements to be returned. If not specified, there is no filtering on `QueueEntry/@GangName`. |
| `@JobID?` | NMTOKEN | Return only QueueEntry elements with specified `@JobID`. If not specified, there is no filtering on `QueueEntry/@JobID`. |
| `@JobPartID?` | NMTOKEN | Return only QueueEntry elements with specified `@JobPartID`. If not specified, there is no filtering on `QueueEntry/@JobPartID`. |
| `@LastEntry?` | NMTOKEN | `@QueueEntryID` of the last QueueEntry that this QueueFilter applies to. Only QueueEntry elements that are in front of this (including this) QueueEntry in the current queue sorting SHALL be selected. If not specified, there is no filtering based on the position of items in the queue. |
| `@MaxEntries?` | integer | Maximum number of QueueEntry elements to provide in the Queue element. If not specified, fill in all matching QueueEntry elements. |
| `@MaxPriority?` | integer | Only QueueEntry elements with a `@Priority` lower than or equal to the value of `@MaxPriority` SHALL be provided in the Queue element. If not specified, there is no `@Priority` upper bound on candidates. |
| `@MinPriority?` | integer | Only QueueEntry elements with a `@Priority` higher than or equal to the value of `@MinPriority` SHALL be provided in the Queue element. If not specified, there is no `@Priority` lower bound on candidates. |
| `@NewerThan?` | dateTime | Only QueueEntry elements with a `@SubmissionTime` newer than or equal to `@NewerThan` SHALL BE provided in the Queue element. If not specified, there is no dateTime upper bound on candidates. |
| `@OlderThan?` | dateTime | Only QueueEntry elements with a `@SubmissionTime` older than or equal to `@OlderThan` SHALL BE provided in the Queue element. If not specified, there is no dateTime lower bound on candidates. |
| `@QueueEntryIDs?` | NMTOKENS | Defines an explicit list of queue entries. If not specified, all entries in the Queue are considered. |
| `@StatusList?` | enumerations | Only QueueEntry elements with a `@Status` matching one of the entries in `@StatusList` SHALL be returned. If not specified, there is no filtering on `QueueEntry/@Status`. Allowed values are from: Status. |
| `GangSource*` | element | If present only QueueEntry elements that contain a GangSource element that matches at least one of these GangSource elements SHALL be selected. If not specified, there is no filtering based on GangSource. |
| `Part*` | element | Only QueueEntry elements with all specified Part elements SHALL be returned. If not specified, there is no filtering on `QueueEntry/Part`. |

---

## 8.37 RefAnchor

RefAnchor describes the relative position with respect to a related element in a layout. Depending on the value of `@AnchorType`, it specifies either a parent element or a sibling element.

**Element Properties**

- Element referenced by: `Content/PositionObj`, `Layout/StripMark`

**Table 8.56: RefAnchor Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Anchor` | enumeration | `@Anchor` specifies the origin (0,0) of the vector specified in the rotated coordinate system of the related layout element. Allowed value is from: Anchor. |
| `@AnchorType` | enumeration | Role of this RefAnchor. Allowed values are: `Parent` – The layout element referenced by this RefAnchor is a parent. This layout element is transformed with the parent. `Sibling` – The layout element referenced by this RefAnchor is a sibling. Both layout elements share a common parent. The parent of this layout element SHALL be specified as the RefAnchor of the first child in the chain of siblings. |
| `@rRef` | IDREF | Reference to a layout element that this layout element is positioned relative to. This shall be one of `Layout/@ID`, `StripMark/@ID` or `Position/@ID`. |

---

## 8.38 RegisterMark

RegisterMark defines a register mark, which can be used for setting up and monitoring color registration in a printing process. It can also be used to synchronize the sheet position in a paper path.

**Element Properties**

- Element referenced by: `Layout/MarkObject`, `StripMark`

**Table 8.57: RegisterMark Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Center?` | XYPair | Position of the center of the register mark in the coordinates of the object that contains this mark. `@Center` SHALL be calculated as the center of the bounding rectangle for the RegisterMark. |
| `@MarkName?` *(New in XJDF 2.1)* | NMTOKEN | Name of the RegisterMark. `@MarkName` MAY be used for specifying a pre-defined, workflow specific combined register mark. See `StripMark/@MarkName`. |
| `@MarkType?` *(Deprecated in XJDF 2.1)* | NMTOKENS | Type of RegisterMark. **Note:** Marks can be combined to form a composite mark, see Figure 8-1: Combining Mark Types. Values include those from: Table 8.59 MarkType Attribute Values. **Deprecation note:** Use `MarkElement/@MarkType`. |
| `@MarkUsage?` | enumerations | Specifies the usage of the RegisterMark. Allowed values are: `Color` – The mark is used for separation color registration. `PaperPath` – The mark is used for paper path synchronization. `Tile` – The mark is used to mark the position of tiles. |
| `@Rotation?` | float | Rotation in degrees. Positive values indicate counter-clockwise rotation; negative values indicate clockwise rotation. |
| `@Separations?` *(Deprecated in XJDF 2.1)* | NMTOKENS | Set of separation identifiers to which the register mark is bound. Additional details of the colorants SHOULD be provided in `ResourceSet[@Name="Color"]`. **Deprecation note:** Use `MarkElement/@Separation`. |
| `@Size?` *(New in XJDF 2.1)* | XYPair | Size of the outer bounding box of the unrotated RegisterMark. |
| `MarkElement*` *(New in XJDF 2.1)* | element | MarkElement describes an element of a combined RegisterMark. |

### 8.38.1 MarkElement

*New in XJDF 2.1*

MarkElement describes an individual element of a combined RegisterMark.

> **Note:** Marks can be combined to form a composite mark, see Figure 8-1: Combining Mark Types.

**Table 8.58: MarkElement Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Center` | XYPair | Position of the center of the MarkElement in the coordinates of the parent RegisterMark. `@Center` SHALL be calculated as the center of the bounding rectangle for the MarkElement. |
| `@MarkType` | NMTOKEN | Geometry of the MarkElement. Values include those from: Table 8.59 MarkType Attribute Values. |
| `@Rotation?` | float | Rotation of the MarkElement in degrees. Positive values SHALL indicate counter-clockwise rotation; negative values SHALL indicate clockwise rotation. |
| `@Separation` | NMTOKEN | Separation identifier to which the MarkElement is bound. Additional details of the colorant SHOULD be provided in a `ResourceSet[@Name="Color"]`. |
| `@Size?` | XYPair | Size of the outer bounding box of the unrotated MarkElement. |

### 8.38.2 Register MarkType

Register mark types specifies the types of printers mark used to aid registration.

**Table 8.59: MarkType Attribute Values**

| Value | Image | Description |
|-------|-------|-------------|
| `Arc` | *(Image: An arc shape)* | An arc, upper right quadrant. |
| `Circle` | *(Image: A circle shape)* | A circle. |
| `Cross` | *(Image: A cross shape)* | A cross. |
| `Dot` | *(Image: Small filled circles)* | Small filled circles that are typically arranged in a well defined matrix. |
| `Rectangle` | *(Image: A filled rectangle)* | Filled rectangle. **Note:** Lines are rectangles with a small X or Y value. |

### 8.38.3 Combined Register Mark

The following figure illustrates a combined RegisterMark consisting of an arc, a cross and a circle as well as the individual mark components.

> **Figure 8-1: Combining Mark Types**
>
> *(Image description: The figure shows two examples of combining mark types. Left: Combining Arc, Cross and Circle to create a 'target' mark. Right: Combining Dots of different colors to create a sheet registration mark. The individual components Arc, Cross, Circle are labeled.)*

---

## 8.39 RegisterRibbon

Description of a register ribbon used for book binding. The relationship of visible, hidden and overall length of the register ribbon is shown in Figure 8-2: RegisterRibbon lengths and coordinate system for BlockPreparation.

**Element Properties**

- Element referenced by: `BindingIntent/HardCoverBinding`, `BlockPreparationParams`

**Table 8.60: RegisterRibbon Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@LengthOverall?` | float | Overall length of the register ribbon. See Figure 8-2: RegisterRibbon lengths and coordinate system for BlockPreparation. |
| `@Material?` | string | Material of the register ribbon. |
| `@RibbonColor?` | enumeration | `@RibbonColor` specifies the Machine readable color of ribbon. Allowed value is from: NamedColor. |
| `@RibbonColorDetails?` | string | A more specific, specialized or site-defined name for the color. If `@RibbonColorDetails` is supplied, `@RibbonColor` SHOULD also be supplied. |
| `@RibbonEnd?` | NMTOKEN | End of the Ribbon. Values include: `Cut`, `CutSealed`, `Knot`, `SealedOffset` – The ribbon is sealed some distance from the cut. |
| `@VisibleLength?` | float | Length of the register ribbon that will be seen when opening the book. See Figure 8-2: RegisterRibbon lengths and coordinate system for BlockPreparation. |

> **Figure 8-2: RegisterRibbon lengths and coordinate system for BlockPreparation**
>
> *(Image description: The figure shows a book block with a register ribbon. It illustrates the following: Hidden length = LengthOverall - VisibleLength, Hidden length, Origin of the process coordinate system, X and Y axes, VisibleLength. The book block is shown with the ribbon extending from it, with the hidden portion inside the book block and the visible portion extending out.)*

---

## 8.40 RegistrationQuality

*New in XJDF 2.1*

RegistrationQuality defines a measurement of the color separation compared to a master separation that is defined by `@Reference`.

**Element Properties**

- Element referenced by: `QualityControlParams`, `QualityControlResult`

**Table 8.61: RegistrationQuality Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Offset` | XYPair | Registration offset compared to the color separation specified in `@Reference`. |
| `@Reference` | NMTOKEN | Separation identifier of the color separation that `@Offset` SHALL be measured against. |

---

## 8.41 RuleLength

Elements describing the length of die rules for the different types of rules. Each RuleLength element describes the accumulated length of all rules of a certain type.

**Element Properties**

- Element referenced by: `DieLayout`, `ShapeDef`

**Table 8.62: RuleLength Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@DDESCutType` | integer | `@DDESCutType` specifies the type of rule. A value from Appendix A.4.1 DDES3 Diecutting Data SHOULD be used. See [DDES3]. |
| `@Length` | float | `@Length` specifies the accumulated length, in points, of all of the rules of this type in the parent resource. |

---

## 8.42 ScavengerArea

ScavengerArea describes a scavenger area for removing excess ink from printed sheets.

**Element Properties**

- Element referenced by: `Layout/MarkObject`, `StripMark`

**Table 8.63: ScavengerArea Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Center` | XYPair | Position of the center of the scavenger area in the coordinates of the object that contains this mark. |
| `@Separations?` | NMTOKENS | Set of separation identifiers to which the scavenger area is bound. Additional details of the colorants SHOULD be provided in `ResourceSet[@Name="Color"]`. |
| `@Size?` | XYPair | Size of the scavenger area. |

---

## 8.43 ScreenSelector

Description of screening for a selection of source object types and separations.

**Element Properties**

- Element referenced by: `ColorSpaceConversionOp`, `Content`, `ScreeningParams`

**Table 8.64: ScreenSelector Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Angle?` | float | Specifies the first angle of the screen when AM screening is used, otherwise `@Angle` is ignored. |
| `@DotSize?` | float | Specifies the dot size of the screen, in microns [µm], when FM screening (`@ScreeningType="FM"` or `"Adaptive"`) is used. |
| `@Frequency?` | float | Specifies the halftone screen frequency in lines per inch (lpi) of the screen when AM screening is used, otherwise `@Frequency` is ignored. With some screens, frequency can change as a function of gray level. In this case, the `@Frequency` value is interpreted for a mid tone (50%) gray level. |
| `@ScreeningFamily?` | string | Vendor specific screening family name. |
| `@ScreeningType?` | enumeration | General type of screening. Allowed values are: `Adaptive`, `AM` – Can be line or dot. See `@SpotFunction`. `ErrorDiffusion`, `FM` – Includes all stochastic screening types. `HybridAM-FM`, `HybridAMline-dot`. |
| `@Separation?` | NMTOKEN | The separation identifier that this ScreenSelector SHALL apply to. Additional details of the colorants SHOULD be provided in `ResourceSet[@Name="Color"]`. |
| `@SourceFrequencyMax?` | float | Specifies the maximum line frequency of screens that SHALL be matched from the source file when screen matching is to be done. **Note:** This is a filter that selects on which objects to apply this ScreenSelector. |
| `@SourceFrequencyMin?` | float | Specifies the minimum line frequency of screens that SHALL be matched from the source file when screen matching is to be done. **Note:** This is a filter that selects on which objects to apply this ScreenSelector. |
| `@SourceObjects?` | enumerations | Identifies the class(es) of incoming graphical objects on which to use the selected screen. If `@SourceObjects` is not specified then ScreenSelector SHALL apply to all object classes. Allowed values are from: SourceObjects. |
| `@SpotFunction?` | NMTOKEN | Specifies the spot function of the screen when AM screening is used. In general, it is common for a spot function to change its shape as a function of gray level. Response to these spot function names MAY be implementation-dependent. These example names are the same as the spot function names defined in PDF. Values include: `CosineDot`, `Cross`, `Diamond`, `Double`, `DoubleDot`, `Ellipse`, `EllipseA`, `EllipseB`, `EllipseC`, `InvertedDouble`, `InvertedDoubleDot`, `InvertedEllipseA`, `InvertedEllipseC`, `InvertedSimpleDot`, `Line`, `LineX`, `LineY`, `Rhomboid`, `Round`, `SimpleDot`, `Square`. |

---

## 8.44 Shape

**Element Properties**

- Element referenced by: `ShapeCuttingParams`, `ShapeDef`

**Table 8.65: Shape Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@CutBox?` | rectangle | Specification of a rectangular window. |
| `@CutOut?` | boolean | If `"true"`, the inside of a specified shape SHALL be removed. If `"false"`, the outside of a specified shape SHALL be removed. An example of an inside shape is a window. An example of an outside shape is a shaped greeting card. |
| `@CutPath?` | PDFPath | Specification of a complex path. |
| `@DDESCutType?` | integer | `@DDESCutType` specifies the type of cut or perforation used. See Appendix A.4.1 DDES3 Diecutting Data for permissible values. See [DDES3]. |
| `@ShapeDepth?` | float | Depth of the shape cut, measured in microns [µm]. If not specified, the shape SHALL be completely cut. |
| `@ShapeType` | enumeration | Describes any precision cutting other than hole making. Allowed values are: `Path`, `Rectangular`, `Round`, `RoundedRectangle` – Rectangle with rounded corners. |
| `@TeethPerDimension?` | float | Number of teeth in a given perforation extent, in teeth/point. MicroPerforation is defined by specifying a large number of teeth (n > 1000). |

---

## 8.45 StripMark

The StripMark element specifies automatically generated production marks.

Whereas `Layout/MarkObject` elements define the explicit and detailed positions of production marks, StripMark elements are generally high level instructions to a Stripping processor to appropriately place the resulting MarkObject elements during the Stripping process.

**Element Properties**

- Element referenced by: `Layout`, `StackingParams/InsertSheet`

**Table 8.66: StripMark Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@AbsoluteHeight?` | float | Absolute height of the StripMark in points. |
| `@AbsoluteWidth?` | float | Absolute width of the StripMark in points. |
| `@Anchor?` | enumeration | Origin of the mark coordinate system. Allowed value is from: Anchor. |
| `@Font?` | NMTOKEN | The name of the font that SHALL be used for the StripMark. Values include: `Courier`, `Helvetica`, `Helvetica-Condensed`, `Times-Roman`. |
| `@FontSize?` | float | The size of the font that SHALL be used for the StripMark, in points. |
| `@HorizontalFitPolicy?` | enumeration | How to modify the mark to fit into the horizontal space that is provided for the StripMark prior to rotation. Allowed value is from: FitPolicy. |
| `@ID?` | ID | Identifier of the StripMark. Used for internal references within the Layout. |
| `@MarkName?` | NMTOKEN | Name of the mark that SHALL be marked on the Layout. Values include those from: Table 8.67 MarkName Attribute Values. |
| `@Offset?` | XYPair | Position of the anchor of this StripMark relative to `RefAnchor/@Anchor` as defined by `@Anchor` and `RefAnchor/@Anchor`. |
| `@Orientation?` | enumeration | Orientation of this StripMark in the coordinate system of the related object defined by RefAnchor. Allowed value is from: Orientation. |
| `@RelativeHeight?` | float | Height relative to the height of the related object defined by RefAnchor of this StripMark. |
| `@RelativeWidth?` | float | Width relative to the width of the related object defined by RefAnchor of this StripMark. |
| `@StripMarkDetails?` | string | More detailed information about the StripMark. For example, if `@MarkName="Set"` or `@MarkName="TabMark"` then `@StripMarkDetails` is a name to refer to a proprietary or site specific set of marks. |
| `@VerticalFitPolicy?` | enumeration | Policy of how to modify the mark to fit into the vertical space that is provided for the StripMark prior to rotation. Allowed value is from: FitPolicy. |
| `BarcodeReproParams?` | element | Description of the formatting and reproduction parameters for dynamically generated barcodes. |
| `ColorControlStrip*` *(New in XJDF 2.1)* | element | ColorControlStrip describes a color control strip. |
| `CutMark*` *(New in XJDF 2.1)* | element | CutMark describes cut marks on a sheet. |
| `FillMark*` | element | Each FillMark specifies a fill layer that SHALL be completely filled, e.g. for backlit displays. |
| `IdentificationField*` | element | Contents of barcodes. |
| `JobField?` | element | Details of automatically generated text marks. JobField SHOULD NOT be specified unless `@MarkName="JobField"`, `@MarkName="TabMark"` or `@MarkName="WaterMark"`. |
| `RefAnchor?` *(Modified in XJDF 2.1)* | element | Reference to an element that defines the coordinate system that this mark SHALL be placed relative to. If not specified, the StripMark is defined in the parent coordinate system. **Modification note:** From XJDF 2.1 RefAnchor is optional if the StripMark is defined in the default parent coordinate system. |
| `RegisterMark*` *(New in XJDF 2.1)* | element | RegisterMark describes a register mark that can be used to measure color registration. |
| `ScavengerArea*` *(New in XJDF 2.1)* | element | ScavengerArea describes a scavenger area for removing excess ink from printed sheets. |

> **Figure 8-3: Anchor with no scaling and rotation of 90º clockwise**
>
> *(Image description: The figure shows a text bounding box with the text "This is a line of TEXT." The Anchor point is shown as the point about which the Rotation is applied. RefAnchor/@Anchor = BottomLeft is indicated. The StripMark/@Anchor = BottomCenter position is shown. StripMark/@Orientation = Rotate270 is indicated. StripMark/@Offset is shown with an arrow pointing to the offset position.)*

**Table 8.67: MarkName Attribute Values**

| Value | Description |
|-------|-------------|
| `BleedMark` | Marks that indicate the zone beyond the trim in which content is printed and later removed. |
| `CenterMark` | Marks, usually a thin line, used to indicate the center of a trim margin or used to assist with registration. |
| `CIELABMeasuringField` *(Deprecated in XJDF 2.1)* | **Deprecation note:** See `Patch`. |
| `CollationMark` | Marks, usually a numbered symbol on the folded edge of a signature, used to indicate the required collating or gathering sequence. |
| `ColorControlStrip` | A test strip comprising a series of grayscale and/or color patches to assist in ensuring proper and uniform color balance. |
| `ColorRegisterMark` | Marks used to ensure correct register or alignment of successive color separations. |
| `CutMark` | Marks used as a guide to cutting. |
| `DensityMeasuringField` *(Deprecated in XJDF 2.1)* | **Deprecation note:** See `Patch`. |
| `FillMark` | Marks that specify a fill layer that SHALL be completely filled, e.g. for backlit displays. |
| `FoldMark` | Marks used as a guide for post press folding. |
| `GrommetMark` | Marks that describe the type and position for grommets (e.g., for banners). Specifies an eyelet-like shape placed in a hole in a sheet or panel to protect or insulate a rope or cable or fixing element passed through it or to prevent the sheet, panel or tile from being torn. Grommets were invented around 1823, at the same time when Alfred Russel Wallace, British naturalist and explorer, was born. |
| `IdentificationField` | Marks used for OCR based verification or document separation. |
| `JobField` | Marks used to contain details about the job. |
| `PaperPathRegisterMark` | Marks used to assist in the routing of the substrate through a press. |
| `RegisterMark` | Marks used to ensure correct register or alignment of successive colors and/or images. |
| `ScavengerArea` | Marks used to identify the scavenger area. |
| `Set` | Specifies to use a MarkSet (file containing multiple marks). The name of the MarkSet MAY be passed in `@StripMarkDetails`. |
| `TabMark` | Specifies automatically generated data for tab blocks. |
| `TrimMark` | Marks used to indicate the proper cropping of the product. |
| `WaterMark` | A faint design superimposed as a lighter background to text or images. Typically used for protection. |

### 8.45.1 FillMark

**Table 8.68: FillMark Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@KnockoutBleed?` | float | Bleed in points that the fill SHALL grow into (positive values) from the knockout area. **Note:** This attribute implies the same bleed for all separations. |
| `@KnockoutRefs?` | IDREFS | Reference to the PlacedObject, Position or StripMark elements that SHALL not be filled by this FillMark. The knockout boundaries are defined by the value of `@KnockoutSource`. |
| `@KnockoutSource` | enumeration | Definition of the source of the knockout from the referenced PlacedObject elements. Allowed values are: `ClipPath` – Use the clip path as defined by the referenced `PlacedObject/@ClipPath`. `SourceClipPath` – Use the clip path as defined by the referenced `PlacedObject/@SourceClipPath`. `TrimBox` – Use the clip path as defined by the referenced `PlacedObject/@TrimCTM` and `PlacedObject/@TrimSize`. |
| `MarkColor+` | element | Definition of the separations used to fill the mark. |

### 8.45.2 MarkColor

Definition of the separations used to fill a dynamic mark.

**Table 8.69: MarkColor Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@Name` | string | Identifier of the separation. Additional details SHOULD be specified in `ResourceSet[@Name="Color"]`. |
| `@Tint` | float | Value from 0 (not used) to 1 (100% tint) of the separation specified in `@Name`. |

### 8.45.3 JobField

A JobField is a mark object that specifies the details of a job. JobField elements are also referred to as slug lines.

**Table 8.70: JobField Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@JobFormat?` | string | A formatting string used with `@JobTemplate` to generate a string. Allowed values are from: Appendix D String Generation. |
| `@JobTemplate?` | NMTOKENS | A list of values used with `@JobFormat` to generate a string. Values include those from: Appendix D String Generation. |

---

## 8.46 SubscriptionInfo

A SubscriptionInfo element describes the details of existing subscriptions.

**Element Properties**

- Element referenced by: `ResponseKnownSubscriptions`, `SignalKnownSubscriptions`, `ResponseStopPersistentChannel`

**Table 8.71: SubscriptionInfo Element**

| Name | Data Type | Description |
|------|-----------|-------------|
| `@ChannelID` | NMTOKEN | `@ChannelID` specifies the `Header/@ID` of the Query message that initiated the Subscription. `@ChannelID` SHALL match `Header/@refID` of each Signal that is transmitted on this persistent channel. |
| `@DeviceID?` | NMTOKEN | Identifier of the Controller that subscribed for the persistent channel. `@DeviceID` SHALL match `Header/@DeviceID` of the query that subscribed for this persistent channel. |
| `@MessageType` | NMTOKEN | `@MessageType` SHALL match the local element name (i.e. without namespace prefix) of the Signals that comprise this persistent channel. |
| `@Languages` *(New in XJDF 2.2)* | languages | `@Languages` SHALL specify the list of languages selected for human readable communication in the Query message that created the persistent channel. |
| `Subscription` | element | The Subscription element that describes the persistent channel. |
