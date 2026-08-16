package xjdf4s.laws

import xjdf4s.laws.Arbitraries.given
import xjdf4s.model.*
import xjdf4s.prim.*
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

/** The selection semantics of §6.1.3.2 “Selecting a Partition”, and the
 *  overlay/merge algebra of `Part`, as properties.
 */
class PartitionLaws extends ScalaCheckSuite:

  property("Part.empty applies to the whole ResourceSet — matches everything"):
    forAll { (selector: Part) => Part.empty.matches(selector) }

  property("a Part matches itself"):
    forAll { (p: Part) => p.matches(p) }

  property("a Part with a superset of keys matches the same selector"):
    forAll { (p: Part, extra: Part) =>
      if p.conflictingKeys(extra).isEmpty then Part.combine(p, extra).matches(p)
      else true
    }

  property("right-biased overlay always matches the right side"):
    forAll { (left: Part, right: Part) =>
      Part.combine(left, right).matches(right)
    }

  property("conflict-free merge matches both sides"):
    forAll { (a: Part, b: Part) =>
      a.mergeWith(b) match
        case Left(conflicts) => conflicts == a.conflictingKeys(b)
        case Right(merged) => merged.matches(a) && merged.matches(b)
    }

  property("merging a Part with itself never conflicts"):
    forAll { (p: Part) => p.mergeWith(p).isRight }

  property("runtime-tagged accessor round-trips NMTOKEN values"):
    forAll { (name: NmToken) =>
      Part.token(PartitionKey.SheetName, name).valueOf(PartitionKey.SheetName).contains(PartitionValue.Token(name))
    }

  property("runtime-tagged accessor round-trips range values"):
    forAll { (r: IntegerRange) =>
      Part.range(PartitionKey.DocIndex, r).valueOf(PartitionKey.DocIndex).contains(PartitionValue.Range(r))
    }

  property("typed constructor bySide carries the Side enum"):
    Part.bySide(Side.Front).valueOf(PartitionKey.Side).contains(PartitionValue.BySide(Side.Front))

  test("PartBuilder.withValue returns Left for a mismatched runtime value kind"):
    val result = PartBuilder.empty.withValue(PartitionKey.DocIndex, PartitionValue.Token(NmToken.unsafe("not-a-range")))
    assert(result.isLeft)

  test("PartBuilder.withValueUnsafe throws for the same mismatched value kind"):
    intercept[IllegalArgumentException](
      PartBuilder.empty.withValueUnsafe(PartitionKey.DocIndex, PartitionValue.Token(NmToken.unsafe("not-a-range")))
    )

  test("PartBuilder.withSeparation remains a typed non-throwing path"):
    val builder = PartBuilder.empty.withSeparation(NmToken.unsafe("Cyan"))
    assertEquals(builder.build.separation, Some(NmToken.unsafe("Cyan")))

  // --- M1.2-1: per-key law families (Table 6.4) ---------------------------

  property("Part.keys ↔ valueOf are consistent"):
    forAll { (p: Part) =>
      PartitionKey.values.forall(k => p.keys.contains(k) == p.valueOf(k).isDefined)
    }

  property("Part.combine is right-biased per key"):
    forAll { (a: Part, b: Part) =>
      PartitionKey.values.forall(k =>
        Part.combine(a, b).valueOf(k) == b.valueOf(k).orElse(a.valueOf(k))
      )
    }

  property("matches(b) == conflictingKeys(b).isEmpty"):
    forAll { (a: Part, b: Part) => a.matches(b) == a.conflictingKeys(b).isEmpty }

  // --- ADR-0005: tolerance relation and merge-derived partial order ----------

  /** Partial order derived from conflict-free merge (ADR-0005): `a ≤ b` iff the
   *  merge succeeds and yields `b` — i.e. `b` extends `a` as a partial map of
   *  Partition Keys. A genuine partial order; `matches` itself is not one.
   */
  private def le(a: Part, b: Part): Boolean =
    a.mergeWith(b).isRight && Part.combine(a, b) == b

  property("Part.matches is symmetric"):
    forAll { (a: Part, b: Part) => a.matches(b) == b.matches(a) }

  test("Part.matches is a tolerance relation (reflexive, symmetric, non-transitive)"):
    val a = Part.bySide(Side.Front)
    val b = Part.empty
    val c = Part.bySide(Side.Back)
    // a.matches(b) = true, b.matches(c) = true, but a.matches(c) = false
    assert(a.matches(b) && b.matches(c) && !a.matches(c))

  property("merge-derived order is reflexive"):
    forAll { (a: Part) => le(a, a) }

  property("merge-derived order is antisymmetric"):
    forAll { (a: Part, b: Part) => !(le(a, b) && le(b, a)) || a == b }

  property("merge-derived order is transitive"):
    forAll { (a: Part, b: Part, c: Part) => !(le(a, b) && le(b, c)) || le(a, c) }

  test("regression: overlay is right-biased (X-03 archive)"):
    val l = Part(docIndex = Some(IntegerRange(3, 3)))
    val r = Part(docIndex = Some(IntegerRange(-10, -10)))
    assertEquals(Part.combine(l, r).docIndex, r.docIndex)

  test("Table 6.4: PartitionKey wire names match attributeName"):
    val wireNames: Map[PartitionKey, String] = Map(
      PartitionKey.BinderySignatureID -> "BinderySignatureID",
      PartitionKey.BlockName -> "BlockName",
      PartitionKey.ContactType -> "ContactType",
      PartitionKey.DocIndex -> "DocIndex",
      PartitionKey.DropID -> "DropID",
      PartitionKey.Location -> "Location",
      PartitionKey.LotID -> "LotID",
      PartitionKey.Metadata -> "Metadata",
      PartitionKey.OptionKey -> "Option",
      PartitionKey.PageNumber -> "PageNumber",
      PartitionKey.PartVersion -> "PartVersion",
      PartitionKey.PreviewType -> "PreviewType",
      PartitionKey.PrintCondition -> "PrintCondition",
      PartitionKey.Product -> "Product",
      PartitionKey.ProductPart -> "ProductPart",
      PartitionKey.QualityMeasurement -> "QualityMeasurement",
      PartitionKey.Run -> "Run",
      PartitionKey.RunIndex -> "RunIndex",
      PartitionKey.Separation -> "Separation",
      PartitionKey.SetIndex -> "SetIndex",
      PartitionKey.SheetIndex -> "SheetIndex",
      PartitionKey.SheetName -> "SheetName",
      PartitionKey.Side -> "Side",
      PartitionKey.StationName -> "StationName",
      PartitionKey.TileID -> "TileID",
      PartitionKey.TransferCurveName -> "TransferCurveName",
      PartitionKey.WebName -> "WebName"
    )
    // The golden map must cover *all* enum cases: a newly added Partition Key
    // without a wire-name entry breaks this test (requirement M1.2-1).
    assertEquals(PartitionKey.values.toSet, wireNames.keySet)
    PartitionKey.values.foreach(k => assertEquals(k.attributeName, wireNames(k)))

  test("Table 6.4: runtime tag of each PartitionValue kind"):
    val p = Part(
      binderySignatureId = Some(NmToken.unsafe("b1")),
      blockName = Some(NmToken.unsafe("b2")),
      contactType = Some(NmToken.unsafe("c1")),
      docIndex = Some(IntegerRange(1, 2)),
      dropId = Some(NmToken.unsafe("d1")),
      location = Some(NmToken.unsafe("l1")),
      lotId = Some(NmToken.unsafe("lot1")),
      metadata = Some(RegExp.unsafe("Meta.*")),
      optionKey = Some(NmToken.unsafe("o1")),
      pageNumber = Some(IntegerRange(3, 4)),
      partVersion = Some(NmToken.unsafe("v1")),
      previewType = Some(PreviewType.ThumbNail),
      printCondition = Some(NmToken.unsafe("p1")),
      product = Some(NmToken.unsafe("prod1")),
      productPart = Some(NmToken.unsafe("pp1")),
      qualityMeasurement = Some(NmToken.unsafe("q1")),
      run = Some(NmToken.unsafe("r1")),
      runIndex = Some(IntegerRange(5, 6)),
      separation = Some(NmToken.unsafe("sep1")),
      setIndex = Some(IntegerRange(7, 8)),
      sheetIndex = Some(IntegerRange(9, 10)),
      sheetName = Some(NmToken.unsafe("s1")),
      side = Some(Side.Front),
      stationName = Some(NmToken.unsafe("st1")),
      tileId = Some(XYPair(1.0, 2.0)),
      transferCurveName = Some(TransferCurveTarget.Plate),
      webName = Some(NmToken.unsafe("w1"))
    )
    def tagOf(key: PartitionKey): String =
      p.valueOf(key).map(_.productPrefix).getOrElse(fail(s"Missing partition value for $key"))
    val rangeKeys = List(
      PartitionKey.DocIndex,
      PartitionKey.PageNumber,
      PartitionKey.RunIndex,
      PartitionKey.SetIndex,
      PartitionKey.SheetIndex
    )
    rangeKeys.foreach(k => assertEquals(tagOf(k), "Range"))
    assertEquals(tagOf(PartitionKey.Metadata), "RegExpValue")
    assertEquals(tagOf(PartitionKey.ProductPart), "ProductRef")
    assertEquals(tagOf(PartitionKey.Side), "BySide")
    assertEquals(tagOf(PartitionKey.TileID), "Tile")
    assertEquals(tagOf(PartitionKey.PreviewType), "ByPreviewType")
    assertEquals(tagOf(PartitionKey.TransferCurveName), "ByTransferCurveTarget")
    val nonTokenKeys =
      (rangeKeys ++ List(
        PartitionKey.Metadata,
        PartitionKey.ProductPart,
        PartitionKey.Side,
        PartitionKey.TileID,
        PartitionKey.PreviewType,
        PartitionKey.TransferCurveName
      )).toSet
    PartitionKey.values.filterNot(nonTokenKeys).foreach(k => assertEquals(tagOf(k), "Token"))

  property("the ValueOf match type agrees with the typed fields at the type level"):
    // `sheetName` is Option[NmToken] == Option[ValueOf[PartitionKey.SheetName.type]]:
    // the match type is used here as a *type-level* witness, since GADT
    // refinement of abstract keys is not available (see Partition.scala).
    val sheet: Option[ValueOf[PartitionKey.SheetName.type]] = Part.sheetName("S1").flatMap(_.sheetName)
    sheet.contains(NmToken.unsafe("S1"))

  property("selection: a Resource without parts applies to the entire set"):
    forAll { (selector: Part) =>
      val payload = xjdf4s.resources.ResourcePayload.Foreign(NsPrefix.unsafe("foo"), NmToken.unsafe("Bar"))
      val resource = Resource(specific = Some(payload))
      resource.matches(selector)
    }

  property("selection: §6.1.3.2 iterates top to bottom and takes the first match"):
    val s1 = Part.sheetName("S1").get
    val s2 = Part.sheetName("S2").get
    val payload = xjdf4s.resources.ResourcePayload.Foreign(NsPrefix.unsafe("foo"), NmToken.unsafe("Bar"))
    val r1 = Resource(specific = Some(payload), parts = cats.data.Chain.one(s1))
    val r2 = Resource(specific = Some(payload), parts = cats.data.Chain.one(s2))
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("foo:Bar"),
      resources = cats.data.Chain(r1, r2)
    )
    rs.select(Part.sheetName("S2").get) == Some(r2) &&
    rs.select(Part.empty) == Some(r1)

  property("selection: IDREF selection ignores Part elements"):
    val id = Id.unsafe("r_1")
    val payload = xjdf4s.resources.ResourcePayload.Foreign(NsPrefix.unsafe("foo"), NmToken.unsafe("Bar"))
    val r = Resource(specific = Some(payload), id = Some(id), parts = cats.data.Chain.one(Part.sheetName("S9").get))
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("foo:Bar"),
      resources = cats.data.Chain.one(r)
    )
    rs.byId(id) == Some(r) &&
    rs.select(Part.sheetName("S9").get) == Some(r) &&
    rs.select(Part.sheetName("other").get) == None
end PartitionLaws
