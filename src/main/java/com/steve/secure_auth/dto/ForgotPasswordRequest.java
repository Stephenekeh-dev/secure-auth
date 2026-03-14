// src/main/java/com/steve/secure_auth/dto/ForgotPasswordRequest.java
package com.steve.secure_auth.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank
    @Email
    private String email;
}