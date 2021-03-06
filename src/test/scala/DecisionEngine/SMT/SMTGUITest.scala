package DecisionEngine.SMT

import Data.DataModel
import DecisionEngine.{DecisionEngineGUI, DecisionEnginePlugin}
import org.scalatest.FunSuite

//TODO -DELETE CLASS
class SMTGUITest extends FunSuite{
  val maxDepth = 4
  val maxPhi = 2
  val maxSeqCount = 1
  val smoothing = 1.0
  val prior = 1.0
  val ints = true
  val condition1 = Vector(1, 2, 3)
  val condition2 = Vector(4, 5, 6)
  val condition3 = Vector(7, 8, 9)
  val strCondition1 = Vector("one", "two", "three")
  val strCondition2 = Vector("four", "five", "six")
  val strCondition3 = Vector("seven", "eight", "nine")
  val event1 = 666
  val event2 = 777
  val event3 = 888
  val eventStr1 = "eventStr1"
  val eventStr2 = "eventStr2"
  val eventStr3 = "eventStr3"

 /* test("GUI displays"){
    val n1 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(condition1, event1)
    val dm = new DataModel
    dm.store(n1)
    /*val plugin = new SMTPlugin
    plugin.loadModel(dm)
    plugin.setThreshold(0.5)
    plugin.setTolerance(20)
    val gui = new SMTGUI
    gui.setPluginInstance(plugin)
    gui.getGUIComponent*/

    val guiToAdd = new SMTGUI
    val plugin2: DecisionEnginePlugin = new SMTPlugin(guiToAdd)
    val gui2 = plugin2.getGUI.get
    gui2.getGUIComponent
    println("20 sleep to change maxDepth")
    Thread.sleep(20000)
    gui2.asInstanceOf[SMTGUI].setPluginRoot(dm)
    println("After setplugin root .20 sleep to change maxDepth")
    Thread.sleep(20000)

    val untrainedRoot = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    val dm2 = new DataModel
    dm2.store(untrainedRoot)
    gui2.asInstanceOf[SMTGUI].setPluginRoot(dm2)

    Thread.sleep(1000000)


  }*/
}
