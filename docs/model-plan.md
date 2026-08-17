# XJDF data-model plan

## Sources and method

The normative prose is read from `reference/xjdf/*`. Schema navigation is performed with
`reference/xjdf/tool/xsdq.py` against `xsd-index.json`; the raw XSD is not manually traversed. Initial queries included:

```bash
python3 reference/xjdf/tool/xsdq.py summary --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}XJDF' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}Resource' --depth 1 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py hierarchy 'complexType:{http://www.CIP4.org/JDFSchema_2_0}SpecificResource' --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}XJMF' --depth 1 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}AuditStatus' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}AssemblingIntent' --depth 1 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}BindingIntent' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}MediaIntent' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py hierarchy 'complexType:{http://www.CIP4.org/JDFSchema_2_0}ProductIntent' --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py hierarchy 'complexType:{http://www.CIP4.org/JDFSchema_2_0}SpecificResource' --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}IdentificationField' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}StitchingParams' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}ImageEnhancementParams' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}ApprovalDetails' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}Assembly' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}ScreeningParams' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}Media' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}LooseBindingParams' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}WebInlineFinishingParams' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}ImageCompressionParams' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}InterpretingParams' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}BinderySignature' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}Content' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}StackingParams' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}PDLCreationParams' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}QualityControlResult' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}Layout' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}SignalStatus' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}CommandPipeControl' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}ResponseKnownMessages' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}SignalQueueStatus' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}CommandSubmitQueueEntry' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}FileSpec' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
python3 reference/xjdf/tool/xsdq.py bundle 'element:{http://www.CIP4.org/JDFSchema_2_0}RasterReadingParams' --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json
```

The index reports 365 elements, 366 complex types and 228 simple types. All 101 substitution members of
`SpecificResource` (the `xsdq hierarchy` default `--limit 100` truncates the list to 100 and thereby omits
`RasterReadingParams`) plus the normative 2.2 `SheetOptimizingReport` are represented. All 44
concrete XJMF messages are also represented; the public algebra remains extensible at normative extension points. The
checked-in schema's `Version`
facet lists `2.0` and `2.1`, while the normative 2.2 prose requires `2.2`; the domain enum deliberately accepts all three
and records this source discrepancy rather than silently dropping the current specification value. Two further index/schema
mismatches are resolved in favor of normative tables: `Part/@BlockName` is modelled as `NMTOKEN` rather than the indexed
`XYPair`, `PartWaste/@ModuleIDs` as `NMTOKENS` rather than the indexed `float`, `Resource/@Brand` as `string` rather than
the indexed `boolean`, `PreflightTest/@Action` as the normative action enumeration rather than indexed `duration`, and
`ColorantAlias/@RawName` as normative `hexBinary` rather than indexed `dateTime`. XJDF 2.2 additions absent from the
checked-in schema, including `Device/@RestApiBaseURL` and `ShapeDef/RuleLength`, are retained from the normative tables.
Image compression follows chapter 8 where schema index types are corrupted (`CCITTFaxParams/@EndOfBlock` and
`JPEG2000Params/@LayerRates`). `ContentMetadata/@ISBN`, `RefAnchor/@AnchorType`, and
`StopPersChParams/@MessageType` likewise follow their normative token/enumeration definitions instead of corrupt index
types. `CommandShutDown/ShutDownCmdParams` follows the normative optional cardinality. Queue-entry resubmission uses
normative `NMTOKEN` and closed update-method types instead of indexed `float` and unconstrained `string`. The generated
hierarchy omits the XSD substitution member `RasterReadingParams` when called with its default `--limit 100`; the
normative 2.2-only `SheetOptimizingReport` is absent from the XSD entirely. Both are included in the model and the
combined coverage report.

## Audit consolidation (2026-08-17)

The consolidated audit (`AUDIT.md`) and its three fact-checks (`FACTS-A/B/C.md`) were applied to the model:

- **Critical (3/3 fixed):** `ChannelMode` is now the normative `FireAndForget | Reliable` pair with
  `Subscription/@ChannelMode` as an ordered list; `BoxFoldingParams` models the 2.2 ordered `BoxFoldAction*`
  sequence with the `Action="Glue"` step and a child `Glue`, keeping the deprecated top-level glues optional;
  `MediaLayers` is an ordered `Glue* | Media*` layer vector with front/back boundary validation.
- **High (14/14 fixed):** Table A.14 `GeneralId.DataType`; the 2.2 XJMF fields (`ResourceQuParams/@Types`,
  `SignalResource/@ReplaceAfter|Before`, `SubscriptionInfo/@Languages`, NMTOKEN `@ChannelID`); `Tool` 2.2 fields and
  `Patch/@SpotType` (with the normative `Patch` name restored); the missing enum values (`Scope.Device`,
  `ISOPaperSubstrate.PS9`, `MediaType.Synthetic` with `Vinyl` deprecated, `Sides.Unprinted`, `JdfVersion` `1.0`/`1.8`);
  `NamedColor` as a closed 147-name enum; `CombinedProcessIndex` as `IntegerList`; `Part` integer ranges; validated
  even-length lists and bounded colors; per-binding-type detail ADTs; the `ValidatedNode` compositional validation
  layer; the shared `Priority0To100`; real calendar/time-zone validation for `XsdDateTime`/`XsdDuration`;
  `Illumination` as `NMTOKEN`. The clean `sbt "clean ; compile ; test"` build remains unverified in this environment
  (no JDK/sbt), and must be executed in CI.
- **Medium:** `Condition/@PartContext` as `NMTOKENS` with the `LayoutCondition`/`CellConditionTerm` clones unified;
  the `Query` family `@Languages` policy documented per concrete table; `XsdId` vs `XsdIdRef` split at the type
  level with document-level ID uniqueness validation; the JSON-only members removed from the domain roots; the
  normative `string` type backed by the `XjdfString` opaque type; ordered mixed content for foreign extensions;
  namespace-safe `ForeignQName` for extension fallbacks; tests are now discovered munit suites; coverage documents
  distinguish name/field/type/validation/transport coverage.
- **Low:** documented in [normative-choices.md](normative-choices.md), including the `NumberUp` narrowing, the
  `RunList/@Docs` normative `IntegerRange`, the `FoldCatalog` pattern, and the deferred strict-equality sweep.

## Scala 3 modelling rules

1. XSD products become immutable `case class` values.
2. Closed enumerations and choices become `enum` coproducts.
3. Open substitution points (`SpecificResource`, XJMF message extensions and foreign namespaces) remain traits;
   sealing them would incorrectly prohibit conforming extensions.
4. XSD identifiers, constrained strings, bounded numbers and parity-checked lists use zero-overhead `opaque type`
   values with smart constructors. `XsdId` (declaration) and `XsdIdRef` (reference) are distinct types; normative
   `string` is `XjdfString` (normalized, ≤ 1023 characters).
5. `minOccurs=1, maxOccurs=unbounded` is represented by `NonEmptyVector`, preventing invalid empty values.
6. Optional values use `Option`; repeated values use `Vector`, preserving document order.
7. Protocol alternatives are exposed as union types; cross-cutting capabilities use intersection types.
8. Cross-field SHALL constraints that a product shape cannot express are exposed through the `ValidatedNode#validate`
   hook so documents can be rejected uniformly; constraint-free nodes simply return an empty vector.
9. Parsing/encoding will be separate from the domain algebra so the model remains pure and transport-independent;
   JSON-only members never appear in the domain.

## Delivery slices

- **Slice 1 (implemented):** common scalar vocabulary, extensions, XJDF/XJMF roots, Product/ProductList, Intent envelope,
  ResourceSet/Resource, partition keys, headers and abstract message families.
- **Slice 2 (implemented):** the five closed audit variants, status/resource/notification subelements, process-run data,
  amount/partition constraints, and a typed `FileSpec` graph including mutually exclusive locations, disposition, and
  network headers.
- **Slice 3 (implemented):** all 14 schema-defined chapter-4 ProductIntent descendants and their direct child algebras.
  `ProductIntent` remains open only because section 3.5.4 explicitly permits extension intents; `StandardProductIntent`
  exposes the complete closed union supplied by XJDF 2.2.
- **Slice 4 (implemented):** all 102 combined schema-index and normative 2.2 resources, grouped by
  general/prepress/press/postpress domains, including reusable and recursive child graphs. See
  [resource-coverage.md](resource-coverage.md).
- **Slice 5 (implemented):** all 44 concrete XJMF query/command/signal/response elements and their payload graphs.
  Complete closed unions are exposed as `StandardCommand`, `StandardQuery`, `StandardResponse`, `StandardSignal`, and
  `StandardMessage`; protocol traits remain open for foreign extensions. See [message-coverage.md](message-coverage.md).
  Audit payloads are complete in Slice 2.
- **Slice 6:** schema-derived XML and JSON codecs, validation laws, round-trip fixtures and compatibility tests.

Each slice must record the exact `xsdq bundle`, `attrs`, and `hierarchy` queries used for traceability.
