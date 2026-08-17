# Registry of documented XSD/normative choices

Priority order: **normative tables and prose > XSD/index > Scala model > docs**. Every deliberate divergence between
the sources is recorded here so that "intentional" never decays into "accidental".

## Normative corrections over stale/corrupt XSD

| Topic | Normative | Checked-in XSD | Model | Verdict |
|---|---|---|---|---|
| `Version` (XJDF/XJMF `@Version`) | `2.0`, `2.1`, `2.2` (Table A.52) | only `2.0`, `2.1` | `V2_0`, `V2_1`, `V2_2` | Correct |
| `JdfVersion` (`Device/@JDFVersions`) | JDF `1.0`–`1.8` + XJDF `2.x` (Table A.27) | stale | `V1_0`…`V1_8`, `V2_0`…`V2_2` | Correct |
| `Resource/@Brand` | `string` | `boolean` | `XjdfString` | Correct |
| `Part/@BlockName` | `NMTOKEN` | `XYPair` | `Nmtoken` | Correct |
| `PartWaste/@ModuleIDs` | `NMTOKENS` | `float` | `NonEmptyVector[Nmtoken]` | Correct |
| `AssemblyItem/@ChildRef` | `IDREF` | `float` | `XsdIdRef` | Correct |
| `ResourceSet/@CombinedProcessIndex` | `IntegerList` (Table 3.12) | `FloatList` | `Vector[Int]` | Correct |
| `Condition/@PartContext` | `NMTOKENS` (Table 8.15) | singular `NMTOKEN` | `Vector[Nmtoken]` | Correct |
| `ColorMeasurementConditions/@Illumination` | `NMTOKEN` (Table 8.13) | `float` | `Nmtoken` | Correct |
| `GeneralId/@DataType` | Table A.14 (`boolean`…`NamedFeature`) | stale lexical forms | enum per Table A.14 | Correct |
| `RunList/@Docs` | `IntegerRange` (Table 6.148) | `IntegerList` | `IntegerRange` | Correct |
| `FileSpec/@NPage` | New in 2.2 (Table 8.17) | absent | `numberOfPages: Int` | Correct |
| `Device/@RestApiBaseURL` | New in 2.2 | absent | `UriRef` | Correct |
| `ShapeDef/RuleLength`, multiple `FileSpec` | 2.2 changes | stale | present | Correct |
| `CommandShutDown` params | normative optional | `minOccurs=1` | `Option` | Correct |
| `BoxFoldingParams` | 2.2 `BoxFoldAction*` + `Action="Glue"` (Tables 6.17/6.19/6.20) | stale | ordered actions + optional legacy glues | Correct |
| `MediaLayers` | ordered `Glue* | Media*` (8.28) | stale | ordered `Vector[MediaLayer]` | Correct |

## Deliberate refinements (documented narrowings)

| Topic | Decision |
|---|---|
| `LabColor` | `L` restricted to `[0..100]` (normative); `a`/`b` unbounded. |
| `CMYKColor` | components restricted to `[0..1.0]` (normative, Appendix A.1). |
| `sRGBColor` | components restricted to `[0..1.0]`: the XSD restricts only list length; the range is the sRGB value space. |
| `NamedColor` | closed 147-name enum generated from the XSD pattern vocabulary (`[Color Names]`); case-insensitive matching. |
| `FoldCatalog` | opaque type preserving the XSD pattern `F[0-9]+-([0-9]+|X)` (no longer a bare `Nmtoken`). |
| `TransferFunction` / `GluingPattern` | opaque even-length float lists; empty and odd lists rejected. |
| `NeutralDensity` | opaque float in `[0.001..10]` per Tables 8.10/6.x. |
| `Priority0To100` | single shared 0..100 opaque type for `Disposition/@Priority`, `NodeInfo/@JobPriority`, `GangElement/@Priority` and queue priorities. |
| `XjdfString` | all normative `string` attributes use the opaque `XjdfString` (normalized, ≤ 1023 characters, no tabs/line feeds/control chars). XML `text` bodies remain `String` (`Comment/@Value`, `Address/AddressLine`, `Company/OrganizationalUnit`, TIFF tag text). `Part/@Metadata` remains `String` as `regExp` with regex validity checked in `validate`. |
| `LayoutIntent/@NumberUp` | integer `GridSize` instead of XSD `XYPair` ("both numbers are integers" per the normative text). |
| `XsdDateTime` | time zone mandatory (normative `dateTime`); calendar values validated. `XsdDuration` rejects empty `P`/`PT` forms. |
| `ID` vs `IDREF` | declarations use `XsdId`, references use `XsdIdRef`; the two cannot be confused at the type level. Document-level uniqueness is checked by `XJDF#validate` / `XJMF#validate`; reference target-type checks are codec-layer policy. |
| Float/Double | geometric and color values use `Double`, measured/scalar values use `Float`; a unified policy is deferred to the codec slice. |
| Strict equality | `-language:strictEquality` is not enabled yet: it requires a uniform `CanEqual` sweep that is planned together with the codec slice. |

## Known index/tooling artifacts (not model defects)

| Topic | Fact |
|---|---|
| `xsdq hierarchy SpecificResource` | default `--limit 100` truncates `substitutionMembers` to 100; `--limit 200` returns all 101 including `RasterReadingParams`. |
| `RasterReadingParams` | XSD substitution member present in the model and in `StandardSpecificResource`. |
| `SheetOptimizingReport` | normative Table 6.162 resource absent from the XSD; present in the model. |

## Transport policy (JSON exceptions)

The domain model is transport-independent. The following JSON-only normative requirements belong to the codec layer
(planned Slice 6) and are intentionally not represented in the domain:

- JSON `$schema` and `@Name` members of `XJDF`/`XJMF`;
- JSON exactly-one-message restriction of `XJMF`;
- JSON `@Name` in-lining exception of `MediaLayers`;
- XML element-name emission for normative names (`Patch`, `ResourceQuParams`, `StatusQuParams`) and wildcard
  position enforcement (`anyAttribute` only vs. element-capable nodes).

## Validation scope and deferred document-level checks

`ValidatedNode#validate` covers the cross-field SHALL constraints that are local to one node: `Resource` placement and
timing XOR pairs, `Media`/`MediaIntent` back-side companions, `MediaLayers` front/back boundaries, `Part` range
ordering and `regExp` metadata, `SignalResource` replacement-window ordering, and document-level ID uniqueness
(`XJDF`, `XJMF`). The remaining whole-document checks are graph problems and are deferred to the validator/codec
slice together with their tests:

- IDREF existence and target-type correctness across the full reference graph (the `XsdId`/`XsdIdRef` split makes
  the graph typeable; walking it needs the codec's node registry);
- Query payload vs. `Subscription` filter exclusions (`ResourceQuParams`/`StatusQuParams` vs. `Subscription`);
- product amount contradictions (`@Amount` vs. `@MinAmount`/`@MaxAmount`) and `PartWaste` totals;
- queue-entry move-target and priority coherence checks beyond the type-level single-target guarantee.

Nothing on this list changes the *representable* data; each item is either already prevented by a type shape or
becomes rejectable through `validate` once the document traversal layer exists.

