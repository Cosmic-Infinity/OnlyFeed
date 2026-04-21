package com.onlyfeed.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.onlyfeed.api.dto.auth.AuthResponse;
import com.onlyfeed.api.dto.auth.RegisterRequest;
import com.onlyfeed.api.entity.UserAccount;
import com.onlyfeed.api.exception.ApiException;
import com.onlyfeed.api.repository.UserRepository;
import com.onlyfeed.api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtService = Mockito.mock(JwtService.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerThrowsWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("alice", "password123");
        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(true);

        assertThrows(ApiException.class, () -> authService.register(request));
    }

    @Test
    void registerReturnsTokenWhenSuccess() {
        RegisterRequest request = new RegisterRequest("alice", "password123");

        UserAccount saved = new UserAccount();
        saved.setId(1L);
        saved.setUsername("alice");
        saved.setPasswordHash("hash");

        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hash");
        when(userRepository.save(any(UserAccount.class))).thenReturn(saved);
        when(jwtService.generateToken(any())).thenReturn("token");

        AuthResponse response = authService.register(request);

        assertEquals(1L, response.userId());
        assertEquals("alice", response.username());
        assertEquals("token", response.token());
    }
}
