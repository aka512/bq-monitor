package de.akalla.bqmonitor.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Sub Part of TableauBqEntity
 *
 * @author Andreas Kalla, Feb. 2020
 */
public class TableauBqViewEntity {

    private Long cols;

    private Double elapsed;

    @SerializedName("is-command")
    private Boolean is_command;

    @SerializedName("protocol-class")
    private String protocol_class;

    @SerializedName("protocol-id")
    private Long protocol_id;

    @SerializedName("query-category")
    private String query_category;

    @SerializedName("query-hash")
    private String query_hash;

    private String query;

    @SerializedName("query-trunc")
    private String query_trunc;

    private Long rows;


    @Override
    public String toString() {
        return "TableauBqDetailsEntity{" + "cols=" + cols + ", elapsed=" + elapsed + ", is_command=" + is_command
                + ", protocol_class='" + protocol_class + '\'' + ", protocol_id=" + protocol_id + ", query_category='"
                + query_category + '\'' + ", query_hash=" + query_hash + ", query='" + query + '\'' + ", query_trunc='"
                + query_trunc + '\'' + ", rows=" + rows + '}';
    }

    public Long getCols() {
        return cols;
    }

    public void setCols(Long cols) {
        this.cols = cols;
    }

    public Double getElapsed() {
        return elapsed;
    }

    public void setElapsed(Double elapsed) {
        this.elapsed = elapsed;
    }

    public Boolean getIs_command() {
        return is_command;
    }

    public void setIs_command(Boolean is_command) {
        this.is_command = is_command;
    }

    public String getProtocol_class() {
        return protocol_class;
    }

    public void setProtocol_class(String protocol_class) {
        this.protocol_class = protocol_class;
    }

    public Long getProtocol_id() {
        return protocol_id;
    }

    public void setProtocol_id(Long protocol_id) {
        this.protocol_id = protocol_id;
    }

    public String getQuery_category() {
        return query_category;
    }

    public void setQuery_category(String query_category) {
        this.query_category = query_category;
    }

    public String getQuery_hash() {
        return query_hash;
    }

    public void setQuery_hash(String query_hash) {
        this.query_hash = query_hash;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery_trunc() {
        return query_trunc;
    }

    public void setQuery_trunc(String query_trunc) {
        this.query_trunc = query_trunc;
    }

    public Long getRows() {
        return rows;
    }

    public void setRows(Long rows) {
        this.rows = rows;
    }

}
