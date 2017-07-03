package TreeTest

import org.scalatest.FunSuite

/**
  * Created by Case on 02/07/2017.
  */
class TreeTest extends FunSuite{

  val strTrace = "ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15040 kernel32.dll+0x15c8b kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15fa7 kernel32.dll+0x15c8b kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15cfc kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15cfc"
  val intTrace = "1 2 3 4 5 6 7 8 9 10 11 1 2 3 12 13 4 5 6 7 8 9 10 11 1 2 3 14 13 4 5 6 7 8 9 10 11 1 2 3 15 4 5 6 7 8 9 10 11 1 2 3 15"
  val intListTrace = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 12, 13, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 14, 13, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 15, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 15)

  test("Node Test"){
    //val n = new Node(1,2,3,4, ())
    /*val n = new Node(2, 1, 3, 6, Nil)
    println(n)*/

    /*println("max depth: " + n.MAX_DEPTH)
    println("max phi: " + n.MAX_PHI)
    println("expand count: " + n.EXPAND_SEQUENCE_COUNT)
    println("node key: " + n.key)


    assert(n.MAX_DEPTH == 1)
    assert(n.MAX_PHI == 2)
    assert(n.EXPAND_SEQUENCE_COUNT == 3)
    assert(n.key == 4)*/
  }
  test("Node with 'None' predictions returns None for getProbability") {
    val n = new Node[Int](1, 1)
    val num = 1
    assert(n.getProbability(num) == None)
  }
  test("Node initialises without key"){
    val n = new Node[Int](1, 1)
    assert(n.getKey == None)
  }
  test("Node setKey works"){
    val n = new Node[Int](1,1)
    n.setKey(2)
    assert(n.getKey.get == 2)
  }
  test("Node key cannot be reset"){
    val n = new Node[Int](1,1)
    n.setKey(2)
    assertThrows[IllegalStateException](n.setKey(3))
  }
  test("Node correct error message for invalid key reset"){
    val n = new Node[Int](1,1)
    n.setKey(2)
    val caught = intercept[IllegalStateException](n.setKey(3))
    assert(caught.getMessage == "Node key cannot be reset")
  }

}
