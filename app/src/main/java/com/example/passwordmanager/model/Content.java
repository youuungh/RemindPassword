package com.example.passwordmanager.model;

import com.google.firebase.Timestamp;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Content {
    private String title;
    private String id;
    private String pw;
    private String memo;
    private Timestamp timestamp;

    public Content() {

    }

    public Content(String title, String id, String pw, String memo, Timestamp timestamp) {
        this.title = title;
        this.id = id;
        this.pw = pw;
        this.memo = memo;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getPw() {
        return pw;
    }

    public String getMemo() {
        return memo;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
