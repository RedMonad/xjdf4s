package xjdf4s
package model

import xjdf4s.model.elements.GeneralID
import xjdf4s.prim.*
import cats.Show
import cats.data.Chain
import cats.kernel.{Eq, Monoid}

/** A NamedFeature (§3.1.3.1): the projection of a
 *  `GeneralID[@DataType="NamedFeature"]` element (§8.23 / Table 8.28).
 *
 *  > XJDF MAY contain zero or more `GeneralID[@Datatype="NamedFeature"]`
 *  > elements to specify global setup definitions. These `GeneralID` elements
 *  > … allow a Controller to define a named set of parameters for processes
 *  > that SHALL be executed without defining the details or even the
 *  > resources.
 *
 *  Appendix A, Table A.14 describes the value as «identified by a specific
 *  name; thus … it is expected to have both an item containing the value and
 *  an item containing the name», i.e. `@IDUsage` carries the name and
 *  `@IDValue` the value — exactly the two required attributes of Table 8.28.
 *
 *  This is a lossless projection: `toGeneralID` reconstructs the element the
 *  value came from, so the round trip through `NamedFeature.from` is the
 *  identity on named features (the same technique as `FileSpec.location` /
 *  `FileLocation`, N-51).
 */
final case class NamedFeature(name: NmToken, value: XjdfString):

  /** The `GeneralID` element this named feature projects from. */
  def toGeneralID: GeneralID =
    GeneralID(idUsage = name, idValue = value, dataType = Some(DataType.NamedFeature))

end NamedFeature

object NamedFeature:

  /** The projection of a single `GeneralID`; `None` for any other data type,
   *  including an absent `@DataType` (Table 8.28 makes the attribute optional
   *  and §3.1.3.1 only speaks about `@DataType="NamedFeature"`).
   */
  def from(generalId: GeneralID): Option[NamedFeature] =
    Option.when(generalId.isNamedFeature)(NamedFeature(generalId.idUsage, generalId.idValue))

  /** The named features of a `GeneralID*` chain, in document order. */
  def collect(generalIds: Chain[GeneralID]): Chain[NamedFeature] =
    generalIds.flatMap(g => Chain.fromOption(from(g)))

  given Show[NamedFeature] =
    Show.show(f => s"NamedFeature(${f.name.value}=${f.value.value})")

  given Eq[NamedFeature] = Eq.fromUniversalEquals

end NamedFeature

/** A set of Traits addressed by simple XPath expressions (§1.3, glossary
 *  «Trait»: «a Trait of that element is either a single Subelement of it, a
 *  single attribute of it or a single attribute value of one of its
 *  Attributes»).
 *
 *  A `TraitSet` is the carrier of the §3.1.3.1 override rule: implied Traits
 *  come from the setup definition a Controller attaches to a NamedFeature,
 *  explicit Traits are those written in the XJDF document itself.
 *
 *  Algebraically the set is the right-biased overlay monoid over
 *  `XjdfXPath => XjdfString`: `combine` keeps the right-hand value on a
 *  common key and `empty` is the neutral element. That is precisely the
 *  operation §3.1.3.1 asks for — see `NamedFeatures.resolve`, which puts the
 *  explicit Traits on the right.
 *
 *  The key type is the XJDF data type `XjdfXPath` (Table A.1), not the
 *  validation locator `model.XPath` (ADR-0013, N-54).
 */
opaque type TraitSet = Map[XjdfXPath, XjdfString]

object TraitSet:

  /** The empty Trait set — no Trait is specified. */
  val empty: TraitSet = Map.empty

  def of(entries: (XjdfXPath, XjdfString)*): TraitSet = entries.toMap

  def fromMap(entries: Map[XjdfXPath, XjdfString]): TraitSet = entries

  /** The underlying map. Private so the opaque type stays the only public
   *  representation; every extension below is expressed through it, which
   *  also keeps the accessors from resolving to themselves.
   */
  private def underlying(traits: TraitSet): Map[XjdfXPath, XjdfString] = traits

  extension (traits: TraitSet)

    /** The value of a single Trait, if it is specified. */
    def get(path: XjdfXPath): Option[XjdfString] = underlying(traits).get(path)

    /** True when the Trait is specified by this set. */
    def contains(path: XjdfXPath): Boolean = underlying(traits).contains(path)

    def isEmpty: Boolean = underlying(traits).isEmpty

    def nonEmpty: Boolean = underlying(traits).nonEmpty

    def size: Int = underlying(traits).size

    /** The addressed Traits, sorted by XPath so renders and reports are
     *  deterministic.
     */
    def paths: List[XjdfXPath] = entriesOf(traits).map(_._1)

    /** The entries, sorted by XPath. */
    def entries: List[(XjdfXPath, XjdfString)] = entriesOf(traits)

    def toMap: Map[XjdfXPath, XjdfString] = underlying(traits)

  private def entriesOf(traits: TraitSet): List[(XjdfXPath, XjdfString)] =
    underlying(traits).toList.sortBy((path, _) => path.value)

  given Monoid[TraitSet] with
    def empty: TraitSet = TraitSet.empty
    def combine(x: TraitSet, y: TraitSet): TraitSet = underlying(x) ++ underlying(y)

  given Show[TraitSet] =
    Show.show { traits =>
      entriesOf(traits)
        .map((path, value) => s"${path.value}=${value.value}")
        .mkString("TraitSet(", ", ", ")")
    }

  given Eq[TraitSet] = Eq.fromUniversalEquals

end TraitSet

/** The outcome of applying §3.1.3.1 to one ticket: the Traits that are in
 *  force, plus the implied Traits that were overridden by explicit ones.
 *
 *  `overridden` is reported at `Information` severity by `issues`: an override
 *  is normal, expected behaviour — the specification mandates it — so it is
 *  never an error (ADR-0006).
 */
final case class TraitResolution(resolved: TraitSet, overridden: Chain[XjdfXPath]):

  /** Diagnostics describing every implied Trait that lost to an explicit one. */
  def issues: Chain[Issue] =
    overridden.map: path =>
      Issue(
        severity = SeverityClass.Information,
        location = XPath(path.value),
        message =
          "Explicitly specified Trait overrides the Trait implied by " +
            "GeneralID[@DataType=\"NamedFeature\"] (§3.1.3.1)",
        code = Some(IssueCode.NamedFeatureTraitOverridden)
      )

end TraitResolution

object TraitResolution:

  given Show[TraitResolution] =
    Show.show(r => s"TraitResolution(${Show[TraitSet].show(r.resolved)}, overridden=${r.overridden.size})")

  given Eq[TraitResolution] = Eq.fromUniversalEquals

end TraitResolution

/** §3.1.3.1 «Specifying NamedFeatures with GeneralID»: global setup
 *  definitions and the precedence of explicit Traits.
 *
 *  > Explicitly specified Traits SHALL override any implied Traits defined by
 *  > `GeneralID[@Datatype="NamedFeature"]`.
 *
 *  A NamedFeature names a setup definition that lives *outside* the document —
 *  «a named set of parameters for processes that SHALL be executed without
 *  defining the details or even the resources». The implied Traits are
 *  therefore supplied by a Controller- or Device-side registry, modelled here
 *  as a plain function `NamedFeature => TraitSet`. That is why the override
 *  rule is expressed as resolution semantics with laws rather than as a
 *  `DomainRule` of the root validator: a single ticket does not contain the
 *  implied side of the comparison, so no ticket can violate the rule on its
 *  own (SPEC-COVERAGE, Deliberate Deviations).
 *
 *  What the root validator does check locally is Table 8.28 — the value of
 *  `@IDValue` SHALL correspond to `@DataType` (`GeneralID.law`).
 */
object NamedFeatures:

  /** A Controller-side setup registry: the Traits a NamedFeature stands for. */
  type Registry = NamedFeature => TraitSet

  /** A registry that implies nothing — the identity element of resolution. */
  val emptyRegistry: Registry = _ => TraitSet.empty

  /** Builds a registry from an explicit table, defaulting to no Traits. */
  def registryOf(table: Map[NamedFeature, TraitSet]): Registry =
    feature => table.getOrElse(feature, TraitSet.empty)

  /** The Traits implied by a chain of named features, in document order:
   *  later features override earlier ones on a common Trait, exactly as
   *  later explicit values do.
   */
  def implied(features: Chain[NamedFeature], registry: Registry): TraitSet =
    features.foldLeft(TraitSet.empty)((acc, feature) => Monoid[TraitSet].combine(acc, registry(feature)))

  /** §3.1.3.1: explicit Traits override implied ones. The resolved set is the
   *  right-biased overlay `implied |+| explicit`; the overridden Traits are
   *  the implied ones the explicit set replaced with a *different* value.
   */
  def resolve(impliedTraits: TraitSet, explicitTraits: TraitSet): TraitResolution =
    val overridden = Chain.fromSeq(
      impliedTraits.entries.collect {
        case (path, impliedValue) if explicitTraits.get(path).exists(explicit => explicit != impliedValue) =>
          path
      }
    )
    TraitResolution(Monoid[TraitSet].combine(impliedTraits, explicitTraits), overridden)

  /** Resolves the Traits of a ticket: the NamedFeatures of `XJDF/GeneralID`
   *  expanded through `registry`, overridden by the explicitly specified
   *  Traits of the same ticket.
   */
  def resolveTicket(ticket: XJDF, registry: Registry, explicitTraits: TraitSet): TraitResolution =
    resolve(implied(ticket.namedFeatures, registry), explicitTraits)

end NamedFeatures

/** §3.1.3.1: the NamedFeatures of a ticket — the projection of every
 *  `XJDF/GeneralID[@DataType="NamedFeature"]`, in document order.
 *
 *  A top-level extension rather than a member of `XJDF`, so the file
 *  dependency stays one-directional (`NamedFeatures.scala → Ticket.scala`,
 *  N-21) — the same technique as `XJDF.validate` in `TicketValidator.scala`.
 *  Source-compatible wherever `xjdf4s.model.*` is imported.
 */
extension (ticket: XJDF) def namedFeatures: Chain[NamedFeature] = NamedFeature.collect(ticket.generalIds)
