package xjdf4s.examples

import xjdf4s.dsl.dsl
import xjdf4s.intents.*
import xjdf4s.model.*
import xjdf4s.prim.*
import xjdf4s.resources.*
import cats.Show
import cats.data.{Chain, NonEmptyChain, Validated, ValidatedNec}
import cats.syntax.all.*

/**
 * The worked examples of the XJDF specification, expressed with the
 * declarative DSL. Every example is a plain Scala value — building it is
 * validation: the result is a `ValidatedNec[Issue, XJDF]`.
 */
object SpecExamples:

  /**
   * Sequential chaining of `Validated`. `Validated` is applicative, not a
   * monad: it deliberately has no `flatMap` (see cats, "Validated vs Either"),
   * so sequencing is spelled out as a pattern match on the two cases — no
   * syntax imports involved.
   */
  private def chainV[E, A, B](v: ValidatedNec[E, A])(f: A => ValidatedNec[E, B]): ValidatedNec[E, B] =
    v match
      case Validated.Valid(a)          => f(a)
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
        chainV(dsl.product(amount = Some(1), isRoot = false, productType = Some("FrontCover"), id = Some("ICover"))()) { cover =>
          chainV(dsl.product(amount = Some(50), isRoot = false, productType = Some("BookBlock"), id = Some("IBody"))()) { body =>
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
        chainV(dsl.resourceSet("CuttingParams", usage = Some(Usage.Input))()) { cuttingParams =>
          chainV(dsl.resourceSet("FoldingParams", usage = Some(Usage.Input))()) { foldingParams =>
            chainV(dsl.TicketDraft.of("CPI_Example", ProcessType.Cutting, ProcessType.Folding)) { draft =>
              draft
                .withResources(cuttingInfo, foldingInfo, cuttingParams, foldingParams)
                .build
            }
          }
        }
      }
    }

  /** Example 5.2: Split delivery — thirty books, ten to Drop1, twenty to Drop2. */
  val splitDelivery: ValidatedNec[Issue, XJDF] =
    val drop1 = PartBuilder.empty
      .withToken(PartitionKey.ContactType, Catalog.ContactType.Delivery)
      .withToken(PartitionKey.DropID, NmToken.unsafe("Drop1"))
      .build
    val drop2 = PartBuilder.empty
      .withToken(PartitionKey.ContactType, Catalog.ContactType.Delivery)
      .withToken(PartitionKey.DropID, NmToken.unsafe("Drop2"))
      .build
    val contact1 = Resource(
      specific = ResourcePayload.ContactResource(
        Contact(address = Some(Address(city = Some(XjdfString.unsafe("city1")))))
      ),
      parts = Chain.one(drop1)
    )
    val contact2 = Resource(
      specific = ResourcePayload.ContactResource(
        Contact(address = Some(Address(city = Some(XjdfString.unsafe("city2")))))
      ),
      parts = Chain.one(drop2)
    )
    val delivery1 = Resource(
      specific = ResourcePayload.DeliveryParamsResource(
        DeliveryParams(dropItems = Chain.one(DropItem(10, IdRef.unsafe("IDBook"))))
      ),
      parts = Chain.one(Part.token(PartitionKey.DropID, NmToken.unsafe("Drop1")))
    )
    val delivery2 = Resource(
      specific = ResourcePayload.DeliveryParamsResource(
        DeliveryParams(dropItems = Chain.one(DropItem(20, IdRef.unsafe("IDBook"))))
      ),
      parts = Chain.one(Part.token(PartitionKey.DropID, NmToken.unsafe("Drop2")))
    )
    val book = Product(amount = Some(30), id = Some(Id.unsafe("IDBook")), productType = Some(Catalog.ProductType.Book))
    chainV(dsl.TicketDraft.of("splitDelivery", ProcessType.Product)) { draft =>
      draft
        .withProductList(ProductList(NonEmptyChain.one(book)))
        .withResources(
          ResourceSet(ResourceSetName.unsafe("Contact"), usage = Some(Usage.Input), resources = Chain(contact1, contact2)),
          ResourceSet(ResourceSetName.unsafe("DeliveryParams"), usage = Some(Usage.Input), resources = Chain(delivery1, delivery2))
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
      specific = ResourcePayload.MediaResource(Media(MediaType.Paper, weight = Some(Grammage(90.0))))
    )
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
                RunList(fileSpecs = Chain.one(FileSpec(url = Some(Url.unsafe("file:///artwork/brochure.pdf"))))),
                id = Some("runlist_1")
              )
            ) { runList =>
              chainV(dsl.media(Media.paper(Grammage(115.0)), id = Some("media_1"))) { mediaRes =>
                chainV(dsl.resourceSet("RunList", usage = Some(Usage.Input))(runList)) { inputRunList =>
                  chainV(dsl.resourceSet("Media", usage = Some(Usage.Input))(mediaRes)) { inputMedia =>
                    chainV(dsl.component(Component.of(Catalog.ProductType.Brochure), id = Some("comp_1"))) { componentRes =>
                      chainV(dsl.resourceSet("Component", usage = Some(Usage.Output))(componentRes)) { outputComponent =>
                        chainV(dsl.TicketDraft.of("Brochure-2026", ProcessType.DigitalPrinting, ProcessType.Stitching)) { draft =>
                          val run = ProcessRun(
                            start = Timestamp.unsafe("2026-08-14T08:00:00+02:00"),
                            end = Timestamp.unsafe("2026-08-14T08:37:00+02:00"),
                            endStatus = EndStatus.Completed
                          )
                          val audits = AuditPool.of(
                            Audit.Created(Header(NmToken.unsafe("MIS"), Timestamp.unsafe("2026-08-14T07:55:00+02:00"))),
                            Audit.Run(Header(NmToken.unsafe("Press-7"), Timestamp.unsafe("2026-08-14T08:37:00+02:00")), run)
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

  /** A change order: raise the amount of the brochure run (a `Patch`). */
  val changeOrder: Patch =
    Patch.updateResourceSets { rs =>
      if rs.name == ResourceSetName.unsafe("Component") && rs.usage.contains(Usage.Output) then
        Some(
          rs.copy(resources = rs.resources.map { r =>
            r.copy(amountPool = Some(AmountPool.of(PartAmount(amount = Some(Amount(650.0))))))
          })
        )
      else Some(rs)
    }

  /** The brochure ticket after the change order has been applied. */
  val updatedBrochureJob: ValidatedNec[Issue, XJDF] =
    chainV(brochureJob.map(_.withPatch(changeOrder)))(t => t.validate.as(t))

  /** Render everything for the demo main. */
  def renderAll: List[String] =
    List(
      "Example 3.1 (minimal product):" -> minimalProduct.map(Show[XJDF].show),
      "Example 3.4 (notebook BOM):"    -> notebook.map(Show[ProductList].show),
      "Example 3.6 (combined):"        -> combinedProcesses.map(Show[XJDF].show),
      "Example 5.2 (split delivery):"  -> splitDelivery.map(Show[XJDF].show),
      "Brochure job:"                  -> brochureJob.map(Show[XJDF].show),
      "Brochure job after change:"     -> updatedBrochureJob.map(Show[XJDF].show)
    ).map { case (label, result) =>
      s"$label\n  ${result.fold(_.toChain.toList.map(Show[Issue].show).mkString(", "), identity)}"
    }
