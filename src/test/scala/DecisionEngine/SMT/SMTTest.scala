package DecisionEngine.SMT

import java.io.{File, FileInputStream, FileOutputStream, IOException, InvalidClassException, NotSerializableException, ObjectInputStream, ObjectOutputStream, Serializable, StreamCorruptedException, _}

import Data.DataException
import org.apache.commons.io.FilenameUtils
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.collection.immutable.ListMap

/**
  * Created by Case on 20/07/2017.
  */
class SMTTest extends FunSuite with BeforeAndAfterAll {
  val isHome = true
  val serializePathHome = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Serialised\\"

  /* val serializePath = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Serialised\\"
    val workDataParent = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\WindowsToProcess\\"
    val linuxDataHome = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\Old\\ADFA-LD\\ADFA-LD\\Training_Data_Master\\"
    val windowsTrainingDataWork = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Main\\Full_Process_Traces\\Full_Trace_Training_Data"
    val windowsTrainingDataHome = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Main\\Full_Process_Traces_Int\\Full_Trace_Training_Data\\"
    val linuxDataWork = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\ADFA-LD\\ADFA-LD\\Training_Data_Master\\"
    val dataHome = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Old\\Full_Process_Traces 2\\Full_Process_Traces\\Full_Trace_Training_Data\\"
    val dataWork = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Full_Process_Traces 2\\Full_Process_Traces\\Full_Trace_Training_Data\\"
    val homeTrainingPath = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Old\\Full_Process_Traces 2\\Full_Process_Traces\\Full_Trace_Training_Data\\Training-Wireless-Karma_680.GHC"
    val workTrainingPath = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Full_Process_Traces 2\\Full_Process_Traces\\Full_Trace_Training_Data\\Training-Wireless-Karma_2132.GHC"
  */
  def time[T](block: => T): T = {
    val start = System.currentTimeMillis
    val res = block
    val totalTime = System.currentTimeMillis - start
    println("Elapsed time: %1d ms".format(totalTime))
    res
  }

  def getListOfFiles(dir: String, extensions: List[String]): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList.filter { file => (file.getName.contains("Training-Backdoored-Executable") || file.getName.contains("Training-Background")) && extensions.exists(file.getName.endsWith(_))
      }
    } else {
      List[File]()
    }
  }

  def getListOfWindowsFiles(dir: String, extensions: List[String]): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) d.listFiles.filter(_.isFile).toList.filter { file => file.getName.contains("INT") && extensions.exists(file.getName.endsWith(_))
    } else {
      List[File]()
    }
  }

  private def fileStreamNoDirs(dir: File, extensions: Array[String]): Stream[File] =
    try {
      Option(dir.listFiles).map(_.toList.sortBy(_.getName).toStream.partition(_.isDirectory))
        .map { case (dirs, files) =>
          files.filter(f => checkExtension(f, extensions)).append(dirs.flatMap(fileStreamNoDirs(_, extensions)))
        } getOrElse {
        Stream.empty
      }
    } catch {
      case se: SecurityException => throw new DataException("SecurityException thrown during processing source files.", se)
    }

  private def checkExtension(f: File, extensions: Array[String]): Boolean = extensions.contains(FilenameUtils.getExtension(f.getName))

  /*def serializeTree(smt: SparseMarkovTree[Int, Int], target: File): Unit = {
    val fos = new FileOutputStream(target)
    val oos = new ObjectOutputStream(fos)
    try {
      oos.writeObject(smt)
      oos.close
    } catch {
      case e: Exception => println(e.getStackTrace)
    } finally {
      fos.close
      oos.close
    }
  }

  def deserializeTree(source: File): Option[SparseMarkovTree[Int, Int]] = {
    println("1")
    val fis = new FileInputStream(source)
    println("2")
    val ois = new ObjectInputStreamWithCustomClassLoader(fis)
    println("3")
    try {
      println("4")
      val smt = ois.readObject
      println("5")
      ois.close
      println("6")
      println("smt as node: " + smt.asInstanceOf[Node[Int, Int]])
      Some(smt.asInstanceOf[SparseMarkovTree[Int, Int]])
    } catch {
      case e: Exception => println("EXCEPTION IN DES"); println(e.getStackTrace); None
    } finally {
      fis.close
      ois.close
    }
  }*/

  @throws(classOf[DataException])
  def serialise(model: Serializable, _target: File): Boolean = {

    val fos = new FileOutputStream(_target)
    val oos = new ObjectOutputStream(fos)
    try {

      oos.writeObject(model)
      oos.close
      true
    } catch {
      case ice: InvalidClassException => throw new DataException("InvalidClassException thrown during model serialisation.", ice)
      case nse: NotSerializableException => throw new DataException("NotSerializableException thrown during model serialisation.", nse)
      case ioe: IOException => throw new DataException("IOException thrown during model serialisation.", ioe)
    } finally {
      fos.close
      oos.close
    }
  }

  def deserialise(_source: File): Option[Serializable] = {
    if (!_source.exists || !_source.isFile) return None
    val fis = new FileInputStream(_source)
    val ois = new ObjectInputStreamWithCustomClassLoader(fis)
    try {
      val newModel = ois.readObject.asInstanceOf[Serializable]
      ois.close
      Some(newModel)
    } catch {
      case cnfe: ClassNotFoundException => throw new DataException("ClassNotFoundException thrown during model deserialisation.", cnfe)
      case ice: InvalidClassException => throw new DataException("InvalidClassException thrown during model deserialisation.", ice)
      case sce: StreamCorruptedException => throw new DataException("StreamCorruptedException thrown during model deserialisation.", sce)
      case ioe: IOException => throw new DataException("IOException thrown during model deserialisation.", ioe)
    } finally {
      fis.close
      ois.close
    }
  }

  private class ObjectInputStreamWithCustomClassLoader(
                                                        fileInputStream: FileInputStream
                                                      ) extends ObjectInputStream(fileInputStream) {
    override def resolveClass(desc: java.io.ObjectStreamClass): Class[_] = {
      try {
        Class.forName(desc.getName, false, getClass.getClassLoader)
      }
      catch {
        case ex: ClassNotFoundException => super.resolveClass(desc)
      }
    }
  }

  def bottom(n: Int, li: List[Double]): List[Double] = {

    def updateSofar(sofar: List[Double], el: Double): List[Double] = {
      // println (el + " - " + sofar)
      if (el < sofar.head)
        (el :: sofar.tail).sortWith(_ > _)
      else sofar
    }

    (li.take(n).sortWith(_ > _) /: li.drop(n)) (updateSofar(_, _))
  }


  def top(n: Int, li: List[Double]): List[Double] = {

    def updateSofar(sofar: List[Double], el: Double): List[Double] = {
      // println (el + " - " + sofar)
      if (el > sofar.head)
        (el :: sofar.tail).sortWith(_ < _)
      else sofar
    }

    (li.take(n).sortWith(_ < _) /: li.drop(n)) (updateSofar(_, _))
  }


  test("top") {
    val v1 = Vector(80.0, 100.0, -5.0, 10.0, 8.0, 6.0, 4.0, 2.0, 1.0, 3.0, 5.0, 0.0, 0.1, 7.0, 9.0)
    val bottom5 = bottom(5, v1.toList)
    println("bottom 5: " + bottom5)

    val top5 = top(5, v1.toList)
    println("top 5: " + top5)
  }


  test("SparseMarkovTree. maxDepth is not zero") {
    assertThrows[IllegalArgumentException](Node(0, 1, 3, 0.0, 1.0))
    val caught = intercept[IllegalArgumentException](Node(0, 1, 3, 0.0, 1.0))
    assert(caught.getMessage == "requirement failed: Node max depth count must be positive!")
  }
  test("SparseMarkovTree. maxDepth is not negative") {
    assertThrows[IllegalArgumentException](Node(-1, 1, 3, 0.0, 1.0))
    val caught = intercept[IllegalArgumentException](Node(-1, 1, 3, 0.0, 1.0))
    assert(caught.getMessage == "requirement failed: Node max depth count must be positive!")
  }
  test("SparseMarkovTree. maxPhi is non-negative") {
    assertThrows[IllegalArgumentException](Node(1, -1, 3, 0.0, 1.0))
    val caught = intercept[IllegalArgumentException](Node(1, -1, 3, 0.0, 1.0))
    assert(caught.getMessage == "requirement failed: Node max Phi count must be non-negative!")
  }
  test("SparseMarkovTree. maxSeqCount is not zero") {
    assertThrows[IllegalArgumentException](Node(1, 1, 0, 0.0, 1.0))
    val caught = intercept[IllegalArgumentException](Node(1, 1, 0, 0.0, 1.0))
    assert(caught.getMessage == "requirement failed: Node max sequence count must be positive!")
  }
  test("SparseMarkovTree. maxSeqCount is not negative") {
    assertThrows[IllegalArgumentException](Node(1, 1, -1, 0.0, 1.0))
    val caught = intercept[IllegalArgumentException](Node(1, 1, -1, 0.0, 1.0))
    assert(caught.getMessage == "requirement failed: Node max sequence count must be positive!")
  }
  test("SparseMarkovTree. Default key is None") {
    val root: Node[Int, Int] = Node(5, 1, 3, 0.0, 1.0)
    root.getKey shouldBe None
  }
  test("SparseMarkovTree. setKey sets correct Int key") {
    val root: Node[Int, Int] = Node(5, 1, 3, 0.0, 1.0)
    root.getKey shouldBe None
    root.setKey(0)
    root.getKey shouldBe 'defined
    assert(root.getKey.get == 0)
  }
  test("SparseMarkovTree. setKey sets correct String key") {
    val root: Node[String, Int] = Node(5, 1, 3, 0.0, 1.0)
    root.getKey shouldBe None
    root.setKey("TestKey")
    root.getKey shouldBe 'defined
    assert(root.getKey.get equals "TestKey")
  }
  test("SparseMarkovTree. setKey fails if called multiple times - String key") {
    val root: Node[String, Int] = Node(5, 1, 3, 0.0, 1.0)
    root.setKey("TestKey")
    root.getKey shouldBe 'defined
    assert(root.getKey.get equals "TestKey")

    val caught = intercept[IllegalStateException](root.setKey("NewTestKey"))
    assert(caught.getMessage == "Node key cannot be reset!")
  }
  test("SparseMarkovTree. setKey fails if called multiple times - Int key") {
    val root: Node[Int, Int] = Node(5, 1, 3, 0.0, 1.0)
    root.setKey(0)
    root.getKey shouldBe 'defined
    assert(root.getKey.get == 0)

    val caught = intercept[IllegalStateException](root.setKey(222))
    assert(caught.getMessage equals "Node key cannot be reset!")
  }
  test("SparseMarkovTree eventCount is correct") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)
    assert(n1.getEventCount == 0)
    n1.updateEvents(666)
    assert(n1.getEventCount == 1)
    n1.updateEvents(666)
    assert(n1.getEventCount == 2)
    n1.updateEvents(777)
    assert(n1.getEventCount == 3)
  }
  test("SparseMarkovTree, events initialised empty") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)
    assertThrows[NoSuchElementException](n1.getEvents(666))
    assert(n1.getEvents.isEmpty)
  }
  test("SparseMarkovTree 'events' is correct after updates with same event") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)

    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)

    n1.updateEvents(666)
    assert(n1.getEventCount == 1)
    assert(n1.getEvents.size == 1)
    assert(n1.getProbability(666) == 1.00)

    n1.updateEvents(666)
    assert(n1.getEventCount == 2)
    assert(n1.getEvents.size == 1)
    assert(n1.getProbability(666) == 1.00)

    n1.updateEvents(666)
    assert(n1.getEventCount == 3)
    assert(n1.getEvents.size == 1)
    assert(n1.getProbability(666) == 1.00)
  }
  test("SparseMarkovTree events is correct after updates with different events") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)

    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)

    n1.updateEvents(666)
    assert(n1.getEventCount == 1)
    assert(n1.getEvents.size == 1)
    assert(n1.getProbability(666) == 1.00)

    n1.updateEvents(666)
    assert(n1.getEventCount == 2)
    assert(n1.getEvents.size == 1)
    assert(n1.getProbability(666) == 1.00)

    n1.updateEvents(666)
    assert(n1.getEventCount == 3)
    assert(n1.getEvents.size == 1)
    assert(n1.getProbability(666) == 1.00)

    n1.updateEvents(777)
    assert(n1.getEventCount == 4)
    assert(n1.getEvents.size == 2)
    assert(n1.getEvents(666) == 3)
    assert(n1.getEvents.get(777).get == 1)
    assert(n1.getProbability(666) == 3.00 / 4)
    assert(n1.getProbability(777) == 1.00 / 4)

    n1.updateEvents(777)
    assert(n1.getEventCount == 5)
    assert(n1.getEvents.size == 2)
    assert(n1.getEvents.get(666).get == 3)
    assert(n1.getEvents.get(777).get == 2)
    assert(n1.getProbability(666) == 3.00 / 5)
    assert(n1.getProbability(777) == 2.00 / 5)

    n1.updateEvents(888)
    assert(n1.getEventCount == 6)
    assert(n1.getEvents.size == 3)
    assert(n1.getEvents.get(666).get == 3)
    assert(n1.getEvents.get(777).get == 2)
    assert(n1.getEvents.get(888).get == 1)
    assert(n1.getProbability(666) == 3.00 / 6)
    assert(n1.getProbability(777) == 2.00 / 6)
    assert(n1.getProbability(888) == 1.00 / 6)
  }
  test("SparseMarkovTree predictions has single element after first event update") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
  }
  test("SparseMarkovTree predictions has two elements after two different event key updates") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)

    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(777)
    assert(n1.getPredictions.size == 2)
  }
  test("SparseMarkovTree predictions has one element after the same event key is updated twice") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
  }
  test("SparseMarkovTree predictions has two elements after adding three events, two the same") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(666)
    assert(n1.getPredictions.size == 1)
    n1.updateEvents(777)
    assert(n1.getPredictions.size == 2)
  }
  test("SparseMarkovTree predictions are correct") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)
    n1.updateEvents(666)
    assert(n1.getPredictions(666) == 1.0)
    n1.updateEvents(666)
    assert(n1.getPredictions(666) == 1.0)
    n1.updateEvents(777)
    assert(n1.getPredictions(777) == 1.0 / 3)
    n1.updateEvents(777)
    assert(n1.getPredictions(777) == 2.0 / 4)
    n1.updateEvents(888)
    assert(n1.getPredictions(666) == 2.0 / 5)
    assert(n1.getPredictions(777) == 2.0 / 5)
    assert(n1.getPredictions(888) == 1.0 / 5)
    n1.updateEvents(999)
    assert(n1.getPredictions(666) == 2.0 / 6)
    assert(n1.getPredictions(777) == 2.0 / 6)
    assert(n1.getPredictions(888) == 1.0 / 6)
    assert(n1.getPredictions(999) == 1.0 / 6)
  }
  test("SparseMarkovTree getProbability for single event") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)
    n1.updateEvents(666)
    assert(n1.getProbability(666) == 1.0)
  }
  test("SparseMarkovTree getProbability for events after multiple updates") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)
    n1.updateEvents(666)
    assert(n1.getProbability(666) == 1.0)
    n1.updateEvents(777)
    assert(n1.getProbability(666) == 0.5)
    assert(n1.getProbability(777) == 0.5)

    n1.updateEvents(888)
    assert(n1.getProbability(666) == 1.0 / 3)
    assert(n1.getProbability(777) == 1.0 / 3)
    assert(n1.getProbability(888) == 1.0 / 3)
    n1.updateEvents(888)
    assert(n1.getProbability(666) == 1.0 / 4)
    assert(n1.getProbability(777) == 1.0 / 4)
    assert(n1.getProbability(888) == 2.0 / 4)
    n1.updateEvents(999)
    assert(n1.getProbability(666) == 1.0 / 5)
    assert(n1.getProbability(777) == 1.0 / 5)
    assert(n1.getProbability(888) == 2.0 / 5)
    assert(n1.getProbability(999) == 1.0 / 5)
    n1.updateEvents(666)
    assert(n1.getProbability(666) == 2.0 / 6)
    assert(n1.getProbability(777) == 1.0 / 6)
    assert(n1.getProbability(888) == 2.0 / 6)
    assert(n1.getProbability(999) == 1.0 / 6)
  }
  test("SparseMarkovTree getProbability for String events after multiple updates. (String key)") {
    val n1 = new Node[Int, String](5, 1, 1, 0.0, 1.0)
    n1.updateEvents("ntdll.dll+0x2173e")
    assert(n1.getProbability("ntdll.dll+0x2173e") == 1.0)
    n1.updateEvents("ntdll.dll+0x21639")
    assert(n1.getProbability("ntdll.dll+0x2173e") == 0.5)
    assert(n1.getProbability("ntdll.dll+0x21639") == 0.5)
    n1.updateEvents("ntdll.dll+0xeac7")
    assert(n1.getProbability("ntdll.dll+0x2173e") == 1.0 / 3)
    assert(n1.getProbability("ntdll.dll+0x21639") == 1.0 / 3)
    assert(n1.getProbability("ntdll.dll+0xeac7") == 1.0 / 3)
    n1.updateEvents("ntdll.dll+0xeac7")
    assert(n1.getProbability("ntdll.dll+0x2173e") == 1.0 / 4)
    assert(n1.getProbability("ntdll.dll+0x21639") == 1.0 / 4)
    assert(n1.getProbability("ntdll.dll+0xeac7") == 2.0 / 4)
    n1.updateEvents("kernel32.dll+0x15568")
    assert(n1.getProbability("ntdll.dll+0x2173e") == 1.0 / 5)
    assert(n1.getProbability("ntdll.dll+0x21639") == 1.0 / 5)
    assert(n1.getProbability("ntdll.dll+0xeac7") == 2.0 / 5)
    assert(n1.getProbability("kernel32.dll+0x15568") == 1.0 / 5)
    n1.updateEvents("ntdll.dll+0x2173e")
    assert(n1.getProbability("ntdll.dll+0x2173e") == 2.0 / 6)
    assert(n1.getProbability("ntdll.dll+0x21639") == 1.0 / 6)
    assert(n1.getProbability("ntdll.dll+0xeac7") == 2.0 / 6)
    assert(n1.getProbability("kernel32.dll+0x15568") == 1.0 / 6)
  }
  test("Sequence getProbability for String events after multiple updates. (Int key)") {
    val n1 = new Node[Int, String](5, 1, 1, 0.0, 1.0)
    n1.updateEvents("ntdll.dll+0x2173e")
    assert(n1.getProbability("ntdll.dll+0x2173e") == 1.0)
    n1.updateEvents("ntdll.dll+0x21639")
    assert(n1.getProbability("ntdll.dll+0x2173e") == 0.5)
    assert(n1.getProbability("ntdll.dll+0x21639") == 0.5)
    n1.updateEvents("ntdll.dll+0xeac7")
    assert(n1.getProbability("ntdll.dll+0x2173e") == 1.0 / 3)
    assert(n1.getProbability("ntdll.dll+0x21639") == 1.0 / 3)
    assert(n1.getProbability("ntdll.dll+0xeac7") == 1.0 / 3)
    n1.updateEvents("ntdll.dll+0xeac7")
    assert(n1.getProbability("ntdll.dll+0x2173e") == 1.0 / 4)
    assert(n1.getProbability("ntdll.dll+0x21639") == 1.0 / 4)
    assert(n1.getProbability("ntdll.dll+0xeac7") == 2.0 / 4)
    n1.updateEvents("kernel32.dll+0x15568")
    assert(n1.getProbability("ntdll.dll+0x2173e") == 1.0 / 5)
    assert(n1.getProbability("ntdll.dll+0x21639") == 1.0 / 5)
    assert(n1.getProbability("ntdll.dll+0xeac7") == 2.0 / 5)
    assert(n1.getProbability("kernel32.dll+0x15568") == 1.0 / 5)
    n1.updateEvents("ntdll.dll+0x2173e")
    assert(n1.getProbability("ntdll.dll+0x2173e") == 2.0 / 6)
    assert(n1.getProbability("ntdll.dll+0x21639") == 1.0 / 6)
    assert(n1.getProbability("ntdll.dll+0xeac7") == 2.0 / 6)
    assert(n1.getProbability("kernel32.dll+0x15568") == 1.0 / 6)
  }
  test("SparseMarkovTree eventCount == events sum in events") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)
    n1.updateEvents(666)
    assert(n1.getEventCount == 1)
    var eventNum = 0
    for (e <- n1.getEvents) {
      eventNum += e._2
    }
    assert(eventNum == n1.getEventCount)

    n1.updateEvents(666)
    assert(n1.getEventCount == 2)
    var eventNum2 = 0
    for (e <- n1.getEvents) {
      eventNum2 += e._2
    }
    assert(eventNum2 == n1.getEventCount)
    assert(eventNum2 == 2)
    assert(2 == n1.getEventCount)

    n1.updateEvents(777)
    assert(n1.getEventCount == 3)
    var eventNum3 = 0
    for (e <- n1.getEvents) {
      eventNum3 += e._2
    }
    assert(eventNum3 == n1.getEventCount)
    assert(eventNum3 == 3)
    assert(3 == n1.getEventCount)

    n1.updateEvents(888)
    assert(n1.getEventCount == 4)
    var eventNum4 = 0
    for (e <- n1.getEvents) {
      eventNum4 += e._2
    }
    assert(eventNum4 == n1.getEventCount)
    assert(eventNum4 == 4)
    assert(4 == n1.getEventCount)
  }
  test("SparseMarkovTree, invalid argument to learn: empty Vector| event") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)
    n1.learn(Vector.empty[Int], 666)
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    assert(n1.getChildren.isEmpty)
  }
  test("SparseMarkovTree, learn called with Vector size == 1 | event") {
    val n1 = new Node[Int, Int](5, 1, 1, 0.0, 1.0)
    val condition = Vector(1)
    val event = 666
    n1.learn(condition, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 1)
    assert(n1.getChildren(0).size == 1)
    n1.getChildren(0)(0) shouldBe a[SequenceList[_, _]]

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
  test("SparseMarkovTree, learn called with Vector size == 2 | event") {
    val maxDepth = 5
    val maxPhi = 1
    val maxSeqCount = 1
    val smoothing = 0.0
    val prior = 1.0
    val n1 = new Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    val condition = Vector(1, 2)
    val event = 666
    n1.learn(condition, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 1)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[SequenceList[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    val child0: SequenceList[Int, Int] = n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]
    val child1: SequenceList[Int, Int] = n1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val child = n1.getChildren(i)(0).asInstanceOf[SequenceList[Int, Int]]
      assert(child.getSmoothing == smoothing)
      assert(child.getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
      assert(child.getPrior == 1.0 * 1 / 2)
      assert(child.getKeys.size == 1)
      assert(child.getKeys(0) == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
    }
  }
  test("SparseMarkovTree, learn called with Vector size == 3 | event") {
    val maxDepth = 5
    val maxPhi = 1
    val maxSeqCount = 1
    val smoothing = 0.0
    val prior = 1.0
    val n1 = new Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    val condition = Vector(1, 2, 3)
    val event = 666
    n1.learn(condition, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 1)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[SequenceList[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    val child0: SequenceList[Int, Int] = n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]
    val child1: SequenceList[Int, Int] = n1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val child = n1.getChildren(i)(0).asInstanceOf[SequenceList[Int, Int]]
      assert(child.getSmoothing == smoothing)
      assert(child.getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
      assert(child.getPrior == 1.0 * 1 / 2)
      assert(child.getKeys.size == 1)
      assert(child.getKeys(0) == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
    }

  }
  test("SparseMarkovTree, learn called with two 'Vector size == 2 | event' sequences") {
    val maxDepth = 2
    val maxPhi = 1
    val maxSeqCount = 1
    val smoothing = 0.0
    val prior = 1.0
    val n1 = new Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    val condition = Vector(1, 2)
    val condition2 = Vector(3, 4)
    val event = 666
    n1.learn(condition, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 1)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[SequenceList[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    val child0: SequenceList[Int, Int] = n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]
    val child1: SequenceList[Int, Int] = n1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val child = n1.getChildren(i)(0).asInstanceOf[SequenceList[Int, Int]]
      assert(child.getSmoothing == smoothing)
      assert(child.getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
      assert(child.getPrior == 1.0 * 1 / 2)
      assert(child.getKeys.size == 1)
      assert(child.getKeys(0) == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
    }

    n1.learn(condition2, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 2)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[Node[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    for (i <- n1.getChildren.indices) {
      val c = n1.getChildren(i)(0)

      c match {
        case child: SequenceList[Int, Int] => {
          assert(child.getSmoothing == smoothing)
          assert(child.getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
          assert(child.getPrior == 1.0 * 1 / 2)
          assert(child.getKeys.size == 2)
          assert(child.getKeys(0) == condition.drop(i))
          assert(child.getKeys(1) == condition2.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
          assert(child.getSequence(condition2.drop(i)).get.getKey == condition2.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event) == 1.00)
        }
        case _: Node[_, _] => {
          assert(n1.getChildren(i).size == 2)
          n1.getChildren(i) shouldBe a[Vector[Node[_, _]]]

          for (j <- n1.getChildren(i).indices) {
            val curr = n1.getChildren(i)(j).asInstanceOf[Node[_, _]]
            assert(curr.maxDepth == 1)
            assert(curr.getSmoothing == smoothing)
            assert(curr.getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
            assert(curr.getPrior == 1.0 * 1 / 2)
            if (j == 0) {
              assert(curr.getKey.get == condition.drop(i)(0))
              assert(curr.getChildren.size == 1)
              assert(curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].maxDepth == 0)
              assert(curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].maxPhi == maxPhi)
              assert(curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq(0).getSmoothing == smoothing)
              assert(currSeq(0).getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
              assert(currSeq(0).getPrior == 1.0 * 1 / 2)
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 2)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666) == 1.00)
              assert(currSeq(0).getProbability(777) == 0.00)
            }
            else if (j == 1) {
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 4)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666) == 1.00)
              assert(currSeq(0).getProbability(777) == 0.00)
            }
          }
        }
      }
    }
  }
  test("SparseMarkovTree, learn called with three 'Vector size == 2 | three events' sequences") {
    val maxDepth = 2
    val maxPhi = 1
    val maxSeqCount = 1
    val smoothing = 0.0
    val prior = 1.0
    val n1 = new Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    val condition = Vector(1, 2)
    val condition2 = Vector(3, 4)
    val condition3 = Vector(5, 6)
    val event = 666
    val event2 = 777
    val event3 = 888

    n1.learn(condition, event)
    n1.learn(condition2, event2)
    n1.learn(condition3, event3)

    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 3)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[Node[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val c = n1.getChildren(i)(0)

      c match {
        case child: SequenceList[Int, Int] => {
          assert(child.getSmoothing == smoothing)
          assert(child.getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
          assert(child.getPrior == 1.0 * 1 / 2)
          assert(child.getKeys.size == 3)
          assert(child.getKeys(0) == condition.drop(i))
          assert(child.getKeys(1) == condition2.drop(i))
          assert(child.getKeys(2) == condition3.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
          assert(child.getSequence(condition2.drop(i)).get.getKey == condition2.drop(i))
          assert(child.getSequence(condition3.drop(i)).get.getKey == condition3.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition3.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition3.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event2) == 1)
          assert(child.getSequence(condition3.drop(i)).get.getEvents(event3) == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event2) == 1.00)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions(event3) == 1.00)
        }
        case _: Node[Int, Int] => {
          assert(n1.getChildren(i).size == 3)
          n1.getChildren(i) shouldBe a[Vector[Node[_, _]]]

          for (j <- n1.getChildren(i).indices) {
            val curr = n1.getChildren(i)(j).asInstanceOf[Node[_, _]]
            assert(curr.getSmoothing == smoothing)
            assert(curr.getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
            assert(curr.getPrior == 1.0 * 1 / 2)

            if (j == 0) {
              assert(curr.getKey.get == condition.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq(0).getSmoothing == smoothing)
              assert(currSeq(0).getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
              assert(currSeq(0).getPrior == 1.0 * 1 / 2)
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 2)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666) == 1.00)
              assert(currSeq(0).getProbability(777) == 0.00)
            }
            else if (j == 1) {
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq(0).getSmoothing == smoothing)
              assert(currSeq(0).getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
              assert(currSeq(0).getPrior == 1.0 * 1 / 2)
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 4)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(777) == 1.00)
              assert(currSeq(0).getProbability(666) == 0.00)
            }
            else if (j == 2) {
              assert(curr.getKey.get == condition3.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq(0).getSmoothing == smoothing)
              assert(currSeq(0).getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
              assert(currSeq(0).getPrior == 1.0 * 1 / 2)
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 6)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(888) == 1.00)
              assert(currSeq(0).getProbability(777) == 0.00)
            }
          }
        }
      }
    }
  }
  test("SparseMarkovTree, learn called with three 'Vector size == 2 | three events' sequences twice each") {
    val n1 = new Node[Int, Int](2, 1, 1, 0.0, 1.0)
    val condition = Vector(1, 2)
    val condition2 = Vector(3, 4)
    val condition3 = Vector(5, 6)
    val event = 666
    val event2 = 777
    val event3 = 888

    n1.learn(condition, event)
    n1.learn(condition2, event2)
    n1.learn(condition3, event3)

    n1.learn(condition, event)
    n1.learn(condition2, event2)
    n1.learn(condition3, event3)

    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 3)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[Node[Int, Int]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val c = n1.getChildren(i)(0)

      c match {
        case child: SequenceList[Int, Int] => {
          assert(child.getKeys.size == 3)
          assert(child.getKeys(0) == condition.drop(i))
          assert(child.getKeys(1) == condition2.drop(i))
          assert(child.getKeys(2) == condition3.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
          assert(child.getSequence(condition2.drop(i)).get.getKey == condition2.drop(i))
          assert(child.getSequence(condition3.drop(i)).get.getKey == condition3.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getEventCount == 2)
          assert(child.getSequence(condition2.drop(i)).get.getEventCount == 2)
          assert(child.getSequence(condition3.drop(i)).get.getEventCount == 2)
          assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition3.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 2)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event2) == 2)
          assert(child.getSequence(condition3.drop(i)).get.getEvents(event3) == 2)
          assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event2) == 1.00)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions(event3) == 1.00)
        }
        case _: Node[Int, Int] => {
          assert(n1.getChildren(i).size == 3)
          n1.getChildren(i) shouldBe a[Vector[Node[_, _]]]

          for (j <- n1.getChildren(i).indices) {
            val curr = n1.getChildren(i)(j).asInstanceOf[Node[_, _]]

            if (j == 0) {
              assert(curr.getKey.get == condition.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 2)
              assert(currSeq(0).getEventCount == 2)
              assert(currSeq(0).getEvents.size == 1)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666) == 1.00)
              assert(currSeq(0).getProbability(777) == 0.00)
            }
            else if (j == 1) {
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 4)
              assert(currSeq(0).getEventCount == 2)
              assert(currSeq(0).getEvents.size == 1)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(777) == 1.00)
              assert(currSeq(0).getProbability(666) == 0.00)
            }
            else if (j == 2) {
              assert(curr.getKey.get == condition3.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 6)
              assert(currSeq(0).getEventCount == 2)
              assert(currSeq(0).getEvents.size == 1)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(888) == 1.00)
              assert(currSeq(0).getProbability(777) == 0.00)
            }
          }
        }
      }
    }
  }
  test("SparseMarkovTree, learn called with three 'Vector size == 2 | three events' sequences twice each with different events for each") {
    val n1 = new Node[Int, Int](2, 1, 1, 0.0, 1.0)
    val condition = Vector(1, 2)
    val condition2 = Vector(3, 4)
    val condition3 = Vector(5, 6)
    val event = 666
    val event2 = 777
    val event3 = 888

    n1.learn(condition, event)
    n1.learn(condition2, event2)
    n1.learn(condition3, event3)

    n1.learn(condition, event3)
    n1.learn(condition2, event)
    n1.learn(condition3, event2)

    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 3)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[Node[Int, Int]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val c = n1.getChildren(i)(0)

      c match {
        case child: SequenceList[Int, Int] => {
          assert(child.getKeys.size == 3)
          assert(child.getKeys(0) == condition.drop(i))
          assert(child.getKeys(1) == condition2.drop(i))
          assert(child.getKeys(2) == condition3.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
          assert(child.getSequence(condition2.drop(i)).get.getKey == condition2.drop(i))
          assert(child.getSequence(condition3.drop(i)).get.getKey == condition3.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getEventCount == 2)
          assert(child.getSequence(condition2.drop(i)).get.getEventCount == 2)
          assert(child.getSequence(condition3.drop(i)).get.getEventCount == 2)
          assert(child.getSequence(condition.drop(i)).get.getEvents.size == 2)
          assert(child.getSequence(condition2.drop(i)).get.getEvents.size == 2)
          assert(child.getSequence(condition3.drop(i)).get.getEvents.size == 2)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event3) == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event2) == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition3.drop(i)).get.getEvents(event3) == 1)
          assert(child.getSequence(condition3.drop(i)).get.getEvents(event2) == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 2)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions.size == 2)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions.size == 2)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 0.50)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event3) == 0.50)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event2) == 0.50)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event) == 0.50)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions(event3) == 0.50)
          assert(child.getSequence(condition3.drop(i)).get.getPredictions(event2) == 0.50)
        }
        case _: Node[Int, Int] => {
          assert(n1.getChildren(i).size == 3)
          n1.getChildren(i) shouldBe a[Vector[Node[_, _]]]

          for (j <- n1.getChildren(i).indices) {
            val curr = n1.getChildren(i)(j).asInstanceOf[Node[_, _]]

            if (j == 0) {
              assert(curr.getKey.get == condition.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 2)
              assert(currSeq(0).getEventCount == 2)
              assert(currSeq(0).getEvents.size == 2)
              assert(currSeq(0).getPredictions.size == 2)
              assert(currSeq(0).getProbability(666) == 0.50)
              assert(currSeq(0).getProbability(888) == 0.50)
              assert(currSeq(0).getProbability(999) == 0.00)
            }
            else if (j == 1) {
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 4)
              assert(currSeq(0).getEventCount == 2)
              assert(currSeq(0).getEvents.size == 2)
              assert(currSeq(0).getPredictions.size == 2)
              assert(currSeq(0).getProbability(777) == 0.50)
              assert(currSeq(0).getProbability(666) == 0.50)
              assert(currSeq(0).getProbability(999) == 0.00)
            }
            else if (j == 2) {
              assert(curr.getKey.get == condition3.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 6)
              assert(currSeq(0).getEvents.size == 2)
              assert(currSeq(0).getPredictions.size == 2)
              assert(currSeq(0).getProbability(888) == 0.50)
              assert(currSeq(0).getProbability(777) == 0.50)
              assert(currSeq(0).getProbability(999) == 0.00)
            }
          }
        }
      }
    }
  }
  test("SparseMarkovTree, maxDepth 3 - learn called with two 'Vector size == 2 | event'") {
    val n1 = new Node[Int, Int](3, 1, 1, 0.0, 1.0)
    val condition = Vector(1, 2)
    val condition2 = Vector(3, 4)
    val event = 666

    n1.learn(condition, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 1)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[SequenceList[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    val child0: SequenceList[Int, Int] = n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]
    val child1: SequenceList[Int, Int] = n1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val child = n1.getChildren(i)(0).asInstanceOf[SequenceList[Int, Int]]
      assert(child.getKeys.size == 1)
      assert(child.getKeys(0) == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
    }

    n1.learn(condition2, event)

    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 2)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[Node[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    for (i <- n1.getChildren.indices) {
      val c = n1.getChildren(i)(0)

      c match {
        case child: SequenceList[Int, Int] => {
          assert(child.getKeys.size == 2)
          assert(child.getKeys(0) == condition.drop(i))
          assert(child.getKeys(1) == condition2.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
          assert(child.getSequence(condition2.drop(i)).get.getKey == condition2.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event) == 1.00)
        }
        case _: Node[Int, Int] => {
          assert(n1.getChildren(i).size == 2)
          n1.getChildren(i) shouldBe a[Vector[Node[_, _]]]

          for (j <- n1.getChildren(i).indices) {
            val curr = n1.getChildren(i)(j).asInstanceOf[Node[_, _]]

            if (j == 0) {
              assert(curr.getKey.get == condition.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 2)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666) == 1.00)
              assert(currSeq(0).getProbability(777) == 0.00)
            }
            else if (j == 1) {
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 4)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666) == 1.00)
              assert(currSeq(0).getProbability(777) == 0.00)
            }
          }

        }
      }
    }
  }
  test("SparseMarkovTree, maxDepth 3 - learn called with two 'Vector size == 2 | event' - first element same") {
    val n1 = new Node[Int, Int](3, 1, 1, 0.0, 1.0)
    val condition = Vector(1, 2)
    val condition2 = Vector(1, 4)
    val event = 666

    n1.learn(condition, event)
    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 1)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[SequenceList[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    val child0: SequenceList[Int, Int] = n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]
    val child1: SequenceList[Int, Int] = n1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]]

    for (i <- n1.getChildren.indices) {
      val child = n1.getChildren(i)(0).asInstanceOf[SequenceList[Int, Int]]
      assert(child.getKeys.size == 1)
      assert(child.getKeys(0) == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
      assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
      assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
    }

    n1.learn(condition2, event)

    n1.getKey shouldBe None
    assert(n1.getEventCount == 0)
    assert(n1.getEvents.isEmpty)
    assert(n1.getPredictions.isEmpty)
    n1.getPredictions.get(event) shouldBe None
    assert(n1.getChildren.size == 2)
    assert(n1.getChildren(0).size == 1)
    assert(n1.getChildren(1).size == 1)
    n1.getChildren(0)(0) shouldBe a[Node[_, _]]
    n1.getChildren(1)(0) shouldBe a[SequenceList[_, _]]

    for (i <- n1.getChildren.indices) {
      val c = n1.getChildren(i)(0)

      c match {
        case child: SequenceList[Int, Int] => {
          assert(child.getKeys(0) == condition.drop(i))
          assert(child.getKeys(1) == condition2.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getKey == condition.drop(i))
          assert(child.getSequence(condition2.drop(i)).get.getKey == condition2.drop(i))
          assert(child.getSequence(condition.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEventCount == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition2.drop(i)).get.getEvents(event) == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions.size == 1)
          assert(child.getSequence(condition.drop(i)).get.getPredictions(event) == 1.00)
          assert(child.getSequence(condition2.drop(i)).get.getPredictions(event) == 1.00)
        }
        case _: Node[_, _] => {
          assert(n1.getChildren(i).size == 1)
          n1.getChildren(i) shouldBe a[Vector[Node[_, _]]]

          for (j <- n1.getChildren(i).indices) {
            val curr = n1.getChildren(i)(j).asInstanceOf[Node[_, _]]

            if (j == 0) {
              assert(curr.getKey.get == condition.drop(i)(0))
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 2)
              assert(currSeq(0).getKey(0) == 2)
              assert(currSeq(1).getKey(0) == 4)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(1).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666) == 1.00)
              assert(currSeq(1).getProbability(666) == 1.00)
              assert(currSeq(0).getProbability(777) == 0.00)
              assert(currSeq(1).getProbability(777) == 0.00)
            }
            else if (j == 1) {
              assert(curr.getKey.get == condition2.drop(i)(0))
              assert(curr.getChildren.size == 1)
              val currSeq = curr.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences
              assert(currSeq.size == 1)
              assert(currSeq(0).getKey(0) == 4)
              assert(currSeq(0).getPredictions.size == 1)
              assert(currSeq(0).getProbability(666) == 1.00)
              assert(currSeq(0).getProbability(777) == 0.00)
            }
          }
        }
      }
    }
  }
  test("SparseMarkovTree, mD = 5, window size 5") {
    val maxDepth = 5
    val maxPhi = 2
    val maxSeqCount = 1
    val smoothing = 0.1
    val prior = 1.0
    val n1 = new Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)

    val event = 666
    val event2 = 777
    val event3 = 888

    val t1 = Vector(1, 2, 9, 4, 5)
    val t2 = Vector(5, 4, 3, 2, 1)
    val t3 = Vector(6, 7, 8, 9, 10)
    val t4 = Vector(1, 3, 9, 4, 5)

    n1.learn(t1, event)
    n1.learn(t2, event)

    assert(n1.getChildren.size == 3)
    assert(n1.getChildren(0).size == 2)
    assert(n1.getChildren(1).size == 2)
    assert(n1.getChildren(2).size == 2)

    val phi_0 = n1.getChildren(0)
    val phi_0_node_0 = phi_0(0).asInstanceOf[Node[Int, Int]]
    val phi_0_node_1 = phi_0(1).asInstanceOf[Node[Int, Int]]

    val phi_1 = n1.getChildren(1)
    val phi_1_node_0 = phi_1(0).asInstanceOf[Node[Int, Int]]
    val phi_1_node_1 = phi_1(1).asInstanceOf[Node[Int, Int]]

    val phi_2 = n1.getChildren(2)
    val phi_2_node_0 = phi_2(0).asInstanceOf[Node[Int, Int]]
    val phi_2_node_1 = phi_2(1).asInstanceOf[Node[Int, Int]]

    assert(phi_0_node_0.getKey.get == t1.head)
    assert(phi_0_node_0.maxDepth == n1.maxDepth - 1)
    assert(phi_0_node_0.getSmoothing == smoothing)
    assert(phi_0_node_0.getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
    assert(phi_0_node_0.getPrior == 1.0 * 1 / 3)
    assert(phi_0_node_0.getChildren.size == 3)
    assert(phi_0_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(1))
    assert(phi_0_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(2))
    assert(phi_0_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_0_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    assert(phi_0_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    assert(phi_0_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val seq1 = phi_0_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences(0)
    val seq2 = phi_0_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].sequences(0)
    val seq3 = phi_0_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].sequences(0)
    assert(seq1.getPrior == phi_0_node_0.getPrior * 1 / 3)
    assert(seq2.getPrior == phi_0_node_0.getPrior * 1 / 3)
    assert(seq3.getPrior == phi_0_node_0.getPrior * 1 / 3)


    assert(phi_0_node_1.getKey.get == t2.head)
    assert(phi_0_node_1.maxDepth == n1.maxDepth - 1)
    assert(phi_0_node_1.getSmoothing == smoothing)
    assert(phi_0_node_1.getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
    assert(phi_0_node_1.getPrior == 1.0 * 1 / 3)
    assert(phi_0_node_1.getChildren.size == 3)
    assert(phi_0_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(1))
    assert(phi_0_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(2))
    assert(phi_0_node_1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_0_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    assert(phi_0_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    assert(phi_0_node_1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_1_node_0.getKey.get == t1.drop(1).head)
    assert(phi_1_node_0.maxDepth == n1.maxDepth - 2)
    assert(phi_1_node_0.getSmoothing == smoothing)
    assert(phi_1_node_0.getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
    assert(phi_1_node_0.getPrior == 1.0 * 1 / 3)
    assert(phi_1_node_0.getChildren.size == 3)
    assert(phi_1_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(2))
    assert(phi_1_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_1_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(4))
    assert(phi_1_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_1_node_1.getKey.get == t2.drop(1).head)
    assert(phi_1_node_1.maxDepth == n1.maxDepth - 2)
    assert(phi_1_node_1.getSmoothing == smoothing)
    assert(phi_1_node_1.getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
    assert(phi_1_node_1.getPrior == 1.0 * 1 / 3)
    assert(phi_1_node_1.getChildren.size == 3)
    assert(phi_1_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(2))
    assert(phi_1_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_1_node_1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(4))
    assert(phi_1_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_2_node_0.getKey.get == t1.drop(2).head)
    assert(phi_2_node_0.maxDepth == n1.maxDepth - 3)
    assert(phi_2_node_0.getSmoothing == smoothing)
    assert(phi_2_node_0.getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
    assert(phi_2_node_0.getPrior == 1.0 * 1 / 3)
    assert(phi_2_node_0.getChildren.size == 2)
    assert(phi_2_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_2_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(4))
    assert(phi_2_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val seq4 = phi_2_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences(0)
    val seq5 = phi_2_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].sequences(0)
    assert(seq4.getPrior == phi_2_node_0.getPrior * 1 / 2)
    assert(seq5.getPrior == phi_2_node_0.getPrior * 1 / 2)

    assert(phi_2_node_1.getKey.get == t2.drop(2).head)
    assert(phi_2_node_1.maxDepth == n1.maxDepth - 3)
    assert(phi_2_node_1.getSmoothing == smoothing)
    assert(phi_2_node_1.getPrior == n1.getPrior * (1.0 / (n1.maxPhi + 1)))
    assert(phi_2_node_1.getPrior == 1.0 * 1 / 3)
    assert(phi_2_node_1.getChildren.size == 2)
    assert(phi_2_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_2_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(4))
    assert(phi_2_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val seq6 = phi_2_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences(0)
    val seq7 = phi_2_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].sequences(0)
    assert(seq6.getPrior == phi_2_node_1.getPrior * 1 / 2)
    assert(seq7.getPrior == phi_2_node_1.getPrior * 1 / 2)

    n1.learn(t3, event2)

    assert(n1.getChildren.size == 3)
    assert(n1.getChildren(0).size == 3)
    assert(n1.getChildren(1).size == 3)
    assert(n1.getChildren(2).size == 3)

    val newPhi_0 = n1.getChildren(0)
    val newPhi_1 = n1.getChildren(1)
    val newPhi_2 = n1.getChildren(2)

    val phi_0_node_2 = newPhi_0(2).asInstanceOf[Node[Int, Int]]
    val phi_1_node_2 = newPhi_1(2).asInstanceOf[Node[Int, Int]]
    val phi_2_node_2 = newPhi_2(2).asInstanceOf[Node[Int, Int]]

    assert(phi_0_node_0.getKey.get == t1.head)
    assert(phi_0_node_0.getChildren.size == 3)
    assert(phi_0_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(1))
    assert(phi_0_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(2))
    assert(phi_0_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_0_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_0_node_1.getKey.get == t2.head)
    assert(phi_0_node_1.getChildren.size == 3)
    assert(phi_0_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(1))
    assert(phi_0_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(2))
    assert(phi_0_node_1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_0_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_0_node_2.getKey.get == t3.head)
    assert(phi_0_node_2.maxDepth == n1.maxDepth - 1)
    assert(phi_0_node_2.maxDepth == 4)
    assert(phi_0_node_2.getChildren.size == 3)
    assert(phi_0_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(1))
    assert(phi_0_node_2.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(2))
    assert(phi_0_node_2.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(3))
    assert(phi_0_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val newSeq0 = phi_0_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(t3.drop(1)).get
    assert(newSeq0.getKey == t3.drop(1))
    assert(newSeq0.getEventCount == 1)
    assert(newSeq0.getEvents(event2) == 1)
    val newSeq0Prior = newSeq0.getPrior
    val newSeqWeight = newSeq0.getWeight
    assert(newSeq0Prior == prior * 1 / 3 * 1 / 3)
    assert(newSeq0Prior == 1.0 / 3 * 1 / 3)
    assert(newSeqWeight == (1.0 / 3 * 1.0 / 3 * (1.0 + smoothing)))

    assert(newSeq0.getWeightedProbability(event2) == ((1.0 + smoothing) * newSeqWeight))
    assert(newSeq0.getWeightedProbability(event2) == ((1.0 + smoothing) * newSeqWeight))
    assert(newSeq0.getWeightedProbability(event2) == ((1.0 + smoothing) * newSeqWeight))
    assert(newSeq0.getWeightedProbability(event2) == ((1.0 + smoothing) * newSeqWeight))

    assert(newSeq0.getWeightedProbability(event2) == (1.0 / 3 * 1.0 / 3 * (1.0 + smoothing) * (1.0 + smoothing)))
    assert(newSeq0.getWeightedProbability(event2) == prior * 1.0 / 3 * 1.0 / 3 * (1.0 + 0.1) * (1.0 + 0.1))
    assert(newSeq0.getProbability(event2) == 1.00 + smoothing)
    assert(newSeq0.getProbability(event2) == 1.00 + smoothing)
    assert(newSeq0.getProbability(event2) == 1.00 + newSeq0.getSmoothing)

    assert(phi_1_node_0.getKey.get == t1.drop(1).head)
    assert(phi_1_node_0.getChildren.size == 3)
    assert(phi_1_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(2))
    assert(phi_1_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_1_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(4))
    assert(phi_1_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_1_node_1.getKey.get == t2.drop(1).head)
    assert(phi_1_node_1.getChildren.size == 3)
    assert(phi_1_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(2))
    assert(phi_1_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_1_node_1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(4))
    assert(phi_1_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_1_node_2.getKey.get == t3.drop(1).head)
    assert(phi_1_node_2.getChildren.size == 3)
    assert(phi_1_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(2))
    assert(phi_1_node_2.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(3))
    assert(phi_1_node_2.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(4))
    assert(phi_1_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val seqlist = phi_1_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]
    val newSeq1 = seqlist.getSequence(t3.drop(2)).get
    assert(seqlist.maxDepth == 2)
    assert(seqlist.getPrior == 1.0 / 3 * 1.0 / 3)
    assert(newSeq1.getKey == t3.drop(2))
    assert(newSeq1.getPrior == 1.0 / 3 * 1.0 / 3)
    assert(newSeq1.getWeight == 1.0 / 3 * 1.0 / 3 * (1.0 + smoothing))
    assert(newSeq1.getWeight == prior * 1.0 / 3 * 1.0 / 3 * (1.0 + smoothing))
    assert(newSeq1.getEventCount == 1)
    assert(newSeq1.getEvents(event2) == 1)
    assert(newSeq1.getProbability(event2) == 1.1)
    assert(newSeq1.getProbability(event2) == 1.0 + smoothing)
    assert(newSeq1.getWeightedProbability(event2) == prior * 1.0 / 3 * 1.0 / 3 * (1.0 + smoothing) * (1.0 + smoothing))

    assert(phi_2_node_0.getKey.get == t1.drop(2).head)
    assert(phi_2_node_0.getChildren.size == 2)
    assert(phi_2_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_2_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(4))
    assert(phi_2_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_2_node_1.getKey.get == t2.drop(2).head)
    assert(phi_2_node_1.getChildren.size == 2)
    assert(phi_2_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_2_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(4))
    assert(phi_2_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_2_node_2.getKey.get == t3.drop(2).head)
    assert(phi_2_node_2.getChildren.size == 2)
    assert(phi_2_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(3))
    assert(phi_2_node_2.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(4))
    assert(phi_2_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val newSeq2 = phi_2_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(t3.drop(3)).get
    assert(newSeq2.getKey == t3.drop(3))
    assert(newSeq2.getEventCount == 1)
    assert(newSeq2.getEvents(event2) == 1)
    assert(newSeq2.getProbability(event2) == 1.1)


    n1.learn(t3, event)

    assert(n1.getChildren.size == 3)
    assert(n1.getChildren(0).size == 3)
    assert(n1.getChildren(1).size == 3)
    assert(n1.getChildren(2).size == 3)

    val newNewPhi_0 = n1.getChildren(0)
    val newNewPhi_1 = n1.getChildren(1)
    val newNewPhi_2 = n1.getChildren(2)

    val newPhi_0_node_2 = newNewPhi_0(2).asInstanceOf[Node[Int, Int]]
    val newPhi_1_node_2 = newNewPhi_1(2).asInstanceOf[Node[Int, Int]]
    val newPhi_2_node_2 = newNewPhi_2(2).asInstanceOf[Node[Int, Int]]

    assert(phi_0_node_0.getKey.get == t1.head)
    assert(phi_0_node_0.getChildren.size == 3)
    assert(phi_0_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(1))
    assert(phi_0_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(2))
    assert(phi_0_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_0_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_0_node_1.getKey.get == t2.head)
    assert(phi_0_node_1.getChildren.size == 3)
    assert(phi_0_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(1))
    assert(phi_0_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(2))
    assert(phi_0_node_1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_0_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(newPhi_0_node_2.getKey.get == t3.head)
    assert(newPhi_0_node_2.getChildren.size == 3)
    assert(newPhi_0_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(1))
    assert(newPhi_0_node_2.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(2))
    assert(newPhi_0_node_2.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(3))
    assert(newPhi_0_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val newNewSeq0 = newPhi_0_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(t3.drop(1)).get
    assert(newNewSeq0.getKey == t3.drop(1))
    assert(newNewSeq0.getEventCount == 2)
    assert(newNewSeq0.getEvents(event2) == 1)
    assert(newNewSeq0.getEvents(event) == 1)
    val newSeq0Prior2 = newNewSeq0.getPrior
    val newSeqWeight2 = newNewSeq0.getWeight
    assert(newSeq0Prior2 == prior * 1 / 3 * 1 / 3)
    assert(newSeq0Prior2 == 1.0 / 3 * 1 / 3)
    assert(newSeqWeight2 == (prior * 1.0 / 3 * 1.0 / 3 * (1.0 + smoothing) * (1.0 + smoothing) / 2))
    assert(newNewSeq0.getWeightedProbability(event2) == ((1.0 + smoothing) / 2 * newSeqWeight2))
    assert(newNewSeq0.getWeightedProbability(event) == ((1.0 + smoothing) / 2 * newSeqWeight2))
    assert(newNewSeq0.getProbability(event2) == (1.0 + smoothing) / 2)
    assert(newNewSeq0.getProbability(event) == (1.0 + smoothing) / 2)

    assert(phi_1_node_0.getKey.get == t1.drop(1).head)
    assert(phi_1_node_0.getChildren.size == 3)
    assert(phi_1_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(2))
    assert(phi_1_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_1_node_0.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(4))
    assert(phi_1_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_1_node_1.getKey.get == t2.drop(1).head)
    assert(phi_1_node_1.getChildren.size == 3)
    assert(phi_1_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(2))
    assert(phi_1_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_1_node_1.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(4))
    assert(phi_1_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(newPhi_1_node_2.getKey.get == t3.drop(1).head)
    assert(newPhi_1_node_2.getChildren.size == 3)
    assert(newPhi_1_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(2))
    assert(newPhi_1_node_2.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(3))
    assert(newPhi_1_node_2.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(4))
    assert(newPhi_1_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val newNewSeq1 = newPhi_1_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(t3.drop(2)).get
    assert(newNewSeq1.getKey == t3.drop(2))
    assert(newNewSeq1.getEventCount == 2)
    assert(newNewSeq1.getEvents(event2) == 1)
    assert(newNewSeq1.getEvents(event) == 1)
    val newSeq1Prior2 = newNewSeq1.getPrior
    val newSeq1Weight2 = newNewSeq1.getWeight
    assert(newSeq1Prior2 == prior * 1 / 3 * 1 / 3)
    assert(newSeq1Prior2 == 1.0 / 3 * 1 / 3)
    assert(newSeq1Weight2 == (prior * 1.0 / 3 * 1.0 / 3 * (1.0 + smoothing) * (1.0 + smoothing) / 2))
    assert(newNewSeq1.getWeightedProbability(event2) == ((1.0 + smoothing) / 2 * newSeq1Weight2))
    assert(newNewSeq1.getWeightedProbability(event) == ((1.0 + smoothing) / 2 * newSeq1Weight2))
    assert(newNewSeq1.getProbability(event2) == (1.0 + smoothing) / 2)
    assert(newNewSeq1.getProbability(event) == (1.0 + smoothing) / 2)

    assert(phi_2_node_0.getKey.get == t1.drop(2).head)
    assert(phi_2_node_0.getChildren.size == 2)
    assert(phi_2_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(3))
    assert(phi_2_node_0.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t1.drop(4))
    assert(phi_2_node_0.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(phi_2_node_1.getKey.get == t2.drop(2).head)
    assert(phi_2_node_1.getChildren.size == 2)
    assert(phi_2_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(3))
    assert(phi_2_node_1.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t2.drop(4))
    assert(phi_2_node_1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)

    assert(newPhi_2_node_2.getKey.get == t3.drop(2).head)
    assert(newPhi_2_node_2.getChildren.size == 2)
    assert(newPhi_2_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(3))
    assert(newPhi_2_node_2.getChildren(1)(0).asInstanceOf[SequenceList[Int, Int]].getKeys(0) == t3.drop(4))
    assert(newPhi_2_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].sequences.size == 1)
    val newNewSeq2 = newPhi_2_node_2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(t3.drop(3)).get
    assert(newNewSeq2.getKey == t3.drop(3))
    assert(newNewSeq2.getEventCount == 2)
    assert(newNewSeq2.getEvents(event2) == 1)
    assert(newNewSeq2.getEvents(event) == 1)
    val newSeq2Prior2 = newNewSeq2.getPrior
    val newSeq2Weight2 = newNewSeq2.getWeight
    assert(newSeq2Prior2 == prior * 1.0 / 3 * 1.0 / 2)
    assert(newSeq2Prior2 == 1.0 / 3 * 1.0 / 2)
    assert(newSeq2Weight2 == (prior * 1.0 / 3 * 1.0 / 2 * (1.0 + smoothing) * (1.0 + smoothing) / 2))
    assert(newNewSeq2.getWeightedProbability(event2) == ((1.0 + smoothing) / 2 * newSeq2Weight2))
    assert(newNewSeq2.getWeightedProbability(event) == ((1.0 + smoothing) / 2 * newSeq2Weight2))
    assert(newNewSeq2.getProbability(event2) == (1.0 + smoothing) / 2)
    assert(newNewSeq2.getProbability(event) == (1.0 + smoothing) / 2)

    //val t4 = Vector(1, 3, 9, 4, 5)
    n1.learn(t4, event2)

    val node1: Node[Int, Int] = n1.getChildren(0)(0).asInstanceOf[Node[Int, Int]].getChildren(0)(0).asInstanceOf[Node[Int, Int]]
    assert(node1.getKey.get == 2)

    //Route to node below: root -> Phi_0 -> Node(1) -> Phi_0 -> Node(2) -> Phi_0
    val newSplitSeqList1: SequenceList[Int, Int] = node1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]
    assert(newSplitSeqList1.maxDepth == 2)
    assert(newSplitSeqList1.getKeys.size == 1)
    newSplitSeqList1.getSequence(t1.drop(2)) shouldBe defined


    val node2: Node[Int, Int] = n1.getChildren(0)(0).asInstanceOf[Node[Int, Int]].getChildren(0)(1).asInstanceOf[Node[Int, Int]]
    assert(node2.getKey.get == 3)

    //Route to node below: root -> Phi_0 -> Node(1) -> Phi_0 -> Node(3) -> Phi_2
    val newSplitSeqList2: SequenceList[Int, Int] = node2.getChildren(2)(0).asInstanceOf[SequenceList[Int, Int]]
    assert(newSplitSeqList2.maxDepth == 0)
    assert(newSplitSeqList2.getKeys.size == 1)
    newSplitSeqList2.getSequence(t4.drop(4)) shouldBe defined

    val splitSeq2 = newSplitSeqList2.getSequence(t4.drop(4)).get
    val newSplitSeqListPrior2 = splitSeq2.getPrior
    val newSplitSeqListWeight2 = splitSeq2.getWeight
    assert(newSplitSeqListPrior2 == prior * 1.0 / 3 * 1.0 / 3 * 1.0 / 3)
    assert(newSplitSeqListPrior2 == 1.0 / 3 * 1.0 / 3 * 1.0 / 3)
    assert(newSplitSeqListWeight2 == (prior * 1.0 / 3 * 1.0 / 3 * 1.0 / 3 * (1.0 + smoothing)))
    assert(splitSeq2.getWeightedProbability(event2) == ((1.0 + smoothing) * newSplitSeqListWeight2))
    assert(splitSeq2.getProbability(event2) == (1.0 + smoothing))
    assert(splitSeq2.getProbability(event2) == 1.0 + smoothing)
    assert(splitSeq2.getPredictions.size == 1)
    assert(splitSeq2.getPredictions(event2) == 1.00 + smoothing)
    assert(splitSeq2.getEventCount == 1)
    assert(splitSeq2.getEvents.size == 1)
    assert(splitSeq2.getEvents(event2) == 1)

    assert(splitSeq2.getProbability(event2) == 1.00 + smoothing)
    assert(splitSeq2.getPredictions.size == 1)
    assert(splitSeq2.getPredictions(event2) == 1.00 + smoothing)
    assert(splitSeq2.getEventCount == 1)
    assert(splitSeq2.getEvents.size == 1)
    assert(splitSeq2.getEvents(event2) == 1)

    val node3: Node[Int, Int] = n1.getChildren(2)(0).asInstanceOf[Node[Int, Int]]
    assert(node3.getKey.get == 9)

    //Route to node below: root -> Phi_2 -> Node(9) -> Phi_0
    val newSplitSeqList3: SequenceList[Int, Int] = node3.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]]
    assert(newSplitSeqList3.maxDepth == 1)
    assert(newSplitSeqList3.getKeys.size == 1)
    newSplitSeqList3.getSequence(t1.drop(3)) shouldBe defined
    newSplitSeqList3.getSequence(t4.drop(3)) shouldBe defined

    val splitSeq3 = newSplitSeqList3.getSequence(t4.drop(3)).get
    val newSplitSeqListPrior3 = splitSeq3.getPrior
    val newSplitSeqListWeight3 = splitSeq3.getWeight
    assert(newSplitSeqListPrior3 == prior * 1.0 / 3 * 1.0 / 2)
    assert(newSplitSeqListPrior3 == 1.0 / 3 * 1.0 / 2)
    assert(newSplitSeqListWeight3 == (prior * 1.0 / 3 * 1.0 / 2 * (1.0 + smoothing) * (1.0 + smoothing) / 2))
    assert(splitSeq3.getWeightedProbability(event2) == (newSplitSeqListPrior3 * (1.0 + smoothing) * (1.0 + smoothing) / 2 * (1.0 + smoothing) / 2))

    assert(newSplitSeqList3.getSequence(t1.drop(3)).get.getProbability(event) == (1.0 + smoothing) / 2)
    assert(splitSeq3.getProbability(event2) == (1.0 + smoothing) / 2)
    assert(newSplitSeqList3.getSequence(t1.drop(3)).get.getPredictions.size == 2)
    assert(splitSeq3.getPredictions.size == 2)

    assert(newSplitSeqList3.getSequence(t1.drop(3)).get.getPredictions(event) == (1.0 + smoothing) / 2)
    assert(splitSeq3.getPredictions(event2) == (1.0 + smoothing) / 2)

    assert(newSplitSeqList3.getSequence(t1.drop(3)).get.getEventCount == 2)
    assert(splitSeq3.getEventCount == 2)

    assert(newSplitSeqList3.getSequence(t1.drop(3)).get.getEvents.size == 2)
    assert(splitSeq3.getEvents.size == 2)

    assert(newSplitSeqList3.getSequence(t1.drop(3)).get.getEvents(event) == 1)
    assert(splitSeq3.getEvents(event2) == 1)
  }


  /*

 test("Create tree models") {
    val extensions = List("GHC")
    val files = getListOfWindowsFiles(windowsTrainingDataWork, extensions)
    //val files = getListOfWindowsFiles(windowsTrainingDataHome, extensions)
    var maxSeqCount = 1000


   for (i <- 15 to 20) {
    /*  for (j <- 0 to 5) {
        for (k <- 1 to 10) {*/

          var maxDepth = i
          /*var maxPhi = j
          var smoothing = k.toDouble*/
          var maxPhi = 2
          var smoothing = 1.0

          try {
            val n1 = new Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, 1.0)
            //var in: BufferedReader = new BufferedReader(new FileReader(files(0)))
            var counter = 0

            for (f <- files) {
              counter += 1
              println("Processing file " + counter + " - filename: " + f.getName)
              val source = scala.io.Source.fromFile(f)
              val lines = try source.getLines mkString "\n" finally source.close()
              val wholeTrace: Vector[Int] = lines.split("\\s+").map(_.trim.toInt).toVector
              var trainingData_whole: Vector[(Vector[Int], Int)] = Vector[(Vector[Int], Int)]()
              for (t <- wholeTrace.sliding(maxDepth, 1)) {
                if (t.size == maxDepth)
                  trainingData_whole = trainingData_whole :+ (t.take(maxDepth - 1), t.takeRight(1)(0))
              }

              for (t <- trainingData_whole) {
                n1.learn(t._1, t._2)
              }
            }
            System.gc
            serializeTree(n1.asInstanceOf[SparseMarkovTree[Int, Int]], new File(serializePathHome + "SMT_" + maxDepth + "_" + maxPhi + "_" + smoothing + ".tmp"))
            System.gc
          } catch {
            case _: Exception => println("Exception. maxDepth: " + maxDepth + " - maxPhi: " + maxPhi + " - smoothing: " + smoothing)
          }
       /* }
        System.gc
      }
      System.gc*/
    }
    System.gc
  }
*/

  /*test("Deserialisation") {
  val n1: Node[Int, Int] = deserializeTree(new File(serializePath + "SMT_2_2_1.0.tmp")).get.asInstanceOf[Node[Int, Int]]
  //val n1: Node[Int, Int] = deserializeTree(new File(serializePath + "SMT_4_2_1.0.tmp")).get.asInstanceOf[Node[Int, Int]]
  println("n1 deserialised:\n" + n1)
  println("n1 children: " + n1.getChildren.size)
}*/
  test("SparseMarkovTree predict explores whole structure") {
    val maxDepth = 3
    val maxPhi = 2
    val maxSeqCount = 1
    val smoothing = 1.0
    val prior = 1.0
    val condition1 = Vector(1, 2, 3)
    val condition2 = Vector(4, 5, 6)
    val condition3 = Vector(7, 8, 9)
    val event1 = 666
    val event2 = 777
    val event3 = 888

    val n1 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(condition1, event1)
    var r1 = n1.predict(condition1, event1)
    var r2 = n1.predict(condition1, event2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)

    n1.learn(condition1, event2)
    r1 = n1.predict(condition1, event1)
    r2 = n1.predict(condition1, event2)
    var r3 = n1.predict(condition1, event3)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)
    assert(r1._1 == 2.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(r3._1 == 1.0 && r3._2 == 2.0)

    n1.learn(condition2, event1)
    r1 = n1.predict(condition1, event1)
    r2 = n1.predict(condition1, event2)
    r3 = n1.predict(condition1, event3)
    var r4 = n1.predict(condition2, event1)
    var r5 = n1.predict(condition2, event2)
    var r6 = n1.predict(condition2, event3)

    assert(n1.getChildren(0)(0).asInstanceOf[Node[Int, Int]].getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1.drop(1)).get.getWeight == 1.0 / 3)
    assert(r1._1 == 2.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(r3._1 == 1.0 && r3._2 == 2.0)
    assert(r4._1 == 4.0 && r4._2 == 2.0)
    assert(r5._1 == 2.0 && r5._2 == 2.0)
    assert(r6._1 == 2.0 && r6._2 == 2.0)

    n1.learn(condition3, event2)
    var r7 = n1.predict(condition3, event2)
    assert(r7._1 == 4.0 && r7._2 == 2.0)

    val condition4 = Vector(1, 3, 4)
    n1.learn(condition4, event1)
    var r8 = n1.predict(condition4, event1)
    assert(r8._1 == 4.0 && r8._2 == 2.0)
  }


  case class Distribution(nBins: Int, data: List[Double]) {
    require(data.length > nBins)

    val Epsilon = 0.000001
    val (max, min) = (data.max, data.min)
    val binWidth = (max - min) / nBins + Epsilon
    val bounds = (1 to nBins).map { x => min + binWidth * x }.toList

    def histo(bounds: List[Double], data: List[Double]): List[List[Double]] =
      bounds match {
        case h :: Nil => List(data)
        case h :: t => val (l, r) = data.partition(_ < h); l :: histo(t, r)
      }

    val histogram = histo(bounds, data)
  }

  test("CREATE REPORTS") {
    val maxDepth = 12
    val maxPhi = 3
    val maxSeqCount = 50
    val smoothing = 1.0
    val prior = 1.0

    var extensions = Array("GHC")
    var trainingDir = ""
    var validationDir = ""
    var attackDir = ""
    var serialiseDir = ""
    var valPredictions = ""
    var attackPredictions = ""

    if (isHome) {
      trainingDir = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Main\\Full_Process_Traces_Int\\Full_Trace_Training_Data\\"
      validationDir = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Main\\Full_Process_Traces_Int\\Full_Trace_Validation_Data\\"
      attackDir = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Main\\Full_Process_Traces_Int\\Full_Trace_Attack_Data\\"
      serialiseDir = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Reports\\serialiseDir\\"
      valPredictions = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Reports\\valPredictions\\"
      attackPredictions = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Reports\\attackPredictions\\"
    } else {
      trainingDir = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Main\\Full_Process_Traces\\Full_Trace_Training_Data\\"
      validationDir = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Main\\Full_Process_Traces\\Full_Trace_Validation_Data\\"
      attackDir = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Main\\Full_Process_Traces\\Full_Trace_Attack_Data\\"
      serialiseDir = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Serialised\\trainValClass\\models\\"
      valPredictions = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Serialised\\trainValClass\\valPredictions\\"
      attackPredictions = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Serialised\\trainValClass\\attackPredictions\\"

    }
    val trainingFiles = fileStreamNoDirs(new File(trainingDir), extensions)
    val validationFiles = fileStreamNoDirs(new File(validationDir), extensions)
    val attackFiles = fileStreamNoDirs(new File(attackDir), extensions)

    //var allVal = Vector()

    var counter = 0
    val n1 = new Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    /*val modelFile = new File(serialiseDir + "SMT_10_3_1.0.tmp")
    val n1: Node[Int, Int] = deserialise(modelFile).get.asInstanceOf[Node[Int, Int]]*/

    /*try {
      val modelFile = new File(serialiseDir + "SMT_10_3_1.0.tmp")
      println("modelFile exists and is file: " + (modelFile.exists && modelFile.isFile))
      val n1: Node[Int, Int] = deserialise(modelFile).get.asInstanceOf[Node[Int, Int]]
      println("n1 deserialised. Children size: " +  n1.getChildren.size)
    }catch{
      case e: Throwable => println("deserialisation error: " + e.getMessage)
    }*/

    // home
    //val n1: Node[Int, Int] = deserializeTree(new File(serialiseDir + "SMT_9_3_2.0.tmp")).get.asInstanceOf[Node[Int, Int]]
    // val n1: Node[Int, Int] = deserialise(new File(serialiseDir + "SMT_5_0_1.0.tmp")).get.asInstanceOf[Node[Int, Int]]

    //Train and save model
    try {
      for (f <- trainingFiles) {
        counter += 1
        println("Training. Processing file " + counter + " - filename: " + f.getName)
        val source = scala.io.Source.fromFile(f)
        val lines = try source.getLines mkString "\n" finally source.close()
        val wholeTrace: Vector[Int] = lines.split("\\s+").map(_.trim.toInt).toVector
        var trainingData_whole: Vector[(Vector[Int], Int)] = Vector[(Vector[Int], Int)]()
        for (t <- wholeTrace.sliding(maxDepth, 1)) {
          if (t.size == maxDepth)
            trainingData_whole = trainingData_whole :+ (t.take(maxDepth - 1), t.takeRight(1)(0))
        }

        for (t <- trainingData_whole) {
          n1.learn(t._1, t._2)
        }
      }
      serialise(n1.asInstanceOf[SparseMarkovTree[Int, Int]], new File(serialiseDir + "SMT_" + maxDepth + "_" + maxPhi + "_" + smoothing + ".tmp"))
    } catch {
      case _: Exception => println("Exception. maxDepth: " + maxDepth + " - maxPhi: " + maxPhi + " - smoothing: " + smoothing)
    }

    //Validate files
    println("Training finished. n1 root children size: " + n1.getChildren.size)
    println("\n----\nValidating files")
    var allStatsPath = ""
    if (isHome) {
      allStatsPath = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Reports\\allStats\\allStats"
    } else {
      allStatsPath = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\REPORTS\\allStats\\allStats"
    }
    generateReport(validationFiles, false, n1, valPredictions, allStatsPath)
    //Predictions for Attack traces
    generateReport(attackFiles, true, n1, attackPredictions, allStatsPath)
  }

  /*  test("CREATE REPORTS = EACH ATTACK TYPE") {

    var extensions = Array("GHC")
    var validationDir = ""
    var attackDir = ""
    var valPredictions = ""
    var attackPredictions = ""
    var serialiseDir = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Serialised\\trainValClass\\models\\"
    val modelFile = new File(serialiseDir + "SMT_10_3_1.0.tmp")
    val n1: Node[Int, Int] = deserialise(modelFile).get.asInstanceOf[Node[Int, Int]]

    for(i <- 1 to 11){
      validationDir = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Main" + i + "\\Full_Process_Traces\\Full_Trace_Validation_Data\\"
      attackDir = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Main" + i + "\\Full_Process_Traces\\Full_Trace_Attack_Data\\"

      val validationFiles = fileStreamNoDirs(new File(validationDir), extensions)
      val attackFiles = fileStreamNoDirs(new File(attackDir), extensions)

      var allStatsPath = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Main" + i + "\\"
      generateReport(validationFiles,false,n1,"", allStatsPath)
      //Predictions for Attack traces
      generateReport(attackFiles,true,n1,"", allStatsPath)
    }
  }*/


  private def generateReport(srcFiles: Stream[File], isAttack: Boolean, n1: Node[Int, Int], predictionsPath: String, allStatsPath: String) {
    try {
      var allStatsMap: Map[Double, Double] = Map()
      val allStatsBuilder = StringBuilder.newBuilder
      //(qLt06, qLt08, qLt10, pLQ06, pLQ08, pLQ10, normalisedSum)
      var allStatsVector: Vector[(Double, Double, Double, Double, Double, Double, Double)] = Vector()
      var counter = 0
      var subTraceCounter = 0
      var modeStr = ""
      if (isAttack) {
        modeStr = "Attack"
      } else {
        modeStr = "Validation"
      }

      for (f <- srcFiles) {
        //   val builder = StringBuilder.newBuilder
        //   var predictionStr = modeStr + " - File name: " + f.getName
        //   builder.append(predictionStr)

        counter += 1
        println(modeStr + ". Processing file " + counter + " - filename: " + f.getName)

        val source = scala.io.Source.fromFile(f)
        val lines = try source.getLines mkString "\n" finally source.close()
        val wholeTrace: Vector[Int] = lines.split("\\s+").map(_.trim.toInt).toVector
        var trainingData_whole: Vector[(Vector[Int], Int)] = Vector[(Vector[Int], Int)]()
        for (t <- wholeTrace.sliding(n1.maxDepth, 1)) {
          if (t.size == n1.maxDepth)
            trainingData_whole = trainingData_whole :+ (t.take(n1.maxDepth - 1), t.takeRight(1)(0))
        }

        var quotVector: Vector[Double] = Vector()
        for (t <- trainingData_whole) {
          val pred = n1.predict(t._1, t._2)
          var quotient = 0.0
          if (pred._2 != 0.0) {
            quotient = pred._1 / pred._2
          }
          quotVector = quotVector :+ quotient
          //builder.append("\n----------\nsubsegment: " + t.toString + " - prediction: " + pred.toString + " ---> P = " + quotient.toString)
        }
        println("File: " + f.getName + " - number of predictions (length): " + quotVector.length + " -- size: " + quotVector.size)

        if (quotVector.nonEmpty) {
          subTraceCounter += quotVector.length
          /*val quotMax = quotVector max
          val quotMin = quotVector min*/

          val lowValCount1p0 = quotVector.count(_ < 1.0)
          val lowValCount0p8 = quotVector.count(_ < 0.8)
          val lowValCount0p6 = quotVector.count(_ < 0.6)
          val lowValPercentage1p0 = (lowValCount1p0 / quotVector.size.toDouble) * 100.00
          val lowValPercentage0p8 = (lowValCount0p8 / quotVector.size.toDouble) * 100.00
          val lowValPercentage0p6 = (lowValCount0p6 / quotVector.size.toDouble) * 100.00

          val quotientAvg = quotVector.foldLeft(0.0)(_ + _) / quotVector.foldLeft(0.0)((r, c) => r + 1)

          var statsMap: Map[Double, Double] = Map()
          for (x <- 0.0 to 1.5 by 0.1) {
            //val c = quotVector.count(_<x).toDouble
            statsMap = statsMap + (x -> quotVector.count(_ < x).toDouble)
          }

          allStatsMap = merged(allStatsMap, statsMap)


          /* builder.append("\n----------\nNr of subtraces: " + quotVector.size + " - Max quotient: " + quotMax + " - Min quotient: " + quotMin + " - Quotient average: " + quotientAvg)
          builder.append("\nNr of quotients less than 0.6: " + lowValCount0p6 + " - percentage of low quotients: " + lowValPercentage0p6)
          builder.append("\nNr of quotients less than 0.8: " + lowValCount0p8 + " - percentage of low quotients: " + lowValPercentage0p8)
          builder.append("\nNr of quotients less than 1.0: " + lowValCount1p0 + " - percentage of low quotients: " + lowValPercentage1p0)*/


          val nBins = 50
          if (quotVector.length > nBins) {
            val h = Distribution(nBins, quotVector.toList).histogram
            // builder.append("\n----------\nHistogram:\n" + h)
            val tabulated = h.map {
              _.size
            }
            //   builder.append("\n----------\nTabulated:\n" + tabulated)

            val sumOfBottom10Bins = tabulated.take(10).toVector.sum
            val normalisedSum = sumOfBottom10Bins.toDouble / quotVector.size
            //     builder.append("\n----\nLength normalised sum of bottom 10 bins: " + normalisedSum)
            allStatsVector = allStatsVector :+ (lowValCount0p6.toDouble, lowValPercentage0p6, lowValCount0p8.toDouble, lowValPercentage0p8, lowValCount1p0.toDouble, lowValPercentage1p0, normalisedSum)
          } else {
            allStatsVector = allStatsVector :+ (lowValCount0p6.toDouble, lowValPercentage0p6, lowValCount0p8.toDouble, lowValPercentage0p8, lowValCount1p0.toDouble, lowValPercentage1p0, 0.0)
            //     builder.append("data length (" + quotVector.length + ") is not larger than nBins (" + nBins + ")")
          }
        }
        /*  val file = new File(predictionsPath + FilenameUtils.getBaseName(f.getCanonicalPath) + ".VAL")
        val bw = new BufferedWriter(new FileWriter(file))
        bw.write(builder.toString)
        bw.close()*/
      }
      val statsSum = allStatsVector.foldLeft(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) { case ((acc1, acc2, acc3, acc4, acc5, acc6, acc7), (a, b, c, d, e, f, g)) => (acc1 + a, acc2 + b, acc3 + c, acc4 + d, acc5 + e, acc6 + f, acc7 + g) }
      val statsAvg = (statsSum._1 * 100.0 / subTraceCounter, statsSum._2 * 100.0 / subTraceCounter, statsSum._3 * 100.0 / subTraceCounter, statsSum._4 * 100.0 / subTraceCounter, statsSum._5 * 100.0 / subTraceCounter, statsSum._6 * 100.0 / subTraceCounter, statsSum._7 * 100.0 / subTraceCounter)

      allStatsBuilder.append("\n-----\nStats (Sum):\n" + statsSum.toString)
      allStatsBuilder.append("\n-----\nStats (Avg):\n" + statsAvg.toString)

      allStatsBuilder.append("\n\n------------\nSorted Sum:\n\n")
      val sorted = ListMap(allStatsMap.toSeq.sortBy(_._1): _*)
      for (s <- sorted) {
        allStatsBuilder.append("\n" + s)
      }

      allStatsBuilder.append("\n\n------------\nSorted Avg:\n\n")
      val sortedAvg = sorted.map(x => (x._1, (x._2 * 100.0) / subTraceCounter))
      for (s <- sortedAvg) {
        allStatsBuilder.append("\n" + s)
      }

      allStatsBuilder.append("\n\nSubtrace count (all files): " + subTraceCounter)


      /* allStatsBuilder.append("\n-----\nStats - Map (Avg):\n" + sortedAvg.toString)
      allStatsBuilder.append("\n-----\nStats - Map (Sum):\n" + sorted.toString)*/


      val file2 = new File(allStatsPath + modeStr + ".txt")
      val bw2 = new BufferedWriter(new FileWriter(file2))
      bw2.write(allStatsBuilder.toString)
      bw2.close()
    } catch {
      case e: Exception => println("Exception: " + e.getMessage); println(e.printStackTrace())
    }
  }


  def merged(map1: Map[Double, Double], map2: Map[Double, Double]) = (map1 /: map2) { case (map, (k, v)) =>
    map + (k -> (v + map.getOrElse(k, 0.0)))
  }

  test("Learn to learn"){
    var allStatsPath = ""
    if (isHome) {
      allStatsPath = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Reports\\allStats\\allStats"
    } else {
      allStatsPath = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\REPORTS\\allStats\\allStats"
    }

    var goodSettings: Array[String] = Array()

    for(i <- 6 to 10; j <- 0 to 5; k <- 0.01 to 3.1 by 0.10; l <- 0.1 to 2.0 by 0.1; m <- 0.1 to 1.0 by 0.1; n <- 20.0 to 35.0 by 1.0){
      if(createReports(i,j,k,l,m,n)){
        val goodSettStr = "maxDepth: " + i + " - maxPhi: " + j + " - smoothing: " + k + " - prior: " + l + " - threshold: " + m + " - tolerance: " + n
        goodSettings = goodSettings :+ goodSettStr
      }
    }

    val sb4 = new StringBuilder
    sb4.append(goodSettings.toString)
    val file4 = new File(allStatsPath + "GOODSETTINGS.txt")
    val bw4 = new BufferedWriter(new FileWriter(file4))
    bw4.write(sb4.toString)
    bw4.close()
  }

  def createReports(_maxDepth: Int, _maxPhi: Int, _smoothing: Double, _prior: Double, _threshold: Double, _tolerance: Double): Boolean = {
    val maxDepth = _maxDepth
    val maxPhi = _maxPhi
    val maxSeqCount = 50
    val smoothing = _smoothing
    val prior = _prior
    val threshold = _threshold
    val tolerance = _tolerance

    var extensions = Array("GHC")
    var trainingDir = ""
    var validationDir = ""
    var attackDir = ""
    var serialiseDir = ""
    var valPredictions = ""
    var attackPredictions = ""

    if (isHome) {
      trainingDir = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Main\\Full_Process_Traces_Int\\Full_Trace_Training_Data\\"
      validationDir = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Main\\Full_Process_Traces_Int\\Full_Trace_Validation_Data\\"
      attackDir = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Main\\Full_Process_Traces_Int\\Full_Trace_Attack_Data\\"
      serialiseDir = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Reports\\serialiseDir\\"
      valPredictions = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Reports\\valPredictions\\"
      attackPredictions = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Reports\\attackPredictions\\"
    } else {
      trainingDir = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Main\\Full_Process_Traces\\Full_Trace_Training_Data\\"
      validationDir = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Main\\Full_Process_Traces\\Full_Trace_Validation_Data\\"
      attackDir = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Main\\Full_Process_Traces\\Full_Trace_Attack_Data\\"
      serialiseDir = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Serialised\\trainValClass\\models\\"
      valPredictions = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Serialised\\trainValClass\\valPredictions\\"
      attackPredictions = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\Serialised\\trainValClass\\attackPredictions\\"

    }
    val trainingFiles = fileStreamNoDirs(new File(trainingDir), extensions)
    val validationFiles = fileStreamNoDirs(new File(validationDir), extensions)
    val attackFiles = fileStreamNoDirs(new File(attackDir), extensions)

    var counter = 0
    /*val modelFile = new File(serialiseDir + "SMT_10_3_1.0.tmp")
    val n1: Node[Int, Int] = deserialise(modelFile).get.asInstanceOf[Node[Int, Int]]*/
    val n1 = new Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)


    try {
      for (f <- trainingFiles) {
        counter += 1
        println("Training. Processing file " + counter + " - filename: " + f.getName)
        val source = scala.io.Source.fromFile(f)
        val lines = try source.getLines mkString "\n" finally source.close()
        val wholeTrace: Vector[Int] = lines.split("\\s+").map(_.trim.toInt).toVector
        var trainingData_whole: Vector[(Vector[Int], Int)] = Vector[(Vector[Int], Int)]()
        for (t <- wholeTrace.sliding(maxDepth, 1)) {
          if (t.size == maxDepth)
            trainingData_whole = trainingData_whole :+ (t.take(maxDepth - 1), t.takeRight(1)(0))
        }

        for (t <- trainingData_whole) {
          n1.learn(t._1, t._2)
        }
      }
      serialise(n1.asInstanceOf[SparseMarkovTree[Int, Int]], new File(serialiseDir + "SMT_" + maxDepth + "_" + maxPhi + "_" + smoothing + ".tmp"))
    } catch {
      case _: Exception => println("Exception. maxDepth: " + maxDepth + " - maxPhi: " + maxPhi + " - smoothing: " + smoothing)
    }

    //Validate files
    println("Training finished. n1 root children size: " + n1.getChildren.size)
    println("\n----\nValidating files")
    var allStatsPath = ""
    if (isHome) {
      allStatsPath = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Reports\\allStats\\allStats"
    } else {
      allStatsPath = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\REPORTS\\allStats\\allStats"
    }


    val resVal1 = classifyFiles(validationFiles, false, n1, valPredictions, allStatsPath, threshold, tolerance)
    val sb1 = resVal1._1
    val isGoodValPrediction = resVal1._2
    sb1.append("\n\n------\nValidation:\nmaxDepth: " + maxDepth + "\nmaxPhi: " + maxPhi + "\nsmoothing: " + smoothing + "\nprior: " + prior + "\nthreshold: " + threshold + "\ntolerance: " + tolerance)
    //_maxDepth: Int, _maxPhi: Int, _smoothing: Double, _prior: Double, _threshold: Double, _tolerance: Double
    val file2 = new File(allStatsPath + "Validation_" + maxDepth + "_" + maxPhi + "_" + smoothing + "_" + prior + "_" + threshold + "_" + tolerance + ".txt")
    val bw2 = new BufferedWriter(new FileWriter(file2))
    bw2.write(sb1.toString)
    bw2.close()

    val resVal2 = classifyFiles(attackFiles, true, n1, attackPredictions, allStatsPath, threshold, tolerance)
    val sb2 = resVal2._1
    val isGoodAttackPrediction = resVal2._2
    sb2.append("\n\n------\nAttack:\nmaxDepth: " + maxDepth + "\nmaxPhi: " + maxPhi + "\nsmoothing: " + smoothing + "\nprior: " + prior + "\nthreshold: " + threshold + "\ntolerance: " + tolerance)
    val file3 = new File(allStatsPath + "Attack_" + maxDepth + "_" + maxPhi + "_" + smoothing + "_" + prior + "_" + threshold + "_" + tolerance + ".txt")
    val bw3 = new BufferedWriter(new FileWriter(file3))
    bw3.write(sb2.toString)
    bw3.close()

    (isGoodValPrediction && isGoodAttackPrediction)
  }

  private def classifyFiles(srcFiles: Stream[File], isAttack: Boolean, n1: Node[Int, Int], predictionsPath: String, allStatsPath: String, threshold: Double, tolerance: Double): (StringBuilder,Boolean) = {
    try {
      var traceCount = 0
      var anomalousTraceCount = 0
      var normalTraceCount = 0
      var modeStr = ""
      val sb = StringBuilder.newBuilder
      if (isAttack) {
        sb.append("---- Attack traces ----\n"); modeStr = "Attack"
      } else {
        sb.append("---- Normal traces ----\n"); modeStr = "Validation"
      }

      for (f <- srcFiles) {


        println(modeStr + ". Processing file " + traceCount + " - filename: " + f.getName)

        val source = scala.io.Source.fromFile(f)
        val lines = try source.getLines mkString "\n" finally source.close()
        val wholeTrace: Vector[Int] = lines.split("\\s+").map(_.trim.toInt).toVector
        var trainingData_whole: Vector[(Vector[Int], Int)] = Vector[(Vector[Int], Int)]()
        for (t <- wholeTrace.sliding(n1.maxDepth, 1)) {
          if (t.size == n1.maxDepth)
            trainingData_whole = trainingData_whole :+ (t.take(n1.maxDepth - 1), t.takeRight(1)(0))
        }

        var quotVector: Vector[Double] = Vector()
        for (t <- trainingData_whole) {
          val pred = n1.predict(t._1, t._2)
          var quotient = 0.0
          if (pred._2 != 0.0) {
            quotient = pred._1 / pred._2
          }
          quotVector = quotVector :+ quotient
        }


        if (quotVector.nonEmpty) {
          traceCount += 1
          val anomalyCount = quotVector.count(_ < threshold)
          val anomalyPercentage = (anomalyCount / quotVector.size.toDouble) * 100.00
          var isAnomaly = if (anomalyPercentage < tolerance) false else true
          var result = if (isAnomaly)  "ANOMALY" else "NORMAL"

          sb.append("\nfile: " + f.getName + " - " + anomalyCount + " anomalous subtraces out of " + quotVector.size + ". - " + anomalyPercentage + "% anomalous. Classification: " + result)
          if (isAnomaly) anomalousTraceCount += 1 else normalTraceCount += 1
        }
      }

      var isGoodPrediction: Boolean = false
      if(isAttack && ((anomalousTraceCount.toDouble/traceCount.toDouble)*100 > 75)) isGoodPrediction = true
      else if(!isAttack && ((normalTraceCount.toDouble/traceCount.toDouble)*100 < 25)) isGoodPrediction = true

      (sb.append("\n--------\nFile count: " + traceCount + " - Normal: " + normalTraceCount + " - Anomalous: " + anomalousTraceCount), isGoodPrediction)
/*      val file2 = new File(allStatsPath + modeStr + ".txt")
      val bw2 = new BufferedWriter(new FileWriter(file2))
      bw2.write(sb.toString)
      bw2.close()*/
    } catch {
      case e: Exception => println("Exception: " + e.getMessage); println(e.printStackTrace()); (new StringBuilder("EXCEPTION THROWN IN CLASSIFY FILES"), false)
    }
  }
}