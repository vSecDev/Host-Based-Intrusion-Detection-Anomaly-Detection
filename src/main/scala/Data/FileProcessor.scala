package Data

import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.util.{Try, Success, Failure}
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.FileUtils
import java.io.IOException


class FileProcessor(source: File, target: File, delimiters: Array[String] = Array("\\s"), extensions: Array[String] = Array[String]()) extends DataProcessor {

  require(source.exists && source.isDirectory, "Source directory does not exist or is not a directory!")
  require(target.exists && target.isDirectory, "Target directory does not exist or is not a directory!")

  override def configure(): Unit = {}

  //TODO - FIX EXCEPTION HANDLING - Lock resources!
  @throws(classOf[DataException])
  override def preprocess(): Unit = {
    try {
      //println("target is empty: " + target.)
      clearDir(target)

      var sysCallSet = mutable.Set[String]()
      var sysCallMap = Map[String, Int]()

      fileStreamNoDirs(source).foreach(f => sysCallSet = fileToSet(f, sysCallSet))
      sysCallMap = sysCallSet.zipWithIndex.map { case (v, i) => (v, i + 1) }.toMap
      println("sysCallMap: " + sysCallMap)
      fileStream(source).foreach(f => {
        if (f.isDirectory) {
          val path = target.getCanonicalPath + f.getCanonicalPath.replace(source.getCanonicalPath, "")
          Files.createDirectory(Paths.get(path))
        } else {
          if (extensions.exists(f.getName.endsWith(_))) {

            println("processing file: " + f.getName)
            val path = target.getCanonicalPath + FilenameUtils.removeExtension(f.getCanonicalPath.replace(source.getCanonicalPath, "")) + "_INT.IDS"
            val intTrace = toIntTrace(f, sysCallMap)
            val newFile = new File(path)
            val bw = new BufferedWriter(new FileWriter(newFile))
            bw.write(intTrace)
            bw.close()
          }
        }
      })
    } catch {
      case de: DataException => throw de
      case ex: Throwable => throw new DataException("An error occurred during data processing.", ex)
    }
  }


  def clearDir(file: File): Unit = {
    try {
      target.listFiles.foreach(f => FileUtils.deleteDirectory(f))
    } catch {
      case se: SecurityException => throw new DataException("SecurityException thrown during clearing target directory.", se)
      case ioe: IOException => throw new DataException("IOException thrown during clearing target directory.", ioe)
    }
  }


  def fileStream(dir: File): Stream[File] =
    try {
      Option(dir.listFiles).map(_.toList.sortBy(_.getName).toStream).map {
        files => files.append(files.filter(_.isDirectory).flatMap(fileStream))
      } getOrElse {
        println("exception: dir cannot be listed: " + dir.getPath)
        Stream.empty
      }
    } catch {
      case se: SecurityException => throw new DataException("SecurityException thrown during processing source files.", se)
    }

  def fileStreamNoDirs(dir: File): Stream[File] =
    try {
      Option(dir.listFiles).map(_.toList.sortBy(_.getName).toStream.partition(_.isDirectory))
        .map { case (dirs, files) =>
          files.filter(f => extensions.exists(f.getName.endsWith(_))).append(dirs.flatMap(fileStreamNoDirs))
        } getOrElse {
        println("exception: dir cannot be listed: " + dir.getPath)
        Stream.empty
      }
    } catch {
      case se: SecurityException => throw new DataException("SecurityException thrown during processing source files.", se)
    }

  def fileToSet(f: File, set: mutable.Set[String]): mutable.Set[String] = {
    try {
      val source = scala.io.Source.fromFile(f)
      val lines = try source.getLines mkString "\n" finally source.close()
      println("delimiters.mkstring:" + delimiters.mkString("|"))
      val vec: Vector[String] = lines.split(delimiters.mkString("|")).map(_.trim).toVector
      set ++= vec
    } catch {
      case ioe: IOException => throw new DataException("IOException thrown during data conversion to set.", ioe)
    }
  }

  private def toIntTrace(f: File, map: Map[String, Int]): String = {
    try {
      val source = scala.io.Source.fromFile(f)
      val lines = try source.getLines mkString "\n" finally source.close()
      val result = map.foldLeft(lines)((a, b) => a.replaceAllLiterally(b._1, b._2.toString))
      result
    } catch {
      case ioe: IOException => throw new DataException("IOException thrown during conversion of source data file.", ioe)
    }
  }
}