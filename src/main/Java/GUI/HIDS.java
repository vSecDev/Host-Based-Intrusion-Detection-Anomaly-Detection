package GUI;

import Data.DataModel;
import Data.DataProcessor;
import Data.DataWrapper;
import DecisionEngine.DecisionEngineGUI;
import DecisionEngine.DecisionEnginePlugin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
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

import scala.None$;
import scala.Option;
import scala.collection.immutable.Vector;

//public class HIDS extends Observable implements Observer {
public class HIDS extends Observable implements Observer {

    private static final String configPath = new File("").getAbsolutePath() + "\\src\\main\\resources\\config.properties";
    private Properties props = null;
    private String[] extensions = new String[0];
    private String[] delimiters = new String[0];
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

    private void setSource(File source) {
        this.source = source;
        setChanged();
        notifyObservers("source");
    }

    public File getTarget() {
        return target;
    }

    private void setTarget(File target) {
        this.target = target;
        setChanged();
        notifyObservers("target");
    }

    public String[] getDelimiters() {
        return delimiters;
    }

    private void setDelimiters(String[] delimiters) {
        this.delimiters = delimiters;
    }

    public String[] getExtensions() {
        return extensions;
    }

    private void setExtensions(String[] extensions) {
        this.extensions = extensions;
    }

    public static void main(String[] args) {

        HIDS hids = new HIDS();
        hids.loadProperties(configPath);
        if (!(hids.moduleInit() && hids.extensionsInit() && hids.delimitersInit())) {
            System.out.println("here");
            hids.showError("An error occurred during initialisation!", "Error");
            //JOptionPane.showMessageDialog(new JPanel(), "An error occurred during initialisation!", "Error", JOptionPane.ERROR_MESSAGE);
        }


        for(String s : hids.getExtensions()){
            System.out.println("extension:{" + s + "}");
        }
        for(String s : hids.getDelimiters()){
            System.out.println("delimiter:{" + s + "}");
        }



        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                hids.initGUI();
            }
        });
    }

    private boolean moduleInit() {
        //Properties props = this.loadProperties(configPath);
        if (props != null) {
            if (loadModules(this, props, "dePlugin") &&
                    loadModules(this, props, "dataModule")) {
                currentDecisionEngine = decisionEngines.get(0);
                currentDecisionEngine.registerHIDS(this);
                currentDataModule = dataModules.get(0);
                //TODO SUBSCRIBE ALL DES TO FILE CHANGES HERE
                //TODO SUBSCRIBE HIDS TO CHANGES IN DES
                for (DecisionEnginePlugin de : decisionEngines) {
                    System.out.println("adding " + de.getClass().getName() + " as observer");
                    addObserver(de);
                }
                return true;
            }
        }
        return false;
    }

    private boolean extensionsInit() {
        if (props != null) {
            try {
                setExtensions(props.getProperty("extensions").trim().split("\\s*,\\s*"));
                return getExtensions() != null && getExtensions().length > 0;
            } catch (NullPointerException e){
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private boolean delimitersInit() {
        if (props != null) {
            try {
                List<String> delimiters = new ArrayList<String>(Arrays.asList(props.getProperty("delimiters").trim().split("\\s*-\\s*")));
                delimiters.add(" ");
                String[] list2 = new String[delimiters.size()];
                setDelimiters(delimiters.toArray(list2));
                return getDelimiters() != null && getDelimiters().length > 0;
            } catch (NullPointerException e){
                System.out.println("nullpointerexception thrown");
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private Properties loadProperties(String path) {
        //Properties prop = new Properties();
        props = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(path);
            props.load(input);
            return props;
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
                return hids.decisionEngines.size() > 0;
            } else if (propName == "dataModule") {
                return hids.dataModules.size() > 0;
            }
            return false;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NullPointerException e) {
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
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InstantiationException e) {
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

        BtnListener listener = new BtnListener();
        buttonP.add(setupBtn(sourceBtn, sourcePathL, true, listener));
        buttonP.add(setupBtn(targetBtn, targetPathL, true, listener));
        buttonP.add(setupBtn(preProcBtn, null, false, listener));

        buttonP.setBorder(BorderFactory.createLineBorder(Color.black));
        frame.add(buttonP, BorderLayout.NORTH);
        frame.getContentPane().add(blueLabel, BorderLayout.CENTER);
        renderBtns();

        Option<DecisionEngineGUI> de = currentDecisionEngine.getGUI();

        if (de.nonEmpty()) {
            frame.getContentPane().add(de.get().getGUIComponent().get(), BorderLayout.CENTER);
        }

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private Boolean canPreProcess(){
        return (getSource() != null &&
                getTarget() != null &&
                getSource().isDirectory() &&
                getTarget().isDirectory() &&
                currentDataModule != null);
    }

    private void renderBtns(){
        preProcBtn.setEnabled(canPreProcess());
    }

    private Container setupBtn(JButton btn, JLabel label, Boolean hasField, BtnListener listener) {
        Container cnt = new Container();
        cnt.setLayout(new FlowLayout(FlowLayout.LEFT));
        cnt.add(btn);
        btn.addActionListener(listener);
        if (hasField) {
            cnt.add(label);
            label.setText("...");
        }
        return cnt;
    }

    private void showError(String txt, String title) {
        JOptionPane.showMessageDialog(new JPanel(), txt, title, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("nodified. arg: " + arg);
        if (arg.toString().equals("learn")) {
            if (currentDataModule == null ||
                    source == null ||
                    extensions.length == 0 ||
                    delimiters.length == 0) {
                showError("An error occurred during learn request processing", "Error");
            }else{
                if(source.isDirectory()){
                   Option<Vector<DataWrapper>> input = currentDataModule.getAllData(source, extensions);
                   if(input.isEmpty()){
                       showError("An error occurred during data processing! No input data is sent to the Decision Engine.", "Error");
                   }else{
                       Boolean isInt = currentDecisionEngine.getGUI().get().isSetToInt();
                       scala.Option<DataModel> none = scala.Option.apply(null);
                       Option<DataModel> trainedModel = currentDecisionEngine.learn(input.get(), none, isInt);
                       if(trainedModel.isEmpty()){
                           System.out.println("empty model returned after learning");
                       }else{
                           System.out.println("Trained model: " + (trainedModel.get()).toString());
                           System.out.println("--- add code to save returned model!");
                       }
                   }
                } else {
                    //source is a file

                    Option<DataWrapper> in = currentDataModule.getData(source, extensions);
                    if(in.isEmpty()){
                        showError("An error occurred during data processing! No input data is sent to the Decision Engine.", "Error");
                    }else{
                        Vector<DataWrapper> input = new Vector<DataWrapper>(0,0,0);
                        input.appendBack(in.get());
                        Boolean isInt = currentDecisionEngine.getGUI().get().isSetToInt();
                        scala.Option<DataModel> none = scala.Option.apply(null);
                        Option<DataModel> trainedModel = currentDecisionEngine.learn(input, none, isInt);
                        if(trainedModel.isEmpty()){
                            System.out.println("empty model returned after learning");
                        }else{
                            System.out.println("Trained model: " + trainedModel.get().toString());
                            System.out.println("--- add code to save returned model!");
                        }
                    }



                }
            }
        }
    }

    private class BtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            String btnLabel = evt.getActionCommand();
            if(btnLabel.equals("Pre-process")){
                preProcessHandler();
            }else {
                srcTargetBtnHandler(btnLabel);
            }
            renderBtns();
        }

        private void srcTargetBtnHandler(String btnLabel){

            String currDir = "...";
            try {
                if(btnLabel.equals("Source") && (getSource() != null)) { currDir = getSource().getCanonicalPath(); }
                else if(btnLabel.equals("Target") && (getTarget() != null)) {  currDir = getTarget().getCanonicalPath(); }

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

        private void preProcessHandler(){
            currentDataModule.preprocess(getSource(), getTarget(), getDelimiters(), getExtensions()).get();
        }
    }
}
