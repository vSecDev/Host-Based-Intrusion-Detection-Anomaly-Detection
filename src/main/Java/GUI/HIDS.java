package GUI;

import Data.DataException;
import Data.DataModel;
import Data.DataProcessor;
import Data.DataWrapper;
import DecisionEngine.DecisionEngineGUI;
import DecisionEngine.DecisionEnginePlugin;
import DecisionEngine.DecisionEngineReport;
import javafx.util.Pair;
import scala.Option;
import scala.collection.immutable.Vector;
import sun.misc.Launcher;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class HIDS extends Observable implements Observer {

 //   private static final String configPath = new File("").getAbsolutePath() + "\\src\\main\\resources\\config.properties";
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
        //hids.loadProperties(configPath);
        hids.loadProperties();
        if (!(hids.moduleInit() && hids.extensionsInit() && hids.delimitersInit())) {
            hids.showError("An error occurred during initialisation!", "Error");
        }

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                hids.initGUI();
            }
        });
    }

    private boolean moduleInit() {
        if (props != null) {
            if (loadModules(this, props, "dePlugin") &&
                    loadModules(this, props, "dataModule")) {
                currentDecisionEngine = decisionEngines.get(0);
                currentDecisionEngine.registerHIDS(this);
                currentDataModule = dataModules.get(0);
                for (DecisionEnginePlugin de : decisionEngines) {
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
            } catch (NullPointerException e) {
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
            } catch (NullPointerException e) {
                return false;
            }
        }
        return false;
    }

    //private Properties loadProperties(String path) {
    private Properties loadProperties() {
        props = new Properties();
        InputStream input = null;
        try {
            //input = new FileInputStream(path);
            //input = new FileInputStream(configPath);
            input = Launcher.class.getResourceAsStream("/config.properties");

            props.load(input);
            return props;
        } catch (IOException ex) {
            showError("An error occurred during initialisation!", "Error");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    showError("An error occurred during initialisation!", "Error");
                }
            }
        }
        return null;
    }

    private boolean loadModules(HIDS hids, Properties props, String propName) {

        ClassLoader classLoader = HIDS.class.getClassLoader();
        try {
            String[] modules = props.getProperty(propName).trim().split("\\s*,\\s*");
            for (String de : modules) {
                //get constructor params
                if (props.containsKey(de)) {
                    String[] params = props.getProperty(de).trim().split("\\s*,\\s*");
                    if (params.length > 0) {
                        Pair<Constructor<?>, Object[]> pair = getMultiParamConst(de, params, classLoader);
                        if (pair == null) {
                            return false;
                        }
                        if (propName.equals("dePlugin")) {
                            DecisionEnginePlugin plugin = (DecisionEnginePlugin) pair.getKey().newInstance(pair.getValue());
                            hids.decisionEngines.add(plugin);
                        } else if (propName.equals("dataModule")) {
                            DataProcessor dataProcessor = (DataProcessor) pair.getKey().newInstance(pair.getValue());
                            hids.dataModules.add(dataProcessor);
                        }
                    }
                } else {
                    Class c = classLoader.loadClass(de);
                    if (propName.equals("dePlugin")) {
                        DecisionEnginePlugin plugin = (DecisionEnginePlugin) c.newInstance();
                        hids.decisionEngines.add(plugin);
                    } else if (propName.equals("dataModule")) {
                        DataProcessor dataProcessor = (DataProcessor) c.newInstance();
                        hids.dataModules.add(dataProcessor);
                    }
                }
            }

            if (propName.equals("dePlugin")) {
                return hids.decisionEngines.size() > 0;
            } else if (propName.equals("dataModule")) {
                return hids.dataModules.size() > 0;
            }
            return false;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NullPointerException e) {
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
            return null;
        }
    }

    private void initGUI() {

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 1000);
        frame.setLayout(new BorderLayout(3, 20));
        ((JComponent)frame.getContentPane()).setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        frame.getContentPane().setBackground(new Color(240,248,255));

        JPanel buttonP = new JPanel();
        buttonP.setLayout(new BoxLayout(buttonP, BoxLayout.Y_AXIS));
        BtnListener listener = new BtnListener();
        buttonP.add(setupBtn(sourceBtn, sourcePathL, true, listener));
        buttonP.add(setupBtn(targetBtn, targetPathL, true, listener));
        buttonP.add(setupBtn(preProcBtn, null, false, listener));
        buttonP.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        frame.getContentPane().add(buttonP, BorderLayout.NORTH);
        renderBtns();

        Option<DecisionEngineGUI> de = currentDecisionEngine.getGUI();
        if (de.nonEmpty()) {
            frame.getContentPane().add(de.get().getGUIComponent().get(), BorderLayout.CENTER);
        }
        frame.pack();
        frame.setVisible(true);
    }

    private Boolean canPreProcess() {
        return (getSource() != null &&
                getTarget() != null &&
                getSource().isDirectory() &&
                getTarget().isDirectory() &&
                currentDataModule != null);
    }

    private void renderBtns() { preProcBtn.setEnabled(canPreProcess()); }

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

    private void showError(String txt, String title) { JOptionPane.showMessageDialog(new JPanel(), txt, title, JOptionPane.ERROR_MESSAGE); }

    @Override
    public void update(Observable o, Object arg) {
        String action = arg.toString();
        if (action.equals("learn") || action.equals("classify") || action.equals("validate")) {
            if (currentDataModule == null ||
                    source == null ||
                    extensions.length == 0 ||
                    delimiters.length == 0) {
                showError("An error occurred during " + action + " request processing", "Error");
            } else {
                if (source.isDirectory()) {
                    Option<Vector<DataWrapper>> input = currentDataModule.getAllData(source, extensions);
                    if (input.isEmpty()) {
                        showError("An error occurred during data processing! No input data is sent to the Decision Engine.", "Error");
                    } else {
                        Boolean isInt = currentDecisionEngine.getGUI().get().isSetToInt();
                        scala.Option<DataModel> none = scala.Option.apply(null);

                        if (action.equals("learn")) {
                            handleLearn(input.get(), none, isInt);
                        } else if (action.equals("classify")) {
                            handleClassify(input.get(), none, isInt);
                        } else if (action.equals("validate")) {
                            handleValidate(input.get(), none, isInt);
                        }
                    }
                } else {
                    Option<DataWrapper> in = currentDataModule.getData(source, extensions);
                    if (in.isEmpty()) {
                        showError("An error occurred during data processing! No input data is sent to the Decision Engine.", "Error");
                    } else {
                        Vector<DataWrapper> input = new Vector<DataWrapper>(0, 0, 0);
                        input = input.appendBack(in.get());
                        Boolean isInt = currentDecisionEngine.getGUI().get().isSetToInt();
                        scala.Option<DataModel> none = scala.Option.apply(null);

                        if (action.equals("learn")) {
                            handleLearn(input, none, isInt);
                        } else if (action.equals("classify")) {
                            handleClassify(input, none, isInt);
                        } else if (action.equals("validate")) {
                            handleValidate(input, none, isInt);
                        }
                    }
                }
            }
        } else if (action.equals("loadModel")) {
            if (currentDataModule == null || source == null || source.isDirectory()) {
                showError("An error occurred during 'Load Model' request processing.\nCheck if source is set and is a file!", "Error");
            } else {
                handleLoadModel();
            }
        } else if (action.equals("saveModel") || action.equals("saveReport")) {
            if (currentDecisionEngine == null || currentDataModule == null || target == null || target.isFile()) {
                showError("An error occurred during 'Save' request processing.\nCheck if target is set and is a folder!", "Error");
            } else {
                if (action.equals("saveModel")) {
                    handleSaveModel();
                } else if (action.equals("saveReport")) {
                    handleSaveReport();
                }
            }
        }
    }

    private Option<DataModel> handleLearn(Vector<DataWrapper> input, Option<DataModel> model, boolean isInt) {
        try {
            Option<DataModel> trainedModel = currentDecisionEngine.learn(input, model, isInt);
            return trainedModel;
        }catch(DataException de){
            showError("An error occurred during learning. Please check the SMT parameters!\nLarge MaxDepth/MaxPhi and small Maximum Sequence count may cause the tree to become too large!", "Error");
            return scala.Option.apply(null);
        }
    }

    private Option<DecisionEngineReport> handleClassify(Vector<DataWrapper> input, Option<DataModel> model, boolean isInt){
        return currentDecisionEngine.classify(input, model, isInt);
    }

    private Option<DecisionEngineReport> handleValidate(Vector<DataWrapper> input, Option<DataModel> model, boolean isInt){
        return currentDecisionEngine.validate(input, model, isInt);
    }

    private void handleLoadModel() {
        DataModel dm = new DataModel();
        try {
            Option<DataModel> dmo = currentDataModule.loadModel(dm, source);
            if (dmo.isEmpty()) {
                showError("An error occurred during 'Load Model' request processing.\nNo model was returned by the Data Processor module.", "Error");
            } else {
                dm = dmo.get();
                if (!currentDecisionEngine.loadModel(dm, currentDecisionEngine.getGUI().get().isSetToInt())) {
                    showError("The Decision Engine could not load the model!", "Error");
                }
            }
        } catch (DataException de) {
            showError("An error occurred during 'Load Model' request processing.\nNo model was returned by the Data Processor module.", "Error");
        }
    }

    private void handleSaveModel() {
        Option<DataModel> dm = currentDecisionEngine.saveModel();
        if (dm.nonEmpty()) {
            try {
                Option<String> fileName = currentDecisionEngine.getModelName();
                if (fileName.nonEmpty()) {
                    File targetFile = new File(target.getCanonicalPath() + "\\" + fileName.get() + ".IDM");
                    if (currentDataModule.saveModel(dm.get(), targetFile)) {
                        JOptionPane.showMessageDialog(new JPanel(), "The model has been saved to disk!",
                                "Information", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        showError("The Data Processor module could not save the model!", "Error");
                    }
                } else {
                    showError("An error occurred during 'Save Model' request processing.", "Error");
                }
            } catch (DataException | IOException e) {
                showError("The Data Processor module could not save the model!", "Error");
            }
        } else {
            showError("An error occurred during 'Save Model' request processing.\nNo model was returned by the Decision Engine.", "Error");
        }
    }

    private void handleSaveReport() {
        Option<DecisionEngineReport> der = currentDecisionEngine.saveReport();
        if (der.nonEmpty()) {
            try {
                Option<String> fileName = currentDecisionEngine.getReportName();
                if (fileName.nonEmpty()) {
                    File targetFile = new File(target.getCanonicalPath() + "\\" + fileName.get() + ".IDR");
                    if (currentDataModule.saveReport(der.get(), targetFile)) {
                        JOptionPane.showMessageDialog(new JPanel(), "The report has been saved to disk!",
                                "Information", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        showError("The Data Processor module could not save the report!", "Error");
                    }
                } else {
                    showError("An error occurred during 'Save Report' request processing.", "Error");
                }
            } catch (DataException | IOException e) {
                showError("The Data Processor module could not save the report!", "Error");
            }
        } else {
            showError("An error occurred during 'Save Report' request processing.\nNo report was returned by the Decision Engine.", "Error");
        }
    }

    private class BtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            String btnLabel = evt.getActionCommand();
            if (btnLabel.equals("Pre-process")) {
                preProcessHandler();
            } else {
                srcTargetBtnHandler(btnLabel);
            }
            renderBtns();
        }

        private void srcTargetBtnHandler(String btnLabel) {
            String currDir = "...";
            try {
                if (btnLabel.equals("Source") && (getSource() != null)) {
                    currDir = getSource().getCanonicalPath();
                } else if (btnLabel.equals("Target") && (getTarget() != null)) {
                    currDir = getTarget().getCanonicalPath();
                }

                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int returnVal = fc.showOpenDialog(HIDS.this.frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (btnLabel.equals("Source")) {
                        if (getTarget() != null && Files.isSameFile(file.toPath(), getTarget().toPath())) {
                            showError("Source file or directory must be different from Target!", "Warning");
                        } else {
                            HIDS.this.setSource(file);
                            sourcePathL.setText(file.getCanonicalPath());
                        }
                    } else if (btnLabel.equals("Target")) {
                        if (getSource() != null && Files.isSameFile(file.toPath(), getSource().toPath())) {
                            showError("Target file or directory must be different from Source!", "Warning");
                        } else {
                            HIDS.this.setTarget(file);
                            targetPathL.setText(file.getCanonicalPath());
                        }
                    }
                }
            } catch (IOException e) {
                if (btnLabel.equals("Source")) {
                    sourcePathL.setText(currDir);
                } else if (btnLabel.equals("Target")) {
                    targetPathL.setText(currDir);
                }
                //e.printStackTrace();
            }
        }

        private void preProcessHandler() {
            currentDataModule.preprocess(getSource(), getTarget(), getDelimiters(), getExtensions()).get();
        }
    }
}