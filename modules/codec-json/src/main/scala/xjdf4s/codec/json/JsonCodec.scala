package xjdf4s.codec.json

/**
 * JSON codec facade: re-exports the shared helpers and the given instances of the codec objects at package level,
 * so users import a single `xjdf4s.codec.json.given`. The codec objects themselves depend on [[JsonHelpers]], not
 * on this facade - the exports point outward only.
 */
export JsonHelpers.*
export JsonNodeCodecs.given
export JsonScalars.given
export JsonMediaCodecs.given
export JsonMessagingCodecs.given
