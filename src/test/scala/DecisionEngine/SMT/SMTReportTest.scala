package DecisionEngine.SMT

import org.scalatest.FunSuite

/**
  * Created by apinter on 23/08/2017.
  */
class SMTReportTest extends FunSuite{

  test("TraceReport - classification correct"){
    val tr = new TraceReport("ReportName", 100, 60, true)
    assert(tr.getClassification.equals("ANOMALY"))
  }
  test("TraceReport - counts correct"){
    val tr = new TraceReport("ReportName", 100, 60, true)
    assert(tr.subtraceCnt == 100)
    assert(tr.anomalyCnt == 60)
    assert(tr.normalCount == 40)
  }
}
