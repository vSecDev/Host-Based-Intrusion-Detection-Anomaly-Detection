package Data

import java.io.File

trait DataProcessor {
  def configure(): Unit

  def preprocess(): Option[Map[String, Int]]

  def getData(source: File): Option[DataWrapper]

  def getAllData(source: File): Option[Vector[DataWrapper]]

  def saveModel(model: DataModel, target: File): Boolean

  def loadModel(source: File): Option[DataModel]
}
