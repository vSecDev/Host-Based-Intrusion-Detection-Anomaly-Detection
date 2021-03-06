package DecisionEngine.SMT

import org.scalatest.FunSuite

/**
  * Created by Case on 02/07/2017.
  */
class SequenceTest extends FunSuite{

  val intCondition = Vector(1,2,3,4,5)
  val stringCondition = Vector("ntdll.dll+0x2173e, ntdll.dll+0x21639, ntdll.dll+0xeac7, kernel32.dll+0x15568, comctl32.dll+0x8ac2d")

  test("Sequence constructor arg validation. Key isn't Nil!"){
    assertThrows[IllegalArgumentException](new Sequence[Int, Int](Vector(), 666, 0.0, 1.0))
  }
  test("Sequence constructor arg validation. Key isn't empty list!"){
    assertThrows[IllegalArgumentException](new Sequence[Int, Int](Vector[Int](), 666, 0.0, 1.0))
  }
  test("Sequence constructor arg validation. Correct error message if key is Nil!"){
    val caught = intercept[IllegalArgumentException](new Sequence[Int, Int](Vector[Int](), 666, 0.0, 1.0))
    assert(caught.getMessage == "requirement failed: Sequence key cannot be an empty list!")
  }
  test("Sequence constructor arg validation. Event isn't Nil!"){
    assertThrows[IllegalArgumentException](new Sequence[Int, List[Int]](intCondition, Nil, 0.0, 1.0))
  }
  test("Sequence constructor arg validation. Event isn't empty list!"){
    assertThrows[IllegalArgumentException](new Sequence[Int, List[Int]](intCondition, List[Int](), 0.0, 1.0))
  }
  test("Sequence constructor arg validation. Correct error message if event is Nil!"){
    val caught = intercept[IllegalArgumentException](new Sequence[Int, List[Int]](intCondition, Nil, 0.0, 1.0))
    assert(caught.getMessage == "requirement failed: Sequence event cannot be Nil or None!")
  }
  test("Sequence constructor arg validation. Event isn't None!"){
    assertThrows[IllegalArgumentException](new Sequence[Int, Option[Int]](intCondition, None, 0.0, 1.0))
  }
  test("Sequence constructor arg validation. Correct error message if event is None!"){
    val caught = intercept[IllegalArgumentException](new Sequence[Int, Option[Int]](intCondition, None, 0.0, 1.0))
    assert(caught.getMessage == "requirement failed: Sequence event cannot be Nil or None!")
  }
  test("Sequence constructor arg validation. Correct error message if prior is smaller than zero!"){
    val caught = intercept[IllegalArgumentException](new Sequence[Int, Option[Int]](intCondition, Some(666), 0.0, -1.0))
    assert(caught.getMessage == "requirement failed: Sequence prior weight must be larger than zero!")
  }
  test("Sequence constructor arg validation. Correct error message if prior is zero!"){
    val caught = intercept[IllegalArgumentException](new Sequence[Int, Option[Int]](intCondition, Some(666), 0.0, 0.0))
    assert(caught.getMessage == "requirement failed: Sequence prior weight must be larger than zero!")
  }
  test("Sequence constructor arg validation. Correct error message if smoothing is smaller than zero!"){
    val caught = intercept[IllegalArgumentException](new Sequence[Int, Option[Int]](intCondition, Some(666), -1.0, 0.0))
    assert(caught.getMessage == "requirement failed: Sequence smoothing value must be non-negative!")
  }
  test("Sequence correct error message if trying to reset smoothing"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    val caught = intercept[IllegalStateException](s1.setSmoothing(5.0))
    assert(caught.getMessage == "Sequence smoothing cannot be reset")
  }
  test("Sequence correct error message if trying to reset prior"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    val caught = intercept[IllegalStateException](s1.setPrior(5.0))
    assert(caught.getMessage == "Sequence prior cannot be changed after initialisation")
  }
  test("Sequence returns probability for input"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)

    assert(s1.getProbability(666) == 1.0)
    assert(s1.getProbability(777) == 0.0)
  }
  test("Sequence key is set"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    assert(s1.getKey == intCondition)
  }
  test("Sequence key cannot be reset"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    assert(s1.getKey == intCondition)
    assertThrows[IllegalStateException](s1.setKey(intCondition))
  }
  test("Sequence key cannot be reset with new key"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    assert(s1.getKey == intCondition)
    assertThrows[IllegalStateException](s1.setKey(Vector(444,555,666)))
  }
  test("Sequence setKey error message correct for key reset"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    val caught = intercept[IllegalStateException](s1.setKey(Vector(1,2,3)))
    assert(caught.getMessage == "Sequence key cannot be reset")
  }
  test("Sequence eventCount is correct"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    assert(s1.getEventCount == 1)
    s1.updateEvents(666)
    assert(s1.getEventCount == 2)
    s1.updateEvents(666)
    assert(s1.getEventCount == 3)
    s1.updateEvents(777)
    assert(s1.getEventCount == 4)
  }
  test("Sequence events is correct after creating instance"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    assert(s1.getEvents(666) == 1)
    assert(s1.getEvents.size == 1)
  }
  test("Sequence events is correct after updates with same event") {
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    assert(s1.getEvents(666) == 1)
    assert(s1.getEvents.size == 1)
    s1.updateEvents(666)
    assert(s1.getEvents(666) == 2)
    assert(s1.getEvents.size == 1)
    s1.updateEvents(666)
    assert(s1.getEvents(666) == 3)
    assert(s1.getEvents.size == 1)
  }
  test("Sequence events is correct after updates with different events") {
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    assert(s1.getEvents(666) == 1)
    assert(s1.getEvents.size == 1)
    s1.updateEvents(666)
    assert(s1.getEvents(666) == 2)
    assert(s1.getEvents.size == 1)
    s1.updateEvents(666)
    assert(s1.getEvents(666) == 3)
    assert(s1.getEvents.size == 1)
    s1.updateEvents(777)
    assert(s1.getEvents(666) == 3)
    assert(s1.getEvents(777) == 1)
    assert(s1.getEvents.size == 2)
    s1.updateEvents(777)
    assert(s1.getEvents(666) == 3)
    assert(s1.getEvents(777) == 2)
    assert(s1.getEvents.size == 2)
    s1.updateEvents(888)
    assert(s1.getEvents(666) == 3)
    assert(s1.getEvents(777) == 2)
    assert(s1.getEvents(888) == 1)
    assert(s1.getEvents.size == 3)
  }
  test("Sequence predictiona has single element after first event update"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    s1.updateEvents(666)
    assert(s1.getPredictions.size == 1)
  }
  test("Sequence updatePrediction has two elements after two different event key updates"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)

    s1.updateEvents(666)
    assert(s1.getPredictions.size == 1)
    s1.updateEvents(777)
    assert(s1.getPredictions.size == 2)
  }
  test("Sequence updatePrediction has one element after the same event key is updated twice"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)

    s1.updateEvents(666)
    assert(s1.getPredictions.size == 1)
    s1.updateEvents(666)
    assert(s1.getPredictions.size == 1)
    s1.updateEvents(666)
    assert(s1.getPredictions.size == 1)
  }
  test("Sequence updatePrediction has two elements after adding three events, two the same"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)

    s1.updateEvents(666)
    assert(s1.getPredictions.size == 1)
    s1.updateEvents(666)
    assert(s1.getPredictions.size == 1)
    s1.updateEvents(777)
    assert(s1.getPredictions.size == 2)
  }
  test("Sequence predictions are correct"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    assert(s1.getPredictions(666) == 1.0)
    s1.updateEvents(666)
    assert(s1.getPredictions(666) == 1.0)
    s1.updateEvents(777)
    assert(s1.getPredictions(777) == 1.0/3)
    s1.updateEvents(777)
    assert(s1.getPredictions(777) == 2.0/4)
    s1.updateEvents(888)
    assert(s1.getPredictions(666) == 2.0/5)
    assert(s1.getPredictions(777) == 2.0/5)
    assert(s1.getPredictions(888) == 1.0/5)
    s1.updateEvents(999)
    assert(s1.getPredictions(666) == 2.0/6)
    assert(s1.getPredictions(777) == 2.0/6)
    assert(s1.getPredictions(888) == 1.0/6)
    assert(s1.getPredictions(999) == 1.0/6)
  }
  test("Sequence getProbability for single event"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)

    s1.updateEvents(666)
    assert(s1.getProbability(666) == 1.0)
  }
  test("Sequence getProbability for events after multiple updates"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    assert(s1.getProbability(666) == 1.0)
    s1.updateEvents(777)
    assert(s1.getProbability(666) == 0.5)
    assert(s1.getProbability(777) == 0.5)

    assert(s1.getProbability(777) == 0.5)
    assert(s1.getProbability(777) == 0.5)
    assert(s1.getProbability(777) == 0.5)
    assert(s1.getProbability(777) == 0.5)

    s1.updateEvents(888)
    assert(s1.getProbability(666) == 1.0/3)
    assert(s1.getProbability(777) == 1.0/3)
    assert(s1.getProbability(888) == 1.0/3)
    s1.updateEvents(888)
    assert(s1.getProbability(666) == 1.0/4)
    assert(s1.getProbability(777) == 1.0/4)
    assert(s1.getProbability(888) == 2.0/4)
    s1.updateEvents(999)
    assert(s1.getProbability(666) == 1.0/5)
    assert(s1.getProbability(777) == 1.0/5)
    assert(s1.getProbability(888) == 2.0/5)
    assert(s1.getProbability(999)== 1.0/5)
    s1.updateEvents(666)
    assert(s1.getProbability(666) == 2.0/6)
    assert(s1.getProbability(777) == 1.0/6)
    assert(s1.getProbability(888) == 2.0/6)
    assert(s1.getProbability(999)== 1.0/6)
  }
  test("Sequence getProbability for String events after multiple updates. (String key)"){
    val s1 = new Sequence[String, String](stringCondition, "ntdll.dll+0x2173e", 0.0, 1.0)
    assert(s1.getProbability("ntdll.dll+0x2173e") == 1.0)
    s1.updateEvents("ntdll.dll+0x21639")
    assert(s1.getProbability("ntdll.dll+0x2173e") == 0.5)
    assert(s1.getProbability("ntdll.dll+0x21639") == 0.5)
    s1.updateEvents("ntdll.dll+0xeac7")
    assert(s1.getProbability("ntdll.dll+0x2173e") == 1.0/3)
    assert(s1.getProbability("ntdll.dll+0x21639") == 1.0/3)
    assert(s1.getProbability("ntdll.dll+0xeac7") == 1.0/3)
    s1.updateEvents("ntdll.dll+0xeac7")
    assert(s1.getProbability("ntdll.dll+0x2173e") == 1.0/4)
    assert(s1.getProbability("ntdll.dll+0x21639") == 1.0/4)
    assert(s1.getProbability("ntdll.dll+0xeac7") == 2.0/4)
    s1.updateEvents("kernel32.dll+0x15568")
    assert(s1.getProbability("ntdll.dll+0x2173e") == 1.0/5)
    assert(s1.getProbability("ntdll.dll+0x21639") == 1.0/5)
    assert(s1.getProbability("ntdll.dll+0xeac7") == 2.0/5)
    assert(s1.getProbability("kernel32.dll+0x15568") == 1.0/5)
    s1.updateEvents("ntdll.dll+0x2173e")
    assert(s1.getProbability("ntdll.dll+0x2173e") == 2.0/6)
    assert(s1.getProbability("ntdll.dll+0x21639") == 1.0/6)
    assert(s1.getProbability("ntdll.dll+0xeac7") == 2.0/6)
    assert(s1.getProbability("kernel32.dll+0x15568") == 1.0/6)
  }
  test("Sequence getProbability for String events after multiple updates. (Int key)"){
    val s1 = new Sequence[String, String](stringCondition, "ntdll.dll+0x2173e", 0.0, 1.0)
    assert(s1.getProbability("ntdll.dll+0x2173e") == 1.0)
    s1.updateEvents("ntdll.dll+0x21639")
    assert(s1.getProbability("ntdll.dll+0x2173e") == 0.5)
    assert(s1.getProbability("ntdll.dll+0x21639") == 0.5)
    s1.updateEvents("ntdll.dll+0xeac7")
    assert(s1.getProbability("ntdll.dll+0x2173e") == 1.0/3)
    assert(s1.getProbability("ntdll.dll+0x21639") == 1.0/3)
    assert(s1.getProbability("ntdll.dll+0xeac7") == 1.0/3)
    s1.updateEvents("ntdll.dll+0xeac7")
    assert(s1.getProbability("ntdll.dll+0x2173e") == 1.0/4)
    assert(s1.getProbability("ntdll.dll+0x21639") == 1.0/4)
    assert(s1.getProbability("ntdll.dll+0xeac7") == 2.0/4)
    s1.updateEvents("kernel32.dll+0x15568")
    assert(s1.getProbability("ntdll.dll+0x2173e") == 1.0/5)
    assert(s1.getProbability("ntdll.dll+0x21639") == 1.0/5)
    assert(s1.getProbability("ntdll.dll+0xeac7") == 2.0/5)
    assert(s1.getProbability("kernel32.dll+0x15568") == 1.0/5)
    s1.updateEvents("ntdll.dll+0x2173e")
    assert(s1.getProbability("ntdll.dll+0x2173e") == 2.0/6)
    assert(s1.getProbability("ntdll.dll+0x21639") == 1.0/6)
    assert(s1.getProbability("ntdll.dll+0xeac7") == 2.0/6)
    assert(s1.getProbability("kernel32.dll+0x15568") == 1.0/6)
  }


  test("Sequence getProbability for String events after multiple updates. With smoothing!"){
    val smoothing = 0.1
    val s1 = new Sequence[String, String](stringCondition, "ntdll.dll+0x2173e", smoothing, 1.0)

    assert(s1.getProbability("Nonexistent event") == smoothing)
    assert(s1.getProbability("ntdll.dll+0x2173e") == 1.1)
    s1.updateEvents("ntdll.dll+0x21639")
    assert(s1.getProbability("ntdll.dll+0x2173e") == (1.0 + smoothing) / 2)
    assert(s1.getProbability("ntdll.dll+0x21639") == (1.0 + smoothing) / 2)
    s1.updateEvents("ntdll.dll+0xeac7")
    assert(s1.getProbability("ntdll.dll+0x2173e") == (1.0 + smoothing) / 3)
    assert(s1.getProbability("ntdll.dll+0x21639") == (1.0 + smoothing) / 3)
    assert(s1.getProbability("ntdll.dll+0xeac7") == (1.0 + smoothing) / 3)
    s1.updateEvents("ntdll.dll+0xeac7")
    assert(s1.getProbability("ntdll.dll+0x2173e") == (1.0 + smoothing) / 4)
    assert(s1.getProbability("ntdll.dll+0x21639") == (1.0 + smoothing) / 4)
    assert(s1.getProbability("ntdll.dll+0xeac7") == (2.0 + smoothing) / 4)
    s1.updateEvents("kernel32.dll+0x15568")
    assert(s1.getProbability("ntdll.dll+0x2173e") == (1.0 + smoothing) / 5)
    assert(s1.getProbability("ntdll.dll+0x21639") == (1.0 + smoothing) / 5)
    assert(s1.getProbability("ntdll.dll+0xeac7") == (2.0 + smoothing) / 5)
    assert(s1.getProbability("kernel32.dll+0x15568") == (1.0 + smoothing) / 5)
    s1.updateEvents("ntdll.dll+0x2173e")
    assert(s1.getProbability("ntdll.dll+0x2173e") == (2.0 + smoothing) / 6)
    assert(s1.getProbability("ntdll.dll+0x21639") == (1.0 + smoothing) / 6)
    assert(s1.getProbability("ntdll.dll+0xeac7") == (2.0 + smoothing) / 6)
    assert(s1.getProbability("kernel32.dll+0x15568") == (1.0 + smoothing) / 6)
  }
  test("Sequence getWeightedProbability"){
    val smoothing = 0.0
    val s1 = new Sequence[String, String](stringCondition, "ntdll.dll+0x2173e", smoothing, 1.0)

    assert(s1.getWeightedProbability("Nonexistent event") == smoothing)
    assert(s1.getWeightedProbability("ntdll.dll+0x2173e") == 1.0)
    assert(s1.getWeight == 1.0)

    s1.updateEvents("ntdll.dll+0x21639")
    assert(s1.getWeight == 0.5)
    assert(s1.getProbability("ntdll.dll+0x2173e") == (1.0 + smoothing) / 2)
    assert(s1.getProbability("ntdll.dll+0x21639") == (1.0 + smoothing) / 2)

    assert(s1.getWeightedProbability("Nonexistent event") == smoothing)
    assert(s1.getWeightedProbability("ntdll.dll+0x2173e") == 0.5 * (1.0 + smoothing) / 2)
    assert(s1.getWeightedProbability("ntdll.dll+0x21639") == 0.5 * (1.0 + smoothing) / 2)

    val prevWeight = s1.getWeight
    s1.updateEvents("ntdll.dll+0xeac7")
    val p = s1.getProbability("ntdll.dll+0xeac7")
    assert(s1.getWeightedProbability("Nonexistent event") == smoothing)
    assert(s1.getWeightedProbability("ntdll.dll+0x2173e") == 0.5 * 1.0/3 * (1.0 + smoothing) / 3)
    assert(s1.getWeightedProbability("ntdll.dll+0x2173e") == prevWeight * p * (1.0 + smoothing) / 3)
    assert(s1.getWeightedProbability("ntdll.dll+0x21639") == 0.5 * 1.0/3 * (1.0 + smoothing) / 3)
    assert(s1.getWeightedProbability("ntdll.dll+0xeac7") == 0.5 * 1.0/3 * (1.0 + smoothing) / 3)
  }
  test("Sequence eventCount == events sum in events"){
    val s1 = new Sequence[Int, Int](intCondition, 666, 0.0, 1.0)
    assert(s1.getEventCount == 1)
    var eventNum = 0
    for(e <- s1.getEvents){
      eventNum += e._2
    }
    assert(eventNum == s1.getEventCount)


    s1.updateEvents(666)
    assert(s1.getEventCount == 2)
    var eventNum2 = 0
    for(e <- s1.getEvents){
      eventNum2 += e._2
    }
    assert(eventNum2 == s1.getEventCount)
    assert(eventNum2 == 2)
    assert(2 == s1.getEventCount)

    s1.updateEvents(777)
    assert(s1.getEventCount == 3)
    var eventNum3 = 0
    for(e <- s1.getEvents){
      eventNum3 += e._2
    }
    assert(eventNum3 == s1.getEventCount)
    assert(eventNum3 == 3)
    assert(3 == s1.getEventCount)


    s1.updateEvents(888)
    assert(s1.getEventCount == 4)
    var eventNum4 = 0
    for(e <- s1.getEvents){
      eventNum4 += e._2
    }
    assert(eventNum4 == s1.getEventCount)
    assert(eventNum4 == 4)
    assert(4 == s1.getEventCount)
  }
}
