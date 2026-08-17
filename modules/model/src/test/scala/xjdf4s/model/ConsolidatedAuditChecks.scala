package xjdf4s.model

import xjdf4s.core.*
import xjdf4s.model.resources.*

/**
 * Regression checks for the consolidated AUDIT.md findings: closed vocabularies, 2.2 fields, scalar/list corrections
 * and the compositional validation hooks.
 */
object ConsolidatedAuditChecks:
  val closedVocabularies: Unit =
    assert(Scope.Device.ordinal >= 0)
    assert(IsoPaperSubstrate.PS9.ordinal >= 0)
    assert(MediaType.Synthetic.ordinal >= 0)
    assert(Sides.Unprinted.ordinal >= 0)
    assert(JdfVersion.V1_0.lexical == "1.0")
    assert(JdfVersion.V1_8.lexical == "1.8")
    val expectedDataTypes = Set(
      GeneralId.DataType.Boolean,
      GeneralId.DataType.DateTime,
      GeneralId.DataType.Duration,
      GeneralId.DataType.Float,
      GeneralId.DataType.Integer,
      GeneralId.DataType.NamedFeature,
      GeneralId.DataType.Nmtoken,
      GeneralId.DataType.String,
    )
    assert(GeneralId.DataType.values.toSet == expectedDataTypes)

  val namedColorVocabulary: Unit =
    assert(NamedColor.from("AliceBlue").isRight)
    assert(NamedColor.from("aliceblue").isRight)
    assert(NamedColor.from("not-a-color").isLeft)

  val boundedColorProducts: Unit =
    assert(LabColor.from(50.0, 0.0, 0.0).isRight)
    assert(LabColor.from(101.0, 0.0, 0.0).isLeft)
    assert(CmykColor.from(0.0, 0.5, 1.0, 0.25).isRight)
    assert(CmykColor.from(0.0, 1.5, 0.0, 0.0).isLeft)
    assert(SrgbColor.from(0.0, 0.5, 1.0).isRight)
    assert(SrgbColor.from(1.1, 0.0, 0.0).isLeft)

  val evenLengthLists: Unit =
    assert(TransferFunction.from(Vector(0.0f, 1.0f)).isRight)
    assert(TransferFunction.from(Vector(0.0f, 1.0f, 0.5f)).isLeft)
    assert(TransferFunction.from(Vector.empty[Float]).isLeft)
    assert(GluingPattern.from(Vector(1.0f, 0.0f)).isRight)
    assert(GluingPattern.from(Vector(1.0f)).isLeft)
    assert(NeutralDensity.from(0.5f).isRight)
    assert(NeutralDensity.from(0.0001f).isLeft)
    assert(NeutralDensity.from(11.0f).isLeft)

  val foldCatalogPattern: Unit =
    assert(FoldCatalog.from("F6-4").isRight)
    assert(FoldCatalog.from("F12-X").isRight)
    assert(FoldCatalog.from("plain").isLeft)

  val partIntegerRanges: Unit =
    val part = Part(
      docIndex = Some(IntegerRange(0, 9)),
      sheetIndex = Some(IntegerRange(2, 2)),
    )
    assert(part.validate.isEmpty)
    assert(Part(docIndex = Some(IntegerRange(9, 0))).validate.nonEmpty)
    assert(Part(metadata = Some("not a (regex")).validate.nonEmpty)

  val resourceXorPlacementAndTiming: Unit =
    val bothPlacements = Resource(orientation = Some(Orientation.Rotate90), transformation = Some(Matrix.identity))
    val bothTimings = Resource(
      start = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption,
      startOffset = XsdDuration.from("PT1H").toOption,
    )
    assert(Resource().validate.isEmpty)
    assert(bothPlacements.validate.nonEmpty)
    assert(bothTimings.validate.nonEmpty)

  val mediaIntentBackIsoCompanion: Unit =
    val backOnly = MediaIntent(MediaType.Paper, backIsoPaperSubstrate = Some(IsoPaperSubstrate.PS5))
    assert(backOnly.validate.nonEmpty)
    assert(MediaIntent(MediaType.Paper).validate.isEmpty)

  val xjdfDocumentValidation: Unit =
    val jobId = Nmtoken.from("job-1").toOption.get
    val process = Nmtoken.from("Product").toOption.get
    val danglingPart = XJDF(jobId, NonEmptyVector.one(process), relatedJobPartId = Some(jobId))
    assert(danglingPart.validate.nonEmpty)
    val duplicateId = XsdId.from("dup").toOption.get
    val document = XJDF(
      jobId,
      NonEmptyVector.one(process),
      resourceSets = Vector(
        ResourceSet(Nmtoken.from("Media").toOption.get, id = Some(duplicateId)),
        ResourceSet(Nmtoken.from("Color").toOption.get, id = Some(duplicateId)),
      ),
    )
    assert(document.validate.nonEmpty)
    assert(XJDF(jobId, NonEmptyVector.one(process)).validate.isEmpty)

  val toolAndPatch22Fields: Unit =
    val tool = Tool(manufacturer = XjdfString.from("Acme").toOption, serialNumber = XjdfString.from("SN-1").toOption)
    assert(tool.manufacturer.nonEmpty)
    val patch = Patch(PatchUsage.Color, spotType = Some(SpotType.Emulated))
    assert(patch.spotType.contains(SpotType.Emulated))

  val priorityCarriers: Unit =
    val disposition = Disposition(priority = Priority0To100.from(99).toOption)
    val nodeInfo = NodeInfo(jobPriority = Priority0To100.from(1).toOption)
    val gang = GangElement(Nmtoken.from("gang-1").toOption.get, 10, priority = Priority0To100.from(50).toOption)
    assert(disposition.priority.nonEmpty)
    assert(nodeInfo.jobPriority.nonEmpty)
    assert(gang.priority.nonEmpty)

  val conditionPartContextIsList: Unit =
    val docIndex = Nmtoken.from("DocIndex").toOption.get
    val setIndex = Nmtoken.from("SetIndex").toOption.get
    val condition = Condition(NonEmptyVector.one(Part()), partContext = Vector(docIndex, setIndex))
    assert(condition.partContext.size == 2)

  val integerProcessIndices: Unit =
    val set = ResourceSet(Nmtoken.from("Media").toOption.get, combinedProcessIndex = Vector(0, 1))
    assert(set.combinedProcessIndex.forall(_ >= 0))
end ConsolidatedAuditChecks
