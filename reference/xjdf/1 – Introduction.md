# Chapter 1 Introduction

This document defines the technical specification for the Exchange Job Definition Format (XJDF) and its counterpart, the Exchange Job Messaging Format (XJMF).

XJDF is a technology that allows systems from many different vendors to interoperate in automated workflows. While technically it is a data exchange standard, it is more importantly a means to connect multiple vendor solutions to a workflow solution for automation.

XJDF 2.0 was the first major version update of JDF. It is such a major update that we decided to provide a new name for the XML root element: `XJDF`. Whereas the minor revisions were at least nominally backwards compatible, XJDF is a major re-design that takes more than a decade of experience into account. XJDF 2.2 is a minor update and is backwards compatible with XJDF 2.1.

> **Note:** The specification uses two forms for references to XJDF/XJMF (the general concept of the specification) and `XJDF`/`XJMF` (for specific reference to the root element of an XML instance).

This document is intended for use by programmers and systems integrators. It provides both the syntactical requirements for the elements and attributes of XJDF and XJMF as well as requirements for Devices and Controllers to act upon the data.

In this first chapter, we present the concept of XJDF, and its relationship to JDF and other industry standards.

## 1.1 Further Information

Additional information such as application notes and examples can be found on the CIP4 website at http://www.CIP4.org and the CIP4 technical website at http://confluence.CIP4.org.

### 1.1.1 NMTOKEN repository

Open lists are marked with a data type of `NMTOKEN` or `NMTOKENS` and contain a list of suggested values. The list of values may be incomplete and sometimes needs to be extended with new values without updating the specification, e.g. when a new domain ICS is developed.

Additional, suggested values are maintained in the CIP4 technical discussion area at http://confluence.CIP4.org. In order to avoid different extension values being used for the same purpose, vendors are encouraged to check this area prior to using new values. In the event that no existing extension exists then vendors are further encouraged to submit their extensions to CIP4 using the CIP4 issue tracking system at http://jira.CIP4.org.

### 1.1.2 Errata

Although great care has been taken to ensure that this specification is correct and complete, some errors cannot be avoided. CIP4 therefore maintains an online errata repository in its technical discussion area at http://confluence.CIP4.org. A copy of the original specification with annotations identifying the errata is also published and can be found at http://confluence.CIP4.org.

The corrections in the errata override the published specification.

## 1.2 Background on XJDF

XJDF is an extensible data interchange format built upon more than twenty years of experience with JDF.

XJDF is an interchange data format that can be used by a system of Controllers, Devices and MIS, which together produce printed products. It provides the means to describe print jobs in terms of the products eventually to be created, as well as in terms of the work steps needed to create those products. XJDF provides a syntax to explicitly specify the details of processes, which might be specific to the Devices that execute the processes.

XJDF is aligned with a communication format known as the Exchange Job Messaging Format or XJMF. XJMF provides the means for production components of an XJDF workflow to communicate with Controllers such as MIS. It gives MIS and other Controllers the ability to receive information from Devices or other Controllers about the status of jobs and Devices. XJDF and XJMF are maintained and developed by CIP4 (http://www.CIP4.org).

## 1.3 Design Criteria for XJDF

The major conceptual change is that XJDF no longer attempts to model the entire job as one large “job ticket” but rather specifies an interchange format between 2 applications that are assumed to have an internal data model that is not necessarily based on XJDF. Thus each XJDF ticket specifies a single transaction between two parties. A single job may be modeled as one or more XJDF transactions.

The following criteria were taken into account in this redesign:
* XJDF should be simple to use.
* The number of methods to describe similar Traits should be as limited as possible, ideally one.
* XJDF should be compatible with the latest XML tools to simplify development.
* Simple XPath expression to reference XJDF Traits.
* Direct use of ID-IDREF pairs for referencing distributed data within an XML document.
* Use of XML schema rather than proprietary data structures to describe Device capabilities.
* The semantics of JDF 1.x should be retained and mapping between JDF 1.x and XJDF should be simple.
* Change orders (Modifications of submitted jobs) should be easy to describe.

These requirements lead to some significant modifications that are not syntactically backwards compatible, but can easily be converted using JDF 1.x aware middleware.

The original requirements were defined assuming that XML is the underlying encoding. Use of JSON has the same requirements but focuses on standard JSON tooling rather than standard XML tooling. All references to XML, XML schema etc. apply appropriately to JSON, JSON-schema and similar technologies.

### 1.3.1 Simplify and reduce variations

JDF 1.x allowed shorthand for some simple cases. What seemed reasonable actually made things more complex, since both shorthand and the long version had to be implemented. For instance, amount related attributes could be found either directly in a `ResourceLink` or in `ResourceLink/AmountPool/PartAmount`. XJDF removes much of this variability.

#### 1.3.1.1 Reduce the barrier of entry
Simple tasks should be easy to describe. In such cases the XJDF should be capable of being described as a short list of simple XPaths.

#### 1.3.1.2 Single XJDF
JDF 1.x allowed for multiple ‘JDF’ nodes within one ticket. This grouping of multiple nodes in process groups resulted in many variations of JDF for the same or similar requirements. Version 2.x has exactly one `XJDF` element, namely the root element; this contains no `XJDF` child nodes. This means there can be no ambiguity about where to locate and retrieve a given Trait.

#### 1.3.1.3 Replace abstract data types with explicit elements and children
Abstract elements are more concise to write, but inherited Traits also tend to be overlooked by newcomers to a specification. If elements are designed to be final with sub-elements, each specification entry can be found by searching for the explicit element name.

#### 1.3.1.4 Remove ResourceLinks
XJDF allowed specification of interdependencies of processes using ‘ResourceLink’ elements. In most cases, this feature is not required if the Controller maintains an internal job model. Therefore XJDF does not provide mechanisms to describe process networks within a single XJDF.

The Process/Resource model has been conceptually retained. But since there is only one `XJDF` element per XJDF transaction, reuse of resources is no longer an issue and ‘ResourceLink’ elements have been merged with their respective resources. Thus data that belongs together is also stored in the same region of the XML.

#### 1.3.1.5 Remove RefElements
RefElements have been replaced with one of `IDREF`, `IDREFS` or inline element.

For each RefElement (i.e., choice of `ResourceRef` or inline element), exactly one choice was made. Thus the variability is reduced and implementation is simplified.

#### 1.3.1.6 Product Description
Product descriptions are now elements in their own right rather than a different type of ‘JDF’ element. Thus a modification of the underlying product structure no longer modifies the overall structure of the XJDF. This also allows description of Gang jobs where production relates to multiple products.

Intent Traits are now simple attributes rather than structured spans of ranges that allow negotiation between customer and print provider. This specification assumes that any negotiation between print provider and customer takes place dynamically out of scope of this specification.

Intents that were essentially 1 to 1 copies of the respective process resource such as `DeliveryIntent` or `PackingIntent` have been removed. If the data that was provided in these intents is required for a product, then the respective process resource, e.g. `DeliveryParams` should be provided.

#### 1.3.1.7 Imposition
JDF had three methods to describe imposed sheets: `LayoutPreparationParams` for digital printing, `StrippingParams` for MIS level imposition and `Layout` for low level RIP imposition. XJDF has removed `StrippingParams` and merged its properties with `Layout` which can now describe both MIS level descriptions and RIP level descriptions. Since digital printing is also moving to larger sheet sizes, `LayoutPreparationParams` have been replaced with automated `Layout`.

### 1.3.2 Enable dynamic changes

The monolithic model of JDF 1.x lent itself well to a plan and execute philosophy but had its limitations when changes were made after a job had been submitted. Since a job may be modeled as a set of transactions in XJDF, the idea of multiple transactions and thus also job changes is inherently built into the standard. The simplest method of initiating a change transaction is to send an XJDF that contains only the modified values. Only the explicitly stated values will then be modified.

#### 1.3.2.1 Remove schema defaults.
All schema defaults have been removed.

### 1.3.3 Retain the semantic structures

A lot of work was put into the definition of individual messages, processes and resources. The detailed semantics of JMF messages and resources have been retained. Thus detailed element and attribute names and their definitions have been retained. Thus translation between JDF 1.x and XJDF is straightforward. All deprecated Traits have been removed completely.

### 1.3.4 Remove implementation specific details

JDF 1.x exposes many implementation details that are not necessarily easily obtained by the writers of JDF. XJDF is designed as a pure interface specification that encapsulates internal data as much as possible.

#### 1.3.4.1 Spawning and Merging
Since XJDF is only an interface, the specification of serializing from the internal data model and deserializing to the internal data model is outside the scope of this specification and has been removed.

### 1.3.5 Enhance Compatibility with standard XML and XML Tools

XML and XML related tools and technologies such as XPath, XSL transforms, Schema, class generators etc. have evolved and matured significantly since the turn of the century. Some of the choices in JDF 1.x, although compliant with XML have proved difficult to implement using standard tools.

#### 1.3.5.1 Order of Child Elements
JDF 1.x allowed for arbitrary ordering of sibling elements. This is convenient for the writer, but degrades the quality of XML schema validation because cardinality cannot be correctly enforced for unordered elements. Therefore XJDF generally requires sibling elements to be provided in the order as specified in the element definitions. In general the order of elements is lexically sorted in ascending order. Exceptions to alphabetical sorting will be explicitly called out in the relevant sections.

> **Note:** Although XML is case sensitive, the ordering of elements will be determined ignoring the case of any capital letters.
> **Note:** Attributes NEED NOT be sorted within an element.

#### 1.3.5.2 Partitioning and Inheritance
The general concept of partitioning (i.e., the notion of resource sets with multiple individual parts) is retained but the encoding has been simplified. While inheritance of elements and attributes in partitioned resource sets can reduce data redundancy, it also greatly increases the flexibility and variability of specifying similar data. This causes potential for reader/writer mismatch. Inheritance and the corresponding definition of cardinality (e.g., “SHALL occur somewhere in the inherited hierarchy”) is also difficult to encode as XPath or in an XML schema. XJDF therefore removes inheritance at the cost of redundant specification of Traits in partitioned resources.

##### 1.3.5.2.1 Removal of Partition SignatureName
`@SignatureName` in JDF was used to describe a set of multiple printed sheets, which is contrary to the usage of signature in traditional printing. Since most systems refer directly to sheets, the `@SignatureName` Partition Key was removed.

### 1.3.6 Device Capabilities

JDF provided proprietary methods to describe Device limitations. XML schema is a standard technology that is also designed for this purpose albeit with some limitations such as the lack of a mechanism to describe constraints dependencies. Nonetheless we decided to define Device limitations using XML schema in order to make use of the existing tool base for XML schema.

### 1.3.7 Compatibility with JDF and prior versions of XJDF

Backwards compatibility within versions that belong to a major release is a design goal for all CIP4 standards. XJDF was a major revision and therefore is aligned with JDF but is not backwards compatible with JDF.

This version of the specification is designed to be backwards compatible with previous minor releases of XJDF 2.0. Therefore any valid XJDF 2.0 or XJDF 2.1 document will also be a valid document for this version of the specification. The namespace URI for XJDF remains the same as for XJDF 2.0: `"http://www.CIP4.org/JDFSchema_2_0"`.

> **Note:** It is anticipated that the value of the XJDF namespace will remain constant for all future versions of XJDF 2.0. This reflects the backwards compatibility or major versions of XJDF.

The version of an XJDF or XJMF document SHOULD be specified in `XJDF/@Version` or `XJMF/@Version`.

The JDF and XJDF specifications are developed and released in tandem, as such JDF 1.8 matches XJDF 2.2 in terms of functional detail.

## 1.4 Encoding Methods

The original XJDF 2.0 and 2.1 specifications were based on XML as the sole underlying data encoding method. In the early 2000s, XML was the dominant standard for data exchange in the Internet. The role of XML is being challenged by JSON and many modern interfaces use JSON as the underlying grammar. See [JSON]. Therefore JSON has been added as a secondary encoding method in XJDF 2.2.

### 1.4.1 Use of XML

The original and preferred encoding for XJDF is XML. XML-encoded XJDF SHALL be a valid XML document according to [XML].

> **Note:** Most data in XJDF is encoded in XML attributes; XML elements provide the hierarchical structure of the data.

#### 1.4.1.1 Use of XML Namespaces
XML-encoded XJDF requires the use of XML namespaces. For details on using namespaces in XML, see [XMLNS]. The namespace for this version of XJDF is `"http://www.CIP4.org/JDFSchema_2_0"` and SHALL be declared and SHOULD use either the default namespace or a prefix of `'xjdf'`.

In a number of places XJDF allows for the use of items from a foreign namespace. If the instance contains such items then the foreign namespace SHALL be declared.

#### 1.4.1.2 Use of XML Schema
The XML schema for XJDF is designed to ensure that XJDF documents are syntactically valid, thus XJDF documents that are successfully validated against the XJDF schema SHALL be considered conformant to the syntax requirements described in this specification.

#### 1.4.1.3 Schema and XJDF Context
CIP4 anticipates the uses of XJDF in three broad contexts:
* Original job instruction
* Change order
* Device capabilities

For original job instructions, this specification defines mandatory content that SHALL be present in the XJDF document. As change orders can only be used to alter an existing job, mandatory content will have been delivered to the executing Device by the original job instruction, and the change order does not need to convey this same data again. In fact, the XJDF document being used for a change order SHOULD only describe those values that have changed.

> **Note:** Sending only modified values very much simplifies the executing Device's task of identifying and implementing the required changes.

CIP4 provides two XML schema definitions for use with XJDF depending upon which context the XJDF document is being used in. The schema for original job instruction validates an XML document ensuring all cardinality requirements are met and can be considered to be a more rigid implementation. For change orders, most attributes and elements have been made optional in the schema which thus allows XML documents with minimum structure to be used to convey simple alterations to the consuming Device. Both schemas are defined for the XJDF namespace `http://www.CIP4.org/JDFSchema_2_0`.

For convenience the latest schema implementations can be found online at [XJDF Schema Repository]. Conforming XML documents NEED NOT use this in an `xsi:schemaLocation` attribute.

### 1.4.2 Use of JSON

*New in XJDF 2.2*

The main value of CIP4 standards is the well-defined specification of print products, processes, messages and resources. These definitions are independent of the underlying grammar and therefore can be represented either as XML, JSON or any other underlying format that is aware of hierarchical key-value maps.

Therefore a standard syntax mapping of XML to JSON and vice versa is provided by CIP4. The mapping is bidirectional and allows for simple and reasonably generic conversion between JSON and XML and vice versa. In some cases, special mapping was introduced to enhance the readability and usefulness for automated code generation of the resulting JSON code.

See Section 9.10 Use of JSON and REST APIs for more details.

## 1.5 Conceptual Changes from JDF to XJDF

This section details significant structural and conceptual changes between JDF and XJDF.

### 1.5.1 Use of Abstract Elements
The concept of abstract element types has been largely replaced by explicit element definitions. Specific details are provided in the relevant subsections in Chapter 3 Structure and Chapter 7 Messaging.

### 1.5.2 Resource Partitioning
Resource partitioning has been completely revised. Inheritance of abstract resource elements has been replaced by lists of resource elements within a `ResourceSet`.

### 1.5.3 Structural Changes
XJDF is no longer nested, there is exactly one `XJDF` element in an XJDF ticket. Multiple XJDF each with a different `@JobPartId` MAY be sent to a Controller to specify multiple individual tasks.

In JDF terms, an XJDF is a Gray Box that is to be processed by a Device. There are no Gray Box expansion requirements to allow a Gray Box to be processed by lower level Devices.

Resources have been split into two classes. Product Intent elements are specified within their respective product elements. All other resource classes from JDF 1.x have been combined into the `ResourceSet/Resource` group. All generic attributes and elements SHALL be specified in the Product Intent or resource element, whereas specific attributes and elements SHALL be specified in a corresponding Product Intent or specific resource as specified in Chapter 4 Product Intent or Chapter 6 Resources, respectively.

Partitioning has been limited syntactically to exactly one level. Zero or more `Part` elements specify the part usage, and each `Part` element MAY still contain multiple partition attributes. Multiple `Part` elements replace the `Identical` element.

Product descriptions are now specified as a `ProductList` subelement of the `XJDF`. This allows informative specification of one or more products for any process, without requiring the process to be a descendent of the respective product.

### 1.5.4 Process Model Changes
The concept of test running has been removed. Section 7.6 KnownDevices SHOULD be used to query the abilities of a Device. Capabilities are described as XML schema, see Section 9.9 Use of XML Schema for Capability Descriptions.

Whereas JDF 1.x supported explicit encoding of process networks, XJDF assumes that the network is implemented in a proprietary fashion by the Controller. Each individual XJDF therefore pertains only to the receiving Device. Nonetheless, this version of XJDF allows for a number of execution models as detailed in Section 9.3 Execution Model.

### 1.5.5 Alignment of Signals and Audits
In JDF, audits and signals were conceptually paired but syntactically slightly different. XJDF aligns the signals that are relevant for job costing with their respective audits. The data is syntactically identical whether it is contained in an audit or a signal, e.g. `AuditStatus` and `SignalStatus`.

### 1.5.6 Messaging Changes
The root element of the message package has been renamed from `JMF` to `XJMF`. This allows immediate identification of XJMF and aligns closely with XJDF.

#### 1.5.6.1 Removal of Redundant Message Families
Two JMF families have been removed:
1. **Registrations**, i.e. the request to the recipient of the registration to send command messages to a command recipient that is specified in a subscription. Registrations have been replaced by command elements with embedded subscriptions. This follows the same model as query elements with embedded subscriptions.
2. **Acknowledges**, i.e. asynchronous responses. The only valid asynchronous response is a signal that may be subscribed to.

> **Note:** This does require all queue submissions to be handled synchronously, but this has also been the case in JMF, where the `Command/@AcknowledgeURL` could be omitted, thus forcing the recipient to handle the message synchronously.

#### 1.5.6.2 Type Safe Message Elements
`Message/@Type` has been replaced by an explicit message element that is structured as the combination of Message Family and type. For instance:

**Example 1.1: Message Type vs Explicit Message Element**

A JMF known Devices query:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<JMF AgentName="CIP4 JDF Writer Java" AgentVersion="1.5 BLD 93" MaxVersion="1.6" SenderID="SenderID" TimeStamp="2017-05-06T16:49:46+02:00" Version="1.6" xmlns="http://www.CIP4.org/JDFSchema_1_1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="JMFRootMessage">
  <!--Generated by the CIP4 Java open source JDF Library version: CIP4 JDF Writer Java 1.5 BLD 93-->
  <Query ID="m.1831._170506_164946177_000002" Type="KnownDevices" xsi:type="QueryKnownDevices">
    <DeviceFilter DeviceDetails="Brief"/>
  </Query>
</JMF>
```

Is now encoded in XJMF as:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--XJDF converter version: using: CIP4 JDF Writer Java 1.6 BLD 009-->
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
  <Header AgentName="CIP4 JDF Writer Java" AgentVersion="1.6 BLD 009" DeviceID="TestSender" ID="l_001007" Time="2019-03-26T14:14:32+00:00"/>
  <QueryKnownDevices>
    <Header DeviceID="dummy" ID="m.1831._000002" Time="2019-03-26T14:14:32+00:00"/>
    <DeviceFilter DeviceDetails="Brief"/>
  </QueryKnownDevices>
</XJMF>
```

This naming element structure allows a much cleaner XML schema definition and specification of the respective message elements.

#### 1.5.6.3 Combining Queue and Pipe Control Messages
Individual `QueueEntry` modification messages have been combined into a single `ModifyQueueEntry` message.
All pipe control messages have been combined into a single `PipeControl` message.

## 1.6 Conventions Used in this Specification

This section contains conventions and notations used within this document.

### 1.6.1 Document References
Throughout this specification, references to other documents are indicated by short symbolic names inside square brackets (e.g., [ICC.1]). Appendix G References lists all such references, with their full title, date, source and availability.

### 1.6.2 Text Styles
There are a number of text styles that are used to identify the various components of the specification. Some of the text styles support dynamic links; these allow the reader to click on the term and navigate to the definition of the term (if it is locally defined).

* **NodeInfo**: An XJDF or XJMF element. Usually these are dynamic links leading to the definition of the element.
* **Process**: A specific process such as `ColorSpaceConversion` or `Rendering`. These can be dynamic links leading to the definition of the process.
* **@Attribute**: An XJDF or XJMF attribute within the context of an element.
* **"Value"**: The content of an attribute.
* **XJDF**: XJDF or XJMF are used when referring to the specification in general rather than elements with the same name.
* **New in XJDF 2.2**: Highlights a change and the version it was introduced. See Section 1.6.4 Modification Notes. It may also be used to highlight a difference due to the use of JSON encoding.
* **Glossary Item**: The document utilizes some specialist terms; these are defined in Table 1.4 Glossary and highlighted throughout the document.
* **[CIP4Names]**: Identifies a reference to an item within this specification (such as a particular table, section etc) or to an entry in the references appendix. These are dynamic links leading to the item itself.
* **http://www.CIP4.org**: A hyperlink reference to an external item.

### 1.6.3 XPath Notation
* `Media/@MediaType`: The document utilizes [XPath] notation when it is required to define the particular context for an item. It is particularly useful when there is a conditional term relating to the context, e.g. `Media[@MediaType="Paper"]` identifies unprinted paper media resource.

### 1.6.4 Modification Notes
*New in XJDF 2.1*

To help the reader familiar with earlier versions of XJDF, this specification indicates additions, deprecations and clarifications using the callouts described in Table 1.1 Modification Notes. Please note that not all changes are identified with modified callout flags. When modification occurs in multiple versions, sometimes only the most recent version is indicated. A few changes have been made globally and are explained in the body of the document and only significant changes have been flagged with callouts, as determined by CIP4 Working Groups.

#### 1.6.4.1 Location of Modification Notes
A callout occurs after one of the following document elements.
* **Section head**: applies to entire section and all subsections and contained tables (if any).
* **Attribute/Element name**: applies to entire row for the designated attribute/element.
* **Attribute value**: applies to attribute value.

**Table 1.1: Modification Notes**

| CALLOUT | MEANING |
| --- | --- |
| **New in XJDF 2.x** | New sections, attributes/elements and attribute values. |
| **Deprecated in XJDF 2.x** | Deprecated sections, attributes/elements and attribute values. Usually there is a deprecation note describing the mechanism that replaces the deprecated item. |
| **Modified in XJDF 2.x** | Changed syntax or semantics of sections or attributes/elements. Might include clarification as well. Usually there is a modification note describing the change. |
| **JSON Exception** | Highlights the changed syntax or semantics required if the XJDF or XJMF is encoded using JSON. There will be an exception note describing the change. |

### 1.6.5 Specification of Cardinality

The cardinality of XJDF attributes and elements is expressed using the notations described in Table 1.2 Cardinality Symbols.

The cardinality for XJDF and any child elements applies to original job instruction XJDF documents that are submitted to a Device. In case of change orders, i.e. XJDF that is referenced by a `CommandResubmitQueueEntry`, the cardinality restrictions are loosened and all elements and attributes that are not required to identify the context of the change order become optional.

> **Note:** The XML schema for change orders is designed to reflect this loosened state.

The symbol `T` in the table below represents an attribute or element. The symbol `T` consists of either a single name, such as “RunList” or an element name followed by a parenthesized name, such as “RunList(Document)”. The name in parentheses "Document" identifies a particular element instance when several of the same type exist in some context. For further details, see Section 5.1 Process Template and Section 1.6.6 Template for Tables that Describe Elements.

**Table 1.2: Cardinality Symbols**

| NOTATION | DESCRIPTION |
| --- | --- |
| `T` | T SHALL occur exactly once and represents an attribute or element. |
| `T?` | T MAY occur zero or once, and represents an attribute or element. The description field MAY explain some circumstances that if met SHALL result in T occurring exactly once. |
| `T+` | T occurs one or more times, and represents an element. |
| `T*` | T occurs zero or more times, and represents an element. |

### 1.6.6 Template for Tables that Describe Elements

Elements are defined by their attributes and sub-elements.
The ordering of the elements in the tables defines the order in which the elements SHALL appear in the respective elements.

**Table 1.3: Template for Element Descriptions**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Attribute-Name`<br>Cardinality | `Attribute-data-type` | Information about the attribute. |
| `Element-Name`<br>Cardinality | `element` | Information about the element. |
| `FileSpec`<br>`(ResourceUsage)`<br>Cardinality | `element` | Information about the FileSpec element.<br>If "ResourceUsage" is specified, `FileSpec/@ResourceUsage` SHALL match the value specified in the parentheses.<br>*Note: When an element potentially contains multiple FileSpec children, the value of `FileSpec/@ResourceUsage` is used to distinguish them.* |

## 1.7 Changes from the Previous Version

This section summarizes the major changes from the previous version of XJDF. Details and additional minor changes are listed in Appendix H Release Notes.

### 1.7.1 Additions
* JSON has been added as a secondary encoding in Section 9.10 Use of JSON and REST APIs.
* This includes a set of recommended REST endpoints that are defined in Section 9.10.3 REST API Endpoints.
* A `SheetOptimizingReport` resource has been defined to specify the results of ganging individual jobs.
* The ability to perform black point compensation has been added to `ColorSpaceConversionOp`.

### 1.7.2 Removals
There are no significant removals.

### 1.7.3 Modifications
* The use of `Media` for non-printing purposes has been reduced and replaced with either `Tool` or `MiscConsumable`.
* The version has been updated to 2.2.

## 1.8 Glossary

The following terms are defined as they are used throughout this specification. For more detail on job and workflow components, see Section 2.3 System Components.

**Table 1.4: Glossary**

| TERM | DEFINITION |
| --- | --- |
| **Attribute** | An XML syntactic construct describing an unstructured characteristic of an Element. See [XML] for details. |
| **Controller** | The component of an XJDF based workflow that initiates Devices, routes XJDF, and communicates status information. A MIS is an example of a top level Controller. |
| **Deprecated** | Indicates that an XJDF feature is being phased out of XJDF. Controllers and Devices SHOULD NOT write XJDF that contains deprecated features. |
| **Device** | The component of an XJDF workflow part that interprets XJDF and executes the instructions. If a Device controls a Machine, it does so in a proprietary manner. For details, see Section 2.3.1.2 Device about Devices in workflow components. |
| **Document Set** | A set of Instance Documents presumed to be related. |
| **Element** | An XML-based syntactic construct describing structured data in XJDF. |
| **Final Product** | The product that was ordered by the customer. |
| **Finished Page** | A page of a Final Product that normally has no folds inside. The folds of the finished product for packaging (e.g., folding letters into an envelope), or Z-fold of an oversized book, have no effect on the Finished Page definition. |
| **Gang** | Gang (also known as ‘Gang Run’ or ‘Combination Run’) is a term for printing multiple jobs in the same production run on a printing press. By grouping similar jobs from multiple customers on the same press sheet, the cost of each job is substantially reduced. |
| **Gray Box** | A Gray Box is an incomplete specification of a process. ResourceSets that are required for actual production MAY but NEED NOT be complete. Gray Boxes are typically specified by an MIS. |
| **Heartbeat** | A signal that is sent in regular intervals and that is not caused by a state change in the Device. |
| **Input Resource** | A Resource is an input to a Process. |
| **Instance Document** | A document record that is part of the output of a variable data job. For example, in a credit card statement run, each statement is an Instance Document. |
| **Intent** | An Intent is an element that defines the details of products to be produced without defining the Process to produce them. Intent elements typically describe aspects of the end-customer view of a printed product. |
| **Job Part** | A granular task that is represented by a single XJDF. |
| **Machine** | The part of a Device that does not know XJDF and is controlled by an XJDF Device in a proprietary manner. |
| **Message Family** | A Message Family is a set of messages. The 4 Message Families are Query, Command, Response and Signal. |
| **MIS** | Management Information System. The functional part of an XJDF workflow that oversees all Processes and communication between system components and system control. MIS is assumed to be a role rather than an individual application. A single application may fulfill various roles of an MIS and various roles of an MIS may be implemented by multiple applications. Typical MIS roles include estimation, costing, scheduling, Process planning and invoicing. |
| **Output Resource** | A Resource that is an output from a Process. |
| **Partial Product** | The product is an intermediate product that will be combined with other Partial Products to create a Final Product. |
| **Partition** | Resource elements within a ResourceSet are partitions. |
| **Partition Key** | A Partition Key is an Attribute in `Resource/Part` that can identify a specific Resource within its parent ResourceSet. |
| **PDL** | Page Description Language. A generic term for any language that describes pages that might be printed. Examples are PDF®, PostScript® or PCL®. |
| **Process** | An individual step in the workflow. |
| **Product Intent** | Describes the end result that a customer is requesting. See Chapter 4 Product Intent. |
| **Quote** | The Quote is an offer to sell printed material, usually in response to an RFQ. The Quote contents commonly include precise specifications, pricing and terms. Upon acceptance by the print buyer the Quote may become a legally binding agreement. See [PrintTalk]. |
| **Reader Page** | A logical page as perceived by a reader. One Reader Page might span more than one Finished Page (e.g., a centerfold). One Finished Page might contain contents defined by multiple Reader Pages. |
| **Receiver** | Device or Controller that responds to an XJMF request. |
| **RFQ** | RFQ, Request For Quote, is a request for pricing that a print buyer sends to a print provider. See [PrintTalk]. |
| **Roll** | A Roll is media that is mainly used in connection with web printing. In British English the name “reel” for “roll” is in widespread use. Roll is used as a synonym of reel. |
| **Sender** | Device or Controller that initiates an XJMF exchange. |
| **Subelement** | A child Element of some other Element. |
| **Trait** | In the context of an element, a Trait of that element is either a single Subelement of it, a single attribute of it or a single attribute value of one of its Attributes. In the context of the specification, a table for an element contains all Traits of the element. |
| **Workstep** | A Workstep is an individual XJDF Process that can be processed on a single Device in one pass. A workstep is comprised of one or multiple phases such as setup, production or cleanup. |

## 1.9 Conformance

### 1.9.1 Conformance Terminology

The words “SHALL”, “SHALL NOT”, “SHOULD”, “SHOULD NOT”, “RECOMMENDED”, “MAY” and “NEED NOT” are used in this specification to define a requirement for the indicated XJDF consumer as follows.

**Table 1.5: Conformance Terminology**

| TERM | MEANING |
| --- | --- |
| **SHALL** | Means that the definition is an absolute requirement of the specification. |
| **SHALL NOT** | Means that the definition is an absolute prohibition of the specification. |
| **SHOULD or RECOMMENDED** | Means that there might exist valid reasons in particular circumstances for an implementer to ignore a particular item, but the implementer SHALL fully understand the implications and carefully weigh the alternatives before choosing a different course. |
| **SHOULD NOT or NOT RECOMMENDED** | Means that there might exist valid reasons in particular circumstances when the particular behavior is acceptable or even useful, but the implementer should fully understand the implications and then carefully weigh the alternatives before implementing any behavior described with this label. |
| **MAY or NEED NOT** | Means that an XJDF feature is truly optional. |

### 1.9.2 Interoperability Conformance Specifications

Interoperability Conformance Specifications (i.e., ICS documents) are developed by CIP4 working committees. They establish the minimum XJDF support requirements for Devices of a common class, including expected behavior. An ICS document can subset XJDF but cannot expand upon XJDF. For instance, an ICS that covers desktop printers can either omit or prohibit all of the postpress processes related to case binding. ICS documents can also establish minimum XJMF support requirements for a class of Devices.

Once published, ICS documents will form the basis for testing and self-certification by the product vendors.

The development of ICS documents is done in parallel, but not in synchronization, with the development of editions of the XJDF specification (e.g., an ICS is related to a specific edition of the XJDF specification, but might be released at a later date). Once approved, all published ICS documents will be available at http://www.CIP4.org.

## 1.10 Data Structures

Unless stated otherwise, this specification uses XML data types as defined by [XMLSchema]. For more details on XJDF data types, see Appendix A Data Types and Values.

### 1.10.1 Units of measurement

XJDF specifies most values in default units. This means that an implementation SHALL use the defined default units and SHALL NOT use alternate units.

The supported default units are described in Table A.3.23 Units which associates measurement types with the default unit. If there is no suitable entry, i.e. when a new resource is defined that introduces a new measurement type not listed in Table A.3.23 Units, then the processor MAY introduce a new unit, and that unit SHALL be based upon metric units. Speed SHALL be specified in units (as defined in the previous paragraph) per hour.

### 1.10.2 Counting in XJDF

When accessing data using an index, zero-based indices SHALL be used in XJDF. Thus the first index is 0, the second index is 1, etc. Negative values SHALL specify a number that is counted from the back of the list. Thus the last item is at index -1, the second to last item is at index -2 etc.

XJDF also allows ranges of items to be sub-selected from lists by using a pair of integer values where the first item identifies the start of the selection and the second item identifies the end of the selection. Thus the range `"0-1"` represents all entries of a list and the range `"-1 0"` represents the same list in reverse order.

### 1.10.3 Human and Machine readable strings and tokens

Tokens and strings are defined using three data types within XJDF, which are described in the following sections.

#### 1.10.3.1 Enumeration data types
The data type in the tables is either ‘enumeration’ or ‘enumerations’.

These are designed to be Machine readable values with a limited, well-defined, closed set of valid values. Enumeration data types cannot be localized. Thus implementers can rely on the values of these data types to be from the known list.

If the data type of the attribute in the tables is ‘enumeration’ then the description contains either the phrase “Allowed values are:” to show a set of values, or “Allowed value is from:” to refer to a set of values defined elsewhere. In either case one of the values from the indicated set SHALL be used as the value of the attribute.

If the data type of the attribute in the table is ‘enumerations’ then the phrase “Allowed values are from:” is used in the description to show or refer to a set of values, one or more of which (whitespace separated) SHALL be used as the value of the attribute.

If, in a later version of XJDF, values are added or deprecated from the list of values for an enumeration data type, then this will be called out in a modification note, see Section 1.6.4 Modification Notes.

#### 1.10.3.2 NMTOKEN data types
The data type in the tables is either ‘NMTOKEN’ or ‘NMTOKENS’.

These are designed to be Machine readable values with a limited set of recommended values but an unlimited set of valid values. NMTOKEN data types SHOULD NOT be localized. As the list of values is an open list, implementers cannot rely on the values of these data types to be from a predetermined list.

If the data type of the attribute in the tables is ‘NMTOKEN’ or ‘string’ then the description contains either the phrase “Values include:” to show a set of recommended values, or “Values include those from:” to refer to a set of values defined elsewhere. In either case one of the values from the indicated set MAY be used as the value of the attribute. This does not preclude the use of other values as required by vendor or customer extensions.

If the data type of the attribute in the table is ‘NMTOKENS’ then the phrase “Values include:” to show a set of recommended values, or “Values include those from:” to refer to a set of values defined elsewhere. In either case one or more of the (whitespace separated) values MAY be used as the value of the attribute. This does not preclude the use of other values as required by vendor or customer extensions.

If, in a later version of XJDF, recommended values are added or deprecated from an NMTOKEN data type, this will be not called out in a modification note. Modification to the list of suggested values will be provided at [CIP4Names] and updated with every specification release.

#### 1.10.3.3 String data types
The data type in the tables is ‘string’.

These are designed to be human readable values with an unlimited set of valid values. String data types may be localized. Thus implementers cannot rely on the values of these data types to be from a known list. No attempt is made to provide a list of valid string values.

> **Note:** In some cases, string data types are also designed to be Machine readable. This is typically the case when the value set is not defined by CIP4 and therefore a limitation to NMTOKEN is not possible without reducing functionality.
