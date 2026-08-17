package xjdf4s.codec.xml

import xjdf4s.codec.xml.domain.given
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * Round-trip law for the special hand codecs and for the generic derivation: `decode(encode(value)) == value`
 * across intents, audits, derived resources and derived messages.
 */
object DerivedRoundTripChecks:
  private def roundTrip[A: XmlElementCodec](value: A): A =
    val codec = summon[XmlElementCodec[A]]
    val decoded = codec.decode(codec.encode(value))
    assert(decoded.isRight, decoded.left.toOption.map(_.toString).getOrElse(""))
    decoded.toOption.get

  private val jobId = Nmtoken.from("job-1").toOption.get
  private val process = Nmtoken.from("Product").toOption.get
  private val mediaName = Nmtoken.from("Media").toOption.get
  private val deviceId = Nmtoken.from("device-1").toOption.get
  private val time = XsdDateTime.from("2026-08-17T12:00:00+03:00").toOption.get
  private val header = Header(deviceId, time)

  val intents: Unit =
    assert(roundTrip(MediaIntent(MediaType.Paper)) == MediaIntent(MediaType.Paper))
    assert(roundTrip(LaminatingIntent(LaminatedSurfaces.Front)) == LaminatingIntent(LaminatedSurfaces.Front))
    assert(roundTrip(LayoutIntent(pages = EvenPageCount.from(8).toOption)) == LayoutIntent(pages = EvenPageCount.from(8).toOption))
    val colorIntent = ColorIntent(ColorSurfaces.Both(SurfaceColor(), SurfaceColor(colorsUsed = Vector(Nmtoken.from("Cyan").toOption.get))))
    assert(roundTrip(colorIntent) == colorIntent)

  val bindingIntent: Unit =
    val value = BindingIntent(
      BindingSpecification.HardCover(Some(HardCoverBindingDetails(headBands = Some(true)))),
      bindingSide = Some(BindingEdge.Top),
    )
    assert(roundTrip(value) == value)
    val stitched = BindingIntent(BindingSpecification.SideStitch(Some(StitchingDetails(stapleShape = Some(StapleShape.Crown)))))
    assert(roundTrip(stitched) == stitched)

  val fileSpecRoles: Unit =
    val device = Device(
      deviceId,
      schemas = DeviceSchemas(current = Some(FileSpec(location = FileLocation.Url(UriRef.from("https://example.org/schema.xsd").toOption.get)))),
      modules = Vector(DeviceModule(Nmtoken.from("module-1").toOption.get)),
    )
    // The schema FileSpecs acquire their @ResourceUsage role on the wire (CurrentSchema/Schema); the round-trip
    // comparison therefore checks the location and the role instead of full equality with the role-less input.
    val decodedDevice = roundTrip(device)
    assert(decodedDevice.schemas.current.exists(_.resourceUsage.exists(_.value == "CurrentSchema")))
    assert(decodedDevice.schemas.current.exists(_.location.isInstanceOf[FileLocation.Url]))
    assert(decodedDevice.modules.nonEmpty)
    val verification = VerificationResult(files = VerificationFiles(accepted = Some(FileSpec())))
    val decodedVerification = roundTrip(verification)
    assert(decodedVerification.files.accepted.exists(_.resourceUsage.exists(_.value == "Accepted")))
    assert(decodedVerification.files.accepted.exists(_.location == FileLocation.Pipe))

  val tiffAndPatch: Unit =
    val tag = TiffTag(tagNumber = 270, tagType = 2, value = Some(TiffTagValue.Text("description")))
    assert(roundTrip(tag) == tag)
    val patch = Patch(PatchUsage.Color, spotType = Some(SpotType.Emulated), neutralDensity = NeutralDensity.from(0.5f).toOption)
    assert(roundTrip(patch) == patch)

  val derivedResources: Unit =
    assert(roundTrip(RunList(pages = Some(IntegerRange(0, 9)))) == RunList(pages = Some(IntegerRange(0, 9))))
    assert(roundTrip(RegisterMark(center = Some(XYPair(1, 2)))) == RegisterMark(center = Some(XYPair(1, 2))))
    val assembly = Assembly(AssemblyPlan.Listed(NonEmptyVector.one(AssemblySection(Nmtoken.from("sig-1").toOption.get))))
    assert(roundTrip(assembly) == assembly)
    val delivery = DeliveryParams(files = DeliveryFiles(mailingList = Some(FileSpec())))
    val decodedDelivery = roundTrip(delivery)
    assert(decodedDelivery.files.mailingList.exists(_.resourceUsage.exists(_.value == "MailingList")))
    assert(decodedDelivery.files.mailingList.exists(_.location == FileLocation.Pipe))

  val specialMessages: Unit =
    val priority = Priority0To100.from(42).toOption.get
    val modify = ModifyQueueEntryParams(
      QueueModification.Move(Some(QueueMoveTarget.Priority(priority))),
      QueueFilter(queueEntryIds = Vector(Nmtoken.from("entry-1").toOption.get)),
    )
    assert(roundTrip(modify) == modify)
    val signalStatus = SignalStatus(header, DeviceInfo(DeviceStatus.Idle), channelMode = Some(ChannelMode.Reliable))
    assert(roundTrip(signalStatus) == signalStatus)

  val derivedMessages: Unit =
    val knownDevices = QueryKnownDevices(header, languages = Vector(LanguageTag.from("en-US").toOption.get))
    assert(roundTrip(knownDevices) == knownDevices)
    val notification = SignalNotification(header, Notification(Severity.Event))
    assert(roundTrip(notification) == notification)
    val knownSubs = ResponseKnownSubscriptions(
      header,
      subscriptions = Vector(SubscriptionInfo(Nmtoken.from("Q1").toOption.get, Nmtoken.from("SignalResource").toOption.get, Subscription(UriRef.from("https://example.org/sub").toOption.get))),
    )
    assert(roundTrip(knownSubs) == knownSubs)

  val fullDocument: Unit =
    val audit = AuditCreated(header)
    val product = Product(
      id = XsdId.from("product-1").toOption,
      intents = Vector(Intent(Nmtoken.from("Binding").toOption.get, Some(BindingIntent(BindingSpecification.Tape)))),
    )
    val document = XJDF(
      jobId,
      NonEmptyVector.one(process),
      auditPool = Some(AuditPool(Vector(audit))),
      productList = Some(ProductList(NonEmptyVector.one(product))),
      resourceSets = Vector(ResourceSet(mediaName)),
      version = Some(Version.V2_2),
    )
    val decoded = roundTrip(document)
    assert(decoded.auditPool.nonEmpty)
    assert(decoded.productList.nonEmpty)
    assert(decoded.resourceSets.size == 1)

  val fullXjmf: Unit =
    val xjmf = XJMF(header, NonEmptyVector.one(QueryKnownMessages(header)), version = Some(Version.V2_2))
    val decoded = roundTrip(xjmf)
    assert(decoded.messages.toVector.head.isInstanceOf[QueryKnownMessages])
end DerivedRoundTripChecks
