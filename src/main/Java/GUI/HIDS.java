package GUI;

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

    private List<DecisionEnginePlugin> plugins = new ArrayList<DecisionEnginePlugin>();

    public static void main(String[] args) {

        HIDS hids = new HIDS();
        String configPath = new File("").getAbsolutePath() + "\\src\\main\\resources\\config.properties";
        //System.out.println(System.getProperty("user.dir"));
        Properties props = hids.loadProperties(configPath);
        if (props != null) {
            loadPlugins(hids, props);
        }
        for (DecisionEnginePlugin de : hids.plugins) {
            System.out.println("de in main: " + de.pluginName());
        }


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

    private static void loadPlugins(HIDS hids, Properties props) {

        ClassLoader classLoader = HIDS.class.getClassLoader();
        try {
            String[] decisionEngines = props.getProperty("dePlugin").trim().split("\\s*,\\s*");
            for (String s : decisionEngines) {
                Class c = classLoader.loadClass(s);
                DecisionEnginePlugin smtPlugin = (DecisionEnginePlugin) c.newInstance();
                hids.plugins.add(smtPlugin);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
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
}
