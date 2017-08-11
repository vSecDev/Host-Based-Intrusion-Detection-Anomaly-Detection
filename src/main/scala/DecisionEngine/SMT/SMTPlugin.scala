package DecisionEngine.SMT

import java.io.File

import Data.{DataException, DataModel, DataWrapper}
import DecisionEngine.{DecisionEngineConfig, DecisionEnginePlugin, DecisionEngineReport}

/**
  * Created by apinter on 08/08/2017.
  */
class SMTPlugin extends DecisionEnginePlugin{

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

  override def learn(_data: Vector[DataWrapper], _model: Option[DataModel]): DataModel = ???

  override def validate(_data: Vector[DataWrapper], _model: Option[DataModel]): DecisionEngineReport = ???

  override def classify(_data: Vector[DataWrapper], _model: Option[DataModel]): DecisionEngineReport = ???

  private def setRoot(node: Node[_, _]) = root = Some(node)
}
