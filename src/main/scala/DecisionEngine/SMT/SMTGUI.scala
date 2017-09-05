package DecisionEngine.SMT

import java.awt.event.{ActionEvent, ActionListener, ItemEvent, ItemListener}
import java.awt.{Toolkit, _}
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.{JFormattedTextField, _}
import javax.swing.text._
import javax.swing.JOptionPane
import javax.swing.BorderFactory
import javax.swing.border.{Border, EmptyBorder, EtchedBorder}

import Data.DataModel
import DecisionEngine.DecisionEngineGUI
import javax.swing.Box
import java.awt.Dimension
import javax.swing.JScrollPane

class SMTGUI extends DecisionEngineGUI {

  private val mainPanel = new JPanel()
  private val outputPanel = new JPanel()
  //private val inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT))
  private val inputPanel = new JPanel()
  override type T = SMTPlugin
  override var pluginInstance: Option[SMTPlugin] = None
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
  private final var outputLabelStr = "Report"

  private final val maxDepthLabel = new JLabel(maxDepthStr)
  private final val maxPhiLabel = new JLabel(maxPhiStr)
  private final val maxSeqCntLabel = new JLabel(maxSeqCntStr)
  private final val smoothingLabel = new JLabel(smoothingStr)
  private final val priorLabel = new JLabel(priorStr)
  private final val thresholdLabel = new JLabel(thresholdStr)
  private final val toleranceLabel = new JLabel(toleranceStr)
  private final val isIntLabel = new JLabel(isIntStr)
  private final val outputLabel = new JLabel(outputLabelStr)

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
  private final val loadModelBtn = new JButton("Load SMT")
  private final val saveModelBtn = new JButton("Save SMT")
  private final val saveReportBtn = new JButton("Save report")

  private final val outputTxtA = new JTextArea(30,134)

  //TODO - HOW TO CHECK IF CURRENT ROOT IS NODE[INT, INT]??
  private final val intCheckBox = new JCheckBox("Integer traces")

  initialise

  override def setPluginInstance(plugin: SMTPlugin): Unit = {
    super.setPluginInstance(plugin)
    //TODO - RENDER HERE!
    renderBtns
  }

  override def getGUIComponent: Option[JPanel] = {
    //    test(mainPanel)
    Some(mainPanel)
  }

  private def initialise(): Unit = {
    inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT))
    inputPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
      BorderFactory.createEmptyBorder(10, 10, 10, 10)))

    //SMT param components
    val p1 = new JPanel(new FlowLayout(FlowLayout.LEFT))
    p1.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED))

    p1.add(maxDepthLabel)
    addTxtField(p1, maxDepthField, maxDepthStr, maxDepthToolTipStr, 2,
      isPositive = true, isDouble = false, isPercent = false, registerChange = true)

    p1.add(maxPhiLabel)
    addTxtField(p1, maxPhiField, maxPhiStr, maxPhiToolTipStr, 2,
      isPositive = false, isDouble = false, isPercent = false, registerChange = true)

    p1.add(maxSeqCntLabel)
    addTxtField(p1, maxSeqCntField, maxSeqCntStr, maxSeqCntToolTipStr, 3,
      isPositive = true, isDouble = false, isPercent = false, registerChange = true)

    p1.add(smoothingLabel)
    addTxtField(p1, smoothingField, smoothingStr, smoothingToolTipStr, 3,
      isPositive = false, isDouble = true, isPercent = false, registerChange = true)

    p1.add(priorLabel)
    addTxtField(p1, priorField, priorStr, priorToolTipStr, 2,
      isPositive = false, isDouble = true, isPercent = false, registerChange = true)
    priorField.setText("0.1")

    priorField.setEditable(false)

    p1.add(intCheckBox)
    intCheckBox.addItemListener(new ItemListener {
      override def itemStateChanged(e: ItemEvent): Unit = {
        println("checkbox. paramsChanged before: " + paramChanged)
        paramChanged = hasRoot //If root is already set, manual change of SMT parameters is registered!
        println("checkbox. paramsChanged after: " + paramChanged)
      }
    })
    inputPanel.add(p1)
    inputPanel.add(Box.createRigidArea(new Dimension(0, 5)))

    //Classification params
    val p2 = new JPanel(new FlowLayout(FlowLayout.LEFT))
    p2.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED))

    p2.add(thresholdLabel)
    addTxtField(p2, thresholdField, thresholdStr, thresholdToolTipStr, 5,
      isPositive = false, isDouble = true, isPercent = false, registerChange = false)
    p2.add(toleranceLabel)
    addTxtField(p2, toleranceField, toleranceStr, toleranceToolTipStr, 2,
      isPositive = false, isDouble = true, isPercent = true, registerChange = false)

    inputPanel.add(p2)
    inputPanel.add(Box.createRigidArea(new Dimension(0, 5)))

    //ML buttons
    val p3 = new JPanel(new FlowLayout(FlowLayout.LEFT))
    p3.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED))
    val listener = new SMTBtnListener
    setupButton(p3, learnBtn, learnBtn.getText, listener)
    setupButton(p3, classifyBtn, classifyBtn.getText, listener)
    setupButton(p3, validateBtn, validateBtn.getText, listener)
    setupButton(p3, loadModelBtn, loadModelBtn.getText, listener)
    setupButton(p3, saveModelBtn, saveModelBtn.getText, listener)
    setupButton(p3, saveReportBtn, saveReportBtn.getText, listener)
    inputPanel.add(p3)

    //Output panel

    outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.PAGE_AXIS))
    outputTxtA.setEditable(false)
    outputTxtA.setBackground(new Color(173,216,230))
    outputTxtA.setFont(outputTxtA.getFont().deriveFont(12f))
    val sp = new JScrollPane(outputTxtA)
    sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS)
    sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)
    sp.setAlignmentX(Component.LEFT_ALIGNMENT)

    outputPanel.add(outputLabel)
    outputLabel.setLabelFor(sp)
    outputPanel.add(Box.createRigidArea(new Dimension(0, 10)))
    outputPanel.add(sp)
    outputPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
      BorderFactory.createEmptyBorder(10, 10, 10, 10)))

    //Add panels to mainPanel
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS))
    mainPanel.add(new JLabel("SPARSE MARKOV TREE"))
    mainPanel.setBackground(new Color(176,196,222))
    mainPanel.add(Box.createVerticalStrut(10))
    mainPanel.add(inputPanel)
    inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
    //mainPanel.add(Box.createHorizontalGlue())
    mainPanel.add(Box.createVerticalStrut(10))
    outputPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
    mainPanel.add(outputPanel)
    mainPanel.add(Box.createVerticalStrut(10))
    mainPanel.add(Box.createVerticalGlue())
    //mainPanel.setBorder(BorderFactory.createBevelBorder(50))
   // mainPanel.setBorder(new EmptyBorder(10,10,10,10))
   //val border2 = BorderFactory.createLineBorder(Color.black, 2)
   //val border2 = BorderFactory.createRaisedBevelBorder()
   val border2 = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)
    mainPanel.setBorder(BorderFactory.createCompoundBorder(border2,
      BorderFactory.createEmptyBorder(10, 10, 10, 10)))

    println("inputPanel: " + inputPanel.getLayout)
    println("outputPanel: " + outputPanel.getLayout)

  }

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

  private def canCreateRoot: Boolean =
    maxDepthField.getText.nonEmpty &&
      maxPhiField.getText.nonEmpty &&
      maxSeqCntField.getText.nonEmpty &&
      smoothingField.getText.nonEmpty &&
      priorField.getText.nonEmpty

  private def createPluginRoot: Option[Node[_, _]] = {
    if (canCreateRoot) {
      if (intCheckBox.isSelected) {
        Some(new Node[Int, Int](
          maxDepthField.getText.toInt,
          maxPhiField.getText.toInt,
          maxSeqCntField.getText.toInt,
          smoothingField.getText.toDouble,
          priorField.getText.toDouble))
      } else {
        //TODO - EXTEND FOR OTHER NODE TYPES
        Some(new Node[String, String](
          maxDepthField.getText.toInt,
          maxPhiField.getText.toInt,
          maxSeqCntField.getText.toInt,
          smoothingField.getText.toDouble,
          priorField.getText.toDouble))
      }
    } else return None
  }

  private def canLearn: Boolean = pluginInstance match {
    case None => false
    case Some(plugin) =>
      plugin.source.isDefined && (hasRoot || canCreateRoot)
  }

  private def canClassify: Boolean = pluginInstance match {
    case None => false
    case Some(plugin) => plugin.source.isDefined && (plugin.isTrained && thresholdField.getText.nonEmpty && toleranceField.getText.nonEmpty)
  }

  private def canLoadModel: Boolean = pluginInstance match {
    case None => false
    case Some(plugin) => if (plugin.source.isDefined) {
      plugin.source.get.isFile && plugin.isTrained
    } else false
  }

  private def canSaveModel: Boolean = pluginInstance match {
    case None => false
    case Some(plugin) => plugin.target.isDefined && plugin.isTrained
  }

  private def setupButton(panel: JPanel, btn: JButton, btnTxt: String, listener: ActionListener) = {
    panel.add(btn)
    btn.setActionCommand(btnTxt)
    addFieldDocListener(maxDepthField)
    addFieldDocListener(maxPhiField)
    addFieldDocListener(maxSeqCntField)
    addFieldDocListener(smoothingField)
    addFieldDocListener(thresholdField)
    addFieldDocListener(toleranceField)
    btn.addActionListener(listener)
    renderBtns
  }

  private def addFieldDocListener(field: JFormattedTextField) = {
    field.getDocument.asInstanceOf[PlainDocument].addDocumentListener(new DocumentListener {
      override def insertUpdate(e: DocumentEvent) = {
        renderBtns
      }

      override def changedUpdate(e: DocumentEvent) = {
        renderBtns
      }

      override def removeUpdate(e: DocumentEvent) = {
        renderBtns
      }
    })
  }

  private def renderBtns = {
    learnBtn.setEnabled(canLearn)
    classifyBtn.setEnabled(canClassify)
    validateBtn.setEnabled(canClassify)
    loadModelBtn.setEnabled(canLoadModel)
    saveModelBtn.setEnabled(canSaveModel)
    //saveReportBtn.setEnabled(canSaveReport)
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
      // println("in insertString")
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

  //TODO - MAKE SETPLUGINROOT PRIVATE
  def setPluginRoot(model: DataModel): Boolean = {
    pluginInstance match {
      case None => false
      case Some(plugin) => {
        //TODO - CHECK CONDITION BELOW!
        if (plugin.loadModel(model, isSetToInt)) {
          val newRoot = model.retrieve.get.asInstanceOf[Node[_, _]]

          maxDepthField.setText(newRoot.maxDepth.toString)
          maxPhiField.setText(newRoot.maxPhi.toString)
          maxSeqCntField.setText(newRoot.maxSeqCount.toString)
          smoothingField.setText(newRoot.smoothing.toString)
          priorField.setText(newRoot.prior.toString)
          paramChanged = false
          renderBtns
          true
        } else {
          false
        }
      }
    }
  }

  def render = {
    renderBtns
  }

  def clearText = {
    outputTxtA.setText("")
  }

  def appendText(str: String) = {

    outputTxtA.append("\n " + str)
  }

  override def isSetToInt = intCheckBox.isSelected

  private class SMTBtnListener extends ActionListener {
    override def actionPerformed(e: ActionEvent): Unit = {
      val btnLabel = e.getActionCommand
      println("SMT GUI button pressed: " + btnLabel)
      btnLabel match {
        case "Learn" => learnHandler
        case "Classify" => classifyValidateHandler(isClassify = true)
        case "Validate" => classifyValidateHandler(isClassify = false)
      }

    }

    private def learnHandler = {
      //check again in case conditions changed
      if (canLearn) {
        if (hasRoot) {
          if (paramChanged && !compareParams) {
            val response = JOptionPane.showConfirmDialog(null, "SMT root is already set with different parameters. Would you like to overwrite it?",
              "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
            if (response == JOptionPane.YES_OPTION) {
              if(addNewRoot){
                appendText("New SMT root set: " + pluginInstance.get.getModel().get.retrieve().get.asInstanceOf[Node[_,_]])
                setFlag("learn")
              }else{
                showError("An error occurred during SMT root initialisation!", "Error")
              }
            }

          } else {
            println("learnHandler: params have not changed. setting learnFlag in plugin")
           setFlag("learn")
          }
        } else {
          println("learnHandler: SMTPlugin -> no root set. creating new root")
          //no root set - learn btn active so SMT params must have been set manually
          if(addNewRoot){
            println("new root set. setting learnflag.")
            appendText("New SMT root set: " + pluginInstance.get.getModel().get.retrieve().get.asInstanceOf[Node[_,_]])
            setFlag("learn")
          }else{
            showError("An error occurred during SMT root initialisation!", "Error")
          }
        }
      } else {
        showError("An error occurred. Cannot train SMT!", "Error")
      }
    }

    private def classifyValidateHandler(isClassify: Boolean) = {
      if(canClassify){
        if(paramChanged && !compareParams){
          val response = JOptionPane.showConfirmDialog(null, "A trained SMT root is already set with different parameters. If you overwrite it, the new root will need to be trained before used for classification! Would you like to overwrite the current root?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)

          if (response == JOptionPane.YES_OPTION) {
            if(addNewRoot){
              appendText("New SMT root set: " + pluginInstance.get.getModel().get.retrieve().get + "\nNew root must be trained before classification!")
            }else{
              showError("An error occurred during SMT root initialisation!", "Error")
            }
          }else if (response == JOptionPane.CLOSED_OPTION) System.out.println("JOptionPane closed")
          else if (response == JOptionPane.NO_OPTION) System.out.println("No button clicked")
        }else{
          println("classifyHandler: params have not changed. setting classifyFlag in plugin")
          pluginInstance.get.setThreshold(thresholdField.getText().toDouble)
          pluginInstance.get.setTolerance(toleranceField.getText().toDouble)
          if(isClassify)
            setFlag("classify")
          else
            setFlag("validate")
        }
      }else{
        showError("An error occurred. Cannot classify with current SMT!", "Error")
      }
    }

    private def addNewRoot: Boolean = {
      println("in addnewRoot")
      createPluginRoot match {
        case None => showError("An error occurred during SMT root initialisation!", "Error"); false
        case Some(node) =>
          println("created new root. setting new root for plugin")
          val dm = new DataModel
          dm.store(node)
          setPluginRoot(dm)
      }
    }

    private def setFlag(flag: String) = {
      val thread = new Thread(() => flag match {
        case "learn" => pluginInstance.get.setLearnFlag
        case "classify" => pluginInstance.get.setClassifyFlag
        case "validate" => pluginInstance.get.setValidateFlag
        case _ =>
      })
      thread.start
    }

    private def compareParams(): Boolean = {
      val root = pluginInstance.get.getModel.get.retrieve.get.asInstanceOf[Node[_,_]]

      maxDepthField.getText.toInt.equals(root.maxDepth) &&
      maxPhiField.getText.toInt.equals(root.maxPhi) &&
      maxSeqCntField.getText.toInt.equals(root.maxSeqCount) &&
      smoothingField.getText.toDouble.equals(root.smoothing) &&
      priorField.getText.toDouble.equals(root.prior)
    }


    def showError(txt: String, title: String) = {
      JOptionPane.showMessageDialog(new JPanel, txt, title, JOptionPane.ERROR_MESSAGE)
    }
  }

}
