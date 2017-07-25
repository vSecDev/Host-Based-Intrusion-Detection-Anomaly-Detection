package TreeTest

import scala.collection.mutable.Map

/**
  * Created by Case on 02/07/2017.
  * Implementation of Sparse Markov Tree
  * @constructor create new Node with only  maxDepth and maxPhi set
  * @param maxDepth the maximum depth of the tree
  * @param maxPhi the maximum number of wildcards in the tree
  */
abstract class SMT[A,B](maxDepth: Int, maxPhi: Int, maxSeqCount: Int)

case class Node[A,B](maxDepth: Int, maxPhi: Int, maxSeqCount: Int) extends SMT(maxDepth, maxPhi, maxSeqCount) {

  //A root node has no key. Root.getKey = None
  private var key: Option[A] = None
  private var eventCount = 0
  private var events: Map[B, Int] = Map[B, Int]()
  private var predictions: Map[B, Double] = Map[B, Double]()
  private var isChanged: Boolean = false

  require(maxSeqCount > 0, "Max sequence count must be positive!")
  require(maxDepth > 0, "Max depth count must be positive!")
  require(maxPhi >= 0, "Max Phi count must be non-negative!")

  private var children: Vector[Vector[SMT[_ <: A, _ <: B]]] = Vector[Vector[SMT[A, B]]]()

  def getKey: Option[A] = key

  def setKey(aKey: A): Unit = key match {
    case None => key = Option(aKey)
    case _ => throw new IllegalStateException("Node key cannot be reset!")
  }

  def getEventCount: Int = eventCount

  //TODO MAKE THIS PRIVATE OR CONTROL ACCESS
  def getEvents: Map[B, Int] = events

  def getPredictions: Map[B, Double] = {
    if (isChanged) {
      updatePredictions
      isChanged = false
    }
    predictions
  }

  def getProbability(input: B): Option[Double] = getPredictions.get(input)

  def updateEvents(newEvent: B): Unit = {

    //update events to keep count on how many times this input has been seen
    events.get(newEvent) match {
      case None => events += (newEvent -> 1)
      case Some(event) => events.update(newEvent, events(newEvent) + 1)
    }
    //update event count to keep track of number of overall observations
    eventCount += 1
    isChanged = true
  }

  def updatePredictions(): Unit = {
    for ((k, v) <- events) {
      if (predictions.contains(k)) predictions.update(k, v.toDouble / eventCount)
      else predictions += (k -> (v.toDouble / eventCount))
    }
  }

  def getChildren: Vector[Vector[SMT[_ <: A, _ <: B]]] = children

  def growTree(condition: Vector[A], event: B): Unit = {

    if (maxDepth > 0) for {
      i <- 0 to maxPhi
      if condition.length > i && maxDepth - i > 0
    } {
      val newCondition = condition.drop(i)

      if (children.size > i) children(i)(0) match {
        case sl: SequenceList[A, B] =>
          sl.updateSequences((newCondition, event)) match {
            case Some(x) =>
              children = children.updated(i, x)
              //TODO - DELETE CALL TO GC IF NECESSARY
              System.gc()
            case None =>
          }
        case _: Node[A, B] =>
          val nextNode: Option[Node[A, B]] = children(i).asInstanceOf[Vector[Node[A, B]]].find(x => x.getKey.get == newCondition.head)

          nextNode match {
            case Some(x: Node[A, B]) =>
              newCondition.tail match {
                case y +: ys => x.growTree(y +: ys, event)
                case _ => println("should not ever get here with static window size"); updateEvents(event)
              }
            case None =>
              val newNode: Node[A, B] = Node(maxDepth - i - 1, maxPhi, maxSeqCount)
              newNode.setKey(newCondition.head)
              newCondition.tail match {
                case y +: ys => newNode.growTree(y +: ys, event)
                case _ => println("should not ever get here with static window size"); updateEvents(event)
              }
              children = children.updated(i, children(i) :+ newNode)
          }
      } else {
        val newSeqList = new SequenceList[A, B](maxDepth - i - 1, maxPhi, maxSeqCount)

        newSeqList.updateSequences((newCondition, event)) match {
          case Some(x) => children = children :+ x
          case None => children = children :+ Vector(newSeqList)
        }
      }
    } else throw new IllegalStateException("SMT with maxDepth <= 0 has no predictions.")
  }

  override def toString: String = {
    val buf = new StringBuilder
    buf ++= "\n\n-------------------------------\nNode\nKey: " + getKey + "\nmaxDepth: " + maxDepth + " - maxPhi: " + maxPhi + " - maxSeqCount: " + maxSeqCount + " - eventCount: " + getEventCount + " - events size: " + getEvents.size + " - predictions size: " + getPredictions.size + "\nChildrenGroup size: " + children.size + "\nChildren:"
    for (i <- 0 to maxPhi) {
      if (children.size > i) {
        buf ++= "\n-Phi_" + i + ":\nsize: " + children(i).size
        children(i)(0) match {
          case sl: SequenceList[A, B] =>
            buf ++= " - group type: SequenceList. maxDepth: " + sl.maxDepth + " -  Sequences in list:\n"
            val ss = sl.sequences
            for (s <- ss) {
              buf ++= s.toString
            }
          case _: Node[A, B] =>
            buf ++= " - type: Node list. Nodes in list:\n"
            val ns = children(i)
            for (n <- ns) {
              buf ++= n.toString
            }
        }
      }
    }
    buf.toString
  }
}

case class SequenceList[A,B](maxDepth: Int, maxPhi: Int, maxSeqCount: Int) extends SMT(maxDepth, maxPhi, maxSeqCount) {

  var sequences: Vector[Sequence[A, B]] = Vector[Sequence[A, B]]()

  //Constructor arg validation
  require(maxSeqCount > 0, "Max sequence count must be positive!")
  require(maxDepth >= 0, "Max depth count must be non-negative!")
  require(maxPhi >= 0, "Max Phi count must be non-negative!")

  /**
    * Updates the sequence list with a new sequence.
    * The updated sequence list cannot exceed maxSeqCount in size if maxDepth > 0, that is when the SequenceList CAN and SHOULD split.
    *
    * @param newSeq new sequence to add. If newSeq's key is identical to an existing sequence's, that sequence's events and predictions are updated.
    * @return true if the sequence list has been updated, false otherwise.
    */
  def updateSequences(newSeq: (Vector[A], B)): Option[Vector[Node[A, B]]] = sequences.find(x => x.getKey == newSeq._1) match {
    case Some(y) =>
      y.updateEvents(newSeq._2)
      None
    case None =>
      if (canSplit) Some(split(newSeq))
      else { sequences = sequences :+ new Sequence[A, B](newSeq._1, newSeq._2); None }
  }

  def getSequence(key: Vector[A]): Option[Sequence[A, B]] = sequences.find(x => x.getKey == key)

  def getKeys: Vector[Vector[A]] = sequences.map(_.getKey)

  //private def canSplit = sequences.size >= maxSeqCount && maxDepth > 1 && getKeys(0).length > 1
  private def canSplit = sequences.size >= maxSeqCount && maxDepth > 0 && getKeys(0).length > 1

  private def split(newSeq: (Vector[A], B)): Vector[Node[ A, B]] = {

    var newVector = Vector[Node[A, B]]()

    sequences = sequences :+ new Sequence[A, B](newSeq._1, newSeq._2)
    for (s <- sequences) {
      newVector.find(p = x => x.getKey.get == s.getKey(0)) match {
        case None =>
          val newNode: Node[A, B] = Node[A, B](maxDepth, maxPhi, maxSeqCount)
          newNode.setKey(s.getKey(0))
          splitHelper(newNode, s.getKey.tail, s.getEvents)
          newVector = newVector :+ newNode
        case Some(x) =>
          splitHelper(x, s.getKey.tail, s.getEvents)
      }
    }
    newVector
  }

  private def splitHelper(node: Node[A, B], keyTail: Vector[A], events: Map[B, Int]) = {
    for ((event, count) <- events) {
      for (i <- 1 to count) { node.growTree(keyTail, event) }
    }
  }
}



