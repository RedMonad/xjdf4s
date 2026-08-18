package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

final case class BlockPreparationParams(
    backing: Option[Float] = None,
    rounding: Option[Float] = None,
    tightBacking: Option[TightBacking] = None,
    registerRibbons: Vector[RegisterRibbon] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("BlockPreparationParams")

final case class EndSheetGluingParams(
    glue: Glue,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("EndSheetGluingParams")

final case class GluingParams(
    gluingProductionId: Option[XjdfString] = None,
    glues: Vector[Glue] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("GluingParams")

enum LaminatingMethod derives CanEqual:
  case CompoundFoil, DispersionGlue, Fusing

final case class LaminatingParams(
    gapList: Vector[Float] = Vector.empty,
    laminatingBox: Option[Rectangle] = None,
    laminatingMethod: Option[LaminatingMethod] = None,
    moduleId: Option[Nmtoken] = None,
    nipWidth: Option[Float] = None,
    temperature: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("LaminatingParams")

final case class PerforatingParams(
    perforations: NonEmptyVector[Perforate],
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("PerforatingParams")

final case class SpinePreparationParams(
    millingDepth: Option[Float] = None,
    notchingDepth: Option[Float] = None,
    notchingDistance: Option[Float] = None,
    operations: Vector[Nmtoken] = Vector.empty,
    sealingTemperature: Option[Int] = None,
    startPosition: Option[Float] = None,
    workingLength: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("SpinePreparationParams")

final case class SpineTapingParams(
    horizontalExcess: Option[Float] = None,
    horizontalExcessBack: Option[Float] = None,
    stripLength: Option[Float] = None,
    topExcess: Option[Float] = None,
    glues: Vector[Glue] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("SpineTapingParams")

enum StrappingType derives CanEqual:
  case Single, Double, Cross, DoubleCross

final case class StrappingParams(
    strappingType: StrappingType,
    strapPositions: Vector[Float] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("StrappingParams")

final case class ThreadSealingParams(
    blindStitch: Option[Boolean] = None,
    threadLength: Option[Float] = None,
    threadPositions: Vector[Float] = Vector.empty,
    threadStitchWidth: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ThreadSealingParams")

enum VarnishingModuleType derives CanEqual:
  case PrintModule, CoatingModule

enum VarnishArea derives CanEqual:
  case Full, Spot

enum VarnishMethod derives CanEqual:
  case Blanket, Plate, Independent

final case class VarnishingParams(
    moduleId: Option[Nmtoken] = None,
    moduleType: Option[VarnishingModuleType] = None,
    varnishArea: Option[VarnishArea] = None,
    varnishMethod: Option[VarnishMethod] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("VarnishingParams")
