package GUI;

import Data.DataProcessor;
import DecisionEngine.DecisionEngineGUI;
import DecisionEngine.DecisionEnginePlugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javafx.util.Pair;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import scala.Option;

//public class HIDS extends Observable implements Observer {
public class HIDS extends Observable {

    private static final String configPath = new File("").getAbsolutePath() + "\\src\\main\\resources\\config.properties";
    private List<DecisionEnginePlugin> decisionEngines = new ArrayList<DecisionEnginePlugin>();
    private List<DataProcessor> dataModules = new ArrayList<DataProcessor>();
    private DecisionEnginePlugin currentDecisionEngine = null;
    private DataProcessor currentDataModule = null;

    //Observed by DEs
    private File source = null;
    private File target = null;

    //Observed field in DEs


    //GUI
    private final JFrame frame = new JFrame("HIDS");
    private final JButton sourceBtn = new JButton("Source");
    private final JButton targetBtn = new JButton("Target");
    private final JButton preProcBtn = new JButton("Pre-process");
    private final JLabel sourcePathL = new JLabel();
    private final JLabel targetPathL = new JLabel();

    private final JFileChooser fc = new JFileChooser();


    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
        setChanged();
        notifyObservers("source");
    }

    public File getTarget() {
        return target;
    }

    public void setTarget(File target) {
        this.target = target;
        setChanged();
        notifyObservers("target");
    }


    public static void main(String[] args) {

        HIDS hids = new HIDS();
        if (!hids.moduleInit()) {
            //TODO - HANDLE HIDS INITIALISATION ERROR HERE + POPUP ERROR MSG + CALL INITIALISE AGAIN
            System.out.println("Initialisation unsuccessful!");
        }

        /*for (DecisionEnginePlugin de : hids.decisionEngines) {
            System.out.println("de in main: " + de.pluginName());
        }
        for(DataProcessor dp : hids.dataModules){
            System.out.println("dm in main: " + dp.getClass().getName());
        }
        System.out.println("current de: " + hids.currentDecisionEngine.getClass().getName());
        System.out.println("current dm: " + hids.currentDataModule.getClass().getName());


        hids.setSource(new File(configPath));
        hids.setTarget(new File(configPath + "1"));*/


        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                hids.initGUI();
            }
        });
    }

    private boolean moduleInit() {
        Properties props = this.loadProperties(configPath);
        if (props != null) {
            // return(loadModules(this, props) && loadDataModules(this, props));
            /*return(loadModules(this, props, "dePlugin") &&
            loadModules(this, props, "dataModule"));*/
            if (loadModules(this, props, "dePlugin") &&
                    loadModules(this, props, "dataModule")) {
                currentDecisionEngine = decisionEngines.get(0);
                currentDataModule = dataModules.get(0);
                //TODO SUBSCRIBE ALL DES TO FILE CHANGES HERE
                //TODO SUBSCRIBE HIDS TO CHANGES IN DES
                for (DecisionEnginePlugin de : decisionEngines) {
                    System.out.println("adding " + de.getClass().getName() + " as observer");
                    addObserver(de);
                }


                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private Properties loadProperties(String path) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(path);
            prop.load(input);
            return prop;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    //TODO - EXCEPTION HANDLING!!!
    private boolean loadModules(HIDS hids, Properties props, String propName) {

        ClassLoader classLoader = HIDS.class.getClassLoader();
        try {
            String[] decisionEngines = props.getProperty(propName).trim().split("\\s*,\\s*");
            for (String de : decisionEngines) {
                //get constructor params
                if (props.containsKey(de)) {
                    String[] params = props.getProperty(de).trim().split("\\s*,\\s*");
                    if (params.length > 0) {
                        Pair<Constructor<?>, Object[]> pair = getMultiParamConst(de, params, classLoader);
                        if (pair == null) {
                            return false;
                        }
                        if (propName == "dePlugin") {
                            DecisionEnginePlugin plugin = (DecisionEnginePlugin) pair.getKey().newInstance(pair.getValue());
                            hids.decisionEngines.add(plugin);
                        } else if (propName == "dataModule") {
                            DataProcessor dataProcessor = (DataProcessor) pair.getKey().newInstance(pair.getValue());
                            hids.dataModules.add(dataProcessor);
                        }
                    }
                } else {
                    Class c = classLoader.loadClass(de);
                    if (propName == "dePlugin") {
                        DecisionEnginePlugin plugin = (DecisionEnginePlugin) c.newInstance();
                        hids.decisionEngines.add(plugin);
                    } else if (propName == "dataModule") {
                        DataProcessor dataProcessor = (DataProcessor) c.newInstance();
                        hids.dataModules.add(dataProcessor);
                    }
                }
            }

            if (propName == "dePlugin") {
                if (hids.decisionEngines.size() > 0) {
                    //hids.currentDecisionEngine = hids.decisionEngines.get(0);
                    return true;
                }
            } else if (propName == "dataModule") {
                if (hids.dataModules.size() > 0) {
                    //hids.currentDataModule = hids.dataModules.get(0);
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Pair<Constructor<?>, Object[]> getMultiParamConst(String deName, String[] params, ClassLoader classLoader) {
        try {
            List<Class> classes = new ArrayList<>();
            List<Object> args = new ArrayList<>();
            for (String p : params) {
                Class pClass = classLoader.loadClass(p);
                classes.add(pClass);
                Object instance = pClass.newInstance();
                args.add(instance);
            }

            Class[] classArr = classes.toArray(new Class[classes.size()]);
            Object[] objArr = args.toArray(new Object[args.size()]);

            Class<?> c = classLoader.loadClass(deName);
            Constructor<?> ctor = c.getConstructor(classArr);
            return new Pair<>(ctor, objArr);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void initGUI() {

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 1000);
        frame.setLayout(new BorderLayout(3, 3));

        JLabel blueLabel = new JLabel();
        blueLabel.setOpaque(true);
        blueLabel.setBackground(new Color(70, 130, 180));
        blueLabel.setPreferredSize(new Dimension(800, 500));


        JPanel buttonP = new JPanel();
        buttonP.setLayout(new BoxLayout(buttonP, BoxLayout.Y_AXIS));





        /*Container srcCont = new Container();
        srcCont.setLayout(new FlowLayout(FlowLayout.LEFT));
        //sourceBtn.setMaximumSize(new Dimension(10,sourceBtn.getMinimumSize().height));
        srcCont.add(sourceBtn);
        srcCont.add(sourcePathL);
        sourcePathL.setColumns(50);
        sourcePathL.setEditable(false);*/

        //buttonP.add(srcCont);
        BtnListener listener = new BtnListener();
        buttonP.add(setupBtn(sourceBtn, sourcePathL, true, listener));
        buttonP.add(setupBtn(targetBtn, targetPathL, true, listener));
        buttonP.add(setupBtn(preProcBtn, null, false, listener));


       /* Container targetCont = new Container();
        targetCont.setLayout(new FlowLayout(FlowLayout.LEFT));
        targetCont.add(targetBtn);
        targetCont.add(targetPathL);
        targetPathL.setColumns(50);
        targetPathL.setEditable(false);
        buttonP.add(targetCont);

        Container preprocCont = new Container();
        preprocCont.setLayout(new FlowLayout(FlowLayout.LEFT));
        preprocCont.add(preProcBtn);*/
        //      preprocCont.setSize(new Dimension(100,50));
        //buttonP.add(preprocCont);




    /*    //setup buttons here
        sourceBtn.setPreferredSize(new Dimension(10,5));

        //sourceBtn.setActionCommand(sourceBtn.getText());

        buttonP.add(sourceBtn);
        buttonP.add(sourcePathL);
        buttonP.add(targetBtn);
        buttonP.add(targetPathL);
        buttonP.add(preProcBtn);

        */
        buttonP.setBorder(BorderFactory.createLineBorder(Color.black));
        frame.add(buttonP, BorderLayout.NORTH);

        frame.getContentPane().add(blueLabel, BorderLayout.CENTER);

        Option<DecisionEngineGUI> de = currentDecisionEngine.getGUI();

        if (de.nonEmpty()) {
            frame.getContentPane().add(de.get().getGUIComponent().get(), BorderLayout.CENTER);
        }


        //Set the menu bar and add the label to the content pane.


        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private Container setupBtn(JButton btn, JLabel label, Boolean hasField, BtnListener listener) {
        Container cnt = new Container();
        cnt.setLayout(new FlowLayout(FlowLayout.LEFT));
        cnt.add(btn);
        btn.addActionListener(listener);
        if (hasField) {
            cnt.add(label);
            label.setText("...");
            //label.setEditable(false);
        }
        return cnt;
    }

    private class BtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            // Need to determine which button fired the event.
            // the getActionCommand() returns the Button's label
            System.out.println("evt src: " + evt.getSource());
            String btnLabel = evt.getActionCommand();
            btnHandler(btnLabel);
        }

        private void btnHandler(String btnLabel){

            String currDir = "...";
            try {
                if(btnLabel.equals("Source") && (source != null)) { currDir = getSource().getCanonicalPath(); }
                else if(btnLabel.equals("Target") && (target != null)) {  currDir = target.getCanonicalPath(); }

                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int returnVal = fc.showOpenDialog(HIDS.this.frame);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if(btnLabel.equals("Source")){
                        HIDS.this.setSource(file);
                        sourcePathL.setText(file.getCanonicalPath());
                    }else if(btnLabel.equals("Target")){
                        HIDS.this.setTarget(file);
                        targetPathL.setText(file.getCanonicalPath());
                    }
                } else {
                    //TODO - DELETE BELOW
                    System.out.println("source but not Approve Option.");
                }
            } catch (IOException e) {
                if(btnLabel.equals("Source")){
                    sourcePathL.setText(currDir);
                }else if(btnLabel.equals("Target")){
                    targetPathL.setText(currDir);
                }
                e.printStackTrace();
            }
        }
    }
}
