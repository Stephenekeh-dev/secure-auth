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
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService, VerificationTokenRepository verificationTokenRepository, UserRepository userRepository) {
        this.authService = authService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
    }

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;

    // ---- REGISTER ----
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully. Please check your email to verify your account."
        ));
    }

    // ---- VERIFY EMAIL ---- (you already had this; kept intact)
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token expired"));
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Email verified successfully. You can now log in."));
    }

    // ---- LOGIN ----
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ---- REFRESH ----
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    // ---- LOGOUT ----
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestParam String email,
            @RequestParam(required = false) String refreshToken
    ) {
        return ResponseEntity.ok(authService.logout(email, refreshToken));
    }

    // ---- Forgot / Reset (stubs to implement next) ----
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        // TODO: implement (create PasswordResetToken, email link)
        return ResponseEntity.ok(Map.of("message", "If this email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        // TODO: implement (validate token, set new encoded password)
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    // ---- MFA (stubs to implement later) ----
    @GetMapping("/mfa/setup")
    public ResponseEntity<MfaSetupResponse> mfaSetup() {
        // TODO: require auth, return QR + secret
        return ResponseEntity.ok(new MfaSetupResponse(null, null));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<?> mfaVerify(@RequestBody MfaVerifyRequest req) {
        // TODO: verify TOTP, enable MFA on account
        return ResponseEntity.ok(Map.of("message", "MFA verified"));
    }
}
