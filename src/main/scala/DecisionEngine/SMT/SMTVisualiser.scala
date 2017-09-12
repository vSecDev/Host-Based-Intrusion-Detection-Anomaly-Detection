package DecisionEngine.SMT

import java.awt.{BorderLayout, Color}
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javax.swing.{JDialog, _}
import Data.DataException
import DecisionEngine.DecisionEngineVisualiser
import prefuse.action.assignment.{ColorAction, DataColorAction}
import prefuse.action.layout.graph.ForceDirectedLayout
import prefuse.action.{ActionList, RepaintAction}
import prefuse.activity.Activity
import prefuse.controls.{DragControl, PanControl, ZoomControl}
import prefuse.data.Graph
import prefuse.data.io.GraphMLReader
import prefuse.render.{DefaultRendererFactory, LabelRenderer}
import prefuse.util.ColorLib
import prefuse.util.force.SpringForce
import prefuse.visual.VisualItem
import prefuse.{Constants, Display, Visualization}

class SMTVisualiser(tree: Node[_,_], canPrune: Boolean, maxNodeCount: Int) extends DecisionEngineVisualiser {

  @throws(classOf[DataException])
  override def getVisualisation: Option[JDialog] = {

    val xmlStr = tree.toXML(canPrune, maxNodeCount)

    val graph: Option[Graph] = try {
      val gr = new GraphMLReader
      Some(gr.readGraph(new ByteArrayInputStream(xmlStr.getBytes(StandardCharsets.UTF_8.name()))))
    } catch {
      case t: Throwable => throw new DataException("Error during parsing SMT xml.", t)
    }

    if (graph.isEmpty) {
      throw new DataException("Error during parsing SMT xml.")
    }

    val vis = new Visualization
    vis.add("graph", graph.get)

    val r = new LabelRenderer("key")
    r.setRoundedCorner(8, 8)
    vis.setRendererFactory(new DefaultRendererFactory(r))

    var palette = Array(ColorLib.rgb(248, 65, 8), ColorLib.rgb(78, 182, 211))
    var fill = new DataColorAction("graph.nodes", "type",
      Constants.NOMINAL, VisualItem.FILLCOLOR, palette)

    var text = new ColorAction("graph.nodes",
      VisualItem.TEXTCOLOR, ColorLib.gray(0))

    var edges = new ColorAction("graph.edges",
      VisualItem.STROKECOLOR, ColorLib.gray(200))

    var color = new ActionList()
    color.add(fill)
    color.add(text)
    color.add(edges)

    var layout = new ActionList(Activity.INFINITY)
    val fdl = new ForceDirectedLayout("graph")

    val forces = fdl.getForceSimulator.getForces
    val sf = forces(2).asInstanceOf[SpringForce]
    sf.setParameter(0, 0.05E-4f)

    layout.add(fdl)
    layout.add(new RepaintAction())

    vis.putAction("color", color)
    vis.putAction("layout", layout)

    var display = new Display(vis)
    display.setSize(720 * 2, 500 * 2)
    display.setBackground(new Color(0,142,164))
    display.addControlListener(new DragControl())
    display.addControlListener(new PanControl())
    display.addControlListener(new ZoomControl())

    val p: JPanel = new JPanel(new BorderLayout())
    p.add(display, BorderLayout.CENTER)
    val d: JDialog = new JDialog(new JDialog(),
      "Sparse Markov Tree Visualisation - Max Depth: " + tree.maxDepth +
        "  -  Max Phi: " + tree.maxPhi +
        "  -  Max Sequence Count in leaves: " + tree.maxSeqCount)

    d.add(p)
    d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    d.pack()
    vis.run("color")
    vis.run("layout")

    Some(d)
  }
}
