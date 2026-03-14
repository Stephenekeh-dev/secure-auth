// src/main/java/com/steve/secure_auth/dto/MfaSetupResponse.java
package com.steve.secure_auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;




@Data
@NoArgsConstructor
@AllArgsConstructor
public class MfaSetupResponse {
    private String secret;
    private String qrCodeUrl;
}