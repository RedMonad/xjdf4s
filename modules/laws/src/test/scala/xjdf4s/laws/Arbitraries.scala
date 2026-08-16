package xjdf4s.laws

import xjdf4s.model.*
import xjdf4s.model.elements.Comment
import xjdf4s.prim.*
import cats.data.{Chain, NonEmptyChain}
import org.scalacheck.{Arbitrary, Gen}

/** ScalaCheck generators for the domain types.
 *
 *  ROADMAP §12.2: lawful and *intentionally invalid* generators are kept apart.
 *  Everything in this object is lawful by construction and drives the algebraic
 *  laws; deliberately law-breaking values live in `Arbitraries.Invalid` and
 *  drive the negative properties (the validator MUST reject them).
 */
object Arbitraries:

  implicit val arbNmToken: Arbitrary[NmToken] =
    Arbitrary(Gen.alphaNumStr.suchThat(_.nonEmpty).map(NmToken.unsafe))

  implicit val arbId: Arbitrary[Id] =
    Arbitrary(arbNmToken.arbitrary.map(t => Id.unsafe(s"id_${t.value}")))

  implicit val arbIdRef: Arbitrary[IdRef] =
    Arbitrary(arbNmToken.arbitrary.map(t => IdRef.unsafe(t.value)))

  implicit val arbAmount: Arbitrary[Amount] =
    Arbitrary(Gen.choose(0.0, 10000.0).map(Amount.apply))

  implicit val arbTimestamp: Arbitrary[Timestamp] =
    Arbitrary(Gen.choose(0L, 1000000L).map(Timestamp.ofEpochSecond))

  implicit val arbTimeSpan: Arbitrary[TimeSpan] =
    Arbitrary(Gen.choose(0L, 100000L).map(TimeSpan.ofSeconds))

  implicit val arbIntegerRange: Arbitrary[IntegerRange] =
    Arbitrary:
      for
        from <- Gen.choose(-10L, 10L)
        to   <- Gen.choose(-10L, 10L)
      yield IntegerRange(from, to)

  implicit val arbMatrix: Arbitrary[Matrix] =
    Arbitrary:
      for
        a  <- Gen.choose(-10.0, 10.0)
        b  <- Gen.choose(-10.0, 10.0)
        c  <- Gen.choose(-10.0, 10.0)
        d  <- Gen.choose(-10.0, 10.0)
        tx <- Gen.choose(-100.0, 100.0)
        ty <- Gen.choose(-100.0, 100.0)
      yield Matrix(a, b, c, d, tx, ty)

  implicit val arbXYPair: Arbitrary[XYPair] =
    Arbitrary:
      for
        x <- Gen.choose(-1000.0, 1000.0)
        y <- Gen.choose(-1000.0, 1000.0)
      yield XYPair(x, y)

  implicit val arbPoints: Arbitrary[Points] =
    Arbitrary(Gen.choose(-10000.0, 10000.0).map(Points.apply))

  implicit val arbCoverage: Arbitrary[Coverage] =
    Arbitrary(Gen.choose(0.0, 100.0).map(Coverage.unsafe))

  implicit val arbUnitInterval: Arbitrary[UnitInterval] =
    Arbitrary(Gen.choose(0.0, 1.0).map(UnitInterval.unsafe))

  implicit val arbRegExp: Arbitrary[RegExp] =
    Arbitrary(Gen.alphaNumStr.suchThat(_.nonEmpty).map(RegExp.unsafe))

  /** `Part` generator covering all 27 Partition Keys of Table 6.4 (N-29).
   *  Every key is drawn independently, so each law is exercised with every
   *  combination of keys, including the empty Part.
   */
  implicit val arbPart: Arbitrary[Part] =
    Arbitrary:
      for
        binderySignatureId <- Gen.option(arbNmToken.arbitrary)
        blockName          <- Gen.option(arbNmToken.arbitrary)
        contactType        <- Gen.option(arbNmToken.arbitrary)
        docIndex           <- Gen.option(arbIntegerRange.arbitrary)
        dropId             <- Gen.option(arbNmToken.arbitrary)
        location           <- Gen.option(arbNmToken.arbitrary)
        lotId              <- Gen.option(arbNmToken.arbitrary)
        metadata           <- Gen.option(arbRegExp.arbitrary)
        optionKey          <- Gen.option(arbNmToken.arbitrary)
        pageNumber         <- Gen.option(arbIntegerRange.arbitrary)
        partVersion        <- Gen.option(arbNmToken.arbitrary)
        previewType        <- Gen.option(Gen.oneOf(PreviewType.all))
        printCondition     <- Gen.option(arbNmToken.arbitrary)
        product            <- Gen.option(arbNmToken.arbitrary)
        productPart        <- Gen.option(arbNmToken.arbitrary)
        qualityMeasurement <- Gen.option(arbNmToken.arbitrary)
        run                <- Gen.option(arbNmToken.arbitrary)
        runIndex           <- Gen.option(arbIntegerRange.arbitrary)
        separation         <- Gen.option(arbNmToken.arbitrary)
        setIndex           <- Gen.option(arbIntegerRange.arbitrary)
        sheetIndex         <- Gen.option(arbIntegerRange.arbitrary)
        sheetName          <- Gen.option(arbNmToken.arbitrary)
        side               <- Gen.option(Gen.oneOf(Side.all))
        stationName        <- Gen.option(arbNmToken.arbitrary)
        tileId             <- Gen.option(arbXYPair.arbitrary)
        transferCurveName  <- Gen.option(Gen.oneOf(TransferCurveTarget.all))
        webName            <- Gen.option(arbNmToken.arbitrary)
      yield Part(
        binderySignatureId = binderySignatureId,
        blockName = blockName,
        contactType = contactType,
        docIndex = docIndex,
        dropId = dropId,
        location = location,
        lotId = lotId,
        metadata = metadata,
        optionKey = optionKey,
        pageNumber = pageNumber,
        partVersion = partVersion,
        previewType = previewType,
        printCondition = printCondition,
        product = product,
        productPart = productPart,
        qualityMeasurement = qualityMeasurement,
        run = run,
        runIndex = runIndex,
        separation = separation,
        setIndex = setIndex,
        sheetIndex = sheetIndex,
        sheetName = sheetName,
        side = side,
        stationName = stationName,
        tileId = tileId,
        transferCurveName = transferCurveName,
        webName = webName
      )

  implicit val arbAmountBounds: Arbitrary[AmountBounds] =
    Arbitrary:
      for
        min <- Gen.option(arbAmount.arbitrary)
        max <- Gen.option(arbAmount.arbitrary)
      yield (min, max) match
        case (Some(lower), Some(upper)) if lower.value > upper.value => AmountBounds(Some(upper), Some(lower))
        case _ => AmountBounds(min, max)

  implicit val arbPartAmount: Arbitrary[PartAmount] =
    Arbitrary:
      for
        amount <- Gen.option(arbAmount.arbitrary)
        waste  <- Gen.option(arbAmount.arbitrary)
        // Bound the `Part*` cardinality: an unbounded `Gen.listOf` here (default
        // size ~100) is squared again by `arbAmountPool`'s own `Gen.listOf`,
        // exploding the AmountPool semigroup law in AlgebraLaws. 0..3 still
        // exercises the empty, single and multiple Part cases (N-10).
        n     <- Gen.choose(0, 3)
        parts <- Gen.listOfN(n, arbPart.arbitrary)
      yield PartAmount(amount = amount, waste = waste, parts = Chain.fromSeq(parts))

  implicit val arbAmountPool: Arbitrary[AmountPool] =
    Arbitrary:
      for
        head <- arbPartAmount.arbitrary
        tail <- Gen.listOf(arbPartAmount.arbitrary)
      yield AmountPool.of(head, tail*)

  implicit val arbUsage: Arbitrary[Usage] =
    Arbitrary(Gen.oneOf(Usage.Input, Usage.Output))

  implicit val arbMediaType: Arbitrary[MediaType] =
    Arbitrary(Gen.oneOf(MediaType.all))

  implicit val arbBindingType: Arbitrary[BindingType] =
    Arbitrary(Gen.oneOf(BindingType.all))

  implicit val arbPatch: Arbitrary[Patch] =
    Arbitrary:
      Gen.oneOf(
        Patch.identity,
        Patch.withProductList(ProductList(NonEmptyChain.one(Product(amount = Some(1))))),
        Patch.addResourceSet(ResourceSet(ResourceSetName.unsafe("Media"), usage = Some(Usage.Input))),
        Patch.addComment(Comment("law-test"))
      )

  implicit val arbAudit: Arbitrary[Audit] =
    Arbitrary:
      for
        t   <- arbTimestamp.arbitrary
        dev <- arbNmToken.arbitrary
      yield Audit.Created(Header(dev, t))

  implicit val arbAuditPool: Arbitrary[AuditPool] =
    Arbitrary:
      for
        head <- arbAudit.arbitrary
        tail <- Gen.listOf(arbAudit.arbitrary)
      yield AuditPool.of(head, tail*)

  implicit val arbProcessType: Arbitrary[ProcessType] =
    Arbitrary(Gen.oneOf(ProcessType.Cutting, ProcessType.Folding, ProcessType.DigitalPrinting))

  implicit val arbResourceSet: Arbitrary[ResourceSet] =
    Arbitrary:
      for
        name  <- Gen.oneOf("Media", "Component", "NodeInfo")
        usage <- Gen.option(arbUsage.arbitrary)
      yield ResourceSet(ResourceSetName.unsafe(name), usage = usage)

  implicit val arbTicket: Arbitrary[XJDF] =
    Arbitrary:
      for
        job   <- arbNmToken.arbitrary
        types <- Gen.nonEmptyListOf(arbProcessType.arbitrary)
        sets  <- Gen.listOf(arbResourceSet.arbitrary)
      yield XJDF(
        jobId = JobId.unsafe(job.value),
        types = NonEmptyChain.fromChainUnsafe(Chain.fromSeq(types)),
        resourceSets = Chain.fromSeq(sets)
      )

  /** Intentionally law-breaking generators (ROADMAP §12.2). None of these
   *  values is valid by construction; negative properties assert that the
   *  model or the validator REJECTS them. Kept separate from the lawful
   *  generators above so a defect in the model cannot hide behind a generator
   *  that never reaches the boundary.
   */
  object Invalid:

    /** A ticket with two `ResourceSet`s that clash under the §3.4 predicate
     *  (identical `@Name`/`@Usage`/`@ProcessUsage`, overlapping Combined
     *  Process Index) — the validator must report `RESOURCESET-CLASH`.
     */
    val arbDuplicateResourceSets: Arbitrary[XJDF] =
      Arbitrary:
        for
          name  <- Gen.oneOf("Media", "Component", "NodeInfo")
          usage <- Gen.option(Gen.oneOf(Usage.Input, Usage.Output))
          types <- Gen.oneOf(
            NonEmptyChain.one(ProcessType.Product),
            NonEmptyChain(ProcessType.Cutting, ProcessType.Folding)
          )
          rs = ResourceSet(ResourceSetName.unsafe(name), usage = usage)
        yield XJDF(
          jobId = JobId.unsafe("invalid-duplicate-keys"),
          types = types,
          resourceSets = Chain(rs, rs)
        )
  end Invalid

end Arbitraries
