# xjdf4s

Immutable Scala 3 ADTs for the XJDF 2.2 data model, built in the style of transport-neutral FP libraries: the domain
algebra is pure, typed invariants make invalid states unrepresentable, and cross-field SHALL constraints are exposed
through a compositional `validate` hook.

The project is intentionally split along protocol boundaries:

- `xjdf4s-core` — refined XSD scalar values (`Nmtoken`, `XsdId`/`XsdIdRef`, `XjdfString`, `Priority0To100`, temporal
  types), cardinality containers, namespace-safe extension nodes and the validation vocabulary;
- `xjdf4s-model` — XJDF job, product intent, resource-set and resource structures (102 standard resources, 14
  product intents);
- `xjdf4s-messaging` — XJMF envelope, headers and the four message families (44 concrete messages);
- `xjdf4s-protocol` — public protocol-wide union/intersection types.

The domain stays free of transport-specific members (the JSON `$schema`/`@Name` properties, XML escaping, MIME
types live in the codec/HTTP layers planned in the roadmap).

## Roadmap

The development plan from the finished data-model to a full http4s-style framework (cats type classes, Validated
validation, a Free-based construction DSL, XML/JSON codecs, XJMF transport, effects/HTTP runtime, laws and
publishing) lives in [roadmap/](roadmap/README.md) — one file per stage, with prerequisites from the bundled
`reference/cats` documentation, design rationale, code sketches and acceptance checklists for each stage.

## Build

The build follows the checked-in sbt 2.x documentation: bare settings are common settings, the build DSL is Scala 3,
and slash syntax is used for scoped keys.

```bash
sbt "clean ; compile ; test"
```
