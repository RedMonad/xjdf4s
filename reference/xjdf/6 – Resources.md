# 6 Resources

This chapter provides the detailed definition of the Resource element followed by a list (in alphabetical order) of all specific resource types.

## 6.1 Resource

Resource elements are child elements of a ResourceSet and describe the physical or logical entity in the partition context that is defined in Resource/Part. For instance a ResourceSet/@Name="ExposedMedia" can specify a set of printing plates and each child Resource element will describe an individual plate.

**Table 6.1: Resource Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Brand? | string | Brand or manufacturer of the Resource. |
| CommentURL? | URL | URL to an external, human-readable description of the Resource. |
| DescriptiveName? | string | Human-readable descriptive name of the Resource. It is strongly RECOMMENDED to supply @DescriptiveName in Resource elements that describe physical entities for communication from applications to humans in order to reference the Resource. |
| Duration? | duration | If @Duration is specified for ResourceSet/@Usage="Input", @Duration specifies the time duration during which the Resource will be or has been used.<br><br>Note: @Duration in conjunction with @Start or @StartOffset can be used to schedule or track resources such as tools that are only required during part of the processing time as defined in NodeInfo.<br><br>If @Duration is specified for ResourceSet/@Usage="Output", @Duration specifies the time that the Resource SHALL be or has been stored after it has been produced.<br><br>Note: @Duration can be used to define resting periods, e.g. to allow press sheets to dry prior to further processing.<br><br>Note: If @Duration is specified in descendents of ResourceInfo it SHALL specify actual duration. In all other cases it SHALL specify a planned or requested duration. |
| Expires? *(New in XJDF 2.1)* | dateTime | Date and time beyond which the resource SHOULD NOT be used. |
| ExternalID? | NMTOKEN | An identifier of the resource as defined in the MIS system. For instance item codes or article numbers or identifiers on semi-finished products. @ExternalID SHALL be used to uniquely identify resources and products for the purpose of inventory tracking and SHOULD be used for resource synchronization using the CommandResource XJMF message. |
| GrossWeight? | float | Gross weight of a single Resource, as counted in @Amount, in grams. |
| ID? | ID | Unique XJDF internal identifier of a Resource. |
| Orientation? | enumeration | Named orientation describing the orientation of a Resource relative to the ideal process coordinate that uses this Resource as input or output. If @Orientation is specified for an output Resource, the XJDF that processes the Resource SHALL manipulate the Resource in such a way as to reflect the transformation. The coordinate system of the Resource itself is not modified.<br><br>At most one of @Orientation or @Transformation SHALL be specified.<br><br>For details on coordinate systems, see Section 2.6 Coordinate Systems in XJDF.<br><br>Allowed value is from: Orientation. |
| ResourceWeight? | float | Net weight of a single Resource, as counted in @Amount, in grams. |
| Start? | dateTime | Time and date when the usage of the Resource SHALL start or has started. If @Start is specified in descendents of ResourceInfo it SHALL specify an actual start time. In all other cases, it SHALL specify a planned or requested start time. |
| StartOffset? | duration | Offset time when the Resource is scheduled to be used after processing has begun. @StartOffset SHALL NOT be specified if @Start is present. @StartOffset SHALL NOT be specified in the context of ResourceInfo. |
| Status? | enumeration | The status of a resource indicates whether it is available for processing.<br><br>@Status SHALL NOT be specified if ResourceSet/@Usage="Output".<br><br>Allowed values are:<br>• Available – Indicates that the resource is available for processing.<br>• Unavailable – Indicates that the resource is not available for processing. |
| Transformation? | matrix | Matrix describing the orientation of a Resource relative to the ideal process coordinate using this Resource as input or output. If @Transformation is specified for an output Resource, the XJDF that processes the Resource SHALL manipulate the Resource in such a way as to reflect the transformation. The coordinate system of the resource itself is not modified.<br><br>At most one of @Orientation or @Transformation SHALL be specified.<br><br>For details on coordinate systems, see Section 2.6 Coordinate Systems in XJDF. |
| AmountPool? | element | AmountPool specifies partial amounts and waste for this Resource. |
| Comment* | element | Any human-readable text that describes the Resource. |
| GeneralID* | element | Additional identifiers related to the Resource. |
| Part* | element | The Part elements identify the partition context of the Resource element. The structure of the Part element is defined in Table 6.4 Part Element. For details on partitioned Resource elements, see Section 6.1.3.2 Selecting a Partition.<br><br>If no Part element is specified, then the Resource applies to the entire ResourceSet. If multiple Part elements are specified, the Resource describes one entity that applies to multiple partitions (e.g., the color plates that apply to all versions of a multi version job). |
| Specific Resource? | element | Details of the Resource. The XML element name SHALL be the value of ResourceSet/@Name. If the specific resource is defined in the XJDF namespace, then it SHALL have the prefix that is declared in the xmlns attribute of the root element. Specific resource SHALL be specified as the last XJDF namespace element in the Resource.<br><br>Note: This is an exception to the general instruction that all elements are ordered alphabetically. |
| <foreign namespace elements>* | element | Any elements in a foreign namespace. Foreign namespace extensions SHOULD NOT duplicate functionality of XJDF. Foreign namespace extensions SHALL be specified after all elements in the XJDF namespace. |

### 6.1.1 AmountPool

Whereas Resource/Part identifies the context of Resource that the process is consuming or producing, AmountPool is a container for the amount-related metadata of the Resource.
The interpretation of the amounts specified in an AmountPool depends on the context of the AmountPool, i.e. AmountPool elements that are specified in descendents of ResourceInfo elements SHALL specify actual amounts. All other AmountPool elements SHALL specify planned, calculated or requested amounts.

**Table 6.2: AmountPool Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| PartAmount+ | element | PartAmount SHALL specify the amounts and waste of a resource partition. |

### 6.1.2 PartAmount

PartAmount provides a container for specifying amount related attributes.
Note: Multiple PartAmount elements are used to specify partial completion of resources. For instance, specifying PartAmount/Part/@Side=”Front” for a Component would define the number of sheets that have been printed on the front side prior to printing the back side in a second press run.

#### 6.1.2.1 Specifying Amount for a Partially-Completed Process
A process can be interrupted before the requested amount of output has been produced. When the job is resent from the Controller to the Device, the Controller SHALL specify only the remaining @Amount that the Device SHALL produce in the resent job run.

**Table 6.3: PartAmount Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Amount? | float | Amount, excluding waste, in units defined in ResourceSet/@Unit or implied by Table A.3.23 Units. |
| MaxAmount? | float | Defines the planned @Amount including the maximum overage. @MaxAmount SHALL NOT be specified as actual amounts. |
| MinAmount? | float | Defines the planned @Amount including the maximum underage that the customer is willing to accept. @MinAmount SHALL NOT be specified as actual amounts. |
| Waste? | float | Waste amount in units defined in ResourceSet/@Unit or implied by Table A.3.23 Units. For a resource with a @Usage of "Input", @Waste specifies the amount of the resource that MAY be consumed or has been consumed by the process. For an resource with a @Usage of "Output", @Waste specifies the amount of the resource that MAY be produced by the process. |
| Part* | element | Part specifies the selected parts that the PartAmount is valid for. If the parent AmountPool is specified in a Resource element that also contains Part elements, then these PartAmount/Part elements SHALL NOT include any Partition Keys that are already uniquely specified in any parent Resource/Part element.<br><br>If any of these Part elements specify the same Partition Key as the parent Resource/Part element, then the value of that key SHALL match one of the values from the parent Resource/Part. |
| PartWaste* | element | Particulars of different types and/or sources of waste MAY be specified by providing one or more PartWaste elements. |

### 6.1.3 Part

Part elements define the context in which the individual Resource is used. Resource partitions are uniquely identified by the Resource/Part elements. If multiple Part elements are specified within one Resource, the Resource specifies one entity that applies to all parts.
Note: The attributes of Part are also referred to as Partition Keys.

**Table 6.4: Part Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BinderySignatureID? | NMTOKEN | Master identifier of a BinderySignature. |
| BlockName? | NMTOKEN | @BlockName SHALL identify a CutBlock from a Cutting process. The value of this attribute SHALL match the value of the @BlockName attribute of a CutBlock that produces this resource. @BlockName SHOULD be unique in the context of a job.<br><br>Note: Part/@BlockName identifies partitions that have been created by cutting in a previous process. When used as an input resource to a Cutting process, CuttingParams/CutBlock/@BlockName identifies the partitions that SHALL be created. |
| ContactType? | NMTOKEN | @ContactType specifies the role of a contact. @ContactType SHALL be provided for ResourceSet[@Name=”Contact”].<br><br>Values include those from: Contact Types. |
| DocIndex? | IntegerRange | @DocIndex SHALL select a set of logical Instance Documents. The index SHALL refer to the list of documents in the context of all Document Sets selected by @SetIndex. Specifying @DocIndex in a RunList SHALL select individual documents without modifying the page position. |
| DropID? | NMTOKEN | Identifier of an individual drop within a Delivery. A drop represents one or more items being delivered to one address at one point in time.<br><br>If multiple DeliveryParams contain the same @DropID, they SHOULD be delivered in one delivery, regardless of whether the DeliveryParams are defined in the same XJDF or not. |
| Location? | NMTOKEN | Name of a location. This part key allows the description of distributed ResourceSet items.<br><br>Values include those from: Input Tray and Output Bin Names. |
| LotID? | NMTOKEN | Identifier of the lot of a resource in a lot controlled environment. Examples include individual reels for web printing. |
| Metadata? | regExp | Metadata SHALL match metadata extracted from a PDL using RunList/MetadataMap or IdentificationField/MetadataMap. See Section 8.29 MetadataMap. |
| Option? | NMTOKEN | Generic option that MAY be semantic free. |
| PageNumber? | IntegerRange | @PageNumber is a zero-based page number. @PageNumber is used when a document/file-based RunList is broken down into a page-based RunList or a page-based RunList is combined into a document-based RunList. |
| PartVersion? | NMTOKEN | Version identifier (e.g., the language version of a catalog). |
| PreviewType? | enumeration | @PreviewType specifies the type and usage of a Preview. @PreviewType SHALL NOT be specified for resources other than Preview or PreviewGenerationParams.<br><br>Allowed values are:<br>• Animation – Animated previews for 3D display.<br>• Identification – Preview is used as a visual help to identify one or more products, e.g. on a Gang form.<br>• SeparatedThumbNail – Very low resolution separated preview.<br>• Separation – Separated preview in medium resolution.<br>• SeparationRaw – Separated preview in medium resolution with no compensation.<br>• Static3D – Static 3D model.<br>• ThumbNail – Very low resolution RGB preview.<br>• Viewable – RGB preview in medium resolution. |
| PrintCondition? | NMTOKEN | @PrintCondition specifies a characterization data set that is applied to a specific setup including paper selection and screening. See PrintCondition for details of characterization data sets. |
| Product? *(New in XJDF 2.1)* | NMTOKEN | References the Product/@ExternalID that this Part applies to. |
| ProductPart? *(Deprecated in XJDF 2.1)* | NMTOKEN | References the Product/@ID that this Part applies to.<br>Deprecation note: Use @Product to reference Product/@ExternalID. |
| QualityMeasurement? | NMTOKEN | Identifier of an individual quality measurement in a QualityControl process. |
| Run? | NMTOKEN | @Run identifies an individual RunList Resource. |
| RunIndex? | IntegerRange | @RunIndex SHALL select a set of logical pages from a RunList resource in a manner that is independent from the internal structure of the RunList. The index SHALL refer to the list of pages in the context of all documents and sets selected by @DocIndex and @SetIndex. Specifying @RunIndex in a RunList SHALL select individual pages without modifying the page position. |
| Separation? | NMTOKEN | Identifies a color separation. If the separation name can be represented as an NMTOKEN, the value of @Separation SHOULD be identical to the separation name. Otherwise the separation name SHALL be provided in Resource/Color/@ActualColorName of a Resource that contains a matching value of Part/@Separation.<br><br>Values include: Cyan, Magenta, Yellow, Black, Red, Green, Blue, Orange, Spot, Varnish, none. |
| SetIndex? | IntegerRange | The @SetIndex attribute SHALL select a set of logical Instance Document Sets. The index always refers to entries of the entire RunList. Specifying @SetIndex in a RunList SHALL select individual Document Sets without modifying the page position. |
| SheetIndex? | IntegerRange | @SheetIndex selects a set of logical sheets from a RunList resource either implicitly or explicitly partitioned by @SheetIndex. @SheetIndex SHALL NOT be specified unless the RunList is describing imposed sheets or surfaces. |
| SheetName? *(Modified in XJDF 2.2)* | NMTOKEN | @SheetName specifies a name that identifies a press sheet. The value of @SheetName SHALL be unique within the context of a job. |
| Side? | enumeration | Denotes the side of the sheet.<br><br>If @Side is specified, the Part element refers to one surface of the sheet. In case of web printing, "Front" is a synonym for the upper side and "Back" for the down side of the web.<br><br>Allowed value is from: Side. |
| StationName? | NMTOKEN | The name of the 1-up design in a DieLayout. |
| TileID? | XYPair | XYPair of integer values that identifies a tile when a surface has been split into multiple tiles by the Imposition process. Values are zero-based and SHALL originate at the lower left. So "0 0" is the lower left tile and "1 0" is the tile next to it on the right. |
| TransferCurveName? | enumeration | @TransferCurveName SHALL specify the destination system that the TransferCurve SHALL apply to.<br><br>Allowed values are: Film, Plate, Press, Proof, Substrate. |
| WebName? *(Modified in XJDF 2.2)* | NMTOKEN | @WebName specifies a name that identifies a web on a web press. The value of @WebName SHALL be unique within the context of a job. |

#### 6.1.3.1 Partition Bootstrapping
Partition bootstrapping is the process that is employed by a consuming Device to identify the Resource partitions that SHOULD be used when executing an XJDF process.
ResourceSet[@Name="NodeInfo"] defines the structure of the individual planned process steps. Thus the list of ResourceSet[@Name="NodeInfo"]/Resource/Part defines the planned partitions that SHALL be searched for each work-step. The NodeInfo structure MAY be a superset of the actual processes, since the planning can be less granular than the capabilities of a given Device.
Note: In general the partitioning of NodeInfo will correspond to the partitioning of the least granular resources.

#### 6.1.3.2 Selecting a Partition
*(Modified in XJDF 2.2)* A matching partition for a given set of Partition Keys is selected by iterating the Resource elements of the respective ResourceSet from top to bottom. If any of the Resource/Part elements has no mismatching attributes, that Resource SHALL be selected. If a single result is expected, the iteration SHALL stop after the first match. If multiple results are expected, the iteration SHALL continue for all Resource elements of the ResourceSet.
If a Resource with an @ID is referenced by an attribute with the data type of IDREF, then the Resource with a matching @ID SHALL be selected and any Resource/Part elements SHALL be ignored.

#### 6.1.3.3 Multiple Part Elements in One Resource
A ResourceSet MAY contain one or more Resource elements that MAY respectively contain zero or more Part elements. Each Resource represents one entity, regardless of the number of Part elements. If a Resource contains more than one Part element, this Resource is applicable to any of the contained Part elements.

**Example 6.1: Versioned Set Of Plates with Multiple Part Elements**
```xml
<ResourceSet Usage="Output" Name="ExposedMedia"> 
  <!-- 3 Common Plates for English and French--> 
  <Resource> 
    <Part Separation="Cyan" PartVersion="English"/> 
    <Part Separation="Cyan" PartVersion="French"/> 
    <ExposedMedia MediaRef="EM42"/> 
  </Resource> 
  <!-- Specific Black Plate for English--> 
  <Resource> 
    <Part Separation="Black" PartVersion="English"/> 
    <ExposedMedia MediaRef="EM42"/> 
  </Resource> 
</ResourceSet>
```

### 6.1.4 PartWaste
PartWaste associates waste with individual Device modules or waste types.
Note: The sum of specific waste can be higher than the total waste due to double counting.

**Table 6.5: PartWaste Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ModuleIDs? | NMTOKENS | Specifies the module or modules where the waste was produced. |
| Waste | float | Specific waste amount that SHALL be in the same units as those of the parent PartAmount. |
| WasteDetails? | NMTOKEN | @WasteDetails specifies additional details about how the waste was produced. See Table 6.6 WasteDetails Attribute Values for suggested values.<br><br>At least one of @ModuleIDs or @WasteDetails SHALL be specified. |

**Table 6.6: WasteDetails Attribute Values**

| VALUE | DESCRIPTION |
| --- | --- |
| AuxiliarySheet | This value identifies InsertSheet media that was consumed as specified by StackingParams/Disjointing/InsertSheet. |
| BadFeedWaste | Waste caused by a bad feed. |
| BindingQualityTest | Components that were destroyed in a QualityControl process that tests binding quality. |
| CaliperWaste | Waste by caliper on gathering/ collecting. |
| DoubleFeedWaste | Waste by double feeds on feeders. |
| IncorrectComponentWaste | Waste by the attempted use of an incorrect component (e.g., on a feeder). |
| ObliqueSheetWaste | Waste by oblique sheets on gathering/ collecting chains. |
| Overrun | Excess Component resource(s) that were produced by running the Device after the specified amount had been produced. |
| PaperJamWaste | Waste by paper or other media jam. |
| Rejected | Rejected in an approval process. |
| Reusable | Waste to be used for setup in the next process. |
| Waste | General waste. |
| WhitePaperWaste | White paper waste. |

## 6.2 ApprovalDetails
The signed ApprovalDetails resource indicates whether a resource has been approved or rejected.
*Resource Properties: Intent Pairing: ContentCheckIntent | Input of Processes: Any Process | Output of Processes: Approval*

**Table 6.7: ApprovalDetails Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ApprovalState | enumeration | Decision made by the approver.<br>Allowed values are: Approved, ApprovedWithComment, Rejected. |
| ApprovalStateDetails? | string | Additional details on the decision made by the approver are specified in this @ApprovalStateDetails. |
| ApprovalPerson? | element | Details of the person (e.g., a customer, printer or manager) who processed the approval. |
| Comment? *(Deprecated in XJDF 2.1)* | element | This Comment provides a container for human readable notes that are provided by the approver.<br>Deprecation note: Use ../Resource/Comment. |
| FileSpec? | element | FileSpec SHALL refer to a representation of a digital signature. |

## 6.3 ApprovalParams
ApprovalParams provides the details of an Approval process.
*Resource Properties: Intent Pairing: ContentCheckIntent | Input of Processes: Approval*

**Table 6.8: ApprovalParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ApprovalPerson+ | element | List of people (e.g., a customer, printer or manager) who can sign the approval. |

## 6.4 Assembly
Assembly describes how multiple BinderySignatures are bound together to produce a bound product.
*Resource Properties: Intent Pairing: LayoutIntent | Input of Processes: Collecting, Gathering, SheetOptimizing, Stripping*

**Table 6.9: Assembly Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BinderySignatureIDs? | NMTOKENS | @BinderySignatureIDs specifies an ordered list of BinderySignature that SHALL be assembled by the method specified in @Order. @BinderySignatureIDs SHALL NOT be present, if @Order="List". |
| Order | enumeration | Ordering of the individual BinderySignature elements. Order specifies the topology of the final Assembly.<br>Allowed values are: Collecting, Gathering, List, None. |
| AssemblySection* | element | Each AssemblySection represents one section that SHALL be gathered. AssemblySection elements SHALL NOT be specified unless @Order="List" and SHALL be specified if @Order="List". |

### 6.4.1 AssemblySection
An AssemblySection represents a recursive set of BinderySignature elements. The topology of the AssemblySection elements represents the topology of the binding, where sibling AssemblySection elements SHALL be gathered from top to bottom and child AssemblySection elements SHALL be collected from outside to inside.

**Table 6.10: AssemblySection Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BinderySignatureID | NMTOKEN | @BinderySignatureID identifies the BinderySignature that this AssemblySection represents. |
| CommonFolds? *(New in XJDF 2.1)* | integer | A value, that is greater than "1", that specifies the number of consecutive bindery signatures that SHALL be gathered prior to folding. |
| DescriptiveName? *(New in XJDF 2.1)* | string | Human readable descriptive name of the AssemblySection. |
| ExternalID? *(New in XJDF 2.1)* | NMTOKEN | External identifier of the AssemblySection, e.g. in an MIS. |
| AssemblySection* | element | Additional child AssemblySection elements that SHALL be gathered. |

**Example 6.2: Gathering of AssemblySections**
```xml
<ResourceSet Name="Assembly" Usage="Input"> 
  <Resource> 
    <Assembly Order="List"> 
      <AssemblySection BinderySignatureID="bs1" CommonFolds="2"/> 
      <AssemblySection BinderySignatureID="bs2"/> 
    </Assembly> 
  </Resource> 
</ResourceSet>
```

## 6.5 BendingParams
BendingParams describes the parameter set for a plate bending and punching Device. A plate is bent and/or punched to fit the press cylinder.
*Resource Properties: Input of Processes: Bending*

**Table 6.11: BendingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Bend? | boolean | If "true", indicates that the Device SHALL bend. |
| Punch? | boolean | If "true", indicates that the Device SHALL create registration punch holes. |
| PunchType? | NMTOKEN | Name of the registration punch scheme (e.g., Bacher). |

## 6.6 BinderySignature
A BinderySignature represents both sides of a folding signature, a die cut surface or a flat product such as a postcard, each with one or more pages. Resource/Part/@BinderySignatureID SHALL be provided for a BinderySignature.
*Resource Properties: Intent Pairing: LayoutIntent | Input of Processes: SheetOptimizing, Stripping*

**Table 6.12: BinderySignature Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BinderySignatureSize? | XYPair | Size of the BinderySignature. @BinderySignatureSize SHALL be identical to the sum of sizes including trim of all SignatureCells. |
| BinderySignatureType | enumeration | @BinderySignatureType specifies the type of BinderySignature and the pagination rules.<br>Allowed values are: Die, Fold, Grid. |
| BindingOrientation? | enumeration | After folding a BinderySignature, the default coordinate system SHALL be the coordinate system with the binding edge on the left side and the jog edge at the top. @BindingOrientation defines the transformation that SHALL be applied to the BinderySignature prior to calculating pagination.<br>Allowed value is from: Orientation. |
| Bottling? | enumeration | @Bottling SHALL specify the method to use for compensating the bottle angle.<br>Allowed values are: All, Last, None. |
| DieLayoutRef? | IDREF | @DieLayoutRef references a pre-existing die. |
| FoldCatalog? | NMTOKEN | @FoldCatalog describes folding of the BinderySignature. Values include those from: Fold Catalog. |
| NumberUp? | XYPair | Specifies a regular, multi-up grid of SignatureCell elements into which content pages are mapped. |
| Overfold? | float | Size of the overfold. |
| OverfoldSide? | enumeration | Position of the overfold in the finished signature.<br>Allowed values are: Back, BackHalf, Front, FrontHalf. |
| SpreadType? *(New in XJDF 2.1)* | enumeration | @SpreadType SHALL specify how the pages for the BinderySignature are delivered. |
| StaggerColumns? | FloatList | A list of values describing the staggering for subsequent columns. |
| StaggerContinuous? | boolean | Indicates if the BinderySignature SHALL be considered as a continuous repetition for staggering. |
| StaggerRows? | FloatList | A list of values describing the staggering for subsequent rows. |
| MultiPageFold* *(New in XJDF 2.1)* | element | MultiPageFold elements SHALL reference all BinderySignature elements including this BinderySignature that are produced in common so that each surface of the final folded signature contains more than one Reader Page. |
| SignatureCell* | element | Describes the SignatureCell elements used in this BinderySignature. |

### 6.6.1 MultiPageFold
*(New in XJDF 2.1)*

**Table 6.13: MultiPageFold Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BinderySignatureID | NMTOKEN | @BinderySinatureID shall reference the BinderySignature that is produced in common. |
| Orientation | enumeration | Orientation of the referenced BinderySignature prior to the final cut.<br>Allowed value is from: Orientation. |

*Figure 6-1: Pagination diagram for Two Up Head to Foot.*
*Figure 6-2: Pagination diagram for Come and Go.*

### 6.6.2 SignatureCell
SignatureCell elements describe the geometry of one or more individual page cells in a BinderySignature.

**Table 6.14: SignatureCell Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BackPages? | IntegerList | Page numbers of the back pages of a SignatureCell. |
| BackSpread? | IntegerList | List of indices of SignatureCell elements that are combined into a spread on the back side. |
| BleedFace? | float | Amount of bleed that SHALL be added to the box defined in @TrimBox at the face side. |
| BleedFoot? | float | Amount of bleed that SHALL be added to the box defined in @TrimBox at the foot side. |
| BleedHead? | float | Amount of bleed that SHALL be added to the box defined in @TrimBox at the head side. |
| BleedSpine? | float | Amount of bleed that SHALL be added to the box defined in @TrimBox at the spine side. |
| FaceCells? | IntegerList | List of indices of SignatureCell elements that form a foldout together with this SignatureCell. |
| FrontPages? | IntegerList | Page numbers of the front pages of a SignatureCell. |
| FrontSpread? | IntegerList | List of indices of SignatureCell elements that are combined into a spread on the front side. |
| Mask? | enumeration | The definition of the clipping mask for the placed graphics.<br>Allowed values are: BleedBox, DieCut, None, PDL, SourceBleedBox, SourceTrimBox, TrimBox. |
| MaskBleed? | float | The distance over which to expand the mask in points. |
| MaskSeparation? | NMTOKEN | Color/../Resource/Part/@Separation of the Color that specifies @Mask. |
| Orientation? | enumeration | Indicates the orientation of the SignatureCell on the BinderySignature.<br>Allowed values are: Down, Left, Right, Up. |
| Sides? | enumeration | @Sides SHALL specify which side of the finished product SHALL be printed. |
| StationName? | NMTOKEN | The name of the 1-up station in the die layout. |
| TrimFace? | float | Value for the trim distance at the face side. |
| TrimFoot? | float | Value for the trim distance at the foot side. |
| TrimHead? | float | Value for the trim distance at the head side. |
| TrimSize? | XYPair | @TrimSize defines the dimensions of the trim box. |
| TrimSpine? | float | Amount of paper that is not cut-off from the spine. |
| CellCondition* *(New in XJDF 2.1)* | element | CellCondition defines conditions on which page content SHALL NOT be placed in the parent SignatureCell. |

#### 6.6.2.1 CellCondition
The CellCondition element defines restrictions on when content SHALL NOT be placed in its parent SignatureCell.

**Table 6.15: CellCondition Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Side | enumeration | Side of the SignatureCell that this CellCondition applies to.<br>Allowed value is from: Side. |
| Condition+ | element | This CellCondition SHALL be applied when all Condition elements evaluate to true. |

### 6.6.3 Definition of Margins in Signature Cell
*Figure 6-3: Diagram defining margins in SignatureCell (TrimHead, BleedHead, Overfold, TrimSpine, BleedSpine, TrimFace, BleedFace, TrimFoot, BleedFoot, TrimSize).*

## 6.7 BlockPreparationParams
BlockPreparationParams describes the settings of a BlockPreparation process.
*Resource Properties: Intent Pairing: BindingIntent | Input of Processes: BlockPreparation*

**Table 6.16: BlockPreparationParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Backing? | float | Backing distance in points. |
| Rounding? | float | Rounding distance in points. |
| TightBacking? | enumeration | Definition of the geometry of the back of the book block. |
| RegisterRibbon* | element | Description of the register ribbons that are included within the book block. |

*Figure 6-4: Diagram showing Backing and Rounding measurements for TightBacking.*

## 6.8 BoxFoldingParams
BoxFoldingParams defines the parameters for folding and gluing blanks to folded flat boxes in a box folder-gluer Device.
*Resource Properties: Input of Processes: BoxFolding*

**Table 6.17: BoxFoldingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BlankDimensionsX? | FloatList | @BlankDimensionsX contains a list of the X positions of folds for an unfolded box. |
| BlankDimensionsY? | FloatList | @BlankDimensionsY contains a list of the Y positions of folds for an unfolded box. |
| BoxFoldingType | enumeration | Basic predefined folding types. Allowed value is from: Table 6.18 BoxFoldingType Attribute Values. |
| BoxFoldAction* | element | Individual work step in a box folder-gluer. |
| Glue* *(Deprecated in XJDF 2.2)* | element | Specification of a glue line.<br>Deprecation note: From version 2.2, use BoxFoldAction/@Action="Glue" and BoxFoldAction/Glue. |

### 6.8.1 BoxFoldingType attribute values
**Table 6.18: BoxFoldingType Attribute Values**
*(Contains predefined types like Type00, Type01, Type02, etc., mapping to specific folding sequences and dimensions. Diagrams illustrate the fold layout from the print side with the lid at the top.)*

### 6.8.2 BoxFoldAction
BoxFoldAction describes an action in the folder-gluer that is perpendicular or diagonal to the movement path of the blank.

**Table 6.19: BoxFoldAction Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Action | enumeration | @Action describes an individual action in the folder gluer. Allowed value is from: Table 6.20 Action Attribute Values. |
| FoldIndex | XYPair | Pair of indices that identify the upper right corner of the flap or fold that is affected by this BoxFoldAction. |
| Glue? *(New in XJDF 2.2)* | element | Glue SHALL provide details of the glue application. |

**Table 6.20: Action Attribute Values**
Values include: BackFoldComplete, BackFoldCompleteDiagonal, BackFoldDiagonal, FrontFoldComplete, FrontFoldCompleteDiagonal, FrontFoldDiagonal, Glue, LongFoldLeftToRight, LongFoldRightToLeft, LongPreFoldLeftToRight, LongPreFoldRightToLeft, Milling, ReverseFold, Rotate180, Rotate270, Rotate90.

## 6.9 BoxPackingParams
BoxPackingParams defines the parameters for packing a box of components.
*Resource Properties: Input of Processes: BoxPacking*

**Table 6.21: BoxPackingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BoxType | enumeration | @BoxType specifies the general category of the package to be packed.<br>Allowed values are: Box, Carton, Envelope, Tube. |
| BoxTypeDetails? | string | Additional details of @BoxType. |
| Columns? | integer | Columns per shipping box. |
| ComponentsPerRow? | integer | Components or Products per row in the shipping box. |
| Copies? | integer | Number of copies in the box. |
| FaceDown? | enumeration | Defines the surface that is facing the bottom of the box. |
| Layers? | integer | Layers per shipping box. |
| MaxWeight? | float | Maximum weight of a packed box in grams. |
| Pattern? | NMTOKEN | Name of the box packing pattern. |
| Rows? | integer | Rows per shipping box. |
| Ties? | IntegerList | Number of tie sheets at each row. |
| UnderLays? | IntegerList | Number of underlay sheets at each layer. |

*Figure 6-6: Diagram illustrating Box packing parameters (Rows, Layers, Ties, UnderLays).*

## 6.10 Bundle
Bundles are used to describe various kinds of sets of packing units such as boxes, cartons and pallets.
*Resource Properties: Input of Processes: BoxPacking, Bundling, Labeling, Palletizing, Shrinking, Stacking, Strapping, Wrapping*

**Table 6.22: Bundle Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BundleItem+ | element | References to the individual BundleItems that form this Bundle. |

### 6.10.1 BundleItem
A Bundle is described as a set of BundleItem elements.

**Table 6.23: BundleItem Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Amount | integer | Number of identical bundle items of the same type and structure that this BundleItem represents. |
| BundleType? | enumeration | @BundleType defines the type of bundle that the BundleItem represents. |
| ItemRef? | IDREF | Reference to an individual Component or Product that is represented by this BundleItem. |
| TotalAmount? | integer | Total amount of individual products that this BundleItem contains. |
| TotalDimensions? | shape | Total dimensions in points of all individual items including packaging. |
| TotalVolume? | float | Total volume in liters of all individual items including packaging. |
| TotalWeight? | float | Total weight in grams of all individual items including packaging. |
| BundleItem* | element | Individual BundleItem elements that this parent BundleItem contains. |

*Figure 6-7: Bundle coordinate system diagram (X-Axis, Y-Axis along spine, Z-Axis height).*

## 6.11 BundlingParams
BundlingParams describes the details of a Bundling process.
*Resource Properties: Input of Processes: Bundling*

**Table 6.24: BundlingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Copies? | integer | Number of products within a bundle. |
| Length? | float | Length of a bundle. |

## 6.12 CaseMakingParams
CaseMakingParams describes the settings of a CaseMaking process for hardcover binding.
*Resource Properties: Intent Pairing: BindingIntent | Input of Processes: CaseMaking*

**Table 6.25: CaseMakingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BottomFoldIn? | float | Defines the width of the part of the cover material on the lower edge inside of the case. |
| CornerType? | NMTOKEN | Method of wrapping the corners of the cover material around the corners of the board. |
| CoverWidth? | float | Width of the cover cardboard in points. |
| FrontFoldIn? | float | Defines the width of the part of the cover material on the front edges inside of the case. |
| Height? | float | Height of the book case, in points. |
| JointWidth? | float | Width of the joint as seen when laying the cardboard on the cover material. |
| SpineWidth? | float | Width of the spine cardboard, in points. |
| TopFoldIn? | float | Defines the width of the cover material on the top edge inside of the case. |
| Glue? | element | Details of the glue. |

*Figure 6-9: Diagram illustrating CaseMakingParams geometry.*

## 6.13 CasingInParams
CasingInParams describes the settings of a CasingIn process. The geometry SHALL always be centered.
*Resource Properties: Intent Pairing: BindingIntent | Input of Processes: CasingIn*

**Table 6.26: CasingInParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CaseRadius? | float | Inner radius of the case spine rounding. |
| CoverWidth? | float | Width of the cover board. |
| SpineWidth? | float | Width of the spine board. |
| Glue* | element | Properties of the glue to attach the case. |

## 6.14 Color
Color describes the details of spot color inks, process color inks and any other coating.
*Resource Properties: Intent Pairing: ColorIntent | Input of Processes: Any Process*

**Table 6.27: Color Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ActualColorName? | string | Actual name of the color in the PDL. |
| CMYK? | CMYKColor | CMYK value of the 100% tint value of the colorant. |
| ColorBook? | string | Definition of the color identification book name. |
| ColorBookEntry? | string | Definition of the Color within the standard specified by @ColorBook. |
| ColorDetails? | string | A more specific, specialized or site-defined name for the color. |
| ColorName? | enumeration | Mapping to a color name. Allowed value is from: NamedColor. |
| ColorType? | enumeration | A name that characterizes the colorant.<br>Allowed values are: DieLine, Normal, Opaque, OpaqueIgnore, Primer, Transparent. |
| ColorTypeDetails? | string | Additional information about the color type. |
| Density? | float | Density value of colorant (100% tint). |
| Gray? | float | Gray value of the 100% tint value of the colorant. |
| Lab? | LabColor | L, a, b value of the 100% tint value of the colorant. |
| NeutralDensity? | float | A number in the range of 0.001 to 10 that represents the neutral density of the colorant. |
| PrintingTechnology? | NMTOKEN | Printing technology of the press, press module or printer. |
| PrintStandard? *(Deprecated in XJDF 2.1)* | string | Specifies the reference name of a characterization data set.<br>Deprecation note: Use PrintCondition. |
| RawName? | hexBinary | Representation of the original 8-bit byte stream of the ../Part/@Separation. |
| Spectrum? *(New in XJDF 2.1)* | Transfer-Function | Spectrum of the color as measured with the measurement conditions defined in ColorMeasurementConditions. |
| sRGB? | RGBColor | sRGB value of the 100% tint value of the colorant. |
| ColorMeasurementConditions? *(New in XJDF 2.1)* | element | Contains the color measurement conditions. |
| DeviceNColor* | element | Each DeviceNColor element defines the colorant in the DeviceN color space. |

### 6.14.1 DeviceNColor
**Table 6.28: DeviceNColor Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ColorList | FloatList | Value of the 100% tint value of the colorant in the ColorantControl/DeviceNSpace. |
| Name | NMTOKEN | @Name of the matching DeviceNSpace. |

## 6.15 ColorantControl
ColorantControl defines how color separations of PDL or raster data SHALL be output on a target Device or file.
*Resource Properties: Intent Pairing: ColorIntent | Input of Processes: ColorCorrection, ColorSpaceConversion, ConventionalPrinting, DigitalPrinting, ImageSetting, Interpreting, PreviewGeneration, QualityControl, Separation, Stripping, Trapping | Output of Processes: ColorSpaceConversion*

**Table 6.29: ColorantControl Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ColorantConvertProcess? | NMTOKENS | List of colors that SHALL be converted to process colors. |
| ColorantOrder? | NMTOKENS | The ordering of the colorant identifiers to be processed. |
| ColorantParams? | NMTOKENS | @ColorantParams defines all the colorant identifiers that are expected to be available on the printing Device. |
| MappingSelection? | enumeration | @MappingSelection specifies how a combination of process colorant values SHALL be obtained for any spot color. |
| ProcessColorModel? | enumeration | Specifies the model to be used for rendering the colorants.<br>Allowed values are: DeviceCMY, DeviceCMYK, DeviceGray, DeviceN, DeviceRGB, None. |
| ColorantAlias* | element | Each ColorantAlias is used to map a color name from the PDL to a separation. |
| DeviceNSpace? | element | DeviceNSpace defines the colorants that make up a DeviceN color space. |

### 6.15.1 ColorantAlias
**Table 6.30: ColorantAlias Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ColorantName | string | The name of the colorant that SHALL be replaced in PDL files. |
| RawName? | hexBinary | @RawName represents the original 8-bit byte stream of the color. |
| ReplacementColorantName | NMTOKEN | The separation identifier that SHALL be selected for the colorant @ColorantName. |

### 6.15.2 DeviceNSpace
**Table 6.32: DeviceNSpace Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Name | NMTOKEN | Name of the DeviceNSpace. |
| Separations | NMTOKENS | Ordered list of colorant identifiers that define the DeviceN color space. |

## 6.16 ColorCorrectionParams
ColorCorrectionParams provides the information needed to algorithmically correct colors on some PDL pages or content elements.
*Resource Properties: Intent Pairing: ColorIntent | Input of Processes: ColorCorrection*

**Table 6.33: ColorCorrectionParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ColorCorrectionOp* | element | List of ColorCorrectionOp subelements. |

### 6.16.1 ColorCorrectionOp
**Table 6.34: ColorCorrectionOp Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| AdjustContrast? | float | Specifies the L*a*b* contrast adjustment in the range -100 to +100. |
| AdjustHue? | float | Specifies the change in the L*a*b* hue in the range -180 to +180. |
| AdjustLightness? | float | Specifies the decrease or increase of the L*a*b* lightness in the range -100 to +100. |
| AdjustSaturation? | float | Specifies the increase or decrease of the L*a*b* color saturation in the range -100 to +100. |
| SourceObjects? | enumerations | Identifies which class(es) of incoming graphical objects SHALL be operated on. |

## 6.17 ColorSpaceConversionParams
This set of parameters defines the rules for a ColorSpaceConversion process.
*Resource Properties: Intent Pairing: ColorIntent | Input of Processes: ColorSpaceConversion*

**Table 6.35: ColorSpaceConversionParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ICCProfileUsage? | enumeration | @ICCProfileUsage specifies where to obtain either the destination profile or Device link transform.<br>Allowed values are: UsePDL, UseSupplied. |
| ColorSpaceConversionOp* | element | List of ColorSpaceConversionOp elements. |
| FileSpec? | element | A FileSpec element pointing to an ICC profile. |

### 6.17.1 ColorSpaceConversionOp
**Table 6.36: ColorSpaceConversionOp Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BlackPointCompensation? *(New in XJDF 2.2)* | boolean | If "true", black point compensation SHALL be applied. |
| BlackPointCompensationDetails? *(New in XJDF 2.2)* | NMTOKEN | @BlackPointCompensationDetails SHALL specify the implementation dependent algorithm. |
| Operation | enumeration | @Operation specifies the task.<br>Allowed values are: Convert, Tag, Untag. |
| PreserveBlack? | boolean | Controls how the tints of black (K in CMYK) SHALL be handled. |
| RenderingIntent? | enumeration | Identifies the rendering intent to be applied. |
| RGBGray2Black? | boolean | Controls what happens to gray values (R=G=B) when converting from RGB to CMYK. |
| RGBGray2BlackThreshold? | float | Threshold value above which the Device SHALL NOT convert gray to black. |
| Separations? | NMTOKENS | List of separation identifiers that specify on which separation(s) to operate. |
| SourceCS? | enumeration | Identifies which of the incoming color spaces SHALL be operated on. |
| SourceObjects? | enumerations | List of object classes that identifies which incoming graphical objects SHALL be operated on. |
| SourceRenderingIntent? | enumeration | Identifies the rendering intent transform elements to be selected from the source profile. |
| FileSpec (DeviceLinkProfile)? | element | FileSpec specifies an ICC profile file that contains a Device link transform. |
| FileSpec (PDLSourceProfile)? | element | FileSpec specifies an ICC profile that describes a profiled source color space. |
| FileSpec (SourceProfile)? | element | FileSpec specifies an ICC profile that SHALL be used as the profile for the source object’s color space. |
| ScreenSelector? | element | If specified, only objects that match the screening properties defined in ScreenSelector SHALL be operated on. |

## 6.18 Component
Component is used to describe the unprinted media, Partial and Final Products in the press and postpress area.
*Resource Properties: Input/Output of many Prepress, Press, and Postpress Processes.*

*Figure 6-11: Diagram defining Component Terms (Binding edge, Face Side, Product front side, etc.).*

**Table 6.37: Component Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Automation? | enumeration | Identifies dynamic and static components. Allowed value is from: Automation. |
| CartonTopFlaps? | XYPair | Size of the two top flaps of a carton or box. |
| Columns? | integer | Number of columns of images that are placed on a finished roll. |
| ContentRefs? | IDREFS | Reference to Content that provides metadata related to this Component. |
| Dimensions? | shape | The dimensions of the component. |
| MaxHeat? | float | Maximum temperature the Component can resist. |
| MediaRef? | IDREF | Reference to the Media for this Component. |
| Overfold? | float | Expansion of the overfold of a Component. |
| OverfoldSide? | enumeration | Specifies the longer side of a folded component. |
| ProductType? | NMTOKEN | Type of product that this component specifies. |
| ProductTypeDetails? | string | @ProductTypeDetails specifies additional details of the product. |
| ReaderPageCount? *(Deprecated in XJDF 2.1)* | integer | Total amount of individual Reader Pages.<br>Deprecation note: Use @SurfaceCount. |
| SurfaceCount? | integer | Total amount of individual surfaces that this Component contains. |
| WindingResult? | integer | Orientation of the finished product on the roll. |
| IdentificationField* | element | IdentificationField associates bar codes or labels with this Component. |

## 6.19 Contact
Contact describes a person or a role within an organization.
*Resource Properties: Input of Processes: Any Process*

**Table 6.38: Contact Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ContactTypeDetails? | NMTOKENS | @ContactTypeDetails specifies the details of the contact's role or roles. |
| CostCenterID? | NMTOKEN | Identifier of the cost center. |
| UserID? | string | User ID of user. |
| Address? | element | Element describing the address. |
| ComChannel* | element | Communication channels such as phone number or email. |
| Company? | element | Company that this Contact is associated with. |
| Person? | element | Name of the contact person. |

### 6.19.1 ComChannel
**Table 6.39: ComChannel Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ChannelType | NMTOKEN | Type of the communication channel (Email, Fax, JMF, Mobile, Phone, WWW). |
| ChannelUsage? | NMTOKENS | Communication channel usage (Business, DayTime, NightTime, Private, WeekEnd). |
| DescriptiveName? | string | Human readable representation of ComChannel. |
| Locator | string | Locator of this type of channel (e.g. phone number, URL). |

### 6.19.2 Company
**Table 6.40: Company Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CompanyID? | NMTOKEN | An ID of the company. |
| DescriptiveName? | string | Human readable representation of Company. |
| OrganizationName | string | Name of the organization or company. |
| OrganizationalUnit* *(JSON Exception)* | element | Describes one or more organizational units. (Encoded as an ‘array of string’ in JSON). |

### 6.19.3 OrganizationalUnit
**Table 6.41: OrganizationalUnit Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| (Text) | text | Description of one organizational unit. |

### 6.19.4 Person
**Table 6.42: Person Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| AdditionalNames? | string | Additional names of the contact person. |
| DescriptiveName? | string | Human readable representation of Person. |
| FamilyName? | string | The family name of the contact person. |
| FirstName? | string | The first name of the contact person. |
| JobTitle? | string | Job function of the person in the company. |
| Languages? | languages | List of languages related to the person. |
| NamePrefix? | string | Prefix of the name. |
| NameSuffix? | string | Suffix of the name. |
| PhoneticFirstName? | string | Alternative spelling of a first name. |
| PhoneticLastName? | string | Alternative spelling of a last name. |

## 6.20 Content
Content defines the additional metadata of individual graphic elements.
*Resource Properties: Referenced by RunList*

**Table 6.43: Content Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BinderySignatureIDs? | NMTOKENS | List of BinderySignature/@BinderySignatureID of all bindery signatures that this Content applies to. |
| ContentStatus? | NMTOKENS | Status of a single Content element. |
| ContentType? | NMTOKEN | Type of content. |
| HasBleeds? | boolean | If "true", the Content has bleeds. |
| IsBlank? | boolean | If "true", the Content has no content marks and is blank. |
| IsTrapped? | boolean | If "true", the Content has been trapped. |
| PageLabel? | string | Complete identification of the Finished Page. |
| Separations? | NMTOKENS | List of separation identifiers that are present in the content. |
| SourceBleedBox? | rectangle | A rectangle that describes the bleed area of the content. |
| SourceClipBox? | rectangle | A rectangle that defines the region of the finished content to be included. |
| SourceTrimBox? | rectangle | A rectangle that describes the intended trimmed size. |
| BarcodeProductionParams? | element | Description of the specific parameters for barcode production. |
| ContentMetadata? | element | Container for document related metadata. |
| FileSpec* | element | Reference to dependent references such as fonts, external images, etc. |
| ImageCompression? | element | Specification of the image compression properties. |
| OCGControl* | element | OCGControl provides a list of the OCGs (layers) that SHALL be included or excluded. |
| PositionObj* | element | Definition of the size and positioning of any child Content elements. |
| ScreenSelector? | element | Specification of the screening properties of the Content. |

### 6.20.1 BarcodeProductionParams
**Table 6.45: BarcodeProductionParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BarcodeReproParams? | element | Description of the formatting and reproduction parameters for barcode production. |
| IdentificationField? | element | Description of the barcode metadata. |

### 6.20.2 ContentMetadata
**Table 6.46: ContentMetadata Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ContactRefs? | IDREFS | @ContactRefs SHALL reference the Contacts. |
| ISBN? | NMTOKEN | An International Standard Book Number. |
| Title? | NMTOKEN | The title of the content. |
| Comment? | element | If required, an abstract MAY be specified. |
| GeneralID* | element | Additional metadata MAY be defined by adding GeneralID elements. |

### 6.20.3 PositionObj
**Table 6.47: PositionObj Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Anchor? | enumeration | Specifies the origin (0,0) of the coordinate system in the unrotated Content. |
| CTM? | matrix | Specifies the transformation matrix of the origin of Content. |
| PageRange? | IntegerRange | Reader Page index in the Content referenced by RefAnchor. |
| PositionPolicy? | enumeration | Specifies the level of freedom when applying the values. |
| RelativeSize? | XYPair | Specifies the size of the unrotated and unscaled object, relative to the parent. |
| RotationPolicy? | enumeration | Specifies the level of freedom when applying the rotation. |
| Size? | XYPair | Specifies the size of the unrotated and unscaled object, in points. |
| SizePolicy? | enumeration | Specifies the level of freedom when applying the size. |
| RefAnchor? | element | Reference to a Content that this Content is positioned relative to. |

## 6.21 ConventionalPrintingParams
ConventionalPrintingParams defines the Device specific setup of the ConventionalPrinting process.
*Resource Properties: Input of Processes: ConventionalPrinting*

**Table 6.48: ConventionalPrintingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Drying? | enumeration | The way in which ink is dried after a print run. |
| FirstSurface? | enumeration | Printing order of the surfaces on the sheet. |
| FountainSolution? | enumeration | State of the fountain solution module (On, Off). |
| ModuleDrying? | enumeration | The way in which ink is dried in individual modules. |
| Powder? | float | Quantity of powder in percent. |
| SheetLay? | enumeration | Lay of input media. |
| Speed? *(Deprecated in XJDF 2.1)* | float | Maximum print speed.<br>Deprecation note: Use Device/@MaxRunSpeed. |
| WorkStyle? | enumeration | The direction in which to turn the press sheet. |

## 6.22 CoverApplicationParams
CoverApplicationParams define the parameters for applying a cover to a book block.
*Resource Properties: Intent Pairing: BindingIntent | Input of Processes: CoverApplication*

**Table 6.49: CoverApplicationParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Glue* | element | Describes where and how to apply glue to the book block. |
| Score* | element | Describes where and how to score the cover. |

### 6.22.1 Score
**Table 6.50: Score Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Offset | float | Position of scoring given in the operation coordinate system. |
| Side | enumeration | Specifies the side from which the scoring tool works (FromInside, FromOutside). |

## 6.23 CreasingParams
CreasingParams define the parameters for creasing or grooving a sheet.
*Resource Properties: Intent Pairing: FoldingIntent | Input of Processes: Creasing*

**Table 6.51: CreasingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Crease+ | element | Each Crease element defines one crease line. |

## 6.24 CustomerInfo
The CustomerInfo resource contains information about the customer who orders the job.
*Resource Properties: Input of Processes: Any Process*

**Table 6.52: CustomerInfo Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CustomerID? | NMTOKEN | Customer identification used by the application that created the job. |
| CustomerJobName? | string | The human readable descriptive name that the customer uses to refer to the job. |
| CustomerOrderID? | string | The internal order number in the system of the customer. |
| CustomerProjectID? | string | The internal project id in the system of the customer. |

## 6.25 CuttingParams
CuttingParams describes the parameters of a Cutting process.
*Resource Properties: Intent Pairing: FoldingIntent, ShapeCuttingIntent | Input of Processes: Cutting*

**Table 6.53: CuttingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| SheetLay? | enumeration | Lay of the input Component. |
| Cut* | element | Cut elements describe an individual cut. |
| CutBlock* | element | These CutBlock elements describe the output cut blocks. |
| FileSpec(CIP3)? | element | Reference to a CIP3 file that contains cutting instructions. |

*Figure 6-15: Diagram illustrating Nested cut blocks.*

## 6.26 DeliveryParams
DeliveryParams provides information needed by a Delivery process.
*Resource Properties: Input of Processes: Delivery*

**Table 6.54: DeliveryParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BuyerAccount? | string | Account ID of the buyer with the delivery service. |
| Earliest? | dateTime | @Earliest SHALL specify the earliest date and time after which the delivery is intended to be made. |
| EarliestDuration? | duration | @EarliestDuration SHALL specify the earliest duration by which the delivery SHALL be made. |
| Method? | NMTOKEN | Specifies a delivery method. |
| Ownership? | enumeration | Point of transfer of ownership (Destination, Origin). |
| Required? | dateTime | @Required SHALL specify the date and time by which the delivery is intended to be made. |
| RequiredDuration? | duration | @RequiredDuration SHALL specify the time duration by which the delivery SHALL be made. |
| TrackingID? | string | The carrier's identifier for the delivery. |
| Transfer? | enumeration | Describes the direction and responsibility of the transfer (BuyerToPrinterDeliver, BuyerToPrinterPickup, PrinterToBuyerDeliver, PrinterToBuyerPickup). |
| DropItem* | element | A delivery MAY consist of multiple products. |
| FileSpec (DeliveryContents)? | element | Reference to a document that identifies the contents of this delivery. |
| FileSpec (MailingList)? | element | A FileSpec element pointing to a mailing list. |
| FileSpec (RemoteURL)? | element | A FileSpec element that specifies the remote location of a digital delivery. |

### 6.26.1 DropItem
**Table 6.55: DropItem Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Amount | integer | @Amount SHALL be present and specify the number of products or resources. |
| ItemRef | IDREF | @ItemRef SHALL reference the Resource, ResourceSet, ProofItem or ProductList/Product. |
| TotalDimensions? | shape | Total dimensions in points of all individual items. |
| TotalVolume? | float | Total volume in liters. |
| TotalWeight? | float | Total weight of all individual items. |

## 6.27 DevelopingParams
DevelopingParams specifies information about the chemical and physical properties of the developing and fixing process for film and plates.
*Resource Properties: Input of Processes: ImageSetting*

**Table 6.56: DevelopingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| PostBakeTemp? | float | Temperature of the post-baking process in °C. |
| PostBakeTime? | duration | Duration of the post-baking process. |
| PostExposeTime? | duration | Duration of the post-exposing process. |
| PreHeatTemp? | float | Temperature of the preheating process in °C. |
| PreHeatTime? | duration | Duration of the preheating process. |

## 6.28 Device
Device describes the physical properties of the main Device that executes an XJDF process.
*Resource Properties: Input of Processes: Any Process*

**Table 6.57: Device Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CostCenterID? | NMTOKEN | MIS cost center ID. |
| DescriptiveName? | string | Human readable description of the Device. |
| DeviceClass? | NMTOKENS | Indicates the class of Device. |
| DeviceID | NMTOKEN | Identifier of the Device. |
| DeviceType? | string | Manufacturer type ID. |
| ICSVersions? | NMTOKENS | CIP4 Interoperability Conformance Specification (ICS) Versions. |
| JDFVersions? | enumerations | Whitespace separated list of JDF and XJDF versions. |
| KnownLocalizations? | languages | A list of all language codes supported by the Device. |
| Manufacturer? | string | Manufacturer name. |
| ManufacturerURL? | URL | Web site for manufacturer. |
| MaxRunSpeed? *(New in XJDF 2.1)* | float | Maximum Device speed in units per hour. |
| Packaging? | enumerations | List of packaging methods supported (XML, Zip). |
| PresentationURL? | URL | @PresentationURL specifies a URL to a Device-provided user interface. |
| RestApiBaseURL? *(JSON Exception, New in XJDF 2.2)* | URL | @RestApiBaseURL specifies the base URL of the Device port that SHALL accept JSON-based REST API calls. |
| Revision? | string | Hardware or software version of the Device. |
| SerialNumber? | string | Serial number of the Device. |
| URLSchemes? | NMTOKENS | List of schemes supported for retrieving XJDF files (file, ftp, http, https). |
| XJMFURL? | URL | Explicit URL of the Device port that will accept XJMF messages. |
| FileSpec (CurrentSchema)? *(New in XJDF 2.1)* | element | Reference to an XML schema that describes the present limitations of the Device. |
| FileSpec (Schema)? *(New in XJDF 2.1)* | element | Reference to an XML schema that describes the global limitations of the Device. |
| IconList? | element | List of locations of icons that can be used to represent the Device. |
| IdentificationField* | element | IdentificationField associates bar codes or labels with this Device. |
| Module* | element | Individual modules that are represented by this Device. |

### 6.28.1 Icon
**Table 6.58: Icon Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BitDepth | integer | Bit depth of one color. |
| IconUsage? | enumerations | The DeviceInfo/@Status of the Device that this Icon represents. |
| Size | XYPair | Height and width of the icon in pixels. |
| FileSpec? | element | Reference to details of the icon data. |

### 6.28.2 IconList
**Table 6.59: IconList Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Icon+ | element | Individual icon description. |

### 6.28.3 Module
**Table 6.60: Module Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| DescriptiveName? | string | Human readable description of the module. |
| Manufacturer? | string | Manufacturer name. |
| ManufacturerURL? | URL | Web site for manufacturer. |
| ModuleID | NMTOKEN | Identifier of the module. |
| ModuleType? | NMTOKENS | @ModuleType provides a classification of the module. |
| Revision? | string | Hardware or software version of the Module. |
| SerialNumber? | string | Serial number of the Module. |
| IdentificationField* | element | IdentificationField associates bar codes or labels with this Module. |

## 6.29 DieLayout
DieLayout represents a die layout described in an external file.
*Resource Properties: Input of Processes: DieDesign, DieMaking | Output of Processes: DieDesign, DieLayoutProduction*

**Table 6.61: DieLayout Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CutBox? | rectangle | A rectangle describing the bounding box of all cut lines. |
| CutLines? | NMTOKENS | Selects the die line separation identifiers. |
| DieSide? | enumeration | Determines the die side (Down, Up). |
| MediaSide? | enumeration | Determines the printing side (Front, Back). |
| Rotated? | boolean | Indicates if some structural designs are oriented cross grain/flute. |
| Waste? | float | The percent of the material that is wasted. |
| Device* | element | The Devices for which this DieLayout was made. |
| FileSpec* *(Modified in XJDF 2.1)* | element | Reference to an external URL that represents the die. |
| Media? | element | Media for which this DieLayout was intended. |
| RuleLength* | element | Elements describing the length of die rules. |
| Station* | element | Description of the stations in a DieLayout. |

### 6.29.1 Station
**Table 6.62: Station Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BinderySignatureIDs? | NMTOKENS | List of BinderySignature/@BinderySignatureID processed by this Station. |
| ShapeDefRef? | IDREF | Reference to a ShapeDef that defines the shape of the Station. |
| StationName | NMTOKEN | The name of the 1-up design in the DieLayout. |

## 6.30 DieLayoutProductionParams
Parameters for the die layout.
*Resource Properties: Input of Processes: DieLayoutProduction*

**Table 6.63: DieLayoutProductionParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Estimate? | boolean | Determines if the process SHALL run in estimate mode. |
| Position? | enumeration | The position of the DieLayout on the sheet. |
| ConvertingConfig+ | element | Describes a range of sheet sizes. |
| RepeatDesc+ | element | Step and repeat parameters for a set of ShapeDef. |

### 6.30.1 RepeatDesc
**Table 6.64: RepeatDesc Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| AllowedRotate? | enumeration | Allowed methods to rotate structural designs (None, Grain, MinorGrain, CrossGrain). |
| GutterX? | float | Gutter between columns. |
| GutterX2? | float | Secondary gutter between columns. |
| GutterY? | float | Gutter between rows. |
| GutterY2? | float | Secondary gutter between rows. |
| LayoutStyle? | NMTOKENS | The allowed styles for the layout (StraightNest, Reverse2ndRow, etc.). |
| OrderQuantity? | integer | The order quantity for the 1-up. |
| ShapeDefRef | IDREF | Reference to a ShapeDef describing the 1-up structural design. |
| UseBleed? | boolean | If true, the print bleed defined in the structural design SHALL be used. |

*(Figures 6-16 to 6-22 illustrate various LayoutStyle examples and Secondary Gutters.)*

## 6.31 DigitalPrintingParams
DigitalPrintingParams contains details of the DigitalPrinting process.
*Resource Properties: Intent Pairing: VariableIntent | Input of Processes: DigitalPrinting*

**Table 6.65: DigitalPrintingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Collate? | enumeration | Determines the sequencing of the printed sheets (None, Sheet). |
| ManualFeed? | boolean | Indicates whether the media will be fed manually. |
| PageDelivery? | enumeration | Indicates how pages SHALL be delivered (FanFold, SameOrderFaceUp, etc.). |
| SheetLay? | enumeration | Lay of input media. |
| Sides? | enumeration | Indicates whether the ByteMap SHALL be imaged on one or both sides (OneSidedBack, OneSidedFront, TwoSided). |

## 6.32 EmbossingParams
EmbossingParams contains attributes and elements used in executing the Embossing process.
*Resource Properties: Intent Pairing: EmbossingIntent | Input of Processes: Embossing*

**Table 6.66: EmbossingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ModuleID? | NMTOKEN | Identifier of the embossing module. |
| Emboss+ | element | One Emboss element is specified for each impression. |

### 6.32.1 Emboss
**Table 6.67: Emboss Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Direction | enumeration | The direction of the image. |
| EdgeAngle? | float | The angle of a beveled edge in degrees. |
| EdgeShape? | enumeration | The transition between the embossed surface and the surrounding media (Beveled, Rounded). |
| EmbossingType | enumeration | Specifies the type of embossing required. |
| Face? | enumeration | Position of the embossing on the product. |
| Height? | float | The height of the levels. |
| ImageSize? | XYPair | The size of the bounding box of one single image. |
| Position? | XYPair | Position of the lower left corner of the bounding box. |
| ToolRef? | IDREF | @ToolRef SHALL reference the Tool that is used. |
| IdentificationField? | element | If @EmbossingType="Braille", describes the content of the Braille element. |

## 6.33 EndSheetGluingParams
EndSheetGluingParams describes the attributes and elements used in executing the EndSheetGluing process.
*Resource Properties: Intent Pairing: BindingIntent | Input of Processes: EndSheetGluing*

**Table 6.68: EndSheetGluingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Glue | element | Description of the glue that is used to attach the end sheet to the cover. |

*Figure 6-23: Parameters and coordinate system used for EndSheetGluing.*

## 6.34 ExposedMedia
ExposedMedia represents processed Media such as film or plate.
*Resource Properties: Input of Processes: Bending, ConventionalPrinting, Varnishing | Output of Processes: Bending, ImageSetting*

**Table 6.69: ExposedMedia Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| MediaRef | IDREF | @MediaRef SHALL reference a Media. |
| PlateType? | enumeration | Specifies whether a plate is exposed or a dummy plate (Dummy, Exposed). |
| Polarity? | enumeration | @Polarity specifies the polarity of the image. |
| PunchType? | NMTOKEN | Name of the registration punch scheme. |
| Resolution? | XYPair | Resolution of the output. |
| IdentificationField* | element | IdentificationField associates bar codes or labels. |

## 6.35 FeedingParams
The parameters for any XJDF feeder processing Device.
*Resource Properties: Input of Processes: Feeding*

**Table 6.70: FeedingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CollatingItem* | element | Defines the collating sequence of the input Component(s). |
| Feeder* | element | Defines the specifics of an individual Feeder. |

### 6.35.1 CollatingItem
**Table 6.71: CollatingItem Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Amount? | integer | Determines how many consecutive items SHALL be consumed. |
| ComponentRef? | IDREF | References one of the input components. |
| Orientation? | enumeration | Named orientation of the CollatingItem. |
| Transformation? | matrix | Transformation of the CollatingItem. |
| TransformationContext? | enumeration | Specifies the object that SHALL be manipulated (CollateItem, Component, StackItem). |

### 6.35.2 Feeder
**Table 6.72: Feeder Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| AlternatePositions? | IntegerList | Positions of alternate feeders. |
| ComponentRef? | IDREF | References the Component that SHALL be loaded into this Feeder. |
| FeederSynchronization? | enumeration | Specifies the synchronization of multiple Feeder elements (Alternate, Backup, Chain, Primary). |
| FeederType? | NMTOKEN | Specifies the feeder type (AddOn, BookBlock, Folding, Gluing, Roll, Sheet, Signature). |
| Loading? | NMTOKEN | Specifies the feeder loading (Bundle, FanFold, Manual, Online, PrintRoll). |
| Opening? | enumeration | Specifies the opening of signatures (Back, Front, None, Sucker). |
| Position? | integer | @Position of feeder on a collecting and gathering chain. |
| FeederQualityParams? | element | Definition of the setup and policy for feeding quality. |

### 6.35.3 FeederQualityParams
**Table 6.73: FeederQualityParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BadFeedQuality? | enumeration | Defines the operation of the bad feed quality control. |
| BadFeeds? | integer | Number of consecutive bad feeds until the Device SHALL stop. |
| DoubleFeedQuality? | enumeration | Defines the operation of the double feed quality control. |
| DoubleFeeds? | integer | Number of consecutive double feeds until the Device SHALL stop. |
| IncorrectComponentQuality? | enumeration | Defines the operation of the incorrect components quality control. |
| IncorrectComponents? | integer | Number of consecutive incorrect components until the Device SHALL stop. |

## 6.36 FoldingParams
FoldingParams describes the folding parameters, including the sequence of folding steps.
*Resource Properties: Intent Pairing: FoldingIntent | Input of Processes: Folding*

**Table 6.74: FoldingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| FoldCatalog? | NMTOKEN | Describes the type of fold. |
| FoldingDetails? | NMTOKEN | @FoldingDetails is a system dependent descriptor of the folding. |
| SheetLay? | enumeration | Lay of input media. |
| FileSpec(CIP3)? | element | Reference to a CIP3 file that contains folding instructions. |
| Crease* | element | Defines one or more Crease lines. |
| Cut* | element | Cut elements describe an individual cut. |
| Fold* | element | Describes the folding operations in the sequence. |
| Perforate* | element | Defines one or more Perforate lines. |

## 6.37 FontPolicy
FontPolicy defines the policies that Devices SHALL follow when font errors occur while PDL files are being processed.
*Resource Properties: Input of Processes: Interpreting, Trapping*

**Table 6.75: FontPolicy Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| PreferredFont | NMTOKEN | The name of a font that SHALL be used as the default font for this job. |
| UseDefaultFont | boolean | If "true", the Device SHALL resort to a default font if a font cannot be found. |
| UseFontEmulation | boolean | If "true", the Device SHALL emulate a requested font if a font cannot be found. |

## 6.38 GluingParams
GluingParams define the parameters for applying a generic line of glue to a component.
*Resource Properties: Intent Pairing: BindingIntent | Input of Processes: Gluing*

**Table 6.76: GluingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| GluingProductionID? | NMTOKEN | Defines a gluing scheme for production. |
| Glue* | element | Definition of one or more Glue line applications. |

## 6.39 HeadBandApplicationParams
HeadBandApplicationParams specifies how to apply head bands in hardcover book production.
*Resource Properties: Input of Processes: HeadBandApplication*

**Table 6.77: HeadBandApplicationParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Length? | float | Length of the carrier material of the head band along the binding edge. |
| Width? | float | Width of the head bands and carrier. |
| Glue* | element | The carrier can be applied to the book block with glue. |

## 6.40 HoleMakingParams
HoleMakingParams specifies the shape and positions of holes in a Component.
*Resource Properties: Intent Pairing: HoleMakingIntent | Input of Processes: HoleMaking*

**Table 6.78: HoleMakingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| HolePattern+ | element | Description of individual or lines of HolePattern elements. |

## 6.41 ImageCompressionParams
ImageCompressionParams provides a set of controls that determines how images will be compressed.
*Resource Properties: Input of Processes: PDLCreation*

**Table 6.79: ImageCompressionParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ImageCompression+ | element | Specifies how images SHALL be compressed. |

## 6.42 ImageEnhancementParams
ImageEnhancementParams describes the controls for manipulating images.
*Resource Properties: Input of Processes: ImageEnhancement*

**Table 6.80: ImageEnhancementParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ImageEnhancementOp+ | element | Each ImageEnhancementOp describes an individual enhancement operation. |

### 6.42.1 ImageEnhancementOp
**Table 6.81: ImageEnhancementOp Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Operation | NMTOKEN | Individual enhancement operation name (BestGuess, Blurring, RedEyeRemoval, Sharpening). |
| OperationDetails? | string | Additional details of the @Operation. |
| SourceObjects? | enumerations | Identifies which class(es) of incoming graphical objects SHALL be operated on. |

## 6.43 ImageSetterParams
ImageSetterParams specifies the settings for an imagesetter.
*Resource Properties: Input of Processes: ImageSetting*

**Table 6.82: ImageSetterParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| AdvanceDistance? | float | Additional media advancement beyond the media dimensions. |
| BurnOutArea? | XYPair | Size of the burnout area. |
| CenterAcross? | enumeration | Specifies the axis around which a Device SHALL center an image. |
| CutMedia? | boolean | Indicates whether or not to cut the media (web-fed). |
| ManualFeed? | boolean | Indicates whether the media will be fed manually. |
| MirrorAround? | enumeration | Specifies the axis around which a Device SHALL mirror an image. |
| Polarity? | enumeration | Definition of the polarity of the image. |
| RollCut? | float | Length of media to be cut off of a roll, in points. |

## 6.44 Ink
Ink describes the ink, primer, toner or varnish that is applied to a substrate when printing or varnishing.
*Resource Properties: Intent Pairing: ColorIntent | Input of Processes: ConventionalPrinting, DigitalPrinting, Varnishing*

**Table 6.83: Ink Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| InkType? | NMTOKENS | @InkType SHALL list specific ink type and qualities. |
| SpecificYield? | float | Weight per area at total coverage in g/m2. |
| Certification* *(New in XJDF 2.1)* | element | Each Certification SHALL specify an ink certification level. |
| IdentificationField* | element | IdentificationField associates bar codes or labels. |

## 6.45 InkZoneCalculationParams
InkZoneCalculationParams specifies the parameters for the InkZoneCalculation process.
*Resource Properties: Input of Processes: InkZoneCalculation*

**Table 6.84: InkZoneCalculationParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| PrintableArea? | rectangle | Position and size of the printable area. |
| ZoneHeight? | float | The width of one zone in the feed direction. |
| Zones? | integer | The number of ink zones of the press. |
| ZonesY? | integer | Number of ink zones in feed direction of the press. |
| ZoneWidth? | float | The width of one zone. |
| Device? | element | Device provides a reference to the press. |

## 6.46 InkZoneProfile
InkZoneProfile specifies ink zone settings that are specific to the geometry of the printing Device being used.
*Resource Properties: Input of Processes: ConventionalPrinting | Output of Processes: InkZoneCalculation*

**Table 6.85: InkZoneProfile Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ZoneHeight? | float | The width of one zone in the feed direction. |
| ZoneSettingsX | FloatList | Each entry is the value of one ink zone in the X direction. |
| ZoneSettingsY? | FloatList | Each entry is the value of one ink zone in the Y direction. |
| ZoneWidth | float | The width of one zone. |

## 6.47 InsertingParams
InsertingParams specifies the parameters for the Inserting process.
*Resource Properties: Intent Pairing: AssemblingIntent, BindingIntent | Input of Processes: Inserting*

**Table 6.86: InsertingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| FinishedPage? | integer | Finished Page number of the mother Component on which the child Component SHALL be placed. |
| InsertLocation | enumeration | Where to place the “child” sheet (Back, Front, Overfold, FinishedPage). |
| Method? | enumeration | Inserting method (BindIn, BlowIn). |
| Glue* | element | Array of all Glue elements. |

*Table 6.87 & Figures: Location of Inserts diagrams showing placement on Front, Back, Overfold, and FinishedPage.*

## 6.48 InterpretingParams
InterpretingParams contains the parameters needed to interpret PDL pages.
*Resource Properties: Input of Processes: Interpreting*

**Table 6.88: InterpretingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Center? | boolean | Indicates whether or not the Finished Page image SHALL be centered. |
| FilmRef? | IDREF | Reference to film Media. |
| MirrorAround? | enumeration | Specifies the axis around which a RIP SHALL mirror an image. |
| PaperRef? | IDREF | Reference to final paper Media. |
| PlateRef? | IDREF | Reference to plate Media. |
| Polarity? | enumeration | The image SHALL be RIPed in the specified polarity. |
| PrintQuality? *(Deprecated in XJDF 2.1)* | enumeration | Generic switch for setting the quality.<br>Deprecation note: Use the generic input resource PrintCondition. |
| ProofPaperRef? | IDREF | Reference to paper Media used for proofing. |
| Scaling? | XYPair | A pair of positive real values that indicates the scaling factor. |
| ScalingOrigin? | XYPair | A pair of real values that identifies the point in the unscaled PDL page that remains at the same position after scaling. |
| FitPolicy? | element | Allows printing even if the size of the imagable area does not match the requirements. |
| InterpretingDetails? | element | Container for interpreter-specific details. |
| ObjectResolution* | element | Indicates the resolution at which the PDL contents will be interpreted in DPI. |
| PDFInterpretingParams? | element | Details of interpreting for PDF. |

### 6.48.1 InterpretingDetails
**Table 6.89: InterpretingDetails Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| MinLineWidth? | float | If present, specifies the minimum width in points for PDL line objects. |

### 6.48.2 PDFInterpretingParams
**Table 6.90: PDFInterpretingParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| EmitPDFBG? | boolean | Indicates whether BlackGeneration functions SHALL be emitted. |
| EmitPDFHalftones? | boolean | Indicates whether halftones SHALL be emitted. |
| EmitPDFTransfers? | boolean | Indicates whether transfer functions SHALL be emitted. |
| EmitPDFUCR? | boolean | Indicates whether under color removal functions SHALL be emitted. |
| HonorPDFOverprint? | boolean | Indicates whether or not overprint settings in the file SHALL be honored. |
| ICCColorAsDeviceColor? | boolean | Indicates whether colors specified by ICC color spaces SHALL be treated as Device colorants. |
| OCGIntent? | NMTOKEN | The value of @OCGIntent sets the intent for which OCGs SHALL be selected (Design, View). |
| OCGProcess? | NMTOKEN | Sets the purpose for which the Interpreting process is being performed (Export, Print, View). |
| OCGZoom? | float | Sets the magnification that SHALL be assumed in comparisons with the zoom dictionary. |
| PrintPDFAnnotations? | boolean | Indicates whether the contents of annotations on PDF pages SHALL be included. |
| PrintTrapAnnotations? | boolean | Indicates whether the contents of trap annotations on PDF pages SHALL be included. |
| TransparencyRenderingQuality? | float | Values are 0 to 1. A value of 0 represents the lowest allowable quality; 1 represents the highest. |
| OCGControl* | element | OCGControl provides a list of the OCGs (layers) that SHALL be explicitly included or excluded. |
| ReferenceXObjParams? | element | Describes how the interpreter should handle PDF Reference XObjects. |

### 6.48.3 ReferenceXObjParams
**Table 6.91: ReferenceXObjParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Mode | NMTOKEN | Specifies how to handle a Reference XObject's reference (Ignore, ResolveAlways, ResolveIfPDFX5). |
| FileSpec (SearchPath)* | element | An ordered list of FileSpec elements that specify search paths. |

## 6.49 JacketingParams
Description of the setup of the jacketing machinery.
*Resource Properties: Intent Pairing: BindingIntent | Input of Processes: Jacketing*

**Table 6.92: JacketingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| FoldingDistance? | float | Distance from the fold at @FoldingWidth to the other fold. |
| FoldingWidth | float | Definition of the dimension of the folding width of the front cover fold. |

*Figures 6-24 & 6-25: Setup of the jacketing machinery and coordinate system.*

## 6.50 LabelingParams
LabelingParams defines the details of the Labeling process.
*Resource Properties: Input of Processes: Labeling*

**Table 6.93: LabelingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Application? | NMTOKEN | Application method of the label (Glue, Loose, SelfAdhesive, Staple). |
| Face? | enumeration | Position of the label on the bundle. |
| Offset? | XYPair | Position of the lower left corner of the label. |
| FileSpec? | element | A FileSpec element pointing to an address list. |

## 6.51 LaminatingParams
LaminatingParams specifies the parameters needed for laminating.
*Resource Properties: Intent Pairing: LaminatingIntent | Input of Processes: Laminating*

**Table 6.94: LaminatingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| GapList? | FloatList | List of non-laminated gap positions in the X direction. |
| LaminatingBox? | rectangle | Area on the Component that SHALL be laminated. |
| LaminatingMethod? | enumeration | Laminating technology that SHALL be applied (CompoundFoil, DispersionGlue, Fusing). |
| ModuleID? | NMTOKEN | Identifier of the laminating module. |
| NipWidth? | float | Width of the nip in points. |
| Temperature? | float | Temperature that SHALL be used in the Laminating process. |

## 6.52 Layout
Layout is used both for fixed-layout and for automated printing.
*Resource Properties: Intent Pairing: LayoutIntent | Input of Processes: ConventionalPrinting, DigitalPrinting, Imposition, InkZoneCalculation, QualityControl, Stripping | Output of Processes: Stripping*

**Table 6.95: Layout Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Anchor? *(New in XJDF 2.1)* | enumeration | @Anchor specifies the anchor point of the grid of Position elements. |
| Automated? | boolean | If "true", the Imposition process is expected to perform automated imposition. |
| ExpansionBox? *(New in XJDF 2.1)* | rectangle | @ExpansionBox SHOULD specify the box in which Position elements SHALL be placed. |
| FilmRef? | IDREF | Reference to film Media. |
| InnermostShingling? | float | Relative creep compensation that SHALL be applied to the innermost part. |
| MaxCollect? | integer | Maximum number of sheets that SHALL be collected into a signature. |
| MinCollect? | integer | Minimum number of sheets that SHALL be collected into a signature. |
| OutermostShingling? | float | Relative creep compensation that SHALL be applied to the outermost part. |
| PaperRef? | IDREF | Reference to final paper Media. |
| PlateRef? | IDREF | Reference to plate Media. |
| ProofPaperRef? | IDREF | Reference to paper Media used for proofing. |
| SheetLay? *(New in XJDF 2.1)* | enumeration | Lay of the input media on the press. |
| SurfaceContentsBox? | rectangle | This box defines the area into which PlacedObject elements SHALL be positioned. |
| WorkStyle? | enumeration | The direction in which to turn the press sheet. |
| Device* | element | List of Device resources that the MIS expects to execute this Layout. |
| FileSpec (ExternalImpositionTemplate)? | element | Reference to an external imposition template. |
| FitPolicy? | element | Specifies automated fit policy for Position elements. |
| PlacedObject* | element | PlacedObject elements specify content or marks that SHALL be placed on the surface. |
| Position* | element | The Position elements specify how the BinderySignature SHALL be placed onto a sheet. |
| SheetActivation? | element | Specifies the conditions under which the optional sheet defined by this Layout SHALL be produced. |
| StripMark* | element | StripMark provides a description of production marks for Stripping. |

*Figures 6-26 to 6-28: Diagrams showing Use of ExpansionBox and Shingling for stripping.*

### 6.52.1 CIELABMeasuringField
*(Deprecated in XJDF 2.1)*

### 6.52.2 ContentObject
ContentObject elements identify containers for page content on a surface. ContentObject SHALL be an empty element.

### 6.52.3 DensityMeasuringField
*(Deprecated in XJDF 2.1)*

### 6.52.4 MarkObject
MarkObject elements identify containers for production marks on a surface.

**Table 6.96: MarkObject Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ContentRef? | IDREF | @ContentRef SHALL refer to the parent PlacedObject of the ContentObject. |
| ColorControlStrip* | element | ColorControlStrip describes a color control strip. |
| CutMark* | element | CutMark describes cut marks on a sheet. |
| RegisterMark* | element | RegisterMark describes a register mark. |
| ScavengerArea* | element | ScavengerArea describes a scavenger area. |

### 6.52.5 PageActivation
PageActivation SHALL define when content SHALL be conditionally placed in its parent PlacedObject.

**Table 6.97: PageActivation Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Condition+ | element | This PageActivation SHALL be applied when all Condition elements evaluate to true. |

### 6.52.6 PageCondition
The PageCondition element defines restrictions on when content SHALL NOT be placed in its parent PlacedObject.

**Table 6.98: PageCondition Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Condition+ | element | This PageCondition SHALL be applied when all Condition elements evaluate to true. |

### 6.52.7 PlacedObject
PlacedObject elements describe any kind of marks or content on a surface.

**Table 6.99: PlacedObject Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Anchor? | enumeration | Specifies the anchor point of the PlacedObject. |
| ClipBox? | rectangle | Clipping rectangle in terms of the coordinates of the @SurfaceContentsBox. |
| ClipPath? | PDFPath | Clip path for the PlacedObject. |
| CTM | matrix | The coordinate transformation matrix (CTM) of the object. |
| HalfTonePhaseOrigin? | XYPair | Location of the origin for screening of this PlacedObject. |
| ID? | ID | Identifier for referencing this PlacedObject. |
| Ord? | integer | @Ord SHALL specify a zero-based reference to an index in the RunList. |
| PositionRef? | IDREF | Reference to the Position that defines where this PlacedObject SHALL be located. |
| SourceClipPath? | PDFPath | Clip path for the PlacedObject in the source coordinate system. |
| TrimCTM? | matrix | @TrimCTM SHALL specify the transformation matrix of the trim box. |
| TrimSize? | XYPair | @TrimSize SHALL specify the size of the object's trim box. |
| ContentObject? | element | If present, this PlacedObject shall be filled from RunList(Document). |
| MarkObject? | element | Details of the production mark. |
| PageActivation? | element | PageActivation defines conditions on which page content SHALL be placed. |
| PageCondition? | element | PageCondition defines conditions on which page content SHALL NOT be placed. |

### 6.52.8 Position
The Position element allows the aligned placement of a BinderySignature onto a Layout.

**Table 6.100: Position Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| AbsoluteBox? | rectangle | Absolute position, in points, of the display area of this BinderySignature. |
| Anchor? *(New in XJDF 2.1)* | enumeration | Specifies the anchor point of the BinderySignature. |
| BinderySignatureID? | NMTOKEN | If present, @BinderySignatureID SHALL reference a BinderySignature. |
| BlockName? | NMTOKEN | Identifies a CutBlock resulting from a Cutting process. |
| GangElementID? | NMTOKEN | If present, @GangElementID SHALL reference a GangElement element. |
| ID? | ID | Identifier of this Position. |
| MarginBottom? | float | Bottom margin, in points. |
| MarginLeft? | float | Left margin, in points. |
| MarginRight? | float | Right margin, in points. |
| MarginTop? | float | Top margin, in points. |
| Orientation? | enumeration | Named orientation describing the transformation of the orientation. |
| PositionOrd? *(New in XJDF 2.1)* | integer | @PositionOrd contains the index of the Position element on the Layout. |
| RelativeBox? | rectangle | @RelativeBox is a rough definition of the general position of the display area. |
| StackDepth? | integer | Maximum number of sheets on a stack for cut and stack imposition. |
| StackOrd? | integer | Index of the stack. |

*Figure 6-29: Diagram showing RelativeBox including margins.*

### 6.52.9 SheetActivation
SheetActivation specifies the conditions under which an optional sheet SHALL be produced.

**Table 6.101: SheetActivation Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Condition+ | element | This SheetActivation shall be applied when all Condition elements evaluate to true. |

### 6.52.10 More about Layout
Includes definitions for Partition Key Restrictions, CTM Definitions, Finding the Trim Box of an Object, Using Ord to Reference Elements in RunList Resources, and Calculating Ord values in automated imposition.

## 6.53 LayoutElementProductionParams
LayoutElementProductionParams is needed for LayoutElementProduction.
*Resource Properties: Intent Pairing: VariableIntent | Input of Processes: LayoutElementProduction*

**Table 6.104: LayoutElementProductionParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ContentRefs? | IDREFS | @ContentRefs SHALL reference Content that provide metadata related to the output RunList. |
| ShapeDefRef? | IDREF | @ShapeDefRef SHALL reference a ShapeDef that represents the shape of the RunList to be produced. |
| FileSpec(DataList)? | element | References a data list containing record information for variable data production. |

## 6.54 LayoutShift
LayoutShift defines the parameters for separation dependent paper stretch compensation.
*Resource Properties: Input of Processes: LayoutShifting*

**Table 6.105: LayoutShift Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ShiftPoint+ | element | Description of separation dependent transformations for a given point on the Layout. |

### 6.54.1 ShiftPoint
**Table 6.106: ShiftPoint Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CTM | matrix | @CTM that SHALL be applied to the separation after all other transformations. |
| Position | XYPair | Point that this ShiftPoint applies to. |

## 6.55 LooseBindingParams
LooseBindingParams describes the details of the LooseBinding process.
*Resource Properties: Intent Pairing: BindingIntent | Input of Processes: LooseBinding*

**Table 6.107: LooseBindingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BindingType | enumeration | Type of binding that is performed (ChannelBinding, CoilBinding, CombBinding, RingBinding, StripBinding). |
| CoverMaterial? | NMTOKEN | @CoverMaterial describes the binder materials used. |
| ChannelBindingDetails? | element | Specifies additional details for channel binding. |
| CoilBindingDetails? | element | Specifies additional details for coil binding. |
| CombBindingDetails? | element | Specifies additional details for comb binding. |
| HolePattern* | element | Details of the holes for binding. |
| RingBindingDetails? | element | Specifies additional details for ring binding. |
| StripBindingDetails? | element | Specifies additional details for strip binding. |

### 6.55.1 ChannelBindingDetails
**Table 6.108: ChannelBindingDetails Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ClampD? | float | The distance of the clamp that was “pressed away”. |
| ClampSize? | shape | The shape size of the clamp. |
| Cover? | boolean | If "true" the clamp is inside of a preassembled cover. |

### 6.55.2 CoilBindingDetails
**Table 6.109: CoilBindingDetails Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CoilShape? | NMTOKEN | The shape of the wire coil binding. |
| Diameter? | float | Specifies the diameter of the comb to be produced. |

### 6.55.3 CombBindingDetails
**Table 6.110: CombBindingDetails Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CombShape? | NMTOKEN | The shape of the wire comb binding. |
| Diameter? | float | Specifies the diameter of the comb to be produced. |
| FlipBackCover? | boolean | Specifies that the cover SHALL be flipped after the wire was “closed”. |

### 6.55.4 RingBindingDetails
**Table 6.111: RingBindingDetails Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Diameter? | float | Specifies the diameter of the rings, in points. |
| RingMechanic? | boolean | If "true", a hand lever is available for opening. |
| RingShape? | NMTOKEN | @RingShape specifies the shape of the ring binder rings (D-shape, Oval, Round, SlantD). |
| RivetsExposed? | boolean | @RivetsExposed describes the mounting of the ring mechanism in the binder case. |
| SpineWidth? | float | The spine width is determined by the final height of the block of sheets. |
| ViewBinder? | NMTOKEN | @ViewBinder specifies the details of the clear vinyl outer-wrap types (Embedded, Pocket). |

### 6.55.5 StripBindingDetails
**Table 6.112: StripBindingDetails Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Length? | float | The length of the pin is determined by the height of the pile of sheets to be bound. |

## 6.56 ManualLaborParams
ManualLaborParams describes the parameters to qualify generic manual work within graphic arts production.
*Resource Properties: Input of Processes: ManualLabor*

**Table 6.113: ManualLaborParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| LaborType | NMTOKEN | Type of manual labor that is performed (CarvePotato, CreateCoatingForm, EditArt, EditMarks, EditTraps, ManageJob, PhoneCallToCustomer, SeparateBlanks). |

## 6.57 Media
Media represents the properties of a raw, unexposed printable surface such as a paper sheet, film or plate.
*Resource Properties: Intent Pairing: MediaIntent, HoleMakingIntent | Input of Processes: BoxPacking, Bundling, Embossing, ImageSetting, Laminating, Winding*

**Table 6.114: Media Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BackBrightness? | float | Equivalent to @Brightness, but applied to the back surface. |
| BackCIETint? *(New in XJDF 2.1)* | float | Average CIE tint value applied to the back surface. |
| BackCIEWhiteness? *(New in XJDF 2.1)* | float | Average CIE whiteness value applied to the back surface. |
| BackCoating? | enumeration | Identical to @Coating, but applied to the back surface. |
| BackCoatingDetail? | NMTOKEN | Identical to @CoatingDetail, but applied to the back surface. |
| BackGlossValue? | float | Identical to @GlossValue, but applied to the back surface. |
| BackISOPaperSubstrate? | enumeration | @BackISOPaperSubstrate SHALL be used to classify the back surface of the paper. |
| BackLabColorValue? *(New in XJDF 2.1)* | LabColor | @BackLabColorValue is the CIELAB color value applied to the back surface. |
| BackSpectrum? *(New in XJDF 2.1)* | Transfer-Function | @BackSpectrum is the spectrum of the Media applied to the back surface. |
| Brightness? | float | Reflectance percentage of diffuse blue reflectance. |
| CIETint? | float | Average CIE tint value. |
| CIEWhiteness? | float | Average CIE whiteness value. |
| Coating? | enumeration | The pre-process coating that has been applied to the media. |
| CoatingDetail? | NMTOKEN | Describes additional details of the coating. |
| CoreWeight? | float | Weight of the core of a roll, in grams [g]. |
| Dimension? | XYPair | The X and Y dimensions of the Media, measured in points. |
| FluteDirection? | enumeration | Direction of the flute of corrugated media. |
| Flute? | NMTOKEN | Single, capital letter that specifies the flute type of corrugated media. |
| GlossValue? | float | Gloss of the media in gloss units. |
| GrainDirection? | enumeration | Direction of the grain in the coordinate system of the Media. |
| ImagableSide? | enumeration | Side of the chosen medium that can be marked (Front, Back, Both, Neither). |
| InnerCoreDiameter? | float | Specifies the inner diameter of the core of a roll, in points. |
| InsideLoss? | float | The inside loss of corrugated board material in microns [µm]. |
| ISOPaperSubstrate? | enumeration | @ISOPaperSubstrate SHALL be used to classify the surface of the paper. |
| LabColorValue? | LabColor | @LabColorValue is the CIELAB color value of the media. |
| MediaColorName? | enumeration | A name for the color. |
| MediaColorNameDetails? | string | A more specific, specialized or site-defined name for the media color. |
| MediaQuality? | string | Named quality description of the media. |
| MediaSetCount? | integer | When the input media is grouped in sets, identifies the number of pieces of media in each set. |
| MediaType | enumeration | Describes the general type of the Media. |
| MediaTypeDetails? | NMTOKEN | Additional details of the chosen medium. |
| MediaUnit? | enumeration | Describes the format of the media as it is delivered (Continuous, Roll, Sheet). |
| Opacity? | enumeration | The opacity of the media. |
| OpacityLevel? | float | Normalized TAPPI opacity (Cn). |
| OuterCoreDiameter? | float | Specifies the outer diameter of the core of a roll, in points. |
| OutsideGain? | float | The outside gain of corrugated board material in microns [µm]. |
| PlateTechnology? | enumeration | Exposure technology of the plates. |
| Polarity? | enumeration | Polarity of the chosen medium. |
| PrintingTechnology? | NMTOKEN | Describes the printing technology that the media or coatings are intended for. |
| RecycledPercentage? | float | The percentage, between 0 and 100, of recycled material. |
| ReliefThickness? | float | The thickness of the relief, measured in microns [µm]. |
| RollDiameter? | float | Specifies the diameter of a roll, in points. |
| ShrinkIndex? | XYPair | Specifies the ratio of the media linear dimension after shrinking. |
| SleeveInterlock? | NMTOKEN | The type of interlock (or notch) to use for a flexo sleeve. |
| Spectrum? *(New in XJDF 2.1)* | Transfer-Function | Spectrum of the Media. |
| StockType? | NMTOKEN | @StockType defines the base size when calculating paper weights. |
| Texture? | NMTOKEN | The texture of paper media. |
| Thickness? | float | The thickness of the chosen medium, measured in microns [µm]. |
| Weight? | float | Weight of the chosen medium, measured in grams per square meter [g/m2]. |
| Certification* | element | Each Certification SHALL specify a paper certification level. |
| ColorMeasurementConditions? *(New in XJDF 2.1)* | element | Detailed description of the measurement conditions for color measurements. |
| HolePattern* | element | List of holes in the Media. |
| IdentificationField* | element | IdentificationField associates bar codes or labels with this Media. |
| MediaLayers? | element | MediaLayers describes the layer structure of media. |
| TabDimensions? | element | Specifies the dimensions of the tabs. |

### 6.57.1 TabDimensions
**Table 6.115: TabDimensions Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| TabEdge? | enumeration | Indicates which edge of the media has tabs. |
| TabExtensionDistance? | float | The positive distance in points that the tab extends beyond the body of the other media. |
| TabOffset? | float | Specifies the magnitude of the distance in points from the two corners to the edge of the first “tab pitch” point. |
| TabSetCollationOrder? | NMTOKEN | Collation order of media provided in sets (Forward, Reverse). |
| TabsPerBank? | integer | Specifies the number of equal-sized tabs in a single bank if all positions were filled. |
| TabWidth? | float | The width along the @TabEdge of each tab. |

### 6.57.2 More about Media
Includes definitions for Inside Loss and Outside Gain, Corrugated Media, Self adhesive Media, Flexo Plate Media, and Flexo Sleeve Media.
*Figures 6-31 to 6-34: Diagrams illustrating Paper roll information, Relief and floor thickness, Tab dimensions, and Inside Loss/Outside Gain.*

## 6.58 MiscConsumable
The MiscConsumable resource is intended for cost accounting, inventory control and availability scheduling of supplies used in the production workflow.
*Resource Properties: Input of Processes: Any Process*

**Table 6.117: MiscConsumable Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Color? | enumeration | @Color specifies the Machine readable color of the consumable. |
| ColorDetails? | string | @ColorDetails specifies additional details of the color. |
| Type | NMTOKEN | Identifies the type of MiscConsumable (Machine-readable). |
| TypeDetails? | NMTOKEN | Additional details of the consumable such as material. |
| Certification* *(New in XJDF 2.1)* | element | Each Certification SHALL specify a certification level. |
| IdentificationField* | element | IdentificationField associates bar codes or labels. |

### 6.58.1 MiscConsumableType
**Table 6.118: MiscConsumableType Attribute Values**
*(Contains a comprehensive list of values such as BackReinforcement, BlisterPack, ChannelBinder, Coil, Comb, Cover, Developer, DigitalMedia, Electricity, Foil, FuserOil, Gas, Glue, Grommet, Hardener, Headband, Laminate, MountingTape, Paper, PaperBand, PaperWrap, PlasticBand, RegisterRibbon, RingBinder, RubberBand, ShrinkWrap, Staples, Strap, StripBinder, Styrofoam, Tape, Thread, WasteContainer, Wire.)*

## 6.59 NodeInfo
NodeInfo contains information about planned scheduling and the status of individual Worksteps.
*Resource Properties: Input of Processes: Any Process*

**Table 6.119: NodeInfo Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CleanupDuration? | duration | Estimated duration of the clean-up phase of the process. |
| DueLevel? | enumeration | Description of the severity of a missed deadline (JobCancelled, Penalty, Trivial). |
| End? | dateTime | Date and time at which the process is scheduled to end. |
| FirstEnd? | dateTime | Earliest date and time at which the process SHALL end. |
| FirstStart? | dateTime | Earliest date and time at which the process SHALL begin. |
| JobPriority? | integer | The scheduling priority for the node where 100 is the highest and 0 is the lowest. |
| LastEnd? | dateTime | Latest date and time at which the process SHALL end. |
| LastStart? | dateTime | Latest date and time at which the process SHALL begin. |
| NaturalLang? | language | Language selected for human readable communication. |
| PersonalID? | NMTOKEN | Resource/@ExternalID of the Contact that represents the employee responsible. |
| SetupDuration? | duration | Estimated duration of the setup phase of the process. |
| Start? | dateTime | Date and time of the planned process start. |
| Status? | enumeration | Identifies the status of an individual part of the XJDF. |
| StatusDetails? | NMTOKEN | Machine readable description of the status. |
| TotalDuration? | duration | Estimated total duration of the process, including setup and cleanup. |
| GangSource* | element | If present, each GangSource SHALL represent the source jobs that are being processed as a Gang job. |
| MISDetails? | element | Definition how the costs for the execution of this node SHALL be charged. |

## 6.60 Pallet
A Pallet represents the pallet used in packing goods.
*Resource Properties: Input of Processes: Palletizing*

**Table 6.120: Pallet Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| PalletType | NMTOKEN | Type of pallet used. |
| Size? | XYPair | Describes the length and width of the pallet, in points. |
| IdentificationField* | element | IdentificationField associates bar codes or labels. |

## 6.61 PalletizingParams
PalletizingParams defines the details of Palletizing.
*Resource Properties: Input of Processes: Palletizing*

**Table 6.121: PalletizingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| LayerAmount? | IntegerList | Ordered number of input components in a layer. |
| MaxHeight? | float | Maximum height of a loaded pallet in points. |
| MaxWeight? | float | Maximum weight of a loaded pallet in grams. |
| Overhang? | XYPair | Overhang in x and y direction on each side. |
| OverhangOffset? | XYPair | Overhang offset if overhang is not centered. |
| Pattern? | NMTOKEN | Name of the palletizing pattern. |

## 6.62 PDLCreationParams
PDLCreationParams describes the details of generating the supported output PDL types.
*Resource Properties: Input of Processes: PDLCreation*

**Table 6.122: PDLCreationParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| MimeType | string | This resource identifies the MIME type associated with this output file format. |
| FontParams? | element | FontParams describes how fonts SHALL be handled when creating PDL. |
| PDFCreationDetails? | element | PDF specific element for the output. |
| PSCreationDetails? | element | Postscript specific element for the output. |

### 6.62.1 AdvancedParams
**Table 6.123: AdvancedParams Element**
*(Contains boolean flags for PDF generation such as AllowPSXObjects, AllowTransparency, AutoPositionEPSInfo, EmbedJobOptions, EmitDSCWarnings, LockDistillerParams, ParseDSCCommentForDocInfo, ParseDSCComments, PassThroughJPEGImages, PreserveCopyPage, PreserveEPSInfo, PreserveHalftoneInfo, PreserveOPIComments, PreserveOverprintSettings, TransferFunctionInfo, UCRandBGInfo, UsePrologue.)*

### 6.62.2 FontParams
**Table 6.124: FontParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| AlwaysEmbed? | NMTOKENS | @AlwaysEmbed specifies a list of one or more names of fonts that SHALL be embedded. |
| EmbedAllFonts? | boolean | If "true", specifies that all fonts, except those in the @NeverEmbed list, SHALL be embedded. |
| MaxSubsetPct? | integer | If the percentage of glyphs used from a font is below the value, a subset SHALL be embedded. |
| NeverEmbed? | NMTOKENS | @NeverEmbed specifies a list of fonts that SHALL NOT be embedded. |

### 6.62.3 PDFCreationDetails
**Table 6.125: PDFCreationDetails Element**
*(Contains parameters for PDF generation like AllowJBIG2Globals, ASCII85EncodePages, AutoRotatePages, Binding, CompressPages, DefaultRenderingIntent, DetectBlend, DoThumbnails, InitialPageSize, InitialResolution, Optimize, OverPrintMode, PDFVersion, AdvancedParams, PDFXParams, ThinPDFParams.)*

### 6.62.4 PDFXParams
**Table 6.126: PDFXParams Element**
*(Contains parameters for generating PDF/X files such as PDFXBleedBoxtoTrimBoxOffset, PDFXCheck, PDFXCompliantPDFOnly, PDFXNoTrimBoxError, PDFXSetBleedBoxToMediaBox, PDFXTrapped, PDFXTrimBoxToMediaBoxOffset, FileSpec (ReferenceOutputProfile).)*

### 6.62.5 PSCreationDetails
**Table 6.127: PSCreationDetails Element**
*(Contains configurable options for generating PostScript files like BinaryOK, BoundingBox, CenterCropBox, GeneratePageStreams, IgnoreAnnotForms, IgnoreBG, IgnoreColorSeps, IgnoreDSC, IgnoreExternStreamRef, IgnoreHalftones, IgnoreOverprint, IgnorePageRotation, IgnoreRawData, IgnoreSeparableImagesOnly, IgnoreShowPage, IgnoreTransfers, IgnoreTTFontsFirst, IgnoreUCR, IncludeBaseFonts, IncludeCIDFonts, IncludeEmbeddedFonts, IncludeOtherResources, IncludeProcSets, IncludeTrueTypeFonts, IncludeType1Fonts, IncludeType3Fonts, OutputType, PSLevel, Scale, SetPageSize, SetupProcsets, ShrinkToFit, SuppressCenter, SuppressRotate, TTasT42, UseFontAliasNames.)*

### 6.62.6 ThinPDFParams
**Table 6.128: ThinPDFParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| FilePerPage? | boolean | If "true", the process SHALL generate 1 PDF file per page. |
| SidelineEPS? | boolean | If "true", embedded EPS files in PostScript source documents SHALL not be converted. |
| SidelineFonts? | boolean | If "true", font data MAY be stored in external files. |
| SidelineImages? | boolean | If "true", image data MAY be stored in an external stream. |

## 6.63 PerforatingParams
PerforatingParams define the parameters for perforating a sheet.
*Resource Properties: Intent Pairing: FoldingIntent | Input of Processes: Perforating*

**Table 6.129: PerforatingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Perforate+ | element | Defines one or more Perforate lines. |

## 6.64 PreflightParams
The PreflightParams resource specifies the tests for the Preflight process to run.
*Resource Properties: Intent Pairing: ContentCheckIntent | Input of Processes: LayoutElementProduction, Preflight*

**Table 6.130: PreflightParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| FileSpec? | element | File that describes the preflight actions in a computer-readable, non XJDF format. |
| PreflightTest* | element | Descriptions of individual tests. |

### 6.64.1 PreflightTest
**Table 6.131: PreflightTest Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Action? | enumeration | Action that SHOULD be taken whenever this test fails. |
| DescriptiveName? | string | Human readable description of this preflight test. |
| Severity? | enumeration | Severity of a failure of the test. |
| TestClass? | NMTOKEN | General area of the preflight test (Colorspace, FileFormat, Font, PageFormat, Resolution). |
| TestID? | NMTOKEN | System dependent preflight test identifier. |
| GeneralID* | element | Detailed individual parameters of the PreflightTest. |

## 6.65 PreflightReport
The PreflightReport resource describes the results of the preflight tests.
*Resource Properties: Intent Pairing: ContentCheckIntent | Output of Processes: Preflight*

**Table 6.132: PreflightReport Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ErrorCount? | integer | The count of errors that were encountered while preflighting. |
| WarningCount? | integer | The count of warnings that were encountered while preflighting. |
| FileSpec? | element | References a human readable preflight report. |
| PreflightCheck* | element | List of individual preflight results. |

### 6.65.1 PreflightCheck
**Table 6.133: PreflightCheck Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Action? | enumeration | Action that has been taken. |
| Count? | integer | The total number of occurrences of this PreflightCheck. |
| Pages? | IntegerList | A 0-based index of pages where errors occurred. |
| Severity? | enumeration | Severity of the PreflightCheck. |
| TestClass? | NMTOKEN | General area of the preflight check. |
| TestID? | NMTOKEN | System dependent error identifier. |
| Comment? | element | Human readable description of this preflight check. |
| GeneralID* | element | Detailed individual parameters. |

## 6.66 Preview
The preview of the content of a surface.
*Resource Properties: Input of Processes: Any Process, InkZoneCalculation | Output of Processes: PreviewGeneration*

**Table 6.134: Preview Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Compensation? | enumeration | Compensation of the image to reflect the application of transfer curves. |
| CTM? *(Deprecated in XJDF 2.1)* | matrix | Orientation of the Preview with respect to the Layout coordinate system.<br>Deprecation note: Use either Resource/@Transformation or Resource/@Orientation. |
| PreviewFileType? | enumeration | The file type of the preview (CIP3Multiple, CIP3Single, PNG). |
| FileSpec? | element | FileSpec SHALL identify the preview. |

## 6.67 PreviewGenerationParams
Parameters specifying the size and the type of the preview.
*Resource Properties: Input of Processes: PreviewGeneration*

**Table 6.135: PreviewGenerationParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| AspectRatio? | enumeration | Policy that defines how to define the preview size (CenterMax, CenterMin, Crop, Expand, Ignore). |
| Resolution? | XYPair | Resolution of the preview, in dpi. |
| Size? | XYPair | Size of the preview, in pixels. |

## 6.68 PrintCondition
*(New in XJDF 2.1)* PrintCondition is used to describe the target print condition for a given printing process.
*Resource Properties: Input of Processes: Any Process*

**Table 6.136: PrintCondition Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ColorantOrder? | NMTOKENS | @ColorantOrder SHALL specify the number and order of colorants. |
| Name | NMTOKEN | @Name SHALL identify the print condition. |
| PrintQuality? | enumeration | Generic switch for setting the quality of an otherwise inaccessible Device (High, Normal, Draft). |

## 6.69 QualityControlParams
QualityControlParams defines the set of parameters for the quality control process.
*Resource Properties: Input of Processes: QualityControl*

**Table 6.137: QualityControlParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Box? *(New in XJDF 2.1)* | rectangle | Position and size of the requested measurement area. |
| Position? *(New in XJDF 2.1)* | enumeration | Position of the requested measurement on the physical object. |
| QualityBase? *(New in XJDF 2.1)* | enumeration | @QualityBase SHALL specify the basis of the target master measurement (Absolute, Master). |
| QualityControlMethods? *(New in XJDF 2.1)* | NMTOKENS | @QualityControlMethods SHOULD be provided and SHALL specify the types of quality control method. |
| SampleInterval? | integer | Interval in number of samples between tests. |
| Severity? *(New in XJDF 2.1)* | integer | @Severity SHALL define the maximum allowed overall severity of all defects. |
| SourceDeviceID? *(New in XJDF 2.1)* | NMTOKEN | Device/@DeviceID of the Device that is producing the ResourceSet. |
| TimeInterval? | duration | Time interval between individual tests. |
| BindingQuality? *(New in XJDF 2.1)* | element | Specification of the binding quality measurements. |
| BindingQualityParams? *(Deprecated in XJDF 2.1)* | element | Deprecation note: Use BindingQuality. |
| ColorMeasurement? *(New in XJDF 2.1)* | element | ColorMeasurement SHALL specify a color quality measurement setup. |
| FileSpec(Image)? *(New in XJDF 2.1)* | element | FileSpec(Image) SHALL reference a master image. |
| FileSpec(Setup)? *(Modified in XJDF 2.1)* | element | FileSpec(Setup) SHALL reference the location of an external file containing setup details. |
| RegistrationQuality? *(New in XJDF 2.1)* | element | RegistrationQuality SHALL specify the setup of the color registration quality measurements. |

## 6.70 QualityControlResult
QualityControlResult defines the set of results from a QualityControl process.
*Resource Properties: Output of Processes: QualityControl*

**Table 6.138: QualityControlResult Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Box? *(New in XJDF 2.1)* | rectangle | Defines the position and size of the measurement area. |
| End? | dateTime | Date and time of the end of the measurement. |
| Failed? | integer | Total number of failed measurements. |
| Measurements? *(New in XJDF 2.1)* | integer | @Measurements SHALL specify the total number of measurements. |
| MeasurementUsage? *(New in XJDF 2.1)* | enumerations | @MeasurementUsage SHALL specify the usages of this QualityControlResult (Master, Standard). |
| Passed? | integer | Total number of passed measurements. |
| Position? *(New in XJDF 2.1)* | enumeration | Position of the requested measurement on the physical object. |
| QualityControlMethods? *(New in XJDF 2.1)* | NMTOKENS | @QualityControlMethods SHOULD be provided. |
| Sample? *(New in XJDF 2.1)* | IntegerRange | The value of @Sample SHALL be the index of the first and last measurement. |
| Severity? *(New in XJDF 2.1)* | integer | @Severity SHALL define the maximum allowed overall severity. |
| SourceDeviceID? *(New in XJDF 2.1)* | NMTOKEN | Device/@DeviceID of the Device that has produced the ResourceSet. |
| Start? | dateTime | Date and time of the start of the measurement. |
| BindingQuality? *(New in XJDF 2.1)* | element | BindingQuality SHALL define the details of an individual or average binding quality measurement. |
| BindingQualityMeasurement* *(Deprecated in XJDF 2.1)* | element | Deprecation note: Use BindingQuality. |
| ColorMeasurement? *(New in XJDF 2.1)* | element | ColorMeasurement SHALL specify a color quality measurement. |
| FileSpec? | element | Location of an external file that contains details of the measurement. |
| Inspection? *(New in XJDF 2.1)* | element | Inspection SHALL describe a set of measurements on an individual sheet or component. |
| RegistrationQuality? *(New in XJDF 2.1)* | element | RegistrationQuality SHALL define the details of an individual or average color registration measurement. |

### 6.70.1 Defect
*(New in XJDF 2.1)* Each Defect SHALL describe an individual defect or problem.

**Table 6.139: Defect Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Box? | rectangle | Position of the defect in the coordinate system. |
| DefectReason? | NMTOKEN | Cause of the defect (ElectroStaticCharge, Humidity, Temperature). |
| DefectType | enumeration | Machine readable type of the defect (FinishingDefect, ImageDefect, ImageFinishingDefect, Other, SheetDefect, SubstrateDefect). |
| DefectTypeDetails? | NMTOKEN | Machine readable details of the type of the defect (Abrasion, Arching, BarcodeDefect, Blocking, BoardSplitting, Cockling, ColorMismatch, CuttingDefect, Delamination, Dusting, Fanout, FiberLifting, FinishingDeregistration, FoldCrack, FrontBackDeregistration, Ghosting, GlueBindingDefect, Graininess, ImageDoubling, ImageMismatch, InkBlistering, InkSetoff, InkSplash, InsertingDefect, Moire, Mottling, Picking, Scumming, SeparationDeregistration, ShineThrough, StitchingDefect, StrikeThrough, SubstrateMottling, Wrinkling, Hole). |
| Face? | enumeration | @Face SHALL specify the side of a three dimensional physical object where the defect is located. |
| Severity? | integer | @Severity SHALL define the severity of the defect. |
| Size? | float | The area of the defect in square points. |
| Comment? | element | Human readable description of the defect. |
| FileSpec? | element | FileSpec SHALL reference an image of the Defect. |

### 6.70.2 Inspection
*(New in XJDF 2.1)* An Inspection SHALL describe the inspection of one or more Components.

**Table 6.142: Inspection Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Defect* | element | Each Defect SHALL describe an individual defect or problem. |
| FileSpec? | element | FileSpec SHALL reference an image of the inspected Component. |

### 6.70.3 BindingQualityMeasurement
*(Deprecated in XJDF 2.1)*

## 6.71 RasterReadingParams
This set of parameters specifies the details for RasterReading.
*Resource Properties: Input of Processes: RasterReading*

**Table 6.143: RasterReadingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Center? | boolean | Indicates whether or not the Finished Page image SHALL be centered. |
| FilmRef? | IDREF | Reference to film Media. |
| MirrorAround? | enumeration | Specifies the axis around which a raster reader SHALL mirror an image. |
| PaperRef? | IDREF | Reference to final paper Media. |
| PlateRef? | IDREF | Reference to plate Media. |
| Polarity? | enumeration | The image SHALL be RIPed in the polarity specified. |
| ProofPaperRef? | IDREF | Reference to paper Media used for proofing. |
| Scaling? | XYPair | A pair of positive real values that indicates the scaling factor. |
| ScalingOrigin? | XYPair | A pair of real values that identify the point in the unscaled page that SHALL become the origin. |
| FitPolicy? | element | Allows printing even if the size of the imagable area does not match the requirements. |

## 6.72 RenderingParams
This set of parameters identifies how the Rendering process SHALL operate.
*Resource Properties: Input of Processes: Rendering*

**Table 6.144: RenderingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BandHeight? | integer | Height of output bands expressed in lines. |
| BandOrdering? | enumeration | Indicates whether output buffers are generated in "BandMajor" or "ColorMajor" order. |
| BandWidth? | integer | Width of output bands, in pixels. |
| ColorantDepth? | integer | Number of bits per colorant. |
| Interleaved? | boolean | If "true", the resulting colorant values SHALL be interleaved. |
| MimeType? | string | @MimeType identifies the MIME type associated with this output file format. |
| AutomatedOverPrintParams? | element | Controls for overprint substitutions. |
| ObjectResolution* | element | Elements that define the resolutions at which to render the contents. |
| TIFFFormatParams? | element | Parameters specific for creating TIFF files. |

### 6.72.1 TIFFEmbeddedFile
**Table 6.145: TIFFEmbeddedFile Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| TagNumber | integer | Tag number of the specified tag. |
| TagType | integer | The type of the tag as defined in [TIFF6]. |
| FileSpec | element | Reference to the file that SHALL be embedded. |

### 6.72.2 TIFFFormatParams
**Table 6.146: TIFFFormatParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ByteOrder? | enumeration | Byte order of the TIFF file (II, MM). |
| Interleaving? | integer | How the components of each pixel are stored (1: Chunky, 2: Planar). |
| RowsPerStrip? | integer | The number of image scan lines per strip. |
| Segmentation? | enumeration | How the image data are segmented (SingleStrip, Stripped, Tiled). |
| SeparationNameTag? | integer | When color separations are stored in individual TIFF files, marks each with the name of the colorant. |
| TileSize? | XYPair | Two integers providing width and height of tiles. |
| WhiteIsZero? | boolean | Indicates whether the data SHALL be written with either white values encoded as zero or black values encoded as zero. |
| TIFFEmbeddedFile* | element | Files to be embedded within the created TIFF file. |
| TIFFtag* | element | Specific tag values for inclusion in the TIFF file. |

### 6.72.3 TIFFtag
**Table 6.147: TIFFtag Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BinaryValue? | hexBinary | If the type of the tag is UNDEFINED, @BinaryValue is used. |
| IntegerValue? | IntegerList | If the type is BYTE, SHORT, LONG, SBYTE, SSHORT or SLONG, @IntegerValue is used. |
| NumberValue? | FloatList | If the type is RATIONAL, SRATIONAL or FLOAT, @NumberValue is used. |
| StringValue? | string | If the type is ASCII, @StringValue is used. |
| TagNumber | integer | Tag number of the specified tag. |
| TagType | integer | The type of the tag as defined in [TIFF6]. |

## 6.73 RunList
A RunList defines one or more printable logical documents or Document Sets.
*Resource Properties: Input/Output of many Prepress and Press Processes.*

**Table 6.148: RunList Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Automation? | enumeration | Identifies dynamic and static RunList elements. |
| ClipPath? | PDFPath | Path that describes the outline of the RunList that SHALL be clipped. |
| ContentRefs? | IDREFS | Ordered list of IDs of Content elements. |
| DocPages? *(New in XJDF 2.1)* | IntegerList | @DocPages SHALL specify the number of pages in each document of a multi-Document Set. |
| Docs? | IntegerRange | Zero-based range of document indices. |
| EndOfDocument? | boolean | If "true", the last page in the RunList is the last page of an Instance Document. |
| EndOfSet? | boolean | If "true", the last page in the RunList is the last page of a set of Instance Documents. |
| FinishedPages? | integer | Number of Finished Page surfaces that one PDL page of this RunList refers to. |
| LogicalPage? | integer | The logical page number of the first page in a RunList. |
| NPage? | integer | Total number of pages (placed object slots) that are defined by the RunList. |
| OrdType? | enumeration | @OrdType SHALL specify the usage of this RunList element (Content, Insert, Reservation). |
| Pages? | IntegerRange | Zero-based range of indices of the pages. |
| Sets? | IntegerRange | Zero-based range of document-set indices. |
| SourceBleedBox? | rectangle | A rectangle that describes the bleed area. |
| SourceClipBox? | rectangle | A rectangle that defines the region of the element to be included. |
| SourceMediaBox? | rectangle | A rectangle that defines the intended media size. |
| SourceTrimBox? | rectangle | A rectangle that describes the intended trimmed size. |
| ByteMap? | element | Describes the page or stream of pages. |
| FileSpec? | element | URL plus metadata about the physical characteristics of a file. |
| MetadataMap* | element | Describes the mapping of metadata in a RunList to Partition Keys. |

### 6.73.1 Referencing pages of a RunList from a Layout
The Layout resource in the Imposition process references individual pages in a RunList by index in Layout/@Ord.

### 6.73.2 Filtering parts of a RunList
The Partition Keys: Part/@DocIndex, Part/@RunIndex, Part/@SetIndex and Part/@SheetIndex are provided to select subsets of a RunList.

### 6.73.3 Pages, Documents and Sets for common PDL types
**Table 6.149: Pages, Documents and Sets for common PDL types**
Maps PostScript, PDF, PDF/VT, and PPML structures to RunList Pages, Documents, and Sets.

### 6.73.4 Band
**Table 6.150: Band Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Height? | integer | Height in pixels of the band. |
| Width? | integer | Width in pixels of the band. |

### 6.73.5 ByteMap
A ByteMap represents a raster of image data.

**Table 6.151: ByteMap Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BandOrdering? | enumeration | Identifies the precedence given when ordering the produced bands. |
| FrameHeight? | integer | Height of the overall image. |
| FrameWidth? | integer | Width of overall image. |
| Halftoned? | boolean | Indicates whether or not the data has been halftoned. |
| Interleaved? | boolean | If "true", the data are interleaved or chunky. |
| PixelColorants? | NMTOKENS | Ordered list of separation identifiers. |
| PixelDepth? | integer | Number of bits per pixel for each colorant. |
| PixelSkip? | integer | Number of bits to skip between pixels of interleaved data. |
| Resolution? | XYPair | Output resolution of the ByteMap in dpi. |
| Band? | element | Description of the structure of the bands or tiles containing the raster data. |

## 6.74 ScreeningParams
ScreeningParams specifies the parameters of the Screening process.
*Resource Properties: Input of Processes: Screening*

**Table 6.152: ScreeningParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| IgnoreSourceFile? | boolean | If "true", the screen settings specified in a source file SHALL NOT be applied. |
| ScreenSelector+ | element | List of screen selectors. |

## 6.75 SeparationControlParams
SeparationControlParams provides the controls needed to separate composite color files.
*Resource Properties: Input of Processes: Separation*

**Table 6.153: SeparationControlParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| AutomatedOverPrintParams? | element | Controls for overprint substitutions. |

## 6.76 ShapeCuttingParams
ShapeCuttingParams defines the details of the ShapeCutting process.
*Resource Properties: Intent Pairing: ShapeCuttingIntent | Input of Processes: ShapeCutting*

**Table 6.154: ShapeCuttingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| DeliveryMode? | enumeration | Allowed values are: FullSheet, RemoveGripperMargin, SeparateBlanks. |
| DieLayoutRef? | IDREF | Reference to a DieLayout. |
| ModuleID? | NMTOKEN | Identifier of the shape-cutting module. |
| SheetLay? | enumeration | Lay of input media. |
| Shape* | element | Details of each individual cut shape. |

## 6.77 ShapeDef
A structural design describing a 2D surface with paths that describe different finishing operations.
*Resource Properties: Input of Processes: ShapeDefProduction | Output of Processes: ShapeDefProduction*

**Table 6.155: ShapeDef Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Area? | float | The net area of the shape in m2, after cutting. |
| CutBox? | rectangle | A rectangle describing the bounding box of all cut lines. |
| CutLines? | NMTOKENS | Selects the die line separation identifiers. |
| Dimensions? | shape | Width x, height y and depth z coordinates of the open 3D shape. |
| FlatDimensions? | shape | Width x, height y and depth z coordinates of the flat 3D shape. |
| FluteDirection? | enumeration | Intended direction of the flute for this design. |
| GrainDirection? | enumeration | Intended direction of the grain for this design. |
| MediaRef? | IDREF | Reference to a Media resource. |
| MediaSide? | enumeration | Determines the printing side (Front, Back). |
| ResourceWeight? | float | The weight of the shape after cutting (g). |
| FileSpec* *(Modified in XJDF 2.2)* | element | The FileSpec of the structural design files. |
| RuleLength* *(New in XJDF 2.2)* | element | Elements describing the length of die rules. |
| Shape* | element | The shape is defined by a collection of Shape elements. |

## 6.78 ShapeDefProductionParams
Parameters for the structural design.
*Resource Properties: Input of Processes: ShapeDefProduction*

**Table 6.156: ShapeDefProductionParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ObjectModel* | element | A 3D model of the objects that need to be packed. |
| ShapeTemplate? | element | A structural template sometimes referred to as a parametric structural design. |

### 6.78.1 ObjectModel
**Table 6.157: ObjectModel Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Dimensions? | shape | Width x, height y and depth z values for the bounding box of the object. |
| FileSpec? | element | The FileSpec of the 3D model of the objects. |

### 6.78.2 ShapeDimension
**Table 6.158: ShapeDimension Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Usage | string | @Usage specifies the name of the ShapeDimension (D, L, W). |
| Value | float | @Value specifies the length of the ShapeDimension in points. |

### 6.78.3 ShapeTemplate
**Table 6.159: ShapeTemplate Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| InnerDimensions? | shape | Width x, height y and depth z coordinates of the 3D shape. |
| Name? | NMTOKEN | The name of a parametric structural design or CAD template. |
| Standard? | NMTOKEN | The name of the standard this template belongs to. |
| FileSpec* *(Modified in XJDF 2.2)* | element | Reference to an external URL that represents the parametric structural design. |
| ShapeDimension* | element | ShapeDimension elements define additional parametric values. |

*(Figures 6-35 to 6-37 illustrate ShapeTemplate examples.)*

## 6.79 SheetOptimizingParams
SheetOptimizingParams describes the parameter set for the SheetOptimizing process.
*Resource Properties: Input of Processes: SheetOptimizing*

**Table 6.160: SheetOptimizingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Policy? *(New in XJDF 2.1)* | enumeration | The policy with which the GangElements in the Gang SHALL be processed (All, Collect, Optimized). |
| ConvertingConfig* *(Modified in XJDF 2.1)* | element | Specification of the Device configurations for destination sheet sizes. |
| GangElement+ | element | Each GangElement describes an individual product or product part. |

### 6.79.1 GangElement
**Table 6.161: GangElement Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BinderySignatureIDs? | NMTOKENS | List of Part/@BinderySignatureID of all BinderySignatures. |
| CollapseBleeds? | boolean | If "true", the bleed margin between the instances of GangElement elements SHOULD be removed. |
| CustomerID? *(New in XJDF 2.1)* | NMTOKEN | Human or Machine readable identifier of the Customer. |
| Dimension? | XYPair | The GangElement block size including trims and bleeds. |
| DeliveryDate? *(New in XJDF 2.1)* | dateTime | The latest date and time that the Final Product SHALL be delivered. |
| DueDate? | dateTime | The latest date and time the GangElement SHALL be included on a Gang. |
| ExternalID? | NMTOKEN | The ID of the product in an external system. |
| FillPriority? | integer | If non-zero, the ganging engine SHOULD fill any left over space with this GangElement. |
| GangElementID | NMTOKEN | An identifier of the GangElement that is unique within the context of the workflow. |
| GrainDirection? | enumeration | The allowed grain direction of the paper. |
| GroupCode? | NMTOKEN | Code specifying a group of products. |
| JobID? | NMTOKEN | The original XJDF/@JobID of the GangElement. |
| MaxQuantity? | integer | The maximum number of printed (fold) sheets that may be produced. |
| MediaRef? *(Deprecated in XJDF 2.1)* | IDREF | Deprecation note: Use the inline Media resource. |
| MinQuantity? | integer | The minimum number of printed (fold) sheets that SHALL be produced. |
| NPage? | integer | The total number of pages of the GangElement. |
| NumberUp? | XYPair | The number up that SHALL be placed on the Gang in a single block. |
| OneSheet? | NMTOKEN | @OneSheet controls how this GangElement SHOULD be placed (Any, GangElementID, JobID). |
| Operations? *(New in XJDF 2.1)* | NMTOKENS | List of finishing operations or properties required. |
| OrderQuantity | integer | The number of printed (fold) sheets to produce. |
| PageDimension? | XYPair | The page size, including trims and bleeds. |
| PlacedQuantity? *(New in XJDF 2.1)* | integer | @PlacedQuantity SHALL specify the total quantity of all positions. |
| Priority? | integer | @Priority controls the relative order of including GangElement items. |
| RotationPolicy? | enumeration | Specifies the level of freedom when applying @GrainDirection. |
| RunListRef? *(Deprecated in XJDF 2.1)* | IDREF | Deprecation note: Use the inline RunList resource. |
| SeparationListBack? | NMTOKENS | @SeparationListBack SHALL specify the list of separation identifiers for the back side. |
| SeparationListFront? | NMTOKENS | @SeparationListFront SHALL specify the list of separation identifiers for the front side. |
| GeneralID* *(New in XJDF 2.1)* | element | GangElements MAY be labelled by generic identifiers. |
| Media? *(New in XJDF 2.1)* | element | Media definition whose characteristics SHALL be met in the Gang. |
| RunList* *(New in XJDF 2.1)* | element | RunList SHALL specify the content data for this GangElement. |

## 6.80 SheetOptimizingReport
*(New in XJDF 2.2)* Output of Processes: SheetOptimizing. SheetOptimizingReport SHALL specify a summary of the Gang quality.

**Table 6.162: SheetOptimizingReport Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| AreaUse | float | @AreaUse SHALL specify the ratio of printed area to total printable area. |
| BackUse? | float | @BackUse SHALL specify the ratio of duplex area to the used area. |
| DateSpread? | duration | @DateSpread SHALL specify the difference between the earliest and latest date of printing. |
| OrderQuantity? | integer | @OrderQuantity SHALL specify the number of printed GangElement copies that can be invoiced. |
| Positions? | integer | @Positions SHALL specify the total number of Position elements placed on the sheet. |
| PrintableArea? | float | @PrintableArea SHALL specify the ratio of printable area to total media size. |
| PrintedWaste? | float | @PrintedWaste SHALL specify the ratio of printed waste area. |
| UniquePositions? | integer | @UniquePositions SHALL specify the number of unique Positions. |
| UniqueUse? | float | @UniqueUse SHALL specify the ratio of printed area used if each Gang element were placed once. |
| VolumeUse | float | @VolumeUse SHALL specify the ratio of printed area on all sheets to total printable area. |
| WasteQuantity? | integer | @WasteQuantity SHALL specify the number of printed GangElement copies that cannot be invoiced. |

## 6.81 ShrinkingParams
ShrinkingParams provides the parameters for the Shrinking process in shrink wrapping.
*Resource Properties: Input of Processes: Shrinking*

**Table 6.163: ShrinkingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ShrinkingMethod? | enumeration | Specifics of the shrinking method (ShrinkCool, ShrinkHot). |
| Temperature? | float | Shrinking temperature. |

## 6.82 SpinePreparationParams
SpinePreparationParams describes the preparation of the spine of book blocks.
*Resource Properties: Intent Pairing: BindingIntent | Input of Processes: SpinePreparation*

**Table 6.164: SpinePreparationParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| MillingDepth? | float | Milling depth, in points. |
| NotchingDepth? | float | Notching depth relative to the leveled spine, in points. |
| NotchingDistance? | float | Notching distance, in points. |
| Operations? | NMTOKENS | List of operations that SHALL be applied to the spine. |
| SealingTemperature? | integer | @SealingTemperature is the temperature needed to melt the sealing thread and sheet. |
| StartPosition? | float | Starting position of the milling tool along the Y-axis. |
| WorkingLength? | float | Working length of the milling operation. |

*Figure 6-38: Parameters and coordinate systems for the SpinePreparation process.*

## 6.83 SpineTapingParams
SpineTapingParams define the parameters for taping a strip tape or kraft paper to the spine.
*Resource Properties: Intent Pairing: BindingIntent | Input of Processes: SpineTaping*

**Table 6.165: SpineTapingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| HorizontalExcess? | float | Taping spine excess on each side. |
| HorizontalExcessBack? | float | Horizontal excess of back if tape is not centered. |
| StripLength? | float | Length of strip material along binding edge. |
| TopExcess? | float | Top spine taping excess. |
| Glue* | element | Describes where and how to apply glue to the book block. |

*Figure 6-39: Parameters and coordinate system for the SpineTaping process.*

## 6.84 StackingParams
Settings for the Stacking process.
*Resource Properties: Input of Processes: Stacking*

**Table 6.166: StackingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BundleType? | enumeration | @BundleType specifies the BundleItem/@BundleType of the items counted as an individual item. |
| Compensate? | boolean | 180 degree rotation applied to successive layers to compensate for uneven stacking. |
| LayerAmount? | IntegerList | Ordered number of products in a layer. |
| LayerCompression? | boolean | If "true", layer is compressed before next layer is started. |
| LayerLift? | boolean | If "true", layer is lifted to reduce height. |
| MaxAmount? | integer | Maximum number of products in a stack. |
| MaxHeight? | integer | Maximum height of the stack in points. |
| MaxWeight? | float | Maximum weight of a stack in grams. |
| MinAmount? | integer | Minimum number of products in a stack or layer. |
| OutputBin? | NMTOKENS | Specifies the bin or bins to which the finished documents SHALL be output. |
| PreStackAmount? | integer | Amount that is initially gathered. |
| PreStackMethod? | enumeration | Allowed values are: All, First, None. |
| StackAmount? | integer | Specifies the maximum sheet count before switching to the next stacker. |
| StackCompression? | boolean | If "true", the stack is compressed before push out. |
| StandardAmount? | integer | Number of products in a standard stack. |
| UnderLays? | IntegerList | Number of underlay sheets at each layer. |
| Disjointing? | element | Details of the offset or shift applied to successive layers or documents. |
| InsertSheet* *(Modified in XJDF 2.1)* | element | Each InsertSheet SHALL specify some kind of physical marker. |

### 6.84.1 Disjointing
**Table 6.167: Disjointing Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Amount? | integer | The number of components that SHALL be shifted in @Direction simultaneously. |
| Direction? | enumeration | Offset-shift action for the first component (Alternate, Left, None, Right). |
| Offset? | XYPair | Offset dimension in X and Y dimensions. |
| Units? | NMTOKEN | This attribute specifies the type of component counted (DocCopies, Docs, Jobs, SetCopies, Sets, Sheets). |

### 6.84.2 InsertSheet
**Table 6.168: InsertSheet Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| IsWaste? | boolean | Specifies whether the InsertSheet is waste. |
| MediaRef? *(New in XJDF 2.1)* | IDREF | Reference to the Media that is used for this InsertSheet. |
| SheetFormat? | NMTOKEN | Identifies that Device dependent information SHALL be included (Blank, Brief, Full, Standard). |
| SheetType | enumeration | Identifies the type of sheet (AccountingSheet, ErrorSheet, JobSheet, SeparatorSheet). |
| SheetUsage | enumeration | Indicates where this InsertSheet SHALL be produced (Header, Interleaved, InterleavedBefore, OnError, Slip, SlipCopy, Trailer). |
| StripMark* | element | StripMark provides formatting and content for the InsertSheet. |

## 6.85 StitchingParams
StitchingParams provides the parameters for the Stitching process.
*Resource Properties: Intent Pairing: BindingIntent | Input of Processes: Stitching*

**Table 6.170: StitchingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Angle? | float | Angle of stitch in degrees. |
| NumberOfStitches? | integer | @NumberOfStitches specifies the number of stitches. |
| Offset? | float | Distance between stitch and binding edge. |
| StapleShape? | enumeration | Specifies the shape of the staples to be used. |
| StitchOrigin? | enumeration | Defines the origin of @StitchPositions (TrimBoxCenter, TrimBoxJogSide, UntrimmedJogSide). |
| StitchPositions? | FloatList | Array containing the stitch positions. |
| StitchType? | enumeration | Specifies the type of the Stitching operation (Corner, Saddle, Side). |
| StitchWidth? | float | Width of the stitch to be used. |
| TightBacking? | enumeration | Definition of the geometry of the back of the product. |
| WireGauge? | float | Gauge of the wire to be used. |
| FileSpec(CIP3)? | element | Reference to a CIP3 file that contains stitching instructions. |

*Figures 6-40 to 6-42: Diagrams illustrating Stitching coordinate systems and parameters.*

## 6.86 StrappingParams
StrappingParams defines the details of Strapping.
*Resource Properties: Input of Processes: Strapping*

**Table 6.171: StrappingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| StrappingType | enumeration | Strapping pattern (Cross, Double, DoubleCross, Single). |
| StrapPositions? | FloatList | Positions of the straps beginning from the origin of the coordinate system. |

*Figures 6-43 & 6-44: Diagrams illustrating Strapped bundles.*

## 6.87 ThreadSealingParams
ThreadSealingParams provides the parameters for the ThreadSealing process.
*Resource Properties: Intent Pairing: BindingIntent | Input of Processes: ThreadSealing*

**Table 6.172: ThreadSealingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BlindStitch? | boolean | A value of "true" specifies a blind stitch after the last stitch. |
| ThreadLength? | float | Length of one thread. |
| ThreadPositions? | FloatList | Array containing the y-coordinate of the center positions of the thread. |
| ThreadStitchWidth? | float | Width of one stitch. |

## 6.88 ThreadSewingParams
ThreadSewingParams provides the parameters for the ThreadSewing process.
*Resource Properties: Intent Pairing: BindingIntent | Input of Processes: ThreadSewing*

**Table 6.173: ThreadSewingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| BlindStitch? | boolean | A value of "true" specifies a blind stitch after the last stitch. |
| NeedlePositions? | FloatList | Array containing the y-coordinate of the needle positions. |
| NumberOfNeedles? | integer | Specifies the number of needles to be used. |
| Offset? | float | Specifies the distance between the stitch and the binding edge. |
| SewingPattern? | enumeration | Sewing pattern (CombinedStaggered, Normal, Side, Staggered). |
| ThreadThickness? | float | Thread thickness. |

*Figures 6-45 & 6-46: Diagrams illustrating Thread sewing coordinate systems.*

## 6.89 Tool
A Tool defines a generic tool that can be customized for a given job or an auxiliary Device.
*Resource Properties: Input of Processes: Any Process, Embossing, ShapeCutting | Output of Processes: DieMaking*

**Table 6.174: Tool Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Manufacturer? *(New in XJDF 2.2)* | string | Manufacturer name. |
| ManufacturerURL? *(New in XJDF 2.2)* | URL | Web site for manufacturer. |
| SerialNumber? *(New in XJDF 2.2)* | string | Serial number of the tool. |
| ToolType? | NMTOKEN | @ToolType specifies the type of the tool (Braille, CentralStripper, ChangingCuttingBlock, CounterDie, CutDie, EndBoard, EmbossingCalendar, EmbossingStamp, ForkLift, FrontWasteSeparator, LowerBlanker, LowerStripper, RollStand, ScreeningRoller, ToolSet, UpperBlanker, UpperStripper). |
| IdentificationField* | element | IdentificationField associates bar codes or labels with this tool. |

*Figure 6-47: Diagram illustrating a Roll stand.*

## 6.90 TransferCurve
TransferCurve elements specify the characteristic curve of transfer of densities between systems.
*Resource Properties: Input of Processes: Any Process*

**Table 6.175: TransferCurve Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CTM? | matrix | @CTM SHALL define the transformation of the coordinate system in the Device. |
| Curve? | Transfer-Function | The density mapping curve for this TransferCurve. |

## 6.91 TrappingParams
TrappingParams provides a set of controls that are used to generate traps.
*Resource Properties: Input of Processes: Trapping*

**Table 6.176: TrappingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ImageInternalTrapping? | boolean | If "true", the planes of color images are trapped against each other. |
| ImageMaskTrapping? | boolean | Controls trapping when the image contains a stencil mask. |
| ImageToImageTrapping? | boolean | If "true", traps are generated along a boundary between images. |
| ImageToObjectTrapping? | boolean | If "true", images are trapped to other objects. |
| MinimumBlackWidth? | float | Specifies the minimum width, in points, of a trap that uses black ink. |
| StepLimit? | float | Specifies the smallest step needed in the color value of a colorant to trigger trapping. |
| TrapColorScaling? | float | Specifies a scaling of the amount of color applied in traps towards the neutral density. |
| TrapWidth? | XYPair | Specifies the trap width in the X and Y directions. |

## 6.92 TrimmingParams
TrimmingParams provides the parameters for the Trimming process.
*Resource Properties: Input of Processes: Trimming*

**Table 6.177: TrimmingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Height? | float | Height of the trimmed product. |
| TrimCover? | enumeration | Specifies the covers to be trimmed (Back, Both, Front, Neither). |
| TrimmingOffset? | float | Amount to be cut from the bottom side. |
| Width? | float | Width of the trimmed product. |

## 6.93 UsageCounter
UsageCounter represents a type of equipment or software usage that is tracked by the value of a usage counter.
*Resource Properties: Input of Processes: Any Process*

**Table 6.178: UsageCounter Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| CounterTypes? | NMTOKENS | This attribute indicates the types of usage being counted (Insert, OneSided, TwoSided, NormalSize, LargeSize, Black, Color, Blank, HighlightColor). |
| Scope | enumeration | The scope of this usage counter (Job, Lifetime, PowerOn). |

## 6.94 VarnishingParams
VarnishingParams provides the parameters of a Varnishing process.
*Resource Properties: Intent Pairing: ColorIntent | Input of Processes: Varnishing*

**Table 6.179: VarnishingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ModuleID? | NMTOKEN | Identifier of the varnishing module. |
| ModuleType? | enumeration | The type of module used to apply the varnish (CoatingModule, PrintModule). |
| VarnishArea? | enumeration | Area to be varnished (Full, Spot). |
| VarnishMethod? | enumeration | Method used for varnishing (Blanket, Independent, Plate). |

### 6.94.1 Combined Use of VarnishingParams Attributes
*(New in XJDF 2.2)*
**Table 6.180: Combinations of ModuleType, VarnishArea and VarnishMethod**
*(Details combinations such as CoatingModule + Full + Blanket = Flood varnishing in a dedicated coating module, PrintModule + Spot = DigitalPrinting or ConventionalPrinting using transparent ink, etc.)*

## 6.95 VerificationParams
VerificationParams provides the parameters of a Verification process.
*Resource Properties: Input of Processes: Verification*

**Table 6.181: VerificationParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Tolerance? | float | Ratio of tolerated verification failures to the total number of tests. |
| FileSpec? | element | Reference to data that contains implementation specific descriptions of the resources to be verified. |

## 6.96 VerificationResult
VerificationResult defines the set of results from the Verification process.
*Resource Properties: Output of Processes: Verification*

**Table 6.182: VerificationResult Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Accepted? | integer | Number of resources that were correctly verified. |
| Rejected? | integer | Number of resources that were not correctly verified. |
| Unknown? | integer | Number of resources that were scanned but are not in the explicit or implied list of known resources. |
| FileSpec(Accepted)? | element | Reference to data for correctly verified resources. |
| FileSpec(Combined)? | element | Reference to data for an implementation specific description of the result. |
| FileSpec(Rejected)? | element | Reference to data for NOT correctly verified resources. |
| FileSpec(Unknown)? | element | Reference to data for scanned but unknown resources. |

## 6.97 WebInlineFinishingParams
WebInlineFinishingParams specifies the parameters for web inline finishing equipment.
*Resource Properties: Input of Processes: WebInlineFinishing*

**Table 6.183: WebInlineFinishingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| FolderProduction* | element | Specifies the folder setup for newspaper presses. |
| ProductionPath? | element | ProductionPath describes the paper path that is used through the press. |

### 6.97.1 FolderProduction
**Table 6.184: FolderProduction Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ModuleID? | NMTOKEN | Identifies a particular folder module to be used. |
| ProductionType? | enumeration | Indicates whether the product is collected or not (Collect, NonCollect). |

### 6.97.2 ProductionPath
**Table 6.185: ProductionPath Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| ProductionPathID? | NMTOKEN | @ProductionPathID specifies the identification of the entire production path. |

## 6.98 WindingParams
The parameters for the Winding process.
*Resource Properties: Input of Processes: Winding*

**Table 6.186: WindingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| Copies? | integer | Number of copies in one column that SHOULD be placed on a finished roll. |
| Diameter? | float | Outer diameter in points of the finished roll. |
| Fixation? | NMTOKEN | Method specifying how the Component is attached to the core (DoubleSidedTape, GlueLabel, None, SingleSidedTape). |
| Length? | float | Length in points of the Component to be placed on a finished roll. |

## 6.99 WrappingParams
WrappingParams defines the details of Wrapping.
*Resource Properties: Input of Processes: Wrapping*

**Table 6.187: WrappingParams Resource**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| WrappingKind | enumeration | @WrappingKind specifies the wrapping method (Band, LooseWrap, ShrinkWrap). |