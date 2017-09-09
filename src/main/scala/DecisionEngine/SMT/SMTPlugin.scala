package DecisionEngine.SMT

import java.util.Observable
import Data.{DataModel, DataWrapper, StringDataWrapper}
import DecisionEngine.{DecisionEngineConfig, DecisionEngineGUI, DecisionEnginePlugin, DecisionEngineReport}
import GUI.HIDS

/**
  * Created by apinter on 08/08/2017.
  */
class SMTPlugin(gui: SMTGUI) extends Observable with DecisionEnginePlugin {

  override val pluginName: String = "Sparse Markov Tree"
  private var currHIDS: Option[HIDS] = None
  private var root: Option[Node[_, _]] = None
  private var isIntRoot: Boolean = false
  private var threshold: Option[Double] = None
  private var tolerance: Option[Double] = None
  private var lastReport: Option[DecisionEngineReport] = None
  private var learnFlag: Boolean = false
  private var classifyFlag: Boolean = false
  private var validateFlag: Boolean = false
  private var loadModelFlag: Boolean = false
  private var saveModelFlag: Boolean = false
  private var saveReportFlag: Boolean = false

  gui.setPluginInstance(this)

  override def registerHIDS(hids: HIDS): Boolean = {
    currHIDS = Some(hids)
    addObserver(currHIDS.get)
    true
  }

  override def configure(config: DecisionEngineConfig): Boolean = config match {
    case c: SMTConfig =>
      try
        c.getSettings match {
          case None => false
          case Some(s) => if (s.isIntTrace) {
            val newRoot = new Node[Int, Int](s.maxDepth, s.maxPhi, s.maxSeqCount, s.smoothing, s.prior)
            newRoot.setKey(Int.MinValue)
            setRoot(newRoot, isInt = true)
            setThreshold(s.threshold)
            setTolerance(s.tolerance)
            gui.render
            true
          } else {
            val newRoot = new Node[String, String](s.maxDepth, s.maxPhi, s.maxSeqCount, s.smoothing, s.prior)
            newRoot.setKey("Root")
            setRoot(newRoot, isInt = false)
            setThreshold(s.threshold)
            setTolerance(s.tolerance)
            gui.render
            false
          }
        }
      catch {
        case iae: IllegalArgumentException => false
      }
    case _ => false
  }

  override def getConfiguration: Option[DecisionEngineConfig] = if (isConfigured) {
    val config = new SMTConfig
    val r = root.get
    val settings = new SMTSettings(r.maxDepth, r.maxPhi, r.maxSeqCount, r.smoothing, r.prior, r.isInstanceOf[Node[Int, Int]], threshold.get, tolerance.get)
    config.storeSettings(settings)
    Some(config)
  } else None

  override def getGUI: Option[DecisionEngineGUI] = Some(gui)

  override def isIntModel: Boolean = isIntRoot

  override def update(o: Observable, arg: Any): Unit = {
    super.update(o, arg)
    gui.render
  }

  override def learn(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DataModel] = {
    gui.appendText("Training SMT...")

    if (data.isEmpty) {
      gui.appendText("No data to process...")
      resetLearn
      return model
    }

    model match {
      case None =>
        root match {
          case None =>
            resetLearn
            None //No model/SMT to train
          case Some(node) =>
            val result = learnHelper(data, node, ints)
            resetLearn
            gui.appendText("Training completed.")
            gui.render
            result
        }
      case Some(w) =>
        w.retrieve match {
          case None =>
            resetLearn
            None
          case Some(m) => m match {
            case node: Node[_, _] =>
              val result = learnHelper(data, node, ints)
              resetLearn
              gui.appendText("Training completed.")
              gui.render
              result
            case _ =>
              resetLearn
              None
          }
        }
    }
  }

  override def validate(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DecisionEngineReport] = {

    gui.appendText("Validating input...")
    classify(data, model, ints) match {
      case None =>
        gui.appendText("Validation completed! No trace report to display. The analysed files may be empty or may contain too short traces.")
        resetValidate
        None
      case Some(report: SMTReport) =>
        gui.appendText("Validation completed!")
        val vReport = new SMTValidationReport(report)
        resetValidate
        lastReport = Some(vReport)
        gui.appendText(vReport.toString)
        gui.render
        Some(vReport)
    }
  }

  override def classify(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DecisionEngineReport] = {

    if (!validateFlag)
      gui.appendText("Classifying input...")

    if (data.isEmpty || threshold.isEmpty || tolerance.isEmpty) {
      gui.appendText("No data to process or threshold or tolerance not set...")
      resetClassify
      return None
    }

    model match {
      case None =>
        root match {
          case None =>
            resetClassify
            None //No model/SMT to classify with
          case Some(node) =>
            val report = classifyHelper(data, node, ints)
            if (!validateFlag) {
              if (report.isEmpty) {
                gui.appendText("Classification completed! No trace report to display. The analysed files may be empty or may contain too short traces.")
              } else {
                gui.appendText("Classification completed!\n" + report.get.toString)
                lastReport = report
                gui.render
              }
            }
            resetClassify
            report
        }
      case Some(w) =>
        w.retrieve match {
          case None =>
            resetClassify
            None
          case Some(m) => m match {
            case node: Node[_, _] =>
              val report = classifyHelper(data, node, ints)
              if (!validateFlag) {
                if (report.isEmpty) {
                  gui.appendText("Classification completed! No trace report to display. The analysed files may be empty or may contain too short traces.")
                } else {
                  gui.appendText("Classification completed!\n" + report.get.toString)
                  lastReport = report
                  gui.render
                }
              }
              resetClassify
              report
            case _ =>
              resetClassify
              None
          }
        }
    }
  }

  override def loadModel(model: DataModel, isInt: Boolean): Boolean = model.retrieve match {
    case None =>
      resetLoadModel
      false
    case Some(mod) => mod match {
      case m: Node[_, _] =>
        setRoot(m, isInt)
        gui.appendText("New root loaded:\n" + root.get.toString)
        //gui.appendText("\n\n\nTEST:\n\n\n" + root.get.toXML)
        resetLoadModel
        true
      case _ =>
        resetLoadModel
        false
    }
  }

  override def getModel(): Option[DataModel] = root match {
    case None => None
    case Some(m) =>
      val dm = new DataModel
      dm.store(m)
      Some(dm)
  }

  override def saveModel: Option[DataModel] = {
    resetSaveModel
    getModel
  }

  override def saveReport: Option[DecisionEngineReport] = {
    resetSaveReport
    lastReport
  }

  override def getModelName: Option[String] = root match {
    case None => None
    case Some(node) =>
      Some(
        node.maxDepth.toString + "_" +
          node.maxPhi.toString + "_" +
          node.maxSeqCount.toString + "_" +
          node.smoothing.toString + "_" +
          node.prior.toString)
  }


  override def getReportName: Option[String] = root match {
    case None => None
    case Some(node) =>
      lastReport match {
        case None => None
        case Some(r) =>
          var name = node.maxDepth.toString + "_" +
            node.maxPhi.toString + "_" +
            node.maxSeqCount.toString + "_" +
            node.smoothing.toString + "_" +
            node.prior.toString + "_REPORT_" + r.getReportName.get
          Some(name)
      }
  }

  private def learnHelper(data: Vector[DataWrapper], node: Node[_, _], ints: Boolean): Option[DataModel] = {
    data foreach (wrapper => wrapper match {
      case w: StringDataWrapper => w.retrieve match {
        case None =>
        case Some(d) =>
          if (d._2.nonEmpty) {
            gui.appendText("Processing " + d._1)
            if (ints && isIntRoot) {
              //process as int trace
              for (t <- getIntInput(node.maxDepth, d._2)) {
                node.asInstanceOf[Node[Int, Int]].learn(t._1, t._2)
              }
            } else node match {
              case n: Node[String, String] =>
                //process as string trace
                for (t <- getStrInput(n.maxDepth, d._2)) {
                  n.learn(t._1, t._2)
                }
              case _ => //TODO - ADD LOGIC FOR OTHER TYPES
            }
          }
      }
      case _ => //Handle other types of wrappers in future extensions here!
    })
    val dm = new DataModel
    dm.store(node)
    Some(dm)
  }

  private def classifyHelper(data: Vector[DataWrapper], node: Node[_, _], ints: Boolean): Option[DecisionEngineReport] = {
    var report = new SMTReport(threshold.get, tolerance.get)

    data foreach (wrapper => wrapper match {
      case w: StringDataWrapper => w.retrieve match {
        case None =>
        case Some(d) =>
          if (d._2.nonEmpty) {
            if (ints && isIntRoot) {
              //process as int trace
              var quotients: Vector[Double] = Vector()
              for (t <- getIntInput(node.maxDepth, d._2)) {
                val prediction = node.asInstanceOf[Node[Int, Int]].predict(t._1, t._2)
                var quotient = 0.0
                if (prediction._2 != 0.0) {
                  quotient = prediction._1 / prediction._2
                }
                quotients = quotients :+ quotient
              }

              if (quotients.nonEmpty) {
                val anomalyCount = quotients.count(_ < threshold.get)
                val anomalyPercentage = (anomalyCount / quotients.size.toDouble) * 100.00
                val isAnomaly = anomalyPercentage > tolerance.get
                val traceReport = new SMTTraceReport(d._1, quotients.size, anomalyCount, isAnomaly, quotients, threshold.get, tolerance.get)
                report.addTraceReport(traceReport)
                gui.appendText(traceReport.toString)
              }
            } else node match {
              case n: Node[String, String] =>
                //process as string trace
                var quotients: Vector[Double] = Vector()
                for (t <- getStrInput(node.maxDepth, d._2)) {
                  val prediction = node.asInstanceOf[Node[String, String]].predict(t._1, t._2)
                  var quotient = 0.0
                  if (prediction._2 != 0.0) {
                    quotient = prediction._1 / prediction._2
                  }
                  quotients = quotients :+ quotient
                }
                if (quotients.nonEmpty) {
                  val anomalyCount = quotients.count(_ < threshold.get)
                  val anomalyPercentage = (anomalyCount / quotients.size.toDouble) * 100.00
                  var isAnomaly = anomalyPercentage > tolerance.get
                  val traceReport = new SMTTraceReport(d._1, quotients.size, anomalyCount, isAnomaly, quotients, threshold.get, tolerance.get)
                  report.addTraceReport(traceReport)
                  gui.appendText(traceReport.toString)
                }
              case _ => //TODO - ADD LOGIC FOR OTHER TYPES
            }
          }
      }
      case _ => //Handle other types of wrappers in future extensions here!
    })
    if (report.getTraceReports.isEmpty) None else Some(report)
  }

  private def getIntInput(maxDepth: Int, lines: String): Vector[(Vector[Int], Int)] = {
    var wholeTrace: Vector[Int] = Vector.empty
    val linesArray = lines.split("\\s+")
    if (linesArray.forall(_.matches("[0-9]*"))) wholeTrace = linesArray.map(_.trim.toInt).toVector

    var input: Vector[(Vector[Int], Int)] = Vector[(Vector[Int], Int)]()
    for (t <- wholeTrace.sliding(maxDepth, 1)) {
      if (t.size == maxDepth)
        input = input :+ (t.take(maxDepth - 1), t.takeRight(1)(0))
    }
    input
  }

  private def getStrInput(maxDepth: Int, lines: String): Vector[(Vector[String], String)] = {
    val wholeTrace: Vector[String] = lines.split("\\s+").map(_.trim).toVector
    var input: Vector[(Vector[String], String)] = Vector[(Vector[String], String)]()
    for (t <- wholeTrace.sliding(maxDepth, 1)) {
      if (t.size == maxDepth)
        input = input :+ (t.take(maxDepth - 1), t.takeRight(1)(0))
    }
    input
  }

  private def resetLearn = learnFlag = false

  private def resetClassify = classifyFlag = false

  private def resetValidate = validateFlag = false

  private def resetLoadModel = loadModelFlag = false

  private def resetSaveModel = saveModelFlag = false

  private def resetSaveReport = saveReportFlag = false

  private def setRoot(node: Node[_, _], isInt: Boolean) = {
    root = Some(node)
    isIntRoot = isInt
    gui.render
  }

  //TODO - SETINTROOT WILL BE DELETED OR MADE PRIVATE => TEST FROM UI
  def setIntRoot(int: Boolean): Unit = {
    isIntRoot = int
  }

  def setThreshold(t: Double) = threshold = Some(t)

  def getThreshold: Option[Double] = threshold

  def setTolerance(t: Double) = tolerance = Some(t)

  def getTolerance: Option[Double] = tolerance

  def setLearnFlag: Unit = {
    learnFlag = true
    setChanged
    notifyObservers("learn")
  }

  def setClassifyFlag: Unit = {
    classifyFlag = true
    setChanged
    notifyObservers("classify")
  }

  def setValidateFlag: Unit = {
    validateFlag = true
    setChanged
    notifyObservers("validate")
  }

  def setLoadModelFlag: Unit = {
    loadModelFlag = true
    setChanged
    notifyObservers("loadModel")
  }

  def setSaveModelFlag: Unit = {
    saveModelFlag = true
    setChanged
    notifyObservers("saveModel")
  }

  def setSaveReportFlag: Unit = {
    saveReportFlag = true
    setChanged
    notifyObservers("saveReport")
  }

  def getVisualiser: Option[SMTVisualiser] = if(isTrained){
    Some(new SMTVisualiser(root.get))
  }else None

  def isConfigured: Boolean = root.isDefined && threshold.isDefined && tolerance.isDefined

  def isTrained: Boolean = root match {
    case None => false
    case Some(x: Node[_, _]) => x.getChildren.nonEmpty
  }

  def hasReport: Boolean = lastReport.nonEmpty
}
