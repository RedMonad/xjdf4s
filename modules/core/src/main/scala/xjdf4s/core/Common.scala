package xjdf4s.core

final case class Comment(
    value: String,
    author: Option[String] = None,
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
    value: String,
    dataType: Option[GeneralId.DataType] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

object GeneralId:
  enum DataType derives CanEqual:
    case Boolean, DateTime, Double, Duration, Integer, Name, Nmtoken, String, Uri
  end DataType
end GeneralId
