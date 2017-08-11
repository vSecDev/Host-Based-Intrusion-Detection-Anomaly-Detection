package DecisionEngine

import java.io.File

import Data.{DataModel, DataWrapper}

trait DecisionEnginePlugin {

  def configure(config: DecisionEngineConfig): Boolean
  def learn(_data: Vector[DataWrapper], _model: Option[DataModel]): DataModel //_model is optional (if passed, the model is further trained with additional training examples
  def validate(_data: Vector[DataWrapper], _model: Option[DataModel]): DecisionEngineReport
  def classify(_data: Vector[DataWrapper], _model: Option[DataModel]): DecisionEngineReport
}
