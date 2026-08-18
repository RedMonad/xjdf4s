package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.core.*

object GeneralIdCodec:
  /** Table A.14 lexical forms: lowercase XSD names, `NamedFeature`, and the uppercase `NMTOKEN`. */
  private def nameOf(value: GeneralId.DataType): String = value match
    case GeneralId.DataType.Boolean => "boolean"
    case GeneralId.DataType.DateTime => "dateTime"
    case GeneralId.DataType.Duration => "duration"
    case GeneralId.DataType.Float => "float"
    case GeneralId.DataType.Integer => "integer"
    case GeneralId.DataType.NamedFeature => "NamedFeature"
    case GeneralId.DataType.Nmtoken => "NMTOKEN"
    case GeneralId.DataType.String => "string"

  val dataType: Lexical.Lex[GeneralId.DataType] =
    value =>
      GeneralId.DataType.values
        .find(candidate => nameOf(candidate).equalsIgnoreCase(value.trim))
        .toRight(s"'$value' is not a GeneralID/@DataType value")

  val decoder: XmlDecoder[GeneralId] =
    XmlDecoder.instance: element =>
      for
        usage         <- XmlDecoders.requiredAttribute("IDUsage")(Lexical.nmtoken).decode(element)
        value         <- XmlDecoders.requiredAttribute("IDValue")(Lexical.xjdfString).decode(element)
        dataTypeValue <- XmlDecoders.attributeOf("DataType")(dataType).decode(element)
        _             <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
      yield GeneralId(usage, value, dataTypeValue, CodecHelpers.decodeExtensionAttributes(element))

  val encoder: XmlEncoder[GeneralId] =
    XmlEncoder.instance: generalId =>
      val attributes =
        CodecHelpers.attributeOf("DataType", generalId.dataType, nameOf) ++
          CodecHelpers.attribute("IDUsage", Some(generalId.usage.value)) ++
          CodecHelpers.attribute("IDValue", Some(generalId.value.value)) ++
          CodecHelpers.extensionAttributes(generalId.extensions)
      Xml.Element(CodecHelpers.qname("GeneralID"), attributes, Vector.empty)
end GeneralIdCodec
