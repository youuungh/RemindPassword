package com.example.passwordmanager.model;

import com.google.firebase.Timestamp;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Content {
    private String title;
    private String search;
    private String id;
    private String pw;
    private String memo;
    private String docId;
    private Timestamp timestamp;

    public Content() {

    }

    public Content(String title, String search, String id, String pw, String memo, String docId, Timestamp timestamp) {
        this.title = title;
        this.search = search;
        this.id = id;
        this.pw = pw;
        this.memo = memo;
        this.docId = docId;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getSearch() { return search; }

    public String getId() {
        return id;
    }

    public String getPw() {
        return pw;
    }

    public String getMemo() {
        return memo;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
