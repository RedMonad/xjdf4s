package xjdf4s.examples

import xjdf4s.model.*
import xjdf4s.prim.*
import cats.Show

/**
 * Demo entry point: `sbt examples/run`.
 *
 * Renders the specification examples built by `SpecExamples` and demonstrates
 * the categorical machinery: the BOM catamorphism, the audit alignment and the
 * change-order monoid.
 */
object Main:

  def main(args: Array[String]): Unit =
    SpecExamples.renderAll.foreach(println)

    demoBomFold()
    demoChangeOrder()
    demoAlignment()
    demoMatrix()

  /** The bill of materials as an initial algebra: unfold + catamorphism. */
  private def demoBomFold(): Unit =
    println("\n--- BOM: unfold the notebook, fold the amounts ---")
    SpecExamples.notebook match
      case cats.data.Validated.Invalid(issues) =>
        println(s"invalid: ${issues.toChain.toList.map(_.message).mkString("; ")}")
      case cats.data.Validated.Valid(pl) =>
        Bom.fromProductList(pl) match
          case Left(issue) => println(s"unfold failed: ${issue.message}")
          case Right(forest) =>
            forest.toChain.toList.foreach { tree =>
              Bom.totalCopies(tree).foreach { case (p, copies) =>
                println(s"  ${p.productType.fold("?")(_.value)}: $copies copies")
              }
              println(s"  amount validation: ${Bom.validateAmounts(tree)}")
            }

  /** The monoid action of change orders on tickets. */
  private def demoChangeOrder(): Unit =
    println("\n--- Change order: Patch monoid action ---")
    SpecExamples.updatedBrochureJob match
      case cats.data.Validated.Invalid(issues) =>
        println(s"invalid: ${issues.toChain.toList.map(_.message).mkString("; ")}")
      case cats.data.Validated.Valid(ticket) =>
        ticket.resourceSetsNamed(ResourceSetName.unsafe("Component")).toList.foreach { rs =>
          rs.resources.toList.foreach { r =>
            println(s"  ${rs.name.toNmToken.value} amount = ${r.amountPool.fold("unset")(_.totalAmount.value.toString)}")
          }
        }

  /** Signals → audits via the Table 3.2 alignment. */
  private def demoAlignment(): Unit =
    println("\n--- Alignment: Signal → Audit (Table 3.2) ---")
    val header = Header(NmToken.unsafe("Press-7"), Timestamp.ofEpochSecond(100))
    val pulse = Pulse.beat(Signal(header, SignalPayload.Status(DeviceInfo(DeviceStatus.Production))))
    val audit = pulse match
      case Pulse.Beat(signal) => Alignment.signalToAudit(signal)
    println(s"  ${audit.elementName.value} at ${audit.time}")

  /** Affine matrices as the monoid of plane transformations. */
  private def demoMatrix(): Unit =
    println("\n--- Matrix monoid: Table 2.1 orientations ---")
    val rot90 = Orientation.Rotate90.matrix(210.0, 297.0)
    val point = XYPair(10.0, 0.0)
    println(s"  Rotate90 matrix: ${Show[Matrix].show(rot90)}")
    println(s"  applied to (10, 0): ${Show[XYPair].show(rot90.applyTo(point))}")
    println(s"  identity == 1 0 0 1 0 0: ${cats.kernel.Monoid[Matrix].empty == Matrix.identity}")
