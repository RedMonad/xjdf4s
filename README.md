# xjdf4s

Immutable Scala 3 ADTs for the XJDF 2.x data model.

The project is intentionally split along protocol boundaries:

- `xjdf4s-core` — refined XSD scalar values, cardinalities, extension nodes and shared value objects;
- `xjdf4s-model` — XJDF job, product intent, resource-set and resource structures;
- `xjdf4s-messaging` — XJMF envelope, headers and the four message families;
- `xjdf4s-protocol` — public protocol-wide union/intersection types.

See [docs/model-plan.md](docs/model-plan.md) for the schema-driven implementation plan and traceability notes.

## Build

The build follows the checked-in sbt 2.x documentation: bare settings are common settings, the build DSL is Scala 3,
and slash syntax is used for scoped keys.

```bash
sbt "clean ; compile ; test"
```
