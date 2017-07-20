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

  //  TODO - DELETE BELOW
 /* test("SMT first. ") {
    val root: Node[Int, Int] = Node(5, 1, 3)
    //root.setKey(0)

    root.growTree(shortListTrace, 666)
    println(root.toString)
  }
  test("SMT, root toString") {
    val root: Node[Int, Int] = Node(5, 1, 3)
    println("Only root:")
    println(root.toString)
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
}