package de.akalla.bqmonitor.gcloudapi;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.akalla.bqmonitor.entities.JobEntity;
import de.akalla.bqmonitor.entities.TableauBqEntity;
import de.akalla.bqmonitor.gui.MonitorGui;
import de.akalla.bqmonitor.util.Utils;

/**
 * handles querying the google cloud API until the "Job" is finished
 * (status:DONE)
 *
 * @author Andreas Kalla, Jan. 2020
 */
public class JobController {
    private static final Logger log = Logger.getLogger(JobController.class);

    private APICall apiCall;
    private MonitorGui monitorGui;
    private Gson gson;
    private APIJobFinder apiJobFinder;
//    private List<JobEntity> jobs = new ArrayList<>();

    private boolean initialLoaded = false;

    /**
     * needs a reference to monitor for injecting the loaded jobs also the key file
     * for proper accessing the google cloud API
     *
     * @param key
     * @param monitorGui
     * @param apiJobFinder
     */
    public JobController(File key, MonitorGui monitorGui, APIJobFinder apiJobFinder) {
        gson = new GsonBuilder().setPrettyPrinting().create();
        this.monitorGui = monitorGui;
        this.apiJobFinder = apiJobFinder;

        executor = Executors.newCachedThreadPool((r) -> {
            Thread result = new Thread(r);
            result.setDaemon(true);
            return result;
        });

        try {
            APICredentialsHelper cred = new APICredentialsHelper(key);
            apiCall = new APICall(cred.getCredentials());
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }
    }

    /**
     * handle for result of API call threads
     *
     * @param e
     */
    synchronized public void handleInsertOrUpdateToList(JobEntity e) {
        long start = System.currentTimeMillis();

        monitorGui.insertToListAndKPis(e);

        log.info("handleInsertOrUpdateToList jobid=" + e.getJobId() + " took " + (System.currentTimeMillis() - start)
                + " ms");

    }

    public void loadids(int num) {
        long start = System.currentTimeMillis();
        List<String> list = apiCall.loadJobIds(num);
        log.info("loadLatestEntries num=" + num + " took " + (System.currentTimeMillis() - start) + " ms");
        System.out.println(list);

    }


    public JobEntity loadEntryById(String jobid) {
        long start = System.currentTimeMillis();
        JobEntity ret = apiCall.loadJobEntityById(jobid);
        log.info("loadEntryById jobid=" + jobid + " took " + (System.currentTimeMillis() - start) + " ms");
        return ret;
    }

    /**
     * triggered by thread: load more entries to get full job id properly using sql
     * matching TODO threading safe?
     *
     * @param num
     */
    public List<JobEntity> loadLatestEntries(int num) {
        long start = System.currentTimeMillis();
        List<JobEntity> list = apiCall.loadJobs(num);
        log.info("loadLatestEntries num=" + num + " took " + (System.currentTimeMillis() - start) + " ms");
        return list;

    }

    /**
     * load initial amount of jobs to show in the GUI
     *
     * @param num
     */
    public void loadLatestEntriesInverseOrderForList(int num) {
        if (initialLoaded) {
            Utils.notifyUser(monitorGui.getWindow(), "sorry, the loading  works only once at start");
        }

        List<JobEntity> jobs = apiCall.loadJobs(num);
        Collections.reverse(jobs);

        // TODO wrong ordering
        for (JobEntity e : jobs) {
            monitorGui.insertToListAndKPis(e);
        }

        initialLoaded = true;
    }

    private Set<String> startedQHashes = new HashSet<>();

    private ExecutorService executor;


    /**
     * called by log watcher to request the details for a new job
     */
    public void insertJob(String line, boolean isStartMarker) {
        TableauBqEntity logLine = gson.fromJson(line, TableauBqEntity.class);

        // check what to to with that line
        String qhash = logLine.getSafeQueryHash();
        String sql = logLine.getSafeQuery();

        if (qhash == null || qhash.equals("")) {
            throw new RuntimeException("qHash parameter can not be null or empty at this point");
        }

        if (isStartMarker) {
            log.debug("insertJob STARTMARKER for qhash=" + qhash);
//            boolean startApiRun = false;

//          //  synchronized(startedQHashes) {
//                if (!startedQHashes.contains(qhash)) {
//                    startedQHashes.add(qhash);
//                    startApiRun = true;
//                } else {
//                    log.debug("job on this instance of qhash=" + qhash + " allready started");
//                }
//            }
//            if (startApiRun) {
            APIRunnable apirun = new APIRunnable(this, sql, apiJobFinder);
            executor.submit(apirun);
//            }

        } else {
            log.debug("insertJob ENDMARKER for qhash=" + qhash);
        }


    }

}
