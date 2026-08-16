package xjdf4s.core

object CardinalityChecks:
  val nonEmptyConstruction: Unit =
    assert(NonEmptyVector.from(Vector.empty[Int]).isLeft)
    assert(NonEmptyVector(1, 2, 3).toVector == Vector(1, 2, 3))

  val refinedValues: Unit =
    assert(Nmtoken.from("Job-42").isRight)
    assert(Nmtoken.from("not a token").isLeft)
    assert(XsdId.from("42-invalid").isLeft)
end CardinalityChecks
