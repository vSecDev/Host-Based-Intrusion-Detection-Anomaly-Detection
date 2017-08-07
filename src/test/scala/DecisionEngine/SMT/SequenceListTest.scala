package DecisionEngine.SMT

import org.scalatest.FunSuite
import org.scalatest.Matchers._
/**
  * Created by apinter on 14/07/2017.
  */
class SequenceListTest extends FunSuite {

  val condition = Vector(1, 2, 3, 4, 5)
  val condition2 = Vector(5, 4, 3, 2, 1)
  val condition3 = Vector(1, 2, 3)
  val condition4 = Vector(4, 5, 6)
 
  test("SequenceList constructor arg validation. maxSeqcount is not zero.") {
    assertThrows[IllegalArgumentException](new SequenceList[Int, Int](1,1,0, 0.0, 1.0))
  }
  test("SequenceList constructor arg validation. maxSeqcount is not negative.") {
    assertThrows[IllegalArgumentException](new SequenceList[Int, Int](1,1,-1, 0.0, 1.0))
  }
  test("SequenceList constructor arg validation. Correct error message displayed if maxSeqCount is zero.!") {
    val caught = intercept[IllegalArgumentException](new SequenceList[Int, Int](1,1,0, 0.0, 1.0))
    assert(caught.getMessage == "requirement failed: SequenceList max sequence count must be positive!")
  }
  test("SequenceList updateSequences fails if maxSeqSize would be exceeded and maxDepth > 1") {
    val sl = new SequenceList[Int, Int](2,1,1, 0.0, 1.0)
    val seq1 = (condition, 666)
    val seq2 = (condition2, 666)

    sl.updateSequences(seq1)
    sl.updateSequences(seq2) should be('defined)
  }
  test("SequenceList updateSequences succeeds if maxSeqSize would be exceeded BUT maxDepth < 1") {
    val sl = new SequenceList[Int, Int](0,1,1, 0.0, 1.0)
    val seq1 = (condition, 666)
    val seq2 = (condition2, 666)
    sl.updateSequences(seq1)
    sl.updateSequences(seq2) shouldBe None
    assert(sl.getKeys.size == 2)
    assert(sl.getKeys.contains(condition))
    assert(sl.getKeys.contains(condition2))
  }
  test("SequenceList updateSequences doesn't not fail maxSeqSize check if Sequence with existing key is added multiple times.") {
    val sl = new SequenceList[Int, Int](1,1,1, 0.0, 1.0)
    val seq1 = (condition, 666)
    val seq2 = (condition, 666)
    val seq3 = (condition, 666)
    val seq4 = (condition, 666)

    sl.updateSequences(seq1) shouldBe None
    sl.updateSequences(seq2) shouldBe None
    sl.updateSequences(seq3) shouldBe None
    sl.updateSequences(seq4) shouldBe None
  }
  test("SequenceList updateSequences doesn't not fail maxSeqSize check if Sequence with existing key is added multiple times. (Different event values)") {
    val sl = new SequenceList[Int, Int](1,1,1, 0.0, 1.0)
    val seq1 = (condition, 666)
    val seq2 = (condition, 777)
    val seq3 = (condition, 888)
    val seq4 = (condition, 999)

    sl.updateSequences(seq1) shouldBe None
    sl.updateSequences(seq2) shouldBe None
    sl.updateSequences(seq3) shouldBe None
    sl.updateSequences(seq4) shouldBe None
  }
  test("SequenceList, only one Sequence exists with a given key ") {
    val sl = new SequenceList[Int, Int](1,1,1, 0.0, 1.0)
    val seq1 = (condition, 666)
    val seq2 = (condition, 777)
    val seq3 = (condition, 888)
    val seq4 = (condition, 999)
    val seq5 = (condition, 666)

    assert(sl.sequences.size == 0)
    sl.updateSequences(seq1)
    assert(sl.sequences.size == 1)
    sl.updateSequences(seq2)
    assert(sl.sequences.size == 1)
    sl.updateSequences(seq3)
    assert(sl.sequences.size == 1)
    sl.updateSequences(seq4)
    assert(sl.sequences.size == 1)
    sl.updateSequences(seq5)
    assert(sl.sequences.size == 1)
  }
  test("SequenceList, getSequence returns None if sequence with key doesn't exist in list") {
    val sl = new SequenceList[Int, Int](1,1,1, 0.0, 1.0)
    val seq1 = (condition, 666)
    sl.updateSequences(seq1)
    assert(sl.getSequence(condition2).isEmpty)
  }
  test("SequenceList, getSequence returns sequence if sequence with key exists in list") {
    val sl = new SequenceList[Int, Int](1,1,1, 0.0, 1.0)
    val seq1 = (condition, 666)
    sl.updateSequences(seq1)
    assert(sl.getSequence(condition).get.getKey == condition)
  }
  test("SequenceList getKeys returns empty Vector if there are no sequences stored in SequenceList") {
    val sl = new SequenceList[Int, Int](1,1,1, 0.0, 1.0)
    assert(sl.getKeys.isEmpty)
  }
  test("SequenceList getKeys returns Vector of size 1 if there is one sequence stored in SequenceList") {
    val sl = new SequenceList[Int, Int](1,1,1, 0.0, 1.0)
    val seq1 = (condition, 666)
    sl.updateSequences(seq1)
    assert(sl.getKeys.size == 1)
  }
  test("SequenceList getKeys returns Vector of correct size if there are multiple sequences stored in SequenceList") {
    val sl = new SequenceList[Int, Int](1,1,4, 0.0, 1.0)
    val seq1 = (condition, 666)
    val seq2 = (condition, 777)
    val seq3 = (condition, 888)
    val seq4 = (condition, 999)
    val seq5 = (condition, 666)

    assert(sl.getKeys.isEmpty)
    sl.updateSequences(seq1)
    assert(sl.getKeys.size == 1)
    sl.updateSequences(seq2)
    assert(sl.getKeys.size == 1)
    sl.updateSequences(seq3)
    assert(sl.getKeys.size == 1)
    sl.updateSequences(seq4)
    assert(sl.getKeys.size == 1)
    sl.updateSequences(seq5)
    assert(sl.getKeys.size == 1)

    val seq6 = (condition2, 666)
    val seq7 = (condition3, 777)
    val seq8 = (condition4, 888)


    sl.updateSequences(seq6)
    assert(sl.getKeys.size == 2)
    sl.updateSequences(seq7)
    assert(sl.getKeys.size == 3)
    sl.updateSequences(seq8)
    assert(sl.getKeys.size == 4)

    val seq9 = (condition2, 999)
    val seq10 = (condition3, 666)
    val seq11 = (condition4, 888)

    sl.updateSequences(seq9)
    assert(sl.getKeys.size == 4)
    sl.updateSequences(seq10)
    assert(sl.getKeys.size == 4)
    sl.updateSequences(seq11)
    assert(sl.getKeys.size == 4)
  }
  test("SequenceList, getKeys returns correct keys") {
    val sl = new SequenceList[Int, Int](1,1,4, 0.0, 1.0)
    val seq1 = (condition, 666)

    assert(sl.getKeys.isEmpty)
    sl.updateSequences(seq1)
    assert(sl.getKeys.size == 1)
    assert(sl.getKeys.contains(condition))

    val seq2 = (condition2, 666)
    val seq3 = (condition3, 777)
    val seq4 = (condition4, 888)


    sl.updateSequences(seq2)
    assert(sl.getKeys.size == 2)
    assert(sl.getKeys.contains(condition))
    assert(sl.getKeys.contains(condition2))
    sl.updateSequences(seq3)
    assert(sl.getKeys.size == 3)
    assert(sl.getKeys.contains(condition))
    assert(sl.getKeys.contains(condition2))
    assert(sl.getKeys.contains(condition3))
    sl.updateSequences(seq4)
    assert(sl.getKeys.size == 4)
    assert(sl.getKeys.contains(condition))
    assert(sl.getKeys.contains(condition2))
    assert(sl.getKeys.contains(condition3))
    assert(sl.getKeys.contains(condition4))

    val seq6 = (condition2, 999)
    val seq7 = (condition3, 666)
    val seq8 = (condition4, 888)

    sl.updateSequences(seq6)
    assert(sl.getKeys.size == 4)
    assert(sl.getKeys.contains(condition))
    assert(sl.getKeys.contains(condition2))
    assert(sl.getKeys.contains(condition3))
    assert(sl.getKeys.contains(condition4))
    sl.updateSequences(seq7)
    assert(sl.getKeys.size == 4)
    assert(sl.getKeys.contains(condition))
    assert(sl.getKeys.contains(condition2))
    assert(sl.getKeys.contains(condition3))
    assert(sl.getKeys.contains(condition4))
    sl.updateSequences(seq8)
    assert(sl.getKeys.size == 4)
    assert(sl.getKeys.contains(condition))
    assert(sl.getKeys.contains(condition2))
    assert(sl.getKeys.contains(condition3))
    assert(sl.getKeys.contains(condition4))
  }
  test("SequenceList, getKeys returns empty Vector when no sequence is stored"){
    val sl = new SequenceList[Int, Int](1,1,4, 0.0, 1.0)
    assert(sl.getKeys.isEmpty)
  }
  test("SequenceList, getKeys returns Vector with correct keys when multiple sequences are stored"){

    val sl = new SequenceList[Int, Int](1,1,4, 0.0, 1.0)
    val seq1 = (condition, 666)
    val seq2 = (condition2, 666)
    val seq3 = (condition3, 777)
    val seq4 = (condition4, 888)

    assert(sl.getKeys.isEmpty)
    sl.updateSequences(seq1)
    assert(sl.getKeys.contains(condition))

    sl.updateSequences(seq2)
    assert(sl.getKeys.contains(condition))
    assert(sl.getKeys.contains(condition2))

    sl.updateSequences(seq3)
    assert(sl.getKeys.contains(condition))
    assert(sl.getKeys.contains(condition2))
    assert(sl.getKeys.contains(condition3))

    sl.updateSequences(seq4)
    assert(sl.getKeys.contains(condition))
    assert(sl.getKeys.contains(condition2))
    assert(sl.getKeys.contains(condition3))
    assert(sl.getKeys.contains(condition4))
  }
  test("SequenceList, sequence added despite maxSeqCount is reached IF maxDepth < 1."){

    val sl = new SequenceList[Int, Int](0,1,1, 0.0, 1.0)
    val seq1 = (condition, 666)
    val seq2 = (condition2, 666)
    val seq3 = (condition3, 777)
    val seq4 = (condition4, 888)

    assert(sl.getKeys.isEmpty)
    sl.updateSequences(seq1)
    assert(sl.getKeys.contains(condition))

    sl.updateSequences(seq2)
    assert(sl.getKeys.contains(condition))
    assert(sl.getKeys.contains(condition2))

    sl.updateSequences(seq3)
    assert(sl.getKeys.contains(condition))
    assert(sl.getKeys.contains(condition2))
    assert(sl.getKeys.contains(condition3))

    sl.updateSequences(seq4)
    assert(sl.getKeys.contains(condition))
    assert(sl.getKeys.contains(condition2))
    assert(sl.getKeys.contains(condition3))
    assert(sl.getKeys.contains(condition4))
  }
}