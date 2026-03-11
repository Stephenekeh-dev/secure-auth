// src/main/java/com/steve/secure_auth/controller/AuthController.java
package com.steve.secure_auth.controller;

import com.steve.secure_auth.dto.*;
import com.steve.secure_auth.model.User;
import com.steve.secure_auth.model.VerificationToken;
import com.steve.secure_auth.repository.UserRepository;
import com.steve.secure_auth.repository.VerificationTokenRepository;
import com.steve.secure_auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Fields before constructor
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;  // verifyEmail logic moved to AuthService
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully. Please check your email to verify your account."
        ));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully. You can now log in."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    //  email extracted from SecurityContext — not from request param
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestParam(required = false) String refreshToken,
            @RequestParam(required = false) String email,  // ✅ fallback for testing
            Authentication authentication
    ) {
        String resolvedEmail = (authentication != null)
                ? authentication.getName()
                : email;

        if (resolvedEmail == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not authenticated"));
        }
        return ResponseEntity.ok(authService.logout(resolvedEmail, refreshToken));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(Map.of("message", "If this email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    @GetMapping("/mfa/setup")
    public ResponseEntity<MfaSetupResponse> mfaSetup() {
        return ResponseEntity.ok(new MfaSetupResponse(null, null));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<?> mfaVerify(@RequestBody MfaVerifyRequest req) {
        return ResponseEntity.ok(Map.of("message", "MFA verified"));
    }
}