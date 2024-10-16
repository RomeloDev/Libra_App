package com.example.libraapp;

public class LogEntry {
    private String userId;
    private String timestamp;

    public LogEntry(){

    }

    public LogEntry(String userId, String timestamp){
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getUserId(){
        return userId;
    }

    public String getTimestamp(){
        return timestamp;
    }
}
