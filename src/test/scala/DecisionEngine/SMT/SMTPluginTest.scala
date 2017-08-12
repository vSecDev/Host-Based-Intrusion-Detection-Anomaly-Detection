package DecisionEngine.SMT

import java.io._

import Data.{DataModel, DataWrapper, StringDataWrapper}
import Data.File.FileProcessor
import DecisionEngine.DecisionEngineConfig
import DecisionEngine.SMT.{Node, SequenceList}
import org.apache.commons.io.{FileUtils, FilenameUtils}
import org.scalatest.FunSuite

import scala.collection.mutable
import scala.io.Source

class SMTPluginTest extends  FunSuite {
  val isHome = true
  var testSource = ""
  var testTarget = ""
  var mapTestSource = ""
  var mapTestTarget = ""

  if (isHome) {
    mapTestSource = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\MapTest\\source"
    mapTestTarget = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\MapTest\\target"
    testSource = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\"
    testTarget = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\main\\target"
  }
  else {
    mapTestSource = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\MapTest\\source"
    mapTestTarget = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\MapTest\\target"
    testSource = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\"
    testTarget = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\main\\target"
  }

  test("SMTPlugin - configuration works") {
    val maxDepth = 3
    val maxPhi = 2
    val maxSeqCount = 1
    val smoothing = 1.0
    val prior = 1.0
    val ints = true

    val settings = new SMTSettings(maxDepth, maxPhi, maxSeqCount, smoothing, prior, ints)
    val config: DecisionEngineConfig = new SMTConfig
    config.asInstanceOf[SMTConfig].storeSettings(settings)
    assert(config.getSettings.get == settings)
    assert(config.asInstanceOf[SMTConfig].getSettings.get.maxDepth == maxDepth)
    assert(config.asInstanceOf[SMTConfig].getSettings.get.maxPhi == maxPhi)
    assert(config.asInstanceOf[SMTConfig].getSettings.get.maxSeqCount == maxSeqCount)
    assert(config.asInstanceOf[SMTConfig].getSettings.get.smoothing == smoothing)
    assert(config.asInstanceOf[SMTConfig].getSettings.get.prior == prior)
    assert(config.asInstanceOf[SMTConfig].getSettings.get.isIntTrace == ints)

    val plugin = new SMTPlugin
    plugin.configure(config)

    val returnedModel = plugin.getModel
    assert(returnedModel isDefined)
    val model = returnedModel.get
    val root = model.retrieve.get.asInstanceOf[Node[Int, Int]]

    assert(root.maxDepth == maxDepth)

    assert(root.maxPhi == maxPhi)
    assert(root.maxSeqCount == maxSeqCount)
    assert(root.smoothing == smoothing)
    assert(root.prior == prior)
  }
  test("SMTPlugin - learn returns trained model"){

    val maxDepth = 4
    val maxPhi = 2
    val maxSeqCount = 1
    val smoothing = 1.0
    val prior = 1.0
    val ints = true
    val condition1 = Vector(1, 2, 3)
    val condition2 = Vector(4, 5, 6)
    val condition3 = Vector(7, 8, 9)
    val event1 = 666
    val event2 = 777
    val event3 = 888

    val n1 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(condition1, event1)
    var r1 = n1.predict(condition1, event1)
    var r2 = n1.predict(condition1, event2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)

    val model = new DataModel
    model.store(n1)
    assert(model.retrieve.get.isInstanceOf[Node[Int,Int]])

    val plugin = new SMTPlugin
    assert(plugin.loadModel(model))
    val returnedModel1 = plugin.getModel.get.retrieve.get
    assert(returnedModel1.isInstanceOf[Node[Int, Int]])
    val retNode1 = returnedModel1.asInstanceOf[Node[Int, Int]]

    var r3 = retNode1.predict(condition1, event1)
    var r4 = retNode1.predict(condition1, event2)
    assert(r3._1 == 4.0 && r1._2 == 2.0)
    assert(r4._1 == 2.0 && r2._2 == 2.0)
    assert(retNode1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)


    val dw = new StringDataWrapper
    //storing condition1, event2
    dw.store("1 2 3 777")
    plugin.learn(Vector(dw), None, ints)
    val returnedModel2 = plugin.getModel.get.retrieve.get
    val retNode2 = returnedModel2.asInstanceOf[Node[Int, Int]]

    val r5 = retNode2.predict(condition1, event1)
    val r6 = retNode2.predict(condition1, event2)
    var r7 = retNode2.predict(condition1, event3)
    assert(retNode2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)
    assert(r5._1 == 2.0 && r5._2 == 2.0)
    assert(r6._1 == 2.0 && r6._2 == 2.0)
    assert(r7._1 == 1.0 && r7._2 == 2.0)
  }


}





