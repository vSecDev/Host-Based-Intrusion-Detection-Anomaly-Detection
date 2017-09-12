package Data

import java.io._
import Data.File.FileProcessor
import DecisionEngine.SMT.{Node, SequenceList}
import org.apache.commons.io.{FileUtils, FilenameUtils}
import org.scalatest.FunSuite
import scala.collection.mutable
import scala.io.Source

class FileProcessorTest extends FunSuite {
  val isHome = false
  var testSource = ""
  var testTarget = ""
  var mapTestSource = ""
  var mapTestTarget = ""

  if (isHome) {
    mapTestSource = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\MapTest\\source"
    mapTestTarget = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\MapTest\\target"
    testSource = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\"
    testTarget = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\main\\target"
  }
  else {
    mapTestSource = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\MapTest\\source"
    mapTestTarget = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\MapTest\\target"
    testSource = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\"
    testTarget = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\main\\target"
  }

  def recursiveListDirs(f: File): Array[File] = {
    val these = f.listFiles.filter(_.isDirectory)
    these ++ these.flatMap(recursiveListDirs)
  }

  def recursiveListFiles(f: File): Array[File] = {
    val all = f.listFiles
    val files = f.listFiles.filter(_.isFile)
    files ++ all.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

  test("FileProcessor - preprocess returns None if source doesn't exist anymore") {
    val d = Array("\\s")
    val e = Array("GHC")
    val t = new File(testTarget)
    val tempF = new File(testSource + "main\\source" + "\\tempSource")

    tempF.mkdir
    if (tempF.exists) {
      val fp = new FileProcessor
      tempF.delete()
      assert(fp.preprocess(tempF,t,d,e) == None)
    }
  }
  test("FileProcessor - preprocess returns None if target doesn't exist anymore") {
    val d = Array("\\s")
    val e = Array("GHC")
    val s = new File(testSource + "main\\source")
    val tempF = new File(testTarget + "\\tempTarget")
    tempF.mkdir
    if (tempF.exists) {
      val fp = new FileProcessor
      tempF.delete()
      assert(fp.preprocess(s,tempF,d,e) == None)
    }
  }
  test("FileProcessor - preprocess creates target dirs") {
    val s = new File(testSource + "main\\source")
    val t = new File(testTarget)
    val d = Array("\\s")
    val e = Array("GHC")
    val fp = new FileProcessor
    fp.preprocess(s, t, d, e)
    val sourceDirs = recursiveListDirs(s).map(f => f.getName)
    val dirs = recursiveListDirs(t).map(f => f.getName)

    for (n <- sourceDirs) {
      assert(dirs.contains(n))
    }
  }
  test("FileProcessor - preprocess works with multiple delimiters.") {
     val s = new File(testSource + "\\multDel\\source")
     val t = new File(testSource + "\\multDel\\target")
     val d = Array("\\s", "\\|", "\\;", "\\,", "\\_")
     val e = Array("GHC")
     val fp = new FileProcessor
     var sysCallMap = mutable.Map[String, Int]()
     try {
       sysCallMap = fp.preprocess(s, t, d, e).get
     } catch {
       case e: Throwable => println("MESSAGE: " + e.getCause.getMessage)
     }

     var sfStr = Array[String]()
     val s1 = Source.fromFile(recursiveListFiles(s).filter(f => f.getName == "multipleDelimiters.GHC")(0))
     try
       sfStr = s1.mkString.split(d.mkString("|"))
     finally s1.close()

    var tfStr = Array[String]()
     val s2 = Source.fromFile(recursiveListFiles(t).filter(f => f.getName == "multipleDelimiters_INT.IDS")(0))
     try
       tfStr = s2.mkString.split(d.mkString("|"))
     finally s2.close()

     for (i <- sfStr.indices) {
       assert(sysCallMap(sfStr(i)) == tfStr(i).toInt)
     }
   }
  test("FileProcessor - preprocess works with multiple extensions.") {
    val s = new File(testSource + "main\\source")
    val t = new File(testTarget)
    val d = Array("\\s", "\\|", "\\;", "\\,", "\\_")
    val e = Array("GHC", "AAA", "EEE")
    val fp = new FileProcessor
    fp.preprocess(s, t, d, e)
    val sourceFiles = recursiveListFiles(s)
    val sourceFilesToCopy = recursiveListFiles(s).filter(f => FilenameUtils.getExtension(f.getName) == "GHC" || FilenameUtils.getExtension(f.getName) == "AAA" || FilenameUtils.getExtension(f.getName) == "EEE")
    val sourceFilesToCopy2 = recursiveListFiles(s).filter(f => e.exists(f.getName.endsWith(_)))
    val targetFiles = recursiveListFiles(t)

    assert(targetFiles.nonEmpty)
    assert(targetFiles.length == sourceFilesToCopy.length)
    assert(targetFiles.length == sourceFilesToCopy2.length)

    for (f <- targetFiles) assert(sourceFiles.exists(f2 => f.getName == FilenameUtils.removeExtension(f2.getName) + "_INT.IDS"))

    assert(sourceFiles.exists(f => FilenameUtils.removeExtension(f.getName) == "file2_DontCopy"))
    assert(!targetFiles.exists(f => FilenameUtils.removeExtension(f.getName) == "file2_DontCopy"))
  }
  test("FileProcessor - preprocess returns map of correct size") {
    val s = new File(mapTestSource)
    val t = new File(mapTestTarget)
    val d = Array("\\s", "\\|", "\\;", "\\,", "\\_")
    val e = Array("GHC", "AAA", "EEE")
    val fp = new FileProcessor
    val sysCallMap = fp.preprocess(s, t, d, e).get
    assert(sysCallMap.size == 5)
  }
  test("FileProcessor - getData returns content of file") {
    val s = new File(testSource)
    val t = new File(testTarget)
    val d = Array("\\s", "\\|", "\\;", "\\,", "\\_")
    val e = Array("GHC")
    val fp = new FileProcessor
    val f = new File(testSource + "main\\source\\2\\getDataTest.GHC")
    val w = fp.getData(f, e)
    assert(w isDefined)
    assert(w.get.retrieve.get._2 == "kernel32.dll+0xc939 ntdll.dll+0x10b63 kernel32.dll+0xb50b")
  }
  test("FileProcessor - getAllData returns content of all files") {
    val s = new File(testSource)
    val t = new File(testTarget)
    val d = Array("\\s", "\\|", "\\;", "\\,", "\\_")
    val e = Array("GHC", "AAA", "EEE")
    val fp = new FileProcessor
    val f = new File(testSource + "\\getAllDataTest")
    val traces = Vector("kernel32.dll+0xc939 ntdll.dll+0x10b63 kernel32.dll+0xb50b", "ntdll.dll+0x16d33 ntdll.dll+0x16f03 ntdll.dll+0x16d33 ntdll.dll+0x2b82e", "330 577 335 779 703 342 1034 641 602 268 913 1149 779 703 342 1034 641 602 268 913 1149 779 703 342 1034 641 602 268 913 1149 779 703 342 1034 641 602 268 913 1149 779 703 342 1034 641 602 268 913 1149 779 703 342 1034 641 602 268 913 1149", "")
    val w = fp.getAllData(f,e)
    assert(w isDefined)
    val vect = w.get
    for (i <- vect.indices) assert(vect(i).retrieve.get._2.equals(traces(i)))
  }
  test("FileProcessor - processNew creates new file and returns updated system call map"){
    val s = new File(testSource + "\\unseenTest\\source")
    val t = new File(testSource + "\\unseenTest\\target")
    val unseen = new File(testSource + "\\unseenTest\\unseen.GHC")
    val d = Array("\\s", ";", "_")
    val e = Array("GHC")
    val fp = new FileProcessor

    val sysCallMap = fp.preprocess(s,t,d,e).get
    assert(sysCallMap.size == 8)
    for(i <- 1 to sysCallMap.size){
      assert(sysCallMap.contains("seen" + i))
    }

    fp.processNew(unseen, t, sysCallMap, d, e).get
    assert(sysCallMap.size == 12)
    for(i <- 1 to 4){
      assert(sysCallMap.contains("unseen" + i))
    }
  }
  test("FileProcessor - saveModel serialisation works"){
    val toClean = new File(testSource + "\\serialisation\\target\\")
    toClean.listFiles.foreach { f => if (f.isDirectory) FileUtils.deleteDirectory(f) else if (f.isFile)   f.delete() }

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

    val s = new File(testSource + "\\serialisation\\source")
    val t = new File(testSource + "\\serialisation\\target\\test.SMT")
    val d = Array("\\s", "\\|", "\\;", "\\,", "\\_")
    val e = Array("GHC", "AAA", "EEE")
    val fp = new FileProcessor

    val n1 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(condition1, event1)

    val dm1 = new DataModel
    dm1.store(n1)
    assert(fp.saveModel(dm1, t))

    val loadedModel = fp.loadModel(new DataModel, t).get.retrieve.get.asInstanceOf[Node[Int, Int]]

    var r1 = loadedModel.predict(condition1, event1)
    var r2 = loadedModel.predict(condition1, event2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(loadedModel.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)

    loadedModel.learn(condition1, event2)
    dm1.store(loadedModel)
    val t2 = new File(testSource + "\\serialisation\\target\\test2.SMT")
    assert(fp.saveModel(dm1, t2))

    val loadedModel2 = fp.loadModel(new DataModel, t2).get.retrieve.get.asInstanceOf[Node[Int, Int]]
    r1 = loadedModel2.predict(condition1, event1)
    r2 = loadedModel2.predict(condition1, event2)
    var r3 = loadedModel2.predict(condition1, event3)
    assert(loadedModel2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)
    assert(r1._1 == 2.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(r3._1 == 1.0 && r3._2 == 2.0)

    loadedModel2.learn(condition2, event1)
    dm1.store(loadedModel2)
    val t3 = new File(testSource + "\\serialisation\\target\\test3.SMT")
    assert(fp.saveModel(dm1, t3))
    val loadedModel3 = fp.loadModel(new DataModel, t3).get.retrieve.get.asInstanceOf[Node[Int, Int]]
    r1 = loadedModel3.predict(condition1, event1)
    r2 = loadedModel3.predict(condition1, event2)
    r3 = loadedModel3.predict(condition1, event3)
    var r4 = loadedModel3.predict(condition2, event1)
    var r5 = loadedModel3.predict(condition2, event2)
    var r6 = loadedModel3.predict(condition2, event3)

    assert(loadedModel3.getChildren(0)(0).asInstanceOf[Node[Int, Int]].getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1.drop(1)).get.getWeight == 1.0 / 3)
    assert(r1._1 == 2.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(r3._1 == 1.0 && r3._2 == 2.0)
    assert(r4._1 == 4.0 && r4._2 == 2.0)
    assert(r5._1 == 2.0 && r5._2 == 2.0)
    assert(r6._1 == 2.0 && r6._2 == 2.0)

    loadedModel3.learn(condition2, event1)
    dm1.store(loadedModel3)
    val t4 = new File(testSource + "\\serialisation\\target\\test4.SMT")
    assert(fp.saveModel(dm1, t4))
    val loadedModel4 = fp.loadModel(new DataModel, t4).get.retrieve.get.asInstanceOf[Node[Int, Int]]

    loadedModel4.learn(condition3, event2)

    dm1.store(loadedModel4)
    val t5 = new File(testSource + "\\serialisation\\target\\test5.SMT")
    assert(fp.saveModel(dm1, t5))
    val loadedModel5 = fp.loadModel(new DataModel, t5).get.retrieve.get.asInstanceOf[Node[Int, Int]]

    var r7 = loadedModel5.predict(condition3, event2)
    assert(r7._1 == 4.0 && r7._2 == 2.0)

    val condition4 = Vector(1, 3, 4)
    loadedModel5.learn(condition4, event1)
    var r8 = loadedModel5.predict(condition4, event1)
    assert(r8._1 == 4.0 && r8._2 == 2.0)
  }
}
