package DecisionEngine.SMT

import java.io._
import scala.collection.mutable.Map

@SerialVersionUID(666L)
class Sequence[A,B](_condition: Vector[A], _event: B, _smoothing: Double, _prior: Double) extends Serializable {

  var smoothing: Double = -1.0
  var prior: Double = -1.0
  var weight: Double = 1.0
  private var condition: Vector[A] = Vector[A]()
  private var eventCount: Int = 0
  private var events: Map[B, Int] = Map[B, Int]()
  private var predictions: Map[B, Double] = Map[B, Double]()
  private var isChanged: Boolean = false

  require(_condition.nonEmpty, "Sequence key cannot be an empty list!")
  require(_event != Nil && _event != None, "Sequence event cannot be Nil or None!")
  require(_smoothing >= 0, "Sequence smoothing value must be non-negative!")
  require(_prior > 0, "Sequence prior weight must be larger than zero!")

  setKey(_condition)
  setSmoothing(_smoothing)
  setPrior(_prior)
  setWeight(prior)
  updateEvents(_event)

  def getKey: Vector[A] = condition

  def setKey(aKey: Vector[A]): Unit = if (condition.isEmpty) condition = aKey else throw new IllegalStateException("Sequence key cannot be reset")

  def getSmoothing: Double = smoothing

  def setSmoothing(aSmoothing: Double): Unit = if (smoothing == -1.0) smoothing = aSmoothing else throw new IllegalStateException("Sequence smoothing cannot be reset")

  def getPrior: Double = prior

  def setPrior(aPrior: Double): Unit = if (prior == -1.0) prior = aPrior else throw new IllegalStateException("Sequence prior cannot be changed after initialisation")

  def getWeight: Double = weight

  def setWeight(aWeight: Double): Unit = if (aWeight > 0.0) weight = aWeight else throw new IllegalStateException("Sequence weight cannot be negative")

  def getEventCount: Int = eventCount

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
    case None => smoothing / eventCount
  }

  def getWeightedProbability(input: B): Double = weight * getProbability(input)

  def updateEvents(newEvent: B): Unit = {

    //update events to keep count on how many times this input has been seen
    events.get(newEvent) match {
      case Some(event) => events.update(newEvent, events(newEvent) + 1)
      case None => events += (newEvent -> 1)
    }
    //update event count to keep track of number of overall observations
    eventCount += 1
    isChanged = true
    updateWeight(newEvent)
  }

  private def updateWeight(newEvent: B): Unit = weight *= getProbability(newEvent)

  private def updatePredictions(): Unit = {
    for ((k, v) <- events) {
      if (predictions.contains(k)) predictions.update(k, (v.toDouble + smoothing) / eventCount)
      else predictions += (k -> ((v.toDouble + smoothing) / eventCount))
    }
  }

  override def toString: String = "\nSequence\nkey: " + condition + "\nevents: " + events + "\n"
}