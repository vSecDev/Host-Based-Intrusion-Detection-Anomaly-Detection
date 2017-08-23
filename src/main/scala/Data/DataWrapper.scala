package Data

/**
  * Created by apinter on 08/08/2017.
  */
trait DataWrapper {
  type T
  var data: Option[T]

  def store(_data: T): Unit
  def retrieve(): Option[T]
}

case class StringDataWrapper() extends DataWrapper {
  override type T = (String, String)
  override var data: Option[(String, String)] = None

  override def store(_data: (String, String)): Unit = {data = Some(_data)}
  override def retrieve(): Option[(String, String)] = data
}

