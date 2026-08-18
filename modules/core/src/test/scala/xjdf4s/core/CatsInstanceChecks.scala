package xjdf4s.core

import cats.{Eq, Hash, Show}
import cats.syntax.semigroup.*

/** Hand-rolled smoke checks of the cats instances (stage 01): Monoid laws for Extensions, Eq/Hash agreement and
 *  Show rendering. The full law suite via cats-laws + discipline is planned for stage 08.
 */
object CatsInstanceChecks:
  private val key = QualifiedName("urn:vendor:ns", "weight")
  private val foreignName = ForeignQName.from("urn:vendor:ns", "Note").toOption.get

  val extensionMonoidLaws: Unit =
    val a = Extensions(attributes = Map(key -> ExtensionValue.Number(BigDecimal(1))))
    val b = Extensions(
      attributes = Map(key -> ExtensionValue.Number(BigDecimal(2))),
      elements = Vector(ExtensionElement(foreignName)),
    )
    val c =
      Extensions(attributes = Map(QualifiedName("urn:vendor:ns", "count") -> ExtensionValue.Number(BigDecimal(3))))
    assert((a |+| Extensions.empty) == a)
    assert((Extensions.empty |+| a) == a)
    assert(((a |+| b) |+| c) == (a |+| (b |+| c)))
    assert((a |+| b).attributes(key) == ExtensionValue.Number(BigDecimal(2))) // right-biased merge
    assert((a |+| b).elements.size == 1)

  val eqShowHashSmoke: Unit =
    val x = Nmtoken.from("Job-42").toOption.get
    val y = Nmtoken.from("Job-42").toOption.get
    val z = Nmtoken.from("Other").toOption.get
    assert(Eq[Nmtoken].eqv(x, y))
    assert(!Eq[Nmtoken].eqv(x, z))
    assert(Hash[Nmtoken].hash(x) == Hash[Nmtoken].hash(y))
    assert(Show[Nmtoken].show(x) == "Job-42")
    val q1 = QualifiedName("urn:vendor:ns", "a")
    val q2 = QualifiedName("urn:vendor:ns", "a")
    val q3 = QualifiedName("urn:vendor:ns", "b")
    assert(Eq[QualifiedName].eqv(q1, q1))
    assert(Eq[QualifiedName].eqv(q1, q2))
    assert(!Eq[QualifiedName].eqv(q2, q3))
    assert(Show[ValidationError].show(ValidationError.EmptyValue("field")).nonEmpty)
    assert(Show[ForeignQName].show(foreignName).contains("Note"))
end CatsInstanceChecks
