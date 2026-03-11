package com.steve.secure_auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steve.secure_auth.config.JwtAuthenticationFilter;
import com.steve.secure_auth.config.TestExceptionHandler;
import com.steve.secure_auth.dto.AuthRequest;
import com.steve.secure_auth.dto.AuthenticationResponse;
import com.steve.secure_auth.dto.RefreshRequest;
import com.steve.secure_auth.dto.RegisterRequest;
import com.steve.secure_auth.service.AuthService;
import com.steve.secure_auth.service.CustomUserDetailsService;
import com.steve.secure_auth.util.JwtProvider;
import config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ─────────────────────────────────────────────
    // POST /register
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /register — returns 200 with success message")
    void register_validRequest_returns200() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "stevejobs", "steve@example.com", "SecurePass123!",
                "Steve", "Jobs", null);

        doNothing().when(authService).register(any());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "User registered successfully. Please check your email to verify your account."));
    }

    @Test
    @DisplayName("POST /register — returns 500 when service throws")
    void register_serviceThrows_returns500() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "stevejobs", "steve@example.com", "pass",
                "Steve", "Jobs", null);

        doThrow(new RuntimeException("ROLE_USER not found")).when(authService).register(any());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    // ─────────────────────────────────────────────
    // GET /verify-email
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /verify-email — returns 200 on valid token")
    void verifyEmail_validToken_returns200() throws Exception {
        doNothing().when(authService).verifyEmail("valid-token");

        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "Email verified successfully. You can now log in."));
    }

    @Test
    @DisplayName("GET /verify-email — returns 500 when token invalid")
    void verifyEmail_invalidToken_returns500() throws Exception {
        doThrow(new RuntimeException("Invalid or unrecognized token"))
                .when(authService).verifyEmail("bad-token");

        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", "bad-token"))
                .andExpect(status().is5xxServerError());
    }

    // ─────────────────────────────────────────────
    // POST /login
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /login — returns 200 with tokens and user info")
    void login_validCredentials_returns200() throws Exception {
        AuthRequest request = new AuthRequest("steve@example.com", "SecurePass123!", null);

        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .user(AuthenticationResponse.UserInfo.builder()
                        .id(1L)
                        .email("steve@example.com")
                        .firstName("Steve")
                        .lastName("Jobs")
                        .roles(Set.of("ROLE_USER"))
                        .build())
                .build();

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.user.email").value("steve@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("Steve"));
    }

    @Test
    @DisplayName("POST /login — returns 500 when credentials are wrong")
    void login_badCredentials_returns500() throws Exception {
        AuthRequest request = new AuthRequest("steve@example.com", "wrongpass", null);
        when(authService.login(any()))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    // ─────────────────────────────────────────────
    // POST /refresh
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /refresh — returns 200 with new access token")
    void refresh_validToken_returns200() throws Exception {
        RefreshRequest request = new RefreshRequest("valid-refresh-token");

        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("valid-refresh-token")
                .user(AuthenticationResponse.UserInfo.builder()
                        .id(1L)
                        .email("steve@example.com")
                        .firstName("Steve")
                        .lastName("Jobs")
                        .roles(Set.of("ROLE_USER"))
                        .build())
                .build();

        when(authService.refresh(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    @DisplayName("POST /refresh — returns 500 when refresh token is invalid")
    void refresh_invalidToken_returns500() throws Exception {
        RefreshRequest request = new RefreshRequest("bad-token");
        when(authService.refresh(any()))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    // ─────────────────────────────────────────────
    // POST /logout
    // ─────────────────────────────────────────────

    @Test
    void logout_authenticated_returns200() throws Exception {
        when(authService.logout(anyString(), any()))
                .thenReturn(Map.of("message", "Logged out"));

        mockMvc.perform(post("/api/auth/logout")
                        .param("email", "test@test.com")  //  no Authentication needed
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out"));
    }
    // ─────────────────────────────────────────────
    // POST /forgot-password
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /forgot-password — returns 200 with generic message")
    void forgotPassword_returns200() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"steve@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "If this email exists, a reset link has been sent."));
    }
}