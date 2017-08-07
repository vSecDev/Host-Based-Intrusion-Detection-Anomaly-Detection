package Data

import java.io.{BufferedWriter, File, FileWriter, IOException}
import java.nio.file.{Files, Paths}
import org.apache.commons.io.{FileUtils, FilenameUtils}
import scala.collection.mutable

class FileProcessor(_source: File, _target: File, _delimiters: Array[String] = Array("\\s"), _extensions: Array[String] = Array[String]()) extends DataProcessor {

  private var source: Option[File] = None
  private var target: Option[File] = None
  private var delimiters: Array[String] = Array[String]()
  private var extensions: Array[String] = Array[String]()

  require(_source.exists && _source.isDirectory, "Source directory does not exist or is not a directory!")
  require(_target.exists && _target.isDirectory, "Target directory does not exist or is not a directory!")

  setSource(_source)
  setTarget(_target)
  setDelimiters(_delimiters)
  setExtensions(_extensions)

  def getSource = source
  def setSource(newSource: File) = if(newSource.exists && newSource.isDirectory) source = newSource
  def getTarget = target
  def setTarget(newTarget: File) = if(newTarget.exists && newTarget.isDirectory) target = newTarget
  def getDelimiters = delimiters
  def setDelimiters(newDelimiters: Array[String]) = if(newDelimiters.isEmpty) delimiters = Array("\\s") else delimiters = newDelimiters
  def getExtensions = extensions
  def setExtensions(newExtensions: Array[String]) = if(newExtensions.isEmpty) extensions = Array[String]() else extensions = newExtensions


  override def configure(): Unit = {}

  //TODO - FIX EXCEPTION HANDLING - Lock resources!
  @throws(classOf[DataException])
  override def preprocess(): Map[String, Int] = {
    try {
      clearDir(target)

      var sysCallSet = mutable.Set[String]()
      var sysCallMap = Map[String, Int]()

      fileStreamNoDirs(source).foreach(f => sysCallSet = fileToSet(f, sysCallSet))
      sysCallMap = sysCallSet.zipWithIndex.map { case (v, i) => (v, i + 1) }.toMap
      fileStream(source).foreach(f => {
        if (f.isDirectory) {
          val path = target.getCanonicalPath + f.getCanonicalPath.replace(source.getCanonicalPath, "")
          Files.createDirectory(Paths.get(path))
        } else {
          if (extensions.exists(f.getName.endsWith(_))) {

            val path = target.getCanonicalPath + FilenameUtils.removeExtension(f.getCanonicalPath.replace(source.getCanonicalPath, "")) + "_INT.IDS"
            val intTrace = toIntTrace(f, sysCallMap)
            val newFile = new File(path)
            val bw = new BufferedWriter(new FileWriter(newFile))
            bw.write(intTrace)
            bw.close()
          }
        }
      })
      sysCallMap
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

  private def fileStreamNoDirs(dir: File): Stream[File] =
    try {
      Option(dir.listFiles).map(_.toList.sortBy(_.getName).toStream.partition(_.isDirectory))
        .map { case (dirs, files) =>
          files.filter(f => extensions.exists(f.getName.endsWith(_))).append(dirs.flatMap(fileStreamNoDirs))
        } getOrElse {
        //println("exception: dir cannot be listed: " + dir.getPath)
        Stream.empty
      }
    } catch {
      case se: SecurityException => throw new DataException("SecurityException thrown during processing source files.", se)
    }

  private def fileToSet(f: File, set: mutable.Set[String]): mutable.Set[String] = {
    try {
      val source = scala.io.Source.fromFile(f)
      val lines = try source.getLines mkString "\n" finally source.close()
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
      //val result = map.foldLeft(lines)((a, b) => a.replaceAllLiterally(b._1, b._2.toString))
      val result = map.foldLeft(lines)((a, b) => a.replaceAllLiterally(b._1, b._2.toString)).replaceAll(delimiters.mkString("|"), " ")
      result
    } catch {
      case ioe: IOException => throw new DataException("IOException thrown during conversion of source data file.", ioe)
    }
  }
}