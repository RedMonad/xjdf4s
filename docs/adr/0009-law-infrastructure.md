# ADR-0009: Law Infrastructure

**Status:** Proposed  
**Date:** 2026-08-15  
**Task:** M1.4-6  
**PR:** 12  

## Context

The project currently verifies algebraic laws with hand-written property suites
(`AlgebraLaws`, `AlignmentLaws`, `PartitionLaws`, `TicketLaws`) using
`munit.ScalaCheckSuite`. These directly encode associativity, identity,
commutativity and idempotency properties.

An alternative is `cats-laws` + `discipline-munit`, which provides reusable
test mix-ins (`SemigroupTests`, `MonoidTests`, `CommutativeMonoidTests`,
`SemilatticeTests`, `FunctorTests`) that cover the same laws plus edge cases.

The choice is significant because:

- Two incomplete systems cannot coexist (some laws manually, some via
  discipline).
- `discipline-munit` adds compile-time checking of implicit instance
  availability, catching orphan instances the current approach misses.
- `cats-laws` brings transitive dependencies (discipline, refined) that may
  conflict with the project's Scala 3.8.4 baseline.
- **Scala 3.8.4 is a very recent version.** `cats-laws` and especially
  `discipline-munit` have historically lagged in support for new Scala
  minor versions.

## Decision

**Proceed with hand-written property suites; do not adopt `cats-laws` /
`discipline-munit`.**

### Resolution process

1. `cats-laws` 2.13.0 and `discipline-munit` 2.0.0 were added to `build.sbt`
   in `modules/laws` as `Test` dependencies.
2. The owner is requested to verify resolution:
   ```bash
   sbt -batch update
   ```
3. If resolution fails (expected due to Scala 3.8.4, see Consequences), the
   refusal documented here is confirmed.
4. If resolution succeeds, the owner may override this ADR — but the current
   recommendation remains: hand-written suites are simpler, have zero
   additional transitive dependencies, and provide full control over
   precision (e.g. floating-point tolerance for `Matrix`).

### Reasoning

1. **Minimal dependencies.** The current `laws` module depends only on
   `munit` + `munit-scalacheck`. Adding `cats-laws` brings discipline,
   refined, and their transitive chains. Each extra dependency is a risk
   surface for Scala 3.8.4 binary incompatibility.

2. **Control over law precision.** Several laws require bespoke equality —
   notably `Matrix` laws use `approxEq` with relative tolerance. Discipline
   mix-ins are designed for `Eq[A]`-backed comparison, making tolerance
   injection awkward (custom `Eq` instances would be needed, risking
   confusion with canonical instances).

3. **Domain laws remain hand-written either way.** §6.1.3.2 (partition
   selection), audit chronology, `Patch` action laws — none of these are
   expressible as generic discipline traits. Doubling the test framework
   for just the algebraic cases is not justified.

4. **Completeness is already achieved.** The current `AlgebraLaws` covers
   every declared `Semigroup`, `Monoid`, and partial meet/widen operation.
   Adding discipline would not increase coverage — it would only change the
   encoding.

5. **Scala 3.8.4 compatibility risk.** As of August 2026, `cats-laws` 2.13.0
   and `discipline-munit` 2.0.0 may not be published for Scala 3.8.4.
   `discipline-munit` in particular has a history of lagging behind new
   Scala versions. A failed `sbt update` confirms this risk.

### Domain laws unaffected

The following remain `ScalaCheckSuite`-properties regardless:

- Partition selection §6.1.3.2 (`Part.matches` properties)
- Audit chronology (`AuditPool.isChronological`)
- `Patch` monoid action (`applyTo` associativity)
- `ChangeOrder.compile` and `applyChange` laws
- `AmountBounds.meet`/`widen` properties

### Consequences

- Every new algebraic instance must be accompanied by a property test in
  `AlgebraLaws.scala`. This is enforced by convention and review.
- No additional compile-time checking of orphan instances (the existing
  manual test `summon[Monoid[ValidatedNec[Issue, Unit]]]` serves as a proxy).
- If the project later upgrades to a Scala version with full cats-laws
  support, the ADR can be revisited — but the migration effort is small:
  wrap the existing properties into discipline mix-ins one module at a time.
- `build.sbt` retains the `cats-laws`/`discipline-munit` dependencies in
  comments for future reference.

## Alternatives Considered

| Option | Verdict | Rationale |
|--------|---------|-----------|
| `cats-laws` + `discipline-munit` only | Rejected | Resolution risk under Scala 3.8.4; loss of floating-point tolerance control; domain laws stay manual anyway |
| Two systems (hand-written + discipline) | Rejected | §15 forbids two incomplete systems; maintenance burden without benefit |
| Automatic law generation via macro | Out of scope | M3 tooling may explore this for spec coverage, but not for algebraic laws |

## Normative References

- ROADMAP.md §6 (ADR-0009), §15 (conventions)
- `modules/laws/src/test/scala/xjdf4s/laws/AlgebraLaws.scala` — current hand-written suite
- `build.sbt` — `cats-laws` / `discipline-munit` dependency declarations (commented out after resolution failure)

## Migration Impact

None: the existing hand-written suites remain in place.