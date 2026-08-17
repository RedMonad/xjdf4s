# 📈 Architecture Mermaid Diagrams

*(Рендерится в GitHub, GitLab, VS Code, Notion)*

## 📦 High-Level Module Dependencies

```mermaid
graph TD
    codec_json["codec-json<br/>(7 files)"]
    codec_xml["codec-xml<br/>(26 files)"]
    core["core<br/>(6 files)"]
    dsl["dsl<br/>(2 files)"]
    messaging["messaging<br/>(9 files)"]
    model["model<br/>(50 files)"]
    protocol["protocol<br/>(1 files)"]
    codec_xml -- 48 --> core
    dsl -. 8 .-> core
    dsl -. 7 .-> model
    protocol -. 2 .-> messaging
    codec_json -. 13 .-> core
    messaging -. 19 .-> model
    codec_json -- 33 --> model
    model == 162 ==> core
    protocol -. 1 .-> core
    messaging -- 21 --> core
    protocol -. 1 .-> model
    codec_json -. 5 .-> messaging
    codec_xml == 173 ==> model
    codec_xml -- 25 --> messaging
```

## 🚨 Critical Bottlenecks & Immediate Context

```mermaid
graph TD
    xjdf4s_core_Values["core.Values"]
    xjdf4s_model_resources_ProcessResources["resources.ProcessResources"]
    xjdf4s_model_ContentAndShapeIntents["🔴 model.ContentAndShapeIntents"]:::bottleneck
    xjdf4s_model_resources_ColorSpaceConversion["resources.ColorSpaceConversion"]
    xjdf4s_model_resources_DeliveryAndPreflightResources["resources.DeliveryAndPreflightResources"]
    xjdf4s_core_Validation["core.Validation"]
    xjdf4s_model_resources_DieLayoutProduction["resources.DieLayoutProduction"]
    xjdf4s_model_resources_BinderySignature["resources.BinderySignature"]
    xjdf4s_codec_xml_Lexical["🔴 xml.Lexical"]:::bottleneck
    xjdf4s_model_resources_ColorantControl["resources.ColorantControl"]
    xjdf4s_model_resources_FoundationalResources["resources.FoundationalResources"]
    xjdf4s_model_resources_Content["resources.Content"]
    xjdf4s_codec_json_JsonMessagingCodecs["json.JsonMessagingCodecs"]
    xjdf4s_codec_xml_domain_MessagingCodecs["domain.MessagingCodecs"]
    xjdf4s_model_resources_PostpressResources["resources.PostpressResources"]
    xjdf4s_model_resources_FoldingResources["resources.FoldingResources"]
    xjdf4s_model_BindingIntent["model.BindingIntent"]
    xjdf4s_model_resources_AdditionalResources["resources.AdditionalResources"]
    xjdf4s_messaging_StatusNotificationResourceMessages["messaging.StatusNotificationResourceMessages"]
    xjdf4s_model_ColorValues["model.ColorValues"]
    xjdf4s_model_resources_GeneralAndPressResources["resources.GeneralAndPressResources"]
    xjdf4s_codec_xml_domain_MediaCodec["domain.MediaCodec"]
    xjdf4s_model_TypedValues["model.TypedValues"]
    xjdf4s_model_resources_Rendering["resources.Rendering"]
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs["domain.IntentAndAuditCodecs"]
    xjdf4s_model_resources_RunList["resources.RunList"]
    xjdf4s_dsl_DocOp["dsl.DocOp"]
    xjdf4s_codec_json_JsonMediaCodecs["json.JsonMediaCodecs"]
    xjdf4s_codec_json_JsonResources["json.JsonResources"]
    xjdf4s_model_SimpleIntents["model.SimpleIntents"]
    xjdf4s_codec_xml_domain_CoreNodeCodecs["domain.CoreNodeCodecs"]
    xjdf4s_model_resources_Contact["resources.Contact"]
    xjdf4s_model_resources_Interpreting["resources.Interpreting"]
    xjdf4s_model_resources_PdlCreation["resources.PdlCreation"]
    xjdf4s_messaging_QueueEntryMessages["messaging.QueueEntryMessages"]
    xjdf4s_codec_xml_domain_CodecHelpers["domain.CodecHelpers"]
    xjdf4s_model_Subelements["🔴 model.Subelements"]:::bottleneck
    xjdf4s_messaging_KnownMessages["messaging.KnownMessages"]
    xjdf4s_model_resources_Device["resources.Device"]
    xjdf4s_model_resources_SimpleResources["resources.SimpleResources"]
    xjdf4s_model_resources_ProcessResources --> xjdf4s_model_resources_AdditionalResources
    xjdf4s_model_resources_ProcessResources --> xjdf4s_model_resources_SimpleResources
    xjdf4s_model_resources_ProcessResources --> xjdf4s_model_Subelements
    xjdf4s_model_resources_ProcessResources --> xjdf4s_model_resources_FoldingResources
    xjdf4s_model_resources_ProcessResources --> xjdf4s_core_Values
    xjdf4s_model_resources_ProcessResources --> xjdf4s_model_ContentAndShapeIntents
    xjdf4s_model_ContentAndShapeIntents --> xjdf4s_model_Subelements
    xjdf4s_model_ContentAndShapeIntents --> xjdf4s_core_Validation
    xjdf4s_model_resources_ColorSpaceConversion --> xjdf4s_model_Subelements
    xjdf4s_model_resources_ColorSpaceConversion --> xjdf4s_model_resources_ProcessResources
    xjdf4s_model_resources_DeliveryAndPreflightResources --> xjdf4s_model_SimpleIntents
    xjdf4s_model_resources_DeliveryAndPreflightResources --> xjdf4s_core_Values
    xjdf4s_model_resources_DeliveryAndPreflightResources --> xjdf4s_model_Subelements
    xjdf4s_model_resources_DieLayoutProduction --> xjdf4s_model_resources_ProcessResources
    xjdf4s_model_resources_DieLayoutProduction --> xjdf4s_model_resources_Device
    xjdf4s_model_resources_BinderySignature --> xjdf4s_model_TypedValues
    xjdf4s_model_resources_BinderySignature --> xjdf4s_model_SimpleIntents
    xjdf4s_model_resources_BinderySignature --> xjdf4s_core_Values
    xjdf4s_codec_xml_Lexical --> xjdf4s_model_resources_AdditionalResources
    xjdf4s_codec_xml_Lexical --> xjdf4s_model_resources_ProcessResources
    xjdf4s_codec_xml_Lexical --> xjdf4s_messaging_KnownMessages
    xjdf4s_codec_xml_Lexical --> xjdf4s_core_Values
    xjdf4s_codec_xml_Lexical --> xjdf4s_model_ContentAndShapeIntents
    xjdf4s_codec_xml_Lexical --> xjdf4s_model_ColorValues
    xjdf4s_codec_xml_Lexical --> xjdf4s_model_TypedValues
    xjdf4s_codec_xml_Lexical --> xjdf4s_model_resources_FoundationalResources
    xjdf4s_codec_xml_Lexical --> xjdf4s_core_Validation
    xjdf4s_codec_xml_Lexical --> xjdf4s_model_resources_Device
    xjdf4s_codec_xml_Lexical --> xjdf4s_model_SimpleIntents
    xjdf4s_model_resources_FoundationalResources --> xjdf4s_model_SimpleIntents
    xjdf4s_model_resources_FoundationalResources --> xjdf4s_model_Subelements
    xjdf4s_model_resources_FoundationalResources --> xjdf4s_core_Values
    xjdf4s_model_resources_Content --> xjdf4s_model_resources_Interpreting
    xjdf4s_model_resources_Content --> xjdf4s_model_ContentAndShapeIntents
    xjdf4s_model_resources_Content --> xjdf4s_model_resources_DieLayoutProduction
    xjdf4s_model_resources_Content --> xjdf4s_core_Values
    xjdf4s_model_resources_Content --> xjdf4s_model_Subelements
    xjdf4s_model_resources_Content --> xjdf4s_model_resources_ProcessResources
    xjdf4s_codec_json_JsonMessagingCodecs --> xjdf4s_core_Values
    xjdf4s_codec_json_JsonMessagingCodecs --> xjdf4s_messaging_StatusNotificationResourceMessages
    xjdf4s_codec_json_JsonMessagingCodecs --> xjdf4s_model_Subelements
    xjdf4s_codec_json_JsonMessagingCodecs --> xjdf4s_messaging_KnownMessages
    xjdf4s_codec_xml_domain_MessagingCodecs --> xjdf4s_messaging_StatusNotificationResourceMessages
    xjdf4s_codec_xml_domain_MessagingCodecs --> xjdf4s_messaging_KnownMessages
    xjdf4s_codec_xml_domain_MessagingCodecs --> xjdf4s_model_Subelements
    xjdf4s_codec_xml_domain_MessagingCodecs --> xjdf4s_codec_xml_Lexical
    xjdf4s_codec_xml_domain_MessagingCodecs --> xjdf4s_core_Values
    xjdf4s_codec_xml_domain_MessagingCodecs --> xjdf4s_codec_xml_domain_CodecHelpers
    xjdf4s_model_resources_PostpressResources --> xjdf4s_model_ContentAndShapeIntents
    xjdf4s_model_resources_PostpressResources --> xjdf4s_model_BindingIntent
    xjdf4s_model_resources_FoldingResources --> xjdf4s_model_Subelements
    xjdf4s_model_resources_FoldingResources --> xjdf4s_core_Values
    xjdf4s_model_BindingIntent --> xjdf4s_model_ColorValues
    xjdf4s_model_resources_AdditionalResources --> xjdf4s_model_TypedValues
    xjdf4s_model_resources_AdditionalResources --> xjdf4s_model_SimpleIntents
    xjdf4s_model_resources_AdditionalResources --> xjdf4s_core_Validation
    xjdf4s_model_resources_AdditionalResources --> xjdf4s_core_Values
    xjdf4s_model_resources_AdditionalResources --> xjdf4s_model_Subelements
    xjdf4s_messaging_StatusNotificationResourceMessages --> xjdf4s_core_Values
    xjdf4s_messaging_StatusNotificationResourceMessages --> xjdf4s_model_Subelements
    xjdf4s_messaging_StatusNotificationResourceMessages --> xjdf4s_core_Validation
    xjdf4s_model_ColorValues --> xjdf4s_core_Validation
    xjdf4s_model_resources_GeneralAndPressResources --> xjdf4s_core_Values
    xjdf4s_model_resources_GeneralAndPressResources --> xjdf4s_model_Subelements
    xjdf4s_codec_xml_domain_MediaCodec --> xjdf4s_model_ColorValues
    xjdf4s_codec_xml_domain_MediaCodec --> xjdf4s_model_SimpleIntents
    xjdf4s_codec_xml_domain_MediaCodec --> xjdf4s_model_TypedValues
    xjdf4s_codec_xml_domain_MediaCodec --> xjdf4s_codec_xml_domain_CodecHelpers
    xjdf4s_codec_xml_domain_MediaCodec --> xjdf4s_codec_xml_Lexical
    xjdf4s_model_TypedValues --> xjdf4s_core_Validation
    xjdf4s_model_resources_Rendering --> xjdf4s_model_Subelements
    xjdf4s_model_resources_Rendering --> xjdf4s_model_resources_RunList
    xjdf4s_model_resources_Rendering --> xjdf4s_model_resources_ProcessResources
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_model_resources_DieLayoutProduction
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_model_ContentAndShapeIntents
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_model_BindingIntent
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_codec_xml_domain_MessagingCodecs
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_messaging_StatusNotificationResourceMessages
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_model_resources_Device
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_model_Subelements
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_core_Values
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_codec_xml_domain_CodecHelpers
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_messaging_QueueEntryMessages
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_codec_xml_Lexical
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_codec_xml_domain_MediaCodec
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_model_SimpleIntents
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_model_resources_AdditionalResources
    xjdf4s_codec_xml_domain_IntentAndAuditCodecs --> xjdf4s_model_ColorValues
    xjdf4s_model_resources_RunList --> xjdf4s_model_ContentAndShapeIntents
    xjdf4s_model_resources_RunList --> xjdf4s_model_Subelements
    xjdf4s_model_resources_RunList --> xjdf4s_model_resources_FoundationalResources
    xjdf4s_dsl_DocOp --> xjdf4s_core_Values
    xjdf4s_codec_json_JsonMediaCodecs --> xjdf4s_model_resources_RunList
    xjdf4s_codec_json_JsonMediaCodecs --> xjdf4s_model_TypedValues
    xjdf4s_codec_json_JsonMediaCodecs --> xjdf4s_model_SimpleIntents
    xjdf4s_codec_json_JsonMediaCodecs --> xjdf4s_model_ContentAndShapeIntents
    xjdf4s_codec_json_JsonMediaCodecs --> xjdf4s_model_resources_SimpleResources
    xjdf4s_codec_json_JsonMediaCodecs --> xjdf4s_core_Values
    xjdf4s_codec_json_JsonMediaCodecs --> xjdf4s_model_resources_Device
    xjdf4s_codec_json_JsonMediaCodecs --> xjdf4s_model_ColorValues
    xjdf4s_codec_json_JsonMediaCodecs --> xjdf4s_model_resources_ProcessResources
    xjdf4s_codec_json_JsonMediaCodecs --> xjdf4s_model_resources_FoundationalResources
    xjdf4s_codec_json_JsonResources --> xjdf4s_model_resources_FoundationalResources
    xjdf4s_codec_json_JsonResources --> xjdf4s_model_resources_RunList
    xjdf4s_codec_json_JsonResources --> xjdf4s_model_resources_ProcessResources
    xjdf4s_codec_json_JsonResources --> xjdf4s_model_resources_SimpleResources
    xjdf4s_model_SimpleIntents --> xjdf4s_core_Validation
    xjdf4s_model_SimpleIntents --> xjdf4s_core_Values
    xjdf4s_codec_xml_domain_CoreNodeCodecs --> xjdf4s_codec_xml_Lexical
    xjdf4s_codec_xml_domain_CoreNodeCodecs --> xjdf4s_codec_xml_domain_CodecHelpers
    xjdf4s_model_resources_Contact --> xjdf4s_core_Validation
    xjdf4s_model_resources_Interpreting --> xjdf4s_model_resources_Rendering
    xjdf4s_model_resources_Interpreting --> xjdf4s_model_Subelements
    xjdf4s_model_resources_Interpreting --> xjdf4s_model_resources_FoundationalResources
    xjdf4s_model_resources_Interpreting --> xjdf4s_model_resources_SimpleResources
    xjdf4s_model_resources_PdlCreation --> xjdf4s_model_Subelements
    xjdf4s_model_resources_PdlCreation --> xjdf4s_model_ContentAndShapeIntents
    xjdf4s_model_resources_PdlCreation --> xjdf4s_model_resources_ColorSpaceConversion
    xjdf4s_messaging_QueueEntryMessages --> xjdf4s_model_Subelements
    xjdf4s_codec_xml_domain_CodecHelpers --> xjdf4s_core_Values
    xjdf4s_codec_xml_domain_CodecHelpers --> xjdf4s_model_SimpleIntents
    xjdf4s_codec_xml_domain_CodecHelpers --> xjdf4s_model_ContentAndShapeIntents
    xjdf4s_codec_xml_domain_CodecHelpers --> xjdf4s_model_ColorValues
    xjdf4s_model_Subelements --> xjdf4s_core_Values
    xjdf4s_messaging_KnownMessages --> xjdf4s_model_resources_Device
    xjdf4s_messaging_KnownMessages --> xjdf4s_model_Subelements
    xjdf4s_model_resources_Device --> xjdf4s_core_Values
    xjdf4s_model_resources_Device --> xjdf4s_model_Subelements
    xjdf4s_model_resources_SimpleResources --> xjdf4s_model_SimpleIntents
    xjdf4s_model_resources_SimpleResources --> xjdf4s_model_ColorValues
    xjdf4s_model_resources_SimpleResources --> xjdf4s_model_ContentAndShapeIntents
    classDef bottleneck fill:#ff6b6b,stroke:#c0392b,stroke-width:3px,color:#fff;
```

## 🔄 Cyclic Dependencies

### Cycle 1 (2 files)

```mermaid
graph LR
    xjdf4s_codec_xml_derivation_FieldCodec["derivation.FieldCodec"] --> xjdf4s_codec_xml_derivation_Derived["derivation.Derived"]
    xjdf4s_codec_xml_derivation_Derived["derivation.Derived"] --> xjdf4s_codec_xml_derivation_FieldCodec["derivation.FieldCodec"]
```

### Cycle 2 (43 files)

```mermaid
graph LR
    xjdf4s_model_resources_ProcessResources["resources.ProcessResources"] --> xjdf4s_model_resources_DeliveryAndPreflightResources["resources.DeliveryAndPreflightResources"]
    xjdf4s_model_resources_DeliveryAndPreflightResources["resources.DeliveryAndPreflightResources"] --> xjdf4s_model_resources_ColorSpaceConversion["resources.ColorSpaceConversion"]
    xjdf4s_model_resources_ColorSpaceConversion["resources.ColorSpaceConversion"] --> xjdf4s_model_ContentAndShapeIntents["model.ContentAndShapeIntents"]
    xjdf4s_model_ContentAndShapeIntents["model.ContentAndShapeIntents"] --> xjdf4s_model_resources_QualityControl["resources.QualityControl"]
    xjdf4s_model_resources_QualityControl["resources.QualityControl"] --> xjdf4s_model_resources_DieLayoutProduction["resources.DieLayoutProduction"]
    xjdf4s_model_resources_DieLayoutProduction["resources.DieLayoutProduction"] --> xjdf4s_model_resources_BinderySignature["resources.BinderySignature"]
    xjdf4s_model_resources_BinderySignature["resources.BinderySignature"] --> xjdf4s_model_resources_ColorantControl["resources.ColorantControl"]
    xjdf4s_model_resources_ColorantControl["resources.ColorantControl"] --> xjdf4s_model_resources_FoundationalResources["resources.FoundationalResources"]
    xjdf4s_model_resources_FoundationalResources["resources.FoundationalResources"] --> xjdf4s_model_resources_Content["resources.Content"]
    xjdf4s_model_resources_Content["resources.Content"] --> xjdf4s_model_resources_MarksAndStacking["resources.MarksAndStacking"]
    xjdf4s_model_resources_MarksAndStacking["resources.MarksAndStacking"] --> xjdf4s_model_resources_PrepressResources["resources.PrepressResources"]
    xjdf4s_model_resources_PrepressResources["resources.PrepressResources"] --> xjdf4s_model_resources_PostpressResources["resources.PostpressResources"]
    xjdf4s_model_resources_PostpressResources["resources.PostpressResources"] --> xjdf4s_model_resources_FoldingResources["resources.FoldingResources"]
    xjdf4s_model_resources_FoldingResources["resources.FoldingResources"] --> xjdf4s_model_BindingIntent["model.BindingIntent"]
    xjdf4s_model_BindingIntent["model.BindingIntent"] --> xjdf4s_model_Product["model.Product"]
    xjdf4s_model_Product["model.Product"] --> xjdf4s_model_resources_AdditionalResources["resources.AdditionalResources"]
    xjdf4s_model_resources_AdditionalResources["resources.AdditionalResources"] --> xjdf4s_model_resources_SmallProductionResources["resources.SmallProductionResources"]
    xjdf4s_model_resources_SmallProductionResources["resources.SmallProductionResources"] --> xjdf4s_model_resources_MissingSchemaResources["resources.MissingSchemaResources"]
    xjdf4s_model_resources_MissingSchemaResources["resources.MissingSchemaResources"] --> xjdf4s_model_resources_GeneralAndPressResources["resources.GeneralAndPressResources"]
    xjdf4s_model_resources_GeneralAndPressResources["resources.GeneralAndPressResources"] --> xjdf4s_model_resources_ShapeDefinitionResources["resources.ShapeDefinitionResources"]
    xjdf4s_model_resources_ShapeDefinitionResources["resources.ShapeDefinitionResources"] --> xjdf4s_model_resources_Rendering["resources.Rendering"]
    xjdf4s_model_resources_Rendering["resources.Rendering"] --> xjdf4s_model_resources_RemainingPostpressResources["resources.RemainingPostpressResources"]
    xjdf4s_model_resources_RemainingPostpressResources["resources.RemainingPostpressResources"] --> xjdf4s_model_resources_RunList["resources.RunList"]
    xjdf4s_model_resources_RunList["resources.RunList"] --> xjdf4s_model_resources_DieLayout["resources.DieLayout"]
    xjdf4s_model_resources_DieLayout["resources.DieLayout"] --> xjdf4s_model_Resource["model.Resource"]
    xjdf4s_model_Resource["model.Resource"] --> xjdf4s_model_FinishingIntents["model.FinishingIntents"]
    xjdf4s_model_FinishingIntents["model.FinishingIntents"] --> xjdf4s_model_SimpleIntents["model.SimpleIntents"]
    xjdf4s_model_SimpleIntents["model.SimpleIntents"] --> xjdf4s_model_resources_Contact["resources.Contact"]
    xjdf4s_model_resources_Contact["resources.Contact"] --> xjdf4s_model_resources_Interpreting["resources.Interpreting"]
    xjdf4s_model_resources_Interpreting["resources.Interpreting"] --> xjdf4s_model_resources_Layout["resources.Layout"]
    xjdf4s_model_resources_Layout["resources.Layout"] --> xjdf4s_model_resources_PdlCreation["resources.PdlCreation"]
    xjdf4s_model_resources_PdlCreation["resources.PdlCreation"] --> xjdf4s_model_Subelements["model.Subelements"]
    xjdf4s_model_Subelements["model.Subelements"] --> xjdf4s_model_Partition["model.Partition"]
    xjdf4s_model_Partition["model.Partition"] --> xjdf4s_model_resources_Device["resources.Device"]
    xjdf4s_model_resources_Device["resources.Device"] --> xjdf4s_model_resources_SimpleResources["resources.SimpleResources"]
    xjdf4s_model_resources_SimpleResources["resources.SimpleResources"] --> xjdf4s_model_resources_SheetOptimizing["resources.SheetOptimizing"]
    xjdf4s_model_resources_SheetOptimizing["resources.SheetOptimizing"] --> xjdf4s_model_resources_MorePostpressResources["resources.MorePostpressResources"]
    xjdf4s_model_resources_MorePostpressResources["resources.MorePostpressResources"] --> xjdf4s_model_resources_ImageCompression["resources.ImageCompression"]
    xjdf4s_model_resources_ImageCompression["resources.ImageCompression"] --> xjdf4s_model_resources_Identification["resources.Identification"]
    xjdf4s_model_resources_Identification["resources.Identification"] --> xjdf4s_model_MediaIntent["model.MediaIntent"]
    xjdf4s_model_MediaIntent["model.MediaIntent"] --> xjdf4s_model_resources_MediaAndColor["resources.MediaAndColor"]
    xjdf4s_model_resources_MediaAndColor["resources.MediaAndColor"] --> xjdf4s_model_AssemblingIntent["model.AssemblingIntent"]
    xjdf4s_model_AssemblingIntent["model.AssemblingIntent"] --> xjdf4s_model_resources_ProcessResources["resources.ProcessResources"]
```

