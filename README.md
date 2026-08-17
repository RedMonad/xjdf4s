# xjdf4s

Immutable Scala 3 ADTs for the XJDF 2.2 data model, built in the style of transport-neutral FP libraries: the domain
algebra is pure, typed invariants make invalid states unrepresentable, and cross-field SHALL constraints are exposed
through a compositional `validate` hook.

The project is intentionally split along protocol boundaries:

- `xjdf4s-core` — refined XSD scalar values (`Nmtoken`, `XsdId`/`XsdIdRef`, `XjdfString`, `Priority0To100`, temporal
  types), cardinality containers, namespace-safe extension nodes and the validation vocabulary (cats type class
  instances included);
- `xjdf4s-model` — XJDF job, product intent, resource-set and resource structures (102 standard resources, 14
  product intents) plus accumulating document validation;
- `xjdf4s-messaging` — XJMF envelope, headers and the four message families (44 concrete messages);
- `xjdf4s-protocol` — public protocol-wide union/intersection types;
- `xjdf4s-dsl` — Free-based DSL for constructing XJDF documents (one program, several interpreters).

The domain stays free of transport-specific members (the JSON `$schema`/`@Name` properties, XML escaping, MIME
types live in the codec/HTTP layers planned in the roadmap).

## Documentation

A small set of long-lived, foundational documents lives in [docs/](docs/):

- [docs/domain-model.md](docs/domain-model.md) — the domain architecture: modules, invariants, scalar vocabulary,
  normative corrections;
- [docs/fp-glossary.md](docs/fp-glossary.md) — minimal FP vocabulary for reading the code and the roadmap;
- [docs/free-dsl.md](docs/free-dsl.md) — the design of the Free-based construction DSL.

The development plan (data-model → codecs → transport → effects → publishing) lives in
[roadmap/](roadmap/README.md) — one file per stage.

## Build

The build follows the checked-in sbt 2.x documentation: bare settings are common settings, the build DSL is Scala 3,
and slash syntax is used for scoped keys.

```bash
sbt "clean ; compile ; test"
```
