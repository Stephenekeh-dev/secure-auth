package com.steve.secure_auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  //
@DisplayName("RedisService Tests")
class RedisServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ─────────────────────────────────────────────
    // saveOtp
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("saveOtp — calls set with correct key, value and TTL")
    void saveOtp_callsSetWithCorrectArguments() {
        redisService.saveOtp("otp:user@example.com", "123456", 300L);

        verify(valueOperations, times(1))
                .set("otp:user@example.com", "123456", 300L, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("saveOtp — calls opsForValue once")
    void saveOtp_callsOpsForValueOnce() {
        redisService.saveOtp("otp:user@example.com", "123456", 300L);

        verify(redisTemplate, times(1)).opsForValue();
    }

    @Test
    @DisplayName("saveOtp — works with zero TTL")
    void saveOtp_withZeroTtl() {
        redisService.saveOtp("otp:user@example.com", "123456", 0L);

        verify(valueOperations).set("otp:user@example.com", "123456", 0L, TimeUnit.SECONDS);
    }

    // ─────────────────────────────────────────────
    // getOtp
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("getOtp — returns value from Redis")
    void getOtp_returnsValue() {
        when(valueOperations.get("otp:user@example.com")).thenReturn("123456");

        String result = redisService.getOtp("otp:user@example.com");

        assertThat(result).isEqualTo("123456");
    }

    @Test
    @DisplayName("getOtp — returns null when key does not exist")
    void getOtp_returnsNullWhenKeyNotFound() {
        when(valueOperations.get("otp:missing")).thenReturn(null);

        String result = redisService.getOtp("otp:missing");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getOtp — calls opsForValue once")
    void getOtp_callsOpsForValueOnce() {
        when(valueOperations.get(anyString())).thenReturn("123456");

        redisService.getOtp("otp:user@example.com");

        verify(redisTemplate, times(1)).opsForValue();
    }

    // ─────────────────────────────────────────────
    // deleteOtp
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("deleteOtp — calls redisTemplate.delete with correct key")
    void deleteOtp_callsDeleteWithCorrectKey() {
        redisService.deleteOtp("otp:user@example.com");

        verify(redisTemplate, times(1)).delete("otp:user@example.com");
    }

    @Test
    @DisplayName("deleteOtp — does not call opsForValue")
    void deleteOtp_doesNotCallOpsForValue() {
        redisService.deleteOtp("otp:user@example.com");

        verify(redisTemplate, never()).opsForValue();
    }

    // ─────────────────────────────────────────────
    // round-trip
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("saveOtp then getOtp — round trip returns saved value")
    void saveAndGet_roundTrip() {
        when(valueOperations.get("otp:user@example.com")).thenReturn("654321");

        redisService.saveOtp("otp:user@example.com", "654321", 60L);
        String result = redisService.getOtp("otp:user@example.com");

        assertThat(result).isEqualTo("654321");
        verify(valueOperations).set("otp:user@example.com", "654321", 60L, TimeUnit.SECONDS);
        verify(valueOperations).get("otp:user@example.com");
    }
}

