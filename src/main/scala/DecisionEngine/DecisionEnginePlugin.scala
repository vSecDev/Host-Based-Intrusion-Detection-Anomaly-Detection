package DecisionEngine

import java.io.File

import Data.{DataModel, DataWrapper}

trait DecisionEnginePlugin {

  val pluginName: String

  def configure(config: DecisionEngineConfig): Boolean
  def learn(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DataModel] //model is optional (if passed, the model is further trained with additional training examples
  def validate(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DecisionEngineReport]
  def classify(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DecisionEngineReport]
}
