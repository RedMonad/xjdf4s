package xjdf4s.codec.xml

import xjdf4s.core.*
import xjdf4s.model.*

object LexicalChecks:
  val numbers: Unit =
    assert(Lexical.int("42").isRight)
    assert(Lexical.int("x").isLeft)
    assert(Lexical.float("INF").toOption.get.isPosInfinity)
    assert(Lexical.double("-INF").toOption.get.isNegInfinity)
    assert(Lexical.bool("1").toOption.contains(true))
    assert(Lexical.bool("false").toOption.contains(false))

  val lists: Unit =
    assert(Lexical.intList("0 1 2").toOption.contains(Vector(0, 1, 2)))
    assert(Lexical.nmtokens("a b").toOption.exists(_.map(_.value) == Vector("a", "b")))
    assert(Lexical.floatList("1.5 2.5").toOption.exists(_.size == 2))

  val fixedProducts: Unit =
    assert(Lexical.integerRange("3 7").toOption.contains(IntegerRange(3, 7)))
    assert(Lexical.integerRange("7 3").isLeft)
    assert(Lexical.integerRange("1 2 3").isLeft)
    assert(Lexical.xypair("1 2").toOption.contains(XYPair(1.0, 2.0)))
    assert(Lexical.matrix("1 0 0 1 0 0").toOption.contains(Matrix.identity))

  val hexBinary: Unit =
    val bytes = Lexical.hexBinary("0a0b").toOption.get
    assert(bytes == Vector(0x0a.toByte, 0x0b.toByte))
    assert(Lexical.renderHexBinary(bytes) == "0a0b")
    assert(Lexical.hexBinary("abc").isLeft)

  val enums: Unit =
    assert(Lexical.mediaType("paper").toOption.contains(MediaType.Paper))
    assert(Lexical.orientation("Rotate90").toOption.contains(Orientation.Rotate90))
    assert(Lexical.namedColor("aliceblue").toOption.contains(NamedColor.AliceBlue))
    assert(Lexical.version("2.2").toOption.contains(Version.V2_2))
    assert(Lexical.scope("Device").toOption.contains(Scope.Device))
    assert(Lexical.mediaType("NoSuchType").isLeft)
end LexicalChecks
