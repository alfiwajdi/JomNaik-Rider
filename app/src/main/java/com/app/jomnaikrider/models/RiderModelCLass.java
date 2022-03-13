package com.app.jomnaikrider.models;

import android.content.Intent;

import java.util.List;

public class RiderModelCLass {
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String password;
    private String token;
    private String userType;
    private String gender;
    private boolean emailVerified;

    public RiderModelCLass() { }

    public RiderModelCLass(String userId, String fullName, String email, String phone, String address,
                           String password, String token, String userType, String gender, boolean emailVerified) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.password = password;
        this.token = token;
        this.userType = userType;
        this.gender = gender;
        this.emailVerified = emailVerified;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public String getUserType() {
        return userType;
    }

    public String getGender() {
        return gender;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}
