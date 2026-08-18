package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

final case class CaseMakingParams(
    bottomFoldIn: Option[Float] = None,
    cornerType: Option[Nmtoken] = None,
    coverWidth: Option[Float] = None,
    frontFoldIn: Option[Float] = None,
    height: Option[Float] = None,
    jointWidth: Option[Float] = None,
    spineWidth: Option[Float] = None,
    topFoldIn: Option[Float] = None,
    glue: Option[Glue] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("CaseMakingParams")

final case class CasingInParams(
    caseRadius: Option[Float] = None,
    coverWidth: Option[Float] = None,
    spineWidth: Option[Float] = None,
    glues: Vector[Glue] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("CasingInParams")

enum ScoreSide derives CanEqual:
  case FromInside, FromOutside

final case class Score(
    offset: Float,
    side: ScoreSide,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class CoverApplicationParams(
    glues: Vector[Glue] = Vector.empty,
    scores: Vector[Score] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("CoverApplicationParams")

final case class HeadBandApplicationParams(
    length: Option[Float] = None,
    width: Option[Float] = None,
    glues: Vector[Glue] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("HeadBandApplicationParams")

final case class JacketingParams(
    foldingWidth: Float,
    foldingDistance: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("JacketingParams")

enum ShrinkingMethod derives CanEqual:
  case ShrinkCool, ShrinkHot

final case class ShrinkingParams(
    shrinkingMethod: Option[ShrinkingMethod] = None,
    temperature: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("ShrinkingParams")

enum WrappingKind derives CanEqual:
  case Band, LooseWrap, ShrinkWrap

final case class WrappingParams(
    wrappingKind: WrappingKind,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("WrappingParams")

final case class WindingParams(
    copies: Option[Int] = None,
    diameter: Option[Float] = None,
    fixation: Option[Nmtoken] = None,
    length: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("WindingParams")

enum TrimCover derives CanEqual:
  case Back, Both, Front, Neither

final case class TrimmingParams(
    height: Option[Float] = None,
    trimCover: Option[TrimCover] = None,
    trimmingOffset: Option[Float] = None,
    width: Option[Float] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("TrimmingParams")

enum StitchOrigin derives CanEqual:
  case TrimBoxCenter, TrimBoxJogSide, UntrimmedJogSide

enum StitchType derives CanEqual:
  case Corner, Saddle, Side

final case class StitchingParams(
    angle: Option[Float] = None,
    numberOfStitches: Option[Int] = None,
    offset: Option[Float] = None,
    stapleShape: Option[StapleShape] = None,
    stitchOrigin: Option[StitchOrigin] = None,
    stitchPositions: Vector[Float] = Vector.empty,
    stitchType: Option[StitchType] = None,
    stitchWidth: Option[Float] = None,
    tightBacking: Option[TightBacking] = None,
    wireGauge: Option[Float] = None,
    fileSpec: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("StitchingParams")
