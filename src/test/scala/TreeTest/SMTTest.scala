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
  val shortListTrace = Vector(1, 2, 3, 4, 5)
  val shortListTrace2 = Vector(5, 4, 3, 2, 1)
  val shortListTrace3 = Vector(1, 2, 3)
  val shortListTrace4 = Vector(4, 5, 6)
  val shortMap = Map(1 -> 0.1, 2 -> 0.2, 3 -> 0.3)
  val shortStringListTrace = Vector("ntdll.dll+0x2173e, ntdll.dll+0x21639, ntdll.dll+0xeac7, kernel32.dll+0x15568, comctl32.dll+0x8ac2d")
  val shortStringMap = Map("ntdll.dll+0x2173e" -> 0.1, "ntdll.dll+0x21639" -> 0.2, "ntdll.dll+0xeac7" -> 0.3)


  /*override def beforeAll(): Unit = {

  }*/

  test("SMT. maxDepth is not zero"){
    assertThrows[IllegalArgumentException](Node(0, 1, 3))
    val caught = intercept[IllegalArgumentException](Node(0, 1, 3))
    assert(caught.getMessage == "requirement failed: Max depth count must be positive!")
  }
  test("SMT. maxDepth is not negative"){
    assertThrows[IllegalArgumentException](Node(-1, 1, 3))
    val caught = intercept[IllegalArgumentException](Node(-1, 1, 3))
    assert(caught.getMessage == "requirement failed: Max depth count must be positive!")
  }
  test("SMT. maxPhi is non-negative"){
    assertThrows[IllegalArgumentException](Node(1, -1, 3))
    val caught = intercept[IllegalArgumentException](Node(1, -1, 3))
    assert(caught.getMessage == "requirement failed: Max Phi count must be non-negative!")
  }
  test("SMT. maxSeqCount is not zero"){
    assertThrows[IllegalArgumentException](Node(1, 1, 0))
    val caught = intercept[IllegalArgumentException](Node(1, 1, 0))
    assert(caught.getMessage == "requirement failed: Max sequence count must be positive!")
  }
  test("SMT. maxSeqCount is not negative"){
    assertThrows[IllegalArgumentException](Node(1, 1, -1))
    val caught = intercept[IllegalArgumentException](Node(1, 1, -1))
    assert(caught.getMessage == "requirement failed: Max sequence count must be positive!")
  }
  test("SMT. Default key is None"){
    val root: Node[Int, Int] = Node(5, 1, 3)
    root.getKey shouldBe None
  }
  test("SMT. setKey sets correct Int key"){
    val root: Node[Int, Int] = Node(5, 1, 3)
    root.getKey shouldBe None
    root.setKey(0)
    root.getKey shouldBe 'defined
    assert(root.getKey.get == 0)
  }
  test("SMT. setKey sets correct String key"){
    val root: Node[String, Int] = Node(5, 1, 3)
    root.getKey shouldBe None
    root.setKey("TestKey")
    root.getKey shouldBe 'defined
    assert(root.getKey.get equals "TestKey")
  }
  test("SMT. setKey fails if called multiple times - String key"){
    val root: Node[String, Int] = Node(5, 1, 3)
    root.setKey("TestKey")
    root.getKey shouldBe 'defined
    assert(root.getKey.get equals "TestKey")

    val caught = intercept[IllegalStateException](root.setKey("NewTestKey"))
    assert(caught.getMessage == "Node key cannot be reset!")
  }
  test("SMT. setKey fails if called multiple times - Int key"){
    val root: Node[Int, Int] = Node(5, 1, 3)
    root.setKey(0)
    root.getKey shouldBe 'defined
    assert(root.getKey.get == 0)

    val caught = intercept[IllegalStateException](root.setKey(222))
    assert(caught.getMessage equals "Node key cannot be reset!")
  }
  test("SMT eventCount is correct"){
    val n1 = new Node[Int, Int](5,1,1)
    assert(n1.getEventCount == 0)
    n1.updateEvents(666)
    assert(n1.getEventCount == 1)
    n1.updateEvents(666)
    assert(n1.getEventCount == 2)
    n1.updateEvents(777)
    assert(n1.getEventCount == 3)
  }
  test("SMT, events initialised empty"){
    val n1 = new Node[Int, Int](5,1,1)
    assertThrows[NoSuchElementException](n1.getEvents(666))
    assert(n1.getEvents.isEmpty)
  }
  test("SMT 'events' is correct after updates with same event") {
    val n1 = new Node[Int, Int](5,1,1)

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
    val n1 = new Node[Int, Int](5,1,1)

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
    assert(n1.getProbability(666).get == 3.00/4)
    assert(n1.getProbability(777).get == 1.00/4)

    n1.updateEvents(777)
    assert(n1.getEventCount == 5)
    assert(n1.getEvents.size == 2)
    assert(n1.getEvents.get(666).get == 3)
    assert(n1.getEvents.get(777).get == 2)
    assert(n1.getProbability(666).get == 3.00/5)
    assert(n1.getProbability(777).get == 2.00/5)

    n1.updateEvents(888)
    assert(n1.getEventCount == 6)
    assert(n1.getEvents.size == 3)
    assert(n1.getEvents.get(666).get == 3)
    assert(n1.getEvents.get(777).get == 2)
    assert(n1.getEvents.get(888).get == 1)
    assert(n1.getProbability(666).get == 3.00/6)
    assert(n1.getProbability(777).get == 2.00/6)
    assert(n1.getProbability(888).get == 1.00/6)
  }
  test("SMT predictions has single element after first event update"){
    val n1 = new Node[Int, Int](5,1,1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
  }
  test("SMT predictions has two elements after two different event key updates"){
    val n1 = new Node[Int, Int](5,1,1)

    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(777)
    assert(n1.getPredictions.size == 2)
  }
  test("SMT predictions has one element after the same event key is updated twice"){
    val n1 = new Node[Int, Int](5,1,1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
  }
  test("SMT predictions has two elements after adding three events, two the same"){
    val n1 = new Node[Int, Int](5,1,1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(777)
    assert(n1.getPredictions.size == 2)
  }
  test("SMT predictions are correct"){
    val n1 = new Node[Int, Int](5,1,1)
    n1.updateEvents(666)
    assert(n1.getPredictions(666) == 1.0)
    n1.updateEvents(666)
    assert(n1.getPredictions(666) == 1.0)
    n1.updateEvents(777)
    assert(n1.getPredictions(777) == 1.0/3)
    n1.updateEvents(777)
    assert(n1.getPredictions(777) == 2.0/4)
    n1.updateEvents(888)
    assert(n1.getPredictions(666) == 2.0/5)
    assert(n1.getPredictions(777) == 2.0/5)
    assert(n1.getPredictions(888) == 1.0/5)
    n1.updateEvents(999)
    assert(n1.getPredictions(666) == 2.0/6)
    assert(n1.getPredictions(777) == 2.0/6)
    assert(n1.getPredictions(888) == 1.0/6)
    assert(n1.getPredictions(999) == 1.0/6)
  }
  test("SMT getProbability for single event"){
    val n1 = new Node[Int, Int](5,1,1)
    n1.updateEvents(666)
    assert(n1.getProbability(666).get == 1.0)
  }
  test("SMT getProbability for events after multiple updates"){
    val n1 = new Node[Int, Int](5,1,1)
    n1.updateEvents(666)
    assert(n1.getProbability(666).get == 1.0)
    n1.updateEvents(777)
    assert(n1.getProbability(666).get == 0.5)
    assert(n1.getProbability(777).get == 0.5)

    n1.updateEvents(888)
    assert(n1.getProbability(666).get == 1.0/3)
    assert(n1.getProbability(777).get == 1.0/3)
    assert(n1.getProbability(888).get == 1.0/3)
    n1.updateEvents(888)
    assert(n1.getProbability(666).get == 1.0/4)
    assert(n1.getProbability(777).get == 1.0/4)
    assert(n1.getProbability(888).get == 2.0/4)
    n1.updateEvents(999)
    assert(n1.getProbability(666).get == 1.0/5)
    assert(n1.getProbability(777).get == 1.0/5)
    assert(n1.getProbability(888).get == 2.0/5)
    assert(n1.getProbability(999).get == 1.0/5)
    n1.updateEvents(666)
    assert(n1.getProbability(666).get == 2.0/6)
    assert(n1.getProbability(777).get == 1.0/6)
    assert(n1.getProbability(888).get == 2.0/6)
    assert(n1.getProbability(999).get == 1.0/6)
  }
  test("SMT getProbability for String events after multiple updates. (String key)"){
    val n1 = new Node[Int, String](5,1,1)
    n1.updateEvents("ntdll.dll+0x2173e")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0)
    n1.updateEvents("ntdll.dll+0x21639")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 0.5)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 0.5)
    n1.updateEvents("ntdll.dll+0xeac7")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0/3)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0/3)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 1.0/3)
    n1.updateEvents("ntdll.dll+0xeac7")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0/4)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0/4)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 2.0/4)
    n1.updateEvents("kernel32.dll+0x15568")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0/5)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0/5)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 2.0/5)
    assert(n1.getProbability("kernel32.dll+0x15568").get == 1.0/5)
    n1.updateEvents("ntdll.dll+0x2173e")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 2.0/6)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0/6)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 2.0/6)
    assert(n1.getProbability("kernel32.dll+0x15568").get == 1.0/6)
  }
  test("Sequence getProbability for String events after multiple updates. (Int key)"){
    val n1 = new Node[Int, String](5,1,1)
    n1.updateEvents("ntdll.dll+0x2173e")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0)
    n1.updateEvents("ntdll.dll+0x21639")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 0.5)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 0.5)
    n1.updateEvents("ntdll.dll+0xeac7")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0/3)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0/3)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 1.0/3)
    n1.updateEvents("ntdll.dll+0xeac7")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0/4)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0/4)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 2.0/4)
    n1.updateEvents("kernel32.dll+0x15568")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 1.0/5)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0/5)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 2.0/5)
    assert(n1.getProbability("kernel32.dll+0x15568").get == 1.0/5)
    n1.updateEvents("ntdll.dll+0x2173e")
    assert(n1.getProbability("ntdll.dll+0x2173e").get == 2.0/6)
    assert(n1.getProbability("ntdll.dll+0x21639").get == 1.0/6)
    assert(n1.getProbability("ntdll.dll+0xeac7").get == 2.0/6)
    assert(n1.getProbability("kernel32.dll+0x15568").get == 1.0/6)
  }
  test("SMT eventCount == events sum in events"){
    val n1 = new Node[Int, Int](5,1,1)
    n1.updateEvents(666)
    assert(n1.getEventCount == 1)
    var eventNum = 0
    for(e <- n1.getEvents){
      eventNum += e._2
    }
    assert(eventNum == n1.getEventCount)

    n1.updateEvents(666)
    assert(n1.getEventCount == 2)
    var eventNum2 = 0
    for(e <- n1.getEvents){
      eventNum2 += e._2
    }
    assert(eventNum2 == n1.getEventCount)
    assert(eventNum2 == 2)
    assert(2 == n1.getEventCount)

    n1.updateEvents(777)
    assert(n1.getEventCount == 3)
    var eventNum3 = 0
    for(e <- n1.getEvents){
      eventNum3 += e._2
    }
    assert(eventNum3 == n1.getEventCount)
    assert(eventNum3 == 3)
    assert(3 == n1.getEventCount)

    n1.updateEvents(888)
    assert(n1.getEventCount == 4)
    var eventNum4 = 0
    for(e <- n1.getEvents){
      eventNum4 += e._2
    }
    assert(eventNum4 == n1.getEventCount)
    assert(eventNum4 == 4)
    assert(4 == n1.getEventCount)
  }
  //SMT ----------------------------------
  test("SMT, invalid argument to growTree: empty Vector| event"){
    val n1 = new Node[Int, Int](5,1,1)
    n1.growTree(Vector.empty[Int], 666)
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    assert(n1.getChildren.isEmpty)
  }
  test("SMT, growTree called with Vector size == 1 | event"){
    val n1 = new Node[Int, Int](5,1,1)
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
    n1.getChildren(0)(0) shouldBe a [SequenceList[Int, Int]]

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



}