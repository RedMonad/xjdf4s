package xjdf4s.codec.xml.domain

import xjdf4s.codec.xml.*
import xjdf4s.core.*

object CommentCodec:
  val decoder: XmlDecoder[Comment] =
    XmlDecoder.instance: element =>
      for
        value       <- XmlDecoders.textContent.decode(element)
        author      <- XmlDecoders.attributeOf("Author")(Lexical.xjdfString).decode(element)
        externalId  <- XmlDecoders.attributeOf("ExternalID")(Lexical.nmtoken).decode(element)
        language    <- XmlDecoders.attributeOf("Language")(Lexical.languageTag).decode(element)
        personalId  <- XmlDecoders.attributeOf("PersonalID")(Lexical.nmtoken).decode(element)
        timeStamp   <- XmlDecoders.attributeOf("TimeStamp")(Lexical.dateTime).decode(element)
        commentType <- XmlDecoders.attributeOf("CommentType")(Lexical.nmtoken).decode(element)
        _           <- XmlDecoders.expectChildrenOnly(Set.empty).decode(element)
      yield Comment(
        value,
        author,
        externalId,
        language,
        personalId,
        timeStamp,
        commentType,
        CodecHelpers.decodeExtensionAttributes(element),
      )

  val encoder: XmlEncoder[Comment] =
    XmlEncoder.instance: comment =>
      val attributes =
        CodecHelpers.attributeOf("Author", comment.author, (value: XjdfString) => value.value) ++
          CodecHelpers.attributeOf("ExternalID", comment.externalId, (value: Nmtoken) => value.value) ++
          CodecHelpers.attributeOf("Language", comment.language, (value: LanguageTag) => value.value) ++
          CodecHelpers.attributeOf("PersonalID", comment.personalId, (value: Nmtoken) => value.value) ++
          CodecHelpers.attributeOf("TimeStamp", comment.timeStamp, (value: XsdDateTime) => value.value) ++
          CodecHelpers.attributeOf("CommentType", comment.commentType, (value: Nmtoken) => value.value) ++
          CodecHelpers.extensionAttributes(comment.extensions)
      Xml.Element(CodecHelpers.qname("Comment"), attributes, Vector(Xml.Text(comment.value)))
end CommentCodec
