package DecisionEngine.SMT

import java.io.File

import Data.{DataException, DataModel, DataWrapper, StringDataWrapper}
import DecisionEngine.{DecisionEngineConfig, DecisionEnginePlugin, DecisionEngineReport}

/**
  * Created by apinter on 08/08/2017.
  */
class SMTPlugin extends DecisionEnginePlugin {

  private var root: Option[Node[_, _]] = None

  override def configure(config: DecisionEngineConfig): Boolean = config match {
    case c: SMTConfig =>
      try
        c.asInstanceOf[SMTConfig].getSettings match {
          case None => false
          case Some(s) => if (s.isIntTrace) {
            setRoot(new Node[Int, Int](s.maxDepth, s.maxPhi, s.maxSeqCount, s.smoothing, s.prior))
            true
          } else {
            setRoot(new Node[String, String](s.maxDepth, s.maxPhi, s.maxSeqCount, s.smoothing, s.prior))
            false
          }
        }
      catch {
        case iae: IllegalArgumentException => false
      }
    case _ => false
  }

  override def learn(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DataModel] = {
    if (data.isEmpty) return model

    model match {
      case None => {
        root match {
          case None => None //No model/SMT to train
          case Some(node) =>
            //TODO - LEARN WITH NODE STORED IN ROOT HERE - delete None
            learnHelper(data, node, ints)
        }
      }
      case Some(w) => {
        //TODO - LEARN WITH NODE PASSED IN HERE - delete None
        w.retrieve match {
          case None => None
          case Some(m) => m match {
            case node: Node[_, _] => learnHelper(data, node, ints)
            case _ => None
          }
        }
      }
    }
  }

  private def learnHelper(data: Vector[DataWrapper], node: Node[_,_], ints: Boolean): Option[DataModel] = {
    data foreach(wrapper => wrapper match {
      case w: StringDataWrapper => w.retrieve match {
        case None =>
        case Some(lines) => {
          if(ints && node.isInstanceOf[Node[Int, Int]] && lines.nonEmpty){
            //process as int trace
            var wholeTrace: Vector[Int] = Vector.empty
            val linesArray = lines.split("\\s")
            println("linesArray.size: " + linesArray.size)
            if(linesArray.forall(_.matches("[0-9]*"))){
              wholeTrace = linesArray.map(_.trim.toInt).toVector
            }else{
              println("lines has a non-numeric call: " + lines)
            }
            println("after check. wholeTrace.size: " + wholeTrace.size)



            var trainingData_whole: Vector[(Vector[Int], Int)] = Vector[(Vector[Int], Int)]()
            for (t <- wholeTrace.sliding(node.maxDepth, 1)) {
              if (t.size == node.maxDepth)
                trainingData_whole = trainingData_whole :+ (t.take(node.maxDepth - 1), t.takeRight(1)(0))
            }

            for (t <- trainingData_whole) {
              node.asInstanceOf[Node[Int, Int]].learn(t._1, t._2)
            }
          }else node match {
            case value: Node[String, String] =>
              //process as string trace
              val wholeTrace: Vector[String] = lines.split("\\s+").map(_.trim).toVector
              var trainingData_whole: Vector[(Vector[String], String)] = Vector[(Vector[String], String)]()
              for (t <- wholeTrace.sliding(node.maxDepth, 1)) {
                if (t.size == node.maxDepth)
                  trainingData_whole = trainingData_whole :+ (t.take(node.maxDepth - 1), t.takeRight(1)(0))
              }

              for (t <- trainingData_whole) {
                value.learn(t._1, t._2)
              }
            case _ =>
          }
        }
      }
      case _ =>
    })
    val dm = new DataModel
    dm.store(node)
    Some(dm)
  }

  override def validate(data: Vector[DataWrapper], model: Option[DataModel]): DecisionEngineReport = ???

  override def classify(data: Vector[DataWrapper], model: Option[DataModel]): DecisionEngineReport = ???

  private def setRoot(node: Node[_, _]) = root = Some(node)

  def loadModel(model: DataModel): Boolean = model.retrieve match {
    case None => false
    case Some(mod) => mod match {
      case m: Node[_, _] => setRoot(m); true
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
}
