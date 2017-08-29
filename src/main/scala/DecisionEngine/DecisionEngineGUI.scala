package DecisionEngine

import javax.swing.JPanel

trait DecisionEngineGUI {
  type T <: DecisionEnginePlugin

  var pluginInstance: Option[T]

  def getGUIComponent: Option[JPanel]
  def setPluginInstance(plugin: T): Unit = this.pluginInstance = Some(plugin)

}
