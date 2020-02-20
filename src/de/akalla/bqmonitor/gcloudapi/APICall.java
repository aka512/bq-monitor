package de.akalla.bqmonitor.gcloudapi;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.JobField;
import com.google.cloud.bigquery.BigQuery.JobListOption;
import com.google.cloud.bigquery.BigQuery.JobOption;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobStatistics.QueryStatistics;
import com.google.cloud.bigquery.QueryJobConfiguration;
import de.akalla.bqmonitor.entities.JobEntity;
import de.akalla.bqmonitor.gui.MonitorGui;
import de.akalla.bqmonitor.util.Utils;

/**
 * Accesses the google cloud api for BigQuery need a valid access key to a
 * project what is used for billing (google requires this)
 * 
 * @author Andreas Kalla, Jan. 2020
 *
 */
public class APICall {
    private static final Logger log = Logger.getLogger(APICall.class);

    private BigQuery bq;
//   private GoogleCredentials credentials;
    // private long currentMinCreationTime; // for querying ony the next new entries

    List<JobEntity> statEList = new ArrayList<>();

    public APICall(GoogleCredentials credentials) {
//        this.credentials = credentials;
        this.bq = BigQueryOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    public void collectDataCallback() {
        System.out.println("callback! get new data from gcloud");
    }

    public void startLoader(MonitorGui gui) {
        // staets the laoder...
    }

    public JobEntity loadNewestJob() {
        try {
            return loadJobs(1).get(0);
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }
        throw new RuntimeException("error loading newest job");
    }

    public List<JobEntity> loadJobs(int pageSize) {

        // query to get current jobs
        JobListOption[] op = { JobListOption.pageSize(pageSize), JobListOption.allUsers() };
        Page<Job> jobs = bq.listJobs(op);

        // local entity used for statistic
        List<JobEntity> addEList = new ArrayList<>();

        for (Job job : jobs.getValues()) {

            addEList.add(getJobEntityFromJob(job));

        }
        return addEList;

    }
    
    
    public List<String> loadJobIds(int pageSize) {

        // query to get current jobs
        JobListOption[] op = { JobListOption.pageSize(pageSize), JobListOption.allUsers(), JobListOption.fields(JobField.ID) };
        Page<Job> jobs = bq.listJobs(op);

        // local entity used for statistic
        List<String> idList = new ArrayList<>();

        for (Job job : jobs.getValues()) {
            idList.add(job.getJobId().getJob());
        }
        return idList;

    }
    
    
    public JobEntity loadJobEntityById(String jobid) {

        JobOption[] op = {}; // JobOption.fields(fields)
        Job job = bq.getJob(jobid, op);
        
        return getJobEntityFromJob(job);
        
    }
    

    private JobEntity getJobEntityFromJob(Job job) {

        JobEntity ent = new JobEntity();

        try {
            // gerneral job meta data
            ent.setJobId(job.getJobId().getJob());
           // ent.setFullJobId(job.getGeneratedId()); // with projectid "generated", not needen
            ent.setStatus(job.getStatus());
            ent.setUserEmail(job.getUserEmail());

            // protopayload_auditlog.servicedata_v1_bigquery.jobCompletedEvent.job.jobConfiguration.query.query
            QueryJobConfiguration conf = job.getConfiguration();
            ent.setStatement(conf.getQuery());

            // job statistic data
            QueryStatistics stat = job.getStatistics();
            ent.setStatTier(stat.getBillingTier());
            ent.setStatBytesBilled(stat.getTotalBytesBilled());
            ent.setStatStartTime(stat.getStartTime());
            ent.setStatEndTime(stat.getEndTime());
            ent.setStatStatementType(stat.getStatementType());
            ent.setStatCacheHit(stat.getCacheHit());

            ent.validate();

        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            Utils.notifyUserAboutException(e);

        }

        return ent;
    }

//    private void addToStatEList(List<JobEntity> newList) {
//
//        // add all if new List
//        if (statEList == null || statEList.size() == 0) {
//            statEList.addAll(newList);
//            return;
//        }
//
//        // add only if not existing
//        Boolean found = false;
//        for (JobEntity eNew : newList) {
//            for (JobEntity eExist : statEList) {
//                if (eNew.getJobId().equals(eExist.getJobId())) {
//                    found = true;
//                    // TODO: replace if found (could be running currently..) remove old add new with
//                    // new stats
//                    break;
//                }
//            }
//
//            // if found continue loop here
//            if (found) {
//                found = false;
//                continue;
//            }
//
//            // add to list
//            statEList.add(eNew);
//        }
//    }

//    private Long getBillingBytesTotal() {
//
////        Long totalBytes = 0L;
////
////        for (JobEntity e : statEList) {
////            totalBytes += e.getStatBytesBilled();
////        }
////
////        return totalBytes;
//
//    }

    public void start() {

//        List<JobEntity> newEntries = loadJobs(5);
//        addToStatEList(newEntries);
//
//        Long t = getBillingBytesTotal();
//
//        System.out.println(t);
//        System.out.println(getGB(t) + " GB");

    }



}
