package Data

import scala.io.BufferedSource

trait DataProcessor {
  def configure(): Unit
  def preprocess(): Option[Map[String,Int]]
  def getData(wrapper: DataWrapper): Option[DataWrapper]
}
