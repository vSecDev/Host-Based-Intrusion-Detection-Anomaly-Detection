package DecisionEngine

import javax.swing.{JDialog, JPanel}

trait DecisionEngineVisualiser {

  def getVisualisation: Option[JDialog]
 // def getVisualisation: Option[JPanel]
}
