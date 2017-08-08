package Data

/**
  * Created by apinter on 08/08/2017.
  */
class DataWrapper [T]{
  var data: Option[T] = None

  def store(_data: T) = data = Some(_data)
  def retrieve(): Option[T] = data
}
