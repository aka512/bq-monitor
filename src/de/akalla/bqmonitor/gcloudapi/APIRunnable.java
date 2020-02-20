package de.akalla.bqmonitor.gcloudapi;

import org.apache.log4j.Logger;

import de.akalla.bqmonitor.entities.JobEntity;

public class APIRunnable implements Runnable {
    private static final Logger log = Logger.getLogger(APIRunnable.class);

    private final Object lock = new Object();
    private volatile String jobid;
    private String sql;
    private JobController jobController;
    private APIJobFinder apiJobFinder;

    public APIRunnable(JobController jobController, String sql, APIJobFinder apiJobFinder) {
        this.jobController = jobController;
        this.sql = sql;
        this.apiJobFinder = apiJobFinder;

    }

    // just to prevent a endless loop for any thread, for safety reasons
    private int runs = 0;
    private int maxRuns = 600; // is equal to maximum 10 minutes can a query take (600 sec.) when 1000ms wait
    private long waitTime = 1000L;

    @Override
    public void run() {

        // finds the id to be polled below
        apiJobFinder.addOneTimeListener(new APIJobFinder.JobFinderListener((jobEntity) -> {
            this.jobid = jobEntity.getJobId();
            synchronized (lock) {
                lock.notifyAll();
            }
        }, sql));
        try {
            //stops this thread here, until the job finder pushes a correct job (by comparing the sql)
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // find the final job (by Job == DONE)
        log.debug("Thread started." + Thread.currentThread().getName());
        try {
            while (runs < maxRuns) {
                // initial sleep the wait time
                // it is expected, that the query will not immediately be finished

                if (jobid == null) {
                    throw new RuntimeException("fulljobid must be set at this point!");
                }

                // store the job id
                JobEntity e = jobController.loadEntryById(jobid);

                jobController.handleInsertOrUpdateToList(e);

                if (e.isDone()) {
                    log.debug("API OK Step 2: job is done, jobid=" + jobid);
                    break;
                }


                Thread.sleep(waitTime);

                runs++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
        }

    }

}
