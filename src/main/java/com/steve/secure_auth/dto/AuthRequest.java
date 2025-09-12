package com.steve.secure_auth.dto;

public class AuthRequest {
    private String email;
    private String password;
    private String otp; // optional, for MFA later

    // Default constructor
    public AuthRequest() {
    }

    // All-args constructor
    public AuthRequest(String email, String password, String otp) {
        this.email = email;
        this.password = password;
        this.otp = otp;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getOtp() {
        return otp;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    // toString()
    @Override
    public String toString() {
        return "AuthRequest{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", otp='" + otp + '\'' +
                '}';
    }
}
