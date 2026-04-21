package com.onlyfeed.api.service;

import com.onlyfeed.api.dto.auth.AuthResponse;
import com.onlyfeed.api.dto.auth.LoginRequest;
import com.onlyfeed.api.dto.auth.RegisterRequest;
import com.onlyfeed.api.entity.UserAccount;
import com.onlyfeed.api.entity.UserRole;
import com.onlyfeed.api.exception.ApiException;
import com.onlyfeed.api.repository.UserRepository;
import com.onlyfeed.api.security.AppUserPrincipal;
import com.onlyfeed.api.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already taken");
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setRole(UserRole.USER);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        UserAccount saved = userRepository.save(user);

        AppUserPrincipal principal = AppUserPrincipal.from(saved);
        return new AuthResponse(saved.getId(), saved.getUsername(), effectiveRole(saved),
                jwtService.generateToken(principal));
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        AppUserPrincipal principal = AppUserPrincipal.from(user);
        return new AuthResponse(user.getId(), user.getUsername(), effectiveRole(user),
                jwtService.generateToken(principal));
    }

    private String effectiveRole(UserAccount user) {
        return (user.getRole() == null ? UserRole.USER : user.getRole()).name();
    }
}
