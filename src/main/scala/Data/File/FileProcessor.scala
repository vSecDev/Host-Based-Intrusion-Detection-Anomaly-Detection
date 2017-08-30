package Data.File

import java.io.{BufferedWriter, File, FileWriter, IOException}
import java.nio.file.{Files, Paths}
import Data._
import org.apache.commons.io.{FileUtils, FilenameUtils}
import scala.collection.mutable

class FileProcessor extends DataProcessor {

  override def configure(): Unit = {}

  //TODO - FIX EXCEPTION HANDLING - Lock resources!

  @throws(classOf[DataException])
  def processNew(f: File, target: File, map: mutable.Map[String, Int], delimiters: Array[String], extensions: Array[String]): Option[mutable.Map[String, Int]] = {
    if (!checkFiles(f) || !checkDirs(target)) { return None }
    val data = getData(f, extensions)
    try {
      data match {
        case None => None
        case Some(strOpt) => strOpt.retrieve match {
          case None => None
          case Some(str) => {
            var sysCallSet = mutable.Set[String]()
            sysCallSet = fileToSet(f, sysCallSet, delimiters)
            sysCallSet.foreach(s => if (!map.keySet.contains(s)) {
              map += (s -> (map.valuesIterator.max + 1))
            })
            val path = target.getCanonicalPath + "\\" + FilenameUtils.getBaseName(f.getName) + "_INT.IDS"
            val intTrace = toIntTrace(f, map, delimiters)
            val newFile = new File(path)
            val bw = new BufferedWriter(new FileWriter(newFile))
            bw.write(intTrace)
            bw.close()
            Some(map)
          }
        }
      }
    } catch {
      case de: DataException => throw de
      case ex: Throwable => throw new DataException("An error occurred during data processing.", ex)
    }
  }

  @throws(classOf[DataException])
  override def preprocess(source: File, target: File, delimiters: Array[String] = Array("\\s"), extensions: Array[String]): Option[mutable.Map[String, Int]] = {
    if(!checkDirs(source, target)) return None
    try {
      clearDir(target)

      var sysCallSet = mutable.Set[String]()
      var sysCallMap = mutable.Map[String, Int]()
      fileStreamNoDirs(source, extensions).foreach(f => sysCallSet = fileToSet(f, sysCallSet, delimiters))
      //sysCallMap = sysCallSet.zipWithIndex.map { case (v, i) => (v, i + 1) }.toMap
      sysCallMap ++= sysCallSet.zipWithIndex.map { case (v, i) => (v, i + 1) }

      fileStream(source).foreach(f => {
        if (f.isDirectory) {
          val path = target.getCanonicalPath + f.getCanonicalPath.replace(source.getCanonicalPath, "")
          Files.createDirectory(Paths.get(path))
        } else {
          if (checkExtension(f, extensions)) {
            val path = target.getCanonicalPath + FilenameUtils.removeExtension(f.getCanonicalPath.replace(source.getCanonicalPath, "")) + "_INT.IDS"
            val intTrace = toIntTrace(f, sysCallMap, delimiters)
            val newFile = new File(path)
            val bw = new BufferedWriter(new FileWriter(newFile))
            bw.write(intTrace)
            bw.close()
          }
        }
      })
      Some(sysCallMap)
    } catch {
      case de: DataException => throw de
      case ex: Throwable => throw new DataException("An error occurred during data processing.", ex)
    }
  }

  override def getData(f: File, extensions: Array[String]): Option[StringDataWrapper] = {
    if (checkFiles(f) && checkExtension(f, extensions)) {
      val src = scala.io.Source.fromFile(f)
      val lines = try src.getLines mkString "\n" finally src.close()
      val wrapper = new StringDataWrapper
      wrapper.store((f.getName, lines))
      Some(wrapper)
    } else {
      None
    }
  }

  override def getAllData(d: File, extensions: Array[String]): Option[Vector[StringDataWrapper]] = {
    if (checkDirs(d)) {
      val fs = fileStreamNoDirs(d, extensions)
      var ws = Vector[StringDataWrapper]()

      fs.foreach(f => {
        val src = scala.io.Source.fromFile(f)
        val lines = try src.getLines mkString "\n" finally src.close()
        val wrapper = new StringDataWrapper
        wrapper.store((f.getName, lines))
        ws = ws :+ wrapper
      })
      Some(ws)
    } else { None }
  }

  @throws(classOf[DataException])
  override def saveModel(dm: DataModel, target: File): Boolean = {
    try {
      dm.serialise(target)
    } catch {
      case de: DataException => throw de
    }
  }

  @throws(classOf[DataException])
  override def loadModel(dm: DataModel, source: File): Option[DataModel] = {
    try {
      dm.deserialise(source)
    } catch {
      case de: DataException => throw de
    }
  }

  private def checkDirs(dirs: File*): Boolean ={
    dirs.foreach(f => if(!f.exists || !f.isDirectory) return false)
    true
  }

  private def checkFiles(files: File*): Boolean = {
    files.foreach(f => if(!f.exists || !f.isFile) return false)
    true
  }

  private def checkExtension(f: File, extensions: Array[String]): Boolean = extensions.contains(FilenameUtils.getExtension(f.getName))

  private def clearDir(dir: File): Unit = {
    try
      dir.listFiles.foreach { f => {
        if (f.isDirectory) FileUtils.deleteDirectory(f) else if (f.isFile) f.delete()
      }
      }
    catch {
      case se: SecurityException => throw new DataException("SecurityException thrown during clearing target directory.", se)
      case ioe: IOException => throw new DataException("IOException thrown during clearing target directory.", ioe)
    }
  }

  private def fileStream(dir: File): Stream[File] =
    try {
      Option(dir.listFiles).map(_.toList.sortBy(_.getName).toStream).map {
        files => files.append(files.filter(_.isDirectory).flatMap(fileStream))
      } getOrElse {
        //println("exception: dir cannot be listed: " + dir.getPath)
        Stream.empty
      }
    } catch {
      case se: SecurityException => throw new DataException("SecurityException thrown during processing source files.", se)
    }

  private def fileStreamNoDirs(dir: File, extensions: Array[String]): Stream[File] =
    try {
      Option(dir.listFiles).map(_.toList.sortBy(_.getName).toStream.partition(_.isDirectory))
        .map { case (dirs, files) =>
          files.filter(f => checkExtension(f,extensions)).append(dirs.flatMap(fileStreamNoDirs(_,extensions)))
        } getOrElse {
        Stream.empty
      }
    } catch {
      case se: SecurityException => throw new DataException("SecurityException thrown during processing source files.", se)
    }

  private def fileToSet(f: File, set: mutable.Set[String], delimiters: Array[String] = Array("\\s")): mutable.Set[String] = {
    try {
      val source = scala.io.Source.fromFile(f)
      val lines = try source.getLines mkString "\n" finally source.close()
      val vec: Vector[String] = lines.split(delimiters.mkString("|")).map(_.trim).toVector
      set ++= vec
    } catch {
      case ioe: IOException => throw new DataException("IOException thrown during data conversion to set.", ioe)
    }
  }

  private def toIntTrace(f: File, map: mutable.Map[String, Int], delimiters: Array[String]): String = {
    try {
      val source = scala.io.Source.fromFile(f)
      val lines = try " " + (source.getLines mkString "\n" replaceAll(delimiters.mkString("|"), " ")) finally source.close()
      val result = map.foldLeft(lines)((a, b) => a.replaceAllLiterally(" " + b._1, " " + b._2.toString)).trim
      result
    } catch {
      case ioe: IOException => throw new DataException("IOException thrown during conversion of source data file.", ioe)
    }
  }
}