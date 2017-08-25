package GUI;

import DecisionEngine.DecisionEnginePlugin;


import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/* HIDS.java requires no other files. */
public class HIDS {


    //private List plugins = new ArrayList<DecisionEnginePlugin>();







    private static void startGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("HIDS");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,500);

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

    public static void main(String[] args) {
        String configPath = new File("").getAbsolutePath() + "\\src\\main\\resources\\config.properties";
        //System.out.println(System.getProperty("user.dir"));
        System.out.println("configPath: " + configPath);
        Properties props = loadProperties(configPath);
        if(props != null){
            loadPlugins(props);
        }







      /*  javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                startGUI();
            }
        });*/
    }

    private static void loadPlugins(Properties props){

        ClassLoader classLoader = HIDS.class.getClassLoader();

        try {
            System.out.println(System.getProperty("user.dir"));
            //
             Class aClass = classLoader.loadClass(props.getProperty("dePlugin"));
             System.out.println("aClass name: " + aClass.getName());

            System.out.println(Class.forName(props.getProperty("dePlugin")).getName());
            //System.out.println(Class.forName("DecisionEngine.SMT.SparseMarkovTree"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Properties loadProperties(String path){
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(path);

            // load a properties file
            prop.load(input);
            return prop;
            // get the property value and print it out
            //System.out.println(prop.getProperty("dePlugin"));

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
