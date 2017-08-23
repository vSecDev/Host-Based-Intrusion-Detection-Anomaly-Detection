package DecisionEngine.SMT

import org.scalatest.FunSuite

/**
  * Created by apinter on 23/08/2017.
  */
class SMTReportTest extends FunSuite{

  test("SMTTraceReport - classification correct"){
    val tr = new SMTTraceReport("ReportName", 100, 60, true)
    assert(tr.getClassification.equals("ANOMALY"))
  }
  test("SMTTraceReport - counts correct"){
    val tr = new SMTTraceReport("ReportName", 100, 60, true)
    assert(tr.subtraceCnt == 100)
    assert(tr.anomalyCnt == 60)
    assert(tr.normalCount == 40)
  }
  test("SMTTraceReport - counts correct - 2"){
    val tr = new SMTTraceReport("ReportName", 100, 100, true)
    assert(tr.subtraceCnt == 100)
    assert(tr.anomalyCnt == 100)
    assert(tr.normalCount == 0)
  }
  test("SMTTraceReport -  trace counts are non-negative"){
    val caught = intercept[IllegalArgumentException](new SMTTraceReport("ReportName", -1, 60, true))
    assert(caught.getMessage.equals("requirement failed: SMTTraceReport subtrace count must be non-negative!"))
  }
  test("SMTTraceReport -  anomalous subtrace count not higher than overall subtrace count"){
    val caught = intercept[IllegalArgumentException](new SMTTraceReport("ReportName", 1, 60, true))
    assert(caught.getMessage.equals("requirement failed: SMTTraceReport anomalous subtrace count cannot be higher than overall subtrace count!"))
  }
  test("SMTTraceReport -  anomalous subtrace count non-negative"){
    val caught = intercept[IllegalArgumentException](new SMTTraceReport("ReportName", 10, -1, true))
    assert(caught.getMessage.equals("requirement failed: SMTTraceReport anomalous subtrace count must be non-negative!"))
  }
  test("SMTTraceReport - anomaly/normalPercentage correct for zero subtraces"){
    val tr = new SMTTraceReport("ReportName", 0, 0, true)
    assert(tr.subtraceCnt == 0)
    assert(tr.anomalyCnt == 0)
    assert(tr.normalCount == 0)
    assert(tr.anomalyPercentage isEmpty)
    assert(tr.normalPercentage isEmpty)
  }
  test("SMTTraceReport - anomaly/normalPercentage correct for non zero subtraces"){
    val tr = new SMTTraceReport("ReportName", 1, 0, true)
    assert(tr.anomalyPercentage.get == 0.0)
    assert(tr.normalPercentage.get == 100.0)
    val tr2 = new SMTTraceReport("ReportName", 1, 1, true)
    assert(tr2.anomalyPercentage.get == 100.0)
    assert(tr2.normalPercentage.get == 0.0)
  }

}
