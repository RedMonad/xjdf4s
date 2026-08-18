#!/usr/bin/env python3
"""Regenerates the JSON derivation instances and dispatch registry for the codec-json module.

Sources of truth:
  - the XML derivation baseline (which case classes are derivable at all): DerivedInstances.scala
  - the XML registry (authoritative resource/intent/message names): Registry.scala
  - the model/messaging sources (field types, for dependency ordering and validation)
  - the JSON hand-codec list (slice codecs + specials with payload-enum fields)

Run from the repository root:  python3 tools/gen-json-codecs.py
"""
import os
import re
from collections import deque

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODEL_DIRS = [
    os.path.join(ROOT, "modules/model/src/main/scala"),
    os.path.join(ROOT, "modules/messaging/src/main/scala"),
]
XML_DERIVED = os.path.join(ROOT, "modules/codec-xml/src/main/scala/xjdf4s/codec/xml/domain/DerivedInstances.scala")
XML_REGISTRY = os.path.join(ROOT, "modules/codec-xml/src/main/scala/xjdf4s/codec/xml/domain/Registry.scala")
OUT_INSTANCES = os.path.join(ROOT, "modules/codec-json/src/main/scala/xjdf4s/codec/json/JsonDerivedInstances.scala")
OUT_REGISTRY = os.path.join(ROOT, "modules/codec-json/src/main/scala/xjdf4s/codec/json/JsonRegistry.scala")

# Types whose JSON codecs are hand-written in the slice or deferred (payload-enum fields need dedicated codecs).
JSON_HAND = """Comment GeneralId Part PartWaste PartAmount AmountPool Resource ResourceSet XJDF Glue
ColorMeasurementConditions Media MediaLayers Color Component Tool RunList RegisterMark Device Header Subscription
ResourceQuParams ResourceInfo DeviceInfo Notification MessageService QueryKnownMessages QueryResource
ResponseKnownMessages ResponseResource SignalNotification SignalResource SignalStatus XJMF ProcessRun AuditPool
Address Company FileSpec Disposition NetworkHeader TiffTag PlacedObject DeliveryFiles DeviceSchemas
DeviceInfoSchemas VerificationFiles QualityControlFiles BundleItem AssemblySection BindingIntent ColorIntent
StickOn CollatingItem LooseBindingParams Assembly ModifyQueueEntryParams QueueSubmissionParams""".split()

JSON_SPECIALS = """""".split()

# Types that transitively contain the specials (their codecs must exist first).
JSON_AFFECTED = []

# XML hand codecs whose JSON shape is plain field-uniform, so they derive normally. The binding details are
# XML Derived.derivedNamed instances (name overrides for their element names); the JSON codecs name members
# explicitly at their hand-codec call sites, so the default element names are fine and the types derive plainly.
JSON_ADDITIONS = [
    "Patch",
    "DeviceModule",
    "Dependent",
    "AdhesiveNoteDetails",
    "CoilLooseBindingDetails",
    "CombLooseBindingDetails",
    "EdgeGluingDetails",
    "HardCoverBindingDetails",
    "LooseBindingDetails",
    "RingLooseBindingDetails",
    "SoftCoverBindingDetails",
    "ChannelBindingProductionDetails",
    "CoilBindingProductionDetails",
    "CombBindingProductionDetails",
    "RingBindingProductionDetails",
    "StripBindingProductionDetails",
]

VALUE_TYPES = """Int Long Float Double Boolean String Vector[Byte] Nmtoken XsdId XsdIdRef XjdfString XsdDateTime
XsdDuration LanguageTag UriRef Priority0To100 CountryCode XPath PdfPath EvenPageCount CommonFolds QualityScore
NamedColor FoldCatalog NeutralDensity TransferFunction GluingPattern LabColor CmykColor SrgbColor IntegerRange
XYPair TileCoordinate Shape3D GridSize Matrix Rectangle Version JdfVersion HolePatternCatalog ScreeningType
MessageUrlScheme Extensions ExtensionElement NonEmptyVector TwoOrMore AtMostTwo Vector[Float]""".split()


def model_sources():
    for base in MODEL_DIRS:
        for root, _, files in os.walk(base):
            for name in sorted(files):
                if name.endswith(".scala"):
                    yield os.path.join(root, name)


def parse_classes():
    classes = {}
    for path in model_sources():
        text = open(path, encoding="utf-8").read()
        for m in re.finditer(r"(?:final\s+)?case class (\w+)\s*\((.*?)\)\s*(?:extends|derives)", text, re.S):
            classes[m.group(1)] = m.group(2)
    return classes


def fields_of(body):
    parts, depth, cur = [], 0, ""
    for ch in body:
        if ch in "([":
            depth += 1
        elif ch in ")]":
            depth -= 1
        if ch == "," and depth == 0:
            parts.append(cur)
            cur = ""
        else:
            cur += ch
    parts.append(cur)
    out = []
    for part in parts:
        mm = re.match(r"\s*(\w+)\s*:\s*([A-Za-z][\w.]*(?:\s*\[[^=\]]*\])?)\s*(?:=|,|$)", part)
        if mm:
            out.append(mm.group(2).strip().split(".")[-1])
    return out


def xml_derived_set():
    # match only real given declarations, so scaladoc mentions like summon[XmlElementCodec[X]] never leak in
    lines = open(XML_DERIVED, encoding="utf-8").read().splitlines()
    return set(
        name
        for line in lines
        for name in re.findall(r"^given \w+: XmlElementCodec\[(\w+)\]", line)
    )


def registry_names():
    text = open(XML_REGISTRY, encoding="utf-8").read()

    def block(start_marker, end_marker):
        begin = text.index(start_marker)
        end = text.index(end_marker, begin)
        return re.findall(r'name\("(\w+)"\)', text[begin:end])

    resources = block("val resourceDecoders:", "val resourceNames")
    messages = block("val messageDecoders:", "val messageNames")
    intents = block("val intentDecoders:", "val intentNames")
    return resources, messages, intents


def main():
    classes = parse_classes()
    xml_derived = xml_derived_set()
    resources, messages, intents = registry_names()
    print(f"xml derived: {len(xml_derived)}, resources: {len(resources)}, messages: {len(messages)}, intents: {len(intents)}")

    excluded = set(JSON_HAND) | set(JSON_SPECIALS) | set(JSON_AFFECTED)
    candidates = sorted((xml_derived - excluded) | set(JSON_ADDITIONS) - set(JSON_AFFECTED))
    print(f"json candidates: {len(candidates)}")

    # affected closure: any class that (transitively) contains a deferred special gets deferred itself, so the
    # inline deriveOrSummon fallback never silently encodes a payload enum as a bare case name
    wrappers = ("Option[", "Vector[", "NonEmptyVector[", "TwoOrMore[", "AtMostTwo[")

    def inner(ftype):
        current = ftype
        while current.startswith(wrappers):
            current = current[current.index("[") + 1 : -1]
        return current

    frontier, affected = set(JSON_SPECIALS), set()
    changed = True
    while changed:
        changed = False
        for name, body in classes.items():
            if name in frontier:
                continue
            if {inner(ftype) for ftype in fields_of(body)} & frontier:
                frontier.add(name)
                affected.add(name)
                changed = True
    print(f"affected closure: {len(affected)} classes")
    candidates = sorted(set(candidates) - affected)

    # validate: every candidate field must be a value type, an enum, a container, or a class with a codec path
    covered = set(candidates) | set(JSON_HAND) | set(JSON_SPECIALS) | affected
    problems = []
    for name in candidates:
        for ftype in fields_of(classes.get(name, "")):
            bare = inner(ftype)
            if bare in VALUE_TYPES or bare in covered:
                continue
            # any remaining CamelCase type is an enum (plain enums have the generic circe instances)
            if re.fullmatch(r"[A-Z][A-Za-z0-9]*", bare):
                continue
            problems.append((name, ftype))
    if problems:
        for p in problems:
            print("  UNCOVERED FIELD:", p)
        raise SystemExit(1)

    # topological order by field dependencies (classes first, then their containers)
    deps = {name: [f for f in fields_of(classes.get(name, "")) if f in set(candidates)] for name in candidates}
    order, seen = [], set()

    def visit(name, stack):
        if name in seen:
            return
        # mutual/self recursion is fine at the given level (givens reference each other's types, not bodies);
        # break the cycle by emitting in arbitrary order - the order does not affect resolution
        if name in stack:
            return
        stack.add(name)
        for dep in sorted(deps.get(name, [])):
            visit(dep, stack)
        stack.remove(name)
        seen.add(name)
        order.append(name)

    for name in candidates:
        visit(name, set())
    print(f"ordered: {len(order)}")

    instance_lines = []
    for name in order:
        instance_lines.append(f"given derivedJsonEncoder{name}: Encoder[{name}] = JsonDerived.derivedEncoder[{name}]")
        instance_lines.append(f"given derivedJsonDecoder{name}: Decoder[{name}] = JsonDerived.derivedDecoder[{name}]")

    with open(OUT_INSTANCES, "w", encoding="utf-8") as out:
        out.write("""package xjdf4s.codec.json

import io.circe.{Decoder, Encoder}

import xjdf4s.codec.json.JsonFieldCodec.given

import xjdf4s.codec.json.JsonScalars.given

import xjdf4s.codec.json.JsonNodeCodecs.given

import xjdf4s.codec.json.JsonMediaCodecs.given

import xjdf4s.codec.json.JsonMessagingCodecs.given

import xjdf4s.codec.json.JsonSpecialCodecs.given

import xjdf4s.codec.json.JsonResources.given

import xjdf4s.core.*

import xjdf4s.messaging.*

import xjdf4s.model.*

import xjdf4s.model.resources.*

/**
 * GENERATED: root circe Encoder/Decoder givens for every case class without a hand-written JSON codec. They are
 * non-inline givens because inline givens cannot be found by ordinary implicit search; each one calls the
 * inline derivation entry point, which then expands at this call site. Nested product fields resolve through
 * the canonical deriveOrSummon fallback in JsonFieldCodec.productFieldCodec, so the list order does not affect
 * resolution (it is kept topologically sorted by field dependencies anyway).
 *
 * Excluded: the JSON hand codecs (slice codecs plus the special forms with payload-enum fields whose JSON
 * mappings live in JsonSpecialCodecs), the self-recursive BundleItem/AssemblySection (XML rule f: the generic
 * derivation would recurse infinitely - the given is not visible in its own initializer, so the inline fallback
 * re-derives the type forever; their hand codecs recurse at runtime instead), the specials still waiting for
 * their hand codecs (BindingIntent, ColorIntent, StickOn, CollatingItem, LooseBindingParams, Assembly,
 * ModifyQueueEntryParams, QueueSubmissionParams) and every class that transitively embeds them (computed as a
 * closure, so the inline deriveOrSummon fallback never silently encodes a payload enum as a bare case name).
 *
 * Regenerate with tools/gen-json-codecs.py when the model grows.
 */
""")
        for line in instance_lines:
            out.write(line + "\n")
    print(f"wrote {OUT_INSTANCES}")

    # registry: exclude the deferred classes without JSON codecs
    covered_resources = [r for r in resources if r not in JSON_SPECIALS and r not in affected]
    covered_intents = [i for i in intents if i not in JSON_SPECIALS and i not in affected]
    covered_messages = [m for m in messages if m not in JSON_SPECIALS and m not in affected]
    print(f"registry: resources {len(covered_resources)}, intents {len(covered_intents)}, messages {len(covered_messages)}")

    with open(OUT_REGISTRY, "w", encoding="utf-8") as out:
        out.write("""package xjdf4s.codec.json

import io.circe.{Decoder, Json}
import io.circe.syntax.*

import xjdf4s.codec.json.JsonSpecialCodecs.given

import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * GENERATED registry: JSON dispatch tables for the open substitution points. Every entry summons the
 * Encoder/Decoder of the type - hand-written where the JSON shape is special (the slice codecs), derived
 * otherwise. Regenerate with tools/gen-json-codecs.py when the model grows.
 *
 * Deferred until their hand codecs land: Assembly, LooseBindingParams (payload-enum resources),
 * FeedingParams (embeds CollatingItem), BindingIntent/ColorIntent/AssemblingIntent (payload-enum intents),
 * CommandModifyQueueEntry/CommandSubmitQueueEntry (embed deferred params); the foreign message carriers
 * stay rejected by design.
 */
object JsonRegistry:

""")
        out.write("  val resourceNames: Set[String] = Set(\n")
        for name in covered_resources:
            out.write(f'    "{name}",\n')
        out.write("  )\n\n")

        out.write("  val resourceEncoders: Map[String, SpecificResource => Json] = Map(\n")
        for name in covered_resources:
            out.write(f'    "{name}" -> (value => value.asInstanceOf[{name}].asJson),\n')
        out.write("  )\n\n")

        out.write("  val resourceDecoders: Map[String, Json => Decoder.Result[SpecificResource]] = Map(\n")
        for name in covered_resources:
            out.write(f'    "{name}" -> (json => summon[Decoder[{name}]].decodeJson(json)),\n')
        out.write("  )\n\n")

        out.write("  val intentNames: Set[String] = Set(\n")
        for name in covered_intents:
            out.write(f'    "{name}",\n')
        out.write("  )\n\n")

        out.write("  val intentEncoders: Map[String, ProductIntent => Json] = Map(\n")
        for name in covered_intents:
            out.write(f'    "{name}" -> (value => value.asInstanceOf[{name}].asJson),\n')
        out.write("  )\n\n")

        out.write("  val intentDecoders: Map[String, Json => Decoder.Result[ProductIntent]] = Map(\n")
        for name in covered_intents:
            out.write(f'    "{name}" -> (json => summon[Decoder[{name}]].decodeJson(json)),\n')
        out.write("  )\n\n")

        out.write("  val messageNames: Set[String] = Set(\n")
        for name in covered_messages:
            out.write(f'    "{name}",\n')
        out.write("  )\n\n")

        out.write("  val messageEncoders: Map[String, Message => Json] = Map(\n")
        for name in covered_messages:
            out.write(f'    "{name}" -> (value => value.asInstanceOf[{name}].asJson),\n')
        out.write("  )\n\n")

        out.write("  val messageDecoders: Map[String, Json => Decoder.Result[Message]] = Map(\n")
        for name in covered_messages:
            out.write(f'    "{name}" -> (json => summon[Decoder[{name}]].decodeJson(json)),\n')
        out.write("  )\n\n")

        out.write("""  /** The dispatch name of a specific resource: the normative element name equals the runtime class name. */
  def resourceName(resource: SpecificResource): String = resource.getClass.getSimpleName

  /** The dispatch name of a product intent. */
  def intentName(intent: ProductIntent): String = intent.getClass.getSimpleName

  /** The dispatch name of a message. */
  def messageName(message: Message): String = message.getClass.getSimpleName

  def encodeSpecificResource(resource: SpecificResource): Json =
    resourceEncoders.get(resourceName(resource)) match
      case Some(encode) => encode(resource)
      case None => throw new UnsupportedOperationException(
          s"no JSON codec for resource ${resourceName(resource)}",
        )

  def decodeSpecificResource(name: String, json: Json): Decoder.Result[SpecificResource] =
    resourceDecoders.get(name) match
      case Some(decode) => decode(json)
      case None         => JsonHelpers.fail(json.hcursor, s"resource '$name' is not covered by the JSON codec")

  def encodeProductIntent(intent: ProductIntent): Json =
    intentEncoders.get(intentName(intent)) match
      case Some(encode) => encode(intent)
      case None => throw new UnsupportedOperationException(
          s"no JSON codec for intent ${intentName(intent)}",
        )

  def decodeProductIntent(name: String, json: Json): Decoder.Result[ProductIntent] =
    intentDecoders.get(name) match
      case Some(decode) => decode(json)
      case None         => JsonHelpers.fail(json.hcursor, s"intent '$name' is not covered by the JSON codec")

  def encodeMessage(message: Message): Json =
    messageEncoders.get(messageName(message)) match
      case Some(encode) => encode(message)
      case None => throw new UnsupportedOperationException(
          s"no JSON codec for message ${messageName(message)}",
        )

  def decodeMessage(name: String, json: Json): Decoder.Result[Message] =
    messageDecoders.get(name) match
      case Some(decode) => decode(json)
      case None         => JsonHelpers.fail(json.hcursor, s"message '$name' is not covered by the JSON codec")
end JsonRegistry
""")
    print(f"wrote {OUT_REGISTRY}")


if __name__ == "__main__":
    main()
