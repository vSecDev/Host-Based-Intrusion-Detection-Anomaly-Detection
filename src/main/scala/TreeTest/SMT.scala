package TreeTest

/**
  * Created by Case on 02/07/2017.
  */
abstract class SMT[A](maxDepth: Int, maxPhi: Int)

  case class Node[A](val maxDepth: Int, val maxPhi: Int) extends SMT(maxDepth, maxPhi){

    private var key: Option[A] = None
    private var children: List[SMT[A]] = Nil
    private var predictions: Option[Map[A, Double]] = None

    def getProbability(input: A): Option[Double] = predictions match {
      case None => None
      case Some(x) => x.get(input)
    }

    def getKey = key
    def setKey(aKey: A) = key match {
      case Some(x) => throw new IllegalStateException("Node key cannot be reset")
      case None => key = Option(aKey)
    }

    def getChildren = children
    def getPredictions = predictions
  }
  case class SequenceList[A](list: List[Sequence[A]]) extends SMT(maxDepth=0, maxPhi = 0){}


