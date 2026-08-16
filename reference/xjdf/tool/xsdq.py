#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import json
import os
import re
import sys
from collections import defaultdict, deque

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

KINDS = [
    "element",
    "complexType",
    "simpleType",
    "attribute",
    "group",
    "attributeGroup",
    "builtin",
    "anonymous",
]

DEFAULT_INDEX_CANDIDATES = [
    "xsd-index.json",
    ".xsd-index/xsd-index.json",
    "index.json",
    ".xsd-index/index.json",
    ".xsd-index",
]

# Relations that are safe to traverse when building a bundle.
TRAVERSE_OUT = {
    "element_type",
    "child_type",
    "child_element",
    "attribute_type",
    "attribute_ref",
    "base",
    "extension",
    "restriction",
    "substitution_group",
    "group_ref",
    "attribute_group_ref",
    "itemType",
    "union_member",
}

# Very conservative Scala 3 mapping for XSD built-ins.
# Can be overridden by providing "scala" hints in the index itself.
BUILTIN_SCALA = {
    "string": "String",
    "normalizedString": "String",
    "token": "String",
    "boolean": "Boolean",
    "int": "Int",
    "integer": "BigInt",
    "positiveInteger": "BigInt",
    "negativeInteger": "BigInt",
    "nonPositiveInteger": "BigInt",
    "nonNegativeInteger": "BigInt",
    "long": "Long",
    "short": "Short",
    "byte": "Byte",
    "decimal": "BigDecimal",
    "float": "Float",
    "double": "Double",
    "date": "java.time.LocalDate",
    "dateTime": "java.time.Instant",
    "time": "java.time.LocalTime",
    "duration": "String",
    "yearMonth": "String",
    "monthDay": "String",
    "gYear": "String",
    "gYearMonth": "String",
    "gMonth": "String",
    "gMonthDay": "String",
    "gDay": "String",
    "anyURI": "java.net.URI",
    "QName": "String",
    "NOTATION": "String",
    "base64Binary": "Array[Byte]",
    "hexBinary": "Array[Byte]",
    "unsignedInt": "Long",
    "unsignedLong": "BigInt",
    "unsignedShort": "Int",
    "unsignedByte": "Short",
    "anySimpleType": "String",
    "anyType": "Any",
}

SCALA_KEYWORDS = {
    "abstract", "case", "catch", "class", "def", "do", "else", "enum",
    "export", "extends", "false", "final", "finally", "for", "forSome",
    "given", "if", "implicit", "import", "lazy", "match", "new", "null",
    "object", "override", "package", "private", "protected", "return",
    "sealed", "super", "then", "this", "throw", "trait", "true", "try",
    "type", "val", "var", "while", "with", "yield",
}

HELP_EPILOG = """
Examples:
  xsdq summary
  xsdq list --kind element --limit 20
  xsdq search Order --kind element
  xsdq get "element:{urn:example}Order"
  xsdq type "element:{urn:example}Order"
  xsdq children "element:{urn:example}Order" --depth 2
  xsdq attrs "complexType:{urn:example}OrderType"
  xsdq used-by "complexType:{urn:example}AddressType"
  xsdq uses "complexType:{urn:example}OrderType"
  xsdq hierarchy "complexType:{urn:example}PaymentType"
  xsdq bundle "element:{urn:example}Order" --depth 2 --max-nodes 200 --scala --compact
  xsdq scala-hints "complexType:{urn:example}OrderType" --depth 1

Index location:
  By default the tool searches for:
    - xsd-index.json
    - .xsd-index/xsd-index.json
    - index.json
    - .xsd-index/index.json
    - .xsd-index/

  You can point to a file or directory explicitly:
    xsdq summary --index path/to/xsd-index.json
    xsdq summary --index path/to/.xsd-index

ID format:
  The preferred stable IDs are:
    element:{namespace}LocalName
    complexType:{namespace}LocalName
    simpleType:{namespace}LocalName
    attribute:{namespace}LocalName
    group:{namespace}LocalName
    attributeGroup:{namespace}LocalName
    builtin:xs:string
    anonymous:some/stable/path

  Examples:
    element:{urn:example:order}Order
    complexType:{urn:example:order}OrderType
    simpleType:{urn:example:order}CurrencyCode
    builtin:xs:string

  If a bare name is given, e.g. "Order", the tool tries to resolve it.
  If resolution is ambiguous, it returns candidates as JSON error.

Expected index format:
  The index is a single JSON file:

  {
    "meta": {
      "source": "schema.xsd",
      "generatedAt": "2026-01-01T00:00:00Z",
      "targetNamespace": "urn:example:order",
      "roots": ["element:{urn:example:order}Order"]
    },
    "nodes": {
      "element:{urn:example:order}Order": {
        "id": "element:{urn:example:order}Order",
        "kind": "element",
        "name": "Order",
        "namespace": "urn:example:order",
        "type": "complexType:{urn:example:order}OrderType",
        "abstract": false,
        "nillable": false,
        "substitutionGroup": null,
        "annotation": null
      },
      "complexType:{urn:example:order}OrderType": {
        "id": "complexType:{urn:example:order}OrderType",
        "kind": "complexType",
        "name": "OrderType",
        "namespace": "urn:example:order",
        "abstract": false,
        "mixed": false,
        "base": null,
        "derivation": null,
        "attributes": [
          {
            "name": "id",
            "type": "builtin:xs:string",
            "use": "required",
            "default": null,
            "fixed": null,
            "wildcard": false
          }
        ],
        "children": [
          {
            "name": "Header",
            "node": "element:{urn:example:order}Header",
            "type": "complexType:{urn:example:order}HeaderType",
            "minOccurs": 1,
            "maxOccurs": 1,
            "container": "sequence",
            "choiceId": null,
            "nillable": false,
            "wildcard": false
          }
        ],
        "annotation": null
      },
      "simpleType:{urn:example:order}CurrencyCode": {
        "id": "simpleType:{urn:example:order}CurrencyCode",
        "kind": "simpleType",
        "name": "CurrencyCode",
        "namespace": "urn:example:order",
        "base": "builtin:xs:string",
        "facets": [
          {
            "kind": "enumeration",
            "values": ["USD", "EUR", "GBP"]
          }
        ]
      }
    },
    "edges": [
      {
        "source": "element:{urn:example:order}Order",
        "target": "complexType:{urn:example:order}OrderType",
        "relation": "element_type"
      },
      {
        "source": "complexType:{urn:example:order}OrderType",
        "target": "complexType:{urn:example:order}HeaderType",
        "relation": "child_type"
      }
    ]
  }

Important:
  - children/attributes inside complexType nodes are expected to be EFFECTIVE:
    already expanded through xs:group, xs:attributeGroup, xs:extension,
    xs:restriction, refs and anonymous types.
  - edges are optional but strongly recommended. If missing, the tool tries to
    synthesize basic edges from node.type/node.base/node.children/node.attributes.
  - For best agent experience, generate stable IDs and include source location
    or annotations if useful.

Recommended relations in edges:
  element_type
  child_type
  child_element
  attribute_type
  attribute_ref
  base
  extension
  restriction
  substitution_group
  group_ref
  attribute_group_ref
  itemType
  union_member

Output:
  All commands print JSON to stdout.
  Use --compact for single-line JSON and lower token usage.
"""

# ---------------------------------------------------------------------------
# Globals
# ---------------------------------------------------------------------------

INDEX = {}
NODES = {}
EDGES = []
OUTGOING = defaultdict(list)
INCOMING = defaultdict(list)


class ToolError(Exception):
    pass


# ---------------------------------------------------------------------------
# Small utilities
# ---------------------------------------------------------------------------

def pascal(name):
    if not name:
        return "Unknown"
    s = re.sub(r"[^0-9A-Za-z_]+", " ", str(name)).strip()
    parts = [p for p in s.split() if p]
    if not parts:
        return "Unknown"
    out = "".join(p[:1].upper() + p[1:] for p in parts)
    if out[0].isdigit():
        out = "_" + out
    if out.lower() in SCALA_KEYWORDS:
        out = out + "_"
    return out


def field_name(name):
    if not name:
        return "field"
    s = re.sub(r"[^0-9A-Za-z_]+", "_", str(name)).strip("_")
    if not s:
        s = "field"
    if s[0].isdigit():
        s = "_" + s
    if len(s) == 1:
        s = s.lower()
    else:
        # lowerCamel, but preserve ALL_CAPS-like names mostly
        if s[0].isupper() and not s.isupper():
            s = s[0].lower() + s[1:]
    if s in SCALA_KEYWORDS:
        s = "`%s`" % s
    return s


def enum_value_name(value):
    if value is None:
        return "VALUE"
    s = re.sub(r"[^0-9A-Za-z_]+", "_", str(value)).strip("_")
    if not s:
        s = "VALUE"
    if s[0].isdigit():
        s = "_" + s
    if s in SCALA_KEYWORDS:
        s = "`%s`" % s
    return s


def id_local(ident):
    if not ident:
        return ""
    if ident.startswith("anonymous:"):
        return ident.split(":")[-1]
    if ":" in ident:
        rest = ident.split(":", 1)[1]
        if rest.startswith("builtin:"):
            rest = rest[len("builtin:"):]
        if "}" in rest:
            return rest.split("}", 1)[1]
        if ":" in rest:
            return rest.split(":", 1)[1]
        return rest
    return ident


def builtin_local(ident):
    if not ident:
        return ""
    if ident.startswith("builtin:"):
        ident = ident[len("builtin:"):]
    if ident.startswith("{"):
        ident = ident.split("}", 1)[1]
    if ":" in ident:
        ident = ident.split(":", 1)[1]
    return ident


def normalize_type_ref(ref):
    if not ref:
        return ref
    if ref.startswith("builtin:"):
        return ref
    if ref.startswith("{http://www.w3.org/2001/XMLSchema}"):
        return "builtin:xs:" + ref.split("}", 1)[1]
    if ref.startswith("xsd:") or ref.startswith("xs:"):
        return "builtin:xs:" + ref.split(":", 1)[1]
    return ref


def parse_occurs(value, default=1):
    if value is None:
        return default
    if isinstance(value, int):
        return value
    s = str(value).strip()
    if s == "unbounded":
        return "unbounded"
    try:
        return int(s)
    except Exception:
        return default


def is_many(max_occurs):
    m = parse_occurs(max_occurs, 1)
    return m == "unbounded" or (isinstance(m, int) and m > 1)


def emit(payload, args):
    compact = bool(getattr(args, "compact", False))
    if compact:
        print(json.dumps(payload, ensure_ascii=False, separators=(",", ":")))
    else:
        print(json.dumps(payload, ensure_ascii=False, indent=2))


def node_summary(node_or_id):
    if isinstance(node_or_id, dict):
        node = node_or_id
    else:
        node = NODES.get(node_or_id) or {"id": node_or_id}
    out = {}
    for key in ("id", "kind", "name", "namespace", "abstract"):
        if node.get(key) is not None:
            out[key] = node[key]
    return out


# ---------------------------------------------------------------------------
# Index loading
# ---------------------------------------------------------------------------

def find_index_path(spec):
    if spec:
        if os.path.isfile(spec):
            return spec
        if os.path.isdir(spec):
            for name in ("xsd-index.json", "index.json"):
                path = os.path.join(spec, name)
                if os.path.isfile(path):
                    return path
        raise ToolError("Index not found: %s" % spec)

    for cand in DEFAULT_INDEX_CANDIDATES:
        if os.path.isfile(cand):
            return cand
        if os.path.isdir(cand):
            for name in ("xsd-index.json", "index.json"):
                path = os.path.join(cand, name)
                if os.path.isfile(path):
                    return path

    raise ToolError(
        "Index not found. Provide --index path/to/xsd-index.json or create one of: "
        + ", ".join(DEFAULT_INDEX_CANDIDATES)
    )


def load_index(spec):
    global INDEX, NODES, EDGES, OUTGOING, INCOMING

    path = find_index_path(spec)
    with open(path, "r", encoding="utf-8") as fh:
        data = json.load(fh)

    if not isinstance(data, dict):
        raise ToolError("Index root must be a JSON object")

    INDEX = data
    INDEX["__file__"] = path

    nodes = data.get("nodes", {})
    if isinstance(nodes, list):
        tmp = {}
        for n in nodes:
            if isinstance(n, dict) and n.get("id"):
                tmp[n["id"]] = n
        nodes = tmp

    if not isinstance(nodes, dict):
        raise ToolError("Index 'nodes' must be an object or array")

    for nid, node in nodes.items():
        if isinstance(node, dict):
            node.setdefault("id", nid)

    NODES = nodes

    edge_set = set()
    edges = []

    def add_edge(source, target, relation, extra=None):
        if not source or not target:
            return
        key = (source, target, relation)
        if key in edge_set:
            return
        edge_set.add(key)
        edge = {
            "source": source,
            "target": target,
            "relation": relation,
        }
        if isinstance(extra, dict) and extra:
            edge.update(extra)
        edges.append(edge)

    # Explicit edges from index
    for e in data.get("edges", []) or []:
        if not isinstance(e, dict):
            continue
        source = e.get("source")
        target = e.get("target")
        relation = e.get("relation", "related")
        if source and target:
            extra = {k: v for k, v in e.items() if k not in ("source", "target", "relation")}
            add_edge(source, target, relation, extra)

    # Synthetic edges from node content, so used-by works even if edges are incomplete.
    for node in NODES.values():
        if not isinstance(node, dict):
            continue

        nid = node.get("id")
        kind = node.get("kind")

        typ = node.get("type")
        if typ:
            typ = normalize_type_ref(typ)
            if kind == "element":
                add_edge(nid, typ, "element_type")
            elif kind == "attribute":
                add_edge(nid, typ, "attribute_type")
            else:
                add_edge(nid, typ, "type")

        base = node.get("base")
        if base:
            add_edge(nid, normalize_type_ref(base), "base")

        sg = node.get("substitutionGroup")
        if sg:
            add_edge(nid, sg, "substitution_group")

        item_type = node.get("itemType")
        if item_type:
            add_edge(nid, normalize_type_ref(item_type), "itemType")

        for member in node.get("memberTypes", []) or []:
            add_edge(nid, normalize_type_ref(member), "union_member")

        for child in node.get("children", []) or []:
            if not isinstance(child, dict):
                continue
            if child.get("node"):
                add_edge(nid, child["node"], "child_element")
            if child.get("type"):
                add_edge(nid, normalize_type_ref(child["type"]), "child_type")

        for attr in node.get("attributes", []) or []:
            if not isinstance(attr, dict):
                continue
            if attr.get("node"):
                add_edge(nid, attr["node"], "attribute_ref")
            if attr.get("type"):
                add_edge(nid, normalize_type_ref(attr["type"]), "attribute_type")

    EDGES = edges
    OUTGOING = defaultdict(list)
    INCOMING = defaultdict(list)

    for e in EDGES:
        OUTGOING[e["source"]].append(e)
        INCOMING[e["target"]].append(e)


# ---------------------------------------------------------------------------
# Resolution helpers
# ---------------------------------------------------------------------------

def find_candidates(ref, kind=None):
    if not ref:
        return []

    if ref in NODES:
        return [ref]

    norm = normalize_type_ref(ref)
    if norm != ref and norm in NODES:
        return [norm]

    candidates = []
    ref_l = str(ref).lower()

    for nid, node in NODES.items():
        if kind and node.get("kind") != kind:
            continue

        name = node.get("name") or ""
        qname = node.get("qname") or ""

        if ref == nid or ref == name or ref == qname:
            candidates.append(nid)
            continue

        if nid.endswith("}" + ref) or nid.endswith(":" + ref):
            candidates.append(nid)
            continue

        if name and name.lower() == ref_l:
            candidates.append(nid)

    seen = set()
    out = []
    for c in candidates:
        if c not in seen:
            seen.add(c)
            out.append(c)
    return out


def resolve_id(ref, kind=None, allow_missing=False):
    if not ref:
        raise ToolError("Empty ID/reference")

    if ref in NODES:
        return ref

    norm = normalize_type_ref(ref)
    if norm != ref and norm in NODES:
        return norm

    candidates = find_candidates(ref, kind=kind)
    if len(candidates) == 1:
        return candidates[0]

    if len(candidates) > 1:
        raise ToolError({
            "error": "ambiguous reference",
            "ref": ref,
            "candidates": candidates[:25],
        })

    if allow_missing:
        if norm.startswith("builtin:") or ref.startswith("builtin:") or ref.startswith("xs:") or ref.startswith("xsd:"):
            return norm
        return ref

    raise ToolError({
        "error": "not found",
        "ref": ref,
        "hint": "Use 'xsdq search <pattern>' to find IDs.",
    })


def get_node(ref, kind=None):
    rid = resolve_id(ref, kind=kind, allow_missing=True)
    node = NODES.get(rid)
    if node:
        return node

    if rid.startswith("builtin:"):
        return {
            "id": rid,
            "kind": "builtin",
            "name": builtin_local(rid),
            "namespace": "http://www.w3.org/2001/XMLSchema",
        }

    if rid.startswith("xs:") or rid.startswith("xsd:"):
        norm = normalize_type_ref(rid)
        return {
            "id": norm,
            "kind": "builtin",
            "name": builtin_local(norm),
            "namespace": "http://www.w3.org/2001/XMLSchema",
        }

    raise ToolError({
        "error": "node not found",
        "id": rid,
        "hint": "If this is a built-in XSD type, use builtin:xs:<type>.",
    })


def resolve_content_node(node):
    """
    Given an element or type node, return the node that contains effective
    children/attributes. Usually this is the element's complexType.
    """
    if not isinstance(node, dict):
        raise ToolError("Invalid node")

    kind = node.get("kind")

    if kind == "element":
        typ = normalize_type_ref(node.get("type"))
        tnode = NODES.get(typ) if typ else None
        if tnode:
            return tnode
        # Anonymous or inline content fallback
        if node.get("children") or node.get("attributes"):
            return node
        return {
            "id": node.get("id"),
            "kind": "complexType",
            "name": node.get("name"),
            "children": [],
            "attributes": [],
        }

    if kind in ("complexType", "element"):
        return node

    return node


def has_substitution_members(node_id):
    for e in INCOMING.get(node_id, []):
        if e.get("relation") == "substitution_group":
            return True
    return False


# ---------------------------------------------------------------------------
# Scala hint helpers
# ---------------------------------------------------------------------------

def builtin_scala(type_id):
    local = builtin_local(type_id)
    return BUILTIN_SCALA.get(local, "String")


def scala_for_type(type_id):
    notes = []
    if not type_id:
        return "Any", ["missing type"]

    type_id = normalize_type_ref(type_id)

    if type_id.startswith("builtin:"):
        return builtin_scala(type_id), notes

    node = NODES.get(type_id)
    if not node:
        return pascal(id_local(type_id)), ["type not in index: %s" % type_id]

    kind = node.get("kind")

    if kind == "simpleType":
        values = []
        for f in node.get("facets", []) or []:
            if not isinstance(f, dict):
                continue
            if f.get("kind") == "enumeration":
                vals = f.get("values", []) or []
                if isinstance(vals, list):
                    values.extend(vals)
            elif "enumeration" in f:
                vals = f.get("enumeration")
                if isinstance(vals, list):
                    values.extend(vals)
                elif vals is not None:
                    values.append(vals)

        if values or node.get("values"):
            return pascal(node.get("name") or id_local(type_id)), notes

        if node.get("itemType"):
            item, item_notes = scala_for_type(node.get("itemType"))
            notes.extend(item_notes)
            return "List[%s]" % item, notes

        if node.get("memberTypes"):
            return pascal(node.get("name") or id_local(type_id)), notes + [
                "simpleType union; consider sealed trait or custom codec"
            ]

        base = node.get("base")
        if base:
            base_scala, base_notes = scala_for_type(base)
            notes.extend(base_notes)
            if node.get("facets"):
                return pascal(node.get("name") or id_local(type_id)), notes + [
                    "restricted simpleType; consider opaque type/value class with validation"
                ]
            return base_scala, notes

        return "String", notes + ["simpleType without base; using String"]

    if kind == "complexType":
        return pascal(node.get("name") or id_local(type_id)), notes

    if kind == "element":
        t = node.get("type")
        if t:
            return scala_for_type(t)
        return pascal(node.get("name") or id_local(type_id)), notes

    if kind == "builtin":
        return builtin_scala(type_id), notes

    return pascal(node.get("name") or id_local(type_id)), notes


def wrap_cardinality(base_type, min_occurs, max_occurs, nillable=False, inside_choice=False):
    if is_many(max_occurs):
        return "List[%s]" % base_type

    minv = parse_occurs(min_occurs, 1)
    optional = (
        (isinstance(minv, int) and minv == 0)
        or bool(nillable)
        or bool(inside_choice)
    )

    if optional:
        return "Option[%s]" % base_type
    return base_type


def attribute_field_hint(attr):
    if attr.get("use") == "prohibited":
        return None

    if attr.get("wildcard"):
        return {
            "name": "extraAttributes",
            "scalaType": "Map[String, String]",
            "required": False,
            "attribute": True,
            "wildcard": True,
            "notes": ["xs:anyAttribute wildcard"],
        }

    name = field_name(attr.get("name") or id_local(attr.get("node") or "attr"))
    base, notes = scala_for_type(attr.get("type"))

    use = attr.get("use", "optional")
    required = use == "required"
    scala_type = base if required else "Option[%s]" % base

    hint = {
        "name": name,
        "xsdName": attr.get("name"),
        "scalaType": scala_type,
        "required": required,
        "attribute": True,
    }

    if attr.get("default") is not None:
        hint["default"] = attr.get("default")
        hint.setdefault("notes", []).append("has XSD default value")

    if attr.get("fixed") is not None:
        hint["fixed"] = attr.get("fixed")
        hint.setdefault("notes", []).append("has XSD fixed value")

    if notes:
        hint.setdefault("notes", []).extend(notes)

    return hint


def child_field_hint(child):
    if child.get("wildcard"):
        many = is_many(child.get("maxOccurs", 1))
        base = "Any"
        scala_type = "List[Any]" if many else "Option[Any]"
        return {
            "name": "any",
            "scalaType": scala_type,
            "required": False,
            "many": many,
            "wildcard": True,
            "notes": ["xs:any wildcard; consider extensible representation"],
        }

    name_source = child.get("name") or id_local(child.get("node") or child.get("type") or "field")
    name = field_name(name_source)

    type_id = child.get("type")
    if not type_id and child.get("node"):
        el = NODES.get(child.get("node"))
        if isinstance(el, dict):
            type_id = el.get("type")

    base, notes = scala_for_type(type_id)

    inside_choice = bool(child.get("choiceId") or child.get("container") == "choice")
    scala_type = wrap_cardinality(
        base,
        child.get("minOccurs", 1),
        child.get("maxOccurs", 1),
        nillable=bool(child.get("nillable", False)),
        inside_choice=inside_choice,
    )

    minv = parse_occurs(child.get("minOccurs", 1), 1)
    required = (
        not inside_choice
        and isinstance(minv, int)
        and minv >= 1
        and not is_many(child.get("maxOccurs", 1))
        and not child.get("nillable", False)
    )

    hint = {
        "name": name,
        "xsdName": child.get("name"),
        "scalaType": scala_type,
        "required": required,
        "many": is_many(child.get("maxOccurs", 1)),
    }

    if child.get("node"):
        hint["element"] = child.get("node")
    if child.get("type"):
        hint["type"] = normalize_type_ref(child.get("type"))
    if child.get("container"):
        hint["container"] = child.get("container")
    if child.get("choiceId"):
        hint["choiceId"] = child.get("choiceId")
        hint.setdefault("notes", []).append(
            "part of xs:choice; often better modeled as sealed trait/coproduct"
        )

    if notes:
        hint.setdefault("notes", []).extend(notes)

    return hint


def scala_hint(ref, depth=0, visited=None):
    if visited is None:
        visited = set()

    rid = resolve_id(ref, allow_missing=True)

    if rid in visited:
        return {"id": rid, "recursive": True}

    node = NODES.get(rid)
    if not node:
        scala_type, notes = scala_for_type(rid)
        return {
            "id": rid,
            "missing": True,
            "scalaType": scala_type,
            "notes": notes,
        }

    visited = visited | {rid}
    kind = node.get("kind")

    if kind == "element":
        type_id = normalize_type_ref(node.get("type"))
        base, notes = scala_for_type(type_id)

        if node.get("abstract") or has_substitution_members(rid):
            scala_kind = "sealedTrait"
        else:
            tnode = NODES.get(type_id) if type_id else None
            if isinstance(tnode, dict) and tnode.get("kind") == "complexType":
                scala_kind = "caseClass"
            else:
                scala_kind = "value"

        hint = {
            "id": rid,
            "kind": "element",
            "scalaKind": scala_kind,
            "name": pascal(node.get("name") or id_local(rid)),
            "scalaType": base,
        }

        if node.get("abstract"):
            hint["abstract"] = True
            hint.setdefault("notes", []).append("abstract element; consider sealed trait")

        if has_substitution_members(rid):
            hint.setdefault("notes", []).append(
                "has substitutionGroup members; consider sealed trait hierarchy"
            )

        if notes:
            hint.setdefault("notes", []).extend(notes)

        if depth > 0 and type_id and not type_id.startswith("builtin:"):
            hint["typeHint"] = scala_hint(type_id, depth - 1, visited)

        return hint

    if kind == "complexType":
        fields = []
        notes = []

        if node.get("mixed"):
            notes.append("mixed content; Scala model may need special representation")

        if node.get("base"):
            base_id = normalize_type_ref(node.get("base"))
            base_node = NODES.get(base_id)
            if base_node and base_node.get("kind") == "complexType":
                extends_name = pascal(base_node.get("name") or id_local(base_id))
            else:
                extends_name = pascal(id_local(base_id))
        else:
            extends_name = None

        for attr in node.get("attributes", []) or []:
            if not isinstance(attr, dict):
                continue
            h = attribute_field_hint(attr)
            if h:
                fields.append(h)

        choice_ids = set()
        for child in node.get("children", []) or []:
            if not isinstance(child, dict):
                continue
            if child.get("choiceId"):
                choice_ids.add(child.get("choiceId"))
            h = child_field_hint(child)
            if h:
                fields.append(h)

        if choice_ids:
            notes.append(
                "contains xs:choice groups: "
                + ", ".join(sorted(str(x) for x in choice_ids))
                + "; consider sealed trait/coproduct instead of optional fields"
            )

        if any(c.get("wildcard") for c in node.get("children", []) or [] if isinstance(c, dict)):
            notes.append("contains xs:any wildcard")

        if any(a.get("wildcard") for a in node.get("attributes", []) or [] if isinstance(a, dict)):
            notes.append("contains xs:anyAttribute wildcard")

        scala_kind = "sealedTrait" if node.get("abstract") else "caseClass"

        hint = {
            "id": rid,
            "kind": "complexType",
            "scalaKind": scala_kind,
            "name": pascal(node.get("name") or id_local(rid)),
            "fields": fields,
        }

        if extends_name:
            hint["extends"] = extends_name

        if node.get("abstract"):
            hint["abstract"] = True

        if notes:
            hint["notes"] = notes

        if depth > 0:
            nested = []
            seen_nested = set()

            def maybe_nested(type_ref):
                if not type_ref:
                    return
                tid = normalize_type_ref(type_ref)
                if tid.startswith("builtin:"):
                    return
                if tid in seen_nested or tid in visited:
                    return
                seen_nested.add(tid)
                nested.append(scala_hint(tid, depth - 1, visited))

            for attr in node.get("attributes", []) or []:
                if isinstance(attr, dict):
                    maybe_nested(attr.get("type"))

            for child in node.get("children", []) or []:
                if not isinstance(child, dict):
                    continue
                maybe_nested(child.get("type"))
                if child.get("node"):
                    el = NODES.get(child.get("node"))
                    if isinstance(el, dict):
                        maybe_nested(el.get("type"))

            if nested:
                hint["nested"] = nested

        return hint

    if kind == "simpleType":
        name = pascal(node.get("name") or id_local(rid))

        values = []
        for f in node.get("facets", []) or []:
            if not isinstance(f, dict):
                continue
            if f.get("kind") == "enumeration":
                vals = f.get("values", []) or []
                if isinstance(vals, list):
                    values.extend(vals)
            elif "enumeration" in f:
                vals = f.get("enumeration")
                if isinstance(vals, list):
                    values.extend(vals)
                elif vals is not None:
                    values.append(vals)

        if node.get("values"):
            vals = node.get("values")
            if isinstance(vals, list):
                values.extend(vals)

        if values:
            return {
                "id": rid,
                "kind": "simpleType",
                "scalaKind": "enum",
                "name": name,
                "values": values,
                "scalaValues": [enum_value_name(v) for v in values],
                "notes": ["Scala 3 enum is usually a good fit"],
            }

        if node.get("itemType"):
            item, notes = scala_for_type(node.get("itemType"))
            return {
                "id": rid,
                "kind": "simpleType",
                "scalaKind": "typeAlias",
                "name": name,
                "scalaType": "List[%s]" % item,
                "notes": notes + ["xs:list simpleType"],
            }

        if node.get("memberTypes"):
            members = []
            notes = []
            for m in node.get("memberTypes", []) or []:
                member_type, member_notes = scala_for_type(m)
                members.append(member_type)
                notes.extend(member_notes)
            return {
                "id": rid,
                "kind": "simpleType",
                "scalaKind": "sealedTrait",
                "name": name,
                "members": members,
                "notes": notes + ["xs:union simpleType; consider sealed trait or custom codec"],
            }

        base = node.get("base")
        base_scala, notes = scala_for_type(base)

        if node.get("facets"):
            return {
                "id": rid,
                "kind": "simpleType",
                "scalaKind": "opaqueType",
                "name": name,
                "underlying": base_scala,
                "facets": node.get("facets", []),
                "notes": notes + [
                    "restricted simpleType; consider Scala 3 opaque type with smart constructor/validation"
                ],
            }

        return {
            "id": rid,
            "kind": "simpleType",
            "scalaKind": "typeAlias",
            "name": name,
            "scalaType": base_scala,
            "notes": notes,
        }

    if kind == "attribute":
        h = attribute_field_hint(node)
        if h:
            h.update({
                "id": rid,
                "kind": "attribute",
            })
            return h
        return {
            "id": rid,
            "kind": "attribute",
            "prohibited": True,
        }

    return {
        "id": rid,
        "kind": kind,
        "name": pascal(node.get("name") or id_local(rid)),
    }


# ---------------------------------------------------------------------------
# Commands
# ---------------------------------------------------------------------------

def cmd_summary(args):
    counts = defaultdict(int)
    for node in NODES.values():
        counts[node.get("kind", "unknown")] += 1

    meta = INDEX.get("meta", {})
    roots = meta.get("roots") or INDEX.get("roots") or []

    if not roots:
        elements = [nid for nid, node in NODES.items() if node.get("kind") == "element"]
        child_element_targets = {
            e.get("target")
            for e in EDGES
            if e.get("relation") == "child_element"
        }
        roots = [nid for nid in elements if nid not in child_element_targets][:50]

    payload = {
        "indexFile": INDEX.get("__file__"),
        "meta": meta,
        "counts": counts,
        "rootCandidates": roots,
        "commands": [
            "summary",
            "list",
            "search",
            "get",
            "type",
            "children",
            "attrs",
            "used-by",
            "uses",
            "hierarchy",
            "bundle",
            "scala-hints",
        ],
    }
    emit(payload, args)


def cmd_list(args):
    pattern = None
    if args.match:
        flags = 0 if args.case_sensitive else re.IGNORECASE
        pattern = re.compile(args.match, flags)

    items = []
    for nid, node in NODES.items():
        if args.kind and node.get("kind") != args.kind:
            continue
        if args.namespace and node.get("namespace") != args.namespace:
            continue
        if pattern:
            text = " ".join(
                str(x)
                for x in (
                    nid,
                    node.get("name"),
                    node.get("qname"),
                    node.get("namespace"),
                    node.get("annotation"),
                )
                if x is not None
            )
            if not pattern.search(text):
                continue

        if args.full:
            items.append(node)
        else:
            items.append(node_summary(node))

    items.sort(key=lambda x: x.get("id") or "")
    total = len(items)
    items = items[: args.limit]

    emit({
        "count": total,
        "returned": len(items),
        "items": items,
    }, args)


def cmd_search(args):
    flags = 0 if args.case_sensitive else re.IGNORECASE
    pattern = re.compile(args.pattern, flags)

    items = []
    for nid, node in NODES.items():
        if args.kind and node.get("kind") != args.kind:
            continue

        text = " ".join(
            str(x)
            for x in (
                nid,
                node.get("name"),
                node.get("qname"),
                node.get("namespace"),
                node.get("annotation"),
            )
            if x is not None
        )

        if pattern.search(text):
            if args.full:
                items.append(node)
            else:
                items.append(node_summary(node))

    items.sort(key=lambda x: x.get("id") or "")
    total = len(items)
    items = items[: args.limit]

    emit({
        "pattern": args.pattern,
        "count": total,
        "returned": len(items),
        "items": items,
    }, args)


def cmd_get(args):
    node = get_node(args.id, kind=args.kind)
    payload = {"node": node}

    if node.get("kind") == "element" and node.get("type"):
        type_id = normalize_type_ref(node.get("type"))
        tnode = NODES.get(type_id)
        if tnode:
            payload["resolvedType"] = tnode
            if tnode.get("kind") == "complexType":
                payload["effectiveChildrenCount"] = len(tnode.get("children", []) or [])
                payload["effectiveAttributesCount"] = len(tnode.get("attributes", []) or [])
        else:
            payload["resolvedType"] = {"id": type_id, "missing": True}

    elif node.get("kind") == "complexType":
        payload["effectiveChildrenCount"] = len(node.get("children", []) or [])
        payload["effectiveAttributesCount"] = len(node.get("attributes", []) or [])

    out_edges = OUTGOING.get(node.get("id"), [])
    in_edges = INCOMING.get(node.get("id"), [])

    payload["outgoingCount"] = len(out_edges)
    payload["incomingCount"] = len(in_edges)

    payload["outgoing"] = [
        {
            "relation": e.get("relation"),
            "target": e.get("target"),
            "targetNode": node_summary(e.get("target")),
        }
        for e in out_edges[: args.limit]
    ]

    payload["incoming"] = [
        {
            "relation": e.get("relation"),
            "source": e.get("source"),
            "sourceNode": node_summary(e.get("source")),
        }
        for e in in_edges[: args.limit]
    ]

    emit(payload, args)


def cmd_type(args):
    node = get_node(args.id, kind=args.kind)

    if node.get("kind") in ("element", "attribute"):
        type_id = normalize_type_ref(node.get("type"))
    elif node.get("kind") in ("complexType", "simpleType", "builtin"):
        type_id = node.get("id")
    else:
        type_id = normalize_type_ref(node.get("type"))

    type_node = NODES.get(type_id) if type_id else None

    payload = {
        "source": node.get("id"),
        "sourceKind": node.get("kind"),
        "typeId": type_id,
    }

    if type_node:
        payload["typeNode"] = type_node
    else:
        scala_type, notes = scala_for_type(type_id)
        payload["typeNode"] = {
            "id": type_id,
            "missing": True,
            "scalaType": scala_type,
            "notes": notes,
        }

    emit(payload, args)


def child_tree(child, depth, visited, limit):
    out = dict(child)

    if depth <= 0:
        return out

    type_id = child.get("type")
    if not type_id and child.get("node"):
        el = NODES.get(child.get("node"))
        if isinstance(el, dict):
            type_id = el.get("type")

    type_id = normalize_type_ref(type_id) if type_id else None
    tnode = NODES.get(type_id) if type_id else None

    if tnode and tnode.get("kind") in ("complexType", "element") and tnode.get("id") not in visited:
        sub_children = tnode.get("children", []) or []
        if sub_children:
            out["children"] = [
                child_tree(c, depth - 1, visited | {tnode.get("id")}, limit)
                for c in sub_children[:limit]
                if isinstance(c, dict)
            ]
        out["typeNode"] = node_summary(tnode)
    elif type_id:
        out["typeNode"] = node_summary(type_id)

    return out


def cmd_children(args):
    node = get_node(args.id, kind=args.kind)
    content = resolve_content_node(node)

    children = [c for c in (content.get("children", []) or []) if isinstance(c, dict)]
    total = len(children)

    if args.depth <= 1:
        result = children[: args.limit]
    else:
        visited = {content.get("id")}
        result = [
            child_tree(c, args.depth - 1, visited, args.limit)
            for c in children[: args.limit]
        ]

    emit({
        "node": content.get("id"),
        "total": total,
        "returned": len(result),
        "children": result,
    }, args)


def cmd_attrs(args):
    node = get_node(args.id, kind=args.kind)
    content = resolve_content_node(node)

    attrs = [a for a in (content.get("attributes", []) or []) if isinstance(a, dict)]
    total = len(attrs)
    result = attrs[: args.limit]

    emit({
        "node": content.get("id"),
        "total": total,
        "returned": len(result),
        "attributes": result,
    }, args)


def cmd_used_by(args):
    target = resolve_id(args.id, kind=args.kind, allow_missing=True)
    edges = INCOMING.get(target, [])

    if args.relation:
        edges = [e for e in edges if e.get("relation") == args.relation]

    total = len(edges)
    items = [
        {
            "source": e.get("source"),
            "relation": e.get("relation"),
            "sourceNode": node_summary(e.get("source")),
        }
        for e in edges[: args.limit]
    ]

    emit({
        "target": target,
        "count": total,
        "returned": len(items),
        "usedBy": items,
    }, args)


def cmd_uses(args):
    source = resolve_id(args.id, kind=args.kind, allow_missing=True)
    edges = OUTGOING.get(source, [])

    if args.relation:
        edges = [e for e in edges if e.get("relation") == args.relation]

    total = len(edges)
    items = [
        {
            "target": e.get("target"),
            "relation": e.get("relation"),
            "targetNode": node_summary(e.get("target")),
        }
        for e in edges[: args.limit]
    ]

    emit({
        "source": source,
        "count": total,
        "returned": len(items),
        "uses": items,
    }, args)


def cmd_hierarchy(args):
    node = get_node(args.id, kind=args.kind)
    node_id = node.get("id")

    bases = []
    seen_bases = set()
    cur = node.get("base")

    while cur:
        cur = normalize_type_ref(cur)
        if cur in seen_bases:
            break
        seen_bases.add(cur)
        bases.append(node_summary(cur))
        cur = NODES.get(cur, {}).get("base")

    derived = []
    derived_seen = set()
    stack = [node_id]

    while stack and len(derived) < args.limit:
        current = stack.pop()
        for e in INCOMING.get(current, []):
            if e.get("relation") in ("base", "extension", "restriction"):
                source = e.get("source")
                if source and source not in derived_seen:
                    derived_seen.add(source)
                    derived.append(node_summary(source))
                    stack.append(source)
                    if len(derived) >= args.limit:
                        break

    substitution_members = []
    for e in INCOMING.get(node_id, []):
        if e.get("relation") == "substitution_group":
            substitution_members.append(node_summary(e.get("source")))
            if len(substitution_members) >= args.limit:
                break

    emit({
        "node": node_summary(node),
        "bases": bases,
        "derivedCount": len(derived),
        "derived": derived,
        "substitutionMembers": substitution_members,
    }, args)


def cmd_bundle(args):
    root_node = get_node(args.id, kind=args.kind)
    root_id = root_node.get("id")

    queue = deque([(root_id, 0)])
    seen_ids = set()
    collected_nodes = []
    missing = []

    while queue and len(collected_nodes) < args.max_nodes:
        nid, d = queue.popleft()
        nid = normalize_type_ref(nid)

        if nid in seen_ids:
            continue
        seen_ids.add(nid)

        node = NODES.get(nid)
        if node:
            collected_nodes.append(node)
        else:
            missing.append(nid)
            continue

        if d >= args.depth:
            continue

        # Outgoing structural relations
        for e in OUTGOING.get(nid, []):
            rel = e.get("relation")
            target = e.get("target")
            if rel in TRAVERSE_OUT and target:
                queue.append((target, d + 1))

        # For abstract/root nodes, include direct subtypes/substitution members.
        if node.get("abstract") or d == 0:
            for e in INCOMING.get(nid, []):
                rel = e.get("relation")
                source = e.get("source")
                if rel in ("base", "substitution_group") and source:
                    queue.append((source, d + 1))

    collected_ids = {n.get("id") for n in collected_nodes}
    collected_edges = [
        e for e in EDGES
        if e.get("source") in collected_ids and e.get("target") in collected_ids
    ]

    warnings = []
    if len(collected_nodes) >= args.max_nodes and queue:
        warnings.append("max-nodes limit reached; bundle may be incomplete")

    payload = {
        "root": root_id,
        "depth": args.depth,
        "nodeCount": len(collected_nodes),
        "edgeCount": len(collected_edges),
    }

    if args.brief:
        payload["nodes"] = [node_summary(n) for n in collected_nodes]
    else:
        payload["nodes"] = collected_nodes

    payload["edges"] = collected_edges

    if missing:
        payload["missing"] = missing[:100]

    if warnings:
        payload["warnings"] = warnings

    if args.scala:
        payload["scalaHints"] = [
            scala_hint(n.get("id"), depth=0, visited=set())
            for n in collected_nodes
        ]

    emit(payload, args)


def cmd_scala_hints(args):
    node = get_node(args.id, kind=args.kind)
    hint = scala_hint(node.get("id"), depth=args.depth, visited=set())
    emit(hint, args)


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

def make_parser():
    parser = argparse.ArgumentParser(
        prog="xsdq",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        description=(
            "xsdq: query a prebuilt XSD navigation index.\n"
            "Designed for AI agents generating Scala 3 domain models from XSD."
        ),
        epilog=HELP_EPILOG,
    )

    parser.add_argument(
        "--index",
        default=None,
        help="Path to index JSON file or directory containing xsd-index.json/index.json.",
    )
    parser.add_argument(
        "--compact",
        action="store_true",
        default=False,
        help="Print compact single-line JSON.",
    )

    common = argparse.ArgumentParser(add_help=False)
    common.add_argument(
        "--index",
        default=argparse.SUPPRESS,
        help="Path to index JSON file or directory.",
    )
    common.add_argument(
        "--compact",
        action="store_true",
        default=argparse.SUPPRESS,
        help="Print compact single-line JSON.",
    )

    sub = parser.add_subparsers(dest="command", metavar="COMMAND")

    def add_limit(sp, default=100):
        sp.add_argument(
            "--limit",
            type=int,
            default=default,
            help="Maximum number of returned items (default: %(default)s).",
        )

    # summary
    sp = sub.add_parser(
        "summary",
        parents=[common],
        help="Show index meta, counts and root candidates.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sp.set_defaults(func=cmd_summary)

    # list
    sp = sub.add_parser(
        "list",
        parents=[common],
        help="List nodes by kind/namespace/regex.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sp.add_argument("--kind", help="Filter by node kind, e.g. element, complexType.")
    sp.add_argument("--namespace", help="Filter by exact namespace.")
    sp.add_argument("--match", help="Regex to search in id/name/namespace/annotation.")
    sp.add_argument("--case-sensitive", action="store_true", help="Case-sensitive regex.")
    sp.add_argument("--full", action="store_true", help="Return full node objects.")
    add_limit(sp, 100)
    sp.set_defaults(func=cmd_list)

    # search
    sp = sub.add_parser(
        "search",
        parents=[common],
        help="Search nodes by regex.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sp.add_argument("pattern", help="Regex pattern.")
    sp.add_argument("--kind", help="Filter by node kind.")
    sp.add_argument("--full", action="store_true", help="Return full node objects.")
    sp.add_argument("--case-sensitive", action="store_true", help="Case-sensitive search.")
    add_limit(sp, 50)
    sp.set_defaults(func=cmd_search)

    # get
    sp = sub.add_parser(
        "get",
        parents=[common],
        help="Get one node plus direct relations.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sp.add_argument("id", help="Node ID or resolvable name.")
    sp.add_argument("--kind", help="Expected node kind for disambiguation.")
    add_limit(sp, 50)
    sp.set_defaults(func=cmd_get)

    # type
    sp = sub.add_parser(
        "type",
        parents=[common],
        help="Resolve type of an element/attribute or return type node itself.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sp.add_argument("id", help="Node ID or resolvable name.")
    sp.add_argument("--kind", help="Expected node kind for disambiguation.")
    sp.set_defaults(func=cmd_type)

    # children
    sp = sub.add_parser(
        "children",
        parents=[common],
        help="Show effective children of element/complexType.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sp.add_argument("id", help="Element or complexType ID/name.")
    sp.add_argument("--kind", help="Expected node kind for disambiguation.")
    sp.add_argument("--depth", type=int, default=1, help="Recursive depth (default: 1).")
    add_limit(sp, 200)
    sp.set_defaults(func=cmd_children)

    # attrs
    sp = sub.add_parser(
        "attrs",
        parents=[common],
        help="Show effective attributes of element/complexType.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sp.add_argument("id", help="Element or complexType ID/name.")
    sp.add_argument("--kind", help="Expected node kind for disambiguation.")
    add_limit(sp, 200)
    sp.set_defaults(func=cmd_attrs)

    # used-by
    sp = sub.add_parser(
        "used-by",
        parents=[common],
        help="Find incoming references/usages of a node.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sp.add_argument("id", help="Target node ID/name.")
    sp.add_argument("--kind", help="Expected node kind for disambiguation.")
    sp.add_argument("--relation", help="Filter by relation, e.g. child_type.")
    add_limit(sp, 100)
    sp.set_defaults(func=cmd_used_by)

    # uses
    sp = sub.add_parser(
        "uses",
        parents=[common],
        help="Show outgoing references from a node.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sp.add_argument("id", help="Source node ID/name.")
    sp.add_argument("--kind", help="Expected node kind for disambiguation.")
    sp.add_argument("--relation", help="Filter by relation.")
    add_limit(sp, 100)
    sp.set_defaults(func=cmd_uses)

    # hierarchy
    sp = sub.add_parser(
        "hierarchy",
        parents=[common],
        help="Show base chain, derived types and substitution members.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sp.add_argument("id", help="Node ID/name.")
    sp.add_argument("--kind", help="Expected node kind for disambiguation.")
    add_limit(sp, 100)
    sp.set_defaults(func=cmd_hierarchy)

    # bundle
    sp = sub.add_parser(
        "bundle",
        parents=[common],
        help="Collect a context subgraph around a node for code generation.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sp.add_argument("id", help="Root node ID/name.")
    sp.add_argument("--kind", help="Expected root node kind for disambiguation.")
    sp.add_argument("--depth", type=int, default=2, help="Traversal depth (default: 2).")
    sp.add_argument("--max-nodes", type=int, default=200, help="Max nodes (default: 200).")
    sp.add_argument("--brief", action="store_true", help="Return node summaries instead of full nodes.")
    sp.add_argument("--scala", action="store_true", help="Include Scala 3 hints for collected nodes.")
    sp.set_defaults(func=cmd_bundle)

    # scala-hints
    sp = sub.add_parser(
        "scala-hints",
        parents=[common],
        help="Generate Scala 3 implementation hint for one node.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sp.add_argument("id", help="Node ID/name.")
    sp.add_argument("--kind", help="Expected node kind for disambiguation.")
    sp.add_argument("--depth", type=int, default=0, help="Include nested type hints to this depth.")
    sp.set_defaults(func=cmd_scala_hints)

    return parser


def main(argv=None):
    parser = make_parser()
    args = parser.parse_args(argv)

    if not getattr(args, "command", None):
        parser.print_help()
        return 0

    try:
        index_spec = getattr(args, "index", None)
        load_index(index_spec)
        args.func(args)
        return 0

    except ToolError as exc:
        payload = {"error": exc.args[0] if exc.args else str(exc)}
        print(json.dumps(payload, ensure_ascii=False, indent=2), file=sys.stderr)
        return 2

    except BrokenPipeError:
        return 0

    except Exception as exc:
        payload = {
            "error": "unexpected error",
            "type": type(exc).__name__,
            "message": str(exc),
        }
        print(json.dumps(payload, ensure_ascii=False, indent=2), file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())