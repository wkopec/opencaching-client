package com.kopec.wojciech.occlient;

/**
 * Created by Wojtek on 2016-06-08.
 */

public class CacheInfo {

    public String code;
    public String name;
    public String location;
    public String type;
    public String status;
    public String rating;
    public String size;
    public String owner;
    public String recommendations;

    public CacheInfo(String code, String name, String location, String type, String status) {
        this.code = code;
        this.name = name;
        this.location = location;
        this.type = type;
        this.status = status;
    }
}
