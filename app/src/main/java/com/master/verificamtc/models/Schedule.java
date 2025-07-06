package com.master.verificamtc.models;

public class Schedule {
    public String id;
    public String date;
    public String time;

    public Schedule() { }
    public Schedule(String id, String date, String time) {
        this.id   = id;
        this.date = date;
        this.time = time;
    }
}
