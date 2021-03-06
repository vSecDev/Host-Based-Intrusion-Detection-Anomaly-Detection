package DecisionEngine.SMT

import org.scalatest.FunSuite

/**
  * Created by Case on 02/07/2017.
  */
class NodeTest extends FunSuite{

  test("Node, smoothing is non-negative"){
    val caught = intercept[IllegalArgumentException](new Node[Int, Int](1,1,1,-1.0,1.0))
    assert(caught.getMessage == "requirement failed: Node smoothing value must be non-negative!")
  }
  test("Node, prior weight is not zero"){
    val caught = intercept[IllegalArgumentException](new Node[Int, Int](1,1,1,1.0,0.0))
    assert(caught.getMessage == "requirement failed: Node prior weight must be larger than zero!")
  }
  test("Node, prior weight is not negative"){
    val caught = intercept[IllegalArgumentException](new Node[Int, Int](1,1,1,1.0,-1.0))
    assert(caught.getMessage == "requirement failed: Node prior weight must be larger than zero!")
  }
  test("Node with 'None' predictions returns None for getProbability - zero smoothing") {
    val n = new Node[Int, Int](1, 1, 1, 0.0,1.0)
    val num = 1
    assert(n.getProbability(num) == 0.0)
  }
  test("Node initialises without key"){
    val n = new Node[Int, Int](1, 1, 1, 0.0, 1.0)
    assert(n.getKey.isEmpty)
  }
  test("Node setKey works"){
    val n = new Node[Int, Int](1, 1, 1, 0.0, 1.0)
    n.setKey(2)
    assert(n.getKey.get == 2)
  }
  test("Node key cannot be reset"){
    val n = new Node[Int, Int](1, 1, 1,  0.0, 1.0)
    n.setKey(2)
    assertThrows[IllegalStateException](n.setKey(3))
  }
  test("Node correct error message for invalid key reset"){
    val n = new Node[Int, Int](1, 1, 1,  0.0, 1.0)
    n.setKey(2)
    val caught = intercept[IllegalStateException](n.setKey(3))
    assert(caught.getMessage == "Node key cannot be reset!")
  }
  test("Node value update"){
    val n = new Node[Int, Int](1, 1, 1,  0.0, 1.0)
    n.setKey(2)
    assert(n.getEventCount == 0)
    assert(n.getEvents.isEmpty)

    n.updateEvents(666)
    assert(n.getEventCount == 1)
    assert(n.getEvents(666) == 1)
    assert(n.getEvents.get(777).isEmpty)


    n.updateEvents(666)
    assert(n.getEventCount == 2)
    assert(n.getEvents(666) == 2)
    assert(n.getEvents.get(777).isEmpty)

    n.updateEvents(777)
    assert(n.getEventCount == 3)
    assert(n.getEvents(666) == 2)
    assert(n.getEvents(777) == 1)
  }
  test("Node updatePredictions is none before events are added"){
    val n = new Node[Int, Int](1, 1, 1,  0.0, 1.0)
    n.setKey(2)
    assert(n.getPredictions.isEmpty)
  }
  test("Node updatePrediction has single element after first event update"){
    val n = new Node[Int, Int](1, 1, 1,  0.0, 1.0)
    n.setKey(2)
    n.updateEvents(666)
    assert(n.getPredictions.size == 1)
  }
  test("Node updatePrediction has two elements after two different event key updates"){
    val n = new Node[Int, Int](1, 1, 1,  0.0, 1.0)
    n.setKey(2)
    n.updateEvents(666)
    assert(n.getPredictions.size == 1)
    n.updateEvents(777)
    assert(n.getPredictions.size == 2)
  }
  test("Node updatePrediction has one element after the same event key is updated twice"){
    val n = new Node[Int, Int](1, 1, 1,  0.0, 1.0)
    n.setKey(2)
    n.updateEvents(666)
    assert(n.getPredictions.size == 1)
    n.updateEvents(666)
    assert(n.getPredictions.size == 1)
    n.updateEvents(666)
    assert(n.getPredictions.size == 1)
  }
  test("Node updatePrediction has two elements after adding three events, two the same"){
    val n = new Node[Int, Int](1, 1, 1,  0.0, 1.0)
    n.setKey(2)
    n.updateEvents(666)
    assert(n.getPredictions.size == 1)
    n.updateEvents(666)
    assert(n.getPredictions.size == 1)
    n.updateEvents(777)
    assert(n.getPredictions.size == 2)
  }
  test("Node predictions are correct"){
    val n = new Node[Int, Int](1, 1, 1,  0.0, 1.0)
    n.setKey(2)
    n.updateEvents(666)
    assert(n.getPredictions(666) == 1.0)
    n.updateEvents(666)
    assert(n.getPredictions(666) == 1.0)
    n.updateEvents(777)
    assert(n.getPredictions(777) == 1.0/3)
    n.updateEvents(777)
    assert(n.getPredictions(777) == 2.0/4)
    n.updateEvents(888)
    assert(n.getPredictions(666) == 2.0/5)
    assert(n.getPredictions(777) == 2.0/5)
    assert(n.getPredictions(888) == 1.0/5)
    n.updateEvents(999)
    assert(n.getPredictions(666) == 2.0/6)
    assert(n.getPredictions(777) == 2.0/6)
    assert(n.getPredictions(888) == 1.0/6)
    assert(n.getPredictions(999) == 1.0/6)
  }
  test("Node getProbability for single event"){
    val n = new Node[Int, Int](1, 1, 1,  0.0, 1.0)
    n.setKey(2)
    n.updateEvents(666)
    assert(n.getProbability(666) == 1.0)
  }
  test("Node getProbability for events after multiple updates"){
    val n = new Node[Int, Int](1, 1, 1,  0.0, 1.0)
    n.setKey(2)
    n.updateEvents(666)
    assert(n.getProbability(666) == 1.0)
    n.updateEvents(777)
    assert(n.getProbability(666) == 0.5)
    assert(n.getProbability(777) == 0.5)
    n.updateEvents(888)
    assert(n.getProbability(666) == 1.0/3)
    assert(n.getProbability(777) == 1.0/3)
    assert(n.getProbability(777) == 1.0/3)
    n.updateEvents(888)
    assert(n.getProbability(666) == 1.0/4)
    assert(n.getProbability(777) == 1.0/4)
    assert(n.getProbability(888) == 2.0/4)
    n.updateEvents(999)
    assert(n.getProbability(666) == 1.0/5)
    assert(n.getProbability(777) == 1.0/5)
    assert(n.getProbability(888) == 2.0/5)
    assert(n.getProbability(999) == 1.0/5)
    n.updateEvents(666)
    assert(n.getProbability(666) == 2.0/6)
    assert(n.getProbability(777) == 1.0/6)
    assert(n.getProbability(888) == 2.0/6)
    assert(n.getProbability(999) == 1.0/6)
  }
  test("Node getProbability for String events after multiple updates. (String key)"){
    val n = new Node[String, String](1,1,1, 0.0, 1.0)
    n.setKey("ntdll.dll+0x2173e")
    n.updateEvents("ntdll.dll+0x2173e")
    assert(n.getProbability("ntdll.dll+0x2173e") == 1.0)
    n.updateEvents("ntdll.dll+0x21639")
    assert(n.getProbability("ntdll.dll+0x2173e") == 0.5)
    assert(n.getProbability("ntdll.dll+0x21639")== 0.5)
    n.updateEvents("ntdll.dll+0xeac7")
    assert(n.getProbability("ntdll.dll+0x2173e") == 1.0/3)
    assert(n.getProbability("ntdll.dll+0x21639")== 1.0/3)
    assert(n.getProbability("ntdll.dll+0xeac7") == 1.0/3)
    n.updateEvents("ntdll.dll+0xeac7")
    assert(n.getProbability("ntdll.dll+0x2173e") == 1.0/4)
    assert(n.getProbability("ntdll.dll+0x21639")== 1.0/4)
    assert(n.getProbability("ntdll.dll+0xeac7") == 2.0/4)
    n.updateEvents("kernel32.dll+0x15568")
    assert(n.getProbability("ntdll.dll+0x2173e") == 1.0/5)
    assert(n.getProbability("ntdll.dll+0x21639")== 1.0/5)
    assert(n.getProbability("ntdll.dll+0xeac7") == 2.0/5)
    assert(n.getProbability("kernel32.dll+0x15568") == 1.0/5)
    n.updateEvents("ntdll.dll+0x2173e")
    assert(n.getProbability("ntdll.dll+0x2173e") == 2.0/6)
    assert(n.getProbability("ntdll.dll+0x21639")== 1.0/6)
    assert(n.getProbability("ntdll.dll+0xeac7") == 2.0/6)
    assert(n.getProbability("kernel32.dll+0x15568") == 1.0/6)
  }
  test("Node getProbability for String events after multiple updates. (Int key)"){
    val n = new Node[Int, String](1,1,1, 0.0, 1.0)
    n.setKey(1)
    n.updateEvents("ntdll.dll+0x2173e")
    assert(n.getProbability("ntdll.dll+0x2173e") == 1.0)
    n.updateEvents("ntdll.dll+0x21639")
    assert(n.getProbability("ntdll.dll+0x2173e") == 0.5)
    assert(n.getProbability("ntdll.dll+0x21639")== 0.5)
    n.updateEvents("ntdll.dll+0xeac7")
    assert(n.getProbability("ntdll.dll+0x2173e") == 1.0/3)
    assert(n.getProbability("ntdll.dll+0x21639")== 1.0/3)
    assert(n.getProbability("ntdll.dll+0xeac7") == 1.0/3)
    n.updateEvents("ntdll.dll+0xeac7")
    assert(n.getProbability("ntdll.dll+0x2173e") == 1.0/4)
    assert(n.getProbability("ntdll.dll+0x21639")== 1.0/4)
    assert(n.getProbability("ntdll.dll+0xeac7") == 2.0/4)
    n.updateEvents("kernel32.dll+0x15568")
    assert(n.getProbability("ntdll.dll+0x2173e") == 1.0/5)
    assert(n.getProbability("ntdll.dll+0x21639")== 1.0/5)
    assert(n.getProbability("ntdll.dll+0xeac7") == 2.0/5)
    assert(n.getProbability("kernel32.dll+0x15568") == 1.0/5)
    n.updateEvents("ntdll.dll+0x2173e")
    assert(n.getProbability("ntdll.dll+0x2173e") == 2.0/6)
    assert(n.getProbability("ntdll.dll+0x21639")== 1.0/6)
    assert(n.getProbability("ntdll.dll+0xeac7") == 2.0/6)
    assert(n.getProbability("kernel32.dll+0x15568") == 1.0/6)
  }
  test("Predict with untrained node"){

    val condition1 = Vector(1, 2, 3)
    val condition2 = Vector(4, 5, 6)
    val condition3 = Vector(7, 8, 9)
    val event1 = 666
    val event2 = 777
    val event3 = 888

    val n = new Node[Int, Int](1,1,1, 0.0, 1.0)
    var r1 = n.predict(condition1, event1)
    var r2 = n.predict(condition1, event2)
    println("r1: " + r1)
    println("r2: " + r2)
  }

}
