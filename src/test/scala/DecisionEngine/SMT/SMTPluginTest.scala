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
  test("SMTPlugin - learn returns trained model - root loaded"){

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

    //storing condition2, event1
    dw.store("4 5 6 666")

    plugin.learn(Vector(dw), None, ints)
    val returnedModel3 = plugin.getModel.get.retrieve.get
    val retNode3 = returnedModel3.asInstanceOf[Node[Int, Int]]

    var r8 = retNode3.predict(condition1, event1)
    var r9 = retNode3.predict(condition1, event2)
    var r10 = retNode3.predict(condition1, event3)
    var r11 = retNode3.predict(condition2, event1)
    var r12 = retNode3.predict(condition2, event2)
    var r13 = retNode3.predict(condition2, event3)

    assert(retNode3.getChildren(0)(0).asInstanceOf[Node[Int, Int]].getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1.drop(1)).get.getWeight == 2.0 / 9)
    assert(r8._1 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3 && r8._2 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3)
    assert(r9._1 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3 && r9._2 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3)
    assert(r10._1 == 1.0/9 + 1.0/9 + 1.0/6 + 1.0/3 && r10._2 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3)
    assert(r11._1 == 4.0/9 + 4.0/9 + 4.0/6 + 4.0/3 && r11._2 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3)
    assert(r12._1 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3 && r12._2 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3)
    assert(r13._1 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3 && r13._2 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3)



    val n2 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    val n2Wrapper = new DataModel
    n2Wrapper.store(n2)
    val dw2 = new StringDataWrapper
    //store condition1, event1
    dw2.store("1 2 3 666")

    //DataModel stored as root in DecisionEngine should not change. Only the new tree passed in should be trained.
    val newDM = plugin.learn(Vector(dw2), Some(n2Wrapper), ints)
    val n2Trained = newDM.get.retrieve.get.asInstanceOf[Node[Int,Int]]
    assert(n2 == n2Trained)

    var newR1 = n2Trained.predict(condition1, event1)
    var newR2 = n2Trained.predict(condition1, event2)
    assert(newR1._1 == 4.0 && newR1._2 == 2.0)
    assert(newR2._1 == 2.0 && newR2._2 == 2.0)
    assert(n2Trained.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)


    //Root has not changed after training new model passed in to learn
    val returnedModel4 = plugin.getModel.get.retrieve.get
    val retNode4 = returnedModel3.asInstanceOf[Node[Int, Int]]

    r8 = retNode4.predict(condition1, event1)
    r9 = retNode4.predict(condition1, event2)
    r10 = retNode4.predict(condition1, event3)
    r11 = retNode4.predict(condition2, event1)
    r12 = retNode4.predict(condition2, event2)
    r13 = retNode4.predict(condition2, event3)

    assert(retNode4.getChildren(0)(0).asInstanceOf[Node[Int, Int]].getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1.drop(1)).get.getWeight == 2.0 / 9)
    assert(r8._1 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3 && r8._2 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3)
    assert(r9._1 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3 && r9._2 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3)
    assert(r10._1 == 1.0/9 + 1.0/9 + 1.0/6 + 1.0/3 && r10._2 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3)
    assert(r11._1 == 4.0/9 + 4.0/9 + 4.0/6 + 4.0/3 && r11._2 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3)
    assert(r12._1 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3 && r12._2 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3)
    assert(r13._1 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3 && r13._2 == 2.0/9 + 2.0/9 + 2.0/6 + 2.0/3)
  }
  test("SMTPlugin - learn returns model if data is empty"){

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

    //Creating + loading root
    val n1 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(condition1, event1)
    var r1 = n1.predict(condition1, event1)
    var r2 = n1.predict(condition1, event2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)

    val model = new DataModel
    model.store(n1)
    val plugin = new SMTPlugin
    assert(plugin.loadModel(model))
    val rootM = plugin.getModel.get.retrieve.get
    assert(rootM.isInstanceOf[Node[Int, Int]])
    val root = rootM.asInstanceOf[Node[Int, Int]]

    var r3 = root.predict(condition1, event1)
    var r4 = root.predict(condition1, event2)
    assert(r3._1 == 4.0 && r1._2 == 2.0)
    assert(r4._1 == 2.0 && r2._2 == 2.0)
    assert(root.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)


    //Passing in new model to learn - root should not change
    val n2 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    val childrenCountBefore = n2.getChildren.size
    val dm2 = new DataModel
    dm2.store(n2)

    //No data passed in
    val nonTrainedModel = plugin.learn(Vector(), Some(dm2), ints).get.retrieve.get.asInstanceOf[Node[Int,Int]]
    val root2 = plugin.getModel.get.retrieve.get.asInstanceOf[Node[Int,Int]]
    assert(n2 == nonTrainedModel)
    assert(nonTrainedModel.getChildren.size == childrenCountBefore)
    assert(nonTrainedModel.getChildren.size != root2.getChildren.size)

    //root hasn't changed

    r3 = root2.predict(condition1, event1)
    r4 = root2.predict(condition1, event2)
    assert(r3._1 == 4.0 && r1._2 == 2.0)
    assert(r4._1 == 2.0 && r2._2 == 2.0)
    assert(root2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)
  }
}