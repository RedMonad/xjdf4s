> **Note:** Chapter 9, “Building a System,” is omitted from this conversion. References to that chapter or its sections are marked as omitted where they appear.

# Chapter 3 Structure

A single XJDF describes the information about a job or process step that is transferred from a Controller to a Device. The scope of the exchanged information varies depending on the nature of the recipient Device. An XJDF that is targeted at an individual Device will typically contain only the details that are required by that Device, along with some optional information about the Final Product. Multiple work steps belonging to one job that need to be submitted from a Controller to a workflow system that controls multiple Devices SHALL be submitted as a separate XJDF for each work step. These MAY be packaged together and submitted as one or more transactions. See Building a System — chapter omitted — for details of packaging and referencing of the individual XJDF.

## 3.1 XJDF

The top-level element of an XJDF instance SHALL be an `XJDF` element. See **Table 3.1: XJDF** below for details. `XJDF` elements MAY be embedded within other XML documents.

`XJDF/@Types` defines whether an XJDF specifies an end product or a list of processes that SHALL be executed. XJDF that are created by print buyers typically describe only the desired product rather than manufacturing process details. XJDF that describe finished products SHALL have a value of `XJDF/@Types` that contains `"Product"`. If additional process information that is not defined in the `ProductList` is required, this information SHOULD be provided in `ResourceSet` elements. `ProductList` MAY be provided in a process XJDF for informational purposes.

### Table 3.1: XJDF — Sheet 1 of 2

| Name | Data Type | Description |
|---|---|---|
| `$schema`? | `URL` | `@$schema` SHOULD reference the JSON schema for XJDF.<br><br>**JSON Exception:** `@$schema` SHOULD be provided in JSON if `XJDF` is the root JSON object and SHALL NOT be provided in XML. |
| `Category`? | `NMTOKEN` | `@Category` specifies the named category of this XJDF. Controllers SHOULD specify `@Category` for processes that have many optional values in `@Types`.<br><br>This allows processors to identify the general purpose of an XJDF without parsing the `@Types` field. For instance, a RIP for final output and a RIP for proof process have identical `@Types` attribute values, but have `@Category="RIPing"` or `@Category="ProofRIPing"`, respectively.<br><br>Values include those from: **Node Categories**.<br><br>**Note:** `@Category` MAY also be the name of a Gray Box defined by an ICS document. See Section 1.9.2 **Interoperability Conformance Specifications** for details. |
| `CommentURL`? | `URL` | `@CommentURL` SHALL refer to an external, human-readable description of this XJDF. |
| `DescriptiveName`? | `string` | Human-readable descriptive name of this XJDF. `@DescriptiveName` SHOULD be provided for communication from applications to humans in order to reference the XJDF. |
| `ICSVersions`? | `NMTOKENS` | `@ICSVersions` SHALL list all CIP4 Interoperability Conformance Specification — ICS — Versions that this XJDF complies with. The value of `@ICSVersions` SHALL conform to the value format described in Section 3.1.1 **ICS Versions Value**. |
| `JobID` | `NMTOKEN` | Job identification used by the application that created the XJDF job. Typically, a job is identified by the internal order number of the MIS system that created the job. |
| `JobPartID`? | `NMTOKEN` | `@JobPartID` SHALL identify one or more worksteps of the same type that can be described as one XJDF. `@JobPartID` is internal to the MIS system that created the XJDF. |

### Table 3.1: XJDF — Sheet 2 of 2

| Name | Data Type | Description |
|---|---|---|
| `Name`? | `enumeration` | `@Name` SHALL specify the local name of the XJDF when `XJDF` is defined as a root JSON object.<br><br>Allowed value is:<br>`XJDF`<br><br>**JSON Exception:** `@Name` SHALL be provided in JSON if `XJDF` is the root JSON object and SHALL NOT be provided in XML. |
| `ProjectID`? | `NMTOKEN` | Identification of the project context that this XJDF belongs to. `@ProjectID` SHOULD be used by a Controller to group a set of XJDF jobs. |
| `RelatedJobID`? | `NMTOKEN` | Job identification of a related job. Used to identify the `@JobID` of a previous run of this job or job with very similar settings. It MAY be used to retrieve additional job and Device specific settings from a data store.<br><br>`@RelatedJobID` SHALL be specified if `@RelatedJobPartID` is specified. |
| `RelatedJobPartID`? | `NMTOKEN` | Job identification of a related Job Part. Used to identify the `@JobPartID` of a previous run of this job or job with very similar settings. It MAY be used to retrieve additional job and Device specific settings from a data store.<br><br>`@RelatedJobPartID` SHALL NOT be specified unless `@RelatedJobID` is also specified. |
| `RelatedProjectID`? | `NMTOKEN` | Identification of a related project context that this XJDF belongs to.<br><br>`@RelatedProjectID` SHOULD be used by a Controller to group a set of XJDF jobs. |
| `Types` | `NMTOKENS` | A list of one or more process names that are specified within this XJDF document. For details on using processes, see Section 3.1.3 **XJDF for Process Description and Gray Boxes**. A value of `@Types` that contains `"Product"` specifies that the products that are described shall be produced without complete knowledge of the production workflow. Additional process specifics MAY be supplied in a ticket that contains `"Product"`.<br><br>Values include those from: **Chapter 5 Processes**. |
| `Version`? | `enumeration` | `@Version` SHALL define the version of the XJDF document. The value of `@Version` SHALL be `"2.2"` for documents that comply to this specification.<br><br>Allowed value is from: **XJDFXJMFVersion**. |
| `AuditPool`? | `element` | List of elements that contains all relevant audit information. `AuditPool` elements are intended to serve the requirements of MIS for evaluation and post calculation. See Section 3.2 **AuditPool**. |
| `Comment`* | `element` | Any human-readable text. The `Comment` element is different from an XML comment `<!-- XML Comment -->`. The XJDF comment is meant for display in a user interface whereas the XML comment is used to add developer’s comments to the underlying XML. |
| `GeneralID`* | `element` | Additional identifiers related to the XJDF. |
| `ProductList`? | `element` | Bill of materials — description of the product, or products that this XJDF produces. |
| `ResourceSet`* | `element` | Container elements for `Resource` elements. |

### Example 3.1: JSON-encoded XJDF

The following example illustrates how a simple XJDF root node is encoded in both XML and JSON.

**XML Encoding**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<XJDF xmlns="http://www.CIP4.org/JDFSchema_2_0" JobID="J1" Types="Product" Version="2.2"/>
```

**JSON Encoding**

```json
{
  "JobID": "J1",
  "Name": "XJDF",
  "Types": [
    "Product"
  ],
  "Version": "2.2"
}
```

## 3.1.1 ICS Versions Value

To assist with interoperability conformance the XJDF can refer to one or more CIP4 Interoperability Conformance Specification documents. Each document is referenced by using an `NMTOKEN` that complies with the following:

Value format:

```text
<ICSName>_L<ICSLevel>-<ICSVersion>
```

If an XJDF corresponds to multiple Conformance Levels of the same ICS, the highest applicable level SHOULD be provided.

Example:

```text
"MISPRE_L1-2.0"
```

for the MIS to Prepress ICS.

## 3.1.2 XJDF for Product Intent

XJDF that are created by end customers typically describe only the desired product rather than manufacturing process details.

`XJDF/@Types` SHALL have a value of `"Product"` to indicate that the XJDF does not specify any processing.

`Product` elements SHOULD include intent elements that describe the end results the customer is requesting. If additional process information that is not defined in the intent elements is required, this information SHOULD be provided in `ResourceSet` elements. The value of `XJDF/@Types` SHALL remain `"Product"`.

## 3.1.3 XJDF for Process Description and Gray Boxes

Process XJDF contain processing instructions in `ResourceSet` elements that are targeted to a specific Device in addition to an optional product definition in a `ProductList` and intent elements. `@Types` of process XJDF SHALL NOT contain the token `"Product"` if any additional process type tokens are present. In some cases such as prepress, a Controller such as an MIS will not know all the details of the process including the exact list of `XJDF/@Types` or any details of the `ResourceSets` for the respective processes. It can still provide an XJDF with the limited, known information. These limited XJDF are referred to as Gray Boxes.

**Note:** There is no syntactical difference between a Gray Box and a dedicated process XJDF. The boundary between Gray Boxes and dedicated process XJDF is very fuzzy, since Devices will typically apply defaults to any data that is missing in a process XJDF.

### 3.1.3.1 Specifying NamedFeatures with GeneralID

XJDF MAY contain zero or more `GeneralID[@Datatype="NamedFeature"]` elements to specify global setup definitions. These `GeneralID` elements that are referred to as “NamedFeatures” in this paragraph allow a Controller to define a named set of parameters for processes that SHALL be executed without defining the details or even the resources.

Explicitly specified Traits SHALL override any implied Traits defined by `GeneralID[@Datatype="NamedFeature"]`.

`XJDF/@Types` abstractly specifies the set of processes to execute, whereas `"NamedFeatures"` abstractly specifies the set of resources for the processes specified in `@Types`.

## 3.2 AuditPool

`AuditPool` elements contain the recorded results of a process. Audits are conceptually very similar to job-specific signals. Signals record the current state of a process or Device, whereas audits summarize that Device state over a longer period during the execution of a single process. Thus an audit will summarize the result of multiple signals belonging to a unique phase in the returned XJDF. A unique phase SHOULD contain the same combination of `JobPhase/@Status` and `JobPhase/@StatusDetails`. Thus minor variations such as speed NEED NOT be recorded as separate audits although they MAY have slightly varying values in the respective signals. `AuditPool` elements record any event related to the situations described in **Table 3.2: Alignment of Audits and Messages**.

**Note:** Audits are always in the context of a process. Thus job independent signals such as the `SignalStatus` of an idle Device will never be tracked as an audit.

**Note:** The data in XJMF responses are very similar to the data in XJMF signals. The only difference is that XJMF responses are synchronous HTTP responses. Therefore, all discussions referring to signals in this section apply equally to responses.

Audit information might be used by MIS for operations such as evaluation or invoicing. `AuditPool` entries are ordered chronologically, with the last entry in the `AuditPool` representing the newest. An `AuditProcessRun` element shall finalize each Workstep. All subsequent entries in the `AuditPool` belong to the next Workstep.

The following table defines the contents of the `AuditPool` element. In contrast to most other elements in XJDF the child elements of `AuditPool` SHALL be ordered chronologically from oldest to newest rather than alphabetically.

**JSON Exception:** JSON does not allow for arrays with mixed element types. Therefore the audits are defined as individual entries in an `AuditPool` array with a `@Name` that SHALL have the value of the local name of the individual audit. See **Example 3.2: JSON-Encoded AuditPool**.

### Table 3.2: Alignment of Audits and Messages

| Audit | Signal | Comment |
|---|---|---|
| `AuditCreated` | — | `AuditCreated` SHOULD be specified by the original creator of the XJDF and SHOULD be the first audit in the `AuditPool`. |
| `AuditNotification` | `SignalNotification` | One `AuditNotification` SHOULD be specified for each `SignalNotification`. |
| `AuditProcessRun` | `CommandReturnQueueEntry` | A process run SHOULD be specified whenever an XJDF is returned to the Controller by a Device. |
| `AuditResource` | `SignalResource` | One `AuditResource` SHOULD be specified with the final data for each unique phase. |
| `AuditStatus` | `SignalStatus` | One `AuditStatus` SHOULD be specified with the final data for each phase. |

### Table 3.3: AuditPool Element

| Name | Data Type | Description |
|---|---|---|
| `Name`? | `enumeration` | `@Name` SHALL specify the local name of the individual audit.<br><br>Allowed values are:<br>`AuditCreated`<br>`AuditNotification`<br>`AuditProcessRun`<br>`AuditResource`<br>`AuditStatus`<br><br>**JSON Exception:** `@Name` SHALL be provided in JSON and SHALL NOT be provided in XML. |
| `AuditCreated`* | `element` | Logs creation of an XJDF. |
| `AuditNotification`* | `element` | Logs individual events that occurred during processing. |
| `AuditProcessRun`* | `element` | Summarizes one complete execution run of an XJDF or delimits a group of `AuditPool` elements for each individual process run. |
| `AuditResource`* | `element` | Describes the usage of resources during execution of an XJDF or the modification of the intended usage of a resource. |
| `AuditStatus`* | `element` | Logs start and end times of any process states and sub-states, denoted as phases. Phases can reflect any arbitrary subdivisions of a process. |

### Example 3.2: JSON-Encoded AuditPool

The following example illustrates how the XJDF `AuditPool` element is encoded in both XML and JSON.

**XML Encoding**

```xml
<AuditPool>
  <AuditCreated>
    <Header DeviceID="CIP4_JDF_Writer_Java" Time="2024-04-29T12:21:56+02:00"/>
  </AuditCreated>
  <AuditStatus>
    <Header DeviceID="CIP4_JDF_Writer_Java" Time="2024-04-29T12:21:56+02:00"/>
    <DeviceInfo Status="Production"/>
  </AuditStatus>
  <AuditResource>
    <Header DeviceID="CIP4_JDF_Writer_Java" Time="2024-04-29T12:21:56+02:00"/>
    <ResourceInfo>
      <ResourceSet Name="Component"/>
    </ResourceInfo>
  </AuditResource>
  <AuditNotification>
    <Header DeviceID="CIP4_JDF_Writer_Java" Time="2024-04-29T12:21:56+02:00"/>
    <Notification Class="Warning"/>
  </AuditNotification>
  <AuditProcessRun>
    <Header DeviceID="CIP4_JDF_Writer_Java" Time="2024-04-29T12:21:56+02:00"/>
    <ProcessRun End="2024-04-29T12:21:56+02:00" EndStatus="Completed" Start="2024-04-29T12:21:56+02:00"/>
  </AuditProcessRun>
</AuditPool>
```

**JSON Encoding**

```json
{
  "AuditPool": [
    {
      "Header": {
        "DeviceID": "CIP4_JDF_Writer_Java",
        "Time": "2024-04-29T12:18:00+02:00"
      },
      "Name": "AuditCreated"
    },
    {
      "DeviceInfo": {
        "Status": "Production"
      },
      "Header": {
        "DeviceID": "CIP4_JDF_Writer_Java",
        "Time": "2024-04-29T12:18:00+02:00"
      },
      "Name": "AuditStatus"
    },
    {
      "Header": {
        "DeviceID": "CIP4_JDF_Writer_Java",
        "Time": "2024-04-29T12:18:00+02:00"
      },
      "Name": "AuditResource",
      "ResourceInfo": {
        "ResourceSet": {
          "Name": "Component"
        }
      }
    },
    {
      "Header": {
        "DeviceID": "CIP4_JDF_Writer_Java",
        "Time": "2024-04-29T12:18:00+02:00"
      },
      "Name": "AuditNotification",
      "Notification": {
        "Class": "Warning"
      }
    },
    {
      "Header": {
        "DeviceID": "CIP4_JDF_Writer_Java",
        "Time": "2024-04-29T12:18:00+02:00"
      },
      "Name": "AuditProcessRun",
      "ProcessRun": {
        "End": "2024-04-29T12:18:00+02:00",
        "EndStatus": "Completed",
        "Start": "2024-04-29T12:18:00+02:00"
      }
    }
  ]
}
```

## 3.2.1 AuditCreated

`AuditCreated` allows the original agent that created an XJDF to provide details about the software and the time of creation.

### Table 3.4: AuditCreated Element

| Name | Data Type | Description |
|---|---|---|
| `Header` | `element` | See `Message/Header`. |
| `<foreign namespace elements>`* | `element` | See `Message` — foreign namespace elements. |

## 3.2.2 AuditNotification

`AuditNotification` contains information about individual events that occurred during processing. For a detailed discussion of event properties, see Error Handling — omitted chapter “Building a System.”

`AuditNotification` is syntactically the same as `SignalNotification`. A Device SHOULD write an `AuditNotification` element for every `SignalNotification` XJMF that it emits.

### Table 3.5: AuditNotification Element

| Name | Data Type | Description |
|---|---|---|
| `Header` | `element` | See `Message/Header`. |
| `Notification` | `element` | Notification that describes the event. See Section 8.18 **Event** and Section 8.31.1 **Milestone**. |
| `<foreign namespace elements>`* | `element` | See `Message` — foreign namespace elements. |

## 3.2.3 AuditProcessRun

`AuditProcessRun` summarizes one execution of a Workstep. An `AuditProcessRun` SHALL be written each time an XJDF is returned to a Controller.

All job related amounts in subsequent `AuditPool` elements and XJMF messages SHALL restart at 0 when an XJDF is processed on a Device after an `AuditProcessRun` has been sent.

### Table 3.6: AuditProcessRun Element

| Name | Data Type | Description |
|---|---|---|
| `Header` | `element` | See `Message/Header`. |
| `ProcessRun` | `element` | Details of the individual Workstep execution. |
| `<foreign namespace elements>`* | `element` | See `Message` — foreign namespace elements. |

### 3.2.3.1 ProcessRun

The `ProcessRun` element contains the details of the individual Workstep execution.

### Table 3.7: ProcessRun Element — Sheet 1 of 2

| Name | Data Type | Description |
|---|---|---|
| `Duration`? | `duration` | Time span of the effective Workstep runtime without intentional or unintentional breaks. That time span is the sum of all process phases when the `NodeInfo/@Status` is `"InProgress"`, `"Setup"` or `"Cleanup"`. |
| `End` | `dateTime` | Date and time at which the Workstep ended. |
| `EndStatus` | `enumeration` | The `NodeInfo/@Status` of the Workstep at the end of the run. For a description of process states, see Appendix A.2.45 **Status**.<br><br>Allowed values are:<br>`Aborted` — The XJDF has been aborted before producing the desired result.<br>`Completed` — The XJDF has been completed and the desired result has been produced. |
| `QueueEntryID`? | `NMTOKEN` | `@QueueEntryID` of the `QueueEntry` for which this `AuditProcessRun` was generated. |
| `ReturnTime`? | `dateTime` | Date and time of the `ReturnQueueEntry` submission. |
| `Start` | `dateTime` | Date and time at which the Workstep started. |
| `SubmissionTime`? | `dateTime` | Date and time of the `SubmitQueueEntry` submission. This value SHOULD be identical with `QueueEntry/@SubmissionTime`. |

### Table 3.7: ProcessRun Element — Sheet 2 of 2

| Name | Data Type | Description |
|---|---|---|
| `Part`* | `element` | Describes which parts of a Workstep this `ProcessRun` belongs to. If `Part` is not specified for a `ProcessRun`, it refers to all parts. |

## 3.2.4 AuditResource

The `AuditResource` element describes the usage of resources during execution of a process. It logs consumption and production amounts of any quantifiable resources, accumulated over one process run or one part of a process run.

`AuditResource` is syntactically the same as `SignalResource`. Whereas `XJMF/SignalResource` MAY convey the momentary consumption or production of a resource, `AuditResource` conveys the consumption or production of a resource during an entire phase. A Device SHALL write a copy of the last `SignalResource` that it emits during an `AuditProcessRun` as an `AuditResource`.

### Table 3.8: AuditResource Element

| Name | Data Type | Description |
|---|---|---|
| `Header` | `element` | See `Message/Header`. |
| `ResourceInfo` | `element` | `ResourceInfo` describes the consumption or production of an individual `Resource`. `ResourceInfo/ResourceSet/Resource` elements NEED NOT contain the explicit resources as defined in `ResourceInfo/ResourceSet/@Name`. |
| `<foreign namespace elements>`* | `element` | See `Message` — foreign namespace elements. |

### Example 3.3: AuditResource — Logging of Consumption

The following example describes the logging of a modification of the media weight and amount. The XJDF document before modification requests 400 copies of 80 gram media. The XJDF after modification specifies that 421 copies of 90-gram media have been consumed.

```xml
<XJDF xmlns="http://www.CIP4.org/JDFSchema_2_0" JobID="PaperAudit" Types="ConventionalPrinting">
  <AuditPool>
    <AuditCreated>
      <Header AgentName="Writer" AgentVersion="V_2.0" DeviceID="TestSender" Time="2020-03-01T19:55:57+01:00"/>
    </AuditCreated>
    <AuditResource>
      <Header AgentName="Writer" AgentVersion="V_2.0" DeviceID="TestSender" Time="2020-03-01T19:55:57+01:00"/>
      <ResourceInfo>
        <ResourceSet Name="Component" Usage="Input">
          <Resource>
            <AmountPool>
              <PartAmount Amount="400" Waste="21"/>
            </AmountPool>
            <Part SheetName="S1"/>
            <Component/>
          </Resource>
        </ResourceSet>
      </ResourceInfo>
    </AuditResource>
    <AuditResource>
      <Header AgentName="Writer" AgentVersion="V_2.0" DeviceID="TestSender" Time="2020-03-01T19:55:57+01:00"/>
      <ResourceInfo>
        <ResourceSet Name="Media">
          <Resource>
            <Media MediaType="Paper" Weight="90"/>
          </Resource>
        </ResourceSet>
      </ResourceInfo>
    </AuditResource>
  </AuditPool>
  <ResourceSet Name="Media">
    <Resource ID="r_000007">
      <Part SheetName="S1"/>
      <Media MediaType="Paper" Weight="80"/>
    </Resource>
  </ResourceSet>
  <ResourceSet Name="Component" Usage="Input">
    <Resource>
      <AmountPool>
        <PartAmount Amount="400"/>
      </AmountPool>
      <Part SheetName="S1"/>
      <Component MediaRef="r_000007"/>
    </Resource>
  </ResourceSet>
</XJDF>
```

## 3.2.5 AuditStatus

`AuditStatus` contains audit information about the start and end times of any process states and sub-states, denoted as phases. Phases can reflect any arbitrary subdivisions of a process, such as maintenance, washing, plate changing, failures and breaks. `AuditStatus` elements SHOULD be written for every significant status change that is detected. `AuditStatus` is syntactically the same as `SignalStatus`. Whereas `XJMF/SignalStatus` conveys the momentary status of a Device and or job, `AuditStatus` conveys the status during an entire phase. A Device SHALL NOT write new `AuditStatus` elements for every `SignalStatus` XJMF that it emits.

`AuditStatus` elements MAY also be used to log the actual time spans when Resources are used by a process. For example, the temporary usage of a fork lift can be logged if an `AuditStatus` element is added that contains an `AuditStatus/Header/@DeviceID` of the fork lift and specifies the actual start and end time of the usage of that fork lift.

### Table 3.9: AuditStatus Element

| Name | Data Type | Description |
|---|---|---|
| `Header` | `element` | See `Message/Header`. |
| `DeviceInfo` | `element` | `DeviceInfo` describes details of the actual Device status. |
| `<foreign namespace elements>`* | `element` | See `Message` — foreign namespace elements. |

## 3.3 ProductList

The products or set of products that are processed during a given workstep MAY be specified in a `ProductList`, which describes a bill of materials. `ProductList` specifies a list of products and product parts from the print buyer’s point of view.

Multiple end products MAY be specified, e.g. when a press sheet of a Gang job that contains multiple individual customer jobs is printed. Unless the XJDF describes a Gang job, not more than one `Product[@IsRoot="true"]` SHOULD be specified.

For more details on Product Intent, see **Chapter 4 Product Intent**.

### Table 3.10: ProductList Element

| Name | Data Type | Description |
|---|---|---|
| `Product`+ | `element` | Each `Product` element in this list represents a product or part of a product with unique properties such as substrate, colors or size. |

## 3.3.1 Product

The `Product` element specifies an individual product or product part.

### Table 3.11: Product Element — Sheet 1 of 2

| Name | Data Type | Description |
|---|---|---|
| `Amount`? | `integer` | Total number of products or product parts of this type to produce. For product parts, if present `@Amount` SHALL specify the number of copies of the product part that are needed in order to produce one product. |
| `CommentURL`? | `URL` | URL to an external, human-readable description of the product or product part. |
| `DescriptiveName`? | `string` | Human-readable descriptive name of the product or product part. |
| `ExternalID`? | `NMTOKEN` | Identifier of the product in an MIS.<br><br>**Note:** The granularity with which an MIS defines individual products is system dependent. It will typically not include the artwork unless it applies to pre-printed products that can be reordered from stock. |
| `ID`? | `ID` | Internal identifier of this product. |
| `IsRoot`? | `boolean` | If true, this `Product` is a self-contained product. If false, this `Product` is a product part of another `Product`, such as a cover or insert. Multiple `Product` elements with `@IsRoot="true"` MAY be specified, for instance in a Gang job. If the parent `ProductList` element contains multiple `Product` elements, `@IsRoot` SHOULD be specified in all root products. |
| `MaxAmount`? | `integer` | Maximum total number of products to produce including the maximum overage that the customer is willing to accept. `@MaxAmount` SHOULD NOT be specified for product parts. |
| `MinAmount`? | `integer` | Minimum total number of products to produce including the maximum underage that the customer is willing to accept. `@MinAmount` SHOULD NOT be specified for product parts. |

### Table 3.11: Product Element — Sheet 2 of 2

| Name | Data Type | Description |
|---|---|---|
| `PartVersion`? | `NMTOKEN` | Version identifier — for example, the language version of a catalog. See also `Part/@PartVersion`. If `@PartVersion` is specified for a child product, the root products that reference the child products SHALL also contain `@PartVersion` with the same value.<br><br>**New in XJDF 2.1.** |
| `ProductType`? | `NMTOKEN` | Classification of this product or product part.<br><br>Values include those from: **Product Types**. |
| `ProductTypeDetails`? | `string` | `@ProductTypeDetails` specifies additional details of the product or product part that MAY be site specific and MAY be human readable. `@ProductType` SHOULD be present if `@ProductTypeDetails` is specified. |
| `Comment`* | `element` | Any human-readable text that relates to the product or product part. |
| `GeneralID`* | `element` | Additional identifiers related to the product or product part. |
| `Intent`* | `element` | Container elements for intents. |

### 3.3.1.1 Product Amount

`Product/@Amount` SHALL be applied within the context of one parent product. If `Product/@IsRoot="true"` then `Product/@Amount` SHALL specify the total number of products. If `Product/@IsRoot="false"` then `Product/@Amount` SHALL specify the total number of the respective child products required to create one parent product.

The following example shows the simplified description of 10 notebooks with a front and back cover and a 50 page book block.

### Example 3.4: Amounts in a Notebook

```xml
<ProductList>
  <Product Amount="10" IsRoot="true" ProductType="Notebook">
    <Intent Name="BindingIntent">
      <BindingIntent BindingSide="Top" BindingType="EdgeGluing" ChildRefs="IBack IBody ICover"/>
    </Intent>
  </Product>
  <Product Amount="1" ID="ICover" IsRoot="false" ProductType="FrontCover"/>
  <Product Amount="50" ID="IBody" IsRoot="false" ProductType="BookBlock"/>
  <Product Amount="1" ID="IBack" IsRoot="false" ProductType="BackCover"/>
</ProductList>
```

### 3.3.1.2 Product Amount for Variable Data

If a `Product` contains a `VariableIntent`, then `Product/@Amount` SHALL refer to the number of Instance Documents, also referred to as recipients or records.

The following example describes a variable job with two finishing options. The entire job has 10000 records of which 9000 are brochures and 1000 are hardcover books. Each brochure and each book has one cover and one body.

### Example 3.5: Amounts in Variable Data

```xml
<ProductList>
  <Product Amount="10000" IsRoot="true">
    <Intent Name="VariableIntent">
      <VariableIntent ChildRefs="IDBrochure IDBook" VariableType="Area"/>
    </Intent>
  </Product>
  <Product Amount="1000" ID="IDBook" IsRoot="false" ProductType="Book">
    <Intent Name="BindingIntent">
      <BindingIntent BindingType="HardCover" ChildRefs="IDBookCover IDBody"/>
    </Intent>
  </Product>
  <Product Amount="1" ID="IDBookCover" IsRoot="false" ProductType="Cover"/>
  <Product Amount="1" ID="IDBody" IsRoot="false"/>
  <Product Amount="9000" ID="IDBrochure" IsRoot="false">
    <Intent Name="BindingIntent">
      <BindingIntent BindingType="SaddleStitch" ChildRefs="IDBrochureCover IDBody"/>
    </Intent>
  </Product>
  <Product Amount="1" ID="IDBrochureCover" IsRoot="false" ProductType="Cover"/>
</ProductList>
```

## 3.4 ResourceSet

A `ResourceSet` describes a set of one or more `Resource` elements that are logically grouped together. A `ResourceSet` can describe either physical entities such as paper or logical entities such as process parameters. `ResourceSet` elements with the same values of `@Name`, `@Usage`, `@ProcessUsage` and common or no entries in `@CombinedProcessIndex` SHALL NOT be specified.

**Note:** This restriction is designed to ensure that the applicable `ResourceSet` for a process can be unambiguously identified. For instance a `ResourceSet[@Name="NodeInfo"]` MAY be defined for end customer scheduling requirements by specifying `ResourceSet/@ProcessUsage="EndCustomer"` and partitioning by `@Product`. Then the production scheduling SHOULD be defined in a separate `ResourceSet[@Name="NodeInfo"]` without `@ProcessUsage`.

An individual `Resource` SHALL be referenced by referencing `Resource/@ID`. The `ResourceSet` SHALL be referenced by referencing `ResourceSet/@ID`. Unless otherwise specified, an `@IDREF` or `@IDREFS` will refer to an individual `Resource` rather than an entire `ResourceSet`.

In some cases the partitioning structure of a `ResourceSet` is not explicitly required because the `Resource` elements are individually referenced by ID from other elements. In this case the `Part` elements NEED NOT be specified, even if there are multiple `Resource` elements in one `ResourceSet`.

### Table 3.12: ResourceSet Element — Sheet 1 of 2

| Name | Data Type | Description |
|---|---|---|
| `CombinedProcessIndex`? | `IntegerList` | `@CombinedProcessIndex` specifies the zero based indices of individual processes within the complete list of `XJDF/@Types` that this `ResourceSet` SHALL apply to. Multiple entries in `@CombinedProcessIndex` specify that the `ResourceSet` is used by the respective multiple processes.<br><br>`@CombinedProcessIndex` SHALL be specified if multiple `ResourceSet` items with the same `@Name`, `@ProcessUsage` and `@Usage` are specified in one XJDF. If `@CombinedProcessIndex` is not specified, the `ResourceSet` applies to all processes that match the `@Name`, `@ProcessUsage` and `@Usage` requirements as listed in **Chapter 5 Processes**. |
| `CommentURL`? | `URL` | URL to an external, human-readable description of the `ResourceSet`. |
| `DescriptiveName`? | `string` | Human-readable descriptive name of the `ResourceSet`. |
| `ID`? | `ID` | Identifier of the `ResourceSet`. `@ID` SHOULD be specified if the `ResourceSet` is a direct child of `XJDF` and `@Usage` is not specified and `Resource/@ID` is not specified in the child resources and `@CombinedProcessIndex` does not specify an exchange `ResourceSet`. |

### Table 3.12: ResourceSet Element — Sheet 2 of 2

| Name | Data Type | Description |
|---|---|---|
| `Name` | `NMTOKEN` | `@Name` SHALL specify the name of the explicit resource that this `ResourceSet` represents. Child resource elements of this `ResourceSet` SHALL NOT contain resources that do not match `@Name`. `@Name` of resource types that are specified in **Chapter 6 Resources** of this specification SHALL be provided without an XML namespace prefix. `@Name` of proprietary resources SHALL be provided with an XML namespace prefix. See Section 3.5 **XJDF Extensibility** for details.<br><br>A list of predefined resources is specified in **Chapter 6 Resources**. |
| `ProcessUsage`? | `NMTOKEN` | `@ProcessUsage` identifies the context of a `Resource` if multiple `Resource` elements of the same type are supplied for an individual process type.<br><br>Values include those specified in the appropriate process descriptions in **Chapter 5 Processes**.<br><br>**Note:** ICS documents MAY define additional values for `@ProcessUsage`. |
| `Unit`? | `NMTOKEN` | Unit of measurement for the values of `AmountPool/PartAmount/@Amount`, `AmountPool/PartAmount/@MaxAmount`, `AmountPool/PartAmount/@MinAmount` and `AmountPool/PartAmount/@Waste`.<br><br>Values include those from: **Units**.<br><br>**Note:** Units other than those defined in the above table SHOULD NOT be specified. |
| `Usage`? | `enumeration` | `@Usage` shows that the resource is either consumed or produced within this XJDF document.<br><br>If no `@Usage` is specified and the `ResourceSet` is a direct child of `XJDF` and the value of `@CombinedProcessIndex` does not specify an exchange `ResourceSet`, then the `ResourceSet` or its `Resource` children SHOULD contain `@ID` and be referenced from elsewhere within the XJDF. See Section 5.2.1 **Exchange ResourceSets in combined processes**.<br><br>Allowed value is from: **Usage**.<br><br>**Note:** `ResourceSet[@Usage="Output"]` MAY contain data that is conceptually input data for the XJDF. |
| `Comment`* | `element` | Any human-readable text that relates to the `ResourceSet`. |
| `Dependent`* | `element` | Reference to an XJDF that produces this `ResourceSet[@Usage="Input"]` or consumes this `ResourceSet[@Usage="Output"]`. Multiple `Dependent` elements specify that the `Dependent` relates to multiple consuming or producing processes. |
| `GeneralID`* | `element` | Additional identifiers related to the `ResourceSet`. |
| `Resource`* | `element` | List of `Resource` elements. |

### Example 3.6: ResourceSet with CombinedProcessIndex

The following example shows the use of `ResourceSet/@CombinedProcessIndex` to differentiate the scheduling for a finishing combined process that contains both Cutting and Folding. The `NodeInfo` with `@CombinedProcessIndex="0"` applies to the first token in `XJDF/@Types`, i.e. Cutting, whereas the `NodeInfo` with `@CombinedProcessIndex="1"` applies to the second token in `XJDF/@Types`, i.e. Folding. Since `CuttingParams` is uniquely linked to the Cutting process, and similarly `FoldingParams` is uniquely linked to the Folding process — see Cutting and Folding input resources — `@CombinedProcessIndex` NEED NOT be specified for those resources.

```xml
<XJDF xmlns="http://www.CIP4.org/JDFSchema_2_0" JobID="CPI_Example" Types="Cutting Folding">
  <ResourceSet CombinedProcessIndex="0" Name="NodeInfo" Usage="Input">
    <Resource>
      <NodeInfo Start="2020-03-01T13:00:00+01:00"/>
    </Resource>
  </ResourceSet>
  <ResourceSet CombinedProcessIndex="1" Name="NodeInfo" Usage="Input">
    <Resource>
      <NodeInfo Start="2020-03-01T17:00:00+01:00"/>
    </Resource>
  </ResourceSet>
  <ResourceSet Name="CuttingParams" Usage="Input">
    <Resource/>
  </ResourceSet>
  <ResourceSet Name="FoldingParams" Usage="Input">
    <Resource/>
  </ResourceSet>
</XJDF>
```

## 3.4.1 Dependent

A `Dependent` element SHALL reference an XJDF that produces an input `ResourceSet` or consumes an output `ResourceSet`.

The data in `Dependent` elements allows Devices to communicate directly with other Devices in the workflow that are processing the same job. The data provided in `Dependent` also provides pipe control information. See Overlapping Processing — omitted chapter “Building a System” — and Section 7.11 **PipeControl**.

### Table 3.13: Dependent Element

| Name | Data Type | Description |
|---|---|---|
| `JobID` | `NMTOKEN` | `@JobID` of the referenced process.<br><br>**Note:** `@JobID` will typically match `XJDF/@JobID` unless parts of the job are being produced on Gang forms. |
| `JobPartID`? | `NMTOKEN` | `@JobPartID` of the referenced process. `@JobPartID` SHALL NOT match `XJDF/@JobPartID`. |
| `PipeID`? | `NMTOKEN` | If this attribute exists, the resource is a pipe. `@PipeID` is used by XJMF pipe-control messages to identify the pipe. For more information, see Overlapping Processing — omitted chapter “Building a System.” |
| `PipeProtocol`? | `NMTOKEN` | `@PipeProtocol` defines the protocol use for pipe handling. Proprietary pipe protocols MAY be specified in addition to those defined below but will not necessarily be inter-operable.<br><br>Values include:<br>`IdentificationField` — The pipe data is provided by barcodes that are defined in `IdentificationField` elements.<br>`XJMF` — XJMF based `PipeControl` messages. The sequence of pipe initialization is undefined. See next two values: `"XJMFPush"` and `"XJMFPull"`.<br>`XJMFPush` — XJMF based `PipeControl` protocol. The producing Device initiates the protocol.<br>`XJMFPull` — XJMF based `PipeControl` protocol. The consuming Device initiates the protocol.<br>`None` — No pipe support. |
| `XJMFURL`? | `URL` | URL of a processor that has knowledge of the referenced process. The processor at this URL MAY be queried for additional information using XJMF. |

## 3.5 XJDF Extensibility

The XJDF specification aims to support plug-and-play as much as possible. Nonetheless, XJDF is meant to be flexible and therefore useful to any vendor, as each vendor may have specific data to include in the XJDF files. However, foreign namespace extensions SHOULD NOT duplicate functionality of XJDF defined attributes and elements. This section describes how XJDF MAY be extended. XJDF extensibility SHALL be implemented using XML namespaces; see XMLNS.

### 3.5.1 Foreign Namespaces

Attributes in a foreign namespace MAY be added to any XJDF element.

Elements in a foreign namespace SHALL NOT be specified in any XJDF or XJMF element unless explicitly allowed in the element definition table. The children of these elements SHALL be ordered so that all elements in a foreign namespace follow all of the elements in the XJDF namespace.

### Example 3.7: Namespaces in XML

The example illustrates how private namespaces are declared and used to extend an existing XJDF `Media` element by adding a private attribute and a namespace declaration.

```xml
<Resource>
  <Part SheetName="S1"/>
  <Media MediaType="Paper" xmlns:foo="http://www.foo.org" foo:FooAtt="FooVal"/>
</Resource>
```

### 3.5.2 Creating Extension ResourceSets

New types of `ResourceSet` may be defined by creating a `ResourceSet` with `@Name` referring to a proprietary XML namespace. The extension element SHALL reside in the appropriate child `ResourceSet/Resource` element.

### Example 3.8: Creating Extension ResourceSets

```xml
<ResourceSet Name="foo:FooParams" Usage="Input" xmlns:foo="http://www.foo.org">
  <Resource>
    <Part Run="R1"/>
    <foo:FooParams FooAtt="FooVal"/>
  </Resource>
</ResourceSet>
```

### 3.5.3 Creating Extension Message Type Elements

New message types may be defined by creating a message in a proprietary XML namespace that adheres to the naming scheme of XJMF.

The extension message SHALL reside in the `XJMF` element. Extension messages SHOULD follow the naming scheme using Message Family and type and SHOULD contain a `Header` element. The following example shows a query and its matching response for a new message type in the `foo` namespace.

### Example 3.9: Creating Extension Messages

**Query**

```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0" xmlns:foo="www.foo.org">
  <Header DeviceID="TestSender" ID="l_000002" Time="2019-03-26T14:07:48.076+00:00"/>
  <foo:QueryBar>
    <foo:BarParams BarDetails="value"/>
    <Header DeviceID="TestSender" ID="queryID" Time="2019-03-26T14:07:48.077+00:00"/>
  </foo:QueryBar>
</XJMF>
```

**Response**

```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0" xmlns:foo="www.foo.org">
  <Header DeviceID="TestSender" ID="l_000002" Time="2019-03-26T14:07:48.017+00:00"/>
  <foo:ResponseBar>
    <foo:BarResonseParams BarDetails="value"/>
    <Header DeviceID="TestSender" ID="l_000003" Time="2019-03-26T14:07:48.017+00:00" refID="queryID"/>
  </foo:ResponseBar>
</XJMF>
```

### Example 3.10: Creating Mixed Extension Messages

The following example shows how XJMF messages can be mixed and interleaved with extension messages.

```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0" xmlns:foo="www.foo.org">
  <Header DeviceID="TestSender" ID="l_000002" Time="2019-03-26T14:07:49.120+00:00"/>
  <QueryKnownDevices>
    <Header DeviceID="TestSender" ID="Q1" Time="2019-03-26T14:07:49.120+00:00"/>
  </QueryKnownDevices>
  <foo:QueryBar>
    <Header DeviceID="TestSender" ID="F1" Time="2019-03-26T14:07:49.120+00:00"/>
    <foo:BarParams BarDetails="value"/>
  </foo:QueryBar>
  <QueryKnownMessages>
    <Header DeviceID="TestSender" ID="Q2" Time="2019-03-26T14:07:49.120+00:00"/>
  </QueryKnownMessages>
</XJMF>
```

### 3.5.4 Creating Extension Intent Elements

New intent elements may be defined by creating an intent with `@Name` referring to a proprietary XML namespace.

The extension element SHALL reside in the intent element.

### Example 3.11: Creating Extension Intent Elements

```xml
<Product IsRoot="true">
  <Intent Name="foo:FooIntent">
    <foo:FooIntent xmlns:foo="http://www.foo.org" FooAtt="FooVal"/>
  </Intent>
</Product>
```

### 3.5.5 Extending NMTOKEN Lists

Some elements contain attributes of type `NMTOKEN` and some of these have a set of predefined suggested values. These sets are open by design and MAY be extended with other values providing such additional values do not conflict with the usage of those already defined in this specification.

If an ICS requires new `NMTOKEN` values or a work group has agreed upon new recommended `NMTOKEN` values, these will be published at CIP4Names prior to being added to the specification.

Additional values MAY use a namespace like syntax — that is, a namespace prefix separated by a single colon `:` — in which case the namespace prefix SHOULD be defined in the XJDF ticket with the standard `xmlns:Prefix="someURI"` notation, even if no other use of that namespace occurs in the XJDF ticket. Implementations that find an unknown `NMTOKEN` that has a namespace prefix MAY then attempt to use its default value of that attribute.

For other `NMTOKEN` lists that have a pre-defined meaning or employ a specific syntax — for example `@Separation` or `@FoldCatalog` — additional values SHOULD NOT use the namespace prefix format but SHOULD conform to the usage for that data type, i.e. a new value for `@Separation` SHOULD be the name of a separation employed within the XJDF. Similarly, a new value of `@FoldCatalog` SHOULD conform to the normal `Fx-y` syntax. Implementations that find an unknown `NMTOKEN` without a namespace prefix MAY then raise an error.

### 3.5.6 Extending Process Types

XJDF defines a basic set of process types. However, because XJDF allows flexible encoding, this list, by definition, will not be complete. Vendors that have specific processes that do not fit in the general XJDF processes and that are not combinations of individual XJDF processes — see Section 3.1.3 **XJDF for Process Description and Gray Boxes** — can create process XJDF of their own type. Then the content of the `@Types` attribute MAY be specified with a prefix that identifies the organization. The prefix and name SHALL be separated by a single colon `:` as shown in the following example.

### Example 3.12: Extending Process Types

```xml
<XJDF xmlns="http://www.CIP4.org/JDFSchema_2_0" JobID="IntentExtension" Types="foo:FooMaking" xmlns:foo="http://www.foo.org">
</XJDF>
```

### 3.5.6.1 Rules about Process Extension

The use of namespace prefixes in the `@Types` attribute is for extensions only. Standard XJDF process types SHALL be specified without a prefix in `XJDF/@Types`. If a process is simply an extension of an existing process, it is possible to describe the private data by extending the existing resource types.