package TreeTest

import scala.collection.mutable.Map
import scalaz._
import scalaz.Scalaz._

/**
  * Created by Case on 02/07/2017.
  */
class Sequence[A,B] (_key: Vector[A], _event: B, _count: Int) {

  private var key: Vector[A] = Vector[A]()
  private var eventCount: Int = 0
  private var events: Map[B, Int] = Map[B, Int]()
  private var predictions: Map[B, Double] = Map[B, Double]()
  private var isChanged: Boolean = false
  //TODO - MODIFY THIS, SO PRIOR COULD BE SET IN CONSTRUCTOR
  //private var prior: Option[Double] = Some(0.00)

  def this(_key: Vector[A], _event: B){
    this(_key, _event, 1)
    println("In aux constructor.")
  }

  def this(_key: Vector[A], _events: Map[B, Int]){
    this(_key, _events.head._1, _events.head._2)
    updateEvents(_events.tail)
    println("In aux constructor 2.")
  }

  //Constructor argument validation
  require(_key.nonEmpty, "Sequence key cannot be an empty list!")
  require(_event != Nil && _event != None, "Sequence event cannot be Nil or None!")
  require(_count >= 1, "Event count must be larger than zero!")

  //Initialise Sequence
  setKey(_key)
  updateEvents(_event, _count)

  def getKey: Vector[A] = key

  def setKey(aKey: Vector[A]): Unit = if (key.isEmpty) key = aKey else throw new IllegalStateException("Sequence key cannot be reset")

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

  def updateEvents(newEvent: B, count: Int): Unit = {

    //update events to keep count on how many times this input has been seen
    events.get(newEvent) match {
      case None => events += (newEvent -> count)
      case Some(event) => events.update(newEvent, events(newEvent) + count)
    }
    //update event count to keep track of number of overall observations
    eventCount += count
    isChanged = true
  }

  def updateEvents(newEvent: B): Unit = updateEvents(newEvent, 1)

  def updateEvents(newEvents: Map[B, Int]): Unit = {


    events = (events /: newEvents) { case (map, (k,v)) =>
      map + ( k -> (v + map.getOrElse(k, 0)) )}

    //events = events |+| newEvents
    eventCount = events.foldLeft(0)(_+_._2)
    isChanged = true
  }


  def updatePredictions(): Unit = {
    for ((k, v) <- events) {
      if (predictions.contains(k)) predictions.update(k, v.toDouble / eventCount)
      else predictions += (k -> (v.toDouble / eventCount))
    }
  }

  override def toString: String = "\nSequence\nkey: " + key + "\nevents: " + events + "\n"
}
































/*class Sequence[A,B] (val sequence: List[A], var predictions: Map[B, Double]){

  def getProbability(input: B): Option[Double] = predictions.get(input)
  override def toString: String = "\n--------\nSequence\n-sequence:\n    " + sequence + "\n-predictions:\n    " + predictions + "\n-------"
}*/


/*
//Non-generic -> Int
class Sequence (val sequence: List[Int], var predictions: Map[Int, Double]){

  override def toString: String = "\n--------\nSequence\n-sequence:\n    " + sequence + "\n-predictions:\n    " + predictions + "\n-------"

  def getProbability(input:Int): Option[Double] = predictions.get(input)
}*/

