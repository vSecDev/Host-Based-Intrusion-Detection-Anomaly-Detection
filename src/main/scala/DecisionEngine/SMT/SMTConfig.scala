package DecisionEngine.SMT

import DecisionEngine.DecisionEngineConfig

/**
  * Created by apinter on 11/08/2017.
  */
case class SMTConfig() extends DecisionEngineConfig{

  override type T = SMTSettings
  override var settings: Option[SMTSettings] = None

  override def storeSettings(_settings: SMTSettings): Unit = {settings = Some(_settings)}
  override def getSettings(): Option[SMTSettings] = settings
}

class SMTSettings(val maxDepth: Int, val maxPhi: Int, val maxSeqCount: Int, val smoothing: Double, val prior: Double, val isIntTrace: Boolean, val threshold: Double, val tolerance: Double){
  require(maxDepth > 0, "SMTSettings max depth count must be positive!")
  require(maxPhi >= 0, "SMTSettings max Phi count must be non-negative!")
  require(maxSeqCount > 0, "SMTSettings max sequence count must be positive!")
  require(smoothing >= 0, "SMTSettings smoothing value must be non-negative!")
  require(prior > 0, "SMTSettings prior weight must be larger than zero!")
  require(threshold >= 0.0, "SMTSettings threshold must be non-negative!")
  require(tolerance >= 0.0 && tolerance <= 100.0, "SMTSettings tolerance must be between 0-100%!")
}