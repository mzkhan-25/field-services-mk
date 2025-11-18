package com.fieldservices.security;

import com.fieldservices.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private User testUser;
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "test-secret-key-for-testing-purposes-only-must-be-long-enough");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 3600000L);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.Role.TECHNICIAN);
        testUser.setActive(true);

        authentication = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
    }

    @Test
    public void testGenerateToken() {
        String token = tokenProvider.generateToken(authentication);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    public void testGetUsernameFromToken() {
        String token = tokenProvider.generateToken(authentication);

        String username = tokenProvider.getUsernameFromToken(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    public void testValidateToken_ValidToken() {
        String token = tokenProvider.generateToken(authentication);

        boolean isValid = tokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    public void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = tokenProvider.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    public void testValidateToken_EmptyToken() {
        boolean isValid = tokenProvider.validateToken("");

        assertThat(isValid).isFalse();
    }
}
