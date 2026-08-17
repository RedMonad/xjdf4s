package xjdf4s.core

object PrimitiveValueChecks:
  val idRefLexicalSpace: Unit =
    assert(XsdIdRef.from("media-1").isRight)
    assert(XsdIdRef.from("42-invalid").isLeft)
    assert(XsdIdRef.from(" ").isLeft)

  val xjdfStringNormalization: Unit =
    assert(XjdfString.from("plain text").isRight)
    assert(XjdfString.from("a" * 1023).isRight)
    assert(XjdfString.from("a" * 1024).isLeft)
    assert(XjdfString.from("tab\tseparated").isLeft)
    assert(XjdfString.from("line\nfeed").isLeft)

  val priorityBounds: Unit =
    assert(Priority0To100.from(0).isRight)
    assert(Priority0To100.from(100).isRight)
    assert(Priority0To100.from(101).isLeft)
    assert(Priority0To100.from(-1).isLeft)

  val dateTimeRequiresZoneAndCalendar: Unit =
    assert(XsdDateTime.from("2026-08-17T12:00:00+03:00").isRight)
    assert(XsdDateTime.from("2026-08-17T12:00:00Z").isRight)
    assert(XsdDateTime.from("2026-08-17T12:00:00").isLeft)
    assert(XsdDateTime.from("2026-02-30T12:00:00+03:00").isLeft)
    assert(XsdDateTime.from("2026-13-01T12:00:00+03:00").isLeft)
    assert(XsdDateTime.from("2026-08-17T25:00:00+03:00").isLeft)
    assert(XsdDateTime.from("2024-02-29T12:00:00+03:00").isRight)
    assert(XsdDateTime.from("2025-02-29T12:00:00+03:00").isLeft)

  val durationRequiresAtLeastOneComponent: Unit =
    assert(XsdDuration.from("PT1H").isRight)
    assert(XsdDuration.from("P1Y2M3DT4H5M6.5S").isRight)
    assert(XsdDuration.from("-P1D").isRight)
    assert(XsdDuration.from("P").isLeft)
    assert(XsdDuration.from("-P").isLeft)
    assert(XsdDuration.from("PT").isLeft)
    assert(XsdDuration.from("P1YT").isLeft)
    assert(XsdDuration.from("P1.5Y").isLeft)

  val foreignNamesRejectStandardNamespace: Unit =
    assert(ForeignQName.from("urn:vendor:ns", "CustomIntent").isRight)
    assert(ForeignQName.from(XjdfNamespace.uri, "MediaIntent").isLeft)
    assert(ForeignQName.from("", "CustomIntent").isLeft)

  val extensionContentPreservesOrder: Unit =
    val name = ForeignQName.from("urn:vendor:ns", "Note").toOption.get
    val element = ExtensionElement.text(name, "ordered")
    val content = Vector(
      ExtensionContent.Text("before "),
      ExtensionContent.Element(element),
      ExtensionContent.Text(" after"),
    )
    assert(ExtensionElement(name, content = content).content == content)
end PrimitiveValueChecks
