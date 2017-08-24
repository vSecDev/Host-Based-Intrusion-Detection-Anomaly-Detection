package DecisionEngine.SMT

import java.text.DecimalFormat
import DecisionEngine.DecisionEngineReport

/**
  * Created by apinter on 11/08/2017.
  */
class SMTReport(threshold: Double, tolerance: Double) extends DecisionEngineReport {
  private val id = SMTReport.inc
  private val df: DecimalFormat = new DecimalFormat("##.##")
  private var traceReports: Vector[SMTTraceReport] = Vector()

  override type T = Vector[SMTTraceReport]
  override def getReport(): Option[Vector[SMTTraceReport]] = Some(getTraceReports)

  //TODO - TEST CLASS
  def getID = id

  def addTraceReport(report: SMTTraceReport): Unit = traceReports = traceReports :+ report

  def getTraceReports: Vector[SMTTraceReport] = traceReports

  def traceCount: Int = traceReports.size

  def normalCount: Int = traceReports.count(!_.classification)

  def anomalyCount: Int = traceReports.count(_.classification)

  def anomalyPercentage: Option[Double] = if(traceReports.isEmpty) None else Some((anomalyCount / traceReports.size.toDouble) * 100.00)

  def normalPercentage: Option[Double] = if(traceReports.isEmpty) None else Some((normalCount/traceReports.size.toDouble) * 100.00)

  def getAnomalousTraces: Vector[SMTTraceReport] = traceReports.filter(_.classification)

  def getNormalTraces: Vector[SMTTraceReport] = traceReports.filter(!_.classification)

  private def aPercentStr: String = anomalyPercentage match {
    case None => "N/A"
    case Some(x) => df.format(x)
  }

  private def nPercentStr: String = normalPercentage match {
    case None => "N/A"
    case Some(x) => df.format(x)
  }

  override def toString: String = {
    val sb = new StringBuilder
    sb.append("\nSMT Report ID: " + id + " - Trace Count: " + traceCount + " - Anomalous traces: " + anomalyCount + " - Normal traces: " + normalCount + " - Anomaly percentage: " + aPercentStr + " - Normal percentage: " + nPercentStr + " - Threshold: " + threshold + " - Tolerance: " + tolerance + "\nClassified traces: ")

    for(tr <- traceReports){
      sb.append(tr.toString)
    }
    sb.toString
  }

}
object SMTReport{
  private var id = 0
  private def inc = {id+= 1; id}
}

//TODO - TEST CLASS
class SMTTraceReport(val name: String, val subtraceCnt: Int, val anomalyCnt: Int, val classification: Boolean, quotients: Vector[Double], threshold: Double, tolerance: Double){

  private val id = SMTTraceReport.inc
  private val df: DecimalFormat = new DecimalFormat("##.##")

  require(subtraceCnt >= 0, "SMTTraceReport subtrace count must be non-negative!")
  require(anomalyCnt >= 0, "SMTTraceReport anomalous subtrace count must be non-negative!")
  require(anomalyCnt <= subtraceCnt, "SMTTraceReport anomalous subtrace count cannot be higher than overall subtrace count!")
  require(threshold >= 0.0, "SMTSettings threshold must be non-negative!")
  require(tolerance >= 0.0 && tolerance <= 100.0, "SMTSettings tolerance must be between 0-100%!")

  def getID = id

  def anomalyPercentage: Option[Double] = if(subtraceCnt == 0) None else Some((anomalyCnt.toDouble / subtraceCnt) * 100.00)

  def normalPercentage: Option[Double] = if(subtraceCnt == 0) None else Some((normalCount.toDouble / subtraceCnt) * 100.00)

  def normalCount: Int = subtraceCnt - anomalyCnt

  def getClassification: String = if(classification) "ANOMALY" else "NORMAL"

  def getHistogram: List[List[Double]] = Distribution(10, quotients.toList).histogram

  private def aPercentStr: String = anomalyPercentage match {
    case None => "N/A"
    case Some(x) => df.format(x)
  }

  private def nPercentStr: String = normalPercentage match {
    case None => "N/A"
    case Some(x) => df.format(x)
  }

  override def toString: String = "\nID: "+ id + " - Trace: " + name + " - Subtrace count: " + subtraceCnt + " - Anomalous subtraces: " + anomalyCnt + " - Normal subtraces: " + normalCount + " = Anomaly percentage: " + aPercentStr + " - Classification: " + getClassification

  case class Distribution(nBins: Int, data: List[Double]) {
    require(data.length > nBins)

    val Epsilon = 0.000001
    val (max, min) = (data.max, data.min)
    val binWidth = (max - min) / nBins + Epsilon
    val bounds = (1 to nBins).map { x => min + binWidth * x }.toList

    def histo(bounds: List[Double], data: List[Double]): List[List[Double]] =
      bounds match {
        case h :: Nil => List(data)
        case h :: t => val (l, r) = data.partition(_ < h); l :: histo(t, r)
      }

    val histogram = histo(bounds, data)
  }
}

object SMTTraceReport{
  private var id = 0
  private def inc = {id+= 1; id}
}

