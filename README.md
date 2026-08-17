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

The data-model is complete at the entity-name level and consolidated against the XJDF/XJMF 2.2 audit reports
(`AUDIT.md`, `FACTS-A/B/C.md`): see [docs/model-plan.md](docs/model-plan.md) for the implementation plan,
[docs/resource-coverage.md](docs/resource-coverage.md) and [docs/message-coverage.md](docs/message-coverage.md) for
coverage, and [docs/normative-choices.md](docs/normative-choices.md) for the register of deliberate
XSD/normative divergences.

Transport (XML/JSON codecs) is a separate planned slice; the domain stays free of transport-specific members such as
the JSON `$schema`/`@Name` properties.

## Build

The build follows the checked-in sbt 2.x documentation: bare settings are common settings, the build DSL is Scala 3,
and slash syntax is used for scoped keys.

```bash
sbt "clean ; compile ; test"
```
