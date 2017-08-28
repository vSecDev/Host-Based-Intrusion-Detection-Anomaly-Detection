package DecisionEngine.SMT

import java.awt.event.{ItemEvent, ItemListener}
import java.awt.{Toolkit, _}
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.{JFormattedTextField, _}
import javax.swing.text._

import Data.DataModel
import DecisionEngine.DecisionEngineGUI

class SMTGUI extends DecisionEngineGUI {

  private val mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT))
  override type T = SMTPlugin
  override var pluginInstance: Option[SMTPlugin] = Some(new SMTPlugin)
  private var paramChanged = false

  private final val maxDepthStr = "Max Depth:"
  private final val maxDepthToolTipStr = "Maximum Depth (sliding window size) of the Sparse Markov Tree. Must be a positive integer!"
  private final val maxPhiStr = "Max Phi:"
  private final val maxPhiToolTipStr = "Maximum Phi: the maximum number of wildcards in the conditioning sequences used for generating the Sparse Markov Tree. Must be a non-negative integer!"
  private final val maxSeqCntStr = "Max sequence count:"
  private final val maxSeqCntToolTipStr = "Maximum Sequence Count: the maximum number of sequence-trails stored in the leaf nodes of the Sparse Markov Tree. Must be a positive integer!"
  private final val smoothingStr = "Smoothing:"
  private final val smoothingToolTipStr = "Smoothing value for calculating probabilities of unseen sequences. Must be a non-negative decimal!"
  private final val priorStr = "Prior weight:"
  private final val priorToolTipStr = "The prior probability of the Sparse Markov Tree mixture. Must be a positive decimal!"
  private final val thresholdStr = "Threshold:"
  private final val thresholdToolTipStr = "The probability threshold for sequence classification. Probabilities below the threshold are classified as anomalous. Must be a non-negative decimal!"
  private final val toleranceStr = "Tolerance (%):"
  private final val toleranceToolTipStr = "If the percentage of subsequences in the analysed system call trace exceed the tolerance, the whole trace is classified as anomalous. Must be a decimal between 0-100!"
  private final val isIntStr = "Integer traces"
  private final val isIntToolTipStr = "Integer traces: check if the analysed system call traces have been pre-processed using \"system call-to-integer\" mapping."

  private final val maxDepthLabel = new JLabel(maxDepthStr)
  private final val maxPhiLabel = new JLabel(maxPhiStr)
  private final val maxSeqCntLabel = new JLabel(maxSeqCntStr)
  private final val smoothingLabel = new JLabel(smoothingStr)
  private final val priorLabel = new JLabel(priorStr)
  private final val thresholdLabel = new JLabel(thresholdStr)
  private final val toleranceLabel = new JLabel(toleranceStr)
  private final val isIntLabel = new JLabel(isIntStr)

  private final val maxDepthField = new JFormattedTextField
  private final val maxPhiField = new JFormattedTextField
  private final val maxSeqCntField = new JFormattedTextField
  private final val smoothingField = new JFormattedTextField
  private final val priorField = new JFormattedTextField
  private final val thresholdField = new JFormattedTextField
  private final val toleranceField = new JFormattedTextField

  private final val learnBtn = new JButton("Learn")
  private final val classifyBtn = new JButton("Classify")
  private final val validateBtn = new JButton("Validate")

  //TODO - HOW TO CHECK IF CURRENT ROOT IS NODE[INT, INT]??
  private final val intCheckBox = new JCheckBox("Integer traces")

  initialise

  override def setPluginInstance(plugin: SMTPlugin): Unit = {
    super.setPluginInstance(plugin)
    //TODO - RENDER HERE!
  }

  override def getGUIComponent: Option[JPanel] = {
    test(mainPanel)
    Some(mainPanel)
  }

  private def initialise(): Unit = {
    //SMT param components
    val smtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT))
    smtPanel.add(maxDepthLabel)
    addTxtField(smtPanel, maxDepthField, maxDepthStr, maxDepthToolTipStr, 2, isPositive = true, isDouble = false, isPercent = false, registerChange = true)
    smtPanel.add(maxPhiLabel)
    addTxtField(smtPanel, maxPhiField, maxPhiStr, maxPhiToolTipStr, 2, isPositive = false, isDouble = false, isPercent = false, registerChange = true)
    smtPanel.add(maxSeqCntLabel)
    addTxtField(smtPanel, maxSeqCntField, maxSeqCntStr, maxSeqCntToolTipStr, 3, isPositive = true, isDouble = false, isPercent = false, registerChange = true)
    smtPanel.add(smoothingLabel)
    addTxtField(smtPanel, smoothingField, smoothingStr, smoothingToolTipStr, 3, isPositive = false, isDouble = true, isPercent = false, registerChange = true)
    smtPanel.add(priorLabel)
    //TODO - CHECK FOR 0.0 PRIOR BEFORE CLASSIFICATION
    addTxtField(smtPanel, priorField, priorStr, priorToolTipStr, 2, isPositive = false, isDouble = true, isPercent = false, registerChange = true)
    priorField.setText("1.0")
    priorField.setEditable(false)
    smtPanel.add(intCheckBox)
    intCheckBox.addItemListener(new ItemListener {
      override def itemStateChanged(e: ItemEvent): Unit = {
        println("checkbox. paramsChanged before: " + paramChanged)
        paramChanged = hasRoot //If root is already set, manual change of SMT parameters is registered!
        println("checkbox. paramsChanged after: " + paramChanged)
      }
    })
    smtPanel.setBorder(BorderFactory.createLineBorder(Color.black))
    mainPanel.add(smtPanel)

    //Classification params
    val classifyParamsP = new JPanel(new FlowLayout(FlowLayout.LEFT))
    classifyParamsP.add(thresholdLabel)
    addTxtField(classifyParamsP, thresholdField, thresholdStr, thresholdToolTipStr, 5, isPositive = false, isDouble = true, isPercent = false, registerChange = false)
    classifyParamsP.add(toleranceLabel)
    addTxtField(classifyParamsP, toleranceField, toleranceStr, toleranceToolTipStr, 2, isPositive = false, isDouble = true, isPercent = true, registerChange = false)
    classifyParamsP.setBorder(BorderFactory.createLineBorder(Color.black))
    mainPanel.add(classifyParamsP)

    //ML buttons
    val mlParamsP = new JPanel(new FlowLayout(FlowLayout.LEFT))
    setupButton(mlParamsP, learnBtn, learnBtn.getText)
    setupButton(mlParamsP, classifyBtn, classifyBtn.getText)
    setupButton(mlParamsP, validateBtn, validateBtn.getText)
    mlParamsP.setBorder(BorderFactory.createLineBorder(Color.black))
    mainPanel.add(mlParamsP)
  }

  private def setupButton(panel: JPanel, btn: JButton, btnTxt: String) = {
    panel.add(btn)
    btn.setActionCommand(btnTxt)
    //TODO - LISTEN TO ALL BUTTON TEXT UPDATES ->  CALCULATE CANLEARN/CANCLASSIFY -> ENABLA/DISABLE BUTTON ACCORDINGLY!
    //maxDepthField.getDocument.asInstanceOf[PlainDocument].getDocumentFilter.
  }

  private def createPluginRoot: Option[Node[_,_]] = {
    if(canCreateRoot){
      if(intCheckBox.isSelected){
        Some(new Node[Int, Int](
          maxDepthField.getText.toInt,
          maxPhiField.getText.toInt,
          maxSeqCntField.getText.toInt,
          smoothingField.getText.toDouble,
          priorField.getText.toDouble))
      }else{
        //TODO - EXTEND FOR OTHER NODE TYPES
        Some(new Node[String, String](
          maxDepthField.getText.toInt,
          maxPhiField.getText.toInt,
          maxSeqCntField.getText.toInt,
          smoothingField.getText.toDouble,
          priorField.getText.toDouble))
      }
    }else return None
  }

  private def canCreateRoot: Boolean =
    maxDepthField.getText.nonEmpty &&
      maxPhiField.getText.nonEmpty &&
      maxSeqCntField.getText.nonEmpty &&
      smoothingField.getText.nonEmpty &&
      priorField.getText.nonEmpty

  private def hasRoot: Boolean = {
    pluginInstance match {
      case None => false
      case Some(plugin) => {
        plugin.getModel match {
          case None => false
          case Some(dataModel) => dataModel.retrieve match {
            case None => false
            case Some(n: Node[_, _]) => true
            case _ => false
          }
        }
      }
    }
  }

  private def canLearn: Boolean = hasRoot || canCreateRoot

  private def canClassify: Boolean = pluginInstance match {
    case None => false
    case Some(plugin) => plugin.isTrained && thresholdField.getText.nonEmpty && toleranceField.getText.nonEmpty
  }

  private def addTxtField(panel: JPanel, field: JFormattedTextField, fieldName: String, tooltipStr: String, col: Int, isPositive: Boolean, isDouble: Boolean, isPercent: Boolean, registerChange: Boolean) = {
    field.setColumns(col)
    field.setName(fieldName)
    field.createToolTip()
    field.setToolTipText(tooltipStr)
    val doc = field.getDocument.asInstanceOf[PlainDocument]
    doc.setDocumentFilter(new NonNegNumFilter(isPositive, isDouble, isPercent))
    if (registerChange) {
      doc.addDocumentListener(new DocumentListener {
        override def removeUpdate(e: DocumentEvent): Unit = {
          println("in documentlistener removeUpdate. paramschanged before: " + paramChanged);
          paramChanged = hasRoot;
          println("paramschanged after: " + paramChanged)
        }

        override def changedUpdate(e: DocumentEvent): Unit = {
          println("in documentlistener changedUpdate");
          paramChanged = hasRoot
        }

        override def insertUpdate(e: DocumentEvent): Unit = {
          println("in documentlistener insertUpdate. paramschanged before: " + paramChanged);
          paramChanged = hasRoot
        }
      })
    }
    panel.add(field)
  }

  private class NonNegNumFilter(isPositive: Boolean, isDouble: Boolean, isPercent: Boolean) extends DocumentFilter {

    @throws[BadLocationException]
    override def insertString(fb: DocumentFilter.FilterBypass, offset: Int, string: String, attr: AttributeSet): Unit = {
      println("in insertString")
      val doc = fb.getDocument
      val sb = new StringBuilder
      sb.append(doc.getText(0, doc.getLength))
      sb.insert(offset, string)
      if (test(sb.toString, isDouble, isPercent)) super.insertString(fb, offset, string, attr)
      else {
        // warn the user and don't allow the insert
        println("insertString test failed. removing new char")
        Toolkit.getDefaultToolkit.beep
      }
    }

    private def test(text: String, isDouble: Boolean, isPercent: Boolean) = try {
      println("in test")

      if (isDouble) {
        val input = text.toDouble
        if (isPercent) {
          (isPositive && input > 0.0 && input <= 100.0) || (!isPositive && input >= 0.0 && input <= 100.0)
        } else {
          (isPositive && input > 0.0) || (!isPositive && input >= 0.0)
        }
      } else {
        val input = text.toInt
        (isPositive && input > 0) || (!isPositive && input >= 0)
      }
    } catch {
      case e: Throwable =>
        println("EXCEPTION: " + e.getMessage)
        false
    }

    @throws[BadLocationException]
    override def replace(fb: DocumentFilter.FilterBypass, offset: Int, length: Int, text: String, attrs: AttributeSet): Unit = {
      println("in replace")
      val doc = fb.getDocument
      val sb = new StringBuilder
      sb.append(doc.getText(0, doc.getLength))
      sb.replace(offset, offset + length, text)
      if (test(sb.toString, isDouble, isPercent)) super.replace(fb, offset, length, text, attrs)
      else {
        Toolkit.getDefaultToolkit.beep
      }
    }

    @throws[BadLocationException]
    override def remove(fb: DocumentFilter.FilterBypass, offset: Int, length: Int): Unit = {
      println("in remove")
      val doc = fb.getDocument
      val sb = new StringBuilder
      sb.append(doc.getText(0, doc.getLength))
      sb.delete(offset, offset + length)
      if (sb.toString().length() == 0) super.replace(fb, offset, length, "", null)
      else if (test(sb.toString, isDouble, isPercent)) super.remove(fb, offset, length)
      else {
        Toolkit.getDefaultToolkit.beep
      }
    }
  }

  def setPluginRoot(model: DataModel): Boolean = {
    pluginInstance match {
      case None => false
      case Some(plugin) => {
        if (plugin.loadModel(model)) {
          val newRoot = model.retrieve.get.asInstanceOf[Node[_, _]]

          maxDepthField.setText(newRoot.maxDepth.toString)
          maxPhiField.setText(newRoot.maxPhi.toString)
          maxSeqCntField.setText(newRoot.maxSeqCount.toString)
          smoothingField.setText(newRoot.smoothing.toString)
          priorField.setText(newRoot.prior.toString)
          paramChanged = false
          true
        } else {
          false
        }
      }
    }
  }


  //TODO - DELETE TEST
  private def test(panel: JPanel): Unit = {
    val frame = new JFrame("HIDS")
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setSize(800, 500)

    /* panel.add(maxDepthLabel)
     addNonNegNumTextField(panel, maxDepthField, maxDepthStr, maxDepthToolTipStr, 3, isPositive = true, isDouble = false, isPercent = false)
     panel.add(maxPhiLabel)
     addNonNegNumTextField(panel, maxPhiField, maxPhiStr, maxPhiToolTipStr, 3, isPositive = false, isDouble = false, isPercent = false)
     panel.add(maxSeqCntLabel)
     addNonNegNumTextField(panel, maxSeqCntField, maxSeqCntStr, maxSeqCntToolTipStr, 3, isPositive = true, isDouble = false, isPercent = false)


     panel.add(smoothingLabel)
     addNonNegNumTextField(panel, smoothingField, smoothingStr, smoothingToolTipStr, 3, isPositive = false, isDouble = true, isPercent = false)
     panel.add(priorLabel)
     //TODO - CHECK FOR 0.0 PRIOR BEFORE CLASSIFICATION
     addNonNegNumTextField(panel, priorField, priorStr, priorToolTipStr, 3, isPositive = false, isDouble = true, isPercent = false)
     panel.add(thresholdLabel)
     addNonNegNumTextField(panel, thresholdField, thresholdStr, thresholdToolTipStr, 5, isPositive = false, isDouble = true, isPercent = false)
     panel.add(toleranceLabel)
     addNonNegNumTextField(panel, toleranceField, toleranceStr, toleranceToolTipStr, 5, isPositive = false, isDouble = true, isPercent = true)*/

    /*    isIntCheckBox.addItemListener(new ItemListener {
          override def itemStateChanged(e: ItemEvent): Unit = {
            val isIntsBox = e.getSource.asInstanceOf[JCheckBox]
            isInts = isIntsBox.isSelected
            println("intbox state: " + isInts)
          }
        })*/
    /* panel.add(isIntCheckBox)*/


    val cp = frame.getContentPane
    cp.setLayout(new FlowLayout(FlowLayout.LEFT))
    cp.add(panel)
    frame.pack()
    frame.setVisible(true)


  }
}
