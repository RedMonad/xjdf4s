# Chapter 2 Overview

## 2.1 Introduction

This chapter explains the basic aspects of XJDF. It outlines the terminology that is used and the components of a workflow necessary to execute a printing job using XJDF. Also provided is a brief discussion of XJDF process structure and the role of messaging in an XJDF job.

The reader is assumed to have basic knowledge of XML syntax, i.e. [XML], [XPath], [XMLNS] and [XMLSchema].

## 2.2 Referencing Data

### 2.2.1 Referencing External Data
External data is referenced from XJDF using standard URLs (Uniform Resource Locators) [RFC1738].

### 2.2.2 Identifying Sections of XJDF from External Sources
Certain data elements need to be identified from multiple XJDF or XJMF instances. Examples include `XJDF/@JobID`, `XJDF/@JobPartID`, `@ExternalID` and all attributes of `Part`. These entities do NOT have a data type of `ID` and systems that use XJDF SHALL maintain them.

An individual workstep SHALL be uniquely identified by the combination of `@JobID`, `@JobPartID` and the Partition Keys defined in any `Part`.

### 2.2.3 Identifying Sections of XJDF from within the Same XJDF
Certain data elements of a job need to be identified from within a single XJDF instance. XML provides an ID-IDREF mechanism where an `ID` SHALL only be defined once within an XML instance and MAY be referenced multiple times by an `IDREF` or `IDREFS` from within the same XJDF instance.

All attributes in XJDF with a data type of `ID` SHALL be named `ID`. The reference types MAY have names other than `IDREF`.

IDs and IDREFS are only valid within the scope of a single XJDF instance and NEED NOT be maintained when a new XJDF is generated.

The `@ID` attribute is generally defined in generic elements such as `Header`, `Product` or `Resource`. When the referencing element requires to reference a specific element such as `Media`, the `@ID` attribute will be that of the containing generic element.

*Example:* An attribute `@MediaRef` will reference `Resource/@ID` of the respective parent of the `Media`.

## 2.3 System Components

This section defines unique terminology used in this specification for the job and workflow components of XJDF. Links to additional information are included for some terms.

### 2.3.1 Workflow Component Roles
The components that create, modify, route, interpret and execute an XJDF job are known as Controllers, queues, Devices and Machines. The MIS or Management Information Systems is the top level Controller in an XJDF workflow.

By defining these terms, this specification does not intend to dictate to manufacturers how to design, build or implement an XJDF/XJMF system. In practice, it is very likely that individual system components will include a mixture of the roles described in the following sections.

#### 2.3.1.1 Machine
A Machine is any part of the workflow system designed to execute a process. Most often, this term refers to a piece of physical equipment, such as a press or a binder, but it can also refer to the software components used to run a particular Machine or perform a calculation. Computerized workstations, whether run through automated batch files or controlled by a human worker, are also considered Machines if they have no XJDF interface.

#### 2.3.1.2 Device
The most basic function of a Device is to execute the information specified and routed by a Controller. Devices SHALL be able to execute the instructions that are specified in XJDF and initiate Machines that can perform the physical execution. The communication between Machines and Devices is by definition proprietary and therefore not defined in this specification. Handling of inconsistent process instructions or Product Intent definitions by a Device is implementation dependent and out of scope of this specification. Devices SHOULD support XJMF messaging in order to interact dynamically with a Controller.

#### 2.3.1.3 Queue
Whereas a Device processes XJDF to produce a result, queues provide a method of ordering, prioritizing and scheduling queue entries that represent XJDF processes. Every Device that is capable of accepting XJDF via XJMF messaging SHALL provide exactly one queue. This specification makes no assumptions on implementation limitations of a queue. Thus a Device that can only process a single queue entry and cannot store any waiting queue entries still implements an albeit minimalistic queue.

#### 2.3.1.4 Controller
Controllers route XJDF information to the appropriate Devices. The minimum requirement of a Controller is that it can initiate processes on at least one Device, or at least one other slave Controller that will then initiate processes on a Device. In other words, a Controller is not a Controller if it has nothing to control. A pyramid-like hierarchy of Controllers can be built, with a Controller at the top of the pyramid controlling a series of lower-level Controllers at the bottom. The lowest-level Controllers in the pyramid, however, SHALL have Device capability. Therefore, Controllers SHALL be able to work in collaboration with other Controllers. Controllers can also determine process planning and scheduling data, such as process times and planned production amounts.

#### 2.3.1.5 Management Information System—MIS
The highest level Controller in a workflow is known as a Management Information Systems or MIS. It is responsible for dictating and monitoring the execution of all of the diverse aspects of the workflow. This task is facilitated by access to production information, either in real time using XJMF messaging or retrospectively using the audit records within a returned XJDF.

## 2.4 XJDF Workflow

XJDF does not dictate that a workflow must be constructed in any particular way. XJDF is equally as effective with a simple system using a single Controller and Device as it is with a completely automated industrial press workflow with integrated prepress and postpress operations.

An XJDF is defined in terms of inputs and outputs. The inputs of an XJDF consist of the materials it uses and the parameters that control it. For example, the inputs of an XJDF describing the process parameters for imaging the cover of a brochure might include requirements for trapping, raster image processing, and imposing the image. The output of our example XJDF might be a raster image.

A print job will typically require more than one process step to produce the Final Product. Each process step is completely defined by an XJDF. The interdependencies of the process steps MAY be specified in XJDF if the receiving Device requires this information. Otherwise these interdependencies SHOULD remain opaque and be processed in a proprietary manner by the job Controller.

### 2.4.1 Product Intent and Processes
XJDF describes a job from two points of view that are related but not identical.
* **Product Intent**: The customer or product designer will typically describe the desired Final Product without any knowledge of the manufacturing process. In XJDF this type of information is encoded in the `ProductList` and its child elements. Product Intent is described in detail in Chapter 4 Product Intent.
* **Process**: The Devices that execute a processing step will typically receive processing instructions for that specific work as part of the manufacturing process for a product. In XJDF this type of information is encoded in `ResourceSet` and their child elements. Process resources are described in detail in Chapter 6 Resources.

Intent descriptions have been consciously limited to details of the more common products. In order to reduce duplication of resources and keep intent definitions simple, some features that are typically required to describe products that are used in business to business workflows such as packaging have not been included in Intent descriptions. These specialized products SHOULD be described by adding process resources that describe the desired features.

Controllers such as MIS SHOULD evaluate Product Intent and provide all processing instructions for Devices as `ResourceSet` elements. Devices NEED NOT evaluate Product Intent to infer processing instructions. Product Intent is provided to Devices in order to provide operators with an overview of the context of the process step within one or more customer jobs.

### 2.4.2 Process Reporting
In most cases a Controller will be interested in processing results of an XJDF that has been submitted to a Device. Typical processing results include actual processing times, produced and consumed amounts, production reports and descriptions of process specific resources. These results SHOULD be provided either intermediately with XJMF signals or as descendents of `AuditPool`. The values of descendents of `XJDF/ResourceSet` MAY be updated.

> **Note:** Updates of descendants of `XJDF/ResourceSet` are discouraged and is provided only for backwards compatibility when transforming JDF to XJDF.

## 2.5 Role of Messaging in XJDF

Whereas XJDF will typically be submitted to a Device and only be returned after the process has been executed, XJMF provides methods to dynamically synchronize and manipulate Controllers and Devices. For more details on XJMF, see Chapter 7 Messaging and Chapter 9 Building a System.

## 2.6 Coordinate Systems in XJDF

This chapter explains how coordinate systems are defined and used in XJDF. It also shows how the matrices are used to specify a certain transformation and how these matrices can be used to transform coordinates from one coordinate system to another coordinate system.

### 2.6.1 Introduction

During the production of a printed product it often happens that one object is placed onto another object. During imposition, for example, single pages and marks (like cut, fold or register marks) are placed on a sheet surface. Later, at image setting, a bitmap containing one separation of a sheet surface is imposed on a piece of film. In a following step, the film is copied to a printing plate that is then mounted on a press. In postpress, the printed sheets are gathered on a pile. The objects involved in all these operations have a certain orientation and size when they are put together. In addition, one has to know where to place one object on the other.

The position of an object (e.g., a cut mark) on a plane can be specified by a two-dimensional coordinate. Every digital or physical Resource has its own coordinate system. The origin of each coordinate system is located in the lower left corner (i.e., the X coordinate increases from left to the right, and the Y coordinate increases from bottom to top).

Each page contained in a PDL file has its own coordinate system. In the same way a piece of film or a sheet of paper has a coordinate system. Within XJDF each of these coordinate systems is called a resource coordinate system.

If a process has more than one input resource with a coordinate system, it is necessary to define the relationship between these input coordinate systems. Therefore, a process coordinate system is defined for each process. XJDF tickets are written assuming an idealized Device that is defined in the process coordinate system for each process that the Device implements.

A real Device SHALL map the idealized process coordinate system to its own Device coordinate system.

The coordinate systems of the input resources are mapped to the process coordinate system. Each of those mappings is defined by a transformation matrix, which specifies how a coordinate (or position) of the input coordinate system is transformed into a coordinate of the target coordinate system. (See Section 2.6.5 Homogeneous Coordinates for mathematical background information.) In the same way, the mapping from the process coordinate system to the coordinate systems of the output resources is defined. The process coordinate system is also used to define the meaning of terms like "Top" or "Left", which are used as values for parameters in some processes.

*Figure 2-1: Standard coordinate system*
> **Image Description:** A diagram showing a standard 2D Cartesian coordinate system with the Origin at the lower left corner. The X-axis increases to the right, and the Y-axis increases towards the top.

It is important that no implicit transformations (such as rotations) are assumed if the dimensions of the input resources of a process do not match each other. Instead every transformation (e.g., a rotation) SHALL be specified explicitly by using the `Resource/@Orientation` or `Resource/@Transformation`.

#### 2.6.1.1 Source Coordinate Systems
The source coordinate system of a referenced object is defined by the lower left of the object. X values are increasing to the right, Y values are increasing towards the top. In case of PDF the lower left of the `MediaBox` defines the lower left of the source coordinate system.

> **Note:** Some object coordinate systems have optional tags to indicate internal transformations. These internal transformations SHALL be applied prior to defining the source coordinate system; for instance:
> * **PDF**: the rotation defined by the `Rotate` key SHALL be applied. The lower left of the `MediaBox` of the rotated PDF defines the lower left of the PDF source coordinate system.
> * **TIFF**: the orientation defined by the `Orientation` tag SHALL be applied. The lower left of the rotated TIFF defines the lower left of the TIFF source coordinate system.

### 2.6.2 Coordinate Systems of Resources and Processes

Each physical resource (e.g., `Component`) of a process has its own coordinate system, which is called the resource coordinate system. The coordinate system also implies a specific orientation of that Resource. On the other hand there is a coordinate system that is used to define various process-specific parameters. This coordinate system is called a target or process coordinate system.

It is often necessary to change the orientation of an input resource before executing the operation. This can be done by specifying `Resource/@Orientation` or `Resource/@Transformation`. This provides the ability to specify different matrices for the individual resources of a process.

#### 2.6.2.1 Use of Preview to Display Resource Orientation
It is often necessary to load printed material into finishing equipment manually. Particularly in the case of imposed sheets, the page orientation will not be unique and even the concept of "Front" or "Back" can be confusing, since front and back pages can be printed on the same surface of the imposed sheet. Preview ResourceSets with `Part/@PreviewType="ThumbNail"` or `Part/@PreviewType="Viewable"` SHOULD be provided to illustrate the desired orientation of the input components with respect to the Device.

#### 2.6.2.2 Coordinate Systems of Combined Processes
`XJDF/@Types` MAY specify multiple individual processes and thus also the respective coordinate systems of those processes. The individual process coordinate systems are not modified by the fact that the processes are part of a combined process. The orientation of a Resource for a specific process can be modified by specifying `Resource/@Orientation` or `Resource/@Transformation`. The resources that apply to a given process are defined explicitly in the process tables in Chapter 5 Processes for a mapping of parameter resources to process types.

*Figure 2-2: Relation between resource and process coordinate systems*
> **Image Description & Mermaid Diagram:** A flow diagram illustrating how multiple input resource coordinate systems are mapped to a central process coordinate system using transformation matrices, and how the process coordinate system is then mapped to multiple output resource coordinate systems.

```mermaid
flowchart LR
    subgraph Input Resources
        IR1[Resource coordinate system of input resource 1]
        IR2[Resource coordinate system of input resource 2]
        IRn[Resource coordinate system of input resource n]
    end
    
    PC((Process coordinate system))
    
    subgraph Output Resources
        OR1[Resource coordinate system of output resource 1]
        OR2[Resource coordinate system of output resource 2]
        ORn[Resource coordinate system of output resource n]
    end

    IR1 -->|Resource/@Transformation| PC
    IR2 -->|Resource/@Transformation| PC
    IRn -->|Resource/@Transformation| PC
    
    PC -->|Transformation| OR1
    PC -->|Transformation| OR2
    PC -->|Transformation| ORn
```

### 2.6.3 Coordinate System Transformations

The following table shows some matrices that can be used to change the orientation of a Resource. Most of the transformations require the width (`w`) and the height (`h`) of the Component as specified by X and Y in `Component/@Dimensions`. If these are unknown, it is still possible to define a general orientation in `Resource/@Orientation`. The naming of the attribute reflects the state of the resource and not necessarily the order of applied transformations. Thus "Rotate90" and "Flip90" specify that the original Y axis as represented by the spine is on top. In the case of Flip90, the Component is additionally flipped front to back.

*(Note: F = Front, B = Back, X/Y = coordinate axes)*

**Table 2.1: Matrices and Orientation values for describing the orientation of a Component**

| ORIENTATION VALUE | SOURCE COORDINATE SYSTEM | TRANSFORMATION MATRIX | ACTION | TARGET COORDINATE SYSTEM |
| --- | --- | --- | --- | --- |
| **Rotate0** | Y FX | `1 0 0 1 0 0` | No Action | Y FX |
| **Rotate90** | Y FX | `0 1 -1 0 h 0` | 90° Counterclockwise Rotation | Y FX |
| **Rotate180** | Y FX | `-1 0 0 -1 w h` | 180° Rotation | Y X F |
| **Rotate270** | Y FX | `0 -1 1 0 0 w` | 270° Counterclockwise Rotation | YF X |
| **Flip0** | Y FX | `1 0 0 -1 0 h` | Flip around X | Y X B |
| **Flip90** | Y FX | `0 -1 -1 0 h w` | 90° Counterclockwise Rotation + Flip around X | Y X |
| **Flip180** | Y FX | `-1 0 0 1 w 0` | 180° Rotation + Flip around X | Y BX B |
| **Flip270** | Y FX | `0 1 1 0 0 0` | 270° Counterclockwise Rotation + Flip around X | Y X |

### 2.6.4 General Rules

The following rules summarize the use of coordinate systems in XJDF.
* Every individual piece of material (film, plate, paper) has a resource coordinate system.
* Every process has a process coordinate system.
* Terms like top, left, etc., are used with respect to the process coordinate system in which they are used and are independent of orientation (i.e., landscape or portrait), and the human reading direction.
* The coordinate system of each input component is mapped to the process coordinate system.
* The coordinate system might change during processing (e.g., in Folding).
* The description of a product in XJDF is independent of the particular Machine used to produce this product. When creating setup information for an individual Machine, it might be necessary to compensate for certain Machine characteristics. At printing, for example, it might be necessary to rotate a landscape job because the printing width of the press is not large enough to run the job without rotation.

### 2.6.5 Homogeneous Coordinates

A convenient way to calculate coordinate transformations in a two-dimensional space is by using so-called homogeneous coordinates. With this concept, a two-dimensional coordinate `P=(x,y)` is expressed in vector form as `[x y 1]`. The third element “1” is added to allow the vector being multiplied with a transformation matrix describing scaling, rotation, and translation in one shot. Although this only requires a 2 × 3 matrix (e.g., as it is used in PostScript) in practice 3 × 3 matrices are much more common, because they can be concatenated very easily. Thus, the third column SHALL be set to `"0 0 1"`.

#### 2.6.5.1 Transforming a point
In this example, the position P given in the coordinate system A is transformed to a position of coordinate system B. The relationship between the two coordinate systems is given by the transformation matrix `Trf`.

**Table 2.2: Coordinate Transformation Examples**

| MATRIX | XJDF VALUE | DESCRIPTION |
| --- | --- | --- |
| `a b 0`<br>`c d 0`<br>`e f 1` | `"a b c d e f"` | General transformation case. |
| `1 0 0`<br>`0 1 0`<br>`0 0 1` | `"1 0 0 1 0 0"` | Identity transformation. |
| `1 0 0`<br>`0 1 0`<br>`dx dy 1` | `"1 0 0 1 dx dy"` | Translation by dx, dy. |
| `cos φ  sin φ  0`<br>`-sin φ cos φ  0`<br>`0      0      1` | `"cos φ sin φ -sin φ cos φ 0 0"` | Rotation around the origin by φ degrees counter-clockwise.<br>*Note: Since the rotation is around the origin in the lower left hand corner, an additional translation will typically be required to shift the object back to its original position.* |

*Figure 2-3: Transforming a point (example)*
> **Image Description:** A visual example of a coordinate transformation. A point PA(30, 100) in coordinate system A is translated by a matrix (dx=40, dy=60) to a new position PB(70, 160) in coordinate system B.
