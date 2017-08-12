package DecisionEngine.SMT

import java.io._

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

}





