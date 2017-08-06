package Data

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.Matchers._
import java.io._

class FileProcessorTest extends FunSuite {

  val testSource = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\source"
  val testTarget = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\target"

  test("FileProcessor - preprocess creates target dirs"){
    val s = new File(testSource)
    val t = new File(testTarget)
    val d = Array("\\s")
    val e = Array("GHC")

    val fp = new FileProcessor(s,t,d,e)
    fp.preprocess()
  }

  //TODO - FIX LOGIC WITH MULTIPLE DELIMITERS
  test("FileProcessor - preprocess works with multiple delimiters."){
    val s = new File(testSource)
    val t = new File(testTarget)
    val d = Array("\\s", "\\|", "\\;", "\\,", "\\_")
    val e = Array("GHC")
    val fp = new FileProcessor(s,t,d,e)
    fp.preprocess()
  }

}
