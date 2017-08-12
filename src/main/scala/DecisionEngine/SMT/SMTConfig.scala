package DecisionEngine.SMT

import DecisionEngine.DecisionEngineConfig

/**
  * Created by apinter on 11/08/2017.
  */
case class SMTConfig() extends DecisionEngineConfig{

  override type T = SMTSettings
  override var settings: Option[SMTSettings] = _

  override def storeSettings(_settings: SMTSettings): Unit = {settings = Some(_settings)}
  override def getSettings(): Option[SMTSettings] = settings
}

class SMTSettings(val maxDepth: Int, val maxPhi: Int, val maxSeqCount: Int, val smoothing: Double, val prior: Double, val isIntTrace: Boolean){}