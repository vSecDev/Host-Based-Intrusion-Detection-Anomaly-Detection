package DecisionEngine.SMT

import java.io.File
import java.util.Observable

import Data.{DataException, DataModel, DataWrapper, StringDataWrapper}
import DecisionEngine.{DecisionEngineConfig, DecisionEngineGUI, DecisionEnginePlugin, DecisionEngineReport}
import GUI.HIDS

/**
  * Created by apinter on 08/08/2017.
  */
//TODO REFACTOR TO AVOID REPETITION OF CODE!
class SMTPlugin(gui: SMTGUI, hids: HIDS) extends Observable with DecisionEnginePlugin {

  override val pluginName: String = "Sparse Markov Tree"
  private var currHIDS: Option[HIDS] = None
  private var root: Option[Node[_, _]] = None
  private var isIntRoot: Boolean = false
  private var threshold: Option[Double] = None
  private var tolerance: Option[Double] = None
  private var learnFlag: Boolean = false


  gui.setPluginInstance(this)
  registerHIDS(hids)

  override def registerHIDS(hids: HIDS): Boolean = {
    currHIDS = Some(hids)

    true
  }

  override def configure(config: DecisionEngineConfig): Boolean = config match {
    case c: SMTConfig =>
      try
        c.getSettings match {
          case None => false
          case Some(s) => if (s.isIntTrace) {
            setRoot(new Node[Int, Int](s.maxDepth, s.maxPhi, s.maxSeqCount, s.smoothing, s.prior), isInt = true)
            setThreshold(s.threshold)
            setTolerance(s.tolerance)
            true
          } else {
            setRoot(new Node[String, String](s.maxDepth, s.maxPhi, s.maxSeqCount, s.smoothing, s.prior), isInt = false)
            setThreshold(s.threshold)
            setTolerance(s.tolerance)
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


  override def update(o: Observable, arg: Any): Unit = {
    super.update(o, arg)
    gui.render
  }

  override def learn(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DataModel] = {
    if (data.isEmpty) return model

    model match {
      case None =>
        root match {
          case None => None //No model/SMT to train
          case Some(node) => learnHelper(data, node, ints)
        }
      case Some(w) =>
        w.retrieve match {
          case None => None
          case Some(m) => m match {
            case node: Node[_, _] => learnHelper(data, node, ints)
            case _ => None
          }
        }
    }
  }

  override def validate(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DecisionEngineReport] = classify(data, model, ints) match {
    case None => None
    case Some(report: SMTReport) => Some(new SMTValidationReport(report))
  }

  override def classify(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DecisionEngineReport] = {
    if (data.isEmpty || threshold.isEmpty || tolerance.isEmpty) return None

    model match {
      case None =>
        root match {
          case None => None //No model/SMT to classify with
          case Some(node) => classifyHelper(data, node, ints)
        }
      case Some(w) =>
        w.retrieve match {
          case None => None
          case Some(m) => m match {
            case node: Node[_, _] => classifyHelper(data, node, ints)
            case _ => None
          }
        }
    }
  }

  private def learnHelper(data: Vector[DataWrapper], node: Node[_, _], ints: Boolean): Option[DataModel] = {
    data foreach (wrapper => wrapper match {
      case w: StringDataWrapper => w.retrieve match {
        case None =>
        case Some(d) =>
          if (d._2.nonEmpty) {
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
    resetLearn
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
                report.addTraceReport(new SMTTraceReport(d._1, quotients.size, anomalyCount, isAnomaly, quotients, threshold.get, tolerance.get))
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
                  report.addTraceReport(new SMTTraceReport(d._1, quotients.size, anomalyCount, isAnomaly, quotients, threshold.get, tolerance.get))
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

  private def setRoot(node: Node[_, _], isInt: Boolean) = {
    root = Some(node)
    isIntRoot = isInt
  }

  def setThreshold(t: Double) = threshold = Some(t)

  def getThreshold: Option[Double] = threshold

  def setTolerance(t: Double) = tolerance = Some(t)

  def getTolerance: Option[Double] = tolerance

  def setLearnFlag: Unit = {
    learnFlag = true
    notifyObservers("learn")
  }

  def resetLearn = learnFlag = false

  def isConfigured: Boolean = root.isDefined && threshold.isDefined && tolerance.isDefined

  def loadModel(model: DataModel, isInt: Boolean): Boolean = model.retrieve match {
    case None => false
    case Some(mod) => mod match {
      case m: Node[_, _] => setRoot(m, isInt); true
      case _ => false
    }
  }

  def getModel(): Option[DataModel] = root match {
    case None => None
    case Some(m) =>
      val dm = new DataModel
      dm.store(m)
      Some(dm)
  }

  def isTrained: Boolean = root match {
    case None => false
    //TODO - CHECK CONDITION!
    case Some(x: Node[_, _]) => x.getChildren.nonEmpty
  }
}
