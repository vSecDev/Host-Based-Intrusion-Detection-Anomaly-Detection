package DecisionEngine.SMT

import java.io._

import scala.annotation.tailrec
import scala.collection.mutable.Map

//TODO - SORT OUT VISIBILITY + ACCESSORS OF ARGS AND FIELDS
@SerialVersionUID(667L)
abstract class SparseMarkovTree[A,B](maxDepth: Int, maxPhi: Int, maxSeqCount: Int, private val _smoothing: Double, private val _prior: Double) extends Serializable {

  private val id = SparseMarkovTree.inc
  var smoothing: Double
  var prior: Double
  var weight: Double

  def getID = id

  def getSmoothing: Double = smoothing

  def setSmoothing(aSmoothing: Double): Unit = if (smoothing == -1.0) smoothing = aSmoothing else throw new IllegalStateException("SparseMarkovTree smoothing cannot be reset")

  def getPrior: Double = prior

  def setPrior(aPrior: Double): Unit = if (prior == -1.0) prior = aPrior else throw new IllegalStateException("SparseMarkovTree - prior cannot be changed after initialisation!")

  def getWeight: Double = weight

  def setWeight(aWeight: Double): Unit = if (aWeight > 0.0) weight = aWeight else throw new IllegalStateException("SparseMarkovTree weight cannot be negative")
}

object SparseMarkovTree{
  private var id = 0
  private def inc = {id+= 1; id}
}

case class Node[A,B](maxDepth: Int, maxPhi: Int, maxSeqCount: Int, private val _smoothing: Double, private val _prior: Double) extends SparseMarkovTree(maxDepth, maxPhi, maxSeqCount, _smoothing, _prior) {

  var smoothing: Double = -1.0
  var prior: Double = -1.0
  var weight: Double = 1.0

  private var key: Option[A] = None
  private var eventCount = 0
  private var events: Map[B, Int] = Map[B, Int]()
  private var predictions: Map[B, Double] = Map[B, Double]()
  private var isChanged: Boolean = false
  private var children: Vector[Vector[SparseMarkovTree[_ <: A, _ <: B]]] = Vector[Vector[SparseMarkovTree[A, B]]]()

  require(maxDepth > 0, "Node max depth count must be positive!")
  require(maxPhi >= 0, "Node max Phi count must be non-negative!")
  require(maxSeqCount > 0, "Node max sequence count must be positive!")
  require(_smoothing >= 0, "Node smoothing value must be non-negative!")
  require(_prior > 0, "Node prior weight must be larger than zero!")

  setSmoothing(_smoothing)
  //root prior is 1.0
  setPrior(_prior)
  setWeight(prior)

  def getKey: Option[A] = key

  def setKey(aKey: A): Unit = key match {
    case None => key = Option(aKey)
    case _ => throw new IllegalStateException("Node key cannot be reset!")
  }

  def getEventCount: Int = eventCount

  def getEvents: Map[B, Int] = events

  def getPredictions: Map[B, Double] = {
    if (isChanged) {
      updatePredictions
      isChanged = false
    }
    predictions
  }

  def getProbability(input: B): Double = getPredictions.get(input) match {
    case Some(x) => x
    case None => if(eventCount == 0) smoothing else  smoothing / eventCount
  }

  def getChildren: Vector[Vector[SparseMarkovTree[_ <: A, _ <: B]]] = children

  def updateEvents(newEvent: B): Unit = {

    events.get(newEvent) match {
      case Some(event) => events.update(newEvent, events(newEvent) + 1)
      case None => events += (newEvent -> 1)
    }

    eventCount += 1
    isChanged = true
    updateWeight(newEvent)
  }

  def updatePredictions(): Unit = {
    for ((k, v) <- events) {
      if (predictions.contains(k)) predictions.update(k, (v.toDouble + smoothing) / eventCount)
      else predictions += (k -> ((v.toDouble + smoothing) / eventCount))
    }
  }

  //NODECOUNT/VISUALISATION TEST
  def nodeAndLeafCount: (Int, Int) = {
    var nodes = 0
    var leaves = 0

    for (i <- 0 to maxPhi) {
      if (children.size > i) {
        children(i)(0) match {
          case sl: SequenceList[A, B] =>
            leaves += 1
          case _: Node[A, B] =>
            val ns = children(i).asInstanceOf[Vector[Node[A,B]]]
            for (n <- ns) {
              nodes += 1
              val subTreeNodes = n.nodeAndLeafCount
              nodes += subTreeNodes._1
              leaves += subTreeNodes._2
            }
        }
      }
    }
    (nodes, leaves)
  }

  def learn(condition: Vector[A], event: B): Unit = {

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
              System.gc()
            case None =>
          }
        case _: Node[A, B] =>
          val nextNode: Option[Node[A, B]] = children(i).asInstanceOf[Vector[Node[A, B]]].find(x => x.getKey.get == newCondition.head)

          nextNode match {
            case Some(x: Node[A, B]) =>
              newCondition.tail match {
                case y +: ys => x.learn(y +: ys, event)
                case _ => updateEvents(event) //should not ever get here with static window size")
              }
            case None =>
              val newNode: Node[A, B] = Node(maxDepth - i - 1, maxPhi, maxSeqCount, _smoothing, getNewPrior)
              newNode.setKey(newCondition.head)
              newCondition.tail match {
                case y +: ys => newNode.learn(y +: ys, event)
                case _ => updateEvents(event) //should not ever get here with static window size")
              }
              children = children.updated(i, children(i) :+ newNode)
          }
      } else {
        val newSeqList = new SequenceList[A, B](maxDepth - i - 1, maxPhi, maxSeqCount, _smoothing, getNewPrior)

        newSeqList.updateSequences((newCondition, event)) match {
          case Some(x) => children = children :+ x
          case None => children = children :+ Vector(newSeqList)
        }
      }
    } else throw new IllegalStateException("SparseMarkovTree with maxDepth <= 0 has no predictions.")
  }

  def predict(condition: Vector[A], event: B): (Double, Double) = {
    outer(condition, event, getChildren)
  }

  private def updateWeight(newEvent: B): Unit = weight *= getProbability(newEvent)

  private def getNewPrior: Double = if(maxDepth > maxPhi) prior * (1.0 / (maxPhi+1)) else prior * (1.0 / maxDepth)

  //TODO - FIX TAIL RECURSION
  private def inner(condition: Vector[A], event: B, children: Vector[SparseMarkovTree[_ <: A, _ <: B]]): (Double, Double) = {
    @tailrec
    def innerHelper(condition: Vector[A], event: B, children: Vector[SparseMarkovTree[_ <: A, _ <: B]], acc: (Double, Double)): (Double, Double) = {
      children match {
        case x +: xs => x match {
          case sl: SequenceList[A, B] => {
            val res: (Double, Double) = sl.getPredictionWithWeight(condition, event)
            innerHelper(condition, event, xs, (acc._1 + res._1, acc._2 + res._2))
          }

          case _: Node[A, B] => {
            val nextNode: Option[Node[A, B]] = children.asInstanceOf[Vector[Node[A, B]]].find(x => x.getKey.get == condition.head)
            // (x:xs hasNodeWith key = condition.head) match {
            nextNode match {
              case None => acc
              case Some(x: Node[A, B]) => {
                val newCondition = condition.drop(1)
                outer(newCondition, event, x.getChildren)

              }
            }
          }
        }
        case _ => acc
      }
    }

    innerHelper(condition, event, children, (0.0, 0.0))  }

  private def outer(condition: Vector[A], event: B, children: Vector[Vector[SparseMarkovTree[_ <: A, _ <: B]]]): (Double, Double) = {
    @tailrec
    def outerHelper(condition: Vector[A], event: B, children: Vector[Vector[SparseMarkovTree[_ <: A, _ <: B]]], acc: (Double, Double)): (Double, Double) = children match {
      case x +: xs => {
        val sub = inner(condition, event, x)
        outerHelper(condition.drop(1), event, xs, (acc._1 + sub._1, acc._2 + sub._2))
      }
      case _ => acc
    }
    outerHelper(condition, event, children, (0.0, 0.0))
  }

  def toXML(canPrune: Boolean, maxNodeCount: Int): String = {
    val count = nodeAndLeafCount
    val overallCount = count._1 + count._2 //sum of node and leaf count in tree
    val pruned = overallCount > maxNodeCount

    val sb = new StringBuilder
    val elements = xmlOuter(0, Some(this), getChildren, pruned)

    sb ++= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<graphml xmlns=\"http://graphml.smt.org/xmlns\">\n<graph edgedefault=\"undirected\">\n \n<!-- data schema -->\n<key id=\"key\" for=\"node\" attr.name=\"key\" attr.type=\"string\"/>\n<key id=\"type\" for=\"node\" attr.name=\"type\" attr.type=\"string\"/>\n \n<!-- nodes -->\n"
    sb ++= "<node id=\"" + getID + "\">\n <data key=\"key\">Root</data>\n <data key=\"type\">N</data>\n </node>\n"

    for(n <- elements._1){
      val nvs = n.split(",")
      sb ++= "<node id=\"" + nvs(0) + "\">\n <data key=\"key\">" + nvs(1) + "</data>\n <data key=\"type\">" + nvs(2) + "</data>\n </node>\n"
    }

    sb ++= "\n<!-- edges -->\n"

    for(e <- elements._2){
      val evs = e.split(",")
      sb ++= "<edge source=\"" + evs(0) + "\" target=\"" + evs(1) + "\" phi=\"" + evs(2) + "\"></edge>\n"
    }

    sb ++= "</graph>\n</graphml>"
    sb.toString
  }

  private def xmlInner(phi: Int, parent: Option[SparseMarkovTree[_ <: A, _ <: B]], children: Vector[SparseMarkovTree[_ <: A, _ <: B]], pruned: Boolean): (Vector[String], Vector[String]) = {
    @tailrec
    def xmlInnerHelper(phi: Int, parent: Option[SparseMarkovTree[_ <: A, _ <: B]], children: Vector[SparseMarkovTree[_ <: A, _ <: B]], pruned: Boolean, acc: (Vector[String], Vector[String])): (Vector[String], Vector[String]) = {
      children match {
        case x +: xs => x match {
          case sl: SequenceList[A, B] => {
            //if(pruned && sl.sequences.length == 1){
            if(pruned){
              xmlInnerHelper(phi, parent, xs, pruned, (acc._1, acc._2))
            }else {
              val res = (Vector(sl.getID.toString + "," + sl.sequences.length + ",SL"), Vector(parent.get.getID.toString + "," + sl.getID.toString + "," + phi.toString))
              xmlInnerHelper(phi, parent, xs, pruned, (acc._1 ++ res._1, acc._2 ++ res._2))
            }
          }
          case n: Node[A, B] => {
            var sub1: (Vector[String], Vector[String]) = (Vector(), Vector())
            parent match {
              case None =>
                sub1 = (sub1._1 ++ Vector[String](n.getID.toString + ",Root,N"), sub1._2 ++ Vector[String]())
              case Some(p) =>
                sub1 = (sub1._1 ++ Vector[String](n.getID.toString + "," + n.getKey.get.toString + ",N"), sub1._2 ++ Vector[String](p.getID.toString + "," + n.getID.toString + "," + phi.toString))
            }
            val sub2 = xmlOuter(0, Some(n), n.getChildren, pruned)
            val res = (sub1._1 ++ sub2._1, sub1._2 ++ sub2._2)
            xmlInnerHelper(phi, parent, xs, pruned, (acc._1 ++ res._1, acc._2 ++ res._2))
          }
        }
        case _ => acc
      }
    }
    xmlInnerHelper(phi, parent, children, pruned, (Vector(),Vector()))
  }

  private def xmlOuter(phi: Int, parent: Option[SparseMarkovTree[_ <: A, _ <: B]], children: Vector[Vector[SparseMarkovTree[_ <: A, _ <: B]]], pruned: Boolean): (Vector[String], Vector[String]) = {
    @tailrec
    def xmlOuterHelper(phi: Int, parent: Option[SparseMarkovTree[_ <: A, _ <: B]], children: Vector[Vector[SparseMarkovTree[_ <: A, _ <: B]]], pruned: Boolean, acc: (Vector[String], Vector[String])): (Vector[String], Vector[String]) = children match {
      case x +: xs => {
        val sub = xmlInner(phi, parent, x, pruned)
        xmlOuterHelper(phi+1, parent, xs, pruned, (acc._1 ++ sub._1, acc._2 ++ sub._2))
      }
      case _ => acc
    }
    xmlOuterHelper(phi, parent, children, pruned, (Vector[String](), Vector[String]()))
  }

  override def toString: String = {
    val buf = new StringBuilder
    val keyStr = getKey match {
      case None => "No key"
      case Some(x) => if(x.toString.equals(Int.MinValue.toString)) "Root" else x.toString
    }
   // buf ++= "-------\nID: " + getID + "\n Node\nKey: " + getKey.get + "\nmaxDepth: " + maxDepth + " - maxPhi: " + maxPhi + " - maxSeqCount: " + maxSeqCount + "\nChildrenGroup size: " + children.size + "\nChildren:"
    buf ++= "-------\nID: " + getID + "\n Node\nKey: " + keyStr + "\nmaxDepth: " + maxDepth + " - maxPhi: " + maxPhi + " - maxSeqCount: " + maxSeqCount + "\nChildrenGroup size: " + children.size + "\nChildren:"

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

case class SequenceList[A,B](maxDepth: Int, maxPhi: Int, maxSeqCount: Int, _smoothing: Double, _prior: Double) extends SparseMarkovTree(maxDepth, maxPhi, maxSeqCount, _smoothing, _prior) {

  var smoothing: Double = -1.0
  var prior = -1.0
  var weight: Double = 1.0
  var sequences: Vector[Sequence[A, B]] = Vector[Sequence[A, B]]()

  require(maxDepth >= 0, "SequenceList max depth count must be non-negative!")
  require(maxPhi >= 0, "SequenceList max Phi count must be non-negative!")
  require(maxSeqCount > 0, "SequenceList max sequence count must be positive!")
  require(_smoothing >= 0, "SequenceList smoothing value must be non-negative!")
  require(_prior > 0, "SequenceList prior weight must be larger than zero!")

  setSmoothing(_smoothing)
  setPrior(_prior)
  setWeight(prior)

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
      else { sequences = sequences :+ new Sequence[A, B](newSeq._1, newSeq._2, _smoothing, _prior); None }
  }

  def getSequence(key: Vector[A]): Option[Sequence[A, B]] = sequences.find(x => x.getKey == key)

  def getKeys: Vector[Vector[A]] = sequences.map(_.getKey)

  def getPredictionWithWeight(condition: Vector[A], event: B): (Double, Double) = getSequence(condition) match {
    case None => (0.0, 0.0)
    case Some(x) => (x.getWeightedProbability(event), x.getWeight)
  }

  private def canSplit = sequences.size >= maxSeqCount && maxDepth > 0 && getKeys(0).length > 1

  private def split(newSeq: (Vector[A], B)): Vector[Node[ A, B]] = {
    def helper(node: Node[A, B], keyTail: Vector[A], events: Map[B, Int]) = {
      for ((event, count) <- events) {
        for (i <- 1 to count) { node.learn(keyTail, event) }
      }
    }

    var newVector = Vector[Node[A, B]]()

    sequences = sequences :+ new Sequence[A, B](newSeq._1, newSeq._2, _smoothing, _prior)
    for (s <- sequences) {
      newVector.find(p = x => x.getKey.get == s.getKey(0)) match {
        case None =>
          val newNode: Node[A, B] = Node[A, B](maxDepth, maxPhi, maxSeqCount, _smoothing, _prior)
          newNode.setKey(s.getKey(0))
          helper(newNode, s.getKey.tail, s.getEvents)
          newVector = newVector :+ newNode
        case Some(x) =>
          helper(x, s.getKey.tail, s.getEvents)
      }
    }
    newVector
  }
}



