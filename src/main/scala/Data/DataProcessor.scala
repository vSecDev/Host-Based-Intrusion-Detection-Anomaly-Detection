package Data

import java.io._

trait DataProcessor {
  def configure(): Unit
  def preprocess(): Option[Map[String,Int]]
}
