package TreeTest

import scala.collection.mutable.Map
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
    private var events: Map[B, Int] = Map[B, Int]()
    private var predictions: Map[B, Double] = Map[B, Double]()
    private var children: List[List[SMT[A,B]]] = Nil

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
    def getChildren: List[SMT[A,B]] = children
    def getEvents: Map[B, Int] = events

    def getProbability(input: B): Option[Double] = predictions.get(input)
  }


  //TODO - DO THIS CLASS!
  case class SequenceList[A,B](list: List[Sequence[A,B]]) extends SMT(maxDepth=0, maxPhi = 0){
    /*private var keys: List[A] = Nil
    private var eventCount = 0
    private var events: Map[B, Int] = Map[B, Int]()
    private var predictions: Map[B, Double] = Map[B, Double]()
    private var children: List[List[SMT[A,B]]] = Nil*/
  }


