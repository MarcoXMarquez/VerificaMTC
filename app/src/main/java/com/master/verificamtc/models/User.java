package com.master.verificamtc.models;

public class User {
    public String dni;
    public String firstName;
    public String lastName;
    public String birthDate;
    public String email;
    public String password;
    public boolean paymentStatus;
    public boolean writtenExamPassed;
    public boolean drivingExamPassed;

    public User() { }  // Firebase necesita el constructor vacío

    public User(String dni, String firstName, String lastName, String birthDate, String email, String password) {
        this.dni                = dni;
        this.firstName          = firstName;
        this.lastName           = lastName;
        this.birthDate          = birthDate;
        this.email              = email;
        this.password           = password;
        this.paymentStatus      = false;  // valor inicial
        this.writtenExamPassed  = false;
        this.drivingExamPassed  = false;
    }
}
