package com.steve.secure_auth.service;

import com.steve.secure_auth.model.RefreshToken;
import com.steve.secure_auth.model.User;

import com.steve.secure_auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Tests")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    // ─────────────────────────────────────────────
    // create
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("create — deletes existing token for user before creating new one")
    void create_deletesExistingTokenFirst() {
        User user = new User();
        user.setEmail("steve@example.com");

        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        refreshTokenService.create(user);

        verify(refreshTokenRepository, times(1)).deleteByUser(user);
    }

    @Test
    @DisplayName("create — saves new token with correct user")
    void create_savesTokenWithCorrectUser() {
        User user = new User();
        user.setEmail("steve@example.com");

        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = refreshTokenService.create(user);

        assertThat(result.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("create — generates non-null UUID token string")
    void create_generatesNonNullUuidToken() {
        User user = new User();
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = refreshTokenService.create(user);

        assertThat(result.getToken()).isNotNull();
        assertThat(result.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("create — sets expiry date approximately 30 days from now")
    void create_setsExpiryDate30DaysFromNow() {
        User user = new User();
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = refreshTokenService.create(user);

        Instant expectedExpiry = Instant.now().plus(30, ChronoUnit.DAYS);
        assertThat(result.getExpiryDate())
                .isAfter(expectedExpiry.minus(5, ChronoUnit.SECONDS))
                .isBefore(expectedExpiry.plus(5, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("create — returns the saved token from repository")
    void create_returnsTokenFromRepository() {
        User user = new User();
        RefreshToken saved = new RefreshToken();
        saved.setToken("saved-token");
        saved.setUser(user);
        saved.setExpiryDate(Instant.now().plus(30, ChronoUnit.DAYS));

        when(refreshTokenRepository.save(any())).thenReturn(saved);

        RefreshToken result = refreshTokenService.create(user);

        assertThat(result).isEqualTo(saved);
    }

    // ─────────────────────────────────────────────
    // validate
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("validate — returns token when valid and not expired")
    void validate_returnsTokenWhenValid() {
        RefreshToken rt = new RefreshToken();
        rt.setToken("valid-token");
        rt.setExpiryDate(Instant.now().plus(1, ChronoUnit.DAYS));

        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(rt));

        RefreshToken result = refreshTokenService.validate("valid-token");

        assertThat(result).isEqualTo(rt);
    }

    @Test
    @DisplayName("validate — throws IllegalArgumentException when token not found")
    void validate_throwsWhenTokenNotFound() {
        when(refreshTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validate("bad-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    @DisplayName("validate — throws IllegalArgumentException when token is expired")
    void validate_throwsWhenTokenExpired() {
        RefreshToken rt = new RefreshToken();
        rt.setToken("expired-token");
        rt.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS));

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> refreshTokenService.validate("expired-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token expired");
    }

    @Test
    @DisplayName("validate — deletes expired token from repository")
    void validate_deletesExpiredToken() {
        RefreshToken rt = new RefreshToken();
        rt.setToken("expired-token");
        rt.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS));

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> refreshTokenService.validate("expired-token"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(refreshTokenRepository, times(1)).delete(rt);
    }

    // ─────────────────────────────────────────────
    // revokeByUser
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("revokeByUser — calls deleteByUser with correct user")
    void revokeByUser_callsDeleteByUser() {
        User user = new User();
        user.setEmail("steve@example.com");

        refreshTokenService.revokeByUser(user);

        verify(refreshTokenRepository, times(1)).deleteByUser(user);
    }

    // ─────────────────────────────────────────────
    // revokeByToken
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("revokeByToken — deletes token when found")
    void revokeByToken_deletesWhenFound() {
        RefreshToken rt = new RefreshToken();
        rt.setToken("some-token");

        when(refreshTokenRepository.findByToken("some-token")).thenReturn(Optional.of(rt));

        refreshTokenService.revokeByToken("some-token");

        verify(refreshTokenRepository, times(1)).delete(rt);
    }

    @Test
    @DisplayName("revokeByToken — does nothing when token not found")
    void revokeByToken_doesNothingWhenNotFound() {
        when(refreshTokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

        refreshTokenService.revokeByToken("missing-token");

        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }
}