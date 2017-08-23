package DecisionEngine.SMT

import DecisionEngine.DecisionEngineReport

/**
  * Created by apinter on 11/08/2017.
  */
class SMTReport extends DecisionEngineReport {
  private val id = SMTReport.inc
  private var traceReports: Vector[TraceReport] = Vector()




  override type T = this.type
  override var data: Option[SMTReport.this.type] = _

  override def store(_data: SMTReport.this.type): Unit = ???

  override def retrieve(): Option[SMTReport.this.type] = ???

  //TODO - TEST CLASS
  def addTraceReport(report: TraceReport) = traceReports :+ report
  def traceCount: Int = traceReports.size
  def normalCount: Int = traceReports.count(!_.classification)
  def anomalyCount: Int = traceReports.count(_.classification)
  def anomalyPercentage: Option[Double] = if(traceReports.isEmpty) None else Some((anomalyCount / traceReports.size.toDouble) * 100.00)
  def normalPercentage: Option[Double] = if(traceReports.isEmpty) None else Some((normalCount/traceReports.size.toDouble) * 100.00)
  def getAnomalousTraces: Vector[TraceReport] = traceReports.filter(_.classification)
  def getNormalTraces: Vector[TraceReport] = traceReports.filter(!_.classification)


  override def toString: String = {
    val sb = new StringBuilder
    sb.append("\nSMT Report ID: " + id + " - Trace Count: " + traceCount + " - Anomalous traces: " + anomalyCount + " - Normal traces: " + normalCount + " - Anomaly percentage: " + anomalyPercentage + " - Normal percentage: " + normalPercentage + "\nClassified traces: ")

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
class TraceReport(val name: String, val subtraceCnt: Int, val anomalyCnt: Int, val classification: Boolean){
  private val id = TraceReport.inc

  //Percentage of anomalous subtraces within one sequence of system calls
  def anomalyPercentage: Double = (anomalyCnt / subtraceCnt) * 100.00
  def normalCount: Int = subtraceCnt - anomalyCnt
  def getClassification: String = if(classification) "ANOMALY" else "NORMAL"

  override def toString: String = "\nID: "+ id + " - Trace: " + name + " - Subtrace count: " + subtraceCnt + " - Anomalous subtraces: " + anomalyCnt + " - Normal subtraces: " + normalCount + " = Anomaly percentage: " + anomalyPercentage + " - Classification: " + getClassification
}

object TraceReport{
  private var id = 0
  private def inc = {id+= 1; id}
}

