# 7 Messaging

A workflow is a dynamic set of interacting Controllers and Devices. For the workflow to run efficiently, these Controllers and Devices need to communicate and interact in a well defined manner. Whereas XJDF will typically be submitted to a Device and only be returned after the process has been executed, XJMF messages MAY be exchanged at any time. Typical use cases for XJMF include but are not limited to:

* System bootstrapping and setup
* Dynamic status, resource usage and error tracking for jobs and Devices
* Pipe control
* Device setup and job changes
* Queue handling and job submission

This chapter specifies the XML structure of XJMF. For details of the exchange protocol and data packaging, see Section 9.5 XJDF and XJMF Interchange Protocol and Section 9.7 XJDF Packaging.

## 7.1 XJMF

XJMF and XJDF have inherently different structures. In order to allow immediate identification of messages, XJMF uses the unique name `XJMF` as its own root-element name. XJMF elements SHALL contain one or more messages that provide more detailed information.

**Table 7.1: XJMF Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@$schema?`<br>*(JSON Exception, New in XJDF 2.2)* | URL | `@$schema` SHOULD reference the JSON schema for XJMF.<br>**JSON Exception:** `@$schema` SHOULD be provided in JSON if XJMF is the root JSON object and SHALL NOT be provided in XML. |
| `@Name?`<br>*(JSON Exception, New in XJDF 2.2)* | enumeration | `@Name` SHALL specify the local name of the XJMF when XJMF is defined as a root JSON object.<br>Allowed value is: `XJMF`<br>**JSON Exception:** `@Name` SHALL be provided in JSON if XJMF is the root JSON object and SHALL NOT be provided in XML. |
| `@Version?` | enumeration | `@Version` SHALL define the version of the XJMF document. The value of `@Version` SHALL be `"2.2"` for documents that comply with this specification.<br>Allowed value is from: *XJDFXJMFVersion*. |
| `Header` | element | `Header` SHALL provide information about the sender of the XJMF package. If the sender is a proxy Controller that forwards information from multiple Devices, `XJMF/Header` SHALL provide information about the proxy Controller.<br>See also `Message/Header`. |
| `<message elements>+`<br>*(JSON Exception, Modified in XJDF 2.2)* | element | One or more messages SHALL be provided. The messages SHOULD be one of the message element types that are defined in XJMF, see Section 7.3 List of All XJMF Messages. The recipient SHALL process the messages in XML order.<br>**Modification note:** Starting with XJDF 2.2 the cardinality symbol has changed from zero or more occurrences to one or more occurrences. The normative text has always required one or more messages.<br>**JSON Exception:** Cardinality change - for XJMF encoded in JSON exactly one message element SHALL be provided. |

### Example 7.1: JSON-encoded XJMF
The following example illustrates how a simple XJMF root node with a `SignalNotification` is encoded in both XML and JSON.

**XML Encoding**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0" Version="2.2">
  <Header DeviceID="CIP4_JDF_Writer_Java" ID="l_230910_094905994_000000" Time="2023-09-10T09:49:05+02:00"/>
  <SignalNotification>
    <Header DeviceID="CIP4_JDF_Writer_Java" ID="l_230910_094906015_000001" Time="2023-09-10T09:49:06+02:00"/>
    <Notification Class="Event"/>
  </SignalNotification>
</XJMF>
```

**JSON Encoding**
```json
{
  "Header": {
    "DeviceID": "CIP4_JDF_Writer_Java",
    "ID": "l_230910_094905994_000000",
    "Time": "2023-09-10T09:49:05+02:00"
  },
  "Name": "XJMF",
  "SignalNotification": {
    "Header": {
      "DeviceID": "CIP4_JDF_Writer_Java",
      "ID": "l_230910_094906015_000001",
      "Time": "2023-09-10T09:49:06+02:00"
    },
    "Notification": {
      "Class": "Event"
    }
  },
  "Version": "2.2"
}
```

## 7.1.1 Message

The following table describes the contents of a message. A message is an abstract data type. Section 7.3 List of All XJMF Messages provides a list of all message element instances.

**Table 7.2: Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | `Header` SHALL provide information about the original sending Device of the individual message. `Header` SHALL be the first element in a Message, regardless of the alphabetical ordering of any other elements.<br>The information in `Header` SHALL NOT be modified if the message is passed through a proxy Controller. See also `XJMF/Header`. |
| `<foreign namespace message elements>*` | element | Any message elements in a foreign namespace.<br>Foreign namespace extensions SHOULD NOT duplicate functionality of existing XJDF messages. They SHALL adhere to the structure that is defined in Section 7.1.1 Message and SHALL adhere to whichever of the Message Family definitions, as described in Section 7.2 XJMF Message Families, is appropriate.<br>These elements MAY occur interleaved between messages in the XJDF namespace. |

## 7.1.2 Header

`Header` SHALL provide information about the sender of an audit, message or XJMF. The information in `Message/Header` and `AuditXXX/Header` SHALL NOT be modified and SHALL represent the status of the original message or audit if the message is passed through a proxy Controller. The information in `XJMF/Header` SHALL be modified to represent the status of the proxy Controller if the message is passed through a proxy Controller.

The `Header` element SHALL be the first element in any audit or message as shown in those tables.

**Table 7.3: Header**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `AgentName?` | string | The name of the application that generated the parent message or XJMF.<br>Both the company name and the product name MAY appear, and SHOULD be consistent between versions of the application. |
| `AgentVersion?` | string | The version of the application that generated the parent message or XJMF.<br>The format of the version string MAY vary from one application to another, but SHOULD be consistent for an individual application. |
| `Author?` | string | Human readable description of the employee that entered the message.<br>`XJMF/Header/@Author` SHOULD NOT be specified. |
| `DescriptiveName?`<br>*(New in XJDF 2.1)* | string | Human readable descriptive name of the parent message, audit or XJMF. |
| `DeviceID` | NMTOKEN | Unique identifier of the sender. |
| `ICSVersions?` | NMTOKENS | CIP4 Interoperability Conformance Specification (ICS) Versions that the sender complies with. The value of `@ICSVersions` SHALL conform to the value format described in Section 3.1.1 ICS Versions Value. |
| `ID?`<br>*(Modified in XJDF 2.2)* | ID | If present, `@ID` SHALL identify the parent message or XJMF and SHALL be unique for all messages and XJMF initiated by the Sender.<br>`@ID` SHALL be present if `Subscription` is present in the parent `Query`.<br>`@ID` SHOULD be present if the parent is a `Response` or `Signal`.<br>**Modification note:** The requirements for the scope of uniqueness were clarified in XJDF 2.2. |
| `PersonalID?` | NMTOKEN | Machine readable identifier of the employee that entered the message.<br>`XJMF/Header/@PersonalID` SHOULD NOT be specified. |
| `refID?` | NMTOKEN | `@refID` SHALL identify a message that the parent of this `Header` responds to. If the parent is a `Response`, this SHALL be `Header/@ID` of the initiating `Query` or `Command`. If the parent is a `Signal`, this SHALL be `Header/@ID` of the initiating `Query`.<br>*Note: The data type is NMTOKEN because the referenced Header/@ID need not be in the same XML document.* |
| `Time` | dateTime | Date and time when the message was generated. |
| `<foreign namespace elements>*` | element | Any elements in a foreign namespace. Foreign namespace extensions SHOULD NOT duplicate functionality of XJDF. Foreign namespace extensions SHALL be specified after all elements in the XJDF namespace. |

---

## 7.2 XJMF Message Families

A message belongs to one of four Message Families. These families are `Query`, `Command`, `Signal` and `Response`. An explanation of each family is provided in the following sections. Message Families are abstract data types, Section 7.3 List of All XJMF Messages provides a list of all Message Family element instances.

### 7.2.1 Query

A `Query` is an abstract message that retrieves information from a receiver without changing the state of that receiver. For details of XJMF handshaking, see Section 9.6 XJMF Handshaking.

**Table 7.4: Query**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@Languages?`<br>*(New in XJDF 2.2)* | languages | `@Languages` SHALL specify the list of languages selected for human readable communication in the resulting `Signal` or `Response` messages. If not specified, the operating system language SHALL be used. If multiple languages are specified, the second and further languages SHOULD only be used for providing additional localized `Comment` elements. Messages SHALL NOT be sent multiple times for the same event. |
| `Header` | element | See `Message/Header`. |
| `Subscription?` | element | A `Subscription` is a request for a persistent channel. If present, `Subscription` SHALL be specified after `Header` and prior to any other elements in a `Query`. Any other elements in the `Query` will then be alphabetically ordered in the normal way. If `Subscription` is present then `Header/@ID` shall be specified. For details of creating and managing persistent channels see Section 9.6.3 Managing Persistent Channels. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

#### 7.2.1.1 Subscription

A `Subscription` specifies the target URL for signals and optionally, additional details of the persistent channel. See Section 9.6.2 Subscribing for Signals for details.

**Table 7.5: Subscription Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ChannelMode?` | enumerations | Specifies reliability of persistent channel, and whether it is required or just preferred. Ordered list, with most preferred channel mode first.<br>If none of the provided values of `@ChannelMode` are supported by the consumer of the subscription, the `Response` SHOULD indicate `@ReturnCode="111"`, which is “Subscription request denied”.<br>Allowed values are from: *ChannelMode*.<br>*Note: See Signal/@ChannelMode.* |
| `@Languages?`<br>*(Deprecated in XJDF 2.2)* | languages | List of languages selected for human readable communication. If not specified, the operating system language SHOULD be used. If multiple languages are specified, the second and further languages SHOULD only be used for providing additional localized `Comment` elements. Messages SHALL NOT be sent multiple times for the same event.<br>**Deprecation note:** `@Languages` has been moved from `Subscription` to `Query`, `QueryKnownDevices`, `QueryNotification`, `QueryResource`, `QueryStatus` and `SubscriptionInfo`. |
| `@RepeatTime?` | float | Requests an update `Signal` every `@RepeatTime` seconds. If specified, the `Signal` SHALL be generated periodically independent of any other trigger conditions.<br>`@RepeatTime` SHALL NOT override any `Signals` triggered by a change of status. Signals triggered by a status change SHALL be sent regardless of the value of `@RepeatTime`. A sender MAY restart counting for `@RepeatTime` based Signals whenever it sends a `Signal` to the same subscription. |
| `@URL` | URL | `@URL` specifies the URL of the persistent channel receiving end.<br>The protocol of the `Subscription` is specified by the scheme of `@URL`. |

### 7.2.2 Command

A `Command` is syntactically equivalent to a `Query`, but rather than simply retrieving information, it is an abstract message that causes a state change in the receiver. For details of XJMF handshaking, see Section 9.6 XJMF Handshaking.

**Table 7.6: Command Family**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

### 7.2.3 Signal

A `Signal` is an abstract message element that a receiver of a `Query` with a subscription SHALL asynchronously send whenever the conditions specified in the `Subscription` are true.
*Note: Signals are typically sent from a Device to a Controller. For details of XJMF handshaking, see Section 9.6 XJMF Handshaking. For details of setting up subscriptions for signals, see Section 9.6.2 Subscribing for Signals.*

**Table 7.7: Signal Family**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ChannelMode?` | enumeration | Specifies reliability of the signal.<br>Allowed value is from: *ChannelMode*. |
| `Header` | element | See `Message/Header`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

### 7.2.4 Response

A `Response` is a message that a receiver SHALL synchronously send to a sender as a response to a message. For details of XJMF handshaking, see Section 9.6 XJMF Handshaking.

**Table 7.8: Response Family**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | `@ReturnCode` summarizes the result of the response. The value `"0"` indicates success. `@ReturnCode` SHALL be provided if an error occurred. For predefined values see Appendix A.4.2 Return Codes.<br>*Note: Additional values MAY be specified in ICS documents.* |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | Additional information including textual description of the return code. The `Notification` element SHOULD be provided if the `@ReturnCode` is greater than 0.<br>See Section 7.10 Notification.<br>If present then the `Notification` element SHALL be placed after `Header` and prior to any other elements in a `Response` message. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

---

## 7.3 List of All XJMF Messages

The following table provides a list of all message element types.

**Table 7.9: List of XJMF Messages**

| MESSAGES | DESCRIPTION |
| --- | --- |
| `CommandForceGang`<br>`ResponseForceGang` | A Gang is forced to execute. |
| `QueryGangStatus`<br>`ResponseGangStatus`<br>`SignalGangStatus` | The status of a Gang is queried. |
| `QueryKnownDevices`<br>`ResponseKnownDevices`<br>`SignalKnownDevices` | Returns information about the Devices that are controlled by a Controller. |
| `QueryKnownMessages`<br>`ResponseKnownMessages` | Returns a list of all messages that are supported by the Controller. |
| `QueryKnownSubscriptions`<br>`ResponseKnownSubscriptions`<br>`SignalKnownSubscriptions` | Returns a list of active persistent channels. |
| `CommandModifyQueueEntry`<br>`ResponseModifyQueueEntry` | Modifies the properties of one or more `QueueEntry` elements. |
| `QueryNotification`<br>`ResponseNotification`<br>`SignalNotification` | Used to signal events due to any activities of a Device, operator, etc. Generally sent as signals. `QueryNotification` allows subscriptions for `SignalNotification` messages. |
| `CommandPipeControl`<br>`ResponsePipeControl` | All pipe related commands are implemented using the `PipeControl` message. |
| `QueryQueueStatus`<br>`ResponseQueueStatus`<br>`SignalQueueStatus` | Returns the `Queue` elements that describe a queue or set of queues. |
| `CommandRequestQueueEntry`<br>`ResponseRequestQueueEntry` | A new job is requested by the Device. This message is used to signal that a Device has processing resources available. |
| `CommandResource`<br>`QueryResource`<br>`ResponseResource`<br>`SignalResource` | Queries and/or modifies XJDF resources that are used by a Device, such as Device settings. This message can also be used to query the level of consumables in a Device. |
| `CommandResubmitQueueEntry`<br>`ResponseResubmitQueueEntry` | Replaces a queue entry without affecting the entry’s parameters. `CommandResubmitQueueEntry` is used, for example, for late changes to a submitted XJDF. |
| `CommandReturnQueueEntry`<br>`ResponseReturnQueueEntry` | Returns a job that had been submitted with a `SubmitQueueEntry` to the Controller that originally submitted the job. |
| `CommandShutDown`<br>`ResponseShutDown` | Shuts down a Device. |
| `QueryStatus`<br>`ResponseStatus`<br>`SignalStatus` | Queries or signals the general status of a Device, Controller or job. |
| `CommandStopPersistentChannel`<br>`ResponseStopPersistentChannel` | Closes a persistent channel. |
| `CommandSubmitQueueEntry`<br>`ResponseSubmitQueueEntry` | Submits an XJDF to a queue in order to be executed. |
| `CommandWakeUp`<br>`ResponseWakeUp` | Wakes up a Device that is in standby mode. |

---

## 7.4 ForceGang

The `ForceGang` message forces all the selected `QueueEntry[@Status="Waiting"]` elements that belong to a Gang to be executed, even though the Device dependent queue entry collecting algorithm might not be completed. A `QueueEntry` belongs to a Gang if `QueueEntry/@GangName` is included in the list of `GangCmdFilter/@GangNames`.

### 7.4.1 CommandForceGang

#### 7.4.1.1 GangCmdFilter

**Table 7.10: CommandForceGang Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `GangCmdFilter` | element | Defines the Gang(s) to be forcibly executed. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.11: GangCmdFilter Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@GangNames?` | NMTOKENS | `@GangNames` SHALL specify a list of queue entries with matching values of `QueueEntry/@GangName` that SHALL be processed. If not specified, all queue entries with a non-empty value of `QueueEntry/@GangName` SHALL be processed. |
| `@Policy?` | enumeration | The policy with which the elements in the Gang SHALL be processed.<br>Allowed values are:<br>**All** - All elements in the given Gang SHALL be processed.<br>**Optimized** - As many elements in a given Gang as can be processed without unnecessary waste SHOULD be processed. The algorithm for selecting the respective elements is implementation dependent and SHOULD take priority and scheduling data into account. |

### 7.4.2 ResponseForceGang

**Table 7.12: ResponseForceGang Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

---

## 7.5 GangStatus

`GangStatus` returns a description of the Gang(s). Details are specified in the `GangInfo` element.

### 7.5.1 QueryGangStatus

#### 7.5.1.1 GangQuFilter

**Table 7.13: QueryGangStatus Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `Subscription?` | element | See `Query/Subscription`. |
| `GangQuFilter?` | element | Defines a filter for the Gang(s) that are queried. If `GangQuFilter` is not supplied, all Gangs are queried. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.14: GangQuFilter Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@GangNames?` | NMTOKENS | `@GangNames` SHALL specify a list of `GangInfo` elements with matching values of `GangInfo/@GangName` that SHALL be returned. If not specified, all available `GangInfo` elements SHALL be returned. |

### 7.5.2 ResponseGangStatus

#### 7.5.2.1 GangInfo
Details of the Gang are specified in `GangInfo` elements.

**Table 7.15: ResponseGangStatus Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `GangInfo*` | element | Describes the status of the Gang(s). |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.16: GangInfo Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@Amount?` | float | Quantity of `QueueEntry` items that are currently waiting to be executed. If the Device specifies amount in a unit other than countable objects, such as m², `@Amount` SHALL be specified in the units of the Device. |
| `@GangName` | NMTOKEN | Name of the Gang. |

### 7.5.3 SignalGangStatus

**Table 7.17: SignalGangStatus Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ChannelMode?` | enumeration | Allowed value is from: *ChannelMode*.<br>*Note: See Signal/@ChannelMode.* |
| `Header` | element | See `Message/Header`. |
| `GangInfo*` | element | Describes the status of the Gang(s). |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

---

## 7.6 KnownDevices

The `KnownDevices` query message requests information about the Devices that are controlled by a Controller. If a high level Controller controls lower level Controllers, it SHOULD also list the Devices that are controlled by these. The response is a list of `Device` elements. Example 7.2 shows the response from a press Controller that controls two physical presses.

### Example 7.2: KnownDevices Response
```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
  <Header DeviceID="VeggieController" ID="l_000002" Time="2020-03-01T19:56:15.072+01:00"/>
  <ResponseKnownDevices ReturnCode="0">
    <Header DeviceID="VeggieController" ID="R1" Time="2020-03-01T19:56:15.110+01:00" refID="Q1"/>
    <Device DeviceID="dev1" DeviceType="ACME Linda potato press V16-12" XJMFURL="http://acmepotato1:1234/xjmfurl"/>
    <Device DeviceID="dev2" DeviceType="ACME Baldrick turnip press V42-66" XJMFURL="http://acmeturnip1:1234/xjmfurl"/>
    <!-- One Device element for each known device follows here -->
  </ResponseKnownDevices>
</XJMF>
```

### 7.6.1 QueryKnownDevices

#### 7.6.1.1 DeviceFilter
The `DeviceFilter` element specifies the level of detail that is requested for the list of Devices in the response.

**Table 7.18: QueryKnownDevices Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@Languages?`<br>*(New in XJDF 2.2)* | languages | See `Query/@Languages`. |
| `Header` | element | See `Message/Header`. |
| `Subscription?` | element | See `Query/Subscription`. |
| `DeviceFilter?` | element | `DeviceFilter` refines the level of detail of the list of Devices queried. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.19: DeviceFilter Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@DeviceDetails?` | enumeration | `@DeviceDetails` refines the level of provided information about the Device.<br>Allowed values are:<br>**Brief** – Provide only `Device/@DeviceID`.<br>**Full** – Provide maximum available Device information including all Module and Device descriptions.<br>**Modules** – `Module` elements SHALL be provided if the Device has modules. |

### 7.6.2 ResponseKnownDevices

**Table 7.20: ResponseKnownDevices Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `Device*` | element | Each `Device` SHALL represent one of the known Devices. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

### 7.6.3 SignalKnownDevices

**Table 7.21: SignalKnownDevices Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ChannelMode?` | enumeration | Allowed value is from: *ChannelMode*.<br>*Note: See Signal/@ChannelMode.* |
| `Header` | element | See `Message/Header`. |
| `Device*` | element | Each `Device` SHALL represent one of the known Devices. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

---

## 7.7 KnownMessages

The `KnownMessages` query message returns a list of all message types that are supported by the Controller.

### 7.7.1 QueryKnownMessages

**Table 7.22: QueryKnownMessages Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

### 7.7.2 ResponseKnownMessages

#### 7.7.2.1 MessageService
The response is a list of `MessageService` elements. Each `MessageService` SHALL specify one explicit message type that is supported by the Device.

**Table 7.23: ResponseKnownMessages Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `MessageService*` | element | Each `MessageService` SHALL specify one supported message. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.24: MessageService Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ResponseModes?` | enumerations | Specifies the supported synchronous `Response` or `Signal` channel modes with which the receiver can reply to a `Query`. `@ResponseModes` SHALL NOT be specified unless `@Type` specifies a `Query` message.<br>Allowed values are:<br>**FireAndForget** – The response to the message is implemented as a persistent channel `Signal` with `Signal/@ChannelMode="FireAndForget"`.<br>**Reliable** – The response to the message is implemented as a persistent channel `Signal` with `Signal/@ChannelMode="Reliable"`.<br>**Response** - The response to the message is implemented as a synchronous `Response`. |
| `@Type` | NMTOKEN | Name of the supported message element, e.g. `QueryKnownMessages`. |
| `@URLSchemes?` | enumerations | List of schemes supported for the message defined by this `MessageService`.<br>Allowed values are:<br>**http** – http (Hypertext Transport Protocol)<br>**https** – https (Hypertext Transport Protocol – Secure) |

### Example 7.3: KnownMessages Response
The following is an example of a `ResponseKnownMessages`.
```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
  <Header DeviceID="DeviceID" ID="l_000002" Time="2019-03-26T14:07:48.241+00:00"/>
  <ResponseKnownMessages ReturnCode="0">
    <Header DeviceID="DeviceID" ID="R1" Time="2019-03-26T14:07:48.242+00:00" refID="Q1"/>
    <MessageService ResponseModes="Response" Type="QueryKnownMessages"/>
    <MessageService ResponseModes="FireAndForget Reliable" Type="QueryStatus"/>
    <MessageService Type="CommandSubmitQueueEntry"/>
    <MessageService Type="ResponseReturnQueueEntry"/>
  </ResponseKnownMessages>
</XJMF>
```

---

## 7.8 KnownSubscriptions

The `KnownSubscriptions` message enables Controllers to query Devices for a list of active persistent channels.

### 7.8.1 QueryKnownSubscriptions

#### 7.8.1.1 SubscriptionFilter
The `SubscriptionFilter` element is a filter to limit the list of `SubscriptionInfo` elements that are returned in the `KnownSubscriptions` response.

**Table 7.25: QueryKnownSubscriptions Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `Subscription?` | element | See `Query/Subscription`. |
| `SubscriptionFilter?` | element | This `SubscriptionFilter` selects which subscriptions SHALL be included in the returned messages’ list of `SubscriptionInfo` elements. If not specified a `SubscriptionInfo` element is supplied for all known subscriptions. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.26: SubscriptionFilter Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@DeviceID?` | NMTOKEN | Only `SubscriptionInfo` elements for subscriptions from Devices or Controllers with a matching `@DeviceID` attribute SHALL be returned. |
| `@URL?` | URL | URL of the receiving Controller. This SHALL be identical to the `Subscription/@URL` that was used to create the persistent channel. Only `SubscriptionInfo` elements with a matching value of `SubscriptionInfo/@URL` SHALL be returned. |

### 7.8.2 ResponseKnownSubscriptions

**Table 7.27: ResponseKnownSubscriptions Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `SubscriptionInfo*` | element | One `SubscriptionInfo` SHALL be provided for each active persistent channel that is selected by `QueryKnownSubscriptions/SubscriptionFilter`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

### 7.8.3 SignalKnownSubscriptions

**Table 7.28: SignalKnownSubscriptions Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ChannelMode?` | enumeration | Allowed value is from: *ChannelMode*.<br>*Note: See Signal/@ChannelMode.* |
| `Header` | element | See `Message/Header`. |
| `SubscriptionInfo*` | element | One `SubscriptionInfo` SHALL be provided for each active persistent channel that is selected by `QueryKnownSubscriptions/SubscriptionFilter`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

---

## 7.9 ModifyQueueEntry

`ModifyQueueEntry` modifies the state or position of one of more `QueueEntry` elements that are selected by `QueueFilter`. `@Operation` specifies the operation that SHALL be applied to the selected queue entries.
See also `ResubmitQueueEntry` command for modifications of the underlying XJDF without modifying the queues.

### 7.9.1 CommandModifyQueueEntry

#### 7.9.1.1 ModifyQueueEntryParams

**Table 7.29: CommandModifyQueueEntry Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `ModifyQueueEntryParams` | element | `ModifyQueueEntryParams` defines the selected queue entries and the operation to be performed. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.30: ModifyQueueEntryParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@GangName?` | NMTOKEN | Name of the Gang that all `QueueEntry` items selected by the `QueueFilter` SHALL be moved to. `@GangName` SHALL NOT be specified unless `@Operation="SetGang"`. If `@Operation="SetGang"` and `@GangName` is not specified, then all selected `QueueEntry` items SHALL be removed from their current Gang. |
| `@NextQueueEntryID?` | NMTOKEN | `QueueEntry/@QueueEntryID` of the queue entry that SHALL be positioned directly behind the queue entries that are selected by `QueueFilter`. If more than one `QueueEntry` is selected, the ordering of these elements in the resulting queue is implementation dependent. Not more than one of `@NextQueueEntryID`, `@PrevQueueEntryID`, `@Position` or `@Priority` SHALL be specified.<br>`@NextQueueEntryID` SHALL NOT be specified unless `@Operation="Move"`. |
| `@Operation` | enumeration | The operation that SHALL be performed on the queue entries that are selected by `QueueFilter`.<br>Allowed value is from: *Table 7.31 Operation Attribute Values*. |
| `@Position?` | integer | Position in the queue where the queue entries that are selected by `QueueFilter` SHALL be moved to.<br>*Note: The position is based on the queue before modification. Thus if a queue entry is moved back in the queue, its final position is one lower than specified in `@Position`.*<br>If more than one `QueueEntry` is selected, the ordering of these elements in the resulting queue is implementation dependent. Not more than one of `@NextQueueEntryID`, `@PrevQueueEntryID`, `@Position` or `@Priority` SHALL be specified.<br>`@Position` SHALL NOT be specified unless `@Operation="Move"`. |
| `@PrevQueueEntryID?` | NMTOKEN | ID of the queue entry that SHALL be positioned directly in front of the queue entries that are selected by `QueueFilter`. If more than one `QueueEntry` is selected, the ordering of these elements in the resulting queue is implementation dependent. Not more than one of `@NextQueueEntryID`, `@PrevQueueEntryID`, `@Position` or `@Priority` SHALL be specified. `@PrevQueueEntryID` SHALL NOT be specified unless `@Operation="Move"`. |
| `@Priority?` | integer | New priority of the `QueueEntry` elements that are selected by `QueueFilter`. Priority is a number from `"0"` to `"100"`, where `"0"` is the lowest priority and `"100"` is the maximum priority. If more than one `QueueEntry` is selected, the ordering of these elements in the resulting queue is implementation dependent. Not more than one of `@NextQueueEntryID`, `@PrevQueueEntryID`, `@Position` or `@Priority` SHALL be specified. `@Priority` SHALL NOT be specified unless `@Operation="Move"`. |
| `QueueFilter` | element | This `QueueFilter` selects the `QueueEntry` elements that the operation SHALL be applied to. |

#### 7.9.1.2 Operation Attribute Values

**Table 7.31: Operation Attribute Values**

| VALUE | DESCRIPTION |
| --- | --- |
| **Abort** | The `QueueEntry` elements selected by `QueueFilter` SHALL be aborted and remain in the `Queue` with `QueueEntry/@Status="Aborted"`. `ProcessRun/@EndStatus` and `NodeInfo/@Status` of the XJDF that represents the queue entry SHALL be set to `"Aborted"` and the XJDF SHALL be delivered to the URL as specified by `QueueSubmissionParams/@ReturnJMF`. |
| **Complete** | The `QueueEntry` elements selected by `QueueFilter` SHALL be stopped and remain in the `Queue` with `QueueEntry/@Status="Completed"`. `ProcessRun/@EndStatus` and `NodeInfo/@Status` of the XJDF that represents the queue entry SHALL be set to `"Completed"` and the XJDF SHALL be delivered to the URL as specified by `QueueSubmissionParams/@ReturnJMF`. |
| **Hold** | If `QueueEntry/@Status` is `"Waiting"`, `QueueEntry/@Activation` SHALL be set to `"Held"`. The "Hold" operation SHALL NOT be applied to `QueueEntry` elements with a `@Status` other than `"Waiting"` or an `@Activation` other than `"Active"`. If `QueueEntry/@GangPolicy` is other than `"NoGang"`, a held `QueueEntry` retains its respective Gang data but SHALL NOT influence execution of other `QueueEntry` elements that are in the Gang. |
| **Move** | The position of the `QueueEntry` elements selected by `QueueFilter` SHALL be modified. The position of a `QueueEntry` SHALL NOT be modified unless `@Status="Waiting"` or `@Status="Held"`.<br>If `ModifyQueueEntryParams/@Priority` is not specified, then each `QueueEntry` element selected by `ModifyQueueEntryParams/QueueFilter` SHALL have `@Priority` set to a value based upon its new Queue position such that it is smaller than or equal to the value of `@Priority` of the `QueueEntry` than precedes it and is greater than or equal to the value of `@Priority` of the `QueueEntry` that follows it.<br>*Note: The requirement to set the value of `@Priority` ensures that the `QueueEntry` elements in a Queue are always sorted by `@Priority`.* |
| **Remove** | The `QueueEntry` elements selected by `QueueFilter` SHALL be removed from the queue. The `Remove` operation SHALL NOT be applied if `QueueEntry[@Status="InProgress"` or `@Status="Suspended"]`. |
| **Resume** | The `QueueEntry` elements selected by `QueueFilter` SHALL be resumed. If `QueueEntry/@Activation="Held"`, `QueueEntry/@Activation` SHALL be set to `"Active"`. If `QueueEntry/@Status="Suspended"`, `QueueEntry/@Status` SHALL be set to `"InProgress"`. If `QueueEntry/@GangPolicy` is other than `"NoGang"`, a resumed `QueueEntry` joins its respective Gang. |
| **SetGang** | `QueueEntry/@GangName` of the items selected by `QueueFilter` SHALL be set to the value of `@GangName`. The `"SetGang"` operation SHALL NOT be applied unless `QueueEntry/@Status="Waiting"`. |
| **Suspend** | The `QueueEntry` elements selected by `QueueFilter` SHALL be suspended and its `@Status` set to `"Suspended"` if its `@Status` is `"InProgress"`. Whether other queue entries can be run while the queue entries remain suspended depends on implementation. The `"Suspend"` operation has no effect on `QueueEntry` elements with a `@Status` other than `"InProgress"`. |

### 7.9.2 ResponseModifyQueueEntry

**Table 7.32: ResponseModifyQueueEntry Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`.<br>In case of at least one failure due to an incorrect `QueueEntry/@Status`, `@ReturnCode` SHALL be set to a non-zero value that SHOULD be in the range of 105-116. See Appendix A.4.2 Return Codes for details. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `QueueEntry*` | element | Describes the selected `QueueEntry` elements after the command has been executed. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

### Example 7.4: Resuming a Queue Entry
The following examples illustrate the Command and Response sequence to resume a previously held `QueueEntry` with `@JobID="j1"`.

**Command:**
```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
  <Header DeviceID="TestSender" ID="l_000002" Time="2019-03-26T14:07:48.342+00:00"/>
  <CommandModifyQueueEntry>
    <Header DeviceID="TestSender" ID="C1" Time="2019-03-26T14:07:48.342+00:00"/>
    <ModifyQueueEntryParams Operation="Resume">
      <QueueFilter JobID="j1"/>
    </ModifyQueueEntryParams>
  </CommandModifyQueueEntry>
</XJMF>
```

**Response:**
```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
  <Header DeviceID="DeviceID" ID="l_000002" Time="2019-03-26T14:07:48.601+00:00"/>
  <ResponseModifyQueueEntry ReturnCode="0">
    <Header DeviceID="DeviceID" ID="R1" Time="2019-03-26T14:07:48.601+00:00" refID="C1"/>
    <QueueEntry Activation="Active" JobID="j1" QueueEntryID="QE1" Status="Waiting"/>
  </ResponseModifyQueueEntry>
</XJMF>
```

---

## 7.10 Notification

`Notification` messages are generally sent as Signals. `QueryNotification` is defined to allow subscriptions for `Notification` messages. `Notification` elements MAY be used to signal usual events due to any activities of a Device, operator, etc. (e.g., scanning a bar code).

### 7.10.1 QueryNotification

#### 7.10.1.1 NotificationFilter

**Table 7.33: QueryNotification Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@Languages?`<br>*(New in XJDF 2.2)* | languages | See `Query/@Languages`. |
| `Header` | element | See `Message/Header`. |
| `Subscription?` | element | See `Query/Subscription`. |
| `NotificationFilter?` | element | Defines the types of `Notification` elements that SHALL be signaled. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.34: NotificationFilter Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@Classes?` | enumerations | Defines the set of `Notification/@Class` types to be queried/subscribed for. If `@Classes` is not specified then all `Notification` classes are queried or subscribed to.<br>Allowed values are from: *Severity*. |
| `@MilestoneTypes?` | NMTOKENS | Matching milestone types SHALL be returned and/or subscribed to. If `@MilestoneTypes` is not specified then all supported milestone values are queried or subscribed to.<br>Values include those from: *Milestones*. |

### 7.10.2 ResponseNotification

**Table 7.35: ResponseNotification Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | `Notification` that describes the event. See `Notification`, and also `Response/Notification`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

### 7.10.3 SignalNotification

`SignalNotification` SHOULD be used to send milestone events.
Machine events SHOULD be provided in the context of a `SignalStatus` or `SignalResource` in order to provide additional context of the event such as counter values and job identifiers and SHOULD NOT be sent in `SignalNotification` messages.

**Table 7.36: SignalNotification Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ChannelMode?` | enumeration | Allowed value is from: *ChannelMode*.<br>*Note: See Signal/@ChannelMode.* |
| `Header` | element | See `Message/Header`. |
| `Notification` | element | `Notification` that describes the event. See Section 7.10 Notification. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

---

## 7.11 PipeControl

`CommandPipeControl` modulates a flow of resources in a pipe. The type of pipe operation SHALL be specified in `PipeParams/@Operation`. A pipe describes the consumption of a resource (by a consuming Device) that commences when some lesser quantity of the resource becomes available without having to wait for the production Device to complete production of the entire quantity.
See Section 9.3.5 Overlapping Processing for a more detailed discussion regarding the use of `PipeControl` messages.

### 7.11.1 CommandPipeControl

#### 7.11.1.1 PipeParams

**Table 7.37: CommandPipeControl Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `PipeParams` | element | Details of the `PipeControl` message. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.38: PipeParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@JobID` | NMTOKEN | Specifies `XJDF/@JobID` of the process at the receiving end. |
| `@JobPartID?` | NMTOKEN | Specifies `XJDF/@JobPartID` of the process at the receiving end. |
| `@Operation` | enumeration | `@Operation` specifies whether the flow is being pushed or pulled.<br>Allowed values are:<br>**Close** – The `PipeControl` is a request to the other end of a dynamic pipe that the sender of this message needs no further resources or will produce no further resources through the pipe.<br>**Pause** – The `PipeControl` is a request to the other end of a dynamic pipe that the sender of this message can currently not process resources through the pipe.<br>**Pull** – The `PipeControl` is a request to the producer to create resources by the consumer of the resources.<br>**Push** – The `PipeControl` is a notification to the consumer that resources have been created by the producer of the resources. |
| `@PipeID` | NMTOKEN | `ResourceSet/Dependent/@PipeID` at the receiving end. `@PipeID` SHALL be unique in the scope of the job that is selected by `@JobID`. |
| `MISDetails?` | element | Definition of how the costs for the production of the Resource SHALL be charged. |
| `ResourceSet?` | element | Updated `ResourceSet` that SHALL be used by the process that receives the `PipeControl` command. |

### 7.11.2 ResponsePipeControl

`ResponsePipeControl/@ReturnCode` SHALL be set to `"0"` if the `CommandPipeControl` has been accepted by the receiver. If not successful the `@ReturnCode` SHALL be set to one of the codes shown in Appendix A.4.2 Return Codes.

`@ReturnCode="0"` only specifies that the `CommandPipeControl` has been received and can be processed. Any problems that occur during processing of the resources and that lead to an interruption of the pipe SHALL be communicated with the appropriate `CommandPipeControl` messages.

**Table 7.39: ResponsePipeControl Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

---

## 7.12 QueueStatus

`QueueStatus` returns a description of the current state of a Queue.

### 7.12.1 QueryQueueStatus

#### 7.12.1.1 QueueStatusParams

**Table 7.40: QueryQueueStatus Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `Subscription?` | element | See `Query/Subscription`. |
| `QueueStatusParams` | element | `QueueStatusParams` defines a filter for the `QueueStatus` message. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.41: QueueStatusParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@UpdateGranularity?` | enumeration | Specifies whether all or only the updated `QueueEntry` elements should be included in the Queue.<br>Allowed value is from: *UpdateGranularity*.<br>*Note: The first instance of a Signal shall result in the Queue describing all jobs.* |
| `QueueFilter?` | element | Filter that selects the `QueueEntry` elements that SHALL be returned in the `Queue` element of the response. |

### 7.12.2 ResponseQueueStatus

**Table 7.42: ResponseQueueStatus Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `Queue?` | element | Describes the status of the queue. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

### 7.12.3 SignalQueueStatus

**Table 7.43: SignalQueueStatus Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ChannelMode?` | enumeration | Allowed value is from: *ChannelMode*.<br>*Note: See Signal/@ChannelMode.* |
| `Header` | element | See `Message/Header`. |
| `Queue` | element | Describes the status of the queue. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

### 7.12.4 Queue

**Table 7.44: Queue Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@MaxQueueSize?` | integer | The maximum number of `QueueEntry` elements excluding `QueueEntry[@Status="Completed"]` or `QueueEntry[@Status="Aborted"]` elements that can be contained in the Queue. |
| `@QueueSize?` | integer | The total number of `QueueEntry` elements that are in the Queue regardless of the settings in the `QueueFilter`. Thus the value of `@QueueSize` may be higher than the number of `QueueEntry` elements. |
| `@UpdateGranularity?` | enumeration | Specifies whether all or only the updated `QueueEntry` elements are included in the Queue.<br>Allowed value is from: *UpdateGranularity*.<br>*Note: The first instance of a Signal shall result in the Queue describing all jobs.* |
| `QueueEntry*` | element | Each queue entry that was selected by `QueueFilter` SHALL be provided as an individual `QueueEntry` element. The entries SHALL be ordered in the sequence they have been or will be executed, beginning with the running entries, followed by the waiting entries, highest `QueueEntry/@Priority` first, which are then followed by the completed entries, sorted beginning with the youngest `QueueEntry/@EndTime`.<br>A `QueueEntry` is not automatically deleted when executed or aborted, but rather it remains in the Queue and its `@Status` is changed to `"Completed"` or `"Aborted"` accordingly. |

---

## 7.13 RequestQueueEntry

This command requests a new queue entry from a potential submitting Controller. The actual submission is still handled by `CommandSubmitQueueEntry`.
*Note: This command is emitted from the Device that is represented by the queue to a Controller or Device and not to the queue, as is the case with most other queue handling commands.*
Whereas XJDF generally assumes a "Push" workflow, where a Controller or MIS assigns a task to a given Device, `RequestQueueEntry` allows a "Pull" workflow to be implemented, where a Device with free processing capabilities dynamically requests a new task.

### 7.13.1 CommandRequestQueueEntry

#### 7.13.1.1 RequestQueueEntryParams

**Table 7.45: CommandRequestQueueEntry Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `RequestQueueEntryParams` | element | Defines the specifics for the requested job. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.46: RequestQueueEntryParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@Activation?` | enumeration | Specifies the activation of the requested `QueueEntry`.<br>Allowed value is from: *Activation*. |
| `@JobID?` | NMTOKEN | `@JobID` of the requested `QueueEntry`. |
| `@JobPartID?` | NMTOKEN | `@JobPartID` of the requested `QueueEntry`. |
| `@QueueURL` | URL | URL of the Queue Device that is requesting the `QueueEntry` and will accept Queue manipulation messages. |
| `Part*` | element | Partition parts of the requested `QueueEntry`. |

### 7.13.2 ResponseRequestQueueEntry

The response to this message contains no element that is special for this message.

**Table 7.47: ResponseRequestQueueEntry Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

---

## 7.14 Resource

The `Resource` message can be a `Command` message or a `Query` message to modify or to query XJDF resources.
`QueryResource` retrieves information about the resources without modifying them, whereas `CommandResource` modifies those settings within the resource that is specified.

### 7.14.1 QueryResource

The `QueryResource` message retrieves information about resources and can be made selective by specifying a `ResourceQuParams` element. `QueryResource` can be used to retrieve information about either job-specific or global Devices resources.

#### 7.14.1.1 ResourceQuParams

**Table 7.48: QueryResource Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@Languages?`<br>*(New in XJDF 2.2)* | languages | See `Query/@Languages`. |
| `Header` | element | See `Message/Header`. |
| `Subscription?` | element | See `Query/Subscription`. |
| `ResourceQuParams` | element | Specifies the resources queried. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.49: ResourceQuParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ExternalID?` | NMTOKEN | `Resource/@ExternalID` of the resource that is queried. |
| `@JobID?` | NMTOKEN | `XJDF/@JobID` for which resource information is being queried. If no `@JobID` is specified, the request applies to the currently running process or global resources, depending on the value of `@Scope`.<br>`@JobID` SHALL NOT be specified if `QueryResource/Subscription` is present. |
| `@JobPartID?` | NMTOKEN | `XJDF/@JobPartID` for which resource information is being queried. If no `@JobPartID` is specified, all resources related to `@JobID` are queried. `@JobPartID` SHALL NOT be specified if `@JobID` is not specified.<br>`@JobPartID` SHALL NOT be specified if `QueryResource/Subscription` is present. |
| `@QueueEntryID?` | NMTOKEN | `QueueEntry/@QueueEntryID` of the process that is currently being executed for which resource information is being queried. If `@QueueEntryID` is specified, `@JobID`, `@JobPartID` and `Part` SHALL NOT be specified. If none of `@JobID`, `@JobPartID`, `Part` or `@QueueEntryID` are specified, `ResourceQuParams` applies to all jobs. `@QueueEntryID` SHALL NOT be specified if `QueryResource/Subscription` is present. |
| `@ResourceDetails?` | enumeration | `@ResourceDetails` refines the level of information provided about the resources.<br>Allowed values are:<br>**Brief** – `ResourceInfo/ResourceSet` SHALL NOT contain the explicit resource elements as requested by `@ResourceName`.<br>**Full** – `ResourceInfo/ResourceSet` SHALL contain the explicit resource elements as requested by `@ResourceName`. |
| `@ResourceName?` | NMTOKEN | If specified, `ResourceInfo/ResourceSet/@Name` SHALL match `@ResourceName`.<br>Values include those from: *Chapter 6 Resources*. |
| `@Scope` | enumeration | Specifies whether the `Response` or `Signal` SHALL return a complete list of all known resources, or the currently loaded resources or the resources related to a specific job.<br>Allowed value is from: *Scope*. |
| `@Types?`<br>*(New in XJDF 2.2)* | NMTOKENS | Filter for `@Types` of the XJDF for which resource information is being queried.<br>If at least one value in `ResourceQuParams/@Types` is present in the corresponding `XJDF/@Types`, the resource information of that process SHALL be provided. |
| `Part*` | element | `Part` elements that describe the resource for which resource information is being queried.<br>`Part` SHALL NOT be specified if `QueryResource/Subscription` is present. |

### Example 7.5: Resource Query about Paper
The following is an example of an MIS sending a `QueryResource` to another MIS to get information on all paper known by the press.
```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
  <Header DeviceID="TestSender" ID="l_000002" Time="2019-03-26T14:07:48.454+00:00"/>
  <QueryResource>
    <Header DeviceID="TestSender" ID="Q1" Time="2019-03-26T14:07:48.455+00:00"/>
    <ResourceQuParams ResourceDetails="Full" ResourceName="Media" Scope="Allowed"/>
  </QueryResource>
</XJMF>
```

### 7.14.2 CommandResource

*(Modified in XJDF 2.2)* The `CommandResource` message SHALL be used to modify or create global Device databases such as media catalogs or lists of known Machine operators.
*Note: For modifications of job specific resources see `CommandResubmitQueueEntry`.*

If the `CommandResource` cannot be completely applied, the behavior of the Device is implementation dependent. The Device MAY either reject the entire `CommandResource` or partially apply the `CommandResource` in an implementation dependent manner.
Partial application of the `CommandResource` SHOULD also be flagged as a warning with `Notification[@Class="Warning"` and `@Type="Error"]`. If the value of `@ReturnCode` is larger than `"0"`, the Controller that issued the command SHOULD evaluate the returned resource in order to find the setting that could not be applied.
**Modification note:** The behavior of incomplete modifications has been clarified in XJDF 2.2.

#### 7.14.2.1 ResourceCmdParams

**Table 7.50: CommandResource Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `ResourceCmdParams` | element | `ResourceCmdParams` SHALL specify the resources to be modified and the update method. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.51: ResourceCmdParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@UpdateMethod`<br>*(Modified in XJDF 2.2)* | enumeration | `@UpdateMethod` specifies how the `ResourceSet` SHALL be updated.<br>Allowed values are:<br>**Complete** – Any resource selected by a `Part` or identified by `@ExternalID` SHALL be completely overwritten with the matching `Resource` from the `ResourceSet` in this message. Any `Resource` not selected by a `Part` or identified by `@ExternalID` SHALL not be modified. If no `Resource` is selected by a `Part` or identified by `@ExternalID`, the `Resource` SHALL be created. If a `Resource` does not exist, it SHALL be created.<br>**CompleteSet** - The entire `ResourceSet` selected by this command SHALL be replaced by the `ResourceSet` this message contains. If a `ResourceSet` does not exist, it SHALL be created.<br>**Incremental** – Any `Resource` selected by a `Part` or identified by `@ExternalID` SHALL be incrementally updated with values from the matching `Resource` of the `ResourceSet` in this message whereby all Traits SHALL be added to the original `Resource`, replacing any previously existing matching Traits. Individual items not matched SHALL not be modified or removed. If a `Resource` does not exist, it SHALL be created.<br>**Remove** – Any `Resource` selected by `Part` or identified by `@ExternalID` SHALL be completely removed from the `ResourceSet`. All other `Resources` SHALL NOT be modified or removed. If a `Resource` does not exist, it SHALL NOT be created and the behavior is implementation specific.<br>**RemoveSet** – The entire `ResourceSet` selected by this message SHALL be removed.<br>**Modification note:** The behavior of modifications of non-existing resources has been clarified in XJDF 2.2. |
| `ResourceSet` | element | `ResourceSet` SHALL define the resources that are modified on the Device according to the policy specified in `@UpdateMethod`. `ResourceSet/Resource/@ExternalID` SHOULD be used to uniquely identify each individual resource. |

### Example 7.7: Resource Command: Uploading a list of paper Media
The following is an example of an MIS uploading a paper catalog to a Device.
```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
  <Header DeviceID="TestSender" ID="l_000002" Time="2019-03-26T14:07:48.181+00:00"/>
  <CommandResource>
    <Header DeviceID="TestSender" ID="C1" Time="2019-03-26T14:07:48.181+00:00"/>
    <ResourceCmdParams UpdateMethod="Incremental">
      <ResourceSet Name="Media">
        <Resource DescriptiveName="Paper# 1" ExternalID="ID_1">
          <Media Dimension="595.27559055 822.04724409" MediaType="Paper" Weight="80"/>
        </Resource>
        <Resource DescriptiveName="Paper# 2" ExternalID="ID_2">
          <Media Dimension="595.27559055 822.04724409" MediaType="Paper" Weight="100"/>
        </Resource>
        <!-- One Resource element for each paper to upload follows here -->
      </ResourceSet>
    </ResourceCmdParams>
  </CommandResource>
</XJMF>
```

### 7.14.3 ResponseResource

When responding to `QueryResource` or `CommandResource`, `ResponseResource` returns a `ResourceInfo` that SHALL contain the queried information concerning the resources.

#### 7.14.3.1 ResourceInfo
*(Modified in XJDF 2.1)* `ResourceInfo` SHALL specify the current state of a `ResourceSet` after any applicable modifications have been applied.

**Table 7.52: ResponseResource Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`.<br>If `ResourceInfo/ResourceSet` is empty because the selective query parameters of the `ResourceQuParams` lead to a null selection of the known Device or job resources, then `@ReturnCode` SHALL be one of `"103"` (`@JobID` unknown), `"104"` (`@JobPartID` unknown) or `"108"` (empty list) and SHOULD be flagged as an error with `Notification[@Class="Error"]`.<br>When responding to an unsuccessful `CommandResource`, the value of `@ReturnCode` SHALL be set to `"140"` (`CommandResource` rejected).<br>If the data could only be partially updated `ResponseResource/@ReturnCode` SHALL be `"141"`. See Appendix A.4.2 Return Codes. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `ResourceInfo*` | element | **Response to a `CommandResource`:** Exactly one `ResourceInfo` SHALL be specified and SHALL contain information about the `ResourceSet` after modification.<br>**Response to a `QueryResource`:** One `ResourceInfo` SHALL be specified for each `ResourceSet` that matches the `QueryResource` and SHALL contain information about the `ResourceSet`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.53: ResourceInfo Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@CommandResult?` | enumeration | Result of a `CommandResource`.<br>Allowed values are:<br>**Merged** – Values from the `ResourceSet` in `ResourceCmdParams` were merged into an existing `ResourceSet`. See the `ResourceInfo/ResourceSet` for the merged result.<br>**New** – A new `ResourceSet` with the values specified in `ResourceCmdParams` was created.<br>**Rejected** – The `CommandResource` was not applied to this `ResourceSet`.<br>**Removed** – An existing `ResourceSet` was removed completely by a `ResourceSet` specified in `ResourceCmdParams`.<br>**Replaced** – An existing `ResourceSet` was replaced completely by a `ResourceSet` specified in `ResourceCmdParams`. |
| `@JobID?` | NMTOKEN | `@JobID` specifies the `@JobID` of the job that this `ResourceInfo` applies to. |
| `@JobPartID?` | NMTOKEN | `@JobPartID` species the `@JobPartID` of the work step that this `ResourceInfo` applies to. |
| `@Level?` | enumeration | Level of the consumable or output bin that is represented by this `ResourceInfo` for the Device. If specified, exactly one `ResourceSet/Resource` SHALL be present.<br>Allowed values are:<br>**Empty** – The bin is empty.<br>**Full** - The bin is full.<br>**High** - The output bin is filling up and can soon be `Full`. This value is for output levels only and SHOULD NOT be specified for input resources.<br>**Low** – The resources are running low and can soon be `Empty`. This value is for input levels only and SHOULD NOT be specified for output bins.<br>**OK** – Specification is left to the Device manufacturer. |
| `@ModuleID?`<br>*(New in XJDF 2.1)* | NMTOKEN | Identifier of the module in a multi-functional Device that this `ResourceInfo` applies to. |
| `@QueueEntryID?` | NMTOKEN | `@QueueEntryID` specifies the `@QueueEntryID` of the queue entry that this `ResourceInfo` applies to. |
| `@Scope?` | enumeration | `@Scope` specifies the context of the resources defined in this `ResourceInfo`.<br>Allowed value is from: *Scope*. |
| `@Speed?` | float | The current speed at which the resource that this `ResourceInfo` describes is being consumed or produced. `@Speed` SHALL be defined in the units specified by `ResourceSet/@Unit/hour`. If specified, exactly one `ResourceSet/Resource` SHALL be present. |
| `@TotalAmount?` | float | `@TotalAmount` specifies the job independent total counter setting for a given type of resource.<br>*Note: This allows tracking of power consumption without requiring a Device to track it individually for each job.* |
| `@Types?`<br>*(New in XJDF 2.1)* | NMTOKENS | `@Types` SHALL specify a list of one or more process names.<br>`@Types` is required when `ResourceInfo` contains a `ResourceSet` with a `@CombinedProcessIndex` which is used to identify the subset of processes that use the `ResourceSet`. The subset is taken from the complete set described by the XJDF (referred to as the parent XJDF below) to which this `ResourceInfo` relates.<br>Usage constraints:<br>a. `@Types` SHALL be a copy of `XJDF/@Types` of the parent XJDF.<br>b. `ResourceInfo/ResourceSet/@CombinedProcessIndex` SHALL reference a value in `ResourceInfo/@Types`. |
| `Event*`<br>*(New in XJDF 2.1)* | element | `Event` MAY be used to specify Machine-dependent codes that triggered a `SignalResource` or `AuditResource`. |
| `MISDetails?` | element | Definition of how the costs for the production of the Resource SHALL be charged. |
| `ResourceSet` | element | Additional information about the resource. If the `QueryResource` or `CommandResource` leading to this `ResourceInfo` contains `Part` elements, the `ResourceSet` SHALL contain no more than the appropriate matching `Resource` elements. Unless `@Speed` or `@Level` are present, `ResourceInfo` SHOULD contain all matching `Resource` elements. |

### Example 7.6: Resource Response about Paper
The following is an example of a `ResponseResource` sent in response to the previous `QueryResource`.
```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
  <Header DeviceID="DeviceID" ID="l_000002" Time="2019-03-26T14:07:48.552+00:00"/>
  <ResponseResource>
    <Header DeviceID="DeviceID" ID="R1" Time="2019-03-26T14:07:48.553+00:00" refID="Q1"/>
    <ResourceInfo Scope="Allowed">
      <ResourceSet Name="Media">
        <Resource DescriptiveName="Paper# 1" ExternalID="ID_1">
          <Media Dimension="595.27559055 822.04724409" MediaType="Paper" Weight="80"/>
        </Resource>
        <Resource DescriptiveName="Paper# 2" ExternalID="ID_2">
          <Media Dimension="595.27559055 822.04724409" MediaType="Paper" Weight="100"/>
        </Resource>
        <!-- One Resource element for each paper follows here -->
      </ResourceSet>
    </ResourceInfo>
  </ResponseResource>
</XJMF>
```

### 7.14.4 SignalResource

`SignalResource` returns a `ResourceInfo` that contains the information concerning the subscribed resources.

**Table 7.54: SignalResource Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ChannelMode?` | enumeration | Allowed value is from: *ChannelMode*.<br>*Note: See Signal/@ChannelMode.* |
| `@ReplaceAfter?` | DateTime | The data from previous `SignalResource` messages in the same scope as specified by `ResourceInfo/@Scope` with the same `Header/@DeviceID` and a `Header/@Time` value after the time `@ReplaceAfter` and prior to the time specified by `@ReplaceBefore` (if present), SHALL be replaced by data in this `SignalResource`.<br>If neither `@ReplaceAfter` or `@ReplaceBefore` is specified, this `SignalResource` is the original and SHALL NOT replace a previous `SignalResource`. |
| `@ReplaceBefore?` | DateTime | The data from previous `SignalResource` messages in the same scope as specified by `ResourceInfo/@Scope` with the same `Header/@DeviceID` and a `Header/@Time` value prior to the time `@ReplaceBefore` and after the time specified by `@ReplaceAfter` (if present), SHALL be replaced by data in this `SignalResource`.<br>If neither `@ReplaceAfter` or `@ReplaceBefore` is specified, this `SignalResource` is the original and SHALL NOT replace a previous `SignalResource`. |
| `Header` | element | See `Message/Header`. |
| `ResourceInfo*` | element | `ResourceInfo` SHALL contain information concerning the subscribed resources. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

### Example 7.8: Resource Signal about Consumed Resources
The following is an example of a Resource signal used to report the consumption of paper Media.
```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
  <Header DeviceID="DeviceID" ID="l_000002" Time="2019-03-26T14:07:48.698+00:00"/>
  <SignalResource>
    <Header DeviceID="DeviceID" ID="S1" Time="2019-03-26T14:07:48.698+00:00" refID="Sub1"/>
    <ResourceInfo JobID="Job1" JobPartID="Printing" Scope="Job">
      <ResourceSet Name="Media" Usage="Input">
        <Resource ExternalID="MIS-ID">
          <AmountPool>
            <PartAmount Amount="4500" Waste="66">
              <Part LotID="Lot1"/>
            </PartAmount>
            <PartAmount Amount="2200" Waste="22">
              <Part LotID="Lot2"/>
            </PartAmount>
          </AmountPool>
          <Part SheetName="S1"/>
        </Resource>
      </ResourceSet>
    </ResourceInfo>
  </SignalResource>
</XJMF>
```

---

## 7.15 ResubmitQueueEntry

A `QueueEntry` is resubmitted to a queue using the `ResubmitQueueEntry` message. This allows late changes to be made to an XJDF without affecting `QueueEntry` elements and their positions in a Queue. Resubmission modifies the XJDF with information specified in `ResubmissionParams/@URL`. If `QueueEntry/@Status` is neither `"Waiting"` nor `"Held"`, resubmitting a queue entry MAY fail because a Device NEED NOT implement `ResubmitQueueEntry` for running queue entries.
*Note: See the `ModifyQueueEntry` command to modify the `QueueEntry` without modifying the underlying XJDF job.*

### 7.15.1 CommandResubmitQueueEntry

#### 7.15.1.1 ResubmissionParams
`ResubmissionParams` provides details of the `QueueEntry` resubmission. The value of `ResubmissionParams/@UpdateMethod` determines how the `QueueEntry` modification SHALL be applied.
Devices NEED NOT support `@UpdateMethod="Incremental"` with variable `XJDF/@JobPartID`. This feature allows MIS to provide complex workflows to production workflow systems that are capable of managing multiple Devices.

**Table 7.55: CommandResubmitQueueEntry Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `ResubmissionParams` | element | Defines the job resubmission. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.56: ResubmissionParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@QueueEntryID` | NMTOKEN | `QueueEntry/@QueueEntryID` of the `QueueEntry` that is resubmitted SHALL match `QueueEntry/@QueueEntryID` of the queue entry to be replaced. |
| `@UpdateMethod` | enumeration | `@UpdateMethod` specifies how the `QueueEntry` SHALL be updated.<br>Allowed values are:<br>**Complete** – The `QueueEntry` SHALL be completely replaced by the XJDF that is referenced by `@URL`.<br>**Incremental** – The `QueueEntry` SHALL be incrementally updated by the values of the XJDF that is referenced by `@URL`. All Traits of the referenced XJDF are optional and only values that are explicitly specified in the referenced XJDF SHALL be modified. If `XJDF/@JobPartID` of the referenced XJDF is identical to `XJDF/@JobPartID` of the originally submitted XJDF or an XJDF that has been resubmitted with `ResubmissionParams/@UpdateMethod="Incremental"`, then the process step that is identified by `XJDF/@JobID` and `XJDF/@JobPartID` SHALL be modified. Otherwise a new process step SHALL be submitted to the Device.<br>**Remove** – The `QueueEntry` SHALL be incrementally updated by removing all elements that are specified in the XJDF that is referenced by `@URL`. `XJDF/@JobPartID` of the referenced XJDF SHALL be identical to `XJDF/@JobPartID` of the originally submitted XJDF or an XJDF that has been resubmitted with `ResubmissionParams/@UpdateMethod="Incremental"`. |
| `@URL` | URL | Location of the XJDF to be submitted. `XJDF/@JobID` SHALL be identical to `XJDF/@JobID` of the originally submitted XJDF.<br>If `@URL` refers to a directory, then all files with an extension of `.xjdf` that reside directly in the directory SHALL be processed in lexical order. The first XJDF is referred to as the primary XJDF. See Chapter 9 Referencing Multiple XJDF in a Directory.<br>*Note: A referenced directory MAY be inside a zip package. See [ZIP] for details.* |

### 7.15.2 ResponseResubmitQueueEntry

**Table 7.57: ResponseResubmitQueueEntry Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

---

## 7.16 ReturnQueueEntry

The `ReturnQueueEntry` message SHALL return an XJDF that had been submitted with a `SubmitQueueEntry` to the Controller that originally submitted the XJDF. `ReturnQueueEntry` SHALL be sent for all queue entries that have been completed or aborted if `QueueSubmissionParams/@ReturnJMF` has been specified. This also applies to queue entries that have been removed prior to processing. If `ReturnQueueEntry` is sent for a `QueueEntry` that has been removed prior to processing, the value of `XJDF/NodeInfo/@Status` SHALL be `"Aborted"`.
*Note: This command is sent from the Device to a Controller and not from Controller to Device as is the case with most other queue handling commands.*

If the XJDF has been enhanced by submitting additional process XJDFs with different `XJDF/@JobPartID` using the `ResubmitQueueEntry` command, then only the primary XJDF SHALL be returned. The audit elements of the process XJDFs SHALL be copied into `XJDF/AuditPool` of the primary XJDF. Each such audit element SHALL contain a copy of `XJDF/@JobPartID`.

### 7.16.1 CommandReturnQueueEntry

#### 7.16.1.1 ReturnQueueEntryParams
The `@URL` attribute specifies the location where the XJDF file to be returned can be retrieved by the Controller. The scheme of the `@URL` attribute (such as `"file"`, `"http"` or local url) SHALL define the retrieval method to be used to retrieve the XJDF.

**Table 7.58: CommandReturnQueueEntry Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `ReturnQueueEntryParams` | element | Defines the job being returned from Device to Controller after processing is completed or aborted. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.59: ReturnQueueEntryParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@QueueEntryID` | NMTOKEN | `QueueEntry/@QueueEntryID` of the returned queue entry. |
| `@URL` | URL | Location of the XJDF that represents the final state of the `QueueEntry` to be returned. `URL` SHALL NOT reference a directory. |

### 7.16.2 ResponseReturnQueueEntry

**Table 7.60: ResponseReturnQueueEntry Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

---

## 7.17 ShutDown

The `ShutDown` command message shuts down a Controller or Device. A Device SHALL use the `Status` message if it signals its own shutdown.

### 7.17.1 CommandShutDown

#### 7.17.1.1 ShutDownCmdParams

**Table 7.61: CommandShutDown Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `ShutDownCmdParams?` | element | Defines the details of a shutdown. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.62: ShutDownCmdParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ShutDownType?` | enumeration | Defines the Device shutdown method.<br>Allowed values are:<br>**Full** – Completely shut down the Device. It is no longer accessible via XJMF after the shutdown.<br>**StandBy** – The Device is set to standby mode. It can be restarted using a `CommandWakeUp` XJMF message. `DeviceInfo/@StatusDetails` SHOULD be `"StandBy"`. If the Device requires a `CommandWakeUp` XJMF prior to accepting new jobs, `DeviceInfo/@Status` SHALL be `"Offline"`, else it SHALL be `"Idle"`. |

### 7.17.2 ResponseShutDown

**Table 7.63: ResponseShutDown Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

---

## 7.18 Status

The `Status` message queries the general status of a Device or a Controller and the status of jobs associated with this Device or Controller. No job context is needed to issue a `Status` message. The response SHOULD contain a `DeviceInfo` element that contains the job-independent details of the Device or Controller and which MAY contain `JobPhase` elements that in turn contain the job specific information.

### 7.18.1 QueryStatus

#### 7.18.1.1 StatusQuParams
`StatusQuParams` is a filter that defines the job context for which information SHALL be returned in the response.

**Table 7.64: QueryStatus Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@Languages?`<br>*(New in XJDF 2.2)* | languages | See `Query/@Languages`. |
| `Header` | element | See `Message/Header`. |
| `Subscription?` | element | See `Query/Subscription`. |
| `StatusQuParams?` | element | Acts as a filter to select the context of the Device or Controller that should be reported in the response messages’ `DeviceInfo` element. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.65: StatusQuParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@JobID?`<br>*(New in XJDF 2.2)* | string | `@JobID` of the XJDF node whose status is being queried. The `@JobID` SHALL be unique within the workflow. If not specified, list all known jobs.<br>`@JobID` SHALL NOT be specified if `QueryStatus/Subscription` is present. |
| `@JobPartID?`<br>*(New in XJDF 2.2)* | string | `@JobPartID` of the XJDF node whose status is being queried. `@JobPartID` SHALL NOT be specified if `@JobID` is not specified.<br>`@JobPartID` SHALL NOT be specified if `QueryStatus/Subscription` is present. |
| `@QueueEntryID?` | NMTOKEN | `@QueueEntryID` of the queue entry that is being queried. If `@QueueEntryID` is not specified, information SHALL be provided for all jobs (that are currently active) and job independent status. `@QueueEntryID` SHALL NOT be specified if `QueryStatus/Subscription` is present. |
| `@Types?`<br>*(New in XJDF 2.2)* | NMTOKENS | Filter for `@Types` of the XJDF whose status is being queried. If at least one value in `StatusQuParams/@Types` is present in the corresponding `XJDF/@Types`, the status of that process SHALL be provided. |
| `Part*`<br>*(New in XJDF 2.2)* | element | `Part` elements that describe the partition of the job whose status is being queried.<br>`Part` SHALL NOT be specified if `QueryStatus/Subscription` is present. |

### 7.18.2 ResponseStatus

**Table 7.66: ResponseStatus Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `DeviceInfo?` | element | `DeviceInfo` describes details of the actual Device status. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

#### 7.18.2.1 DeviceInfo
The response message returns a `DeviceInfo` element for the queried Device. `Header/@DeviceID` SHALL specify the Device that `DeviceInfo` describes.

**Table 7.67: DeviceInfo Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@CounterUnit?` | NMTOKEN | The unit of the `@ProductionCounter`, the `@TotalProductionCounter` and numerator unit of `@Speed`.<br>The default unit is the default unit defined by XJDF for the output resource of the node executed by the Device. For example, in case of a sheet-fed printer, it is the number of sheets; in case of a web printer, it is the length of printed web in meters.<br>Values include those from: *Units*. |
| `@EndTime?`<br>*(New in XJDF 2.1)* | dateTime | `@EndTime` SHALL specify the end time of a Device status and SHALL be specified when the Device status changes. A Device status changes when the subsequent `DeviceInfo` that describes the same Device has a different `@Status` or `@StatusDetails`. `@EndTime` SHALL NOT be specified in a Heartbeat signal. |
| `@HourCounter?` | duration | The total integrated time (life time) of Device operation in hours. |
| `@IdleStartTime?` | dateTime | `@IdleStartTime` SHALL specify the time when the Device switched to either `@Status="Idle"`, `@Status="Offline"` or `@Status="NonProductive"`. `@IdleStartTime` SHALL NOT be specified if `@Status="Production"` or `@Status="Stopped"`. |
| `@ModuleIDs?` | NMTOKENS | `@ModuleIDs` SHALL reference the values of `Device/Module/@ModuleID` of individual modules that are in use independent of a job. `@ModuleIDs` SHALL not be specified for modules that are specified in `Status/JobPhase/@ModuleIDs`. |
| `@PowerOnTime?` | dateTime | Date and time when the Device was switched on. |
| `@ProductionCounter?` | float | The current Machine production counter. This counter can be reset manually.<br>Typically, it starts counting at power-on time. The reset of this counter MAY be signaled by a `SignalNotification` with a `Notification/Event/@EventValue="CounterReset"`. The value of `Event/@EventID` for a counter reset is implementation specific. See Section 8.18 Event. |
| `@Speed?` | float | `@Speed` specifies the current Machine speed. `@Speed` SHALL be defined in the same units as `@CounterUnit` per hour. |
| `@Status` | enumeration | `@Status` describes the overall status of the Device.<br>Allowed value is from: *DeviceStatus*. |
| `@StatusDetails?` | NMTOKEN | String that defines the Device state more specifically.<br>Values include those from: *Status Details*. |
| `@ToolIDs?`<br>*(New in XJDF 2.1)* | NMTOKENS | `@ToolIDs` SHALL reference the values of `ResourceSet[@Name="Tool"]/Resource/@ExternalID` of individual tools that are in use independent of a job.<br>`@ToolIDs` SHALL NOT be specified for tools that are specified in `JobPhase/@ToolIDs`. |
| `@TotalProductionCounter?` | float | The current total Machine production counter since the Machine was produced. |
| `Activity*` | element | Device and operator activities that are related to the Device and are unrelated to a specific job. |
| `Event*`<br>*(New in XJDF 2.1)* | element | `Event` MAY be used to specify Machine-dependent codes that triggered a `SignalStatus` or `AuditStatus`. |
| `FileSpec(CurrentSchema)?`<br>*(Deprecated in XJDF 2.1)* | element | Reference to an XML schema in XSD format [XMLSchema] that describes the present limitations of the Device that can be used without operator intervention. The referenced XML schema SHALL use the XJDF namespace to describe elements and attributes that are defined in the XJDF namespace.<br>**Deprecation note:** Use `KnownDevices` to locate any Device specific schema. |
| `FileSpec(Schema)?`<br>*(Deprecated in XJDF 2.1)* | element | Reference to an XML schema in XSD format [XMLSchema] that describes the global limitations of the Device including those that can only be used with operator intervention. The referenced XML schema SHALL use the XJDF namespace to describe elements and attributes that are defined in the XJDF namespace.<br>**Deprecation note:** Use `KnownDevices` to locate any Device specific schema. |
| `JobPhase*` | element | Each `JobPhase` SHALL describe the actual status of a job in the Device. All jobs that are active on the Device SHALL be specified. No `JobPhase` elements SHALL be present if `@Status="Idle"` or `@Status="Offline"`.<br>Multiple `JobPhase` elements specify that multiple job phases are active simultaneously on the Device. |

#### 7.18.2.2 Activity
`Activity` elements allow tracking of Device and operator tasks in addition to the values of the global attributes `@Status` and `@StatusDetails`. An `Activity` SHOULD define a task that has a duration. Singular events SHOULD be specified in `DeviceInfo/Event`.

**Table 7.68: Activity Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ActivityID?` | NMTOKEN | ID of the activity being performed. This ID is unique, site specific and internal to the MIS. |
| `@ActivityName?` | string | Name of the activity being performed. |
| `@EndTime?`<br>*(New in XJDF 2.1)* | dateTime | `@EndTime` SHALL specify the end time of the activity. `@EndTime` SHALL NOT be specified in a Heartbeat signal that defines an ongoing activity. |
| `@PersonalID?` | NMTOKEN | MIS identifier of the employee that performs the activity. |
| `@Roles?`<br>*(New in XJDF 2.1)* | NMTOKENS | Current roles of the operator that is specified in `@PersonalID` in the context of this activity.<br>Values include those from: *Employee Roles*. |
| `@StartTime?` | dateTime | Date and time that the employee started the activity. This value MAY remain the same in multiple messages. |
| `Comment?`<br>*(New in XJDF 2.1)* | element | The text within `Comment` SHALL contain human readable text that relates to the current activity. |

#### 7.18.2.3 JobPhase
`JobPhase` represents the actual state of a job. Any amounts specified in `JobPhase` are cumulated amounts since `@StartTime`.
The main difference between a `JobPhase` element within an XJMF message and an `AuditStatus` is that a `JobPhase` reflects a snapshot of the current job status whereas `AuditStatus` reflects a time span bordered by two status transitions. Events that cause a status transition are shown in Table 7.70 Status Transition Events.
If `Part` elements are specified, all attributes in `JobPhase` apply only to the specified parts.

**Table 7.69: JobPhase Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@Amount?` | float | Total actual amount of good production that the process defined in this `JobPhase` produced since `@StartTime`. If `@Waste` is also specified, the value SHALL be without waste. The unit MAY be specified in the `@CounterUnit` attribute of the parent element `DeviceInfo`. |
| `@CostCenterID?` | NMTOKEN | The cost center that this `JobPhase` job is currently being charged to. |
| `@DeadLine?` | enumeration | Scheduling state of the job.<br>Allowed values are:<br>**InTime** – The job or Job Part will probably not miss the deadline.<br>**Late** – The job or Job Part will miss the deadline.<br>**Warning** – The job or Job Part could miss the deadline.<br>*Note: For more details on scheduling, see NodeInfo.* |
| `@EndTime?`<br>*(New in XJDF 2.1)* | dateTime | `@EndTime` SHALL specify the end time of a `JobPhase` and SHALL be specified when the job status changes. A job status changes when the subsequent `JobPhase` that describes the same job has a different `@Status` or `@StatusDetails` and when the job is completed on the Device. `@EndTime` SHALL NOT be specified in a Heartbeat signal. |
| `@JobID` | NMTOKEN | `XJDF/@JobID` of the process that is executing. |
| `@JobPartID?` | NMTOKEN | `XJDF/@JobPartID` of the process that is executing. |
| `@ModuleIDs?`<br>*(Modified in XJDF 2.2)* | NMTOKENS | `@ModuleIDs` SHALL reference the values of `Device/Module/@ModuleID` of individual modules that are in use in the context of a job. `@ModuleIDs` SHALL NOT be specified for modules that are specified in `Status/ResponseStatus/DeviceInfo/@ModuleIDs`.<br>**Modification note:** In XJDF 2.2 the description of the `@ModuleIDs` attribute has been corrected to reference modules used in the context of a job, previously it was erroneously described as referring to modules used independently of the job. Also clarified that the values refer to modules, previously erroneously described as referring to tools. |
| `@PercentCompleted?`<br>*(Modified in XJDF 2.2)* | float | `JobPhase` processing progress in percent (%) completed. The value of `@PercentCompleted` SHOULD not be higher than 100, even if the value of `@Amount` is higher than the value of `@TotalAmount`.<br>**Modification note:** The scope of `@PercentCompleted` was clarified to be the individual `JobPhase`, and the recommendation was added to not provide values above 100%, in XJDF 2.2. |
| `@QueueEntryID?` | NMTOKEN | If the job was submitted to a Queue and the `@QueueEntryID` is known, this attribute SHOULD be provided. |
| `@RelatedJobID?`<br>*(New in XJDF 2.1)* | string | The `@RelatedJobID` of the XJDF process that is executing. |
| `@RelatedJobPartID?`<br>*(New in XJDF 2.1)* | string | The `@RelatedJobPartID` of the XJDF process that is executing. |
| `@RestTime?` | duration | Estimated duration of time to finishing processing. |
| `@StartTime?`<br>*(Modified in XJDF 2.1)* | dateTime | Time when execution of this `JobPhase` has been started.<br>**Modification note:** The description of `@StartTime` has been corrected to the start of the `JobPhase`, previously it was erroneously described as the start of the execution of the node. |
| `@Status` | enumeration | `@Status` SHALL specify the `NodeInfo/@Status` of the process during this `JobPhase`.<br>Allowed value is from: *Status*. |
| `@StatusDetails?` | NMTOKEN | Machine readable description that defines the job state more specifically.<br>Values include those from: *Status Details*. |
| `@ToolIDs?`<br>*(New in XJDF 2.1)* | NMTOKENS | `@ToolIDs` SHALL reference the values of `ResourceSet[@Name="Tool"]/Resource/@ExternalID` of individual tools that are used to execute this job.<br>`@ToolIDs` SHALL NOT be specified for tools that are specified in `DeviceInfo/@ToolIDs`. |
| `@TotalAmount?`<br>*(New in XJDF 2.2)* | float | The amount that is planned to be produced when this `JobPhase` is 100% completed. The unit is specified in the `@CounterUnit` attribute of the parent element `DeviceInfo`. |
| `@Waste?` | float | Total actual amount of waste that the process defined in this `JobPhase` produced since `@StartTime`. The unit MAY be specified in the `@CounterUnit` attribute of the parent element `DeviceInfo`. |
| `@WorkStepID?`<br>*(New in XJDF 2.1)* | NMTOKEN | If present, `@WorkStepID` SHALL identify the Workstep that is described by this `JobPhase`. If `ResourceSet[@Name="NodeInfo"]/Resource/@ExternalID` is specified, the value SHALL be copied from there; otherwise the value MAY be generated by the Device that is generating the `JobPhase`. |
| `Activity*` | element | Device and operator activities that are related to a specific job or job phase. |
| `GangSource*` | element | If present, each `GangSource` SHALL represent the source jobs that are being processed as a Gang job by this `QueueEntry`. |
| `MISDetails?` | element | Definition of how the costs for this `JobPhase` SHALL be charged. |
| `Part*` | element | `Part` SHALL define which parts are currently being processed. The values should be a subset of `ResourceSet[@Name="NodeInfo"]/Resource/Part` of the XJDF that is processed during this `JobPhase`.<br>For details on partitions, see Section 9.3.3 Partial Processing of XJDF with Partitioned ResourceSets. |

##### 7.18.2.3.1 Status Transition Events
*(New in XJDF 2.2)*

**Table 7.70: Status Transition Events**

| EVENT | MODIFIED TRAIT IN JOBPHASE | EXAMPLE |
| --- | --- | --- |
| A new Process is loaded. | `@JobID` or `@JobPartID` | A new job is printed on a digital press. |
| A new Job Part is loaded. | Any attribute in `Part` | A new press run for a sheet of the same job is started on a conventional press. |
| The job changes status. | `@Status` or `@StatusDetails` | The setup phase on a press is completed and the operator starts the good counter. |
| The Device production speed changes. | `DeviceInfo/@Speed` | The definition of significant speed changes is device dependent.<br>*Note: Minor fluctuations in speed typically are not considered to be status changes.* |

### 7.18.3 SignalStatus

**Table 7.71: SignalStatus Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ChannelMode?` | enumeration | Allowed value is from: *ChannelMode*.<br>*Note: See Signal/@ChannelMode.* |
| `@ReplaceAfter?` | DateTime | The data from previous `SignalStatus` messages with the same `Header/@DeviceID` and a `Header/@Time` after the time specified by `@ReplaceAfter` and prior to the time specified by `@ReplaceBefore` SHALL be replaced by data in this `SignalStatus`. `@ReplaceAfter` SHALL be specified if `@ReplaceBefore` is specified. If `@ReplaceAfter` and `@ReplaceBefore` are not specified, this `SignalStatus` is the original and SHALL NOT replace a previous `SignalStatus`. |
| `@ReplaceBefore?` | DateTime | The data from previous `SignalStatus` messages with the same `Header/@DeviceID` and a `Header/@Time` after the time specified by `@ReplaceAfter` and prior to the time specified by `@ReplaceBefore` SHALL be replaced by data in this `SignalStatus`. `@ReplaceBefore` SHALL be specified if `@ReplaceAfter` is specified. If `@ReplaceBefore` and `@ReplaceAfter` are not specified, this `SignalStatus` is the original and SHALL NOT replace a previous `SignalStatus`. |
| `Header` | element | See `Message/Header`. |
| `DeviceInfo` | element | `DeviceInfo` describes details of the actual Device status. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

### Example 7.9: Status Signal
Example of two XJMF messages with `SignalStatus` elements. The first XJMF contains a Heartbeat `SignalStatus` that was sent at 16:59 while the Device was being setup. The second XJMF contains a `SignalStatus` that was sent at 17:00 as a result of a phase change when `JobPhase/@Status` went from `”Setup”` to `”InProgress”`.

```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
  <Header DeviceID="DeviceID" ID="l_000002" Time="2019-03-26T16:59:00.000+00:00"/>
  <SignalStatus>
    <Header DeviceID="DeviceID" ID="S1" Time="2019-03-26T16:59:00.000+00:00" refID="Sub1"/>
    <DeviceInfo Status="Production">
      <JobPhase JobID="j1" JobPartID="p1" StartTime="2019-03-26T16:00:00.000+00:00" Status="Setup"/>
    </DeviceInfo>
  </SignalStatus>
</XJMF>
```

```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
  <Header DeviceID="DeviceID" ID="l_000004" Time="2019-03-26T17:00:00.000+00:00"/>
  <SignalStatus>
    <Header DeviceID="DeviceID" ID="S2" Time="2019-03-26T17:00:00.000+00:00" refID="Sub1"/>
    <DeviceInfo Status="Production">
      <JobPhase JobID="j1" JobPartID="p1" StartTime="2019-03-26T17:00:00.000+00:00" Status="InProgress"/>
    </DeviceInfo>
  </SignalStatus>
</XJMF>
```

---

## 7.19 StopPersistentChannel

The `StopPersistentChannel` command message unregisters a listening Controller from a persistent channel. No more signal messages are sent to the Controller once the command has been issued. A certain subset of signals MAY be addressed to be unsubscribed by specifying a `StopPersChParams` element.

### 7.19.1 CommandStopPersistentChannel

#### 7.19.1.1 StopPersChParams
*(Modified in XJDF 2.2)* `StopPersChParams` provides a filter which selects persistent channels that SHALL be unregistered. A persistent channel SHALL be removed if all filters provided in `StopPersChParams` match. If no filters are provided, all persistent channels SHALL be removed.
**Modification note:** The behavior of a combination of filters in `StopPersChParams` was clarified in XJDF 2.2.

**Table 7.72: CommandStopPersistentChannel Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `StopPersChParams` | element | Specifies the persistent channel and the message types to be unsubscribed. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.73: StopPersChParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ChannelID?` | NMTOKEN | `Header/@refID` of the persistent channel to be deleted. `@ChannelID` specifies the `Header/@ID` of the `Query` message (identical to the `Header/@refID` of the `Signal` message). |
| `@MessageType?` | NMTOKEN | `@MessageType` SHALL match the local element name (i.e. without namespace prefix) of the `Signal` elements that SHALL be unregistered. |
| `@URL?` | URL | URL of the receiving Controller. This SHALL be identical to the `Subscription/@URL` that was used to create the persistent channel. |

### 7.19.2 ResponseStopPersistentChannel

**Table 7.74: ResponseStopPersistentChannel Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `SubscriptionInfo*` | element | One `SubscriptionInfo` element SHALL be returned for every persistent channel that was removed. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

---

## 7.20 SubmitQueueEntry

`SubmitQueueEntry` initially submits a `QueueEntry` to a Device. Modifications to a `QueueEntry` can be applied by using the `ResubmitQueueEntry` or `ModifyQueueEntry` command. `QueueSubmissionParams` provides the parameters associated with the submission.

`ResponseSubmitQueueEntry/QueueEntry/@QueueEntryID` SHALL be unique within the Device. A new `@QueueEntryID` SHALL be generated for each `SubmitQueueEntry`.

### 7.20.1 CommandSubmitQueueEntry

#### 7.20.1.1 QueueSubmissionParams
The job submission can contain queue-ordering attributes equivalent to those used by `ModifyQueueEntryParams/[@Operation="Move"]` of the `ModifyQueueEntry` messages. `@ReturnJMF` MAY specify the location where the modified XJDF SHALL be sent after the job is completed or aborted.
The `@URL` attribute specifies the location where the queue Controller can retrieve the XJDF file to be submitted.

**Table 7.75: CommandSubmitQueueEntry Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `QueueSubmissionParams` | element | Defines the job submission. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

**Table 7.76: QueueSubmissionParams Element**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@Activation?` | enumeration | Activation of the submitted `QueueEntry`.<br>Allowed value is from: *Activation*. |
| `@GangName?` | NMTOKEN | Name of the Gang for the job. If `@GangName` is specified, the `QueueEntry` SHOULD be executed along with other `QueueEntry` elements that share a common value of `@GangName`. If `@GangName` is not known, the receiving Device MAY either return an error `"131"` or create the Gang with `@GangName` on the fly. |
| `@GangPolicy?` | enumeration | Ganging policy for the `QueueEntry`.<br>Allowed value is from: *GangPolicy*. |
| `@NextQueueEntryID?` | NMTOKEN | `QueueEntry/@QueueEntryID` of the queue entry that SHALL be positioned directly behind the entry. At most one of `@NextQueueEntryID`, `@PrevQueueEntryID` or `@Priority` SHALL be specified. |
| `@PrevQueueEntryID?` | NMTOKEN | `QueueEntry/@QueueEntryID` of the queue entry that SHALL be positioned directly in front of the entry. At most one of `@NextQueueEntryID`, `@PrevQueueEntryID` or `@Priority` SHALL be specified. |
| `@Priority?` | integer | Number from `"0"` to `"100"`, where `"0"` is the lowest priority and `"100"` is the maximum priority. At most one of `@NextQueueEntryID`, `@PrevQueueEntryID` or `@Priority` SHALL be specified.<br>*Note that `QueueSubmissionParams/@Priority` is not the same as `NodeInfo/@JobPriority`. `QueueSubmissionParams/@Priority` specifies the priority in the context of the Device queue whereas `NodeInfo/@JobPriority` specifies the priority of the task in general. `QueueSubmissionParams/@Priority` MAY be modified due to additional scheduling information (e.g., `NodeInfo/@FirstStart`).*<br>`QueueSubmissionParams/@Priority` and `ModifyQueueEntryParams/@Priority` SHALL take precedence over `NodeInfo/@JobPriority`. |
| `@ReturnJMF?` | URL | URL where a `CommandReturnQueueEntry` SHALL be sent when the `QueueEntry` is completed or aborted. |
| `@URL` | URL | Location of the XJDF to be submitted or resubmitted. If `@URL` refers to a directory, then all files with an extension of `.xjdf` that reside directly in the directory SHALL be processed in lexical order. The first XJDF is referred to as the primary XJDF. See Section 9.4.1 Referencing Multiple XJDF in a Directory.<br>*Note: A referenced directory MAY be inside a zip package. Refer to the application note [ZIP].* |

### Example 7.10: SubmitQueueEntry Command with “http” Scheme
In this example, the queue Controller retrieves the file with a standard http get command from a host that MAY be remote.
```xml
<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
  <Header DeviceID="TestSender" ID="l_000002" Time="2019-03-26T14:07:49.216+00:00"/>
  <CommandSubmitQueueEntry>
    <Header DeviceID="TestSender" ID="C1" Time="2019-03-26T14:07:49.217+00:00"/>
    <QueueSubmissionParams URL="http://jobserver.xjdf.org?job1"/>
  </CommandSubmitQueueEntry>
</XJMF>
```

### 7.20.2 ResponseSubmitQueueEntry

**Table 7.77: ResponseSubmitQueueEntry Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `QueueEntry?` | element | Provides the queue entry of the submitted job. `QueueEntry` SHALL be specified if the submission was successful and SHALL be omitted in case the submission was rejected. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

---

## 7.21 WakeUp

The `WakeUp` command message activates a Controller or Device that has been in stand-by mode. All `QueueEntry` elements SHALL have `@Activation="Held"` and SHALL be explicitly resumed with a `ModifyQueueEntry` with a `"Resume"` operation. A Device SHALL use the `Status` message to signal its own awakening.

### 7.21.1 CommandWakeUp

**Table 7.78: CommandWakeUp Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `Header` | element | See `Message/Header`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |

### 7.21.2 ResponseWakeUp

**Table 7.79: ResponseWakeUp Message**

| NAME | DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `@ReturnCode?` | integer | See `Response/@ReturnCode`. |
| `Header` | element | See `Message/Header`. |
| `Notification?` | element | See `Response/Notification`. |
| `<foreign namespace elements>*` | element | See Message - foreign namespace elements. |
