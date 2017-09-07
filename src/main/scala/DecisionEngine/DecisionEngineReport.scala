package DecisionEngine

/**
  * Created by apinter on 11/08/2017.
  */
trait DecisionEngineReport {
  type T

  def getReport(): Option[T]

  def getReportName(): Option[String]
}
