package de.akalla.bqmonitor.logaccess;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.akalla.bqmonitor.MonitorMain;
import de.akalla.bqmonitor.util.Utils;

/**
 * Is responsible to spawn new Threads on every wanted logfile. Those Threads
 * must call the LogAccess callback function on every new line
 *
 * @author Andreas Kalla, Feb. 2020
 */
public class LogFilesWatcherRunnable implements Runnable {
    private static final Logger log = Logger.getLogger(LogFilesWatcherRunnable.class);

    private File logDir;
    private List<String> watchingLogfiles = new ArrayList<>();
    private ExecutorService executor;
    private LogAccessController logAccess;

    /**
     * constructor sets up the thread pool, see notice at watchFile method on that.
     * 
     * @param logDir
     * @param logAccess
     */
    public LogFilesWatcherRunnable(File logDir, LogAccessController logAccess) {
        this.logDir = logDir;
        this.logAccess = logAccess;
        executor = Executors.newCachedThreadPool((r) -> {
            Thread result = new Thread(r);
            result.setDaemon(true);
            return result;
        });
    }

    @Override
    public void run() {
        try {
            log.debug("Start Tableau Logs watching");

            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path dir = FileSystems.getDefault().getPath(logDir.getAbsolutePath());
            dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);

            while (true) {
                // wait for key to be signaled
                WatchKey key;
                try {
                    key = watcher.take(); // this will wait here until the next event
                } catch (InterruptedException x) {
                    break;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // This key is registered only ENTRY events,
                    // but an OVERFLOW event can occur regardless, see docs
                    if (kind == OVERFLOW) {
                        log.error("The OS triggered an overflow (special event to indicate"
                                + " that events may have been lost or discarded)");
                        continue;
                    }

                    if (event.context() != null && event.context() instanceof Path) {
                        Path file = (Path) event.context();
                        watchFile(file);

                    } else {
                        // this should not happen at this point
                        // log it, but ignore it and continue because we cannot handle this
                        if (event.context() != null) {
                            log.error("event.context must be a Path! event.context=" + event.context());
                        } else {
                            log.error("event.context is null! event=" + event);
                        }
                    }

                }
                boolean valid = key.reset();
                if (!valid) {
                    log.error("not valid, break loop!");
                    throw new RuntimeException("The Log dir watcher stopped, the key reset is not valid anymore.");

                }
            }

        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            Utils.notifyUserAboutException(e);
        }
    }

    /**
     * Spawns new tailer threads to monitor the actual log files
     *
     * It should be considered to implement a method to stop tailer threads from
     * scanning old log files.
     * 
     * NOTICE: the Apache commons IO tailer class uses polling to scan for changes,
     * so we do not want to open endless tailer threads unfortunately tableau does
     * not always use the latest file by numbering, so a method to close tailing the
     * files may be to find out, what file is actually being appended (what is not
     * that trivial when the tableau logic is not completely clear when files fill
     * be recycled).
     *
     * @param file
     */
    private void watchFile(Path file) {
        try {
            // continue if if valid name
            if (file == null || file.getFileName() == null) {
                return;
            }

            // continue if matches the wanted log files pattern
            String newFile = file.getFileName().toString();
            if (!MonitorMain.LOG_PATTERN.matcher(newFile).matches()) {
                log.debug("log file " + newFile + " changed, but does not match pattern");
                return;
            }

            // continue if not already in watching list
            for (String f : watchingLogfiles) {
                if (f.equals(newFile)) {
                    log.debug("log file " + newFile + " already on the list for watching");
                    return;
                }
            }

            // add to list and start watcher thread to monitor new log lines..
            log.debug("add the file " + newFile + " to watchers!");

            String fullFileStringForTailer = logDir.getAbsolutePath() + File.separator + newFile;
            LogTailerRunnable tailer = new LogTailerRunnable(fullFileStringForTailer, logAccess);
            executor.submit(tailer);

            watchingLogfiles.add(newFile);

            log.debug("Status files currently Watching:" + watchingLogfiles);

        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            Utils.notifyUserAboutException(e);
        }

    }

}
