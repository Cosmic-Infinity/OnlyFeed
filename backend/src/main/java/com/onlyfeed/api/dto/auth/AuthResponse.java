package com.onlyfeed.api.dto.auth;

public record AuthResponse(Long userId, String username, String role, String token) {
}
