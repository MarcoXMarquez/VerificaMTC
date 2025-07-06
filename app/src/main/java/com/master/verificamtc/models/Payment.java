package com.master.verificamtc.models;

public class Payment {
    public String id;
    public String userId;
    public double amount;
    public String date;

    public Payment() { }
    public Payment(String id, String userId, double amount, String date) {
        this.id     = id;
        this.userId = userId;
        this.amount = amount;
        this.date   = date;
    }
}
