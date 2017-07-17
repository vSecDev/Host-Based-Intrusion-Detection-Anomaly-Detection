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

  case class Node[A,B](maxDepth: Int, maxPhi: Int, maxSeqCount: Int) extends SMT(maxDepth, maxPhi, maxSeqCount){

    private var key: Option[A] = None
    private var eventCount = 0
    private var events: Map[B, Int] = Map[B, Int]()
    private var predictions: Map[B, Double] = Map[B, Double]()

    //TODO ADD CONSTRUCTOR ARG CHECKING LOGIC. NO NEGATIVE MAXDEPTH,MAXPHI/MAXSEQCOUNT


    //TODO CHECK TYPE OF THIS VARIABLE. FIND OPTIMAL TYPE.
    private var children: Vector[Vector[SMT[A,B]]] = Vector[Vector[SMT[A,B]]]()

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

      if (eventCount > 0){

        for ((k,v) <- events){
          if(predictions.contains(k))
            predictions.update(k, v.toDouble / eventCount)
          else
            predictions += (k -> (1.00/eventCount))
        }
      }
    }
    def getPredictions: Map[B, Double] = predictions
    def getChildren: Vector[Vector[SMT[A,B]]] = children
    def getEvents: Map[B, Int] = events

    def getProbability(input: B): Option[Double] = predictions.get(input)

    //TODO GROWTREE
    //TODO SEQUENCELIST WITHIN SMTS WILL SPLIT WHEN MAXSEQCOUNT WOULD BE EXCEEDED AS A RESULT ADDING A SEQUENCE WITH A NEW KEY
    //TODO SO MAKE SURE THE SEQUENCE THAT COULD NOT BE ADDED (BECAUSE UPDATESEQUENCES RETURNED FALSE) IS ADDED AFTER THE SPLIT!!!!!
    def growTree(sequence: Vector[A], event: B): Unit = {
      if(maxDepth > 0){
        for{
          i <- 0 to maxPhi
          if sequence.length > i
        }{

        }
      }else{

      }
    }
  }

  case class SequenceList[A,B](maxDepth: Int, maxPhi: Int, maxSeqCount: Int) extends SMT(maxDepth, maxPhi, maxSeqCount){

    var sequences: Vector[Sequence[A,B]] = Vector[Sequence[A,B]]()

    /*private var keys: List[A] = Nil
    private var eventCount = 0
    private var events: Map[B, Int] = Map[B, Int]()
    private var predictions: Map[B, Double] = Map[B, Double]()
    private var children: List[List[SMT[A,B]]] = Nil*/

    //Constructor arg validation
    require(maxSeqCount > 0, "Max sequence count must be positive!")


    //TODO : updateSequenceList MUST CHECK IF SEQUENCELIST HAS TO SPLIT, NEW SEQUENCE HAS UNIQUE KEY
    /**
      * Updates the sequence list with a new sequence.
      * The updated sequence list cannot exceed maxSeqCount in size.
      * @param newSeq new sequence to add. If newSeq's key is identical to an existing sequence's, that sequence's events and predictions are updated.
      * @return true if the sequence list has been updated, false otherwise.
      */
    def updateSequences(newSeq: (Vector[A], B)): Boolean = sequences.find(x => x.getKey == newSeq._1) match {
      case Some(x) => { println("SequenceList.updateSequences: found Sequence with key = newSeq.key. Number of Sequences with this key (should be 1): " + sequences.count(y => y.getKey == newSeq._1)); x.updateEvents(newSeq._2); println("After update. Number of Sequences with the same key as newSeq (should still be 1): " + + sequences.count(y => y.getKey == newSeq._1)); true }
      case None => {
        if(sequences.size == maxSeqCount){
          println("sequences.size == maxSeqCount. -> " + sequences.size + " . Reached split size. Sequence not added! Make sure it's added after split!")
          false
        }else{
          println("sequences.size != maxSeqCount. Sequences.size (should be smaller than maxSeqCount): " + sequences.size + " - maxSeqCount: " + maxSeqCount + " Adding new sequence to sequences.")
          sequences = sequences :+ new Sequence[A, B](newSeq._1, newSeq._2)
          println("Sequences.size after adding newSeq: " + sequences.size + " - maxSeqCount: " + maxSeqCount)
          true
        }
      }
    }

    def getSequence(key: Vector[A]): Option[Sequence[A,B]] = sequences.find(x => x.getKey == key)
    def getKeys: Vector[Vector[A]] = sequences.map(_.getKey)

    //TODO SEQUENCELIST WITHIN SMTS WILL SPLIT WHEN MAXSEQCOUNT WOULD BE EXCEEDED AS A RESULT ADDING A SEQUENCE WITH A NEW KEY
    //TODO SO MAKE SURE THE SEQUENCE THAT COULD NOT BE ADDED (BECAUSE UPDATESEQUENCES RETURNED FALSE) IS ADDED AFTER THE SPLIT!!!!!
    def split(): Vector[Node[A, B]] = {

      var newVector: Vector[Node[A, B]] = Vector[Node[A, B]]()

      for (s <- sequences){

        newVector.find(x => x.getKey == s.getKey(0)) match {
          case None => {
            var newNode: Node[A,B] = Node[A,B](maxDepth, maxPhi, maxSeqCount)
            newNode.setKey(s.getKey(0))
            splitHelper(newNode, s.getKey.tail, s.getEvents)
            newVector = newVector :+ newNode
          }
          case Some(x) => splitHelper(x, s.getKey.tail, s.getEvents)
        }
      }
      newVector
    }

    def splitHelper(node: Node[A, B], keyTail: Vector[A], events: Map[B, Int]) = {
      for((event, count) <- events){
        for(i <- 1 to count){
          node.growTree(keyTail, event)
        }
      }
    }





    /*foreach sequence in SequenceList {
      take head :: tail and prediction/count
        check if newList contains Node with key = head  ///newList.exists { x => customPredicate(x) }   ====> customPredicate could be x.getKey == head
      if(newList.exists { x => x.getKey == head }){
        //newList contains a Node with key = head.
        nodeToAdd = that node in newList
        f  nodeToAdd . call growTree with tail and predictions  //two method for this: 1. call growTree with same input and once for each instance of each event, OR 2. create a setEvents function that allows updating events + eventCount with multiple events once.
      }else{
        //newList does not contain a Node with key = head yet. A new Node needs to be created and added to newList
        create Node (nodeToAdd) with key = head, maxDepth = previous maxDepth-1, maxPhi = previous maxPhi
          nodeToAdd . call growTree with tail and predictions  //two method for this: 1. call growTree with same input and once for each instance of each event, OR 2. create a setEvents function that allows updating events + eventCount with multiple events once.

        add Node to newList
      }
    }
    replace split Sequence with newList (in "childrenGroup")*/




  }



