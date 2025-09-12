// src/main/java/com/steve/secure_auth/dto/RefreshRequest.java
package com.steve.secure_auth.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshRequest {
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    private String refreshToken; // optional if you use cookie
}
