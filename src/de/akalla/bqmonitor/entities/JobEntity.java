package de.akalla.bqmonitor.entities;

import java.util.Locale;

import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.JobStatistics.QueryStatistics.StatementType;
import com.google.cloud.bigquery.JobStatus;
import de.akalla.bqmonitor.util.Utils;

/**
 * Holds the information about the BigQuery Jobs History. Important is to
 * distinguish between a PRENDING or RUNNING and a DONE Job.
 * <p>
 * PENDING and RUNNING Jobs needed to be reload until they are DONE. When DONE
 * the Entity can be considered as final and are not being requested again. To
 * make the identifikation more easy, there is a "isDone" Flog provided, that
 * checks the "JobStatus.status" Value more easily.
 * <p>
 * Entities are loaded by request only and not by polling, because we do not
 * want to flood the google api with requests and beeing banned or billed for
 * such unnecessary reasons. The Trigger for request comes from the Tableau Log
 * Watcher service.
 *
 * @author Andreas Kalla, Jan. 2020
 */
public class JobEntity {
//    private static final Logger log = Logger.getLogger(JobEntity.class); 

    // global unique jobid: project name + execution location + jobid
    // e.g.: dulcet-bastion-261919:US.job_Ys8OWef-Fm_tG-JCs6-oOEtshlAH
    // private String fullJobId;

    // the last part of the fullJobId: job_Ys8OWef-Fm_tG-JCs6-oOEtshlAH
    private String jobId;

    // for fast check if job needs to be checked again for final result (true only
    // when jobstatus.state == DONE)
    private boolean isDone = false;

    // full status object, sub:
    // { "errorResult": { object (ErrorProto) }, "errors": [ { object (ErrorProto) }
    // ], "state": string // PENDING, RUNNING, DONE: When the state is DONE,
    // errorResult can be checked to determine whether the job succeeded or failed.
    private JobStatus status;

    // email address of the user who ran the job
    private String userEmail;

    // billing tier for the job, usually 1
    private Integer statTier;

    // total number of bytes billed for the job
    private Long statBytesBilled;

    // start time in timestamp UTC
    private Long statStartTime;

    // end time in timestamp UTC
    private Long statEndTime;

    // is cache hit (no billing usually)
    private Boolean statCacheHit;

    // (BETA API) call, distinguished between INSERT, SELECT, UPDATE...
    private StatementType statStatementType;

    private int queryState;
    private String statement;

    // calculated values, by validate method
    private String statGiBBilled;
    private String startDatetime;
    private String endDatetime;
    private String tookMs;
    private String tookSec;

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

//    public String getFullJobId() {
//        return fullJobId;
//    }
//
//    public void setFullJobId(String fullJobId) {
//        this.fullJobId = fullJobId;
//    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Integer getStatTier() {
        return statTier;
    }

    public void setStatTier(Integer statTier) {
        this.statTier = statTier;
    }

    public Long getStatBytesBilled() {
        return statBytesBilled;
    }

    public void setStatBytesBilled(Long statBytesBilled) {
        this.statBytesBilled = statBytesBilled;
    }

    public String getStatGiBBilled() {
        return statGiBBilled;
    }

    public void setStatGiBBilled(String statGiBBilled) {
        this.statGiBBilled = statGiBBilled;
    }

    public String getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(String startDatetime) {
        this.startDatetime = startDatetime;
    }

    public String getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(String endDatetime) {
        this.endDatetime = endDatetime;
    }

    public Long getStatStartTime() {
        return statStartTime;
    }

    public void setStatStartTime(Long statStartTime) {
        this.statStartTime = statStartTime;
    }

    public Long getStatEndTime() {
        return statEndTime;
    }

    public void setStatEndTime(Long statEndTime) {
        this.statEndTime = statEndTime;
    }

    public Boolean getStatCacheHit() {
        return statCacheHit;
    }

    public void setStatCacheHit(Boolean statCacheHit) {
        this.statCacheHit = statCacheHit;
    }

    public StatementType getStatStatementType() {
        return statStatementType;
    }

    public void setStatStatementType(StatementType statStatementType) {
        this.statStatementType = statStatementType;
    }

    public int getQueryState() {
        return queryState;
    }

    public void setQueryState(int queryState) {
        this.queryState = queryState;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean isDone) {
        this.isDone = isDone;
    }

    public JobStatus getStatus() {
        return status;
    }

    /**
     * set isDone to true as well, when job status is DONE.
     *
     * @param status
     */
    public void setStatus(JobStatus status) {
        this.status = status;
        if (status != null && status.getState() != null) {
            setDone(status.getState().equals(JobStatus.State.DONE));
        }
    }

    public String getTookMs() {
        return tookMs;
    }

    public void setTookMs(String tookMs) {
        this.tookMs = tookMs;
    }

    public String getTookSec() {
        return tookSec;
    }

    public void setTookSec(String tookSec) {
        this.tookSec = tookSec;
    }

    @Override
    public String toString() {
        return toHumanString();
    }

    public String toHumanString() {
        StringBuilder res = new StringBuilder();

        if (statBytesBilled != null) {
            res.append(Utils.humanReadableByteCountBin(statBytesBilled));
        } else {
            res.append("? Byte");
        }

        if (statCacheHit == null) {
            res.append(" ");
        } else if (statCacheHit) {
            res.append(" (cache hit)");
        } else {
            res.append(" billed");
        }


        res.append("\n\n");

        if (status != null) {
            res.append(status.getState());
            BigQueryError er = status.getError();
            if (er != null) {
                res.append(", Error: " + er.getMessage());
            }
        } else {
            res.append("Status unknown");
        }

        res.append("\n\n");
        res.append("Started: " + Utils.formatOptionalTimestampToString(statStartTime));
        res.append("\n");
        res.append("Ended: " + Utils.formatOptionalTimestampToString(statEndTime));
        res.append("\n");
        res.append("Took: " + getTookSec() + " sec. \n");

        res.append("\n\n");

        res.append("google job id: ");

        res.append(getJobId());
        res.append("\n");

        res.append("by user:");
        res.append(userEmail);

        res.append("\n\n");

        res.append("SQL:");
        res.append("\n");
        res.append(statement);

        return res.toString();
    }

    /**
     * inserts some calculated values, esp. for json presentation / visualisation =>
     * GB billed, Starttime in ISO Timestamp, Endtime in ISO Timestamp, Took in
     * Seconds and millis
     * <p>
     * should be called after setting all initial values and before adding it to a
     * list
     */
    public void validate() {

        if (this.getStatBytesBilled() != null) {
            this.setStatGiBBilled(Utils.getGB(this.getStatBytesBilled()));
        }
        if (this.getStatStartTime() != null) {
            this.setStartDatetime(Utils.getTimestampToISOString(this.getStatStartTime()));
        }
        if (this.getStatEndTime() != null) {
            this.setEndDatetime(Utils.getTimestampToISOString(this.getStatEndTime()));

        }

        if (statStartTime != null && statEndTime != null) {
            this.setTookMs(generateTookMs());
            this.setTookSec(generateTookSec());
        }

    }

    /**
     * returns time of statement in milliseconds from time stamps
     *
     * @return
     */
    private String generateTookMs() {
        if (statStartTime != null && statEndTime != null) {
            Long ms = statEndTime - statStartTime;
            return ms.toString();
        } else {
            return "?";
        }
    }

    private String generateTookSec() {
        if (statStartTime != null && statEndTime != null) {
            double x = (double) statEndTime - (double) statStartTime;
            double s = Math.round(x / 1000.0);
            return String.format(Locale.US, "%.1f", s);

        } else {
            return "?";
        }
    }

}
