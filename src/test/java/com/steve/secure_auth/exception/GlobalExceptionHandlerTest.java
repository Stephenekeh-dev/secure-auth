package com.steve.secure_auth.exception;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }


    //--------BadCredentialsException---------
    @Test
    @DisplayName("handleBadCredentials — returns 401 status")
    void handleBadCredentials_returns401() {
        ResponseEntity<?> response = handler.handleBadCredentials(
                new BadCredentialsException("Bad credentials"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("handleBadCredentials — returns generic error message")
    void handleBadCredentials_returnsGenericMessage() {
        ResponseEntity<?> response = handler.handleBadCredentials(
                new BadCredentialsException("Bad credentials"));

        assertThat(response.getBody()).isInstanceOf(Map.class);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body.get("error")).isEqualTo("Invalid email or password");
    }


    //----------UsernameNotFoundException-----------
    @Test
    @DisplayName("handleUserNotFound — returns 401 status")
    void handleUserNotFound_returns401() {
        ResponseEntity<?> response = handler.handleUserNotFound(
                new UsernameNotFoundException("User not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("handleUserNotFound — returns generic error message")
    void handleUserNotFound_returnsGenericMessage() {
        ResponseEntity<?> response = handler.handleUserNotFound(
                new UsernameNotFoundException("User not found"));

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body.get("error")).isEqualTo("Invalid email or password");
    }


    // --------IllegalStateException----------
    @Test
    @DisplayName("handleIllegalState — returns 400 status")
    void handleIllegalState_returns400() {
        ResponseEntity<?> response = handler.handleIllegalState(
                new IllegalStateException("Account not verified"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("handleIllegalState — returns exception message")
    void handleIllegalState_returnsExceptionMessage() {
        ResponseEntity<?> response = handler.handleIllegalState(
                new IllegalStateException("Account not verified"));

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body.get("error")).isEqualTo("Account not verified");
    }


    //--------- IllegalArgumentException ---------
    @Test
    @DisplayName("handleIllegalArgument — returns 400 status")
    void handleIllegalArgument_returns400() {
        ResponseEntity<?> response = handler.handleIllegalArgument(
                new IllegalArgumentException("Invalid token"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("handleIllegalArgument — returns exception message")
    void handleIllegalArgument_returnsExceptionMessage() {
        ResponseEntity<?> response = handler.handleIllegalArgument(
                new IllegalArgumentException("Invalid token"));

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body.get("error")).isEqualTo("Invalid token");
    }


    //------ RuntimeException------
    @Test
    @DisplayName("handleRuntime — returns 500 status")
    void handleRuntime_returns500() {
        ResponseEntity<?> response = handler.handleRuntime(
                new RuntimeException("Unexpected error"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("handleRuntime — returns exception message")
    void handleRuntime_returnsExceptionMessage() {
        ResponseEntity<?> response = handler.handleRuntime(
                new RuntimeException("Unexpected error"));

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body.get("error")).isEqualTo("Unexpected error");
    }
}
