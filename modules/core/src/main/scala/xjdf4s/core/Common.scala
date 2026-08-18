package xjdf4s.core

final case class Comment(
    value: String,
    author: Option[XjdfString] = None,
    externalId: Option[Nmtoken] = None,
    language: Option[LanguageTag] = None,
    personalId: Option[Nmtoken] = None,
    timeStamp: Option[XsdDateTime] = None,
    commentType: Option[Nmtoken] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class GeneralId(
    usage: Nmtoken,
    value: XjdfString,
    dataType: Option[GeneralId.DataType] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

object GeneralId:
  /** The normative Table A.14 vocabulary. `NamedFeature` is the standard gray-box mechanism for process presets and
   *  pairs a value item with a name item. The `xs:` prefixes are lexical codec details and do not change the value set.
   */
  enum DataType derives CanEqual:
    case Boolean, DateTime, Duration, Float, Integer, NamedFeature, Nmtoken, String
