package com.fieldservices.controller;

import com.fieldservices.dto.LoginRequest;
import com.fieldservices.dto.LoginResponse;
import com.fieldservices.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    public void testAuthenticateUser_Success() {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        LoginResponse loginResponse = new LoginResponse("jwt-token", "testuser", "test@example.com", "TECHNICIAN");

        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(loginResponse);

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LoginResponse body = (LoginResponse) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getToken()).isEqualTo("jwt-token");
        assertThat(body.getUsername()).isEqualTo("testuser");
    }

    @Test
    public void testAuthenticateUser_InvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        when(authService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testHealthCheck() {
        ResponseEntity<?> response = authController.healthCheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
