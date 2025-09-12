// src/main/java/com/steve/secure_auth/dto/ResetPasswordRequest.java
package com.steve.secure_auth.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
}
