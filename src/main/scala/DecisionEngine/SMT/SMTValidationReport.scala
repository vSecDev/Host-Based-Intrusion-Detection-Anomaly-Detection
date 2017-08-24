package DecisionEngine.SMT

import DecisionEngine.DecisionEngineReport

/**
  * Created by apinter on 24/08/2017.
  */
class SMTValidationReport(report: SMTReport) extends DecisionEngineReport{

  override type T = SMTReport
  override def getReport(): Option[SMTReport] = Some(report)

  def classificationError = report.anomalyPercentage
  def sensitivity = report.normalCount.toDouble / (report.normalCount + report.anomalyCount)
  def specificity = 0.0
  def precision = 1.0
  def recall = sensitivity
}
