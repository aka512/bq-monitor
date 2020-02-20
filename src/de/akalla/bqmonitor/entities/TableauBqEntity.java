package de.akalla.bqmonitor.entities;

/**
 * Main Object from Tableau logs corresonding BigQuery Statements it represents
 * a full log line comming from tableau tabprotosrv_* log files. used by GSON
 * deserializer View (v) and Context (ctx) is a sub part of this class.
 *
 * NOTE the format is slightly different depending if the Entry for starting a
 * Query or finished a Query: if beginning there are additionally a ctx field,
 * if finishing there is additionally cols and elapsed in v field
 *
 * @author Andreas Kalla, Feb. 2020
 */
public class TableauBqEntity {
    private String ts;
    private Long pid;
    private String tid;
    private String sev;
    private String req;
    private String sess;
    private String site;
    private String user;
    private String k;
    private TableauBqViewEntity v;
    private TableauBqCtxEntity ctx;

    @Override
    public String toString() {
        return "TableauBqEntity{" + "ts='" + ts + '\'' + ", pid=" + pid + ", tid='" + tid + '\'' + ", sev='" + sev
                + '\'' + ", req='" + req + '\'' + ", sess='" + sess + '\'' + ", site='" + site + '\'' + ", user='"
                + user + '\'' + ", k='" + k + '\'' + ", v=" + v + ", ctx=" + ctx + '}';
    }

    /**
     * null pointer safe operation for getting tableau generated unique hash that identifies
     * the operation
     *
     * @return a empty string or the given query hash from v.query-hash
     */
    public String getSafeQueryHash() {
        if (v != null) {
            if (v.getQuery_hash() != null) {
                return v.getQuery_hash();
            }
        }
        return "";
    }
    
    /**
     * null pointer safe operation for getting tableau generated SQL query that identifies
     * the operation
     *
     * @return a empty string or the given query hash from v.query-hash
     */
    public String getSafeQuery() {
        if (v != null) {
            if (v.getQuery() != null) {
                return v.getQuery();
            }
        }
        return "";
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getSev() {
        return sev;
    }

    public void setSev(String sev) {
        this.sev = sev;
    }

    public String getReq() {
        return req;
    }

    public void setReq(String req) {
        this.req = req;
    }

    public String getSess() {
        return sess;
    }

    public void setSess(String sess) {
        this.sess = sess;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }

    public TableauBqViewEntity getV() {
        return v;
    }

    public void setV(TableauBqViewEntity v) {
        this.v = v;
    }

    public TableauBqCtxEntity getCtx() {
        return ctx;
    }

    public void setCtx(TableauBqCtxEntity ctx) {
        this.ctx = ctx;
    }
}
