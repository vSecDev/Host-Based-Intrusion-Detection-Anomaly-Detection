package GUI;

import Data.DataModel;
import Data.DataProcessor;
import DecisionEngine.DecisionEnginePlugin;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HIDS {

    private static final String configPath =  new File("").getAbsolutePath() + "\\src\\main\\resources\\config.properties";
    private List<DecisionEnginePlugin> decisionEngines = new ArrayList<DecisionEnginePlugin>();
    private List<DataProcessor> dataModules = new ArrayList<DataProcessor>();
    private DecisionEnginePlugin currentDecisionEngine = null;
    private DataProcessor currentDataModule = null;


    //TODO - ADD FILEPROCESSOR DATA MODULE TO CONFIG + INIT LOGIC


    public static void main(String[] args) {

        HIDS hids = new HIDS();
        if(!hids.initialise()){
            //TODO - HANDLE HIDS INITIALISATION ERROR HERE + POPUP ERROR MSG + CALL INITIALISE AGAIN

        }
        for (DecisionEnginePlugin de : hids.decisionEngines) {
            System.out.println("de in main: " + de.pluginName());
        }
        for(DataProcessor dp : hids.dataModules){
            System.out.println("dm in main: " + dp.getClass().getName());
        }
        System.out.println("current de: " + hids.currentDecisionEngine.getClass().getName());
        System.out.println("current dm: " + hids.currentDataModule.getClass().getName());





        /*javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                startGUI();
            }
        });*/
    }


    private static void startGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("HIDS");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);

        //Create the menu bar.  Make it have a green background.
        JMenuBar greenMenuBar = new JMenuBar();
        greenMenuBar.setOpaque(true);
        greenMenuBar.setBackground(new Color(154, 165, 127));
        greenMenuBar.setPreferredSize(new Dimension(800, 25));

        //Create a yellow label to put in the content pane.
        JLabel yellowLabel = new JLabel();
        yellowLabel.setOpaque(true);
        yellowLabel.setBackground(new Color(248, 213, 131));
        yellowLabel.setPreferredSize(new Dimension(800, 500));

        //Set the menu bar and add the label to the content pane.
        frame.setJMenuBar(greenMenuBar);
        frame.getContentPane().add(yellowLabel, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private boolean initialise(){
        Properties props = this.loadProperties(configPath);
        if (props != null) {
           return(loadPlugins(this, props) && loadDataModules(this, props));
        }else {return false;}
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
    private boolean loadPlugins(HIDS hids, Properties props) {

        ClassLoader classLoader = HIDS.class.getClassLoader();
        try {
            String[] decisionEngines = props.getProperty("dePlugin").trim().split("\\s*,\\s*");
            for (String s : decisionEngines) {
                Class c = classLoader.loadClass(s);
                DecisionEnginePlugin smtPlugin = (DecisionEnginePlugin) c.newInstance();
                hids.decisionEngines.add(smtPlugin);
            }
            if(hids.decisionEngines.size() > 0){
                hids.currentDecisionEngine = hids.decisionEngines.get(0);
                return true;
            }else{
                return false;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return false;
        }
    }
    private boolean loadDataModules(HIDS hids, Properties props) {

        ClassLoader classLoader = HIDS.class.getClassLoader();
        try {
            String[] dataModules = props.getProperty("dataModule").trim().split("\\s*,\\s*");
            for (String s : dataModules) {
                Class c = classLoader.loadClass(s);
                DataProcessor dataProcessor = (DataProcessor) c.newInstance();
                hids.dataModules.add(dataProcessor);
            }

            if(hids.dataModules.size() > 0){
                hids.currentDataModule = hids.dataModules.get(0);
                return true;
            }else{
                return false;
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return false;
        }
    }


}
