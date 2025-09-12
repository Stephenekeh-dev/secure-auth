// src/main/java/com/steve/secure_auth/dto/MfaVerifyRequest.java
package com.steve.secure_auth.dto;

import lombok.Data;

@Data
public class MfaVerifyRequest {
    private String code;
}
