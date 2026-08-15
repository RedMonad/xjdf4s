# 📈 Architecture Mermaid Diagrams

*(Рендерится в GitHub, GitLab, VS Code, Notion)*

## 📦 High-Level Module Dependencies

```mermaid
graph TD
    core["core<br/>(36 files)"]
    examples["examples<br/>(2 files)"]
    laws["laws<br/>(5 files)"]
    examples -- 27 --> core
    laws -- 42 --> core
```

## 🚨 Critical Bottlenecks & Immediate Context

```mermaid
graph TD
    xjdf4s_model_Validation["🔴 model.Validation"]:::bottleneck
    test_xjdf4s_laws_AlignmentLaws["laws.AlignmentLaws"]
    xjdf4s_model_Ticket["model.Ticket"]
    xjdf4s_resources_Color["resources.Color"]
    xjdf4s_resources_Delivery["resources.Delivery"]
    xjdf4s_dsl_XjdfDsl["dsl.XjdfDsl"]
    xjdf4s_resources_Finishing["resources.Finishing"]
    xjdf4s_prim_Time["prim.Time"]
    xjdf4s_resources_RunList["resources.RunList"]
    xjdf4s_examples_Main["examples.Main"]
    xjdf4s_intents_MediaLayout["intents.MediaLayout"]
    xjdf4s_model_Intent["🔴 model.Intent"]:::bottleneck
    xjdf4s_model_Audit["model.Audit"]
    xjdf4s_model_Product["model.Product"]
    xjdf4s_prim_Quantity["prim.Quantity"]
    xjdf4s_intents_FoldingVariable["intents.FoldingVariable"]
    xjdf4s_resources_NodeInfo["resources.NodeInfo"]
    xjdf4s_resources_AllResources["🔴 resources.AllResources"]:::bottleneck
    test_xjdf4s_laws_Arbitraries["laws.Arbitraries"]
    xjdf4s_model_Patch["model.Patch"]
    xjdf4s_prim_Ids["prim.Ids"]
    xjdf4s_prim_Enums["prim.Enums"]
    xjdf4s_model_Header["model.Header"]
    xjdf4s_intents_Binding["intents.Binding"]
    xjdf4s_intents_ColorProduction["intents.ColorProduction"]
    xjdf4s_model_Amounts["model.Amounts"]
    test_xjdf4s_laws_TicketLaws["laws.TicketLaws"]
    xjdf4s_model_Resource["🔴 model.Resource"]:::bottleneck
    xjdf4s_prim_Tokens["prim.Tokens"]
    xjdf4s_model_Partition["model.Partition"]
    test_xjdf4s_laws_PartitionLaws["laws.PartitionLaws"]
    xjdf4s_prim_Common["prim.Common"]
    xjdf4s_resources_Component["resources.Component"]
    xjdf4s_examples_SpecExamples["examples.SpecExamples"]
    xjdf4s_resources_Contact["resources.Contact"]
    xjdf4s_resources_Preview["resources.Preview"]
    xjdf4s_resources_Device["resources.Device"]
    xjdf4s_intents_AllIntents["🔴 intents.AllIntents"]:::bottleneck
    xjdf4s_prim_Versions["prim.Versions"]
    xjdf4s_resources_Layout["resources.Layout"]
    xjdf4s_model_Validation --> xjdf4s_model_Intent
    xjdf4s_model_Validation --> xjdf4s_model_Product
    xjdf4s_model_Validation --> xjdf4s_model_Audit
    xjdf4s_model_Validation --> xjdf4s_prim_Enums
    xjdf4s_model_Validation --> xjdf4s_model_Ticket
    xjdf4s_model_Validation --> xjdf4s_prim_Tokens
    xjdf4s_model_Validation --> xjdf4s_model_Amounts
    xjdf4s_model_Validation --> xjdf4s_model_Resource
    xjdf4s_model_Validation --> xjdf4s_model_Partition
    xjdf4s_model_Validation --> xjdf4s_prim_Ids
    xjdf4s_model_Validation --> xjdf4s_prim_Versions
    test_xjdf4s_laws_AlignmentLaws --> xjdf4s_model_Audit
    test_xjdf4s_laws_AlignmentLaws --> xjdf4s_prim_Time
    test_xjdf4s_laws_AlignmentLaws --> xjdf4s_prim_Tokens
    test_xjdf4s_laws_AlignmentLaws --> xjdf4s_model_Resource
    test_xjdf4s_laws_AlignmentLaws --> xjdf4s_prim_Enums
    xjdf4s_model_Ticket --> xjdf4s_model_Patch
    xjdf4s_model_Ticket --> xjdf4s_model_Audit
    xjdf4s_model_Ticket --> xjdf4s_model_Header
    xjdf4s_model_Ticket --> xjdf4s_model_Partition
    xjdf4s_model_Ticket --> xjdf4s_model_Validation
    xjdf4s_model_Ticket --> xjdf4s_model_Product
    xjdf4s_model_Ticket --> xjdf4s_prim_Ids
    xjdf4s_model_Ticket --> xjdf4s_prim_Versions
    xjdf4s_model_Ticket --> xjdf4s_prim_Tokens
    xjdf4s_model_Ticket --> xjdf4s_prim_Common
    xjdf4s_model_Ticket --> xjdf4s_model_Resource
    xjdf4s_resources_Color --> xjdf4s_prim_Tokens
    xjdf4s_resources_Color --> xjdf4s_prim_Quantity
    xjdf4s_resources_Color --> xjdf4s_prim_Enums
    xjdf4s_resources_Delivery --> xjdf4s_prim_Ids
    xjdf4s_resources_Delivery --> xjdf4s_prim_Time
    xjdf4s_resources_Delivery --> xjdf4s_prim_Tokens
    xjdf4s_dsl_XjdfDsl --> xjdf4s_resources_Component
    xjdf4s_dsl_XjdfDsl --> xjdf4s_resources_NodeInfo
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Validation
    xjdf4s_dsl_XjdfDsl --> xjdf4s_prim_Ids
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Audit
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Partition
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Ticket
    xjdf4s_dsl_XjdfDsl --> xjdf4s_resources_Delivery
    xjdf4s_dsl_XjdfDsl --> xjdf4s_prim_Enums
    xjdf4s_dsl_XjdfDsl --> xjdf4s_resources_RunList
    xjdf4s_dsl_XjdfDsl --> xjdf4s_prim_Tokens
    xjdf4s_dsl_XjdfDsl --> xjdf4s_prim_Common
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Resource
    xjdf4s_dsl_XjdfDsl --> xjdf4s_intents_AllIntents
    xjdf4s_dsl_XjdfDsl --> xjdf4s_resources_AllResources
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Intent
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Header
    xjdf4s_dsl_XjdfDsl --> xjdf4s_model_Product
    xjdf4s_resources_Finishing --> xjdf4s_intents_FoldingVariable
    xjdf4s_resources_Finishing --> xjdf4s_prim_Enums
    xjdf4s_resources_Finishing --> xjdf4s_prim_Common
    xjdf4s_resources_Finishing --> xjdf4s_prim_Quantity
    xjdf4s_resources_Finishing --> xjdf4s_prim_Tokens
    xjdf4s_resources_RunList --> xjdf4s_prim_Enums
    xjdf4s_resources_RunList --> xjdf4s_prim_Common
    xjdf4s_resources_RunList --> xjdf4s_prim_Quantity
    xjdf4s_resources_RunList --> xjdf4s_prim_Tokens
    xjdf4s_resources_RunList --> xjdf4s_prim_Ids
    xjdf4s_examples_Main --> xjdf4s_prim_Enums
    xjdf4s_examples_Main --> xjdf4s_prim_Quantity
    xjdf4s_examples_Main --> xjdf4s_prim_Time
    xjdf4s_examples_Main --> xjdf4s_model_Product
    xjdf4s_examples_Main --> xjdf4s_model_Resource
    xjdf4s_examples_Main --> xjdf4s_prim_Tokens
    xjdf4s_examples_Main --> xjdf4s_model_Validation
    xjdf4s_examples_Main --> xjdf4s_examples_SpecExamples
    xjdf4s_examples_Main --> xjdf4s_model_Audit
    xjdf4s_intents_MediaLayout --> xjdf4s_prim_Tokens
    xjdf4s_intents_MediaLayout --> xjdf4s_prim_Enums
    xjdf4s_intents_MediaLayout --> xjdf4s_prim_Quantity
    xjdf4s_model_Intent --> xjdf4s_intents_AllIntents
    xjdf4s_model_Intent --> xjdf4s_prim_Tokens
    xjdf4s_model_Intent --> xjdf4s_prim_Ids
    xjdf4s_model_Audit --> xjdf4s_prim_Tokens
    xjdf4s_model_Audit --> xjdf4s_model_Header
    xjdf4s_model_Audit --> xjdf4s_prim_Time
    xjdf4s_model_Product --> xjdf4s_prim_Ids
    xjdf4s_model_Product --> xjdf4s_model_Intent
    xjdf4s_model_Product --> xjdf4s_prim_Tokens
    xjdf4s_model_Product --> xjdf4s_model_Validation
    xjdf4s_model_Product --> xjdf4s_prim_Common
    xjdf4s_intents_FoldingVariable --> xjdf4s_prim_Ids
    xjdf4s_intents_FoldingVariable --> xjdf4s_prim_Enums
    xjdf4s_intents_FoldingVariable --> xjdf4s_prim_Quantity
    xjdf4s_intents_FoldingVariable --> xjdf4s_prim_Tokens
    xjdf4s_resources_NodeInfo --> xjdf4s_prim_Enums
    xjdf4s_resources_NodeInfo --> xjdf4s_prim_Time
    xjdf4s_resources_NodeInfo --> xjdf4s_prim_Tokens
    xjdf4s_resources_AllResources --> xjdf4s_prim_Ids
    xjdf4s_resources_AllResources --> xjdf4s_resources_Preview
    xjdf4s_resources_AllResources --> xjdf4s_resources_Finishing
    xjdf4s_resources_AllResources --> xjdf4s_resources_Contact
    xjdf4s_resources_AllResources --> xjdf4s_resources_Device
    xjdf4s_resources_AllResources --> xjdf4s_resources_Layout
    xjdf4s_resources_AllResources --> xjdf4s_resources_RunList
    xjdf4s_resources_AllResources --> xjdf4s_resources_NodeInfo
    xjdf4s_resources_AllResources --> xjdf4s_resources_Color
    xjdf4s_resources_AllResources --> xjdf4s_prim_Tokens
    xjdf4s_resources_AllResources --> xjdf4s_resources_Component
    xjdf4s_resources_AllResources --> xjdf4s_resources_Delivery
    test_xjdf4s_laws_Arbitraries --> xjdf4s_model_Patch
    test_xjdf4s_laws_Arbitraries --> xjdf4s_prim_Common
    test_xjdf4s_laws_Arbitraries --> xjdf4s_prim_Ids
    test_xjdf4s_laws_Arbitraries --> xjdf4s_prim_Tokens
    test_xjdf4s_laws_Arbitraries --> xjdf4s_model_Resource
    test_xjdf4s_laws_Arbitraries --> xjdf4s_prim_Quantity
    test_xjdf4s_laws_Arbitraries --> xjdf4s_model_Audit
    test_xjdf4s_laws_Arbitraries --> xjdf4s_prim_Enums
    test_xjdf4s_laws_Arbitraries --> xjdf4s_model_Ticket
    test_xjdf4s_laws_Arbitraries --> xjdf4s_model_Partition
    test_xjdf4s_laws_Arbitraries --> xjdf4s_model_Amounts
    test_xjdf4s_laws_Arbitraries --> xjdf4s_prim_Time
    xjdf4s_model_Patch --> xjdf4s_prim_Common
    xjdf4s_model_Patch --> xjdf4s_model_Resource
    xjdf4s_model_Patch --> xjdf4s_model_Audit
    xjdf4s_model_Patch --> xjdf4s_model_Validation
    xjdf4s_model_Patch --> xjdf4s_model_Ticket
    xjdf4s_model_Patch --> xjdf4s_model_Product
    xjdf4s_model_Patch --> xjdf4s_prim_Tokens
    xjdf4s_prim_Enums --> xjdf4s_prim_Tokens
    xjdf4s_prim_Enums --> xjdf4s_prim_Quantity
    xjdf4s_model_Header --> xjdf4s_prim_Common
    xjdf4s_model_Header --> xjdf4s_prim_Tokens
    xjdf4s_model_Header --> xjdf4s_prim_Enums
    xjdf4s_model_Header --> xjdf4s_model_Resource
    xjdf4s_model_Header --> xjdf4s_prim_Time
    xjdf4s_model_Header --> xjdf4s_model_Partition
    xjdf4s_model_Header --> xjdf4s_prim_Ids
    xjdf4s_intents_Binding --> xjdf4s_prim_Tokens
    xjdf4s_intents_Binding --> xjdf4s_prim_Enums
    xjdf4s_intents_Binding --> xjdf4s_prim_Ids
    xjdf4s_intents_Binding --> xjdf4s_prim_Quantity
    xjdf4s_intents_ColorProduction --> xjdf4s_prim_Quantity
    xjdf4s_intents_ColorProduction --> xjdf4s_prim_Enums
    xjdf4s_intents_ColorProduction --> xjdf4s_prim_Tokens
    xjdf4s_model_Amounts --> xjdf4s_prim_Enums
    xjdf4s_model_Amounts --> xjdf4s_prim_Quantity
    xjdf4s_model_Amounts --> xjdf4s_prim_Tokens
    xjdf4s_model_Amounts --> xjdf4s_model_Partition
    test_xjdf4s_laws_TicketLaws --> xjdf4s_model_Partition
    test_xjdf4s_laws_TicketLaws --> xjdf4s_prim_Time
    test_xjdf4s_laws_TicketLaws --> xjdf4s_resources_AllResources
    test_xjdf4s_laws_TicketLaws --> xjdf4s_model_Resource
    test_xjdf4s_laws_TicketLaws --> xjdf4s_model_Product
    test_xjdf4s_laws_TicketLaws --> xjdf4s_model_Ticket
    test_xjdf4s_laws_TicketLaws --> xjdf4s_prim_Tokens
    test_xjdf4s_laws_TicketLaws --> xjdf4s_prim_Enums
    test_xjdf4s_laws_TicketLaws --> xjdf4s_model_Audit
    test_xjdf4s_laws_TicketLaws --> xjdf4s_prim_Ids
    test_xjdf4s_laws_TicketLaws --> xjdf4s_prim_Common
    xjdf4s_model_Resource --> xjdf4s_prim_Quantity
    xjdf4s_model_Resource --> xjdf4s_prim_Common
    xjdf4s_model_Resource --> xjdf4s_model_Partition
    xjdf4s_model_Resource --> xjdf4s_resources_AllResources
    xjdf4s_model_Resource --> xjdf4s_model_Amounts
    xjdf4s_model_Resource --> xjdf4s_prim_Ids
    xjdf4s_model_Resource --> xjdf4s_prim_Time
    xjdf4s_model_Resource --> xjdf4s_prim_Tokens
    xjdf4s_model_Resource --> xjdf4s_prim_Enums
    xjdf4s_model_Partition --> xjdf4s_prim_Tokens
    xjdf4s_model_Partition --> xjdf4s_prim_Enums
    xjdf4s_model_Partition --> xjdf4s_prim_Quantity
    xjdf4s_model_Partition --> xjdf4s_prim_Ids
    test_xjdf4s_laws_PartitionLaws --> xjdf4s_model_Resource
    test_xjdf4s_laws_PartitionLaws --> xjdf4s_prim_Enums
    test_xjdf4s_laws_PartitionLaws --> xjdf4s_model_Partition
    test_xjdf4s_laws_PartitionLaws --> test_xjdf4s_laws_Arbitraries
    test_xjdf4s_laws_PartitionLaws --> xjdf4s_prim_Ids
    test_xjdf4s_laws_PartitionLaws --> xjdf4s_prim_Quantity
    test_xjdf4s_laws_PartitionLaws --> xjdf4s_prim_Tokens
    test_xjdf4s_laws_PartitionLaws --> xjdf4s_resources_AllResources
    xjdf4s_prim_Common --> xjdf4s_prim_Enums
    xjdf4s_prim_Common --> xjdf4s_prim_Ids
    xjdf4s_prim_Common --> xjdf4s_prim_Tokens
    xjdf4s_prim_Common --> xjdf4s_prim_Time
    xjdf4s_resources_Component --> xjdf4s_prim_Tokens
    xjdf4s_resources_Component --> xjdf4s_prim_Quantity
    xjdf4s_resources_Component --> xjdf4s_prim_Enums
    xjdf4s_resources_Component --> xjdf4s_prim_Ids
    xjdf4s_examples_SpecExamples --> xjdf4s_dsl_XjdfDsl
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Resource
    xjdf4s_examples_SpecExamples --> xjdf4s_prim_Common
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Audit
    xjdf4s_examples_SpecExamples --> xjdf4s_resources_AllResources
    xjdf4s_examples_SpecExamples --> xjdf4s_prim_Time
    xjdf4s_examples_SpecExamples --> xjdf4s_prim_Quantity
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Validation
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Product
    xjdf4s_examples_SpecExamples --> xjdf4s_prim_Enums
    xjdf4s_examples_SpecExamples --> xjdf4s_resources_Component
    xjdf4s_examples_SpecExamples --> xjdf4s_prim_Ids
    xjdf4s_examples_SpecExamples --> xjdf4s_prim_Tokens
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Patch
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Amounts
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Ticket
    xjdf4s_examples_SpecExamples --> xjdf4s_model_Partition
    xjdf4s_examples_SpecExamples --> xjdf4s_intents_AllIntents
    xjdf4s_resources_Contact --> xjdf4s_prim_Tokens
    xjdf4s_resources_Preview --> xjdf4s_prim_Tokens
    xjdf4s_resources_Preview --> xjdf4s_prim_Common
    xjdf4s_resources_Device --> xjdf4s_prim_Tokens
    xjdf4s_resources_Device --> xjdf4s_prim_Common
    xjdf4s_intents_AllIntents --> xjdf4s_intents_FoldingVariable
    xjdf4s_intents_AllIntents --> xjdf4s_intents_ColorProduction
    xjdf4s_intents_AllIntents --> xjdf4s_intents_MediaLayout
    xjdf4s_intents_AllIntents --> xjdf4s_prim_Tokens
    xjdf4s_intents_AllIntents --> xjdf4s_intents_Binding
    xjdf4s_intents_AllIntents --> xjdf4s_prim_Ids
    xjdf4s_resources_Layout --> xjdf4s_prim_Enums
    xjdf4s_resources_Layout --> xjdf4s_prim_Quantity
    xjdf4s_resources_Layout --> xjdf4s_prim_Tokens
    xjdf4s_resources_Layout --> xjdf4s_prim_Common
    xjdf4s_resources_Layout --> xjdf4s_prim_Ids
    classDef bottleneck fill:#ff6b6b,stroke:#c0392b,stroke-width:3px,color:#fff;
```

## 🔄 Cyclic Dependencies

### Cycle 1 (4 files)

```mermaid
graph LR
    xjdf4s_model_Validation["model.Validation"] --> xjdf4s_model_Product["model.Product"]
    xjdf4s_model_Product["model.Product"] --> xjdf4s_model_Ticket["model.Ticket"]
    xjdf4s_model_Ticket["model.Ticket"] --> xjdf4s_model_Patch["model.Patch"]
    xjdf4s_model_Patch["model.Patch"] --> xjdf4s_model_Validation["model.Validation"]
```

