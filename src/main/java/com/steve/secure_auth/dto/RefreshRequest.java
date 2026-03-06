// src/main/java/com/steve/secure_auth/dto/RefreshRequest.java
package com.steve.secure_auth.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshRequest {
    private String refreshToken;
}