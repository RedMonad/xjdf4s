package xjdf4s
package model

import xjdf4s.prim.*
import cats.Show
import cats.kernel.{Eq, Semigroup}

/**
 * The Partition Keys of `Resource/Part` (Table 6.4). A Partition Key is an
 * attribute of `Part` that can identify a specific Resource within its parent
 * ResourceSet.
 */
enum PartitionKey:
  case BinderySignatureID, BlockName, ContactType, DocIndex, DropID, Location, LotID,
       Metadata, OptionKey, PageNumber, PartVersion, PreviewType, PrintCondition, Product,
       ProductPart, QualityMeasurement, Run, RunIndex, Separation, SetIndex, SheetIndex,
       SheetName, Side, StationName, TileID, TransferCurveName, WebName

object PartitionKey:

  def values: List[PartitionKey] =
    List(BinderySignatureID, BlockName, ContactType, DocIndex, DropID, Location, LotID,
      Metadata, OptionKey, PageNumber, PartVersion, PreviewType, PrintCondition, Product,
      ProductPart, QualityMeasurement, Run, RunIndex, Separation, SetIndex, SheetIndex,
      SheetName, Side, StationName, TileID, TransferCurveName, WebName)

  given Show[PartitionKey] = Show.fromToString

  given Eq[PartitionKey] = Eq.fromUniversalEquals

end PartitionKey

/**
 * The runtime-tagged value of a Partition Key. Used when the key is not known
 * at compile time; for literal keys the match-type-typed `Part.get` returns the
 * exact value type instead.
 */
enum PartitionValue:
  case Token(value: NmToken)
  case Range(value: IntegerRange)
  case BySide(value: Side)
  case Tile(value: XYPair)
  case ByPreviewType(value: PreviewType)
  case ByTransferCurveTarget(value: TransferCurveTarget)
  case ProductRef(value: IdRef)

object PartitionValue:

  given Show[PartitionValue] =
    Show.show:
      case Token(v)               => v.value
      case Range(v)               => Show[IntegerRange].show(v)
      case BySide(v)              => v.token.value
      case Tile(v)                => Show[XYPair].show(v)
      case ByPreviewType(v)       => v.token.value
      case ByTransferCurveTarget(v) => v.token.value
      case ProductRef(v)          => v.value

  given Eq[PartitionValue] = Eq.fromUniversalEquals

end PartitionValue

/**
 * A *match type*: the value type of each partition key (Table 6.4). Keys that
 * select ranges of items carry an `IntegerRange`; `Side`, `PreviewType` and
 * `TransferCurveName` carry their closed enums; `TileID` carries an `XYPair` of
 * integers; `ProductPart` carries an `IdRef`; everything else is an open
 * `NmToken`.
 */
type ValueOf[K <: PartitionKey] = K match
  case PartitionKey.DocIndex.type | PartitionKey.PageNumber.type | PartitionKey.RunIndex.type
     | PartitionKey.SetIndex.type | PartitionKey.SheetIndex.type => IntegerRange
  case PartitionKey.Side.type              => Side
  case PartitionKey.TileID.type            => XYPair
  case PartitionKey.PreviewType.type       => PreviewType
  case PartitionKey.TransferCurveName.type => TransferCurveTarget
  case PartitionKey.ProductPart.type       => IdRef
  case _                                   => NmToken

/**
 * The `Part` element (Table 6.4): the partition context in which a Resource is
 * used. `Part.empty` applies to the entire ResourceSet.
 *
 * Parts form a `Semigroup` under *right-biased per-key overlay*: combining a
 * scheduling part with a more specific part refines each key that the right
 * side mentions and keeps everything else. The field-wise “last write wins”
 * rule is associative; conflicts (the same key present on both sides with
 * different values) are detected separately with `conflictingKeys`.
 */
final case class Part(
  binderySignatureId: Option[NmToken] = None,
  blockName: Option[NmToken] = None,
  contactType: Option[NmToken] = None,
  docIndex: Option[IntegerRange] = None,
  dropId: Option[NmToken] = None,
  location: Option[NmToken] = None,
  lotId: Option[NmToken] = None,
  metadata: Option[NmToken] = None,
  optionKey: Option[NmToken] = None,
  pageNumber: Option[IntegerRange] = None,
  partVersion: Option[NmToken] = None,
  previewType: Option[PreviewType] = None,
  printCondition: Option[NmToken] = None,
  product: Option[NmToken] = None,
  productPart: Option[IdRef] = None,
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

  /** Runtime-safe accessor for a key that is not a compile-time literal. */
  def valueOf(key: PartitionKey): Option[PartitionValue] =
    key match
      case PartitionKey.BinderySignatureID => binderySignatureId.map(PartitionValue.Token.apply)
      case PartitionKey.BlockName          => blockName.map(PartitionValue.Token.apply)
      case PartitionKey.ContactType        => contactType.map(PartitionValue.Token.apply)
      case PartitionKey.DocIndex           => docIndex.map(PartitionValue.Range.apply)
      case PartitionKey.DropID             => dropId.map(PartitionValue.Token.apply)
      case PartitionKey.Location           => location.map(PartitionValue.Token.apply)
      case PartitionKey.LotID              => lotId.map(PartitionValue.Token.apply)
      case PartitionKey.Metadata           => metadata.map(PartitionValue.Token.apply)
      case PartitionKey.OptionKey          => optionKey.map(PartitionValue.Token.apply)
      case PartitionKey.PageNumber         => pageNumber.map(PartitionValue.Range.apply)
      case PartitionKey.PartVersion        => partVersion.map(PartitionValue.Token.apply)
      case PartitionKey.PreviewType        => previewType.map(PartitionValue.ByPreviewType.apply)
      case PartitionKey.PrintCondition     => printCondition.map(PartitionValue.Token.apply)
      case PartitionKey.Product            => product.map(PartitionValue.Token.apply)
      case PartitionKey.ProductPart        => productPart.map(PartitionValue.ProductRef.apply)
      case PartitionKey.QualityMeasurement => qualityMeasurement.map(PartitionValue.Token.apply)
      case PartitionKey.Run                => run.map(PartitionValue.Token.apply)
      case PartitionKey.RunIndex           => runIndex.map(PartitionValue.Range.apply)
      case PartitionKey.Separation         => separation.map(PartitionValue.Token.apply)
      case PartitionKey.SetIndex           => setIndex.map(PartitionValue.Range.apply)
      case PartitionKey.SheetIndex         => sheetIndex.map(PartitionValue.Range.apply)
      case PartitionKey.SheetName          => sheetName.map(PartitionValue.Token.apply)
      case PartitionKey.Side               => side.map(PartitionValue.BySide.apply)
      case PartitionKey.StationName        => stationName.map(PartitionValue.Token.apply)
      case PartitionKey.TileID             => tileId.map(PartitionValue.Tile.apply)
      case PartitionKey.TransferCurveName  => transferCurveName.map(PartitionValue.ByTransferCurveTarget.apply)
      case PartitionKey.WebName            => webName.map(PartitionValue.Token.apply)

  /**
   * Type-safe accessor for literal keys. The return type is computed by the
   * `ValueOf` match type: `part.get(PartitionKey.DocIndex): Option[IntegerRange]`
   * and `part.get(PartitionKey.Separation): Option[NmToken]` are both checked at
   * compile time.
   */
  def get[K <: PartitionKey](key: K): Option[ValueOf[K]] =
    key match
      case PartitionKey.BinderySignatureID  => binderySignatureId
      case PartitionKey.BlockName           => blockName
      case PartitionKey.ContactType         => contactType
      case PartitionKey.DocIndex            => docIndex
      case PartitionKey.DropID              => dropId
      case PartitionKey.Location            => location
      case PartitionKey.LotID               => lotId
      case PartitionKey.Metadata            => metadata
      case PartitionKey.OptionKey           => optionKey
      case PartitionKey.PageNumber          => pageNumber
      case PartitionKey.PartVersion         => partVersion
      case PartitionKey.PreviewType         => previewType
      case PartitionKey.PrintCondition      => printCondition
      case PartitionKey.Product             => product
      case PartitionKey.ProductPart         => productPart
      case PartitionKey.QualityMeasurement  => qualityMeasurement
      case PartitionKey.Run                 => run
      case PartitionKey.RunIndex            => runIndex
      case PartitionKey.Separation          => separation
      case PartitionKey.SetIndex            => setIndex
      case PartitionKey.SheetIndex          => sheetIndex
      case PartitionKey.SheetName           => sheetName
      case PartitionKey.Side                => side
      case PartitionKey.StationName         => stationName
      case PartitionKey.TileID              => tileId
      case PartitionKey.TransferCurveName   => transferCurveName
      case PartitionKey.WebName             => webName

  /** Keys present on both sides with *different* values. */
  def conflictingKeys(other: Part): List[PartitionKey] =
    keys.filter: k =>
      (valueOf(k), other.valueOf(k)) match
        case (Some(a), Some(b)) => a != b
        case _                  => false

  /**
   * §6.1.3.2 Selecting a Partition: this Part *matches* a selector when it has
   * no attribute that mismatches the selector — i.e. every key present here is
   * either absent in the selector or equal to it.
   */
  def matches(selector: Part): Boolean =
    keys.forall: k =>
      selector.valueOf(k) match
        case None    => true
        case Some(v) => valueOf(k).contains(v)

  /**
   * Merges two parts. When no key conflicts, the merge is the overlay of both;
   * conflicting keys are reported on the Left.
   */
  def mergeWith(other: Part): Either[List[PartitionKey], Part] =
    val conflicts = conflictingKeys(other)
    if conflicts.nonEmpty then Left(conflicts)
    else Right(Part.combine(this, other))

object Part:

  val empty: Part = Part()

  /** Overlay: right-biased per key. */
  def combine(a: Part, b: Part): Part =
    a.copy(
      binderySignatureId = a.binderySignatureId.orElse(b.binderySignatureId),
      blockName          = a.blockName.orElse(b.blockName),
      contactType        = a.contactType.orElse(b.contactType),
      docIndex           = a.docIndex.orElse(b.docIndex),
      dropId             = a.dropId.orElse(b.dropId),
      location           = a.location.orElse(b.location),
      lotId              = a.lotId.orElse(b.lotId),
      metadata           = a.metadata.orElse(b.metadata),
      optionKey          = a.optionKey.orElse(b.optionKey),
      pageNumber         = a.pageNumber.orElse(b.pageNumber),
      partVersion        = a.partVersion.orElse(b.partVersion),
      previewType        = a.previewType.orElse(b.previewType),
      printCondition     = a.printCondition.orElse(b.printCondition),
      product            = a.product.orElse(b.product),
      productPart        = a.productPart.orElse(b.productPart),
      qualityMeasurement = a.qualityMeasurement.orElse(b.qualityMeasurement),
      run                = a.run.orElse(b.run),
      runIndex           = a.runIndex.orElse(b.runIndex),
      separation         = a.separation.orElse(b.separation),
      setIndex           = a.setIndex.orElse(b.setIndex),
      sheetIndex         = a.sheetIndex.orElse(b.sheetIndex),
      sheetName          = a.sheetName.orElse(b.sheetName),
      side               = a.side.orElse(b.side),
      stationName        = a.stationName.orElse(b.stationName),
      tileId             = a.tileId.orElse(b.tileId),
      transferCurveName  = a.transferCurveName.orElse(b.transferCurveName),
      webName            = a.webName.orElse(b.webName)
    )

  /** Builds a Part with exactly one key. */
  def of[K <: PartitionKey](key: K, value: ValueOf[K]): Part =
    PartBuilder.empty.withKey(key, value).build

  /** `@SheetName` partition — the classic sheet-partitioned resource. */
  def sheetName(name: String): Option[Part] =
    NmToken.from(name).map(t => of(PartitionKey.SheetName, t))

  /** `@Separation` partition — e.g. the Cyan plate. */
  def separation(name: String): Option[Part] =
    NmToken.from(name).map(t => of(PartitionKey.Separation, t))

  /** `@Side` partition — e.g. the Front side of a sheet. */
  def side(s: Side): Part = of(PartitionKey.Side, s)

  /** `@Run` partition — an individual RunList resource. */
  def run(name: String): Option[Part] =
    NmToken.from(name).map(t => of(PartitionKey.Run, t))

  given Semigroup[Part] with
    def combine(a: Part, b: Part): Part = Part.combine(a, b)

  given Show[Part] =
    Show.show { p =>
      if p.isEmpty then "Part(whole set)"
      else
        val entries = p.keys.map { k =>
          val rendered = p.valueOf(k).map(Show[PartitionValue].show).getOrElse("?")
          s"${k.toString}=$rendered"
        }
        s"Part(${entries.mkString(", ")})"
    }

  given Eq[Part] = Eq.fromUniversalEquals

end Part

/**
 * Incremental, type-safe constructor of `Part` values. Every `withKey` call is
 * checked against the `ValueOf` match type, so an incompatible key/value pair
 * (e.g. `PartitionKey.DocIndex` with an `NmToken`) does not compile.
 */
final case class PartBuilder private (part: Part):

  def withKey[K <: PartitionKey](key: K, value: ValueOf[K]): PartBuilder =
    PartBuilder(
      key match
        case PartitionKey.BinderySignatureID  => part.copy(binderySignatureId = Some(value))
        case PartitionKey.BlockName           => part.copy(blockName = Some(value))
        case PartitionKey.ContactType         => part.copy(contactType = Some(value))
        case PartitionKey.DocIndex            => part.copy(docIndex = Some(value))
        case PartitionKey.DropID              => part.copy(dropId = Some(value))
        case PartitionKey.Location            => part.copy(location = Some(value))
        case PartitionKey.LotID               => part.copy(lotId = Some(value))
        case PartitionKey.Metadata            => part.copy(metadata = Some(value))
        case PartitionKey.OptionKey           => part.copy(optionKey = Some(value))
        case PartitionKey.PageNumber          => part.copy(pageNumber = Some(value))
        case PartitionKey.PartVersion         => part.copy(partVersion = Some(value))
        case PartitionKey.PreviewType         => part.copy(previewType = Some(value))
        case PartitionKey.PrintCondition      => part.copy(printCondition = Some(value))
        case PartitionKey.Product             => part.copy(product = Some(value))
        case PartitionKey.ProductPart         => part.copy(productPart = Some(value))
        case PartitionKey.QualityMeasurement  => part.copy(qualityMeasurement = Some(value))
        case PartitionKey.Run                 => part.copy(run = Some(value))
        case PartitionKey.RunIndex            => part.copy(runIndex = Some(value))
        case PartitionKey.Separation          => part.copy(separation = Some(value))
        case PartitionKey.SetIndex            => part.copy(setIndex = Some(value))
        case PartitionKey.SheetIndex          => part.copy(sheetIndex = Some(value))
        case PartitionKey.SheetName           => part.copy(sheetName = Some(value))
        case PartitionKey.Side                => part.copy(side = Some(value))
        case PartitionKey.StationName         => part.copy(stationName = Some(value))
        case PartitionKey.TileID              => part.copy(tileId = Some(value))
        case PartitionKey.TransferCurveName   => part.copy(transferCurveName = Some(value))
        case PartitionKey.WebName             => part.copy(webName = Some(value))
    )

  def build: Part = part

object PartBuilder:
  val empty: PartBuilder = PartBuilder(Part.empty)
