package DecisionEngine.SMT

import DecisionEngine.DecisionEngineReport

/**
  * Created by apinter on 11/08/2017.
  */
class SMTReport extends DecisionEngineReport {
  private var traceReports: Option[Vector[TraceReport]] = None

}

//TODO - TEST CLASS
class TraceReport(val name: String, val subtraceCnt: Int, val anomalyCnt: Int, classification: Boolean){
  //Percentage of anomalous subtraces within one sequence of system calls
  def anomalyPercentage: Double = (anomalyCnt / subtraceCnt) * 100.00
  def normalCount: Int = subtraceCnt - anomalyCnt
  def getClassification: String = if(classification) "ANOMALY" else "NORMAL"

  override def toString: String = "\nTrace: " + name + " - Subtrace count: " + subtraceCnt + " - Anomalous subtraces: " + anomalyCnt + " - Normal subtraces: " + normalCount + " = Anomaly percentage: " + anomalyPercentage + " - Classification: " + getClassification
}


