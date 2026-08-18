package xjdf4s.model

import xjdf4s.core.ValidationError

/** `TransferFunction` (Appendix A.1.1): a sampled function encoded as an even-length float list of `x y` pairs.
 *  Odd-length lists and empty curves are rejected at construction.
 */
opaque type TransferFunction = Vector[Float]
object TransferFunction:
  def from(values: Vector[Float]): Either[ValidationError, TransferFunction] =
    Either.cond(
      values.nonEmpty && values.length % 2 == 0,
      values,
      ValidationError.OddListLength("TransferFunction", values.length),
    )

  extension (values: TransferFunction)
    def toVector: Vector[Float] = values
    def pairs: Vector[(Float, Float)] = values.grouped(2).map(pair => (pair(0), pair(1))).toVector
end TransferFunction

/** `@GluingPattern` (Glue, chapter 8): an even-length float list alternating glue line segment lengths (odd indices)
 *  and gap lengths (even indices). A solid line is expressed by the pattern `(1 0)`.
 */
opaque type GluingPattern = Vector[Float]
object GluingPattern:
  def from(values: Vector[Float]): Either[ValidationError, GluingPattern] =
    Either.cond(
      values.nonEmpty && values.length % 2 == 0,
      values,
      ValidationError.OddListLength("GluingPattern", values.length),
    )

  extension (values: GluingPattern)
    def toVector: Vector[Float] = values
    def segmentLengths: Vector[Float] = values.zipWithIndex.collect { case (value, index) if index % 2 == 0 => value }
    def gapLengths: Vector[Float] = values.zipWithIndex.collect { case (value, index) if index % 2 == 1 => value }
end GluingPattern

/** `FoldCatalog` (XSD `pattern = F[0-9]+-([0-9]+|X)` over NMTOKEN): identifiers of folding patterns from the
 *  Folding Catalog (Appendix E). The pattern semantics are preserved instead of weakening the type to a bare
 *  NMTOKEN.
 */
opaque type FoldCatalog = String
object FoldCatalog:
  private val Pattern = "F[0-9]+-([0-9]+|X)".r

  def from(value: String): Either[ValidationError, FoldCatalog] =
    Either.cond(
      Pattern.matches(value),
      value,
      ValidationError.InvalidValue("FoldCatalog", value, "an identifier matching F[0-9]+-([0-9]+|X)"),
    )

  extension (value: FoldCatalog) def value: String = value
end FoldCatalog

/** Neutral density (Patch and Color resources, chapter 8): a float in the range `[0.001..10]`, defined as
 *  `10 * log10(1/Y)` with the tristimulus value Y normalized to 1.0.
 */
opaque type NeutralDensity = Float
object NeutralDensity:
  def from(value: Float): Either[ValidationError, NeutralDensity] =
    Either.cond(
      value >= 0.001f && value <= 10.0f,
      value,
      ValidationError.ValueOutOfBounds("NeutralDensity", value.toString, "[0.001..10]"),
    )

  extension (value: NeutralDensity) def value: Float = value
end NeutralDensity
