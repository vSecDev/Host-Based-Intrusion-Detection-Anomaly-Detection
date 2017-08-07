package Data

import java.io._
import org.apache.commons.io.FilenameUtils
import org.scalatest.FunSuite
import scala.io.Source

class FileProcessorTest extends FunSuite {

  //val testSourceHome = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\source"
  //val testTargetHome = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\target"
  val testSourceWork = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\source"
  val testTargetWork = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\target"

  def recursiveListDirs(f: File): Array[File] = {
    val these = f.listFiles.filter(_.isDirectory)
    these ++ these.flatMap(recursiveListDirs)
  }

  def recursiveListFiles(f: File): Array[File] = {
    val all = f.listFiles
    val files = f.listFiles.filter(_.isFile)
    //val these = f.listFiles
    //these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
    files ++ all.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

  test("FileProcessor - preprocess creates target dirs") {

    /*val s = new File(testSourceHome)
    val t = new File(testTargetHome)*/
    val s = new File(testSourceWork)
    val t = new File(testTargetWork)
    val d = Array("\\s")
    val e = Array("GHC")
    val fp = new FileProcessor(s, t, d, e)
    fp.preprocess()
    val folderNames = Array("1", "2", "3", "4")
    val dirs = recursiveListDirs(t).map(f => f.getName)
    assert(dirs.length == 4)
    for (n <- folderNames) {
      assert(dirs.contains(n))
    }
  }
  test("FileProcessor - preprocess works with multiple delimiters.") {
    /*val s = new File(testSourceHome)
    val t = new File(testTargetHome)*/
    val s = new File(testSourceWork)
    val t = new File(testTargetWork)
    val d = Array("\\s", "\\|", "\\;", "\\,", "\\_")
    val e = Array("GHC")
    val fp = new FileProcessor(s, t, d, e)
    val sysCallMap = fp.preprocess()

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
    /*val s = new File(testSourceHome)
    val t = new File(testTargetHome)*/
    val s = new File(testSourceWork)
    val t = new File(testTargetWork)
    val d = Array("\\s", "\\|", "\\;", "\\,", "\\_")
    val e = Array("GHC", "AAA", "EEE")
    val fp = new FileProcessor(s, t, d, e)
    fp.preprocess()
    val sourceFiles = recursiveListFiles(s)
    val sourceFilesToCopy = recursiveListFiles(s).filter(f => FilenameUtils.getExtension(f.getName) == "GHC" || FilenameUtils.getExtension(f.getName) == "AAA" || FilenameUtils.getExtension(f.getName) == "EEE")
    val sourceFilesToCopy2 = recursiveListFiles(s).filter(f => e.exists(f.getName.endsWith(_)))
    val targetFiles = recursiveListFiles(t)

    assert(targetFiles.nonEmpty)
    assert(targetFiles.length == sourceFilesToCopy.length)
    assert(targetFiles.length == sourceFilesToCopy2.length)

    for (f <- targetFiles) assert(sourceFiles.exists(f2 => f.getName == FilenameUtils.removeExtension(f2.getName) + "_INT.IDS"))

    assert(sourceFiles.filter(f => FilenameUtils.removeExtension(f.getName) == "file2_DontCopy").nonEmpty)
    assert(targetFiles.filter(f => FilenameUtils.removeExtension(f.getName) == "file2_DontCopy").isEmpty)
  }
}
