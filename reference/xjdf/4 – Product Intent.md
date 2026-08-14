# 4 Product Intent

Product Intent provides a description of finished products from the print buyer's point of view.

## 4.1 Intent

Intent elements are a container for specific Product Intent elements. Product elements SHALL contain at most one intent element with the same `Intent/@Name`. If multiple product parts with different intent descriptions are needed, each product part SHALL be defined as a separate Product.

### 4.1.1 Product Intent

A Product Intent is any specific intent defined in this chapter (e.g. BindingIntent). A Product Intent is a child of a Intent element. Table 4.2 Product Intent Elements defines the list of Product Intents.

**Table 4.1: Intent Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| DescriptiveName? | string | Human readable descriptive name of the Intent. |
| ExternalID? | NMTOKEN | Identifier of the Intent in an external system such as an MIS. |
| Name | NMTOKEN | Type of the Product Intent. A list of predefined intent types is specified in Table 4.2 Product Intent Elements. Extension intent types MAY be defined. See Section 3.5.4 Creating Extension Intent Elements for details. |
| Product Intent? | element | Details of the Intent. The XML element name SHALL be the value of `@Name`. |
| \<foreign namespace elements\>* | element | Any elements in a foreign namespace. Foreign namespace extensions SHOULD NOT duplicate functionality of XJDF. |

**Table 4.2: Product Intent Elements (Sheet 1 of 2)**

| NAME | DESCRIPTION |
| --- | --- |
| AssemblingIntent? | This intent specifies the placing or inserting of one component within another, using information that identifies page location, position and attachment method. |
| BindingIntent? | This intent specifies the binding intent for a Product. |
| ColorIntent? | This intent specifies the type of ink to be used for a Product. |
| ContentCheckIntent? | This intent specifies the prepress proofing intent for a Product, using information that identifies the type, quality, brand name and overlay of the proof. |
| EmbossingIntent? | This intent specifies the embossing and/or foil stamping intent for a Product. |
| FoldingIntent? | This intent specifies the fold intent for a Product using information that identifies the number of folds, the height and width of the folds, and the folding catalog number. |
| HoleMakingIntent? | This intent specifies the hole making intent for a Product. |
| LaminatingIntent? | This intent specifies the laminating intent for a Product using information that identifies whether or not the product is laminated. |
| LayoutIntent? | This intent records the size of the Finished Pages for the product component. |
| MediaIntent? | This intent describes the media to be used for the product component. |
| ProductionIntent? | This intent specifies the manufacturing intent and considerations for a Product using information that identifies the desired result or specified manufacturing path. |

**Table 4.2: Product Intent Elements (Sheet 2 of 2)**

| NAME | DESCRIPTION |
| --- | --- |
| ShapeCuttingIntent? | This intent specifies form and line cutting for a Product. |
| VariableIntent? | This intent specifies the variations for printed data with variable content. |

### 4.1.2 Representation of Product Binding

BindingIntent and AssemblingIntent SHALL specify how multiple product parts are combined.

## 4.2 AssemblingIntent

This Product Intent element specifies the creation of a composite component by providing page location, position and attachment method of the respective child products that shall be assembled with the parent product. The containing Product SHALL be referenced in `AssemblingIntent/@Container`.

> **Note:** The containing Product is not identical to this parent Product. For instance an empty envelope (the product that is referenced by `@Container`) is not the same thing as a filled envelope (the parent Product). Whereas products that are bound together with BindingIntent SHALL be counted when calculating the page numbers of final bound products, AssemblyItems SHALL be ignored when calculating page numbers.

**Intent Properties**

Process Resource Pairing: InsertingParams

**Table 4.3: AssemblingIntent Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Container | IDREF | `@Container` SHALL reference the main Product that the additional products that are referenced from AssemblyItem, BindIn, BlowIn or StickOn are assembled with. `@Container` SHALL NOT reference the parent Product of this AssemblingIntent. |
| AssemblyItem* | element | Each AssemblyItem element describes an individual item that is assembled with the main Product that is referenced by `@Container`. |
| BindIn* | element | Each BindIn element describes an individual insert that is glued into the main Product that is referenced by `@Container`. |
| BlowIn* | element | Each BlowIn element describes an individual insert that is loosely inserted into the main Product that is referenced by `@Container`. |
| StickOn* | element | Each StickOn element describes an individual child Product that is glued onto the main Product that is referenced by `@Container`. StickOn is typically used for labels. |

### 4.2.1 AssemblyItem

An AssemblyItem element describes any individual item that is assembled with the main Product. Examples of assembly items include stands for roll-up displays or frames.

*[Figure 4-1: Roll-up display — изображение рулонного дисплея с выдвижной стойкой]*

**Table 4.4: AssemblyItem Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ChildRef | IDREF | `Product/@ID` of the product that describes the AssemblyItem. |

### 4.2.2 BindIn

BindIn elements describe inserts that are glued into the main product.

**Table 4.5: BindIn Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ChildRef | IDREF | `Product/@ID` of the product that describes the insert. |
| Folio? | integer | Index of the parent surface where the insert SHALL be bound in the context of the main product that is referenced by `AssemblingIntent/@Container`. |
| Orientation? | enumeration | Orientation of the insert in the coordinate system of the surface specified by `@Folio`. Allowed value is from: Orientation. |
| Position? | XYPair | Position of the bottom left corner of the insert in the coordinate system of the surface specified by `@Folio` after applying all rotations. |
| Glue? | element | Details of the glue used to fasten the insert. |

### 4.2.3 BlowIn

BlowIn elements describe inserts that are loosely inserted into the main product. This includes filling items into an envelope.

**Table 4.6: BlowIn Element (Sheet 1 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ChildRef | IDREF | `Product/@ID` of the product that describes the insert. |
| FolioFrom? | integer | Index of the first valid parent surface where the insert SHALL be placed in the context of the main product that is referenced by `AssemblingIntent/@Container`. |

**Table 4.6: BlowIn Element (Sheet 2 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| FolioTo? | integer | Index of the last valid parent surface where this Product SHALL be placed in the context of the main product that is referenced by `AssemblingIntent/@Container`. |
| Orientation? | enumeration | Orientation of the referenced insert in the coordinate system of the surface specified by `@Folio`. Allowed value is from: Orientation. |

**Example 4.1: Inserting a Letter into an Envelope**

This example illustrates using BlowIn to describe a single letter in an envelope.

```xml
<ProductList>
  <Product Amount="10" IsRoot="true" ProductType="FilledEnvelope">
    <Intent Name="AssemblingIntent">
      <AssemblingIntent Container="ID_Envelope">
        <BlowIn ChildRef="ID_Letter"/>
      </AssemblingIntent>
    </Intent>
  </Product>
  <Product Amount="1" ExternalID="MISID_Envelope" ID="ID_Envelope" IsRoot="false" ProductType="Envelope"/>
  <Product Amount="1" ID="ID_Letter" IsRoot="false" ProductType="Letter"/>
</ProductList>
```

### 4.2.4 StickOn

StickOn elements describe labels that are applied to the main product.

**Table 4.7: StickOn Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ChildRef | IDREF | `Product/@ID` of the product that describes the stick-on. |
| Face? | enumeration | Location of the stick-on on the Product. `@Face` SHALL NOT be specified if `@Folio` is specified. Allowed value is from: Face. |
| Folio? | integer | Index of the parent surface where the stick-on SHALL be placed in the context of the main product that is referenced by `AssemblingIntent/@Container`. `@Folio` SHALL NOT be specified if `@Face` is specified. |
| Orientation? | enumeration | Orientation of the stick-on in the coordinate system of the surface specified by `@Folio` or `@Face`. Allowed value is from: Orientation. |
| Position? | XYPair | Position of the bottom left corner of the stick-on in the coordinate system of the surface specified by `@Folio` or `@Face` after applying all rotations. |
| Glue? | element | Details of the glue used to fasten the stick-on. |

## 4.3 BindingIntent

This Product Intent specifies the binding intent for a Product using information that identifies the desired type of binding and which sides SHALL be bound. All other Products SHALL be bound in the order of their appearance in `BindingIntent/@ChildRefs`. When stack binding (see Gathering) the first product in the `BindingIntent/@ChildRefs` list SHALL represent the bottom or back of the bound items (and therefore the last product SHALL represent the top or front). When wrap around binding (see Collecting) the first product in the `BindingIntent/@ChildRefs` list SHALL represent the outermost item of the bound items (and therefore the last product SHALL represent the innermost item).

**Intent Properties**

Process Resource Pairing: BlockPreparationParams, CaseMakingParams, CasingInParams, CoverApplicationParams, EndSheetGluingParams, GluingParams, InsertingParams, JacketingParams, LooseBindingParams, SpinePreparationParams, SpineTapingParams, StitchingParams, ThreadSealingParams, ThreadSewingParams

**Table 4.8: BindingIntent Element (Sheet 1 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BackCoverColor? | enumeration | Defines the color of the back cover material of the binding. Allowed value is from: NamedColor. |
| BackCoverColorDetails? | string | A more specific, specialized or site-defined name for the color. If `@BackCoverColorDetails` is supplied, `@BackCoverColor` SHOULD also be supplied. |
| BindingColor? | enumeration | Defines the color of the spine material of the binding. Allowed value is from: NamedColor. |
| BindingColorDetails? | string | A more specific, specialized or site-defined name for the color. If `@BindingColorDetails` is supplied, `@BindingColor` SHOULD also be supplied. |
| BindingOrder? | enumeration | Specifies whether the child Component resources are to be collected or gathered if multiple child Component resources are combined. Allowed values are: **None** – The products referenced by `@ChildRefs` are NOT bound together. Typically used for flatwork jobs. **Collecting** – The products referenced by `@ChildRefs` are collected on a spine and placed within one another. The first Component is on the outside. **Gathering** – The child Component resources are gathered on a pile and placed on top of one another. The first child product specified by `@ChildRefs` is on the top. |
| BindingSide? | enumeration | `@BindingSide` indicates which side of the product SHALL be bound. Each of these values SHALL identify the binding edge. `@BindingSide` is defined in the coordinate system of the product. `@BindingSide` SHALL NOT be provided if `@BindingOrder="None"`. Allowed value is from: Edge. |
| BindingType | enumeration | Describes the desired binding for the job. Allowed value is from: BindingType. |
| ChildRefs? | IDREFS | `@ChildRefs` contains references to two or more child products each identified by `Product/@ID` (e.g., cover and body of a book) that SHALL be bound together. `@ChildRefs` SHALL NOT be specified if a single Product is bound. |
| CoverColor? | enumeration | Defines the color of the cover material of the binding. Allowed value is from: NamedColor. |
| CoverColorDetails? | string | A more specific, specialized or site-defined name for the color. If `@CoverColorDetails` is supplied, `@CoverColor` SHOULD also be supplied. |
| AdhesiveNote? | element | Details of AdhesiveNote binding. AdhesiveNote SHALL NOT be provided unless `BindingIntent/@BindingType="AdhesiveNote"`. |
| EdgeGluing? | element | Details of EdgeGluing. EdgeGluing SHALL NOT be provided unless `BindingIntent/@BindingType="EdgeGluing"`. |
| HardCoverBinding? | element | Details of HardCoverBinding. HardCoverBinding SHALL NOT be provided unless `BindingIntent/@BindingType="HardCover"`. |
| LooseBinding? | element | Details of LooseBinding. LooseBinding SHALL NOT be provided unless `BindingIntent/@BindingType` is one of "ChannelBinding", "CoilBinding", "CombBinding", "RingBinding" or "StripBinding". |
| SaddleStitching? | element | Details of SaddleStitching. SaddleStitching SHALL NOT be provided unless `BindingIntent/@BindingType="SaddleStitch"`. |
| SideStitching? | element | Details of SideStitching. SideStitching SHALL NOT be provided unless `BindingIntent/@BindingType="SideStitch"`. |

**Table 4.8: BindingIntent Element (Sheet 2 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| SoftCoverBinding? | element | Details of SoftCoverBinding. SoftCoverBinding SHALL NOT be provided unless `BindingIntent/@BindingType="SoftCover"`. |
| Tabs? | element | Details of Tabs. |

### 4.3.1 AdhesiveNote

Details of adhesive note binding.

**Table 4.9: AdhesiveNote Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Glue? | element | Glue provides details of the shape of the glue application and type of glue used. |

### 4.3.2 EdgeGluing

**Table 4.10: EdgeGluing Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| EdgeGlue? | enumeration | Glue type used to glue the edge of the gathered sheets. Allowed value is from: Glue. |

### 4.3.3 HardCoverBinding

**Table 4.11: HardCoverBinding Element (Sheet 1 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BlockThreadSewing? | boolean | Specified if the block is thread sewn. |
| CoverStyle? | NMTOKEN | Defines the style of the cover board. Values include: **Simple** – Single layer cover board, see Figure 4-2. **Padded** – Padded cover board, see Figure 4-3. |
| EndSheets? | boolean | `@EndSheets` SHALL be specified if end sheets SHALL be applied. Additional details of the end sheets MAY be specified by supplying a Product that is referenced by the parent `BindingIntent/@ChildRefs` with `@ProductType="EndSheet"`. |
| HeadBands? | boolean | The following case binding choice specifies the use of head bands on a case bound book. If "true", head bands are inserted both top and bottom. |
| HeadBandColor? | enumeration | Defines the color of the head band. Allowed value is from: NamedColor. |
| HeadBandColorDetails? | string | A more specific, specialized or site-defined name for the color. If `@HeadBandColorDetails` is supplied, `@HeadBandColor` SHOULD also be supplied. |
| Jacket? | enumeration | Specifies whether a hardcover jacket is needed and how it is attached. Details of the jacket MAY be described in the Product that is referenced by the parent `BindingIntent/@ChildRefs` whose `@ProductType="Jacket"`. Allowed values are: **None** – No jacket is needed. **Loose** – The jacket is loosely wrapped. **Glue** – The jacket is glued to the spine. |
| JacketFoldingWidth? | float | Dimension of the jacket folds. See JacketingParams for details. |

**Table 4.11: HardCoverBinding Element (Sheet 2 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| JapanBind? | boolean | Bind the book block at the open edge, so that the folds are visible on the outside. If not specified explicitly, this option is never selected. |
| SpineGlue? | enumeration | Glue type used to glue the book block to the cover. Allowed value is from: Glue. |
| SpineOperations? | NMTOKENS | `@SpineOperations` lists the operations that SHOULD be performed when preparing the spine. Values include those from: Spine Operations. |
| Thickness? | float | Specifies the thickness of the board that is wrapped as front and back covers of a case bound book, in points. |
| TightBacking? | enumeration | Definition of the geometry of the back of the book block. Allowed value is from: TightBacking. |
| RegisterRibbon* | element | Number, materials, colors and details of register ribbons. |

*[Figure 4-2: Structure of a normal hardcover book — схема структуры обычной книги в твёрдом переплёте: spine board, cover board, cover material]*

*[Figure 4-3: Structure of a padded hardcover book — схема структуры книги в мягком твёрдом переплёте: spine board, cover board, cover material, foam plastic padding]*

### 4.3.4 LooseBinding

**Table 4.12: LooseBinding Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Brand? | string | `@Brand` specifies the binder brand. |
| Diameter? | float | `@Diameter` specifies the diameter of coil, comb or rings, in points. |
| HolePattern? | element | HolePattern describes the hole pattern that the binder requires. Note that this MAY differ from the holes in the media. For instance the media for a 2 hole ring binder MAY have additional holes that are compatible with a 3 hole ring binder. |
| CoilBinding? | element | CoilBinding specifies additional details of coil binding. CoilBinding SHALL NOT be specified unless `BindingIntent/@BindingType="CoilBinding"`. |
| CombBinding? | element | CombBinding specifies additional details of either plastic comb binding or wire comb binding. CombBinding SHALL NOT be specified unless `BindingIntent/@BindingType="CombBinding"`. |
| RingBinding? | element | RingBinding specifies additional details of ring binding. RingBinding SHALL NOT be specified unless `BindingIntent/@BindingType="RingBinding"`. |

#### 4.3.4.1 CoilBinding

**Table 4.13: CoilBinding**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CoilShape? | NMTOKEN | The shape of the wire coil used for the binding. Value includes those from: Comb and Coil Shapes. |
| Material? | enumeration | The material available for forming the coil binding when `BindingIntent/@BindingType="CoilBinding"` or "WireComb". Allowed value is from: BinderMaterial. |

#### 4.3.4.2 CombBinding

**Table 4.14: CombBinding**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CombShape? | NMTOKEN | The shape of the plastic comb used for the binding. Value includes those from: Comb and Coil Shapes. |
| Material? | enumeration | The material available for forming the comb binding when `BindingIntent/@BindingType="CombBinding"`. Allowed value is from: BinderMaterial. |

#### 4.3.4.3 RingBinding

**Table 4.15: RingBinding**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BinderMaterial? | NMTOKEN | `@BinderMaterial` describes the required material to be used for the binder cover. Values include: **Cardboard** – Cardboard with no covering. **ClothCovered** – Cardboard with cloth covering. **Plastic** – Binder cover fabricated from solid plastic sheet material (e.g., PVC sheet). **VinylCovered** – Cardboard with colored vinyl covering. |
| RingShape? | NMTOKEN | `@RingShape` specifies the shape of the ring binder rings. Values include: Round, Oval, D-shape, SlantD. |
| RivetsExposed? | boolean | `@RivetsExposed` describes the ring mechanism mounting in a binder case. If "true", the heads of the rivets are visible on the exterior of the binder. If "false", the binder covering material covers the rivet heads. |
| ViewBinder? | NMTOKEN | `@ViewBinder` specifies the details of clear vinyl outer-wrap types on top of a colored base wrap. Values include: **Embedded** – Printed material is embedded by sealing between the colored and clear vinyl layers during the binder manufacturing. **Pocket** – Binder is designed so that printed material can be inserted between the color and clear vinyl layers after the binder is manufactured. |

### 4.3.5 SaddleStitching

**Table 4.16: SaddleStitching Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| StapleShape? | enumeration | Specifies the shape of the staples to be used. Allowed value is from: StapleShape. |
| StitchNumber? | integer | Number of stitches used for saddle stitching. |

### 4.3.6 SideStitching

**Table 4.17: SideStitching Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| StapleShape? | enumeration | Specifies the shape of the staples to be used. Allowed value is from: StapleShape. |
| StitchNumber? | integer | Number of stitches used for side stitching. |

### 4.3.7 SoftCoverBinding

**Table 4.18: SoftCoverBinding Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BlockThreadSewing? | boolean | Specifies whether the block is also thread sewn. |
| EndSheets? | boolean | `@EndSheets` SHALL be specified if end sheets SHALL be applied. Additional details of the end sheets MAY be specified by supplying a Product that is referenced by the parent `BindingIntent/@ChildRefs` whose `@ProductType="EndSheet"`. |
| FoldingWidth? | float | Definition of the dimension of the folding width of the front cover fold. See JacketingParams for details. |
| FoldingWidthBack? | float | Definition of the dimension of the folding width of the back cover fold. If not specified, `@FoldingWidthBack` defaults to `@FoldingWidth`. |
| GlueProcedure? | enumeration | Glue procedure used to glue the book block to the cover. Allowed values are: **Spine** – Glued at the spine. **SideOnly** – Glued at the side or end sheets but not at the spine. "SideOnly" books are also referred to as "layflat" if `@EndSheets` is also specified. See Figure 4-4. **SingleSide** – Swiss brochure. **SideSpine** – Both side gluing and spine gluing. |
| Scoring? | enumeration | Scoring option for SoftCoverBinding. Values are based on viewing the cover in its flat, pre-bound state. See Figure 4-5. Allowed values are: TwiceScored, QuadScored, None. |
| SpineGlue? | enumeration | Glue type used to glue the book block to the cover. Allowed value is from: Glue. |
| SpineOperations? | NMTOKENS | `@SpineOperations` lists the operations that SHOULD be performed when preparing the spine. Values include those from: Spine Operations. |

*[Figure 4-4: Structure of a book with GlueProcedure="SideOnly" (Layflat) — схема структуры книги с боковым клеевым соединением: Block, Endsheet, Softcover, Glue]*

*[Figure 4-5: Scoring for soft cover binding — схема вариантов биговки для мягкого переплёта: TwiceScored и QuadScored]*

### 4.3.8 Tabs

Specifies tabs in a bound document.

**Table 4.19: Tabs Element (Sheet 1 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ReinforceTabs? | boolean | If "true", the tab extension will be reinforced, e.g. with polyester film. |
| ReinforceBind? | boolean | If "true", the tab bind edge will be reinforced, e.g. with polyester film. |
| ReinforceColor? | enumeration | Specifies the color of the tab extension reinforcement. `@ReinforceColor` SHALL NOT be specified unless `@ReinforceTabs="true"`. Allowed value is from: NamedColor. |
| ReinforceColorDetails? | string | A more specific, specialized or site-defined name for the color. If `@ReinforceColorDetails` is supplied, `@ReinforceColor` SHOULD also be supplied. |
| TabBrand? | string | Strings providing available brand names for the Tabs. |
| TabCount? | integer | Number of tabs across all banks. If `@TabsPerSet` is not an even multiple of `@TabsPerBank`, the last bank in each set is partially filled. |
| TabsPerBank? | integer | Number of equal-sized tabs in a single bank if all positions were filled. Note that banks can have tabs only in some of the possible positions. |
| TabExtensionDistance? | float | Distance tab extends beyond the body of the book block, in points. |

**Table 4.19: Tabs Element (Sheet 2 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| TabBodyCopy? | boolean | If "true", color will be applied not only on tab extension, but also on tab body. Note: The lack of body copy allows all tabs within a bank to be printed on a single sheet. |

## 4.4 ColorIntent

ColorIntent specifies the color and varnishing of the product. Each surface SHALL be specified individually in a SurfaceColor element. Single sided printing SHALL be specified by providing exactly one SurfaceColor element.

In addition to the printed images, ColorIntent also provides details of protective or gloss enhancing coatings. Customers may either specify the performance characteristic they desire in the coating or specify a coating type. Common examples are water-resistance, and rub-resistance. Both characteristics may be required at the same time. An example is in the wine industry, where the white wine label has to survive transport rubbing, followed by water and rubbing ice cubes in a bucket upon serving.

**Intent Properties**

Process Resource Pairing: Color, ColorantControl, ColorCorrectionParams, ColorSpaceConversionParams, Ink, VarnishingParams

### 4.4.1 SurfaceColor

This element specifies the color configuration of the desired product's surface.

**Table 4.20: ColorIntent Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| SurfaceColor(Back)? | element | `SurfaceColor[@Surface="Back"]` describes the color intent of the back surfaces of the Final Product. If not specified the "Back" surfaces SHALL NOT be marked. |
| SurfaceColor(Front)? | element | `SurfaceColor[@Surface="Front"]` describes the color intent of the front surfaces of the Final Product. If not specified the "Front" surfaces SHALL NOT be marked. |

**Table 4.21: SurfaceColor Element (Sheet 1 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Coatings? | NMTOKENS | Material usually applied to a full surface on a press as a protective or gloss-enhancing layer over ink. Values include those from: Ink and Varnish Coatings. Note: Multiple NMTOKENS MAY be selected to indicate multiple coatings. Note: `@Surface` specifies the surface to which this coating applies. Note: Spot coating is specified in `@ColorsUsed`. |
| ColorsUsed? | NMTOKENS | Array of colorant separation identifiers that are requested for this SurfaceColor. If specified `@ColorsUsed` SHALL contain a list of all separation identifiers used by the product or a list of spot colors specified by "Spot" that specifies a generic spot color whose details are unknown. "Spot" MAY be specified multiple times in one `@ColorsUsed` value. If not specified, then this SurfaceColor explicitly requests no colors on the surface that is specified in `@Surface`. If additional information about the colors and colorants is needed, it MAY be specified in `ResourceSet/Resource/Color` elements that are partitioned by matching `@Separation` Partition Keys. In addition, partial (spot) coating MAY be specified by adding NMTOKENS with any value from `@Coatings`. |
| Coverage? | float | Cumulative colorant coverage percentage. For example, a full sheet of 100% deep black in CMYK has `@Coverage="400"`. Typical coverages based on one color plane are: Light – 1-9%, Medium – 10-35%, Heavy – 36+%. Note: `@Surface` specifies the surface to which this coverage applies. |

**Table 4.21: SurfaceColor Element (Sheet 2 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| PrintStandard? | NMTOKEN | `@PrintStandard` SHALL specify the reference name of a characterization data set. See Appendix A.3.17 PrintStandard Characterization Data Sets for details. Note: The characterization data set defines the expected minimal color gamut of the print process. |
| Surface | enumeration | Allowed value is from: Side. |
| Certification* *(New in XJDF 2.1)* | element | Each Certification SHALL specify a minimum requested ink certification level. If more than one Certification is present, at least one of the ink certification levels SHALL be met. |

## 4.5 ContentCheckIntent

This Product Intent element specifies the prepress proofing and preflighting intent for a Product.

**Intent Properties**

Process Resource Pairing: ApprovalParams, ApprovalDetails, PreflightParams, PreflightReport

### 4.5.1 PreflightItem

PreflightItem defines the preflight rules for the pages in a Product.

### 4.5.2 ProofItem

**Table 4.22: ContentCheckIntent Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| PreflightItem* | element | PreflightItem defines the preflight rules for the Product. |
| ProofItem* | element | Specifies the details of the proofs that are needed. If no ProofItem exists in a ContentCheckIntent, no customer proofs SHALL be provided. Note: ProofItem describes proofs that will be provided to the customer and does not specify internal production proofs. |

**Table 4.23: PreflightItem Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| PreflightLevel? | enumeration | Level of content data checking/preflighting. The details are implementation specific. Allowed values are: **Basic** – Check only for severe errors. Examples include missing fonts, unknown file format, incorrect page size, missing passwords. **Extended** – Check for additional errors that can degrade output quality and can be resolved by the customer. Examples include: low image resolution, unknown color space details. **Premium** – Highest available check for additional errors. This level MAY include manual repairs by the printer. |

**Table 4.24: ProofItem Element (Sheet 1 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Amount? | integer | Specifies the total number of copies of this proof that are needed. |
| ColorType? | enumeration | Color quality of the proof. Allowed values are: **Monochrome** – Generic single color printing condition (e.g., black and white or one single spot color). **BasicColor** – Color does not match precisely. This implies the absence of a color matching system. **MatchedColor** – Color is matched to the output of the press using a color matching system. |

**Table 4.24: ProofItem Element (Sheet 2 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Contract? | boolean | Requires proof to be a legally binding, accurate representation of the image to be printed (i.e., color quality requirements have been met when the printed piece acceptably matches the proof). |
| HalfTone? | boolean | If `@HalfTone="true"`, the proof SHALL emulate halftone screens. |
| ID? | ID | Identifier of the ProofItem. This field SHALL be specified if delivery of a proof is specified in DeliveryParams. |
| PageIndex? | IntegerRange | Index of pages that SHALL be proofed in reader order. If `@PageIndex` is not specified, then all pages SHALL be proofed. |
| ProofTarget? *(Deprecated in XJDF 2.1)* | URL | Identifies a remote target for the proof output in a remote proofing environment. This can be either a soft or a hard proofing target. The file to be displayed or output SHALL be sent to the URL specified in `@ProofTarget`. Deprecation note: Use FileSpec. |
| FileSpec? *(New in XJDF 2.1)* | element | Identifies a remote target for the proof output in a remote proofing environment. This can be either a soft or a hard proofing target. The file to be displayed or output SHALL be sent to the URL specified in FileSpec. |

## 4.6 EmbossingIntent

This Product Intent specifies the embossing and/or foil stamping intent for a Product using information that identifies whether the product is embossed or stamped, and if desired, the complexity of the affected area.

**Intent Properties**

Process Resource Pairing: EmbossingParams

### 4.6.1 EmbossingItem

**Table 4.25: EmbossingIntent Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| EmbossingItem+ | element | Each embossed image is described by one EmbossingItem. |

**Table 4.26: EmbossingItem Element (Sheet 1 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Direction? | enumeration | The direction of the image. Allowed value is from: EmbossDirection. |
| EmbossingType | enumeration | The embossing type required. Allowed value is from: EmbossType. |
| Face? | enumeration | Position of the embossing on the product. Allowed value is from: Face. |
| FoilColor? | enumeration | Defines the color of the foil material that is used for embossing. Allowed value is from: NamedColor. |
| FoilColorDetails? | string | A more specific, specialized or site-defined name for the color. If `@FoilColorDetails` is supplied, `@FoilColor` SHOULD also be supplied. `@FoilColorDetails` SHOULD be used to specify specialized foil properties such as holographic or transparent foils. Example combinations of `@FoilColor` and `@FoilColorDetails` include: Holographic foils: `@FoilColor="Silver"` and `@FoilColorDetails="Holographic"`. Matte transparent foil: `@FoilColor="White"` and `@FoilColorDetails="TransparentMatte"`. |
| Height? | float | The height of the levels. This value specifies the vertical distance between the highest and lowest point of the stamp, regardless of the value of `@Direction`. |

**Table 4.26: EmbossingItem Element (Sheet 2 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ImageSize? | XYPair | The size of the bounding box of one single image. |
| Position? | XYPair | Position of the lower left corner of the bounding box of the embossed image in the coordinate system of the surface of the Component that is selected by `@Face`. |
| Separation? | NMTOKEN | `@Separation` identifies the separation within the PDL whose color values SHALL be used as the embossing values. A value of 0.0 in the PDL SHALL specify no embossing, a value of 1.0 in the PDL SHALL specify embossing with full depth. If a `ResourceSet/Resource/Color` element is specified for this separation, the value of `Color/@ColorType` SHALL be "DieLine". |
| ToolName? | NMTOKEN | Name of the embossing tool. |

## 4.7 FoldingIntent

This Product Intent specifies the straight line folding, creasing and perforating of a product. Folds that are implied by binding such as "F4-1" of a saddle stitched booklet SHALL NOT be specified. Table 4.28 Product Folds illustrates some typical product folds. See Section 4.3 BindingIntent for additional details.

**Intent Properties**

Process Resource Pairing: CreasingParams, CuttingParams, FoldingParams, PerforatingParams

### 4.7.1 Typical Product Folds

The following figure illustrates some typical product folds.

> **Note:** This list is not complete.

**Table 4.27: FoldingIntent Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| FoldCatalog? | NMTOKEN | Describes the folding scheme. Note: The folding scheme in this context refers to the folding of the finished product as seen after the cutting, not the folding, of the sheet as seen in production. See Table 4.28 Product Folds for an illustration of typical product folding schemes. Value includes those from: Fold Catalog. |
| FoldingDetails? | NMTOKEN | `@FoldingDetails` is a system dependent descriptor of the folding. `@FoldingDetails` MAY be used to differentiate differing fold dimensions with the same general topology, such as asymmetrical Z-folds. `@FoldingDetails` SHALL NOT be specified if `@FoldCatalog` is not present. |
| Orientation? | enumeration | `@Orientation` indicates the orientation of the unfolded product with respect to the lay of the fold. A value of "Rotate0" SHALL be mapped to the lay of the fold on the lower left of the product prior to folding and the front side of the product oriented in the direction of an upward fold. Allowed value is from: Orientation. |
| Crease* | element | Crease elements describe the details of any creasing operations in the coordinate system of the Final Product. If no geometrical details are specified in the Crease element and a `@FoldCatalog` is specified, the customer is requesting production creasing. |
| Fold* | element | This describes the details of folding operations in the sequence described by the value of `@FoldCatalog`. Fold SHALL be specified if non-symmetrical folds are requested. |
| Perforate* | element | Perforate elements describe the details of any perforating operations in the coordinate system of the Final Product. If no geometrical details are specified in the Perforate element and a `@FoldCatalog` is specified, the customer is requesting production perforation. |

**Table 4.28: Product Folds (Sheet 1 of 2)**

| FOLD CATALOG VALUE | IMAGE | DESCRIPTION |
| --- | --- | --- |
| F2-1 | *[схема: без фальца]* | No fold. |
| F4-1 | *[схема: один фальц]* | Single fold. |
| F6-1 | *[схема: зигзагообразный фальц]* | Zigzag fold. |
| F6-3 | *[схема: алтарный фальц]* | Altar fold. |
| F6-4 | *[схема: тройной фальц]* | Tri-fold. |

**Table 4.28: Product Folds (Sheet 2 of 2)**

| FOLD CATALOG VALUE | IMAGE | DESCRIPTION |
| --- | --- | --- |
| F6-7 | *[схема: Z-фальц]* | Z fold. |
| F8-2 | *[схема: параллельный фальц]* | Parallel fold. |
| F8-4 | *[схема: воротниковый фальц]* | Gate fold. |
| F8-5 | *[схема: бочкообразный фальц]* | Barrel fold. |

## 4.8 HoleMakingIntent

This Product Intent specifies the hole making intent for a Product. This Product Intent does not specify whether the media will be pre-drilled or the media will be drilled or punched as part of making the product.

**Intent Properties**

Process Resource Pairing: HoleMakingParams, Media

**Table 4.29: HoleMakingIntent Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| HolePattern+ | element | Each HolePattern describes one hole or a specific set of holes that SHALL be provided. The coordinate system for applying the holes SHALL be the coordinate system of the Product. |

## 4.9 LaminatingIntent

This Product Intent specifies the laminating intent for a Product.

**Intent Properties**

Process Resource Pairing: LaminatingParams

**Table 4.30: LaminatingIntent Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Surface | enumerations | The surface or surfaces to be laminated. Allowed values are from: Side. |
| Temperature? | enumeration | Temperature used in the Laminating process. Allowed values are: Hot, Cold. |
| Texture? | NMTOKEN | The intended texture of the laminate. Value includes those from: Texture. |
| Thickness? | float | Thickness of the laminating material. Measured in microns [µm]. |

## 4.10 LayoutIntent

This Product Intent records the size of the Finished Pages for the product component. It does not, however, specify the size of any intermediate results such as press sheets. It also describes how the Finished Pages of the product component SHALL be imaged onto the finished media.

**Intent Properties**

Process Resource Pairing: Assembly, BinderySignature, Layout

**Table 4.31: LayoutIntent Element (Sheet 1 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Bleed? | float | Bleed of the artwork in points. The value of 0 means no bleed. A negative value indicates bleed is needed but the value is unknown. |
| Dimensions? | XYPair | Specifies the width (X) and height (Y) in points, respectively, of the trimmed and unfolded (flat) product. For example, `@Dimensions` for a Z-fold is the unfolded dimensions, while `@FinishedDimensions` is the folded dimensions if known. Use `@Dimensions` if `@FinishedDimensions` is not known. `@Dimensions` is provided for the rare case that `@FinishedDimensions` does not unambiguously define the finished product, due to complex folding schemes. If both values are specified, `@FinishedDimensions` takes precedence. |
| FinishedDimensions? | shape | Specifies the width (X), height (Y) and depth (Z) in points, respectively, of the finished product Component after all finishing operations, including folding, trimming, etc. If the Z coordinate is 0, it SHALL be ignored. Only `@FinishedDimensions` SHOULD be specified if both `@FinishedDimensions` and `@Dimensions` are known. |
| NamedDimensions? | NMTOKEN | Named size (e.g., "A4" or "Letter") that corresponds to the value specified in `@FinishedDimensions`. If both `@NamedDimensions` and `@FinishedDimensions` are specified, then `@FinishedDimensions` has precedence. See Appendix C Media Size for a list of preferred values. |
| NumberUp? | XYPair | Specifies a regular, multi-up grid of page cells into which content pages are mapped. The first value specifies the number of columns of page cells and the second value specifies the number of rows of page cells in the multi-up grid (both numbers are integers). |

**Table 4.31: LayoutIntent Element (Sheet 2 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Orientation? | enumeration | `@Orientation` SHALL specify the orientation of the artwork on the surface as defined by `@Sides`. `@Orientation` is used to define products such as back-lit displays, where the orientation of the image with respect to the Final Product is rotated or mirrored. Allowed value is from: Orientation. |
| Pages? | integer | Specifies the number of Finished Pages (surfaces) of the product component, including blank pages. See `@SpreadType` for a discussion of the scope of `@Pages`. This value SHALL be an even number. For example, the value for `@Pages` for a two-sided booklet with seven Reader Pages would be "8", whether the booklet was either saddle stitched or glued. |
| Sides? | enumeration | `@Sides` specifies which side of the product SHALL be printed. Allowed value is from: Sides. |
| SpreadType? | enumeration | `@SpreadType` SHALL specify the treatment of individual PDF pages referenced by the product for imposition purposes. Allowed value is from: SpreadType. Note: Content will typically be provided as single pages. However, products with Finished Pages of varying size such as wrap around covers with a spine or fold outs in a booklet will typically be defined as spreads. |

## 4.11 MediaIntent

This Product Intent describes the media to be used for the Product.

**Intent Properties**

Process Resource Pairing: Media

**Table 4.32: MediaIntent Element (Sheet 1 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BackCoating? | enumeration | `@BackCoating` SHALL specify the pre-process coating of the back surface of the media. If not specified the value of `@Coating` SHALL be applied. Allowed value is from: Coating. |
| BackISOPaperSubstrate? | enumeration | `@BackISOPaperSubstrate` SHALL be used to classify the back surface of paper. Additional technical specifications such as `@Opacity` or `@BackCoating` MAY be specified to enhance the definition of the Media. If `@BackISOPaperSubstrate` is not specified, the value of `@ISOPaperSubstrate` SHALL be applied. If `@BackISOPaperSubstrate` is specified, then `@ISOPaperSubstrate` SHALL also be specified. Allowed value is from: ISOPaperSubstrate. |
| Brand? | string | Strings providing available brand names. The customer might know exactly what paper is to be used. Example is "Lustro" or "Warren Lustro" even though the manufacturer name is included. |
| BuyerSupplied? | boolean | Indicates whether the customer will supply the media. |
| Coating? | enumeration | `@Coating` SHALL specify the pre-process coating of the media. Allowed value is from: Coating. |
| Flute? | NMTOKEN | Single, capital letter that specifies the flute type of corrugated media. Values include those from: Flute Types. |
| FluteDirection? | enumeration | Direction of the flute of corrugated media in the coordinate system of the product. Allowed value is from: MediaDirection. |

**Table 4.32: MediaIntent Element (Sheet 2 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| GrainDirection? | enumeration | Direction of the grain in the coordinate system of the Product. Allowed value is from: MediaDirection. |
| ISOPaperSubstrate? | enumeration | `@ISOPaperSubstrate` SHALL be used to classify the surface of paper. Additional technical specifications such as `@Opacity` or `@BackCoatings` MAY be specified to enhance the definition of the Media. Allowed value is from: ISOPaperSubstrate. Note: See Section B.3 Paper Grade for a mapping to the paper grade values defined in [ISO12647-2:2004]. |
| LabColorValue? | LabColor | `@LabColorValue` is the CIELAB color value of the media, computed as specified in [TAPPI T527]. |
| MediaColor? | enumeration | Color of the media. If more-specific, specialized or site-specific media color names are needed, use `@MediaColorDetails`. Allowed value is from: NamedColor. |
| MediaColorDetails? | string | A more specific, specialized or site-defined name for the media color. If `@MediaColorDetails` is supplied, `@MediaColor` SHOULD also be supplied. Note: There is a one-to-many relationship between entries in `@MediaColor` and `@MediaColorDetails` (e.g., `@MediaColorDetails` values of "Burgundy" and "Ruby" both correspond to a `@MediaColor` of "DarkRed"). |
| MediaQuality? | string | Named quality description of the media. Media with the same `@MediaQuality` are identical from the customer point of view. Thus characteristics such as weight, coatings or recycling percentage are identical whereas lot or sheet dimension may vary based on production or warehousing requirements. |
| MediaType | enumeration | Describes the medium being employed. Allowed value is from: MediaType. Note: Values from MediaType are RECOMMENDED. However, some process related values, such as "Plate", SHOULD NOT be used for this attribute. |
| MediaTypeDetails? | NMTOKEN | Describes additional details of the medium described in `@MediaType`. Value includes those from: MediaType Details. Note: Values from MediaType Details are RECOMMENDED. However, some process related values, such as "DryFilm", SHOULD NOT be used for this attribute. |
| Opacity? | enumeration | The opacity of the media. Allowed value is from: Opacity. |
| PrePrinted? | boolean | Indicates whether the media is preprinted. |
| StockType? | NMTOKEN | `@StockType` defines the base size when calculating North American or Japanese paper weights. See Appendix B Media Weight for details including pre-defined values. |
| Texture? | NMTOKEN | The intended texture of the media. Value includes those from: Texture. |
| Thickness? | float | The thickness of the chosen medium. Measured in microns [µm]. |
| Weight? | float | The intended weight of the media, measured in grammage (g/m²) of the media. See Appendix B Media Weight for an explanation of how to calculate the US weight from the grammage for different stock types. |
| Certification* | element | Each Certification SHALL specify a minimum requested paper certification level. If more than one Certification is present, at least one of the paper certification levels SHALL be met. |

## 4.12 ProductionIntent

This Product Intent specifies the manufacturing intent and considerations for a Product using information that identifies the desired result or specified manufacturing path. If specific details of print quality, such as color quality, need to be specified, `@Types` SHOULD contain "QualityControl". A QualityControlParams ResourceSet that contains the requirements SHOULD also be provided.

**Intent Properties**

**Table 4.33: ProductionIntent Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| PrintPreference? | enumeration | Intended result or goal. Allowed values are: **Balanced** – Request for a manufacturing process that balances the requirements for cost, speed and quality. **CostEffective** – Request for the most cost effective manufacturing process. **Fastest** – Request for the most time effective manufacturing process. Cost and quality can be sacrificed for a fast turnaround time. **HighestQuality** – Request for the manufacturing process that will result in the highest quality. |
| PrintProcess? | NMTOKENS | Print process requested. If more than one value is specified, then `@PrintProcess` requests hybrid printing, e.g. inkjet imprint on a preprinted shell. Values include those from: Printing Technologies. |
| Certification* *(New in XJDF 2.1)* | element | Each Certification SHALL specify a minimum requested certification level for production. If more than one Certification is present, at least one of the certification levels SHALL be met. |

## 4.13 ShapeCuttingIntent

ShapeCuttingIntent describes finishing of products with irregular shapes, including die cutting and adding windows to envelopes.

**Intent Properties**

Process Resource Pairing: CuttingParams, ShapeCuttingParams

### 4.13.1 ShapeCut

**Table 4.34: ShapeCuttingIntent Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ShapeCut+ | element | Array of all ShapeCut elements. Used when each shape is exactly specified. |

**Table 4.35: ShapeCut Element (Sheet 1 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CutBox? | rectangle | Specification of a rectangular window. An orthogonal line MAY be defined by specifying a rectangle with identical dimensions. |
| CutDepth? | enumeration | Allowed values are: **Full** – The form is completely cut out or perforated. **Partial** – The form is not completely cut out or perforated. The exact depth MAY be specified in ShapeCuttingParams. |
| CutOut? | boolean | `@CutOut` specifies whether the inside or outside of the ShapeCut SHALL be removed. If `@CutOut="true"`, the inside of a specified shape SHALL be removed, otherwise the outside of a specified shape SHALL be removed. An example of an inside shape is a window, while an example of an outside shape is a shaped greeting card. |

**Table 4.35: ShapeCut Element (Sheet 2 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CutPath? | PDFPath | Specification of a complex path. This MAY be an open path in the case of a single line. |
| CutType? | enumeration | Type of cut or perforation used. Allowed values are: **Cut** – Full cut. **Perforate** – Interrupted perforation that does not span the entire sheet. |
| ShapeType | enumeration | Describes any precision cutting other than hole making. Allowed values are: **Line** – The coordinates specified in `@CutBox` specify the end points of a straight line. **Path** – Any irregular shape. Additional details SHOULD be provided in `@CutPath` or `@ShapeTypeDetails`. **Rectangular** – The coordinates specified in `@CutBox` specify the lower left and upper right coordinates of a rectangle. **Round** – Circular or elliptical shape depending on the aspect ratio of `@CutBox`. **RoundedRectangle** – Rectangle with rounded corners. The coordinates specified in `@CutBox` specify the outer bounds of the rectangle. |
| ShapeTypeDetails? | string | A more specific, specialized or site-defined name for the shape of the ShapeCut. |

## 4.14 VariableIntent

VariableIntent specifies the variations of the content for printed data with variable content such as lottery tickets or direct mail.

**Intent Properties**

Process Resource Pairing: DigitalPrintingParams, LayoutElementProductionParams

**Table 4.36: VariableIntent Element (Sheet 1 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Area? | float | Ratio of the document that can contain variable content. A value of 0 specifies a non variable document. A value of 1 specifies a full variable document. |
| AveragePages? | integer | `@AveragePages` SHALL specify the average number of printed pages in each record. |
| ChildRefs? | IDREFS | `Product/@ID` of the product elements that describe individual finishing variants. `@ChildRefs` SHALL NOT be specified if AssemblingIntent or BindingIntent are specified for this product. |
| ColorsUsedBack? | NMTOKENS | Array of colorant separation identifiers that are required to print the variable part of the documents. The values that are specified in `@ColorsUsedBack` SHALL also be specified in `ColorIntent/SurfaceColor[@Surface="Back"]/@ColorsUsed`. See `ColorIntent/SurfaceColor/@ColorsUsed` for additional details. |
| ColorsUsedFront? | NMTOKENS | Array of colorant separation identifiers that are required to print the variable part of the documents. The values that are specified in `@ColorsUsedFront` SHALL also be specified in `ColorIntent/SurfaceColor[@Surface="Front"]/@ColorsUsed`. See `ColorIntent/SurfaceColor/@ColorsUsed` for additional details. |
| MaxPages? | integer | `@MaxPages` SHALL specify the maximum number of printed pages in each record. `@MaxPages` SHALL NOT be smaller than `@AveragePages`. |
| MinPages? | integer | `@MinPages` SHALL specify the minimum number of printed pages in each record. `@MinPages` SHALL NOT be larger than `@AveragePages`. |
| NumberOfCopies? | integer | Average number of copies of each record. This value SHALL equal "1" for fully variable data. |

**Table 4.36: VariableIntent Element (Sheet 2 of 2)**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| VariableType | enumeration | Type of variable content. Allowed values are (in order of rising complexity): **OneLine** – A single line of text data is variable. OneLine includes simple numbering applications. **AddressField** – Multiple lines of text data are variable. **IdentificationField** – The variable data includes a Barcode or QR-Code. **Area** – The area, as defined in `@Area`, is fully variable. |
| VariableQuality? | enumeration | `@VariableQuality` specifies the desired quality of the variable data. Allowed values are: **Simple** – The variable text MAY be recognized as printed by a different technology such as dot matrix or simple inkjet overprints. **Imprint** – The variable data SHOULD be similar to the non-variable part but MAY be imprinted. **Full** – All data SHOULD be printed with the same technology. |