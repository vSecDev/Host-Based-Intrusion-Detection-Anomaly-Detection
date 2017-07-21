package TreeTest

//import scala.collection.mutable
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

    private var childrenGroup: Vector[Vector[SMT[_ <: A, _ <: B]]] = Vector[Vector[SMT[A, B]]]()

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

    def getChildren: Vector[Vector[SMT[_ <: A, _ <: B]]] = childrenGroup

    def growTree(condition: Vector[A], event: B): Unit = {
      println("\n\n-------\ncondition: " + condition)
      if (maxDepth > 0) for {
        i <- 0 to maxPhi
        if condition.length > i
      } {
        val newCondition = condition.drop(i)
        println("------------------  i: " + i)
        println("newCondition: " + newCondition)

        if (childrenGroup.size > i) {
          println("childrenGroup.size > i (i=" + i + ") => true");
          childrenGroup(i)(0) match {
          case sl: SequenceList[A, B] =>{
            println("childrenGroup(" + i + ")(0) = SequenceList")
            sl.updateSequences((newCondition, event)) match {
              case Some(x) => println("??? Some, i: " + i + " - newCondition: " + newCondition);childrenGroup = childrenGroup.updated(i, x)
              case None => println("??? None, i: " + i + " - newCondition: " + newCondition)
            }
          }
          case _: Node[A, B] =>{
            println("childrenGroup(" + i + ")(0) = Node")

            val nextNode: Option[Node[A, B]] = childrenGroup(i).asInstanceOf[Vector[Node[A, B]]].find(x => x.getKey == newCondition.head)

            nextNode match{
              case Some(x: Node[A, B]) => println("found node with key: " + x.getKey)
              case _ => println("did not find node with key: " + newCondition.head)
            }


            nextNode match {
              case Some(x: Node[A, B]) =>
                newCondition.tail match {
                  case y +: ys => x.growTree(y +: ys, event)
                  case _ => println("should not ever get here with static window size"); updateEvents(event)
                }
              case None =>
                val newNode: Node[A, B] = new Node(maxDepth - i - 1, maxPhi, maxSeqCount)
                newNode.setKey(newCondition.head)

                newCondition.tail match {
                  case y +: ys => newNode.growTree(y +: ys, event)
                  case _ => println("should not ever get here with static window size"); updateEvents(event)
                }
                childrenGroup(i) :+ newNode
            }
        }}

          println("\n\n\n-childrenGroup after update: " + childrenGroup)
        } else {
          println("childrenGroup.size > i (i=" + i + ") => false ==> Creating a new SequenceList under this node.")
          println("i: " + i + " - newCondition: " + newCondition)

          val newSeqList = new SequenceList[A, B](maxDepth - i - 1, maxPhi, maxSeqCount)

          newSeqList.updateSequences((newCondition, event)) match {
            case Some(x) => println("newSeqList split. Should never get here."); childrenGroup = childrenGroup :+ x
            case None => childrenGroup = childrenGroup :+ Vector(newSeqList)
          }
        }
      } else throw new IllegalStateException("SMT with maxDepth <= 0 has no predictions.")
    }

    override def toString: String = {
      val buf = new StringBuilder
      buf ++= "\n\n-------------------------------\nNode\nKey: " + getKey + "\nmaxDepth: " + maxDepth + " - maxPhi: " + maxPhi + " - maxSeqCount: " + maxSeqCount + " - eventCount: " + getEventCount + " - events size: " + getEvents.size + " - predictions size: " + getPredictions.size + "\nChildrenGroup size: " + childrenGroup.size + "\nChildren:"
      for (i <- 0 to maxPhi) {
        if (childrenGroup.nonEmpty) {
          buf ++= "\n-Phi_" + i + ":\nsize: " + childrenGroup(i).size


          childrenGroup(i)(0) match {
            case sl: SequenceList[A, B] =>
              buf ++= " - group type: SequenceList. maxDepth: " + sl.maxDepth + " -  Sequences in list:\n"
              val ss = sl.sequences
              for (s <- ss) {
                buf ++= s.toString
              }
            case _: Node[A, B] => buf ++= " - type: Node list. Nodes in list:\n"
              val ns = childrenGroup(i)
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
    //def updateSequences(newSeq: (Vector[A], B)): Option[Vector[SMT[_ <: A, _ <: B]]] ={
    def updateSequences(newSeq: (Vector[A], B)): Option[Vector[Node[A, B]]] ={
      println("updateSequences seq: " + newSeq)

      sequences.find(x => x.getKey == newSeq._1) match {
      case Some(y) =>{
        println("Found stored seq with key: " + y.asInstanceOf[Sequence[A,B]].getKey)
        y.updateEvents(newSeq._2)
        None
      }
      case None =>
        if (canSplit){ println("canSplit is: " + canSplit); val sp = Some(split(newSeq)); println("after split returning: " + sp.get.size); sp}
        else {
          println("canSplit: " + canSplit)
          println("adding seq with key: " + newSeq._1 + " to current sequencelist")
          sequences = sequences :+ new Sequence[A, B](newSeq._1, newSeq._2)
          println("added seq with key: " + newSeq._1 + " to current sequencelist")
          None
        }
    }}

    def getSequence(key: Vector[A]): Option[Sequence[A, B]] = sequences.find(x => x.getKey == key)

    def getKeys: Vector[Vector[A]] = sequences.map(_.getKey)

    private def canSplit = sequences.size >= maxSeqCount && maxDepth > 1 && getKeys(0).length > 1

    //private def split(newSeq: (Vector[A], B)): Vector[SMT[_ <: A, _ <: B]] = {
    private def split(newSeq: (Vector[A], B)): Vector[Node[ A, B]] = {
      //var newVector: Vector[SMT[_ <: A, _ <: B]] = Vector[SMT[A, B]]()
      var newVector = Vector[Node[A, B]]()
      println("newVector is emtpy after declaration: " + newVector.isEmpty)

      sequences = sequences :+ new Sequence[A, B](newSeq._1, newSeq._2)

      for (s <- sequences) {
        println("\nadding sequence " + s.getKey + " to new nodeVector")
        newVector.find(p = x => x.getKey.get == s.getKey(0)) match {
          case None => {
            println("Didn't find node with head " + s.getKey(0) + " . Creating a new one")
            var newNode: Node[A, B] = Node[A, B](maxDepth, maxPhi, maxSeqCount)
            newNode.setKey(s.getKey(0))
            splitHelper(newNode, s.getKey.tail, s.getEvents)
            newVector = newVector :+ newNode
          }
          case Some(x) => { println("Found node with key: " + x.getKey + " . Updating this node.") ; splitHelper(x, s.getKey.tail, s.getEvents)}
        }
      }
      println("created new vector with split: " + newVector.size + "  -------------------------")
      /*println("\nnewVector after adding new node with key: " + newNode.getKey)
      println(newVector.size)
      println("------------- nodes in newVector: ")
      for(nx <- newVector){
        println("node key: " + nx.getKey)
      }*/


      newVector
    }

    private def splitHelper(node: Node[A, B], keyTail: Vector[A], events: Map[B, Int]) = {
      println("In splitHelper. Updating node with key " + node.getKey)
      for ((event, count) <- events) {
        println("Updating node with event " + event)
        for (i <- 1 to count) { println("calling growTree on node with keyTail: " + keyTail + " and event: " + event ) ; node.growTree(keyTail, event)}
      }
    }
  }



