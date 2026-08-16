# 📈 Architecture Mermaid Diagrams

*(Рендерится в GitHub, GitLab, VS Code, Notion)*

## 📦 High-Level Module Dependencies

```mermaid
graph TD
    modules["modules<br/>(47 files)"]
```

## 🚨 Critical Bottlenecks & Immediate Context

```mermaid
graph TD
    xjdf4s_model_ValidationTypes["model.ValidationTypes"]
    xjdf4s_intents_HoleMaking["intents.HoleMaking"]
    xjdf4s_intents_Embossing["intents.Embossing"]
    xjdf4s_model_Ticket["model.Ticket"]
    xjdf4s_resources_Color["resources.Color"]
    xjdf4s_intents_Laminating["intents.Laminating"]
    xjdf4s_resources_Delivery["resources.Delivery"]
    xjdf4s_model_TicketValidator["model.TicketValidator"]
    xjdf4s_dsl_XjdfDsl["dsl.XjdfDsl"]
    xjdf4s_resources_Finishing["resources.Finishing"]
    xjdf4s_prim_Time["prim.Time"]
    xjdf4s_resources_RunList["resources.RunList"]
    xjdf4s_examples_Main["examples.Main"]
    xjdf4s_intents_MediaLayout["intents.MediaLayout"]
    xjdf4s_model_Intent["🔴 model.Intent"]:::bottleneck
    xjdf4s_model_Product["model.Product"]
    xjdf4s_prim_Quantity["prim.Quantity"]
    xjdf4s_intents_FoldingVariable["intents.FoldingVariable"]
    xjdf4s_resources_NodeInfo["resources.NodeInfo"]
    xjdf4s_resources_AllResources["🔴 resources.AllResources"]:::bottleneck
    xjdf4s_model_Patch["model.Patch"]
    xjdf4s_prim_Ids["prim.Ids"]
    xjdf4s_prim_Enums["prim.Enums"]
    xjdf4s_model_Header["model.Header"]
    xjdf4s_model_elements_CommonElements["🔴 elements.CommonElements"]:::bottleneck
    xjdf4s_intents_Binding["intents.Binding"]
    xjdf4s_model_NamedFeatures["model.NamedFeatures"]
    xjdf4s_intents_ColorProduction["intents.ColorProduction"]
    xjdf4s_model_ChangeOrder["model.ChangeOrder"]
    xjdf4s_intents_ContentCheck["intents.ContentCheck"]
    xjdf4s_model_Amounts["model.Amounts"]
    xjdf4s_model_Resource["🔴 model.Resource"]:::bottleneck
    xjdf4s_prim_Tokens["prim.Tokens"]
    xjdf4s_model_Partition["model.Partition"]
    xjdf4s_prim_Common["prim.Common"]
    xjdf4s_resources_Component["resources.Component"]
    xjdf4s_intents_ShapeCutting["intents.ShapeCutting"]
    xjdf4s_examples_SpecExamples["examples.SpecExamples"]
    xjdf4s_resources_Contact["resources.Contact"]
    xjdf4s_resources_Preview["resources.Preview"]
    xjdf4s_model_ValidationTypes --> xjdf4s_prim_Tokens
    xjdf4s_model_ValidationTypes --> xjdf4s_prim_Enums
    xjdf4s_intents_HoleMaking --> xjdf4s_model_elements_CommonElements
    xjdf4s_intents_Embossing --> xjdf4s_prim_Tokens
    xjdf4s_intents_Embossing --> xjdf4s_prim_Quantity
    xjdf4s_intents_Embossing --> xjdf4s_prim_Enums
    xjdf4s_model_Ticket --> xjdf4s_model_Partition
    xjdf4s_model_Ticket --> xjdf4s_model_elements_CommonElements
    xjdf4s_model_Ticket --> xjdf4s_model_ValidationTypes
    xjdf4s_model_Ticket --> xjdf4s_model_Product
    xjdf4s_model_Ticket --> xjdf4s_prim_Ids
    xjdf4s_model_Ticket --> xjdf4s_prim_Tokens
    xjdf4s_model_Ticket --> xjdf4s_prim_Common
    xjdf4s_model_Ticket --> xjdf4s_model_Resource
    xjdf4s_resources_Color --> xjdf4s_prim_Tokens
    xjdf4s_resources_Color --> xjdf4s_prim_Quantity
    xjdf4s_resources_Color --> xjdf4s_prim_Enums
    xjdf4s_intents_Laminating --> xjdf4s_prim_Enums
    xjdf4s_intents_Laminating --> xjdf4s_prim_Tokens
    xjdf4s_intents_Laminating --> xjdf4s_prim_Quantity
    xjdf4s_resources_Delivery --> xjdf4s_prim_Ids
    xjdf4s_resources_Delivery --> xjdf4s_prim_Time
    xjdf4s_resources_Delivery --> xjdf4s_prim_Quantity
    xjdf4s_resources_Delivery --> xjdf4s_prim_Tokens
    xjdf4s_model_TicketValidator --> xjdf4s_model_elements_CommonElements
    xjdf4s_model_TicketValidator --> xjdf4s_intents_Embossing
    xjdf4s_model_TicketValidator --> xjdf4s_model_Product
    xjdf4s_model_TicketValidator --> xjdf4s_model_Partition
    xjdf4s_model_TicketValidator --> xjdf4s_prim_Tokens
    xjdf4s_model_TicketValidator --> xjdf4s_model_Intent
    xjdf4s_model_TicketValidator --> xjdf4s_resources_Component
    xjdf4s_model_TicketValidator --> xjdf4s_prim_Enums
    xjdf4s_model_TicketValidator --> xjdf4s_model_Header
    xjdf4s_model_TicketValidator --> xjdf4s_intents_ContentCheck
    xjdf4s_model_TicketValidator --> xjdf4s_model_Resource
    xjdf4s_model_TicketValidator --> xjdf4s_model_ValidationTypes
    xjdf4s_model_TicketValidator --> xjdf4s_resources_RunList
    xjdf4s_model_TicketValidator --> xjdf4s_resources_AllResources
    xjdf4s_model_TicketValidator --> xjdf4s_intents_Binding
    xjdf4s_model_TicketValidator --> xjdf4s_prim_Ids
    xjdf4s_model_TicketValidator --> xjdf4s_intents_MediaLayout
    xjdf4s_model_TicketValidator --> xjdf4s_intents_ColorProduction
    xjdf4s_model_TicketValidator --> xjdf4s_model_Amounts
    xjdf4s_model_TicketValidator --> xjdf4s_intents_ShapeCutting
    xjdf4s_model_TicketValidator --> xjdf4s_resources_Preview
    xjdf4s_model_TicketValidator --> xjdf4s_resources_Color
    xjdf4s_model_TicketValidator --> xjdf4s_model_Ticket
    xjdf4s_model_TicketValidator --> xjdf4s_resources_Finishing
    xjdf4s_model_TicketValidator --> xjdf4s_intents_HoleMaking
    xjdf4s_model_TicketValidator --> xjdf4s_intents_FoldingVariable
    xjdf4s_dsl_XjdfDsl --> xjdf4s_resources_Component
    xjdf4s_dsl_XjdfDsl --> xjdf4s_resources_NodeInfo
    xjdf4s_dsl_XjdfDsl --> xjdf4s_prim_Ids
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_elements_CommonElements
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Partition
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Ticket
    xjdf4s_dsl_XjdfDsl --> xjdf4s_resources_Delivery
    xjdf4s_dsl_XjdfDsl --> xjdf4s_prim_Enums
    xjdf4s_dsl_XjdfDsl --> xjdf4s_resources_RunList
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_TicketValidator
    xjdf4s_dsl_XjdfDsl --> xjdf4s_prim_Tokens
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_ValidationTypes
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Resource
    xjdf4s_dsl_XjdfDsl --> xjdf4s_resources_AllResources
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Intent
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Header
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Product
    xjdf4s_resources_Finishing --> xjdf4s_intents_FoldingVariable
    xjdf4s_resources_Finishing --> xjdf4s_prim_Enums
    xjdf4s_resources_Finishing --> xjdf4s_prim_Quantity
    xjdf4s_resources_Finishing --> xjdf4s_model_elements_CommonElements
    xjdf4s_resources_Finishing --> xjdf4s_prim_Tokens
    xjdf4s_resources_RunList --> xjdf4s_prim_Enums
    xjdf4s_resources_RunList --> xjdf4s_model_elements_CommonElements
    xjdf4s_resources_RunList --> xjdf4s_prim_Quantity
    xjdf4s_resources_RunList --> xjdf4s_prim_Tokens
    xjdf4s_resources_RunList --> xjdf4s_prim_Ids
    xjdf4s_examples_Main --> xjdf4s_prim_Enums
    xjdf4s_examples_Main --> xjdf4s_prim_Quantity
    xjdf4s_examples_Main --> xjdf4s_model_ValidationTypes
    xjdf4s_examples_Main --> xjdf4s_prim_Time
    xjdf4s_examples_Main --> xjdf4s_model_Product
    xjdf4s_examples_Main --> xjdf4s_model_Resource
    xjdf4s_examples_Main --> xjdf4s_prim_Tokens
    xjdf4s_examples_Main --> xjdf4s_examples_SpecExamples
    xjdf4s_intents_MediaLayout --> xjdf4s_prim_Tokens
    xjdf4s_intents_MediaLayout --> xjdf4s_prim_Enums
    xjdf4s_intents_MediaLayout --> xjdf4s_model_elements_CommonElements
    xjdf4s_intents_MediaLayout --> xjdf4s_prim_Quantity
    xjdf4s_model_Intent --> xjdf4s_prim_Tokens
    xjdf4s_model_Intent --> xjdf4s_model_ValidationTypes
    xjdf4s_model_Intent --> xjdf4s_prim_Ids
    xjdf4s_model_Product --> xjdf4s_prim_Ids
    xjdf4s_model_Product --> xjdf4s_model_Intent
    xjdf4s_model_Product --> xjdf4s_prim_Tokens
    xjdf4s_model_Product --> xjdf4s_model_elements_CommonElements
    xjdf4s_model_Product --> xjdf4s_prim_Common
    xjdf4s_model_Product --> xjdf4s_model_ValidationTypes
    xjdf4s_intents_FoldingVariable --> xjdf4s_prim_Ids
    xjdf4s_intents_FoldingVariable --> xjdf4s_model_ValidationTypes
    xjdf4s_intents_FoldingVariable --> xjdf4s_prim_Enums
    xjdf4s_intents_FoldingVariable --> xjdf4s_prim_Quantity
    xjdf4s_intents_FoldingVariable --> xjdf4s_model_elements_CommonElements
    xjdf4s_intents_FoldingVariable --> xjdf4s_prim_Tokens
    xjdf4s_resources_NodeInfo --> xjdf4s_prim_Enums
    xjdf4s_resources_NodeInfo --> xjdf4s_prim_Time
    xjdf4s_resources_NodeInfo --> xjdf4s_prim_Ids
    xjdf4s_resources_NodeInfo --> xjdf4s_model_elements_CommonElements
    xjdf4s_resources_NodeInfo --> xjdf4s_prim_Tokens
    xjdf4s_resources_AllResources --> xjdf4s_prim_Ids
    xjdf4s_resources_AllResources --> xjdf4s_resources_Preview
    xjdf4s_resources_AllResources --> xjdf4s_resources_Finishing
    xjdf4s_resources_AllResources --> xjdf4s_resources_Contact
    xjdf4s_resources_AllResources --> xjdf4s_resources_RunList
    xjdf4s_resources_AllResources --> xjdf4s_resources_NodeInfo
    xjdf4s_resources_AllResources --> xjdf4s_resources_Color
    xjdf4s_resources_AllResources --> xjdf4s_prim_Tokens
    xjdf4s_resources_AllResources --> xjdf4s_resources_Component
    xjdf4s_resources_AllResources --> xjdf4s_resources_Delivery
    xjdf4s_model_Patch --> xjdf4s_model_Resource
    xjdf4s_model_Patch --> xjdf4s_model_elements_CommonElements
    xjdf4s_model_Patch --> xjdf4s_model_Ticket
    xjdf4s_model_Patch --> xjdf4s_model_Product
    xjdf4s_model_Patch --> xjdf4s_model_ValidationTypes
    xjdf4s_prim_Enums --> xjdf4s_prim_Tokens
    xjdf4s_prim_Enums --> xjdf4s_prim_Quantity
    xjdf4s_model_Header --> xjdf4s_model_elements_CommonElements
    xjdf4s_model_Header --> xjdf4s_prim_Tokens
    xjdf4s_model_Header --> xjdf4s_prim_Enums
    xjdf4s_model_Header --> xjdf4s_model_Resource
    xjdf4s_model_Header --> xjdf4s_prim_Time
    xjdf4s_model_Header --> xjdf4s_model_Partition
    xjdf4s_model_Header --> xjdf4s_prim_Ids
    xjdf4s_model_Header --> xjdf4s_model_ValidationTypes
    xjdf4s_model_elements_CommonElements --> xjdf4s_prim_Quantity
    xjdf4s_model_elements_CommonElements --> xjdf4s_prim_Ids
    xjdf4s_model_elements_CommonElements --> xjdf4s_prim_Enums
    xjdf4s_model_elements_CommonElements --> xjdf4s_model_ValidationTypes
    xjdf4s_model_elements_CommonElements --> xjdf4s_prim_Common
    xjdf4s_model_elements_CommonElements --> xjdf4s_prim_Tokens
    xjdf4s_model_elements_CommonElements --> xjdf4s_prim_Time
    xjdf4s_intents_Binding --> xjdf4s_prim_Tokens
    xjdf4s_intents_Binding --> xjdf4s_prim_Enums
    xjdf4s_intents_Binding --> xjdf4s_prim_Ids
    xjdf4s_intents_Binding --> xjdf4s_model_elements_CommonElements
    xjdf4s_intents_Binding --> xjdf4s_model_ValidationTypes
    xjdf4s_intents_Binding --> xjdf4s_prim_Quantity
    xjdf4s_model_NamedFeatures --> xjdf4s_model_ValidationTypes
    xjdf4s_model_NamedFeatures --> xjdf4s_model_Ticket
    xjdf4s_model_NamedFeatures --> xjdf4s_prim_Tokens
    xjdf4s_model_NamedFeatures --> xjdf4s_model_elements_CommonElements
    xjdf4s_model_NamedFeatures --> xjdf4s_prim_Enums
    xjdf4s_intents_ColorProduction --> xjdf4s_prim_Quantity
    xjdf4s_intents_ColorProduction --> xjdf4s_model_elements_CommonElements
    xjdf4s_intents_ColorProduction --> xjdf4s_prim_Enums
    xjdf4s_intents_ColorProduction --> xjdf4s_prim_Tokens
    xjdf4s_model_ChangeOrder --> xjdf4s_model_Product
    xjdf4s_model_ChangeOrder --> xjdf4s_model_Ticket
    xjdf4s_model_ChangeOrder --> xjdf4s_model_TicketValidator
    xjdf4s_model_ChangeOrder --> xjdf4s_model_ValidationTypes
    xjdf4s_model_ChangeOrder --> xjdf4s_model_Resource
    xjdf4s_model_ChangeOrder --> xjdf4s_model_elements_CommonElements
    xjdf4s_model_ChangeOrder --> xjdf4s_prim_Ids
    xjdf4s_model_ChangeOrder --> xjdf4s_model_Patch
    xjdf4s_intents_ContentCheck --> xjdf4s_prim_Ids
    xjdf4s_intents_ContentCheck --> xjdf4s_prim_Common
    xjdf4s_intents_ContentCheck --> xjdf4s_prim_Enums
    xjdf4s_intents_ContentCheck --> xjdf4s_prim_Quantity
    xjdf4s_intents_ContentCheck --> xjdf4s_model_elements_CommonElements
    xjdf4s_model_Amounts --> xjdf4s_prim_Enums
    xjdf4s_model_Amounts --> xjdf4s_prim_Quantity
    xjdf4s_model_Amounts --> xjdf4s_prim_Tokens
    xjdf4s_model_Amounts --> xjdf4s_model_Partition
    xjdf4s_model_Amounts --> xjdf4s_model_ValidationTypes
    xjdf4s_model_Resource --> xjdf4s_model_elements_CommonElements
    xjdf4s_model_Resource --> xjdf4s_prim_Quantity
    xjdf4s_model_Resource --> xjdf4s_prim_Common
    xjdf4s_model_Resource --> xjdf4s_model_Partition
    xjdf4s_model_Resource --> xjdf4s_resources_AllResources
    xjdf4s_model_Resource --> xjdf4s_model_ValidationTypes
    xjdf4s_model_Resource --> xjdf4s_model_Amounts
    xjdf4s_model_Resource --> xjdf4s_prim_Ids
    xjdf4s_model_Resource --> xjdf4s_prim_Time
    xjdf4s_model_Resource --> xjdf4s_prim_Tokens
    xjdf4s_model_Resource --> xjdf4s_prim_Enums
    xjdf4s_model_Partition --> xjdf4s_prim_Tokens
    xjdf4s_model_Partition --> xjdf4s_prim_Enums
    xjdf4s_model_Partition --> xjdf4s_prim_Quantity
    xjdf4s_model_Partition --> xjdf4s_model_ValidationTypes
    xjdf4s_prim_Common --> xjdf4s_prim_Tokens
    xjdf4s_resources_Component --> xjdf4s_prim_Tokens
    xjdf4s_resources_Component --> xjdf4s_model_elements_CommonElements
    xjdf4s_resources_Component --> xjdf4s_prim_Quantity
    xjdf4s_resources_Component --> xjdf4s_prim_Enums
    xjdf4s_resources_Component --> xjdf4s_prim_Ids
    xjdf4s_intents_ShapeCutting --> xjdf4s_prim_Enums
    xjdf4s_intents_ShapeCutting --> xjdf4s_prim_Ids
    xjdf4s_intents_ShapeCutting --> xjdf4s_prim_Tokens
    xjdf4s_intents_ShapeCutting --> xjdf4s_prim_Quantity
    xjdf4s_intents_ShapeCutting --> xjdf4s_model_ValidationTypes
    xjdf4s_examples_SpecExamples --> xjdf4s_dsl_XjdfDsl
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Resource
    xjdf4s_examples_SpecExamples --> xjdf4s_model_ChangeOrder
    xjdf4s_examples_SpecExamples --> xjdf4s_prim_Common
    xjdf4s_examples_SpecExamples --> xjdf4s_model_elements_CommonElements
    xjdf4s_examples_SpecExamples --> xjdf4s_resources_AllResources
    xjdf4s_examples_SpecExamples --> xjdf4s_prim_Time
    xjdf4s_examples_SpecExamples --> xjdf4s_prim_Quantity
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Product
    xjdf4s_examples_SpecExamples --> xjdf4s_prim_Enums
    xjdf4s_examples_SpecExamples --> xjdf4s_resources_Component
    xjdf4s_examples_SpecExamples --> xjdf4s_prim_Ids
    xjdf4s_examples_SpecExamples --> xjdf4s_prim_Tokens
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Amounts
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Ticket
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Intent
    xjdf4s_examples_SpecExamples --> xjdf4s_model_NamedFeatures
    xjdf4s_examples_SpecExamples --> xjdf4s_model_ValidationTypes
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Partition
    xjdf4s_resources_Contact --> xjdf4s_prim_Tokens
    xjdf4s_resources_Preview --> xjdf4s_model_elements_CommonElements
    xjdf4s_resources_Preview --> xjdf4s_prim_Tokens
    classDef bottleneck fill:#ff6b6b,stroke:#c0392b,stroke-width:3px,color:#fff;
```

