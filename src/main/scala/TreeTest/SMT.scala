package TreeTest

/**
  * Created by Case on 02/07/2017.
  * Implementation of Sparse Markov Tree
  * @constructor create new Node with only  maxDepth and maxPhi set
  * @param maxDepth the maximum depth of the tree
  * @param maxPhi the maximum number of wildcards in the tree
  */
abstract class SMT[A,B](maxDepth: Int, maxPhi: Int)

  case class Node[A,B](val maxDepth: Int, val maxPhi: Int) extends SMT(maxDepth, maxPhi){

    private var key: Option[A] = None
    private var eventCount = 0
    private var events: Option[Map[B, Int]] = None
    private var predictions: Option[Map[B, Double]] = None
    private var children: List[SMT[A,B]] = Nil

    def getProbability(input: B): Option[Double] = predictions match {
      case None => None
      case Some(x) => x.get(input)
    }
    def getKey = key
    def setKey(aKey: A) = key match {
      case Some(x) => throw new IllegalStateException("Node key cannot be reset")
      case None => key = Option(aKey)
    }
    def getEventCount = eventCount
   // def updateEvents()
    def getChildren = children
    def getEventCounts = events
    def getPredictions = predictions
  }
  case class SequenceList[A,B](list: List[Sequence[A,B]]) extends SMT(maxDepth=0, maxPhi = 0){}


