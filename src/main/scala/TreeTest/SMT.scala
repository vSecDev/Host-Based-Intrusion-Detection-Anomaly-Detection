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

    def getKey: Option[A] = key
    def setKey(aKey: A): Unit = key match {
      case Some(x) => throw new IllegalStateException("Node key cannot be reset")
      case None => key = Option(aKey)
    }
    def getEventCount: Int = eventCount
    def updateEvents(newEvent: B): Unit = {

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
      updatePredictions
    }

    def updatePredictions: Unit = {
      println("--------------------")
      if (eventCount > 0){
        predictions match {
          case None => predictions = Some(Map[B, Double]()); println("predictions did not exist. creating predictions")
          case _ => println("predictions existed. didn't create new one")
        }

        for ((k,v) <- events.get){
          println("\n(k,v) from events: " + (k,v))
          if(predictions.get.contains(k)) {
            println("predictions contained k: " + k)
            println("v.toDouble: " + v.toDouble)
            println("eventCount.toDouble: " + eventCount.toDouble)
            println("(v.toDouble / eventCount.toDouble): " + (v.toDouble / eventCount.toDouble))
            predictions.get.update(k, (v.toDouble / eventCount.toDouble))
          }else{
            println("predictions did not contain k: " + k)
            println("eventCount.toDouble: " + eventCount.toDouble)
            println("(1.00/eventCount.toDouble): " + (1.00/(eventCount.toDouble)))
            predictions.get += (k -> (1.toDouble / (eventCount.toDouble)))

          }
          println("updated predictions")
          for((keyV,valV) <- predictions.get) println("(keyV, valV) : " + (keyV, valV))
        }
      }
    }
    def getPredictions: Option[Map[B, Double]]  = predictions
    def getChildren: List[SMT[A,B]] = children
    def getEvents: Option[Map[B, Int]] = events

    def getProbability(input: B): Option[Double] = predictions match {
      case None => None
      case Some(x) => x.get(input)
    }
  }
  case class SequenceList[A,B](list: List[Sequence[A,B]]) extends SMT(maxDepth=0, maxPhi = 0){}


