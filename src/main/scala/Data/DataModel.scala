package Data

/**
  * Created by apinter on 08/08/2017.
  */
trait DataModel {
  type T
  var model: Option[T]
}
