#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
xsdgen: generate JSON navigation index for xsdq from XSD schemas.

This tool parses XSD files and produces an index suitable for xsdq.
It is intentionally pragmatic: it covers common XSD 1.0 patterns used
for domain model generation:

  - global elements / complexTypes / simpleTypes / attributes
  - groups and attributeGroups
  - element/attribute refs
  - extension / restriction
  - anonymous types
  - substitutionGroup
  - simpleType enumeration / list / union / facets
  - effective children and attributes for complexTypes

It is not a full XSD validator. For very exotic XSD features, review
the generated index manually.

Dependency:
  pip install lxml

Examples:
  xsdgen schema.xsd --out xsd-index.json
  xsdgen src/xsd/main.xsd src/xsd/common.xsd --out .xsd-index/xsd-index.json --pretty
"""

import argparse
import json
import os
import sys
from collections import defaultdict
from datetime import datetime, timezone

try:
    from lxml import etree
except ImportError:
    sys.stderr.write(
        "lxml is required for xsdgen.\n"
        "Install it with:\n"
        "  python -m pip install lxml\n"
    )
    sys.exit(1)


XSD_NS = "http://www.w3.org/2001/XMLSchema"

GLOBAL_KINDS = (
    "element",
    "complexType",
    "simpleType",
    "attribute",
    "group",
    "attributeGroup",
)

# ---------------------------------------------------------------------------
# Global state
# ---------------------------------------------------------------------------

NODES = {}
EDGES = []
EDGE_SET = set()

SCHEMAS = []
LOADED = set()

GLOBAL_MAPS = {kind: {} for kind in GLOBAL_KINDS}
LOCAL_MAPS = {kind: defaultdict(list) for kind in GLOBAL_KINDS}

COMPLEX_ELEMS = {}
GROUP_ELEMS = {}
ATTR_GROUP_ELEMS = {}
ELEMENT_ELEMS = {}

ELEMENT_TYPE_CACHE = {}
ATTRIBUTE_TYPE_CACHE = {}

COMPLEX_CACHE = {}
IN_PROGRESS = set()


# ---------------------------------------------------------------------------
# Utilities
# ---------------------------------------------------------------------------

class GenError(Exception):
    pass


def is_xsd(elem):
    return isinstance(elem.tag, str) and etree.QName(elem).namespace == XSD_NS


def localname(elem):
    return etree.QName(elem).localname


def find_xsd_child(elem, tag):
    for child in elem:
        if is_xsd(child) and localname(child) == tag:
            return child
    return None


def elem_path(elem):
    try:
        return elem.getroottree().getpath(elem)
    except Exception:
        try:
            return "line-%s" % elem.sourceline
        except Exception:
            return "unknown"


def elem_url(elem):
    try:
        return elem.getroottree().docinfo.URL or "schema.xsd"
    except Exception:
        return "schema.xsd"


def source_loc(elem, url=None):
    if url is None:
        url = elem_url(elem)
    base = os.path.basename(url) if url else "schema.xsd"
    try:
        line = elem.sourceline
    except Exception:
        line = None
    if line:
        return "%s:%s" % (base, line)
    return base


def annotation_text(elem):
    ann = find_xsd_child(elem, "annotation")
    if ann is None:
        return None

    docs = []
    for child in ann:
        if is_xsd(child) and localname(child) == "documentation":
            text = (child.text or "").strip()
            if text:
                docs.append(text)

    if not docs:
        return None
    return " ".join(docs)


def id_local(ident):
    if not ident:
        return ""
    if ":" in ident:
        rest = ident.split(":", 1)[1]
        if "}" in rest:
            return rest.split("}", 1)[1]
        if ":" in rest:
            return rest.split(":", 1)[1]
        return rest
    return ident


def make_id(kind, ns, name):
    if ns == XSD_NS:
        return "builtin:xs:%s" % name
    if ns:
        return "%s:{%s}%s" % (kind, ns, name)
    return "%s:%s" % (kind, name)


def add_node(node):
    nid = node.get("id")
    if not nid:
        return

    if nid not in NODES:
        NODES[nid] = node
        return

    existing = NODES[nid]
    for key, value in node.items():
        if key not in existing:
            existing[key] = value
        elif existing[key] in (None, [], {}) and value not in (None, [], {}):
            existing[key] = value


def add_edge(source, target, relation):
    if not source or not target:
        return

    key = (source, target, relation)
    if key in EDGE_SET:
        return

    EDGE_SET.add(key)
    EDGES.append(
        {
            "source": source,
            "target": target,
            "relation": relation,
        }
    )


def parse_qname(value, elem):
    if not value:
        return None, None

    value = value.strip()
    if ":" in value:
        prefix, local = value.split(":", 1)
        ns = elem.nsmap.get(prefix)
        if ns is None and prefix in ("xs", "xsd"):
            ns = XSD_NS
        return ns, local

    return None, value


def find_global_key(kind, ns, local, target_ns=None):
    candidates = []

    if ns is not None:
        candidates.append((ns, local))
    else:
        candidates.append((None, local))
        if target_ns is not None:
            candidates.append((target_ns, local))

    for cand in candidates:
        if cand in GLOBAL_MAPS[kind]:
            return cand

    matches = LOCAL_MAPS[kind].get(local, [])
    if len(matches) == 1:
        return matches[0]

    if len(matches) > 1:
        if target_ns is not None:
            for m in matches:
                if m[0] == target_ns:
                    return m
        return matches[0]

    return None


def find_type_key(ns, local, target_ns=None):
    for kind in ("complexType", "simpleType"):
        key = find_global_key(kind, ns, local, target_ns)
        if key:
            return kind, key
    return None, None


def resolve_type_ref(value, elem, target_ns):
    if not value:
        return None

    ns, local = parse_qname(value, elem)

    if ns == XSD_NS:
        return "builtin:xs:%s" % local

    kind, key = find_type_key(ns, local, target_ns)
    if key:
        return make_id(kind, key[0], key[1])

    fallback_ns = ns if ns is not None else target_ns
    return make_id("complexType", fallback_ns, local)


def resolve_global_ref(kind, value, elem, target_ns):
    if not value:
        return None

    ns, local = parse_qname(value, elem)
    key = find_global_key(kind, ns, local, target_ns)

    if key:
        return make_id(kind, key[0], key[1])

    fallback_ns = ns if ns is not None else target_ns
    return make_id(kind, fallback_ns, local)


def anon_id(kind, elem):
    url = elem_url(elem)
    return "%s:anonymous:%s#%s" % (kind, os.path.basename(url), elem_path(elem))


def derive_anon_name(elem, suffix="Type"):
    parent = elem.getparent()
    if parent is not None and is_xsd(parent):
        tag = localname(parent)
        if tag in ("element", "attribute"):
            val = parent.get("name") or parent.get("ref")
            if val:
                return id_local(val) + suffix
    return None


# ---------------------------------------------------------------------------
# Schema loading
# ---------------------------------------------------------------------------

def load_schema(path, override_tns=None):
    abs_path = os.path.abspath(path)

    if not os.path.isfile(abs_path):
        sys.stderr.write("WARNING: schema file not found: %s\n" % abs_path)
        return

    key = (abs_path, override_tns or "")
    if key in LOADED:
        return

    LOADED.add(key)

    try:
        tree = etree.parse(abs_path)
    except Exception as exc:
        sys.stderr.write("WARNING: cannot parse %s: %s\n" % (abs_path, exc))
        return

    root = tree.getroot()

    if not (
        isinstance(root.tag, str)
        and etree.QName(root).namespace == XSD_NS
        and localname(root) == "schema"
    ):
        sys.stderr.write("WARNING: not an XSD schema root: %s\n" % abs_path)
        return

    tns = root.get("targetNamespace")
    if override_tns and not tns:
        tns = override_tns

    SCHEMAS.append((root, tns, abs_path))

    # Collect global declarations.
    for child in root:
        if not is_xsd(child):
            continue

        tag = localname(child)
        name = child.get("name")

        if tag in GLOBAL_MAPS and name:
            gkey = (tns, name)
            if gkey not in GLOBAL_MAPS[tag]:
                GLOBAL_MAPS[tag][gkey] = (child, tns, abs_path)
                LOCAL_MAPS[tag][name].append(gkey)

    # Load imports/includes.
    base_dir = os.path.dirname(abs_path)

    for child in root:
        if not is_xsd(child):
            continue

        tag = localname(child)
        loc = child.get("schemaLocation")
        if not loc:
            continue

        next_path = os.path.normpath(os.path.join(base_dir, loc))

        if tag == "import":
            load_schema(next_path, child.get("namespace"))
        elif tag in ("include", "redefine"):
            load_schema(next_path, tns)


# ---------------------------------------------------------------------------
# Simple type generation
# ---------------------------------------------------------------------------

def add_simple_type_node(elem, tid, target_ns, name=None):
    if tid in NODES:
        return tid

    node = {
        "id": tid,
        "kind": "simpleType",
        "name": name or elem.get("name") or derive_anon_name(elem, ""),
        "namespace": target_ns,
        "source": source_loc(elem),
    }

    ann = annotation_text(elem)
    if ann:
        node["annotation"] = ann

    facets = []

    restriction = find_xsd_child(elem, "restriction")
    list_el = find_xsd_child(elem, "list")
    union_el = find_xsd_child(elem, "union")

    if restriction is not None:
        base = restriction.get("base")
        if base:
            node["base"] = resolve_type_ref(base, restriction, target_ns)

        enums = []

        for facet_elem in restriction:
            if not is_xsd(facet_elem):
                continue

            facet_tag = localname(facet_elem)

            if facet_tag == "enumeration":
                enums.append(facet_elem.get("value"))
            elif facet_tag in (
                "minInclusive",
                "minExclusive",
                "maxInclusive",
                "maxExclusive",
                "length",
                "minLength",
                "maxLength",
                "pattern",
                "totalDigits",
                "fractionDigits",
                "whiteSpace",
            ):
                facet = {"kind": facet_tag}
                if facet_elem.get("value") is not None:
                    facet["value"] = facet_elem.get("value")
                facets.append(facet)

        if enums:
            facets.insert(0, {"kind": "enumeration", "values": enums})

    if list_el is not None:
        item_type = list_el.get("itemType")
        if item_type:
            node["itemType"] = resolve_type_ref(item_type, list_el, target_ns)
        else:
            inline_st = find_xsd_child(list_el, "simpleType")
            if inline_st is not None:
                sub_tid = anon_id("simpleType", inline_st)
                add_simple_type_node(
                    inline_st,
                    sub_tid,
                    target_ns,
                    derive_anon_name(inline_st, "Item"),
                )
                node["itemType"] = sub_tid

    if union_el is not None:
        member_types = []

        member_types_attr = union_el.get("memberTypes")
        if member_types_attr:
            for token in member_types_attr.split():
                member_types.append(resolve_type_ref(token, union_el, target_ns))

        for inline_st in union_el:
            if is_xsd(inline_st) and localname(inline_st) == "simpleType":
                sub_tid = anon_id("simpleType", inline_st)
                add_simple_type_node(
                    inline_st,
                    sub_tid,
                    target_ns,
                    derive_anon_name(inline_st, "Member"),
                )
                member_types.append(sub_tid)

        if member_types:
            node["memberTypes"] = member_types

    if facets:
        node["facets"] = facets

    add_node(node)
    return tid


# ---------------------------------------------------------------------------
# Complex type shells
# ---------------------------------------------------------------------------

def add_complex_type_shell(elem, tid, target_ns, name=None):
    COMPLEX_ELEMS.setdefault(tid, (elem, target_ns))

    if tid in NODES:
        return tid

    node = {
        "id": tid,
        "kind": "complexType",
        "name": name or elem.get("name") or derive_anon_name(elem),
        "namespace": target_ns,
        "abstract": elem.get("abstract") == "true",
        "mixed": elem.get("mixed") == "true",
        "children": [],
        "attributes": [],
        "source": source_loc(elem),
    }

    ann = annotation_text(elem)
    if ann:
        node["annotation"] = ann

    add_node(node)
    return tid


# ---------------------------------------------------------------------------
# Element / attribute type resolution
# ---------------------------------------------------------------------------

def get_element_type_id(elem, target_ns):
    key = id(elem)
    if key in ELEMENT_TYPE_CACHE:
        return ELEMENT_TYPE_CACHE[key]

    type_attr = elem.get("type")

    if type_attr:
        tid = resolve_type_ref(type_attr, elem, target_ns)
    else:
        inline_ct = find_xsd_child(elem, "complexType")
        inline_st = find_xsd_child(elem, "simpleType")

        if inline_ct is not None:
            tid = anon_id("complexType", inline_ct)
            add_complex_type_shell(
                inline_ct,
                tid,
                target_ns,
                derive_anon_name(inline_ct),
            )
        elif inline_st is not None:
            tid = anon_id("simpleType", inline_st)
            add_simple_type_node(
                inline_st,
                tid,
                target_ns,
                derive_anon_name(inline_st, ""),
            )
        else:
            tid = "builtin:xs:anyType"

    ELEMENT_TYPE_CACHE[key] = tid
    return tid


def get_attribute_type_id(elem, target_ns):
    key = id(elem)
    if key in ATTRIBUTE_TYPE_CACHE:
        return ATTRIBUTE_TYPE_CACHE[key]

    type_attr = elem.get("type")

    if type_attr:
        tid = resolve_type_ref(type_attr, elem, target_ns)
    else:
        inline_st = find_xsd_child(elem, "simpleType")
        if inline_st is not None:
            tid = anon_id("simpleType", inline_st)
            add_simple_type_node(
                inline_st,
                tid,
                target_ns,
                derive_anon_name(inline_st, ""),
            )
        else:
            tid = "builtin:xs:anySimpleType"

    ATTRIBUTE_TYPE_CACHE[key] = tid
    return tid


def get_element_type_id_by_id(ref_id):
    node = NODES.get(ref_id)
    if isinstance(node, dict) and node.get("type"):
        return node["type"]

    info = ELEMENT_ELEMS.get(ref_id)
    if info:
        elem, tns = info
        return get_element_type_id(elem, tns)

    return "builtin:xs:anyType"


# ---------------------------------------------------------------------------
# Effective content processing
# ---------------------------------------------------------------------------

def merge_attributes(attrs):
    merged = {}
    order = []

    for attr in attrs:
        name = attr.get("name") or "unknown"
        if name not in merged:
            order.append(name)
        merged[name] = attr

    return [merged[name] for name in order]


def process_element_particle(el, children, target_ns, owner_id, container, choice_id):
    ref = el.get("ref")

    if not ref and not el.get("name"):
        return

    min_occurs = el.get("minOccurs", "1")
    max_occurs = el.get("maxOccurs", "1")
    nillable = el.get("nillable") == "true"

    if ref:
        ref_id = resolve_global_ref("element", ref, el, target_ns)
        add_edge(owner_id, ref_id, "child_element")

        name = NODES.get(ref_id, {}).get("name") or id_local(ref_id)
        type_id = get_element_type_id_by_id(ref_id)
        node_id = ref_id
    else:
        name = el.get("name")
        type_id = get_element_type_id(el, target_ns)
        node_id = None

    child = {
        "name": name,
        "type": type_id,
        "minOccurs": min_occurs,
        "maxOccurs": max_occurs,
        "container": container,
        "choiceId": choice_id,
        "nillable": nillable,
        "wildcard": False,
    }

    if node_id:
        child["node"] = node_id

    add_edge(owner_id, type_id, "child_type")
    children.append(child)


def process_particles(
    host,
    children,
    target_ns,
    owner_id,
    container=None,
    choice_id=None,
    visited_groups=None,
):
    if visited_groups is None:
        visited_groups = set()

    for child in host:
        if not is_xsd(child):
            continue

        tag = localname(child)

        if tag in ("sequence", "choice", "all"):
            child_container = tag
            child_choice_id = choice_id

            if tag == "choice":
                child_choice_id = "choice:" + elem_path(child)

            process_particles(
                child,
                children,
                target_ns,
                owner_id,
                child_container,
                child_choice_id,
                visited_groups,
            )

        elif tag == "group":
            ref = child.get("ref")
            if not ref:
                continue

            group_id = resolve_global_ref("group", ref, child, target_ns)
            add_edge(owner_id, group_id, "group_ref")

            if group_id in visited_groups:
                continue

            info = GROUP_ELEMS.get(group_id)
            if info:
                group_elem, group_tns = info
                visited_groups.add(group_id)
                process_particles(
                    group_elem,
                    children,
                    group_tns,
                    owner_id,
                    container or "group",
                    choice_id,
                    visited_groups,
                )
                visited_groups.discard(group_id)

        elif tag == "element":
            process_element_particle(
                child,
                children,
                target_ns,
                owner_id,
                container,
                choice_id,
            )

        elif tag == "any":
            children.append(
                {
                    "name": "any",
                    "type": "builtin:xs:anyType",
                    "minOccurs": child.get("minOccurs", "1"),
                    "maxOccurs": child.get("maxOccurs", "1"),
                    "container": container,
                    "choiceId": choice_id,
                    "wildcard": True,
                }
            )


def process_attribute_particle(el, attrs, target_ns, owner_id):
    ref = el.get("ref")

    if not ref and not el.get("name"):
        return

    if ref:
        attr_id = resolve_global_ref("attribute", ref, el, target_ns)
        add_edge(owner_id, attr_id, "attribute_ref")

        global_attr = NODES.get(attr_id, {})
        name = global_attr.get("name") or id_local(attr_id)
        type_id = global_attr.get("type") or "builtin:xs:anySimpleType"

        use = el.get("use") or "optional"
        default = el.get("default")
        if default is None:
            default = global_attr.get("default")

        fixed = el.get("fixed")
        if fixed is None:
            fixed = global_attr.get("fixed")

        attr = {
            "name": name,
            "type": type_id,
            "use": use,
            "default": default,
            "fixed": fixed,
            "wildcard": False,
            "node": attr_id,
        }
    else:
        name = el.get("name")
        type_id = get_attribute_type_id(el, target_ns)

        attr = {
            "name": name,
            "type": type_id,
            "use": el.get("use", "optional"),
            "default": el.get("default"),
            "fixed": el.get("fixed"),
            "wildcard": False,
        }

    if attr.get("type"):
        add_edge(owner_id, attr["type"], "attribute_type")

    attrs.append(attr)


def process_attributes(
    host,
    attrs,
    target_ns,
    owner_id,
    visited_attr_groups=None,
):
    if visited_attr_groups is None:
        visited_attr_groups = set()

    for child in host:
        if not is_xsd(child):
            continue

        tag = localname(child)

        if tag == "attribute":
            process_attribute_particle(child, attrs, target_ns, owner_id)

        elif tag == "attributeGroup":
            ref = child.get("ref")
            if not ref:
                continue

            ag_id = resolve_global_ref("attributeGroup", ref, child, target_ns)
            add_edge(owner_id, ag_id, "attribute_group_ref")

            if ag_id in visited_attr_groups:
                continue

            info = ATTR_GROUP_ELEMS.get(ag_id)
            if info:
                ag_elem, ag_tns = info
                visited_attr_groups.add(ag_id)
                process_attributes(
                    ag_elem,
                    attrs,
                    ag_tns,
                    owner_id,
                    visited_attr_groups,
                )
                visited_attr_groups.discard(ag_id)

        elif tag == "anyAttribute":
            attrs.append(
                {
                    "name": "anyAttribute",
                    "type": None,
                    "use": "optional",
                    "wildcard": True,
                }
            )


def safe_complex_effective(type_id):
    if not type_id or type_id.startswith("builtin:"):
        return {"children": [], "attributes": []}
    return get_complex_effective(type_id)


def get_complex_effective(type_id):
    if type_id in COMPLEX_CACHE:
        return COMPLEX_CACHE[type_id]

    if type_id in IN_PROGRESS:
        return {
            "children": [],
            "attributes": [],
            "recursive": True,
        }

    IN_PROGRESS.add(type_id)

    info = COMPLEX_ELEMS.get(type_id)

    children = []
    attributes = []
    base_id = None
    derivation = None
    simple_content = False

    if info:
        elem, target_ns = info

        complex_content = find_xsd_child(elem, "complexContent")
        simple_content_elem = find_xsd_child(elem, "simpleContent")

        if complex_content is not None:
            deriv = find_xsd_child(complex_content, "extension")
            if deriv is None:
                deriv = find_xsd_child(complex_content, "restriction")

            host = deriv if deriv is not None else complex_content

            if deriv is not None and deriv.get("base"):
                base_id = resolve_type_ref(deriv.get("base"), deriv, target_ns)
                derivation = localname(deriv)

                if derivation == "extension":
                    base_eff = safe_complex_effective(base_id)

                    for c in base_eff.get("children", []):
                        cc = dict(c)
                        cc["inherited"] = True
                        children.append(cc)

                    for a in base_eff.get("attributes", []):
                        aa = dict(a)
                        aa["inherited"] = True
                        attributes.append(aa)

            process_particles(host, children, target_ns, type_id)
            process_attributes(host, attributes, target_ns, type_id)

            if derivation == "restriction":
                if not children and base_id:
                    base_eff = safe_complex_effective(base_id)
                    for c in base_eff.get("children", []):
                        cc = dict(c)
                        cc["inherited"] = True
                        children.append(cc)

                if not attributes and base_id:
                    base_eff = safe_complex_effective(base_id)
                    for a in base_eff.get("attributes", []):
                        aa = dict(a)
                        aa["inherited"] = True
                        attributes.append(aa)

        elif simple_content_elem is not None:
            simple_content = True

            deriv = find_xsd_child(simple_content_elem, "extension")
            if deriv is None:
                deriv = find_xsd_child(simple_content_elem, "restriction")

            value_type = "builtin:xs:string"

            if deriv is not None:
                if deriv.get("base"):
                    base_id = resolve_type_ref(deriv.get("base"), deriv, target_ns)
                    derivation = localname(deriv)
                    value_type = base_id or "builtin:xs:string"

                process_attributes(deriv, attributes, target_ns, type_id)

            children.append(
                {
                    "name": "value",
                    "type": value_type,
                    "minOccurs": 1,
                    "maxOccurs": 1,
                    "container": "simpleContent",
                    "text": True,
                    "wildcard": False,
                }
            )

        else:
            process_particles(elem, children, target_ns, type_id)
            process_attributes(elem, attributes, target_ns, type_id)

    attributes = merge_attributes(attributes)

    result = {
        "children": children,
        "attributes": attributes,
        "base": base_id,
        "derivation": derivation,
        "simpleContent": simple_content,
    }

    COMPLEX_CACHE[type_id] = result
    IN_PROGRESS.discard(type_id)

    return result


# ---------------------------------------------------------------------------
# Global builders
# ---------------------------------------------------------------------------

def build_global_simple_types():
    for key, value in GLOBAL_MAPS["simpleType"].items():
        elem, tns, url = value
        tid = make_id("simpleType", key[0], key[1])
        add_simple_type_node(elem, tid, tns, key[1])


def build_global_complex_shells():
    for key, value in GLOBAL_MAPS["complexType"].items():
        elem, tns, url = value
        tid = make_id("complexType", key[0], key[1])
        add_complex_type_shell(elem, tid, tns, key[1])


def build_global_groups():
    for key, value in GLOBAL_MAPS["group"].items():
        elem, tns, url = value
        gid = make_id("group", key[0], key[1])

        node = {
            "id": gid,
            "kind": "group",
            "name": key[1],
            "namespace": key[0],
            "source": source_loc(elem, url),
        }

        ann = annotation_text(elem)
        if ann:
            node["annotation"] = ann

        add_node(node)
        GROUP_ELEMS[gid] = (elem, tns)


def build_global_attribute_groups():
    for key, value in GLOBAL_MAPS["attributeGroup"].items():
        elem, tns, url = value
        agid = make_id("attributeGroup", key[0], key[1])

        node = {
            "id": agid,
            "kind": "attributeGroup",
            "name": key[1],
            "namespace": key[0],
            "source": source_loc(elem, url),
        }

        ann = annotation_text(elem)
        if ann:
            node["annotation"] = ann

        add_node(node)
        ATTR_GROUP_ELEMS[agid] = (elem, tns)


def build_global_attributes():
    for key, value in GLOBAL_MAPS["attribute"].items():
        elem, tns, url = value
        aid = make_id("attribute", key[0], key[1])

        type_id = get_attribute_type_id(elem, tns)

        node = {
            "id": aid,
            "kind": "attribute",
            "name": key[1],
            "namespace": key[0],
            "type": type_id,
            "default": elem.get("default"),
            "fixed": elem.get("fixed"),
            "source": source_loc(elem, url),
        }

        ann = annotation_text(elem)
        if ann:
            node["annotation"] = ann

        add_node(node)


def build_global_elements():
    for key, value in GLOBAL_MAPS["element"].items():
        elem, tns, url = value
        eid = make_id("element", key[0], key[1])

        type_id = get_element_type_id(elem, tns)

        substitution_group = elem.get("substitutionGroup")
        substitution_id = None
        if substitution_group:
            substitution_id = resolve_global_ref(
                "element",
                substitution_group,
                elem,
                tns,
            )

        node = {
            "id": eid,
            "kind": "element",
            "name": key[1],
            "namespace": key[0],
            "type": type_id,
            "abstract": elem.get("abstract") == "true",
            "nillable": elem.get("nillable") == "true",
            "substitutionGroup": substitution_id,
            "source": source_loc(elem, url),
        }

        ann = annotation_text(elem)
        if ann:
            node["annotation"] = ann

        add_node(node)
        ELEMENT_ELEMS[eid] = (elem, tns)


def fill_complex_types():
    pending = set(COMPLEX_ELEMS.keys())

    while pending:
        tid = pending.pop()

        if tid in COMPLEX_CACHE:
            continue

        before = set(COMPLEX_ELEMS.keys())

        eff = get_complex_effective(tid)
        node = NODES.get(tid)

        if node:
            node["children"] = eff.get("children", [])
            node["attributes"] = eff.get("attributes", [])

            if eff.get("base"):
                node["base"] = eff["base"]

            if eff.get("derivation"):
                node["derivation"] = eff["derivation"]

            if eff.get("simpleContent"):
                node["simpleContent"] = True

        after = set(COMPLEX_ELEMS.keys())
        pending.update(after - before)


def build_edges():
    for node in NODES.values():
        nid = node.get("id")
        kind = node.get("kind")

        typ = node.get("type")
        if typ:
            if kind == "element":
                add_edge(nid, typ, "element_type")
            elif kind == "attribute":
                add_edge(nid, typ, "attribute_type")
            else:
                add_edge(nid, typ, "type")

        if node.get("base"):
            add_edge(nid, node["base"], "base")

        if node.get("substitutionGroup"):
            add_edge(nid, node["substitutionGroup"], "substitution_group")

        if node.get("itemType"):
            add_edge(nid, node["itemType"], "itemType")

        for member in node.get("memberTypes", []) or []:
            add_edge(nid, member, "union_member")

        for child in node.get("children", []) or []:
            if child.get("node"):
                add_edge(nid, child["node"], "child_element")
            if child.get("type"):
                add_edge(nid, child["type"], "child_type")

        for attr in node.get("attributes", []) or []:
            if attr.get("node"):
                add_edge(nid, attr["node"], "attribute_ref")
            if attr.get("type"):
                add_edge(nid, attr["type"], "attribute_type")


def compute_roots():
    child_element_targets = {
        e["target"]
        for e in EDGES
        if e.get("relation") == "child_element"
    }

    roots = []
    for nid, node in NODES.items():
        if node.get("kind") == "element" and nid not in child_element_targets:
            roots.append(nid)

    return sorted(roots)


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(
        prog="xsdgen",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        description=(
            "xsdgen: generate xsd-index.json for xsdq from XSD schemas.\n"
            "The generated index contains nodes, effective children/attributes,\n"
            "relations and hints needed for Scala 3 domain model generation."
        ),
        epilog="""
Examples:
  xsdgen schema.xsd --out xsd-index.json
  xsdgen schema.xsd --out .xsd-index/xsd-index.json --pretty
  xsdgen a.xsd b.xsd --out xsd-index.json

Notes:
  - Imports/includes are followed automatically.
  - If you have multiple root XSD files, pass all of them.
  - The generator supports common XSD patterns, but XSD is large.
    Always verify generated index for exotic schemas.
""",
    )

    parser.add_argument(
        "schemas",
        nargs="+",
        help="Root XSD schema files.",
    )
    parser.add_argument(
        "--out",
        default="xsd-index.json",
        help="Output index file (default: xsd-index.json).",
    )
    parser.add_argument(
        "--pretty",
        action="store_true",
        help="Pretty-print JSON. By default output is compact.",
    )
    parser.add_argument(
        "--root-limit",
        type=int,
        default=200,
        help="Maximum number of root candidates in meta.roots (default: 200).",
    )

    args = parser.parse_args()

    for schema_path in args.schemas:
        load_schema(schema_path)

    if not SCHEMAS:
        sys.stderr.write("ERROR: no schemas were loaded.\n")
        return 1

    build_global_simple_types()
    build_global_complex_shells()
    build_global_groups()
    build_global_attribute_groups()
    build_global_attributes()
    build_global_elements()

    fill_complex_types()
    build_edges()

    roots = compute_roots()

    counts = defaultdict(int)
    for node in NODES.values():
        counts[node.get("kind", "unknown")] += 1

    meta = {
        "source": args.schemas,
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "rootCandidates": roots[: args.root_limit],
        "counts": counts,
    }

    payload = {
        "meta": meta,
        "nodes": NODES,
        "edges": EDGES,
    }

    out_dir = os.path.dirname(os.path.abspath(args.out))
    if out_dir:
        os.makedirs(out_dir, exist_ok=True)

    with open(args.out, "w", encoding="utf-8") as fh:
        if args.pretty:
            json.dump(payload, fh, ensure_ascii=False, indent=2)
        else:
            json.dump(payload, fh, ensure_ascii=False, separators=(",", ":"))

    summary = {
        "out": args.out,
        "nodes": len(NODES),
        "edges": len(EDGES),
        "counts": counts,
        "roots": len(roots),
    }

    sys.stderr.write(json.dumps(summary, ensure_ascii=False, indent=2) + "\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
