package TreeTest

import scala.collection.mutable.Map

/**
  * Created by Case on 02/07/2017.
  */
class Sequence[A,B] (_key: Vector[A], _event: B) {

  private var key: Vector[A] = Vector[A]()
  private var eventCount: Int = 0
  private var events: Map[B, Int] = Map[B, Int]()
  private var predictions: Map[B, Double] = Map[B, Double]()
  private var isChanged: Boolean = false

  //Constructor argument validation
  require(_key.nonEmpty, "Sequence key cannot be an empty list!")
  require(_event != Nil && _event != None, "Sequence event cannot be Nil or None!")
  //Initialise Sequence
  setKey(_key)
  updateEvents(_event)

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

  def updateEvents(newEvent: B): Unit = {

    //update events to keep count on how many times this input has been seen
    events.get(newEvent) match {
      case None => events += (newEvent -> 1)
      case Some(event) => events.update(newEvent, events(newEvent) + 1)
    }
    //update event count to keep track of number of overall observations
    eventCount += 1
    isChanged = true
    //TODO MOVE UPDATEPREDICTIONS OUTSIDE THIS FUNCTION. BUT MAKE SURE GETPREDICTIONS ALWAYS USES UP-DO-DATE DATA.
    //updatePredictions
  }

  def updatePredictions(): Unit = {
    for ((k, v) <- events) {
      if (predictions.contains(k)) {
        predictions.update(k, v.toDouble / eventCount)
      }
      else {
        //predictions += (k -> (1.00/eventCount))}
        predictions += (k -> (v.toDouble / eventCount))
      }
    }
  }
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

