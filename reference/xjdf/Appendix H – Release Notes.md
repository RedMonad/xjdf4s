# Appendix H

## H Release Notes

This appendix contains a brief summary of items that have been changed in XJDF 2.2.
*(Примечание: Ссылки на разделы Главы 9 сохранены в оригинальном виде, несмотря на то, что в текущей версии документа Глава 9 пропущена).*

**Table H.1: Release notes for XJDF 2.2**

| ITEM | ACTION | DESCRIPTION | LOCATION |
| --- | --- | --- | --- |
| Use of JSON | New | Added section introducing JSON as an encoding method. | Section 1.4.2 Use of JSON. |
| @$schema | New | Added attribute to XJDF. | Table 3.1 XJDF. |
| @Name | New | Added attribute to XJDF. | Table 3.1 XJDF. |
| @Name | New | Added attribute to AuditPool. | Table 3.3 AuditPool Element. |
| SheetOptimizingReport | New | Added output resource to SheetOptimizing process. | Table 5.54 SheetOptimizing – Output Resources. |
| Media | Deprecated | Removed resource from Bundling input resources. | Table 5.71 Bundling – Input Resources. |
| Tool | New | Added resource to Bundling input resources. | Table 5.71 Bundling – Input Resources. |
| Media (Foil) | Deprecated | Removed resource from Embossing input resources. | Table 5.87 Embossing – Input Resources. |
| MiscConsumable (Foil) | New | Added resource to Embossing input resources. | Table 5.87 Embossing – Input Resources. |
| Media (Foil) | Deprecated | Removed resource from Laminating input resources. | Table 5.109 Laminating – Input Resources. |
| MiscConsumable (Foil) | New | Added resource to Laminating input resources. | Table 5.109 Laminating – Input Resources. |
| MiscConsumable (Glue) | Modified | Cardinality changed to ‘?’ in Laminating input resources. | Table 5.109 Laminating – Input Resources. |
| MiscConsumable (Hardener) | Modified | Cardinality changed to ‘?’ in Laminating input resources. | Table 5.109 Laminating – Input Resources. |
| @SheetName | Modified | Modified description regarding the scope of uniqueness. | Table 6.4 Part Element. |
| @WebName | Modified | Modified description regarding the scope of uniqueness. | Table 6.4 Part Element. |
| Selecting a Partition | Modified | Modified section describing partition selection. | Section 6.1.3.2 Selecting a Partition. |
| Glue | Deprecated | Removed element from BoxFoldingParams. | Table 6.17 BoxFoldingParams Resource. |
| Glue | New | Added element to BoxFoldAction. | Table 6.19 BoxFoldAction Element. |
| Glue | New | Added enumeration to BoxFoldAction/@Action attribute values. | Table 6.20 Action Attribute Values. |
| @BlackPointCompensation | New | Added attribute to ColorSpaceConversionOp. | Table 6.36 ColorSpaceConversionOp Element. |
| @BlackPointCompensationDetails | New | Added attribute to ColorSpaceConversionOp. | Table 6.36 ColorSpaceConversionOp Element. |
| @RestApiBaseURL | New | Added attribute to Device. | Table 6.57 Device Resource. |
| FileSpec | Modified | Cardinality changed to ‘*’ in ShapeDef. | Table 6.155 ShapeDef Resource. |
| RuleLength | New | Added element to ShapeDef. | Table 6.155 ShapeDef Resource. |
| FileSpec | Modified | Cardinality changed to ‘*’ in ShapeTemplate. | Table 6.159 ShapeTemplate Element. |
| SheetOptimizingReport | New | Added resource. | Section 6.80 SheetOptimizingReport. |
| @Manufacturer | New | Added attribute to Tool. | Table 6.174 Tool Resource. |
| @ManufacturerURL | New | Added attribute to Tool. | Table 6.174 Tool Resource. |
| @SerialNumber | New | Added attribute to Tool. | Table 6.174 Tool Resource. |
| Combined Use of Varnishing-Params Attributes | New | Added new section and table to specify combinations of varnishing attributes. | Section 6.94.1 Combined Use of VarnishingParams Attributes. |
| @$schema | New | Added attribute to XJMF. | Table 7.1 XJMF Element. |
| @Name | New | Added attribute to XJMF. | Table 7.1 XJMF Element. |
| `<message elements>` | Modified | Cardinality changed to exactly one in the case of JSON encoding in XJMF. | Table 7.1 XJMF Element. |
| @ID | Modified | The description has been revised to clarify the scope of uniqueness of @ID. | Table 7.3 Header. |
| @Languages | New | Added attribute to Query. | Table 7.4 Query. |
| @Languages | Deprecated | Removed attribute from Subscription. | Table 7.5 Subscription Element. |
| CommandResource | Modified | The behavior of incomplete modifications has been clarified. | Section 7.14.2 CommandResource. |
| @Languages | New | Added attribute to QueryKnownDevices. | Table 7.18 QueryKnownDevices Message. |
| @Languages | New | Added attribute to QueryNotification. | Table 7.33 QueryNotification Message. |
| @Languages | New | Added attribute to QueryResource. | Table 7.48 QueryResource Message. |
| @Types | New | Added attribute to ResourceQuParams. | Table 7.49 ResourceQuParams Element. |
| @UpdateMethod | Modified | The behavior of modifications of non-existing resources has been clarified. | Table 7.51 ResourceCmdParams Element. |
| @Languages | New | Added attribute to QueryStatus. | Table 7.64 QueryStatus Message. |
| @JobID | New | Added attribute to StatusQuParams. | Table 7.65 StatusQuParams Element. |
| @JobPartID | New | Added attribute to StatusQuParams. | Table 7.65 StatusQuParams Element. |
| @Types | New | Added attribute to StatusQuParams. | Table 7.65 StatusQuParams Element. |
| Part | New | Added element to StatusQuParams. | Table 7.65 StatusQuParams Element. |
| @ModuleIDs | New | Modified attribute to clarify the items it references. | Table 7.69 JobPhase Element. |
| @PercentCompleted | Modified | The scope has been clarified to be the individual JobPhase and the recommendation not to provide values above 100% has been added. | Table 7.69 JobPhase Element. |
| @TotalAmount | New | Added attribute to JobPhase. | Table 7.69 JobPhase Element. |
| Status Transition Events | New | Added new section and table to define events that trigger a status transition. | Section 7.18.2.3.1 Status Transition Events. |
| StopPersChParams | Modified | The behavior of a combination of filters has been clarified. | Section 7.19.1.1 StopPersChParams. |
| @SpotType | New | Added attribute to ColorControlStrip/Patch. | Table 8.10 Patch Element. |
| @Text | New | Added attribute to Comment. | Table 8.14 Comment Element. |
| @NPage | New | Added attribute to FileSpec. | Table 8.22 FileSpec Element. |
| @Name | New | Added attribute to MediaLayers. | Table 8.45 MediaLayers Element. |
| QueueFilter | Modified | The introduction to QueueFilter has been revised to clarify its behavior. | Section 8.36 QueueFilter. |
| @Languages | New | Added attribute to SubscriptionInfo. | Table 8.71 SubscriptionInfo Element. |
| HTTP Response Code | New | Added new section to define response code usage. | Section 9.5.2 HTTP Response Code. |
| Use of JSON and REST APIs | New | Added section describing JSON as an encoding method. | Section 9.10 Use of JSON and REST APIs. |
| PS9 | New | Added enumeration to ISOPaperSubstrate. | Table A.26 ISOPaperSubstrate Enumeration Values. |
| EmbossingFoil | Deprecated | Removed enumeration from MediaType. | Table A.30 MediaType Enumeration Values. |
| Foil | Deprecated | Removed enumeration from MediaType. | Table A.30 MediaType Enumeration Values. |
| LaminatingFoil | Deprecated | Removed enumeration from MediaType. | Table A.30 MediaType Enumeration Values. |
| MountingTape | Deprecated | Removed enumeration from MediaType. | Table A.30 MediaType Enumeration Values. |
| SelfAdhesive | Deprecated | Removed enumeration from MediaType. | Table A.30 MediaType Enumeration Values. |
| ShrinkFoil | Deprecated | Removed enumeration from MediaType. | Table A.30 MediaType Enumeration Values. |
| Device | New | Added enumeration to Scope. | Table A.36 Scope Enumeration Values. |
| 2.2 | New | Added enumeration to XJDFXJMFVersion. | Table A.52 XJDFXJMFVersion Enumeration Values. |
| Contact Types | Modified | Modified the layout of the table to add a ‘Usage’ column. | Table A.54 Contact Types. |
| PrintQuality | Modified | The parent element of the @PrintQuality attribute has been changed to PrintCondition. | Table D.1 Template Variables. |