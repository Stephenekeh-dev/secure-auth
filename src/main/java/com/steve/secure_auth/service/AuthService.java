// src/main/java/com/steve/secure_auth/service/AuthService.java
package com.steve.secure_auth.service;

import com.steve.secure_auth.dto.*;
import com.steve.secure_auth.model.Role;
import com.steve.secure_auth.model.User;
import com.steve.secure_auth.model.VerificationToken;
import com.steve.secure_auth.repository.RoleRepository;
import com.steve.secure_auth.repository.UserRepository;
import com.steve.secure_auth.repository.VerificationTokenRepository;
import com.steve.secure_auth.util.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtProvider jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private static final long PASSWORD_RESET_TTL_SECONDS = 30 * 60; // 30 minutes
    private static final String PASSWORD_RESET_PREFIX = "pwd_reset:";

    // Add to constructor parameters:
    private final RedisService redisService;
    private final S3Service s3Service;


    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       VerificationTokenRepository verificationTokenRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       JwtProvider jwtService,
                       RefreshTokenService refreshTokenService,
                       AuthenticationManager authenticationManager,
                       RedisService redisService, S3Service s3Service) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
        this.redisService = redisService;
        this.s3Service = s3Service;
    }

    @Transactional
    public void register(RegisterRequest request) {
        User user = new User();
        user.setFirstname(request.getFirstName());
        user.setLastname(request.getLastName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);

        // Upload profile image to S3 if provided
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            String imageUrl = s3Service.uploadFile(request.getProfileImage());
            user.setProfileImage(imageUrl);
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
        user.getRoles().add(userRole);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(24, ChronoUnit.HOURS);
        VerificationToken verificationToken = new VerificationToken(token, user, expiryDate);
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user, token);
    }
    public AuthenticationResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new IllegalStateException("Please verify your email before logging in.");
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.create(user).getToken();

        //  Unified to builder style (consistent with refresh())
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(AuthenticationResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstname())
                        .lastName(user.getLastname())
                        .profileImage(user.getProfileImage())
                        .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                        .build())
                .build();
    }

    public AuthenticationResponse refresh(RefreshRequest request) {
        var rt = refreshTokenService.validate(request.getRefreshToken());
        var user = rt.getUser();

        String newAccess = jwtService.generateToken(user.getEmail());

        return AuthenticationResponse.builder()
                .accessToken(newAccess)
                .refreshToken(rt.getToken())
                .user(AuthenticationResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstname())
                        .lastName(user.getLastname())
                        .profileImage(user.getProfileImage())
                        .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                        .build())
                .build();
    }

    public Map<String, String> logout(String email, String refreshToken) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (refreshToken != null && !refreshToken.isBlank()) {
                refreshTokenService.revokeByToken(refreshToken);
            } else {
                refreshTokenService.revokeByUser(user);
            }
        });
        return Map.of("message", "Logged out");
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or unrecognized token"));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            verificationTokenRepository.delete(verificationToken); // clean up expired token
            throw new IllegalStateException("Verification token has expired. Please register again.");
        }

        User user = verificationToken.getUser();

        if (user.isEnabled()) {
            throw new IllegalStateException("Account is already verified.");  //  guard against double-verification
        }

        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken); //  one-time use — delete after success
    }
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always return success — never reveal if email exists (security)
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            redisService.saveOtp(
                    PASSWORD_RESET_PREFIX + token,
                    user.getEmail(),
                    PASSWORD_RESET_TTL_SECONDS
            );
            emailService.sendPasswordResetEmail(user, token);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String key = PASSWORD_RESET_PREFIX + request.getToken();
        String email = redisService.getOtp(key);

        if (email == null) {
            throw new IllegalArgumentException("Invalid or expired password reset token.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisService.deleteOtp(key); // one-time use — delete after success
    }
}