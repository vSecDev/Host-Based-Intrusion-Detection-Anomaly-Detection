package Data

import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.{Files, Paths}
import scala.collection.mutable
import org.apache.commons.io.FilenameUtils


class FileProcessor(source: File, target: File, delimiter: String) extends DataProcessor {

  override def configure(): Unit = {}

/*  override def preprocess(target: File): Unit = {

    var sysCallSet = mutable.Set[String]()
    var sysCallMap = Map[String, Int]()

    println("sysCallSet.size BEFORE: " + sysCallSet.size)
    val parentDir = new File(source)

    fileStreamNoDirs(parentDir).foreach(f => sysCallSet = fileToSet(f, sysCallSet))
    sysCallMap = sysCallSet.zipWithIndex.map { case (v, i) => (v, i + 1) }.toMap

    fileStreamNoDirs(parentDir).foreach(f => toIntTrace(f, target, sysCallMap))
  }*/

  //TODO - TRY/CATCH BLOCKS / EXCEPTION HANDLING TO ADD FOR IO OPS
  override def preprocess(): Unit = {

    var sysCallSet = mutable.Set[String]()
    var sysCallMap = Map[String, Int]()

    println("sysCallSet.size BEFORE: " + sysCallSet.size)
    val parentDir = new File(source)

    fileStreamNoDirs(parentDir).foreach(f => sysCallSet = fileToSet(f, sysCallSet))
    println("sysCallSet.size AFTER: " + sysCallSet.size)

    sysCallMap = sysCallSet.zipWithIndex.map { case (v, i) => (v, i + 1) }.toMap
    println("sysCallMap.size AFTER (should be same as sysCallSet.size): " + sysCallMap.size)

    fileStream(parentDir).foreach(f => {
      if(f.isDirectory){
        println("new dir will be created here: " + (target + f.getCanonicalPath.replaceAll(source.getCanonicalPath, "") + "_INT"))
       // val path = Paths.get(target + f.getName + "_INT")
       // Files.createFile(path)
      }else {
       // toIntTrace(f, target, sysCallMap)
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
    val vec: Vector[String] = lines.split(delimiter).map(_.trim).toVector
    set ++= vec
  }

  private def toIntTrace(f: File, target: File, map: Map[String, Int]): File = {
    val source = scala.io.Source.fromFile(f)
    val lines = try source.getLines mkString "\n" finally source.close()
    val result = map.foldLeft(lines)((a, b) => a.replaceAllLiterally(b._1, b._2.toString))
    val newFile = new File(target, FilenameUtils.removeExtension(f.getName) + "_INT.IDS")
    val bw = new BufferedWriter(new FileWriter(newFile))
    bw.write(result)
    bw.close()
  }
}