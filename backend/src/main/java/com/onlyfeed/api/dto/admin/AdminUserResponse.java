package com.onlyfeed.api.dto.admin;

import java.time.Instant;

public record AdminUserResponse(Long id, String username, String role, Instant createdAt) {
}