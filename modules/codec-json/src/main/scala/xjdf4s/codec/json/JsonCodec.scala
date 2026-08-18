package xjdf4s.codec.json

/** JSON codec facade: re-exports the shared helpers and the given instances of the base codec objects at package
 *  level, so users import a single `xjdf4s.codec.json.given`. The codec objects themselves depend on
 *  [[JsonHelpers]], not on this facade - the exports point outward only.
 *
 *  NOTE: top-level `export` clauses are the link that closes compiler cycles (dotty #25894 / #26340, see
 *  Typer.typedPackageDef): the package lookup during an implicit search resolves the export forwarders of the
 *  very file whose givens are being typed, chaining back through the package class ("Cyclic reference involving
 *  val <import>"). The export set is therefore frozen to the five base objects proven cycle-free by the green
 *  builds; the audit, root, special and resource codecs are consumed through explicit `import X.given` clauses
 *  at their use sites instead.
 *
 *  NOTE 2: JsonMediaCodecs relies on these facade forwarders for its own internal forward references
 *  (Media's codec references the later-defined MediaLayers codecs - the package-level forwarder makes the
 *  reference visible). Do not remove it from this export set, and keep the same mechanism in mind before
 *  reordering that file.
 */
export JsonHelpers.*
export JsonNodeCodecs.given
export JsonScalars.given
export JsonMediaCodecs.given
export JsonMessagingCodecs.given
