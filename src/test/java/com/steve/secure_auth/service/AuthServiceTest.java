package com.steve.secure_auth.service;

import com.steve.secure_auth.dto.AuthRequest;
import com.steve.secure_auth.dto.AuthenticationResponse;
import com.steve.secure_auth.dto.RefreshRequest;
import com.steve.secure_auth.dto.RegisterRequest;
import com.steve.secure_auth.model.RefreshToken;
import com.steve.secure_auth.model.Role;
import com.steve.secure_auth.model.User;
import com.steve.secure_auth.model.VerificationToken;
import com.steve.secure_auth.repository.RoleRepository;
import com.steve.secure_auth.repository.UserRepository;
import com.steve.secure_auth.repository.VerificationTokenRepository;
import com.steve.secure_auth.util.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private VerificationTokenRepository verificationTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private JwtProvider jwtProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role("ROLE_USER");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("steve@example.com");
        testUser.setUsername("stevejobs");
        testUser.setFirstname("Steve");
        testUser.setLastname("Jobs");
        testUser.setPassword("$2a$encoded");
        testUser.setEnabled(true);
        testUser.setRoles(new HashSet<>(Set.of(userRole)));
    }

    // ─────────────────────────────────────────────
    // register
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("register — saves user with encoded password")
    void register_savesUserWithEncodedPassword() {
        RegisterRequest request = buildRegisterRequest();
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$encodedPassword");

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("$2a$encodedPassword");
    }

    @Test
    @DisplayName("register — saves user with enabled=false")
    void register_savesUserAsDisabled() {
        RegisterRequest request = buildRegisterRequest();
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isEnabled()).isFalse();
    }

    @Test
    @DisplayName("register — assigns ROLE_USER to new user")
    void register_assignsDefaultRole() {
        RegisterRequest request = buildRegisterRequest();
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRoles()).contains(userRole);
    }

    @Test
    @DisplayName("register — saves a verification token")
    void register_savesVerificationToken() {
        RegisterRequest request = buildRegisterRequest();
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        authService.register(request);

        verify(verificationTokenRepository).save(any(VerificationToken.class));
    }

    @Test
    @DisplayName("register — sends verification email")
    void register_sendsVerificationEmail() {
        RegisterRequest request = buildRegisterRequest();
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        authService.register(request);

        verify(emailService).sendVerificationEmail(any(User.class), anyString());
    }

    @Test
    @DisplayName("register — throws RuntimeException when ROLE_USER not found")
    void register_roleNotFound_throwsException() {
        RegisterRequest request = buildRegisterRequest();
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ROLE_USER not found");
    }

    // ─────────────────────────────────────────────
    // login
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("login — returns AuthenticationResponse with tokens and user info")
    void login_validCredentials_returnsAuthResponse() {
        AuthRequest request = new AuthRequest("steve@example.com", "rawPassword");
        when(userRepository.findByEmail("steve@example.com")).thenReturn(Optional.of(testUser));
        when(jwtProvider.generateToken("steve@example.com")).thenReturn("access-token");
        RefreshToken rt = buildRefreshToken("refresh-token");
        when(refreshTokenService.create(testUser)).thenReturn(rt);

        AuthenticationResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser().getEmail()).isEqualTo("steve@example.com");
    }

    @Test
    @DisplayName("login — calls authenticationManager.authenticate")
    void login_callsAuthenticationManager() {
        AuthRequest request = new AuthRequest("steve@example.com", "rawPassword");
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(jwtProvider.generateToken(any())).thenReturn("token");
        when(refreshTokenService.create(any())).thenReturn(buildRefreshToken("rt"));

        authService.login(request);

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("login — throws IllegalStateException when user is not enabled")
    void login_userNotEnabled_throwsException() {
        testUser.setEnabled(false);
        AuthRequest request = new AuthRequest("steve@example.com", "rawPassword");
        when(userRepository.findByEmail("steve@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("verify your email");
    }

    @Test
    @DisplayName("login — throws UsernameNotFoundException when user not found")
    void login_userNotFound_throwsException() {
        AuthRequest request = new AuthRequest("ghost@example.com", "pass");
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("login — throws BadCredentialsException when password is wrong")
    void login_wrongPassword_throwsException() {
        AuthRequest request = new AuthRequest("steve@example.com", "wrongPass");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login — response includes user roles")
    void login_responseContainsRoles() {
        AuthRequest request = new AuthRequest("steve@example.com", "pass");
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(jwtProvider.generateToken(any())).thenReturn("token");
        when(refreshTokenService.create(any())).thenReturn(buildRefreshToken("rt"));

        AuthenticationResponse response = authService.login(request);

        assertThat(response.getUser().getRoles()).contains("ROLE_USER");
    }

    // ─────────────────────────────────────────────
    // refresh
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("refresh — returns new access token with same refresh token")
    void refresh_validToken_returnsNewAccessToken() {
        RefreshToken rt = buildRefreshToken("valid-refresh-token");
        rt.setUser(testUser);
        when(refreshTokenService.validate("valid-refresh-token")).thenReturn(rt);
        when(jwtProvider.generateToken("steve@example.com")).thenReturn("new-access-token");

        RefreshRequest request = new RefreshRequest("valid-refresh-token");
        AuthenticationResponse response = authService.refresh(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
    }

    @Test
    @DisplayName("refresh — throws when refresh token is invalid")
    void refresh_invalidToken_throwsException() {
        when(refreshTokenService.validate("bad-token"))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("bad-token")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    // ─────────────────────────────────────────────
    // logout
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("logout — revokes specific refresh token when provided")
    void logout_withRefreshToken_revokesToken() {
        when(userRepository.findByEmail("steve@example.com")).thenReturn(Optional.of(testUser));

        authService.logout("steve@example.com", "some-refresh-token");

        verify(refreshTokenService).revokeByToken("some-refresh-token");
        verify(refreshTokenService, never()).revokeByUser(any());
    }

    @Test
    @DisplayName("logout — revokes all tokens for user when no refresh token provided")
    void logout_withoutRefreshToken_revokesAllUserTokens() {
        when(userRepository.findByEmail("steve@example.com")).thenReturn(Optional.of(testUser));

        authService.logout("steve@example.com", null);

        verify(refreshTokenService).revokeByUser(testUser);
        verify(refreshTokenService, never()).revokeByToken(any());
    }

    @Test
    @DisplayName("logout — returns message map")
    void logout_returnsMessage() {
        when(userRepository.findByEmail("steve@example.com")).thenReturn(Optional.of(testUser));

        Map<String, String> result = authService.logout("steve@example.com", "token");

        assertThat(result).containsEntry("message", "Logged out");
    }

    @Test
    @DisplayName("logout — does nothing gracefully when user not found")
    void logout_userNotFound_doesNothing() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatCode(() -> authService.logout("ghost@example.com", "token"))
                .doesNotThrowAnyException();

        verify(refreshTokenService, never()).revokeByToken(any());
        verify(refreshTokenService, never()).revokeByUser(any());
    }

    // ─────────────────────────────────────────────
    // verifyEmail
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("verifyEmail — enables user and deletes token on success")
    void verifyEmail_validToken_enablesUser() {
        testUser.setEnabled(false);
        VerificationToken vt = new VerificationToken("valid-token", testUser,
                Instant.now().plusSeconds(3600));
        when(verificationTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(vt));

        authService.verifyEmail("valid-token");

        assertThat(testUser.isEnabled()).isTrue();
        verify(userRepository).save(testUser);
        verify(verificationTokenRepository).delete(vt);
    }

    @Test
    @DisplayName("verifyEmail — throws RuntimeException for unknown token")
    void verifyEmail_unknownToken_throwsException() {
        when(verificationTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail("bad-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    @DisplayName("verifyEmail — throws IllegalStateException for expired token")
    void verifyEmail_expiredToken_throwsException() {
        VerificationToken expired = new VerificationToken("expired", testUser,
                Instant.now().minusSeconds(1));
        when(verificationTokenRepository.findByToken("expired")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.verifyEmail("expired"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("verifyEmail — throws IllegalStateException when already verified")
    void verifyEmail_alreadyEnabled_throwsException() {
        testUser.setEnabled(true); // already verified
        VerificationToken vt = new VerificationToken("token", testUser,
                Instant.now().plusSeconds(3600));
        when(verificationTokenRepository.findByToken("token")).thenReturn(Optional.of(vt));

        assertThatThrownBy(() -> authService.verifyEmail("token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already verified");
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private RegisterRequest buildRegisterRequest() {
        return new RegisterRequest("stevejobs", "steve@example.com",
                "rawPassword", "Steve", "Jobs", null);
    }

    private RefreshToken buildRefreshToken(String token) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(token);
        rt.setUser(testUser);
        rt.setExpiryDate(Instant.now().plusSeconds(3600));
        return rt;
    }
}