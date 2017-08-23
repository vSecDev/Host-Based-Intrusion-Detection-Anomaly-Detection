package DecisionEngine.SMT

import org.scalatest.FunSuite

/**
  * Created by apinter on 23/08/2017.
  */
class SMTReportTest extends FunSuite {

  //SMTTraceReport
  test("SMTTraceReport - classification correct") {
    val tr = new SMTTraceReport("ReportName", 100, 60, true)
    assert(tr.getClassification.equals("ANOMALY"))
    val tr2 = new SMTTraceReport("ReportName", 100, 60, false)
    assert(tr2.getClassification.equals("NORMAL"))
  }
  test("SMTTraceReport - counts correct") {
    val tr = new SMTTraceReport("ReportName", 100, 60, true)
    assert(tr.subtraceCnt == 100)
    assert(tr.anomalyCnt == 60)
    assert(tr.normalCount == 40)
  }
  test("SMTTraceReport - counts correct - 2") {
    val tr = new SMTTraceReport("ReportName", 100, 100, true)
    assert(tr.subtraceCnt == 100)
    assert(tr.anomalyCnt == 100)
    assert(tr.normalCount == 0)
  }
  test("SMTTraceReport -  trace counts are non-negative") {
    val caught = intercept[IllegalArgumentException](new SMTTraceReport("ReportName", -1, 60, true))
    assert(caught.getMessage.equals("requirement failed: SMTTraceReport subtrace count must be non-negative!"))
  }
  test("SMTTraceReport -  anomalous subtrace count not higher than overall subtrace count") {
    val caught = intercept[IllegalArgumentException](new SMTTraceReport("ReportName", 1, 60, true))
    assert(caught.getMessage.equals("requirement failed: SMTTraceReport anomalous subtrace count cannot be higher than overall subtrace count!"))
  }
  test("SMTTraceReport -  anomalous subtrace count non-negative") {
    val caught = intercept[IllegalArgumentException](new SMTTraceReport("ReportName", 10, -1, true))
    assert(caught.getMessage.equals("requirement failed: SMTTraceReport anomalous subtrace count must be non-negative!"))
  }
  test("SMTTraceReport - anomaly/normalPercentage correct for zero subtraces") {
    val tr = new SMTTraceReport("ReportName", 0, 0, true)
    assert(tr.subtraceCnt == 0)
    assert(tr.anomalyCnt == 0)
    assert(tr.normalCount == 0)
    assert(tr.anomalyPercentage isEmpty)
    assert(tr.normalPercentage isEmpty)
  }
  test("SMTTraceReport - anomaly/normalPercentage correct for non zero subtraces") {
    val tr = new SMTTraceReport("ReportName", 1, 0, true)
    assert(tr.anomalyPercentage.get == 0.0)
    assert(tr.normalPercentage.get == 100.0)
    val tr2 = new SMTTraceReport("ReportName", 1, 1, true)
    assert(tr2.anomalyPercentage.get == 100.0)
    assert(tr2.normalPercentage.get == 0.0)
  }
  test("SMTTraceReport - anomaly/normalPercentage correct for non zero subtraces - 2") {
    val tr = new SMTTraceReport("ReportName", 100, 50, true)
    assert(tr.anomalyPercentage.get == 50.0)
    assert(tr.normalPercentage.get == 50.0)
    val tr2 = new SMTTraceReport("ReportName", 100, 60, true)
    assert(tr2.anomalyPercentage.get == 60.0)
    assert(tr2.normalPercentage.get == 40.0)
  }
  test("SMTTraceReport - IDs are non-identical") {
    val tr = new SMTTraceReport("ReportName", 100, 50, true)
    val tr2 = new SMTTraceReport("ReportName", 100, 50, true)
    val tr3 = new SMTTraceReport("ReportName", 100, 50, true)
    assert(tr.getID != tr2.getID)
    assert(tr.getID != tr3.getID)
    assert(tr2.getID != tr3.getID)
  }

  //SMTReport
  test("SMTReport - IDs are non-identical") {
    val tr = new SMTReport
    val tr2 = new SMTReport
    val tr3 = new SMTReport
    assert(tr.getID != tr2.getID)
    assert(tr.getID != tr3.getID)
    assert(tr2.getID != tr3.getID)
  }
  test("SMTReport - initialised with empty vector for trace reports") {
    val tr = new SMTReport
    assert(tr.getTraceReports.isEmpty)
  }
  test("SMTReport - addTraceReport works") {
    val tr = new SMTReport
    val tr2 = new SMTReport
    assert(tr.getTraceReports.isEmpty)
    assert(tr2.getTraceReports.isEmpty)

    tr.addTraceReport(new SMTTraceReport("ReportName", 1, 0, true))
    assert(tr.getTraceReports.size == 1)
    assert(tr.getTraceReports(0).getClassification.equals("ANOMALY"))
    assert(tr.getTraceReports(0).name.equals("ReportName"))
    assert(tr2.getTraceReports.isEmpty)
    tr2.addTraceReport(new SMTTraceReport("ReportName2", 1, 0, true))
    tr2.addTraceReport(new SMTTraceReport("ReportName3", 1, 0, false))
    assert(tr2.getTraceReports.size == 2)
    assert(tr2.getTraceReports(0).getClassification.equals("ANOMALY"))
    assert(tr2.getTraceReports(0).name.equals("ReportName2"))
    assert(tr2.getTraceReports(1).getClassification.equals("NORMAL"))
    assert(tr2.getTraceReports(1).name.equals("ReportName3"))

  }
}