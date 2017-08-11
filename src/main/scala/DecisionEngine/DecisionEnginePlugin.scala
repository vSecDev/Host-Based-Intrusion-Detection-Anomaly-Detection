package DecisionEngine

import java.io.File

import Data.{DataModel, DataWrapper}

trait DecisionEnginePlugin {

  def configure(config: DecisionEngineConfig): Boolean
  def learn(data: Vector[DataWrapper], model: Option[DataModel]): Option[DataModel] //model is optional (if passed, the model is further trained with additional training examples
  def validate(data: Vector[DataWrapper], model: Option[DataModel]): DecisionEngineReport
  def classify(data: Vector[DataWrapper], model: Option[DataModel]): DecisionEngineReport
}
