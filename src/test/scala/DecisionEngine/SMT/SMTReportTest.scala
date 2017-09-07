package DecisionEngine.SMT

import org.scalatest.FunSuite

/**
  * Created by apinter on 23/08/2017.
  */
class SMTReportTest extends FunSuite {

  //SMTTraceReport
  test("SMTTraceReport - classification correct") {
    val tr = new SMTTraceReport("ReportName", 100, 60, true, Vector(0.1,0.1), 0.1, 0.1)
    assert(tr.getClassification.equals("ANOMALY"))
    val tr2 = new SMTTraceReport("ReportName", 100, 60, false, Vector(0.1,0.1), 0.1, 0.1)
    assert(tr2.getClassification.equals("NORMAL"))
  }
  test("SMTTraceReport - counts correct") {
    val tr = new SMTTraceReport("ReportName", 100, 60, true, Vector(0.1,0.1), 0.1, 0.1)
    assert(tr.subtraceCnt == 100)
    assert(tr.anomalyCnt == 60)
    assert(tr.normalCount == 40)
  }
  test("SMTTraceReport - counts correct - 2") {
    val tr = new SMTTraceReport("ReportName", 100, 100, true, Vector(0.1,0.1), 0.1, 0.1)
    assert(tr.subtraceCnt == 100)
    assert(tr.anomalyCnt == 100)
    assert(tr.normalCount == 0)
  }
  test("SMTTraceReport -  trace counts are non-negative") {
    val caught = intercept[IllegalArgumentException](new SMTTraceReport("ReportName", -1, 60, true, Vector(0.1,0.1), 0.1, 0.1))
    assert(caught.getMessage.equals("requirement failed: SMTTraceReport subtrace count must be non-negative!"))
  }
  test("SMTTraceReport -  anomalous subtrace count not higher than overall subtrace count") {
    val caught = intercept[IllegalArgumentException](new SMTTraceReport("ReportName", 1, 60, true, Vector(0.1,0.1), 0.1, 0.1))
    assert(caught.getMessage.equals("requirement failed: SMTTraceReport anomalous subtrace count cannot be higher than overall subtrace count!"))
  }
  test("SMTTraceReport -  anomalous subtrace count non-negative") {
    val caught = intercept[IllegalArgumentException](new SMTTraceReport("ReportName", 10, -1, true, Vector(0.1,0.1), 0.1, 0.1))
    assert(caught.getMessage.equals("requirement failed: SMTTraceReport anomalous subtrace count must be non-negative!"))
  }
  test("SMTTraceReport - anomaly/normalPercentage correct for zero subtraces") {
    val tr = new SMTTraceReport("ReportName", 0, 0, true, Vector(0.1,0.1), 0.1, 0.1)
    assert(tr.subtraceCnt == 0)
    assert(tr.anomalyCnt == 0)
    assert(tr.normalCount == 0)
    assert(tr.anomalyPercentage isEmpty)
    assert(tr.normalPercentage isEmpty)
  }
  test("SMTTraceReport - anomaly/normalPercentage correct for non zero subtraces") {
    val tr = new SMTTraceReport("ReportName", 1, 0, true, Vector(0.1,0.1), 0.1, 0.1)
    assert(tr.anomalyPercentage.get == 0.0)
    assert(tr.normalPercentage.get == 100.0)
    val tr2 = new SMTTraceReport("ReportName", 1, 1, true, Vector(0.1,0.1), 0.1, 0.1)
    assert(tr2.anomalyPercentage.get == 100.0)
    assert(tr2.normalPercentage.get == 0.0)
  }
  test("SMTTraceReport - anomaly/normalPercentage correct for non zero subtraces - 2") {
    val tr = new SMTTraceReport("ReportName", 100, 50, true, Vector(0.1,0.1), 0.1, 0.1)
    assert(tr.anomalyPercentage.get == 50.0)
    assert(tr.normalPercentage.get == 50.0)
    val tr2 = new SMTTraceReport("ReportName", 100, 60, true, Vector(0.1,0.1), 0.1, 0.1)
    assert(tr2.anomalyPercentage.get == 60.0)
    assert(tr2.normalPercentage.get == 40.0)
  }
  test("SMTTraceReport - IDs are non-identical") {
    val tr = new SMTTraceReport("ReportName", 100, 50, true, Vector(0.1,0.1), 0.1, 0.1)
    val tr2 = new SMTTraceReport("ReportName", 100, 50, true, Vector(0.1,0.1), 0.1, 0.1)
    val tr3 = new SMTTraceReport("ReportName", 100, 50, true, Vector(0.1,0.1), 0.1, 0.1)
    assert(tr.getID != tr2.getID)
    assert(tr.getID != tr3.getID)
    assert(tr2.getID != tr3.getID)
  }

  //SMTReport
  test("SMTReport - IDs are non-identical") {
    val tr = new SMTReport(0.5, 30.0)
    val tr2 = new SMTReport(0.5, 30.0)
    val tr3 = new SMTReport(0.5, 30.0)
    assert(tr.getID != tr2.getID)
    assert(tr.getID != tr3.getID)
    assert(tr2.getID != tr3.getID)
  }
  test("SMTReport - initialised with empty vector for trace reports") {
    val tr = new SMTReport(0.5, 30.0)
    assert(tr.getTraceReports.isEmpty)
  }
  test("SMTReport - add/getTraceReport works") {
    val tr = new SMTReport(0.5, 30.0)
    val tr2 = new SMTReport(0.5, 30.0)
    assert(tr.getTraceReports.isEmpty)
    assert(tr2.getTraceReports.isEmpty)
    tr.addTraceReport(new SMTTraceReport("ReportName", 1, 0, true, Vector(0.1,0.1), 0.1, 0.1))
    assert(tr.getTraceReports.size == 1)
    assert(tr.getTraceReports(0).getClassification.equals("ANOMALY"))
    assert(tr.getTraceReports(0).name.equals("ReportName"))
    assert(tr2.getTraceReports.isEmpty)
    tr2.addTraceReport(new SMTTraceReport("ReportName2", 1, 0, true, Vector(0.1,0.1), 0.1, 0.1))
    tr2.addTraceReport(new SMTTraceReport("ReportName3", 1, 0, false, Vector(0.1,0.1), 0.1, 0.1))
    assert(tr2.getTraceReports.size == 2)
    assert(tr2.getTraceReports(0).getClassification.equals("ANOMALY"))
    assert(tr2.getTraceReports(0).name.equals("ReportName2"))
    assert(tr2.getTraceReports(1).getClassification.equals("NORMAL"))
    assert(tr2.getTraceReports(1).name.equals("ReportName3"))
  }
  test("SMTReport - traceCount works") {
    val tr = new SMTReport(0.5, 30.0)
    val tr2 = new SMTReport(0.5, 30.0)
    assert(tr.traceCount == 0)
    assert(tr2.traceCount == 0)
    tr.addTraceReport(new SMTTraceReport("ReportName", 1, 0, true, Vector(0.1,0.1), 0.1, 0.1))
    tr.addTraceReport(new SMTTraceReport("ReportName2", 1, 0, true, Vector(0.1,0.1), 0.1, 0.1))
    tr.addTraceReport(new SMTTraceReport("ReportName3", 1, 0, false, Vector(0.1,0.1), 0.1, 0.1))
    assert(tr.traceCount == 3)
    assert(tr2.traceCount == 0)
  }
  test("SMTReport - normal/anomalyCount works") {
    val tr = new SMTReport(0.5, 30.0)
    val tr2 = new SMTReport(0.5, 30.0)
    assert(tr.normalCount == 0)
    assert(tr2.normalCount == 0)
    tr.addTraceReport(new SMTTraceReport("ReportName", 1, 0, true, Vector(0.1,0.1), 0.1, 0.1))
    tr.addTraceReport(new SMTTraceReport("ReportName2", 1, 0, true, Vector(0.1,0.1), 0.1, 0.1))
    tr.addTraceReport(new SMTTraceReport("ReportName3", 1, 0, false, Vector(0.1,0.1), 0.1, 0.1))
    assert(tr.traceCount == 3)
    assert(tr.normalCount == 1)
    assert(tr.anomalyCount == 2)
    assert(tr2.normalCount == 0)
  }
  test("SMTReport - normal/anomalyPercentage functions work") {
    val tr = new SMTReport(0.5, 30.0)
    val tr2 = new SMTReport(0.5, 30.0)
    assert(!tr.normalPercentage.isDefined)
    assert(!tr.anomalyPercentage.isDefined)
    assert(!tr2.normalPercentage.isDefined)
    assert(!tr2.anomalyPercentage.isDefined)
    tr.addTraceReport(new SMTTraceReport("ReportName", 1, 0, true, Vector(0.1,0.1), 0.1, 0.1))
    tr.addTraceReport(new SMTTraceReport("ReportName2", 1, 0, true, Vector(0.1,0.1), 0.1, 0.1))
    tr.addTraceReport(new SMTTraceReport("ReportName3", 1, 0, false, Vector(0.1,0.1), 0.1, 0.1))
    assert(tr.normalPercentage.get == 1/3.0 * 100)
    assert(tr.anomalyPercentage.get == 2/3.0 * 100)
    assert(!tr2.normalPercentage.isDefined)
    assert(!tr2.anomalyPercentage.isDefined)
    tr.addTraceReport(new SMTTraceReport("ReportName4", 1, 0, false, Vector(0.1,0.1), 0.1, 0.1))
    assert(tr.normalPercentage.get == 2/4.0 * 100)
    assert(tr.anomalyPercentage.get == 2/4.0 * 100)
    tr.addTraceReport(new SMTTraceReport("ReportName5", 1, 0, false, Vector(0.1,0.1), 0.1, 0.1))
    assert(tr.normalPercentage.get == 3/5.0 * 100)
    assert(tr.anomalyPercentage.get == 2/5.0 * 100)
  }
  test("SMTReport - getNormal/AnomalousTraces functions work"){
    val tr = new SMTReport(0.5, 30.0)
    assert(tr.getNormalTraces.isEmpty)
    assert(tr.getAnomalousTraces.isEmpty)
    tr.addTraceReport(new SMTTraceReport("ReportName", 2, 1, true, Vector(0.1,0.1), 0.1, 0.1))
    tr.addTraceReport(new SMTTraceReport("ReportName2", 3, 2, true, Vector(0.1,0.1), 0.1, 0.1))
    tr.addTraceReport(new SMTTraceReport("ReportName3", 1, 0, false, Vector(0.1,0.1), 0.1, 0.1))

    val n = tr.getNormalTraces
    assert(n.size == 1)
    //assert(n(0).getID == 3)
    assert(n(0).getClassification.equals("NORMAL"))
    assert(n(0).subtraceCnt == 1)
    assert(n(0).anomalyCnt == 0)
    assert(n(0).normalCount == 1)

    val a = tr.getAnomalousTraces
    assert(a.size == 2)
    //assert(a(0).getID == 1)
    //assert(a(1).getID == 2)
    assert(a(0).getClassification.equals("ANOMALY"))
    assert(a(1).getClassification.equals("ANOMALY"))
    assert(a(0).subtraceCnt == 2)
    assert(a(0).anomalyCnt == 1)
    assert(a(0).normalCount == 1)
    assert(a(1).subtraceCnt == 3)
    assert(a(1).anomalyCnt == 2)
    assert(a(1).normalCount == 1)
  }
}