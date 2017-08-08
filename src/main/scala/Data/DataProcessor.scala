package Data

import java.io.File

trait DataProcessor {
  def configure(): Unit
  def preprocess(): Option[Map[String,Int]]
  def getData(wrapper: DataWrapper): Option[DataWrapper]
  def saveModel(model: DataModel): Boolean
  def loadModel(source: File): Option[DataModel]
}
