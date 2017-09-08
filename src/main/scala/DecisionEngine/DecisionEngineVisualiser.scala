package DecisionEngine

import javax.swing.JDialog

trait DecisionEngineVisualiser {

  def getVisualisation: Option[JDialog]
}
