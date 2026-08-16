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
```

The index reports 365 elements, 366 complex types and 228 simple types. `SpecificResource` alone has 100 derived
complex types, so implementation is staged while keeping the public algebra extensible. The checked-in schema's `Version`
facet lists `2.0` and `2.1`, while the normative 2.2 prose requires `2.2`; the domain enum deliberately accepts all three
and records this source discrepancy rather than silently dropping the current specification value.

## Scala 3 modelling rules

1. XSD products become immutable `case class` values.
2. Closed enumerations and choices become `enum` coproducts.
3. Open substitution points (`SpecificResource`, XJMF message extensions and foreign namespaces) remain traits;
   sealing them would incorrectly prohibit conforming extensions.
4. XSD identifiers and constrained strings use zero-overhead `opaque type` values with smart constructors.
5. `minOccurs=1, maxOccurs=unbounded` is represented by `NonEmptyVector`, preventing invalid empty values.
6. Optional values use `Option`; repeated values use `Vector`, preserving document order.
7. Protocol alternatives are exposed as union types; cross-cutting capabilities use intersection types.
8. Parsing/encoding will be separate from the domain algebra so the model remains pure and transport-independent.

## Delivery slices

- **Slice 1 (implemented):** common scalar vocabulary, extensions, XJDF/XJMF roots, Product/ProductList, Intent envelope,
  ResourceSet/Resource, partition keys, headers and abstract message families.
- **Slice 2:** common subelements and audits; complete amount/partition structures.
- **Slice 3:** all product-intent ADTs from chapter 4.
- **Slice 4:** all 100 `SpecificResource` descendants grouped by general/prepress/press/postpress process domains.
- **Slice 5:** every concrete XJMF query/command/signal/response and audit payload.
- **Slice 6:** schema-derived XML and JSON codecs, validation laws, round-trip fixtures and compatibility tests.

Each slice must record the exact `xsdq bundle`, `attrs`, and `hierarchy` queries used for traceability.
