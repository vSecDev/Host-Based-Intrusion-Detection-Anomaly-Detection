package TreeTest

/**
  * Created by Case on 02/07/2017.
  */
class Sequence[A,B] (val sequence: List[A], var predictions: Map[B, Double]){

  def getProbability(input: B): Option[Double] = predictions.get(input)
  override def toString: String = "\n--------\nSequence\n-sequence:\n    " + sequence + "\n-predictions:\n    " + predictions + "\n-------"
}


/*
//Non-generic -> Int
class Sequence (val sequence: List[Int], var predictions: Map[Int, Double]){

  override def toString: String = "\n--------\nSequence\n-sequence:\n    " + sequence + "\n-predictions:\n    " + predictions + "\n-------"

  def getProbability(input:Int): Option[Double] = predictions.get(input)
}*/

