package TreeTest

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.Matchers._

/**
  * Created by Case on 20/07/2017.
  */
class SMTTest extends FunSuite with BeforeAndAfterAll {

  val strTrace = "ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15040 kernel32.dll+0x15c8b kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15fa7 kernel32.dll+0x15c8b kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15cfc kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15cfc"
  val intTrace = "1 2 3 4 5 6 7 8 9 10 11 1 2 3 12 13 4 5 6 7 8 9 10 11 1 2 3 14 13 4 5 6 7 8 9 10 11 1 2 3 15 4 5 6 7 8 9 10 11 1 2 3 15"
  val intListTrace = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 12, 13, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 14, 13, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 15, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 15)
  val intVectorTrace = Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 12, 13, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 14, 13, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 15, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 15)
  val shortListTrace = Vector(1, 2, 3, 4, 5)
  val shortListTrace2 = Vector(5, 4, 3, 2, 1)
  val shortListTrace3 = Vector(1, 2, 3)
  val shortListTrace4 = Vector(4, 5, 6)
  val shortMap = Map(1 -> 0.1, 2 -> 0.2, 3 -> 0.3)
  val shortStringListTrace = Vector("ntdll.dll+0x2173e, ntdll.dll+0x21639, ntdll.dll+0xeac7, kernel32.dll+0x15568, comctl32.dll+0x8ac2d")
  val shortStringMap = Map("ntdll.dll+0x2173e" -> 0.1, "ntdll.dll+0x21639" -> 0.2, "ntdll.dll+0xeac7" -> 0.3)


  /*override def beforeAll(): Unit = {

  }*/

  test("SMT. maxDepth is not zero") {
    assertThrows[IllegalArgumentException](Node(0, 1, 3))
    val caught = intercept[IllegalArgumentException](Node(0, 1, 3))
    assert(caught.getMessage == "requirement failed: Max depth count must be positive!")
  }
  test("SMT. maxDepth is not negative") {
    assertThrows[IllegalArgumentException](Node(-1, 1, 3))
    val caught = intercept[IllegalArgumentException](Node(-1, 1, 3))
    assert(caught.getMessage == "requirement failed: Max depth count must be positive!")
  }
  test("SMT. maxPhi is non-negative") {
    assertThrows[IllegalArgumentException](Node(1, -1, 3))
    val caught = intercept[IllegalArgumentException](Node(1, -1, 3))
    assert(caught.getMessage == "requirement failed: Max Phi count must be non-negative!")
  }
  test("SMT. maxSeqCount is not zero") {
    assertThrows[IllegalArgumentException](Node(1, 1, 0))
    val caught = intercept[IllegalArgumentException](Node(1, 1, 0))
    assert(caught.getMessage == "requirement failed: Max sequence count must be positive!")
  }
  test("SMT. maxSeqCount is not negative") {
    assertThrows[IllegalArgumentException](Node(1, 1, -1))
    val caught = intercept[IllegalArgumentException](Node(1, 1, -1))
    assert(caught.getMessage == "requirement failed: Max sequence count must be positive!")
  }
  test("SMT. Default key is None") {
    val root: Node[Int, Int] = Node(5, 1, 3)
    root.getKey shouldBe None
  }
  test("SMT. setKey sets correct Int key") {
    val root: Node[Int, Int] = Node(5, 1, 3)
    root.getKey shouldBe None
    root.setKey(0)
    root.getKey shouldBe 'defined
    assert(root.getKey.get == 0)
  }
  test("SMT. setKey sets correct String key") {
    val root: Node[String, Int] = Node(5, 1, 3)
    root.getKey shouldBe None
    root.setKey("TestKey")
    root.getKey shouldBe 'defined
    assert(root.getKey.get equals "TestKey")
  }
  test("SMT. setKey fails if called multiple times - String key") {
    val root: Node[String, Int] = Node(5, 1, 3)
    root.setKey("TestKey")
    root.getKey shouldBe 'defined
    assert(root.getKey.get equals "TestKey")

    val caught = intercept[IllegalStateException](root.setKey("NewTestKey"))
    assert(caught.getMessage == "Node key cannot be reset!")
  }
  test("SMT. setKey fails if called multiple times - Int key") {
    val root: Node[Int, Int] = Node(5, 1, 3)
    root.setKey(0)
    root.getKey shouldBe 'defined
    assert(root.getKey.get == 0)

    val caught = intercept[IllegalStateException](root.setKey(222))
    assert(caught.getMessage equals "Node key cannot be reset!")
  }
  test("SMT eventCount is correct") {
    val n1 = new Node[Int, Int](5, 1, 1)
    assert(n1.getEventCount == 0)
    n1.updateEvents(666)
    assert(n1.getEventCount == 1)
    n1.updateEvents(666)
    assert(n1.getEventCount == 2)
    n1.updateEvents(777)
    assert(n1.getEventCount == 3)
  }
  test("SMT, events initialised empty") {
    val n1 = new Node[Int, Int](5, 1, 1)
    assertThrows[NoSuchElementException](n1.getEvents(666))
    assert(n1.getEvents.isEmpty)
  }
  test("SMT 'events' is correct after updates with same event") {
    val n1 = new Node[Int, Int](5, 1, 1)

    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)

    n1.updateEvents(666)
    assert(n1.getEventCount == 1)
    assert(n1.getEvents.size == 1)
    assert(n1.getProbability(666).get == 1.00)

    n1.updateEvents(666)
    assert(n1.getEventCount == 2)
    assert(n1.getEvents.size == 1)
    assert(n1.getProbability(666).get == 1.00)

    n1.updateEvents(666)
    assert(n1.getEventCount == 3)
    assert(n1.getEvents.size == 1)
    assert(n1.getProbability(666).get == 1.00)
  }
  test("SMT events is correct after updates with different events") {
    val n1 = new Node[Int, Int](5, 1, 1)

    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)

    n1.updateEvents(666)
    assert(n1.getEventCount == 1)
    assert(n1.getEvents.size == 1)
    assert(n1.getProbability(666).get == 1.00)

    n1.updateEvents(666)
    assert(n1.getEventCount == 2)
    assert(n1.getEvents.size == 1)
    assert(n1.getProbability(666).get == 1.00)

    n1.updateEvents(666)
    assert(n1.getEventCount == 3)
    assert(n1.getEvents.size == 1)
    assert(n1.getProbability(666).get == 1.00)

    n1.updateEvents(777)
    assert(n1.getEventCount == 4)
    assert(n1.getEvents.size == 2)
    assert(n1.getEvents.get(666).get == 3)
    assert(n1.getEvents.get(777).get == 1)
    assert(n1.getProbability(666).get == 3.00 / 4)
    assert(n1.getProbability(777).get == 1.00 / 4)

    n1.updateEvents(777)
    assert(n1.getEventCount == 5)
    assert(n1.getEvents.size == 2)
    assert(n1.getEvents.get(666).get == 3)
    assert(n1.getEvents.get(777).get == 2)
    assert(n1.getProbability(666).get == 3.00 / 5)
    assert(n1.getProbability(777).get == 2.00 / 5)

    n1.updateEvents(888)
    assert(n1.getEventCount == 6)
    assert(n1.getEvents.size == 3)
    assert(n1.getEvents.get(666).get == 3)
    assert(n1.getEvents.get(777).get == 2)
    assert(n1.getEvents.get(888).get == 1)
    assert(n1.getProbability(666).get == 3.00 / 6)
    assert(n1.getProbability(777).get == 2.00 / 6)
    assert(n1.getProbability(888).get == 1.00 / 6)
  }
  test("SMT predictions has single element after first event update") {
    val n1 = new Node[Int, Int](5, 1, 1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
  }
  test("SMT predictions has two elements after two different event key updates") {
    val n1 = new Node[Int, Int](5, 1, 1)

    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(777)
    assert(n1.getPredictions.size == 2)
  }
  test("SMT predictions has one element after the same event key is updated twice") {
    val n1 = new Node[Int, Int](5, 1, 1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
  }
  test("SMT predictions has two elements after adding three events, two the same") {
    val n1 = new Node[Int, Int](5, 1, 1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(777)
    assert(n1.getPredictions.size == 2)
  }
  test("SMT predictions are correct") {
    val n1 = new Node[Int, Int](5, 1, 1)
    n1.updateEvents(666)
    assert(n1.getPredictions(666) == 1.0)
    n1.updateEvents(666)
    assert(n1.getPredictions(666) == 1.0)
    n1.updateEvents(777)
    assert(n1.getPredictions(777) == 1.0 / 3)
    n1.updateEvents(777)
    assert(n1.getPredictions(777) == 2.0 / 4)
    n1.updateEvents(888)
    assert(n1.getPredictions(666) == 2.0 / 5)
    assert(n1.getPredictions(777) == 2.0 / 5)
    assert(n1.getPredictions(888) == 1.0 / 5)
    n1.updateEvents(999)
    assert(n1.getPredictions(666) == 2.0 / 6)
    assert(n1.getPredictions(777) == 2.0 / 6)
    assert(n1.getPredictions(888) == 1.0 / 6)
    assert(n1.getPredictions(999) == 1.0 / 6)
  }
  test("SMT getProbability for single event") {
    val n1 = new Node[Int, Int](5, 1, 1)
    n1.updateEvents(666)
    assert(n1.getProbability(666).get == 1.0)
  }
  test("SMT getProbability for events after multiple updates") {
    val n1 = new Node[Int, Int](5, 1, 1)
    n1.updateEvents(666)
    assert(n1.getProbability(666).get == 1.0)
    n1.updateEvents(777)
    assert(n1.getProbability(666).get == 0.5)
    assert(n1.getProbability(777).get == 0.5)

    n1.updateEvents(888)
    assert(n1.getProbability(666).get == 1.0 / 3)
    assert(n1.getProbability(777).get == 1.0 / 3)
    assert(n1.getProbability(888).get == 1.0 / 3)
    n1.updateEvents(888)
    assert(n1.getProbability(666).get == 1.0 / 4)
    assert(n1.getProbability(777).get == 1.0 / 4)
    assert(n1.getProbability(888).get == 2.0 / 4)
    n1.updateEvents(999)
    assert(n1.getProbability(666).get == 1.0 / 5)
    assert(n1.getProbability(777).get == 1.0 / 5)
    assert(n1.getProbability(888).get == 2.0 / 5)
    assert(n1.getProbability(999).get == 1.0 / 5)
    n1.updateEvents(666)
    assert(n1.getProbability(666).get == 2.0 / 6)
    assert(n1.getProbability(777).get == 1.0 / 6)
    assert(n1.getProbability(888).get == 2.0 / 6)
    assert(n1.getProbability(999).get == 1.0 / 6)
  }
  test("SMT getProbability for String events after multiple updates. (String key)") {
    val n1 = new Node[Int, String](5, 1, 1)
    n1.updateEvents("ntdll.dll+0x2173e")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0)
    n1.updateEvents("ntdll.dll+0x21639")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 0.5)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 0.5)
    n1.updateEvents("ntdll.dll+0xeac7")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0 / 3)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0 / 3)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 1.0 / 3)
    n1.updateEvents("ntdll.dll+0xeac7")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0 / 4)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0 / 4)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 2.0 / 4)
    n1.updateEvents("kernel32.dll+0x15568")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0 / 5)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0 / 5)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 2.0 / 5)
    assert(n1.getProbability("kernel32.dll+0x15568").get == 1.0 / 5)
    n1.updateEvents("ntdll.dll+0x2173e")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 2.0 / 6)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0 / 6)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 2.0 / 6)
    assert(n1.getProbability("kernel32.dll+0x15568").get == 1.0 / 6)
  }
  test("Sequence getProbability for String events after multiple updates. (Int key)") {
    val n1 = new Node[Int, String](5, 1, 1)
    n1.updateEvents("ntdll.dll+0x2173e")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0)
    n1.updateEvents("ntdll.dll+0x21639")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 0.5)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 0.5)
    n1.updateEvents("ntdll.dll+0xeac7")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0 / 3)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0 / 3)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 1.0 / 3)
    n1.updateEvents("ntdll.dll+0xeac7")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0 / 4)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0 / 4)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 2.0 / 4)
    n1.updateEvents("kernel32.dll+0x15568")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0 / 5)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0 / 5)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 2.0 / 5)
    assert(n1.getProbability("kernel32.dll+0x15568").get == 1.0 / 5)
    n1.updateEvents("ntdll.dll+0x2173e")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 2.0 / 6)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0 / 6)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 2.0 / 6)
    assert(n1.getProbability("kernel32.dll+0x15568").get == 1.0 / 6)
  }
  test("SMT eventCount == events sum in events") {
    val n1 = new Node[Int, Int](5, 1, 1)
    n1.updateEvents(666)
    assert(n1.getEventCount == 1)
    var eventNum = 0
    for (e <- n1.getEvents) {
      eventNum += e._2
    }
    assert(eventNum == n1.getEventCount)

    n1.updateEvents(666)
    assert(n1.getEventCount == 2)
    var eventNum2 = 0
    for (e <- n1.getEvents) {
      eventNum2 += e._2
    }
    assert(eventNum2 == n1.getEventCount)
    assert(eventNum2 == 2)
    assert(2 == n1.getEventCount)

    n1.updateEvents(777)
    assert(n1.getEventCount == 3)
    var eventNum3 = 0
    for (e <- n1.getEvents) {
      eventNum3 += e._2
    }
    assert(eventNum3 == n1.getEventCount)
    assert(eventNum3 == 3)
    assert(3 == n1.getEventCount)

    n1.updateEvents(888)
    assert(n1.getEventCount == 4)
    var eventNum4 = 0
    for (e <- n1.getEvents) {
      eventNum4 += e._2
    }
    assert(eventNum4 == n1.getEventCount)
    assert(eventNum4 == 4)
    assert(4 == n1.getEventCount)
  }
  //SMT ----------------------------------
  test("SMT, invalid argument to growTree: empty Vector| event") {
    val n1 = new Node[Int, Int](5, 1, 1)
    n1.growTree(Vector.empty[Int], 666)
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    assert(n1.getChildren.isEmpty)
  }
  test("SMT, growTree called with Vector size == 1 | event") {
    val n1 = new Node[Int, Int](5, 1, 1)
    val condition = Vector(1)
    val event = 666
    n1.growTree(condition, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 1)
    assert(n1.getChildren(0).size == 1)
    n1.getChildren(0)(0) shouldBe a[SequenceList[_, _]]

    val child: SequenceList[Int, Int] = n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]

    assert(child.getKeys.size == 1)
    assert(child.getKeys(0) == condition)
    assert(child.getSequence(condition).get.getKey == condition)
    assert(child.getSequence(condition).get.getEventCount == 1)
    assert(child.getSequence(condition).get.getEvents.size == 1)
    assert(child.getSequence(condition).get.getEvents(event) == 1)
    assert(child.getSequence(condition).get.getPredictions.size == 1)
    assert(child.getSequence(condition).get.getPredictions(event) == 1.00)
  }
  test("SMT, growTree called with Vector size == 2 | event") {
    val n1 = new Node[Int, Int](5, 1, 1)
    val condition = Vector(1, 2)
    val event = 666
    n1.growTree(condition, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 1)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[SequenceList[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    val child0: SequenceList[Int, Int] = n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]
    val child1: SequenceList[Int, Int] = n1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val child = n1.getChildren(i)(0).asInstanceOf[SequenceList[Int, Int]]
      assert(child.getKeys.size == 1)
      assert(child.getKeys(0) == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
    }
  }
  test("SMT, growTree called with Vector size == 3 | event") {
    val n1 = new Node[Int, Int](5, 1, 1)
    val condition = Vector(1, 2, 3)
    val event = 666
    n1.growTree(condition, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 1)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[SequenceList[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    val child0: SequenceList[Int, Int] = n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]
    val child1: SequenceList[Int, Int] = n1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val child = n1.getChildren(i)(0).asInstanceOf[SequenceList[Int, Int]]
      assert(child.getKeys.size == 1)
      assert(child.getKeys(0) == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
    }

  }
  test("SMT, growTree called with two 'Vector size == 2 | event' sequences") {
    val n1 = new Node[Int, Int](2, 1, 1)
    val condition = Vector(1, 2)
    val condition2 = Vector(3, 4)
    val event = 666
    n1.growTree(condition, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 1)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[SequenceList[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    val child0: SequenceList[Int, Int] = n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]
    val child1: SequenceList[Int, Int] = n1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val child = n1.getChildren(i)(0).asInstanceOf[SequenceList[Int, Int]]
      assert(child.getKeys.size == 1)
      assert(child.getKeys(0) == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
    }

    n1.growTree(condition2, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 2)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[Node[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_,_]]

    for (i <- n1.getChildren.indices) {
      val c = n1.getChildren(i)(0)

      c match {
        case child: SequenceList[Int,Int] => {
          assert(child.getKeys.size == 2)
          assert(child.getKeys(0) == condition.drop(i))
          assert(child.getKeys(1) == condition2.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
          assert(child.getSequence(condition2.drop(i)).get.getKey == condition2.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event) == 1.00)
        }
        case _: Node[Int,Int] => {
          assert(n1.getChildren(i).size == 2)
          n1.getChildren(i) shouldBe a[Vector[Node[_, _]]]

          for (j <- n1.getChildren(i).indices) {
            val curr = n1.getChildren(i)(j).asInstanceOf[Node[_, _]]

            if (j == 0) {
              assert(curr.getKey.get == condition.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 2)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666).get == 1.00)
              assert(currSeq(0).getProbability(777).getOrElse(0.00) == 0.00)
            }
            else if (j == 1) {
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 4)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666).get == 1.00)
              assert(currSeq(0).getProbability(777).getOrElse(0.00) == 0.00)
            }
          }
        }
      }
    }
  }
  test("SMT, growTree called with three 'Vector size == 2 | three events' sequences") {
    val n1 = new Node[Int, Int](2, 1, 1)
    val condition = Vector(1, 2)
    val condition2 = Vector(3, 4)
    val condition3 = Vector(5, 6)
    val event = 666
    val event2 = 777
    val event3 = 888

    n1.growTree(condition, event)
    n1.growTree(condition2, event2)
    n1.growTree(condition3, event3)

    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 3)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a [Node[Int, Int]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val c = n1.getChildren(i)(0)

      c match {
        case child: SequenceList[Int, Int] => {
          assert(child.getKeys.size == 3)
          assert(child.getKeys(0) == condition.drop(i))
          assert(child.getKeys(1) == condition2.drop(i))
          assert(child.getKeys(2) == condition3.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
          assert(child.getSequence(condition2.drop(i)).get.getKey == condition2.drop(i))
          assert(child.getSequence(condition3.drop(i)).get.getKey == condition3.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition3.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition3.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event2) == 1)
          assert(child.getSequence(condition3.drop(i)).get.getEvents(event3) == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event2) == 1.00)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions(event3) == 1.00)
        }
        case _: Node[Int, Int] => {
          assert(n1.getChildren(i).size == 3)
          n1.getChildren(i) shouldBe a[Vector[Node[_, _]]]

          for (j <- n1.getChildren(i).indices) {
            val curr = n1.getChildren(i)(j).asInstanceOf[Node[_, _]]

            if (j == 0) {
              assert(curr.getKey.get == condition.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 2)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666).get == 1.00)
              assert(currSeq(0).getProbability(777).getOrElse(0.00) == 0.00)
            }
            else if (j == 1) {
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 4)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(777).get == 1.00)
              assert(currSeq(0).getProbability(666).getOrElse(0.00) == 0.00)
            }
            else if (j == 2) {
              assert(curr.getKey.get == condition3.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 6)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(888).get == 1.00)
              assert(currSeq(0).getProbability(777).getOrElse(0.00) == 0.00)
            }
          }
        }
      }
    }
  }
  test("SMT, growTree called with three 'Vector size == 2 | three events' sequences twice each") {
    val n1 = new Node[Int, Int](2, 1, 1)
    val condition = Vector(1, 2)
    val condition2 = Vector(3, 4)
    val condition3 = Vector(5, 6)
    val event = 666
    val event2 = 777
    val event3 = 888

    n1.growTree(condition, event)
    n1.growTree(condition2, event2)
    n1.growTree(condition3, event3)

    n1.growTree(condition, event)
    n1.growTree(condition2, event2)
    n1.growTree(condition3, event3)

    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 3)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a [Node[Int, Int]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val c = n1.getChildren(i)(0)

      c match {
        case child: SequenceList[Int, Int] => {
          assert(child.getKeys.size == 3)
          assert(child.getKeys(0) == condition.drop(i))
          assert(child.getKeys(1) == condition2.drop(i))
          assert(child.getKeys(2) == condition3.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
          assert(child.getSequence(condition2.drop(i)).get.getKey == condition2.drop(i))
          assert(child.getSequence(condition3.drop(i)).get.getKey == condition3.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getEventCount == 2)
          assert(child.getSequence(condition2.drop(i)).get.getEventCount == 2)
          assert(child.getSequence(condition3.drop(i)).get.getEventCount == 2)
          assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition3.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 2)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event2) == 2)
          assert(child.getSequence(condition3.drop(i)).get.getEvents(event3) == 2)
          assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event2) == 1.00)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions(event3) == 1.00)
        }
        case _: Node[Int, Int] => {
          assert(n1.getChildren(i).size == 3)
          n1.getChildren(i) shouldBe a[Vector[Node[_, _]]]

          for (j <- n1.getChildren(i).indices) {
            val curr = n1.getChildren(i)(j).asInstanceOf[Node[_, _]]

            if (j == 0) {
              assert(curr.getKey.get == condition.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 2)
              assert(currSeq(0).getEventCount == 2)
              assert(currSeq(0).getEvents.size == 1)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666).get == 1.00)
              assert(currSeq(0).getProbability(777).getOrElse(0.00) == 0.00)
            }
            else if (j == 1) {
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 4)
              assert(currSeq(0).getEventCount == 2)
              assert(currSeq(0).getEvents.size == 1)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(777).get == 1.00)
              assert(currSeq(0).getProbability(666).getOrElse(0.00) == 0.00)
            }
            else if (j == 2) {
              assert(curr.getKey.get == condition3.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 6)
              assert(currSeq(0).getEventCount == 2)
              assert(currSeq(0).getEvents.size == 1)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(888).get == 1.00)
              assert(currSeq(0).getProbability(777).getOrElse(0.00) == 0.00)
            }
          }
        }
      }
    }
  }
  test("SMT, growTree called with three 'Vector size == 2 | three events' sequences twice each with different events for each") {
    val n1 = new Node[Int, Int](2, 1, 1)
    val condition = Vector(1, 2)
    val condition2 = Vector(3, 4)
    val condition3 = Vector(5, 6)
    val event = 666
    val event2 = 777
    val event3 = 888

    n1.growTree(condition, event)
    n1.growTree(condition2, event2)
    n1.growTree(condition3, event3)

    n1.growTree(condition, event3)
    n1.growTree(condition2, event)
    n1.growTree(condition3, event2)

    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 3)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a [Node[Int, Int]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val c = n1.getChildren(i)(0)

      c match {
        case child: SequenceList[Int, Int] => {
          assert(child.getKeys.size == 3)
          assert(child.getKeys(0) == condition.drop(i))
          assert(child.getKeys(1) == condition2.drop(i))
          assert(child.getKeys(2) == condition3.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
          assert(child.getSequence(condition2.drop(i)).get.getKey == condition2.drop(i))
          assert(child.getSequence(condition3.drop(i)).get.getKey == condition3.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getEventCount == 2)
          assert(child.getSequence(condition2.drop(i)).get.getEventCount == 2)
          assert(child.getSequence(condition3.drop(i)).get.getEventCount == 2)
          assert(child.getSequence(condition.drop(i)).get.getEvents.size == 2)
          assert(child.getSequence(condition2.drop(i)).get.getEvents.size == 2)
          assert(child.getSequence(condition3.drop(i)).get.getEvents.size == 2)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event3) == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event2) == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition3.drop(i)).get.getEvents(event3) == 1)
          assert(child.getSequence(condition3.drop(i)).get.getEvents(event2) == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 2)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions.size == 2)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions.size == 2)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 0.50)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event3) == 0.50)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event2) == 0.50)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event) == 0.50)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions(event3) == 0.50)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions(event2) == 0.50)
        }
        case _: Node[Int, Int] => {
          assert(n1.getChildren(i).size == 3)
          n1.getChildren(i) shouldBe a[Vector[Node[_, _]]]

          for (j <- n1.getChildren(i).indices) {
            val curr = n1.getChildren(i)(j).asInstanceOf[Node[_, _]]

            if (j == 0) {
              assert(curr.getKey.get == condition.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 2)
              assert(currSeq(0).getEventCount == 2)
              assert(currSeq(0).getEvents.size == 2)
              assert(currSeq(0).getPredictions.size == 2)
              assert(currSeq(0).getProbability(666).getOrElse(0.00) == 0.50)
              assert(currSeq(0).getProbability(888).getOrElse(0.00) == 0.50)
              assert(currSeq(0).getProbability(999).getOrElse(0.00) == 0.00)
            }
            else if (j == 1) {
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 4)
              assert(currSeq(0).getEventCount == 2)
              assert(currSeq(0).getEvents.size == 2)
              assert(currSeq(0).getPredictions.size == 2)
              assert(currSeq(0).getProbability(777).getOrElse(0.00) == 0.50)
              assert(currSeq(0).getProbability(666).getOrElse(0.00) == 0.50)
              assert(currSeq(0).getProbability(999).getOrElse(0.00) == 0.00)
            }
            else if (j == 2) {
              assert(curr.getKey.get == condition3.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 6)
              assert(currSeq(0).getEvents.size == 2)
              assert(currSeq(0).getPredictions.size == 2)
              assert(currSeq(0).getProbability(888).getOrElse(0.00) == 0.50)
              assert(currSeq(0).getProbability(777).getOrElse(0.00) == 0.50)
              assert(currSeq(0).getProbability(999).getOrElse(0.00) == 0.00)
            }
          }
        }
      }
    }
  }
  test("SMT, maxDepth 3 - growTree called with two 'Vector size == 2 | event'") {
    val n1 = new Node[Int, Int](3, 1, 1)
    val condition = Vector(1, 2)
    //val condition2 = Vector(1,4)
    val condition2 = Vector(3, 4)
    val event = 666

    n1.growTree(condition, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 1)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[SequenceList[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    val child0: SequenceList[Int, Int] = n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]
    val child1: SequenceList[Int, Int] = n1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val child = n1.getChildren(i)(0).asInstanceOf[SequenceList[Int, Int]]
      assert(child.getKeys.size == 1)
      assert(child.getKeys(0) == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
    }

    n1.growTree(condition2, event)

    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 2)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[Node[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    for (i <- n1.getChildren.indices) {
      val c = n1.getChildren(i)(0)

      c match {
        case child: SequenceList[Int, Int] => {
          assert(child.getKeys.size == 2)
          assert(child.getKeys(0) == condition.drop(i))
          assert(child.getKeys(1) == condition2.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
          assert(child.getSequence(condition2.drop(i)).get.getKey == condition2.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event) == 1.00)
        }
        case _: Node[Int, Int] => {
          assert(n1.getChildren(i).size == 2)
          n1.getChildren(i) shouldBe a[Vector[Node[_, _]]]

          for (j <- n1.getChildren(i).indices) {
            val curr = n1.getChildren(i)(j).asInstanceOf[Node[_, _]]

            if (j == 0) {
              assert(curr.getKey.get == condition.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 2)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666).get == 1.00)
              assert(currSeq(0).getProbability(777).getOrElse(0.00) == 0.00)
            }
            else if (j == 1) {
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 4)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666).get == 1.00)
              assert(currSeq(0).getProbability(777).getOrElse(0.00) == 0.00)
            }
          }

        }
      }
    }
  }
  test("SMT, maxDepth 3 - growTree called with two 'Vector size == 2 | event' - first element same") {
    val n1 = new Node[Int, Int](3, 1, 1)
    val condition = Vector(1, 2)
    val condition2 = Vector(1, 4)
    //val condition2 = Vector(3,4)
    val event = 666

    n1.growTree(condition, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 1)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[SequenceList[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    val child0: SequenceList[Int, Int] = n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]
    val child1: SequenceList[Int, Int] = n1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val child = n1.getChildren(i)(0).asInstanceOf[SequenceList[Int, Int]]
      assert(child.getKeys.size == 1)
      assert(child.getKeys(0) == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
    }

    n1.growTree(condition2, event)

    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 1)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[Node[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    for (i <- n1.getChildren.indices) {
      val c = n1.getChildren(i)(0)

      c match {
        case child: SequenceList[Int, Int] => {
          for (k <- child.getKeys) println(k)
          assert(child.getKeys(0) == condition.drop(i))
          assert(child.getKeys(1) == condition2.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
          assert(child.getSequence(condition2.drop(i)).get.getKey == condition2.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event) == 1.00)
        }
        case _: Node[Int, Int] => {
          assert(n1.getChildren(i).size == 1)
          n1.getChildren(i) shouldBe a[Vector[Node[_, _]]]

          for (j <- n1.getChildren(i).indices) {
            val curr = n1.getChildren(i)(j).asInstanceOf[Node[_, _]]

            if (j == 0) {
              assert(curr.getKey.get == condition.drop(i)(0))
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 2)
              assert(currSeq(0).getKey(0) == 2)
              assert(currSeq(1).getKey(0) == 4)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(1).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666).get == 1.00)
              assert(currSeq(1).getProbability(666).get == 1.00)
              assert(currSeq(0).getProbability(777).getOrElse(0.00) == 0.00)
              assert(currSeq(1).getProbability(777).getOrElse(0.00) == 0.00)
            }
            else if (j == 1) {
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 4)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666).get == 1.00)
              assert(currSeq(0).getProbability(777).getOrElse(0.00) == 0.00)
            }
          }
        }
      }
    }
  }
  test("SMT, mD = 5, window size 5") {
    val n1 = new Node[Int, Int](5, 2, 1)
    val event = 666
    val event2 = 777
    val event3 = 888

    val t1 = Vector(1, 2, 9, 4, 5)
    val t2 = Vector(5, 4, 3, 2, 1)
    val t3 = Vector(6, 7, 8, 9, 10)

    /*n1.growTree(shortListTrace, event)
    n1.growTree(shortListTrace2, event)*/
    n1.growTree(t1, event)
    n1.growTree(t2, event)

    assert(n1.getChildren.size == 3)
    assert(n1.getChildren(0).size == 2)
    assert(n1.getChildren(1).size == 2)
    assert(n1.getChildren(2).size == 2)

    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")

    println("n1.getChildren(2):")
    println("n1.getChildren(2) size: " + n1.getChildren(2).size)
    println("n1.getChildren(2)(0): ")
    println(n1.getChildren(2)(0))


    /*  println()
    for (s <- n1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].sequences){
      println("sequence key: " + s.getKey)
    }*/


    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")

    val phi_0 = n1.getChildren(0)
    val phi_0_node_0 = phi_0(0).asInstanceOf[Node[Int, Int]]
    val phi_0_node_1 = phi_0(1).asInstanceOf[Node[Int, Int]]

    val phi_1 = n1.getChildren(1)
    val phi_1_node_0 = phi_1(0).asInstanceOf[Node[Int, Int]]
    val phi_1_node_1 = phi_1(1).asInstanceOf[Node[Int, Int]]

    val phi_2 = n1.getChildren(2)
    val phi_2_node_0 = phi_2(0).asInstanceOf[Node[Int, Int]]
    val phi_2_node_1 = phi_2(1).asInstanceOf[Node[Int, Int]]

    assert(phi_0_node_0.getKey.get == t1.head)
    assert(phi_0_node_0.getChildren.size == 3)
    assert(phi_0_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(1))
    assert(phi_0_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(2))
    assert(phi_0_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_0_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)


    assert(phi_0_node_1.getKey.get == t2.head)
    assert(phi_0_node_1.getChildren.size == 3)
    assert(phi_0_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(1))
    assert(phi_0_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(2))
    assert(phi_0_node_1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_0_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)


    assert(phi_1_node_0.getKey.get == t1.drop(1).head)
    assert(phi_1_node_0.getChildren.size == 3)
    assert(phi_1_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(2))
    assert(phi_1_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_1_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(4))
    assert(phi_1_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_1_node_1.getKey.get == t2.drop(1).head)
    assert(phi_1_node_1.getChildren.size == 3)
    assert(phi_1_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(2))
    assert(phi_1_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_1_node_1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(4))
    assert(phi_1_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_2_node_0.getKey.get == t1.drop(2).head)
    assert(phi_2_node_0.getChildren.size == 2)
    assert(phi_2_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_2_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(4))
    assert(phi_2_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_2_node_1.getKey.get == t2.drop(2).head)
    assert(phi_2_node_1.getChildren.size == 2)
    assert(phi_2_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_2_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(4))
    assert(phi_2_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    n1.growTree(t3, event2)

    assert(n1.getChildren.size == 3)
    assert(n1.getChildren(0).size == 3)
    assert(n1.getChildren(1).size == 3)
    assert(n1.getChildren(2).size == 3)

    val newPhi_0 = n1.getChildren(0)
    val newPhi_1 = n1.getChildren(1)
    val newPhi_2 = n1.getChildren(2)

    val phi_0_node_2 = newPhi_0(2).asInstanceOf[Node[Int, Int]]
    val phi_1_node_2 = newPhi_1(2).asInstanceOf[Node[Int, Int]]
    val phi_2_node_2 = newPhi_2(2).asInstanceOf[Node[Int, Int]]

    assert(phi_0_node_0.getKey.get == t1.head)
    assert(phi_0_node_0.getChildren.size == 3)
    assert(phi_0_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(1))
    assert(phi_0_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(2))
    assert(phi_0_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_0_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_0_node_1.getKey.get == t2.head)
    assert(phi_0_node_1.getChildren.size == 3)
    assert(phi_0_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(1))
    assert(phi_0_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(2))
    assert(phi_0_node_1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_0_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_0_node_2.getKey.get == t3.head)
    assert(phi_0_node_2.getChildren.size == 3)
    assert(phi_0_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(1))
    assert(phi_0_node_2.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(2))
    assert(phi_0_node_2.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(3))
    assert(phi_0_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val newSeq0 = phi_0_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(t3.drop(1)).get
    assert(newSeq0.getKey == t3.drop(1))
    assert(newSeq0.getEventCount == 1)
    assert(newSeq0.getEvents(event2) == 1)
    assert(newSeq0.getProbability(event2).getOrElse(0.00) == 1.00)

    assert(phi_1_node_0.getKey.get == t1.drop(1).head)
    assert(phi_1_node_0.getChildren.size == 3)
    assert(phi_1_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(2))
    assert(phi_1_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_1_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(4))
    assert(phi_1_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_1_node_1.getKey.get == t2.drop(1).head)
    assert(phi_1_node_1.getChildren.size == 3)
    assert(phi_1_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(2))
    assert(phi_1_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_1_node_1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(4))
    assert(phi_1_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_1_node_2.getKey.get == t3.drop(1).head)
    assert(phi_1_node_2.getChildren.size == 3)
    assert(phi_1_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(2))
    assert(phi_1_node_2.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(3))
    assert(phi_1_node_2.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(4))
    assert(phi_1_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val newSeq1 = phi_1_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(t3.drop(2)).get
    assert(newSeq1.getKey == t3.drop(2))
    assert(newSeq1.getEventCount == 1)
    assert(newSeq1.getEvents(event2) == 1)
    assert(newSeq1.getProbability(event2).getOrElse(0.00) == 1.00)

    assert(phi_2_node_0.getKey.get == t1.drop(2).head)
    assert(phi_2_node_0.getChildren.size == 2)
    assert(phi_2_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_2_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(4))
    assert(phi_2_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_2_node_1.getKey.get == t2.drop(2).head)
    assert(phi_2_node_1.getChildren.size == 2)
    assert(phi_2_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_2_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(4))
    assert(phi_2_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_2_node_2.getKey.get == t3.drop(2).head)
    assert(phi_2_node_2.getChildren.size == 2)
    assert(phi_2_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(3))
    assert(phi_2_node_2.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(4))
    assert(phi_2_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val newSeq2 = phi_2_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(t3.drop(3)).get
    assert(newSeq2.getKey == t3.drop(3))
    assert(newSeq2.getEventCount == 1)
    assert(newSeq2.getEvents(event2) == 1)
    assert(newSeq2.getProbability(event2).getOrElse(0.00) == 1.00)


    n1.growTree(t3, event)

    assert(n1.getChildren.size == 3)
    assert(n1.getChildren(0).size == 3)
    assert(n1.getChildren(1).size == 3)
    assert(n1.getChildren(2).size == 3)

    val newNewPhi_0 = n1.getChildren(0)
    val newNewPhi_1 = n1.getChildren(1)
    val newNewPhi_2 = n1.getChildren(2)

    val newPhi_0_node_2 = newNewPhi_0(2).asInstanceOf[Node[Int, Int]]
    val newPhi_1_node_2 = newNewPhi_1(2).asInstanceOf[Node[Int, Int]]
    val newPhi_2_node_2 = newNewPhi_2(2).asInstanceOf[Node[Int, Int]]

    assert(phi_0_node_0.getKey.get == t1.head)
    assert(phi_0_node_0.getChildren.size == 3)
    assert(phi_0_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(1))
    assert(phi_0_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(2))
    assert(phi_0_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_0_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_0_node_1.getKey.get == t2.head)
    assert(phi_0_node_1.getChildren.size == 3)
    assert(phi_0_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(1))
    assert(phi_0_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(2))
    assert(phi_0_node_1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_0_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(newPhi_0_node_2.getKey.get == t3.head)
    assert(newPhi_0_node_2.getChildren.size == 3)
    assert(newPhi_0_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(1))
    assert(newPhi_0_node_2.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(2))
    assert(newPhi_0_node_2.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(3))
    assert(newPhi_0_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val newNewSeq0 = newPhi_0_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(t3.drop(1)).get
    assert(newNewSeq0.getKey == t3.drop(1))
    assert(newNewSeq0.getEventCount == 2)
    assert(newNewSeq0.getEvents(event2) == 1)
    assert(newNewSeq0.getEvents(event) == 1)
    assert(newNewSeq0.getProbability(event2).getOrElse(0.00) == 0.50)
    assert(newNewSeq0.getProbability(event).getOrElse(0.00) == 0.50)

    assert(phi_1_node_0.getKey.get == t1.drop(1).head)
    assert(phi_1_node_0.getChildren.size == 3)
    assert(phi_1_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(2))
    assert(phi_1_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_1_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(4))
    assert(phi_1_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_1_node_1.getKey.get == t2.drop(1).head)
    assert(phi_1_node_1.getChildren.size == 3)
    assert(phi_1_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(2))
    assert(phi_1_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_1_node_1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(4))
    assert(phi_1_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(newPhi_1_node_2.getKey.get == t3.drop(1).head)
    assert(newPhi_1_node_2.getChildren.size == 3)
    assert(newPhi_1_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(2))
    assert(newPhi_1_node_2.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(3))
    assert(newPhi_1_node_2.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(4))
    assert(newPhi_1_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val newNewSeq1 = newPhi_1_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(t3.drop(2)).get
    assert(newNewSeq1.getKey == t3.drop(2))
    assert(newNewSeq1.getEventCount == 2)
    assert(newNewSeq1.getEvents(event2) == 1)
    assert(newNewSeq1.getEvents(event) == 1)
    assert(newNewSeq1.getProbability(event2).getOrElse(0.00) == 0.50)
    assert(newNewSeq1.getProbability(event).getOrElse(0.00) == 0.50)

    assert(phi_2_node_0.getKey.get == t1.drop(2).head)
    assert(phi_2_node_0.getChildren.size == 2)
    assert(phi_2_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_2_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(4))
    assert(phi_2_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_2_node_1.getKey.get == t2.drop(2).head)
    assert(phi_2_node_1.getChildren.size == 2)
    assert(phi_2_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_2_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(4))
    assert(phi_2_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(newPhi_2_node_2.getKey.get == t3.drop(2).head)
    assert(newPhi_2_node_2.getChildren.size == 2)
    assert(newPhi_2_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(3))
    assert(newPhi_2_node_2.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(4))
    assert(newPhi_2_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val newNewSeq2 = newPhi_2_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(t3.drop(3)).get
    assert(newNewSeq2.getKey == t3.drop(3))
    assert(newNewSeq2.getEventCount == 2)
    assert(newNewSeq2.getEvents(event2) == 1)
    assert(newNewSeq2.getEvents(event) == 1)
    assert(newNewSeq2.getProbability(event2).getOrElse(0.00) == 0.50)
    assert(newNewSeq2.getProbability(event).getOrElse(0.00) == 0.50)


    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")

    println("n1:")
    println(n1)



    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")
    println("888888888888888888888888888888888888888")

    //TODO - CAUSE ONE MORE SPLIT FROM EXISTING NODE
  }


  /* test("asdfsfd"){
    var childrenGroup: Vector[Vector[SMT[_ <: Int, _ <: Int]]] = Vector[Vector[SMT[Int, Int]]]()
    childrenGroup = childrenGroup :+ Vector(SequenceList[Int, Int](11,11,11))
    println("childrenGroup.size " + childrenGroup.size)
    println("childreanGroup: " + childrenGroup)
    println("childrenGroup(0): " + childrenGroup(0))

    val nV = Vector(Node[Int, Int](22,22,22), Node[Int, Int](66,66,66))
    //childrenGroup = childrenGroup.updated(0, nV)
    childrenGroup = childrenGroup.updated(0, nV)
    println("after: ")
    println("childrenGroup.size " + childrenGroup.size)
    println("---------------------childrenGroup-------------")
    println("childreanGroup: " + childrenGroup)
    /*println("---------------------childrenGroup-------------")
    println("childrenGroup(0): " + childrenGroup(0))*/


    //childrenGroup(0) = childrenGroup(0)

    childrenGroup = childrenGroup :+ Vector(Node[Int, Int](7777777,7777777,7777777))

    println("????????????????????????????????????????????????????? after append: ")
    println("childrenGroup.size " + childrenGroup.size)
    println("---------------------childrenGroup-------------")
    println("childreanGroup: " + childrenGroup)
    /*println("---------------------childrenGroup-------------")
    println("childrenGroup(0): " + childrenGroup(0))*/

  }*/


  test("temp") {
    val n1 = new Node[Int, Int](1, 1, 1)
    val condition = Vector(1, 2)
    val condition2 = Vector(3, 4)
    val event = 666
    n1.growTree(condition, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 1)
    assert(n1.getChildren(0).size == 1)
    n1.getChildren(0)(0) shouldBe a[SequenceList[_, _]]

    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys.size == 1)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences(0).getKey == condition)
  }


  /*test("sliding window test") {
    val n1 = new Node[Int, Int](5, 3, 1)
    var count: Int = 0
    for (t <- intVectorTrace.sliding(5, 1)) {
      count += 1
      n1.growTree(t, t.last)
    }

    println("--------------------------n1--------------------------")
    println(n1)
    println("--------------------------n1--------------------------")
    println("count : " + count)

  }*/
}
