package com.steve.secure_auth.dto;

public class AuthRequest {
    private String email;
    private String password;

    // Default constructor
    public AuthRequest() {
    }

    // All-args constructor
    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;

    }

    // Getters
    public String getEmail() {

        return email;
    }

    public String getPassword() {

        return password;
    }



    // Setters
    public void setEmail(String email) {

        this.email = email;
    }

    public void setPassword(String password) {

        this.password = password;
    }



    @Override
    public String toString() {
        return "AuthRequest{email='" + email + "', otp='" + "'}";

    }
}
