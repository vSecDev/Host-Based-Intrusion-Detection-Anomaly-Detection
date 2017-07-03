package TreeTest

/**
  * Created by Case on 02/07/2017.
  */
abstract class SMT[A](maxDepth: Int, maxPhi: Int)

  case class Node[A](val maxDepth: Int, val maxPhi: Int) extends SMT(maxDepth, maxPhi){

    var key: Option[A] = None
    var children: List[SMT] = Nil
    var predictions: Option[Map[A, Double]] = None

    def getProbability(input: A): Option[Double] = predictions.get(input)
  }
  case class SequenceList[A](list: List[Sequence[A]]) extends SMT(maxDepth=0, maxPhi = 0){}


