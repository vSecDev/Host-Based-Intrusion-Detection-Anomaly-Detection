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

    //TODO ADD CONSTRUCTOR ARG CHECKING LOGIC. NO NEGATIVE MAXDEPTH,MAXPHI/MAXSEQCOUNT


    //TODO CHECK TYPE OF THIS VARIABLE. FIND OPTIMAL TYPE.
    private var childrenGroup: Vector[Vector[SMT[_ <: A, _ <: B]]] = Vector[Vector[SMT[A, B]]]()

    def getKey: Option[A] = key

    def setKey(aKey: A): Unit = key match {
      case None => key = Option(aKey)
      case _ => throw new IllegalStateException("Node key cannot be reset")
    }

    def getEventCount: Int = eventCount

    def updateEvents(newEvent: B): Unit = {

      //update events to keep count on how many times this input has been seen
      events.get(newEvent) match {
        case None => events += (newEvent -> 1)
        case Some(event) => events.update(newEvent, events(newEvent) + 1)
      }

      //update event count to keep track of number of overall observations
      eventCount += 1
      updatePredictions
    }

    def updatePredictions(): Unit = {

      if (eventCount > 0) {

        for ((k, v) <- events) {
          if (predictions.contains(k))
            predictions.update(k, v.toDouble / eventCount)
          else
            predictions += (k -> (1.00 / eventCount))
        }
      }
    }

    def getPredictions: Map[B, Double] = predictions

    def getChildren: Vector[Vector[SMT[_ <: A, _ <: B]]] = childrenGroup

    def getEvents: Map[B, Int] = events

    def getProbability(input: B): Option[Double] = predictions.get(input)

    //TODO GROWTREE

    def growTree(condition: Vector[A], event: B): Unit = {
      println("Inside growTree. Current node key: " + getKey.get)
      if (maxDepth > 0) for {
        i <- 0 to maxPhi
        if condition.length > i
      } {
        val newCondition = condition.drop(i)
        println("growTree. newCondition is: " + newCondition)

        if (childrenGroup.size > i) childrenGroup(i)(0) match {
          case sl: SequenceList[A,B] =>
            sl.updateSequences((newCondition, event)) match {
              case Some(x) => childrenGroup.updated(i, x) // TODO CHECK IF UPDATESEQUENCES, REMOVES THE HEAD/CORRECT NUMBER OF ELEMENTS FROM CONDITION
              case None =>
            }
          case _: Node[A,B] =>
            val nextNode: Option[Node[A, B]] = childrenGroup(i).asInstanceOf[Vector[Node[A, B]]].find(x => x.getKey == newCondition.head)
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
        } else {
          val newSeqList = new SequenceList[A, B](maxDepth - i - 1, maxPhi, maxSeqCount)
          newSeqList.updateSequences((newCondition, event))
          childrenGroup.updated(i, Vector[SMT[_ <: A, _ <: B]](newSeqList))
        }
      } else {
        //  (!maxDepth > 0)   //maxDepth is 0! here
        println("maxDepth <= 0. We should never get here if maxDepth of the root is larger than 0.")
        throw new IllegalStateException("SMT with maxDepth <= 0 has no predictions.")
      }
    }
  }

  case class SequenceList[A,B](maxDepth: Int, maxPhi: Int, maxSeqCount: Int) extends SMT(maxDepth, maxPhi, maxSeqCount) {

    var sequences: Vector[Sequence[A, B]] = Vector[Sequence[A, B]]()

    /*private var keys: List[A] = Nil
    private var eventCount = 0
    private var events: Map[B, Int] = Map[B, Int]()
    private var predictions: Map[B, Double] = Map[B, Double]()
    private var children: List[List[SMT[A,B]]] = Nil*/

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
    def updateSequences(newSeq: (Vector[A], B)): Option[Vector[Node[A, B]]] = {
      println("in updateSequences. newSeq._1: " + newSeq._1)
      sequences.find(x => x.getKey == newSeq._1) match {
        case Some(x) =>
          x.updateEvents(newSeq._2)

          //TODO DELETE NEXT TWO LINES
          println("------------------\nAfter SequenceList updateSequences update. Stored sequences: ")
          for (s <- sequences) println(s)

          None
        case None => if (canSplit) Some(split(newSeq))
        else {
          sequences = sequences :+ new Sequence[A, B](newSeq._1, newSeq._2)

          //TODO DELETE NEXT TWO LINES
          println("------------------\nAfter SequenceList updateSequences update. Stored sequences: ")
          for (s <- sequences) println(s)

          None
        }
      }
    }

    def getSequence(key: Vector[A]): Option[Sequence[A, B]] = sequences.find(x => x.getKey == key)

    def getKeys: Vector[Vector[A]] = sequences.map(_.getKey)

    private def canSplit = sequences.size >= maxSeqCount && maxDepth > 0 && getKeys(0).length > 1

    //TODO SEQUENCELIST WITHIN SMTS WILL SPLIT WHEN MAXSEQCOUNT WOULD BE EXCEEDED AS A RESULT ADDING A SEQUENCE WITH A NEW KEY
    //TODO SO MAKE SURE THE SEQUENCE THAT COULD NOT BE ADDED (BECAUSE UPDATESEQUENCES RETURNED FALSE) IS ADDED AFTER THE SPLIT!!!!!
    private def split(newSeq: (Vector[A], B)): Vector[Node[A, B]] = {
      println("\nin split. newSeq._1 : " + newSeq._1)

      var newVector: Vector[Node[A, B]] = Vector[Node[A, B]]()
      sequences = sequences :+ new Sequence[A, B](newSeq._1, newSeq._2)

      println("in split. added the extra sequence to sequences before splitting. currently stored sequences:")
      for(s <- sequences) println(s)


      println("")
      for (s <- sequences) {
        println("in split for loop. sequence s.getKey: " + s.getKey)

        newVector.find(x => x.getKey == s.getKey(0)) match {
          case None => {
            var newNode: Node[A, B] = Node[A, B](maxDepth, maxPhi, maxSeqCount)
            println("Creating new node with key: " + s.getKey(0))
            newNode.setKey(s.getKey(0))
            splitHelper(newNode, s.getKey.tail, s.getEvents)
            newVector = newVector :+ newNode
          }
          case Some(x) => splitHelper(x, s.getKey.tail, s.getEvents)
        }
      }

      println("------------------------------------newVector returned by split.: " + newVector)
      println("newVector size: " + newVector.size)
      newVector
    }

    private def splitHelper(node: Node[A, B], keyTail: Vector[A], events: Map[B, Int]) = {
      println("in splitHelper. keyTail: " + keyTail)
      for ((event, count) <- events) {
        for (i <- 1 to count) {
          println("calling growTree with (keyTail, event) = " + (keyTail, event))
          node.growTree(keyTail, event)
        }
      }
    }
  }



