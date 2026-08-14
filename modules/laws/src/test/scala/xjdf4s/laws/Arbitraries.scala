package xjdf4s.laws

import xjdf4s.model.*
import xjdf4s.prim.*
import cats.data.{Chain, NonEmptyChain}
import org.scalacheck.{Arbitrary, Gen}

/** ScalaCheck generators for the domain types. */
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

  implicit val arbPart: Arbitrary[Part] =
    Arbitrary:
      for
        sheet <- Gen.option(arbNmToken.arbitrary)
        sep   <- Gen.option(arbNmToken.arbitrary)
        run   <- Gen.option(arbNmToken.arbitrary)
        side  <- Gen.option(Gen.oneOf(Side.Front, Side.Back))
        doc   <- Gen.option(Gen.choose(-10L, 10L).map(IntegerRange.single))
      yield Part(sheetName = sheet, separation = sep, run = run, side = side, docIndex = doc)

  implicit val arbAmountRange: Arbitrary[AmountRange] =
    Arbitrary:
      for
        amount <- Gen.option(arbAmount.arbitrary)
        max    <- Gen.option(arbAmount.arbitrary)
        min    <- Gen.option(arbAmount.arbitrary)
      yield AmountRange(amount, max, min)

  implicit val arbPartAmount: Arbitrary[PartAmount] =
    Arbitrary:
      for
        amount <- Gen.option(arbAmount.arbitrary)
        waste  <- Gen.option(arbAmount.arbitrary)
        part   <- arbPart.arbitrary
      yield PartAmount(amount = amount, waste = waste, part = part)

  implicit val arbAmountPool: Arbitrary[AmountPool] =
    Arbitrary:
      for
        head <- arbPartAmount.arbitrary
        tail <- Gen.listOf(arbPartAmount.arbitrary)
      yield AmountPool.of(head, tail*)

  implicit val arbUsage: Arbitrary[Usage] =
    Arbitrary(Gen.oneOf(Usage.Input, Usage.Output))

  implicit val arbMediaType: Arbitrary[MediaType] =
    Arbitrary(Gen.oneOf(MediaType.values))

  implicit val arbBindingType: Arbitrary[BindingType] =
    Arbitrary(Gen.oneOf(BindingType.values))

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
