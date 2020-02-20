package de.akalla.bqmonitor.logaccess;

import java.io.File;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.log4j.Logger;

/**
 * Handles a line by the commons io Tailer service dispatches the information to
 * the LogAccess-callback where it will trigger further actions
 *
 * @author Andreas Kalla, Feb. 2020
 */
public class LogTailerListener extends TailerListenerAdapter {
    private static final Logger log = Logger.getLogger(LogTailerListener.class);

    private File file;
    private LogAccessController logAccess;

    public LogTailerListener(File file, LogAccessController logAccess) {
        this.file = file;
        this.logAccess = logAccess;
    }

    /**
     * Call to the logAccess main starting point, what will inspect the line if it
     * is in this context a useful log entry line (there are lot of noise in the log
     * file) and then call loading of the next item from the API Service, what then
     * itself will show it to the GUI list.
     */
    @Override
    public void handle(String line) {
        logAccess.handleNewLogLine(file, line);
    }

    // error situations logging, should not happen normally:
    @Override
    public void fileNotFound() {
        log.error("file not found");
    }

    @Override
    public void fileRotated() {
        log.error("file rotated");
    }

    @Override
    public void handle(Exception e) {
        log.error(e);
    }

    // some special situations below, may be removed:
    @Override
    public void init(Tailer tailer) {
    }

    @Override
    public void endOfFileReached() {
    }

}
