package xjdf4s.core

import cats.Show

/** Stage 02 checks for the core part of the validation DSL. */
object ValidationDslCoreChecks:
  val accumulatingVector: Unit =
    assert(Vector.empty[ValidationError].toValidatedNel.isValid)
    val invalid = Vector(ValidationError.EmptyValue("field")).toValidatedNel
    assert(invalid.isInvalid)
    assert(invalid.toEither.isLeft)

  val validationOutcome: Unit =
    assert(ValidationOutcome(Vector.empty, Vector.empty).isValid)
    val warned = ValidationOutcome(Vector.empty, Vector(Warning("deprecated", "use the new API")))
    assert(warned.isValid)
    val failed = ValidationOutcome(Vector(ValidationError.EmptyValue("field")), Vector.empty)
    assert(!failed.isValid)
    assert(Show[Warning].show(Warning("code", "message")) == "[code] message")
end ValidationDslCoreChecks
