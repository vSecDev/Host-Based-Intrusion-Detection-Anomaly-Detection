package Data

trait DataProcessor {
  def configure(): Unit
  def preprocess(): Option[Map[String,Int]]
}
