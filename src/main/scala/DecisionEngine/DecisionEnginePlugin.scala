package DecisionEngine

import java.io.File
import java.util.{Observable, Observer}

import Data.{DataModel, DataWrapper}
import GUI.HIDS

//trait DecisionEnginePlugin extends Observable with Observer {
trait DecisionEnginePlugin extends Observer{

  val pluginName: String
  //TODO - CHANGE ACCESS MODIFIERS!
  var source: Option[File] = None
  var target: Option[File] = None

  override def update(o: Observable, arg: scala.Any): Unit = {
   arg match {
     case "source" => source = Some(o.asInstanceOf[HIDS].getSource)
     case "target" => target = Some(o.asInstanceOf[HIDS].getTarget)
     case _ =>
   }
  }

  def registerHIDS(hids: HIDS): Boolean
  def configure(config: DecisionEngineConfig): Boolean
  def getConfiguration: Option[DecisionEngineConfig]
  def getGUI: Option[DecisionEngineGUI]
  def isIntModel: Boolean
  def learn(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DataModel] //model is optional (if passed, the model is further trained with additional training examples
  def validate(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DecisionEngineReport]
  def classify(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DecisionEngineReport]
  def loadModel(model: DataModel, isInt: Boolean): Boolean
  def getModel: Option[DataModel]
  def getModelName: Option[String]
}
