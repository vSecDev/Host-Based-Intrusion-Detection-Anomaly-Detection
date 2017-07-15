package TreeTest

//import scala.collection.mutable
import scala.collection.mutable.Map
import scala.collection.mutable.Set

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


    //TODO SHOULD BELOW BE A List[Set[SMT[A,B]]] ???
    private var children: Vector[Set[SMT[A,B]]] = Vector[Set[SMT[A,B]]]()

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

    def updatePredictions: Unit = {

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
    def getChildren: Vector[Set[SMT[A,B]]] = children
    def getEvents: Map[B, Int] = events

    def getProbability(input: B): Option[Double] = predictions.get(input)

    //TODO GROWTREE
  //  def growTree()
  }

  case class SequenceList[A,B](maxSeqCount: Int) extends SMT(maxDepth=0, maxPhi = 0, maxSeqCount){
    //TODO - DO THIS CLASS! +++++

    var sequences: Set[Sequence[A,B]] = Set[Sequence[A,B]]()

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
          sequences.add(new Sequence[A, B](newSeq._1, newSeq._2))
          println("Sequences.size after adding newSeq: " + sequences.size + " - maxSeqCount: " + maxSeqCount)
          true
        }
      }
    }




    /*sequences.find(x => x.getKey == newSeq.getKey) match {
      case Some(x) => x.updateEvents(newSeq.getE)

     /* if(sequences.exists { x => x.getKey == newSeq.getKey}){

      }else if{
        /*if(sequences.size == maxSeqCount)  //only if new sequence would increase sequences.size
          false*/
      }else{

      }*/
    }*/

  }


