package TreeTest

import org.scalatest.FunSuite

/**
  * Created by apinter on 14/07/2017.
  */
class SequenceListTest extends FunSuite{

  val strTrace = "ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15040 kernel32.dll+0x15c8b kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15fa7 kernel32.dll+0x15c8b kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15cfc kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15cfc"
  val intTrace = "1 2 3 4 5 6 7 8 9 10 11 1 2 3 12 13 4 5 6 7 8 9 10 11 1 2 3 14 13 4 5 6 7 8 9 10 11 1 2 3 15 4 5 6 7 8 9 10 11 1 2 3 15"
  val intListTrace = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 12, 13, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 14, 13, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 15, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 15)
  val shortListTrace = Vector(1,2,3,4,5)
  val shortListTrace2 = Vector(5,4,3,2,1)
  val shortMap = Map(1 -> 0.1, 2 -> 0.2, 3 -> 0.3)
  val shortStringListTrace = Vector("ntdll.dll+0x2173e, ntdll.dll+0x21639, ntdll.dll+0xeac7, kernel32.dll+0x15568, comctl32.dll+0x8ac2d")
  val shortStringMap = Map("ntdll.dll+0x2173e" -> 0.1, "ntdll.dll+0x21639" -> 0.2, "ntdll.dll+0xeac7" -> 0.3)

  test("SequenceList constructor arg validation. maxSeqcount is not zero."){
    assertThrows[IllegalArgumentException](new SequenceList[Int, Int](0))
  }
  test("SequenceList constructor arg validation. maxSeqcount is not negative."){
    assertThrows[IllegalArgumentException](new SequenceList[Int, Int](-1))
  }
  test("SequenceList constructor arg validation. Correct error message displayed if maxSeqCount is zero.!") {
    val caught = intercept[IllegalArgumentException](new SequenceList[Int, Int](0))
    assert(caught.getMessage == "requirement failed: Max sequence count must be positive!")
  }
  test("SequenceList updateSequences fails if maxSeqSize would be exceeded"){
    val sl = new SequenceList[Int, Int](1)
    val seq1 = (shortListTrace, 666)
    val seq2 = (shortListTrace2, 666)
    sl.updateSequences(seq1)
    assert(!sl.updateSequences(seq2))
  }
  test("SequenceList updateSequences doesn't not fail maxSeqSize check if Sequence with existing key is added multiple times."){
    val sl = new SequenceList[Int, Int](1)
    val seq1 = (shortListTrace, 666)
    val seq2 = (shortListTrace, 666)
    val seq3 = (shortListTrace, 666)
    val seq4 = (shortListTrace, 666)
    sl.updateSequences(seq1)
    assert(sl.updateSequences(seq2))
    assert(sl.updateSequences(seq3))
    assert(sl.updateSequences(seq4))
  }
  test("SequenceList updateSequences doesn't not fail maxSeqSize check if Sequence with existing key is added multiple times. (Different event values)"){
    val sl = new SequenceList[Int, Int](1)
    val seq1 = (shortListTrace, 666)
    val seq2 = (shortListTrace, 777)
    val seq3 = (shortListTrace, 888)
    val seq4 = (shortListTrace, 999)
    sl.updateSequences(seq1)
    assert(sl.updateSequences(seq2))
    assert(sl.updateSequences(seq3))
    assert(sl.updateSequences(seq4))
  }
  test("SequenceList, only one Sequence exists with a given key "){
  val sl = new SequenceList[Int, Int](1)
  val seq1 = (shortListTrace, 666)
  val seq2 = (shortListTrace, 777)
  val seq3 = (shortListTrace, 888)
  val seq4 = (shortListTrace, 999)

  assert(sl.sequences.size == 0)
  sl.updateSequences(seq1)
  assert(sl.sequences.size == 1)
  sl.updateSequences(seq2)
  assert(sl.sequences.size == 1)
  sl.updateSequences(seq3)
  assert(sl.sequences.size == 1)
  sl.updateSequences(seq4)
  assert(sl.sequences.size == 1)

}

}
