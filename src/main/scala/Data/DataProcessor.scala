package Data

import java.io.File
import scala.collection.mutable

trait DataProcessor {
  def configure(): Unit

  def preprocess(source: File, target: File, delimiters: Array[String], extensions: Array[String]): Option[mutable.Map[String, Int]]

  def getData(source: File, extensions: Array[String]): Option[DataWrapper]

  def getAllData(source: File, extensions: Array[String]): Option[Vector[DataWrapper]]

  def saveModel(model: DataModel, target: File): Boolean

  def loadModel(model: DataModel, source: File): Option[DataModel]

}
