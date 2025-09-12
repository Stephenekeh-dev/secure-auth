// src/main/java/com/steve/secure_auth/dto/MfaSetupResponse.java
package com.steve.secure_auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MfaSetupResponse {
    private String qrImageDataUrl;
    private String secret;


    // No-args constructor
    public MfaSetupResponse() {}

    // All-args constructor
    public MfaSetupResponse(String qrImageDataUrl, String secret) {
        this.qrImageDataUrl = qrImageDataUrl;
        this.secret = secret;
    }

    // Getters & Setters
    public String getQrImageDataUrl() {
        return qrImageDataUrl;
    }

    public void setQrImageDataUrl(String qrImageDataUrl) {
        this.qrImageDataUrl = qrImageDataUrl;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
