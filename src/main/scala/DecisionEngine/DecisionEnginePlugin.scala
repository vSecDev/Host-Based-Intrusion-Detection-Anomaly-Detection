package DecisionEngine

import java.io.File
import java.util.{Observable, Observer}

import Data.{DataModel, DataWrapper}

trait DecisionEnginePlugin extends Observable with Observer {

  val pluginName: String

  def configure(config: DecisionEngineConfig): Boolean
  def getConfiguration: Option[DecisionEngineConfig]
  def getGUI: Option[DecisionEngineGUI]
  def learn(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DataModel] //model is optional (if passed, the model is further trained with additional training examples
  def validate(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DecisionEngineReport]
  def classify(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DecisionEngineReport]

}
