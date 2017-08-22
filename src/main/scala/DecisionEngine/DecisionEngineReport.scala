package DecisionEngine

/**
  * Created by apinter on 11/08/2017.
  */
trait DecisionEngineReport {
  type T
  var data: Option[T]

  def store(_data: T): Unit
  def retrieve(): Option[T]
}
