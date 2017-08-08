package Data

import java.io.File

trait DataProcessor[T] {
  def configure(): Unit

  def preprocess(): Option[Map[String, Int]]

  def getData(source: File): Option[DataWrapper[T]]

  def getAllData(source: File): Option[List[DataWrapper[T]]]

  def saveModel(model: DataModel, target: File): Boolean

  def loadModel(source: File): Option[DataModel]
}
