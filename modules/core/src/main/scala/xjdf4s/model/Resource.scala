package xjdf4s
package model

import xjdf4s.model.elements.{Comment, Dependent, GeneralID}
import xjdf4s.prim.*
import xjdf4s.resources.ResourcePayload
import cats.Show
import cats.data.{Chain, NonEmptyChain}
import cats.kernel.Eq

/** A process type — a value of `XJDF/@Types` (Table 3.1, Chapter 5). The
 *  predefined tokens are a catalog; extension process types may use a namespace
 *  prefix (§3.5.6).
 */
opaque type ProcessType = NmToken

object ProcessType:

  def apply(token: NmToken): ProcessType = token

  def from(raw: String): Option[ProcessType] = NmToken.from(raw)

  extension (pt: ProcessType)
    def toNmToken: NmToken = pt
    def isExtension: Boolean = pt.value.contains(':')

  /** `"Product"` — the ticket specifies products without process details (§3.1.2). */
  val Product: ProcessType = NmToken.unsafe("Product")

  // A representative subset of Chapter 5 processes.
  val Approval: ProcessType = NmToken.unsafe("Approval")
  val Binding: ProcessType = NmToken.unsafe("Binding")
  val Collecting: ProcessType = NmToken.unsafe("Collecting")
  val ColorSpaceConversion: ProcessType = NmToken.unsafe("ColorSpaceConversion")
  val ConventionalPrinting: ProcessType = NmToken.unsafe("ConventionalPrinting")
  val Cutting: ProcessType = NmToken.unsafe("Cutting")
  val Delivery: ProcessType = NmToken.unsafe("Delivery")
  val DigitalPrinting: ProcessType = NmToken.unsafe("DigitalPrinting")
  val Embossing: ProcessType = NmToken.unsafe("Embossing")
  val Folding: ProcessType = NmToken.unsafe("Folding")
  val Gathering: ProcessType = NmToken.unsafe("Gathering")
  val HoleMaking: ProcessType = NmToken.unsafe("HoleMaking")
  val Imposition: ProcessType = NmToken.unsafe("Imposition")
  val Interpreting: ProcessType = NmToken.unsafe("Interpreting")
  val Laminating: ProcessType = NmToken.unsafe("Laminating")
  val ManualLabor: ProcessType = NmToken.unsafe("ManualLabor")
  val Preflight: ProcessType = NmToken.unsafe("Preflight")
  val QualityControl: ProcessType = NmToken.unsafe("QualityControl")
  val Rendering: ProcessType = NmToken.unsafe("Rendering")
  val Stitching: ProcessType = NmToken.unsafe("Stitching")
  val Stripping: ProcessType = NmToken.unsafe("Stripping")

  given Show[ProcessType] = Show.show(_.value)

  given Eq[ProcessType] = Eq.fromUniversalEquals

end ProcessType

/** `XJDF/@Types` (§5.2): the ordered list of processes executed by this XJDF.
 *  Categorically this is a *word* — a morphism — of the free category generated
 *  by the graph of process transitions (see `ProcessPath`).
 */
final case class ProcessPath(steps: NonEmptyChain[ProcessType]):

  def contains(pt: ProcessType): Boolean = steps.exists(_ == pt)

  def size: Int = steps.toChain.size.toInt

  /** The process at a zero-based position of the path. */
  def at(index: Int): Option[ProcessType] = steps.toChain.toList.lift(index)

  /** The zero-based positions at which `pt` occurs. */
  def indicesOf(pt: ProcessType): Chain[Int] =
    Chain.fromSeq(steps.toChain.toList.zipWithIndex.collect { case (`pt`, i) => i })
end ProcessPath

object ProcessPath:

  def of(head: ProcessType, tail: ProcessType*): ProcessPath =
    ProcessPath(NonEmptyChain(head, tail*))

  /** A product ticket: the single word `"Product"`. */
  val product: ProcessPath = ProcessPath(NonEmptyChain.one(ProcessType.Product))

  given Show[ProcessPath] = Show.show(_.steps.toChain.toList.map(Show[ProcessType].show).mkString(" "))

  given Eq[ProcessPath] = Eq.fromUniversalEquals

end ProcessPath

/** `ResourceSet/@CombinedProcessIndex` (§3.4, §5.2): the zero-based index of a
 *  process within the complete list of `XJDF/@Types`. Bounds are validated
 *  against the enclosing ticket's `ProcessPath`.
 */
opaque type ProcessIndex = Int

object ProcessIndex:

  def from(i: Int): Option[ProcessIndex] = Option.when(i >= 0)(i)

  def unsafe(i: Int): ProcessIndex =
    from(i).getOrElse(throw new IllegalArgumentException(s"Negative process index: $i"))

  extension (ix: ProcessIndex) def value: Int = ix

  given Show[ProcessIndex] = Show.show(_.value.toString)

  given Eq[ProcessIndex] = Eq.fromUniversalEquals

end ProcessIndex

/** `ResourceSet/@Name` (Table 3.12): the name of the explicit resource that the
 *  ResourceSet represents. Names of standard resources have no namespace prefix;
 *  proprietary resources use one (§3.5.2).
 */
opaque type ResourceSetName = NmToken

object ResourceSetName:

  def from(raw: String): Option[ResourceSetName] =
    NmToken.from(raw).flatMap { t =>
      t.value.indexOf(':') match
        case -1 => Some(t)
        case i => Option.when(i > 0 && i < t.value.length - 1)(t)
    }

  def unsafe(raw: String): ResourceSetName =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid ResourceSet name: '$raw'"))

  def of(token: NmToken): ResourceSetName = token

  extension (name: ResourceSetName)
    def toNmToken: NmToken = name
    def isExtension: Boolean = name.value.contains(':')

  given Show[ResourceSetName] = Show.show(_.value)

  given Eq[ResourceSetName] = Eq.fromUniversalEquals

end ResourceSetName

/** The identification key of a ResourceSet within a ticket (§3.4). */
final case class ResourceSetKey(
    name: ResourceSetName,
    usage: Option[Usage],
    processUsage: Option[NmToken],
    combinedProcessIndex: Option[NonEmptyChain[ProcessIndex]]
)

object ResourceSetKey:

  given Show[ResourceSetKey] = Show.fromToString

  given Eq[ResourceSetKey] = Eq.fromUniversalEquals

end ResourceSetKey

/** `ResourceSet` (Table 3.12): a set of `Resource` elements that are logically
 *  grouped together — physical entities such as paper or logical entities such
 *  as process parameters.
 *
 *  §3.4: ResourceSet elements with the same values of `@Name`, `@Usage`,
 *  `@ProcessUsage` and common or no entries in `@CombinedProcessIndex` SHALL NOT
 *  be specified — i.e. `key` is unique within a ticket (checked by
 *  `XJDF.validate`).
 */
final case class ResourceSet(
    name: ResourceSetName,
    usage: Option[Usage] = None,
    processUsage: Option[NmToken] = None,
    combinedProcessIndex: Option[NonEmptyChain[ProcessIndex]] = None,
    unit: Option[NmToken] = None,
    id: Option[Id] = None,
    commentUrl: Option[Url] = None,
    descriptiveName: Option[XjdfString] = None,
    resources: Chain[Resource] = Chain.empty,
    comments: Chain[Comment] = Chain.empty,
    dependents: Chain[Dependent] = Chain.empty,
    generalIds: Chain[GeneralID] = Chain.empty
) extends Named[ResourceSetName]:

  /** The §3.4 uniqueness key of this ResourceSet. */
  def key: ResourceSetKey = ResourceSetKey(name, usage, processUsage, combinedProcessIndex)

  /** §6.1.3.2 Selecting a Partition: iterate the Resource elements top to
   *  bottom; the first Resource with no mismatching Part attributes is selected.
   *  A Resource referenced by `@ID` is selected directly, ignoring its parts.
   */
  def select(selector: Part): Option[Resource] =
    resources.iterator.find(_.matches(selector))

  /** All Resources matching the selector, in document order. */
  def selectAll(selector: Part): Chain[Resource] =
    resources.filter(_.matches(selector))

  /** Direct selection by document-scoped `@ID` (§6.1.3.2). */
  def byId(id: Id): Option[Resource] =
    resources.iterator.find(_.id.contains(id))

  /** All IDREFs referenced by the resources in this set. */
  def references: Chain[IdRef] = resources.flatMap(_.references)

  /** True when every child Resource with a payload matches the `@Name` of this set.
   *  Bodyless Resource elements (`<Resource/>`, Table 6.1) are lawful in any ResourceSet.
   */
  def hasLawfulChildren: Boolean =
    resources.forall(r => r.elementName.forall(_ == name.toNmToken))

  /** True when `@Usage` and the resource statuses are consistent (Table 6.1). */
  def hasLawfulStatuses: Boolean =
    if usage.contains(Usage.Output) then resources.forall(_.status.isEmpty) else true
end ResourceSet

/** Local laws of `ResourceSet` (Table 6.1): @Name↔payload pairing and the
 *  `@Status` / `@Usage="Output"` exclusion. Explicitly invoked from
 *  `TicketValidator.checkLocalLaws`.
 */
object ResourceSetLaw:

  /** Table 6.1: a specific resource element name SHALL match `ResourceSet/@Name`.
   *  Bodyless `<Resource/>` elements (no payload) are skipped.
   */
  val children: DomainRule[ResourceSet] =
    (rs: ResourceSet, at: XPath) =>
      rs.resources
        .filterNot(r => r.elementName.forall(_ == rs.name.toNmToken))
        .map { r =>
          val actual = r.elementName.fold("<bodyless>")(_.value)
          Issue.errorC(
            IssueCode.ResourceSetChildNameMismatch,
            at,
            s"Resource element '$actual' does not match ResourceSet/@Name='${rs.name.toNmToken.value}' (Table 6.1)"
          )
        }

  /** Table 6.1: `@Status` SHALL NOT be specified if `ResourceSet/@Usage="Output"`. */
  val statuses: DomainRule[ResourceSet] =
    (rs: ResourceSet, at: XPath) =>
      if rs.usage.contains(Usage.Output) then
        rs.resources.filter(_.status.isDefined).map { r =>
          Issue.errorC(
            IssueCode.ResourceStatusOnOutput,
            at,
            s"Resource @Status SHALL NOT be specified for Usage=\"Output\" (Table 6.1): $r"
          )
        }
      else Chain.empty

object ResourceSet:

  /** §3.4: two ResourceSets clash when `@Name`/`@Usage`/`@ProcessUsage` are equal AND
   *  their `@CombinedProcessIndex` lists have common entries, or either list is absent
   *  ("no entries" applies to all processes). The single conflict predicate shared by
   *  the validator and `Patch.mergeResourceSets` (M1.1-2).
   */
  def clashesWith(a: ResourceSet, b: ResourceSet): Boolean =
    a.name == b.name && a.usage == b.usage && a.processUsage == b.processUsage &&
      cpiOverlap(a.combinedProcessIndex, b.combinedProcessIndex)

  private def cpiOverlap(
      a: Option[NonEmptyChain[ProcessIndex]],
      b: Option[NonEmptyChain[ProcessIndex]]
  ): Boolean = (a, b) match
    case (None, _) | (_, None) => true
    case (Some(x), Some(y))    =>
      x.toChain.toList.toSet.intersect(y.toChain.toList.toSet).nonEmpty

  given Show[ResourceSet] =
    Show.show(rs => s"ResourceSet(${rs.name.toNmToken.value}${rs.usage.fold("")(u => s", ${u.token.value}")})")

  given Eq[ResourceSet] = Eq.fromUniversalEquals

end ResourceSet

/** `Resource` (Table 6.1): one physical or logical entity in the partition
 *  context defined by its `Part` elements. The specific resource — the last
 *  XJDF-namespace element of the Resource — is optional (`specific: Option[ResourcePayload]`).
 *  A bodyless `<Resource/>` element represents an unelaborated resource.
 */
final case class Resource(
    specific: Option[ResourcePayload] = None,
    id: Option[Id] = None,
    externalId: Option[NmToken] = None,
    descriptiveName: Option[XjdfString] = None,
    brand: Option[XjdfString] = None,
    parts: Chain[Part] = Chain.empty,
    amountPool: Option[AmountPool] = None,
    placement: Option[OrientationSpec] = None,
    status: Option[ResourceStatus] = None,
    start: Option[Timestamp] = None,
    duration: Option[TimeSpan] = None,
    grossWeight: Option[Double] = None,
    resourceWeight: Option[Double] = None,
    comments: Chain[Comment] = Chain.empty,
    generalIds: Chain[GeneralID] = Chain.empty
):

  /** The local element name of the specific resource payload, if present.
   *  Bodyless resources have no specific element name (`None`).
   */
  def elementName: Option[NmToken] = specific.map(_.elementName)

  /** True when this resource is bodyless (`<Resource/>`, Table 6.1 / Example 3.6). */
  def isBodyless: Boolean = specific.isEmpty

  /** §6.1.3.3: a Resource without `Part` elements applies to the entire
   *  ResourceSet; a Resource with several parts applies to any of them.
   */
  def matches(selector: Part): Boolean =
    parts.isEmpty || parts.exists(_.matches(selector))

  /** All IDREFs used by this resource. */
  def references: Chain[IdRef] = specific.fold(Chain.empty[IdRef])(_.references)
end Resource

object Resource:

  /** A bodyless resource, e.g. `<Resource/>` (Table 6.1, Example 3.6). */
  val empty: Resource = Resource()

  /** A plain resource with just the specific payload. */
  def of(payload: ResourcePayload): Resource = Resource(Some(payload))

  /** A resource with the given specific payload. */
  def withPayload(payload: ResourcePayload): Resource = Resource(Some(payload))

  given Show[Resource] =
    Show.show(r => s"Resource(${r.elementName.fold("<bodyless>")(_.value)})")

  given Eq[Resource] = Eq.fromUniversalEquals

end Resource

/** `Resource/@Orientation | @Transformation` — at most one of the two SHALL be
 *  specified (Table 6.1). A union type: the value is either the named
 *  orientation or an explicit transformation matrix.
 */
type OrientationSpec = Orientation | Matrix
