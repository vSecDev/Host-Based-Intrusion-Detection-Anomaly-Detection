package TreeTest

import scala.collection.mutable

/**
  * Created by Case on 02/07/2017.
  */
class Sequence[A,B] (_key: List[A], _event: B){

  private var key: List[A] = Nil
  private var events: mutable.Map[B, Int] = mutable.Map[B, Int]()
  private var eventCount: Int = 0

  def this(val _key: List[A], _event:B){
    setKey(_key)


    //events += _event -> 1 //use updateEvent instead

  }


  def getKey: List[A] = key
  def setKey(aKey: List[A]): Unit = key match {
    case Nil => key = aKey
    case _ => throw new IllegalStateException("Node key cannot be reset")
  }
  def getEventCount: Int = eventCount

  def getEvents: mutable.Map[B, Int] = events


}







private var events: Map[B, Int] = Map[B, Int]()
private var predictions: Map[B, Double] = Map[B, Double]()
private var children: List[List[SMT[A,B]]] = Nil



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


def getProbability(input: B): Option[Double] = predictions.get(input)



















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

