package xjdf4s.laws

import xjdf4s.intents.*
import xjdf4s.model.*
import xjdf4s.prim.*
import cats.Eval
import cats.data.{Chain, NonEmptyChain}
import munit.FunSuite

/** M1.1-1: `Bom.fromProductList` is a *monadic* unfold of the ProductList reference
 *  graph. Regression suite for N-01 — the false-cycle bug that rejected every valid
 *  tree with `@ChildRefs`.
 *
 *  M1.4-7 (N-27): stack safety of the unfold and catamorphism; deep chains
 *  (≥ 10 000) do not overflow the JVM stack.
 */
class BomLaws extends FunSuite:

  /** A product whose binding intent references `childRefs` (empty ⇒ a leaf). */
  private def product(
      amount: Long,
      id: Option[String],
      isRoot: Boolean,
      childRefs: List[String] = Nil
  ): Product =
    val intents =
      if childRefs.isEmpty then Chain.empty[Intent]
      else
        val refs = childRefs.map(IdRef.unsafe)
        Chain.one(
          Intent(
            name = IntentName.unsafe("BindingIntent"),
            specific = IntentPayload.Binding(
              BindingIntent(bindingType = BindingType.EdgeGluing, childRefs = Some(IdRefs.of(refs.head, refs.tail*)))
            )
          )
        )
    Product(amount = Some(amount), id = id.map(Id.unsafe), isRoot = isRoot, intents = intents)

  private def depth(t: Bom.Tree): Int =
    t.unfix match
      case ProductTree.Leaf(_) => 1
      case ProductTree.Node(_, kids) => 1 + kids.toList.map(depth).maxOption.getOrElse(0)

  test("leaf without @ID unfolds to a single leaf tree"):
    val pl = ProductList.of(Product(amount = Some(1)))
    val result = Bom.fromProductList(pl)
    assert(result.isRight)
    val forest = result.toOption.get.toChain.toList
    assertEquals(forest.size, 1)
    forest.head.unfix match
      case ProductTree.Leaf(p) => assertEquals(p.amount, Some(1L))
      case _ => fail("expected a leaf")

  test("a valid tree of depth 3 unfolds without false cycles"):
    val c = product(1, Some("C"), isRoot = false)
    val b = product(1, Some("B"), isRoot = false, childRefs = List("C"))
    val a = product(10, Some("A"), isRoot = true, childRefs = List("B"))
    val result = Bom.fromProductList(ProductList.of(a, b, c))
    assert(result.isRight, result.toString)
    val forest = result.toOption.get.toChain.toList
    assertEquals(forest.size, 1)
    assertEquals(depth(forest.head), 3)

  test("an unresolved @ChildRef fails the unfold"):
    val a = product(10, Some("A"), isRoot = true, childRefs = List("Missing"))
    val result = Bom.fromProductList(ProductList.of(a))
    assert(result.isLeft)
    assert(result.left.toOption.exists(_.message.contains("Unresolved ChildRef 'Missing'")))

  test("a self-cycle (A -> A) is detected"):
    val a = product(1, Some("A"), isRoot = true, childRefs = List("A"))
    val result = Bom.fromProductList(ProductList.of(a))
    assert(result.isLeft)
    assert(result.left.toOption.exists(_.message.contains("Cycle")))

  test("an indirect cycle (A -> B -> C -> A) is detected"):
    val c = product(1, Some("C"), isRoot = false, childRefs = List("A"))
    val b = product(1, Some("B"), isRoot = false, childRefs = List("C"))
    val a = product(1, Some("A"), isRoot = true, childRefs = List("B"))
    val result = Bom.fromProductList(ProductList.of(a, b, c))
    assert(result.isLeft)

  test("a DAG with a shared child is not a cycle"):
    val c = product(1, Some("C"), isRoot = false)
    val a = product(1, Some("A"), isRoot = false, childRefs = List("C"))
    val b = product(1, Some("B"), isRoot = false, childRefs = List("C"))
    val r = product(2, Some("R"), isRoot = true, childRefs = List("A", "B"))
    val result = Bom.fromProductList(ProductList.of(r, a, b, c))
    assert(result.isRight, result.toString)
    val forest = result.toOption.get.toChain.toList
    assertEquals(forest.size, 1)
    // the shared child C is counted once per branch, not flagged as a cycle
    assertEquals(Bom.totalCopies(forest.head).count(_._1.id.contains(Id.unsafe("C"))), 2)

  test("duplicate Product/@ID is rejected by root ID uniqueness (§2.2.3), not by the unfold"):
    val root = Product(amount = Some(1), id = Some(Id.unsafe("dup")), isRoot = true)
    val part = Product(amount = Some(1), id = Some(Id.unsafe("dup")), isRoot = false)
    val ticket = XJDF(
      jobId = JobId.unsafe("dupTest"),
      types = NonEmptyChain.one(ProcessType.Product),
      productList = Some(ProductList.of(root, part))
    )
    assert(ticket.validate.isInvalid)

  // --- M1.4-7 (N-27): stack-safe BOM with Eval ---------------------------------

  /** Builds a single child-ref intent linking `id` → `childId`. */
  private def linkingIntent(childId: String): Intent =
    Intent(
      name = IntentName.unsafe("BindingIntent"),
      specific = IntentPayload.Binding(
        BindingIntent(
          bindingType = BindingType.EdgeGluing,
          childRefs = Some(IdRefs.of(IdRef.unsafe(childId)))
        )
      )
    )

  // Slow test: creates a synthetic chain of 10 000 products via @ChildRefs.
  // Both `fromProductList` (unfold) and `cataEval` (fold) use `Eval.defer`
  // trampolining, so this should complete without `StackOverflowError`.
  test("BOM depth >= 10000 does not overflow stack"):
    val n = 10000
    val products = (0 until n).map { i =>
      val id = Some(s"P$i")
      val isRoot = i == 0
      val intents =
        if i < n - 1 then Chain.one(linkingIntent(s"P${i + 1}"))
        else Chain.empty[Intent]
      Product(
        amount = Some(1L),
        id = id.map(Id.unsafe),
        isRoot = isRoot,
        intents = intents
      )
    }.toList
    val productList = ProductList(NonEmptyChain.fromChainUnsafe(Chain.fromSeq(products)))
    val result = Bom.fromProductList(productList)
    assert(result.isRight)
    val tree = result.toOption.get.head
    val measuredDepth = Bom.cataEval[Int] {
      case ProductTree.Leaf(_) => Eval.now(1)
      case ProductTree.Node(_, kids) => Eval.now(1 + (if kids.isEmpty then 0 else kids.toList.max))
    }(tree).value
    assertEquals(measuredDepth, n)
end BomLaws
