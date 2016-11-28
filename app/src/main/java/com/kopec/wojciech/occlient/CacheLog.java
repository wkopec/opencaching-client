package com.kopec.wojciech.occlient;

import java.util.ArrayList;

public class CacheLog {
    public String date;
    public String type;
    public String comment;
    public String username;

    public CacheLog(String date, String type, String comment, String username) {
        this.username = username;
        this.date = date;
        this.type = type;
        this.comment = comment;
    }

    public String getDate() { return date; }
    public String getType() {return type; }
    public String getUsername() { return username; }
    public String getComment() { return comment; }

    public static class List extends ArrayList<CacheLog> {

    }
}
