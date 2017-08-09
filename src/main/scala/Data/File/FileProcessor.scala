package Data.File

import java.io.{BufferedWriter, File, FileWriter, IOException}
import java.nio.file.{Files, Paths}
import Data._
import org.apache.commons.io.{FileUtils, FilenameUtils}
import scala.collection.mutable

class FileProcessor extends DataProcessor {

  //_source: File, _target: File, _delimiters: Array[String] = Array("\\s"), _extensions: Array[String] = Array[String]()

  /*private var source: Option[File] = None
  private var target: Option[File] = None
  private var delimiters: Array[String] = Array[String]()
  private var extensions: Array[String] = Array[String]()

  require(_source.exists && _source.isDirectory, "Source directory does not exist or is not a directory!")
  require(_target.exists && _target.isDirectory, "Target directory does not exist or is not a directory!")*/

  /*setSource(_source)
  setTarget(_target)
  setDelimiters(_delimiters)
  setExtensions(_extensions)*/

  /*def getSource: Option[File] = source
  def setSource(newSource: File): Unit = if(newSource.exists && newSource.isDirectory) source = Some(newSource)
  def getTarget: Option[File] = target
  def setTarget(newTarget: File): Unit = if(newTarget.exists && newTarget.isDirectory) target = Some(newTarget)
  def getDelimiters: Array[String] = delimiters
  def setDelimiters(newDelimiters: Array[String]): Unit = if(newDelimiters.isEmpty) delimiters = Array("\\s") else delimiters = newDelimiters
  def getExtensions: Array[String] = extensions
  def setExtensions(newExtensions: Array[String]): Unit = if(newExtensions.isEmpty) extensions = Array[String]() else extensions = newExtensions*/

  override def configure(): Unit = {}

  //TODO - HANDLE PREVIOUSLY UNSEEN CALLS
  //TODO - FIX EXCEPTION HANDLING - Lock resources!
  @throws(classOf[DataException])
  override def preprocess(source: File, target: File, delimiters: Array[String], extensions: Array[String]): Option[Map[String, Int]] = {
    if(!checkDirs(source, target)) return None
    try {
      clearDir(target)

      var sysCallSet = mutable.Set[String]()
      var sysCallMap = Map[String, Int]()
      fileStreamNoDirs(source, extensions).foreach(f => sysCallSet = fileToSet(f, sysCallSet, delimiters))
      sysCallMap = sysCallSet.zipWithIndex.map { case (v, i) => (v, i + 1) }.toMap

      println("\nsysCallMap: " + sysCallMap)
      fileStream(source).foreach(f => {
        if (f.isDirectory) {
          val path = target.getCanonicalPath + f.getCanonicalPath.replace(source.getCanonicalPath, "")
          Files.createDirectory(Paths.get(path))
        } else {
          if (extensions.exists(f.getName.endsWith(_))) {
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
    if (f.exists() && f.isFile && extensions.contains(FilenameUtils.getExtension(f.getName))) {
      val src = scala.io.Source.fromFile(f)
      val lines = try src.getLines mkString "\n" finally src.close()
      val wrapper = new StringDataWrapper
      wrapper.store(lines)
      Some(wrapper)
    } else {
      None
    }
  }

  override def getAllData(d: File, extensions: Array[String]): Option[Vector[StringDataWrapper]] = {
    if (d.exists() && d.isDirectory) {
      val fs = fileStreamNoDirs(d, extensions)
      var ws = Vector[StringDataWrapper]()

      fs.foreach(f => {
        val src = scala.io.Source.fromFile(f)
        val lines = try src.getLines mkString "\n" finally src.close()
        val wrapper = new StringDataWrapper
        wrapper.store(lines)
        ws = ws :+ wrapper
      })
      Some(ws)
    } else { None }
  }

  override def saveModel(model: DataModel, target: File): Boolean = ???

  override def loadModel(source: File): Option[DataModel] = ???

  private def checkDirs(source: File, target: File): Boolean = {
    source.exists && target.exists && source.isDirectory && target.isDirectory
  }

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
          files.filter(f => extensions.exists(f.getName.endsWith(_))).append(dirs.flatMap(fileStreamNoDirs(_,extensions)))
        } getOrElse {
        //println("exception: dir cannot be listed: " + dir.getPath)
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

  private def toIntTrace(f: File, map: Map[String, Int], delimiters: Array[String]): String = {
    try {
      val source = scala.io.Source.fromFile(f)
      val lines = try source.getLines mkString "\n" finally source.close()
      println("lines: " + lines)
      //val result = map.foldLeft(lines)((a, b) => a.replaceAllLiterally(b._1, b._2.toString))
      val result = map.foldLeft(lines)((a, b) => a.replaceAllLiterally(b._1, b._2.toString)).replaceAll(delimiters.mkString("|"), " ")
      println("result: " + result)
      result
    } catch {
      case ioe: IOException => throw new DataException("IOException thrown during conversion of source data file.", ioe)
    }
  }
}