package DecisionEngine.SMT

import java.io.File

import Data.{DataException, DataModel, DataWrapper}
import DecisionEngine.{DecisionEngineConfig, DecisionEnginePlugin, DecisionEngineReport}

/**
  * Created by apinter on 08/08/2017.
  */
class SMTPlugin extends DecisionEnginePlugin {

  var root: Option[Node[_, _]] = _

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

  override def learn(data: Vector[DataWrapper], model: Option[DataModel]): Option[DataModel] = {
    if (data.isEmpty) return model

    model match {
      case None => {
        root match {
          case None => None
          case Some(m1) =>
            //TODO  - LEARN WITH NODE STORED IN ROOT HERE - delete None
            None
        }
      }
      case Some(m2) => {
        //TODO - LEARN WITH NODE PASSED IN HERE - delete None
        None
      }
    }

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
