// src/main/java/com/steve/secure_auth/dto/MfaVerifyRequest.java
package com.steve.secure_auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MfaVerifyRequest {

    @NotBlank
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    private String code;
}