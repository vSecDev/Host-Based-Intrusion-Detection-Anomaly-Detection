package DecisionEngine

import java.io.File
import Data.DataModel

trait DecisionEnginePlugin {

  def configure(config: DecisionEngineConfig): Unit
  def learn(_source: List[File], _target: File, _model: DataModel = new DataModel) //_model is optional (if passed, the model is further trained with additional training examples
  def validate(_source: List[File], _target: Option[File], _model: DataModel)
  def classify(_source: List[File], _target: Option[File], _model: DataModel)
}
