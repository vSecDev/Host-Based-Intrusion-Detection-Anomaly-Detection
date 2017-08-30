package DecisionEngine

import java.io.File
import java.util.{Observable, Observer}

import Data.{DataModel, DataWrapper}
import GUI.HIDS

//trait DecisionEnginePlugin extends Observable with Observer {
trait DecisionEnginePlugin extends Observer{

  val pluginName: String
  private var srcFile: Option[File] = None
  private var trgtFile: Option[File] = None
  private var srcDir: Option[File] = None
  private var trgtDir: Option[File] = None
  private var loadModelFile: Option[File] = None


  override def update(o: Observable, arg: scala.Any): Unit = {
   arg match {
     case "srcFile" => srcFile = Some(o.asInstanceOf[HIDS].getSrcFile); println("srcFile in DE: " + srcFile.get)
     case "trgtFile" => trgtFile = Some(o.asInstanceOf[HIDS].getTrgtFile); println("trgtFile in DE: " + trgtFile.get)
     case "srcDir" => srcDir = Some(o.asInstanceOf[HIDS].getSrcDir); println("srcDir in DE: " + srcDir.get)
     case "trgtDir" => trgtDir = Some(o.asInstanceOf[HIDS].getTrgtDir); println("trgtDir in DE: " + trgtDir.get)
     case "loadModelFile" => loadModelFile = Some(o.asInstanceOf[HIDS].getLoadModelFile); println("loadModelFile in DE: " + loadModelFile.get)
     case _ => println("couldnt identify arg")
   }
  }

  def configure(config: DecisionEngineConfig): Boolean
  def getConfiguration: Option[DecisionEngineConfig]
  def getGUI: Option[DecisionEngineGUI]
  def learn(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DataModel] //model is optional (if passed, the model is further trained with additional training examples
  def validate(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DecisionEngineReport]
  def classify(data: Vector[DataWrapper], model: Option[DataModel], ints: Boolean): Option[DecisionEngineReport]

}
