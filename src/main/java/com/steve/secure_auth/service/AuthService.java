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

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, VerificationTokenRepository verificationTokenRepository, PasswordEncoder passwordEncoder, EmailService emailService, JwtProvider jwtService,
                       RefreshTokenService refreshTokenService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
    }

    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ⬇️ new dependencies for login/refresh/logout
    private final JwtProvider jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    // ================== REGISTER ==================
    public void register(RegisterRequest request) {
        // Create user (disabled until email verification)
        User user = new User();
        user.setFirstname(request.getFirstName());
        user.setLastname(request.getLastName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProfileImage(request.getProfileImage());
        user.setEnabled(false);

        // Assign default role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
        user.getRoles().add(userRole);

        userRepository.save(user);

        // Generate email verification token
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(24, ChronoUnit.HOURS);
        VerificationToken verificationToken = new VerificationToken(token, user, expiryDate);
        verificationTokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(user, token);
    }

    // ================== LOGIN ==================
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

        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getEmail(),
                user.getFirstname(),
                user.getLastname(),
                user.getProfileImage(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );

        return new AuthenticationResponse(accessToken, refreshToken, userInfo);
    }

    // ================== REFRESH ==================
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

    // ================== LOGOUT ==================
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
}
