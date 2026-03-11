// src/main/java/com/steve/secure_auth/dto/MfaSetupResponse.java
package com.steve.secure_auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
public class MfaSetupResponse {
    private String qrImageDataUrl;
    private String secret;

    public MfaSetupResponse() {}

    public MfaSetupResponse(String qrImageDataUrl, String secret) {
        this.qrImageDataUrl = qrImageDataUrl;
        this.secret = secret;
    }
}