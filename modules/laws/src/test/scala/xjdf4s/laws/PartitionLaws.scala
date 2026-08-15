package xjdf4s.laws

import xjdf4s.laws.Arbitraries.given
import xjdf4s.model.*
import xjdf4s.prim.*
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

/** The selection semantics of §6.1.3.2 “Selecting a Partition”, and the
 *  overlay/merge algebra of `Part`, as properties.
 */
class PartitionLaws extends ScalaCheckSuite:

  property("Part.empty applies to the whole ResourceSet — matches everything"):
    forAll { (selector: Part) => Part.empty.matches(selector) }

  property("a Part matches itself"):
    forAll { (p: Part) => p.matches(p) }

  property("a Part with a superset of keys matches the same selector"):
    forAll { (p: Part, extra: Part) =>
      if p.conflictingKeys(extra).isEmpty then Part.combine(p, extra).matches(p)
      else true
    }

  property("right-biased overlay always matches the right side"):
    forAll { (left: Part, right: Part) =>
      Part.combine(left, right).matches(right)
    }

  property("conflict-free merge matches both sides"):
    forAll { (a: Part, b: Part) =>
      a.mergeWith(b) match
        case Left(conflicts) => conflicts == a.conflictingKeys(b)
        case Right(merged) => merged.matches(a) && merged.matches(b)
    }

  property("merging a Part with itself never conflicts"):
    forAll { (p: Part) => p.mergeWith(p).isRight }

  property("runtime-tagged accessor round-trips NMTOKEN values"):
    forAll { (name: NmToken) =>
      Part.token(PartitionKey.SheetName, name).valueOf(PartitionKey.SheetName).contains(PartitionValue.Token(name))
    }

  property("runtime-tagged accessor round-trips range values"):
    forAll { (r: IntegerRange) =>
      Part.range(PartitionKey.DocIndex, r).valueOf(PartitionKey.DocIndex).contains(PartitionValue.Range(r))
    }

  property("typed constructor bySide carries the Side enum"):
    Part.bySide(Side.Front).valueOf(PartitionKey.Side).contains(PartitionValue.BySide(Side.Front))

  property("the ValueOf match type agrees with the typed fields at the type level"):
    // `sheetName` is Option[NmToken] == Option[ValueOf[PartitionKey.SheetName.type]]:
    // the match type is used here as a *type-level* witness, since GADT
    // refinement of abstract keys is not available (see Partition.scala).
    val sheet: Option[ValueOf[PartitionKey.SheetName.type]] = Part.sheetName("S1").flatMap(_.sheetName)
    sheet.contains(NmToken.unsafe("S1"))

  property("selection: a Resource without parts applies to the entire set"):
    forAll { (selector: Part) =>
      val resource =
        Resource(specific = xjdf4s.resources.ResourcePayload.Foreign(NsPrefix.unsafe("foo"), NmToken.unsafe("Bar")))
      resource.matches(selector)
    }

  property("selection: §6.1.3.2 iterates top to bottom and takes the first match"):
    val s1 = Part.sheetName("S1").get
    val s2 = Part.sheetName("S2").get
    val payload = xjdf4s.resources.ResourcePayload.Foreign(NsPrefix.unsafe("foo"), NmToken.unsafe("Bar"))
    val r1 = Resource(specific = payload, parts = cats.data.Chain.one(s1))
    val r2 = Resource(specific = payload, parts = cats.data.Chain.one(s2))
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("foo:Bar"),
      resources = cats.data.Chain(r1, r2)
    )
    rs.select(Part.sheetName("S2").get) == Some(r2) &&
    rs.select(Part.empty) == Some(r1)

  property("selection: IDREF selection ignores Part elements"):
    val id = Id.unsafe("r_1")
    val payload = xjdf4s.resources.ResourcePayload.Foreign(NsPrefix.unsafe("foo"), NmToken.unsafe("Bar"))
    val r = Resource(specific = payload, id = Some(id), parts = cats.data.Chain.one(Part.sheetName("S9").get))
    val rs = ResourceSet(
      name = ResourceSetName.unsafe("foo:Bar"),
      resources = cats.data.Chain.one(r)
    )
    rs.byId(id) == Some(r) &&
    rs.select(Part.sheetName("S9").get) == Some(r) &&
    rs.select(Part.sheetName("other").get) == None
end PartitionLaws
