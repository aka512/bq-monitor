package de.akalla.bqmonitor.logaccess;

import java.io.File;

import org.apache.log4j.Logger;

import de.akalla.bqmonitor.MonitorMain;
import de.akalla.bqmonitor.gcloudapi.JobController;
import de.akalla.bqmonitor.util.Utils;

/**
 * @author Andreas Kalla, Feb. 2020
 */
public class LogAccessController {
    private static final Logger log = Logger.getLogger(LogAccessController.class);

    private int cntstartline = 0;
    private int cntendline =0;

    private JobController jobController;
    private File logDir;

    public LogAccessController(File logDir, JobController jobController) {
        this.logDir = logDir;
        this.jobController = jobController;

    }

    /**
     * sets up and start the main log dir daemon thread, that will start more file
     * Tailer Threads on each new or modified file within the folder automatically.
     */
    public void startScanning() {
        try {
            LogFilesWatcherRunnable filesWatcher = new LogFilesWatcherRunnable(logDir, this);
            Thread scan = new Thread(filesWatcher);
            scan.setDaemon(true);
            scan.start();

        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            Utils.notifyUserAboutException(e);
        }
    }

    /**
     * This function will be called with new log data from the file watcher threads,
     * we convert it here to a JSON object, Analyse it and notify the API to load
     * new data.
     * 
     * The API is called only when a begin-protocol.query or end-protocol.query
     * indentifier is found, becuase there is a lot of noise in the log
     * 
     * @param contextFile
     * @param lineText
     */
    public void handleNewLogLine(File contextFile, String lineText) {
        try {
            // make sure we are in a JSON input
            if (lineText != null && lineText.startsWith(MonitorMain.JSON_START_IDENTIFIER)) {

                // check the expected type of this line
                boolean isStartMarker;
                if (lineText.contains(MonitorMain.BEGIN_QUERYMARKER)) {
                    isStartMarker = true;
                    cntstartline++;
                    log.debug("line ist startline "+ cntstartline +" >>"+lineText+"<<");

                } else if (lineText.contains(MonitorMain.END_QUERYMARKER)) {
                    isStartMarker = false;
                    cntendline++;
                    log.debug("line ist endline "+ cntendline +" >>"+lineText+"<<");
                } else {
                    return; // a not relevant log line is not being processed
                }

                log.debug("handleNewLogLine (isStartMarker="+isStartMarker+") in file=" + contextFile.getName());
                jobController.insertJob(lineText, isStartMarker);
            }

        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            Utils.notifyUserAboutException(e);
        }
    }

}
