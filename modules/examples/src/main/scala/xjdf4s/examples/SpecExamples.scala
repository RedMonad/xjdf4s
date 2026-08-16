package xjdf4s.examples

import xjdf4s.dsl.dsl
import xjdf4s.intents.*
import xjdf4s.model.*
import xjdf4s.model.elements.{
  BarcodeDetails,
  Certification,
  Crease,
  ExtraValues,
  FileSpec,
  GangSource,
  Glue => GlueElement,
  HolePattern,
  IdentificationField,
  MetadataMap,
  MISDetails,
  Expr
}
import xjdf4s.prim.*
import xjdf4s.resources.*
import cats.Show
import cats.data.{Chain, NonEmptyChain, Validated, ValidatedNec}
import cats.syntax.all.*

/** The worked examples of the XJDF specification, expressed with the
 *  declarative DSL. Every example is a plain Scala value — building it is
 *  validation: the result is a `ValidatedNec[Issue, XJDF]`.
 */
object SpecExamples:

  /** Sequential chaining of `Validated`. `Validated` is applicative, not a
   *  monad: it deliberately has no `flatMap` (see cats, "Validated vs Either"),
   *  so sequencing is spelled out as a pattern match on the two cases — no
   *  syntax imports involved.
   */
  private def chainV[E, A, B](v: ValidatedNec[E, A])(f: A => ValidatedNec[E, B]): ValidatedNec[E, B] =
    v match
      case Validated.Valid(a) => f(a)
      case Validated.Invalid(problems) => Validated.Invalid(problems)

  /** Example 3.1: JSON-encoded XJDF — a minimal product ticket. */
  val minimalProduct: ValidatedNec[Issue, XJDF] =
    chainV(dsl.TicketDraft.of("J1", ProcessType.Product))(_.build)

  /** Example 3.4: Amounts in a Notebook — a 10-copy notebook BOM. */
  val notebook: ValidatedNec[Issue, ProductList] =
    chainV(
      dsl.intent(
        "BindingIntent",
        IntentPayload.Binding(
          BindingIntent(
            bindingType = BindingType.EdgeGluing,
            bindingSide = Some(Edge.Top),
            childRefs = Some(IdRefs.of(IdRef.unsafe("IBack"), IdRef.unsafe("IBody"), IdRef.unsafe("ICover")))
          )
        )
      )
    ) { binding =>
      chainV(dsl.product(amount = Some(10), productType = Some("Notebook"))(binding)) { root =>
        chainV(dsl.product(amount = Some(1), isRoot = false, productType = Some("FrontCover"), id = Some("ICover"))()) {
          cover =>
            chainV(dsl.product(
              amount = Some(50),
              isRoot = false,
              productType = Some("BookBlock"),
              id = Some("IBody")
            )()) { body =>
              dsl.product(amount = Some(1), isRoot = false, productType = Some("BackCover"), id = Some("IBack"))()
                .map(back => ProductList(NonEmptyChain(root, cover, body, back)))
            }
        }
      }
    }

  /** Example 3.6: ResourceSets with CombinedProcessIndex (Cutting + Folding). */
  val combinedProcesses: ValidatedNec[Issue, XJDF] =
    chainV(
      dsl.resourceSet(
        "NodeInfo",
        usage = Some(Usage.Input),
        combinedProcessIndex = Some(NonEmptyChain.one(ProcessIndex.unsafe(0)))
      )(dsl.nodeInfo(NodeInfo(start = Some(Timestamp.ofEpochSecond(1700)))))
    ) { cuttingInfo =>
      chainV(
        dsl.resourceSet(
          "NodeInfo",
          usage = Some(Usage.Input),
          combinedProcessIndex = Some(NonEmptyChain.one(ProcessIndex.unsafe(1)))
        )(dsl.nodeInfo(NodeInfo(start = Some(Timestamp.ofEpochSecond(1800)))))
      ) { foldingInfo =>
        chainV(dsl.resourceSet("CuttingParams", usage = Some(Usage.Input))(dsl.emptyResource)) { cuttingParams =>
          chainV(dsl.resourceSet("FoldingParams", usage = Some(Usage.Input))(dsl.emptyResource)) { foldingParams =>
            chainV(dsl.TicketDraft.of("CPI_Example", ProcessType.Cutting, ProcessType.Folding)) { draft =>
              draft
                .withResources(cuttingInfo, foldingInfo, cuttingParams, foldingParams)
                .build
            }
          }
        }
      }
    }

  /** Fixture (Table 8.17 / Table 6.74): a FoldingParams resource carrying a
   *  crease line. Not a numbered spec example — it exercises the `Crease`
   *  element mapping end-to-end (M1.6-2).
   */
  val creasingJob: ValidatedNec[Issue, XJDF] =
    chainV(
      dsl.resourceSet("FoldingParams", usage = Some(Usage.Input))(
        Resource.of(
          ResourcePayload.FoldingParamsResource(
            FoldingParams(
              creases = Chain.one(
                Crease(
                  depth = Some(Microns(150.0)),
                  startPosition = Some(XYPair(0.0, 0.0)),
                  workingDirection = Some(WorkingDirection.Top),
                  workingPath = Some(XYPair(595.28, 0.0))
                )
              )
            )
          )
        )
      )
    ) { foldingParams =>
      chainV(dsl.TicketDraft.of("creaseJob", ProcessType.Folding)) { draft =>
        draft.withResources(foldingParams).build
      }
    }

  /** Fixture (§8.22 / Table 8.27): source-job information for one
   *  `BinderySignature` placed on a gang form. Kept standalone on purpose —
   *  the element in its container is exercised by `gangJob` (M1.6-8).
   */
  val gangSource: ValidatedNec[Issue, GangSource] =
    GangSource(
      copies = 500L,
      jobId = JobId.unsafe("SourceJob-42"),
      binderySignatureId = Some(NmToken.unsafe("Signature-A"))
    ).validNec

  /** Fixture (§8.30 / Table 8.48): MIS accounting details for a rework caused
   *  by a damaged resource. Kept standalone on purpose — the element in its
   *  container is exercised by `gangJob` (M1.6-8).
   */
  val misDetails: ValidatedNec[Issue, MISDetails] =
    MISDetails(
      complexity = Some(UnitInterval.unsafe(0.5)),
      costType = Some(CostType.NonChargeable),
      workType = Some(WorkType.Rework),
      workTypeDetails = Some(Catalog.WorkTypeDetails.ResourceDamaged)
    ).validNec

  /** Fixture (§6.59 / Table 6.119): a gang job whose `NodeInfo` carries both
   *  child elements at once — two `GangSource` entries (`GangSource*`,
   *  Table 8.27) for the two source jobs imposed on the form, and the single
   *  `MISDetails` (`MISDetails?`, Table 8.48) that says how the work is
   *  charged. `@DueLevel` exercises the closed enumeration recovered in
   *  N-52; `@PersonalID` names a `Resource/@ExternalID`, not an `@ID`, so the
   *  ticket collects no references (M1.6-8).
   */
  val gangJob: ValidatedNec[Issue, XJDF] =
    chainV(
      dsl.resourceSet("NodeInfo", usage = Some(Usage.Input))(
        dsl.nodeInfo(
          NodeInfo(
            dueLevel = Some(DueLevel.Penalty),
            jobPriority = Some(80L),
            personalId = Some(NmToken.unsafe("Operator-7")),
            start = Some(Timestamp.ofEpochSecond(1700)),
            status = Some(Status.Waiting),
            gangSources = Chain(
              GangSource(
                copies = 500L,
                jobId = JobId.unsafe("SourceJob-42"),
                binderySignatureId = Some(NmToken.unsafe("Signature-A"))
              ),
              GangSource(copies = 250L, jobId = JobId.unsafe("SourceJob-43"))
            ),
            misDetails = Some(
              MISDetails(
                complexity = Some(UnitInterval.unsafe(0.5)),
                costType = Some(CostType.Chargeable),
                workType = Some(WorkType.Original)
              )
            )
          )
        )
      )
    ) { nodeInfoSet =>
      chainV(dsl.TicketDraft.of("gangJob", ProcessType.Cutting)) { draft =>
        draft.withResources(nodeInfoSet).build
      }
    }

  /** Fixture (§8.26 / Table 8.31, after Example 8.4): a Component carrying two
   *  `IdentificationField` marks — the EAN_13 product barcode of Example 8.4
   *  and a QR code whose matrix geometry lives in `BarcodeDetails`
   *  (Table 8.33) and whose supplemental digits live in `ExtraValues`
   *  (Table 8.34).
   *
   *  Example 8.4 itself places the field in `Content/BarcodeProductionParams`,
   *  which is not modelled; `Component/IdentificationField*` (Table 6.37) is
   *  the container wired in M1.6-6, so the fixture exercises the same element
   *  through the container the library actually has.
   *
   *  Each field specifies exactly one value source, as the SHALL of Table 8.31
   *  requires: `@Value` for the first, the pair `@ValueFormat` +
   *  `@ValueTemplate` for the second.
   */
  val barcodeJob: ValidatedNec[Issue, XJDF] =
    chainV(
      dsl.component(
        Component(
          productType = Some(Catalog.ProductType.Book),
          identificationFields = Chain(
            IdentificationField(
              encoding = Some(FieldEncoding.Barcode),
              encodingDetails = Some(Catalog.EncodingDetails.EAN_13),
              purpose = Some(FieldPurpose.Label),
              purposeDetails = Some(Catalog.PurposeDetails.ProductIdentification),
              position = Some(Face.Front),
              boundingBox = Some(Rectangle.unsafe(0.0, 0.0, 113.0, 73.5)),
              value = Some(XjdfString.unsafe("0123456789128"))
            ),
            IdentificationField(
              encoding = Some(FieldEncoding.Barcode),
              encodingDetails = Some(Catalog.EncodingDetails.QR),
              purpose = Some(FieldPurpose.Verification),
              valueFormat = Some(XjdfString.unsafe("Job_%s")),
              valueTemplate = Some(NmTokens.of(NmToken.unsafe("job"))),
              barcodeDetails = Some(
                BarcodeDetails(
                  barcodeVersion = Some(Catalog.BarcodeVersion.qr(7)),
                  errorCorrectionLevel = Some(Catalog.ErrorCorrectionLevel.QR_EC_M),
                  xCells = Some(45L),
                  yCells = Some(45L)
                )
              ),
              extraValues = Some(
                ExtraValues(Catalog.ExtraValuesUsage.Supplemental, XjdfString.unsafe("12345"))
              )
            )
          )
        )
      )
    ) { component =>
      chainV(dsl.resourceSet("Component", usage = Some(Usage.Output))(component)) { components =>
        chainV(dsl.TicketDraft.of("barcodeJob", ProcessType.Cutting)) { draft =>
          draft.withResources(components).build
        }
      }
    }

  /** Positive MetadataMap integration fixture based on Examples 8.6 and 8.7
   *  (§8.29 / Table 8.46). The RunList mapping is literal Example 8.6. The
   *  IdentificationField mapping follows Example 8.7, with the three mapping
   *  names also present in the parent template as Table 8.31 explicitly
   *  requires (ADR-0014 records the contradiction in the printed example).
   */
  val metadataMapJob: ValidatedNec[Issue, XJDF] =
    val runListMap = MetadataMap(
      name = NmToken.unsafe("Metadata"),
      valueFormat = XjdfString.unsafe("%s_%s"),
      valueTemplate = NmTokens.of(NmToken.unsafe("gender"), NmToken.unsafe("status")),
      expressions = Chain(
        Expr(NmToken.unsafe("gender"), XjdfXPath.unsafe("/doc/record/Geschlecht")),
        Expr(NmToken.unsafe("status"), XjdfXPath.unsafe("/doc/record/Status"))
      )
    )
    val identificationField = IdentificationField(
      valueFormat = Some(XjdfString.unsafe("%6s%3i%2i%s%s%s")),
      valueTemplate = Some(NmTokens.of(
        NmToken.unsafe("job"), NmToken.unsafe("doc"), NmToken.unsafe("sheet"),
        NmToken.unsafe("JobID"), NmToken.unsafe("DocIndex"), NmToken.unsafe("SheetIndex")
      )),
      metadataMaps = Chain(
        MetadataMap(NmToken.unsafe("JobID"), XjdfString.unsafe("Job_%s"), NmTokens.of(NmToken.unsafe("job"))),
        MetadataMap(
          NmToken.unsafe("DocIndex"),
          XjdfString.unsafe("%i%i"),
          NmTokens.of(NmToken.unsafe("doc"), NmToken.unsafe("doc"))
        ),
        MetadataMap(
          NmToken.unsafe("SheetIndex"),
          XjdfString.unsafe("%i%i"),
          NmTokens.of(NmToken.unsafe("sheet"), NmToken.unsafe("sheet"))
        )
      )
    )
    val runList = Resource.of(ResourcePayload.RunListResource(RunList(
      fileSpecs = Some(FileSpec.ofUrl(Url.unsafe("file://host/file/data.pdf"))),
      metadataMaps = Chain.one(runListMap)
    )))
    val component = Resource.of(ResourcePayload.ComponentResource(Component(
      identificationFields = Chain.one(identificationField)
    )))
    chainV(dsl.resourceSet("RunList", usage = Some(Usage.Input))(runList)) { runListSet =>
      chainV(dsl.resourceSet("Component", usage = Some(Usage.Input))(component)) { componentSet =>
        chainV(dsl.TicketDraft.of("metadataMapJob", ProcessType.Cutting)) { draft =>
          draft.withResources(runListSet, componentSet).build
        }
      }
    }

  /** Example 8.15 (Table 8.29): a binding ticket demonstrating the `Glue`
   *  element with `@GlueType="Removable"` inside `AdhesiveNote`.
   */
  val gluingJob: ValidatedNec[Issue, XJDF] =
    chainV(dsl.TicketDraft.of("glueJob", ProcessType.Binding)) { draft =>
      val binding = BindingIntent(
        bindingType = BindingType.AdhesiveNote,
        details = Some(AdhesiveNote(
          glue = Some(GlueElement(
            areaGlue = Some(true),
            glueType = Some(GlueType.Removable)
          ))
        ))
      )
      val payload = IntentPayload.Binding(binding)
      val intent = Intent(
        name = IntentName.of(payload.elementName),
        specific = payload
      )
      // Add a product with the binding intent
      val product = Product(
        id = Some(Id.unsafe("P1")),
        isRoot = true,
        amount = Some(100L),
        intents = Chain.one(intent)
      )
      draft
        .withProductList(ProductList(products = NonEmptyChain.one(product)))
        .build
    }

  /** Fixture (Table 8.30 / Appendix F): a binding ticket demonstrating the
   *  `HolePattern` element (M1.6-5). The pattern uses a predefined catalog
   *  value `R2m-DIN` (Appendix F) and is carried by `LooseBinding`
   *  (Table 4.12) — a container that already exists in the model.
   */
  val holePunchingJob: ValidatedNec[Issue, XJDF] =
    chainV(dsl.TicketDraft.of("holeJob", ProcessType.Binding)) { draft =>
      val holePattern = HolePattern(
        pattern = Some(Catalog.HolePattern.R2mDIN),
        referenceEdge = Some(HoleReferenceEdge.Left),
        shape = Some(HoleShape.Round),
        center = Some(XYPair(0.0, 0.0)),
        extent = Some(XYPair(6.0, 6.0))
      )
      val binding = BindingIntent(
        bindingType = BindingType.RingBinding,
        details = Some(LooseBinding(
          brand = Some(XjdfString.unsafe("RingBinder-A")),
          holePattern = Some(holePattern)
        ))
      )
      val payload = IntentPayload.Binding(binding)
      val intent = Intent(
        name = IntentName.of(payload.elementName),
        specific = payload
      )
      val product = Product(
        id = Some(Id.unsafe("P1")),
        isRoot = true,
        amount = Some(50L),
        intents = Chain.one(intent)
      )
      draft
        .withProductList(ProductList(products = NonEmptyChain.one(product)))
        .build
    }

  /** Fixture (Table 4.29 / Table 8.30 / Appendix F): a hole making ticket
   *  demonstrating the `HoleMakingIntent` (M1.6-12). The intent carries a
   *  `NonEmptyChain` of `HolePattern` elements (cardinality `+`); the first
   *  uses a predefined catalog value `R4m-DIN-A4` (Appendix F), the second
   *  specifies its geometry explicitly instead of a catalog pattern.
   */
  val holeMakingJob: ValidatedNec[Issue, XJDF] =
    chainV(dsl.TicketDraft.of("holeMakingJob", ProcessType.HoleMaking)) { draft =>
      val patterns = NonEmptyChain(
        HolePattern(
          pattern = Some(Catalog.HolePattern.R4mDINA4),
          referenceEdge = Some(HoleReferenceEdge.Left)
        ),
        HolePattern(
          center = Some(XYPair(0.0, 0.0)),
          extent = Some(XYPair(6.0, 6.0)),
          shape = Some(HoleShape.Round)
        )
      )
      val payload = IntentPayload.HoleMaking(HoleMakingIntent(patterns))
      val intent = Intent(name = IntentName.of(payload.elementName), specific = payload)
      val product = Product(
        id = Some(Id.unsafe("P1")),
        isRoot = true,
        amount = Some(20L),
        intents = Chain.one(intent)
      )
      draft
        .withProductList(ProductList(products = NonEmptyChain.one(product)))
        .build
    }

  /** Fixture (§4.9 / Table 4.30): a two-sided hot laminating intent using a
   *  recommended texture from Table A.80 and a 25 micron laminate.
   */
  val laminatingJob: ValidatedNec[Issue, XJDF] =
    chainV(dsl.TicketDraft.of("laminatingJob", ProcessType.Laminating)) { draft =>
      val laminating = LaminatingIntent(
        surface = NonEmptyChain(Side.Front, Side.Back),
        temperature = Some(LaminatingTemperature.Hot),
        texture = Some(Catalog.Texture.Gloss),
        thickness = Some(Microns(25.0))
      )
      val payload = IntentPayload.Laminating(laminating)
      val intent = Intent(name = IntentName.of(payload.elementName), specific = payload)
      val product = Product(
        id = Some(Id.unsafe("P1")),
        isRoot = true,
        amount = Some(100L),
        intents = Chain.one(intent)
      )
      draft
        .withProductList(ProductList(products = NonEmptyChain.one(product)))
        .build
    }

  /** Fixture (§8.7 / Table 8.8): a sustainability-certified product intent
   *  (M1.6-1). The same `Certification` element is reused by three of its four
   *  modelled containers — `ColorIntent/SurfaceColor` (Table 4.21, ink
   *  certification), `MediaIntent` (Table 4.32, paper certification) and
   *  `ProductionIntent` (Table 4.33, production certification) — so the
   *  local-law bus is exercised across several containers of one element.
   *
   *  Two `Certification`s are given for production on purpose: Table 4.33 says
   *  "If more than one Certification is present, at least one of the
   *  certification levels SHALL be met", which constrains production and not
   *  the document, so the ticket stays valid (SPEC-COVERAGE, Deliberate
   *  Deviations).
   */
  val certificationJob: ValidatedNec[Issue, XJDF] =
    chainV(dsl.TicketDraft.of("certificationJob", ProcessType.Product)) { draft =>
      val fscMix = Certification(
        claim = Some(Catalog.CertificationClaim.FscMix70),
        identifier = Some(XjdfString.unsafe("FSC-C012345")),
        organization = Some(Catalog.CertificationOrganization.FSC)
      )
      val pefc = Certification(
        claim = Some(Catalog.CertificationClaim.pefcPercent(70)),
        organization = Some(Catalog.CertificationOrganization.PEFC)
      )
      val colorPayload = IntentPayload.Color(
        ColorIntent(front = Some(SurfaceColor(
          surface = Side.Front,
          coverage = Some(Coverage.unsafe(80.0)),
          certifications = Chain.one(fscMix)
        )))
      )
      val mediaPayload = IntentPayload.Media(
        MediaIntent(MediaType.Paper, weight = Some(Grammage(120.0)), certifications = Chain.one(fscMix))
      )
      val productionPayload = IntentPayload.Production(
        ProductionIntent(
          printPreference = Some(PrintPreference.HighestQuality),
          certifications = Chain(fscMix, pefc)
        )
      )
      val product = Product(
        id = Some(Id.unsafe("P1")),
        isRoot = true,
        amount = Some(250L),
        intents = Chain(
          Intent(name = IntentName.of(colorPayload.elementName), specific = colorPayload),
          Intent(name = IntentName.of(mediaPayload.elementName), specific = mediaPayload),
          Intent(name = IntentName.of(productionPayload.elementName), specific = productionPayload)
        )
      )
      draft
        .withProductList(ProductList(products = NonEmptyChain.one(product)))
        .build
    }

  /** Fixture (§4.6 / Tables 4.25–4.26): an embossing ticket with a blind
   *  embossing item and a foil stamping item (M1.6-10). The blind item names
   *  the embossing separation and the ticket carries the matching `Color`
   *  resource typed `DieLine`, as Table 4.26 SHALLs.
   */
  val embossingJob: ValidatedNec[Issue, XJDF] =
    chainV(dsl.TicketDraft.of("embossingJob", ProcessType.Embossing)) { draft =>
      val items = NonEmptyChain(
        EmbossingItem(
          direction = Some(EmbossDirection.Raised),
          embossingType = EmbossType.BlindEmbossing,
          face = Some(Face.Front),
          height = Some(0.3),
          imageSize = Some(XYPair(20.0, 20.0)),
          position = Some(XYPair(50.0, 50.0)),
          separation = Some(NmToken.unsafe("Emboss")),
          toolName = Some(NmToken.unsafe("EmbossDie-1"))
        ),
        EmbossingItem(
          direction = Some(EmbossDirection.Flat),
          embossingType = EmbossType.FoilStamping,
          foilColor = Some(Catalog.NamedColor.Silver),
          foilColorDetails = Some(XjdfString.unsafe("Holographic"))
        )
      )
      val payload = IntentPayload.Embossing(EmbossingIntent(items))
      val intent = Intent(name = IntentName.of(payload.elementName), specific = payload)
      val product = Product(
        id = Some(Id.unsafe("P1")),
        isRoot = true,
        amount = Some(200L),
        intents = Chain.one(intent)
      )
      val embossColor = Resource(
        specific = Some(ResourcePayload.ColorResource(Color(colorType = Some(ColorType.DieLine)))),
        parts = Chain.one(Part.token(PartitionKey.Separation, NmToken.unsafe("Emboss")))
      )
      draft
        .withProductList(ProductList(products = NonEmptyChain.one(product)))
        .withResources(
          ResourceSet(
            ResourceSetName.unsafe("Color"),
            usage = Some(Usage.Input),
            resources = Chain.one(embossColor)
          )
        )
        .build
    }

  /** Fixture (§4.5 / Tables 4.22–4.24): a content-check ticket (M1.6-11).
   *  The intent pairs with the `Approval` (§5.3.1) and `Preflight` (§5.4.14)
   *  processes — Chapter 5 defines no `ContentCheck` process of its own. A
   *  Premium preflight combines with a matched-color contract proof whose
   *  `@ID` is referenced by `DeliveryParams/DropItem/@ItemRef` (Table 6.55),
   *  exercising the intent-level `declaredIds` wiring.
   */
  val contentCheckJob: ValidatedNec[Issue, XJDF] =
    chainV(dsl.TicketDraft.of("contentCheckJob", ProcessType.Approval, ProcessType.Preflight)) { draft =>
      val contentCheck = ContentCheckIntent(
        preflightItems = Chain.one(PreflightItem(preflightLevel = Some(PreflightLevel.Premium))),
        proofItems = Chain.one(
          ProofItem(
            amount = Some(2L),
            colorType = Some(ProofColorType.MatchedColor),
            contract = Some(true),
            id = Some(Id.unsafe("Proof1")),
            pageIndex = Some(IntegerRange(0, 3)),
            fileSpec = Some(FileSpec.ofUrl(Url.unsafe("file:///proofs/customer-record.pdf")))
          )
        )
      )
      val payload = IntentPayload.ContentCheck(contentCheck)
      val intent = Intent(name = IntentName.of(payload.elementName), specific = payload)
      val product = Product(
        id = Some(Id.unsafe("P1")),
        isRoot = true,
        amount = Some(100L),
        intents = Chain.one(intent)
      )
      val delivery = Resource(
        specific = Some(ResourcePayload.DeliveryParamsResource(
          DeliveryParams(dropItems = Chain.one(DropItem(2L, IdRef.unsafe("Proof1"))))
        ))
      )
      draft
        .withProductList(ProductList(products = NonEmptyChain.one(product)))
        .withResources(
          ResourceSet(
            ResourceSetName.unsafe("DeliveryParams"),
            usage = Some(Usage.Input),
            resources = Chain.one(delivery)
          )
        )
        .build
    }

  /** Example 5.2: Split delivery — thirty books, ten to Drop1, twenty to Drop2. */
  val splitDelivery: ValidatedNec[Issue, XJDF] =
    val drop1 = PartBuilder.empty
      .withTokenUnsafe(PartitionKey.ContactType, Catalog.ContactType.Delivery)
      .withTokenUnsafe(PartitionKey.DropID, NmToken.unsafe("Drop1"))
      .build
    val drop2 = PartBuilder.empty
      .withTokenUnsafe(PartitionKey.ContactType, Catalog.ContactType.Delivery)
      .withTokenUnsafe(PartitionKey.DropID, NmToken.unsafe("Drop2"))
      .build
    val contact1 = Resource(
      specific = Some(ResourcePayload.ContactResource(
        Contact(address = Some(Address(city = Some(XjdfString.unsafe("city1")))))
      )),
      parts = Chain.one(drop1)
    )
    val contact2 = Resource(
      specific = Some(ResourcePayload.ContactResource(
        Contact(address = Some(Address(city = Some(XjdfString.unsafe("city2")))))
      )),
      parts = Chain.one(drop2)
    )
    val delivery1 = Resource(
      specific = Some(ResourcePayload.DeliveryParamsResource(
        DeliveryParams(dropItems = Chain.one(DropItem(10, IdRef.unsafe("IDBook"))))
      )),
      parts = Chain.one(Part.token(PartitionKey.DropID, NmToken.unsafe("Drop1")))
    )
    val delivery2 = Resource(
      specific = Some(ResourcePayload.DeliveryParamsResource(
        DeliveryParams(dropItems = Chain.one(DropItem(20, IdRef.unsafe("IDBook"))))
      )),
      parts = Chain.one(Part.token(PartitionKey.DropID, NmToken.unsafe("Drop2")))
    )
    val book = Product(amount = Some(30), id = Some(Id.unsafe("IDBook")), productType = Some(Catalog.ProductType.Book))
    chainV(dsl.TicketDraft.of("splitDelivery", ProcessType.Product)) { draft =>
      draft
        .withProductList(ProductList(NonEmptyChain.one(book)))
        .withResources(
          ResourceSet(
            ResourceSetName.unsafe("Contact"),
            usage = Some(Usage.Input),
            resources = Chain(contact1, contact2)
          ),
          ResourceSet(
            ResourceSetName.unsafe("DeliveryParams"),
            usage = Some(Usage.Input),
            resources = Chain(delivery1, delivery2)
          )
        )
        .build
    }

  /** Example 3.3: AuditResource — logging consumption of 421 copies of 90 g/m² media. */
  val mediaConsumptionAudit: ValidatedNec[Issue, AuditPool] =
    val header = Header(
      deviceId = NmToken.unsafe("TestSender"),
      time = Timestamp.unsafe("2020-03-01T19:55:57+01:00"),
      agentName = Some(XjdfString.unsafe("Writer")),
      agentVersion = Some(XjdfString.unsafe("V_2.0"))
    )
    val consumedMedia = Resource(
      specific = Some(ResourcePayload.MediaResource(Media(MediaType.Paper, weight = Some(Grammage(90.0))))))
    val audit = Audit.Resource(
      header = header,
      resourceInfo = ResourceInfo(
        ResourceSet(ResourceSetName.unsafe("Media"), resources = Chain.one(consumedMedia))
      )
    )
    AuditPool.of(audit).validNec

  /** A small brochure job with intents, resources and a completed process run. */
  val brochureJob: ValidatedNec[Issue, XJDF] =
    chainV(
      dsl.intent(
        "MediaIntent",
        IntentPayload.Media(MediaIntent(MediaType.Paper, weight = Some(Grammage(115.0))))
      )
    ) { mediaIntent =>
      chainV(
        dsl.intent(
          "LayoutIntent",
          IntentPayload.Layout(LayoutIntent(finishedDimensions = Some(Shape.flat(595.28, 841.89)), pages = Some(8)))
        )
      ) { layoutIntent =>
        chainV(
          dsl.intent(
            "BindingIntent",
            IntentPayload.Binding(
              BindingIntent(
                bindingType = BindingType.SaddleStitch,
                bindingSide = Some(Edge.Left),
                details = Some(SaddleStitching(stapleShape = Some(StapleShape.Crown), stitchNumber = Some(2)))
              )
            )
          )
        ) { bindingIntent =>
          val intents = Chain(mediaIntent, layoutIntent, bindingIntent)
          chainV(dsl.product(amount = Some(500), productType = Some("Brochure"))(intents.toList*)) { brochure =>
            chainV(
              dsl.runList(
                RunList(fileSpecs = Some(FileSpec(url = Some(Url.unsafe("file:///artwork/brochure.pdf"))))),
                id = Some("runlist_1")
              )
            ) { runList =>
              chainV(dsl.media(Media.paper(Grammage(115.0)), id = Some("media_1"))) { mediaRes =>
                chainV(dsl.resourceSet("RunList", usage = Some(Usage.Input))(runList)) { inputRunList =>
                  chainV(dsl.resourceSet("Media", usage = Some(Usage.Input))(mediaRes)) { inputMedia =>
                    chainV(dsl.component(Component.of(Catalog.ProductType.Brochure), id = Some("comp_1"))) {
                      componentRes =>
                        chainV(dsl.resourceSet("Component", usage = Some(Usage.Output))(componentRes)) {
                          outputComponent =>
                            chainV(dsl.TicketDraft.of(
                              "Brochure-2026",
                              ProcessType.DigitalPrinting,
                              ProcessType.Stitching
                            )) { draft =>
                              val run = ProcessRun(
                                start = Timestamp.unsafe("2026-08-14T08:00:00+02:00"),
                                end = Timestamp.unsafe("2026-08-14T08:37:00+02:00"),
                                endStatus = EndStatus.Completed
                              )
                              val audits = AuditPool.of(
                                Audit.Created(Header(
                                  NmToken.unsafe("MIS"),
                                  Timestamp.unsafe("2026-08-14T07:55:00+02:00")
                                )),
                                Audit.Run(
                                  Header(NmToken.unsafe("Press-7"), Timestamp.unsafe("2026-08-14T08:37:00+02:00")),
                                  run
                                )
                              )
                              draft
                                .withProductList(ProductList(NonEmptyChain.one(brochure)))
                                .withResources(inputRunList, inputMedia, outputComponent)
                                .withAuditPool(audits)
                                .build
                            }
                        }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

  /** Example 9.6-style change order: raise the brochure run amount (§1.3.2).
   *  A nominal partial document (ADR-0001), not a degenerate `XJDF & Partial`.
   */
  val changeOrder: ChangeOrder =
    ChangeOrder(
      jobId = JobId.unsafe("Brochure-2026"),
      resourceSets = Chain.one(
        ResourceSet(
          name = ResourceSetName.unsafe("Component"),
          usage = Some(Usage.Output),
          resources = Chain.one(
            Resource(
              specific = Some(ResourcePayload.ComponentResource(Component.of(Catalog.ProductType.Brochure))),
              id = Some(Id.unsafe("comp_1")),
              amountPool = Some(AmountPool.of(PartAmount(amount = Some(Amount(650.0)))))
            )
          )
        )
      )
    )

  /** The brochure ticket after the change order has been compiled, applied
   *  and revalidated (ADR-0001).
   */
  val updatedBrochureJob: ValidatedNec[Issue, XJDF] =
    chainV(brochureJob)(_.applyChange(changeOrder))

  /** Render everything for the demo main. */
  def renderAll: List[String] =
    List(
      "Example 3.1 (minimal product):" -> minimalProduct.map(Show[XJDF].show),
      "Example 3.4 (notebook BOM):" -> notebook.map(Show[ProductList].show),
      "Example 3.6 (combined):" -> combinedProcesses.map(Show[XJDF].show),
      "Creasing job (Table 8.17):" -> creasingJob.map(Show[XJDF].show),
      "Gang source (Table 8.27):" -> gangSource.map(Show[GangSource].show),
      "MIS details (Table 8.48):" -> misDetails.map(Show[MISDetails].show),
      "Gang job (Table 6.119):" -> gangJob.map(Show[XJDF].show),
      "Barcode job (Table 8.31):" -> barcodeJob.map(Show[XJDF].show),
      "Metadata map (Examples 8.6/8.7):" -> metadataMapJob.map(Show[XJDF].show),
      "Gluing job (Table 8.29):" -> gluingJob.map(Show[XJDF].show),
      "Hole punching job (Table 8.30 / Appendix F):" -> holePunchingJob.map(Show[XJDF].show),
      "Hole making intent (Table 4.29):" -> holeMakingJob.map(Show[XJDF].show),
      "Laminating intent (Table 4.30):" -> laminatingJob.map(Show[XJDF].show),
      "Embossing intent (Table 4.25):" -> embossingJob.map(Show[XJDF].show),
      "Certification (Table 8.8):" -> certificationJob.map(Show[XJDF].show),
      "Content check intent (Table 4.22):" -> contentCheckJob.map(Show[XJDF].show),
      "Example 5.2 (split delivery):" -> splitDelivery.map(Show[XJDF].show),
      "Brochure job:" -> brochureJob.map(Show[XJDF].show),
      "Brochure job after change:" -> updatedBrochureJob.map(Show[XJDF].show)
    ).map { case (label, result) =>
      s"$label\n  ${result.fold(_.toChain.toList.map(Show[Issue].show).mkString(", "), identity)}"
    }
end SpecExamples
