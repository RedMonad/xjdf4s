package xjdf4s.model

import cats.{Eq, Hash, Show}

import xjdf4s.core.*

/** Stage 01 checks for the value-type instances of the model module. */
object ModelInstanceChecks:
  val valueTypeInstances: Unit =
    assert(Eq[IntegerRange].eqv(IntegerRange(0, 5), IntegerRange(0, 5)))
    assert(!Eq[IntegerRange].eqv(IntegerRange(0, 5), IntegerRange(1, 5)))
    assert(Show[XYPair].show(XYPair(1.0, 2.0)) == "1.0 2.0")
    val lab = LabColor.from(50.0, 0.0, 0.0).toOption.get
    assert(Hash[LabColor].hash(lab) == Hash[LabColor].hash(LabColor.from(50.0, 0.0, 0.0).toOption.get))
    assert(Show[LabColor].show(lab) == "50.0 0.0 0.0")
    assert(
      Show[ValidationError]
        .show(ValidationError.AtPath(Vector("ResourceSet[0]"), ValidationError.EmptyValue("x")))
        .contains("ResourceSet[0]"),
    )
end ModelInstanceChecks
