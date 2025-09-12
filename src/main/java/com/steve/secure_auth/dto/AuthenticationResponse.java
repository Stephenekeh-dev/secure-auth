package com.steve.secure_auth.dto;

import java.util.Set;

public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private UserInfo user; // full user details

    // ✅ No-args constructor
    public AuthenticationResponse() {}

    // ✅ All-args constructor
    public AuthenticationResponse(String accessToken, String refreshToken, UserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public AuthenticationResponse(String accessToken, String refreshToken, com.steve.secure_auth.dto.UserInfo userInfo) {
    }

    // ✅ Getters & Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }

    // ✅ Manual Builder
    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private UserInfo user;

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder user(UserInfo user) {
            this.user = user;
            return this;
        }

        public AuthenticationResponse build() {
            return new AuthenticationResponse(accessToken, refreshToken, user);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // ✅ Inner UserInfo class (expanded)
    public static class UserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String profileImage;
        private Set<String> roles;

        public UserInfo() {}

        public UserInfo(Long id, String email, String firstName, String lastName, String profileImage, Set<String> roles) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.profileImage = profileImage;
            this.roles = roles;
        }

        // ✅ Getters & Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

        public Set<String> getRoles() { return roles; }
        public void setRoles(Set<String> roles) { this.roles = roles; }

        // ✅ Builder for UserInfo
        public static class Builder {
            private Long id;
            private String email;
            private String firstName;
            private String lastName;
            private String profileImage;
            private Set<String> roles;

            public Builder id(Long id) {
                this.id = id;
                return this;
            }

            public Builder email(String email) {
                this.email = email;
                return this;
            }

            public Builder firstName(String firstName) {
                this.firstName = firstName;
                return this;
            }

            public Builder lastName(String lastName) {
                this.lastName = lastName;
                return this;
            }

            public Builder profileImage(String profileImage) {
                this.profileImage = profileImage;
                return this;
            }

            public Builder roles(Set<String> roles) {
                this.roles = roles;
                return this;
            }

            public UserInfo build() {
                return new UserInfo(id, email, firstName, lastName, profileImage, roles);
            }
        }

        public static Builder builder() {
            return new Builder();
        }
    }

    @Override
    public String toString() {
        return "AuthenticationResponse{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", user=" + user +
                '}';
    }
}
