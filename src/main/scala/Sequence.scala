package TreeTest

/**
  * Created by Case on 02/07/2017.
  */
class Sequence (val sequence: List[Int], var predictions: Map[Int, Double]){

  override def toString: String = "\n--------\nSequence:\nsequence:\n" + sequence + "\n-predictions:\n" + predictions + "\n-------"

}


/* //With String
class Sequence (val sequence: String, var predictions: Map[String, Double]){

}*/
