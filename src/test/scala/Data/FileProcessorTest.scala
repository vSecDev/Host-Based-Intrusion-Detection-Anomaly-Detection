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
    val d = "\\s"
    val fp = new FileProcessor(s,t,d)
    fp.preprocess()

  }
}
