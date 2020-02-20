package de.akalla.bqmonitor.gcloudapi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import de.akalla.bqmonitor.entities.JobEntity;


public class APIJobFinder {

    private static final Logger log = Logger.getLogger(APIJobFinder.class);

    private static String cleanSQLQuery(String sql) {
        return sql.replaceAll("[\\n\\t\\r ]", "");
    }

    private final List<JobFinderListener> oneTimeListeners = new ArrayList<JobFinderListener>();
    private JobController jobController;

    public APIJobFinder() {
    }

    // cosntructor like
    public void init(JobController jobController) {
        this.jobController = jobController;

        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (oneTimeListeners) {
                    if (oneTimeListeners.size() > 0) {
                        List<JobEntity> listEntities = jobController.loadLatestEntries(10);

                        for (JobEntity e : listEntities) {

                            final String cleanedEntitySql = cleanSQLQuery(e.getStatement());

                            for (int i = 0; i < oneTimeListeners.size(); i++) {
                                log.debug("cleanedEntitySql=" + cleanedEntitySql + " <===========> " + oneTimeListeners.get(i).getCleanedSqlQuery());
                                if (cleanedEntitySql.equalsIgnoreCase(oneTimeListeners.get(i).getCleanedSqlQuery())) {
                                    log.debug("found match "+i);
                                    oneTimeListeners.get(i).jobFound.accept(e);
                                    oneTimeListeners.remove(i--);
                                }
                            }
                        }
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }


    void addOneTimeListener(JobFinderListener jobFinderListener) {
        synchronized (oneTimeListeners) {
            oneTimeListeners.add(jobFinderListener);
        }
    }

    public static class JobFinderListener {
        private final Consumer<JobEntity> jobFound;
        private final String cleanedSqlQuery;

        public JobFinderListener(Consumer<JobEntity> jobFound, String sqlQuery) {
            this.jobFound = jobFound;
            this.cleanedSqlQuery = cleanSQLQuery(sqlQuery);
        }

        public Consumer<JobEntity> getJobFound() {
            return jobFound;
        }

        public String getCleanedSqlQuery() {
            return cleanedSqlQuery;
        }
    }


}
