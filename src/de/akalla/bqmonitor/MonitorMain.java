package de.akalla.bqmonitor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import de.akalla.bqmonitor.gui.MonitorController;
import de.akalla.bqmonitor.util.Utils;

public class MonitorMain {
    static final Logger log = Logger.getLogger(MonitorMain.class);

    // configurations used while starting up the application
    private static final String KEY_DEFAULT_PATH = "key.json"; // default name for the Google Cloud API key
    private static final String ENV_TABLEAU_LOG_DIR = "TABLEAU_LOG_DIR"; // name of the ENV Variable

    // configurations used inside the Log Scanner Part
    public static final long TAILER_DELAY_MS = 150; // the interval for searching the log files in milliseconds
    public static final Pattern LOG_PATTERN = Pattern.compile("^tabprotosrv_.*"); // scan only the relevant logs filename
    public static final CharSequence BEGIN_QUERYMARKER = "begin-protocol.query";
    public static final CharSequence END_QUERYMARKER = "end-protocol.query";
    public static final String JSON_START_IDENTIFIER = "{";

    // configurations for GUI
    public static final int LOAD_MAXJOBS_INITIAL = 20;


    private JFileChooser filechooser;
    private File keyFile = null;
    private File tablLogDirFile = null;
    private JFrame parent = new JFrame(); // is needed to dispose the application correctly

    /**
     * main starting point
     * the args are not used but the user will be prompted for parameters
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            new MonitorMain().start();

        } catch (Exception e) {
            Utils.notifyUserAboutException(e);
            log.error(e);
            e.printStackTrace();
        }
    }

    /**
     * main starting point continue
     */
    private void start() {
        log.info(" >>> START <<<");

        try {
            // LOGDIR: check environment variable for Log dir
            String tablLogDir = System.getenv(ENV_TABLEAU_LOG_DIR);
            if (tablLogDir != null && tablLogDir.length() > 0) {
                handleEnvironemtVarToLogDir(tablLogDir);
            }
            // otherwise get Log directory by prompt, if not correct in previous step
            if (tablLogDirFile == null || !tablLogDirFile.exists() || !tablLogDirFile.isDirectory()) {
                getTableauFolder();
            }

            // KEYFILE: check if keyfile is in current directory
            File keyFileDefault = new File(KEY_DEFAULT_PATH);
            if (keyFileDefault.exists() && !keyFileDefault.isDirectory()) {
                keyFile = keyFileDefault;
                log.info("OK: Keyfile opened by using " + KEY_DEFAULT_PATH + " in current directory.");

            }
            // otherwise get keyfile by prompt
            if (keyFile == null) {
                getKeyfile();
            }

        } catch (Exception e) {
            Utils.notifyUserAboutException(e);
            log.error(e);
            e.printStackTrace();
        }

        // Start the Application here if all pre-conditions are met
        if (tablLogDirFile != null && keyFile != null) {
            new MonitorController().start(keyFile, tablLogDirFile);

        } else {
            JOptionPane.showMessageDialog(parent,
                    "The paths were not specified correctly.\nApplication can not be started.");
        }
        parent.dispose();
    }


    /**
     * loads the tableau lof dir from environment, if set
     * also shows a message, when the specified value is not a folder
     *
     * @param tablLogDir
     */
    private void handleEnvironemtVarToLogDir(String tablLogDir) {
        tablLogDirFile = new File(tablLogDir);
        if (tablLogDirFile.exists() && tablLogDirFile.isDirectory()) {
            log.info("OK: Tableau Log opened by using environment variable " + ENV_TABLEAU_LOG_DIR + "=" + tablLogDirFile.getAbsolutePath());
        } else {
            JOptionPane.showMessageDialog(parent,
                    "ERROR: Environment variable not pointing to a valid tableau log folder.\n"
                            + ENV_TABLEAU_LOG_DIR + "=" + tablLogDirFile.getAbsolutePath());
        }
    }

    /**
     * Messages for choosing the tableau log folder directory
     *
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    private void getTableauFolder() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            JOptionPane.showMessageDialog(parent,
                    "Please specify in the next Step the Folder to the Tableau Logs.\n"
                            + "This is usually called Folder 'Protocols' in the Tableau Repository.\n"
                            + "You can automate this step by configuring the environment variable '"
                            + ENV_TABLEAU_LOG_DIR + "' in your System.");
            JFileChooser choose = getJFileChooser();
            choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            choose.setFileFilter(null);
            if (JFileChooser.APPROVE_OPTION == choose.showDialog(null, "select")) {
                tablLogDirFile = choose.getSelectedFile();
                log.info("selected logdir=" + tablLogDirFile);
            }
        });
    }

    /**
     * logic to get the key file by prompting
     *
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    private void getKeyfile() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            JOptionPane.showMessageDialog(parent,
                    "Please specify the JSON keyfile for accessing Google Cloud BigQuery API. \n"
                            + "You can automate this step by placing a file named key.json file in the current directory.");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON", "json");
            JFileChooser choose = getJFileChooser();
            choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
            choose.setFileFilter(filter);

            if (JFileChooser.APPROVE_OPTION == choose.showDialog(null, "Select")) {
                keyFile = choose.getSelectedFile();
                log.info("selected keyfile=" + keyFile);
            }
        });
    }

    /**
     * lazy initialisation of a filechooser component, to make loading a bit faster
     *
     * @return the filechooser instance
     */
    private JFileChooser getJFileChooser() {
        if (filechooser == null) {
            filechooser = new JFileChooser();
        }
        return filechooser;
    }

}
