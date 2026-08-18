package xjdf4s.codec.json

import io.circe.{Decoder, DecodingFailure, HCursor, Json}

import xjdf4s.core.*

/**
 * JSON support for foreign-namespace content (9.10.2.4, Example 9.12): XJDF members are unprefixed, foreign
 * attributes and elements map to `"Prefix:Name"` members, and the prefix-to-namespace mapping is carried by the
 * JSON-LD `"@context"` member. The JSON-LD subset used here is minimal by design.
 *
 * Namespaces without a prefix get a synthesized `"ns1"`, `"ns2"`, ... prefix (assigned per encode call, sorted by
 * namespace for determinism). On decode the `"@context"` map reverses the prefix; an unmapped prefix falls back
 * to using the prefix itself as the namespace, so the JSON round-trip stays lossless.
 */
object JsonForeign:

  /** Per-encode prefix assignment: one table per call keeps the encoder thread-safe and deterministic. */
  private final class PrefixTable:
    private val byNamespace = scala.collection.mutable.Map.empty[String, String]
    private var counter: Int = 0

    def keyOf(name: QualifiedName): String =
      name.prefix match
        case Some(prefix) => s"$prefix:${name.localName}"
        case None         => s"${synthesized(name.namespace)}:${name.localName}"

    private def synthesized(namespace: String): String =
      byNamespace.getOrElseUpdate(
        namespace, {
          counter += 1
          s"ns$counter"
        },
      )
  end PrefixTable

  /** Members contributed by an [[Extensions]] value: foreign attributes/elements plus the shared `"@context"`. */
  def encodeExtensions(extensions: Extensions): Vector[(String, Json)] =
    val prefixes = new PrefixTable
    val attributes = extensions.attributes.toVector
      .sortBy(pair => (pair._1.namespace, pair._1.localName))
      .map { case (name, value) => (prefixes.keyOf(name), name.namespace, extensionValueJson(value)) }
    val elements = extensions.elements.map { element =>
      (prefixes.keyOf(element.name.qualifiedName), element.name.qualifiedName.namespace, element)
    }
    val entries = attributes.map(entry => (entry._1, entry._2)) ++ elements.map(entry => (entry._1, entry._2))
    contextMember(entries) ++ attributes.map(entry => (entry._1, entry._3)) ++
      elements.map(entry => (entry._1, encodeElement(entry._3, prefixes)))

  /** Decodes the foreign attributes and child elements of an object plus its `"@context"`. */
  def decodeExtensions(cursor: HCursor): Decoder.Result[Extensions] =
    decodeForeignMembers(cursor).map { case (attributes, elements) =>
      if attributes.isEmpty && elements.isEmpty then Extensions.empty
      else Extensions(attributes = attributes, elements = elements)
    }

  /** Decodes only the foreign child elements of an object (`foreignElements` fields). */
  def decodeForeignElements(cursor: HCursor): Decoder.Result[Vector[ExtensionElement]] =
    decodeForeignMembers(cursor).map(_._2)

  /** The first foreign element member of an object, if any (the open-intent fallback of the Intent codec). */
  def decodeForeignElement(cursor: HCursor): Decoder.Result[Option[ExtensionElement]] =
    decodeForeignMembers(cursor).map(_._2.headOption)

  /** Members contributed by a foreign element: its `"Prefix:Name"` key plus the shared `"@context"`. */
  def encodeForeignElementMember(element: ExtensionElement): Vector[(String, Json)] =
    val prefixes = new PrefixTable
    val key = prefixes.keyOf(element.name.qualifiedName)
    contextMember(Vector((key, element.name.qualifiedName.namespace))) ++ Vector((key, encodeElement(element, prefixes)))

  private def contextMember(entries: Vector[(String, String)]): Vector[(String, Json)] =
    val mappings = entries.distinctBy(_._1).sortBy(_._1).map { case (key, namespace) =>
      (key.substring(0, key.indexOf(":")), Json.fromString(namespace))
    }
    if mappings.isEmpty then Vector.empty
    else Vector(("@context", Json.obj(mappings*)))

  /** JSON object for a foreign element: attributes become scalar members, children become object members. */
  private def encodeElement(element: ExtensionElement, prefixes: PrefixTable): Json =
    val attributeEntries = (element.attributes.toVector ++ element.extensions.attributes.toVector)
      .sortBy(pair => (pair._1.namespace, pair._1.localName))
      .map { case (name, value) => (prefixes.keyOf(name), name.namespace, extensionValueJson(value)) }
    val childEntries = element.content
      .collect { case ExtensionContent.Element(node) => node }
      .map(node => (prefixes.keyOf(node.name.qualifiedName), node.name.qualifiedName.namespace, node))
    val childMembers = childEntries
      .groupBy(entry => entry._1)
      .toVector
      .sortBy(_._1)
      .map { case (key, entries) => (key, oneOrArray(entries.map(entry => encodeElement(entry._3, prefixes)))) }
    val attributeMembers = attributeEntries.map { case (key, _, value) => (key, value) }
    val context = contextMember((attributeEntries.map(entry => (entry._1, entry._2)) ++ childEntries.map(entry => (entry._1, entry._2))).toVector)
    Json.obj((context ++ attributeMembers ++ childMembers ++ textMember(element.content))*)

  private def decodeForeignMembers(cursor: HCursor): Decoder.Result[(Map[QualifiedName, ExtensionValue], Vector[ExtensionElement])] =
    val keys = cursor.keys.getOrElse(Iterable.empty).toVector
    val context = contextOf(cursor)
    keys
      .filter(key => key != "@context" && key.contains(":"))
      .foldLeft[Decoder.Result[(Map[QualifiedName, ExtensionValue], Vector[ExtensionElement])]](
        Right((Map.empty, Vector.empty)),
      ) { (acc, key) =>
        for
          accumulated <- acc
          member <- cursor.downField(key).focus.toRight(DecodingFailure(s"missing foreign member '$key'", cursor.history))
          decoded <- decodeForeignMember(key, context, member)
        yield (accumulated._1 ++ decoded._1, accumulated._2 ++ decoded._2)
      }

  private def contextOf(cursor: HCursor): Map[String, String] =
    val mappings = Vector.newBuilder[(String, String)]
    for
      key <- cursor.keys.getOrElse(Iterable.empty)
      if key == "@context"
      json <- cursor.downField(key).focus
      obj <- json.asObject
      (prefix, value) <- obj.toIterable
      if value.isString
    do mappings += ((prefix, value.asString.getOrElse("")))
    mappings.result().toMap

  private def contextOf(json: Json): Map[String, String] =
    contextOf(json.hcursor)

  private def decodeForeignMember(
      key: String,
      context: Map[String, String],
      json: Json,
  ): Decoder.Result[(Map[QualifiedName, ExtensionValue], Vector[ExtensionElement])] =
    val split = key.indexOf(":")
    val prefix = key.substring(0, split)
    val localName = key.substring(split + 1)
    if isElementJson(json) then
      val items = if json.isArray then json.asArray.get.toVector else Vector(json)
      items
        .foldLeft[Decoder.Result[Vector[ExtensionElement]]](Right(Vector.empty)) { (acc, item) =>
          for
            elements <- acc
            element <- decodeElement(prefix, localName, context, item)
          yield elements :+ element
        }
        .map(elements => (Map.empty, elements))
    else
      val name = QualifiedName(context.getOrElse(prefix, prefix), localName, Some(prefix))
      jsonExtensionValue(json, key).map(value => (Map(name -> value), Vector.empty))

  private def isElementJson(json: Json): Boolean =
    json.isObject || (json.isArray && json.asArray.exists(_.forall(_.isObject)))

  private def decodeElement(prefix: String, localName: String, context: Map[String, String], json: Json): Decoder.Result[ExtensionElement] =
    ForeignQName
      .from(context.getOrElse(prefix, prefix), localName, Some(prefix))
      .left
      .map(error => DecodingFailure(error.toString, Nil))
      .flatMap { name =>
        val keys = json.asObject.map(_.keys.toVector).getOrElse(Vector.empty)
        val innerContext = contextOf(json)
        val memberKeys = keys.filter(key => key != "@context" && key != "Text" && key.contains(":"))
        memberKeys
          .foldLeft[Decoder.Result[(Map[QualifiedName, ExtensionValue], Vector[ExtensionContent])]](
            Right((Map.empty, Vector.empty)),
          ) { (acc, key) =>
            for
              accumulated <- acc
              member <- json.hcursor.downField(key).focus.toRight(DecodingFailure(s"missing foreign member '$key'", Nil))
              decoded <-
                if isElementJson(member) then
                  val memberSplit = key.indexOf(":")
                  val items = if member.isArray then member.asArray.get.toVector else Vector(member)
                  items
                    .foldLeft[Decoder.Result[Vector[ExtensionElement]]](Right(Vector.empty)) { (innerAcc, item) =>
                      for
                        elements <- innerAcc
                        element <- decodeElement(key.substring(0, memberSplit), key.substring(memberSplit + 1), innerContext, item)
                      yield elements :+ element
                    }
                    .map(elements => elements.map(element => Right(ExtensionContent.Element(element))))
                else
                  jsonExtensionValue(member, key).map { value =>
                    val memberSplit = key.indexOf(":")
                    val attributeName = QualifiedName(
                      innerContext.getOrElse(key.substring(0, memberSplit), key.substring(0, memberSplit)),
                      key.substring(memberSplit + 1),
                      Some(key.substring(0, memberSplit)),
                    )
                    Vector(Left((attributeName, value)))
                  }
            yield
              decoded.foldLeft(accumulated) { case ((attributes, contents), entry) =>
                entry match
                  case Left(attribute) => (attributes + attribute, contents)
                  case Right(content)  => (attributes, contents :+ content)
              }
          }
          .flatMap { case (attributes, contents) =>
            decodeText(json, keys).map { texts =>
              ExtensionElement(name, attributes, texts.map(ExtensionContent.Text(_)) ++ contents)
            }
          }
      }

  private def decodeText(json: Json, keys: Vector[String]): Decoder.Result[Vector[String]] =
    keys
      .filter(_ == "Text")
      .foldLeft[Decoder.Result[Vector[String]]](Right(Vector.empty)) { (acc, key) =>
        for
          accumulated <- acc
          member <- json.hcursor.downField(key).focus.toRight(DecodingFailure("missing 'Text' member", Nil))
          decoded <-
            if member.isString then Right(Vector(member.asString.get))
            else if member.isArray then
              member.asArray.get.toVector.foldLeft[Decoder.Result[Vector[String]]](Right(Vector.empty)) { (innerAcc, item) =>
                for
                  items <- innerAcc
                  text <- item.asString.toRight(DecodingFailure("Text member entries must be strings", Nil))
                yield items :+ text
              }
            else Left(DecodingFailure("Text member must be a string or an array of strings", Nil))
        yield accumulated ++ decoded
      }

  private def extensionValueJson(value: ExtensionValue): Json =
    value match
      case ExtensionValue.Text(text) => Json.fromString(text)
      case ExtensionValue.Number(n)  => Json.fromBigDecimal(n)
      case ExtensionValue.Bool(flag) => Json.fromBoolean(flag)
      case ExtensionValue.Null       => Json.Null

  private def jsonExtensionValue(json: Json, key: String): Decoder.Result[ExtensionValue] =
    if json.isString then Right(ExtensionValue.Text(json.asString.get))
    else if json.isNumber then
      json.asNumber
        .flatMap(_.toBigDecimal)
        .toRight(DecodingFailure(s"foreign member '$key' is not a valid number", Nil))
        .map(ExtensionValue.Number(_))
    else if json.isBoolean then Right(ExtensionValue.Bool(json.asBoolean.get))
    else if json.isNull then Right(ExtensionValue.Null)
    else Left(DecodingFailure(s"foreign member '$key' must be a scalar", Nil))

  private def textMember(content: Vector[ExtensionContent]): Vector[(String, Json)] =
    val texts = content.collect { case ExtensionContent.Text(value) => value }
    texts match
      case Vector()       => Vector.empty
      case Vector(single) => Vector(("Text", Json.fromString(single)))
      case many           => Vector(("Text", Json.arr(many.map(Json.fromString)*)))

  private def oneOrArray(values: Vector[Json]): Json =
    values match
      case Vector(single) => single
      case many           => Json.arr(many*)
end JsonForeign
