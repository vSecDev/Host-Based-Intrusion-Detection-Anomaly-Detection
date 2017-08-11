package DecisionEngine.SMT

import java.io.File

import Data.DataModel
import DecisionEngine.{DecisionEngineConfig, DecisionEnginePlugin}

/**
  * Created by apinter on 08/08/2017.
  */
class SMTPlugin extends DecisionEnginePlugin{

  //TODO - REF TO TREE + PLUGIN IMPLEMENTATION LOGIC HERE
  var root: Option[Node[_, _]] = _

  override def configure(config: DecisionEngineConfig): Boolean = config match {
    case _: SMTConfig =>
      try
        config.asInstanceOf[SMTConfig].getSettings match {
          case None => false
          case Some(s) => if (s.isIntTrace) {
            setRoot(new Node[Int, Int](s.maxDepth, s.maxPhi, s.maxSeqCount, s.smoothing, s.prior))
            true
          } else {
            //TODO - CREATE NON-INT ROOT HERE
            setRoot(new Node[String, String](s.maxDepth, s.maxPhi, s.maxSeqCount, s.smoothing, s.prior))
            false
          }
        }
      catch {
        case iae: IllegalArgumentException => false
      }
    case _ => false
  }

  override def learn(_source: List[File], _target: File, _model: DataModel): Unit = ???

  override def validate(_source: List[File], _target: Option[File], _model: DataModel): Unit = ???

  override def classify(_source: List[File], _target: Option[File], _model: DataModel): Unit = ???

  private def setRoot(node: Node[_, _]) = root = Some(node)
}
