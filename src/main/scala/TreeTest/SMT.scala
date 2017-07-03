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
    private var events: Option[Map[B, Int]] = None
    private var predictions: Option[Map[B, Double]] = None
    private var children: List[SMT[A,B]] = Nil

    def getKey = key
    def setKey(aKey: A) = key match {
      case Some(x) => throw new IllegalStateException("Node key cannot be reset")
      case None => key = Option(aKey)
    }
    def getEventCount = eventCount
    def updateEvents(newEvent: B) = {

      //update events to keep count on how many times this input has been seen
      events match {
        case None => events = Some(Map[B, Int]()); events.get += (newEvent -> 1)
        case Some(eventsMap) => eventsMap.get(newEvent) match {
          case None => eventsMap += (newEvent -> 1)
          case Some(event) => eventsMap.update(newEvent, eventsMap(newEvent) + 1)
        }
      }
      //update event count to keep track of number of overall observations
      eventCount += 1
      println("checking if eventcount is correct")
      println("eventCount == events.get.foldLeft(0)(_+_._2): " + (eventCount == events.get.foldLeft(0)(_+_._2)))

    /*  //update predictions for the new event
      predictions match {
        None => predictions = Some(Map[B, Double]()); predictions.get += (newEvent -> 1)
      }*/
    }

    def updatePredictions = {}
    def getPredictions = predictions
    def getChildren = children
    def getEvents = events

    def getProbability(input: B): Option[Double] = predictions match {
      case None => None
      case Some(x) => x.get(input)
    }
  }
  case class SequenceList[A,B](list: List[Sequence[A,B]]) extends SMT(maxDepth=0, maxPhi = 0){}


