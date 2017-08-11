package DecisionEngine

/**
  * Created by apinter on 11/08/2017.
  */
trait DecisionEngineConfig {
  type T
  var settings: Option[T]

  def storeSettings(_settings: T): Unit
  def getSettings(): Option[T]
}

