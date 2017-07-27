package TreeTest

import scala.collection.mutable.Map

/**
  * Created by Case on 02/07/2017.
  */
class Sequence[A,B](_condition: Vector[A], _event: B, _smoothing: Double) {

  private var condition: Vector[A] = Vector[A]()
  private var smoothing: Double = 0.0
  private var eventCount: Int = 0
  private var events: Map[B, Int] = Map[B, Int]()
  private var predictions: Map[B, Double] = Map[B, Double]()
  private var isChanged: Boolean = false
  //TODO - MODIFY THIS, SO PRIOR COULD BE SET IN CONSTRUCTOR
  //private var prior: Option[Double] = Some(0.00)

  //Constructor argument validation
  require(_condition.nonEmpty, "Sequence key cannot be an empty list!")
  require(_event != Nil && _event != None, "Sequence event cannot be Nil or None!")
  require(_smoothing > 0, "Smoothing value must be larger than zero!")
  //Initialise Sequence
  setKey(_condition)
  setSmoothing(_smoothing)
  updateEvents(_event)

  def getKey: Vector[A] = condition

  def setKey(aKey: Vector[A]): Unit = if (condition.isEmpty) condition = aKey else throw new IllegalStateException("Sequence key cannot be reset")

  def getSmoothing: Double = smoothing

  def setSmoothing(aSmoothing: Double): Unit = if (smoothing == 0.0) smoothing = aSmoothing else throw new IllegalStateException("Sequence smoothing cannot be reset")

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

  def getProbability(input: B): Double = getPredictions.get(input) match {
    case Some(x) => x
    case None => _smoothing / eventCount
  }

  def updateEvents(newEvent: B): Unit = {

    //update events to keep count on how many times this input has been seen
    events.get(newEvent) match {
      case Some(event) => events.update(newEvent, events(newEvent) + 1)
      case None => events += (newEvent -> 1)
    }
    //update event count to keep track of number of overall observations
    eventCount += 1
    isChanged = true
  }

  def updatePredictions(): Unit = {
    for ((k, v) <- events) {
      if (predictions.contains(k)) predictions.update(k, v.toDouble + getSmoothing / eventCount)
      else predictions += (k -> (v.toDouble + getSmoothing / eventCount))
    }
  }

  override def toString: String = "\nSequence\nkey: " + condition + "\nevents: " + events + "\n"
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

