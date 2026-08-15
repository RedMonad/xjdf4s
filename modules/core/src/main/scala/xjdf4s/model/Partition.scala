package xjdf4s
package model

import xjdf4s.prim.*
import cats.Show
import cats.kernel.{Eq, Semigroup}

/** The Partition Keys of `Resource/Part` (Table 6.4). A Partition Key is an
 *  attribute of `Part` that can identify a specific Resource within its parent
 *  ResourceSet.
 */
enum PartitionKey:
  case BinderySignatureID, BlockName, ContactType, DocIndex, DropID, Location, LotID,
    Metadata, OptionKey, PageNumber, PartVersion, PreviewType, PrintCondition, Product,
    ProductPart, QualityMeasurement, Run, RunIndex, Separation, SetIndex, SheetIndex,
    SheetName, Side, StationName, TileID, TransferCurveName, WebName

  /** The XJDF attribute name of this Partition Key (Table 6.4). `OptionKey` is
   *  the only key whose Scala name differs from its wire name (the collision
   *  with `scala.Option` forces the rename). `Show[Part]`, validator messages
   *  and the future M2 codecs must use this, not `toString` of the key.
   */
  def attributeName: String = this match
    case OptionKey => "Option"
    case other     => other.toString

object PartitionKey:

  /** All keys, in Table 6.4 order. (`all`, not `values`: enums synthesize `values`.) */
  def all: List[PartitionKey] =
    List(
      BinderySignatureID,
      BlockName,
      ContactType,
      DocIndex,
      DropID,
      Location,
      LotID,
      Metadata,
      OptionKey,
      PageNumber,
      PartVersion,
      PreviewType,
      PrintCondition,
      Product,
      ProductPart,
      QualityMeasurement,
      Run,
      RunIndex,
      Separation,
      SetIndex,
      SheetIndex,
      SheetName,
      Side,
      StationName,
      TileID,
      TransferCurveName,
      WebName
    )

  given Show[PartitionKey] = Show.fromToString

  given Eq[PartitionKey] = Eq.fromUniversalEquals

end PartitionKey

/** The runtime-tagged value of a Partition Key. Used when the key is not known
 *  at compile time; for compile-time-known keys the typed fields of `Part`
 *  (e.g. `part.docIndex: Option[IntegerRange]`, `part.side: Option[Side]`) and
 *  the typed constructors of the `Part` companion are the primary interface.
 */
enum PartitionValue:
  case Token(value: NmToken)
  case Range(value: IntegerRange)
  case BySide(value: Side)
  case Tile(value: XYPair)
  case ByPreviewType(value: PreviewType)
  case ByTransferCurveTarget(value: TransferCurveTarget)
  case ProductRef(value: NmToken)
  case RegExpValue(value: RegExp)

object PartitionValue:

  given Show[PartitionValue] =
    Show.show:
      case Token(v) => v.value
      case Range(v) => Show[IntegerRange].show(v)
      case BySide(v) => v.token.value
      case Tile(v) => Show[XYPair].show(v)
      case ByPreviewType(v) => v.token.value
      case ByTransferCurveTarget(v) => v.token.value
      case ProductRef(v) => v.value
      case RegExpValue(v) => v.value

  given Eq[PartitionValue] = Eq.fromUniversalEquals

end PartitionValue

/** The *type-level* reference mapping of Partition Keys to their value types
 *  (Table 6.4), as a match type:
 *
 *  - range keys (`DocIndex`, `PageNumber`, `RunIndex`, `SetIndex`, `SheetIndex`)
 *    carry `IntegerRange`;
 *  - `Side`, `PreviewType`, `TransferCurveName` carry their closed enums;
 *  - `Metadata` carries `RegExp`;
 *  - `TileID` carries an `XYPair` of integers;
 *  - `ProductPart` carries an open `NmToken` (Table 6.4: NMTOKEN, not an IDREF —
 *    it is outside the §2.2.3 ID/IDREF mechanism);
 *  - everything else is an open `NmToken`.
 *
 *  Exposed for type-level programming downstream; the value-level accessors of
 *  `Part` are the typed case-class fields and `PartitionValue` (the compiler
 *  does not refine abstract keys in match-type scrutinees, so a generic
 *  `get[K <: PartitionKey](key: K): Option[ValueOf[K]]` cannot be implemented
 *  without casts — see ROADMAP.md, "Риски и меры снижения").
 */
type ValueOf[K <: PartitionKey] = K match
  case PartitionKey.DocIndex.type | PartitionKey.PageNumber.type | PartitionKey.RunIndex.type
        | PartitionKey.SetIndex.type | PartitionKey.SheetIndex.type => IntegerRange
  case PartitionKey.Side.type => Side
  case PartitionKey.TileID.type => XYPair
  case PartitionKey.PreviewType.type => PreviewType
  case PartitionKey.TransferCurveName.type => TransferCurveTarget
  case PartitionKey.Metadata.type => RegExp
  case PartitionKey.ProductPart.type => NmToken
  case _ => NmToken

/** The `Part` element (Table 6.4): the partition context in which a Resource is
 *  used. `Part.empty` applies to the entire ResourceSet.
 *
 *  Parts form a `Semigroup` under *right-biased per-key overlay*: combining a
 *  scheduling part with a more specific part refines each key that the right
 *  side mentions and keeps everything else. The field-wise “last write wins”
 *  rule is associative; conflicts (the same key present on both sides with
 *  different values) are detected separately with `conflictingKeys`.
 */
final case class Part(
    binderySignatureId: Option[NmToken] = None,
    blockName: Option[NmToken] = None,
    contactType: Option[NmToken] = None,
    docIndex: Option[IntegerRange] = None,
    dropId: Option[NmToken] = None,
    location: Option[NmToken] = None,
    lotId: Option[NmToken] = None,
    metadata: Option[RegExp] = None,
    optionKey: Option[NmToken] = None,
    pageNumber: Option[IntegerRange] = None,
    partVersion: Option[NmToken] = None,
    previewType: Option[PreviewType] = None,
    printCondition: Option[NmToken] = None,
    product: Option[NmToken] = None,
    productPart: Option[NmToken] = None,
    qualityMeasurement: Option[NmToken] = None,
    run: Option[NmToken] = None,
    runIndex: Option[IntegerRange] = None,
    separation: Option[NmToken] = None,
    setIndex: Option[IntegerRange] = None,
    sheetIndex: Option[IntegerRange] = None,
    sheetName: Option[NmToken] = None,
    side: Option[Side] = None,
    stationName: Option[NmToken] = None,
    tileId: Option[XYPair] = None,
    transferCurveName: Option[TransferCurveTarget] = None,
    webName: Option[NmToken] = None
):

  /** True when no Partition Key is specified — the Part applies to the whole set. */
  def isEmpty: Boolean = keys.isEmpty

  /** The keys that are present in this Part. */
  def keys: List[PartitionKey] =
    List(
      Option.when(binderySignatureId.isDefined)(PartitionKey.BinderySignatureID),
      Option.when(blockName.isDefined)(PartitionKey.BlockName),
      Option.when(contactType.isDefined)(PartitionKey.ContactType),
      Option.when(docIndex.isDefined)(PartitionKey.DocIndex),
      Option.when(dropId.isDefined)(PartitionKey.DropID),
      Option.when(location.isDefined)(PartitionKey.Location),
      Option.when(lotId.isDefined)(PartitionKey.LotID),
      Option.when(metadata.isDefined)(PartitionKey.Metadata),
      Option.when(optionKey.isDefined)(PartitionKey.OptionKey),
      Option.when(pageNumber.isDefined)(PartitionKey.PageNumber),
      Option.when(partVersion.isDefined)(PartitionKey.PartVersion),
      Option.when(previewType.isDefined)(PartitionKey.PreviewType),
      Option.when(printCondition.isDefined)(PartitionKey.PrintCondition),
      Option.when(product.isDefined)(PartitionKey.Product),
      Option.when(productPart.isDefined)(PartitionKey.ProductPart),
      Option.when(qualityMeasurement.isDefined)(PartitionKey.QualityMeasurement),
      Option.when(run.isDefined)(PartitionKey.Run),
      Option.when(runIndex.isDefined)(PartitionKey.RunIndex),
      Option.when(separation.isDefined)(PartitionKey.Separation),
      Option.when(setIndex.isDefined)(PartitionKey.SetIndex),
      Option.when(sheetIndex.isDefined)(PartitionKey.SheetIndex),
      Option.when(sheetName.isDefined)(PartitionKey.SheetName),
      Option.when(side.isDefined)(PartitionKey.Side),
      Option.when(stationName.isDefined)(PartitionKey.StationName),
      Option.when(tileId.isDefined)(PartitionKey.TileID),
      Option.when(transferCurveName.isDefined)(PartitionKey.TransferCurveName),
      Option.when(webName.isDefined)(PartitionKey.WebName)
    ).flatten

  /** Runtime accessor for a key that is not a compile-time literal. */
  def valueOf(key: PartitionKey): Option[PartitionValue] =
    key match
      case PartitionKey.BinderySignatureID => binderySignatureId.map(PartitionValue.Token.apply)
      case PartitionKey.BlockName => blockName.map(PartitionValue.Token.apply)
      case PartitionKey.ContactType => contactType.map(PartitionValue.Token.apply)
      case PartitionKey.DocIndex => docIndex.map(PartitionValue.Range.apply)
      case PartitionKey.DropID => dropId.map(PartitionValue.Token.apply)
      case PartitionKey.Location => location.map(PartitionValue.Token.apply)
      case PartitionKey.LotID => lotId.map(PartitionValue.Token.apply)
      case PartitionKey.Metadata => metadata.map(PartitionValue.RegExpValue.apply)
      case PartitionKey.OptionKey => optionKey.map(PartitionValue.Token.apply)
      case PartitionKey.PageNumber => pageNumber.map(PartitionValue.Range.apply)
      case PartitionKey.PartVersion => partVersion.map(PartitionValue.Token.apply)
      case PartitionKey.PreviewType => previewType.map(PartitionValue.ByPreviewType.apply)
      case PartitionKey.PrintCondition => printCondition.map(PartitionValue.Token.apply)
      case PartitionKey.Product => product.map(PartitionValue.Token.apply)
      case PartitionKey.ProductPart => productPart.map(PartitionValue.ProductRef.apply)
      case PartitionKey.QualityMeasurement => qualityMeasurement.map(PartitionValue.Token.apply)
      case PartitionKey.Run => run.map(PartitionValue.Token.apply)
      case PartitionKey.RunIndex => runIndex.map(PartitionValue.Range.apply)
      case PartitionKey.Separation => separation.map(PartitionValue.Token.apply)
      case PartitionKey.SetIndex => setIndex.map(PartitionValue.Range.apply)
      case PartitionKey.SheetIndex => sheetIndex.map(PartitionValue.Range.apply)
      case PartitionKey.SheetName => sheetName.map(PartitionValue.Token.apply)
      case PartitionKey.Side => side.map(PartitionValue.BySide.apply)
      case PartitionKey.StationName => stationName.map(PartitionValue.Token.apply)
      case PartitionKey.TileID => tileId.map(PartitionValue.Tile.apply)
      case PartitionKey.TransferCurveName => transferCurveName.map(PartitionValue.ByTransferCurveTarget.apply)
      case PartitionKey.WebName => webName.map(PartitionValue.Token.apply)

  /** Keys present on both sides with *different* values. */
  def conflictingKeys(other: Part): List[PartitionKey] =
    keys.filter: k =>
      (valueOf(k), other.valueOf(k)) match
        case (Some(a), Some(b)) => a != b
        case _ => false

  /** §6.1.3.2 Selecting a Partition: this Part *matches* a selector when it has
   *  no attribute that mismatches the selector — i.e. every key present here is
   *  either absent in the selector or equal to it.
   */
  def matches(selector: Part): Boolean =
    keys.forall: k =>
      selector.valueOf(k) match
        case None => true
        case Some(v) => valueOf(k).contains(v)

  /** Merges two parts. When no key conflicts, the merge is the overlay of both;
   *  conflicting keys are reported on the Left.
   */
  def mergeWith(other: Part): Either[List[PartitionKey], Part] =
    val conflicts = conflictingKeys(other)
    if conflicts.nonEmpty then Left(conflicts)
    else Right(Part.combine(this, other))
end Part

object Part:

  val empty: Part = Part()

  /** Overlay: right-biased per key. */
  def combine(a: Part, b: Part): Part =
    a.copy(
      binderySignatureId = b.binderySignatureId.orElse(a.binderySignatureId),
      blockName = b.blockName.orElse(a.blockName),
      contactType = b.contactType.orElse(a.contactType),
      docIndex = b.docIndex.orElse(a.docIndex),
      dropId = b.dropId.orElse(a.dropId),
      location = b.location.orElse(a.location),
      lotId = b.lotId.orElse(a.lotId),
      metadata = b.metadata.orElse(a.metadata),
      optionKey = b.optionKey.orElse(a.optionKey),
      pageNumber = b.pageNumber.orElse(a.pageNumber),
      partVersion = b.partVersion.orElse(a.partVersion),
      previewType = b.previewType.orElse(a.previewType),
      printCondition = b.printCondition.orElse(a.printCondition),
      product = b.product.orElse(a.product),
      productPart = b.productPart.orElse(a.productPart),
      qualityMeasurement = b.qualityMeasurement.orElse(a.qualityMeasurement),
      run = b.run.orElse(a.run),
      runIndex = b.runIndex.orElse(a.runIndex),
      separation = b.separation.orElse(a.separation),
      setIndex = b.setIndex.orElse(a.setIndex),
      sheetIndex = b.sheetIndex.orElse(a.sheetIndex),
      sheetName = b.sheetName.orElse(a.sheetName),
      side = b.side.orElse(a.side),
      stationName = b.stationName.orElse(a.stationName),
      tileId = b.tileId.orElse(a.tileId),
      transferCurveName = b.transferCurveName.orElse(a.transferCurveName),
      webName = b.webName.orElse(a.webName)
    )

  /** Builds a Part with exactly one runtime-tagged key value. */
  def ofValue(key: PartitionKey, value: PartitionValue): Part =
    PartBuilder.empty.withValueUnsafe(key, value).build

  // ------------------------------------------------------------------
  // Typed constructors, one per value kind
  // ------------------------------------------------------------------

  /** Builds a Part with one NMTOKEN-valued key. */
  def token(key: PartitionKey, value: NmToken): Part =
    ofValue(key, PartitionValue.Token(value))

  /** Builds a Part with one IntegerRange-valued key. */
  def range(key: PartitionKey, value: IntegerRange): Part =
    ofValue(key, PartitionValue.Range(value))

  /** `@Side` partition — e.g. the Front side of a sheet. */
  def bySide(value: Side): Part =
    ofValue(PartitionKey.Side, PartitionValue.BySide(value))

  /** `@TileID` partition — a tile of a surface split by the Imposition process. */
  def byTile(value: XYPair): Part =
    ofValue(PartitionKey.TileID, PartitionValue.Tile(value))

  /** `@PreviewType` partition — e.g. `ThumbNail` or `Viewable` previews. */
  def byPreviewType(value: PreviewType): Part =
    ofValue(PartitionKey.PreviewType, PartitionValue.ByPreviewType(value))

  /** `@TransferCurveName` partition — the destination system of a TransferCurve. */
  def byTransferCurveTarget(value: TransferCurveTarget): Part =
    ofValue(PartitionKey.TransferCurveName, PartitionValue.ByTransferCurveTarget(value))

  /** `@ProductPart` partition — references the `Product/@ID` this Part applies to
   *  (Table 6.4: `NMTOKEN`; deprecated in XJDF 2.1. Not an IDREF: it stays
   *  outside the §2.2.3 ID/IDREF collection — a semantic reference to
   *  `Product/@ID` is checked by a separate rule, if at all).
   */
  def byProductPart(value: NmToken): Part =
    ofValue(PartitionKey.ProductPart, PartitionValue.ProductRef(value))

  // ------------------------------------------------------------------
  // Typed constructors, one per common key
  // ------------------------------------------------------------------

  /** `@SheetName` partition — the classic sheet-partitioned resource. */
  def sheetName(name: String): Option[Part] =
    NmToken.from(name).map(token(PartitionKey.SheetName, _))

  /** `@Separation` partition — e.g. the Cyan plate. */
  def separation(name: String): Option[Part] =
    NmToken.from(name).map(token(PartitionKey.Separation, _))

  /** `@Run` partition — an individual RunList resource. */
  def run(name: String): Option[Part] =
    NmToken.from(name).map(token(PartitionKey.Run, _))

  /** `@ContactType` partition — the role of a contact (Table 6.4, §5.1). */
  def contactType(name: String): Option[Part] =
    NmToken.from(name).map(token(PartitionKey.ContactType, _))

  /** `@DropID` partition — one drop within a Delivery (§5.3.2). */
  def dropId(name: String): Option[Part] =
    NmToken.from(name).map(token(PartitionKey.DropID, _))

  /** `@BlockName` partition — a CutBlock produced by Cutting (Table 6.4). */
  def blockName(name: String): Option[Part] =
    NmToken.from(name).map(token(PartitionKey.BlockName, _))

  /** `@WebName` partition — a web on a web press. */
  def webName(name: String): Option[Part] =
    NmToken.from(name).map(token(PartitionKey.WebName, _))

  /** `@DocIndex` partition — a selection of logical Instance Documents (§6.1.3). */
  def docIndex(r: IntegerRange): Part = range(PartitionKey.DocIndex, r)

  /** `@PageNumber` partition — a zero-based page selection. */
  def pageNumber(r: IntegerRange): Part = range(PartitionKey.PageNumber, r)

  /** `@RunIndex` partition — a selection of logical pages of a RunList. */
  def runIndex(r: IntegerRange): Part = range(PartitionKey.RunIndex, r)

  /** `@SetIndex` partition — a selection of Instance Document Sets. */
  def setIndex(r: IntegerRange): Part = range(PartitionKey.SetIndex, r)

  /** `@SheetIndex` partition — a selection of imposed sheets or surfaces. */
  def sheetIndex(r: IntegerRange): Part = range(PartitionKey.SheetIndex, r)

  given Semigroup[Part] with
    def combine(a: Part, b: Part): Part = Part.combine(a, b)

  given Show[Part] =
    Show.show { p =>
      if p.isEmpty then "Part(whole set)"
      else
        val entries = p.keys.map { k =>
          val rendered = p.valueOf(k).map(Show[PartitionValue].show).getOrElse("?")
          s"${k.attributeName}=$rendered"
        }
        s"Part(${entries.mkString(", ")})"
    }

  given Eq[Part] = Eq.fromUniversalEquals

end Part

/** Incremental constructor of `Part` values for runtime-tagged keys. The typed
 *  alternative is the per-key constructors of the `Part` companion.
 */
final case class PartBuilder private (part: Part):

  /** Safely sets (overrides) one runtime-tagged key value. A mismatched value
   *  kind is returned as an `Issue`; it is never silently discarded.
   */
  def withValue(key: PartitionKey, value: PartitionValue): Either[Issue, PartBuilder] =
    PartBuilder.set(part, key, value).map(PartBuilder.apply)

  /** Sets one runtime-tagged key value or throws `IllegalArgumentException`.
   *  Prefer `withValue`; this method is explicit for callers that deliberately
   *  elect an exception boundary.
   */
  def withValueUnsafe(key: PartitionKey, value: PartitionValue): PartBuilder =
    withValue(key, value).fold(issue => throw new IllegalArgumentException(issue.message), identity)

  /** Safely sets one NMTOKEN-valued key. */
  def withToken(key: PartitionKey, value: NmToken): Either[Issue, PartBuilder] =
    withValue(key, PartitionValue.Token(value))

  /** Unsafe counterpart of `withToken`. */
  def withTokenUnsafe(key: PartitionKey, value: NmToken): PartBuilder =
    withValueUnsafe(key, PartitionValue.Token(value))

  /** Safely sets one IntegerRange-valued key. */
  def withRange(key: PartitionKey, value: IntegerRange): Either[Issue, PartBuilder] =
    withValue(key, PartitionValue.Range(value))

  /** Unsafe counterpart of `withRange`. */
  def withRangeUnsafe(key: PartitionKey, value: IntegerRange): PartBuilder =
    withValueUnsafe(key, PartitionValue.Range(value))

  /** Sets `@Separation`; its value kind is fixed by the signature. */
  def withSeparation(separation: NmToken): PartBuilder =
    PartBuilder(part.copy(separation = Some(separation)))

  def build: Part = part
end PartBuilder
object PartBuilder:

  val empty: PartBuilder = PartBuilder(Part.empty)

  /** Total field assignment of one runtime-tagged PartitionValue onto a Part. */
  private def set(part: Part, key: PartitionKey, value: PartitionValue): Either[Issue, Part] =
    key match
      case PartitionKey.BinderySignatureID => expectToken(key, value).map(v => part.copy(binderySignatureId = Some(v)))
      case PartitionKey.BlockName => expectToken(key, value).map(v => part.copy(blockName = Some(v)))
      case PartitionKey.ContactType => expectToken(key, value).map(v => part.copy(contactType = Some(v)))
      case PartitionKey.DocIndex => expectRange(key, value).map(v => part.copy(docIndex = Some(v)))
      case PartitionKey.DropID => expectToken(key, value).map(v => part.copy(dropId = Some(v)))
      case PartitionKey.Location => expectToken(key, value).map(v => part.copy(location = Some(v)))
      case PartitionKey.LotID => expectToken(key, value).map(v => part.copy(lotId = Some(v)))
      case PartitionKey.Metadata => expectRegExp(key, value).map(v => part.copy(metadata = Some(v)))
      case PartitionKey.OptionKey => expectToken(key, value).map(v => part.copy(optionKey = Some(v)))
      case PartitionKey.PageNumber => expectRange(key, value).map(v => part.copy(pageNumber = Some(v)))
      case PartitionKey.PartVersion => expectToken(key, value).map(v => part.copy(partVersion = Some(v)))
      case PartitionKey.PreviewType => expectPreviewType(key, value).map(v => part.copy(previewType = Some(v)))
      case PartitionKey.PrintCondition => expectToken(key, value).map(v => part.copy(printCondition = Some(v)))
      case PartitionKey.Product => expectToken(key, value).map(v => part.copy(product = Some(v)))
      case PartitionKey.ProductPart => expectProductRef(key, value).map(v => part.copy(productPart = Some(v)))
      case PartitionKey.QualityMeasurement => expectToken(key, value).map(v => part.copy(qualityMeasurement = Some(v)))
      case PartitionKey.Run => expectToken(key, value).map(v => part.copy(run = Some(v)))
      case PartitionKey.RunIndex => expectRange(key, value).map(v => part.copy(runIndex = Some(v)))
      case PartitionKey.Separation => expectToken(key, value).map(v => part.copy(separation = Some(v)))
      case PartitionKey.SetIndex => expectRange(key, value).map(v => part.copy(setIndex = Some(v)))
      case PartitionKey.SheetIndex => expectRange(key, value).map(v => part.copy(sheetIndex = Some(v)))
      case PartitionKey.SheetName => expectToken(key, value).map(v => part.copy(sheetName = Some(v)))
      case PartitionKey.Side => expectSide(key, value).map(v => part.copy(side = Some(v)))
      case PartitionKey.StationName => expectToken(key, value).map(v => part.copy(stationName = Some(v)))
      case PartitionKey.TileID => expectTile(key, value).map(v => part.copy(tileId = Some(v)))
      case PartitionKey.TransferCurveName => expectTransferCurveTarget(key, value).map(v => part.copy(transferCurveName = Some(v)))
      case PartitionKey.WebName => expectToken(key, value).map(v => part.copy(webName = Some(v)))

  private def mismatch[A](key: PartitionKey, expected: String, value: PartitionValue): Either[Issue, A] =
    Left(Issue.error(
      XPath(s"/XJDF/ResourceSet/Resource/Part/@${key.attributeName}"),
      s"Expected $expected for @${key.attributeName}, got ${Show[PartitionValue].show(value)}"
    ))

  private def expectToken(key: PartitionKey, value: PartitionValue): Either[Issue, NmToken] = value match
    case PartitionValue.Token(token) => Right(token)
    case _ => mismatch(key, "an NMTOKEN partition value", value)

  private def expectRegExp(key: PartitionKey, value: PartitionValue): Either[Issue, RegExp] = value match
    case PartitionValue.RegExpValue(regexp) => Right(regexp)
    case _ => mismatch(key, "a Metadata partition value", value)

  private def expectRange(key: PartitionKey, value: PartitionValue): Either[Issue, IntegerRange] = value match
    case PartitionValue.Range(range) => Right(range)
    case _ => mismatch(key, "an IntegerRange partition value", value)

  private def expectSide(key: PartitionKey, value: PartitionValue): Either[Issue, Side] = value match
    case PartitionValue.BySide(side) => Right(side)
    case _ => mismatch(key, "a Side partition value", value)

  private def expectTile(key: PartitionKey, value: PartitionValue): Either[Issue, XYPair] = value match
    case PartitionValue.Tile(tile) => Right(tile)
    case _ => mismatch(key, "a Tile partition value", value)

  private def expectPreviewType(key: PartitionKey, value: PartitionValue): Either[Issue, PreviewType] = value match
    case PartitionValue.ByPreviewType(previewType) => Right(previewType)
    case _ => mismatch(key, "a PreviewType partition value", value)

  private def expectTransferCurveTarget(
      key: PartitionKey,
      value: PartitionValue
  ): Either[Issue, TransferCurveTarget] = value match
    case PartitionValue.ByTransferCurveTarget(target) => Right(target)
    case _ => mismatch(key, "a TransferCurveTarget partition value", value)

  private def expectProductRef(key: PartitionKey, value: PartitionValue): Either[Issue, NmToken] = value match
    case PartitionValue.ProductRef(reference) => Right(reference)
    case _ => mismatch(key, "a ProductPart partition value", value)
end PartBuilder
