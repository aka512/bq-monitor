package de.akalla.bqmonitor.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Sub Part of TableauBqEntity, see main object class.
 *
 * @author Andreas Kalla, Feb. 2020
 */
public class TableauBqCtxEntity {

    @SerializedName("client-type")
    private String client_type;

    private String procid;

    private String tid;

    private String version;

    @Override
    public String toString() {
        return "TableauBqCtx{" + "client_type='" + client_type + '\'' + ", procid='" + procid + '\'' + ", tid='" + tid
                + '\'' + ", version='" + version + '\'' + '}';
    }

    public String getClient_type() {
        return client_type;
    }

    public void setClient_type(String client_type) {
        this.client_type = client_type;
    }

    public String getProcid() {
        return procid;
    }

    public void setProcid(String procid) {
        this.procid = procid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
