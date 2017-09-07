package DecisionEngine.SMT

import java.text.DecimalFormat
import DecisionEngine.DecisionEngineReport

/**
  * Created by apinter on 24/08/2017.
  */
class SMTValidationReport(report: SMTReport) extends DecisionEngineReport{

  private val id = SMTValidationReport.inc
  private val df: DecimalFormat = new DecimalFormat("##.##")

  override type T = SMTReport

  override def getReport(): Option[SMTReport] = Some(report)

  override def getReportName(): Option[String] = Some(getID.toString)

  def getID = id

  def classificationError = report.anomalyPercentage

  def sensitivity = report.normalCount.toDouble / (report.normalCount + report.anomalyCount)

  def specificity = 0.0

  def precision = 1.0

  def recall = sensitivity

  private def classErrorStr = classificationError match {
    case None => "N/A"
    case Some(x) => df.format(x)
  }

  private def sensitivityStr = df.format(sensitivity.toDouble)

  override def toString: String = report.toString + "\nClassification error: " + classErrorStr + " = Sensitivity: " + sensitivityStr
}

object SMTValidationReport{
  private var id = 0
  private def inc = {id+= 1; id}
}