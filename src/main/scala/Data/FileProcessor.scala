package Data

import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.util.{Try, Success, Failure}
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.FileUtils


class FileProcessor(source: File, target: File, delimiters: Array[String] = Array("\\s"), extensions: Array[String] = Array[String]()) extends DataProcessor {

  require(source.exists && source.isDirectory, "Source directory does not exist or is not a directory!")
  require(target.exists && target.isDirectory, "Target directory does not exist or is not a directory!")

  override def configure(): Unit = {}

  //TODO - TRY/CATCH BLOCKS / EXCEPTION HANDLING TO ADD FOR IO OPS
  override def preprocess(): Unit = {

    if(target.listFiles.nonEmpty) {
      target.listFiles.foreach(f => FileUtils.deleteDirectory(f))
    }

    var sysCallSet = mutable.Set[String]()
    var sysCallMap = Map[String, Int]()

    //val parentDir = source

    fileStreamNoDirs(source).foreach(f => sysCallSet = fileToSet(f, sysCallSet))
    println("sysCallSet.size AFTER: " + sysCallSet.size)

    sysCallMap = sysCallSet.zipWithIndex.map { case (v, i) => (v, i + 1) }.toMap
    println("sysCallMap.size AFTER (should be same as sysCallSet.size): " + sysCallMap.size)

    fileStream(source).foreach(f => {
      //val path = target.getCanonicalPath + f.getCanonicalPath.replace(source.getCanonicalPath, "")


      if (f.isDirectory) {
        println("\ntarget.getCanonicalPath: " + target.getCanonicalPath)
        println("source.getCanonicalPath: " + source.getCanonicalPath)
        println("f.getCanonicalPath: " + f.getCanonicalPath)
        println("f.getCanonicalPath.replace(source.getCanonicalPath: " + f.getCanonicalPath.replace(source.getCanonicalPath, ""))

        println("new dir will be created here: " + (target.getCanonicalPath + f.getCanonicalPath.replace(source.getCanonicalPath, "")))

        val path = target.getCanonicalPath + f.getCanonicalPath.replace(source.getCanonicalPath, "")
        println("\npath: " + path)
        Files.createDirectory(Paths.get(path))
        println()
        println()
      } else {
        val path = target.getCanonicalPath + FilenameUtils.removeExtension(f.getCanonicalPath.replace(source.getCanonicalPath, "")) + "_INT.IDS"
        println("\npath: " + path)
        val intTrace = toIntTrace(f, sysCallMap)
        val newFile = new File(path)


        val bw = new BufferedWriter(new FileWriter(newFile))
        bw.write(intTrace)
        bw.close()
      }
    })
  }


  def fileStream(dir: File): Stream[File] =
    Option(dir.listFiles).map(_.toList.sortBy(_.getName).toStream).map {
      files => files.append(files.filter(_.isDirectory).flatMap(fileStream))
    } getOrElse {
      println("exception: dir cannot be listed: " + dir.getPath)
      Stream.empty
    }

  def fileStreamNoDirs(dir: File): Stream[File] =
    Option(dir.listFiles).map(_.toList.sortBy(_.getName).toStream.partition(_.isDirectory))
      .map { case (dirs, files) =>
        files.append(dirs.flatMap(fileStreamNoDirs))
      } getOrElse {
      println("exception: dir cannot be listed: " + dir.getPath)
      Stream.empty
    }

  def fileToSet(f: File, set: mutable.Set[String]): mutable.Set[String] = {
    println("processing file: " + f.getName)
    val source = scala.io.Source.fromFile(f)
    val lines = try source.getLines mkString "\n" finally source.close()
    //val vec: Vector[String] = lines.split("\\s+").map(_.trim).toVector
    val vec: Vector[String] = lines.split(delimiters.mkString("|")).map(_.trim).toVector
    set ++= vec
  }

  private def toIntTrace(f: File, map: Map[String, Int]): String = {
    val source = scala.io.Source.fromFile(f)
    val lines = try source.getLines mkString "\n" finally source.close()
    val result = map.foldLeft(lines)((a, b) => a.replaceAllLiterally(b._1, b._2.toString))
   result
  }
}