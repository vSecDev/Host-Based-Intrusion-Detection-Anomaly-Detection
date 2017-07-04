package TreeTest

import org.scalatest.FunSuite
//import TreeTest._

/**
  * Created by Case on 02/07/2017.
  */
class SequenceTest extends FunSuite{
  val strTrace = "ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15040 kernel32.dll+0x15c8b kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15fa7 kernel32.dll+0x15c8b kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15cfc kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15cfc"
  val intTrace = "1 2 3 4 5 6 7 8 9 10 11 1 2 3 12 13 4 5 6 7 8 9 10 11 1 2 3 14 13 4 5 6 7 8 9 10 11 1 2 3 15 4 5 6 7 8 9 10 11 1 2 3 15"
  val intListTrace = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 12, 13, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 14, 13, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 15, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 15)
  val shortListTrace = List(1,2,3,4,5)
  val shortMap = Map(1 -> 0.1, 2 -> 0.2, 3 -> 0.3)
  val shortStringListTrace = List("ntdll.dll+0x2173e, ntdll.dll+0x21639, ntdll.dll+0xeac7, kernel32.dll+0x15568, comctl32.dll+0x8ac2d")
  val shortStringMap = Map("ntdll.dll+0x2173e" -> 0.1, "ntdll.dll+0x21639" -> 0.2, "ntdll.dll+0xeac7" -> 0.3)



  test("Sequence returns probability for input"){
    val s1 = new Sequence[Int, Int](shortListTrace, 666)

    assert(s1.getProbability(666).get == 1.0)
    assert(s1.getProbability(777) == None)
  }
  test("Sequence key is set"){
    val s1 = new Sequence[Int, Int](shortListTrace, 666)
    assert(s1.getKey == shortListTrace)
  }
  test("Sequence key cannot be reset"){
    val s1 = new Sequence[Int, Int](shortListTrace, 666)
    assert(s1.getKey == shortListTrace)
    assertThrows[IllegalStateException](s1.setKey(shortListTrace))
  }
  test("Sequence key cannot be reset with new key"){
    val s1 = new Sequence[Int, Int](shortListTrace, 666)
    assert(s1.getKey == shortListTrace)
    assertThrows[IllegalStateException](s1.setKey(List(444,555,666)))
  }
  test("Sequence setKey error message correct for key reset"){
    val s1 = new Sequence[Int, Int](shortListTrace, 666)
    val caught = intercept[IllegalStateException](s1.setKey(List(1,2,3)))
    assert(caught.getMessage == "Sequence key cannot be reset")
  }
  test("Sequence eventCount is correct"){
    val s1 = new Sequence[Int, Int](shortListTrace, 666)
    assert(s1.getEventCount == 1)
    s1.updateEvents(666)
    assert(s1.getEventCount == 2)
    s1.updateEvents(666)
    assert(s1.getEventCount == 3)
    s1.updateEvents(777)
    assert(s1.getEventCount == 4)
  }




  /*private var events: mutable.Map[B, Int] = mutable.Map[B, Int]()
  private var predictions: mutable.Map[B, Double] = mutable.Map[B, Double]()*/



  /*test("Generic Sequence returns probability for Int input"){
    val s1 = new Sequence[Int, Int](shortListTrace, shortMap)

    assert(s1.getProbability(1).get == 0.1)
    assert(s1.getProbability(2).get == 0.2)
    assert(s1.getProbability(3).get == 0.3)
  }
  test("Generic Sequence returns probability for String input"){
    val s1 = new Sequence[String, String](shortStringListTrace, shortStringMap)

    assert(s1.getProbability("ntdll.dll+0x2173e").get == 0.1)
    assert(s1.getProbability("ntdll.dll+0x21639").get == 0.2)
    assert(s1.getProbability("ntdll.dll+0xeac7").get == 0.3)
  }*/


}
