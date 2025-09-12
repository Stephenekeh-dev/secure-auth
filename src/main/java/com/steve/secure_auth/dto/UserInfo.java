package com.steve.secure_auth.dto;

public class UserInfo {
    private Long id;
    private String email;
    private String firstname;
    private String lastname;
    private String profileImage;

    public UserInfo() {}

    public UserInfo(Long id, String email, String firstname, String lastname, String profileImage) {
        this.id = id;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.profileImage = profileImage;
    }

    public <R> UserInfo(Long id, String email, String firstname, String lastname, String profileImage, R collect) {
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}
