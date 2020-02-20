package de.akalla.bqmonitor.logaccess;

import java.io.File;

import org.apache.commons.io.input.Tailer;
import org.apache.log4j.Logger;

import com.google.common.util.concurrent.Monitor;

import de.akalla.bqmonitor.MonitorMain;
import de.akalla.bqmonitor.util.Utils;

/**
 *
 * @author Andreas Kalla, Feb. 2020
 */
public class LogTailerRunnable implements Runnable {
    private static final Logger log = Logger.getLogger(LogTailerRunnable.class);

    private LogAccessController logAccess;
    private String fileFullPath; // Coming from Path to File Class by String..

    public LogTailerRunnable(String fileFullPath, LogAccessController logAccess) {
        this.logAccess = logAccess;
        this.fileFullPath = fileFullPath;
    }

    /**
     * Runs the this Tailer thread for a single log file.
     *
     * NOTICE that run instead of start is called here, because we are inside an own
     * thread already
     * 
     * When the Path is not valid for any reason a message is shown to the user.
     */
    @Override
    public void run() {
        try {
            File file = new File(fileFullPath);
            if (file.isFile() && file.canRead()) {
                LogTailerListener listener = new LogTailerListener(file, logAccess);
                Tailer tailer = new Tailer(file, listener, MonitorMain.TAILER_DELAY_MS);
                tailer.run();

            } else {
                // this should not happen
                throw new RuntimeException("Can not read logfile: " + fileFullPath);

            }

        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            Utils.notifyUserAboutException(e);
        }

    }

}
